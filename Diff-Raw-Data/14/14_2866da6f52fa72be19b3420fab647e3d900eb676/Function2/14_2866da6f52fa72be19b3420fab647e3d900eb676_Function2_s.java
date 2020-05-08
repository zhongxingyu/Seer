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
 import net.sf.staccatocommons.lang.Lazy;
 
 /**
  * A two-arguments function, that implements {@link Applicable2}.
  * 
  * {@link Function2} can also be <a
  * href="http://en.wikipedia.org/wiki/Partial_application">partially
  * applied</a>, which means, apply it with less arguments than required,
  * returning, instead of the result of the transformation, a new function that
  * expects the rest of the arguments. Thus, {@link Function2} do also implement
  * {@link Applicable}
  * 
  * @author flbulgarelli
  * 
  * @param <A>
  *          function first argument type
  * @param <B>
  *          function second argument type
  * @param <C>
  *          function return type
  * 
  */
 public abstract class Function2<A, B, C> implements Applicable2<A, B, C>,
 	Applicable<A, Function<B, C>> {
 
 	/**
 	 * Applies the function
 	 */
 	public abstract C apply(A arg1, B arg2);
 
 	/**
 	 * Partially applies the function passing just its first parameter
 	 */
 	public Function<B, C> apply(final A arg1) {
 		return new Function<B, C>() {
 			public C apply(B arg2) {
 				return Function2.this.apply(arg1, arg2);
 			}
 		};
 	}
 
 	@NonNull
 	@ForceChecks
 	public <D> Function2<D, B, C> of(@NonNull final Applicable<? super D, ? extends A> other) {
 		return new Function2<D, B, C>() {
 			public C apply(D arg1, B arg2) {
 				return Function2.this.apply(other.apply(arg1), arg2);
 			}
 		};
 	}
 
 	/**
 	 * Composes other function with this.
 	 * 
 	 * @param <D>
 	 * @param other
 	 * @return <code>other.of(this)</code>
 	 */
	public <D> Function2<A, B, D> then(final Function<? super C, ? extends D> other) {
		return (Function2<A, B, D>) other.of(this);
 	}
 
 	/**
 	 * Inverts function parameters order
 	 * 
 	 * @return a new {@link Function2} that produces the same result of this one
 	 *         when applied, but with arguments flipped
 	 */
 	public Function2<B, A, C> flip() {
 		return new Function2<B, A, C>() {
 			public C apply(B arg2, A arg1) {
 				return Function2.this.apply(arg1, arg2);
 			}
 		};
 	}
 
 	/**
 	 * Lazily applies this function, by returning a {@link Lazy} that will send
 	 * {@link #apply(Object, Object)} when {@link Lazy#value()} is evaluated by
 	 * first time.
 	 * 
 	 * @param arg1
 	 * @param arg2
 	 * @return a new {@link Lazy}
 	 */
 	public Lazy<C> lazy(final A arg1, final B arg2) {
 		return new Lazy<C>() {
 			protected C init() {
 				return apply(arg1, arg2);
 			}
 		};
 	}
 
 	public String toString() {
 		return "Function2";
 	}
 
 }
