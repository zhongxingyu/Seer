 /*******************************************************************************
  * Copyright (c) 2011 Formal Mind GmbH and University of Dusseldorf.
  * 
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  *     Michael Jastram - initial API and implementation
  ******************************************************************************/
 package org.eclipse.rmf.pror.reqif10.util;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 import org.eclipse.emf.common.command.Command;
 import org.eclipse.emf.common.command.CommandWrapper;
 import org.eclipse.emf.common.command.CompoundCommand;
 import org.eclipse.emf.common.command.UnexecutableCommand;
 import org.eclipse.emf.common.notify.AdapterFactory;
 import org.eclipse.emf.common.notify.Notification;
 import org.eclipse.emf.common.util.EList;
 import org.eclipse.emf.common.util.TreeIterator;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.ecore.EReference;
 import org.eclipse.emf.ecore.EStructuralFeature;
 import org.eclipse.emf.ecore.impl.ENotificationImpl;
 import org.eclipse.emf.ecore.impl.EObjectImpl;
 import org.eclipse.emf.ecore.util.EcoreUtil;
 import org.eclipse.emf.edit.EMFEditPlugin;
 import org.eclipse.emf.edit.command.AddCommand;
 import org.eclipse.emf.edit.command.CommandParameter;
 import org.eclipse.emf.edit.command.RemoveCommand;
 import org.eclipse.emf.edit.command.SetCommand;
 import org.eclipse.emf.edit.domain.EditingDomain;
 import org.eclipse.emf.edit.provider.ComposedImage;
 import org.eclipse.emf.edit.provider.IItemLabelProvider;
 import org.eclipse.emf.edit.provider.IItemPropertyDescriptor;
 import org.eclipse.emf.edit.provider.ItemPropertyDescriptorDecorator;
 import org.eclipse.emf.edit.provider.ItemProviderAdapter;
 import org.eclipse.rmf.pror.reqif10.edit.presentation.service.PresentationEditManager;
 import org.eclipse.rmf.pror.reqif10.edit.presentation.service.PresentationEditService;
 import org.eclipse.rmf.pror.reqif10.provider.SpecElementWithAttributesItemProvider;
 import org.eclipse.rmf.reqif10.AttributeDefinition;
 import org.eclipse.rmf.reqif10.AttributeDefinitionBoolean;
 import org.eclipse.rmf.reqif10.AttributeDefinitionDate;
 import org.eclipse.rmf.reqif10.AttributeDefinitionEnumeration;
 import org.eclipse.rmf.reqif10.AttributeDefinitionInteger;
 import org.eclipse.rmf.reqif10.AttributeDefinitionReal;
 import org.eclipse.rmf.reqif10.AttributeDefinitionString;
 import org.eclipse.rmf.reqif10.AttributeDefinitionXhtml;
 import org.eclipse.rmf.reqif10.AttributeValue;
 import org.eclipse.rmf.reqif10.AttributeValueBoolean;
 import org.eclipse.rmf.reqif10.AttributeValueDate;
 import org.eclipse.rmf.reqif10.AttributeValueEnumeration;
 import org.eclipse.rmf.reqif10.AttributeValueInteger;
 import org.eclipse.rmf.reqif10.AttributeValueReal;
 import org.eclipse.rmf.reqif10.AttributeValueString;
 import org.eclipse.rmf.reqif10.AttributeValueXhtml;
 import org.eclipse.rmf.reqif10.DatatypeDefinition;
 import org.eclipse.rmf.reqif10.ReqIf;
 import org.eclipse.rmf.reqif10.ReqIfContent;
 import org.eclipse.rmf.reqif10.Reqif10Factory;
 import org.eclipse.rmf.reqif10.Reqif10Package;
 import org.eclipse.rmf.reqif10.SpecElementWithAttributes;
 import org.eclipse.rmf.reqif10.SpecHierarchy;
 import org.eclipse.rmf.reqif10.SpecObject;
 import org.eclipse.rmf.reqif10.SpecRelation;
 import org.eclipse.rmf.reqif10.SpecType;
 import org.eclipse.rmf.reqif10.Specification;
 import org.eclipse.rmf.reqif10.util.Reqif10Switch;
 import org.eclipse.rmf.reqif10.util.Reqif10Util;
 
 /**
  * A Class full of tools for PorR-Programming. Note that you find more tools in
  * {@link ReqifUtil}, which are independent from ProR.
  * 
  * @author jastram
  * 
  */
 public final class ProrUtil {
 
 	/**
 	 * This class is not designed to be instantiated.
 	 */
 	private ProrUtil() {
 		throw new InstantiationError(
 				"This class is not designed to be instantiated.");
 	}
 
 	/**
 	 * Creates Properties for the attributes of the
 	 * {@link SpecElementWithAttributes}. This essentially allows us to handle
 	 * the Attributes from the associated {@link SpecType} as properties. This
 	 * applies to four types of Objects (TODO not all are implemented yet):
 	 * {@link SpecObject}, {@link SpecRelation}, {@link Specification} and
 	 * SpecGroup.
 	 * <p>
 	 * 
 	 * TODO We assume that the AttributeValues have been created. This is a
 	 * valid assumption within ProR, as setting the {@link SpecType}s creates
 	 * the {@link AttributeValue}s. The actual values may still be null. But
 	 * this assumption may not hold for externally generated ReqIF files.
 	 * 
 	 * @param object
 	 */
 	public static void addAttributePropertyDescriptor(
 			final SpecElementWithAttributes specElement,
 			ItemProviderAdapter provider,
 			List<IItemPropertyDescriptor> itemPropertyDescriptors) {
 
 		for (AttributeValue value : specElement.getValues()) {
 			ItemProviderAdapter valueProvider = getItemProvider(
 					provider.getAdapterFactory(), value);
 
 			IItemPropertyDescriptor descriptor = valueProvider
 					.getPropertyDescriptor(value,
 							Reqif10Util.getTheValueFeature(value));
 			AttributeDefinition definition = Reqif10Util
 					.getAttributeDefinition(value);
 
 			if (definition == null)
 				continue;
 
 			final String label = definition.getLongName() != null ? definition
 					.getLongName() : "UNNAMED (" + definition.getIdentifier()
 					+ ")";
 			itemPropertyDescriptors
 					.add(buildAttributeValueItemPropertyDescriptor(specElement,
 							value, descriptor, label));
 		}
 	}
 
 	private static ItemPropertyDescriptorDecorator buildAttributeValueItemPropertyDescriptor(
 			final SpecElementWithAttributes specElement, AttributeValue value,
 			IItemPropertyDescriptor descriptor, final String label) {
 		if (label == null) {
 			throw new NullPointerException("Label must not be null");
 		}
 		return new ItemPropertyDescriptorDecorator(value, descriptor) {
 			@Override
 			public String getCategory(Object thisObject) {
 				SpecType specType = Reqif10Util.getSpecType(specElement);
 				if (specType != null) {
 					if (specType.getLongName() == null) {
 						return "<UNNAMED TYPE>";
 					} else {
 						return specType.getLongName();
 					}
 				}
 				return "<NO CATEGORY>";
 			}
 
 			@Override
 			public String getDisplayName(Object thisObject) {
 				return label;
 			}
 
 			@Override
 			public String getId(Object thisObject) {
 				return label;
 			}
 		};
 	}
 
 	/**
 	 * Sets the value of the given {@link AttributeValue}. This helper method
 	 * exists to work around the lack of inheritance in the
 	 * {@link AttributeValue} setValue() infrastructure.
 	 */
 	public static void setTheValue(AttributeValue av, Object value,
 			EditingDomain ed) {
 		EStructuralFeature feature = Reqif10Util.getTheValueFeature(av);
 		Command cmd = SetCommand.create(ed, av, feature, value);
 		ed.getCommandStack().execute(cmd);
 	}
 
 	/**
 	 * Sets the value of the given {@link AttributeValue}. This helper method
 	 * exists to work around the lack of inheritance in the
 	 * {@link AttributeValue} setValue() infrastructure. In addition, it takes a
 	 * {@link SpecHierarchy} as an argument that is being used as the affected
 	 * object.
 	 */
 	public static void setTheValue(final AttributeValue av, Object value,
 			final Object affectedObject, EditingDomain ed) {
 		EStructuralFeature feature = Reqif10Util.getTheValueFeature(av);
 		Command cmd = SetCommand.create(ed, av, feature, value);
 
 		Command cmd2 = new CommandWrapper(cmd) {
 			@SuppressWarnings({ "unchecked", "rawtypes" })
 			@Override
 			public Collection<?> getAffectedObjects() {
 				List list = new ArrayList();
 				list.add(affectedObject);
 				return list;
 			}
 		};
 
 		ed.getCommandStack().execute(cmd2);
 	}
 
 	/**
 	 * Sets the value on the given element, provided the value exists.
 	 * 
 	 * @param specObject
 	 * @param definition
 	 * @param value
 	 * 
 	 * @return true if the value was set, otherwise false.
 	 */
 	public static boolean setTheValue(SpecObject specObject,
 			DatatypeDefinition definition, Object value, EditingDomain ed) {
 		EList<AttributeValue> list = specObject.getValues();
 		for (AttributeValue av : list) {
 			if (definition.equals(Reqif10Util.getDatatypeDefinition(av))) {
				ProrUtil.setTheValue(av, specObject, value, ed);
 				return true;
 			}
 		}
 		return false;
 	}
 
 	/**
 	 * Tries to retrieve the ItemProvider for the given object, using the
 	 * {@link AdapterFactory}. May return null.
 	 * 
 	 * @return The AdpaterFactory for object or null if none found.
 	 */
 	public static ItemProviderAdapter getItemProvider(
 			AdapterFactory adapterFactory, Object object) {
 		// Workaround - we simply try to retrieve an IItemLabelProvider.
 		return (ItemProviderAdapter) adapterFactory.adapt(object,
 				IItemLabelProvider.class);
 	}
 
 	public static void visitAllSpecElementsWithAttributes(ReqIf reqif,
 			Reqif10Switch<?> visitor) {
 		for (TreeIterator<Object> i = EcoreUtil.getAllContents(reqif, false); i
 				.hasNext();) {
 			Object obj = i.next();
 			if (obj instanceof SpecElementWithAttributes) {
 				visitor.doSwitch((EObject) obj);
 			}
 		}
 	}
 
 	/**
 	 * Collects NewChildDescriptors for the creation of new Elements for
 	 * SpecElements that are already typed. These should be hooked into the
 	 * various methods of the ItemProviders for SpecObject, SpecHierarchy,
 	 * Specification and SpecRelation.
 	 * <p>
 	 * 
 	 * We provide the parent as the owner. This is either {@link ReqIfContent},
 	 * but could also be a {@link Specification} or {@link SpecHierarchy}.
 	 * <p>
 	 * 
 	 * As the feature we provide the actual feature for the data object.
 	 * <p>
 	 * 
 	 * The value is a class derived from {@link SpecType} (e.g. SpecObjectType).
 	 * <p>
 	 * 
 	 * These parameters will be handed to
 	 * {@link #collectNewChildDescriptorsForTypeCreators(Collection, Object, EStructuralFeature, Class)}
 	 * and eventually processed by
 	 * {@link SpecElementWithAttributesItemProvider#createAddCommand()}.
 	 * 
 	 * @param newChildDescriptors
 	 * @param object
 	 */
 	public static void collectNewChildDescriptorsForTypeCreators(
 			Collection<Object> newChildDescriptors, Object object,
 			EStructuralFeature feature, Class<?> specTypeClass) {
 
 		// Add a Descriptor for each SpecType
 		EList<SpecType> specTypes = Reqif10Util.getReqIf(object)
 				.getCoreContent().getSpecTypes();
 
 		for (final SpecType specType : specTypes) {
 			if (specTypeClass.isAssignableFrom(specType.getClass())) {
 				newChildDescriptors.add(new CommandParameter(object, feature,
 						specType));
 			}
 		}
 	}
 
 	/**
 	 * Creates a command for adding a typed SpecElement. This should work no
 	 * matter what the type is. A correct icon is provided. We return a
 	 * {@link CompoundCommand}, so additional commands can be appended (e.g. for
 	 * SpecHierarchies). The result index can be adjusted.
 	 * <p>
 	 * 
 	 * @param parent
 	 *            The parent of newSpecLement
 	 * @param childFeature
 	 *            The Feature for adding newSpecElement to parent
 	 * @param newSpecElement
 	 *            an instance of {@link SpecElementWithAttributes} that will be
 	 *            typed.
 	 * @param typeFeature
 	 *            the feature for adding specType to newSpecElement
 	 * @param specType
 	 *            an instance of the specType to be used for newSpecElement
 	 * @param index
 	 *            The index for the position of newSpecElement under parent
 	 * @param resultIndex
 	 *            The index of the command to be used for affected Elements (the
 	 *            resulting CompoundCommand already contains 3 commands)
 	 * @param domain
 	 *            the EditingDomain
 	 * @param adapterFactory
 	 *            the AdapterFactory
 	 * @return
 	 */
 	public static CompoundCommand createAddTypedElementCommand(Object parent,
 			EReference childFeature, SpecElementWithAttributes newSpecElement,
 			EReference typeFeature, SpecType specType, int index,
 			int resultIndex, EditingDomain domain, AdapterFactory adapterFactory) {
 
 		ItemProviderAdapter newElementItemProvider = ProrUtil.getItemProvider(
 				adapterFactory, newSpecElement);
 		Object icon = newElementItemProvider.getImage(newSpecElement);
 
 		final CompoundCommand cmd = createCompoundCommandWithAddIcon(icon,
 				resultIndex);
 
 		cmd.append(AddCommand.create(domain, parent, childFeature,
 				newSpecElement, index));
 
 		HashSet<SpecType> typeCollection = new HashSet<SpecType>();
 		typeCollection.add((SpecType) specType);
 		CommandParameter typeParameter = new CommandParameter(newSpecElement,
 				typeFeature, typeCollection);
 
 		// TODO doesn't feel right
 		cmd.append(newElementItemProvider.createCommand(parent, domain,
 				AddCommand.class, typeParameter));
 		String name = newSpecElement.getClass().getSimpleName();
 		name = name.length() > 4 ? name.substring(0, name.length() - 4) : name;
 		cmd.setLabel(name + " (" + ((SpecType) specType).getLongName() + ")");
 
 		return cmd;
 	}
 
 	/**
 	 * Builds a CompoundCommand that has the given icon, with an overlay plus
 	 * (+) to indicate that the command executes an addition.
 	 * 
 	 * @param icon
 	 * @return
 	 */
 	public static CompoundCommandActionDelegate createCompoundCommandWithAddIcon(
 			final Object icon, int resultIndex) {
 		return new CompoundCommandActionDelegate(resultIndex) {
 			@Override
 			public Object getImage() {
 				List<Object> images = new ArrayList<Object>();
 				images.add(icon);
 				images.add(EMFEditPlugin.INSTANCE
 						.getImage("full/ovr16/CreateChild"));
 				return new ComposedImage(images) {
 					@Override
 					public List<Point> getDrawPoints(Size size) {
 						List<Point> result = super.getDrawPoints(size);
 						result.get(1).x = size.width - 7;
 						return result;
 					}
 				};
 			}
 		};
 	}
 
 	/**
 	 * @return the handle drag and drop command from presentation plugin or null
 	 *         if no plugin can handle the operation.
 	 */
 	public static Command getPresentationHandleDragAndDropCommand(
 			EditingDomain domain, Object owner, float location, int operations,
 			int operation, java.util.Collection<?> collection) {
 		// See whether a Presentation feels responsible.
 		Collection<PresentationEditService> services = PresentationEditManager
 				.getPresentationEditServiceMap().values();
 		for (PresentationEditService service : services) {
 			Command cmd = service.handleDragAndDrop(collection, owner, domain,
 					operation);
 			if (cmd != null) {
 				return cmd;
 			}
 		}
 		return null;
 	}
 
 	/**
 	 * This method creates the command for updating the {@link SpecType} of an
 	 * {@link SpecElementWithUserDefinedAttributes}. It does <b>not</b> update
 	 * the type itself; instead, it updates the values to match the type of the
 	 * given specElement.
 	 * <p>
 	 * 
 	 * Using this command from the ItemProviders allows a clean change of type
 	 * and values via the CommandStack.
 	 * <p>
 	 * 
 	 * WATCH OUT: This method may return a command that is empty, which in turn
 	 * isn't executable by default.
 	 * 
 	 * @return The Command that updates the Values
 	 */
 	public static CompoundCommand createValueAdjustCommand(
 			EditingDomain domain, SpecElementWithAttributes specElement,
 			Collection<AttributeDefinition> definitions) {
 		// First make sure all required values exist
 		HashSet<AttributeValue> existingObsoleteValues = new HashSet<AttributeValue>(
 				specElement.getValues());
 
 		// The list of types for the new values.
 		Set<AttributeDefinition> newDefs = new HashSet<AttributeDefinition>(
 				definitions);
 
 		// A CompoundCommand for adding and removing values
 		CompoundCommand cmd = new CompoundCommand(
 				"Updating Type (and associated Values)");
 
 		// Iterate over the required attributes...
 		outer: for (AttributeDefinition newDef : newDefs) {
 			// ... and check for each whether it already exists:
 			for (AttributeValue value : specElement.getValues()) {
 				AttributeDefinition def = Reqif10Util
 						.getAttributeDefinition(value);
 				if (def != null && def.equals(newDef)) {
 					// It does: Continue the outer loop
 					existingObsoleteValues.remove(value);
 					continue outer;
 				}
 			}
 
 			// The attribute is missing: Let's add it
 			AttributeValue value = createAttributeValue(newDef);
 			if (value != null) {
 				cmd.append(AddCommand
 						.create(domain,
 								specElement,
 								Reqif10Package.Literals.SPEC_ELEMENT_WITH_ATTRIBUTES__VALUES,
 								value));
 			}
 		}
 		// If there are any values left, we need to remove them
 		for (AttributeValue value : existingObsoleteValues) {
 			cmd.append(RemoveCommand
 					.create(domain,
 							specElement,
 							Reqif10Package.Literals.SPEC_ELEMENT_WITH_ATTRIBUTES__VALUES,
 							value));
 		}
 		return cmd;
 	}
 
 	/**
 	 * Returns an empty value of the correct type for the given
 	 * {@link AttributeDefinition} (Would be so much easier with inheritance).
 	 * Note that we do not use the command stack here.
 	 * <p>
 	 * TODO There must be a better way (reflection?)
 	 */
 	private static AttributeValue createAttributeValue(
 			AttributeDefinition attributeDefinition) {
 		if (attributeDefinition == null) {
 			return null;
 		} else if (attributeDefinition instanceof AttributeDefinitionBoolean) {
 			AttributeValueBoolean value = Reqif10Factory.eINSTANCE
 					.createAttributeValueBoolean();
 			value.setDefinition((AttributeDefinitionBoolean) attributeDefinition);
 			AttributeValueBoolean defaultValue = ((AttributeDefinitionBoolean) attributeDefinition)
 					.getDefaultValue();
 			if (defaultValue != null) {
 				value.setTheValue(defaultValue.getTheValue());
 			}
 			return value;
 		} else if (attributeDefinition instanceof AttributeDefinitionDate) {
 			AttributeValueDate value = Reqif10Factory.eINSTANCE
 					.createAttributeValueDate();
 			value.setDefinition((AttributeDefinitionDate) attributeDefinition);
 			AttributeValueDate defaultValue = ((AttributeDefinitionDate) attributeDefinition)
 					.getDefaultValue();
 			if (defaultValue != null) {
 				value.setTheValue(defaultValue.getTheValue());
 			}
 			return value;
 		} else if (attributeDefinition instanceof AttributeDefinitionInteger) {
 			AttributeValueInteger value = Reqif10Factory.eINSTANCE
 					.createAttributeValueInteger();
 			value.setDefinition((AttributeDefinitionInteger) attributeDefinition);
 			AttributeValueInteger defaultValue = ((AttributeDefinitionInteger) attributeDefinition)
 					.getDefaultValue();
 			if (defaultValue != null) {
 				value.setTheValue(defaultValue.getTheValue());
 			}
 			return value;
 		} else if (attributeDefinition instanceof AttributeDefinitionReal) {
 			AttributeValueReal value = Reqif10Factory.eINSTANCE
 					.createAttributeValueReal();
 			value.setDefinition((AttributeDefinitionReal) attributeDefinition);
 			AttributeValueReal defaultValue = ((AttributeDefinitionReal) attributeDefinition)
 					.getDefaultValue();
 			if (defaultValue != null) {
 				value.setTheValue(defaultValue.getTheValue());
 			}
 			return value;
 		} else if (attributeDefinition instanceof AttributeDefinitionString) {
 			AttributeValueString value = Reqif10Factory.eINSTANCE
 					.createAttributeValueString();
 			value.setDefinition((AttributeDefinitionString) attributeDefinition);
 
 			AttributeValueString defaultValue = ((AttributeDefinitionString) attributeDefinition)
 					.getDefaultValue();
 			if (defaultValue != null) {
 				value.setTheValue(defaultValue.getTheValue());
 			}
 			return value;
 		} else if (attributeDefinition instanceof AttributeDefinitionXhtml) {
 			AttributeValueXhtml value = Reqif10Factory.eINSTANCE
 					.createAttributeValueXhtml();
 			value.setDefinition((AttributeDefinitionXhtml) attributeDefinition);
 			AttributeValueXhtml defaultValue = ((AttributeDefinitionXhtml) attributeDefinition)
 					.getDefaultValue();
 			if (defaultValue != null) {
 				value.setTheValue(defaultValue.getTheValue());
 			}
 			return value;
 		} else if (attributeDefinition instanceof AttributeDefinitionEnumeration) {
 			AttributeValueEnumeration value = Reqif10Factory.eINSTANCE
 					.createAttributeValueEnumeration();
 			value.setDefinition((AttributeDefinitionEnumeration) attributeDefinition);
 			AttributeValueEnumeration defaultValue = ((AttributeDefinitionEnumeration) attributeDefinition)
 					.getDefaultValue();
 			if (defaultValue != null) {
 				value.getValues().addAll(defaultValue.getValues());
 			}
 			return value;
 		} else {
 			throw new IllegalArgumentException("Type not supported: "
 					+ attributeDefinition);
 		}
 	}
 
 	/**
 	 * Builds a command that creates new {@link SpecRelation}s between the given
 	 * sources and target. Both, source and target can be {@link SpecObject}s
 	 * and {@link SpecHierarchy}s.
 	 */
 	public static Command createCreateSpecRelationsCommand(
 			EditingDomain domain, Collection<?> sources, Object target) {
 
 		// Find the target SpecObject
 		SpecObject targetObject = null;
 		if (target instanceof SpecObject) {
 			targetObject = (SpecObject) target;
 		} else if (target instanceof SpecHierarchy) {
 			targetObject = ((SpecHierarchy) target).getObject();
 		}
 		if (targetObject == null) {
 			return UnexecutableCommand.INSTANCE;
 		}
 
 		ReqIfContent content = Reqif10Util.getReqIf(targetObject)
 				.getCoreContent();
 		ArrayList<SpecRelation> relations = new ArrayList<SpecRelation>();
 
 		for (Object source : sources) {
 			if (source instanceof SpecHierarchy) {
 				source = ((SpecHierarchy) source).getObject();
 			}
 			if (source instanceof SpecObject) {
 				SpecObject sourceObject = (SpecObject) source;
 				SpecRelation relation = Reqif10Factory.eINSTANCE
 						.createSpecRelation();
 				relation.setSource(sourceObject);
 				relation.setTarget(targetObject);
 				relations.add(relation);
 			}
 		}
 		return AddCommand.create(domain, content,
 				Reqif10Package.Literals.REQ_IF_CONTENT__SPEC_RELATIONS,
 				relations);
 	}
 
 	/**
 	 * This class reflectively looks for the given postfix and removes it from
 	 * the classname of the given object. Should the result contain camel case,
 	 * then spaces will be inserted.
 	 * <p>
 	 * 
 	 * If obj is itself a {@link Class}, its simple name is used directly.
 	 * 
 	 * If the postfix does not match, the simple class name is returned.
 	 * <p>
 	 * 
 	 * If obj is null, the empty string is returned.
 	 * <p>
 	 * 
 	 * The idea is that in some places, it is convenient to extract information
 	 * directly from the CamelCased classname, e.g. SpecRelationTypeItemProvider
 	 * => "Spec Relation Type".
 	 */
 	public static String substractPrefixPostfix(Object obj, String prefix,
 			String suffix) {
 		if (obj == null) {
 			return "";
 		}
 
 		String className = obj instanceof Class ? ((Class<?>) obj)
 				.getSimpleName() : obj.getClass().getSimpleName();
 		if (!className.startsWith(suffix) && !className.endsWith(suffix)) {
 			return className;
 		}
 		String name = className.substring(prefix.length(), className.length()
 				- suffix.length());
 		StringBuilder sb = new StringBuilder();
 		for (int i = 0; i < name.length(); i++) {
 			char c = name.charAt(i);
 			if (i != 0 && Character.isUpperCase(c)) {
 				sb.append(' ');
 			}
 			sb.append(c);
 		}
 		return sb.toString();
 	}
 
 	/**
 	 * This method must be called by all setType() calls of the subclasses, to
 	 * set the values that correspond to the attributes of the type.
 	 * <p>
 	 * 
 	 * @param valueFeature
 	 *            the correct value from {@link ReqIFPackage}, e.g.
 	 *            {@link ReqIFPackage#SPEC_OBJECT__VALUES}.
 	 */
 	public static void updateValuesForCurrentType(SpecObject specObject) {
 
 		// First make sure all required values exist
 		HashSet<AttributeValue> existingRequiredValues = new HashSet<AttributeValue>(
 				specObject.getValues());
 
 		if (specObject.getType() != null) {
 			// Iterate over the required attributes...
 			outer: for (AttributeDefinition attrDefFromNewType : specObject
 					.getType().getSpecAttributes()) {
 				// ... and check for each whether it already exists:
 				for (AttributeValue value : specObject.getValues()) {
 					AttributeDefinition definition = Reqif10Util
 							.getAttributeDefinition(value);
 					if (definition != null
 							&& definition.equals(attrDefFromNewType)) {
 						// It does: Continue the outer loop
 						existingRequiredValues.remove(value);
 						continue outer;
 					}
 				}
 
 				// The attribute is missing: Let's add it; but we can only add
 				// it, if a type is set.
 				AttributeValue value = createAttributeValue(attrDefFromNewType);
 
 				if (value != null) {
 					specObject.getValues().add(value);
 					if (((EObjectImpl) specObject).eNotificationRequired())
 						specObject.eNotify(new ENotificationImpl(
 								(EObjectImpl) specObject, Notification.ADD,
 								null, null, value));
 				}
 				// If there are any values left, we need to remove them
 				for (AttributeValue attributeValue : existingRequiredValues) {
 					specObject.getValues().remove(attributeValue);
 				}
 			}
 		} else {
 			// We don't do a thing: We leave the (now stale) Attributes. They
 			// will be removed if a new type is set.
 		}
 	}
 
 }
