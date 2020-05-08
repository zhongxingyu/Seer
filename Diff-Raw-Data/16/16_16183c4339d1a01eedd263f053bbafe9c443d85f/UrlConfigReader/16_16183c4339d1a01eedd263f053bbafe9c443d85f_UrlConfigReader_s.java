 package ru.scf37.config.impl;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.HttpURLConnection;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.net.URLConnection;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import ru.scf37.config.ConfigException;
 import ru.scf37.config.ConfigReader;
 /**
  * Loads data from specified URL
  * supports:
  * - classpath urls i.e. classpath:/a/b/c
  * - java.net.URLs including file: and http:
  * 
  * @author scf37
  *
  */
 public class UrlConfigReader implements ConfigReader<InputStream> {
 	private Logger log = LoggerFactory.getLogger(getClass());
 	private String url;
 	
 	public UrlConfigReader(String url) {
 		if (url == null) {
 			throw new IllegalArgumentException("url cannot be null");
 		}
 		if (url.endsWith("/")) {
 			url = url.substring(0, url.length() - 1);
 		}
 		url = url.replaceAll("\\\\", "/");
 		this.url = url;
 	}
 	
 	@Override
 	public InputStream read(String application, String version, String environment, String name) {
 		String url = appendParameters(application, version, environment, name);
 		
 		return read(url);		
 	}
 
 	private String appendParameters(String application, String version,
 			String environment, String name) {
 		String url = this.url;
 		if (application != null) {
 			url += "/" + application;
 		}
 		if (version != null) {
 			url += "/" + version;
 		}
 		if (environment != null) {
 			url += "/" + environment;
 		}
 		url += "/" + (name == null ? "configuration.properties" : name);
 		return url;
 	}
 
 	private InputStream read(String url) {
 		if (url.startsWith("classpath:")) {
 			return classpathUrl(url.substring("classpath:".length()));
 		} else {
 			try {
 				return url(new URL(url));
 			}
 			catch (MalformedURLException ex) {
 				throw new ConfigException("Invalid url format: '" + url + "'", ex);
 			}
 		}
 	}
 	
 	protected InputStream url(URL u) {
 		URLConnection connection = null;
 		try {
 			connection = u.openConnection();
 			connection.setUseCaches(connection.getClass().getName().startsWith("JNLP"));
 			log.info("Reading {}", u);
 			return connection.getInputStream();
 		}
 		catch (IOException ex) {
 			if (connection instanceof HttpURLConnection) {
 				((HttpURLConnection) connection).disconnect();
 			}
 			return null;
 		}
 	}
 
 	protected InputStream classpathUrl(String location) {
 		if (location.startsWith("/")) {
 			location = location.substring(1);
 		}
 		InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(location);
 		if (is != null) {
 			log.info("Reading classpath:{}", location);
 		}
 		return is;
 	}
 	
 }
