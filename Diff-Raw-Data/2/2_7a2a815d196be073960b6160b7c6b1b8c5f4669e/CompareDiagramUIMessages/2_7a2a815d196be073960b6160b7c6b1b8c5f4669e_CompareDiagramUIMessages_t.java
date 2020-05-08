 /*******************************************************************************
  * Copyright (c) 2013 Obeo.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  *     Obeo - initial API and implementation
  *******************************************************************************/
 package org.eclipse.emf.compare.diagram.internal;
 
 import java.text.MessageFormat;
 import java.util.MissingResourceException;
 import java.util.ResourceBundle;
 
 /**
  * Utility class to access externalized Strings for EMF Compare's UI.
  * 
  * @author <a href="mailto:cedric.notot@obeo.fr">Cedric Notot</a>
  */
 public final class CompareDiagramUIMessages {
 	/** Full qualified path to the properties file in which to seek the keys. */
	private static final String BUNDLE_NAME = "org.eclipse.emf.compare.diagram.internal.diagramcompareuimessages"; //$NON-NLS-1$
 
 	/** Contains the locale specific {@link String}s needed by this plug-in. */
 	private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle.getBundle(BUNDLE_NAME);
 
 	/**
 	 * Utility classes don't need to (and shouldn't) be instantiated.
 	 */
 	private CompareDiagramUIMessages() {
 		// prevents instantiation
 	}
 
 	/**
 	 * Returns a specified {@link String} from the resource bundle.
 	 * 
 	 * @param key
 	 *            Key of the String we seek.
 	 * @return The String from the resource bundle associated with <code>key</code>.
 	 */
 	public static String getString(String key) {
 		try {
 			return RESOURCE_BUNDLE.getString(key);
 		} catch (MissingResourceException e) {
 			return '!' + key + '!';
 		}
 	}
 
 	/**
 	 * Returns a String from the resource bundle bound with the given arguments.
 	 * 
 	 * @param key
 	 *            Key of the String we seek.
 	 * @param arguments
 	 *            Arguments for the String formatting.
 	 * @return formatted {@link String}.
 	 * @see MessageFormat#format(String, Object[])
 	 */
 	public static String getString(String key, Object... arguments) {
 		if (arguments == null) {
 			return getString(key);
 		}
 		return MessageFormat.format(getString(key), arguments);
 	}
 }
