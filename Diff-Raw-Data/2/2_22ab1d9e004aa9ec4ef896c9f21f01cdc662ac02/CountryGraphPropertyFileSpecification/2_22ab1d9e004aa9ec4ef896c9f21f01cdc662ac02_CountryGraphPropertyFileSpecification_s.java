 // CountryGraphPropertyFileSpecification.java
 package org.eclipse.stem.internal.data.geography.specifications;
 
 /*******************************************************************************
  * Copyright (c) 2009 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     IBM Corporation - initial API and implementation
  *******************************************************************************/
 
 import java.io.File;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Properties;
 import java.util.StringTokenizer;
 
 import org.eclipse.stem.data.geography.ISOKey;
 import org.eclipse.stem.internal.data.specifications.GraphPropertyFileSpecification;
 
 /**
  * This class represents a {@link Graph} specific to a single country.
  */
 abstract public class CountryGraphPropertyFileSpecification extends
 		GraphPropertyFileSpecification {
 
 	/**
 	 * This is the property in a data file that specifies the administration
 	 * level the data applies to.
 	 */
 	public static final String ADMIN_LEVEL_PROPERTY = "ADMIN_LEVEL"; //$NON-NLS-1$
 
 	/**
 	 * This is the property that specifies the ISO-3166-1 alpha3 key of the
 	 * {@link Graph}'s country.
 	 */
 	public static final String ISOKEY_PROPERTY = "ISOKEY"; //$NON-NLS-1$
 
 	/**
 	 * This is the name of the directory under which country {@link Graph}s are
 	 * serialized.
 	 */
 	public static final String COUNTRY_DIR = "country"; //$NON-NLS-1$
 
 	/**
 	 * This is the ISO-3166 based key for the country associated with the
 	 * {@link Graph}.
 	 */
 	protected ISOKey isoKey = null;
 
 	/**
 	 * This is the UN Administration level of the data for the country.
 	 */
 	protected AdminLevel adminLevel = null;
 
 	/**
 	 */
 	protected CountryGraphPropertyFileSpecification() {
 		super();
 	} // CountryGraphPropertyFileSpecification
 
 	/**
 	 * @return the isoKey of the Country
 	 */
 	public final ISOKey getISOKey() {
 		return isoKey;
 	}
 
 	@Override
 	protected void collectNonDataProperties(final Properties dataSetProperties) {
 		// The call to the super class will collect the Dublin Core properties
 		// if they exist.
 		super.collectNonDataProperties(dataSetProperties);
 
 		adminLevel = new AdminLevel(dataSetProperties
 				.getProperty(ADMIN_LEVEL_PROPERTY));
 
 		isoKey = new ISOKey(dataSetProperties.getProperty(ISOKEY_PROPERTY));
 
 		dataSetProperties.remove(ADMIN_LEVEL_PROPERTY);
 		dataSetProperties.remove(ISOKEY_PROPERTY);
 	} // initialize
 
 	@Override
 	protected String getTargetPluginId() {
 		return org.eclipse.stem.data.geography.Activator.PLUGIN_ID;
 	}
 
 	@Override
 	protected String getRelativeSerializationPath() {
 		final StringBuilder sb = new StringBuilder();
 		sb.append(COUNTRY_DIR);
 		sb.append(File.separatorChar);
 		sb.append(isoKey.toString());
 		return sb.toString();
 	}
 
 	@Override
 	protected String getSerializationFileNameRoot() {
 		final StringBuilder sb = new StringBuilder(isoKey.toString());
 		sb.append("_"); //$NON-NLS-1$
 		sb.append(adminLevel.toString());
 		return sb.toString();
 	}
 
 	/**
 	 * @see org.eclipse.stem.internal.data.specifications.IdentifiableSpecification#createDubinCoreCoverage()
 	 */
 	@Override
 	protected String createDubinCoreCoverage() {
 		final AdminLevelSet adminLevelSet = new AdminLevelSet(adminLevel);
 		final Map<ISOKey, AdminLevelSet> map = new HashMap<ISOKey, AdminLevelSet>();
 		map.put(isoKey, adminLevelSet);
 		return createDublinCoreCoverageString(map);
 	} // createDubinCoreCoverage
 
 
 	/**
 	 * @param map
 	 * @return a string that serializes/summaries the contents of the map
 	 * @see #parseDublinCoreCoverageString(String)
 	 */
 	public static String createDublinCoreCoverageString(
 			final Map<ISOKey, AdminLevelSet> map) {
 		final StringBuilder sb = new StringBuilder();
 		for (ISOKey isoKey : map.keySet()) {
 			final AdminLevelSet adminLevelSet = map.get(isoKey);
 			sb.append(isoKey);
 			sb.append(":"); //$NON-NLS-1$
 			sb.append(adminLevelSet.toString());
 			sb.append("/"); //$NON-NLS-1$
 		} // for each isoKey
 		return sb.toString();
 	} // createDublinCoreCoverageString
 
 	/**
 	 * @param dcCoverageString
 	 * @return the map extracted from the string
 	 * @see #createDublinCoreCoverageString(Map)
 	 */
 	public static Map<ISOKey, AdminLevelSet> parseDublinCoreCoverageString(
 			final String dcCoverageString) {
 		final Map<ISOKey, AdminLevelSet> retValue = new HashMap<ISOKey, AdminLevelSet>();
 
		if (dcCoverageString != null && dcCoverageString != "") { //$NON-NLS-1$
 
 			final StringTokenizer st = new StringTokenizer(dcCoverageString,
 					":/"); //$NON-NLS-1$
 			while (st.hasMoreElements()) {
 				final String isoKeyToken = st.nextToken();
 				final ISOKey isoKey = new ISOKey(isoKeyToken);
 				final String adminSetToken = st.nextToken();
 				final AdminLevelSet adminLevelSet = new AdminLevelSet(
 						adminSetToken);
 				retValue.put(isoKey, adminLevelSet);
 			} // while
 		} // if there is a string
 
 		return retValue;
 	} // createDublinCoreCoverageString
 
 	/**
 	 * @see org.eclipse.stem.internal.data.specifications.IdentifiableSpecification#getTitleDescriptor()
 	 */
 	@Override
 	protected String getTitleDescriptor() {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	/**
 	 * @see java.lang.Object#toString()
 	 */
 	@Override
 	public String toString() {
 		final StringBuilder sb = new StringBuilder(isoKey.toString());
 		sb.append(" "); //$NON-NLS-1$
 		sb.append(adminLevel);
 		return sb.toString();
 	} // toString
 
 } // CountryGraphPropertyFileSpecification
