 package gov.va.vinci.v3nlp;
 
 import gov.va.vinci.cm.Annotation;
 import gov.va.vinci.cm.Feature;
 import gov.va.vinci.cm.FeatureElement;
 
 import java.util.Date;
 import java.util.List;
 import java.util.Set;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 public class NlpUtilities {
 
     public static Annotation convertAnnotation(gate.Annotation gateAnnotation,
 			String content, Integer offset) {
         Annotation resultAnnotation = new Annotation(gateAnnotation.getId());
 
             resultAnnotation.setBeginOffset(gateAnnotation.getStartNode()
                     .getOffset().intValue() + offset);
             resultAnnotation.setEndOffset(gateAnnotation.getEndNode().getOffset()
                     .intValue() + offset);
             Feature feature = new Feature((String) gateAnnotation.getFeatures()
                     .get("type"), (String) gateAnnotation.getFeatures().get("name"));
             feature.getFeatureElements().add(new FeatureElement("type", gateAnnotation.getType()));
             feature.getMetaData().setCreatedDate(new Date());
 
             // Set Pedigree.
             if (gateAnnotation.getType() != null) {
                 feature.getMetaData().setPedigree((String) gateAnnotation.getType());
                feature.setFeatureName((String) gateAnnotation.getType());
             } else if (gateAnnotation.getFeatures().get("type") != null) {
                 feature.getMetaData().setPedigree((String) gateAnnotation.getFeatures().get("type"));
                feature.setFeatureName((String) gateAnnotation.getFeatures().get("type"));
             } else {
                 feature.getMetaData().setPedigree(gateAnnotation.getType());
                feature.setFeatureName(gateAnnotation.getType());
             }
 
 
             // Copy Features
             for (Object o : gateAnnotation.getFeatures().keySet()) {
                 Object j = gateAnnotation.getFeatures().get(o);
                 if (j instanceof String) {
                     feature.getFeatureElements().add(
                             new FeatureElement((String) o, (String) j));
                 } else if (j instanceof List) {
                     // TODO Implement
                 } else if (j instanceof Set) {
                     // TODO Implement
                 } else if (j == null) {
 
                 } else {
                     throw new RuntimeException("Unknown Feature type.");
                 }
             }
 
             resultAnnotation.setOriginalContent(content);
             resultAnnotation.getFeatures().add(feature);
             return resultAnnotation;
 
     }
 
 	@SuppressWarnings("unchecked")
 	public static Annotation convertAnnotation(gate.Annotation gateAnnotation,
 			String content) {
         return(convertAnnotation(gateAnnotation, content, 0));
 	}
 
     public static boolean isValidNameNumbersAndCharacters(String s) {
         String regExp="^[a-zA-Z_0-9]+$";
 
         Pattern p = Pattern.compile(regExp);
         Matcher m = p.matcher(s);
         return(m.matches());
     }
 }
