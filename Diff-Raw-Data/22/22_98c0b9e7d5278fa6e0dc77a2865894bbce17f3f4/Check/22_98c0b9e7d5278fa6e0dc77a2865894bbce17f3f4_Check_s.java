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
 package net.sf.staccatocommons.check;
 
 import java.math.BigDecimal;
 import java.math.BigInteger;
 import java.util.Collection;
 import java.util.Map;
 import java.util.regex.Pattern;
 
 import net.sf.staccatocommons.check.format.Var;
 import net.sf.staccatocommons.check.internal.EmptyAwareTypes;
 import net.sf.staccatocommons.check.internal.SizeAwareTypes;
 import net.sf.staccatocommons.defs.ContainsAware;
 import net.sf.staccatocommons.defs.EmptyAware;
 import net.sf.staccatocommons.defs.SizeAware;
 import net.sf.staccatocommons.defs.type.EmptyAwareType;
 import net.sf.staccatocommons.defs.type.NumberType;
 import net.sf.staccatocommons.defs.type.SizeAwareType;
 
 /**
  * {@link Check}s are objects that validate conditions. It is heavily inspired
  * on several validation utilities like {@link org.apache.commons.lang.Validate}
  * from Apache commons, but is designed to be more flexible and easy-to-use, so,
  * it should be seen as higher lever replacement.
  * 
  * The four main differences are the following
  * <ul>
  * <li>
  * This class is abstract and offers instance methods, and the exact behavior
  * when checks fail depends on subclasses. Three concrete implementation of
  * {@link Check}, are {@link Ensure}, {@link Assert} and {@link Validate}</li>
  * <li> {@link Check} offers a rich interface. Not only method for checking nulls
  * or boolean values, but also, {@link Collection}s sizes, regular expresion and
  * so on are exposed</li>
  * <li>Simple messages are automatically generated, so most user code do not
  * need to provide strings that tell what it is checking</li>
  * <li>Exception type thrown by implementors is generic, so they can throw
  * either checked or unchecked exceptions at no cost for client code</li>
  * </ul>
  * 
  * All check methods take a paremeterName as first argument. This is a String
  * identifier of the var being check, that allows to find it in context. For
  * example, if what is being check is a method var or local var, its var name
  * should be used, if is its an an attribute or property is being used, its
  * property name should be used.
  * 
  * 
  * @author flbulgarelli
  * 
  * @see Validate
  * @see Ensure
  * @see Assert
  * 
  * @param <ExceptionType>
  *          The kind of exception that this check will thrown on validation
  *          failures
  * 
  */
 public abstract class Check<ExceptionType extends Throwable> {
 
 	/* Minimal Ops */
 
 	protected ExceptionType createVarException(VarFailure failure) {
 		return createException(failure);
 	}
 
 	protected abstract ExceptionType createException(Failure failure);
 
 	/**
 	 * Fails, throwing an exception with a message. This method never returns
 	 * normally.
 	 * 
 	 * @param <Nothing>
 	 * @param message
 	 *          the error message
 	 * @param args
 	 *          the error message args
 	 * @return this method does never return normally
 	 * @throws ExceptionType
 	 *           always
 	 */
 	public <Nothing> Nothing fail(String message, Object... args) throws ExceptionType {
 		throw createException(new Failure(String.format(message, args)));
 	}
 
 	/**
 	 * Fails, throwing an exception with a message, the var and its name. This
 	 * method never returns normally.
 	 * 
 	 * @param <Nothing>
 	 * @param varName
 	 *          the name of the variable whose check failed
 	 * @param var
 	 *          the variable whose check failed
 	 * @param message
 	 *          the error message
 	 * @param args
 	 *          the error message arguments
 	 * @return this method does never return normally
 	 * @throws ExceptionType
 	 *           always
 	 */
 	public <Nothing> Nothing fail(String varName, Object var, String message, Object... args)
 		throws ExceptionType {
 		throw createVarException(new VarFailure(varName, var, String.format(message, args)));
 	}
 
 	/**
 	 * Checks a condition, failing if not met.
 	 * 
 	 * @param varName
 	 *          the name of the variable to be checked
 	 * @param var
 	 *          the variable to be checked
 	 * @param condition
 	 *          the condition to be checked
 	 * @param message
 	 *          the error message
 	 * @param args
 	 *          the error message arguments
 	 * @return <code>this</code>, in order to allow method chaining
 	 * @throws ExceptionType
 	 *           if the check failed
 	 */
 	public final Check<ExceptionType> that(String varName, Object var, boolean condition,
 		String message, Object... args) throws ExceptionType {
 		if (!condition)
 			fail(varName, var, message, args);
 		return this;
 	}
 
 	/**
 	 * Checks a that a condition is true, failing if it is not.
 	 * 
 	 * @param condition
 	 *          the condition to be checked
 	 * @param message
 	 *          the error message
 	 * @param args
 	 *          the error message arguments
 	 * @return <code>this</code>, in order to allow method chaining
 	 * @throws ExceptionType
 	 *           if the check failed
 	 */
 	public final Check<ExceptionType> that(boolean condition, String message, Object... args)
 		throws ExceptionType {
 		if (!condition)
 			fail(message, args);
 		return this;
 	}
 
 	/**
 	 * Checks the variable is not null.
 	 * 
 	 * @param varName
 	 *          the name of the variable to be checked
 	 * @param var
 	 *          the variable to be checked
 	 * @return <code>this</code>, in order to allow method chaining
 	 * @throws ExceptionType
 	 *           if the check failed
 	 */
 	public final Check<ExceptionType> isNotNull(String varName, Object var) throws ExceptionType {
 		return that(varName, var, var != null, "must not be null");
 	}
 
 	/**
 	 * Checks the variable is null.
 	 * 
 	 * @param varName
 	 *          the name of the variable to be checked
 	 * @param var
 	 *          the variable to be checked
 	 * @return <code>this</code>, in order to allow method chaining
 	 * @throws ExceptionType
 	 *           if the check failed
 	 */
 	public final Check<ExceptionType> isNull(String varName, Object var) throws ExceptionType {
 		return that(varName, var, var == null, "must be null");
 	}
 
 	/* Extra ops */
 
 	/**
 	 * Checks a that the variable is true, failing with a generated message if it
 	 * is not.
 	 * 
 	 * @param varName
 	 *          the name of the variable to be checked
 	 * @param var
 	 *          the var to be checked
 	 * @return <code>this</code>, in order to allow method chaining
 	 * @throws ExceptionType
 	 *           if the check failed
 	 */
 	public final Check<ExceptionType> isTrue(String varName, boolean var) throws ExceptionType {
 		return that(varName, var, var, "must be true");
 	}
 
 	/**
 	 * Checks that the variable is not null and matches a given regular
 	 * expression.
 	 * 
 	 * @param varName
 	 *          the name of the variable to be checked
 	 * @param var
 	 *          the var to be checked
 	 * @param regex
 	 *          the regular expression the var must match
 	 * @return <code>this</code>, in order to allow method chaining
 	 * @throws ExceptionType
 	 *           if the check failed
 	 * 
 	 * @see #that(boolean, String, Object...)
 	 */
 	public final Check<ExceptionType> matches(String varName, String var, String regex)
 		throws ExceptionType {
 		return isNotNull(varName, var).matches(varName, var, Pattern.compile(regex));
 	}
 
 	/**
 	 * Checks that the variable is not null and matches a given pattern
 	 * 
 	 * @param varName
 	 *          the name of the variable to be checked
 	 * @param var
 	 *          the variable to be checked
 	 * @param pattern
 	 *          the pattern the variable must match
 	 * @return <code>this</code>, in order to allow method chaining
 	 * @throws ExceptionType
 	 *           if the check failed
 	 * 
 	 * @see #that(boolean, String, Object...)
 	 */
 	public final Check<ExceptionType> matches(String varName, String var, Pattern pattern)
 		throws ExceptionType {
 		return isNotNull(varName, var) //
 			.that(varName, var, pattern.matcher(var).matches(), "must match %s", pattern.pattern());
 	}
 
 	/**
 	 * Checks that the variable is not null and empty
 	 * 
 	 * @param varName
 	 *          the name of the variable to be checked
 	 * @param var
 	 *          the variable to be checked
 	 * @return <code>this</code>, in order to allow method chaining
 	 * @throws ExceptionType
 	 *           if the check failed
 	 * 
 	 * @see #that(boolean, String, Object...)
 	 */
 	public final Check<ExceptionType> isEmpty(String varName, Collection<?> var) throws ExceptionType {
 		return isEmpty(varName, var, SizeAwareTypes.COLLECTION);
 	}
 
 	/**
 	 * Checks that the variable is not null and empty
 	 * 
 	 * @param varName
 	 *          the name of the variable to be checked
 	 * @param var
 	 *          the variable to be checked
 	 * @return <code>this</code>, in order to allow method chaining
 	 * @throws ExceptionType
 	 *           if the check failed
 	 * 
 	 * @see #that(boolean, String, Object...)
 	 */
 	public final Check<ExceptionType> isEmpty(String varName, Map<?, ?> var) throws ExceptionType {
 		return isEmpty(varName, var, SizeAwareTypes.MAP);
 	}
 
 	/**
 	 * Checks that the variable is not null and empty
 	 * 
 	 * @param varName
 	 *          the name of the variable to be checked
 	 * @param var
 	 *          the variable to be checked
 	 * @return <code>this</code>, in order to allow method chaining
 	 * @throws ExceptionType
 	 *           if the check failed
 	 * 
 	 * @see #that(boolean, String, Object...)
 	 */
 	public final Check<ExceptionType> isEmpty(String varName, Iterable<?> var) throws ExceptionType {
 		return isEmpty(varName, var, EmptyAwareTypes.ITERABLE);
 	}
 
 	/**
 	 * Checks that the variable is not null and empty
 	 * 
 	 * @param varName
 	 *          the name of the variable to be checked
 	 * @param var
 	 *          the variable to be checked
 	 * @return <code>this</code>, in order to allow method chaining
 	 * @throws ExceptionType
 	 *           if the check failed
 	 * 
 	 * @see #that(boolean, String, Object...)
 	 */
 	public final Check<ExceptionType> isEmpty(String varName, CharSequence var) throws ExceptionType {
 		return isEmpty(varName, var, SizeAwareTypes.CHAR_SEQUENCE);
 	}
 
 	/**
 	 * Checks that the variable is not null and empty
 	 * 
 	 * @param varName
 	 *          the name of the variable to be checked
 	 * @param var
 	 *          the variable to be checked
 	 * @return <code>this</code>, in order to allow method chaining
 	 * @throws ExceptionType
 	 *           if the check failed
 	 * 
 	 * @see #that(boolean, String, Object...)
 	 */
 	public final Check<ExceptionType> isEmpty(String varName, EmptyAware var) throws ExceptionType {
 		return isEmpty(varName, var, EmptyAwareTypes.EMPTY_AWARE);
 	}
 
 	/**
 	 * Checks that the variable is not null and empty
 	 * 
 	 * @param <A>
 	 * @param varName
 	 *          the name of the variable to be checked
 	 * @param var
 	 *          the variable to be checked
 	 * @param type
 	 *          the {@link EmptyAwareType} used to determine if <code>var</code>
 	 *          is not empty
 	 * @return <code>this</code>, in order to allow method chaining
 	 * @throws ExceptionType
 	 *           if the check failed
 	 * @see #that(boolean, String, Object...)
 	 */
 	public final <A> Check<ExceptionType> isEmpty(String varName, A var, EmptyAwareType<A> type)
 		throws ExceptionType {
 		return isNotNull(varName, var).that(varName, var, type.isEmpty(var), "must be empty");
 	}
 
 	/**
 	 * Checks that the variable is not null and instance of the given class
 	 * 
 	 * @param varName
 	 *          the name of the variable to be checked
 	 * @param var
 	 *          the variable to be checked
 	 * @param expectedClass
 	 *          the class the variable must be instance of
 	 * @return <code>this</code>, in order to allow method chaining
 	 * @throws ExceptionType
 	 *           if the check failed
 	 * 
 	 * @see #that(boolean, String, Object...)
 	 */
 	public final Check<ExceptionType> isInstanceOf(String varName, Object var, Class<?> expectedClass)
 		throws ExceptionType {
 		return isNotNull(varName, var)//
 			.that(varName, var, expectedClass.isInstance(var), //
 				"must be instance of class %s",
 				expectedClass);
 	}
 
 	/**
 	 * Checks that <code>var</code> is not null and its size is the given
 	 * <code>expectedSize</code>
 	 * 
 	 * @param varName
 	 *          the name of the collection variable to be checked
 	 * @param var
 	 *          the {@link Collection} variable to be checked
 	 * @param size
 	 *          the size the variable must have
 	 * @return <code>this</code>, in order to allow method chaining
 	 * @throws ExceptionType
 	 *           if the check failed
 	 */
 	public final Check<ExceptionType> isSize(String varName, Collection<?> var, int size)
 		throws ExceptionType {
 		return isSize(varName, var, size, SizeAwareTypes.COLLECTION);
 	}
 
 	/**
 	 * Checks that <code>var</code> is not null and its size is the given
 	 * <code>expectedSize</code>
 	 * 
 	 * @param varName
 	 *          the name of the char sequence variable to be checked
 	 * @param var
 	 *          the {@link CharSequence} variable to be checked
 	 * @param size
 	 *          the size the variable must have
 	 * @return <code>this</code>, in order to allow method chaining
 	 * @throws ExceptionType
 	 *           if the check failed
 	 */
 	public final Check<ExceptionType> isSize(String varName, CharSequence var, int size)
 		throws ExceptionType {
 		return isSize(varName, var, size, SizeAwareTypes.CHAR_SEQUENCE);
 	}
 
 	/**
 	 * Checks that <code>var</code> is not null and its size is the given
 	 * <code>expectedSize</code>
 	 * 
 	 * @param varName
 	 *          the name of the size-aware variable to be checked
 	 * @param var
 	 *          the {@link SizeAware} variable to be checked
 	 * @param size
 	 *          the size the variable must have
 	 * @return <code>this</code>, in order to allow method chaining
 	 * @throws ExceptionType
 	 *           if the check failed
 	 */
 	public final Check<ExceptionType> isSize(String varName, SizeAware var, int size)
 		throws ExceptionType {
 		return isSize(varName, var, size, SizeAwareTypes.SIZE_AWARE);
 	}
 
 	/**
 	 * Checks that <code>var</code> is not null and its size is the given
 	 * <code>expectedSize</code>
 	 * 
 	 * @param varName
 	 *          the name of the {@link Map} variable to be checked
 	 * @param var
 	 *          the {@link Map} variable to be checked
 	 * @param size
 	 *          the size the variable must have
 	 * @return <code>this</code>, in order to allow method chaining
 	 * @throws ExceptionType
 	 *           if the check failed
 	 */
 	public final Check<ExceptionType> isSize(String varName, Map<?, ?> var, int size)
 		throws ExceptionType {
 		return isSize(varName, var, size, SizeAwareTypes.MAP);
 	}
 
 	/**
 	 * Checks that <code>var</code> is not null and its ize is the given
 	 * <code>expectedSize</code>
 	 * 
 	 * @param <A>
 	 * @param varName
 	 *          the name of the variable to be checked
 	 * @param var
 	 *          the variable to be checked
 	 * @param expectedSize
 	 * @param type
 	 *          the {@link SizeAwareType} used to determine if <code>var</code>
 	 *          has the given <code>expectedSize</code>
 	 * @return <code>this</code>, in order to allow method chaining
 	 * @throws ExceptionType
 	 *           if the check failed
 	 * @see #that(boolean, String, Object...)
 	 */
 	public final <A> Check<ExceptionType> isSize(String varName, A var, int expectedSize,
 		SizeAwareType<A> type) throws ExceptionType {
 		return that(varName, var, type.size(var) == expectedSize, //
 			"must be of size %s, but was %s",
 			expectedSize,
 			type.size(var));
 	}
 
 	/**
 	 * Checks that <code>var</code> is not null and its size greater than or
 	 * equals to the the given <code>minSize</code>
 	 * 
 	 * @param <A>
 	 * @param varName
 	 *          the name of the variable to be checked
 	 * @param var
 	 *          the variable to be checked
 	 * @param minSize
 	 *          the minimum size
 	 * @param type
 	 *          the {@link SizeAwareType} used to determine the <code>var</code>
 	 *          size
 	 * @return <code>this</code>, in order to allow method chaining
 	 * @throws ExceptionType
 	 *           if the check failed
 	 * @see #that(boolean, String, Object...)
 	 */
 	public final <A> Check<ExceptionType> isMinSize(String varName, A var, int minSize,
 		SizeAwareType<A> type) throws ExceptionType {
 		return isNotNull(varName, var) //
 			.that(varName, var, type.size(var) >= minSize, //
 				"must be at least of size %s, but was %s",
 				minSize,
 				type.size(var));
 	}
 
 	/**
 	 * Checks that <code>var</code> is not null and its size less than or equals
 	 * to the the given <code>maxSize</code>
 	 * 
 	 * @param <A>
 	 * @param varName
 	 *          the name of the variable to be checked
 	 * @param var
 	 *          the variable to be checked
	 * @param minSize
	 *          the minimum size
 	 * @param type
 	 *          the {@link SizeAwareType} used to determine the <code>var</code>
 	 *          size
 	 * @return <code>this</code>, in order to allow method chaining
 	 * @throws ExceptionType
 	 *           if the check failed
 	 * @see #that(boolean, String, Object...)
 	 */
 	public final <A> Check<ExceptionType> isMaxSize(String varName, A var, int maxSize,
 		SizeAwareType<A> type) throws ExceptionType {
 		return isNotNull(varName, var) //
 			.that(varName, var, type.size(var) <= maxSize, //
 				"must be at most of size %s, but was %s",
 				maxSize,
 				type.size(var));
 	}
 
 	/**
 	 * Checks the variable is &gt;= 0
 	 * 
 	 * @param varName
 	 *          the name of the variable to be checked
 	 * @param var
 	 *          the variable to be checked
 	 * @return <code>this</code>, in order to allow method chaining
 	 * @throws ExceptionType
 	 *           if the check failed
 	 */
 	public final Check<ExceptionType> isNotNegative(String varName, long var) throws ExceptionType {
 		return isNotNegative(varName, var, var >= 0);
 	}
 
 	/**
 	 * Checks the variable is &gt;= 0
 	 * 
 	 * @param varName
 	 *          the name of the variable to be checked
 	 * @param var
 	 *          the variable to be checked
 	 * @return <code>this</code>, in order to allow method chaining
 	 * @throws ExceptionType
 	 *           if the check failed
 	 */
 	public final Check<ExceptionType> isNotNegative(String varName, int var) throws ExceptionType {
 		return isNotNegative(varName, var, var >= 0);
 	}
 
 	/**
 	 * Checks the variable is &gt;= 0
 	 * 
 	 * @param varName
 	 *          the name of the variable to be checked
 	 * @param var
 	 *          the variable to be checked
 	 * @return <code>this</code>, in order to allow method chaining
 	 * @throws ExceptionType
 	 *           if the check failed
 	 */
 	public final Check<ExceptionType> isNotNegative(String varName, double var) throws ExceptionType {
 		return isNotNegative(varName, var, var >= 0);
 	}
 
 	/**
 	 * Checks the variable is &gt;= 0
 	 * 
 	 * @param varName
 	 *          the name of the variable to be checked
 	 * @param var
 	 *          the variable to be checked
 	 * @return <code>this</code>, in order to allow method chaining
 	 * @throws ExceptionType
 	 *           if the check failed
 	 */
 	public final Check<ExceptionType> isNotNegative(String varName, float var) throws ExceptionType {
 		return isNotNegative(varName, var, var >= 0);
 	}
 
 	/**
 	 * Checks the variable is &gt;= 0
 	 * 
 	 * @param varName
 	 *          the name of the variable to be checked
 	 * @param var
 	 *          the variable to be checked
 	 * @return <code>this</code>, in order to allow method chaining
 	 * @throws ExceptionType
 	 *           if the check failed
 	 */
 	public final Check<ExceptionType> isNotNegative(String varName, BigDecimal var)
 		throws ExceptionType {
 		return isNotNull(varName, var) //
 			.isNotNegative(varName, var, var.compareTo(BigDecimal.ZERO) >= 0);
 	}
 
 	/**
 	 * Checks the variable is &gt;= 0
 	 * 
 	 * @param varName
 	 *          the name of the variable to be checked
 	 * @param var
 	 *          the variable to be checked
 	 * @return <code>this</code>, in order to allow method chaining
 	 * @throws ExceptionType
 	 *           if the check failed
 	 */
 	public final Check<ExceptionType> isNotNegative(String varName, BigInteger var)
 		throws ExceptionType {
 		return isNotNull(varName, var) //
 			.isNotNegative(varName, var, var.compareTo(BigInteger.ZERO) >= 0);
 	}
 
 	/**
 	 * Checks the <code>var</code> is &gt;= 0
 	 * 
 	 * @param <A>
 	 * @param varName
 	 *          the name of the variable to be checked
 	 * @param var
 	 *          the variable to be checked
 	 * @param type
 	 *          the {@link NumberType} used to determine if <code>var</code> is
 	 *          negative
 	 * @return <code>this</code>, in order to allow method chaining
 	 * @throws ExceptionType
 	 *           if the check failed
 	 */
 	public final <A> Check<ExceptionType> isNotNegative(String varName, A var, NumberType<A> type)
 		throws ExceptionType {
 		return isNotNegative(varName, var, !type.isNegative(var));
 	}
 
 	private Check<ExceptionType> isNotNegative(String varName, Object number, boolean notNegative)
 		throws ExceptionType {
 		return that(varName, number, notNegative, "must be not negative");
 	}
 
 	/**
 	 * Checks the <code>var</code> is not null and not equal to 0
 	 * 
 	 * @param <A>
 	 * @param varName
 	 *          the name of the variable to be checked
 	 * @param var
 	 *          the variable to be checked
 	 * @param type
 	 *          the {@link NumberType} used to determine if <code>var</code> is
 	 *          not zero, based on {@link NumberType#isZero(Object)}
 	 * @return <code>this</code>, in order to allow method chaining
 	 * @throws ExceptionType
 	 *           if the check failed
 	 */
 	public final <A> Check<ExceptionType> isNotZero(String varName, A var, NumberType<A> type)
 		throws ExceptionType {
 		return isNotNull(varName, var).that(varName, var, !type.isZero(var), "must not be zero");
 	}
 
 	/**
 	 * Checks the <code>var</code> is not null and equal to 0
 	 * 
 	 * @param <A>
 	 * @param varName
 	 *          the name of the variable to be checked
 	 * @param var
 	 *          the variable to be checked
 	 * @param type
 	 *          the {@link NumberType} used to determine if <code>var</code> is
 	 *          zero, based on {@link NumberType#isZero(Object)}
 	 * @return <code>this</code>, in order to allow method chaining
 	 * @throws ExceptionType
 	 *           if the check failed
 	 */
 	public final <A> Check<ExceptionType> isZero(String varName, A var, NumberType<A> type)
 		throws ExceptionType {
 		return isNotNull(varName, var).that(varName, var, type.isZero(var), "must be zero");
 	}
 
 	/**
 	 * Checks that the {@link EmptyAware} var is not null and not empty
 	 * 
 	 * @param varName
 	 *          the name of the variable to be checked
 	 * @param var
 	 *          the variable to be checked
 	 * @return <code>this</code>, in order to allow method chaining
 	 * @throws ExceptionType
 	 *           if the check failed
 	 */
 	public final Check<ExceptionType> isNotEmpty(String varName, EmptyAware var) throws ExceptionType {
 		return isNotEmpty(varName, var, EmptyAwareTypes.EMPTY_AWARE);
 	}
 
 	/**
 	 * Checks that the {@link Collection} variable is not null and not empty
 	 * 
 	 * @param varName
 	 *          the name of the variable to be checked
 	 * @param var
 	 *          the variable to be checked
 	 * @return <code>this</code>, in order to allow method chaining
 	 * @throws ExceptionType
 	 *           if the check failed
 	 */
 	public final Check<ExceptionType> isNotEmpty(String varName, Collection<?> var)
 		throws ExceptionType {
 		return isNotEmpty(varName, var, SizeAwareTypes.COLLECTION);
 	}
 
 	/**
 	 * Checks that the {@link CharSequence} variable is not null and not empty
 	 * 
 	 * @param varName
 	 *          the name of the variable to be checked
 	 * @param var
 	 *          the variable to be checked
 	 * @return <code>this</code>, in order to allow method chaining
 	 * @throws ExceptionType
 	 *           if the check failed
 	 */
 	public final Check<ExceptionType> isNotEmpty(String varName, CharSequence var)
 		throws ExceptionType {
 		return isNotEmpty(varName, var, SizeAwareTypes.CHAR_SEQUENCE);
 	}
 
 	/**
 	 * Checks that the {@link Map} variable is not null and not empty
 	 * 
 	 * @param varName
 	 *          the name of the variable to be checked
 	 * @param var
 	 *          the variable to be checked
 	 * @return <code>this</code>, in order to allow method chaining
 	 * @throws ExceptionType
 	 *           if the check failed
 	 */
 	public final Check<ExceptionType> isNotEmpty(String varName, Map<?, ?> var) throws ExceptionType {
 		return isNotEmpty(varName, var, SizeAwareTypes.MAP);
 	}
 
 	/**
 	 * Checks that the {@link Iterable} variable is not null and not empty
 	 * 
 	 * @param varName
 	 *          the name of the variable to be checked
 	 * @param var
 	 *          the variable to be checked
 	 * @return <code>this</code>, in order to allow method chaining
 	 * @throws ExceptionType
 	 *           if the check failed
 	 */
 	public final Check<ExceptionType> isNotEmpty(String varName, Iterable<?> var)
 		throws ExceptionType {
 		return isNotEmpty(varName, var, EmptyAwareTypes.ITERABLE);
 	}
 
 	/**
 	 * Checks that the variable is not null and not empty
 	 * 
 	 * @param <A>
 	 * @param varName
 	 *          the name of the variable to be checked
 	 * @param var
 	 *          the variable to be checked
 	 * @param type
 	 *          the {@link EmptyAwareType} used to determine if <code>var</code>
 	 *          is empty
 	 * @return <code>this</code>, in order to allow method chaining
 	 * @throws ExceptionType
 	 *           if the check failed
 	 * @see #that(boolean, String, Object...)
 	 */
 	public final <A> Check<ExceptionType> isNotEmpty(String varName, A var, EmptyAwareType<A> type)
 		throws ExceptionType {
 		return isNotNull(varName, var).that(varName, var, !type.isEmpty(var), "must not be empty");
 	}
 
 	/**
 	 * Checks the var is > 0
 	 * 
 	 * @param varName
 	 *          the name of the variable to be checked
 	 * @param var
 	 *          the variable to be checked
 	 * @return <code>this</code>, in order to allow method chaining
 	 * @throws ExceptionType
 	 *           if the check failed
 	 */
 	public final Check<ExceptionType> isPositive(String varName, long var) throws ExceptionType {
 		return isPositive(varName, var, var > 0);
 	}
 
 	/**
 	 * Checks the var is > 0
 	 * 
 	 * @param varName
 	 *          the name of the variable to be checked
 	 * @param var
 	 *          the variable to be checked
 	 * @return <code>this</code>, in order to allow method chaining
 	 * @throws ExceptionType
 	 *           if the check failed
 	 */
 	public final Check<ExceptionType> isPositive(String varName, int var) throws ExceptionType {
 		return isPositive(varName, var, var > 0);
 	}
 
 	/**
 	 * Checks the var is > 0
 	 * 
 	 * @param varName
 	 *          the name of the variable to be checked
 	 * @param var
 	 *          the variable to be checked
 	 * @return <code>this</code>, in order to allow method chaining
 	 * @throws ExceptionType
 	 *           if the check failed
 	 */
 	public final Check<ExceptionType> isPositive(String varName, double var) throws ExceptionType {
 		return isPositive(varName, var, var > 0);
 	}
 
 	/**
 	 * Checks the var is > 0
 	 * 
 	 * @param varName
 	 *          the name of the variable to be checked
 	 * @param var
 	 *          the variable to be checked
 	 * @return <code>this</code>, in order to allow method chaining
 	 * @throws ExceptionType
 	 *           if the check failed
 	 */
 	public final Check<ExceptionType> isPositive(String varName, float var) throws ExceptionType {
 		return isPositive(varName, var, var > 0);
 	}
 
 	/**
 	 * Checks the var is > 0
 	 * 
 	 * @param varName
 	 *          the name of the variable to be checked
 	 * @param var
 	 *          the variable to be checked
 	 * @return <code>this</code>, in order to allow method chaining
 	 * @throws ExceptionType
 	 *           if the check failed
 	 */
 	public final Check<ExceptionType> isPositive(String varName, BigDecimal var) throws ExceptionType {
 		return isNotNull(varName, var).isPositive(varName, var, var.compareTo(BigDecimal.ZERO) > 0);
 	}
 
 	/**
 	 * Checks the var is not null and > 0
 	 * 
 	 * @param varName
 	 *          the name of the variable to be checked
 	 * @param var
 	 *          the variable to be checked
 	 * @return <code>this</code>, in order to allow method chaining
 	 * @throws ExceptionType
 	 *           if the check failed
 	 */
 	public final Check<ExceptionType> isPositive(String varName, BigInteger var) throws ExceptionType {
 		return isNotNull(varName, var)//
 			.isPositive(varName, var, var.compareTo(BigInteger.ZERO) > 0);
 	}
 
 	/**
 	 * Checks the <code>var</code> is &gt; 0
 	 * 
 	 * @param <A>
 	 * @param varName
 	 *          the name of the variable to be checked
 	 * @param var
 	 *          the variable to be checked
 	 * @param type
 	 *          the {@link NumberType} used to determine if <code>var</code> is
 	 *          positive
 	 * @return <code>this</code>, in order to allow method chaining
 	 * @throws ExceptionType
 	 *           if the check failed
 	 */
 	public final <A> Check<ExceptionType> isPositive(String varName, A var, NumberType<A> type)
 		throws ExceptionType {
 		return isNotNull(varName, var).isPositive(varName, var, type.isPositive(var));
 	}
 
 	private Check<ExceptionType> isPositive(String varName, Object var, boolean positive)
 		throws ExceptionType {
 		return that(varName, var, positive, "must be positive");
 	}
 
 	/**
 	 * Checks the <code>var</code> is not null, less than or equal to
 	 * <code>max</code> and greater than or equal to <code>min</code>
 	 * 
 	 * @param varName
 	 *          the name of the variable to be checked
 	 * @param var
 	 *          the variable to be checked
 	 * @param min
 	 * @param max
 	 * @return <code>this</code>, in order to allow method chaining
 	 * @throws ExceptionType
 	 *           if the check failed
 	 */
 	public final <T extends Comparable<T>> Check<ExceptionType> isBetween(String varName, T var,
 		T min, T max) throws ExceptionType {
 		return isNotNull(varName, var) //
 			.that(varName, var, var.compareTo(max) <= 0 && var.compareTo(min) >= 0, //
 				"must be between %s and %s",
 				min,
 				max);
 	}
 
 	/**
 	 * Checks that the variable contains the given element
 	 * 
 	 * @param <T>
 	 *          the contained element type
 	 * @param varName
 	 *          the name of the variable to be checked
 	 * @param var
 	 *          the variable to be checked
 	 * @param element
 	 *          the element that the variable must contain
 	 * @return <code>this</code>, in order to allow method chaining
 	 * @throws ExceptionType
 	 *           if the check failed
 	 */
 	public final <T> Check<ExceptionType> contains(String varName, ContainsAware<T> var, T element)
 		throws ExceptionType {
 		return isNotNull(varName, var)//
 			.that(varName, var, var.contains(element), "must contain %s", element);
 	}
 
 	/**
 	 * Checks the <code>var</code> is not null and contained by the given
 	 * <code>container</code>
 	 * 
 	 * @param varName
 	 *          the name of the variable to be checked
 	 * @param var
 	 *          the variable to be checked
 	 * @param container
 	 *          the {@link ContainsAware} that must contain <code>var</code>
 	 * @return <code>this</code>, in order to allow method chaining
 	 * @throws ExceptionType
 	 *           if the check failed
 	 */
 	public final <T> Check<ExceptionType> isIn(String varName, T var, ContainsAware<T> container)
 		throws ExceptionType {
 		return isNotNull(varName, var)//
 			.that(varName, var, container.contains(var), "must be in %s", container);
 	}
 
 	/**
 	 * Checks that the variable is not <code>null</code> and &gt; than
 	 * <code>other</code>
 	 * 
 	 * @param <T>
 	 *          the contained element type
 	 * @param varName
 	 *          the name of the variable to be checked
 	 * @param var
 	 *          the variable to be checked
 	 * @param other
 	 *          the {@link Comparable} object of the same type to be compared
 	 *          against <code>var</code>
 	 * @return <code>this</code>, in order to allow method chaining
 	 * @throws ExceptionType
 	 *           if the check failed
 	 */
 	public final <T extends Comparable<T>> Check<ExceptionType> isGreaterThan(String varName, T var,
 		T other) throws ExceptionType {
 		return isNotNull(varName, var)//
 			.that(varName, var, var.compareTo(other) > 0, "must be greater than %s", other);
 	}
 
 	/**
 	 * Checks that the variable is not <code>null</code> and &gt;= than
 	 * <code>other</code>
 	 * 
 	 * @param <T>
 	 *          the contained element type
 	 * @param varName
 	 *          the name of the variable to be checked
 	 * @param var
 	 *          the variable to be checked
 	 * @param other
 	 *          the {@link Comparable} object of the same type to be compared
 	 *          against <code>var</code>
 	 * @return <code>this</code>, in order to allow method chaining
 	 * @throws ExceptionType
 	 *           if the check failed
 	 */
 	public final <T extends Comparable<T>> Check<ExceptionType> isGreaterThanOrEqualTo(
 		String varName, T var, T other) throws ExceptionType {
 		return isNotNull(varName, var)//
 			.that(varName, var, var.compareTo(other) >= 0, "must be greater than or equal to %s", other);
 	}
 
 	/**
 	 * Checks that the variable is not <code>null</code> and &lt; than
 	 * <code>other</code>
 	 * 
 	 * @param <T>
 	 *          the contained element type
 	 * @param varName
 	 *          the name of the variable to be checked
 	 * @param var
 	 *          the variable to be checked
 	 * @param other
 	 *          the {@link Comparable} object of the same type to be compared
 	 *          against <code>var</code>
 	 * @return <code>this</code>, in order to allow method chaining
 	 * @throws ExceptionType
 	 *           if the check failed
 	 */
 	public final <T extends Comparable<T>> Check<ExceptionType> isLessThan(String varName, T var,
 		T other) throws ExceptionType {
 		return isNotNull(varName, var)//
 			.that(varName, var, var.compareTo(other) < 0, "must be less than %s", other);
 	}
 
 	/**
 	 * Checks that the variable is not <code>null</code> and &lt;= than
 	 * <code>other</code>
 	 * 
 	 * @param <T>
 	 *          the contained element type
 	 * @param varName
 	 *          the name of the variable to be checked
 	 * @param var
 	 *          the variable to be checked
 	 * @param other
 	 *          the {@link Comparable} object of the same type to be compared
 	 *          against <code>var</code>
 	 * @return <code>this</code>, in order to allow method chaining
 	 * @throws ExceptionType
 	 *           if the check failed
 	 */
 	public final <T extends Comparable<T>> Check<ExceptionType> isLessThanOrEqualTo(String varName,
 		T var, T other) throws ExceptionType {
 		return isNotNull(varName, var) //
 			.that(varName, var, var.compareTo(other) <= 0, "must be less than or equal to %s", other);
 	}
 
 	/**
 	 * A basic check failure
 	 * 
 	 * @author flbulgarelli
 	 */
 	public static class Failure {
 		private final String message;
 
 		/**
 		 * Creates a new {@link Failure}
 		 * 
 		 * @param message
 		 *          the failure message
 		 */
 		public Failure(String message) {
 			this.message = message;
 		}
 
 		/**
 		 * @return the failure message
 		 */
 		public String createMessage() {
 			return message;
 		}
 	}
 
 	/**
 	 * A check failure that contains the name and value of the variable that was
 	 * checked
 	 * 
 	 * @author flbulgarelli
 	 * 
 	 */
 	public static class VarFailure extends Failure {
 		private final String varName;
 		private final Object var;
 
 		/**
 		 * Creates a new {@link VarFailure}
 		 * 
 		 * @param varName
 		 *          the name of the variable checked
 		 * @param var
 		 *          the variable checked
 		 * 
 		 * @param message
 		 *          the failure message
 		 */
 		public VarFailure(String varName, Object var, String message) {
 			super(message);
 			this.varName = varName;
 			this.var = var;
 		}
 
 		/**
 		 * @return the var
 		 */
 		public Object getVar() {
 			return var;
 		}
 
 		/**
 		 * @return the varName
 		 */
 		public String getVarName() {
 			return varName;
 		}
 
 		/**
 		 * @return the failure message, which does not contain the variable name nor
 		 *         value
 		 */
 		public String createSimpleMessage() {
 			return super.createMessage();
 		}
 
 		/**
 		 * @return the failure message, which contains the variable name and value
 		 */
 		public String createMessage() {
 			return Var.format(varName, var, ": " + super.createMessage());
 		}
 	}
 }
