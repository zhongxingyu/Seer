 package suite.util;
 
 import java.lang.reflect.Field;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.concurrent.ConcurrentHashMap;
 
 /**
  * .. Converts (supposedly) any Java structures to a recursive map.
  * 
  * @author ywsing
  */
 public class InspectUtil {
 
 	private static InspectUtil instance = new InspectUtil();
 
 	private Map<Class<?>, ClassInformation> clazzInformation = new ConcurrentHashMap<>();
 
 	private class ClassInformation {
 		private List<Field> fields;
 	}
 
 	private InspectUtil() {
 	}
 
 	public static Object mapify(Object object) {
 		Object result;
 
 		if (object != null) {
 			Class<?> clazz = object.getClass();
 
 			if (clazz.isPrimitive() //
 					|| clazz.isEnum() //
 					|| clazz == Boolean.class //
 					|| clazz == String.class //
 					|| Number.class.isAssignableFrom(clazz))
 				result = object;
 			else {
 				Map<Object, Object> map = new HashMap<>();
 				map.put("@class", clazz.getCanonicalName());
 
 				if (clazz.isArray()) {
 					Object[] array = (Object[]) object;
 					for (int i = 0; i < array.length; i++)
 						map.put(i, mapify(array[i]));
 				} else if (Collection.class.isAssignableFrom(clazz)) {
 					Collection<?> col = (Collection<?>) object;
 					int i = 0;
 
 					for (Object elem : col)
 						map.put(i++, mapify(elem));
 				} else if (Map.class.isAssignableFrom(clazz))
 					for (Entry<?, ?> entry : ((Map<?, ?>) object).entrySet())
 						map.put(mapify(entry.getKey()), mapify(entry.getValue()));
 				else
 					for (Field field : instance.getFields(object))
 						try {
 							map.put(mapify(field.getName()), mapify(field.get(object)));
 						} catch (IllegalAccessException ex) {
 							throw new RuntimeException(ex);
 						}
 
 				result = map;
 			}
 		} else
 			result = null;
 
 		return result;
 	}
 
 	public static <T> boolean equals(T o0, T o1) {
 		boolean result;
 		if (o0 != o1)
 			result = o0 != null && o1 != null //
 					&& o0.getClass() == o1.getClass() //
 					&& Util.equals(instance.toList(o0), instance.toList(o1));
 		else
 			result = true;
 		return result;
 	}
 
 	public static int hashCode(Object object) {
 		return instance.toList(object).hashCode();
 	}
 
 	private List<Object> toList(Object object) {
 		List<Object> list = new ArrayList<>();
 
 		for (Field field : getFields(object))
 			try {
 				list.add(field.get(object));
 			} catch (IllegalAccessException ex) {
 				throw new RuntimeException(ex);
 			}
 
 		return list;
 	}
 
 	private List<Field> getFields(Object object) {
 		return instance.getClassInformation(object.getClass()).fields;
 	}
 
 	private ClassInformation getClassInformation(Class<?> clazz) {
 		ClassInformation ci = clazzInformation.get(clazz);
 		if (ci == null)
 			clazzInformation.put(clazz, ci = getClassInformation0(clazz));
 		return ci;
 	}
 
 	private ClassInformation getClassInformation0(Class<?> clazz) {
 		ClassInformation parent = getClassInformation(clazz.getSuperclass());
 
 		ClassInformation ci = new ClassInformation();
 		ci.fields = new ArrayList<>(parent.fields);
 
		for (Field field : clazz.getDeclaredFields()) {
			field.setAccessible(true);
			ci.fields.add(field);
		}
 
 		return ci;
 	}
 
 }
