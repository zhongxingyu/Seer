 /**
  * <copyright>
  *
  * Copyright (c) 2012 E.D.Willink and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     E.D.Willink - initial API and implementation
  *
  * </copyright>
  */
 package org.eclipse.ocl.examples.pivot.context;
 
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 
 import org.eclipse.jdt.annotation.NonNull;
 import org.eclipse.jdt.annotation.Nullable;
 import org.eclipse.ocl.examples.domain.utilities.DomainUtil;
 import org.eclipse.ocl.examples.pivot.Metaclass;
 import org.eclipse.ocl.examples.pivot.CollectionType;
 import org.eclipse.ocl.examples.pivot.ExpressionInOCL;
 import org.eclipse.ocl.examples.pivot.NamedElement;
 import org.eclipse.ocl.examples.pivot.Operation;
 import org.eclipse.ocl.examples.pivot.Parameter;
 import org.eclipse.ocl.examples.pivot.PivotFactory;
 import org.eclipse.ocl.examples.pivot.PrimitiveType;
 import org.eclipse.ocl.examples.pivot.Property;
 import org.eclipse.ocl.examples.pivot.TupleType;
 import org.eclipse.ocl.examples.pivot.Type;
 import org.eclipse.ocl.examples.pivot.TypedElement;
 import org.eclipse.ocl.examples.pivot.UnspecifiedType;
 import org.eclipse.ocl.examples.pivot.Variable;
 import org.eclipse.ocl.examples.pivot.manager.MetaModelManager;
 import org.eclipse.ocl.examples.pivot.utilities.AbstractConversion;
 import org.eclipse.ocl.examples.pivot.utilities.PivotUtil;
 
 /**
  * AbstractBase2PivotConversion provides the Xtext independent support for Concrete Syntax
  * to Pivot conversion.
  */
 public abstract class AbstractBase2PivotConversion extends AbstractConversion implements Base2PivotConversion
 {
 	/**
 	 * Set of all expression nodes whose type involves an UnspecifiedType. These are
 	 * created during the left2right pass and are finally resolved to
 	 * minimize invalidity.
 	 */
 	private HashSet<TypedElement> underspecifiedTypedElements = null;
 
 	protected AbstractBase2PivotConversion(@NonNull MetaModelManager metaModelManager) {
 		super(metaModelManager);
 	}
 
 	protected void addUnderspecifiedTypedElement(@NonNull TypedElement pivotElement) {
 		if (underspecifiedTypedElements == null) {
 			underspecifiedTypedElements  = new HashSet<TypedElement>();
 		}
 		underspecifiedTypedElements.add(pivotElement);
 	}
 
 	public void refreshName(@NonNull NamedElement pivotNamedElement, @NonNull String newName) {
 		String oldName = pivotNamedElement.getName();
 		if ((newName != oldName) && (/*(newName == null) ||*/ !newName.equals(oldName))) {
 			pivotNamedElement.setName(newName);
 		}
 	}
 
 	protected void resolveUnderspecifiedTypes() {
 		if (underspecifiedTypedElements != null) {
 			for (TypedElement underspecifiedTypedElement : underspecifiedTypedElements) {
 				Type underspecifiedType = DomainUtil.nonNullModel(underspecifiedTypedElement.getType());
 				Type resolvedType = resolveUnderspecifiedType(underspecifiedType);
 				underspecifiedTypedElement.setType(resolvedType);
 			}
 		}
 	}
 	
 	protected @NonNull Type resolveUnderspecifiedType(@NonNull Type type) {
 		if (type instanceof UnspecifiedType) {
 			return DomainUtil.nonNullModel(((UnspecifiedType)type).getLowerBound());
 		}
 		if (type instanceof CollectionType) {
 			CollectionType collectionType = (CollectionType)type;
 			Type resolvedElementType = resolveUnderspecifiedType(DomainUtil.nonNullModel(collectionType.getElementType()));
 			return metaModelManager.getCollectionType(PivotUtil.getUnspecializedTemplateableElement(collectionType), resolvedElementType, null, null);
 //			return metaModelManager.getCollectionType(DomainUtil.nonNullModel(collectionType.getName()), resolvedElementType);
 		}
 		if (type instanceof PrimitiveType) {
 			return type;
 		}
 		if (type instanceof TupleType) {
 			TupleType tupleType = (TupleType)type;
 			List<Property> resolvedProperties = new ArrayList<Property>();
 			for (Property part : ((TupleType)type).getOwnedAttribute()) {
 				if (metaModelManager.isUnderspecified(part.getType())) {
 					Property prop = PivotFactory.eINSTANCE.createProperty();
 					prop.setName(part.getName());
 					prop.setType(resolveUnderspecifiedType(DomainUtil.nonNullModel(part.getType())));
					resolvedProperties.add(part);
 				}
 				else {
 					resolvedProperties.add(part);
 				}
 			}
 			return metaModelManager.getTupleType(DomainUtil.nonNullModel(tupleType.getName()), resolvedProperties, null);
 		}
 		if (type instanceof Metaclass) {
 			Metaclass metaclass = (Metaclass)type;
 			Type resolvedElementType = resolveUnderspecifiedType(DomainUtil.nonNullModel(metaclass.getInstanceType()));
 			return metaModelManager.getMetaclass(resolvedElementType);
 		}
 		throw new UnsupportedOperationException();
 //		return null;
 	}
 
 	public void setBehavioralType(@NonNull TypedElement targetElement, @NonNull TypedElement sourceElement) {
 		if (!sourceElement.eIsProxy()) {
 			Type type = PivotUtil.getBehavioralType(sourceElement);
 			if (!type.eIsProxy()) {
 				setType(targetElement, type);
 				return;
 			}
 		}
 		setType(targetElement, null);
 	}
 
 	public void setContextVariable(@NonNull ExpressionInOCL pivotSpecification, @NonNull String selfVariableName, @Nullable Type contextType) {
 		Variable contextVariable = pivotSpecification.getContextVariable();
 		if (contextVariable == null) {
 			@SuppressWarnings("null")
 			@NonNull Variable nonNullContextVariable = PivotFactory.eINSTANCE.createVariable();
 			contextVariable = nonNullContextVariable;
 			pivotSpecification.setContextVariable(contextVariable);
 		}
 		refreshName(contextVariable, selfVariableName);
 		setType(contextVariable, contextType);
 	}
 
 	public void setClassifierContext(@NonNull ExpressionInOCL pivotSpecification, @NonNull Type contextType) {
 		Variable contextVariable = pivotSpecification.getContextVariable();
 		if (contextVariable != null) {
 			if (contextType.eIsProxy()) {
 				setType(contextVariable, null);
 			}
 			else {
 				setType(contextVariable, contextType);
 			}
 		}
 	}
 
 	public void setOperationContext(@NonNull ExpressionInOCL pivotSpecification, @NonNull Operation contextOperation, @Nullable String resultName) {
 		Variable contextVariable = pivotSpecification.getContextVariable();
 //		pivotSpecification.getParameterVariable().clear();
 		if ((contextVariable != null) && !contextOperation.eIsProxy()) {
 			setType(contextVariable, contextOperation.getOwningType());
 			setParameterVariables(pivotSpecification, DomainUtil.nonNullEMF(contextOperation.getOwnedParameter()));
 		}
 		if (resultName != null) {
 			setResultVariable(pivotSpecification, contextOperation, resultName);
 		}
 	}
 
 	public void setParameterVariables(@NonNull ExpressionInOCL pivotSpecification, @NonNull List<Parameter> parameters) {
 		List<Variable> oldVariables = new ArrayList<Variable>(pivotSpecification.getParameterVariable());
 		List<Variable> newVariables = new ArrayList<Variable>();
 		for (Parameter parameter : parameters) {
 		    String name = parameter.getName();
 			Variable param = DomainUtil.getNamedElement(oldVariables, name);
 		    if (param != null) {
 		    	oldVariables.remove(param);
 		    }
 		    else {
 		    	param = PivotFactory.eINSTANCE.createVariable();
 		        param.setName(name);
 		    }
 		    setBehavioralType(param, parameter);
 		    param.setRepresentedParameter(parameter);
 		    newVariables.add(param);
 		}
 		refreshList(DomainUtil.nonNullModel(pivotSpecification.getParameterVariable()), newVariables);
 	}
 
 	public void setParameterVariables(@NonNull ExpressionInOCL pivotSpecification, @NonNull Map<String, Type> parameters) {
 		List<Variable> oldVariables = new ArrayList<Variable>(pivotSpecification.getParameterVariable());
 		List<Variable> newVariables = new ArrayList<Variable>();
 		for (String name : parameters.keySet()) {
 		    Type type = parameters.get(name);
 			Variable param = DomainUtil.getNamedElement(oldVariables, name);
 		    if (param != null) {
 		    	oldVariables.remove(param);
 		    }
 		    else {
 		    	param = PivotFactory.eINSTANCE.createVariable();
 		        param.setName(name);
 		    }
 			setType(param, type);
 //		    param.setRepresentedParameter(parameter);
 		    newVariables.add(param);
 		}
 		refreshList(DomainUtil.nonNullModel(pivotSpecification.getParameterVariable()), newVariables);
 	}
 
 	public void setPropertyContext(@NonNull ExpressionInOCL pivotSpecification, @NonNull Property contextProperty) {
 		Variable contextVariable = pivotSpecification.getContextVariable();
 		if ((contextVariable != null) && !contextProperty.eIsProxy()) {
 			setType(contextVariable, contextProperty.getOwningType());
 		}
 	}
 
 	public void setResultVariable(@NonNull ExpressionInOCL pivotSpecification, @NonNull Operation contextOperation, @NonNull String resultName) {
 		Type returnType = contextOperation.getType();
 		if (returnType != null) {					// FIXME BUG 385711 Use OclVoid rather than null
 			Variable resultVariable = pivotSpecification.getResultVariable();
 			if (resultVariable == null) {
 				resultVariable = PivotFactory.eINSTANCE.createVariable();
 			}
 			resultVariable.setName(resultName);
 			setBehavioralType(resultVariable, contextOperation);
 			pivotSpecification.setResultVariable(resultVariable);
 		}
 		else {
 			pivotSpecification.setResultVariable(null);
 		}
 	}
 
 	/**
 	 * Set the type and so potentially satisfy some TypeOfDependency. This method ensures that
 	 * type is not set to null.
 	 * 
 	 * @param pivotExpression
 	 * @param type
 	 */
 	public void setType(@NonNull TypedElement pivotElement, Type type) {
 	//	PivotUtil.debugObjectUsage("setType ", pivotElement);
 	//	PivotUtil.debugObjectUsage(" to ", type);
 //		if (type != null) {
 //			if (type.eResource() == null) {			// WIP
 	//			PivotUtil.debugObjectUsage("setType orphan ", type);
 //				assert false;
 //			}
 //		}
 //		if (type == null) {
 //			type = metaModelManager.getOclInvalidType();	// FIXME unresolved type with explanation
 //		}
 		Type primaryType = type != null ? metaModelManager.getPrimaryType(type) : null;
 		if (primaryType != pivotElement.getType()) {
 			pivotElement.setType(primaryType);
 			if (metaModelManager.isUnderspecified(primaryType)) {
 				addUnderspecifiedTypedElement(pivotElement);
 			}
 		}
 		if (primaryType != null) {
 			PivotUtil.debugWellContainedness(primaryType);
 		}
 	}
 }
