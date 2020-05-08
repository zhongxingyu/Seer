 /*******************************************************************************
  * Copyright (c) 2008-2011 Chair for Applied Software Engineering,
  * Technische Universitaet Muenchen.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  ******************************************************************************/
 package org.eclipse.emf.emfstore.internal.client.model.provider;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.LinkedHashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.eclipse.emf.common.notify.AdapterFactory;
 import org.eclipse.emf.common.notify.Notification;
 import org.eclipse.emf.common.notify.Notifier;
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
 import org.eclipse.emf.emfstore.internal.client.model.ModelPackage;
 import org.eclipse.emf.emfstore.internal.client.model.ProjectSpace;
 import org.eclipse.emf.emfstore.internal.common.model.ModelFactory;
 import org.eclipse.emf.emfstore.internal.common.model.Project;
 import org.eclipse.emf.emfstore.internal.common.model.provider.IdentifiableElementItemProvider;
 import org.eclipse.emf.emfstore.internal.server.model.versioning.VersioningFactory;
 
 /**
  * This is the item provider adapter for a {@link org.eclipse.emf.emfstore.internal.client.model.ProjectSpace} object.
  * <!--
  * begin-user-doc --> <!-- end-user-doc -->
  * 
  * @generated
  */
 public class ProjectSpaceItemProvider extends IdentifiableElementItemProvider implements IEditingDomainItemProvider,
 	IStructuredItemContentProvider, ITreeItemContentProvider, IItemLabelProvider, IItemPropertySource {
 
 	private Map<Project, ProjectSpace> projectToProjectSpaceMap;
 
 	/**
 	 * This constructs an instance from a factory and a notifier. <!--
 	 * begin-user-doc -->
 	 * 
 	 * @param adapterFactory the adapter factory
 	 *            <!-- end-user-doc -->
 	 * 
 	 * @generated NOT
 	 */
 	public ProjectSpaceItemProvider(AdapterFactory adapterFactory) {
 		super(adapterFactory);
 	}
 
 	/**
 	 * This returns the property descriptors for the adapted class. <!--
 	 * begin-user-doc --> <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	@Override
 	public List<IItemPropertyDescriptor> getPropertyDescriptors(Object object) {
 		if (itemPropertyDescriptors == null) {
 			super.getPropertyDescriptors(object);
 
 			addProjectNamePropertyDescriptor(object);
 			addProjectDescriptionPropertyDescriptor(object);
 			addUsersessionPropertyDescriptor(object);
 			addLastUpdatedPropertyDescriptor(object);
 			addResourceCountPropertyDescriptor(object);
 			addDirtyPropertyDescriptor(object);
 			addOldLogMessagesPropertyDescriptor(object);
 			addChangedSharedPropertiesPropertyDescriptor(object);
 		}
 		return itemPropertyDescriptors;
 	}
 
 	/**
 	 * This adds a property descriptor for the Project Name feature. <!--
 	 * begin-user-doc --> <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	protected void addProjectNamePropertyDescriptor(Object object) {
 		itemPropertyDescriptors.add(createItemPropertyDescriptor(
 			((ComposeableAdapterFactory) adapterFactory).getRootAdapterFactory(),
 			getResourceLocator(),
 			getString("_UI_ProjectSpace_projectName_feature"),
 			getString("_UI_PropertyDescriptor_description", "_UI_ProjectSpace_projectName_feature",
 				"_UI_ProjectSpace_type"), ModelPackage.Literals.PROJECT_SPACE__PROJECT_NAME, true, false, false,
 			ItemPropertyDescriptor.GENERIC_VALUE_IMAGE, null, null));
 	}
 
 	/**
 	 * This adds a property descriptor for the Project Description feature. <!--
 	 * begin-user-doc --> <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	protected void addProjectDescriptionPropertyDescriptor(Object object) {
 		itemPropertyDescriptors.add(createItemPropertyDescriptor(
 			((ComposeableAdapterFactory) adapterFactory).getRootAdapterFactory(),
 			getResourceLocator(),
 			getString("_UI_ProjectSpace_projectDescription_feature"),
 			getString("_UI_PropertyDescriptor_description", "_UI_ProjectSpace_projectDescription_feature",
 				"_UI_ProjectSpace_type"), ModelPackage.Literals.PROJECT_SPACE__PROJECT_DESCRIPTION, true, false, false,
 			ItemPropertyDescriptor.GENERIC_VALUE_IMAGE, null, null));
 	}
 
 	/**
 	 * This adds a property descriptor for the Usersession feature. <!--
 	 * begin-user-doc --> <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	protected void addUsersessionPropertyDescriptor(Object object) {
 		itemPropertyDescriptors.add(createItemPropertyDescriptor(
 			((ComposeableAdapterFactory) adapterFactory).getRootAdapterFactory(),
 			getResourceLocator(),
 			getString("_UI_ProjectSpace_usersession_feature"),
 			getString("_UI_PropertyDescriptor_description", "_UI_ProjectSpace_usersession_feature",
 				"_UI_ProjectSpace_type"), ModelPackage.Literals.PROJECT_SPACE__USERSESSION, true, false, true, null,
 			null, null));
 	}
 
 	/**
 	 * This adds a property descriptor for the Last Updated feature. <!--
 	 * begin-user-doc --> <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	protected void addLastUpdatedPropertyDescriptor(Object object) {
 		itemPropertyDescriptors.add(createItemPropertyDescriptor(
 			((ComposeableAdapterFactory) adapterFactory).getRootAdapterFactory(),
 			getResourceLocator(),
 			getString("_UI_ProjectSpace_lastUpdated_feature"),
 			getString("_UI_PropertyDescriptor_description", "_UI_ProjectSpace_lastUpdated_feature",
 				"_UI_ProjectSpace_type"), ModelPackage.Literals.PROJECT_SPACE__LAST_UPDATED, true, false, false,
 			ItemPropertyDescriptor.GENERIC_VALUE_IMAGE, null, null));
 	}
 
 	/**
 	 * This adds a property descriptor for the Resource Count feature. <!--
 	 * begin-user-doc --> <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	protected void addResourceCountPropertyDescriptor(Object object) {
 		itemPropertyDescriptors.add(createItemPropertyDescriptor(
 			((ComposeableAdapterFactory) adapterFactory).getRootAdapterFactory(),
 			getResourceLocator(),
 			getString("_UI_ProjectSpace_resourceCount_feature"),
 			getString("_UI_PropertyDescriptor_description", "_UI_ProjectSpace_resourceCount_feature",
 				"_UI_ProjectSpace_type"), ModelPackage.Literals.PROJECT_SPACE__RESOURCE_COUNT, true, false, false,
 			ItemPropertyDescriptor.INTEGRAL_VALUE_IMAGE, null, null));
 	}
 
 	/**
 	 * This adds a property descriptor for the Dirty feature. <!--
 	 * begin-user-doc --> <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	protected void addDirtyPropertyDescriptor(Object object) {
 		itemPropertyDescriptors.add(createItemPropertyDescriptor(
 			((ComposeableAdapterFactory) adapterFactory).getRootAdapterFactory(), getResourceLocator(),
 			getString("_UI_ProjectSpace_dirty_feature"),
 			getString("_UI_PropertyDescriptor_description", "_UI_ProjectSpace_dirty_feature", "_UI_ProjectSpace_type"),
 			ModelPackage.Literals.PROJECT_SPACE__DIRTY, true, false, false, ItemPropertyDescriptor.BOOLEAN_VALUE_IMAGE,
 			null, null));
 	}
 
 	/**
 	 * This adds a property descriptor for the Old Log Messages feature. <!--
 	 * begin-user-doc --> <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	protected void addOldLogMessagesPropertyDescriptor(Object object) {
 		itemPropertyDescriptors.add(createItemPropertyDescriptor(
 			((ComposeableAdapterFactory) adapterFactory).getRootAdapterFactory(),
 			getResourceLocator(),
 			getString("_UI_ProjectSpace_oldLogMessages_feature"),
 			getString("_UI_PropertyDescriptor_description", "_UI_ProjectSpace_oldLogMessages_feature",
 				"_UI_ProjectSpace_type"), ModelPackage.Literals.PROJECT_SPACE__OLD_LOG_MESSAGES, true, false, false,
 			ItemPropertyDescriptor.GENERIC_VALUE_IMAGE, null, null));
 	}
 
 	/**
 	 * This adds a property descriptor for the Changed Shared Properties feature.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	protected void addChangedSharedPropertiesPropertyDescriptor(Object object) {
 		itemPropertyDescriptors.add(createItemPropertyDescriptor(
 			((ComposeableAdapterFactory) adapterFactory).getRootAdapterFactory(),
 			getResourceLocator(),
 			getString("_UI_ProjectSpace_changedSharedProperties_feature"),
 			getString("_UI_PropertyDescriptor_description", "_UI_ProjectSpace_changedSharedProperties_feature",
 				"_UI_ProjectSpace_type"), ModelPackage.Literals.PROJECT_SPACE__CHANGED_SHARED_PROPERTIES, true, false,
 			true, null, null, null));
 	}
 
 	/**
 	 * This specifies how to implement {@link #getChildren} and is used to deduce an appropriate feature for an
 	 * {@link org.eclipse.emf.edit.command.AddCommand}, {@link org.eclipse.emf.edit.command.RemoveCommand} or
 	 * {@link org.eclipse.emf.edit.command.MoveCommand} in {@link #createCommand}.
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	@Override
 	public Collection<? extends EStructuralFeature> getChildrenFeatures(Object object) {
 		if (childrenFeatures == null) {
 			super.getChildrenFeatures(object);
 			childrenFeatures.add(ModelPackage.Literals.PROJECT_SPACE__PROJECT);
 			childrenFeatures.add(ModelPackage.Literals.PROJECT_SPACE__PROJECT_ID);
 			childrenFeatures.add(ModelPackage.Literals.PROJECT_SPACE__BASE_VERSION);
 			childrenFeatures.add(ModelPackage.Literals.PROJECT_SPACE__LOCAL_OPERATIONS);
 			childrenFeatures.add(ModelPackage.Literals.PROJECT_SPACE__WAITING_UPLOADS);
 			childrenFeatures.add(ModelPackage.Literals.PROJECT_SPACE__PROPERTIES);
 			childrenFeatures.add(ModelPackage.Literals.PROJECT_SPACE__LOCAL_CHANGE_PACKAGE);
 			childrenFeatures.add(ModelPackage.Literals.PROJECT_SPACE__MERGED_VERSION);
 		}
 		return childrenFeatures;
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
 
 	// begin of custom code
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.edit.provider.ItemProviderAdapter#getImage(java.lang.Object)
 	 * @generated NOT
 	 */
 	@Override
 	public Object getImage(Object object) {
 		return getResourceLocator().getImage("prj_obj.gif");
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.edit.provider.ItemProviderAdapter#getText(java.lang.Object)
 	 * @generated NOT
 	 */
 	@Override
 	public String getText(Object object) {
 		if (object instanceof ProjectSpace) {
 			ProjectSpace projectSpace = (ProjectSpace) object;
 			StringBuilder stringBuilder = new StringBuilder();
 			stringBuilder.append(projectSpace.getProjectName());
 			String string = stringBuilder.toString();
 			return string;
 		}
 		return getString("_UI_ProjectSpace_type");
 	}
 
 	// end of custom code
 
 	/**
 	 * {@inheritDoc} This handles model notifications by calling {@link #updateChildren} to update any cached children
 	 * and by creating a
 	 * viewer notification, which it passes to {@link #fireNotifyChanged}. <!--
 	 * begin-user-doc --> <!-- end-user-doc -->
 	 * 
 	 * @generated NOT
 	 */
 	@Override
 	public void notifyChanged(Notification notification) {
 		// in case the notification comes in from a project then issue a viewer
 		// update on the appropriate projectspace
 
 		if (notification != null && getProjectToProjectSpaceMap().get(notification.getNotifier()) != null) {
 			ProjectSpace projectSpace = getProjectToProjectSpaceMap().get(notification.getNotifier());
 			fireNotifyChanged(new ViewerNotification(notification, projectSpace, true, true));
 			return;
 		}
 		notifyChangedGen(notification);
 	}
 
 	/**
 	 * @generated
 	 */
 	private void notifyChangedGen(Notification notification) {
 		updateChildren(notification);
 
 		switch (notification.getFeatureID(ProjectSpace.class)) {
 		case ModelPackage.PROJECT_SPACE__PROJECT_NAME:
 		case ModelPackage.PROJECT_SPACE__PROJECT_DESCRIPTION:
 		case ModelPackage.PROJECT_SPACE__LAST_UPDATED:
 		case ModelPackage.PROJECT_SPACE__RESOURCE_COUNT:
 		case ModelPackage.PROJECT_SPACE__DIRTY:
 		case ModelPackage.PROJECT_SPACE__OLD_LOG_MESSAGES:
 			fireNotifyChanged(new ViewerNotification(notification, notification.getNotifier(), false, true));
 			return;
 		case ModelPackage.PROJECT_SPACE__PROJECT:
 		case ModelPackage.PROJECT_SPACE__PROJECT_ID:
 		case ModelPackage.PROJECT_SPACE__BASE_VERSION:
 		case ModelPackage.PROJECT_SPACE__LOCAL_OPERATIONS:
 		case ModelPackage.PROJECT_SPACE__WAITING_UPLOADS:
 		case ModelPackage.PROJECT_SPACE__PROPERTIES:
 		case ModelPackage.PROJECT_SPACE__LOCAL_CHANGE_PACKAGE:
 		case ModelPackage.PROJECT_SPACE__MERGED_VERSION:
 			fireNotifyChanged(new ViewerNotification(notification, notification.getNotifier(), true, false));
 			return;
 		}
 		super.notifyChanged(notification);
 	}
 
 	/**
 	 * This adds {@link org.eclipse.emf.edit.command.CommandParameter}s
 	 * describing the children that can be created under this object. <!--
 	 * begin-user-doc --> <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	@Override
 	protected void collectNewChildDescriptors(Collection<Object> newChildDescriptors, Object object) {
 		super.collectNewChildDescriptors(newChildDescriptors, object);
 
 		newChildDescriptors.add(createChildParameter(ModelPackage.Literals.PROJECT_SPACE__PROJECT,
 			ModelFactory.eINSTANCE.createProject()));
 
 		newChildDescriptors.add(createChildParameter(ModelPackage.Literals.PROJECT_SPACE__PROJECT_ID,
			org.eclipse.emf.emfstore.server.model.ModelFactory.eINSTANCE.createProjectId()));
 
 		newChildDescriptors.add(createChildParameter(ModelPackage.Literals.PROJECT_SPACE__BASE_VERSION,
 			VersioningFactory.eINSTANCE.createPrimaryVersionSpec()));
 
 		newChildDescriptors.add(createChildParameter(ModelPackage.Literals.PROJECT_SPACE__LOCAL_OPERATIONS,
 			org.eclipse.emf.emfstore.internal.client.model.ModelFactory.eINSTANCE.createOperationComposite()));
 
 		newChildDescriptors.add(createChildParameter(ModelPackage.Literals.PROJECT_SPACE__WAITING_UPLOADS,
			org.eclipse.emf.emfstore.server.model.ModelFactory.eINSTANCE.createFileIdentifier()));
 
 		newChildDescriptors.add(createChildParameter(ModelPackage.Literals.PROJECT_SPACE__PROPERTIES,
 			ModelFactory.eINSTANCE.createEMFStoreProperty()));
 
 		newChildDescriptors.add(createChildParameter(ModelPackage.Literals.PROJECT_SPACE__LOCAL_CHANGE_PACKAGE,
 			VersioningFactory.eINSTANCE.createChangePackage()));
 
 		newChildDescriptors.add(createChildParameter(ModelPackage.Literals.PROJECT_SPACE__MERGED_VERSION,
 			VersioningFactory.eINSTANCE.createPrimaryVersionSpec()));
 	}
 
 	/**
 	 * This returns the label text for {@link org.eclipse.emf.edit.command.CreateChildCommand}.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	@Override
 	public String getCreateChildText(Object owner, Object feature, Object child, Collection<?> selection) {
 		Object childFeature = feature;
 		Object childObject = child;
 
 		boolean qualify = childFeature == ModelPackage.Literals.PROJECT_SPACE__BASE_VERSION
 			|| childFeature == ModelPackage.Literals.PROJECT_SPACE__MERGED_VERSION;
 
 		if (qualify) {
 			return getString("_UI_CreateChild_text2", new Object[] { getTypeText(childObject),
 				getFeatureText(childFeature), getTypeText(owner) });
 		}
 		return super.getCreateChildText(owner, feature, child, selection);
 	}
 
 	/**
 	 * Return the resource locator for this item provider's resources. <!--
 	 * begin-user-doc --> <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	@Override
 	public ResourceLocator getResourceLocator() {
 		return ClientModelEditPlugin.INSTANCE;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.edit.provider.ItemProviderAdapter#setTarget(org.eclipse.emf.common.notify.Notifier)
 	 */
 	@Override
 	public void setTarget(Notifier target) {
 		super.setTarget(target);
 		// also register for notifications on the corresponding project to be
 		// able to trigger viewer updates accordingly, see method notifyChanged().
 		if (target instanceof ProjectSpace) {
 			ProjectSpace projectSpace = (ProjectSpace) target;
 			Project project = projectSpace.getProject();
 			project.eAdapters().add(this);
 			setTarget(project);
 			getProjectToProjectSpaceMap().put(project, projectSpace);
 		}
 	}
 
 	@Override
 	public Collection<?> getChildren(Object object) {
 		if (object instanceof ProjectSpace) {
 			final Project project = ((ProjectSpace) object).getProject();
 			if (project == null) {
 				return Collections.EMPTY_LIST;
 			}
 			// TODO: find a better way
 			return project.getModelElements();
 		}
 		return new ArrayList<Object>();
 
 	}
 
 	private Map<Project, ProjectSpace> getProjectToProjectSpaceMap() {
 
 		if (projectToProjectSpaceMap == null) {
 			projectToProjectSpaceMap = new LinkedHashMap<Project, ProjectSpace>();
 		}
 
 		return projectToProjectSpaceMap;
 	}
 }
