 package pl.jsolve.sweetener.core;
 
 import java.lang.annotation.Annotation;
 import java.lang.reflect.Constructor;
 import java.lang.reflect.Field;
 import java.lang.reflect.Method;
 import java.util.ArrayList;
 import java.util.List;
 
 import pl.jsolve.sweetener.exception.AccessToFieldException;
 
 public class Reflections {
 
     private final static String DOT = "\\.";
 
     public static Object getFieldValue(Object o, String stringOfFieldsName) {
 
 	String[] fieldsName = stringOfFieldsName.split(DOT);
 	int levelOfNestedObject = 0;
 	Class<?> clazz = o.getClass();
 	while (!Object.class.equals(clazz)) {
 
 	    for (int i = levelOfNestedObject; i < fieldsName.length; i++) {
 		Field field = getDeclaredField(fieldsName[i], o.getClass());
 		if (field != null) {
 		    boolean isLastNestedObject = (i == fieldsName.length - 1);
 		    if (isLastNestedObject) {
 			return getValueOfField(o, field);
 		    }
 		    o = getValueOfField(o, field);
 		    levelOfNestedObject++;
 		    if (o == null) {
 			return null;
 		    }
 		}
 	    }
 	    clazz = clazz.getSuperclass();
 	}
 	throw new AccessToFieldException(String.format("The field %s does not exist", fieldsName[levelOfNestedObject]));
     }
 
     public static void setFieldValue(Object object, String stringOfFieldsName, Object value) {
 
 	String[] fieldsName = stringOfFieldsName.split(DOT);
 	int levelOfNestedObject = 0;
 	Class<?> clazz = object.getClass();
 	while (!Object.class.equals(clazz)) {
 
 	    for (int i = levelOfNestedObject; i < fieldsName.length; i++) {
 		Field field = getDeclaredField(fieldsName[i], object.getClass());
 		if (field != null) {
 		    boolean isLastNestedObject = (i == fieldsName.length - 1);
 		    if (isLastNestedObject) {
 			setValue(field, object, value);
 			return;
		    } else {
			createValueIfNull(object, field);
 		    }
 		    object = getValueOfField(object, field);
 		    levelOfNestedObject++;
 		}
 	    }
 	    clazz = clazz.getSuperclass();
 	}
 	throw new AccessToFieldException(String.format("The field %s does not exist", fieldsName[levelOfNestedObject]));
     }
 
     private static void createValueIfNull(Object object, Field field) {
 	try {
 	    Object valueOfField = getValueOfField(object, field);
 	    if (valueOfField == null) {
 		Object newInstance = field.getType().newInstance();
 		field.setAccessible(true);
 		field.set(object, newInstance);
 	    }
 	} catch (Exception ex) {
 
 	} finally {
 	    field.setAccessible(false);
 	}
     }
 
     private static Field getDeclaredField(String fieldName, Class<?> clazz) {
 	try {
 	    return clazz.getDeclaredField(fieldName);
 	} catch (Exception ex) {
 	    return null;
 	}
     }
 
     private static Object getValueOfField(Object o, Field field) {
 	try {
 	    field.setAccessible(true);
 	    return field.get(o);
 	} catch (Exception e) {
 	    throw new AccessToFieldException(String.format("Exception during getting value of %s field", field.getName()));
 	} finally {
 	    field.setAccessible(false);
 	}
     }
 
     private static void setValue(Field field, Object object, Object value) {
 	try {
 	    field.setAccessible(true);
 	    field.set(object, value);
 	} catch (Exception e) {
 	    throw new AccessToFieldException(String.format("Exception during setting value of %s field", field.getName()));
 	} finally {
 	    field.setAccessible(false);
 	}
     }
 
     public static List<Class<?>> getClasses(Object object) {
	List<Class<?>> classes = new ArrayList<Class<?>>();
 	Class<?> clazz = object.getClass();
 	classes.add(clazz);
 	while (!Object.class.equals(clazz)) {
 	    clazz = clazz.getSuperclass();
 	    classes.add(clazz);
 	}
 	return classes;
     }
 
     public static List<Field> getFields(Object object) {
	List<Field> fields = new ArrayList<Field>();
 	Class<?> clazz = object.getClass();
 	while (!Object.class.equals(clazz)) {
 	    Field[] arrayOfFields = clazz.getDeclaredFields();
 	    for (int i = 0; i < arrayOfFields.length; i++) {
 		fields.add(arrayOfFields[i]);
 	    }
 	    clazz = clazz.getSuperclass();
 	}
 	return fields;
     }
 
     public static List<Annotation> getAnnotations(Object object) {
	List<Annotation> annotations = new ArrayList<Annotation>();
 	Class<?> clazz = object.getClass();
 	while (!Object.class.equals(clazz)) {
 	    Annotation[] arrayOfAnnotations = clazz.getDeclaredAnnotations();
 	    for (int i = 0; i < arrayOfAnnotations.length; i++) {
 		annotations.add(arrayOfAnnotations[i]);
 	    }
 	    clazz = clazz.getSuperclass();
 	}
 	return annotations;
     }
 
     public static List<Constructor<?>> getConstructors(Object object) {
	List<Constructor<?>> constructors = new ArrayList<Constructor<?>>();
 	Class<?> clazz = object.getClass();
 	while (!Object.class.equals(clazz)) {
 	    Constructor<?>[] declaredConstructors = clazz.getDeclaredConstructors();
 	    for (int i = 0; i < declaredConstructors.length; i++) {
 		constructors.add(declaredConstructors[i]);
 	    }
 	    clazz = clazz.getSuperclass();
 	}
 	return constructors;
     }
 
     public static List<Method> getMethods(Object object) {
	List<Method> methods = new ArrayList<Method>();
 	Class<?> clazz = object.getClass();
 	while (!Object.class.equals(clazz)) {
 	    Method[] declaredMethods = clazz.getDeclaredMethods();
 	    for (int i = 0; i < declaredMethods.length; i++) {
 		methods.add(declaredMethods[i]);
 	    }
 	    clazz = clazz.getSuperclass();
 	}
 	return methods;
     }
 
 }
