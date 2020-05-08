 package org.eclipse.uml2.diagram.profile.part;
 
 import org.eclipse.core.runtime.Platform;
 
 import org.eclipse.emf.ecore.EAnnotation;
 import org.eclipse.emf.ecore.EClass;
 import org.eclipse.emf.ecore.EObject;
 
 import org.eclipse.gmf.runtime.notation.Diagram;
 import org.eclipse.gmf.runtime.notation.View;
 
 import org.eclipse.uml2.diagram.profile.edit.parts.ConstraintEditPart;
 import org.eclipse.uml2.diagram.profile.edit.parts.ElementImport2EditPart;
 import org.eclipse.uml2.diagram.profile.edit.parts.ElementImportEditPart;
 import org.eclipse.uml2.diagram.profile.edit.parts.EnumerationEditPart;
 import org.eclipse.uml2.diagram.profile.edit.parts.EnumerationLiteralEditPart;
 import org.eclipse.uml2.diagram.profile.edit.parts.EnumerationLiteralsEditPart;
 import org.eclipse.uml2.diagram.profile.edit.parts.EnumerationNameEditPart;
 import org.eclipse.uml2.diagram.profile.edit.parts.ExtensionEditPart;
 import org.eclipse.uml2.diagram.profile.edit.parts.ExtensionLink_requiredEditPart;
 import org.eclipse.uml2.diagram.profile.edit.parts.GeneralizationEditPart;
 import org.eclipse.uml2.diagram.profile.edit.parts.Profile2EditPart;
 import org.eclipse.uml2.diagram.profile.edit.parts.Profile3EditPart;
 import org.eclipse.uml2.diagram.profile.edit.parts.ProfileContentsEditPart;
 import org.eclipse.uml2.diagram.profile.edit.parts.ProfileEditPart;
 import org.eclipse.uml2.diagram.profile.edit.parts.ProfileName2EditPart;
 import org.eclipse.uml2.diagram.profile.edit.parts.ProfileNameEditPart;
 import org.eclipse.uml2.diagram.profile.edit.parts.ProfileProfileLabelsEditPart;
 import org.eclipse.uml2.diagram.profile.edit.parts.PropertyEditPart;
 import org.eclipse.uml2.diagram.profile.edit.parts.ReferencedMetaclassNode_classNameEditPart;
 import org.eclipse.uml2.diagram.profile.edit.parts.Stereotype2EditPart;
 import org.eclipse.uml2.diagram.profile.edit.parts.StereotypeAttributesEditPart;
 import org.eclipse.uml2.diagram.profile.edit.parts.StereotypeConstraintsEditPart;
 import org.eclipse.uml2.diagram.profile.edit.parts.StereotypeEditPart;
 import org.eclipse.uml2.diagram.profile.edit.parts.StereotypeNameEditPart;
 
 import org.eclipse.uml2.diagram.profile.expressions.UMLAbstractExpression;
 import org.eclipse.uml2.diagram.profile.expressions.UMLOCLFactory;
 
 import org.eclipse.uml2.uml.Constraint;
 import org.eclipse.uml2.uml.ElementImport;
 import org.eclipse.uml2.uml.Enumeration;
 import org.eclipse.uml2.uml.EnumerationLiteral;
 import org.eclipse.uml2.uml.Extension;
 import org.eclipse.uml2.uml.Generalization;
 import org.eclipse.uml2.uml.Profile;
 import org.eclipse.uml2.uml.Property;
 import org.eclipse.uml2.uml.Stereotype;
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
 	private static final String DEBUG_KEY = UMLDiagramEditorPlugin.getInstance().getBundle().getSymbolicName() + "/debug/visualID"; //$NON-NLS-1$
 
 	/**
 	 * @generated
 	 */
 	public static int getVisualID(View view) {
 		if (view instanceof Diagram) {
 			if (ProfileEditPart.MODEL_ID.equals(view.getType())) {
 				return ProfileEditPart.VISUAL_ID;
 			} else {
 				return -1;
 			}
 		}
 		return getVisualID(view.getType());
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
 		EClass domainElementMetaclass = domainElement.eClass();
 		return getDiagramVisualID(domainElement, domainElementMetaclass);
 	}
 
 	/**
 	 * @generated
 	 */
 	private static int getDiagramVisualID(EObject domainElement, EClass domainElementMetaclass) {
 		if (UMLPackage.eINSTANCE.getProfile().isSuperTypeOf(domainElementMetaclass) && isDiagramProfile_1000((Profile) domainElement)) {
 			return ProfileEditPart.VISUAL_ID;
 		}
 		return getUnrecognizedDiagramID(domainElement);
 	}
 
 	/**
 	 * @generated
 	 */
 	public static int getNodeVisualID(View containerView, EObject domainElement) {
 		if (domainElement == null) {
 			return -1;
 		}
 		EClass domainElementMetaclass = domainElement.eClass();
 		return getNodeVisualID(containerView, domainElement, domainElementMetaclass, null);
 	}
 
 	/**
	 * @generated
 	 *
 	 * We want to additionally show the Canvas Semantical Element in the auxiliary 
 	 * Profile3EditPart (that serves as a visual container for children imports). 
 	 * To do this, we modified CanonicalEditPolicy to add the Canvas semantic Element into the children 
 	 * list. The only remaining part is to return correct VID for this special case.
 	 * 
 	 * @see ProfileCanonicalEditPolicy#getSemanticChildrenList
 	 *
 	 */
 	public static int getNodeVisualID(View containerView, EObject domainElement, EClass domainElementMetaclass, String semanticHint) {
 		String containerModelID = getModelID(containerView);
 		if (!ProfileEditPart.MODEL_ID.equals(containerModelID)) {
 			return -1;
 		}
 		int containerVisualID;
 		if (ProfileEditPart.MODEL_ID.equals(containerModelID)) {
 			containerVisualID = getVisualID(containerView);
 		} else {
 			if (containerView instanceof Diagram) {
 				containerVisualID = ProfileEditPart.VISUAL_ID;
 			} else {
 				return -1;
 			}
 		}
 		if (containerView instanceof Diagram && domainElement != null && domainElement.equals(containerView.getElement())) {
 			return Profile3EditPart.VISUAL_ID;
 		}
 		int nodeVisualID = semanticHint != null ? getVisualID(semanticHint) : -1;
 		switch (containerVisualID) {
 		case StereotypeEditPart.VISUAL_ID:
 			if (StereotypeNameEditPart.VISUAL_ID == nodeVisualID) {
 				return StereotypeNameEditPart.VISUAL_ID;
 			}
 			if (StereotypeAttributesEditPart.VISUAL_ID == nodeVisualID) {
 				return StereotypeAttributesEditPart.VISUAL_ID;
 			}
 			if (StereotypeConstraintsEditPart.VISUAL_ID == nodeVisualID) {
 				return StereotypeConstraintsEditPart.VISUAL_ID;
 			}
 			return getUnrecognizedStereotype_2001ChildNodeID(domainElement, semanticHint);
 		case Profile2EditPart.VISUAL_ID:
 			if (ProfileNameEditPart.VISUAL_ID == nodeVisualID) {
 				return ProfileNameEditPart.VISUAL_ID;
 			}
 			if (ProfileContentsEditPart.VISUAL_ID == nodeVisualID) {
 				return ProfileContentsEditPart.VISUAL_ID;
 			}
 			return getUnrecognizedProfile_2002ChildNodeID(domainElement, semanticHint);
 		case EnumerationEditPart.VISUAL_ID:
 			if (EnumerationNameEditPart.VISUAL_ID == nodeVisualID) {
 				return EnumerationNameEditPart.VISUAL_ID;
 			}
 			if (EnumerationLiteralsEditPart.VISUAL_ID == nodeVisualID) {
 				return EnumerationLiteralsEditPart.VISUAL_ID;
 			}
 			return getUnrecognizedEnumeration_2003ChildNodeID(domainElement, semanticHint);
 		case ElementImportEditPart.VISUAL_ID:
 			if (ReferencedMetaclassNode_classNameEditPart.VISUAL_ID == nodeVisualID) {
 				return ReferencedMetaclassNode_classNameEditPart.VISUAL_ID;
 			}
 			return getUnrecognizedElementImport_2006ChildNodeID(domainElement, semanticHint);
 		case Profile3EditPart.VISUAL_ID:
 			if (ProfileName2EditPart.VISUAL_ID == nodeVisualID) {
 				return ProfileName2EditPart.VISUAL_ID;
 			}
 			if (ProfileProfileLabelsEditPart.VISUAL_ID == nodeVisualID) {
 				return ProfileProfileLabelsEditPart.VISUAL_ID;
 			}
 			return getUnrecognizedProfile_2007ChildNodeID(domainElement, semanticHint);
 		case PropertyEditPart.VISUAL_ID:
 			return getUnrecognizedProperty_3001ChildNodeID(domainElement, semanticHint);
 		case ConstraintEditPart.VISUAL_ID:
 			return getUnrecognizedConstraint_3008ChildNodeID(domainElement, semanticHint);
 		case Stereotype2EditPart.VISUAL_ID:
 			return getUnrecognizedStereotype_3003ChildNodeID(domainElement, semanticHint);
 		case EnumerationLiteralEditPart.VISUAL_ID:
 			return getUnrecognizedEnumerationLiteral_3005ChildNodeID(domainElement, semanticHint);
 		case ElementImport2EditPart.VISUAL_ID:
 			return getUnrecognizedElementImport_3009ChildNodeID(domainElement, semanticHint);
 		case StereotypeAttributesEditPart.VISUAL_ID:
 			if ((semanticHint == null || PropertyEditPart.VISUAL_ID == nodeVisualID) && UMLPackage.eINSTANCE.getProperty().isSuperTypeOf(domainElementMetaclass)
 					&& (domainElement == null || isNodeProperty_3001((Property) domainElement))) {
 				return PropertyEditPart.VISUAL_ID;
 			}
 			return getUnrecognizedStereotypeAttributes_7001ChildNodeID(domainElement, semanticHint);
 		case StereotypeConstraintsEditPart.VISUAL_ID:
 			if ((semanticHint == null || ConstraintEditPart.VISUAL_ID == nodeVisualID) && UMLPackage.eINSTANCE.getConstraint().isSuperTypeOf(domainElementMetaclass)
 					&& (domainElement == null || isNodeConstraint_3008((Constraint) domainElement))) {
 				return ConstraintEditPart.VISUAL_ID;
 			}
 			return getUnrecognizedStereotypeConstraints_7002ChildNodeID(domainElement, semanticHint);
 		case ProfileContentsEditPart.VISUAL_ID:
 			if ((semanticHint == null || Stereotype2EditPart.VISUAL_ID == nodeVisualID) && UMLPackage.eINSTANCE.getStereotype().isSuperTypeOf(domainElementMetaclass)
 					&& (domainElement == null || isNodeStereotype_3003((Stereotype) domainElement))) {
 				return Stereotype2EditPart.VISUAL_ID;
 			}
 			return getUnrecognizedProfileContents_7003ChildNodeID(domainElement, semanticHint);
 		case EnumerationLiteralsEditPart.VISUAL_ID:
 			if ((semanticHint == null || EnumerationLiteralEditPart.VISUAL_ID == nodeVisualID) && UMLPackage.eINSTANCE.getEnumerationLiteral().isSuperTypeOf(domainElementMetaclass)
 					&& (domainElement == null || isNodeEnumerationLiteral_3005((EnumerationLiteral) domainElement))) {
 				return EnumerationLiteralEditPart.VISUAL_ID;
 			}
 			return getUnrecognizedEnumerationLiterals_7004ChildNodeID(domainElement, semanticHint);
 		case ProfileProfileLabelsEditPart.VISUAL_ID:
 			if ((semanticHint == null || ElementImport2EditPart.VISUAL_ID == nodeVisualID) && UMLPackage.eINSTANCE.getElementImport().isSuperTypeOf(domainElementMetaclass)
 					&& (domainElement == null || isNodeElementImport_3009((ElementImport) domainElement))) {
 				return ElementImport2EditPart.VISUAL_ID;
 			}
 			return getUnrecognizedProfileProfileLabels_7005ChildNodeID(domainElement, semanticHint);
 		case ProfileEditPart.VISUAL_ID:
 			if ((semanticHint == null || StereotypeEditPart.VISUAL_ID == nodeVisualID) && UMLPackage.eINSTANCE.getStereotype().isSuperTypeOf(domainElementMetaclass)
 					&& (domainElement == null || isNodeStereotype_2001((Stereotype) domainElement))) {
 				return StereotypeEditPart.VISUAL_ID;
 			}
 			if ((semanticHint == null || Profile2EditPart.VISUAL_ID == nodeVisualID) && UMLPackage.eINSTANCE.getProfile().isSuperTypeOf(domainElementMetaclass)
 					&& (domainElement == null || isNodeProfile_2002((Profile) domainElement))) {
 				return Profile2EditPart.VISUAL_ID;
 			}
 			if ((semanticHint == null || EnumerationEditPart.VISUAL_ID == nodeVisualID) && UMLPackage.eINSTANCE.getEnumeration().isSuperTypeOf(domainElementMetaclass)
 					&& (domainElement == null || isNodeEnumeration_2003((Enumeration) domainElement))) {
 				return EnumerationEditPart.VISUAL_ID;
 			}
 			if ((semanticHint == null || ElementImportEditPart.VISUAL_ID == nodeVisualID) && UMLPackage.eINSTANCE.getElementImport().isSuperTypeOf(domainElementMetaclass)
 					&& (domainElement == null || isNodeElementImport_2006((ElementImport) domainElement))) {
 				return ElementImportEditPart.VISUAL_ID;
 			}
 			if ((semanticHint == null || Profile3EditPart.VISUAL_ID == nodeVisualID) && UMLPackage.eINSTANCE.getProfile().isSuperTypeOf(domainElementMetaclass)
 					&& (domainElement == null || isNodeProfile_2007((Profile) domainElement))) {
 				return Profile3EditPart.VISUAL_ID;
 			}
 			return getUnrecognizedProfile_1000ChildNodeID(domainElement, semanticHint);
 		case ExtensionEditPart.VISUAL_ID:
 			if (ExtensionLink_requiredEditPart.VISUAL_ID == nodeVisualID) {
 				return ExtensionLink_requiredEditPart.VISUAL_ID;
 			}
 			return getUnrecognizedExtension_4002LinkLabelID(semanticHint);
 		}
 		return -1;
 	}
 
 	/**
 	 * @generated
 	 */
 	public static int getLinkWithClassVisualID(EObject domainElement) {
 		if (domainElement == null) {
 			return -1;
 		}
 		EClass domainElementMetaclass = domainElement.eClass();
 		return getLinkWithClassVisualID(domainElement, domainElementMetaclass);
 	}
 
 	/**
 	 * @generated
 	 */
 	public static int getLinkWithClassVisualID(EObject domainElement, EClass domainElementMetaclass) {
 		if (UMLPackage.eINSTANCE.getGeneralization().isSuperTypeOf(domainElementMetaclass) && (domainElement == null || isLinkWithClassGeneralization_4001((Generalization) domainElement))) {
 			return GeneralizationEditPart.VISUAL_ID;
 		} else if (UMLPackage.eINSTANCE.getExtension().isSuperTypeOf(domainElementMetaclass) && (domainElement == null || isLinkWithClassExtension_4002((Extension) domainElement))) {
 			return ExtensionEditPart.VISUAL_ID;
 		} else {
 			return getUnrecognizedLinkWithClassID(domainElement);
 		}
 	}
 
 	/**
 	 * User can change implementation of this method to check some additional 
 	 * conditions here.
 	 *
 	 * @generated
 	 */
 	private static boolean isDiagramProfile_1000(Profile element) {
 		return true;
 	}
 
 	/**
 	 * User can change implementation of this method to handle some specific
 	 * situations not covered by default logic.
 	 *
 	 * @generated
 	 */
 	private static int getUnrecognizedDiagramID(EObject domainElement) {
 		return -1;
 	}
 
 	/**
 	 * User can change implementation of this method to check some additional 
 	 * conditions here.
 	 *
 	 * @generated
 	 */
 	private static boolean isNodeStereotype_2001(Stereotype element) {
 		return Stereotype_2001.matches(element);
 	}
 
 	/**
 	 * User can change implementation of this method to check some additional 
 	 * conditions here.
 	 *
 	 * @generated
 	 */
 	private static boolean isNodeProfile_2002(Profile element) {
 		return Profile_2002.matches(element);
 	}
 
 	/**
 	 * User can change implementation of this method to check some additional 
 	 * conditions here.
 	 *
 	 * @generated
 	 */
 	private static boolean isNodeEnumeration_2003(Enumeration element) {
 		return true;
 	}
 
 	/**
 	 * User can change implementation of this method to check some additional 
 	 * conditions here.
 	 *
 	 * @generated
 	 */
 	private static boolean isNodeElementImport_2006(ElementImport element) {
 		return ElementImport_2006.matches(element);
 	}
 
 	/**
 	 * User can change implementation of this method to check some additional 
 	 * conditions here.
 	 *
 	 * @generated
 	 */
 	private static boolean isNodeProfile_2007(Profile element) {
 		return true;
 	}
 
 	/**
 	 * User can change implementation of this method to check some additional 
 	 * conditions here.
 	 *
 	 * @generated
 	 */
 	private static boolean isNodeProperty_3001(Property element) {
 		return Property_3001.matches(element);
 	}
 
 	/**
 	 * User can change implementation of this method to check some additional 
 	 * conditions here.
 	 *
 	 * @generated
 	 */
 	private static boolean isNodeConstraint_3008(Constraint element) {
 		return true;
 	}
 
 	/**
 	 * User can change implementation of this method to check some additional 
 	 * conditions here.
 	 *
 	 * @generated
 	 */
 	private static boolean isNodeStereotype_3003(Stereotype element) {
 		return true;
 	}
 
 	/**
 	 * User can change implementation of this method to check some additional 
 	 * conditions here.
 	 *
 	 * @generated
 	 */
 	private static boolean isNodeEnumerationLiteral_3005(EnumerationLiteral element) {
 		return true;
 	}
 
 	/**
 	 * User can change implementation of this method to check some additional 
 	 * conditions here.
 	 *
 	 * @generated
 	 */
 	private static boolean isNodeElementImport_3009(ElementImport element) {
 		return ElementImport_3009.matches(element);
 	}
 
 	/**
 	 * User can change implementation of this method to handle some specific
 	 * situations not covered by default logic.
 	 *
 	 * @generated
 	 */
 	private static int getUnrecognizedStereotype_2001ChildNodeID(EObject domainElement, String semanticHint) {
 		return -1;
 	}
 
 	/**
 	 * User can change implementation of this method to handle some specific
 	 * situations not covered by default logic.
 	 *
 	 * @generated
 	 */
 	private static int getUnrecognizedProfile_2002ChildNodeID(EObject domainElement, String semanticHint) {
 		return -1;
 	}
 
 	/**
 	 * User can change implementation of this method to handle some specific
 	 * situations not covered by default logic.
 	 *
 	 * @generated
 	 */
 	private static int getUnrecognizedEnumeration_2003ChildNodeID(EObject domainElement, String semanticHint) {
 		return -1;
 	}
 
 	/**
 	 * User can change implementation of this method to handle some specific
 	 * situations not covered by default logic.
 	 *
 	 * @generated
 	 */
 	private static int getUnrecognizedElementImport_2006ChildNodeID(EObject domainElement, String semanticHint) {
 		return -1;
 	}
 
 	/**
 	 * User can change implementation of this method to handle some specific
 	 * situations not covered by default logic.
 	 *
 	 * @generated
 	 */
 	private static int getUnrecognizedProfile_2007ChildNodeID(EObject domainElement, String semanticHint) {
 		return -1;
 	}
 
 	/**
 	 * User can change implementation of this method to handle some specific
 	 * situations not covered by default logic.
 	 *
 	 * @generated
 	 */
 	private static int getUnrecognizedProperty_3001ChildNodeID(EObject domainElement, String semanticHint) {
 		return -1;
 	}
 
 	/**
 	 * User can change implementation of this method to handle some specific
 	 * situations not covered by default logic.
 	 *
 	 * @generated
 	 */
 	private static int getUnrecognizedConstraint_3008ChildNodeID(EObject domainElement, String semanticHint) {
 		return -1;
 	}
 
 	/**
 	 * User can change implementation of this method to handle some specific
 	 * situations not covered by default logic.
 	 *
 	 * @generated
 	 */
 	private static int getUnrecognizedStereotype_3003ChildNodeID(EObject domainElement, String semanticHint) {
 		return -1;
 	}
 
 	/**
 	 * User can change implementation of this method to handle some specific
 	 * situations not covered by default logic.
 	 *
 	 * @generated
 	 */
 	private static int getUnrecognizedEnumerationLiteral_3005ChildNodeID(EObject domainElement, String semanticHint) {
 		return -1;
 	}
 
 	/**
 	 * User can change implementation of this method to handle some specific
 	 * situations not covered by default logic.
 	 *
 	 * @generated
 	 */
 	private static int getUnrecognizedElementImport_3009ChildNodeID(EObject domainElement, String semanticHint) {
 		return -1;
 	}
 
 	/**
 	 * User can change implementation of this method to handle some specific
 	 * situations not covered by default logic.
 	 *
 	 * @generated
 	 */
 	private static int getUnrecognizedStereotypeAttributes_7001ChildNodeID(EObject domainElement, String semanticHint) {
 		return -1;
 	}
 
 	/**
 	 * User can change implementation of this method to handle some specific
 	 * situations not covered by default logic.
 	 *
 	 * @generated
 	 */
 	private static int getUnrecognizedStereotypeConstraints_7002ChildNodeID(EObject domainElement, String semanticHint) {
 		return -1;
 	}
 
 	/**
 	 * User can change implementation of this method to handle some specific
 	 * situations not covered by default logic.
 	 *
 	 * @generated
 	 */
 	private static int getUnrecognizedProfileContents_7003ChildNodeID(EObject domainElement, String semanticHint) {
 		return -1;
 	}
 
 	/**
 	 * User can change implementation of this method to handle some specific
 	 * situations not covered by default logic.
 	 *
 	 * @generated
 	 */
 	private static int getUnrecognizedEnumerationLiterals_7004ChildNodeID(EObject domainElement, String semanticHint) {
 		return -1;
 	}
 
 	/**
 	 * User can change implementation of this method to handle some specific
 	 * situations not covered by default logic.
 	 *
 	 * @generated
 	 */
 	private static int getUnrecognizedProfileProfileLabels_7005ChildNodeID(EObject domainElement, String semanticHint) {
 		return -1;
 	}
 
 	/**
 	 * User can change implementation of this method to handle some specific
 	 * situations not covered by default logic.
 	 *
 	 * @generated
 	 */
 	private static int getUnrecognizedProfile_1000ChildNodeID(EObject domainElement, String semanticHint) {
 		return -1;
 	}
 
 	/**
 	 * User can change implementation of this method to handle some specific
 	 * situations not covered by default logic.
 	 *
 	 * @generated
 	 */
 	private static int getUnrecognizedExtension_4002LinkLabelID(String semanticHint) {
 		return -1;
 	}
 
 	/**
 	 * User can change implementation of this method to handle some specific
 	 * situations not covered by default logic.
 	 *
 	 * @generated
 	 */
 	private static int getUnrecognizedLinkWithClassID(EObject domainElement) {
 		return -1;
 	}
 
 	/**
 	 * User can change implementation of this method to check some additional 
 	 * conditions here.
 	 *
 	 * @generated
 	 */
 	private static boolean isLinkWithClassGeneralization_4001(Generalization element) {
 		return true;
 	}
 
 	/**
 	 * User can change implementation of this method to check some additional 
 	 * conditions here.
 	 *
 	 * @generated
 	 */
 	private static boolean isLinkWithClassExtension_4002(Extension element) {
 		return true;
 	}
 
 	/**
 	 * @generated
 	 */
 	private static final Matcher Property_3001 = new Matcher(UMLOCLFactory.getExpression("self.association.oclIsUndefined() or not self.association.oclIsKindOf(uml::Extension)", //$NON-NLS-1$
 			UMLPackage.eINSTANCE.getProperty()));
 
 	/**
 	 * @generated
 	 */
 	private static final Matcher ElementImport_3009 = new Matcher(UMLOCLFactory.getExpression(
 			"let imported : NamedElement = self.importedElement in \r\nnot imported.oclIsKindOf(Class) or not imported.oclAsType(Class).isMetaclass()\r\n", //$NON-NLS-1$
 			UMLPackage.eINSTANCE.getElementImport()));
 
 	/**
 	 * @generated
 	 */
 	private static final Matcher Stereotype_2001 = new Matcher(UMLOCLFactory.getExpression(
 			"generalization.general->forAll(e | e.oclIsKindOf(uml::Stereotype)) and\r\ngeneralization.specific->forAll(e | e.oclIsKindOf(uml::Stereotype))", //$NON-NLS-1$
 			UMLPackage.eINSTANCE.getStereotype()));
 
 	/**
 	 * @generated
 	 */
 	private static final Matcher Profile_2002 = new Matcher(
 			UMLOCLFactory
 					.getExpression(
 							"self.metaclassReference.importedElement->\r\nselect(c | c.oclIsKindOf(uml::Classifier) and\r\n(c.oclAsType(uml::Classifier).generalization.general.namespace->exists(n|n=self) or \r\n c.oclAsType(uml::Classifier).generalization.specific.namespace->exists(n|n=self)))->isEmpty()\r\nand \r\n(self.metamodelReference->isEmpty() or self.metaclassReference->isEmpty() or self.metamodelReference.importedPackage.elementImport.importedElement.allOwningPackages()->\r\nunion(self.metaclassReference.importedElement.allOwningPackages() )->notEmpty())", //$NON-NLS-1$
 							UMLPackage.eINSTANCE.getProfile()));
 
 	/**
 	 * @generated
 	 */
 	private static final Matcher ElementImport_2006 = new Matcher(UMLOCLFactory.getExpression("self.importedElement.oclIsUndefined() or self.importedElement.oclAsType(uml::Class).isMetaclass()", //$NON-NLS-1$
 			UMLPackage.eINSTANCE.getElementImport()));
 
 	/**
 	 * @generated	
 	 */
 	static class Matcher {
 
 		/**
 		 * @generated	
 		 */
 		private UMLAbstractExpression condition;
 
 		/**
 		 * @generated	
 		 */
 		Matcher(UMLAbstractExpression conditionExpression) {
 			this.condition = conditionExpression;
 		}
 
 		/**
 		 * @generated	
 		 */
 		boolean matches(EObject object) {
 			Object result = condition.evaluate(object);
 			return result instanceof Boolean && ((Boolean) result).booleanValue();
 		}
 	}// Matcher
 }
