 package org.eclipse.uml2.diagram.csd.part;
 
 import org.eclipse.core.runtime.Platform;
 import org.eclipse.emf.ecore.EAnnotation;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.gmf.runtime.notation.Diagram;
 import org.eclipse.gmf.runtime.notation.View;
 import org.eclipse.uml2.diagram.common.genapi.IVisualIDRegistry;
 import org.eclipse.uml2.diagram.csd.edit.parts.AssociationEditPart;
 import org.eclipse.uml2.diagram.csd.edit.parts.AssociationInstanceEditPart;
 import org.eclipse.uml2.diagram.csd.edit.parts.AssociationInstanceSourceEditPart;
 import org.eclipse.uml2.diagram.csd.edit.parts.AssociationInstanceTargetEditPart;
 import org.eclipse.uml2.diagram.csd.edit.parts.AssociationName2EditPart;
 import org.eclipse.uml2.diagram.csd.edit.parts.AssociationName3EditPart;
 import org.eclipse.uml2.diagram.csd.edit.parts.AssociationName4EditPart;
 import org.eclipse.uml2.diagram.csd.edit.parts.AssociationName5EditPart;
 import org.eclipse.uml2.diagram.csd.edit.parts.AssociationName6EditPart;
 import org.eclipse.uml2.diagram.csd.edit.parts.AssociationName7EditPart;
 import org.eclipse.uml2.diagram.csd.edit.parts.AssociationNameEditPart;
 import org.eclipse.uml2.diagram.csd.edit.parts.Class2EditPart;
 import org.eclipse.uml2.diagram.csd.edit.parts.Class3EditPart;
 import org.eclipse.uml2.diagram.csd.edit.parts.ClassAttributesEditPart;
 import org.eclipse.uml2.diagram.csd.edit.parts.ClassClass_contentsEditPart;
 import org.eclipse.uml2.diagram.csd.edit.parts.ClassClassesEditPart;
 import org.eclipse.uml2.diagram.csd.edit.parts.ClassEditPart;
 import org.eclipse.uml2.diagram.csd.edit.parts.ClassName2EditPart;
 import org.eclipse.uml2.diagram.csd.edit.parts.ClassNameEditPart;
 import org.eclipse.uml2.diagram.csd.edit.parts.ClassOperationsEditPart;
 import org.eclipse.uml2.diagram.csd.edit.parts.ClassQualifiedNameEditPart;
 import org.eclipse.uml2.diagram.csd.edit.parts.ClassStereoEditPart;
 import org.eclipse.uml2.diagram.csd.edit.parts.CollaborationContentsEditPart;
 import org.eclipse.uml2.diagram.csd.edit.parts.CollaborationEditPart;
 import org.eclipse.uml2.diagram.csd.edit.parts.CollaborationNameEditPart;
 import org.eclipse.uml2.diagram.csd.edit.parts.CollaborationStereoEditPart;
 import org.eclipse.uml2.diagram.csd.edit.parts.CollaborationUse2EditPart;
 import org.eclipse.uml2.diagram.csd.edit.parts.CollaborationUseName2EditPart;
 import org.eclipse.uml2.diagram.csd.edit.parts.CollaborationUseStereoEditPart;
 import org.eclipse.uml2.diagram.csd.edit.parts.CommentBodyEditPart;
 import org.eclipse.uml2.diagram.csd.edit.parts.CommentEditPart;
 import org.eclipse.uml2.diagram.csd.edit.parts.ConnectorEditPart;
 import org.eclipse.uml2.diagram.csd.edit.parts.ConnectorName2EditPart;
 import org.eclipse.uml2.diagram.csd.edit.parts.ConnectorName3EditPart;
 import org.eclipse.uml2.diagram.csd.edit.parts.ConnectorName4EditPart;
 import org.eclipse.uml2.diagram.csd.edit.parts.ConnectorName5EditPart;
 import org.eclipse.uml2.diagram.csd.edit.parts.ConnectorName6EditPart;
 import org.eclipse.uml2.diagram.csd.edit.parts.ConnectorName7EditPart;
 import org.eclipse.uml2.diagram.csd.edit.parts.ConnectorNameEditPart;
 import org.eclipse.uml2.diagram.csd.edit.parts.ConstraintEditPart;
 import org.eclipse.uml2.diagram.csd.edit.parts.ConstraintNameEditPart;
 import org.eclipse.uml2.diagram.csd.edit.parts.DependencyEditPart;
 import org.eclipse.uml2.diagram.csd.edit.parts.DependencyNameEditPart;
 import org.eclipse.uml2.diagram.csd.edit.parts.ElementImportEditPart;
 import org.eclipse.uml2.diagram.csd.edit.parts.InstanceSpecificationEditPart;
 import org.eclipse.uml2.diagram.csd.edit.parts.InstanceSpecificationNameEditPart;
 import org.eclipse.uml2.diagram.csd.edit.parts.InstanceSpecificationSlotsEditPart;
 import org.eclipse.uml2.diagram.csd.edit.parts.InstanceSpecificationStereoEditPart;
 import org.eclipse.uml2.diagram.csd.edit.parts.InterfaceEditPart;
 import org.eclipse.uml2.diagram.csd.edit.parts.InterfaceNameEditPart;
 import org.eclipse.uml2.diagram.csd.edit.parts.InterfaceRealizationEditPart;
 import org.eclipse.uml2.diagram.csd.edit.parts.OperationEditPart;
 import org.eclipse.uml2.diagram.csd.edit.parts.Package2EditPart;
 import org.eclipse.uml2.diagram.csd.edit.parts.PackageEditPart;
 import org.eclipse.uml2.diagram.csd.edit.parts.PackageImportsEditPart;
 import org.eclipse.uml2.diagram.csd.edit.parts.PackageNameEditPart;
 import org.eclipse.uml2.diagram.csd.edit.parts.ParameterEditPart;
 import org.eclipse.uml2.diagram.csd.edit.parts.ParameterNameEditPart;
 import org.eclipse.uml2.diagram.csd.edit.parts.Port2EditPart;
 import org.eclipse.uml2.diagram.csd.edit.parts.Port3EditPart;
 import org.eclipse.uml2.diagram.csd.edit.parts.PortEditPart;
 import org.eclipse.uml2.diagram.csd.edit.parts.PortIsBehavior2EditPart;
 import org.eclipse.uml2.diagram.csd.edit.parts.PortIsBehaviorEditPart;
 import org.eclipse.uml2.diagram.csd.edit.parts.PortName2EditPart;
 import org.eclipse.uml2.diagram.csd.edit.parts.PortName3EditPart;
 import org.eclipse.uml2.diagram.csd.edit.parts.PortNameEditPart;
 import org.eclipse.uml2.diagram.csd.edit.parts.Property2EditPart;
 import org.eclipse.uml2.diagram.csd.edit.parts.Property3EditPart;
 import org.eclipse.uml2.diagram.csd.edit.parts.PropertyEditPart;
 import org.eclipse.uml2.diagram.csd.edit.parts.PropertyName2EditPart;
 import org.eclipse.uml2.diagram.csd.edit.parts.PropertyNameEditPart;
 import org.eclipse.uml2.diagram.csd.edit.parts.SlotEditPart;
 import org.eclipse.uml2.diagram.csd.edit.parts.UsageEditPart;
 import org.eclipse.uml2.diagram.csd.expressions.UMLAbstractExpression;
 import org.eclipse.uml2.diagram.csd.expressions.UMLOCLFactory;
 import org.eclipse.uml2.uml.Class;
 import org.eclipse.uml2.uml.Connector;
 import org.eclipse.uml2.uml.Dependency;
 import org.eclipse.uml2.uml.InterfaceRealization;
 import org.eclipse.uml2.uml.Package;
 import org.eclipse.uml2.uml.Property;
 import org.eclipse.uml2.uml.Slot;
 import org.eclipse.uml2.uml.UMLPackage;
 import org.eclipse.uml2.uml.Usage;
 
 /**
  * This registry is used to determine which type of visual object should be
  * created for the corresponding Diagram, Node, ChildNode or Link represented
  * by a domain model object.
  * 
  * @generated
  */
 public class UMLVisualIDRegistry {
 
 	/**
 	 * @generated
 	 */
 	private static final String DEBUG_KEY = "org.eclipse.uml2.diagram.csd/debug/visualID"; //$NON-NLS-1$
 
 	/**
 	 * @generated
 	 */
 	private static UMLAbstractExpression Class_2006_Constraint;
 
 	/**
 	 * @generated
 	 */
 	private static UMLAbstractExpression Class_2007_Constraint;
 
 	/**
 	 * @generated
 	 */
 	private static UMLAbstractExpression Property_3007_Constraint;
 
 	/**
 	 * @generated
 	 */
 	private static UMLAbstractExpression Property_3008_Constraint;
 
 	/**
 	 * @generated
 	 */
 	private static UMLAbstractExpression Property_3014_Constraint;
 
 	/**
 	 * @generated
 	 */
 	private static UMLAbstractExpression Connector_4005_Constraint;
 
 	/**
 	 * @generated
 	 */
 	private static UMLAbstractExpression Dependency_4006_Constraint;
 
 	/**
 	 * @generated
 	 */
 	private static UMLAbstractExpression InterfaceRealization_4007_Constraint;
 
 	/**
 	 * @generated
 	 */
 	private static UMLAbstractExpression Usage_4008_Constraint;
 
 	/**
 	 * @generated
 	 */
 	private static UMLAbstractExpression Slot_4015_Constraint;
 
 	/**
 	 * @generated
 	 */
 	public static int getVisualID(View view) {
 		if (view instanceof Diagram) {
 			if (PackageEditPart.MODEL_ID.equals(view.getType())) {
 				return PackageEditPart.VISUAL_ID;
 			} else {
 				return -1;
 			}
 		}
 		return org.eclipse.uml2.diagram.csd.part.UMLVisualIDRegistry.getVisualID(view.getType());
 	}
 
 	/**
 	 * @generated
 	 */
 	public static String getModelID(View view) {
 		View diagram = view.getDiagram();
 		while (view != diagram) {
 			EAnnotation annotation = view.getEAnnotation("Shortcut"); //$NON-NLS-1$
 			if (annotation != null) {
 				return (String) annotation.getDetails().get("modelID"); //$NON-NLS-1$
 			}
 			view = (View) view.eContainer();
 		}
 		return diagram != null ? diagram.getType() : null;
 	}
 
 	/**
 	 * @generated
 	 */
 	public static int getVisualID(String type) {
 		try {
 			return Integer.parseInt(type);
 		} catch (NumberFormatException e) {
 			if (Boolean.TRUE.toString().equalsIgnoreCase(Platform.getDebugOption(DEBUG_KEY))) {
 				UMLDiagramEditorPlugin.getInstance().logError("Unable to parse view type as a visualID number: " + type);
 			}
 		}
 		return -1;
 	}
 
 	/**
 	 * @generated
 	 */
 	public static String getType(int visualID) {
 		return String.valueOf(visualID);
 	}
 
 	/**
 	 * @generated
 	 */
 	public static int getDiagramVisualID(EObject domainElement) {
 		if (domainElement == null) {
 			return -1;
 		}
 		if (UMLPackage.eINSTANCE.getPackage().isSuperTypeOf(domainElement.eClass()) && isDiagram((Package) domainElement)) {
 			return PackageEditPart.VISUAL_ID;
 		}
 		return -1;
 	}
 
 	/**
 	 * @generated
 	 */
 	public static int getNodeVisualID(View containerView, EObject domainElement) {
 		if (domainElement == null) {
 			return -1;
 		}
 		String containerModelID = org.eclipse.uml2.diagram.csd.part.UMLVisualIDRegistry.getModelID(containerView);
 		if (!PackageEditPart.MODEL_ID.equals(containerModelID) && !"uml".equals(containerModelID)) { //$NON-NLS-1$
 			return -1;
 		}
 		int containerVisualID;
 		if (PackageEditPart.MODEL_ID.equals(containerModelID)) {
 			containerVisualID = org.eclipse.uml2.diagram.csd.part.UMLVisualIDRegistry.getVisualID(containerView);
 		} else {
 			if (containerView instanceof Diagram) {
 				containerVisualID = PackageEditPart.VISUAL_ID;
 			} else {
 				return -1;
 			}
 		}
 		switch (containerVisualID) {
 		case CollaborationEditPart.VISUAL_ID:
 			if (UMLPackage.eINSTANCE.getParameter().isSuperTypeOf(domainElement.eClass())) {
 				return ParameterEditPart.VISUAL_ID;
 			}
 			break;
 		case ClassEditPart.VISUAL_ID:
 			if (UMLPackage.eINSTANCE.getPort().isSuperTypeOf(domainElement.eClass())) {
 				return PortEditPart.VISUAL_ID;
 			}
 			break;
 		case Class3EditPart.VISUAL_ID:
 			if (UMLPackage.eINSTANCE.getPort().isSuperTypeOf(domainElement.eClass())) {
 				return Port3EditPart.VISUAL_ID;
 			}
 			break;
 		case Property3EditPart.VISUAL_ID:
 			if (UMLPackage.eINSTANCE.getPort().isSuperTypeOf(domainElement.eClass())) {
 				return Port2EditPart.VISUAL_ID;
 			}
 			break;
 		case CollaborationContentsEditPart.VISUAL_ID:
 			if (UMLPackage.eINSTANCE.getCollaborationUse().isSuperTypeOf(domainElement.eClass())) {
 				return CollaborationUse2EditPart.VISUAL_ID;
 			}
 			if (UMLPackage.eINSTANCE.getProperty().isSuperTypeOf(domainElement.eClass()) && isProperty_3007((Property) domainElement)) {
 				return PropertyEditPart.VISUAL_ID;
 			}
 			break;
 		case ClassAttributesEditPart.VISUAL_ID:
 			if (UMLPackage.eINSTANCE.getProperty().isSuperTypeOf(domainElement.eClass()) && isProperty_3008((Property) domainElement)) {
 				return Property2EditPart.VISUAL_ID;
 			}
 			break;
 		case ClassOperationsEditPart.VISUAL_ID:
 			if (UMLPackage.eINSTANCE.getOperation().isSuperTypeOf(domainElement.eClass())) {
 				return OperationEditPart.VISUAL_ID;
 			}
 			break;
 		case ClassClassesEditPart.VISUAL_ID:
 			if (UMLPackage.eINSTANCE.getClass_().isSuperTypeOf(domainElement.eClass())) {
 				return Class2EditPart.VISUAL_ID;
 			}
 			break;
 		case PackageImportsEditPart.VISUAL_ID:
 			if (UMLPackage.eINSTANCE.getElementImport().isSuperTypeOf(domainElement.eClass())) {
 				return ElementImportEditPart.VISUAL_ID;
 			}
 			break;
 		case ClassClass_contentsEditPart.VISUAL_ID:
 			if (UMLPackage.eINSTANCE.getProperty().isSuperTypeOf(domainElement.eClass()) && isProperty_3014((Property) domainElement)) {
 				return Property3EditPart.VISUAL_ID;
 			}
 			break;
 		case InstanceSpecificationSlotsEditPart.VISUAL_ID:
 			if (UMLPackage.eINSTANCE.getSlot().isSuperTypeOf(domainElement.eClass())) {
 				return SlotEditPart.VISUAL_ID;
 			}
 			break;
 		case PackageEditPart.VISUAL_ID:
 			// We want to additionally show the Canvas Semantical Element in the auxiliary 
 			// org.eclipse.uml2.diagram.csd.edit.parts.Package2EditPart (that serves as a pure visual container for children). 
 			// To do this, we modified CanonicalEditPolicy to add the Canvas semantic Element into the children 
 			// list. The only remaining part is to return correct VID for this special case.
 
 			if (containerView instanceof Diagram && domainElement != null && domainElement.equals(containerView.getElement())) {
 				return Package2EditPart.VISUAL_ID;
 			}
 			if (UMLPackage.eINSTANCE.getCollaboration().isSuperTypeOf(domainElement.eClass())) {
 				return CollaborationEditPart.VISUAL_ID;
 			}
 			if (UMLPackage.eINSTANCE.getClass_().isSuperTypeOf(domainElement.eClass()) && isClass_2006((Class) domainElement)) {
 				return ClassEditPart.VISUAL_ID;
 			}
 			// Diagram header is already processed above
 			if (UMLPackage.eINSTANCE.getClass_().isSuperTypeOf(domainElement.eClass()) && isClass_2007((Class) domainElement)) {
 				return Class3EditPart.VISUAL_ID;
 			}
 			if (UMLPackage.eINSTANCE.getInterface().isSuperTypeOf(domainElement.eClass())) {
 				return InterfaceEditPart.VISUAL_ID;
 			}
 			if (UMLPackage.eINSTANCE.getInstanceSpecification().isSuperTypeOf(domainElement.eClass())) {
 				return InstanceSpecificationEditPart.VISUAL_ID;
 			}
 			if (UMLPackage.eINSTANCE.getConstraint().isSuperTypeOf(domainElement.eClass())) {
 				return ConstraintEditPart.VISUAL_ID;
 			}
 			if (UMLPackage.eINSTANCE.getComment().isSuperTypeOf(domainElement.eClass())) {
 				return CommentEditPart.VISUAL_ID;
 			}
 			break;
 		}
 		return -1;
 	}
 
 	/**
 	 * @generated
 	 */
 	public static boolean canCreateNode(View containerView, int nodeVisualID) {
 		String containerModelID = org.eclipse.uml2.diagram.csd.part.UMLVisualIDRegistry.getModelID(containerView);
 		if (!PackageEditPart.MODEL_ID.equals(containerModelID) && !"uml".equals(containerModelID)) { //$NON-NLS-1$
 			return false;
 		}
 		int containerVisualID;
 		if (PackageEditPart.MODEL_ID.equals(containerModelID)) {
 			containerVisualID = org.eclipse.uml2.diagram.csd.part.UMLVisualIDRegistry.getVisualID(containerView);
 		} else {
 			if (containerView instanceof Diagram) {
 				containerVisualID = PackageEditPart.VISUAL_ID;
 			} else {
 				return false;
 			}
 		}
 		switch (containerVisualID) {
 		case CollaborationEditPart.VISUAL_ID:
 			if (CollaborationNameEditPart.VISUAL_ID == nodeVisualID) {
 				return true;
 			}
 			if (CollaborationStereoEditPart.VISUAL_ID == nodeVisualID) {
 				return true;
 			}
 			if (CollaborationContentsEditPart.VISUAL_ID == nodeVisualID) {
 				return true;
 			}
 			if (ParameterEditPart.VISUAL_ID == nodeVisualID) {
 				return true;
 			}
 			break;
 		case ClassEditPart.VISUAL_ID:
 			if (ClassNameEditPart.VISUAL_ID == nodeVisualID) {
 				return true;
 			}
 			if (ClassStereoEditPart.VISUAL_ID == nodeVisualID) {
 				return true;
 			}
 			if (ClassAttributesEditPart.VISUAL_ID == nodeVisualID) {
 				return true;
 			}
 			if (ClassOperationsEditPart.VISUAL_ID == nodeVisualID) {
 				return true;
 			}
 			if (ClassClassesEditPart.VISUAL_ID == nodeVisualID) {
 				return true;
 			}
 			if (PortEditPart.VISUAL_ID == nodeVisualID) {
 				return true;
 			}
 			break;
 		case Package2EditPart.VISUAL_ID:
 			if (PackageNameEditPart.VISUAL_ID == nodeVisualID) {
 				return true;
 			}
 			if (PackageImportsEditPart.VISUAL_ID == nodeVisualID) {
 				return true;
 			}
 			break;
 		case Class3EditPart.VISUAL_ID:
 			if (ClassName2EditPart.VISUAL_ID == nodeVisualID) {
 				return true;
 			}
 			if (ClassClass_contentsEditPart.VISUAL_ID == nodeVisualID) {
 				return true;
 			}
 			if (Port3EditPart.VISUAL_ID == nodeVisualID) {
 				return true;
 			}
 			break;
 		case InterfaceEditPart.VISUAL_ID:
 			if (InterfaceNameEditPart.VISUAL_ID == nodeVisualID) {
 				return true;
 			}
 			break;
 		case InstanceSpecificationEditPart.VISUAL_ID:
 			if (InstanceSpecificationNameEditPart.VISUAL_ID == nodeVisualID) {
 				return true;
 			}
 			if (InstanceSpecificationStereoEditPart.VISUAL_ID == nodeVisualID) {
 				return true;
 			}
 			if (InstanceSpecificationSlotsEditPart.VISUAL_ID == nodeVisualID) {
 				return true;
 			}
 			break;
 		case ConstraintEditPart.VISUAL_ID:
 			if (ConstraintNameEditPart.VISUAL_ID == nodeVisualID) {
 				return true;
 			}
 			break;
 		case CommentEditPart.VISUAL_ID:
 			if (CommentBodyEditPart.VISUAL_ID == nodeVisualID) {
 				return true;
 			}
 			break;
 		case CollaborationUse2EditPart.VISUAL_ID:
 			if (CollaborationUseName2EditPart.VISUAL_ID == nodeVisualID) {
 				return true;
 			}
 			if (CollaborationUseStereoEditPart.VISUAL_ID == nodeVisualID) {
 				return true;
 			}
 			break;
 		case PropertyEditPart.VISUAL_ID:
 			if (PropertyNameEditPart.VISUAL_ID == nodeVisualID) {
 				return true;
 			}
 			break;
 		case ParameterEditPart.VISUAL_ID:
 			if (ParameterNameEditPart.VISUAL_ID == nodeVisualID) {
 				return true;
 			}
 			break;
 		case PortEditPart.VISUAL_ID:
 			if (PortNameEditPart.VISUAL_ID == nodeVisualID) {
 				return true;
 			}
 			if (PortIsBehaviorEditPart.VISUAL_ID == nodeVisualID) {
 				return true;
 			}
 			break;
 		case Property3EditPart.VISUAL_ID:
 			if (PropertyName2EditPart.VISUAL_ID == nodeVisualID) {
 				return true;
 			}
 			if (Port2EditPart.VISUAL_ID == nodeVisualID) {
 				return true;
 			}
 			break;
 		case Port2EditPart.VISUAL_ID:
 			if (PortName2EditPart.VISUAL_ID == nodeVisualID) {
 				return true;
 			}
 			break;
 		case Port3EditPart.VISUAL_ID:
 			if (PortName3EditPart.VISUAL_ID == nodeVisualID) {
 				return true;
 			}
 			if (PortIsBehavior2EditPart.VISUAL_ID == nodeVisualID) {
 				return true;
 			}
 			break;
 		case CollaborationContentsEditPart.VISUAL_ID:
 			if (CollaborationUse2EditPart.VISUAL_ID == nodeVisualID) {
 				return true;
 			}
 			if (PropertyEditPart.VISUAL_ID == nodeVisualID) {
 				return true;
 			}
 			break;
 		case ClassAttributesEditPart.VISUAL_ID:
 			if (Property2EditPart.VISUAL_ID == nodeVisualID) {
 				return true;
 			}
 			break;
 		case ClassOperationsEditPart.VISUAL_ID:
 			if (OperationEditPart.VISUAL_ID == nodeVisualID) {
 				return true;
 			}
 			break;
 		case ClassClassesEditPart.VISUAL_ID:
 			if (Class2EditPart.VISUAL_ID == nodeVisualID) {
 				return true;
 			}
 			break;
 		case PackageImportsEditPart.VISUAL_ID:
 			if (ElementImportEditPart.VISUAL_ID == nodeVisualID) {
 				return true;
 			}
 			break;
 		case ClassClass_contentsEditPart.VISUAL_ID:
 			if (Property3EditPart.VISUAL_ID == nodeVisualID) {
 				return true;
 			}
 			break;
 		case InstanceSpecificationSlotsEditPart.VISUAL_ID:
 			if (SlotEditPart.VISUAL_ID == nodeVisualID) {
 				return true;
 			}
 			break;
 		case PackageEditPart.VISUAL_ID:
 			if (CollaborationEditPart.VISUAL_ID == nodeVisualID) {
 				return true;
 			}
 			if (ClassEditPart.VISUAL_ID == nodeVisualID) {
 				return true;
 			}
 			if (Package2EditPart.VISUAL_ID == nodeVisualID) {
 				return true;
 			}
 			if (Class3EditPart.VISUAL_ID == nodeVisualID) {
 				return true;
 			}
 			if (InterfaceEditPart.VISUAL_ID == nodeVisualID) {
 				return true;
 			}
 			if (InstanceSpecificationEditPart.VISUAL_ID == nodeVisualID) {
 				return true;
 			}
 			if (ConstraintEditPart.VISUAL_ID == nodeVisualID) {
 				return true;
 			}
 			if (CommentEditPart.VISUAL_ID == nodeVisualID) {
 				return true;
 			}
 			break;
 		case ConnectorEditPart.VISUAL_ID:
 			if (ConnectorNameEditPart.VISUAL_ID == nodeVisualID) {
 				return true;
 			}
 			if (ConnectorName2EditPart.VISUAL_ID == nodeVisualID) {
 				return true;
 			}
 			if (ConnectorName3EditPart.VISUAL_ID == nodeVisualID) {
 				return true;
 			}
 			if (ConnectorName4EditPart.VISUAL_ID == nodeVisualID) {
 				return true;
 			}
 			if (ConnectorName5EditPart.VISUAL_ID == nodeVisualID) {
 				return true;
 			}
 			if (ConnectorName6EditPart.VISUAL_ID == nodeVisualID) {
 				return true;
 			}
 			if (ConnectorName7EditPart.VISUAL_ID == nodeVisualID) {
 				return true;
 			}
 			break;
 		case DependencyEditPart.VISUAL_ID:
 			if (DependencyNameEditPart.VISUAL_ID == nodeVisualID) {
 				return true;
 			}
 			break;
 		case AssociationEditPart.VISUAL_ID:
 			if (AssociationNameEditPart.VISUAL_ID == nodeVisualID) {
 				return true;
 			}
 			if (AssociationName2EditPart.VISUAL_ID == nodeVisualID) {
 				return true;
 			}
 			if (AssociationName3EditPart.VISUAL_ID == nodeVisualID) {
 				return true;
 			}
 			if (AssociationName4EditPart.VISUAL_ID == nodeVisualID) {
 				return true;
 			}
 			if (AssociationName5EditPart.VISUAL_ID == nodeVisualID) {
 				return true;
 			}
 			if (AssociationName6EditPart.VISUAL_ID == nodeVisualID) {
 				return true;
 			}
 			if (AssociationName7EditPart.VISUAL_ID == nodeVisualID) {
 				return true;
 			}
 			break;
 		case AssociationInstanceEditPart.VISUAL_ID:
 			if (AssociationInstanceSourceEditPart.VISUAL_ID == nodeVisualID) {
 				return true;
 			}
 			if (AssociationInstanceTargetEditPart.VISUAL_ID == nodeVisualID) {
 				return true;
 			}
 			break;
 		}
 		return false;
 	}
 
 	/**
 	 * @generated
 	 */
 	public static int getLinkWithClassVisualID(EObject domainElement) {
 		if (domainElement == null) {
 			return -1;
 		}
 		if (UMLPackage.eINSTANCE.getConnector().isSuperTypeOf(domainElement.eClass()) && isConnector_4005((Connector) domainElement)) {
 			return ConnectorEditPart.VISUAL_ID;
 		}
 		if (UMLPackage.eINSTANCE.getDependency().isSuperTypeOf(domainElement.eClass()) && isDependency_4006((Dependency) domainElement)) {
 			return DependencyEditPart.VISUAL_ID;
 		}
 		if (UMLPackage.eINSTANCE.getInterfaceRealization().isSuperTypeOf(domainElement.eClass()) && isInterfaceRealization_4007((InterfaceRealization) domainElement)) {
 			return InterfaceRealizationEditPart.VISUAL_ID;
 		}
 		if (UMLPackage.eINSTANCE.getUsage().isSuperTypeOf(domainElement.eClass()) && isUsage_4008((Usage) domainElement)) {
 			return UsageEditPart.VISUAL_ID;
 		}
 		if (UMLPackage.eINSTANCE.getAssociation().isSuperTypeOf(domainElement.eClass())) {
 			return AssociationEditPart.VISUAL_ID;
 		}
 		if (UMLPackage.eINSTANCE.getSlot().isSuperTypeOf(domainElement.eClass()) && isSlot_4015((Slot) domainElement)) {
 			return AssociationInstanceEditPart.VISUAL_ID;
 		}
 		return -1;
 	}
 
 	/**
 	 * User can change implementation of this method to handle some specific
 	 * situations not covered by default logic.
 	 * 
 	 * @generated
 	 */
 	private static boolean isDiagram(Package element) {
 		return true;
 	}
 
 	/**
 	 * @generated
 	 */
 	private static boolean isClass_2006(Class domainElement) {
 		if (Class_2006_Constraint == null) { // lazy initialization
 			Class_2006_Constraint = UMLOCLFactory.getExpression("not oclIsKindOf(uml::AssociationClass) and not oclIsKindOf(uml::StateMachine)", UMLPackage.eINSTANCE.getClass_()); //$NON-NLS-1$
 		}
 		Object result = Class_2006_Constraint.evaluate(domainElement);
 		return result instanceof Boolean && ((Boolean) result).booleanValue();
 	}
 
 	/**
 	 * @generated
 	 */
 	private static boolean isClass_2007(Class domainElement) {
 		if (Class_2007_Constraint == null) { // lazy initialization
 			Class_2007_Constraint = UMLOCLFactory.getExpression("not oclIsKindOf(uml::AssociationClass) and not oclIsKindOf(uml::StateMachine)", UMLPackage.eINSTANCE.getClass_()); //$NON-NLS-1$
 		}
 		Object result = Class_2007_Constraint.evaluate(domainElement);
 		return result instanceof Boolean && ((Boolean) result).booleanValue();
 	}
 
 	/**
 	 * @generated
 	 */
 	private static boolean isProperty_3007(Property domainElement) {
 		if (Property_3007_Constraint == null) { // lazy initialization
 			Property_3007_Constraint = UMLOCLFactory.getExpression("not self.oclIsKindOf(uml::Port)", UMLPackage.eINSTANCE.getProperty()); //$NON-NLS-1$
 		}
 		Object result = Property_3007_Constraint.evaluate(domainElement);
 		return result instanceof Boolean && ((Boolean) result).booleanValue();
 	}
 
 	/**
 	 * @generated
 	 */
 	private static boolean isProperty_3008(Property domainElement) {
 		if (Property_3008_Constraint == null) { // lazy initialization
 			Property_3008_Constraint = UMLOCLFactory.getExpression("not oclIsKindOf(uml::Port)", UMLPackage.eINSTANCE.getProperty()); //$NON-NLS-1$
 		}
 		Object result = Property_3008_Constraint.evaluate(domainElement);
 		return result instanceof Boolean && ((Boolean) result).booleanValue();
 	}
 
 	/**
 	 * @generated
 	 */
 	private static boolean isProperty_3014(Property domainElement) {
 		if (Property_3014_Constraint == null) { // lazy initialization
 			Property_3014_Constraint = UMLOCLFactory.getExpression("not self.oclIsKindOf(uml::Port)", UMLPackage.eINSTANCE.getProperty()); //$NON-NLS-1$
 		}
 		Object result = Property_3014_Constraint.evaluate(domainElement);
 		return result instanceof Boolean && ((Boolean) result).booleanValue();
 	}
 
 	/**
 	 * @generated
 	 */
 	private static boolean isConnector_4005(Connector domainElement) {
 		if (Connector_4005_Constraint == null) { // lazy initialization
 			Connector_4005_Constraint = UMLOCLFactory.getExpression("kind =ConnectorKind::delegation", UMLPackage.eINSTANCE.getConnector()); //$NON-NLS-1$
 		}
 		Object result = Connector_4005_Constraint.evaluate(domainElement);
 		return result instanceof Boolean && ((Boolean) result).booleanValue();
 	}
 
 	/**
 	 * @generated
 	 */
 	private static boolean isDependency_4006(Dependency domainElement) {
 		if (Dependency_4006_Constraint == null) { // lazy initialization
 			Dependency_4006_Constraint = UMLOCLFactory.getExpression("not self.oclIsKindOf(uml::Usage) and not self.oclIsKindOf(uml::InterfaceRealization)", UMLPackage.eINSTANCE.getDependency()); //$NON-NLS-1$
 		}
 		Object result = Dependency_4006_Constraint.evaluate(domainElement);
 		return result instanceof Boolean && ((Boolean) result).booleanValue();
 	}
 
 	/**
 	 * @generated
 	 */
 	private static boolean isInterfaceRealization_4007(InterfaceRealization domainElement) {
 		if (InterfaceRealization_4007_Constraint == null) { // lazy initialization
 			InterfaceRealization_4007_Constraint = UMLOCLFactory.getExpression("self.supplier->forAll(e|e.oclIsKindOf(uml::Interface))", UMLPackage.eINSTANCE.getInterfaceRealization()); //$NON-NLS-1$
 		}
 		Object result = InterfaceRealization_4007_Constraint.evaluate(domainElement);
 		return result instanceof Boolean && ((Boolean) result).booleanValue();
 	}
 
 	/**
 	 * @generated
 	 */
 	private static boolean isUsage_4008(Usage domainElement) {
 		if (Usage_4008_Constraint == null) { // lazy initialization
 			Usage_4008_Constraint = UMLOCLFactory.getExpression(
 					"self.supplier->forAll(e|e.oclIsKindOf(uml::Interface)) and self.client->forAll(e|not e.oclIsKindOf(uml::Port))", UMLPackage.eINSTANCE.getUsage()); //$NON-NLS-1$
 		}
 		Object result = Usage_4008_Constraint.evaluate(domainElement);
 		return result instanceof Boolean && ((Boolean) result).booleanValue();
 	}
 
 	/**
 	 * @generated
 	 */
 	private static boolean isSlot_4015(Slot domainElement) {
 		if (Slot_4015_Constraint == null) { // lazy initialization
 			Slot_4015_Constraint = UMLOCLFactory.getExpression(
 					"self.value->exists(v : ValueSpecification | v.oclIsKindOf(InstanceValue) and not v.oclAsType(InstanceValue).oclIsUndefined())", UMLPackage.eINSTANCE.getSlot()); //$NON-NLS-1$
 		}
 		Object result = Slot_4015_Constraint.evaluate(domainElement);
 		return result instanceof Boolean && ((Boolean) result).booleanValue();
 	}
 
 	/**
 	 * @generated
 	 */
 	protected static boolean hasViewChild(View containerView, EObject domainElement, int visualId) {
 		if (containerView == null) {
 			return false;
 		}
 		if (domainElement == null) {
 			return false;
 		}
 		for (Object next : containerView.getChildren()) {
 			View nextView = (View) next;
 			if (domainElement.equals(nextView.getElement()) && getType(visualId).equals(nextView.getType())) {
 				return true;
 			}
 		}
 		return false;
 	}
 
 	/**
 	 * @generated
 	 */
 	public static int getChildDescriptorVisualID(UMLNodeDescriptor container, EObject domainElement) {
 		if (domainElement == null) {
 			return -1;
 		}
 		switch (container.getVisualID()) {
 		case CollaborationEditPart.VISUAL_ID:
 			if (UMLPackage.eINSTANCE.getParameter().isSuperTypeOf(domainElement.eClass())) {
 				return ParameterEditPart.VISUAL_ID;
 			}
 			if (UMLPackage.eINSTANCE.getCollaborationUse().isSuperTypeOf(domainElement.eClass())) {
 				return CollaborationUse2EditPart.VISUAL_ID;
 			}
 			if (UMLPackage.eINSTANCE.getProperty().isSuperTypeOf(domainElement.eClass()) && isProperty_3007((Property) domainElement)) {
 				return PropertyEditPart.VISUAL_ID;
 			}
 			break;
 		case ClassEditPart.VISUAL_ID:
 			if (UMLPackage.eINSTANCE.getPort().isSuperTypeOf(domainElement.eClass())) {
 				return PortEditPart.VISUAL_ID;
 			}
 			if (UMLPackage.eINSTANCE.getProperty().isSuperTypeOf(domainElement.eClass()) && isProperty_3008((Property) domainElement)) {
 				return Property2EditPart.VISUAL_ID;
 			}
 			if (UMLPackage.eINSTANCE.getOperation().isSuperTypeOf(domainElement.eClass())) {
 				return OperationEditPart.VISUAL_ID;
 			}
 			if (UMLPackage.eINSTANCE.getClass_().isSuperTypeOf(domainElement.eClass())) {
 				return Class2EditPart.VISUAL_ID;
 			}
 			break;
 		case Package2EditPart.VISUAL_ID:
 			if (UMLPackage.eINSTANCE.getElementImport().isSuperTypeOf(domainElement.eClass())) {
 				return ElementImportEditPart.VISUAL_ID;
 			}
 			break;
 		case Class3EditPart.VISUAL_ID:
 			if (UMLPackage.eINSTANCE.getPort().isSuperTypeOf(domainElement.eClass())) {
 				return Port3EditPart.VISUAL_ID;
 			}
 			if (UMLPackage.eINSTANCE.getProperty().isSuperTypeOf(domainElement.eClass()) && isProperty_3014((Property) domainElement)) {
 				return Property3EditPart.VISUAL_ID;
 			}
 			break;
 		case InstanceSpecificationEditPart.VISUAL_ID:
 			if (UMLPackage.eINSTANCE.getSlot().isSuperTypeOf(domainElement.eClass())) {
 				return SlotEditPart.VISUAL_ID;
 			}
 			break;
 		case Property3EditPart.VISUAL_ID:
 			if (UMLPackage.eINSTANCE.getPort().isSuperTypeOf(domainElement.eClass())) {
 				return Port2EditPart.VISUAL_ID;
 			}
 			break;
 		case PackageEditPart.VISUAL_ID:
 			if (UMLPackage.eINSTANCE.getCollaboration().isSuperTypeOf(domainElement.eClass())) {
 				return CollaborationEditPart.VISUAL_ID;
 			}
 			if (UMLPackage.eINSTANCE.getClass_().isSuperTypeOf(domainElement.eClass()) && isClass_2006((Class) domainElement)) {
 				return ClassEditPart.VISUAL_ID;
 			}
 			if (UMLPackage.eINSTANCE.getClass_().isSuperTypeOf(domainElement.eClass()) && isClass_2007((Class) domainElement)) {
 				return Class3EditPart.VISUAL_ID;
 			}
 			if (UMLPackage.eINSTANCE.getInterface().isSuperTypeOf(domainElement.eClass())) {
 				return InterfaceEditPart.VISUAL_ID;
 			}
 			if (UMLPackage.eINSTANCE.getInstanceSpecification().isSuperTypeOf(domainElement.eClass())) {
 				return InstanceSpecificationEditPart.VISUAL_ID;
 			}
 			if (UMLPackage.eINSTANCE.getConstraint().isSuperTypeOf(domainElement.eClass())) {
 				return ConstraintEditPart.VISUAL_ID;
 			}
 			if (UMLPackage.eINSTANCE.getComment().isSuperTypeOf(domainElement.eClass())) {
 				return CommentEditPart.VISUAL_ID;
 			}
			if (UMLPackage.eINSTANCE.getCollaborationUse().isSuperTypeOf(domainElement.eClass())) {
				return CollaborationUse2EditPart.VISUAL_ID;
			}
			if (UMLPackage.eINSTANCE.getProperty().isSuperTypeOf(domainElement.eClass()) && isProperty_3007((Property) domainElement)) {
				return PropertyEditPart.VISUAL_ID;
			}
			if (UMLPackage.eINSTANCE.getProperty().isSuperTypeOf(domainElement.eClass()) && isProperty_3008((Property) domainElement)) {
				return Property2EditPart.VISUAL_ID;
			}
			if (UMLPackage.eINSTANCE.getOperation().isSuperTypeOf(domainElement.eClass())) {
				return OperationEditPart.VISUAL_ID;
			}
			if (UMLPackage.eINSTANCE.getClass_().isSuperTypeOf(domainElement.eClass())) {
				return Class2EditPart.VISUAL_ID;
			}
			if (UMLPackage.eINSTANCE.getElementImport().isSuperTypeOf(domainElement.eClass())) {
				return ElementImportEditPart.VISUAL_ID;
			}
			if (UMLPackage.eINSTANCE.getProperty().isSuperTypeOf(domainElement.eClass()) && isProperty_3014((Property) domainElement)) {
				return Property3EditPart.VISUAL_ID;
			}
			if (UMLPackage.eINSTANCE.getSlot().isSuperTypeOf(domainElement.eClass())) {
				return SlotEditPart.VISUAL_ID;
			}
 			break;
 		}
 		return -1;
 	}
 
 	/**
 	 * @generated
 	 */
 	protected static boolean canSubstitute(int visualId, int substituteCandidate) {
 		if (visualId == substituteCandidate) {
 			return true;
 		}
 		switch (visualId) {
 		case ClassEditPart.VISUAL_ID:
 			return (substituteCandidate == Class3EditPart.VISUAL_ID);
 		case Class3EditPart.VISUAL_ID:
 			return (substituteCandidate == ClassEditPart.VISUAL_ID);
 
 		default:
 			break;
 		}
 		return false;
 	}
 
 	/**
 	 * @generated
 	 */
 	public static boolean checkNodeVisualID(View containerView, EObject domainElement, int candidate) {
 		if (candidate == -1) {
 			//unrecognized id is always bad
 			return false;
 		}
 		int basic = getNodeVisualID(containerView, domainElement);
 		return basic == candidate || canSubstitute(basic, candidate);
 	}
 
 	/**
 	 * @generated
 	 */
 	public static boolean isCompartmentVisualID(int visualID) {
 		switch (visualID) {
 		case CollaborationContentsEditPart.VISUAL_ID:
 		case ClassAttributesEditPart.VISUAL_ID:
 		case ClassOperationsEditPart.VISUAL_ID:
 		case ClassClassesEditPart.VISUAL_ID:
 		case PackageImportsEditPart.VISUAL_ID:
 		case ClassClass_contentsEditPart.VISUAL_ID:
 		case InstanceSpecificationSlotsEditPart.VISUAL_ID:
 			return true;
 		default:
 			break;
 		}
 		return false;
 	}
 
 	/**
 	 * @generated
 	 */
 	public static boolean isSemanticLeafVisualID(int visualID) {
 		switch (visualID) {
 		case Package2EditPart.VISUAL_ID:
 		case InterfaceEditPart.VISUAL_ID:
 		case InstanceSpecificationEditPart.VISUAL_ID:
 		case ConstraintEditPart.VISUAL_ID:
 		case CommentEditPart.VISUAL_ID:
 		case CollaborationUse2EditPart.VISUAL_ID:
 		case PropertyEditPart.VISUAL_ID:
 		case ParameterEditPart.VISUAL_ID:
 		case Property2EditPart.VISUAL_ID:
 		case OperationEditPart.VISUAL_ID:
 		case Class2EditPart.VISUAL_ID:
 		case PortEditPart.VISUAL_ID:
 		case ElementImportEditPart.VISUAL_ID:
 		case Port2EditPart.VISUAL_ID:
 		case Port3EditPart.VISUAL_ID:
 		case SlotEditPart.VISUAL_ID:
 			return true;
 		default:
 			break;
 		}
 		return false;
 	}
 
 	/**
 	 * @generated
 	 */
 	public static final IVisualIDRegistry TYPED_ADAPTER = new IVisualIDRegistry() {
 
 		/**
 		 * @generated
 		 */
 		public String getModelID(View view) {
 			return org.eclipse.uml2.diagram.csd.part.UMLVisualIDRegistry.getModelID(view);
 		}
 
 		/**
 		 * @generated
 		 */
 		public int getVisualID(View view) {
 			return org.eclipse.uml2.diagram.csd.part.UMLVisualIDRegistry.getVisualID(view);
 		}
 
 		/**
 		 * @generated
 		 */
 		public int getNodeVisualID(View containerView, EObject domainElement) {
 			return org.eclipse.uml2.diagram.csd.part.UMLVisualIDRegistry.getNodeVisualID(containerView, domainElement);
 		}
 
 		/**
 		 * @generated
 		 */
 		public boolean checkNodeVisualID(View containerView, EObject domainElement, int candidate) {
 			return org.eclipse.uml2.diagram.csd.part.UMLVisualIDRegistry.checkNodeVisualID(containerView, domainElement, candidate);
 		}
 
 		/**
 		 * @generated
 		 */
 		public boolean isCompartmentVisualID(int visualID) {
 			return org.eclipse.uml2.diagram.csd.part.UMLVisualIDRegistry.isCompartmentVisualID(visualID);
 		}
 
 		/**
 		 * @generated
 		 */
 		public boolean isSemanticLeafVisualID(int visualID) {
 			return org.eclipse.uml2.diagram.csd.part.UMLVisualIDRegistry.isSemanticLeafVisualID(visualID);
 		}
 
 		/**
 		 * @generated
 		 */
 		public boolean isShortcutDescendant(View view) {
 			return org.eclipse.uml2.diagram.csd.part.UMLVisualIDRegistry.isShortcutDescendant(view);
 		}
 
 	};
 
 	/**
 	 * @generated
 	 */
 	public static boolean isShortcutDescendant(View view) {
 		View diagram = view.getDiagram();
 		while (view != diagram && view != null) {
 			EAnnotation annotation = view.getEAnnotation("Shortcut"); //$NON-NLS-1$
 			if (annotation != null) {
 				return true;
 			}
 			view = (View) view.eContainer();
 		}
 		return false;
 	}
 
 }
