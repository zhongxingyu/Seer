 /*
  Copyright (c) 2010, The Staccato-Commons Team   
  
  This program is free software; you can redistribute it and/or modify
  it under the terms of the GNU Lesser General Public License as published by
  the Free Software Foundation; version 3 of the License.
 
  This program is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU Lesser General Public License for more details.
  */
 package net.sf.staccatocommons.lang.function;
 
 import net.sf.staccatocommons.check.annotation.ForceChecks;
 import net.sf.staccatocommons.check.annotation.NonNull;
 import net.sf.staccatocommons.defs.Applicable;
 import net.sf.staccatocommons.defs.Applicable2;
 import net.sf.staccatocommons.defs.Applicable3;
 import net.sf.staccatocommons.defs.Thunk;
 import net.sf.staccatocommons.lang.Lazy;
 import net.sf.staccatocommons.lang.provider.Provider;
 
 /**
  * A one argument function.
  * 
  * @author flbulgarelli
  * 
  * @param <A>
  *          function argument type
  * @param <B>
  *          function return type
  */
 public abstract class Function<A, B> implements Applicable<A, B> {
 
 	@Override
 	public abstract B apply(A arg);
 
 	/**
 	 * Composes other function with this.
 	 * 
 	 * @param <C>
 	 * @param other
 	 * @return <code>other.of(this)</code>
 	 */
 	@NonNull
	public <C> Function<A, C> then(@NonNull final Applicable<? super B, ? extends C> other) {
		/* the unnecessary cast is a hack for eclipse compiler */
		return Functions.from(other).of((Applicable) this);
 	}
 
 	@NonNull
 	@ForceChecks
 	public Function<A, B> of(@NonNull final Thunk<? extends A> thunk) {
 		return new Function<A, B>() {
 			public B apply(A arg) {
 				return Function.this.apply(thunk.value());
 			}
 		};
 	}
 
 	/**
 	 * <a href="http://en.wikipedia.org/wiki/Function_composition">Composes</a>
 	 * this function with another {@link Applicable}, resulting in a new
 	 * {@link Function} that when applied returns
 	 * <code>this.apply(other.apply(arg)</code>
 	 * 
 	 * @param <C>
 	 * @param other
 	 * @return a new function, <code>this</code> composed with <code>other</code>
 	 */
 	@NonNull
 	@ForceChecks
 	public <C> Function<C, B> of(@NonNull final Applicable<? super C, ? extends A> other) {
 		return new Function<C, B>() {
 			public B apply(C arg) {
 				return Function.this.apply(other.apply(arg));
 			}
 		};
 	}
 
 	/**
 	 * <a href="http://en.wikipedia.org/wiki/Function_composition">Composes</a>
 	 * this function with another {@link Applicable2}, resulting in a new
 	 * {@link Function2} that when applied returns
 	 * <code>this.apply(other.apply(arg1, arg2)</code>
 	 * 
 	 * @param <Tp1>
 	 * @param <Tp2>
 	 * @param other
 	 *          non null
 	 * @return a new function, this composed with other. Non null.
 	 */
 	@NonNull
 	@ForceChecks
 	public <Tp1, Tp2> Function2<Tp1, Tp2, B> of(
 		@NonNull final Applicable2<Tp1, Tp2, ? extends A> other) {
 		return new Function2<Tp1, Tp2, B>() {
 			public B apply(Tp1 arg1, Tp2 arg2) {
 				return Function.this.apply(other.apply(arg1, arg2));
 			}
 		};
 	}
 
 	/**
 	 * <a href="http://en.wikipedia.org/wiki/Function_composition">Composes</a>
 	 * this function with another {@link Applicable3}, resulting in a new
 	 * {@link Function3} that when applied returns
 	 * <code>this.apply(other.apply(arg1,arg2,arg3)</code>
 	 * 
 	 * @param <Tp1>
 	 * @param <Tp2>
 	 * @param <Tp3>
 	 * @param other
 	 *          non null
 	 * @return a new function, this composed with other. Non null
 	 */
 	@NonNull
 	@ForceChecks
 	public <Tp1, Tp2, Tp3> Function3<Tp1, Tp2, Tp3, B> of(
 		@NonNull final Applicable3<Tp1, Tp2, Tp3, ? extends A> other) {
 		return new Function3<Tp1, Tp2, Tp3, B>() {
 			public B apply(Tp1 arg1, Tp2 arg2, Tp3 arg3) {
 				return Function.this.apply(other.apply(arg1, arg2, arg3));
 			}
 		};
 	}
 
 	/**
 	 * Lazily applies this function, by returning a {@link Lazy} that will send
 	 * {@link #apply(Object)} when {@link Lazy#value()} is evaluated by first
 	 * time.
 	 * 
 	 * @param arg
 	 * @return a new {@link Lazy}
 	 */
 	public Provider<B> lazy(final A arg) {
 		return new Lazy<B>() {
 			protected B init() {
 				return Function.this.apply(arg);
 			}
 		};
 	}
 
 	public String toString() {
 		return "Function";
 	}
 
 }
