 /*******************************************************************************
  * Copyright (c) 2003, 2004 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  * IBM Corporation - initial API and implementation
  *******************************************************************************/
 /*
  * Created on Mar 29, 2004
  *
  * To change the template for this generated file go to
  * Window - Preferences - Java - Code Generation - Code and Comments
  */
 package org.eclipse.jst.j2ee.internal.war.ui.util;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.List;
 
 import org.eclipse.emf.common.notify.AdapterFactory;
 import org.eclipse.jst.j2ee.internal.plugin.J2EEPlugin;
 import org.eclipse.jst.j2ee.internal.web.providers.WebAppEditResourceHandler;
 import org.eclipse.jst.j2ee.internal.webservices.WSDLServiceExtManager;
 import org.eclipse.jst.j2ee.internal.webservices.WSDLServiceHelper;
 import org.eclipse.jst.j2ee.webapplication.WebApp;
 
 /**
  * @author jlanuti
  * 
  * To change the template for this generated type comment go to Window - Preferences - Java - Code
  * Generation - Code and Comments
  */
 public class WebReferencesGroupItemProvider extends WebGroupItemProvider {
 
 	/**
 	 * @param adapterFactory
 	 */
 	public WebReferencesGroupItemProvider(AdapterFactory adapterFactory, WebApp webApp) {
 		super(adapterFactory, webApp);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.eclipse.emf.edit.provider.ITreeItemContentProvider#getParent(java.lang.Object)
 	 */
 	public Object getParent(Object object) {
 		return webApp;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.eclipse.emf.edit.provider.ITreeItemContentProvider#getChildren(java.lang.Object)
 	 */
 	public Collection getChildren(Object object) {
 		List result = new ArrayList();
 		if (!webApp.getEjbLocalRefs().isEmpty())
 			result.addAll(webApp.getEjbLocalRefs());
 		if (!webApp.getEjbRefs().isEmpty())
 			result.addAll(webApp.getEjbRefs());
 		if (!webApp.getResourceEnvRefs().isEmpty())
 			result.addAll(webApp.getResourceEnvRefs());
 		if (!webApp.getResourceRefs().isEmpty())
 			result.addAll(webApp.getResourceRefs());
 		if (!webApp.getMessageDestinationRefs().isEmpty())
 			result.addAll(webApp.getMessageDestinationRefs());
 		if (!webApp.getServiceRefs().isEmpty())
 			result.addAll(webApp.getServiceRefs());
 		Collection serviceRefs = null;
 		try {
 			WSDLServiceHelper serviceHelper = WSDLServiceExtManager.getServiceHelper();
 			serviceRefs = serviceHelper.get13ServiceRefs(webApp);
		} catch (RuntimeException re) {
 			serviceRefs = Collections.EMPTY_LIST;
 		}
 
		if (!serviceRefs.isEmpty())
 			result.addAll(serviceRefs);
 		return result;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.eclipse.emf.edit.provider.ITreeItemContentProvider#hasChildren(java.lang.Object)
 	 */
 	public boolean hasChildren(Object object) {
 		return !getChildren(object).isEmpty();
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.eclipse.emf.edit.provider.ItemProvider#getImage()
 	 */
 	public Object getImage(Object object) {
 		return J2EEPlugin.getDefault().getImage("resourceRef_obj"); //$NON-NLS-1$
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.eclipse.emf.edit.provider.ItemProvider#getText()
 	 */
 	public String getText(Object object) {
 		return WebAppEditResourceHandler.getString("References_1"); //$NON-NLS-1$ 
 	}
 }
