 /**
  * Copyright (c) 2006-2009, Cloudsmith Inc.
  * The code, documentation and other materials contained herein have been
  * licensed under the Eclipse Public License - v 1.0 by the copyright holder
  * listed above, as the Initial Contributor under such license. The text of
  * such license is available at www.eclipse.org.
  *
  * $Id$
  */
 package org.eclipse.b3.aggregator.p2view.provider;
 
 import java.util.Collection;
 import java.util.List;
 
import org.eclipse.b3.aggregator.p2view.InstallableUnits;
 import org.eclipse.b3.aggregator.p2view.P2viewPackage;
 import org.eclipse.b3.aggregator.provider.AggregatorEditPlugin;
 import org.eclipse.b3.aggregator.provider.AggregatorItemProviderAdapter;
 
 import org.eclipse.emf.common.notify.AdapterFactory;
 import org.eclipse.emf.common.notify.Notification;
 
 import org.eclipse.emf.common.util.ResourceLocator;
 
 import org.eclipse.emf.ecore.EStructuralFeature;
 import org.eclipse.emf.edit.provider.IEditingDomainItemProvider;
 import org.eclipse.emf.edit.provider.IItemColorProvider;
 import org.eclipse.emf.edit.provider.IItemFontProvider;
 import org.eclipse.emf.edit.provider.IItemLabelProvider;
 import org.eclipse.emf.edit.provider.IItemPropertyDescriptor;
 import org.eclipse.emf.edit.provider.IItemPropertySource;
 import org.eclipse.emf.edit.provider.IStructuredItemContentProvider;
 import org.eclipse.emf.edit.provider.ITreeItemContentProvider;
import org.eclipse.emf.edit.provider.ViewerNotification;
 
 /**
  * This is the item provider adapter for a {@link org.eclipse.b3.aggregator.p2view.InstallableUnits} object.
  * <!-- begin-user-doc --> <!-- end-user-doc -->
  * 
  * @generated
  */
 public class InstallableUnitsItemProvider extends AggregatorItemProviderAdapter implements IEditingDomainItemProvider,
 		IStructuredItemContentProvider, ITreeItemContentProvider, IItemLabelProvider, IItemPropertySource,
 		IItemColorProvider, IItemFontProvider {
 	/**
 	 * This constructs an instance from a factory and a notifier.
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	public InstallableUnitsItemProvider(AdapterFactory adapterFactory) {
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
 			childrenFeatures.add(P2viewPackage.Literals.INSTALLABLE_UNITS__CATEGORY_CONTAINER);
 			childrenFeatures.add(P2viewPackage.Literals.INSTALLABLE_UNITS__FEATURE_CONTAINER);
 			childrenFeatures.add(P2viewPackage.Literals.INSTALLABLE_UNITS__PRODUCT_CONTAINER);
 			childrenFeatures.add(P2viewPackage.Literals.INSTALLABLE_UNITS__BUNDLE_CONTAINER);
 			childrenFeatures.add(P2viewPackage.Literals.INSTALLABLE_UNITS__FRAGMENT_CONTAINER);
 			childrenFeatures.add(P2viewPackage.Literals.INSTALLABLE_UNITS__MISCELLANEOUS_CONTAINER);
 		}
 		return childrenFeatures;
 	}
 
 	/**
 	 * This returns InstallableUnits.gif.
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	@Override
 	public Object getImage(Object object) {
 		return overlayImage(object, getResourceLocator().getImage("full/obj16/InstallableUnits"));
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
 		return getString("_UI_InstallableUnits_type");
 	}
 
 	/**
 	 * This handles model notifications by calling {@link #updateChildren} to update any cached children and by creating
 	 * a viewer notification, which it passes to {@link #fireNotifyChanged}. <!-- begin-user-doc --> <!-- end-user-doc
 	 * -->
 	 * 
 	 * @generated
 	 */
 	@Override
 	public void notifyChanged(Notification notification) {
 		updateChildren(notification);

		switch(notification.getFeatureID(InstallableUnits.class)) {
			case P2viewPackage.INSTALLABLE_UNITS__ALL_IUS:
				fireNotifyChanged(new ViewerNotification(notification, notification.getNotifier(), false, true));
				return;
		}
 		super.notifyChanged(notification);
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
 
 }
