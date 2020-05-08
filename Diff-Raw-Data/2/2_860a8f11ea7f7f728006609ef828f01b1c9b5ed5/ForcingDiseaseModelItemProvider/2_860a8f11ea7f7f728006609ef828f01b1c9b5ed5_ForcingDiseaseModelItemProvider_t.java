 package org.eclipse.stem.diseasemodels.forcing.provider;
 
 /*******************************************************************************
  * Copyright (c) 2009 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     IBM Corporation - initial API and implementation
  *******************************************************************************/
 
 
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
 import org.eclipse.emf.edit.provider.ItemPropertyDescriptor;
 import org.eclipse.emf.edit.provider.ViewerNotification;
 
 import org.eclipse.stem.diseasemodels.forcing.ForcingDiseaseModel;
 import org.eclipse.stem.diseasemodels.forcing.ForcingPackage;
 
 import org.eclipse.stem.diseasemodels.standard.provider.StochasticSIRDiseaseModelItemProvider;
 
 /**
  * This is the item provider adapter for a {@link org.eclipse.stem.diseasemodels.forcing.ForcingDiseaseModel} object.
  * <!-- begin-user-doc -->
  * <!-- end-user-doc -->
  * @generated
  */
 @SuppressWarnings("unused") 
 public class ForcingDiseaseModelItemProvider
 	extends StochasticSIRDiseaseModelItemProvider
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
 	public ForcingDiseaseModelItemProvider(AdapterFactory adapterFactory) {
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
 
 			addSeasonalModulationExponentPropertyDescriptor(object);
 			addModulationPeriodPropertyDescriptor(object);
 			addModulationPhaseShiftPropertyDescriptor(object);
 			addSeasonalModulationFloorPropertyDescriptor(object);
 		}
 		return itemPropertyDescriptors;
 	}
 
 	/**
 	 * This adds a property descriptor for the Seasonal Modulation Exponent feature.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	protected void addSeasonalModulationExponentPropertyDescriptor(Object object) {
 		itemPropertyDescriptors.add
 			(createItemPropertyDescriptor
 				(((ComposeableAdapterFactory)adapterFactory).getRootAdapterFactory(),
 				 getResourceLocator(),
 				 getString("_UI_ForcingDiseaseModel_seasonalModulationExponent_feature"), //$NON-NLS-1$
 				 getString("_UI_PropertyDescriptor_description", "_UI_ForcingDiseaseModel_seasonalModulationExponent_feature", "_UI_ForcingDiseaseModel_type"), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
 				 ForcingPackage.Literals.FORCING_DISEASE_MODEL__SEASONAL_MODULATION_EXPONENT,
 				 true,
 				 false,
 				 false,
 				 ItemPropertyDescriptor.REAL_VALUE_IMAGE,
 				 null,
 				 null));
 	}
 
 	/**
 	 * This adds a property descriptor for the Modulation Period feature.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	protected void addModulationPeriodPropertyDescriptor(Object object) {
 		itemPropertyDescriptors.add
 			(createItemPropertyDescriptor
 				(((ComposeableAdapterFactory)adapterFactory).getRootAdapterFactory(),
 				 getResourceLocator(),
 				 getString("_UI_ForcingDiseaseModel_modulationPeriod_feature"), //$NON-NLS-1$
 				 getString("_UI_PropertyDescriptor_description", "_UI_ForcingDiseaseModel_modulationPeriod_feature", "_UI_ForcingDiseaseModel_type"), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
 				 ForcingPackage.Literals.FORCING_DISEASE_MODEL__MODULATION_PERIOD,
 				 true,
 				 false,
 				 false,
 				 ItemPropertyDescriptor.REAL_VALUE_IMAGE,
 				 null,
 				 null));
 	}
 
 	/**
 	 * This adds a property descriptor for the Modulation Phase Shift feature.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	protected void addModulationPhaseShiftPropertyDescriptor(Object object) {
 		itemPropertyDescriptors.add
 			(createItemPropertyDescriptor
 				(((ComposeableAdapterFactory)adapterFactory).getRootAdapterFactory(),
 				 getResourceLocator(),
 				 getString("_UI_ForcingDiseaseModel_modulationPhaseShift_feature"), //$NON-NLS-1$
 				 getString("_UI_PropertyDescriptor_description", "_UI_ForcingDiseaseModel_modulationPhaseShift_feature", "_UI_ForcingDiseaseModel_type"), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
 				 ForcingPackage.Literals.FORCING_DISEASE_MODEL__MODULATION_PHASE_SHIFT,
 				 true,
 				 false,
 				 false,
 				 ItemPropertyDescriptor.REAL_VALUE_IMAGE,
 				 null,
 				 null));
 	}
 
 	/**
 	 * This adds a property descriptor for the Seasonal Modulation Floor feature.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	protected void addSeasonalModulationFloorPropertyDescriptor(Object object) {
 		itemPropertyDescriptors.add
 			(createItemPropertyDescriptor
 				(((ComposeableAdapterFactory)adapterFactory).getRootAdapterFactory(),
 				 getResourceLocator(),
 				 getString("_UI_ForcingDiseaseModel_seasonalModulationFloor_feature"), //$NON-NLS-1$
 				 getString("_UI_PropertyDescriptor_description", "_UI_ForcingDiseaseModel_seasonalModulationFloor_feature", "_UI_ForcingDiseaseModel_type"), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
 				 ForcingPackage.Literals.FORCING_DISEASE_MODEL__SEASONAL_MODULATION_FLOOR,
 				 true,
 				 false,
 				 false,
 				 ItemPropertyDescriptor.REAL_VALUE_IMAGE,
 				 null,
 				 null));
 	}
 
 	/**
 	 * This returns ForcingDiseaseModel.gif.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	@Override
 	public Object getImage(Object object) {
 		return overlayImage(object, getResourceLocator().getImage("full/obj16/ForcingDiseaseModel")); //$NON-NLS-1$
 	}
 
 	/**
 	 * This returns the label text for the adapted class.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated NOT
 	 */
 	@Override
 	public String getText(Object object) {
 		String label = ((ForcingDiseaseModel)object).getURI().lastSegment();
 		return label == null || label.length() == 0 ?
 			getString("_UI_ForcingDiseaseModel_type") : //$NON-NLS-1$
			label; //$NON-NLS-1$ //$NON-NLS-2$
 	}
 
 	/**
 	 * This returns the label text for the adapted class.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	@Override
 	public String getTextGen(Object object) {
 		String label = ((ForcingDiseaseModel)object).getDiseaseName();
 		return label == null || label.length() == 0 ?
 			getString("_UI_ForcingDiseaseModel_type") : //$NON-NLS-1$
 			getString("_UI_ForcingDiseaseModel_type") + " " + label; //$NON-NLS-1$ //$NON-NLS-2$
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
 
 		switch (notification.getFeatureID(ForcingDiseaseModel.class)) {
 			case ForcingPackage.FORCING_DISEASE_MODEL__SEASONAL_MODULATION_EXPONENT:
 			case ForcingPackage.FORCING_DISEASE_MODEL__MODULATION_PERIOD:
 			case ForcingPackage.FORCING_DISEASE_MODEL__MODULATION_PHASE_SHIFT:
 			case ForcingPackage.FORCING_DISEASE_MODEL__SEASONAL_MODULATION_FLOOR:
 				fireNotifyChanged(new ViewerNotification(notification, notification.getNotifier(), false, true));
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
 		return ForcingEditPlugin.INSTANCE;
 	}
 
 }
