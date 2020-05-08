 /* Copyright (c) 2011 TOPP - www.openplans.org. All rights reserved.
  * This code is licensed under the LGPL 2.1 license, available at the root
  * application directory.
  */
 package org.geogit.cli.porcelain;
 
 import static com.google.common.base.Preconditions.checkArgument;
 import static com.google.common.base.Preconditions.checkState;
 
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.InputStreamReader;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map.Entry;
 import java.util.Set;
 
 import jline.console.ConsoleReader;
 
 import org.geogit.api.GeoGIT;
 import org.geogit.api.NodeRef;
 import org.geogit.api.ObjectId;
 import org.geogit.api.Ref;
 import org.geogit.api.RevFeature;
 import org.geogit.api.RevFeatureType;
 import org.geogit.api.RevObject;
 import org.geogit.api.plumbing.RevObjectParse;
 import org.geogit.api.plumbing.diff.AttributeDiff;
 import org.geogit.api.plumbing.diff.FeatureDiff;
 import org.geogit.api.plumbing.diff.FeatureTypeDiff;
 import org.geogit.api.plumbing.diff.Patch;
 import org.geogit.api.plumbing.diff.PatchFeature;
 import org.geogit.api.plumbing.diff.PatchSerializer;
 import org.geogit.api.porcelain.ApplyPatchOp;
 import org.geogit.api.porcelain.CannotApplyPatchException;
 import org.geogit.cli.AbstractCommand;
 import org.geogit.cli.GeogitCLI;
 import org.geogit.repository.DepthSearch;
 import org.opengis.feature.type.PropertyDescriptor;
 
 import com.beust.jcommander.Parameter;
 import com.beust.jcommander.Parameters;
 import com.google.common.base.Charsets;
 import com.google.common.base.Objects;
 import com.google.common.base.Optional;
 import com.google.common.collect.ImmutableList;
 import com.google.common.io.Files;
 
 /**
  * Applies a patch that modifies the current working tree.
  * 
  * Patches are generated using the format-patch command, not with the diff command
  * 
  */
 @Parameters(commandNames = "apply", commandDescription = "Apply a patch to the current working tree")
 public class Apply extends AbstractCommand {
 
     /**
      * The path to the patch file
      */
     @Parameter(description = "<patch>")
     private List<String> patchFiles = new ArrayList<String>();
 
     /**
      * Check if patch can be applied
      */
     @Parameter(names = { "--check" }, description = "Do not apply. Just check that patch can be applied")
     private boolean check;
 
     @Parameter(names = { "--reverse" }, description = "apply the patch in reverse")
     private boolean reverse;
 
     /**
      * Apply directly on index, not on working tree. (temporarily disabled)
      */
     // @Parameter(names = { "--cached" }, description =
     // "Apply directly on index, not on working tree")
     // private boolean cached;
 
     /**
      * Whether to apply the patch partially and generate new patch file with rejected changes, or
      * try to apply the whole patch
      */
     @Parameter(names = { "--reject" }, description = "Apply the patch partially and generate new patch file with rejected changes")
     private boolean reject;
 
     @Parameter(names = { "--summary" }, description = "Do not apply. Just show a summary of changes contained in the patch")
     private boolean summary;
 
     /**
      * @param cli
      * @see org.geogit.cli.CLICommand#run(org.geogit.cli.GeogitCLI)
      */
     @Override
     public void runInternal(GeogitCLI cli) throws Exception {
         checkState(cli.getGeogit() != null, "Not a geogit repository: " + cli.getPlatform().pwd());
         checkArgument(patchFiles.size() < 2, "Only one single patch file accepted");
         checkArgument(!patchFiles.isEmpty(), "No patch file specified");
 
         ConsoleReader console = cli.getConsole();
         GeoGIT geogit = cli.getGeogit();
 
         File patchFile = new File(patchFiles.get(0));
         checkArgument(patchFile.exists(), "Patch file cannot be found");
         FileInputStream stream = new FileInputStream(patchFile);
         BufferedReader reader = new BufferedReader(new InputStreamReader(stream, "UTF-8"));
         Patch patch = PatchSerializer.read(reader);
         reader.close();
         stream.close();
 
         if (reverse) {
             patch = patch.reversed();
         }
 
         if (summary) {
             console.println(patch.toString());
         } else if (check) {
             Patch applicable = new Patch();
             Patch rejected = new Patch();
             checkPatch(geogit, patch, applicable, rejected);
             if (rejected.isEmpty()) {
                 console.println("Patch can be applied.");
             } else {
                 console.println("Error: Patch cannot be applied\n");
                 console.println("Applicable entries:\n");
                 console.println(applicable.toString());
                 console.println("\nConflicting entries:\n");
                 console.println(rejected.toString());
             }
         } else {
             try {
                 Patch rejected = geogit.command(ApplyPatchOp.class).setPatch(patch)
                         .setApplyPartial(reject).call();
                 if (reject) {
                     if (rejected.isEmpty()) {
                         console.println("Patch applied succesfully");
                     } else {
                         int accepted = patch.count() - rejected.count();
                         File file = new File(patchFile.getAbsolutePath() + ".rej");
                         console.println("Patch applied only partially.");
                         console.println(Integer.toString(accepted) + " changes were applied.");
                         console.println(Integer.toString(rejected.count())
                                 + " changes were rejected.");
                         BufferedWriter writer = Files.newWriter(file, Charsets.UTF_8);
                         PatchSerializer.write(writer, patch);
                         writer.flush();
                         writer.close();
                         console.println("Patch file with rejected changes created at "
                                 + file.getAbsolutePath());
                     }
                 } else {
                     console.println("Patch applied succesfully");
                 }
             } catch (CannotApplyPatchException e) {
                 console.println(e.getMessage());
             }
 
         }
 
     }
 
     private void checkPatch(GeoGIT geogit, Patch originalPatch, Patch toApply, Patch rejected) {
         for (RevFeatureType ft : originalPatch.getFeatureTypes()) {
             toApply.addFeatureType(ft);
             rejected.addFeatureType(ft);
         }
         String path;
         Optional<RevObject> obj;
         List<FeatureDiff> diffs = originalPatch.getModifiedFeatures();
         for (FeatureDiff diff : diffs) {
             path = diff.getPath();
             String refSpec = Ref.WORK_HEAD + ":" + path;
             obj = geogit.command(RevObjectParse.class).setRefSpec(refSpec).call();
             if (!obj.isPresent()) {
                 rejected.addModifiedFeature(diff);
                 continue;
             }
             RevFeature feature = (RevFeature) obj.get();
            DepthSearch depthSearch = new DepthSearch(geogit.getRepository().getObjectDatabase());
             Optional<NodeRef> noderef = depthSearch.find(geogit.getRepository().getWorkingTree()
                     .getTree(), path);
             RevFeatureType featureType = geogit.command(RevObjectParse.class)
                     .setObjectId(noderef.get().getMetadataId()).call(RevFeatureType.class).get();
             ImmutableList<PropertyDescriptor> descriptors = featureType.sortedDescriptors();
             Set<Entry<PropertyDescriptor, AttributeDiff>> attrDiffs = diff.getDiffs().entrySet();
             boolean ok = true;
             for (Iterator<Entry<PropertyDescriptor, AttributeDiff>> iterator = attrDiffs.iterator(); iterator
                     .hasNext();) {
                 Entry<PropertyDescriptor, AttributeDiff> entry = iterator.next();
                 AttributeDiff attrDiff = entry.getValue();
                 PropertyDescriptor descriptor = entry.getKey();
                 switch (attrDiff.getType()) {
                 case ADDED:
                     if (descriptors.contains(descriptor)) {
                         ok = false;
                     }
                     break;
                 case REMOVED:
                 case MODIFIED:
                     if (!descriptors.contains(descriptor)) {
                         ok = false;
                         break;
                     }
                     for (int i = 0; i < descriptors.size(); i++) {
                         if (descriptors.get(i).equals(descriptor)) {
                             Optional<Object> value = feature.getValues().get(i);
                             if (!attrDiff.canBeAppliedOn(value)) {
                                 ok = false;
                             }
                             break;
                         }
                     }
                 }
                 if (!ok) {
                     break;
                 }
             }
             if (!ok) {
                 rejected.addModifiedFeature(diff);
             } else {
                 toApply.addModifiedFeature(diff);
             }
         }
         List<PatchFeature> added = originalPatch.getAddedFeatures();
         for (PatchFeature feature : added) {
             String refSpec = Ref.WORK_HEAD + ":" + feature.getPath();
             obj = geogit.command(RevObjectParse.class).setRefSpec(refSpec).call();
             if (obj.isPresent()) {
                 rejected.addAddedFeature(feature.getPath(), feature.getFeature(),
                         feature.getFeatureType());
             } else {
                 toApply.addAddedFeature(feature.getPath(), feature.getFeature(),
                         feature.getFeatureType());
             }
 
         }
 
         List<PatchFeature> removed = originalPatch.getRemovedFeatures();
         for (PatchFeature feature : removed) {
             String refSpec = Ref.WORK_HEAD + ":" + feature.getPath();
             obj = geogit.command(RevObjectParse.class).setRefSpec(refSpec).call();
             if (!obj.isPresent()) {
                 rejected.addRemovedFeature(feature.getPath(), feature.getFeature(),
                         feature.getFeatureType());
             } else {
                 RevFeature revFeature = (RevFeature) obj.get();
                DepthSearch depthSearch = new DepthSearch(geogit.getRepository()
                        .getObjectDatabase());
                 Optional<NodeRef> noderef = depthSearch.find(geogit.getRepository()
                         .getWorkingTree().getTree(), feature.getPath());
                 RevFeatureType revFeatureType = geogit.command(RevObjectParse.class)
                         .setObjectId(noderef.get().getMetadataId()).call(RevFeatureType.class)
                         .get();
                 if (revFeature.equals(feature.getFeature())
                         && revFeatureType.equals(feature.getFeatureType())) {
                     toApply.addRemovedFeature(feature.getPath(), feature.getFeature(),
                             feature.getFeatureType());
                 } else {
                     rejected.addRemovedFeature(feature.getPath(), feature.getFeature(),
                             feature.getFeatureType());
                 }
             }
         }
 
         ImmutableList<FeatureTypeDiff> alteredTrees = originalPatch.getAlteredTrees();
         for (FeatureTypeDiff diff : alteredTrees) {
             DepthSearch depthSearch = new DepthSearch(geogit.getRepository().getObjectDatabase());
             Optional<NodeRef> noderef = depthSearch.find(geogit.getRepository().getWorkingTree()
                     .getTree(), diff.getPath());
             ObjectId metadataId = noderef.isPresent() ? noderef.get().getMetadataId()
                     : ObjectId.NULL;
             if (Objects.equal(metadataId, diff.getOldFeatureType())) {
                 toApply.addAlteredTree(diff);
             } else {
                 rejected.addAlteredTree(diff);
             }
         }
 
     }
 }
