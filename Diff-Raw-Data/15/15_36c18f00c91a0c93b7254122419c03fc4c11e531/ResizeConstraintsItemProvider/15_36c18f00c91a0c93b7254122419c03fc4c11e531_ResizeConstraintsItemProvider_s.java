 /**
  * <copyright>
  * </copyright>
  *
  * $Id$
  */
 package org.eclipse.gmf.codegen.gmfgen.provider;
 
 
 import java.util.Collection;
 import java.util.List;
 
 import org.eclipse.emf.common.notify.AdapterFactory;
 import org.eclipse.emf.common.notify.Notification;
 import org.eclipse.emf.common.util.ResourceLocator;
 import org.eclipse.emf.edit.provider.ComposeableAdapterFactory;
 import org.eclipse.emf.edit.provider.IEditingDomainItemProvider;
 import org.eclipse.emf.edit.provider.IItemLabelProvider;
 import org.eclipse.emf.edit.provider.IItemPropertyDescriptor;
 import org.eclipse.emf.edit.provider.IItemPropertySource;
 import org.eclipse.emf.edit.provider.IStructuredItemContentProvider;
 import org.eclipse.emf.edit.provider.ITreeItemContentProvider;
 import org.eclipse.emf.edit.provider.ItemPropertyDescriptor;
 import org.eclipse.emf.edit.provider.ItemProviderAdapter;
 import org.eclipse.emf.edit.provider.ViewerNotification;
 import org.eclipse.gmf.codegen.gmfgen.GMFGenPackage;
 import org.eclipse.gmf.codegen.gmfgen.ResizeConstraints;
 import org.eclipse.gmf.codegen.gmfgen.presentation.EditorPlugin;
 
 /**
  * This is the item provider adapter for a {@link org.eclipse.gmf.codegen.gmfgen.ResizeConstraints} object.
  * <!-- begin-user-doc -->
  * <!-- end-user-doc -->
  * @generated
  */
 public class ResizeConstraintsItemProvider
 	extends ItemProviderAdapter
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
 	public ResizeConstraintsItemProvider(AdapterFactory adapterFactory) {
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
 
 			addResizeHandlesPropertyDescriptor(object);
 			addNonResizeHandlesPropertyDescriptor(object);
 			addResizeHandleNamesPropertyDescriptor(object);
 			addNonResizeHandleNamesPropertyDescriptor(object);
 		}
 		return itemPropertyDescriptors;
 	}
 
 	/**
 	 * This adds a property descriptor for the Resize Handles feature.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	protected void addResizeHandlesPropertyDescriptor(Object object) {
 		itemPropertyDescriptors.add
 			(createItemPropertyDescriptor
 				(((ComposeableAdapterFactory)adapterFactory).getRootAdapterFactory(),
 				 getResourceLocator(),
 				 getString("_UI_ResizeConstraints_resizeHandles_feature"),
 				 getString("_UI_PropertyDescriptor_description", "_UI_ResizeConstraints_resizeHandles_feature", "_UI_ResizeConstraints_type"),
 				 GMFGenPackage.eINSTANCE.getResizeConstraints_ResizeHandles(),
 				 true,
 				 false,
 				 false,
 				 ItemPropertyDescriptor.INTEGRAL_VALUE_IMAGE,
 				 null,
 				 null));
 	}
 
 	/**
 	 * This adds a property descriptor for the Non Resize Handles feature.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	protected void addNonResizeHandlesPropertyDescriptor(Object object) {
 		itemPropertyDescriptors.add
 			(createItemPropertyDescriptor
 				(((ComposeableAdapterFactory)adapterFactory).getRootAdapterFactory(),
 				 getResourceLocator(),
 				 getString("_UI_ResizeConstraints_nonResizeHandles_feature"),
 				 getString("_UI_PropertyDescriptor_description", "_UI_ResizeConstraints_nonResizeHandles_feature", "_UI_ResizeConstraints_type"),
 				 GMFGenPackage.eINSTANCE.getResizeConstraints_NonResizeHandles(),
 				 true,
 				 false,
 				 false,
 				 ItemPropertyDescriptor.INTEGRAL_VALUE_IMAGE,
 				 null,
 				 null));
 	}
 
 	/**
 	 * This adds a property descriptor for the Resize Handle Names feature.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	protected void addResizeHandleNamesPropertyDescriptor(Object object) {
 		itemPropertyDescriptors.add
 			(createItemPropertyDescriptor
 				(((ComposeableAdapterFactory)adapterFactory).getRootAdapterFactory(),
 				 getResourceLocator(),
 				 getString("_UI_ResizeConstraints_resizeHandleNames_feature"),
 				 getString("_UI_PropertyDescriptor_description", "_UI_ResizeConstraints_resizeHandleNames_feature", "_UI_ResizeConstraints_type"),
 				 GMFGenPackage.eINSTANCE.getResizeConstraints_ResizeHandleNames(),
 				 false,
 				 false,
 				 false,
 				 ItemPropertyDescriptor.GENERIC_VALUE_IMAGE,
 				 null,
 				 null));
 	}
 
 	/**
 	 * This adds a property descriptor for the Non Resize Handle Names feature.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	protected void addNonResizeHandleNamesPropertyDescriptor(Object object) {
 		itemPropertyDescriptors.add
 			(createItemPropertyDescriptor
 				(((ComposeableAdapterFactory)adapterFactory).getRootAdapterFactory(),
 				 getResourceLocator(),
 				 getString("_UI_ResizeConstraints_nonResizeHandleNames_feature"),
 				 getString("_UI_PropertyDescriptor_description", "_UI_ResizeConstraints_nonResizeHandleNames_feature", "_UI_ResizeConstraints_type"),
 				 GMFGenPackage.eINSTANCE.getResizeConstraints_NonResizeHandleNames(),
 				 false,
 				 false,
 				 false,
 				 ItemPropertyDescriptor.GENERIC_VALUE_IMAGE,
 				 null,
 				 null));
 	}
 
 	/**
 	 * This returns ResizeConstraints.gif.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	@Override
 	public Object getImage(Object object) {
 		return overlayImage(object, getResourceLocator().getImage("full/obj16/ResizeConstraints"));
 	}
 
 	/**
 	 * This returns the label text for the adapted class.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
	 * @generated
 	 */
 	@Override
 	public String getText(Object object) {
 		ResizeConstraints resizeConstraints = (ResizeConstraints)object;
		return getString("_UI_ResizeConstraints_type") + " " + resizeConstraints.getResizeHandles();
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
 
 		switch (notification.getFeatureID(ResizeConstraints.class)) {
 			case GMFGenPackage.RESIZE_CONSTRAINTS__RESIZE_HANDLES:
 			case GMFGenPackage.RESIZE_CONSTRAINTS__NON_RESIZE_HANDLES:
 			case GMFGenPackage.RESIZE_CONSTRAINTS__RESIZE_HANDLE_NAMES:
 			case GMFGenPackage.RESIZE_CONSTRAINTS__NON_RESIZE_HANDLE_NAMES:
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
 		return EditorPlugin.INSTANCE;
 	}
 
 }
