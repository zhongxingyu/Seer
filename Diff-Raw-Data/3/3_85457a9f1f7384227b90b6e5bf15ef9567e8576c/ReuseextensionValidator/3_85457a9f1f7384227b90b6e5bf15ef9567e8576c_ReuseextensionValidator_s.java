 
 package org.reuseware.coconut.reuseextension.util;
 
 import java.util.Map;
 
 import org.eclipse.emf.common.util.BasicDiagnostic;
 import org.eclipse.emf.common.util.Diagnostic;
 import org.eclipse.emf.common.util.DiagnosticChain;
 import org.eclipse.emf.common.util.ResourceLocator;
 import org.eclipse.emf.common.util.TreeIterator;
 import org.eclipse.emf.ecore.EAttribute;
 import org.eclipse.emf.ecore.EClass;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.ecore.EPackage;
 import org.eclipse.emf.ecore.EReference;
 import org.eclipse.emf.ecore.EcorePackage;
 import org.eclipse.emf.ecore.util.EObjectValidator;
 import org.eclipse.emf.ecore.util.EcoreUtil;
 import org.reuseware.coconut.fracol.CompositionAssociation;
 import org.reuseware.coconut.fracol.DynamicPortType;
 import org.reuseware.coconut.fracol.PortType;
 import org.reuseware.coconut.fracol.StaticPortType;
 import org.reuseware.coconut.reuseextension.AddressablePointDerivationRule;
 import org.reuseware.coconut.reuseextension.AnchorDerivationRule;
 import org.reuseware.coconut.reuseextension.ComponentModelSpecification;
 import org.reuseware.coconut.reuseextension.CompositionAssociation2CompositionLinkBinding;
 import org.reuseware.coconut.reuseextension.CompositionAssociationBinding;
 import org.reuseware.coconut.reuseextension.CompositionLanguageSpecification;
 import org.reuseware.coconut.reuseextension.CompositionLanguageSyntaxSpecification;
 import org.reuseware.coconut.reuseextension.FragmentRole2FragmentBinding;
 import org.reuseware.coconut.reuseextension.FragmentRole2FragmentInstanceBinding;
 import org.reuseware.coconut.reuseextension.FragmentRole2FragmentReferenceBinding;
 import org.reuseware.coconut.reuseextension.FragmentRole2SyntaxBinding;
 import org.reuseware.coconut.reuseextension.FragmentRoleBinding;
 import org.reuseware.coconut.reuseextension.HookDerivationRule;
 import org.reuseware.coconut.reuseextension.PortType2HeterogeneousPortBinding;
 import org.reuseware.coconut.reuseextension.PortType2HomogenousPortBinding;
 import org.reuseware.coconut.reuseextension.PortType2PortBinding;
 import org.reuseware.coconut.reuseextension.PortType2SettingBinding;
 import org.reuseware.coconut.reuseextension.PortType2SyntaxBinding;
 import org.reuseware.coconut.reuseextension.PortTypeBinding;
 import org.reuseware.coconut.reuseextension.PrototypeDerivationRule;
 import org.reuseware.coconut.reuseextension.ReferencePointDerivationRule;
 import org.reuseware.coconut.reuseextension.ReuseExtension;
 import org.reuseware.coconut.reuseextension.ReuseextensionPackage;
 import org.reuseware.coconut.reuseextension.RootElementContext;
 import org.reuseware.coconut.reuseextension.RuleContext;
 import org.reuseware.coconut.reuseextension.SettingDerivationRule;
 import org.reuseware.coconut.reuseextension.SlotDerivationRule;
 import org.reuseware.coconut.reuseextension.SyntaxDerivationRule;
 import org.reuseware.coconut.reuseextension.ValueHookDerivationRule;
 import org.reuseware.coconut.reuseextension.ValuePrototypeDerivationRule;
 import org.reuseware.coconut.reuseextension.VariationPointDerivationRule;
 import org.reuseware.coconut.reuseextension.evaluator.EvaluatorUtil;
 
 /**
  * <!-- begin-user-doc -->
  * The <b>Validator</b> for the model.
  * <!-- end-user-doc -->
  * @see org.reuseware.coconut.reuseextension.ReuseextensionPackage
  * @generated
  */
 public class ReuseextensionValidator extends EObjectValidator
 {
   /**
    * The cached model package
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @generated
    */
   public static final ReuseextensionValidator INSTANCE = new ReuseextensionValidator();
 
   /**
    * A constant for the {@link org.eclipse.emf.common.util.Diagnostic#getSource() source} of diagnostic {@link org.eclipse.emf.common.util.Diagnostic#getCode() codes} from this package.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @see org.eclipse.emf.common.util.Diagnostic#getSource()
    * @see org.eclipse.emf.common.util.Diagnostic#getCode()
    * @generated
    */
   public static final String DIAGNOSTIC_SOURCE = "org.reuseware.coconut.reuseextension";
 
   /**
    * A constant with a fixed name that can be used as the base value for additional hand written constants.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @generated
    */
   private static final int GENERATED_DIAGNOSTIC_CODE_COUNT = 0;
 
   /**
    * A constant with a fixed name that can be used as the base value for additional hand written constants in a derived class.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @generated
    */
   protected static final int DIAGNOSTIC_CODE_COUNT = GENERATED_DIAGNOSTIC_CODE_COUNT;
 
   /**
    * Creates an instance of the switch.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @generated
    */
   public ReuseextensionValidator()
   {
     super();
   }
 
   /**
    * Returns the package of this validator switch.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @generated
    */
   @Override
   protected EPackage getEPackage()
   {
     return ReuseextensionPackage.eINSTANCE;
   }
 
   /**
    * Calls <code>validateXXX</code> for the corresponding classifier of the model.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @generated
    */
   @Override
   protected boolean validate(int classifierID, Object value, DiagnosticChain diagnostics, Map<Object, Object> context)
   {
     switch (classifierID)
     {
       case ReuseextensionPackage.ADDRESSABLE_POINT_DERIVATION_RULE:
         return validateAddressablePointDerivationRule((AddressablePointDerivationRule)value, diagnostics, context);
       case ReuseextensionPackage.ANCHOR_DERIVATION_RULE:
         return validateAnchorDerivationRule((AnchorDerivationRule)value, diagnostics, context);
       case ReuseextensionPackage.COMPONENT_MODEL_SPECIFICATION:
         return validateComponentModelSpecification((ComponentModelSpecification)value, diagnostics, context);
       case ReuseextensionPackage.COMPOSITION_ASSOCIATION2_COMPOSITION_LINK_BINDING:
         return validateCompositionAssociation2CompositionLinkBinding((CompositionAssociation2CompositionLinkBinding)value, diagnostics, context);
       case ReuseextensionPackage.COMPOSITION_ASSOCIATION_BINDING:
         return validateCompositionAssociationBinding((CompositionAssociationBinding)value, diagnostics, context);
       case ReuseextensionPackage.COMPOSITION_LANGUAGE_SPECIFICATION:
         return validateCompositionLanguageSpecification((CompositionLanguageSpecification)value, diagnostics, context);
       case ReuseextensionPackage.COMPOSITION_LANGUAGE_SYNTAX_SPECIFICATION:
         return validateCompositionLanguageSyntaxSpecification((CompositionLanguageSyntaxSpecification)value, diagnostics, context);
       case ReuseextensionPackage.FRAGMENT_ROLE2_FRAGMENT_BINDING:
         return validateFragmentRole2FragmentBinding((FragmentRole2FragmentBinding)value, diagnostics, context);
       case ReuseextensionPackage.FRAGMENT_ROLE2_FRAGMENT_INSTANCE_BINDING:
         return validateFragmentRole2FragmentInstanceBinding((FragmentRole2FragmentInstanceBinding)value, diagnostics, context);
       case ReuseextensionPackage.FRAGMENT_ROLE2_FRAGMENT_REFERENCE_BINDING:
         return validateFragmentRole2FragmentReferenceBinding((FragmentRole2FragmentReferenceBinding)value, diagnostics, context);
       case ReuseextensionPackage.FRAGMENT_ROLE2_SYNTAX_BINDING:
         return validateFragmentRole2SyntaxBinding((FragmentRole2SyntaxBinding)value, diagnostics, context);
       case ReuseextensionPackage.FRAGMENT_ROLE_BINDING:
         return validateFragmentRoleBinding((FragmentRoleBinding)value, diagnostics, context);
       case ReuseextensionPackage.HOOK_DERIVATION_RULE:
         return validateHookDerivationRule((HookDerivationRule)value, diagnostics, context);
       case ReuseextensionPackage.PORT_TYPE2_HETEROGENEOUS_PORT_BINDING:
         return validatePortType2HeterogeneousPortBinding((PortType2HeterogeneousPortBinding)value, diagnostics, context);
       case ReuseextensionPackage.PORT_TYPE2_HOMOGENOUS_PORT_BINDING:
         return validatePortType2HomogenousPortBinding((PortType2HomogenousPortBinding)value, diagnostics, context);
       case ReuseextensionPackage.PORT_TYPE2_PORT_BINDING:
         return validatePortType2PortBinding((PortType2PortBinding)value, diagnostics, context);
       case ReuseextensionPackage.PORT_TYPE2_SETTING_BINDING:
         return validatePortType2SettingBinding((PortType2SettingBinding)value, diagnostics, context);
       case ReuseextensionPackage.PORT_TYPE2_SYNTAX_BINDING:
         return validatePortType2SyntaxBinding((PortType2SyntaxBinding)value, diagnostics, context);
       case ReuseextensionPackage.PORT_TYPE_BINDING:
         return validatePortTypeBinding((PortTypeBinding)value, diagnostics, context);
       case ReuseextensionPackage.PROTOTYPE_DERIVATION_RULE:
         return validatePrototypeDerivationRule((PrototypeDerivationRule)value, diagnostics, context);
       case ReuseextensionPackage.REFERENCE_POINT_DERIVATION_RULE:
         return validateReferencePointDerivationRule((ReferencePointDerivationRule)value, diagnostics, context);
       case ReuseextensionPackage.REUSE_EXTENSION:
         return validateReuseExtension((ReuseExtension)value, diagnostics, context);
       case ReuseextensionPackage.ROOT_ELEMENT_CONTEXT:
         return validateRootElementContext((RootElementContext)value, diagnostics, context);
       case ReuseextensionPackage.RULE_CONTEXT:
         return validateRuleContext((RuleContext)value, diagnostics, context);
       case ReuseextensionPackage.SETTING_DERIVATION_RULE:
         return validateSettingDerivationRule((SettingDerivationRule)value, diagnostics, context);
       case ReuseextensionPackage.SLOT_DERIVATION_RULE:
         return validateSlotDerivationRule((SlotDerivationRule)value, diagnostics, context);
       case ReuseextensionPackage.SYNTAX_DERIVATION_RULE:
         return validateSyntaxDerivationRule((SyntaxDerivationRule)value, diagnostics, context);
       case ReuseextensionPackage.VALUE_HOOK_DERIVATION_RULE:
         return validateValueHookDerivationRule((ValueHookDerivationRule)value, diagnostics, context);
       case ReuseextensionPackage.VALUE_PROTOTYPE_DERIVATION_RULE:
         return validateValuePrototypeDerivationRule((ValuePrototypeDerivationRule)value, diagnostics, context);
       case ReuseextensionPackage.VARIATION_POINT_DERIVATION_RULE:
         return validateVariationPointDerivationRule((VariationPointDerivationRule)value, diagnostics, context);
       default:
         return true;
     }
   }
 
   /**
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @generated
    */
   public boolean validateAddressablePointDerivationRule(AddressablePointDerivationRule addressablePointDerivationRule, DiagnosticChain diagnostics, Map<Object, Object> context)
   {
     if (!validate_NoCircularContainment(addressablePointDerivationRule, diagnostics, context)) return false;
     boolean result = validate_EveryMultiplicityConforms(addressablePointDerivationRule, diagnostics, context);
     if (result || diagnostics != null) result &= validate_EveryDataValueConforms(addressablePointDerivationRule, diagnostics, context);
     if (result || diagnostics != null) result &= validate_EveryReferenceIsContained(addressablePointDerivationRule, diagnostics, context);
     if (result || diagnostics != null) result &= validate_EveryBidirectionalReferenceIsPaired(addressablePointDerivationRule, diagnostics, context);
     if (result || diagnostics != null) result &= validate_EveryProxyResolves(addressablePointDerivationRule, diagnostics, context);
     if (result || diagnostics != null) result &= validate_UniqueID(addressablePointDerivationRule, diagnostics, context);
     if (result || diagnostics != null) result &= validate_EveryKeyUnique(addressablePointDerivationRule, diagnostics, context);
     if (result || diagnostics != null) result &= validate_EveryMapEntryUnique(addressablePointDerivationRule, diagnostics, context);
     if (result || diagnostics != null) result &= validateRuleContext_AllExpressions(addressablePointDerivationRule, diagnostics, context);
     return result;
   }
 
   /**
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @generated
    */
   public boolean validateAnchorDerivationRule(AnchorDerivationRule anchorDerivationRule, DiagnosticChain diagnostics, Map<Object, Object> context)
   {
     if (!validate_NoCircularContainment(anchorDerivationRule, diagnostics, context)) return false;
     boolean result = validate_EveryMultiplicityConforms(anchorDerivationRule, diagnostics, context);
     if (result || diagnostics != null) result &= validate_EveryDataValueConforms(anchorDerivationRule, diagnostics, context);
     if (result || diagnostics != null) result &= validate_EveryReferenceIsContained(anchorDerivationRule, diagnostics, context);
     if (result || diagnostics != null) result &= validate_EveryBidirectionalReferenceIsPaired(anchorDerivationRule, diagnostics, context);
     if (result || diagnostics != null) result &= validate_EveryProxyResolves(anchorDerivationRule, diagnostics, context);
     if (result || diagnostics != null) result &= validate_UniqueID(anchorDerivationRule, diagnostics, context);
     if (result || diagnostics != null) result &= validate_EveryKeyUnique(anchorDerivationRule, diagnostics, context);
     if (result || diagnostics != null) result &= validate_EveryMapEntryUnique(anchorDerivationRule, diagnostics, context);
     if (result || diagnostics != null) result &= validateRuleContext_AllExpressions(anchorDerivationRule, diagnostics, context);
     if (result || diagnostics != null) result &= validateAnchorDerivationRule_NotDefinedForContainmentReference(anchorDerivationRule, diagnostics, context);
     return result;
   }
 
 	/**
 	 * Validates the NotDefinedForContainmentReference constraint of '
 	 * <em>Anchor Derivation Rule</em>'. <!-- begin-user-doc --> <!--
 	 * end-user-doc -->
 	 * 
 	 * @generated NOT
 	 */
 	public boolean validateAnchorDerivationRule_NotDefinedForContainmentReference(
 			AnchorDerivationRule anchorDerivationRule,
 			DiagnosticChain diagnostics, Map<Object, Object> context) {
 		
 		if (anchorDerivationRule.getEBoundFeature() instanceof EReference
 				&& ((EReference) anchorDerivationRule.getEBoundFeature()).isContainment()) {
 			addError(
 					diagnostics,
 					"Anchors cannot be defined for containment references (use prototypes instead)",
 					anchorDerivationRule);
 			return false;
 		}
 		if (anchorDerivationRule.getEBoundFeature() instanceof EAttribute) {
 			addError(
 					diagnostics,
 					"Anchors cannot be defined for attributes (use value prototypes instead)",
 					anchorDerivationRule);
 			return false;
 		}
 		return true;
 	}
 
   /**
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @generated
    */
   public boolean validateComponentModelSpecification(ComponentModelSpecification componentModelSpecification, DiagnosticChain diagnostics, Map<Object, Object> context)
   {
     return validate_EveryDefaultConstraint(componentModelSpecification, diagnostics, context);
   }
 
   /**
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @generated
    */
   public boolean validateCompositionAssociation2CompositionLinkBinding(CompositionAssociation2CompositionLinkBinding compositionAssociation2CompositionLinkBinding, DiagnosticChain diagnostics, Map<Object, Object> context)
   {
     if (!validate_NoCircularContainment(compositionAssociation2CompositionLinkBinding, diagnostics, context)) return false;
     boolean result = validate_EveryMultiplicityConforms(compositionAssociation2CompositionLinkBinding, diagnostics, context);
     if (result || diagnostics != null) result &= validate_EveryDataValueConforms(compositionAssociation2CompositionLinkBinding, diagnostics, context);
     if (result || diagnostics != null) result &= validate_EveryReferenceIsContained(compositionAssociation2CompositionLinkBinding, diagnostics, context);
     if (result || diagnostics != null) result &= validate_EveryBidirectionalReferenceIsPaired(compositionAssociation2CompositionLinkBinding, diagnostics, context);
     if (result || diagnostics != null) result &= validate_EveryProxyResolves(compositionAssociation2CompositionLinkBinding, diagnostics, context);
     if (result || diagnostics != null) result &= validate_UniqueID(compositionAssociation2CompositionLinkBinding, diagnostics, context);
     if (result || diagnostics != null) result &= validate_EveryKeyUnique(compositionAssociation2CompositionLinkBinding, diagnostics, context);
     if (result || diagnostics != null) result &= validate_EveryMapEntryUnique(compositionAssociation2CompositionLinkBinding, diagnostics, context);
     if (result || diagnostics != null) result &= validateRuleContext_AllExpressions(compositionAssociation2CompositionLinkBinding, diagnostics, context);
     if (result || diagnostics != null) result &= validateCompositionAssociation2CompositionLinkBinding_NameExpressionsSet(compositionAssociation2CompositionLinkBinding, diagnostics, context);
     return result;
   }
 
 	/**
 	 * Validates the NameExpressionsSet constraint of '
 	 * <em>Composition Association2 Composition Link Binding</em>'. <!--
 	 * begin-user-doc --> <!-- end-user-doc -->
 	 * 
 	 * @generated NOT
 	 */
 	public boolean validateCompositionAssociation2CompositionLinkBinding_NameExpressionsSet(
 			CompositionAssociation2CompositionLinkBinding compositionAssociation2CompositionLinkBinding,
 			DiagnosticChain diagnostics, Map<Object, Object> context) {	
 		boolean result = true;
 		
 		CompositionAssociation association = compositionAssociation2CompositionLinkBinding.getCompositionAssociation();
 		if (association == null) {
 			return true;
 		}
 		PortType portType1 = association.getEnd1();
 		PortType portType2 = association.getEnd2();
 		if (portType1 == null || portType2 == null) {
 			return true;
 		}
 		String nameExpression1 = compositionAssociation2CompositionLinkBinding.getPortInstance1NameExpression();
 		String nameExpression2 = compositionAssociation2CompositionLinkBinding.getPortInstance2NameExpression();
 			
 		if (portType1 instanceof StaticPortType && nameExpression1 != null) {
 			addError(
 				diagnostics, 
 				"Ports of type '" + portType1.getName() + "' have the fixed name '" + portType1.getName() + "'",
 				compositionAssociation2CompositionLinkBinding);
 			result = false;
 		}
 		if (portType2 instanceof StaticPortType && nameExpression2 != null) {
 			addError(
 				diagnostics, 
 				"Ports of type '" + portType2.getName() + "' have the fixed name '" + portType2.getName() + "'",
 				compositionAssociation2CompositionLinkBinding);
 			result = false;
 		}
 		if (portType1 instanceof DynamicPortType && nameExpression1 == null) {
 			addError(
 				diagnostics, 
 				"Ports of type '" + portType1.getName() + "' require a port expression",
 				compositionAssociation2CompositionLinkBinding);
 			result = false;
 		}
 		if (portType2 instanceof DynamicPortType && nameExpression2 == null) {
 			addError(
 				diagnostics, 
 				"Ports of type '" + portType2.getName() + "' require a port expression",
 				compositionAssociation2CompositionLinkBinding);
 			result = false;
 		}
 		return result;
 	}
 
   /**
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @generated
    */
   public boolean validateCompositionAssociationBinding(CompositionAssociationBinding compositionAssociationBinding, DiagnosticChain diagnostics, Map<Object, Object> context)
   {
     if (!validate_NoCircularContainment(compositionAssociationBinding, diagnostics, context)) return false;
     boolean result = validate_EveryMultiplicityConforms(compositionAssociationBinding, diagnostics, context);
     if (result || diagnostics != null) result &= validate_EveryDataValueConforms(compositionAssociationBinding, diagnostics, context);
     if (result || diagnostics != null) result &= validate_EveryReferenceIsContained(compositionAssociationBinding, diagnostics, context);
     if (result || diagnostics != null) result &= validate_EveryBidirectionalReferenceIsPaired(compositionAssociationBinding, diagnostics, context);
     if (result || diagnostics != null) result &= validate_EveryProxyResolves(compositionAssociationBinding, diagnostics, context);
     if (result || diagnostics != null) result &= validate_UniqueID(compositionAssociationBinding, diagnostics, context);
     if (result || diagnostics != null) result &= validate_EveryKeyUnique(compositionAssociationBinding, diagnostics, context);
     if (result || diagnostics != null) result &= validate_EveryMapEntryUnique(compositionAssociationBinding, diagnostics, context);
     if (result || diagnostics != null) result &= validateRuleContext_AllExpressions(compositionAssociationBinding, diagnostics, context);
     return result;
   }
 
   /**
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @generated
    */
   public boolean validateCompositionLanguageSpecification(CompositionLanguageSpecification compositionLanguageSpecification, DiagnosticChain diagnostics, Map<Object, Object> context)
   {
     return validate_EveryDefaultConstraint(compositionLanguageSpecification, diagnostics, context);
   }
 
   /**
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @generated
    */
   public boolean validateCompositionLanguageSyntaxSpecification(CompositionLanguageSyntaxSpecification compositionLanguageSyntaxSpecification, DiagnosticChain diagnostics, Map<Object, Object> context)
   {
     return validate_EveryDefaultConstraint(compositionLanguageSyntaxSpecification, diagnostics, context);
   }
 
   /**
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @generated
    */
   public boolean validateFragmentRole2FragmentBinding(FragmentRole2FragmentBinding fragmentRole2FragmentBinding, DiagnosticChain diagnostics, Map<Object, Object> context)
   {
     return validate_EveryDefaultConstraint(fragmentRole2FragmentBinding, diagnostics, context);
   }
 
   /**
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @generated
    */
   public boolean validateFragmentRole2FragmentInstanceBinding(FragmentRole2FragmentInstanceBinding fragmentRole2FragmentInstanceBinding, DiagnosticChain diagnostics, Map<Object, Object> context)
   {
     if (!validate_NoCircularContainment(fragmentRole2FragmentInstanceBinding, diagnostics, context)) return false;
     boolean result = validate_EveryMultiplicityConforms(fragmentRole2FragmentInstanceBinding, diagnostics, context);
     if (result || diagnostics != null) result &= validate_EveryDataValueConforms(fragmentRole2FragmentInstanceBinding, diagnostics, context);
     if (result || diagnostics != null) result &= validate_EveryReferenceIsContained(fragmentRole2FragmentInstanceBinding, diagnostics, context);
     if (result || diagnostics != null) result &= validate_EveryBidirectionalReferenceIsPaired(fragmentRole2FragmentInstanceBinding, diagnostics, context);
     if (result || diagnostics != null) result &= validate_EveryProxyResolves(fragmentRole2FragmentInstanceBinding, diagnostics, context);
     if (result || diagnostics != null) result &= validate_UniqueID(fragmentRole2FragmentInstanceBinding, diagnostics, context);
     if (result || diagnostics != null) result &= validate_EveryKeyUnique(fragmentRole2FragmentInstanceBinding, diagnostics, context);
     if (result || diagnostics != null) result &= validate_EveryMapEntryUnique(fragmentRole2FragmentInstanceBinding, diagnostics, context);
     if (result || diagnostics != null) result &= validateRuleContext_AllExpressions(fragmentRole2FragmentInstanceBinding, diagnostics, context);
     return result;
   }
 
   /**
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @generated
    */
   public boolean validateFragmentRole2FragmentReferenceBinding(FragmentRole2FragmentReferenceBinding fragmentRole2FragmentReferenceBinding, DiagnosticChain diagnostics, Map<Object, Object> context)
   {
     if (!validate_NoCircularContainment(fragmentRole2FragmentReferenceBinding, diagnostics, context)) return false;
     boolean result = validate_EveryMultiplicityConforms(fragmentRole2FragmentReferenceBinding, diagnostics, context);
     if (result || diagnostics != null) result &= validate_EveryDataValueConforms(fragmentRole2FragmentReferenceBinding, diagnostics, context);
     if (result || diagnostics != null) result &= validate_EveryReferenceIsContained(fragmentRole2FragmentReferenceBinding, diagnostics, context);
     if (result || diagnostics != null) result &= validate_EveryBidirectionalReferenceIsPaired(fragmentRole2FragmentReferenceBinding, diagnostics, context);
     if (result || diagnostics != null) result &= validate_EveryProxyResolves(fragmentRole2FragmentReferenceBinding, diagnostics, context);
     if (result || diagnostics != null) result &= validate_UniqueID(fragmentRole2FragmentReferenceBinding, diagnostics, context);
     if (result || diagnostics != null) result &= validate_EveryKeyUnique(fragmentRole2FragmentReferenceBinding, diagnostics, context);
     if (result || diagnostics != null) result &= validate_EveryMapEntryUnique(fragmentRole2FragmentReferenceBinding, diagnostics, context);
     if (result || diagnostics != null) result &= validateRuleContext_AllExpressions(fragmentRole2FragmentReferenceBinding, diagnostics, context);
     return result;
   }
 
   /**
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @generated
    */
   public boolean validateFragmentRole2SyntaxBinding(FragmentRole2SyntaxBinding fragmentRole2SyntaxBinding, DiagnosticChain diagnostics, Map<Object, Object> context)
   {
     if (!validate_NoCircularContainment(fragmentRole2SyntaxBinding, diagnostics, context)) return false;
     boolean result = validate_EveryMultiplicityConforms(fragmentRole2SyntaxBinding, diagnostics, context);
     if (result || diagnostics != null) result &= validate_EveryDataValueConforms(fragmentRole2SyntaxBinding, diagnostics, context);
     if (result || diagnostics != null) result &= validate_EveryReferenceIsContained(fragmentRole2SyntaxBinding, diagnostics, context);
     if (result || diagnostics != null) result &= validate_EveryBidirectionalReferenceIsPaired(fragmentRole2SyntaxBinding, diagnostics, context);
     if (result || diagnostics != null) result &= validate_EveryProxyResolves(fragmentRole2SyntaxBinding, diagnostics, context);
     if (result || diagnostics != null) result &= validate_UniqueID(fragmentRole2SyntaxBinding, diagnostics, context);
     if (result || diagnostics != null) result &= validate_EveryKeyUnique(fragmentRole2SyntaxBinding, diagnostics, context);
     if (result || diagnostics != null) result &= validate_EveryMapEntryUnique(fragmentRole2SyntaxBinding, diagnostics, context);
     if (result || diagnostics != null) result &= validateSyntaxDerivationRule_AllExpressions(fragmentRole2SyntaxBinding, diagnostics, context);
     return result;
   }
 
   /**
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @generated
    */
   public boolean validateFragmentRoleBinding(FragmentRoleBinding fragmentRoleBinding, DiagnosticChain diagnostics, Map<Object, Object> context)
   {
     return validate_EveryDefaultConstraint(fragmentRoleBinding, diagnostics, context);
   }
 
   /**
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @generated
    */
   public boolean validateHookDerivationRule(HookDerivationRule hookDerivationRule, DiagnosticChain diagnostics, Map<Object, Object> context)
   {
     if (!validate_NoCircularContainment(hookDerivationRule, diagnostics, context)) return false;
     boolean result = validate_EveryMultiplicityConforms(hookDerivationRule, diagnostics, context);
     if (result || diagnostics != null) result &= validate_EveryDataValueConforms(hookDerivationRule, diagnostics, context);
     if (result || diagnostics != null) result &= validate_EveryReferenceIsContained(hookDerivationRule, diagnostics, context);
     if (result || diagnostics != null) result &= validate_EveryBidirectionalReferenceIsPaired(hookDerivationRule, diagnostics, context);
     if (result || diagnostics != null) result &= validate_EveryProxyResolves(hookDerivationRule, diagnostics, context);
     if (result || diagnostics != null) result &= validate_UniqueID(hookDerivationRule, diagnostics, context);
     if (result || diagnostics != null) result &= validate_EveryKeyUnique(hookDerivationRule, diagnostics, context);
     if (result || diagnostics != null) result &= validate_EveryMapEntryUnique(hookDerivationRule, diagnostics, context);
     if (result || diagnostics != null) result &= validateRuleContext_AllExpressions(hookDerivationRule, diagnostics, context);
     if (result || diagnostics != null) result &= validateHookDerivationRule_NotDefinedForCrossReference(hookDerivationRule, diagnostics, context);
     return result;
   }
 
 	/**
 	 * Validates the NotDefinedForCrossReference constraint of '
 	 * <em>Hook Derivation Rule</em>'. <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * 
 	 * @generated NOT
 	 */
 	public boolean validateHookDerivationRule_NotDefinedForCrossReference(
 			HookDerivationRule hookDerivationRule, DiagnosticChain diagnostics,
 			Map<Object, Object> context) {
 		
 		if (hookDerivationRule.getEBoundFeature().eIsProxy()) {
 			//an error is already reported that the feature was not found at all
 			return true;
 		}
 		
 		if (hookDerivationRule.getEBoundFeature() instanceof EReference
 				&& !((EReference) hookDerivationRule.getEBoundFeature()).isContainment()) {
 			addError(
 					diagnostics,
 					"Hooks cannot be defined for cross-references (use slots instead)",
 					hookDerivationRule);
 			return false;
 		}
 		if (hookDerivationRule.getEBoundFeature() instanceof EAttribute) {
 			addError(
 					diagnostics,
 					"Hooks cannot be defined for attributes (use value hooks instead)",
 					hookDerivationRule);
 			return false;
 		}
 		return true;
 	}
 
   /**
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @generated
    */
   public boolean validatePortType2HeterogeneousPortBinding(PortType2HeterogeneousPortBinding portType2HeterogeneousPortBinding, DiagnosticChain diagnostics, Map<Object, Object> context)
   {
     return validate_EveryDefaultConstraint(portType2HeterogeneousPortBinding, diagnostics, context);
   }
 
   /**
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @generated
    */
   public boolean validatePortType2HomogenousPortBinding(PortType2HomogenousPortBinding portType2HomogenousPortBinding, DiagnosticChain diagnostics, Map<Object, Object> context)
   {
     return validate_EveryDefaultConstraint(portType2HomogenousPortBinding, diagnostics, context);
   }
 
   /**
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @generated
    */
   public boolean validatePortType2PortBinding(PortType2PortBinding portType2PortBinding, DiagnosticChain diagnostics, Map<Object, Object> context)
   {
     return validate_EveryDefaultConstraint(portType2PortBinding, diagnostics, context);
   }
 
   /**
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @generated
    */
   public boolean validatePortType2SettingBinding(PortType2SettingBinding portType2SettingBinding, DiagnosticChain diagnostics, Map<Object, Object> context)
   {
     return validate_EveryDefaultConstraint(portType2SettingBinding, diagnostics, context);
   }
 
   /**
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @generated
    */
   public boolean validatePortType2SyntaxBinding(PortType2SyntaxBinding portType2SyntaxBinding, DiagnosticChain diagnostics, Map<Object, Object> context)
   {
     if (!validate_NoCircularContainment(portType2SyntaxBinding, diagnostics, context)) return false;
     boolean result = validate_EveryMultiplicityConforms(portType2SyntaxBinding, diagnostics, context);
     if (result || diagnostics != null) result &= validate_EveryDataValueConforms(portType2SyntaxBinding, diagnostics, context);
     if (result || diagnostics != null) result &= validate_EveryReferenceIsContained(portType2SyntaxBinding, diagnostics, context);
     if (result || diagnostics != null) result &= validate_EveryBidirectionalReferenceIsPaired(portType2SyntaxBinding, diagnostics, context);
     if (result || diagnostics != null) result &= validate_EveryProxyResolves(portType2SyntaxBinding, diagnostics, context);
     if (result || diagnostics != null) result &= validate_UniqueID(portType2SyntaxBinding, diagnostics, context);
     if (result || diagnostics != null) result &= validate_EveryKeyUnique(portType2SyntaxBinding, diagnostics, context);
     if (result || diagnostics != null) result &= validate_EveryMapEntryUnique(portType2SyntaxBinding, diagnostics, context);
     if (result || diagnostics != null) result &= validateSyntaxDerivationRule_AllExpressions(portType2SyntaxBinding, diagnostics, context);
     return result;
   }
 
   /**
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @generated
    */
   public boolean validatePortTypeBinding(PortTypeBinding portTypeBinding, DiagnosticChain diagnostics, Map<Object, Object> context)
   {
     return validate_EveryDefaultConstraint(portTypeBinding, diagnostics, context);
   }
 
   /**
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @generated
    */
   public boolean validatePrototypeDerivationRule(PrototypeDerivationRule prototypeDerivationRule, DiagnosticChain diagnostics, Map<Object, Object> context)
   {
     if (!validate_NoCircularContainment(prototypeDerivationRule, diagnostics, context)) return false;
     boolean result = validate_EveryMultiplicityConforms(prototypeDerivationRule, diagnostics, context);
     if (result || diagnostics != null) result &= validate_EveryDataValueConforms(prototypeDerivationRule, diagnostics, context);
     if (result || diagnostics != null) result &= validate_EveryReferenceIsContained(prototypeDerivationRule, diagnostics, context);
     if (result || diagnostics != null) result &= validate_EveryBidirectionalReferenceIsPaired(prototypeDerivationRule, diagnostics, context);
     if (result || diagnostics != null) result &= validate_EveryProxyResolves(prototypeDerivationRule, diagnostics, context);
     if (result || diagnostics != null) result &= validate_UniqueID(prototypeDerivationRule, diagnostics, context);
     if (result || diagnostics != null) result &= validate_EveryKeyUnique(prototypeDerivationRule, diagnostics, context);
     if (result || diagnostics != null) result &= validate_EveryMapEntryUnique(prototypeDerivationRule, diagnostics, context);
     if (result || diagnostics != null) result &= validateRuleContext_AllExpressions(prototypeDerivationRule, diagnostics, context);
     if (result || diagnostics != null) result &= validatePrototypeDerivationRule_NotDefinedForCrossReference(prototypeDerivationRule, diagnostics, context);
     return result;
   }
 
 	/**
 	 * Validates the NotDefinedForCrossReference constraint of '
 	 * <em>Prototype Derivation Rule</em>'. <!-- begin-user-doc --> <!--
 	 * end-user-doc -->
 	 * 
 	 * @generated NOT
 	 */
 	public boolean validatePrototypeDerivationRule_NotDefinedForCrossReference(
 			PrototypeDerivationRule prototypeDerivationRule,
 			DiagnosticChain diagnostics, Map<Object, Object> context) {
 		
 		if (prototypeDerivationRule.getEBoundFeature() instanceof EReference
 				&& !((EReference) prototypeDerivationRule.getEBoundFeature()).isContainment()) {
 			addError(
 					diagnostics,
 					"Prototypes cannot be defined for cross-references (use anchors instead)",
 					prototypeDerivationRule);
 			return false;
 		}
 		if (prototypeDerivationRule.getEBoundFeature() instanceof EAttribute) {
 			addError(
 					diagnostics,
 					"Prototypes cannot be defined for attributes (use value prototypes instead)",
 					prototypeDerivationRule);
 			return false;
 		}
 		return true;
 	}
 
   /**
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @generated
    */
   public boolean validateReferencePointDerivationRule(ReferencePointDerivationRule referencePointDerivationRule, DiagnosticChain diagnostics, Map<Object, Object> context)
   {
     if (!validate_NoCircularContainment(referencePointDerivationRule, diagnostics, context)) return false;
     boolean result = validate_EveryMultiplicityConforms(referencePointDerivationRule, diagnostics, context);
     if (result || diagnostics != null) result &= validate_EveryDataValueConforms(referencePointDerivationRule, diagnostics, context);
     if (result || diagnostics != null) result &= validate_EveryReferenceIsContained(referencePointDerivationRule, diagnostics, context);
     if (result || diagnostics != null) result &= validate_EveryBidirectionalReferenceIsPaired(referencePointDerivationRule, diagnostics, context);
     if (result || diagnostics != null) result &= validate_EveryProxyResolves(referencePointDerivationRule, diagnostics, context);
     if (result || diagnostics != null) result &= validate_UniqueID(referencePointDerivationRule, diagnostics, context);
     if (result || diagnostics != null) result &= validate_EveryKeyUnique(referencePointDerivationRule, diagnostics, context);
     if (result || diagnostics != null) result &= validate_EveryMapEntryUnique(referencePointDerivationRule, diagnostics, context);
     if (result || diagnostics != null) result &= validateRuleContext_AllExpressions(referencePointDerivationRule, diagnostics, context);
     return result;
   }
 
   /**
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @generated
    */
   public boolean validateReuseExtension(ReuseExtension reuseExtension, DiagnosticChain diagnostics, Map<Object, Object> context)
   {
     return validate_EveryDefaultConstraint(reuseExtension, diagnostics, context);
   }
 
 	/**
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @generated
    */
   public boolean validateRootElementContext(RootElementContext rootElementContext, DiagnosticChain diagnostics, Map<Object, Object> context)
   {
     if (!validate_NoCircularContainment(rootElementContext, diagnostics, context)) return false;
     boolean result = validate_EveryMultiplicityConforms(rootElementContext, diagnostics, context);
     if (result || diagnostics != null) result &= validate_EveryDataValueConforms(rootElementContext, diagnostics, context);
     if (result || diagnostics != null) result &= validate_EveryReferenceIsContained(rootElementContext, diagnostics, context);
     if (result || diagnostics != null) result &= validate_EveryBidirectionalReferenceIsPaired(rootElementContext, diagnostics, context);
     if (result || diagnostics != null) result &= validate_EveryProxyResolves(rootElementContext, diagnostics, context);
     if (result || diagnostics != null) result &= validate_UniqueID(rootElementContext, diagnostics, context);
     if (result || diagnostics != null) result &= validate_EveryKeyUnique(rootElementContext, diagnostics, context);
     if (result || diagnostics != null) result &= validate_EveryMapEntryUnique(rootElementContext, diagnostics, context);
     if (result || diagnostics != null) result &= validateRuleContext_AllExpressions(rootElementContext, diagnostics, context);
     return result;
   }
 
   /**
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @generated
    */
   public boolean validateRuleContext(RuleContext ruleContext, DiagnosticChain diagnostics, Map<Object, Object> context)
   {
     if (!validate_NoCircularContainment(ruleContext, diagnostics, context)) return false;
     boolean result = validate_EveryMultiplicityConforms(ruleContext, diagnostics, context);
     if (result || diagnostics != null) result &= validate_EveryDataValueConforms(ruleContext, diagnostics, context);
     if (result || diagnostics != null) result &= validate_EveryReferenceIsContained(ruleContext, diagnostics, context);
     if (result || diagnostics != null) result &= validate_EveryBidirectionalReferenceIsPaired(ruleContext, diagnostics, context);
     if (result || diagnostics != null) result &= validate_EveryProxyResolves(ruleContext, diagnostics, context);
     if (result || diagnostics != null) result &= validate_UniqueID(ruleContext, diagnostics, context);
     if (result || diagnostics != null) result &= validate_EveryKeyUnique(ruleContext, diagnostics, context);
     if (result || diagnostics != null) result &= validate_EveryMapEntryUnique(ruleContext, diagnostics, context);
     if (result || diagnostics != null) result &= validateRuleContext_AllExpressions(ruleContext, diagnostics, context);
     return result;
   }
 
 	/**
 	 * Validates the AllExpressions constraint of '<em>Rule Context</em>'. <!--
 	 * begin-user-doc --> <!-- end-user-doc -->
 	 * 
 	 * @generated NOT
 	 */
 	public boolean validateRuleContext_AllExpressions(RuleContext ruleContext,
 			DiagnosticChain diagnostics, Map<Object, Object> context) {
 		boolean result = true;
 		
 		ReuseExtension rex = (ReuseExtension) EcoreUtil.getRootContainer(ruleContext);
 
 		if (ruleContext.getEBoundClass() != null) {
 			for(EAttribute attribute : ruleContext.eClass().getEAllAttributes()) {
 				if (attribute.getName().endsWith("Expression")) {
 					EClass eClass = ruleContext.getEBoundClass();
 					if (ruleContext instanceof CompositionAssociation2CompositionLinkBinding) {
 						CompositionAssociation2CompositionLinkBinding binding =
 							(CompositionAssociation2CompositionLinkBinding) ruleContext;
 						if (attribute.getName().contains("Instance1") && binding.getForEach1Expression() != null) {
 							eClass = EvaluatorUtil.getResultType(
 									binding.getEBoundClass(), binding.getForEach1Expression(), rex.getParameters());
 						}
 						else if (attribute.getName().contains("Instance2") && binding.getForEach2Expression() != null) {
 							eClass = EvaluatorUtil.getResultType(
 									binding.getEBoundClass(), binding.getForEach2Expression(), rex.getParameters());
 						}
 					}
 					if (ruleContext instanceof AddressablePointDerivationRule) {
 						AddressablePointDerivationRule derivationRule =
 							(AddressablePointDerivationRule) ruleContext;
 						if (attribute.getName().contains("NameExpression") && derivationRule.getForEachExpression() != null) {
 							eClass = EvaluatorUtil.getResultType(
 									derivationRule.getEBoundClass(), derivationRule.getForEachExpression(), rex.getParameters());
 						}
 					}
 					String expression = (String) ruleContext.eGet(attribute);
 					String errorMessage = EvaluatorUtil.validateExpression(
 							eClass, expression, rex.getParameters());
 					if (errorMessage != null) {
 						addError(diagnostics, errorMessage, ruleContext);
 					}
 					result = false;
 				}
 			}
 		}
 		
 		//additional expressions in the model that are evaluated in the root element context
 		if (ruleContext instanceof RootElementContext && ruleContext.eResource() != null) {
 			TreeIterator<EObject> i = EcoreUtil.getAllContents(ruleContext.eResource(), true);
 			//all expressions of ReuseExtension, FragmentRole2FragmentBinding, FragmentRole2SyntaxBinding
 			while(i.hasNext()) {
 				EObject next = i.next();
 				if (next instanceof ReuseExtension || next instanceof FragmentRole2FragmentBinding || next instanceof FragmentRole2SyntaxBinding) {
 					for(EAttribute attribute : next.eClass().getEAllAttributes()) {
 						if (attribute.getName().endsWith("Expression")) {
 							String expression = (String) next.eGet(attribute);
 							String errorMessage = EvaluatorUtil.validateExpression(
 									ruleContext.getEBoundClass(), expression, rex.getParameters());
 							if (errorMessage != null) {
 								addError(diagnostics, errorMessage, next);
 								result = false;
 							}
 						}
 					}
 				}
 			}
 		}
 		
 		//SettingDerivationRule
 		if (ruleContext instanceof FragmentRole2FragmentInstanceBinding) {
 			String errorMessage = null;
 			FragmentRole2FragmentInstanceBinding fragmenRoleBinding = (FragmentRole2FragmentInstanceBinding) ruleContext;
 			for(PortType2SettingBinding portTypBinding : fragmenRoleBinding.getPortTypeBindings()) {
 				for(SettingDerivationRule settingDerivationRule : portTypBinding.getDerivationRules()) {
 					String propertyExpression = settingDerivationRule.getPropertyExpression();
 					errorMessage = EvaluatorUtil.validateExpression(
 							ruleContext.getEBoundClass(), propertyExpression, rex.getParameters());
 					if (errorMessage != null) {
 						addError(diagnostics, errorMessage, settingDerivationRule);
 						result = false;
 					}
 					String valueExpression = settingDerivationRule.getValueExpression();
 					errorMessage = EvaluatorUtil.validateExpression(
 							ruleContext.getEBoundClass(), valueExpression, rex.getParameters());
 					if (errorMessage != null) {
 						addError(diagnostics, errorMessage, settingDerivationRule);
 						result = false;
 					}
 				}
 			}
 		}
 		
 		return result;
 	}
 
   /**
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @generated
    */
   public boolean validateSettingDerivationRule(SettingDerivationRule settingDerivationRule, DiagnosticChain diagnostics, Map<Object, Object> context)
   {
     return validate_EveryDefaultConstraint(settingDerivationRule, diagnostics, context);
   }
 
   /**
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @generated
    */
   public boolean validateSlotDerivationRule(SlotDerivationRule slotDerivationRule, DiagnosticChain diagnostics, Map<Object, Object> context)
   {
     if (!validate_NoCircularContainment(slotDerivationRule, diagnostics, context)) return false;
     boolean result = validate_EveryMultiplicityConforms(slotDerivationRule, diagnostics, context);
     if (result || diagnostics != null) result &= validate_EveryDataValueConforms(slotDerivationRule, diagnostics, context);
     if (result || diagnostics != null) result &= validate_EveryReferenceIsContained(slotDerivationRule, diagnostics, context);
     if (result || diagnostics != null) result &= validate_EveryBidirectionalReferenceIsPaired(slotDerivationRule, diagnostics, context);
     if (result || diagnostics != null) result &= validate_EveryProxyResolves(slotDerivationRule, diagnostics, context);
     if (result || diagnostics != null) result &= validate_UniqueID(slotDerivationRule, diagnostics, context);
     if (result || diagnostics != null) result &= validate_EveryKeyUnique(slotDerivationRule, diagnostics, context);
     if (result || diagnostics != null) result &= validate_EveryMapEntryUnique(slotDerivationRule, diagnostics, context);
     if (result || diagnostics != null) result &= validateRuleContext_AllExpressions(slotDerivationRule, diagnostics, context);
     if (result || diagnostics != null) result &= validateSlotDerivationRule_NotDefinedForContainmentReference(slotDerivationRule, diagnostics, context);
     return result;
   }
 
 	/**
 	 * Validates the NotDefinedForContainmentReference constraint of '
 	 * <em>Slot Derivation Rule</em>'. <!-- begin-user-doc --> <!-- end-user-doc
 	 * -->
 	 * 
 	 * @generated NOT
 	 */
 	public boolean validateSlotDerivationRule_NotDefinedForContainmentReference(
 			SlotDerivationRule slotDerivationRule, DiagnosticChain diagnostics,
 			Map<Object, Object> context) {
 
 		if (slotDerivationRule.getEBoundFeature() instanceof EReference
 				&& ((EReference) slotDerivationRule.getEBoundFeature()).isContainment()) {
 			addError(diagnostics,
 					"Slots cannot be defined for containment references (use hooks instead)",
 					slotDerivationRule);
 			return false;
 		}
 		if (slotDerivationRule.getEBoundFeature() instanceof EAttribute)  {
 			addError(diagnostics,
 					"Slots cannot be defined for attributes (use value hooks instead)",
 					slotDerivationRule);
 			return false;
 		}
 		return true;
 	}
 
   /**
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @generated
    */
   public boolean validateSyntaxDerivationRule(SyntaxDerivationRule syntaxDerivationRule, DiagnosticChain diagnostics, Map<Object, Object> context)
   {
     if (!validate_NoCircularContainment(syntaxDerivationRule, diagnostics, context)) return false;
     boolean result = validate_EveryMultiplicityConforms(syntaxDerivationRule, diagnostics, context);
     if (result || diagnostics != null) result &= validate_EveryDataValueConforms(syntaxDerivationRule, diagnostics, context);
     if (result || diagnostics != null) result &= validate_EveryReferenceIsContained(syntaxDerivationRule, diagnostics, context);
     if (result || diagnostics != null) result &= validate_EveryBidirectionalReferenceIsPaired(syntaxDerivationRule, diagnostics, context);
     if (result || diagnostics != null) result &= validate_EveryProxyResolves(syntaxDerivationRule, diagnostics, context);
     if (result || diagnostics != null) result &= validate_UniqueID(syntaxDerivationRule, diagnostics, context);
     if (result || diagnostics != null) result &= validate_EveryKeyUnique(syntaxDerivationRule, diagnostics, context);
     if (result || diagnostics != null) result &= validate_EveryMapEntryUnique(syntaxDerivationRule, diagnostics, context);
     if (result || diagnostics != null) result &= validateSyntaxDerivationRule_AllExpressions(syntaxDerivationRule, diagnostics, context);
     return result;
   }
 
   /**
    * Validates the AllExpressions constraint of '<em>Syntax Derivation Rule</em>'.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @generated NOT
    */
 	public boolean validateSyntaxDerivationRule_AllExpressions(
 			SyntaxDerivationRule syntaxDerivationRule,
 			DiagnosticChain diagnostics, Map<Object, Object> context) {
 		
 		boolean result = true;
 		
 		ReuseExtension rex = (ReuseExtension) EcoreUtil.getRootContainer(syntaxDerivationRule);
 		
 		for(EAttribute attribute : syntaxDerivationRule.eClass().getEAllAttributes()) {
 			if (attribute.getName().endsWith("Expression")) {
 				String expression = (String) syntaxDerivationRule.eGet(attribute);
 				String errorMessage = EvaluatorUtil.validateExpression(
 						EcorePackage.eINSTANCE.getEObject(), expression, rex.getParameters());
 				if (errorMessage != null) {
 					addError(diagnostics, errorMessage, syntaxDerivationRule);
 					result = false;
 				}
 			}
 		}
 
 		return result;
 	}
 
   /**
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @generated
    */
   public boolean validateValueHookDerivationRule(ValueHookDerivationRule valueHookDerivationRule, DiagnosticChain diagnostics, Map<Object, Object> context)
   {
     if (!validate_NoCircularContainment(valueHookDerivationRule, diagnostics, context)) return false;
     boolean result = validate_EveryMultiplicityConforms(valueHookDerivationRule, diagnostics, context);
     if (result || diagnostics != null) result &= validate_EveryDataValueConforms(valueHookDerivationRule, diagnostics, context);
     if (result || diagnostics != null) result &= validate_EveryReferenceIsContained(valueHookDerivationRule, diagnostics, context);
     if (result || diagnostics != null) result &= validate_EveryBidirectionalReferenceIsPaired(valueHookDerivationRule, diagnostics, context);
     if (result || diagnostics != null) result &= validate_EveryProxyResolves(valueHookDerivationRule, diagnostics, context);
     if (result || diagnostics != null) result &= validate_UniqueID(valueHookDerivationRule, diagnostics, context);
     if (result || diagnostics != null) result &= validate_EveryKeyUnique(valueHookDerivationRule, diagnostics, context);
     if (result || diagnostics != null) result &= validate_EveryMapEntryUnique(valueHookDerivationRule, diagnostics, context);
     if (result || diagnostics != null) result &= validateRuleContext_AllExpressions(valueHookDerivationRule, diagnostics, context);
     if (result || diagnostics != null) result &= validateValueHookDerivationRule_DefinedForAttribute(valueHookDerivationRule, diagnostics, context);
     return result;
   }
 
 	/**
 	 * Validates the DefinedForAttribute constraint of '
 	 * <em>Value Hook Derivation Rule</em>'. <!-- begin-user-doc --> <!--
 	 * end-user-doc -->
 	 * 
 	 * @generated NOT
 	 */
 	public boolean validateValueHookDerivationRule_DefinedForAttribute(
 			ValueHookDerivationRule valueHookDerivationRule,
 			DiagnosticChain diagnostics, Map<Object, Object> context) {
 
 		if (!(valueHookDerivationRule.getEBoundFeature() instanceof EAttribute)) {
 			addError(diagnostics,
 					"Value hooks can only by defined for attributes",
 					valueHookDerivationRule);
 			return false;
 		}
 		return true;
 	}
 
   /**
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @generated
    */
   public boolean validateValuePrototypeDerivationRule(ValuePrototypeDerivationRule valuePrototypeDerivationRule, DiagnosticChain diagnostics, Map<Object, Object> context)
   {
     if (!validate_NoCircularContainment(valuePrototypeDerivationRule, diagnostics, context)) return false;
     boolean result = validate_EveryMultiplicityConforms(valuePrototypeDerivationRule, diagnostics, context);
     if (result || diagnostics != null) result &= validate_EveryDataValueConforms(valuePrototypeDerivationRule, diagnostics, context);
     if (result || diagnostics != null) result &= validate_EveryReferenceIsContained(valuePrototypeDerivationRule, diagnostics, context);
     if (result || diagnostics != null) result &= validate_EveryBidirectionalReferenceIsPaired(valuePrototypeDerivationRule, diagnostics, context);
     if (result || diagnostics != null) result &= validate_EveryProxyResolves(valuePrototypeDerivationRule, diagnostics, context);
     if (result || diagnostics != null) result &= validate_UniqueID(valuePrototypeDerivationRule, diagnostics, context);
     if (result || diagnostics != null) result &= validate_EveryKeyUnique(valuePrototypeDerivationRule, diagnostics, context);
     if (result || diagnostics != null) result &= validate_EveryMapEntryUnique(valuePrototypeDerivationRule, diagnostics, context);
     if (result || diagnostics != null) result &= validateRuleContext_AllExpressions(valuePrototypeDerivationRule, diagnostics, context);
     if (result || diagnostics != null) result &= validateValuePrototypeDerivationRule_NotDefinedForReference(valuePrototypeDerivationRule, diagnostics, context);
     return result;
   }
 
   /**
    * Validates the NotDefinedForReference constraint of '<em>Value Prototype Derivation Rule</em>'.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @generated NOT
    */
 	public boolean validateValuePrototypeDerivationRule_NotDefinedForReference(
 			ValuePrototypeDerivationRule valuePrototypeDerivationRule,
 			DiagnosticChain diagnostics, Map<Object, Object> context) {
 
 		if (valuePrototypeDerivationRule.getEBoundFeature() != null) {
 			addError(diagnostics,
 					"Value prototypes cannot be defined for features",
 					valuePrototypeDerivationRule);
 			return false;
 		}
 		return true;
 	}
 
   /**
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @generated
    */
   public boolean validateVariationPointDerivationRule(VariationPointDerivationRule variationPointDerivationRule, DiagnosticChain diagnostics, Map<Object, Object> context)
   {
     if (!validate_NoCircularContainment(variationPointDerivationRule, diagnostics, context)) return false;
     boolean result = validate_EveryMultiplicityConforms(variationPointDerivationRule, diagnostics, context);
     if (result || diagnostics != null) result &= validate_EveryDataValueConforms(variationPointDerivationRule, diagnostics, context);
     if (result || diagnostics != null) result &= validate_EveryReferenceIsContained(variationPointDerivationRule, diagnostics, context);
     if (result || diagnostics != null) result &= validate_EveryBidirectionalReferenceIsPaired(variationPointDerivationRule, diagnostics, context);
     if (result || diagnostics != null) result &= validate_EveryProxyResolves(variationPointDerivationRule, diagnostics, context);
     if (result || diagnostics != null) result &= validate_UniqueID(variationPointDerivationRule, diagnostics, context);
     if (result || diagnostics != null) result &= validate_EveryKeyUnique(variationPointDerivationRule, diagnostics, context);
     if (result || diagnostics != null) result &= validate_EveryMapEntryUnique(variationPointDerivationRule, diagnostics, context);
     if (result || diagnostics != null) result &= validateRuleContext_AllExpressions(variationPointDerivationRule, diagnostics, context);
     return result;
   }
 
   /**
    * Returns the resource locator that will be used to fetch messages for this validator's diagnostics.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @generated NOT
    */
 	@Override
 	public ResourceLocator getResourceLocator() {
 		// Specialize this to return a resource locator for messages specific to
 		// this validator.
 		return super.getResourceLocator();
 	}
 	
 	@Override
 	public boolean validate_EveryProxyResolves(EObject eObject,
 			DiagnosticChain diagnostics, Map<Object, Object> context) {
 		// disable proxy check since it is covered by resolvers
 		return true;
 	}
 	
 	protected void addError(DiagnosticChain diagnostics, String message, EObject data) {
 		if (diagnostics != null) {
 			diagnostics.add(new BasicDiagnostic(
 					Diagnostic.ERROR,
 					DIAGNOSTIC_SOURCE,
 					0,
 					message,
 					new Object[] { data }));
 		}
 	}
 
 } //ReuseextensionValidator
