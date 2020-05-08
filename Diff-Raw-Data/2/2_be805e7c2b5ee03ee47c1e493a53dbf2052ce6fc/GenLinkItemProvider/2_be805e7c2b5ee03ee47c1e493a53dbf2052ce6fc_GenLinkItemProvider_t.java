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
 import org.eclipse.emf.ecore.EStructuralFeature;
 import org.eclipse.emf.edit.provider.ComposeableAdapterFactory;
 import org.eclipse.emf.edit.provider.IEditingDomainItemProvider;
 import org.eclipse.emf.edit.provider.IItemLabelProvider;
 import org.eclipse.emf.edit.provider.IItemPropertyDescriptor;
 import org.eclipse.emf.edit.provider.IItemPropertySource;
 import org.eclipse.emf.edit.provider.IStructuredItemContentProvider;
 import org.eclipse.emf.edit.provider.ITreeItemContentProvider;
 import org.eclipse.emf.edit.provider.ItemPropertyDescriptor;
 import org.eclipse.emf.edit.provider.ViewerNotification;
 import org.eclipse.gmf.codegen.gmfgen.GMFGenFactory;
 import org.eclipse.gmf.codegen.gmfgen.GMFGenPackage;
 import org.eclipse.gmf.codegen.gmfgen.GenLink;
 import org.eclipse.gmf.codegen.gmfgen.presentation.EditorPlugin;
 
 /**
  * This is the item provider adapter for a {@link org.eclipse.gmf.codegen.gmfgen.GenLink} object.
  * <!-- begin-user-doc -->
  * <!-- end-user-doc -->
  * @generated
  */
 public class GenLinkItemProvider
 	extends GenCommonBaseItemProvider
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
 	public GenLinkItemProvider(AdapterFactory adapterFactory) {
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
 
 			addOutgoingCreationAllowedPropertyDescriptor(object);
 			addIncomingCreationAllowedPropertyDescriptor(object);
 			addViewDirectionAlignedWithModelPropertyDescriptor(object);
 			addCreateCommandClassNamePropertyDescriptor(object);
 			addReorientCommandClassNamePropertyDescriptor(object);
 			addTreeBranchPropertyDescriptor(object);
 		}
 		return itemPropertyDescriptors;
 	}
 
 	/**
 	 * This adds a property descriptor for the Outgoing Creation Allowed feature.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	protected void addOutgoingCreationAllowedPropertyDescriptor(Object object) {
 		itemPropertyDescriptors.add
 			(createItemPropertyDescriptor
 				(((ComposeableAdapterFactory)adapterFactory).getRootAdapterFactory(),
 				 getResourceLocator(),
 				 getString("_UI_GenLink_outgoingCreationAllowed_feature"),
 				 getString("_UI_PropertyDescriptor_description", "_UI_GenLink_outgoingCreationAllowed_feature", "_UI_GenLink_type"),
 				 GMFGenPackage.eINSTANCE.getGenLink_OutgoingCreationAllowed(),
 				 true,
 				 false,
 				 false,
 				 ItemPropertyDescriptor.BOOLEAN_VALUE_IMAGE,
 				 getString("_UI_DiagramElementPropertyCategory"),
 				 null));
 	}
 
 	/**
 	 * This adds a property descriptor for the Incoming Creation Allowed feature.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	protected void addIncomingCreationAllowedPropertyDescriptor(Object object) {
 		itemPropertyDescriptors.add
 			(createItemPropertyDescriptor
 				(((ComposeableAdapterFactory)adapterFactory).getRootAdapterFactory(),
 				 getResourceLocator(),
 				 getString("_UI_GenLink_incomingCreationAllowed_feature"),
 				 getString("_UI_PropertyDescriptor_description", "_UI_GenLink_incomingCreationAllowed_feature", "_UI_GenLink_type"),
 				 GMFGenPackage.eINSTANCE.getGenLink_IncomingCreationAllowed(),
 				 true,
 				 false,
 				 false,
 				 ItemPropertyDescriptor.BOOLEAN_VALUE_IMAGE,
 				 getString("_UI_DiagramElementPropertyCategory"),
 				 null));
 	}
 
 	/**
 	 * This adds a property descriptor for the View Direction Aligned With Model feature.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	protected void addViewDirectionAlignedWithModelPropertyDescriptor(Object object) {
 		itemPropertyDescriptors.add
 			(createItemPropertyDescriptor
 				(((ComposeableAdapterFactory)adapterFactory).getRootAdapterFactory(),
 				 getResourceLocator(),
 				 getString("_UI_GenLink_viewDirectionAlignedWithModel_feature"),
 				 getString("_UI_PropertyDescriptor_description", "_UI_GenLink_viewDirectionAlignedWithModel_feature", "_UI_GenLink_type"),
 				 GMFGenPackage.eINSTANCE.getGenLink_ViewDirectionAlignedWithModel(),
 				 true,
 				 false,
 				 false,
 				 ItemPropertyDescriptor.BOOLEAN_VALUE_IMAGE,
 				 getString("_UI_DiagramElementPropertyCategory"),
 				 null));
 	}
 
 	/**
 	 * This adds a property descriptor for the Create Command Class Name feature.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	protected void addCreateCommandClassNamePropertyDescriptor(Object object) {
 		itemPropertyDescriptors.add
 			(createItemPropertyDescriptor
 				(((ComposeableAdapterFactory)adapterFactory).getRootAdapterFactory(),
 				 getResourceLocator(),
 				 getString("_UI_GenLink_createCommandClassName_feature"),
 				 getString("_UI_PropertyDescriptor_description", "_UI_GenLink_createCommandClassName_feature", "_UI_GenLink_type"),
 				 GMFGenPackage.eINSTANCE.getGenLink_CreateCommandClassName(),
 				 true,
 				 false,
 				 false,
 				 ItemPropertyDescriptor.GENERIC_VALUE_IMAGE,
 				 getString("_UI_ClassNamesPropertyCategory"),
 				 null));
 	}
 
 	/**
 	 * This adds a property descriptor for the Reorient Command Class Name feature.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	protected void addReorientCommandClassNamePropertyDescriptor(Object object) {
 		itemPropertyDescriptors.add
 			(createItemPropertyDescriptor
 				(((ComposeableAdapterFactory)adapterFactory).getRootAdapterFactory(),
 				 getResourceLocator(),
 				 getString("_UI_GenLink_reorientCommandClassName_feature"),
 				 getString("_UI_PropertyDescriptor_description", "_UI_GenLink_reorientCommandClassName_feature", "_UI_GenLink_type"),
 				 GMFGenPackage.eINSTANCE.getGenLink_ReorientCommandClassName(),
 				 true,
 				 false,
 				 false,
 				 ItemPropertyDescriptor.GENERIC_VALUE_IMAGE,
 				 getString("_UI_ClassNamesPropertyCategory"),
 				 null));
 	}
 
 	/**
 	 * This adds a property descriptor for the Tree Branch feature.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	protected void addTreeBranchPropertyDescriptor(Object object) {
 		itemPropertyDescriptors.add
 			(createItemPropertyDescriptor
 				(((ComposeableAdapterFactory)adapterFactory).getRootAdapterFactory(),
 				 getResourceLocator(),
 				 getString("_UI_GenLink_treeBranch_feature"),
 				 getString("_UI_PropertyDescriptor_description", "_UI_GenLink_treeBranch_feature", "_UI_GenLink_type"),
 				 GMFGenPackage.eINSTANCE.getGenLink_TreeBranch(),
 				 true,
 				 false,
 				 false,
 				 ItemPropertyDescriptor.BOOLEAN_VALUE_IMAGE,
				 getString("_UI_DiagramElementPropertyCategory"),
 				 null));
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
 			childrenFeatures.add(GMFGenPackage.eINSTANCE.getGenLink_ModelFacet());
 			childrenFeatures.add(GMFGenPackage.eINSTANCE.getGenLink_Labels());
 			childrenFeatures.add(GMFGenPackage.eINSTANCE.getGenLink_CreationConstraints());
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
 	 * This returns GenLink.gif.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	@Override
 	public Object getImage(Object object) {
 		return overlayImage(object, getResourceLocator().getImage("full/obj16/GenLink"));
 	}
 
 	/**
 	 * This returns the label text for the adapted class.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	@Override
 	public String getText(Object object) {
 		String label = ((GenLink)object).getEditPartClassName();
 		return label == null || label.length() == 0 ?
 			getString("_UI_GenLink_type") :
 			getString("_UI_GenLink_type") + " " + label;
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
 
 		switch (notification.getFeatureID(GenLink.class)) {
 			case GMFGenPackage.GEN_LINK__OUTGOING_CREATION_ALLOWED:
 			case GMFGenPackage.GEN_LINK__INCOMING_CREATION_ALLOWED:
 			case GMFGenPackage.GEN_LINK__VIEW_DIRECTION_ALIGNED_WITH_MODEL:
 			case GMFGenPackage.GEN_LINK__CREATE_COMMAND_CLASS_NAME:
 			case GMFGenPackage.GEN_LINK__REORIENT_COMMAND_CLASS_NAME:
 			case GMFGenPackage.GEN_LINK__TREE_BRANCH:
 				fireNotifyChanged(new ViewerNotification(notification, notification.getNotifier(), false, true));
 				return;
 			case GMFGenPackage.GEN_LINK__MODEL_FACET:
 			case GMFGenPackage.GEN_LINK__LABELS:
 			case GMFGenPackage.GEN_LINK__CREATION_CONSTRAINTS:
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
 				(GMFGenPackage.eINSTANCE.getGenLink_ModelFacet(),
 				 GMFGenFactory.eINSTANCE.createTypeLinkModelFacet()));
 
 		newChildDescriptors.add
 			(createChildParameter
 				(GMFGenPackage.eINSTANCE.getGenLink_ModelFacet(),
 				 GMFGenFactory.eINSTANCE.createFeatureLinkModelFacet()));
 
 		newChildDescriptors.add
 			(createChildParameter
 				(GMFGenPackage.eINSTANCE.getGenLink_Labels(),
 				 GMFGenFactory.eINSTANCE.createGenLinkLabel()));
 
 		newChildDescriptors.add
 			(createChildParameter
 				(GMFGenPackage.eINSTANCE.getGenLink_CreationConstraints(),
 				 GMFGenFactory.eINSTANCE.createGenLinkConstraints()));
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
