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
 package org.eclipse.jst.j2ee.internal.application.provider;
 
 
 import java.util.ArrayList;
 import java.util.Collection;
 
 import org.eclipse.emf.common.notify.AdapterFactory;
 import org.eclipse.emf.common.notify.Notification;
 import org.eclipse.emf.common.util.ResourceLocator;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.ecore.EReference;
 import org.eclipse.emf.edit.command.AddCommand;
 import org.eclipse.emf.edit.command.RemoveCommand;
 import org.eclipse.emf.edit.provider.ComposeableAdapterFactory;
 import org.eclipse.emf.edit.provider.IEditingDomainItemProvider;
 import org.eclipse.emf.edit.provider.IItemLabelProvider;
 import org.eclipse.emf.edit.provider.IItemPropertySource;
 import org.eclipse.emf.edit.provider.IStructuredItemContentProvider;
 import org.eclipse.emf.edit.provider.ITreeItemContentProvider;
 import org.eclipse.emf.edit.provider.ItemPropertyDescriptor;
 import org.eclipse.jst.j2ee.application.Application;
 import org.eclipse.jst.j2ee.application.ApplicationFactory;
 import org.eclipse.jst.j2ee.application.ApplicationPackage;
 import org.eclipse.jst.j2ee.application.Module;
 import org.eclipse.jst.j2ee.common.CommonFactory;
 import org.eclipse.jst.j2ee.common.internal.provider.CompatibilityDescriptionGroupItemProvider;
 import org.eclipse.jst.j2ee.internal.J2EEVersionConstants;
 import org.eclipse.jst.j2ee.internal.plugin.J2EEPlugin;
 
 
 /**
  * This is the item provider adpater for a
  * {@link org.eclipse.jst.j2ee.internal.internal.application.Application}object.
  */
 public class ApplicationItemProvider extends CompatibilityDescriptionGroupItemProvider implements IEditingDomainItemProvider, IItemLabelProvider, IItemPropertySource, IStructuredItemContentProvider, ITreeItemContentProvider {
 	/**
 	 * This constructs an instance from a factory and a notifier.
 	 */
 	public ApplicationItemProvider(AdapterFactory adapterFactory) {
 		super(adapterFactory);
 	}
 
 	/**
 	 * This creates a new child for
 	 * {@link org.eclipse.jst.j2ee.internal.internal.application.command.CreateChildCommand}.
 	 */
 	public Object createChild(Object object) {
 		// TODO: check that this is what you want.
 		Module child = org.eclipse.jst.j2ee.internal.plugin.J2EEPlugin.getPlugin().getApplicationFactory().createModule();
 
 		// TODO: initialize child here...
 
 		return child;
 	}
 
 	/**
 	 * This specifies how to implement {@link #getChildren}and {@link AddCommand}and
 	 * {@link RemoveCommand}support in {@link #createCommand}.
 	 */
 	public Collection getChildrenReferences(Object object) {
 		ApplicationPackage pkg = ApplicationPackage.eINSTANCE;
 		Collection result = new ArrayList();
 		result.add(pkg.getApplication_SecurityRoles());
 		result.add(pkg.getApplication_Modules());
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
 	 * {@link org.eclipse.jst.j2ee.internal.internal.application.command.CreateChildCommand}.
 	 */
 	public Object getCreateChildImage(Object object) {
 		EObject refObject = (EObject) object;
 		return org.eclipse.jst.j2ee.internal.plugin.J2EEPlugin.getPlugin().getImage(refObject.eClass().getName() + "CreateModule"); //$NON-NLS-1$
 	}
 
 	/**
 	 * This returns the label for
 	 * {@link org.eclipse.jst.j2ee.internal.internal.application.command.CreateChildCommand}.
 	 */
 	public String getCreateChildText(Object object) {
 		return ApplicationProvidersResourceHandler.getString("Create_Module_UI_"); //$NON-NLS-1$
 	}
 
 	/**
 	 * This returns the help text for
 	 * {@link org.eclipse.jst.j2ee.internal.internal.application.command.CreateChildCommand}.
 	 */
 	public String getCreateChildToolTipText(Object object) {
 		EObject refObject = (EObject) object;
 		return ApplicationProvidersResourceHandler.getString("Create_a_child_of_type_Module_for_the_selected_UI_") + refObject.eClass().getName() + "."; //$NON-NLS-1$ //$NON-NLS-2$
 	}
 
 	/**
 	 * This returns Application.gif.
 	 */
 	public Object getImage(Object object) {
 		String key = null;
 		if (((Application) object).eResource() == null)
			return J2EEPlugin.getPlugin().getImage("earFile_obj"); //$NON-NLS-1$
 		switch (((Application) object).getVersionID()) {
 			case J2EEVersionConstants.J2EE_1_2_ID :
 				key = "12_ear_obj"; //$NON-NLS-1$
 				break;
 			case J2EEVersionConstants.J2EE_1_3_ID :
 				key = "13_ear_obj"; //$NON-NLS-1$
 				break;
 			case J2EEVersionConstants.J2EE_1_4_ID :
 			default :
 				key = "14_ear_obj"; //$NON-NLS-1$
 				break;
 		}
 		return J2EEPlugin.getPlugin().getImage(key);
 	}
 
 	/**
 	 * This returns the parent of the Application.
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
 		itemPropertyDescriptors.add(new ItemPropertyDescriptor(((ComposeableAdapterFactory) adapterFactory).getRootAdapterFactory(), getString("_UI_Application_version_feature"), //$NON-NLS-1$
 					getString("_UI_PropertyDescriptor_description", "_UI_Application_version_feature", "_UI_Application_type"), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
 					ApplicationPackage.eINSTANCE.getApplication_Version(), true, ItemPropertyDescriptor.GENERIC_VALUE_IMAGE));
 	}
 
 	public String getText(Object object) {
 
 		String displayName = ((Application) object).getDisplayName();
 		return displayName == null ? ApplicationProvidersResourceHandler.getString("<<No_display_name>>_ERROR_") : displayName; //$NON-NLS-1$
 	}
 
 	/**
 	 * This handles notification by calling {@link #fireNotifyChanged fireNotifyChanged}. <!--
 	 * begin-user-doc --> <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	public void notifyChanged(Notification notification) {
 		switch (notification.getFeatureID(Application.class)) {
 			case ApplicationPackage.APPLICATION__VERSION :
 			case ApplicationPackage.APPLICATION__SECURITY_ROLES :
 			case ApplicationPackage.APPLICATION__MODULES : {
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
 
 		newChildDescriptors.add(createChildParameter(ApplicationPackage.eINSTANCE.getApplication_SecurityRoles(), CommonFactory.eINSTANCE.createSecurityRole()));
 
 		newChildDescriptors.add(createChildParameter(ApplicationPackage.eINSTANCE.getApplication_Modules(), ApplicationFactory.eINSTANCE.createModule()));
 
 		newChildDescriptors.add(createChildParameter(ApplicationPackage.eINSTANCE.getApplication_Modules(), ApplicationFactory.eINSTANCE.createWebModule()));
 
 		newChildDescriptors.add(createChildParameter(ApplicationPackage.eINSTANCE.getApplication_Modules(), ApplicationFactory.eINSTANCE.createJavaClientModule()));
 
 		newChildDescriptors.add(createChildParameter(ApplicationPackage.eINSTANCE.getApplication_Modules(), ApplicationFactory.eINSTANCE.createEjbModule()));
 
 		newChildDescriptors.add(createChildParameter(ApplicationPackage.eINSTANCE.getApplication_Modules(), ApplicationFactory.eINSTANCE.createConnectorModule()));
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
 }
