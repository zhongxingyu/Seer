 package com.ecs.soap.proxy.config;
 
 import java.io.File;
 import java.io.IOException;
 
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.parsers.ParserConfigurationException;
 
 import org.apache.commons.lang.StringUtils;
 import org.apache.log4j.Logger;
 
 public class Configuration {
 
 	private static final Logger logger = Logger.getLogger(Configuration.class);
 	
 	private static String SOAP_PROXY_PATH = "/var/soap-proxy";
 	
	private static final String SOAP_PROXY_CONFIG_PATH = "config";
 
	private static final String SOAP_PROXY_XSD_PATH = "xsd";
 
 	private static final String SOAP_PROXY_URI_MAPPING = "uri-mapping.properties";
 
 	private static final String SOAP_PROXY_SCHEMA_MAPPING = "schema-mapping.properties";
 	
 	private static boolean instanciated = false;
 	
 	public static synchronized void setSoapProxyPath(String path){
 		if(instanciated){
 			throw new IllegalStateException(Configuration.class.getSimpleName() + " singleton has already been instanciated, can't change default SOAP_PROXY_PATH");
 		} else {
 			if(StringUtils.isEmpty(path)){
 				logger.warn("SOAP_PROXY_PATH should not be empty or null, keeping default value (" + SOAP_PROXY_PATH + ")");
 			} else {
 				SOAP_PROXY_PATH = path;
 			}
 		}
 	}
 	
 	private static Configuration instance;
 	
 	public static synchronized Configuration getInstance(){
 		if(instance == null){
 			instance = new Configuration();
 			instanciated = true;
 		}
 		return instance;
 	}
 
 	private File uriMappingFile;
 
 	private File schemaMappingFile;
 
 	private File xsdDir;
 
 	private DocumentBuilder documentBuilder;
 	
 	private Configuration(){
 		File mainDir = new File(SOAP_PROXY_PATH);
 		logger.info("main directory is " + mainDir.getAbsolutePath());
 		if (!mainDir.exists()){
 			logger.info("main directory " + mainDir.getAbsolutePath() + " does not exist, creating it");
 			boolean success = mainDir.mkdirs();
 			if(!success){
 				throw new IllegalStateException("failed to create main directory " + mainDir.getAbsolutePath());
 			}
 		}
 		if (!mainDir.isDirectory() || !mainDir.canRead()) {
 			throw new IllegalStateException(mainDir.getAbsolutePath() + " is not a directory, or is not readable");
 		}
 		logger.info("checking configuration ...");
		File configDir = new File(mainDir, SOAP_PROXY_CONFIG_PATH);
 		if (!configDir.exists()){
 			logger.info("config directory " + configDir.getAbsolutePath() + " does not exist, creating it");
 			boolean success = configDir.mkdirs();
 			if(!success){
 				throw new IllegalStateException("failed to create config directory " + configDir.getAbsolutePath());
 			}
 		}
 		if (!configDir.isDirectory() || !configDir.canRead()) {
 			throw new IllegalStateException(configDir.getAbsolutePath() + " is not a directory, or is not readable");
 		} else {
 			logger.info(configDir.getAbsolutePath() + " directory: ok.");
 		}
 		
 		File uriMappingFile = new File(configDir, SOAP_PROXY_URI_MAPPING);
 		if(!uriMappingFile.exists()){
 			logger.info("uri mapping config file " + uriMappingFile.getAbsolutePath() + " does not exist, creating it");
 			boolean success;
 			try {
 				success = uriMappingFile.createNewFile();
 			} catch (IOException e) {
 				success = false;
 			}
 			if(!success){
 				throw new IllegalStateException("failed to create uri mapping config file " + uriMappingFile.getAbsolutePath());
 			}
 		}
 		if (!uriMappingFile.isFile() || !uriMappingFile.canRead()) {
 			throw new IllegalStateException(uriMappingFile.getAbsolutePath() + " is not a file, or is not readable");
 		} else {
 			this.uriMappingFile = uriMappingFile;
 			logger.info(uriMappingFile.getAbsolutePath() + " directory: ok.");
 		}
 		File schemaMappingFile = new File(configDir, SOAP_PROXY_SCHEMA_MAPPING);
 		if(!schemaMappingFile.exists()){
 			logger.info("schema mapping config file " + schemaMappingFile.getAbsolutePath() + " does not exist, creating it");
 			boolean success;
 			try {
 				success = schemaMappingFile.createNewFile();
 			} catch (IOException e) {
 				success = false;
 			}
 			if(!success){
 				throw new IllegalStateException("failed to create schema mapping config file " + schemaMappingFile.getAbsolutePath());
 			}
 		}
 		if (!schemaMappingFile.isFile() || !schemaMappingFile.canRead()) {
 			throw new IllegalStateException(schemaMappingFile.getAbsolutePath() + " is not a file, or is not readable");
 		} else {
 			this.schemaMappingFile = schemaMappingFile;
 			logger.info(schemaMappingFile.getAbsolutePath() + " directory: ok.");
 		}
		File xsdDir = new File(configDir, SOAP_PROXY_XSD_PATH);
 		if (!xsdDir.exists()){
 			logger.info("xsd directory " + xsdDir.getAbsolutePath() + " does not exist, creating it");
 			boolean success = xsdDir.mkdirs();
 			if(!success){
 				throw new IllegalStateException("failed to create xsd directory " + xsdDir.getAbsolutePath());
 			}
 		}
 		if (!xsdDir.isDirectory() || !xsdDir.canRead()) {
 			throw new IllegalStateException(xsdDir.getAbsolutePath() + " is not a directory, or is not readable");
 		} else {
 			this.xsdDir = xsdDir;
 			logger.info(xsdDir.getAbsolutePath() + " directory: ok.");
 		}
 		logger.info("configuration successfully checked.");
 		logger.info("initializing XML document builder ...");
 		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
 		factory.setNamespaceAware(true);
 		try {
 			this.documentBuilder = factory.newDocumentBuilder();
 		} catch (ParserConfigurationException e) {
 			throw new IllegalStateException("Failed to initialize XML document builder", e);
 		}
 		logger.info("XML document builder successfully initialized.");
 	}
 
 	public File getUriMappingFile() {
 		return uriMappingFile;
 	}
 
 	public void setUriMappingFile(File uriMappingFile) {
 		this.uriMappingFile = uriMappingFile;
 	}
 
 	public File getSchemaMappingFile() {
 		return schemaMappingFile;
 	}
 
 	public void setSchemaMappingFile(File schemaMappingFile) {
 		this.schemaMappingFile = schemaMappingFile;
 	}
 
 	public File getXsdDir() {
 		return xsdDir;
 	}
 
 	public void setXsdDir(File xsdDir) {
 		this.xsdDir = xsdDir;
 	}
 
 	public DocumentBuilder getDocumentBuilder() {
 		return documentBuilder;
 	}
 
 	public void setDocumentBuilder(DocumentBuilder documentBuilder) {
 		this.documentBuilder = documentBuilder;
 	}
 	
 }
