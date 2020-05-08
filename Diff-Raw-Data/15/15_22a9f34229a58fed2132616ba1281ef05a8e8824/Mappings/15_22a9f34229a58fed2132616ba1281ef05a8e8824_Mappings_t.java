 package org.funcish.core;
 
 import org.funcish.core.fn.Function;
 import org.funcish.core.fn.Mappicator;
 import org.funcish.core.fn.Mapping;
 import org.funcish.core.fn.ParaMappicator;
 import org.funcish.core.impl.AbstractMappicator;
 import org.funcish.core.impl.ProxyMappicator;
 import org.funcish.core.impl.ProxyMapping;
 import org.funcish.core.impl.ProxyParaMappicator;
 
 public class Mappings {
 	
 	public static <K, V> Mapping<K, V> mapping(Class<K> k, Function<V> target) {
 		return new ProxyMapping<K, V>(k, target);
 	}
 	
 	public static <K, V> Mappicator<K, V> mappicator(Mapping<K, V> target) {
 		return new ProxyMappicator<K, V>(target);
 	}
 	
 	public static <K, V> ParaMappicator<K, V> paraMappicator(Mapping<K, V> target) {
 		return new ProxyParaMappicator<K, V>(target);
 	}
 	
 	public static <K, L extends K, V> Mappicator<L, V> narrow(Class<L> l, final Mapping<K, V> mapping) {
 		return new NarrowingMappicator<K, L, V>(l, mapping.v(), mapping);
 	}
 	
 	public static <K, U, V extends U> Mappicator<K, U> widen(Class<U> u, final Mapping<K, V> mapping) {
 		return new WideningMappicator<K, U, V>(mapping.k(), u, mapping);
 	}
 	
 	@SuppressWarnings({ "unchecked", "rawtypes" })
 	public static Mappicator<Class<?>, String> classSimpleName() {
 		return new ClassSimpleName((Class) Class.class, String.class);
 	}
 
 	public static <K, V> Mappicator<K, V> classCast(Class<K> k, Class<V> v) {
 		return new ClassCast<K, V>(k, v, v);
 	}
 	
	@SuppressWarnings({ "unchecked", "rawtypes" })
 	public static <T> Mappicator<Class<? extends T>, T> classNewInstance(Class<T> t) {
 		return new ClassNewInstance<T>((Class) Class.class, t);
 	}
 	
 	private static class ClassNewInstance<T> extends
 			AbstractMappicator<Class<? extends T>, T> {
 		private ClassNewInstance(Class<Class<? extends T>> k, Class<T> v) {
 			super(k, v);
 		}
 
 		@Override
 		public T map0(Class<? extends T> key, Integer index) throws Exception {
 			return key.newInstance();
 		}
 	}
 
 	private static class ClassCast<K, V> extends AbstractMappicator<K, V> {
 		private final Class<V> v;
 
 		private ClassCast(Class<K> k, Class<V> v, Class<V> v2) {
 			super(k, v);
 			this.v = v2;
 		}
 
 		@Override
 		public V map0(K key, Integer index) throws Exception {
 			return v.cast(key);
 		}
 	}
 
 	private static class WideningMappicator<K, U, V extends U> extends AbstractMappicator<K, U> {
 		private final Mapping<K, V> mapping;
 
 		private WideningMappicator(Class<K> k, Class<U> v, Mapping<K, V> mapping) {
 			super(k, v);
 			this.mapping = mapping;
 		}
 
 		@Override
 		public U map0(K key, Integer index) throws Exception {
 			return mapping.map(key, index);
 		}
 	}
 
 	private static class NarrowingMappicator<K, L extends K, V> extends AbstractMappicator<L, V> {
 		private final Mapping<K, V> mapping;
 
 		private NarrowingMappicator(Class<L> k, Class<V> v, Mapping<K, V> mapping) {
 			super(k, v);
 			this.mapping = mapping;
 		}
 
 		@Override
 		public V map0(L key, Integer index) throws Exception {
 			return mapping.map(key, index);
 		}
 	}
 
 	private static class ClassSimpleName extends AbstractMappicator<Class<?>, String> {
 		private ClassSimpleName(Class<Class<?>> k, Class<String> v) {
 			super(k, v);
 		}
 	
 		@Override
 		public String map0(Class<?> key, Integer index) throws Exception {
 			String ret = key.getSimpleName();
 			if(ret.isEmpty())
 				ret = key.getName();
 			return ret;
 		}
 	}
 
 	private Mappings() {}
 }
