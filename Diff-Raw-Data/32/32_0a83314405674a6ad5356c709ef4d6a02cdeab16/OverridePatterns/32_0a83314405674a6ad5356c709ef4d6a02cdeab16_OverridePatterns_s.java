 package org.eclipse.recommenders.livedoc.taglets.overrides;
 
 import static com.google.common.base.Optional.absent;
 import static com.google.common.base.Optional.of;
 import static org.eclipse.recommenders.utils.Checks.ensureIsInRange;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map.Entry;
 import java.util.zip.ZipEntry;
 import java.util.zip.ZipFile;
 
 import org.eclipse.recommenders.apidocs.ClassOverridePatterns;
 import org.eclipse.recommenders.apidocs.MethodPattern;
 import org.eclipse.recommenders.livedoc.javadoc.RecommendersAetherTaglet;
 import org.eclipse.recommenders.livedoc.utils.LiveDocUtils;
import org.eclipse.recommenders.models.BasedTypeName;
 import org.eclipse.recommenders.models.ProjectCoordinate;
 import org.eclipse.recommenders.utils.IOUtils;
 import org.eclipse.recommenders.utils.Zips;
 import org.eclipse.recommenders.utils.gson.GsonUtil;
 import org.eclipse.recommenders.utils.names.IMethodName;
 import org.eclipse.recommenders.utils.names.ITypeName;
 import org.eclipse.recommenders.utils.names.VmTypeName;
 import org.sonatype.aether.artifact.Artifact;
 
 import com.google.common.base.Optional;
 import com.sun.javadoc.ClassDoc;
 import com.sun.javadoc.Doc;
 import com.sun.javadoc.Tag;
 import com.sun.tools.doclets.internal.toolkit.taglets.TagletOutput;
 import com.sun.tools.doclets.internal.toolkit.taglets.TagletWriter;
 
 public class OverridePatterns extends RecommendersAetherTaglet {
     
     private ZipFile modelsZip;
     private final int ranking = 20;
 
     public void initialize() {
         
         Artifact artifact = downloadModelsArtifact("ovrp");
         try {
             modelsZip = new ZipFile(artifact.getFile());
         } catch (IOException e) {
             e.printStackTrace();
         }
     }
 
     @Override
     public boolean inType() {
         return true;
     }
 
     @Override
     public String getName() {
         return "ovrp";
     }
 
     @Override
     public TagletOutput getTagletOutput(Tag tag, TagletWriter writer) throws IllegalArgumentException {
         throw new UnsupportedOperationException();
     }
 
     @Override
     public TagletOutput getTagletOutput(Doc holder, TagletWriter writer) throws IllegalArgumentException {
         
         if (holder.isOrdinaryClass()){
             return generateClassDoc((ClassDoc) holder, writer);
         }
         
         return null;
     }
 
     private TagletOutput generateClassDoc(ClassDoc holder, TagletWriter writer) {
         
         ProjectCoordinate coordinate = new ProjectCoordinate(groupId, artifactId, artifactVersion);
         ITypeName typeName = VmTypeName.get(LiveDocUtils.extractTypeName(holder));
         
        BasedTypeName key = new BasedTypeName(coordinate, typeName);
         
         Optional<ClassOverridePatterns> overridePatterns = loadModel(modelsZip, key);
 
         
         StringBuilder sb = new StringBuilder();
         
         if (overridePatterns.isPresent()){
             
             sb.append("<h5>Method override patterns:</h5>")
             .append("The following groups of methods are frequently overriden together:")
             .append("<br>")
             .append("<br>")
             .append("<ul>");
             
             MethodPattern[] methodPatterns = overridePatterns.get().getPatterns();
             
             for (int i = 0; i < methodPatterns.length; i++) {
                 MethodPattern methodPattern = methodPatterns[i];
                 
                 sb.append("<li>");
                 sb.append("<b>");
                 sb.append("Pattern #" + (i+1));
                 sb.append(", ");
                 sb.append("</b>");
                 sb.append(methodPattern.getNumberOfObservations() + " times observed");
 
                 sb.append("<br>")
                 .append("<br>");
                 
                 List<Entry<IMethodName, Double>> methods = new ArrayList<Entry<IMethodName, Double>>(methodPattern.getMethods().entrySet());
                 sortMethods(methods);
 
                 
                 for (Iterator<Entry<IMethodName, Double>> iterator = methods.iterator(); iterator.hasNext();) {
                     Entry<IMethodName, Double> entry  = (Entry<IMethodName, Double>) iterator.next();
                     
                     sb.append(LiveDocUtils.methodSignature(entry.getKey())) 
                     .append(" - ")
                     .append("<font color=\"#0000FF\">")
                     .append(asPercentage(entry) + "%")
                     .append("</font>");
                     
                     if (iterator.hasNext()){
                         sb.append("<br>");
                     }
                     
                 }
                 
                 if (i < (methodPatterns.length - 1)){
                     sb.append("<br>")
                         .append("<br>");
                 }
              sb.append("</li>");   
             }
             sb.append("</ul>");
         }
         
         
         
         TagletOutput output = writer.getOutputInstance();
         output.setOutput(sb.toString());
         return output;
     }
 
     private void sortMethods(List<Entry<IMethodName, Double>> methods) {
         Collections.sort(methods, new Comparator<Entry<IMethodName, Double>>() {
 
             @Override
             public int compare(Entry<IMethodName, Double> o1, Entry<IMethodName, Double> o2) {
                 if (!o1.getValue().equals(o2.getValue())){
                     return o2.getValue().compareTo(o1.getValue());
                 }else{
                     return o1.getKey().getName().compareTo(o2.getKey().getName());
                 }
             }
             
         });
         
     }
 
     private int asPercentage(Entry<IMethodName, Double> entry) {
         Double rel =  entry.getValue();
         ensureIsInRange(rel, 0, 1, "relevance '%f' not in interval [0, 1]", rel);
         return (int) Math.round(rel * 100);
     }
 
    protected Optional<ClassOverridePatterns> loadModel(ZipFile zip, BasedTypeName key) {
         String path = Zips.path(key.getName(), ".json");
         ZipEntry entry = zip.getEntry(path);
         if (entry == null) {
             return absent();
         }
         InputStream is = null;
         
         try {
             is = zip.getInputStream(entry);
         } catch (IOException e) {
             e.printStackTrace();
         }
         
         ClassOverridePatterns res = GsonUtil.deserialize(is, ClassOverridePatterns.class);
         IOUtils.closeQuietly(is);
         return of(res);
     }
     
     @Override
     protected int getRanking() {
         return ranking ;
     };
 
     @Override
     public void finish() {
         
     }
 
 }
