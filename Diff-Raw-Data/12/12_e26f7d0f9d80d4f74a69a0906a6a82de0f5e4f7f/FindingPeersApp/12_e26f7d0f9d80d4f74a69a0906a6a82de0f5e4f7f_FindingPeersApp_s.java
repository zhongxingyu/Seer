 package nl.rug.peerbox;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
 import java.util.Properties;
 import java.util.Scanner;
 
 import nl.rug.peerbox.logic.Peerbox;
 import nl.rug.peerbox.logic.Property;
 
 import org.apache.log4j.BasicConfigurator;
 import org.apache.log4j.Logger;
 
 public class FindingPeersApp {
 
 	private static final String PEERBOX_PROPERTIES_FILE = "peerbox.properties";
 	private static final String DEFAULT_PROPERTIES_FILE = "default.properties";
 	private static final String LOGGER_PROPERTIES_FILE = "logger.properties";
 
 	private static Logger logger = Logger.getLogger(FindingPeersApp.class);
 
 	/**
 	 * @param args
 	 * @throws IOException
 	 * @throws InterruptedException
 	 */
 	public static void main(String[] args) throws IOException,
 			InterruptedException {
 		Thread.currentThread().setName("Main");
 
		BasicConfigurator.configure();
		// PropertyConfigurator.configure(LOGGER_PROPERTIES_FILE);

 		Properties defaultProperties = new Properties();
 		createDefaults(defaultProperties);
 
 		Properties properties = new Properties(defaultProperties);
 		if (new File(PEERBOX_PROPERTIES_FILE).exists()) {
 			try (FileInputStream in = new FileInputStream(
 					PEERBOX_PROPERTIES_FILE)) {
 				properties.load(in);
 			} catch (FileNotFoundException fnfe) {
 				logger.error(fnfe);
 			}
 		}
 
 		String message;
 		Scanner scanner = new Scanner(System.in);
 
 		Peerbox peerbox = new Peerbox(properties);
 		peerbox.join();
 
 		boolean alive = true;
 		do {
 			message = scanner.nextLine();
 			String[] parts = message.split(" ");
 			String command = parts[0];
 			String arg = "";
 			if (parts.length == 2) {
 				arg = parts[1];
 			}
 
 			if ("leave".equals(command)) {
 				peerbox.leave();
 				alive = false;
 				scanner.close();
 			} else if ("threads".equals(command)) {
 				Thread.currentThread().getThreadGroup().list();
 			} else if ("list".equals(command)) {
 				peerbox.listFiles();
 			} else if ("request".equals(command)) {
 				peerbox.requestFiles();
 			} else if ("get".equals(command)) {
 				peerbox.getFile(arg);
 			}
 		} while (alive);
 
 	}
 
 	private static void createDefaults(Properties properties) {
 		String homeDirectory = System.getProperty("user.home");
 		String computerName = System.getProperty("user.name");
 
 		properties.setProperty(Property.PATH, homeDirectory + "/Peerbox");
 		properties.setProperty(Property.MULTICAST_ADDRESS, "239.1.2.4");
 		properties.setProperty(Property.MULTICAST_PORT, "1567");
 		properties.setProperty(Property.SERVER_PORT, "6666");
 		properties.setProperty(Property.NAME, computerName);
 		try (FileOutputStream out = new FileOutputStream(
 				DEFAULT_PROPERTIES_FILE)) {
 			properties.store(out, "");
 		} catch (IOException e) {
 			logger.error(e);
 		}
 	}
 }
