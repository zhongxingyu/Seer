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
 package org.eclipse.jst.j2ee.internal.provider;
 
 
 import java.util.ArrayList;
 import java.util.Collection;
 
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.runtime.Path;
 import org.eclipse.emf.common.notify.AdapterFactory;
 import org.eclipse.emf.common.notify.Notification;
 import org.eclipse.emf.common.util.ResourceLocator;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.ecore.EReference;
 import org.eclipse.emf.ecore.resource.Resource;
 import org.eclipse.emf.edit.provider.ComposeableAdapterFactory;
 import org.eclipse.emf.edit.provider.IEditingDomainItemProvider;
 import org.eclipse.emf.edit.provider.IItemLabelProvider;
 import org.eclipse.emf.edit.provider.IItemPropertySource;
 import org.eclipse.emf.edit.provider.IStructuredItemContentProvider;
 import org.eclipse.emf.edit.provider.ITreeItemContentProvider;
 import org.eclipse.emf.edit.provider.ItemPropertyDescriptor;
 import org.eclipse.jem.util.emf.workbench.ProjectUtilities;
 import org.eclipse.jst.j2ee.client.ApplicationClient;
 import org.eclipse.jst.j2ee.common.CommonFactory;
 import org.eclipse.jst.j2ee.common.internal.provider.CompatibilityDescriptionGroupItemProvider;
 import org.eclipse.jst.j2ee.internal.J2EEVersionConstants;
 import org.eclipse.jst.j2ee.internal.application.provider.ApplicationProvidersResourceHandler;
 import org.eclipse.jst.j2ee.internal.client.ClientPackage;
 import org.eclipse.jst.j2ee.internal.plugin.J2EEPlugin;
 import org.eclipse.jst.j2ee.internal.webservices.WSDLServiceExtManager;
 import org.eclipse.jst.j2ee.internal.webservices.WSDLServiceHelper;
 import org.eclipse.jst.j2ee.webservice.wsclient.Webservice_clientFactory;
 
 /**
  * This is the item provider adpater for a
  * {@link org.eclipse.jst.j2ee.internal.internal.client.ApplicationClient}object.
  */
 public class ApplicationClientItemProvider extends CompatibilityDescriptionGroupItemProvider implements IEditingDomainItemProvider, IItemLabelProvider, IItemPropertySource, IStructuredItemContentProvider, ITreeItemContentProvider {
 	/**
 	 * This constructs an instance from a factory and a notifier.
 	 */
 	public ApplicationClientItemProvider(AdapterFactory adapterFactory) {
 		super(adapterFactory);
 	}
 
 	/**
 	 * This creates a new child for
 	 * {@link org.eclipse.jst.j2ee.internal.internal.client.command.CreateChildCommand}.
 	 */
 	public Object createChild(Object object) {
 		// TODO: create some child object.
 		return null;
 	}
 
 	/**
 	 * This specifies how to implement {@link #getChildren}and {@link AddCommand}and
 	 * {@link RemoveCommand}support in {@link #createCommand}.
 	 */
 	public Collection getChildrenReferences(Object object) {
 		ClientPackage pkg = ClientPackage.eINSTANCE;
 		Collection result = new ArrayList();
 		result.add(pkg.getApplicationClient_EjbReferences());
 		result.add(pkg.getApplicationClient_ResourceRefs());
 		result.add(pkg.getApplicationClient_ResourceEnvRefs());
 		result.add(pkg.getApplicationClient_EnvironmentProps());
 		result.add(pkg.getApplicationClient_MessageDestinationRefs());
 		result.add(pkg.getApplicationClient_ServiceRefs());
 		return result;
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
 	 * This returns the image for
 	 * {@link org.eclipse.jst.j2ee.internal.internal.client.command.CreateChildCommand}.
 	 */
 	public Object getCreateChildImage(Object object) {
 		EObject refObject = (EObject) object;
 		return J2EEPlugin.getPlugin().getImage(refObject.eClass().getName() + "Create#CHILD_CLASS_NAME#"); //$NON-NLS-1$
 	}
 
 	/**
 	 * This returns the label for
 	 * {@link org.eclipse.jst.j2ee.internal.internal.client.command.CreateChildCommand}.
 	 */
 	public String getCreateChildText(Object object) {
 		return ApplicationProvidersResourceHandler.getString("Create_Child_UI_"); //$NON-NLS-1$
 	}
 
 	/**
 	 * This returns the help text for
 	 * {@link org.eclipse.jst.j2ee.internal.internal.client.command.CreateChildCommand}.
 	 */
 	public String getCreateChildToolTipText(Object object) {
 		EObject refObject = (EObject) object;
 		return ApplicationProvidersResourceHandler.getString("Create_a_child_for_the_selected_UI_") + refObject.eClass().getName() + "."; //$NON-NLS-1$ //$NON-NLS-2$
 	}
 
 	/**
 	 * This returns ApplicationClient.gif.
 	 */
 	public Object getImage(Object object) {
 		String key = null;
 		switch (((ApplicationClient) object).getVersionID()) {
 			case J2EEVersionConstants.J2EE_1_2_ID :
 				key = "appclient_12"; //$NON-NLS-1$
 				break;
 			case J2EEVersionConstants.J2EE_1_3_ID :
 				key = "appclient_13"; //$NON-NLS-1$
 				break;
 			case J2EEVersionConstants.J2EE_1_4_ID :
 			default :
 				key = "appclient_14"; //$NON-NLS-1$
 				break;
 		}
 		return J2EEPlugin.getPlugin().getImage(key);
 	}
 
 	/**
 	 * This returns the parent of the ApplicationClient.
 	 */
 	public Object getParent(Object object) {
 		return ((EObject) object).eContainer();
 	}
 
 	/**
 	 * This adds a property descriptor for the Version feature. <!-- begin-user-doc --> <!--
 	 * end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	protected void addVersionPropertyDescriptor(Object object) {
 		itemPropertyDescriptors.add(new ItemPropertyDescriptor(((ComposeableAdapterFactory) adapterFactory).getRootAdapterFactory(), getString("_UI_ApplicationClient_version_feature"), //$NON-NLS-1$
 					getString("_UI_PropertyDescriptor_description", "_UI_ApplicationClient_version_feature", "_UI_ApplicationClient_type"), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
 					ClientPackage.eINSTANCE.getApplicationClient_Version(), true, ItemPropertyDescriptor.GENERIC_VALUE_IMAGE));
 	}
 
 	/**
 	 * This adds a property descriptor for the Callback Handler feature. <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	protected void addCallbackHandlerPropertyDescriptor(Object object) {
 		itemPropertyDescriptors.add(new ItemPropertyDescriptor(((ComposeableAdapterFactory) adapterFactory).getRootAdapterFactory(), getString("_UI_ApplicationClient_callbackHandler_feature"), //$NON-NLS-1$
 					getString("_UI_PropertyDescriptor_description", "_UI_ApplicationClient_callbackHandler_feature", "_UI_ApplicationClient_type"), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
 					ClientPackage.eINSTANCE.getApplicationClient_CallbackHandler(), true));
 	}
 
 	public String getText(Object object) {
 
 		/*
 		 * String displayName = ((ApplicationClient) object).getDisplayName(); return displayName ==
 		 * null ? "" ApplicationProvidersResourceHandler.getString(" < <No_display_name>>_ERROR_") :
 		 * displayName; //$NON-NLS-1$
 		 */
 		ApplicationClient appclient = ((ApplicationClient) object);
 		if (appclient.getDisplayName() == null) {
 			try {
 				IProject project = ProjectUtilities.getProject(appclient);
 				return project.getDescription().getName();
 			} catch (Exception e) {
 				//Ignore
 			}
 
 			Resource resource = appclient.eResource();
 			if (resource != null) {
 				return new Path(resource.getURI().toString()).removeFileExtension().lastSegment();
 			}
 			return ""; //$NON-NLS-1$
 		}
 		return appclient.getDisplayName();
 	}
 
 	/**
 	 * This handles notification by calling {@link #fireNotifyChanged fireNotifyChanged}. <!--
 	 * begin-user-doc --> <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	public void notifyChanged(Notification notification) {
 		switch (notification.getFeatureID(ApplicationClient.class)) {
 			case ClientPackage.APPLICATION_CLIENT__VERSION :
 			case ClientPackage.APPLICATION_CLIENT__RESOURCE_REFS :
 			case ClientPackage.APPLICATION_CLIENT__ENVIRONMENT_PROPS :
 			case ClientPackage.APPLICATION_CLIENT__EJB_REFERENCES :
 			case ClientPackage.APPLICATION_CLIENT__RESOURCE_ENV_REFS :
 			case ClientPackage.APPLICATION_CLIENT__SERVICE_REFS :
 			case ClientPackage.APPLICATION_CLIENT__MESSAGE_DESTINATION_REFS :
 			case ClientPackage.APPLICATION_CLIENT__MESSAGE_DESTINATIONS : {
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
 
 		newChildDescriptors.add(createChildParameter(ClientPackage.eINSTANCE.getApplicationClient_ResourceRefs(), CommonFactory.eINSTANCE.createResourceRef()));
 
 		newChildDescriptors.add(createChildParameter(ClientPackage.eINSTANCE.getApplicationClient_EnvironmentProps(), CommonFactory.eINSTANCE.createEnvEntry()));
 
 		newChildDescriptors.add(createChildParameter(ClientPackage.eINSTANCE.getApplicationClient_EjbReferences(), CommonFactory.eINSTANCE.createEjbRef()));
 
 		newChildDescriptors.add(createChildParameter(ClientPackage.eINSTANCE.getApplicationClient_EjbReferences(), CommonFactory.eINSTANCE.createEJBLocalRef()));
 
 		newChildDescriptors.add(createChildParameter(ClientPackage.eINSTANCE.getApplicationClient_ResourceEnvRefs(), CommonFactory.eINSTANCE.createResourceEnvRef()));
 
 		newChildDescriptors.add(createChildParameter(ClientPackage.eINSTANCE.getApplicationClient_ServiceRefs(), Webservice_clientFactory.eINSTANCE.createServiceRef()));
 
 		newChildDescriptors.add(createChildParameter(ClientPackage.eINSTANCE.getApplicationClient_MessageDestinationRefs(), CommonFactory.eINSTANCE.createMessageDestinationRef()));
 
 		newChildDescriptors.add(createChildParameter(ClientPackage.eINSTANCE.getApplicationClient_MessageDestinations(), CommonFactory.eINSTANCE.createMessageDestination()));
 	}
 
 	/**
 	 * Return the resource locator for this item provider's resources. <!-- begin-user-doc --> <!--
 	 * end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	public ResourceLocator getResourceLocator() {
 		return J2EEPlugin.getDefault();
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.eclipse.emf.edit.provider.ItemProviderAdapter#getChildren(java.lang.Object)
 	 */
 	public Collection getChildren(Object object) {
 		ApplicationClient client = (ApplicationClient) object;
 		Collection myChildren = super.getChildren(object);
		myChildren.addAll(client.getMessageDestinations());
 		if (client.getVersionID() <= J2EEVersionConstants.J2EE_1_3_ID) {
 			WSDLServiceHelper serviceHelper = WSDLServiceExtManager.getServiceHelper();
 			myChildren.addAll(serviceHelper.get13ServiceRefs(client));
 		}	
 		return myChildren;
 	}
 }
