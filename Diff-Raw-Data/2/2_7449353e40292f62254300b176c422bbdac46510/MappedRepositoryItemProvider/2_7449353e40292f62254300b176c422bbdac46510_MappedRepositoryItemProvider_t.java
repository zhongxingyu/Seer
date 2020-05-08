 /**
  * <copyright>
  * </copyright>
  *
  * $Id$
  */
 package org.eclipse.b3.aggregator.provider;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.List;
 
 import org.eclipse.b3.aggregator.AggregatorFactory;
 import org.eclipse.b3.aggregator.AggregatorPackage;
 import org.eclipse.b3.aggregator.CustomCategory;
 import org.eclipse.b3.aggregator.Feature;
 import org.eclipse.b3.aggregator.MappedRepository;
 import org.eclipse.b3.aggregator.p2.InstallableUnit;
 import org.eclipse.b3.aggregator.p2view.IUPresentation;
 import org.eclipse.b3.aggregator.util.AddIUsToMappedRepositoryCommand;
 import org.eclipse.b3.aggregator.util.AggregatorResource;
 import org.eclipse.b3.aggregator.util.ItemSorter;
 import org.eclipse.b3.aggregator.util.ItemUtils;
 import org.eclipse.b3.aggregator.util.ItemSorter.ItemGroup;
 import org.eclipse.emf.common.command.Command;
 import org.eclipse.emf.common.command.CompoundCommand;
 import org.eclipse.emf.common.command.UnexecutableCommand;
 import org.eclipse.emf.common.notify.AdapterFactory;
 import org.eclipse.emf.common.notify.Notification;
 import org.eclipse.emf.common.util.ResourceLocator;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.ecore.EReference;
 import org.eclipse.emf.ecore.EStructuralFeature;
 import org.eclipse.emf.edit.command.CommandParameter;
 import org.eclipse.emf.edit.command.RemoveCommand;
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
 import org.eclipse.equinox.p2.metadata.IInstallableUnit;
 
 /**
  * This is the item provider adapter for a {@link org.eclipse.b3.aggregator.MappedRepository} object. <!--
  * begin-user-doc --> <!-- end-user-doc -->
  * 
  * @generated
  */
 public class MappedRepositoryItemProvider extends MetadataRepositoryReferenceItemProvider implements
 		IEditingDomainItemProvider, IStructuredItemContentProvider, ITreeItemContentProvider, IItemLabelProvider,
 		IItemPropertySource, IItemColorProvider, IItemFontProvider {
 	/**
 	 * This constructs an instance from a factory and a notifier.
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	public MappedRepositoryItemProvider(AdapterFactory adapterFactory) {
 		super(adapterFactory);
 	}
 
 	/**
 	 * This specifies how to implement {@link #getChildren} and is used to deduce an appropriate feature for an
 	 * {@link org.eclipse.emf.edit.command.AddCommand}, {@link org.eclipse.emf.edit.command.RemoveCommand} or
 	 * {@link org.eclipse.emf.edit.command.MoveCommand} in {@link #createCommand}.
 	 * <!-- begin-user-doc --> <!--
 	 * end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	@Override
 	public Collection<? extends EStructuralFeature> getChildrenFeatures(Object object) {
 		if(childrenFeatures == null) {
 			super.getChildrenFeatures(object);
 			childrenFeatures.add(AggregatorPackage.Literals.MAPPED_REPOSITORY__PRODUCTS);
 			childrenFeatures.add(AggregatorPackage.Literals.MAPPED_REPOSITORY__BUNDLES);
 			childrenFeatures.add(AggregatorPackage.Literals.MAPPED_REPOSITORY__FEATURES);
 			childrenFeatures.add(AggregatorPackage.Literals.MAPPED_REPOSITORY__CATEGORIES);
 			childrenFeatures.add(AggregatorPackage.Literals.MAPPED_REPOSITORY__MAP_RULES);
 		}
 		return childrenFeatures;
 	}
 
 	/**
 	 * This returns MappedRepository.gif. <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * 
 	 * @generated NOT
 	 */
 	@Override
 	public Object getImage(Object object) {
 		return super.getImage(object);
 	}
 
 	/**
 	 * Allow adding children only if the repository enabled
 	 */
 	@Override
 	public Collection<?> getNewChildDescriptors(Object object, EditingDomain editingDomain, Object sibling) {
 		if(!(((MappedRepository) object).isBranchEnabled())
 				|| ((MappedRepository) object).getMetadataRepository(false) == null
				|| ((EObject) ((MappedRepository) object).getMetadataRepository(false)).eIsProxy())
 			return Collections.emptySet();
 
 		return super.getNewChildDescriptors(object, editingDomain, sibling);
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
 			addMirrorArtifactsPropertyDescriptor(object);
 			addCategoryPrefixPropertyDescriptor(object);
 		}
 		return itemPropertyDescriptors;
 	}
 
 	/**
 	 * This returns the label text for the adapted class. <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * 
 	 * @generated NOT
 	 */
 	@Override
 	public String getText(Object object) {
 		return super.getText(object);
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
 
 		switch(notification.getFeatureID(MappedRepository.class)) {
 		case AggregatorPackage.MAPPED_REPOSITORY__DESCRIPTION:
 		case AggregatorPackage.MAPPED_REPOSITORY__MIRROR_ARTIFACTS:
 		case AggregatorPackage.MAPPED_REPOSITORY__CATEGORY_PREFIX:
 			fireNotifyChanged(new ViewerNotification(notification, notification.getNotifier(), false, true));
 			return;
 		case AggregatorPackage.MAPPED_REPOSITORY__PRODUCTS:
 		case AggregatorPackage.MAPPED_REPOSITORY__BUNDLES:
 		case AggregatorPackage.MAPPED_REPOSITORY__FEATURES:
 		case AggregatorPackage.MAPPED_REPOSITORY__CATEGORIES:
 		case AggregatorPackage.MAPPED_REPOSITORY__MAP_RULES:
 			fireNotifyChanged(new ViewerNotification(notification, notification.getNotifier(), true, false));
 			((AggregatorResource) ((EObject) notification.getNotifier()).eResource()).analyzeResource();
 			return;
 		}
 		super.notifyChanged(notification);
 	}
 
 	/**
 	 * This adds a property descriptor for the Category Prefix feature.
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	protected void addCategoryPrefixPropertyDescriptor(Object object) {
 		itemPropertyDescriptors.add(createItemPropertyDescriptor(
 				((ComposeableAdapterFactory) adapterFactory).getRootAdapterFactory(), getResourceLocator(),
 				getString("_UI_MappedRepository_categoryPrefix_feature"), getString(
 						"_UI_PropertyDescriptor_description", "_UI_MappedRepository_categoryPrefix_feature",
 						"_UI_MappedRepository_type"), AggregatorPackage.Literals.MAPPED_REPOSITORY__CATEGORY_PREFIX,
 				true, false, false, ItemPropertyDescriptor.GENERIC_VALUE_IMAGE, null, null));
 	}
 
 	/**
 	 * This adds a property descriptor for the Description feature.
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	protected void addDescriptionPropertyDescriptor(Object object) {
 		itemPropertyDescriptors.add(createItemPropertyDescriptor(
 				((ComposeableAdapterFactory) adapterFactory).getRootAdapterFactory(), getResourceLocator(),
 				getString("_UI_DescriptionProvider_description_feature"), getString(
 						"_UI_PropertyDescriptor_description", "_UI_DescriptionProvider_description_feature",
 						"_UI_DescriptionProvider_type"), AggregatorPackage.Literals.DESCRIPTION_PROVIDER__DESCRIPTION,
 				true, true, false, ItemPropertyDescriptor.GENERIC_VALUE_IMAGE, null, null));
 	}
 
 	/**
 	 * This adds a property descriptor for the Mirror Artifacts feature.
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	protected void addMirrorArtifactsPropertyDescriptor(Object object) {
 		itemPropertyDescriptors.add(createItemPropertyDescriptor(
 				((ComposeableAdapterFactory) adapterFactory).getRootAdapterFactory(), getResourceLocator(),
 				getString("_UI_MappedRepository_mirrorArtifacts_feature"), getString(
 						"_UI_PropertyDescriptor_description", "_UI_MappedRepository_mirrorArtifacts_feature",
 						"_UI_MappedRepository_type"), AggregatorPackage.Literals.MAPPED_REPOSITORY__MIRROR_ARTIFACTS,
 				true, false, false, ItemPropertyDescriptor.BOOLEAN_VALUE_IMAGE, null, null));
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
 
 		newChildDescriptors.add(createChildParameter(AggregatorPackage.Literals.MAPPED_REPOSITORY__PRODUCTS,
 				AggregatorFactory.eINSTANCE.createProduct()));
 
 		newChildDescriptors.add(createChildParameter(AggregatorPackage.Literals.MAPPED_REPOSITORY__BUNDLES,
 				AggregatorFactory.eINSTANCE.createBundle()));
 
 		newChildDescriptors.add(createChildParameter(AggregatorPackage.Literals.MAPPED_REPOSITORY__FEATURES,
 				AggregatorFactory.eINSTANCE.createFeature()));
 
 		newChildDescriptors.add(createChildParameter(AggregatorPackage.Literals.MAPPED_REPOSITORY__CATEGORIES,
 				AggregatorFactory.eINSTANCE.createCategory()));
 
 		newChildDescriptors.add(createChildParameter(AggregatorPackage.Literals.MAPPED_REPOSITORY__MAP_RULES,
 				AggregatorFactory.eINSTANCE.createExclusionRule()));
 
 		newChildDescriptors.add(createChildParameter(AggregatorPackage.Literals.MAPPED_REPOSITORY__MAP_RULES,
 				AggregatorFactory.eINSTANCE.createValidConfigurationsRule()));
 	}
 
 	/**
 	 * Supports DnD from IUs to MappedRepo
 	 */
 	@Override
 	protected Command createDragAndDropCommand(EditingDomain domain, Object owner, float location, int operations,
 			int operation, Collection<?> collection) {
 		if((operation & (AggregatorEditPlugin.ADD_IU | AggregatorEditPlugin.ADD_EXCLUSION_RULE | AggregatorEditPlugin.ADD_VALID_CONFIGURATIONS_RULE)) == 0)
 			operation = AggregatorEditPlugin.ADD_IU;
 
 		Command command = createAddIUsToMappedRepositoryCommand(owner, collection, operation);
 
 		if(command != null && command.canExecute())
 			return command;
 
 		return UnexecutableCommand.INSTANCE;
 	}
 
 	/**
 	 * Creates a dynamic property descriptor which alters the readonly attribute according to the "enabled" flag
 	 */
 	@Override
 	protected ItemPropertyDescriptor createItemPropertyDescriptor(AdapterFactory adapterFactory,
 			ResourceLocator resourceLocator, String displayName, String description, EStructuralFeature feature,
 			boolean isSettable, boolean multiLine, boolean sortChoices, Object staticImage, String category,
 			String[] filterFlags) {
 		return new ContributionItemProvider.DynamicItemPropertyDescriptor(adapterFactory, resourceLocator, displayName,
 				description, feature, isSettable, multiLine, sortChoices, staticImage, category, filterFlags);
 	}
 
 	/**
 	 * Allow deleting a child from mapped repository only if the mapped repository is enabled
 	 */
 	@Override
 	@Deprecated
 	protected Command createRemoveCommand(EditingDomain domain, EObject owner, EReference feature,
 			Collection<?> collection) {
 		if(((MappedRepository) owner).isBranchEnabled()
 				&& ((MappedRepository) owner).getMetadataRepository(false) != null
 				&& !((EObject) ((MappedRepository) owner).getMetadataRepository(false)).eIsProxy())
 			return createCompoundRemoveCommand(domain, (MappedRepository) owner, feature, collection);
 
 		return UnexecutableCommand.INSTANCE;
 	}
 
 	/**
 	 * Allow deleting a child from mapped repository only if the mapped repository is enabled
 	 */
 	@Override
 	protected Command createRemoveCommand(EditingDomain domain, EObject owner, EStructuralFeature feature,
 			Collection<?> collection) {
 		if(feature instanceof EReference) {
 			return createRemoveCommand(domain, owner, (EReference) feature, collection);
 		}
 
 		if(((MappedRepository) owner).isBranchEnabled()
 				&& ((MappedRepository) owner).getMetadataRepository(false) != null
 				&& !((EObject) ((MappedRepository) owner).getMetadataRepository(false)).eIsProxy())
 			return createCompoundRemoveCommand(domain, (MappedRepository) owner, feature, collection);
 
 		return UnexecutableCommand.INSTANCE;
 	}
 
 	/**
 	 * Supports copy&paste from IUs to MappedRepo
 	 */
 	@Override
 	protected Command factorAddCommand(EditingDomain domain, CommandParameter commandParameter) {
 		Command command = createAddIUsToMappedRepositoryCommand(commandParameter.getOwner(),
 				commandParameter.getCollection(), AggregatorEditPlugin.ADD_IU);
 
 		if(command != null)
 			return command;
 
 		return super.factorAddCommand(domain, commandParameter);
 	}
 
 	/**
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	@Override
 	protected EStructuralFeature getChildFeature(Object object, Object child) {
 		// Check the type of the specified child object and return the proper feature to use for
 		// adding (see {@link AddCommand}) it as a child.
 
 		return super.getChildFeature(object, child);
 	}
 
 	@Override
 	protected String getTypeName() {
 		return "_UI_MappedRepository_type";
 	}
 
 	@SuppressWarnings("unchecked")
 	private Command createAddIUsToMappedRepositoryCommand(Object owner, Collection<?> collection, int operation) {
 		ItemSorter itemSorter = new ItemSorter(collection);
 
 		if(((MappedRepository) owner).isEnabled()
 				&& itemSorter.getTotalItemCount() > 0
 				&& (itemSorter.getTotalItemCount() == itemSorter.getGroupItems(ItemGroup.IU).size()
 						&& ItemUtils.haveSameLocation((MappedRepository) owner,
 								(List<InstallableUnit>) itemSorter.getGroupItems(ItemGroup.IU)) || itemSorter.getTotalItemCount() == itemSorter.getGroupItems(
 						ItemGroup.IU_STRUCTURED).size()
 						&& ItemUtils.haveSameLocation(
 								(MappedRepository) owner,
 								ItemUtils.getIUs((List<IUPresentation>) itemSorter.getGroupItems(ItemGroup.IU_STRUCTURED))))) {
 			List<IInstallableUnit> ius = new ArrayList<IInstallableUnit>();
 
 			ius.addAll((List<InstallableUnit>) itemSorter.getGroupItems(ItemGroup.IU));
 			ius.addAll(ItemUtils.getIUs((List<IUPresentation>) itemSorter.getGroupItems(ItemGroup.IU_STRUCTURED)));
 
 			return new AddIUsToMappedRepositoryCommand((MappedRepository) owner, ius, operation);
 		}
 
 		return null;
 	}
 
 	private Command createCompoundRemoveCommand(EditingDomain domain, MappedRepository mappedRepository,
 			EStructuralFeature feature, Collection<?> collection) {
 		List<Command> commands = new ArrayList<Command>();
 		commands.add(new RemoveCommand(domain, (EObject) mappedRepository, feature, collection));
 
 		if(feature.getFeatureID() == AggregatorPackage.MAPPED_REPOSITORY__FEATURES) {
 			for(Object object : collection) {
 				Feature removedFeature = (Feature) object;
 				for(CustomCategory category : removedFeature.getCategories()) {
 					IEditingDomainItemProvider editingDomainItemProvider = (IEditingDomainItemProvider) adapterFactory.adapt(
 							category, IEditingDomainItemProvider.class);
 
 					Command cmd = editingDomainItemProvider.createCommand(category, domain, RemoveCommand.class,
 							new CommandParameter(category, null, Collections.singleton(removedFeature)));
 					commands.add(cmd);
 				}
 			}
 		}
 
 		return new CompoundCommand("Delete", commands);
 	}
 }
