 /*
  * C5Connector.Java - The Java backend for the filemanager of corefive.
  * It's a bridge between the filemanager and a storage backend and 
  * works like a transparent VFS or proxy.
  * Copyright (C) Thilo Schwarz
  * 
  * == BEGIN LICENSE ==
  * 
  * Licensed under the terms of any of the following licenses at your
  * choice:
  * 
  *  - GNU General Public License Version 2 or later (the "GPL")
  *    http://www.gnu.org/licenses/gpl.html
  * 
  *  - GNU Lesser General Public License Version 2.1 or later (the "LGPL")
  *    http://www.gnu.org/licenses/lgpl.html
  * 
  *  - Mozilla Public License Version 1.1 or later (the "MPL")
  *    http://www.mozilla.org/MPL/MPL-1.1.html
  * 
  * == END LICENSE ==
  */
 package de.thischwa.c5c.resource;
 
 import java.io.BufferedInputStream;
 import java.io.InputStream;
 import java.util.Locale;
 import java.util.Properties;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  * Static object for managing the default and user-defined properties files.<br/>
  * The files are loaded in the following order:
  * <ol>
  * <li>The default properties.</li>
  * <li>The user-defined properties <code>c5connector.properties</code> if present.</li>
  * </ol> 
 * Static wrapper methods are provider for all properties.<br/>
  * Please note: The user-defined properties <em>override</em> the default one.<br/> 
  * Moreover, you can set properties programmatically too ({@link #setProperty(String, String)}), 
  * but make sure to override them <em>before</em> the first call of any property.
  */
 public class PropertiesLoader {
 	
 	private static final Logger logger = LoggerFactory.getLogger(PropertiesLoader.class);
 	
 	private static final String DEFAULT_FILENAME = "default.properties";
 	
 	private static final String LOCAL_PROPERTIES = "/c5connector.properties";
 	
 	private static Properties properties = new Properties();
 
 	static {
 		// 1. load library defaults
 		InputStream inDefault = PropertiesLoader.class.getResourceAsStream(DEFAULT_FILENAME);
 
 		if (inDefault == null) {
 			logger.error("{} not found", DEFAULT_FILENAME);
 			throw new RuntimeException(DEFAULT_FILENAME + " not found");
 		} else {
 			if (!(inDefault instanceof BufferedInputStream))
 				inDefault = new BufferedInputStream(inDefault);
 
 			try {
 				properties.load(inDefault);
 				inDefault.close();
 				logger.info("{} successful loaded", DEFAULT_FILENAME);
 			} catch (Exception e) {
 				String msg = "Error while processing " + DEFAULT_FILENAME;
 				logger.error(msg);
 				throw new RuntimeException(msg, e);
 			}
 		}
 
 		// 2. load user defaults if present
 		InputStream inUser = PropertiesLoader.class.getResourceAsStream(LOCAL_PROPERTIES);
 		if (inUser == null) {
 			logger.info("{} not found", LOCAL_PROPERTIES);
 		} else {
 			if (!(inUser instanceof BufferedInputStream))
 				inUser = new BufferedInputStream(inUser);
 			try {
 				properties.load(inUser);
 				inUser.close();
 				logger.info("{} successful loaded", LOCAL_PROPERTIES);
 			} catch (Exception e) {
 				String msg = "Error while processing " + LOCAL_PROPERTIES;
 				logger.error(msg);
 				throw new RuntimeException(msg, e);
 			}
 		}
 	}
 
 	/**
 	 * Searches for the property with the specified key in this property list.
 	 *
 	 * @param key the key
 	 * @return the property
 	 * @see Properties#getProperty(String)
 	 */
 	public static String getProperty(final String key) {
 		return properties.getProperty(key);
 	}
 
 	/**
 	 * Sets the property with the specified key in this property list.
 	 *
 	 * @param key the key
 	 * @param value the value
 	 * @see Properties#setProperty(String, String)
 	 */
 	public static void setProperty(final String key, final String value) {
 		properties.setProperty(key, value);
 	}
 
 	/**
 	 * Returns <code>default.encoding</code> property.
 	 *
 	 * @return the default encoding
 	 */
 	public static String getDefaultEncoding() {
 		return properties.getProperty("default.encoding");
 	}
 	
 	/**
 	 * Obtains the default locale dependent on the <code>default.language</code> property.
 	 * 
 	 * @return the default locale
 	 */
 	public static Locale getDefaultLocale() {
		return new Locale(properties.getProperty("default.language"));
 	}
 	
 	/**
 	 * Returns <code>ckeditor.toolbar</code> property
 	 *
 	 * @return the editor toolbar
 	 */
 	public static String getEditorToolbar() {
 		return properties.getProperty("ckeditor.toolbar");
 	}
 
 	/**
 	 * Returns <code>ckeditor.width</code> property
 	 *
 	 * @return the editor width
 	 */
 	public static String getEditorWidth() {
 		return properties.getProperty("ckeditor.width");
 	}
 
 	/**
 	 * Returns <code>ckeditor.height</code> property
 	 *
 	 * @return the editor height
 	 */
 	public static String getEditorHeight() {
 		return properties.getProperty("ckeditor.height");
 	}
 	
 	/**
 	 * Returns <code>ckeditor.textarea.cols</code> property
 	 *
 	 * @return the editor textarea cols
 	 */
 	public static String getEditorTextareaCols() {
 		return properties.getProperty("ckeditor.textarea.cols");
 	}
 	
 	/**
 	 * Returns <code>ckeditor.textarea.rows</code> property
 	 *
 	 * @return the editor textarea rows
 	 */
 	public static String getEditorTextareaRows() {
 		return properties.getProperty("ckeditor.textarea.rows");
 	}
 
 	/**
 	 * Returns <code>ckeditor.basePath</code> property
 	 *
 	 * @return the editor base path
 	 */
 	public static String getEditorBasePath() {
 		return properties.getProperty("ckeditor.basePath");
 	}
 
 	/**
 	 * Returns <code>connector.resourceType.archive.extensions.allowed</code> property
 	 *
 	 * @return the archive resource type allowed extensions
 	 */
 	public static String getArchiveResourceTypeAllowedExtensions() {
 		return properties.getProperty("connector.resourceType.archive.extensions.allowed");
 	}
 
 	/**
 	 * Returns <code>connector.resourceType.archive.extensions.denied</code> property
 	 *
 	 * @return the archive resource type denied extensions
 	 */
 	public static String getArchiveResourceTypeDeniedExtensions() {
 		return properties.getProperty("connector.resourceType.archive.extensions.denied");
 	}
 	
 	/**
 	 * Returns <code>connector.resourceType.doc.extensions.allowed</code> property
 	 *
 	 * @return the doc resource type allowed extensions
 	 */
 	public static String getDocResourceTypeAllowedExtensions() {
 		return properties.getProperty("connector.resourceType.doc.extensions.allowed");
 	}
 
 	/**
 	 * Returns <code>connector.resourceType.doc.extensions.denied</code> property
 	 *
 	 * @return the doc resource type denied extensions
 	 */
 	public static String getDocResourceTypeDeniedExtensions() {
 		return properties.getProperty("connector.resourceType.doc.extensions.denied");
 	}
 
 	/**
 	 * Returns <code>connector.resourceType.image.extensions.allowed</code> property
 	 *
 	 * @return the image resource type allowed extensions
 	 */
 	public static String getImageResourceTypeAllowedExtensions() {
 		return properties.getProperty("connector.resourceType.image.extensions.allowed");
 	}
 
 	/**
 	 * Returns <code>connector.resourceType.image.extensions.denied</code> property
 	 *
 	 * @return the image resource type denied extensions
 	 */
 	public static String getImageResourceTypeDeniedExtensions() {
 		return properties.getProperty("connector.resourceType.image.extensions.denied");
 	}
 
 	/**
 	 * Returns <code>connector.resourceType.other.extensions.allowed</code> property
 	 *
 	 * @return the other resource type allowed extensions
 	 */
 	public static String getOtherResourceTypeAllowedExtensions() {
 		return properties.getProperty("connector.resourceType.other.extensions.allowed");
 	}
 
 	/**
 	 * Returns <code>connector.resourceType.other.extensions.denied</code> property
 	 *
 	 * @return the oher resource type denied extensions
 	 */
 	public static String getOherResourceTypeDeniedExtensions() {
 		return properties.getProperty("connector.resourceType.other.extensions.denied");
 	}
 	
 	/**
 	 * Returns <code>connector.filemanagerPath</code> property
 	 *
 	 * @return the filemanger path
 	 */
 	public static String getFilemangerPath() {
 		return properties.getProperty("connector.filemanagerPath");
 	}
 
 	/**
 	 * Returns <code>connector.secureImageUploads</code> property
 	 *
 	 * @return true, if is secure image uploads
 	 */
 	public static boolean isSecureImageUploads() {
 		return Boolean.valueOf(properties.getProperty("connector.secureImageUploads"));
 	}
 
 	/**
 	 * Returns <code>connector.userActionImpl</code> property
 	 *
 	 * @return the user action impl
 	 */
 	public static String getUserActionImpl() {
 		return properties.getProperty("connector.userActionImpl");
 	}
 
 	/**
 	 * Returns <code>connector.iconResolverImpl</code> property
 	 *
 	 * @return the icon resolver impl
 	 */
 	public static String getIconResolverImpl() {
 		return properties.getProperty("connector.iconResolverImpl");
 	}
 
 	/**
 	 * Returns <code>jii.impl</code> property
 	 *
 	 * @return the dimension provider impl
 	 */
 	public static String getDimensionProviderImpl() {
 		return properties.getProperty("jii.impl");
 	}
 
 	/**
 	 * Returns <code>connector.userPathBuilderImpl</code> property
 	 *
 	 * @return the user action impl
 	 */
 	public static String getUserPathBuilderImpl() {
 		return properties.getProperty("connector.userPathBuilderImpl");
 	}
 
 	/**
 	 * Returns <code>connector.forceSingleExtension</code> property
 	 *
 	 * @return true, if is force single extension
 	 */
 	public static boolean isForceSingleExtension() {
 		return Boolean.valueOf(properties.getProperty("connector.forceSingleExtension"));
 	}
 
 	/**
 	 * Gets the connector impl.
 	 *
 	 * @return <code>connector.impl</code> property
 	 */
 	public static String getConnectorImpl() {
 		return properties.getProperty("connector.impl");
 	}
 	
 	/**
 	 * Gets the default capacity.
 	 *
 	 * @return <code>connector.capabilities</code> property
 	 */
 	public static String getDefaultCapacity() {
 		return properties.getProperty("connector.capabilities");
 	} 
 	
 	/**
 	 * Gets the file capability impl.
 	 *
 	 * @return <code>connector.fileCapabilityImpl</code> property
 	 */
 	public static String getFileCapabilityImpl() {
 		return properties.getProperty("connector.fileCapabilityImpl");
 	} 
 	
 	/**
 	 * Returns <code>connector.imagePreview</code> property
 	 *
 	 * @return true, if successful
 	 */
 	public static boolean imageViewingEnabled() {
 		return Boolean.valueOf(properties.getProperty("connector.imagePreview"));
 	}
 }
