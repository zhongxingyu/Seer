 package org.eclipse.b3.backend.evaluator.typesystem;
 
 import java.lang.ref.WeakReference;
 import java.lang.reflect.GenericArrayType;
 import java.lang.reflect.ParameterizedType;
 import java.lang.reflect.Type;
 import java.util.List;
 import java.util.Map;
 
 import org.eclipse.b3.backend.evaluator.b3backend.B3FunctionType;
 import org.eclipse.b3.backend.evaluator.b3backend.B3JavaImport;
 import org.eclipse.b3.backend.evaluator.b3backend.BFunction;
 
 public class TypeUtils {
 	static WeakReference<TypeDistance> typeDistance = new WeakReference<TypeDistance>(null);
 	static final String lock = "";
 	public static Class<?> getRaw(Type t) {
 		if(t instanceof Class<?>)
 			return (Class<?>)t;
 		if(t instanceof ParameterizedType) {
 			ParameterizedType pt = ParameterizedType.class.cast(t);
 			return getRaw(pt.getRawType());
 		}
 		if(t instanceof GenericArrayType) {
 			GenericArrayType ga = GenericArrayType.class.cast(t);
 			return ga.getClass();
 		}
 		if(t instanceof B3JavaImport) {
 			return getRaw(B3JavaImport.class.cast(t).getType());
 		}
 		if(t instanceof B3FunctionType) {
 			B3FunctionType ft = B3FunctionType.class.cast(t);
 			return getRaw(ft.getFunctionType()); // i.e. what type of function this is B3, or Java
 		}
 		throw new UnsupportedOperationException("ONLY CLASS AND PARAMETERIZED TYPE SUPPORTED - was: "+t);
 	}
 	public static boolean isAssignableFrom(Type baseType, Type fromType)
 	{
 		if(baseType instanceof B3FunctionType)
 			return ((B3FunctionType)baseType).isAssignableFrom(fromType);
 		return getRaw(baseType).isAssignableFrom(getRaw(fromType));
 	}
 	public static boolean isArray(Type baseType) {
 		return baseType instanceof GenericArrayType || (baseType instanceof Class<?> && ((Class<?>)baseType).isArray());
 //		return getRaw(baseType).isArray();
 	}
 	/**
 	 * Returns the (best) specificity distance for an interface.
 	 * (A class may restate its implementation of an inherited interface - this will give a shorter
 	 * distance).
 	 * @param ptc
 	 * @param pc
 	 * @return
 	 */
 	@SuppressWarnings("unchecked")
 	public static int interfaceDistance(Class ptc, Class pc) {
 		if(ptc == pc)
 			return 0;
 		int best = Integer.MAX_VALUE;
 		for(Class i : pc.getInterfaces()) {
 			int distance = interfaceDistance(ptc, i);
 			if(distance < best) {
 				best = distance;
 				if(best == 0)
 					break; // unbeatable
 			}
 		}
 		return best != Integer.MAX_VALUE? best+1 : best;
 	}	
 	public static int typeDistance(Type baseType, Type queriedType) {
 		Class<?> baseClass = getRaw(baseType);
 		if(baseClass.isInterface())
 			return interfaceDistance(baseClass, getRaw(queriedType));
 		return classDistance(baseClass, getRaw(queriedType));
 	}
 	/**
 	 * Computes the specificity distance of a class
 	 * @param ptc
 	 * @param pc
 	 * @return
 	 */
 	@SuppressWarnings("unchecked")
 	public static int classDistance(Class ptc, Class pc) {
 		if(pc == null)
 			throw new IllegalArgumentException("Internal error: type is not a specialization of the class");
 		if(ptc == pc)
 			return 0;
		if(pc.isInterface())
			return classDistance(ptc, Object.class)+1;
 		return classDistance(ptc, pc.getSuperclass())+1;
 	}
 	/**
 	 * Returns the element type of a list or the value type of map
 	 * @param baseType
 	 * @return
 	 */
 	public static Type getElementType(Type baseType) {
 		if(baseType instanceof ParameterizedType) {
 			ParameterizedType pt = ParameterizedType.class.cast(baseType);
 			int returnTypeIdx = -1;
 			if(List.class.isAssignableFrom(getRaw(pt.getRawType())))
 					returnTypeIdx = 0;
 			else if (Map.class.isAssignableFrom(getRaw(pt.getRawType())))
 				returnTypeIdx = 1;
 			Type[] typeargs = pt.getActualTypeArguments();
 			// TODO: probably too relaxed, if it was not a List<?> or Map<?>, then Object is returned
 			//
 			if(returnTypeIdx < 0 || typeargs == null || typeargs.length <= returnTypeIdx)
 				return Object.class; // unspecified 
 			return typeargs[returnTypeIdx];
 		}
 		// TODO: probably too relaxed
 		return Object.class;
 	}
 	public static Type getCommonSuperType(Type[] types) {
 		TypeDistance td = null;;
 		synchronized (lock) {
 			td = typeDistance.get();
 			if(td == null)
 				typeDistance = new WeakReference<TypeDistance>(td = new TypeDistance());
 		}
 		return td.getMostSpecificCommonType(types);
 //		Class<?>[] classes = new Class[types.length];
 //		for(int i = 0; i < types.length; i++)
 //			classes[i] = getRaw(types[i]);
 //		
 //		int limit = classes.length;
 //		nexti: for(int i = 0; i < limit; i++) {
 //			nextj: for(int j = 0; j < limit; j++) {
 //				if(i == j)
 //					continue nextj;
 //				if(!classes[i].isAssignableFrom(classes[j]))
 //					continue nexti;
 //			}
 //			return classes[i];
 //		}
 //		return Object.class; // did not find any commonality
 	}
 	public static Class<?> getArrayComponentClass(Type type) {
 		if(type instanceof Class<?>)
 			return ((Class<?>)type).getComponentType();
 		if(type instanceof GenericArrayType)
 			type = ((GenericArrayType)type).getGenericComponentType();
 		else
 			throw new IllegalArgumentException("Not possible to get array component type from type:" + type);
 		
 		return getRaw(type);
 	}
 		
 }
