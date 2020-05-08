 package org.eclipse.uml2.diagram.clazz.part;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 
 import org.eclipse.emf.common.util.EList;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.ecore.EStructuralFeature;
 import org.eclipse.emf.ecore.util.EcoreUtil;
 import org.eclipse.gmf.runtime.notation.View;
 import org.eclipse.uml2.diagram.clazz.edit.parts.*;
 import org.eclipse.uml2.diagram.clazz.providers.UMLElementTypes;
 import org.eclipse.uml2.diagram.common.conventions.AssociationEndConvention;
 import org.eclipse.uml2.diagram.common.genapi.IDiagramUpdater;
 import org.eclipse.uml2.diagram.common.links.PortOperationsExt;
 import org.eclipse.uml2.diagram.common.links.ProvidedInterfaceLink;
 import org.eclipse.uml2.uml.Association;
 import org.eclipse.uml2.uml.AssociationClass;
 import org.eclipse.uml2.uml.BehavioredClassifier;
 import org.eclipse.uml2.uml.Class;
 import org.eclipse.uml2.uml.Classifier;
 import org.eclipse.uml2.uml.CollaborationUse;
 import org.eclipse.uml2.uml.Comment;
 import org.eclipse.uml2.uml.Constraint;
 import org.eclipse.uml2.uml.DataType;
 import org.eclipse.uml2.uml.Dependency;
 import org.eclipse.uml2.uml.Element;
 import org.eclipse.uml2.uml.ElementImport;
 import org.eclipse.uml2.uml.Enumeration;
 import org.eclipse.uml2.uml.EnumerationLiteral;
 import org.eclipse.uml2.uml.Generalization;
 import org.eclipse.uml2.uml.GeneralizationSet;
 import org.eclipse.uml2.uml.InstanceSpecification;
 import org.eclipse.uml2.uml.InstanceValue;
 import org.eclipse.uml2.uml.Interface;
 import org.eclipse.uml2.uml.InterfaceRealization;
 import org.eclipse.uml2.uml.NamedElement;
 import org.eclipse.uml2.uml.Operation;
 import org.eclipse.uml2.uml.Package;
 import org.eclipse.uml2.uml.PackageableElement;
 import org.eclipse.uml2.uml.Port;
 import org.eclipse.uml2.uml.PrimitiveType;
 import org.eclipse.uml2.uml.Property;
 import org.eclipse.uml2.uml.Realization;
 import org.eclipse.uml2.uml.RedefinableTemplateSignature;
 import org.eclipse.uml2.uml.Relationship;
 import org.eclipse.uml2.uml.Slot;
 import org.eclipse.uml2.uml.TemplateBinding;
 import org.eclipse.uml2.uml.TemplateSignature;
 import org.eclipse.uml2.uml.TemplateableElement;
 import org.eclipse.uml2.uml.Type;
 import org.eclipse.uml2.uml.UMLPackage;
 import org.eclipse.uml2.uml.Usage;
 import org.eclipse.uml2.uml.ValueSpecification;
 
 /**
  * @generated
  */
 public class UMLDiagramUpdater {
 
 	/**
 	 * @generated
 	 */
 	public static boolean isShortcutOrphaned(View view) {
 		return !view.isSetElement() || view.getElement() == null || view.getElement().eIsProxy();
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.clazz.part.UMLNodeDescriptor> getSemanticChildren(View view) {
 		switch (UMLVisualIDRegistry.getVisualID(view)) {
 		case PackageEditPart.VISUAL_ID: {
 			//We have "dummy" TopLevelNode (with vid = org.eclipse.uml2.diagram.clazz.edit.parts.Package4EditPart.VISUAL_ID). 
 			//The only purpose for this node is to be a container for children (imports, etc)
 			//of the "main" diagram figure (that one shown as Canvas).
 			//Also we have modified the VisualIDRegistry#getNodeVisualID() to return
 			//VID = org.eclipse.uml2.diagram.clazz.edit.parts.Package4EditPart.VISUAL_ID, 
 			//for the case when top-level view is created for the same semantic element as the canvas view.
 
 			LinkedList<org.eclipse.uml2.diagram.clazz.part.UMLNodeDescriptor> resultAndHeader = new LinkedList<org.eclipse.uml2.diagram.clazz.part.UMLNodeDescriptor>();
 			resultAndHeader.add(new org.eclipse.uml2.diagram.clazz.part.UMLNodeDescriptor(view.getElement(), Package4EditPart.VISUAL_ID));
 			resultAndHeader.addAll(getPackage_1000SemanticChildren(view));
 			return resultAndHeader;
 		}
 		case Class2EditPart.VISUAL_ID:
 			return getClass_2001SemanticChildren(view);
 		case AssociationClass2EditPart.VISUAL_ID:
 			return getAssociationClass_2007SemanticChildren(view);
 		case DataType2EditPart.VISUAL_ID:
 			return getDataType_2004SemanticChildren(view);
 		case PrimitiveType2EditPart.VISUAL_ID:
 			return getPrimitiveType_2005SemanticChildren(view);
 		case Enumeration2EditPart.VISUAL_ID:
 			return getEnumeration_2003SemanticChildren(view);
 		case InstanceSpecification4EditPart.VISUAL_ID:
 			return getInstanceSpecification_2017SemanticChildren(view);
 		case Class5EditPart.VISUAL_ID:
 			return getClass_3033SemanticChildren(view);
 		case Enumeration3EditPart.VISUAL_ID:
 			return getEnumeration_3034SemanticChildren(view);
 		case DataType3EditPart.VISUAL_ID:
 			return getDataType_3036SemanticChildren(view);
 		case PrimitiveType3EditPart.VISUAL_ID:
 			return getPrimitiveType_3037SemanticChildren(view);
 		case PackagePackagesEditPart.VISUAL_ID:
 			return getPackagePackages_7010SemanticChildren(view);
 		case PackageClassifiersEditPart.VISUAL_ID:
 			return getPackageClassifiers_7011SemanticChildren(view);
 		case PackageOtherEditPart.VISUAL_ID:
 			return getPackageOther_7012SemanticChildren(view);
 		case ClassAttributesEditPart.VISUAL_ID:
 			return getClassAttributes_7001SemanticChildren(view);
 		case ClassOperationsEditPart.VISUAL_ID:
 			return getClassOperations_7002SemanticChildren(view);
 		case ClassClassesEditPart.VISUAL_ID:
 			return getClassClasses_7003SemanticChildren(view);
 		case AssociationClassAttributesEditPart.VISUAL_ID:
 			return getAssociationClassAttributes_7024SemanticChildren(view);
 		case AssociationClassOperationsEditPart.VISUAL_ID:
 			return getAssociationClassOperations_7025SemanticChildren(view);
 		case AssociationClassClassesEditPart.VISUAL_ID:
 			return getAssociationClassClasses_7026SemanticChildren(view);
 		case DataTypeAttributesEditPart.VISUAL_ID:
 			return getDataTypeAttributes_7017SemanticChildren(view);
 		case DataTypeOperationsEditPart.VISUAL_ID:
 			return getDataTypeOperations_7018SemanticChildren(view);
 		case PrimitiveTypeAttributesEditPart.VISUAL_ID:
 			return getPrimitiveTypeAttributes_7020SemanticChildren(view);
 		case PrimitiveTypeOperationsEditPart.VISUAL_ID:
 			return getPrimitiveTypeOperations_7021SemanticChildren(view);
 		case EnumerationLiteralsEditPart.VISUAL_ID:
 			return getEnumerationLiterals_7013SemanticChildren(view);
 		case EnumerationAttributesEditPart.VISUAL_ID:
 			return getEnumerationAttributes_7014SemanticChildren(view);
 		case EnumerationOperationsEditPart.VISUAL_ID:
 			return getEnumerationOperations_7015SemanticChildren(view);
 		case InstanceSpecificationSlotsEditPart.VISUAL_ID:
 			return getInstanceSpecificationSlots_7028SemanticChildren(view);
 		case InterfaceAttributesEditPart.VISUAL_ID:
 			return getInterfaceAttributes_7029SemanticChildren(view);
 		case InterfaceOperationsEditPart.VISUAL_ID:
 			return getInterfaceOperations_7030SemanticChildren(view);
 		case InterfaceClassesEditPart.VISUAL_ID:
 			return getInterfaceClasses_7031SemanticChildren(view);
 		case PackageImportsEditPart.VISUAL_ID:
 			return getPackageImports_7032SemanticChildren(view);
 		case PackageAsFrameContentsEditPart.VISUAL_ID:
 			return getPackageFramecontents_7033SemanticChildren(view);
 		case PackageAsFrameContents2EditPart.VISUAL_ID:
 			return getPackageFramecontents_7034SemanticChildren(view);
 		case ClassAttributes2EditPart.VISUAL_ID:
 			return getClassAttributes_7035SemanticChildren(view);
 		case ClassOperations2EditPart.VISUAL_ID:
 			return getClassOperations_7036SemanticChildren(view);
 		case ClassClasses2EditPart.VISUAL_ID:
 			return getClassClasses_7037SemanticChildren(view);
 		case EnumerationLiterals2EditPart.VISUAL_ID:
 			return getEnumerationLiterals_7038SemanticChildren(view);
 		case EnumerationAttributes2EditPart.VISUAL_ID:
 			return getEnumerationAttributes_7039SemanticChildren(view);
 		case EnumerationOperations2EditPart.VISUAL_ID:
 			return getEnumerationOperations_7040SemanticChildren(view);
 		case InstanceSpecificationSlots2EditPart.VISUAL_ID:
 			return getInstanceSpecificationSlots_7041SemanticChildren(view);
 		case DataTypeAttributes2EditPart.VISUAL_ID:
 			return getDataTypeAttributes_7042SemanticChildren(view);
 		case DataTypeOperations2EditPart.VISUAL_ID:
 			return getDataTypeOperations_7043SemanticChildren(view);
 		case PrimitiveTypeAttributes2EditPart.VISUAL_ID:
 			return getPrimitiveTypeAttributes_7044SemanticChildren(view);
 		case PrimitiveTypeOperations2EditPart.VISUAL_ID:
 			return getPrimitiveTypeOperations_7045SemanticChildren(view);
 		case InterfaceAttributes2EditPart.VISUAL_ID:
 			return getInterfaceAttributes_7047SemanticChildren(view);
 		case InterfaceOperations2EditPart.VISUAL_ID:
 			return getInterfaceOperations_7048SemanticChildren(view);
 		case InterfaceClasses2EditPart.VISUAL_ID:
 			return getInterfaceClasses_7049SemanticChildren(view);
 		case InstanceSpecificationValueEditPart.VISUAL_ID:
 			return getInstanceSpecificationValue_7046SemanticChildren(view);
 		}
 		return Collections.emptyList();
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.clazz.part.UMLNodeDescriptor> getClass_2001SemanticChildren(View view) {
 		if (!view.isSetElement()) {
 			return Collections.emptyList();
 		}
 		Class modelElement = (Class) view.getElement();
 		LinkedList<org.eclipse.uml2.diagram.clazz.part.UMLNodeDescriptor> result = new LinkedList<org.eclipse.uml2.diagram.clazz.part.UMLNodeDescriptor>();
 		for (Iterator<?> it = modelElement.getOwnedAttributes().iterator(); it.hasNext();) {
 			Property childElement = (Property) it.next();
 			int visualID = UMLVisualIDRegistry.getNodeVisualID(view, childElement);
 			if (visualID == PortEditPart.VISUAL_ID) {
 				result.add(new org.eclipse.uml2.diagram.clazz.part.UMLNodeDescriptor(childElement, visualID));
 				continue;
 			}
 		}
 		{
 			TemplateSignature childElement = modelElement.getOwnedTemplateSignature();
 			int visualID = UMLVisualIDRegistry.getNodeVisualID(view, childElement);
 			if (visualID == RedefinableTemplateSignatureEditPart.VISUAL_ID) {
 				result.add(new org.eclipse.uml2.diagram.clazz.part.UMLNodeDescriptor(childElement, visualID));
 			}
 		}
 		return result;
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.clazz.part.UMLNodeDescriptor> getAssociationClass_2007SemanticChildren(View view) {
 		if (!view.isSetElement()) {
 			return Collections.emptyList();
 		}
 		AssociationClass modelElement = (AssociationClass) view.getElement();
 		LinkedList<org.eclipse.uml2.diagram.clazz.part.UMLNodeDescriptor> result = new LinkedList<org.eclipse.uml2.diagram.clazz.part.UMLNodeDescriptor>();
 		{
 			TemplateSignature childElement = modelElement.getOwnedTemplateSignature();
 			int visualID = UMLVisualIDRegistry.getNodeVisualID(view, childElement);
 			if (visualID == RedefinableTemplateSignatureEditPart.VISUAL_ID) {
 				result.add(new org.eclipse.uml2.diagram.clazz.part.UMLNodeDescriptor(childElement, visualID));
 			}
 		}
 		return result;
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.clazz.part.UMLNodeDescriptor> getDataType_2004SemanticChildren(View view) {
 		if (!view.isSetElement()) {
 			return Collections.emptyList();
 		}
 		DataType modelElement = (DataType) view.getElement();
 		LinkedList<org.eclipse.uml2.diagram.clazz.part.UMLNodeDescriptor> result = new LinkedList<org.eclipse.uml2.diagram.clazz.part.UMLNodeDescriptor>();
 		{
 			TemplateSignature childElement = modelElement.getOwnedTemplateSignature();
 			int visualID = UMLVisualIDRegistry.getNodeVisualID(view, childElement);
 			if (visualID == RedefinableTemplateSignatureEditPart.VISUAL_ID) {
 				result.add(new org.eclipse.uml2.diagram.clazz.part.UMLNodeDescriptor(childElement, visualID));
 			}
 		}
 		return result;
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.clazz.part.UMLNodeDescriptor> getPrimitiveType_2005SemanticChildren(View view) {
 		if (!view.isSetElement()) {
 			return Collections.emptyList();
 		}
 		PrimitiveType modelElement = (PrimitiveType) view.getElement();
 		LinkedList<org.eclipse.uml2.diagram.clazz.part.UMLNodeDescriptor> result = new LinkedList<org.eclipse.uml2.diagram.clazz.part.UMLNodeDescriptor>();
 		{
 			TemplateSignature childElement = modelElement.getOwnedTemplateSignature();
 			int visualID = UMLVisualIDRegistry.getNodeVisualID(view, childElement);
 			if (visualID == RedefinableTemplateSignatureEditPart.VISUAL_ID) {
 				result.add(new org.eclipse.uml2.diagram.clazz.part.UMLNodeDescriptor(childElement, visualID));
 			}
 		}
 		return result;
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.clazz.part.UMLNodeDescriptor> getEnumeration_2003SemanticChildren(View view) {
 		if (!view.isSetElement()) {
 			return Collections.emptyList();
 		}
 		Enumeration modelElement = (Enumeration) view.getElement();
 		LinkedList<org.eclipse.uml2.diagram.clazz.part.UMLNodeDescriptor> result = new LinkedList<org.eclipse.uml2.diagram.clazz.part.UMLNodeDescriptor>();
 		{
 			TemplateSignature childElement = modelElement.getOwnedTemplateSignature();
 			int visualID = UMLVisualIDRegistry.getNodeVisualID(view, childElement);
 			if (visualID == RedefinableTemplateSignatureEditPart.VISUAL_ID) {
 				result.add(new org.eclipse.uml2.diagram.clazz.part.UMLNodeDescriptor(childElement, visualID));
 			}
 		}
 		return result;
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.clazz.part.UMLNodeDescriptor> getInstanceSpecification_2017SemanticChildren(View view) {
 		if (!view.isSetElement()) {
 			return Collections.emptyList();
 		}
 		InstanceSpecification modelElement = (InstanceSpecification) view.getElement();
 		LinkedList<org.eclipse.uml2.diagram.clazz.part.UMLNodeDescriptor> result = new LinkedList<org.eclipse.uml2.diagram.clazz.part.UMLNodeDescriptor>();
 		{
 			ValueSpecification childElement = modelElement.getSpecification();
 			int visualID = UMLVisualIDRegistry.getNodeVisualID(view, childElement);
 			if (visualID == LiteralIntegerEditPart.VISUAL_ID) {
 				result.add(new org.eclipse.uml2.diagram.clazz.part.UMLNodeDescriptor(childElement, visualID));
 			}
 		}
 		return result;
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.clazz.part.UMLNodeDescriptor> getClass_3033SemanticChildren(View view) {
 		if (!view.isSetElement()) {
 			return Collections.emptyList();
 		}
 		Class modelElement = (Class) view.getElement();
 		LinkedList<org.eclipse.uml2.diagram.clazz.part.UMLNodeDescriptor> result = new LinkedList<org.eclipse.uml2.diagram.clazz.part.UMLNodeDescriptor>();
 		for (Iterator<?> it = modelElement.getOwnedAttributes().iterator(); it.hasNext();) {
 			Property childElement = (Property) it.next();
 			int visualID = UMLVisualIDRegistry.getNodeVisualID(view, childElement);
 			if (visualID == PortEditPart.VISUAL_ID) {
 				result.add(new org.eclipse.uml2.diagram.clazz.part.UMLNodeDescriptor(childElement, visualID));
 				continue;
 			}
 		}
 		{
 			TemplateSignature childElement = modelElement.getOwnedTemplateSignature();
 			int visualID = UMLVisualIDRegistry.getNodeVisualID(view, childElement);
 			if (visualID == RedefinableTemplateSignatureEditPart.VISUAL_ID) {
 				result.add(new org.eclipse.uml2.diagram.clazz.part.UMLNodeDescriptor(childElement, visualID));
 			}
 		}
 		return result;
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.clazz.part.UMLNodeDescriptor> getEnumeration_3034SemanticChildren(View view) {
 		if (!view.isSetElement()) {
 			return Collections.emptyList();
 		}
 		Enumeration modelElement = (Enumeration) view.getElement();
 		LinkedList<org.eclipse.uml2.diagram.clazz.part.UMLNodeDescriptor> result = new LinkedList<org.eclipse.uml2.diagram.clazz.part.UMLNodeDescriptor>();
 		{
 			TemplateSignature childElement = modelElement.getOwnedTemplateSignature();
 			int visualID = UMLVisualIDRegistry.getNodeVisualID(view, childElement);
 			if (visualID == RedefinableTemplateSignatureEditPart.VISUAL_ID) {
 				result.add(new org.eclipse.uml2.diagram.clazz.part.UMLNodeDescriptor(childElement, visualID));
 			}
 		}
 		return result;
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.clazz.part.UMLNodeDescriptor> getDataType_3036SemanticChildren(View view) {
 		if (!view.isSetElement()) {
 			return Collections.emptyList();
 		}
 		DataType modelElement = (DataType) view.getElement();
 		LinkedList<org.eclipse.uml2.diagram.clazz.part.UMLNodeDescriptor> result = new LinkedList<org.eclipse.uml2.diagram.clazz.part.UMLNodeDescriptor>();
 		{
 			TemplateSignature childElement = modelElement.getOwnedTemplateSignature();
 			int visualID = UMLVisualIDRegistry.getNodeVisualID(view, childElement);
 			if (visualID == RedefinableTemplateSignatureEditPart.VISUAL_ID) {
 				result.add(new org.eclipse.uml2.diagram.clazz.part.UMLNodeDescriptor(childElement, visualID));
 			}
 		}
 		return result;
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.clazz.part.UMLNodeDescriptor> getPrimitiveType_3037SemanticChildren(View view) {
 		if (!view.isSetElement()) {
 			return Collections.emptyList();
 		}
 		PrimitiveType modelElement = (PrimitiveType) view.getElement();
 		LinkedList<org.eclipse.uml2.diagram.clazz.part.UMLNodeDescriptor> result = new LinkedList<org.eclipse.uml2.diagram.clazz.part.UMLNodeDescriptor>();
 		{
 			TemplateSignature childElement = modelElement.getOwnedTemplateSignature();
 			int visualID = UMLVisualIDRegistry.getNodeVisualID(view, childElement);
 			if (visualID == RedefinableTemplateSignatureEditPart.VISUAL_ID) {
 				result.add(new org.eclipse.uml2.diagram.clazz.part.UMLNodeDescriptor(childElement, visualID));
 			}
 		}
 		return result;
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.clazz.part.UMLNodeDescriptor> getPackagePackages_7010SemanticChildren(View view) {
 		if (false == view.eContainer() instanceof View) {
 			return Collections.emptyList();
 		}
 		View containerView = (View) view.eContainer();
 		if (!containerView.isSetElement()) {
 			return Collections.emptyList();
 		}
 		Package modelElement = (Package) containerView.getElement();
 		LinkedList<org.eclipse.uml2.diagram.clazz.part.UMLNodeDescriptor> result = new LinkedList<org.eclipse.uml2.diagram.clazz.part.UMLNodeDescriptor>();
 		for (Iterator<?> it = modelElement.getNestedPackages().iterator(); it.hasNext();) {
 			Package childElement = (Package) it.next();
 			int visualID = UMLVisualIDRegistry.getNodeVisualID(view, childElement);
 			if (visualID == Package3EditPart.VISUAL_ID) {
 				result.add(new org.eclipse.uml2.diagram.clazz.part.UMLNodeDescriptor(childElement, visualID));
 				continue;
 			}
 		}
 		return result;
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.clazz.part.UMLNodeDescriptor> getPackageClassifiers_7011SemanticChildren(View view) {
 		if (false == view.eContainer() instanceof View) {
 			return Collections.emptyList();
 		}
 		View containerView = (View) view.eContainer();
 		if (!containerView.isSetElement()) {
 			return Collections.emptyList();
 		}
 		Package modelElement = (Package) containerView.getElement();
 		LinkedList<org.eclipse.uml2.diagram.clazz.part.UMLNodeDescriptor> result = new LinkedList<org.eclipse.uml2.diagram.clazz.part.UMLNodeDescriptor>();
 		for (Iterator<?> it = modelElement.getOwnedTypes().iterator(); it.hasNext();) {
 			Type childElement = (Type) it.next();
 			int visualID = UMLVisualIDRegistry.getNodeVisualID(view, childElement);
 			if (visualID == ClassEditPart.VISUAL_ID) {
 				result.add(new org.eclipse.uml2.diagram.clazz.part.UMLNodeDescriptor(childElement, visualID));
 				continue;
 			}
 			if (visualID == DataTypeEditPart.VISUAL_ID) {
 				result.add(new org.eclipse.uml2.diagram.clazz.part.UMLNodeDescriptor(childElement, visualID));
 				continue;
 			}
 			if (visualID == PrimitiveTypeEditPart.VISUAL_ID) {
 				result.add(new org.eclipse.uml2.diagram.clazz.part.UMLNodeDescriptor(childElement, visualID));
 				continue;
 			}
 			if (visualID == EnumerationEditPart.VISUAL_ID) {
 				result.add(new org.eclipse.uml2.diagram.clazz.part.UMLNodeDescriptor(childElement, visualID));
 				continue;
 			}
 			if (visualID == AssociationClassEditPart.VISUAL_ID) {
 				result.add(new org.eclipse.uml2.diagram.clazz.part.UMLNodeDescriptor(childElement, visualID));
 				continue;
 			}
 			if (visualID == Interface3EditPart.VISUAL_ID) {
 				result.add(new org.eclipse.uml2.diagram.clazz.part.UMLNodeDescriptor(childElement, visualID));
 				continue;
 			}
 		}
 		return result;
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.clazz.part.UMLNodeDescriptor> getPackageOther_7012SemanticChildren(View view) {
 		if (false == view.eContainer() instanceof View) {
 			return Collections.emptyList();
 		}
 		View containerView = (View) view.eContainer();
 		if (!containerView.isSetElement()) {
 			return Collections.emptyList();
 		}
 		Package modelElement = (Package) containerView.getElement();
 		LinkedList<org.eclipse.uml2.diagram.clazz.part.UMLNodeDescriptor> result = new LinkedList<org.eclipse.uml2.diagram.clazz.part.UMLNodeDescriptor>();
 		for (Iterator<?> it = modelElement.getPackagedElements().iterator(); it.hasNext();) {
 			PackageableElement childElement = (PackageableElement) it.next();
 			int visualID = UMLVisualIDRegistry.getNodeVisualID(view, childElement);
 			if (visualID == InstanceSpecificationEditPart.VISUAL_ID) {
 				result.add(new org.eclipse.uml2.diagram.clazz.part.UMLNodeDescriptor(childElement, visualID));
 				continue;
 			}
 		}
 		return result;
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.clazz.part.UMLNodeDescriptor> getClassAttributes_7001SemanticChildren(View view) {
 		if (false == view.eContainer() instanceof View) {
 			return Collections.emptyList();
 		}
 		View containerView = (View) view.eContainer();
 		if (!containerView.isSetElement()) {
 			return Collections.emptyList();
 		}
 		Class modelElement = (Class) containerView.getElement();
 		LinkedList<org.eclipse.uml2.diagram.clazz.part.UMLNodeDescriptor> result = new LinkedList<org.eclipse.uml2.diagram.clazz.part.UMLNodeDescriptor>();
 		for (Iterator<?> it = modelElement.getOwnedAttributes().iterator(); it.hasNext();) {
 			Property childElement = (Property) it.next();
 			int visualID = UMLVisualIDRegistry.getNodeVisualID(view, childElement);
 			if (visualID == PropertyEditPart.VISUAL_ID) {
 				result.add(new org.eclipse.uml2.diagram.clazz.part.UMLNodeDescriptor(childElement, visualID));
 				continue;
 			}
 		}
 		return result;
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.clazz.part.UMLNodeDescriptor> getClassOperations_7002SemanticChildren(View view) {
 		if (false == view.eContainer() instanceof View) {
 			return Collections.emptyList();
 		}
 		View containerView = (View) view.eContainer();
 		if (!containerView.isSetElement()) {
 			return Collections.emptyList();
 		}
 		Class modelElement = (Class) containerView.getElement();
 		LinkedList<org.eclipse.uml2.diagram.clazz.part.UMLNodeDescriptor> result = new LinkedList<org.eclipse.uml2.diagram.clazz.part.UMLNodeDescriptor>();
 		for (Iterator<?> it = modelElement.getOwnedOperations().iterator(); it.hasNext();) {
 			Operation childElement = (Operation) it.next();
 			int visualID = UMLVisualIDRegistry.getNodeVisualID(view, childElement);
 			if (visualID == OperationEditPart.VISUAL_ID) {
 				result.add(new org.eclipse.uml2.diagram.clazz.part.UMLNodeDescriptor(childElement, visualID));
 				continue;
 			}
 		}
 		return result;
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.clazz.part.UMLNodeDescriptor> getClassClasses_7003SemanticChildren(View view) {
 		if (false == view.eContainer() instanceof View) {
 			return Collections.emptyList();
 		}
 		View containerView = (View) view.eContainer();
 		if (!containerView.isSetElement()) {
 			return Collections.emptyList();
 		}
 		Class modelElement = (Class) containerView.getElement();
 		LinkedList<org.eclipse.uml2.diagram.clazz.part.UMLNodeDescriptor> result = new LinkedList<org.eclipse.uml2.diagram.clazz.part.UMLNodeDescriptor>();
 		for (Iterator<?> it = modelElement.getNestedClassifiers().iterator(); it.hasNext();) {
 			Classifier childElement = (Classifier) it.next();
 			int visualID = UMLVisualIDRegistry.getNodeVisualID(view, childElement);
 			if (visualID == Class3EditPart.VISUAL_ID) {
 				result.add(new org.eclipse.uml2.diagram.clazz.part.UMLNodeDescriptor(childElement, visualID));
 				continue;
 			}
 		}
 		return result;
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.clazz.part.UMLNodeDescriptor> getAssociationClassAttributes_7024SemanticChildren(View view) {
 		if (false == view.eContainer() instanceof View) {
 			return Collections.emptyList();
 		}
 		View containerView = (View) view.eContainer();
 		if (!containerView.isSetElement()) {
 			return Collections.emptyList();
 		}
 		AssociationClass modelElement = (AssociationClass) containerView.getElement();
 		LinkedList<org.eclipse.uml2.diagram.clazz.part.UMLNodeDescriptor> result = new LinkedList<org.eclipse.uml2.diagram.clazz.part.UMLNodeDescriptor>();
 		for (Iterator<?> it = modelElement.getOwnedAttributes().iterator(); it.hasNext();) {
 			Property childElement = (Property) it.next();
 			int visualID = UMLVisualIDRegistry.getNodeVisualID(view, childElement);
 			if (visualID == Property2EditPart.VISUAL_ID) {
 				result.add(new org.eclipse.uml2.diagram.clazz.part.UMLNodeDescriptor(childElement, visualID));
 				continue;
 			}
 		}
 		return result;
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.clazz.part.UMLNodeDescriptor> getAssociationClassOperations_7025SemanticChildren(View view) {
 		if (false == view.eContainer() instanceof View) {
 			return Collections.emptyList();
 		}
 		View containerView = (View) view.eContainer();
 		if (!containerView.isSetElement()) {
 			return Collections.emptyList();
 		}
 		AssociationClass modelElement = (AssociationClass) containerView.getElement();
 		LinkedList<org.eclipse.uml2.diagram.clazz.part.UMLNodeDescriptor> result = new LinkedList<org.eclipse.uml2.diagram.clazz.part.UMLNodeDescriptor>();
 		for (Iterator<?> it = modelElement.getOwnedOperations().iterator(); it.hasNext();) {
 			Operation childElement = (Operation) it.next();
 			int visualID = UMLVisualIDRegistry.getNodeVisualID(view, childElement);
 			if (visualID == Operation2EditPart.VISUAL_ID) {
 				result.add(new org.eclipse.uml2.diagram.clazz.part.UMLNodeDescriptor(childElement, visualID));
 				continue;
 			}
 		}
 		return result;
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.clazz.part.UMLNodeDescriptor> getAssociationClassClasses_7026SemanticChildren(View view) {
 		if (false == view.eContainer() instanceof View) {
 			return Collections.emptyList();
 		}
 		View containerView = (View) view.eContainer();
 		if (!containerView.isSetElement()) {
 			return Collections.emptyList();
 		}
 		AssociationClass modelElement = (AssociationClass) containerView.getElement();
 		LinkedList<org.eclipse.uml2.diagram.clazz.part.UMLNodeDescriptor> result = new LinkedList<org.eclipse.uml2.diagram.clazz.part.UMLNodeDescriptor>();
 		for (Iterator<?> it = modelElement.getNestedClassifiers().iterator(); it.hasNext();) {
 			Classifier childElement = (Classifier) it.next();
 			int visualID = UMLVisualIDRegistry.getNodeVisualID(view, childElement);
 			if (visualID == Class3EditPart.VISUAL_ID) {
 				result.add(new org.eclipse.uml2.diagram.clazz.part.UMLNodeDescriptor(childElement, visualID));
 				continue;
 			}
 		}
 		return result;
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.clazz.part.UMLNodeDescriptor> getDataTypeAttributes_7017SemanticChildren(View view) {
 		if (false == view.eContainer() instanceof View) {
 			return Collections.emptyList();
 		}
 		View containerView = (View) view.eContainer();
 		if (!containerView.isSetElement()) {
 			return Collections.emptyList();
 		}
 		DataType modelElement = (DataType) containerView.getElement();
 		LinkedList<org.eclipse.uml2.diagram.clazz.part.UMLNodeDescriptor> result = new LinkedList<org.eclipse.uml2.diagram.clazz.part.UMLNodeDescriptor>();
 		for (Iterator<?> it = modelElement.getOwnedAttributes().iterator(); it.hasNext();) {
 			Property childElement = (Property) it.next();
 			int visualID = UMLVisualIDRegistry.getNodeVisualID(view, childElement);
 			if (visualID == Property3EditPart.VISUAL_ID) {
 				result.add(new org.eclipse.uml2.diagram.clazz.part.UMLNodeDescriptor(childElement, visualID));
 				continue;
 			}
 		}
 		return result;
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.clazz.part.UMLNodeDescriptor> getDataTypeOperations_7018SemanticChildren(View view) {
 		if (false == view.eContainer() instanceof View) {
 			return Collections.emptyList();
 		}
 		View containerView = (View) view.eContainer();
 		if (!containerView.isSetElement()) {
 			return Collections.emptyList();
 		}
 		DataType modelElement = (DataType) containerView.getElement();
 		LinkedList<org.eclipse.uml2.diagram.clazz.part.UMLNodeDescriptor> result = new LinkedList<org.eclipse.uml2.diagram.clazz.part.UMLNodeDescriptor>();
 		for (Iterator<?> it = modelElement.getOwnedOperations().iterator(); it.hasNext();) {
 			Operation childElement = (Operation) it.next();
 			int visualID = UMLVisualIDRegistry.getNodeVisualID(view, childElement);
 			if (visualID == Operation3EditPart.VISUAL_ID) {
 				result.add(new org.eclipse.uml2.diagram.clazz.part.UMLNodeDescriptor(childElement, visualID));
 				continue;
 			}
 		}
 		return result;
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.clazz.part.UMLNodeDescriptor> getPrimitiveTypeAttributes_7020SemanticChildren(View view) {
 		if (false == view.eContainer() instanceof View) {
 			return Collections.emptyList();
 		}
 		View containerView = (View) view.eContainer();
 		if (!containerView.isSetElement()) {
 			return Collections.emptyList();
 		}
 		PrimitiveType modelElement = (PrimitiveType) containerView.getElement();
 		LinkedList<org.eclipse.uml2.diagram.clazz.part.UMLNodeDescriptor> result = new LinkedList<org.eclipse.uml2.diagram.clazz.part.UMLNodeDescriptor>();
 		for (Iterator<?> it = modelElement.getOwnedAttributes().iterator(); it.hasNext();) {
 			Property childElement = (Property) it.next();
 			int visualID = UMLVisualIDRegistry.getNodeVisualID(view, childElement);
 			if (visualID == Property4EditPart.VISUAL_ID) {
 				result.add(new org.eclipse.uml2.diagram.clazz.part.UMLNodeDescriptor(childElement, visualID));
 				continue;
 			}
 		}
 		return result;
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.clazz.part.UMLNodeDescriptor> getPrimitiveTypeOperations_7021SemanticChildren(View view) {
 		if (false == view.eContainer() instanceof View) {
 			return Collections.emptyList();
 		}
 		View containerView = (View) view.eContainer();
 		if (!containerView.isSetElement()) {
 			return Collections.emptyList();
 		}
 		PrimitiveType modelElement = (PrimitiveType) containerView.getElement();
 		LinkedList<org.eclipse.uml2.diagram.clazz.part.UMLNodeDescriptor> result = new LinkedList<org.eclipse.uml2.diagram.clazz.part.UMLNodeDescriptor>();
 		for (Iterator<?> it = modelElement.getOwnedOperations().iterator(); it.hasNext();) {
 			Operation childElement = (Operation) it.next();
 			int visualID = UMLVisualIDRegistry.getNodeVisualID(view, childElement);
 			if (visualID == Operation4EditPart.VISUAL_ID) {
 				result.add(new org.eclipse.uml2.diagram.clazz.part.UMLNodeDescriptor(childElement, visualID));
 				continue;
 			}
 		}
 		return result;
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.clazz.part.UMLNodeDescriptor> getEnumerationLiterals_7013SemanticChildren(View view) {
 		if (false == view.eContainer() instanceof View) {
 			return Collections.emptyList();
 		}
 		View containerView = (View) view.eContainer();
 		if (!containerView.isSetElement()) {
 			return Collections.emptyList();
 		}
 		Enumeration modelElement = (Enumeration) containerView.getElement();
 		LinkedList<org.eclipse.uml2.diagram.clazz.part.UMLNodeDescriptor> result = new LinkedList<org.eclipse.uml2.diagram.clazz.part.UMLNodeDescriptor>();
 		for (Iterator<?> it = modelElement.getOwnedLiterals().iterator(); it.hasNext();) {
 			EnumerationLiteral childElement = (EnumerationLiteral) it.next();
 			int visualID = UMLVisualIDRegistry.getNodeVisualID(view, childElement);
 			if (visualID == EnumerationLiteralEditPart.VISUAL_ID) {
 				result.add(new org.eclipse.uml2.diagram.clazz.part.UMLNodeDescriptor(childElement, visualID));
 				continue;
 			}
 		}
 		return result;
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.clazz.part.UMLNodeDescriptor> getEnumerationAttributes_7014SemanticChildren(View view) {
 		if (false == view.eContainer() instanceof View) {
 			return Collections.emptyList();
 		}
 		View containerView = (View) view.eContainer();
 		if (!containerView.isSetElement()) {
 			return Collections.emptyList();
 		}
 		Enumeration modelElement = (Enumeration) containerView.getElement();
 		LinkedList<org.eclipse.uml2.diagram.clazz.part.UMLNodeDescriptor> result = new LinkedList<org.eclipse.uml2.diagram.clazz.part.UMLNodeDescriptor>();
 		for (Iterator<?> it = modelElement.getOwnedAttributes().iterator(); it.hasNext();) {
 			Property childElement = (Property) it.next();
 			int visualID = UMLVisualIDRegistry.getNodeVisualID(view, childElement);
 			if (visualID == Property5EditPart.VISUAL_ID) {
 				result.add(new org.eclipse.uml2.diagram.clazz.part.UMLNodeDescriptor(childElement, visualID));
 				continue;
 			}
 		}
 		return result;
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.clazz.part.UMLNodeDescriptor> getEnumerationOperations_7015SemanticChildren(View view) {
 		if (false == view.eContainer() instanceof View) {
 			return Collections.emptyList();
 		}
 		View containerView = (View) view.eContainer();
 		if (!containerView.isSetElement()) {
 			return Collections.emptyList();
 		}
 		Enumeration modelElement = (Enumeration) containerView.getElement();
 		LinkedList<org.eclipse.uml2.diagram.clazz.part.UMLNodeDescriptor> result = new LinkedList<org.eclipse.uml2.diagram.clazz.part.UMLNodeDescriptor>();
 		for (Iterator<?> it = modelElement.getOwnedOperations().iterator(); it.hasNext();) {
 			Operation childElement = (Operation) it.next();
 			int visualID = UMLVisualIDRegistry.getNodeVisualID(view, childElement);
 			if (visualID == Operation5EditPart.VISUAL_ID) {
 				result.add(new org.eclipse.uml2.diagram.clazz.part.UMLNodeDescriptor(childElement, visualID));
 				continue;
 			}
 		}
 		return result;
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.clazz.part.UMLNodeDescriptor> getInstanceSpecificationSlots_7028SemanticChildren(View view) {
 		if (false == view.eContainer() instanceof View) {
 			return Collections.emptyList();
 		}
 		View containerView = (View) view.eContainer();
 		if (!containerView.isSetElement()) {
 			return Collections.emptyList();
 		}
 		InstanceSpecification modelElement = (InstanceSpecification) containerView.getElement();
 		LinkedList<org.eclipse.uml2.diagram.clazz.part.UMLNodeDescriptor> result = new LinkedList<org.eclipse.uml2.diagram.clazz.part.UMLNodeDescriptor>();
 		for (Iterator<?> it = modelElement.getSlots().iterator(); it.hasNext();) {
 			Slot childElement = (Slot) it.next();
 			int visualID = UMLVisualIDRegistry.getNodeVisualID(view, childElement);
 			if (visualID == SlotEditPart.VISUAL_ID) {
 				result.add(new org.eclipse.uml2.diagram.clazz.part.UMLNodeDescriptor(childElement, visualID));
 				continue;
 			}
 		}
 		return result;
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.clazz.part.UMLNodeDescriptor> getInterfaceAttributes_7029SemanticChildren(View view) {
 		if (false == view.eContainer() instanceof View) {
 			return Collections.emptyList();
 		}
 		View containerView = (View) view.eContainer();
 		if (!containerView.isSetElement()) {
 			return Collections.emptyList();
 		}
 		Interface modelElement = (Interface) containerView.getElement();
 		LinkedList<org.eclipse.uml2.diagram.clazz.part.UMLNodeDescriptor> result = new LinkedList<org.eclipse.uml2.diagram.clazz.part.UMLNodeDescriptor>();
 		for (Iterator<?> it = modelElement.getOwnedAttributes().iterator(); it.hasNext();) {
 			Property childElement = (Property) it.next();
 			int visualID = UMLVisualIDRegistry.getNodeVisualID(view, childElement);
 			if (visualID == Property6EditPart.VISUAL_ID) {
 				result.add(new org.eclipse.uml2.diagram.clazz.part.UMLNodeDescriptor(childElement, visualID));
 				continue;
 			}
 		}
 		return result;
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.clazz.part.UMLNodeDescriptor> getInterfaceOperations_7030SemanticChildren(View view) {
 		if (false == view.eContainer() instanceof View) {
 			return Collections.emptyList();
 		}
 		View containerView = (View) view.eContainer();
 		if (!containerView.isSetElement()) {
 			return Collections.emptyList();
 		}
 		Interface modelElement = (Interface) containerView.getElement();
 		LinkedList<org.eclipse.uml2.diagram.clazz.part.UMLNodeDescriptor> result = new LinkedList<org.eclipse.uml2.diagram.clazz.part.UMLNodeDescriptor>();
 		for (Iterator<?> it = modelElement.getOwnedOperations().iterator(); it.hasNext();) {
 			Operation childElement = (Operation) it.next();
 			int visualID = UMLVisualIDRegistry.getNodeVisualID(view, childElement);
 			if (visualID == Operation6EditPart.VISUAL_ID) {
 				result.add(new org.eclipse.uml2.diagram.clazz.part.UMLNodeDescriptor(childElement, visualID));
 				continue;
 			}
 		}
 		return result;
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.clazz.part.UMLNodeDescriptor> getInterfaceClasses_7031SemanticChildren(View view) {
 		if (false == view.eContainer() instanceof View) {
 			return Collections.emptyList();
 		}
 		View containerView = (View) view.eContainer();
 		if (!containerView.isSetElement()) {
 			return Collections.emptyList();
 		}
 		Interface modelElement = (Interface) containerView.getElement();
 		LinkedList<org.eclipse.uml2.diagram.clazz.part.UMLNodeDescriptor> result = new LinkedList<org.eclipse.uml2.diagram.clazz.part.UMLNodeDescriptor>();
 		for (Iterator<?> it = modelElement.getNestedClassifiers().iterator(); it.hasNext();) {
 			Classifier childElement = (Classifier) it.next();
 			int visualID = UMLVisualIDRegistry.getNodeVisualID(view, childElement);
 			if (visualID == Class4EditPart.VISUAL_ID) {
 				result.add(new org.eclipse.uml2.diagram.clazz.part.UMLNodeDescriptor(childElement, visualID));
 				continue;
 			}
 		}
 		return result;
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.clazz.part.UMLNodeDescriptor> getPackageImports_7032SemanticChildren(View view) {
 		if (false == view.eContainer() instanceof View) {
 			return Collections.emptyList();
 		}
 		View containerView = (View) view.eContainer();
 		if (!containerView.isSetElement()) {
 			return Collections.emptyList();
 		}
 		Package modelElement = (Package) containerView.getElement();
 		LinkedList<org.eclipse.uml2.diagram.clazz.part.UMLNodeDescriptor> result = new LinkedList<org.eclipse.uml2.diagram.clazz.part.UMLNodeDescriptor>();
 		for (Iterator<?> it = modelElement.getElementImports().iterator(); it.hasNext();) {
 			ElementImport childElement = (ElementImport) it.next();
 			int visualID = UMLVisualIDRegistry.getNodeVisualID(view, childElement);
 			if (visualID == ElementImportEditPart.VISUAL_ID) {
 				result.add(new org.eclipse.uml2.diagram.clazz.part.UMLNodeDescriptor(childElement, visualID));
 				continue;
 			}
 		}
 		return result;
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.clazz.part.UMLNodeDescriptor> getPackageFramecontents_7033SemanticChildren(View view) {
 		if (false == view.eContainer() instanceof View) {
 			return Collections.emptyList();
 		}
 		View containerView = (View) view.eContainer();
 		if (!containerView.isSetElement()) {
 			return Collections.emptyList();
 		}
 		Package modelElement = (Package) containerView.getElement();
 		LinkedList<org.eclipse.uml2.diagram.clazz.part.UMLNodeDescriptor> result = new LinkedList<org.eclipse.uml2.diagram.clazz.part.UMLNodeDescriptor>();
 		for (Iterator<?> it = modelElement.getNestedPackages().iterator(); it.hasNext();) {
 			Package childElement = (Package) it.next();
 			int visualID = UMLVisualIDRegistry.getNodeVisualID(view, childElement);
 			if (visualID == Package6EditPart.VISUAL_ID) {
 				result.add(new org.eclipse.uml2.diagram.clazz.part.UMLNodeDescriptor(childElement, visualID));
 				continue;
 			}
 		}
 		for (Iterator<?> it = modelElement.getOwnedTypes().iterator(); it.hasNext();) {
 			Type childElement = (Type) it.next();
 			int visualID = UMLVisualIDRegistry.getNodeVisualID(view, childElement);
 			if (visualID == Class5EditPart.VISUAL_ID) {
 				result.add(new org.eclipse.uml2.diagram.clazz.part.UMLNodeDescriptor(childElement, visualID));
 				continue;
 			}
 			if (visualID == Enumeration3EditPart.VISUAL_ID) {
 				result.add(new org.eclipse.uml2.diagram.clazz.part.UMLNodeDescriptor(childElement, visualID));
 				continue;
 			}
 			if (visualID == DataType3EditPart.VISUAL_ID) {
 				result.add(new org.eclipse.uml2.diagram.clazz.part.UMLNodeDescriptor(childElement, visualID));
 				continue;
 			}
 			if (visualID == PrimitiveType3EditPart.VISUAL_ID) {
 				result.add(new org.eclipse.uml2.diagram.clazz.part.UMLNodeDescriptor(childElement, visualID));
 				continue;
 			}
 			if (visualID == Interface4EditPart.VISUAL_ID) {
 				result.add(new org.eclipse.uml2.diagram.clazz.part.UMLNodeDescriptor(childElement, visualID));
 				continue;
 			}
 			if (visualID == Interface5EditPart.VISUAL_ID) {
 				result.add(new org.eclipse.uml2.diagram.clazz.part.UMLNodeDescriptor(childElement, visualID));
 				continue;
 			}
 		}
 		for (Iterator<?> it = modelElement.getPackagedElements().iterator(); it.hasNext();) {
 			PackageableElement childElement = (PackageableElement) it.next();
 			int visualID = UMLVisualIDRegistry.getNodeVisualID(view, childElement);
 			if (visualID == InstanceSpecification3EditPart.VISUAL_ID) {
 				result.add(new org.eclipse.uml2.diagram.clazz.part.UMLNodeDescriptor(childElement, visualID));
 				continue;
 			}
 		}
 		return result;
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.clazz.part.UMLNodeDescriptor> getPackageFramecontents_7034SemanticChildren(View view) {
 		if (false == view.eContainer() instanceof View) {
 			return Collections.emptyList();
 		}
 		View containerView = (View) view.eContainer();
 		if (!containerView.isSetElement()) {
 			return Collections.emptyList();
 		}
 		Package modelElement = (Package) containerView.getElement();
 		LinkedList<org.eclipse.uml2.diagram.clazz.part.UMLNodeDescriptor> result = new LinkedList<org.eclipse.uml2.diagram.clazz.part.UMLNodeDescriptor>();
 		for (Iterator<?> it = modelElement.getNestedPackages().iterator(); it.hasNext();) {
 			Package childElement = (Package) it.next();
 			int visualID = UMLVisualIDRegistry.getNodeVisualID(view, childElement);
 			if (visualID == Package6EditPart.VISUAL_ID) {
 				result.add(new org.eclipse.uml2.diagram.clazz.part.UMLNodeDescriptor(childElement, visualID));
 				continue;
 			}
 		}
 		for (Iterator<?> it = modelElement.getOwnedTypes().iterator(); it.hasNext();) {
 			Type childElement = (Type) it.next();
 			int visualID = UMLVisualIDRegistry.getNodeVisualID(view, childElement);
 			if (visualID == Class5EditPart.VISUAL_ID) {
 				result.add(new org.eclipse.uml2.diagram.clazz.part.UMLNodeDescriptor(childElement, visualID));
 				continue;
 			}
 			if (visualID == Enumeration3EditPart.VISUAL_ID) {
 				result.add(new org.eclipse.uml2.diagram.clazz.part.UMLNodeDescriptor(childElement, visualID));
 				continue;
 			}
 			if (visualID == DataType3EditPart.VISUAL_ID) {
 				result.add(new org.eclipse.uml2.diagram.clazz.part.UMLNodeDescriptor(childElement, visualID));
 				continue;
 			}
 			if (visualID == PrimitiveType3EditPart.VISUAL_ID) {
 				result.add(new org.eclipse.uml2.diagram.clazz.part.UMLNodeDescriptor(childElement, visualID));
 				continue;
 			}
 			if (visualID == Interface4EditPart.VISUAL_ID) {
 				result.add(new org.eclipse.uml2.diagram.clazz.part.UMLNodeDescriptor(childElement, visualID));
 				continue;
 			}
 			if (visualID == Interface5EditPart.VISUAL_ID) {
 				result.add(new org.eclipse.uml2.diagram.clazz.part.UMLNodeDescriptor(childElement, visualID));
 				continue;
 			}
 		}
 		for (Iterator<?> it = modelElement.getPackagedElements().iterator(); it.hasNext();) {
 			PackageableElement childElement = (PackageableElement) it.next();
 			int visualID = UMLVisualIDRegistry.getNodeVisualID(view, childElement);
 			if (visualID == InstanceSpecification3EditPart.VISUAL_ID) {
 				result.add(new org.eclipse.uml2.diagram.clazz.part.UMLNodeDescriptor(childElement, visualID));
 				continue;
 			}
 		}
 		return result;
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.clazz.part.UMLNodeDescriptor> getClassAttributes_7035SemanticChildren(View view) {
 		if (false == view.eContainer() instanceof View) {
 			return Collections.emptyList();
 		}
 		View containerView = (View) view.eContainer();
 		if (!containerView.isSetElement()) {
 			return Collections.emptyList();
 		}
 		Class modelElement = (Class) containerView.getElement();
 		LinkedList<org.eclipse.uml2.diagram.clazz.part.UMLNodeDescriptor> result = new LinkedList<org.eclipse.uml2.diagram.clazz.part.UMLNodeDescriptor>();
 		for (Iterator<?> it = modelElement.getOwnedAttributes().iterator(); it.hasNext();) {
 			Property childElement = (Property) it.next();
 			int visualID = UMLVisualIDRegistry.getNodeVisualID(view, childElement);
 			if (visualID == PropertyEditPart.VISUAL_ID) {
 				result.add(new org.eclipse.uml2.diagram.clazz.part.UMLNodeDescriptor(childElement, visualID));
 				continue;
 			}
 		}
 		return result;
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.clazz.part.UMLNodeDescriptor> getClassOperations_7036SemanticChildren(View view) {
 		if (false == view.eContainer() instanceof View) {
 			return Collections.emptyList();
 		}
 		View containerView = (View) view.eContainer();
 		if (!containerView.isSetElement()) {
 			return Collections.emptyList();
 		}
 		Class modelElement = (Class) containerView.getElement();
 		LinkedList<org.eclipse.uml2.diagram.clazz.part.UMLNodeDescriptor> result = new LinkedList<org.eclipse.uml2.diagram.clazz.part.UMLNodeDescriptor>();
 		for (Iterator<?> it = modelElement.getOwnedOperations().iterator(); it.hasNext();) {
 			Operation childElement = (Operation) it.next();
 			int visualID = UMLVisualIDRegistry.getNodeVisualID(view, childElement);
 			if (visualID == OperationEditPart.VISUAL_ID) {
 				result.add(new org.eclipse.uml2.diagram.clazz.part.UMLNodeDescriptor(childElement, visualID));
 				continue;
 			}
 		}
 		return result;
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.clazz.part.UMLNodeDescriptor> getClassClasses_7037SemanticChildren(View view) {
 		if (false == view.eContainer() instanceof View) {
 			return Collections.emptyList();
 		}
 		View containerView = (View) view.eContainer();
 		if (!containerView.isSetElement()) {
 			return Collections.emptyList();
 		}
 		Class modelElement = (Class) containerView.getElement();
 		LinkedList<org.eclipse.uml2.diagram.clazz.part.UMLNodeDescriptor> result = new LinkedList<org.eclipse.uml2.diagram.clazz.part.UMLNodeDescriptor>();
 		for (Iterator<?> it = modelElement.getNestedClassifiers().iterator(); it.hasNext();) {
 			Classifier childElement = (Classifier) it.next();
 			int visualID = UMLVisualIDRegistry.getNodeVisualID(view, childElement);
 			if (visualID == Class3EditPart.VISUAL_ID) {
 				result.add(new org.eclipse.uml2.diagram.clazz.part.UMLNodeDescriptor(childElement, visualID));
 				continue;
 			}
 		}
 		return result;
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.clazz.part.UMLNodeDescriptor> getEnumerationLiterals_7038SemanticChildren(View view) {
 		if (false == view.eContainer() instanceof View) {
 			return Collections.emptyList();
 		}
 		View containerView = (View) view.eContainer();
 		if (!containerView.isSetElement()) {
 			return Collections.emptyList();
 		}
 		Enumeration modelElement = (Enumeration) containerView.getElement();
 		LinkedList<org.eclipse.uml2.diagram.clazz.part.UMLNodeDescriptor> result = new LinkedList<org.eclipse.uml2.diagram.clazz.part.UMLNodeDescriptor>();
 		for (Iterator<?> it = modelElement.getOwnedLiterals().iterator(); it.hasNext();) {
 			EnumerationLiteral childElement = (EnumerationLiteral) it.next();
 			int visualID = UMLVisualIDRegistry.getNodeVisualID(view, childElement);
 			if (visualID == EnumerationLiteralEditPart.VISUAL_ID) {
 				result.add(new org.eclipse.uml2.diagram.clazz.part.UMLNodeDescriptor(childElement, visualID));
 				continue;
 			}
 		}
 		return result;
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.clazz.part.UMLNodeDescriptor> getEnumerationAttributes_7039SemanticChildren(View view) {
 		if (false == view.eContainer() instanceof View) {
 			return Collections.emptyList();
 		}
 		View containerView = (View) view.eContainer();
 		if (!containerView.isSetElement()) {
 			return Collections.emptyList();
 		}
 		Enumeration modelElement = (Enumeration) containerView.getElement();
 		LinkedList<org.eclipse.uml2.diagram.clazz.part.UMLNodeDescriptor> result = new LinkedList<org.eclipse.uml2.diagram.clazz.part.UMLNodeDescriptor>();
 		for (Iterator<?> it = modelElement.getOwnedAttributes().iterator(); it.hasNext();) {
 			Property childElement = (Property) it.next();
 			int visualID = UMLVisualIDRegistry.getNodeVisualID(view, childElement);
 			if (visualID == Property5EditPart.VISUAL_ID) {
 				result.add(new org.eclipse.uml2.diagram.clazz.part.UMLNodeDescriptor(childElement, visualID));
 				continue;
 			}
 		}
 		return result;
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.clazz.part.UMLNodeDescriptor> getEnumerationOperations_7040SemanticChildren(View view) {
 		if (false == view.eContainer() instanceof View) {
 			return Collections.emptyList();
 		}
 		View containerView = (View) view.eContainer();
 		if (!containerView.isSetElement()) {
 			return Collections.emptyList();
 		}
 		Enumeration modelElement = (Enumeration) containerView.getElement();
 		LinkedList<org.eclipse.uml2.diagram.clazz.part.UMLNodeDescriptor> result = new LinkedList<org.eclipse.uml2.diagram.clazz.part.UMLNodeDescriptor>();
 		for (Iterator<?> it = modelElement.getOwnedOperations().iterator(); it.hasNext();) {
 			Operation childElement = (Operation) it.next();
 			int visualID = UMLVisualIDRegistry.getNodeVisualID(view, childElement);
 			if (visualID == Operation5EditPart.VISUAL_ID) {
 				result.add(new org.eclipse.uml2.diagram.clazz.part.UMLNodeDescriptor(childElement, visualID));
 				continue;
 			}
 		}
 		return result;
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.clazz.part.UMLNodeDescriptor> getInstanceSpecificationSlots_7041SemanticChildren(View view) {
 		if (false == view.eContainer() instanceof View) {
 			return Collections.emptyList();
 		}
 		View containerView = (View) view.eContainer();
 		if (!containerView.isSetElement()) {
 			return Collections.emptyList();
 		}
 		InstanceSpecification modelElement = (InstanceSpecification) containerView.getElement();
 		LinkedList<org.eclipse.uml2.diagram.clazz.part.UMLNodeDescriptor> result = new LinkedList<org.eclipse.uml2.diagram.clazz.part.UMLNodeDescriptor>();
 		for (Iterator<?> it = modelElement.getSlots().iterator(); it.hasNext();) {
 			Slot childElement = (Slot) it.next();
 			int visualID = UMLVisualIDRegistry.getNodeVisualID(view, childElement);
 			if (visualID == SlotEditPart.VISUAL_ID) {
 				result.add(new org.eclipse.uml2.diagram.clazz.part.UMLNodeDescriptor(childElement, visualID));
 				continue;
 			}
 		}
 		return result;
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.clazz.part.UMLNodeDescriptor> getDataTypeAttributes_7042SemanticChildren(View view) {
 		if (false == view.eContainer() instanceof View) {
 			return Collections.emptyList();
 		}
 		View containerView = (View) view.eContainer();
 		if (!containerView.isSetElement()) {
 			return Collections.emptyList();
 		}
 		DataType modelElement = (DataType) containerView.getElement();
 		LinkedList<org.eclipse.uml2.diagram.clazz.part.UMLNodeDescriptor> result = new LinkedList<org.eclipse.uml2.diagram.clazz.part.UMLNodeDescriptor>();
 		for (Iterator<?> it = modelElement.getOwnedAttributes().iterator(); it.hasNext();) {
 			Property childElement = (Property) it.next();
 			int visualID = UMLVisualIDRegistry.getNodeVisualID(view, childElement);
 			if (visualID == Property3EditPart.VISUAL_ID) {
 				result.add(new org.eclipse.uml2.diagram.clazz.part.UMLNodeDescriptor(childElement, visualID));
 				continue;
 			}
 		}
 		return result;
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.clazz.part.UMLNodeDescriptor> getDataTypeOperations_7043SemanticChildren(View view) {
 		if (false == view.eContainer() instanceof View) {
 			return Collections.emptyList();
 		}
 		View containerView = (View) view.eContainer();
 		if (!containerView.isSetElement()) {
 			return Collections.emptyList();
 		}
 		DataType modelElement = (DataType) containerView.getElement();
 		LinkedList<org.eclipse.uml2.diagram.clazz.part.UMLNodeDescriptor> result = new LinkedList<org.eclipse.uml2.diagram.clazz.part.UMLNodeDescriptor>();
 		for (Iterator<?> it = modelElement.getOwnedOperations().iterator(); it.hasNext();) {
 			Operation childElement = (Operation) it.next();
 			int visualID = UMLVisualIDRegistry.getNodeVisualID(view, childElement);
 			if (visualID == Operation3EditPart.VISUAL_ID) {
 				result.add(new org.eclipse.uml2.diagram.clazz.part.UMLNodeDescriptor(childElement, visualID));
 				continue;
 			}
 		}
 		return result;
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.clazz.part.UMLNodeDescriptor> getPrimitiveTypeAttributes_7044SemanticChildren(View view) {
 		if (false == view.eContainer() instanceof View) {
 			return Collections.emptyList();
 		}
 		View containerView = (View) view.eContainer();
 		if (!containerView.isSetElement()) {
 			return Collections.emptyList();
 		}
 		PrimitiveType modelElement = (PrimitiveType) containerView.getElement();
 		LinkedList<org.eclipse.uml2.diagram.clazz.part.UMLNodeDescriptor> result = new LinkedList<org.eclipse.uml2.diagram.clazz.part.UMLNodeDescriptor>();
 		for (Iterator<?> it = modelElement.getOwnedAttributes().iterator(); it.hasNext();) {
 			Property childElement = (Property) it.next();
 			int visualID = UMLVisualIDRegistry.getNodeVisualID(view, childElement);
 			if (visualID == Property4EditPart.VISUAL_ID) {
 				result.add(new org.eclipse.uml2.diagram.clazz.part.UMLNodeDescriptor(childElement, visualID));
 				continue;
 			}
 		}
 		return result;
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.clazz.part.UMLNodeDescriptor> getPrimitiveTypeOperations_7045SemanticChildren(View view) {
 		if (false == view.eContainer() instanceof View) {
 			return Collections.emptyList();
 		}
 		View containerView = (View) view.eContainer();
 		if (!containerView.isSetElement()) {
 			return Collections.emptyList();
 		}
 		PrimitiveType modelElement = (PrimitiveType) containerView.getElement();
 		LinkedList<org.eclipse.uml2.diagram.clazz.part.UMLNodeDescriptor> result = new LinkedList<org.eclipse.uml2.diagram.clazz.part.UMLNodeDescriptor>();
 		for (Iterator<?> it = modelElement.getOwnedOperations().iterator(); it.hasNext();) {
 			Operation childElement = (Operation) it.next();
 			int visualID = UMLVisualIDRegistry.getNodeVisualID(view, childElement);
 			if (visualID == Operation4EditPart.VISUAL_ID) {
 				result.add(new org.eclipse.uml2.diagram.clazz.part.UMLNodeDescriptor(childElement, visualID));
 				continue;
 			}
 		}
 		return result;
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.clazz.part.UMLNodeDescriptor> getInterfaceAttributes_7047SemanticChildren(View view) {
 		if (false == view.eContainer() instanceof View) {
 			return Collections.emptyList();
 		}
 		View containerView = (View) view.eContainer();
 		if (!containerView.isSetElement()) {
 			return Collections.emptyList();
 		}
 		Interface modelElement = (Interface) containerView.getElement();
 		LinkedList<org.eclipse.uml2.diagram.clazz.part.UMLNodeDescriptor> result = new LinkedList<org.eclipse.uml2.diagram.clazz.part.UMLNodeDescriptor>();
 		for (Iterator<?> it = modelElement.getOwnedAttributes().iterator(); it.hasNext();) {
 			Property childElement = (Property) it.next();
 			int visualID = UMLVisualIDRegistry.getNodeVisualID(view, childElement);
 			if (visualID == Property6EditPart.VISUAL_ID) {
 				result.add(new org.eclipse.uml2.diagram.clazz.part.UMLNodeDescriptor(childElement, visualID));
 				continue;
 			}
 		}
 		return result;
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.clazz.part.UMLNodeDescriptor> getInterfaceOperations_7048SemanticChildren(View view) {
 		if (false == view.eContainer() instanceof View) {
 			return Collections.emptyList();
 		}
 		View containerView = (View) view.eContainer();
 		if (!containerView.isSetElement()) {
 			return Collections.emptyList();
 		}
 		Interface modelElement = (Interface) containerView.getElement();
 		LinkedList<org.eclipse.uml2.diagram.clazz.part.UMLNodeDescriptor> result = new LinkedList<org.eclipse.uml2.diagram.clazz.part.UMLNodeDescriptor>();
 		for (Iterator<?> it = modelElement.getOwnedOperations().iterator(); it.hasNext();) {
 			Operation childElement = (Operation) it.next();
 			int visualID = UMLVisualIDRegistry.getNodeVisualID(view, childElement);
 			if (visualID == Operation6EditPart.VISUAL_ID) {
 				result.add(new org.eclipse.uml2.diagram.clazz.part.UMLNodeDescriptor(childElement, visualID));
 				continue;
 			}
 		}
 		return result;
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.clazz.part.UMLNodeDescriptor> getInterfaceClasses_7049SemanticChildren(View view) {
 		if (false == view.eContainer() instanceof View) {
 			return Collections.emptyList();
 		}
 		View containerView = (View) view.eContainer();
 		if (!containerView.isSetElement()) {
 			return Collections.emptyList();
 		}
 		Interface modelElement = (Interface) containerView.getElement();
 		LinkedList<org.eclipse.uml2.diagram.clazz.part.UMLNodeDescriptor> result = new LinkedList<org.eclipse.uml2.diagram.clazz.part.UMLNodeDescriptor>();
 		for (Iterator<?> it = modelElement.getNestedClassifiers().iterator(); it.hasNext();) {
 			Classifier childElement = (Classifier) it.next();
 			int visualID = UMLVisualIDRegistry.getNodeVisualID(view, childElement);
 			if (visualID == Class4EditPart.VISUAL_ID) {
 				result.add(new org.eclipse.uml2.diagram.clazz.part.UMLNodeDescriptor(childElement, visualID));
 				continue;
 			}
 		}
 		return result;
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.clazz.part.UMLNodeDescriptor> getInstanceSpecificationValue_7046SemanticChildren(View view) {
 		if (false == view.eContainer() instanceof View) {
 			return Collections.emptyList();
 		}
 		View containerView = (View) view.eContainer();
 		if (!containerView.isSetElement()) {
 			return Collections.emptyList();
 		}
 		InstanceSpecification modelElement = (InstanceSpecification) containerView.getElement();
 		LinkedList<org.eclipse.uml2.diagram.clazz.part.UMLNodeDescriptor> result = new LinkedList<org.eclipse.uml2.diagram.clazz.part.UMLNodeDescriptor>();
 		{
 			ValueSpecification childElement = modelElement.getSpecification();
 			int visualID = UMLVisualIDRegistry.getNodeVisualID(view, childElement);
 			if (visualID == LiteralStringEditPart.VISUAL_ID) {
 				result.add(new org.eclipse.uml2.diagram.clazz.part.UMLNodeDescriptor(childElement, visualID));
 			}
 			if (visualID == ExpressionEditPart.VISUAL_ID) {
 				result.add(new org.eclipse.uml2.diagram.clazz.part.UMLNodeDescriptor(childElement, visualID));
 			}
 		}
 		return result;
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.clazz.part.UMLNodeDescriptor> getPackage_1000SemanticChildrenGen(View view) {
 		if (!view.isSetElement()) {
 			return Collections.emptyList();
 		}
 		Package modelElement = (Package) view.getElement();
 		LinkedList<org.eclipse.uml2.diagram.clazz.part.UMLNodeDescriptor> result = new LinkedList<org.eclipse.uml2.diagram.clazz.part.UMLNodeDescriptor>();
 		for (Iterator<?> it = modelElement.getNestedPackages().iterator(); it.hasNext();) {
 			Package childElement = (Package) it.next();
 			int visualID = UMLVisualIDRegistry.getNodeVisualID(view, childElement);
 			if (visualID == Package2EditPart.VISUAL_ID) {
 				result.add(new org.eclipse.uml2.diagram.clazz.part.UMLNodeDescriptor(childElement, visualID));
 				continue;
 			}
 			if (visualID == PackageAsFrameEditPart.VISUAL_ID) {
 				result.add(new org.eclipse.uml2.diagram.clazz.part.UMLNodeDescriptor(childElement, visualID));
 				continue;
 			}
 		}
 		for (Iterator<?> it = modelElement.getOwnedTypes().iterator(); it.hasNext();) {
 			Type childElement = (Type) it.next();
 			int visualID = UMLVisualIDRegistry.getNodeVisualID(view, childElement);
 			if (visualID == Class2EditPart.VISUAL_ID) {
 				result.add(new org.eclipse.uml2.diagram.clazz.part.UMLNodeDescriptor(childElement, visualID));
 				continue;
 			}
 			if (visualID == AssociationClass2EditPart.VISUAL_ID) {
 				result.add(new org.eclipse.uml2.diagram.clazz.part.UMLNodeDescriptor(childElement, visualID));
 				result.add(new org.eclipse.uml2.diagram.clazz.part.UMLNodeDescriptor(childElement, AssociationClassRhombEditPart.VISUAL_ID));
 				continue;
 			}
 			if (visualID == DataType2EditPart.VISUAL_ID) {
 				result.add(new org.eclipse.uml2.diagram.clazz.part.UMLNodeDescriptor(childElement, visualID));
 				continue;
 			}
 			if (visualID == PrimitiveType2EditPart.VISUAL_ID) {
 				result.add(new org.eclipse.uml2.diagram.clazz.part.UMLNodeDescriptor(childElement, visualID));
 				continue;
 			}
 			if (visualID == Enumeration2EditPart.VISUAL_ID) {
 				result.add(new org.eclipse.uml2.diagram.clazz.part.UMLNodeDescriptor(childElement, visualID));
 				continue;
 			}
 			if (visualID == InterfaceEditPart.VISUAL_ID) {
 				result.add(new org.eclipse.uml2.diagram.clazz.part.UMLNodeDescriptor(childElement, visualID));
 				continue;
 			}
 			if (visualID == Interface2EditPart.VISUAL_ID) {
 				result.add(new org.eclipse.uml2.diagram.clazz.part.UMLNodeDescriptor(childElement, visualID));
 				continue;
 			}
 			if (visualID == AssociationClassRhombEditPart.VISUAL_ID) {
 				result.add(new org.eclipse.uml2.diagram.clazz.part.UMLNodeDescriptor(childElement, visualID));
 				result.add(new org.eclipse.uml2.diagram.clazz.part.UMLNodeDescriptor(childElement, AssociationClass2EditPart.VISUAL_ID));
 				continue;
 			}
 		}
 		for (Iterator<?> it = modelElement.getPackagedElements().iterator(); it.hasNext();) {
 			PackageableElement childElement = (PackageableElement) it.next();
 			int visualID = UMLVisualIDRegistry.getNodeVisualID(view, childElement);
 			if (visualID == ConstraintEditPart.VISUAL_ID) {
 				result.add(new org.eclipse.uml2.diagram.clazz.part.UMLNodeDescriptor(childElement, visualID));
 				continue;
 			}
 			if (visualID == InstanceSpecification2EditPart.VISUAL_ID) {
 				result.add(new org.eclipse.uml2.diagram.clazz.part.UMLNodeDescriptor(childElement, visualID));
 				continue;
 			}
 			if (visualID == DependencyEditPart.VISUAL_ID) {
 				result.add(new org.eclipse.uml2.diagram.clazz.part.UMLNodeDescriptor(childElement, visualID));
 				continue;
 			}
 			if (visualID == GeneralizationSetEditPart.VISUAL_ID) {
 				result.add(new org.eclipse.uml2.diagram.clazz.part.UMLNodeDescriptor(childElement, visualID));
 				continue;
 			}
 			if (visualID == InstanceSpecification4EditPart.VISUAL_ID) {
 				result.add(new org.eclipse.uml2.diagram.clazz.part.UMLNodeDescriptor(childElement, visualID));
 				continue;
 			}
 		}
 		for (Iterator<?> it = modelElement.getPackagedElements().iterator(); it.hasNext();) {
 			PackageableElement childElement = (PackageableElement) it.next();
 			int visualID = UMLVisualIDRegistry.getNodeVisualID(view, childElement);
 		}
 		for (Iterator<?> it = modelElement.getOwnedComments().iterator(); it.hasNext();) {
 			Comment childElement = (Comment) it.next();
 			int visualID = UMLVisualIDRegistry.getNodeVisualID(view, childElement);
 			if (visualID == CommentEditPart.VISUAL_ID) {
 				result.add(new org.eclipse.uml2.diagram.clazz.part.UMLNodeDescriptor(childElement, visualID));
 				continue;
 			}
 		}
 		return result;
 	}
 
 	/**
 	 * @generated NOT
 	 */
 	public static List<UMLNodeDescriptor> getPackage_1000SemanticChildren(View view) {
 		List<UMLNodeDescriptor> result = new ArrayList<UMLNodeDescriptor>();
 		result.addAll(getPackage_1000SemanticChildrenGen(view));
 		result.addAll(getPackage_1000SemanticChildren_ConstraintsAsOwnedRules(view));
 		return result;
 	}
 
 	public static List<UMLNodeDescriptor> getPackage_1000SemanticChildren_ConstraintsAsOwnedRules(View view) {
 		Package modelElement = (Package) view.getElement();
 		List<UMLNodeDescriptor> result = new LinkedList<UMLNodeDescriptor>();
 		for (Constraint next : modelElement.getOwnedRules()) {
 			int visualID = UMLVisualIDRegistry.getNodeVisualID(view, next);
 			if (visualID == ConstraintEditPart.VISUAL_ID) {
 				result.add(new UMLNodeDescriptor(next, visualID));
 				continue;
 			}
 		}
 		return result;
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor> getContainedLinks(View view) {
 		switch (UMLVisualIDRegistry.getVisualID(view)) {
 		case PackageEditPart.VISUAL_ID:
 			return getPackage_1000ContainedLinks(view);
 		case Package2EditPart.VISUAL_ID:
 			return getPackage_2002ContainedLinks(view);
 		case Class2EditPart.VISUAL_ID:
 			return getClass_2001ContainedLinks(view);
 		case AssociationClass2EditPart.VISUAL_ID:
 			return getAssociationClass_2007ContainedLinks(view);
 		case DataType2EditPart.VISUAL_ID:
 			return getDataType_2004ContainedLinks(view);
 		case PrimitiveType2EditPart.VISUAL_ID:
 			return getPrimitiveType_2005ContainedLinks(view);
 		case Enumeration2EditPart.VISUAL_ID:
 			return getEnumeration_2003ContainedLinks(view);
 		case InterfaceEditPart.VISUAL_ID:
 			return getInterface_2010ContainedLinks(view);
 		case ConstraintEditPart.VISUAL_ID:
 			return getConstraint_2006ContainedLinks(view);
 		case InstanceSpecification2EditPart.VISUAL_ID:
 			return getInstanceSpecification_2008ContainedLinks(view);
 		case DependencyEditPart.VISUAL_ID:
 			return getDependency_2009ContainedLinks(view);
 		case GeneralizationSetEditPart.VISUAL_ID:
 			return getGeneralizationSet_2012ContainedLinks(view);
 		case Interface2EditPart.VISUAL_ID:
 			return getInterface_2013ContainedLinks(view);
 		case Package4EditPart.VISUAL_ID:
 			return getPackage_2014ContainedLinks(view);
 		case AssociationClassRhombEditPart.VISUAL_ID:
 			return getAssociationClass_2015ContainedLinks(view);
 		case PackageAsFrameEditPart.VISUAL_ID:
 			return getPackage_2016ContainedLinks(view);
 		case InstanceSpecification4EditPart.VISUAL_ID:
 			return getInstanceSpecification_2017ContainedLinks(view);
 		case CommentEditPart.VISUAL_ID:
 			return getComment_2018ContainedLinks(view);
 		case Package3EditPart.VISUAL_ID:
 			return getPackage_3006ContainedLinks(view);
 		case ClassEditPart.VISUAL_ID:
 			return getClass_3007ContainedLinks(view);
 		case DataTypeEditPart.VISUAL_ID:
 			return getDataType_3008ContainedLinks(view);
 		case PrimitiveTypeEditPart.VISUAL_ID:
 			return getPrimitiveType_3009ContainedLinks(view);
 		case EnumerationEditPart.VISUAL_ID:
 			return getEnumeration_3011ContainedLinks(view);
 		case AssociationClassEditPart.VISUAL_ID:
 			return getAssociationClass_3012ContainedLinks(view);
 		case Interface3EditPart.VISUAL_ID:
 			return getInterface_3041ContainedLinks(view);
 		case InstanceSpecificationEditPart.VISUAL_ID:
 			return getInstanceSpecification_3013ContainedLinks(view);
 		case PropertyEditPart.VISUAL_ID:
 			return getProperty_3001ContainedLinks(view);
 		case OperationEditPart.VISUAL_ID:
 			return getOperation_3002ContainedLinks(view);
 		case Class3EditPart.VISUAL_ID:
 			return getClass_3003ContainedLinks(view);
 		case PortEditPart.VISUAL_ID:
 			return getPort_3025ContainedLinks(view);
 		case RedefinableTemplateSignatureEditPart.VISUAL_ID:
 			return getRedefinableTemplateSignature_3027ContainedLinks(view);
 		case Property2EditPart.VISUAL_ID:
 			return getProperty_3019ContainedLinks(view);
 		case Operation2EditPart.VISUAL_ID:
 			return getOperation_3020ContainedLinks(view);
 		case Property3EditPart.VISUAL_ID:
 			return getProperty_3014ContainedLinks(view);
 		case Operation3EditPart.VISUAL_ID:
 			return getOperation_3015ContainedLinks(view);
 		case Property4EditPart.VISUAL_ID:
 			return getProperty_3021ContainedLinks(view);
 		case Operation4EditPart.VISUAL_ID:
 			return getOperation_3022ContainedLinks(view);
 		case EnumerationLiteralEditPart.VISUAL_ID:
 			return getEnumerationLiteral_3016ContainedLinks(view);
 		case Property5EditPart.VISUAL_ID:
 			return getProperty_3023ContainedLinks(view);
 		case Operation5EditPart.VISUAL_ID:
 			return getOperation_3024ContainedLinks(view);
 		case SlotEditPart.VISUAL_ID:
 			return getSlot_3017ContainedLinks(view);
 		case Property6EditPart.VISUAL_ID:
 			return getProperty_3028ContainedLinks(view);
 		case Operation6EditPart.VISUAL_ID:
 			return getOperation_3029ContainedLinks(view);
 		case Class4EditPart.VISUAL_ID:
 			return getClass_3030ContainedLinks(view);
 		case ElementImportEditPart.VISUAL_ID:
 			return getElementImport_3031ContainedLinks(view);
 		case Package6EditPart.VISUAL_ID:
 			return getPackage_3032ContainedLinks(view);
 		case Class5EditPart.VISUAL_ID:
 			return getClass_3033ContainedLinks(view);
 		case Enumeration3EditPart.VISUAL_ID:
 			return getEnumeration_3034ContainedLinks(view);
 		case InstanceSpecification3EditPart.VISUAL_ID:
 			return getInstanceSpecification_3035ContainedLinks(view);
 		case DataType3EditPart.VISUAL_ID:
 			return getDataType_3036ContainedLinks(view);
 		case PrimitiveType3EditPart.VISUAL_ID:
 			return getPrimitiveType_3037ContainedLinks(view);
 		case Interface4EditPart.VISUAL_ID:
 			return getInterface_3042ContainedLinks(view);
 		case Interface5EditPart.VISUAL_ID:
 			return getInterface_3043ContainedLinks(view);
 		case LiteralStringEditPart.VISUAL_ID:
 			return getLiteralString_3038ContainedLinks(view);
 		case LiteralIntegerEditPart.VISUAL_ID:
 			return getLiteralInteger_3039ContainedLinks(view);
 		case ExpressionEditPart.VISUAL_ID:
 			return getExpression_3040ContainedLinks(view);
 		case GeneralizationEditPart.VISUAL_ID:
 			return getGeneralization_4001ContainedLinks(view);
 		case Dependency2EditPart.VISUAL_ID:
 			return getDependency_4002ContainedLinks(view);
 		case Property7EditPart.VISUAL_ID:
 			return getProperty_4003ContainedLinks(view);
 		case AssociationEditPart.VISUAL_ID:
 			return getAssociation_4005ContainedLinks(view);
 		case InterfaceRealizationEditPart.VISUAL_ID:
 			return getInterfaceRealization_4008ContainedLinks(view);
 		case RealizationEditPart.VISUAL_ID:
 			return getRealization_4010ContainedLinks(view);
 		case Generalization2EditPart.VISUAL_ID:
 			return getGeneralization_4011ContainedLinks(view);
 		case UsageEditPart.VISUAL_ID:
 			return getUsage_4013ContainedLinks(view);
 		case AssociationClassConnectorEditPart.VISUAL_ID:
 			return getAssociationClass_4014ContainedLinks(view);
 		case AssociationInstanceEditPart.VISUAL_ID:
 			return getSlot_4015ContainedLinks(view);
 		case TemplateBindingEditPart.VISUAL_ID:
 			return getTemplateBinding_4016ContainedLinks(view);
 		}
 		return Collections.emptyList();
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor> getIncomingLinks(View view) {
 		switch (UMLVisualIDRegistry.getVisualID(view)) {
 		case Package2EditPart.VISUAL_ID:
 			return getPackage_2002IncomingLinks(view);
 		case Class2EditPart.VISUAL_ID:
 			return getClass_2001IncomingLinks(view);
 		case AssociationClass2EditPart.VISUAL_ID:
 			return getAssociationClass_2007IncomingLinks(view);
 		case DataType2EditPart.VISUAL_ID:
 			return getDataType_2004IncomingLinks(view);
 		case PrimitiveType2EditPart.VISUAL_ID:
 			return getPrimitiveType_2005IncomingLinks(view);
 		case Enumeration2EditPart.VISUAL_ID:
 			return getEnumeration_2003IncomingLinks(view);
 		case InterfaceEditPart.VISUAL_ID:
 			return getInterface_2010IncomingLinks(view);
 		case ConstraintEditPart.VISUAL_ID:
 			return getConstraint_2006IncomingLinks(view);
 		case InstanceSpecification2EditPart.VISUAL_ID:
 			return getInstanceSpecification_2008IncomingLinks(view);
 		case DependencyEditPart.VISUAL_ID:
 			return getDependency_2009IncomingLinks(view);
 		case GeneralizationSetEditPart.VISUAL_ID:
 			return getGeneralizationSet_2012IncomingLinks(view);
 		case Interface2EditPart.VISUAL_ID:
 			return getInterface_2013IncomingLinks(view);
 		case Package4EditPart.VISUAL_ID:
 			return getPackage_2014IncomingLinks(view);
 		case AssociationClassRhombEditPart.VISUAL_ID:
 			return getAssociationClass_2015IncomingLinks(view);
 		case PackageAsFrameEditPart.VISUAL_ID:
 			return getPackage_2016IncomingLinks(view);
 		case InstanceSpecification4EditPart.VISUAL_ID:
 			return getInstanceSpecification_2017IncomingLinks(view);
 		case CommentEditPart.VISUAL_ID:
 			return getComment_2018IncomingLinks(view);
 		case Package3EditPart.VISUAL_ID:
 			return getPackage_3006IncomingLinks(view);
 		case ClassEditPart.VISUAL_ID:
 			return getClass_3007IncomingLinks(view);
 		case DataTypeEditPart.VISUAL_ID:
 			return getDataType_3008IncomingLinks(view);
 		case PrimitiveTypeEditPart.VISUAL_ID:
 			return getPrimitiveType_3009IncomingLinks(view);
 		case EnumerationEditPart.VISUAL_ID:
 			return getEnumeration_3011IncomingLinks(view);
 		case AssociationClassEditPart.VISUAL_ID:
 			return getAssociationClass_3012IncomingLinks(view);
 		case Interface3EditPart.VISUAL_ID:
 			return getInterface_3041IncomingLinks(view);
 		case InstanceSpecificationEditPart.VISUAL_ID:
 			return getInstanceSpecification_3013IncomingLinks(view);
 		case PropertyEditPart.VISUAL_ID:
 			return getProperty_3001IncomingLinks(view);
 		case OperationEditPart.VISUAL_ID:
 			return getOperation_3002IncomingLinks(view);
 		case Class3EditPart.VISUAL_ID:
 			return getClass_3003IncomingLinks(view);
 		case PortEditPart.VISUAL_ID:
 			return getPort_3025IncomingLinks(view);
 		case RedefinableTemplateSignatureEditPart.VISUAL_ID:
 			return getRedefinableTemplateSignature_3027IncomingLinks(view);
 		case Property2EditPart.VISUAL_ID:
 			return getProperty_3019IncomingLinks(view);
 		case Operation2EditPart.VISUAL_ID:
 			return getOperation_3020IncomingLinks(view);
 		case Property3EditPart.VISUAL_ID:
 			return getProperty_3014IncomingLinks(view);
 		case Operation3EditPart.VISUAL_ID:
 			return getOperation_3015IncomingLinks(view);
 		case Property4EditPart.VISUAL_ID:
 			return getProperty_3021IncomingLinks(view);
 		case Operation4EditPart.VISUAL_ID:
 			return getOperation_3022IncomingLinks(view);
 		case EnumerationLiteralEditPart.VISUAL_ID:
 			return getEnumerationLiteral_3016IncomingLinks(view);
 		case Property5EditPart.VISUAL_ID:
 			return getProperty_3023IncomingLinks(view);
 		case Operation5EditPart.VISUAL_ID:
 			return getOperation_3024IncomingLinks(view);
 		case SlotEditPart.VISUAL_ID:
 			return getSlot_3017IncomingLinks(view);
 		case Property6EditPart.VISUAL_ID:
 			return getProperty_3028IncomingLinks(view);
 		case Operation6EditPart.VISUAL_ID:
 			return getOperation_3029IncomingLinks(view);
 		case Class4EditPart.VISUAL_ID:
 			return getClass_3030IncomingLinks(view);
 		case ElementImportEditPart.VISUAL_ID:
 			return getElementImport_3031IncomingLinks(view);
 		case Package6EditPart.VISUAL_ID:
 			return getPackage_3032IncomingLinks(view);
 		case Class5EditPart.VISUAL_ID:
 			return getClass_3033IncomingLinks(view);
 		case Enumeration3EditPart.VISUAL_ID:
 			return getEnumeration_3034IncomingLinks(view);
 		case InstanceSpecification3EditPart.VISUAL_ID:
 			return getInstanceSpecification_3035IncomingLinks(view);
 		case DataType3EditPart.VISUAL_ID:
 			return getDataType_3036IncomingLinks(view);
 		case PrimitiveType3EditPart.VISUAL_ID:
 			return getPrimitiveType_3037IncomingLinks(view);
 		case Interface4EditPart.VISUAL_ID:
 			return getInterface_3042IncomingLinks(view);
 		case Interface5EditPart.VISUAL_ID:
 			return getInterface_3043IncomingLinks(view);
 		case LiteralStringEditPart.VISUAL_ID:
 			return getLiteralString_3038IncomingLinks(view);
 		case LiteralIntegerEditPart.VISUAL_ID:
 			return getLiteralInteger_3039IncomingLinks(view);
 		case ExpressionEditPart.VISUAL_ID:
 			return getExpression_3040IncomingLinks(view);
 		case GeneralizationEditPart.VISUAL_ID:
 			return getGeneralization_4001IncomingLinks(view);
 		case Dependency2EditPart.VISUAL_ID:
 			return getDependency_4002IncomingLinks(view);
 		case Property7EditPart.VISUAL_ID:
 			return getProperty_4003IncomingLinks(view);
 		case AssociationEditPart.VISUAL_ID:
 			return getAssociation_4005IncomingLinks(view);
 		case InterfaceRealizationEditPart.VISUAL_ID:
 			return getInterfaceRealization_4008IncomingLinks(view);
 		case RealizationEditPart.VISUAL_ID:
 			return getRealization_4010IncomingLinks(view);
 		case Generalization2EditPart.VISUAL_ID:
 			return getGeneralization_4011IncomingLinks(view);
 		case UsageEditPart.VISUAL_ID:
 			return getUsage_4013IncomingLinks(view);
 		case AssociationClassConnectorEditPart.VISUAL_ID:
 			return getAssociationClass_4014IncomingLinks(view);
 		case AssociationInstanceEditPart.VISUAL_ID:
 			return getSlot_4015IncomingLinks(view);
 		case TemplateBindingEditPart.VISUAL_ID:
 			return getTemplateBinding_4016IncomingLinks(view);
 		}
 		return Collections.emptyList();
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor> getOutgoingLinks(View view) {
 		switch (UMLVisualIDRegistry.getVisualID(view)) {
 		case Package2EditPart.VISUAL_ID:
 			return getPackage_2002OutgoingLinks(view);
 		case Class2EditPart.VISUAL_ID:
 			return getClass_2001OutgoingLinks(view);
 		case AssociationClass2EditPart.VISUAL_ID:
 			return getAssociationClass_2007OutgoingLinks(view);
 		case DataType2EditPart.VISUAL_ID:
 			return getDataType_2004OutgoingLinks(view);
 		case PrimitiveType2EditPart.VISUAL_ID:
 			return getPrimitiveType_2005OutgoingLinks(view);
 		case Enumeration2EditPart.VISUAL_ID:
 			return getEnumeration_2003OutgoingLinks(view);
 		case InterfaceEditPart.VISUAL_ID:
 			return getInterface_2010OutgoingLinks(view);
 		case ConstraintEditPart.VISUAL_ID:
 			return getConstraint_2006OutgoingLinks(view);
 		case InstanceSpecification2EditPart.VISUAL_ID:
 			return getInstanceSpecification_2008OutgoingLinks(view);
 		case DependencyEditPart.VISUAL_ID:
 			return getDependency_2009OutgoingLinks(view);
 		case GeneralizationSetEditPart.VISUAL_ID:
 			return getGeneralizationSet_2012OutgoingLinks(view);
 		case Interface2EditPart.VISUAL_ID:
 			return getInterface_2013OutgoingLinks(view);
 		case Package4EditPart.VISUAL_ID:
 			return getPackage_2014OutgoingLinks(view);
 		case AssociationClassRhombEditPart.VISUAL_ID:
 			return getAssociationClass_2015OutgoingLinks(view);
 		case PackageAsFrameEditPart.VISUAL_ID:
 			return getPackage_2016OutgoingLinks(view);
 		case InstanceSpecification4EditPart.VISUAL_ID:
 			return getInstanceSpecification_2017OutgoingLinks(view);
 		case CommentEditPart.VISUAL_ID:
 			return getComment_2018OutgoingLinks(view);
 		case Package3EditPart.VISUAL_ID:
 			return getPackage_3006OutgoingLinks(view);
 		case ClassEditPart.VISUAL_ID:
 			return getClass_3007OutgoingLinks(view);
 		case DataTypeEditPart.VISUAL_ID:
 			return getDataType_3008OutgoingLinks(view);
 		case PrimitiveTypeEditPart.VISUAL_ID:
 			return getPrimitiveType_3009OutgoingLinks(view);
 		case EnumerationEditPart.VISUAL_ID:
 			return getEnumeration_3011OutgoingLinks(view);
 		case AssociationClassEditPart.VISUAL_ID:
 			return getAssociationClass_3012OutgoingLinks(view);
 		case Interface3EditPart.VISUAL_ID:
 			return getInterface_3041OutgoingLinks(view);
 		case InstanceSpecificationEditPart.VISUAL_ID:
 			return getInstanceSpecification_3013OutgoingLinks(view);
 		case PropertyEditPart.VISUAL_ID:
 			return getProperty_3001OutgoingLinks(view);
 		case OperationEditPart.VISUAL_ID:
 			return getOperation_3002OutgoingLinks(view);
 		case Class3EditPart.VISUAL_ID:
 			return getClass_3003OutgoingLinks(view);
 		case PortEditPart.VISUAL_ID:
 			return getPort_3025OutgoingLinks(view);
 		case RedefinableTemplateSignatureEditPart.VISUAL_ID:
 			return getRedefinableTemplateSignature_3027OutgoingLinks(view);
 		case Property2EditPart.VISUAL_ID:
 			return getProperty_3019OutgoingLinks(view);
 		case Operation2EditPart.VISUAL_ID:
 			return getOperation_3020OutgoingLinks(view);
 		case Property3EditPart.VISUAL_ID:
 			return getProperty_3014OutgoingLinks(view);
 		case Operation3EditPart.VISUAL_ID:
 			return getOperation_3015OutgoingLinks(view);
 		case Property4EditPart.VISUAL_ID:
 			return getProperty_3021OutgoingLinks(view);
 		case Operation4EditPart.VISUAL_ID:
 			return getOperation_3022OutgoingLinks(view);
 		case EnumerationLiteralEditPart.VISUAL_ID:
 			return getEnumerationLiteral_3016OutgoingLinks(view);
 		case Property5EditPart.VISUAL_ID:
 			return getProperty_3023OutgoingLinks(view);
 		case Operation5EditPart.VISUAL_ID:
 			return getOperation_3024OutgoingLinks(view);
 		case SlotEditPart.VISUAL_ID:
 			return getSlot_3017OutgoingLinks(view);
 		case Property6EditPart.VISUAL_ID:
 			return getProperty_3028OutgoingLinks(view);
 		case Operation6EditPart.VISUAL_ID:
 			return getOperation_3029OutgoingLinks(view);
 		case Class4EditPart.VISUAL_ID:
 			return getClass_3030OutgoingLinks(view);
 		case ElementImportEditPart.VISUAL_ID:
 			return getElementImport_3031OutgoingLinks(view);
 		case Package6EditPart.VISUAL_ID:
 			return getPackage_3032OutgoingLinks(view);
 		case Class5EditPart.VISUAL_ID:
 			return getClass_3033OutgoingLinks(view);
 		case Enumeration3EditPart.VISUAL_ID:
 			return getEnumeration_3034OutgoingLinks(view);
 		case InstanceSpecification3EditPart.VISUAL_ID:
 			return getInstanceSpecification_3035OutgoingLinks(view);
 		case DataType3EditPart.VISUAL_ID:
 			return getDataType_3036OutgoingLinks(view);
 		case PrimitiveType3EditPart.VISUAL_ID:
 			return getPrimitiveType_3037OutgoingLinks(view);
 		case Interface4EditPart.VISUAL_ID:
 			return getInterface_3042OutgoingLinks(view);
 		case Interface5EditPart.VISUAL_ID:
 			return getInterface_3043OutgoingLinks(view);
 		case LiteralStringEditPart.VISUAL_ID:
 			return getLiteralString_3038OutgoingLinks(view);
 		case LiteralIntegerEditPart.VISUAL_ID:
 			return getLiteralInteger_3039OutgoingLinks(view);
 		case ExpressionEditPart.VISUAL_ID:
 			return getExpression_3040OutgoingLinks(view);
 		case GeneralizationEditPart.VISUAL_ID:
 			return getGeneralization_4001OutgoingLinks(view);
 		case Dependency2EditPart.VISUAL_ID:
 			return getDependency_4002OutgoingLinks(view);
 		case Property7EditPart.VISUAL_ID:
 			return getProperty_4003OutgoingLinks(view);
 		case AssociationEditPart.VISUAL_ID:
 			return getAssociation_4005OutgoingLinks(view);
 		case InterfaceRealizationEditPart.VISUAL_ID:
 			return getInterfaceRealization_4008OutgoingLinks(view);
 		case RealizationEditPart.VISUAL_ID:
 			return getRealization_4010OutgoingLinks(view);
 		case Generalization2EditPart.VISUAL_ID:
 			return getGeneralization_4011OutgoingLinks(view);
 		case UsageEditPart.VISUAL_ID:
 			return getUsage_4013OutgoingLinks(view);
 		case AssociationClassConnectorEditPart.VISUAL_ID:
 			return getAssociationClass_4014OutgoingLinks(view);
 		case AssociationInstanceEditPart.VISUAL_ID:
 			return getSlot_4015OutgoingLinks(view);
 		case TemplateBindingEditPart.VISUAL_ID:
 			return getTemplateBinding_4016OutgoingLinks(view);
 		}
 		return Collections.emptyList();
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor> getPackage_2002ContainedLinks(View view) {
 		Package modelElement = (Package) view.getElement();
 		LinkedList<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor> result = new LinkedList<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor>();
 		result.addAll(getContainedTypeModelFacetLinks_Dependency_4002(modelElement));
 		result.addAll(getContainedTypeModelFacetLinks_Association_4005(modelElement));
 		result.addAll(getContainedTypeModelFacetLinks_Realization_4010(modelElement));
 		result.addAll(getContainedTypeModelFacetLinks_Usage_4013(modelElement));
 		result.addAll(getContainedTypeModelFacetLinks_AssociationClass_4014(modelElement));
 		result.addAll(getContainedTypeModelFacetLinks_TemplateBinding_4016(modelElement));
 		return result;
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor> getClass_2001ContainedLinks(View view) {
 		Class modelElement = (Class) view.getElement();
 		LinkedList<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor> result = new LinkedList<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor>();
 		result.addAll(getContainedTypeModelFacetLinks_Generalization_4001(modelElement));
 		result.addAll(getContainedTypeModelFacetLinks_InterfaceRealization_4008(modelElement));
 		result.addAll(getContainedTypeModelFacetLinks_Generalization_4011(modelElement));
 		result.addAll(getContainedTypeModelFacetLinks_TemplateBinding_4016(modelElement));
 		return result;
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor> getAssociationClass_2007ContainedLinks(View view) {
 		AssociationClass modelElement = (AssociationClass) view.getElement();
 		LinkedList<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor> result = new LinkedList<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor>();
 		result.addAll(getContainedTypeModelFacetLinks_Generalization_4001(modelElement));
 		result.addAll(getContainedTypeModelFacetLinks_Property_4003(modelElement));
 		result.addAll(getContainedTypeModelFacetLinks_InterfaceRealization_4008(modelElement));
 		result.addAll(getContainedTypeModelFacetLinks_Generalization_4011(modelElement));
 		result.addAll(getContainedTypeModelFacetLinks_TemplateBinding_4016(modelElement));
 		return result;
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor> getDataType_2004ContainedLinks(View view) {
 		DataType modelElement = (DataType) view.getElement();
 		LinkedList<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor> result = new LinkedList<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor>();
 		result.addAll(getContainedTypeModelFacetLinks_Generalization_4001(modelElement));
 		result.addAll(getContainedTypeModelFacetLinks_Generalization_4011(modelElement));
 		result.addAll(getContainedTypeModelFacetLinks_TemplateBinding_4016(modelElement));
 		return result;
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor> getPrimitiveType_2005ContainedLinks(View view) {
 		PrimitiveType modelElement = (PrimitiveType) view.getElement();
 		LinkedList<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor> result = new LinkedList<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor>();
 		result.addAll(getContainedTypeModelFacetLinks_Generalization_4001(modelElement));
 		result.addAll(getContainedTypeModelFacetLinks_Generalization_4011(modelElement));
 		result.addAll(getContainedTypeModelFacetLinks_TemplateBinding_4016(modelElement));
 		return result;
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor> getEnumeration_2003ContainedLinks(View view) {
 		Enumeration modelElement = (Enumeration) view.getElement();
 		LinkedList<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor> result = new LinkedList<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor>();
 		result.addAll(getContainedTypeModelFacetLinks_Generalization_4001(modelElement));
 		result.addAll(getContainedTypeModelFacetLinks_Generalization_4011(modelElement));
 		result.addAll(getContainedTypeModelFacetLinks_TemplateBinding_4016(modelElement));
 		return result;
 	}
 
 	/**
 	 * @generated NOT
 	 */
 	public static List<UMLLinkDescriptor> getInterface_2010ContainedLinks(View view) {
 		List<UMLLinkDescriptor> result = new LinkedList<UMLLinkDescriptor>();
 		result.addAll(getInterface_2010ContainedLinksGen(view));
 		Interface modelElement = (Interface) view.getElement();
 		result.addAll(getContainedTypeModelFacetLinks_InterfaceRealization_4008_ForAllClassifiers(modelElement));
 		return result;
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor> getInterface_2010ContainedLinksGen(View view) {
 		Interface modelElement = (Interface) view.getElement();
 		LinkedList<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor> result = new LinkedList<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor>();
 		result.addAll(getContainedTypeModelFacetLinks_Generalization_4001(modelElement));
 		result.addAll(getContainedTypeModelFacetLinks_Generalization_4011(modelElement));
 		result.addAll(getContainedTypeModelFacetLinks_TemplateBinding_4016(modelElement));
 		return result;
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor> getConstraint_2006ContainedLinks(View view) {
 		Constraint modelElement = (Constraint) view.getElement();
 		LinkedList<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor> result = new LinkedList<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor>();
 		result.addAll(getOutgoingFeatureModelFacetLinks_Constraint_ConstrainedElement_4004(modelElement));
 		return result;
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor> getInstanceSpecification_2008ContainedLinks(View view) {
 		InstanceSpecification modelElement = (InstanceSpecification) view.getElement();
 		LinkedList<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor> result = new LinkedList<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor>();
 		result.addAll(getContainedTypeModelFacetLinks_Slot_4015(modelElement));
 		return result;
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor> getDependency_2009ContainedLinks(View view) {
 		Dependency modelElement = (Dependency) view.getElement();
 		LinkedList<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor> result = new LinkedList<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor>();
 		result.addAll(getOutgoingFeatureModelFacetLinks_Dependency_Supplier_4006(modelElement));
 		result.addAll(getOutgoingFeatureModelFacetLinks_Dependency_Client_4007(modelElement));
 		return result;
 	}
 
 	/**
 	 * @generated NOT
 	 */
 	public static List<UMLLinkDescriptor> getGeneralizationSet_2012ContainedLinks(View view) {
 		GeneralizationSet modelElement = (GeneralizationSet) view.getElement();
 		List<UMLLinkDescriptor> result = new LinkedList<UMLLinkDescriptor>();
 		result.addAll(getOutgoingFeatureModelFacetLinks_Generalization_General_4012(modelElement));
 		return result;
 	}
 
 	/**
 	 * @generated NOT
 	 */
 	public static List<UMLLinkDescriptor> getInterface_2013ContainedLinks(View view) {
 		List<UMLLinkDescriptor> result = new LinkedList<UMLLinkDescriptor>();
 		result.addAll(getInterface_2013ContainedLinksGen(view));
 		Interface modelElement = (Interface) view.getElement();
 		result.addAll(getContainedTypeModelFacetLinks_InterfaceRealization_4008_ForAllClassifiers(modelElement));
 		return result;
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor> getInterface_2013ContainedLinksGen(View view) {
 		Interface modelElement = (Interface) view.getElement();
 		LinkedList<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor> result = new LinkedList<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor>();
 		result.addAll(getContainedTypeModelFacetLinks_Generalization_4001(modelElement));
 		result.addAll(getContainedTypeModelFacetLinks_Generalization_4011(modelElement));
 		result.addAll(getContainedTypeModelFacetLinks_TemplateBinding_4016(modelElement));
 		return result;
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor> getPackage_2014ContainedLinks(View view) {
 		//no links to, from and inside the diagram header
 		return Collections.emptyList();
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor> getAssociationClass_2015ContainedLinks(View view) {
 		AssociationClass modelElement = (AssociationClass) view.getElement();
 		LinkedList<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor> result = new LinkedList<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor>();
 		result.addAll(getContainedTypeModelFacetLinks_Generalization_4001(modelElement));
 		result.addAll(getContainedTypeModelFacetLinks_Property_4003(modelElement));
 		result.addAll(getContainedTypeModelFacetLinks_InterfaceRealization_4008(modelElement));
 		result.addAll(getContainedTypeModelFacetLinks_Generalization_4011(modelElement));
 		result.addAll(getContainedTypeModelFacetLinks_TemplateBinding_4016(modelElement));
 		return result;
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor> getPackage_2016ContainedLinks(View view) {
 		Package modelElement = (Package) view.getElement();
 		LinkedList<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor> result = new LinkedList<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor>();
 		result.addAll(getContainedTypeModelFacetLinks_Dependency_4002(modelElement));
 		result.addAll(getContainedTypeModelFacetLinks_Association_4005(modelElement));
 		result.addAll(getContainedTypeModelFacetLinks_Realization_4010(modelElement));
 		result.addAll(getContainedTypeModelFacetLinks_Usage_4013(modelElement));
 		result.addAll(getContainedTypeModelFacetLinks_AssociationClass_4014(modelElement));
 		result.addAll(getContainedTypeModelFacetLinks_TemplateBinding_4016(modelElement));
 		return result;
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor> getInstanceSpecification_2017ContainedLinks(View view) {
 		InstanceSpecification modelElement = (InstanceSpecification) view.getElement();
 		LinkedList<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor> result = new LinkedList<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor>();
 		result.addAll(getContainedTypeModelFacetLinks_Slot_4015(modelElement));
 		return result;
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor> getComment_2018ContainedLinks(View view) {
 		Comment modelElement = (Comment) view.getElement();
 		LinkedList<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor> result = new LinkedList<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor>();
 		result.addAll(getOutgoingFeatureModelFacetLinks_Comment_AnnotatedElement_4019(modelElement));
 		return result;
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor> getPackage_3006ContainedLinks(View view) {
 		Package modelElement = (Package) view.getElement();
 		LinkedList<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor> result = new LinkedList<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor>();
 		result.addAll(getContainedTypeModelFacetLinks_Dependency_4002(modelElement));
 		result.addAll(getContainedTypeModelFacetLinks_Association_4005(modelElement));
 		result.addAll(getContainedTypeModelFacetLinks_Realization_4010(modelElement));
 		result.addAll(getContainedTypeModelFacetLinks_Usage_4013(modelElement));
 		result.addAll(getContainedTypeModelFacetLinks_AssociationClass_4014(modelElement));
 		result.addAll(getContainedTypeModelFacetLinks_TemplateBinding_4016(modelElement));
 		return result;
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor> getClass_3007ContainedLinks(View view) {
 		Class modelElement = (Class) view.getElement();
 		LinkedList<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor> result = new LinkedList<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor>();
 		result.addAll(getContainedTypeModelFacetLinks_Generalization_4001(modelElement));
 		result.addAll(getContainedTypeModelFacetLinks_InterfaceRealization_4008(modelElement));
 		result.addAll(getContainedTypeModelFacetLinks_Generalization_4011(modelElement));
 		result.addAll(getContainedTypeModelFacetLinks_TemplateBinding_4016(modelElement));
 		return result;
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor> getDataType_3008ContainedLinks(View view) {
 		DataType modelElement = (DataType) view.getElement();
 		LinkedList<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor> result = new LinkedList<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor>();
 		result.addAll(getContainedTypeModelFacetLinks_Generalization_4001(modelElement));
 		result.addAll(getContainedTypeModelFacetLinks_Generalization_4011(modelElement));
 		result.addAll(getContainedTypeModelFacetLinks_TemplateBinding_4016(modelElement));
 		return result;
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor> getPrimitiveType_3009ContainedLinks(View view) {
 		PrimitiveType modelElement = (PrimitiveType) view.getElement();
 		LinkedList<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor> result = new LinkedList<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor>();
 		result.addAll(getContainedTypeModelFacetLinks_Generalization_4001(modelElement));
 		result.addAll(getContainedTypeModelFacetLinks_Generalization_4011(modelElement));
 		result.addAll(getContainedTypeModelFacetLinks_TemplateBinding_4016(modelElement));
 		return result;
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor> getEnumeration_3011ContainedLinks(View view) {
 		Enumeration modelElement = (Enumeration) view.getElement();
 		LinkedList<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor> result = new LinkedList<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor>();
 		result.addAll(getContainedTypeModelFacetLinks_Generalization_4001(modelElement));
 		result.addAll(getContainedTypeModelFacetLinks_Generalization_4011(modelElement));
 		result.addAll(getContainedTypeModelFacetLinks_TemplateBinding_4016(modelElement));
 		return result;
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor> getAssociationClass_3012ContainedLinks(View view) {
 		AssociationClass modelElement = (AssociationClass) view.getElement();
 		LinkedList<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor> result = new LinkedList<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor>();
 		result.addAll(getContainedTypeModelFacetLinks_Generalization_4001(modelElement));
 		result.addAll(getContainedTypeModelFacetLinks_Property_4003(modelElement));
 		result.addAll(getContainedTypeModelFacetLinks_InterfaceRealization_4008(modelElement));
 		result.addAll(getContainedTypeModelFacetLinks_Generalization_4011(modelElement));
 		result.addAll(getContainedTypeModelFacetLinks_TemplateBinding_4016(modelElement));
 		return result;
 	}
 
 	/**
 	 * @generated NOT
 	 */
 	public static List<UMLLinkDescriptor> getInterface_3041ContainedLinks(View view) {
 		List<UMLLinkDescriptor> result = new LinkedList<UMLLinkDescriptor>();
 		result.addAll(getInterface_3041ContainedLinksGen(view));
 		Interface modelElement = (Interface) view.getElement();
 		result.addAll(getContainedTypeModelFacetLinks_InterfaceRealization_4008_ForAllClassifiers(modelElement));
 		return result;
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor> getInterface_3041ContainedLinksGen(View view) {
 		Interface modelElement = (Interface) view.getElement();
 		LinkedList<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor> result = new LinkedList<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor>();
 		result.addAll(getContainedTypeModelFacetLinks_Generalization_4001(modelElement));
 		result.addAll(getContainedTypeModelFacetLinks_Generalization_4011(modelElement));
 		result.addAll(getContainedTypeModelFacetLinks_TemplateBinding_4016(modelElement));
 		return result;
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor> getInstanceSpecification_3013ContainedLinks(View view) {
 		InstanceSpecification modelElement = (InstanceSpecification) view.getElement();
 		LinkedList<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor> result = new LinkedList<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor>();
 		result.addAll(getContainedTypeModelFacetLinks_Slot_4015(modelElement));
 		return result;
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor> getProperty_3001ContainedLinks(View view) {
 		return Collections.emptyList();
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor> getOperation_3002ContainedLinks(View view) {
 		Operation modelElement = (Operation) view.getElement();
 		LinkedList<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor> result = new LinkedList<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor>();
 		result.addAll(getContainedTypeModelFacetLinks_TemplateBinding_4016(modelElement));
 		return result;
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor> getClass_3003ContainedLinks(View view) {
 		Class modelElement = (Class) view.getElement();
 		LinkedList<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor> result = new LinkedList<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor>();
 		result.addAll(getContainedTypeModelFacetLinks_Generalization_4001(modelElement));
 		result.addAll(getContainedTypeModelFacetLinks_InterfaceRealization_4008(modelElement));
 		result.addAll(getContainedTypeModelFacetLinks_Generalization_4011(modelElement));
 		result.addAll(getContainedTypeModelFacetLinks_TemplateBinding_4016(modelElement));
 		return result;
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor> getPort_3025ContainedLinks(View view) {
 		Port modelElement = (Port) view.getElement();
 		LinkedList<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor> result = new LinkedList<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor>();
 		result.addAll(getOutgoingFeatureModelFacetLinks_Port_Provided_4017(modelElement));
 		result.addAll(getOutgoingFeatureModelFacetLinks_Port_Required_4018(modelElement));
 		return result;
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor> getRedefinableTemplateSignature_3027ContainedLinks(View view) {
 		return Collections.emptyList();
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor> getProperty_3019ContainedLinks(View view) {
 		return Collections.emptyList();
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor> getOperation_3020ContainedLinks(View view) {
 		Operation modelElement = (Operation) view.getElement();
 		LinkedList<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor> result = new LinkedList<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor>();
 		result.addAll(getContainedTypeModelFacetLinks_TemplateBinding_4016(modelElement));
 		return result;
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor> getProperty_3014ContainedLinks(View view) {
 		return Collections.emptyList();
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor> getOperation_3015ContainedLinks(View view) {
 		Operation modelElement = (Operation) view.getElement();
 		LinkedList<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor> result = new LinkedList<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor>();
 		result.addAll(getContainedTypeModelFacetLinks_TemplateBinding_4016(modelElement));
 		return result;
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor> getProperty_3021ContainedLinks(View view) {
 		return Collections.emptyList();
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor> getOperation_3022ContainedLinks(View view) {
 		Operation modelElement = (Operation) view.getElement();
 		LinkedList<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor> result = new LinkedList<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor>();
 		result.addAll(getContainedTypeModelFacetLinks_TemplateBinding_4016(modelElement));
 		return result;
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor> getEnumerationLiteral_3016ContainedLinks(View view) {
 		EnumerationLiteral modelElement = (EnumerationLiteral) view.getElement();
 		LinkedList<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor> result = new LinkedList<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor>();
 		result.addAll(getContainedTypeModelFacetLinks_Slot_4015(modelElement));
 		return result;
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor> getProperty_3023ContainedLinks(View view) {
 		return Collections.emptyList();
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor> getOperation_3024ContainedLinks(View view) {
 		Operation modelElement = (Operation) view.getElement();
 		LinkedList<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor> result = new LinkedList<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor>();
 		result.addAll(getContainedTypeModelFacetLinks_TemplateBinding_4016(modelElement));
 		return result;
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor> getSlot_3017ContainedLinks(View view) {
 		return Collections.emptyList();
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor> getProperty_3028ContainedLinks(View view) {
 		return Collections.emptyList();
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor> getOperation_3029ContainedLinks(View view) {
 		Operation modelElement = (Operation) view.getElement();
 		LinkedList<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor> result = new LinkedList<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor>();
 		result.addAll(getContainedTypeModelFacetLinks_TemplateBinding_4016(modelElement));
 		return result;
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor> getClass_3030ContainedLinks(View view) {
 		Class modelElement = (Class) view.getElement();
 		LinkedList<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor> result = new LinkedList<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor>();
 		result.addAll(getContainedTypeModelFacetLinks_Generalization_4001(modelElement));
 		result.addAll(getContainedTypeModelFacetLinks_InterfaceRealization_4008(modelElement));
 		result.addAll(getContainedTypeModelFacetLinks_Generalization_4011(modelElement));
 		result.addAll(getContainedTypeModelFacetLinks_TemplateBinding_4016(modelElement));
 		return result;
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor> getElementImport_3031ContainedLinks(View view) {
 		return Collections.emptyList();
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor> getPackage_3032ContainedLinks(View view) {
 		Package modelElement = (Package) view.getElement();
 		LinkedList<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor> result = new LinkedList<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor>();
 		result.addAll(getContainedTypeModelFacetLinks_Dependency_4002(modelElement));
 		result.addAll(getContainedTypeModelFacetLinks_Association_4005(modelElement));
 		result.addAll(getContainedTypeModelFacetLinks_Realization_4010(modelElement));
 		result.addAll(getContainedTypeModelFacetLinks_Usage_4013(modelElement));
 		result.addAll(getContainedTypeModelFacetLinks_AssociationClass_4014(modelElement));
 		result.addAll(getContainedTypeModelFacetLinks_TemplateBinding_4016(modelElement));
 		return result;
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor> getClass_3033ContainedLinks(View view) {
 		Class modelElement = (Class) view.getElement();
 		LinkedList<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor> result = new LinkedList<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor>();
 		result.addAll(getContainedTypeModelFacetLinks_Generalization_4001(modelElement));
 		result.addAll(getContainedTypeModelFacetLinks_InterfaceRealization_4008(modelElement));
 		result.addAll(getContainedTypeModelFacetLinks_Generalization_4011(modelElement));
 		result.addAll(getContainedTypeModelFacetLinks_TemplateBinding_4016(modelElement));
 		return result;
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor> getEnumeration_3034ContainedLinks(View view) {
 		Enumeration modelElement = (Enumeration) view.getElement();
 		LinkedList<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor> result = new LinkedList<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor>();
 		result.addAll(getContainedTypeModelFacetLinks_Generalization_4001(modelElement));
 		result.addAll(getContainedTypeModelFacetLinks_Generalization_4011(modelElement));
 		result.addAll(getContainedTypeModelFacetLinks_TemplateBinding_4016(modelElement));
 		return result;
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor> getInstanceSpecification_3035ContainedLinks(View view) {
 		InstanceSpecification modelElement = (InstanceSpecification) view.getElement();
 		LinkedList<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor> result = new LinkedList<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor>();
 		result.addAll(getContainedTypeModelFacetLinks_Slot_4015(modelElement));
 		return result;
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor> getDataType_3036ContainedLinks(View view) {
 		DataType modelElement = (DataType) view.getElement();
 		LinkedList<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor> result = new LinkedList<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor>();
 		result.addAll(getContainedTypeModelFacetLinks_Generalization_4001(modelElement));
 		result.addAll(getContainedTypeModelFacetLinks_Generalization_4011(modelElement));
 		result.addAll(getContainedTypeModelFacetLinks_TemplateBinding_4016(modelElement));
 		return result;
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor> getPrimitiveType_3037ContainedLinks(View view) {
 		PrimitiveType modelElement = (PrimitiveType) view.getElement();
 		LinkedList<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor> result = new LinkedList<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor>();
 		result.addAll(getContainedTypeModelFacetLinks_Generalization_4001(modelElement));
 		result.addAll(getContainedTypeModelFacetLinks_Generalization_4011(modelElement));
 		result.addAll(getContainedTypeModelFacetLinks_TemplateBinding_4016(modelElement));
 		return result;
 	}
 
 	/**
 	 * @generated NOT
 	 */
 	public static List<UMLLinkDescriptor> getInterface_3042ContainedLinks(View view) {
 		List<UMLLinkDescriptor> result = new LinkedList<UMLLinkDescriptor>();
 		result.addAll(getInterface_3042ContainedLinksGen(view));
 		Interface modelElement = (Interface) view.getElement();
 		result.addAll(getContainedTypeModelFacetLinks_InterfaceRealization_4008_ForAllClassifiers(modelElement));
 		return result;
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor> getInterface_3042ContainedLinksGen(View view) {
 		Interface modelElement = (Interface) view.getElement();
 		LinkedList<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor> result = new LinkedList<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor>();
 		result.addAll(getContainedTypeModelFacetLinks_Generalization_4001(modelElement));
 		result.addAll(getContainedTypeModelFacetLinks_Generalization_4011(modelElement));
 		result.addAll(getContainedTypeModelFacetLinks_TemplateBinding_4016(modelElement));
 		return result;
 	}
 
 	/**
 	 * @generated NOT
 	 */
 	public static List<UMLLinkDescriptor> getInterface_3043ContainedLinks(View view) {
 		List<UMLLinkDescriptor> result = new LinkedList<UMLLinkDescriptor>();
 		result.addAll(getInterface_3043ContainedLinksGen(view));
 		Interface modelElement = (Interface) view.getElement();
 		result.addAll(getContainedTypeModelFacetLinks_InterfaceRealization_4008_ForAllClassifiers(modelElement));
 		return result;
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor> getInterface_3043ContainedLinksGen(View view) {
 		Interface modelElement = (Interface) view.getElement();
 		LinkedList<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor> result = new LinkedList<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor>();
 		result.addAll(getContainedTypeModelFacetLinks_Generalization_4001(modelElement));
 		result.addAll(getContainedTypeModelFacetLinks_Generalization_4011(modelElement));
 		result.addAll(getContainedTypeModelFacetLinks_TemplateBinding_4016(modelElement));
 		return result;
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor> getLiteralString_3038ContainedLinks(View view) {
 		return Collections.emptyList();
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor> getLiteralInteger_3039ContainedLinks(View view) {
 		return Collections.emptyList();
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor> getExpression_3040ContainedLinks(View view) {
 		return Collections.emptyList();
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor> getGeneralization_4001ContainedLinks(View view) {
 		return Collections.emptyList();
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor> getDependency_4002ContainedLinks(View view) {
 		return Collections.emptyList();
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor> getProperty_4003ContainedLinks(View view) {
 		return Collections.emptyList();
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor> getAssociation_4005ContainedLinks(View view) {
 		return Collections.emptyList();
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor> getInterfaceRealization_4008ContainedLinks(View view) {
 		return Collections.emptyList();
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor> getRealization_4010ContainedLinks(View view) {
 		return Collections.emptyList();
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor> getGeneralization_4011ContainedLinks(View view) {
 		return Collections.emptyList();
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor> getUsage_4013ContainedLinks(View view) {
 		return Collections.emptyList();
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor> getAssociationClass_4014ContainedLinks(View view) {
 		return Collections.emptyList();
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor> getSlot_4015ContainedLinks(View view) {
 		return Collections.emptyList();
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor> getTemplateBinding_4016ContainedLinks(View view) {
 		return Collections.emptyList();
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor> getPackage_2002IncomingLinks(View view) {
 		Package modelElement = (Package) view.getElement();
 		Map<EObject, Collection<EStructuralFeature.Setting>> crossReferences = EcoreUtil.CrossReferencer.find(view.eResource().getResourceSet().getResources());
 		LinkedList<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor> result = new LinkedList<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor>();
 		result.addAll(getIncomingTypeModelFacetLinks_Dependency_4002(modelElement, crossReferences));
 		result.addAll(getIncomingFeatureModelFacetLinks_Constraint_ConstrainedElement_4004(modelElement, crossReferences));
 		result.addAll(getIncomingFeatureModelFacetLinks_Dependency_Supplier_4006(modelElement, crossReferences));
 		result.addAll(getIncomingFeatureModelFacetLinks_Dependency_Client_4007(modelElement, crossReferences));
 		result.addAll(getIncomingTypeModelFacetLinks_Realization_4010(modelElement, crossReferences));
 		result.addAll(getIncomingTypeModelFacetLinks_Usage_4013(modelElement, crossReferences));
 		result.addAll(getIncomingTypeModelFacetLinks_TemplateBinding_4016(modelElement, crossReferences));
 		result.addAll(getIncomingFeatureModelFacetLinks_Comment_AnnotatedElement_4019(modelElement, crossReferences));
 		return result;
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor> getClass_2001IncomingLinks(View view) {
 		Class modelElement = (Class) view.getElement();
 		Map<EObject, Collection<EStructuralFeature.Setting>> crossReferences = EcoreUtil.CrossReferencer.find(view.eResource().getResourceSet().getResources());
 		LinkedList<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor> result = new LinkedList<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor>();
 		result.addAll(getIncomingTypeModelFacetLinks_Generalization_4001(modelElement, crossReferences));
 		result.addAll(getIncomingTypeModelFacetLinks_Dependency_4002(modelElement, crossReferences));
 		result.addAll(getIncomingTypeModelFacetLinks_Property_4003(modelElement, crossReferences));
 		result.addAll(getIncomingFeatureModelFacetLinks_Constraint_ConstrainedElement_4004(modelElement, crossReferences));
 		result.addAll(getIncomingTypeModelFacetLinks_Association_4005(modelElement, crossReferences));
 		result.addAll(getIncomingFeatureModelFacetLinks_Dependency_Supplier_4006(modelElement, crossReferences));
 		result.addAll(getIncomingFeatureModelFacetLinks_Dependency_Client_4007(modelElement, crossReferences));
 		result.addAll(getIncomingTypeModelFacetLinks_Realization_4010(modelElement, crossReferences));
 		result.addAll(getIncomingFeatureModelFacetLinks_Generalization_General_4012(modelElement, crossReferences));
 		result.addAll(getIncomingTypeModelFacetLinks_Usage_4013(modelElement, crossReferences));
 		result.addAll(getIncomingTypeModelFacetLinks_TemplateBinding_4016(modelElement, crossReferences));
 		result.addAll(getIncomingFeatureModelFacetLinks_Comment_AnnotatedElement_4019(modelElement, crossReferences));
 		return result;
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor> getAssociationClass_2007IncomingLinks(View view) {
 		AssociationClass modelElement = (AssociationClass) view.getElement();
 		Map<EObject, Collection<EStructuralFeature.Setting>> crossReferences = EcoreUtil.CrossReferencer.find(view.eResource().getResourceSet().getResources());
 		LinkedList<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor> result = new LinkedList<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor>();
 		result.addAll(getIncomingTypeModelFacetLinks_Generalization_4001(modelElement, crossReferences));
 		result.addAll(getIncomingTypeModelFacetLinks_Dependency_4002(modelElement, crossReferences));
 		result.addAll(getIncomingTypeModelFacetLinks_Property_4003(modelElement, crossReferences));
 		result.addAll(getIncomingFeatureModelFacetLinks_Constraint_ConstrainedElement_4004(modelElement, crossReferences));
 		result.addAll(getIncomingTypeModelFacetLinks_Association_4005(modelElement, crossReferences));
 		result.addAll(getIncomingFeatureModelFacetLinks_Dependency_Supplier_4006(modelElement, crossReferences));
 		result.addAll(getIncomingFeatureModelFacetLinks_Dependency_Client_4007(modelElement, crossReferences));
 		result.addAll(getIncomingTypeModelFacetLinks_Realization_4010(modelElement, crossReferences));
 		result.addAll(getIncomingFeatureModelFacetLinks_Generalization_General_4012(modelElement, crossReferences));
 		result.addAll(getIncomingTypeModelFacetLinks_Usage_4013(modelElement, crossReferences));
 		result.addAll(getIncomingTypeModelFacetLinks_TemplateBinding_4016(modelElement, crossReferences));
 		result.addAll(getIncomingFeatureModelFacetLinks_Comment_AnnotatedElement_4019(modelElement, crossReferences));
 		return result;
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor> getDataType_2004IncomingLinks(View view) {
 		DataType modelElement = (DataType) view.getElement();
 		Map<EObject, Collection<EStructuralFeature.Setting>> crossReferences = EcoreUtil.CrossReferencer.find(view.eResource().getResourceSet().getResources());
 		LinkedList<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor> result = new LinkedList<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor>();
 		result.addAll(getIncomingTypeModelFacetLinks_Generalization_4001(modelElement, crossReferences));
 		result.addAll(getIncomingTypeModelFacetLinks_Dependency_4002(modelElement, crossReferences));
 		result.addAll(getIncomingTypeModelFacetLinks_Property_4003(modelElement, crossReferences));
 		result.addAll(getIncomingFeatureModelFacetLinks_Constraint_ConstrainedElement_4004(modelElement, crossReferences));
 		result.addAll(getIncomingTypeModelFacetLinks_Association_4005(modelElement, crossReferences));
 		result.addAll(getIncomingFeatureModelFacetLinks_Dependency_Supplier_4006(modelElement, crossReferences));
 		result.addAll(getIncomingFeatureModelFacetLinks_Dependency_Client_4007(modelElement, crossReferences));
 		result.addAll(getIncomingTypeModelFacetLinks_Realization_4010(modelElement, crossReferences));
 		result.addAll(getIncomingFeatureModelFacetLinks_Generalization_General_4012(modelElement, crossReferences));
 		result.addAll(getIncomingTypeModelFacetLinks_Usage_4013(modelElement, crossReferences));
 		result.addAll(getIncomingTypeModelFacetLinks_TemplateBinding_4016(modelElement, crossReferences));
 		result.addAll(getIncomingFeatureModelFacetLinks_Comment_AnnotatedElement_4019(modelElement, crossReferences));
 		return result;
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor> getPrimitiveType_2005IncomingLinks(View view) {
 		PrimitiveType modelElement = (PrimitiveType) view.getElement();
 		Map<EObject, Collection<EStructuralFeature.Setting>> crossReferences = EcoreUtil.CrossReferencer.find(view.eResource().getResourceSet().getResources());
 		LinkedList<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor> result = new LinkedList<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor>();
 		result.addAll(getIncomingTypeModelFacetLinks_Generalization_4001(modelElement, crossReferences));
 		result.addAll(getIncomingTypeModelFacetLinks_Dependency_4002(modelElement, crossReferences));
 		result.addAll(getIncomingTypeModelFacetLinks_Property_4003(modelElement, crossReferences));
 		result.addAll(getIncomingFeatureModelFacetLinks_Constraint_ConstrainedElement_4004(modelElement, crossReferences));
 		result.addAll(getIncomingTypeModelFacetLinks_Association_4005(modelElement, crossReferences));
 		result.addAll(getIncomingFeatureModelFacetLinks_Dependency_Supplier_4006(modelElement, crossReferences));
 		result.addAll(getIncomingFeatureModelFacetLinks_Dependency_Client_4007(modelElement, crossReferences));
 		result.addAll(getIncomingTypeModelFacetLinks_Realization_4010(modelElement, crossReferences));
 		result.addAll(getIncomingFeatureModelFacetLinks_Generalization_General_4012(modelElement, crossReferences));
 		result.addAll(getIncomingTypeModelFacetLinks_Usage_4013(modelElement, crossReferences));
 		result.addAll(getIncomingTypeModelFacetLinks_TemplateBinding_4016(modelElement, crossReferences));
 		result.addAll(getIncomingFeatureModelFacetLinks_Comment_AnnotatedElement_4019(modelElement, crossReferences));
 		return result;
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor> getEnumeration_2003IncomingLinks(View view) {
 		Enumeration modelElement = (Enumeration) view.getElement();
 		Map<EObject, Collection<EStructuralFeature.Setting>> crossReferences = EcoreUtil.CrossReferencer.find(view.eResource().getResourceSet().getResources());
 		LinkedList<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor> result = new LinkedList<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor>();
 		result.addAll(getIncomingTypeModelFacetLinks_Generalization_4001(modelElement, crossReferences));
 		result.addAll(getIncomingTypeModelFacetLinks_Dependency_4002(modelElement, crossReferences));
 		result.addAll(getIncomingTypeModelFacetLinks_Property_4003(modelElement, crossReferences));
 		result.addAll(getIncomingFeatureModelFacetLinks_Constraint_ConstrainedElement_4004(modelElement, crossReferences));
 		result.addAll(getIncomingTypeModelFacetLinks_Association_4005(modelElement, crossReferences));
 		result.addAll(getIncomingFeatureModelFacetLinks_Dependency_Supplier_4006(modelElement, crossReferences));
 		result.addAll(getIncomingFeatureModelFacetLinks_Dependency_Client_4007(modelElement, crossReferences));
 		result.addAll(getIncomingTypeModelFacetLinks_Realization_4010(modelElement, crossReferences));
 		result.addAll(getIncomingFeatureModelFacetLinks_Generalization_General_4012(modelElement, crossReferences));
 		result.addAll(getIncomingTypeModelFacetLinks_Usage_4013(modelElement, crossReferences));
 		result.addAll(getIncomingTypeModelFacetLinks_TemplateBinding_4016(modelElement, crossReferences));
 		result.addAll(getIncomingFeatureModelFacetLinks_Comment_AnnotatedElement_4019(modelElement, crossReferences));
 		return result;
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor> getInterface_2010IncomingLinks(View view) {
 		Interface modelElement = (Interface) view.getElement();
 		Map<EObject, Collection<EStructuralFeature.Setting>> crossReferences = EcoreUtil.CrossReferencer.find(view.eResource().getResourceSet().getResources());
 		LinkedList<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor> result = new LinkedList<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor>();
 		result.addAll(getIncomingTypeModelFacetLinks_Generalization_4001(modelElement, crossReferences));
 		result.addAll(getIncomingTypeModelFacetLinks_Dependency_4002(modelElement, crossReferences));
 		result.addAll(getIncomingTypeModelFacetLinks_Property_4003(modelElement, crossReferences));
 		result.addAll(getIncomingFeatureModelFacetLinks_Constraint_ConstrainedElement_4004(modelElement, crossReferences));
 		result.addAll(getIncomingTypeModelFacetLinks_Association_4005(modelElement, crossReferences));
 		result.addAll(getIncomingFeatureModelFacetLinks_Dependency_Supplier_4006(modelElement, crossReferences));
 		result.addAll(getIncomingFeatureModelFacetLinks_Dependency_Client_4007(modelElement, crossReferences));
 		result.addAll(getIncomingTypeModelFacetLinks_InterfaceRealization_4008(modelElement, crossReferences));
 		result.addAll(getIncomingTypeModelFacetLinks_Realization_4010(modelElement, crossReferences));
 		result.addAll(getIncomingFeatureModelFacetLinks_Generalization_General_4012(modelElement, crossReferences));
 		result.addAll(getIncomingTypeModelFacetLinks_Usage_4013(modelElement, crossReferences));
 		result.addAll(getIncomingTypeModelFacetLinks_TemplateBinding_4016(modelElement, crossReferences));
 		result.addAll(getIncomingFeatureModelFacetLinks_Port_Provided_4017(modelElement, crossReferences));
 		result.addAll(getIncomingFeatureModelFacetLinks_Port_Required_4018(modelElement, crossReferences));
 		result.addAll(getIncomingFeatureModelFacetLinks_Comment_AnnotatedElement_4019(modelElement, crossReferences));
 		return result;
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor> getConstraint_2006IncomingLinks(View view) {
 		Constraint modelElement = (Constraint) view.getElement();
 		Map<EObject, Collection<EStructuralFeature.Setting>> crossReferences = EcoreUtil.CrossReferencer.find(view.eResource().getResourceSet().getResources());
 		LinkedList<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor> result = new LinkedList<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor>();
 		result.addAll(getIncomingTypeModelFacetLinks_Dependency_4002(modelElement, crossReferences));
 		result.addAll(getIncomingFeatureModelFacetLinks_Constraint_ConstrainedElement_4004(modelElement, crossReferences));
 		result.addAll(getIncomingFeatureModelFacetLinks_Dependency_Supplier_4006(modelElement, crossReferences));
 		result.addAll(getIncomingFeatureModelFacetLinks_Dependency_Client_4007(modelElement, crossReferences));
 		result.addAll(getIncomingTypeModelFacetLinks_Realization_4010(modelElement, crossReferences));
 		result.addAll(getIncomingTypeModelFacetLinks_Usage_4013(modelElement, crossReferences));
 		result.addAll(getIncomingFeatureModelFacetLinks_Comment_AnnotatedElement_4019(modelElement, crossReferences));
 		return result;
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor> getInstanceSpecification_2008IncomingLinks(View view) {
 		InstanceSpecification modelElement = (InstanceSpecification) view.getElement();
 		Map<EObject, Collection<EStructuralFeature.Setting>> crossReferences = EcoreUtil.CrossReferencer.find(view.eResource().getResourceSet().getResources());
 		LinkedList<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor> result = new LinkedList<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor>();
 		result.addAll(getIncomingTypeModelFacetLinks_Dependency_4002(modelElement, crossReferences));
 		result.addAll(getIncomingFeatureModelFacetLinks_Constraint_ConstrainedElement_4004(modelElement, crossReferences));
 		result.addAll(getIncomingFeatureModelFacetLinks_Dependency_Supplier_4006(modelElement, crossReferences));
 		result.addAll(getIncomingFeatureModelFacetLinks_Dependency_Client_4007(modelElement, crossReferences));
 		result.addAll(getIncomingTypeModelFacetLinks_Realization_4010(modelElement, crossReferences));
 		result.addAll(getIncomingTypeModelFacetLinks_Usage_4013(modelElement, crossReferences));
 		result.addAll(getIncomingTypeModelFacetLinks_Slot_4015(modelElement, crossReferences));
 		result.addAll(getIncomingFeatureModelFacetLinks_Comment_AnnotatedElement_4019(modelElement, crossReferences));
 		return result;
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor> getDependency_2009IncomingLinks(View view) {
 		Dependency modelElement = (Dependency) view.getElement();
 		Map<EObject, Collection<EStructuralFeature.Setting>> crossReferences = EcoreUtil.CrossReferencer.find(view.eResource().getResourceSet().getResources());
 		LinkedList<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor> result = new LinkedList<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor>();
 		result.addAll(getIncomingTypeModelFacetLinks_Dependency_4002(modelElement, crossReferences));
 		result.addAll(getIncomingFeatureModelFacetLinks_Constraint_ConstrainedElement_4004(modelElement, crossReferences));
 		result.addAll(getIncomingFeatureModelFacetLinks_Dependency_Supplier_4006(modelElement, crossReferences));
 		result.addAll(getIncomingFeatureModelFacetLinks_Dependency_Client_4007(modelElement, crossReferences));
 		result.addAll(getIncomingTypeModelFacetLinks_Realization_4010(modelElement, crossReferences));
 		result.addAll(getIncomingTypeModelFacetLinks_Usage_4013(modelElement, crossReferences));
 		result.addAll(getIncomingFeatureModelFacetLinks_Comment_AnnotatedElement_4019(modelElement, crossReferences));
 		return result;
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor> getGeneralizationSet_2012IncomingLinks(View view) {
 		GeneralizationSet modelElement = (GeneralizationSet) view.getElement();
 		Map<EObject, Collection<EStructuralFeature.Setting>> crossReferences = EcoreUtil.CrossReferencer.find(view.eResource().getResourceSet().getResources());
 		LinkedList<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor> result = new LinkedList<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor>();
 		result.addAll(getIncomingTypeModelFacetLinks_Dependency_4002(modelElement, crossReferences));
 		result.addAll(getIncomingFeatureModelFacetLinks_Constraint_ConstrainedElement_4004(modelElement, crossReferences));
 		result.addAll(getIncomingFeatureModelFacetLinks_Dependency_Supplier_4006(modelElement, crossReferences));
 		result.addAll(getIncomingFeatureModelFacetLinks_Dependency_Client_4007(modelElement, crossReferences));
 		result.addAll(getIncomingTypeModelFacetLinks_Realization_4010(modelElement, crossReferences));
 		result.addAll(getIncomingTypeModelFacetLinks_Generalization_4011(modelElement, crossReferences));
 		result.addAll(getIncomingTypeModelFacetLinks_Usage_4013(modelElement, crossReferences));
 		result.addAll(getIncomingFeatureModelFacetLinks_Comment_AnnotatedElement_4019(modelElement, crossReferences));
 		return result;
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor> getInterface_2013IncomingLinks(View view) {
 		Interface modelElement = (Interface) view.getElement();
 		Map<EObject, Collection<EStructuralFeature.Setting>> crossReferences = EcoreUtil.CrossReferencer.find(view.eResource().getResourceSet().getResources());
 		LinkedList<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor> result = new LinkedList<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor>();
 		result.addAll(getIncomingTypeModelFacetLinks_Generalization_4001(modelElement, crossReferences));
 		result.addAll(getIncomingTypeModelFacetLinks_Dependency_4002(modelElement, crossReferences));
 		result.addAll(getIncomingTypeModelFacetLinks_Property_4003(modelElement, crossReferences));
 		result.addAll(getIncomingFeatureModelFacetLinks_Constraint_ConstrainedElement_4004(modelElement, crossReferences));
 		result.addAll(getIncomingTypeModelFacetLinks_Association_4005(modelElement, crossReferences));
 		result.addAll(getIncomingFeatureModelFacetLinks_Dependency_Supplier_4006(modelElement, crossReferences));
 		result.addAll(getIncomingFeatureModelFacetLinks_Dependency_Client_4007(modelElement, crossReferences));
 		result.addAll(getIncomingTypeModelFacetLinks_InterfaceRealization_4008(modelElement, crossReferences));
 		result.addAll(getIncomingTypeModelFacetLinks_Realization_4010(modelElement, crossReferences));
 		result.addAll(getIncomingFeatureModelFacetLinks_Generalization_General_4012(modelElement, crossReferences));
 		result.addAll(getIncomingTypeModelFacetLinks_Usage_4013(modelElement, crossReferences));
 		result.addAll(getIncomingTypeModelFacetLinks_TemplateBinding_4016(modelElement, crossReferences));
 		result.addAll(getIncomingFeatureModelFacetLinks_Port_Provided_4017(modelElement, crossReferences));
 		result.addAll(getIncomingFeatureModelFacetLinks_Port_Required_4018(modelElement, crossReferences));
 		result.addAll(getIncomingFeatureModelFacetLinks_Comment_AnnotatedElement_4019(modelElement, crossReferences));
 		return result;
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor> getPackage_2014IncomingLinks(View view) {
 		//no links to, from and inside the diagram header
 		return Collections.emptyList();
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor> getAssociationClass_2015IncomingLinks(View view) {
 		AssociationClass modelElement = (AssociationClass) view.getElement();
 		Map<EObject, Collection<EStructuralFeature.Setting>> crossReferences = EcoreUtil.CrossReferencer.find(view.eResource().getResourceSet().getResources());
 		LinkedList<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor> result = new LinkedList<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor>();
 		result.addAll(getIncomingTypeModelFacetLinks_Generalization_4001(modelElement, crossReferences));
 		result.addAll(getIncomingTypeModelFacetLinks_Dependency_4002(modelElement, crossReferences));
 		result.addAll(getIncomingTypeModelFacetLinks_Property_4003(modelElement, crossReferences));
 		result.addAll(getIncomingFeatureModelFacetLinks_Constraint_ConstrainedElement_4004(modelElement, crossReferences));
 		result.addAll(getIncomingTypeModelFacetLinks_Association_4005(modelElement, crossReferences));
 		result.addAll(getIncomingFeatureModelFacetLinks_Dependency_Supplier_4006(modelElement, crossReferences));
 		result.addAll(getIncomingFeatureModelFacetLinks_Dependency_Client_4007(modelElement, crossReferences));
 		result.addAll(getIncomingTypeModelFacetLinks_Realization_4010(modelElement, crossReferences));
 		result.addAll(getIncomingFeatureModelFacetLinks_Generalization_General_4012(modelElement, crossReferences));
 		result.addAll(getIncomingTypeModelFacetLinks_Usage_4013(modelElement, crossReferences));
 		result.addAll(getIncomingTypeModelFacetLinks_TemplateBinding_4016(modelElement, crossReferences));
 		result.addAll(getIncomingFeatureModelFacetLinks_Comment_AnnotatedElement_4019(modelElement, crossReferences));
 		return result;
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor> getPackage_2016IncomingLinks(View view) {
 		Package modelElement = (Package) view.getElement();
 		Map<EObject, Collection<EStructuralFeature.Setting>> crossReferences = EcoreUtil.CrossReferencer.find(view.eResource().getResourceSet().getResources());
 		LinkedList<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor> result = new LinkedList<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor>();
 		result.addAll(getIncomingTypeModelFacetLinks_Dependency_4002(modelElement, crossReferences));
 		result.addAll(getIncomingFeatureModelFacetLinks_Constraint_ConstrainedElement_4004(modelElement, crossReferences));
 		result.addAll(getIncomingFeatureModelFacetLinks_Dependency_Supplier_4006(modelElement, crossReferences));
 		result.addAll(getIncomingFeatureModelFacetLinks_Dependency_Client_4007(modelElement, crossReferences));
 		result.addAll(getIncomingTypeModelFacetLinks_Realization_4010(modelElement, crossReferences));
 		result.addAll(getIncomingTypeModelFacetLinks_Usage_4013(modelElement, crossReferences));
 		result.addAll(getIncomingTypeModelFacetLinks_TemplateBinding_4016(modelElement, crossReferences));
 		result.addAll(getIncomingFeatureModelFacetLinks_Comment_AnnotatedElement_4019(modelElement, crossReferences));
 		return result;
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor> getInstanceSpecification_2017IncomingLinks(View view) {
 		InstanceSpecification modelElement = (InstanceSpecification) view.getElement();
 		Map<EObject, Collection<EStructuralFeature.Setting>> crossReferences = EcoreUtil.CrossReferencer.find(view.eResource().getResourceSet().getResources());
 		LinkedList<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor> result = new LinkedList<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor>();
 		result.addAll(getIncomingTypeModelFacetLinks_Dependency_4002(modelElement, crossReferences));
 		result.addAll(getIncomingFeatureModelFacetLinks_Constraint_ConstrainedElement_4004(modelElement, crossReferences));
 		result.addAll(getIncomingFeatureModelFacetLinks_Dependency_Supplier_4006(modelElement, crossReferences));
 		result.addAll(getIncomingFeatureModelFacetLinks_Dependency_Client_4007(modelElement, crossReferences));
 		result.addAll(getIncomingTypeModelFacetLinks_Realization_4010(modelElement, crossReferences));
 		result.addAll(getIncomingTypeModelFacetLinks_Usage_4013(modelElement, crossReferences));
 		result.addAll(getIncomingTypeModelFacetLinks_Slot_4015(modelElement, crossReferences));
 		result.addAll(getIncomingFeatureModelFacetLinks_Comment_AnnotatedElement_4019(modelElement, crossReferences));
 		return result;
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor> getComment_2018IncomingLinks(View view) {
 		Comment modelElement = (Comment) view.getElement();
 		Map<EObject, Collection<EStructuralFeature.Setting>> crossReferences = EcoreUtil.CrossReferencer.find(view.eResource().getResourceSet().getResources());
 		LinkedList<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor> result = new LinkedList<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor>();
 		result.addAll(getIncomingFeatureModelFacetLinks_Constraint_ConstrainedElement_4004(modelElement, crossReferences));
 		result.addAll(getIncomingFeatureModelFacetLinks_Comment_AnnotatedElement_4019(modelElement, crossReferences));
 		return result;
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor> getPackage_3006IncomingLinks(View view) {
 		return Collections.emptyList();
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor> getClass_3007IncomingLinks(View view) {
 		return Collections.emptyList();
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor> getDataType_3008IncomingLinks(View view) {
 		return Collections.emptyList();
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor> getPrimitiveType_3009IncomingLinks(View view) {
 		return Collections.emptyList();
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor> getEnumeration_3011IncomingLinks(View view) {
 		return Collections.emptyList();
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor> getAssociationClass_3012IncomingLinks(View view) {
 		return Collections.emptyList();
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor> getInterface_3041IncomingLinks(View view) {
 		return Collections.emptyList();
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor> getInstanceSpecification_3013IncomingLinks(View view) {
 		return Collections.emptyList();
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor> getProperty_3001IncomingLinks(View view) {
 		return Collections.emptyList();
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor> getOperation_3002IncomingLinks(View view) {
 		return Collections.emptyList();
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor> getClass_3003IncomingLinks(View view) {
 		return Collections.emptyList();
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor> getPort_3025IncomingLinks(View view) {
 		Port modelElement = (Port) view.getElement();
 		Map<EObject, Collection<EStructuralFeature.Setting>> crossReferences = EcoreUtil.CrossReferencer.find(view.eResource().getResourceSet().getResources());
 		LinkedList<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor> result = new LinkedList<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor>();
 		result.addAll(getIncomingTypeModelFacetLinks_Dependency_4002(modelElement, crossReferences));
 		result.addAll(getIncomingFeatureModelFacetLinks_Constraint_ConstrainedElement_4004(modelElement, crossReferences));
 		result.addAll(getIncomingFeatureModelFacetLinks_Dependency_Supplier_4006(modelElement, crossReferences));
 		result.addAll(getIncomingFeatureModelFacetLinks_Dependency_Client_4007(modelElement, crossReferences));
 		result.addAll(getIncomingTypeModelFacetLinks_Realization_4010(modelElement, crossReferences));
 		result.addAll(getIncomingTypeModelFacetLinks_Usage_4013(modelElement, crossReferences));
 		result.addAll(getIncomingFeatureModelFacetLinks_Comment_AnnotatedElement_4019(modelElement, crossReferences));
 		return result;
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor> getRedefinableTemplateSignature_3027IncomingLinks(View view) {
 		RedefinableTemplateSignature modelElement = (RedefinableTemplateSignature) view.getElement();
 		Map<EObject, Collection<EStructuralFeature.Setting>> crossReferences = EcoreUtil.CrossReferencer.find(view.eResource().getResourceSet().getResources());
 		LinkedList<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor> result = new LinkedList<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor>();
 		result.addAll(getIncomingTypeModelFacetLinks_Dependency_4002(modelElement, crossReferences));
 		result.addAll(getIncomingFeatureModelFacetLinks_Constraint_ConstrainedElement_4004(modelElement, crossReferences));
 		result.addAll(getIncomingFeatureModelFacetLinks_Dependency_Supplier_4006(modelElement, crossReferences));
 		result.addAll(getIncomingFeatureModelFacetLinks_Dependency_Client_4007(modelElement, crossReferences));
 		result.addAll(getIncomingTypeModelFacetLinks_Realization_4010(modelElement, crossReferences));
 		result.addAll(getIncomingTypeModelFacetLinks_Usage_4013(modelElement, crossReferences));
 		result.addAll(getIncomingFeatureModelFacetLinks_Comment_AnnotatedElement_4019(modelElement, crossReferences));
 		return result;
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor> getProperty_3019IncomingLinks(View view) {
 		return Collections.emptyList();
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor> getOperation_3020IncomingLinks(View view) {
 		return Collections.emptyList();
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor> getProperty_3014IncomingLinks(View view) {
 		return Collections.emptyList();
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor> getOperation_3015IncomingLinks(View view) {
 		return Collections.emptyList();
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor> getProperty_3021IncomingLinks(View view) {
 		return Collections.emptyList();
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor> getOperation_3022IncomingLinks(View view) {
 		return Collections.emptyList();
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor> getEnumerationLiteral_3016IncomingLinks(View view) {
 		return Collections.emptyList();
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor> getProperty_3023IncomingLinks(View view) {
 		return Collections.emptyList();
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor> getOperation_3024IncomingLinks(View view) {
 		return Collections.emptyList();
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor> getSlot_3017IncomingLinks(View view) {
 		return Collections.emptyList();
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor> getProperty_3028IncomingLinks(View view) {
 		return Collections.emptyList();
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor> getOperation_3029IncomingLinks(View view) {
 		return Collections.emptyList();
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor> getClass_3030IncomingLinks(View view) {
 		return Collections.emptyList();
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor> getElementImport_3031IncomingLinks(View view) {
 		return Collections.emptyList();
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor> getPackage_3032IncomingLinks(View view) {
 		Package modelElement = (Package) view.getElement();
 		Map<EObject, Collection<EStructuralFeature.Setting>> crossReferences = EcoreUtil.CrossReferencer.find(view.eResource().getResourceSet().getResources());
 		LinkedList<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor> result = new LinkedList<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor>();
 		result.addAll(getIncomingTypeModelFacetLinks_Dependency_4002(modelElement, crossReferences));
 		result.addAll(getIncomingFeatureModelFacetLinks_Constraint_ConstrainedElement_4004(modelElement, crossReferences));
 		result.addAll(getIncomingFeatureModelFacetLinks_Dependency_Supplier_4006(modelElement, crossReferences));
 		result.addAll(getIncomingFeatureModelFacetLinks_Dependency_Client_4007(modelElement, crossReferences));
 		result.addAll(getIncomingTypeModelFacetLinks_Realization_4010(modelElement, crossReferences));
 		result.addAll(getIncomingTypeModelFacetLinks_Usage_4013(modelElement, crossReferences));
 		result.addAll(getIncomingTypeModelFacetLinks_TemplateBinding_4016(modelElement, crossReferences));
 		result.addAll(getIncomingFeatureModelFacetLinks_Comment_AnnotatedElement_4019(modelElement, crossReferences));
 		return result;
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor> getClass_3033IncomingLinks(View view) {
 		Class modelElement = (Class) view.getElement();
 		Map<EObject, Collection<EStructuralFeature.Setting>> crossReferences = EcoreUtil.CrossReferencer.find(view.eResource().getResourceSet().getResources());
 		LinkedList<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor> result = new LinkedList<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor>();
 		result.addAll(getIncomingTypeModelFacetLinks_Generalization_4001(modelElement, crossReferences));
 		result.addAll(getIncomingTypeModelFacetLinks_Dependency_4002(modelElement, crossReferences));
 		result.addAll(getIncomingTypeModelFacetLinks_Property_4003(modelElement, crossReferences));
 		result.addAll(getIncomingFeatureModelFacetLinks_Constraint_ConstrainedElement_4004(modelElement, crossReferences));
 		result.addAll(getIncomingTypeModelFacetLinks_Association_4005(modelElement, crossReferences));
 		result.addAll(getIncomingFeatureModelFacetLinks_Dependency_Supplier_4006(modelElement, crossReferences));
 		result.addAll(getIncomingFeatureModelFacetLinks_Dependency_Client_4007(modelElement, crossReferences));
 		result.addAll(getIncomingTypeModelFacetLinks_Realization_4010(modelElement, crossReferences));
 		result.addAll(getIncomingFeatureModelFacetLinks_Generalization_General_4012(modelElement, crossReferences));
 		result.addAll(getIncomingTypeModelFacetLinks_Usage_4013(modelElement, crossReferences));
 		result.addAll(getIncomingTypeModelFacetLinks_TemplateBinding_4016(modelElement, crossReferences));
 		result.addAll(getIncomingFeatureModelFacetLinks_Comment_AnnotatedElement_4019(modelElement, crossReferences));
 		return result;
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor> getEnumeration_3034IncomingLinks(View view) {
 		Enumeration modelElement = (Enumeration) view.getElement();
 		Map<EObject, Collection<EStructuralFeature.Setting>> crossReferences = EcoreUtil.CrossReferencer.find(view.eResource().getResourceSet().getResources());
 		LinkedList<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor> result = new LinkedList<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor>();
 		result.addAll(getIncomingTypeModelFacetLinks_Generalization_4001(modelElement, crossReferences));
 		result.addAll(getIncomingTypeModelFacetLinks_Dependency_4002(modelElement, crossReferences));
 		result.addAll(getIncomingTypeModelFacetLinks_Property_4003(modelElement, crossReferences));
 		result.addAll(getIncomingFeatureModelFacetLinks_Constraint_ConstrainedElement_4004(modelElement, crossReferences));
 		result.addAll(getIncomingTypeModelFacetLinks_Association_4005(modelElement, crossReferences));
 		result.addAll(getIncomingFeatureModelFacetLinks_Dependency_Supplier_4006(modelElement, crossReferences));
 		result.addAll(getIncomingFeatureModelFacetLinks_Dependency_Client_4007(modelElement, crossReferences));
 		result.addAll(getIncomingTypeModelFacetLinks_Realization_4010(modelElement, crossReferences));
 		result.addAll(getIncomingFeatureModelFacetLinks_Generalization_General_4012(modelElement, crossReferences));
 		result.addAll(getIncomingTypeModelFacetLinks_Usage_4013(modelElement, crossReferences));
 		result.addAll(getIncomingTypeModelFacetLinks_TemplateBinding_4016(modelElement, crossReferences));
 		result.addAll(getIncomingFeatureModelFacetLinks_Comment_AnnotatedElement_4019(modelElement, crossReferences));
 		return result;
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor> getInstanceSpecification_3035IncomingLinks(View view) {
 		InstanceSpecification modelElement = (InstanceSpecification) view.getElement();
 		Map<EObject, Collection<EStructuralFeature.Setting>> crossReferences = EcoreUtil.CrossReferencer.find(view.eResource().getResourceSet().getResources());
 		LinkedList<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor> result = new LinkedList<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor>();
 		result.addAll(getIncomingTypeModelFacetLinks_Dependency_4002(modelElement, crossReferences));
 		result.addAll(getIncomingFeatureModelFacetLinks_Constraint_ConstrainedElement_4004(modelElement, crossReferences));
 		result.addAll(getIncomingFeatureModelFacetLinks_Dependency_Supplier_4006(modelElement, crossReferences));
 		result.addAll(getIncomingFeatureModelFacetLinks_Dependency_Client_4007(modelElement, crossReferences));
 		result.addAll(getIncomingTypeModelFacetLinks_Realization_4010(modelElement, crossReferences));
 		result.addAll(getIncomingTypeModelFacetLinks_Usage_4013(modelElement, crossReferences));
 		result.addAll(getIncomingTypeModelFacetLinks_Slot_4015(modelElement, crossReferences));
 		result.addAll(getIncomingFeatureModelFacetLinks_Comment_AnnotatedElement_4019(modelElement, crossReferences));
 		return result;
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor> getDataType_3036IncomingLinks(View view) {
 		DataType modelElement = (DataType) view.getElement();
 		Map<EObject, Collection<EStructuralFeature.Setting>> crossReferences = EcoreUtil.CrossReferencer.find(view.eResource().getResourceSet().getResources());
 		LinkedList<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor> result = new LinkedList<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor>();
 		result.addAll(getIncomingTypeModelFacetLinks_Generalization_4001(modelElement, crossReferences));
 		result.addAll(getIncomingTypeModelFacetLinks_Dependency_4002(modelElement, crossReferences));
 		result.addAll(getIncomingTypeModelFacetLinks_Property_4003(modelElement, crossReferences));
 		result.addAll(getIncomingFeatureModelFacetLinks_Constraint_ConstrainedElement_4004(modelElement, crossReferences));
 		result.addAll(getIncomingTypeModelFacetLinks_Association_4005(modelElement, crossReferences));
 		result.addAll(getIncomingFeatureModelFacetLinks_Dependency_Supplier_4006(modelElement, crossReferences));
 		result.addAll(getIncomingFeatureModelFacetLinks_Dependency_Client_4007(modelElement, crossReferences));
 		result.addAll(getIncomingTypeModelFacetLinks_Realization_4010(modelElement, crossReferences));
 		result.addAll(getIncomingFeatureModelFacetLinks_Generalization_General_4012(modelElement, crossReferences));
 		result.addAll(getIncomingTypeModelFacetLinks_Usage_4013(modelElement, crossReferences));
 		result.addAll(getIncomingTypeModelFacetLinks_TemplateBinding_4016(modelElement, crossReferences));
 		result.addAll(getIncomingFeatureModelFacetLinks_Comment_AnnotatedElement_4019(modelElement, crossReferences));
 		return result;
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor> getPrimitiveType_3037IncomingLinks(View view) {
 		PrimitiveType modelElement = (PrimitiveType) view.getElement();
 		Map<EObject, Collection<EStructuralFeature.Setting>> crossReferences = EcoreUtil.CrossReferencer.find(view.eResource().getResourceSet().getResources());
 		LinkedList<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor> result = new LinkedList<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor>();
 		result.addAll(getIncomingTypeModelFacetLinks_Generalization_4001(modelElement, crossReferences));
 		result.addAll(getIncomingTypeModelFacetLinks_Dependency_4002(modelElement, crossReferences));
 		result.addAll(getIncomingTypeModelFacetLinks_Property_4003(modelElement, crossReferences));
 		result.addAll(getIncomingFeatureModelFacetLinks_Constraint_ConstrainedElement_4004(modelElement, crossReferences));
 		result.addAll(getIncomingTypeModelFacetLinks_Association_4005(modelElement, crossReferences));
 		result.addAll(getIncomingFeatureModelFacetLinks_Dependency_Supplier_4006(modelElement, crossReferences));
 		result.addAll(getIncomingFeatureModelFacetLinks_Dependency_Client_4007(modelElement, crossReferences));
 		result.addAll(getIncomingTypeModelFacetLinks_Realization_4010(modelElement, crossReferences));
 		result.addAll(getIncomingFeatureModelFacetLinks_Generalization_General_4012(modelElement, crossReferences));
 		result.addAll(getIncomingTypeModelFacetLinks_Usage_4013(modelElement, crossReferences));
 		result.addAll(getIncomingTypeModelFacetLinks_TemplateBinding_4016(modelElement, crossReferences));
 		result.addAll(getIncomingFeatureModelFacetLinks_Comment_AnnotatedElement_4019(modelElement, crossReferences));
 		return result;
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor> getInterface_3042IncomingLinks(View view) {
 		Interface modelElement = (Interface) view.getElement();
 		Map<EObject, Collection<EStructuralFeature.Setting>> crossReferences = EcoreUtil.CrossReferencer.find(view.eResource().getResourceSet().getResources());
 		LinkedList<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor> result = new LinkedList<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor>();
 		result.addAll(getIncomingTypeModelFacetLinks_Generalization_4001(modelElement, crossReferences));
 		result.addAll(getIncomingTypeModelFacetLinks_Dependency_4002(modelElement, crossReferences));
 		result.addAll(getIncomingTypeModelFacetLinks_Property_4003(modelElement, crossReferences));
 		result.addAll(getIncomingFeatureModelFacetLinks_Constraint_ConstrainedElement_4004(modelElement, crossReferences));
 		result.addAll(getIncomingTypeModelFacetLinks_Association_4005(modelElement, crossReferences));
 		result.addAll(getIncomingFeatureModelFacetLinks_Dependency_Supplier_4006(modelElement, crossReferences));
 		result.addAll(getIncomingFeatureModelFacetLinks_Dependency_Client_4007(modelElement, crossReferences));
 		result.addAll(getIncomingTypeModelFacetLinks_InterfaceRealization_4008(modelElement, crossReferences));
 		result.addAll(getIncomingTypeModelFacetLinks_Realization_4010(modelElement, crossReferences));
 		result.addAll(getIncomingFeatureModelFacetLinks_Generalization_General_4012(modelElement, crossReferences));
 		result.addAll(getIncomingTypeModelFacetLinks_Usage_4013(modelElement, crossReferences));
 		result.addAll(getIncomingTypeModelFacetLinks_TemplateBinding_4016(modelElement, crossReferences));
 		result.addAll(getIncomingFeatureModelFacetLinks_Port_Provided_4017(modelElement, crossReferences));
 		result.addAll(getIncomingFeatureModelFacetLinks_Port_Required_4018(modelElement, crossReferences));
 		result.addAll(getIncomingFeatureModelFacetLinks_Comment_AnnotatedElement_4019(modelElement, crossReferences));
 		return result;
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor> getInterface_3043IncomingLinks(View view) {
 		Interface modelElement = (Interface) view.getElement();
 		Map<EObject, Collection<EStructuralFeature.Setting>> crossReferences = EcoreUtil.CrossReferencer.find(view.eResource().getResourceSet().getResources());
 		LinkedList<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor> result = new LinkedList<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor>();
 		result.addAll(getIncomingTypeModelFacetLinks_Generalization_4001(modelElement, crossReferences));
 		result.addAll(getIncomingTypeModelFacetLinks_Dependency_4002(modelElement, crossReferences));
 		result.addAll(getIncomingTypeModelFacetLinks_Property_4003(modelElement, crossReferences));
 		result.addAll(getIncomingFeatureModelFacetLinks_Constraint_ConstrainedElement_4004(modelElement, crossReferences));
 		result.addAll(getIncomingTypeModelFacetLinks_Association_4005(modelElement, crossReferences));
 		result.addAll(getIncomingFeatureModelFacetLinks_Dependency_Supplier_4006(modelElement, crossReferences));
 		result.addAll(getIncomingFeatureModelFacetLinks_Dependency_Client_4007(modelElement, crossReferences));
 		result.addAll(getIncomingTypeModelFacetLinks_InterfaceRealization_4008(modelElement, crossReferences));
 		result.addAll(getIncomingTypeModelFacetLinks_Realization_4010(modelElement, crossReferences));
 		result.addAll(getIncomingFeatureModelFacetLinks_Generalization_General_4012(modelElement, crossReferences));
 		result.addAll(getIncomingTypeModelFacetLinks_Usage_4013(modelElement, crossReferences));
 		result.addAll(getIncomingTypeModelFacetLinks_TemplateBinding_4016(modelElement, crossReferences));
 		result.addAll(getIncomingFeatureModelFacetLinks_Port_Provided_4017(modelElement, crossReferences));
 		result.addAll(getIncomingFeatureModelFacetLinks_Port_Required_4018(modelElement, crossReferences));
 		result.addAll(getIncomingFeatureModelFacetLinks_Comment_AnnotatedElement_4019(modelElement, crossReferences));
 		return result;
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor> getLiteralString_3038IncomingLinks(View view) {
 		return Collections.emptyList();
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor> getLiteralInteger_3039IncomingLinks(View view) {
 		return Collections.emptyList();
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor> getExpression_3040IncomingLinks(View view) {
 		return Collections.emptyList();
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor> getGeneralization_4001IncomingLinks(View view) {
 		Generalization modelElement = (Generalization) view.getElement();
 		Map<EObject, Collection<EStructuralFeature.Setting>> crossReferences = EcoreUtil.CrossReferencer.find(view.eResource().getResourceSet().getResources());
 		LinkedList<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor> result = new LinkedList<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor>();
 		result.addAll(getIncomingFeatureModelFacetLinks_Constraint_ConstrainedElement_4004(modelElement, crossReferences));
 		result.addAll(getIncomingFeatureModelFacetLinks_Comment_AnnotatedElement_4019(modelElement, crossReferences));
 		return result;
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor> getDependency_4002IncomingLinks(View view) {
 		Dependency modelElement = (Dependency) view.getElement();
 		Map<EObject, Collection<EStructuralFeature.Setting>> crossReferences = EcoreUtil.CrossReferencer.find(view.eResource().getResourceSet().getResources());
 		LinkedList<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor> result = new LinkedList<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor>();
 		result.addAll(getIncomingTypeModelFacetLinks_Dependency_4002(modelElement, crossReferences));
 		result.addAll(getIncomingFeatureModelFacetLinks_Constraint_ConstrainedElement_4004(modelElement, crossReferences));
 		result.addAll(getIncomingFeatureModelFacetLinks_Dependency_Supplier_4006(modelElement, crossReferences));
 		result.addAll(getIncomingFeatureModelFacetLinks_Dependency_Client_4007(modelElement, crossReferences));
 		result.addAll(getIncomingTypeModelFacetLinks_Realization_4010(modelElement, crossReferences));
 		result.addAll(getIncomingTypeModelFacetLinks_Usage_4013(modelElement, crossReferences));
 		result.addAll(getIncomingFeatureModelFacetLinks_Comment_AnnotatedElement_4019(modelElement, crossReferences));
 		return result;
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor> getProperty_4003IncomingLinks(View view) {
 		Property modelElement = (Property) view.getElement();
 		Map<EObject, Collection<EStructuralFeature.Setting>> crossReferences = EcoreUtil.CrossReferencer.find(view.eResource().getResourceSet().getResources());
 		LinkedList<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor> result = new LinkedList<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor>();
 		result.addAll(getIncomingTypeModelFacetLinks_Dependency_4002(modelElement, crossReferences));
 		result.addAll(getIncomingFeatureModelFacetLinks_Constraint_ConstrainedElement_4004(modelElement, crossReferences));
 		result.addAll(getIncomingFeatureModelFacetLinks_Dependency_Supplier_4006(modelElement, crossReferences));
 		result.addAll(getIncomingFeatureModelFacetLinks_Dependency_Client_4007(modelElement, crossReferences));
 		result.addAll(getIncomingTypeModelFacetLinks_Realization_4010(modelElement, crossReferences));
 		result.addAll(getIncomingTypeModelFacetLinks_Usage_4013(modelElement, crossReferences));
 		result.addAll(getIncomingFeatureModelFacetLinks_Comment_AnnotatedElement_4019(modelElement, crossReferences));
 		return result;
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor> getAssociation_4005IncomingLinks(View view) {
 		Association modelElement = (Association) view.getElement();
 		Map<EObject, Collection<EStructuralFeature.Setting>> crossReferences = EcoreUtil.CrossReferencer.find(view.eResource().getResourceSet().getResources());
 		LinkedList<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor> result = new LinkedList<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor>();
 		result.addAll(getIncomingTypeModelFacetLinks_Generalization_4001(modelElement, crossReferences));
 		result.addAll(getIncomingTypeModelFacetLinks_Dependency_4002(modelElement, crossReferences));
 		result.addAll(getIncomingTypeModelFacetLinks_Property_4003(modelElement, crossReferences));
 		result.addAll(getIncomingFeatureModelFacetLinks_Constraint_ConstrainedElement_4004(modelElement, crossReferences));
 		result.addAll(getIncomingTypeModelFacetLinks_Association_4005(modelElement, crossReferences));
 		result.addAll(getIncomingFeatureModelFacetLinks_Dependency_Supplier_4006(modelElement, crossReferences));
 		result.addAll(getIncomingFeatureModelFacetLinks_Dependency_Client_4007(modelElement, crossReferences));
 		result.addAll(getIncomingTypeModelFacetLinks_Realization_4010(modelElement, crossReferences));
 		result.addAll(getIncomingFeatureModelFacetLinks_Generalization_General_4012(modelElement, crossReferences));
 		result.addAll(getIncomingTypeModelFacetLinks_Usage_4013(modelElement, crossReferences));
 		result.addAll(getIncomingTypeModelFacetLinks_TemplateBinding_4016(modelElement, crossReferences));
 		result.addAll(getIncomingFeatureModelFacetLinks_Comment_AnnotatedElement_4019(modelElement, crossReferences));
 		return result;
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor> getInterfaceRealization_4008IncomingLinks(View view) {
 		InterfaceRealization modelElement = (InterfaceRealization) view.getElement();
 		Map<EObject, Collection<EStructuralFeature.Setting>> crossReferences = EcoreUtil.CrossReferencer.find(view.eResource().getResourceSet().getResources());
 		LinkedList<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor> result = new LinkedList<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor>();
 		result.addAll(getIncomingTypeModelFacetLinks_Dependency_4002(modelElement, crossReferences));
 		result.addAll(getIncomingFeatureModelFacetLinks_Constraint_ConstrainedElement_4004(modelElement, crossReferences));
 		result.addAll(getIncomingFeatureModelFacetLinks_Dependency_Supplier_4006(modelElement, crossReferences));
 		result.addAll(getIncomingFeatureModelFacetLinks_Dependency_Client_4007(modelElement, crossReferences));
 		result.addAll(getIncomingTypeModelFacetLinks_Realization_4010(modelElement, crossReferences));
 		result.addAll(getIncomingTypeModelFacetLinks_Usage_4013(modelElement, crossReferences));
 		result.addAll(getIncomingFeatureModelFacetLinks_Comment_AnnotatedElement_4019(modelElement, crossReferences));
 		return result;
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor> getRealization_4010IncomingLinks(View view) {
 		Realization modelElement = (Realization) view.getElement();
 		Map<EObject, Collection<EStructuralFeature.Setting>> crossReferences = EcoreUtil.CrossReferencer.find(view.eResource().getResourceSet().getResources());
 		LinkedList<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor> result = new LinkedList<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor>();
 		result.addAll(getIncomingTypeModelFacetLinks_Dependency_4002(modelElement, crossReferences));
 		result.addAll(getIncomingFeatureModelFacetLinks_Constraint_ConstrainedElement_4004(modelElement, crossReferences));
 		result.addAll(getIncomingFeatureModelFacetLinks_Dependency_Supplier_4006(modelElement, crossReferences));
 		result.addAll(getIncomingFeatureModelFacetLinks_Dependency_Client_4007(modelElement, crossReferences));
 		result.addAll(getIncomingTypeModelFacetLinks_Realization_4010(modelElement, crossReferences));
 		result.addAll(getIncomingTypeModelFacetLinks_Usage_4013(modelElement, crossReferences));
 		result.addAll(getIncomingFeatureModelFacetLinks_Comment_AnnotatedElement_4019(modelElement, crossReferences));
 		return result;
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor> getGeneralization_4011IncomingLinks(View view) {
 		Generalization modelElement = (Generalization) view.getElement();
 		Map<EObject, Collection<EStructuralFeature.Setting>> crossReferences = EcoreUtil.CrossReferencer.find(view.eResource().getResourceSet().getResources());
 		LinkedList<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor> result = new LinkedList<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor>();
 		result.addAll(getIncomingFeatureModelFacetLinks_Constraint_ConstrainedElement_4004(modelElement, crossReferences));
 		result.addAll(getIncomingFeatureModelFacetLinks_Comment_AnnotatedElement_4019(modelElement, crossReferences));
 		return result;
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor> getUsage_4013IncomingLinks(View view) {
 		Usage modelElement = (Usage) view.getElement();
 		Map<EObject, Collection<EStructuralFeature.Setting>> crossReferences = EcoreUtil.CrossReferencer.find(view.eResource().getResourceSet().getResources());
 		LinkedList<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor> result = new LinkedList<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor>();
 		result.addAll(getIncomingTypeModelFacetLinks_Dependency_4002(modelElement, crossReferences));
 		result.addAll(getIncomingFeatureModelFacetLinks_Constraint_ConstrainedElement_4004(modelElement, crossReferences));
 		result.addAll(getIncomingFeatureModelFacetLinks_Dependency_Supplier_4006(modelElement, crossReferences));
 		result.addAll(getIncomingFeatureModelFacetLinks_Dependency_Client_4007(modelElement, crossReferences));
 		result.addAll(getIncomingTypeModelFacetLinks_Realization_4010(modelElement, crossReferences));
 		result.addAll(getIncomingTypeModelFacetLinks_Usage_4013(modelElement, crossReferences));
 		result.addAll(getIncomingFeatureModelFacetLinks_Comment_AnnotatedElement_4019(modelElement, crossReferences));
 		return result;
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor> getAssociationClass_4014IncomingLinks(View view) {
 		AssociationClass modelElement = (AssociationClass) view.getElement();
 		Map<EObject, Collection<EStructuralFeature.Setting>> crossReferences = EcoreUtil.CrossReferencer.find(view.eResource().getResourceSet().getResources());
 		LinkedList<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor> result = new LinkedList<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor>();
 		result.addAll(getIncomingTypeModelFacetLinks_Generalization_4001(modelElement, crossReferences));
 		result.addAll(getIncomingTypeModelFacetLinks_Dependency_4002(modelElement, crossReferences));
 		result.addAll(getIncomingTypeModelFacetLinks_Property_4003(modelElement, crossReferences));
 		result.addAll(getIncomingFeatureModelFacetLinks_Constraint_ConstrainedElement_4004(modelElement, crossReferences));
 		result.addAll(getIncomingTypeModelFacetLinks_Association_4005(modelElement, crossReferences));
 		result.addAll(getIncomingFeatureModelFacetLinks_Dependency_Supplier_4006(modelElement, crossReferences));
 		result.addAll(getIncomingFeatureModelFacetLinks_Dependency_Client_4007(modelElement, crossReferences));
 		result.addAll(getIncomingTypeModelFacetLinks_Realization_4010(modelElement, crossReferences));
 		result.addAll(getIncomingFeatureModelFacetLinks_Generalization_General_4012(modelElement, crossReferences));
 		result.addAll(getIncomingTypeModelFacetLinks_Usage_4013(modelElement, crossReferences));
 		result.addAll(getIncomingTypeModelFacetLinks_TemplateBinding_4016(modelElement, crossReferences));
 		result.addAll(getIncomingFeatureModelFacetLinks_Comment_AnnotatedElement_4019(modelElement, crossReferences));
 		return result;
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor> getSlot_4015IncomingLinks(View view) {
 		Slot modelElement = (Slot) view.getElement();
 		Map<EObject, Collection<EStructuralFeature.Setting>> crossReferences = EcoreUtil.CrossReferencer.find(view.eResource().getResourceSet().getResources());
 		LinkedList<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor> result = new LinkedList<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor>();
 		result.addAll(getIncomingFeatureModelFacetLinks_Constraint_ConstrainedElement_4004(modelElement, crossReferences));
 		result.addAll(getIncomingFeatureModelFacetLinks_Comment_AnnotatedElement_4019(modelElement, crossReferences));
 		return result;
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor> getTemplateBinding_4016IncomingLinks(View view) {
 		TemplateBinding modelElement = (TemplateBinding) view.getElement();
 		Map<EObject, Collection<EStructuralFeature.Setting>> crossReferences = EcoreUtil.CrossReferencer.find(view.eResource().getResourceSet().getResources());
 		LinkedList<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor> result = new LinkedList<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor>();
 		result.addAll(getIncomingFeatureModelFacetLinks_Constraint_ConstrainedElement_4004(modelElement, crossReferences));
 		result.addAll(getIncomingFeatureModelFacetLinks_Comment_AnnotatedElement_4019(modelElement, crossReferences));
 		return result;
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor> getPackage_2002OutgoingLinks(View view) {
 		Package modelElement = (Package) view.getElement();
 		LinkedList<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor> result = new LinkedList<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor>();
 		result.addAll(getOutgoingTypeModelFacetLinks_Dependency_4002(modelElement));
 		result.addAll(getOutgoingTypeModelFacetLinks_Realization_4010(modelElement));
 		result.addAll(getOutgoingTypeModelFacetLinks_Usage_4013(modelElement));
 		result.addAll(getOutgoingTypeModelFacetLinks_TemplateBinding_4016(modelElement));
 		return result;
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor> getClass_2001OutgoingLinks(View view) {
 		Class modelElement = (Class) view.getElement();
 		LinkedList<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor> result = new LinkedList<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor>();
 		result.addAll(getContainedTypeModelFacetLinks_Generalization_4001(modelElement));
 		result.addAll(getOutgoingTypeModelFacetLinks_Dependency_4002(modelElement));
 		result.addAll(getOutgoingTypeModelFacetLinks_Association_4005(modelElement));
 		result.addAll(getOutgoingTypeModelFacetLinks_InterfaceRealization_4008(modelElement));
 		result.addAll(getOutgoingTypeModelFacetLinks_Realization_4010(modelElement));
 		result.addAll(getContainedTypeModelFacetLinks_Generalization_4011(modelElement));
 		result.addAll(getOutgoingTypeModelFacetLinks_Usage_4013(modelElement));
 		result.addAll(getOutgoingTypeModelFacetLinks_TemplateBinding_4016(modelElement));
 		return result;
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor> getAssociationClass_2007OutgoingLinks(View view) {
 		AssociationClass modelElement = (AssociationClass) view.getElement();
 		LinkedList<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor> result = new LinkedList<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor>();
 		result.addAll(getContainedTypeModelFacetLinks_Generalization_4001(modelElement));
 		result.addAll(getOutgoingTypeModelFacetLinks_Dependency_4002(modelElement));
 		result.addAll(getContainedTypeModelFacetLinks_Property_4003(modelElement));
 		result.addAll(getOutgoingTypeModelFacetLinks_Association_4005(modelElement));
 		result.addAll(getOutgoingTypeModelFacetLinks_InterfaceRealization_4008(modelElement));
 		result.addAll(getOutgoingTypeModelFacetLinks_Realization_4010(modelElement));
 		result.addAll(getContainedTypeModelFacetLinks_Generalization_4011(modelElement));
 		result.addAll(getOutgoingTypeModelFacetLinks_Usage_4013(modelElement));
 		result.addAll(getOutgoingTypeModelFacetLinks_TemplateBinding_4016(modelElement));
 		return result;
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor> getDataType_2004OutgoingLinks(View view) {
 		DataType modelElement = (DataType) view.getElement();
 		LinkedList<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor> result = new LinkedList<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor>();
 		result.addAll(getContainedTypeModelFacetLinks_Generalization_4001(modelElement));
 		result.addAll(getOutgoingTypeModelFacetLinks_Dependency_4002(modelElement));
 		result.addAll(getOutgoingTypeModelFacetLinks_Association_4005(modelElement));
 		result.addAll(getOutgoingTypeModelFacetLinks_Realization_4010(modelElement));
 		result.addAll(getContainedTypeModelFacetLinks_Generalization_4011(modelElement));
 		result.addAll(getOutgoingTypeModelFacetLinks_Usage_4013(modelElement));
 		result.addAll(getOutgoingTypeModelFacetLinks_TemplateBinding_4016(modelElement));
 		return result;
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor> getPrimitiveType_2005OutgoingLinks(View view) {
 		PrimitiveType modelElement = (PrimitiveType) view.getElement();
 		LinkedList<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor> result = new LinkedList<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor>();
 		result.addAll(getContainedTypeModelFacetLinks_Generalization_4001(modelElement));
 		result.addAll(getOutgoingTypeModelFacetLinks_Dependency_4002(modelElement));
 		result.addAll(getOutgoingTypeModelFacetLinks_Association_4005(modelElement));
 		result.addAll(getOutgoingTypeModelFacetLinks_Realization_4010(modelElement));
 		result.addAll(getContainedTypeModelFacetLinks_Generalization_4011(modelElement));
 		result.addAll(getOutgoingTypeModelFacetLinks_Usage_4013(modelElement));
 		result.addAll(getOutgoingTypeModelFacetLinks_TemplateBinding_4016(modelElement));
 		return result;
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor> getEnumeration_2003OutgoingLinks(View view) {
 		Enumeration modelElement = (Enumeration) view.getElement();
 		LinkedList<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor> result = new LinkedList<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor>();
 		result.addAll(getContainedTypeModelFacetLinks_Generalization_4001(modelElement));
 		result.addAll(getOutgoingTypeModelFacetLinks_Dependency_4002(modelElement));
 		result.addAll(getOutgoingTypeModelFacetLinks_Association_4005(modelElement));
 		result.addAll(getOutgoingTypeModelFacetLinks_Realization_4010(modelElement));
 		result.addAll(getContainedTypeModelFacetLinks_Generalization_4011(modelElement));
 		result.addAll(getOutgoingTypeModelFacetLinks_Usage_4013(modelElement));
 		result.addAll(getOutgoingTypeModelFacetLinks_TemplateBinding_4016(modelElement));
 		return result;
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor> getInterface_2010OutgoingLinks(View view) {
 		Interface modelElement = (Interface) view.getElement();
 		LinkedList<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor> result = new LinkedList<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor>();
 		result.addAll(getContainedTypeModelFacetLinks_Generalization_4001(modelElement));
 		result.addAll(getOutgoingTypeModelFacetLinks_Dependency_4002(modelElement));
 		result.addAll(getOutgoingTypeModelFacetLinks_Association_4005(modelElement));
 		result.addAll(getOutgoingTypeModelFacetLinks_Realization_4010(modelElement));
 		result.addAll(getContainedTypeModelFacetLinks_Generalization_4011(modelElement));
 		result.addAll(getOutgoingTypeModelFacetLinks_Usage_4013(modelElement));
 		result.addAll(getOutgoingTypeModelFacetLinks_TemplateBinding_4016(modelElement));
 		return result;
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor> getConstraint_2006OutgoingLinks(View view) {
 		Constraint modelElement = (Constraint) view.getElement();
 		LinkedList<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor> result = new LinkedList<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor>();
 		result.addAll(getOutgoingTypeModelFacetLinks_Dependency_4002(modelElement));
 		result.addAll(getOutgoingFeatureModelFacetLinks_Constraint_ConstrainedElement_4004(modelElement));
 		result.addAll(getOutgoingTypeModelFacetLinks_Realization_4010(modelElement));
 		result.addAll(getOutgoingTypeModelFacetLinks_Usage_4013(modelElement));
 		return result;
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor> getInstanceSpecification_2008OutgoingLinks(View view) {
 		InstanceSpecification modelElement = (InstanceSpecification) view.getElement();
 		LinkedList<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor> result = new LinkedList<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor>();
 		result.addAll(getOutgoingTypeModelFacetLinks_Dependency_4002(modelElement));
 		result.addAll(getOutgoingTypeModelFacetLinks_Realization_4010(modelElement));
 		result.addAll(getOutgoingTypeModelFacetLinks_Usage_4013(modelElement));
 		result.addAll(getOutgoingTypeModelFacetLinks_Slot_4015(modelElement));
 		return result;
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor> getDependency_2009OutgoingLinks(View view) {
 		Dependency modelElement = (Dependency) view.getElement();
 		LinkedList<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor> result = new LinkedList<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor>();
 		result.addAll(getOutgoingTypeModelFacetLinks_Dependency_4002(modelElement));
 		result.addAll(getOutgoingFeatureModelFacetLinks_Dependency_Supplier_4006(modelElement));
 		result.addAll(getOutgoingFeatureModelFacetLinks_Dependency_Client_4007(modelElement));
 		result.addAll(getOutgoingTypeModelFacetLinks_Realization_4010(modelElement));
 		result.addAll(getOutgoingTypeModelFacetLinks_Usage_4013(modelElement));
 		return result;
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor> getGeneralizationSet_2012OutgoingLinksGen(View view) {
 		GeneralizationSet modelElement = (GeneralizationSet) view.getElement();
 		LinkedList<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor> result = new LinkedList<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor>();
 		result.addAll(getOutgoingTypeModelFacetLinks_Dependency_4002(modelElement));
 		result.addAll(getOutgoingTypeModelFacetLinks_Realization_4010(modelElement));
 		result.addAll(getOutgoingTypeModelFacetLinks_Usage_4013(modelElement));
 		return result;
 	}
 
 	/**
 	 * @generated NOT
 	 */
 	public static List<UMLLinkDescriptor> getGeneralizationSet_2012OutgoingLinks(View view) {
 		GeneralizationSet modelElement = (GeneralizationSet) view.getElement();
 		List<UMLLinkDescriptor> result = new LinkedList<UMLLinkDescriptor>();
 		result.addAll(getGeneralizationSet_2012OutgoingLinksGen(view));
 		result.addAll(getOutgoingFeatureModelFacetLinks_Generalization_General_4012(modelElement));
 		return result;
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor> getInterface_2013OutgoingLinks(View view) {
 		Interface modelElement = (Interface) view.getElement();
 		LinkedList<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor> result = new LinkedList<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor>();
 		result.addAll(getContainedTypeModelFacetLinks_Generalization_4001(modelElement));
 		result.addAll(getOutgoingTypeModelFacetLinks_Dependency_4002(modelElement));
 		result.addAll(getOutgoingTypeModelFacetLinks_Association_4005(modelElement));
 		result.addAll(getOutgoingTypeModelFacetLinks_Realization_4010(modelElement));
 		result.addAll(getContainedTypeModelFacetLinks_Generalization_4011(modelElement));
 		result.addAll(getOutgoingTypeModelFacetLinks_Usage_4013(modelElement));
 		result.addAll(getOutgoingTypeModelFacetLinks_TemplateBinding_4016(modelElement));
 		return result;
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor> getPackage_2014OutgoingLinks(View view) {
 		//no links to, from and inside the diagram header
 		return Collections.emptyList();
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor> getAssociationClass_2015OutgoingLinks(View view) {
 		AssociationClass modelElement = (AssociationClass) view.getElement();
 		LinkedList<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor> result = new LinkedList<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor>();
 		result.addAll(getContainedTypeModelFacetLinks_Generalization_4001(modelElement));
 		result.addAll(getOutgoingTypeModelFacetLinks_Dependency_4002(modelElement));
 		result.addAll(getContainedTypeModelFacetLinks_Property_4003(modelElement));
 		result.addAll(getOutgoingTypeModelFacetLinks_Association_4005(modelElement));
 		result.addAll(getOutgoingTypeModelFacetLinks_InterfaceRealization_4008(modelElement));
 		result.addAll(getOutgoingTypeModelFacetLinks_Realization_4010(modelElement));
 		result.addAll(getContainedTypeModelFacetLinks_Generalization_4011(modelElement));
 		result.addAll(getOutgoingTypeModelFacetLinks_Usage_4013(modelElement));
 		result.addAll(getOutgoingTypeModelFacetLinks_TemplateBinding_4016(modelElement));
 		return result;
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor> getPackage_2016OutgoingLinks(View view) {
 		Package modelElement = (Package) view.getElement();
 		LinkedList<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor> result = new LinkedList<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor>();
 		result.addAll(getOutgoingTypeModelFacetLinks_Dependency_4002(modelElement));
 		result.addAll(getOutgoingTypeModelFacetLinks_Realization_4010(modelElement));
 		result.addAll(getOutgoingTypeModelFacetLinks_Usage_4013(modelElement));
 		result.addAll(getOutgoingTypeModelFacetLinks_TemplateBinding_4016(modelElement));
 		return result;
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor> getInstanceSpecification_2017OutgoingLinks(View view) {
 		InstanceSpecification modelElement = (InstanceSpecification) view.getElement();
 		LinkedList<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor> result = new LinkedList<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor>();
 		result.addAll(getOutgoingTypeModelFacetLinks_Dependency_4002(modelElement));
 		result.addAll(getOutgoingTypeModelFacetLinks_Realization_4010(modelElement));
 		result.addAll(getOutgoingTypeModelFacetLinks_Usage_4013(modelElement));
 		result.addAll(getOutgoingTypeModelFacetLinks_Slot_4015(modelElement));
 		return result;
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor> getComment_2018OutgoingLinks(View view) {
 		Comment modelElement = (Comment) view.getElement();
 		LinkedList<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor> result = new LinkedList<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor>();
 		result.addAll(getOutgoingFeatureModelFacetLinks_Comment_AnnotatedElement_4019(modelElement));
 		return result;
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor> getPackage_3006OutgoingLinks(View view) {
 		return Collections.emptyList();
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor> getClass_3007OutgoingLinks(View view) {
 		return Collections.emptyList();
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor> getDataType_3008OutgoingLinks(View view) {
 		return Collections.emptyList();
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor> getPrimitiveType_3009OutgoingLinks(View view) {
 		return Collections.emptyList();
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor> getEnumeration_3011OutgoingLinks(View view) {
 		return Collections.emptyList();
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor> getAssociationClass_3012OutgoingLinks(View view) {
 		return Collections.emptyList();
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor> getInterface_3041OutgoingLinks(View view) {
 		return Collections.emptyList();
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor> getInstanceSpecification_3013OutgoingLinks(View view) {
 		return Collections.emptyList();
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor> getProperty_3001OutgoingLinks(View view) {
 		return Collections.emptyList();
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor> getOperation_3002OutgoingLinks(View view) {
 		return Collections.emptyList();
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor> getClass_3003OutgoingLinks(View view) {
 		return Collections.emptyList();
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor> getPort_3025OutgoingLinks(View view) {
 		Port modelElement = (Port) view.getElement();
 		LinkedList<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor> result = new LinkedList<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor>();
 		result.addAll(getOutgoingTypeModelFacetLinks_Dependency_4002(modelElement));
 		result.addAll(getOutgoingTypeModelFacetLinks_Realization_4010(modelElement));
 		result.addAll(getOutgoingTypeModelFacetLinks_Usage_4013(modelElement));
 		result.addAll(getOutgoingFeatureModelFacetLinks_Port_Provided_4017(modelElement));
 		result.addAll(getOutgoingFeatureModelFacetLinks_Port_Required_4018(modelElement));
 		return result;
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor> getRedefinableTemplateSignature_3027OutgoingLinks(View view) {
 		RedefinableTemplateSignature modelElement = (RedefinableTemplateSignature) view.getElement();
 		LinkedList<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor> result = new LinkedList<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor>();
 		result.addAll(getOutgoingTypeModelFacetLinks_Dependency_4002(modelElement));
 		result.addAll(getOutgoingTypeModelFacetLinks_Realization_4010(modelElement));
 		result.addAll(getOutgoingTypeModelFacetLinks_Usage_4013(modelElement));
 		return result;
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor> getProperty_3019OutgoingLinks(View view) {
 		return Collections.emptyList();
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor> getOperation_3020OutgoingLinks(View view) {
 		return Collections.emptyList();
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor> getProperty_3014OutgoingLinks(View view) {
 		return Collections.emptyList();
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor> getOperation_3015OutgoingLinks(View view) {
 		return Collections.emptyList();
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor> getProperty_3021OutgoingLinks(View view) {
 		return Collections.emptyList();
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor> getOperation_3022OutgoingLinks(View view) {
 		return Collections.emptyList();
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor> getEnumerationLiteral_3016OutgoingLinks(View view) {
 		return Collections.emptyList();
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor> getProperty_3023OutgoingLinks(View view) {
 		return Collections.emptyList();
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor> getOperation_3024OutgoingLinks(View view) {
 		return Collections.emptyList();
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor> getSlot_3017OutgoingLinks(View view) {
 		return Collections.emptyList();
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor> getProperty_3028OutgoingLinks(View view) {
 		return Collections.emptyList();
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor> getOperation_3029OutgoingLinks(View view) {
 		return Collections.emptyList();
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor> getClass_3030OutgoingLinks(View view) {
 		return Collections.emptyList();
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor> getElementImport_3031OutgoingLinks(View view) {
 		return Collections.emptyList();
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor> getPackage_3032OutgoingLinks(View view) {
 		Package modelElement = (Package) view.getElement();
 		LinkedList<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor> result = new LinkedList<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor>();
 		result.addAll(getOutgoingTypeModelFacetLinks_Dependency_4002(modelElement));
 		result.addAll(getOutgoingTypeModelFacetLinks_Realization_4010(modelElement));
 		result.addAll(getOutgoingTypeModelFacetLinks_Usage_4013(modelElement));
 		result.addAll(getOutgoingTypeModelFacetLinks_TemplateBinding_4016(modelElement));
 		return result;
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor> getClass_3033OutgoingLinks(View view) {
 		Class modelElement = (Class) view.getElement();
 		LinkedList<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor> result = new LinkedList<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor>();
 		result.addAll(getContainedTypeModelFacetLinks_Generalization_4001(modelElement));
 		result.addAll(getOutgoingTypeModelFacetLinks_Dependency_4002(modelElement));
 		result.addAll(getOutgoingTypeModelFacetLinks_Association_4005(modelElement));
 		result.addAll(getOutgoingTypeModelFacetLinks_InterfaceRealization_4008(modelElement));
 		result.addAll(getOutgoingTypeModelFacetLinks_Realization_4010(modelElement));
 		result.addAll(getContainedTypeModelFacetLinks_Generalization_4011(modelElement));
 		result.addAll(getOutgoingTypeModelFacetLinks_Usage_4013(modelElement));
 		result.addAll(getOutgoingTypeModelFacetLinks_TemplateBinding_4016(modelElement));
 		return result;
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor> getEnumeration_3034OutgoingLinks(View view) {
 		Enumeration modelElement = (Enumeration) view.getElement();
 		LinkedList<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor> result = new LinkedList<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor>();
 		result.addAll(getContainedTypeModelFacetLinks_Generalization_4001(modelElement));
 		result.addAll(getOutgoingTypeModelFacetLinks_Dependency_4002(modelElement));
 		result.addAll(getOutgoingTypeModelFacetLinks_Association_4005(modelElement));
 		result.addAll(getOutgoingTypeModelFacetLinks_Realization_4010(modelElement));
 		result.addAll(getContainedTypeModelFacetLinks_Generalization_4011(modelElement));
 		result.addAll(getOutgoingTypeModelFacetLinks_Usage_4013(modelElement));
 		result.addAll(getOutgoingTypeModelFacetLinks_TemplateBinding_4016(modelElement));
 		return result;
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor> getInstanceSpecification_3035OutgoingLinks(View view) {
 		InstanceSpecification modelElement = (InstanceSpecification) view.getElement();
 		LinkedList<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor> result = new LinkedList<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor>();
 		result.addAll(getOutgoingTypeModelFacetLinks_Dependency_4002(modelElement));
 		result.addAll(getOutgoingTypeModelFacetLinks_Realization_4010(modelElement));
 		result.addAll(getOutgoingTypeModelFacetLinks_Usage_4013(modelElement));
 		result.addAll(getOutgoingTypeModelFacetLinks_Slot_4015(modelElement));
 		return result;
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor> getDataType_3036OutgoingLinks(View view) {
 		DataType modelElement = (DataType) view.getElement();
 		LinkedList<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor> result = new LinkedList<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor>();
 		result.addAll(getContainedTypeModelFacetLinks_Generalization_4001(modelElement));
 		result.addAll(getOutgoingTypeModelFacetLinks_Dependency_4002(modelElement));
 		result.addAll(getOutgoingTypeModelFacetLinks_Association_4005(modelElement));
 		result.addAll(getOutgoingTypeModelFacetLinks_Realization_4010(modelElement));
 		result.addAll(getContainedTypeModelFacetLinks_Generalization_4011(modelElement));
 		result.addAll(getOutgoingTypeModelFacetLinks_Usage_4013(modelElement));
 		result.addAll(getOutgoingTypeModelFacetLinks_TemplateBinding_4016(modelElement));
 		return result;
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor> getPrimitiveType_3037OutgoingLinks(View view) {
 		PrimitiveType modelElement = (PrimitiveType) view.getElement();
 		LinkedList<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor> result = new LinkedList<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor>();
 		result.addAll(getContainedTypeModelFacetLinks_Generalization_4001(modelElement));
 		result.addAll(getOutgoingTypeModelFacetLinks_Dependency_4002(modelElement));
 		result.addAll(getOutgoingTypeModelFacetLinks_Association_4005(modelElement));
 		result.addAll(getOutgoingTypeModelFacetLinks_Realization_4010(modelElement));
 		result.addAll(getContainedTypeModelFacetLinks_Generalization_4011(modelElement));
 		result.addAll(getOutgoingTypeModelFacetLinks_Usage_4013(modelElement));
 		result.addAll(getOutgoingTypeModelFacetLinks_TemplateBinding_4016(modelElement));
 		return result;
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor> getInterface_3042OutgoingLinks(View view) {
 		Interface modelElement = (Interface) view.getElement();
 		LinkedList<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor> result = new LinkedList<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor>();
 		result.addAll(getContainedTypeModelFacetLinks_Generalization_4001(modelElement));
 		result.addAll(getOutgoingTypeModelFacetLinks_Dependency_4002(modelElement));
 		result.addAll(getOutgoingTypeModelFacetLinks_Association_4005(modelElement));
 		result.addAll(getOutgoingTypeModelFacetLinks_Realization_4010(modelElement));
 		result.addAll(getContainedTypeModelFacetLinks_Generalization_4011(modelElement));
 		result.addAll(getOutgoingTypeModelFacetLinks_Usage_4013(modelElement));
 		result.addAll(getOutgoingTypeModelFacetLinks_TemplateBinding_4016(modelElement));
 		return result;
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor> getInterface_3043OutgoingLinks(View view) {
 		Interface modelElement = (Interface) view.getElement();
 		LinkedList<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor> result = new LinkedList<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor>();
 		result.addAll(getContainedTypeModelFacetLinks_Generalization_4001(modelElement));
 		result.addAll(getOutgoingTypeModelFacetLinks_Dependency_4002(modelElement));
 		result.addAll(getOutgoingTypeModelFacetLinks_Association_4005(modelElement));
 		result.addAll(getOutgoingTypeModelFacetLinks_Realization_4010(modelElement));
 		result.addAll(getContainedTypeModelFacetLinks_Generalization_4011(modelElement));
 		result.addAll(getOutgoingTypeModelFacetLinks_Usage_4013(modelElement));
 		result.addAll(getOutgoingTypeModelFacetLinks_TemplateBinding_4016(modelElement));
 		return result;
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor> getLiteralString_3038OutgoingLinks(View view) {
 		return Collections.emptyList();
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor> getLiteralInteger_3039OutgoingLinks(View view) {
 		return Collections.emptyList();
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor> getExpression_3040OutgoingLinks(View view) {
 		return Collections.emptyList();
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor> getGeneralization_4001OutgoingLinks(View view) {
 		Generalization modelElement = (Generalization) view.getElement();
 		LinkedList<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor> result = new LinkedList<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor>();
 		result.addAll(getOutgoingFeatureModelFacetLinks_Generalization_General_4012(modelElement));
 		return result;
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor> getDependency_4002OutgoingLinks(View view) {
 		Dependency modelElement = (Dependency) view.getElement();
 		LinkedList<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor> result = new LinkedList<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor>();
 		result.addAll(getOutgoingTypeModelFacetLinks_Dependency_4002(modelElement));
 		result.addAll(getOutgoingFeatureModelFacetLinks_Dependency_Supplier_4006(modelElement));
 		result.addAll(getOutgoingFeatureModelFacetLinks_Dependency_Client_4007(modelElement));
 		result.addAll(getOutgoingTypeModelFacetLinks_Realization_4010(modelElement));
 		result.addAll(getOutgoingTypeModelFacetLinks_Usage_4013(modelElement));
 		return result;
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor> getProperty_4003OutgoingLinks(View view) {
 		Property modelElement = (Property) view.getElement();
 		LinkedList<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor> result = new LinkedList<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor>();
 		result.addAll(getOutgoingTypeModelFacetLinks_Dependency_4002(modelElement));
 		result.addAll(getOutgoingTypeModelFacetLinks_Realization_4010(modelElement));
 		result.addAll(getOutgoingTypeModelFacetLinks_Usage_4013(modelElement));
 		return result;
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor> getAssociation_4005OutgoingLinks(View view) {
 		Association modelElement = (Association) view.getElement();
 		LinkedList<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor> result = new LinkedList<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor>();
 		result.addAll(getContainedTypeModelFacetLinks_Generalization_4001(modelElement));
 		result.addAll(getOutgoingTypeModelFacetLinks_Dependency_4002(modelElement));
 		result.addAll(getContainedTypeModelFacetLinks_Property_4003(modelElement));
 		result.addAll(getOutgoingTypeModelFacetLinks_Association_4005(modelElement));
 		result.addAll(getOutgoingTypeModelFacetLinks_Realization_4010(modelElement));
 		result.addAll(getContainedTypeModelFacetLinks_Generalization_4011(modelElement));
 		result.addAll(getOutgoingTypeModelFacetLinks_Usage_4013(modelElement));
 		result.addAll(getOutgoingTypeModelFacetLinks_TemplateBinding_4016(modelElement));
 		return result;
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor> getInterfaceRealization_4008OutgoingLinks(View view) {
 		InterfaceRealization modelElement = (InterfaceRealization) view.getElement();
 		LinkedList<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor> result = new LinkedList<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor>();
 		result.addAll(getOutgoingTypeModelFacetLinks_Dependency_4002(modelElement));
 		result.addAll(getOutgoingFeatureModelFacetLinks_Dependency_Supplier_4006(modelElement));
 		result.addAll(getOutgoingFeatureModelFacetLinks_Dependency_Client_4007(modelElement));
 		result.addAll(getOutgoingTypeModelFacetLinks_Realization_4010(modelElement));
 		result.addAll(getOutgoingTypeModelFacetLinks_Usage_4013(modelElement));
 		return result;
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor> getRealization_4010OutgoingLinks(View view) {
 		Realization modelElement = (Realization) view.getElement();
 		LinkedList<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor> result = new LinkedList<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor>();
 		result.addAll(getOutgoingTypeModelFacetLinks_Dependency_4002(modelElement));
 		result.addAll(getOutgoingFeatureModelFacetLinks_Dependency_Supplier_4006(modelElement));
 		result.addAll(getOutgoingFeatureModelFacetLinks_Dependency_Client_4007(modelElement));
 		result.addAll(getOutgoingTypeModelFacetLinks_Realization_4010(modelElement));
 		result.addAll(getOutgoingTypeModelFacetLinks_Usage_4013(modelElement));
 		return result;
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor> getGeneralization_4011OutgoingLinks(View view) {
 		Generalization modelElement = (Generalization) view.getElement();
 		LinkedList<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor> result = new LinkedList<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor>();
 		result.addAll(getOutgoingFeatureModelFacetLinks_Generalization_General_4012(modelElement));
 		return result;
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor> getUsage_4013OutgoingLinks(View view) {
 		Usage modelElement = (Usage) view.getElement();
 		LinkedList<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor> result = new LinkedList<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor>();
 		result.addAll(getOutgoingTypeModelFacetLinks_Dependency_4002(modelElement));
 		result.addAll(getOutgoingFeatureModelFacetLinks_Dependency_Supplier_4006(modelElement));
 		result.addAll(getOutgoingFeatureModelFacetLinks_Dependency_Client_4007(modelElement));
 		result.addAll(getOutgoingTypeModelFacetLinks_Realization_4010(modelElement));
 		result.addAll(getOutgoingTypeModelFacetLinks_Usage_4013(modelElement));
 		return result;
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor> getAssociationClass_4014OutgoingLinks(View view) {
 		AssociationClass modelElement = (AssociationClass) view.getElement();
 		LinkedList<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor> result = new LinkedList<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor>();
 		result.addAll(getContainedTypeModelFacetLinks_Generalization_4001(modelElement));
 		result.addAll(getOutgoingTypeModelFacetLinks_Dependency_4002(modelElement));
 		result.addAll(getContainedTypeModelFacetLinks_Property_4003(modelElement));
 		result.addAll(getOutgoingTypeModelFacetLinks_Association_4005(modelElement));
 		result.addAll(getOutgoingTypeModelFacetLinks_InterfaceRealization_4008(modelElement));
 		result.addAll(getOutgoingTypeModelFacetLinks_Realization_4010(modelElement));
 		result.addAll(getContainedTypeModelFacetLinks_Generalization_4011(modelElement));
 		result.addAll(getOutgoingTypeModelFacetLinks_Usage_4013(modelElement));
 		result.addAll(getOutgoingTypeModelFacetLinks_TemplateBinding_4016(modelElement));
 		return result;
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor> getSlot_4015OutgoingLinks(View view) {
 		return Collections.emptyList();
 	}
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor> getTemplateBinding_4016OutgoingLinks(View view) {
 		return Collections.emptyList();
 	}
 
 	/**
 	 * @generated
 	 */
 	private static Collection<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor> getContainedTypeModelFacetLinks_Generalization_4001(Classifier container) {
 		LinkedList<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor> result = new LinkedList<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor>();
 		for (Iterator<?> links = container.getGeneralizations().iterator(); links.hasNext();) {
 			EObject linkObject = (EObject) links.next();
 			if (false == linkObject instanceof Generalization) {
 				continue;
 			}
 			Generalization link = (Generalization) linkObject;
 			if (GeneralizationEditPart.VISUAL_ID != UMLVisualIDRegistry.getLinkWithClassVisualID(link)) {
 				continue;
 			}
 			Classifier dst = link.getGeneral();
 			result.add(new org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor(container, dst, link, UMLElementTypes.Generalization_4001, GeneralizationEditPart.VISUAL_ID));
 		}
 		return result;
 	}
 
 	/**
 	 * @generated
 	 */
 	private static Collection<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor> getContainedTypeModelFacetLinks_Dependency_4002(Package container) {
 		LinkedList<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor> result = new LinkedList<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor>();
 		for (Iterator<?> links = container.getPackagedElements().iterator(); links.hasNext();) {
 			EObject linkObject = (EObject) links.next();
 			if (false == linkObject instanceof Dependency) {
 				continue;
 			}
 			Dependency link = (Dependency) linkObject;
 			if (Dependency2EditPart.VISUAL_ID != UMLVisualIDRegistry.getLinkWithClassVisualID(link)) {
 				continue;
 			}
 			List targets = link.getSuppliers();
 			Object theTarget = targets.size() == 1 ? targets.get(0) : null;
 			if (false == theTarget instanceof NamedElement) {
 				continue;
 			}
 			NamedElement dst = (NamedElement) theTarget;
 			List sources = link.getClients();
 			Object theSource = sources.size() == 1 ? sources.get(0) : null;
 			if (false == theSource instanceof NamedElement) {
 				continue;
 			}
 			NamedElement src = (NamedElement) theSource;
 			result.add(new org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor(src, dst, link, UMLElementTypes.Dependency_4002, Dependency2EditPart.VISUAL_ID));
 		}
 		return result;
 	}
 
 	/**
 	 * We wants to create links for any member ends, not only owned ends.
 	 * Unfortunately, its not possible to set not containment feature in the GMF
 	 * map
 	 * 
 	 * @generated NOT
 	 */
 	private static Collection<UMLLinkDescriptor> getContainedTypeModelFacetLinks_Property_4003(Association container) {
 		Collection<UMLLinkDescriptor> result = new LinkedList<UMLLinkDescriptor>();
 		for (Property link : container.getMemberEnds()) { //getOwnedEnds -> getMemberEnds -- sic
 			if (Property7EditPart.VISUAL_ID != UMLVisualIDRegistry.getLinkWithClassVisualID(link)) {
 				continue;
 			}
 			Type dst = link.getType();
 			result.add(new UMLLinkDescriptor(container, dst, link, UMLElementTypes.Property_4003, Property7EditPart.VISUAL_ID));
 		}
 		return result;
 	}
 
 	/**
 	 * @generated NOT
 	 */
 	private static Collection<UMLLinkDescriptor> getContainedTypeModelFacetLinks_Association_4005(Package container) {
 		Collection<UMLLinkDescriptor> result = new LinkedList<UMLLinkDescriptor>();
 		for (PackageableElement linkObject : container.getPackagedElements()) {
 			if (false == linkObject instanceof Association) {
 				continue;
 			}
 			Association association = (Association) linkObject;
 			if (AssociationEditPart.VISUAL_ID != UMLVisualIDRegistry.getLinkWithClassVisualID(association)) {
 				continue;
 			}
 			if (association.isBinary()) {
 				Property sourceEnd = AssociationEndConvention.getSourceEnd(association);
 				Property targetEnd = AssociationEndConvention.getTargetEnd(association);
 				EObject gmfSource = sourceEnd.getType();
 				EObject gmfTarget = targetEnd.getType();
 				result.add(new UMLLinkDescriptor(gmfSource, gmfTarget, association, UMLElementTypes.Association_4005, AssociationEditPart.VISUAL_ID));
 			}
 		}
 		return result;
 	}
 
 	/**
 	 * @generated NOT
 	 */
 	private static Collection<UMLLinkDescriptor> getIncomingTypeModelFacetLinks_Association_4005(Type target, Map<EObject, Collection<EStructuralFeature.Setting>> crossReferences) {
 		return findRelatedAssociations(target, false);
 	}
 
 	/**
 	 * @generated NOT
 	 */
 	private static Collection<UMLLinkDescriptor> getOutgoingTypeModelFacetLinks_Association_4005(Type source) {
 		return findRelatedAssociations(source, true);
 	}
 
 	/**
 	 * @NOT-GENERATED
 	 */
 	private static Collection<UMLLinkDescriptor> findRelatedAssociations(Type type, boolean sourceNotTarget) {
 		Package container = type.getNearestPackage();
 		if (container == null) {
 			return Collections.emptyList();
 		}
 
 		List<UMLLinkDescriptor> result = new LinkedList<UMLLinkDescriptor>();
 		for (PackageableElement next : container.getPackagedElements()) {
 			if (false == next instanceof Association) {
 				continue;
 			}
 			if (AssociationEditPart.VISUAL_ID != UMLVisualIDRegistry.getLinkWithClassVisualID(next)) {
 				continue;
 			}
 			Association link = (Association) next;
 			Property sourceEnd = AssociationEndConvention.getSourceEnd(link);
 			Property targetEnd = AssociationEndConvention.getTargetEnd(link);
 
 			if (sourceEnd == null || targetEnd == null) {
 				continue;
 			}
 
 			Property subjectEnd = sourceNotTarget ? sourceEnd : targetEnd;
 			if (!type.equals(subjectEnd.getType())) {
 				continue;
 			}
 
 			EObject gmfSource = sourceEnd.getType();
 			EObject gmfTarget = targetEnd.getType();
 			result.add(new UMLLinkDescriptor(gmfSource, gmfTarget, link, UMLElementTypes.Association_4005, AssociationEditPart.VISUAL_ID));
 		}
 		return result;
 	}
 
 	/**
 	 * @generated NOT
 	 */
 	private static Collection<UMLLinkDescriptor> getContainedTypeModelFacetLinks_InterfaceRealization_4008(BehavioredClassifier container) {
 		Collection<UMLLinkDescriptor> result = new LinkedList<UMLLinkDescriptor>();
 		result.addAll(getContainedTypeModelFacetLinks_InterfaceRealization_4008Gen(container));
		result.addAll(getContainedTypeModelFacetLinks_InterfaceRealization_4008_ForAllClassifiers(container));
 		return result;
 	}
 
 	/**
 	 * @generated NOT
 	 */
 	private static Collection<UMLLinkDescriptor> getContainedTypeModelFacetLinks_InterfaceRealization_4008_ForAllClassifiers(Classifier container) {
 		Collection<UMLLinkDescriptor> result = new LinkedList<UMLLinkDescriptor>();
 
 		for (Relationship linkObject : container.getRelationships(UMLPackage.eINSTANCE.getInterfaceRealization())) {
 			if (false == linkObject instanceof InterfaceRealization) {
 				continue;
 			}
 			InterfaceRealization link = (InterfaceRealization) linkObject;
 			if (InterfaceRealizationEditPart.VISUAL_ID != UMLVisualIDRegistry.getLinkWithClassVisualID(link)) {
 				continue;
 			}
 			EList<NamedElement> targets = link.getSuppliers();
 			if (targets.size() != 1) {
 				continue;
 			}
 			if (link.getClients().contains(container)) {
 				result.add(new UMLLinkDescriptor(container, targets.get(0), link, UMLElementTypes.InterfaceRealization_4008, InterfaceRealizationEditPart.VISUAL_ID));
 			}
 		}
 		return result;
 	}
 
 	/**
 	 * @generated
 	 */
 	private static Collection<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor> getContainedTypeModelFacetLinks_InterfaceRealization_4008Gen(BehavioredClassifier container) {
 		LinkedList<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor> result = new LinkedList<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor>();
 		for (Iterator<?> links = container.getInterfaceRealizations().iterator(); links.hasNext();) {
 			EObject linkObject = (EObject) links.next();
 			if (false == linkObject instanceof InterfaceRealization) {
 				continue;
 			}
 			InterfaceRealization link = (InterfaceRealization) linkObject;
 			if (InterfaceRealizationEditPart.VISUAL_ID != UMLVisualIDRegistry.getLinkWithClassVisualID(link)) {
 				continue;
 			}
 			Interface dst = link.getContract();
 			BehavioredClassifier src = link.getImplementingClassifier();
 			result.add(new org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor(src, dst, link, UMLElementTypes.InterfaceRealization_4008, InterfaceRealizationEditPart.VISUAL_ID));
 		}
 		return result;
 	}
 
 	/**
 	 * @generated
 	 */
 	private static Collection<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor> getContainedTypeModelFacetLinks_Realization_4010(Package container) {
 		LinkedList<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor> result = new LinkedList<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor>();
 		for (Iterator<?> links = container.getPackagedElements().iterator(); links.hasNext();) {
 			EObject linkObject = (EObject) links.next();
 			if (false == linkObject instanceof Realization) {
 				continue;
 			}
 			Realization link = (Realization) linkObject;
 			if (RealizationEditPart.VISUAL_ID != UMLVisualIDRegistry.getLinkWithClassVisualID(link)) {
 				continue;
 			}
 			List targets = link.getSuppliers();
 			Object theTarget = targets.size() == 1 ? targets.get(0) : null;
 			if (false == theTarget instanceof NamedElement) {
 				continue;
 			}
 			NamedElement dst = (NamedElement) theTarget;
 			List sources = link.getClients();
 			Object theSource = sources.size() == 1 ? sources.get(0) : null;
 			if (false == theSource instanceof NamedElement) {
 				continue;
 			}
 			NamedElement src = (NamedElement) theSource;
 			result.add(new org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor(src, dst, link, UMLElementTypes.Realization_4010, RealizationEditPart.VISUAL_ID));
 		}
 		return result;
 	}
 
 	/**
 	 * @generated
 	 */
 	private static Collection<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor> getContainedTypeModelFacetLinks_Generalization_4011(Classifier container) {
 		LinkedList<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor> result = new LinkedList<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor>();
 		for (Iterator<?> links = container.getGeneralizations().iterator(); links.hasNext();) {
 			EObject linkObject = (EObject) links.next();
 			if (false == linkObject instanceof Generalization) {
 				continue;
 			}
 			Generalization link = (Generalization) linkObject;
 			if (Generalization2EditPart.VISUAL_ID != UMLVisualIDRegistry.getLinkWithClassVisualID(link)) {
 				continue;
 			}
 			List targets = link.getGeneralizationSets();
 			Object theTarget = targets.size() == 1 ? targets.get(0) : null;
 			if (false == theTarget instanceof GeneralizationSet) {
 				continue;
 			}
 			GeneralizationSet dst = (GeneralizationSet) theTarget;
 			result.add(new org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor(container, dst, link, UMLElementTypes.Generalization_4011, Generalization2EditPart.VISUAL_ID));
 		}
 		return result;
 	}
 
 	/**
 	 * @generated
 	 */
 	private static Collection<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor> getContainedTypeModelFacetLinks_Usage_4013(Package container) {
 		LinkedList<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor> result = new LinkedList<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor>();
 		for (Iterator<?> links = container.getPackagedElements().iterator(); links.hasNext();) {
 			EObject linkObject = (EObject) links.next();
 			if (false == linkObject instanceof Usage) {
 				continue;
 			}
 			Usage link = (Usage) linkObject;
 			if (UsageEditPart.VISUAL_ID != UMLVisualIDRegistry.getLinkWithClassVisualID(link)) {
 				continue;
 			}
 			List targets = link.getSuppliers();
 			Object theTarget = targets.size() == 1 ? targets.get(0) : null;
 			if (false == theTarget instanceof NamedElement) {
 				continue;
 			}
 			NamedElement dst = (NamedElement) theTarget;
 			List sources = link.getClients();
 			Object theSource = sources.size() == 1 ? sources.get(0) : null;
 			if (false == theSource instanceof NamedElement) {
 				continue;
 			}
 			NamedElement src = (NamedElement) theSource;
 			result.add(new org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor(src, dst, link, UMLElementTypes.Usage_4013, UsageEditPart.VISUAL_ID));
 		}
 		return result;
 	}
 
 	/**
 	 * This link is connector between association class' rhomb and rectangle
 	 * parts CollaborationUse's were selected in the gmfmap instead of not
 	 * allowed "null" features, because they are completely unrelated and can
 	 * not interfere with link-related editpolicies.
 	 * 
 	 * In the ideal world we would prefere to specify either custom relationship
 	 * between AssociationClass and source/target for this link or don't specify
 	 * these features at all.
 	 * 
 	 * In this method we are going to create link descriptor with identical
 	 * semantic elements (association class itself for source, target and link
 	 * itself). The selection of the node's for source/target is made in the
 	 * custom code of the PackageCanonicalEditPolicy#getSourceEditPart(...)/
 	 * PackageCanonicalEditPolicy#getTargetEditPart(...) methods.
 	 * 
 	 * @generated NOT
 	 */
 	private static Collection<UMLLinkDescriptor> getContainedTypeModelFacetLinks_AssociationClass_4014(Package container) {
 		Collection<UMLLinkDescriptor> result = new LinkedList<UMLLinkDescriptor>();
 		for (PackageableElement linkObject : container.getPackagedElements()) {
 			if (false == linkObject instanceof AssociationClass) {
 				continue;
 			}
 			AssociationClass link = (AssociationClass) linkObject;
 			if (AssociationClassConnectorEditPart.VISUAL_ID != UMLVisualIDRegistry.getLinkWithClassVisualID(link)) {
 				continue;
 			}
 
 			//the same link-element, source and target
 			AssociationClass src = link;
 			AssociationClass dst = link;
 			result.add(new UMLLinkDescriptor(src, dst, link, UMLElementTypes.AssociationClass_4014, AssociationClassConnectorEditPart.VISUAL_ID));
 		}
 		return result;
 	}
 
 	/**
 	 * @generated NOT
 	 */
 	private static Collection<UMLLinkDescriptor> getContainedTypeModelFacetLinks_Slot_4015(InstanceSpecification container) {
 		return getOutgoingTypeModelFacetLinks_Slot_4015(container);
 	}
 
 	/**
 	 * @generated NOT
 	 */
 	private static Collection<UMLLinkDescriptor> getContainedTypeModelFacetLinks_TemplateBinding_4016(TemplateableElement container) {
 		Collection<UMLLinkDescriptor> result = new LinkedList<UMLLinkDescriptor>();
 		for (TemplateBinding link : container.getTemplateBindings()) {
 			if (TemplateBindingEditPart.VISUAL_ID != UMLVisualIDRegistry.getLinkWithClassVisualID(link)) {
 				continue;
 			}
 			TemplateableElement dst = link.getSignature().getTemplate();
 			TemplateableElement src = link.getBoundElement();
 			result.add(new UMLLinkDescriptor(src, dst, link, UMLElementTypes.TemplateBinding_4016, TemplateBindingEditPart.VISUAL_ID));
 		}
 		return result;
 	}
 
 	/**
 	 * @generated
 	 */
 	private static Collection<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor> getIncomingTypeModelFacetLinks_Generalization_4001(Classifier target,
 			Map<EObject, Collection<EStructuralFeature.Setting>> crossReferences) {
 		LinkedList<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor> result = new LinkedList<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor>();
 		Collection<EStructuralFeature.Setting> settings = crossReferences.get(target);
 		for (EStructuralFeature.Setting setting : settings) {
 			if (setting.getEStructuralFeature() != UMLPackage.eINSTANCE.getGeneralization_General() || false == setting.getEObject() instanceof Generalization) {
 				continue;
 			}
 			Generalization link = (Generalization) setting.getEObject();
 			if (GeneralizationEditPart.VISUAL_ID != UMLVisualIDRegistry.getLinkWithClassVisualID(link)) {
 				continue;
 			}
 			if (false == link.eContainer() instanceof Classifier) {
 				continue;
 			}
 			Classifier container = (Classifier) link.eContainer();
 			result.add(new org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor(container, target, link, UMLElementTypes.Generalization_4001, GeneralizationEditPart.VISUAL_ID));
 
 		}
 		return result;
 	}
 
 	/**
 	 * @generated
 	 */
 	private static Collection<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor> getIncomingTypeModelFacetLinks_Dependency_4002(NamedElement target,
 			Map<EObject, Collection<EStructuralFeature.Setting>> crossReferences) {
 		LinkedList<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor> result = new LinkedList<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor>();
 		Collection<EStructuralFeature.Setting> settings = crossReferences.get(target);
 		for (EStructuralFeature.Setting setting : settings) {
 			if (setting.getEStructuralFeature() != UMLPackage.eINSTANCE.getDependency_Supplier() || false == setting.getEObject() instanceof Dependency) {
 				continue;
 			}
 			Dependency link = (Dependency) setting.getEObject();
 			if (Dependency2EditPart.VISUAL_ID != UMLVisualIDRegistry.getLinkWithClassVisualID(link)) {
 				continue;
 			}
 			List sources = link.getClients();
 			Object theSource = sources.size() == 1 ? sources.get(0) : null;
 			if (false == theSource instanceof NamedElement) {
 				continue;
 			}
 			NamedElement src = (NamedElement) theSource;
 			result.add(new org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor(src, target, link, UMLElementTypes.Dependency_4002, Dependency2EditPart.VISUAL_ID));
 		}
 		return result;
 	}
 
 	/**
 	 * @generated
 	 */
 	private static Collection<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor> getIncomingTypeModelFacetLinks_Property_4003(Type target,
 			Map<EObject, Collection<EStructuralFeature.Setting>> crossReferences) {
 		LinkedList<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor> result = new LinkedList<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor>();
 		Collection<EStructuralFeature.Setting> settings = crossReferences.get(target);
 		for (EStructuralFeature.Setting setting : settings) {
 			if (setting.getEStructuralFeature() != UMLPackage.eINSTANCE.getTypedElement_Type() || false == setting.getEObject() instanceof Property) {
 				continue;
 			}
 			Property link = (Property) setting.getEObject();
 			if (Property7EditPart.VISUAL_ID != UMLVisualIDRegistry.getLinkWithClassVisualID(link)) {
 				continue;
 			}
 			if (false == link.eContainer() instanceof Association) {
 				continue;
 			}
 			Association container = (Association) link.eContainer();
 			result.add(new org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor(container, target, link, UMLElementTypes.Property_4003, Property7EditPart.VISUAL_ID));
 
 		}
 		return result;
 	}
 
 	/**
 	 * @generated
 	 */
 	private static Collection<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor> getIncomingFeatureModelFacetLinks_Constraint_ConstrainedElement_4004(Element target,
 			Map<EObject, Collection<EStructuralFeature.Setting>> crossReferences) {
 		LinkedList<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor> result = new LinkedList<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor>();
 		Collection<EStructuralFeature.Setting> settings = crossReferences.get(target);
 		for (EStructuralFeature.Setting setting : settings) {
 			if (setting.getEStructuralFeature() == UMLPackage.eINSTANCE.getConstraint_ConstrainedElement()) {
 				result.add(new org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor(setting.getEObject(), target, UMLElementTypes.ConstraintConstrainedElement_4004,
 						ConstraintConstrainedElementEditPart.VISUAL_ID));
 			}
 		}
 		return result;
 	}
 
 	/**
 	 * @generated
 	 */
 	private static Collection<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor> getIncomingFeatureModelFacetLinks_Dependency_Supplier_4006(NamedElement target,
 			Map<EObject, Collection<EStructuralFeature.Setting>> crossReferences) {
 		LinkedList<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor> result = new LinkedList<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor>();
 		Collection<EStructuralFeature.Setting> settings = crossReferences.get(target);
 		for (EStructuralFeature.Setting setting : settings) {
 			if (setting.getEStructuralFeature() == UMLPackage.eINSTANCE.getDependency_Supplier()) {
 				result.add(new org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor(setting.getEObject(), target, UMLElementTypes.DependencySupplier_4006, DependencySupplierEditPart.VISUAL_ID));
 			}
 		}
 		return result;
 	}
 
 	/**
 	 * @generated
 	 */
 	private static Collection<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor> getIncomingFeatureModelFacetLinks_Dependency_Client_4007(NamedElement target,
 			Map<EObject, Collection<EStructuralFeature.Setting>> crossReferences) {
 		LinkedList<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor> result = new LinkedList<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor>();
 		Collection<EStructuralFeature.Setting> settings = crossReferences.get(target);
 		for (EStructuralFeature.Setting setting : settings) {
 			if (setting.getEStructuralFeature() == UMLPackage.eINSTANCE.getDependency_Client()) {
 				result.add(new org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor(setting.getEObject(), target, UMLElementTypes.DependencyClient_4007, DependencyClientEditPart.VISUAL_ID));
 			}
 		}
 		return result;
 	}
 
 	/**
 	 * @generated
 	 */
 	private static Collection<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor> getIncomingTypeModelFacetLinks_InterfaceRealization_4008(Interface target,
 			Map<EObject, Collection<EStructuralFeature.Setting>> crossReferences) {
 		LinkedList<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor> result = new LinkedList<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor>();
 		Collection<EStructuralFeature.Setting> settings = crossReferences.get(target);
 		for (EStructuralFeature.Setting setting : settings) {
 			if (setting.getEStructuralFeature() != UMLPackage.eINSTANCE.getInterfaceRealization_Contract() || false == setting.getEObject() instanceof InterfaceRealization) {
 				continue;
 			}
 			InterfaceRealization link = (InterfaceRealization) setting.getEObject();
 			if (InterfaceRealizationEditPart.VISUAL_ID != UMLVisualIDRegistry.getLinkWithClassVisualID(link)) {
 				continue;
 			}
 			BehavioredClassifier src = link.getImplementingClassifier();
 			result.add(new org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor(src, target, link, UMLElementTypes.InterfaceRealization_4008, InterfaceRealizationEditPart.VISUAL_ID));
 		}
 		return result;
 	}
 
 	/**
 	 * @generated
 	 */
 	private static Collection<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor> getIncomingTypeModelFacetLinks_Realization_4010(NamedElement target,
 			Map<EObject, Collection<EStructuralFeature.Setting>> crossReferences) {
 		LinkedList<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor> result = new LinkedList<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor>();
 		Collection<EStructuralFeature.Setting> settings = crossReferences.get(target);
 		for (EStructuralFeature.Setting setting : settings) {
 			if (setting.getEStructuralFeature() != UMLPackage.eINSTANCE.getDependency_Supplier() || false == setting.getEObject() instanceof Realization) {
 				continue;
 			}
 			Realization link = (Realization) setting.getEObject();
 			if (RealizationEditPart.VISUAL_ID != UMLVisualIDRegistry.getLinkWithClassVisualID(link)) {
 				continue;
 			}
 			List sources = link.getClients();
 			Object theSource = sources.size() == 1 ? sources.get(0) : null;
 			if (false == theSource instanceof NamedElement) {
 				continue;
 			}
 			NamedElement src = (NamedElement) theSource;
 			result.add(new org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor(src, target, link, UMLElementTypes.Realization_4010, RealizationEditPart.VISUAL_ID));
 		}
 		return result;
 	}
 
 	/**
 	 * @generated
 	 */
 	private static Collection<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor> getIncomingTypeModelFacetLinks_Generalization_4011(GeneralizationSet target,
 			Map<EObject, Collection<EStructuralFeature.Setting>> crossReferences) {
 		LinkedList<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor> result = new LinkedList<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor>();
 		Collection<EStructuralFeature.Setting> settings = crossReferences.get(target);
 		for (EStructuralFeature.Setting setting : settings) {
 			if (setting.getEStructuralFeature() != UMLPackage.eINSTANCE.getGeneralization_GeneralizationSet() || false == setting.getEObject() instanceof Generalization) {
 				continue;
 			}
 			Generalization link = (Generalization) setting.getEObject();
 			if (Generalization2EditPart.VISUAL_ID != UMLVisualIDRegistry.getLinkWithClassVisualID(link)) {
 				continue;
 			}
 			if (false == link.eContainer() instanceof Classifier) {
 				continue;
 			}
 			Classifier container = (Classifier) link.eContainer();
 			result.add(new org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor(container, target, link, UMLElementTypes.Generalization_4011, Generalization2EditPart.VISUAL_ID));
 
 		}
 		return result;
 	}
 
 	/**
 	 * @generated
 	 */
 	private static Collection<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor> getIncomingFeatureModelFacetLinks_Generalization_General_4012(Classifier target,
 			Map<EObject, Collection<EStructuralFeature.Setting>> crossReferences) {
 		LinkedList<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor> result = new LinkedList<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor>();
 		Collection<EStructuralFeature.Setting> settings = crossReferences.get(target);
 		for (EStructuralFeature.Setting setting : settings) {
 			if (setting.getEStructuralFeature() == UMLPackage.eINSTANCE.getGeneralization_General()) {
 				result.add(new org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor(setting.getEObject(), target, UMLElementTypes.GeneralizationGeneral_4012, GeneralizationGeneralEditPart.VISUAL_ID));
 			}
 		}
 		return result;
 	}
 
 	/**
 	 * @generated
 	 */
 	private static Collection<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor> getIncomingTypeModelFacetLinks_Usage_4013(NamedElement target,
 			Map<EObject, Collection<EStructuralFeature.Setting>> crossReferences) {
 		LinkedList<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor> result = new LinkedList<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor>();
 		Collection<EStructuralFeature.Setting> settings = crossReferences.get(target);
 		for (EStructuralFeature.Setting setting : settings) {
 			if (setting.getEStructuralFeature() != UMLPackage.eINSTANCE.getDependency_Supplier() || false == setting.getEObject() instanceof Usage) {
 				continue;
 			}
 			Usage link = (Usage) setting.getEObject();
 			if (UsageEditPart.VISUAL_ID != UMLVisualIDRegistry.getLinkWithClassVisualID(link)) {
 				continue;
 			}
 			List sources = link.getClients();
 			Object theSource = sources.size() == 1 ? sources.get(0) : null;
 			if (false == theSource instanceof NamedElement) {
 				continue;
 			}
 			NamedElement src = (NamedElement) theSource;
 			result.add(new org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor(src, target, link, UMLElementTypes.Usage_4013, UsageEditPart.VISUAL_ID));
 		}
 		return result;
 	}
 
 	/**
 	 * @generated NOT
 	 */
 	private static Collection<UMLLinkDescriptor> getIncomingTypeModelFacetLinks_Slot_4015(InstanceSpecification target, Map<EObject, Collection<EStructuralFeature.Setting>> crossReferences) {
 		Collection<UMLLinkDescriptor> result = new LinkedList<UMLLinkDescriptor>();
 		for (EStructuralFeature.Setting setting : crossReferences.get(target)) {
 			if (setting.getEStructuralFeature() != UMLPackage.eINSTANCE.getInstanceValue_Instance() || false == setting.getEObject() instanceof InstanceValue) {
 				continue;
 			}
 			InstanceValue instanceValue = (InstanceValue) setting.getEObject();
 			if (false == instanceValue.eContainer() instanceof Slot) {
 				continue;
 			}
 			Slot link = (Slot) instanceValue.eContainer();
 			if (AssociationInstanceEditPart.VISUAL_ID != UMLVisualIDRegistry.getLinkWithClassVisualID(link)) {
 				continue;
 			}
 			InstanceSpecification src = link.getOwningInstance();
 			result.add(new UMLLinkDescriptor(src, target, link, UMLElementTypes.Slot_4015, AssociationInstanceEditPart.VISUAL_ID));
 		}
 		return result;
 	}
 
 	/**
 	 * @generated
 	 */
 	private static Collection<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor> getIncomingTypeModelFacetLinks_TemplateBinding_4016(TemplateableElement target,
 			Map<EObject, Collection<EStructuralFeature.Setting>> crossReferences) {
 		LinkedList<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor> result = new LinkedList<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor>();
 		Collection<EStructuralFeature.Setting> settings = crossReferences.get(target);
 		for (EStructuralFeature.Setting setting : settings) {
 			if (setting.getEStructuralFeature() != UMLPackage.eINSTANCE.getTemplateSignature_Template() || false == setting.getEObject() instanceof TemplateBinding) {
 				continue;
 			}
 			TemplateBinding link = (TemplateBinding) setting.getEObject();
 			if (TemplateBindingEditPart.VISUAL_ID != UMLVisualIDRegistry.getLinkWithClassVisualID(link)) {
 				continue;
 			}
 			TemplateableElement src = link.getBoundElement();
 			result.add(new org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor(src, target, link, UMLElementTypes.TemplateBinding_4016, TemplateBindingEditPart.VISUAL_ID));
 		}
 		return result;
 	}
 
 	/**
 	 * @generated
 	 */
 	private static Collection<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor> getIncomingFeatureModelFacetLinks_Port_Provided_4017(Interface target,
 			Map<EObject, Collection<EStructuralFeature.Setting>> crossReferences) {
 		LinkedList<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor> result = new LinkedList<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor>();
 		Collection<EStructuralFeature.Setting> settings = crossReferences.get(target);
 		for (EStructuralFeature.Setting setting : settings) {
 			if (setting.getEStructuralFeature() == UMLPackage.eINSTANCE.getPort_Provided()) {
 				result.add(new org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor(setting.getEObject(), target, UMLElementTypes.PortProvided_4017, PortProvidedEditPart.VISUAL_ID));
 			}
 		}
 		return result;
 	}
 
 	/**
 	 * @generated
 	 */
 	private static Collection<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor> getIncomingFeatureModelFacetLinks_Port_Required_4018(Interface target,
 			Map<EObject, Collection<EStructuralFeature.Setting>> crossReferences) {
 		LinkedList<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor> result = new LinkedList<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor>();
 		Collection<EStructuralFeature.Setting> settings = crossReferences.get(target);
 		for (EStructuralFeature.Setting setting : settings) {
 			if (setting.getEStructuralFeature() == UMLPackage.eINSTANCE.getPort_Required()) {
 				result.add(new org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor(setting.getEObject(), target, UMLElementTypes.PortRequired_4018, PortRequiredEditPart.VISUAL_ID));
 			}
 		}
 		return result;
 	}
 
 	/**
 	 * @generated
 	 */
 	private static Collection<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor> getIncomingFeatureModelFacetLinks_Comment_AnnotatedElement_4019(Element target,
 			Map<EObject, Collection<EStructuralFeature.Setting>> crossReferences) {
 		LinkedList<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor> result = new LinkedList<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor>();
 		Collection<EStructuralFeature.Setting> settings = crossReferences.get(target);
 		for (EStructuralFeature.Setting setting : settings) {
 			if (setting.getEStructuralFeature() == UMLPackage.eINSTANCE.getComment_AnnotatedElement()) {
 				result.add(new org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor(setting.getEObject(), target, UMLElementTypes.CommentAnnotatedElement_4019,
 						CommentAnnotatedElementEditPart.VISUAL_ID));
 			}
 		}
 		return result;
 	}
 
 	/**
 	 * @generated
 	 */
 	private static Collection<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor> getOutgoingTypeModelFacetLinks_Dependency_4002(NamedElement source) {
 		Package container = null;
 		// Find container element for the link.
 		// Climb up by containment hierarchy starting from the source
 		// and return the first element that is instance of the container class.
 		for (EObject element = source; element != null && container == null; element = element.eContainer()) {
 			if (element instanceof Package) {
 				container = (Package) element;
 			}
 		}
 		if (container == null) {
 			return Collections.emptyList();
 		}
 		LinkedList<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor> result = new LinkedList<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor>();
 		for (Iterator<?> links = container.getPackagedElements().iterator(); links.hasNext();) {
 			EObject linkObject = (EObject) links.next();
 			if (false == linkObject instanceof Dependency) {
 				continue;
 			}
 			Dependency link = (Dependency) linkObject;
 			if (Dependency2EditPart.VISUAL_ID != UMLVisualIDRegistry.getLinkWithClassVisualID(link)) {
 				continue;
 			}
 			List targets = link.getSuppliers();
 			Object theTarget = targets.size() == 1 ? targets.get(0) : null;
 			if (false == theTarget instanceof NamedElement) {
 				continue;
 			}
 			NamedElement dst = (NamedElement) theTarget;
 			List sources = link.getClients();
 			Object theSource = sources.size() == 1 ? sources.get(0) : null;
 			if (false == theSource instanceof NamedElement) {
 				continue;
 			}
 			NamedElement src = (NamedElement) theSource;
 			if (src != source) {
 				continue;
 			}
 			result.add(new org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor(src, dst, link, UMLElementTypes.Dependency_4002, Dependency2EditPart.VISUAL_ID));
 		}
 		return result;
 	}
 
 	/**
 	 * @generated
 	 */
 	private static Collection<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor> getOutgoingFeatureModelFacetLinks_Constraint_ConstrainedElement_4004(Constraint source) {
 		LinkedList<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor> result = new LinkedList<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor>();
 		for (Iterator<?> destinations = source.getConstrainedElements().iterator(); destinations.hasNext();) {
 			Element destination = (Element) destinations.next();
 			result.add(new org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor(source, destination, UMLElementTypes.ConstraintConstrainedElement_4004, ConstraintConstrainedElementEditPart.VISUAL_ID));
 		}
 		return result;
 	}
 
 	/**
 	 * @generated
 	 */
 	private static Collection<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor> getOutgoingFeatureModelFacetLinks_Dependency_Supplier_4006(Dependency source) {
 		LinkedList<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor> result = new LinkedList<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor>();
 		for (Iterator<?> destinations = source.getSuppliers().iterator(); destinations.hasNext();) {
 			NamedElement destination = (NamedElement) destinations.next();
 			result.add(new org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor(source, destination, UMLElementTypes.DependencySupplier_4006, DependencySupplierEditPart.VISUAL_ID));
 		}
 		return result;
 	}
 
 	/**
 	 * @generated
 	 */
 	private static Collection<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor> getOutgoingFeatureModelFacetLinks_Dependency_Client_4007(Dependency source) {
 		LinkedList<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor> result = new LinkedList<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor>();
 		for (Iterator<?> destinations = source.getClients().iterator(); destinations.hasNext();) {
 			NamedElement destination = (NamedElement) destinations.next();
 			result.add(new org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor(source, destination, UMLElementTypes.DependencyClient_4007, DependencyClientEditPart.VISUAL_ID));
 		}
 		return result;
 	}
 
 	/**
 	 * @generated
 	 */
 	private static Collection<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor> getOutgoingTypeModelFacetLinks_InterfaceRealization_4008(BehavioredClassifier source) {
 		BehavioredClassifier container = null;
 		// Find container element for the link.
 		// Climb up by containment hierarchy starting from the source
 		// and return the first element that is instance of the container class.
 		for (EObject element = source; element != null && container == null; element = element.eContainer()) {
 			if (element instanceof BehavioredClassifier) {
 				container = (BehavioredClassifier) element;
 			}
 		}
 		if (container == null) {
 			return Collections.emptyList();
 		}
 		LinkedList<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor> result = new LinkedList<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor>();
 		for (Iterator<?> links = container.getInterfaceRealizations().iterator(); links.hasNext();) {
 			EObject linkObject = (EObject) links.next();
 			if (false == linkObject instanceof InterfaceRealization) {
 				continue;
 			}
 			InterfaceRealization link = (InterfaceRealization) linkObject;
 			if (InterfaceRealizationEditPart.VISUAL_ID != UMLVisualIDRegistry.getLinkWithClassVisualID(link)) {
 				continue;
 			}
 			Interface dst = link.getContract();
 			BehavioredClassifier src = link.getImplementingClassifier();
 			if (src != source) {
 				continue;
 			}
 			result.add(new org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor(src, dst, link, UMLElementTypes.InterfaceRealization_4008, InterfaceRealizationEditPart.VISUAL_ID));
 		}
 		return result;
 	}
 
 	/**
 	 * @generated
 	 */
 	private static Collection<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor> getOutgoingTypeModelFacetLinks_Realization_4010(NamedElement source) {
 		Package container = null;
 		// Find container element for the link.
 		// Climb up by containment hierarchy starting from the source
 		// and return the first element that is instance of the container class.
 		for (EObject element = source; element != null && container == null; element = element.eContainer()) {
 			if (element instanceof Package) {
 				container = (Package) element;
 			}
 		}
 		if (container == null) {
 			return Collections.emptyList();
 		}
 		LinkedList<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor> result = new LinkedList<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor>();
 		for (Iterator<?> links = container.getPackagedElements().iterator(); links.hasNext();) {
 			EObject linkObject = (EObject) links.next();
 			if (false == linkObject instanceof Realization) {
 				continue;
 			}
 			Realization link = (Realization) linkObject;
 			if (RealizationEditPart.VISUAL_ID != UMLVisualIDRegistry.getLinkWithClassVisualID(link)) {
 				continue;
 			}
 			List targets = link.getSuppliers();
 			Object theTarget = targets.size() == 1 ? targets.get(0) : null;
 			if (false == theTarget instanceof NamedElement) {
 				continue;
 			}
 			NamedElement dst = (NamedElement) theTarget;
 			List sources = link.getClients();
 			Object theSource = sources.size() == 1 ? sources.get(0) : null;
 			if (false == theSource instanceof NamedElement) {
 				continue;
 			}
 			NamedElement src = (NamedElement) theSource;
 			if (src != source) {
 				continue;
 			}
 			result.add(new org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor(src, dst, link, UMLElementTypes.Realization_4010, RealizationEditPart.VISUAL_ID));
 		}
 		return result;
 	}
 
 	/**
 	 * @generated
 	 */
 	private static Collection<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor> getOutgoingFeatureModelFacetLinks_Generalization_General_4012(Generalization source) {
 		LinkedList<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor> result = new LinkedList<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor>();
 		Classifier destination = source.getGeneral();
 		if (destination == null) {
 			return result;
 		}
 		result.add(new org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor(source, destination, UMLElementTypes.GeneralizationGeneral_4012, GeneralizationGeneralEditPart.VISUAL_ID));
 		return result;
 	}
 
 	/**
 	 * @generated
 	 */
 	private static Collection<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor> getOutgoingTypeModelFacetLinks_Usage_4013(NamedElement source) {
 		Package container = null;
 		// Find container element for the link.
 		// Climb up by containment hierarchy starting from the source
 		// and return the first element that is instance of the container class.
 		for (EObject element = source; element != null && container == null; element = element.eContainer()) {
 			if (element instanceof Package) {
 				container = (Package) element;
 			}
 		}
 		if (container == null) {
 			return Collections.emptyList();
 		}
 		LinkedList<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor> result = new LinkedList<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor>();
 		for (Iterator<?> links = container.getPackagedElements().iterator(); links.hasNext();) {
 			EObject linkObject = (EObject) links.next();
 			if (false == linkObject instanceof Usage) {
 				continue;
 			}
 			Usage link = (Usage) linkObject;
 			if (UsageEditPart.VISUAL_ID != UMLVisualIDRegistry.getLinkWithClassVisualID(link)) {
 				continue;
 			}
 			List targets = link.getSuppliers();
 			Object theTarget = targets.size() == 1 ? targets.get(0) : null;
 			if (false == theTarget instanceof NamedElement) {
 				continue;
 			}
 			NamedElement dst = (NamedElement) theTarget;
 			List sources = link.getClients();
 			Object theSource = sources.size() == 1 ? sources.get(0) : null;
 			if (false == theSource instanceof NamedElement) {
 				continue;
 			}
 			NamedElement src = (NamedElement) theSource;
 			if (src != source) {
 				continue;
 			}
 			result.add(new org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor(src, dst, link, UMLElementTypes.Usage_4013, UsageEditPart.VISUAL_ID));
 		}
 		return result;
 	}
 
 	/**
 	 * @generated NOT
 	 */
 	private static Collection<UMLLinkDescriptor> getOutgoingTypeModelFacetLinks_Slot_4015(InstanceSpecification source) {
 		InstanceSpecification container = null;
 		// Find container element for the link.
 		// Climb up by containment hierarchy starting from the source
 		// and return the first element that is instance of the container class.
 		for (EObject element = source; element != null && container == null; element = element.eContainer()) {
 			if (element instanceof InstanceSpecification) {
 				container = (InstanceSpecification) element;
 			}
 		}
 		if (container == null) {
 			return Collections.emptyList();
 		}
 		Collection<UMLLinkDescriptor> result = new LinkedList<UMLLinkDescriptor>();
 		for (Slot link : container.getSlots()) {
 			if (AssociationInstanceEditPart.VISUAL_ID != UMLVisualIDRegistry.getLinkWithClassVisualID(link)) {
 				continue;
 			}
 			InstanceSpecification src = link.getOwningInstance();
 			for (ValueSpecification next : link.getValues()) {
 				if (next instanceof InstanceValue) {
 					InstanceValue nextValue = (InstanceValue) next;
 					InstanceSpecification nextDst = nextValue.getInstance();
 					if (nextDst != null) {
 						result.add(new UMLLinkDescriptor(src, nextDst, link, UMLElementTypes.Slot_4015, AssociationInstanceEditPart.VISUAL_ID));
 					}
 				}
 			}
 		}
 		return result;
 	}
 
 	/**
 	 * @generated NOT
 	 */
 	private static Collection<UMLLinkDescriptor> getOutgoingTypeModelFacetLinks_TemplateBinding_4016(TemplateableElement source) {
 		TemplateableElement container = null;
 		// Find container element for the link.
 		// Climb up by containment hierarchy starting from the source
 		// and return the first element that is instance of the container class.
 		for (EObject element = source; element != null && container == null; element = element.eContainer()) {
 			if (element instanceof TemplateableElement) {
 				container = (TemplateableElement) element;
 			}
 		}
 		if (container == null) {
 			return Collections.emptyList();
 		}
 		Collection<UMLLinkDescriptor> result = new LinkedList<UMLLinkDescriptor>();
 		for (TemplateBinding link : container.getTemplateBindings()) {
 			if (TemplateBindingEditPart.VISUAL_ID != UMLVisualIDRegistry.getLinkWithClassVisualID(link)) {
 				continue;
 			}
 			TemplateableElement dst = link.getSignature().getTemplate();
 			TemplateableElement src = link.getBoundElement();
 			if (src != source) {
 				continue;
 			}
 			result.add(new UMLLinkDescriptor(src, dst, link, UMLElementTypes.TemplateBinding_4016, TemplateBindingEditPart.VISUAL_ID));
 		}
 		return result;
 	}
 
 	/**
 	 * @generated NOT
 	 */
 	private static Collection<UMLLinkDescriptor> getOutgoingFeatureModelFacetLinks_Port_Provided_4017(Port source) {
 		Collection<UMLLinkDescriptor> result = new LinkedList<UMLLinkDescriptor>();
 		Collection<UMLLinkDescriptor> generatedLinks = getOutgoingFeatureModelFacetLinks_Port_Provided_4017Gen(source);
 		result.addAll(generatedLinks);
 
 		Collection<ProvidedInterfaceLink> provideds = PortOperationsExt.getProvideds(source);
 		for (ProvidedInterfaceLink provided : provideds) {
 			boolean alreadyAdded = false;
 			for (UMLLinkDescriptor linkDescriptor : generatedLinks) {
 				if (linkDescriptor.getDestination() == provided.getTarget()) {
 					alreadyAdded = true;
 					break;
 				}
 			}
 			if (!alreadyAdded) {
 				result.add(new UMLLinkDescriptor(source, provided.getTarget(), UMLElementTypes.PortProvided_4017, PortProvidedEditPart.VISUAL_ID));
 			}
 		}
 		return result;
 	}
 
 	/**
 	 * @generated
 	 */
 	private static Collection<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor> getOutgoingFeatureModelFacetLinks_Port_Provided_4017Gen(Port source) {
 		LinkedList<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor> result = new LinkedList<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor>();
 		for (Iterator<?> destinations = source.getProvideds().iterator(); destinations.hasNext();) {
 			Interface destination = (Interface) destinations.next();
 			result.add(new org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor(source, destination, UMLElementTypes.PortProvided_4017, PortProvidedEditPart.VISUAL_ID));
 		}
 		return result;
 	}
 
 	/**
 	 * @generated
 	 */
 	private static Collection<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor> getOutgoingFeatureModelFacetLinks_Port_Required_4018(Port source) {
 		LinkedList<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor> result = new LinkedList<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor>();
 		for (Iterator<?> destinations = source.getRequireds().iterator(); destinations.hasNext();) {
 			Interface destination = (Interface) destinations.next();
 			result.add(new org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor(source, destination, UMLElementTypes.PortRequired_4018, PortRequiredEditPart.VISUAL_ID));
 		}
 		return result;
 	}
 
 	/**
 	 * @generated
 	 */
 	private static Collection<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor> getOutgoingFeatureModelFacetLinks_Comment_AnnotatedElement_4019(Comment source) {
 		LinkedList<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor> result = new LinkedList<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor>();
 		for (Iterator<?> destinations = source.getAnnotatedElements().iterator(); destinations.hasNext();) {
 			Element destination = (Element) destinations.next();
 			result.add(new org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor(source, destination, UMLElementTypes.CommentAnnotatedElement_4019, CommentAnnotatedElementEditPart.VISUAL_ID));
 		}
 		return result;
 	}
 
 	/**
 	 * @generated
 	 */
 	public static final IDiagramUpdater TYPED_ADAPTER = new IDiagramUpdater() {
 
 		/**
 		* @generated
 		*/
 		public List<UMLNodeDescriptor> getSemanticChildren(View view) {
 			return org.eclipse.uml2.diagram.clazz.part.UMLDiagramUpdater.getSemanticChildren(view);
 		}
 
 		/**
 		* @generated
 		*/
 		public List<UMLLinkDescriptor> getContainedLinks(View view) {
 			return org.eclipse.uml2.diagram.clazz.part.UMLDiagramUpdater.getContainedLinks(view);
 		}
 
 		/**
 		* @generated
 		*/
 		public List<UMLLinkDescriptor> getIncomingLinks(View view) {
 			return org.eclipse.uml2.diagram.clazz.part.UMLDiagramUpdater.getIncomingLinks(view);
 		}
 
 		/**
 		* @generated
 		*/
 		public List<UMLLinkDescriptor> getOutgoingLinks(View view) {
 			return org.eclipse.uml2.diagram.clazz.part.UMLDiagramUpdater.getOutgoingLinks(view);
 		}
 	};
 
 	/**
 	 * @generated
 	 */
 	public static List<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor> getPackage_1000ContainedLinks(View view) {
 		Package modelElement = (Package) view.getElement();
 		LinkedList<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor> result = new LinkedList<org.eclipse.uml2.diagram.clazz.part.UMLLinkDescriptor>();
 		result.addAll(getContainedTypeModelFacetLinks_Dependency_4002(modelElement));
 		result.addAll(getContainedTypeModelFacetLinks_Association_4005(modelElement));
 		result.addAll(getContainedTypeModelFacetLinks_Realization_4010(modelElement));
 		result.addAll(getContainedTypeModelFacetLinks_Usage_4013(modelElement));
 		result.addAll(getContainedTypeModelFacetLinks_AssociationClass_4014(modelElement));
 		result.addAll(getContainedTypeModelFacetLinks_TemplateBinding_4016(modelElement));
 		return result;
 	}
 
 	/**
 	 * @NOT-generated
 	 */
 	private static Collection<UMLLinkDescriptor> getOutgoingFeatureModelFacetLinks_Generalization_General_4012(GeneralizationSet source) {
 		Collection<UMLLinkDescriptor> result = new LinkedList<UMLLinkDescriptor>();
 		List<Generalization> generalizations = source.getGeneralizations();
 		if (generalizations.size() > 0) {
 			Generalization generalization = generalizations.get(0);
 			Classifier destination = generalization.getGeneral();
 			result.add(new UMLLinkDescriptor(source, destination, UMLElementTypes.GeneralizationGeneral_4012, GeneralizationGeneralEditPart.VISUAL_ID));
 		}
 		return result;
 	}
 
 }
