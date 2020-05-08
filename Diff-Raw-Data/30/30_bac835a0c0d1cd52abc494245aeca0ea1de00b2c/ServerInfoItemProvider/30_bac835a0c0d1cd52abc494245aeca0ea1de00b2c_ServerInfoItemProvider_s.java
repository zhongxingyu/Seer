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
import org.eclipse.emf.emfstore.server.model.ModelFactory;
 
 /**
 * This is the item provider adapter for a {@link org.eclipse.emf.emfstore.internal.client.model.ServerInfo} object. <!--
  * begin-user-doc --> <!-- end-user-doc -->
  * 
  * @generated
  */
 public class ServerInfoItemProvider extends ItemProviderAdapter implements IEditingDomainItemProvider,
 	IStructuredItemContentProvider, ITreeItemContentProvider, IItemLabelProvider, IItemPropertySource {
 	/**
 	 * This constructs an instance from a factory and a notifier. <!--
 	 * begin-user-doc --> <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	public ServerInfoItemProvider(AdapterFactory adapterFactory) {
 		super(adapterFactory);
 	}
 
 	/**
 	 * This returns the property descriptors for the adapted class. <!--
 	 * begin-user-doc --> <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	@Override
 	public List<IItemPropertyDescriptor> getPropertyDescriptors(Object object) {
 		if (itemPropertyDescriptors == null) {
 			super.getPropertyDescriptors(object);
 
 			addNamePropertyDescriptor(object);
 			addUrlPropertyDescriptor(object);
 			addPortPropertyDescriptor(object);
 			addLastUsersessionPropertyDescriptor(object);
 			addCertificateAliasPropertyDescriptor(object);
 		}
 		return itemPropertyDescriptors;
 	}
 
 	/**
 	 * This adds a property descriptor for the Name feature.
 	 * <!-- begin-user-doc
 	 * --> <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	protected void addNamePropertyDescriptor(Object object) {
 		itemPropertyDescriptors.add(createItemPropertyDescriptor(
 			((ComposeableAdapterFactory) adapterFactory).getRootAdapterFactory(), getResourceLocator(),
 			getString("_UI_ServerInfo_name_feature"),
 			getString("_UI_PropertyDescriptor_description", "_UI_ServerInfo_name_feature", "_UI_ServerInfo_type"),
 			ModelPackage.Literals.SERVER_INFO__NAME, true, false, false, ItemPropertyDescriptor.GENERIC_VALUE_IMAGE,
 			null, null));
 	}
 
 	/**
 	 * This adds a property descriptor for the Url feature.
 	 * <!-- begin-user-doc
 	 * --> <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	protected void addUrlPropertyDescriptor(Object object) {
 		itemPropertyDescriptors.add(createItemPropertyDescriptor(
 			((ComposeableAdapterFactory) adapterFactory).getRootAdapterFactory(), getResourceLocator(),
 			getString("_UI_ServerInfo_url_feature"),
 			getString("_UI_PropertyDescriptor_description", "_UI_ServerInfo_url_feature", "_UI_ServerInfo_type"),
 			ModelPackage.Literals.SERVER_INFO__URL, true, false, false, ItemPropertyDescriptor.GENERIC_VALUE_IMAGE,
 			null, null));
 	}
 
 	/**
 	 * This adds a property descriptor for the Port feature.
 	 * <!-- begin-user-doc
 	 * --> <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	protected void addPortPropertyDescriptor(Object object) {
 		itemPropertyDescriptors.add(createItemPropertyDescriptor(
 			((ComposeableAdapterFactory) adapterFactory).getRootAdapterFactory(), getResourceLocator(),
 			getString("_UI_ServerInfo_port_feature"),
 			getString("_UI_PropertyDescriptor_description", "_UI_ServerInfo_port_feature", "_UI_ServerInfo_type"),
 			ModelPackage.Literals.SERVER_INFO__PORT, true, false, false, ItemPropertyDescriptor.INTEGRAL_VALUE_IMAGE,
 			null, null));
 	}
 
 	/**
 	 * This adds a property descriptor for the Last Usersession feature. <!--
 	 * begin-user-doc --> <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	protected void addLastUsersessionPropertyDescriptor(Object object) {
 		itemPropertyDescriptors.add(createItemPropertyDescriptor(
 			((ComposeableAdapterFactory) adapterFactory).getRootAdapterFactory(),
 			getResourceLocator(),
 			getString("_UI_ServerInfo_lastUsersession_feature"),
 			getString("_UI_PropertyDescriptor_description", "_UI_ServerInfo_lastUsersession_feature",
 				"_UI_ServerInfo_type"), ModelPackage.Literals.SERVER_INFO__LAST_USERSESSION, true, false, true, null,
 			null, null));
 	}
 
 	/**
 	 * This adds a property descriptor for the Certificate Alias feature. <!--
 	 * begin-user-doc --> <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	protected void addCertificateAliasPropertyDescriptor(Object object) {
 		itemPropertyDescriptors.add(createItemPropertyDescriptor(
 			((ComposeableAdapterFactory) adapterFactory).getRootAdapterFactory(),
 			getResourceLocator(),
 			getString("_UI_ServerInfo_certificateAlias_feature"),
 			getString("_UI_PropertyDescriptor_description", "_UI_ServerInfo_certificateAlias_feature",
 				"_UI_ServerInfo_type"), ModelPackage.Literals.SERVER_INFO__CERTIFICATE_ALIAS, true, false, false,
 			ItemPropertyDescriptor.GENERIC_VALUE_IMAGE, null, null));
 	}
 
 	/**
 	 * This specifies how to implement {@link #getChildren} and is used to deduce an appropriate feature for an
 	 * {@link org.eclipse.emf.edit.command.AddCommand}, {@link org.eclipse.emf.edit.command.RemoveCommand} or
 	 * {@link org.eclipse.emf.edit.command.MoveCommand} in {@link #createCommand}.
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	@Override
 	public Collection<? extends EStructuralFeature> getChildrenFeatures(Object object) {
 		if (childrenFeatures == null) {
 			super.getChildrenFeatures(object);
 			childrenFeatures.add(ModelPackage.Literals.SERVER_INFO__PROJECT_INFOS);
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
 	 * This returns ServerInfo.gif. <!-- begin-user-doc --> <!-- end-user-doc
 	 * -->
 	 * 
 	 * @generated
 	 */
 	@Override
 	public Object getImage(Object object) {
 		return overlayImage(object, getResourceLocator().getImage("full/obj16/ServerInfo"));
 	}
 
 	/**
 	 * This returns the label text for the adapted class.
 	 * <!-- begin-user-doc
 	 * --> <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	@Override
 	public String getText(Object object) {
 		String label = ((ServerInfo) object).getName();
 		return label == null || label.length() == 0 ? getString("_UI_ServerInfo_type")
 			: getString("_UI_ServerInfo_type") + " " + label;
 	}
 
 	/**
 	 * This handles model notifications by calling {@link #updateChildren} to update any cached
 	 * children and by creating a viewer notification, which it passes to {@link #fireNotifyChanged}.
 	 * <!-- begin-user-doc --> <!--
 	 * end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	@Override
 	public void notifyChanged(Notification notification) {
 		updateChildren(notification);
 
 		switch (notification.getFeatureID(ServerInfo.class)) {
 		case ModelPackage.SERVER_INFO__NAME:
 		case ModelPackage.SERVER_INFO__URL:
 		case ModelPackage.SERVER_INFO__PORT:
 		case ModelPackage.SERVER_INFO__CERTIFICATE_ALIAS:
 			fireNotifyChanged(new ViewerNotification(notification, notification.getNotifier(), false, true));
 			return;
 		case ModelPackage.SERVER_INFO__PROJECT_INFOS:
 			fireNotifyChanged(new ViewerNotification(notification, notification.getNotifier(), true, false));
 			return;
 		}
 		super.notifyChanged(notification);
 	}
 
 	/**
 	 * This adds {@link org.eclipse.emf.edit.command.CommandParameter}s
 	 * describing the children that can be created under this object. <!--
 	 * begin-user-doc --> <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	@Override
 	protected void collectNewChildDescriptors(Collection<Object> newChildDescriptors, Object object) {
 		super.collectNewChildDescriptors(newChildDescriptors, object);
 
 		newChildDescriptors.add(createChildParameter(ModelPackage.Literals.SERVER_INFO__PROJECT_INFOS,
 			ModelFactory.eINSTANCE.createProjectInfo()));
 	}
 
 	/**
 	 * Return the resource locator for this item provider's resources. <!--
 	 * begin-user-doc --> <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	@Override
 	public ResourceLocator getResourceLocator() {
 		return ClientModelEditPlugin.INSTANCE;
 	}
 
 }
