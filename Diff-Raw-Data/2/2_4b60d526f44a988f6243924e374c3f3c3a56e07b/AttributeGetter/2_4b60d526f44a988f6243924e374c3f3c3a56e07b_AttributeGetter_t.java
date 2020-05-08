 package gov.usgs.cida.coastalhazards.util;
 
 import java.util.Collection;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.Map;
 import org.opengis.feature.simple.SimpleFeature;
 import org.opengis.feature.type.FeatureType;
 import org.opengis.feature.type.GeometryType;
 import org.opengis.feature.type.Name;
 import org.opengis.feature.type.PropertyDescriptor;
 import org.opengis.feature.type.PropertyType;
 
 /**
  *
  * @author Jordan Walker <jiwalker@usgs.gov>
  */
 public class AttributeGetter {
 
     private FeatureType type;
     private Map<String, Name> attrMap;
     
     public AttributeGetter(FeatureType type) {
         this.type = type;
         this.attrMap = new HashMap<String, Name>();
         Collection<PropertyDescriptor> descriptors = type.getDescriptors();
         for (PropertyDescriptor desc : descriptors) {
             if (isGeom(desc)) {
                 attrMap.put(Constants.DEFAULT_GEOM_ATTR, desc.getName());
             }
             else if (isDate(desc)) {
                 attrMap.put(Constants.DATE_ATTR, desc.getName());
             }
             else if (isUncertainty(desc)) {
                 attrMap.put(Constants.UNCY_ATTR, desc.getName());
             }
             else if (isOrient(desc)) {
                 attrMap.put(Constants.BASELINE_ORIENTATION_ATTR, desc.getName());
             }
             else if (isOther(desc, Constants.TRANSECT_ID_ATTR)) {
                 attrMap.put(Constants.TRANSECT_ID_ATTR, desc.getName());
             }
             else if (isOther(desc, Constants.BASELINE_ID_ATTR)) {
                 attrMap.put(Constants.BASELINE_ID_ATTR, desc.getName());
             }
             else if (isOther(desc, Constants.BASELINE_DIST_ATTR)) {
                 attrMap.put(Constants.BASELINE_DIST_ATTR, desc.getName());
             }
             else if (isOther(desc, Constants.DISTANCE_ATTR)) {
                 attrMap.put(Constants.DISTANCE_ATTR, desc.getName());
             }
             else if (isOther(desc, Constants.LCI_ATTR)) {
                 attrMap.put(Constants.LCI_ATTR, desc.getName());
             }
             else if (isOther(desc, Constants.LRR_ATTR)) {
                 attrMap.put(Constants.LRR_ATTR, desc.getName());
             }
             else if (isOther(desc, Constants.SCE_ATTR)) {
                 attrMap.put(Constants.SCE_ATTR, desc.getName());
             }
             
         }
     }
     
     public Object getValue(String guess, SimpleFeature feature) {
         Name name = attrMap.get(guess);
         if (name == null) {
             return null;
         }
         else {
             return feature.getAttribute(name);
         }
     }
     
     public boolean exists(String guess) {
         Name name = attrMap.get(guess);
         PropertyDescriptor descriptor = type.getDescriptor(name);
         return (descriptor != null);
     }
     
     public boolean matches(Name actual, String guess) {
         Name name = attrMap.get(guess);
         return (null != actual && actual.equals(name));
     }
     
     private boolean isGeom(PropertyDescriptor desc) {
         PropertyType propType = desc.getType();
         return (propType instanceof GeometryType);
     }
     
     private boolean isDate(PropertyDescriptor desc) {
         String name = desc.getName().getLocalPart();
         PropertyType propType = desc.getType();
         if (propType.getBinding() == Date.class) {
             return true;
         }
         if (propType.getBinding() == String.class) {
             if ("date_".equalsIgnoreCase(name) || "date".equalsIgnoreCase(name) ||
                     Constants.DATE_ATTR.equalsIgnoreCase(name)) {
                 return true;
             }
         }
         return false;
     }
     
     private boolean isUncertainty(PropertyDescriptor desc) {
         String name = desc.getName().getLocalPart();
         if ("uncertainty_".equalsIgnoreCase(name) || "uncertainty".equalsIgnoreCase(name) ||
                 "uncy_".equalsIgnoreCase(name) || "uncy".equalsIgnoreCase(name) ||
                "accuracy".equalsIgnoreCase(name) || Constants.UNCY_ATTR.equalsIgnoreCase(name)) {
             return true;
         }
         return false;
     }
     
     private boolean isOrient(PropertyDescriptor desc) {
         String name = desc.getName().getLocalPart();
         if ("OFFshore".equalsIgnoreCase(name) || Constants.BASELINE_ORIENTATION_ATTR.equalsIgnoreCase(name)) {
             return true;
         }
         return false;
     }
     
     private boolean isOther(PropertyDescriptor desc, String guess) {
         String name = desc.getName().getLocalPart();
         return (guess.equalsIgnoreCase(name));
     }
     
 }
