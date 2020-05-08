 /*******************************************************************************
  * Copyright (c) 2011 Formal Mind GmbH and University of Dusseldorf.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  *     Michael Jastram - initial API and implementation
  ******************************************************************************/
 
 package org.eclipse.rmf.pror.reqif10.provider;
 
 
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
 import org.eclipse.rmf.reqif10.Identifiable;
 import org.eclipse.rmf.reqif10.ReqIF10Package;
 
 /**
  * This is the item provider adapter for a {@link org.eclipse.rmf.pror.reqif10.Identifiable} object.
  * <!-- begin-user-doc -->
  * <!-- end-user-doc -->
  * @generated
  */
 public class IdentifiableItemProvider
 	extends ItemProviderAdapter
 	implements
 		IEditingDomainItemProvider,
 		IStructuredItemContentProvider,
 		ITreeItemContentProvider,
 		IItemLabelProvider,
 		IItemPropertySource {
 	/**
 	 * This constructs an instance from a factory and a notifier.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public IdentifiableItemProvider(AdapterFactory adapterFactory) {
 		super(adapterFactory);
 	}
 
 	/**
 	 * This returns the property descriptors for the adapted class.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	@Override
 	public List<IItemPropertyDescriptor> getPropertyDescriptors(Object object) {
 		if (itemPropertyDescriptors == null) {
 			super.getPropertyDescriptors(object);
 
 			addDescPropertyDescriptor(object);
 			addIdentifierPropertyDescriptor(object);
 			addLastChangePropertyDescriptor(object);
 			addLongNamePropertyDescriptor(object);
 		}
 		return itemPropertyDescriptors;
 	}
 
 	/**
 	 * This adds a property descriptor for the Desc feature.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	protected void addDescPropertyDescriptor(Object object) {
 		itemPropertyDescriptors.add
 			(createItemPropertyDescriptor
 				(((ComposeableAdapterFactory)adapterFactory).getRootAdapterFactory(),
 				 getResourceLocator(),
 				 getString("_UI_Identifiable_desc_feature"),
 				 getString("_UI_PropertyDescriptor_description", "_UI_Identifiable_desc_feature", "_UI_Identifiable_type"),
 				 ReqIF10Package.Literals.IDENTIFIABLE__DESC,
 				 true,
 				 false,
 				 false,
 				 ItemPropertyDescriptor.GENERIC_VALUE_IMAGE,
 				 null,
 				 null));
 	}
 
 	/**
 	 * This adds a property descriptor for the Identifier feature.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
	 * @generated
 	 */
 	protected void addIdentifierPropertyDescriptor(Object object) {
 		itemPropertyDescriptors.add
 			(createItemPropertyDescriptor
 				(((ComposeableAdapterFactory)adapterFactory).getRootAdapterFactory(),
 				 getResourceLocator(),
 				 getString("_UI_Identifiable_identifier_feature"),
 				 getString("_UI_PropertyDescriptor_description", "_UI_Identifiable_identifier_feature", "_UI_Identifiable_type"),
 				 ReqIF10Package.Literals.IDENTIFIABLE__IDENTIFIER,
				 true,
 				 false,
 				 false,
 				 ItemPropertyDescriptor.GENERIC_VALUE_IMAGE,
 				 null,
 				 null));
 	}
 
 	/**
 	 * This adds a property descriptor for the Last Change feature.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
	 * @generated
 	 */
 	protected void addLastChangePropertyDescriptor(Object object) {
 		itemPropertyDescriptors.add
 			(createItemPropertyDescriptor
 				(((ComposeableAdapterFactory)adapterFactory).getRootAdapterFactory(),
 				 getResourceLocator(),
 				 getString("_UI_Identifiable_lastChange_feature"),
 				 getString("_UI_PropertyDescriptor_description", "_UI_Identifiable_lastChange_feature", "_UI_Identifiable_type"),
 				 ReqIF10Package.Literals.IDENTIFIABLE__LAST_CHANGE,
				 true,
 				 false,
 				 false,
 				 ItemPropertyDescriptor.GENERIC_VALUE_IMAGE,
 				 null,
 				 null));
 	}
 
 	/**
 	 * This adds a property descriptor for the Long Name feature.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	protected void addLongNamePropertyDescriptor(Object object) {
 		itemPropertyDescriptors.add
 			(createItemPropertyDescriptor
 				(((ComposeableAdapterFactory)adapterFactory).getRootAdapterFactory(),
 				 getResourceLocator(),
 				 getString("_UI_Identifiable_longName_feature"),
 				 getString("_UI_PropertyDescriptor_description", "_UI_Identifiable_longName_feature", "_UI_Identifiable_type"),
 				 ReqIF10Package.Literals.IDENTIFIABLE__LONG_NAME,
 				 true,
 				 false,
 				 false,
 				 ItemPropertyDescriptor.GENERIC_VALUE_IMAGE,
 				 null,
 				 null));
 	}
 
 	/**
 	 * This specifies how to implement {@link #getChildren} and is used to deduce an appropriate feature for an
 	 * {@link org.eclipse.emf.edit.command.AddCommand}, {@link org.eclipse.emf.edit.command.RemoveCommand} or
 	 * {@link org.eclipse.emf.edit.command.MoveCommand} in {@link #createCommand}.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	@Override
 	public Collection<? extends EStructuralFeature> getChildrenFeatures(Object object) {
 		if (childrenFeatures == null) {
 			super.getChildrenFeatures(object);
 			childrenFeatures.add(ReqIF10Package.Literals.IDENTIFIABLE__ALTERNATIVE_ID);
 		}
 		return childrenFeatures;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	@Override
 	protected EStructuralFeature getChildFeature(Object object, Object child) {
 		// Check the type of the specified child object and return the proper feature to use for
 		// adding (see {@link AddCommand}) it as a child.
 
 		return super.getChildFeature(object, child);
 	}
 
 	/**
 	 * Missing image to prevent {@link NullPointerException} when accidentally building a command.
 	 */
 	@Override
 	public Object getImage(Object object) {
 		return overlayImage(object, getResourceLocator().getImage("full/obj16/Missing.png"));
 	}
 
 	/**
 	 * This returns the label text for the adapted class.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	@Override
 	public String getText(Object object) {
 		String label = ((Identifiable)object).getLongName();
 		return label == null || label.length() == 0 ?
 			getString("_UI_Identifiable_type") :
 			getString("_UI_Identifiable_type") + " " + label;
 	}
 
 	/**
 	 * This handles model notifications by calling {@link #updateChildren} to update any cached
 	 * children and by creating a viewer notification, which it passes to {@link #fireNotifyChanged}.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	@Override
 	public void notifyChanged(Notification notification) {
 		updateChildren(notification);
 
 		switch (notification.getFeatureID(Identifiable.class)) {
 			case ReqIF10Package.IDENTIFIABLE__DESC:
 			case ReqIF10Package.IDENTIFIABLE__IDENTIFIER:
 			case ReqIF10Package.IDENTIFIABLE__LAST_CHANGE:
 			case ReqIF10Package.IDENTIFIABLE__LONG_NAME:
 				fireNotifyChanged(new ViewerNotification(notification, notification.getNotifier(), false, true));
 				return;
 			case ReqIF10Package.IDENTIFIABLE__ALTERNATIVE_ID:
 				fireNotifyChanged(new ViewerNotification(notification, notification.getNotifier(), true, false));
 				return;
 		}
 		super.notifyChanged(notification);
 	}
 
 	/**
 	 * This adds {@link org.eclipse.emf.edit.command.CommandParameter}s describing the children
 	 * that can be created under this object.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	@Override
 	protected void collectNewChildDescriptors(Collection<Object> newChildDescriptors, Object object) {
 		super.collectNewChildDescriptors(newChildDescriptors, object);
 	}
 
 	/**
 	 * Return the resource locator for this item provider's resources.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	@Override
 	public ResourceLocator getResourceLocator() {
 		return Reqif10EditPlugin.INSTANCE;
 	}
 
 }
