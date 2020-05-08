 /**
  * <copyright> Copyright (c) 2008-2009 Jonas Helming, Maximilian Koegel. All rights reserved. This program and the
  * accompanying materials are made available under the terms of the Eclipse Public License v1.0 which accompanies this
  * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html </copyright>
  */
 package org.eclipse.emf.emfstore.client.ui.views.users;
 
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 import org.eclipse.core.runtime.IAdapterFactory;
 import org.eclipse.emf.common.notify.AdapterFactory;
 import org.eclipse.emf.common.util.BasicEList;
 import org.eclipse.emf.common.util.EList;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.edit.provider.ComposedAdapterFactory;
 import org.eclipse.emf.edit.provider.IItemPropertyDescriptor;
 import org.eclipse.emf.edit.provider.IItemPropertySource;
 import org.eclipse.emf.edit.provider.ItemPropertyDescriptor.PropertyValueWrapper;
 import org.eclipse.emf.edit.ui.provider.AdapterFactoryContentProvider;
 import org.eclipse.emf.edit.ui.provider.PropertyDescriptor;
 import org.eclipse.emf.edit.ui.provider.PropertySource;
 import org.eclipse.emf.emfstore.server.model.ModelFactory;
 import org.eclipse.emf.emfstore.server.model.ProjectId;
 import org.eclipse.emf.emfstore.server.model.accesscontrol.ACOrgUnit;
 import org.eclipse.emf.emfstore.server.model.accesscontrol.AccesscontrolFactory;
 import org.eclipse.emf.emfstore.server.model.accesscontrol.AccesscontrolPackage;
 import org.eclipse.emf.emfstore.server.model.accesscontrol.RoleAssignment;
 import org.eclipse.jface.viewers.CellEditor;
 import org.eclipse.jface.viewers.DialogCellEditor;
 import org.eclipse.jface.viewers.ILabelProvider;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Control;
 import org.eclipse.ui.dialogs.CheckedTreeSelectionDialog;
 import org.eclipse.ui.views.properties.IPropertyDescriptor;
 import org.eclipse.ui.views.properties.IPropertySource;
 
 /**
  * @author boehlke
  * 
  */
 public class PropertySourceAdapterFactory implements IAdapterFactory {
 
 	private class CustomPropertyDescriptor extends PropertyDescriptor {
 
 		private UserUiController controller;
 
 		public CustomPropertyDescriptor(ACOrgUnit object, IItemPropertyDescriptor itemPropertyDescriptor) {
 			super(object, itemPropertyDescriptor);
 			this.controller = UserUiController.getInstance();
 		}
 
 		@Override
 		public CellEditor createPropertyEditor(Composite composite) {
 			return new DialogCellEditor(composite) {
 
 				@Override
 				protected Object openDialogBox(Control cellEditorWindow) {
 					CheckedTreeSelectionDialog checkedTreeSelectionDialog = new CheckedTreeRoleSelectionDialog(
 						cellEditorWindow.getShell(), controller);
 					EList<RoleAssignment> roles = ((ACOrgUnit) object).getRoles();
 					checkedTreeSelectionDialog.setInput(roles);
 					checkedTreeSelectionDialog.open();
 					Object[] result = checkedTreeSelectionDialog.getResult();
 
 					if (result == null) {
 						return null;
 					}
 
 					List<RoleAssignment> added = new ArrayList<RoleAssignment>();
 					Set<RoleAssignment> removed = new HashSet<RoleAssignment>();
 
 					removed.addAll(roles);
 
 					RESULT: for (Object object : result) {
 						RoleSelection sel = (RoleSelection) object;
 
 						if (sel.getRole() == null) {
 							continue;
 						}
 
 						String projectId = null;
 						if (sel.getProject() != null) {
 							projectId = sel.getProject().getProjectId().getId();
 						}
 						for (RoleAssignment assignmentData : roles) {
 							ProjectId projectId2 = assignmentData.getProjectId();
 							if (projectId2 != null && projectId2.equals(projectId)
 								&& assignmentData.getRole().equals(sel.getRole())) {
 								removed.remove(assignmentData);
 								continue RESULT;
 							}
 						}
 
 						// TODO: send to server
 						RoleAssignment newAssignment = AccesscontrolFactory.eINSTANCE.createRoleAssignment();
 
 						if (projectId != null) {
 							ProjectId id = ModelFactory.eINSTANCE.createProjectId();
 							id.setId(projectId);
 							newAssignment.setProjectId(id);
 						}
 						newAssignment.setRole(sel.getRole());
 						added.add(newAssignment);
 					}
 
 					// for (RoleAssignment RoleAssignment : added) {
 					// userData.getRoles().add(RoleAssignment);
 					// }
 					//
 					// for (RoleAssignment roleAssignment : removed) {
 					// userData.getRoles().remove(roleAssignment);
 					// }
 
 					List<RoleAssignment> assignments = new ArrayList<RoleAssignment>();
 					for (Object object : result) {
 						RoleSelection sel = (RoleSelection) object;
						if (sel.getRole() == null) {
							continue;
						}
 						RoleAssignment assignment = AccesscontrolFactory.eINSTANCE.createRoleAssignment();
 						assignment.setRole(sel.getRole());
 						if (sel.getProject() != null) {
 							assignment.setProjectId(sel.getProject().getProjectId());
 						}
 						assignments.add(assignment);
 					}
 
 					return new BasicEList<Object>(assignments);
 				}
 			};
 		}
 	}
 
 	private class CustomPropertySource extends PropertySource {
 
 		public CustomPropertySource(Object object, IItemPropertySource itemPropertySource) {
 			super(object, itemPropertySource);
 		}
 
 		@Override
 		protected IPropertyDescriptor createPropertyDescriptor(IItemPropertyDescriptor itemPropertyDescriptor) {
 			Object object = getEditableValue();
 			if (itemPropertyDescriptor.getFeature(object) == AccesscontrolPackage.eINSTANCE.getACOrgUnit_Roles()) {
 				return new CustomPropertyDescriptor((ACOrgUnit) object, itemPropertyDescriptor);
 			}
 			return super.createPropertyDescriptor(itemPropertyDescriptor);
 		}
 
 	}
 
 	private class CustomAdapterFactoryContentProvider extends AdapterFactoryContentProvider {
 
 		public CustomAdapterFactoryContentProvider(AdapterFactory adapterFactory) {
 			super(adapterFactory);
 		}
 
 		@Override
 		protected IPropertySource createPropertySource(Object object, IItemPropertySource itemPropertySource) {
 			if (object instanceof ACOrgUnit) {
 				return new CustomPropertySource(object, itemPropertySource);
 			}
 			return super.createPropertySource(object, itemPropertySource);
 		}
 
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.core.runtime.IAdapterFactory#getAdapter(java.lang.Object, java.lang.Class)
 	 */
 	public Object getAdapter(Object adaptableObject, Class adapterType) {
 		if (adaptableObject instanceof EObject && adapterType == IPropertySource.class) {
 			EObject eObject = (EObject) adaptableObject;
 			AdapterFactory adapterFactory = new ComposedAdapterFactory(
 				ComposedAdapterFactory.Descriptor.Registry.INSTANCE);
 			final AdapterFactoryContentProvider contentProvider = new CustomAdapterFactoryContentProvider(
 				adapterFactory);
 			return new UnwrappingPropertySource(contentProvider.getPropertySource(adaptableObject));
 		}
 		return null;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.core.runtime.IAdapterFactory#getAdapterList()
 	 */
 	public Class[] getAdapterList() {
 		return new Class[] { IPropertySource.class };
 	}
 
 	/**
 	 * A property source which unwraps values that are wrapped in an EMF {@link PropertyValueWrapper}
 	 * 
 	 * @author vainolo
 	 * 
 	 */
 	public class UnwrappingPropertySource implements IPropertySource {
 		private IPropertySource source;
 
 		public UnwrappingPropertySource(final IPropertySource source) {
 			this.source = source;
 		}
 
 		public Object getEditableValue() {
 			Object value = source.getEditableValue();
 			if (value instanceof PropertyValueWrapper) {
 				PropertyValueWrapper wrapper = (PropertyValueWrapper) value;
 				return wrapper.getEditableValue(null);
 			} else {
 				return source.getEditableValue();
 			}
 		}
 
 		public IPropertyDescriptor[] getPropertyDescriptors() {
 			List<IPropertyDescriptor> descriptors = new ArrayList<IPropertyDescriptor>();
 			for (IPropertyDescriptor iPropertyDescriptor : source.getPropertyDescriptors()) {
 				descriptors.add(new PropertyDescriptorWrapper(iPropertyDescriptor, source.getEditableValue()));
 			}
 			return descriptors.toArray(new IPropertyDescriptor[0]);
 		}
 
 		public Object getPropertyValue(Object id) {
 			Object value = source.getPropertyValue(id);
 			if (value instanceof PropertyValueWrapper) {
 				PropertyValueWrapper wrapper = (PropertyValueWrapper) value;
 				return wrapper.getEditableValue(null);
 			} else {
 				return source.getPropertyValue(id);
 			}
 		}
 
 		public boolean isPropertySet(Object id) {
 			return source.isPropertySet(id);
 		}
 
 		public void resetPropertyValue(Object id) {
 			source.resetPropertyValue(id);
 		}
 
 		public void setPropertyValue(Object id, Object value) {
 			source.setPropertyValue(id, value);
 		}
 	}
 
 	/**
 	 * if the property is not editable by the user, returns no property editor
 	 * 
 	 * @author boehlke
 	 * 
 	 */
 	private static class PropertyDescriptorWrapper implements IPropertyDescriptor {
 
 		private IPropertyDescriptor descriptor;
 		private UserUiController controller;
 		private Object object;
 
 		public PropertyDescriptorWrapper(IPropertyDescriptor iPropertyDescriptor, Object object) {
 			this.descriptor = iPropertyDescriptor;
 			this.controller = UserUiController.getInstance();
 			this.object = object;
 		}
 
 		public CellEditor createPropertyEditor(Composite parent) {
 			if (controller.canChangeOrgUnit((ACOrgUnit) this.object)) {
 				return descriptor.createPropertyEditor(parent);
 			}
 			return null;
 		}
 
 		public String getCategory() {
 			return descriptor.getCategory();
 		}
 
 		public String getDescription() {
 			return descriptor.getDescription();
 		}
 
 		public String getDisplayName() {
 			return descriptor.getDisplayName();
 		}
 
 		public String[] getFilterFlags() {
 			return descriptor.getFilterFlags();
 		}
 
 		public Object getHelpContextIds() {
 			return descriptor.getHelpContextIds();
 		}
 
 		public Object getId() {
 			return descriptor.getId();
 		}
 
 		public ILabelProvider getLabelProvider() {
 			return descriptor.getLabelProvider();
 		}
 
 		public boolean isCompatibleWith(IPropertyDescriptor anotherProperty) {
 			return descriptor.isCompatibleWith(anotherProperty);
 		}
 
 	}
 }
