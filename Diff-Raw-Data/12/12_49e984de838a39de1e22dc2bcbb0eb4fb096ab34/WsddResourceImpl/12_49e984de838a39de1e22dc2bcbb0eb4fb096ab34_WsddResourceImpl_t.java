 /*******************************************************************************
  * Copyright (c) 2001, 2005 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  * IBM Corporation - initial API and implementation
  *******************************************************************************/
 package org.eclipse.jst.j2ee.webservice.wsdd.internal.impl;
 
 
 import java.io.InputStream;
 
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.emf.common.util.URI;
 import org.eclipse.jst.j2ee.common.internal.impl.XMLResourceImpl;
 import org.eclipse.jst.j2ee.internal.J2EEVersionConstants;
 import org.eclipse.jst.j2ee.internal.model.translator.webservices.WsddTranslator;
 import org.eclipse.jst.j2ee.webservice.internal.WebServiceConstants;
 import org.eclipse.jst.j2ee.webservice.wsdd.WebServices;
 import org.eclipse.jst.j2ee.webservice.wsdd.WsddResource;
 import org.eclipse.jst.jee.util.internal.JavaEEQuickPeek;
 import org.eclipse.wst.common.internal.emf.resource.Renderer;
 import org.eclipse.wst.common.internal.emf.resource.Translator;
 import org.eclipse.wst.common.internal.emfworkbench.WorkbenchResourceHelper;
 import org.eclipse.jst.j2ee.core.internal.plugin.J2EECorePlugin;
 
 public class WsddResourceImpl extends XMLResourceImpl implements WsddResource
 {
 	/**
 	 * Constructor for WsddResourceImpl.
 	 */
 	public WsddResourceImpl(URI uri, Renderer aRenderer) {
 		super(uri, aRenderer);
 	}
 
 	/**
 	 * Constructor for WsddResourceImpl.
 	 * @param uri
 	 */
 	public WsddResourceImpl(Renderer aRenderer) {
 		super(aRenderer);
 	}
 
 	/**
 	 * Return the first element in the EList.
 	 */
 	public WebServices getWebServices() {
 		return (WebServices) getRootObject();
 	}
 	
 	/* (non-Javadoc)
 	 * @see com.ibm.etools.emf2xml.TranslatorResource#getRootTranslator()
 	 */
 	public Translator getRootTranslator() {
 		return WsddTranslator.INSTANCE;
 	}
 
 	/**
 	 * @see org.eclipse.jst.j2ee.internal.common.XMLResource#getType()
 	 */
 	public int getType() {
 		return WEB_SERVICE_TYPE;
 	}
 
 	/* (non-Javadoc)
 	 * @see com.ibm.etools.emf2xml.TranslatorResource#getDoctype()
 	 */
 	public String getDoctype() 
 	{
 	  switch (getJ2EEVersionID()) 
 	  {
 	    case (J2EE_1_2_ID) :
 	    case (J2EE_1_3_ID) :	
 	      return WebServiceConstants.WEBSERVICE_DOCTYPE;
 	    default :
     		return null;
     }
 
 
 	}
 
    public boolean isWebService1_0()
    {
       return (getModuleVersionID()==WebServiceConstants.WEBSERVICE_1_0_ID);
    }
 
    public boolean isWebService1_1()
    {
       return (getModuleVersionID()==WebServiceConstants.WEBSERVICE_1_1_ID);  
    }
    public boolean isWebService1_2()
    {
       return (getModuleVersionID()==WebServiceConstants.WEBSERVICE_1_2_ID);  
    }
 
 	/* 
 	 * This directly sets the module version id
 	 */
 	public void setModuleVersionID(int id) {
	//super.setVersionID(id);
 	switch (id) {
 			case (WebServiceConstants.WEBSERVICE_1_2_ID) :
 				super.setDoctypeValues(null, null);
 				primSetVersionID(WebServiceConstants.WEBSERVICE_1_2_ID);
 				break;
 			case (WebServiceConstants.WEBSERVICE_1_1_ID) :
 				super.setDoctypeValues(null, null);
 				primSetVersionID(WebServiceConstants.WEBSERVICE_1_1_ID);
 				break;
 			case (WebServiceConstants.WEBSERVICE_1_0_ID) :
 				super.setDoctypeValues(getJ2EE_1_3_PublicID(), getJ2EE_1_3_SystemID());
 				break;			
 		}
 	}
 	/*
 	 * Based on the J2EE version, this will set the module version
 	 */
 	public void setJ2EEVersionID(int id) 
   {
     switch (id) {
 	    case (JEE_5_0_ID) :
 					primSetDoctypeValues(null, null);
 					primSetVersionID(WebServiceConstants.WEBSERVICE_1_2_ID);
 					break;
     	case (J2EE_1_4_ID) :
     				primSetDoctypeValues(null, null);
     				primSetVersionID(WebServiceConstants.WEBSERVICE_1_1_ID);
     				break;
     	case (J2EE_1_3_ID) :
     				primSetDoctypeValues(getJ2EE_1_3_PublicID(), getJ2EE_1_3_SystemID());
     				primSetVersionID(WebServiceConstants.WEBSERVICE_1_0_ID);
     				break;
     	case (J2EE_1_2_ID) :
     				primSetDoctypeValues(getJ2EE_1_2_PublicID(), getJ2EE_1_2_SystemID());
     				primSetVersionID(WebServiceConstants.WEBSERVICE_1_0_ID);
     		}
 
 	}
 
 	/* (non-Javadoc)
 	 * @see com.ibm.etools.j2eexml.XMLResourceImpl#getJ2EE_1_2_PublicID()
 	 */
 	public String getJ2EE_1_2_PublicID() {
 	  return  WebServiceConstants.WEBSERVICE_PUBLICID;
 	}
 
 	/* (non-Javadoc)
 	 * @see com.ibm.etools.j2eexml.XMLResourceImpl#getJ2EE_1_2_SystemID()
 	 */
 	public String getJ2EE_1_2_SystemID() {
 	  return WebServiceConstants.WEBSERVICE_SYSTEMID;
 	}
 
 	/* (non-Javadoc)
 	 * @see com.ibm.etools.j2eexml.XMLResourceImpl#getJ2EE_1_3_PublicID()
 	 */
 	public String getJ2EE_1_3_PublicID() {
 	  return WebServiceConstants.WEBSERVICE_PUBLICID;
 	}
 
 	/* (non-Javadoc)
 	 * @see com.ibm.etools.j2eexml.XMLResourceImpl#getJ2EE_1_3_SystemID()
 	 */
 	public String getJ2EE_1_3_SystemID() {
     return WebServiceConstants.WEBSERVICE_SYSTEMID;
 	}
 
 	/* Return J2EE version based on module version
 	*/
 	public int getJ2EEVersionID() {
 		switch (getModuleVersionID()) {
 			case WebServiceConstants.WEBSERVICE_1_0_ID:
 				return J2EEVersionConstants.J2EE_1_3_ID;
 			case WebServiceConstants.WEBSERVICE_1_1_ID :
 				return J2EEVersionConstants.J2EE_1_4_ID;
 			case WebServiceConstants.WEBSERVICE_1_2_ID :
 				return J2EEVersionConstants.JEE_5_0_ID;
 			default :
 			return J2EEVersionConstants.JEE_5_0_ID;
 		}
 	}
 
 	/* (non-Javadoc)
 	 * @see com.ibm.etools.emf2xml.impl.TranslatorResourceImpl#getDefaultVersionID()
 	 */
 	protected int getDefaultVersionID() {
 		return WebServiceConstants.WEBSERVICE_1_0_ID;
 	}
 
 
   protected void syncVersionOfRootObject()
   {
     //Do nothing for now since the root object does not have a version.
   }
 
 
 public void setDoctypeValues(String publicId, String systemId) {
 	
 		int myVersion = J2EE_1_4_ID;
 		if (systemId == null) {
 			myVersion = primGetVersionID();
 			setModuleVersionID(myVersion);
 			return;
 		}
 		super.setDoctypeValues(publicId, systemId);
 		
 }
 
 private int primGetVersionID() {
 	IFile afile = WorkbenchResourceHelper.getFile(this);
 	InputStream in = null;
 	JavaEEQuickPeek quickPeek = null;
 	if (afile != null && afile.exists()) {
 		try {
 			in = afile.getContents();
 			quickPeek = new JavaEEQuickPeek(in);
 		}
 		catch (CoreException e) {
 			J2EECorePlugin.logError(e);
 		}
 	return quickPeek.getVersion();
 	}
 	return getModuleVersionID();
 }
 
   
     
 }
 
