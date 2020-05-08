 /**
  * <copyright>
  * </copyright>
  *
  * $Id$
  */
 package org.eclipse.gmf.codegen.gmfgen.impl;
 
 import java.util.Collection;
 import java.util.LinkedHashMap;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 
 import org.eclipse.emf.codegen.ecore.genmodel.GenClass;
 import org.eclipse.emf.codegen.ecore.genmodel.GenPackage;
 import org.eclipse.emf.common.notify.Notification;
 import org.eclipse.emf.common.notify.NotificationChain;
 import org.eclipse.emf.common.util.BasicEList;
 import org.eclipse.emf.common.util.EList;
 import org.eclipse.emf.ecore.EClass;
 import org.eclipse.emf.ecore.InternalEObject;
 import org.eclipse.emf.ecore.impl.ENotificationImpl;
 import org.eclipse.emf.ecore.util.EDataTypeUniqueEList;
 import org.eclipse.emf.ecore.util.EObjectContainmentEList;
 import org.eclipse.emf.ecore.util.EObjectContainmentWithInverseEList;
 import org.eclipse.emf.ecore.util.EcoreEList;
 import org.eclipse.emf.ecore.util.InternalEList;
 import org.eclipse.gmf.codegen.gmfgen.BatchValidation;
 import org.eclipse.gmf.codegen.gmfgen.EditPartCandies;
 import org.eclipse.gmf.codegen.gmfgen.EditorCandies;
 import org.eclipse.gmf.codegen.gmfgen.FeatureLinkModelFacet;
 import org.eclipse.gmf.codegen.gmfgen.GMFGenPackage;
 import org.eclipse.gmf.codegen.gmfgen.GenChildContainer;
 import org.eclipse.gmf.codegen.gmfgen.GenChildNode;
 import org.eclipse.gmf.codegen.gmfgen.GenCommonBase;
 import org.eclipse.gmf.codegen.gmfgen.GenCompartment;
 import org.eclipse.gmf.codegen.gmfgen.GenContainerBase;
 import org.eclipse.gmf.codegen.gmfgen.GenDiagram;
 import org.eclipse.gmf.codegen.gmfgen.GenDiagramPreferences;
 import org.eclipse.gmf.codegen.gmfgen.GenEditorGenerator;
 import org.eclipse.gmf.codegen.gmfgen.GenLink;
 import org.eclipse.gmf.codegen.gmfgen.GenPreferencePage;
 import org.eclipse.gmf.codegen.gmfgen.GenNode;
 import org.eclipse.gmf.codegen.gmfgen.GenTopLevelNode;
 import org.eclipse.gmf.codegen.gmfgen.LinkConstraints;
 import org.eclipse.gmf.codegen.gmfgen.MeasurementUnit;
 import org.eclipse.gmf.codegen.gmfgen.PackageNames;
 import org.eclipse.gmf.codegen.gmfgen.Palette;
 import org.eclipse.gmf.codegen.gmfgen.ProviderClassNames;
 import org.eclipse.gmf.codegen.gmfgen.ProviderPriority;
 import org.eclipse.gmf.codegen.gmfgen.Shortcuts;
 import org.eclipse.gmf.codegen.gmfgen.TypeLinkModelFacet;
 import org.eclipse.gmf.codegen.gmfgen.TypeModelFacet;
 import org.eclipse.gmf.common.codegen.ImportAssistant;
 
 /**
  * <!-- begin-user-doc -->
  * An implementation of the model object '<em><b>Gen Diagram</b></em>'.
  * <!-- end-user-doc -->
  * <p>
  * The following features are implemented:
  * <ul>
  *   <li>{@link org.eclipse.gmf.codegen.gmfgen.impl.GenDiagramImpl#getContainedNodes <em>Contained Nodes</em>}</li>
  *   <li>{@link org.eclipse.gmf.codegen.gmfgen.impl.GenDiagramImpl#getCanonicalEditPolicyClassName <em>Canonical Edit Policy Class Name</em>}</li>
  *   <li>{@link org.eclipse.gmf.codegen.gmfgen.impl.GenDiagramImpl#getEditCommandsPackageName <em>Edit Commands Package Name</em>}</li>
  *   <li>{@link org.eclipse.gmf.codegen.gmfgen.impl.GenDiagramImpl#getEditHelpersPackageName <em>Edit Helpers Package Name</em>}</li>
  *   <li>{@link org.eclipse.gmf.codegen.gmfgen.impl.GenDiagramImpl#getEditPartsPackageName <em>Edit Parts Package Name</em>}</li>
  *   <li>{@link org.eclipse.gmf.codegen.gmfgen.impl.GenDiagramImpl#getEditPoliciesPackageName <em>Edit Policies Package Name</em>}</li>
  *   <li>{@link org.eclipse.gmf.codegen.gmfgen.impl.GenDiagramImpl#getPreferencesPackageName <em>Preferences Package Name</em>}</li>
  *   <li>{@link org.eclipse.gmf.codegen.gmfgen.impl.GenDiagramImpl#getProvidersPackageName <em>Providers Package Name</em>}</li>
  *   <li>{@link org.eclipse.gmf.codegen.gmfgen.impl.GenDiagramImpl#getParsersPackageName <em>Parsers Package Name</em>}</li>
  *   <li>{@link org.eclipse.gmf.codegen.gmfgen.impl.GenDiagramImpl#getNotationViewFactoriesPackageName <em>Notation View Factories Package Name</em>}</li>
  *   <li>{@link org.eclipse.gmf.codegen.gmfgen.impl.GenDiagramImpl#getElementTypesClassName <em>Element Types Class Name</em>}</li>
  *   <li>{@link org.eclipse.gmf.codegen.gmfgen.impl.GenDiagramImpl#getNotationViewProviderClassName <em>Notation View Provider Class Name</em>}</li>
  *   <li>{@link org.eclipse.gmf.codegen.gmfgen.impl.GenDiagramImpl#getNotationViewProviderPriority <em>Notation View Provider Priority</em>}</li>
  *   <li>{@link org.eclipse.gmf.codegen.gmfgen.impl.GenDiagramImpl#getEditPartProviderClassName <em>Edit Part Provider Class Name</em>}</li>
  *   <li>{@link org.eclipse.gmf.codegen.gmfgen.impl.GenDiagramImpl#getEditPartProviderPriority <em>Edit Part Provider Priority</em>}</li>
  *   <li>{@link org.eclipse.gmf.codegen.gmfgen.impl.GenDiagramImpl#getModelingAssistantProviderClassName <em>Modeling Assistant Provider Class Name</em>}</li>
  *   <li>{@link org.eclipse.gmf.codegen.gmfgen.impl.GenDiagramImpl#getModelingAssistantProviderPriority <em>Modeling Assistant Provider Priority</em>}</li>
  *   <li>{@link org.eclipse.gmf.codegen.gmfgen.impl.GenDiagramImpl#getIconProviderClassName <em>Icon Provider Class Name</em>}</li>
  *   <li>{@link org.eclipse.gmf.codegen.gmfgen.impl.GenDiagramImpl#getIconProviderPriority <em>Icon Provider Priority</em>}</li>
  *   <li>{@link org.eclipse.gmf.codegen.gmfgen.impl.GenDiagramImpl#getParserProviderClassName <em>Parser Provider Class Name</em>}</li>
  *   <li>{@link org.eclipse.gmf.codegen.gmfgen.impl.GenDiagramImpl#getParserProviderPriority <em>Parser Provider Priority</em>}</li>
  *   <li>{@link org.eclipse.gmf.codegen.gmfgen.impl.GenDiagramImpl#getContributionItemProviderClassName <em>Contribution Item Provider Class Name</em>}</li>
  *   <li>{@link org.eclipse.gmf.codegen.gmfgen.impl.GenDiagramImpl#getReorientConnectionViewCommandClassName <em>Reorient Connection View Command Class Name</em>}</li>
  *   <li>{@link org.eclipse.gmf.codegen.gmfgen.impl.GenDiagramImpl#getBaseEditHelperClassName <em>Base Edit Helper Class Name</em>}</li>
  *   <li>{@link org.eclipse.gmf.codegen.gmfgen.impl.GenDiagramImpl#getEditPartFactoryClassName <em>Edit Part Factory Class Name</em>}</li>
  *   <li>{@link org.eclipse.gmf.codegen.gmfgen.impl.GenDiagramImpl#getBaseExternalNodeLabelEditPartClassName <em>Base External Node Label Edit Part Class Name</em>}</li>
  *   <li>{@link org.eclipse.gmf.codegen.gmfgen.impl.GenDiagramImpl#getBaseItemSemanticEditPolicyClassName <em>Base Item Semantic Edit Policy Class Name</em>}</li>
  *   <li>{@link org.eclipse.gmf.codegen.gmfgen.impl.GenDiagramImpl#getBaseGraphicalNodeEditPolicyClassName <em>Base Graphical Node Edit Policy Class Name</em>}</li>
  *   <li>{@link org.eclipse.gmf.codegen.gmfgen.impl.GenDiagramImpl#getTextSelectionEditPolicyClassName <em>Text Selection Edit Policy Class Name</em>}</li>
  *   <li>{@link org.eclipse.gmf.codegen.gmfgen.impl.GenDiagramImpl#getTextNonResizableEditPolicyClassName <em>Text Non Resizable Edit Policy Class Name</em>}</li>
  *   <li>{@link org.eclipse.gmf.codegen.gmfgen.impl.GenDiagramImpl#getCreationWizardClassName <em>Creation Wizard Class Name</em>}</li>
  *   <li>{@link org.eclipse.gmf.codegen.gmfgen.impl.GenDiagramImpl#getCreationWizardPageClassName <em>Creation Wizard Page Class Name</em>}</li>
  *   <li>{@link org.eclipse.gmf.codegen.gmfgen.impl.GenDiagramImpl#getCreationWizardIconPath <em>Creation Wizard Icon Path</em>}</li>
  *   <li>{@link org.eclipse.gmf.codegen.gmfgen.impl.GenDiagramImpl#getCreationWizardIconPathX <em>Creation Wizard Icon Path X</em>}</li>
  *   <li>{@link org.eclipse.gmf.codegen.gmfgen.impl.GenDiagramImpl#getCreationWizardCategoryID <em>Creation Wizard Category ID</em>}</li>
  *   <li>{@link org.eclipse.gmf.codegen.gmfgen.impl.GenDiagramImpl#getDiagramEditorUtilClassName <em>Diagram Editor Util Class Name</em>}</li>
  *   <li>{@link org.eclipse.gmf.codegen.gmfgen.impl.GenDiagramImpl#getDocumentProviderClassName <em>Document Provider Class Name</em>}</li>
  *   <li>{@link org.eclipse.gmf.codegen.gmfgen.impl.GenDiagramImpl#getInitDiagramFileActionClassName <em>Init Diagram File Action Class Name</em>}</li>
  *   <li>{@link org.eclipse.gmf.codegen.gmfgen.impl.GenDiagramImpl#getNewDiagramFileWizardClassName <em>New Diagram File Wizard Class Name</em>}</li>
  *   <li>{@link org.eclipse.gmf.codegen.gmfgen.impl.GenDiagramImpl#getDiagramContentInitializerClassName <em>Diagram Content Initializer Class Name</em>}</li>
  *   <li>{@link org.eclipse.gmf.codegen.gmfgen.impl.GenDiagramImpl#getMatchingStrategyClassName <em>Matching Strategy Class Name</em>}</li>
  *   <li>{@link org.eclipse.gmf.codegen.gmfgen.impl.GenDiagramImpl#getVisualIDRegistryClassName <em>Visual ID Registry Class Name</em>}</li>
  *   <li>{@link org.eclipse.gmf.codegen.gmfgen.impl.GenDiagramImpl#getElementChooserClassName <em>Element Chooser Class Name</em>}</li>
  *   <li>{@link org.eclipse.gmf.codegen.gmfgen.impl.GenDiagramImpl#getLoadResourceActionClassName <em>Load Resource Action Class Name</em>}</li>
  *   <li>{@link org.eclipse.gmf.codegen.gmfgen.impl.GenDiagramImpl#getEditingDomainID <em>Editing Domain ID</em>}</li>
  *   <li>{@link org.eclipse.gmf.codegen.gmfgen.impl.GenDiagramImpl#getShortcutsDecoratorProviderClassName <em>Shortcuts Decorator Provider Class Name</em>}</li>
  *   <li>{@link org.eclipse.gmf.codegen.gmfgen.impl.GenDiagramImpl#getShortcutsDecoratorProviderPriority <em>Shortcuts Decorator Provider Priority</em>}</li>
  *   <li>{@link org.eclipse.gmf.codegen.gmfgen.impl.GenDiagramImpl#getCreateShortcutActionClassName <em>Create Shortcut Action Class Name</em>}</li>
  *   <li>{@link org.eclipse.gmf.codegen.gmfgen.impl.GenDiagramImpl#getCreateShortcutDecorationsCommandClassName <em>Create Shortcut Decorations Command Class Name</em>}</li>
  *   <li>{@link org.eclipse.gmf.codegen.gmfgen.impl.GenDiagramImpl#getShortcutPropertyTesterClassName <em>Shortcut Property Tester Class Name</em>}</li>
  *   <li>{@link org.eclipse.gmf.codegen.gmfgen.impl.GenDiagramImpl#getContainsShortcutsTo <em>Contains Shortcuts To</em>}</li>
  *   <li>{@link org.eclipse.gmf.codegen.gmfgen.impl.GenDiagramImpl#getShortcutsProvidedFor <em>Shortcuts Provided For</em>}</li>
  *   <li>{@link org.eclipse.gmf.codegen.gmfgen.impl.GenDiagramImpl#getValidationProviderClassName <em>Validation Provider Class Name</em>}</li>
  *   <li>{@link org.eclipse.gmf.codegen.gmfgen.impl.GenDiagramImpl#getValidationProviderPriority <em>Validation Provider Priority</em>}</li>
  *   <li>{@link org.eclipse.gmf.codegen.gmfgen.impl.GenDiagramImpl#getMarkerNavigationProviderClassName <em>Marker Navigation Provider Class Name</em>}</li>
  *   <li>{@link org.eclipse.gmf.codegen.gmfgen.impl.GenDiagramImpl#getMarkerNavigationProviderPriority <em>Marker Navigation Provider Priority</em>}</li>
  *   <li>{@link org.eclipse.gmf.codegen.gmfgen.impl.GenDiagramImpl#isValidationEnabled <em>Validation Enabled</em>}</li>
  *   <li>{@link org.eclipse.gmf.codegen.gmfgen.impl.GenDiagramImpl#getMetricProviderClassName <em>Metric Provider Class Name</em>}</li>
  *   <li>{@link org.eclipse.gmf.codegen.gmfgen.impl.GenDiagramImpl#getMetricProviderPriority <em>Metric Provider Priority</em>}</li>
  *   <li>{@link org.eclipse.gmf.codegen.gmfgen.impl.GenDiagramImpl#getValidationDecoratorProviderClassName <em>Validation Decorator Provider Class Name</em>}</li>
  *   <li>{@link org.eclipse.gmf.codegen.gmfgen.impl.GenDiagramImpl#isValidationDecorators <em>Validation Decorators</em>}</li>
  *   <li>{@link org.eclipse.gmf.codegen.gmfgen.impl.GenDiagramImpl#getValidationDecoratorProviderPriority <em>Validation Decorator Provider Priority</em>}</li>
  *   <li>{@link org.eclipse.gmf.codegen.gmfgen.impl.GenDiagramImpl#isLiveValidationUIFeedback <em>Live Validation UI Feedback</em>}</li>
  *   <li>{@link org.eclipse.gmf.codegen.gmfgen.impl.GenDiagramImpl#getUnits <em>Units</em>}</li>
  *   <li>{@link org.eclipse.gmf.codegen.gmfgen.impl.GenDiagramImpl#getEditorGen <em>Editor Gen</em>}</li>
  *   <li>{@link org.eclipse.gmf.codegen.gmfgen.impl.GenDiagramImpl#getDomainDiagramElement <em>Domain Diagram Element</em>}</li>
  *   <li>{@link org.eclipse.gmf.codegen.gmfgen.impl.GenDiagramImpl#getChildNodes <em>Child Nodes</em>}</li>
  *   <li>{@link org.eclipse.gmf.codegen.gmfgen.impl.GenDiagramImpl#getTopLevelNodes <em>Top Level Nodes</em>}</li>
  *   <li>{@link org.eclipse.gmf.codegen.gmfgen.impl.GenDiagramImpl#getLinks <em>Links</em>}</li>
  *   <li>{@link org.eclipse.gmf.codegen.gmfgen.impl.GenDiagramImpl#getCompartments <em>Compartments</em>}</li>
  *   <li>{@link org.eclipse.gmf.codegen.gmfgen.impl.GenDiagramImpl#getPalette <em>Palette</em>}</li>
  *   <li>{@link org.eclipse.gmf.codegen.gmfgen.impl.GenDiagramImpl#isSynchronized <em>Synchronized</em>}</li>
  *   <li>{@link org.eclipse.gmf.codegen.gmfgen.impl.GenDiagramImpl#getPreferences <em>Preferences</em>}</li>
  *   <li>{@link org.eclipse.gmf.codegen.gmfgen.impl.GenDiagramImpl#getPreferencePages <em>Preference Pages</em>}</li>
  * </ul>
  * </p>
  *
  * @generated
  */
 public class GenDiagramImpl extends GenCommonBaseImpl implements GenDiagram {
 
 	/**
 	 * The default value of the '{@link #getCanonicalEditPolicyClassName() <em>Canonical Edit Policy Class Name</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getCanonicalEditPolicyClassName()
 	 * @generated
 	 * @ordered
 	 */
 	protected static final String CANONICAL_EDIT_POLICY_CLASS_NAME_EDEFAULT = null;
 
 	/**
 	 * The cached value of the '{@link #getCanonicalEditPolicyClassName() <em>Canonical Edit Policy Class Name</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getCanonicalEditPolicyClassName()
 	 * @generated
 	 * @ordered
 	 */
 	protected String canonicalEditPolicyClassName = CANONICAL_EDIT_POLICY_CLASS_NAME_EDEFAULT;
 
 	/**
 	 * The default value of the '{@link #getEditCommandsPackageName() <em>Edit Commands Package Name</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getEditCommandsPackageName()
 	 * @generated
 	 * @ordered
 	 */
 	protected static final String EDIT_COMMANDS_PACKAGE_NAME_EDEFAULT = null;
 
 	/**
 	 * The cached value of the '{@link #getEditCommandsPackageName() <em>Edit Commands Package Name</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getEditCommandsPackageName()
 	 * @generated
 	 * @ordered
 	 */
 	protected String editCommandsPackageName = EDIT_COMMANDS_PACKAGE_NAME_EDEFAULT;
 
 	/**
 	 * The default value of the '{@link #getEditHelpersPackageName() <em>Edit Helpers Package Name</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getEditHelpersPackageName()
 	 * @generated
 	 * @ordered
 	 */
 	protected static final String EDIT_HELPERS_PACKAGE_NAME_EDEFAULT = null;
 
 	/**
 	 * The cached value of the '{@link #getEditHelpersPackageName() <em>Edit Helpers Package Name</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getEditHelpersPackageName()
 	 * @generated
 	 * @ordered
 	 */
 	protected String editHelpersPackageName = EDIT_HELPERS_PACKAGE_NAME_EDEFAULT;
 
 	/**
 	 * The default value of the '{@link #getEditPartsPackageName() <em>Edit Parts Package Name</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getEditPartsPackageName()
 	 * @generated
 	 * @ordered
 	 */
 	protected static final String EDIT_PARTS_PACKAGE_NAME_EDEFAULT = null;
 
 	/**
 	 * The cached value of the '{@link #getEditPartsPackageName() <em>Edit Parts Package Name</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getEditPartsPackageName()
 	 * @generated
 	 * @ordered
 	 */
 	protected String editPartsPackageName = EDIT_PARTS_PACKAGE_NAME_EDEFAULT;
 
 	/**
 	 * The default value of the '{@link #getEditPoliciesPackageName() <em>Edit Policies Package Name</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getEditPoliciesPackageName()
 	 * @generated
 	 * @ordered
 	 */
 	protected static final String EDIT_POLICIES_PACKAGE_NAME_EDEFAULT = null;
 
 	/**
 	 * The cached value of the '{@link #getEditPoliciesPackageName() <em>Edit Policies Package Name</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getEditPoliciesPackageName()
 	 * @generated
 	 * @ordered
 	 */
 	protected String editPoliciesPackageName = EDIT_POLICIES_PACKAGE_NAME_EDEFAULT;
 
 	/**
 	 * The default value of the '{@link #getPreferencesPackageName() <em>Preferences Package Name</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getPreferencesPackageName()
 	 * @generated
 	 * @ordered
 	 */
 	protected static final String PREFERENCES_PACKAGE_NAME_EDEFAULT = null;
 
 	/**
 	 * The cached value of the '{@link #getPreferencesPackageName() <em>Preferences Package Name</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getPreferencesPackageName()
 	 * @generated
 	 * @ordered
 	 */
 	protected String preferencesPackageName = PREFERENCES_PACKAGE_NAME_EDEFAULT;
 
 	/**
 	 * The default value of the '{@link #getProvidersPackageName() <em>Providers Package Name</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getProvidersPackageName()
 	 * @generated
 	 * @ordered
 	 */
 	protected static final String PROVIDERS_PACKAGE_NAME_EDEFAULT = null;
 
 	/**
 	 * The cached value of the '{@link #getProvidersPackageName() <em>Providers Package Name</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getProvidersPackageName()
 	 * @generated
 	 * @ordered
 	 */
 	protected String providersPackageName = PROVIDERS_PACKAGE_NAME_EDEFAULT;
 
 	/**
 	 * The default value of the '{@link #getParsersPackageName() <em>Parsers Package Name</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getParsersPackageName()
 	 * @generated
 	 * @ordered
 	 */
 	protected static final String PARSERS_PACKAGE_NAME_EDEFAULT = null;
 
 	/**
 	 * The cached value of the '{@link #getParsersPackageName() <em>Parsers Package Name</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getParsersPackageName()
 	 * @generated
 	 * @ordered
 	 */
 	protected String parsersPackageName = PARSERS_PACKAGE_NAME_EDEFAULT;
 
 	/**
 	 * The default value of the '{@link #getNotationViewFactoriesPackageName() <em>Notation View Factories Package Name</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getNotationViewFactoriesPackageName()
 	 * @generated
 	 * @ordered
 	 */
 	protected static final String NOTATION_VIEW_FACTORIES_PACKAGE_NAME_EDEFAULT = null;
 
 	/**
 	 * The cached value of the '{@link #getNotationViewFactoriesPackageName() <em>Notation View Factories Package Name</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getNotationViewFactoriesPackageName()
 	 * @generated
 	 * @ordered
 	 */
 	protected String notationViewFactoriesPackageName = NOTATION_VIEW_FACTORIES_PACKAGE_NAME_EDEFAULT;
 
 	/**
 	 * The default value of the '{@link #getElementTypesClassName() <em>Element Types Class Name</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getElementTypesClassName()
 	 * @generated
 	 * @ordered
 	 */
 	protected static final String ELEMENT_TYPES_CLASS_NAME_EDEFAULT = null;
 
 	/**
 	 * The cached value of the '{@link #getElementTypesClassName() <em>Element Types Class Name</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getElementTypesClassName()
 	 * @generated
 	 * @ordered
 	 */
 	protected String elementTypesClassName = ELEMENT_TYPES_CLASS_NAME_EDEFAULT;
 
 	/**
 	 * The default value of the '{@link #getNotationViewProviderClassName() <em>Notation View Provider Class Name</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getNotationViewProviderClassName()
 	 * @generated
 	 * @ordered
 	 */
 	protected static final String NOTATION_VIEW_PROVIDER_CLASS_NAME_EDEFAULT = null;
 
 	/**
 	 * The cached value of the '{@link #getNotationViewProviderClassName() <em>Notation View Provider Class Name</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getNotationViewProviderClassName()
 	 * @generated
 	 * @ordered
 	 */
 	protected String notationViewProviderClassName = NOTATION_VIEW_PROVIDER_CLASS_NAME_EDEFAULT;
 
 	/**
 	 * The default value of the '{@link #getNotationViewProviderPriority() <em>Notation View Provider Priority</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getNotationViewProviderPriority()
 	 * @generated
 	 * @ordered
 	 */
 	protected static final ProviderPriority NOTATION_VIEW_PROVIDER_PRIORITY_EDEFAULT = ProviderPriority.LOWEST_LITERAL;
 
 	/**
 	 * The cached value of the '{@link #getNotationViewProviderPriority() <em>Notation View Provider Priority</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getNotationViewProviderPriority()
 	 * @generated
 	 * @ordered
 	 */
 	protected ProviderPriority notationViewProviderPriority = NOTATION_VIEW_PROVIDER_PRIORITY_EDEFAULT;
 
 	/**
 	 * The default value of the '{@link #getEditPartProviderClassName() <em>Edit Part Provider Class Name</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getEditPartProviderClassName()
 	 * @generated
 	 * @ordered
 	 */
 	protected static final String EDIT_PART_PROVIDER_CLASS_NAME_EDEFAULT = null;
 
 	/**
 	 * The cached value of the '{@link #getEditPartProviderClassName() <em>Edit Part Provider Class Name</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getEditPartProviderClassName()
 	 * @generated
 	 * @ordered
 	 */
 	protected String editPartProviderClassName = EDIT_PART_PROVIDER_CLASS_NAME_EDEFAULT;
 
 	/**
 	 * The default value of the '{@link #getEditPartProviderPriority() <em>Edit Part Provider Priority</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getEditPartProviderPriority()
 	 * @generated
 	 * @ordered
 	 */
 	protected static final ProviderPriority EDIT_PART_PROVIDER_PRIORITY_EDEFAULT = ProviderPriority.LOWEST_LITERAL;
 
 	/**
 	 * The cached value of the '{@link #getEditPartProviderPriority() <em>Edit Part Provider Priority</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getEditPartProviderPriority()
 	 * @generated
 	 * @ordered
 	 */
 	protected ProviderPriority editPartProviderPriority = EDIT_PART_PROVIDER_PRIORITY_EDEFAULT;
 
 	/**
 	 * The default value of the '{@link #getModelingAssistantProviderClassName() <em>Modeling Assistant Provider Class Name</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getModelingAssistantProviderClassName()
 	 * @generated
 	 * @ordered
 	 */
 	protected static final String MODELING_ASSISTANT_PROVIDER_CLASS_NAME_EDEFAULT = null;
 
 	/**
 	 * The cached value of the '{@link #getModelingAssistantProviderClassName() <em>Modeling Assistant Provider Class Name</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getModelingAssistantProviderClassName()
 	 * @generated
 	 * @ordered
 	 */
 	protected String modelingAssistantProviderClassName = MODELING_ASSISTANT_PROVIDER_CLASS_NAME_EDEFAULT;
 
 	/**
 	 * The default value of the '{@link #getModelingAssistantProviderPriority() <em>Modeling Assistant Provider Priority</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getModelingAssistantProviderPriority()
 	 * @generated
 	 * @ordered
 	 */
 	protected static final ProviderPriority MODELING_ASSISTANT_PROVIDER_PRIORITY_EDEFAULT = ProviderPriority.LOWEST_LITERAL;
 
 	/**
 	 * The cached value of the '{@link #getModelingAssistantProviderPriority() <em>Modeling Assistant Provider Priority</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getModelingAssistantProviderPriority()
 	 * @generated
 	 * @ordered
 	 */
 	protected ProviderPriority modelingAssistantProviderPriority = MODELING_ASSISTANT_PROVIDER_PRIORITY_EDEFAULT;
 
 	/**
 	 * The default value of the '{@link #getIconProviderClassName() <em>Icon Provider Class Name</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getIconProviderClassName()
 	 * @generated
 	 * @ordered
 	 */
 	protected static final String ICON_PROVIDER_CLASS_NAME_EDEFAULT = null;
 
 	/**
 	 * The cached value of the '{@link #getIconProviderClassName() <em>Icon Provider Class Name</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getIconProviderClassName()
 	 * @generated
 	 * @ordered
 	 */
 	protected String iconProviderClassName = ICON_PROVIDER_CLASS_NAME_EDEFAULT;
 
 	/**
 	 * The default value of the '{@link #getIconProviderPriority() <em>Icon Provider Priority</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getIconProviderPriority()
 	 * @generated
 	 * @ordered
 	 */
 	protected static final ProviderPriority ICON_PROVIDER_PRIORITY_EDEFAULT = ProviderPriority.LOWEST_LITERAL;
 
 	/**
 	 * The cached value of the '{@link #getIconProviderPriority() <em>Icon Provider Priority</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getIconProviderPriority()
 	 * @generated
 	 * @ordered
 	 */
 	protected ProviderPriority iconProviderPriority = ICON_PROVIDER_PRIORITY_EDEFAULT;
 
 	/**
 	 * The default value of the '{@link #getParserProviderClassName() <em>Parser Provider Class Name</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getParserProviderClassName()
 	 * @generated
 	 * @ordered
 	 */
 	protected static final String PARSER_PROVIDER_CLASS_NAME_EDEFAULT = null;
 
 	/**
 	 * The cached value of the '{@link #getParserProviderClassName() <em>Parser Provider Class Name</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getParserProviderClassName()
 	 * @generated
 	 * @ordered
 	 */
 	protected String parserProviderClassName = PARSER_PROVIDER_CLASS_NAME_EDEFAULT;
 
 	/**
 	 * The default value of the '{@link #getParserProviderPriority() <em>Parser Provider Priority</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getParserProviderPriority()
 	 * @generated
 	 * @ordered
 	 */
 	protected static final ProviderPriority PARSER_PROVIDER_PRIORITY_EDEFAULT = ProviderPriority.LOWEST_LITERAL;
 
 	/**
 	 * The cached value of the '{@link #getParserProviderPriority() <em>Parser Provider Priority</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getParserProviderPriority()
 	 * @generated
 	 * @ordered
 	 */
 	protected ProviderPriority parserProviderPriority = PARSER_PROVIDER_PRIORITY_EDEFAULT;
 
 	/**
 	 * The default value of the '{@link #getContributionItemProviderClassName() <em>Contribution Item Provider Class Name</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getContributionItemProviderClassName()
 	 * @generated
 	 * @ordered
 	 */
 	protected static final String CONTRIBUTION_ITEM_PROVIDER_CLASS_NAME_EDEFAULT = null;
 
 	/**
 	 * The cached value of the '{@link #getContributionItemProviderClassName() <em>Contribution Item Provider Class Name</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getContributionItemProviderClassName()
 	 * @generated
 	 * @ordered
 	 */
 	protected String contributionItemProviderClassName = CONTRIBUTION_ITEM_PROVIDER_CLASS_NAME_EDEFAULT;
 
 	/**
 	 * The default value of the '{@link #getReorientConnectionViewCommandClassName() <em>Reorient Connection View Command Class Name</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getReorientConnectionViewCommandClassName()
 	 * @generated
 	 * @ordered
 	 */
 	protected static final String REORIENT_CONNECTION_VIEW_COMMAND_CLASS_NAME_EDEFAULT = null;
 
 	/**
 	 * The cached value of the '{@link #getReorientConnectionViewCommandClassName() <em>Reorient Connection View Command Class Name</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getReorientConnectionViewCommandClassName()
 	 * @generated
 	 * @ordered
 	 */
 	protected String reorientConnectionViewCommandClassName = REORIENT_CONNECTION_VIEW_COMMAND_CLASS_NAME_EDEFAULT;
 
 	/**
 	 * The default value of the '{@link #getBaseEditHelperClassName() <em>Base Edit Helper Class Name</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getBaseEditHelperClassName()
 	 * @generated
 	 * @ordered
 	 */
 	protected static final String BASE_EDIT_HELPER_CLASS_NAME_EDEFAULT = null;
 
 	/**
 	 * The cached value of the '{@link #getBaseEditHelperClassName() <em>Base Edit Helper Class Name</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getBaseEditHelperClassName()
 	 * @generated
 	 * @ordered
 	 */
 	protected String baseEditHelperClassName = BASE_EDIT_HELPER_CLASS_NAME_EDEFAULT;
 
 	/**
 	 * The default value of the '{@link #getEditPartFactoryClassName() <em>Edit Part Factory Class Name</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getEditPartFactoryClassName()
 	 * @generated
 	 * @ordered
 	 */
 	protected static final String EDIT_PART_FACTORY_CLASS_NAME_EDEFAULT = null;
 
 	/**
 	 * The cached value of the '{@link #getEditPartFactoryClassName() <em>Edit Part Factory Class Name</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getEditPartFactoryClassName()
 	 * @generated
 	 * @ordered
 	 */
 	protected String editPartFactoryClassName = EDIT_PART_FACTORY_CLASS_NAME_EDEFAULT;
 
 	/**
 	 * The default value of the '{@link #getBaseExternalNodeLabelEditPartClassName() <em>Base External Node Label Edit Part Class Name</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getBaseExternalNodeLabelEditPartClassName()
 	 * @generated
 	 * @ordered
 	 */
 	protected static final String BASE_EXTERNAL_NODE_LABEL_EDIT_PART_CLASS_NAME_EDEFAULT = null;
 
 	/**
 	 * The cached value of the '{@link #getBaseExternalNodeLabelEditPartClassName() <em>Base External Node Label Edit Part Class Name</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getBaseExternalNodeLabelEditPartClassName()
 	 * @generated
 	 * @ordered
 	 */
 	protected String baseExternalNodeLabelEditPartClassName = BASE_EXTERNAL_NODE_LABEL_EDIT_PART_CLASS_NAME_EDEFAULT;
 
 	/**
 	 * The default value of the '{@link #getBaseItemSemanticEditPolicyClassName() <em>Base Item Semantic Edit Policy Class Name</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getBaseItemSemanticEditPolicyClassName()
 	 * @generated
 	 * @ordered
 	 */
 	protected static final String BASE_ITEM_SEMANTIC_EDIT_POLICY_CLASS_NAME_EDEFAULT = null;
 
 	/**
 	 * The cached value of the '{@link #getBaseItemSemanticEditPolicyClassName() <em>Base Item Semantic Edit Policy Class Name</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getBaseItemSemanticEditPolicyClassName()
 	 * @generated
 	 * @ordered
 	 */
 	protected String baseItemSemanticEditPolicyClassName = BASE_ITEM_SEMANTIC_EDIT_POLICY_CLASS_NAME_EDEFAULT;
 
 	/**
 	 * The default value of the '{@link #getBaseGraphicalNodeEditPolicyClassName() <em>Base Graphical Node Edit Policy Class Name</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getBaseGraphicalNodeEditPolicyClassName()
 	 * @generated
 	 * @ordered
 	 */
 	protected static final String BASE_GRAPHICAL_NODE_EDIT_POLICY_CLASS_NAME_EDEFAULT = null;
 
 	/**
 	 * The cached value of the '{@link #getBaseGraphicalNodeEditPolicyClassName() <em>Base Graphical Node Edit Policy Class Name</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getBaseGraphicalNodeEditPolicyClassName()
 	 * @generated
 	 * @ordered
 	 */
 	protected String baseGraphicalNodeEditPolicyClassName = BASE_GRAPHICAL_NODE_EDIT_POLICY_CLASS_NAME_EDEFAULT;
 
 	/**
 	 * The default value of the '{@link #getTextSelectionEditPolicyClassName() <em>Text Selection Edit Policy Class Name</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getTextSelectionEditPolicyClassName()
 	 * @generated
 	 * @ordered
 	 */
 	protected static final String TEXT_SELECTION_EDIT_POLICY_CLASS_NAME_EDEFAULT = null;
 
 	/**
 	 * The cached value of the '{@link #getTextSelectionEditPolicyClassName() <em>Text Selection Edit Policy Class Name</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getTextSelectionEditPolicyClassName()
 	 * @generated
 	 * @ordered
 	 */
 	protected String textSelectionEditPolicyClassName = TEXT_SELECTION_EDIT_POLICY_CLASS_NAME_EDEFAULT;
 
 	/**
 	 * The default value of the '{@link #getTextNonResizableEditPolicyClassName() <em>Text Non Resizable Edit Policy Class Name</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getTextNonResizableEditPolicyClassName()
 	 * @generated
 	 * @ordered
 	 */
 	protected static final String TEXT_NON_RESIZABLE_EDIT_POLICY_CLASS_NAME_EDEFAULT = null;
 
 	/**
 	 * The cached value of the '{@link #getTextNonResizableEditPolicyClassName() <em>Text Non Resizable Edit Policy Class Name</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getTextNonResizableEditPolicyClassName()
 	 * @generated
 	 * @ordered
 	 */
 	protected String textNonResizableEditPolicyClassName = TEXT_NON_RESIZABLE_EDIT_POLICY_CLASS_NAME_EDEFAULT;
 
 	/**
 	 * The default value of the '{@link #getCreationWizardClassName() <em>Creation Wizard Class Name</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getCreationWizardClassName()
 	 * @generated
 	 * @ordered
 	 */
 	protected static final String CREATION_WIZARD_CLASS_NAME_EDEFAULT = null;
 
 	/**
 	 * The cached value of the '{@link #getCreationWizardClassName() <em>Creation Wizard Class Name</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getCreationWizardClassName()
 	 * @generated
 	 * @ordered
 	 */
 	protected String creationWizardClassName = CREATION_WIZARD_CLASS_NAME_EDEFAULT;
 
 	/**
 	 * The default value of the '{@link #getCreationWizardPageClassName() <em>Creation Wizard Page Class Name</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getCreationWizardPageClassName()
 	 * @generated
 	 * @ordered
 	 */
 	protected static final String CREATION_WIZARD_PAGE_CLASS_NAME_EDEFAULT = null;
 
 	/**
 	 * The cached value of the '{@link #getCreationWizardPageClassName() <em>Creation Wizard Page Class Name</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getCreationWizardPageClassName()
 	 * @generated
 	 * @ordered
 	 */
 	protected String creationWizardPageClassName = CREATION_WIZARD_PAGE_CLASS_NAME_EDEFAULT;
 
 	/**
 	 * The default value of the '{@link #getCreationWizardIconPath() <em>Creation Wizard Icon Path</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getCreationWizardIconPath()
 	 * @generated
 	 * @ordered
 	 */
 	protected static final String CREATION_WIZARD_ICON_PATH_EDEFAULT = null;
 
 	/**
 	 * The cached value of the '{@link #getCreationWizardIconPath() <em>Creation Wizard Icon Path</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getCreationWizardIconPath()
 	 * @generated
 	 * @ordered
 	 */
 	protected String creationWizardIconPath = CREATION_WIZARD_ICON_PATH_EDEFAULT;
 
 	/**
 	 * The default value of the '{@link #getCreationWizardIconPathX() <em>Creation Wizard Icon Path X</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getCreationWizardIconPathX()
 	 * @generated
 	 * @ordered
 	 */
 	protected static final String CREATION_WIZARD_ICON_PATH_X_EDEFAULT = null;
 
 	/**
 	 * The default value of the '{@link #getCreationWizardCategoryID() <em>Creation Wizard Category ID</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getCreationWizardCategoryID()
 	 * @generated
 	 * @ordered
 	 */
 	protected static final String CREATION_WIZARD_CATEGORY_ID_EDEFAULT = null;
 
 	/**
 	 * The cached value of the '{@link #getCreationWizardCategoryID() <em>Creation Wizard Category ID</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getCreationWizardCategoryID()
 	 * @generated
 	 * @ordered
 	 */
 	protected String creationWizardCategoryID = CREATION_WIZARD_CATEGORY_ID_EDEFAULT;
 
 	/**
 	 * The default value of the '{@link #getDiagramEditorUtilClassName() <em>Diagram Editor Util Class Name</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getDiagramEditorUtilClassName()
 	 * @generated
 	 * @ordered
 	 */
 	protected static final String DIAGRAM_EDITOR_UTIL_CLASS_NAME_EDEFAULT = null;
 
 	/**
 	 * The cached value of the '{@link #getDiagramEditorUtilClassName() <em>Diagram Editor Util Class Name</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getDiagramEditorUtilClassName()
 	 * @generated
 	 * @ordered
 	 */
 	protected String diagramEditorUtilClassName = DIAGRAM_EDITOR_UTIL_CLASS_NAME_EDEFAULT;
 
 	/**
 	 * The default value of the '{@link #getDocumentProviderClassName() <em>Document Provider Class Name</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getDocumentProviderClassName()
 	 * @generated
 	 * @ordered
 	 */
 	protected static final String DOCUMENT_PROVIDER_CLASS_NAME_EDEFAULT = null;
 
 	/**
 	 * The cached value of the '{@link #getDocumentProviderClassName() <em>Document Provider Class Name</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getDocumentProviderClassName()
 	 * @generated
 	 * @ordered
 	 */
 	protected String documentProviderClassName = DOCUMENT_PROVIDER_CLASS_NAME_EDEFAULT;
 
 	/**
 	 * The default value of the '{@link #getInitDiagramFileActionClassName() <em>Init Diagram File Action Class Name</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getInitDiagramFileActionClassName()
 	 * @generated
 	 * @ordered
 	 */
 	protected static final String INIT_DIAGRAM_FILE_ACTION_CLASS_NAME_EDEFAULT = null;
 
 	/**
 	 * The cached value of the '{@link #getInitDiagramFileActionClassName() <em>Init Diagram File Action Class Name</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getInitDiagramFileActionClassName()
 	 * @generated
 	 * @ordered
 	 */
 	protected String initDiagramFileActionClassName = INIT_DIAGRAM_FILE_ACTION_CLASS_NAME_EDEFAULT;
 
 	/**
 	 * The default value of the '{@link #getNewDiagramFileWizardClassName() <em>New Diagram File Wizard Class Name</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getNewDiagramFileWizardClassName()
 	 * @generated
 	 * @ordered
 	 */
 	protected static final String NEW_DIAGRAM_FILE_WIZARD_CLASS_NAME_EDEFAULT = null;
 
 	/**
 	 * The cached value of the '{@link #getNewDiagramFileWizardClassName() <em>New Diagram File Wizard Class Name</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getNewDiagramFileWizardClassName()
 	 * @generated
 	 * @ordered
 	 */
 	protected String newDiagramFileWizardClassName = NEW_DIAGRAM_FILE_WIZARD_CLASS_NAME_EDEFAULT;
 
 	/**
 	 * The default value of the '{@link #getDiagramContentInitializerClassName() <em>Diagram Content Initializer Class Name</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getDiagramContentInitializerClassName()
 	 * @generated
 	 * @ordered
 	 */
 	protected static final String DIAGRAM_CONTENT_INITIALIZER_CLASS_NAME_EDEFAULT = null;
 
 	/**
 	 * The cached value of the '{@link #getDiagramContentInitializerClassName() <em>Diagram Content Initializer Class Name</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getDiagramContentInitializerClassName()
 	 * @generated
 	 * @ordered
 	 */
 	protected String diagramContentInitializerClassName = DIAGRAM_CONTENT_INITIALIZER_CLASS_NAME_EDEFAULT;
 
 	/**
 	 * The default value of the '{@link #getMatchingStrategyClassName() <em>Matching Strategy Class Name</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getMatchingStrategyClassName()
 	 * @generated
 	 * @ordered
 	 */
 	protected static final String MATCHING_STRATEGY_CLASS_NAME_EDEFAULT = null;
 
 	/**
 	 * The cached value of the '{@link #getMatchingStrategyClassName() <em>Matching Strategy Class Name</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getMatchingStrategyClassName()
 	 * @generated
 	 * @ordered
 	 */
 	protected String matchingStrategyClassName = MATCHING_STRATEGY_CLASS_NAME_EDEFAULT;
 
 	/**
 	 * The default value of the '{@link #getVisualIDRegistryClassName() <em>Visual ID Registry Class Name</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getVisualIDRegistryClassName()
 	 * @generated
 	 * @ordered
 	 */
 	protected static final String VISUAL_ID_REGISTRY_CLASS_NAME_EDEFAULT = null;
 
 	/**
 	 * The cached value of the '{@link #getVisualIDRegistryClassName() <em>Visual ID Registry Class Name</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getVisualIDRegistryClassName()
 	 * @generated
 	 * @ordered
 	 */
 	protected String visualIDRegistryClassName = VISUAL_ID_REGISTRY_CLASS_NAME_EDEFAULT;
 
 	/**
 	 * The default value of the '{@link #getElementChooserClassName() <em>Element Chooser Class Name</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getElementChooserClassName()
 	 * @generated
 	 * @ordered
 	 */
 	protected static final String ELEMENT_CHOOSER_CLASS_NAME_EDEFAULT = null;
 
 	/**
 	 * The cached value of the '{@link #getElementChooserClassName() <em>Element Chooser Class Name</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getElementChooserClassName()
 	 * @generated
 	 * @ordered
 	 */
 	protected String elementChooserClassName = ELEMENT_CHOOSER_CLASS_NAME_EDEFAULT;
 
 	/**
 	 * The default value of the '{@link #getLoadResourceActionClassName() <em>Load Resource Action Class Name</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getLoadResourceActionClassName()
 	 * @generated
 	 * @ordered
 	 */
 	protected static final String LOAD_RESOURCE_ACTION_CLASS_NAME_EDEFAULT = null;
 
 	/**
 	 * The cached value of the '{@link #getLoadResourceActionClassName() <em>Load Resource Action Class Name</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getLoadResourceActionClassName()
 	 * @generated
 	 * @ordered
 	 */
 	protected String loadResourceActionClassName = LOAD_RESOURCE_ACTION_CLASS_NAME_EDEFAULT;
 
 	/**
 	 * The default value of the '{@link #getEditingDomainID() <em>Editing Domain ID</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getEditingDomainID()
 	 * @generated
 	 * @ordered
 	 */
 	protected static final String EDITING_DOMAIN_ID_EDEFAULT = null;
 
 	/**
 	 * The cached value of the '{@link #getEditingDomainID() <em>Editing Domain ID</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getEditingDomainID()
 	 * @generated
 	 * @ordered
 	 */
 	protected String editingDomainID = EDITING_DOMAIN_ID_EDEFAULT;
 
 	/**
 	 * The default value of the '{@link #getShortcutsDecoratorProviderClassName() <em>Shortcuts Decorator Provider Class Name</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getShortcutsDecoratorProviderClassName()
 	 * @generated
 	 * @ordered
 	 */
 	protected static final String SHORTCUTS_DECORATOR_PROVIDER_CLASS_NAME_EDEFAULT = null;
 
 	/**
 	 * The cached value of the '{@link #getShortcutsDecoratorProviderClassName() <em>Shortcuts Decorator Provider Class Name</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getShortcutsDecoratorProviderClassName()
 	 * @generated
 	 * @ordered
 	 */
 	protected String shortcutsDecoratorProviderClassName = SHORTCUTS_DECORATOR_PROVIDER_CLASS_NAME_EDEFAULT;
 
 	/**
 	 * The default value of the '{@link #getShortcutsDecoratorProviderPriority() <em>Shortcuts Decorator Provider Priority</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getShortcutsDecoratorProviderPriority()
 	 * @generated
 	 * @ordered
 	 */
 	protected static final ProviderPriority SHORTCUTS_DECORATOR_PROVIDER_PRIORITY_EDEFAULT = ProviderPriority.LOWEST_LITERAL;
 
 	/**
 	 * The cached value of the '{@link #getShortcutsDecoratorProviderPriority() <em>Shortcuts Decorator Provider Priority</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getShortcutsDecoratorProviderPriority()
 	 * @generated
 	 * @ordered
 	 */
 	protected ProviderPriority shortcutsDecoratorProviderPriority = SHORTCUTS_DECORATOR_PROVIDER_PRIORITY_EDEFAULT;
 
 	/**
 	 * The default value of the '{@link #getCreateShortcutActionClassName() <em>Create Shortcut Action Class Name</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getCreateShortcutActionClassName()
 	 * @generated
 	 * @ordered
 	 */
 	protected static final String CREATE_SHORTCUT_ACTION_CLASS_NAME_EDEFAULT = null;
 
 	/**
 	 * The cached value of the '{@link #getCreateShortcutActionClassName() <em>Create Shortcut Action Class Name</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getCreateShortcutActionClassName()
 	 * @generated
 	 * @ordered
 	 */
 	protected String createShortcutActionClassName = CREATE_SHORTCUT_ACTION_CLASS_NAME_EDEFAULT;
 
 	/**
 	 * The default value of the '{@link #getCreateShortcutDecorationsCommandClassName() <em>Create Shortcut Decorations Command Class Name</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getCreateShortcutDecorationsCommandClassName()
 	 * @generated
 	 * @ordered
 	 */
 	protected static final String CREATE_SHORTCUT_DECORATIONS_COMMAND_CLASS_NAME_EDEFAULT = null;
 
 	/**
 	 * The cached value of the '{@link #getCreateShortcutDecorationsCommandClassName() <em>Create Shortcut Decorations Command Class Name</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getCreateShortcutDecorationsCommandClassName()
 	 * @generated
 	 * @ordered
 	 */
 	protected String createShortcutDecorationsCommandClassName = CREATE_SHORTCUT_DECORATIONS_COMMAND_CLASS_NAME_EDEFAULT;
 
 	/**
 	 * The default value of the '{@link #getShortcutPropertyTesterClassName() <em>Shortcut Property Tester Class Name</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getShortcutPropertyTesterClassName()
 	 * @generated
 	 * @ordered
 	 */
 	protected static final String SHORTCUT_PROPERTY_TESTER_CLASS_NAME_EDEFAULT = null;
 
 	/**
 	 * The cached value of the '{@link #getShortcutPropertyTesterClassName() <em>Shortcut Property Tester Class Name</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getShortcutPropertyTesterClassName()
 	 * @generated
 	 * @ordered
 	 */
 	protected String shortcutPropertyTesterClassName = SHORTCUT_PROPERTY_TESTER_CLASS_NAME_EDEFAULT;
 
 	/**
 	 * The cached value of the '{@link #getContainsShortcutsTo() <em>Contains Shortcuts To</em>}' attribute list.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getContainsShortcutsTo()
 	 * @generated
 	 * @ordered
 	 */
 	protected EList<String> containsShortcutsTo;
 
 	/**
 	 * The cached value of the '{@link #getShortcutsProvidedFor() <em>Shortcuts Provided For</em>}' attribute list.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getShortcutsProvidedFor()
 	 * @generated
 	 * @ordered
 	 */
 	protected EList<String> shortcutsProvidedFor;
 
 	/**
 	 * The default value of the '{@link #getValidationProviderClassName() <em>Validation Provider Class Name</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getValidationProviderClassName()
 	 * @generated
 	 * @ordered
 	 */
 	protected static final String VALIDATION_PROVIDER_CLASS_NAME_EDEFAULT = null;
 
 	/**
 	 * The cached value of the '{@link #getValidationProviderClassName() <em>Validation Provider Class Name</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getValidationProviderClassName()
 	 * @generated
 	 * @ordered
 	 */
 	protected String validationProviderClassName = VALIDATION_PROVIDER_CLASS_NAME_EDEFAULT;
 
 	/**
 	 * The default value of the '{@link #getValidationProviderPriority() <em>Validation Provider Priority</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getValidationProviderPriority()
 	 * @generated
 	 * @ordered
 	 */
 	protected static final ProviderPriority VALIDATION_PROVIDER_PRIORITY_EDEFAULT = ProviderPriority.LOWEST_LITERAL;
 
 	/**
 	 * The cached value of the '{@link #getValidationProviderPriority() <em>Validation Provider Priority</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getValidationProviderPriority()
 	 * @generated
 	 * @ordered
 	 */
 	protected ProviderPriority validationProviderPriority = VALIDATION_PROVIDER_PRIORITY_EDEFAULT;
 
 	/**
 	 * The default value of the '{@link #getMarkerNavigationProviderClassName() <em>Marker Navigation Provider Class Name</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getMarkerNavigationProviderClassName()
 	 * @generated
 	 * @ordered
 	 */
 	protected static final String MARKER_NAVIGATION_PROVIDER_CLASS_NAME_EDEFAULT = null;
 
 	/**
 	 * The cached value of the '{@link #getMarkerNavigationProviderClassName() <em>Marker Navigation Provider Class Name</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getMarkerNavigationProviderClassName()
 	 * @generated
 	 * @ordered
 	 */
 	protected String markerNavigationProviderClassName = MARKER_NAVIGATION_PROVIDER_CLASS_NAME_EDEFAULT;
 
 	/**
 	 * The default value of the '{@link #getMarkerNavigationProviderPriority() <em>Marker Navigation Provider Priority</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getMarkerNavigationProviderPriority()
 	 * @generated
 	 * @ordered
 	 */
 	protected static final ProviderPriority MARKER_NAVIGATION_PROVIDER_PRIORITY_EDEFAULT = ProviderPriority.LOWEST_LITERAL;
 
 	/**
 	 * The cached value of the '{@link #getMarkerNavigationProviderPriority() <em>Marker Navigation Provider Priority</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getMarkerNavigationProviderPriority()
 	 * @generated
 	 * @ordered
 	 */
 	protected ProviderPriority markerNavigationProviderPriority = MARKER_NAVIGATION_PROVIDER_PRIORITY_EDEFAULT;
 
 	/**
 	 * The default value of the '{@link #isValidationEnabled() <em>Validation Enabled</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #isValidationEnabled()
 	 * @generated
 	 * @ordered
 	 */
 	protected static final boolean VALIDATION_ENABLED_EDEFAULT = false;
 
 	/**
 	 * The cached value of the '{@link #isValidationEnabled() <em>Validation Enabled</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #isValidationEnabled()
 	 * @generated
 	 * @ordered
 	 */
 	protected boolean validationEnabled = VALIDATION_ENABLED_EDEFAULT;
 
 	/**
 	 * The default value of the '{@link #getMetricProviderClassName() <em>Metric Provider Class Name</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getMetricProviderClassName()
 	 * @generated
 	 * @ordered
 	 */
 	protected static final String METRIC_PROVIDER_CLASS_NAME_EDEFAULT = null;
 
 	/**
 	 * The cached value of the '{@link #getMetricProviderClassName() <em>Metric Provider Class Name</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getMetricProviderClassName()
 	 * @generated
 	 * @ordered
 	 */
 	protected String metricProviderClassName = METRIC_PROVIDER_CLASS_NAME_EDEFAULT;
 
 	/**
 	 * The default value of the '{@link #getMetricProviderPriority() <em>Metric Provider Priority</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getMetricProviderPriority()
 	 * @generated
 	 * @ordered
 	 */
 	protected static final ProviderPriority METRIC_PROVIDER_PRIORITY_EDEFAULT = ProviderPriority.LOWEST_LITERAL;
 
 	/**
 	 * The cached value of the '{@link #getMetricProviderPriority() <em>Metric Provider Priority</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getMetricProviderPriority()
 	 * @generated
 	 * @ordered
 	 */
 	protected ProviderPriority metricProviderPriority = METRIC_PROVIDER_PRIORITY_EDEFAULT;
 
 	/**
 	 * The default value of the '{@link #getValidationDecoratorProviderClassName() <em>Validation Decorator Provider Class Name</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getValidationDecoratorProviderClassName()
 	 * @generated
 	 * @ordered
 	 */
 	protected static final String VALIDATION_DECORATOR_PROVIDER_CLASS_NAME_EDEFAULT = null;
 
 	/**
 	 * The cached value of the '{@link #getValidationDecoratorProviderClassName() <em>Validation Decorator Provider Class Name</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getValidationDecoratorProviderClassName()
 	 * @generated
 	 * @ordered
 	 */
 	protected String validationDecoratorProviderClassName = VALIDATION_DECORATOR_PROVIDER_CLASS_NAME_EDEFAULT;
 
 	/**
 	 * The default value of the '{@link #isValidationDecorators() <em>Validation Decorators</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #isValidationDecorators()
 	 * @generated
 	 * @ordered
 	 */
 	protected static final boolean VALIDATION_DECORATORS_EDEFAULT = false;
 
 	/**
 	 * The cached value of the '{@link #isValidationDecorators() <em>Validation Decorators</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #isValidationDecorators()
 	 * @generated
 	 * @ordered
 	 */
 	protected boolean validationDecorators = VALIDATION_DECORATORS_EDEFAULT;
 
 	/**
 	 * The default value of the '{@link #getValidationDecoratorProviderPriority() <em>Validation Decorator Provider Priority</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getValidationDecoratorProviderPriority()
 	 * @generated
 	 * @ordered
 	 */
 	protected static final ProviderPriority VALIDATION_DECORATOR_PROVIDER_PRIORITY_EDEFAULT = ProviderPriority.LOWEST_LITERAL;
 
 	/**
 	 * The cached value of the '{@link #getValidationDecoratorProviderPriority() <em>Validation Decorator Provider Priority</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getValidationDecoratorProviderPriority()
 	 * @generated
 	 * @ordered
 	 */
 	protected ProviderPriority validationDecoratorProviderPriority = VALIDATION_DECORATOR_PROVIDER_PRIORITY_EDEFAULT;
 
 	/**
 	 * The default value of the '{@link #isLiveValidationUIFeedback() <em>Live Validation UI Feedback</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #isLiveValidationUIFeedback()
 	 * @generated
 	 * @ordered
 	 */
 	protected static final boolean LIVE_VALIDATION_UI_FEEDBACK_EDEFAULT = false;
 
 	/**
 	 * The cached value of the '{@link #isLiveValidationUIFeedback() <em>Live Validation UI Feedback</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #isLiveValidationUIFeedback()
 	 * @generated
 	 * @ordered
 	 */
 	protected boolean liveValidationUIFeedback = LIVE_VALIDATION_UI_FEEDBACK_EDEFAULT;
 
 	/**
 	 * The default value of the '{@link #getUnits() <em>Units</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getUnits()
 	 * @generated
 	 * @ordered
 	 */
 	protected static final String UNITS_EDEFAULT = "Pixel";
 
 	/**
 	 * The cached value of the '{@link #getUnits() <em>Units</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getUnits()
 	 * @generated
 	 * @ordered
 	 */
 	protected String units = UNITS_EDEFAULT;
 
 	/**
 	 * The cached value of the '{@link #getDomainDiagramElement() <em>Domain Diagram Element</em>}' reference.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getDomainDiagramElement()
 	 * @generated
 	 * @ordered
 	 */
 	protected GenClass domainDiagramElement;
 
 	/**
 	 * The cached value of the '{@link #getChildNodes() <em>Child Nodes</em>}' containment reference list.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getChildNodes()
 	 * @generated
 	 * @ordered
 	 */
 	protected EList<GenChildNode> childNodes;
 
 	/**
 	 * The cached value of the '{@link #getTopLevelNodes() <em>Top Level Nodes</em>}' containment reference list.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getTopLevelNodes()
 	 * @generated
 	 * @ordered
 	 */
 	protected EList<GenTopLevelNode> topLevelNodes;
 
 	/**
 	 * The cached value of the '{@link #getLinks() <em>Links</em>}' containment reference list.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getLinks()
 	 * @generated
 	 * @ordered
 	 */
 	protected EList<GenLink> links;
 
 	/**
 	 * The cached value of the '{@link #getCompartments() <em>Compartments</em>}' containment reference list.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getCompartments()
 	 * @generated
 	 * @ordered
 	 */
 	protected EList<GenCompartment> compartments;
 
 	/**
 	 * The cached value of the '{@link #getPalette() <em>Palette</em>}' containment reference.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getPalette()
 	 * @generated
 	 * @ordered
 	 */
 	protected Palette palette;
 
 	/**
 	 * The default value of the '{@link #isSynchronized() <em>Synchronized</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #isSynchronized()
 	 * @generated
 	 * @ordered
 	 */
 	protected static final boolean SYNCHRONIZED_EDEFAULT = true;
 
 	/**
 	 * The cached value of the '{@link #isSynchronized() <em>Synchronized</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #isSynchronized()
 	 * @generated
 	 * @ordered
 	 */
 	protected boolean synchronized_ = SYNCHRONIZED_EDEFAULT;
 
 	/**
 	 * The cached value of the '{@link #getPreferences() <em>Preferences</em>}' containment reference.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getPreferences()
 	 * @generated
 	 * @ordered
 	 */
 	protected GenDiagramPreferences preferences;
 
 	/**
 	 * The cached value of the '{@link #getPreferencePages() <em>Preference Pages</em>}' containment reference list.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getPreferencePages()
 	 * @generated
 	 * @ordered
 	 */
 	protected EList<GenPreferencePage> preferencePages;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	protected GenDiagramImpl() {
 		super();
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	@Override
 	protected EClass eStaticClass() {
 		return GMFGenPackage.eINSTANCE.getGenDiagram();
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated NOT
 	 */
 	public EList<GenNode> getContainedNodes() {
 		List<GenTopLevelNode> topLevelNodes = getTopLevelNodes();
 		return new EcoreEList.UnmodifiableEList<GenNode>(this, GMFGenPackage.eINSTANCE.getGenContainerBase_ContainedNodes(), topLevelNodes.size(), topLevelNodes.toArray());
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public GenClass getDomainDiagramElement() {
 		if (domainDiagramElement != null && domainDiagramElement.eIsProxy()) {
 			InternalEObject oldDomainDiagramElement = (InternalEObject)domainDiagramElement;
 			domainDiagramElement = (GenClass)eResolveProxy(oldDomainDiagramElement);
 			if (domainDiagramElement != oldDomainDiagramElement) {
 				if (eNotificationRequired())
 					eNotify(new ENotificationImpl(this, Notification.RESOLVE, GMFGenPackage.GEN_DIAGRAM__DOMAIN_DIAGRAM_ELEMENT, oldDomainDiagramElement, domainDiagramElement));
 			}
 		}
 		return domainDiagramElement;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public GenClass basicGetDomainDiagramElement() {
 		return domainDiagramElement;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public void setDomainDiagramElement(GenClass newDomainDiagramElement) {
 		GenClass oldDomainDiagramElement = domainDiagramElement;
 		domainDiagramElement = newDomainDiagramElement;
 		if (eNotificationRequired())
 			eNotify(new ENotificationImpl(this, Notification.SET, GMFGenPackage.GEN_DIAGRAM__DOMAIN_DIAGRAM_ELEMENT, oldDomainDiagramElement, domainDiagramElement));
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EList<GenChildNode> getChildNodes() {
 		if (childNodes == null) {
 			childNodes = new EObjectContainmentWithInverseEList<GenChildNode>(GenChildNode.class, this, GMFGenPackage.GEN_DIAGRAM__CHILD_NODES, GMFGenPackage.GEN_CHILD_NODE__DIAGRAM);
 		}
 		return childNodes;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EList<GenTopLevelNode> getTopLevelNodes() {
 		if (topLevelNodes == null) {
 			topLevelNodes = new EObjectContainmentWithInverseEList<GenTopLevelNode>(GenTopLevelNode.class, this, GMFGenPackage.GEN_DIAGRAM__TOP_LEVEL_NODES, GMFGenPackage.GEN_TOP_LEVEL_NODE__DIAGRAM);
 		}
 		return topLevelNodes;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EList<GenLink> getLinks() {
 		if (links == null) {
 			links = new EObjectContainmentWithInverseEList<GenLink>(GenLink.class, this, GMFGenPackage.GEN_DIAGRAM__LINKS, GMFGenPackage.GEN_LINK__DIAGRAM);
 		}
 		return links;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EList<GenCompartment> getCompartments() {
 		if (compartments == null) {
 			compartments = new EObjectContainmentWithInverseEList<GenCompartment>(GenCompartment.class, this, GMFGenPackage.GEN_DIAGRAM__COMPARTMENTS, GMFGenPackage.GEN_COMPARTMENT__DIAGRAM);
 		}
 		return compartments;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public Palette getPalette() {
 		return palette;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public NotificationChain basicSetPalette(Palette newPalette, NotificationChain msgs) {
 		Palette oldPalette = palette;
 		palette = newPalette;
 		if (eNotificationRequired()) {
 			ENotificationImpl notification = new ENotificationImpl(this, Notification.SET, GMFGenPackage.GEN_DIAGRAM__PALETTE, oldPalette, newPalette);
 			if (msgs == null) msgs = notification; else msgs.add(notification);
 		}
 		return msgs;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public void setPalette(Palette newPalette) {
 		if (newPalette != palette) {
 			NotificationChain msgs = null;
 			if (palette != null)
 				msgs = ((InternalEObject)palette).eInverseRemove(this, GMFGenPackage.PALETTE__DIAGRAM, Palette.class, msgs);
 			if (newPalette != null)
 				msgs = ((InternalEObject)newPalette).eInverseAdd(this, GMFGenPackage.PALETTE__DIAGRAM, Palette.class, msgs);
 			msgs = basicSetPalette(newPalette, msgs);
 			if (msgs != null) msgs.dispatch();
 		}
 		else if (eNotificationRequired())
 			eNotify(new ENotificationImpl(this, Notification.SET, GMFGenPackage.GEN_DIAGRAM__PALETTE, newPalette, newPalette));
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public boolean isSynchronized() {
 		return synchronized_;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public void setSynchronized(boolean newSynchronized) {
 		boolean oldSynchronized = synchronized_;
 		synchronized_ = newSynchronized;
 		if (eNotificationRequired())
 			eNotify(new ENotificationImpl(this, Notification.SET, GMFGenPackage.GEN_DIAGRAM__SYNCHRONIZED, oldSynchronized, synchronized_));
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public GenDiagramPreferences getPreferences() {
 		return preferences;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public NotificationChain basicSetPreferences(GenDiagramPreferences newPreferences, NotificationChain msgs) {
 		GenDiagramPreferences oldPreferences = preferences;
 		preferences = newPreferences;
 		if (eNotificationRequired()) {
 			ENotificationImpl notification = new ENotificationImpl(this, Notification.SET, GMFGenPackage.GEN_DIAGRAM__PREFERENCES, oldPreferences, newPreferences);
 			if (msgs == null) msgs = notification; else msgs.add(notification);
 		}
 		return msgs;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public void setPreferences(GenDiagramPreferences newPreferences) {
 		if (newPreferences != preferences) {
 			NotificationChain msgs = null;
 			if (preferences != null)
 				msgs = ((InternalEObject)preferences).eInverseRemove(this, EOPPOSITE_FEATURE_BASE - GMFGenPackage.GEN_DIAGRAM__PREFERENCES, null, msgs);
 			if (newPreferences != null)
 				msgs = ((InternalEObject)newPreferences).eInverseAdd(this, EOPPOSITE_FEATURE_BASE - GMFGenPackage.GEN_DIAGRAM__PREFERENCES, null, msgs);
 			msgs = basicSetPreferences(newPreferences, msgs);
 			if (msgs != null) msgs.dispatch();
 		}
 		else if (eNotificationRequired())
 			eNotify(new ENotificationImpl(this, Notification.SET, GMFGenPackage.GEN_DIAGRAM__PREFERENCES, newPreferences, newPreferences));
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EList<GenPreferencePage> getPreferencePages() {
 		if (preferencePages == null) {
 			preferencePages = new EObjectContainmentEList<GenPreferencePage>(GenPreferencePage.class, this, GMFGenPackage.GEN_DIAGRAM__PREFERENCE_PAGES);
 		}
 		return preferencePages;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public String getEditCommandsPackageNameGen() {
 		return editCommandsPackageName;
 	}
 
 	public String getEditCommandsPackageName() {
 		String value = getEditCommandsPackageNameGen();
 		if (isEmpty(value)) {
 			value = getEditorGen().getPackageNamePrefix() + ".edit.commands";
 		}
 		return value;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public void setEditCommandsPackageName(String newEditCommandsPackageName) {
 		String oldEditCommandsPackageName = editCommandsPackageName;
 		editCommandsPackageName = newEditCommandsPackageName;
 		if (eNotificationRequired())
 			eNotify(new ENotificationImpl(this, Notification.SET, GMFGenPackage.GEN_DIAGRAM__EDIT_COMMANDS_PACKAGE_NAME, oldEditCommandsPackageName, editCommandsPackageName));
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public String getEditHelpersPackageNameGen() {
 		return editHelpersPackageName;
 	}
 
 	public String getEditHelpersPackageName() {
 		String value = getEditHelpersPackageNameGen();
 		if (isEmpty(value)) {
 			value = getEditorGen().getPackageNamePrefix() + ".edit.helpers";
 		}
 		return value;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public void setEditHelpersPackageName(String newEditHelpersPackageName) {
 		String oldEditHelpersPackageName = editHelpersPackageName;
 		editHelpersPackageName = newEditHelpersPackageName;
 		if (eNotificationRequired())
 			eNotify(new ENotificationImpl(this, Notification.SET, GMFGenPackage.GEN_DIAGRAM__EDIT_HELPERS_PACKAGE_NAME, oldEditHelpersPackageName, editHelpersPackageName));
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public String getEditPartsPackageNameGen() {
 		return editPartsPackageName;
 	}
 
 	public String getEditPartsPackageName() {
 		String value = getEditPartsPackageNameGen();
 		if (isEmpty(value)) {
 			value = getEditorGen().getPackageNamePrefix() + ".edit.parts";
 		}
 		return value;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public void setEditPartsPackageName(String newEditPartsPackageName) {
 		String oldEditPartsPackageName = editPartsPackageName;
 		editPartsPackageName = newEditPartsPackageName;
 		if (eNotificationRequired())
 			eNotify(new ENotificationImpl(this, Notification.SET, GMFGenPackage.GEN_DIAGRAM__EDIT_PARTS_PACKAGE_NAME, oldEditPartsPackageName, editPartsPackageName));
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public String getEditPoliciesPackageNameGen() {
 		return editPoliciesPackageName;
 	}
 
 	public String getEditPoliciesPackageName() {
 		String value = getEditPoliciesPackageNameGen();
 		if (isEmpty(value)) {
 			value = getEditorGen().getPackageNamePrefix() + ".edit.policies";
 		}
 		return value;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public void setEditPoliciesPackageName(String newEditPoliciesPackageName) {
 		String oldEditPoliciesPackageName = editPoliciesPackageName;
 		editPoliciesPackageName = newEditPoliciesPackageName;
 		if (eNotificationRequired())
 			eNotify(new ENotificationImpl(this, Notification.SET, GMFGenPackage.GEN_DIAGRAM__EDIT_POLICIES_PACKAGE_NAME, oldEditPoliciesPackageName, editPoliciesPackageName));
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public String getPreferencesPackageNameGen() {
 		return preferencesPackageName;
 	}
 
 	public String getPreferencesPackageName() {
 		String value = getPreferencesPackageNameGen();
 		if (isEmpty(value)) {
 			value = getEditorGen().getPackageNamePrefix() + ".preferences";
 		}
 		return value;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public void setPreferencesPackageName(String newPreferencesPackageName) {
 		String oldPreferencesPackageName = preferencesPackageName;
 		preferencesPackageName = newPreferencesPackageName;
 		if (eNotificationRequired())
 			eNotify(new ENotificationImpl(this, Notification.SET, GMFGenPackage.GEN_DIAGRAM__PREFERENCES_PACKAGE_NAME, oldPreferencesPackageName, preferencesPackageName));
 	}
 
 	public String getClassNamePrefix() {
 		// should be consistent with ClassNamingStrategy
 		if (getDomainDiagramElement() != null) {
 			String name = getDomainDiagramElement().getName();
 			if (!isEmpty(name)) {
 				return getValidClassName(name);
 			}
 		}
 		return CLASS_NAME_PREFIX;
 	}
 
 	public GenDiagram getDiagram() {
 		return this;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public String getVisualIDRegistryClassNameGen() {
 		return visualIDRegistryClassName;
 	}
 
 	public String getVisualIDRegistryClassName() {
 		String value = getVisualIDRegistryClassNameGen();
 		if (isEmpty(value)) {
 			value = getDomainPackageCapName() + "VisualIDRegistry"; //$NON-NLS-1$
 		}
 		return value;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public void setVisualIDRegistryClassName(String newVisualIDRegistryClassName) {
 		String oldVisualIDRegistryClassName = visualIDRegistryClassName;
 		visualIDRegistryClassName = newVisualIDRegistryClassName;
 		if (eNotificationRequired())
 			eNotify(new ENotificationImpl(this, Notification.SET, GMFGenPackage.GEN_DIAGRAM__VISUAL_ID_REGISTRY_CLASS_NAME, oldVisualIDRegistryClassName, visualIDRegistryClassName));
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public String getCreateShortcutActionClassNameGen() {
 		return createShortcutActionClassName;
 	}
 
 	public String getCreateShortcutActionClassName() {
 		String value = getCreateShortcutActionClassNameGen();
 		if (isEmpty(value)) {
 			value = getDomainPackageCapName() + "CreateShortcutAction"; //$NON-NLS-1$
 		}
 		return value;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public void setCreateShortcutActionClassName(String newCreateShortcutActionClassName) {
 		String oldCreateShortcutActionClassName = createShortcutActionClassName;
 		createShortcutActionClassName = newCreateShortcutActionClassName;
 		if (eNotificationRequired())
 			eNotify(new ENotificationImpl(this, Notification.SET, GMFGenPackage.GEN_DIAGRAM__CREATE_SHORTCUT_ACTION_CLASS_NAME, oldCreateShortcutActionClassName, createShortcutActionClassName));
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public String getCreateShortcutDecorationsCommandClassNameGen() {
 		return createShortcutDecorationsCommandClassName;
 	}
 
 	public String getCreateShortcutDecorationsCommandClassName() {
 		String value = getCreateShortcutDecorationsCommandClassNameGen();
 		if (isEmpty(value)) {
 			value = getDomainPackageCapName() + "CreateShortcutDecorationsCommand"; //$NON-NLS-1$
 		}
 		return value;
 	}
 	
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public void setCreateShortcutDecorationsCommandClassName(String newCreateShortcutDecorationsCommandClassName) {
 		String oldCreateShortcutDecorationsCommandClassName = createShortcutDecorationsCommandClassName;
 		createShortcutDecorationsCommandClassName = newCreateShortcutDecorationsCommandClassName;
 		if (eNotificationRequired())
 			eNotify(new ENotificationImpl(this, Notification.SET, GMFGenPackage.GEN_DIAGRAM__CREATE_SHORTCUT_DECORATIONS_COMMAND_CLASS_NAME, oldCreateShortcutDecorationsCommandClassName, createShortcutDecorationsCommandClassName));
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public String getShortcutPropertyTesterClassNameGen() {
 		return shortcutPropertyTesterClassName;
 	}
 
 	public String getShortcutPropertyTesterClassName() {
 		String value = getShortcutPropertyTesterClassNameGen();
 		if (isEmpty(value)) {
 			value = getDomainPackageCapName() + "ShortcutPropertyTester"; //$NON-NLS-1$
 		}
 		return value;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public void setShortcutPropertyTesterClassName(String newShortcutPropertyTesterClassName) {
 		String oldShortcutPropertyTesterClassName = shortcutPropertyTesterClassName;
 		shortcutPropertyTesterClassName = newShortcutPropertyTesterClassName;
 		if (eNotificationRequired())
 			eNotify(new ENotificationImpl(this, Notification.SET, GMFGenPackage.GEN_DIAGRAM__SHORTCUT_PROPERTY_TESTER_CLASS_NAME, oldShortcutPropertyTesterClassName, shortcutPropertyTesterClassName));
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public String getElementChooserClassNameGen() {
 		return elementChooserClassName;
 	}
 
 	public String getElementChooserClassName() {
 		String value = getElementChooserClassNameGen();
 		if (isEmpty(value)) {
 			value = getDomainPackageCapName() + "ElementChooserDialog"; //$NON-NLS-1$
 		}
 		return value;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public void setElementChooserClassName(String newElementChooserClassName) {
 		String oldElementChooserClassName = elementChooserClassName;
 		elementChooserClassName = newElementChooserClassName;
 		if (eNotificationRequired())
 			eNotify(new ENotificationImpl(this, Notification.SET, GMFGenPackage.GEN_DIAGRAM__ELEMENT_CHOOSER_CLASS_NAME, oldElementChooserClassName, elementChooserClassName));
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public String getLoadResourceActionClassNameGen() {
 		return loadResourceActionClassName;
 	}
 
 	public String getLoadResourceActionClassName() {
 		String value = getLoadResourceActionClassNameGen();
 		if (isEmpty(value)) {
 			value = getDomainPackageCapName() + "LoadResourceAction"; //$NON-NLS-1$
 		}
 		return value;
 	}
 	
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public void setLoadResourceActionClassName(String newLoadResourceActionClassName) {
 		String oldLoadResourceActionClassName = loadResourceActionClassName;
 		loadResourceActionClassName = newLoadResourceActionClassName;
 		if (eNotificationRequired())
 			eNotify(new ENotificationImpl(this, Notification.SET, GMFGenPackage.GEN_DIAGRAM__LOAD_RESOURCE_ACTION_CLASS_NAME, oldLoadResourceActionClassName, loadResourceActionClassName));
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public String getEditingDomainIDGen() {
 		return editingDomainID;
 	}
 
 	public String getEditingDomainID() {
 		String value = getEditingDomainIDGen();
 		if (isEmpty(value)) {
 			value = getEditorGen().getPlugin().getID() + ".EditingDomain"; //$NON-NLS-1$
 		}
 		return value;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public void setEditingDomainID(String newEditingDomainID) {
 		String oldEditingDomainID = editingDomainID;
 		editingDomainID = newEditingDomainID;
 		if (eNotificationRequired())
 			eNotify(new ENotificationImpl(this, Notification.SET, GMFGenPackage.GEN_DIAGRAM__EDITING_DOMAIN_ID, oldEditingDomainID, editingDomainID));
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public String getShortcutsDecoratorProviderClassNameGen() {
 		return shortcutsDecoratorProviderClassName;
 	}
 
 	public String getShortcutsDecoratorProviderClassName() {
 		String value = getShortcutsDecoratorProviderClassNameGen();
 		if(isEmpty(value)) {
 			value = getDomainPackageCapName() + "ShortcutsDecoratorProvider"; //$NON-NLS-1$						
 		}
 		return value;		
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public void setShortcutsDecoratorProviderClassName(String newShortcutsDecoratorProviderClassName) {
 		String oldShortcutsDecoratorProviderClassName = shortcutsDecoratorProviderClassName;
 		shortcutsDecoratorProviderClassName = newShortcutsDecoratorProviderClassName;
 		if (eNotificationRequired())
 			eNotify(new ENotificationImpl(this, Notification.SET, GMFGenPackage.GEN_DIAGRAM__SHORTCUTS_DECORATOR_PROVIDER_CLASS_NAME, oldShortcutsDecoratorProviderClassName, shortcutsDecoratorProviderClassName));
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public ProviderPriority getShortcutsDecoratorProviderPriority() {
 		return shortcutsDecoratorProviderPriority;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public void setShortcutsDecoratorProviderPriority(ProviderPriority newShortcutsDecoratorProviderPriority) {
 		ProviderPriority oldShortcutsDecoratorProviderPriority = shortcutsDecoratorProviderPriority;
 		shortcutsDecoratorProviderPriority = newShortcutsDecoratorProviderPriority == null ? SHORTCUTS_DECORATOR_PROVIDER_PRIORITY_EDEFAULT : newShortcutsDecoratorProviderPriority;
 		if (eNotificationRequired())
 			eNotify(new ENotificationImpl(this, Notification.SET, GMFGenPackage.GEN_DIAGRAM__SHORTCUTS_DECORATOR_PROVIDER_PRIORITY, oldShortcutsDecoratorProviderPriority, shortcutsDecoratorProviderPriority));
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public String getValidationProviderClassNameGen() {
 		return validationProviderClassName;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated NOT
 	 */
 	public String getValidationProviderClassName() {
 		String value = getValidationProviderClassNameGen();
 		if(isEmpty(value)) {
 			value = getDomainPackageCapName() + "ValidationProvider"; //$NON-NLS-1$						
 		}
 		return value;		
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public void setValidationProviderClassName(String newValidationProviderClassName) {
 		String oldValidationProviderClassName = validationProviderClassName;
 		validationProviderClassName = newValidationProviderClassName;
 		if (eNotificationRequired())
 			eNotify(new ENotificationImpl(this, Notification.SET, GMFGenPackage.GEN_DIAGRAM__VALIDATION_PROVIDER_CLASS_NAME, oldValidationProviderClassName, validationProviderClassName));
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public ProviderPriority getValidationProviderPriority() {
 		return validationProviderPriority;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public void setValidationProviderPriority(ProviderPriority newValidationProviderPriority) {
 		ProviderPriority oldValidationProviderPriority = validationProviderPriority;
 		validationProviderPriority = newValidationProviderPriority == null ? VALIDATION_PROVIDER_PRIORITY_EDEFAULT : newValidationProviderPriority;
 		if (eNotificationRequired())
 			eNotify(new ENotificationImpl(this, Notification.SET, GMFGenPackage.GEN_DIAGRAM__VALIDATION_PROVIDER_PRIORITY, oldValidationProviderPriority, validationProviderPriority));
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public String getMarkerNavigationProviderClassNameGen() {
 		return markerNavigationProviderClassName;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated NOT
 	 */
 	public String getMarkerNavigationProviderClassName() {
 		String value = getMarkerNavigationProviderClassNameGen();
 		if(isEmpty(value)) {
 			value = getDomainPackageCapName() + "MarkerNavigationProvider"; //$NON-NLS-1$						
 		}
 		return value;
 	}	
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated NOT
 	 */
 	public String getMarkerNavigationProviderQualifiedClassName() {
 		return getProvidersPackageName() + "." + getMarkerNavigationProviderClassName(); //$NON-NLS-1$
 	}	
 	
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated NOT
 	 */
 	public String getMetricProviderQualifiedClassName() {
 		return getProvidersPackageName() + "." + getMetricProviderClassName(); //$NON-NLS-1$		
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated NOT
 	 */
 	public String getMetricViewID() {
 		return getEditorGen().getPackageNamePrefix() + ".metricView"; //$NON-NLS-1$
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated NOT
 	 */
	public String getValidationDecoratorProviderQualifiedClassName() {
 		return getProvidersPackageName() + "." + getValidationDecoratorProviderClassName(); //$NON-NLS-1$
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EList<String> getContainsShortcutsTo() {
 		if (containsShortcutsTo == null) {
 			containsShortcutsTo = new EDataTypeUniqueEList<String>(String.class, this, GMFGenPackage.GEN_DIAGRAM__CONTAINS_SHORTCUTS_TO);
 		}
 		return containsShortcutsTo;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EList<String> getShortcutsProvidedFor() {
 		if (shortcutsProvidedFor == null) {
 			shortcutsProvidedFor = new EDataTypeUniqueEList<String>(String.class, this, GMFGenPackage.GEN_DIAGRAM__SHORTCUTS_PROVIDED_FOR);
 		}
 		return shortcutsProvidedFor;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public boolean isValidationEnabled() {
 		return validationEnabled;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public void setValidationEnabled(boolean newValidationEnabled) {
 		boolean oldValidationEnabled = validationEnabled;
 		validationEnabled = newValidationEnabled;
 		if (eNotificationRequired())
 			eNotify(new ENotificationImpl(this, Notification.SET, GMFGenPackage.GEN_DIAGRAM__VALIDATION_ENABLED, oldValidationEnabled, validationEnabled));
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public String getMetricProviderClassNameGen() {
 		return metricProviderClassName;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated NOT
 	 */
 	public String getMetricProviderClassName() {
 		String value = getMetricProviderClassNameGen();
 		if (isEmpty(value)) {
 			value = getDomainPackageCapName() + "MetricProvider"; //$NON-NLS-1$
 		}
 		return value;
 	}	
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public void setMetricProviderClassName(String newMetricProviderClassName) {
 		String oldMetricProviderClassName = metricProviderClassName;
 		metricProviderClassName = newMetricProviderClassName;
 		if (eNotificationRequired())
 			eNotify(new ENotificationImpl(this, Notification.SET, GMFGenPackage.GEN_DIAGRAM__METRIC_PROVIDER_CLASS_NAME, oldMetricProviderClassName, metricProviderClassName));
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public ProviderPriority getMetricProviderPriority() {
 		return metricProviderPriority;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public void setMetricProviderPriority(ProviderPriority newMetricProviderPriority) {
 		ProviderPriority oldMetricProviderPriority = metricProviderPriority;
 		metricProviderPriority = newMetricProviderPriority == null ? METRIC_PROVIDER_PRIORITY_EDEFAULT : newMetricProviderPriority;
 		if (eNotificationRequired())
 			eNotify(new ENotificationImpl(this, Notification.SET, GMFGenPackage.GEN_DIAGRAM__METRIC_PROVIDER_PRIORITY, oldMetricProviderPriority, metricProviderPriority));
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public String getValidationDecoratorProviderClassNameGen() {
 		return validationDecoratorProviderClassName;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated NOT
 	 */
 	public String getValidationDecoratorProviderClassName() {
 		String value = getValidationDecoratorProviderClassNameGen();
 		if (isEmpty(value)) {
 			value = getDomainPackageCapName() + "ValidationDecoratorProvider"; //$NON-NLS-1$
 		}
 		return value;
 	}
 	
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public void setValidationDecoratorProviderClassName(String newValidationDecoratorProviderClassName) {
 		String oldValidationDecoratorProviderClassName = validationDecoratorProviderClassName;
 		validationDecoratorProviderClassName = newValidationDecoratorProviderClassName;
 		if (eNotificationRequired())
 			eNotify(new ENotificationImpl(this, Notification.SET, GMFGenPackage.GEN_DIAGRAM__VALIDATION_DECORATOR_PROVIDER_CLASS_NAME, oldValidationDecoratorProviderClassName, validationDecoratorProviderClassName));
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public boolean isValidationDecorators() {
 		return validationDecorators;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public void setValidationDecorators(boolean newValidationDecorators) {
 		boolean oldValidationDecorators = validationDecorators;
 		validationDecorators = newValidationDecorators;
 		if (eNotificationRequired())
 			eNotify(new ENotificationImpl(this, Notification.SET, GMFGenPackage.GEN_DIAGRAM__VALIDATION_DECORATORS, oldValidationDecorators, validationDecorators));
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public ProviderPriority getValidationDecoratorProviderPriority() {
 		return validationDecoratorProviderPriority;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public void setValidationDecoratorProviderPriority(ProviderPriority newValidationDecoratorProviderPriority) {
 		ProviderPriority oldValidationDecoratorProviderPriority = validationDecoratorProviderPriority;
 		validationDecoratorProviderPriority = newValidationDecoratorProviderPriority == null ? VALIDATION_DECORATOR_PROVIDER_PRIORITY_EDEFAULT : newValidationDecoratorProviderPriority;
 		if (eNotificationRequired())
 			eNotify(new ENotificationImpl(this, Notification.SET, GMFGenPackage.GEN_DIAGRAM__VALIDATION_DECORATOR_PROVIDER_PRIORITY, oldValidationDecoratorProviderPriority, validationDecoratorProviderPriority));
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public boolean isLiveValidationUIFeedback() {
 		return liveValidationUIFeedback;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public void setLiveValidationUIFeedback(boolean newLiveValidationUIFeedback) {
 		boolean oldLiveValidationUIFeedback = liveValidationUIFeedback;
 		liveValidationUIFeedback = newLiveValidationUIFeedback;
 		if (eNotificationRequired())
 			eNotify(new ENotificationImpl(this, Notification.SET, GMFGenPackage.GEN_DIAGRAM__LIVE_VALIDATION_UI_FEEDBACK, oldLiveValidationUIFeedback, liveValidationUIFeedback));
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public String getUnits() {
 		return units;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public void setUnits(String newUnits) {
 		String oldUnits = units;
 		units = newUnits;
 		if (eNotificationRequired())
 			eNotify(new ENotificationImpl(this, Notification.SET, GMFGenPackage.GEN_DIAGRAM__UNITS, oldUnits, units));
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public GenEditorGenerator getEditorGen() {
 		if (eContainerFeatureID != GMFGenPackage.GEN_DIAGRAM__EDITOR_GEN) return null;
 		return (GenEditorGenerator)eContainer();
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public String getProvidersPackageNameGen() {
 		return providersPackageName;
 	}
 
 	public String getProvidersPackageName() {
 		String value = getProvidersPackageNameGen();
 		if (isEmpty(value)) {
 			value = getEditorGen().getPackageNamePrefix() + ".providers";
 		}
 		return value;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public void setProvidersPackageName(String newProvidersPackageName) {
 		String oldProvidersPackageName = providersPackageName;
 		providersPackageName = newProvidersPackageName;
 		if (eNotificationRequired())
 			eNotify(new ENotificationImpl(this, Notification.SET, GMFGenPackage.GEN_DIAGRAM__PROVIDERS_PACKAGE_NAME, oldProvidersPackageName, providersPackageName));
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public String getParsersPackageNameGen() {
 		return parsersPackageName;
 	}
 
 	public String getParsersPackageName() {
 		String value = getParsersPackageNameGen();
 		if (isEmpty(value)) {
 			value = getEditorGen().getPackageNamePrefix() + ".parsers";
 		}
 		return value;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public void setParsersPackageName(String newParsersPackageName) {
 		String oldParsersPackageName = parsersPackageName;
 		parsersPackageName = newParsersPackageName;
 		if (eNotificationRequired())
 			eNotify(new ENotificationImpl(this, Notification.SET, GMFGenPackage.GEN_DIAGRAM__PARSERS_PACKAGE_NAME, oldParsersPackageName, parsersPackageName));
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public String getNotationViewFactoriesPackageNameGen() {
 		return notationViewFactoriesPackageName;
 	}
 
 	public String getNotationViewFactoriesPackageName() {
 		String value = getNotationViewFactoriesPackageNameGen();
 		if (isEmpty(value)) {
 			value = getEditorGen().getPackageNamePrefix() + ".view.factories";
 		}
 		return value;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public void setNotationViewFactoriesPackageName(String newNotationViewFactoriesPackageName) {
 		String oldNotationViewFactoriesPackageName = notationViewFactoriesPackageName;
 		notationViewFactoriesPackageName = newNotationViewFactoriesPackageName;
 		if (eNotificationRequired())
 			eNotify(new ENotificationImpl(this, Notification.SET, GMFGenPackage.GEN_DIAGRAM__NOTATION_VIEW_FACTORIES_PACKAGE_NAME, oldNotationViewFactoriesPackageName, notationViewFactoriesPackageName));
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public String getDocumentProviderClassNameGen() {
 		return documentProviderClassName;
 	}
 
 	public String getDocumentProviderClassName() {
 		String value = getDocumentProviderClassNameGen();
 		if (isEmpty(value)) {
 			value = getDomainPackageCapName() + "DocumentProvider"; //$NON-NLS-1$
 		}
 		return value;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public void setDocumentProviderClassName(String newDocumentProviderClassName) {
 		String oldDocumentProviderClassName = documentProviderClassName;
 		documentProviderClassName = newDocumentProviderClassName;
 		if (eNotificationRequired())
 			eNotify(new ENotificationImpl(this, Notification.SET, GMFGenPackage.GEN_DIAGRAM__DOCUMENT_PROVIDER_CLASS_NAME, oldDocumentProviderClassName, documentProviderClassName));
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public String getEditPartFactoryClassNameGen() {
 		return editPartFactoryClassName;
 	}
 
 	public String getEditPartFactoryClassName() {
 		String value = getEditPartFactoryClassNameGen();
 		if (isEmpty(value)) {
 			value = getDomainPackageCapName() + "EditPartFactory"; //$NON-NLS-1$
 		}
 		return value;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public void setEditPartFactoryClassName(String newEditPartFactoryClassName) {
 		String oldEditPartFactoryClassName = editPartFactoryClassName;
 		editPartFactoryClassName = newEditPartFactoryClassName;
 		if (eNotificationRequired())
 			eNotify(new ENotificationImpl(this, Notification.SET, GMFGenPackage.GEN_DIAGRAM__EDIT_PART_FACTORY_CLASS_NAME, oldEditPartFactoryClassName, editPartFactoryClassName));
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public String getBaseExternalNodeLabelEditPartClassNameGen() {
 		return baseExternalNodeLabelEditPartClassName;
 	}
 
 	public String getBaseExternalNodeLabelEditPartClassName() {
 		String value = getBaseExternalNodeLabelEditPartClassNameGen();
 		if (isEmpty(value)) {
 			value = getDomainPackageCapName() + "ExtNodeLabelEditPart"; //$NON-NLS-1$
 		}
 		return value;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public void setBaseExternalNodeLabelEditPartClassName(String newBaseExternalNodeLabelEditPartClassName) {
 		String oldBaseExternalNodeLabelEditPartClassName = baseExternalNodeLabelEditPartClassName;
 		baseExternalNodeLabelEditPartClassName = newBaseExternalNodeLabelEditPartClassName;
 		if (eNotificationRequired())
 			eNotify(new ENotificationImpl(this, Notification.SET, GMFGenPackage.GEN_DIAGRAM__BASE_EXTERNAL_NODE_LABEL_EDIT_PART_CLASS_NAME, oldBaseExternalNodeLabelEditPartClassName, baseExternalNodeLabelEditPartClassName));
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public String getBaseItemSemanticEditPolicyClassNameGen() {
 		return baseItemSemanticEditPolicyClassName;
 	}
 
 	public String getBaseItemSemanticEditPolicyClassName() {
 		String value = getBaseItemSemanticEditPolicyClassNameGen();
 		if (isEmpty(value)) {
 			value = getDomainPackageCapName() + "BaseItemSemanticEditPolicy"; //$NON-NLS-1$
 		}
 		return value;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public void setBaseItemSemanticEditPolicyClassName(String newBaseItemSemanticEditPolicyClassName) {
 		String oldBaseItemSemanticEditPolicyClassName = baseItemSemanticEditPolicyClassName;
 		baseItemSemanticEditPolicyClassName = newBaseItemSemanticEditPolicyClassName;
 		if (eNotificationRequired())
 			eNotify(new ENotificationImpl(this, Notification.SET, GMFGenPackage.GEN_DIAGRAM__BASE_ITEM_SEMANTIC_EDIT_POLICY_CLASS_NAME, oldBaseItemSemanticEditPolicyClassName, baseItemSemanticEditPolicyClassName));
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public String getBaseGraphicalNodeEditPolicyClassNameGen() {
 		return baseGraphicalNodeEditPolicyClassName;
 	}
 
 	public String getBaseGraphicalNodeEditPolicyClassName() {
 		String value = getBaseGraphicalNodeEditPolicyClassNameGen();
 		if (isEmpty(value)) {
 			value = getDomainPackageCapName() + "GraphicalNodeEditPolicy"; //$NON-NLS-1$
 		}
 		return value;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public void setBaseGraphicalNodeEditPolicyClassName(String newBaseGraphicalNodeEditPolicyClassName) {
 		String oldBaseGraphicalNodeEditPolicyClassName = baseGraphicalNodeEditPolicyClassName;
 		baseGraphicalNodeEditPolicyClassName = newBaseGraphicalNodeEditPolicyClassName;
 		if (eNotificationRequired())
 			eNotify(new ENotificationImpl(this, Notification.SET, GMFGenPackage.GEN_DIAGRAM__BASE_GRAPHICAL_NODE_EDIT_POLICY_CLASS_NAME, oldBaseGraphicalNodeEditPolicyClassName, baseGraphicalNodeEditPolicyClassName));
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public String getCanonicalEditPolicyClassNameGen() {
 		return canonicalEditPolicyClassName;
 	}
 
 	public String getCanonicalEditPolicyClassName() {
 		return getValidClassName(getCanonicalEditPolicyClassNameGen(), this, CANONICAL_EDIT_POLICY_SUFFIX);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public void setCanonicalEditPolicyClassName(String newCanonicalEditPolicyClassName) {
 		String oldCanonicalEditPolicyClassName = canonicalEditPolicyClassName;
 		canonicalEditPolicyClassName = newCanonicalEditPolicyClassName;
 		if (eNotificationRequired())
 			eNotify(new ENotificationImpl(this, Notification.SET, GMFGenPackage.GEN_DIAGRAM__CANONICAL_EDIT_POLICY_CLASS_NAME, oldCanonicalEditPolicyClassName, canonicalEditPolicyClassName));
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public String getTextSelectionEditPolicyClassNameGen() {
 		return textSelectionEditPolicyClassName;
 	}
 
 	public String getTextSelectionEditPolicyClassName() {
 		String value = getTextSelectionEditPolicyClassNameGen();
 		if (isEmpty(value)) {
 			value = getDomainPackageCapName() + "TextSelectionEditPolicy"; //$NON-NLS-1$
 		}
 		return value;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public void setTextSelectionEditPolicyClassName(String newTextSelectionEditPolicyClassName) {
 		String oldTextSelectionEditPolicyClassName = textSelectionEditPolicyClassName;
 		textSelectionEditPolicyClassName = newTextSelectionEditPolicyClassName;
 		if (eNotificationRequired())
 			eNotify(new ENotificationImpl(this, Notification.SET, GMFGenPackage.GEN_DIAGRAM__TEXT_SELECTION_EDIT_POLICY_CLASS_NAME, oldTextSelectionEditPolicyClassName, textSelectionEditPolicyClassName));
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public String getTextNonResizableEditPolicyClassNameGen() {
 		return textNonResizableEditPolicyClassName;
 	}
 
 	public String getTextNonResizableEditPolicyClassName() {
 		String value = getTextNonResizableEditPolicyClassNameGen();
 		if (isEmpty(value)) {
 			value = getDomainPackageCapName() + "TextNonResizableEditPolicy"; //$NON-NLS-1$
 		}
 		return value;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public void setTextNonResizableEditPolicyClassName(String newTextNonResizableEditPolicyClassName) {
 		String oldTextNonResizableEditPolicyClassName = textNonResizableEditPolicyClassName;
 		textNonResizableEditPolicyClassName = newTextNonResizableEditPolicyClassName;
 		if (eNotificationRequired())
 			eNotify(new ENotificationImpl(this, Notification.SET, GMFGenPackage.GEN_DIAGRAM__TEXT_NON_RESIZABLE_EDIT_POLICY_CLASS_NAME, oldTextNonResizableEditPolicyClassName, textNonResizableEditPolicyClassName));
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public String getElementTypesClassNameGen() {
 		return elementTypesClassName;
 	}
 
 	public String getElementTypesClassName() {
 		String value = getElementTypesClassNameGen();
 		if (isEmpty(value)) {
 			value = getDomainPackageCapName() + "ElementTypes"; //$NON-NLS-1$
 		}
 		return value;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public void setElementTypesClassName(String newElementTypesClassName) {
 		String oldElementTypesClassName = elementTypesClassName;
 		elementTypesClassName = newElementTypesClassName;
 		if (eNotificationRequired())
 			eNotify(new ENotificationImpl(this, Notification.SET, GMFGenPackage.GEN_DIAGRAM__ELEMENT_TYPES_CLASS_NAME, oldElementTypesClassName, elementTypesClassName));
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public String getNotationViewProviderClassNameGen() {
 		return notationViewProviderClassName;
 	}
 
 	public String getNotationViewProviderClassName() {
 		String value = getNotationViewProviderClassNameGen();
 		if (isEmpty(value)) {
 			value = getDomainPackageCapName() + "ViewProvider"; //$NON-NLS-1$
 		}
 		return value;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public void setNotationViewProviderClassName(String newNotationViewProviderClassName) {
 		String oldNotationViewProviderClassName = notationViewProviderClassName;
 		notationViewProviderClassName = newNotationViewProviderClassName;
 		if (eNotificationRequired())
 			eNotify(new ENotificationImpl(this, Notification.SET, GMFGenPackage.GEN_DIAGRAM__NOTATION_VIEW_PROVIDER_CLASS_NAME, oldNotationViewProviderClassName, notationViewProviderClassName));
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public ProviderPriority getNotationViewProviderPriority() {
 		return notationViewProviderPriority;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public void setNotationViewProviderPriority(ProviderPriority newNotationViewProviderPriority) {
 		ProviderPriority oldNotationViewProviderPriority = notationViewProviderPriority;
 		notationViewProviderPriority = newNotationViewProviderPriority == null ? NOTATION_VIEW_PROVIDER_PRIORITY_EDEFAULT : newNotationViewProviderPriority;
 		if (eNotificationRequired())
 			eNotify(new ENotificationImpl(this, Notification.SET, GMFGenPackage.GEN_DIAGRAM__NOTATION_VIEW_PROVIDER_PRIORITY, oldNotationViewProviderPriority, notationViewProviderPriority));
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public String getReorientConnectionViewCommandClassNameGen() {
 		return reorientConnectionViewCommandClassName;
 	}
 
 	public String getReorientConnectionViewCommandClassName() {
 		String value = getReorientConnectionViewCommandClassNameGen();
 		if (isEmpty(value)) {
 			value = getDomainPackageCapName() + "ReorientConnectionViewCommand"; //$NON-NLS-1$
 		}
 		return value;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public void setReorientConnectionViewCommandClassName(String newReorientConnectionViewCommandClassName) {
 		String oldReorientConnectionViewCommandClassName = reorientConnectionViewCommandClassName;
 		reorientConnectionViewCommandClassName = newReorientConnectionViewCommandClassName;
 		if (eNotificationRequired())
 			eNotify(new ENotificationImpl(this, Notification.SET, GMFGenPackage.GEN_DIAGRAM__REORIENT_CONNECTION_VIEW_COMMAND_CLASS_NAME, oldReorientConnectionViewCommandClassName, reorientConnectionViewCommandClassName));
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public String getBaseEditHelperClassNameGen() {
 		return baseEditHelperClassName;
 	}
 
 	public String getBaseEditHelperClassName() {
 		String value = getBaseEditHelperClassNameGen();
 		if (isEmpty(value)) {
 			value = getDomainPackageCapName() + "BaseEditHelper"; //$NON-NLS-1$
 		}
 		return value;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public void setBaseEditHelperClassName(String newBaseEditHelperClassName) {
 		String oldBaseEditHelperClassName = baseEditHelperClassName;
 		baseEditHelperClassName = newBaseEditHelperClassName;
 		if (eNotificationRequired())
 			eNotify(new ENotificationImpl(this, Notification.SET, GMFGenPackage.GEN_DIAGRAM__BASE_EDIT_HELPER_CLASS_NAME, oldBaseEditHelperClassName, baseEditHelperClassName));
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public String getInitDiagramFileActionClassNameGen() {
 		return initDiagramFileActionClassName;
 	}
 
 	public String getInitDiagramFileActionClassName() {
 		String value = getInitDiagramFileActionClassNameGen();
 		if (isEmpty(value)) {
 			value = getDomainPackageCapName() + "InitDiagramFileAction"; //$NON-NLS-1$
 		}
 		return value;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public void setInitDiagramFileActionClassName(String newInitDiagramFileActionClassName) {
 		String oldInitDiagramFileActionClassName = initDiagramFileActionClassName;
 		initDiagramFileActionClassName = newInitDiagramFileActionClassName;
 		if (eNotificationRequired())
 			eNotify(new ENotificationImpl(this, Notification.SET, GMFGenPackage.GEN_DIAGRAM__INIT_DIAGRAM_FILE_ACTION_CLASS_NAME, oldInitDiagramFileActionClassName, initDiagramFileActionClassName));
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public String getNewDiagramFileWizardClassNameGen() {
 		return newDiagramFileWizardClassName;
 	}
 
 	public String getNewDiagramFileWizardClassName() {
 		String value = getNewDiagramFileWizardClassNameGen();
 		if (isEmpty(value)) {
 			value = getDomainPackageCapName() + "NewDiagramFileWizard"; //$NON-NLS-1$
 		}
 		return value;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public void setNewDiagramFileWizardClassName(String newNewDiagramFileWizardClassName) {
 		String oldNewDiagramFileWizardClassName = newDiagramFileWizardClassName;
 		newDiagramFileWizardClassName = newNewDiagramFileWizardClassName;
 		if (eNotificationRequired())
 			eNotify(new ENotificationImpl(this, Notification.SET, GMFGenPackage.GEN_DIAGRAM__NEW_DIAGRAM_FILE_WIZARD_CLASS_NAME, oldNewDiagramFileWizardClassName, newDiagramFileWizardClassName));
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public String getDiagramContentInitializerClassNameGen() {
 		return diagramContentInitializerClassName;
 	}
 
 	public String getDiagramContentInitializerClassName() {
 		String value = getDiagramContentInitializerClassNameGen();
 		if (isEmpty(value)) {
 			value = getDomainPackageCapName() + "DiagramContentInitializer"; //$NON-NLS-1$
 		}
 		return value;
 	}
 	
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public void setDiagramContentInitializerClassName(String newDiagramContentInitializerClassName) {
 		String oldDiagramContentInitializerClassName = diagramContentInitializerClassName;
 		diagramContentInitializerClassName = newDiagramContentInitializerClassName;
 		if (eNotificationRequired())
 			eNotify(new ENotificationImpl(this, Notification.SET, GMFGenPackage.GEN_DIAGRAM__DIAGRAM_CONTENT_INITIALIZER_CLASS_NAME, oldDiagramContentInitializerClassName, diagramContentInitializerClassName));
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public String getCreationWizardClassNameGen() {
 		return creationWizardClassName;
 	}
 
 	public String getCreationWizardClassName() {
 		String value = getCreationWizardClassNameGen();
 		if (isEmpty(value)) {
 			value = getDomainPackageCapName() + "CreationWizard"; //$NON-NLS-1$
 		}
 		return value;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public void setCreationWizardClassName(String newCreationWizardClassName) {
 		String oldCreationWizardClassName = creationWizardClassName;
 		creationWizardClassName = newCreationWizardClassName;
 		if (eNotificationRequired())
 			eNotify(new ENotificationImpl(this, Notification.SET, GMFGenPackage.GEN_DIAGRAM__CREATION_WIZARD_CLASS_NAME, oldCreationWizardClassName, creationWizardClassName));
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public String getCreationWizardPageClassNameGen() {
 		return creationWizardPageClassName;
 	}
 
 	public String getCreationWizardPageClassName() {
 		String value = getCreationWizardPageClassNameGen();
 		if (isEmpty(value)) {
 			value = getDomainPackageCapName() + "CreationWizardPage"; //$NON-NLS-1$
 		}
 		return value;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public void setCreationWizardPageClassName(String newCreationWizardPageClassName) {
 		String oldCreationWizardPageClassName = creationWizardPageClassName;
 		creationWizardPageClassName = newCreationWizardPageClassName;
 		if (eNotificationRequired())
 			eNotify(new ENotificationImpl(this, Notification.SET, GMFGenPackage.GEN_DIAGRAM__CREATION_WIZARD_PAGE_CLASS_NAME, oldCreationWizardPageClassName, creationWizardPageClassName));
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public String getCreationWizardIconPathGen() {
 		return creationWizardIconPath;
 	}
 
 	public String getCreationWizardIconPath() {
 		String value = getCreationWizardIconPathGen();
 		if (isEmpty(value)) {
 			return createDefaultIconPath();
 		}
 		return value;
 	}
 
 	static final String REUSE_ICON_VALUE = "{reuseEMFIcon}";
 
 	public String getCreationWizardIconPathX() {
 		String value = getCreationWizardIconPath();
 		if (!REUSE_ICON_VALUE.equalsIgnoreCase(value)) {
 			return value;
 		}
 		if (getDomainDiagramElement() != null) {
 			GenPackage domainMetaModel = getDomainDiagramElement().getGenPackage();
 			return "../" + getEditorGen().getDomainGenModel().getEditorPluginID() + "/icons/full/obj16/" + domainMetaModel.getPrefix() + "ModelFile.gif";
 		} else {
 			// no much sense to return value of REUSE_ICON_VALUE constant
 			return createDefaultIconPath();
 		}
 	}
 
 	private String createDefaultIconPath() {
 		final String stem = getDomainDiagramElement() == null ? "" : getDomainDiagramElement().getGenPackage().getPrefix();
 		return "icons/obj16/" + stem +"DiagramFile.gif";
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public void setCreationWizardIconPath(String newCreationWizardIconPath) {
 		String oldCreationWizardIconPath = creationWizardIconPath;
 		creationWizardIconPath = newCreationWizardIconPath;
 		if (eNotificationRequired())
 			eNotify(new ENotificationImpl(this, Notification.SET, GMFGenPackage.GEN_DIAGRAM__CREATION_WIZARD_ICON_PATH, oldCreationWizardIconPath, creationWizardIconPath));
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public String getCreationWizardCategoryIDGen() {
 		return creationWizardCategoryID;
 	}
 
 	public String getCreationWizardCategoryID() {
 		String value = getCreationWizardCategoryIDGen();
 		if (isEmpty(value)) {
 			value = "org.eclipse.ui.Examples"; //$NON-NLS-1$
 		}
 		return value;
 	}
 
 	
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public void setCreationWizardCategoryID(String newCreationWizardCategoryID) {
 		String oldCreationWizardCategoryID = creationWizardCategoryID;
 		creationWizardCategoryID = newCreationWizardCategoryID;
 		if (eNotificationRequired())
 			eNotify(new ENotificationImpl(this, Notification.SET, GMFGenPackage.GEN_DIAGRAM__CREATION_WIZARD_CATEGORY_ID, oldCreationWizardCategoryID, creationWizardCategoryID));
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public String getDiagramEditorUtilClassNameGen() {
 		return diagramEditorUtilClassName;
 	}
 
 	public String getDiagramEditorUtilClassName() {
 		String value = getDiagramEditorUtilClassNameGen();
 		if (isEmpty(value)) {
 			value = getDomainPackageCapName() + "DiagramEditorUtil"; //$NON-NLS-1$
 		}
 		return value;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public void setDiagramEditorUtilClassName(String newDiagramEditorUtilClassName) {
 		String oldDiagramEditorUtilClassName = diagramEditorUtilClassName;
 		diagramEditorUtilClassName = newDiagramEditorUtilClassName;
 		if (eNotificationRequired())
 			eNotify(new ENotificationImpl(this, Notification.SET, GMFGenPackage.GEN_DIAGRAM__DIAGRAM_EDITOR_UTIL_CLASS_NAME, oldDiagramEditorUtilClassName, diagramEditorUtilClassName));
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public String getMatchingStrategyClassNameGen() {
 		return matchingStrategyClassName;
 	}
 
 	public String getMatchingStrategyClassName() {
 		String value = getMatchingStrategyClassNameGen();
 		if (isEmpty(value)) {
 			value = getDomainPackageCapName() + "MatchingStrategy"; //$NON-NLS-1$
 		}
 		return value;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public void setMatchingStrategyClassName(String newMatchingStrategyClassName) {
 		String oldMatchingStrategyClassName = matchingStrategyClassName;
 		matchingStrategyClassName = newMatchingStrategyClassName;
 		if (eNotificationRequired())
 			eNotify(new ENotificationImpl(this, Notification.SET, GMFGenPackage.GEN_DIAGRAM__MATCHING_STRATEGY_CLASS_NAME, oldMatchingStrategyClassName, matchingStrategyClassName));
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public String getEditPartProviderClassNameGen() {
 		return editPartProviderClassName;
 	}
 
 	public String getEditPartProviderClassName() {
 		String value = getEditPartProviderClassNameGen();
 		if (isEmpty(value)) {
 			value = getDomainPackageCapName() + "EditPartProvider"; //$NON-NLS-1$
 		}
 		return value;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public void setEditPartProviderClassName(String newEditPartProviderClassName) {
 		String oldEditPartProviderClassName = editPartProviderClassName;
 		editPartProviderClassName = newEditPartProviderClassName;
 		if (eNotificationRequired())
 			eNotify(new ENotificationImpl(this, Notification.SET, GMFGenPackage.GEN_DIAGRAM__EDIT_PART_PROVIDER_CLASS_NAME, oldEditPartProviderClassName, editPartProviderClassName));
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public ProviderPriority getEditPartProviderPriority() {
 		return editPartProviderPriority;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public void setEditPartProviderPriority(ProviderPriority newEditPartProviderPriority) {
 		ProviderPriority oldEditPartProviderPriority = editPartProviderPriority;
 		editPartProviderPriority = newEditPartProviderPriority == null ? EDIT_PART_PROVIDER_PRIORITY_EDEFAULT : newEditPartProviderPriority;
 		if (eNotificationRequired())
 			eNotify(new ENotificationImpl(this, Notification.SET, GMFGenPackage.GEN_DIAGRAM__EDIT_PART_PROVIDER_PRIORITY, oldEditPartProviderPriority, editPartProviderPriority));
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public String getModelingAssistantProviderClassNameGen() {
 		return modelingAssistantProviderClassName;
 	}
 
 	public String getModelingAssistantProviderClassName() {
 		String value = getModelingAssistantProviderClassNameGen();
 		if (isEmpty(value)) {
 			value = getDomainPackageCapName() + "ModelingAssistantProvider"; //$NON-NLS-1$
 		}
 		return value;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public void setModelingAssistantProviderClassName(String newModelingAssistantProviderClassName) {
 		String oldModelingAssistantProviderClassName = modelingAssistantProviderClassName;
 		modelingAssistantProviderClassName = newModelingAssistantProviderClassName;
 		if (eNotificationRequired())
 			eNotify(new ENotificationImpl(this, Notification.SET, GMFGenPackage.GEN_DIAGRAM__MODELING_ASSISTANT_PROVIDER_CLASS_NAME, oldModelingAssistantProviderClassName, modelingAssistantProviderClassName));
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public ProviderPriority getModelingAssistantProviderPriority() {
 		return modelingAssistantProviderPriority;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public void setModelingAssistantProviderPriority(ProviderPriority newModelingAssistantProviderPriority) {
 		ProviderPriority oldModelingAssistantProviderPriority = modelingAssistantProviderPriority;
 		modelingAssistantProviderPriority = newModelingAssistantProviderPriority == null ? MODELING_ASSISTANT_PROVIDER_PRIORITY_EDEFAULT : newModelingAssistantProviderPriority;
 		if (eNotificationRequired())
 			eNotify(new ENotificationImpl(this, Notification.SET, GMFGenPackage.GEN_DIAGRAM__MODELING_ASSISTANT_PROVIDER_PRIORITY, oldModelingAssistantProviderPriority, modelingAssistantProviderPriority));
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public String getIconProviderClassNameGen() {
 		return iconProviderClassName;
 	}
 
 	public String getIconProviderClassName() {
 		String value = getIconProviderClassNameGen();
 		if (isEmpty(value)) {
 			value = getDomainPackageCapName() + "IconProvider"; //$NON-NLS-1$
 		}
 		return value;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public void setIconProviderClassName(String newIconProviderClassName) {
 		String oldIconProviderClassName = iconProviderClassName;
 		iconProviderClassName = newIconProviderClassName;
 		if (eNotificationRequired())
 			eNotify(new ENotificationImpl(this, Notification.SET, GMFGenPackage.GEN_DIAGRAM__ICON_PROVIDER_CLASS_NAME, oldIconProviderClassName, iconProviderClassName));
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public ProviderPriority getIconProviderPriority() {
 		return iconProviderPriority;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public void setIconProviderPriority(ProviderPriority newIconProviderPriority) {
 		ProviderPriority oldIconProviderPriority = iconProviderPriority;
 		iconProviderPriority = newIconProviderPriority == null ? ICON_PROVIDER_PRIORITY_EDEFAULT : newIconProviderPriority;
 		if (eNotificationRequired())
 			eNotify(new ENotificationImpl(this, Notification.SET, GMFGenPackage.GEN_DIAGRAM__ICON_PROVIDER_PRIORITY, oldIconProviderPriority, iconProviderPriority));
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public String getParserProviderClassNameGen() {
 		return parserProviderClassName;
 	}
 
 	public String getParserProviderClassName() {
 		String value = getParserProviderClassNameGen();
 		if (isEmpty(value)) {
 			value = getDomainPackageCapName() + "ParserProvider"; //$NON-NLS-1$
 		}
 		return value;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public void setParserProviderClassName(String newParserProviderClassName) {
 		String oldParserProviderClassName = parserProviderClassName;
 		parserProviderClassName = newParserProviderClassName;
 		if (eNotificationRequired())
 			eNotify(new ENotificationImpl(this, Notification.SET, GMFGenPackage.GEN_DIAGRAM__PARSER_PROVIDER_CLASS_NAME, oldParserProviderClassName, parserProviderClassName));
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public ProviderPriority getParserProviderPriority() {
 		return parserProviderPriority;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public void setParserProviderPriority(ProviderPriority newParserProviderPriority) {
 		ProviderPriority oldParserProviderPriority = parserProviderPriority;
 		parserProviderPriority = newParserProviderPriority == null ? PARSER_PROVIDER_PRIORITY_EDEFAULT : newParserProviderPriority;
 		if (eNotificationRequired())
 			eNotify(new ENotificationImpl(this, Notification.SET, GMFGenPackage.GEN_DIAGRAM__PARSER_PROVIDER_PRIORITY, oldParserProviderPriority, parserProviderPriority));
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public String getContributionItemProviderClassNameGen() {
 		return contributionItemProviderClassName;
 	}
 
 	public String getContributionItemProviderClassName() {
 		String value = getContributionItemProviderClassNameGen();
 		if (isEmpty(value)) {
 			value = getDomainPackageCapName() + "ContributionItemProvider"; //$NON-NLS-1$
 		}
 		return value;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public void setContributionItemProviderClassName(String newContributionItemProviderClassName) {
 		String oldContributionItemProviderClassName = contributionItemProviderClassName;
 		contributionItemProviderClassName = newContributionItemProviderClassName;
 		if (eNotificationRequired())
 			eNotify(new ENotificationImpl(this, Notification.SET, GMFGenPackage.GEN_DIAGRAM__CONTRIBUTION_ITEM_PROVIDER_CLASS_NAME, oldContributionItemProviderClassName, contributionItemProviderClassName));
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated NOT
 	 */
 	public String getContributionItemProviderQualifiedClassName() {
 		return getProvidersPackageName() + '.' + getContributionItemProviderClassName();
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated NOT
 	 */
 	public boolean needsCanonicalEditPolicy() {
 		return !isSansDomain() && isSynchronized() && !getContainedNodes().isEmpty();
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated NOT
 	 */
 	public EList<GenNode> getAllNodes() {
 		BasicEList<GenNode> result = new BasicEList<GenNode>();
 		result.addAll(getTopLevelNodes());
 		result.addAll(getChildNodes());
 		return new BasicEList.UnmodifiableEList<GenNode>(result.size(), result.toArray());
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated NOT
 	 */
 	public EList<GenChildContainer> getAllChildContainers() {
 		BasicEList<GenChildContainer> result = new BasicEList<GenChildContainer>();
 		result.addAll(getAllNodes());
 		result.addAll(getCompartments());
 		return new BasicEList.UnmodifiableEList<GenChildContainer>(result.size(), result.toArray());
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated NOT
 	 */
 	public EList<GenContainerBase> getAllContainers() {
 		BasicEList<GenContainerBase> result = new BasicEList<GenContainerBase>();
 		result.addAll(getAllChildContainers());
 		result.add(this);
 		return new BasicEList.UnmodifiableEList<GenContainerBase>(result.size(), result.toArray());
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated NOT
 	 */
 	public String getVisualIDRegistryQualifiedClassName() {
 		return getEditorPackageName() + '.' + getVisualIDRegistryClassName();
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated NOT
 	 */
 	public String getCreateShortcutActionQualifiedClassName() {
 		return getEditorPackageName() + '.' + getCreateShortcutActionClassName();
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated NOT
 	 */
 	public String getCreateShortcutDecorationsCommandQualifiedClassName() {
 		return getEditCommandsPackageName() + '.' + getCreateShortcutDecorationsCommandClassName();
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated NOT
 	 */
 	public String getShortcutPropertyTesterQualifiedClassName() {
 		return getEditorPackageName() + '.' + getShortcutPropertyTesterClassName();
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated NOT
 	 */
 	public String getElementChooserQualifiedClassName() {
 		return getEditorPackageName() + '.' + getElementChooserClassName();
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated NOT
 	 */
 	public String getLoadResourceActionQualifiedClassName() {
 		return getEditorPackageName() + '.' + getLoadResourceActionClassName();
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated NOT
 	 */
 	public boolean generateInitDiagramAction() {
 		return getDomainDiagramElement() != null;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated NOT
 	 */
 	public String getShortcutsDecoratorProviderQualifiedClassName() {
 		return getProvidersPackageName() + '.' + getShortcutsDecoratorProviderClassName();
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated NOT
 	 */
 	public String getValidationProviderQualifiedClassName() {
 		return getProvidersPackageName() + '.' + getValidationProviderClassName();
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated NOT
 	 */
 	public String getValidationDiagnosticMarkerType() {
 		return "diagnostic"; //$NON-NLS-1$
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public void setMarkerNavigationProviderClassName(String newMarkerNavigationProviderClassName) {
 		String oldMarkerNavigationProviderClassName = markerNavigationProviderClassName;
 		markerNavigationProviderClassName = newMarkerNavigationProviderClassName;
 		if (eNotificationRequired())
 			eNotify(new ENotificationImpl(this, Notification.SET, GMFGenPackage.GEN_DIAGRAM__MARKER_NAVIGATION_PROVIDER_CLASS_NAME, oldMarkerNavigationProviderClassName, markerNavigationProviderClassName));
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public ProviderPriority getMarkerNavigationProviderPriority() {
 		return markerNavigationProviderPriority;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public void setMarkerNavigationProviderPriority(ProviderPriority newMarkerNavigationProviderPriority) {
 		ProviderPriority oldMarkerNavigationProviderPriority = markerNavigationProviderPriority;
 		markerNavigationProviderPriority = newMarkerNavigationProviderPriority == null ? MARKER_NAVIGATION_PROVIDER_PRIORITY_EDEFAULT : newMarkerNavigationProviderPriority;
 		if (eNotificationRequired())
 			eNotify(new ENotificationImpl(this, Notification.SET, GMFGenPackage.GEN_DIAGRAM__MARKER_NAVIGATION_PROVIDER_PRIORITY, oldMarkerNavigationProviderPriority, markerNavigationProviderPriority));
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated NOT
 	 */
 	public boolean generateCreateShortcutAction() {
 		return getContainsShortcutsTo().size() > 0;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated NOT
 	 */
 	public boolean generateShortcutIcon() {
 		return getShortcutsProvidedFor().size() > 0;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	@SuppressWarnings("unchecked")
 		@Override
 	public NotificationChain eInverseAdd(InternalEObject otherEnd, int featureID, NotificationChain msgs) {
 		switch (featureID) {
 			case GMFGenPackage.GEN_DIAGRAM__EDITOR_GEN:
 				if (eInternalContainer() != null)
 					msgs = eBasicRemoveFromContainer(msgs);
 				return eBasicSetContainer(otherEnd, GMFGenPackage.GEN_DIAGRAM__EDITOR_GEN, msgs);
 			case GMFGenPackage.GEN_DIAGRAM__CHILD_NODES:
 				return ((InternalEList<InternalEObject>)(InternalEList<?>)getChildNodes()).basicAdd(otherEnd, msgs);
 			case GMFGenPackage.GEN_DIAGRAM__TOP_LEVEL_NODES:
 				return ((InternalEList<InternalEObject>)(InternalEList<?>)getTopLevelNodes()).basicAdd(otherEnd, msgs);
 			case GMFGenPackage.GEN_DIAGRAM__LINKS:
 				return ((InternalEList<InternalEObject>)(InternalEList<?>)getLinks()).basicAdd(otherEnd, msgs);
 			case GMFGenPackage.GEN_DIAGRAM__COMPARTMENTS:
 				return ((InternalEList<InternalEObject>)(InternalEList<?>)getCompartments()).basicAdd(otherEnd, msgs);
 			case GMFGenPackage.GEN_DIAGRAM__PALETTE:
 				if (palette != null)
 					msgs = ((InternalEObject)palette).eInverseRemove(this, EOPPOSITE_FEATURE_BASE - GMFGenPackage.GEN_DIAGRAM__PALETTE, null, msgs);
 				return basicSetPalette((Palette)otherEnd, msgs);
 		}
 		return super.eInverseAdd(otherEnd, featureID, msgs);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	@Override
 	public NotificationChain eInverseRemove(InternalEObject otherEnd, int featureID, NotificationChain msgs) {
 		switch (featureID) {
 			case GMFGenPackage.GEN_DIAGRAM__EDITOR_GEN:
 				return eBasicSetContainer(null, GMFGenPackage.GEN_DIAGRAM__EDITOR_GEN, msgs);
 			case GMFGenPackage.GEN_DIAGRAM__CHILD_NODES:
 				return ((InternalEList<?>)getChildNodes()).basicRemove(otherEnd, msgs);
 			case GMFGenPackage.GEN_DIAGRAM__TOP_LEVEL_NODES:
 				return ((InternalEList<?>)getTopLevelNodes()).basicRemove(otherEnd, msgs);
 			case GMFGenPackage.GEN_DIAGRAM__LINKS:
 				return ((InternalEList<?>)getLinks()).basicRemove(otherEnd, msgs);
 			case GMFGenPackage.GEN_DIAGRAM__COMPARTMENTS:
 				return ((InternalEList<?>)getCompartments()).basicRemove(otherEnd, msgs);
 			case GMFGenPackage.GEN_DIAGRAM__PALETTE:
 				return basicSetPalette(null, msgs);
 			case GMFGenPackage.GEN_DIAGRAM__PREFERENCES:
 				return basicSetPreferences(null, msgs);
 			case GMFGenPackage.GEN_DIAGRAM__PREFERENCE_PAGES:
 				return ((InternalEList<?>)getPreferencePages()).basicRemove(otherEnd, msgs);
 		}
 		return super.eInverseRemove(otherEnd, featureID, msgs);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	@Override
 	public NotificationChain eBasicRemoveFromContainerFeature(NotificationChain msgs) {
 		switch (eContainerFeatureID) {
 			case GMFGenPackage.GEN_DIAGRAM__EDITOR_GEN:
 				return eInternalContainer().eInverseRemove(this, GMFGenPackage.GEN_EDITOR_GENERATOR__DIAGRAM, GenEditorGenerator.class, msgs);
 		}
 		return super.eBasicRemoveFromContainerFeature(msgs);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	@Override
 	public Object eGet(int featureID, boolean resolve, boolean coreType) {
 		switch (featureID) {
 			case GMFGenPackage.GEN_DIAGRAM__CONTAINED_NODES:
 				return getContainedNodes();
 			case GMFGenPackage.GEN_DIAGRAM__CANONICAL_EDIT_POLICY_CLASS_NAME:
 				return getCanonicalEditPolicyClassName();
 			case GMFGenPackage.GEN_DIAGRAM__EDIT_COMMANDS_PACKAGE_NAME:
 				return getEditCommandsPackageName();
 			case GMFGenPackage.GEN_DIAGRAM__EDIT_HELPERS_PACKAGE_NAME:
 				return getEditHelpersPackageName();
 			case GMFGenPackage.GEN_DIAGRAM__EDIT_PARTS_PACKAGE_NAME:
 				return getEditPartsPackageName();
 			case GMFGenPackage.GEN_DIAGRAM__EDIT_POLICIES_PACKAGE_NAME:
 				return getEditPoliciesPackageName();
 			case GMFGenPackage.GEN_DIAGRAM__PREFERENCES_PACKAGE_NAME:
 				return getPreferencesPackageName();
 			case GMFGenPackage.GEN_DIAGRAM__PROVIDERS_PACKAGE_NAME:
 				return getProvidersPackageName();
 			case GMFGenPackage.GEN_DIAGRAM__PARSERS_PACKAGE_NAME:
 				return getParsersPackageName();
 			case GMFGenPackage.GEN_DIAGRAM__NOTATION_VIEW_FACTORIES_PACKAGE_NAME:
 				return getNotationViewFactoriesPackageName();
 			case GMFGenPackage.GEN_DIAGRAM__ELEMENT_TYPES_CLASS_NAME:
 				return getElementTypesClassName();
 			case GMFGenPackage.GEN_DIAGRAM__NOTATION_VIEW_PROVIDER_CLASS_NAME:
 				return getNotationViewProviderClassName();
 			case GMFGenPackage.GEN_DIAGRAM__NOTATION_VIEW_PROVIDER_PRIORITY:
 				return getNotationViewProviderPriority();
 			case GMFGenPackage.GEN_DIAGRAM__EDIT_PART_PROVIDER_CLASS_NAME:
 				return getEditPartProviderClassName();
 			case GMFGenPackage.GEN_DIAGRAM__EDIT_PART_PROVIDER_PRIORITY:
 				return getEditPartProviderPriority();
 			case GMFGenPackage.GEN_DIAGRAM__MODELING_ASSISTANT_PROVIDER_CLASS_NAME:
 				return getModelingAssistantProviderClassName();
 			case GMFGenPackage.GEN_DIAGRAM__MODELING_ASSISTANT_PROVIDER_PRIORITY:
 				return getModelingAssistantProviderPriority();
 			case GMFGenPackage.GEN_DIAGRAM__ICON_PROVIDER_CLASS_NAME:
 				return getIconProviderClassName();
 			case GMFGenPackage.GEN_DIAGRAM__ICON_PROVIDER_PRIORITY:
 				return getIconProviderPriority();
 			case GMFGenPackage.GEN_DIAGRAM__PARSER_PROVIDER_CLASS_NAME:
 				return getParserProviderClassName();
 			case GMFGenPackage.GEN_DIAGRAM__PARSER_PROVIDER_PRIORITY:
 				return getParserProviderPriority();
 			case GMFGenPackage.GEN_DIAGRAM__CONTRIBUTION_ITEM_PROVIDER_CLASS_NAME:
 				return getContributionItemProviderClassName();
 			case GMFGenPackage.GEN_DIAGRAM__REORIENT_CONNECTION_VIEW_COMMAND_CLASS_NAME:
 				return getReorientConnectionViewCommandClassName();
 			case GMFGenPackage.GEN_DIAGRAM__BASE_EDIT_HELPER_CLASS_NAME:
 				return getBaseEditHelperClassName();
 			case GMFGenPackage.GEN_DIAGRAM__EDIT_PART_FACTORY_CLASS_NAME:
 				return getEditPartFactoryClassName();
 			case GMFGenPackage.GEN_DIAGRAM__BASE_EXTERNAL_NODE_LABEL_EDIT_PART_CLASS_NAME:
 				return getBaseExternalNodeLabelEditPartClassName();
 			case GMFGenPackage.GEN_DIAGRAM__BASE_ITEM_SEMANTIC_EDIT_POLICY_CLASS_NAME:
 				return getBaseItemSemanticEditPolicyClassName();
 			case GMFGenPackage.GEN_DIAGRAM__BASE_GRAPHICAL_NODE_EDIT_POLICY_CLASS_NAME:
 				return getBaseGraphicalNodeEditPolicyClassName();
 			case GMFGenPackage.GEN_DIAGRAM__TEXT_SELECTION_EDIT_POLICY_CLASS_NAME:
 				return getTextSelectionEditPolicyClassName();
 			case GMFGenPackage.GEN_DIAGRAM__TEXT_NON_RESIZABLE_EDIT_POLICY_CLASS_NAME:
 				return getTextNonResizableEditPolicyClassName();
 			case GMFGenPackage.GEN_DIAGRAM__CREATION_WIZARD_CLASS_NAME:
 				return getCreationWizardClassName();
 			case GMFGenPackage.GEN_DIAGRAM__CREATION_WIZARD_PAGE_CLASS_NAME:
 				return getCreationWizardPageClassName();
 			case GMFGenPackage.GEN_DIAGRAM__CREATION_WIZARD_ICON_PATH:
 				return getCreationWizardIconPath();
 			case GMFGenPackage.GEN_DIAGRAM__CREATION_WIZARD_ICON_PATH_X:
 				return getCreationWizardIconPathX();
 			case GMFGenPackage.GEN_DIAGRAM__CREATION_WIZARD_CATEGORY_ID:
 				return getCreationWizardCategoryID();
 			case GMFGenPackage.GEN_DIAGRAM__DIAGRAM_EDITOR_UTIL_CLASS_NAME:
 				return getDiagramEditorUtilClassName();
 			case GMFGenPackage.GEN_DIAGRAM__DOCUMENT_PROVIDER_CLASS_NAME:
 				return getDocumentProviderClassName();
 			case GMFGenPackage.GEN_DIAGRAM__INIT_DIAGRAM_FILE_ACTION_CLASS_NAME:
 				return getInitDiagramFileActionClassName();
 			case GMFGenPackage.GEN_DIAGRAM__NEW_DIAGRAM_FILE_WIZARD_CLASS_NAME:
 				return getNewDiagramFileWizardClassName();
 			case GMFGenPackage.GEN_DIAGRAM__DIAGRAM_CONTENT_INITIALIZER_CLASS_NAME:
 				return getDiagramContentInitializerClassName();
 			case GMFGenPackage.GEN_DIAGRAM__MATCHING_STRATEGY_CLASS_NAME:
 				return getMatchingStrategyClassName();
 			case GMFGenPackage.GEN_DIAGRAM__VISUAL_ID_REGISTRY_CLASS_NAME:
 				return getVisualIDRegistryClassName();
 			case GMFGenPackage.GEN_DIAGRAM__ELEMENT_CHOOSER_CLASS_NAME:
 				return getElementChooserClassName();
 			case GMFGenPackage.GEN_DIAGRAM__LOAD_RESOURCE_ACTION_CLASS_NAME:
 				return getLoadResourceActionClassName();
 			case GMFGenPackage.GEN_DIAGRAM__EDITING_DOMAIN_ID:
 				return getEditingDomainID();
 			case GMFGenPackage.GEN_DIAGRAM__SHORTCUTS_DECORATOR_PROVIDER_CLASS_NAME:
 				return getShortcutsDecoratorProviderClassName();
 			case GMFGenPackage.GEN_DIAGRAM__SHORTCUTS_DECORATOR_PROVIDER_PRIORITY:
 				return getShortcutsDecoratorProviderPriority();
 			case GMFGenPackage.GEN_DIAGRAM__CREATE_SHORTCUT_ACTION_CLASS_NAME:
 				return getCreateShortcutActionClassName();
 			case GMFGenPackage.GEN_DIAGRAM__CREATE_SHORTCUT_DECORATIONS_COMMAND_CLASS_NAME:
 				return getCreateShortcutDecorationsCommandClassName();
 			case GMFGenPackage.GEN_DIAGRAM__SHORTCUT_PROPERTY_TESTER_CLASS_NAME:
 				return getShortcutPropertyTesterClassName();
 			case GMFGenPackage.GEN_DIAGRAM__CONTAINS_SHORTCUTS_TO:
 				return getContainsShortcutsTo();
 			case GMFGenPackage.GEN_DIAGRAM__SHORTCUTS_PROVIDED_FOR:
 				return getShortcutsProvidedFor();
 			case GMFGenPackage.GEN_DIAGRAM__VALIDATION_PROVIDER_CLASS_NAME:
 				return getValidationProviderClassName();
 			case GMFGenPackage.GEN_DIAGRAM__VALIDATION_PROVIDER_PRIORITY:
 				return getValidationProviderPriority();
 			case GMFGenPackage.GEN_DIAGRAM__MARKER_NAVIGATION_PROVIDER_CLASS_NAME:
 				return getMarkerNavigationProviderClassName();
 			case GMFGenPackage.GEN_DIAGRAM__MARKER_NAVIGATION_PROVIDER_PRIORITY:
 				return getMarkerNavigationProviderPriority();
 			case GMFGenPackage.GEN_DIAGRAM__VALIDATION_ENABLED:
 				return isValidationEnabled() ? Boolean.TRUE : Boolean.FALSE;
 			case GMFGenPackage.GEN_DIAGRAM__METRIC_PROVIDER_CLASS_NAME:
 				return getMetricProviderClassName();
 			case GMFGenPackage.GEN_DIAGRAM__METRIC_PROVIDER_PRIORITY:
 				return getMetricProviderPriority();
 			case GMFGenPackage.GEN_DIAGRAM__VALIDATION_DECORATOR_PROVIDER_CLASS_NAME:
 				return getValidationDecoratorProviderClassName();
 			case GMFGenPackage.GEN_DIAGRAM__VALIDATION_DECORATORS:
 				return isValidationDecorators() ? Boolean.TRUE : Boolean.FALSE;
 			case GMFGenPackage.GEN_DIAGRAM__VALIDATION_DECORATOR_PROVIDER_PRIORITY:
 				return getValidationDecoratorProviderPriority();
 			case GMFGenPackage.GEN_DIAGRAM__LIVE_VALIDATION_UI_FEEDBACK:
 				return isLiveValidationUIFeedback() ? Boolean.TRUE : Boolean.FALSE;
 			case GMFGenPackage.GEN_DIAGRAM__UNITS:
 				return getUnits();
 			case GMFGenPackage.GEN_DIAGRAM__EDITOR_GEN:
 				return getEditorGen();
 			case GMFGenPackage.GEN_DIAGRAM__DOMAIN_DIAGRAM_ELEMENT:
 				if (resolve) return getDomainDiagramElement();
 				return basicGetDomainDiagramElement();
 			case GMFGenPackage.GEN_DIAGRAM__CHILD_NODES:
 				return getChildNodes();
 			case GMFGenPackage.GEN_DIAGRAM__TOP_LEVEL_NODES:
 				return getTopLevelNodes();
 			case GMFGenPackage.GEN_DIAGRAM__LINKS:
 				return getLinks();
 			case GMFGenPackage.GEN_DIAGRAM__COMPARTMENTS:
 				return getCompartments();
 			case GMFGenPackage.GEN_DIAGRAM__PALETTE:
 				return getPalette();
 			case GMFGenPackage.GEN_DIAGRAM__SYNCHRONIZED:
 				return isSynchronized() ? Boolean.TRUE : Boolean.FALSE;
 			case GMFGenPackage.GEN_DIAGRAM__PREFERENCES:
 				return getPreferences();
 			case GMFGenPackage.GEN_DIAGRAM__PREFERENCE_PAGES:
 				return getPreferencePages();
 		}
 		return super.eGet(featureID, resolve, coreType);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	@SuppressWarnings("unchecked")
 		@Override
 	public void eSet(int featureID, Object newValue) {
 		switch (featureID) {
 			case GMFGenPackage.GEN_DIAGRAM__CANONICAL_EDIT_POLICY_CLASS_NAME:
 				setCanonicalEditPolicyClassName((String)newValue);
 				return;
 			case GMFGenPackage.GEN_DIAGRAM__EDIT_COMMANDS_PACKAGE_NAME:
 				setEditCommandsPackageName((String)newValue);
 				return;
 			case GMFGenPackage.GEN_DIAGRAM__EDIT_HELPERS_PACKAGE_NAME:
 				setEditHelpersPackageName((String)newValue);
 				return;
 			case GMFGenPackage.GEN_DIAGRAM__EDIT_PARTS_PACKAGE_NAME:
 				setEditPartsPackageName((String)newValue);
 				return;
 			case GMFGenPackage.GEN_DIAGRAM__EDIT_POLICIES_PACKAGE_NAME:
 				setEditPoliciesPackageName((String)newValue);
 				return;
 			case GMFGenPackage.GEN_DIAGRAM__PREFERENCES_PACKAGE_NAME:
 				setPreferencesPackageName((String)newValue);
 				return;
 			case GMFGenPackage.GEN_DIAGRAM__PROVIDERS_PACKAGE_NAME:
 				setProvidersPackageName((String)newValue);
 				return;
 			case GMFGenPackage.GEN_DIAGRAM__PARSERS_PACKAGE_NAME:
 				setParsersPackageName((String)newValue);
 				return;
 			case GMFGenPackage.GEN_DIAGRAM__NOTATION_VIEW_FACTORIES_PACKAGE_NAME:
 				setNotationViewFactoriesPackageName((String)newValue);
 				return;
 			case GMFGenPackage.GEN_DIAGRAM__ELEMENT_TYPES_CLASS_NAME:
 				setElementTypesClassName((String)newValue);
 				return;
 			case GMFGenPackage.GEN_DIAGRAM__NOTATION_VIEW_PROVIDER_CLASS_NAME:
 				setNotationViewProviderClassName((String)newValue);
 				return;
 			case GMFGenPackage.GEN_DIAGRAM__NOTATION_VIEW_PROVIDER_PRIORITY:
 				setNotationViewProviderPriority((ProviderPriority)newValue);
 				return;
 			case GMFGenPackage.GEN_DIAGRAM__EDIT_PART_PROVIDER_CLASS_NAME:
 				setEditPartProviderClassName((String)newValue);
 				return;
 			case GMFGenPackage.GEN_DIAGRAM__EDIT_PART_PROVIDER_PRIORITY:
 				setEditPartProviderPriority((ProviderPriority)newValue);
 				return;
 			case GMFGenPackage.GEN_DIAGRAM__MODELING_ASSISTANT_PROVIDER_CLASS_NAME:
 				setModelingAssistantProviderClassName((String)newValue);
 				return;
 			case GMFGenPackage.GEN_DIAGRAM__MODELING_ASSISTANT_PROVIDER_PRIORITY:
 				setModelingAssistantProviderPriority((ProviderPriority)newValue);
 				return;
 			case GMFGenPackage.GEN_DIAGRAM__ICON_PROVIDER_CLASS_NAME:
 				setIconProviderClassName((String)newValue);
 				return;
 			case GMFGenPackage.GEN_DIAGRAM__ICON_PROVIDER_PRIORITY:
 				setIconProviderPriority((ProviderPriority)newValue);
 				return;
 			case GMFGenPackage.GEN_DIAGRAM__PARSER_PROVIDER_CLASS_NAME:
 				setParserProviderClassName((String)newValue);
 				return;
 			case GMFGenPackage.GEN_DIAGRAM__PARSER_PROVIDER_PRIORITY:
 				setParserProviderPriority((ProviderPriority)newValue);
 				return;
 			case GMFGenPackage.GEN_DIAGRAM__CONTRIBUTION_ITEM_PROVIDER_CLASS_NAME:
 				setContributionItemProviderClassName((String)newValue);
 				return;
 			case GMFGenPackage.GEN_DIAGRAM__REORIENT_CONNECTION_VIEW_COMMAND_CLASS_NAME:
 				setReorientConnectionViewCommandClassName((String)newValue);
 				return;
 			case GMFGenPackage.GEN_DIAGRAM__BASE_EDIT_HELPER_CLASS_NAME:
 				setBaseEditHelperClassName((String)newValue);
 				return;
 			case GMFGenPackage.GEN_DIAGRAM__EDIT_PART_FACTORY_CLASS_NAME:
 				setEditPartFactoryClassName((String)newValue);
 				return;
 			case GMFGenPackage.GEN_DIAGRAM__BASE_EXTERNAL_NODE_LABEL_EDIT_PART_CLASS_NAME:
 				setBaseExternalNodeLabelEditPartClassName((String)newValue);
 				return;
 			case GMFGenPackage.GEN_DIAGRAM__BASE_ITEM_SEMANTIC_EDIT_POLICY_CLASS_NAME:
 				setBaseItemSemanticEditPolicyClassName((String)newValue);
 				return;
 			case GMFGenPackage.GEN_DIAGRAM__BASE_GRAPHICAL_NODE_EDIT_POLICY_CLASS_NAME:
 				setBaseGraphicalNodeEditPolicyClassName((String)newValue);
 				return;
 			case GMFGenPackage.GEN_DIAGRAM__TEXT_SELECTION_EDIT_POLICY_CLASS_NAME:
 				setTextSelectionEditPolicyClassName((String)newValue);
 				return;
 			case GMFGenPackage.GEN_DIAGRAM__TEXT_NON_RESIZABLE_EDIT_POLICY_CLASS_NAME:
 				setTextNonResizableEditPolicyClassName((String)newValue);
 				return;
 			case GMFGenPackage.GEN_DIAGRAM__CREATION_WIZARD_CLASS_NAME:
 				setCreationWizardClassName((String)newValue);
 				return;
 			case GMFGenPackage.GEN_DIAGRAM__CREATION_WIZARD_PAGE_CLASS_NAME:
 				setCreationWizardPageClassName((String)newValue);
 				return;
 			case GMFGenPackage.GEN_DIAGRAM__CREATION_WIZARD_ICON_PATH:
 				setCreationWizardIconPath((String)newValue);
 				return;
 			case GMFGenPackage.GEN_DIAGRAM__CREATION_WIZARD_CATEGORY_ID:
 				setCreationWizardCategoryID((String)newValue);
 				return;
 			case GMFGenPackage.GEN_DIAGRAM__DIAGRAM_EDITOR_UTIL_CLASS_NAME:
 				setDiagramEditorUtilClassName((String)newValue);
 				return;
 			case GMFGenPackage.GEN_DIAGRAM__DOCUMENT_PROVIDER_CLASS_NAME:
 				setDocumentProviderClassName((String)newValue);
 				return;
 			case GMFGenPackage.GEN_DIAGRAM__INIT_DIAGRAM_FILE_ACTION_CLASS_NAME:
 				setInitDiagramFileActionClassName((String)newValue);
 				return;
 			case GMFGenPackage.GEN_DIAGRAM__NEW_DIAGRAM_FILE_WIZARD_CLASS_NAME:
 				setNewDiagramFileWizardClassName((String)newValue);
 				return;
 			case GMFGenPackage.GEN_DIAGRAM__DIAGRAM_CONTENT_INITIALIZER_CLASS_NAME:
 				setDiagramContentInitializerClassName((String)newValue);
 				return;
 			case GMFGenPackage.GEN_DIAGRAM__MATCHING_STRATEGY_CLASS_NAME:
 				setMatchingStrategyClassName((String)newValue);
 				return;
 			case GMFGenPackage.GEN_DIAGRAM__VISUAL_ID_REGISTRY_CLASS_NAME:
 				setVisualIDRegistryClassName((String)newValue);
 				return;
 			case GMFGenPackage.GEN_DIAGRAM__ELEMENT_CHOOSER_CLASS_NAME:
 				setElementChooserClassName((String)newValue);
 				return;
 			case GMFGenPackage.GEN_DIAGRAM__LOAD_RESOURCE_ACTION_CLASS_NAME:
 				setLoadResourceActionClassName((String)newValue);
 				return;
 			case GMFGenPackage.GEN_DIAGRAM__EDITING_DOMAIN_ID:
 				setEditingDomainID((String)newValue);
 				return;
 			case GMFGenPackage.GEN_DIAGRAM__SHORTCUTS_DECORATOR_PROVIDER_CLASS_NAME:
 				setShortcutsDecoratorProviderClassName((String)newValue);
 				return;
 			case GMFGenPackage.GEN_DIAGRAM__SHORTCUTS_DECORATOR_PROVIDER_PRIORITY:
 				setShortcutsDecoratorProviderPriority((ProviderPriority)newValue);
 				return;
 			case GMFGenPackage.GEN_DIAGRAM__CREATE_SHORTCUT_ACTION_CLASS_NAME:
 				setCreateShortcutActionClassName((String)newValue);
 				return;
 			case GMFGenPackage.GEN_DIAGRAM__CREATE_SHORTCUT_DECORATIONS_COMMAND_CLASS_NAME:
 				setCreateShortcutDecorationsCommandClassName((String)newValue);
 				return;
 			case GMFGenPackage.GEN_DIAGRAM__SHORTCUT_PROPERTY_TESTER_CLASS_NAME:
 				setShortcutPropertyTesterClassName((String)newValue);
 				return;
 			case GMFGenPackage.GEN_DIAGRAM__CONTAINS_SHORTCUTS_TO:
 				getContainsShortcutsTo().clear();
 				getContainsShortcutsTo().addAll((Collection<? extends String>)newValue);
 				return;
 			case GMFGenPackage.GEN_DIAGRAM__SHORTCUTS_PROVIDED_FOR:
 				getShortcutsProvidedFor().clear();
 				getShortcutsProvidedFor().addAll((Collection<? extends String>)newValue);
 				return;
 			case GMFGenPackage.GEN_DIAGRAM__VALIDATION_PROVIDER_CLASS_NAME:
 				setValidationProviderClassName((String)newValue);
 				return;
 			case GMFGenPackage.GEN_DIAGRAM__VALIDATION_PROVIDER_PRIORITY:
 				setValidationProviderPriority((ProviderPriority)newValue);
 				return;
 			case GMFGenPackage.GEN_DIAGRAM__MARKER_NAVIGATION_PROVIDER_CLASS_NAME:
 				setMarkerNavigationProviderClassName((String)newValue);
 				return;
 			case GMFGenPackage.GEN_DIAGRAM__MARKER_NAVIGATION_PROVIDER_PRIORITY:
 				setMarkerNavigationProviderPriority((ProviderPriority)newValue);
 				return;
 			case GMFGenPackage.GEN_DIAGRAM__VALIDATION_ENABLED:
 				setValidationEnabled(((Boolean)newValue).booleanValue());
 				return;
 			case GMFGenPackage.GEN_DIAGRAM__METRIC_PROVIDER_CLASS_NAME:
 				setMetricProviderClassName((String)newValue);
 				return;
 			case GMFGenPackage.GEN_DIAGRAM__METRIC_PROVIDER_PRIORITY:
 				setMetricProviderPriority((ProviderPriority)newValue);
 				return;
 			case GMFGenPackage.GEN_DIAGRAM__VALIDATION_DECORATOR_PROVIDER_CLASS_NAME:
 				setValidationDecoratorProviderClassName((String)newValue);
 				return;
 			case GMFGenPackage.GEN_DIAGRAM__VALIDATION_DECORATORS:
 				setValidationDecorators(((Boolean)newValue).booleanValue());
 				return;
 			case GMFGenPackage.GEN_DIAGRAM__VALIDATION_DECORATOR_PROVIDER_PRIORITY:
 				setValidationDecoratorProviderPriority((ProviderPriority)newValue);
 				return;
 			case GMFGenPackage.GEN_DIAGRAM__LIVE_VALIDATION_UI_FEEDBACK:
 				setLiveValidationUIFeedback(((Boolean)newValue).booleanValue());
 				return;
 			case GMFGenPackage.GEN_DIAGRAM__UNITS:
 				setUnits((String)newValue);
 				return;
 			case GMFGenPackage.GEN_DIAGRAM__DOMAIN_DIAGRAM_ELEMENT:
 				setDomainDiagramElement((GenClass)newValue);
 				return;
 			case GMFGenPackage.GEN_DIAGRAM__CHILD_NODES:
 				getChildNodes().clear();
 				getChildNodes().addAll((Collection<? extends GenChildNode>)newValue);
 				return;
 			case GMFGenPackage.GEN_DIAGRAM__TOP_LEVEL_NODES:
 				getTopLevelNodes().clear();
 				getTopLevelNodes().addAll((Collection<? extends GenTopLevelNode>)newValue);
 				return;
 			case GMFGenPackage.GEN_DIAGRAM__LINKS:
 				getLinks().clear();
 				getLinks().addAll((Collection<? extends GenLink>)newValue);
 				return;
 			case GMFGenPackage.GEN_DIAGRAM__COMPARTMENTS:
 				getCompartments().clear();
 				getCompartments().addAll((Collection<? extends GenCompartment>)newValue);
 				return;
 			case GMFGenPackage.GEN_DIAGRAM__PALETTE:
 				setPalette((Palette)newValue);
 				return;
 			case GMFGenPackage.GEN_DIAGRAM__SYNCHRONIZED:
 				setSynchronized(((Boolean)newValue).booleanValue());
 				return;
 			case GMFGenPackage.GEN_DIAGRAM__PREFERENCES:
 				setPreferences((GenDiagramPreferences)newValue);
 				return;
 			case GMFGenPackage.GEN_DIAGRAM__PREFERENCE_PAGES:
 				getPreferencePages().clear();
 				getPreferencePages().addAll((Collection<? extends GenPreferencePage>)newValue);
 				return;
 		}
 		super.eSet(featureID, newValue);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	@Override
 	public void eUnset(int featureID) {
 		switch (featureID) {
 			case GMFGenPackage.GEN_DIAGRAM__CANONICAL_EDIT_POLICY_CLASS_NAME:
 				setCanonicalEditPolicyClassName(CANONICAL_EDIT_POLICY_CLASS_NAME_EDEFAULT);
 				return;
 			case GMFGenPackage.GEN_DIAGRAM__EDIT_COMMANDS_PACKAGE_NAME:
 				setEditCommandsPackageName(EDIT_COMMANDS_PACKAGE_NAME_EDEFAULT);
 				return;
 			case GMFGenPackage.GEN_DIAGRAM__EDIT_HELPERS_PACKAGE_NAME:
 				setEditHelpersPackageName(EDIT_HELPERS_PACKAGE_NAME_EDEFAULT);
 				return;
 			case GMFGenPackage.GEN_DIAGRAM__EDIT_PARTS_PACKAGE_NAME:
 				setEditPartsPackageName(EDIT_PARTS_PACKAGE_NAME_EDEFAULT);
 				return;
 			case GMFGenPackage.GEN_DIAGRAM__EDIT_POLICIES_PACKAGE_NAME:
 				setEditPoliciesPackageName(EDIT_POLICIES_PACKAGE_NAME_EDEFAULT);
 				return;
 			case GMFGenPackage.GEN_DIAGRAM__PREFERENCES_PACKAGE_NAME:
 				setPreferencesPackageName(PREFERENCES_PACKAGE_NAME_EDEFAULT);
 				return;
 			case GMFGenPackage.GEN_DIAGRAM__PROVIDERS_PACKAGE_NAME:
 				setProvidersPackageName(PROVIDERS_PACKAGE_NAME_EDEFAULT);
 				return;
 			case GMFGenPackage.GEN_DIAGRAM__PARSERS_PACKAGE_NAME:
 				setParsersPackageName(PARSERS_PACKAGE_NAME_EDEFAULT);
 				return;
 			case GMFGenPackage.GEN_DIAGRAM__NOTATION_VIEW_FACTORIES_PACKAGE_NAME:
 				setNotationViewFactoriesPackageName(NOTATION_VIEW_FACTORIES_PACKAGE_NAME_EDEFAULT);
 				return;
 			case GMFGenPackage.GEN_DIAGRAM__ELEMENT_TYPES_CLASS_NAME:
 				setElementTypesClassName(ELEMENT_TYPES_CLASS_NAME_EDEFAULT);
 				return;
 			case GMFGenPackage.GEN_DIAGRAM__NOTATION_VIEW_PROVIDER_CLASS_NAME:
 				setNotationViewProviderClassName(NOTATION_VIEW_PROVIDER_CLASS_NAME_EDEFAULT);
 				return;
 			case GMFGenPackage.GEN_DIAGRAM__NOTATION_VIEW_PROVIDER_PRIORITY:
 				setNotationViewProviderPriority(NOTATION_VIEW_PROVIDER_PRIORITY_EDEFAULT);
 				return;
 			case GMFGenPackage.GEN_DIAGRAM__EDIT_PART_PROVIDER_CLASS_NAME:
 				setEditPartProviderClassName(EDIT_PART_PROVIDER_CLASS_NAME_EDEFAULT);
 				return;
 			case GMFGenPackage.GEN_DIAGRAM__EDIT_PART_PROVIDER_PRIORITY:
 				setEditPartProviderPriority(EDIT_PART_PROVIDER_PRIORITY_EDEFAULT);
 				return;
 			case GMFGenPackage.GEN_DIAGRAM__MODELING_ASSISTANT_PROVIDER_CLASS_NAME:
 				setModelingAssistantProviderClassName(MODELING_ASSISTANT_PROVIDER_CLASS_NAME_EDEFAULT);
 				return;
 			case GMFGenPackage.GEN_DIAGRAM__MODELING_ASSISTANT_PROVIDER_PRIORITY:
 				setModelingAssistantProviderPriority(MODELING_ASSISTANT_PROVIDER_PRIORITY_EDEFAULT);
 				return;
 			case GMFGenPackage.GEN_DIAGRAM__ICON_PROVIDER_CLASS_NAME:
 				setIconProviderClassName(ICON_PROVIDER_CLASS_NAME_EDEFAULT);
 				return;
 			case GMFGenPackage.GEN_DIAGRAM__ICON_PROVIDER_PRIORITY:
 				setIconProviderPriority(ICON_PROVIDER_PRIORITY_EDEFAULT);
 				return;
 			case GMFGenPackage.GEN_DIAGRAM__PARSER_PROVIDER_CLASS_NAME:
 				setParserProviderClassName(PARSER_PROVIDER_CLASS_NAME_EDEFAULT);
 				return;
 			case GMFGenPackage.GEN_DIAGRAM__PARSER_PROVIDER_PRIORITY:
 				setParserProviderPriority(PARSER_PROVIDER_PRIORITY_EDEFAULT);
 				return;
 			case GMFGenPackage.GEN_DIAGRAM__CONTRIBUTION_ITEM_PROVIDER_CLASS_NAME:
 				setContributionItemProviderClassName(CONTRIBUTION_ITEM_PROVIDER_CLASS_NAME_EDEFAULT);
 				return;
 			case GMFGenPackage.GEN_DIAGRAM__REORIENT_CONNECTION_VIEW_COMMAND_CLASS_NAME:
 				setReorientConnectionViewCommandClassName(REORIENT_CONNECTION_VIEW_COMMAND_CLASS_NAME_EDEFAULT);
 				return;
 			case GMFGenPackage.GEN_DIAGRAM__BASE_EDIT_HELPER_CLASS_NAME:
 				setBaseEditHelperClassName(BASE_EDIT_HELPER_CLASS_NAME_EDEFAULT);
 				return;
 			case GMFGenPackage.GEN_DIAGRAM__EDIT_PART_FACTORY_CLASS_NAME:
 				setEditPartFactoryClassName(EDIT_PART_FACTORY_CLASS_NAME_EDEFAULT);
 				return;
 			case GMFGenPackage.GEN_DIAGRAM__BASE_EXTERNAL_NODE_LABEL_EDIT_PART_CLASS_NAME:
 				setBaseExternalNodeLabelEditPartClassName(BASE_EXTERNAL_NODE_LABEL_EDIT_PART_CLASS_NAME_EDEFAULT);
 				return;
 			case GMFGenPackage.GEN_DIAGRAM__BASE_ITEM_SEMANTIC_EDIT_POLICY_CLASS_NAME:
 				setBaseItemSemanticEditPolicyClassName(BASE_ITEM_SEMANTIC_EDIT_POLICY_CLASS_NAME_EDEFAULT);
 				return;
 			case GMFGenPackage.GEN_DIAGRAM__BASE_GRAPHICAL_NODE_EDIT_POLICY_CLASS_NAME:
 				setBaseGraphicalNodeEditPolicyClassName(BASE_GRAPHICAL_NODE_EDIT_POLICY_CLASS_NAME_EDEFAULT);
 				return;
 			case GMFGenPackage.GEN_DIAGRAM__TEXT_SELECTION_EDIT_POLICY_CLASS_NAME:
 				setTextSelectionEditPolicyClassName(TEXT_SELECTION_EDIT_POLICY_CLASS_NAME_EDEFAULT);
 				return;
 			case GMFGenPackage.GEN_DIAGRAM__TEXT_NON_RESIZABLE_EDIT_POLICY_CLASS_NAME:
 				setTextNonResizableEditPolicyClassName(TEXT_NON_RESIZABLE_EDIT_POLICY_CLASS_NAME_EDEFAULT);
 				return;
 			case GMFGenPackage.GEN_DIAGRAM__CREATION_WIZARD_CLASS_NAME:
 				setCreationWizardClassName(CREATION_WIZARD_CLASS_NAME_EDEFAULT);
 				return;
 			case GMFGenPackage.GEN_DIAGRAM__CREATION_WIZARD_PAGE_CLASS_NAME:
 				setCreationWizardPageClassName(CREATION_WIZARD_PAGE_CLASS_NAME_EDEFAULT);
 				return;
 			case GMFGenPackage.GEN_DIAGRAM__CREATION_WIZARD_ICON_PATH:
 				setCreationWizardIconPath(CREATION_WIZARD_ICON_PATH_EDEFAULT);
 				return;
 			case GMFGenPackage.GEN_DIAGRAM__CREATION_WIZARD_CATEGORY_ID:
 				setCreationWizardCategoryID(CREATION_WIZARD_CATEGORY_ID_EDEFAULT);
 				return;
 			case GMFGenPackage.GEN_DIAGRAM__DIAGRAM_EDITOR_UTIL_CLASS_NAME:
 				setDiagramEditorUtilClassName(DIAGRAM_EDITOR_UTIL_CLASS_NAME_EDEFAULT);
 				return;
 			case GMFGenPackage.GEN_DIAGRAM__DOCUMENT_PROVIDER_CLASS_NAME:
 				setDocumentProviderClassName(DOCUMENT_PROVIDER_CLASS_NAME_EDEFAULT);
 				return;
 			case GMFGenPackage.GEN_DIAGRAM__INIT_DIAGRAM_FILE_ACTION_CLASS_NAME:
 				setInitDiagramFileActionClassName(INIT_DIAGRAM_FILE_ACTION_CLASS_NAME_EDEFAULT);
 				return;
 			case GMFGenPackage.GEN_DIAGRAM__NEW_DIAGRAM_FILE_WIZARD_CLASS_NAME:
 				setNewDiagramFileWizardClassName(NEW_DIAGRAM_FILE_WIZARD_CLASS_NAME_EDEFAULT);
 				return;
 			case GMFGenPackage.GEN_DIAGRAM__DIAGRAM_CONTENT_INITIALIZER_CLASS_NAME:
 				setDiagramContentInitializerClassName(DIAGRAM_CONTENT_INITIALIZER_CLASS_NAME_EDEFAULT);
 				return;
 			case GMFGenPackage.GEN_DIAGRAM__MATCHING_STRATEGY_CLASS_NAME:
 				setMatchingStrategyClassName(MATCHING_STRATEGY_CLASS_NAME_EDEFAULT);
 				return;
 			case GMFGenPackage.GEN_DIAGRAM__VISUAL_ID_REGISTRY_CLASS_NAME:
 				setVisualIDRegistryClassName(VISUAL_ID_REGISTRY_CLASS_NAME_EDEFAULT);
 				return;
 			case GMFGenPackage.GEN_DIAGRAM__ELEMENT_CHOOSER_CLASS_NAME:
 				setElementChooserClassName(ELEMENT_CHOOSER_CLASS_NAME_EDEFAULT);
 				return;
 			case GMFGenPackage.GEN_DIAGRAM__LOAD_RESOURCE_ACTION_CLASS_NAME:
 				setLoadResourceActionClassName(LOAD_RESOURCE_ACTION_CLASS_NAME_EDEFAULT);
 				return;
 			case GMFGenPackage.GEN_DIAGRAM__EDITING_DOMAIN_ID:
 				setEditingDomainID(EDITING_DOMAIN_ID_EDEFAULT);
 				return;
 			case GMFGenPackage.GEN_DIAGRAM__SHORTCUTS_DECORATOR_PROVIDER_CLASS_NAME:
 				setShortcutsDecoratorProviderClassName(SHORTCUTS_DECORATOR_PROVIDER_CLASS_NAME_EDEFAULT);
 				return;
 			case GMFGenPackage.GEN_DIAGRAM__SHORTCUTS_DECORATOR_PROVIDER_PRIORITY:
 				setShortcutsDecoratorProviderPriority(SHORTCUTS_DECORATOR_PROVIDER_PRIORITY_EDEFAULT);
 				return;
 			case GMFGenPackage.GEN_DIAGRAM__CREATE_SHORTCUT_ACTION_CLASS_NAME:
 				setCreateShortcutActionClassName(CREATE_SHORTCUT_ACTION_CLASS_NAME_EDEFAULT);
 				return;
 			case GMFGenPackage.GEN_DIAGRAM__CREATE_SHORTCUT_DECORATIONS_COMMAND_CLASS_NAME:
 				setCreateShortcutDecorationsCommandClassName(CREATE_SHORTCUT_DECORATIONS_COMMAND_CLASS_NAME_EDEFAULT);
 				return;
 			case GMFGenPackage.GEN_DIAGRAM__SHORTCUT_PROPERTY_TESTER_CLASS_NAME:
 				setShortcutPropertyTesterClassName(SHORTCUT_PROPERTY_TESTER_CLASS_NAME_EDEFAULT);
 				return;
 			case GMFGenPackage.GEN_DIAGRAM__CONTAINS_SHORTCUTS_TO:
 				getContainsShortcutsTo().clear();
 				return;
 			case GMFGenPackage.GEN_DIAGRAM__SHORTCUTS_PROVIDED_FOR:
 				getShortcutsProvidedFor().clear();
 				return;
 			case GMFGenPackage.GEN_DIAGRAM__VALIDATION_PROVIDER_CLASS_NAME:
 				setValidationProviderClassName(VALIDATION_PROVIDER_CLASS_NAME_EDEFAULT);
 				return;
 			case GMFGenPackage.GEN_DIAGRAM__VALIDATION_PROVIDER_PRIORITY:
 				setValidationProviderPriority(VALIDATION_PROVIDER_PRIORITY_EDEFAULT);
 				return;
 			case GMFGenPackage.GEN_DIAGRAM__MARKER_NAVIGATION_PROVIDER_CLASS_NAME:
 				setMarkerNavigationProviderClassName(MARKER_NAVIGATION_PROVIDER_CLASS_NAME_EDEFAULT);
 				return;
 			case GMFGenPackage.GEN_DIAGRAM__MARKER_NAVIGATION_PROVIDER_PRIORITY:
 				setMarkerNavigationProviderPriority(MARKER_NAVIGATION_PROVIDER_PRIORITY_EDEFAULT);
 				return;
 			case GMFGenPackage.GEN_DIAGRAM__VALIDATION_ENABLED:
 				setValidationEnabled(VALIDATION_ENABLED_EDEFAULT);
 				return;
 			case GMFGenPackage.GEN_DIAGRAM__METRIC_PROVIDER_CLASS_NAME:
 				setMetricProviderClassName(METRIC_PROVIDER_CLASS_NAME_EDEFAULT);
 				return;
 			case GMFGenPackage.GEN_DIAGRAM__METRIC_PROVIDER_PRIORITY:
 				setMetricProviderPriority(METRIC_PROVIDER_PRIORITY_EDEFAULT);
 				return;
 			case GMFGenPackage.GEN_DIAGRAM__VALIDATION_DECORATOR_PROVIDER_CLASS_NAME:
 				setValidationDecoratorProviderClassName(VALIDATION_DECORATOR_PROVIDER_CLASS_NAME_EDEFAULT);
 				return;
 			case GMFGenPackage.GEN_DIAGRAM__VALIDATION_DECORATORS:
 				setValidationDecorators(VALIDATION_DECORATORS_EDEFAULT);
 				return;
 			case GMFGenPackage.GEN_DIAGRAM__VALIDATION_DECORATOR_PROVIDER_PRIORITY:
 				setValidationDecoratorProviderPriority(VALIDATION_DECORATOR_PROVIDER_PRIORITY_EDEFAULT);
 				return;
 			case GMFGenPackage.GEN_DIAGRAM__LIVE_VALIDATION_UI_FEEDBACK:
 				setLiveValidationUIFeedback(LIVE_VALIDATION_UI_FEEDBACK_EDEFAULT);
 				return;
 			case GMFGenPackage.GEN_DIAGRAM__UNITS:
 				setUnits(UNITS_EDEFAULT);
 				return;
 			case GMFGenPackage.GEN_DIAGRAM__DOMAIN_DIAGRAM_ELEMENT:
 				setDomainDiagramElement((GenClass)null);
 				return;
 			case GMFGenPackage.GEN_DIAGRAM__CHILD_NODES:
 				getChildNodes().clear();
 				return;
 			case GMFGenPackage.GEN_DIAGRAM__TOP_LEVEL_NODES:
 				getTopLevelNodes().clear();
 				return;
 			case GMFGenPackage.GEN_DIAGRAM__LINKS:
 				getLinks().clear();
 				return;
 			case GMFGenPackage.GEN_DIAGRAM__COMPARTMENTS:
 				getCompartments().clear();
 				return;
 			case GMFGenPackage.GEN_DIAGRAM__PALETTE:
 				setPalette((Palette)null);
 				return;
 			case GMFGenPackage.GEN_DIAGRAM__SYNCHRONIZED:
 				setSynchronized(SYNCHRONIZED_EDEFAULT);
 				return;
 			case GMFGenPackage.GEN_DIAGRAM__PREFERENCES:
 				setPreferences((GenDiagramPreferences)null);
 				return;
 			case GMFGenPackage.GEN_DIAGRAM__PREFERENCE_PAGES:
 				getPreferencePages().clear();
 				return;
 		}
 		super.eUnset(featureID);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	@Override
 	public boolean eIsSet(int featureID) {
 		switch (featureID) {
 			case GMFGenPackage.GEN_DIAGRAM__CONTAINED_NODES:
 				return !getContainedNodes().isEmpty();
 			case GMFGenPackage.GEN_DIAGRAM__CANONICAL_EDIT_POLICY_CLASS_NAME:
 				return CANONICAL_EDIT_POLICY_CLASS_NAME_EDEFAULT == null ? canonicalEditPolicyClassName != null : !CANONICAL_EDIT_POLICY_CLASS_NAME_EDEFAULT.equals(canonicalEditPolicyClassName);
 			case GMFGenPackage.GEN_DIAGRAM__EDIT_COMMANDS_PACKAGE_NAME:
 				return EDIT_COMMANDS_PACKAGE_NAME_EDEFAULT == null ? editCommandsPackageName != null : !EDIT_COMMANDS_PACKAGE_NAME_EDEFAULT.equals(editCommandsPackageName);
 			case GMFGenPackage.GEN_DIAGRAM__EDIT_HELPERS_PACKAGE_NAME:
 				return EDIT_HELPERS_PACKAGE_NAME_EDEFAULT == null ? editHelpersPackageName != null : !EDIT_HELPERS_PACKAGE_NAME_EDEFAULT.equals(editHelpersPackageName);
 			case GMFGenPackage.GEN_DIAGRAM__EDIT_PARTS_PACKAGE_NAME:
 				return EDIT_PARTS_PACKAGE_NAME_EDEFAULT == null ? editPartsPackageName != null : !EDIT_PARTS_PACKAGE_NAME_EDEFAULT.equals(editPartsPackageName);
 			case GMFGenPackage.GEN_DIAGRAM__EDIT_POLICIES_PACKAGE_NAME:
 				return EDIT_POLICIES_PACKAGE_NAME_EDEFAULT == null ? editPoliciesPackageName != null : !EDIT_POLICIES_PACKAGE_NAME_EDEFAULT.equals(editPoliciesPackageName);
 			case GMFGenPackage.GEN_DIAGRAM__PREFERENCES_PACKAGE_NAME:
 				return PREFERENCES_PACKAGE_NAME_EDEFAULT == null ? preferencesPackageName != null : !PREFERENCES_PACKAGE_NAME_EDEFAULT.equals(preferencesPackageName);
 			case GMFGenPackage.GEN_DIAGRAM__PROVIDERS_PACKAGE_NAME:
 				return PROVIDERS_PACKAGE_NAME_EDEFAULT == null ? providersPackageName != null : !PROVIDERS_PACKAGE_NAME_EDEFAULT.equals(providersPackageName);
 			case GMFGenPackage.GEN_DIAGRAM__PARSERS_PACKAGE_NAME:
 				return PARSERS_PACKAGE_NAME_EDEFAULT == null ? parsersPackageName != null : !PARSERS_PACKAGE_NAME_EDEFAULT.equals(parsersPackageName);
 			case GMFGenPackage.GEN_DIAGRAM__NOTATION_VIEW_FACTORIES_PACKAGE_NAME:
 				return NOTATION_VIEW_FACTORIES_PACKAGE_NAME_EDEFAULT == null ? notationViewFactoriesPackageName != null : !NOTATION_VIEW_FACTORIES_PACKAGE_NAME_EDEFAULT.equals(notationViewFactoriesPackageName);
 			case GMFGenPackage.GEN_DIAGRAM__ELEMENT_TYPES_CLASS_NAME:
 				return ELEMENT_TYPES_CLASS_NAME_EDEFAULT == null ? elementTypesClassName != null : !ELEMENT_TYPES_CLASS_NAME_EDEFAULT.equals(elementTypesClassName);
 			case GMFGenPackage.GEN_DIAGRAM__NOTATION_VIEW_PROVIDER_CLASS_NAME:
 				return NOTATION_VIEW_PROVIDER_CLASS_NAME_EDEFAULT == null ? notationViewProviderClassName != null : !NOTATION_VIEW_PROVIDER_CLASS_NAME_EDEFAULT.equals(notationViewProviderClassName);
 			case GMFGenPackage.GEN_DIAGRAM__NOTATION_VIEW_PROVIDER_PRIORITY:
 				return notationViewProviderPriority != NOTATION_VIEW_PROVIDER_PRIORITY_EDEFAULT;
 			case GMFGenPackage.GEN_DIAGRAM__EDIT_PART_PROVIDER_CLASS_NAME:
 				return EDIT_PART_PROVIDER_CLASS_NAME_EDEFAULT == null ? editPartProviderClassName != null : !EDIT_PART_PROVIDER_CLASS_NAME_EDEFAULT.equals(editPartProviderClassName);
 			case GMFGenPackage.GEN_DIAGRAM__EDIT_PART_PROVIDER_PRIORITY:
 				return editPartProviderPriority != EDIT_PART_PROVIDER_PRIORITY_EDEFAULT;
 			case GMFGenPackage.GEN_DIAGRAM__MODELING_ASSISTANT_PROVIDER_CLASS_NAME:
 				return MODELING_ASSISTANT_PROVIDER_CLASS_NAME_EDEFAULT == null ? modelingAssistantProviderClassName != null : !MODELING_ASSISTANT_PROVIDER_CLASS_NAME_EDEFAULT.equals(modelingAssistantProviderClassName);
 			case GMFGenPackage.GEN_DIAGRAM__MODELING_ASSISTANT_PROVIDER_PRIORITY:
 				return modelingAssistantProviderPriority != MODELING_ASSISTANT_PROVIDER_PRIORITY_EDEFAULT;
 			case GMFGenPackage.GEN_DIAGRAM__ICON_PROVIDER_CLASS_NAME:
 				return ICON_PROVIDER_CLASS_NAME_EDEFAULT == null ? iconProviderClassName != null : !ICON_PROVIDER_CLASS_NAME_EDEFAULT.equals(iconProviderClassName);
 			case GMFGenPackage.GEN_DIAGRAM__ICON_PROVIDER_PRIORITY:
 				return iconProviderPriority != ICON_PROVIDER_PRIORITY_EDEFAULT;
 			case GMFGenPackage.GEN_DIAGRAM__PARSER_PROVIDER_CLASS_NAME:
 				return PARSER_PROVIDER_CLASS_NAME_EDEFAULT == null ? parserProviderClassName != null : !PARSER_PROVIDER_CLASS_NAME_EDEFAULT.equals(parserProviderClassName);
 			case GMFGenPackage.GEN_DIAGRAM__PARSER_PROVIDER_PRIORITY:
 				return parserProviderPriority != PARSER_PROVIDER_PRIORITY_EDEFAULT;
 			case GMFGenPackage.GEN_DIAGRAM__CONTRIBUTION_ITEM_PROVIDER_CLASS_NAME:
 				return CONTRIBUTION_ITEM_PROVIDER_CLASS_NAME_EDEFAULT == null ? contributionItemProviderClassName != null : !CONTRIBUTION_ITEM_PROVIDER_CLASS_NAME_EDEFAULT.equals(contributionItemProviderClassName);
 			case GMFGenPackage.GEN_DIAGRAM__REORIENT_CONNECTION_VIEW_COMMAND_CLASS_NAME:
 				return REORIENT_CONNECTION_VIEW_COMMAND_CLASS_NAME_EDEFAULT == null ? reorientConnectionViewCommandClassName != null : !REORIENT_CONNECTION_VIEW_COMMAND_CLASS_NAME_EDEFAULT.equals(reorientConnectionViewCommandClassName);
 			case GMFGenPackage.GEN_DIAGRAM__BASE_EDIT_HELPER_CLASS_NAME:
 				return BASE_EDIT_HELPER_CLASS_NAME_EDEFAULT == null ? baseEditHelperClassName != null : !BASE_EDIT_HELPER_CLASS_NAME_EDEFAULT.equals(baseEditHelperClassName);
 			case GMFGenPackage.GEN_DIAGRAM__EDIT_PART_FACTORY_CLASS_NAME:
 				return EDIT_PART_FACTORY_CLASS_NAME_EDEFAULT == null ? editPartFactoryClassName != null : !EDIT_PART_FACTORY_CLASS_NAME_EDEFAULT.equals(editPartFactoryClassName);
 			case GMFGenPackage.GEN_DIAGRAM__BASE_EXTERNAL_NODE_LABEL_EDIT_PART_CLASS_NAME:
 				return BASE_EXTERNAL_NODE_LABEL_EDIT_PART_CLASS_NAME_EDEFAULT == null ? baseExternalNodeLabelEditPartClassName != null : !BASE_EXTERNAL_NODE_LABEL_EDIT_PART_CLASS_NAME_EDEFAULT.equals(baseExternalNodeLabelEditPartClassName);
 			case GMFGenPackage.GEN_DIAGRAM__BASE_ITEM_SEMANTIC_EDIT_POLICY_CLASS_NAME:
 				return BASE_ITEM_SEMANTIC_EDIT_POLICY_CLASS_NAME_EDEFAULT == null ? baseItemSemanticEditPolicyClassName != null : !BASE_ITEM_SEMANTIC_EDIT_POLICY_CLASS_NAME_EDEFAULT.equals(baseItemSemanticEditPolicyClassName);
 			case GMFGenPackage.GEN_DIAGRAM__BASE_GRAPHICAL_NODE_EDIT_POLICY_CLASS_NAME:
 				return BASE_GRAPHICAL_NODE_EDIT_POLICY_CLASS_NAME_EDEFAULT == null ? baseGraphicalNodeEditPolicyClassName != null : !BASE_GRAPHICAL_NODE_EDIT_POLICY_CLASS_NAME_EDEFAULT.equals(baseGraphicalNodeEditPolicyClassName);
 			case GMFGenPackage.GEN_DIAGRAM__TEXT_SELECTION_EDIT_POLICY_CLASS_NAME:
 				return TEXT_SELECTION_EDIT_POLICY_CLASS_NAME_EDEFAULT == null ? textSelectionEditPolicyClassName != null : !TEXT_SELECTION_EDIT_POLICY_CLASS_NAME_EDEFAULT.equals(textSelectionEditPolicyClassName);
 			case GMFGenPackage.GEN_DIAGRAM__TEXT_NON_RESIZABLE_EDIT_POLICY_CLASS_NAME:
 				return TEXT_NON_RESIZABLE_EDIT_POLICY_CLASS_NAME_EDEFAULT == null ? textNonResizableEditPolicyClassName != null : !TEXT_NON_RESIZABLE_EDIT_POLICY_CLASS_NAME_EDEFAULT.equals(textNonResizableEditPolicyClassName);
 			case GMFGenPackage.GEN_DIAGRAM__CREATION_WIZARD_CLASS_NAME:
 				return CREATION_WIZARD_CLASS_NAME_EDEFAULT == null ? creationWizardClassName != null : !CREATION_WIZARD_CLASS_NAME_EDEFAULT.equals(creationWizardClassName);
 			case GMFGenPackage.GEN_DIAGRAM__CREATION_WIZARD_PAGE_CLASS_NAME:
 				return CREATION_WIZARD_PAGE_CLASS_NAME_EDEFAULT == null ? creationWizardPageClassName != null : !CREATION_WIZARD_PAGE_CLASS_NAME_EDEFAULT.equals(creationWizardPageClassName);
 			case GMFGenPackage.GEN_DIAGRAM__CREATION_WIZARD_ICON_PATH:
 				return CREATION_WIZARD_ICON_PATH_EDEFAULT == null ? creationWizardIconPath != null : !CREATION_WIZARD_ICON_PATH_EDEFAULT.equals(creationWizardIconPath);
 			case GMFGenPackage.GEN_DIAGRAM__CREATION_WIZARD_ICON_PATH_X:
 				return CREATION_WIZARD_ICON_PATH_X_EDEFAULT == null ? getCreationWizardIconPathX() != null : !CREATION_WIZARD_ICON_PATH_X_EDEFAULT.equals(getCreationWizardIconPathX());
 			case GMFGenPackage.GEN_DIAGRAM__CREATION_WIZARD_CATEGORY_ID:
 				return CREATION_WIZARD_CATEGORY_ID_EDEFAULT == null ? creationWizardCategoryID != null : !CREATION_WIZARD_CATEGORY_ID_EDEFAULT.equals(creationWizardCategoryID);
 			case GMFGenPackage.GEN_DIAGRAM__DIAGRAM_EDITOR_UTIL_CLASS_NAME:
 				return DIAGRAM_EDITOR_UTIL_CLASS_NAME_EDEFAULT == null ? diagramEditorUtilClassName != null : !DIAGRAM_EDITOR_UTIL_CLASS_NAME_EDEFAULT.equals(diagramEditorUtilClassName);
 			case GMFGenPackage.GEN_DIAGRAM__DOCUMENT_PROVIDER_CLASS_NAME:
 				return DOCUMENT_PROVIDER_CLASS_NAME_EDEFAULT == null ? documentProviderClassName != null : !DOCUMENT_PROVIDER_CLASS_NAME_EDEFAULT.equals(documentProviderClassName);
 			case GMFGenPackage.GEN_DIAGRAM__INIT_DIAGRAM_FILE_ACTION_CLASS_NAME:
 				return INIT_DIAGRAM_FILE_ACTION_CLASS_NAME_EDEFAULT == null ? initDiagramFileActionClassName != null : !INIT_DIAGRAM_FILE_ACTION_CLASS_NAME_EDEFAULT.equals(initDiagramFileActionClassName);
 			case GMFGenPackage.GEN_DIAGRAM__NEW_DIAGRAM_FILE_WIZARD_CLASS_NAME:
 				return NEW_DIAGRAM_FILE_WIZARD_CLASS_NAME_EDEFAULT == null ? newDiagramFileWizardClassName != null : !NEW_DIAGRAM_FILE_WIZARD_CLASS_NAME_EDEFAULT.equals(newDiagramFileWizardClassName);
 			case GMFGenPackage.GEN_DIAGRAM__DIAGRAM_CONTENT_INITIALIZER_CLASS_NAME:
 				return DIAGRAM_CONTENT_INITIALIZER_CLASS_NAME_EDEFAULT == null ? diagramContentInitializerClassName != null : !DIAGRAM_CONTENT_INITIALIZER_CLASS_NAME_EDEFAULT.equals(diagramContentInitializerClassName);
 			case GMFGenPackage.GEN_DIAGRAM__MATCHING_STRATEGY_CLASS_NAME:
 				return MATCHING_STRATEGY_CLASS_NAME_EDEFAULT == null ? matchingStrategyClassName != null : !MATCHING_STRATEGY_CLASS_NAME_EDEFAULT.equals(matchingStrategyClassName);
 			case GMFGenPackage.GEN_DIAGRAM__VISUAL_ID_REGISTRY_CLASS_NAME:
 				return VISUAL_ID_REGISTRY_CLASS_NAME_EDEFAULT == null ? visualIDRegistryClassName != null : !VISUAL_ID_REGISTRY_CLASS_NAME_EDEFAULT.equals(visualIDRegistryClassName);
 			case GMFGenPackage.GEN_DIAGRAM__ELEMENT_CHOOSER_CLASS_NAME:
 				return ELEMENT_CHOOSER_CLASS_NAME_EDEFAULT == null ? elementChooserClassName != null : !ELEMENT_CHOOSER_CLASS_NAME_EDEFAULT.equals(elementChooserClassName);
 			case GMFGenPackage.GEN_DIAGRAM__LOAD_RESOURCE_ACTION_CLASS_NAME:
 				return LOAD_RESOURCE_ACTION_CLASS_NAME_EDEFAULT == null ? loadResourceActionClassName != null : !LOAD_RESOURCE_ACTION_CLASS_NAME_EDEFAULT.equals(loadResourceActionClassName);
 			case GMFGenPackage.GEN_DIAGRAM__EDITING_DOMAIN_ID:
 				return EDITING_DOMAIN_ID_EDEFAULT == null ? editingDomainID != null : !EDITING_DOMAIN_ID_EDEFAULT.equals(editingDomainID);
 			case GMFGenPackage.GEN_DIAGRAM__SHORTCUTS_DECORATOR_PROVIDER_CLASS_NAME:
 				return SHORTCUTS_DECORATOR_PROVIDER_CLASS_NAME_EDEFAULT == null ? shortcutsDecoratorProviderClassName != null : !SHORTCUTS_DECORATOR_PROVIDER_CLASS_NAME_EDEFAULT.equals(shortcutsDecoratorProviderClassName);
 			case GMFGenPackage.GEN_DIAGRAM__SHORTCUTS_DECORATOR_PROVIDER_PRIORITY:
 				return shortcutsDecoratorProviderPriority != SHORTCUTS_DECORATOR_PROVIDER_PRIORITY_EDEFAULT;
 			case GMFGenPackage.GEN_DIAGRAM__CREATE_SHORTCUT_ACTION_CLASS_NAME:
 				return CREATE_SHORTCUT_ACTION_CLASS_NAME_EDEFAULT == null ? createShortcutActionClassName != null : !CREATE_SHORTCUT_ACTION_CLASS_NAME_EDEFAULT.equals(createShortcutActionClassName);
 			case GMFGenPackage.GEN_DIAGRAM__CREATE_SHORTCUT_DECORATIONS_COMMAND_CLASS_NAME:
 				return CREATE_SHORTCUT_DECORATIONS_COMMAND_CLASS_NAME_EDEFAULT == null ? createShortcutDecorationsCommandClassName != null : !CREATE_SHORTCUT_DECORATIONS_COMMAND_CLASS_NAME_EDEFAULT.equals(createShortcutDecorationsCommandClassName);
 			case GMFGenPackage.GEN_DIAGRAM__SHORTCUT_PROPERTY_TESTER_CLASS_NAME:
 				return SHORTCUT_PROPERTY_TESTER_CLASS_NAME_EDEFAULT == null ? shortcutPropertyTesterClassName != null : !SHORTCUT_PROPERTY_TESTER_CLASS_NAME_EDEFAULT.equals(shortcutPropertyTesterClassName);
 			case GMFGenPackage.GEN_DIAGRAM__CONTAINS_SHORTCUTS_TO:
 				return containsShortcutsTo != null && !containsShortcutsTo.isEmpty();
 			case GMFGenPackage.GEN_DIAGRAM__SHORTCUTS_PROVIDED_FOR:
 				return shortcutsProvidedFor != null && !shortcutsProvidedFor.isEmpty();
 			case GMFGenPackage.GEN_DIAGRAM__VALIDATION_PROVIDER_CLASS_NAME:
 				return VALIDATION_PROVIDER_CLASS_NAME_EDEFAULT == null ? validationProviderClassName != null : !VALIDATION_PROVIDER_CLASS_NAME_EDEFAULT.equals(validationProviderClassName);
 			case GMFGenPackage.GEN_DIAGRAM__VALIDATION_PROVIDER_PRIORITY:
 				return validationProviderPriority != VALIDATION_PROVIDER_PRIORITY_EDEFAULT;
 			case GMFGenPackage.GEN_DIAGRAM__MARKER_NAVIGATION_PROVIDER_CLASS_NAME:
 				return MARKER_NAVIGATION_PROVIDER_CLASS_NAME_EDEFAULT == null ? markerNavigationProviderClassName != null : !MARKER_NAVIGATION_PROVIDER_CLASS_NAME_EDEFAULT.equals(markerNavigationProviderClassName);
 			case GMFGenPackage.GEN_DIAGRAM__MARKER_NAVIGATION_PROVIDER_PRIORITY:
 				return markerNavigationProviderPriority != MARKER_NAVIGATION_PROVIDER_PRIORITY_EDEFAULT;
 			case GMFGenPackage.GEN_DIAGRAM__VALIDATION_ENABLED:
 				return validationEnabled != VALIDATION_ENABLED_EDEFAULT;
 			case GMFGenPackage.GEN_DIAGRAM__METRIC_PROVIDER_CLASS_NAME:
 				return METRIC_PROVIDER_CLASS_NAME_EDEFAULT == null ? metricProviderClassName != null : !METRIC_PROVIDER_CLASS_NAME_EDEFAULT.equals(metricProviderClassName);
 			case GMFGenPackage.GEN_DIAGRAM__METRIC_PROVIDER_PRIORITY:
 				return metricProviderPriority != METRIC_PROVIDER_PRIORITY_EDEFAULT;
 			case GMFGenPackage.GEN_DIAGRAM__VALIDATION_DECORATOR_PROVIDER_CLASS_NAME:
 				return VALIDATION_DECORATOR_PROVIDER_CLASS_NAME_EDEFAULT == null ? validationDecoratorProviderClassName != null : !VALIDATION_DECORATOR_PROVIDER_CLASS_NAME_EDEFAULT.equals(validationDecoratorProviderClassName);
 			case GMFGenPackage.GEN_DIAGRAM__VALIDATION_DECORATORS:
 				return validationDecorators != VALIDATION_DECORATORS_EDEFAULT;
 			case GMFGenPackage.GEN_DIAGRAM__VALIDATION_DECORATOR_PROVIDER_PRIORITY:
 				return validationDecoratorProviderPriority != VALIDATION_DECORATOR_PROVIDER_PRIORITY_EDEFAULT;
 			case GMFGenPackage.GEN_DIAGRAM__LIVE_VALIDATION_UI_FEEDBACK:
 				return liveValidationUIFeedback != LIVE_VALIDATION_UI_FEEDBACK_EDEFAULT;
 			case GMFGenPackage.GEN_DIAGRAM__UNITS:
 				return UNITS_EDEFAULT == null ? units != null : !UNITS_EDEFAULT.equals(units);
 			case GMFGenPackage.GEN_DIAGRAM__EDITOR_GEN:
 				return getEditorGen() != null;
 			case GMFGenPackage.GEN_DIAGRAM__DOMAIN_DIAGRAM_ELEMENT:
 				return domainDiagramElement != null;
 			case GMFGenPackage.GEN_DIAGRAM__CHILD_NODES:
 				return childNodes != null && !childNodes.isEmpty();
 			case GMFGenPackage.GEN_DIAGRAM__TOP_LEVEL_NODES:
 				return topLevelNodes != null && !topLevelNodes.isEmpty();
 			case GMFGenPackage.GEN_DIAGRAM__LINKS:
 				return links != null && !links.isEmpty();
 			case GMFGenPackage.GEN_DIAGRAM__COMPARTMENTS:
 				return compartments != null && !compartments.isEmpty();
 			case GMFGenPackage.GEN_DIAGRAM__PALETTE:
 				return palette != null;
 			case GMFGenPackage.GEN_DIAGRAM__SYNCHRONIZED:
 				return synchronized_ != SYNCHRONIZED_EDEFAULT;
 			case GMFGenPackage.GEN_DIAGRAM__PREFERENCES:
 				return preferences != null;
 			case GMFGenPackage.GEN_DIAGRAM__PREFERENCE_PAGES:
 				return preferencePages != null && !preferencePages.isEmpty();
 		}
 		return super.eIsSet(featureID);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	@Override
 	public int eBaseStructuralFeatureID(int derivedFeatureID, Class<?> baseClass) {
 		if (baseClass == PackageNames.class) {
 			switch (derivedFeatureID) {
 				case GMFGenPackage.GEN_DIAGRAM__EDIT_COMMANDS_PACKAGE_NAME: return GMFGenPackage.PACKAGE_NAMES__EDIT_COMMANDS_PACKAGE_NAME;
 				case GMFGenPackage.GEN_DIAGRAM__EDIT_HELPERS_PACKAGE_NAME: return GMFGenPackage.PACKAGE_NAMES__EDIT_HELPERS_PACKAGE_NAME;
 				case GMFGenPackage.GEN_DIAGRAM__EDIT_PARTS_PACKAGE_NAME: return GMFGenPackage.PACKAGE_NAMES__EDIT_PARTS_PACKAGE_NAME;
 				case GMFGenPackage.GEN_DIAGRAM__EDIT_POLICIES_PACKAGE_NAME: return GMFGenPackage.PACKAGE_NAMES__EDIT_POLICIES_PACKAGE_NAME;
 				case GMFGenPackage.GEN_DIAGRAM__PREFERENCES_PACKAGE_NAME: return GMFGenPackage.PACKAGE_NAMES__PREFERENCES_PACKAGE_NAME;
 				case GMFGenPackage.GEN_DIAGRAM__PROVIDERS_PACKAGE_NAME: return GMFGenPackage.PACKAGE_NAMES__PROVIDERS_PACKAGE_NAME;
 				case GMFGenPackage.GEN_DIAGRAM__PARSERS_PACKAGE_NAME: return GMFGenPackage.PACKAGE_NAMES__PARSERS_PACKAGE_NAME;
 				case GMFGenPackage.GEN_DIAGRAM__NOTATION_VIEW_FACTORIES_PACKAGE_NAME: return GMFGenPackage.PACKAGE_NAMES__NOTATION_VIEW_FACTORIES_PACKAGE_NAME;
 				default: return -1;
 			}
 		}
 		if (baseClass == ProviderClassNames.class) {
 			switch (derivedFeatureID) {
 				case GMFGenPackage.GEN_DIAGRAM__ELEMENT_TYPES_CLASS_NAME: return GMFGenPackage.PROVIDER_CLASS_NAMES__ELEMENT_TYPES_CLASS_NAME;
 				case GMFGenPackage.GEN_DIAGRAM__NOTATION_VIEW_PROVIDER_CLASS_NAME: return GMFGenPackage.PROVIDER_CLASS_NAMES__NOTATION_VIEW_PROVIDER_CLASS_NAME;
 				case GMFGenPackage.GEN_DIAGRAM__NOTATION_VIEW_PROVIDER_PRIORITY: return GMFGenPackage.PROVIDER_CLASS_NAMES__NOTATION_VIEW_PROVIDER_PRIORITY;
 				case GMFGenPackage.GEN_DIAGRAM__EDIT_PART_PROVIDER_CLASS_NAME: return GMFGenPackage.PROVIDER_CLASS_NAMES__EDIT_PART_PROVIDER_CLASS_NAME;
 				case GMFGenPackage.GEN_DIAGRAM__EDIT_PART_PROVIDER_PRIORITY: return GMFGenPackage.PROVIDER_CLASS_NAMES__EDIT_PART_PROVIDER_PRIORITY;
 				case GMFGenPackage.GEN_DIAGRAM__MODELING_ASSISTANT_PROVIDER_CLASS_NAME: return GMFGenPackage.PROVIDER_CLASS_NAMES__MODELING_ASSISTANT_PROVIDER_CLASS_NAME;
 				case GMFGenPackage.GEN_DIAGRAM__MODELING_ASSISTANT_PROVIDER_PRIORITY: return GMFGenPackage.PROVIDER_CLASS_NAMES__MODELING_ASSISTANT_PROVIDER_PRIORITY;
 				case GMFGenPackage.GEN_DIAGRAM__ICON_PROVIDER_CLASS_NAME: return GMFGenPackage.PROVIDER_CLASS_NAMES__ICON_PROVIDER_CLASS_NAME;
 				case GMFGenPackage.GEN_DIAGRAM__ICON_PROVIDER_PRIORITY: return GMFGenPackage.PROVIDER_CLASS_NAMES__ICON_PROVIDER_PRIORITY;
 				case GMFGenPackage.GEN_DIAGRAM__PARSER_PROVIDER_CLASS_NAME: return GMFGenPackage.PROVIDER_CLASS_NAMES__PARSER_PROVIDER_CLASS_NAME;
 				case GMFGenPackage.GEN_DIAGRAM__PARSER_PROVIDER_PRIORITY: return GMFGenPackage.PROVIDER_CLASS_NAMES__PARSER_PROVIDER_PRIORITY;
 				case GMFGenPackage.GEN_DIAGRAM__CONTRIBUTION_ITEM_PROVIDER_CLASS_NAME: return GMFGenPackage.PROVIDER_CLASS_NAMES__CONTRIBUTION_ITEM_PROVIDER_CLASS_NAME;
 				default: return -1;
 			}
 		}
 		if (baseClass == LinkConstraints.class) {
 			switch (derivedFeatureID) {
 				default: return -1;
 			}
 		}
 		if (baseClass == EditPartCandies.class) {
 			switch (derivedFeatureID) {
 				case GMFGenPackage.GEN_DIAGRAM__REORIENT_CONNECTION_VIEW_COMMAND_CLASS_NAME: return GMFGenPackage.EDIT_PART_CANDIES__REORIENT_CONNECTION_VIEW_COMMAND_CLASS_NAME;
 				case GMFGenPackage.GEN_DIAGRAM__BASE_EDIT_HELPER_CLASS_NAME: return GMFGenPackage.EDIT_PART_CANDIES__BASE_EDIT_HELPER_CLASS_NAME;
 				case GMFGenPackage.GEN_DIAGRAM__EDIT_PART_FACTORY_CLASS_NAME: return GMFGenPackage.EDIT_PART_CANDIES__EDIT_PART_FACTORY_CLASS_NAME;
 				case GMFGenPackage.GEN_DIAGRAM__BASE_EXTERNAL_NODE_LABEL_EDIT_PART_CLASS_NAME: return GMFGenPackage.EDIT_PART_CANDIES__BASE_EXTERNAL_NODE_LABEL_EDIT_PART_CLASS_NAME;
 				case GMFGenPackage.GEN_DIAGRAM__BASE_ITEM_SEMANTIC_EDIT_POLICY_CLASS_NAME: return GMFGenPackage.EDIT_PART_CANDIES__BASE_ITEM_SEMANTIC_EDIT_POLICY_CLASS_NAME;
 				case GMFGenPackage.GEN_DIAGRAM__BASE_GRAPHICAL_NODE_EDIT_POLICY_CLASS_NAME: return GMFGenPackage.EDIT_PART_CANDIES__BASE_GRAPHICAL_NODE_EDIT_POLICY_CLASS_NAME;
 				case GMFGenPackage.GEN_DIAGRAM__TEXT_SELECTION_EDIT_POLICY_CLASS_NAME: return GMFGenPackage.EDIT_PART_CANDIES__TEXT_SELECTION_EDIT_POLICY_CLASS_NAME;
 				case GMFGenPackage.GEN_DIAGRAM__TEXT_NON_RESIZABLE_EDIT_POLICY_CLASS_NAME: return GMFGenPackage.EDIT_PART_CANDIES__TEXT_NON_RESIZABLE_EDIT_POLICY_CLASS_NAME;
 				default: return -1;
 			}
 		}
 		if (baseClass == EditorCandies.class) {
 			switch (derivedFeatureID) {
 				case GMFGenPackage.GEN_DIAGRAM__CREATION_WIZARD_CLASS_NAME: return GMFGenPackage.EDITOR_CANDIES__CREATION_WIZARD_CLASS_NAME;
 				case GMFGenPackage.GEN_DIAGRAM__CREATION_WIZARD_PAGE_CLASS_NAME: return GMFGenPackage.EDITOR_CANDIES__CREATION_WIZARD_PAGE_CLASS_NAME;
 				case GMFGenPackage.GEN_DIAGRAM__CREATION_WIZARD_ICON_PATH: return GMFGenPackage.EDITOR_CANDIES__CREATION_WIZARD_ICON_PATH;
 				case GMFGenPackage.GEN_DIAGRAM__CREATION_WIZARD_ICON_PATH_X: return GMFGenPackage.EDITOR_CANDIES__CREATION_WIZARD_ICON_PATH_X;
 				case GMFGenPackage.GEN_DIAGRAM__CREATION_WIZARD_CATEGORY_ID: return GMFGenPackage.EDITOR_CANDIES__CREATION_WIZARD_CATEGORY_ID;
 				case GMFGenPackage.GEN_DIAGRAM__DIAGRAM_EDITOR_UTIL_CLASS_NAME: return GMFGenPackage.EDITOR_CANDIES__DIAGRAM_EDITOR_UTIL_CLASS_NAME;
 				case GMFGenPackage.GEN_DIAGRAM__DOCUMENT_PROVIDER_CLASS_NAME: return GMFGenPackage.EDITOR_CANDIES__DOCUMENT_PROVIDER_CLASS_NAME;
 				case GMFGenPackage.GEN_DIAGRAM__INIT_DIAGRAM_FILE_ACTION_CLASS_NAME: return GMFGenPackage.EDITOR_CANDIES__INIT_DIAGRAM_FILE_ACTION_CLASS_NAME;
 				case GMFGenPackage.GEN_DIAGRAM__NEW_DIAGRAM_FILE_WIZARD_CLASS_NAME: return GMFGenPackage.EDITOR_CANDIES__NEW_DIAGRAM_FILE_WIZARD_CLASS_NAME;
 				case GMFGenPackage.GEN_DIAGRAM__DIAGRAM_CONTENT_INITIALIZER_CLASS_NAME: return GMFGenPackage.EDITOR_CANDIES__DIAGRAM_CONTENT_INITIALIZER_CLASS_NAME;
 				case GMFGenPackage.GEN_DIAGRAM__MATCHING_STRATEGY_CLASS_NAME: return GMFGenPackage.EDITOR_CANDIES__MATCHING_STRATEGY_CLASS_NAME;
 				case GMFGenPackage.GEN_DIAGRAM__VISUAL_ID_REGISTRY_CLASS_NAME: return GMFGenPackage.EDITOR_CANDIES__VISUAL_ID_REGISTRY_CLASS_NAME;
 				case GMFGenPackage.GEN_DIAGRAM__ELEMENT_CHOOSER_CLASS_NAME: return GMFGenPackage.EDITOR_CANDIES__ELEMENT_CHOOSER_CLASS_NAME;
 				case GMFGenPackage.GEN_DIAGRAM__LOAD_RESOURCE_ACTION_CLASS_NAME: return GMFGenPackage.EDITOR_CANDIES__LOAD_RESOURCE_ACTION_CLASS_NAME;
 				case GMFGenPackage.GEN_DIAGRAM__EDITING_DOMAIN_ID: return GMFGenPackage.EDITOR_CANDIES__EDITING_DOMAIN_ID;
 				default: return -1;
 			}
 		}
 		if (baseClass == Shortcuts.class) {
 			switch (derivedFeatureID) {
 				case GMFGenPackage.GEN_DIAGRAM__SHORTCUTS_DECORATOR_PROVIDER_CLASS_NAME: return GMFGenPackage.SHORTCUTS__SHORTCUTS_DECORATOR_PROVIDER_CLASS_NAME;
 				case GMFGenPackage.GEN_DIAGRAM__SHORTCUTS_DECORATOR_PROVIDER_PRIORITY: return GMFGenPackage.SHORTCUTS__SHORTCUTS_DECORATOR_PROVIDER_PRIORITY;
 				case GMFGenPackage.GEN_DIAGRAM__CREATE_SHORTCUT_ACTION_CLASS_NAME: return GMFGenPackage.SHORTCUTS__CREATE_SHORTCUT_ACTION_CLASS_NAME;
 				case GMFGenPackage.GEN_DIAGRAM__CREATE_SHORTCUT_DECORATIONS_COMMAND_CLASS_NAME: return GMFGenPackage.SHORTCUTS__CREATE_SHORTCUT_DECORATIONS_COMMAND_CLASS_NAME;
 				case GMFGenPackage.GEN_DIAGRAM__SHORTCUT_PROPERTY_TESTER_CLASS_NAME: return GMFGenPackage.SHORTCUTS__SHORTCUT_PROPERTY_TESTER_CLASS_NAME;
 				case GMFGenPackage.GEN_DIAGRAM__CONTAINS_SHORTCUTS_TO: return GMFGenPackage.SHORTCUTS__CONTAINS_SHORTCUTS_TO;
 				case GMFGenPackage.GEN_DIAGRAM__SHORTCUTS_PROVIDED_FOR: return GMFGenPackage.SHORTCUTS__SHORTCUTS_PROVIDED_FOR;
 				default: return -1;
 			}
 		}
 		if (baseClass == BatchValidation.class) {
 			switch (derivedFeatureID) {
 				case GMFGenPackage.GEN_DIAGRAM__VALIDATION_PROVIDER_CLASS_NAME: return GMFGenPackage.BATCH_VALIDATION__VALIDATION_PROVIDER_CLASS_NAME;
 				case GMFGenPackage.GEN_DIAGRAM__VALIDATION_PROVIDER_PRIORITY: return GMFGenPackage.BATCH_VALIDATION__VALIDATION_PROVIDER_PRIORITY;
 				case GMFGenPackage.GEN_DIAGRAM__MARKER_NAVIGATION_PROVIDER_CLASS_NAME: return GMFGenPackage.BATCH_VALIDATION__MARKER_NAVIGATION_PROVIDER_CLASS_NAME;
 				case GMFGenPackage.GEN_DIAGRAM__MARKER_NAVIGATION_PROVIDER_PRIORITY: return GMFGenPackage.BATCH_VALIDATION__MARKER_NAVIGATION_PROVIDER_PRIORITY;
 				case GMFGenPackage.GEN_DIAGRAM__VALIDATION_ENABLED: return GMFGenPackage.BATCH_VALIDATION__VALIDATION_ENABLED;
 				case GMFGenPackage.GEN_DIAGRAM__METRIC_PROVIDER_CLASS_NAME: return GMFGenPackage.BATCH_VALIDATION__METRIC_PROVIDER_CLASS_NAME;
 				case GMFGenPackage.GEN_DIAGRAM__METRIC_PROVIDER_PRIORITY: return GMFGenPackage.BATCH_VALIDATION__METRIC_PROVIDER_PRIORITY;
 				case GMFGenPackage.GEN_DIAGRAM__VALIDATION_DECORATOR_PROVIDER_CLASS_NAME: return GMFGenPackage.BATCH_VALIDATION__VALIDATION_DECORATOR_PROVIDER_CLASS_NAME;
 				case GMFGenPackage.GEN_DIAGRAM__VALIDATION_DECORATORS: return GMFGenPackage.BATCH_VALIDATION__VALIDATION_DECORATORS;
 				case GMFGenPackage.GEN_DIAGRAM__VALIDATION_DECORATOR_PROVIDER_PRIORITY: return GMFGenPackage.BATCH_VALIDATION__VALIDATION_DECORATOR_PROVIDER_PRIORITY;
 				case GMFGenPackage.GEN_DIAGRAM__LIVE_VALIDATION_UI_FEEDBACK: return GMFGenPackage.BATCH_VALIDATION__LIVE_VALIDATION_UI_FEEDBACK;
 				default: return -1;
 			}
 		}
 		if (baseClass == MeasurementUnit.class) {
 			switch (derivedFeatureID) {
 				case GMFGenPackage.GEN_DIAGRAM__UNITS: return GMFGenPackage.MEASUREMENT_UNIT__UNITS;
 				default: return -1;
 			}
 		}
 		return super.eBaseStructuralFeatureID(derivedFeatureID, baseClass);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	@Override
 	public int eDerivedStructuralFeatureID(int baseFeatureID, Class<?> baseClass) {
 		if (baseClass == PackageNames.class) {
 			switch (baseFeatureID) {
 				case GMFGenPackage.PACKAGE_NAMES__EDIT_COMMANDS_PACKAGE_NAME: return GMFGenPackage.GEN_DIAGRAM__EDIT_COMMANDS_PACKAGE_NAME;
 				case GMFGenPackage.PACKAGE_NAMES__EDIT_HELPERS_PACKAGE_NAME: return GMFGenPackage.GEN_DIAGRAM__EDIT_HELPERS_PACKAGE_NAME;
 				case GMFGenPackage.PACKAGE_NAMES__EDIT_PARTS_PACKAGE_NAME: return GMFGenPackage.GEN_DIAGRAM__EDIT_PARTS_PACKAGE_NAME;
 				case GMFGenPackage.PACKAGE_NAMES__EDIT_POLICIES_PACKAGE_NAME: return GMFGenPackage.GEN_DIAGRAM__EDIT_POLICIES_PACKAGE_NAME;
 				case GMFGenPackage.PACKAGE_NAMES__PREFERENCES_PACKAGE_NAME: return GMFGenPackage.GEN_DIAGRAM__PREFERENCES_PACKAGE_NAME;
 				case GMFGenPackage.PACKAGE_NAMES__PROVIDERS_PACKAGE_NAME: return GMFGenPackage.GEN_DIAGRAM__PROVIDERS_PACKAGE_NAME;
 				case GMFGenPackage.PACKAGE_NAMES__PARSERS_PACKAGE_NAME: return GMFGenPackage.GEN_DIAGRAM__PARSERS_PACKAGE_NAME;
 				case GMFGenPackage.PACKAGE_NAMES__NOTATION_VIEW_FACTORIES_PACKAGE_NAME: return GMFGenPackage.GEN_DIAGRAM__NOTATION_VIEW_FACTORIES_PACKAGE_NAME;
 				default: return -1;
 			}
 		}
 		if (baseClass == ProviderClassNames.class) {
 			switch (baseFeatureID) {
 				case GMFGenPackage.PROVIDER_CLASS_NAMES__ELEMENT_TYPES_CLASS_NAME: return GMFGenPackage.GEN_DIAGRAM__ELEMENT_TYPES_CLASS_NAME;
 				case GMFGenPackage.PROVIDER_CLASS_NAMES__NOTATION_VIEW_PROVIDER_CLASS_NAME: return GMFGenPackage.GEN_DIAGRAM__NOTATION_VIEW_PROVIDER_CLASS_NAME;
 				case GMFGenPackage.PROVIDER_CLASS_NAMES__NOTATION_VIEW_PROVIDER_PRIORITY: return GMFGenPackage.GEN_DIAGRAM__NOTATION_VIEW_PROVIDER_PRIORITY;
 				case GMFGenPackage.PROVIDER_CLASS_NAMES__EDIT_PART_PROVIDER_CLASS_NAME: return GMFGenPackage.GEN_DIAGRAM__EDIT_PART_PROVIDER_CLASS_NAME;
 				case GMFGenPackage.PROVIDER_CLASS_NAMES__EDIT_PART_PROVIDER_PRIORITY: return GMFGenPackage.GEN_DIAGRAM__EDIT_PART_PROVIDER_PRIORITY;
 				case GMFGenPackage.PROVIDER_CLASS_NAMES__MODELING_ASSISTANT_PROVIDER_CLASS_NAME: return GMFGenPackage.GEN_DIAGRAM__MODELING_ASSISTANT_PROVIDER_CLASS_NAME;
 				case GMFGenPackage.PROVIDER_CLASS_NAMES__MODELING_ASSISTANT_PROVIDER_PRIORITY: return GMFGenPackage.GEN_DIAGRAM__MODELING_ASSISTANT_PROVIDER_PRIORITY;
 				case GMFGenPackage.PROVIDER_CLASS_NAMES__ICON_PROVIDER_CLASS_NAME: return GMFGenPackage.GEN_DIAGRAM__ICON_PROVIDER_CLASS_NAME;
 				case GMFGenPackage.PROVIDER_CLASS_NAMES__ICON_PROVIDER_PRIORITY: return GMFGenPackage.GEN_DIAGRAM__ICON_PROVIDER_PRIORITY;
 				case GMFGenPackage.PROVIDER_CLASS_NAMES__PARSER_PROVIDER_CLASS_NAME: return GMFGenPackage.GEN_DIAGRAM__PARSER_PROVIDER_CLASS_NAME;
 				case GMFGenPackage.PROVIDER_CLASS_NAMES__PARSER_PROVIDER_PRIORITY: return GMFGenPackage.GEN_DIAGRAM__PARSER_PROVIDER_PRIORITY;
 				case GMFGenPackage.PROVIDER_CLASS_NAMES__CONTRIBUTION_ITEM_PROVIDER_CLASS_NAME: return GMFGenPackage.GEN_DIAGRAM__CONTRIBUTION_ITEM_PROVIDER_CLASS_NAME;
 				default: return -1;
 			}
 		}
 		if (baseClass == LinkConstraints.class) {
 			switch (baseFeatureID) {
 				default: return -1;
 			}
 		}
 		if (baseClass == EditPartCandies.class) {
 			switch (baseFeatureID) {
 				case GMFGenPackage.EDIT_PART_CANDIES__REORIENT_CONNECTION_VIEW_COMMAND_CLASS_NAME: return GMFGenPackage.GEN_DIAGRAM__REORIENT_CONNECTION_VIEW_COMMAND_CLASS_NAME;
 				case GMFGenPackage.EDIT_PART_CANDIES__BASE_EDIT_HELPER_CLASS_NAME: return GMFGenPackage.GEN_DIAGRAM__BASE_EDIT_HELPER_CLASS_NAME;
 				case GMFGenPackage.EDIT_PART_CANDIES__EDIT_PART_FACTORY_CLASS_NAME: return GMFGenPackage.GEN_DIAGRAM__EDIT_PART_FACTORY_CLASS_NAME;
 				case GMFGenPackage.EDIT_PART_CANDIES__BASE_EXTERNAL_NODE_LABEL_EDIT_PART_CLASS_NAME: return GMFGenPackage.GEN_DIAGRAM__BASE_EXTERNAL_NODE_LABEL_EDIT_PART_CLASS_NAME;
 				case GMFGenPackage.EDIT_PART_CANDIES__BASE_ITEM_SEMANTIC_EDIT_POLICY_CLASS_NAME: return GMFGenPackage.GEN_DIAGRAM__BASE_ITEM_SEMANTIC_EDIT_POLICY_CLASS_NAME;
 				case GMFGenPackage.EDIT_PART_CANDIES__BASE_GRAPHICAL_NODE_EDIT_POLICY_CLASS_NAME: return GMFGenPackage.GEN_DIAGRAM__BASE_GRAPHICAL_NODE_EDIT_POLICY_CLASS_NAME;
 				case GMFGenPackage.EDIT_PART_CANDIES__TEXT_SELECTION_EDIT_POLICY_CLASS_NAME: return GMFGenPackage.GEN_DIAGRAM__TEXT_SELECTION_EDIT_POLICY_CLASS_NAME;
 				case GMFGenPackage.EDIT_PART_CANDIES__TEXT_NON_RESIZABLE_EDIT_POLICY_CLASS_NAME: return GMFGenPackage.GEN_DIAGRAM__TEXT_NON_RESIZABLE_EDIT_POLICY_CLASS_NAME;
 				default: return -1;
 			}
 		}
 		if (baseClass == EditorCandies.class) {
 			switch (baseFeatureID) {
 				case GMFGenPackage.EDITOR_CANDIES__CREATION_WIZARD_CLASS_NAME: return GMFGenPackage.GEN_DIAGRAM__CREATION_WIZARD_CLASS_NAME;
 				case GMFGenPackage.EDITOR_CANDIES__CREATION_WIZARD_PAGE_CLASS_NAME: return GMFGenPackage.GEN_DIAGRAM__CREATION_WIZARD_PAGE_CLASS_NAME;
 				case GMFGenPackage.EDITOR_CANDIES__CREATION_WIZARD_ICON_PATH: return GMFGenPackage.GEN_DIAGRAM__CREATION_WIZARD_ICON_PATH;
 				case GMFGenPackage.EDITOR_CANDIES__CREATION_WIZARD_ICON_PATH_X: return GMFGenPackage.GEN_DIAGRAM__CREATION_WIZARD_ICON_PATH_X;
 				case GMFGenPackage.EDITOR_CANDIES__CREATION_WIZARD_CATEGORY_ID: return GMFGenPackage.GEN_DIAGRAM__CREATION_WIZARD_CATEGORY_ID;
 				case GMFGenPackage.EDITOR_CANDIES__DIAGRAM_EDITOR_UTIL_CLASS_NAME: return GMFGenPackage.GEN_DIAGRAM__DIAGRAM_EDITOR_UTIL_CLASS_NAME;
 				case GMFGenPackage.EDITOR_CANDIES__DOCUMENT_PROVIDER_CLASS_NAME: return GMFGenPackage.GEN_DIAGRAM__DOCUMENT_PROVIDER_CLASS_NAME;
 				case GMFGenPackage.EDITOR_CANDIES__INIT_DIAGRAM_FILE_ACTION_CLASS_NAME: return GMFGenPackage.GEN_DIAGRAM__INIT_DIAGRAM_FILE_ACTION_CLASS_NAME;
 				case GMFGenPackage.EDITOR_CANDIES__NEW_DIAGRAM_FILE_WIZARD_CLASS_NAME: return GMFGenPackage.GEN_DIAGRAM__NEW_DIAGRAM_FILE_WIZARD_CLASS_NAME;
 				case GMFGenPackage.EDITOR_CANDIES__DIAGRAM_CONTENT_INITIALIZER_CLASS_NAME: return GMFGenPackage.GEN_DIAGRAM__DIAGRAM_CONTENT_INITIALIZER_CLASS_NAME;
 				case GMFGenPackage.EDITOR_CANDIES__MATCHING_STRATEGY_CLASS_NAME: return GMFGenPackage.GEN_DIAGRAM__MATCHING_STRATEGY_CLASS_NAME;
 				case GMFGenPackage.EDITOR_CANDIES__VISUAL_ID_REGISTRY_CLASS_NAME: return GMFGenPackage.GEN_DIAGRAM__VISUAL_ID_REGISTRY_CLASS_NAME;
 				case GMFGenPackage.EDITOR_CANDIES__ELEMENT_CHOOSER_CLASS_NAME: return GMFGenPackage.GEN_DIAGRAM__ELEMENT_CHOOSER_CLASS_NAME;
 				case GMFGenPackage.EDITOR_CANDIES__LOAD_RESOURCE_ACTION_CLASS_NAME: return GMFGenPackage.GEN_DIAGRAM__LOAD_RESOURCE_ACTION_CLASS_NAME;
 				case GMFGenPackage.EDITOR_CANDIES__EDITING_DOMAIN_ID: return GMFGenPackage.GEN_DIAGRAM__EDITING_DOMAIN_ID;
 				default: return -1;
 			}
 		}
 		if (baseClass == Shortcuts.class) {
 			switch (baseFeatureID) {
 				case GMFGenPackage.SHORTCUTS__SHORTCUTS_DECORATOR_PROVIDER_CLASS_NAME: return GMFGenPackage.GEN_DIAGRAM__SHORTCUTS_DECORATOR_PROVIDER_CLASS_NAME;
 				case GMFGenPackage.SHORTCUTS__SHORTCUTS_DECORATOR_PROVIDER_PRIORITY: return GMFGenPackage.GEN_DIAGRAM__SHORTCUTS_DECORATOR_PROVIDER_PRIORITY;
 				case GMFGenPackage.SHORTCUTS__CREATE_SHORTCUT_ACTION_CLASS_NAME: return GMFGenPackage.GEN_DIAGRAM__CREATE_SHORTCUT_ACTION_CLASS_NAME;
 				case GMFGenPackage.SHORTCUTS__CREATE_SHORTCUT_DECORATIONS_COMMAND_CLASS_NAME: return GMFGenPackage.GEN_DIAGRAM__CREATE_SHORTCUT_DECORATIONS_COMMAND_CLASS_NAME;
 				case GMFGenPackage.SHORTCUTS__SHORTCUT_PROPERTY_TESTER_CLASS_NAME: return GMFGenPackage.GEN_DIAGRAM__SHORTCUT_PROPERTY_TESTER_CLASS_NAME;
 				case GMFGenPackage.SHORTCUTS__CONTAINS_SHORTCUTS_TO: return GMFGenPackage.GEN_DIAGRAM__CONTAINS_SHORTCUTS_TO;
 				case GMFGenPackage.SHORTCUTS__SHORTCUTS_PROVIDED_FOR: return GMFGenPackage.GEN_DIAGRAM__SHORTCUTS_PROVIDED_FOR;
 				default: return -1;
 			}
 		}
 		if (baseClass == BatchValidation.class) {
 			switch (baseFeatureID) {
 				case GMFGenPackage.BATCH_VALIDATION__VALIDATION_PROVIDER_CLASS_NAME: return GMFGenPackage.GEN_DIAGRAM__VALIDATION_PROVIDER_CLASS_NAME;
 				case GMFGenPackage.BATCH_VALIDATION__VALIDATION_PROVIDER_PRIORITY: return GMFGenPackage.GEN_DIAGRAM__VALIDATION_PROVIDER_PRIORITY;
 				case GMFGenPackage.BATCH_VALIDATION__MARKER_NAVIGATION_PROVIDER_CLASS_NAME: return GMFGenPackage.GEN_DIAGRAM__MARKER_NAVIGATION_PROVIDER_CLASS_NAME;
 				case GMFGenPackage.BATCH_VALIDATION__MARKER_NAVIGATION_PROVIDER_PRIORITY: return GMFGenPackage.GEN_DIAGRAM__MARKER_NAVIGATION_PROVIDER_PRIORITY;
 				case GMFGenPackage.BATCH_VALIDATION__VALIDATION_ENABLED: return GMFGenPackage.GEN_DIAGRAM__VALIDATION_ENABLED;
 				case GMFGenPackage.BATCH_VALIDATION__METRIC_PROVIDER_CLASS_NAME: return GMFGenPackage.GEN_DIAGRAM__METRIC_PROVIDER_CLASS_NAME;
 				case GMFGenPackage.BATCH_VALIDATION__METRIC_PROVIDER_PRIORITY: return GMFGenPackage.GEN_DIAGRAM__METRIC_PROVIDER_PRIORITY;
 				case GMFGenPackage.BATCH_VALIDATION__VALIDATION_DECORATOR_PROVIDER_CLASS_NAME: return GMFGenPackage.GEN_DIAGRAM__VALIDATION_DECORATOR_PROVIDER_CLASS_NAME;
 				case GMFGenPackage.BATCH_VALIDATION__VALIDATION_DECORATORS: return GMFGenPackage.GEN_DIAGRAM__VALIDATION_DECORATORS;
 				case GMFGenPackage.BATCH_VALIDATION__VALIDATION_DECORATOR_PROVIDER_PRIORITY: return GMFGenPackage.GEN_DIAGRAM__VALIDATION_DECORATOR_PROVIDER_PRIORITY;
 				case GMFGenPackage.BATCH_VALIDATION__LIVE_VALIDATION_UI_FEEDBACK: return GMFGenPackage.GEN_DIAGRAM__LIVE_VALIDATION_UI_FEEDBACK;
 				default: return -1;
 			}
 		}
 		if (baseClass == MeasurementUnit.class) {
 			switch (baseFeatureID) {
 				case GMFGenPackage.MEASUREMENT_UNIT__UNITS: return GMFGenPackage.GEN_DIAGRAM__UNITS;
 				default: return -1;
 			}
 		}
 		return super.eDerivedStructuralFeatureID(baseFeatureID, baseClass);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated NOT
 	 */
 	public boolean hasLinkCreationConstraints() {
 		for (GenLink nextLink : getLinks()) {
 			if(nextLink.getCreationConstraints() != null) {
 				return true;
 			}
 		}
 		return false;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated NOT
 	 */
 	public String getLinkCreationConstraintsClassName() {
 		return "LinkConstraints"; //$NON-NLS-1$		
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated NOT
 	 */
 	public String getLinkCreationConstraintsQualifiedClassName() {
 		String owningClass = getBaseItemSemanticEditPolicyQualifiedClassName();
 		if(owningClass != null) {
 			return owningClass + "." + getLinkCreationConstraintsClassName(); //$NON-NLS-1$
 		}
 		return getLinkCreationConstraintsClassName();
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated NOT
 	 */
 	public String getReorientConnectionViewCommandQualifiedClassName() {
 		return getEditCommandsPackageName() + '.' + getReorientConnectionViewCommandClassName();
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated NOT
 	 */
 	public String getBaseEditHelperQualifiedClassName() {
 		return getEditHelpersPackageName() + '.' + getBaseEditHelperClassName();
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated NOT
 	 */
 	public String getEditPartFactoryQualifiedClassName() {
 		return getEditPartsPackageName() + '.' + getEditPartFactoryClassName();
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated NOT
 	 */
 	public String getBaseExternalNodeLabelEditPartQualifiedClassName() {
 		return getEditPartsPackageName() + '.' + getBaseExternalNodeLabelEditPartClassName();
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated NOT
 	 */
 	public String getBaseItemSemanticEditPolicyQualifiedClassName() {
 		return getEditPoliciesPackageName() + '.' + getBaseItemSemanticEditPolicyClassName();
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated NOT
 	 */
 	public String getBaseGraphicalNodeEditPolicyQualifiedClassName() {
 		return getEditPoliciesPackageName() + '.' + getBaseGraphicalNodeEditPolicyClassName();
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated NOT
 	 */
 	public String getCanonicalEditPolicyQualifiedClassName() {
 		return getEditPoliciesPackageName() + '.' + getCanonicalEditPolicyClassName();
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated NOT
 	 */
 	public String getTextSelectionEditPolicyQualifiedClassName() {
 		return getEditPoliciesPackageName() + '.' + getTextSelectionEditPolicyClassName();
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated NOT
 	 */
 	public String getTextNonResizableEditPolicyQualifiedClassName() {
 		return getEditPoliciesPackageName() + '.' + getTextNonResizableEditPolicyClassName();
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated NOT
 	 */
 	public String getElementTypesQualifiedClassName() {
 		return getProvidersPackageName() + '.' + getElementTypesClassName();
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated NOT
 	 */
 	public String getNotationViewProviderQualifiedClassName() {
 		return getProvidersPackageName() + '.' + getNotationViewProviderClassName();
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated NOT
 	 */
 	public String getInitDiagramFileActionQualifiedClassName() {
 		return getEditorPackageName() + '.' + getInitDiagramFileActionClassName();
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated NOT
 	 */
 	public String getNewDiagramFileWizardQualifiedClassName() {
 		return getEditorPackageName() + '.' + getNewDiagramFileWizardClassName();
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated NOT
 	 */
 	public String getDiagramContentInitializerQualifiedClassName() {
 		return getEditorPackageName() + '.' + getDiagramContentInitializerClassName();
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated NOT
 	 */
 	public String getEditPartProviderQualifiedClassName() {
 		return getProvidersPackageName() + '.' + getEditPartProviderClassName();
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated NOT
 	 */
 	public String getModelingAssistantProviderQualifiedClassName() {
 		return getProvidersPackageName() + '.' + getModelingAssistantProviderClassName();
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated NOT
 	 */
 	public String getIconProviderQualifiedClassName() {
 		return getProvidersPackageName() + '.' + getIconProviderClassName();
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated NOT
 	 */
 	public String getParserProviderQualifiedClassName() {
 		return getProvidersPackageName() + '.' + getParserProviderClassName();
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated NOT
 	 */
 	public String getCreationWizardQualifiedClassName() {
 		return getEditorPackageName() + '.' + getCreationWizardClassName();
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated NOT
 	 */
 	public String getCreationWizardPageQualifiedClassName() {
 		return getEditorPackageName() + '.' + getCreationWizardPageClassName();
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated NOT
 	 */
 	public String getDiagramEditorUtilQualifiedClassName() {
 		return getEditorPackageName() + '.' + getDiagramEditorUtilClassName();
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated NOT
 	 */
 	public String getDocumentProviderQualifiedClassName() {
 		return getEditorPackageName() + '.' + getDocumentProviderClassName();
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated NOT
 	 */
 	public String getMatchingStrategyQualifiedClassName() {
 		return getEditorPackageName() + '.' + getMatchingStrategyClassName();
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated NOT
 	 */
 	public String getElementInitializersClassName() {
 		return "ElementInitializers"; //$NON-NLS-1$
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated NOT
 	 */
 	public String getElementInitializersPackageName() {
 		return getProvidersPackageName();
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	@Override
 	public String toString() {
 		if (eIsProxy()) return super.toString();
 
 		StringBuffer result = new StringBuffer(super.toString());
 		result.append(" (canonicalEditPolicyClassName: ");
 		result.append(canonicalEditPolicyClassName);
 		result.append(", editCommandsPackageName: ");
 		result.append(editCommandsPackageName);
 		result.append(", editHelpersPackageName: ");
 		result.append(editHelpersPackageName);
 		result.append(", editPartsPackageName: ");
 		result.append(editPartsPackageName);
 		result.append(", editPoliciesPackageName: ");
 		result.append(editPoliciesPackageName);
 		result.append(", preferencesPackageName: ");
 		result.append(preferencesPackageName);
 		result.append(", providersPackageName: ");
 		result.append(providersPackageName);
 		result.append(", parsersPackageName: ");
 		result.append(parsersPackageName);
 		result.append(", notationViewFactoriesPackageName: ");
 		result.append(notationViewFactoriesPackageName);
 		result.append(", elementTypesClassName: ");
 		result.append(elementTypesClassName);
 		result.append(", notationViewProviderClassName: ");
 		result.append(notationViewProviderClassName);
 		result.append(", notationViewProviderPriority: ");
 		result.append(notationViewProviderPriority);
 		result.append(", editPartProviderClassName: ");
 		result.append(editPartProviderClassName);
 		result.append(", editPartProviderPriority: ");
 		result.append(editPartProviderPriority);
 		result.append(", modelingAssistantProviderClassName: ");
 		result.append(modelingAssistantProviderClassName);
 		result.append(", modelingAssistantProviderPriority: ");
 		result.append(modelingAssistantProviderPriority);
 		result.append(", iconProviderClassName: ");
 		result.append(iconProviderClassName);
 		result.append(", iconProviderPriority: ");
 		result.append(iconProviderPriority);
 		result.append(", parserProviderClassName: ");
 		result.append(parserProviderClassName);
 		result.append(", parserProviderPriority: ");
 		result.append(parserProviderPriority);
 		result.append(", contributionItemProviderClassName: ");
 		result.append(contributionItemProviderClassName);
 		result.append(", reorientConnectionViewCommandClassName: ");
 		result.append(reorientConnectionViewCommandClassName);
 		result.append(", baseEditHelperClassName: ");
 		result.append(baseEditHelperClassName);
 		result.append(", editPartFactoryClassName: ");
 		result.append(editPartFactoryClassName);
 		result.append(", baseExternalNodeLabelEditPartClassName: ");
 		result.append(baseExternalNodeLabelEditPartClassName);
 		result.append(", baseItemSemanticEditPolicyClassName: ");
 		result.append(baseItemSemanticEditPolicyClassName);
 		result.append(", baseGraphicalNodeEditPolicyClassName: ");
 		result.append(baseGraphicalNodeEditPolicyClassName);
 		result.append(", textSelectionEditPolicyClassName: ");
 		result.append(textSelectionEditPolicyClassName);
 		result.append(", textNonResizableEditPolicyClassName: ");
 		result.append(textNonResizableEditPolicyClassName);
 		result.append(", creationWizardClassName: ");
 		result.append(creationWizardClassName);
 		result.append(", creationWizardPageClassName: ");
 		result.append(creationWizardPageClassName);
 		result.append(", creationWizardIconPath: ");
 		result.append(creationWizardIconPath);
 		result.append(", creationWizardCategoryID: ");
 		result.append(creationWizardCategoryID);
 		result.append(", diagramEditorUtilClassName: ");
 		result.append(diagramEditorUtilClassName);
 		result.append(", documentProviderClassName: ");
 		result.append(documentProviderClassName);
 		result.append(", initDiagramFileActionClassName: ");
 		result.append(initDiagramFileActionClassName);
 		result.append(", newDiagramFileWizardClassName: ");
 		result.append(newDiagramFileWizardClassName);
 		result.append(", diagramContentInitializerClassName: ");
 		result.append(diagramContentInitializerClassName);
 		result.append(", matchingStrategyClassName: ");
 		result.append(matchingStrategyClassName);
 		result.append(", visualIDRegistryClassName: ");
 		result.append(visualIDRegistryClassName);
 		result.append(", elementChooserClassName: ");
 		result.append(elementChooserClassName);
 		result.append(", loadResourceActionClassName: ");
 		result.append(loadResourceActionClassName);
 		result.append(", editingDomainID: ");
 		result.append(editingDomainID);
 		result.append(", shortcutsDecoratorProviderClassName: ");
 		result.append(shortcutsDecoratorProviderClassName);
 		result.append(", shortcutsDecoratorProviderPriority: ");
 		result.append(shortcutsDecoratorProviderPriority);
 		result.append(", createShortcutActionClassName: ");
 		result.append(createShortcutActionClassName);
 		result.append(", createShortcutDecorationsCommandClassName: ");
 		result.append(createShortcutDecorationsCommandClassName);
 		result.append(", shortcutPropertyTesterClassName: ");
 		result.append(shortcutPropertyTesterClassName);
 		result.append(", containsShortcutsTo: ");
 		result.append(containsShortcutsTo);
 		result.append(", shortcutsProvidedFor: ");
 		result.append(shortcutsProvidedFor);
 		result.append(", validationProviderClassName: ");
 		result.append(validationProviderClassName);
 		result.append(", validationProviderPriority: ");
 		result.append(validationProviderPriority);
 		result.append(", markerNavigationProviderClassName: ");
 		result.append(markerNavigationProviderClassName);
 		result.append(", markerNavigationProviderPriority: ");
 		result.append(markerNavigationProviderPriority);
 		result.append(", validationEnabled: ");
 		result.append(validationEnabled);
 		result.append(", metricProviderClassName: ");
 		result.append(metricProviderClassName);
 		result.append(", metricProviderPriority: ");
 		result.append(metricProviderPriority);
 		result.append(", validationDecoratorProviderClassName: ");
 		result.append(validationDecoratorProviderClassName);
 		result.append(", validationDecorators: ");
 		result.append(validationDecorators);
 		result.append(", validationDecoratorProviderPriority: ");
 		result.append(validationDecoratorProviderPriority);
 		result.append(", liveValidationUIFeedback: ");
 		result.append(liveValidationUIFeedback);
 		result.append(", units: ");
 		result.append(units);
 		result.append(", synchronized: ");
 		result.append(synchronized_);
 		result.append(')');
 		return result.toString();
 	}
 
 	String getDomainPackageCapName() {
 		return ((GenEditorGeneratorImpl) getEditorGen()).getDomainModelCapName();
 	}
 
 	private String getEditorPackageName() {
 		return getEditorGen().getEditor().getPackageName();
 	}
 
 	public String getMetaPackageName(ImportAssistant importManager) {
 		return importManager.getImportedName(getDomainDiagramElement().getGenPackage().getQualifiedPackageInterfaceName());
 	}
 	
 	public Map<TypeModelFacet, GenCommonBase> getTypeModelFacet2GenBaseMap() {
 		Map<TypeModelFacet, GenCommonBase> resultMap = new LinkedHashMap<TypeModelFacet, GenCommonBase>();
 		for (GenNode next : getAllNodes()) {
 			TypeModelFacet modelFacet = null;
 			modelFacet = next.getModelFacet();
 			if(modelFacet != null) {
 				resultMap.put(modelFacet, next);					
 			}
 		}
 		for (GenLink next : getLinks()) {
 			if (next.getModelFacet() instanceof TypeLinkModelFacet) {
 				TypeLinkModelFacet modelFacet = (TypeLinkModelFacet) next.getModelFacet();
 				if(modelFacet != null) {
 					resultMap.put(modelFacet, next);
 				}
 			}
 		}
 		return resultMap;
 	}
 
 	public Map<GenClass, GenTopLevelNode> getGenClass2PhantomMap() {
 		LinkedHashMap<GenClass, GenTopLevelNode> genClass2Phantom = new LinkedHashMap<GenClass, GenTopLevelNode>();
 		for (GenTopLevelNode nextTopLevelNode : getTopLevelNodes()) {
 			TypeModelFacet nextModelFacet = nextTopLevelNode.getModelFacet();
 			if (nextModelFacet == null || !nextModelFacet.isPhantomElement()) {
 				continue;
 			}
 			genClass2Phantom.put(nextModelFacet.getMetaClass(), nextTopLevelNode);
 		}
 		return genClass2Phantom;
 	}
 
 	public List<GenLink> getPhantomLinks() {
 		LinkedList<GenLink> phantomLinks = new LinkedList<GenLink>();
 		for (GenLink nextLink : getLinks()) {
 			if (nextLink.getModelFacet() instanceof FeatureLinkModelFacet) {
 				FeatureLinkModelFacet nextModelFacet = (FeatureLinkModelFacet) nextLink.getModelFacet();
 				if (nextModelFacet.getMetaFeature().isContains()) {
 					phantomLinks.add(nextLink);
 				}
 			}
 		}
 		return phantomLinks;
 	}
 	
 	public boolean isSansDomain() {
 		return getDomainDiagramElement() == null;
 	}
 } //GenDiagramImpl
