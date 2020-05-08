 package togos.schemaschema;
 
 import java.util.Collection;
 import java.util.Collections;
 import java.util.HashSet;
 import java.util.LinkedHashSet;
 import java.util.Map;
 import java.util.Set;
 
 import togos.schemaschema.namespaces.Core;
 import togos.schemaschema.namespaces.Types;
 
 public class PropertyUtil
 {
 	//// Modify proprety lists
 	
 	public static <V> void addAll( Map<Predicate,Set<V>> dest, Predicate key, Collection<? extends V> values ) {
 		if( values.size() == 0 ) return;
 		
 		Set<V> vs = dest.get(key);
 		if( vs == null ) dest.put(key, vs = new LinkedHashSet<V>() );
 		vs.addAll( values );
 	}
 	
 	public static <V> void addAll( Map<Predicate,Set<V>> dest, Map<? extends Predicate,? extends Set<? extends V>> source ) {
 		for( Map.Entry<? extends Predicate,? extends Set<? extends V>> e : source.entrySet() ) {
 			Set<V> vs = dest.get(e.getKey());
 			if( vs == null ) dest.put(e.getKey(), vs = new LinkedHashSet<V>() );
 			vs.addAll( e.getValue() );
 		}
 	}
 	
 	public static <V> void add( Map<Predicate,Set<V>> dest, Predicate key, V value ) {
 		assert key != null;
 		assert value != null;
 		Set<V> vs = dest.get(key);
 		if( vs == null ) dest.put(key, vs = new LinkedHashSet<V>() );
 		vs.add( value );
 	}
 	
 	//// Query proprety lists
 
 	public static boolean hasValue( Map<Predicate,? extends Set<?>> properties, Predicate key ) {
 		Set<?> vs = properties.get(key);
 		return vs != null && vs.size() > 0;
 	}
 	
 	public static boolean hasValue( Map<Predicate,? extends Set<?>> properties, Predicate key, Object value ) {
 		Set<?> vs = properties.get(key);
 		return vs != null && vs.contains(value);
 	}
 	
 	public static <V> V toScalar( SchemaObject obj, Class<V> vClass ) {
 		return vClass.cast(obj.getScalarValue());
 	}
 	
 	public static boolean isTrue( SchemaObject obj ) {
 		if( obj == null ) return false;
 		return toScalar( obj, Boolean.class ).booleanValue();
 	}
 	
 	public static boolean isTrue( SchemaObject obj, Predicate pred ) {
 		return getFirstInheritedBoolean(obj, pred, false);
 	}
 	
 	public static boolean isMemberOf( SchemaObject obj, Type t ) {
 		if( hasValue( obj.getProperties(), Core.TYPE, t ) ) return true;
 		for( Type pt : t.getExtendedTypes() ) {
 			if( isMemberOf( obj, pt ) ) return true;
 		}
 		return false;
 	}
 	
 	public static <V> Set<V> getAll(Map<Predicate, ? extends Set<V>> properties, Predicate key ) {
 		Set<V> values = properties.get( key );
 		if( values == null || values.size() == 0 ) return Collections.emptySet();
 		return values;
 	}
 	
 	public static <T> Set<T> getAll(Map<Predicate, ? extends Set<?>> properties, Predicate key, Class<T> klass ) {
 		Set<?> values = properties.get( key );
 		if( values == null || values.size() == 0 ) return Collections.emptySet();
 		
 		LinkedHashSet<T> valuesOfTheDesiredType = new LinkedHashSet<T>();
 		for( Object v : values ) {
 			if( klass.isAssignableFrom(v.getClass()) ) {
 				valuesOfTheDesiredType.add(klass.cast(v));
 			}
 		}
 		return valuesOfTheDesiredType;
 	}
 
 	public static String objectToString(Object o) {
 		if( o instanceof SchemaObject && ((SchemaObject)o).getName() != null ) {
 			return ((SchemaObject)o).getName();
 		} else if( o == Boolean.TRUE ) {
 			return "true";
 		} else if( o == Boolean.FALSE ) {
 			return "false";
 		} else {
 			return o.toString();
 		}
 	}
 	
 	public static String pairToString(Predicate key, Object v) {
 		if( v == Boolean.TRUE ) {
 			return key.getName();
 		} else {
 			return key.getName() + " @ " + objectToString(v);
 		}
 	}
 	
 	protected static void getAllInheritedValues(SchemaObject subject, Predicate pred, Set<SchemaObject> dest) {
 		for( Object parent : getAll(subject.getProperties(), Core.EXTENDS) ) {
 			if( parent instanceof SchemaObject ) {
 				getAllInheritedValues( (SchemaObject)parent, pred, dest );
 			}
 		}
 		dest.addAll( getAll(subject.getProperties(), pred) );
 	}
 	
 	public static Set<SchemaObject> getAllInheritedValues(SchemaObject subject, Predicate pred) {
 		HashSet<SchemaObject> dest = new HashSet<SchemaObject>();
 		getAllInheritedValues( subject, pred, dest );
 		return dest;
 	}
 	
 	public static <C> Set<C> getAllInheritedValuesOfClass(SchemaObject subject, Predicate pred, Class<C> c) {
 		HashSet<SchemaObject> dest = new LinkedHashSet<SchemaObject>();
 		getAllInheritedValues( subject, pred, dest );
 		HashSet<C> destc = new LinkedHashSet<C>();
 		for( SchemaObject o : dest ) {
 			if( c.isAssignableFrom(o.getClass()) ) destc.add(c.cast(o));
 		}
 		return destc;
 	}
 
 	
 	/**
 	 * Walk up the inheritance tree (an exception will be thrown if there is a fork)
 	 * and return the first set of values found for pred. 
 	 */
 	public static Set<SchemaObject> getFirstInheritedValues( SchemaObject obj, Predicate pred ) {
 		while( obj != null ) {
 			Set<SchemaObject> values = PropertyUtil.getAll(obj.getProperties(), pred);
 			if( values.size() > 0 ) return values;
 			
 			Set<?> extended = PropertyUtil.getAll(obj.getProperties(), Core.EXTENDS);
 			
 			SchemaObject extendedObj = null;
 			for( Object o : extended ) {
 				if( o instanceof SchemaObject ) {
 					if( extendedObj != null ) {
 						throw new RuntimeException( obj.getName()+" extends more than one other SchemaObject; cannot find 'first' inherited value of "+pred.getName() );
 					}
 					extendedObj = (SchemaObject)o;
 				}
 			}
 			
 			obj = extendedObj;
 		}
 		
 		return Collections.emptySet();
 	}
 	
 	/*
 	 * In general, methods to find a single value will throw an exeption unless they find exactly one value.
 	 * If the method takes a defaultValue, that will be returned instead of throwing an exception.
 	 */
 	
 	public static SchemaObject getFirstInheritedValue( SchemaObject obj, Predicate pred, SchemaObject defaultValue ) {
 		Set<SchemaObject> values = getFirstInheritedValues(obj, pred);
 		if( values.size() > 1 ) {
 			throw new RuntimeException( obj.getName()+" has more than one value for "+pred);
 		}
 		for( SchemaObject v : values )  return v;
 		return defaultValue;
 	}
 	
 	public static SchemaObject getFirstInheritedValue( SchemaObject obj, Predicate pred ) {
 		SchemaObject val = getFirstInheritedValue( obj, pred, (SchemaObject)null );
 		if( val == null ) {
 			throw new RuntimeException("No value found for "+pred+" on "+obj.getName());
 		}
 		return val;
 	}
 
 	public static <V> V getFirstInheritedScalar( SchemaObject obj, Predicate pred, Class<V> scalarValueClass ) {
 		return toScalar( getFirstInheritedValue( obj, pred ), scalarValueClass );
 	}
 	
 	public static <V> V getFirstInheritedScalar( SchemaObject obj, Predicate pred, Class<V> scalarValueClass, V defaultValue ) {
 		SchemaObject val = getFirstInheritedValue( obj, pred, (SchemaObject)null );  
 		return val == null ? defaultValue : toScalar( val, scalarValueClass );
 	}
 	
 	public static boolean getFirstInheritedBoolean( SchemaObject obj, Predicate pred ) {
 		return toScalar( getFirstInheritedValue( obj, pred ), Boolean.class ).booleanValue();
 	}
 	
 	public static boolean getFirstInheritedBoolean( SchemaObject obj, Predicate pred, boolean defaultValue ) {
		return getFirstInheritedScalar(obj, pred, Boolean.class, Boolean.FALSE ).booleanValue();
 	}
 	
 	public static SchemaObject getType( SchemaObject obj ) {
 		return getFirstInheritedValue( obj, Core.TYPE, Types.OBJECT );
 	}
 	
 	public static boolean isMemberOfClassWith( SchemaObject obj, Predicate classPred ) {
 		for( SchemaObject clash : getAllInheritedValues( obj, Core.TYPE ) ) {
 			if( isTrue(clash, classPred) ) return true;
 		}
 		return false;
 	}
 }
