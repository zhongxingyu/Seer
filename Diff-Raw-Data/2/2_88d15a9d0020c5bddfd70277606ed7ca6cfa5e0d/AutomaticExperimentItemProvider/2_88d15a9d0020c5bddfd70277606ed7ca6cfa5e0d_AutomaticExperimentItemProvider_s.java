 package automaticexperiment.provider;
 
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
 import org.eclipse.emf.common.util.URI;
 import org.eclipse.emf.ecore.EStructuralFeature;
 import org.eclipse.emf.edit.provider.ComposeableAdapterFactory;
 import org.eclipse.emf.edit.provider.IEditingDomainItemProvider;
 import org.eclipse.emf.edit.provider.IItemLabelProvider;
 import org.eclipse.emf.edit.provider.IItemPropertyDescriptor;
 import org.eclipse.emf.edit.provider.IItemPropertySource;
 import org.eclipse.emf.edit.provider.IStructuredItemContentProvider;
 import org.eclipse.emf.edit.provider.ITreeItemContentProvider;
 import org.eclipse.emf.edit.provider.ItemPropertyDescriptor;
 import org.eclipse.emf.edit.provider.ViewerNotification;
 import org.eclipse.stem.core.common.provider.IdentifiableItemProvider;
 
 import automaticexperiment.AutomaticExperiment;
 import automaticexperiment.AutomaticexperimentPackage;
 
 /**
  * This is the item provider adapter for a {@link automaticexperiment.AutomaticExperiment} object.
  * <!-- begin-user-doc -->
  * <!-- end-user-doc -->
  * @generated
  */
 public class AutomaticExperimentItemProvider
 	extends IdentifiableItemProvider
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
 	public AutomaticExperimentItemProvider(AdapterFactory adapterFactory) {
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
 
 			addBaseScenarioPropertyDescriptor(object);
 			addParametersPropertyDescriptor(object);
 			addErrorAnalysisAlgorithmPropertyDescriptor(object);
 			addErrorFunctionPropertyDescriptor(object);
 			addTolerancePropertyDescriptor(object);
 			addReferanceDataDirPropertyDescriptor(object);
 			addMaximumNumberOfIterationsPropertyDescriptor(object);
 			addReInitPropertyDescriptor(object);
 		}
 		return itemPropertyDescriptors;
 	}
 
 	/**
 	 * This adds a property descriptor for the Base Scenario feature.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	protected void addBaseScenarioPropertyDescriptor(Object object) {
 		itemPropertyDescriptors.add
 			(createItemPropertyDescriptor
 				(((ComposeableAdapterFactory)adapterFactory).getRootAdapterFactory(),
 				 getResourceLocator(),
 				 getString("_UI_AutomaticExperiment_baseScenario_feature"),
 				 getString("_UI_PropertyDescriptor_description", "_UI_AutomaticExperiment_baseScenario_feature", "_UI_AutomaticExperiment_type"),
 				 AutomaticexperimentPackage.Literals.AUTOMATIC_EXPERIMENT__BASE_SCENARIO,
 				 true,
 				 false,
 				 true,
 				 null,
 				 null,
 				 null));
 	}
 
 	/**
 	 * This adds a property descriptor for the Parameters feature.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	protected void addParametersPropertyDescriptor(Object object) {
 		itemPropertyDescriptors.add
 			(createItemPropertyDescriptor
 				(((ComposeableAdapterFactory)adapterFactory).getRootAdapterFactory(),
 				 getResourceLocator(),
 				 getString("_UI_AutomaticExperiment_parameters_feature"),
 				 getString("_UI_PropertyDescriptor_description", "_UI_AutomaticExperiment_parameters_feature", "_UI_AutomaticExperiment_type"),
 				 AutomaticexperimentPackage.Literals.AUTOMATIC_EXPERIMENT__PARAMETERS,
 				 true,
 				 false,
 				 true,
 				 null,
 				 null,
 				 null));
 	}
 
 	/**
 	 * This adds a property descriptor for the Error Analysis Algorithm feature.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	protected void addErrorAnalysisAlgorithmPropertyDescriptor(Object object) {
 		itemPropertyDescriptors.add
 			(createItemPropertyDescriptor
 				(((ComposeableAdapterFactory)adapterFactory).getRootAdapterFactory(),
 				 getResourceLocator(),
 				 getString("_UI_AutomaticExperiment_errorAnalysisAlgorithm_feature"),
 				 getString("_UI_PropertyDescriptor_description", "_UI_AutomaticExperiment_errorAnalysisAlgorithm_feature", "_UI_AutomaticExperiment_type"),
 				 AutomaticexperimentPackage.Literals.AUTOMATIC_EXPERIMENT__ERROR_ANALYSIS_ALGORITHM,
 				 true,
 				 false,
 				 false,
 				 ItemPropertyDescriptor.GENERIC_VALUE_IMAGE,
 				 null,
 				 null));
 	}
 
 	/**
 	 * This adds a property descriptor for the Error Function feature.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	protected void addErrorFunctionPropertyDescriptor(Object object) {
 		itemPropertyDescriptors.add
 			(createItemPropertyDescriptor
 				(((ComposeableAdapterFactory)adapterFactory).getRootAdapterFactory(),
 				 getResourceLocator(),
 				 getString("_UI_AutomaticExperiment_errorFunction_feature"),
 				 getString("_UI_PropertyDescriptor_description", "_UI_AutomaticExperiment_errorFunction_feature", "_UI_AutomaticExperiment_type"),
 				 AutomaticexperimentPackage.Literals.AUTOMATIC_EXPERIMENT__ERROR_FUNCTION,
 				 true,
 				 false,
 				 false,
 				 ItemPropertyDescriptor.GENERIC_VALUE_IMAGE,
 				 null,
 				 null));
 	}
 
 	/**
 	 * This adds a property descriptor for the Tolerance feature.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	protected void addTolerancePropertyDescriptor(Object object) {
 		itemPropertyDescriptors.add
 			(createItemPropertyDescriptor
 				(((ComposeableAdapterFactory)adapterFactory).getRootAdapterFactory(),
 				 getResourceLocator(),
 				 getString("_UI_AutomaticExperiment_tolerance_feature"),
 				 getString("_UI_PropertyDescriptor_description", "_UI_AutomaticExperiment_tolerance_feature", "_UI_AutomaticExperiment_type"),
 				 AutomaticexperimentPackage.Literals.AUTOMATIC_EXPERIMENT__TOLERANCE,
 				 true,
 				 false,
 				 false,
 				 ItemPropertyDescriptor.REAL_VALUE_IMAGE,
 				 null,
 				 null));
 	}
 
 	/**
 	 * This adds a property descriptor for the Referance Data Dir feature.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	protected void addReferanceDataDirPropertyDescriptor(Object object) {
 		itemPropertyDescriptors.add
 			(createItemPropertyDescriptor
 				(((ComposeableAdapterFactory)adapterFactory).getRootAdapterFactory(),
 				 getResourceLocator(),
 				 getString("_UI_AutomaticExperiment_referanceDataDir_feature"),
 				 getString("_UI_PropertyDescriptor_description", "_UI_AutomaticExperiment_referanceDataDir_feature", "_UI_AutomaticExperiment_type"),
 				 AutomaticexperimentPackage.Literals.AUTOMATIC_EXPERIMENT__REFERANCE_DATA_DIR,
 				 true,
 				 false,
 				 false,
 				 ItemPropertyDescriptor.GENERIC_VALUE_IMAGE,
 				 null,
 				 null));
 	}
 
 	/**
 	 * This adds a property descriptor for the Maximum Number Of Iterations feature.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	protected void addMaximumNumberOfIterationsPropertyDescriptor(Object object) {
 		itemPropertyDescriptors.add
 			(createItemPropertyDescriptor
 				(((ComposeableAdapterFactory)adapterFactory).getRootAdapterFactory(),
 				 getResourceLocator(),
 				 getString("_UI_AutomaticExperiment_maximumNumberOfIterations_feature"),
 				 getString("_UI_PropertyDescriptor_description", "_UI_AutomaticExperiment_maximumNumberOfIterations_feature", "_UI_AutomaticExperiment_type"),
 				 AutomaticexperimentPackage.Literals.AUTOMATIC_EXPERIMENT__MAXIMUM_NUMBER_OF_ITERATIONS,
 				 true,
 				 false,
 				 false,
 				 ItemPropertyDescriptor.INTEGRAL_VALUE_IMAGE,
 				 null,
 				 null));
 	}
 
 	/**
 	 * This adds a property descriptor for the Re Init feature.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	protected void addReInitPropertyDescriptor(Object object) {
 		itemPropertyDescriptors.add
 			(createItemPropertyDescriptor
 				(((ComposeableAdapterFactory)adapterFactory).getRootAdapterFactory(),
 				 getResourceLocator(),
 				 getString("_UI_AutomaticExperiment_reInit_feature"),
 				 getString("_UI_PropertyDescriptor_description", "_UI_AutomaticExperiment_reInit_feature", "_UI_AutomaticExperiment_type"),
 				 AutomaticexperimentPackage.Literals.AUTOMATIC_EXPERIMENT__RE_INIT,
 				 true,
 				 false,
 				 false,
 				 ItemPropertyDescriptor.BOOLEAN_VALUE_IMAGE,
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
 			childrenFeatures.add(AutomaticexperimentPackage.Literals.AUTOMATIC_EXPERIMENT__BASE_SCENARIO);
 			childrenFeatures.add(AutomaticexperimentPackage.Literals.AUTOMATIC_EXPERIMENT__PARAMETERS);
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
 	 * This returns the label text for the adapted class.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated NOT
 	 */
 	@Override
 	public String getText(Object object) {
 		String label = ((AutomaticExperiment)object).getURI().lastSegment();
 		return label == null || label.length() == 0 ?
 			getString("_UI_AutomaticExperiment_type") :
			getString("_UI_AutomaticExperiment_type") + " " + label;
 	}
 
 	/**
 	 * This returns the label text for the adapted class.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	@Override
 	public String getTextGen(Object object) {
 		URI labelValue = ((AutomaticExperiment)object).getURI();
 		String label = labelValue == null ? null : labelValue.toString();
 		return label == null || label.length() == 0 ?
 			getString("_UI_AutomaticExperiment_type") :
 			getString("_UI_AutomaticExperiment_type") + " " + label;
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
 
 		switch (notification.getFeatureID(AutomaticExperiment.class)) {
 			case AutomaticexperimentPackage.AUTOMATIC_EXPERIMENT__ERROR_ANALYSIS_ALGORITHM:
 			case AutomaticexperimentPackage.AUTOMATIC_EXPERIMENT__ERROR_FUNCTION:
 			case AutomaticexperimentPackage.AUTOMATIC_EXPERIMENT__TOLERANCE:
 			case AutomaticexperimentPackage.AUTOMATIC_EXPERIMENT__REFERANCE_DATA_DIR:
 			case AutomaticexperimentPackage.AUTOMATIC_EXPERIMENT__MAXIMUM_NUMBER_OF_ITERATIONS:
 			case AutomaticexperimentPackage.AUTOMATIC_EXPERIMENT__RE_INIT:
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
 		return AutomaticexperienceEditPlugin.INSTANCE;
 	}
 
 }
