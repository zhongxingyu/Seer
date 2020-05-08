 /**
  * <copyright>
  *
  * Copyright (c) 2011 E.D.Willink and others.
  * All rights reserved.   This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *   E.D. Willink - Initial API and implementation
  *
  * </copyright>
  *
  * $Id: PrettyPrintTypeVisitor.java,v 1.7 2011/05/22 21:06:19 ewillink Exp $
  */
 package org.eclipse.ocl.examples.pivot.prettyprint;
 
 import java.util.List;
 
 import org.eclipse.jdt.annotation.NonNull;
 import org.eclipse.jdt.annotation.Nullable;
 import org.eclipse.ocl.examples.domain.values.Unlimited;
 import org.eclipse.ocl.examples.pivot.Class;
 import org.eclipse.ocl.examples.pivot.CollectionType;
 import org.eclipse.ocl.examples.pivot.Iteration;
 import org.eclipse.ocl.examples.pivot.LambdaType;
 import org.eclipse.ocl.examples.pivot.NamedElement;
import org.eclipse.ocl.examples.pivot.OpaqueExpression;
 import org.eclipse.ocl.examples.pivot.Operation;
 import org.eclipse.ocl.examples.pivot.Parameter;
 import org.eclipse.ocl.examples.pivot.Property;
 import org.eclipse.ocl.examples.pivot.TemplateParameter;
 import org.eclipse.ocl.examples.pivot.TupleType;
 import org.eclipse.ocl.examples.pivot.Type;
 import org.eclipse.ocl.examples.pivot.TypedElement;
 import org.eclipse.ocl.examples.pivot.TypedMultiplicityElement;
 import org.eclipse.ocl.examples.pivot.util.AbstractExtendingVisitor;
 import org.eclipse.ocl.examples.pivot.util.Visitable;
import org.eclipse.ocl.examples.pivot.utilities.PivotUtil;
 
 /**
  * The PivotPrettyPrintVisitor supports pretty printing of a Pivot model elements.
  * PrettyPrintOptions may be used to configure the printing.
  */
 public class PivotPrettyPrintVisitor extends AbstractExtendingVisitor<Object,PrettyPrinter>
 {
 	public PivotPrettyPrintVisitor(@NonNull PrettyPrinter context) {
 		super(context);
 	}
 	
 	@Override
 	public Object safeVisit(@Nullable Visitable element) {
 		if (element == null) {
 			return null;
 		}
 		else {
 			try {
 				return element.accept(this);
 			}
 			catch (Exception e) {
 				context.append("<<" + e.getClass().getSimpleName() + ">>");
 				return null;
 			}
 		}
 	}
 	
 	@Override
 	public String toString() {
 		return context.toString();
 	}
 
 	@Override
 	public Object visitClass(@NonNull Class object) {
 		TemplateParameter owningTemplateParameter = object.getOwningTemplateParameter();
 		if (owningTemplateParameter != null) {
 			return owningTemplateParameter.accept(this);
 		}
 		return super.visitClass(object);
 	}
 
 	@Override
 	public Object visitCollectionType(@NonNull CollectionType object) {
 		context.appendName(object);
 		context.appendTemplateParameters(object);
 		context.appendTemplateBindings(object);
 		Number lower = object.getLower();
 		Number upper = object.getUpper();
 		if ((lower != null) && (upper != null) && ((lower.longValue() != 0) || !(upper instanceof  Unlimited))) {
 			context.appendMultiplicity(lower, upper);
 		}
 		return null;
 	}
 
 	@Override
 	public Object visitLambdaType(@NonNull LambdaType object) {
 //		appendParent(object.eContainer(), object, "::");
 		context.appendName(object, context.getRestrictedNames());
 		context.append(" ");
 		context.appendElement(object.getContextType());
 		context.append("(");
 		String prefix = ""; //$NON-NLS-1$
 		for (Type parameterType : object.getParameterType()) {
 			context.append(prefix);
 			context.appendElement(parameterType);
 //			appendMultiplicity(parameter);
 			prefix = ",";
 		}
 		context.append(") : ");
 		context.appendElement(object.getResultType());
 		return null;
 	}
 
 	@Override
 	public Object visitNamedElement(@NonNull NamedElement object) {
 		if (context.showNames()) {
 			context.appendParent(context.getScope(), object, "::");
 		}
 		context.appendName(object, context.getReservedNames());
 		return null;
 	}
 
 	@Override
	public Object visitOpaqueExpression(@NonNull OpaqueExpression object) {
		context.append(PivotUtil.getBody(object));
		return null;
	}

	@Override
 	public Object visitOperation(@NonNull Operation object) {
 		if (context.showNames()) {
 			context.appendParent(context.getScope(), object, "::");
 			context.appendName(object);
 		}
 		else {
 			context.appendName(object, context.getReservedNames());
 		}
 		context.appendTemplateParameters(object);
 		context.appendTemplateBindings(object);
 		if (context.showNames()) {
 			context.appendParameters(object, true);
 			Type type = object.getType();
 			if (type != null) {
 				context.append(" : ");
 				context.appendTypedMultiplicity(object);
 			}
 		}
 		else {
 			context.append("(");
 			String prefix = ""; //$NON-NLS-1$
 			if (object instanceof Iteration) {
 				Iteration iteration = (Iteration)object;
 				for (Parameter parameter : iteration.getOwnedIterator()) {
 					context.append(prefix);
 					context.appendTypedMultiplicity(parameter);
 					prefix = ",";
 				}
 				if (iteration.getOwnedAccumulator().size() > 0) {
 					prefix = ";";
 					for (Parameter parameter : iteration.getOwnedAccumulator()) {
 						context.append(prefix);
 						context.appendTypedMultiplicity(parameter);
 						prefix = ",";
 					}
 				}
 				prefix = "|";
 			}
 			for (Parameter parameter : object.getOwnedParameter()) {
 				context.append(prefix);
 				context.appendTypedMultiplicity(parameter);
 				prefix = ",";
 			}
 			context.append(")");
 		}
 		return null;
 	}
 
 	@Override
 	public Object visitTemplateParameter(@NonNull TemplateParameter object) {
 		if (context.showNames()) {
 //			context.appendParent(context.getScope(), object.getSignature(), "::");
 			context.appendName((NamedElement) object.getParameteredElement());
 		}
 		else {
 			context.appendName((NamedElement) object.getParameteredElement(), context.getRestrictedNames());
 		}
 		return null;
 	}
 
 	@Override
 	public Object visitTupleType(@NonNull TupleType object) {
 		if (context.showNames()) {
 			context.appendParent(context.getScope(), object, "::");
 		}
 		context.appendName(object);
 		List<Property> tupleParts = object.getOwnedAttribute();
 		if (!tupleParts.isEmpty()) {
 			context.append("(");
 			String prefix = ""; //$NON-NLS-1$
 			for (Property tuplePart : tupleParts) {
 				context.append(prefix);
 				if (context.showNames()) {
 					context.appendElement(tuplePart);
 				}
 				else {
 					context.appendName(tuplePart, context.getRestrictedNames());
 					context.append(" : ");
 					context.appendElement(tuplePart.getType());
 				}
 				prefix = ", ";
 			}
 			context.append(")");
 		}
 		return null;
 	}
 
 	@Override
 	public Object visitType(@NonNull Type object) {
 		if (context.showNames()) {
 			context.appendParent(context.getScope(), object, "::");
 		}
 		context.appendName(object);
 		context.appendTemplateParameters(object);
 		context.appendTemplateBindings(object);
 		return null;
 	}
 
 	@Override
 	public Object visitTypedElement(@NonNull TypedElement object) {
 		if (context.showNames()) {
 			context.appendParent(context.getScope(), object, "::");
 			context.appendName(object);
 			context.append(" : ");
 		}
 		context.appendElement(object.getType());
 		return null;
 	}
 
 	@Override
 	public Object visitTypedMultiplicityElement(@NonNull TypedMultiplicityElement object) {
 		if (context.showNames()) {
 			context.appendParent(context.getScope(), object, "::");
 			context.appendName(object);
 			context.append(" : ");
 		}
 		context.appendTypedMultiplicity(object);
 		return null;
 	}
 
 	public String visiting(@NonNull Visitable visitable) {
 		return visitable.getClass().getName();
 	}
 }
