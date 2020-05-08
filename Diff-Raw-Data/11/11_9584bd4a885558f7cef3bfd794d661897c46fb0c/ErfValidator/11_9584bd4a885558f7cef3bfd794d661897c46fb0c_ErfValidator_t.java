 /**
  * <copyright>
  * </copyright>
  *
  * $Id$
  */
 package era.foss.erf.util;
 
 import era.foss.erf.*;
 
 import java.util.ArrayList;
 import java.util.Map;
 
 import org.eclipse.emf.common.util.Diagnostic;
 import org.eclipse.emf.common.util.DiagnosticChain;
 import org.eclipse.emf.common.util.ResourceLocator;
 
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.ecore.EPackage;
 
 import org.eclipse.emf.ecore.util.EObjectValidator;
 
 /**
  * <!-- begin-user-doc --> The <b>Validator</b> for the model. <!-- end-user-doc -->
  * @see era.foss.erf.ErfPackage
  * @generated
  */
 public class ErfValidator extends EObjectValidator {
     /**
      * The cached model package
      * <!-- begin-user-doc --> <!-- end-user-doc -->
      * @generated
      */
     public static final ErfValidator INSTANCE = new ErfValidator();
 
     /**
      * A constant for the {@link org.eclipse.emf.common.util.Diagnostic#getSource() source} of diagnostic {@link org.eclipse.emf.common.util.Diagnostic#getCode() codes} from this package.
      * <!-- begin-user-doc --> <!--
      * end-user-doc -->
      * @see org.eclipse.emf.common.util.Diagnostic#getSource()
      * @see org.eclipse.emf.common.util.Diagnostic#getCode()
      * @generated
      */
     public static final String DIAGNOSTIC_SOURCE = "era.foss.erf";
 
     /**
      * A constant with a fixed name that can be used as the base value for additional hand written constants. <!--
      * begin-user-doc --> <!-- end-user-doc -->
      * 
      * @generated
      */
     private static final int GENERATED_DIAGNOSTIC_CODE_COUNT = 0;
 
     /**
      * A constant with a fixed name that can be used as the base value for additional hand written constants in a derived class.
      * <!-- begin-user-doc --> <!-- end-user-doc -->
      * @generated
      */
     protected static final int DIAGNOSTIC_CODE_COUNT = GENERATED_DIAGNOSTIC_CODE_COUNT;
 
     /**
      * Creates an instance of the switch.
      * <!-- begin-user-doc --> <!-- end-user-doc -->
      * @generated
      */
     public ErfValidator() {
         super();
     }
 
     /**
      * Returns the package of this validator switch.
      * <!-- begin-user-doc --> <!-- end-user-doc -->
      * @generated
      */
     @Override
     protected EPackage getEPackage() {
         return ErfPackage.eINSTANCE;
     }
 
     /**
      * Calls <code>validateXXX</code> for the corresponding classifier of the model.
      * <!-- begin-user-doc --> <!--
      * end-user-doc -->
      * @generated
      */
     @Override
     protected boolean validate( int classifierID,
                                 Object value,
                                 DiagnosticChain diagnostics,
                                 Map<Object, Object> context ) {
         switch (classifierID) {
         case ErfPackage.ATTRIBUTE_DEFINITION:
             return validateAttributeDefinition( (AttributeDefinition)value, diagnostics, context );
         case ErfPackage.ATTRIBUTE_VALUE:
             return validateAttributeValue( (AttributeValue)value, diagnostics, context );
         case ErfPackage.DATATYPE_DEFINITION:
             return validateDatatypeDefinition( (DatatypeDefinition)value, diagnostics, context );
         case ErfPackage.SPEC_ELEMENT_WITH_USER_DEFINED_ATTRIBUTES:
             return validateSpecElementWithUserDefinedAttributes( (SpecElementWithUserDefinedAttributes)value,
                                                                  diagnostics,
                                                                  context );
         case ErfPackage.SPEC_OBJECT:
             return validateSpecObject( (SpecObject)value, diagnostics, context );
         case ErfPackage.SPEC_TYPE:
             return validateSpecType( (SpecType)value, diagnostics, context );
         case ErfPackage.MAP:
             return validateMap( (Map<?, ?>)value, diagnostics, context );
         case ErfPackage.ATTRIBUTE_VALUE_SIMPLE:
             return validateAttributeValueSimple( (AttributeValueSimple)value, diagnostics, context );
         case ErfPackage.DATATYPE_DEFINITION_INTEGER:
             return validateDatatypeDefinitionInteger( (DatatypeDefinitionInteger)value, diagnostics, context );
         case ErfPackage.DATATYPE_DEFINITION_SIMPLE:
             return validateDatatypeDefinitionSimple( (DatatypeDefinitionSimple)value, diagnostics, context );
         case ErfPackage.DATATYPE_DEFINITION_STRING:
             return validateDatatypeDefinitionString( (DatatypeDefinitionString)value, diagnostics, context );
         case ErfPackage.IDENTIFIABLE:
             return validateIdentifiable( (Identifiable)value, diagnostics, context );
         case ErfPackage.ATTRIBUTE_DEFINITION_SIMPLE:
             return validateAttributeDefinitionSimple( (AttributeDefinitionSimple)value, diagnostics, context );
         case ErfPackage.ERF:
             return validateERF( (ERF)value, diagnostics, context );
         case ErfPackage.CONTENT:
             return validateContent( (Content)value, diagnostics, context );
         default:
             return true;
         }
     }
 
     /**
      * <!-- begin-user-doc --> <!-- end-user-doc -->
      * @generated
      */
     public boolean validateAttributeDefinition( AttributeDefinition attributeDefinition,
                                                 DiagnosticChain diagnostics,
                                                 Map<Object, Object> context ) {
         return validate_EveryDefaultConstraint( attributeDefinition, diagnostics, context );
     }
 
     /**
      * <!-- begin-user-doc --> <!-- end-user-doc -->
      * @generated
      */
     public boolean validateAttributeValue( AttributeValue attributeValue,
                                            DiagnosticChain diagnostics,
                                            Map<Object, Object> context ) {
         return validate_EveryDefaultConstraint( attributeValue, diagnostics, context );
     }
 
     /**
      * <!-- begin-user-doc --> <!-- end-user-doc -->
      * @generated
      */
     public boolean validateDatatypeDefinition( DatatypeDefinition datatypeDefinition,
                                                DiagnosticChain diagnostics,
                                                Map<Object, Object> context ) {
         return validate_EveryDefaultConstraint( datatypeDefinition, diagnostics, context );
     }
 
     /**
      * <!-- begin-user-doc --> <!-- end-user-doc -->
      * @generated
      */
     public boolean validateSpecElementWithUserDefinedAttributes( SpecElementWithUserDefinedAttributes specElementWithUserDefinedAttributes,
                                                                  DiagnosticChain diagnostics,
                                                                  Map<Object, Object> context ) {
         return validate_EveryDefaultConstraint( specElementWithUserDefinedAttributes, diagnostics, context );
     }
 
     /**
      * <!-- begin-user-doc --> <!-- end-user-doc -->
      * @generated
      */
     public boolean validateSpecObject( SpecObject specObject, DiagnosticChain diagnostics, Map<Object, Object> context ) {
         return validate_EveryDefaultConstraint( specObject, diagnostics, context );
     }
 
     /**
      * <!-- begin-user-doc --> <!-- end-user-doc -->
      * @generated
      */
     public boolean validateSpecType( SpecType specType, DiagnosticChain diagnostics, Map<Object, Object> context ) {
         return validate_EveryDefaultConstraint( specType, diagnostics, context );
     }
 
     /**
      * <!-- begin-user-doc --> <!-- end-user-doc -->
      * @generated
      */
     public boolean validateMap( Map<?, ?> map, DiagnosticChain diagnostics, Map<Object, Object> context ) {
         return validate_EveryDefaultConstraint( (EObject)map, diagnostics, context );
     }
 
     /**
      * <!-- begin-user-doc --> <!-- end-user-doc -->
      * @generated
      */
     public boolean validateAttributeValueSimple( AttributeValueSimple attributeValueSimple,
                                                  DiagnosticChain diagnostics,
                                                  Map<Object, Object> context ) {
        if( !validate_NoCircularContainment( attributeValueSimple, diagnostics, context ) ) return false;
         boolean result = validate_EveryMultiplicityConforms( attributeValueSimple, diagnostics, context );
         if( result || diagnostics != null ) result &= validate_EveryDataValueConforms( attributeValueSimple,
                                                                                        diagnostics,
                                                                                        context );
         if( result || diagnostics != null ) result &= validate_EveryReferenceIsContained( attributeValueSimple,
                                                                                           diagnostics,
                                                                                           context );
        if( result || diagnostics != null ) result &= validate_EveryBidirectionalReferenceIsPaired( attributeValueSimple,
                                                                                                    diagnostics,
                                                                                                    context );
         if( result || diagnostics != null ) result &= validate_EveryProxyResolves( attributeValueSimple,
                                                                                    diagnostics,
                                                                                    context );
         if( result || diagnostics != null ) result &= validate_UniqueID( attributeValueSimple, diagnostics, context );
         if( result || diagnostics != null ) result &= validate_EveryKeyUnique( attributeValueSimple,
                                                                                diagnostics,
                                                                                context );
         if( result || diagnostics != null ) result &= validate_EveryMapEntryUnique( attributeValueSimple,
                                                                                     diagnostics,
                                                                                     context );
         if( result || diagnostics != null ) result &= validateAttributeValueSimple_DatatypeDefinitionConstraints( attributeValueSimple,
                                                                                                                   diagnostics,
                                                                                                                   context );
         return result;
     }
 
     /**
      * Validates the DatatypeDefinitionConstraints constraint of '<em>Attribute Value Simple</em>'. <!-- begin-user-doc
      * --> <!-- end-user-doc -->
      * 
      * @NOT generated
      */
     public boolean validateAttributeValueSimple_DatatypeDefinitionConstraints( AttributeValueSimple attributeValueSimple,
                                                                                DiagnosticChain diagnostics,
                                                                                Map<Object, Object> context ) {
         /* String holding the error message key in case an error is detected */
         String errorMsgKey = null;
         DatatypeDefinition datatypeDefinition = attributeValueSimple.getDefinition().getType();
         ArrayList<Object> substitutions = new ArrayList<Object>();
         substitutions.add( attributeValueSimple.getTheValue() );
 
         /* Check constraints if value is of DatatypedefinitionInteger */
         if( datatypeDefinition instanceof DatatypeDefinitionInteger ) {
 
             DatatypeDefinitionInteger datatypeDefinitionInteger = (DatatypeDefinitionInteger)datatypeDefinition;
 
             int integerValue = 0;
             try {
                 integerValue = Integer.parseInt( attributeValueSimple.getTheValue() );
             } catch( NumberFormatException e ) {
                 errorMsgKey = "_UI_DatatypeDefinitionConstraints_InvalidInteger";
             }
 
             if( errorMsgKey == null ) {
                 if( (datatypeDefinitionInteger.isSetMax() && integerValue > datatypeDefinitionInteger.getMax())
                     || (datatypeDefinitionInteger.isSetMin() && integerValue < datatypeDefinitionInteger.getMin()) ) {
                     errorMsgKey = "_UI_DatatypeDefinitionConstraints_Range";
                     substitutions.add( datatypeDefinitionInteger.getMin() );
                     substitutions.add( datatypeDefinitionInteger.getMax() );
                 }
             }
         }
 
         /* Check constraints if value is of DatatypedefinitionString */
         else if( datatypeDefinition instanceof DatatypeDefinitionString ) {
             DatatypeDefinitionString datatypeDefinitionString = (DatatypeDefinitionString)datatypeDefinition;
             if( datatypeDefinitionString.isSetMaxLength()
                 && attributeValueSimple.getTheValue().length() > datatypeDefinitionString.getMaxLength() ) {
                 errorMsgKey = "_UI_DatatypeDefinitionConstraints_StringLength";
                 substitutions.add( datatypeDefinitionString.getMaxLength() );
             }
         }
 
         /* check if an error has occurred */
         if( errorMsgKey != null && diagnostics != null ) {
             if( diagnostics != null ) {
                 diagnostics.add( createDiagnostic( Diagnostic.ERROR,
                                                    DIAGNOSTIC_SOURCE,
                                                    0,
                                                    errorMsgKey,
                                                    substitutions.toArray(),
                                                    new Object[]{attributeValueSimple},
                                                    context ) );
             }
             return false;
         }
         return true;
     }
 
     /**
      * <!-- begin-user-doc --> <!-- end-user-doc -->
      * @generated
      */
     public boolean validateDatatypeDefinitionInteger( DatatypeDefinitionInteger datatypeDefinitionInteger,
                                                       DiagnosticChain diagnostics,
                                                       Map<Object, Object> context ) {
        if( !validate_NoCircularContainment( datatypeDefinitionInteger, diagnostics, context ) ) return false;
         boolean result = validate_EveryMultiplicityConforms( datatypeDefinitionInteger, diagnostics, context );
         if( result || diagnostics != null ) result &= validate_EveryDataValueConforms( datatypeDefinitionInteger,
                                                                                        diagnostics,
                                                                                        context );
         if( result || diagnostics != null ) result &= validate_EveryReferenceIsContained( datatypeDefinitionInteger,
                                                                                           diagnostics,
                                                                                           context );
        if( result || diagnostics != null ) result &= validate_EveryBidirectionalReferenceIsPaired( datatypeDefinitionInteger,
                                                                                                    diagnostics,
                                                                                                    context );
         if( result || diagnostics != null ) result &= validate_EveryProxyResolves( datatypeDefinitionInteger,
                                                                                    diagnostics,
                                                                                    context );
         if( result || diagnostics != null ) result &= validate_UniqueID( datatypeDefinitionInteger,
                                                                          diagnostics,
                                                                          context );
         if( result || diagnostics != null ) result &= validate_EveryKeyUnique( datatypeDefinitionInteger,
                                                                                diagnostics,
                                                                                context );
         if( result || diagnostics != null ) result &= validate_EveryMapEntryUnique( datatypeDefinitionInteger,
                                                                                     diagnostics,
                                                                                     context );
         if( result || diagnostics != null ) result &= validateDatatypeDefinitionInteger_NonNegative( datatypeDefinitionInteger,
                                                                                                      diagnostics,
                                                                                                      context );
         if( result || diagnostics != null ) result &= validateDatatypeDefinitionInteger_MaxGreaterThanMin( datatypeDefinitionInteger,
                                                                                                            diagnostics,
                                                                                                            context );
         return result;
     }
 
     /**
      * Validates the NonNegative constraint of '<em>Datatype Definition Integer</em>'. <!-- begin-user-doc --> <!--
      * end-user-doc -->
      * 
      * @NOT generated
      */
     public boolean validateDatatypeDefinitionInteger_NonNegative( DatatypeDefinitionInteger datatypeDefinitionInteger,
                                                                   DiagnosticChain diagnostics,
                                                                   Map<Object, Object> context ) {
         if( (datatypeDefinitionInteger.isSetMax() && datatypeDefinitionInteger.getMax() < 0)
             || (datatypeDefinitionInteger.isSetMin() && datatypeDefinitionInteger.getMin() < 0) ) {
             if( diagnostics != null ) {
                 diagnostics.add( createDiagnostic( Diagnostic.ERROR,
                                                    DIAGNOSTIC_SOURCE,
                                                    0,
                                                    "_UI_GenericConstraint_diagnostic",
                                                    new Object[]{
                                                        "NonNegative",
                                                        getObjectLabel( datatypeDefinitionInteger, context )},
                                                    new Object[]{datatypeDefinitionInteger},
                                                    context ) );
             }
             return false;
         }
         return true;
     }
 
     /**
      * Validates the MaxGreaterThanMin constraint of '<em>Datatype Definition Integer</em>'. <!-- begin-user-doc -->
      * <!-- end-user-doc -->
      * 
      * @NOT generated
      */
     public boolean validateDatatypeDefinitionInteger_MaxGreaterThanMin( DatatypeDefinitionInteger datatypeDefinitionInteger,
                                                                         DiagnosticChain diagnostics,
                                                                         Map<Object, Object> context ) {
         if( datatypeDefinitionInteger.isSetMax()
             && datatypeDefinitionInteger.isSetMin()
             && datatypeDefinitionInteger.getMax() < datatypeDefinitionInteger.getMin() ) {
             if( diagnostics != null ) {
                 diagnostics.add( createDiagnostic( Diagnostic.ERROR,
                                                    DIAGNOSTIC_SOURCE,
                                                    0,
                                                    "_UI_GenericConstraint_diagnostic",
                                                    new Object[]{
                                                        "MaxGreaterThanMin",
                                                        getObjectLabel( datatypeDefinitionInteger, context )},
                                                    new Object[]{datatypeDefinitionInteger},
                                                    context ) );
             }
             return false;
         }
         return true;
     }
 
     /**
      * <!-- begin-user-doc --> <!-- end-user-doc -->
      * @generated
      */
     public boolean validateDatatypeDefinitionSimple( DatatypeDefinitionSimple datatypeDefinitionSimple,
                                                      DiagnosticChain diagnostics,
                                                      Map<Object, Object> context ) {
         return validate_EveryDefaultConstraint( datatypeDefinitionSimple, diagnostics, context );
     }
 
     /**
      * <!-- begin-user-doc --> <!-- end-user-doc -->
      * @generated
      */
     public boolean validateDatatypeDefinitionString( DatatypeDefinitionString datatypeDefinitionString,
                                                      DiagnosticChain diagnostics,
                                                      Map<Object, Object> context ) {
         return validate_EveryDefaultConstraint( datatypeDefinitionString, diagnostics, context );
     }
 
     /**
      * <!-- begin-user-doc --> <!-- end-user-doc -->
      * @generated
      */
     public boolean validateIdentifiable( Identifiable identifiable,
                                          DiagnosticChain diagnostics,
                                          Map<Object, Object> context ) {
         return validate_EveryDefaultConstraint( identifiable, diagnostics, context );
     }
 
     /**
      * <!-- begin-user-doc --> <!-- end-user-doc -->
      * @generated
      */
     public boolean validateAttributeDefinitionSimple( AttributeDefinitionSimple attributeDefinitionSimple,
                                                       DiagnosticChain diagnostics,
                                                       Map<Object, Object> context ) {
         return validate_EveryDefaultConstraint( attributeDefinitionSimple, diagnostics, context );
     }
 
     /**
      * <!-- begin-user-doc --> <!-- end-user-doc -->
      * @generated
      */
     public boolean validateERF( ERF erf, DiagnosticChain diagnostics, Map<Object, Object> context ) {
         return validate_EveryDefaultConstraint( erf, diagnostics, context );
     }
 
     /**
      * <!-- begin-user-doc --> <!-- end-user-doc -->
      * @generated
      */
     public boolean validateContent( Content content, DiagnosticChain diagnostics, Map<Object, Object> context ) {
         return validate_EveryDefaultConstraint( content, diagnostics, context );
     }
 
     /**
      * Returns the resource locator that will be used to fetch messages for this validator's diagnostics. <!--
      * begin-user-doc --> <!-- end-user-doc -->
      * 
      * @NOT generated
      */
     @Override
     public ResourceLocator getResourceLocator() {
         return Activator.INSTANCE.getPluginResourceLocator();
     }
 
 } // ErfValidator
