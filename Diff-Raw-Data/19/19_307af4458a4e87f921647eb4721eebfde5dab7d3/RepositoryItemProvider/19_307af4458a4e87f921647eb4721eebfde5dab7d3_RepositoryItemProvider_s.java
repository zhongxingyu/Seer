 /**
  */
 package org.obeonetwork.dsl.gen.eclipse.provider;
 
 
 import java.util.Collection;
 import java.util.List;
 
 import org.eclipse.emf.common.notify.AdapterFactory;
 import org.eclipse.emf.common.notify.Notification;
 import org.eclipse.emf.ecore.EStructuralFeature;
 import org.eclipse.emf.edit.provider.IEditingDomainItemProvider;
 import org.eclipse.emf.edit.provider.IItemLabelProvider;
 import org.eclipse.emf.edit.provider.IItemPropertyDescriptor;
 import org.eclipse.emf.edit.provider.IItemPropertySource;
 import org.eclipse.emf.edit.provider.IStructuredItemContentProvider;
 import org.eclipse.emf.edit.provider.ITreeItemContentProvider;
 import org.eclipse.emf.edit.provider.ViewerNotification;
 import org.obeonetwork.dsl.gen.eclipse.Application;
 import org.obeonetwork.dsl.gen.eclipse.EclipseFactory;
 import org.obeonetwork.dsl.gen.eclipse.EclipsePackage;
 import org.obeonetwork.dsl.gen.eclipse.Repository;
 
 /**
  * This is the item provider adapter for a {@link org.obeonetwork.dsl.gen.eclipse.Repository} object.
  * <!-- begin-user-doc -->
  * <!-- end-user-doc -->
  * @generated
  */
 public class RepositoryItemProvider
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
 	public RepositoryItemProvider(AdapterFactory adapterFactory) {
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
 
 		}
 		return itemPropertyDescriptors;
 	}
 
 	/**
 	 * This specifies how to implement {@link #getChildren} and is used to deduce an appropriate feature for an
 	 * {@link org.eclipse.emf.edit.command.AddCommand}, {@link org.eclipse.emf.edit.command.RemoveCommand} or
 	 * {@link org.eclipse.emf.edit.command.MoveCommand} in {@link #createCommand}.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	@Override
 	public Collection<? extends EStructuralFeature> getChildrenFeatures(Object object) {
 		if (childrenFeatures == null) {
 			super.getChildrenFeatures(object);
 			childrenFeatures.add(EclipsePackage.Literals.REPOSITORY__REPOSITORY_CATEGORIES);
 		}
 		return childrenFeatures;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	@Override
 	protected EStructuralFeature getChildFeature(Object object, Object child) {
 		// Check the type of the specified child object and return the proper feature to use for
 		// adding (see {@link AddCommand}) it as a child.
 
 		return super.getChildFeature(object, child);
 	}
 
 	/**
 	 * This returns Repository.gif.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	@Override
 	public Object getImage(Object object) {
 		return overlayImage(object, getResourceLocator().getImage("full/obj16/Repository"));
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
 		String label = ((Repository)object).getName();
 		String id = ((Repository)object).getID();
 		String baseID = "";
		if ((((Application)((Repository)object).eContainer()).getBaseNamespace().length() == 0) || 
				(((Application)((Repository)object).eContainer()).getBaseNamespace() == null)) {
 			baseID = " complete the baseNamespace field";
 		} else {
 			baseID = ((Application)((Repository)object).eContainer()).getBaseNamespace();			
 		}
 		
 		if ((label == null || label.length() == 0) && (id == null || id.length() == 0)) {
 			return getString("_UI_Repository_type");			
 		} else if ((label == null || label.length() == 0) && (id != null || id.length() != 0)) {
 			return getString("_UI_Repository_type") + " " + baseID + "." + id + " -- no name define";
 		} else if ((label != null || label.length() != 0) && (id == null || id.length() == 0)) {
 			return getString("_UI_Repsoitory_type") + " no ID define -- " + label;
 		} else {
 			return getString("_UI_Repository_type") + " " + baseID + "." + id + " -- " + label;
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
 
 		switch (notification.getFeatureID(Repository.class)) {
 			case EclipsePackage.REPOSITORY__REPOSITORY_CATEGORIES:
 				fireNotifyChanged(new ViewerNotification(notification, notification.getNotifier(), true, false));
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
 
 		newChildDescriptors.add
 			(createChildParameter
 				(EclipsePackage.Literals.REPOSITORY__REPOSITORY_CATEGORIES,
 				 EclipseFactory.eINSTANCE.createRepositoryCategory()));
 	}
 
 }
