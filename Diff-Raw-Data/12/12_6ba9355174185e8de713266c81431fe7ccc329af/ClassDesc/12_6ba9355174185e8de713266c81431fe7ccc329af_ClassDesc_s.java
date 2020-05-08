 package chaschev.lang.reflect;
 
 import com.google.common.base.Preconditions;
 
 import javax.annotation.Nullable;
 import java.lang.reflect.Constructor;
 import java.lang.reflect.Field;
 import java.lang.reflect.Method;
 import java.lang.reflect.Modifier;
 import java.util.*;
 
 /**
 * @author Andrey Chaschev chaschev@gmail.com
 */
 public final class ClassDesc<T> {
     private static final Map<Class, ClassDesc> CLASS_DESC_MAP = new HashMap<Class, ClassDesc>();
     Class<T> aClass;
 
     public static <T> ClassDesc<T> getClassDesc(Class<T> aClass) {
         Preconditions.checkNotNull(aClass, "class must not be null");
 
         ClassDesc result = CLASS_DESC_MAP.get(aClass);
 
         if (result == null) {
             result = new ClassDesc(aClass);
             synchronized (CLASS_DESC_MAP) {
                 CLASS_DESC_MAP.put(aClass, result);
             }
         }
 
         return result;
     }
 
     //this is 1.5-2x faster than List
     public final Field[] fields;
     public final Field[] staticFields;
     public final MethodDesc[] staticMethods;
     public final MethodDesc[] methods;
     public final ConstructorDesc[] constructors;
 
     ClassDesc(Class aClass) {
         this.aClass = aClass;
 
         List<Field> tempFields = new ArrayList<Field>();
         List<Field> tempStaticFields = new ArrayList<Field>();
         List<Method> tempMethods = new ArrayList<Method>();
         List<Method> tempStaticMethods = new ArrayList<Method>();
 
         Constructor[] constructors = aClass.getDeclaredConstructors();
         this.constructors = new ConstructorDesc[constructors.length];
 
         for (int i = 0; i < constructors.length; i++) {
             Constructor constructor = constructors[i];
             this.constructors[i] = new ConstructorDesc(constructor);
         }
 
         while (aClass != Object.class && aClass != null) {
             Field[] fields = aClass.getDeclaredFields();
 
             for (Field field : fields) {
                 field.setAccessible(true);
 
                 final int modifiers = field.getModifiers();
 
                 if (Modifier.isStatic(modifiers)) {
                     tempStaticFields.add(field);
                 } else {
                     tempFields.add(field);
                 }
             }
 
             final Method[] methods = aClass.getDeclaredMethods();
 
             for (Method method : methods) {
                 method.setAccessible(true);
                 final int modifiers = method.getModifiers();
 
                 if (Modifier.isStatic(modifiers)) {
                     tempStaticMethods.add(method);
                 } else {
                     tempMethods.add(method);
                 }
             }
 
             aClass = aClass.getSuperclass();
         }
 
         tempFields.toArray(this.fields = new Field[tempFields.size()]);
         tempStaticFields.toArray(this.staticFields = new Field[tempStaticFields.size()]);
 
         this.methods = new MethodDesc[tempMethods.size()];
         this.staticMethods = new MethodDesc[tempStaticMethods.size()];
 
         for (int i = 0; i < tempMethods.size(); i++) {
             this.methods[i] = new MethodDesc(tempMethods.get(i));
         }
 
         for (int i = 0; i < tempStaticMethods.size(); i++) {
             this.staticMethods[i] = new MethodDesc(tempStaticMethods.get(i));
         }
 
         Arrays.sort(this.fields, FIELD_COMPARATOR);
         Arrays.sort(this.staticFields, FIELD_COMPARATOR);
         Arrays.sort(this.methods, METHOD_COMPARATOR);
         Arrays.sort(this.staticMethods, METHOD_COMPARATOR);
     }
 
     @SuppressWarnings("ForLoopReplaceableByForEach")
     @Nullable
     public Field getField(String name) {
         return findField(fields, name);
     }
 
     @SuppressWarnings("ForLoopReplaceableByForEach")
     @Nullable
     public Field getStaticField(String name) {
         return findField(staticFields, name);
     }
 
     @Nullable
     private static Field findField(Field[] array, String name) {
         final int n = array.length;
 
         for (int i = 0; i < n; i++) {
             Field field = array[i];
             if (field.getName().equals(name)) {
                 return field;
             }
         }
 
         return null;
     }
 
 
 
     private MethodDesc getMethodQuickly(String name, MethodDesc[] methods) {
         final int n = methods.length;
         for (int i = 0; i < n; i++) {
             MethodDesc method = methods[i];
             if (method.method.getName().equals(name)) {
                 return method;
             }
         }
 
         return null;
     }
 
     public ConstructorDesc<T> getConstructorDesc(final boolean strictly, Class... parameters) {
         return (ConstructorDesc<T>) findSignature(null, strictly, constructors, parameters);
     }
 
     public ConstructorDesc<T> getConstructorDesc(final boolean strictly, Object... parameters) {
         return (ConstructorDesc<T>) findSignature(null, strictly, constructors, parameters);
     }
 
     /**
      * @param strictly Strictly = false, slower, but normal java matching.
      */
     public MethodDesc getMethodDesc(String name,final boolean strictly, Object... parameters) {
         return (MethodDesc) findSignature(name, strictly, methods, parameters);
     }
 
     public MethodDesc getMethodDesc(String name,final boolean strictly, Class... parameters) {
         return (MethodDesc) findSignature(name, strictly, methods, parameters);
     }
 
     public MethodDesc getStaticMethodDesc(String name, final boolean strictly, Object... parameters) {
         return (MethodDesc) findSignature(name, strictly, staticMethods, parameters);
     }
 
     protected HavingMethodSignature findSignature(@Nullable String name, final boolean strictly, HavingMethodSignature[] signatures, Class[] parameters) {
         if (strictly) {
             for (HavingMethodSignature signature : signatures) {
                 if(name != null && !name.equals(signature.getName())){
                     continue;
                 }
 
                 if (signature.matchesStrictly(parameters)) {
                     return signature;
                 }
             }
         } else {
             for (HavingMethodSignature signature : signatures) {
                 if(name != null && !name.equals(signature.getName())){
                     continue;
                 }
 
                 if (signature.matches(parameters)) {
                     return signature;
                 }
             }
         }
 
         return null;
     }
 
     protected  HavingMethodSignature findSignature(@Nullable String name, final boolean strictly, HavingMethodSignature[] methodSignatures, Object[] parameters) {
         if (strictly) {
             for (HavingMethodSignature signature : methodSignatures) {
                 if(name != null && !name.equals(signature.getName())){
                     continue;
                 }
 
                 if (signature.matchesStrictly(parameters)) {
                     return signature;
                 }
             }
         } else {
             for (HavingMethodSignature signature : methodSignatures) {
                 if(name != null && !name.equals(signature.getName())){
                     continue;
                 }
 
                 if (signature.matches(parameters)) {
                    return signature;
                 }
             }
         }
 
         return null;
     }
 
     public static final Comparator<MethodDesc> METHOD_COMPARATOR = new Comparator<MethodDesc>() {
         @Override
         public int compare(MethodDesc o1, MethodDesc o2) {
             return o1.method.getName().compareTo(o1.method.getName());
         }
     };
     public static final Comparator<Field> FIELD_COMPARATOR = new Comparator<Field>() {
         @Override
         public int compare(Field o1, Field o2) {
             return o1.getName().compareTo(o2.getName());
         }
     };
 
     @Override
     public String toString() {
         final StringBuilder sb = new StringBuilder("ClassDesc{");
         sb.append("aClass=").append(aClass.getSimpleName());
         sb.append(", fields=").append(Arrays.toString(fields));
         sb.append('}');
         return sb.toString();
     }
 
     public T newInstance(Object... params){
         ConstructorDesc<T> constructorDesc = getConstructorDesc(false, params);
 
         if(constructorDesc == null){
             throw new IllegalArgumentException("constructor not found for params: " + Arrays.asList(params));
         }
 
         return constructorDesc.newInstance(params);
     }
 }
