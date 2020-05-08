 package org.ow2.mindEd.adl.editor.graphic.ui.custom.constraints;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.emf.common.util.EList;
 import org.eclipse.emf.validation.IValidationContext;
import org.ow2.mindEd.adl.ArchitectureDefinition;
 import org.ow2.mindEd.adl.Element;
 import org.ow2.mindEd.adl.SubComponentDefinition;
import org.ow2.mindEd.adl.ComponentKind;
 
 public class JavaConstraints {
 
 	public static boolean isCompositeSubComponent(SubComponentDefinition domainElement) {
 		return domainElement.getComponentKind().getValue() == ComponentKind.COMPOSITE_VALUE;
 	}
 
 	public static boolean isPrimitiveSubComponent(SubComponentDefinition domainElement) {
 		return domainElement.getComponentKind().getValue() == ComponentKind.PRIMITIVE_VALUE;
 	}
 	
 	public static boolean isUndefinedSubComponent(SubComponentDefinition domainElement) {
 		return (!isPrimitiveSubComponent(domainElement) && ! isCompositeSubComponent(domainElement));
 	}
 
 	public static IStatus subCompSameName(SubComponentDefinition context,
 			IValidationContext ctx) {
 
 		EList<Element> elements = context.getParentBody().getElements();
 		List<SubComponentDefinition> subComponents = new ArrayList<SubComponentDefinition>();
 		for (Element elem : elements) {
 			if (elem instanceof SubComponentDefinition &&
 					elem != context)
 				subComponents.add((SubComponentDefinition) elem);
 		}
 		
 		for (SubComponentDefinition subComp : subComponents) {
 			if (subComp.getName().equals(context.getName()))
 				return ctx.createFailureStatus(context);
 		}
 		
 		return ctx.createSuccessStatus();
 	}
 }
