 /*
  * Copyright 2012 INRIA
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package com.mymed.controller.core.manager;
 
 import java.lang.reflect.Constructor;
 import java.lang.reflect.Field;
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Method;
 import java.lang.reflect.Modifier;
 import java.net.InetAddress;
 import java.net.UnknownHostException;
 import java.util.Locale;
 import java.util.Map;
 import java.util.Map.Entry;
 
 import ch.qos.logback.classic.Logger;
 
 import com.mymed.controller.core.exception.InternalBackEndException;
 import com.mymed.controller.core.manager.storage.IStorageManager;
 import com.mymed.model.data.AbstractMBean;
 import com.mymed.properties.IProperties;
 import com.mymed.properties.PropType;
 import com.mymed.properties.PropertiesManager;
 import com.mymed.utils.ClassType;
 import com.mymed.utils.MLogger;
 
 /**
  * Abstract manager the all the managers should extend.
  * <p>
  * This manager provides the basic operation to recreate a bean object.
  * 
  * @author lvanni
  * @author Milo Casagrande
  */
 public abstract class AbstractManager {
 	/**
 	 * Default value for the 403 HTTP error code.
 	 */
 	public static final int ERROR_FORBIDDEN = 403;
 
 	/**
 	 * Default value for the 404 HTTP error code.
 	 */
 	public static final int ERROR_NOT_FOUND = 404;
 
 	/**
 	 * Default value for the 409 HTTP error code.
 	 */
 	public static final int ERROR_CONFLICT = 409;
 
 	/**
 	 * The default storage manager.
 	 */
 	protected IStorageManager storageManager;
 
 	/**
 	 * The default logger to be used.
 	 */
 	protected static final Logger LOGGER = MLogger.getLogger();
 
 	/**
 	 * The default properties manager.
 	 */
 	private static final PropertiesManager PROPERTIES = PropertiesManager.getInstance();
 
 	/**
 	 * General properties about mymed.
 	 */
 	protected static final IProperties GENERAL = PROPERTIES.getManager(PropType.GENERAL);
 
 	/**
 	 * Properties values for the columns in the data structure.
 	 */
 	protected static final IProperties COLUMNS = PROPERTIES.getManager(PropType.COLUMNS);
 
 	/**
 	 * Common error strings.
 	 */
 	protected static final IProperties ERRORS = PROPERTIES.getManager(PropType.ERRORS);
 
 	/**
 	 * Fields of the various beans/objects.
 	 */
 	protected static final IProperties FIELDS = PROPERTIES.getManager(PropType.FIELDS);
 
 
 	/**
 	 * This is the URL address of the server, necessary for configuring data across all the backend. If nothing has been
 	 * set at compile time, the default value is an empty string.
 	 */
 	private static final String DEFAULT_SERVER_URI = PROPERTIES.getManager(PropType.GENERAL).get("general.server.uri");
 
 	/**
 	 * The protocol used for communications. This is the default value provided via the properties.
 	 */
 	private static final String DEFAULT_SERVER_PROTOCOL = PROPERTIES.getManager(PropType.GENERAL).get(
 			"general.server.protocol");
 
 	/**
 	 * This is the server URI as to be used in the backend.
 	 */
 	private String SERVER_URI;
 
 	/**
 	 * The protocol used for communications: should be 'http://' or 'https://'.
 	 */
 	private final String SERVER_PROTOCOL;
 
 	/**
 	 * Default error string for encoding exceptions.
 	 */
 	protected static final String ERROR_ENCODING = ERRORS.get("error.encoding");
 
 	/**
 	 * Value of the private modifiers (for introspection).
 	 */
 	private static final int PRIV = Modifier.PRIVATE;
 
 	/**
 	 * Default constructor.
 	 * 
 	 * @param storageManager
 	 */
 	public AbstractManager(final IStorageManager storageManager) {
 		this.storageManager = storageManager;
 
 		// Set a default global URI to be used by the backend, if we cannot get the host name
 		// we fallback to use localhost.
 		if ("".equals(DEFAULT_SERVER_URI)) {
 			try {
 				SERVER_URI = InetAddress.getLocalHost().getCanonicalHostName();
 			} catch (final UnknownHostException ex) {
 				LOGGER.debug("Error retrieving host name of the machine, falling back to 'localhost'", ex);
 				SERVER_URI = "localhost"; // NOPMD
 			}
 		} else {
 			SERVER_URI = DEFAULT_SERVER_URI;
 		}
 
 		// We do not trust what users write on the command line
 		SERVER_PROTOCOL = DEFAULT_SERVER_PROTOCOL.split(":")[0] + "://";
 	}
 
 	/**
 	 * Introspection
 	 * 
 	 * @param clazz
 	 *            the class to introspect
 	 * @param args
 	 *            the values of the class fields
 	 * @return an object of the defined clazz
 	 * @throws InternalBackEndException
 	 */
 	public AbstractMBean introspection(final Class<? extends AbstractMBean> clazz, final Map<byte[], byte[]> args)
 			throws InternalBackEndException {
 
 		AbstractMBean mbean = null;
 		String fieldName = "";
 
 		try {
 			// We create a new instance of the object we are reflecting on
 			final Constructor<?> ctor = clazz.getConstructor();
 			mbean = (AbstractMBean) ctor.newInstance();
 
 			for (final Entry<byte[], byte[]> arg : args.entrySet()) {
 				fieldName = com.mymed.utils.MConverter.byteArrayToString(arg.getKey());
 				Field field;
 				try {
 					field = clazz.getDeclaredField(fieldName);
 
 					/*
 					 * We check the value of the modifiers of the field: if the field is private and final, or private
 					 * static and final, we skip it.
 					 */
 					final int modifiers = field.getModifiers();
 					if (modifiers != PRIV) {
 						continue;
 					}
 
 					final ClassType classType = ClassType.inferType(field.getGenericType());
 					final String setterName = createSetterName(field, classType);
 					final Method method = clazz.getMethod(setterName, classType.getPrimitiveType());
 					final Object argument = ClassType.objectFromClassType(classType, arg.getValue());
 
 					method.invoke(mbean, argument);
 				} catch (NoSuchFieldException e) {
 					LOGGER.info("WARNING: {} is not a bean field", fieldName);
 				}
 			}
 		}  catch (final SecurityException ex) {
 			throw new InternalBackEndException(ex);
 		} catch (final NoSuchMethodException ex) {
 			throw new InternalBackEndException(ex);
		} catch (final IllegalArgumentException ex) {
			throw new InternalBackEndException(ex);
		} catch (final IllegalAccessException ex) {
 			throw new InternalBackEndException(ex);
 		} catch (final InvocationTargetException ex) {
 			throw new InternalBackEndException(ex);
 		} catch (final InstantiationException ex) {
 			throw new InternalBackEndException(ex);
 		}
 
 		return mbean;
 	}
 
 	/**
 	 * Create the name of the setter method based on the field name and its class.
 	 * <p>
 	 * This is particularly useful due to the fact that boolean fields do not have a normal setter name.
 	 * 
 	 * @param field
 	 *            the filed we want the setter method of
 	 * @param classType
 	 *            the class type of the field
 	 * @return the name of the setter method
 	 */
 	private String createSetterName(final Field field, final ClassType classType) {
 		final StringBuilder setterName = new StringBuilder(20);
 		final String fieldName = field.getName();
 
 		setterName.append("set");
 		String subName = fieldName;
 
 		/*
 		 * Check that the boolean field we are on does start with 'is'. This should be the default prefix for boolean
 		 * fields. In this case the setter method will be based on the field name, but without the 'is' prefix.
 		 */
 		if (classType.equals(ClassType.BOOL) && fieldName.startsWith("is")) {
 			subName = fieldName.substring(2, fieldName.length());
 		}
 
 		setterName.append(subName.substring(0, 1).toUpperCase(Locale.US));
 		setterName.append(subName.substring(1));
 		setterName.trimToSize();
 
 		return setterName.toString();
 	}
 
 	/**
 	 * Retrieves the server URI as defined at build time.
 	 * 
 	 * @return the server URI to use
 	 */
 	public String getServerURI() {
 		return SERVER_URI;
 	}
 
 	/**
 	 * Retrieves the server protocol as defined at build time.
 	 * 
 	 * @return the server protocol to use
 	 */
 	public String getServerProtocol() {
 		return SERVER_PROTOCOL;
 	}
 
 }
