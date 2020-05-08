 package org.geworkbench.bison.annotation;
 
 import org.geworkbench.bison.datastructure.biocollections.DSDataSet;
 import org.geworkbench.bison.datastructure.complex.panels.DSPanel;
 import org.geworkbench.bison.datastructure.complex.panels.CSPanel;
 import org.geworkbench.bison.datastructure.bioobjects.DSBioObject;
 import org.apache.commons.collections15.map.ListOrderedMap;
 
 import java.util.Map;
 import java.util.HashMap;
 import java.util.ArrayList;
 import java.util.Set;
 
 /**
  * @author John Watkinson
  */
 public class CSAnnotationContext<T extends DSBioObject> implements DSAnnotationContext<T> {
 
     private String name;
     private DSDataSet dataSet;
 
     private ListOrderedMap<DSAnnotationType, Map<T,?>> annotations;
     private ListOrderedMap<String, DSPanel<T>> labels;
 
     public CSAnnotationContext(String name, DSDataSet dataSet) {
         this.name = name;
         this.dataSet = dataSet;
         annotations = new ListOrderedMap<DSAnnotationType, Map<T, ?>>();
         labels = new ListOrderedMap<String, DSPanel<T>>();
     }
 
     public String getName() {
         return name;
     }
 
     public void setName(String newName) {
         name = newName;
     }
 
     public DSDataSet<T> getDataSet() {
         return dataSet;
     }
 
     public boolean addAnnotationType(DSAnnotationType annotationType) {
         if (annotations.containsKey(annotationType)) {
             return false;
         } else {
             HashMap<T, Object> map = new HashMap<T, Object>();
             annotations.put(annotationType, map);
             return true;
         }
     }
 
     public boolean removeAnnotationType(DSAnnotationType annotationType) {
         return (annotations.remove(annotationType) != null);
     }
 
     public int getNumberOfAnnotationTypes() {
         return annotations.keySet().size();
     }
 
     public DSAnnotationType getAnnotationType(int index) {
        return annotations.get(index);
     }
 
     public <Q> void annotateItem(T item, DSAnnotationType<Q> annotationType, Q value) {
         Map<T, Q> map = (Map<T, Q>) annotations.get(annotationType);
         if (map == null) {
             map = new HashMap<T, Q>();
             annotations.put(annotationType, map);
         }
         map.put(item, value);
     }
 
     public boolean removeAnnotationFromItem(T item, DSAnnotationType annotationType) {
         Map<T, ?> map = annotations.get(annotationType);
         if (map != null) {
             return (map.remove(item) != null);
         } else {
             return false;
         }
     }
 
     public DSAnnotationType[] getAnnotationTypesForItem(T item) {
         ArrayList<DSAnnotationType> list = new ArrayList<DSAnnotationType>();
         Set<DSAnnotationType> keySet = annotations.keySet();
         for (DSAnnotationType type : keySet) {
             if (annotations.get(type).containsKey(item)) {
                 list.add(type);
             }
         }
         return list.toArray(new DSAnnotationType[0]);
     }
 
     public <Q> Q getAnnotationForItem(T item, DSAnnotationType<Q> annotationType) {
         Map<T, Q> map = (Map<T, Q>) annotations.get(annotationType);
         if (map != null) {
             return map.get(item);
         } else {
             return null;
         }
     }
 
     public boolean addLabel(String label) {
         if (labels.get(label) != null) {
             return false;
         } else {
             CSPanel<T> panel = new CSPanel<T>(label);
             labels.put(label, panel);
             return true;
         }
     }
 
     public boolean addCriterionLabel(String label, DSCriterion<T> criterion) {
         return false;  //To change body of implemented methods use File | Settings | File Templates.
     }
 
     public boolean removeLabel(String label) {
         return false;  //To change body of implemented methods use File | Settings | File Templates.
     }
 
     public boolean isCriterionLabel(String label) {
         return false;  //To change body of implemented methods use File | Settings | File Templates.
     }
 
     public DSCriterion<T> getCriterionForLabel(String label) {
         return null;  //To change body of implemented methods use File | Settings | File Templates.
     }
 
     public int getNumberOfLabels() {
         return 0;  //To change body of implemented methods use File | Settings | File Templates.
     }
 
     public String getLabel(int index) {
         return null;  //To change body of implemented methods use File | Settings | File Templates.
     }
 
     public void setLabelActive(String label, boolean active) {
         //To change body of implemented methods use File | Settings | File Templates.
     }
 
     public void activateLabel(String label) {
         //To change body of implemented methods use File | Settings | File Templates.
     }
 
     public void deactivateLabel(String label) {
         //To change body of implemented methods use File | Settings | File Templates.
     }
 
     public boolean isLabelActive(String label) {
         return false;  //To change body of implemented methods use File | Settings | File Templates.
     }
 
     public boolean labelItem(T item, String label) {
         return false;  //To change body of implemented methods use File | Settings | File Templates.
     }
 
     public DSPanel<T> getActiveItems() {
         return null;  //To change body of implemented methods use File | Settings | File Templates.
     }
 
     public DSPanel<T> getItemsWithLabel(String label) {
         return null;  //To change body of implemented methods use File | Settings | File Templates.
     }
 
     public boolean hasLabel(T item, String label) {
         return false;  //To change body of implemented methods use File | Settings | File Templates.
     }
 
     public DSPanel<T> getItemsWithAnyLabel(String... labels) {
         return null;  //To change body of implemented methods use File | Settings | File Templates.
     }
 
     public DSPanel<T> getItemsWithAllLabels(String... labels) {
         return null;  //To change body of implemented methods use File | Settings | File Templates.
     }
 
     public String[] getLabelsForItem(T item) {
         return new String[0];  //To change body of implemented methods use File | Settings | File Templates.
     }
 
     public boolean removeLabelFromItem(T item, String label) {
         return false;  //To change body of implemented methods use File | Settings | File Templates.
     }
 
     public boolean addClass(String clazz) {
         return false;  //To change body of implemented methods use File | Settings | File Templates.
     }
 
     public boolean removeClass(String clazz) {
         return false;  //To change body of implemented methods use File | Settings | File Templates.
     }
 
     public int getNumberOfClasses() {
         return 0;  //To change body of implemented methods use File | Settings | File Templates.
     }
 
     public String getClass(int index) {
         return null;  //To change body of implemented methods use File | Settings | File Templates.
     }
 
     public String getDefaultClass() {
         return null;  //To change body of implemented methods use File | Settings | File Templates.
     }
 
     public boolean setDefaultClass(String clazz) {
         return false;  //To change body of implemented methods use File | Settings | File Templates.
     }
 
     public boolean assignClassToLabel(String label, String clazz) {
         return false;  //To change body of implemented methods use File | Settings | File Templates.
     }
 
     public String getClassForLabel(String label) {
         return null;  //To change body of implemented methods use File | Settings | File Templates.
     }
 
     public String getClassForItem(T item) {
         return null;  //To change body of implemented methods use File | Settings | File Templates.
     }
 
     public String[] getLabelsForClass(String clazz) {
         return new String[0];  //To change body of implemented methods use File | Settings | File Templates.
     }
 
     public String removeClassFromLabel(String label) {
         return null;  //To change body of implemented methods use File | Settings | File Templates.
     }
 
     public DSPanel<T> getItemsForClass(String clazz) {
         return null;  //To change body of implemented methods use File | Settings | File Templates.
     }
 }
