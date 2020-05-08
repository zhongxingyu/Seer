 package ar.edu.it.itba.pdc.proxy;
 
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.net.InetAddress;
 import java.util.Properties;
 
 import org.apache.log4j.BasicConfigurator;
 import org.apache.log4j.Level;
 import org.apache.log4j.Logger;
 
 import ar.edu.it.itba.pdc.proxy.implementations.configurator.implementations.ConfiguratorImpl;
 import ar.edu.it.itba.pdc.proxy.implementations.configurator.interfaces.Configurator;
 import ar.edu.it.itba.pdc.proxy.implementations.monitor.implementations.Monitor;
 import ar.edu.it.itba.pdc.proxy.implementations.proxy.ConnectionManagerImpl;
 import ar.edu.it.itba.pdc.proxy.implementations.proxy.ThreadedSocketServer;
 import ar.edu.it.itba.pdc.proxy.interfaces.ConnectionManager;
 
 public class Start {
 
 	private static Logger proxy;
 
 	public static void main(String args[]) {
//		BasicConfigurator.configure();
 		proxy = Logger.getLogger("START");
 		proxy.setLevel(Level.INFO);
 		ProxyData pd;
 		if ((pd = loadProperties()) != null) {
 			init(pd);
 		}
 	}
 
 	private static ProxyData loadProperties() {
 		Properties proxyProp = new Properties();
 		try {
 			proxy.info("Reading proxy.properties file...");
 			FileInputStream in = new FileInputStream("proxy.properties");
 			proxyProp.load(in);
 			in.close();
 			int configPort = Integer.valueOf(proxyProp
 					.getProperty("configuratorPort"));
 			proxy.info("Configurator port loaded. Listening on port "
 					+ configPort);
 			int serverPort = Integer.valueOf(proxyProp
 					.getProperty("serverPort"));
 			proxy.info("Server port loaded. Listening on port " + serverPort);
 			int monitorPort = Integer.valueOf(proxyProp
 					.getProperty("monitorPort"));
 			proxy.info("Monitor port loaded. Listening on port " + monitorPort);
 			String intermProxyAddr = proxyProp.getProperty("intermProxyAddr");
 			String intermProxyPort = proxyProp.getProperty("intermProxyAddr");
 			if (intermProxyAddr == null || intermProxyAddr.isEmpty()) {
 				proxy.info("No intermediate proxy configuration was found");
 				return new ProxyData(configPort, serverPort, monitorPort,
 						false, null, -1);
 			} else {
 				InetAddress addr = InetAddress.getByName(intermProxyAddr);
 				int port = Integer.valueOf(intermProxyPort);
 				proxy.info("Intermediate proxy configuration found. Address: "
 						+ addr.toString() + " - Port: " + port);
 				return new ProxyData(configPort, serverPort, monitorPort, true,
 						addr, port);
 			}
 		} catch (IOException e) {
 			System.out
 					.println("No se ha podido encontrar el archivo 'proxy.properties'. El proxy no puede funcionar sin el");
 			return null;
 		} catch (NumberFormatException e) {
 			System.out
 					.println("Los puertos ingresados no son validos. Por favor revise el archivo proxy.properties");
 			return null;
 		}
 	}
 
 	private static void init(ProxyData pd) {
 		try {
 
 			proxy.info("Instantiating configurator");
 			Configurator configurator = new ConfiguratorImpl(pd.getConfigPort());
 			proxy.info("Creating configurator thread");
 			Thread configuratorThread = new Thread(configurator);
 			Monitor monitor = new Monitor(pd.getMonitorPort());
 			Thread monitorThread = new Thread(monitor);
 			ConnectionManager cm = new ConnectionManagerImpl(monitor, pd);
 			Thread connManagerThread = new Thread(cm);
 			proxy.info("Instantiating server");
 			ThreadedSocketServer server = new ThreadedSocketServer(
 					pd.getServerPort(), InetAddress.getByName("localhost"),
 					configurator, monitor, cm);
 			proxy.info("Creating server thread");
 			Thread serverThread = new Thread(server);
 
 			proxy.info("Configurator thread starting");
 			monitorThread.start();
 			connManagerThread.start();
 			configuratorThread.start();
 			proxy.info("Server thread starting");
 			serverThread.start();
 			monitorThread.join();
 			connManagerThread.join();
 			configuratorThread.join();
 			serverThread.join();
 		} catch (final Exception e) {
 			System.out.println("Ocurrio un error");
 			e.printStackTrace();
 		}
 	}
 }
