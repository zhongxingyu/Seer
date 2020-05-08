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
 import org.eclipse.gmf.codegen.gmfgen.DefaultSizeAttributes;
 import org.eclipse.gmf.codegen.gmfgen.GMFGenPackage;
 import org.eclipse.gmf.codegen.gmfgen.presentation.EditorPlugin;
 
 /**
  * This is the item provider adapter for a {@link org.eclipse.gmf.codegen.gmfgen.DefaultSizeAttributes} object.
  * <!-- begin-user-doc -->
  * <!-- end-user-doc -->
  * @generated
  */
 public class DefaultSizeAttributesItemProvider
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
 	public DefaultSizeAttributesItemProvider(AdapterFactory adapterFactory) {
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
 
 			addWidthPropertyDescriptor(object);
 			addHeightPropertyDescriptor(object);
 		}
 		return itemPropertyDescriptors;
 	}
 
 	/**
 	 * This adds a property descriptor for the Width feature.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	protected void addWidthPropertyDescriptor(Object object) {
 		itemPropertyDescriptors.add
 			(createItemPropertyDescriptor
 				(((ComposeableAdapterFactory)adapterFactory).getRootAdapterFactory(),
 				 getResourceLocator(),
 				 getString("_UI_DefaultSizeAttributes_width_feature"),
 				 getString("_UI_PropertyDescriptor_description", "_UI_DefaultSizeAttributes_width_feature", "_UI_DefaultSizeAttributes_type"),
 				 GMFGenPackage.eINSTANCE.getDefaultSizeAttributes_Width(),
 				 true,
 				 false,
 				 false,
 				 ItemPropertyDescriptor.INTEGRAL_VALUE_IMAGE,
 				 null,
 				 null));
 	}
 
 	/**
 	 * This adds a property descriptor for the Height feature.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	protected void addHeightPropertyDescriptor(Object object) {
 		itemPropertyDescriptors.add
 			(createItemPropertyDescriptor
 				(((ComposeableAdapterFactory)adapterFactory).getRootAdapterFactory(),
 				 getResourceLocator(),
 				 getString("_UI_DefaultSizeAttributes_height_feature"),
 				 getString("_UI_PropertyDescriptor_description", "_UI_DefaultSizeAttributes_height_feature", "_UI_DefaultSizeAttributes_type"),
 				 GMFGenPackage.eINSTANCE.getDefaultSizeAttributes_Height(),
 				 true,
 				 false,
 				 false,
 				 ItemPropertyDescriptor.INTEGRAL_VALUE_IMAGE,
 				 null,
 				 null));
 	}
 
 	/**
 	 * This returns DefaultSizeAttributes.gif.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	@Override
 	public Object getImage(Object object) {
 		return overlayImage(object, getResourceLocator().getImage("full/obj16/DefaultSizeAttributes"));
 	}
 
 	/**
 	 * This returns the label text for the adapted class.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
	 * @generated NOT
 	 */
 	@Override
 	public String getText(Object object) {
 		DefaultSizeAttributes defaultSizeAttributes = (DefaultSizeAttributes)object;
		return getString("_UI_DefaultSizeAttributes_type") + " " + defaultSizeAttributes.getWidth() + "," + defaultSizeAttributes.getHeight();
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
 
 		switch (notification.getFeatureID(DefaultSizeAttributes.class)) {
 			case GMFGenPackage.DEFAULT_SIZE_ATTRIBUTES__WIDTH:
 			case GMFGenPackage.DEFAULT_SIZE_ATTRIBUTES__HEIGHT:
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
