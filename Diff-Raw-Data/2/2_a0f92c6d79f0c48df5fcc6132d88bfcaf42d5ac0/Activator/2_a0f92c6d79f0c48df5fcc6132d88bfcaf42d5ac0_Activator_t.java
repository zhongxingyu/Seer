 package com.secpro.platform.log;
 
 import org.osgi.framework.BundleActivator;
 import org.osgi.framework.BundleContext;
 import org.slf4j.LoggerFactory;
 
 import ch.qos.logback.classic.LoggerContext;
 import ch.qos.logback.classic.joran.JoranConfigurator;
 import ch.qos.logback.core.joran.spi.JoranException;
 
 import com.secpro.platform.log.utils.PlatformLogger;
 
 /**
  * @author Martin Bai. Logging bundle just provide logging function for other
  *         bundle. May 31, 2012
  */
 public class Activator implements BundleActivator {
 	private static PlatformLogger _logger = PlatformLogger.getLogger(Activator.class);
 	private static BundleContext context;
 	public final static String LOGGING_CONFIGURATION_PATH = "loggingConfigurationPath";
 	public final static String DEFAULT_LOGGING_CONFIGURATION_PATH = "configuration/logging.xml";
 
 	static BundleContext getContext() {
 		return context;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext
 	 * )
 	 */
 	public void start(BundleContext bundleContext) throws Exception {
 		Activator.context = bundleContext;
 		initConfigurationForLogging(getLoggingConfigurationPath());
 		_logger.info("Logging bundle is ready ^");
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
 	 */
 	public void stop(BundleContext bundleContext) throws Exception {
 		Activator.context = null;
 	}
 
 	/**
 	 * @param logConfigruationPath
 	 * @throws Exception
 	 *             read and set the logging configuration from starting
 	 *             parameter
 	 */
 	private void initConfigurationForLogging(String logConfigruationPath) throws Exception {
 		if (logConfigruationPath == null || logConfigruationPath.trim().equals("")) {
			throw new Exception("invalid logging configuration path.");
 		}
 		try {
 			LoggerContext logContext = (LoggerContext) LoggerFactory.getILoggerFactory();
 			JoranConfigurator configurator = new JoranConfigurator();
 			configurator.setContext(logContext);
 			logContext.reset();
 			configurator.doConfigure(logConfigruationPath);
 		} catch (JoranException je) {
 			je.printStackTrace();
 			throw je;
 		}
 	}
 
 	/**
 	 * Gets the file path.
 	 * 
 	 * @return
 	 */
 	public String getLoggingConfigurationPath() {
 		String path = System.getProperty(LOGGING_CONFIGURATION_PATH);
 		if (path == null || path.trim().length() == 0) {
 			path = DEFAULT_LOGGING_CONFIGURATION_PATH;
 		}
 		return path;
 	}
 }
