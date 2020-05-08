 package org.funcish.core;
 
 import org.funcish.core.fn.Function;
 import org.funcish.core.fn.ParaPredicator;
 import org.funcish.core.fn.Predicate;
 import org.funcish.core.fn.Predicator;
 import org.funcish.core.impl.AbstractPredicator;
 import org.funcish.core.impl.ProxyParaPredicator;
 import org.funcish.core.impl.ProxyPredicate;
 import org.funcish.core.impl.ProxyPredicator;
 
 public class Predicates {
 	
 	public static <T> Predicate<T> predicate(Class<T> t, Function<Boolean> target) {
 		return new ProxyPredicate<T>(t, target);
 	}
 	
 	public static <T> Predicator<T> predicator(Predicate<T> target) {
 		return new ProxyPredicator<T>(target);
 	}
 	
 	public static <T> ParaPredicator<T> paraPredicator(Predicate<T> target) {
 		return new ProxyParaPredicator<T>(target);
 	}
 	
 	public static <T, U extends T> Predicator<U> narrow(Class<U> u, Predicate<T> test) {
 		return new NarrowingPredicator<T, U>(u, test);
 	}
 	
 	public static <T> Predicator<T> and(Predicate<T> first, Predicate<T>... rest) {
 		return new AndPredicator<T>(first.t(), rest, first);
 	}
 	
 	public static <T> Predicator<T> or(Predicate<T> first, Predicate<T>... rest) {
 		return new OrPredicator<T>(first.t(), first, rest);
 	}
 	
 	public static <T> Predicator<T> not(Predicate<T> p) {
 		return new NotPredicator<T>(p.t(), p);
 	}
 	
 	public static <T> Predicator<Object> classIsInstance(final Class<T> t) {
 		return new ClassIsInstance<T>(Object.class, t);
 	}
 	
 	private static class ClassIsInstance<T> extends AbstractPredicator<Object> {
 		private final Class<T> t;
 
 		private ClassIsInstance(Class<Object> t, Class<T> t2) {
 			super(t);
 			this.t = t2;
 		}
 
 		@Override
 		public boolean test0(Object value, Integer index) throws Exception {
 			return t.isInstance(value);
 		}
 	}
 
 	private static class NotPredicator<T> extends AbstractPredicator<T> {
 		private Predicate<T> p;
 
 		private NotPredicator(Class<T> t, Predicate<T> p) {
 			super(t);
 			this.p = p;
 		}
 
 		@Override
 		public boolean test0(T value, Integer index) throws Exception {
 			return !p.test(value, index);
 		}
 	}
 
 	private static class AndPredicator<T> extends AbstractPredicator<T> {
 		private Predicate<T>[] rest;
 		private Predicate<T> first;
 
 		private AndPredicator(Class<T> t, Predicate<T>[] rest,
 				Predicate<T> first) {
 			super(t);
 			this.rest = rest;
 			this.first = first;
 		}
 
 		@Override
 		public boolean test0(T value, Integer index) throws Exception {
 			if(!first.test(value, index))
 				return false;
 			for(Predicate<T> p : rest) {
 				if(!p.test(value, index))
 					return false;
 			}
 			return true;
 		}
 	}
 
 	private static class OrPredicator<T> extends AbstractPredicator<T> {
 		private Predicate<T> first;
 		private Predicate<T>[] rest;
 
 		private OrPredicator(Class<T> t, Predicate<T> first, Predicate<T>[] rest) {
 			super(t);
 			this.first = first;
 			this.rest = rest;
 		}
 
 		@Override
 		public boolean test0(T value, Integer index) throws Exception {
 			if(first.test(value, index))
 				return true;
 			for(Predicate<T> p : rest) {
 				if(p.test(value, index))
 					return true;
 			}
 			return false;
 		}
 	}
 
 	private static class NarrowingPredicator<T, U extends T> extends AbstractPredicator<U> {
 		private Predicate<T> test;
 	
 		private NarrowingPredicator(Class<U> t, Predicate<T> test) {
 			super(t);
 			this.test = test;
 		}
 	
 		@Override
 		public boolean test0(U value, Integer index) throws Exception {
 			return test.test(value, index);
 		}
 	}
 
 	private Predicates() {}
 }
