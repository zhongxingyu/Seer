 package org.dynamicvalues;
 
 import static java.lang.System.*;
 import static java.lang.reflect.Modifier.*;
 
 import java.lang.reflect.Array;
 import java.lang.reflect.Field;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.LinkedHashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.dynamicvalues.Externals.ValueList;
 import org.dynamicvalues.Externals.ValueMap;
 
 /**
  * Enumeration-based engine for recursive type analysis.
  * 
  * @author Fabio Simeoni
  * 
  */
 enum Type {
 
 	valuemap {
 
 		@Override
 		Object toDynamic(Object o, Map<Integer, Object> state, Directives directives) throws Exception {
 
 			Object value = state.get(identityHashCode(o));
 			if (value != null)
 				return value;
 
 			Map<Object, Object> map = new LinkedHashMap<Object, Object>();
 
 			state.put(identityHashCode(o), map);
 
 			for (Map.Entry<Object, Object> el : ValueMap.class.cast(o).elements.entrySet())
 				map.put(el.getKey(), Dynamic.valueOf(el.getValue(), state, directives));
 
 			return map;
 		}
 	},
 
 	valuelist {
 
 		@Override
 		Object toDynamic(Object o, Map<Integer, Object> state, Directives directives) throws Exception {
 
 			Object value = state.get(identityHashCode(o));
 			
 			if (value != null)
 				return value;
 
 			List<Object> list = new ArrayList<Object>();
 
 			state.put(identityHashCode(o), list);
 
 			for (Object el : ValueList.class.cast(o).elements)
 				list.add(Dynamic.valueOf(el, state, directives));
 
 			return list;
 		}
 	},
 
 	voidtype,
 
 	atomic,
 
 	collection {
 		Object toExternal(Object o, Map<Integer, Object> state, Directives directives) throws Exception {
 
 			Object value = state.get(identityHashCode(o));
 			if (value != null)
 				return value;
 
 			List<Object> list = new ArrayList<Object>();
 
 			value = new ValueList(list);
 			
 			state.put(identityHashCode(o), value);
 
 			for (Object element : Iterable.class.cast(o))
 				list.add(Dynamic.externalValueOf(element, state, directives));
 
 			return value;
 		}
 
 		@Override
 		Object toDynamic(Object o, Map<Integer, Object> state,  Directives directives) throws Exception {
 
 			Object value = state.get(identityHashCode(o));
 			
 			if (value != null)
 				return value;
 
 			List<Object> list = new ArrayList<Object>();
 
 			state.put(identityHashCode(o), list);
 
 			for (Object element : Iterable.class.cast(o))
 				list.add(Dynamic.valueOf(element, state, directives));
 
 			return list;
 		}
 	},
 
 	array {
 		Object toExternal(Object o, Map<Integer, Object> state, Directives directives) throws Exception {
 
 			Object value = state.get(identityHashCode(o));
 			if (value != null)
 				return value;
 
 			List<Object> list = new ArrayList<Object>();
 
 			value = new ValueList(list);
 
 			state.put(identityHashCode(o), value);
 
 			for (int i = 0; i < Array.getLength(o); i++)
 				list.add(Dynamic.externalValueOf(Array.get(o, i), state, directives));
 
 			return value;
 		}
 
 		@Override
 		Object toDynamic(Object o, Map<Integer, Object> state,  Directives directives) throws Exception {
 
 			Object value = state.get(identityHashCode(o));
 
 			if (value != null)
 				return value;
 
 			List<Object> list = new ArrayList<Object>();
 
 			state.put(identityHashCode(o), list);
 
 			for (int i = 0; i < Array.getLength(o); i++)
 				list.add(Dynamic.valueOf(Array.get(o, i), state, directives));
 
 			return list;
 		}
 	},
 
 	map {
 		@Override
 		Object toExternal(Object o, Map<Integer, Object> state,  Directives directives) throws Exception {
 
 			Object value = state.get(identityHashCode(o));
 
 			if (value != null)
 				return value;
 
 			Map<Object, Object> map = new LinkedHashMap<Object, Object>();
 
 			value = new ValueMap(map);
 
 			state.put(identityHashCode(o), value);
 
 			for (Map.Entry<?, ?> e : ((Map<?, ?>) o).entrySet())
 				map.put(Dynamic.externalValueOf(e.getKey(), state, directives),
 						Dynamic.externalValueOf(e.getValue(), state, directives));
 
 			return value;
 		}
 
 		@Override
 		Object toDynamic(Object o, Map<Integer, Object> state,  Directives directives) throws Exception {
 
 			Object value = state.get(identityHashCode(o));
 
 			if (value != null)
 				return value;
 
 			Map<Object, Object> map = new LinkedHashMap<Object, Object>();
 
 			state.put(identityHashCode(o), map);
 
 			for (Map.Entry<?, ?> e : ((Map<?, ?>) o).entrySet())
 				map.put(Dynamic.valueOf(e.getKey(), state, directives),
 						Dynamic.valueOf(e.getValue(), state, directives));
 
 			return map;
 		}
 	},
 
 	object {
 		
 		Object toExternal(Object o, Map<Integer, Object> state,  Directives directives) throws Exception {
 
 			Object value = state.get(System.identityHashCode(o));
 			
 			if (value != null)
 				return value;
 
 			Map<Object, Object> map = new HashMap<Object, Object>();
 
 			value = new ValueMap(map);
 			
 			state.put(System.identityHashCode(o), value);
 
 			for (Map.Entry<String,Object> field : gatherFields(o, state, directives).entrySet()) {
 				
 				Object fieldValue = Dynamic.externalValueOf(field.getValue(), state, directives);
 				
 				if (fieldValue != null)
 					map.put(field.getKey(), fieldValue);
 			}
 
 			return value;
 		}
 
 		Object toDynamic(Object o, Map<Integer, Object> state, Directives directives) throws Exception {
 
 			// have/are we already produced/producing a value for this?
 			Object value = state.get(identityHashCode(o));
 
 			// then return it
 			if (value != null)
 				return value;
 
 			// otherwise compute fresh one
 			Map<Object, Object> map = new HashMap<Object, Object>();
 
 			// and store it _before_ next recursive invocation
 			state.put(System.identityHashCode(o), map);
 			
 			for (Map.Entry<String,Object> field : gatherFields(o, state, directives).entrySet()) {
 				
 				Object fieldValue = Dynamic.valueOf(field.getValue(), state, directives);
 				if (fieldValue != null)
 					map.put(field.getKey(), fieldValue);
 			}
 
 			return map;
 		}
 		
 		//helper
 		
 		private Map<String,Object> gatherFields(Object o, Map<Integer, Object> state, Directives directives) throws Exception {
 			
 			Map<String,Object> values = new LinkedHashMap<String,Object>();
 			
 			Class<?> clazz = o.getClass();
 			
 			List<Field> fields = valueFieldsOf(o, clazz, directives);
 			for (Field field : fields) {
 				
 				field.setAccessible(true);
 				
 				Object fieldValue = field.get(o);
 				
 				//adapted value?
 				for (Mapping mapping : directives.mappings()) {
 					Object adapted = mapping.map(o,field,fieldValue);
 					if (adapted!=null) {
 						fieldValue=adapted;
 						break;
 					}
 				}
 			
 				values.put(field.getName(),fieldValue);
 			}
 			
 			return values;
 		}
 		
 		private List<Field> valueFieldsOf(Object o, Class<?> clazz, Directives directives) throws Exception {
 
 			List<Field> fields = new ArrayList<Field>();
 
 			Class<?> superclass = clazz.getSuperclass();
 
 			if (superclass != null)
 				fields.addAll(valueFieldsOf(o, superclass, directives));
 			
 			field: for (Field field : clazz.getDeclaredFields())
 					
 					if (!isStatic(field.getModifiers())) {
 							
 							for (Exclusion directive : directives.excludes())
 								if (directive.exclude(o, field))
 									continue field;
 							
 							fields.add(field);
 						}
 							
 				
 			return fields;
 		}
 	};
 
 	Object toExternal(Object o, Map<Integer, Object> state, Directives directives) throws Exception {
 
 		return o; // by default, the object is in external form
 	}
 
 	// from static to dynamic
 	Object toDynamic(Object o, Map<Integer, Object> state, Directives directives) throws Exception {
 		return o; // by default, the object is a value
 	}
 	
 	
 
 	@SuppressWarnings("all")
 	private static final List<Class<?>> atomics = Arrays.asList(String.class, Boolean.class, Character.class,
 			Byte.class, Short.class, Integer.class, Long.class, Float.class, Double.class, Void.class);
 
 	public static Type of(Object o) {
 
 		if (o == null)
 			return voidtype;
 
 		if (o instanceof ValueMap)
 			return valuemap;
 
 		if (o instanceof ValueList)
 			return valuelist;
 
 		if (atomics.contains(o.getClass()))
 			return atomic;
 
 		if (o.getClass().isArray())
 			return array;
 
 		if (o instanceof Iterable<?>)
 			return collection;
 
 		if (o instanceof Map<?, ?>)
 			return map;
 
 		return object;
 	}
 
 }
