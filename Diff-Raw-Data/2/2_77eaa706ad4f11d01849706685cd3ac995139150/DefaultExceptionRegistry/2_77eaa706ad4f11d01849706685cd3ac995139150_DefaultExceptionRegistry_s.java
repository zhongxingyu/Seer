 package net.meisen.general.genmisc.exceptions.registry;
 
 import java.lang.reflect.Constructor;
 import java.lang.reflect.InvocationTargetException;
 import java.util.HashMap;
 import java.util.Locale;
 import java.util.Map;
 import java.util.Map.Entry;
 
 import net.meisen.general.genmisc.exceptions.catalog.IBundledExceptionCatalog;
 import net.meisen.general.genmisc.exceptions.catalog.IExceptionCatalog;
 import net.meisen.general.genmisc.exceptions.catalog.ILocalizedExceptionCatalog;
 import net.meisen.general.genmisc.exceptions.catalog.InvalidCatalogEntryException;
 import net.meisen.general.genmisc.types.Classes;
 
 /**
  * Default implementation of a <code>ExceptionRegistry</code>. The default
  * implementation is based on a <code>Map</code> of
  * <code>ExceptionCatalog</code> instances, which are used to resolve
  * <code>Exceptions</code> of a concrete implementation.
  * 
  * @author pmeisen
  * 
  * @see IExceptionCatalog
  * @see Exception
  * @see RuntimeException
  * 
  */
 public class DefaultExceptionRegistry extends AbstractExceptionRegistry {
 	private Map<Class<? extends Exception>, IExceptionCatalog> registry = new HashMap<Class<? extends Exception>, IExceptionCatalog>();;
 
 	/**
 	 * Adds the passed <code>ExceptionCatalog</code> to the registry and
 	 * associates it to the passed <code>exceptionClazz</code>.
 	 * 
 	 * @param exceptionClazz
 	 *          the class of the exception to associate the catalog to
 	 * @param catalog
 	 *          the catalog to be associated
 	 */
 	public void addExceptionCatalog(
 			final Class<? extends Exception> exceptionClazz,
 			final IExceptionCatalog catalog) {
 		registry.put(exceptionClazz, catalog);
 	}
 
 	/**
 	 * Adds the passed <code>ExceptionCatalog</code> to the registry by creating
 	 * it via the default constructor. The created catalog is associated to the
 	 * specified <code>exceptionClazz</code>.
 	 * 
 	 * @param exceptionClazz
 	 *          the class of the exception to associate the catalog to
 	 * @param exceptionCatalogClazz
 	 *          the class of the <code>ExceptionCatalog</code>
 	 * 
 	 * @see IExceptionCatalog
 	 */
 	public void addExceptionCatalogByName(
 			final Class<? extends Exception> exceptionClazz,
 			final String exceptionCatalogClazz) {
 		final Class<?> clazz = Classes.getClass(exceptionCatalogClazz);
 
 		if (clazz == null) {
 			throw new IllegalArgumentException("Could not find the catalog clazz '"
 					+ exceptionCatalogClazz + "'.");
 		} else if (IExceptionCatalog.class.isAssignableFrom(clazz)) {
 			@SuppressWarnings("unchecked")
 			final Class<? extends IExceptionCatalog> catalogClazz = (Class<? extends IExceptionCatalog>) clazz;
 			addExceptionCatalogByClass(exceptionClazz, catalogClazz);
 		} else {
 			throw new IllegalArgumentException("The catalog clazz '"
 					+ exceptionCatalogClazz + "' is not an concrete implementation of '"
 					+ IExceptionCatalog.class.getName() + "'.");
 		}
 	}
 
 	/**
 	 * Adds the passed <code>ExceptionCatalog</code> to the registry by creating
 	 * it via the default constructor. The created catalog is associated to the
 	 * specified <code>exceptionClazz</code>.
 	 * 
 	 * @param exceptionClazz
 	 *          the class of the exception to associate the catalog to
 	 * @param exceptionCatalogClazz
 	 *          the class of the <code>ExceptionCatalog</code>
 	 * 
 	 * @see IExceptionCatalog
 	 */
 	public void addExceptionCatalogByClass(
 			final Class<? extends Exception> exceptionClazz,
 			final Class<? extends IExceptionCatalog> exceptionCatalogClazz) {
 
 		final IExceptionCatalog catalog;
 		try {
 			catalog = (IExceptionCatalog) exceptionCatalogClazz.newInstance();
 		} catch (final Exception e) {
 			throw new IllegalArgumentException("The catalog clazz '"
 					+ exceptionCatalogClazz
 					+ "' could not be instantiated using the default constructor.", e);
 		}
 
 		// if it is a bundled ExceptionCatalog start it
 		if (catalog instanceof IBundledExceptionCatalog) {
 			final IBundledExceptionCatalog bundledCatalog = (IBundledExceptionCatalog) catalog;
 			final String bundle = exceptionClazz.getName().replace(".", "/");
 
 			// load the bundle
 			try {
 				bundledCatalog.loadBundle(bundle);
 			} catch (final InvalidCatalogEntryException e) {
 				// don't do anything maybe it just shouldn't happen
 			}
 		}
 
 		addExceptionCatalog(exceptionClazz, catalog);
 	}
 
 	/**
 	 * Adds all the passed <code>ExceptionCatalog</code> to the registry and
 	 * associates it to the passed <code>exceptionClazz</code>.
 	 * 
 	 * @param exceptionCatalogs
 	 *          the map with the <code>ExceptionCatalog</code> instances and their
 	 *          associated <code>Exception</code> classes
 	 */
 	public void addExceptionCatalogs(
 			final Map<Class<? extends Exception>, IExceptionCatalog> exceptionCatalogs) {
 
 		// add each defined catalog
 		for (final Entry<Class<? extends Exception>, IExceptionCatalog> entry : exceptionCatalogs
 				.entrySet()) {
 			addExceptionCatalog(entry.getKey(), entry.getValue());
 		}
 	}
 
 	/**
 	 * Adds all the passed <code>ExceptionCatalog</code> to the registry and
 	 * associates it to the passed <code>exceptionClazz</code>. The
 	 * <code>ExceptionCatalog</code> is created using the default constructor.
 	 * 
 	 * @param exceptionCatalogs
 	 *          the map with the <code>ExceptionCatalog</code> instances and their
 	 *          associated <code>Exception</code> classes
 	 */
 	public void addExceptionCatalogsByName(
 			final Map<Class<? extends Exception>, String> exceptionCatalogs) {
 
 		// add each defined catalog
 		for (final Entry<Class<? extends Exception>, String> entry : exceptionCatalogs
 				.entrySet()) {
 			addExceptionCatalogByName(entry.getKey(), entry.getValue());
 		}
 	}
 
 	/**
 	 * Adds all the passed <code>ExceptionCatalog</code> to the registry and
 	 * associates it to the passed <code>exceptionClazz</code>. The
 	 * <code>ExceptionCatalog</code> is created using the default constructor.
 	 * 
 	 * @param exceptionCatalogs
 	 *          the map with the <code>ExceptionCatalog</code> instances and their
 	 *          associated <code>Exception</code> classes
 	 */
 	public void addExceptionCatalogsByClass(
 			final Map<Class<? extends Exception>, Class<? extends IExceptionCatalog>> exceptionCatalogs) {
 
 		// add each defined catalog
 		for (final Entry<Class<? extends Exception>, Class<? extends IExceptionCatalog>> entry : exceptionCatalogs
 				.entrySet()) {
 			addExceptionCatalogByClass(entry.getKey(), entry.getValue());
 		}
 	}
 
 	@Override
 	public <T extends RuntimeException> void throwRuntimeException(
 			final Class<T> exceptionClazz, final Integer number, final Locale locale,
 			final Throwable reason, final Object... parameter) throws T {
 		throwException(exceptionClazz, number, locale, reason, parameter);
 	}
 
 	@Override
 	public <T extends Exception> void throwException(
 			final Class<T> exceptionClazz, final Integer number, final Locale locale,
 			final Throwable reason, final Object... parameter) throws T {
 		final IExceptionCatalog catalog = registry.get(exceptionClazz);
 
 		// get the message to be shown
 		final String message;
 		if (catalog == null) {
 			throw new IllegalArgumentException(
 					"The catalog for the exceptionClass '"
 							+ exceptionClazz
							+ "' cannot be found, please register it within the registertry prior to using it.");
 		} else if (locale != null && catalog instanceof ILocalizedExceptionCatalog) {
 			final ILocalizedExceptionCatalog localizedCatalog = (ILocalizedExceptionCatalog) catalog;
 			message = localizedCatalog.getMessage(number, locale);
 		} else {
 			message = catalog.getMessage(number);
 		}
 
 		// replace the placeholders in the message
 		final String formattedMessage;
 		if (message == null) {
 			formattedMessage = "";
 		} else if (locale == null) {
 			formattedMessage = String.format(message, parameter);
 		} else {
 			formattedMessage = String.format(locale, message, parameter);
 		}
 
 		// construct the exception
 		T exception = null;
 		try {
 			exception = getException(exceptionClazz, formattedMessage, reason);
 		} catch (final Exception e) {
 			throw new IllegalArgumentException("Unable to throw the exception '"
 					+ exceptionClazz + "', please see the nested exceptions for details",
 					e);
 		}
 
 		if (exception == null) {
 			throw new IllegalArgumentException(
 					"Unable to generate an exception of class '"
 							+ (exceptionClazz == null ? null : exceptionClazz.getName())
 							+ "', no valid constructor was found. Make sure at least one constructor supports a string parameter.");
 		}
 
 		throw exception;
 	}
 
 	/**
 	 * Creates the <code>Exception</code> of the specified
 	 * <code>exceptionClazz</code>, whereby different constructs are used. The
 	 * following logic checks which construct should be used:
 	 * <ol>
 	 * <li>if a <code>Throwable</code> is defined a constructor with a
 	 * <code>String</code> and a <code>Throwable</code> is searched</li>
 	 * <li>otherwise a constructor with just a <code>String</code> is searched</li>
 	 * <li>if no constructor wasn't found yet, the default constructor is used (if
 	 * available)</li>
 	 * <li>if nothing could be created so far, the method will return
 	 * <code>null</code></li>
 	 * </ol>
 	 * 
 	 * @param exceptionClazz
 	 *          the <code>Class</code> of the exception to be generated
 	 * @param msg
 	 *          the message of the exception
 	 * @param r
 	 *          the reason for the exception
 	 * 
 	 * @return the created instance, <code>null</code> if no valid constructor was
 	 *         found
 	 * 
 	 * @throws InstantiationException
 	 *           if the <code>Exception</code> couldn't be instantiated
 	 * @throws IllegalAccessException
 	 *           if the <code>Constructor</code> couldn't be accessed
 	 * @throws InvocationTargetException
 	 *           if the invocation of the constructor failed
 	 */
 	protected <T extends Exception> T getException(final Class<T> exceptionClazz,
 			final String msg, final Throwable r) throws InstantiationException,
 			IllegalAccessException, InvocationTargetException {
 		T exception = null;
 
 		if (r != null && exception == null) {
 			try {
 				final Constructor<T> c = exceptionClazz.getConstructor(String.class,
 						Throwable.class);
 				exception = c.newInstance(msg, r);
 			} catch (final NoSuchMethodException e) {
 				// nothing to do
 			}
 		}
 
 		if (r != null && exception == null) {
 			try {
 				final Constructor<T> c = exceptionClazz.getConstructor(Throwable.class,
 						String.class);
 				exception = c.newInstance(r, msg);
 			} catch (final NoSuchMethodException e) {
 				// nothing to do
 			}
 		}
 
 		// if we still don't have one keep on searching
 		if (exception == null) {
 			try {
 				final Constructor<T> c = exceptionClazz.getConstructor(String.class);
 				exception = c.newInstance(msg);
 			} catch (final NoSuchMethodException e) {
 				// nothing to do
 			}
 		}
 
 		if (exception == null && r == null) {
 			try {
 				final Constructor<T> c = exceptionClazz.getConstructor(String.class,
 						Throwable.class);
 				exception = c.newInstance(msg, null);
 			} catch (final NoSuchMethodException e) {
 				// nothing to do
 			}
 		}
 
 		if (exception == null && r == null) {
 			try {
 				final Constructor<T> c = exceptionClazz.getConstructor(Throwable.class,
 						String.class);
 				exception = c.newInstance(msg, null);
 			} catch (final NoSuchMethodException e) {
 				// nothing to do
 			}
 		}
 
 		if (exception == null) {
 			try {
 				final Constructor<T> c = exceptionClazz.getConstructor();
 				exception = c.newInstance();
 			} catch (final NoSuchMethodException e) {
 				// nothing to do
 			}
 		}
 
 		// we are lost, what else should we do
 		return exception;
 	}
 }
