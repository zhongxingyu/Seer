 /*******************************************************************************
  * Copyright (c) 2011 Formal Mind GmbH and University of Dusseldorf.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  *     Michael Jastram - initial API and implementation
  ******************************************************************************/
 
 package org.eclipse.rmf.pror.reqif10.provider;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.List;
 
 import org.eclipse.emf.common.command.Command;
 import org.eclipse.emf.common.command.CompoundCommand;
 import org.eclipse.emf.common.notify.AdapterFactory;
 import org.eclipse.emf.common.notify.Notification;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.ecore.EStructuralFeature;
 import org.eclipse.emf.edit.command.AddCommand;
 import org.eclipse.emf.edit.command.SetCommand;
 import org.eclipse.emf.edit.domain.EditingDomain;
 import org.eclipse.emf.edit.provider.IEditingDomainItemProvider;
 import org.eclipse.emf.edit.provider.IItemLabelProvider;
 import org.eclipse.emf.edit.provider.IItemPropertyDescriptor;
 import org.eclipse.emf.edit.provider.IItemPropertySource;
 import org.eclipse.emf.edit.provider.IStructuredItemContentProvider;
 import org.eclipse.emf.edit.provider.ITreeItemContentProvider;
 import org.eclipse.emf.edit.provider.ItemPropertyDescriptorDecorator;
 import org.eclipse.emf.edit.provider.ViewerNotification;
 import org.eclipse.rmf.pror.reqif10.util.ConfigurationUtil;
 import org.eclipse.rmf.pror.reqif10.util.ProrUtil;
 import org.eclipse.rmf.reqif10.AttributeDefinition;
 import org.eclipse.rmf.reqif10.ReqIF10Package;
 import org.eclipse.rmf.reqif10.SpecElementWithAttributes;
 import org.eclipse.rmf.reqif10.SpecObject;
 import org.eclipse.rmf.reqif10.SpecType;
 
 /**
  * This is the item provider adapter for a
  * {@link org.eclipse.rmf.reqif10.SpecElementWithAttributes} object.
  * <!-- begin-user-doc -->
  * Made class abstract, as we want to make common behavior available
  * through concrete implementations.
  * <!-- end-user-doc -->
  * 
  * @generated NOT
  */
 public abstract class SpecElementWithAttributesItemProvider extends
 		IdentifiableItemProvider implements IEditingDomainItemProvider,
 		IStructuredItemContentProvider, ITreeItemContentProvider,
 		IItemLabelProvider, IItemPropertySource {
 	/**
 	 * This constructs an instance from a factory and a notifier. <!--
 	 * begin-user-doc --> <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	public SpecElementWithAttributesItemProvider(AdapterFactory adapterFactory) {
 		super(adapterFactory);
 	}
 
 	/**
 	 * This returns the property descriptors for the adapted class. <!--
 	 * begin-user-doc --> Adding the properties from the {@link SpecType} (the
 	 * properties show the SpecType's attributes).
 	 * <p>
 	 * 
 	 * NOTE: In order for this to work, the properties must not be cached. We
 	 * achieve this by setting itemPropertyDescriptors to null in the subclasses
 	 * in {@link #getPropertyDescriptors(Object)}
 	 * <p>
 	 * <!-- end-user-doc -->
 	 * 
 	 * @generated NOT
 	 */
 	@Override
 	public List<IItemPropertyDescriptor> getPropertyDescriptors(Object object) {
 		itemPropertyDescriptors = null;
 		super.getPropertyDescriptors(object);
 		ProrUtil.addAttributePropertyDescriptor(
 				(SpecElementWithAttributes) object, this,
 				itemPropertyDescriptors);
 		return itemPropertyDescriptors;
 	}
 
 	/**
 	 * This method does the same as {@link #getPropertyDescriptors(Object)}, but
 	 * it rewrites the categories for the standard properties (not the one from
 	 * the {@link SpecType}s) with the given String. This allows elements like
 	 * {@link SpecObject} to present properties like "Identifiable" in the
 	 * Category "Spec Object", instead of "Identifiable".
 	 */
 	public List<IItemPropertyDescriptor> getPropertyDescriptors(Object object, final String categoryForStandardProps) {
 		itemPropertyDescriptors = null;
 		super.getPropertyDescriptors(object);
 
 		// Rewrite the category for the properties we collected so far
 		ArrayList<IItemPropertyDescriptor> newDescriptors = new ArrayList<IItemPropertyDescriptor>();
 		for (IItemPropertyDescriptor descriptor : itemPropertyDescriptors) {
 			newDescriptors.add(new ItemPropertyDescriptorDecorator(object, descriptor) {
 				public String getCategory(Object thisObject) {
 					return categoryForStandardProps;
 				}
 			});
 		}
 		itemPropertyDescriptors = newDescriptors;
 
 		ProrUtil.addAttributePropertyDescriptor(
 				(SpecElementWithAttributes) object, this,
 				itemPropertyDescriptors);
 		return itemPropertyDescriptors;
 	}
 
 	/**
 	 * This returns the label text for the adapted class.
 	 * <!-- begin-user-doc -->
 	 * TODO: Not yet the right label
 	 * <!-- end-user-doc -->
 	 * 
 	 * @generated NOT
 	 */
 	@Override
 	public String getText(Object object) {
 		SpecElementWithAttributes specElement = (SpecElementWithAttributes) object;
 		
 		return ConfigurationUtil.getSpecElementLabel(specElement);
 	}
 
 	/**
 	 * This handles model notifications by calling {@link #updateChildren} to update any cached
 	 * children and by creating a viewer notification, which it passes to {@link #fireNotifyChanged}.
 	 * <!-- begin-user-doc --> <!--
 	 * end-user-doc -->
 	 * @generated
 	 */
 	@Override
 	public void notifyChanged(Notification notification) {
 		updateChildren(notification);
 
 		switch (notification.getFeatureID(SpecElementWithAttributes.class)) {
 			case ReqIF10Package.SPEC_ELEMENT_WITH_ATTRIBUTES__VALUES:
 				fireNotifyChanged(new ViewerNotification(notification, notification.getNotifier(), false, true));
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
 	protected void collectNewChildDescriptors(
 			Collection<Object> newChildDescriptors, Object object) {
 		super.collectNewChildDescriptors(newChildDescriptors, object);
 	}
 
 	/**
 	 * We have to intercept {@link SetCommand}s that set the {@link SpecType},
 	 * as we potentially have to update all specElements that use the specType.
 	 */
 	@Override
 	protected Command createSetCommand(EditingDomain domain, EObject owner,
 			EStructuralFeature feature, Object value, int index) {

 		if (feature == getSpecTypeFeature()) {
 			CompoundCommand cmd = new CompoundCommand();

 			Collection<AttributeDefinition> specAttributes;

 			if (value instanceof SpecType) {
 				specAttributes = ((SpecType) value).getSpecAttributes();
 			} else {
 				specAttributes = Collections.emptyList();
 			}

			CompoundCommand createValueAdjustCommand = ProrUtil
					.createValueAdjustCommand(domain,
							(SpecElementWithAttributes) owner, specAttributes);

			// We need to check if the adjust command is executable,
			// since the next command (set command) would not be executed
			// whenever it is disabled
			if (createValueAdjustCommand.canExecute())
				cmd.append(createValueAdjustCommand);

 			cmd.append(super.createSetCommand(domain, owner, feature, value,
 					index));

 			return cmd;
 		}
 		return super.createSetCommand(domain, owner, feature, value, index);
 	}
 
 
 	/**
 	 * We have to intercept {@link AddCommand}s that add the {@link SpecType}.
 	 * This happens when we add typed elements. This may require the values to
 	 * be updated as well.
 	 */
 	@Override
 	protected Command createAddCommand(EditingDomain domain, EObject owner,
 			EStructuralFeature feature, Collection<?> collection, int index) {
 		if (collection != null && collection.size() == 1) {
 			Object value = collection.iterator().next();
 			if (value instanceof SpecType) {
 				SpecType specType = (SpecType) value;
 				CompoundCommand cmd = new CompoundCommand();
 
 				Collection<AttributeDefinition> specAttributes = specType.getSpecAttributes();
 
 				CompoundCommand adjustValuesCommand = ProrUtil.createValueAdjustCommand(domain,
 						(SpecElementWithAttributes) owner, specAttributes);
 				if (!adjustValuesCommand.isEmpty()) {
 					cmd.append(adjustValuesCommand);
 				}
 				cmd.append(super.createSetCommand(domain, owner, feature,
 						specType, index));
 				return cmd;
 			}
 		}
 		return super
 				.createAddCommand(domain, owner, feature, collection, index);
 	}
 	/**
 	 * Simply returns the appropriate feature from {@link ReqIF10Package.Literals}.
 	 */
 	protected abstract EStructuralFeature getSpecTypeFeature();
 
 }
