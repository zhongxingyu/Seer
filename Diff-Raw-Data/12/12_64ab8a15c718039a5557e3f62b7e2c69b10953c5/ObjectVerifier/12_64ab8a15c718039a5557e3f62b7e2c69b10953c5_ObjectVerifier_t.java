 package com.github.ver4j;
 
 import java.util.LinkedHashMap;
 import java.util.Locale;
 import java.util.Map;
 
 import javax.annotation.Nonnull;
 
 public class ObjectVerifier extends AVerifier {
 	private static final String FAILED_IS_TRUE_CAUSE = "the expression is false";
 
 	private static final String FAILED_IS_FALSE_CAUSE = "the expression is true";
 
 	private static final String FAILED_IS_ASSIGNABLE_FROM_CAUSE = "the class can not be casted to the reference class";
 
 	private static final String FAILED_IS_INSTANCE_OF_CAUSE = "the object is not an instance of the reference class";
 
 	private static final String FAILED_NOT_NULL_CAUSE = "the object is null";
 
 	private final String category;
 
 	private final ExceptionMessageInfo defaultExceptionMessageInfo;
 
 	private final ExceptionFactory<?> isTrueExceptionFactory;
 
 	private final ExceptionFactory<?> isFalseExceptionFactory;
 
 	private final ExceptionFactory<?> isAssignableFromExceptionFactory;
 
 	private final ExceptionFactory<?> isInstanceOfExceptionFactory;
 
 	private final ExceptionFactory<?> nullExceptionFactory;
 
 	public class Internal {
 		public final boolean isTrue(final boolean expression, Locale locale,
 				String errorMessageTemplate, Object errorMessage,
 				Object[] errorMessageArgs) {
 			if (isDisabled())
 				return expression;
 
 			return new AVerifierTask<Boolean>() {
 				@Override
 				protected Boolean result() {
 					return expression;
 				}
 
 				@Override
 				protected boolean isValid() {
 					return expression;
 				}
 			}.verify(isTrueExceptionFactory, locale, errorMessageTemplate,
 					errorMessage, errorMessageArgs);
 		}
 	}
 
	private final Internal internal = new Internal();
 
 	public ObjectVerifier(String category,
 			@Nonnull ExceptionMessageInfo defaultExceptionMessageInfo,
 			@Nonnull ExceptionTypeInfo<?> generalExceptionTypeInfo,
 			@Nonnull ExceptionTypeInfo<?> classCastExceptionTypeInfo,
 			@Nonnull ExceptionTypeInfo<?> nullExceptionTypeInfo) {
 		this.category = category;
 		this.defaultExceptionMessageInfo = defaultExceptionMessageInfo;
 		isTrueExceptionFactory = exceptionFactoryOf(generalExceptionTypeInfo,
 				defaultExceptionMessageInfo.appendCause(FAILED_IS_TRUE_CAUSE));
 		isFalseExceptionFactory = exceptionFactoryOf(generalExceptionTypeInfo,
 				defaultExceptionMessageInfo.appendCause(FAILED_IS_FALSE_CAUSE));
 		isAssignableFromExceptionFactory = exceptionFactoryOf(
 				classCastExceptionTypeInfo,
 				defaultExceptionMessageInfo
 						.appendCause(FAILED_IS_ASSIGNABLE_FROM_CAUSE));
 		isInstanceOfExceptionFactory = exceptionFactoryOf(
 				classCastExceptionTypeInfo,
 				defaultExceptionMessageInfo
 						.appendCause(FAILED_IS_INSTANCE_OF_CAUSE));
 		nullExceptionFactory = exceptionFactoryOf(nullExceptionTypeInfo,
 				defaultExceptionMessageInfo.appendCause(FAILED_NOT_NULL_CAUSE));
 	}
 
 	public final Internal internal() {
 		return internal;
 	}
 
 	public final boolean isTrue(boolean expression) {
 		return internal.isTrue(expression, null, null, null, null);
 	}
 
 	public final boolean isTrue(boolean expression, Object errorMessage,
 			Object... errorMessageArgs) {
 		return internal.isTrue(expression, null, null, errorMessage,
 				errorMessageArgs);
 	}
 
 	public final boolean isTrueCm(boolean expression,
 			String errorMessageTemplate, Locale locale,
 			Object... errorMessageArgs) {
 		return internal.isTrue(expression, locale, errorMessageTemplate, null,
 				errorMessageArgs);
 	}
 
 	public final void isFalse(boolean expression) {
 		if (isDisabled() || !expression)
 			return;
 		throw isFalseExceptionFactory.newException();
 	}
 
 	public final void isFalse(boolean expression, Object errorMessage,
 			Object... errorMessageArgs) {
 		if (isDisabled() || !expression)
 			return;
 		throw isFalseExceptionFactory.newException(errorMessage,
 				errorMessageArgs);
 	}
 
 	public final void isFalseCm(boolean expression,
 			String errorMessageTemplate, Locale locale,
 			Object... errorMessageArgs) {
 		if (isDisabled() || !expression)
 			return;
 		throw isFalseExceptionFactory.newExceptionCm(errorMessageTemplate,
 				locale, errorMessageArgs);
 	}
 
 	@Nonnull
 	public final <C> Class<C> assignableFrom(@Nonnull Class<?> superType,
 			@Nonnull Class<C> type) {
 		notNull(superType);
 		notNull(type);
 		if (isDisabled() || superType.isAssignableFrom(type))
 			return type;
 		throw isAssignableFromExceptionFactory.newException(
 				newAssignableFromContext(superType, type), (Throwable) null);
 	}
 
 	@Nonnull
 	public final <C> Class<C> assignableFrom(@Nonnull Class<?> superType,
 			@Nonnull Class<C> type, Object errorMessage,
 			Object... errorMessageArgs) {
 		notNull(superType, errorMessage, errorMessageArgs);
 		notNull(type, errorMessage, errorMessageArgs);
 		if (isDisabled() || superType.isAssignableFrom(type))
 			return type;
 		throw isAssignableFromExceptionFactory.newException(errorMessage,
 				errorMessageArgs, newAssignableFromContext(superType, type),
 				null);
 	}
 
 	@Nonnull
 	public final <C> Class<C> assignableFromCm(@Nonnull Class<?> superType,
 			@Nonnull Class<C> type, String errorMessageTemplate, Locale locale,
 			Object... errorMessageArgs) {
 		notNull(superType, errorMessageTemplate, locale, errorMessageArgs);
 		notNull(type, errorMessageTemplate, locale, errorMessageArgs);
 		if (isDisabled() || superType.isAssignableFrom(type))
 			return type;
 		throw isAssignableFromExceptionFactory.newExceptionCm(
 				errorMessageTemplate, locale, errorMessageArgs,
 				newAssignableFromContext(superType, type), null);
 	}
 
 	@Nonnull
 	private final Map<Object, Object> newAssignableFromContext(
 			@Nonnull Class<?> superType, @Nonnull Class<?> type) {
 		Map<Object, Object> context = newContextMap();
 		context.put("reference class", superType);
 		context.put("tested class", type);
 		return context;
 	}
 
 	@Nonnull
 	public final <T> T instanceOf(@Nonnull Object obj, @Nonnull Class<T> type) {
 		notNull(type);
 		notNull(obj);
 		if (isDisabled() || type.isInstance(obj))
 			return type.cast(obj);
 		throw isInstanceOfExceptionFactory.newException(
 				newInstanceOfContext(obj, type), (Throwable) null);
 	}
 
 	@Nonnull
 	public final <T> T instanceOf(@Nonnull Object obj, @Nonnull Class<T> type,
 			Object errorMessage, Object... errorMessageArgs) {
 		notNull(type, errorMessage, errorMessageArgs);
 		notNull(obj, errorMessage, errorMessageArgs);
 		if (isDisabled() || type.isInstance(obj))
 			return type.cast(obj);
 		throw isInstanceOfExceptionFactory.newException(errorMessage,
 				errorMessageArgs, newInstanceOfContext(obj, type), null);
 	}
 
 	@Nonnull
 	public final <T> T instanceOfCm(@Nonnull Object obj,
 			@Nonnull Class<T> type, String errorMessageTemplate, Locale locale,
 			Object... errorMessageArgs) {
 		notNullCm(type, errorMessageTemplate, locale, errorMessageArgs);
 		notNullCm(obj, errorMessageTemplate, locale, errorMessageArgs);
 		if (isDisabled() || type.isInstance(obj))
 			return type.cast(obj);
 		throw isInstanceOfExceptionFactory
 				.newExceptionCm(errorMessageTemplate, locale, errorMessageArgs,
 						newInstanceOfContext(obj, type), null);
 	}
 
 	@Nonnull
 	private final Map<Object, Object> newInstanceOfContext(@Nonnull Object obj,
 			@Nonnull Class<?> type) {
 		Map<Object, Object> context = newContextMap();
 		context.put("tested object", obj);
 		context.put("reference class", type);
 		return context;
 	}
 
 	@Nonnull
 	public final <T> T notNull(@Nonnull T object) {
 		if (isDisabled() || object != null)
 			return object;
 		throw nullExceptionFactory.newException();
 	}
 
 	@Nonnull
 	public final <T> T notNull(@Nonnull T object, Object errorMessage,
 			Object... errorMessageArgs) {
 		if (isDisabled() || object != null)
 			return object;
 		throw nullExceptionFactory.newException(errorMessage, errorMessageArgs);
 	}
 
 	@Nonnull
 	public final <T> T notNullCm(@Nonnull T object,
 			String errorMessageTemplate, Locale locale,
 			Object... errorMessageArgs) {
 		if (isDisabled() || object != null)
 			return object;
 		throw nullExceptionFactory.newExceptionCm(errorMessageTemplate, locale,
 				errorMessageArgs);
 	}
 
 	@Nonnull
 	protected final Map<Object, Object> newContextMap() {
 		return new LinkedHashMap<Object, Object>();
 	}
 
 	@Nonnull
 	public ExceptionMessageInfo getDefaultExceptionMessageInfo() {
 		return defaultExceptionMessageInfo;
 	}
 
 	@Override
 	public final String getCategory() {
 		return category;
 	}
 }
