 /*******************************************************************************
  * Copyright (c) 2011, 2013 Formal Mind GmbH and University of Dusseldorf.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  *     Michael Jastram - initial API and implementation
  ******************************************************************************/
 
 package org.eclipse.rmf.reqif10.pror.provider;
 
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.GregorianCalendar;
 import java.util.List;
 
 
 import org.eclipse.emf.common.command.Command;
 import org.eclipse.emf.common.command.CommandWrapper;
 import org.eclipse.emf.common.command.CompoundCommand;
 import org.eclipse.emf.common.notify.AdapterFactory;
 import org.eclipse.emf.common.notify.Notification;
 import org.eclipse.emf.common.util.ResourceLocator;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.ecore.EStructuralFeature;
 import org.eclipse.emf.edit.command.SetCommand;
 import org.eclipse.emf.edit.domain.EditingDomain;
 import org.eclipse.emf.edit.provider.IDisposable;
 import org.eclipse.emf.edit.provider.IEditingDomainItemProvider;
 import org.eclipse.emf.edit.provider.IItemLabelProvider;
 import org.eclipse.emf.edit.provider.IItemPropertyDescriptor;
 import org.eclipse.emf.edit.provider.IItemPropertySource;
 import org.eclipse.emf.edit.provider.IStructuredItemContentProvider;
 import org.eclipse.emf.edit.provider.ITreeItemContentProvider;
 import org.eclipse.emf.edit.provider.ItemProviderAdapter;
 import org.eclipse.emf.edit.provider.ViewerNotification;
 import org.eclipse.rmf.reqif10.Identifiable;
 import org.eclipse.rmf.reqif10.ReqIF10Package;
 import org.eclipse.rmf.reqif10.ReqIFContent;
 import org.eclipse.rmf.reqif10.common.util.ReqIF10Util;
 
 /**
  * This is the item provider adapter for a {@link org.eclipse.rmf.reqif10.ReqIFContent} object.
  * <!-- begin-user-doc -->
  * <p>This ItemProvider is special, as it contains only virutal children that group
  * the "real" children into more useful groups. To achieve this, we apply the
  * pattern described in the EMF book in Chapter 19 (Non-Model intermediary View
  * Objects).</p>
  * 
  * <p>This means, amongst other things, that we changed this ItemProvider to be
  * stateful.</p> 
  * <!-- end-user-doc -->
  * @generated
  */
 public class ReqIFContentItemProvider
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
 	public ReqIFContentItemProvider(AdapterFactory adapterFactory) {
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
 			childrenFeatures.add(ReqIF10Package.Literals.REQ_IF_CONTENT__DATATYPES);
 			childrenFeatures.add(ReqIF10Package.Literals.REQ_IF_CONTENT__SPEC_TYPES);
 			childrenFeatures.add(ReqIF10Package.Literals.REQ_IF_CONTENT__SPEC_OBJECTS);
 			childrenFeatures.add(ReqIF10Package.Literals.REQ_IF_CONTENT__SPEC_RELATIONS);
 			childrenFeatures.add(ReqIF10Package.Literals.REQ_IF_CONTENT__SPECIFICATIONS);
 			childrenFeatures.add(ReqIF10Package.Literals.REQ_IF_CONTENT__SPEC_RELATION_GROUPS);
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
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	@Override
 	protected boolean shouldComposeCreationImage() {
 		return true;
 	}
 
 	@Override
 	public Object getImage(Object object) {
 		return overlayImage(object, getResourceLocator().getImage("full/obj16/ReqIfContent.png"));
 	}
 
 	/**
 	 * This returns the label text for the adapted class.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	@Override
 	public String getText(Object object) {
 		return getString("_UI_ReqIFContent_type");
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
 
 		switch (notification.getFeatureID(ReqIFContent.class)) {
 			case ReqIF10Package.REQ_IF_CONTENT__DATATYPES:
 			case ReqIF10Package.REQ_IF_CONTENT__SPEC_TYPES:
 			case ReqIF10Package.REQ_IF_CONTENT__SPEC_OBJECTS:
 			case ReqIF10Package.REQ_IF_CONTENT__SPEC_RELATIONS:
 			case ReqIF10Package.REQ_IF_CONTENT__SPECIFICATIONS:
 			case ReqIF10Package.REQ_IF_CONTENT__SPEC_RELATION_GROUPS:
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
 	}
 
 	/**
 	 * Return the resource locator for this item provider's resources.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	@Override
 	public ResourceLocator getResourceLocator() {
 		return Reqif10EditPlugin.INSTANCE;
 	}
 	
 	@SuppressWarnings("rawtypes")
 	protected List children = null;
 
 	@SuppressWarnings({ "rawtypes", "unchecked" })
 	@Override
	public Collection<?> getChildren(Object object) {
 		if (children == null) {
 			children = new ArrayList();
 
 			ReqIFContent reqifContent = (ReqIFContent) object;
 			children.add(0, new VirtualSpecificationsItemProvider(
 					adapterFactory, reqifContent));
 			children.add(1, new VirtualSpecTypeItemProvider(adapterFactory,
 					reqifContent));
 			children.add(2, new VirtualDatatypeDefinitionItemProvider(
 					adapterFactory, reqifContent));
 			children.add(3, new VirtualSpecObjectItemProvider(adapterFactory,
 					reqifContent));
 			children.add(4, new VirtualSpecRelationsItemProvider(
 					adapterFactory, reqifContent));
 			children.add(5, new VirtualSpecRelationGroupItemProvider(
 					adapterFactory, reqifContent));
 		}
 		return children;
 	}
 
 	public Object getVirtualSpecifications(Object object) {
 		// Call to getChildren to ensure children is not null
 		// getChildren(object);
		return children.get(0);
 	}
 
 	public Object getVirtualSpecTypes(Object object) {
 		// Call to getChildren to ensure children is not null
 		// getChildren(object);
		return children.get(1);
 	}
 
 	public Object getVirtualDataTypes(Object object) {
 		// Call to getChildren to ensure children is not null
 		// getChildren(object);
		return children.get(2);
 	}
 
 	public Object getVirtualSpecObjects(Object object) {
 		// Call to getChildren to ensure children is not null
 		// getChildren(object);
		return children.get(3);
 	}
 
 	public Object getVirtualSpecRelations(Object object) {
 		// Call to getChildren to ensure children is not null
 		// getChildren(object);
		return children.get(4);
 	}
 
 	public Object getVirtualSpecRelationGroups(Object object) {
 		// Call to getChildren to ensure children is not null
 		// getChildren(object);
		return children.get(5);
 	}
 
 	@Override
 	protected Command createRemoveCommand(EditingDomain domain, EObject owner,
 			EStructuralFeature feature, Collection<?> collection) {
 		return createWrappedCommand(
 				super.createRemoveCommand(domain, owner, feature, collection),
 				owner, feature);
 	}
 
 	/**
 	 * We extended the command in order to append additional commands for
 	 * setting the lastChanged attribute of the to be added object.
 	 */
 	@Override
 	protected Command createAddCommand(EditingDomain domain, EObject owner,
 			EStructuralFeature feature, Collection<?> collection, int index) {
 		List<Command> commandList = new ArrayList<Command>();
 		commandList.add(super.createAddCommand(domain, owner, feature,
 				collection, index));
 		GregorianCalendar lastChange = ReqIF10Util.getReqIFLastChange();
 		for (Object c : collection) {
 			if (c instanceof Identifiable) {
 				SetCommand sc = new SetCommand(domain, (Identifiable) c,
 						ReqIF10Package.eINSTANCE.getIdentifiable_LastChange(),
 						lastChange);
 				commandList.add(sc);
 			}
 		}
 		CompoundCommand cc = new CompoundCommand(commandList);
 		return createWrappedCommand(cc, owner, feature);
 	}
 	
 	protected Command createWrappedCommand(Command command,
 			final EObject owner, final EStructuralFeature feature) {
 		if (feature == ReqIF10Package.Literals.REQ_IF_CONTENT__SPECIFICATIONS) {
 			return new CommandWrapper(command) {
 				@Override
 				public Collection<?> getAffectedObjects() {
 					Collection<?> affected = super.getAffectedObjects();
 					if (affected.contains(owner)) {
 						affected = Collections
 								.singleton(getVirtualSpecifications(owner));
 					}
 					return affected;
 				}
 			};
 		} else if (feature == ReqIF10Package.Literals.REQ_IF_CONTENT__SPEC_TYPES) {
 			return new CommandWrapper(command) {
 				@Override
 				public Collection<?> getAffectedObjects() {
 					Collection<?> affected = super.getAffectedObjects();
 					if (affected.contains(owner)) {
 						affected = Collections
 								.singleton(getVirtualSpecTypes(owner));
 					}
 					return affected;
 				}
 			};
 		} else if (feature == ReqIF10Package.Literals.REQ_IF_CONTENT__DATATYPES) {
 			return new CommandWrapper(command) {
 				@Override
 				public Collection<?> getAffectedObjects() {
 					Collection<?> affected = super.getAffectedObjects();
 					if (affected.contains(owner)) {
 						affected = Collections
 								.singleton(getVirtualDataTypes(owner));
 					}
 					return affected;
 				}
 			};
 		} else if (feature == ReqIF10Package.Literals.REQ_IF_CONTENT__SPEC_OBJECTS) {
 			return new CommandWrapper(command) {
 				@Override
 				public Collection<?> getAffectedObjects() {
 					Collection<?> affected = super.getAffectedObjects();
 					if (affected.contains(owner)) {
 						affected = Collections
 								.singleton(getVirtualSpecObjects(owner));
 					}
 					return affected;
 				}
 			};
 		} else if (feature == ReqIF10Package.Literals.REQ_IF_CONTENT__SPEC_RELATIONS) {
 			return new CommandWrapper(command) {
 				@Override
 				public Collection<?> getAffectedObjects() {
 					Collection<?> affected = super.getAffectedObjects();
 					if (affected.contains(owner)) {
 						affected = Collections
 								.singleton(getVirtualSpecRelations(owner));
 					}
 					return affected;
 				}
 			};
 		} else if (feature == ReqIF10Package.Literals.REQ_IF_CONTENT__SPEC_RELATION_GROUPS) {
 			return new CommandWrapper(command) {
 				@Override
 				public Collection<?> getAffectedObjects() {
 					Collection<?> affected = super.getAffectedObjects();
 					if (affected.contains(owner)) {
 						affected = Collections
 								.singleton(getVirtualSpecRelationGroups(owner));
 					}
 					return affected;
 				}
 			};
 		}
 		return command;
 	}
 
 	@Override
 	public void dispose() {
 		super.dispose();
 		if (children != null) {
 			((IDisposable) children.get(0)).dispose();
 			((IDisposable) children.get(1)).dispose();
 			((IDisposable) children.get(2)).dispose();
 			((IDisposable) children.get(3)).dispose();
 			((IDisposable) children.get(4)).dispose();
 			((IDisposable) children.get(5)).dispose();
 		}
 	}
 }
