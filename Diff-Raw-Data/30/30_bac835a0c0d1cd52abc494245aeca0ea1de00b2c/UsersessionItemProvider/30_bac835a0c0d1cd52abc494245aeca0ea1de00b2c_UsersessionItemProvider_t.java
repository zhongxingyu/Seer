 /*******************************************************************************
  * Copyright (c) 2008-2011 Chair for Applied Software Engineering,
  * Technische Universitaet Muenchen.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  ******************************************************************************/
 package org.eclipse.emf.emfstore.internal.client.model.provider;
 
 import java.util.Collection;
 import java.util.List;
 
 import org.eclipse.emf.common.notify.AdapterFactory;
 import org.eclipse.emf.common.notify.Notification;
 import org.eclipse.emf.common.util.ResourceLocator;
 import org.eclipse.emf.ecore.EStructuralFeature;
 import org.eclipse.emf.edit.provider.ComposeableAdapterFactory;
 import org.eclipse.emf.edit.provider.IEditingDomainItemProvider;
 import org.eclipse.emf.edit.provider.IItemLabelProvider;
 import org.eclipse.emf.edit.provider.IItemPropertyDescriptor;
 import org.eclipse.emf.edit.provider.IItemPropertySource;
 import org.eclipse.emf.edit.provider.IStructuredItemContentProvider;
 import org.eclipse.emf.edit.provider.ITreeItemContentProvider;
 import org.eclipse.emf.edit.provider.ItemPropertyDescriptor;
 import org.eclipse.emf.edit.provider.ItemProviderAdapter;
 import org.eclipse.emf.edit.provider.ViewerNotification;
 import org.eclipse.emf.emfstore.internal.client.model.ModelPackage;
 import org.eclipse.emf.emfstore.internal.client.model.ServerInfo;
 import org.eclipse.emf.emfstore.internal.client.model.Usersession;
import org.eclipse.emf.emfstore.internal.server.model.accesscontrol.AccesscontrolFactory;
 
 /**
  * This is the item provider adapter for a {@link org.eclipse.emf.emfstore.internal.client.model.Usersession} object. <!--
  * begin-user-doc --> <!-- end-user-doc -->
  * 
  * @generated
  */
 public class UsersessionItemProvider extends ItemProviderAdapter implements IEditingDomainItemProvider,
 	IStructuredItemContentProvider, ITreeItemContentProvider, IItemLabelProvider, IItemPropertySource {
 	/**
 	 * This constructs an instance from a factory and a notifier.
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	public UsersessionItemProvider(AdapterFactory adapterFactory) {
 		super(adapterFactory);
 	}
 
 	/**
 	 * This returns the property descriptors for the adapted class.
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	@Override
 	public List<IItemPropertyDescriptor> getPropertyDescriptors(Object object) {
 		if (itemPropertyDescriptors == null) {
 			super.getPropertyDescriptors(object);
 
 			addUsernamePropertyDescriptor(object);
 			addPasswordPropertyDescriptor(object);
 			addSessionIdPropertyDescriptor(object);
 			addPersistentPasswordPropertyDescriptor(object);
 			addServerInfoPropertyDescriptor(object);
 			addSavePasswordPropertyDescriptor(object);
 		}
 		return itemPropertyDescriptors;
 	}
 
 	/**
 	 * This adds a property descriptor for the Username feature.
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	protected void addUsernamePropertyDescriptor(Object object) {
 		itemPropertyDescriptors
 			.add(createItemPropertyDescriptor(
 				((ComposeableAdapterFactory) adapterFactory).getRootAdapterFactory(),
 				getResourceLocator(),
 				getString("_UI_Usersession_username_feature"),
 				getString("_UI_PropertyDescriptor_description", "_UI_Usersession_username_feature",
 					"_UI_Usersession_type"), ModelPackage.Literals.USERSESSION__USERNAME, true, false, false,
 				ItemPropertyDescriptor.GENERIC_VALUE_IMAGE, null, null));
 	}
 
 	/**
 	 * This adds a property descriptor for the Password feature.
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	protected void addPasswordPropertyDescriptor(Object object) {
 		itemPropertyDescriptors
 			.add(createItemPropertyDescriptor(
 				((ComposeableAdapterFactory) adapterFactory).getRootAdapterFactory(),
 				getResourceLocator(),
 				getString("_UI_Usersession_password_feature"),
 				getString("_UI_PropertyDescriptor_description", "_UI_Usersession_password_feature",
 					"_UI_Usersession_type"), ModelPackage.Literals.USERSESSION__PASSWORD, true, false, false,
 				ItemPropertyDescriptor.GENERIC_VALUE_IMAGE, null, null));
 	}
 
 	/**
 	 * This adds a property descriptor for the Session Id feature.
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	protected void addSessionIdPropertyDescriptor(Object object) {
 		itemPropertyDescriptors
 			.add(createItemPropertyDescriptor(
 				((ComposeableAdapterFactory) adapterFactory).getRootAdapterFactory(),
 				getResourceLocator(),
 				getString("_UI_Usersession_sessionId_feature"),
 				getString("_UI_PropertyDescriptor_description", "_UI_Usersession_sessionId_feature",
 					"_UI_Usersession_type"), ModelPackage.Literals.USERSESSION__SESSION_ID, true, false, true, null,
 				null, null));
 	}
 
 	/**
 	 * This adds a property descriptor for the Persistent Password feature. <!-- begin-user-doc --> <!-- end-user-doc
 	 * -->
 	 * 
 	 * @generated
 	 */
 	protected void addPersistentPasswordPropertyDescriptor(Object object) {
 		itemPropertyDescriptors.add(createItemPropertyDescriptor(
 			((ComposeableAdapterFactory) adapterFactory).getRootAdapterFactory(),
 			getResourceLocator(),
 			getString("_UI_Usersession_persistentPassword_feature"),
 			getString("_UI_PropertyDescriptor_description", "_UI_Usersession_persistentPassword_feature",
 				"_UI_Usersession_type"), ModelPackage.Literals.USERSESSION__PERSISTENT_PASSWORD, true, false, false,
 			ItemPropertyDescriptor.GENERIC_VALUE_IMAGE, null, null));
 	}
 
 	/**
 	 * This adds a property descriptor for the Server Info feature.
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	protected void addServerInfoPropertyDescriptor(Object object) {
 		itemPropertyDescriptors.add(createItemPropertyDescriptor(
 			((ComposeableAdapterFactory) adapterFactory).getRootAdapterFactory(),
 			getResourceLocator(),
 			getString("_UI_Usersession_serverInfo_feature"),
 			getString("_UI_PropertyDescriptor_description", "_UI_Usersession_serverInfo_feature",
 				"_UI_Usersession_type"), ModelPackage.Literals.USERSESSION__SERVER_INFO, true, false, true, null, null,
 			null));
 	}
 
 	/**
 	 * This adds a property descriptor for the Save Password feature.
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	protected void addSavePasswordPropertyDescriptor(Object object) {
 		itemPropertyDescriptors.add(createItemPropertyDescriptor(
 			((ComposeableAdapterFactory) adapterFactory).getRootAdapterFactory(),
 			getResourceLocator(),
 			getString("_UI_Usersession_savePassword_feature"),
 			getString("_UI_PropertyDescriptor_description", "_UI_Usersession_savePassword_feature",
 				"_UI_Usersession_type"), ModelPackage.Literals.USERSESSION__SAVE_PASSWORD, true, false, false,
 			ItemPropertyDescriptor.BOOLEAN_VALUE_IMAGE, null, null));
 	}
 
 	/**
 	 * This specifies how to implement {@link #getChildren} and is used to deduce an appropriate feature for an
 	 * {@link org.eclipse.emf.edit.command.AddCommand}, {@link org.eclipse.emf.edit.command.RemoveCommand} or
 	 * {@link org.eclipse.emf.edit.command.MoveCommand} in {@link #createCommand}.
 	 * <!-- begin-user-doc --> <!--
 	 * end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	@Override
 	public Collection<? extends EStructuralFeature> getChildrenFeatures(Object object) {
 		if (childrenFeatures == null) {
 			super.getChildrenFeatures(object);
 			childrenFeatures.add(ModelPackage.Literals.USERSESSION__AC_USER);
 			childrenFeatures.add(ModelPackage.Literals.USERSESSION__CHANGED_PROPERTIES);
 		}
 		return childrenFeatures;
 	}
 
 	/**
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	@Override
 	protected EStructuralFeature getChildFeature(Object object, Object child) {
 		// Check the type of the specified child object and return the proper feature to use for
 		// adding (see {@link AddCommand}) it as a child.
 
 		return super.getChildFeature(object, child);
 	}
 
 	/**
 	 * This returns Usersession.gif.
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	@Override
 	public Object getImage(Object object) {
 		return overlayImage(object, getResourceLocator().getImage("full/obj16/Usersession"));
 	}
 
 	/**
 	 * This returns the label text for the adapted class.
 	 * 
 	 * @param object the object to provide the label for
 	 * @return the label <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * @generated NOT
 	 */
 	@Override
 	public String getText(Object object) {
 		Usersession session = ((Usersession) object);
 		ServerInfo server = session.getServerInfo();
 		return session.getUsername() + "@" + (server != null ? server.getName() : "unknown");
 	}
 
 	/**
 	 * This handles model notifications by calling {@link #updateChildren} to update any cached children and by creating
 	 * a viewer notification, which it passes to {@link #fireNotifyChanged}. <!-- begin-user-doc --> <!-- end-user-doc
 	 * -->
 	 * 
 	 * @generated
 	 */
 	@Override
 	public void notifyChanged(Notification notification) {
 		updateChildren(notification);
 
 		switch (notification.getFeatureID(Usersession.class)) {
 		case ModelPackage.USERSESSION__USERNAME:
 		case ModelPackage.USERSESSION__PASSWORD:
 		case ModelPackage.USERSESSION__PERSISTENT_PASSWORD:
 		case ModelPackage.USERSESSION__SAVE_PASSWORD:
 			fireNotifyChanged(new ViewerNotification(notification, notification.getNotifier(), false, true));
 			return;
 		case ModelPackage.USERSESSION__AC_USER:
 		case ModelPackage.USERSESSION__CHANGED_PROPERTIES:
 			fireNotifyChanged(new ViewerNotification(notification, notification.getNotifier(), true, false));
 			return;
 		}
 		super.notifyChanged(notification);
 	}
 
 	/**
 	 * This adds {@link org.eclipse.emf.edit.command.CommandParameter}s describing the children
 	 * that can be created under this object.
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	@Override
 	protected void collectNewChildDescriptors(Collection<Object> newChildDescriptors, Object object) {
 		super.collectNewChildDescriptors(newChildDescriptors, object);
 
 		newChildDescriptors.add(createChildParameter(ModelPackage.Literals.USERSESSION__AC_USER,
 			AccesscontrolFactory.eINSTANCE.createACUser()));
 
 		newChildDescriptors.add(createChildParameter(ModelPackage.Literals.USERSESSION__CHANGED_PROPERTIES,
 			AccesscontrolFactory.eINSTANCE.createOrgUnitProperty()));
 	}
 
 	/**
 	 * Return the resource locator for this item provider's resources.
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	@Override
 	public ResourceLocator getResourceLocator() {
 		return ClientModelEditPlugin.INSTANCE;
 	}
 
 }
