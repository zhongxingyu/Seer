 package org.eclipse.uml2.diagram.usecase.part;
 
 import org.eclipse.core.runtime.Platform;
 import org.eclipse.emf.ecore.EAnnotation;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.gmf.runtime.notation.Diagram;
 import org.eclipse.gmf.runtime.notation.View;
 import org.eclipse.uml2.diagram.common.genapi.IVisualIDRegistry;
 import org.eclipse.uml2.diagram.usecase.edit.parts.ActorAsRectangleEditPart;
 import org.eclipse.uml2.diagram.usecase.edit.parts.ActorEditPart;
 import org.eclipse.uml2.diagram.usecase.edit.parts.ActorInPackageEditPart;
 import org.eclipse.uml2.diagram.usecase.edit.parts.ActorName2EditPart;
 import org.eclipse.uml2.diagram.usecase.edit.parts.ActorName3EditPart;
 import org.eclipse.uml2.diagram.usecase.edit.parts.ActorNameEditPart;
 import org.eclipse.uml2.diagram.usecase.edit.parts.ActorStereoEditPart;
 import org.eclipse.uml2.diagram.usecase.edit.parts.AssociationEditPart;
 import org.eclipse.uml2.diagram.usecase.edit.parts.AssociationSourceMultiplicityEditPart;
 import org.eclipse.uml2.diagram.usecase.edit.parts.AssociationTargetMultiplicityEditPart;
 import org.eclipse.uml2.diagram.usecase.edit.parts.CommentBodyEditPart;
 import org.eclipse.uml2.diagram.usecase.edit.parts.CommentEditPart;
 import org.eclipse.uml2.diagram.usecase.edit.parts.ComponentStereoEditPart;
 import org.eclipse.uml2.diagram.usecase.edit.parts.ConstraintEditPart;
 import org.eclipse.uml2.diagram.usecase.edit.parts.ConstraintNameEditPart;
 import org.eclipse.uml2.diagram.usecase.edit.parts.DependencyEditPart;
 import org.eclipse.uml2.diagram.usecase.edit.parts.DependencyNameEditPart;
 import org.eclipse.uml2.diagram.usecase.edit.parts.DiagramHeaderEditPart;
 import org.eclipse.uml2.diagram.usecase.edit.parts.ElementImportEditPart;
 import org.eclipse.uml2.diagram.usecase.edit.parts.ExtendEditPart;
 import org.eclipse.uml2.diagram.usecase.edit.parts.ExtendsLink_fixedEditPart;
 import org.eclipse.uml2.diagram.usecase.edit.parts.ExtensionPoint2EditPart;
 import org.eclipse.uml2.diagram.usecase.edit.parts.ExtensionPointEditPart;
 import org.eclipse.uml2.diagram.usecase.edit.parts.GeneralizationEditPart;
 import org.eclipse.uml2.diagram.usecase.edit.parts.IncludeEditPart;
 import org.eclipse.uml2.diagram.usecase.edit.parts.IncludeLink_fixedEditPart;
 import org.eclipse.uml2.diagram.usecase.edit.parts.InnerUseCaseEditPart;
 import org.eclipse.uml2.diagram.usecase.edit.parts.InnerUseCaseExtensionPointsEditPart;
 import org.eclipse.uml2.diagram.usecase.edit.parts.NestedPackageEditPart;
 import org.eclipse.uml2.diagram.usecase.edit.parts.NestedPackageNameEditPart;
 import org.eclipse.uml2.diagram.usecase.edit.parts.PackageEditPart;
 import org.eclipse.uml2.diagram.usecase.edit.parts.PackageFramecontentsEditPart;
 import org.eclipse.uml2.diagram.usecase.edit.parts.PackageImportsEditPart;
 import org.eclipse.uml2.diagram.usecase.edit.parts.PackageNameEditPart;
 import org.eclipse.uml2.diagram.usecase.edit.parts.SubjectEditPart;
 import org.eclipse.uml2.diagram.usecase.edit.parts.SubjectNameEditPart;
 import org.eclipse.uml2.diagram.usecase.edit.parts.SubjectUsecasesEditPart;
 import org.eclipse.uml2.diagram.usecase.edit.parts.UseCaseAsClassEditPart;
 import org.eclipse.uml2.diagram.usecase.edit.parts.UseCaseAsClassExtensionPointsEditPart;
 import org.eclipse.uml2.diagram.usecase.edit.parts.UseCaseEditPart;
 import org.eclipse.uml2.diagram.usecase.edit.parts.UseCaseExtensionPointsEditPart;
 import org.eclipse.uml2.diagram.usecase.edit.parts.UseCaseName2EditPart;
 import org.eclipse.uml2.diagram.usecase.edit.parts.UseCaseName3EditPart;
 import org.eclipse.uml2.diagram.usecase.edit.parts.UseCaseName4EditPart;
 import org.eclipse.uml2.diagram.usecase.edit.parts.UseCaseNameEditPart;
 import org.eclipse.uml2.diagram.usecase.edit.parts.UseCasePointsEditPart;
 import org.eclipse.uml2.diagram.usecase.edit.parts.UseCaseStereoEditPart;
 import org.eclipse.uml2.diagram.usecase.edit.parts.UseCaseinPackageEditPart;
 import org.eclipse.uml2.diagram.usecase.expressions.UMLAbstractExpression;
 import org.eclipse.uml2.diagram.usecase.expressions.UMLOCLFactory;
 import org.eclipse.uml2.uml.Association;
 import org.eclipse.uml2.uml.Dependency;
 import org.eclipse.uml2.uml.Package;
 import org.eclipse.uml2.uml.UMLPackage;
 
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
 	private static final String DEBUG_KEY = "org.eclipse.uml2.diagram.usecase/debug/visualID"; //$NON-NLS-1$
 
 	/**
 	 * @generated
 	 */
 	private static UMLAbstractExpression Association_4004_Constraint;
 
 	/**
 	 * @generated
 	 */
 	private static UMLAbstractExpression Dependency_4006_Constraint;
 
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
 		return org.eclipse.uml2.diagram.usecase.part.UMLVisualIDRegistry.getVisualID(view.getType());
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
 		String containerModelID = org.eclipse.uml2.diagram.usecase.part.UMLVisualIDRegistry.getModelID(containerView);
 		if (!PackageEditPart.MODEL_ID.equals(containerModelID) && !"uml".equals(containerModelID)) { //$NON-NLS-1$
 			return -1;
 		}
 		int containerVisualID;
 		if (PackageEditPart.MODEL_ID.equals(containerModelID)) {
 			containerVisualID = org.eclipse.uml2.diagram.usecase.part.UMLVisualIDRegistry.getVisualID(containerView);
 		} else {
 			if (containerView instanceof Diagram) {
 				containerVisualID = PackageEditPart.VISUAL_ID;
 			} else {
 				return -1;
 			}
 		}
 		switch (containerVisualID) {
 		case PackageImportsEditPart.VISUAL_ID:
 			if (UMLPackage.eINSTANCE.getElementImport().isSuperTypeOf(domainElement.eClass())) {
 				return ElementImportEditPart.VISUAL_ID;
 			}
 			break;
 		case UseCaseExtensionPointsEditPart.VISUAL_ID:
 			if (UMLPackage.eINSTANCE.getExtensionPoint().isSuperTypeOf(domainElement.eClass())) {
 				return ExtensionPointEditPart.VISUAL_ID;
 			}
 			break;
 		case UseCaseAsClassExtensionPointsEditPart.VISUAL_ID:
 			if (UMLPackage.eINSTANCE.getExtensionPoint().isSuperTypeOf(domainElement.eClass())) {
 				return ExtensionPoint2EditPart.VISUAL_ID;
 			}
 			break;
 		case SubjectUsecasesEditPart.VISUAL_ID:
 			if (UMLPackage.eINSTANCE.getUseCase().isSuperTypeOf(domainElement.eClass())) {
 				return InnerUseCaseEditPart.VISUAL_ID;
 			}
 			break;
 		case InnerUseCaseExtensionPointsEditPart.VISUAL_ID:
 			if (UMLPackage.eINSTANCE.getExtensionPoint().isSuperTypeOf(domainElement.eClass())) {
 				return ExtensionPointEditPart.VISUAL_ID;
 			}
 			break;
 		case PackageFramecontentsEditPart.VISUAL_ID:
 			if (UMLPackage.eINSTANCE.getActor().isSuperTypeOf(domainElement.eClass())) {
 				return ActorInPackageEditPart.VISUAL_ID;
 			}
 			if (UMLPackage.eINSTANCE.getUseCase().isSuperTypeOf(domainElement.eClass())) {
 				return UseCaseinPackageEditPart.VISUAL_ID;
 			}
 			break;
 		case UseCasePointsEditPart.VISUAL_ID:
 			if (UMLPackage.eINSTANCE.getExtensionPoint().isSuperTypeOf(domainElement.eClass())) {
 				return ExtensionPointEditPart.VISUAL_ID;
 			}
 			break;
 		case PackageEditPart.VISUAL_ID:
 			// We want to additionally show the Canvas Semantical Element in the auxiliary 
 			// org.eclipse.uml2.diagram.usecase.edit.parts.DiagramHeaderEditPart (that serves as a pure visual container for children). 
 			// To do this, we modified CanonicalEditPolicy to add the Canvas semantic Element into the children 
 			// list. The only remaining part is to return correct VID for this special case.
 
 			if (containerView instanceof Diagram && domainElement != null && domainElement.equals(containerView.getElement())) {
 				return DiagramHeaderEditPart.VISUAL_ID;
 			}
 			// Diagram header is already processed above
 			if (UMLPackage.eINSTANCE.getActor().isSuperTypeOf(domainElement.eClass())) {
 				return ActorEditPart.VISUAL_ID;
 			}
 			if (UMLPackage.eINSTANCE.getActor().isSuperTypeOf(domainElement.eClass())) {
 				return ActorAsRectangleEditPart.VISUAL_ID;
 			}
 			if (UMLPackage.eINSTANCE.getUseCase().isSuperTypeOf(domainElement.eClass())) {
 				return UseCaseEditPart.VISUAL_ID;
 			}
 			if (UMLPackage.eINSTANCE.getUseCase().isSuperTypeOf(domainElement.eClass())) {
 				return UseCaseAsClassEditPart.VISUAL_ID;
 			}
 			if (UMLPackage.eINSTANCE.getComponent().isSuperTypeOf(domainElement.eClass())) {
 				return SubjectEditPart.VISUAL_ID;
 			}
 			if (UMLPackage.eINSTANCE.getPackage().isSuperTypeOf(domainElement.eClass())) {
 				return NestedPackageEditPart.VISUAL_ID;
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
 		String containerModelID = org.eclipse.uml2.diagram.usecase.part.UMLVisualIDRegistry.getModelID(containerView);
 		if (!PackageEditPart.MODEL_ID.equals(containerModelID) && !"uml".equals(containerModelID)) { //$NON-NLS-1$
 			return false;
 		}
 		int containerVisualID;
 		if (PackageEditPart.MODEL_ID.equals(containerModelID)) {
 			containerVisualID = org.eclipse.uml2.diagram.usecase.part.UMLVisualIDRegistry.getVisualID(containerView);
 		} else {
 			if (containerView instanceof Diagram) {
 				containerVisualID = PackageEditPart.VISUAL_ID;
 			} else {
 				return false;
 			}
 		}
 		switch (containerVisualID) {
 		case DiagramHeaderEditPart.VISUAL_ID:
 			if (PackageNameEditPart.VISUAL_ID == nodeVisualID) {
 				return true;
 			}
 			if (PackageImportsEditPart.VISUAL_ID == nodeVisualID) {
 				return true;
 			}
 			break;
 		case ActorEditPart.VISUAL_ID:
 			if (ActorNameEditPart.VISUAL_ID == nodeVisualID) {
 				return true;
 			}
 			break;
 		case ActorAsRectangleEditPart.VISUAL_ID:
 			if (ActorName2EditPart.VISUAL_ID == nodeVisualID) {
 				return true;
 			}
 			if (ActorStereoEditPart.VISUAL_ID == nodeVisualID) {
 				return true;
 			}
 			break;
 		case UseCaseEditPart.VISUAL_ID:
 			if (UseCaseNameEditPart.VISUAL_ID == nodeVisualID) {
 				return true;
 			}
 			if (UseCaseExtensionPointsEditPart.VISUAL_ID == nodeVisualID) {
 				return true;
 			}
 			break;
 		case UseCaseAsClassEditPart.VISUAL_ID:
 			if (UseCaseName2EditPart.VISUAL_ID == nodeVisualID) {
 				return true;
 			}
 			if (UseCaseStereoEditPart.VISUAL_ID == nodeVisualID) {
 				return true;
 			}
 			if (UseCaseAsClassExtensionPointsEditPart.VISUAL_ID == nodeVisualID) {
 				return true;
 			}
 			break;
 		case SubjectEditPart.VISUAL_ID:
 			if (SubjectNameEditPart.VISUAL_ID == nodeVisualID) {
 				return true;
 			}
 			if (ComponentStereoEditPart.VISUAL_ID == nodeVisualID) {
 				return true;
 			}
 			if (SubjectUsecasesEditPart.VISUAL_ID == nodeVisualID) {
 				return true;
 			}
 			break;
 		case NestedPackageEditPart.VISUAL_ID:
 			if (NestedPackageNameEditPart.VISUAL_ID == nodeVisualID) {
 				return true;
 			}
 			if (PackageFramecontentsEditPart.VISUAL_ID == nodeVisualID) {
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
 		case InnerUseCaseEditPart.VISUAL_ID:
 			if (UseCaseName3EditPart.VISUAL_ID == nodeVisualID) {
 				return true;
 			}
 			if (InnerUseCaseExtensionPointsEditPart.VISUAL_ID == nodeVisualID) {
 				return true;
 			}
 			break;
 		case ActorInPackageEditPart.VISUAL_ID:
 			if (ActorName3EditPart.VISUAL_ID == nodeVisualID) {
 				return true;
 			}
 			break;
 		case UseCaseinPackageEditPart.VISUAL_ID:
 			if (UseCaseName4EditPart.VISUAL_ID == nodeVisualID) {
 				return true;
 			}
 			if (UseCasePointsEditPart.VISUAL_ID == nodeVisualID) {
 				return true;
 			}
 			break;
 		case PackageImportsEditPart.VISUAL_ID:
 			if (ElementImportEditPart.VISUAL_ID == nodeVisualID) {
 				return true;
 			}
 			break;
 		case UseCaseExtensionPointsEditPart.VISUAL_ID:
 			if (ExtensionPointEditPart.VISUAL_ID == nodeVisualID) {
 				return true;
 			}
 			break;
 		case UseCaseAsClassExtensionPointsEditPart.VISUAL_ID:
 			if (ExtensionPoint2EditPart.VISUAL_ID == nodeVisualID) {
 				return true;
 			}
 			break;
 		case SubjectUsecasesEditPart.VISUAL_ID:
 			if (InnerUseCaseEditPart.VISUAL_ID == nodeVisualID) {
 				return true;
 			}
 			break;
 		case InnerUseCaseExtensionPointsEditPart.VISUAL_ID:
 			if (ExtensionPointEditPart.VISUAL_ID == nodeVisualID) {
 				return true;
 			}
 			break;
 		case PackageFramecontentsEditPart.VISUAL_ID:
 			if (ActorInPackageEditPart.VISUAL_ID == nodeVisualID) {
 				return true;
 			}
 			if (UseCaseinPackageEditPart.VISUAL_ID == nodeVisualID) {
 				return true;
 			}
 			break;
 		case UseCasePointsEditPart.VISUAL_ID:
 			if (ExtensionPointEditPart.VISUAL_ID == nodeVisualID) {
 				return true;
 			}
 			break;
 		case PackageEditPart.VISUAL_ID:
 			if (DiagramHeaderEditPart.VISUAL_ID == nodeVisualID) {
 				return true;
 			}
 			if (ActorEditPart.VISUAL_ID == nodeVisualID) {
 				return true;
 			}
 			if (ActorAsRectangleEditPart.VISUAL_ID == nodeVisualID) {
 				return true;
 			}
 			if (UseCaseEditPart.VISUAL_ID == nodeVisualID) {
 				return true;
 			}
 			if (UseCaseAsClassEditPart.VISUAL_ID == nodeVisualID) {
 				return true;
 			}
 			if (SubjectEditPart.VISUAL_ID == nodeVisualID) {
 				return true;
 			}
 			if (NestedPackageEditPart.VISUAL_ID == nodeVisualID) {
 				return true;
 			}
 			if (ConstraintEditPart.VISUAL_ID == nodeVisualID) {
 				return true;
 			}
 			if (CommentEditPart.VISUAL_ID == nodeVisualID) {
 				return true;
 			}
 			break;
 		case IncludeEditPart.VISUAL_ID:
 			if (IncludeLink_fixedEditPart.VISUAL_ID == nodeVisualID) {
 				return true;
 			}
 			break;
 		case ExtendEditPart.VISUAL_ID:
 			if (ExtendsLink_fixedEditPart.VISUAL_ID == nodeVisualID) {
 				return true;
 			}
 			break;
 		case AssociationEditPart.VISUAL_ID:
 			if (AssociationTargetMultiplicityEditPart.VISUAL_ID == nodeVisualID) {
 				return true;
 			}
 			if (AssociationSourceMultiplicityEditPart.VISUAL_ID == nodeVisualID) {
 				return true;
 			}
 			break;
 		case DependencyEditPart.VISUAL_ID:
 			if (DependencyNameEditPart.VISUAL_ID == nodeVisualID) {
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
 		if (UMLPackage.eINSTANCE.getInclude().isSuperTypeOf(domainElement.eClass())) {
 			return IncludeEditPart.VISUAL_ID;
 		}
 		if (UMLPackage.eINSTANCE.getExtend().isSuperTypeOf(domainElement.eClass())) {
 			return ExtendEditPart.VISUAL_ID;
 		}
 		if (UMLPackage.eINSTANCE.getGeneralization().isSuperTypeOf(domainElement.eClass())) {
 			return GeneralizationEditPart.VISUAL_ID;
 		}
 		if (UMLPackage.eINSTANCE.getAssociation().isSuperTypeOf(domainElement.eClass()) && isAssociation_4004((Association) domainElement)) {
 			return AssociationEditPart.VISUAL_ID;
 		}
 		if (UMLPackage.eINSTANCE.getDependency().isSuperTypeOf(domainElement.eClass()) && isDependency_4006((Dependency) domainElement)) {
 			return DependencyEditPart.VISUAL_ID;
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
 	private static boolean isAssociation_4004(Association domainElement) {
 		if (Association_4004_Constraint == null) { // lazy initialization
 			Association_4004_Constraint = UMLOCLFactory.getExpression("not self.oclIsTypeOf(uml::AssociationClass)", UMLPackage.eINSTANCE.getAssociation()); //$NON-NLS-1$
 		}
 		Object result = Association_4004_Constraint.evaluate(domainElement);
 		return result instanceof Boolean && ((Boolean) result).booleanValue();
 	}
 
 	/**
 	 * @generated
 	 */
 	private static boolean isDependency_4006(Dependency domainElement) {
 		if (Dependency_4006_Constraint == null) { // lazy initialization
 			Dependency_4006_Constraint = UMLOCLFactory
 					.getExpression(
 							"(self.oclIsTypeOf(uml::Dependency) or self.oclIsTypeOf(uml::Abstraction) or self.oclIsTypeOf(uml::Substitution) or self.oclIsTypeOf(uml::Usage)) and self.supplier->size() = 1 and self.client->size() = 1 and self.supplier->forAll(e|not e.oclIsKindOf(uml::Interface))", UMLPackage.eINSTANCE.getDependency()); //$NON-NLS-1$
 		}
 		Object result = Dependency_4006_Constraint.evaluate(domainElement);
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
 		case DiagramHeaderEditPart.VISUAL_ID:
 			if (UMLPackage.eINSTANCE.getElementImport().isSuperTypeOf(domainElement.eClass())) {
 				return ElementImportEditPart.VISUAL_ID;
 			}
 			break;
 		case UseCaseEditPart.VISUAL_ID:
 			if (UMLPackage.eINSTANCE.getExtensionPoint().isSuperTypeOf(domainElement.eClass())) {
 				return ExtensionPointEditPart.VISUAL_ID;
 			}
 			break;
 		case UseCaseAsClassEditPart.VISUAL_ID:
 			if (UMLPackage.eINSTANCE.getExtensionPoint().isSuperTypeOf(domainElement.eClass())) {
 				return ExtensionPoint2EditPart.VISUAL_ID;
 			}
 			break;
 		case SubjectEditPart.VISUAL_ID:
 			if (UMLPackage.eINSTANCE.getUseCase().isSuperTypeOf(domainElement.eClass())) {
 				return InnerUseCaseEditPart.VISUAL_ID;
 			}
 			break;
 		case NestedPackageEditPart.VISUAL_ID:
 			if (UMLPackage.eINSTANCE.getActor().isSuperTypeOf(domainElement.eClass())) {
 				return ActorInPackageEditPart.VISUAL_ID;
 			}
 			if (UMLPackage.eINSTANCE.getUseCase().isSuperTypeOf(domainElement.eClass())) {
 				return UseCaseinPackageEditPart.VISUAL_ID;
 			}
 			break;
 		case InnerUseCaseEditPart.VISUAL_ID:
 			if (UMLPackage.eINSTANCE.getExtensionPoint().isSuperTypeOf(domainElement.eClass())) {
 				return ExtensionPointEditPart.VISUAL_ID;
 			}
 			break;
 		case UseCaseinPackageEditPart.VISUAL_ID:
 			if (UMLPackage.eINSTANCE.getExtensionPoint().isSuperTypeOf(domainElement.eClass())) {
 				return ExtensionPointEditPart.VISUAL_ID;
 			}
 			break;
 		case PackageEditPart.VISUAL_ID:
 			if (UMLPackage.eINSTANCE.getActor().isSuperTypeOf(domainElement.eClass())) {
 				return ActorEditPart.VISUAL_ID;
 			}
 			if (UMLPackage.eINSTANCE.getActor().isSuperTypeOf(domainElement.eClass())) {
 				return ActorAsRectangleEditPart.VISUAL_ID;
 			}
 			if (UMLPackage.eINSTANCE.getUseCase().isSuperTypeOf(domainElement.eClass())) {
 				return UseCaseEditPart.VISUAL_ID;
 			}
 			if (UMLPackage.eINSTANCE.getUseCase().isSuperTypeOf(domainElement.eClass())) {
 				return UseCaseAsClassEditPart.VISUAL_ID;
 			}
 			if (UMLPackage.eINSTANCE.getComponent().isSuperTypeOf(domainElement.eClass())) {
 				return SubjectEditPart.VISUAL_ID;
 			}
 			if (UMLPackage.eINSTANCE.getPackage().isSuperTypeOf(domainElement.eClass())) {
 				return NestedPackageEditPart.VISUAL_ID;
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
 	protected static boolean canSubstitute(int visualId, int substituteCandidate) {
 		if (visualId == substituteCandidate) {
 			return true;
 		}
 		switch (visualId) {
 		case ActorEditPart.VISUAL_ID:
 			return (substituteCandidate == ActorAsRectangleEditPart.VISUAL_ID);
 		case ActorAsRectangleEditPart.VISUAL_ID:
 			return (substituteCandidate == ActorEditPart.VISUAL_ID);
 		case UseCaseEditPart.VISUAL_ID:
 			return (substituteCandidate == UseCaseAsClassEditPart.VISUAL_ID);
 		case UseCaseAsClassEditPart.VISUAL_ID:
 			return (substituteCandidate == UseCaseEditPart.VISUAL_ID);
 
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
 		case PackageImportsEditPart.VISUAL_ID:
 		case UseCaseExtensionPointsEditPart.VISUAL_ID:
 		case UseCaseAsClassExtensionPointsEditPart.VISUAL_ID:
 		case SubjectUsecasesEditPart.VISUAL_ID:
 		case InnerUseCaseExtensionPointsEditPart.VISUAL_ID:
 		case PackageFramecontentsEditPart.VISUAL_ID:
 		case UseCasePointsEditPart.VISUAL_ID:
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
 		case DiagramHeaderEditPart.VISUAL_ID:
 		case ActorEditPart.VISUAL_ID:
 		case ActorAsRectangleEditPart.VISUAL_ID:
 		case UseCaseEditPart.VISUAL_ID:
 		case UseCaseAsClassEditPart.VISUAL_ID:
 		case SubjectEditPart.VISUAL_ID:
 		case NestedPackageEditPart.VISUAL_ID:
 		case ConstraintEditPart.VISUAL_ID:
 		case CommentEditPart.VISUAL_ID:
 		case ElementImportEditPart.VISUAL_ID:
 		case ExtensionPointEditPart.VISUAL_ID:
 		case ExtensionPoint2EditPart.VISUAL_ID:
 		case InnerUseCaseEditPart.VISUAL_ID:
 		case ActorInPackageEditPart.VISUAL_ID:
 		case UseCaseinPackageEditPart.VISUAL_ID:
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
 			return org.eclipse.uml2.diagram.usecase.part.UMLVisualIDRegistry.getModelID(view);
 		}
 
 		/**
 		 * @generated
 		 */
 		public int getVisualID(View view) {
 			return org.eclipse.uml2.diagram.usecase.part.UMLVisualIDRegistry.getVisualID(view);
 		}
 
 		/**
 		 * @generated
 		 */
 		public int getNodeVisualID(View containerView, EObject domainElement) {
 			return org.eclipse.uml2.diagram.usecase.part.UMLVisualIDRegistry.getNodeVisualID(containerView, domainElement);
 		}
 
 		/**
 		 * @generated
 		 */
 		public boolean checkNodeVisualID(View containerView, EObject domainElement, int candidate) {
 			return org.eclipse.uml2.diagram.usecase.part.UMLVisualIDRegistry.checkNodeVisualID(containerView, domainElement, candidate);
 		}
 
 		/**
 		 * @generated
 		 */
 		public boolean isCompartmentVisualID(int visualID) {
 			return org.eclipse.uml2.diagram.usecase.part.UMLVisualIDRegistry.isCompartmentVisualID(visualID);
 		}
 
 		/**
 		 * @generated
 		 */
 		public boolean isSemanticLeafVisualID(int visualID) {
 			return org.eclipse.uml2.diagram.usecase.part.UMLVisualIDRegistry.isSemanticLeafVisualID(visualID);
 		}
 
 		/**
 		 * @generated
 		 */
 		public boolean isShortcutDescendant(View view) {
 			return org.eclipse.uml2.diagram.usecase.part.UMLVisualIDRegistry.isShortcutDescendant(view);
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
