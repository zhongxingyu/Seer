 /*******************************************************************************
  * Copyright (c) 2011 itemis GmbH.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  *     Nirmal Sasidharan - initial API and implementation
  ******************************************************************************/
 package org.eclipse.rmf.reqif10.common.util;
 
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Method;
 import java.util.Collection;
 import java.util.List;
 import java.util.Set;
 import java.util.UUID;
 
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.ecore.EStructuralFeature;
 import org.eclipse.emf.ecore.resource.impl.ResourceImpl;
 import org.eclipse.rmf.reqif10.AttributeDefinition;
 import org.eclipse.rmf.reqif10.AttributeDefinitionBoolean;
 import org.eclipse.rmf.reqif10.AttributeDefinitionDate;
 import org.eclipse.rmf.reqif10.AttributeDefinitionEnumeration;
 import org.eclipse.rmf.reqif10.AttributeDefinitionInteger;
 import org.eclipse.rmf.reqif10.AttributeDefinitionReal;
 import org.eclipse.rmf.reqif10.AttributeDefinitionString;
 import org.eclipse.rmf.reqif10.AttributeDefinitionXHTML;
 import org.eclipse.rmf.reqif10.AttributeValue;
 import org.eclipse.rmf.reqif10.AttributeValueBoolean;
 import org.eclipse.rmf.reqif10.AttributeValueDate;
 import org.eclipse.rmf.reqif10.AttributeValueEnumeration;
 import org.eclipse.rmf.reqif10.AttributeValueInteger;
 import org.eclipse.rmf.reqif10.AttributeValueReal;
 import org.eclipse.rmf.reqif10.AttributeValueSimple;
 import org.eclipse.rmf.reqif10.AttributeValueString;
 import org.eclipse.rmf.reqif10.AttributeValueXHTML;
 import org.eclipse.rmf.reqif10.DatatypeDefinition;
 import org.eclipse.rmf.reqif10.EnumValue;
 import org.eclipse.rmf.reqif10.Identifiable;
 import org.eclipse.rmf.reqif10.ReqIF;
 import org.eclipse.rmf.reqif10.ReqIF10Factory;
 import org.eclipse.rmf.reqif10.ReqIF10Package;
 import org.eclipse.rmf.reqif10.SpecElementWithAttributes;
 import org.eclipse.rmf.reqif10.SpecType;
 
 /**
  * This class contains various static helper methods for working with ReqIf data objects. Amongst others, we solve a
  * number of issues that would be better served proper OOP (e.g. {@link #getTheValue(AttributeValue)}. However, we
  * decided not to touch the generated code. We decided to use reflection over long if-then-else blocks to
  * <p>
  * This class is not intended to be instantiated.
  * 
  * @author jastram
  */
 public class ReqIF10Util {
 
 	/**
 	 * This class is not designed to be instantiated.
 	 */
 	private ReqIF10Util() {
 		throw new InstantiationError("This class is not designed to be instantiated."); //$NON-NLS-1$
 	}
 
 	/**
 	 * Returns the root ReqIF object for the corresponding EObject or null if none exists. This method simply traverses
 	 * the object tree to the root - there may be more efficient ways for finding the root ReqIF.
 	 * 
 	 * @return the root {@link ReqIF} object or null if none found.
 	 */
 	public static ReqIF getReqIF(Object obj) {
 		// TODO: replace and test by EcoreUtil.getRootContainer();
 		if (!(obj instanceof EObject)) {
 			return null;
 		}
 		while (((EObject) obj).eContainer() != null) {
 			obj = ((EObject) obj).eContainer();
 		}
 		if (obj instanceof ReqIF) {
 			return (ReqIF) obj;
 		}
 		return null;
 	}
 
 	/**
 	 * Retrieves the Value from the given AttributeValue. We would prefer to have an abstract getTheValue() method on
 	 * AttributeValue, but EMF doesn't support this. Thus, each subclass of AttributeValue has its own getTheValue()
 	 * method. This convenience method returns the value.
 	 */
 	public static Object getTheValue(AttributeValue attributeValue) {
 		if (attributeValue instanceof AttributeValueBoolean) {
 			return ((AttributeValueBoolean) attributeValue).isTheValue();
 		} else if (attributeValue instanceof AttributeValueSimple || attributeValue instanceof AttributeValueXHTML) {
 			return reflectiveGet(attributeValue, "getTheValue"); //$NON-NLS-1$
 		} else if (attributeValue instanceof AttributeValueEnumeration) {
 			return reflectiveGet(attributeValue, "getValues"); //$NON-NLS-1$
 		} else {
 			throw new IllegalArgumentException("Can't get value from " + attributeValue); //$NON-NLS-1$
 		}
 	}
 
 	/**
 	 * Reflectively sets the value. The value must not be null, as it is used to infer the class.
 	 * 
 	 * @param attributeValue
 	 */
 	@SuppressWarnings("unchecked")
 	// for AttributeValueEnumeration
 	public static void setTheValue(AttributeValue attributeValue, Object value) {
 		if (attributeValue instanceof AttributeValueSimple || attributeValue instanceof AttributeValueXHTML) {
 			reflectiveSet(attributeValue, value, "setTheValue"); //$NON-NLS-1$
 		} else if (attributeValue instanceof AttributeValueEnumeration) {
 			((AttributeValueEnumeration) attributeValue).getValues().clear();
 			((AttributeValueEnumeration) attributeValue).getValues().addAll((Collection<? extends EnumValue>) value);
 		} else {
 			throw new IllegalArgumentException("Can't get value from " + attributeValue); //$NON-NLS-1$
 		}
 	}
 
 	/**
 	 * Retrieves the value for the given {@link AttributeValue} from the {@link SpecElementWithAttributes}.
 	 */
 	public static AttributeValue getAttributeValue(SpecElementWithAttributes specElement, AttributeDefinition attributeDefinition) {
 		for (AttributeValue value : specElement.getValues()) {
 			AttributeDefinition definition = (AttributeDefinition) reflectiveGet(value, "getDefinition"); //$NON-NLS-1$
 			if (attributeDefinition.equals(definition)) {
 				return value;
 			}
 		}
 		return null;
 	}
 
 	/**
 	 * Finds the {@link AttributeValue} for the given {@link SpecElementWithUserDefinedAttributes}. If it does not exist
 	 * yet, it is created (but not attached to the specElement. If an attributeDefinition with the label does not exist,
 	 * null is returned.
 	 * <p>
 	 * If a default value is available, it is set as well.
 	 */
 	public static AttributeValue getAttributeValueForLabel(SpecElementWithAttributes element, String label) {
 		if (label == null) {
 			return null;
 		}
 
 		// find the AttributeDefinition for the label
 		SpecType type = ReqIF10Util.getSpecType(element);
 		if (type == null) {
 			return null;
 		}
 
 		AttributeDefinition attrDef = null;
 		for (AttributeDefinition ad : type.getSpecAttributes()) {
 			if (label.equals(ad.getLongName())) {
 				attrDef = ad;
 				break;
 			}
 		}
 
 		if (attrDef == null) {
 			return null;
 		}
 
 		// Is there already a value with this AttributeDefinition?
 		for (AttributeValue value : element.getValues()) {
 			AttributeDefinition ad = getAttributeDefinition(value);
 			if (attrDef.equals(ad)) {
 				return value;
 			}
 		}
 
 		// No: Create a new AttributeDefinition
 		AttributeValue av = createAttributeValue(attrDef);
 		AttributeValue defaultValue = (AttributeValue) ReqIF10Util.reflectiveGet(attrDef, "getDefaultValue"); //$NON-NLS-1$
 		if (defaultValue != null) {
 			Object v = ReqIF10Util.getTheValue(defaultValue);
 			if (v != null) {
 				ReqIF10Util.setTheValue(av, v);
 			}
 		}
 		return av;
 	}
 
 	/**
 	 * Returns the AttributeDefinition for a given value (Would be so much easier with inheritance).
 	 */
 	public static AttributeDefinition getAttributeDefinition(AttributeValue value) {
 		return (AttributeDefinition) reflectiveGet(value, "getDefinition"); //$NON-NLS-1$
 	}
 
 	/**
 	 * Returns the {@link DatatypeDefinition} for the given {@link AttributeDefinition} (Would be so much easier with
 	 * inheritance).
 	 */
 	public static DatatypeDefinition getDatatypeDefinition(AttributeDefinition attributeDefinition) {
 		return (DatatypeDefinition) reflectiveGet(attributeDefinition, "getType"); //$NON-NLS-1$
 	}
 
 	/**
 	 * Returns the {@link DatatypeDefinition} for the given {@link AttributeValue}.
 	 * 
 	 * @return the corresponding {@link DatatypeDefinition} or null if it cannot be determined.
 	 */
 	public static DatatypeDefinition getDatatypeDefinition(AttributeValue value) {
 		if (value == null) {
 			return null;
 		}
 		AttributeDefinition ad = getAttributeDefinition(value);
 		if (ad == null) {
 			return null;
 		}
 		return getDatatypeDefinition(ad);
 	}
 
 	public static SpecType getSpecType(SpecElementWithAttributes specElement) {
 		return (SpecType) reflectiveGet(specElement, "getType"); //$NON-NLS-1$
 	}
 
 	public static SpecType getSpecType(AttributeDefinition ad) {
 		return (SpecType) ad.eContainer();
 	}
 
 	/**
 	 * Helper method that reflectively executes methods. The annoying thing with ReqIF is, that many classes share a
 	 * method (e.g. getType()) that returns the same supertype, but due to the model generation, there isn't a shared
 	 * method in the superclass. If there is a problem, the resulting {@link Exception} is wrapped into a
 	 * {@link RuntimeException}.
 	 * 
 	 * @param object
 	 * @param methodName
 	 * @return the result of the method call
 	 * @throws RuntimeException
 	 */
 	public static Object reflectiveGet(Object object, String methodName) {
 		try {
 			Method method = object.getClass().getMethod(methodName, (Class<?>[]) null);
 			return method.invoke(object, (Object[]) null);
 		} catch (SecurityException e) {
 			throw new RuntimeException(e);
 		} catch (NoSuchMethodException e) {
 			throw new RuntimeException(e);
 		} catch (IllegalArgumentException e) {
 			throw new RuntimeException(e);
 		} catch (IllegalAccessException e) {
 			throw new RuntimeException(e);
 		} catch (InvocationTargetException e) {
 			throw new RuntimeException(e);
 		}
 	}
 
 	public static void reflectiveSet(Object object, Object value, String methodName) {
 		try {
 			Class<?>[] args = new Class<?>[1];
 			if (value instanceof List) {
 				args[0] = List.class;
 			} else {
 				args[0] = value.getClass();
 			}
 			Method method = object.getClass().getMethod(methodName, args);
 			method.invoke(object, value);
 		} catch (SecurityException e) {
 			throw new RuntimeException(e);
 		} catch (NoSuchMethodException e) {
 			throw new RuntimeException(e);
 		} catch (IllegalArgumentException e) {
 			throw new RuntimeException(e);
 		} catch (IllegalAccessException e) {
 			throw new RuntimeException(e);
 		} catch (InvocationTargetException e) {
 			throw new RuntimeException(e);
 		}
 	}
 
 	/**
 	 * Returns the "the value" feature for the given attributeValue. For instance, for an {@link AttributeValueString}
 	 * it returns {@link Reqif10Package.Literals#ATTRIBUTE_VALUE_STRING__THE_VALUE}. The one exception is
 	 * {@link AttributeValueEnumeration}, where the feature name is "values", rather than "the value".
 	 * 
 	 * @throws IllegalArgumentException
 	 *             for unknown {@link AttributeValue}s.
 	 */
 	public static EStructuralFeature getTheValueFeature(AttributeValue attributeValue) {
 		if (attributeValue instanceof AttributeValueBoolean) {
 			return ReqIF10Package.Literals.ATTRIBUTE_VALUE_BOOLEAN__THE_VALUE;
 		} else if (attributeValue instanceof AttributeValueDate) {
 			return ReqIF10Package.Literals.ATTRIBUTE_VALUE_DATE__THE_VALUE;
 		} else if (attributeValue instanceof AttributeValueInteger) {
 			return ReqIF10Package.Literals.ATTRIBUTE_VALUE_INTEGER__THE_VALUE;
 		} else if (attributeValue instanceof AttributeValueReal) {
 			return ReqIF10Package.Literals.ATTRIBUTE_VALUE_REAL__THE_VALUE;
 		} else if (attributeValue instanceof AttributeValueString) {
 			return ReqIF10Package.Literals.ATTRIBUTE_VALUE_STRING__THE_VALUE;
 		} else if (attributeValue instanceof AttributeValueXHTML) {
 			return ReqIF10Package.Literals.ATTRIBUTE_VALUE_XHTML__THE_VALUE;
 		} else if (attributeValue instanceof AttributeValueEnumeration) {
 			return ReqIF10Package.Literals.ATTRIBUTE_VALUE_ENUMERATION__VALUES;
 		} else {
 			throw new IllegalArgumentException("Unknown AttributeValue: " + attributeValue); //$NON-NLS-1$
 		}
 	}
 
 	/**
 	 * Returns an empty value of the correct type for the given {@link AttributeDefinition} (Would be so much easier
 	 * with inheritance). Note that we do not use the command stack here.
 	 * <p>
 	 * TODO There must be a better way (reflection?)
 	 */
 	public static AttributeValue createAttributeValue(AttributeDefinition attributeDefinition) {
 		if (attributeDefinition == null) {
 			return null;
 		} else if (attributeDefinition instanceof AttributeDefinitionBoolean) {
 			AttributeValueBoolean value = ReqIF10Factory.eINSTANCE.createAttributeValueBoolean();
 			value.setDefinition((AttributeDefinitionBoolean) attributeDefinition);
 			AttributeValueBoolean defaultValue = ((AttributeDefinitionBoolean) attributeDefinition).getDefaultValue();
 			if (defaultValue != null) {
 				value.setTheValue(defaultValue.isTheValue());
 			}
 			return value;
 		} else if (attributeDefinition instanceof AttributeDefinitionDate) {
 			AttributeValueDate value = ReqIF10Factory.eINSTANCE.createAttributeValueDate();
 			value.setDefinition((AttributeDefinitionDate) attributeDefinition);
 			AttributeValueDate defaultValue = ((AttributeDefinitionDate) attributeDefinition).getDefaultValue();
 			if (defaultValue != null) {
 				value.setTheValue(defaultValue.getTheValue());
 			}
 			return value;
 		} else if (attributeDefinition instanceof AttributeDefinitionInteger) {
 			AttributeValueInteger value = ReqIF10Factory.eINSTANCE.createAttributeValueInteger();
 			value.setDefinition((AttributeDefinitionInteger) attributeDefinition);
 			AttributeValueInteger defaultValue = ((AttributeDefinitionInteger) attributeDefinition).getDefaultValue();
 			if (defaultValue != null) {
 				value.setTheValue(defaultValue.getTheValue());
 			}
 			return value;
 		} else if (attributeDefinition instanceof AttributeDefinitionReal) {
 			AttributeValueReal value = ReqIF10Factory.eINSTANCE.createAttributeValueReal();
 			value.setDefinition((AttributeDefinitionReal) attributeDefinition);
 			AttributeValueReal defaultValue = ((AttributeDefinitionReal) attributeDefinition).getDefaultValue();
 			if (defaultValue != null) {
 				value.setTheValue(defaultValue.getTheValue());
 			}
 			return value;
 		} else if (attributeDefinition instanceof AttributeDefinitionString) {
 			AttributeValueString value = ReqIF10Factory.eINSTANCE.createAttributeValueString();
 			value.setDefinition((AttributeDefinitionString) attributeDefinition);
 
 			AttributeValueString defaultValue = ((AttributeDefinitionString) attributeDefinition).getDefaultValue();
 			if (defaultValue != null) {
 				value.setTheValue(defaultValue.getTheValue());
 			}
 			return value;
 		} else if (attributeDefinition instanceof AttributeDefinitionXHTML) {
 			AttributeValueXHTML value = ReqIF10Factory.eINSTANCE.createAttributeValueXHTML();
 			value.setDefinition((AttributeDefinitionXHTML) attributeDefinition);
 			AttributeValueXHTML defaultValue = ((AttributeDefinitionXHTML) attributeDefinition).getDefaultValue();
 			if (defaultValue != null) {
 				value.setTheValue(defaultValue.getTheValue());
 			}
 			return value;
 		} else if (attributeDefinition instanceof AttributeDefinitionEnumeration) {
 			AttributeValueEnumeration value = ReqIF10Factory.eINSTANCE.createAttributeValueEnumeration();
 			value.setDefinition((AttributeDefinitionEnumeration) attributeDefinition);
 			AttributeValueEnumeration defaultValue = ((AttributeDefinitionEnumeration) attributeDefinition).getDefaultValue();
 			if (defaultValue != null) {
 				value.getValues().addAll(defaultValue.getValues());
 			}
 			return value;
 		} else {
 			throw new IllegalArgumentException("Type not supported: " + attributeDefinition); //$NON-NLS-1$
 		}
 	}
 
 	/**
 	 * Ensures that the {@link Identifiable}'s ID is unique, with respect to the given {@link ResourceImpl}. This method
 	 * is not using a command, assuming that the {@link Identifiable} is not yet attached to the model.
 	 */
 	public static void ensureIdIsUnique(ResourceImpl resource, Identifiable identifiable) {
 		Set<String> ids = resource.getIntrinsicIDToEObjectMap().keySet();
 		if (identifiable.getIdentifier() == null || ids.contains(identifiable.getIdentifier())) {
 			identifiable.setIdentifier("rmf-" + UUID.randomUUID()); //$NON-NLS-1$
 		}
 	}
 }
