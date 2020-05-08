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
 package org.eclipse.jst.j2ee.internal.webservice.provider;
 
 
 import java.util.Collection;
 import java.util.List;
 
 import org.eclipse.emf.common.notify.AdapterFactory;
 import org.eclipse.emf.common.notify.Notification;
 import org.eclipse.emf.common.util.ResourceLocator;
 import org.eclipse.emf.ecore.EReference;
 import org.eclipse.emf.edit.provider.ComposeableAdapterFactory;
 import org.eclipse.emf.edit.provider.IEditingDomainItemProvider;
 import org.eclipse.emf.edit.provider.IItemLabelProvider;
 import org.eclipse.emf.edit.provider.IItemPropertySource;
 import org.eclipse.emf.edit.provider.IStructuredItemContentProvider;
 import org.eclipse.emf.edit.provider.ITreeItemContentProvider;
 import org.eclipse.emf.edit.provider.ItemPropertyDescriptor;
 import org.eclipse.jst.j2ee.common.CommonFactory;
 import org.eclipse.jst.j2ee.common.internal.provider.CompatibilityDescriptionGroupItemProvider;
 import org.eclipse.jst.j2ee.internal.webservice.plugin.WebServicePlugin;
 import org.eclipse.jst.j2ee.webservice.internal.wsclient.Webservice_clientPackage;
 import org.eclipse.jst.j2ee.webservice.wsclient.ServiceRef;
 import org.eclipse.jst.j2ee.webservice.wsclient.Webservice_clientFactory;
 import org.eclipse.jst.j2ee.webservice.wscommon.WscommonFactory;
 import org.eclipse.jst.j2ee.webservice.wsdd.WsddFactory;
 
 /**
  * This is the item provider adpater for a {@link com.ibm.etools.wsclient.ServiceRef}object. <!--
  * begin-user-doc --> <!-- end-user-doc -->
  * 
  * @generated
  */
 public class ServiceRefItemProvider extends CompatibilityDescriptionGroupItemProvider implements IEditingDomainItemProvider, IStructuredItemContentProvider, ITreeItemContentProvider, IItemLabelProvider, IItemPropertySource {
 	/**
 	 * This constructs an instance from a factory and a notifier. <!-- begin-user-doc --> <!--
 	 * end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	public ServiceRefItemProvider(AdapterFactory adapterFactory) {
 		super(adapterFactory);
 	}
 
 	/**
 	 * This returns the property descriptors for the adapted class. <!-- begin-user-doc --> <!--
 	 * end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	public List getPropertyDescriptors(Object object) {
 		if (itemPropertyDescriptors == null) {
 			super.getPropertyDescriptors(object);
 
 			addServiceInterfacePropertyDescriptor(object);
 			addServiceRefNamePropertyDescriptor(object);
 			addWsdlFilePropertyDescriptor(object);
 			addJaxrpcMappingFilePropertyDescriptor(object);
 		}
 		return itemPropertyDescriptors;
 	}
 
 	/**
 	 * This adds a property descriptor for the Service Ref Name feature. <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	protected void addServiceRefNamePropertyDescriptor(Object object) {
 		itemPropertyDescriptors.add(new ItemPropertyDescriptor(((ComposeableAdapterFactory) adapterFactory).getRootAdapterFactory(), getString("%_UI_ServiceRef_serviceRefName_feature"), //$NON-NLS-1$
 					getString("%_UI_ServiceRef_serviceRefName_feature_desc"), //$NON-NLS-1$
 					Webservice_clientPackage.eINSTANCE.getServiceRef_ServiceRefName(), true, ItemPropertyDescriptor.GENERIC_VALUE_IMAGE));
 	}
 
 	/**
 	 * This adds a property descriptor for the Wsdl File feature. <!-- begin-user-doc --> <!--
 	 * end-user-doc -->
 	 *  
 	 */
 	protected void addWsdlFilePropertyDescriptor(Object object) {
 		itemPropertyDescriptors.add(new ItemPropertyDescriptor(((ComposeableAdapterFactory) adapterFactory).getRootAdapterFactory(), getString("%_UI_ServiceRef_wsdlFile_feature"), //$NON-NLS-1$
 					getString("%_UI_ServiceRef_wsdlFile_feature_desc"), //$NON-NLS-1$
 					Webservice_clientPackage.eINSTANCE.getServiceRef_WsdlFile(), false, ItemPropertyDescriptor.GENERIC_VALUE_IMAGE));
 	}
 
 	/**
 	 * This adds a property descriptor for the Jaxrpc Mapping File feature. <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 *  
 	 */
 	protected void addJaxrpcMappingFilePropertyDescriptor(Object object) {
 		itemPropertyDescriptors.add(new ItemPropertyDescriptor(((ComposeableAdapterFactory) adapterFactory).getRootAdapterFactory(), getString("%_UI_ServiceRef_jaxrpcMappingFile_feature"), //$NON-NLS-1$
 					getString("%_UI_ServiceRef_jaxrpcMappingFile_feature_desc"), //$NON-NLS-1$
 					Webservice_clientPackage.eINSTANCE.getServiceRef_JaxrpcMappingFile(), false, ItemPropertyDescriptor.GENERIC_VALUE_IMAGE));
 	}
 
 	/**
 	 * This adds a property descriptor for the Service Interface feature. <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 *  
 	 */
 	protected void addServiceInterfacePropertyDescriptor(Object object) {
 		itemPropertyDescriptors.add(new ItemPropertyDescriptor(((ComposeableAdapterFactory) adapterFactory).getRootAdapterFactory(), getString("%_UI_ServiceRef_serviceInterface_feature"), //$NON-NLS-1$
 					getString("%_UI_ServiceRef_serviceInterface_feature_desc"), //$NON-NLS-1$
 					Webservice_clientPackage.eINSTANCE.getServiceRef_ServiceInterface(), false));
 	}
 
 	/**
 	 * This specifies how to implement {@link #getChildren}and
 	 * {@link org.eclipse.emf.edit.command.AddCommand}and
 	 * {@link org.eclipse.emf.edit.command.RemoveCommand}support in {@link #createCommand}. <!--
 	 * begin-user-doc --> <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	public Collection getChildrenReferences(Object object) {
 		if (childrenReferences == null) {
 			super.getChildrenReferences(object);
 			//childrenReferences.add(Webservice_clientPackage.eINSTANCE.getServiceRef_PortComponentRefs());
			//childrenReferences.add(Webservice_clientPackage.eINSTANCE.getServiceRef_Handlers());
 			//childrenReferences.add(Webservice_clientPackage.eINSTANCE.getServiceRef_ServiceQname());
 		}
 		return childrenReferences;
 	}
 
 	/**
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	protected EReference getChildReference(Object object, Object child) {
 		// Check the type of the specified child object and return the proper feature to use for
 		// adding (see {@link AddCommand}) it as a child.
 
 		return super.getChildReference(object, child);
 	}
 
 
 	/**
 	 * This returns ServiceRef.gif. <!-- begin-user-doc --> <!-- end-user-doc -->
 	 */
 	public Object getImage(Object object) {
 		return getResourceLocator().getImage("icons/obj16/serviceref.gif"); //$NON-NLS-1$
 	}
 
 	/**
 	 * This returns the label text for the adapted class. <!-- begin-user-doc --> <!-- end-user-doc
 	 * -->
 	 * 
 	 * @generated
 	 */
 	/*
 	 * public String getText(Object object) { String label =
 	 * ((ServiceRef)object).getServiceRefName(); return label == null || label.length() == 0 ?
 	 * getString("_UI_ServiceRef_type") : getString("_UI_ServiceRef_type") + " " + label; }
 	 */
 
 	/**
 	 * This returns the label text for the adapted class. <!-- begin-user-doc --> <!-- end-user-doc
 	 * -->
 	 * 
 	 * @non-generated
 	 */
 	public String getText(Object object) {
 		String label = ((ServiceRef) object).getServiceRefName();
 		return "ServiceRef " + label; //$NON-NLS-1$
 	}
 
 
 	/**
 	 * This handles notification by calling {@link #fireNotifyChanged fireNotifyChanged}. <!--
 	 * begin-user-doc --> <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	public void notifyChanged(Notification notification) {
 		switch (notification.getFeatureID(ServiceRef.class)) {
 			case Webservice_clientPackage.SERVICE_REF__SERVICE_REF_NAME :
 			case Webservice_clientPackage.SERVICE_REF__WSDL_FILE :
 			case Webservice_clientPackage.SERVICE_REF__JAXRPC_MAPPING_FILE :
 			case Webservice_clientPackage.SERVICE_REF__PORT_COMPONENT_REFS :
 			case Webservice_clientPackage.SERVICE_REF__HANDLERS :
 			case Webservice_clientPackage.SERVICE_REF__SERVICE_QNAME : {
 				fireNotifyChanged(notification);
 				return;
 			}
 		}
 		super.notifyChanged(notification);
 	}
 
 	/**
 	 * This adds to the collection of {@link org.eclipse.emf.edit.command.CommandParameter}s
 	 * describing all of the children that can be created under this object. <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	protected void collectNewChildDescriptors(Collection newChildDescriptors, Object object) {
 		super.collectNewChildDescriptors(newChildDescriptors, object);
 
 		newChildDescriptors.add(createChildParameter(Webservice_clientPackage.eINSTANCE.getServiceRef_PortComponentRefs(), Webservice_clientFactory.eINSTANCE.createPortComponentRef()));
 
 		newChildDescriptors.add(createChildParameter(Webservice_clientPackage.eINSTANCE.getServiceRef_Handlers(), Webservice_clientFactory.eINSTANCE.createHandler()));
 
 		newChildDescriptors.add(createChildParameter(Webservice_clientPackage.eINSTANCE.getServiceRef_ServiceQname(), CommonFactory.eINSTANCE.createQName()));
 
 		newChildDescriptors.add(createChildParameter(Webservice_clientPackage.eINSTANCE.getServiceRef_ServiceQname(), WscommonFactory.eINSTANCE.createSOAPHeader()));
 
 		newChildDescriptors.add(createChildParameter(Webservice_clientPackage.eINSTANCE.getServiceRef_ServiceQname(), WsddFactory.eINSTANCE.createWSDLPort()));
 	}
 
 	/**
 	 * Return the resource locator for this item provider's resources. <!-- begin-user-doc --> <!--
 	 * end-user-doc -->
 	 */
 	public ResourceLocator getResourceLocator() {
 		return WebServicePlugin.getInstance();
 	}
 }
