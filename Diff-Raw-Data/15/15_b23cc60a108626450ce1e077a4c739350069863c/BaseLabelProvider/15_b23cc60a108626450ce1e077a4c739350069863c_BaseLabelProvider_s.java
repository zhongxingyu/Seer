 /**
  * <copyright>
  *
  * Copyright (c) 2010,2011 E.D.Willink and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     E.D.Willink - initial API and implementation
  *
  * </copyright>
  *
  * $Id: BaseLabelProvider.java,v 1.9 2011/05/03 06:42:32 ewillink Exp $
  */
 package org.eclipse.ocl.examples.xtext.essentialocl.ui.labeling;
 
 import java.net.URL;
 import java.util.Collection;
 import java.util.List;
 
 import org.eclipse.core.runtime.FileLocator;
 import org.eclipse.core.runtime.Path;
 import org.eclipse.core.runtime.Platform;
 import org.eclipse.emf.ecore.util.EcoreUtil;
 import org.eclipse.emf.edit.ui.provider.AdapterFactoryLabelProvider;
 import org.eclipse.jdt.annotation.NonNull;
 import org.eclipse.jface.resource.ImageDescriptor;
 import org.eclipse.ocl.examples.pivot.Annotation;
 import org.eclipse.ocl.examples.pivot.AnyType;
 import org.eclipse.ocl.examples.pivot.AssociationClassCallExp;
 import org.eclipse.ocl.examples.pivot.BagType;
 import org.eclipse.ocl.examples.pivot.BooleanLiteralExp;
 import org.eclipse.ocl.examples.pivot.CollectionItem;
 import org.eclipse.ocl.examples.pivot.CollectionLiteralExp;
 import org.eclipse.ocl.examples.pivot.CollectionLiteralPart;
 import org.eclipse.ocl.examples.pivot.CollectionRange;
 import org.eclipse.ocl.examples.pivot.CollectionType;
 import org.eclipse.ocl.examples.pivot.Comment;
 import org.eclipse.ocl.examples.pivot.Constraint;
 import org.eclipse.ocl.examples.pivot.DataType;
 import org.eclipse.ocl.examples.pivot.Detail;
 import org.eclipse.ocl.examples.pivot.Element;
 import org.eclipse.ocl.examples.pivot.EnumLiteralExp;
 import org.eclipse.ocl.examples.pivot.EnumerationLiteral;
 import org.eclipse.ocl.examples.pivot.ExpressionInOCL;
 import org.eclipse.ocl.examples.pivot.IfExp;
 import org.eclipse.ocl.examples.pivot.IntegerLiteralExp;
 import org.eclipse.ocl.examples.pivot.InvalidLiteralExp;
 import org.eclipse.ocl.examples.pivot.InvalidType;
 import org.eclipse.ocl.examples.pivot.IterateExp;
 import org.eclipse.ocl.examples.pivot.Iteration;
 import org.eclipse.ocl.examples.pivot.IteratorExp;
 import org.eclipse.ocl.examples.pivot.LambdaType;
 import org.eclipse.ocl.examples.pivot.LetExp;
 import org.eclipse.ocl.examples.pivot.LiteralExp;
 import org.eclipse.ocl.examples.pivot.LoopExp;
 import org.eclipse.ocl.examples.pivot.MessageExp;
 import org.eclipse.ocl.examples.pivot.MessageType;
 import org.eclipse.ocl.examples.pivot.NamedElement;
 import org.eclipse.ocl.examples.pivot.Namespace;
 import org.eclipse.ocl.examples.pivot.NavigationCallExp;
 import org.eclipse.ocl.examples.pivot.NullLiteralExp;
 import org.eclipse.ocl.examples.pivot.NumericLiteralExp;
 import org.eclipse.ocl.examples.pivot.OCLExpression;
 import org.eclipse.ocl.examples.pivot.Operation;
 import org.eclipse.ocl.examples.pivot.OperationCallExp;
 import org.eclipse.ocl.examples.pivot.OrderedSetType;
 import org.eclipse.ocl.examples.pivot.Parameter;
 import org.eclipse.ocl.examples.pivot.ParameterableElement;
 import org.eclipse.ocl.examples.pivot.Precedence;
 import org.eclipse.ocl.examples.pivot.PrimitiveLiteralExp;
 import org.eclipse.ocl.examples.pivot.PrimitiveType;
 import org.eclipse.ocl.examples.pivot.Property;
 import org.eclipse.ocl.examples.pivot.PropertyCallExp;
 import org.eclipse.ocl.examples.pivot.RealLiteralExp;
 import org.eclipse.ocl.examples.pivot.SequenceType;
 import org.eclipse.ocl.examples.pivot.SetType;
 import org.eclipse.ocl.examples.pivot.StateExp;
 import org.eclipse.ocl.examples.pivot.StringLiteralExp;
 import org.eclipse.ocl.examples.pivot.TemplateBinding;
 import org.eclipse.ocl.examples.pivot.TemplateParameter;
 import org.eclipse.ocl.examples.pivot.TemplateParameterSubstitution;
 import org.eclipse.ocl.examples.pivot.TemplateParameterType;
 import org.eclipse.ocl.examples.pivot.TemplateSignature;
 import org.eclipse.ocl.examples.pivot.TemplateableElement;
 import org.eclipse.ocl.examples.pivot.TupleLiteralExp;
 import org.eclipse.ocl.examples.pivot.TupleLiteralPart;
 import org.eclipse.ocl.examples.pivot.TupleType;
 import org.eclipse.ocl.examples.pivot.Type;
 import org.eclipse.ocl.examples.pivot.TypeExp;
 import org.eclipse.ocl.examples.pivot.UMLReflection;
 import org.eclipse.ocl.examples.pivot.UnlimitedNaturalLiteralExp;
 import org.eclipse.ocl.examples.pivot.Variable;
 import org.eclipse.ocl.examples.pivot.VariableExp;
 import org.eclipse.ocl.examples.pivot.VoidType;
 import org.eclipse.ocl.examples.pivot.prettyprint.PrettyPrinter;
 import org.eclipse.ocl.examples.pivot.util.Nameable;
 import org.eclipse.ocl.examples.pivot.utilities.Pivot2Moniker;
 import org.eclipse.ocl.examples.pivot.utilities.PivotUtil;
 import org.eclipse.ocl.examples.xtext.base.baseCST.ImportCS;
 import org.eclipse.ocl.examples.xtext.base.baseCST.NamedElementCS;
 import org.eclipse.ocl.examples.xtext.base.baseCST.PathNameCS;
 import org.eclipse.ocl.examples.xtext.base.baseCST.PivotableElementCS;
 import org.eclipse.ocl.examples.xtext.base.baseCST.TypeRefCS;
 import org.eclipse.swt.graphics.Image;
 import org.eclipse.xtext.ui.label.DefaultEObjectLabelProvider;
 import org.osgi.framework.Bundle;
 
 import com.google.inject.Inject;
 
 /**
  * Provides labels for BaseCST objects redirecting to Pivot objects.
  * 
  * see http://www.eclipse.org/Xtext/documentation/latest/xtext.html#labelProvider
  */
 public class BaseLabelProvider extends DefaultEObjectLabelProvider {
 
 	@Inject
 	public BaseLabelProvider(@NonNull AdapterFactoryLabelProvider delegate) {
 		super(delegate);
 	}
 
 	protected void appendClass(@NonNull StringBuilder s, Object object) {
 		s.append("<");
 		s.append(object != null ? object.getClass().getSimpleName() : "null");
 		s.append(">");
 	}
 
 /*	protected void appendMultiplicity(@NonNull StringBuilder s, TypedMultiplicityElement ele) {
 		long lower = ele.getLower().longValue();
 		long upper = ele.getUpper().longValue();
 		if ((lower != 1) || (upper != 1)) {
 			DomainUtil.formatMultiplicity(s, lower, upper);
 		}
 	} */
 
 	protected void appendName(@NonNull StringBuilder s, NamedElement element) {
 		if (element != null) {
 			if (element.eIsProxy()) {
 				EcoreUtil.resolve(element, element);
 			}
 			appendString(s, element.getName());
 		}	
 	}
 
 	protected void appendName(@NonNull StringBuilder s, NamedElementCS csElement) {
 		if (csElement != null) {
 			if (csElement.eIsProxy()) {
 				EcoreUtil.resolve(csElement, csElement);
 			}
 			appendString(s, csElement.getName());
 		}	
 	}
 
 	protected void appendName(@NonNull StringBuilder s, Nameable csElement) {
 		appendString(s, csElement.getName());
 	}
 
 	protected void appendOptionalName(@NonNull StringBuilder s, Nameable csElement) {
 		if (csElement != null) {
 			appendOptionalString(s, csElement.getName());
 		}	
 	}
 
 	protected void appendOptionalString(@NonNull StringBuilder s, String string) {
 		if (string != null) {
 			s.append(string);
 		}	
 	}
 
 	protected void appendParameters(@NonNull StringBuilder s, List<Parameter> parameters) {
 		s.append("(");
 		String prefix = "";
 		for (Parameter csParameter : parameters) {
 			s.append(prefix);
 			appendType(s, csParameter.getType());
 //			appendMultiplicity(s, csParameter);
 			prefix = ", ";
 		}
 		s.append(")");
 	}
 
 	protected void appendString(@NonNull StringBuilder s, String string) {
 		if (string != null) {
 			s.append(string);
 		}	
 		else {
 			s.append("<null>");
 		}	
 	}
 
 	protected void appendPathName(StringBuilder s, PathNameCS pathName) {
 		s.append(pathName.toString());
 	}
 
 	protected void appendString(@NonNull StringBuilder s, String string, int countLimit) {
 		if (string == null) {
 			s.append("<null>");
 		}	
 		else if (string.length() > countLimit){
 			s.append(string.substring(0, countLimit-3));
 			s.append("...");
 		}	
 		else {
 			s.append(string);
 		}	
 	}
 
 	protected void appendSuperTypes(@NonNull StringBuilder s, List<? extends Type> superTypes) {
 		if (!superTypes.isEmpty()) {
 			String prefix = " -> ";
 			for (Type superType : superTypes) {
 				s.append(prefix);
 				appendType(s, superType);
 				prefix = ", ";
 			}
 		}
 	}
 
 	protected void appendTemplateBindings(@NonNull StringBuilder s, TemplateableElement templateableElement) {
 		if (templateableElement != null) {
 			for (TemplateBinding templateBinding : templateableElement.getTemplateBinding()) {
 				s.append("(");
 				String prefix = "";
 				for (TemplateParameterSubstitution templateParameterSubstitution : templateBinding.getParameterSubstitution()) {
 					s.append(prefix);
 					ParameterableElement actual = templateParameterSubstitution.getActual();
 					if (actual instanceof Type) {
 						appendType(s, (Type)actual);
 					}
 					prefix = ", ";
 				}
 				s.append(")");
 			}
 		}
 	}
 
 	protected void appendTemplateSignature(@NonNull StringBuilder s, TemplateableElement templateableElement) {
 		if (templateableElement != null) {
 			TemplateSignature templateSignature = templateableElement.getOwnedTemplateSignature();
 			if (templateSignature != null) {
 				s.append("(");
 				Collection<TemplateParameter> templateParameters = templateSignature.getParameter();
 				if (!templateParameters.isEmpty()) {
 					String prefix = "";
 					for (TemplateParameter templateParameter : templateParameters) {
 						s.append(prefix);
 						appendName(s, (NamedElement) templateParameter.getParameteredElement());
 						prefix = ", ";
 					}
 				}
 				s.append(")");
 			}
 		}
 	}
 
 	protected void appendType(@NonNull StringBuilder s, Type type) {
 		appendName(s, type);
 		appendTemplateBindings(s, type);
 	}
 
 	protected void appendType(@NonNull StringBuilder s, TypeRefCS type) {
 		Element pivot = type.getPivot();
 		appendString(s, safeGetMoniker(pivot));
 	}
 	
 	@Override
 	protected Image convertToImage(Object imageDescription) {
 		if (imageDescription instanceof String) {
 			String imagePath = (String)imageDescription;
 			if (imagePath.startsWith("/")) {	
 				int index = imagePath.indexOf('/', 1);
 				if (index > 1) {
 					String bundlePath = imagePath.substring(1, index);
 					Bundle bundle = Platform.getBundle(bundlePath);
 					if (bundle != null) {
 						String imageFile = imagePath.substring(index+1);
 						Path path = new Path(imageFile);
 						URL imgUrl = FileLocator.find(bundle, path, null);
 						if (imgUrl != null) {
 							return super.convertToImage(ImageDescriptor.createFromURL(imgUrl));
 						}
 					}
 				}
 			}				
 		}
 		return super.convertToImage(imageDescription);
 	}
 	 
 	@Override
 	protected Object doGetText(Object element) {
 		Object object = super.doGetText(element);
 		if (object instanceof String) {
 			String text = (String)object;
 			int index = text.indexOf('\n');
 			if (index >= 0) {
 				text = text.substring(0, index);
 			}
 			object = text.trim();
 		}
 		return object;
 	}
 
 	protected String safeGetMoniker(Element element) {
 		if (element == null) {
 			return "<null-element>";
 		}
 		else if (element.eIsProxy()) {
 			return "<unresolved-proxy>";
 		}
 		else {
 			return Pivot2Moniker.toString(element);
 		}
 	}
 
 	protected String text(Element ele) {
 		return "<" + ele.getClass().getSimpleName() + ">";
 	}
 
 	protected String image(Annotation ele) {
 		return "/org.eclipse.emf.ecore.edit/icons/full/obj16/EAnnotation.gif";
 	}
 
 	public String text(Annotation ele) {
 		StringBuilder s = new StringBuilder();
 		s.append("\"");
 		appendString(s, ele.getName(), 40);			
 		s.append("\"");
 		return s.toString();
 	}
 
 	protected String image(AnyType ele) {
 		return "/org.eclipse.ocl.edit/icons/full/obj16/AnyType.gif";
 	}
 
 	protected String image(AssociationClassCallExp ele) {
 		return "/org.eclipse.ocl.edit/icons/full/obj16/AssociationClassCallExp.gif";
 	}
 
 	protected String image(BagType ele) {
 		return "/org.eclipse.ocl.edit/icons/full/obj16/BagType.gif";
 	}
 
 	protected String image(BooleanLiteralExp ele) {
 		return "/org.eclipse.ocl.edit/icons/full/obj16/BooleanLiteralExp.gif";
 	}
 
 	protected String image(org.eclipse.ocl.examples.pivot.Class ele) {
 		if (ele.getOwningTemplateParameter() != null) {
 			return "/org.eclipse.uml2.uml.edit/icons/full/obj16/TemplateParameter.gif";
 		}
 		else {
 			return "/org.eclipse.uml2.uml.edit/icons/full/obj16/Class.gif";
 		}
 	}
 
 	protected String text(org.eclipse.ocl.examples.pivot.Class ele) {
 		StringBuilder s = new StringBuilder();
 		appendName(s, ele);
 		appendTemplateSignature(s, ele);
 		appendSuperTypes(s, ele.getSuperClass());
 		return s.toString();
 	}
 
 	protected String image(CollectionItem ele) {
 		return "/org.eclipse.ocl.edit/icons/full/obj16/CollectionItem.gif";
 	}
 
 	protected String image(CollectionLiteralExp ele) {
 		return "/org.eclipse.ocl.edit/icons/full/obj16/CollectionLiteralExp.gif";
 	}
 
 	protected String image(CollectionLiteralPart ele) {
 		return "/org.eclipse.ocl.edit/icons/full/obj16/CollectionLiteralPart.gif";
 	}
 
 //	protected String text(CollectionLiteralPart ele) {
 //		return null;
 //	}
 
 	protected String image(CollectionRange ele) {
 		return "/org.eclipse.ocl.edit/icons/full/obj16/CollectionRange.gif";
 	}
 
 	protected String image(CollectionType ele) {
 		return "/org.eclipse.ocl.edit/icons/full/obj16/CollectionType.gif";
 	}
 
 	protected String text(CollectionType ele) {
 		StringBuilder s = new StringBuilder();
 		appendType(s, ele);
 		return s.toString();
 	}
 
 	protected String image(Comment ele) {
 		return "/org.eclipse.uml2.uml.edit/icons/full/obj16/Comment.gif";
 	}
 
 	public String text(Comment ele) {
 		StringBuilder s = new StringBuilder();
 		s.append("\"");
 		appendString(s, ele.getBody(), 40);			
 		s.append("\"");
 		return s.toString();
 	}
 
 //	protected String image(Constraint ele) {
 //		return "/org.eclipse.uml2.uml.edit/icons/full/obj16/Constraint.gif";
 //	}
 	protected String image(Constraint ele) {
 		String stereotype = ele.getStereotype();
 		if (UMLReflection.BODY.equals(stereotype)) {
 			return "/org.eclipse.ocl.examples.xtext.oclinecore.ui/icons/full/obj16/DefinitionConstraint.gif";
 		}
 		else if (UMLReflection.DERIVATION.equals(stereotype)) {
 			return "/org.eclipse.ocl.examples.xtext.oclinecore.ui/icons/full/obj16/DerivationConstraint.gif";
 		}
 		else if (UMLReflection.INITIAL.equals(stereotype)) {
 			return "/org.eclipse.ocl.examples.xtext.oclinecore.ui/icons/full/obj16/InitialConstraint.gif";
 		}
 		else if (UMLReflection.INVARIANT.equals(stereotype)) {
 			return "/org.eclipse.ocl.examples.xtext.oclinecore.ui/icons/full/obj16/InvariantConstraint.gif";
 		}
 		else if (UMLReflection.POSTCONDITION.equals(stereotype)) {
 			return "/org.eclipse.ocl.examples.xtext.oclinecore.ui/icons/full/obj16/PostconditionConstraint.gif";
 		}
 		else if (UMLReflection.PRECONDITION.equals(stereotype)) {
 			return "/org.eclipse.ocl.examples.xtext.oclinecore.ui/icons/full/obj16/PreconditionConstraint.gif";
 		}
 		return "/org.eclipse.ocl.edit/icons/full/obj16/Constraint.gif";
 	}
 
 	public String text(Constraint ele) {
 		StringBuilder s = new StringBuilder();
 		s.append("<");
 		appendString(s, ele.getStereotype());
 		s.append("> ");
 		String name = ele.getName();
 		if (name != null) {
 			s.append(name);
 		}	
 		return s.toString();
 	}
 
 	protected String text(DataType ele) {
 		StringBuilder s = new StringBuilder();
 		appendName(s, ele);
 		appendTemplateSignature(s, ele);
 		appendSuperTypes(s, ele.getSuperClass());
 		String instance = ele.getInstanceClassName();
 		if (instance != null) {
 			s.append(" [");
 			s.append(instance);
 			s.append("]");
 		}
 		return s.toString();
 	}
 
 	protected String image(EnumLiteralExp ele) {
 		return "/org.eclipse.ocl.edit/icons/full/obj16/EnumLiteralExp.gif";
 	}
 
 	protected String text(org.eclipse.ocl.examples.pivot.Enumeration ele) {
 		StringBuilder s = new StringBuilder();
 		appendName(s, ele);
 		appendTemplateSignature(s, ele);
 		appendSuperTypes(s, ele.getSuperClass());
 		String instance = ele.getInstanceClassName();
 		if (instance != null) {
 			s.append(" [");
 			s.append(instance);
 			s.append("]");
 		}
 		return s.toString();
 	}
 
 	protected String image(Detail ele) {
 		return "/org.eclipse.emf.ecore.edit/icons/full/obj16/EStringToStringMapEntry.gif";
 	}
 
 	public String text(Detail ele) {
 		StringBuilder s = new StringBuilder();
 		s.append("\"");
 		appendString(s, ele.getName());			
 		s.append("\" : ");
 		appendString(s, ele.getValue().get(0), 40);
 		return s.toString();
 	}
 
 	protected String image(EnumerationLiteral ele) {
 		return "/org.eclipse.uml2.uml.edit/icons/full/obj16/EnumerationLiteral.gif";
 	}
 
 	protected String text(EnumerationLiteral ele) {
 		return ele.getName();
 	}
 
 	protected String image(ExpressionInOCL ele) {
 		return "/org.eclipse.ocl.edit/icons/full/obj16/ExpressionInOCL.gif";
 	}
 
 	protected String text(ExpressionInOCL ele) {
 		return "<ExpressionInOCL>";
 	}
 
 	protected String image(IfExp ele) {
 		return "/org.eclipse.ocl.edit/icons/full/obj16/IfExp.gif";
 	}
 
 	protected String text(IfExp ele) {
 		return "if";
 	}
 
 	protected String image(ImportCS ele) {
 		return "/org.eclipse.uml2.uml.edit/icons/full/obj16/PackageImport.gif";
 	}
 
 	protected String text(ImportCS ele) {
 		StringBuilder s = new StringBuilder();
 		appendOptionalName(s, ele);
 		s.append(" : ");
 		appendPathName(s, ele.getPathName());
 		return s.toString();
 	}
 
 	protected String image(IntegerLiteralExp ele) {
 		return "/org.eclipse.ocl.edit/icons/full/obj16/IntegerLiteralExp.gif";
 	}
 
 	protected String image(InvalidLiteralExp ele) {
 		return "/org.eclipse.ocl.edit/icons/full/obj16/InvalidLiteralExp.gif";
 	}
 
 	protected String image(InvalidType ele) {
 		return "/org.eclipse.ocl.edit/icons/full/obj16/InvalidType.gif";
 	}
 
 	protected String image(IterateExp ele) {
 		return "/org.eclipse.ocl.edit/icons/full/obj16/IterateExp.gif";
 	}
 
 	protected String image(Iteration ele) {
 		return "/org.eclipse.ocl.examples.pivot/icons/full/obj16/Iteration.gif";
 	}
 
 	protected String text(Iteration ele) {
 		StringBuilder s = new StringBuilder();
 		appendName(s, ele);
 		appendTemplateSignature(s, ele);
 		appendParameters(s, ele.getOwnedIterator());
 		s.append(" : ");
 		appendType(s, ele.getType());
 //		appendMultiplicity(s, ele);
 		return s.toString();
 	}
 
 	protected String image(IteratorExp ele) {
 		return "/org.eclipse.ocl.edit/icons/full/obj16/IteratorExp.gif";
 	}
 
 	protected String image(LambdaType ele) {
 		return "/org.eclipse.ocl.examples.pivot/icons/full/obj16/LambdaType.gif";
 	}
 
 	protected String image(LetExp ele) {
 		return "/org.eclipse.ocl.edit/icons/full/obj16/LetExp.gif";
 	}
 
 	protected String image(LiteralExp ele) {
 		return "/org.eclipse.ocl.edit/icons/full/obj16/LiteralExp.gif";
 	}
 
 	protected String image(LoopExp ele) {
 		return "/org.eclipse.ocl.edit/icons/full/obj16/LoopExp.gif";
 	}
 
 	protected String text(LoopExp ele) {
		return text(ele.getReferredIteration());
 	}
 
 	protected String image(MessageExp ele) {
 		return "/org.eclipse.ocl.edit/icons/full/obj16/MessageExp.gif";
 	}
 
 	protected String image(MessageType ele) {
 		return "/org.eclipse.ocl.edit/icons/full/obj16/MessageType.gif";
 	}
 
 	protected String image(NavigationCallExp ele) {
 		return "/org.eclipse.ocl.edit/icons/full/obj16/NavigationCallExp.gif";
 	}
 
 	protected String image(NullLiteralExp ele) {
 		return "/org.eclipse.ocl.edit/icons/full/obj16/NullLiteralExp.gif";
 	}
 
 	protected String image(NumericLiteralExp ele) {
 		return "/org.eclipse.ocl.edit/icons/full/obj16/NumericLiteralExp.gif";
 	}
 
 	protected String text(OCLExpression ele) {
 		assert ele != null;
 		Namespace namespace = PivotUtil.getNamespace(ele.eContainer());
 		if (namespace == null) {
 			return "";
 		}
 		StringBuilder s = new StringBuilder();
 		s.append(PrettyPrinter.printName(ele, namespace));
 		s.append(" : ");
 		Type type = ele.getType();
 		if (type != null) {
 			s.append(PrettyPrinter.printType(type, namespace));
 		}
 		return s.toString();
 	}
 
 	protected String image(Operation ele) {
 		return "/org.eclipse.uml2.uml.edit/icons/full/obj16/Operation.gif";
 	}
 
 	protected String text(Operation ele) {
 		StringBuilder s = new StringBuilder();
 		appendName(s, ele);
 		appendTemplateSignature(s, ele);
 		appendParameters(s, ele.getOwnedParameter());
 		s.append(" : ");
 		appendType(s, ele.getType());
 //		appendMultiplicity(s, ele);
 		return s.toString();
 	}
 
 	protected String image(OperationCallExp ele) {
 		return "/org.eclipse.ocl.edit/icons/full/obj16/OperationCallExp.gif";
 	}
 
 	protected String text(OperationCallExp ele) {
		return text(ele.getReferredOperation());
 	}
 
 	protected String image(OrderedSetType ele) {
 		return "/org.eclipse.ocl.edit/icons/full/obj16/OrderedSetType.gif";
 	}
 
 	protected String image(org.eclipse.ocl.examples.pivot.Package ele) {
 		return "/org.eclipse.uml2.uml.edit/icons/full/obj16/Package.gif";
 	}
 
 	protected String text(org.eclipse.ocl.examples.pivot.Package ele) {
 		return ele.getName();
 	}
 
 	protected String image(Parameter ele) {
 		return "/org.eclipse.uml2.uml.edit/icons/full/obj16/Parameter_in.gif";
 	}
 
 	protected String text(Parameter ele) {
 		StringBuilder s = new StringBuilder();
 		appendName(s, ele);
 		s.append(" : ");
 		appendType(s, ele.getType());
 //		appendMultiplicity(s, ele);
 		return s.toString();
 	}
 
 	protected String text(PathNameCS ele) {
 		StringBuilder s = new StringBuilder();
 		appendPathName(s, ele);
 		return s.toString();
 	}
 	
 	protected Object image(PivotableElementCS ele) {
 		return doGetImage(ele.getPivot());
 	}
 	
 	protected Object text(PivotableElementCS ele) {
 		return doGetText(ele.getPivot());
 	}
 
 	protected String text(Precedence ele) {
 		assert ele != null;
 		return PrettyPrinter.print(ele);
 	}
 
 	protected String image(PrimitiveLiteralExp ele) {
 		return "/org.eclipse.ocl.edit/icons/full/obj16/PrimitiveLiteralExp.gif";
 	}
 
 	protected String image(PrimitiveType ele) {
 //		return "/org.eclipse.ocl.edit/icons/full/obj16/PrimitiveType.gif";
 		return "/org.eclipse.uml2.uml.edit/icons/full/obj16/PrimitiveType.gif";
 	}
 
 	protected String image(Property ele) {
 		return "/org.eclipse.uml2.uml.edit/icons/full/obj16/Property.gif";
 	}
 
 	protected String text(Property ele) {
 		StringBuilder s = new StringBuilder();
 		appendName(s, ele);
 		s.append(" : ");
 		appendType(s, ele.getType());
 //		appendMultiplicity(s, ele);
 		return s.toString();
 	}
 
 	protected String image(PropertyCallExp ele) {
 		return "/org.eclipse.ocl.edit/icons/full/obj16/PropertyCallExp.gif";
 	}
 
 	protected String text(PropertyCallExp ele) {
		return text(ele.getReferredProperty());
 	}
 
 	protected String image(RealLiteralExp ele) {
 		return "/org.eclipse.ocl.edit/icons/full/obj16/RealLiteralExp.gif";
 	}
 
 	protected String image(SequenceType ele) {
 		return "/org.eclipse.ocl.edit/icons/full/obj16/SequenceType.gif";
 	}
 
 	protected String image(SetType ele) {
 		return "/org.eclipse.ocl.edit/icons/full/obj16/SetType.gif";
 	}
 
 	protected String image(StateExp ele) {
 		return "/org.eclipse.ocl.edit/icons/full/obj16/StateExp.gif";
 	}
 
 	protected String image(StringLiteralExp ele) {
 		return "/org.eclipse.ocl.edit/icons/full/obj16/StringLiteralExp.gif";
 	}
 
 	protected String image(TemplateParameter ele) {
 		return "/org.eclipse.uml2.uml.edit/icons/full/obj16/TemplateParameter.gif";
 	}
 
 	protected String text(TemplateParameter ele) {
		return text(ele.getParameteredElement());
 	}
 
 	protected String image(TemplateParameterType ele) {
 		return "/org.eclipse.ocl.edit/icons/full/obj16/TemplateParameterType.gif";
 	}
 
 	protected String image(TupleLiteralExp ele) {
 		return "/org.eclipse.ocl.edit/icons/full/obj16/TupleLiteralExp.gif";
 	}
 
 	protected String image(TupleLiteralPart ele) {
 		return "/org.eclipse.ocl.edit/icons/full/obj16/TupleLiteralPart.gif";
 	}
 
 	protected String text(TupleLiteralPart ele) {
 		StringBuilder s = new StringBuilder();
 		appendName(s, ele);
 		return s.toString();
 	}
 
 /*	protected String image(TuplePart ele) {
 		return "/org.eclipse.uml2.uml.edit/icons/full/obj16/Property.gif";
 	}
 
 	protected String text(TuplePart ele) {
 		StringBuilder s = new StringBuilder();
 		appendName(s, ele);
 		s.append(" : ");
 		appendType(s, ele.getType());
 //		appendMultiplicity(s, ele);
 		return s.toString();
 	} */
 
 	protected String image(TupleType ele) {
 		return "/org.eclipse.ocl.edit/icons/full/obj16/TupleType.gif";
 	}
 
 	protected String text(TupleType ele) {
 		StringBuilder s = new StringBuilder();
 		appendType(s, ele);
 		return s.toString();
 	}
 
 	protected String text(Type ele) {
 		StringBuilder s = new StringBuilder();
 		appendType(s, ele);
 		return s.toString();
 	}
 
 	protected String image(TypeExp ele) {
 		return "/org.eclipse.ocl.edit/icons/full/obj16/TypeExp.gif";
 	}
 
 //	protected String text(TypeRefCS ele) {
 //		StringBuilder s = new StringBuilder();
 //		appendType(s, ele);
 //		return s.toString();
 //	}
 
 	protected String image(UnlimitedNaturalLiteralExp ele) {
 		return "/org.eclipse.ocl.edit/icons/full/obj16/UnlimitedNaturalLiteralExp.gif";
 	}
 
 	protected String image(Variable ele) {
 		return "/org.eclipse.ocl.edit/icons/full/obj16/Variable.gif";
 	}
 
 	protected String text(Variable ele) {
 		assert ele != null;
 		return PrettyPrinter.print(ele);
 	}
 
 	protected String image(VariableExp ele) {
 		return "/org.eclipse.ocl.edit/icons/full/obj16/VariableExp.gif";
 	}
 
 	protected String image(VoidType ele) {
 		return "/org.eclipse.ocl.edit/icons/full/obj16/VoidType.gif";
 	}
 }
