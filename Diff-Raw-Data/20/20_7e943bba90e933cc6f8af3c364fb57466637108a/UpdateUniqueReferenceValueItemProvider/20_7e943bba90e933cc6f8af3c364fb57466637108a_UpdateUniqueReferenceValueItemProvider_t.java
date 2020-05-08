 /*******************************************************************************
  * Copyright (c) 2006, 2007, 2008 Obeo.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  *     Obeo - initial API and implementation
  *******************************************************************************/
 package org.eclipse.emf.compare.diff.provider;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.List;
 
 import org.eclipse.emf.common.notify.AdapterFactory;
 import org.eclipse.emf.common.notify.Notification;
 import org.eclipse.emf.common.util.ResourceLocator;
 import org.eclipse.emf.compare.diff.metamodel.DiffPackage;
 import org.eclipse.emf.compare.diff.metamodel.UpdateUniqueReferenceValue;
 import org.eclipse.emf.compare.util.AdapterUtils;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.edit.provider.ComposeableAdapterFactory;
 import org.eclipse.emf.edit.provider.ComposedImage;
 import org.eclipse.emf.edit.provider.IEditingDomainItemProvider;
 import org.eclipse.emf.edit.provider.IItemLabelProvider;
 import org.eclipse.emf.edit.provider.IItemPropertyDescriptor;
 import org.eclipse.emf.edit.provider.IItemPropertySource;
 import org.eclipse.emf.edit.provider.IStructuredItemContentProvider;
 import org.eclipse.emf.edit.provider.ITreeItemContentProvider;
 
 /**
  * This is the item provider adapter for a
  * {@link org.eclipse.emf.compare.diff.metamodel.UpdateUniqueReferenceValue} object. <!-- begin-user-doc -->
  * <!-- end-user-doc -->
  * 
  * @generated
  */
 public class UpdateUniqueReferenceValueItemProvider extends UpdateReferenceItemProvider implements IEditingDomainItemProvider, IStructuredItemContentProvider, ITreeItemContentProvider, IItemLabelProvider, IItemPropertySource {
 	/**
 	 * This constructs an instance from a factory and a notifier. <!-- begin-user-doc --> <!-- end-user-doc
 	 * -->
 	 * 
 	 * @generated
 	 */
 	@SuppressWarnings("hiding")
 	public UpdateUniqueReferenceValueItemProvider(AdapterFactory adapterFactory) {
 		super(adapterFactory);
 	}
 
 	/**
 	 * This returns UpdateUniqueReferenceValue.gif. <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * 
 	 * @generated NOT
 	 */
 	@Override
 	public Object getImage(Object object) {
 		final UpdateUniqueReferenceValue updateReference = (UpdateUniqueReferenceValue)object;
		Object labelImage;
		if (updateReference.getLeftTarget() == null)
			labelImage = AdapterUtils.getItemProviderImage(updateReference.getRightTarget());
		else
			labelImage = AdapterUtils.getItemProviderImage(updateReference.getLeftTarget());
 
 		if (labelImage != null) {
 			List<Object> images = new ArrayList<Object>(2);
 			images.add(labelImage);
 			images.add(getResourceLocator().getImage("full/obj16/UpdateUniqueReferenceValue")); //$NON-NLS-1$
 			labelImage = new ComposedImage(images);
 		} else {
 			labelImage = getResourceLocator().getImage("full/obj16/UpdateUniqueReferenceValue"); //$NON-NLS-1$
 		}
 
 		return labelImage;
 	}
 
 	/**
 	 * This returns the property descriptors for the adapted class. <!-- begin-user-doc --> <!-- end-user-doc
 	 * -->
 	 * 
 	 * @generated
 	 */
 	@Override
 	public List<IItemPropertyDescriptor> getPropertyDescriptors(Object object) {
 		if (itemPropertyDescriptors == null) {
 			super.getPropertyDescriptors(object);
 
 			addLeftTargetPropertyDescriptor(object);
 			addRightTargetPropertyDescriptor(object);
 		}
 		return itemPropertyDescriptors;
 	}
 
 	/**
 	 * Return the resource locator for this item provider's resources. <!-- begin-user-doc --> <!--
 	 * end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	@Override
 	public ResourceLocator getResourceLocator() {
 		return DiffEditPlugin.INSTANCE;
 	}
 
 	/**
 	 * This returns the label text for the adapted class. <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * 
 	 * @generated NOT
 	 */
 	@Override
 	public String getText(Object object) {
 		final UpdateUniqueReferenceValue updateRef = (UpdateUniqueReferenceValue)object;
 
 		final EObject leftValue = (EObject)updateRef.getLeftElement().eGet(updateRef.getReference());
 		final EObject rightValue = (EObject)updateRef.getRightElement().eGet(updateRef.getReference());
 
 		final String elementLabel = AdapterUtils.getItemProviderText(updateRef.getLeftElement());
 		final String referenceLabel = AdapterUtils.getItemProviderText(updateRef.getReference());
		final String leftValueLabel;
		final String rightValueLabel;
		if (leftValue == null)
			leftValueLabel = "null"; //$NON-NLS-1$
		else
			leftValueLabel = AdapterUtils.getItemProviderText(leftValue);
		if (rightValue == null)
			rightValueLabel = "null"; //$NON-NLS-1$
		else
			rightValueLabel = AdapterUtils.getItemProviderText(rightValue);
		
 
 		if (updateRef.isConflicting())
 			return getString("_UI_UpdateUniqueReferenceValue_conflicting", new Object[] {referenceLabel, //$NON-NLS-1$
 					leftValueLabel, rightValueLabel,});
 		return getString("_UI_UpdateUniqueReferenceValue_type", new Object[] {referenceLabel, elementLabel, //$NON-NLS-1$
 				leftValueLabel, rightValueLabel,});
 	}
 
 	/**
 	 * This handles model notifications by calling {@link #updateChildren} to update any cached children and
 	 * by creating a viewer notification, which it passes to {@link #fireNotifyChanged}. <!-- begin-user-doc
 	 * --> <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	@Override
 	public void notifyChanged(Notification notification) {
 		updateChildren(notification);
 		super.notifyChanged(notification);
 	}
 
 	/**
 	 * This adds a property descriptor for the Left Target feature. <!-- begin-user-doc --> <!-- end-user-doc
 	 * -->
 	 * 
 	 * @generated
 	 */
 	@SuppressWarnings("unused")
 	protected void addLeftTargetPropertyDescriptor(Object object) {
 		itemPropertyDescriptors
 				.add(createItemPropertyDescriptor(
 						((ComposeableAdapterFactory)adapterFactory).getRootAdapterFactory(),
 						getResourceLocator(),
 						getString("_UI_UpdateUniqueReferenceValue_leftTarget_feature"), //$NON-NLS-1$
 						getString(
 								"_UI_PropertyDescriptor_description", "_UI_UpdateUniqueReferenceValue_leftTarget_feature", "_UI_UpdateUniqueReferenceValue_type"), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
 						DiffPackage.Literals.UPDATE_UNIQUE_REFERENCE_VALUE__LEFT_TARGET, true, false, true,
 						null, null, null));
 	}
 
 	/**
 	 * This adds a property descriptor for the Right Target feature. <!-- begin-user-doc --> <!-- end-user-doc
 	 * -->
 	 * 
 	 * @generated
 	 */
 	@SuppressWarnings("unused")
 	protected void addRightTargetPropertyDescriptor(Object object) {
 		itemPropertyDescriptors
 				.add(createItemPropertyDescriptor(
 						((ComposeableAdapterFactory)adapterFactory).getRootAdapterFactory(),
 						getResourceLocator(),
 						getString("_UI_UpdateUniqueReferenceValue_rightTarget_feature"), //$NON-NLS-1$
 						getString(
 								"_UI_PropertyDescriptor_description", "_UI_UpdateUniqueReferenceValue_rightTarget_feature", "_UI_UpdateUniqueReferenceValue_type"), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
 						DiffPackage.Literals.UPDATE_UNIQUE_REFERENCE_VALUE__RIGHT_TARGET, true, false, true,
 						null, null, null));
 	}
 
 	/**
 	 * This adds {@link org.eclipse.emf.edit.command.CommandParameter}s describing the children that can be
 	 * created under this object. <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	@Override
 	protected void collectNewChildDescriptors(Collection<Object> newChildDescriptors, Object object) {
 		super.collectNewChildDescriptors(newChildDescriptors, object);
 	}
 
 }
