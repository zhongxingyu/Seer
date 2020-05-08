 /*
  *  Copyright 2010 Visural.
  * 
  *  Licensed under the Apache License, Version 2.0 (the "License");
  *  you may not use this file except in compliance with the License.
  *  You may obtain a copy of the License at
  * 
  *       http://www.apache.org/licenses/LICENSE-2.0
  * 
  *  Unless required by applicable law or agreed to in writing, software
  *  distributed under the License is distributed on an "AS IS" BASIS,
  *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  *  See the License for the specific language governing permissions and
  *  limitations under the License.
  *  under the License.
  */
 package com.visural.common.datastruct;
 
 import com.visural.common.BeanUtil;
 import com.visural.common.collection.readonly.ReadOnlyList;
 import java.io.Serializable;
 import java.lang.reflect.Method;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.ListIterator;
 import java.util.Map;
 
 /**
  * Wrapper for a List of JavaBeans, which allows sorting on different properties.
  * @author Visural
  */
 public class BeanList<T> implements List<T>, Serializable {
 
     private static final String[] StringArray = new String[]{};
 
     private final List<T> list;
     private int numberOfSorts = 1;
     private List<String> sortProperties = new ArrayList<String>();
     private Nulls nullHandling = Nulls.HandledByComparators;
 
     public BeanList(List<T> real) {
         this.list = real;
     }
 
     public BeanList(List<T> real, int numberOfSorts) {
         this.list = real;
         this.numberOfSorts = numberOfSorts;
     }
 
     public BeanList(List<T> real, int numberOfSorts, String... initialSort) {
         this(real, numberOfSorts);
         sortByProperties(initialSort);
     }
 
     public BeanList(List<T> real, int numberOfSorts, Nulls nullHandling, String... initialSort) {
         this(real, numberOfSorts);
         setNullHandling(nullHandling);
         sortByProperties(initialSort);
     }
 
     public void sortByProperties(String... properties) {
         for (int n = properties.length-1; n >= 0; n--) {
             popIfRequiredThenAdd(properties[n]);
         }
         sort(properties);
     }
 
     public BeanList<T> resortByProperty(String property) {
         popIfRequiredThenAdd(property);
         sort(sortProperties.toArray(StringArray));
         return this;
     }
 
     private void popIfRequiredThenAdd(String property) {
         if (sortProperties.size() > 0 && sortProperties.size() >= numberOfSorts) {
             String propAdj = property.startsWith("-") ? property.substring(1) : property;
             if (sortProperties.contains(propAdj)) {
                 // remove old sort on this property
                 sortProperties.remove(propAdj);
             } else if (sortProperties.contains("-"+propAdj)) {
                 sortProperties.remove("-"+propAdj);
             } else {
                 // sort pops oldest sort from end
                 sortProperties.remove(sortProperties.size()-1);
             }
         }
         sortProperties.add(0, property);
     }
 
     public BeanList<T> resort() {
         sort(sortProperties.toArray(StringArray));
         return this;
     }
 
     private BeanList<T> sort(final String... properties) {
         Collections.sort(list, new Comparator<T>() {
             public int compare(T o1, T o2) {
                 try {
                     for (int n = 0; n < properties.length; n++) {
                         boolean ascending = true;
                         String property = properties[n];
                         if (property.startsWith("-")) {
                             ascending = false;
                             property = property.substring(1);
                         }
                         Class type = BeanUtil.getPropertyType(o1, property);
                         Method getter1 = BeanUtil.findGetter(o1.getClass(), property);
                         if (getter1 == null) {
                             throw new IllegalStateException(String.format("Property '%s' on class '%s' can not be resolved.", property, o1.getClass().getName()));
                         }
                         Method getter2 = BeanUtil.findGetter(o2.getClass(), property);
                         if (getter2 == null) {
                             throw new IllegalStateException(String.format("Property '%s' on class '%s' can not be resolved.", property, o2.getClass().getName()));
                         }
                         Object val1 = getter1.invoke(o1);
                         Object val2 = getter2.invoke(o2);
 
                         int result = 0;
 
                         switch (nullHandling) {
                             case AreLess:
                                 if (val1 == null && val2 == null) {
                                    continue;
                                 } else if (val1 == null && val2 != null) {
                                     return ascending ? -1 : 1;
                                 } else if (val1 != null && val2 == null) {
                                     return ascending ? 1 : -1;
                                 }
                                 break;
                             case AreMore:
                                 if (val1 == null && val2 == null) {
                                    continue;
                                 } else if(val1 == null && val2 != null)  {
                                     return ascending ? 1 : -1;
                                 } else if (val1 != null && val2 == null) {
                                     return ascending ? -1 : 1;
                                 }
                                 break;
                         }
 
                         Comparator comparator = getComparator(property, type);
                         if (comparator != null) {
                             result = comparator.compare(val1, val2);                           
                         } else if (Comparable.class.isAssignableFrom(type)) {
                             if (val1 != null) {
                                 result = ((Comparable)val1).compareTo(val2);
                             } else if (val2 != null) {
                                 result = ((Comparable)val2).compareTo(val1);
                             } else {
                                 result = 0;
                             }
                         } else {
                             throw new IllegalStateException("Don't know how to compare type '"+type.getName()+"'");
                         }
 
                         if (result < 0 || result > 0) {
                             return ascending ? result : -result;
                         }
                         // if result == 0 we continue for next property comparison
                     }
                     // if all properties return 0, we return 0 too
                     return 0;
                 } catch (Exception e) {
                     throw new IllegalStateException("Error invoking methods.", e);
                 }
             }
 
         });
         return this;
     }
 
     private Comparator getComparator(String property, Class type) {
         Comparator c = propertyComparators.get(property);
         if (c == null) {
             c = typeComparators.get(type);
         }
         return c;
     }
 
     private final Map<Class, Comparator> typeComparators = new HashMap<Class, Comparator>();
 
     public BeanList<T> registerTypeComparator(Class type, Comparator comparator) {
         typeComparators.put(type, comparator);
         return this;
     }
 
     private final Map<String, Comparator> propertyComparators = new HashMap<String, Comparator>();
 
     public BeanList<T> registerPropertyComparator(String property, Comparator comparator) {
         propertyComparators.put(property, comparator);
         return this;
     }
 
     public int getNumberOfSorts() {
         return numberOfSorts;
     }
 
     public void setNumberOfSorts(int numberOfSorts) {
         this.numberOfSorts = numberOfSorts;
         while (numberOfSorts < sortProperties.size()) {
             sortProperties.remove(sortProperties.size()-1);
         }
     }
 
     public ReadOnlyList<String> getSortProperties() {
         return new ReadOnlyList(sortProperties);
     }
 
     public Nulls getNullHandling() {
         return nullHandling;
     }
 
     public void setNullHandling(Nulls nullHandling) {
         this.nullHandling = nullHandling;
     }
 
     /* wrapped list methods follow */
 
     public int size() {
         return list.size();
     }
 
     public boolean isEmpty() {
         return list.isEmpty();
     }
 
     public boolean contains(Object o) {
         return list.contains(o);
     }
 
     public Iterator<T> iterator() {
         return list.iterator();
     }
 
     public Object[] toArray() {
         return list.toArray();
     }
 
     public <T> T[] toArray(T[] a) {
         return list.toArray(a);
     }
 
     public boolean add(T e) {
         return list.add(e);
     }
 
     public boolean remove(Object o) {
         return list.remove(o);
     }
 
     public boolean containsAll(Collection<?> c) {
         return list.containsAll(c);
     }
 
     public boolean addAll(Collection<? extends T> c) {
         return list.addAll(c);
     }
 
     public boolean addAll(int index, Collection<? extends T> c) {
         return list.addAll(index, c);
     }
 
     public boolean removeAll(Collection<?> c) {
         return list.removeAll(c);
     }
 
     public boolean retainAll(Collection<?> c) {
         return list.retainAll(c);
     }
 
     public void clear() {
         list.clear();
     }
 
     public T get(int index) {
         return list.get(index);
     }
 
     public T set(int index, T element) {
         return list.set(index, element);
     }
 
     public void add(int index, T element) {
         list.add(index, element);
     }
 
     public T remove(int index) {
         return list.remove(index);
     }
 
     public int indexOf(Object o) {
         return list.indexOf(o);
     }
 
     public int lastIndexOf(Object o) {
         return list.lastIndexOf(o);
     }
 
     public ListIterator<T> listIterator() {
         return list.listIterator();
     }
 
     public ListIterator<T> listIterator(int index) {
         return list.listIterator();
     }
 
     public List<T> subList(int fromIndex, int toIndex) {
         return list.subList(fromIndex, toIndex);
     }
 
     @Override
     public String toString() {
         return "Sort"+sortProperties+"-"+list.toString();
     }
 
     public enum Nulls {
         AreLess,
         AreMore,
         HandledByComparators
     }
 }
