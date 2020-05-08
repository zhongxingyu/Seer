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
 
 package org.eclipse.rmf.reqif10.pror.configuration.provider;
 
 
 import java.util.Collection;
 import java.util.List;
 
 import org.eclipse.emf.common.notify.AdapterFactory;
 import org.eclipse.emf.common.notify.Notification;
 import org.eclipse.emf.common.util.ResourceLocator;
 import org.eclipse.emf.edit.provider.ComposeableAdapterFactory;
 import org.eclipse.emf.edit.provider.IEditingDomainItemProvider;
 import org.eclipse.emf.edit.provider.IItemLabelProvider;
 import org.eclipse.emf.edit.provider.IItemPropertyDescriptor;
 import org.eclipse.emf.edit.provider.IItemPropertySource;
 import org.eclipse.emf.edit.provider.IStructuredItemContentProvider;
 import org.eclipse.emf.edit.provider.ITreeItemContentProvider;
 import org.eclipse.emf.edit.provider.ItemProviderAdapter;
import org.eclipse.emf.edit.provider.ViewerNotification;
 import org.eclipse.rmf.reqif10.pror.configuration.ConfigurationPackage;
 import org.eclipse.rmf.reqif10.pror.provider.Reqif10EditPlugin;
 import org.eclipse.rmf.reqif10.pror.util.PresentationEditInterface;
 
 
 /**
  * This is the item provider adapter for a
  * {@link org.eclipse.rmf.reqif10.pror.configuration.ProrPresentationConfiguration}
  * object. <!-- begin-user-doc --> This class also implements
  * {@link PresentationEditInterface} that serves as a hook into the Presentation
  * Framework. To provide services with respect to the editor, also implement
  * PresentationEditorInterface. <!-- end-user-doc -->
  * 
  * @generated NOT
  */
 public abstract class ProrPresentationConfigurationItemProvider
 	extends ItemProviderAdapter
 	implements
 		IEditingDomainItemProvider,
 		IStructuredItemContentProvider,
 		ITreeItemContentProvider,
 		IItemLabelProvider,
 		IItemPropertySource,
 		PresentationEditInterface {
 	/**
 	 * This constructs an instance from a factory and a notifier.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public ProrPresentationConfigurationItemProvider(AdapterFactory adapterFactory) {
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
 
 			addDatatypePropertyDescriptor(object);
 		}
 		return itemPropertyDescriptors;
 	}
 
 	/**
 	 * This adds a property descriptor for the Datatype feature.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	protected void addDatatypePropertyDescriptor(Object object) {
 		itemPropertyDescriptors.add
 			(createItemPropertyDescriptor
 				(((ComposeableAdapterFactory)adapterFactory).getRootAdapterFactory(),
 				 getResourceLocator(),
 				 getString("_UI_ProrPresentationConfiguration_datatype_feature"),
 				 getString("_UI_PropertyDescriptor_description", "_UI_ProrPresentationConfiguration_datatype_feature", "_UI_ProrPresentationConfiguration_type"),
 				 ConfigurationPackage.Literals.PROR_PRESENTATION_CONFIGURATION__DATATYPE,
 				 true,
 				 false,
 				 true,
 				 null,
 				 null,
 				 null));
 	}
 
 	@Override
 	public Object getImage(Object object) {
 		return overlayImage(object, getResourceLocator().getImage("full/obj16/ProrPresentationConfiguration.png"));
 	}
 
 	/**
 	 * This returns the label text for the adapted class.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	@Override
 	public String getText(Object object) {
 		return getString("_UI_ProrPresentationConfiguration_type");
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
 		super.notifyChanged(notification);
		
		// Update the label
		fireNotifyChanged(new ViewerNotification(notification));
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
