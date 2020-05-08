 package com.barchart.missive.core;
 
 import static com.barchart.missive.core.ObjectMapFactory.EMPTY_ENTRY;
 import static com.barchart.missive.core.ObjectMapFactory.build;
 import static com.barchart.missive.core.ObjectMapFactory.indexRegistry;
 import static com.barchart.missive.core.ObjectMapFactory.tagRegistry;
 
 import java.util.Collection;
 
 import com.barchart.missive.api.Castable;
 import com.barchart.missive.api.Initializable;
 import com.barchart.missive.api.Tag;
 import com.barchart.missive.api.TagMap;
 
 /**
  * 
  * @author Gavin M Litchfield
  *
  */
 public abstract class ObjectMap implements TagMap, Castable<ObjectMap>, Initializable {
 
 	volatile int classCode;
 	volatile Object[] values;
 	volatile Class<? extends ObjectMap> childClass;
 	
 	protected ObjectMap() {
 		
 	}
 	
 	/**
 	 * If a missive is masking the values of a subclass, this
 	 * method returns the subclass with all the values of the 
 	 * calling missive.
 	 * 
 	 * @param newClass
 	 * @return
 	 */
 	@Override
 	public <M extends ObjectMap> M cast(final Class<M> newClass) {
 		
 		if(!newClass.isAssignableFrom(childClass)) {
 			throw new MissiveException("Class " + newClass.getName() + 
 					" must be superclass of " + childClass.getName());
 		}
 		
 		final M newMap =  build(newClass);
 		newMap.values = values;
 		
 		return newMap;
 		
 	}
 	
 	@SuppressWarnings("unchecked")
 	@Override
 	public <V> V get(final Tag<V> tag) throws MissiveException {
 		return (V) values[indexRegistry[classCode][tag.index()]];
 	}
 	
 	@Override
 	public boolean contains(final Tag<?> tag) {
 		return indexRegistry[classCode][tag.index()] != EMPTY_ENTRY;
 	}
 
 	@Override
 	public Tag<?>[] tags() {
 		return tagRegistry[classCode];
 	}
 
 	@Override
 	public int size() {
 		return tagRegistry[classCode].length;
 	}
 	
 	@Override
 	public void init() {
 		//Can be overridden by subclasses
 	}
 	
 	/**
 	 * Pass through method for MissiveSafe
 	 */
 	protected <V> void set(final Tag<V> tag, final V value)
 			throws MissiveException {
 		values[indexRegistry[classCode][tag.index()]] = value;
 	}
 	
 	/*
 	 * TODO Review Missive equality 
 	 */
 	@SuppressWarnings({ "rawtypes", "unchecked" })
 	@Override
 	public boolean equals(final Object o) {
 		
 		if(o == null) {
 			return false;
 		}
 		
 		if(this == o) {
 			return true;
 		}
 		
 		if(!(o instanceof TagMap)) {
 			return false;
 		}
 		
 		final TagMap m = (TagMap)o;
 		
 		if(size() != m.size()) {
 			return false;
 		}
 		
 		for(Tag t : tags()) {
 			
 			if(!m.contains(t)) {
 				return false;
 			}
 			
 			if(m.get(t) == null) {
 				if(get(t) != null) {
 					return false;
 				}
 			}
 			
 			if(get(t) == null) {
 				return false;
 			}
 			
 			if(t.isList()) {
 				if(!compareCollections((Collection<Object>)get(t), 
 						(Collection<Object>)m.get(t))) {
 					return false;
 				}
 			}
 			
 			if(!get(t).equals(m.get(t))) {
 				return false;
 			}
 			
 		}
 		
 		return true;
 	}
 	
 	private static boolean compareCollections(final Collection<Object> thisC, 
 			final Collection<Object> thatC) {
 		
 		if(thisC == null || thatC == null) {
 			return false;
 		}
 		
 		if(thisC.size() != thatC.size()) {
 			return false;
 		}
 		
 		for(final Object o : thisC) {
 			
 			if(!thatC.contains(o)) {
 				return false;
 			}
 			
 		}
 		
 		return true;
 	}
 	
 	@Override
 	public String toString() {
 		
 		final StringBuilder sb = new StringBuilder();
 		
 		//TODO
 		
 		return sb.toString();
 		
 	}
 	
 }
