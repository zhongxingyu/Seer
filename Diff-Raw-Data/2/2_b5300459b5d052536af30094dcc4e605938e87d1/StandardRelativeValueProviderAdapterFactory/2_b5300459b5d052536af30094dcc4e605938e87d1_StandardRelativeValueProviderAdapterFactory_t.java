 // StandardRelativeValueProviderAdapterFactory.java
 package org.eclipse.stem.ui.populationmodels.adapters;
 
 /*******************************************************************************
  * Copyright (c) 2006 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     IBM Corporation - initial API and implementation
  *******************************************************************************/
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.eclipse.emf.common.notify.Adapter;
 import org.eclipse.emf.common.notify.Notification;
 import org.eclipse.emf.ecore.EStructuralFeature;
 import org.eclipse.emf.edit.provider.ChangeNotifier;
 import org.eclipse.emf.edit.provider.ComposeableAdapterFactory;
 import org.eclipse.emf.edit.provider.ComposedAdapterFactory;
 import org.eclipse.emf.edit.provider.IChangeNotifier;
 import org.eclipse.emf.edit.provider.IItemPropertyDescriptor;
 import org.eclipse.emf.edit.provider.IItemPropertySource;
 import org.eclipse.emf.edit.provider.INotifyChangedListener;
 import org.eclipse.emf.edit.ui.provider.PropertySource;
 import org.eclipse.stem.definitions.adapters.relativevalue.RelativeValueProvider;
 import org.eclipse.stem.definitions.adapters.relativevalue.RelativeValueProviderAdapter;
 import org.eclipse.stem.definitions.adapters.relativevalue.RelativeValueProviderAdapterFactory;
 import org.eclipse.stem.populationmodels.standard.PopulationModelLabel;
 import org.eclipse.stem.populationmodels.standard.PopulationModelLabelValue;
 import org.eclipse.stem.populationmodels.standard.StandardPackage;
 import org.eclipse.stem.populationmodels.standard.StandardPopulationModelLabelValue;
 import org.eclipse.stem.populationmodels.standard.provider.StandardItemProviderAdapterFactory;
 import org.eclipse.stem.populationmodels.standard.util.StandardAdapterFactory;
 import org.eclipse.stem.ui.Activator;
 import org.eclipse.stem.ui.populationmodels.preferences.PreferenceConstants;
 import org.eclipse.stem.ui.populationmodels.preferences.PreferenceInitializer;
 import org.eclipse.ui.IStartup;
 
 /**
  * This class is a factory for this model that creates
  * {@link RelativeValueProvider}'s for classes in the model.
  */
 public class StandardRelativeValueProviderAdapterFactory extends
 		StandardAdapterFactory implements RelativeValueProviderAdapterFactory, IStartup {
 	
 	/**
 	 * This keeps track of the root adapter factory that delegates to this
 	 * adapter factory.
 	 */
 	protected ComposedAdapterFactory parentAdapterFactory;
 
 	/**
 	 * This is used to implement
 	 * {@link org.eclipse.emf.edit.provider.IChangeNotifier}.
 	 */
 	protected IChangeNotifier changeNotifier = new ChangeNotifier();
 
 	/**
 	 * This factory is used to create item providers for the adapted classes.
 	 * The item providers are used as property sources to get the properties
 	 * that can have relative values.
 	 */
 	private static StandardItemProviderAdapterFactory itemProviderFactory;
 
 	/**
 	 * Constructor
 	 */
 	public StandardRelativeValueProviderAdapterFactory() {
 		super();
 		RelativeValueProviderAdapterFactory.INSTANCE.addAdapterFactory(this);
 	} // StandardRelativeValueProviderAdapterFactory
 
 	/**
 	 * This method is called by the {@link StandardSwitch} instance in the
 	 * parent {@link StandardAdapterFactory} for all cases for classes derived
 	 * from {@link DiseaseModelLabel} (e.g., {@link SILabel}, {@link SIRLabel},
 	 * {@link SEIRLabel}).
 	 * 
 	 * @see org.eclipse.stem.diseasemodels.standard.util.StandardAdapterFactory#createDiseaseModelLabelAdapter()
 	 */
 	@Override
 	public Adapter createStandardPopulationModelLabelAdapter() {
 		// It seems that you can't have singleton adapters that provide
 		// behavioral extensions without explicitly setting the target
 		// each time it is adapted
 		return new StandardPopulationModelLabelRelativeValueProvider();
 	} // createDiseaseModelLabelAdapter
 
 	/**
 	 * This method is called by the {@link StandardSwitch} instance in the
 	 * parent {@link StandardAdapterFactory} for all cases for classes derived
 	 * from {@link DiseaseModelLabelValue} (e.g., {@link SILabelValue},
 	 * {@link SIRLabelValue}, {@link SEIRLabelValue}).
 	 * 
 	 * @see org.eclipse.stem.diseasemodels.standard.util.StandardAdapterFactory#createDiseaseModelLabelValueAdapter()
 	 */
 	@Override
 	public Adapter createStandardPopulationModelLabelValueAdapter() {
 		// It seems that you can't have singleton adapters that provide
 		// behavioral extensions without explicitly setting the target
 		// each time it is adapted
 		return new PopulationModelLabelValueRelativeValueProvider();
 	} // createDiseaseModelLabelValueAdapter
 
 	/**
 	 * @see org.eclipse.emf.edit.provider.IChangeNotifier#addListener(org.eclipse.emf.edit.provider.INotifyChangedListener)
 	 */
 	public void addListener(INotifyChangedListener notifyChangedListener) {
 		changeNotifier.addListener(notifyChangedListener);
 	} // addListener
 
 	/**
 	 * @see org.eclipse.emf.edit.provider.IChangeNotifier#fireNotifyChanged(org.eclipse.emf.common.notify.Notification)
 	 */
 	public void fireNotifyChanged(Notification notification) {
 		changeNotifier.fireNotifyChanged(notification);
 		if (parentAdapterFactory != null) {
 			parentAdapterFactory.fireNotifyChanged(notification);
 		}
 	} // fireNotifyChanged
 
 	/**
 	 * @see org.eclipse.emf.edit.provider.IChangeNotifier#removeListener(org.eclipse.emf.edit.provider.INotifyChangedListener)
 	 */
 	public void removeListener(INotifyChangedListener notifyChangedListener) {
 		changeNotifier.removeListener(notifyChangedListener);
 	} // removeListener
 
 	/**
 	 * @see org.eclipse.emf.edit.provider.ComposeableAdapterFactory#getRootAdapterFactory()
 	 */
 	public ComposeableAdapterFactory getRootAdapterFactory() {
 		return parentAdapterFactory == null ? this : parentAdapterFactory
 				.getRootAdapterFactory();
 	} // getRootAdapterFactory
 
 	/**
 	 * @see org.eclipse.emf.edit.provider.ComposeableAdapterFactory#setParentAdapterFactory(org.eclipse.emf.edit.provider.ComposedAdapterFactory)
 	 */
 	public void setParentAdapterFactory(
 			ComposedAdapterFactory parentAdapterFactory) {
 		this.parentAdapterFactory = parentAdapterFactory;
 	} // setParentAdapterFactory
 
 	/**
 	 * @see org.eclipse.stem.diseasemodels.standard.util.StandardAdapterFactory#isFactoryForType(java.lang.Object)
 	 */
 	@Override
 	public boolean isFactoryForType(Object type) {
 		return type == RelativeValueProvider.class
 				|| super.isFactoryForType(type);
 	} // isFactoryForType
 
 	/**
 	 * This disposes all of the item providers created by this factory.
 	 * 
 	 * @see org.eclipse.emf.edit.provider.IDisposable#dispose()
 	 */
 	public void dispose() {
 		// Nothing
 	} // dispose
 
 	/**
 	 * @return the instance of the Relative Value Provider Adapter Factory
 	 */
 	private static RelativeValueProviderAdapterFactory getRelativeValueProviderAdapterFactory() {
 		return RelativeValueProviderAdapterFactory.INSTANCE;
 	} // getRelativeValueProviderAdapterFactory
 
 	/**
 	 * @return the instance of the Standard Item Provider factory.
 	 */
 	private static StandardItemProviderAdapterFactory getItemProviderFactory() {
 		if (itemProviderFactory == null) {
 			itemProviderFactory = new StandardItemProviderAdapterFactory();
 		}
 		return itemProviderFactory;
 	} // getItemProviderFactory
 
 	/**
 	 * 
 	 * 
 	 * @see DiseaseModelLabelValueRelativeValueProvider
 	 */
 	public static class StandardPopulationModelLabelRelativeValueProvider extends
 			RelativeValueProviderAdapter implements RelativeValueProvider {
 
 		/**
 		 * @see org.eclipse.stem.definitions.adapters.relativevalue.RelativeValueProviderAdapter#getProperties()
 		 */
 		@Override
 		public List<IItemPropertyDescriptor> getProperties() {
 			final PopulationModelLabel pop = (PopulationModelLabel) getTarget();
 			final RelativeValueProvider rvp = (RelativeValueProvider) getRelativeValueProviderAdapterFactory()
 					.adapt(pop.getCurrentValue(), RelativeValueProvider.class);
 			return rvp.getProperties();
 		} // getProperties
 
 		/**
 		 * @see org.eclipse.stem.definitions.adapters.relativevalue.RelativeValueProviderAdapter#getRelativeValue(org.eclipse.emf.ecore.EStructuralFeature)
 		 */
 		@Override
 		public double getRelativeValue(EStructuralFeature feature) {
 			final PopulationModelLabel pop = (PopulationModelLabel) getTarget();
 			final RelativeValueProvider rvp = (RelativeValueProvider) getRelativeValueProviderAdapterFactory()
 					.adapt(pop.getCurrentValue(), RelativeValueProvider.class);
 			return rvp.getRelativeValue(feature);
 		} // getRelativeValue
 		
 		/**
 		 * this helper method returns the absolute total population.
 		 * It is required whenever we need to switch between relative and absolute values
 		 * @return the total current population count (absolute)
 		 */
 		@SuppressWarnings("deprecation")
 		@Override
 		public double getDenominator(final EStructuralFeature feature) {
 			String _popRef = Activator.getDefault().getPluginPreferences().getString(PreferenceConstants.REFERENCE_POPULATION);
 			String _densRef = Activator.getDefault().getPluginPreferences().getString(PreferenceConstants.REFERENCE_DENSITY);
 	
 			double popRef = Double.parseDouble(_popRef);
 			double densRef = Double.parseDouble(_densRef);
 			
 			if(popRef == 0) popRef = PreferenceInitializer.DEFAULT_REFERENCE_POPULATION;
 			if(densRef == 0) popRef = PreferenceInitializer.DEFAULT_REFERENCE_DENSITY;
 		
 			if(feature != null && feature.getFeatureID() == StandardPackage.STANDARD_POPULATION_MODEL_LABEL_VALUE__DENSITY)
 				return densRef;
 			else
 				return popRef;
 		}
 		
 		
 		
 
 	} // DiseaseModelLabelRelativeValueProvider
 
 	/**
 	 *
 	 */
 	public static class PopulationModelLabelValueRelativeValueProvider extends
 			RelativeValueProviderAdapter implements RelativeValueProvider {
 
 		/**
 		 * @see org.eclipse.stem.definitions.adapters.relativevalue.RelativeValueProviderAdapter#getProperties()
 		 */
 		@Override
 		public List<IItemPropertyDescriptor> getProperties() {
 			final List<IItemPropertyDescriptor> retValue = new ArrayList<IItemPropertyDescriptor>();
 			final StandardItemProviderAdapterFactory itemProviderFactory = getItemProviderFactory();
 			final PopulationModelLabelValue popv = (PopulationModelLabelValue) getTarget();
 			final IItemPropertySource propertySource = (IItemPropertySource) itemProviderFactory
 					.adapt(popv, PropertySource.class);
 			final List<IItemPropertyDescriptor> properties = propertySource
 					.getPropertyDescriptors(null);
 			// The list of property descriptors includes ones that do not have
 			// relative values. We filter those out here and return the rest.
 			for (IItemPropertyDescriptor descriptor : properties) {
 				// Does this property have a relative value?
 				final EStructuralFeature feature = (EStructuralFeature) descriptor
 						.getFeature(null);
 				final int featureId = feature.getFeatureID();
 				
 				
 				// new
 				if (!(featureId == StandardPackage.STANDARD_POPULATION_MODEL_LABEL_VALUE__BIRTHS 
 						|| featureId == StandardPackage.STANDARD_POPULATION_MODEL_LABEL_VALUE__DEATHS 
 						|| featureId == StandardPackage.STANDARD_POPULATION_MODEL_LABEL_VALUE__INCIDENCE)) {
 					// Yes
 				
 					retValue.add(descriptor);
 				
 				} // if has relative value
 				// Old
 				/*
 				if (!(featureId == StandardPackage.DISEASE_MODEL_LABEL_VALUE__BIRTHS
 						|| featureId == StandardPackage.DISEASE_MODEL_LABEL_VALUE__DEATHS
 						|| featureId == StandardPackage.DISEASE_MODEL_LABEL_VALUE__DISEASE_DEATHS || featureId == StandardPackage.DISEASE_MODEL_LABEL_VALUE__POPULATION_COUNT)) {
 					// Yes
 					retValue.add(descriptor);
 				} // if has relative value
 				*/
 				
 			} // for
 			return retValue;
 		} // getProperties
 
 		/**
 		 * @see org.eclipse.stem.definitions.adapters.relativevalue.RelativeValueProvider#getRelativeValue(EStructuralFeature)
 		 */
 		@SuppressWarnings("deprecation")
 		@Override
 		public double getRelativeValue(final EStructuralFeature feature) {
 			final StandardPopulationModelLabelValue popv = (StandardPopulationModelLabelValue) getTarget();
 			final double count = ((Double) popv.eGet(feature))
 					.doubleValue();
 			
 			String _popRef = Activator.getDefault().getPluginPreferences().getString(PreferenceConstants.REFERENCE_POPULATION);
 			String _densRef = Activator.getDefault().getPluginPreferences().getString(PreferenceConstants.REFERENCE_DENSITY);
 	
 			if((_popRef==null)||(_popRef.length()<=0)) {
 				_popRef = "0";
 			}
 			if((_densRef==null)||(_densRef.length()<=0)) {
 				_densRef = "0";
 			}
 			double popRef = Double.parseDouble(_popRef);
 			double densRef = Double.parseDouble(_densRef);
 			
 			if(popRef == 0) popRef = PreferenceInitializer.DEFAULT_REFERENCE_POPULATION;
			if(densRef == 0) densRef = PreferenceInitializer.DEFAULT_REFERENCE_DENSITY;
 			
 			double retValue;
 			if(feature.getFeatureID() == StandardPackage.STANDARD_POPULATION_MODEL_LABEL_VALUE__DENSITY)
 				retValue = (count / densRef);
 			else
 				retValue = (count / popRef);
 			return (retValue >1.0) ? 1.0:retValue;
 		} // getRelativeValue
 		
 		/**
 		 * This method returns the denominator or scale used to convert to relative
 		 * values in the range 0-1. For example, in an Epidemic Compartment model
 		 * the state values are normalized by population.
 		 * It is required whenever we need to switch between relative and absolute values
 		 * or can be used to create a label showing the maximum scale for any relative value.
 		 * @return the denominator or scale used to normalize the relative value
 		 */
 		@Override
 		public double getDenominator(final EStructuralFeature feature) {
 			long popRef = Activator.getDefault().getPluginPreferences().getLong(PreferenceConstants.REFERENCE_POPULATION);
 			long densRef = Activator.getDefault().getPluginPreferences().getLong(PreferenceConstants.REFERENCE_DENSITY);
 	
 			if(popRef == 0) popRef = PreferenceInitializer.DEFAULT_REFERENCE_POPULATION;
 			if(densRef == 0) popRef = PreferenceInitializer.DEFAULT_REFERENCE_DENSITY;
 	
 			if(feature.getFeatureID() == StandardPackage.STANDARD_POPULATION_MODEL_LABEL_VALUE__DENSITY)
 				return densRef;
 			else
 				return popRef;
 		
 		}
 		
 	} // PopulationModelLabelValueRelativeValueProvider
 
 	public void earlyStartup() {
 		// Done
 	}
 } // StandardRelativeValueProviderAdapterFactory
