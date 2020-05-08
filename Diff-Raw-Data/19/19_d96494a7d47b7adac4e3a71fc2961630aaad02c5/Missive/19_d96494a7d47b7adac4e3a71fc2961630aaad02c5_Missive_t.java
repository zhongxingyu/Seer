 package com.barchart.missive.core;
 
 import java.util.HashSet;
 import java.util.Set;
 import java.util.concurrent.ConcurrentHashMap;
 import java.util.concurrent.ConcurrentMap;
 import java.util.concurrent.atomic.AtomicInteger;
 
 import com.barchart.missive.util.ClassUtil;
 
 /**
  * 
  * @author Gavin M Litchfield
  *
  */
 public abstract class Missive implements TagMap {
 	
 	private static final AtomicInteger classCount = new AtomicInteger(0);
 	
 	/* ClassCode, TagCode */
 	protected static volatile int[][] indexRegistry = new int[0][];
 	protected static volatile Tag<?>[][] tagRegistry = new Tag<?>[0][];
 	
 	private final static ConcurrentMap<Class<? extends Missive>, Integer> classMap = 
 			new ConcurrentHashMap<Class<? extends Missive>, Integer>();
 	
	private volatile int classCode;
	private volatile Object[] values;
 	
 	public static <V extends Missive> V build(final Class<V> clazz) {
 		
 		V v = null;
 		try {
 			v = clazz.newInstance();
 		} catch (Exception e1) {
 			throw new MissiveException(e1);
 		} 
 		
 		final int clazzCode = classMap.get(clazz);
 		
 		v.classCode = clazzCode;
 		v.values = new Object[tagRegistry[clazzCode].length];
 		
 		return v;
 	}
 	
 	@SuppressWarnings("unchecked")
 	protected static void install(Tag<?>[] tags) {
 		
 		if(tags == null) {
 			tags = new Tag<?>[0];
 		}
 		
 		/* Get current class object */
 		final Class<?>[] trace = new ClassUtil.ClassTrace().getClassContext();
 		Class<? extends Missive> current = (Class<? extends Missive>) trace[2];
 		Class<?> superClazz = current.getSuperclass();
 		
 		/* Build set of tags of all superclasses */
 		final Set<Tag<?>> tagSet = new HashSet<Tag<?>>();
 		
 		for(final Tag<?> t : tags) {
 			tagSet.add(t);
 		}
 		
 		while(Missive.class.isAssignableFrom(superClazz) && 
 				!superClazz.equals(Missive.class) &&
 				!superClazz.equals(MissiveSafe.class)) {
 			
 			Tag<?>[] classTags = new Tag<?>[0];
 			try {
 				classTags = tagRegistry[classMap.get(superClazz)];
 			} catch (final Exception e) {
 				throw new MissiveException(e);
 			} 
 			
 			for(Tag<?> t : classTags) {
 				tagSet.add(t);
 			}
 			
 			superClazz = superClazz.getSuperclass();
 			
 		}
 		
 		/* Build new tag array and update tag registry */
 		final Tag<?>[] newTags = new Tag<?>[tagSet.size()];
 		int counter = 0;
 		for(Tag<?> t : tagSet) {
 			newTags[counter] = t;
 			counter++;
 		}
 		
 		final Tag<?>[][] newTagRegistry = new Tag<?>[classCount.get() + 1][];
 		System.arraycopy(tagRegistry, 0, newTagRegistry, 0, classCount.get());
 		newTagRegistry[classCount.get()] = newTags;
 		tagRegistry = newTagRegistry;
 		
 		/* Build new index array and update index registry */
 		final int[] newIndexes = new int[Tag.maxIndex()];
 		for(int i  = 0; i < Tag.maxIndex(); i++) {
 			newIndexes[i] = -1;
 		}
 		counter = 0;
 		for(Tag<?> t : tagSet) {
 			newIndexes[t.index()] = counter;
 			counter++;
 		}
 		
 		final int[][] newIndexRegistry = new int[classCount.get() + 1][];
 		System.arraycopy(indexRegistry, 0, newIndexRegistry, 0, classCount.get());
 		newIndexRegistry[classCount.get()] = newIndexes;
 		indexRegistry = newIndexRegistry;
 		
 		/* Assign new class code and update counter */
 		classMap.put(current, classCount.getAndIncrement());
 		
 	}
 	
 	static synchronized void incrementIndexRegistry() {
 		
 		final int oldSize = Tag.maxIndex();
 		
 		for(int i = 0; i < indexRegistry.length; i++) {
 			final int[] newIndexes = new int[oldSize + 1];
 			System.arraycopy(indexRegistry[i], 0, newIndexes, 0, oldSize);
 			indexRegistry[i] = newIndexes;
 			indexRegistry[i][oldSize] = -1;
 		}
 		
 	}
 	
	/*
	 * Pass through method for MissiveSafe
	 */
	<V> void set(Tag<V> tag, V value) throws MissiveException {
		values[indexRegistry[classCode][tag.index()]] = value;
	}
	
 	/* Begin public methods */
 	
 	@SuppressWarnings("unchecked")
 	@Override
 	public <V> V get(final Tag<V> tag) throws MissiveException {
 		return (V) values[indexRegistry[classCode][tag.index()]];
 	}
 
 	@Override
 	public boolean contains(final Tag<?> tag) {
 		return indexRegistry[classCode][tag.index()] != -1;
 	}
 
 	@Override
 	public Tag<?>[] tags() {
 		return tagRegistry[classCode];
 	}
 
 	@Override
 	public int size() {
 		return tagRegistry[classCode].length;
 	}
 
 }
