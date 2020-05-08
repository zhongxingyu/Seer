 /*******************************************************************************
  * Copyright (c) 2003, 2005 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  * IBM Corporation - initial API and implementation
  *******************************************************************************/
 package org.eclipse.jst.j2ee.internal.provider;
 
 
 import org.eclipse.core.resources.IFile;
 import org.eclipse.emf.common.notify.Notification;
 import org.eclipse.emf.ecore.EStructuralFeature;
 import org.eclipse.emf.edit.provider.IItemLabelProvider;
 import org.eclipse.emf.edit.ui.provider.AdapterFactoryLabelProvider;
 import org.eclipse.jst.j2ee.common.CommonPackage;
 import org.eclipse.jst.j2ee.common.internal.util.CommonUtil;
 import org.eclipse.jst.j2ee.ejb.internal.impl.EjbFactoryImpl;
 import org.eclipse.jst.j2ee.internal.ejb.provider.AbstractMethodsContentProvider;
 import org.eclipse.jst.j2ee.internal.ejb.provider.J2EEJavaClassProviderHelper;
 import org.eclipse.jst.j2ee.internal.plugin.J2EEUIMessages;
 import org.eclipse.swt.graphics.Image;
 import org.eclipse.ui.model.WorkbenchLabelProvider;
 
 /**
  * Insert the type's description here. Creation date: (6/20/2001 10:40:41 PM)
  * 
  * @author: Administrator
  */
 public class J2EEAdapterFactoryLabelProvider extends AdapterFactoryLabelProvider {
 	// //$NON-NLS-1$
 	protected static final Class IItemLabelProviderClass = IItemLabelProvider.class;
 	private static final EStructuralFeature ROLE_NAME_SF = CommonPackage.eINSTANCE.getSecurityRole_RoleName();
 	private static final EStructuralFeature ROLES_SF = EjbFactoryImpl.getPackage().getMethodPermission_Roles();
 
 
 	/**
 	 * This is used for delegation to get labels for server objects, which don't fit into EMF edit
 	 */
 	protected WorkbenchLabelProvider wbLabelProvider;
 
 	public J2EEAdapterFactoryLabelProvider(org.eclipse.emf.common.notify.AdapterFactory adapterFactory) {
 		super(adapterFactory);
 		wbLabelProvider = new WorkbenchLabelProvider();
 	}
 
 	public Image getImage(Object object) {
 		if (object instanceof J2EEJavaClassProviderHelper)
 			return ((J2EEJavaClassProviderHelper) object).getImage();
 		else if (isEMFEditObject(object))
 			return super.getImage(object);
 		else if (object instanceof AbstractMethodsContentProvider.EJBMethodItem)
 			return super.getImage(((AbstractMethodsContentProvider.EJBMethodItem) object).ejb);
 		return wbLabelProvider.getImage(object);
 	}
 
 	public String getText(Object object) {
 
 		if (object instanceof J2EEJavaClassProviderHelper)
 			return ((J2EEJavaClassProviderHelper) object).getText();
 		else if (isEMFEditObject(object)) {
 			if (CommonUtil.isDeploymentDescriptorRoot(object))
 				return J2EEUIMessages.getResourceString("Deployment_Descriptor_UI_") + ": " + super.getText(object); //$NON-NLS-1$ //$NON-NLS-2$ 
 			return super.getText(object);
 		} else if (object instanceof AbstractMethodsContentProvider.EJBMethodItem)
 			return super.getText(((AbstractMethodsContentProvider.EJBMethodItem) object).ejb);
 		else if (object instanceof IFile) {
 			return ((IFile) object).getName();
 		}
 		return wbLabelProvider.getText(object);
 	}
 
 	protected boolean isEMFEditObject(Object object) {
 		IItemLabelProvider itemLabelProvider = (IItemLabelProvider) adapterFactory.adapt(object, IItemLabelProviderClass);
 		return itemLabelProvider != null;
 	}
 
 	/*
 	 * @see INotifyChangedListener#notifyChanged(new ENotificationImpl((InternalEObject)Object,
 	 *      int,(EStructuralFeature) Object, Object, Object, int))
 	 */
 	public void notifyChanged(Notification notification) {
 		Object feature = notification.getFeature();
 		if (feature == ROLE_NAME_SF || feature == ROLES_SF)
 			fireLabelProviderChanged();
 	}
 }
