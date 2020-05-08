 package chaschev.lang;
 
 import chaschev.lang.reflect.ClassDesc;
 import chaschev.lang.reflect.ConstructorDesc;
 import chaschev.lang.reflect.MethodDesc;
 import chaschev.util.Exceptions;
 import com.google.common.base.Optional;
 import com.google.common.collect.AbstractIterator;
 import com.google.common.collect.FluentIterable;
 
 import java.lang.annotation.Annotation;
 import java.lang.reflect.Field;
 import java.lang.reflect.Method;
 import java.util.*;
 
 import static chaschev.lang.Lists2.projectMethod;
 import static java.util.Arrays.asList;
 
 /**
  * User: chaschev
  * Date: 5/20/13
  */
 public class OpenBean {
 
 
     public static Map<String, Object> putAll(Map<String, Object> dest, Object src) {
         try {
             for (Field field : OpenBean.getClassDesc(src.getClass()).fields) {
                 final Object v = field.get(src);
 
                 dest.put(field.getName(), v);
             }
 
             return dest;
         } catch (IllegalAccessException e) {
             throw Exceptions.runtime(e);
         }
     }
 
     public static Object putAll(Object dest, Map<String, Object> src) {
         try {
             for (Field field : OpenBean.getClassDesc(dest.getClass()).fields) {
                 final String name = field.getName();
 
                 if (src.containsKey(name)) {
                     final Object v = src.get(name);
                     field.set(dest, v);
                 }
             }
 
             return dest;
         } catch (IllegalAccessException e) {
             throw Exceptions.runtime(e);
         }
     }
 
     public static <T> ClassDesc<T> getClassDesc(Class<T> aClass) {
         return ClassDesc.getClassDesc(aClass);
     }
 
     public static <T> Optional<T> getFieldValue(Object object, String fieldName, Class<T> tClass) {
         try {
             Field field = _getField(object, fieldName);
 
             if(field == null){
                 return Optional.absent();
             }
 
             return Optional.of((T)field.get(object));
         } catch (IllegalAccessException e) {
             throw Exceptions.runtime(e);
         }
     }
 
     public static Object getFieldValue(Object object, String fieldName) {
         try {
             return _getField(object, fieldName).get(object);
         } catch (IllegalAccessException e) {
             throw Exceptions.runtime(e);
         }
     }
 
     public static Object getOrInitCollection(Object object, String fieldName) {
         try {
             final Field collectionField = _getField(object, fieldName);
 
             final Class<?> aClass = collectionField.getType();
 
             Object value = collectionField.get(object);
 
             if (value == null) {
                 if (List.class.isAssignableFrom(aClass)) {
                     value = new ArrayList();
                 } else if (Set.class.isAssignableFrom(aClass)) {
                     value = new HashSet();
                 } else if (Map.class.isAssignableFrom(aClass)) {
                     value = new HashMap();
                 } else {
                     throw new IllegalStateException("could not init collection/map for field: " + fieldName + " of class " + object.getClass());
                 }
 
                 collectionField.set(object, value);
             }
 
             return value;
         } catch (IllegalAccessException e) {
             throw Exceptions.runtime(e);
         }
     }
 
     public static Optional<Field> getField(Object object, String fieldName) {
         return Optional.fromNullable(_getField(object, fieldName));
     }
 
     private static Field _getField(Object object, String fieldName) {
         return getClassDesc(object.getClass()).getField(fieldName);
     }
 
     public static Optional<MethodDesc> getMethod(Object object, String methodName, Object... params) {
         MethodDesc methodDesc = getClassDesc(object.getClass()).getMethodDesc(methodName, false, params);
         return  Optional.fromNullable(methodDesc);
     }
 
     public static void setField(Object object, String fieldName, Object value) {
         try {
             _getField(object, fieldName).set(object, value);
         } catch (IllegalAccessException e) {
             throw Exceptions.runtime(e);
         }
     }
 
     public static void copyFields(Object dest, Object src) {
         copyFields(dest, src, DEFAULT_HANDLER);
     }
 
     public static void copyFields(Object dest, Map src) {
         copyFields(dest, src, DEFAULT_HANDLER);
     }
 
     public static abstract class CustomCopyHandler {
         public boolean handle(Field field1, Field field2, Object dest, Object src, String name) throws Exception {
             return handle(field1, field2.get(dest), src, name);
         }
 
         public boolean handle(Field destField, Object srcValue, Object dest, String name) throws Exception {
             throw new UnsupportedOperationException("todo");
         }
     }
 
     private static final CustomCopyHandler DEFAULT_HANDLER = new CustomCopyHandler() {
         @Override
         public final boolean handle(Field field1, Field field2, Object dest, Object src, String name) throws Exception {
             field1.set(dest, field2.get(src));
             return true;
         }
 
         @Override
         public boolean handle(Field destField, Object dest, Object srcValue, String name) throws Exception {
             final Class<?> destClass = destField.getType();
             if (destClass.isEnum() && srcValue instanceof String) {
                 srcValue = Enum.valueOf((Class<Enum>) destClass, (String) srcValue);
             }
             try {
                 destField.set(dest, srcValue);
             } catch (Exception e) {
                 throw Exceptions.runtime(e);
             }
             return true;
         }
     };
 
     public static void copyFields(Object dest, Map<String, ?> src, final CustomCopyHandler handler) {
         final ClassDesc destClassDesc = getClassDesc(dest.getClass());
 
         try {
             for (Map.Entry entry : src.entrySet()) {
                 final String name = (String) entry.getKey();
                 final Field destField = destClassDesc.getField(name);
 
                 if (destField == null) {
                     continue;
                 }
 
                 final Object srcValue = entry.getValue();
 
                 if (!(handler != null && handler.handle(destField, dest, srcValue, name))) {
                     DEFAULT_HANDLER.handle(destField, dest, srcValue, name);
                 }
             }
         } catch (Exception e) {
             throw Exceptions.runtime(e);
         }
     }
 
     public static void copyFields(Object dest, Object src, final CustomCopyHandler handler) {
         final ClassDesc destClassDesc = getClassDesc(dest.getClass());
         final ClassDesc srcClassDesc = getClassDesc(src.getClass());
 
         int i1 = 0, i2 = 0;
 
         final int l1 = destClassDesc.fields.length;
         final int l2 = srcClassDesc.fields.length;
 
         try {
             while (true) {
                 final Field field1 = destClassDesc.fields[i1];
                 final Field field2 = srcClassDesc.fields[i2];
 
                 final String name1 = field1.getName();
                 final String name2 = field2.getName();
 
                 final int r = name1.compareTo(name2);
 
                 if (r == 0) {
                     if (handler == null || !handler.handle(field1, field2, dest, src, name1)) {
                         DEFAULT_HANDLER.handle(field1, field2, dest, src, name1);
                     }
 
                     i1++;
                     i2++;
 
                     if (i1 >= l1 || i2 >= l2) break;
                 } else if (r < 0) {
                     i1++;
                     if (i1 >= l1) break;
                 } else {
                     i2++;
                     if (i2 >= l2) break;
                 }
             }
         } catch (Exception e) {
             throw Exceptions.runtime(e);
         }
     }
 
     private static boolean isClass(Object object) {
         return object instanceof Class;
     }
 
     public static Object getStaticFieldValue(Class aClass, String name) {
         try {
             final Field field = OpenBean.getClassDesc(aClass).getStaticField(name);
 
             if (field == null) {
                 throw new RuntimeException("no such field '" + name + "' " + " in class " + aClass);
             }
 
             return field.get(aClass);
         } catch (IllegalAccessException e) {
             throw Exceptions.runtime(e);
         }
     }
 
     public static Object invokeStatic(Class aClass, String name, Object... args) {
         try {
            final MethodDesc method = OpenBean.getClassDesc(aClass).getStaticMethodDesc(name, false, true);
 
             if (method == null) {
                 throw new RuntimeException("no such method '" + name + "' " + " in class " + aClass);
             }
 
             return method.invoke(aClass, args);
         } catch (Exception e) {
             throw Exceptions.runtime(e);
         }
     }
 
     public static Object invoke(Object object, String name, Object... args) {
         try {
             final MethodDesc method = OpenBean.getClassDesc(object.getClass()).getMethodDesc(name, false, args);
 
             if (method == null) {
                 throw new RuntimeException("no such method '" + name + "' " + " in object " + object.getClass());
             }
 
             return method.invoke(object, args);
         } catch (Exception e) {
             throw Exceptions.runtime(e);
         }
     }
 
     public static Object newByClass(String className, Object... params) {
         try {
             return newInstance(Class.forName(className), params);
         } catch (ClassNotFoundException e) {
             throw Exceptions.runtime(e);
         }
     }
 
     public static Object newByClass(String className, boolean strictly, Object... params) {
         try {
             return newInstance(Class.forName(className), strictly, params);
         } catch (ClassNotFoundException e) {
             throw Exceptions.runtime(e);
         }
     }
 
     public static <T> T newInstance(Class<? extends T> aClass, Object... params) {
         return newInstance(aClass, false, params);
     }
 
     public static <T> T newInstance(Class<T> aClass, boolean strictly, Object... params){
         ConstructorDesc<T> desc = getConstructorDesc(aClass, strictly, params);
 
         if(desc == null){
             throw new chaschev.lang.reflect.NoSuchMethodException("constructor not found" +
                 ", class = " + aClass.getSimpleName() +
                 ", strict = " + strictly +
                 ", params = " + Arrays.asList(params));
         }
 
         return desc.newInstance(params);
     }
 
     public static <T> ConstructorDesc<T> getConstructorDesc(Class<T> aClass, Object... params) {
         return getConstructorDesc(aClass, false, params);
     }
 
     public static <T> ConstructorDesc<T> getConstructorDesc(Class<T> aClass, boolean strictly, Object... params) {
         return getClassDesc(aClass).getConstructorDesc(strictly, params);
     }
 
     public static <T> ConstructorDesc<T> getConstructorDesc(Class<T> aClass, Class... classes) {
         return getConstructorDesc(aClass, false, classes);
     }
 
     public static <T> ConstructorDesc<T> getConstructorDesc(Class<T> aClass, boolean strictly, Class... classes) {
         return getClassDesc(aClass).getConstructorDesc(strictly, classes);
     }
 
     public static Iterable<Field> fieldsOfType(Object obj, final Class<?> aClass) {
         return fieldsOfType(obj, aClass, false);
     }
 
     public static Iterable<Field> fieldsOfType(Object obj, final Class<?> aClass, final boolean strict) {
         return fieldsOfType(obj.getClass(), aClass, strict);
     }
 
     public static Iterable<Field> fieldsOfType(Class<?> objClass, final Class<?> fieldClass){
         return fieldsOfType(objClass, fieldClass, false);
     }
 
     public static <T> List<MethodDesc<? extends T>> methodsReturning(Object obj, final Class<? extends T> returnClass){
         return methodsReturning(obj.getClass(), returnClass);
     }
 
     public static <T> List<MethodDesc<? extends T>> methodsReturning(Class<?> objClass, final Class<? extends T> returnClass){
         return methodsReturning(objClass, returnClass, false);
     }
 
     public static <T> List<MethodDesc<? extends T>> methodsReturning(Class<?> objClass, final Class<? extends T> returnClass, boolean strict){
         List<MethodDesc<? extends T>> result = new ArrayList<MethodDesc<? extends T>>(4);
 
         for (MethodDesc method : getClassDesc(objClass).methods) {
             Class<?> actualReturnType = method.getMethod().getReturnType();
 
             if(!strict){
                 if(returnClass.isAssignableFrom(actualReturnType)){
                     result.add(method);
                 }
             }else{
                 if(returnClass ==actualReturnType) {
                     result.add(method);
                 }
             }
         }
 
         return result;
     }
 
     public static Iterable<Field> fieldsOfType(Class<?> objClass, final Class<?> fieldClass, final boolean strict){
         final Field[] fields = getClassDesc(objClass).fields;
 
         return new FluentIterable<Field>() {
             @Override
             public Iterator<Field> iterator() {
                 return new AbstractIterator<Field>() {
                     int i = 0;
                     final int l = fields.length;
                     @Override
                     protected Field computeNext() {
                         for(;i<l;){
                             final Field field = fields[i++];
                             if(strict){
                                 if(field.getType() == fieldClass){
                                     return field;
                                 }
                             }else{
                                 if(fieldClass.isAssignableFrom(field.getType())){
                                     return field;
                                 }
                             }
                         }
 
                         return endOfData();
                     }
                 };
             }
         };
     }
 
     public static List<String> fieldNames(Class<?> aClass){
         return projectMethod(fields(aClass), Field.class, String.class, "getName");
     }
 
     public static List<Field> fields(Class<?> aClass) {
         return asList(getClassDesc(aClass).fields);
     }
 
     public static List<String> methodNames(Class<?> aClass){
         return projectMethod(methods(aClass), MethodDesc.class, String.class, "getName");
     }
 
     public static List<MethodDesc> methods(Class<?> aClass) {
         return asList(getClassDesc(aClass).methods);
     }
 
 
     public static final OpenBeanInstance INSTANCE = new OpenBeanInstance();
 
 }
