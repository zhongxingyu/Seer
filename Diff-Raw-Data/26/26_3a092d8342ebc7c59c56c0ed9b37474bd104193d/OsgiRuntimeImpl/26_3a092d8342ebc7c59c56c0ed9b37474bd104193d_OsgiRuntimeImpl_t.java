 package com.justcloud.dynamy.kernel.impl;
 
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.nio.channels.FileChannel;
 import java.nio.channels.ReadableByteChannel;
 import java.nio.charset.Charset;
 import java.nio.file.FileSystems;
 import java.nio.file.Files;
 import java.nio.file.OpenOption;
 import java.nio.file.Path;
 import java.nio.file.StandardOpenOption;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.Properties;
 import java.util.ServiceLoader;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import org.osgi.framework.Bundle;
 import org.osgi.framework.launch.Framework;
 import org.osgi.framework.launch.FrameworkFactory;
 
 import com.justcloud.dynamy.kernel.ConfigurationKeys;
 import com.justcloud.dynamy.kernel.DownloadService;
 import com.justcloud.dynamy.kernel.OsgiRuntime;
 
 public class OsgiRuntimeImpl implements OsgiRuntime {
 
 	private Logger logger = Logger.getLogger(OsgiRuntimeImpl.class.getName());
 
 	private Framework framework;
 	private DownloadService downloadService;
 
 	public OsgiRuntimeImpl() {
 		downloadService = new DownloadServiceImpl();
 	}
 
 	@Override
 	public void startup() {
 		logger.info("Looking for Framework in classpath");
 		try {
 			final FrameworkFactory factory = buildFactory();
 			final Map<String, String> configuration = buildConfiguration();
 
 			try {
 				framework = factory.newFramework(buildConfiguration());
 			} catch (Exception e) {
 				logger.log(Level.SEVERE, "Cannot create framework", e);
 				System.exit(-1);
 			}
 			logger.info("Found framework " + framework.getSymbolicName());
 
 			framework.start();
 			if (configuration.containsKey(ConfigurationKeys.INITIAL_URL)) {
 				setup(configuration.get(ConfigurationKeys.INITIAL_URL));
 			}
 		} catch (Exception ex) {
 			throw new RuntimeException(ex);
 		}
 	}
 
 	@Override
 	public void setup(String url) {
 		Path path = FileSystems.getDefault().getPath("storage", "initial");
 
 		try {
 			downloadService.downloadFile(url, path.toString());
 		} catch (IOException e) {
 			throw new RuntimeException("Cannot download " + url);
 		}

 		logger.info("Downloaded initial url successfully");
 
 		try {
 			List<String> lines = Files.readAllLines(path,
 					Charset.forName("UTF-8"));
			for (String line : lines) {
				boolean start = line.endsWith("!start");
				if (start) {
 					line = line.replace("!start", "");
 				}
				logger.info("Installing " + line);
 				Bundle b = framework.getBundleContext().installBundle(line);
 				logger.info("Installed " + b);
				if (start) {
					try {
						b.start();
						logger.info("Started " + b);
					} catch (Exception ex) {
						logger.log(Level.SEVERE, "Cannot start bundle " + b, ex);
					}

 				}

 			}
 		} catch (Exception e) {
 			throw new RuntimeException("Cannot read " + path);
 		}
 
 	}
 
 	private Map<String, String> buildConfiguration()
 			throws FileNotFoundException, IOException {
 		Map<String, String> configuration = new HashMap<>();
 		Properties props = new Properties();
 		Path propertiesPath = FileSystems.getDefault().getPath("conf",
 				"kernel.properties");
 		props.load(new FileInputStream(propertiesPath.toFile()));
 		for (Entry<?, ?> e : props.entrySet()) {
 			if (logger.isLoggable(Level.FINE)) {
 				final String logMsg = String.format(
 						"Setting variable [%s] with value [%s]", e.getKey(),
 						e.getValue());
 				logger.fine(logMsg);
 			}
 			configuration.put((String) e.getKey(), (String) e.getValue());
 
 		}
 		return configuration;
 	}
 
 	@Override
 	public void waitForStop() throws InterruptedException {
 		framework.waitForStop(0);
 	}
 
 	private FrameworkFactory buildFactory() {
 		ServiceLoader<FrameworkFactory> loader = ServiceLoader
 				.load(FrameworkFactory.class);
 		return loader.iterator().next();
 	}
 
 }
