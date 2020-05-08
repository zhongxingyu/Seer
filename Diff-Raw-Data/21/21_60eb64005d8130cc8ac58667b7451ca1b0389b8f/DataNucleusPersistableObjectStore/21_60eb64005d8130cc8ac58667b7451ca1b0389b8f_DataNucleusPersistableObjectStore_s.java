 /*
  * See the NOTICE file distributed with this work for additional
  * information regarding copyright ownership.
  *
  * This is free software; you can redistribute it and/or modify it
  * under the terms of the GNU Lesser General Public License as
  * published by the Free Software Foundation; either version 2.1 of
  * the License, or (at your option) any later version.
  *
  * This software is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
  * Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public
  * License along with this software; if not, write to the Free
  * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
  * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
  */
 package org.xwiki.store.datanucleus.internal;
 
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Map;
 import java.util.Set;
 import java.util.Stack;
 import java.lang.reflect.Array;
 import java.lang.reflect.Field;
 import java.lang.reflect.ParameterizedType;
 
 import javax.jdo.JDOHelper;
 import javax.jdo.JDOObjectNotFoundException;
 import javax.jdo.PersistenceManager;
 import javax.jdo.PersistenceManagerFactory;
 import javax.jdo.Transaction;
 import javax.jdo.JDOObjectNotFoundException;
 import org.apache.commons.lang3.ArrayUtils;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.xwiki.store.TransactionException;
 import org.xwiki.store.TransactionRunnable;
 import org.xwiki.store.UnexpectedException;
 import org.xwiki.store.objects.PersistableObject;
 import org.xwiki.store.objects.PersistableClass;
 import org.xwiki.store.objects.PersistableClassLoader;
 
 /**
  * The DataNucleusClassLoader is designed to load java classes from the data store.
  */
 public class DataNucleusPersistableObjectStore
 {
     private static final Logger LOGGER =
         LoggerFactory.getLogger(DataNucleusPersistableObjectStore.class);
 
     public TransactionRunnable<PersistenceManager> getStoreTransactionRunnable(
         final String key,
         final PersistableObject value)
     {
         return (new TransactionRunnable<PersistenceManager>() {
             protected void onRun()
             {
                 final PersistenceManager manager = this.getContext();
                 manager.setDetachAllOnCommit(true);
                 final Set<PersistableClass> classes = getClassesAndSetIds(key, value);
                 LOGGER.error("Storing object [{}] which contains [{}] classes.", key, classes.size());
                 for (final PersistableClass pc : classes) {
                    if (!JDOHelper.isDetached(pc) || JDOHelper.isDirty(pc)) {
                         LOGGER.error("Storing class [{}] bytecode length [{}].",
                                      pc.getName(), pc.getBytes().length);
                         manager.makePersistent(pc);
                     }
                 }
                 manager.makePersistent(value);
             }
         });
     }
 
     public TransactionRunnable<PersistenceManager> getLoadTransactionRunnable(
         final Collection<String> keys,
         final String className,
         final Collection<PersistableObject> outputs)
     {
         return (new TransactionRunnable<PersistenceManager>() {
             protected void onRun() throws ClassNotFoundException, ClassCastException
             {
                 final Class cls = Class.forName(className);
                 final PersistenceManager pm = this.getContext();
                 pm.setDetachAllOnCommit(true);
 
                 for (final String key : keys) {
                     try {
                         outputs.add((PersistableObject) pm.getObjectById(cls, key));
                     } catch (JDOObjectNotFoundException e) {
                         LOGGER.debug("Document [{}] was not found.", key);
                     }
                 }
             }
         });
     }
 
     private static Set<PersistableClass> getClassesAndSetIds(final String key,
                                                              final PersistableObject value)
     {
         final Map<String, PersistableObject> objectsByKey =
             new HashMap<String, PersistableObject>();
         walkTree(key, value, objectsByKey, new Stack());
 
         final Set<PersistableClass> out = new HashSet<PersistableClass>();
         for (final Map.Entry<String, PersistableObject> e : objectsByKey.entrySet()) {
             e.getValue().setPersistableObjectId(e.getKey());
             out.add(e.getValue().getPersistableClass());
         }
         return out;
     }
 
     private static boolean potentiallyPersistable(final Class c)
     {
         return c.isAssignableFrom(PersistableObject.class)
             || PersistableObject.class.isAssignableFrom(c);
     }
 
     /**
      * Walk the elements in a collection or array looking for persistable objects.
      *
      * @param id the identifier for the object.
      * @param value the collction or array to walk, if this is not a collection or array,
      *              nothing will be done.
      * @param out, the output into which all discovered persistable objects will be placed.
      * @param stack a stack used for internal loop detection.
      */
     private static void walkTree(final String id,
                                  final Object value,
                                  final Map<String, PersistableObject> out,
                                  final Stack stack)
     {
         if (value == null || stack.contains(value)) {
             return;
         }
         stack.push(value);
 
         if (value instanceof PersistableObject) {
             out.put(id, (PersistableObject) value);
 
             // Index over the fields looking for more persistables
             for (final Field field : value.getClass().getDeclaredFields()) {
                 final Class type = field.getType();
                 boolean isMap = Map.class.isAssignableFrom(type);
                 if (isMap || Collection.class.isAssignableFrom(type)) {
                     final ParameterizedType pType = (ParameterizedType) field.getGenericType();
                     final Class<?> componentType =
                         (Class<?>) pType.getActualTypeArguments()[isMap ? 1 : 0];
                     if (!potentiallyPersistable(componentType)) {
                         continue;
                     }
                 } else if (type.isArray()) {
                     if (!potentiallyPersistable(type.getComponentType())) {
                         continue;
                     }
                 } else if (!potentiallyPersistable(type)) {
                     continue;
                 }
                 // We don't need to use reflection for this, we can do it using the jdo state manager.
                 field.setAccessible(true);
                 try {
                     walkTree(id + "." + field.getName(), field.get(value), out, stack);
                 } catch (IllegalAccessException e) {
                     throw new UnexpectedException("Could not reflect nested objects, "
                                                   + "is a security manager preventing it?");
                 }
             }
         } else {
             walkCollection(id, value, out, stack);
         }
 
         stack.pop();
         return;
     }
 
     /**
      * Walk the elements in a collection or array looking for persistable objects.
      *
      * @param id the identifier for the object.
      * @param value the collction or array to walk, if this is not a collection or array,
      *              nothing will be done.
      * @param out, the output into which all discovered persistable objects will be placed.
      * @param stack a stack used for internal loop detection.
      */
     private static void walkCollection(final String id,
                                        final Object value,
                                        final Map<String, PersistableObject> out,
                                        final Stack stack)
     {
         if (value instanceof Map) {
             for (final Map.Entry e : ((Map<?,?>) value).entrySet()) {
                 final String nextId = id + "['" + e.getKey() + "']";
                 walkTree(nextId, e.getValue(), out, stack);
             }
         } else if (value instanceof Collection) {
             int i = 0;
             for (final Object item : ((Collection) value)) {
                 walkTree(id + "[" + i + "]", item, out, stack);
                 i++;
             }
         } else if (value.getClass().isArray()) {
             for (int i = 0; i < Array.getLength(value); i++) {
                 walkTree(id + "[" + i + "]", Array.get(value, i), out, stack);
             }
         }
     }
 }
