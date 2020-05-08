 /*******************************************************************************
  * Copyright (c) 2001, 2006 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  * IBM Corporation - initial API and implementation
  *******************************************************************************/
 /*
  * Created on Mar 18, 2003
  *
  * To change this generated comment go to 
  * Window>Preferences>Java>Code Generation>Code and Comments
  */
 package org.eclipse.jst.j2ee.webapplication.internal.impl;
 
 import org.eclipse.emf.common.util.URI;
 import org.eclipse.jst.j2ee.common.internal.impl.XMLResourceImpl;
 import org.eclipse.jst.j2ee.internal.J2EEConstants;
 import org.eclipse.jst.j2ee.internal.J2EEVersionConstants;
 import org.eclipse.jst.j2ee.internal.model.translator.webapplication.WebAppTranslator;
 import org.eclipse.jst.j2ee.webapplication.WebApp;
 import org.eclipse.jst.j2ee.webapplication.WebAppResource;
 import org.eclipse.wst.common.internal.emf.resource.Renderer;
 import org.eclipse.wst.common.internal.emf.resource.Translator;
 
 /**
  * @author schacher
  *
  * To change this generated comment go to 
  * Window>Preferences>Java>Code Generation>Code and Comments
  */
 public class WebAppResourceImpl extends XMLResourceImpl implements WebAppResource {
 
 	
 	public WebAppResourceImpl(URI uri, Renderer aRenderer) {
 		super(uri, aRenderer);
 	}
 
 	public WebAppResourceImpl(Renderer aRenderer) {
 		super(aRenderer);
 	}
 
 	
 	public WebApp getWebApp() {
 		return (WebApp) getRootObject();
 	}
 
 	/* (non-Javadoc)
 	 * @see com.ibm.etools.j2eexml.XMLResourceImpl#getJ2EE_1_2_PublicID()
 	 */
 	@Override
 	public String getJ2EE_1_2_PublicID() {
 		return J2EEConstants.WEBAPP_PUBLICID_2_2;
 	}
 
 	/* (non-Javadoc)
 	 * @see com.ibm.etools.j2eexml.XMLResourceImpl#getJ2EE_1_2_SystemID()
 	 */
 	@Override
 	public String getJ2EE_1_2_SystemID() {
 		return J2EEConstants.WEBAPP_SYSTEMID_2_2;
 	}
 	
 	@Override
 	public String getJ2EE_Alt_1_2_SystemID() {
 		return J2EEConstants.WEBAPP_ALT_SYSTEMID_2_2;
 	}
 
 	/* (non-Javadoc)
 	 * @see com.ibm.etools.j2eexml.XMLResourceImpl#getJ2EE_1_3_PublicID()
 	 */
 	@Override
 	public String getJ2EE_1_3_PublicID() {
 		return J2EEConstants.WEBAPP_PUBLICID_2_3;
 	}
 
 	/* (non-Javadoc)
 	 * @see com.ibm.etools.j2eexml.XMLResourceImpl#getJ2EE_1_3_SystemID()
 	 */
 	@Override
 	public String getJ2EE_1_3_SystemID() {
 		return J2EEConstants.WEBAPP_SYSTEMID_2_3;
 	}
 
 	@Override
 	public String getJ2EE_Alt_1_3_SystemID() {
 		return J2EEConstants.WEBAPP_ALT_SYSTEMID_2_3;
 	}
 	/*
 	 * @see WebAppResource#isWeb2_2()
 	 * @deprecated - use getModuleVersionID() and J2EEVersionConstants
 	 */
 	public boolean isWeb2_2() {
 		return getModuleVersionID() == WEB_2_2_ID;
 	}
 	/*
 	 * @see WebAppResource#isWeb2_3()
 	 * @deprecated - use getModuleVersionID() and J2EEVersionConstants
 	 */
 	public boolean isWeb2_3() {
 		return getModuleVersionID() == WEB_2_3_ID;
 	}
 	/*
 	 * @see WebAppResource#isWeb2_4()
 	 * @deprecated - use getModuleVersionID() and J2EEVersionConstants
 	 */
 	public boolean isWeb2_4() {
 		return getModuleVersionID() == WEB_2_4_ID;
 	}
 	
 
 	/* (non-Javadoc)
 	 * @see com.ibm.etools.emf2xml.TranslatorResource#getDoctype()
 	 */
 	public String getDoctype() {
 		switch (getJ2EEVersionID()) {
 			case (J2EE_1_2_ID) :
 			case (J2EE_1_3_ID) :	
 				return J2EEConstants.WEBAPP_DOCTYPE;
 			default :
 				return null;
 		}
 	}
 	
 	/* (non-Javadoc)
 	 * @see com.ibm.etools.j2eexml.XMLResource#getType()
 	 */
 	public int getType() {
 		return WEB_APP_TYPE;
 	}
 
 
 	/* (non-Javadoc)
 	 * @see com.ibm.etools.emf2xml.TranslatorResource#getRootTranslator()
 	 */
 	public Translator getRootTranslator() {
 		return WebAppTranslator.INSTANCE;
 	}
 	/* Return J2EE version based on module version
 	*/
 	@Override
 	public int getJ2EEVersionID() {
 		switch (getModuleVersionID()) {
 			case J2EEVersionConstants.WEB_2_2_ID :
 				return J2EEVersionConstants.J2EE_1_2_ID;
 			case J2EEVersionConstants.WEB_2_3_ID :
 				return J2EEVersionConstants.J2EE_1_3_ID;
 			case J2EEVersionConstants.WEB_2_4_ID :
 				return J2EEVersionConstants.J2EE_1_4_ID;
 			default :
 			return J2EEVersionConstants.J2EE_1_4_ID;
 		}
 	}
 	/* 
 	 * This directly sets the module version id
 	 */
 	@Override
 	public void setModuleVersionID(int id) {
 	super.setVersionID(id);
 	switch (id) {
 			case (WEB_2_4_ID) :
 				super.setDoctypeValues(null, null);
 				break;
 			case (WEB_2_3_ID) :
 				super.setDoctypeValues(getJ2EE_1_3_PublicID(), getJ2EE_1_3_SystemID());
 				break;
 			case (WEB_2_2_ID) :
 				super.setDoctypeValues(getJ2EE_1_2_PublicID(), getJ2EE_1_2_SystemID());
 			
 		}
 		syncVersionOfRootObject();
 	}
 	/*
 	 * Based on the J2EE version, this will set the module version
 	 */
 	@Override
 	public void setJ2EEVersionID(int id) {
 	switch (id) {
 		case (J2EE_1_4_ID) :
 					primSetDoctypeValues(null, null);
 					primSetVersionID(WEB_2_4_ID);
 					break;
 		case (J2EE_1_3_ID) :
 					primSetDoctypeValues(getJ2EE_1_3_PublicID(), getJ2EE_1_3_SystemID());
 					primSetVersionID(WEB_2_3_ID);
 					break;
 		case (J2EE_1_2_ID) :
 					primSetDoctypeValues(getJ2EE_1_2_PublicID(), getJ2EE_1_2_SystemID());
 					primSetVersionID(WEB_2_2_ID);
 			}
 		syncVersionOfRootObject();
 	}
 	/* (non-Javadoc)
 	 * @see com.ibm.etools.emf2xml.impl.TranslatorResourceImpl#getDefaultVersionID()
 	 */
 	@Override
 	protected int getDefaultVersionID() {
 		return WEB_2_4_ID;
 	}
 	
 	/* (non-Javadoc)
 	 * @see org.eclipse.jst.j2ee.internal.common.impl.XMLResourceImpl#syncVersionOfRootObject()
 	 */
 	@Override
 	protected void syncVersionOfRootObject() {
 		WebApp app = getWebApp();
 		if (app == null)
 			return;
 	
 		String version = app.getVersion();
 		String newVersion = getModuleVersionString();
 		if (!newVersion.equals(version))
 			app.setVersion(newVersion);
 	}
 
 }
