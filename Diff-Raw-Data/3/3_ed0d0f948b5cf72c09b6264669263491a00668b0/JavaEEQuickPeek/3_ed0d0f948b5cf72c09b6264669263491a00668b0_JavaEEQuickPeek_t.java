 /*******************************************************************************
  * Copyright (c) 2005, 2007 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     IBM Corporation - initial API and implementation
  *******************************************************************************/
 package org.eclipse.jst.jee.util.internal;
 
 import java.io.IOException;
 import java.io.InputStream;
 
 import org.eclipse.jst.j2ee.internal.J2EEConstants;
 import org.eclipse.jst.j2ee.internal.J2EEVersionConstants;
 import org.xml.sax.InputSource;
 
 /**
  * A Utility for quickly determining the type and version of a Java EE
  * deployment descriptor.
  * 
  * @author jasholl
  * 
  */
 public class JavaEEQuickPeek implements J2EEVersionConstants {
 
 	private static final int UNSET = -2;
 
 	private XMLRootHandler handler = null;
 
 	public JavaEEQuickPeek(int type, int version) {
 		this(type, version, UNSET);
 	}
 
 	public JavaEEQuickPeek(int type, int version, int javaEEVersion) {
 		if (type == UNKNOWN) {
 			throw new RuntimeException("type must not be UNKNOWN");
 		} else if (version == UNKNOWN) {
 			throw new RuntimeException("version must not be UNKNONW");
 		} else if (javaEEVersion == UNKNOWN) {
 			throw new RuntimeException("javaEEVersion must not be UNKNONW");
 		}
 		this.type = type;
 		this.version = version;
 		this.javaEEVersion = javaEEVersion;
 	}
 
 	public JavaEEQuickPeek(InputStream in) {
 		if (in != null) {
 			try {
 				InputSource inputSource = new InputSource(in);
 				handler = new XMLRootHandler();
 				handler.parseContents(inputSource);
 			} catch (Exception ex) {
 				// ignore
 			} finally {
 				try {
 					if (in != null) {
 						in.reset();
 					}
 				} catch (IOException ex) {
 					// ignore
 				}
 			}
 		} else {
 			version = UNKNOWN;
 			javaEEVersion = UNKNOWN;
 			type = UNKNOWN;
 		}
 	}
 
 	private int version = UNSET;
 
 	private int javaEEVersion = UNSET;
 
 	private int type = UNSET;
 
 	/**
 	 * Returns the deployment descriptor type as defined by one of
 	 * {@link J2EEVersionConstants#UNKNOWN},
 	 * {@link J2EEVersionConstants#APPLICATION_CLIENT_TYPE},
 	 * {@link J2EEVersionConstants#APPLICATION_TYPE},
 	 * {@link J2EEVersionConstants#EJB_TYPE},
 	 * {@link J2EEVersionConstants#CONNECTOR_TYPE},
 	 * {@link J2EEVersionConstants#WEB_TYPE}
 	 * 
 	 * @return
 	 */
 	public int getType() {
 		if (type == UNSET) {
 			String rootName = handler.getRootName();
 			if (rootName == null) {
 				type = UNKNOWN;
 			} else if (rootName.equals(J2EEConstants.APP_CLIENT_DOCTYPE)) {
 				type = APPLICATION_CLIENT_TYPE;
 			} else if (rootName.equals(J2EEConstants.APPLICATION_DOCTYPE)) {
 				type = APPLICATION_TYPE;
 			} else if (rootName.equals(J2EEConstants.EJBJAR_DOCTYPE)) {
 				type = EJB_TYPE;
 			} else if (rootName.equals(J2EEConstants.CONNECTOR_DOCTYPE)) {
 				type = CONNECTOR_TYPE;
 			} else if (rootName.equals(J2EEConstants.WEBAPP_DOCTYPE)) {
 				type = WEB_TYPE;
 			} else {
 				type = UNKNOWN;
 			}
 		}
 		return type;
 	}
 
 	public static String normalizeSchemaLocation(String schemaLocation){
		if(schemaLocation == null){
			return null;
		}
 		char [] oldChars = schemaLocation.toCharArray();
 		char [] newChars = new char[oldChars.length];
 		int newCharIndex = 0;
 		boolean onWhiteSpace = true;
 		boolean afterWhiteSpace = false;
 		for(int oldCharIndex=0; oldCharIndex<oldChars.length; oldCharIndex++){
 			afterWhiteSpace = onWhiteSpace;
 			onWhiteSpace = Character.isWhitespace(oldChars[oldCharIndex]);
 			boolean shouldSkip = onWhiteSpace && afterWhiteSpace;
 			if(!shouldSkip){
 				newChars[newCharIndex++] = onWhiteSpace ? ' ' : oldChars[oldCharIndex];
 			}
 			boolean atEnd = ((oldCharIndex + 1) == oldChars.length);
 			if(atEnd && onWhiteSpace){
 				while(newCharIndex > 0  && newChars[newCharIndex-1] == ' '){
 					newCharIndex --;
 				}
 			}
 		}
 		if(newChars != null){
 			return new String(newChars, 0, newCharIndex);
 		} else {
 			return schemaLocation;
 		}
 	}
 	
 	/**
 	 * Returns the module version for this deployment descriptor type. For
 	 * example, if this is a EJB 3.0 deployment descriptor, this returns the
 	 * constant for 3.0 as defined bye {@link J2EEVersionConstants}
 	 * 
 	 * @return
 	 */
 	public int getVersion() {
 		if (version == UNSET) {
 			String publicID = handler.getDtdPublicID();
 			String systemID = handler.getDtdSystemID();
 			String schemaName = null;
 			if (publicID == null || systemID == null) {
 				if (handler.getRootAttributes() != null) {
 					schemaName = normalizeSchemaLocation(handler.getRootAttributes().getValue("xsi:schemaLocation")); //$NON-NLS-1$
 				}
 				if (schemaName == null) {
 					version = UNKNOWN;
 					return version;
 				}
 			}
 			switch (getType()) {
 			case APPLICATION_CLIENT_TYPE:
 				if (publicID != null && systemID != null) {
 					if (publicID.equals(J2EEConstants.APP_CLIENT_PUBLICID_1_3)
 							&& (systemID.equals(J2EEConstants.APP_CLIENT_SYSTEMID_1_3) || systemID.equals(J2EEConstants.APP_CLIENT_ALT_SYSTEMID_1_3))) {
 						version = J2EEVersionConstants.J2EE_1_3_ID;
 					} else if (publicID.equals(J2EEConstants.APP_CLIENT_PUBLICID_1_2)
 							&& (systemID.equals(J2EEConstants.APP_CLIENT_SYSTEMID_1_2) || systemID.equals(J2EEConstants.APP_CLIENT_ALT_SYSTEMID_1_2))) {
 						version = J2EEVersionConstants.J2EE_1_2_ID;
 					}
 				} else if (schemaName != null) {
 					if (schemaName.equals(J2EEConstants.APP_CLIENT_SCHEMA_1_4)) {
 						version = J2EEVersionConstants.J2EE_1_4_ID;
 					} else if (schemaName.equals(J2EEConstants.APP_CLIENT_SCHEMA_5)) {
 						version = J2EEVersionConstants.JEE_5_0_ID;
 					}
 				}
 				break;
 			case APPLICATION_TYPE:
 				if (publicID != null && systemID != null) {
 					if (publicID.equals(J2EEConstants.APPLICATION_PUBLICID_1_3)
 							&& (systemID.equals(J2EEConstants.APPLICATION_SYSTEMID_1_3) || systemID.equals(J2EEConstants.APPLICATION_ALT_SYSTEMID_1_3))) {
 						version = J2EEVersionConstants.J2EE_1_3_ID;
 					} else if (publicID.equals(J2EEConstants.APPLICATION_PUBLICID_1_2)
 							&& (systemID.equals(J2EEConstants.APPLICATION_SYSTEMID_1_2) || systemID.equals(J2EEConstants.APPLICATION_ALT_SYSTEMID_1_2))) {
 						version = J2EEVersionConstants.J2EE_1_2_ID;
 					}
 				} else if (schemaName != null) {
 					if (schemaName.equals(J2EEConstants.APPLICATION_SCHEMA_1_4)) {
 						version = J2EEVersionConstants.J2EE_1_4_ID;
 					} else if (schemaName.equals(J2EEConstants.APPLICATION_SCHEMA_5)) {
 						version = J2EEVersionConstants.JEE_5_0_ID;
 					}
 				}
 				break;
 			case EJB_TYPE:
 				if (publicID != null && systemID != null) {
 					if (publicID.equals(J2EEConstants.EJBJAR_PUBLICID_2_0) && (systemID.equals(J2EEConstants.EJBJAR_SYSTEMID_2_0) || systemID.equals(J2EEConstants.EJBJAR_ALT_SYSTEMID_2_0))) {
 						version = J2EEVersionConstants.EJB_2_0_ID;
 					} else if (publicID.equals(J2EEConstants.EJBJAR_PUBLICID_1_1) && (systemID.equals(J2EEConstants.EJBJAR_SYSTEMID_1_1) || systemID.equals(J2EEConstants.EJBJAR_ALT_SYSTEMID_1_1))) {
 						version = J2EEVersionConstants.EJB_1_1_ID;
 					}
 				} else if (schemaName != null) {
 					if (schemaName.equals(J2EEConstants.EJBJAR_SCHEMA_2_1)) {
 						version = J2EEVersionConstants.EJB_2_1_ID;
 					} else if (schemaName.equals(J2EEConstants.EJBJAR_SCHEMA_3_0)) {
 						version = J2EEVersionConstants.EJB_3_0_ID;
 					}
 				}
 				break;
 			case CONNECTOR_TYPE:
 				if (publicID != null && systemID != null) {
 					if (publicID.equals(J2EEConstants.CONNECTOR_PUBLICID_1_0) && (systemID.equals(J2EEConstants.CONNECTOR_SYSTEMID_1_0) || systemID.equals(J2EEConstants.CONNECTOR_ALT_SYSTEMID_1_0))) {
 						version = J2EEVersionConstants.JCA_1_0_ID;
 					}
 				} else if (schemaName != null) {
 					if (schemaName.equals(J2EEConstants.CONNECTOR_SCHEMA_1_5)) {
 						version = J2EEVersionConstants.JCA_1_5_ID;
 					}
 				}
 				break;
 			case WEB_TYPE:
 				if (publicID != null && systemID != null) {
 					if (publicID.equals(J2EEConstants.WEBAPP_PUBLICID_2_3) && (systemID.equals(J2EEConstants.WEBAPP_SYSTEMID_2_3) || systemID.equals(J2EEConstants.WEBAPP_ALT_SYSTEMID_2_3))) {
 						version = J2EEVersionConstants.WEB_2_3_ID;
 					} else if (publicID.equals(J2EEConstants.WEBAPP_PUBLICID_2_2) && (systemID.equals(J2EEConstants.WEBAPP_SYSTEMID_2_2) || systemID.equals(J2EEConstants.WEBAPP_ALT_SYSTEMID_2_2))) {
 						version = J2EEVersionConstants.WEB_2_2_ID;
 					}
 				} else if (schemaName != null) {
 					if (schemaName.equals(J2EEConstants.WEBAPP_SCHEMA_2_4)) {
 						version = J2EEVersionConstants.WEB_2_4_ID;
 					} else if (schemaName.equals(J2EEConstants.WEBAPP_SCHEMA_2_5)) {
 						version = J2EEVersionConstants.WEB_2_5_ID;
 					}
 				}
 				break;
 			default:
 				break;
 			}
 			if (version == UNSET) {
 				version = UNKNOWN;
 			}
 		}
 		return version;
 	}
 
 	/**
 	 * Maps the version returned from {@link #getVersion()} to the Java EE spec
 	 * version. For example, this is an EJB 3.0 deployment descriptor, this
 	 * returns the constant for 5.0 (which maps to the Java EE spec version of
 	 * 5.0). as defined by {@link J2EEVersionConstants}
 	 * 
 	 * @return
 	 */
 	public int getJavaEEVersion() {
 		if (javaEEVersion == UNSET) {
 			int type = getType();
 			int version = getVersion();
 			switch (type) {
 			case APPLICATION_CLIENT_TYPE:
 			case APPLICATION_TYPE:
 				javaEEVersion = version;
 				break;
 			case EJB_TYPE:
 				switch (version) {
 				case J2EEVersionConstants.EJB_1_1_ID:
 					javaEEVersion = J2EEVersionConstants.J2EE_1_2_ID;
 					break;
 				case J2EEVersionConstants.EJB_2_0_ID:
 					javaEEVersion = J2EEVersionConstants.J2EE_1_3_ID;
 					break;
 				case J2EEVersionConstants.EJB_2_1_ID:
 					javaEEVersion = J2EEVersionConstants.J2EE_1_4_ID;
 					break;
 				case J2EEVersionConstants.EJB_3_0_ID:
 					javaEEVersion = J2EEConstants.JEE_5_0_ID;
 				}
 				break;
 			case CONNECTOR_TYPE:
 				switch (version) {
 				case J2EEVersionConstants.JCA_1_0_ID:
 					javaEEVersion = J2EEConstants.J2EE_1_3_ID;
 					break;
 				case J2EEVersionConstants.JCA_1_5_ID:
 					javaEEVersion = J2EEConstants.J2EE_1_4_ID;
 					break;
 				}
 				break;
 			case WEB_TYPE:
 				switch (version) {
 				case J2EEVersionConstants.WEB_2_2_ID:
 					javaEEVersion = J2EEVersionConstants.J2EE_1_2_ID;
 					break;
 				case J2EEVersionConstants.WEB_2_3_ID:
 					javaEEVersion = J2EEVersionConstants.J2EE_1_3_ID;
 					break;
 				case J2EEVersionConstants.WEB_2_4_ID:
 					javaEEVersion = J2EEVersionConstants.J2EE_1_4_ID;
 					break;
 				case J2EEVersionConstants.WEB_2_5_ID:
 					javaEEVersion = J2EEVersionConstants.JEE_5_0_ID;
 				}
 				break;
 			}
 			if (javaEEVersion == UNSET) {
 				javaEEVersion = UNKNOWN;
 			}
 		}
 		return javaEEVersion;
 	}
 
 	public void setVersion(int version) {
 		this.version = version;
 	}
 
 	public void setJavaEEVersion(int javaEEVersion) {
 		this.javaEEVersion = javaEEVersion;
 	}
 
 	public void setType(int type) {
 		this.type = type;
 	}
 
 }
