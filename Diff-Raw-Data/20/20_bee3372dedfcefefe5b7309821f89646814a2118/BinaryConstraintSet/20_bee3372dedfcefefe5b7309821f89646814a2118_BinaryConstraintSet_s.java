 package interiores.business.models.constraints;
 
 import interiores.utils.BinaryConstraintAssociation;
 import java.util.Collection;
 import java.util.HashSet;
 import java.util.Set;
 import java.util.TreeMap;
 import javax.xml.bind.annotation.XmlElementWrapper;
 import javax.xml.bind.annotation.XmlRootElement;
 
 /**
  *
  * @author hector
  */
 @XmlRootElement
 public class BinaryConstraintSet
 {
     private static final String KEY_FORMAT = "-%s-%s-%s-";
     private static final String KEY_TOKEN_FORMAT = "-%s-";
     
     @XmlElementWrapper
     private TreeMap<String, BinaryConstraintAssociation> binaryConstraints;
     
     @XmlElementWrapper
     private TreeMap<String, Integer> binaryConstraintCount;
     
     public BinaryConstraintSet() {
         binaryConstraints = new TreeMap();
         binaryConstraintCount = new TreeMap();
     }
     
     public void add(BinaryConstraint bc, String f1, String f2) {
         BinaryConstraintAssociation bca = new BinaryConstraintAssociation(bc, f1, f2);
         
         if(! binaryConstraintCount.containsKey(f1))
             binaryConstraintCount.put(f1, 0);
         
         if(! binaryConstraintCount.containsKey(f2))
             binaryConstraintCount.put(f2, 0);
         
         binaryConstraintCount.put(f1, binaryConstraintCount.get(f1) + bc.getPriority());
         binaryConstraintCount.put(f2, binaryConstraintCount.get(f2) + bc.getPriority());
         
         binaryConstraints.put(getKey(bc.getClass(), f1, f2), bca);
     }
     
     public void remove(Class<? extends BinaryConstraint> binaryConstraintClass, String f1,
             String f2)
     {
         if(!binaryConstraintCount.containsKey(f1) || !binaryConstraintCount.containsKey(f2))
             return;
         
         String key = getKey(binaryConstraintClass, f1, f2);
         BinaryConstraint bc = binaryConstraints.get(key).getConstraint();
         
         binaryConstraintCount.put(f1, binaryConstraintCount.get(f1) - bc.getPriority());
         binaryConstraintCount.put(f2, binaryConstraintCount.get(f2) - bc.getPriority());
         
         binaryConstraints.remove(key);
     }
     
     public int getPriority(String furnitureId) {
         if(! binaryConstraintCount.containsKey(furnitureId))
             return 0;
         
         return binaryConstraintCount.get(furnitureId);
     }
     
     public Collection<BinaryConstraintAssociation> getConstraints(String furnitureId) {
         Set<BinaryConstraintAssociation> bcaSet = new HashSet();
         
         String keyToken = String.format(KEY_TOKEN_FORMAT, furnitureId);
         
         for(String key : binaryConstraints.keySet()) {
             if(key.contains(keyToken))
                 bcaSet.add(binaryConstraints.get(key));
         }
         
         return bcaSet;
     }
     
     public Collection<BinaryConstraintAssociation> getConstraints() {
         return binaryConstraints.values();
     }
     
         private String getKey(Class binaryConstraintClass, String f1, String f2)
     {
         String className = binaryConstraintClass.getSimpleName();
         String key = String.format(KEY_FORMAT, className, f1, f2);
         
         if(binaryConstraints.containsKey(key))
             return key;
         
         return String.format(KEY_FORMAT, className, f2, f1);
     }
 }
