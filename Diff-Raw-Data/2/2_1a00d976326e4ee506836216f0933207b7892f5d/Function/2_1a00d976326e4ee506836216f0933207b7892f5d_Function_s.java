 package com.bluespot.logic.functions;
 
 /**
  * Applies an input to an output.
  * <p>
  * All functions should rely solely on the input provided to determine the
  * appropriate output. Equal inputs should produce equal outputs. Output
  * instances must be unique unless the output is immutable. These conditions
  * imply that a function should be immutable.
  * <p>
  * Functions should strive to be pure, but they may modify the provided input
  * value if doing so is essential to the function's operation. For example, a
  * file read operation must modify the input to retrieve the next value. On the
  * other hand, a sort operation should return a new list.
  * <p>
  * Functions are only provided one argument, so functions that rely on more than
  * one argument should return a function. This is true even for two-argument
  * operations such as addition and multiplication. In other words, functions
  * must be curried. The returned function should take care to ensure it remains
  * immutable; it must copy the provided argument if the argument is not
  * immutable.
  * 
  * @author Aaron Faanes
  * 
  * @param <I>
  *            type of the input value
  * @param <R>
  *            type of the result value
  */
 public interface Function<I, R> {
 	/**
 	 * Applies the specified input to the function, returning some result.
 	 * 
 	 * @param input
 	 *            the provided input. The input may be modified if the
 	 *            modification is essential to the function's operation.
 	 *            <p>
 	 *            The input may be null. For most cases, this means that the
 	 *            result will also be null, but this behavior is not required.
 	 * @return the value. It may be identical to other returned values if and
 	 *         only if the returned value is identical.
 	 *         <p>
 	 *         The function may return {@code null} to indicate the input was
 	 *         unacceptable. This is true if the input is out of range or domain
 	 *         of the function.
 	 * @throws IllegalStateException
 	 *             if accessing the input failed, such as an IO failure. Note
 	 *             that an exception should be thrown only if the result could
	 *             not have been unanticipated from the input alone.
 	 */
 	R apply(I input);
 }
