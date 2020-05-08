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
 package org.eclipse.jst.j2ee.common.internal.impl;
 
 import java.io.IOException;
 import java.math.BigDecimal;
 import java.util.Map;
 
 import org.eclipse.core.internal.resources.Workspace;
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.emf.common.notify.Notification;
 import org.eclipse.emf.common.notify.NotificationChain;
 import org.eclipse.emf.common.notify.Notifier;
 import org.eclipse.emf.common.notify.impl.AdapterImpl;
 import org.eclipse.emf.common.util.URI;
 import org.eclipse.emf.ecore.resource.ResourceSet;
 import org.eclipse.jst.j2ee.application.Application;
 import org.eclipse.jst.j2ee.internal.J2EEVersionConstants;
 import org.eclipse.jst.j2ee.internal.common.J2EEVersionResource;
 import org.eclipse.jst.j2ee.internal.common.XMLResource;
 import org.eclipse.jst.j2ee.internal.xml.J2EEXmlDtDEntityResolver;
 import org.eclipse.wst.common.internal.emf.resource.Renderer;
 import org.eclipse.wst.common.internal.emf.resource.TranslatorResource;
 import org.eclipse.wst.common.internal.emf.resource.TranslatorResourceImpl;
 import org.eclipse.wst.common.internal.emfworkbench.WorkbenchResourceHelper;
 import org.xml.sax.EntityResolver;
 
 
 public abstract class XMLResourceImpl extends TranslatorResourceImpl implements XMLResource,J2EEVersionResource {
 	/** Indicator to determine if this resource was loaded as an alt-dd (from an ear),
 	  * default is false */
 	protected boolean isAlt = false;
 	/** The application which declared the alt-dd for this resource; exists only if this resource is and
 	  * alt dd */
 	protected Application application;
 	protected boolean isNew = true;
 	private Boolean needsSync = new Boolean(true);
 	
 	
 	private static class RootVersionAdapter extends AdapterImpl {
 		/* (non-Javadoc)
 		 * @see org.eclipse.emf.common.notify.impl.AdapterImpl#isAdapterForType(java.lang.Object)
 		 */
 		@Override
 		public boolean isAdapterForType(Object type) {
 			return super.isAdapterForType(type);
 		}
 		
 		/* (non-Javadoc)
 		 * @see org.eclipse.emf.common.notify.impl.AdapterImpl#notifyChanged(org.eclipse.emf.common.notify.Notification)
 		 */
 		@Override
 		public void notifyChanged(Notification msg) {
 			if (msg.getFeatureID(null) == RESOURCE__CONTENTS &&
 				msg.getEventType() == Notification.ADD) {
 					((XMLResourceImpl)msg.getNotifier()).syncVersionOfRootObject();
 					((Notifier)msg.getNotifier()).eAdapters().remove(this);
 				}
 		}
 		
 		
 
 	
 	}
 	
 	/**
 	 * @deprecated since 4/29/2003 - used for compatibility
 	 * Subclasses should be using the Renderers and translator framework 
 	 */
 	public XMLResourceImpl() {
 		super();
 	}
 
 	/**
 	 * @deprecated since 4/29/2003 - used for compatibility
 	 * Subclasses should be using the Renderers and translator framework 
 	 */
 	public XMLResourceImpl(URI uri) {
 		super(uri);
 	}
 	
 	public XMLResourceImpl(URI uri, Renderer aRenderer) {
 		super(uri, aRenderer);
 	}
 
 	public XMLResourceImpl(Renderer aRenderer) {
 		super(aRenderer);
 	}
 	
 	/* (non-Javadoc)
 	 * @see com.ibm.etools.emf2xml.impl.TranslatorResourceImpl#initializeContents()
 	 */
 	@Override
 	protected void initializeContents() {
 		super.initializeContents();
 		eAdapters().add(new RootVersionAdapter());
 	}
 
 	
 	/**
 	 * Is this a resource loaded as an alternate deployment descriptor?
 	 */
 	public boolean isAlt() {
 		return isAlt;
 	}
 	
 	
 	public void setApplication(Application newApplication) {
 		application = newApplication;
 	}
 	/**
 	 * Is this a resource loaded as an alternate deployment descriptor?
 	 */
 	public void setIsAlt(boolean isAlternateDD) {
 		isAlt = isAlternateDD;
 	}
 	
 	/* (non-Javadoc)
 	 * @see com.ibm.etools.emf2xml.impl.TranslatorResourceImpl#getDefaultVersionID()
 	 */
 	@Override
 	protected int getDefaultVersionID() {
 		return J2EE_1_4_ID;
 	}
 	
 	/* (non-Javadoc)
 	 * @see com.ibm.etools.emf2xml.TranslatorResource#setDoctypeValues(java.lang.String, java.lang.String)
 	 * This is setting the module version on the resource, where values are different that the J2EE version, this will be overridden
 	 */
 	@Override
 	public void setDoctypeValues(String publicId, String systemId) {
 		int version = J2EE_1_4_ID;
 		if (systemId == null) 
 			version = J2EE_1_4_ID;
 		else if (systemId.equals(getJ2EE_1_3_SystemID()) || systemId.equals(getJ2EE_Alt_1_3_SystemID()))
 			version = J2EE_1_3_ID;
 		else if (systemId.equals(getJ2EE_1_2_SystemID()) || systemId.equals(getJ2EE_Alt_1_2_SystemID()))
 			version = J2EE_1_2_ID;
 		super.setDoctypeValues(publicId, systemId);
 		//Only set if versionID not set if version is 14
 		if ((version != J2EE_1_4_ID) || (version == J2EE_1_4_ID && getModuleVersionID() == 0))
 			setJ2EEVersionID(version);
 	}
 
 
 	/* (non-Javadoc)
 	 * @see com.ibm.etools.emf2xml.TranslatorResource#usesDTD()
 	 */
 	@Override
 	public boolean usesDTD() {
 		return (getVersionID() == J2EE_1_2_ID) || (getVersionID() == J2EE_1_3_ID);
 	}
 	
 	/* (non-Javadoc)
 	 * @see com.ibm.etools.emf2xml.TranslatorResource#setVersionID(int)
 	 * @deprecated, Use setJ2EEVersionID() to set module version based on j2ee version
 	 **/
 	@Override
 	public void setVersionID(int id) {
 		setJ2EEVersionID(id);
 	}
 	protected void primSetVersionID(int id) {
 		super.setVersionID(id);
 	}
 	protected void primSetDoctypeValues(String aPublicId, String aSystemId) {
 		super.setDoctypeValues(aPublicId,aSystemId);
 	}
 	/*
 	 * Sets the module version based on the J2EE version
 	 */
 	public abstract void setJ2EEVersionID(int id);
 	/*
 	 * Sets the module version directly
 	 * */
 	 public abstract void setModuleVersionID(int id);
 	/**
 	 * @deprecated 
 	 * (non-Javadoc)
 	 * @see org.eclipse.jst.j2ee.internal.XMLResource#isJ2EE1_3()
 	 */
 	public boolean isJ2EE1_3() {
 		return getVersionID() == J2EE_1_3_ID;
 	}
 	
 	/**
 	 * @deprecated use {@link TranslatorResource#setVersionID(int)}, 
 	 * {@link TranslatorResource#setDoctypeValues(String, String)}
 	 * Sets the system id of the XML document.
 	 * @see J2EEVersionConstants
 	 */
 	public void setPublicId(String id) {
 		setDoctypeValues(id, getSystemId());
 	}
 	
 	/**
 	 * @deprecated use {@link TranslatorResource#setVersionID(int)},
 	 * {@link TranslatorResource#setDoctypeValues(String, String)}
 	 * Sets the public id of the XML document.
 	 * @see J2EEVersionConstants
 	 */
 	public void setSystemId(String id) {
 		setDoctypeValues(getPublicId(), id);
 	}
 	@Override
 	protected String getDefaultPublicId() {
 		switch (getVersionID()) {
 			case (J2EE_1_2_ID) :
 				return getJ2EE_1_2_PublicID();
 			case (J2EE_1_3_ID) :	
 				return getJ2EE_1_3_PublicID();
 			default :
 				return null;
 		}	
 	}
 
 	@Override
 	protected String getDefaultSystemId() {
 		switch (getVersionID()) {
 			case (J2EE_1_2_ID) :
 				return getJ2EE_1_2_SystemID();
 			case (J2EE_1_3_ID) :	
 				return getJ2EE_1_3_SystemID();
 			default :
 				return null;
 		}
 	}
 	
 	public abstract String getJ2EE_1_2_PublicID();
 	
 	public abstract String getJ2EE_1_2_SystemID();
 	
 	/**
 	 * By default just return the proper 1.2 system ID, subclasses may override
 	 * @return alternate string for system ID
 	 */
 	public String getJ2EE_Alt_1_2_SystemID() {
 		return getJ2EE_1_2_SystemID();
 	}
 	
 	public abstract String getJ2EE_1_3_PublicID();
 	
 	public abstract String getJ2EE_1_3_SystemID();
 	
 	/**
 	 * By default just return the proper 1.3 system ID, subclasses may override
 	 * @return alternate string for system ID
 	 */
 	public String getJ2EE_Alt_1_3_SystemID() {
 		return getJ2EE_1_3_SystemID();
 	}
 	
 	
 	@Override
 	public NotificationChain basicSetResourceSet(ResourceSet aResourceSet, NotificationChain notifications) {
 		if (aResourceSet == null && this.resourceSet != null)
 			preDelete();
 		return super.basicSetResourceSet(aResourceSet, notifications);
 	}
 		
 	public Application getApplication() {
 		return application;
 	}
 	
 	/**
 	 * @deprecated - use getJ2EEVersionID() and getModuleVersionID()
 	 */
 	@Override
 	public int getVersionID() {
 		return getJ2EEVersionID();
 	}
 	
 	@Override
 	public EntityResolver getEntityResolver() {
 		return J2EEXmlDtDEntityResolver.INSTANCE;
 	}	
 	/* All subclasses will derive this value based on their module version
 	 */
 	public abstract int getJ2EEVersionID();
 
 	
 
 	/* This will be computed during loads of the resource
 	 */
 	public int getModuleVersionID() {
 		return super.getVersionID();
 	}
 	
 	protected abstract void syncVersionOfRootObject();
 	
 	protected String getModuleVersionString() {
 		
 		int ver = getModuleVersionID();
 		return new BigDecimal(String.valueOf(ver)).movePointLeft(1).toString();
 	}
 
 	@Override
 	public void loadExisting(Map options) throws IOException {
 		boolean localNeedsSync = false;
 		synchronized (needsSync) {
 			localNeedsSync = needsSync;
 		}
 		if (localNeedsSync) { // Only check sync once for life of this model
 			IFile file = WorkbenchResourceHelper.getFile(this);
 			if (!file.isSynchronized(IResource.DEPTH_ZERO))
 			{
 				try {
 					Workspace workspace = (Workspace)file.getWorkspace();
 					if (workspace.getElementTree().isImmutable())
 					{
 						workspace.newWorkingTree();
 					}
					((org.eclipse.core.internal.resources.Resource)file).getLocalManager().refresh(file.getProject(), IResource.DEPTH_INFINITE, true, null);
 				} catch (CoreException e) {
 					throw new org.eclipse.emf.ecore.resource.Resource.IOWrappedException(e);
 				}
 			}
 			synchronized (needsSync) {
 				needsSync = new Boolean(false);
 			}
 		}
 		super.loadExisting(options);
 	}
 }
