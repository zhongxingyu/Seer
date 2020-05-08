 package pl.caltha.cyklotron.helpers;
 
 import java.nio.file.Files;
 import java.nio.file.Path;
 import java.nio.file.Paths;
 
 import org.apache.log4j.Logger;
 
 import pl.caltha.cyklotron.upgrader.UpgraderConfig;
 
 /**
  * TomcatHelper class
  * 
  * @author lukasz
  *
  */
 public class TomcatHelper {
 
 	static final Logger logger = Logger.getLogger(TomcatHelper.class);
 
 	public static final String ROOT_XML_PATH = "/conf/Catalina/localhost/ROOT.xml";
 
 	public static final String ROOT_XML_TEMPLATE_PATH = "files/tomcat/ROOT.xml";
 
 	public static final String WEBAPPS_ROOT_DIR_PATH = "/webapps/ROOT";
 
	private String cyklotronDataLocation;

 	private String cyklotronWarsLocation;
 
 	private String tomcatLocation;
 
 	private Process tomcatProcess;
 
 	public TomcatHelper(UpgraderConfig config) {
		cyklotronDataLocation = config.getCyklotronDataLocation();
 		cyklotronWarsLocation = config.getCyklotronWarsLocation();
		tomcatLocation = config.getTomcatLocation();
 	}
 
 	/**
 	 * Catalina start
 	 */
 	public void start() {
 		try {
 			tomcatProcess = Runtime.getRuntime().exec(
 					tomcatLocation + "/bin/startup.sh");
 			tomcatProcess.waitFor();
 			logger.debug("Catalina start");
 			sleep(3);
 		} catch (Exception e) {
 			logger.error(e.getMessage(), e);
 		}
 	}
 
 	/**
 	 * Catalina start and wait form n-seconds
 	 */
 	public void startAndWait(int seconds) {
 		try {
 			tomcatProcess = Runtime.getRuntime().exec(
 					tomcatLocation + "/bin/startup.sh");
 			tomcatProcess.waitFor();
 			logger.debug("Catalina start");
 			sleep(seconds);
 		} catch (Exception e) {
 			logger.error(e.getMessage(), e);
 		}
 	}
 
 	/**
 	 * Catalina stop
 	 */
 	public void stop() {
 		try {
 			tomcatProcess = Runtime.getRuntime().exec(
 					tomcatLocation + "/bin/shutdown.sh");
 			tomcatProcess.waitFor();
 			logger.debug("Catalina stop");
 			sleep(3);
 		} catch (Exception e) {
 			logger.error(e.getMessage(), e);
 		}
 	}
 
 	/**
 	 * Set ROOT.xml file
 	 * 
 	 * @param warName
 	 */
 	public void setRootXml(String warName, String name, String override) {
 		String rootXMLcontent = ResourceHelper
 				.getResourceFileContent(ROOT_XML_TEMPLATE_PATH);
 		rootXMLcontent = rootXMLcontent
				.replace("${docBase}", cyklotronWarsLocation + "/" + warName)
				.replace("${name}", name)
				.replace("${dataBase}", cyklotronDataLocation)
				.replace("${override}", override);
 		if (rootXMLcontent != null) {
 			ResourceHelper.saveFile(rootXMLcontent, tomcatLocation
 					+ ROOT_XML_PATH);
 		}
 	}
 
 	/**
 	 * Delete WEBAPPS/ROOT directory
 	 */
 	public void deleteWebappRootDir() {
 		Path dir = Paths.get(tomcatLocation + WEBAPPS_ROOT_DIR_PATH);
 		if (dir != null) {
 			if (Files.isWritable(dir)) {
 				ResourceHelper.deleteDir(dir);
 			} else {
 				logger.error("Dir " + dir.toString() + " is not writable");
 			}
 		} else {
 			logger.info("Delete abort. Dir " + tomcatLocation
 					+ WEBAPPS_ROOT_DIR_PATH + " not exist");
 		}
 	}
 
 	/**
 	 * sleep for n seconds
 	 * 
 	 * @param seconds
 	 */
 	private void sleep(int seconds) {
 		try {
 			logger.debug("sleep for " + seconds + " seconds");
 			for (int i = 0; i < seconds; i++) {
 				Thread.sleep(1000);
 			}
 		} catch (InterruptedException e) {
 			logger.error(e.getMessage(), e);
 		}
 	}
 }
