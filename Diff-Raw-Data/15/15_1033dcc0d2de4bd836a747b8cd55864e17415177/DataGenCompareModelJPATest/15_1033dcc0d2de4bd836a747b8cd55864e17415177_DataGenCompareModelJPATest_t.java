 /**
  * <copyright>
  *
  * Copyright (c) 2009, 2010 Springsite BV (The Netherlands) and others
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *   Martin Taal - Initial API and implementation
  *
  * </copyright>
  *
  * $Id: DataGenCompareModelJPATest.java,v 1.6 2011/08/25 12:42:40 mtaal Exp $
  */
 package org.eclipse.emf.texo.orm.test;
 
 import java.lang.reflect.Array;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Collection;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import javax.persistence.Entity;
 import javax.persistence.Query;
 
 import org.eclipse.emf.ecore.EAttribute;
 import org.eclipse.emf.ecore.EClass;
 import org.eclipse.emf.ecore.EReference;
 import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.EcorePackage;
 import org.eclipse.emf.ecore.util.FeatureMapUtil;
 import org.eclipse.emf.texo.model.ModelFeatureMapEntry;
 import org.eclipse.emf.texo.model.ModelObject;
 import org.eclipse.emf.texo.model.ModelPackage;
 import org.eclipse.emf.texo.model.ModelResolver;
 import org.eclipse.emf.texo.modelgenerator.annotator.GenUtils;
 import org.eclipse.emf.texo.modelgenerator.test.models.TestModel;
 import org.eclipse.emf.texo.test.model.base.identifiable.Identifiable;
 
 /**
  * Tests persistence by:
  * <ul>
  * <li>Generating testdata</li>
  * <li>Persisting the testdata</li>
  * <li>Reading the testdata</li>
  * <li>Comparing the read data with the original persisted data</li>
  * <li>Doing some queries (count etc.)</li>
  * </ul>
  * 
  * @see TestModel
  * @author <a href="mailto:mtaal@elver.org">Martin Taal</a>
  * @version $Revision: 1.6 $
  */
 public class DataGenCompareModelJPATest extends JPATest {
   public DataGenCompareModelJPATest(ModelPackage modelPackage, String postFix) {
     super(modelPackage, postFix);
   }
 
   public DataGenCompareModelJPATest(List<ModelPackage> modelPackages) {
     super(modelPackages);
   }
 
   @Override
   public void runTest() {
     final List<Object> testSet = generateTestSet(10, 10, 10, 4000);
 
     // collect the objects
     final Map<InstanceKey, Object> allObjects = collectObjects(testSet);
 
     {
       beginTransaction();
       for (Object o : allObjects.values()) {
         getEntityManager().persist(o);
       }
       commitTransaction();
     }
 
     reinitializeCachesAndEntityManager();
 
     // now query for the test set again
     for (Object object : allObjects.values()) {
       final Object queriedObject = getEntityManager().find(object.getClass(), getDbId(object));
       assertTrue(queriedObject != object);
       compare(object, queriedObject);
     }
 
     reinitializeCachesAndEntityManager();
 
     // do query testing
     // first collect the eclasses
     final Map<EClass, Integer> eClassCount = new HashMap<EClass, Integer>();
     for (Object object : allObjects.values()) {
       final ModelObject<?> modelObject = ModelResolver.getInstance().getModelObject(object);
       Integer count = eClassCount.get(modelObject.eClass());
       if (count == null) {
         count = 0;
       }
       eClassCount.put(modelObject.eClass(), count + 1);
     }
     // don't do counts for supertypes, these are more complex to compute...
     // especially because EMF supports multiple inheritance
     final List<EClass> toRemove = new ArrayList<EClass>();
     for (EClass eClass : eClassCount.keySet()) {
       for (EClass eSubClass : eClassCount.keySet()) {
         if (eClass != eSubClass && eClass.isSuperTypeOf(eSubClass)) {
           toRemove.add(eClass);
         }
       }
     }
     for (EClass eClass : toRemove) {
       eClassCount.remove(eClass);
     }
 
     // do some queries
     for (EClass eClass : eClassCount.keySet()) {
       // first a count query
       if (eClassCount.containsKey(eClass)) {
         Query cntQuery = getEntityManager().createQuery("SELECT COUNT(e) FROM " + getEntityName(eClass) + " e "); //$NON-NLS-1$//$NON-NLS-2$
         final int countResult = ((Number) cntQuery.getSingleResult()).intValue();
         final int compareCount = eClassCount.get(eClass);
         assertEquals(compareCount, countResult);
       }
 
       // then do a comparison query
       Query selectQuery = getEntityManager().createQuery("SELECT e FROM " + getEntityName(eClass) + " e "); //$NON-NLS-1$//$NON-NLS-2$
       for (Object o : selectQuery.getResultList()) {
         final ModelObject<?> modelObject = ModelResolver.getInstance().getModelObject(o);
         final InstanceKey key = new InstanceKey(modelObject.eClass(), getDbId(o));
         final Object original = allObjects.get(key);
         assertNotNull(original);
 
         // and compare the two
         compare(original, o);
       }
     }
   }
 
   protected Map<InstanceKey, Object> collectObjects(List<Object> objects) {
     final Map<InstanceKey, Object> instances = new HashMap<InstanceKey, Object>();
     for (Object object : objects) {
       collectObjects(object, instances);
     }
     return instances;
   }
 
   private String getEntityName(EClass eClass) {
     final Class<?> clz = ModelResolver.getInstance().getImplementationClass(eClass);
     if (clz == null) {
       return eClass.getName();
     }
     final Entity entity = clz.getAnnotation(Entity.class);
     if (entity == null) {
       return eClass.getName();
     }
     return entity.name();
   }
 
   private void collectObjects(Object object, Map<InstanceKey, Object> instances) {
     if (object == null) {
       return;
     }
     final ModelObject<?> modelObject = ModelResolver.getInstance().getModelObject(object);
 
     final Long dbId = getDbId(object);
     if (dbId == null) {
       return;
     }
 
     // don't add doc roots to the overall objects
     if (!GenUtils.isDocumentRoot(modelObject.eClass())) {
       final InstanceKey instanceKey = new InstanceKey(modelObject.eClass(), dbId);
       if (!instances.containsKey(instanceKey)) {
         instances.put(instanceKey, object);
       } else {
         return;
       }
     }
 
     // do feature maps
     for (EAttribute eAttribute : modelObject.eClass().getEAllAttributes()) {
       if (FeatureMapUtil.isFeatureMap(eAttribute)) {
         final Object manyValue = modelObject.eGet(eAttribute);
         if (manyValue instanceof Collection<?>) {
           final Collection<?> collection = (Collection<?>) manyValue;
           for (Object child : collection) {
             ModelFeatureMapEntry<?> fme = ModelResolver.getInstance().getModelFeatureMapEntry(eAttribute, child);
             if (fme.getEStructuralFeature() instanceof EReference
                 && ((EReference) fme.getEStructuralFeature()).isContainment()) {
               collectObjects(fme.getValue(), instances);
             }
           }
         }
       }
     }
 
     // do normal erefs
     for (EReference eReference : modelObject.eClass().getEAllReferences()) {
       if (eReference.isContainment()) {
         if (eReference.isMany()) {
           final Object manyValue = modelObject.eGet(eReference);
           if (manyValue instanceof Collection<?>) {
             final Collection<?> collection = (Collection<?>) manyValue;
             for (Object child : collection) {
               collectObjects(child, instances);
             }
           } else if (manyValue instanceof Map<?, ?>) {
             final Map<?, ?> map = (Map<?, ?>) manyValue;
             for (Map.Entry<?, ?> entry : map.entrySet()) {
               final Object value = entry.getValue();
               final Object key = entry.getKey();
               if (getDbId(value) != null) {
                 collectObjects(value, instances);
               }
               if (getDbId(key) != null) {
                 collectObjects(key, instances);
               }
             }
           } else {
             throw new IllegalArgumentException("Value class " + manyValue + " not supported here"); //$NON-NLS-1$ //$NON-NLS-2$
           }
         } else {
           collectObjects(modelObject.eGet(eReference), instances);
         }
       }
     }
   }
 
   protected void compare(Object o1, Object o2) {
     final ModelObject<?> m1 = ModelResolver.getInstance().getModelObject(o1);
     final ModelObject<?> m2 = ModelResolver.getInstance().getModelObject(o2);
     assertTrue(m1.eClass() == m2.eClass());
     for (EStructuralFeature eFeature : m1.eClass().getEAllStructuralFeatures()) {
       try {
         final Object v1 = m1.eGet(eFeature);
         final Object v2 = m2.eGet(eFeature);

        // not supported by JPA to persist
        if (eFeature.getEType().equals(EcorePackage.eINSTANCE.getEJavaClass())) {
          continue;
        }

         if (eFeature.isMany()) {
           if (v1 instanceof List) {
             compareLists(v1, v2);
           } else if (v1 instanceof Map) {
             compareMaps(v1, v2);
           } else if (v1 instanceof Set) {
             compareSets(v1, v2);
           } else {
             throw new IllegalArgumentException("Class " + v1.getClass() + " not supported"); //$NON-NLS-1$//$NON-NLS-2$
           }
         } else {
           compareValues(v1, v2);
         }
       } catch (UnsupportedOperationException e) {
         // ignore
       }
     }
   }
 
   protected Object getValueUsingId(Map<?, ?> map, Long dbId) {
     for (Object key : map.keySet()) {
       final Long dbId2 = getDbId(key);
       if (key == null) {
         return null;
       }
       if (dbId2.equals(dbId)) {
         return map.get(key);
       }
     }
     return null;
   }
 
   protected void compareLists(Object v1, Object v2) {
     final Collection<?> c1 = (Collection<?>) v1;
     final Collection<?> c2 = (Collection<?>) v2;
     if (c1.size() != c2.size()) {
       assertEquals(c1.size(), c2.size());
     }
     final Iterator<?> i1 = c1.iterator();
     final Iterator<?> i2 = c2.iterator();
     while (i1.hasNext()) {
       final Object value1 = i1.next();
       final Object value2 = i2.next();
       compareValues(value1, value2);
     }
   }
 
   protected void compareMaps(Object v1, Object v2) {
     final Map<?, ?> map1 = (Map<?, ?>) v1;
     final Map<?, ?> map2 = (Map<?, ?>) v2;
     assertTrue(map1.size() == map2.size());
     for (Object key : map1.keySet()) {
       Object value1 = map1.get(key);
       // if the key is not a model object then
       // assume it can be used to compare directly
       // otherwise use a more complex scheme
       Object value2;
       final Long dbId1 = getDbId(key);
       if (dbId1 != null) {
         value2 = getValueUsingId(map2, dbId1);
       } else {
         value2 = map2.get(key);
       }
       final Object dbValueId1 = getDbId(value1);
       if (dbValueId1 != null) {
         assertEqualModelObjects(value1, value2);
       } else {
         assertEquals(value1, value2);
       }
     }
 
   }
 
   protected void compareSets(Object v1, Object v2) {
     final Collection<?> c1 = (Collection<?>) v1;
     final Collection<?> c2 = (Collection<?>) v2;
     if (c1.size() != c2.size()) {
       assertEquals(c1.size(), c2.size());
     }
     final Iterator<?> i1 = c1.iterator();
     while (i1.hasNext()) {
       final Object value1 = i1.next();
       final Object dbId1 = getDbId(value1);
 
       if (dbId1 != null) {
         final Class<?> clz1 = value1.getClass();
         boolean found = false;
         for (Object o2 : c2) {
           final Long dbId2 = getDbId(o2);
 
           if (dbId2 != null && dbId2.equals(dbId1) && o2.getClass() == clz1) {
             compareValues(value1, o2);
             found = true;
             break;
           }
         }
         assertTrue(found);
       } else if (value1 instanceof Date) {
         findData(c2, (Date) value1);
       } else if (value1 instanceof Number) {
         findNumber(c2, (Number) value1);
       } else {
         assertTrue(c2.contains(value1));
       }
     }
   }
 
   private void findNumber(Collection<?> c, Number number) {
     for (Object o : c) {
       final Number n = (Number) o;
       final double diff = n.doubleValue() - number.doubleValue();
       if (diff < 0.1 && diff > -0.1) {
         return;
       }
     }
     fail("Value " + number + " not present in collection " + c); //$NON-NLS-1$ //$NON-NLS-2$ 
   }
 
   private void findData(Collection<?> c, Date date) {
     final Calendar cal1 = Calendar.getInstance();
     cal1.setTime(date);
     for (Object o : c) {
       final Date d2 = (Date) o;
       final Calendar cal2 = Calendar.getInstance();
       cal2.setTime(d2);
       if (cal1.get(Calendar.DATE) != cal2.get(Calendar.DATE) || cal1.get(Calendar.MONTH) != cal2.get(Calendar.MONTH)
           || cal1.get(Calendar.YEAR) != cal2.get(Calendar.YEAR)) {
         return;
       }
     }
     fail("Value " + date + " not present in collection"); //$NON-NLS-1$ //$NON-NLS-2$ 
   }
 
   protected void compareValues(Object v1, Object v2) {
     if (v1 == null && v2 == null) {
       return;
     }
 
     if (v1 == null || v2 == null) {
       fail("Different values " + v1 + " " + v2); //$NON-NLS-1$ //$NON-NLS-2$
       return;
     }
 
     final Long dbId1 = getDbId(v1);
     final Long dbId2 = getDbId(v2);
 
     if (dbId1 != null && dbId2 != null) {
       assertTrue(v1 != v2);
 
       if (v1.getClass() != v2.getClass()) {
         fail("Different values " + v1 + " " + v2); //$NON-NLS-1$ //$NON-NLS-2$
       }
 
       if (!dbId1.equals(dbId2)) {
         fail("Different values " + v1 + " " + v2); //$NON-NLS-1$ //$NON-NLS-2$
       }
       return;
     }
     if (dbId1 != null || dbId2 != null) {
       fail("Different values " + v1 + " " + v2); //$NON-NLS-1$ //$NON-NLS-2$
     }
 
     // compare dates only on the day
     if (v1 instanceof Date) {
       final Date d1 = (Date) v1;
       final Date d2 = (Date) v2;
       final Calendar cal1 = Calendar.getInstance();
       cal1.setTime(d1);
       final Calendar cal2 = Calendar.getInstance();
       cal2.setTime(d2);
       if (cal1.get(Calendar.DATE) != cal2.get(Calendar.DATE) || cal1.get(Calendar.MONTH) != cal2.get(Calendar.MONTH)
           || cal1.get(Calendar.YEAR) != cal2.get(Calendar.YEAR)) {
         fail("Different values " + v1 + " " + v2); //$NON-NLS-1$ //$NON-NLS-2$
       }
       return;
     }
 
     // handle inprecise numbers
     if (v1 instanceof Double) {
       final Double d1 = (Double) v1;
       final Double d2 = (Double) v2;
       if (Math.abs(d1 - d2) > 0.1) {
         fail("Different values " + v1 + " " + v2); //$NON-NLS-1$ //$NON-NLS-2$
       }
       return;
     }
 
     if (v1 instanceof Float) {
       final Float d1 = (Float) v1;
       final Float d2 = (Float) v2;
       if (Math.abs(d1 - d2) > 0.1) {
         fail("Different values " + v1 + " " + v2); //$NON-NLS-1$ //$NON-NLS-2$
       }
       return;
     }
 
     if (v1.getClass().isArray()) {
       assertEquals(Array.getLength(v1), Array.getLength(v2));
       for (int i = 0; i < Array.getLength(v1); i++) {
         final Object elem1 = Array.get(v1, i);
         final Object elem2 = Array.get(v2, i);
         compareValues(elem1, elem2);
       }
       return;
     }
 
     if (v1 instanceof Character && v2 instanceof String) {
 
       final String str1 = new String(new char[] { (Character) v1 });
       final String str2 = (String) v2;
       if (!str1.equals(str2)) {
         fail("Different values " + v1 + " " + v2); //$NON-NLS-1$ //$NON-NLS-2$
       }
       return;
     }
 
     if (v1 instanceof Number && v2 instanceof Number) {
       if (((Number) v1).intValue() != ((Number) v2).intValue()) {
         fail("Different values " + v1 + "/" + ((Number) v1).intValue() + " " + v2 + "/" + ((Number) v2).intValue()); //$NON-NLS-1$ //$NON-NLS-2$
       }
       return;
     }
 
     if (!v1.equals(v2)) {
       fail("Different values " + v1 + " " + v2); //$NON-NLS-1$ //$NON-NLS-2$
     }
   }
 
   protected List<Object> generateTestSet() {
     return generateTestSet(10, 10, 10, 1000);
   }
 
   private static class InstanceKey {
     private final EClass eClass;
     private final long id;
 
     public InstanceKey(EClass eClass, long id) {
       this.eClass = eClass;
       this.id = id;
     }
 
     @Override
     public int hashCode() {
       final int prime = 31;
       int result = 1;
       result = prime * result + (eClass == null ? 0 : eClass.hashCode());
       result = prime * result + (int) (id ^ id >>> 32);
       return result;
     }
 
     @Override
     public boolean equals(Object obj) {
       if (this == obj) {
         return true;
       }
       if (obj == null) {
         return false;
       }
       if (getClass() != obj.getClass()) {
         return false;
       }
       InstanceKey other = (InstanceKey) obj;
       if (eClass == null) {
         if (other.eClass != null) {
           return false;
         }
       } else if (!eClass.equals(other.eClass)) {
         return false;
       }
       if (id != other.id) {
         return false;
       }
       return true;
     }
 
   }
 
   private Long getDbId(Object object) {
     if (object instanceof Identifiable) {
       return ((Identifiable) object).getDb_Id();
     }
     if (!ModelResolver.getInstance().isModelEnabled(object)) {
       return null;
     }
     final ModelObject<?> modelObject = ModelResolver.getInstance().getModelObject(object);
     EStructuralFeature dbIdFeature = modelObject.eClass().getEStructuralFeature("db_Id"); //$NON-NLS-1$
 
     if (dbIdFeature != null) {
       return (Long) modelObject.eGet(dbIdFeature);
     }
 
     dbIdFeature = modelObject.eClass().getEStructuralFeature("dbId"); //$NON-NLS-1$
     if (dbIdFeature != null) {
       return (Long) modelObject.eGet(dbIdFeature);
     }
     return null;
   }
 
   public void assertEqualModelObjects(Object value1, Object value2) {
     final Object dbId1 = getDbId(value1);
     final Object dbId2 = getDbId(value2);
     if (dbId1 == null || dbId2 == null) {
       fail("One of the objects has an id null " + value1 + " - " + value2); //$NON-NLS-1$ //$NON-NLS-2$
     }
     assertEquals(dbId1, dbId2);
     final Class<?> clz1 = value1.getClass();
     final Class<?> clz2 = value2.getClass();
     assertTrue("Classes are unequal " + clz1 + " - " + clz2, clz1 == clz2);//$NON-NLS-1$ //$NON-NLS-2$        
   }
 }
