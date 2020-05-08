 /*
  * Copyright 2012-2013 Danylo Vashchilenko
  *
  * Licensed under the Apache License, Version 2.0 (the "License"); you may not
  * use this file except in compliance with the License. You may obtain a copy of
  * the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
  * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
  * License for the specific language governing permissions and limitations under
  * the License.
  */
 package org.key2gym.client;
 
 import java.awt.EventQueue;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.lang.reflect.InvocationTargetException;
 import java.util.Locale;
 import java.util.Properties;
 
 import javax.swing.UnsupportedLookAndFeelException;
 
 import org.apache.log4j.Level;
 import org.apache.log4j.Logger;
 import org.apache.log4j.PropertyConfigurator;
 import org.springframework.context.ApplicationContext;
 import org.springframework.context.support.AbstractApplicationContext;
 import org.springframework.context.support.ClassPathXmlApplicationContext;
 import org.springframework.security.core.context.SecurityContextHolder;
 
 /**
  * This is the main class of the application.
  * 
  * It's responsible for the following tasks:
  * <p/>
  * 
  * <ul>
  * 
  * <li>Initializing Logging system.</li>
  * 
  * <li>Processing command line arguments.</li>
  * 
  * <li>Reading and applying application properties.</li>
  * 
  * <li>Choosing connection to use.</li>
  * 
  * <li>Launching MainFrame.</li>
  * 
  * </ul>
  * 
  * @author Danylo Vashchilenko
  */
 public class Main {
 
 	private static final Logger logger = Logger.getLogger(Main.class.getName());
 	private static final Properties properties = new Properties();
 
 	/**
 	 * The main method which performs all the task described in the class
 	 * description.
 	 * 
 	 * @param args an array of arguments
 	 */
 	public static void main(String[] args) {
 
 		/*
 		 * Configures the logger using 'etc/logging.properties' or the default
 		 * logging properties file.
 		 */
 		try (InputStream input = new FileInputStream(PATH_LOGGING_PROPERTIES)) {
 			PropertyConfigurator.configure(input);
 		} catch (IOException ex) {
 			try (InputStream input = Thread.currentThread()
 					.getContextClassLoader()
 					.getResourceAsStream(RESOURCE_DEFAULT_LOGGING_PROPERTIES)) {
 				PropertyConfigurator.configure(input);
 
 				/*
 				 * Notify that the default logging properties file has been
 				 * used.
 				 */
 				logger.info("Could not load the logging properties file");
 			} catch (IOException ex2) {
 				throw new RuntimeException(
 						"Failed to initialize logging system", ex2);
 			}
 		}
 
 		logger.info("Starting...");
 
 		/*
 		 * Loads the built-in default client properties file.
 		 */
 		try (InputStream input = Thread.currentThread().getContextClassLoader()
 				.getResourceAsStream(RESOURCE_DEFAULT_CLIENT_PROPERTIES)) {
 			Properties defaultProperties = null;
 			defaultProperties = new Properties();
 			defaultProperties.load(input);
 			properties.putAll(defaultProperties);
 		} catch (IOException ex) {
 			throw new RuntimeException(
 					"Failed to load the default client properties file", ex);
 		}
 
 		/*
 		 * System context is used to retrieve the domain-wide client properties.
 		 */
 		/*InitialContext systemContext;
 		try {
 			systemContext = new InitialContext();
 			properties.putAll((Properties) systemContext.lookup("key2gym"));
 			systemContext.close();
 		} catch (NamingException ex) {
 			throw new RuntimeException(
 					"Failed to retrieve the domain-wide properties from the application server",
 					ex);
 		}*/
 		// TODO: replace with web-based domain properties?
 
 		/*
 		 * Loads the local client properties file.
 		 */
 		try (FileInputStream input = new FileInputStream(
 				PATH_APPLICATION_PROPERTIES)) {
 			Properties localProperties = null;
 			localProperties = new Properties();
 			localProperties.load(input);
 			properties.putAll(localProperties);
 		} catch (IOException ex) {
 			if (logger.isEnabledFor(Level.DEBUG)) {
 				logger.debug("Failed to load the client properties file", ex);
 			} else {
 				logger.info("Could not load the local client properties file");
 			}
 
 			/*
 			 * It's okay to start without the local properties file.
 			 */
 		}
 		
 		logger.debug("Effective properties: " + properties);
 
 		if (properties.containsKey(PROPERTY_LOCALE_COUNTRY)
 				&& properties.containsKey(PROPERTY_LOCALE_LANGUAGE)) {
 
 			/*
 			 * Changes the application's locale.
 			 */
 			Locale.setDefault(new Locale(properties
 					.getProperty(PROPERTY_LOCALE_LANGUAGE), properties
 					.getProperty(PROPERTY_LOCALE_COUNTRY)));
 
 		} else {
 			logger.debug("Using the default locale");
 		}
 
 		/*
 		 * Changes the application's L&F.
 		 */
 		String ui = properties.getProperty(PROPERTY_UI);
 		try {
 			for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager
 					.getInstalledLookAndFeels()) {
 				if (ui.equalsIgnoreCase(info.getName())) {
 					javax.swing.UIManager.setLookAndFeel(info.getClassName());
 					break;
 				}
 			}
 		} catch (ClassNotFoundException | InstantiationException
 				| IllegalAccessException | UnsupportedLookAndFeelException ex) {
 			logger.error("Failed to change the L&F:", ex);
 		}
 		
 		SecurityContextHolder.setStrategyName(SecurityContextHolder.MODE_GLOBAL);
 		
 		// Loads the client application context
 		context = new ClassPathXmlApplicationContext("META-INF/client.xml");
 
 		logger.info("Started!");
 		launchAndWaitMainFrame();
 		logger.info("Shutting down!");
 		
 		context.close();
 	}
 
 	/**
 	 * Launches and waits for the MainFrame to close.
 	 */
 	private static void launchAndWaitMainFrame() {
 		try {
 			EventQueue.invokeAndWait(new Runnable() {
 				@Override
 				public void run() {
 					MainFrame.getInstance().setVisible(true);
 				}
 			});
 		} catch (InterruptedException | InvocationTargetException ex) {
 			logger.error("Unexpected exception:", ex);
 		}
 
 		synchronized (MainFrame.getInstance()) {
 			while (MainFrame.getInstance().isVisible()) {
 				try {
 					MainFrame.getInstance().wait();
 				} catch (InterruptedException ex) {
 					logger.error("Unexpected exception:", ex);
 				}
 			}
 		}
 	}
 
 	/*
 	 * Environment files.
 	 */
 	private static final String PATH_APPLICATION_PROPERTIES = "etc/application.properties";
 	private static final String PATH_LOGGING_PROPERTIES = "etc/logging.properties";
 	private static final String RESOURCE_DEFAULT_CLIENT_PROPERTIES = "org/key2gym/client/resources/default.properties";
 	private static final String RESOURCE_DEFAULT_LOGGING_PROPERTIES = "org/key2gym/client/resources/default-logging.properties";
 	/*
 	 * Command-line arguments.
 	 */
 	/*
 	 * Application registry properties.
 	 */
 	public static final String PROPERTY_LOCALE_COUNTRY = "locale.country";
 	public static final String PROPERTY_LOCALE_LANGUAGE = "locale.language";
 	public static final String PROPERTY_REFRESH_PERIOD = "refreshPeriod";
 	public static final String PROPERTY_UI = "ui";
 
 	public static Properties getProperties() {
 		return properties;
 	}
 	
 	public static ApplicationContext getContext() {
 		return context;
 	}
 	
	private volatile static AbstractApplicationContext context;
 }
