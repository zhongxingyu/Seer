 /**
  * Copyright (c) 2006-2009, Cloudsmith Inc.
  * The code, documentation and other materials contained herein have been
  * licensed under the Eclipse Public License - v 1.0 by the copyright holder
  * listed above, as the Initial Contributor under such license. The text of
  * such license is available at www.eclipse.org.
  */
 package org.eclipse.b3.aggregator.provider;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.List;
 
 import org.eclipse.b3.aggregator.Aggregation;
 import org.eclipse.b3.aggregator.AggregatorFactory;
 import org.eclipse.b3.aggregator.AggregatorPackage;
 import org.eclipse.b3.aggregator.Contribution;
 import org.eclipse.b3.aggregator.MappedRepository;
 import org.eclipse.b3.aggregator.MappedUnit;
 import org.eclipse.b3.aggregator.MavenMapping;
 import org.eclipse.b3.aggregator.MetadataRepositoryReference;
 import org.eclipse.b3.aggregator.ValidationSet;
 import org.eclipse.b3.aggregator.util.GeneralUtils;
 import org.eclipse.b3.aggregator.util.ResourceUtils;
 import org.eclipse.emf.common.command.Command;
 import org.eclipse.emf.common.command.UnexecutableCommand;
 import org.eclipse.emf.common.notify.AdapterFactory;
 import org.eclipse.emf.common.notify.Notification;
 import org.eclipse.emf.common.util.ResourceLocator;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.ecore.EStructuralFeature;
 import org.eclipse.emf.edit.domain.EditingDomain;
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
  * This is the item provider adapter for a {@link org.eclipse.b3.aggregator.ValidationSet} object.
  * <!-- begin-user-doc -->
  * <!-- end-user-doc -->
  * 
  * @generated
  */
 public class ValidationSetItemProvider extends AggregatorItemProviderAdapter implements IEditingDomainItemProvider,
 		IStructuredItemContentProvider, ITreeItemContentProvider, IItemLabelProvider, IItemPropertySource,
 		IItemColorProvider, IItemFontProvider {
 
 	/**
 	 * This constructs an instance from a factory and a notifier.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	public ValidationSetItemProvider(AdapterFactory adapterFactory) {
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
 	 * This adds a property descriptor for the Enabled feature.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	protected void addEnabledPropertyDescriptor(Object object) {
 		itemPropertyDescriptors.add(createItemPropertyDescriptor(
 			((ComposeableAdapterFactory) adapterFactory).getRootAdapterFactory(),
 			getResourceLocator(),
 			getString("_UI_EnabledStatusProvider_enabled_feature"),
 			getString(
 				"_UI_PropertyDescriptor_description", "_UI_EnabledStatusProvider_enabled_feature",
 				"_UI_EnabledStatusProvider_type"), AggregatorPackage.Literals.ENABLED_STATUS_PROVIDER__ENABLED, true,
 			false, false, ItemPropertyDescriptor.BOOLEAN_VALUE_IMAGE, null, null));
 	}
 
 	/**
 	 * This adds a property descriptor for the Extends feature.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * 
 	 * @generated NOT
 	 */
 	protected void addExtendsPropertyDescriptor(Object object) {
 		itemPropertyDescriptors.add(new ItemPropertyDescriptor(
 			((ComposeableAdapterFactory) adapterFactory).getRootAdapterFactory(), getResourceLocator(),
 			getString("_UI_ValidationSet_extends_feature"), getString(
 				"_UI_PropertyDescriptor_description", "_UI_ValidationSet_extends_feature", "_UI_ValidationSet_type"),
 			AggregatorPackage.Literals.VALIDATION_SET__EXTENDS, true, false, true, null, null, null) {
 
 			@Override
 			public Collection<?> getChoiceOfValues(Object object) {
 				// Provide a list of ValidationSets that this set doesn't inherit already
 				ValidationSet vs = (ValidationSet) object;
 				@SuppressWarnings("unchecked")
 				List<ValidationSet> candidates = (List<ValidationSet>) super.getChoiceOfValues(object);
 				List<ValidationSet> filtered = new ArrayList<ValidationSet>();
 				for(ValidationSet candidate : candidates)
 					if(!vs.isExtensionOf(candidate))
 						filtered.add(candidate);
 				return filtered;
 			}
 		});
 
 	}
 
 	/**
 	 * This adds a property descriptor for the Label feature.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	protected void addLabelPropertyDescriptor(Object object) {
 		itemPropertyDescriptors.add(createItemPropertyDescriptor(
 			((ComposeableAdapterFactory) adapterFactory).getRootAdapterFactory(),
 			getResourceLocator(),
 			getString("_UI_ValidationSet_label_feature"),
 			getString("_UI_PropertyDescriptor_description", "_UI_ValidationSet_label_feature", "_UI_ValidationSet_type"),
 			AggregatorPackage.Literals.VALIDATION_SET__LABEL, true, false, false,
 			ItemPropertyDescriptor.GENERIC_VALUE_IMAGE, null, null));
 	}
 
 	/**
 	 * This adds {@link org.eclipse.emf.edit.command.CommandParameter}s describing the children
 	 * that can be created under this object.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	@Override
 	protected void collectNewChildDescriptors(Collection<Object> newChildDescriptors, Object object) {
 		super.collectNewChildDescriptors(newChildDescriptors, object);
 
 		newChildDescriptors.add(createChildParameter(
 			AggregatorPackage.Literals.VALIDATION_SET__CONTRIBUTIONS, AggregatorFactory.eINSTANCE.createContribution()));
 
 		newChildDescriptors.add(createChildParameter(
 			AggregatorPackage.Literals.VALIDATION_SET__VALIDATION_REPOSITORIES,
 			AggregatorFactory.eINSTANCE.createMetadataRepositoryReference()));
 
 		newChildDescriptors.add(createChildParameter(
 			AggregatorPackage.Literals.VALIDATION_SET__VALIDATION_REPOSITORIES,
 			AggregatorFactory.eINSTANCE.createMappedRepository()));
 	}
 
 	@Override
 	protected Command createAddCommand(EditingDomain domain, EObject owner, EStructuralFeature feature,
 			Collection<?> collection, int index) {
 		if(feature == AggregatorPackage.Literals.VALIDATION_SET__VALIDATION_REPOSITORIES) {
 			// disable drag & drop of Mapped Repositories to ValidationSet's validation repositories list;
 			// although - given the class hierarchy - this should be theoretically possible in reality
 			// it isn't as the code in MappedRepositoryImpl expects its container to be a "Contribution"
 			for(Object object : collection) {
 				if(unwrap(object) instanceof MappedRepository)
 					return UnexecutableCommand.INSTANCE;
 			}
 		}
 		return super.createAddCommand(domain, owner, feature, collection, index);
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
 			childrenFeatures.add(AggregatorPackage.Literals.VALIDATION_SET__CONTRIBUTIONS);
 			childrenFeatures.add(AggregatorPackage.Literals.VALIDATION_SET__VALIDATION_REPOSITORIES);
 		}
 		return childrenFeatures;
 	}
 
 	/**
 	 * This returns ValidationSet.gif.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * 
 	 * @generated NOT
 	 */
 	@Override
 	public Object getImage(Object object) {
 		return overlayImage(
 			object, getResourceLocator().getImage("full/obj16/ValidationSet" + (((ValidationSet) object).isEnabled()
 					? ""
 					: "Disabled")));
 	}
 
 	/**
 	 * This returns the property descriptors for the adapted class.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	@Override
 	public List<IItemPropertyDescriptor> getPropertyDescriptors(Object object) {
 		if(itemPropertyDescriptors == null) {
 			super.getPropertyDescriptors(object);
 
 			addEnabledPropertyDescriptor(object);
 			addDescriptionPropertyDescriptor(object);
 			addLabelPropertyDescriptor(object);
 			addExtendsPropertyDescriptor(object);
 		}
 		return itemPropertyDescriptors;
 	}
 
 	/**
 	 * Return the resource locator for this item provider's resources.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	@Override
 	public ResourceLocator getResourceLocator() {
 		return AggregatorEditPlugin.INSTANCE;
 	}
 
 	/**
 	 * This returns the label text for the adapted class.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * 
 	 * @generated NOT
 	 */
 	@Override
 	public String getText(Object object) {
 		ValidationSet self = (ValidationSet) object;
 		String label = self.getLabel();
 		StringBuilder bld = new StringBuilder(getString("_UI_ValidationSet_type")).append(" : ");
 		if(label != null)
 			bld.append(label);
 		if(self.isExtension()) {
 			List<ValidationSet> exs = self.getExtends();
 			int top = exs.size();
 			if(top > 0) {
 				bld.append(" extends ");
 				bld.append(exs.get(0).getLabel());
 				for(int idx = 1; idx < top; ++idx) {
 					bld.append(", ");
 					bld.append(exs.get(idx).getLabel());
 				}
 			}
 		}
 		return bld.toString();
 	}
 
 	@Override
 	public void notifyChanged(Notification notification) {
 		notifyChangedGen(notification);
 		if(notification.getNotifier() instanceof ValidationSet) {
 			ValidationSet vs = (ValidationSet) notification.getNotifier();
 			Aggregation aggr = (Aggregation) ((EObject) vs).eContainer();
 			switch(notification.getFeatureID(ValidationSet.class)) {
 				case AggregatorPackage.VALIDATION_SET__ENABLED:
 				case AggregatorPackage.VALIDATION_SET__STATUS:
					fireNotifyChanged(new ViewerNotification(notification, aggr, false, true));
 
 					// Affects status of extensions of this vs
 					for(ValidationSet other : aggr.getValidationSets(true))
 						if(other != vs && other.isExtensionOf(vs))
 							fireNotifyChanged(new ViewerNotification(notification, other, false, true));
 					return;
 			}
 		}
 
 		if(notification.getEventType() == Notification.REMOVE) {
 			Object oldV = notification.getOldValue();
 			if(oldV instanceof Contribution || oldV instanceof MetadataRepositoryReference)
 				ResourceUtils.cleanUpResources(GeneralUtils.getAggregation((EObject) notification.getNotifier()));
 			if(oldV instanceof MetadataRepositoryReference || oldV instanceof MavenMapping)
 				fireNotifyChanged(new ViewerNotification(notification, notification.getNotifier(), false, true));
 		}
 		else if(notification.getEventType() == Notification.ADD) {
 			Aggregation aggregation = GeneralUtils.getAggregation((EObject) notification.getNotifier());
 			if(aggregation != null)
 				for(ValidationSet vs : aggregation.getValidationSets(true))
 					for(Contribution contribution : vs.getDeclaredContributions())
 						for(MappedRepository mappedRepo : contribution.getRepositories(true))
 							for(MappedUnit unit : mappedRepo.getUnits(false))
 								unit.resolveAvailableVersions(true);
 
 			Object newV = notification.getNewValue();
 			if(newV instanceof Contribution) {
 				for(MappedRepository mappedRepository : ((Contribution) newV).getRepositories(true))
 					ResourceUtils.loadResourceForMappedRepository(mappedRepository);
 			}
 			else if(newV instanceof MetadataRepositoryReference) {
 				ResourceUtils.loadResourceForMappedRepository((MetadataRepositoryReference) newV);
 			}
 
 			if(newV instanceof MetadataRepositoryReference || newV instanceof MavenMapping)
 				fireNotifyChanged(new ViewerNotification(notification, notification.getNotifier(), false, true));
 		}
 	}
 
 	/**
 	 * This handles model notifications by calling {@link #updateChildren} to update any cached
 	 * children and by creating a viewer notification, which it passes to {@link #fireNotifyChanged}.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	public void notifyChangedGen(Notification notification) {
 		updateChildren(notification);
 
 		switch(notification.getFeatureID(ValidationSet.class)) {
 			case AggregatorPackage.VALIDATION_SET__ENABLED:
 			case AggregatorPackage.VALIDATION_SET__DESCRIPTION:
 			case AggregatorPackage.VALIDATION_SET__STATUS:
 			case AggregatorPackage.VALIDATION_SET__LABEL:
 				fireNotifyChanged(new ViewerNotification(notification, notification.getNotifier(), false, true));
 				return;
 			case AggregatorPackage.VALIDATION_SET__CONTRIBUTIONS:
 			case AggregatorPackage.VALIDATION_SET__VALIDATION_REPOSITORIES:
 				fireNotifyChanged(new ViewerNotification(notification, notification.getNotifier(), true, false));
 				return;
 		}
 		super.notifyChanged(notification);
 	}
 
 }
