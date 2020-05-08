 /**
  * <copyright>
  * </copyright>
  *
  * $Id$
  */
 package org.eclipse.stem.definitions.labels.provider;
 
 
 import java.util.Collection;
 import java.util.List;
 
 import org.eclipse.emf.common.notify.AdapterFactory;
 import org.eclipse.emf.common.notify.Notification;
 
 import org.eclipse.emf.common.util.ResourceLocator;
 import org.eclipse.emf.common.util.URI;
 
 import org.eclipse.emf.edit.provider.ComposeableAdapterFactory;
 import org.eclipse.emf.edit.provider.IEditingDomainItemProvider;
 import org.eclipse.emf.edit.provider.IItemLabelProvider;
 import org.eclipse.emf.edit.provider.IItemPropertyDescriptor;
 import org.eclipse.emf.edit.provider.IItemPropertySource;
 import org.eclipse.emf.edit.provider.IStructuredItemContentProvider;
 import org.eclipse.emf.edit.provider.ITreeItemContentProvider;
 
 import org.eclipse.stem.core.graph.provider.StaticNodeLabelItemProvider;
 
 import org.eclipse.stem.definitions.edges.provider.DefinitionsEditPlugin;
 
 import org.eclipse.stem.definitions.labels.EarthScienceLabel;
 import org.eclipse.stem.definitions.labels.LabelsPackage;
 
 /**
  * This is the item provider adapter for a {@link org.eclipse.stem.definitions.labels.EarthScienceLabel} object.
  * <!-- begin-user-doc -->
  * <!-- end-user-doc -->
  * @generated
  */
 public class EarthScienceLabelItemProvider
 	extends StaticNodeLabelItemProvider
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
 	public EarthScienceLabelItemProvider(AdapterFactory adapterFactory) {
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
 
 			addCurrentEarthScienceValuePropertyDescriptor(object);
 		}
 		return itemPropertyDescriptors;
 	}
 
 	/**
 	 * This adds a property descriptor for the Current Earth Science Value feature.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	protected void addCurrentEarthScienceValuePropertyDescriptor(Object object) {
 		itemPropertyDescriptors.add
 			(createItemPropertyDescriptor
 				(((ComposeableAdapterFactory)adapterFactory).getRootAdapterFactory(),
 				 getResourceLocator(),
 				 getString("_UI_EarthScienceLabel_currentEarthScienceValue_feature"), //$NON-NLS-1$
 				 getString("_UI_PropertyDescriptor_description", "_UI_EarthScienceLabel_currentEarthScienceValue_feature", "_UI_EarthScienceLabel_type"), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
 				 LabelsPackage.Literals.EARTH_SCIENCE_LABEL__CURRENT_EARTH_SCIENCE_VALUE,
 				 false,
				 false,
				 false,
 				 null,
 				 null,
 				 null));
 	}
 
 	/**
 	 * This returns EarthScienceLabel.gif.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	@Override
 	public Object getImage(Object object) {
 		return overlayImage(object, getResourceLocator().getImage("full/obj16/EarthScienceLabel")); //$NON-NLS-1$
 	}
 
 	/**
 	 * This returns the label text for the adapted class.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	@Override
 	public String getText(Object object) {
 		URI labelValue = ((EarthScienceLabel)object).getURI();
 		String label = labelValue == null ? null : labelValue.toString();
 		return label == null || label.length() == 0 ?
 			getString("_UI_EarthScienceLabel_type") : //$NON-NLS-1$
 			getString("_UI_EarthScienceLabel_type") + " " + label; //$NON-NLS-1$ //$NON-NLS-2$
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
 		return DefinitionsEditPlugin.INSTANCE;
 	}
 
 }
