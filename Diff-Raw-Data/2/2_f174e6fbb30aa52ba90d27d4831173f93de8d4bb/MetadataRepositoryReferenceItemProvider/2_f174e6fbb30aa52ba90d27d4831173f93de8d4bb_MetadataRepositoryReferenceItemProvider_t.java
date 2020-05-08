 /**
  * Copyright (c) 2006-2009, Cloudsmith Inc.
  * The code, documentation and other materials contained herein have been
  * licensed under the Eclipse Public License - v 1.0 by the copyright holder
  * listed above, as the Initial Contributor under such license. The text of
  * such license is available at www.eclipse.org.
  *
  * $Id$
  */
 package org.eclipse.b3.aggregator.provider;
 
 import java.net.URI;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 import org.eclipse.b3.aggregator.Aggregator;
 import org.eclipse.b3.aggregator.AggregatorPackage;
 import org.eclipse.b3.aggregator.AggregatorPlugin;
 import org.eclipse.b3.aggregator.Contribution;
 import org.eclipse.b3.aggregator.CustomCategory;
 import org.eclipse.b3.aggregator.Feature;
 import org.eclipse.b3.aggregator.MappedRepository;
 import org.eclipse.b3.aggregator.MappedUnit;
 import org.eclipse.b3.aggregator.MetadataRepositoryReference;
 import org.eclipse.b3.aggregator.StatusCode;
 import org.eclipse.b3.aggregator.StatusProvider;
 import org.eclipse.b3.aggregator.p2.MetadataRepository;
 import org.eclipse.b3.aggregator.util.ResourceUtils;
 import org.eclipse.emf.common.notify.AdapterFactory;
 import org.eclipse.emf.common.notify.Notification;
 import org.eclipse.emf.common.util.ResourceLocator;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.edit.provider.ComposeableAdapterFactory;
 import org.eclipse.emf.edit.provider.IEditingDomainItemProvider;
 import org.eclipse.emf.edit.provider.IItemColorProvider;
 import org.eclipse.emf.edit.provider.IItemFontProvider;
 import org.eclipse.emf.edit.provider.IItemLabelProvider;
 import org.eclipse.emf.edit.provider.IItemPropertyDescriptor;
 import org.eclipse.emf.edit.provider.IItemPropertySource;
 import org.eclipse.emf.edit.provider.IStructuredItemContentProvider;
 import org.eclipse.emf.edit.provider.ITreeItemContentProvider;
 import org.eclipse.emf.edit.provider.ItemPropertyDescriptor;
 import org.eclipse.emf.edit.provider.ViewerNotification;
 
 /**
  * This is the item provider adapter for a {@link org.eclipse.b3.aggregator.MetadataRepositoryReference} object.
  * <!-- begin-user-doc --> <!-- end-user-doc -->
  * 
  * @generated
  */
 public class MetadataRepositoryReferenceItemProvider extends AggregatorItemProviderAdapter implements
 		IEditingDomainItemProvider, IStructuredItemContentProvider, ITreeItemContentProvider, IItemLabelProvider,
 		IItemPropertySource, IItemColorProvider, IItemFontProvider {
 	/**
 	 * This constructs an instance from a factory and a notifier.
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	public MetadataRepositoryReferenceItemProvider(AdapterFactory adapterFactory) {
 		super(adapterFactory);
 	}
 
 	@Override
 	public Object getFont(Object object) {
 		return ((StatusProvider) object).getStatus().getCode() == StatusCode.WAITING
 				? IItemFontProvider.ITALIC_FONT
 				: null;
 	}
 
 	/**
 	 * Grey out the label if this item is (directly or indirectly) disabled
 	 */
 	@Override
 	public Object getForeground(Object object) {
 		return ((MetadataRepositoryReference) object).isBranchEnabled()
 				? null
 				: IItemColorProvider.GRAYED_OUT_COLOR;
 	}
 
 	/**
 	 * This returns MetadataRepositoryReference.gif. <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * 
 	 * @generated NOT
 	 */
 	@Override
 	public Object getImage(Object object) {
 		return overlayImage(object, getResourceLocator().getImage(
 				"full/obj16/MetadataRepositoryReference" + (((MetadataRepositoryReference) object).isBranchEnabled()
 						? ""
 						: "Disabled")));
 
 	}
 
 	/**
 	 * This returns the property descriptors for the adapted class.
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	@Override
 	public List<IItemPropertyDescriptor> getPropertyDescriptors(Object object) {
 		if(itemPropertyDescriptors == null) {
 			super.getPropertyDescriptors(object);
 
 			addEnabledPropertyDescriptor(object);
 			addLocationPropertyDescriptor(object);
 			addNaturePropertyDescriptor(object);
 		}
 		return itemPropertyDescriptors;
 	}
 
 	/**
 	 * Return the resource locator for this item provider's resources.
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	@Override
 	public ResourceLocator getResourceLocator() {
 		return AggregatorEditPlugin.INSTANCE;
 	}
 
 	/**
 	 * This returns the label text for the adapted class. <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * 
 	 * @generated NOT
 	 */
 	@Override
 	public String getText(Object object) {
 		MetadataRepositoryReference repoRef = (MetadataRepositoryReference) object;
 		MetadataRepository mdr = null;
 		if(repoRef.isBranchEnabled())
 			mdr = repoRef.getMetadataRepository(false);
 		StringBuilder bld = new StringBuilder();
 		bld.append(getString(getTypeName()));
 		bld.append(' ');
 		if(mdr != null && !((EObject) mdr).eIsProxy()) {
 			String name;
 			String nature = repoRef.getNature();
 			URI location;
 
 			if(!((EObject) mdr).eIsProxy()) {
 				name = mdr.getName();
 				location = mdr.getLocation();
 			}
 			else {
 				name = mdr.getNameFromProxy();
 				location = mdr.getLocationFromProxy();
 			}
 
 			if(location != null) {
 				bld.append(nature);
 				bld.append(':');
 				bld.append(location);
 			}
 			else
 				bld.append("no location");
 
 			if(name != null) {
 				bld.append(" (");
 				bld.append(name);
 				bld.append(')');
 			}
 		}
 		else {
 			if(repoRef.getLocation() != null) {
 				bld.append(repoRef.getNature());
 				bld.append(':');
 				bld.append(repoRef.getLocation());
 				bld.append(" (");
 				bld.append(repoRef.getStatus().getCode() == StatusCode.WAITING
 						? "loading"
 						: "missing");
 				bld.append(')');
 			}
 			else
 				bld.append("no location");
 		}
 
 		return bld.toString();
 	}
 
 	/**
 	 * Experimental. Loads a resource when the user types in a URL.
 	 * 
 	 * @generated NOT
 	 */
 	@Override
 	public void notifyChanged(Notification notification) {
 		notifyChangedGen(notification);
 
 		if(notification.getEventType() != Notification.SET)
 			return;
 
 		MetadataRepositoryReference repoRef = (MetadataRepositoryReference) notification.getNotifier();
 		switch(notification.getFeatureID(MetadataRepositoryReference.class)) {
 		case AggregatorPackage.INSTALLABLE_UNIT_REQUEST__AVAILABLE_VERSIONS:
 			fireNotifyChanged(new ViewerNotification(notification, repoRef, true, false));
 			return;
 		case AggregatorPackage.METADATA_REPOSITORY_REFERENCE__NATURE:
 		case AggregatorPackage.METADATA_REPOSITORY_REFERENCE__LOCATION:
 			if(notification.getNewStringValue() != null
 					&& !notification.getNewStringValue().equals(notification.getOldStringValue())
 					|| notification.getOldStringValue() != null
 					&& !notification.getOldStringValue().equals(notification.getNewStringValue())) {
 				onLocationChange(repoRef);
 				// we have started repository load in the background - that's all for now
 				// once the repository is loaded (or fails to load), we'll return again
 				// by setting a MDR reference (which may be null if the load fails)
 				return;
 			}
 
 		case AggregatorPackage.METADATA_REPOSITORY_REFERENCE__METADATA_REPOSITORY:
 		case AggregatorPackage.METADATA_REPOSITORY_REFERENCE__ENABLED:
 			fireNotifyChanged(new ViewerNotification(notification, repoRef, true, false));
 
 			Set<Object> affectedNodeLabels = new HashSet<Object>();
 			affectedNodeLabels.add(repoRef);
 
 			// Go through all direct ancestors first
 			EObject container = ((EObject) repoRef).eContainer();
 			affectedNodeLabels.add(((EObject) repoRef).eResource());
 			while(container != null) {
 				affectedNodeLabels.add(container);
 				container = container.eContainer();
 			}
 
 			if(repoRef instanceof MappedRepository) {
 				// Browse all mapped units which may have changed their virtual status (inherently enabled/disabled)
 				Set<EObject> affectedNodes = new HashSet<EObject>();
 				for(MappedUnit unit : ((MappedRepository) repoRef).getUnits(true)) {
 					affectedNodes.add((EObject) unit);
 					// And now, find all categories which may contain the feature just being enabled/disabled
 					if(unit instanceof Feature)
 						for(CustomCategory category : ((Feature) unit).getCategories())
 							affectedNodes.add((EObject) category);
 				}
 				for(EObject affectedNode : affectedNodes)
 					fireNotifyChanged(new ViewerNotification(notification, affectedNode, true, true));
 			}
 
 			for(Object affectedNode : affectedNodeLabels)
 				fireNotifyChanged(new ViewerNotification(notification, affectedNode, false, true));
 
 			Aggregator aggregator = repoRef.getAggregator();
 			if(notification.getFeatureID(MetadataRepositoryReference.class) == AggregatorPackage.METADATA_REPOSITORY_REFERENCE__ENABLED) {
 				if(notification.getNewBooleanValue())
 					ResourceUtils.loadResourceForMappedRepository(repoRef);
 				else
 					ResourceUtils.cleanUpResources(aggregator);
 			}
 			else
 				ResourceUtils.cleanUpResources(aggregator);
 
 			break;
 		}
 
 	}
 
 	/**
 	 * This handles model notifications by calling {@link #updateChildren} to update any cached children and by creating
 	 * a viewer notification, which it passes to {@link #fireNotifyChanged}. <!-- begin-user-doc --> <!-- end-user-doc
 	 * -->
 	 * 
 	 * @generated
 	 */
 	public void notifyChangedGen(Notification notification) {
 		updateChildren(notification);
 
 		switch(notification.getFeatureID(MetadataRepositoryReference.class)) {
 		case AggregatorPackage.METADATA_REPOSITORY_REFERENCE__ENABLED:
 		case AggregatorPackage.METADATA_REPOSITORY_REFERENCE__LOCATION:
 		case AggregatorPackage.METADATA_REPOSITORY_REFERENCE__NATURE:
 			fireNotifyChanged(new ViewerNotification(notification, notification.getNotifier(), false, true));
 			return;
 		}
 		super.notifyChanged(notification);
 	}
 
 	/**
 	 * This adds a property descriptor for the Enabled feature.
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	protected void addEnabledPropertyDescriptor(Object object) {
 		itemPropertyDescriptors.add(createItemPropertyDescriptor(
 				((ComposeableAdapterFactory) adapterFactory).getRootAdapterFactory(), getResourceLocator(),
 				getString("_UI_EnabledStatusProvider_enabled_feature"), getString("_UI_PropertyDescriptor_description",
 						"_UI_EnabledStatusProvider_enabled_feature", "_UI_EnabledStatusProvider_type"),
 				AggregatorPackage.Literals.ENABLED_STATUS_PROVIDER__ENABLED, true, false, false,
 				ItemPropertyDescriptor.BOOLEAN_VALUE_IMAGE, null, null));
 	}
 
 	/**
 	 * This adds a property descriptor for the Location feature.
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	protected void addLocationPropertyDescriptor(Object object) {
 		itemPropertyDescriptors.add(createItemPropertyDescriptor(
 				((ComposeableAdapterFactory) adapterFactory).getRootAdapterFactory(), getResourceLocator(),
 				getString("_UI_MetadataRepositoryReference_location_feature"), getString(
 						"_UI_PropertyDescriptor_description", "_UI_MetadataRepositoryReference_location_feature",
 						"_UI_MetadataRepositoryReference_type"),
 				AggregatorPackage.Literals.METADATA_REPOSITORY_REFERENCE__LOCATION, true, false, false,
 				ItemPropertyDescriptor.GENERIC_VALUE_IMAGE, null, null));
 	}
 
 	/**
 	 * This adds a property descriptor for the Metadata Repository feature. <!-- begin-user-doc --> <!-- end-user-doc
 	 * -->
 	 * 
 	 * @generated NOT
 	 */
 	protected void addMetadataRepositoryPropertyDescriptor(Object object) {
 		itemPropertyDescriptors.add(new ContributionItemProvider.DynamicItemPropertyDescriptor(
 				((ComposeableAdapterFactory) adapterFactory).getRootAdapterFactory(), getResourceLocator(),
 				getString("_UI_MetadataRepositoryReference_metadataRepository_feature"), getString(
 						"_UI_PropertyDescriptor_description",
 						"_UI_MetadataRepositoryReference_metadataRepository_feature",
 						"_UI_MetadataRepositoryReference_type"),
 				AggregatorPackage.Literals.METADATA_REPOSITORY_REFERENCE__METADATA_REPOSITORY, true, false, true, null,
 				null, null) {
 			@Override
 			public Collection<?> getChoiceOfValues(Object object) {
 				// Provide a list of repositories that has not already been mapped
 				//
 				MetadataRepositoryReference self = (MetadataRepositoryReference) object;
 				Aggregator aggregator = self.getAggregator();
 				Collection<?> repos = super.getChoiceOfValues(object);
 				for(Contribution contribution : aggregator.getContributions()) {
 					for(MappedRepository mappedRepo : contribution.getRepositories()) {
 						if(mappedRepo == self)
 							continue;
 						MetadataRepository repo = mappedRepo.getMetadataRepository(false);
 						if(repo != null && !((EObject) repo).eIsProxy())
 							repos.remove(repo);
 					}
 				}
 				for(MetadataRepositoryReference mrRef : aggregator.getValidationRepositories()) {
 					if(mrRef == self)
 						continue;
 					MetadataRepository repo = mrRef.getMetadataRepository(false);
 					if(repo != null && !((EObject) repo).eIsProxy())
 						repos.remove(repo);
 				}
 				return repos;
 			}
 		});
 	}
 
 	/**
 	 * This adds a property descriptor for the Nature feature. <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * 
 	 * @generated NOT
 	 */
 	protected void addNaturePropertyDescriptor(Object object) {
 		itemPropertyDescriptors.add(new ItemPropertyDescriptor(
 				((ComposeableAdapterFactory) adapterFactory).getRootAdapterFactory(), getResourceLocator(),
 				getString("_UI_MetadataRepositoryReference_nature_feature"), getString(
 						"_UI_PropertyDescriptor_description", "_UI_MetadataRepositoryReference_nature_feature",
 						"_UI_MetadataRepositoryReference_type"),
 				AggregatorPackage.Literals.METADATA_REPOSITORY_REFERENCE__NATURE, true, false, false,
 				ItemPropertyDescriptor.GENERIC_VALUE_IMAGE, null, null) {
 			@Override
 			public Collection<?> getChoiceOfValues(Object object) {
				MetadataRepositoryReference repo = (MetadataRepositoryReference) object;
 				String currentValue = repo.getNature();
 				List<String> supportedValues = AggregatorPlugin.getPlugin().getSupportedRepositoryNatureList();
 				if(!supportedValues.contains(currentValue)) {
 					List<String> globallySupportedValues = supportedValues;
 					supportedValues = new ArrayList<String>(globallySupportedValues.size() + 1);
 					supportedValues.addAll(globallySupportedValues);
 					supportedValues.add(currentValue);
 					Collections.sort(supportedValues);
 				}
 
 				return supportedValues;
 			}
 		});
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
 	}
 
 	protected String getTypeName() {
 		return "_UI_MetadataRepositoryReference_type";
 	}
 
 	private void onLocationChange(MetadataRepositoryReference repository) {
 		repository.startRepositoryLoad(false);
 	}
 }
