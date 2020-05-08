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
 package net.sf.staccatocommons.lang.block;
 
 import net.sf.staccatocommons.defs.Applicable;
 import net.sf.staccatocommons.defs.Applicable2;
 import net.sf.staccatocommons.defs.Executable3;
 import net.sf.staccatocommons.defs.NullSafeAware;
 import net.sf.staccatocommons.lang.SoftException;
 import net.sf.staccatocommons.lang.function.AbstractDelayable3;
 import net.sf.staccatocommons.restrictions.check.NonNull;
 
 /**
  * 
  * @author flbulgarelli
  * 
  * @param <T1>
  * @param <T2>
  * @param <T3>
  */
 public abstract class Block3<T1, T2, T3> extends AbstractDelayable3<T1, T2, T3, Void> implements
	Executable3<T1, T2, T3>, Applicable<T1, Block2<T2, T3>>, Applicable2<T1, T2, Block<T3>>,
	NullSafeAware<Block3<T1, T2, T3>> {
 
 	/**
 	 * Executes this block. This implementation just invokes
 	 * {@link #softExec(Object, Object, Object)}, and softens any exception it may
 	 * throw. Subclasses may override this behavior.
 	 * 
 	 */
 	public void exec(T1 arg1, T2 arg2, T3 arg3) {
 		try {
 			softExec(arg1, arg2, arg3);
 		} catch (Exception e) {
 			throw SoftException.soften(e);
 		}
 	}
 
 	/**
 	 * Executes this block, potentially throwing a checked {@link Exception}
 	 * 
 	 * @see #exec(Object, Object, Object)
 	 * 
 	 * @param arg1
 	 *          operation first argument
 	 * @param arg2
 	 *          operation second argument
 	 * @param arg3
 	 *          operation third argument
 	 * @throws Exception
 	 */
 	protected void softExec(T1 arg1, T2 arg2, T3 arg3) throws Exception {}
 
 	public final Void apply(T1 arg1, T2 arg2, T3 arg3) {
 		exec(arg1, arg2, arg3);
 		return null;
 	};
 
 	@NonNull
 	public Block2<T2, T3> apply(final T1 arg) {
 		return new Block2<T2, T3>() {
 			public void exec(T2 arg1, T3 arg2) {
 				Block3.this.exec(arg, arg1, arg2);
 			}
 		};
 	}
 
 	@NonNull
 	public Block<T3> apply(final T1 arg1, final T2 arg2) {
 		return new Block<T3>() {
 			public void exec(T3 arg) {
 				Block3.this.exec(arg1, arg2, arg);
 			}
 		};
 	}
 
 	public Block3<T1, T2, T3> nullSafe() {
 		return new Block3<T1, T2, T3>() {
 			public void exec(T1 arg1, T2 arg2, T3 arg3) {
 				if (arg1 == null || arg2 == null || arg3 == null) {
 					return;
 				}
 				Block3.this.exec(arg1, arg2, arg3);
 			};
 		};
 	}
 
 	/**
 	 * Chains this block with the given executable, creating a new {@link Block3}
 	 * that executes this one and then the another one.
 	 * 
 	 * @param other
 	 *          the block to execute after this
 	 * @return a new block that first invokes execute on this, and then on the
 	 *         {@link Executable3} provided
 	 */
 	@NonNull
 	public Block3<T1, T2, T3> then(@NonNull final Executable3<T1, T2, T3> other) {
 		return new Block3<T1, T2, T3>() {
 			public void exec(T1 arg1, T2 arg2, T3 arg3) {
 				Block3.this.exec(arg1, arg2, arg3);
 				other.exec(arg1, arg2, arg3);
 			}
 		};
 	}
 
 	public String toString() {
 		return "Block3";
 	}
 
 }
