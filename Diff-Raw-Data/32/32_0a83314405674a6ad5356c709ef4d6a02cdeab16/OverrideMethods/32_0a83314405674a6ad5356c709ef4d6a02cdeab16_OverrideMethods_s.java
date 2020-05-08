 package org.eclipse.recommenders.livedoc.taglets.overrides;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.Iterator;
 import java.util.List;
 
 import org.eclipse.recommenders.livedoc.javadoc.RecommendersAetherTaglet;
 import org.eclipse.recommenders.livedoc.utils.LiveDocUtils;
import org.eclipse.recommenders.models.BasedTypeName;
 import org.eclipse.recommenders.models.ProjectCoordinate;
 import org.eclipse.recommenders.overrides.IOverrideModel;
 import org.eclipse.recommenders.overrides.SingleZipOverrideModelProvider;
 import org.eclipse.recommenders.utils.Recommendation;
 import org.eclipse.recommenders.utils.Recommendations;
 import org.eclipse.recommenders.utils.names.IMethodName;
 import org.eclipse.recommenders.utils.names.ITypeName;
 import org.eclipse.recommenders.utils.names.VmTypeName;
 import org.sonatype.aether.artifact.Artifact;
 
 import com.google.common.base.Optional;
 import com.google.common.collect.ImmutableSet;
 import com.sun.javadoc.ClassDoc;
 import com.sun.javadoc.Doc;
 import com.sun.javadoc.MethodDoc;
 import com.sun.javadoc.PackageDoc;
 import com.sun.javadoc.Tag;
 import com.sun.tools.doclets.internal.toolkit.taglets.TagletOutput;
 import com.sun.tools.doclets.internal.toolkit.taglets.TagletWriter;
 
 public class OverrideMethods extends RecommendersAetherTaglet {
     
     private SingleZipOverrideModelProvider modelProvider;
     private final int ranking = 10;    
     public void initialize() {
 
         Artifact artifact = downloadModelsArtifact("ovrm");
 
         File models = artifact.getFile();
         modelProvider = new SingleZipOverrideModelProvider(models);
 
         try {
             modelProvider.open();
         } catch (IOException e) {
             e.printStackTrace();
         }
     }
 
     @Override
     public boolean inMethod() {
         return true;
     }
 
     @Override
     public boolean inPackage() {
         return true;
     }
 
     @Override
     public boolean inType() {
         return true;
     }
 
     @Override
     public String getName() {
         return "ovrm";
     }
 
     @Override
     public TagletOutput getTagletOutput(Tag tag, TagletWriter writer) throws IllegalArgumentException {
         throw new UnsupportedOperationException();
     }
 
     @Override
     public TagletOutput getTagletOutput(Doc holder, TagletWriter writer) throws IllegalArgumentException {
 
         if (holder.isOrdinaryClass()) {
             return generateClassDoc((ClassDoc) holder, writer);
         } else if (holder.isMethod() && !((MethodDoc) holder).isAbstract()) {
             return generateMethodDoc((MethodDoc) holder, writer);
         } else if (holder instanceof PackageDoc) {
             return generatePackageDoc((PackageDoc) holder, writer);
         }
         return null;
     }
 
     private TagletOutput generatePackageDoc(PackageDoc packageDoc, TagletWriter writer) {
 
         StringBuilder sb = new StringBuilder();
 
         sb.append("<h2 style=\"font-size:200%; color:blue\" title=")
         .append("\"Code Recommenders\">")
         .append("Code Recommenders")
         .append("</h2>")
         .append("For the following types exists overrides statistics from Code Recommenders:")
         .append("<ul>");
 
         for (ClassDoc classDoc : packageDoc.allClasses()) {
             ITypeName typeName = VmTypeName.get(LiveDocUtils.extractTypeName(classDoc));
             Optional<IOverrideModel> model = ovrmModel(typeName);
 
             if (model.isPresent()){
                 sb.append("<li>")
                     .append(model.get().getType().getClassName())
                     .append("</li>");
             }
         }
 
         sb.append("</ul>");
 
         TagletOutput output = writer.getOutputInstance();
         output.setOutput(sb.toString());
         return output;
     }
 
     private TagletOutput generateClassDoc(ClassDoc holder, TagletWriter writer) {
 
         ITypeName typeName = VmTypeName.get(LiveDocUtils.extractTypeName(holder));
         Optional<IOverrideModel> model = ovrmModel(typeName);
         
         StringBuilder sb = new StringBuilder();
         
         if (model.isPresent()) {
             List<Recommendation<IMethodName>> recommendOverrides = model.get().recommendOverrides();
 
             Collections.sort(recommendOverrides, new Comparator<Recommendation<IMethodName>>() {
 
                 @Override
                 public int compare(Recommendation<IMethodName> o1, Recommendation<IMethodName> o2) {
                     return (Recommendations.asPercentage(o2) - Recommendations.asPercentage(o1));
                 }
             });
 
             sb.append("<h5>Method override recommendations</h5>")
             .append("The following methods are frequently overriden by subclasses of ")
             .append("<code>")
             .append(typeName.getClassName())
             .append("</code>")
             .append(":")
             .append("<br>")
             .append("<br>")
             .append("<ul>");
 
             for (Iterator<Recommendation<IMethodName>> iterator = recommendOverrides.iterator(); iterator.hasNext();) {
 
                 Recommendation<IMethodName> recommendation = (Recommendation<IMethodName>) iterator.next();
 
                 int relevance = Recommendations.asPercentage(recommendation);
 
                 // sb.append("{@link #");
                 IMethodName method = recommendation.getProposal();
 
                 sb.append("<li>");
                 sb.append(LiveDocUtils.methodSignature(method));
                 // sb.append("}");
 
                 sb.append(" - ").append("<font color=\"#0000FF\">").append(relevance + "%").append("</font>");
 
                 if (iterator.hasNext()) {
                     sb.append(", ");
                 }
 
                 sb.append("</li>");
             }
             sb.append("</ul>");
         }
 
         TagletOutput output = writer.getOutputInstance();
         output.setOutput(sb.toString());
         return output;
     }
 
     private TagletOutput generateMethodDoc(MethodDoc methodDoc, TagletWriter writer) {
 
         IMethodName methodName = LiveDocUtils.asIMethodName(methodDoc);
 
         ITypeName methodDeclaringType = VmTypeName.get(LiveDocUtils.extractTypeName(methodDoc));
         Optional<IOverrideModel> optional = ovrmModel(methodDeclaringType);
 
         StringBuilder sb = new StringBuilder();
 
         if (optional.isPresent()) {
             IOverrideModel model = optional.get();
             ImmutableSet<IMethodName> knownMethods = model.getKnownMethods();
 
             if (knownMethods.contains(methodName)) {
 
                 int relevance = 0;
                 for (Recommendation<IMethodName> recommendation : model.recommendOverrides()) {
                     if (recommendation.getProposal().equals(methodName)) {
                         relevance = Recommendations.asPercentage(recommendation);
                         break;
                     }
                 }
                 sb.append("<dl>")
                     .append("<dt>Subclass overrides probability:</dt>")
                     .append("<dd>")
                     .append("<font color=\"#0000FF\">")
                     .append(relevance + "%")
                     .append("</font>")
                     .append("</dd>")
                     .append("</dl>");
             }
         }
 
         TagletOutput output = writer.getOutputInstance();
         output.setOutput(sb.toString());
         return output;
     }
     
     private Optional<IOverrideModel> ovrmModel(ITypeName typeName) {
         ProjectCoordinate coordinate = new ProjectCoordinate(groupId, artifactId, artifactVersion);
 
        BasedTypeName key = new BasedTypeName(coordinate, typeName);
 
         Optional<IOverrideModel> model = modelProvider.acquireModel(key);
         return model;
     }
     
     @Override
     protected int getRanking() {
         return ranking;
     };
 
     @Override
     public void finish() {
         try {
             modelProvider.close();
         } catch (IOException e) {
             e.printStackTrace();
         }
     }
 }
