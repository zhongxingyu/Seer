 package it.unitn.disi.unagi.rcpapp.handlers;
 
 import it.unitn.disi.unagi.rcpapp.IUnagiRcpAppBundleInfoProvider;
 import it.unitn.disi.unagi.rcpapp.nls.Messages;
 import it.unitn.disi.util.logging.LogUtil;
 
 import javax.inject.Inject;
 
 import org.eclipse.e4.core.di.annotations.Execute;
 
 /**
  * Abstract class for command handlers in the RCP App bundle. Contains helper methods that are common to any command
  * handler.
  * 
  * @author Vitor E. Silva Souza (vitorsouza@gmail.com)
  * @version 1.0
  */
 public abstract class AbstractHandler {
 	/** Default class name suffix for handler classes. */
 	private static final String DEFAULT_HANDLER_CLASS_SUFFIX = "Handler"; //$NON-NLS-1$
 
 	/** The bundle's activator, used to retrieve global information about the bundle. */
 	@Inject
 	protected IUnagiRcpAppBundleInfoProvider activator;
 
 	/** The string key that corresponds to the concrete class that implements this abstract class. */
 	private String classKey;
 
 	/**
 	 * Fallback handler method that notifies through the logging infrastructure that something is possibly not right.
 	 */
 	@Execute
 	public void execute() {
 		LogUtil.log.warn("Handler using fallback method (not implemented or DI problem?): ", getClass().getName()); //$NON-NLS-1$
 	}
 
 	/**
 	 * Builds and returns the string key that corresponds to the concrete class that implements this abstract class.
 	 * 
 	 * @return The string key that corresponds to the concrete handler.
 	 */
 	protected String getClassKey() {
 		if (classKey == null) {
 			String className = getClass().getSimpleName();
 			if (className.contains(DEFAULT_HANDLER_CLASS_SUFFIX))
				;
			className = className.substring(0, className.indexOf(DEFAULT_HANDLER_CLASS_SUFFIX));
 			classKey = className.substring(0, 1).toLowerCase() + className.substring(1);
 		}
 
 		return classKey;
 	}
 
 	/**
 	 * Returns the job description that corresponds to the concrete handler that implements this abstract class.
 	 * 
 	 * @param name
 	 *          Name of the item that is handled by this abstract class. This name is used to format the i18n string
 	 *          retrieved from the resource bundle.
 	 * @return The job description that corresponds to the concrete handler.
 	 */
 	protected String getJobDescription(String name) {
 		return Messages.getFormattedString("service." + getClassKey() + ".description", name); //$NON-NLS-1$ //$NON-NLS-2$
 	}
 
 	/**
 	 * Returns the job error status that corresponds to the concrete handler that implements this abstract class.
 	 * 
 	 * @param name
 	 *          Name of the item that is handled by this abstract class. This name is used to format the i18n string
 	 *          retrieved from the resource bundle.
 	 * @return The job error status that corresponds to the concrete handler.
 	 */
 	protected String getJobErrorStatus(String name) {
 		return Messages.getString("service." + getClassKey() + ".error.status"); //$NON-NLS-1$ //$NON-NLS-2$;
 	}
 
 	/**
 	 * Returns the job error title that corresponds to the concrete handler that implements this abstract class.
 	 * 
 	 * @param name
 	 *          Name of the item that is handled by this abstract class. This name is used to format the i18n string
 	 *          retrieved from the resource bundle.
 	 * @return The job error title that corresponds to the concrete handler.
 	 */
 	protected String getJobErrorTitle(String name) {
 		return Messages.getString("service." + getClassKey() + ".error.status"); //$NON-NLS-1$ //$NON-NLS-2$;
 	}
 
 	/**
 	 * Returns the job error message that corresponds to the concrete handler that implements this abstract class.
 	 * 
 	 * @param name
 	 *          Name of the item that is handled by this abstract class. This name is used to format the i18n string
 	 *          retrieved from the resource bundle.
 	 * @return The job error message that corresponds to the concrete handler.
 	 */
 	protected String getJobErrorMessage(String name) {
 		return Messages.getFormattedString("service." + getClassKey() + ".error.message", name); //$NON-NLS-1$ //$NON-NLS-2$
 	}
 }
