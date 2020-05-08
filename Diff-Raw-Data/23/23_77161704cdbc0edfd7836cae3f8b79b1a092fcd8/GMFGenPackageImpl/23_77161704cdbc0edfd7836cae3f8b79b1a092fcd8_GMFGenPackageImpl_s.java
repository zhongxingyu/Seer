 /**
  * <copyright>
  * </copyright>
  *
  * $Id$
  */
 package org.eclipse.gmf.codegen.gmfgen.impl;
 
 import org.eclipse.emf.codegen.ecore.genmodel.GenModelPackage;
 import org.eclipse.emf.ecore.EAttribute;
 import org.eclipse.emf.ecore.EClass;
 import org.eclipse.emf.ecore.EEnum;
 import org.eclipse.emf.ecore.EOperation;
 import org.eclipse.emf.ecore.EPackage;
 import org.eclipse.emf.ecore.EReference;
 import org.eclipse.emf.ecore.EValidator;
 import org.eclipse.emf.ecore.EcorePackage;
 import org.eclipse.emf.ecore.impl.EPackageImpl;
 import org.eclipse.gmf.codegen.gmfgen.*;
 import org.eclipse.gmf.validate.GMFValidator;
 
 /**
  * <!-- begin-user-doc -->
  * An implementation of the model <b>Package</b>.
  * <!-- end-user-doc -->
  * @generated
  */
 public class GMFGenPackageImpl extends EPackageImpl implements GMFGenPackage {
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EClass genEditorGeneratorEClass = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EClass genDiagramEClass = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EClass genEditorViewEClass = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EClass genPreferencePageEClass = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EClass genCustomPreferencePageEClass = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EClass genStandardPreferencePageEClass = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EClass genDiagramPreferencesEClass = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EClass genFontEClass = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EClass genStandardFontEClass = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EClass genCustomFontEClass = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EClass genColorEClass = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EClass genRGBColorEClass = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EClass genConstantColorEClass = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EClass batchValidationEClass = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EClass providerClassNamesEClass = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EClass shortcutsEClass = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EClass packageNamesEClass = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EClass linkConstraintsEClass = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EClass editorCandiesEClass = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EClass editPartCandiesEClass = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EClass measurementUnitEClass = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EClass genDiagramUpdaterEClass = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EClass genPluginEClass = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EClass elementTypeEClass = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EClass metamodelTypeEClass = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EClass specializationTypeEClass = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EClass notationTypeEClass = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EClass genCommonBaseEClass = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EClass behaviourEClass = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EClass customBehaviourEClass = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EClass sharedBehaviourEClass = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EClass openDiagramBehaviourEClass = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EClass genContainerBaseEClass = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EClass genNodeEClass = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EClass genTopLevelNodeEClass = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EClass genChildNodeEClass = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EClass genChildSideAffixedNodeEClass = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EClass genChildLabelNodeEClass = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EClass genCompartmentEClass = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EClass genChildContainerEClass = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EClass genLinkEClass = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EClass genLabelEClass = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EClass genNodeLabelEClass = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EClass genExternalNodeLabelEClass = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EClass genLinkLabelEClass = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EClass modelFacetEClass = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EClass linkModelFacetEClass = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EClass labelModelFacetEClass = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EClass typeModelFacetEClass = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EClass featureLabelModelFacetEClass = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EClass designLabelModelFacetEClass = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EClass typeLinkModelFacetEClass = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EClass featureLinkModelFacetEClass = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EClass viewmapEClass = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EClass attributesEClass = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EClass colorAttributesEClass = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EClass styleAttributesEClass = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EClass resizeConstraintsEClass = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EClass defaultSizeAttributesEClass = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EClass labelOffsetAttributesEClass = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EClass figureViewmapEClass = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EClass snippetViewmapEClass = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EClass innerClassViewmapEClass = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EClass parentAssignedViewmapEClass = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EClass valueExpressionEClass = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EClass genConstraintEClass = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EClass paletteEClass = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EClass entryBaseEClass = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EClass abstractToolEntryEClass = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EClass toolEntryEClass = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EClass standardEntryEClass = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EClass toolGroupItemEClass = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EClass separatorEClass = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EClass toolGroupEClass = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EClass genElementInitializerEClass = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EClass genFeatureSeqInitializerEClass = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EClass genFeatureValueSpecEClass = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EClass genReferenceNewElementSpecEClass = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EClass genFeatureInitializerEClass = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EClass genLinkConstraintsEClass = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EClass genAuditRootEClass = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EClass genAuditContainerEClass = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EClass genRuleBaseEClass = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EClass genAuditRuleEClass = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EClass genRuleTargetEClass = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EClass genDomainElementTargetEClass = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EClass genDiagramElementTargetEClass = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EClass genDomainAttributeTargetEClass = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EClass genNotationElementTargetEClass = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EClass genMetricContainerEClass = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EClass genMetricRuleEClass = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EClass genAuditedMetricTargetEClass = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EClass genAuditableEClass = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EClass genMeasurableEClass = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EClass genExpressionProviderContainerEClass = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EClass genExpressionProviderBaseEClass = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EClass genJavaExpressionProviderEClass = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EClass genExpressionInterpreterEClass = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EClass genDomainModelNavigatorEClass = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EClass genNavigatorEClass = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EClass genNavigatorChildReferenceEClass = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EClass genNavigatorPathEClass = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EClass genNavigatorPathSegmentEClass = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EClass genPropertySheetEClass = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EClass genPropertyTabEClass = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EClass genStandardPropertyTabEClass = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EClass genCustomPropertyTabEClass = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EClass genPropertyTabFilterEClass = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EClass typeTabFilterEClass = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EClass customTabFilterEClass = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EClass genContributionItemEClass = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EClass genSharedContributionItemEClass = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EClass genGroupMarkerEClass = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EClass genSeparatorEClass = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EClass genActionFactoryContributionItemEClass = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EClass genContributionManagerEClass = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EClass genMenuManagerEClass = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EClass genToolBarManagerEClass = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EClass genApplicationEClass = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EEnum standardPreferencePagesEEnum = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EEnum rulerUnitsEEnum = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EEnum routingEEnum = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EEnum jFaceFontEEnum = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EEnum fontStyleEEnum = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EEnum diagramColorsEEnum = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EEnum providerPriorityEEnum = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EEnum linkLabelAlignmentEEnum = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EEnum labelTextAccessMethodEEnum = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EEnum viewmapLayoutTypeEEnum = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EEnum standardEntryKindEEnum = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EEnum genSeverityEEnum = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EEnum genLanguageEEnum = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EEnum genNavigatorReferenceTypeEEnum = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EEnum generatedTypeEEnum = null;
 
 	/**
 	 * Creates an instance of the model <b>Package</b>, registered with
 	 * {@link org.eclipse.emf.ecore.EPackage.Registry EPackage.Registry} by the package
 	 * package URI value.
 	 * <p>Note: the correct way to create the package is via the static
 	 * factory method {@link #init init()}, which also performs
 	 * initialization of the package, or returns the registered package,
 	 * if one already exists.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see org.eclipse.emf.ecore.EPackage.Registry
 	 * @see org.eclipse.gmf.codegen.gmfgen.GMFGenPackage#eNS_URI
 	 * @see #init()
 	 * @generated
 	 */
 	private GMFGenPackageImpl() {
 		super(eNS_URI, GMFGenFactory.eINSTANCE);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private static boolean isInited = false;
 
 	/**
 	 * Creates, registers, and initializes the <b>Package</b> for this
 	 * model, and for any others upon which it depends.  Simple
 	 * dependencies are satisfied by calling this method on all
 	 * dependent packages before doing anything else.  This method drives
 	 * initialization for interdependent packages directly, in parallel
 	 * with this package, itself.
 	 * <p>Of this package and its interdependencies, all packages which
 	 * have not yet been registered by their URI values are first created
 	 * and registered.  The packages are then initialized in two steps:
 	 * meta-model objects for all of the packages are created before any
 	 * are initialized, since one package's meta-model objects may refer to
 	 * those of another.
 	 * <p>Invocation of this method will not affect any packages that have
 	 * already been initialized.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #eNS_URI
 	 * @see #createPackageContents()
 	 * @see #initializePackageContents()
 	 * @generated
 	 */
 	public static GMFGenPackage init() {
 		if (isInited) return (GMFGenPackage)EPackage.Registry.INSTANCE.getEPackage(GMFGenPackage.eNS_URI);
 
 		// Obtain or create and register package
 		GMFGenPackageImpl theGMFGenPackage = (GMFGenPackageImpl)(EPackage.Registry.INSTANCE.getEPackage(eNS_URI) instanceof GMFGenPackageImpl ? EPackage.Registry.INSTANCE.getEPackage(eNS_URI) : new GMFGenPackageImpl());
 
 		isInited = true;
 
 		// Initialize simple dependencies
 		EcorePackage.eINSTANCE.eClass();
 		GenModelPackage.eINSTANCE.eClass();
 
 		// Create package meta-data objects
 		theGMFGenPackage.createPackageContents();
 
 		// Initialize created meta-data
 		theGMFGenPackage.initializePackageContents();
 
 		// Mark meta-data to indicate it can't be changed
 		theGMFGenPackage.freeze();
 
 		return theGMFGenPackage;
 	}
 
 	public void initializePackageContents() {
 		initializePackageContentsGen();
 		EValidator.Registry.INSTANCE.put(this, GMFValidator.INSTANCE);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EClass getGenEditorGenerator() {
 		return genEditorGeneratorEClass;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getGenEditorGenerator_Audits() {
 		return (EReference)genEditorGeneratorEClass.getEStructuralFeatures().get(0);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getGenEditorGenerator_Metrics() {
 		return (EReference)genEditorGeneratorEClass.getEStructuralFeatures().get(1);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getGenEditorGenerator_Diagram() {
 		return (EReference)genEditorGeneratorEClass.getEStructuralFeatures().get(2);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getGenEditorGenerator_Plugin() {
 		return (EReference)genEditorGeneratorEClass.getEStructuralFeatures().get(3);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getGenEditorGenerator_Editor() {
 		return (EReference)genEditorGeneratorEClass.getEStructuralFeatures().get(4);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getGenEditorGenerator_Navigator() {
 		return (EReference)genEditorGeneratorEClass.getEStructuralFeatures().get(5);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getGenEditorGenerator_DiagramUpdater() {
 		return (EReference)genEditorGeneratorEClass.getEStructuralFeatures().get(6);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getGenEditorGenerator_PropertySheet() {
 		return (EReference)genEditorGeneratorEClass.getEStructuralFeatures().get(7);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getGenEditorGenerator_Application() {
 		return (EReference)genEditorGeneratorEClass.getEStructuralFeatures().get(8);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getGenEditorGenerator_DomainGenModel() {
 		return (EReference)genEditorGeneratorEClass.getEStructuralFeatures().get(9);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getGenEditorGenerator_PackageNamePrefix() {
 		return (EAttribute)genEditorGeneratorEClass.getEStructuralFeatures().get(10);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getGenEditorGenerator_ModelID() {
 		return (EAttribute)genEditorGeneratorEClass.getEStructuralFeatures().get(11);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getGenEditorGenerator_SameFileForDiagramAndModel() {
 		return (EAttribute)genEditorGeneratorEClass.getEStructuralFeatures().get(12);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getGenEditorGenerator_DiagramFileExtension() {
 		return (EAttribute)genEditorGeneratorEClass.getEStructuralFeatures().get(13);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getGenEditorGenerator_DomainFileExtension() {
 		return (EAttribute)genEditorGeneratorEClass.getEStructuralFeatures().get(14);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getGenEditorGenerator_DynamicTemplates() {
 		return (EAttribute)genEditorGeneratorEClass.getEStructuralFeatures().get(15);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getGenEditorGenerator_TemplateDirectory() {
 		return (EAttribute)genEditorGeneratorEClass.getEStructuralFeatures().get(16);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getGenEditorGenerator_CopyrightText() {
 		return (EAttribute)genEditorGeneratorEClass.getEStructuralFeatures().get(17);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getGenEditorGenerator_ExpressionProviders() {
 		return (EReference)genEditorGeneratorEClass.getEStructuralFeatures().get(18);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EClass getGenDiagram() {
 		return genDiagramEClass;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getGenDiagram_EditorGen() {
 		return (EReference)genDiagramEClass.getEStructuralFeatures().get(0);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getGenDiagram_DomainDiagramElement() {
 		return (EReference)genDiagramEClass.getEStructuralFeatures().get(1);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getGenDiagram_ChildNodes() {
 		return (EReference)genDiagramEClass.getEStructuralFeatures().get(2);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getGenDiagram_TopLevelNodes() {
 		return (EReference)genDiagramEClass.getEStructuralFeatures().get(3);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getGenDiagram_Links() {
 		return (EReference)genDiagramEClass.getEStructuralFeatures().get(4);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getGenDiagram_Compartments() {
 		return (EReference)genDiagramEClass.getEStructuralFeatures().get(5);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getGenDiagram_Palette() {
 		return (EReference)genDiagramEClass.getEStructuralFeatures().get(6);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getGenDiagram_Synchronized() {
 		return (EAttribute)genDiagramEClass.getEStructuralFeatures().get(7);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getGenDiagram_Preferences() {
 		return (EReference)genDiagramEClass.getEStructuralFeatures().get(8);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getGenDiagram_PreferencePages() {
 		return (EReference)genDiagramEClass.getEStructuralFeatures().get(9);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EClass getGenEditorView() {
 		return genEditorViewEClass;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getGenEditorView_EditorGen() {
 		return (EReference)genEditorViewEClass.getEStructuralFeatures().get(0);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getGenEditorView_PackageName() {
 		return (EAttribute)genEditorViewEClass.getEStructuralFeatures().get(1);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getGenEditorView_ActionBarContributorClassName() {
 		return (EAttribute)genEditorViewEClass.getEStructuralFeatures().get(2);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getGenEditorView_ClassName() {
 		return (EAttribute)genEditorViewEClass.getEStructuralFeatures().get(3);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getGenEditorView_IconPath() {
 		return (EAttribute)genEditorViewEClass.getEStructuralFeatures().get(4);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getGenEditorView_IconPathX() {
 		return (EAttribute)genEditorViewEClass.getEStructuralFeatures().get(5);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getGenEditorView_ID() {
 		return (EAttribute)genEditorViewEClass.getEStructuralFeatures().get(6);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getGenEditorView_EclipseEditor() {
 		return (EAttribute)genEditorViewEClass.getEStructuralFeatures().get(7);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getGenEditorView_ContextID() {
 		return (EAttribute)genEditorViewEClass.getEStructuralFeatures().get(8);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EClass getGenPreferencePage() {
 		return genPreferencePageEClass;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getGenPreferencePage_ID() {
 		return (EAttribute)genPreferencePageEClass.getEStructuralFeatures().get(0);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getGenPreferencePage_Name() {
 		return (EAttribute)genPreferencePageEClass.getEStructuralFeatures().get(1);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getGenPreferencePage_Children() {
 		return (EReference)genPreferencePageEClass.getEStructuralFeatures().get(2);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getGenPreferencePage_Parent() {
 		return (EReference)genPreferencePageEClass.getEStructuralFeatures().get(3);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EClass getGenCustomPreferencePage() {
 		return genCustomPreferencePageEClass;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getGenCustomPreferencePage_QualifiedClassName() {
 		return (EAttribute)genCustomPreferencePageEClass.getEStructuralFeatures().get(0);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EClass getGenStandardPreferencePage() {
 		return genStandardPreferencePageEClass;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getGenStandardPreferencePage_Kind() {
 		return (EAttribute)genStandardPreferencePageEClass.getEStructuralFeatures().get(0);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EClass getGenDiagramPreferences() {
 		return genDiagramPreferencesEClass;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getGenDiagramPreferences_LineStyle() {
 		return (EAttribute)genDiagramPreferencesEClass.getEStructuralFeatures().get(0);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getGenDiagramPreferences_DefaultFont() {
 		return (EReference)genDiagramPreferencesEClass.getEStructuralFeatures().get(1);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getGenDiagramPreferences_FontColor() {
 		return (EReference)genDiagramPreferencesEClass.getEStructuralFeatures().get(2);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getGenDiagramPreferences_FillColor() {
 		return (EReference)genDiagramPreferencesEClass.getEStructuralFeatures().get(3);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getGenDiagramPreferences_LineColor() {
 		return (EReference)genDiagramPreferencesEClass.getEStructuralFeatures().get(4);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getGenDiagramPreferences_NoteFillColor() {
 		return (EReference)genDiagramPreferencesEClass.getEStructuralFeatures().get(5);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getGenDiagramPreferences_NoteLineColor() {
 		return (EReference)genDiagramPreferencesEClass.getEStructuralFeatures().get(6);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getGenDiagramPreferences_ShowConnectionHandles() {
 		return (EAttribute)genDiagramPreferencesEClass.getEStructuralFeatures().get(7);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getGenDiagramPreferences_ShowPopupBars() {
 		return (EAttribute)genDiagramPreferencesEClass.getEStructuralFeatures().get(8);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getGenDiagramPreferences_PromptOnDelFromModel() {
 		return (EAttribute)genDiagramPreferencesEClass.getEStructuralFeatures().get(9);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getGenDiagramPreferences_PromptOnDelFromDiagram() {
 		return (EAttribute)genDiagramPreferencesEClass.getEStructuralFeatures().get(10);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getGenDiagramPreferences_EnableAnimatedLayout() {
 		return (EAttribute)genDiagramPreferencesEClass.getEStructuralFeatures().get(11);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getGenDiagramPreferences_EnableAnimatedZoom() {
 		return (EAttribute)genDiagramPreferencesEClass.getEStructuralFeatures().get(12);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getGenDiagramPreferences_EnableAntiAlias() {
 		return (EAttribute)genDiagramPreferencesEClass.getEStructuralFeatures().get(13);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getGenDiagramPreferences_ShowGrid() {
 		return (EAttribute)genDiagramPreferencesEClass.getEStructuralFeatures().get(14);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getGenDiagramPreferences_ShowRulers() {
 		return (EAttribute)genDiagramPreferencesEClass.getEStructuralFeatures().get(15);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getGenDiagramPreferences_SnapToGrid() {
 		return (EAttribute)genDiagramPreferencesEClass.getEStructuralFeatures().get(16);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getGenDiagramPreferences_RulerUnits() {
 		return (EAttribute)genDiagramPreferencesEClass.getEStructuralFeatures().get(17);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getGenDiagramPreferences_GridSpacing() {
 		return (EAttribute)genDiagramPreferencesEClass.getEStructuralFeatures().get(18);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EClass getGenFont() {
 		return genFontEClass;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EClass getGenStandardFont() {
 		return genStandardFontEClass;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getGenStandardFont_Name() {
 		return (EAttribute)genStandardFontEClass.getEStructuralFeatures().get(0);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EClass getGenCustomFont() {
 		return genCustomFontEClass;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getGenCustomFont_Name() {
 		return (EAttribute)genCustomFontEClass.getEStructuralFeatures().get(0);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getGenCustomFont_Height() {
 		return (EAttribute)genCustomFontEClass.getEStructuralFeatures().get(1);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getGenCustomFont_Style() {
 		return (EAttribute)genCustomFontEClass.getEStructuralFeatures().get(2);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EClass getGenColor() {
 		return genColorEClass;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EClass getGenRGBColor() {
 		return genRGBColorEClass;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getGenRGBColor_Red() {
 		return (EAttribute)genRGBColorEClass.getEStructuralFeatures().get(0);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getGenRGBColor_Green() {
 		return (EAttribute)genRGBColorEClass.getEStructuralFeatures().get(1);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getGenRGBColor_Blue() {
 		return (EAttribute)genRGBColorEClass.getEStructuralFeatures().get(2);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EClass getGenConstantColor() {
 		return genConstantColorEClass;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getGenConstantColor_Name() {
 		return (EAttribute)genConstantColorEClass.getEStructuralFeatures().get(0);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EClass getBatchValidation() {
 		return batchValidationEClass;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getBatchValidation_ValidationProviderClassName() {
 		return (EAttribute)batchValidationEClass.getEStructuralFeatures().get(0);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getBatchValidation_ValidationProviderPriority() {
 		return (EAttribute)batchValidationEClass.getEStructuralFeatures().get(1);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getBatchValidation_MarkerNavigationProviderClassName() {
 		return (EAttribute)batchValidationEClass.getEStructuralFeatures().get(2);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getBatchValidation_MarkerNavigationProviderPriority() {
 		return (EAttribute)batchValidationEClass.getEStructuralFeatures().get(3);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getBatchValidation_ValidationEnabled() {
 		return (EAttribute)batchValidationEClass.getEStructuralFeatures().get(4);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getBatchValidation_MetricProviderClassName() {
 		return (EAttribute)batchValidationEClass.getEStructuralFeatures().get(5);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getBatchValidation_MetricProviderPriority() {
 		return (EAttribute)batchValidationEClass.getEStructuralFeatures().get(6);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getBatchValidation_ValidationDecoratorProviderClassName() {
 		return (EAttribute)batchValidationEClass.getEStructuralFeatures().get(7);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getBatchValidation_ValidationDecorators() {
 		return (EAttribute)batchValidationEClass.getEStructuralFeatures().get(8);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getBatchValidation_ValidationDecoratorProviderPriority() {
 		return (EAttribute)batchValidationEClass.getEStructuralFeatures().get(9);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getBatchValidation_LiveValidationUIFeedback() {
 		return (EAttribute)batchValidationEClass.getEStructuralFeatures().get(10);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EClass getProviderClassNames() {
 		return providerClassNamesEClass;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getProviderClassNames_ElementTypesClassName() {
 		return (EAttribute)providerClassNamesEClass.getEStructuralFeatures().get(0);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getProviderClassNames_NotationViewProviderClassName() {
 		return (EAttribute)providerClassNamesEClass.getEStructuralFeatures().get(1);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getProviderClassNames_NotationViewProviderPriority() {
 		return (EAttribute)providerClassNamesEClass.getEStructuralFeatures().get(2);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getProviderClassNames_EditPartProviderClassName() {
 		return (EAttribute)providerClassNamesEClass.getEStructuralFeatures().get(3);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getProviderClassNames_EditPartProviderPriority() {
 		return (EAttribute)providerClassNamesEClass.getEStructuralFeatures().get(4);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getProviderClassNames_ModelingAssistantProviderClassName() {
 		return (EAttribute)providerClassNamesEClass.getEStructuralFeatures().get(5);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getProviderClassNames_ModelingAssistantProviderPriority() {
 		return (EAttribute)providerClassNamesEClass.getEStructuralFeatures().get(6);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getProviderClassNames_IconProviderClassName() {
 		return (EAttribute)providerClassNamesEClass.getEStructuralFeatures().get(7);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getProviderClassNames_IconProviderPriority() {
 		return (EAttribute)providerClassNamesEClass.getEStructuralFeatures().get(8);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getProviderClassNames_ParserProviderClassName() {
 		return (EAttribute)providerClassNamesEClass.getEStructuralFeatures().get(9);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getProviderClassNames_ParserProviderPriority() {
 		return (EAttribute)providerClassNamesEClass.getEStructuralFeatures().get(10);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getProviderClassNames_ContributionItemProviderClassName() {
 		return (EAttribute)providerClassNamesEClass.getEStructuralFeatures().get(11);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EClass getShortcuts() {
 		return shortcutsEClass;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getShortcuts_ShortcutsDecoratorProviderClassName() {
 		return (EAttribute)shortcutsEClass.getEStructuralFeatures().get(0);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getShortcuts_ShortcutsDecoratorProviderPriority() {
 		return (EAttribute)shortcutsEClass.getEStructuralFeatures().get(1);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getShortcuts_CreateShortcutActionClassName() {
 		return (EAttribute)shortcutsEClass.getEStructuralFeatures().get(2);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getShortcuts_CreateShortcutDecorationsCommandClassName() {
 		return (EAttribute)shortcutsEClass.getEStructuralFeatures().get(3);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getShortcuts_ShortcutPropertyTesterClassName() {
 		return (EAttribute)shortcutsEClass.getEStructuralFeatures().get(4);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getShortcuts_ContainsShortcutsTo() {
 		return (EAttribute)shortcutsEClass.getEStructuralFeatures().get(5);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getShortcuts_ShortcutsProvidedFor() {
 		return (EAttribute)shortcutsEClass.getEStructuralFeatures().get(6);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EClass getPackageNames() {
 		return packageNamesEClass;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getPackageNames_EditCommandsPackageName() {
 		return (EAttribute)packageNamesEClass.getEStructuralFeatures().get(0);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getPackageNames_EditHelpersPackageName() {
 		return (EAttribute)packageNamesEClass.getEStructuralFeatures().get(1);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getPackageNames_EditPartsPackageName() {
 		return (EAttribute)packageNamesEClass.getEStructuralFeatures().get(2);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getPackageNames_EditPoliciesPackageName() {
 		return (EAttribute)packageNamesEClass.getEStructuralFeatures().get(3);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getPackageNames_PreferencesPackageName() {
 		return (EAttribute)packageNamesEClass.getEStructuralFeatures().get(4);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getPackageNames_ProvidersPackageName() {
 		return (EAttribute)packageNamesEClass.getEStructuralFeatures().get(5);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getPackageNames_ParsersPackageName() {
 		return (EAttribute)packageNamesEClass.getEStructuralFeatures().get(6);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getPackageNames_NotationViewFactoriesPackageName() {
 		return (EAttribute)packageNamesEClass.getEStructuralFeatures().get(7);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EClass getLinkConstraints() {
 		return linkConstraintsEClass;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EClass getEditorCandies() {
 		return editorCandiesEClass;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getEditorCandies_CreationWizardClassName() {
 		return (EAttribute)editorCandiesEClass.getEStructuralFeatures().get(0);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getEditorCandies_CreationWizardPageClassName() {
 		return (EAttribute)editorCandiesEClass.getEStructuralFeatures().get(1);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getEditorCandies_CreationWizardIconPath() {
 		return (EAttribute)editorCandiesEClass.getEStructuralFeatures().get(2);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getEditorCandies_CreationWizardIconPathX() {
 		return (EAttribute)editorCandiesEClass.getEStructuralFeatures().get(3);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getEditorCandies_CreationWizardCategoryID() {
 		return (EAttribute)editorCandiesEClass.getEStructuralFeatures().get(4);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getEditorCandies_DiagramEditorUtilClassName() {
 		return (EAttribute)editorCandiesEClass.getEStructuralFeatures().get(5);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getEditorCandies_DocumentProviderClassName() {
 		return (EAttribute)editorCandiesEClass.getEStructuralFeatures().get(6);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getEditorCandies_InitDiagramFileActionClassName() {
 		return (EAttribute)editorCandiesEClass.getEStructuralFeatures().get(7);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getEditorCandies_NewDiagramFileWizardClassName() {
 		return (EAttribute)editorCandiesEClass.getEStructuralFeatures().get(8);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getEditorCandies_DiagramContentInitializerClassName() {
 		return (EAttribute)editorCandiesEClass.getEStructuralFeatures().get(9);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getEditorCandies_MatchingStrategyClassName() {
 		return (EAttribute)editorCandiesEClass.getEStructuralFeatures().get(10);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getEditorCandies_VisualIDRegistryClassName() {
 		return (EAttribute)editorCandiesEClass.getEStructuralFeatures().get(11);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getEditorCandies_ElementChooserClassName() {
 		return (EAttribute)editorCandiesEClass.getEStructuralFeatures().get(12);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getEditorCandies_LoadResourceActionClassName() {
 		return (EAttribute)editorCandiesEClass.getEStructuralFeatures().get(13);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getEditorCandies_EditingDomainID() {
 		return (EAttribute)editorCandiesEClass.getEStructuralFeatures().get(14);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EClass getEditPartCandies() {
 		return editPartCandiesEClass;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getEditPartCandies_ReorientConnectionViewCommandClassName() {
 		return (EAttribute)editPartCandiesEClass.getEStructuralFeatures().get(0);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getEditPartCandies_BaseEditHelperClassName() {
 		return (EAttribute)editPartCandiesEClass.getEStructuralFeatures().get(1);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getEditPartCandies_EditPartFactoryClassName() {
 		return (EAttribute)editPartCandiesEClass.getEStructuralFeatures().get(2);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getEditPartCandies_BaseExternalNodeLabelEditPartClassName() {
 		return (EAttribute)editPartCandiesEClass.getEStructuralFeatures().get(3);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getEditPartCandies_BaseItemSemanticEditPolicyClassName() {
 		return (EAttribute)editPartCandiesEClass.getEStructuralFeatures().get(4);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getEditPartCandies_BaseGraphicalNodeEditPolicyClassName() {
 		return (EAttribute)editPartCandiesEClass.getEStructuralFeatures().get(5);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getEditPartCandies_TextSelectionEditPolicyClassName() {
 		return (EAttribute)editPartCandiesEClass.getEStructuralFeatures().get(6);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getEditPartCandies_TextNonResizableEditPolicyClassName() {
 		return (EAttribute)editPartCandiesEClass.getEStructuralFeatures().get(7);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EClass getMeasurementUnit() {
 		return measurementUnitEClass;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getMeasurementUnit_Units() {
 		return (EAttribute)measurementUnitEClass.getEStructuralFeatures().get(0);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EClass getGenDiagramUpdater() {
 		return genDiagramUpdaterEClass;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getGenDiagramUpdater_EditorGen() {
 		return (EReference)genDiagramUpdaterEClass.getEStructuralFeatures().get(0);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getGenDiagramUpdater_DiagramUpdaterClassName() {
 		return (EAttribute)genDiagramUpdaterEClass.getEStructuralFeatures().get(1);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getGenDiagramUpdater_NodeDescriptorClassName() {
 		return (EAttribute)genDiagramUpdaterEClass.getEStructuralFeatures().get(2);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getGenDiagramUpdater_LinkDescriptorClassName() {
 		return (EAttribute)genDiagramUpdaterEClass.getEStructuralFeatures().get(3);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getGenDiagramUpdater_UpdateCommandClassName() {
 		return (EAttribute)genDiagramUpdaterEClass.getEStructuralFeatures().get(4);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getGenDiagramUpdater_UpdateCommandID() {
 		return (EAttribute)genDiagramUpdaterEClass.getEStructuralFeatures().get(5);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EClass getGenPlugin() {
 		return genPluginEClass;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getGenPlugin_EditorGen() {
 		return (EReference)genPluginEClass.getEStructuralFeatures().get(0);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getGenPlugin_ID() {
 		return (EAttribute)genPluginEClass.getEStructuralFeatures().get(1);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getGenPlugin_Name() {
 		return (EAttribute)genPluginEClass.getEStructuralFeatures().get(2);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getGenPlugin_Provider() {
 		return (EAttribute)genPluginEClass.getEStructuralFeatures().get(3);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getGenPlugin_Version() {
 		return (EAttribute)genPluginEClass.getEStructuralFeatures().get(4);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getGenPlugin_PrintingEnabled() {
 		return (EAttribute)genPluginEClass.getEStructuralFeatures().get(5);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getGenPlugin_RequiredPlugins() {
 		return (EAttribute)genPluginEClass.getEStructuralFeatures().get(6);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getGenPlugin_ActivatorClassName() {
 		return (EAttribute)genPluginEClass.getEStructuralFeatures().get(7);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EClass getElementType() {
 		return elementTypeEClass;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getElementType_DiagramElement() {
 		return (EReference)elementTypeEClass.getEStructuralFeatures().get(0);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getElementType_UniqueIdentifier() {
 		return (EAttribute)elementTypeEClass.getEStructuralFeatures().get(1);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getElementType_DisplayName() {
 		return (EAttribute)elementTypeEClass.getEStructuralFeatures().get(2);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getElementType_DefinedExternally() {
 		return (EAttribute)elementTypeEClass.getEStructuralFeatures().get(3);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EClass getMetamodelType() {
 		return metamodelTypeEClass;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getMetamodelType_EditHelperClassName() {
 		return (EAttribute)metamodelTypeEClass.getEStructuralFeatures().get(0);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EClass getSpecializationType() {
 		return specializationTypeEClass;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getSpecializationType_MetamodelType() {
 		return (EReference)specializationTypeEClass.getEStructuralFeatures().get(0);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getSpecializationType_EditHelperAdviceClassName() {
 		return (EAttribute)specializationTypeEClass.getEStructuralFeatures().get(1);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EClass getNotationType() {
 		return notationTypeEClass;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EClass getGenCommonBase() {
 		return genCommonBaseEClass;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getGenCommonBase_DiagramRunTimeClass() {
 		return (EReference)genCommonBaseEClass.getEStructuralFeatures().get(0);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getGenCommonBase_VisualID() {
 		return (EAttribute)genCommonBaseEClass.getEStructuralFeatures().get(1);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getGenCommonBase_ElementType() {
 		return (EReference)genCommonBaseEClass.getEStructuralFeatures().get(2);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getGenCommonBase_EditPartClassName() {
 		return (EAttribute)genCommonBaseEClass.getEStructuralFeatures().get(3);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getGenCommonBase_ItemSemanticEditPolicyClassName() {
 		return (EAttribute)genCommonBaseEClass.getEStructuralFeatures().get(4);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getGenCommonBase_NotationViewFactoryClassName() {
 		return (EAttribute)genCommonBaseEClass.getEStructuralFeatures().get(5);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getGenCommonBase_Viewmap() {
 		return (EReference)genCommonBaseEClass.getEStructuralFeatures().get(6);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getGenCommonBase_Styles() {
 		return (EReference)genCommonBaseEClass.getEStructuralFeatures().get(7);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getGenCommonBase_Behaviour() {
 		return (EReference)genCommonBaseEClass.getEStructuralFeatures().get(8);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getGenCommonBase_SansDomain() {
 		return (EAttribute)genCommonBaseEClass.getEStructuralFeatures().get(9);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EClass getBehaviour() {
 		return behaviourEClass;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getBehaviour_Subject() {
 		return (EReference)behaviourEClass.getEStructuralFeatures().get(0);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EClass getCustomBehaviour() {
 		return customBehaviourEClass;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getCustomBehaviour_Key() {
 		return (EAttribute)customBehaviourEClass.getEStructuralFeatures().get(0);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getCustomBehaviour_EditPolicyQualifiedClassName() {
 		return (EAttribute)customBehaviourEClass.getEStructuralFeatures().get(1);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EClass getSharedBehaviour() {
 		return sharedBehaviourEClass;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getSharedBehaviour_Delegate() {
 		return (EReference)sharedBehaviourEClass.getEStructuralFeatures().get(0);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EClass getOpenDiagramBehaviour() {
 		return openDiagramBehaviourEClass;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getOpenDiagramBehaviour_EditPolicyClassName() {
 		return (EAttribute)openDiagramBehaviourEClass.getEStructuralFeatures().get(0);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getOpenDiagramBehaviour_DiagramKind() {
 		return (EAttribute)openDiagramBehaviourEClass.getEStructuralFeatures().get(1);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getOpenDiagramBehaviour_EditorID() {
 		return (EAttribute)openDiagramBehaviourEClass.getEStructuralFeatures().get(2);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getOpenDiagramBehaviour_OpenAsEclipseEditor() {
 		return (EAttribute)openDiagramBehaviourEClass.getEStructuralFeatures().get(3);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EClass getGenContainerBase() {
 		return genContainerBaseEClass;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getGenContainerBase_ContainedNodes() {
 		return (EReference)genContainerBaseEClass.getEStructuralFeatures().get(0);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getGenContainerBase_CanonicalEditPolicyClassName() {
 		return (EAttribute)genContainerBaseEClass.getEStructuralFeatures().get(1);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EClass getGenNode() {
 		return genNodeEClass;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getGenNode_Labels() {
 		return (EReference)genNodeEClass.getEStructuralFeatures().get(1);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getGenNode_Compartments() {
 		return (EReference)genNodeEClass.getEStructuralFeatures().get(2);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getGenNode_PrimaryDragEditPolicyQualifiedClassName() {
 		return (EAttribute)genNodeEClass.getEStructuralFeatures().get(3);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getGenNode_GraphicalNodeEditPolicyClassName() {
 		return (EAttribute)genNodeEClass.getEStructuralFeatures().get(4);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getGenNode_CreateCommandClassName() {
 		return (EAttribute)genNodeEClass.getEStructuralFeatures().get(5);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getGenNode_ReorientedIncomingLinks() {
 		return (EReference)genNodeEClass.getEStructuralFeatures().get(6);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EClass getGenTopLevelNode() {
 		return genTopLevelNodeEClass;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getGenTopLevelNode_Diagram() {
 		return (EReference)genTopLevelNodeEClass.getEStructuralFeatures().get(0);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EClass getGenChildNode() {
 		return genChildNodeEClass;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getGenChildNode_Diagram() {
 		return (EReference)genChildNodeEClass.getEStructuralFeatures().get(0);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getGenChildNode_Containers() {
 		return (EReference)genChildNodeEClass.getEStructuralFeatures().get(1);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EClass getGenChildSideAffixedNode() {
 		return genChildSideAffixedNodeEClass;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getGenChildSideAffixedNode_PreferredSideName() {
 		return (EAttribute)genChildSideAffixedNodeEClass.getEStructuralFeatures().get(0);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EClass getGenChildLabelNode() {
 		return genChildLabelNodeEClass;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getGenChildLabelNode_LabelReadOnly() {
 		return (EAttribute)genChildLabelNodeEClass.getEStructuralFeatures().get(0);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getGenChildLabelNode_LabelElementIcon() {
 		return (EAttribute)genChildLabelNodeEClass.getEStructuralFeatures().get(1);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getGenChildLabelNode_LabelModelFacet() {
 		return (EReference)genChildLabelNodeEClass.getEStructuralFeatures().get(2);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EClass getGenCompartment() {
 		return genCompartmentEClass;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getGenCompartment_Title() {
 		return (EAttribute)genCompartmentEClass.getEStructuralFeatures().get(0);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getGenCompartment_CanCollapse() {
 		return (EAttribute)genCompartmentEClass.getEStructuralFeatures().get(1);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getGenCompartment_HideIfEmpty() {
 		return (EAttribute)genCompartmentEClass.getEStructuralFeatures().get(2);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getGenCompartment_NeedsTitle() {
 		return (EAttribute)genCompartmentEClass.getEStructuralFeatures().get(3);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getGenCompartment_Diagram() {
 		return (EReference)genCompartmentEClass.getEStructuralFeatures().get(4);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getGenCompartment_Node() {
 		return (EReference)genCompartmentEClass.getEStructuralFeatures().get(5);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getGenCompartment_ListLayout() {
 		return (EAttribute)genCompartmentEClass.getEStructuralFeatures().get(6);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getGenNode_ModelFacet() {
 		return (EReference)genNodeEClass.getEStructuralFeatures().get(0);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EClass getGenChildContainer() {
 		return genChildContainerEClass;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getGenChildContainer_ChildNodes() {
 		return (EReference)genChildContainerEClass.getEStructuralFeatures().get(0);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EClass getGenLink() {
 		return genLinkEClass;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getGenLink_Diagram() {
 		return (EReference)genLinkEClass.getEStructuralFeatures().get(0);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getGenLink_ModelFacet() {
 		return (EReference)genLinkEClass.getEStructuralFeatures().get(1);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getGenLink_Labels() {
 		return (EReference)genLinkEClass.getEStructuralFeatures().get(2);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getGenLink_OutgoingCreationAllowed() {
 		return (EAttribute)genLinkEClass.getEStructuralFeatures().get(3);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getGenLink_IncomingCreationAllowed() {
 		return (EAttribute)genLinkEClass.getEStructuralFeatures().get(4);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getGenLink_ViewDirectionAlignedWithModel() {
 		return (EAttribute)genLinkEClass.getEStructuralFeatures().get(5);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getGenLink_CreationConstraints() {
 		return (EReference)genLinkEClass.getEStructuralFeatures().get(6);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getGenLink_CreateCommandClassName() {
 		return (EAttribute)genLinkEClass.getEStructuralFeatures().get(7);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getGenLink_ReorientCommandClassName() {
 		return (EAttribute)genLinkEClass.getEStructuralFeatures().get(8);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EClass getGenLabel() {
 		return genLabelEClass;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getGenLabel_ReadOnly() {
 		return (EAttribute)genLabelEClass.getEStructuralFeatures().get(0);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getGenLabel_ElementIcon() {
 		return (EAttribute)genLabelEClass.getEStructuralFeatures().get(1);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getGenLabel_ModelFacet() {
 		return (EReference)genLabelEClass.getEStructuralFeatures().get(2);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EClass getGenNodeLabel() {
 		return genNodeLabelEClass;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getGenNodeLabel_Node() {
 		return (EReference)genNodeLabelEClass.getEStructuralFeatures().get(0);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EClass getGenExternalNodeLabel() {
 		return genExternalNodeLabelEClass;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EClass getGenLinkLabel() {
 		return genLinkLabelEClass;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getGenLinkLabel_Link() {
 		return (EReference)genLinkLabelEClass.getEStructuralFeatures().get(0);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getGenLinkLabel_Alignment() {
 		return (EAttribute)genLinkLabelEClass.getEStructuralFeatures().get(1);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EClass getModelFacet() {
 		return modelFacetEClass;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EClass getLinkModelFacet() {
 		return linkModelFacetEClass;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EClass getLabelModelFacet() {
 		return labelModelFacetEClass;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EClass getTypeModelFacet() {
 		return typeModelFacetEClass;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getTypeModelFacet_MetaClass() {
 		return (EReference)typeModelFacetEClass.getEStructuralFeatures().get(0);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getTypeModelFacet_ContainmentMetaFeature() {
 		return (EReference)typeModelFacetEClass.getEStructuralFeatures().get(1);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getTypeModelFacet_ChildMetaFeature() {
 		return (EReference)typeModelFacetEClass.getEStructuralFeatures().get(2);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getTypeModelFacet_ModelElementSelector() {
 		return (EReference)typeModelFacetEClass.getEStructuralFeatures().get(3);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getTypeModelFacet_ModelElementInitializer() {
 		return (EReference)typeModelFacetEClass.getEStructuralFeatures().get(4);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EClass getFeatureLabelModelFacet() {
 		return featureLabelModelFacetEClass;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getFeatureLabelModelFacet_MetaFeatures() {
 		return (EReference)featureLabelModelFacetEClass.getEStructuralFeatures().get(0);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getFeatureLabelModelFacet_ViewPattern() {
 		return (EAttribute)featureLabelModelFacetEClass.getEStructuralFeatures().get(1);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getFeatureLabelModelFacet_EditorPattern() {
 		return (EAttribute)featureLabelModelFacetEClass.getEStructuralFeatures().get(2);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getFeatureLabelModelFacet_ViewMethod() {
 		return (EAttribute)featureLabelModelFacetEClass.getEStructuralFeatures().get(4);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getFeatureLabelModelFacet_EditPattern() {
 		return (EAttribute)featureLabelModelFacetEClass.getEStructuralFeatures().get(3);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getFeatureLabelModelFacet_EditMethod() {
 		return (EAttribute)featureLabelModelFacetEClass.getEStructuralFeatures().get(5);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EClass getDesignLabelModelFacet() {
 		return designLabelModelFacetEClass;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EClass getTypeLinkModelFacet() {
 		return typeLinkModelFacetEClass;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getTypeLinkModelFacet_SourceMetaFeature() {
 		return (EReference)typeLinkModelFacetEClass.getEStructuralFeatures().get(0);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getTypeLinkModelFacet_TargetMetaFeature() {
 		return (EReference)typeLinkModelFacetEClass.getEStructuralFeatures().get(1);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EClass getFeatureLinkModelFacet() {
 		return featureLinkModelFacetEClass;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getFeatureLinkModelFacet_MetaFeature() {
 		return (EReference)featureLinkModelFacetEClass.getEStructuralFeatures().get(0);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EClass getViewmap() {
 		return viewmapEClass;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getViewmap_Attributes() {
 		return (EReference)viewmapEClass.getEStructuralFeatures().get(0);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getViewmap_RequiredPluginIDs() {
 		return (EAttribute)viewmapEClass.getEStructuralFeatures().get(1);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getViewmap_LayoutType() {
 		return (EAttribute)viewmapEClass.getEStructuralFeatures().get(2);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EClass getAttributes() {
 		return attributesEClass;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EClass getColorAttributes() {
 		return colorAttributesEClass;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getColorAttributes_ForegroundColor() {
 		return (EAttribute)colorAttributesEClass.getEStructuralFeatures().get(0);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getColorAttributes_BackgroundColor() {
 		return (EAttribute)colorAttributesEClass.getEStructuralFeatures().get(1);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EClass getStyleAttributes() {
 		return styleAttributesEClass;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getStyleAttributes_FixedFont() {
 		return (EAttribute)styleAttributesEClass.getEStructuralFeatures().get(0);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getStyleAttributes_FixedForeground() {
 		return (EAttribute)styleAttributesEClass.getEStructuralFeatures().get(1);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getStyleAttributes_FixedBackground() {
 		return (EAttribute)styleAttributesEClass.getEStructuralFeatures().get(2);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EClass getResizeConstraints() {
 		return resizeConstraintsEClass;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getResizeConstraints_ResizeHandles() {
 		return (EAttribute)resizeConstraintsEClass.getEStructuralFeatures().get(0);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getResizeConstraints_NonResizeHandles() {
 		return (EAttribute)resizeConstraintsEClass.getEStructuralFeatures().get(1);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getResizeConstraints_ResizeHandleNames() {
 		return (EAttribute)resizeConstraintsEClass.getEStructuralFeatures().get(2);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getResizeConstraints_NonResizeHandleNames() {
 		return (EAttribute)resizeConstraintsEClass.getEStructuralFeatures().get(3);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EClass getDefaultSizeAttributes() {
 		return defaultSizeAttributesEClass;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getDefaultSizeAttributes_Width() {
 		return (EAttribute)defaultSizeAttributesEClass.getEStructuralFeatures().get(0);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getDefaultSizeAttributes_Height() {
 		return (EAttribute)defaultSizeAttributesEClass.getEStructuralFeatures().get(1);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EClass getLabelOffsetAttributes() {
 		return labelOffsetAttributesEClass;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getLabelOffsetAttributes_X() {
 		return (EAttribute)labelOffsetAttributesEClass.getEStructuralFeatures().get(0);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getLabelOffsetAttributes_Y() {
 		return (EAttribute)labelOffsetAttributesEClass.getEStructuralFeatures().get(1);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EClass getFigureViewmap() {
 		return figureViewmapEClass;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getFigureViewmap_FigureQualifiedClassName() {
 		return (EAttribute)figureViewmapEClass.getEStructuralFeatures().get(0);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EClass getSnippetViewmap() {
 		return snippetViewmapEClass;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getSnippetViewmap_Body() {
 		return (EAttribute)snippetViewmapEClass.getEStructuralFeatures().get(0);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EClass getInnerClassViewmap() {
 		return innerClassViewmapEClass;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getInnerClassViewmap_ClassName() {
 		return (EAttribute)innerClassViewmapEClass.getEStructuralFeatures().get(0);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getInnerClassViewmap_ClassBody() {
 		return (EAttribute)innerClassViewmapEClass.getEStructuralFeatures().get(1);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EClass getParentAssignedViewmap() {
 		return parentAssignedViewmapEClass;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getParentAssignedViewmap_GetterName() {
 		return (EAttribute)parentAssignedViewmapEClass.getEStructuralFeatures().get(0);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getParentAssignedViewmap_SetterName() {
 		return (EAttribute)parentAssignedViewmapEClass.getEStructuralFeatures().get(1);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getParentAssignedViewmap_FigureQualifiedClassName() {
 		return (EAttribute)parentAssignedViewmapEClass.getEStructuralFeatures().get(2);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EClass getValueExpression() {
 		return valueExpressionEClass;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getValueExpression_Body() {
 		return (EAttribute)valueExpressionEClass.getEStructuralFeatures().get(0);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getValueExpression_Language() {
 		return (EAttribute)valueExpressionEClass.getEStructuralFeatures().get(1);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getValueExpression_LangName() {
 		return (EAttribute)valueExpressionEClass.getEStructuralFeatures().get(2);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EClass getGenConstraint() {
 		return genConstraintEClass;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EClass getPalette() {
 		return paletteEClass;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getPalette_Diagram() {
 		return (EReference)paletteEClass.getEStructuralFeatures().get(0);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getPalette_Flyout() {
 		return (EAttribute)paletteEClass.getEStructuralFeatures().get(1);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getPalette_Groups() {
 		return (EReference)paletteEClass.getEStructuralFeatures().get(2);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getPalette_PackageName() {
 		return (EAttribute)paletteEClass.getEStructuralFeatures().get(3);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getPalette_FactoryClassName() {
 		return (EAttribute)paletteEClass.getEStructuralFeatures().get(4);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EClass getEntryBase() {
 		return entryBaseEClass;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getEntryBase_Title() {
 		return (EAttribute)entryBaseEClass.getEStructuralFeatures().get(0);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getEntryBase_Description() {
 		return (EAttribute)entryBaseEClass.getEStructuralFeatures().get(1);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getEntryBase_LargeIconPath() {
 		return (EAttribute)entryBaseEClass.getEStructuralFeatures().get(2);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getEntryBase_SmallIconPath() {
 		return (EAttribute)entryBaseEClass.getEStructuralFeatures().get(3);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getEntryBase_CreateMethodName() {
 		return (EAttribute)entryBaseEClass.getEStructuralFeatures().get(4);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EClass getAbstractToolEntry() {
 		return abstractToolEntryEClass;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getAbstractToolEntry_Default() {
 		return (EAttribute)abstractToolEntryEClass.getEStructuralFeatures().get(0);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getAbstractToolEntry_QualifiedToolName() {
 		return (EAttribute)abstractToolEntryEClass.getEStructuralFeatures().get(1);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getAbstractToolEntry_Properties() {
 		return (EReference)abstractToolEntryEClass.getEStructuralFeatures().get(2);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EClass getToolEntry() {
 		return toolEntryEClass;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getToolEntry_GenNodes() {
 		return (EReference)toolEntryEClass.getEStructuralFeatures().get(0);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getToolEntry_GenLinks() {
 		return (EReference)toolEntryEClass.getEStructuralFeatures().get(1);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getToolEntry_Elements() {
 		return (EReference)toolEntryEClass.getEStructuralFeatures().get(2);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EClass getStandardEntry() {
 		return standardEntryEClass;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getStandardEntry_Kind() {
 		return (EAttribute)standardEntryEClass.getEStructuralFeatures().get(0);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EClass getToolGroupItem() {
 		return toolGroupItemEClass;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getToolGroupItem_Group() {
 		return (EReference)toolGroupItemEClass.getEStructuralFeatures().get(0);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EClass getSeparator() {
 		return separatorEClass;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EClass getToolGroup() {
 		return toolGroupEClass;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getToolGroup_Palette() {
 		return (EReference)toolGroupEClass.getEStructuralFeatures().get(0);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getToolGroup_Stack() {
 		return (EAttribute)toolGroupEClass.getEStructuralFeatures().get(1);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getToolGroup_Collapse() {
 		return (EAttribute)toolGroupEClass.getEStructuralFeatures().get(2);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getToolGroup_Entries() {
 		return (EReference)toolGroupEClass.getEStructuralFeatures().get(3);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getToolGroup_ToolsOnly() {
 		return (EAttribute)toolGroupEClass.getEStructuralFeatures().get(4);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EClass getGenElementInitializer() {
 		return genElementInitializerEClass;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getGenElementInitializer_TypeModelFacet() {
 		return (EReference)genElementInitializerEClass.getEStructuralFeatures().get(0);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EClass getGenFeatureSeqInitializer() {
 		return genFeatureSeqInitializerEClass;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getGenFeatureSeqInitializer_Initializers() {
 		return (EReference)genFeatureSeqInitializerEClass.getEStructuralFeatures().get(0);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getGenFeatureSeqInitializer_ElementClass() {
 		return (EReference)genFeatureSeqInitializerEClass.getEStructuralFeatures().get(1);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getGenFeatureSeqInitializer_CreatingInitializer() {
 		return (EReference)genFeatureSeqInitializerEClass.getEStructuralFeatures().get(2);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EClass getGenFeatureValueSpec() {
 		return genFeatureValueSpecEClass;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EClass getGenReferenceNewElementSpec() {
 		return genReferenceNewElementSpecEClass;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getGenReferenceNewElementSpec_NewElementInitializers() {
 		return (EReference)genReferenceNewElementSpecEClass.getEStructuralFeatures().get(0);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EClass getGenFeatureInitializer() {
 		return genFeatureInitializerEClass;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getGenFeatureInitializer_Feature() {
 		return (EReference)genFeatureInitializerEClass.getEStructuralFeatures().get(0);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getGenFeatureInitializer_FeatureSeqInitializer() {
 		return (EReference)genFeatureInitializerEClass.getEStructuralFeatures().get(1);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EClass getGenLinkConstraints() {
 		return genLinkConstraintsEClass;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getGenLinkConstraints_Link() {
 		return (EReference)genLinkConstraintsEClass.getEStructuralFeatures().get(0);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getGenLinkConstraints_SourceEnd() {
 		return (EReference)genLinkConstraintsEClass.getEStructuralFeatures().get(1);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getGenLinkConstraints_TargetEnd() {
 		return (EReference)genLinkConstraintsEClass.getEStructuralFeatures().get(2);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EClass getGenAuditRoot() {
 		return genAuditRootEClass;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getGenAuditRoot_EditorGen() {
 		return (EReference)genAuditRootEClass.getEStructuralFeatures().get(0);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getGenAuditRoot_Categories() {
 		return (EReference)genAuditRootEClass.getEStructuralFeatures().get(1);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getGenAuditRoot_Rules() {
 		return (EReference)genAuditRootEClass.getEStructuralFeatures().get(2);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EClass getGenAuditContainer() {
 		return genAuditContainerEClass;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getGenAuditContainer_Root() {
 		return (EReference)genAuditContainerEClass.getEStructuralFeatures().get(0);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EClass getGenRuleBase() {
 		return genRuleBaseEClass;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getGenRuleBase_Name() {
 		return (EAttribute)genRuleBaseEClass.getEStructuralFeatures().get(0);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getGenRuleBase_Description() {
 		return (EAttribute)genRuleBaseEClass.getEStructuralFeatures().get(1);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getGenAuditContainer_Id() {
 		return (EAttribute)genAuditContainerEClass.getEStructuralFeatures().get(1);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getGenAuditContainer_Name() {
 		return (EAttribute)genAuditContainerEClass.getEStructuralFeatures().get(2);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getGenAuditContainer_Description() {
 		return (EAttribute)genAuditContainerEClass.getEStructuralFeatures().get(3);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getGenAuditContainer_Path() {
 		return (EReference)genAuditContainerEClass.getEStructuralFeatures().get(4);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getGenAuditContainer_Audits() {
 		return (EReference)genAuditContainerEClass.getEStructuralFeatures().get(5);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EClass getGenAuditRule() {
 		return genAuditRuleEClass;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getGenAuditRule_Root() {
 		return (EReference)genAuditRuleEClass.getEStructuralFeatures().get(0);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getGenAuditRule_ContextSelectorLocalClassName() {
 		return (EAttribute)genAuditRuleEClass.getEStructuralFeatures().get(1);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getGenAuditRule_Id() {
 		return (EAttribute)genAuditRuleEClass.getEStructuralFeatures().get(2);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getGenAuditRule_Rule() {
 		return (EReference)genAuditRuleEClass.getEStructuralFeatures().get(3);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getGenAuditRule_Target() {
 		return (EReference)genAuditRuleEClass.getEStructuralFeatures().get(4);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getGenAuditRule_Message() {
 		return (EAttribute)genAuditRuleEClass.getEStructuralFeatures().get(5);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getGenAuditRule_Severity() {
 		return (EAttribute)genAuditRuleEClass.getEStructuralFeatures().get(6);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getGenAuditRule_UseInLiveMode() {
 		return (EAttribute)genAuditRuleEClass.getEStructuralFeatures().get(7);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getGenAuditRule_RequiresConstraintAdapter() {
 		return (EAttribute)genAuditRuleEClass.getEStructuralFeatures().get(8);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getGenAuditRule_Category() {
 		return (EReference)genAuditRuleEClass.getEStructuralFeatures().get(9);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EClass getGenRuleTarget() {
 		return genRuleTargetEClass;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EClass getGenDomainElementTarget() {
 		return genDomainElementTargetEClass;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getGenDomainElementTarget_Element() {
 		return (EReference)genDomainElementTargetEClass.getEStructuralFeatures().get(0);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EClass getGenDiagramElementTarget() {
 		return genDiagramElementTargetEClass;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getGenDiagramElementTarget_Element() {
 		return (EReference)genDiagramElementTargetEClass.getEStructuralFeatures().get(0);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EClass getGenDomainAttributeTarget() {
 		return genDomainAttributeTargetEClass;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getGenDomainAttributeTarget_Attribute() {
 		return (EReference)genDomainAttributeTargetEClass.getEStructuralFeatures().get(0);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getGenDomainAttributeTarget_NullAsError() {
 		return (EAttribute)genDomainAttributeTargetEClass.getEStructuralFeatures().get(1);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EClass getGenNotationElementTarget() {
 		return genNotationElementTargetEClass;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getGenNotationElementTarget_Element() {
 		return (EReference)genNotationElementTargetEClass.getEStructuralFeatures().get(0);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EClass getGenMetricContainer() {
 		return genMetricContainerEClass;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getGenMetricContainer_EditorGen() {
 		return (EReference)genMetricContainerEClass.getEStructuralFeatures().get(0);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getGenMetricContainer_Metrics() {
 		return (EReference)genMetricContainerEClass.getEStructuralFeatures().get(1);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EClass getGenMetricRule() {
 		return genMetricRuleEClass;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getGenMetricRule_Key() {
 		return (EAttribute)genMetricRuleEClass.getEStructuralFeatures().get(0);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getGenMetricRule_Rule() {
 		return (EReference)genMetricRuleEClass.getEStructuralFeatures().get(1);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getGenMetricRule_Target() {
 		return (EReference)genMetricRuleEClass.getEStructuralFeatures().get(2);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getGenMetricRule_Container() {
 		return (EReference)genMetricRuleEClass.getEStructuralFeatures().get(5);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getGenMetricRule_LowLimit() {
 		return (EAttribute)genMetricRuleEClass.getEStructuralFeatures().get(3);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getGenMetricRule_HighLimit() {
 		return (EAttribute)genMetricRuleEClass.getEStructuralFeatures().get(4);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EClass getGenAuditedMetricTarget() {
 		return genAuditedMetricTargetEClass;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getGenAuditedMetricTarget_Metric() {
 		return (EReference)genAuditedMetricTargetEClass.getEStructuralFeatures().get(0);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getGenAuditedMetricTarget_MetricValueContext() {
 		return (EReference)genAuditedMetricTargetEClass.getEStructuralFeatures().get(1);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EClass getGenAuditable() {
 		return genAuditableEClass;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EClass getGenMeasurable() {
 		return genMeasurableEClass;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EClass getGenExpressionProviderContainer() {
 		return genExpressionProviderContainerEClass;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getGenExpressionProviderContainer_ExpressionsPackageName() {
 		return (EAttribute)genExpressionProviderContainerEClass.getEStructuralFeatures().get(0);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getGenExpressionProviderContainer_AbstractExpressionClassName() {
 		return (EAttribute)genExpressionProviderContainerEClass.getEStructuralFeatures().get(1);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getGenExpressionProviderContainer_Providers() {
 		return (EReference)genExpressionProviderContainerEClass.getEStructuralFeatures().get(2);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getGenExpressionProviderContainer_EditorGen() {
 		return (EReference)genExpressionProviderContainerEClass.getEStructuralFeatures().get(3);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EClass getGenExpressionProviderBase() {
 		return genExpressionProviderBaseEClass;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getGenExpressionProviderBase_Expressions() {
 		return (EReference)genExpressionProviderBaseEClass.getEStructuralFeatures().get(0);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getGenExpressionProviderBase_Container() {
 		return (EReference)genExpressionProviderBaseEClass.getEStructuralFeatures().get(1);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EClass getGenJavaExpressionProvider() {
 		return genJavaExpressionProviderEClass;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EClass getGenExpressionInterpreter() {
 		return genExpressionInterpreterEClass;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getGenExpressionInterpreter_Language() {
 		return (EAttribute)genExpressionInterpreterEClass.getEStructuralFeatures().get(0);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getGenExpressionInterpreter_ClassName() {
 		return (EAttribute)genExpressionInterpreterEClass.getEStructuralFeatures().get(1);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EClass getGenDomainModelNavigator() {
 		return genDomainModelNavigatorEClass;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getGenDomainModelNavigator_GenerateDomainModelNavigator() {
 		return (EAttribute)genDomainModelNavigatorEClass.getEStructuralFeatures().get(0);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getGenDomainModelNavigator_DomainContentExtensionID() {
 		return (EAttribute)genDomainModelNavigatorEClass.getEStructuralFeatures().get(1);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getGenDomainModelNavigator_DomainContentExtensionName() {
 		return (EAttribute)genDomainModelNavigatorEClass.getEStructuralFeatures().get(2);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getGenDomainModelNavigator_DomainContentExtensionPriority() {
 		return (EAttribute)genDomainModelNavigatorEClass.getEStructuralFeatures().get(3);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getGenDomainModelNavigator_DomainContentProviderClassName() {
 		return (EAttribute)genDomainModelNavigatorEClass.getEStructuralFeatures().get(4);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getGenDomainModelNavigator_DomainLabelProviderClassName() {
 		return (EAttribute)genDomainModelNavigatorEClass.getEStructuralFeatures().get(5);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getGenDomainModelNavigator_DomainModelElementTesterClassName() {
 		return (EAttribute)genDomainModelNavigatorEClass.getEStructuralFeatures().get(6);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getGenDomainModelNavigator_DomainNavigatorItemClassName() {
 		return (EAttribute)genDomainModelNavigatorEClass.getEStructuralFeatures().get(7);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EClass getGenNavigator() {
 		return genNavigatorEClass;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getGenNavigator_EditorGen() {
 		return (EReference)genNavigatorEClass.getEStructuralFeatures().get(0);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getGenNavigator_ContentExtensionID() {
 		return (EAttribute)genNavigatorEClass.getEStructuralFeatures().get(1);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getGenNavigator_ContentExtensionName() {
 		return (EAttribute)genNavigatorEClass.getEStructuralFeatures().get(2);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getGenNavigator_ContentExtensionPriority() {
 		return (EAttribute)genNavigatorEClass.getEStructuralFeatures().get(3);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getGenNavigator_LinkHelperExtensionID() {
 		return (EAttribute)genNavigatorEClass.getEStructuralFeatures().get(4);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getGenNavigator_SorterExtensionID() {
 		return (EAttribute)genNavigatorEClass.getEStructuralFeatures().get(5);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getGenNavigator_ActionProviderID() {
 		return (EAttribute)genNavigatorEClass.getEStructuralFeatures().get(6);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getGenNavigator_ContentProviderClassName() {
 		return (EAttribute)genNavigatorEClass.getEStructuralFeatures().get(7);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getGenNavigator_LabelProviderClassName() {
 		return (EAttribute)genNavigatorEClass.getEStructuralFeatures().get(8);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getGenNavigator_LinkHelperClassName() {
 		return (EAttribute)genNavigatorEClass.getEStructuralFeatures().get(9);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getGenNavigator_SorterClassName() {
 		return (EAttribute)genNavigatorEClass.getEStructuralFeatures().get(10);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getGenNavigator_ActionProviderClassName() {
 		return (EAttribute)genNavigatorEClass.getEStructuralFeatures().get(11);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getGenNavigator_AbstractNavigatorItemClassName() {
 		return (EAttribute)genNavigatorEClass.getEStructuralFeatures().get(12);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getGenNavigator_NavigatorGroupClassName() {
 		return (EAttribute)genNavigatorEClass.getEStructuralFeatures().get(13);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getGenNavigator_NavigatorItemClassName() {
 		return (EAttribute)genNavigatorEClass.getEStructuralFeatures().get(14);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getGenNavigator_UriInputTesterClassName() {
 		return (EAttribute)genNavigatorEClass.getEStructuralFeatures().get(15);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getGenNavigator_PackageName() {
 		return (EAttribute)genNavigatorEClass.getEStructuralFeatures().get(16);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getGenNavigator_ChildReferences() {
 		return (EReference)genNavigatorEClass.getEStructuralFeatures().get(17);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EClass getGenNavigatorChildReference() {
 		return genNavigatorChildReferenceEClass;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getGenNavigatorChildReference_Navigator() {
 		return (EReference)genNavigatorChildReferenceEClass.getEStructuralFeatures().get(0);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getGenNavigatorChildReference_Parent() {
 		return (EReference)genNavigatorChildReferenceEClass.getEStructuralFeatures().get(1);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getGenNavigatorChildReference_Child() {
 		return (EReference)genNavigatorChildReferenceEClass.getEStructuralFeatures().get(2);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getGenNavigatorChildReference_ReferenceType() {
 		return (EAttribute)genNavigatorChildReferenceEClass.getEStructuralFeatures().get(3);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getGenNavigatorChildReference_GroupName() {
 		return (EAttribute)genNavigatorChildReferenceEClass.getEStructuralFeatures().get(4);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getGenNavigatorChildReference_GroupIcon() {
 		return (EAttribute)genNavigatorChildReferenceEClass.getEStructuralFeatures().get(5);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getGenNavigatorChildReference_HideIfEmpty() {
 		return (EAttribute)genNavigatorChildReferenceEClass.getEStructuralFeatures().get(6);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EClass getGenNavigatorPath() {
 		return genNavigatorPathEClass;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getGenNavigatorPath_Segments() {
 		return (EReference)genNavigatorPathEClass.getEStructuralFeatures().get(0);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EClass getGenNavigatorPathSegment() {
 		return genNavigatorPathSegmentEClass;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getGenNavigatorPathSegment_Path() {
 		return (EReference)genNavigatorPathSegmentEClass.getEStructuralFeatures().get(0);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getGenNavigatorPathSegment_From() {
 		return (EReference)genNavigatorPathSegmentEClass.getEStructuralFeatures().get(1);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getGenNavigatorPathSegment_To() {
 		return (EReference)genNavigatorPathSegmentEClass.getEStructuralFeatures().get(2);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EClass getGenPropertySheet() {
 		return genPropertySheetEClass;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getGenPropertySheet_EditorGen() {
 		return (EReference)genPropertySheetEClass.getEStructuralFeatures().get(0);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getGenPropertySheet_Tabs() {
 		return (EReference)genPropertySheetEClass.getEStructuralFeatures().get(1);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getGenPropertySheet_PackageName() {
 		return (EAttribute)genPropertySheetEClass.getEStructuralFeatures().get(2);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getGenPropertySheet_ReadOnly() {
 		return (EAttribute)genPropertySheetEClass.getEStructuralFeatures().get(3);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getGenPropertySheet_NeedsCaption() {
 		return (EAttribute)genPropertySheetEClass.getEStructuralFeatures().get(4);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getGenPropertySheet_LabelProviderClassName() {
 		return (EAttribute)genPropertySheetEClass.getEStructuralFeatures().get(5);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EClass getGenPropertyTab() {
 		return genPropertyTabEClass;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getGenPropertyTab_Sheet() {
 		return (EReference)genPropertyTabEClass.getEStructuralFeatures().get(0);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getGenPropertyTab_ID() {
 		return (EAttribute)genPropertyTabEClass.getEStructuralFeatures().get(1);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getGenPropertyTab_Label() {
 		return (EAttribute)genPropertyTabEClass.getEStructuralFeatures().get(2);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EClass getGenStandardPropertyTab() {
 		return genStandardPropertyTabEClass;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EClass getGenCustomPropertyTab() {
 		return genCustomPropertyTabEClass;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getGenCustomPropertyTab_ClassName() {
 		return (EAttribute)genCustomPropertyTabEClass.getEStructuralFeatures().get(0);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getGenCustomPropertyTab_Filter() {
 		return (EReference)genCustomPropertyTabEClass.getEStructuralFeatures().get(1);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EClass getGenPropertyTabFilter() {
 		return genPropertyTabFilterEClass;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getGenPropertyTabFilter_Tab() {
 		return (EReference)genPropertyTabFilterEClass.getEStructuralFeatures().get(0);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EClass getTypeTabFilter() {
 		return typeTabFilterEClass;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getTypeTabFilter_Types() {
 		return (EAttribute)typeTabFilterEClass.getEStructuralFeatures().get(0);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getTypeTabFilter_GeneratedTypes() {
 		return (EAttribute)typeTabFilterEClass.getEStructuralFeatures().get(1);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EClass getCustomTabFilter() {
 		return customTabFilterEClass;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getCustomTabFilter_ClassName() {
 		return (EAttribute)customTabFilterEClass.getEStructuralFeatures().get(0);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EClass getGenContributionItem() {
 		return genContributionItemEClass;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getGenContributionItem_Owner() {
 		return (EReference)genContributionItemEClass.getEStructuralFeatures().get(0);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getGenContributionItem_Application() {
 		return (EReference)genContributionItemEClass.getEStructuralFeatures().get(1);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EClass getGenSharedContributionItem() {
 		return genSharedContributionItemEClass;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getGenSharedContributionItem_ActualItem() {
 		return (EReference)genSharedContributionItemEClass.getEStructuralFeatures().get(0);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EClass getGenGroupMarker() {
 		return genGroupMarkerEClass;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getGenGroupMarker_GroupName() {
 		return (EAttribute)genGroupMarkerEClass.getEStructuralFeatures().get(0);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EClass getGenSeparator() {
 		return genSeparatorEClass;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getGenSeparator_GroupName() {
 		return (EAttribute)genSeparatorEClass.getEStructuralFeatures().get(0);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EClass getGenActionFactoryContributionItem() {
 		return genActionFactoryContributionItemEClass;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getGenActionFactoryContributionItem_Name() {
 		return (EAttribute)genActionFactoryContributionItemEClass.getEStructuralFeatures().get(0);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EClass getGenContributionManager() {
 		return genContributionManagerEClass;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getGenContributionManager_ID() {
 		return (EAttribute)genContributionManagerEClass.getEStructuralFeatures().get(0);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getGenContributionManager_Items() {
 		return (EReference)genContributionManagerEClass.getEStructuralFeatures().get(1);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EClass getGenMenuManager() {
 		return genMenuManagerEClass;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getGenMenuManager_Name() {
 		return (EAttribute)genMenuManagerEClass.getEStructuralFeatures().get(0);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EClass getGenToolBarManager() {
 		return genToolBarManagerEClass;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EClass getGenApplication() {
 		return genApplicationEClass;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getGenApplication_EditorGen() {
 		return (EReference)genApplicationEClass.getEStructuralFeatures().get(0);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getGenApplication_ID() {
 		return (EAttribute)genApplicationEClass.getEStructuralFeatures().get(1);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getGenApplication_Title() {
 		return (EAttribute)genApplicationEClass.getEStructuralFeatures().get(2);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getGenApplication_PackageName() {
 		return (EAttribute)genApplicationEClass.getEStructuralFeatures().get(3);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getGenApplication_ClassName() {
 		return (EAttribute)genApplicationEClass.getEStructuralFeatures().get(4);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getGenApplication_WorkbenchAdvisorClassName() {
 		return (EAttribute)genApplicationEClass.getEStructuralFeatures().get(5);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getGenApplication_WorkbenchWindowAdvisorClassName() {
 		return (EAttribute)genApplicationEClass.getEStructuralFeatures().get(6);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getGenApplication_ActionBarAdvisorClassName() {
 		return (EAttribute)genApplicationEClass.getEStructuralFeatures().get(7);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getGenApplication_PerspectiveClassName() {
 		return (EAttribute)genApplicationEClass.getEStructuralFeatures().get(8);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getGenApplication_PerspectiveId() {
 		return (EAttribute)genApplicationEClass.getEStructuralFeatures().get(9);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getGenApplication_SupportFiles() {
 		return (EAttribute)genApplicationEClass.getEStructuralFeatures().get(10);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getGenApplication_SharedContributionItems() {
 		return (EReference)genApplicationEClass.getEStructuralFeatures().get(11);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getGenApplication_MainMenu() {
 		return (EReference)genApplicationEClass.getEStructuralFeatures().get(12);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getGenApplication_MainToolBar() {
 		return (EReference)genApplicationEClass.getEStructuralFeatures().get(13);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EEnum getStandardPreferencePages() {
 		return standardPreferencePagesEEnum;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EEnum getRulerUnits() {
 		return rulerUnitsEEnum;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EEnum getRouting() {
 		return routingEEnum;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EEnum getJFaceFont() {
 		return jFaceFontEEnum;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EEnum getFontStyle() {
 		return fontStyleEEnum;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EEnum getDiagramColors() {
 		return diagramColorsEEnum;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EEnum getProviderPriority() {
 		return providerPriorityEEnum;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EEnum getLinkLabelAlignment() {
 		return linkLabelAlignmentEEnum;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EEnum getLabelTextAccessMethod() {
 		return labelTextAccessMethodEEnum;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EEnum getViewmapLayoutType() {
 		return viewmapLayoutTypeEEnum;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EEnum getStandardEntryKind() {
 		return standardEntryKindEEnum;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EEnum getGenSeverity() {
 		return genSeverityEEnum;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EEnum getGenLanguage() {
 		return genLanguageEEnum;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EEnum getGenNavigatorReferenceType() {
 		return genNavigatorReferenceTypeEEnum;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EEnum getGeneratedType() {
 		return generatedTypeEEnum;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public GMFGenFactory getGMFGenFactory() {
 		return (GMFGenFactory)getEFactoryInstance();
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private boolean isCreated = false;
 
 	/**
 	 * Creates the meta-model objects for the package.  This method is
 	 * guarded to have no affect on any invocation but its first.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public void createPackageContents() {
 		if (isCreated) return;
 		isCreated = true;
 
 		// Create classes and their features
 		genEditorGeneratorEClass = createEClass(GEN_EDITOR_GENERATOR);
 		createEReference(genEditorGeneratorEClass, GEN_EDITOR_GENERATOR__AUDITS);
 		createEReference(genEditorGeneratorEClass, GEN_EDITOR_GENERATOR__METRICS);
 		createEReference(genEditorGeneratorEClass, GEN_EDITOR_GENERATOR__DIAGRAM);
 		createEReference(genEditorGeneratorEClass, GEN_EDITOR_GENERATOR__PLUGIN);
 		createEReference(genEditorGeneratorEClass, GEN_EDITOR_GENERATOR__EDITOR);
 		createEReference(genEditorGeneratorEClass, GEN_EDITOR_GENERATOR__NAVIGATOR);
 		createEReference(genEditorGeneratorEClass, GEN_EDITOR_GENERATOR__DIAGRAM_UPDATER);
 		createEReference(genEditorGeneratorEClass, GEN_EDITOR_GENERATOR__PROPERTY_SHEET);
 		createEReference(genEditorGeneratorEClass, GEN_EDITOR_GENERATOR__APPLICATION);
 		createEReference(genEditorGeneratorEClass, GEN_EDITOR_GENERATOR__DOMAIN_GEN_MODEL);
 		createEAttribute(genEditorGeneratorEClass, GEN_EDITOR_GENERATOR__PACKAGE_NAME_PREFIX);
 		createEAttribute(genEditorGeneratorEClass, GEN_EDITOR_GENERATOR__MODEL_ID);
 		createEAttribute(genEditorGeneratorEClass, GEN_EDITOR_GENERATOR__SAME_FILE_FOR_DIAGRAM_AND_MODEL);
 		createEAttribute(genEditorGeneratorEClass, GEN_EDITOR_GENERATOR__DIAGRAM_FILE_EXTENSION);
 		createEAttribute(genEditorGeneratorEClass, GEN_EDITOR_GENERATOR__DOMAIN_FILE_EXTENSION);
 		createEAttribute(genEditorGeneratorEClass, GEN_EDITOR_GENERATOR__DYNAMIC_TEMPLATES);
 		createEAttribute(genEditorGeneratorEClass, GEN_EDITOR_GENERATOR__TEMPLATE_DIRECTORY);
 		createEAttribute(genEditorGeneratorEClass, GEN_EDITOR_GENERATOR__COPYRIGHT_TEXT);
 		createEReference(genEditorGeneratorEClass, GEN_EDITOR_GENERATOR__EXPRESSION_PROVIDERS);
 
 		genDiagramEClass = createEClass(GEN_DIAGRAM);
 		createEReference(genDiagramEClass, GEN_DIAGRAM__EDITOR_GEN);
 		createEReference(genDiagramEClass, GEN_DIAGRAM__DOMAIN_DIAGRAM_ELEMENT);
 		createEReference(genDiagramEClass, GEN_DIAGRAM__CHILD_NODES);
 		createEReference(genDiagramEClass, GEN_DIAGRAM__TOP_LEVEL_NODES);
 		createEReference(genDiagramEClass, GEN_DIAGRAM__LINKS);
 		createEReference(genDiagramEClass, GEN_DIAGRAM__COMPARTMENTS);
 		createEReference(genDiagramEClass, GEN_DIAGRAM__PALETTE);
 		createEAttribute(genDiagramEClass, GEN_DIAGRAM__SYNCHRONIZED);
 		createEReference(genDiagramEClass, GEN_DIAGRAM__PREFERENCES);
 		createEReference(genDiagramEClass, GEN_DIAGRAM__PREFERENCE_PAGES);
 
 		genEditorViewEClass = createEClass(GEN_EDITOR_VIEW);
 		createEReference(genEditorViewEClass, GEN_EDITOR_VIEW__EDITOR_GEN);
 		createEAttribute(genEditorViewEClass, GEN_EDITOR_VIEW__PACKAGE_NAME);
 		createEAttribute(genEditorViewEClass, GEN_EDITOR_VIEW__ACTION_BAR_CONTRIBUTOR_CLASS_NAME);
 		createEAttribute(genEditorViewEClass, GEN_EDITOR_VIEW__CLASS_NAME);
 		createEAttribute(genEditorViewEClass, GEN_EDITOR_VIEW__ICON_PATH);
 		createEAttribute(genEditorViewEClass, GEN_EDITOR_VIEW__ICON_PATH_X);
 		createEAttribute(genEditorViewEClass, GEN_EDITOR_VIEW__ID);
 		createEAttribute(genEditorViewEClass, GEN_EDITOR_VIEW__ECLIPSE_EDITOR);
 		createEAttribute(genEditorViewEClass, GEN_EDITOR_VIEW__CONTEXT_ID);
 
 		genPreferencePageEClass = createEClass(GEN_PREFERENCE_PAGE);
 		createEAttribute(genPreferencePageEClass, GEN_PREFERENCE_PAGE__ID);
 		createEAttribute(genPreferencePageEClass, GEN_PREFERENCE_PAGE__NAME);
 		createEReference(genPreferencePageEClass, GEN_PREFERENCE_PAGE__CHILDREN);
 		createEReference(genPreferencePageEClass, GEN_PREFERENCE_PAGE__PARENT);
 
 		genCustomPreferencePageEClass = createEClass(GEN_CUSTOM_PREFERENCE_PAGE);
 		createEAttribute(genCustomPreferencePageEClass, GEN_CUSTOM_PREFERENCE_PAGE__QUALIFIED_CLASS_NAME);
 
 		genStandardPreferencePageEClass = createEClass(GEN_STANDARD_PREFERENCE_PAGE);
 		createEAttribute(genStandardPreferencePageEClass, GEN_STANDARD_PREFERENCE_PAGE__KIND);
 
 		genDiagramPreferencesEClass = createEClass(GEN_DIAGRAM_PREFERENCES);
 		createEAttribute(genDiagramPreferencesEClass, GEN_DIAGRAM_PREFERENCES__LINE_STYLE);
 		createEReference(genDiagramPreferencesEClass, GEN_DIAGRAM_PREFERENCES__DEFAULT_FONT);
 		createEReference(genDiagramPreferencesEClass, GEN_DIAGRAM_PREFERENCES__FONT_COLOR);
 		createEReference(genDiagramPreferencesEClass, GEN_DIAGRAM_PREFERENCES__FILL_COLOR);
 		createEReference(genDiagramPreferencesEClass, GEN_DIAGRAM_PREFERENCES__LINE_COLOR);
 		createEReference(genDiagramPreferencesEClass, GEN_DIAGRAM_PREFERENCES__NOTE_FILL_COLOR);
 		createEReference(genDiagramPreferencesEClass, GEN_DIAGRAM_PREFERENCES__NOTE_LINE_COLOR);
 		createEAttribute(genDiagramPreferencesEClass, GEN_DIAGRAM_PREFERENCES__SHOW_CONNECTION_HANDLES);
 		createEAttribute(genDiagramPreferencesEClass, GEN_DIAGRAM_PREFERENCES__SHOW_POPUP_BARS);
 		createEAttribute(genDiagramPreferencesEClass, GEN_DIAGRAM_PREFERENCES__PROMPT_ON_DEL_FROM_MODEL);
 		createEAttribute(genDiagramPreferencesEClass, GEN_DIAGRAM_PREFERENCES__PROMPT_ON_DEL_FROM_DIAGRAM);
 		createEAttribute(genDiagramPreferencesEClass, GEN_DIAGRAM_PREFERENCES__ENABLE_ANIMATED_LAYOUT);
 		createEAttribute(genDiagramPreferencesEClass, GEN_DIAGRAM_PREFERENCES__ENABLE_ANIMATED_ZOOM);
 		createEAttribute(genDiagramPreferencesEClass, GEN_DIAGRAM_PREFERENCES__ENABLE_ANTI_ALIAS);
 		createEAttribute(genDiagramPreferencesEClass, GEN_DIAGRAM_PREFERENCES__SHOW_GRID);
 		createEAttribute(genDiagramPreferencesEClass, GEN_DIAGRAM_PREFERENCES__SHOW_RULERS);
 		createEAttribute(genDiagramPreferencesEClass, GEN_DIAGRAM_PREFERENCES__SNAP_TO_GRID);
 		createEAttribute(genDiagramPreferencesEClass, GEN_DIAGRAM_PREFERENCES__RULER_UNITS);
 		createEAttribute(genDiagramPreferencesEClass, GEN_DIAGRAM_PREFERENCES__GRID_SPACING);
 
 		genFontEClass = createEClass(GEN_FONT);
 
 		genStandardFontEClass = createEClass(GEN_STANDARD_FONT);
 		createEAttribute(genStandardFontEClass, GEN_STANDARD_FONT__NAME);
 
 		genCustomFontEClass = createEClass(GEN_CUSTOM_FONT);
 		createEAttribute(genCustomFontEClass, GEN_CUSTOM_FONT__NAME);
 		createEAttribute(genCustomFontEClass, GEN_CUSTOM_FONT__HEIGHT);
 		createEAttribute(genCustomFontEClass, GEN_CUSTOM_FONT__STYLE);
 
 		genColorEClass = createEClass(GEN_COLOR);
 
 		genRGBColorEClass = createEClass(GEN_RGB_COLOR);
 		createEAttribute(genRGBColorEClass, GEN_RGB_COLOR__RED);
 		createEAttribute(genRGBColorEClass, GEN_RGB_COLOR__GREEN);
 		createEAttribute(genRGBColorEClass, GEN_RGB_COLOR__BLUE);
 
 		genConstantColorEClass = createEClass(GEN_CONSTANT_COLOR);
 		createEAttribute(genConstantColorEClass, GEN_CONSTANT_COLOR__NAME);
 
 		batchValidationEClass = createEClass(BATCH_VALIDATION);
 		createEAttribute(batchValidationEClass, BATCH_VALIDATION__VALIDATION_PROVIDER_CLASS_NAME);
 		createEAttribute(batchValidationEClass, BATCH_VALIDATION__VALIDATION_PROVIDER_PRIORITY);
 		createEAttribute(batchValidationEClass, BATCH_VALIDATION__MARKER_NAVIGATION_PROVIDER_CLASS_NAME);
 		createEAttribute(batchValidationEClass, BATCH_VALIDATION__MARKER_NAVIGATION_PROVIDER_PRIORITY);
 		createEAttribute(batchValidationEClass, BATCH_VALIDATION__VALIDATION_ENABLED);
 		createEAttribute(batchValidationEClass, BATCH_VALIDATION__METRIC_PROVIDER_CLASS_NAME);
 		createEAttribute(batchValidationEClass, BATCH_VALIDATION__METRIC_PROVIDER_PRIORITY);
 		createEAttribute(batchValidationEClass, BATCH_VALIDATION__VALIDATION_DECORATOR_PROVIDER_CLASS_NAME);
 		createEAttribute(batchValidationEClass, BATCH_VALIDATION__VALIDATION_DECORATORS);
 		createEAttribute(batchValidationEClass, BATCH_VALIDATION__VALIDATION_DECORATOR_PROVIDER_PRIORITY);
 		createEAttribute(batchValidationEClass, BATCH_VALIDATION__LIVE_VALIDATION_UI_FEEDBACK);
 
 		providerClassNamesEClass = createEClass(PROVIDER_CLASS_NAMES);
 		createEAttribute(providerClassNamesEClass, PROVIDER_CLASS_NAMES__ELEMENT_TYPES_CLASS_NAME);
 		createEAttribute(providerClassNamesEClass, PROVIDER_CLASS_NAMES__NOTATION_VIEW_PROVIDER_CLASS_NAME);
 		createEAttribute(providerClassNamesEClass, PROVIDER_CLASS_NAMES__NOTATION_VIEW_PROVIDER_PRIORITY);
 		createEAttribute(providerClassNamesEClass, PROVIDER_CLASS_NAMES__EDIT_PART_PROVIDER_CLASS_NAME);
 		createEAttribute(providerClassNamesEClass, PROVIDER_CLASS_NAMES__EDIT_PART_PROVIDER_PRIORITY);
 		createEAttribute(providerClassNamesEClass, PROVIDER_CLASS_NAMES__MODELING_ASSISTANT_PROVIDER_CLASS_NAME);
 		createEAttribute(providerClassNamesEClass, PROVIDER_CLASS_NAMES__MODELING_ASSISTANT_PROVIDER_PRIORITY);
 		createEAttribute(providerClassNamesEClass, PROVIDER_CLASS_NAMES__ICON_PROVIDER_CLASS_NAME);
 		createEAttribute(providerClassNamesEClass, PROVIDER_CLASS_NAMES__ICON_PROVIDER_PRIORITY);
 		createEAttribute(providerClassNamesEClass, PROVIDER_CLASS_NAMES__PARSER_PROVIDER_CLASS_NAME);
 		createEAttribute(providerClassNamesEClass, PROVIDER_CLASS_NAMES__PARSER_PROVIDER_PRIORITY);
 		createEAttribute(providerClassNamesEClass, PROVIDER_CLASS_NAMES__CONTRIBUTION_ITEM_PROVIDER_CLASS_NAME);
 
 		shortcutsEClass = createEClass(SHORTCUTS);
 		createEAttribute(shortcutsEClass, SHORTCUTS__SHORTCUTS_DECORATOR_PROVIDER_CLASS_NAME);
 		createEAttribute(shortcutsEClass, SHORTCUTS__SHORTCUTS_DECORATOR_PROVIDER_PRIORITY);
 		createEAttribute(shortcutsEClass, SHORTCUTS__CREATE_SHORTCUT_ACTION_CLASS_NAME);
 		createEAttribute(shortcutsEClass, SHORTCUTS__CREATE_SHORTCUT_DECORATIONS_COMMAND_CLASS_NAME);
 		createEAttribute(shortcutsEClass, SHORTCUTS__SHORTCUT_PROPERTY_TESTER_CLASS_NAME);
 		createEAttribute(shortcutsEClass, SHORTCUTS__CONTAINS_SHORTCUTS_TO);
 		createEAttribute(shortcutsEClass, SHORTCUTS__SHORTCUTS_PROVIDED_FOR);
 
 		packageNamesEClass = createEClass(PACKAGE_NAMES);
 		createEAttribute(packageNamesEClass, PACKAGE_NAMES__EDIT_COMMANDS_PACKAGE_NAME);
 		createEAttribute(packageNamesEClass, PACKAGE_NAMES__EDIT_HELPERS_PACKAGE_NAME);
 		createEAttribute(packageNamesEClass, PACKAGE_NAMES__EDIT_PARTS_PACKAGE_NAME);
 		createEAttribute(packageNamesEClass, PACKAGE_NAMES__EDIT_POLICIES_PACKAGE_NAME);
 		createEAttribute(packageNamesEClass, PACKAGE_NAMES__PREFERENCES_PACKAGE_NAME);
 		createEAttribute(packageNamesEClass, PACKAGE_NAMES__PROVIDERS_PACKAGE_NAME);
 		createEAttribute(packageNamesEClass, PACKAGE_NAMES__PARSERS_PACKAGE_NAME);
 		createEAttribute(packageNamesEClass, PACKAGE_NAMES__NOTATION_VIEW_FACTORIES_PACKAGE_NAME);
 
 		linkConstraintsEClass = createEClass(LINK_CONSTRAINTS);
 
 		editorCandiesEClass = createEClass(EDITOR_CANDIES);
 		createEAttribute(editorCandiesEClass, EDITOR_CANDIES__CREATION_WIZARD_CLASS_NAME);
 		createEAttribute(editorCandiesEClass, EDITOR_CANDIES__CREATION_WIZARD_PAGE_CLASS_NAME);
 		createEAttribute(editorCandiesEClass, EDITOR_CANDIES__CREATION_WIZARD_ICON_PATH);
 		createEAttribute(editorCandiesEClass, EDITOR_CANDIES__CREATION_WIZARD_ICON_PATH_X);
 		createEAttribute(editorCandiesEClass, EDITOR_CANDIES__CREATION_WIZARD_CATEGORY_ID);
 		createEAttribute(editorCandiesEClass, EDITOR_CANDIES__DIAGRAM_EDITOR_UTIL_CLASS_NAME);
 		createEAttribute(editorCandiesEClass, EDITOR_CANDIES__DOCUMENT_PROVIDER_CLASS_NAME);
 		createEAttribute(editorCandiesEClass, EDITOR_CANDIES__INIT_DIAGRAM_FILE_ACTION_CLASS_NAME);
 		createEAttribute(editorCandiesEClass, EDITOR_CANDIES__NEW_DIAGRAM_FILE_WIZARD_CLASS_NAME);
 		createEAttribute(editorCandiesEClass, EDITOR_CANDIES__DIAGRAM_CONTENT_INITIALIZER_CLASS_NAME);
 		createEAttribute(editorCandiesEClass, EDITOR_CANDIES__MATCHING_STRATEGY_CLASS_NAME);
 		createEAttribute(editorCandiesEClass, EDITOR_CANDIES__VISUAL_ID_REGISTRY_CLASS_NAME);
 		createEAttribute(editorCandiesEClass, EDITOR_CANDIES__ELEMENT_CHOOSER_CLASS_NAME);
 		createEAttribute(editorCandiesEClass, EDITOR_CANDIES__LOAD_RESOURCE_ACTION_CLASS_NAME);
 		createEAttribute(editorCandiesEClass, EDITOR_CANDIES__EDITING_DOMAIN_ID);
 
 		editPartCandiesEClass = createEClass(EDIT_PART_CANDIES);
 		createEAttribute(editPartCandiesEClass, EDIT_PART_CANDIES__REORIENT_CONNECTION_VIEW_COMMAND_CLASS_NAME);
 		createEAttribute(editPartCandiesEClass, EDIT_PART_CANDIES__BASE_EDIT_HELPER_CLASS_NAME);
 		createEAttribute(editPartCandiesEClass, EDIT_PART_CANDIES__EDIT_PART_FACTORY_CLASS_NAME);
 		createEAttribute(editPartCandiesEClass, EDIT_PART_CANDIES__BASE_EXTERNAL_NODE_LABEL_EDIT_PART_CLASS_NAME);
 		createEAttribute(editPartCandiesEClass, EDIT_PART_CANDIES__BASE_ITEM_SEMANTIC_EDIT_POLICY_CLASS_NAME);
 		createEAttribute(editPartCandiesEClass, EDIT_PART_CANDIES__BASE_GRAPHICAL_NODE_EDIT_POLICY_CLASS_NAME);
 		createEAttribute(editPartCandiesEClass, EDIT_PART_CANDIES__TEXT_SELECTION_EDIT_POLICY_CLASS_NAME);
 		createEAttribute(editPartCandiesEClass, EDIT_PART_CANDIES__TEXT_NON_RESIZABLE_EDIT_POLICY_CLASS_NAME);
 
 		measurementUnitEClass = createEClass(MEASUREMENT_UNIT);
 		createEAttribute(measurementUnitEClass, MEASUREMENT_UNIT__UNITS);
 
 		genDiagramUpdaterEClass = createEClass(GEN_DIAGRAM_UPDATER);
 		createEReference(genDiagramUpdaterEClass, GEN_DIAGRAM_UPDATER__EDITOR_GEN);
 		createEAttribute(genDiagramUpdaterEClass, GEN_DIAGRAM_UPDATER__DIAGRAM_UPDATER_CLASS_NAME);
 		createEAttribute(genDiagramUpdaterEClass, GEN_DIAGRAM_UPDATER__NODE_DESCRIPTOR_CLASS_NAME);
 		createEAttribute(genDiagramUpdaterEClass, GEN_DIAGRAM_UPDATER__LINK_DESCRIPTOR_CLASS_NAME);
 		createEAttribute(genDiagramUpdaterEClass, GEN_DIAGRAM_UPDATER__UPDATE_COMMAND_CLASS_NAME);
 		createEAttribute(genDiagramUpdaterEClass, GEN_DIAGRAM_UPDATER__UPDATE_COMMAND_ID);
 
 		genPluginEClass = createEClass(GEN_PLUGIN);
 		createEReference(genPluginEClass, GEN_PLUGIN__EDITOR_GEN);
 		createEAttribute(genPluginEClass, GEN_PLUGIN__ID);
 		createEAttribute(genPluginEClass, GEN_PLUGIN__NAME);
 		createEAttribute(genPluginEClass, GEN_PLUGIN__PROVIDER);
 		createEAttribute(genPluginEClass, GEN_PLUGIN__VERSION);
 		createEAttribute(genPluginEClass, GEN_PLUGIN__PRINTING_ENABLED);
 		createEAttribute(genPluginEClass, GEN_PLUGIN__REQUIRED_PLUGINS);
 		createEAttribute(genPluginEClass, GEN_PLUGIN__ACTIVATOR_CLASS_NAME);
 
 		genCommonBaseEClass = createEClass(GEN_COMMON_BASE);
 		createEReference(genCommonBaseEClass, GEN_COMMON_BASE__DIAGRAM_RUN_TIME_CLASS);
 		createEAttribute(genCommonBaseEClass, GEN_COMMON_BASE__VISUAL_ID);
 		createEReference(genCommonBaseEClass, GEN_COMMON_BASE__ELEMENT_TYPE);
 		createEAttribute(genCommonBaseEClass, GEN_COMMON_BASE__EDIT_PART_CLASS_NAME);
 		createEAttribute(genCommonBaseEClass, GEN_COMMON_BASE__ITEM_SEMANTIC_EDIT_POLICY_CLASS_NAME);
 		createEAttribute(genCommonBaseEClass, GEN_COMMON_BASE__NOTATION_VIEW_FACTORY_CLASS_NAME);
 		createEReference(genCommonBaseEClass, GEN_COMMON_BASE__VIEWMAP);
 		createEReference(genCommonBaseEClass, GEN_COMMON_BASE__STYLES);
 		createEReference(genCommonBaseEClass, GEN_COMMON_BASE__BEHAVIOUR);
 		createEAttribute(genCommonBaseEClass, GEN_COMMON_BASE__SANS_DOMAIN);
 
 		behaviourEClass = createEClass(BEHAVIOUR);
 		createEReference(behaviourEClass, BEHAVIOUR__SUBJECT);
 
 		customBehaviourEClass = createEClass(CUSTOM_BEHAVIOUR);
 		createEAttribute(customBehaviourEClass, CUSTOM_BEHAVIOUR__KEY);
 		createEAttribute(customBehaviourEClass, CUSTOM_BEHAVIOUR__EDIT_POLICY_QUALIFIED_CLASS_NAME);
 
 		sharedBehaviourEClass = createEClass(SHARED_BEHAVIOUR);
 		createEReference(sharedBehaviourEClass, SHARED_BEHAVIOUR__DELEGATE);
 
 		openDiagramBehaviourEClass = createEClass(OPEN_DIAGRAM_BEHAVIOUR);
 		createEAttribute(openDiagramBehaviourEClass, OPEN_DIAGRAM_BEHAVIOUR__EDIT_POLICY_CLASS_NAME);
 		createEAttribute(openDiagramBehaviourEClass, OPEN_DIAGRAM_BEHAVIOUR__DIAGRAM_KIND);
 		createEAttribute(openDiagramBehaviourEClass, OPEN_DIAGRAM_BEHAVIOUR__EDITOR_ID);
 		createEAttribute(openDiagramBehaviourEClass, OPEN_DIAGRAM_BEHAVIOUR__OPEN_AS_ECLIPSE_EDITOR);
 
 		genContainerBaseEClass = createEClass(GEN_CONTAINER_BASE);
 		createEReference(genContainerBaseEClass, GEN_CONTAINER_BASE__CONTAINED_NODES);
 		createEAttribute(genContainerBaseEClass, GEN_CONTAINER_BASE__CANONICAL_EDIT_POLICY_CLASS_NAME);
 
 		genChildContainerEClass = createEClass(GEN_CHILD_CONTAINER);
 		createEReference(genChildContainerEClass, GEN_CHILD_CONTAINER__CHILD_NODES);
 
 		genNodeEClass = createEClass(GEN_NODE);
 		createEReference(genNodeEClass, GEN_NODE__MODEL_FACET);
 		createEReference(genNodeEClass, GEN_NODE__LABELS);
 		createEReference(genNodeEClass, GEN_NODE__COMPARTMENTS);
 		createEAttribute(genNodeEClass, GEN_NODE__PRIMARY_DRAG_EDIT_POLICY_QUALIFIED_CLASS_NAME);
 		createEAttribute(genNodeEClass, GEN_NODE__GRAPHICAL_NODE_EDIT_POLICY_CLASS_NAME);
 		createEAttribute(genNodeEClass, GEN_NODE__CREATE_COMMAND_CLASS_NAME);
 		createEReference(genNodeEClass, GEN_NODE__REORIENTED_INCOMING_LINKS);
 
 		genTopLevelNodeEClass = createEClass(GEN_TOP_LEVEL_NODE);
 		createEReference(genTopLevelNodeEClass, GEN_TOP_LEVEL_NODE__DIAGRAM);
 
 		genChildNodeEClass = createEClass(GEN_CHILD_NODE);
 		createEReference(genChildNodeEClass, GEN_CHILD_NODE__DIAGRAM);
 		createEReference(genChildNodeEClass, GEN_CHILD_NODE__CONTAINERS);
 
 		genChildSideAffixedNodeEClass = createEClass(GEN_CHILD_SIDE_AFFIXED_NODE);
 		createEAttribute(genChildSideAffixedNodeEClass, GEN_CHILD_SIDE_AFFIXED_NODE__PREFERRED_SIDE_NAME);
 
 		genChildLabelNodeEClass = createEClass(GEN_CHILD_LABEL_NODE);
 		createEAttribute(genChildLabelNodeEClass, GEN_CHILD_LABEL_NODE__LABEL_READ_ONLY);
 		createEAttribute(genChildLabelNodeEClass, GEN_CHILD_LABEL_NODE__LABEL_ELEMENT_ICON);
 		createEReference(genChildLabelNodeEClass, GEN_CHILD_LABEL_NODE__LABEL_MODEL_FACET);
 
 		genCompartmentEClass = createEClass(GEN_COMPARTMENT);
 		createEAttribute(genCompartmentEClass, GEN_COMPARTMENT__TITLE);
 		createEAttribute(genCompartmentEClass, GEN_COMPARTMENT__CAN_COLLAPSE);
 		createEAttribute(genCompartmentEClass, GEN_COMPARTMENT__HIDE_IF_EMPTY);
 		createEAttribute(genCompartmentEClass, GEN_COMPARTMENT__NEEDS_TITLE);
 		createEReference(genCompartmentEClass, GEN_COMPARTMENT__DIAGRAM);
 		createEReference(genCompartmentEClass, GEN_COMPARTMENT__NODE);
 		createEAttribute(genCompartmentEClass, GEN_COMPARTMENT__LIST_LAYOUT);
 
 		genLinkEClass = createEClass(GEN_LINK);
 		createEReference(genLinkEClass, GEN_LINK__DIAGRAM);
 		createEReference(genLinkEClass, GEN_LINK__MODEL_FACET);
 		createEReference(genLinkEClass, GEN_LINK__LABELS);
 		createEAttribute(genLinkEClass, GEN_LINK__OUTGOING_CREATION_ALLOWED);
 		createEAttribute(genLinkEClass, GEN_LINK__INCOMING_CREATION_ALLOWED);
 		createEAttribute(genLinkEClass, GEN_LINK__VIEW_DIRECTION_ALIGNED_WITH_MODEL);
 		createEReference(genLinkEClass, GEN_LINK__CREATION_CONSTRAINTS);
 		createEAttribute(genLinkEClass, GEN_LINK__CREATE_COMMAND_CLASS_NAME);
 		createEAttribute(genLinkEClass, GEN_LINK__REORIENT_COMMAND_CLASS_NAME);
 
 		genLabelEClass = createEClass(GEN_LABEL);
 		createEAttribute(genLabelEClass, GEN_LABEL__READ_ONLY);
 		createEAttribute(genLabelEClass, GEN_LABEL__ELEMENT_ICON);
 		createEReference(genLabelEClass, GEN_LABEL__MODEL_FACET);
 
 		genNodeLabelEClass = createEClass(GEN_NODE_LABEL);
 		createEReference(genNodeLabelEClass, GEN_NODE_LABEL__NODE);
 
 		genExternalNodeLabelEClass = createEClass(GEN_EXTERNAL_NODE_LABEL);
 
 		genLinkLabelEClass = createEClass(GEN_LINK_LABEL);
 		createEReference(genLinkLabelEClass, GEN_LINK_LABEL__LINK);
 		createEAttribute(genLinkLabelEClass, GEN_LINK_LABEL__ALIGNMENT);
 
 		elementTypeEClass = createEClass(ELEMENT_TYPE);
 		createEReference(elementTypeEClass, ELEMENT_TYPE__DIAGRAM_ELEMENT);
 		createEAttribute(elementTypeEClass, ELEMENT_TYPE__UNIQUE_IDENTIFIER);
 		createEAttribute(elementTypeEClass, ELEMENT_TYPE__DISPLAY_NAME);
 		createEAttribute(elementTypeEClass, ELEMENT_TYPE__DEFINED_EXTERNALLY);
 
 		metamodelTypeEClass = createEClass(METAMODEL_TYPE);
 		createEAttribute(metamodelTypeEClass, METAMODEL_TYPE__EDIT_HELPER_CLASS_NAME);
 
 		specializationTypeEClass = createEClass(SPECIALIZATION_TYPE);
 		createEReference(specializationTypeEClass, SPECIALIZATION_TYPE__METAMODEL_TYPE);
 		createEAttribute(specializationTypeEClass, SPECIALIZATION_TYPE__EDIT_HELPER_ADVICE_CLASS_NAME);
 
 		notationTypeEClass = createEClass(NOTATION_TYPE);
 
 		modelFacetEClass = createEClass(MODEL_FACET);
 
 		linkModelFacetEClass = createEClass(LINK_MODEL_FACET);
 
 		labelModelFacetEClass = createEClass(LABEL_MODEL_FACET);
 
 		typeModelFacetEClass = createEClass(TYPE_MODEL_FACET);
 		createEReference(typeModelFacetEClass, TYPE_MODEL_FACET__META_CLASS);
 		createEReference(typeModelFacetEClass, TYPE_MODEL_FACET__CONTAINMENT_META_FEATURE);
 		createEReference(typeModelFacetEClass, TYPE_MODEL_FACET__CHILD_META_FEATURE);
 		createEReference(typeModelFacetEClass, TYPE_MODEL_FACET__MODEL_ELEMENT_SELECTOR);
 		createEReference(typeModelFacetEClass, TYPE_MODEL_FACET__MODEL_ELEMENT_INITIALIZER);
 
 		typeLinkModelFacetEClass = createEClass(TYPE_LINK_MODEL_FACET);
 		createEReference(typeLinkModelFacetEClass, TYPE_LINK_MODEL_FACET__SOURCE_META_FEATURE);
 		createEReference(typeLinkModelFacetEClass, TYPE_LINK_MODEL_FACET__TARGET_META_FEATURE);
 
 		featureLinkModelFacetEClass = createEClass(FEATURE_LINK_MODEL_FACET);
 		createEReference(featureLinkModelFacetEClass, FEATURE_LINK_MODEL_FACET__META_FEATURE);
 
 		featureLabelModelFacetEClass = createEClass(FEATURE_LABEL_MODEL_FACET);
 		createEReference(featureLabelModelFacetEClass, FEATURE_LABEL_MODEL_FACET__META_FEATURES);
 		createEAttribute(featureLabelModelFacetEClass, FEATURE_LABEL_MODEL_FACET__VIEW_PATTERN);
 		createEAttribute(featureLabelModelFacetEClass, FEATURE_LABEL_MODEL_FACET__EDITOR_PATTERN);
 		createEAttribute(featureLabelModelFacetEClass, FEATURE_LABEL_MODEL_FACET__EDIT_PATTERN);
 		createEAttribute(featureLabelModelFacetEClass, FEATURE_LABEL_MODEL_FACET__VIEW_METHOD);
 		createEAttribute(featureLabelModelFacetEClass, FEATURE_LABEL_MODEL_FACET__EDIT_METHOD);
 
 		designLabelModelFacetEClass = createEClass(DESIGN_LABEL_MODEL_FACET);
 
 		attributesEClass = createEClass(ATTRIBUTES);
 
 		colorAttributesEClass = createEClass(COLOR_ATTRIBUTES);
 		createEAttribute(colorAttributesEClass, COLOR_ATTRIBUTES__FOREGROUND_COLOR);
 		createEAttribute(colorAttributesEClass, COLOR_ATTRIBUTES__BACKGROUND_COLOR);
 
 		styleAttributesEClass = createEClass(STYLE_ATTRIBUTES);
 		createEAttribute(styleAttributesEClass, STYLE_ATTRIBUTES__FIXED_FONT);
 		createEAttribute(styleAttributesEClass, STYLE_ATTRIBUTES__FIXED_FOREGROUND);
 		createEAttribute(styleAttributesEClass, STYLE_ATTRIBUTES__FIXED_BACKGROUND);
 
 		resizeConstraintsEClass = createEClass(RESIZE_CONSTRAINTS);
 		createEAttribute(resizeConstraintsEClass, RESIZE_CONSTRAINTS__RESIZE_HANDLES);
 		createEAttribute(resizeConstraintsEClass, RESIZE_CONSTRAINTS__NON_RESIZE_HANDLES);
 		createEAttribute(resizeConstraintsEClass, RESIZE_CONSTRAINTS__RESIZE_HANDLE_NAMES);
 		createEAttribute(resizeConstraintsEClass, RESIZE_CONSTRAINTS__NON_RESIZE_HANDLE_NAMES);
 
 		defaultSizeAttributesEClass = createEClass(DEFAULT_SIZE_ATTRIBUTES);
 		createEAttribute(defaultSizeAttributesEClass, DEFAULT_SIZE_ATTRIBUTES__WIDTH);
 		createEAttribute(defaultSizeAttributesEClass, DEFAULT_SIZE_ATTRIBUTES__HEIGHT);
 
 		labelOffsetAttributesEClass = createEClass(LABEL_OFFSET_ATTRIBUTES);
 		createEAttribute(labelOffsetAttributesEClass, LABEL_OFFSET_ATTRIBUTES__X);
 		createEAttribute(labelOffsetAttributesEClass, LABEL_OFFSET_ATTRIBUTES__Y);
 
 		viewmapEClass = createEClass(VIEWMAP);
 		createEReference(viewmapEClass, VIEWMAP__ATTRIBUTES);
 		createEAttribute(viewmapEClass, VIEWMAP__REQUIRED_PLUGIN_IDS);
 		createEAttribute(viewmapEClass, VIEWMAP__LAYOUT_TYPE);
 
 		figureViewmapEClass = createEClass(FIGURE_VIEWMAP);
 		createEAttribute(figureViewmapEClass, FIGURE_VIEWMAP__FIGURE_QUALIFIED_CLASS_NAME);
 
 		snippetViewmapEClass = createEClass(SNIPPET_VIEWMAP);
 		createEAttribute(snippetViewmapEClass, SNIPPET_VIEWMAP__BODY);
 
 		innerClassViewmapEClass = createEClass(INNER_CLASS_VIEWMAP);
 		createEAttribute(innerClassViewmapEClass, INNER_CLASS_VIEWMAP__CLASS_NAME);
 		createEAttribute(innerClassViewmapEClass, INNER_CLASS_VIEWMAP__CLASS_BODY);
 
 		parentAssignedViewmapEClass = createEClass(PARENT_ASSIGNED_VIEWMAP);
 		createEAttribute(parentAssignedViewmapEClass, PARENT_ASSIGNED_VIEWMAP__GETTER_NAME);
 		createEAttribute(parentAssignedViewmapEClass, PARENT_ASSIGNED_VIEWMAP__SETTER_NAME);
 		createEAttribute(parentAssignedViewmapEClass, PARENT_ASSIGNED_VIEWMAP__FIGURE_QUALIFIED_CLASS_NAME);
 
 		valueExpressionEClass = createEClass(VALUE_EXPRESSION);
 		createEAttribute(valueExpressionEClass, VALUE_EXPRESSION__BODY);
 		createEAttribute(valueExpressionEClass, VALUE_EXPRESSION__LANGUAGE);
 		createEAttribute(valueExpressionEClass, VALUE_EXPRESSION__LANG_NAME);
 
 		genConstraintEClass = createEClass(GEN_CONSTRAINT);
 
 		paletteEClass = createEClass(PALETTE);
 		createEReference(paletteEClass, PALETTE__DIAGRAM);
 		createEAttribute(paletteEClass, PALETTE__FLYOUT);
 		createEReference(paletteEClass, PALETTE__GROUPS);
 		createEAttribute(paletteEClass, PALETTE__PACKAGE_NAME);
 		createEAttribute(paletteEClass, PALETTE__FACTORY_CLASS_NAME);
 
 		entryBaseEClass = createEClass(ENTRY_BASE);
 		createEAttribute(entryBaseEClass, ENTRY_BASE__TITLE);
 		createEAttribute(entryBaseEClass, ENTRY_BASE__DESCRIPTION);
 		createEAttribute(entryBaseEClass, ENTRY_BASE__LARGE_ICON_PATH);
 		createEAttribute(entryBaseEClass, ENTRY_BASE__SMALL_ICON_PATH);
 		createEAttribute(entryBaseEClass, ENTRY_BASE__CREATE_METHOD_NAME);
 
 		abstractToolEntryEClass = createEClass(ABSTRACT_TOOL_ENTRY);
 		createEAttribute(abstractToolEntryEClass, ABSTRACT_TOOL_ENTRY__DEFAULT);
 		createEAttribute(abstractToolEntryEClass, ABSTRACT_TOOL_ENTRY__QUALIFIED_TOOL_NAME);
 		createEReference(abstractToolEntryEClass, ABSTRACT_TOOL_ENTRY__PROPERTIES);
 
 		toolEntryEClass = createEClass(TOOL_ENTRY);
 		createEReference(toolEntryEClass, TOOL_ENTRY__GEN_NODES);
 		createEReference(toolEntryEClass, TOOL_ENTRY__GEN_LINKS);
 		createEReference(toolEntryEClass, TOOL_ENTRY__ELEMENTS);
 
 		standardEntryEClass = createEClass(STANDARD_ENTRY);
 		createEAttribute(standardEntryEClass, STANDARD_ENTRY__KIND);
 
 		toolGroupItemEClass = createEClass(TOOL_GROUP_ITEM);
 		createEReference(toolGroupItemEClass, TOOL_GROUP_ITEM__GROUP);
 
 		separatorEClass = createEClass(SEPARATOR);
 
 		toolGroupEClass = createEClass(TOOL_GROUP);
 		createEReference(toolGroupEClass, TOOL_GROUP__PALETTE);
 		createEAttribute(toolGroupEClass, TOOL_GROUP__STACK);
 		createEAttribute(toolGroupEClass, TOOL_GROUP__COLLAPSE);
 		createEReference(toolGroupEClass, TOOL_GROUP__ENTRIES);
 		createEAttribute(toolGroupEClass, TOOL_GROUP__TOOLS_ONLY);
 
 		genElementInitializerEClass = createEClass(GEN_ELEMENT_INITIALIZER);
 		createEReference(genElementInitializerEClass, GEN_ELEMENT_INITIALIZER__TYPE_MODEL_FACET);
 
 		genFeatureSeqInitializerEClass = createEClass(GEN_FEATURE_SEQ_INITIALIZER);
 		createEReference(genFeatureSeqInitializerEClass, GEN_FEATURE_SEQ_INITIALIZER__INITIALIZERS);
 		createEReference(genFeatureSeqInitializerEClass, GEN_FEATURE_SEQ_INITIALIZER__ELEMENT_CLASS);
 		createEReference(genFeatureSeqInitializerEClass, GEN_FEATURE_SEQ_INITIALIZER__CREATING_INITIALIZER);
 
 		genFeatureValueSpecEClass = createEClass(GEN_FEATURE_VALUE_SPEC);
 
 		genReferenceNewElementSpecEClass = createEClass(GEN_REFERENCE_NEW_ELEMENT_SPEC);
 		createEReference(genReferenceNewElementSpecEClass, GEN_REFERENCE_NEW_ELEMENT_SPEC__NEW_ELEMENT_INITIALIZERS);
 
 		genFeatureInitializerEClass = createEClass(GEN_FEATURE_INITIALIZER);
 		createEReference(genFeatureInitializerEClass, GEN_FEATURE_INITIALIZER__FEATURE);
 		createEReference(genFeatureInitializerEClass, GEN_FEATURE_INITIALIZER__FEATURE_SEQ_INITIALIZER);
 
 		genLinkConstraintsEClass = createEClass(GEN_LINK_CONSTRAINTS);
 		createEReference(genLinkConstraintsEClass, GEN_LINK_CONSTRAINTS__LINK);
 		createEReference(genLinkConstraintsEClass, GEN_LINK_CONSTRAINTS__SOURCE_END);
 		createEReference(genLinkConstraintsEClass, GEN_LINK_CONSTRAINTS__TARGET_END);
 
 		genAuditRootEClass = createEClass(GEN_AUDIT_ROOT);
 		createEReference(genAuditRootEClass, GEN_AUDIT_ROOT__EDITOR_GEN);
 		createEReference(genAuditRootEClass, GEN_AUDIT_ROOT__CATEGORIES);
 		createEReference(genAuditRootEClass, GEN_AUDIT_ROOT__RULES);
 
 		genAuditContainerEClass = createEClass(GEN_AUDIT_CONTAINER);
 		createEReference(genAuditContainerEClass, GEN_AUDIT_CONTAINER__ROOT);
 		createEAttribute(genAuditContainerEClass, GEN_AUDIT_CONTAINER__ID);
 		createEAttribute(genAuditContainerEClass, GEN_AUDIT_CONTAINER__NAME);
 		createEAttribute(genAuditContainerEClass, GEN_AUDIT_CONTAINER__DESCRIPTION);
 		createEReference(genAuditContainerEClass, GEN_AUDIT_CONTAINER__PATH);
 		createEReference(genAuditContainerEClass, GEN_AUDIT_CONTAINER__AUDITS);
 
 		genRuleBaseEClass = createEClass(GEN_RULE_BASE);
 		createEAttribute(genRuleBaseEClass, GEN_RULE_BASE__NAME);
 		createEAttribute(genRuleBaseEClass, GEN_RULE_BASE__DESCRIPTION);
 
 		genAuditRuleEClass = createEClass(GEN_AUDIT_RULE);
 		createEReference(genAuditRuleEClass, GEN_AUDIT_RULE__ROOT);
 		createEAttribute(genAuditRuleEClass, GEN_AUDIT_RULE__CONTEXT_SELECTOR_LOCAL_CLASS_NAME);
 		createEAttribute(genAuditRuleEClass, GEN_AUDIT_RULE__ID);
 		createEReference(genAuditRuleEClass, GEN_AUDIT_RULE__RULE);
 		createEReference(genAuditRuleEClass, GEN_AUDIT_RULE__TARGET);
 		createEAttribute(genAuditRuleEClass, GEN_AUDIT_RULE__MESSAGE);
 		createEAttribute(genAuditRuleEClass, GEN_AUDIT_RULE__SEVERITY);
 		createEAttribute(genAuditRuleEClass, GEN_AUDIT_RULE__USE_IN_LIVE_MODE);
 		createEAttribute(genAuditRuleEClass, GEN_AUDIT_RULE__REQUIRES_CONSTRAINT_ADAPTER);
 		createEReference(genAuditRuleEClass, GEN_AUDIT_RULE__CATEGORY);
 
 		genRuleTargetEClass = createEClass(GEN_RULE_TARGET);
 
 		genDomainElementTargetEClass = createEClass(GEN_DOMAIN_ELEMENT_TARGET);
 		createEReference(genDomainElementTargetEClass, GEN_DOMAIN_ELEMENT_TARGET__ELEMENT);
 
 		genDiagramElementTargetEClass = createEClass(GEN_DIAGRAM_ELEMENT_TARGET);
 		createEReference(genDiagramElementTargetEClass, GEN_DIAGRAM_ELEMENT_TARGET__ELEMENT);
 
 		genDomainAttributeTargetEClass = createEClass(GEN_DOMAIN_ATTRIBUTE_TARGET);
 		createEReference(genDomainAttributeTargetEClass, GEN_DOMAIN_ATTRIBUTE_TARGET__ATTRIBUTE);
 		createEAttribute(genDomainAttributeTargetEClass, GEN_DOMAIN_ATTRIBUTE_TARGET__NULL_AS_ERROR);
 
 		genNotationElementTargetEClass = createEClass(GEN_NOTATION_ELEMENT_TARGET);
 		createEReference(genNotationElementTargetEClass, GEN_NOTATION_ELEMENT_TARGET__ELEMENT);
 
 		genMetricContainerEClass = createEClass(GEN_METRIC_CONTAINER);
 		createEReference(genMetricContainerEClass, GEN_METRIC_CONTAINER__EDITOR_GEN);
 		createEReference(genMetricContainerEClass, GEN_METRIC_CONTAINER__METRICS);
 
 		genMetricRuleEClass = createEClass(GEN_METRIC_RULE);
 		createEAttribute(genMetricRuleEClass, GEN_METRIC_RULE__KEY);
 		createEReference(genMetricRuleEClass, GEN_METRIC_RULE__RULE);
 		createEReference(genMetricRuleEClass, GEN_METRIC_RULE__TARGET);
 		createEAttribute(genMetricRuleEClass, GEN_METRIC_RULE__LOW_LIMIT);
 		createEAttribute(genMetricRuleEClass, GEN_METRIC_RULE__HIGH_LIMIT);
 		createEReference(genMetricRuleEClass, GEN_METRIC_RULE__CONTAINER);
 
 		genAuditedMetricTargetEClass = createEClass(GEN_AUDITED_METRIC_TARGET);
 		createEReference(genAuditedMetricTargetEClass, GEN_AUDITED_METRIC_TARGET__METRIC);
 		createEReference(genAuditedMetricTargetEClass, GEN_AUDITED_METRIC_TARGET__METRIC_VALUE_CONTEXT);
 
 		genAuditableEClass = createEClass(GEN_AUDITABLE);
 
 		genMeasurableEClass = createEClass(GEN_MEASURABLE);
 
 		genExpressionProviderContainerEClass = createEClass(GEN_EXPRESSION_PROVIDER_CONTAINER);
 		createEAttribute(genExpressionProviderContainerEClass, GEN_EXPRESSION_PROVIDER_CONTAINER__EXPRESSIONS_PACKAGE_NAME);
 		createEAttribute(genExpressionProviderContainerEClass, GEN_EXPRESSION_PROVIDER_CONTAINER__ABSTRACT_EXPRESSION_CLASS_NAME);
 		createEReference(genExpressionProviderContainerEClass, GEN_EXPRESSION_PROVIDER_CONTAINER__PROVIDERS);
 		createEReference(genExpressionProviderContainerEClass, GEN_EXPRESSION_PROVIDER_CONTAINER__EDITOR_GEN);
 
 		genExpressionProviderBaseEClass = createEClass(GEN_EXPRESSION_PROVIDER_BASE);
 		createEReference(genExpressionProviderBaseEClass, GEN_EXPRESSION_PROVIDER_BASE__EXPRESSIONS);
 		createEReference(genExpressionProviderBaseEClass, GEN_EXPRESSION_PROVIDER_BASE__CONTAINER);
 
 		genJavaExpressionProviderEClass = createEClass(GEN_JAVA_EXPRESSION_PROVIDER);
 
 		genExpressionInterpreterEClass = createEClass(GEN_EXPRESSION_INTERPRETER);
 		createEAttribute(genExpressionInterpreterEClass, GEN_EXPRESSION_INTERPRETER__LANGUAGE);
 		createEAttribute(genExpressionInterpreterEClass, GEN_EXPRESSION_INTERPRETER__CLASS_NAME);
 
 		genDomainModelNavigatorEClass = createEClass(GEN_DOMAIN_MODEL_NAVIGATOR);
 		createEAttribute(genDomainModelNavigatorEClass, GEN_DOMAIN_MODEL_NAVIGATOR__GENERATE_DOMAIN_MODEL_NAVIGATOR);
 		createEAttribute(genDomainModelNavigatorEClass, GEN_DOMAIN_MODEL_NAVIGATOR__DOMAIN_CONTENT_EXTENSION_ID);
 		createEAttribute(genDomainModelNavigatorEClass, GEN_DOMAIN_MODEL_NAVIGATOR__DOMAIN_CONTENT_EXTENSION_NAME);
 		createEAttribute(genDomainModelNavigatorEClass, GEN_DOMAIN_MODEL_NAVIGATOR__DOMAIN_CONTENT_EXTENSION_PRIORITY);
 		createEAttribute(genDomainModelNavigatorEClass, GEN_DOMAIN_MODEL_NAVIGATOR__DOMAIN_CONTENT_PROVIDER_CLASS_NAME);
 		createEAttribute(genDomainModelNavigatorEClass, GEN_DOMAIN_MODEL_NAVIGATOR__DOMAIN_LABEL_PROVIDER_CLASS_NAME);
 		createEAttribute(genDomainModelNavigatorEClass, GEN_DOMAIN_MODEL_NAVIGATOR__DOMAIN_MODEL_ELEMENT_TESTER_CLASS_NAME);
 		createEAttribute(genDomainModelNavigatorEClass, GEN_DOMAIN_MODEL_NAVIGATOR__DOMAIN_NAVIGATOR_ITEM_CLASS_NAME);
 
 		genNavigatorEClass = createEClass(GEN_NAVIGATOR);
 		createEReference(genNavigatorEClass, GEN_NAVIGATOR__EDITOR_GEN);
 		createEAttribute(genNavigatorEClass, GEN_NAVIGATOR__CONTENT_EXTENSION_ID);
 		createEAttribute(genNavigatorEClass, GEN_NAVIGATOR__CONTENT_EXTENSION_NAME);
 		createEAttribute(genNavigatorEClass, GEN_NAVIGATOR__CONTENT_EXTENSION_PRIORITY);
 		createEAttribute(genNavigatorEClass, GEN_NAVIGATOR__LINK_HELPER_EXTENSION_ID);
 		createEAttribute(genNavigatorEClass, GEN_NAVIGATOR__SORTER_EXTENSION_ID);
 		createEAttribute(genNavigatorEClass, GEN_NAVIGATOR__ACTION_PROVIDER_ID);
 		createEAttribute(genNavigatorEClass, GEN_NAVIGATOR__CONTENT_PROVIDER_CLASS_NAME);
 		createEAttribute(genNavigatorEClass, GEN_NAVIGATOR__LABEL_PROVIDER_CLASS_NAME);
 		createEAttribute(genNavigatorEClass, GEN_NAVIGATOR__LINK_HELPER_CLASS_NAME);
 		createEAttribute(genNavigatorEClass, GEN_NAVIGATOR__SORTER_CLASS_NAME);
 		createEAttribute(genNavigatorEClass, GEN_NAVIGATOR__ACTION_PROVIDER_CLASS_NAME);
 		createEAttribute(genNavigatorEClass, GEN_NAVIGATOR__ABSTRACT_NAVIGATOR_ITEM_CLASS_NAME);
 		createEAttribute(genNavigatorEClass, GEN_NAVIGATOR__NAVIGATOR_GROUP_CLASS_NAME);
 		createEAttribute(genNavigatorEClass, GEN_NAVIGATOR__NAVIGATOR_ITEM_CLASS_NAME);
 		createEAttribute(genNavigatorEClass, GEN_NAVIGATOR__URI_INPUT_TESTER_CLASS_NAME);
 		createEAttribute(genNavigatorEClass, GEN_NAVIGATOR__PACKAGE_NAME);
 		createEReference(genNavigatorEClass, GEN_NAVIGATOR__CHILD_REFERENCES);
 
 		genNavigatorChildReferenceEClass = createEClass(GEN_NAVIGATOR_CHILD_REFERENCE);
 		createEReference(genNavigatorChildReferenceEClass, GEN_NAVIGATOR_CHILD_REFERENCE__NAVIGATOR);
 		createEReference(genNavigatorChildReferenceEClass, GEN_NAVIGATOR_CHILD_REFERENCE__PARENT);
 		createEReference(genNavigatorChildReferenceEClass, GEN_NAVIGATOR_CHILD_REFERENCE__CHILD);
 		createEAttribute(genNavigatorChildReferenceEClass, GEN_NAVIGATOR_CHILD_REFERENCE__REFERENCE_TYPE);
 		createEAttribute(genNavigatorChildReferenceEClass, GEN_NAVIGATOR_CHILD_REFERENCE__GROUP_NAME);
 		createEAttribute(genNavigatorChildReferenceEClass, GEN_NAVIGATOR_CHILD_REFERENCE__GROUP_ICON);
 		createEAttribute(genNavigatorChildReferenceEClass, GEN_NAVIGATOR_CHILD_REFERENCE__HIDE_IF_EMPTY);
 
 		genNavigatorPathEClass = createEClass(GEN_NAVIGATOR_PATH);
 		createEReference(genNavigatorPathEClass, GEN_NAVIGATOR_PATH__SEGMENTS);
 
 		genNavigatorPathSegmentEClass = createEClass(GEN_NAVIGATOR_PATH_SEGMENT);
 		createEReference(genNavigatorPathSegmentEClass, GEN_NAVIGATOR_PATH_SEGMENT__PATH);
 		createEReference(genNavigatorPathSegmentEClass, GEN_NAVIGATOR_PATH_SEGMENT__FROM);
 		createEReference(genNavigatorPathSegmentEClass, GEN_NAVIGATOR_PATH_SEGMENT__TO);
 
 		genPropertySheetEClass = createEClass(GEN_PROPERTY_SHEET);
 		createEReference(genPropertySheetEClass, GEN_PROPERTY_SHEET__EDITOR_GEN);
 		createEReference(genPropertySheetEClass, GEN_PROPERTY_SHEET__TABS);
 		createEAttribute(genPropertySheetEClass, GEN_PROPERTY_SHEET__PACKAGE_NAME);
 		createEAttribute(genPropertySheetEClass, GEN_PROPERTY_SHEET__READ_ONLY);
 		createEAttribute(genPropertySheetEClass, GEN_PROPERTY_SHEET__NEEDS_CAPTION);
 		createEAttribute(genPropertySheetEClass, GEN_PROPERTY_SHEET__LABEL_PROVIDER_CLASS_NAME);
 
 		genPropertyTabEClass = createEClass(GEN_PROPERTY_TAB);
 		createEReference(genPropertyTabEClass, GEN_PROPERTY_TAB__SHEET);
 		createEAttribute(genPropertyTabEClass, GEN_PROPERTY_TAB__ID);
 		createEAttribute(genPropertyTabEClass, GEN_PROPERTY_TAB__LABEL);
 
 		genStandardPropertyTabEClass = createEClass(GEN_STANDARD_PROPERTY_TAB);
 
 		genCustomPropertyTabEClass = createEClass(GEN_CUSTOM_PROPERTY_TAB);
 		createEAttribute(genCustomPropertyTabEClass, GEN_CUSTOM_PROPERTY_TAB__CLASS_NAME);
 		createEReference(genCustomPropertyTabEClass, GEN_CUSTOM_PROPERTY_TAB__FILTER);
 
 		genPropertyTabFilterEClass = createEClass(GEN_PROPERTY_TAB_FILTER);
 		createEReference(genPropertyTabFilterEClass, GEN_PROPERTY_TAB_FILTER__TAB);
 
 		typeTabFilterEClass = createEClass(TYPE_TAB_FILTER);
 		createEAttribute(typeTabFilterEClass, TYPE_TAB_FILTER__TYPES);
 		createEAttribute(typeTabFilterEClass, TYPE_TAB_FILTER__GENERATED_TYPES);
 
 		customTabFilterEClass = createEClass(CUSTOM_TAB_FILTER);
 		createEAttribute(customTabFilterEClass, CUSTOM_TAB_FILTER__CLASS_NAME);
 
 		genContributionItemEClass = createEClass(GEN_CONTRIBUTION_ITEM);
 		createEReference(genContributionItemEClass, GEN_CONTRIBUTION_ITEM__OWNER);
 		createEReference(genContributionItemEClass, GEN_CONTRIBUTION_ITEM__APPLICATION);
 
 		genSharedContributionItemEClass = createEClass(GEN_SHARED_CONTRIBUTION_ITEM);
 		createEReference(genSharedContributionItemEClass, GEN_SHARED_CONTRIBUTION_ITEM__ACTUAL_ITEM);
 
 		genGroupMarkerEClass = createEClass(GEN_GROUP_MARKER);
 		createEAttribute(genGroupMarkerEClass, GEN_GROUP_MARKER__GROUP_NAME);
 
 		genSeparatorEClass = createEClass(GEN_SEPARATOR);
 		createEAttribute(genSeparatorEClass, GEN_SEPARATOR__GROUP_NAME);
 
 		genActionFactoryContributionItemEClass = createEClass(GEN_ACTION_FACTORY_CONTRIBUTION_ITEM);
 		createEAttribute(genActionFactoryContributionItemEClass, GEN_ACTION_FACTORY_CONTRIBUTION_ITEM__NAME);
 
 		genContributionManagerEClass = createEClass(GEN_CONTRIBUTION_MANAGER);
 		createEAttribute(genContributionManagerEClass, GEN_CONTRIBUTION_MANAGER__ID);
 		createEReference(genContributionManagerEClass, GEN_CONTRIBUTION_MANAGER__ITEMS);
 
 		genMenuManagerEClass = createEClass(GEN_MENU_MANAGER);
 		createEAttribute(genMenuManagerEClass, GEN_MENU_MANAGER__NAME);
 
 		genToolBarManagerEClass = createEClass(GEN_TOOL_BAR_MANAGER);
 
 		genApplicationEClass = createEClass(GEN_APPLICATION);
 		createEReference(genApplicationEClass, GEN_APPLICATION__EDITOR_GEN);
 		createEAttribute(genApplicationEClass, GEN_APPLICATION__ID);
 		createEAttribute(genApplicationEClass, GEN_APPLICATION__TITLE);
 		createEAttribute(genApplicationEClass, GEN_APPLICATION__PACKAGE_NAME);
 		createEAttribute(genApplicationEClass, GEN_APPLICATION__CLASS_NAME);
 		createEAttribute(genApplicationEClass, GEN_APPLICATION__WORKBENCH_ADVISOR_CLASS_NAME);
 		createEAttribute(genApplicationEClass, GEN_APPLICATION__WORKBENCH_WINDOW_ADVISOR_CLASS_NAME);
 		createEAttribute(genApplicationEClass, GEN_APPLICATION__ACTION_BAR_ADVISOR_CLASS_NAME);
 		createEAttribute(genApplicationEClass, GEN_APPLICATION__PERSPECTIVE_CLASS_NAME);
 		createEAttribute(genApplicationEClass, GEN_APPLICATION__PERSPECTIVE_ID);
 		createEAttribute(genApplicationEClass, GEN_APPLICATION__SUPPORT_FILES);
 		createEReference(genApplicationEClass, GEN_APPLICATION__SHARED_CONTRIBUTION_ITEMS);
 		createEReference(genApplicationEClass, GEN_APPLICATION__MAIN_MENU);
 		createEReference(genApplicationEClass, GEN_APPLICATION__MAIN_TOOL_BAR);
 
 		// Create enums
 		standardPreferencePagesEEnum = createEEnum(STANDARD_PREFERENCE_PAGES);
 		rulerUnitsEEnum = createEEnum(RULER_UNITS);
 		routingEEnum = createEEnum(ROUTING);
 		jFaceFontEEnum = createEEnum(JFACE_FONT);
 		fontStyleEEnum = createEEnum(FONT_STYLE);
 		diagramColorsEEnum = createEEnum(DIAGRAM_COLORS);
 		providerPriorityEEnum = createEEnum(PROVIDER_PRIORITY);
 		linkLabelAlignmentEEnum = createEEnum(LINK_LABEL_ALIGNMENT);
 		labelTextAccessMethodEEnum = createEEnum(LABEL_TEXT_ACCESS_METHOD);
 		viewmapLayoutTypeEEnum = createEEnum(VIEWMAP_LAYOUT_TYPE);
 		standardEntryKindEEnum = createEEnum(STANDARD_ENTRY_KIND);
 		genSeverityEEnum = createEEnum(GEN_SEVERITY);
 		genLanguageEEnum = createEEnum(GEN_LANGUAGE);
 		genNavigatorReferenceTypeEEnum = createEEnum(GEN_NAVIGATOR_REFERENCE_TYPE);
 		generatedTypeEEnum = createEEnum(GENERATED_TYPE);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private boolean isInitialized = false;
 
 	/**
 	 * Complete the initialization of the package and its meta-model.  This
 	 * method is guarded to have no affect on any invocation but its first.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public void initializePackageContentsGen() {
 		if (isInitialized) return;
 		isInitialized = true;
 
 		// Initialize package
 		setName(eNAME);
 		setNsPrefix(eNS_PREFIX);
 		setNsURI(eNS_URI);
 
 		// Obtain other dependent packages
 		GenModelPackage theGenModelPackage = (GenModelPackage)EPackage.Registry.INSTANCE.getEPackage(GenModelPackage.eNS_URI);
 
 		// Create type parameters
 
 		// Set bounds for type parameters
 
 		// Add supertypes to classes
 		genDiagramEClass.getESuperTypes().add(this.getGenContainerBase());
 		genDiagramEClass.getESuperTypes().add(this.getPackageNames());
 		genDiagramEClass.getESuperTypes().add(this.getProviderClassNames());
 		genDiagramEClass.getESuperTypes().add(this.getLinkConstraints());
 		genDiagramEClass.getESuperTypes().add(this.getEditPartCandies());
 		genDiagramEClass.getESuperTypes().add(this.getEditorCandies());
 		genDiagramEClass.getESuperTypes().add(this.getShortcuts());
 		genDiagramEClass.getESuperTypes().add(this.getBatchValidation());
 		genDiagramEClass.getESuperTypes().add(this.getMeasurementUnit());
 		genCustomPreferencePageEClass.getESuperTypes().add(this.getGenPreferencePage());
 		genStandardPreferencePageEClass.getESuperTypes().add(this.getGenPreferencePage());
 		genStandardFontEClass.getESuperTypes().add(this.getGenFont());
 		genCustomFontEClass.getESuperTypes().add(this.getGenFont());
 		genRGBColorEClass.getESuperTypes().add(this.getGenColor());
 		genConstantColorEClass.getESuperTypes().add(this.getGenColor());
 		customBehaviourEClass.getESuperTypes().add(this.getBehaviour());
 		sharedBehaviourEClass.getESuperTypes().add(this.getBehaviour());
 		openDiagramBehaviourEClass.getESuperTypes().add(this.getBehaviour());
 		genContainerBaseEClass.getESuperTypes().add(this.getGenCommonBase());
 		genChildContainerEClass.getESuperTypes().add(this.getGenContainerBase());
 		genNodeEClass.getESuperTypes().add(this.getGenChildContainer());
 		genTopLevelNodeEClass.getESuperTypes().add(this.getGenNode());
 		genChildNodeEClass.getESuperTypes().add(this.getGenNode());
 		genChildSideAffixedNodeEClass.getESuperTypes().add(this.getGenChildNode());
 		genChildLabelNodeEClass.getESuperTypes().add(this.getGenChildNode());
 		genCompartmentEClass.getESuperTypes().add(this.getGenChildContainer());
 		genLinkEClass.getESuperTypes().add(this.getGenCommonBase());
 		genLabelEClass.getESuperTypes().add(this.getGenCommonBase());
 		genNodeLabelEClass.getESuperTypes().add(this.getGenLabel());
 		genExternalNodeLabelEClass.getESuperTypes().add(this.getGenNodeLabel());
 		genLinkLabelEClass.getESuperTypes().add(this.getGenLabel());
 		metamodelTypeEClass.getESuperTypes().add(this.getElementType());
 		specializationTypeEClass.getESuperTypes().add(this.getElementType());
 		notationTypeEClass.getESuperTypes().add(this.getElementType());
 		linkModelFacetEClass.getESuperTypes().add(this.getModelFacet());
 		labelModelFacetEClass.getESuperTypes().add(this.getModelFacet());
 		typeModelFacetEClass.getESuperTypes().add(this.getModelFacet());
 		typeLinkModelFacetEClass.getESuperTypes().add(this.getTypeModelFacet());
 		typeLinkModelFacetEClass.getESuperTypes().add(this.getLinkModelFacet());
 		featureLinkModelFacetEClass.getESuperTypes().add(this.getLinkModelFacet());
 		featureLabelModelFacetEClass.getESuperTypes().add(this.getLabelModelFacet());
 		designLabelModelFacetEClass.getESuperTypes().add(this.getLabelModelFacet());
 		colorAttributesEClass.getESuperTypes().add(this.getAttributes());
 		styleAttributesEClass.getESuperTypes().add(this.getAttributes());
 		resizeConstraintsEClass.getESuperTypes().add(this.getAttributes());
 		defaultSizeAttributesEClass.getESuperTypes().add(this.getAttributes());
 		labelOffsetAttributesEClass.getESuperTypes().add(this.getAttributes());
 		figureViewmapEClass.getESuperTypes().add(this.getViewmap());
 		snippetViewmapEClass.getESuperTypes().add(this.getViewmap());
 		innerClassViewmapEClass.getESuperTypes().add(this.getViewmap());
 		parentAssignedViewmapEClass.getESuperTypes().add(this.getViewmap());
 		genConstraintEClass.getESuperTypes().add(this.getValueExpression());
 		abstractToolEntryEClass.getESuperTypes().add(this.getEntryBase());
 		abstractToolEntryEClass.getESuperTypes().add(this.getToolGroupItem());
 		toolEntryEClass.getESuperTypes().add(this.getAbstractToolEntry());
 		standardEntryEClass.getESuperTypes().add(this.getAbstractToolEntry());
 		separatorEClass.getESuperTypes().add(this.getToolGroupItem());
 		toolGroupEClass.getESuperTypes().add(this.getEntryBase());
 		toolGroupEClass.getESuperTypes().add(this.getToolGroupItem());
 		genFeatureSeqInitializerEClass.getESuperTypes().add(this.getGenElementInitializer());
 		genFeatureValueSpecEClass.getESuperTypes().add(this.getValueExpression());
 		genFeatureValueSpecEClass.getESuperTypes().add(this.getGenFeatureInitializer());
 		genReferenceNewElementSpecEClass.getESuperTypes().add(this.getGenFeatureInitializer());
 		genAuditRuleEClass.getESuperTypes().add(this.getGenRuleBase());
 		genDomainElementTargetEClass.getESuperTypes().add(this.getGenAuditable());
 		genDomainElementTargetEClass.getESuperTypes().add(this.getGenMeasurable());
 		genDiagramElementTargetEClass.getESuperTypes().add(this.getGenAuditable());
 		genDiagramElementTargetEClass.getESuperTypes().add(this.getGenMeasurable());
 		genDomainAttributeTargetEClass.getESuperTypes().add(this.getGenAuditable());
 		genNotationElementTargetEClass.getESuperTypes().add(this.getGenAuditable());
 		genNotationElementTargetEClass.getESuperTypes().add(this.getGenMeasurable());
 		genMetricRuleEClass.getESuperTypes().add(this.getGenRuleBase());
 		genAuditedMetricTargetEClass.getESuperTypes().add(this.getGenAuditable());
 		genAuditableEClass.getESuperTypes().add(this.getGenRuleTarget());
 		genMeasurableEClass.getESuperTypes().add(this.getGenRuleTarget());
 		genJavaExpressionProviderEClass.getESuperTypes().add(this.getGenExpressionProviderBase());
 		genExpressionInterpreterEClass.getESuperTypes().add(this.getGenExpressionProviderBase());
 		genNavigatorEClass.getESuperTypes().add(this.getGenDomainModelNavigator());
 		genStandardPropertyTabEClass.getESuperTypes().add(this.getGenPropertyTab());
 		genCustomPropertyTabEClass.getESuperTypes().add(this.getGenPropertyTab());
 		typeTabFilterEClass.getESuperTypes().add(this.getGenPropertyTabFilter());
 		customTabFilterEClass.getESuperTypes().add(this.getGenPropertyTabFilter());
 		genSharedContributionItemEClass.getESuperTypes().add(this.getGenContributionItem());
 		genGroupMarkerEClass.getESuperTypes().add(this.getGenContributionItem());
 		genSeparatorEClass.getESuperTypes().add(this.getGenContributionItem());
 		genActionFactoryContributionItemEClass.getESuperTypes().add(this.getGenContributionItem());
 		genContributionManagerEClass.getESuperTypes().add(this.getGenContributionItem());
 		genMenuManagerEClass.getESuperTypes().add(this.getGenContributionManager());
 		genToolBarManagerEClass.getESuperTypes().add(this.getGenContributionManager());
 
 		// Initialize classes and features; add operations and parameters
 		initEClass(genEditorGeneratorEClass, GenEditorGenerator.class, "GenEditorGenerator", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
 		initEReference(getGenEditorGenerator_Audits(), this.getGenAuditRoot(), this.getGenAuditRoot_EditorGen(), "audits", null, 0, 1, GenEditorGenerator.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEReference(getGenEditorGenerator_Metrics(), this.getGenMetricContainer(), this.getGenMetricContainer_EditorGen(), "metrics", null, 0, 1, GenEditorGenerator.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEReference(getGenEditorGenerator_Diagram(), this.getGenDiagram(), this.getGenDiagram_EditorGen(), "diagram", null, 1, 1, GenEditorGenerator.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEReference(getGenEditorGenerator_Plugin(), this.getGenPlugin(), this.getGenPlugin_EditorGen(), "plugin", null, 1, 1, GenEditorGenerator.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEReference(getGenEditorGenerator_Editor(), this.getGenEditorView(), this.getGenEditorView_EditorGen(), "editor", null, 1, 1, GenEditorGenerator.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEReference(getGenEditorGenerator_Navigator(), this.getGenNavigator(), this.getGenNavigator_EditorGen(), "navigator", null, 0, 1, GenEditorGenerator.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEReference(getGenEditorGenerator_DiagramUpdater(), this.getGenDiagramUpdater(), this.getGenDiagramUpdater_EditorGen(), "diagramUpdater", null, 1, 1, GenEditorGenerator.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEReference(getGenEditorGenerator_PropertySheet(), this.getGenPropertySheet(), this.getGenPropertySheet_EditorGen(), "propertySheet", null, 0, 1, GenEditorGenerator.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEReference(getGenEditorGenerator_Application(), this.getGenApplication(), this.getGenApplication_EditorGen(), "application", null, 0, 1, GenEditorGenerator.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEReference(getGenEditorGenerator_DomainGenModel(), theGenModelPackage.getGenModel(), null, "domainGenModel", null, 0, 1, GenEditorGenerator.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_COMPOSITE, IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEAttribute(getGenEditorGenerator_PackageNamePrefix(), ecorePackage.getEString(), "packageNamePrefix", null, 0, 1, GenEditorGenerator.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEAttribute(getGenEditorGenerator_ModelID(), ecorePackage.getEString(), "modelID", null, 1, 1, GenEditorGenerator.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEAttribute(getGenEditorGenerator_SameFileForDiagramAndModel(), ecorePackage.getEBoolean(), "sameFileForDiagramAndModel", null, 0, 1, GenEditorGenerator.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEAttribute(getGenEditorGenerator_DiagramFileExtension(), ecorePackage.getEString(), "diagramFileExtension", null, 0, 1, GenEditorGenerator.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEAttribute(getGenEditorGenerator_DomainFileExtension(), ecorePackage.getEString(), "domainFileExtension", null, 0, 1, GenEditorGenerator.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEAttribute(getGenEditorGenerator_DynamicTemplates(), ecorePackage.getEBoolean(), "dynamicTemplates", "false", 0, 1, GenEditorGenerator.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEAttribute(getGenEditorGenerator_TemplateDirectory(), ecorePackage.getEString(), "templateDirectory", null, 0, 1, GenEditorGenerator.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEAttribute(getGenEditorGenerator_CopyrightText(), ecorePackage.getEString(), "copyrightText", null, 0, 1, GenEditorGenerator.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEReference(getGenEditorGenerator_ExpressionProviders(), this.getGenExpressionProviderContainer(), this.getGenExpressionProviderContainer_EditorGen(), "expressionProviders", null, 0, 1, GenEditorGenerator.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 
 		EOperation op = addEOperation(genEditorGeneratorEClass, theGenModelPackage.getGenPackage(), "getAllDomainGenPackages", 0, -1, IS_UNIQUE, IS_ORDERED);
 		addEParameter(op, ecorePackage.getEBoolean(), "withUsed", 0, 1, IS_UNIQUE, IS_ORDERED);
 
 		op = addEOperation(genEditorGeneratorEClass, ecorePackage.getEBoolean(), "requiresParser", 0, 1, IS_UNIQUE, IS_ORDERED);
 		addEParameter(op, this.getLabelTextAccessMethod(), "method", 0, 1, IS_UNIQUE, IS_ORDERED);
 
 		initEClass(genDiagramEClass, GenDiagram.class, "GenDiagram", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
 		initEReference(getGenDiagram_EditorGen(), this.getGenEditorGenerator(), this.getGenEditorGenerator_Diagram(), "editorGen", null, 0, 1, GenDiagram.class, !IS_TRANSIENT, !IS_VOLATILE, !IS_CHANGEABLE, !IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEReference(getGenDiagram_DomainDiagramElement(), theGenModelPackage.getGenClass(), null, "domainDiagramElement", null, 0, 1, GenDiagram.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_COMPOSITE, IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEReference(getGenDiagram_ChildNodes(), this.getGenChildNode(), this.getGenChildNode_Diagram(), "childNodes", null, 0, -1, GenDiagram.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEReference(getGenDiagram_TopLevelNodes(), this.getGenTopLevelNode(), this.getGenTopLevelNode_Diagram(), "topLevelNodes", null, 0, -1, GenDiagram.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEReference(getGenDiagram_Links(), this.getGenLink(), this.getGenLink_Diagram(), "links", null, 0, -1, GenDiagram.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEReference(getGenDiagram_Compartments(), this.getGenCompartment(), this.getGenCompartment_Diagram(), "compartments", null, 0, -1, GenDiagram.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEReference(getGenDiagram_Palette(), this.getPalette(), this.getPalette_Diagram(), "palette", null, 0, 1, GenDiagram.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEAttribute(getGenDiagram_Synchronized(), ecorePackage.getEBoolean(), "synchronized", "true", 0, 1, GenDiagram.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEReference(getGenDiagram_Preferences(), this.getGenDiagramPreferences(), null, "preferences", null, 0, 1, GenDiagram.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEReference(getGenDiagram_PreferencePages(), this.getGenPreferencePage(), null, "preferencePages", null, 0, -1, GenDiagram.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 
 		addEOperation(genDiagramEClass, this.getGenNode(), "getAllNodes", 0, -1, IS_UNIQUE, IS_ORDERED);
 
 		addEOperation(genDiagramEClass, this.getGenChildContainer(), "getAllChildContainers", 0, -1, IS_UNIQUE, IS_ORDERED);
 
 		addEOperation(genDiagramEClass, this.getGenContainerBase(), "getAllContainers", 0, -1, IS_UNIQUE, IS_ORDERED);
 
 		addEOperation(genDiagramEClass, ecorePackage.getEString(), "getElementInitializersClassName", 0, 1, IS_UNIQUE, IS_ORDERED);
 
 		addEOperation(genDiagramEClass, ecorePackage.getEString(), "getElementInitializersPackageName", 0, 1, IS_UNIQUE, IS_ORDERED);
 
 		initEClass(genEditorViewEClass, GenEditorView.class, "GenEditorView", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
 		initEReference(getGenEditorView_EditorGen(), this.getGenEditorGenerator(), this.getGenEditorGenerator_Editor(), "editorGen", null, 0, 1, GenEditorView.class, !IS_TRANSIENT, !IS_VOLATILE, !IS_CHANGEABLE, !IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEAttribute(getGenEditorView_PackageName(), ecorePackage.getEString(), "packageName", null, 0, 1, GenEditorView.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEAttribute(getGenEditorView_ActionBarContributorClassName(), ecorePackage.getEString(), "actionBarContributorClassName", null, 0, 1, GenEditorView.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEAttribute(getGenEditorView_ClassName(), ecorePackage.getEString(), "className", null, 0, 1, GenEditorView.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEAttribute(getGenEditorView_IconPath(), ecorePackage.getEString(), "iconPath", null, 1, 1, GenEditorView.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEAttribute(getGenEditorView_IconPathX(), ecorePackage.getEString(), "iconPathX", null, 1, 1, GenEditorView.class, IS_TRANSIENT, IS_VOLATILE, !IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, IS_DERIVED, IS_ORDERED);
 		initEAttribute(getGenEditorView_ID(), ecorePackage.getEString(), "iD", null, 0, 1, GenEditorView.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEAttribute(getGenEditorView_EclipseEditor(), ecorePackage.getEBoolean(), "eclipseEditor", "true", 0, 1, GenEditorView.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEAttribute(getGenEditorView_ContextID(), ecorePackage.getEString(), "contextID", null, 0, 1, GenEditorView.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 
 		addEOperation(genEditorViewEClass, ecorePackage.getEString(), "getActionBarContributorQualifiedClassName", 0, 1, IS_UNIQUE, IS_ORDERED);
 
 		addEOperation(genEditorViewEClass, ecorePackage.getEString(), "getQualifiedClassName", 0, 1, IS_UNIQUE, IS_ORDERED);
 
 		initEClass(genPreferencePageEClass, GenPreferencePage.class, "GenPreferencePage", IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
 		initEAttribute(getGenPreferencePage_ID(), ecorePackage.getEString(), "iD", null, 1, 1, GenPreferencePage.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEAttribute(getGenPreferencePage_Name(), ecorePackage.getEString(), "name", null, 1, 1, GenPreferencePage.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEReference(getGenPreferencePage_Children(), this.getGenPreferencePage(), this.getGenPreferencePage_Parent(), "children", null, 0, -1, GenPreferencePage.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEReference(getGenPreferencePage_Parent(), this.getGenPreferencePage(), this.getGenPreferencePage_Children(), "parent", null, 0, 1, GenPreferencePage.class, !IS_TRANSIENT, !IS_VOLATILE, !IS_CHANGEABLE, !IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 
 		addEOperation(genPreferencePageEClass, this.getGenDiagram(), "getDiagram", 0, 1, IS_UNIQUE, IS_ORDERED);
 
 		initEClass(genCustomPreferencePageEClass, GenCustomPreferencePage.class, "GenCustomPreferencePage", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
 		initEAttribute(getGenCustomPreferencePage_QualifiedClassName(), ecorePackage.getEString(), "qualifiedClassName", null, 1, 1, GenCustomPreferencePage.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 
 		initEClass(genStandardPreferencePageEClass, GenStandardPreferencePage.class, "GenStandardPreferencePage", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
 		initEAttribute(getGenStandardPreferencePage_Kind(), this.getStandardPreferencePages(), "kind", null, 1, 1, GenStandardPreferencePage.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 
 		initEClass(genDiagramPreferencesEClass, GenDiagramPreferences.class, "GenDiagramPreferences", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
 		initEAttribute(getGenDiagramPreferences_LineStyle(), this.getRouting(), "lineStyle", null, 0, 1, GenDiagramPreferences.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEReference(getGenDiagramPreferences_DefaultFont(), this.getGenFont(), null, "defaultFont", null, 0, 1, GenDiagramPreferences.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEReference(getGenDiagramPreferences_FontColor(), this.getGenColor(), null, "fontColor", null, 0, 1, GenDiagramPreferences.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEReference(getGenDiagramPreferences_FillColor(), this.getGenColor(), null, "fillColor", null, 0, 1, GenDiagramPreferences.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEReference(getGenDiagramPreferences_LineColor(), this.getGenColor(), null, "lineColor", null, 0, 1, GenDiagramPreferences.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEReference(getGenDiagramPreferences_NoteFillColor(), this.getGenColor(), null, "noteFillColor", null, 0, 1, GenDiagramPreferences.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEReference(getGenDiagramPreferences_NoteLineColor(), this.getGenColor(), null, "noteLineColor", null, 0, 1, GenDiagramPreferences.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEAttribute(getGenDiagramPreferences_ShowConnectionHandles(), ecorePackage.getEBoolean(), "showConnectionHandles", "true", 0, 1, GenDiagramPreferences.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEAttribute(getGenDiagramPreferences_ShowPopupBars(), ecorePackage.getEBoolean(), "showPopupBars", "true", 0, 1, GenDiagramPreferences.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEAttribute(getGenDiagramPreferences_PromptOnDelFromModel(), ecorePackage.getEBoolean(), "promptOnDelFromModel", null, 0, 1, GenDiagramPreferences.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEAttribute(getGenDiagramPreferences_PromptOnDelFromDiagram(), ecorePackage.getEBoolean(), "promptOnDelFromDiagram", null, 0, 1, GenDiagramPreferences.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEAttribute(getGenDiagramPreferences_EnableAnimatedLayout(), ecorePackage.getEBoolean(), "enableAnimatedLayout", "true", 0, 1, GenDiagramPreferences.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEAttribute(getGenDiagramPreferences_EnableAnimatedZoom(), ecorePackage.getEBoolean(), "enableAnimatedZoom", "true", 0, 1, GenDiagramPreferences.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEAttribute(getGenDiagramPreferences_EnableAntiAlias(), ecorePackage.getEBoolean(), "enableAntiAlias", "true", 0, 1, GenDiagramPreferences.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEAttribute(getGenDiagramPreferences_ShowGrid(), ecorePackage.getEBoolean(), "showGrid", "false", 0, 1, GenDiagramPreferences.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEAttribute(getGenDiagramPreferences_ShowRulers(), ecorePackage.getEBoolean(), "showRulers", "false", 0, 1, GenDiagramPreferences.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEAttribute(getGenDiagramPreferences_SnapToGrid(), ecorePackage.getEBoolean(), "snapToGrid", "true", 0, 1, GenDiagramPreferences.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEAttribute(getGenDiagramPreferences_RulerUnits(), this.getRulerUnits(), "rulerUnits", null, 0, 1, GenDiagramPreferences.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEAttribute(getGenDiagramPreferences_GridSpacing(), ecorePackage.getEDouble(), "gridSpacing", null, 0, 1, GenDiagramPreferences.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 
 		initEClass(genFontEClass, GenFont.class, "GenFont", IS_ABSTRACT, IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
 
 		initEClass(genStandardFontEClass, GenStandardFont.class, "GenStandardFont", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
 		initEAttribute(getGenStandardFont_Name(), this.getJFaceFont(), "name", null, 0, 1, GenStandardFont.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 
 		initEClass(genCustomFontEClass, GenCustomFont.class, "GenCustomFont", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
 		initEAttribute(getGenCustomFont_Name(), ecorePackage.getEString(), "name", null, 0, 1, GenCustomFont.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEAttribute(getGenCustomFont_Height(), ecorePackage.getEInt(), "height", null, 0, 1, GenCustomFont.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEAttribute(getGenCustomFont_Style(), this.getFontStyle(), "style", null, 0, 1, GenCustomFont.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 
 		initEClass(genColorEClass, GenColor.class, "GenColor", IS_ABSTRACT, IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
 
 		initEClass(genRGBColorEClass, GenRGBColor.class, "GenRGBColor", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
 		initEAttribute(getGenRGBColor_Red(), ecorePackage.getEInt(), "red", null, 1, 1, GenRGBColor.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEAttribute(getGenRGBColor_Green(), ecorePackage.getEInt(), "green", null, 1, 1, GenRGBColor.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEAttribute(getGenRGBColor_Blue(), ecorePackage.getEInt(), "blue", null, 1, 1, GenRGBColor.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 
 		initEClass(genConstantColorEClass, GenConstantColor.class, "GenConstantColor", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
 		initEAttribute(getGenConstantColor_Name(), this.getDiagramColors(), "name", null, 0, 1, GenConstantColor.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 
 		initEClass(batchValidationEClass, BatchValidation.class, "BatchValidation", IS_ABSTRACT, IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
 		initEAttribute(getBatchValidation_ValidationProviderClassName(), ecorePackage.getEString(), "validationProviderClassName", null, 0, 1, BatchValidation.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEAttribute(getBatchValidation_ValidationProviderPriority(), this.getProviderPriority(), "validationProviderPriority", null, 0, 1, BatchValidation.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEAttribute(getBatchValidation_MarkerNavigationProviderClassName(), ecorePackage.getEString(), "markerNavigationProviderClassName", null, 0, 1, BatchValidation.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEAttribute(getBatchValidation_MarkerNavigationProviderPriority(), this.getProviderPriority(), "markerNavigationProviderPriority", null, 0, 1, BatchValidation.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEAttribute(getBatchValidation_ValidationEnabled(), ecorePackage.getEBoolean(), "validationEnabled", null, 0, 1, BatchValidation.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEAttribute(getBatchValidation_MetricProviderClassName(), ecorePackage.getEString(), "metricProviderClassName", null, 0, 1, BatchValidation.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEAttribute(getBatchValidation_MetricProviderPriority(), this.getProviderPriority(), "metricProviderPriority", null, 0, 1, BatchValidation.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEAttribute(getBatchValidation_ValidationDecoratorProviderClassName(), ecorePackage.getEString(), "validationDecoratorProviderClassName", null, 0, 1, BatchValidation.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEAttribute(getBatchValidation_ValidationDecorators(), ecorePackage.getEBoolean(), "validationDecorators", null, 0, 1, BatchValidation.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEAttribute(getBatchValidation_ValidationDecoratorProviderPriority(), this.getProviderPriority(), "validationDecoratorProviderPriority", null, 0, 1, BatchValidation.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEAttribute(getBatchValidation_LiveValidationUIFeedback(), ecorePackage.getEBoolean(), "liveValidationUIFeedback", null, 0, 1, BatchValidation.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 
 		addEOperation(batchValidationEClass, ecorePackage.getEString(), "getValidationProviderQualifiedClassName", 0, 1, IS_UNIQUE, IS_ORDERED);
 
 		addEOperation(batchValidationEClass, ecorePackage.getEString(), "getValidationDiagnosticMarkerType", 0, 1, IS_UNIQUE, IS_ORDERED);
 
 		addEOperation(batchValidationEClass, ecorePackage.getEString(), "getMarkerNavigationProviderQualifiedClassName", 0, 1, IS_UNIQUE, IS_ORDERED);
 
 		addEOperation(batchValidationEClass, ecorePackage.getEString(), "getMetricProviderQualifiedClassName", 0, 1, IS_UNIQUE, IS_ORDERED);
 
 		addEOperation(batchValidationEClass, ecorePackage.getEString(), "getMetricViewID", 0, 1, IS_UNIQUE, IS_ORDERED);
 
		addEOperation(batchValidationEClass, ecorePackage.getEString(), "getValidationDecoratorProviderQualifedClassName", 0, 1, IS_UNIQUE, IS_ORDERED);
 
 		initEClass(providerClassNamesEClass, ProviderClassNames.class, "ProviderClassNames", IS_ABSTRACT, IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
 		initEAttribute(getProviderClassNames_ElementTypesClassName(), ecorePackage.getEString(), "elementTypesClassName", null, 0, 1, ProviderClassNames.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEAttribute(getProviderClassNames_NotationViewProviderClassName(), ecorePackage.getEString(), "notationViewProviderClassName", null, 0, 1, ProviderClassNames.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEAttribute(getProviderClassNames_NotationViewProviderPriority(), this.getProviderPriority(), "notationViewProviderPriority", null, 0, 1, ProviderClassNames.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEAttribute(getProviderClassNames_EditPartProviderClassName(), ecorePackage.getEString(), "editPartProviderClassName", null, 0, 1, ProviderClassNames.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEAttribute(getProviderClassNames_EditPartProviderPriority(), this.getProviderPriority(), "editPartProviderPriority", null, 0, 1, ProviderClassNames.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEAttribute(getProviderClassNames_ModelingAssistantProviderClassName(), ecorePackage.getEString(), "modelingAssistantProviderClassName", null, 0, 1, ProviderClassNames.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEAttribute(getProviderClassNames_ModelingAssistantProviderPriority(), this.getProviderPriority(), "modelingAssistantProviderPriority", null, 0, 1, ProviderClassNames.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEAttribute(getProviderClassNames_IconProviderClassName(), ecorePackage.getEString(), "iconProviderClassName", null, 0, 1, ProviderClassNames.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEAttribute(getProviderClassNames_IconProviderPriority(), this.getProviderPriority(), "iconProviderPriority", null, 0, 1, ProviderClassNames.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEAttribute(getProviderClassNames_ParserProviderClassName(), ecorePackage.getEString(), "parserProviderClassName", null, 0, 1, ProviderClassNames.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEAttribute(getProviderClassNames_ParserProviderPriority(), this.getProviderPriority(), "parserProviderPriority", null, 0, 1, ProviderClassNames.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEAttribute(getProviderClassNames_ContributionItemProviderClassName(), ecorePackage.getEString(), "contributionItemProviderClassName", null, 0, 1, ProviderClassNames.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 
 		addEOperation(providerClassNamesEClass, ecorePackage.getEString(), "getElementTypesQualifiedClassName", 0, 1, IS_UNIQUE, IS_ORDERED);
 
 		addEOperation(providerClassNamesEClass, ecorePackage.getEString(), "getNotationViewProviderQualifiedClassName", 0, 1, IS_UNIQUE, IS_ORDERED);
 
 		addEOperation(providerClassNamesEClass, ecorePackage.getEString(), "getEditPartProviderQualifiedClassName", 0, 1, IS_UNIQUE, IS_ORDERED);
 
 		addEOperation(providerClassNamesEClass, ecorePackage.getEString(), "getModelingAssistantProviderQualifiedClassName", 0, 1, IS_UNIQUE, IS_ORDERED);
 
 		addEOperation(providerClassNamesEClass, ecorePackage.getEString(), "getIconProviderQualifiedClassName", 0, 1, IS_UNIQUE, IS_ORDERED);
 
 		addEOperation(providerClassNamesEClass, ecorePackage.getEString(), "getParserProviderQualifiedClassName", 0, 1, IS_UNIQUE, IS_ORDERED);
 
 		addEOperation(providerClassNamesEClass, ecorePackage.getEString(), "getContributionItemProviderQualifiedClassName", 0, 1, IS_UNIQUE, IS_ORDERED);
 
 		initEClass(shortcutsEClass, Shortcuts.class, "Shortcuts", IS_ABSTRACT, IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
 		initEAttribute(getShortcuts_ShortcutsDecoratorProviderClassName(), ecorePackage.getEString(), "shortcutsDecoratorProviderClassName", null, 0, 1, Shortcuts.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEAttribute(getShortcuts_ShortcutsDecoratorProviderPriority(), this.getProviderPriority(), "shortcutsDecoratorProviderPriority", null, 0, 1, Shortcuts.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEAttribute(getShortcuts_CreateShortcutActionClassName(), ecorePackage.getEString(), "createShortcutActionClassName", null, 0, 1, Shortcuts.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEAttribute(getShortcuts_CreateShortcutDecorationsCommandClassName(), ecorePackage.getEString(), "createShortcutDecorationsCommandClassName", null, 0, 1, Shortcuts.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEAttribute(getShortcuts_ShortcutPropertyTesterClassName(), ecorePackage.getEString(), "shortcutPropertyTesterClassName", null, 0, 1, Shortcuts.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEAttribute(getShortcuts_ContainsShortcutsTo(), ecorePackage.getEString(), "containsShortcutsTo", null, 0, -1, Shortcuts.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEAttribute(getShortcuts_ShortcutsProvidedFor(), ecorePackage.getEString(), "shortcutsProvidedFor", null, 0, -1, Shortcuts.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 
 		addEOperation(shortcutsEClass, ecorePackage.getEString(), "getShortcutsDecoratorProviderQualifiedClassName", 0, 1, IS_UNIQUE, IS_ORDERED);
 
 		addEOperation(shortcutsEClass, ecorePackage.getEString(), "getCreateShortcutActionQualifiedClassName", 0, 1, IS_UNIQUE, IS_ORDERED);
 
 		addEOperation(shortcutsEClass, ecorePackage.getEString(), "getCreateShortcutDecorationsCommandQualifiedClassName", 0, 1, IS_UNIQUE, IS_ORDERED);
 
 		addEOperation(shortcutsEClass, ecorePackage.getEString(), "getShortcutPropertyTesterQualifiedClassName", 0, 1, IS_UNIQUE, IS_ORDERED);
 
 		addEOperation(shortcutsEClass, ecorePackage.getEBoolean(), "generateCreateShortcutAction", 0, 1, IS_UNIQUE, IS_ORDERED);
 
 		addEOperation(shortcutsEClass, ecorePackage.getEBoolean(), "generateShortcutIcon", 0, 1, IS_UNIQUE, IS_ORDERED);
 
 		initEClass(packageNamesEClass, PackageNames.class, "PackageNames", IS_ABSTRACT, IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
 		initEAttribute(getPackageNames_EditCommandsPackageName(), ecorePackage.getEString(), "editCommandsPackageName", null, 0, 1, PackageNames.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEAttribute(getPackageNames_EditHelpersPackageName(), ecorePackage.getEString(), "editHelpersPackageName", null, 0, 1, PackageNames.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEAttribute(getPackageNames_EditPartsPackageName(), ecorePackage.getEString(), "editPartsPackageName", null, 0, 1, PackageNames.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEAttribute(getPackageNames_EditPoliciesPackageName(), ecorePackage.getEString(), "editPoliciesPackageName", null, 0, 1, PackageNames.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEAttribute(getPackageNames_PreferencesPackageName(), ecorePackage.getEString(), "preferencesPackageName", null, 0, 1, PackageNames.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEAttribute(getPackageNames_ProvidersPackageName(), ecorePackage.getEString(), "providersPackageName", null, 0, 1, PackageNames.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEAttribute(getPackageNames_ParsersPackageName(), ecorePackage.getEString(), "parsersPackageName", null, 0, 1, PackageNames.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEAttribute(getPackageNames_NotationViewFactoriesPackageName(), ecorePackage.getEString(), "notationViewFactoriesPackageName", null, 0, 1, PackageNames.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 
 		initEClass(linkConstraintsEClass, LinkConstraints.class, "LinkConstraints", IS_ABSTRACT, IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
 
 		addEOperation(linkConstraintsEClass, ecorePackage.getEBoolean(), "hasLinkCreationConstraints", 1, 1, IS_UNIQUE, IS_ORDERED);
 
 		addEOperation(linkConstraintsEClass, ecorePackage.getEString(), "getLinkCreationConstraintsClassName", 1, 1, IS_UNIQUE, IS_ORDERED);
 
 		addEOperation(linkConstraintsEClass, ecorePackage.getEString(), "getLinkCreationConstraintsQualifiedClassName", 1, 1, IS_UNIQUE, IS_ORDERED);
 
 		initEClass(editorCandiesEClass, EditorCandies.class, "EditorCandies", IS_ABSTRACT, IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
 		initEAttribute(getEditorCandies_CreationWizardClassName(), ecorePackage.getEString(), "creationWizardClassName", null, 0, 1, EditorCandies.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEAttribute(getEditorCandies_CreationWizardPageClassName(), ecorePackage.getEString(), "creationWizardPageClassName", null, 0, 1, EditorCandies.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEAttribute(getEditorCandies_CreationWizardIconPath(), ecorePackage.getEString(), "creationWizardIconPath", null, 1, 1, EditorCandies.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEAttribute(getEditorCandies_CreationWizardIconPathX(), ecorePackage.getEString(), "creationWizardIconPathX", null, 1, 1, EditorCandies.class, IS_TRANSIENT, IS_VOLATILE, !IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, IS_DERIVED, IS_ORDERED);
 		initEAttribute(getEditorCandies_CreationWizardCategoryID(), ecorePackage.getEString(), "creationWizardCategoryID", null, 0, 1, EditorCandies.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEAttribute(getEditorCandies_DiagramEditorUtilClassName(), ecorePackage.getEString(), "diagramEditorUtilClassName", null, 0, 1, EditorCandies.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEAttribute(getEditorCandies_DocumentProviderClassName(), ecorePackage.getEString(), "documentProviderClassName", null, 0, 1, EditorCandies.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEAttribute(getEditorCandies_InitDiagramFileActionClassName(), ecorePackage.getEString(), "initDiagramFileActionClassName", null, 0, 1, EditorCandies.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEAttribute(getEditorCandies_NewDiagramFileWizardClassName(), ecorePackage.getEString(), "newDiagramFileWizardClassName", null, 0, 1, EditorCandies.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEAttribute(getEditorCandies_DiagramContentInitializerClassName(), ecorePackage.getEString(), "diagramContentInitializerClassName", null, 0, 1, EditorCandies.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEAttribute(getEditorCandies_MatchingStrategyClassName(), ecorePackage.getEString(), "matchingStrategyClassName", null, 0, 1, EditorCandies.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEAttribute(getEditorCandies_VisualIDRegistryClassName(), ecorePackage.getEString(), "visualIDRegistryClassName", null, 0, 1, EditorCandies.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEAttribute(getEditorCandies_ElementChooserClassName(), ecorePackage.getEString(), "elementChooserClassName", null, 0, 1, EditorCandies.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEAttribute(getEditorCandies_LoadResourceActionClassName(), ecorePackage.getEString(), "loadResourceActionClassName", null, 0, 1, EditorCandies.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEAttribute(getEditorCandies_EditingDomainID(), ecorePackage.getEString(), "editingDomainID", null, 0, 1, EditorCandies.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 
 		addEOperation(editorCandiesEClass, ecorePackage.getEString(), "getCreationWizardQualifiedClassName", 0, 1, IS_UNIQUE, IS_ORDERED);
 
 		addEOperation(editorCandiesEClass, ecorePackage.getEString(), "getCreationWizardPageQualifiedClassName", 0, 1, IS_UNIQUE, IS_ORDERED);
 
 		addEOperation(editorCandiesEClass, ecorePackage.getEString(), "getDiagramEditorUtilQualifiedClassName", 0, 1, IS_UNIQUE, IS_ORDERED);
 
 		addEOperation(editorCandiesEClass, ecorePackage.getEString(), "getDocumentProviderQualifiedClassName", 0, 1, IS_UNIQUE, IS_ORDERED);
 
 		addEOperation(editorCandiesEClass, ecorePackage.getEString(), "getInitDiagramFileActionQualifiedClassName", 0, 1, IS_UNIQUE, IS_ORDERED);
 
 		addEOperation(editorCandiesEClass, ecorePackage.getEString(), "getNewDiagramFileWizardQualifiedClassName", 0, 1, IS_UNIQUE, IS_ORDERED);
 
 		addEOperation(editorCandiesEClass, ecorePackage.getEString(), "getDiagramContentInitializerQualifiedClassName", 0, 1, IS_UNIQUE, IS_ORDERED);
 
 		addEOperation(editorCandiesEClass, ecorePackage.getEString(), "getMatchingStrategyQualifiedClassName", 0, 1, IS_UNIQUE, IS_ORDERED);
 
 		addEOperation(editorCandiesEClass, ecorePackage.getEString(), "getVisualIDRegistryQualifiedClassName", 0, 1, IS_UNIQUE, IS_ORDERED);
 
 		addEOperation(editorCandiesEClass, ecorePackage.getEString(), "getElementChooserQualifiedClassName", 0, 1, IS_UNIQUE, IS_ORDERED);
 
 		addEOperation(editorCandiesEClass, ecorePackage.getEString(), "getLoadResourceActionQualifiedClassName", 0, 1, IS_UNIQUE, IS_ORDERED);
 
 		addEOperation(editorCandiesEClass, ecorePackage.getEBoolean(), "generateInitDiagramAction", 0, 1, IS_UNIQUE, IS_ORDERED);
 
 		initEClass(editPartCandiesEClass, EditPartCandies.class, "EditPartCandies", IS_ABSTRACT, IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
 		initEAttribute(getEditPartCandies_ReorientConnectionViewCommandClassName(), ecorePackage.getEString(), "reorientConnectionViewCommandClassName", null, 0, 1, EditPartCandies.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEAttribute(getEditPartCandies_BaseEditHelperClassName(), ecorePackage.getEString(), "baseEditHelperClassName", null, 0, 1, EditPartCandies.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEAttribute(getEditPartCandies_EditPartFactoryClassName(), ecorePackage.getEString(), "editPartFactoryClassName", null, 0, 1, EditPartCandies.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEAttribute(getEditPartCandies_BaseExternalNodeLabelEditPartClassName(), ecorePackage.getEString(), "baseExternalNodeLabelEditPartClassName", null, 0, 1, EditPartCandies.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEAttribute(getEditPartCandies_BaseItemSemanticEditPolicyClassName(), ecorePackage.getEString(), "baseItemSemanticEditPolicyClassName", null, 0, 1, EditPartCandies.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEAttribute(getEditPartCandies_BaseGraphicalNodeEditPolicyClassName(), ecorePackage.getEString(), "baseGraphicalNodeEditPolicyClassName", null, 0, 1, EditPartCandies.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEAttribute(getEditPartCandies_TextSelectionEditPolicyClassName(), ecorePackage.getEString(), "textSelectionEditPolicyClassName", null, 0, 1, EditPartCandies.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEAttribute(getEditPartCandies_TextNonResizableEditPolicyClassName(), ecorePackage.getEString(), "textNonResizableEditPolicyClassName", null, 0, 1, EditPartCandies.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 
 		addEOperation(editPartCandiesEClass, ecorePackage.getEString(), "getReorientConnectionViewCommandQualifiedClassName", 0, 1, IS_UNIQUE, IS_ORDERED);
 
 		addEOperation(editPartCandiesEClass, ecorePackage.getEString(), "getBaseEditHelperQualifiedClassName", 0, 1, IS_UNIQUE, IS_ORDERED);
 
 		addEOperation(editPartCandiesEClass, ecorePackage.getEString(), "getEditPartFactoryQualifiedClassName", 0, 1, IS_UNIQUE, IS_ORDERED);
 
 		addEOperation(editPartCandiesEClass, ecorePackage.getEString(), "getBaseExternalNodeLabelEditPartQualifiedClassName", 0, 1, IS_UNIQUE, IS_ORDERED);
 
 		addEOperation(editPartCandiesEClass, ecorePackage.getEString(), "getBaseItemSemanticEditPolicyQualifiedClassName", 0, 1, IS_UNIQUE, IS_ORDERED);
 
 		addEOperation(editPartCandiesEClass, ecorePackage.getEString(), "getBaseGraphicalNodeEditPolicyQualifiedClassName", 0, 1, IS_UNIQUE, IS_ORDERED);
 
 		addEOperation(editPartCandiesEClass, ecorePackage.getEString(), "getTextSelectionEditPolicyQualifiedClassName", 0, 1, IS_UNIQUE, IS_ORDERED);
 
 		addEOperation(editPartCandiesEClass, ecorePackage.getEString(), "getTextNonResizableEditPolicyQualifiedClassName", 0, 1, IS_UNIQUE, IS_ORDERED);
 
 		initEClass(measurementUnitEClass, MeasurementUnit.class, "MeasurementUnit", IS_ABSTRACT, IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
 		initEAttribute(getMeasurementUnit_Units(), ecorePackage.getEString(), "units", "Pixel", 0, 1, MeasurementUnit.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 
 		initEClass(genDiagramUpdaterEClass, GenDiagramUpdater.class, "GenDiagramUpdater", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
 		initEReference(getGenDiagramUpdater_EditorGen(), this.getGenEditorGenerator(), this.getGenEditorGenerator_DiagramUpdater(), "editorGen", null, 0, 1, GenDiagramUpdater.class, !IS_TRANSIENT, !IS_VOLATILE, !IS_CHANGEABLE, !IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEAttribute(getGenDiagramUpdater_DiagramUpdaterClassName(), ecorePackage.getEString(), "diagramUpdaterClassName", null, 0, 1, GenDiagramUpdater.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEAttribute(getGenDiagramUpdater_NodeDescriptorClassName(), ecorePackage.getEString(), "nodeDescriptorClassName", null, 0, 1, GenDiagramUpdater.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEAttribute(getGenDiagramUpdater_LinkDescriptorClassName(), ecorePackage.getEString(), "linkDescriptorClassName", null, 0, 1, GenDiagramUpdater.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEAttribute(getGenDiagramUpdater_UpdateCommandClassName(), ecorePackage.getEString(), "updateCommandClassName", null, 0, 1, GenDiagramUpdater.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEAttribute(getGenDiagramUpdater_UpdateCommandID(), ecorePackage.getEString(), "updateCommandID", null, 0, 1, GenDiagramUpdater.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 
 		addEOperation(genDiagramUpdaterEClass, ecorePackage.getEString(), "getDiagramUpdaterQualifiedClassName", 0, 1, IS_UNIQUE, IS_ORDERED);
 
 		addEOperation(genDiagramUpdaterEClass, ecorePackage.getEString(), "getNodeDescriptorQualifiedClassName", 0, 1, IS_UNIQUE, IS_ORDERED);
 
 		addEOperation(genDiagramUpdaterEClass, ecorePackage.getEString(), "getLinkDescriptorQualifiedClassName", 0, 1, IS_UNIQUE, IS_ORDERED);
 
 		addEOperation(genDiagramUpdaterEClass, ecorePackage.getEString(), "getUpdateCommandQualifiedClassName", 0, 1, IS_UNIQUE, IS_ORDERED);
 
 		initEClass(genPluginEClass, GenPlugin.class, "GenPlugin", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
 		initEReference(getGenPlugin_EditorGen(), this.getGenEditorGenerator(), this.getGenEditorGenerator_Plugin(), "editorGen", null, 0, 1, GenPlugin.class, !IS_TRANSIENT, !IS_VOLATILE, !IS_CHANGEABLE, !IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEAttribute(getGenPlugin_ID(), ecorePackage.getEString(), "iD", null, 0, 1, GenPlugin.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEAttribute(getGenPlugin_Name(), ecorePackage.getEString(), "name", null, 0, 1, GenPlugin.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEAttribute(getGenPlugin_Provider(), ecorePackage.getEString(), "provider", "Sample Plugin Provider, Inc", 0, 1, GenPlugin.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEAttribute(getGenPlugin_Version(), ecorePackage.getEString(), "version", "1.0.0.qualifier", 0, 1, GenPlugin.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEAttribute(getGenPlugin_PrintingEnabled(), ecorePackage.getEBoolean(), "printingEnabled", null, 0, 1, GenPlugin.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEAttribute(getGenPlugin_RequiredPlugins(), ecorePackage.getEString(), "requiredPlugins", null, 0, -1, GenPlugin.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEAttribute(getGenPlugin_ActivatorClassName(), ecorePackage.getEString(), "activatorClassName", null, 0, 1, GenPlugin.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 
 		addEOperation(genPluginEClass, ecorePackage.getEString(), "getAllRequiredPlugins", 0, -1, IS_UNIQUE, IS_ORDERED);
 
 		addEOperation(genPluginEClass, ecorePackage.getEString(), "getActivatorQualifiedClassName", 0, 1, IS_UNIQUE, IS_ORDERED);
 
 		initEClass(genCommonBaseEClass, GenCommonBase.class, "GenCommonBase", IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
 		initEReference(getGenCommonBase_DiagramRunTimeClass(), theGenModelPackage.getGenClass(), null, "diagramRunTimeClass", null, 1, 1, GenCommonBase.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_COMPOSITE, IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEAttribute(getGenCommonBase_VisualID(), ecorePackage.getEInt(), "visualID", null, 1, 1, GenCommonBase.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEReference(getGenCommonBase_ElementType(), this.getElementType(), this.getElementType_DiagramElement(), "elementType", null, 0, 1, GenCommonBase.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEAttribute(getGenCommonBase_EditPartClassName(), ecorePackage.getEString(), "editPartClassName", null, 0, 1, GenCommonBase.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEAttribute(getGenCommonBase_ItemSemanticEditPolicyClassName(), ecorePackage.getEString(), "itemSemanticEditPolicyClassName", null, 0, 1, GenCommonBase.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEAttribute(getGenCommonBase_NotationViewFactoryClassName(), ecorePackage.getEString(), "notationViewFactoryClassName", null, 0, 1, GenCommonBase.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEReference(getGenCommonBase_Viewmap(), this.getViewmap(), null, "viewmap", null, 1, 1, GenCommonBase.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEReference(getGenCommonBase_Styles(), theGenModelPackage.getGenClass(), null, "styles", null, 0, -1, GenCommonBase.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_COMPOSITE, IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEReference(getGenCommonBase_Behaviour(), this.getBehaviour(), this.getBehaviour_Subject(), "behaviour", null, 0, -1, GenCommonBase.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEAttribute(getGenCommonBase_SansDomain(), ecorePackage.getEBoolean(), "sansDomain", null, 0, 1, GenCommonBase.class, IS_TRANSIENT, IS_VOLATILE, !IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, IS_DERIVED, IS_ORDERED);
 
 		addEOperation(genCommonBaseEClass, ecorePackage.getEString(), "getEditPartQualifiedClassName", 0, 1, IS_UNIQUE, IS_ORDERED);
 
 		addEOperation(genCommonBaseEClass, ecorePackage.getEString(), "getItemSemanticEditPolicyQualifiedClassName", 0, 1, IS_UNIQUE, IS_ORDERED);
 
 		addEOperation(genCommonBaseEClass, ecorePackage.getEString(), "getNotationViewFactoryQualifiedClassName", 0, 1, IS_UNIQUE, IS_ORDERED);
 
 		addEOperation(genCommonBaseEClass, this.getGenDiagram(), "getDiagram", 0, 1, IS_UNIQUE, IS_ORDERED);
 
 		addEOperation(genCommonBaseEClass, ecorePackage.getEString(), "getClassNamePrefix", 0, 1, IS_UNIQUE, IS_ORDERED);
 
 		addEOperation(genCommonBaseEClass, ecorePackage.getEString(), "getClassNameSuffux", 0, 1, IS_UNIQUE, IS_ORDERED);
 
 		addEOperation(genCommonBaseEClass, ecorePackage.getEString(), "getUniqueIdentifier", 0, 1, IS_UNIQUE, IS_ORDERED);
 
 		addEOperation(genCommonBaseEClass, this.getViewmapLayoutType(), "getLayoutType", 0, 1, IS_UNIQUE, IS_ORDERED);
 
 		initEClass(behaviourEClass, Behaviour.class, "Behaviour", IS_ABSTRACT, IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
 		initEReference(getBehaviour_Subject(), this.getGenCommonBase(), this.getGenCommonBase_Behaviour(), "subject", null, 0, 1, Behaviour.class, !IS_TRANSIENT, !IS_VOLATILE, !IS_CHANGEABLE, !IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 
 		addEOperation(behaviourEClass, ecorePackage.getEString(), "getEditPolicyQualifiedClassName", 0, 1, IS_UNIQUE, IS_ORDERED);
 
 		initEClass(customBehaviourEClass, CustomBehaviour.class, "CustomBehaviour", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
 		initEAttribute(getCustomBehaviour_Key(), ecorePackage.getEString(), "key", null, 1, 1, CustomBehaviour.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEAttribute(getCustomBehaviour_EditPolicyQualifiedClassName(), ecorePackage.getEString(), "editPolicyQualifiedClassName", null, 1, 1, CustomBehaviour.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 
 		initEClass(sharedBehaviourEClass, SharedBehaviour.class, "SharedBehaviour", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
 		initEReference(getSharedBehaviour_Delegate(), this.getBehaviour(), null, "delegate", null, 1, 1, SharedBehaviour.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_COMPOSITE, IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 
 		initEClass(openDiagramBehaviourEClass, OpenDiagramBehaviour.class, "OpenDiagramBehaviour", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
 		initEAttribute(getOpenDiagramBehaviour_EditPolicyClassName(), ecorePackage.getEString(), "editPolicyClassName", null, 1, 1, OpenDiagramBehaviour.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEAttribute(getOpenDiagramBehaviour_DiagramKind(), ecorePackage.getEString(), "diagramKind", null, 0, 1, OpenDiagramBehaviour.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEAttribute(getOpenDiagramBehaviour_EditorID(), ecorePackage.getEString(), "editorID", null, 0, 1, OpenDiagramBehaviour.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEAttribute(getOpenDiagramBehaviour_OpenAsEclipseEditor(), ecorePackage.getEBoolean(), "openAsEclipseEditor", "true", 0, 1, OpenDiagramBehaviour.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 
 		initEClass(genContainerBaseEClass, GenContainerBase.class, "GenContainerBase", IS_ABSTRACT, IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
 		initEReference(getGenContainerBase_ContainedNodes(), this.getGenNode(), null, "containedNodes", null, 0, -1, GenContainerBase.class, IS_TRANSIENT, IS_VOLATILE, !IS_CHANGEABLE, !IS_COMPOSITE, IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, IS_DERIVED, IS_ORDERED);
 		initEAttribute(getGenContainerBase_CanonicalEditPolicyClassName(), ecorePackage.getEString(), "canonicalEditPolicyClassName", null, 0, 1, GenContainerBase.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 
 		addEOperation(genContainerBaseEClass, ecorePackage.getEBoolean(), "needsCanonicalEditPolicy", 0, 1, IS_UNIQUE, IS_ORDERED);
 
 		addEOperation(genContainerBaseEClass, ecorePackage.getEString(), "getCanonicalEditPolicyQualifiedClassName", 0, 1, IS_UNIQUE, IS_ORDERED);
 
 		initEClass(genChildContainerEClass, GenChildContainer.class, "GenChildContainer", IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
 		initEReference(getGenChildContainer_ChildNodes(), this.getGenChildNode(), this.getGenChildNode_Containers(), "childNodes", null, 0, -1, GenChildContainer.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_COMPOSITE, IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 
 		initEClass(genNodeEClass, GenNode.class, "GenNode", IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
 		initEReference(getGenNode_ModelFacet(), this.getTypeModelFacet(), null, "modelFacet", null, 0, 1, GenNode.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEReference(getGenNode_Labels(), this.getGenNodeLabel(), this.getGenNodeLabel_Node(), "labels", null, 0, -1, GenNode.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEReference(getGenNode_Compartments(), this.getGenCompartment(), this.getGenCompartment_Node(), "compartments", null, 0, -1, GenNode.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_COMPOSITE, IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEAttribute(getGenNode_PrimaryDragEditPolicyQualifiedClassName(), ecorePackage.getEString(), "primaryDragEditPolicyQualifiedClassName", null, 0, 1, GenNode.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEAttribute(getGenNode_GraphicalNodeEditPolicyClassName(), ecorePackage.getEString(), "graphicalNodeEditPolicyClassName", null, 0, 1, GenNode.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEAttribute(getGenNode_CreateCommandClassName(), ecorePackage.getEString(), "createCommandClassName", null, 0, 1, GenNode.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEReference(getGenNode_ReorientedIncomingLinks(), this.getGenLink(), null, "reorientedIncomingLinks", null, 0, -1, GenNode.class, IS_TRANSIENT, IS_VOLATILE, !IS_CHANGEABLE, !IS_COMPOSITE, IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, IS_DERIVED, IS_ORDERED);
 
 		addEOperation(genNodeEClass, theGenModelPackage.getGenClass(), "getDomainMetaClass", 1, 1, IS_UNIQUE, IS_ORDERED);
 
 		addEOperation(genNodeEClass, ecorePackage.getEString(), "getGraphicalNodeEditPolicyQualifiedClassName", 0, 1, IS_UNIQUE, IS_ORDERED);
 
 		addEOperation(genNodeEClass, ecorePackage.getEString(), "getCreateCommandQualifiedClassName", 0, 1, IS_UNIQUE, IS_ORDERED);
 
 		initEClass(genTopLevelNodeEClass, GenTopLevelNode.class, "GenTopLevelNode", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
 		initEReference(getGenTopLevelNode_Diagram(), this.getGenDiagram(), this.getGenDiagram_TopLevelNodes(), "diagram", null, 1, 1, GenTopLevelNode.class, !IS_TRANSIENT, !IS_VOLATILE, !IS_CHANGEABLE, !IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 
 		initEClass(genChildNodeEClass, GenChildNode.class, "GenChildNode", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
 		initEReference(getGenChildNode_Diagram(), this.getGenDiagram(), this.getGenDiagram_ChildNodes(), "diagram", null, 1, 1, GenChildNode.class, !IS_TRANSIENT, !IS_VOLATILE, !IS_CHANGEABLE, !IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEReference(getGenChildNode_Containers(), this.getGenChildContainer(), this.getGenChildContainer_ChildNodes(), "containers", null, 0, -1, GenChildNode.class, !IS_TRANSIENT, !IS_VOLATILE, !IS_CHANGEABLE, !IS_COMPOSITE, IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 
 		initEClass(genChildSideAffixedNodeEClass, GenChildSideAffixedNode.class, "GenChildSideAffixedNode", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
 		initEAttribute(getGenChildSideAffixedNode_PreferredSideName(), ecorePackage.getEString(), "preferredSideName", "NONE", 0, 1, GenChildSideAffixedNode.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 
 		initEClass(genChildLabelNodeEClass, GenChildLabelNode.class, "GenChildLabelNode", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
 		initEAttribute(getGenChildLabelNode_LabelReadOnly(), ecorePackage.getEBoolean(), "labelReadOnly", null, 0, 1, GenChildLabelNode.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEAttribute(getGenChildLabelNode_LabelElementIcon(), ecorePackage.getEBoolean(), "labelElementIcon", null, 0, 1, GenChildLabelNode.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEReference(getGenChildLabelNode_LabelModelFacet(), this.getLabelModelFacet(), null, "labelModelFacet", null, 0, 1, GenChildLabelNode.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 
 		addEOperation(genChildLabelNodeEClass, theGenModelPackage.getGenFeature(), "getLabelMetaFeatures", 0, -1, IS_UNIQUE, IS_ORDERED);
 
 		initEClass(genCompartmentEClass, GenCompartment.class, "GenCompartment", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
 		initEAttribute(getGenCompartment_Title(), ecorePackage.getEString(), "title", null, 0, 1, GenCompartment.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEAttribute(getGenCompartment_CanCollapse(), ecorePackage.getEBoolean(), "canCollapse", "true", 0, 1, GenCompartment.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEAttribute(getGenCompartment_HideIfEmpty(), ecorePackage.getEBoolean(), "hideIfEmpty", "true", 0, 1, GenCompartment.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEAttribute(getGenCompartment_NeedsTitle(), ecorePackage.getEBoolean(), "needsTitle", "true", 0, 1, GenCompartment.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEReference(getGenCompartment_Diagram(), this.getGenDiagram(), this.getGenDiagram_Compartments(), "diagram", null, 1, 1, GenCompartment.class, !IS_TRANSIENT, !IS_VOLATILE, !IS_CHANGEABLE, !IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEReference(getGenCompartment_Node(), this.getGenNode(), this.getGenNode_Compartments(), "node", null, 1, 1, GenCompartment.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_COMPOSITE, IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEAttribute(getGenCompartment_ListLayout(), ecorePackage.getEBoolean(), "listLayout", "true", 0, 1, GenCompartment.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 
 		initEClass(genLinkEClass, GenLink.class, "GenLink", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
 		initEReference(getGenLink_Diagram(), this.getGenDiagram(), this.getGenDiagram_Links(), "diagram", null, 1, 1, GenLink.class, !IS_TRANSIENT, !IS_VOLATILE, !IS_CHANGEABLE, !IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEReference(getGenLink_ModelFacet(), this.getLinkModelFacet(), null, "modelFacet", null, 0, 1, GenLink.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEReference(getGenLink_Labels(), this.getGenLinkLabel(), this.getGenLinkLabel_Link(), "labels", null, 0, -1, GenLink.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEAttribute(getGenLink_OutgoingCreationAllowed(), ecorePackage.getEBoolean(), "outgoingCreationAllowed", "true", 0, 1, GenLink.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEAttribute(getGenLink_IncomingCreationAllowed(), ecorePackage.getEBoolean(), "incomingCreationAllowed", "false", 0, 1, GenLink.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEAttribute(getGenLink_ViewDirectionAlignedWithModel(), ecorePackage.getEBoolean(), "viewDirectionAlignedWithModel", "true", 0, 1, GenLink.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEReference(getGenLink_CreationConstraints(), this.getGenLinkConstraints(), this.getGenLinkConstraints_Link(), "creationConstraints", null, 0, 1, GenLink.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEAttribute(getGenLink_CreateCommandClassName(), ecorePackage.getEString(), "createCommandClassName", null, 0, 1, GenLink.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEAttribute(getGenLink_ReorientCommandClassName(), ecorePackage.getEString(), "reorientCommandClassName", null, 0, 1, GenLink.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 
 		addEOperation(genLinkEClass, this.getGenCommonBase(), "getAssistantSources", 0, -1, IS_UNIQUE, IS_ORDERED);
 
 		addEOperation(genLinkEClass, this.getGenCommonBase(), "getAssistantTargets", 0, -1, IS_UNIQUE, IS_ORDERED);
 
 		addEOperation(genLinkEClass, ecorePackage.getEString(), "getCreateCommandQualifiedClassName", 0, 1, IS_UNIQUE, IS_ORDERED);
 
 		addEOperation(genLinkEClass, ecorePackage.getEString(), "getReorientCommandQualifiedClassName", 0, 1, IS_UNIQUE, IS_ORDERED);
 
 		initEClass(genLabelEClass, GenLabel.class, "GenLabel", IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
 		initEAttribute(getGenLabel_ReadOnly(), ecorePackage.getEBoolean(), "readOnly", null, 0, 1, GenLabel.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEAttribute(getGenLabel_ElementIcon(), ecorePackage.getEBoolean(), "elementIcon", null, 0, 1, GenLabel.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEReference(getGenLabel_ModelFacet(), this.getLabelModelFacet(), null, "modelFacet", null, 0, 1, GenLabel.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 
 		addEOperation(genLabelEClass, theGenModelPackage.getGenFeature(), "getMetaFeatures", 0, -1, IS_UNIQUE, IS_ORDERED);
 
 		initEClass(genNodeLabelEClass, GenNodeLabel.class, "GenNodeLabel", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
 		initEReference(getGenNodeLabel_Node(), this.getGenNode(), this.getGenNode_Labels(), "node", null, 1, 1, GenNodeLabel.class, !IS_TRANSIENT, !IS_VOLATILE, !IS_CHANGEABLE, !IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 
 		initEClass(genExternalNodeLabelEClass, GenExternalNodeLabel.class, "GenExternalNodeLabel", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
 
 		initEClass(genLinkLabelEClass, GenLinkLabel.class, "GenLinkLabel", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
 		initEReference(getGenLinkLabel_Link(), this.getGenLink(), this.getGenLink_Labels(), "link", null, 1, 1, GenLinkLabel.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEAttribute(getGenLinkLabel_Alignment(), this.getLinkLabelAlignment(), "alignment", "MIDDLE", 0, 1, GenLinkLabel.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 
 		initEClass(elementTypeEClass, ElementType.class, "ElementType", IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
 		initEReference(getElementType_DiagramElement(), this.getGenCommonBase(), this.getGenCommonBase_ElementType(), "diagramElement", null, 1, 1, ElementType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEAttribute(getElementType_UniqueIdentifier(), ecorePackage.getEString(), "uniqueIdentifier", null, 1, 1, ElementType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEAttribute(getElementType_DisplayName(), ecorePackage.getEString(), "displayName", null, 0, 1, ElementType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEAttribute(getElementType_DefinedExternally(), ecorePackage.getEBoolean(), "definedExternally", null, 0, 1, ElementType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 
 		initEClass(metamodelTypeEClass, MetamodelType.class, "MetamodelType", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
 		initEAttribute(getMetamodelType_EditHelperClassName(), ecorePackage.getEString(), "editHelperClassName", null, 0, 1, MetamodelType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 
 		addEOperation(metamodelTypeEClass, ecorePackage.getEString(), "getEditHelperQualifiedClassName", 0, 1, IS_UNIQUE, IS_ORDERED);
 
 		addEOperation(metamodelTypeEClass, theGenModelPackage.getGenClass(), "getMetaClass", 0, 1, IS_UNIQUE, IS_ORDERED);
 
 		initEClass(specializationTypeEClass, SpecializationType.class, "SpecializationType", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
 		initEReference(getSpecializationType_MetamodelType(), this.getMetamodelType(), null, "metamodelType", null, 0, 1, SpecializationType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_COMPOSITE, IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEAttribute(getSpecializationType_EditHelperAdviceClassName(), ecorePackage.getEString(), "editHelperAdviceClassName", null, 0, 1, SpecializationType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 
 		addEOperation(specializationTypeEClass, theGenModelPackage.getGenClass(), "getMetamodelClass", 0, 1, IS_UNIQUE, IS_ORDERED);
 
 		addEOperation(specializationTypeEClass, ecorePackage.getEString(), "getEditHelperAdviceQualifiedClassName", 0, 1, IS_UNIQUE, IS_ORDERED);
 
 		initEClass(notationTypeEClass, NotationType.class, "NotationType", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
 
 		initEClass(modelFacetEClass, ModelFacet.class, "ModelFacet", IS_ABSTRACT, IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
 
 		initEClass(linkModelFacetEClass, LinkModelFacet.class, "LinkModelFacet", IS_ABSTRACT, IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
 
 		addEOperation(linkModelFacetEClass, theGenModelPackage.getGenClass(), "getSourceType", 0, 1, IS_UNIQUE, IS_ORDERED);
 
 		addEOperation(linkModelFacetEClass, theGenModelPackage.getGenClass(), "getTargetType", 0, 1, IS_UNIQUE, IS_ORDERED);
 
 		addEOperation(linkModelFacetEClass, theGenModelPackage.getGenClass(), "getAssistantSourceTypes", 0, -1, IS_UNIQUE, IS_ORDERED);
 
 		addEOperation(linkModelFacetEClass, theGenModelPackage.getGenClass(), "getAssistantTargetTypes", 0, -1, IS_UNIQUE, IS_ORDERED);
 
 		initEClass(labelModelFacetEClass, LabelModelFacet.class, "LabelModelFacet", IS_ABSTRACT, IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
 
 		initEClass(typeModelFacetEClass, TypeModelFacet.class, "TypeModelFacet", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
 		initEReference(getTypeModelFacet_MetaClass(), theGenModelPackage.getGenClass(), null, "metaClass", null, 1, 1, TypeModelFacet.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_COMPOSITE, IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEReference(getTypeModelFacet_ContainmentMetaFeature(), theGenModelPackage.getGenFeature(), null, "containmentMetaFeature", null, 0, 1, TypeModelFacet.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_COMPOSITE, IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEReference(getTypeModelFacet_ChildMetaFeature(), theGenModelPackage.getGenFeature(), null, "childMetaFeature", null, 0, 1, TypeModelFacet.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_COMPOSITE, IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEReference(getTypeModelFacet_ModelElementSelector(), this.getGenConstraint(), null, "modelElementSelector", null, 0, 1, TypeModelFacet.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEReference(getTypeModelFacet_ModelElementInitializer(), this.getGenElementInitializer(), null, "modelElementInitializer", null, 0, 1, TypeModelFacet.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 
 		addEOperation(typeModelFacetEClass, ecorePackage.getEBoolean(), "isPhantomElement", 0, 1, IS_UNIQUE, IS_ORDERED);
 
 		initEClass(typeLinkModelFacetEClass, TypeLinkModelFacet.class, "TypeLinkModelFacet", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
 		initEReference(getTypeLinkModelFacet_SourceMetaFeature(), theGenModelPackage.getGenFeature(), null, "sourceMetaFeature", null, 0, 1, TypeLinkModelFacet.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_COMPOSITE, IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEReference(getTypeLinkModelFacet_TargetMetaFeature(), theGenModelPackage.getGenFeature(), null, "targetMetaFeature", null, 1, 1, TypeLinkModelFacet.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_COMPOSITE, IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 
 		initEClass(featureLinkModelFacetEClass, FeatureLinkModelFacet.class, "FeatureLinkModelFacet", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
 		initEReference(getFeatureLinkModelFacet_MetaFeature(), theGenModelPackage.getGenFeature(), null, "metaFeature", null, 1, 1, FeatureLinkModelFacet.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_COMPOSITE, IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 
 		initEClass(featureLabelModelFacetEClass, FeatureLabelModelFacet.class, "FeatureLabelModelFacet", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
 		initEReference(getFeatureLabelModelFacet_MetaFeatures(), theGenModelPackage.getGenFeature(), null, "metaFeatures", null, 1, -1, FeatureLabelModelFacet.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_COMPOSITE, IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEAttribute(getFeatureLabelModelFacet_ViewPattern(), ecorePackage.getEString(), "viewPattern", null, 0, 1, FeatureLabelModelFacet.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEAttribute(getFeatureLabelModelFacet_EditorPattern(), ecorePackage.getEString(), "editorPattern", null, 0, 1, FeatureLabelModelFacet.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEAttribute(getFeatureLabelModelFacet_EditPattern(), ecorePackage.getEString(), "editPattern", null, 0, 1, FeatureLabelModelFacet.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEAttribute(getFeatureLabelModelFacet_ViewMethod(), this.getLabelTextAccessMethod(), "viewMethod", null, 0, 1, FeatureLabelModelFacet.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEAttribute(getFeatureLabelModelFacet_EditMethod(), this.getLabelTextAccessMethod(), "editMethod", null, 0, 1, FeatureLabelModelFacet.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 
 		initEClass(designLabelModelFacetEClass, DesignLabelModelFacet.class, "DesignLabelModelFacet", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
 
 		initEClass(attributesEClass, Attributes.class, "Attributes", IS_ABSTRACT, IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
 
 		initEClass(colorAttributesEClass, ColorAttributes.class, "ColorAttributes", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
 		initEAttribute(getColorAttributes_ForegroundColor(), ecorePackage.getEString(), "foregroundColor", null, 0, 1, ColorAttributes.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEAttribute(getColorAttributes_BackgroundColor(), ecorePackage.getEString(), "backgroundColor", null, 0, 1, ColorAttributes.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 
 		initEClass(styleAttributesEClass, StyleAttributes.class, "StyleAttributes", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
 		initEAttribute(getStyleAttributes_FixedFont(), ecorePackage.getEBoolean(), "fixedFont", "false", 0, 1, StyleAttributes.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEAttribute(getStyleAttributes_FixedForeground(), ecorePackage.getEBoolean(), "fixedForeground", "false", 0, 1, StyleAttributes.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEAttribute(getStyleAttributes_FixedBackground(), ecorePackage.getEBoolean(), "fixedBackground", "false", 0, 1, StyleAttributes.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 
 		initEClass(resizeConstraintsEClass, ResizeConstraints.class, "ResizeConstraints", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
 		initEAttribute(getResizeConstraints_ResizeHandles(), ecorePackage.getEInt(), "resizeHandles", "0", 0, 1, ResizeConstraints.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEAttribute(getResizeConstraints_NonResizeHandles(), ecorePackage.getEInt(), "nonResizeHandles", "0", 0, 1, ResizeConstraints.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEAttribute(getResizeConstraints_ResizeHandleNames(), ecorePackage.getEString(), "resizeHandleNames", null, 0, -1, ResizeConstraints.class, IS_TRANSIENT, IS_VOLATILE, !IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, IS_DERIVED, IS_ORDERED);
 		initEAttribute(getResizeConstraints_NonResizeHandleNames(), ecorePackage.getEString(), "nonResizeHandleNames", null, 0, -1, ResizeConstraints.class, IS_TRANSIENT, IS_VOLATILE, !IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, IS_DERIVED, IS_ORDERED);
 
 		initEClass(defaultSizeAttributesEClass, DefaultSizeAttributes.class, "DefaultSizeAttributes", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
 		initEAttribute(getDefaultSizeAttributes_Width(), ecorePackage.getEInt(), "width", "40", 0, 1, DefaultSizeAttributes.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEAttribute(getDefaultSizeAttributes_Height(), ecorePackage.getEInt(), "height", "30", 0, 1, DefaultSizeAttributes.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 
 		initEClass(labelOffsetAttributesEClass, LabelOffsetAttributes.class, "LabelOffsetAttributes", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
 		initEAttribute(getLabelOffsetAttributes_X(), ecorePackage.getEInt(), "x", null, 0, 1, LabelOffsetAttributes.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEAttribute(getLabelOffsetAttributes_Y(), ecorePackage.getEInt(), "y", null, 0, 1, LabelOffsetAttributes.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 
 		initEClass(viewmapEClass, Viewmap.class, "Viewmap", IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
 		initEReference(getViewmap_Attributes(), this.getAttributes(), null, "attributes", null, 0, -1, Viewmap.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEAttribute(getViewmap_RequiredPluginIDs(), ecorePackage.getEString(), "requiredPluginIDs", null, 0, -1, Viewmap.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEAttribute(getViewmap_LayoutType(), this.getViewmapLayoutType(), "layoutType", "UNKNOWN", 0, 1, Viewmap.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 
 		op = addEOperation(viewmapEClass, this.getAttributes(), "find", 0, 1, IS_UNIQUE, IS_ORDERED);
 		addEParameter(op, ecorePackage.getEJavaClass(), "attributesClass", 0, 1, IS_UNIQUE, IS_ORDERED);
 
 		initEClass(figureViewmapEClass, FigureViewmap.class, "FigureViewmap", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
 		initEAttribute(getFigureViewmap_FigureQualifiedClassName(), ecorePackage.getEString(), "figureQualifiedClassName", null, 1, 1, FigureViewmap.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 
 		initEClass(snippetViewmapEClass, SnippetViewmap.class, "SnippetViewmap", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
 		initEAttribute(getSnippetViewmap_Body(), ecorePackage.getEString(), "body", null, 0, 1, SnippetViewmap.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 
 		initEClass(innerClassViewmapEClass, InnerClassViewmap.class, "InnerClassViewmap", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
 		initEAttribute(getInnerClassViewmap_ClassName(), ecorePackage.getEString(), "className", null, 0, 1, InnerClassViewmap.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEAttribute(getInnerClassViewmap_ClassBody(), ecorePackage.getEString(), "classBody", null, 0, 1, InnerClassViewmap.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 
 		initEClass(parentAssignedViewmapEClass, ParentAssignedViewmap.class, "ParentAssignedViewmap", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
 		initEAttribute(getParentAssignedViewmap_GetterName(), ecorePackage.getEString(), "getterName", null, 1, 1, ParentAssignedViewmap.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEAttribute(getParentAssignedViewmap_SetterName(), ecorePackage.getEString(), "setterName", null, 0, 1, ParentAssignedViewmap.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEAttribute(getParentAssignedViewmap_FigureQualifiedClassName(), ecorePackage.getEString(), "figureQualifiedClassName", null, 0, 1, ParentAssignedViewmap.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 
 		initEClass(valueExpressionEClass, ValueExpression.class, "ValueExpression", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
 		initEAttribute(getValueExpression_Body(), ecorePackage.getEString(), "body", null, 1, 1, ValueExpression.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEAttribute(getValueExpression_Language(), this.getGenLanguage(), "language", "ocl", 1, 1, ValueExpression.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEAttribute(getValueExpression_LangName(), ecorePackage.getEString(), "langName", null, 0, 1, ValueExpression.class, IS_TRANSIENT, IS_VOLATILE, !IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, IS_DERIVED, IS_ORDERED);
 
 		addEOperation(valueExpressionEClass, ecorePackage.getEString(), "getBodyString", 0, 1, IS_UNIQUE, IS_ORDERED);
 
 		initEClass(genConstraintEClass, GenConstraint.class, "GenConstraint", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
 
 		initEClass(paletteEClass, Palette.class, "Palette", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
 		initEReference(getPalette_Diagram(), this.getGenDiagram(), this.getGenDiagram_Palette(), "diagram", null, 1, 1, Palette.class, !IS_TRANSIENT, !IS_VOLATILE, !IS_CHANGEABLE, !IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEAttribute(getPalette_Flyout(), ecorePackage.getEBoolean(), "flyout", "true", 0, 1, Palette.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEReference(getPalette_Groups(), this.getToolGroup(), this.getToolGroup_Palette(), "groups", null, 1, -1, Palette.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEAttribute(getPalette_PackageName(), ecorePackage.getEString(), "packageName", null, 0, 1, Palette.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEAttribute(getPalette_FactoryClassName(), ecorePackage.getEString(), "factoryClassName", null, 0, 1, Palette.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 
 		addEOperation(paletteEClass, ecorePackage.getEString(), "getFactoryQualifiedClassName", 0, 1, IS_UNIQUE, IS_ORDERED);
 
 		addEOperation(paletteEClass, ecorePackage.getEBoolean(), "definesStandardTools", 0, 1, IS_UNIQUE, IS_ORDERED);
 
 		initEClass(entryBaseEClass, EntryBase.class, "EntryBase", IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
 		initEAttribute(getEntryBase_Title(), ecorePackage.getEString(), "title", null, 0, 1, EntryBase.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEAttribute(getEntryBase_Description(), ecorePackage.getEString(), "description", null, 0, 1, EntryBase.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEAttribute(getEntryBase_LargeIconPath(), ecorePackage.getEString(), "largeIconPath", null, 0, 1, EntryBase.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEAttribute(getEntryBase_SmallIconPath(), ecorePackage.getEString(), "smallIconPath", null, 0, 1, EntryBase.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEAttribute(getEntryBase_CreateMethodName(), ecorePackage.getEString(), "createMethodName", null, 0, 1, EntryBase.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 
 		initEClass(abstractToolEntryEClass, AbstractToolEntry.class, "AbstractToolEntry", IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
 		initEAttribute(getAbstractToolEntry_Default(), ecorePackage.getEBoolean(), "default", "false", 0, 1, AbstractToolEntry.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEAttribute(getAbstractToolEntry_QualifiedToolName(), ecorePackage.getEString(), "qualifiedToolName", null, 0, 1, AbstractToolEntry.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEReference(getAbstractToolEntry_Properties(), ecorePackage.getEStringToStringMapEntry(), null, "properties", null, 0, -1, AbstractToolEntry.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 
 		initEClass(toolEntryEClass, ToolEntry.class, "ToolEntry", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
 		initEReference(getToolEntry_GenNodes(), this.getGenNode(), null, "genNodes", null, 0, -1, ToolEntry.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_COMPOSITE, IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEReference(getToolEntry_GenLinks(), this.getGenLink(), null, "genLinks", null, 0, -1, ToolEntry.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_COMPOSITE, IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEReference(getToolEntry_Elements(), this.getGenCommonBase(), null, "elements", null, 0, -1, ToolEntry.class, IS_TRANSIENT, IS_VOLATILE, !IS_CHANGEABLE, !IS_COMPOSITE, IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, IS_DERIVED, IS_ORDERED);
 
 		initEClass(standardEntryEClass, StandardEntry.class, "StandardEntry", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
 		initEAttribute(getStandardEntry_Kind(), this.getStandardEntryKind(), "kind", null, 1, 1, StandardEntry.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 
 		initEClass(toolGroupItemEClass, ToolGroupItem.class, "ToolGroupItem", IS_ABSTRACT, IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
 		initEReference(getToolGroupItem_Group(), this.getToolGroup(), this.getToolGroup_Entries(), "group", null, 0, 1, ToolGroupItem.class, !IS_TRANSIENT, !IS_VOLATILE, !IS_CHANGEABLE, !IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 
 		initEClass(separatorEClass, Separator.class, "Separator", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
 
 		initEClass(toolGroupEClass, ToolGroup.class, "ToolGroup", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
 		initEReference(getToolGroup_Palette(), this.getPalette(), this.getPalette_Groups(), "palette", null, 1, 1, ToolGroup.class, !IS_TRANSIENT, !IS_VOLATILE, !IS_CHANGEABLE, !IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEAttribute(getToolGroup_Stack(), ecorePackage.getEBoolean(), "stack", null, 0, 1, ToolGroup.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEAttribute(getToolGroup_Collapse(), ecorePackage.getEBoolean(), "collapse", null, 0, 1, ToolGroup.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEReference(getToolGroup_Entries(), this.getToolGroupItem(), this.getToolGroupItem_Group(), "entries", null, 1, -1, ToolGroup.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEAttribute(getToolGroup_ToolsOnly(), ecorePackage.getEBoolean(), "toolsOnly", null, 0, 1, ToolGroup.class, IS_TRANSIENT, IS_VOLATILE, !IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, IS_DERIVED, IS_ORDERED);
 
 		initEClass(genElementInitializerEClass, GenElementInitializer.class, "GenElementInitializer", IS_ABSTRACT, IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
 		initEReference(getGenElementInitializer_TypeModelFacet(), this.getTypeModelFacet(), null, "typeModelFacet", null, 1, 1, GenElementInitializer.class, IS_TRANSIENT, IS_VOLATILE, !IS_CHANGEABLE, !IS_COMPOSITE, IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 
 		initEClass(genFeatureSeqInitializerEClass, GenFeatureSeqInitializer.class, "GenFeatureSeqInitializer", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
 		initEReference(getGenFeatureSeqInitializer_Initializers(), this.getGenFeatureInitializer(), this.getGenFeatureInitializer_FeatureSeqInitializer(), "initializers", null, 1, -1, GenFeatureSeqInitializer.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEReference(getGenFeatureSeqInitializer_ElementClass(), theGenModelPackage.getGenClass(), null, "elementClass", null, 0, 1, GenFeatureSeqInitializer.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_COMPOSITE, IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEReference(getGenFeatureSeqInitializer_CreatingInitializer(), this.getGenReferenceNewElementSpec(), this.getGenReferenceNewElementSpec_NewElementInitializers(), "creatingInitializer", null, 0, 1, GenFeatureSeqInitializer.class, !IS_TRANSIENT, !IS_VOLATILE, !IS_CHANGEABLE, !IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 
 		addEOperation(genFeatureSeqInitializerEClass, ecorePackage.getEString(), "getElementClassAccessorName", 1, 1, IS_UNIQUE, IS_ORDERED);
 
 		addEOperation(genFeatureSeqInitializerEClass, ecorePackage.getEString(), "getElementQualifiedPackageInterfaceName", 1, 1, IS_UNIQUE, IS_ORDERED);
 
 		initEClass(genFeatureValueSpecEClass, GenFeatureValueSpec.class, "GenFeatureValueSpec", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
 
 		initEClass(genReferenceNewElementSpecEClass, GenReferenceNewElementSpec.class, "GenReferenceNewElementSpec", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
 		initEReference(getGenReferenceNewElementSpec_NewElementInitializers(), this.getGenFeatureSeqInitializer(), this.getGenFeatureSeqInitializer_CreatingInitializer(), "newElementInitializers", null, 1, -1, GenReferenceNewElementSpec.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 
 		initEClass(genFeatureInitializerEClass, GenFeatureInitializer.class, "GenFeatureInitializer", IS_ABSTRACT, IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
 		initEReference(getGenFeatureInitializer_Feature(), theGenModelPackage.getGenFeature(), null, "feature", null, 1, 1, GenFeatureInitializer.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_COMPOSITE, IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEReference(getGenFeatureInitializer_FeatureSeqInitializer(), this.getGenFeatureSeqInitializer(), this.getGenFeatureSeqInitializer_Initializers(), "featureSeqInitializer", null, 1, 1, GenFeatureInitializer.class, !IS_TRANSIENT, !IS_VOLATILE, !IS_CHANGEABLE, !IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 
 		addEOperation(genFeatureInitializerEClass, ecorePackage.getEString(), "getFeatureQualifiedPackageInterfaceName", 1, 1, IS_UNIQUE, IS_ORDERED);
 
 		initEClass(genLinkConstraintsEClass, GenLinkConstraints.class, "GenLinkConstraints", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
 		initEReference(getGenLinkConstraints_Link(), this.getGenLink(), this.getGenLink_CreationConstraints(), "link", null, 1, 1, GenLinkConstraints.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEReference(getGenLinkConstraints_SourceEnd(), this.getGenConstraint(), null, "sourceEnd", null, 0, 1, GenLinkConstraints.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEReference(getGenLinkConstraints_TargetEnd(), this.getGenConstraint(), null, "targetEnd", null, 0, 1, GenLinkConstraints.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 
 		addEOperation(genLinkConstraintsEClass, theGenModelPackage.getGenClass(), "getLinkClass", 0, 1, IS_UNIQUE, IS_ORDERED);
 
 		addEOperation(genLinkConstraintsEClass, theGenModelPackage.getGenClass(), "getSourceEndContextClass", 0, 1, IS_UNIQUE, IS_ORDERED);
 
 		addEOperation(genLinkConstraintsEClass, theGenModelPackage.getGenClass(), "getTargetEndContextClass", 0, 1, IS_UNIQUE, IS_ORDERED);
 
 		addEOperation(genLinkConstraintsEClass, ecorePackage.getEString(), "getConstraintsInstanceFieldName", 1, 1, IS_UNIQUE, IS_ORDERED);
 
 		addEOperation(genLinkConstraintsEClass, ecorePackage.getEBoolean(), "isValid", 0, 1, IS_UNIQUE, IS_ORDERED);
 
 		initEClass(genAuditRootEClass, GenAuditRoot.class, "GenAuditRoot", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
 		initEReference(getGenAuditRoot_EditorGen(), this.getGenEditorGenerator(), this.getGenEditorGenerator_Audits(), "editorGen", null, 1, 1, GenAuditRoot.class, !IS_TRANSIENT, !IS_VOLATILE, !IS_CHANGEABLE, !IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEReference(getGenAuditRoot_Categories(), this.getGenAuditContainer(), this.getGenAuditContainer_Root(), "categories", null, 0, -1, GenAuditRoot.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEReference(getGenAuditRoot_Rules(), this.getGenAuditRule(), this.getGenAuditRule_Root(), "rules", null, 0, -1, GenAuditRoot.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 
 		initEClass(genAuditContainerEClass, GenAuditContainer.class, "GenAuditContainer", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
 		initEReference(getGenAuditContainer_Root(), this.getGenAuditRoot(), this.getGenAuditRoot_Categories(), "root", null, 1, 1, GenAuditContainer.class, !IS_TRANSIENT, !IS_VOLATILE, !IS_CHANGEABLE, !IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEAttribute(getGenAuditContainer_Id(), ecorePackage.getEString(), "id", null, 1, 1, GenAuditContainer.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEAttribute(getGenAuditContainer_Name(), ecorePackage.getEString(), "name", null, 0, 1, GenAuditContainer.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEAttribute(getGenAuditContainer_Description(), ecorePackage.getEString(), "description", null, 0, 1, GenAuditContainer.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEReference(getGenAuditContainer_Path(), this.getGenAuditContainer(), null, "path", null, 1, -1, GenAuditContainer.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_COMPOSITE, IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEReference(getGenAuditContainer_Audits(), this.getGenAuditRule(), this.getGenAuditRule_Category(), "audits", null, 0, -1, GenAuditContainer.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_COMPOSITE, IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 
 		initEClass(genRuleBaseEClass, GenRuleBase.class, "GenRuleBase", IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
 		initEAttribute(getGenRuleBase_Name(), ecorePackage.getEString(), "name", null, 0, 1, GenRuleBase.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEAttribute(getGenRuleBase_Description(), ecorePackage.getEString(), "description", null, 0, 1, GenRuleBase.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 
 		initEClass(genAuditRuleEClass, GenAuditRule.class, "GenAuditRule", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
 		initEReference(getGenAuditRule_Root(), this.getGenAuditRoot(), this.getGenAuditRoot_Rules(), "root", null, 1, 1, GenAuditRule.class, !IS_TRANSIENT, !IS_VOLATILE, !IS_CHANGEABLE, !IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEAttribute(getGenAuditRule_ContextSelectorLocalClassName(), ecorePackage.getEString(), "contextSelectorLocalClassName", null, 0, 1, GenAuditRule.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEAttribute(getGenAuditRule_Id(), ecorePackage.getEString(), "id", null, 1, 1, GenAuditRule.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEReference(getGenAuditRule_Rule(), this.getGenConstraint(), null, "rule", null, 1, 1, GenAuditRule.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEReference(getGenAuditRule_Target(), this.getGenAuditable(), null, "target", null, 1, 1, GenAuditRule.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEAttribute(getGenAuditRule_Message(), ecorePackage.getEString(), "message", null, 0, 1, GenAuditRule.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEAttribute(getGenAuditRule_Severity(), this.getGenSeverity(), "severity", "ERROR", 0, 1, GenAuditRule.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEAttribute(getGenAuditRule_UseInLiveMode(), ecorePackage.getEBoolean(), "useInLiveMode", "false", 0, 1, GenAuditRule.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEAttribute(getGenAuditRule_RequiresConstraintAdapter(), ecorePackage.getEBoolean(), "requiresConstraintAdapter", null, 0, 1, GenAuditRule.class, IS_TRANSIENT, IS_VOLATILE, !IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, IS_DERIVED, IS_ORDERED);
 		initEReference(getGenAuditRule_Category(), this.getGenAuditContainer(), this.getGenAuditContainer_Audits(), "category", null, 1, 1, GenAuditRule.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_COMPOSITE, IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 
 		addEOperation(genAuditRuleEClass, ecorePackage.getEString(), "getContextSelectorClassName", 1, 1, IS_UNIQUE, IS_ORDERED);
 
 		addEOperation(genAuditRuleEClass, ecorePackage.getEString(), "getContextSelectorQualifiedClassName", 1, 1, IS_UNIQUE, IS_ORDERED);
 
 		addEOperation(genAuditRuleEClass, ecorePackage.getEString(), "getConstraintAdapterQualifiedClassName", 0, 1, IS_UNIQUE, IS_ORDERED);
 
 		initEClass(genRuleTargetEClass, GenRuleTarget.class, "GenRuleTarget", IS_ABSTRACT, IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
 
 		addEOperation(genRuleTargetEClass, theGenModelPackage.getGenClassifier(), "getContext", 1, 1, IS_UNIQUE, IS_ORDERED);
 
 		initEClass(genDomainElementTargetEClass, GenDomainElementTarget.class, "GenDomainElementTarget", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
 		initEReference(getGenDomainElementTarget_Element(), theGenModelPackage.getGenClass(), null, "element", null, 1, 1, GenDomainElementTarget.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_COMPOSITE, IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 
 		initEClass(genDiagramElementTargetEClass, GenDiagramElementTarget.class, "GenDiagramElementTarget", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
 		initEReference(getGenDiagramElementTarget_Element(), this.getGenCommonBase(), null, "element", null, 1, -1, GenDiagramElementTarget.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_COMPOSITE, IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 
 		initEClass(genDomainAttributeTargetEClass, GenDomainAttributeTarget.class, "GenDomainAttributeTarget", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
 		initEReference(getGenDomainAttributeTarget_Attribute(), theGenModelPackage.getGenFeature(), null, "attribute", null, 1, 1, GenDomainAttributeTarget.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_COMPOSITE, IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEAttribute(getGenDomainAttributeTarget_NullAsError(), ecorePackage.getEBoolean(), "nullAsError", null, 0, 1, GenDomainAttributeTarget.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 
 		initEClass(genNotationElementTargetEClass, GenNotationElementTarget.class, "GenNotationElementTarget", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
 		initEReference(getGenNotationElementTarget_Element(), theGenModelPackage.getGenClass(), null, "element", null, 1, 1, GenNotationElementTarget.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_COMPOSITE, IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 
 		initEClass(genMetricContainerEClass, GenMetricContainer.class, "GenMetricContainer", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
 		initEReference(getGenMetricContainer_EditorGen(), this.getGenEditorGenerator(), this.getGenEditorGenerator_Metrics(), "editorGen", null, 1, 1, GenMetricContainer.class, !IS_TRANSIENT, !IS_VOLATILE, !IS_CHANGEABLE, !IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEReference(getGenMetricContainer_Metrics(), this.getGenMetricRule(), this.getGenMetricRule_Container(), "metrics", null, 1, -1, GenMetricContainer.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 
 		initEClass(genMetricRuleEClass, GenMetricRule.class, "GenMetricRule", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
 		initEAttribute(getGenMetricRule_Key(), ecorePackage.getEString(), "key", null, 1, 1, GenMetricRule.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEReference(getGenMetricRule_Rule(), this.getValueExpression(), null, "rule", null, 1, 1, GenMetricRule.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEReference(getGenMetricRule_Target(), this.getGenMeasurable(), null, "target", null, 1, 1, GenMetricRule.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEAttribute(getGenMetricRule_LowLimit(), ecorePackage.getEDoubleObject(), "lowLimit", null, 0, 1, GenMetricRule.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEAttribute(getGenMetricRule_HighLimit(), ecorePackage.getEDoubleObject(), "highLimit", null, 0, 1, GenMetricRule.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEReference(getGenMetricRule_Container(), this.getGenMetricContainer(), this.getGenMetricContainer_Metrics(), "container", null, 1, 1, GenMetricRule.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 
 		initEClass(genAuditedMetricTargetEClass, GenAuditedMetricTarget.class, "GenAuditedMetricTarget", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
 		initEReference(getGenAuditedMetricTarget_Metric(), this.getGenMetricRule(), null, "metric", null, 1, 1, GenAuditedMetricTarget.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_COMPOSITE, IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEReference(getGenAuditedMetricTarget_MetricValueContext(), theGenModelPackage.getGenDataType(), null, "metricValueContext", null, 1, 1, GenAuditedMetricTarget.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_COMPOSITE, IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 
 		initEClass(genAuditableEClass, GenAuditable.class, "GenAuditable", IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
 
 		addEOperation(genAuditableEClass, ecorePackage.getEString(), "getClientContextID", 1, 1, IS_UNIQUE, IS_ORDERED);
 
 		addEOperation(genAuditableEClass, theGenModelPackage.getGenClass(), "getTargetClass", 0, 1, IS_UNIQUE, IS_ORDERED);
 
 		addEOperation(genAuditableEClass, ecorePackage.getEString(), "getTargetClassModelQualifiedName", 0, 1, IS_UNIQUE, IS_ORDERED);
 
 		initEClass(genMeasurableEClass, GenMeasurable.class, "GenMeasurable", IS_ABSTRACT, IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
 
 		initEClass(genExpressionProviderContainerEClass, GenExpressionProviderContainer.class, "GenExpressionProviderContainer", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
 		initEAttribute(getGenExpressionProviderContainer_ExpressionsPackageName(), ecorePackage.getEString(), "expressionsPackageName", null, 0, 1, GenExpressionProviderContainer.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEAttribute(getGenExpressionProviderContainer_AbstractExpressionClassName(), ecorePackage.getEString(), "abstractExpressionClassName", null, 0, 1, GenExpressionProviderContainer.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEReference(getGenExpressionProviderContainer_Providers(), this.getGenExpressionProviderBase(), this.getGenExpressionProviderBase_Container(), "providers", null, 0, -1, GenExpressionProviderContainer.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEReference(getGenExpressionProviderContainer_EditorGen(), this.getGenEditorGenerator(), this.getGenEditorGenerator_ExpressionProviders(), "editorGen", null, 1, 1, GenExpressionProviderContainer.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 
 		op = addEOperation(genExpressionProviderContainerEClass, this.getGenExpressionProviderBase(), "getProvider", 0, 1, IS_UNIQUE, IS_ORDERED);
 		addEParameter(op, this.getValueExpression(), "expression", 1, 1, IS_UNIQUE, IS_ORDERED);
 
 		addEOperation(genExpressionProviderContainerEClass, ecorePackage.getEString(), "getAbstractExpressionQualifiedClassName", 1, 1, IS_UNIQUE, IS_ORDERED);
 
 		op = addEOperation(genExpressionProviderContainerEClass, ecorePackage.getEBoolean(), "isCopy", 0, 1, IS_UNIQUE, IS_ORDERED);
 		addEParameter(op, this.getValueExpression(), "expression", 1, 1, IS_UNIQUE, IS_ORDERED);
 
 		initEClass(genExpressionProviderBaseEClass, GenExpressionProviderBase.class, "GenExpressionProviderBase", IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
 		initEReference(getGenExpressionProviderBase_Expressions(), this.getValueExpression(), null, "expressions", null, 0, -1, GenExpressionProviderBase.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_COMPOSITE, IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEReference(getGenExpressionProviderBase_Container(), this.getGenExpressionProviderContainer(), this.getGenExpressionProviderContainer_Providers(), "container", null, 1, 1, GenExpressionProviderBase.class, !IS_TRANSIENT, !IS_VOLATILE, !IS_CHANGEABLE, !IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 
 		addEOperation(genExpressionProviderBaseEClass, this.getGenLanguage(), "getLanguage", 1, 1, IS_UNIQUE, IS_ORDERED);
 
 		op = addEOperation(genExpressionProviderBaseEClass, ecorePackage.getEString(), "getQualifiedInstanceClassName", 0, 1, IS_UNIQUE, IS_ORDERED);
 		addEParameter(op, theGenModelPackage.getGenClassifier(), "genClassifier", 0, 1, IS_UNIQUE, IS_ORDERED);
 
 		op = addEOperation(genExpressionProviderBaseEClass, ecorePackage.getEString(), "getQualifiedTypeInstanceClassName", 0, 1, IS_UNIQUE, IS_ORDERED);
 		addEParameter(op, theGenModelPackage.getGenTypedElement(), "genTypedElement", 0, 1, IS_UNIQUE, IS_ORDERED);
 
 		initEClass(genJavaExpressionProviderEClass, GenJavaExpressionProvider.class, "GenJavaExpressionProvider", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
 
 		op = addEOperation(genJavaExpressionProviderEClass, ecorePackage.getEString(), "getOperationName", 0, 1, IS_UNIQUE, IS_ORDERED);
 		addEParameter(op, this.getValueExpression(), "expression", 1, 1, IS_UNIQUE, IS_ORDERED);
 
 		initEClass(genExpressionInterpreterEClass, GenExpressionInterpreter.class, "GenExpressionInterpreter", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
 		initEAttribute(getGenExpressionInterpreter_Language(), this.getGenLanguage(), "language", null, 1, 1, GenExpressionInterpreter.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEAttribute(getGenExpressionInterpreter_ClassName(), ecorePackage.getEString(), "className", null, 0, 1, GenExpressionInterpreter.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 
 		addEOperation(genExpressionInterpreterEClass, ecorePackage.getEString(), "getQualifiedClassName", 1, 1, IS_UNIQUE, IS_ORDERED);
 
 		op = addEOperation(genExpressionInterpreterEClass, ecorePackage.getEString(), "getExpressionAccessor", 0, 1, IS_UNIQUE, IS_ORDERED);
 		addEParameter(op, this.getValueExpression(), "expression", 1, 1, IS_UNIQUE, IS_ORDERED);
 
 		initEClass(genDomainModelNavigatorEClass, GenDomainModelNavigator.class, "GenDomainModelNavigator", IS_ABSTRACT, IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
 		initEAttribute(getGenDomainModelNavigator_GenerateDomainModelNavigator(), ecorePackage.getEBoolean(), "generateDomainModelNavigator", "true", 0, 1, GenDomainModelNavigator.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEAttribute(getGenDomainModelNavigator_DomainContentExtensionID(), ecorePackage.getEString(), "domainContentExtensionID", null, 0, 1, GenDomainModelNavigator.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEAttribute(getGenDomainModelNavigator_DomainContentExtensionName(), ecorePackage.getEString(), "domainContentExtensionName", null, 0, 1, GenDomainModelNavigator.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEAttribute(getGenDomainModelNavigator_DomainContentExtensionPriority(), ecorePackage.getEString(), "domainContentExtensionPriority", null, 0, 1, GenDomainModelNavigator.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEAttribute(getGenDomainModelNavigator_DomainContentProviderClassName(), ecorePackage.getEString(), "domainContentProviderClassName", null, 0, 1, GenDomainModelNavigator.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEAttribute(getGenDomainModelNavigator_DomainLabelProviderClassName(), ecorePackage.getEString(), "domainLabelProviderClassName", null, 0, 1, GenDomainModelNavigator.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEAttribute(getGenDomainModelNavigator_DomainModelElementTesterClassName(), ecorePackage.getEString(), "domainModelElementTesterClassName", null, 0, 1, GenDomainModelNavigator.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEAttribute(getGenDomainModelNavigator_DomainNavigatorItemClassName(), ecorePackage.getEString(), "domainNavigatorItemClassName", null, 0, 1, GenDomainModelNavigator.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 
 		addEOperation(genDomainModelNavigatorEClass, ecorePackage.getEString(), "getDomainContentProviderQualifiedClassName", 0, 1, IS_UNIQUE, IS_ORDERED);
 
 		addEOperation(genDomainModelNavigatorEClass, ecorePackage.getEString(), "getDomainLabelProviderQualifiedClassName", 0, 1, IS_UNIQUE, IS_ORDERED);
 
 		addEOperation(genDomainModelNavigatorEClass, ecorePackage.getEString(), "getDomainModelElementTesterQualifiedClassName", 0, 1, IS_UNIQUE, IS_ORDERED);
 
 		addEOperation(genDomainModelNavigatorEClass, ecorePackage.getEString(), "getDomainNavigatorItemQualifiedClassName", 0, 1, IS_UNIQUE, IS_ORDERED);
 
 		initEClass(genNavigatorEClass, GenNavigator.class, "GenNavigator", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
 		initEReference(getGenNavigator_EditorGen(), this.getGenEditorGenerator(), this.getGenEditorGenerator_Navigator(), "editorGen", null, 0, 1, GenNavigator.class, !IS_TRANSIENT, !IS_VOLATILE, !IS_CHANGEABLE, !IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEAttribute(getGenNavigator_ContentExtensionID(), ecorePackage.getEString(), "contentExtensionID", null, 0, 1, GenNavigator.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEAttribute(getGenNavigator_ContentExtensionName(), ecorePackage.getEString(), "contentExtensionName", null, 0, 1, GenNavigator.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEAttribute(getGenNavigator_ContentExtensionPriority(), ecorePackage.getEString(), "contentExtensionPriority", null, 0, 1, GenNavigator.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEAttribute(getGenNavigator_LinkHelperExtensionID(), ecorePackage.getEString(), "linkHelperExtensionID", null, 0, 1, GenNavigator.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEAttribute(getGenNavigator_SorterExtensionID(), ecorePackage.getEString(), "sorterExtensionID", null, 0, 1, GenNavigator.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEAttribute(getGenNavigator_ActionProviderID(), ecorePackage.getEString(), "actionProviderID", null, 0, 1, GenNavigator.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEAttribute(getGenNavigator_ContentProviderClassName(), ecorePackage.getEString(), "contentProviderClassName", null, 0, 1, GenNavigator.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEAttribute(getGenNavigator_LabelProviderClassName(), ecorePackage.getEString(), "labelProviderClassName", null, 0, 1, GenNavigator.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEAttribute(getGenNavigator_LinkHelperClassName(), ecorePackage.getEString(), "linkHelperClassName", null, 0, 1, GenNavigator.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEAttribute(getGenNavigator_SorterClassName(), ecorePackage.getEString(), "sorterClassName", null, 0, 1, GenNavigator.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEAttribute(getGenNavigator_ActionProviderClassName(), ecorePackage.getEString(), "actionProviderClassName", null, 0, 1, GenNavigator.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEAttribute(getGenNavigator_AbstractNavigatorItemClassName(), ecorePackage.getEString(), "abstractNavigatorItemClassName", null, 0, 1, GenNavigator.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEAttribute(getGenNavigator_NavigatorGroupClassName(), ecorePackage.getEString(), "navigatorGroupClassName", null, 0, 1, GenNavigator.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEAttribute(getGenNavigator_NavigatorItemClassName(), ecorePackage.getEString(), "navigatorItemClassName", null, 0, 1, GenNavigator.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEAttribute(getGenNavigator_UriInputTesterClassName(), ecorePackage.getEString(), "uriInputTesterClassName", null, 0, 1, GenNavigator.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEAttribute(getGenNavigator_PackageName(), ecorePackage.getEString(), "packageName", null, 0, 1, GenNavigator.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEReference(getGenNavigator_ChildReferences(), this.getGenNavigatorChildReference(), this.getGenNavigatorChildReference_Navigator(), "childReferences", null, 0, -1, GenNavigator.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 
 		addEOperation(genNavigatorEClass, ecorePackage.getEString(), "getContentProviderQualifiedClassName", 0, 1, IS_UNIQUE, IS_ORDERED);
 
 		addEOperation(genNavigatorEClass, ecorePackage.getEString(), "getLabelProviderQualifiedClassName", 0, 1, IS_UNIQUE, IS_ORDERED);
 
 		addEOperation(genNavigatorEClass, ecorePackage.getEString(), "getLinkHelperQualifiedClassName", 0, 1, IS_UNIQUE, IS_ORDERED);
 
 		addEOperation(genNavigatorEClass, ecorePackage.getEString(), "getSorterQualifiedClassName", 0, 1, IS_UNIQUE, IS_ORDERED);
 
 		addEOperation(genNavigatorEClass, ecorePackage.getEString(), "getActionProviderQualifiedClassName", 0, 1, IS_UNIQUE, IS_ORDERED);
 
 		addEOperation(genNavigatorEClass, ecorePackage.getEString(), "getAbstractNavigatorItemQualifiedClassName", 0, 1, IS_UNIQUE, IS_ORDERED);
 
 		addEOperation(genNavigatorEClass, ecorePackage.getEString(), "getNavigatorGroupQualifiedClassName", 0, 1, IS_UNIQUE, IS_ORDERED);
 
 		addEOperation(genNavigatorEClass, ecorePackage.getEString(), "getNavigatorItemQualifiedClassName", 0, 1, IS_UNIQUE, IS_ORDERED);
 
 		addEOperation(genNavigatorEClass, ecorePackage.getEString(), "getUriInputTesterQualifiedClassName", 0, 1, IS_UNIQUE, IS_ORDERED);
 
 		op = addEOperation(genNavigatorEClass, this.getGenNavigatorChildReference(), "getChildReferencesTo", 0, -1, IS_UNIQUE, IS_ORDERED);
 		addEParameter(op, this.getGenCommonBase(), "child", 0, 1, IS_UNIQUE, IS_ORDERED);
 
 		initEClass(genNavigatorChildReferenceEClass, GenNavigatorChildReference.class, "GenNavigatorChildReference", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
 		initEReference(getGenNavigatorChildReference_Navigator(), this.getGenNavigator(), this.getGenNavigator_ChildReferences(), "navigator", null, 0, 1, GenNavigatorChildReference.class, !IS_TRANSIENT, !IS_VOLATILE, !IS_CHANGEABLE, !IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEReference(getGenNavigatorChildReference_Parent(), this.getGenCommonBase(), null, "parent", null, 0, 1, GenNavigatorChildReference.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_COMPOSITE, IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEReference(getGenNavigatorChildReference_Child(), this.getGenCommonBase(), null, "child", null, 1, 1, GenNavigatorChildReference.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_COMPOSITE, IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEAttribute(getGenNavigatorChildReference_ReferenceType(), this.getGenNavigatorReferenceType(), "referenceType", null, 1, 1, GenNavigatorChildReference.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEAttribute(getGenNavigatorChildReference_GroupName(), ecorePackage.getEString(), "groupName", null, 0, 1, GenNavigatorChildReference.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEAttribute(getGenNavigatorChildReference_GroupIcon(), ecorePackage.getEString(), "groupIcon", null, 0, 1, GenNavigatorChildReference.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEAttribute(getGenNavigatorChildReference_HideIfEmpty(), ecorePackage.getEBoolean(), "hideIfEmpty", "true", 0, 1, GenNavigatorChildReference.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 
 		addEOperation(genNavigatorChildReferenceEClass, ecorePackage.getEBoolean(), "isInsideGroup", 0, 1, IS_UNIQUE, IS_ORDERED);
 
 		addEOperation(genNavigatorChildReferenceEClass, this.getGenNavigatorPath(), "findConnectionPaths", 0, -1, IS_UNIQUE, IS_ORDERED);
 
 		initEClass(genNavigatorPathEClass, GenNavigatorPath.class, "GenNavigatorPath", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
 		initEReference(getGenNavigatorPath_Segments(), this.getGenNavigatorPathSegment(), this.getGenNavigatorPathSegment_Path(), "segments", null, 0, -1, GenNavigatorPath.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 
 		initEClass(genNavigatorPathSegmentEClass, GenNavigatorPathSegment.class, "GenNavigatorPathSegment", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
 		initEReference(getGenNavigatorPathSegment_Path(), this.getGenNavigatorPath(), this.getGenNavigatorPath_Segments(), "path", null, 0, 1, GenNavigatorPathSegment.class, !IS_TRANSIENT, !IS_VOLATILE, !IS_CHANGEABLE, !IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEReference(getGenNavigatorPathSegment_From(), this.getGenCommonBase(), null, "from", null, 1, 1, GenNavigatorPathSegment.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_COMPOSITE, IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEReference(getGenNavigatorPathSegment_To(), this.getGenCommonBase(), null, "to", null, 1, 1, GenNavigatorPathSegment.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_COMPOSITE, IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 
 		initEClass(genPropertySheetEClass, GenPropertySheet.class, "GenPropertySheet", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
 		initEReference(getGenPropertySheet_EditorGen(), this.getGenEditorGenerator(), this.getGenEditorGenerator_PropertySheet(), "editorGen", null, 1, 1, GenPropertySheet.class, !IS_TRANSIENT, !IS_VOLATILE, !IS_CHANGEABLE, !IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEReference(getGenPropertySheet_Tabs(), this.getGenPropertyTab(), this.getGenPropertyTab_Sheet(), "tabs", null, 1, -1, GenPropertySheet.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEAttribute(getGenPropertySheet_PackageName(), ecorePackage.getEString(), "packageName", null, 0, 1, GenPropertySheet.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEAttribute(getGenPropertySheet_ReadOnly(), ecorePackage.getEBoolean(), "readOnly", "false", 0, 1, GenPropertySheet.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEAttribute(getGenPropertySheet_NeedsCaption(), ecorePackage.getEBoolean(), "needsCaption", "true", 0, 1, GenPropertySheet.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEAttribute(getGenPropertySheet_LabelProviderClassName(), ecorePackage.getEString(), "labelProviderClassName", null, 0, 1, GenPropertySheet.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 
 		addEOperation(genPropertySheetEClass, ecorePackage.getEString(), "getLabelProviderQualifiedClassName", 0, 1, IS_UNIQUE, IS_ORDERED);
 
 		initEClass(genPropertyTabEClass, GenPropertyTab.class, "GenPropertyTab", IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
 		initEReference(getGenPropertyTab_Sheet(), this.getGenPropertySheet(), this.getGenPropertySheet_Tabs(), "sheet", null, 1, 1, GenPropertyTab.class, !IS_TRANSIENT, !IS_VOLATILE, !IS_CHANGEABLE, !IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEAttribute(getGenPropertyTab_ID(), ecorePackage.getEString(), "iD", null, 1, 1, GenPropertyTab.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEAttribute(getGenPropertyTab_Label(), ecorePackage.getEString(), "label", null, 0, 1, GenPropertyTab.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 
 		initEClass(genStandardPropertyTabEClass, GenStandardPropertyTab.class, "GenStandardPropertyTab", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
 
 		initEClass(genCustomPropertyTabEClass, GenCustomPropertyTab.class, "GenCustomPropertyTab", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
 		initEAttribute(getGenCustomPropertyTab_ClassName(), ecorePackage.getEString(), "className", null, 1, 1, GenCustomPropertyTab.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEReference(getGenCustomPropertyTab_Filter(), this.getGenPropertyTabFilter(), this.getGenPropertyTabFilter_Tab(), "filter", null, 0, 1, GenCustomPropertyTab.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 
 		addEOperation(genCustomPropertyTabEClass, ecorePackage.getEString(), "getQualifiedClassName", 0, 1, IS_UNIQUE, IS_ORDERED);
 
 		initEClass(genPropertyTabFilterEClass, GenPropertyTabFilter.class, "GenPropertyTabFilter", IS_ABSTRACT, IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
 		initEReference(getGenPropertyTabFilter_Tab(), this.getGenCustomPropertyTab(), this.getGenCustomPropertyTab_Filter(), "tab", null, 1, 1, GenPropertyTabFilter.class, !IS_TRANSIENT, !IS_VOLATILE, !IS_CHANGEABLE, !IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 
 		initEClass(typeTabFilterEClass, TypeTabFilter.class, "TypeTabFilter", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
 		initEAttribute(getTypeTabFilter_Types(), ecorePackage.getEString(), "types", null, 1, -1, TypeTabFilter.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEAttribute(getTypeTabFilter_GeneratedTypes(), this.getGeneratedType(), "generatedTypes", null, 0, -1, TypeTabFilter.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 
 		addEOperation(typeTabFilterEClass, ecorePackage.getEString(), "getAllTypes", 1, -1, IS_UNIQUE, IS_ORDERED);
 
 		initEClass(customTabFilterEClass, CustomTabFilter.class, "CustomTabFilter", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
 		initEAttribute(getCustomTabFilter_ClassName(), ecorePackage.getEString(), "className", null, 1, 1, CustomTabFilter.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 
 		addEOperation(customTabFilterEClass, ecorePackage.getEString(), "getQualifiedClassName", 0, 1, IS_UNIQUE, IS_ORDERED);
 
 		initEClass(genContributionItemEClass, GenContributionItem.class, "GenContributionItem", IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
 		initEReference(getGenContributionItem_Owner(), this.getGenContributionManager(), this.getGenContributionManager_Items(), "owner", null, 0, 1, GenContributionItem.class, !IS_TRANSIENT, !IS_VOLATILE, !IS_CHANGEABLE, !IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEReference(getGenContributionItem_Application(), this.getGenApplication(), null, "application", null, 0, 1, GenContributionItem.class, IS_TRANSIENT, IS_VOLATILE, !IS_CHANGEABLE, !IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, IS_DERIVED, IS_ORDERED);
 
 		initEClass(genSharedContributionItemEClass, GenSharedContributionItem.class, "GenSharedContributionItem", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
 		initEReference(getGenSharedContributionItem_ActualItem(), this.getGenContributionItem(), null, "actualItem", null, 1, 1, GenSharedContributionItem.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_COMPOSITE, IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 
 		initEClass(genGroupMarkerEClass, GenGroupMarker.class, "GenGroupMarker", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
 		initEAttribute(getGenGroupMarker_GroupName(), ecorePackage.getEString(), "groupName", null, 1, 1, GenGroupMarker.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 
 		initEClass(genSeparatorEClass, GenSeparator.class, "GenSeparator", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
 		initEAttribute(getGenSeparator_GroupName(), ecorePackage.getEString(), "groupName", null, 0, 1, GenSeparator.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 
 		initEClass(genActionFactoryContributionItemEClass, GenActionFactoryContributionItem.class, "GenActionFactoryContributionItem", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
 		initEAttribute(getGenActionFactoryContributionItem_Name(), ecorePackage.getEString(), "name", null, 1, 1, GenActionFactoryContributionItem.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 
 		initEClass(genContributionManagerEClass, GenContributionManager.class, "GenContributionManager", IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
 		initEAttribute(getGenContributionManager_ID(), ecorePackage.getEString(), "iD", null, 0, 1, GenContributionManager.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEReference(getGenContributionManager_Items(), this.getGenContributionItem(), this.getGenContributionItem_Owner(), "items", null, 0, -1, GenContributionManager.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 
 		initEClass(genMenuManagerEClass, GenMenuManager.class, "GenMenuManager", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
 		initEAttribute(getGenMenuManager_Name(), ecorePackage.getEString(), "name", null, 0, 1, GenMenuManager.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 
 		initEClass(genToolBarManagerEClass, GenToolBarManager.class, "GenToolBarManager", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
 
 		initEClass(genApplicationEClass, GenApplication.class, "GenApplication", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
 		initEReference(getGenApplication_EditorGen(), this.getGenEditorGenerator(), this.getGenEditorGenerator_Application(), "editorGen", null, 1, 1, GenApplication.class, !IS_TRANSIENT, !IS_VOLATILE, !IS_CHANGEABLE, !IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEAttribute(getGenApplication_ID(), ecorePackage.getEString(), "iD", null, 0, 1, GenApplication.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEAttribute(getGenApplication_Title(), ecorePackage.getEString(), "title", null, 0, 1, GenApplication.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEAttribute(getGenApplication_PackageName(), ecorePackage.getEString(), "packageName", null, 0, 1, GenApplication.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEAttribute(getGenApplication_ClassName(), ecorePackage.getEString(), "className", null, 0, 1, GenApplication.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEAttribute(getGenApplication_WorkbenchAdvisorClassName(), ecorePackage.getEString(), "workbenchAdvisorClassName", null, 0, 1, GenApplication.class, IS_TRANSIENT, IS_VOLATILE, !IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, IS_DERIVED, IS_ORDERED);
 		initEAttribute(getGenApplication_WorkbenchWindowAdvisorClassName(), ecorePackage.getEString(), "workbenchWindowAdvisorClassName", null, 0, 1, GenApplication.class, IS_TRANSIENT, IS_VOLATILE, !IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, IS_DERIVED, IS_ORDERED);
 		initEAttribute(getGenApplication_ActionBarAdvisorClassName(), ecorePackage.getEString(), "actionBarAdvisorClassName", null, 0, 1, GenApplication.class, IS_TRANSIENT, IS_VOLATILE, !IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, IS_DERIVED, IS_ORDERED);
 		initEAttribute(getGenApplication_PerspectiveClassName(), ecorePackage.getEString(), "perspectiveClassName", null, 0, 1, GenApplication.class, IS_TRANSIENT, IS_VOLATILE, !IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, IS_DERIVED, IS_ORDERED);
 		initEAttribute(getGenApplication_PerspectiveId(), ecorePackage.getEString(), "perspectiveId", null, 0, 1, GenApplication.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEAttribute(getGenApplication_SupportFiles(), ecorePackage.getEBoolean(), "supportFiles", null, 0, 1, GenApplication.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEReference(getGenApplication_SharedContributionItems(), this.getGenContributionItem(), null, "sharedContributionItems", null, 0, -1, GenApplication.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEReference(getGenApplication_MainMenu(), this.getGenMenuManager(), null, "mainMenu", null, 0, 1, GenApplication.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEReference(getGenApplication_MainToolBar(), this.getGenToolBarManager(), null, "mainToolBar", null, 0, 1, GenApplication.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 
 		addEOperation(genApplicationEClass, ecorePackage.getEString(), "getQualifiedClassName", 0, 1, IS_UNIQUE, IS_ORDERED);
 
 		addEOperation(genApplicationEClass, ecorePackage.getEString(), "getWorkbenchAdvisorQualifiedClassName", 0, 1, IS_UNIQUE, IS_ORDERED);
 
 		addEOperation(genApplicationEClass, ecorePackage.getEString(), "getWorkbenchWindowAdvisorQualifiedClassName", 0, 1, IS_UNIQUE, IS_ORDERED);
 
 		addEOperation(genApplicationEClass, ecorePackage.getEString(), "getActionBarAdvisorQualifiedClassName", 0, 1, IS_UNIQUE, IS_ORDERED);
 
 		addEOperation(genApplicationEClass, ecorePackage.getEString(), "getPerspectiveQualifiedClassName", 0, 1, IS_UNIQUE, IS_ORDERED);
 
 		// Initialize enums and add enum literals
 		initEEnum(standardPreferencePagesEEnum, StandardPreferencePages.class, "StandardPreferencePages");
 		addEEnumLiteral(standardPreferencePagesEEnum, StandardPreferencePages.GENERAL_LITERAL);
 		addEEnumLiteral(standardPreferencePagesEEnum, StandardPreferencePages.APPEARANCE_LITERAL);
 		addEEnumLiteral(standardPreferencePagesEEnum, StandardPreferencePages.CONNECTIONS_LITERAL);
 		addEEnumLiteral(standardPreferencePagesEEnum, StandardPreferencePages.PRINTING_LITERAL);
 		addEEnumLiteral(standardPreferencePagesEEnum, StandardPreferencePages.RULERS_AND_GRID_LITERAL);
 		addEEnumLiteral(standardPreferencePagesEEnum, StandardPreferencePages.PATHMAPS_LITERAL);
 
 		initEEnum(rulerUnitsEEnum, RulerUnits.class, "RulerUnits");
 		addEEnumLiteral(rulerUnitsEEnum, RulerUnits.INCHES_LITERAL);
 		addEEnumLiteral(rulerUnitsEEnum, RulerUnits.CENTIMETERS_LITERAL);
 		addEEnumLiteral(rulerUnitsEEnum, RulerUnits.PIXELS_LITERAL);
 
 		initEEnum(routingEEnum, Routing.class, "Routing");
 		addEEnumLiteral(routingEEnum, Routing.MANUAL_LITERAL);
 		addEEnumLiteral(routingEEnum, Routing.RECTILINEAR_LITERAL);
 		addEEnumLiteral(routingEEnum, Routing.TREE_LITERAL);
 
 		initEEnum(jFaceFontEEnum, JFaceFont.class, "JFaceFont");
 		addEEnumLiteral(jFaceFontEEnum, JFaceFont.DEFAULT_LITERAL);
 		addEEnumLiteral(jFaceFontEEnum, JFaceFont.TEXT_LITERAL);
 		addEEnumLiteral(jFaceFontEEnum, JFaceFont.BANNER_LITERAL);
 		addEEnumLiteral(jFaceFontEEnum, JFaceFont.DIALOG_LITERAL);
 		addEEnumLiteral(jFaceFontEEnum, JFaceFont.HEADER_LITERAL);
 
 		initEEnum(fontStyleEEnum, FontStyle.class, "FontStyle");
 		addEEnumLiteral(fontStyleEEnum, FontStyle.NORMAL_LITERAL);
 		addEEnumLiteral(fontStyleEEnum, FontStyle.BOLD_LITERAL);
 		addEEnumLiteral(fontStyleEEnum, FontStyle.ITALIC_LITERAL);
 
 		initEEnum(diagramColorsEEnum, DiagramColors.class, "DiagramColors");
 		addEEnumLiteral(diagramColorsEEnum, DiagramColors.BUTTON_LIGHTEST_LITERAL);
 		addEEnumLiteral(diagramColorsEEnum, DiagramColors.BUTTON_LITERAL);
 		addEEnumLiteral(diagramColorsEEnum, DiagramColors.BUTTON_DARKER_LITERAL);
 		addEEnumLiteral(diagramColorsEEnum, DiagramColors.BUTTON_DARKEST_LITERAL);
 		addEEnumLiteral(diagramColorsEEnum, DiagramColors.LIST_BACKGROUND_LITERAL);
 		addEEnumLiteral(diagramColorsEEnum, DiagramColors.LIST_FOREGROUND_LITERAL);
 		addEEnumLiteral(diagramColorsEEnum, DiagramColors.MENU_BACKGROUND_LITERAL);
 		addEEnumLiteral(diagramColorsEEnum, DiagramColors.MENU_FOREGROUND_LITERAL);
 		addEEnumLiteral(diagramColorsEEnum, DiagramColors.MENU_BACKGROUND_SELECTED_LITERAL);
 		addEEnumLiteral(diagramColorsEEnum, DiagramColors.MENU_FOREGROUND_SELECTED_LITERAL);
 		addEEnumLiteral(diagramColorsEEnum, DiagramColors.TITLE_BACKGROUND_LITERAL);
 		addEEnumLiteral(diagramColorsEEnum, DiagramColors.TITLE_GRADIENT_LITERAL);
 		addEEnumLiteral(diagramColorsEEnum, DiagramColors.TITLE_FOREGROUND_LITERAL);
 		addEEnumLiteral(diagramColorsEEnum, DiagramColors.TITLE_INACTIVE_FOREGROUND_LITERAL);
 		addEEnumLiteral(diagramColorsEEnum, DiagramColors.TITLE_INACTIVE_BACKGROUND_LITERAL);
 		addEEnumLiteral(diagramColorsEEnum, DiagramColors.TITLE_INACTIVE_GRADIENT_LITERAL);
 		addEEnumLiteral(diagramColorsEEnum, DiagramColors.TOOLTIP_FOREGROUND_LITERAL);
 		addEEnumLiteral(diagramColorsEEnum, DiagramColors.TOOLTIP_BACKGROUND_LITERAL);
 		addEEnumLiteral(diagramColorsEEnum, DiagramColors.WHITE_LITERAL);
 		addEEnumLiteral(diagramColorsEEnum, DiagramColors.LIGHT_GRAY_LITERAL);
 		addEEnumLiteral(diagramColorsEEnum, DiagramColors.GRAY_LITERAL);
 		addEEnumLiteral(diagramColorsEEnum, DiagramColors.DARK_GRAY_LITERAL);
 		addEEnumLiteral(diagramColorsEEnum, DiagramColors.BLACK_LITERAL);
 		addEEnumLiteral(diagramColorsEEnum, DiagramColors.RED_LITERAL);
 		addEEnumLiteral(diagramColorsEEnum, DiagramColors.ORANGE_LITERAL);
 		addEEnumLiteral(diagramColorsEEnum, DiagramColors.YELLOW_LITERAL);
 		addEEnumLiteral(diagramColorsEEnum, DiagramColors.GREEN_LITERAL);
 		addEEnumLiteral(diagramColorsEEnum, DiagramColors.LIGHT_GREEN_LITERAL);
 		addEEnumLiteral(diagramColorsEEnum, DiagramColors.DARK_GREEN_LITERAL);
 		addEEnumLiteral(diagramColorsEEnum, DiagramColors.CYAN_LITERAL);
 		addEEnumLiteral(diagramColorsEEnum, DiagramColors.LIGHT_BLUE_LITERAL);
 		addEEnumLiteral(diagramColorsEEnum, DiagramColors.BLUE_LITERAL);
 		addEEnumLiteral(diagramColorsEEnum, DiagramColors.DARK_BLUE_LITERAL);
 		addEEnumLiteral(diagramColorsEEnum, DiagramColors.DIAGRAM_GREEN_LITERAL);
 		addEEnumLiteral(diagramColorsEEnum, DiagramColors.DIAGRAM_LIGHT_RED_LITERAL);
 		addEEnumLiteral(diagramColorsEEnum, DiagramColors.DIAGRAM_RED_LITERAL);
 		addEEnumLiteral(diagramColorsEEnum, DiagramColors.DIAGRAM_LIGHT_BLUE_LITERAL);
 		addEEnumLiteral(diagramColorsEEnum, DiagramColors.DIAGRAM_BLUE_LITERAL);
 		addEEnumLiteral(diagramColorsEEnum, DiagramColors.DIAGRAM_LIGHT_GRAY_LITERAL);
 		addEEnumLiteral(diagramColorsEEnum, DiagramColors.DIAGRAM_GRAY_LITERAL);
 		addEEnumLiteral(diagramColorsEEnum, DiagramColors.DIAGRAM_DARK_GRAY_LITERAL);
 		addEEnumLiteral(diagramColorsEEnum, DiagramColors.DIAGRAM_LIGHT_YELLOW_LITERAL);
 		addEEnumLiteral(diagramColorsEEnum, DiagramColors.DIAGRAM_DARK_YELLOW_LITERAL);
 		addEEnumLiteral(diagramColorsEEnum, DiagramColors.DIAGRAM_LIGHT_GOLD_YELLOW_LITERAL);
 		addEEnumLiteral(diagramColorsEEnum, DiagramColors.DIAGRAM_BURGUNDY_RED_LITERAL);
 
 		initEEnum(providerPriorityEEnum, ProviderPriority.class, "ProviderPriority");
 		addEEnumLiteral(providerPriorityEEnum, ProviderPriority.LOWEST_LITERAL);
 		addEEnumLiteral(providerPriorityEEnum, ProviderPriority.LOW_LITERAL);
 		addEEnumLiteral(providerPriorityEEnum, ProviderPriority.MEDIUM_LITERAL);
 		addEEnumLiteral(providerPriorityEEnum, ProviderPriority.HIGH_LITERAL);
 		addEEnumLiteral(providerPriorityEEnum, ProviderPriority.HIGHEST_LITERAL);
 
 		initEEnum(linkLabelAlignmentEEnum, LinkLabelAlignment.class, "LinkLabelAlignment");
 		addEEnumLiteral(linkLabelAlignmentEEnum, LinkLabelAlignment.MIDDLE_LITERAL);
 		addEEnumLiteral(linkLabelAlignmentEEnum, LinkLabelAlignment.TARGET_LITERAL);
 		addEEnumLiteral(linkLabelAlignmentEEnum, LinkLabelAlignment.SOURCE_LITERAL);
 
 		initEEnum(labelTextAccessMethodEEnum, LabelTextAccessMethod.class, "LabelTextAccessMethod");
 		addEEnumLiteral(labelTextAccessMethodEEnum, LabelTextAccessMethod.MESSAGE_FORMAT);
 		addEEnumLiteral(labelTextAccessMethodEEnum, LabelTextAccessMethod.NATIVE);
 		addEEnumLiteral(labelTextAccessMethodEEnum, LabelTextAccessMethod.REGEXP);
 		addEEnumLiteral(labelTextAccessMethodEEnum, LabelTextAccessMethod.PRINTF);
 
 		initEEnum(viewmapLayoutTypeEEnum, ViewmapLayoutType.class, "ViewmapLayoutType");
 		addEEnumLiteral(viewmapLayoutTypeEEnum, ViewmapLayoutType.UNKNOWN_LITERAL);
 		addEEnumLiteral(viewmapLayoutTypeEEnum, ViewmapLayoutType.XY_LAYOUT_LITERAL);
 		addEEnumLiteral(viewmapLayoutTypeEEnum, ViewmapLayoutType.FLOW_LAYOUT_LITERAL);
 		addEEnumLiteral(viewmapLayoutTypeEEnum, ViewmapLayoutType.TOOLBAR_LAYOUT_LITERAL);
 
 		initEEnum(standardEntryKindEEnum, StandardEntryKind.class, "StandardEntryKind");
 		addEEnumLiteral(standardEntryKindEEnum, StandardEntryKind.SELECT_LITERAL);
 		addEEnumLiteral(standardEntryKindEEnum, StandardEntryKind.MARQUEE_LITERAL);
 		addEEnumLiteral(standardEntryKindEEnum, StandardEntryKind.ZOOM_LITERAL);
 
 		initEEnum(genSeverityEEnum, GenSeverity.class, "GenSeverity");
 		addEEnumLiteral(genSeverityEEnum, GenSeverity.INFO_LITERAL);
 		addEEnumLiteral(genSeverityEEnum, GenSeverity.WARNING_LITERAL);
 		addEEnumLiteral(genSeverityEEnum, GenSeverity.ERROR_LITERAL);
 
 		initEEnum(genLanguageEEnum, GenLanguage.class, "GenLanguage");
 		addEEnumLiteral(genLanguageEEnum, GenLanguage.OCL_LITERAL);
 		addEEnumLiteral(genLanguageEEnum, GenLanguage.JAVA_LITERAL);
 		addEEnumLiteral(genLanguageEEnum, GenLanguage.REGEXP_LITERAL);
 		addEEnumLiteral(genLanguageEEnum, GenLanguage.NREGEXP_LITERAL);
 
 		initEEnum(genNavigatorReferenceTypeEEnum, GenNavigatorReferenceType.class, "GenNavigatorReferenceType");
 		addEEnumLiteral(genNavigatorReferenceTypeEEnum, GenNavigatorReferenceType.CHILDREN_LITERAL);
 		addEEnumLiteral(genNavigatorReferenceTypeEEnum, GenNavigatorReferenceType.OUT_TARGET_LITERAL);
 		addEEnumLiteral(genNavigatorReferenceTypeEEnum, GenNavigatorReferenceType.IN_SOURCE_LITERAL);
 
 		initEEnum(generatedTypeEEnum, GeneratedType.class, "GeneratedType");
 		addEEnumLiteral(generatedTypeEEnum, GeneratedType.ABSTRACT_NAVIGATOR_ITEM_LITERAL);
 
 		// Create resource
 		createResource(eNS_URI);
 
 		// Create annotations
 		// http://www.eclipse.org/emf/2004/EmfaticAnnotationMap
 		createEmfaticAnnotationMapAnnotations();
 		// http://www.eclipse.org/gmf/2005/constraints
 		createConstraintsAnnotations();
 		// http://www.eclipse.org/gmf/2006/deprecated
 		createDeprecatedAnnotations();
 		// http://www.eclipse.org/gmf/2005/constraints/meta
 		createMetaAnnotations();
 	}
 
 	/**
 	 * Initializes the annotations for <b>http://www.eclipse.org/emf/2004/EmfaticAnnotationMap</b>.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	protected void createEmfaticAnnotationMapAnnotations() {
 		String source = "http://www.eclipse.org/emf/2004/EmfaticAnnotationMap";		
 		addAnnotation
 		  (this, 
 		   source, 
 		   new String[] {
 			 "constraints", "http://www.eclipse.org/gmf/2005/constraints",
 			 "meta", "http://www.eclipse.org/gmf/2005/constraints/meta",
 			 "deprecated", "http://www.eclipse.org/gmf/2006/deprecated"
 		   });																																																																																																																																																																																																							
 	}
 
 	/**
 	 * Initializes the annotations for <b>http://www.eclipse.org/gmf/2005/constraints</b>.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	protected void createConstraintsAnnotations() {
 		String source = "http://www.eclipse.org/gmf/2005/constraints";			
 		addAnnotation
 		  (this, 
 		   source, 
 		   new String[] {
 			 "import", "http://www.eclipse.org/gmf/runtime/1.0.0/notation"
 		   });										
 		addAnnotation
 		  (genDiagramEClass, 
 		   source, 
 		   new String[] {
 			 "ocl", "diagramRunTimeClass.ecoreClass.eAllSuperTypes->including(diagramRunTimeClass.ecoreClass)->one(ePackage.name = \'notation\' and name = \'Diagram\')",
 			 "description", "\'Diagram Runtime Class\' must be a notation::Diagram or sub-class"
 		   });														
 		addAnnotation
 		  (genCommonBaseEClass, 
 		   source, 
 		   new String[] {
 			 "ocl", "styles->forAll(style|style.ecoreClass.eAllSuperTypes->including(style.ecoreClass)->one(ePackage.name = \'notation\' and name = \'Style\'))",
 			 "description", "Each style must be a notation::Style or sub-class"
 		   });				
 		addAnnotation
 		  (getGenCommonBase_VisualID(), 
 		   source, 
 		   new String[] {
 			 "ocl", "visualID >= 0",
 			 "description", "Visual ID must be a non-negative integer"
 		   });							
 		addAnnotation
 		  (genNodeEClass, 
 		   source, 
 		   new String[] {
 			 "ocl", "diagramRunTimeClass.ecoreClass.eAllSuperTypes->including(diagramRunTimeClass.ecoreClass)->one(ePackage.name = \'notation\' and name = \'Node\')",
 			 "description", "Node \'Diagram Runtime Class\' must be a notation::Node or sub-class"
 		   });					
 		addAnnotation
 		  (genTopLevelNodeEClass, 
 		   source, 
 		   new String[] {
 			 "ocl", "not modelFacet.containmentMetaFeature.oclIsUndefined() implies modelFacet.containmentMetaFeature.genClass.ecoreClass.isSuperTypeOf(diagram.domainDiagramElement.ecoreClass)",
 			 "description", "Top level node \'Containment Feature\' must be available in the diagram \'Domain Element\' or its super-class"
 		   });		
 		addAnnotation
 		  (genChildNodeEClass, 
 		   source, 
 		   new String[] {
 			 "ocl", "not modelFacet.oclIsUndefined() implies not modelFacet.containmentMetaFeature.oclIsUndefined()",
 			 "description", "Child node must specify \'Containment Meta Feature\'"
 		   });		
 		addAnnotation
 		  (getGenChildNode_Containers(), 
 		   source, 
 		   new String[] {
 			 "ocl", "let cmps:OrderedSet(GenChildContainer)=containers->select(oclIsKindOf(GenCompartment)) in cmps->exists(oclAsType(GenCompartment).listLayout) implies not cmps->exists(not oclAsType(GenCompartment).listLayout)",
 			 "description", "Node is referenced from multiple containers with different \'List Layout\' value"
 		   });						
 		addAnnotation
 		  (genLinkEClass, 
 		   source, 
 		   new String[] {
 			 "ocl", "diagramRunTimeClass.ecoreClass.eAllSuperTypes->including(diagramRunTimeClass.ecoreClass)->one(ePackage.name = \'notation\' and name = \'Edge\')",
 			 "description", "Link \'Diagram Runtime Class\' must be a notation::Edge or sub-class"
 		   });		
 		addAnnotation
 		  (genLinkEClass, 
 		   source, 
 		   new String[] {
 			 "ocl", "outgoingCreationAllowed or incomingCreationAllowed",
 			 "description", "Either outgoingCreationAllowed or incomingCreationAllowed property should be true"
 		   });												
 		addAnnotation
 		  (genNodeLabelEClass, 
 		   source, 
 		   new String[] {
 			 "ocl", "getMetaFeatures()->forAll(f|f.ecoreFeature.eContainingClass.isSuperTypeOf(node.getDomainMetaClass().ecoreClass))",
 			 "description", "Node label meta features must be owned by the node \'Meta Class\' or its super-classes"
 		   });				
 		addAnnotation
 		  (genLinkLabelEClass, 
 		   source, 
 		   new String[] {
 			 "ocl", "modelFacet.oclIsTypeOf(FeatureLabelModelFacet)=true implies link.modelFacet.oclIsTypeOf(TypeLinkModelFacet)",
 			 "description", "Feature based link labels can only be used on link with class (TypeLinkModelFacet)"
 		   });		
 		addAnnotation
 		  (genLinkLabelEClass, 
 		   source, 
 		   new String[] {
 			 "ocl", "let tl: TypeLinkModelFacet = link.modelFacet.oclAsType(TypeLinkModelFacet) in not tl.oclIsUndefined() implies self.getMetaFeatures()->forAll(f|f.ecoreFeature.eContainingClass.isSuperTypeOf(tl.metaClass.ecoreClass))",
 			 "description", "Link label meta features must be owned by the node \'Meta Class\' or its super-classes"
 		   });												
 		addAnnotation
 		  (getTypeModelFacet_ContainmentMetaFeature(), 
 		   source, 
 		   new String[] {
 			 "ocl", "let r: ecore::EReference=containmentMetaFeature.ecoreFeature.oclAsType(ecore::EReference) in not containmentMetaFeature.oclIsUndefined() implies r.containment or r.eReferenceType.isSuperTypeOf(metaClass.ecoreClass)",
 			 "description", "\'Containment Meta Feature\' must reference \'Meta Class\' or its super-classes"
 		   });			
 		addAnnotation
 		  (getTypeModelFacet_ChildMetaFeature(), 
 		   source, 
 		   new String[] {
 			 "ocl", "not childMetaFeature.oclIsUndefined() implies childMetaFeature.ecoreFeature.oclAsType(ecore::EReference).eReferenceType.isSuperTypeOf(metaClass.ecoreClass)",
 			 "description", "\'Child Meta Feature\' must reference \'Meta Class\' or its sub-classes"
 		   });						
 		addAnnotation
 		  (typeLinkModelFacetEClass, 
 		   source, 
 		   new String[] {
 			 "ocl", "not containmentMetaFeature.oclIsUndefined()",
 			 "description", "Link \'Containment Meta Feature\' must be specified"
 		   });			
 		addAnnotation
 		  (getTypeLinkModelFacet_SourceMetaFeature(), 
 		   source, 
 		   new String[] {
 			 "ocl", "not sourceMetaFeature.oclIsUndefined() implies sourceMetaFeature.genClass.ecoreClass.isSuperTypeOf(metaClass.ecoreClass)",
 			 "description", "Link \'Source Meta Feature\' must be owned by link \'Meta Class\' or its super-class"
 		   });		
 		addAnnotation
 		  (getTypeLinkModelFacet_TargetMetaFeature(), 
 		   source, 
 		   new String[] {
 			 "ocl", "not targetMetaFeature.oclIsUndefined() implies targetMetaFeature.genClass.ecoreClass.isSuperTypeOf(metaClass.ecoreClass)",
 			 "description", "Link \'Target Meta Feature\' must be owned by link \'Meta Class\' or its super-class"
 		   });			
 		addAnnotation
 		  (getFeatureLinkModelFacet_MetaFeature(), 
 		   source, 
 		   new String[] {
 			 "ocl", "metaFeature.ecoreFeature.unique",
 			 "description", "All references are unique in EMF due to the current code generation"
 		   });																																							
 		addAnnotation
 		  (getGenFeatureSeqInitializer_ElementClass(), 
 		   source, 
 		   new String[] {
 			 "ocl", "not creatingInitializer.feature.oclIsUndefined() implies creatingInitializer.feature.ecoreFeature.oclAsType(ecore::EReference).eReferenceType.isSuperTypeOf(elementClass.ecoreClass)",
 			 "description", "\'elementClass\' must be the same as or sub-type of the containing \'GenReferenceNewElementSpec\' reference type"
 		   });		
 		addAnnotation
 		  (getGenFeatureSeqInitializer_ElementClass(), 
 		   source, 
 		   new String[] {
 			 "ocl", "not creatingInitializer.feature.oclIsUndefined() implies not (elementClass.ecoreClass.interface or elementClass.ecoreClass.abstract)",
 			 "description", "\'elementClass\' must be a concrete EClass which is the same or sub-type of the containing \'GenReferenceNewElementSpec\' reference type"
 		   });					
 		addAnnotation
 		  (genReferenceNewElementSpecEClass, 
 		   source, 
 		   new String[] {
 			 "ocl", "feature <> null implies feature.ecoreFeature.oclIsKindOf(ecore::EReference)",
 			 "description", "\'feature\' of \'GenReferenceNewElementSpec\' must refer to ecore::EReference"
 		   });				
 		addAnnotation
 		  (getGenFeatureInitializer_Feature(), 
 		   source, 
 		   new String[] {
 			 "ocl", "feature <> null implies not featureSeqInitializer.initializers->exists(i| i <> self and i.feature = self.feature)",
 			 "description", "The feature is already initialized by another \'GenFeatureInitializer\' in the sequence"
 		   });		
 		addAnnotation
 		  (getGenFeatureInitializer_Feature(), 
 		   source, 
 		   new String[] {
 			 "ocl", "feature <> null implies feature.ecoreFeature.eContainingClass.isSuperTypeOf(featureSeqInitializer.elementClass.ecoreClass)",
 			 "description", "The \'feature\' of \'GenFeatureInitializer\' must be available in \'Meta Class\' of the initialized element"
 		   });			
 		addAnnotation
 		  (genLinkConstraintsEClass, 
 		   source, 
 		   new String[] {
 			 "ocl", "not sourceEnd.oclIsUndefined() or not targetEnd.oclIsUndefined()",
 			 "description", "Either \'sourceEnd\' or \'targetEnd\' constraint must be specified"
 		   });																							
 		addAnnotation
 		  (getGenAuditRule_Id(), 
 		   source, 
 		   new String[] {
 			 "ocl", "not id.oclIsUndefined() implies root.rules->one(i | i.id = self.id)",
 			 "description", "Audit rule with the same ID already exists"
 		   });														
 		addAnnotation
 		  (getGenDiagramElementTarget_Element(), 
 		   source, 
 		   new String[] {
 			 "ocl", "element <> null and element->size() > 1 implies element->forAll(oclIsKindOf(GenNode))",
 			 "description", "Multiple diagram elements must be GenNode type conformant"
 		   });			
 		addAnnotation
 		  (getGenDomainAttributeTarget_Attribute(), 
 		   source, 
 		   new String[] {
 			 "ocl", "attribute.ecoreFeature.oclIsKindOf(ecore::EAttribute)",
 			 "description", "EAttribute element required for auditable domain attribute"
 		   });					
 		addAnnotation
 		  (getGenNotationElementTarget_Element(), 
 		   source, 
 		   new String[] {
 			 "ocl", "element.ecoreClass.eAllSuperTypes->including(element.ecoreClass)->one(ePackage.name = \'notation\' and name = \'View\')",
 			 "description", "\'notation::View\' or its sub-class must be set to NotationElement target"
 		   });		
 		addAnnotation
 		  (genMetricRuleEClass, 
 		   source, 
 		   new String[] {
 			 "ocl", "not lowLimit.oclIsUndefined() and not highLimit.oclIsUndefined()  implies lowLimit < highLimit",
 			 "description", "Metric value \'lowLimit\' must be smaller then \'highLimit\'"
 		   });			
 		addAnnotation
 		  (getGenMetricRule_Key(), 
 		   source, 
 		   new String[] {
 			 "ocl", "not key.oclIsUndefined() implies container.metrics->one(i | i.key = self.key)",
 			 "description", "Metric rule with the same \'key\' already exists"
 		   });																				
 		addAnnotation
 		  (getGenSharedContributionItem_ActualItem(), 
 		   source, 
 		   new String[] {
 			 "ocl", "not actualItem.oclIsKindOf(gmfgen::GenSharedContributionItem)",
 			 "description", "Actual contribution item can\'t be a reference"
 		   });					
 	}
 
 	/**
 	 * Initializes the annotations for <b>http://www.eclipse.org/gmf/2006/deprecated</b>.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	protected void createDeprecatedAnnotations() {
 		String source = "http://www.eclipse.org/gmf/2006/deprecated";																				
 		addAnnotation
 		  (linkConstraintsEClass.getEOperations().get(0), 
 		   source, 
 		   new String[] {
 			 "documentation", "LinkCreationConstants should be generated if diagram has any links"
 		   });																																																																																																																																																																											
 		addAnnotation
 		  (genNavigatorEClass.getEOperations().get(9), 
 		   source, 
 		   new String[] {
 			 "documentation", "corresponding method should be implemented in .ext file"
 		   });											
 	}
 
 	/**
 	 * Initializes the annotations for <b>http://www.eclipse.org/gmf/2005/constraints/meta</b>.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	protected void createMetaAnnotations() {
 		String source = "http://www.eclipse.org/gmf/2005/constraints/meta";																																																																												
 		addAnnotation
 		  (getTypeModelFacet_ModelElementSelector(), 
 		   source, 
 		   new String[] {
 			 "def", "context",
 			 "ocl", "metaClass.ecoreClass"
 		   });																													
 		addAnnotation
 		  (valueExpressionEClass, 
 		   source, 
 		   new String[] {
 			 "def", "ValueSpec"
 		   });				
 		addAnnotation
 		  (getValueExpression_Body(), 
 		   source, 
 		   new String[] {
 			 "def", "body"
 		   });			
 		addAnnotation
 		  (getValueExpression_LangName(), 
 		   source, 
 		   new String[] {
 			 "def", "lang"
 		   });			
 		addAnnotation
 		  (genConstraintEClass, 
 		   source, 
 		   new String[] {
 			 "def", "Constraint"
 		   });											
 		addAnnotation
 		  (getGenFeatureSeqInitializer_Initializers(), 
 		   source, 
 		   new String[] {
 			 "def", "context",
 			 "ocl", "self.elementClass"
 		   });					
 		addAnnotation
 		  (genFeatureValueSpecEClass, 
 		   source, 
 		   new String[] {
 			 "def", "ValueSpec"
 		   });		
 		addAnnotation
 		  (genFeatureValueSpecEClass, 
 		   source, 
 		   new String[] {
 			 "def", "type",
 			 "ocl", "feature.ecoreFeature"
 		   });															
 		addAnnotation
 		  (getGenLinkConstraints_SourceEnd(), 
 		   source, 
 		   new String[] {
 			 "def", "context",
 			 "ocl", "getSourceEndContextClass().ecoreClass"
 		   });		
 		addAnnotation
 		  (getGenLinkConstraints_SourceEnd(), 
 		   source, 
 		   new String[] {
 			 "def", "variable",
 			 "name", "oppositeEnd",
 			 "type.ocl", "getTargetEndContextClass().ecoreClass"
 		   });			
 		addAnnotation
 		  (getGenLinkConstraints_TargetEnd(), 
 		   source, 
 		   new String[] {
 			 "def", "context",
 			 "ocl", "getTargetEndContextClass().ecoreClass"
 		   });		
 		addAnnotation
 		  (getGenLinkConstraints_TargetEnd(), 
 		   source, 
 		   new String[] {
 			 "def", "variable",
 			 "name", "oppositeEnd",
 			 "type.ocl", "getSourceEndContextClass().ecoreClass"
 		   });														
 		addAnnotation
 		  (getGenAuditRule_Rule(), 
 		   source, 
 		   new String[] {
 			 "def", "context",
 			 "ocl", "target.getContext()"
 		   });																							
 		addAnnotation
 		  (getGenMetricRule_Rule(), 
 		   source, 
 		   new String[] {
 			 "def", "context",
 			 "ocl", "target.getContext()"
 		   });		
 		addAnnotation
 		  (getGenMetricRule_Rule(), 
 		   source, 
 		   new String[] {
 			 "def", "type",
 			 "ocl", "\'ecore::EDoubleObject\'"
 		   });																					
 	}
 
 } //GMFGenPackageImpl
