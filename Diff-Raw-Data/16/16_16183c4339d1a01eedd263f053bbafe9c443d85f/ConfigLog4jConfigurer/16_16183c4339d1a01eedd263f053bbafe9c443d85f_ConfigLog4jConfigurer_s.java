 package ru.scf37.config.util;
 
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.nio.charset.Charset;
 
 import org.springframework.util.Log4jConfigurer;
 
 import ru.scf37.config.ConfigException;
 import ru.scf37.config.ConfigReader;
 import ru.scf37.config.ConfigFactory;
 import ru.scf37.config.impl.ConfigUtils;
 /**
  * Configurator for log4j.
  * <p/>
  * This configurator tries to detect current environment via {@link EnvironmentNameResolver}. 
  * It supports both log4j.xml and log4j.properties configuration files.
  * 
  * @author scf37
  * @see EnvironmentNameResolver
  */
 public class ConfigLog4jConfigurer {
 	private EnvironmentNameResolver environmentNameResolver = new EnvironmentNameResolver();
 	
 	/**
 	 * Set custom EnvironmentNameResolver.
 	 * 
 	 * @param environmentNameResolver
 	 */
 	public void setEnvironmentNameResolver(
 			EnvironmentNameResolver environmentNameResolver) {
 		this.environmentNameResolver = environmentNameResolver;
 	}
 
 	/**
 	 * Configure log4j library.
 	 * 
 	 * @param app application name
 	 * @param version application version
 	 */
 	public void configure(String app, String version) {
 		String environment = environmentNameResolver.getEnvironmentName();
 		
 		if (initLog4j(app, version, environment, "log4j.xml")) {
 			return;
 		}
 		
 		if (initLog4j(app, version, environment, "log4j.properties")) {
 			return;
 		}
 		
 		System.err.println("log4j configuration not found");
 	}
 	
 	private boolean initLog4j(String application, String version, String environment, String name) {
		ConfigReader<String> reader = ConfigFactory.readTextFrom("/").build(Charset.forName("UTF-8"));
 		
 		try {
 			String config = reader.read(application, version, environment, name);
 			initLog4j(config, name);
 			
 			System.out.println("Log4j initialized successfully from " + 
 					ConfigUtils.formatAddress(application, version, environment, name));
 			return true;
 		} catch (ConfigException ex) {
 			System.err.println("Unable to resolve environment-specific path for " + 
 					ConfigUtils.formatAddress(application, version, environment, name) + ", error: " + ex.getMessage());
 		} catch (IOException ex) {
 			System.err.println("Unable to resolve environment-specific path for " + 
 					ConfigUtils.formatAddress(application, version, environment, name) + ", error: " + ex.getMessage());
 		}
 		return false;
 	}
 	
 	private void initLog4j(String config, String loc) throws IOException {
 		FileOutputStream fos = null;
 		File tempFile = null;
 		try {
 			tempFile = File.createTempFile("config-", loc);
 			fos = new FileOutputStream(tempFile);
 			fos.write(config.getBytes(Charset.forName("UTF-8")));
 			fos.close();
 			Log4jConfigurer.initLogging(tempFile.getAbsolutePath());
 		} finally  {
 			if (fos != null) {
 				fos.close();
 			}
 			if (tempFile != null) {
 				tempFile.deleteOnExit();
 			}
 		}
 	}
 
 	
 }
