 package org.eclipse.gmf.ecore.editor;
 
 import java.util.Map.Entry;
 
 import org.eclipse.emf.ecore.EAnnotation;
 import org.eclipse.emf.ecore.EAttribute;
 import org.eclipse.emf.ecore.EClass;
 import org.eclipse.emf.ecore.EDataType;
 import org.eclipse.emf.ecore.EEnum;
 import org.eclipse.emf.ecore.EEnumLiteral;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.ecore.EOperation;
 import org.eclipse.emf.ecore.EPackage;
 import org.eclipse.emf.ecore.EReference;
 import org.eclipse.emf.ecore.EcorePackage;
 
import org.eclipse.emf.query.ocl.conditions.OclConstraintCondition;
 
 import org.eclipse.gmf.ecore.providers.EcoreSemanticHints;
 
 import org.eclipse.gmf.runtime.notation.Diagram;
 import org.eclipse.gmf.runtime.notation.View;
 
 /**
  * This registry is used to determine which type of visual object should be
  * created for the corresponding Diagram, Node, ChildNode or Link represented 
  * by a domain model object.
  *
  * @generated
  */
 public class EcoreVisualIDRegistry {
 
 	public static final EcoreVisualIDRegistry INSTANCE = new EcoreVisualIDRegistry();
 
 	/**
 	 * @generated
 	 */
 	public int getDiagramVisualID(EObject domainElement) {
 		if (domainElement == null) {
 			return -1;
 		}
 		EClass domainElementMetaclass = domainElement.eClass();
 		return getDiagramVisualID(domainElement, domainElementMetaclass);
 	}
 
 	/**
 	 * @generated
 	 */
 	public int getDiagramVisualID(EObject domainElement, EClass domainElementMetaclass) {
 		if (EcorePackage.eINSTANCE.getEPackage().equals(domainElementMetaclass) && (domainElement != null ? isDiagramEPackage_79((EPackage) domainElement) : true)) {
 			return 79;
 		}
 		return getUnrecognizedDiagramID(domainElement);
 	}
 
 	/**
 	 * @generated
 	 */
 	public int getNodeVisualID(View containerView, EObject domainElement, String semanticHint) {
 		if (domainElement == null) {
 			return -1;
 		}
 		EClass domainElementMetaclass = domainElement.eClass();
 		return getNodeVisualID(containerView, domainElement, domainElementMetaclass, semanticHint);
 	}
 
 	/**
 	 * @generated
 	 */
 	public int getNodeVisualID(View containerView, EObject domainElement, EClass domainElementMetaclass, String semanticHint) {
 		String containerModelID = getModelID(containerView);
 		if (!"Ecore".equals(containerModelID)) {
 			return -1;
 		}
 		int containerVisualID;
 		if ("Ecore".equals(containerModelID)) {
 			containerVisualID = getVisualID(containerView);
 		} else {
 			if (containerView instanceof Diagram) {
 				containerVisualID = 79;
 			} else {
 				return -1;
 			}
 		}
 		switch (containerVisualID) {
 		case 1001:
 			if (EcoreSemanticHints.EClass_1001Labels.ECLASSNAME_4001_TEXT.equals(semanticHint)) {
 				return 4001;
 			}
 			if (EcoreSemanticHints.EClass_1001Compartments.ATTRIBUTES_5001.equals(semanticHint)) {
 				return 5001;
 			}
 			if (EcoreSemanticHints.EClass_1001Compartments.OPERATIONS_5002.equals(semanticHint)) {
 				return 5002;
 			}
 			if (EcoreSemanticHints.EClass_1001Compartments.CLASS_ANNOTATIONS_5003.equals(semanticHint)) {
 				return 5003;
 			}
 			if (semanticHint != null && !"".equals(semanticHint)) {
 				return getUnrecognizedEClass_1001ChildNodeID(semanticHint);
 			}
 			return getUnrecognizedEClass_1001ChildNodeID(domainElement);
 		case 1002:
 			if (EcoreSemanticHints.EPackage_1002Labels.EPACKAGENAME_4005_TEXT.equals(semanticHint)) {
 				return 4005;
 			}
 			if (EcoreSemanticHints.EPackage_1002Compartments.CLASSES_5004.equals(semanticHint)) {
 				return 5004;
 			}
 			if (EcoreSemanticHints.EPackage_1002Compartments.PACKAGES_5005.equals(semanticHint)) {
 				return 5005;
 			}
 			if (EcoreSemanticHints.EPackage_1002Compartments.DATA_TYPES_5006.equals(semanticHint)) {
 				return 5006;
 			}
 			if (EcoreSemanticHints.EPackage_1002Compartments.ENUMS_5007.equals(semanticHint)) {
 				return 5007;
 			}
 			if (EcoreSemanticHints.EPackage_1002Compartments.PACKAGE_ANNOTATIONS_5008.equals(semanticHint)) {
 				return 5008;
 			}
 			if (semanticHint != null && !"".equals(semanticHint)) {
 				return getUnrecognizedEPackage_1002ChildNodeID(semanticHint);
 			}
 			return getUnrecognizedEPackage_1002ChildNodeID(domainElement);
 		case 1003:
 			if (EcoreSemanticHints.EAnnotation_1003Labels.EANNOTATIONSOURCE_4011_TEXT.equals(semanticHint)) {
 				return 4011;
 			}
 			if (EcoreSemanticHints.EAnnotation_1003Compartments.DETAILS_5009.equals(semanticHint)) {
 				return 5009;
 			}
 			if (semanticHint != null && !"".equals(semanticHint)) {
 				return getUnrecognizedEAnnotation_1003ChildNodeID(semanticHint);
 			}
 			return getUnrecognizedEAnnotation_1003ChildNodeID(domainElement);
 		case 1004:
 			if (EcoreSemanticHints.EDataType_1004Labels.EDATATYPENAME_4013_TEXT.equals(semanticHint)) {
 				return 4013;
 			}
 			if (EcoreSemanticHints.EDataType_1004Compartments.DATA_TYPE_ANNOTATIONS_5010.equals(semanticHint)) {
 				return 5010;
 			}
 			if (semanticHint != null && !"".equals(semanticHint)) {
 				return getUnrecognizedEDataType_1004ChildNodeID(semanticHint);
 			}
 			return getUnrecognizedEDataType_1004ChildNodeID(domainElement);
 		case 1005:
 			if (EcoreSemanticHints.EEnum_1005Labels.EENUMNAME_4015_TEXT.equals(semanticHint)) {
 				return 4015;
 			}
 			if (EcoreSemanticHints.EEnum_1005Compartments.LITERALS_5011.equals(semanticHint)) {
 				return 5011;
 			}
 			if (EcoreSemanticHints.EEnum_1005Compartments.ENUM_ANNOTATIONS_5012.equals(semanticHint)) {
 				return 5012;
 			}
 			if (semanticHint != null && !"".equals(semanticHint)) {
 				return getUnrecognizedEEnum_1005ChildNodeID(semanticHint);
 			}
 			return getUnrecognizedEEnum_1005ChildNodeID(domainElement);
 		case 2001:
 			if (EcoreSemanticHints.EAttribute_2001Labels.EATTRIBUTENAME_4002_TEXT.equals(semanticHint)) {
 				return 4002;
 			}
 			if (semanticHint != null && !"".equals(semanticHint)) {
 				return getUnrecognizedEAttribute_2001ChildNodeID(semanticHint);
 			}
 			return getUnrecognizedEAttribute_2001ChildNodeID(domainElement);
 		case 2002:
 			if (EcoreSemanticHints.EOperation_2002Labels.EOPERATIONNAME_4003_TEXT.equals(semanticHint)) {
 				return 4003;
 			}
 			if (semanticHint != null && !"".equals(semanticHint)) {
 				return getUnrecognizedEOperation_2002ChildNodeID(semanticHint);
 			}
 			return getUnrecognizedEOperation_2002ChildNodeID(domainElement);
 		case 2003:
 			if (EcoreSemanticHints.EAnnotation_2003Labels.EANNOTATIONSOURCE_4004_TEXT.equals(semanticHint)) {
 				return 4004;
 			}
 			if (semanticHint != null && !"".equals(semanticHint)) {
 				return getUnrecognizedEAnnotation_2003ChildNodeID(semanticHint);
 			}
 			return getUnrecognizedEAnnotation_2003ChildNodeID(domainElement);
 		case 2004:
 			if (EcoreSemanticHints.EClass_2004Labels.ECLASSNAME_4006_TEXT.equals(semanticHint)) {
 				return 4006;
 			}
 			if (semanticHint != null && !"".equals(semanticHint)) {
 				return getUnrecognizedEClass_2004ChildNodeID(semanticHint);
 			}
 			return getUnrecognizedEClass_2004ChildNodeID(domainElement);
 		case 2005:
 			if (EcoreSemanticHints.EPackage_2005Labels.EPACKAGENAME_4007_TEXT.equals(semanticHint)) {
 				return 4007;
 			}
 			if (semanticHint != null && !"".equals(semanticHint)) {
 				return getUnrecognizedEPackage_2005ChildNodeID(semanticHint);
 			}
 			return getUnrecognizedEPackage_2005ChildNodeID(domainElement);
 		case 2006:
 			if (EcoreSemanticHints.EDataType_2006Labels.EDATATYPENAME_4008_TEXT.equals(semanticHint)) {
 				return 4008;
 			}
 			if (semanticHint != null && !"".equals(semanticHint)) {
 				return getUnrecognizedEDataType_2006ChildNodeID(semanticHint);
 			}
 			return getUnrecognizedEDataType_2006ChildNodeID(domainElement);
 		case 2007:
 			if (EcoreSemanticHints.EEnum_2007Labels.EENUMNAME_4009_TEXT.equals(semanticHint)) {
 				return 4009;
 			}
 			if (semanticHint != null && !"".equals(semanticHint)) {
 				return getUnrecognizedEEnum_2007ChildNodeID(semanticHint);
 			}
 			return getUnrecognizedEEnum_2007ChildNodeID(domainElement);
 		case 2008:
 			if (EcoreSemanticHints.EAnnotation_2008Labels.EANNOTATIONSOURCE_4010_TEXT.equals(semanticHint)) {
 				return 4010;
 			}
 			if (semanticHint != null && !"".equals(semanticHint)) {
 				return getUnrecognizedEAnnotation_2008ChildNodeID(semanticHint);
 			}
 			return getUnrecognizedEAnnotation_2008ChildNodeID(domainElement);
 		case 2009:
 			if (EcoreSemanticHints.EStringToStringMapEntry_2009Labels.ESTRINGTOSTRINGMAPENTRYKEY_4012_TEXT.equals(semanticHint)) {
 				return 4012;
 			}
 			if (semanticHint != null && !"".equals(semanticHint)) {
 				return getUnrecognizedEStringToStringMapEntry_2009ChildNodeID(semanticHint);
 			}
 			return getUnrecognizedEStringToStringMapEntry_2009ChildNodeID(domainElement);
 		case 2010:
 			if (EcoreSemanticHints.EAnnotation_2010Labels.EANNOTATIONSOURCE_4014_TEXT.equals(semanticHint)) {
 				return 4014;
 			}
 			if (semanticHint != null && !"".equals(semanticHint)) {
 				return getUnrecognizedEAnnotation_2010ChildNodeID(semanticHint);
 			}
 			return getUnrecognizedEAnnotation_2010ChildNodeID(domainElement);
 		case 2011:
 			if (EcoreSemanticHints.EEnumLiteral_2011Labels.EENUMLITERALNAME_4016_TEXT.equals(semanticHint)) {
 				return 4016;
 			}
 			if (semanticHint != null && !"".equals(semanticHint)) {
 				return getUnrecognizedEEnumLiteral_2011ChildNodeID(semanticHint);
 			}
 			return getUnrecognizedEEnumLiteral_2011ChildNodeID(domainElement);
 		case 2012:
 			if (EcoreSemanticHints.EAnnotation_2012Labels.EANNOTATIONSOURCE_4017_TEXT.equals(semanticHint)) {
 				return 4017;
 			}
 			if (semanticHint != null && !"".equals(semanticHint)) {
 				return getUnrecognizedEAnnotation_2012ChildNodeID(semanticHint);
 			}
 			return getUnrecognizedEAnnotation_2012ChildNodeID(domainElement);
 		case 5001:
 			if (semanticHint != null && !"".equals(semanticHint)) {
 				return getUnrecognizedAttributes_5001ChildNodeID(semanticHint);
 			}
 			if (EcorePackage.eINSTANCE.getEAttribute().equals(domainElementMetaclass) && (domainElement != null ? isNodeEAttribute_2001((EAttribute) domainElement) : true)) {
 				return 2001;
 			}
 			return getUnrecognizedAttributes_5001ChildNodeID(domainElement);
 		case 5002:
 			if (semanticHint != null && !"".equals(semanticHint)) {
 				return getUnrecognizedOperations_5002ChildNodeID(semanticHint);
 			}
 			if (EcorePackage.eINSTANCE.getEOperation().equals(domainElementMetaclass) && (domainElement != null ? isNodeEOperation_2002((EOperation) domainElement) : true)) {
 				return 2002;
 			}
 			return getUnrecognizedOperations_5002ChildNodeID(domainElement);
 		case 5003:
 			if (semanticHint != null && !"".equals(semanticHint)) {
 				return getUnrecognizedClass_annotations_5003ChildNodeID(semanticHint);
 			}
 			if (EcorePackage.eINSTANCE.getEAnnotation().equals(domainElementMetaclass) && (domainElement != null ? isNodeEAnnotation_2003((EAnnotation) domainElement) : true)) {
 				return 2003;
 			}
 			return getUnrecognizedClass_annotations_5003ChildNodeID(domainElement);
 		case 5004:
 			if (semanticHint != null && !"".equals(semanticHint)) {
 				return getUnrecognizedClasses_5004ChildNodeID(semanticHint);
 			}
 			if (EcorePackage.eINSTANCE.getEClass().equals(domainElementMetaclass) && (domainElement != null ? isNodeEClass_2004((EClass) domainElement) : true)) {
 				return 2004;
 			}
 			return getUnrecognizedClasses_5004ChildNodeID(domainElement);
 		case 5005:
 			if (semanticHint != null && !"".equals(semanticHint)) {
 				return getUnrecognizedPackages_5005ChildNodeID(semanticHint);
 			}
 			if (EcorePackage.eINSTANCE.getEPackage().equals(domainElementMetaclass) && (domainElement != null ? isNodeEPackage_2005((EPackage) domainElement) : true)) {
 				return 2005;
 			}
 			return getUnrecognizedPackages_5005ChildNodeID(domainElement);
 		case 5006:
 			if (semanticHint != null && !"".equals(semanticHint)) {
 				return getUnrecognizedData_types_5006ChildNodeID(semanticHint);
 			}
 			if (EcorePackage.eINSTANCE.getEDataType().equals(domainElementMetaclass) && (domainElement != null ? isNodeEDataType_2006((EDataType) domainElement) : true)) {
 				return 2006;
 			}
 			return getUnrecognizedData_types_5006ChildNodeID(domainElement);
 		case 5007:
 			if (semanticHint != null && !"".equals(semanticHint)) {
 				return getUnrecognizedEnums_5007ChildNodeID(semanticHint);
 			}
 			if (EcorePackage.eINSTANCE.getEEnum().equals(domainElementMetaclass) && (domainElement != null ? isNodeEEnum_2007((EEnum) domainElement) : true)) {
 				return 2007;
 			}
 			return getUnrecognizedEnums_5007ChildNodeID(domainElement);
 		case 5008:
 			if (semanticHint != null && !"".equals(semanticHint)) {
 				return getUnrecognizedPackage_annotations_5008ChildNodeID(semanticHint);
 			}
 			if (EcorePackage.eINSTANCE.getEAnnotation().equals(domainElementMetaclass) && (domainElement != null ? isNodeEAnnotation_2008((EAnnotation) domainElement) : true)) {
 				return 2008;
 			}
 			return getUnrecognizedPackage_annotations_5008ChildNodeID(domainElement);
 		case 5009:
 			if (semanticHint != null && !"".equals(semanticHint)) {
 				return getUnrecognizedDetails_5009ChildNodeID(semanticHint);
 			}
 			if (EcorePackage.eINSTANCE.getEStringToStringMapEntry().equals(domainElementMetaclass) && (domainElement != null ? isNodeEStringToStringMapEntry_2009((Entry) domainElement) : true)) {
 				return 2009;
 			}
 			return getUnrecognizedDetails_5009ChildNodeID(domainElement);
 		case 5010:
 			if (semanticHint != null && !"".equals(semanticHint)) {
 				return getUnrecognizedData_type_annotations_5010ChildNodeID(semanticHint);
 			}
 			if (EcorePackage.eINSTANCE.getEAnnotation().equals(domainElementMetaclass) && (domainElement != null ? isNodeEAnnotation_2010((EAnnotation) domainElement) : true)) {
 				return 2010;
 			}
 			return getUnrecognizedData_type_annotations_5010ChildNodeID(domainElement);
 		case 5011:
 			if (semanticHint != null && !"".equals(semanticHint)) {
 				return getUnrecognizedLiterals_5011ChildNodeID(semanticHint);
 			}
 			if (EcorePackage.eINSTANCE.getEEnumLiteral().equals(domainElementMetaclass) && (domainElement != null ? isNodeEEnumLiteral_2011((EEnumLiteral) domainElement) : true)) {
 				return 2011;
 			}
 			return getUnrecognizedLiterals_5011ChildNodeID(domainElement);
 		case 5012:
 			if (semanticHint != null && !"".equals(semanticHint)) {
 				return getUnrecognizedEnum_annotations_5012ChildNodeID(semanticHint);
 			}
 			if (EcorePackage.eINSTANCE.getEAnnotation().equals(domainElementMetaclass) && (domainElement != null ? isNodeEAnnotation_2012((EAnnotation) domainElement) : true)) {
 				return 2012;
 			}
 			return getUnrecognizedEnum_annotations_5012ChildNodeID(domainElement);
 		case 79:
 			if (semanticHint != null && !"".equals(semanticHint)) {
 				return getUnrecognizedEPackage_79ChildNodeID(semanticHint);
 			}
 			if (EcorePackage.eINSTANCE.getEClass().equals(domainElementMetaclass) && (domainElement != null ? isNodeEClass_1001((EClass) domainElement) : true)) {
 				return 1001;
 			}
 			if (EcorePackage.eINSTANCE.getEPackage().equals(domainElementMetaclass) && (domainElement != null ? isNodeEPackage_1002((EPackage) domainElement) : true)) {
 				return 1002;
 			}
 			if (EcorePackage.eINSTANCE.getEAnnotation().equals(domainElementMetaclass) && (domainElement != null ? isNodeEAnnotation_1003((EAnnotation) domainElement) : true)) {
 				return 1003;
 			}
 			if (EcorePackage.eINSTANCE.getEDataType().equals(domainElementMetaclass) && (domainElement != null ? isNodeEDataType_1004((EDataType) domainElement) : true)) {
 				return 1004;
 			}
 			if (EcorePackage.eINSTANCE.getEEnum().equals(domainElementMetaclass) && (domainElement != null ? isNodeEEnum_1005((EEnum) domainElement) : true)) {
 				return 1005;
 			}
 			return getUnrecognizedEPackage_79ChildNodeID(domainElement);
 		case 3001:
 			return getUnrecognizedEAnnotationReferences_3001LinkLabelID(semanticHint);
 		case 3002:
 			if (EcoreSemanticHints.EReference_3002Labels.EREFERENCENAME_4018_LABEL.equals(semanticHint)) {
 				return 4018;
 			}
 			return getUnrecognizedEReference_3002LinkLabelID(semanticHint);
 		case 4018:
 			if (EcoreSemanticHints.EReference_3002Labels.EREFERENCENAME_4018_TEXT.equals(semanticHint)) {
 				return 4018;
 			}
 			return getUnrecognizedEReference_3002LinkLabelTextID(semanticHint);
 		case 3003:
 			if (EcoreSemanticHints.EReference_3003Labels.EREFERENCENAME_4019_LABEL.equals(semanticHint)) {
 				return 4019;
 			}
 			return getUnrecognizedEReference_3003LinkLabelID(semanticHint);
 		case 4019:
 			if (EcoreSemanticHints.EReference_3003Labels.EREFERENCENAME_4019_TEXT.equals(semanticHint)) {
 				return 4019;
 			}
 			return getUnrecognizedEReference_3003LinkLabelTextID(semanticHint);
 		case 3004:
 			return getUnrecognizedEClassESuperTypes_3004LinkLabelID(semanticHint);
 		}
 		return -1;
 	}
 
 	/**
 	 * @generated
 	 */
 	public int getLinkWithClassVisualID(EObject domainElement) {
 		EClass domainElementMetaclass = domainElement.eClass();
 		return getLinkWithClassVisualID(domainElement, domainElementMetaclass);
 	}
 
 	/**
 	 * @generated
 	 */
 	public int getLinkWithClassVisualID(EObject domainElement, EClass domainElementMetaclass) {
 		if (EcorePackage.eINSTANCE.getEReference().equals(domainElementMetaclass) && (domainElement != null ? isLinkWithClassEReference_3002((EReference) domainElement) : true)) {
 			return 3002;
 		} else if (EcorePackage.eINSTANCE.getEReference().equals(domainElementMetaclass) && (domainElement != null ? isLinkWithClassEReference_3003((EReference) domainElement) : true)) {
 			return 3003;
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
 	private boolean isDiagramEPackage_79(EPackage element) {
 		return true;
 	}
 
 	/**
 	 * User can change implementation of this method to handle some specific
 	 * situations not covered by default logic.
 	 *
 	 * @generated
 	 */
 	private int getUnrecognizedDiagramID(EObject domainElement) {
 		return -1;
 	}
 
 	/**
 	 * User can change implementation of this method to check some additional 
 	 * conditions here.
 	 *
 	 * @generated
 	 */
 	private boolean isNodeEClass_1001(EClass element) {
 		return ElementSelectors.acceptAllMatcher().matches(element);
 	}
 
 	/**
 	 * User can change implementation of this method to check some additional 
 	 * conditions here.
 	 *
 	 * @generated
 	 */
 	private boolean isNodeEPackage_1002(EPackage element) {
 		return ElementSelectors.acceptAllMatcher().matches(element);
 	}
 
 	/**
 	 * User can change implementation of this method to check some additional 
 	 * conditions here.
 	 *
 	 * @generated
 	 */
 	private boolean isNodeEAnnotation_1003(EAnnotation element) {
 		return ElementSelectors.acceptAllMatcher().matches(element);
 	}
 
 	/**
 	 * User can change implementation of this method to check some additional 
 	 * conditions here.
 	 *
 	 * @generated
 	 */
 	private boolean isNodeEDataType_1004(EDataType element) {
 		return ElementSelectors.acceptAllMatcher().matches(element);
 	}
 
 	/**
 	 * User can change implementation of this method to check some additional 
 	 * conditions here.
 	 *
 	 * @generated
 	 */
 	private boolean isNodeEEnum_1005(EEnum element) {
 		return ElementSelectors.acceptAllMatcher().matches(element);
 	}
 
 	/**
 	 * User can change implementation of this method to check some additional 
 	 * conditions here.
 	 *
 	 * @generated
 	 */
 	private boolean isNodeEAttribute_2001(EAttribute element) {
 		return ElementSelectors.acceptAllMatcher().matches(element);
 	}
 
 	/**
 	 * User can change implementation of this method to check some additional 
 	 * conditions here.
 	 *
 	 * @generated
 	 */
 	private boolean isNodeEOperation_2002(EOperation element) {
 		return ElementSelectors.acceptAllMatcher().matches(element);
 	}
 
 	/**
 	 * User can change implementation of this method to check some additional 
 	 * conditions here.
 	 *
 	 * @generated
 	 */
 	private boolean isNodeEAnnotation_2003(EAnnotation element) {
 		return ElementSelectors.acceptAllMatcher().matches(element);
 	}
 
 	/**
 	 * User can change implementation of this method to check some additional 
 	 * conditions here.
 	 *
 	 * @generated
 	 */
 	private boolean isNodeEClass_2004(EClass element) {
 		return ElementSelectors.acceptAllMatcher().matches(element);
 	}
 
 	/**
 	 * User can change implementation of this method to check some additional 
 	 * conditions here.
 	 *
 	 * @generated
 	 */
 	private boolean isNodeEPackage_2005(EPackage element) {
 		return ElementSelectors.acceptAllMatcher().matches(element);
 	}
 
 	/**
 	 * User can change implementation of this method to check some additional 
 	 * conditions here.
 	 *
 	 * @generated
 	 */
 	private boolean isNodeEDataType_2006(EDataType element) {
 		return ElementSelectors.acceptAllMatcher().matches(element);
 	}
 
 	/**
 	 * User can change implementation of this method to check some additional 
 	 * conditions here.
 	 *
 	 * @generated
 	 */
 	private boolean isNodeEEnum_2007(EEnum element) {
 		return ElementSelectors.acceptAllMatcher().matches(element);
 	}
 
 	/**
 	 * User can change implementation of this method to check some additional 
 	 * conditions here.
 	 *
 	 * @generated
 	 */
 	private boolean isNodeEAnnotation_2008(EAnnotation element) {
 		return ElementSelectors.acceptAllMatcher().matches(element);
 	}
 
 	/**
 	 * User can change implementation of this method to check some additional 
 	 * conditions here.
 	 *
 	 * @generated
 	 */
 	private boolean isNodeEStringToStringMapEntry_2009(Entry element) {
 		return ElementSelectors.acceptAllMatcher().matches(element);
 	}
 
 	/**
 	 * User can change implementation of this method to check some additional 
 	 * conditions here.
 	 *
 	 * @generated
 	 */
 	private boolean isNodeEAnnotation_2010(EAnnotation element) {
 		return ElementSelectors.acceptAllMatcher().matches(element);
 	}
 
 	/**
 	 * User can change implementation of this method to check some additional 
 	 * conditions here.
 	 *
 	 * @generated
 	 */
 	private boolean isNodeEEnumLiteral_2011(EEnumLiteral element) {
 		return ElementSelectors.acceptAllMatcher().matches(element);
 	}
 
 	/**
 	 * User can change implementation of this method to check some additional 
 	 * conditions here.
 	 *
 	 * @generated
 	 */
 	private boolean isNodeEAnnotation_2012(EAnnotation element) {
 		return ElementSelectors.acceptAllMatcher().matches(element);
 	}
 
 	/**
 	 * User can change implementation of this method to handle some specific
 	 * situations not covered by default logic.
 	 *
 	 * @generated
 	 */
 	private int getUnrecognizedEClass_1001ChildNodeID(String semanticHint) {
 		return -1;
 	}
 
 	/**
 	 * User can change implementation of this method to handle some specific
 	 * situations not covered by default logic.
 	 *
 	 * @generated
 	 */
 	private int getUnrecognizedEClass_1001ChildNodeID(EObject domainElement) {
 		return -1;
 	}
 
 	/**
 	 * User can change implementation of this method to handle some specific
 	 * situations not covered by default logic.
 	 *
 	 * @generated
 	 */
 	private int getUnrecognizedEPackage_1002ChildNodeID(String semanticHint) {
 		return -1;
 	}
 
 	/**
 	 * User can change implementation of this method to handle some specific
 	 * situations not covered by default logic.
 	 *
 	 * @generated
 	 */
 	private int getUnrecognizedEPackage_1002ChildNodeID(EObject domainElement) {
 		return -1;
 	}
 
 	/**
 	 * User can change implementation of this method to handle some specific
 	 * situations not covered by default logic.
 	 *
 	 * @generated
 	 */
 	private int getUnrecognizedEAnnotation_1003ChildNodeID(String semanticHint) {
 		return -1;
 	}
 
 	/**
 	 * User can change implementation of this method to handle some specific
 	 * situations not covered by default logic.
 	 *
 	 * @generated
 	 */
 	private int getUnrecognizedEAnnotation_1003ChildNodeID(EObject domainElement) {
 		return -1;
 	}
 
 	/**
 	 * User can change implementation of this method to handle some specific
 	 * situations not covered by default logic.
 	 *
 	 * @generated
 	 */
 	private int getUnrecognizedEDataType_1004ChildNodeID(String semanticHint) {
 		return -1;
 	}
 
 	/**
 	 * User can change implementation of this method to handle some specific
 	 * situations not covered by default logic.
 	 *
 	 * @generated
 	 */
 	private int getUnrecognizedEDataType_1004ChildNodeID(EObject domainElement) {
 		return -1;
 	}
 
 	/**
 	 * User can change implementation of this method to handle some specific
 	 * situations not covered by default logic.
 	 *
 	 * @generated
 	 */
 	private int getUnrecognizedEEnum_1005ChildNodeID(String semanticHint) {
 		return -1;
 	}
 
 	/**
 	 * User can change implementation of this method to handle some specific
 	 * situations not covered by default logic.
 	 *
 	 * @generated
 	 */
 	private int getUnrecognizedEEnum_1005ChildNodeID(EObject domainElement) {
 		return -1;
 	}
 
 	/**
 	 * User can change implementation of this method to handle some specific
 	 * situations not covered by default logic.
 	 *
 	 * @generated
 	 */
 	private int getUnrecognizedEAttribute_2001ChildNodeID(String semanticHint) {
 		return -1;
 	}
 
 	/**
 	 * User can change implementation of this method to handle some specific
 	 * situations not covered by default logic.
 	 *
 	 * @generated
 	 */
 	private int getUnrecognizedEAttribute_2001ChildNodeID(EObject domainElement) {
 		return -1;
 	}
 
 	/**
 	 * User can change implementation of this method to handle some specific
 	 * situations not covered by default logic.
 	 *
 	 * @generated
 	 */
 	private int getUnrecognizedEOperation_2002ChildNodeID(String semanticHint) {
 		return -1;
 	}
 
 	/**
 	 * User can change implementation of this method to handle some specific
 	 * situations not covered by default logic.
 	 *
 	 * @generated
 	 */
 	private int getUnrecognizedEOperation_2002ChildNodeID(EObject domainElement) {
 		return -1;
 	}
 
 	/**
 	 * User can change implementation of this method to handle some specific
 	 * situations not covered by default logic.
 	 *
 	 * @generated
 	 */
 	private int getUnrecognizedEAnnotation_2003ChildNodeID(String semanticHint) {
 		return -1;
 	}
 
 	/**
 	 * User can change implementation of this method to handle some specific
 	 * situations not covered by default logic.
 	 *
 	 * @generated
 	 */
 	private int getUnrecognizedEAnnotation_2003ChildNodeID(EObject domainElement) {
 		return -1;
 	}
 
 	/**
 	 * User can change implementation of this method to handle some specific
 	 * situations not covered by default logic.
 	 *
 	 * @generated
 	 */
 	private int getUnrecognizedEClass_2004ChildNodeID(String semanticHint) {
 		return -1;
 	}
 
 	/**
 	 * User can change implementation of this method to handle some specific
 	 * situations not covered by default logic.
 	 *
 	 * @generated
 	 */
 	private int getUnrecognizedEClass_2004ChildNodeID(EObject domainElement) {
 		return -1;
 	}
 
 	/**
 	 * User can change implementation of this method to handle some specific
 	 * situations not covered by default logic.
 	 *
 	 * @generated
 	 */
 	private int getUnrecognizedEPackage_2005ChildNodeID(String semanticHint) {
 		return -1;
 	}
 
 	/**
 	 * User can change implementation of this method to handle some specific
 	 * situations not covered by default logic.
 	 *
 	 * @generated
 	 */
 	private int getUnrecognizedEPackage_2005ChildNodeID(EObject domainElement) {
 		return -1;
 	}
 
 	/**
 	 * User can change implementation of this method to handle some specific
 	 * situations not covered by default logic.
 	 *
 	 * @generated
 	 */
 	private int getUnrecognizedEDataType_2006ChildNodeID(String semanticHint) {
 		return -1;
 	}
 
 	/**
 	 * User can change implementation of this method to handle some specific
 	 * situations not covered by default logic.
 	 *
 	 * @generated
 	 */
 	private int getUnrecognizedEDataType_2006ChildNodeID(EObject domainElement) {
 		return -1;
 	}
 
 	/**
 	 * User can change implementation of this method to handle some specific
 	 * situations not covered by default logic.
 	 *
 	 * @generated
 	 */
 	private int getUnrecognizedEEnum_2007ChildNodeID(String semanticHint) {
 		return -1;
 	}
 
 	/**
 	 * User can change implementation of this method to handle some specific
 	 * situations not covered by default logic.
 	 *
 	 * @generated
 	 */
 	private int getUnrecognizedEEnum_2007ChildNodeID(EObject domainElement) {
 		return -1;
 	}
 
 	/**
 	 * User can change implementation of this method to handle some specific
 	 * situations not covered by default logic.
 	 *
 	 * @generated
 	 */
 	private int getUnrecognizedEAnnotation_2008ChildNodeID(String semanticHint) {
 		return -1;
 	}
 
 	/**
 	 * User can change implementation of this method to handle some specific
 	 * situations not covered by default logic.
 	 *
 	 * @generated
 	 */
 	private int getUnrecognizedEAnnotation_2008ChildNodeID(EObject domainElement) {
 		return -1;
 	}
 
 	/**
 	 * User can change implementation of this method to handle some specific
 	 * situations not covered by default logic.
 	 *
 	 * @generated
 	 */
 	private int getUnrecognizedEStringToStringMapEntry_2009ChildNodeID(String semanticHint) {
 		return -1;
 	}
 
 	/**
 	 * User can change implementation of this method to handle some specific
 	 * situations not covered by default logic.
 	 *
 	 * @generated
 	 */
 	private int getUnrecognizedEStringToStringMapEntry_2009ChildNodeID(EObject domainElement) {
 		return -1;
 	}
 
 	/**
 	 * User can change implementation of this method to handle some specific
 	 * situations not covered by default logic.
 	 *
 	 * @generated
 	 */
 	private int getUnrecognizedEAnnotation_2010ChildNodeID(String semanticHint) {
 		return -1;
 	}
 
 	/**
 	 * User can change implementation of this method to handle some specific
 	 * situations not covered by default logic.
 	 *
 	 * @generated
 	 */
 	private int getUnrecognizedEAnnotation_2010ChildNodeID(EObject domainElement) {
 		return -1;
 	}
 
 	/**
 	 * User can change implementation of this method to handle some specific
 	 * situations not covered by default logic.
 	 *
 	 * @generated
 	 */
 	private int getUnrecognizedEEnumLiteral_2011ChildNodeID(String semanticHint) {
 		return -1;
 	}
 
 	/**
 	 * User can change implementation of this method to handle some specific
 	 * situations not covered by default logic.
 	 *
 	 * @generated
 	 */
 	private int getUnrecognizedEEnumLiteral_2011ChildNodeID(EObject domainElement) {
 		return -1;
 	}
 
 	/**
 	 * User can change implementation of this method to handle some specific
 	 * situations not covered by default logic.
 	 *
 	 * @generated
 	 */
 	private int getUnrecognizedEAnnotation_2012ChildNodeID(String semanticHint) {
 		return -1;
 	}
 
 	/**
 	 * User can change implementation of this method to handle some specific
 	 * situations not covered by default logic.
 	 *
 	 * @generated
 	 */
 	private int getUnrecognizedEAnnotation_2012ChildNodeID(EObject domainElement) {
 		return -1;
 	}
 
 	/**
 	 * User can change implementation of this method to handle some specific
 	 * situations not covered by default logic.
 	 *
 	 * @generated
 	 */
 	private int getUnrecognizedAttributes_5001ChildNodeID(String semanticHint) {
 		return -1;
 	}
 
 	/**
 	 * User can change implementation of this method to handle some specific
 	 * situations not covered by default logic.
 	 *
 	 * @generated
 	 */
 	private int getUnrecognizedAttributes_5001ChildNodeID(EObject domainElement) {
 		return -1;
 	}
 
 	/**
 	 * User can change implementation of this method to handle some specific
 	 * situations not covered by default logic.
 	 *
 	 * @generated
 	 */
 	private int getUnrecognizedOperations_5002ChildNodeID(String semanticHint) {
 		return -1;
 	}
 
 	/**
 	 * User can change implementation of this method to handle some specific
 	 * situations not covered by default logic.
 	 *
 	 * @generated
 	 */
 	private int getUnrecognizedOperations_5002ChildNodeID(EObject domainElement) {
 		return -1;
 	}
 
 	/**
 	 * User can change implementation of this method to handle some specific
 	 * situations not covered by default logic.
 	 *
 	 * @generated
 	 */
 	private int getUnrecognizedClass_annotations_5003ChildNodeID(String semanticHint) {
 		return -1;
 	}
 
 	/**
 	 * User can change implementation of this method to handle some specific
 	 * situations not covered by default logic.
 	 *
 	 * @generated
 	 */
 	private int getUnrecognizedClass_annotations_5003ChildNodeID(EObject domainElement) {
 		return -1;
 	}
 
 	/**
 	 * User can change implementation of this method to handle some specific
 	 * situations not covered by default logic.
 	 *
 	 * @generated
 	 */
 	private int getUnrecognizedClasses_5004ChildNodeID(String semanticHint) {
 		return -1;
 	}
 
 	/**
 	 * User can change implementation of this method to handle some specific
 	 * situations not covered by default logic.
 	 *
 	 * @generated
 	 */
 	private int getUnrecognizedClasses_5004ChildNodeID(EObject domainElement) {
 		return -1;
 	}
 
 	/**
 	 * User can change implementation of this method to handle some specific
 	 * situations not covered by default logic.
 	 *
 	 * @generated
 	 */
 	private int getUnrecognizedPackages_5005ChildNodeID(String semanticHint) {
 		return -1;
 	}
 
 	/**
 	 * User can change implementation of this method to handle some specific
 	 * situations not covered by default logic.
 	 *
 	 * @generated
 	 */
 	private int getUnrecognizedPackages_5005ChildNodeID(EObject domainElement) {
 		return -1;
 	}
 
 	/**
 	 * User can change implementation of this method to handle some specific
 	 * situations not covered by default logic.
 	 *
 	 * @generated
 	 */
 	private int getUnrecognizedData_types_5006ChildNodeID(String semanticHint) {
 		return -1;
 	}
 
 	/**
 	 * User can change implementation of this method to handle some specific
 	 * situations not covered by default logic.
 	 *
 	 * @generated
 	 */
 	private int getUnrecognizedData_types_5006ChildNodeID(EObject domainElement) {
 		return -1;
 	}
 
 	/**
 	 * User can change implementation of this method to handle some specific
 	 * situations not covered by default logic.
 	 *
 	 * @generated
 	 */
 	private int getUnrecognizedEnums_5007ChildNodeID(String semanticHint) {
 		return -1;
 	}
 
 	/**
 	 * User can change implementation of this method to handle some specific
 	 * situations not covered by default logic.
 	 *
 	 * @generated
 	 */
 	private int getUnrecognizedEnums_5007ChildNodeID(EObject domainElement) {
 		return -1;
 	}
 
 	/**
 	 * User can change implementation of this method to handle some specific
 	 * situations not covered by default logic.
 	 *
 	 * @generated
 	 */
 	private int getUnrecognizedPackage_annotations_5008ChildNodeID(String semanticHint) {
 		return -1;
 	}
 
 	/**
 	 * User can change implementation of this method to handle some specific
 	 * situations not covered by default logic.
 	 *
 	 * @generated
 	 */
 	private int getUnrecognizedPackage_annotations_5008ChildNodeID(EObject domainElement) {
 		return -1;
 	}
 
 	/**
 	 * User can change implementation of this method to handle some specific
 	 * situations not covered by default logic.
 	 *
 	 * @generated
 	 */
 	private int getUnrecognizedDetails_5009ChildNodeID(String semanticHint) {
 		return -1;
 	}
 
 	/**
 	 * User can change implementation of this method to handle some specific
 	 * situations not covered by default logic.
 	 *
 	 * @generated
 	 */
 	private int getUnrecognizedDetails_5009ChildNodeID(EObject domainElement) {
 		return -1;
 	}
 
 	/**
 	 * User can change implementation of this method to handle some specific
 	 * situations not covered by default logic.
 	 *
 	 * @generated
 	 */
 	private int getUnrecognizedData_type_annotations_5010ChildNodeID(String semanticHint) {
 		return -1;
 	}
 
 	/**
 	 * User can change implementation of this method to handle some specific
 	 * situations not covered by default logic.
 	 *
 	 * @generated
 	 */
 	private int getUnrecognizedData_type_annotations_5010ChildNodeID(EObject domainElement) {
 		return -1;
 	}
 
 	/**
 	 * User can change implementation of this method to handle some specific
 	 * situations not covered by default logic.
 	 *
 	 * @generated
 	 */
 	private int getUnrecognizedLiterals_5011ChildNodeID(String semanticHint) {
 		return -1;
 	}
 
 	/**
 	 * User can change implementation of this method to handle some specific
 	 * situations not covered by default logic.
 	 *
 	 * @generated
 	 */
 	private int getUnrecognizedLiterals_5011ChildNodeID(EObject domainElement) {
 		return -1;
 	}
 
 	/**
 	 * User can change implementation of this method to handle some specific
 	 * situations not covered by default logic.
 	 *
 	 * @generated
 	 */
 	private int getUnrecognizedEnum_annotations_5012ChildNodeID(String semanticHint) {
 		return -1;
 	}
 
 	/**
 	 * User can change implementation of this method to handle some specific
 	 * situations not covered by default logic.
 	 *
 	 * @generated
 	 */
 	private int getUnrecognizedEnum_annotations_5012ChildNodeID(EObject domainElement) {
 		return -1;
 	}
 
 	/**
 	 * User can change implementation of this method to handle some specific
 	 * situations not covered by default logic.
 	 *
 	 * @generated
 	 */
 	private int getUnrecognizedEPackage_79ChildNodeID(String semanticHint) {
 		return -1;
 	}
 
 	/**
 	 * User can change implementation of this method to handle some specific
 	 * situations not covered by default logic.
 	 *
 	 * @generated
 	 */
 	private int getUnrecognizedEPackage_79ChildNodeID(EObject domainElement) {
 		return -1;
 	}
 
 	/**
 	 * User can change implementation of this method to handle some specific
 	 * situations not covered by default logic.
 	 *
 	 * @generated
 	 */
 	private int getUnrecognizedEAnnotationReferences_3001LinkLabelID(String semanticHint) {
 		return -1;
 	}
 
 	/**
 	 * User can change implementation of this method to handle some specific
 	 * situations not covered by default logic.
 	 *
 	 * @generated
 	 */
 	private int getUnrecognizedEReference_3002LinkLabelID(String semanticHint) {
 		return -1;
 	}
 
 	/**
 	 * User can change implementation of this method to handle some specific
 	 * situations not covered by default logic.
 	 *
 	 * @generated
 	 */
 	private int getUnrecognizedEReference_3002LinkLabelTextID(String semanticHint) {
 		return -1;
 	}
 
 	/**
 	 * User can change implementation of this method to handle some specific
 	 * situations not covered by default logic.
 	 *
 	 * @generated
 	 */
 	private int getUnrecognizedEReference_3003LinkLabelID(String semanticHint) {
 		return -1;
 	}
 
 	/**
 	 * User can change implementation of this method to handle some specific
 	 * situations not covered by default logic.
 	 *
 	 * @generated
 	 */
 	private int getUnrecognizedEReference_3003LinkLabelTextID(String semanticHint) {
 		return -1;
 	}
 
 	/**
 	 * User can change implementation of this method to handle some specific
 	 * situations not covered by default logic.
 	 *
 	 * @generated
 	 */
 	private int getUnrecognizedEClassESuperTypes_3004LinkLabelID(String semanticHint) {
 		return -1;
 	}
 
 	/**
 	 * User can change implementation of this method to handle some specific
 	 * situations not covered by default logic.
 	 *
 	 * @generated
 	 */
 	private int getUnrecognizedLinkWithClassID(EObject domainElement) {
 		return -1;
 	}
 
 	/**
 	 * User can change implementation of this method to check some additional 
 	 * conditions here.
 	 *
 	 * @generated
 	 */
 	private boolean isLinkWithClassEReference_3002(EReference element) {
 		return ElementSelectors.EReference_3002.matches(element);
 	}
 
 	/**
 	 * User can change implementation of this method to check some additional 
 	 * conditions here.
 	 *
 	 * @generated
 	 */
 	private boolean isLinkWithClassEReference_3003(EReference element) {
 		return ElementSelectors.EReference_3003.matches(element);
 	}
 
 	/**
 	 * @generated
 	 */
 	private String getModelID(View containerView) {
 		EAnnotation annotation = containerView.getEAnnotation("ViewIdentifier"); //$NON-NLS-1$
 		if (annotation == null) {
 			return null;
 		}
 		return (String) annotation.getDetails().get("modelID"); //$NON-NLS-1$
 	}
 
 	/**
 	 * @generated
 	 */
 	private int getVisualID(View containerView) {
 		EAnnotation annotation = containerView.getEAnnotation("ViewIdentifier"); //$NON-NLS-1$
 		if (annotation == null) {
 			return -1;
 		}
 		String visualID = (String) annotation.getDetails().get("visualID"); //$NON-NLS-1$
 		if (visualID == null) {
 			return -1;
 		}
 		try {
 			return Integer.parseInt(visualID);
 		} catch (NumberFormatException e) {
 			EcoreDiagramEditorPlugin.getInstance().logError("Unable to parse \"visualID\" annotation: " + visualID, e);
 		}
 		return -1;
 	}
 
 	/**
 	 * @generated
 	 */
 	private static class ElementSelectors {
 
 		/**
 		 * Element matching condition for EReference_3002.
 		 * <pre>language: ocl</pre>	
 		 * <pre>body    : not containment</pre>
 		 * @generated
 		 */
 		private static final Matcher EReference_3002 = new Matcher("not containment");
 
 		/**
 		 * Element matching condition for EReference_3003.
 		 * <pre>language: ocl</pre>	
 		 * <pre>body    : containment</pre>
 		 * @generated
 		 */
 		private static final Matcher EReference_3003 = new Matcher("containment");
 
 		/**
 		 * @generated
 		 */
 		private ElementSelectors() {
 		}
 
 		/**
 		 * @generated	
 		 */
 		static class Matcher {
 
 			/**
 			 * @generated	
 			 */
 			private EClass evalContext;
 
 			/**
 			 * @generated	
 			 */
			private OclConstraintCondition condition;
 
 			/**
 			 * @generated	
 			 */
 			private String body;
 
 			/**
 			 * @generated	
 			 */
 			Matcher(String expressionBody) {
 				body = expressionBody;
 			}
 
 			/**
 			 * @generated	
 			 */
 			boolean matches(EObject object) {
 				try {
 					Boolean result = (object != null) ? evaluate(object) : Boolean.FALSE;
 					return result.booleanValue();
 				} catch (IllegalArgumentException e) {
 					EcoreDiagramEditorPlugin.getInstance().logError(null, e);
 					return false;
 				}
 			}
 
 			/**
 			 * @generated	
 			 */
 			private Boolean evaluate(EObject context) {
 				this.evalContext = context.eClass();
 				if (condition == null) {
					condition = new OclConstraintCondition(body, evalContext);
 				}
 				if (condition != null) {
 					return booleanCast(condition.evaluate(context));
 				}
 				return Boolean.FALSE;
 			}
 
 			/**
 			 * @generated	
 			 */
 			private static Boolean booleanCast(Object value) {
 				if (value == null) {
 					return null;
 				} else if (value instanceof Boolean) {
 					return (Boolean) value;
 				}
 				return Boolean.FALSE;
 			}
 		} // end of Matcher		
 
 		/**
 		 * @generated
 		 */
 		static class AcceptAllMatcher {
 
 			/**
 			 * @generated
 			 */
 			static final AcceptAllMatcher INSTANCE = new AcceptAllMatcher();
 
 			/**
 			 * @generated
 			 */
 			boolean matches(Object element) {
 				return true;
 			}
 		}
 
 		/**
 		 * @generated
 		 */
 		static AcceptAllMatcher acceptAllMatcher() {
 			return AcceptAllMatcher.INSTANCE;
 		}
 	} // end of ElementSelectors
 }
