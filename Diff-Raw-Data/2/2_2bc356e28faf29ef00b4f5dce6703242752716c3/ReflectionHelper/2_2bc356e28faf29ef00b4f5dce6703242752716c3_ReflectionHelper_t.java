 package cz.datalite.helpers;
 
 import java.lang.annotation.Annotation;
 import java.lang.reflect.*;
 import java.sql.Timestamp;
 import java.util.*;
 import java.util.logging.Level;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  * Utility pro praci s objekty
  */
 public abstract class ReflectionHelper
 {
 
     private static final Logger LOGGER = LoggerFactory.getLogger( ReflectionHelper.class );
     
     /**
      * @param value Testovany objekt
      * @return true pokud je objekt NULL
      */
     public static boolean isNull(Object value)
     {
         return (value == null);
     }
 
     /**
      * @param value Testovany objekt
      * @return true pokud je objekt NULL
      */
     public static boolean isNull(String value)
     {
         return StringHelper.isNull(value);
     }
 
     /**
      * Nastaveni hodnoty vlastnosti
      *
      * @param fieldName   Nazev vlastnosti
      * @param destination Cilovy objekt
      * @param value       Hodnota vlastnosti
      * @throws NoSuchFieldException   Vlastnost neexistuje
      * @throws NoSuchMethodException  Neexistuje setter
      * @throws java.lang.reflect.InvocationTargetException
      *                                Chyba vyvolani setteru
      * @throws IllegalAccessException Chyba pristupu
      */
     public static void setFieldValue(String fieldName, Object destination, Object value) throws NoSuchFieldException, NoSuchMethodException, InvocationTargetException, IllegalAccessException
     {
         setFieldValue(destination.getClass().getDeclaredField(fieldName), destination, value);
     }
 
     /**
      * Nastaveni hodnoty vlastnosti
      *
      * @param destinationField Vlastnosti
      * @param destination      Cilovy objekt
      * @param value            Hodnota vlastnosti
      * @throws NoSuchFieldException   Vlastnost neexistuje
      * @throws NoSuchMethodException  Neexistuje setter
      * @throws java.lang.reflect.InvocationTargetException
      *                                Chyba vyvolani setteru
      * @throws IllegalAccessException Chyba pristupu
      */
     public static void setFieldValue(Field destinationField, Object destination, Object value) throws NoSuchFieldException, NoSuchMethodException, InvocationTargetException, IllegalAccessException
     {
         String fieldName = destinationField.getName();
 
         if ((value == null) || (destinationField.getType().isAssignableFrom(value.getClass()))) {
             if (destinationField.isAccessible()) {
                 destinationField.set(destination, value);
             } else {
 
                 String methodName = fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1);
                 Method setter = destination.getClass().getMethod("set" + methodName, destinationField.getType());
 
                 setter.invoke(destination, value);
             }
         } else {
             Method convertMethod = getConvertMethod(value, destinationField.getType().getSimpleName());
 
             if (convertMethod != null) {
                 try {
                     value = convertMethod.invoke(null, value);
                 } catch (InvocationTargetException e) {
                     value = null;
                 } catch (IllegalAccessException e) {
                     value = null;
                 }
 
                 setFieldValue(fieldName, destination, value);
             } else {
                 try {
                     Object result = destinationField.getType().newInstance();
 
                     convertFields(value, result);
 
                     setFieldValue(fieldName, destination, result);
                 } catch (InstantiationException e) {
                     /* empty */
                 }
             }
         }
     }
 
     /**
      * Funkce pro ziskani iteratoru
      *
      * @param value Seznam
      * @return iterator
      */
     public static Iterator getIterator(Object value)
     {
         try {
             Method m = value.getClass().getMethod("iterator");
 
             return (Iterator) m.invoke(value);
         } catch (NoSuchMethodException e) {
             /* empty */
         } catch (InvocationTargetException e) {
             /* empty */
         } catch (IllegalAccessException e) {
             /* empty */
         }
         return null;
     }
 
     /**
      * Prevod typu
      *
      * @param original        Originalni hodnota
      * @param destinationType Cilovy typ
      * @return prevedena hodnota
      */
     public static Method getConvertMethod(Object original, String destinationType)
     {
         String methodName = "convert" + original.getClass().getSimpleName() + "To" + destinationType;
 
         try {
             return ReflectionHelper.class.getMethod(methodName, original.getClass());
         } catch (NoSuchMethodException e) {
             /* empty */
         }
 
         return null;
     }
 
     /**
      * Prevod typu
      *
      * @param original        Originalni hodnota
      * @param destinationType Cilovy typ
      * @return prevedena hodnota
      */
     public static Object convert(Object original, String destinationType)
     {
         String methodName = "convert" + original.getClass().getSimpleName() + "To" + destinationType;
 
         try {
             Method m = ReflectionHelper.class.getMethod(methodName, original.getClass());
 
             return m.invoke(null, original);
         } catch (NoSuchMethodException e) {
             /* empty */
         } catch (InvocationTargetException e) {
             /* emtpy */
         } catch (IllegalAccessException e) {
             /* empty */
         }
 
         return null;
     }
 
     /**
      * Nastaveni hodnoty vlastnosti
      *
      * @param destinationField Popis vlastnosti
      * @param source           objekt
      * @return hodnota vlastnosti
      * @throws NoSuchFieldException      Vlastnost neexistuje
      * @throws NoSuchMethodException     Neexistuje setter
      * @throws InvocationTargetException Chyba vyvolani setteru
      * @throws IllegalAccessException    Chyba pristupu
      */
     public static <T> T getFieldValue( Field destinationField, Object source ) throws NoSuchFieldException, NoSuchMethodException, InvocationTargetException, IllegalAccessException {
         if ( source == null )
             return null;
 
         if ( destinationField.isAccessible() )
             return ( T ) destinationField.get( source );
         else {
             Method getter = getFieldGetter( source.getClass(), destinationField );
 
             if ( getter != null )
                 return ( T ) getter.invoke( source );
         }
 
         return null;
     }
     
      /**
      * Get field value no matter of security. The security will be supressed for
      * method invocation. The method calls getter first, after that takes the
      * value directly.
      * 
      * @param <T> type of field
      * @param destinationField field name
      * @param source instance of object with given field
      * @return value of that field
      * @throws NoSuchFieldException  the given instance doesn't contain such field
      */
     public static <T> T getForcedFieldValue( String name, Object source ) throws NoSuchFieldException, InvocationTargetException {
         // ToDo dott notation
         // if the instance doesn't exists return null
         if ( source == null ) {
             return null;
         }
 
         // get field
         Field field = null;
         Class type = source.getClass();
         do {
             try {
                 // try to read field
                 field = type.getDeclaredField( name );
             } catch ( NoSuchFieldException ex ) {
                 // if the field doesn't exists in current class
                 // try to look up in a parent class
                 type = type.getSuperclass();
             }
             // continue looking up until field is found or there is no more parents
         } while ( field == null && !Object.class.equals(type) );
 
         if ( field == null )
             throw new NoSuchElementException( String.format( "Field '%1$s' is not present in '%1$s' class.", name, source.getClass() ) );
 
         try {
             // try existence of getter
             Method getter = getFieldGetter( source.getClass(), field );
             if ( getter == null ) {
                 // getter doesn't exists,
                 // try direct access to field
 
                 boolean accessible = field.isAccessible();
                 field.setAccessible( true );
                 T value = ( T ) field.get( source );
                 field.setAccessible( accessible );
                 return value;
             } else {
                 // getter exists, use it
 
                 boolean accessible = getter.isAccessible();
                 getter.setAccessible( true );
                 T value = ( T ) getter.invoke( source );
                 getter.setAccessible( accessible );
                 return value;
             }
         } catch ( IllegalArgumentException ex ) {
             LOGGER.error( "Field couldn't be read.", ex );
             throw new RuntimeException( ex );
         } catch ( IllegalAccessException ex ) {
             LOGGER.error( "Field couldn't be read.", ex );
             throw new RuntimeException( ex );
         }
     }
 
     /**
      * Vyhledani gettru fieldu podle typu
      *
      * @param source Zdrojovy objekt, jehoz field se ma vyhledat
      * @param type   Typ hledane polozky
      * @return hodnota fieldu
      */
     public static Object getFieldByType(Object source, Class type)
     {
         for (Method method : source.getClass().getMethods()) {
             if ((method.getReturnType() == type)
                     && (method.getParameterTypes().length == 0)
                     && ((method.getName().startsWith("get"))
                     || (method.getName().startsWith("is")))) {
                 try {
                     return method.invoke(source);
                 } catch (IllegalAccessException e) {
                     /* empty */
                 } catch (InvocationTargetException e) {
                     /* empty */
                 }
             }
         }
 
         return null;
     }
 
     /**
      * Automaticky prevod promitivnich typu
      *
      * @param source      Zdrojovy objekt
      * @param destination Cilovy objekt
      */
     public static void convertFields(Object source, Object destination)
     {
         if ((source != null) && (destination != null)) {
             for (Field sourceField : source.getClass().getDeclaredFields()) {
                 if ((!sourceField.getType().isArray()) && (sourceField.getType() != List.class)) {
                     try {
                         setFieldValue(sourceField.getName(), destination, getFieldValue(sourceField.getName(), source));
                     } catch (NoSuchFieldException e) {
                         /* empty */
                     } catch (NoSuchMethodException e) {
                         /* empty */
                     } catch (InvocationTargetException e) {
                         /* empty */
                     } catch (IllegalAccessException e) {
                         /* empty */
                     }
                 }
             }
         }
     }
 
     /**
      * Automaticky prevod promitivnich typu
      *
      * @param source      Zdrojovy objekt
      * @param destination Cilovy objekt
      * @param onlyNull    Priznak (true) zda se maji kopirovat ze zdrojoveho objektu vlastnosti pouze v pripade, ze cil je null
      */
     public static void convertFields(Object source, Object destination, boolean onlyNull)
     {
         if ((source != null) && (destination != null)) {
             for (Field sourceField : source.getClass().getDeclaredFields()) {
                 if ((!sourceField.getType().isArray()) && (sourceField.getType() != List.class)) {
                     try {
                         if ((!onlyNull) || (getFieldValue(sourceField.getName(), destination) == null)) {
                             setFieldValue(sourceField.getName(), destination, getFieldValue(sourceField.getName(), source));
                         }
                     } catch (NoSuchFieldException e) {
                         /* empty */
                     } catch (NoSuchMethodException e) {
                         /* empty */
                     } catch (InvocationTargetException e) {
                         /* empty */
                     } catch (IllegalAccessException e) {
                         /* empty */
                     }
                 }
             }
         }
     }
 
     /**
      * Prevod retezce na datum
      *
      * @param date Prevadene datum
      * @return prevedene datum
      */
     public static Timestamp convertStringToTimestamp(String date)
     {
         return DateHelper.fromString(date);
     }
 
     /**
      * Prevod retezce na datum
      *
      * @param date Prevadene datum
      * @return prevedene datum
      */
     public static String convertTimestampToString(Timestamp date)
     {
         return StringHelper.fromTimestamp(date);
     }
 
     /**
      * Provnani dvou objektu zda jsou stejne
      *
      * @param valueOne Prvni objekt
      * @param valueTwo Druhy objekt
      * @return true pokud jsou objekty stejne
      */
     public static boolean isEquals(Object valueOne, Object valueTwo)
     {
         if ((valueOne instanceof String) && (valueTwo instanceof Character)) {
             return isEquals((Character) valueTwo, (String) valueOne);
         }
 
         if ((valueTwo instanceof String) && (valueOne instanceof Character)) {
             return isEquals((String) valueTwo, (Character) valueOne);
         }
 
         return (((valueOne == null) && (valueTwo == null))
                 || ((valueOne != null) && (valueOne.equals(valueTwo))));
     }
 
     /**
      * Provnani dvou objektu zda jsou stejne
      *
      * @param valueOne Prvni objekt
      * @param valueTwo Druhy objekt
      * @return true pokud jsou objekty stejne
      */
     public static boolean isEquals(Character valueOne, String valueTwo)
     {
         return (((valueOne == null) && (valueTwo == null))
                 || ((valueOne != null) && (valueOne.equals(valueTwo.charAt(0)))));
     }
 
     /**
      * Provnani dvou objektu zda jsou stejne
      *
      * @param valueOne Prvni objekt
      * @param valueTwo Druhy objekt
      * @return true pokud jsou objekty stejne
      */
     public static boolean isEquals(String valueOne, Character valueTwo)
     {
         return isEquals(valueTwo, valueOne);
     }
 
     /**
      * Zjisteni zda je instance vlastnosti null
      *
      * @param owner Vlastnik vlastnosti
      * @param field Nazev vlastnosti
      * @return true pokud je vlastost null
      * @throws IllegalAccessException Chyba pristupu k definici ciloveho objektu
      * @throws NoSuchFieldException   Polozka cilohove objektu neexistuje
      * @throws NoSuchMethodException  Metoda pro nastaveni polozky neexistuje
      * @throws java.lang.reflect.InvocationTargetException
      *                                Chyba pri zjistovani polozky objektu
      */
     public static boolean isNullInstance(Object owner, String field) throws InvocationTargetException, NoSuchMethodException, NoSuchFieldException, IllegalAccessException
     {
         return ReflectionHelper.isNull(ReflectionHelper.getFieldValue(field, owner));
     }
 
     /**
      * Zjisteni zda je instance vlastnosti null
      *
      * @param owner Vlastnik vlastnosti
      * @param field Popis vlastnosti
      * @return true pokud je vlastost null
      * @throws IllegalAccessException Chyba pristupu k definici ciloveho objektu
      * @throws NoSuchFieldException   Polozka cilohove objektu neexistuje
      * @throws NoSuchMethodException  Metoda pro nastaveni polozky neexistuje
      * @throws java.lang.reflect.InvocationTargetException
      *                                Chyba pri zjistovani polozky objektu
      */
     public static boolean isNullInstance(Object owner, Field field) throws InvocationTargetException, NoSuchMethodException, NoSuchFieldException, IllegalAccessException
     {
         return ReflectionHelper.isNull(ReflectionHelper.getFieldValue(field, owner));
     }
 
     /**
      * Vyvolani setteru
      *
      * @param fieldName Nazev polozky
      * @param instance  Vlastnik polozky
      * @param value     Nastavovana hodnota
      * @throws IllegalAccessException Chyba pristupu k definici ciloveho objektu
      * @throws NoSuchMethodException  Metoda pro nastaveni polozky neexistuje
      * @throws java.lang.reflect.InvocationTargetException
      *                                Chyba pri zjistovani polozky objektu
      */
     public static void callSetter(String fieldName, Object instance, Object value) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException
     {
         if (instance != null) {
             String methodName = fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1);
             Method setter = instance.getClass().getMethod("set" + methodName, value.getClass());
 
             if (setter != null) {
                 setter.invoke(instance, value);
             }
         }
     }
 
     /**
      * Vyvolani setteru
      *
      * @param methodName Nazev methody
      * @param instance   Vlastnik methody
      * @param value      Nastavovana hodnota
      * @throws IllegalAccessException Chyba pristupu k definici ciloveho objektu
      * @throws NoSuchMethodException  Metoda pro nastaveni polozky neexistuje
      * @throws java.lang.reflect.InvocationTargetException
      *                                Chyba pri zjistovani polozky objektu
      */
     public static void invokeMethod(String methodName, Object instance, Object value) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException
     {
         if (instance != null) {
             Method setter = instance.getClass().getMethod(methodName, value.getClass());
 
             if (setter != null) {
                 setter.invoke(instance, value);
             }
         }
     }
 
     /**
      * Nastaveni hodnoty vlastnosti
      *
      * @param fieldName Nazev vlastnosti
      * @param source    objekt
      * @return hodnota vlastnosti
      * @throws NoSuchFieldException      Vlastnost neexistuje
      * @throws NoSuchMethodException     Neexistuje setter
      * @throws InvocationTargetException Chyba vyvolani setteru
      * @throws IllegalAccessException    Chyba pristupu
      */
     public static Object getFieldValue(String fieldName, Object source) throws NoSuchFieldException, NoSuchMethodException, InvocationTargetException, IllegalAccessException
     {
         int dot = fieldName.indexOf('.');
 
         while (dot > 0) {
             String field = fieldName.substring(0, dot);
 
             source = getFieldValue(field, source);
 
             fieldName = fieldName.substring(dot + 1);
 
             dot = fieldName.indexOf('.');
         }
 
         if (source == null) {
             throw new NoSuchFieldException(fieldName);
         }
 
         return getFieldValue(source.getClass().getDeclaredField(fieldName), source);
     }
 
     /**
      * Funkce vraci prvni ne NULL vyraz
      *
      * @param key1 prvni vyraz
      * @param key2 druhy vyraz
      * @return vyraz
      */
     public static Object nvl(Object key1, Object key2)
     {
         return (ReflectionHelper.isNull(key1)) ? key2 : key1;
     }
 
     /**
      * Funkce vraci prvni ne NULL vyraz
      *
      * @param key1 prvni vyraz
      * @param key2 druhy vyraz
      * @return vyraz
      */
     public static Class nvl(Class key1, Class key2)
     {
         return (key1 != Class.class) ? key2 : key1;
     }
 
     /**
      * Ziskani polozky
      *
      * @param clazz Trida, ve ktere se ma nachazet hledatana polozka
      * @param name  Nazev polozky
      * @return nalezena polozka
      * @throws NoSuchFieldException polozka neexistuje
      */
     public static Field getDeclaredField(Class clazz, String name) throws NoSuchFieldException
     {
         int dot = name.indexOf('.');
 
         while (dot > 0) {
             String field = name.substring(0, dot);
 
             clazz = clazz.getDeclaredField(field).getType();
 
             name = name.substring(dot + 1);
 
             dot = name.indexOf('.');
         }
 
         if (clazz == null) {
             throw new NoSuchFieldException(name);
         }
 
         return clazz.getDeclaredField(name);
     }
     
     public static boolean hasField(Class clazz, String name) {
         try {
             getDeclaredField( clazz, name );
             return true;
         } catch ( NoSuchFieldException ex ) {
             return false;
         } catch ( NoSuchElementException ex ) {
             return false;
         }
     }
 
     /**
      * Ziskani getteru dane polozky
      *
      * @param aClass Trida aktualni instance
      * @param field  Polozka
      * @return getter
      */
     public static Method getFieldGetter(Class aClass, Field field)
     {
         if (aClass == null) {
             throw new IllegalArgumentException("Class aClass is null");
         }
 
         if (field == null) {
             throw new IllegalArgumentException("field argument is null");
         }
 
         String fieldName = field.getName();
         String methodName = fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1);
 
         try {
             return aClass.getMethod("get" + methodName);
         } catch (NoSuchMethodException e) {
             try {
                return aClass.getMethod("is" + methodName);
             } catch (NoSuchMethodException e1) {
                 if (aClass.getSuperclass() != Object.class) {
                     return getFieldGetter(aClass.getSuperclass(), field);
                 }
             }
         } catch (NullPointerException e) {
             e.printStackTrace();
         }
         return null;
     }
 
     /**
      * Get the underlying class for a type, or null if the type is a variable type.
      *
      * @param type the type
      * @return the underlying class
      *
      * @author http://www.artima.com/weblogs/viewpost.jsp?thread=208860
      */
     private static Class<?> getClass(Type type)
     {
         if (type instanceof Class) {
             return (Class) type;
         } else if (type instanceof ParameterizedType) {
             return getClass(((ParameterizedType) type).getRawType());
         } else if (type instanceof GenericArrayType) {
             Type componentType = ((GenericArrayType) type).getGenericComponentType();
             Class<?> componentClass = getClass(componentType);
             if (componentClass != null) {
                 return Array.newInstance(componentClass, 0).getClass();
             } else {
                 return null;
             }
         } else {
             return null;
         }
     }
 
     /**
      * Get the actual type arguments a child class has used to extend a generic base class.
      *
      * @param baseClass the base class
      * @param childClass the child class
      * @return a list of the raw classes for the actual type arguments.
      *
      * @author http://www.artima.com/weblogs/viewpost.jsp?thread=208860
      */
     public static <T> List<Class<?>> getTypeArguments(
             Class<T> baseClass, Class<? extends T> childClass)
     {
         Map<Type, Type> resolvedTypes = new HashMap<Type, Type>();
         Type type = childClass;
         // start walking up the inheritance hierarchy until we hit baseClass
         while (!getClass(type).equals(baseClass)) {
             if (type instanceof Class) {
                 // there is no useful information for us in raw types, so just keep going.
                 type = ((Class) type).getGenericSuperclass();
             } else {
                 ParameterizedType parameterizedType = (ParameterizedType) type;
                 Class<?> rawType = (Class) parameterizedType.getRawType();
 
                 Type[] actualTypeArguments = parameterizedType.getActualTypeArguments();
                 TypeVariable<?>[] typeParameters = rawType.getTypeParameters();
                 for (int i = 0; i < actualTypeArguments.length; i++) {
                     resolvedTypes.put(typeParameters[i], actualTypeArguments[i]);
                 }
 
                 if (!rawType.equals(baseClass)) {
                     type = rawType.getGenericSuperclass();
                 }
             }
         }
 
         // finally, for each actual type argument provided to baseClass, determine (if possible)
         // the raw class for that type argument.
         Type[] actualTypeArguments;
         if (type instanceof Class) {
             actualTypeArguments = ((Class) type).getTypeParameters();
         } else {
             actualTypeArguments = ((ParameterizedType) type).getActualTypeArguments();
         }
         List<Class<?>> typeArgumentsAsClasses = new ArrayList<Class<?>>();
         // resolve types by chasing down type variables.
         for (Type baseType : actualTypeArguments) {
             while (resolvedTypes.containsKey(baseType)) {
                 baseType = resolvedTypes.get(baseType);
             }
             typeArgumentsAsClasses.add(getClass(baseType));
         }
         return typeArgumentsAsClasses;
     }
 
     /**
      * Gets public/private/protected fields from clazz and all superclasses.
      *
      * @param clazz class to inspect
      * @return list of all fields
      */
     public static List<Field> getAllFields(Class clazz)
     {
         List<Field> fields = new LinkedList<Field>();
 
         Class objOrSuper = clazz;
         do {
             fields.addAll(Arrays.asList(objOrSuper.getDeclaredFields()));
             objOrSuper = objOrSuper.getSuperclass();
         } while (objOrSuper != null);
 
         return fields;
     }
 
     /**
      * Gets public/private/protected fields from clazz and all superclasses.
      *
      * @param clazz class to inspect
      * @return list of all fields
      */
     public static List<Method> getAllMethods(Class clazz)
     {
         List<Method> methods = new LinkedList<Method>();
 
         Class objOrSuper = clazz;
         do {
             methods.addAll(Arrays.asList(objOrSuper.getDeclaredMethods()));
             objOrSuper = objOrSuper.getSuperclass();
         } while (objOrSuper != null);
 
         if (clazz.isInterface())
         {
             for (Class superInterface : getGeneralizations(clazz))
             {
                 methods.addAll(Arrays.asList(superInterface.getDeclaredMethods()));
             }
         }
 
         return methods;
     }
 
     /**
      * Finds desired annotation on the given method. If the annotation
      * is not set then method returns null.
      * @param <T> type of annotation
      * @param method investigated method
      * @param type type of annotation
      * @return found annotation or NULL if not set
      */
     public static <T> T findAnnotation( Method method, Class<T> type ) {
         for ( Annotation annotation : method.getAnnotations() ) {
             if ( type.isAssignableFrom( annotation.getClass() ) ) {
                 return ( T ) annotation;
             }
         }
         return null;
     }
 
     /**
      * Builds an <b>unordered</b> set of all interface and object classes that
      * are generalizations of the provided class.
      * @param classObject the class to find generalization of.
      * @return a Set of class objects.
      */
     public static Set<Class> getGeneralizations(Class classObject) {
         Set generalizations = new HashSet();
 
         generalizations.add(classObject);
 
         Class superClass = classObject.getSuperclass();
         if (superClass != null) {
             generalizations.addAll(getGeneralizations(superClass));
         }
 
         Class[] superInterfaces = classObject.getInterfaces();
         for (int i = 0; i < superInterfaces.length; i++) {
             Class superInterface = superInterfaces[i];
             generalizations.addAll(getGeneralizations(superInterface));
         }
 
         return generalizations;
     }
 
 }
