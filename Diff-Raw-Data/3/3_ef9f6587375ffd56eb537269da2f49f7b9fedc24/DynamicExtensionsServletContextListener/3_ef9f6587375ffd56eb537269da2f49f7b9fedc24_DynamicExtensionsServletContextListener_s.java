 
 package edu.common.dynamicextensions.util.listener;
 
 import java.io.File;
 
 import javax.servlet.ServletContextEvent;
 import javax.servlet.ServletContextListener;
 
 import edu.common.dynamicextensions.exception.DynamicExtensionsSystemException;
 import edu.common.dynamicextensions.util.DynamicExtensionsUtility;
 import edu.common.dynamicextensions.util.global.Variables;
 import edu.wustl.common.util.global.ApplicationProperties;
 import edu.wustl.common.util.logger.Logger;
 
 /**
  * 
  * @author sujay_narkar
  * 
  * */
 public class DynamicExtensionsServletContextListener implements ServletContextListener
 {
 
 	/**
 	 * @param sce : Servlet Context Event
 	 */
 	public void contextInitialized(ServletContextEvent sce)
 	{
 		/**
 		 * Getting Application Properties file path
 		 */
 		String applicationResourcesPath = sce.getServletContext().getRealPath("WEB-INF")
 				+ System.getProperty("file.separator") + "classes"
 				+ System.getProperty("file.separator")
 				+ sce.getServletContext().getInitParameter("applicationproperties");
 
 		/**
 		 * Initializing ApplicationProperties with the class 
 		 * corresponding to resource bundle of the application
 		 */
 		ApplicationProperties.initBundle(sce.getServletContext().getInitParameter(
 				"resourcebundleclass"));
 
 		/**
 		 * Getting and storing Home path for the application
 		 */
 		Variables.dynamicExtensionsHome = sce.getServletContext().getRealPath("");
 
 		/**
 		 * Creating Logs Folder inside catissue home
 		 */
 		File logfolder = null;
 		logfolder = new File(Variables.dynamicExtensionsHome + "/Logs");
 		if (!logfolder.exists())
 		{
 			logfolder.mkdir();
 		}
 
 		/**
 		 * setting system property catissue.home which can be utilized 
 		 * by the Logger for creating log file
 		 */
 		System.setProperty("dynamicExtensions.home", Variables.dynamicExtensionsHome + "/Logs");
 
 		/**
 		 * Configuring the Logger class so that it can be utilized by
 		 * the entire application
 		 */
 		Logger.configure(applicationResourcesPath);
 
 		Logger.out.info(ApplicationProperties.getValue("dynamicExtensions.home")
 				+ Variables.dynamicExtensionsHome);
 		Logger.out.info(ApplicationProperties.getValue("logger.conf.filename")
 				+ applicationResourcesPath);
 
 		//QueryBizLogic.initializeQueryData();
 
 		DynamicExtensionsUtility.initialiseApplicationVariables();
 
 		DynamicExtensionsUtility.initialiseApplicationInfo();
 
 		try
 		{
 			DynamicExtensionsUtility.updateDynamicExtensionsCache();
 		}
 		catch (DynamicExtensionsSystemException e)
 		{
			// TODO Auto-generated catch block
 			Logger.out
 					.debug("Exception occured while creating instance of DynamicExtensionsCacheManager");
			e.printStackTrace();
 		}
 	}
 
 	/**
 	 * @param sce Servlet Context Object
 	 */
 	public void contextDestroyed(ServletContextEvent sce)
 	{
 		//	  
 	}
 }
