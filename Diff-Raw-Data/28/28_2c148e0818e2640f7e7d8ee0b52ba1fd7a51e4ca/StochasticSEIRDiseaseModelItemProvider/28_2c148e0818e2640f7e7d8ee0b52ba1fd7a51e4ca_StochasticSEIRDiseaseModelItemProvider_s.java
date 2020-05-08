 package org.eclipse.stem.diseasemodels.standard.provider;
 
 /*******************************************************************************
  * Copyright (c) 2006, 2008 IBM Corporation and others.
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
 //import org.eclipse.emf.common.util.URI;
 import org.eclipse.emf.edit.provider.IEditingDomainItemProvider;
 import org.eclipse.emf.edit.provider.IItemLabelProvider;
 import org.eclipse.emf.edit.provider.IItemPropertyDescriptor;
 import org.eclipse.emf.edit.provider.IItemPropertySource;
 import org.eclipse.emf.edit.provider.IStructuredItemContentProvider;
 import org.eclipse.emf.edit.provider.ITreeItemContentProvider;
 import org.eclipse.emf.edit.provider.ItemPropertyDescriptor;
 import org.eclipse.emf.edit.provider.ViewerNotification;
 import org.eclipse.stem.diseasemodels.standard.StandardPackage;
 import org.eclipse.stem.diseasemodels.standard.StochasticSEIRDiseaseModel;
 
 /**
  * This is the item provider adapter for a {@link org.eclipse.stem.diseasemodels.standard.StochasticSEIRDiseaseModel} object.
  * <!-- begin-user-doc --> <!-- end-user-doc -->
  * @generated
  */
 public class StochasticSEIRDiseaseModelItemProvider extends SEIRItemProvider
 		implements IEditingDomainItemProvider, IStructuredItemContentProvider,
 		ITreeItemContentProvider, IItemLabelProvider, IItemPropertySource {
 	/**
 	 * This constructs an instance from a factory and a notifier. <!--
 	 * begin-user-doc -->
 	 * 
 	 * @param adapterFactory
 	 * 
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public StochasticSEIRDiseaseModelItemProvider(AdapterFactory adapterFactory) {
 		super(adapterFactory);
 	}
 
 	/**
 	 * This returns the property descriptors for the adapted class. <!--
 	 * begin-user-doc -->
 	 * 
 	 * @param object
 	 * @return
 	 * 
 	 * <!-- end-user-doc -->
 	 * @generated NOT
 	 */
 	@Override
 	public List<IItemPropertyDescriptor> getPropertyDescriptors(Object object) {
 		if (itemPropertyDescriptors == null) {
 			super.getPropertyDescriptors(object);
 
 			addSeedPropertyDescriptor(object);
 			//addRandomGeneratorPropertyDescriptor(object);
 			addGainPropertyDescriptor(object);
 		}
 		return itemPropertyDescriptors;
 	}
 
 	/**
 	 * This returns the property descriptors for the adapted class. <!--
 	 * begin-user-doc -->
 	 * 
 	 * @param object
 	 * @return
 	 * 
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public List<IItemPropertyDescriptor> getPropertyDescriptorsGen(Object object) {
 		if (itemPropertyDescriptors == null) {
 			super.getPropertyDescriptors(object);
 
 			addSeedPropertyDescriptor(object);
 			addRandomGeneratorPropertyDescriptor(object);
 			addGainPropertyDescriptor(object);
 		}
 		return itemPropertyDescriptors;
 	}
 	
 	/**
 	 * This adds a property descriptor for the Seed feature.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	protected void addSeedPropertyDescriptor(Object object) {
 		itemPropertyDescriptors.add
 			(createItemPropertyDescriptor
 				(((ComposeableAdapterFactory)adapterFactory).getRootAdapterFactory(),
 				 getResourceLocator(),
 				 getString("_UI_StochasticDiseaseModel_seed_feature"), //$NON-NLS-1$
 				 getString("_UI_PropertyDescriptor_description", "_UI_StochasticDiseaseModel_seed_feature", "_UI_StochasticDiseaseModel_type"), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
 				 StandardPackage.Literals.STOCHASTIC_DISEASE_MODEL__SEED,
 				 true,
 				 false,
 				 false,
 				 ItemPropertyDescriptor.INTEGRAL_VALUE_IMAGE,
 				 null,
 				 null));
 	}
 
 	/**
 	 * This adds a property descriptor for the Random Generator feature.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	protected void addRandomGeneratorPropertyDescriptor(Object object) {
 		itemPropertyDescriptors.add
 			(createItemPropertyDescriptor
 				(((ComposeableAdapterFactory)adapterFactory).getRootAdapterFactory(),
 				 getResourceLocator(),
 				 getString("_UI_StochasticDiseaseModel_randomGenerator_feature"), //$NON-NLS-1$
 				 getString("_UI_PropertyDescriptor_description", "_UI_StochasticDiseaseModel_randomGenerator_feature", "_UI_StochasticDiseaseModel_type"), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
 				 StandardPackage.Literals.STOCHASTIC_DISEASE_MODEL__RANDOM_GENERATOR,
 				 false,
 				 false,
 				 false,
 				 ItemPropertyDescriptor.GENERIC_VALUE_IMAGE,
 				 null,
 				 null));
 	}
 
 	/**
 	 * This adds a property descriptor for the Gain feature.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	protected void addGainPropertyDescriptor(Object object) {
 		itemPropertyDescriptors.add
 			(createItemPropertyDescriptor
 				(((ComposeableAdapterFactory)adapterFactory).getRootAdapterFactory(),
 				 getResourceLocator(),
 				 getString("_UI_StandardStochasticDiseaseModel_gain_feature"), //$NON-NLS-1$
 				 getString("_UI_PropertyDescriptor_description", "_UI_StandardStochasticDiseaseModel_gain_feature", "_UI_StandardStochasticDiseaseModel_type"), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
 				 StandardPackage.Literals.STANDARD_STOCHASTIC_DISEASE_MODEL__GAIN,
 				 true,
 				 false,
 				 false,
 				 ItemPropertyDescriptor.REAL_VALUE_IMAGE,
 				 null,
 				 null));
 	}
 
 	/**
 	 * This returns the label text for the adapted class.
 	 * <!-- begin-user-doc
 	 * -->
 	 * 
 	 * @param object
 	 * @return
 	 * 
 	 * <!-- end-user-doc -->
	 * @generated
 	 */
 	@Override
 	public String getText(Object object) {
		String label = ((StochasticSEIRDiseaseModel)object).getDiseaseName();
 		return label == null || label.length() == 0 ?
 			getString("_UI_StochasticSEIRDiseaseModel_type") : //$NON-NLS-1$
			getString("_UI_StochasticSEIRDiseaseModel_type") + " " + label; //$NON-NLS-1$ //$NON-NLS-2$
 	}
 
 	/**
 	 * This handles model notifications by calling {@link #updateChildren} to update any cached
 	 * children and by creating a viewer notification, which it passes to {@link #fireNotifyChanged}.
 	 * <!-- begin-user-doc -->
 	 * 
 	 * @param notification
 	 * 
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	@Override
 	public void notifyChanged(Notification notification) {
 		updateChildren(notification);
 
 		switch (notification.getFeatureID(StochasticSEIRDiseaseModel.class)) {
 			case StandardPackage.STOCHASTIC_SEIR_DISEASE_MODEL__SEED:
 			case StandardPackage.STOCHASTIC_SEIR_DISEASE_MODEL__RANDOM_GENERATOR:
 			case StandardPackage.STOCHASTIC_SEIR_DISEASE_MODEL__GAIN:
 				fireNotifyChanged(new ViewerNotification(notification, notification.getNotifier(), false, true));
 				return;
 		}
 		super.notifyChanged(notification);
 	}
 
 	/**
 	 * This adds {@link org.eclipse.emf.edit.command.CommandParameter}s describing the children
 	 * that can be created under this object.
 	 * <!-- begin-user-doc
 	 * --> <!-- end-user-doc -->
 	 * @generated
 	 */
 	@Override
 	protected void collectNewChildDescriptors(Collection<Object> newChildDescriptors, Object object) {
 		super.collectNewChildDescriptors(newChildDescriptors, object);
 	}
 
 }
