 package burrito.util;
 
 import java.lang.reflect.Field;
 import java.lang.reflect.Method;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 
 import siena.Model;
 
 public class EntityUtil {
 
 	public static List<Method> getMethods(Class<?> clazz) {
 		List<Method> methods = new ArrayList<Method>();
 		while(clazz != Model.class) {
 			methods.addAll(Arrays.asList(clazz.getDeclaredMethods()));
 			clazz = clazz.getSuperclass();
 		}
 		return methods;
 	}
 
 	public static List<Field> getFields(Class<?> clazz) {
 		List<Field> fields = new ArrayList<Field>();
 		while(clazz != Model.class) {
 			fields.addAll(Arrays.asList(clazz.getDeclaredFields()));
 			clazz = clazz.getSuperclass();
 		}
 		return fields;
 	}
 
 	public static Field getField(Class<?> clazz, String name) {
 		while(clazz != Model.class) {
 			try {
 				return clazz.getDeclaredField(name);
 			} catch (NoSuchFieldException e) {
 				//expected
 			}
 			clazz = clazz.getSuperclass();
 		}
 		return null;
 	}
 }
