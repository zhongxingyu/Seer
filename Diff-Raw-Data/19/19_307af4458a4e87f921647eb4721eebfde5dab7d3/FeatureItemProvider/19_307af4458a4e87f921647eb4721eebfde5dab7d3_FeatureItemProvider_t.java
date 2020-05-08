 /**
  */
 package org.obeonetwork.dsl.gen.eclipse.provider;
 
 
 import java.util.Collection;
 import java.util.List;
 
 import org.eclipse.emf.common.notify.AdapterFactory;
 import org.eclipse.emf.common.notify.Notification;
 import org.eclipse.emf.edit.provider.ComposeableAdapterFactory;
 import org.eclipse.emf.edit.provider.IEditingDomainItemProvider;
 import org.eclipse.emf.edit.provider.IItemLabelProvider;
 import org.eclipse.emf.edit.provider.IItemPropertyDescriptor;
 import org.eclipse.emf.edit.provider.IItemPropertySource;
 import org.eclipse.emf.edit.provider.IStructuredItemContentProvider;
 import org.eclipse.emf.edit.provider.ITreeItemContentProvider;
 import org.eclipse.emf.edit.provider.ItemPropertyDescriptor;
 import org.eclipse.emf.edit.provider.ViewerNotification;
 import org.obeonetwork.dsl.gen.eclipse.Application;
 import org.obeonetwork.dsl.gen.eclipse.EclipsePackage;
 import org.obeonetwork.dsl.gen.eclipse.Feature;
 
 /**
  * This is the item provider adapter for a {@link org.obeonetwork.dsl.gen.eclipse.Feature} object.
  * <!-- begin-user-doc -->
  * <!-- end-user-doc -->
  * @generated
  */
 public class FeatureItemProvider
 	extends ProjectItemProvider
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
 	public FeatureItemProvider(AdapterFactory adapterFactory) {
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
 
 			addCopyrightPropertyDescriptor(object);
 			addDescriptionPropertyDescriptor(object);
 			addVersionPropertyDescriptor(object);
 			addLicensePropertyDescriptor(object);
 			addProviderPropertyDescriptor(object);
 			addBundlesPropertyDescriptor(object);
 			addGenerateSdkFeaturePropertyDescriptor(object);
 			addExcludeBundleForSdkPropertyDescriptor(object);
 			addIncludedFeaturesPropertyDescriptor(object);
 		}
 		return itemPropertyDescriptors;
 	}
 
 	/**
 	 * This adds a property descriptor for the Copyright feature.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	protected void addCopyrightPropertyDescriptor(Object object) {
 		itemPropertyDescriptors.add
 			(createItemPropertyDescriptor
 				(((ComposeableAdapterFactory)adapterFactory).getRootAdapterFactory(),
 				 getResourceLocator(),
 				 getString("_UI_Feature_copyright_feature"),
 				 getString("_UI_PropertyDescriptor_description", "_UI_Feature_copyright_feature", "_UI_Feature_type"),
 				 EclipsePackage.Literals.FEATURE__COPYRIGHT,
 				 true,
 				 false,
 				 false,
 				 ItemPropertyDescriptor.GENERIC_VALUE_IMAGE,
 				 null,
 				 null));
 	}
 
 	/**
 	 * This adds a property descriptor for the Description feature.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	protected void addDescriptionPropertyDescriptor(Object object) {
 		itemPropertyDescriptors.add
 			(createItemPropertyDescriptor
 				(((ComposeableAdapterFactory)adapterFactory).getRootAdapterFactory(),
 				 getResourceLocator(),
 				 getString("_UI_Feature_description_feature"),
 				 getString("_UI_PropertyDescriptor_description", "_UI_Feature_description_feature", "_UI_Feature_type"),
 				 EclipsePackage.Literals.FEATURE__DESCRIPTION,
 				 true,
 				 false,
 				 false,
 				 ItemPropertyDescriptor.GENERIC_VALUE_IMAGE,
 				 null,
 				 null));
 	}
 
 	/**
 	 * This adds a property descriptor for the Version feature.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	protected void addVersionPropertyDescriptor(Object object) {
 		itemPropertyDescriptors.add
 			(createItemPropertyDescriptor
 				(((ComposeableAdapterFactory)adapterFactory).getRootAdapterFactory(),
 				 getResourceLocator(),
 				 getString("_UI_Feature_version_feature"),
 				 getString("_UI_PropertyDescriptor_description", "_UI_Feature_version_feature", "_UI_Feature_type"),
 				 EclipsePackage.Literals.FEATURE__VERSION,
 				 true,
 				 false,
 				 false,
 				 ItemPropertyDescriptor.GENERIC_VALUE_IMAGE,
 				 null,
 				 null));
 	}
 
 	/**
 	 * This adds a property descriptor for the License feature.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	protected void addLicensePropertyDescriptor(Object object) {
 		itemPropertyDescriptors.add
 			(createItemPropertyDescriptor
 				(((ComposeableAdapterFactory)adapterFactory).getRootAdapterFactory(),
 				 getResourceLocator(),
 				 getString("_UI_Feature_license_feature"),
 				 getString("_UI_PropertyDescriptor_description", "_UI_Feature_license_feature", "_UI_Feature_type"),
 				 EclipsePackage.Literals.FEATURE__LICENSE,
 				 true,
 				 false,
 				 false,
 				 ItemPropertyDescriptor.GENERIC_VALUE_IMAGE,
 				 null,
 				 null));
 	}
 
 	/**
 	 * This adds a property descriptor for the Provider feature.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	protected void addProviderPropertyDescriptor(Object object) {
 		itemPropertyDescriptors.add
 			(createItemPropertyDescriptor
 				(((ComposeableAdapterFactory)adapterFactory).getRootAdapterFactory(),
 				 getResourceLocator(),
 				 getString("_UI_Feature_provider_feature"),
 				 getString("_UI_PropertyDescriptor_description", "_UI_Feature_provider_feature", "_UI_Feature_type"),
 				 EclipsePackage.Literals.FEATURE__PROVIDER,
 				 true,
 				 false,
 				 false,
 				 ItemPropertyDescriptor.GENERIC_VALUE_IMAGE,
 				 null,
 				 null));
 	}
 
 	/**
 	 * This adds a property descriptor for the Bundles feature.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	protected void addBundlesPropertyDescriptor(Object object) {
 		itemPropertyDescriptors.add
 			(createItemPropertyDescriptor
 				(((ComposeableAdapterFactory)adapterFactory).getRootAdapterFactory(),
 				 getResourceLocator(),
 				 getString("_UI_Feature_bundles_feature"),
 				 getString("_UI_PropertyDescriptor_description", "_UI_Feature_bundles_feature", "_UI_Feature_type"),
 				 EclipsePackage.Literals.FEATURE__BUNDLES,
 				 true,
 				 false,
 				 true,
 				 null,
 				 null,
 				 null));
 	}
 
 	/**
 	 * This adds a property descriptor for the Generate Sdk Feature feature.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	protected void addGenerateSdkFeaturePropertyDescriptor(Object object) {
 		itemPropertyDescriptors.add
 			(createItemPropertyDescriptor
 				(((ComposeableAdapterFactory)adapterFactory).getRootAdapterFactory(),
 				 getResourceLocator(),
 				 getString("_UI_Feature_generateSdkFeature_feature"),
 				 getString("_UI_PropertyDescriptor_description", "_UI_Feature_generateSdkFeature_feature", "_UI_Feature_type"),
 				 EclipsePackage.Literals.FEATURE__GENERATE_SDK_FEATURE,
 				 true,
 				 false,
 				 false,
 				 ItemPropertyDescriptor.BOOLEAN_VALUE_IMAGE,
 				 null,
 				 null));
 	}
 
 	/**
 	 * This adds a property descriptor for the Exclude Bundle For Sdk feature.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	protected void addExcludeBundleForSdkPropertyDescriptor(Object object) {
 		itemPropertyDescriptors.add
 			(createItemPropertyDescriptor
 				(((ComposeableAdapterFactory)adapterFactory).getRootAdapterFactory(),
 				 getResourceLocator(),
 				 getString("_UI_Feature_excludeBundleForSdk_feature"),
 				 getString("_UI_PropertyDescriptor_description", "_UI_Feature_excludeBundleForSdk_feature", "_UI_Feature_type"),
 				 EclipsePackage.Literals.FEATURE__EXCLUDE_BUNDLE_FOR_SDK,
 				 true,
 				 false,
 				 true,
 				 null,
 				 null,
 				 null));
 	}
 
 	/**
 	 * This adds a property descriptor for the Included Features feature.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	protected void addIncludedFeaturesPropertyDescriptor(Object object) {
 		itemPropertyDescriptors.add
 			(createItemPropertyDescriptor
 				(((ComposeableAdapterFactory)adapterFactory).getRootAdapterFactory(),
 				 getResourceLocator(),
 				 getString("_UI_Feature_includedFeatures_feature"),
 				 getString("_UI_PropertyDescriptor_description", "_UI_Feature_includedFeatures_feature", "_UI_Feature_type"),
 				 EclipsePackage.Literals.FEATURE__INCLUDED_FEATURES,
 				 true,
 				 false,
 				 true,
 				 null,
 				 null,
 				 null));
 	}
 
 	/**
 	 * This returns Feature.gif.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	@Override
 	public Object getImage(Object object) {
 		return overlayImage(object, getResourceLocator().getImage("full/obj16/Feature"));
 	}
 
 	/**
 	 * This returns the label text for the adapted class.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated-NOT
 	 */
 	@SuppressWarnings("null")
 	@Override
 	public String getText(Object object) {
 		String label = ((Feature)object).getName();
 		String id = ((Feature)object).getID();
 		String baseID = "";
		if (((Application)((Feature)object).eContainer()).getBaseNamespace() == null) {
 			baseID = " complete the baseNamespace field";
 		} else {
 			baseID = ((Application)((Feature)object).eContainer()).getBaseNamespace();			
 		}
 		
 		if ((label == null || label.length() == 0) && (id == null || id.length() == 0)) {
 			return getString("_UI_Feature_type");			
 		} else if ((label == null || label.length() == 0) && (id != null || id.length() != 0)) {
 			return getString("_UI_Feature_type") + " " + baseID + "." + id + " -- no name define";
 		} else if ((label != null || label.length() != 0) && (id == null || id.length() == 0)) {
 			return getString("_UI_Feature_type") + " no ID define -- " + label;
 		} else {
 			return getString("_UI_Feature_type") + " " + baseID + "." + id + " -- " + label;
 		}
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
 
 		switch (notification.getFeatureID(Feature.class)) {
 			case EclipsePackage.FEATURE__COPYRIGHT:
 			case EclipsePackage.FEATURE__DESCRIPTION:
 			case EclipsePackage.FEATURE__VERSION:
 			case EclipsePackage.FEATURE__LICENSE:
 			case EclipsePackage.FEATURE__PROVIDER:
 			case EclipsePackage.FEATURE__GENERATE_SDK_FEATURE:
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
 
 }
