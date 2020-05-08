 package net.meisen.general.genmisc.exceptions.registry;
 
 import java.util.Locale;
 
 /**
  * Registry and central location of all the available <code>Exceptions</code>.
  * 
  * @author pmeisen
  * 
  */
 public interface IExceptionRegistry {
 
 	/**
 	 * Throws an <code>RuntimeException</code> of the specified class having the
 	 * error message of the specified number and the specified reason, latter
 	 * might be <code>null</code>.
 	 * 
 	 * @param exceptionClazz
 	 *          the class of the exception to be thrown
 	 * @param number
 	 *          the number of the error, to look up the message within the
 	 *          registry's catalogs
 	 * @param reason
 	 *          the reason for the exception, might be <code>null</code>
 	 * 
 	 * @throws T
 	 *           the exception of the class specified by
 	 *           <code>exceptionClazz</code>
 	 * 
 	 * @see RuntimeException
 	 */
 	public <T extends RuntimeException> void throwRuntimeException(
 			final Class<T> exceptionClazz, final Integer number,
 			final Throwable reason) throws T;
 
 	/**
 	 * Throws an <code>RuntimeException</code> of the specified class having the
 	 * error message of the specified number.
 	 * 
 	 * @param exceptionClazz
 	 *          the class of the exception to be thrown
 	 * @param number
 	 *          the number of the error, to look up the message within the
 	 *          registry's catalogs
 	 * 
 	 * @throws T
 	 *           the exception of the class specified by
 	 *           <code>exceptionClazz</code>
 	 * 
 	 * @see RuntimeException
 	 */
 	public <T extends RuntimeException> void throwRuntimeException(
 			final Class<T> exceptionClazz, final Integer number) throws T;
 
 	/**
 	 * Throws an <code>RuntimeException</code> of the specified class having the
 	 * error message of the specified number, whereby the passed
 	 * <code>parameter<code>s are replaced.
 	 * 
 	 * @param exceptionClazz
 	 *          the class of the exception to be thrown
 	 * @param number
 	 *          the number of the error, to look up the message within the
 	 *          registry's catalogs
 	 * @param parameter
 	 *          the parameters to be replaced within the message
 	 * 
 	 * @throws T
 	 *           the exception of the class specified by <code>exceptionClazz
 	 *           </code>
 	 * 
 	 * @see RuntimeException
 	 * @see String#format(String, Object...)
 	 */
 	public <T extends RuntimeException> void throwRuntimeException(
 			final Class<T> exceptionClazz, final Integer number,
 			final Object... parameter) throws T;
 
 	/**
 	 * Throws an <code>RuntimeException</code> of the specified class having the
 	 * error message (with the replaced <code>parameter</code>s) of the specified
 	 * number and the specified reason, latter might be <code>null</code>.
 	 * 
 	 * @param exceptionClazz
 	 *          the class of the exception to be thrown
 	 * @param number
 	 *          the number of the error, to look up the message within the
 	 *          registry's catalogs
 	 * @param reason
 	 *          the reason for the exception, might be <code>null</code>
 	 * @param parameter
 	 *          the parameters to be replaced within the message
 	 * 
 	 * @throws T
 	 *           the exception of the class specified by
 	 *           <code>exceptionClazz</code>
 	 * 
 	 * @see RuntimeException
 	 * @see String#format(String, Object...)
 	 */
 	public <T extends RuntimeException> void throwRuntimeException(
 			final Class<T> exceptionClazz, final Integer number,
 			final Throwable reason, final Object... parameter) throws T;
 
 	/**
 	 * Throws an <code>RuntimeException</code> of the specified class having the
 	 * error message (in the passed <code>Locale</code>) of the specified number
 	 * and the specified reason, latter might be <code>null</code>.
 	 * 
 	 * @param exceptionClazz
 	 *          the class of the exception to be thrown
 	 * @param number
 	 *          the number of the error, to look up the message within the
 	 *          registry's catalogs
 	 * @param locale
 	 *          the <code>Locale</code> to be used to get the error message and
 	 *          formats
 	 * @param reason
 	 *          the reason for the exception, might be <code>null</code>
 	 * 
 	 * @throws T
 	 *           the exception of the class specified by
 	 *           <code>exceptionClazz</code>
 	 * 
 	 * @see RuntimeException
 	 */
 	public <T extends RuntimeException> void throwRuntimeException(
 			final Class<T> exceptionClazz, final Integer number, final Locale locale,
 			final Throwable reason) throws T;
 
 	/**
 	 * Throws an <code>RuntimeException</code> of the specified class having the
 	 * error message (in the passed <code>Locale</code>) of the specified number.
 	 * 
 	 * @param exceptionClazz
 	 *          the class of the exception to be thrown
 	 * @param number
 	 *          the number of the error, to look up the message within the
 	 *          registry's catalogs
 	 * @param locale
 	 *          the <code>Locale</code> to be used to get the error message and
 	 *          formats
 	 * 
 	 * @throws T
 	 *           the exception of the class specified by
 	 *           <code>exceptionClazz</code>
 	 * 
 	 * @see RuntimeException
 	 */
 	public <T extends RuntimeException> void throwRuntimeException(
 			final Class<T> exceptionClazz, final Integer number, final Locale locale)
 			throws T;
 
 	/**
 	 * Throws an <code>RuntimeException</code> of the specified class having the
 	 * error message (in the passed <code>Locale</code> and with replaced
 	 * <code>parameter</code>s) of the specified number.
 	 * 
 	 * @param exceptionClazz
 	 *          the class of the exception to be thrown
 	 * @param number
 	 *          the number of the error, to look up the message within the
 	 *          registry's catalogs
 	 * @param locale
 	 *          the <code>Locale</code> to be used to get the error message and
 	 *          formats
 	 * @param parameter
 	 *          the parameters to be replaced within the message
 	 * 
 	 * @throws T
 	 *           the exception of the class specified by
 	 *           <code>exceptionClazz</code>
 	 * 
 	 * @see RuntimeException
 	 * @see String#format(Locale, String, Object...)
 	 */
 	public <T extends RuntimeException> void throwRuntimeException(
 			final Class<T> exceptionClazz, final Integer number, final Locale locale,
 			final Object... parameter) throws T;
 
 	/**
 	 * Throws an <code>RuntimeException</code> of the specified class having the
 	 * error message (in the passed <code>Locale</code> and with replaced
 	 * <code>parameter</code>s) of the specified number and the specified reason,
 	 * latter might be <code>null</code>.
 	 * 
 	 * @param exceptionClazz
 	 *          the class of the exception to be thrown
 	 * @param number
 	 *          the number of the error, to look up the message within the
 	 *          registry's catalogs
 	 * @param locale
 	 *          the <code>Locale</code> to be used to get the error message and
 	 *          formats
 	 * @param reason
 	 *          the reason for the exception, might be <code>null</code>
 	 * @param parameter
 	 *          the parameters to be replaced within the message
 	 * 
 	 * @throws T
 	 *           the exception of the class specified by
 	 *           <code>exceptionClazz</code>
 	 * 
 	 * @see RuntimeException
 	 * @see String#format(Locale, String, Object...)
 	 */
 	public <T extends RuntimeException> void throwRuntimeException(
 			final Class<T> exceptionClazz, final Integer number, final Locale locale,
 			final Throwable reason, final Object... parameter) throws T;
 
 	/**
 	 * Throws an <code>Exception</code> of the specified class having the error
 	 * message of the specified number and the specified reason, latter might be
 	 * <code>null</code>.
 	 * 
 	 * @param exceptionClazz
 	 *          the class of the exception to be thrown
 	 * @param number
 	 *          the number of the error, to look up the message within the
 	 *          registry's catalogs
 	 * @param reason
 	 *          the reason for the exception, might be <code>null</code>
 	 * 
 	 * @throws T
 	 *           the exception of the class specified by
 	 *           <code>exceptionClazz</code>
 	 * 
 	 * @see Exception
 	 */
 	public <T extends Exception> void throwException(
 			final Class<T> exceptionClazz, final Integer number,
 			final Throwable reason) throws T;
 
 	/**
 	 * Throws an <code>Exception</code> of the specified class having the error
 	 * message of the specified number.
 	 * 
 	 * @param exceptionClazz
 	 *          the class of the exception to be thrown
 	 * @param number
 	 *          the number of the error, to look up the message within the
 	 *          registry's catalogs
 	 * 
 	 * @throws T
 	 *           the exception of the class specified by
 	 *           <code>exceptionClazz</code>
 	 * 
 	 * @see Exception
 	 */
 	public <T extends Exception> void throwException(
 			final Class<T> exceptionClazz, final Integer number) throws T;
 
 	/**
 	 * Throws an <code>Exception</code> of the specified class having the error
 	 * message of the specified number, whereby the passed
	 * <code>parameter<code>s are replaced.
 	 * 
 	 * @param exceptionClazz
 	 *          the class of the exception to be thrown
 	 * @param number
 	 *          the number of the error, to look up the message within the
 	 *          registry's catalogs
 	 * @param parameter
 	 *          the parameters to be replaced within the message
 	 * 
 	 * @throws T
 	 *           the exception of the class specified by <code>exceptionClazz
 	 *           </code>
 	 * 
 	 * @see Exception
 	 * @see String#format(String, Object...)
 	 */
 	public <T extends Exception> void throwException(
 			final Class<T> exceptionClazz, final Integer number,
 			final Object... parameter) throws T;
 
 	/**
 	 * Throws an <code>Exception</code> of the specified class having the error
 	 * message (with the replaced <code>parameter</code>s) of the specified number
 	 * and the specified reason, latter might be <code>null</code>.
 	 * 
 	 * @param exceptionClazz
 	 *          the class of the exception to be thrown
 	 * @param number
 	 *          the number of the error, to look up the message within the
 	 *          registry's catalogs
 	 * @param reason
 	 *          the reason for the exception, might be <code>null</code>
 	 * @param parameter
 	 *          the parameters to be replaced within the message
 	 * 
 	 * @throws T
 	 *           the exception of the class specified by
 	 *           <code>exceptionClazz</code>
 	 * 
 	 * @see Exception
 	 * @see String#format(String, Object...)
 	 */
 	public <T extends Exception> void throwException(
 			final Class<T> exceptionClazz, final Integer number,
 			final Throwable reason, final Object... parameter) throws T;
 
 	/**
 	 * Throws an <code>Exception</code> of the specified class having the error
 	 * message (in the passed <code>Locale</code>) of the specified number and the
 	 * specified reason, latter might be <code>null</code>.
 	 * 
 	 * @param exceptionClazz
 	 *          the class of the exception to be thrown
 	 * @param number
 	 *          the number of the error, to look up the message within the
 	 *          registry's catalogs
 	 * @param locale
 	 *          the <code>Locale</code> to be used to get the error message and
 	 *          formats
 	 * @param reason
 	 *          the reason for the exception, might be <code>null</code>
 	 * 
 	 * @throws T
 	 *           the exception of the class specified by
 	 *           <code>exceptionClazz</code>
 	 * 
 	 * @see Exception
 	 */
 	public <T extends Exception> void throwException(
 			final Class<T> exceptionClazz, final Integer number, final Locale locale,
 			final Throwable reason) throws T;
 
 	/**
 	 * Throws an <code>Exception</code> of the specified class having the error
 	 * message (in the passed <code>Locale</code>) of the specified number.
 	 * 
 	 * @param exceptionClazz
 	 *          the class of the exception to be thrown
 	 * @param number
 	 *          the number of the error, to look up the message within the
 	 *          registry's catalogs
 	 * @param locale
 	 *          the <code>Locale</code> to be used to get the error message and
 	 *          formats
 	 * 
 	 * @throws T
 	 *           the exception of the class specified by
 	 *           <code>exceptionClazz</code>
 	 * 
 	 * @see Exception
 	 */
 	public <T extends Exception> void throwException(
 			final Class<T> exceptionClazz, final Integer number, final Locale locale)
 			throws T;
 
 	/**
 	 * Throws an <code>Exception</code> of the specified class having the error
 	 * message (in the passed <code>Locale</code> and with replaced
 	 * <code>parameter</code>s) of the specified number.
 	 * 
 	 * @param exceptionClazz
 	 *          the class of the exception to be thrown
 	 * @param number
 	 *          the number of the error, to look up the message within the
 	 *          registry's catalogs
 	 * @param locale
 	 *          the <code>Locale</code> to be used to get the error message and
 	 *          formats
 	 * @param parameter
 	 *          the parameters to be replaced within the message
 	 * 
 	 * @throws T
 	 *           the exception of the class specified by
 	 *           <code>exceptionClazz</code>
 	 * 
 	 * @see Exception
 	 * @see String#format(Locale, String, Object...)
 	 */
 	public <T extends Exception> void throwException(
 			final Class<T> exceptionClazz, final Integer number, final Locale locale,
 			final Object... parameter) throws T;
 
 	/**
 	 * Throws an <code>Exception</code> of the specified class having the error
 	 * message (in the passed <code>Locale</code> and with replaced
 	 * <code>parameter</code>s) of the specified number and the specified reason,
 	 * latter might be <code>null</code>.
 	 * 
 	 * @param exceptionClazz
 	 *          the class of the exception to be thrown
 	 * @param number
 	 *          the number of the error, to look up the message within the
 	 *          registry's catalogs
 	 * @param locale
 	 *          the <code>Locale</code> to be used to get the error message and
 	 *          formats
 	 * @param reason
 	 *          the reason for the exception, might be <code>null</code>
 	 * @param parameter
 	 *          the parameters to be replaced within the message
 	 * 
 	 * @throws T
 	 *           the exception of the class specified by
 	 *           <code>exceptionClazz</code>
 	 * 
 	 * @see Exception
 	 * @see String#format(Locale, String, Object...)
 	 */
 	public <T extends Exception> void throwException(
 			final Class<T> exceptionClazz, final Integer number, final Locale locale,
 			final Throwable reason, final Object... parameter) throws T;
 }
