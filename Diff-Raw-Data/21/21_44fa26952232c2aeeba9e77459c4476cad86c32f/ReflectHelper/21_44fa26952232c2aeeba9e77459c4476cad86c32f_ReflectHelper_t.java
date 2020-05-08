 package nl.astraeus.persistence.reflect;
 
 import nl.astraeus.persistence.*;
 
 import javax.annotation.CheckForNull;
 import javax.annotation.Nonnull;
 import java.lang.reflect.Field;
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Method;
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 
 /**
  * Employee: rnentjes
  * Date: 30-12-10
  * Time: 17:03
  */
 public class ReflectHelper {
 
     private final static ReflectHelper instance = new ReflectHelper();
 
     public static ReflectHelper get() {
         return instance;
     }
 
     private Map<Integer, Method> methodCache = new HashMap<Integer, Method>();
     private Map<Integer, Field> fieldCache = new HashMap<Integer, Field>();
     private Map<Class<?>, java.util.List> classFieldCache = new HashMap<Class<?>, java.util.List>();
     private Map<Class<?>, java.util.List> referenceFieldCache = new HashMap<Class<?>, java.util.List>();
     private Map<Class<?>, java.util.List> listFieldCache = new HashMap<Class<?>, java.util.List>();
     private Map<Class<?>, java.util.List> setFieldCache = new HashMap<Class<?>, java.util.List>();
     private Map<Class<?>, java.util.List> sortedSetFieldCache = new HashMap<Class<?>, java.util.List>();
     private Map<Class<?>, String> classNameMap = new HashMap<Class<?>, String>();
 
     public String getClassName(Class cls) {
         String result = classNameMap.get(cls);
 
         if (result == null) {
             result = cls.getName();
 
             classNameMap.put(cls, result);
         }
 
         return result;
     }
 
     public Class getReturnType(Object object, String field) throws NoSuchMethodException {
         Method getter = findGetMethod(object, field);
 
         return getter.getReturnType();
     }
 
     private int getFullNameHash(Class c, String name) {
         return c.hashCode() * 7 + name.hashCode();
     }
 
     private int getFullNameHash(Object o, String name, Class... parameters) {
         int result = o.hashCode();
 
         result *= 7;
         result += name.hashCode();
 
         for (Class c : parameters) {
             result *= 7;
             result += c.hashCode();
 
         }
 
         return result;
     }
 
     public Method findGetMethod(Object object, String field) throws NoSuchMethodException {
         assert object != null : "Can't find get method on null object!";
         assert field != null : "Can't find get method with null field!";
 
         Integer nameHash = getFullNameHash(object, getGetterFieldName(field));
 
         Method method = methodCache.get(nameHash);
 
         if (method == null) {
             String getterName = getGetterFieldName(field);
 
             if (getterName == null) {
                 return null;
             }
 
             method = object.getClass().getMethod(getGetterFieldName(field), new Class[0]);
 
             methodCache.put(nameHash, method);
         }
 
         return method;
     }
 
     public Method findGetMethod(Object object, String field, Class<?>... parameterTypes) throws NoSuchMethodException {
         assert object != null : "Can't find get method on null object!";
 
         Integer nameHash = getFullNameHash(object, getGetterFieldName(field), parameterTypes);
 
         Method method = methodCache.get(nameHash);
 
         if (method == null) {
             method = object.getClass().getMethod(getGetterFieldName(field), parameterTypes);
 
             methodCache.put(nameHash, method);
         }
 
         return method;
     }
 
     public Method findMethod(Object object, String methodName) throws NoSuchMethodException {
         assert object != null : "Can't find get method on null object!";
         assert methodName != null : "Can't find get method with null field!";
 
         Integer nameHash = getFullNameHash(object, methodName);
 
         Method method = methodCache.get(nameHash);
 
         if (method == null) {
             method = object.getClass().getMethod(methodName, new Class[0]);
 
             methodCache.put(nameHash, method);
         }
 
         return method;
     }
 
     public Method findMethod(Object object, String field, Class<?>... parameterTypes) throws NoSuchMethodException {
         assert object != null : "Can't find get method on null object!";
 
         Integer nameHash = getFullNameHash(object, field, parameterTypes);
 
         Method method = methodCache.get(nameHash);
 
         if (method == null) {
             method = object.getClass().getMethod(field, parameterTypes);
 
             methodCache.put(nameHash, method);
         }
 
         return method;
     }
 
     public Method findSetMethod(Object object, String field) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
         Class returnType = getReturnType(object, field);
 
         Class[] parameter = {returnType};
 
         Integer nameHash = getFullNameHash(object, getSetterFieldName(field), returnType);
 
         Method method = methodCache.get(nameHash);
 
         if (method == null) {
             method = object.getClass().getMethod(getSetterFieldName(field), parameter);
 
             methodCache.put(nameHash, method);
         }
 
         return method;
     }
 
     public Method findSetMethod(Object object, String field, Class<?>... parameterTypes) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
         int hash = getHashCode(object, field, parameterTypes);
 
         Integer nameHash = getFullNameHash(object, getSetterFieldName(field), parameterTypes);
 
         Method method = methodCache.get(nameHash);
 
         if (method == null) {
             method = object.getClass().getMethod(getGetterFieldName(field), parameterTypes);
 
             methodCache.put(nameHash, method);
         }
 
         return method;
     }
 
     public String getGetterFieldName(String fieldName) {
         StringBuilder result = new StringBuilder();
         int fieldLength = fieldName.length();
 
         if (fieldLength == 0) {
             return null;
         }
 
         result.append("get");
         result.append(fieldName.substring(0, 1).toUpperCase());
 
         if (fieldLength > 1) {
             result.append(fieldName.substring(1));
         }
 
         return result.toString();
     }
 
     public String getSetterFieldName(String fieldName) {
         StringBuilder result = new StringBuilder();
 
         assert fieldName.length() > 1 : "fieldName must be longer than 1 character!";
 
         result.append("set");
         result.append(fieldName.substring(0, 1).toUpperCase());
         result.append(fieldName.substring(1));
 
         return result.toString();
     }
 
 
     public Object getMethodValue(Object model, String... fields) {
         return getMethodValue(model, 0, fields);
     }
 
     @CheckForNull
     public Object getMethodValue(Object model, int skip, String... fields) {
         Object result = null;
 
         try {
             if (fields.length > (skip + 1)) {
                 Object subModel = this.getMethodValue(model, fields[skip]);
 
                 result = getFieldValue(subModel, ++skip, fields);
             } else {
                 // work around for: http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=4071957
                 if (model instanceof Map.Entry) {
                     Map.Entry entry = (Map.Entry) model;
                     if (fields[skip].equals("key")) {
                         result = entry.getKey();
                     } else if (fields[skip].equals("value")) {
                         result = entry.getValue();
                     }
                 } else {
                     Method method = findGetMethod(model, fields[skip]);
 
                     if (method == null) {
                         throw new IllegalStateException("Can't find method " + fields[skip] + " in model " + model + ".");
                     }
 
                     result = method.invoke(model);
 
                     if (result instanceof SimpleReference) {
                         SimpleReference ref = (SimpleReference) result;
 
                         result = ref.get();
 
                         if (result == null) {
                             result = ref;
                         }
                     }
                 }
             }
         } catch (NoSuchMethodException e) {
             throw new IllegalArgumentException(e);
         } catch (IllegalAccessException e) {
             throw new IllegalArgumentException(e);
         } catch (InvocationTargetException e) {
             throw new IllegalArgumentException(e);
         }
 
         return result;
     }
 
     public Object getFieldValue(Object model, String... fields) {
         return getFieldValue(model, 0, fields);
     }
 
     @CheckForNull
     public Object getFieldValue(Object model, int skip, String... fields) {
         Object result = null;
 
         try {
             if (fields.length > (skip + 1)) {
                 Object subModel = this.getFieldValue(model, fields[skip]);
 
                 result = getFieldValue(subModel, ++skip, fields);
             } else {
 
                 Field field = getField(model, fields[skip]);
 
                 if (field == null) {
                     throw new IllegalStateException("Can't find field " + field + " in model " + model + ".");
                 }
 
                 result = field.get(model);
 
                 if (result instanceof SimpleReference) {
                     SimpleReference ref = (SimpleReference) result;
 
                     if (ref != null) {
                         result = ref.get();
 
                         if (result == null) {
                             result = ref;
                         }
                     }
                 }
             }
         } catch (IllegalAccessException e) {
             throw new IllegalArgumentException(e);
         }
 
         return result;
     }
 
     public java.util.List getFieldValues(Object model, String... fields) {
         return getFieldValues(model, 0, fields);
     }
 
     public java.util.List getFieldValues(Object model, int skip, String... fields) {
         java.util.List result = new LinkedList<Object>();
 
         try {
             if (fields.length > (skip + 1)) {
                 Object subModel = this.getFieldValue(model, fields[skip]);
 
                 if (subModel instanceof SimpleList) {
                     ++skip;
                     for (Object pm : ((SimpleList) subModel)) {
                         result.addAll(getFieldValues(pm, skip, fields));
                     }
                 } else {
                     if (subModel != null) {
                         result.addAll(getFieldValues(subModel, ++skip, fields));
                     } else {
                         System.out.println(model.getClass() + "." + fields[skip] + " == null");
                     }
                 }
             } else {
                 Field field = getField(model, fields[skip]);
 
                 if (field == null) {
                     throw new IllegalStateException("Can't find field " + field + " in model " + model + ".");
                 }
 
                 Object tmpResult = field.get(model);
 
                 if (tmpResult instanceof SimpleReference) {
                     tmpResult = ((SimpleReference) tmpResult).get();
                 }
 
                 result.add(tmpResult);
             }
         } catch (IllegalAccessException e) {
             throw new IllegalArgumentException(e);
         }
 
         return result;
     }
 
     public Object invoke(Object object, String methodName) {
         try {
             Method method = findMethod(object, methodName);
 
             if (method == null) {
                 return null;
             } else {
                 return method.invoke(object, new Object[0]);
             }
         } catch (NoSuchMethodException e) {
             throw new IllegalStateException(e);
         } catch (InvocationTargetException e) {
             throw new IllegalStateException(e);
         } catch (IllegalAccessException e) {
             throw new IllegalStateException(e);
         }
     }
 
     public Object invoke(Object object, String methodName, Object... parameters) {
         if (object == null) {
             throw new IllegalStateException("Impossible to invoke method " + methodName + " on null object.");
         }
 
         if (methodName == null) {
             throw new IllegalStateException("Impossible to invoke null method on object " + object + ".");
         }
 
         try {
             Class<?>[] parameterTypes = new Class<?>[parameters.length];
 
             for (int i = 0; i < parameterTypes.length; i++) {
                 parameterTypes[i] = parameters[i].getClass();
             }
 
             Method method = findMethod(object, methodName, parameterTypes);
 
             return method.invoke(object, parameters);
         } catch (NoSuchMethodException e) {
             throw new IllegalStateException("Method not include: " + object.getClass().getName() + ":" + methodName, e);
         } catch (InvocationTargetException e) {
             throw new IllegalStateException(e);
         } catch (IllegalAccessException e) {
             throw new IllegalStateException(e);
         }
     }
 
     private int getHashCode(Object... objects) {
         int result = 5;
 
         for (Object o : objects) {
             result += (result * 5) + o.hashCode();
         }
 
         return result;
     }
 
     public Field getField(Object model, String fieldName) {
         if (model == null) {
             throw new IllegalStateException("Model is null, can't get field " + fieldName + ".");
         }
 
         return getField(model.getClass(), fieldName);
     }
 
     public Field getField(Class model, String fieldName) {
 
         Field result = null;
 
         Integer nameHash = getFullNameHash(model, fieldName);
 
         Field field = fieldCache.get(nameHash);
 
         if (field == null) {
             Class cls = model;
 
             do {
                 for (Field f : cls.getDeclaredFields()) {
                     if (f.getName().equals(fieldName)) {
                         field = f;
                         break;
                     }
                 }
 
                 cls = cls.getSuperclass();
             } while (field == null && cls != null);
 
             if (field == null) {
                 throw new IllegalStateException("Field " + fieldName + " not found in " + model + ".");
             } else {
                 field.setAccessible(true);
 
                 fieldCache.put(nameHash, field);
             }
         }
 
         result = field;
 
         return result;
     }
 
     public void setFieldValue(Object model, String fieldName, Object value) {
         try {
             Field field = getField(model, fieldName);
 
             field.set(model, value);
         } catch (IllegalAccessException e1) {
             throw new IllegalStateException(e1);
         }
     }
 
     public List<Field> getFieldsFromClass(Class<?> typeClass) {
         List<Field> result = classFieldCache.get(typeClass);
 
         if (result == null) {
             result = new LinkedList<Field>();
 
             do {
                 Field[] fields = typeClass.getDeclaredFields();
 
                 for (Field field : fields) {
                     field.setAccessible(true);
                     result.add(0, field);
                 }
 
                 typeClass = typeClass.getSuperclass();
             } while (!typeClass.equals(Object.class));
 
             classFieldCache.put(typeClass, result);
         }
 
         return result;
     }
 
     public List<Field> getReferenceFieldsFromClass(Class<?> typeClass) {
         List<Field> result = referenceFieldCache.get(typeClass);
 
         if (result == null) {
             result = new LinkedList<Field>();
 
             do {
                 Field[] fields = typeClass.getDeclaredFields();
 
                 for (Field field : fields) {
                     if (field.getType().equals(SimpleReference.class)) {
                         field.setAccessible(true);
 
                         result.add(0, field);
                     }
                 }
 
                 typeClass = typeClass.getSuperclass();
             } while (!typeClass.equals(Object.class));
 
             referenceFieldCache.put(typeClass, result);
         }
 
         return result;
     }
 
     public List<Field> getListFieldsFromClass(Class<?> typeClass) {
         List<Field> result = listFieldCache.get(typeClass);
 
         if (result == null) {
             result = new LinkedList<Field>();
 
             do {
                 Field[] fields = typeClass.getDeclaredFields();
 
                 for (Field field : fields) {
                     if (field.getType().equals(SimpleList.class)) {
                         field.setAccessible(true);
 
                         result.add(0, field);
                     }
                 }
 
                 typeClass = typeClass.getSuperclass();
             } while (!typeClass.equals(Object.class));
 
             listFieldCache.put(typeClass, result);
         }
 
         return result;
     }
 
     public List<Field> getSetFieldsFromClass(Class<?> typeClass) {
         List<Field> result = setFieldCache.get(typeClass);
 
         if (result == null) {
             result = new LinkedList<Field>();
 
             do {
                 Field[] fields = typeClass.getDeclaredFields();
 
                 for (Field field : fields) {
                     if (field.getType().equals(SimpleSet.class)) {
                         field.setAccessible(true);
 
                         result.add(0, field);
                     }
                 }
 
                 typeClass = typeClass.getSuperclass();
             } while (!typeClass.equals(Object.class));
 
             setFieldCache.put(typeClass, result);
         }
 
         return result;
     }
 
     public String toString() {
         StringBuilder result = new StringBuilder();
 
         for (Integer methodHash : fieldCache.keySet()) {
             result.append("Field:  ");
             result.append(methodHash);
             result.append("\n");
         }
 
         for (Integer methodHash : methodCache.keySet()) {
             result.append("Method: ");
             result.append(methodHash);
             result.append("\n");
         }
 
         return result.toString();
     }
 
     /*
     public SimpleDao getDao(Object model, String field) {
         Object object = getFieldValue(model, field);
         Dao dao = null;
         
         if (object == null) {
             Class cls = getField(model, field).getType();
             
             if (cls.equals(SimpleReference.class)) {
                 throw new IllegalStateException("SimpleReference should never be null ("+model+"."+field+")");
             } else {
                 dao = (Dao)cls.getAnnotation(Dao.class);
             }
         } else if (object instanceof SimpleReference) {
             Class cls = ((SimpleReference)object).getType();
                 
             dao = (Dao)cls.getAnnotation(Dao.class);
         } else {
             dao = object.getClass().getAnnotation(Dao.class);
         }
         
         if (dao == null) {
             throw new IllegalStateException("Class "+object.getClass()+" ("+model+"."+field+") is missing Dao annotation!");
         }
 
         try {
             return dao.value().newInstance();
         } catch (InstantiationException e) {
             throw new IllegalStateException(e);
         } catch (IllegalAccessException e) {
             throw new IllegalStateException(e);
         }
     }*/
 
     public void copyPrevaylerReferenceAndListProperties(@Nonnull SimpleModel source, @Nonnull SimpleModel target) {
         assert source.getClass().equals(target.getClass());
 
         try {
             for (Field field : getReferenceFieldsFromClass(target.getClass())) {
                 SimpleReference ref = (SimpleReference) field.get(source);
 
                 if (ref != null) {
                     SimpleReference newRef = new SimpleReference(ref.getType(), ref.getId());
                     field.set(target, newRef);
                 } else {
                     field.set(target, null);
                 }
             }
 
             for (Field field : getListFieldsFromClass(target.getClass())) {
                 SimpleList list = (SimpleList) field.get(source);
                 if (list != null) {
                     SimpleList newList = new SimpleList(list.getType());
 
                     for (Object id : list.getIdList()) {
                         newList.add((Long) id);
                     }
 
                     field.set(target, newList);
                 } else {
                     field.set(target, null);
                 }
             }
 
             for (Field field : getSetFieldsFromClass(target.getClass())) {
                 SimpleSet set = (SimpleSet) field.get(source);
                 if (set != null) {
                     SimpleSet newSet = new SimpleSet(set.getType());
 
                     for (Object id : set.getIdSet()) {
                         newSet.add((Long) id);
                     }
 
                     field.set(target, newSet);
                 } else {
                     field.set(target, null);
                 }
             }
 
         } catch (IllegalAccessException e) {
             throw new IllegalStateException(e);
         }
     }
 
 }
