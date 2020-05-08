 package de.zib.gndms.kit.config;
 
 import de.zib.gndms.kit.logging.LDPHolder;
 import de.zib.gndms.kit.logging.LoggingDecisionPoint;
 import org.apache.log4j.Logger;
 import org.jetbrains.annotations.NotNull;
 import org.jetbrains.annotations.Nullable;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.util.Collections;
 import java.util.Map;
 import java.util.Properties;
 
 /**
  * Provides a stream of config/properties objects as Map<Object,Object>'s
  *
  * tryNextElement() will only get you a new element if either there was no config file in the
  * first place (than a default one is written to disk) or if it has changed since the last time
  * it was read.
  *
  * @author Stefan Plantikow<plantikow@zib.de>
  * @version $Id$
  *
  *          User: stepn Date: 17.07.2008 Time: 23:05:39
  */
 public class PropertiesFromFile extends InfiniteEnumeration<Map<Object, Object>>
 implements LoggingDecisionPoint, LDPHolder {
 	@NotNull
 	private final File configFile;
 
 	@Nullable
 	private Map<Object, Object> current;
 
 	@NotNull
 	private final String desciptiveName;
 
 	@NotNull
 	private final String defaultsComment;
 
 	@NotNull
 	private final Properties defaults;
 	private Logger logger;
 
 	@Nullable
 	private LoggingDecisionPoint ldp;
 
 	public PropertiesFromFile(@NotNull File theConfigFile, @NotNull String aDescriptiveName,
 	                          @NotNull Properties theDefaults, @NotNull String aDefaultComment,
 							  @Nullable Logger aLogger) {
 		super();
 		configFile = theConfigFile;
 		desciptiveName = aDescriptiveName;
 		defaultsComment = aDefaultComment;
 		defaults = theDefaults;
 		logger = aLogger;
 	}
 
 
 	@Override
 	synchronized Map<Object, Object> tryNextElement() {
 		try {
 			return tryLoadProperties();
 		}
 		catch (IOException e) {
 			logger.warn("Ignoring failure while attempting to load " + desciptiveName, e);
 			return null;
 		}
 	}
 
 	@Override
 	@NotNull
 	synchronized Map<Object, Object> createInitialDefaultElement() {
 		try {
 			return tryLoadProperties();
 		}
 		catch (IOException e) {
 			logger.warn("Failure while attempting to initialize " + desciptiveName, e);
 			throw new RuntimeException(e);
 		}
 	}
 
 	@NotNull
 	private Map<Object, Object> tryLoadProperties() throws IOException {
 		Properties props;
 		FileInputStream in = null;
 		if (shouldLog("load"))
 			logger.debug("Loading " + desciptiveName);
 		try {
			if (!configFile.exists())
 				return createDefaultElement();
 			else {
 				in = new FileInputStream(configFile);
 				props = new Properties();
 				props.load(in);
 				return props;
 			}
 		}
 		finally {
 			if (in != null)
 				try {
 					in.close();
 				}
 				catch (IOException e) {
 					// intentionally left empty
 				}
 		}
 	}
 
 	@NotNull
 	private Map<Object, Object> createDefaultElement() throws IOException {
 		tryWriteDefaults();
 		return Collections.unmodifiableMap(defaults);
 	}
 
 	private  void tryWriteDefaults() throws IOException {
 		if (shouldLog("newdefaults"))
 			logger.info("Creating default " + desciptiveName);
 		FileOutputStream out = null;
 		try {
 			out = new FileOutputStream(configFile);
 			defaults.store(out, defaultsComment);
 		}
 		catch (IOException e) {
 			logger.warn("Failed creating default " + desciptiveName);
 			throw e;
 		}
 		finally {
 			if (out != null)
 				try {
 					out.close();
 				}
 				catch (IOException e) {
 					// skip this one
 				}
 		}
 	}
 
 	@Nullable
 	public synchronized LoggingDecisionPoint getLDP() {
 		return ldp;
 	}
 
 	public synchronized void setLDP(@Nullable LoggingDecisionPoint newLDP) {
 		ldp = newLDP;
 	}
 
 	public synchronized boolean shouldLog(@NotNull String token) {
 		return ldp == null || ldp.shouldLog(token);
 	}
 }
