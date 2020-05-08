 package no.henning.restful.utils;
 
 import java.lang.reflect.Field;
 import java.lang.reflect.GenericArrayType;
 import java.lang.reflect.Method;
 import java.lang.reflect.ParameterizedType;
 import java.lang.reflect.Type;
 import java.util.Collection;
 import java.util.List;
 import java.util.Map;
 
 import android.content.Context;
 import android.content.pm.PackageInfo;
 import android.content.pm.PackageManager.NameNotFoundException;
 import android.os.Build;
 import android.util.Log;
 
 import no.henning.restful.R;
 import no.henning.restful.callback.Callback;
 import no.henning.restful.converter.json.utils.TypeReference;
 import no.henning.restful.model.Model;
 import no.henning.restful.model.annotation.BelongsTo;
 import no.henning.restful.model.annotation.UsesRestService;
 import no.henning.restful.service.RestService;
 import no.henning.restful.service.annotation.Url;
 
 public class GenericHelper {

	public static final String APP_VERSION_NAME = "0.6";

 	/**
 	 * GET REST SERVICE
 	 */
 
 	public static Class<? extends RestService> getRestServiceFromModel(Class<? extends Model> model) {
 		if (model == null)
 			return null;
 
 		Log.d("restful", "getRestServiceFromModel: Trying to retrieve what RestService " + model.getSimpleName()
 				+ " uses...");
 		// Tries to retrieve the annotation that specifies a rest service from a
 		// model
 		UsesRestService annotation = model.getAnnotation(UsesRestService.class);
 
 		if (annotation == null)
 			return null;
 
 		Log.d("restful", "getRestServiceFromModel: " + model.getSimpleName() + " uses "
 				+ annotation.value().getSimpleName());
 
 		return annotation.value();
 	}
 
 	public static Class<? extends RestService> getRestServiceFromProxyMethod(Method method) {
 		if (method == null)
 			return null;
 
 		Log.d("restful", "getRestServiceFromProxyMethod: Trying to retrieve what RestService "
 				+ method.getDeclaringClass().getSimpleName() + " uses");
 
 		Class<? extends Model> model = getModelFromProxyMethod(method);
 
 		return getRestServiceFromModel(model);
 	}
 
 	public static Class<? extends Model> getModelFromProxyMethod(Method method) {
 		if (method == null)
 			return null;
 
 		Log.d("restful", "getModelFromProxyMethod: Trying to find what model "
 				+ method.getDeclaringClass().getSimpleName() + " belongs to");
 
 		// A action interface should implement a BelongsTo annotation so we can
 		// process requests properly.
 		BelongsTo annotation = method.getDeclaringClass().getAnnotation(BelongsTo.class);
 
 		if (annotation == null)
 			return null;
 
 		Log.d("restful", "getModelFromProxyMethod: Belongs to " + annotation.value().getSimpleName());
 
 		return annotation.value();
 	}
 
 	public static Url getUrlAnnotationFromClass(Class<?> clazz) {
 		if (clazz == null)
 			return null;
 
 		Url urlAnnotation = clazz.getAnnotation(Url.class);
 
 		return urlAnnotation;
 	}
 
 	public static String getResourcePathFromModel(Class<? extends Model> model) {
 		if (model == null)
 			return null;
 
 		Url urlAnnotation = getUrlAnnotationFromClass(model);
 
 		if (urlAnnotation != null)
 			return urlAnnotation.value();
 
 		// If Model hasn't specified a Url, we'll use the Model name as resource
 		// path
 
 		// TODO: This can't possibly be the way it's supposed to work
 		return model.getSimpleName().toLowerCase();
 	}
 
 	public static Class<?> getUnderlyingType(Field field) {
 		Class<?> fieldType = field.getType();
 
 		if (isArray(fieldType))
 			return fieldType.getComponentType();
 
 		if (isCollection(fieldType))
 			return getUnderlyingGenericClass((ParameterizedType) field.getGenericType());
 
 		return fieldType;
 	}
 
 	public static Class<?> getUnderlyingArrayType(Class<?> type) {
 		return type.getComponentType();
 	}
 
 	public static Class<?> getUnderlyingGenericClass(ParameterizedType type) {
 		return (Class<?>) type.getActualTypeArguments()[0];
 	}
 
 	public static Type getUnderlyingGenericType(ParameterizedType type) {
 		return type.getActualTypeArguments()[0];
 	}
 
 	public static Type[] getUnderlyingGenericTypes(ParameterizedType type) {
 		return type.getActualTypeArguments();
 	}
 
 	public static Type getUnderlyingGenericArrayType(GenericArrayType type) {
 		return type.getGenericComponentType();
 	}
 
 	public static boolean isCollection(Class<?> type) {
 		if (type == null)
 			return false;
 		if (Collection.class.isAssignableFrom(type))
 			return true;
 		if (List.class.isAssignableFrom(type))
 			return true;
 
 		// Map ain't no ordinary collection, shame on thee!
 		// if (Map.class.isAssignableFrom(type)) return true;
 
 		return false;
 	}
 
 	public static boolean isCollection(Type type) {
 		Log.d("restful", "isCollection: Checking " + type);
 
 		try {
 			if (Class.forName("java.lang.Class") == type.getClass()) {
 				Log.d("restful", "isCollection: Is not a special case, forwarding check..");
 
 				return isCollection((Class<?>) type);
 			} else if (Class.forName("org.apache.harmony.luni.lang.reflect.ImplForArray") == type.getClass()) {
 				Log.d("restful", "isCollection: Is not collection, but an array!");
 
 				return false;
 			}
 		} catch (ClassNotFoundException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 
 		ParameterizedType pType = (ParameterizedType) type;
 
 		Log.d("restful", "isCollection: Type is " + (Class<?>) pType.getRawType());
 		Log.d("restful", "isCollection: Type is " + (Class<?>) pType.getActualTypeArguments()[0]);
 
 		return isCollection((Class<?>) pType.getRawType());
 	}
 
 	public static boolean isArray(Class<?> type) {
 		if (type == null)
 			return false;
 		if (type.isArray())
 			return true;
 
 		return false;
 	}
 
 	public static boolean isArray(Type type) {
 		Log.d("restful", "isArray: Checking " + type);
 
 		try {
 			if (Class.forName("java.lang.Class") == type.getClass()) {
 				Log.d("restful", "isArray: Is not a special case, forwarding check..");
 
 				return isArray((Class<?>) type);
 			} else if (Class.forName("org.apache.harmony.luni.lang.reflect.ImplForType") == type.getClass()) {
 				Log.d("restful", "isArray: Is not array, could be a collection or simple object!");
 
 				return false;
 			}
 		} catch (ClassNotFoundException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 
 		GenericArrayType pType = (GenericArrayType) type;
 
 		Log.d("restful", "isArray: Type is " + (Class<?>) pType.getClass());
 		Log.d("restful", "isArray: Type is " + (Class<?>) pType.getGenericComponentType());
 
 		return true;
 	}
 
 	public static Type getTypeReferenceType(TypeReference<?> type) {
 		Type[] types = type.getClass().getGenericInterfaces();
 
 		Type genericType = GenericHelper.getUnderlyingGenericType((ParameterizedType) types[0]);
 
 		return genericType;
 	}
 
 	public static String getUserAgentString() {
 		StringBuilder userAgentBuilder = new StringBuilder();
 		userAgentBuilder.append("Android/");
 		userAgentBuilder.append(APP_VERSION_NAME);
 		userAgentBuilder.append(" (Android ");
 		userAgentBuilder.append(Build.VERSION.RELEASE);
 		userAgentBuilder.append(", ");
 		userAgentBuilder.append(getDeviceName());
 		userAgentBuilder.append(")");
 		return userAgentBuilder.toString();
 	}
 
 	public static String getDeviceName() {
 		String manufacturer = Build.MANUFACTURER;
 		String model = Build.MODEL;
 		if (model.startsWith(manufacturer)) {
 			return capitalize(model);
 		} else {
 			return capitalize(manufacturer) + " " + model;
 		}
 	}
 
 	private static String capitalize(String s) {
 		if (s == null || s.length() == 0) {
 			return "";
 		}
 		char first = s.charAt(0);
 		if (Character.isUpperCase(first)) {
 			return s;
 		} else {
 			return Character.toUpperCase(first) + s.substring(1);
 		}
 	}
 }
