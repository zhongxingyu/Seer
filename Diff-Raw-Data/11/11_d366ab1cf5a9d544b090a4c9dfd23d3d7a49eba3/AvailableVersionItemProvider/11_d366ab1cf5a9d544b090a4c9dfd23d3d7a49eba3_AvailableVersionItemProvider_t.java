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
 
 import java.util.Collection;
 import java.util.List;
 
 import org.eclipse.b3.aggregator.AggregatorPackage;
 import org.eclipse.b3.aggregator.AvailableFrom;
 import org.eclipse.b3.aggregator.AvailableVersion;
 import org.eclipse.emf.common.notify.AdapterFactory;
 import org.eclipse.emf.common.notify.Notification;
 import org.eclipse.emf.common.util.ResourceLocator;
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
 import org.eclipse.equinox.p2.metadata.Version;
 
 /**
  * This is the item provider adapter for a {@link org.eclipse.b3.aggregator.AvailableVersion} object.
  * <!-- begin-user-doc -->
  * <!-- end-user-doc -->
  * 
  * @generated
  */
 public class AvailableVersionItemProvider extends AggregatorItemProviderAdapter implements IEditingDomainItemProvider,
 		IStructuredItemContentProvider, ITreeItemContentProvider, IItemLabelProvider, IItemPropertySource,
 		IItemColorProvider, IItemFontProvider {
 	/**
 	 * This constructs an instance from a factory and a notifier.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	public AvailableVersionItemProvider(AdapterFactory adapterFactory) {
 		super(adapterFactory);
 	}
 
 	/**
 	 * This adds a property descriptor for the Available From feature.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	protected void addAvailableFromPropertyDescriptor(Object object) {
 		itemPropertyDescriptors.add(createItemPropertyDescriptor(
 			((ComposeableAdapterFactory) adapterFactory).getRootAdapterFactory(),
 			getResourceLocator(),
 			getString("_UI_AvailableVersion_availableFrom_feature"),
 			getString(
 				"_UI_PropertyDescriptor_description", "_UI_AvailableVersion_availableFrom_feature",
 				"_UI_AvailableVersion_type"), AggregatorPackage.Literals.AVAILABLE_VERSION__AVAILABLE_FROM, true,
 			false, false, ItemPropertyDescriptor.GENERIC_VALUE_IMAGE, null, null));
 	}
 
 	/**
 	 * This adds a property descriptor for the Filter feature.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	protected void addFilterPropertyDescriptor(Object object) {
 		itemPropertyDescriptors.add(createItemPropertyDescriptor(
 			((ComposeableAdapterFactory) adapterFactory).getRootAdapterFactory(),
 			getResourceLocator(),
 			getString("_UI_AvailableVersion_filter_feature"),
 			getString(
 				"_UI_PropertyDescriptor_description", "_UI_AvailableVersion_filter_feature",
 				"_UI_AvailableVersion_type"), AggregatorPackage.Literals.AVAILABLE_VERSION__FILTER, false, false,
 			false, ItemPropertyDescriptor.GENERIC_VALUE_IMAGE, null, null));
 	}
 
 	/**
 	 * This adds a property descriptor for the Version Match feature.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	protected void addVersionMatchPropertyDescriptor(Object object) {
 		itemPropertyDescriptors.add(createItemPropertyDescriptor(
 			((ComposeableAdapterFactory) adapterFactory).getRootAdapterFactory(),
 			getResourceLocator(),
 			getString("_UI_AvailableVersion_versionMatch_feature"),
 			getString(
 				"_UI_PropertyDescriptor_description", "_UI_AvailableVersion_versionMatch_feature",
 				"_UI_AvailableVersion_type"), AggregatorPackage.Literals.AVAILABLE_VERSION__VERSION_MATCH, false,
 			false, false, ItemPropertyDescriptor.GENERIC_VALUE_IMAGE, null, null));
 	}
 
 	/**
 	 * This adds a property descriptor for the Version feature.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	protected void addVersionPropertyDescriptor(Object object) {
 		itemPropertyDescriptors.add(createItemPropertyDescriptor(
 			((ComposeableAdapterFactory) adapterFactory).getRootAdapterFactory(),
 			getResourceLocator(),
 			getString("_UI_AvailableVersion_version_feature"),
 			getString(
 				"_UI_PropertyDescriptor_description", "_UI_AvailableVersion_version_feature",
 				"_UI_AvailableVersion_type"), AggregatorPackage.Literals.AVAILABLE_VERSION__VERSION, false, false,
 			false, ItemPropertyDescriptor.GENERIC_VALUE_IMAGE, null, null));
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
 	}
 
 	/**
 	 * Grey out the label if this item is (directly or indirectly) disabled
 	 */
 	@Override
 	public Object getForeground(Object object) {
 		if(((AvailableVersion) object).getAvailableFrom() == AvailableFrom.AGGREGATION)
 			return IItemColorProvider.GRAYED_OUT_COLOR;
 		return null;
 	}
 
 	/**
 	 * This returns AvailableVersion.gif.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * 
 	 * @generated NOT
 	 */
 	@Override
 	public Object getImage(Object object) {
 		Version version = ((AvailableVersion) object).getVersion();
 		if(version == null)
 			return overlayImage(object, getResourceLocator().getImage("full/obj16/NoVersionAvailable"));
 
 		switch(((AvailableVersion) object).getVersionMatch()) {
 			case BELOW:
 				return overlayImage(object, getResourceLocator().getImage("full/obj16/VersionBelowRange"));
 			case MATCHES:
 				return overlayImage(object, getResourceLocator().getImage("full/obj16/VersionOK"));
 			case ABOVE:
 				return overlayImage(object, getResourceLocator().getImage("full/obj16/VersionAboveRange"));
 		}
 
 		return null;
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
 
 			addVersionMatchPropertyDescriptor(object);
 			addVersionPropertyDescriptor(object);
 			addFilterPropertyDescriptor(object);
 			addAvailableFromPropertyDescriptor(object);
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
 
 		AvailableVersion av = (AvailableVersion) object;
 		Version version = av.getVersion();
 		if(version == null)
 			return "no version is available";
 
 		StringBuffer sb = new StringBuffer();
 		version.toString(sb);
 		if(av.getAvailableFrom() != AvailableFrom.REPOSITORY) {
			sb.append(" (from another ");
 			switch(av.getAvailableFrom()) {
 				case CONTRIBUTION:
					sb.append("Repository in this Contribution");
 					break;
 				case VALIDATION_SET:
					sb.append("Contribution in this Validation Set");
 					break;
 				case AGGREGATION:
					sb.append("Validation Set");
 					break;
 			}
 			sb.append(')');
 		}
 		return sb.toString();
 	}
 
 	/**
 	 * This returns AvailableVersion.gif.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * 
 	 * @generated NOT
 	 */
 	@Override
 	public String getTooltipText(Object object) {
 		Version version = ((AvailableVersion) object).getVersion();
 		if(version == null)
 			return null;
 
 		switch(((AvailableVersion) object).getVersionMatch()) {
 			case BELOW:
 				return "This version is too low to match the version range";
 			case MATCHES:
 				return "This version matches the version range";
 			case ABOVE:
 				return "This version is too high to match the version range";
 		}
 
 		return null;
 	}
 
 	/**
 	 * This handles model notifications by calling {@link #updateChildren} to update any cached
 	 * children and by creating a viewer notification, which it passes to {@link #fireNotifyChanged}.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	@Override
 	public void notifyChanged(Notification notification) {
 		updateChildren(notification);
 
 		switch(notification.getFeatureID(AvailableVersion.class)) {
 			case AggregatorPackage.AVAILABLE_VERSION__VERSION_MATCH:
 			case AggregatorPackage.AVAILABLE_VERSION__VERSION:
 			case AggregatorPackage.AVAILABLE_VERSION__FILTER:
 			case AggregatorPackage.AVAILABLE_VERSION__AVAILABLE_FROM:
 				fireNotifyChanged(new ViewerNotification(notification, notification.getNotifier(), false, true));
 				return;
 		}
 		super.notifyChanged(notification);
 	}
 
 }
