 package com.buschmais.tinkerforge4jenkins.client;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.Collection;
 import java.util.concurrent.Executors;
 import java.util.concurrent.ScheduledExecutorService;
 import java.util.concurrent.TimeUnit;
 
 import javax.xml.XMLConstants;
 import javax.xml.bind.JAXBContext;
 import javax.xml.bind.JAXBElement;
 import javax.xml.bind.JAXBException;
 import javax.xml.bind.Unmarshaller;
 import javax.xml.transform.stream.StreamSource;
 import javax.xml.validation.Schema;
 import javax.xml.validation.SchemaFactory;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.xml.sax.SAXException;
 
 import com.buschmais.tinkerforge4jenkins.core.NotifierDevice;
import com.buschmais.tinkerforge4jenkins.core.NotifierDeviceRegistry;
 import com.buschmais.tinkerforge4jenkins.core.schema.configuration.v1.BrickletConfigurationType;
 import com.buschmais.tinkerforge4jenkins.core.schema.configuration.v1.ConfigurationType;
 import com.buschmais.tinkerforge4jenkins.core.schema.configuration.v1.JenkinsConfigurationType;
 import com.buschmais.tinkerforge4jenkins.core.schema.configuration.v1.ObjectFactory;
 import com.tinkerforge.Device;
 
 /**
  * The main class for the TinkerForge4Jenkins client.
  * <p>
  * At startup it reads the configuration and initializes the
  * {@link NotifierDeviceRegistry}.
  * <p>
  * 
  * @author dirk.mahler
  */
 public final class TinkerForge4JenkinsClient {
 
 	/**
 	 * The default Jenkins URL (e.g. if started using Winstone).
 	 */
 	private static final String JENKINS_DEFAULT_URL = "http://localhost:8080";
 
 	/**
 	 * The default Jenkins update interval in seconds.
 	 */
 	private static final int JENKINS_DEFAULT_UPDATE_INTERVAL = 30;
 
 	/**
 	 * The logger.
 	 */
 	private static final Logger LOGGER = LoggerFactory
 			.getLogger(TinkerForge4JenkinsClient.class);
 
 	/**
 	 * Private constructor.
 	 */
 	private TinkerForge4JenkinsClient() {
 	}
 
 	/**
 	 * The main method.
 	 * 
 	 * @param args
 	 *            The arguments. Currently only one optional argument specifying
 	 *            the name of the configuration file is supported.
 	 */
 	public static void main(String[] args) {
 		LOGGER.info("Starting TinkerForge4Jenkins Client.");
 		String configurationFileName = "tinkerforge4jenkins.xml";
 		if (args.length == 1) {
 			configurationFileName = args[0];
 		}
 		// Read the configuration.
 		ConfigurationType configuration = null;
 		try {
 			configuration = getConfiguration(configurationFileName);
 		} catch (IOException e) {
 			logErrorAndExit("Cannot open configuration file.", e);
 		} catch (JAXBException e) {
 			logErrorAndExit("Cannot read configuration file.", e);
 		} catch (SAXException e) {
 			logErrorAndExit("Cannot read configuration file.", e);
 		}
 		// Initialize the connection to the TinkerForge devices.
 		NotifierDeviceRegistry deviceRegistry = new NotifierDeviceRegistry(
 				configuration.getTinkerforge());
 		Collection<NotifierDevice<? extends Device, ? extends BrickletConfigurationType>> notifiers = null;
 		try {
 			notifiers = deviceRegistry.start();
 		} catch (IOException e) {
 			logErrorAndExit("Cannot connect to devices.", e);
 		}
 		// Schedule Jenkins monitoring.
 		String url = JENKINS_DEFAULT_URL;
 		int updateInterval = JENKINS_DEFAULT_UPDATE_INTERVAL;
 		JenkinsConfigurationType jenkinsConfiguration = configuration
 				.getJenkins();
		url = jenkinsConfiguration.getUrl();
		updateInterval = jenkinsConfiguration.getUpdateInterval();
 		LOGGER.info("Polling '{}' with an interval of {}s.", url,
 				Integer.toString(updateInterval));
 		ScheduledExecutorService scheduledExecutorService = Executors
 				.newScheduledThreadPool(1);
 		scheduledExecutorService.scheduleAtFixedRate(new StatusPublisher(
 				new JenkinsHttpClient(jenkinsConfiguration), notifiers), 0,
 				updateInterval, TimeUnit.SECONDS);
 	}
 
 	/**
 	 * Read the configuration from the given file name.
 	 * 
 	 * @param fileName
 	 *            The file name.
 	 * @return The configuration.
 	 * @throws JAXBException
 	 *             If the configuration file cannot be unmarshalled (e.g.
 	 *             invalid XML).
 	 * @throws FileNotFoundException
 	 *             If the configuration file cannot be read.
 	 * @throws SAXException
 	 *             If the schema definition is invalid.
 	 */
 	private static ConfigurationType getConfiguration(String fileName)
 			throws JAXBException, FileNotFoundException, SAXException {
 		File file = new File(fileName);
 		JAXBContext jaxbContext = JAXBContext.newInstance(ObjectFactory.class);
 		InputStream is = new FileInputStream(file);
 		Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
 		InputStream schemaStream = TinkerForge4JenkinsClient.class
 				.getResourceAsStream("/META-INF/xsd/tf4j_configuration_1_0.xsd");
 		Schema schema = SchemaFactory.newInstance(
 				XMLConstants.W3C_XML_SCHEMA_NS_URI).newSchema(
 				new StreamSource(schemaStream));
 		unmarshaller.setSchema(schema);
 		JAXBElement<ConfigurationType> element = unmarshaller.unmarshal(
 				new StreamSource(is), ConfigurationType.class);
 		return element.getValue();
 	}
 
 	/**
 	 * Log an error and exit.
 	 * 
 	 * @param message
 	 *            The error message.
 	 * @param e
 	 *            The {@link Throwable} to log.
 	 */
 	private static void logErrorAndExit(String message, Throwable e) {
 		LOGGER.error(message, e);
 		System.exit(1);
 	}
 
 }
