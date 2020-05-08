 // GeographicMapper.java
 package org.eclipse.stem.geography;
 
 /*******************************************************************************
  * Copyright (c) 2008 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     IBM Corporation - initial API and implementation
  *******************************************************************************/
 
 import java.io.BufferedInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.util.Map;
 import java.util.Properties;
 import java.util.ResourceBundle;
 
 import org.eclipse.emf.common.util.URI;
 import org.eclipse.emf.ecore.resource.URIConverter;
 import org.eclipse.emf.ecore.resource.impl.URIConverterImpl;
 import org.eclipse.stem.core.Constants;
 
 /**
  * This class maps geographic locations to specific data/information about the
  * location.
  */
 @SuppressWarnings("all")
 abstract public class GeographicMapper {
 
 	/**
 	 * The String that is used internally like an ISO-3166 alpha3 key to
 	 * represent the Earth as if it was a country.
 	 */
 	public static final String EARTH_ALPHA3_ISO_KEY = "ZZZ"; //$NON-NLS-1$
 	/**
 	 * The name of the file that maps ISO-3166 alpha2 keys to ISO-3166 alpha3
 	 * keys.
 	 */
 	public static final String ALPHA2_TO_ALPHA3_NAME = "level0ISOKeys.properties"; //$NON-NLS-1$
 	private static final URI LEVEL_0_ISO_MAPPING_FILE_URI = URI
			.createURI("platform:/plugin/org.eclipse.stem.data.geography/resources/data/country/ZZZ/" //$NON-NLS-1$
 					+ ALPHA2_TO_ALPHA3_NAME);
 	protected static final String RB_PREFIX = Constants.ID_ROOT + ".geography"; //$NON-NLS-1$
 	private static Properties alpha2ToAlpha3Map = null;
 	private static Properties alpha3ToAlpha2Map = null;
 
 	/**
 	 * @param propertyFile
 	 * @return the properties read from the file
 	 */
 	protected static Properties readPropertyFiles(final URI propertyFileURI) {
 		final URIConverter converter = new URIConverterImpl();
 
 		final Properties retValue = new Properties();
 		BufferedInputStream propertiesInputStream = null;
 		try {
 			propertiesInputStream = new BufferedInputStream(converter
 					.createInputStream(propertyFileURI));
 			retValue.load(propertiesInputStream);
 			propertiesInputStream.close();
 		} catch (final FileNotFoundException e) {
 			Activator.logError(EARTH_ALPHA3_ISO_KEY, e);
 		} catch (final IOException e) {
 			Activator.logError(EARTH_ALPHA3_ISO_KEY, e);
 		}
 		return retValue;
 	} // readPropertyFiles
 
 	/**
 	 * @param key
 	 * @return the level of the key
 	 */
 	protected static int keyLevel(final String key) {
 		int level = 0;
 		int start = 0;
 		for (int temp = key.indexOf("-"); temp > 0;) { //$NON-NLS-1$
 			level++;
 			start += temp + 1;
 			temp = key.substring(start).indexOf("-"); //$NON-NLS-1$
 		} // for
 		return level;
 	} // keyLevel
 
 	/**
 	 * 
 	 */
 	protected static Properties getAlpha2ToAlpha3Map() {
 		// Got the alpha2->alpha3 map?
 		if (alpha2ToAlpha3Map == null) {
 			// No
 			alpha2ToAlpha3Map = readPropertyFiles(LEVEL_0_ISO_MAPPING_FILE_URI);
 		} // if
 		// Got it now?
 		if (alpha2ToAlpha3Map == null) {
 			// No
 			Activator.logError(
 					"Can't read the alpha2 to alpha3 mapping file \"" //$NON-NLS-1$
 							+ ALPHA2_TO_ALPHA3_NAME + "\"", null); //$NON-NLS-1$
 		} // if
 		return alpha2ToAlpha3Map;
 	} // getAlpha2ToAlpha3Map
 
 	/**
 	 * @param alpha2ISOKey
 	 * @return the alpha 3 ISO key of the alpha2 key, or the alpha 2 key if
 	 *         there is an error
 	 */
 	public static String getAlpha3(final String alpha2ISOKey) {
 		String retValue = alpha2ISOKey;
 		final Properties alpha2ToAlpha3Map = getAlpha2ToAlpha3Map();
 		// Do we have the map?
 		if (alpha2ToAlpha3Map != null) {
 			// Yes
 			retValue = alpha2ToAlpha3Map.getProperty(alpha2ISOKey);
 		} // if
 		return retValue;
 	} // getAlpha3
 
 	/**
 	 * @param alpha3ISOKey
 	 * @return the alpha 2 ISO key of the alpha2 key, or the alpha 2 key if
 	 *         there is an error
 	 */
 	public static String getAlpha2(final String alpha3ISOKey) {
 		String retValue = alpha3ISOKey;
 
 		final Properties alpha3toAlpha2Map = getAlpha3toAlpha2Map();
 		// Do we have the map?
 		if (alpha3toAlpha2Map != null) {
 			// Yes
 			retValue = alpha3toAlpha2Map.getProperty(alpha3ISOKey);
 		} // if
 		return retValue;
 	}
 
 	private static Properties getAlpha3toAlpha2Map() {
 		// Already created the map?
 		if (alpha3ToAlpha2Map == null) {
 			// No
 			final Properties alpha2ToAlpha3Map = getAlpha2ToAlpha3Map();
 			// Do we have the reverse map?
 			if (alpha2ToAlpha3Map != null) {
 				// Yes
 				alpha3ToAlpha2Map = new Properties();
 				for (final Object iso2Key : alpha2ToAlpha3Map.keySet()) {
 					alpha3ToAlpha2Map.put(alpha2ToAlpha3Map.get(iso2Key),
 							iso2Key);
 				} // for each String
 			} // if
 		} // if
 
 		return alpha3ToAlpha2Map;
 	}
 
 	/**
 	 * @param alpha3ISOKey
 	 *            the ISO-3166 alpha-3 key
 	 * @param bundlePrefix
 	 *            TODO
 	 * @param bundleType
 	 *            a {@link String} that identifies the type of bundle
 	 * @return the name of the bundle that contains the names for the iso key's
 	 *         country.
 	 */
 	protected static String makeBundleName(final String alpha3ISOKey,
 			final String bundlePrefix, final String bundleType) {
 		final StringBuilder sb = new StringBuilder(bundlePrefix);
 		sb.append("."); //$NON-NLS-1$
 		sb.append(alpha3ISOKey);
 		sb.append("_"); //$NON-NLS-1$
 		sb.append(bundleType);
 		return sb.toString();
 	} // makeBundleName
 
 	/**
 	 * @param alpha2ISOKey
 	 *            the ISO-3166 alpha-3 key
 	 * @param bundlePrefix
 	 *            TODO
 	 * @param bundleType
 	 *            a {@link String} that identifies the type of bundle
 	 * @return the {@link ResourceBundle} for the country identified by the
 	 *         ISO-3166 alpha2 key
 	 */
 	protected static ResourceBundle readResourceBundle(
 			final String alpha2ISOKey, final String bundlePrefix,
 			final String bundleType) {
 		ResourceBundle retValue = null;
 		final Properties alpha2ToAlpha3Map = getAlpha2ToAlpha3Map();
 		// Got it?
 		if (alpha2ToAlpha3Map != null) {
 			// Yes
 			final String alpha3ISOKey = alpha2ToAlpha3Map
 					.getProperty(alpha2ISOKey);
 			retValue = ResourceBundle.getBundle(makeBundleName(alpha3ISOKey,
 					bundlePrefix, bundleType));
 		} // if
 
 		return retValue;
 	} // readResourceBundle
 
 	/**
 	 * @param isoKey
 	 * @param bundlePrefix
 	 *            TODO
 	 * @param bundleType
 	 *            a {@link String} that identifies the type of bundle
 	 * @param level0RBName
 	 *            the name of the {@link ResourceBundle} file that contains the
 	 *            level 0 resources
 	 * @param level0RB
 	 *            the reference of the level 0 {@link ResourceBundle}
 	 * @param rbMap
 	 *            a {@link Map} between alpha-2 iso key and
 	 *            {@link ResourceBundle}s
 	 * @return the {@link ResourceBundle} that contains the mapping
 	 */
 	protected static ResourceBundle getResourceBundle(final String isoKey,
 			final int level, final String bundlePrefix,
 			final String bundleType, final String level0RBName,
 			ResourceBundle level0RB, Map<String, ResourceBundle> rbMap) {
 		ResourceBundle retValue = null;
 		// If the level is 0, we handle things specially. There is a single
 		// resource bundle that contains all of the level 0 mappings (i.e., the
 		// names of all countries) This allows us to avoid reading in all of the
 		// other (245!) resource bundles just to obtain the names of the
 		// countries. For the other levels we retrieve the bundle specific to
 		// the country because it has the names for all levels (i.e., all ISO
 		// keys).
 
 		// Level 0?
 		if (level == -1 || level == 0) {
 			// Yes
 			// Have we retrieved the level 0 resource bundle yet?
 			if (level0RB == null) {
 				// No
 				level0RB = ResourceBundle.getBundle(level0RBName);
 			} // if
 			retValue = level0RB;
 		} // if level 0
 		else {
 			// No
 			final String alpha2ISOKey = isoKey.substring(0, 2);
 
 			// We use that value as the key to "cache" the resource bundle
 			retValue = rbMap.get(alpha2ISOKey);
 
 			// Have we retrieved this ResourceBundle before?
 			if (retValue == null) {
 				// No
 				retValue = readResourceBundle(alpha2ISOKey, bundlePrefix,
 						bundleType);
 				// Were we successful?
 				if (retValue == null) {
 					// No
 					Activator.logError(
 							"Could not read Resource Bundle of type " //$NON-NLS-1$
 									+ bundleType + " for \"" + alpha2ISOKey //$NON-NLS-1$
 									+ "\"", null); //$NON-NLS-1$
 				} // if
 				else {
 					// No
 					rbMap.put(alpha2ISOKey, retValue);
 				} // else
 			} // if
 		} // else
 		return retValue;
 	} // getResourceBundle
 
 } // GeographicMapper
