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
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 import org.eclipse.b3.aggregator.AggregatorPackage;
 import org.eclipse.b3.aggregator.IAggregatorConstants;
 import org.eclipse.b3.aggregator.InstallableUnitRequest;
 import org.eclipse.b3.aggregator.MappedRepository;
 import org.eclipse.b3.aggregator.util.GeneralUtils;
 import org.eclipse.b3.p2.MetadataRepository;
 import org.eclipse.b3.p2.impl.InstallableUnitImpl;
 import org.eclipse.b3.p2.util.P2Utils;
 import org.eclipse.b3.p2.util.RepositoryTranslationSupport;
 import org.eclipse.emf.common.notify.AdapterFactory;
 import org.eclipse.emf.common.notify.Notification;
 import org.eclipse.emf.common.util.ResourceLocator;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.ecore.EStructuralFeature;
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
 import org.eclipse.equinox.p2.metadata.IInstallableUnit;
 import org.eclipse.equinox.p2.metadata.VersionRange;
 import org.eclipse.equinox.p2.query.IQuery;
 import org.eclipse.equinox.p2.query.IQueryResult;
 
 /**
  * This is the item provider adapter for a {@link org.eclipse.b3.aggregator.InstallableUnitRequest} object.
  * <!-- begin-user-doc --> <!-- end-user-doc -->
  * 
  * @generated
  */
 public class InstallableUnitRequestItemProvider extends AggregatorItemProviderAdapter implements
 		IEditingDomainItemProvider, IStructuredItemContentProvider, ITreeItemContentProvider, IItemLabelProvider,
 		IItemPropertySource, IItemColorProvider, IItemFontProvider {
 
 	/**
 	 * This constructs an instance from a factory and a notifier.
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	public InstallableUnitRequestItemProvider(AdapterFactory adapterFactory) {
 		super(adapterFactory);
 	}
 
 	/**
 	 * This adds a property descriptor for the Description feature.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	protected void addDescriptionPropertyDescriptor(Object object) {
 		itemPropertyDescriptors.add(createItemPropertyDescriptor(
 			((ComposeableAdapterFactory) adapterFactory).getRootAdapterFactory(),
 			getResourceLocator(),
 			getString("_UI_DescriptionProvider_description_feature"),
 			getString(
 				"_UI_PropertyDescriptor_description", "_UI_DescriptionProvider_description_feature",
 				"_UI_DescriptionProvider_type"), AggregatorPackage.Literals.DESCRIPTION_PROVIDER__DESCRIPTION, true,
 			true, false, ItemPropertyDescriptor.GENERIC_VALUE_IMAGE, null, null));
 	}
 
 	/**
 	 * This adds a property descriptor for the Name feature.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * 
 	 * @generated NOT
 	 */
 	protected void addNamePropertyDescriptor(Object object) {
 		itemPropertyDescriptors.add(new ContributionItemProvider.DynamicItemPropertyDescriptor(
 			((ComposeableAdapterFactory) adapterFactory).getRootAdapterFactory(), getResourceLocator(),
 			getString("_UI_InstallableUnitRequest_name_feature"), getString(
 				"_UI_PropertyDescriptor_description", "_UI_InstallableUnitRequest_name_feature",
 				"_UI_InstallableUnitRequest_type"), AggregatorPackage.Literals.INSTALLABLE_UNIT_REQUEST__NAME, true,
 			false, false, ItemPropertyDescriptor.GENERIC_VALUE_IMAGE, null, null) {
 
 			@Override
 			public Collection<?> getChoiceOfValues(Object object) {
 				InstallableUnitRequest self = (InstallableUnitRequest) object;
 				MappedRepository container = (MappedRepository) ((EObject) self).eContainer();
 				MetadataRepository repo = container.getMetadataRepository(false);
 				if(repo == null || ((EObject) repo).eIsProxy())
 					return Collections.singleton(null);
 
 				// Build a list of IU's that correspond to the given type of MappedUnit
 				//
 				IQueryResult<IInstallableUnit> queryResult = repo.query(getInstallableUnitQuery(), null);
 				if(queryResult.isEmpty())
 					return Collections.singleton(null);
 
 				List<String> result = new ArrayList<String>();
 
 				Collection<IInstallableUnit> availableUnits = queryResult.toSet();
 				Set<String> availableUnitNames = new HashSet<String>(availableUnits.size());
 				for(Object availableUnit : availableUnits)
 					availableUnitNames.add(((IInstallableUnit) availableUnit).getId());
 
 				// if current installable unit is not among the newly retrieved ones,
 				// add it to the choice values so that user would not be surprised by
 				// disappearing current choice after clicking on the property value
 				if(self.getName() != null && !availableUnitNames.contains(self.getName()))
 					result.add(self.getName());
 
 				result.addAll(availableUnitNames);
 
 				// Exclude IU's that are already mapped
 				//
 				for(InstallableUnitRequest iuRef : getContainerChildren(container)) {
 					if(iuRef == self)
 						continue;
 
 					String iu = iuRef.getName();
 					if(iu == null)
 						continue;
 
 					int idx = result.indexOf(iu);
 					if(idx >= 0)
 						result.remove(idx);
 				}
 
 				Collections.sort(result);
 				result.add(0, null);
 
 				return result;
 			}
 		});
 	}
 
 	/**
 	 * This adds a property descriptor for the Version Range feature.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	protected void addVersionRangePropertyDescriptor(Object object) {
 		itemPropertyDescriptors.add(createItemPropertyDescriptor(
 			((ComposeableAdapterFactory) adapterFactory).getRootAdapterFactory(),
 			getResourceLocator(),
 			getString("_UI_InstallableUnitRequest_versionRange_feature"),
 			getString(
 				"_UI_PropertyDescriptor_description", "_UI_InstallableUnitRequest_versionRange_feature",
 				"_UI_InstallableUnitRequest_type"), AggregatorPackage.Literals.INSTALLABLE_UNIT_REQUEST__VERSION_RANGE,
 			true, false, false, ItemPropertyDescriptor.GENERIC_VALUE_IMAGE, null, null));
 	}
 
 	protected boolean appendIUText(Object object, String typeKey, StringBuilder bld) {
 		InstallableUnitRequest iuRef = (InstallableUnitRequest) object;
 		String id = iuRef.getName();
 		VersionRange versionRange = iuRef.getVersionRange();
 		if(versionRange == null)
 			versionRange = VersionRange.emptyRange;
 
 		bld.append(getString(typeKey));
 		bld.append(" : ");
 		if(id == null) {
 			bld.append("not mapped");
 			return false;
 		}
 
 		if(id.endsWith(IAggregatorConstants.FEATURE_SUFFIX))
 			id = id.substring(0, id.length() - IAggregatorConstants.FEATURE_SUFFIX.length());
 		bld.append(id);
 
 		String name = null;
 		IInstallableUnit iu = iuRef.resolveAsSingleton();
 		if(iu != null) {
 			name = RepositoryTranslationSupport.getInstance(
 				(MetadataRepository) ((InstallableUnitImpl) iu).eContainer()).getIUProperty(
 				iu, IInstallableUnit.PROP_NAME);
			if(name.startsWith("%"))
 				name = null;
 		}
 
 		if(!(name == null && VersionRange.emptyRange.equals(versionRange))) {
 			bld.append(" / ");
 			if(!VersionRange.emptyRange.equals(versionRange))
 				bld.append(P2Utils.versionRangeToString(versionRange));
 			if(name != null) {
 				bld.append(" (");
 				bld.append(name);
 				bld.append(")");
 			}
 		}
 		return true;
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
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	@Override
 	protected EStructuralFeature getChildFeature(Object object, Object child) {
 		// Check the type of the specified child object and return the proper feature to use for
 		// adding (see {@link AddCommand}) it as a child.
 
 		return super.getChildFeature(object, child);
 	}
 
 	// hides children when disabled
 	@Override
 	public Collection<?> getChildren(Object object) {
 		if(!((InstallableUnitRequest) object).isBranchDisabledOrMappedRepositoryBroken())
 			return super.getChildren(object);
 		return Collections.emptyList();
 	}
 
 	/**
 	 * This specifies how to implement {@link #getChildren} and is used to deduce an appropriate feature for an
 	 * {@link org.eclipse.emf.edit.command.AddCommand}, {@link org.eclipse.emf.edit.command.RemoveCommand} or
 	 * {@link org.eclipse.emf.edit.command.MoveCommand} in {@link #createCommand}.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	@Override
 	public Collection<? extends EStructuralFeature> getChildrenFeatures(Object object) {
 		if(childrenFeatures == null) {
 			super.getChildrenFeatures(object);
 			childrenFeatures.add(AggregatorPackage.Literals.INSTALLABLE_UNIT_REQUEST__AVAILABLE_VERSIONS_HEADER);
 		}
 		return childrenFeatures;
 	}
 
 	// Must be implemented by subclass.
 	protected List<? extends InstallableUnitRequest> getContainerChildren(MappedRepository container) {
 		throw new UnsupportedOperationException();
 	}
 
 	/**
 	 * Grey out the label if this item is (directly or indirectly) disabled
 	 */
 	@Override
 	public Object getForeground(Object object) {
 		return !((InstallableUnitRequest) object).isBranchDisabledOrMappedRepositoryBroken()
 				? null
 				: IItemColorProvider.GRAYED_OUT_COLOR;
 	}
 
 	// Must be implemented by subclass.
 	protected IQuery<IInstallableUnit> getInstallableUnitQuery() {
 		throw new UnsupportedOperationException();
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
 
 			addDescriptionPropertyDescriptor(object);
 			addNamePropertyDescriptor(object);
 			addVersionRangePropertyDescriptor(object);
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
 	 * This returns the label text for the adapted class.
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	@Override
 	public String getText(Object object) {
 		String label = ((InstallableUnitRequest) object).getName();
 		return label == null || label.length() == 0
 				? getString("_UI_InstallableUnitRequest_type")
 				: getString("_UI_InstallableUnitRequest_type") + " " + label;
 	}
 
 	// It always have a child - "Available Versions"
 	@Override
 	public boolean hasChildren(Object object) {
 		return !((InstallableUnitRequest) object).isBranchDisabledOrMappedRepositoryBroken();
 	}
 
 	/**
 	 * This handles model notifications by calling {@link #updateChildren} to update any cached children and by creating
 	 * a viewer notification, which it passes to {@link #fireNotifyChanged}. <!-- begin-user-doc --> <!-- end-user-doc
 	 * -->
 	 * 
 	 * @generated NOT
 	 */
 	@Override
 	public void notifyChanged(Notification notification) {
 		updateChildren(notification);
 
 		switch(notification.getFeatureID(InstallableUnitRequest.class)) {
 			case AggregatorPackage.INSTALLABLE_UNIT_REQUEST__NAME:
 			case AggregatorPackage.INSTALLABLE_UNIT_REQUEST__VERSION_RANGE:
 				InstallableUnitRequest iuRequest = (InstallableUnitRequest) notification.getNotifier();
 				iuRequest.resolveAvailableVersions(true);
 				fireNotifyChanged(new ViewerNotification(notification, notification.getNotifier(), true, false));
 				GeneralUtils.getAggregatorResource((EObject) iuRequest).analyzeResource();
 				return;
 			case AggregatorPackage.INSTALLABLE_UNIT_REQUEST__AVAILABLE_VERSIONS:
 				fireNotifyChanged(new ViewerNotification(notification, notification.getNotifier(), true, false));
 				return;
 			case AggregatorPackage.INSTALLABLE_UNIT_REQUEST__ERRORS:
 			case AggregatorPackage.INSTALLABLE_UNIT_REQUEST__WARNINGS:
 			case AggregatorPackage.INSTALLABLE_UNIT_REQUEST__INFOS:
 				fireNotifyChanged(new ViewerNotification(notification, notification.getNotifier(), false, true));
 				return;
 		}
 		super.notifyChanged(notification);
 	}
 
 }
