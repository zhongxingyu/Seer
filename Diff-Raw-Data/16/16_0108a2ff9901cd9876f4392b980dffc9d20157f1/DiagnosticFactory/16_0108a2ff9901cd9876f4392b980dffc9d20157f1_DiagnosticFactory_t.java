 /*******************************************************************************
  * Copyright (c) 2006 Oracle Corporation7
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *    Cameron Bateman/Oracle - initial API and implementation
  *    
  ********************************************************************************/
 
 package org.eclipse.jst.jsf.validation.internal.el.diagnostics;
 
 import java.text.MessageFormat;
 
 import org.eclipse.emf.common.util.BasicDiagnostic;
 import org.eclipse.emf.common.util.Diagnostic;
 import org.eclipse.jst.jsf.validation.internal.ELValidationPreferences;
 import org.eclipse.osgi.util.NLS;
 
 /**
  * Factory for all operator diagnostics.  Class should
  * not be implemented, instantiated or sub-classed.
  * 
  * @author cbateman
  *
  */
 public final class DiagnosticFactory 
 {
     
     /**
      * Problem id
      */
     public final static int BINARY_OP_BOTH_OPERANDS_NULL_ID = 0;
     /**
      * Problem id
      */
     public final static int BINARY_OP_POSSIBLE_DIVISION_BY_ZERO_ID = 1;
     /**
      * Problem id
      */
     public final static int BINARY_OP_COULD_NOT_MAKE_NUMERIC_COERCION_ID = 2;
     /**
      * Problem id
      */
     public final static int BINARY_OP_CONSTANT_EXPRESSION_ALWAYS_EVAL_SAME_ID = 3;
     /**
      * Problem id
      */
     public final static int BINARY_OP_EQUALITY_COMP_WITH_NULL_ALWAYS_EVAL_SAME_ID = 4;
     /**
      * Problem id
      */
     public final static int BINARY_OP_CANNOT_COERCE_ARGUMENT_TO_BOOLEAN_ID = 5;
     /**
      * Problem id
      */
     public final static int BINARY_OP_FIRST_ARGUMENT_SHORT_CIRCUITS_ID = 6;
     /**
      * Problem id
      */
     public final static int BINARY_OP_SECOND_ARGUMENT_ALWAYS_EVAL_SAME_ID = 7;
     /**
      * Problem id
      */
     public final static int BINARY_OP_NO_AVAILABLE_TYPE_COERCION_ID = 8;
     /**
      * Problem id
      */
     public final static int BINARY_OP_COULD_NOT_COERCE_LITERALS_TO_NUMBERS_ID = 9;
     /**
      * Problem id
      */
     public final static int UNARY_OP_CONSTANT_EXPRESSION_EVAL_SAME_ID = 10;
     /**
      * Problem id
      */
     public final static int UNARY_OP_EMPTY_ALWAYS_FALSE_ON_TYPE_ID = 11;
     /**
      * Problem id
      */
     public final static int UNARY_OP_MINUS_ON_NULL_ALWAYS_ZERO_ID = 12;
     /**
      * Problem id
      */
     public final static int UNARY_OP_COULD_NOT_MAKE_NUMERIC_COERCION_ID = 13;
     /**
      * Problem id
      */
     public final static int UNARY_OP_CANNOT_COERCE_ARGUMENT_TO_BOOLEAN_ID = 14;
     /**
      * Problem id
      */
     public final static int TERNARY_OP_CHOICE_IS_ALWAYS_SAME_ID = 15;
     /**
      * Problem id
      */
     public final static int TERNARY_OP_CANNOT_COERCE_CHOICE_TO_BOOLEAN_ID = 16; 
     /**
      * Problem id
      */
     public final static int UNARY_OP_STRING_CONVERSION_NOT_GUARANTEED_ID = 17;
     /**
      * Problem id
      */
     public final static int CANNOT_APPLY_OPERATOR_TO_METHOD_BINDING_ID = 18;
     /**
      * Problem id
      */
     public final static int MEMBER_NOT_FOUND_ID = 19;
     /**
      * Problem id
      */
     public final static int VARIABLE_NOT_FOUND_ID = 20;
     /**
      * Problem id
      */
     public final static int MISSING_CLOSING_EXPR_BRACKET_ID = 21;
     /**
      * Problem id
      */
     public final static int GENERAL_SYNTAX_ERROR_ID = 22;
     /**
      * Problem id
      */
     public final static int EMPTY_EL_EXPRESSION_ID = 23;
     
     /**
      * 
      */
     public final static int BINARY_OP_DOT_WITH_VALUEB_NULL_ID = 24;
     
     /**
      * Identifies a problem where we have x.y and x is a map.  In this case
      * the recommend syntax is x['y']
      */
     public final static int BINARY_OP_DOT_WITH_DOTTED_KEY_SHOULD_USE_ARRAY_ID = 25;
     
     /**
      * Indicates that a numeric array accessor is being used with a value
      * which is either < 0 or may be greater than the assumed size of the array
      */
     public final static int POSSIBLE_ARRAY_INDEX_OUT_OF_BOUNDS_ID = 26;
     
     /**
      * Identifies the problem where an expr like bean.enumProp == 'blah'
      * but 'blah' does not match any of the possible enum constants, meaning
      * that the operation will always resolve to a constant value (in this case false)
      */
     public final static int BINARY_COMPARISON_WITH_ENUM_ALWAYS_SAME_ID = 27;
     
     /**
      * Identifies a problem where two enum variables are compared but the enums are
      * not compatible.  i.e. bean.enum1 < bean.enum2.  Enum.compareTo() may throw
      * an exception in this case (CCE).
      */
     public final static int BINARY_OP_COMPARISON_OF_ENUMS_INCOMPATIBLE_ID = 28;
     
     /**
      * Count of ids
      */
     public final static int NUM_IDS = 29;
     
     /**
      * @param operatorName
      * @return a configured diagnostic
      */
     public Diagnostic create_BINARY_OP_BOTH_OPERANDS_NULL(final String operatorName)
     {
         final String message = NLS.bind(
                 Messages.BINARY_OP_BOTH_OPERANDS_NULL, operatorName);
         return create(BINARY_OP_BOTH_OPERANDS_NULL_ID, message);
     }
     
     private static Diagnostic DIAGNOSTIC_POSSIBLE_DIV_BY_ZERO;
     
     /**
      * @return a configured diagnostic
      */
     public Diagnostic create_BINARY_OP_POSSIBLE_DIVISION_BY_ZERO()
     {
         if (DIAGNOSTIC_POSSIBLE_DIV_BY_ZERO == null)
         {
             final String message = Messages.POSSIBLE_DIV_BY_ZERO;
             DIAGNOSTIC_POSSIBLE_DIV_BY_ZERO = 
                 create(BINARY_OP_POSSIBLE_DIVISION_BY_ZERO_ID, message);
         }
         return DIAGNOSTIC_POSSIBLE_DIV_BY_ZERO;
     }
     
     /**
      * @param operatorName
      * @return a configured diagnostic
      */
     public Diagnostic create_BINARY_OP_COULD_NOT_MAKE_NUMERIC_COERCION(final String operatorName)
     {
         final String message = 
             NLS.bind(Messages.BINARY_OP_COULD_NOT_MAKE_NUMERIC_COERCION, operatorName);
         return create(BINARY_OP_COULD_NOT_MAKE_NUMERIC_COERCION_ID, message);
     }
     
     /**
      * @param operatorName
      * @param value
      * @return a configured diagnostic
      */
     public Diagnostic create_BINARY_OP_CONSTANT_EXPRESSION_ALWAYS_EVAL_SAME(final String operatorName, final String value)
     {
         final String message =  
             NLS.bind(Messages.BINARY_OP_CONSTANT_EXPRESSION_ALWAYS_EVAL_SAME,operatorName, value);
         return create(BINARY_OP_CONSTANT_EXPRESSION_ALWAYS_EVAL_SAME_ID, message);
     }
     
     /**
      * @param value
      * @return a configured diagnostic
      */
     public Diagnostic create_BINARY_OP_EQUALITY_COMP_WITH_NULL_ALWAYS_EVAL_SAME(final String value)
     {
         final String message = 
             NLS.bind(Messages.BINARY_OP_EQUALITY_COMP_WITH_NULL_ALWAYS_EVAL_SAME, value);
         return create(BINARY_OP_EQUALITY_COMP_WITH_NULL_ALWAYS_EVAL_SAME_ID, message);
     }
 
     /**
      * @param whichArgument
      * @return a configured diagnostic
      */
     public Diagnostic create_BINARY_OP_CANNOT_COERCE_ARGUMENT_TO_BOOLEAN(final String whichArgument)
     {
         final String message = 
             NLS.bind(Messages.BINARY_OP_CANNOT_COERCE_ARGUMENT_TO_BOOLEAN, whichArgument);
         return create(BINARY_OP_CANNOT_COERCE_ARGUMENT_TO_BOOLEAN_ID, message);
     }
     
     /**
      * @param shortCircuitValue
      * @param operatorName
      * @return a configured diagnostic
      */
     public Diagnostic create_BINARY_OP_FIRST_ARGUMENT_SHORT_CIRCUITS(boolean shortCircuitValue, String operatorName)
     {
         final String message = 
             NLS.bind(Messages.BINARY_OP_FIRST_ARGUMENT_SHORT_CIRCUITS, 
                     Boolean.valueOf(shortCircuitValue), operatorName);
         return create(BINARY_OP_FIRST_ARGUMENT_SHORT_CIRCUITS_ID, message); 
     }
     
     /**
      * @param shortCircuitValue
      * @param operatorName
      * @return a configured diagnostic
      */
     public Diagnostic create_BINARY_OP_SECOND_ARGUMENT_ALWAYS_EVAL_SAME(final boolean shortCircuitValue, final String operatorName)
     {
         final String message =
             NLS.bind(Messages.BINARY_OP_SECOND_ARGUMENT_ALWAYS_EVAL_SAME, 
                     Boolean.valueOf(shortCircuitValue), operatorName);
         
         return create(BINARY_OP_SECOND_ARGUMENT_ALWAYS_EVAL_SAME_ID, message); 
     }
 
     private static Diagnostic DIAGNOSTIC_NO_AVAILABLE_TYPE_COERCION;
         
     /**
      * @return a configured diagnostic
      */
     public Diagnostic create_BINARY_OP_NO_AVAILABLE_TYPE_COERCION()
     {
         if (DIAGNOSTIC_NO_AVAILABLE_TYPE_COERCION == null)
         {
             DIAGNOSTIC_NO_AVAILABLE_TYPE_COERCION = 
               create(BINARY_OP_NO_AVAILABLE_TYPE_COERCION_ID, 
                     Messages.BINARY_OP_NO_AVAILABLE_TYPE_COERCION);
         }
         
         return DIAGNOSTIC_NO_AVAILABLE_TYPE_COERCION;
     }
 
     private static Diagnostic DIAGNOSTIC_COULD_NOT_COERCE_LITERALS;
     
     /**
      * @return a configured diagnostic
      */
     public Diagnostic create_BINARY_OP_COULD_NOT_COERCE_LITERALS_TO_NUMBERS()
     {
         if (DIAGNOSTIC_COULD_NOT_COERCE_LITERALS == null)
         {
             DIAGNOSTIC_COULD_NOT_COERCE_LITERALS = 
                 create(BINARY_OP_COULD_NOT_COERCE_LITERALS_TO_NUMBERS_ID, 
                     Messages.BINARY_OP_COULD_NOT_COERCE_LITERALS_TO_NUMBERS);
         }
         
         return DIAGNOSTIC_COULD_NOT_COERCE_LITERALS;
     }
     
     /**
      * @param operatorName
      * @param value
      * @return a configured diagnostic
      */
     public Diagnostic create_UNARY_OP_CONSTANT_EXPRESSION_EVAL_SAME_ID(final String operatorName, final String value)
     {
         final String message = 
             NLS.bind(Messages.UNARY_OP_CONSTANT_EXPRESSION_EVAL_SAME, 
                     operatorName, value);
         return create(UNARY_OP_CONSTANT_EXPRESSION_EVAL_SAME_ID, message);
     }
     
     /**
      * @return a configured diagnostic
      */
     public Diagnostic create_UNARY_OP_EMPTY_ALWAYS_FALSE_ON_TYPE()
     {
         return create(UNARY_OP_EMPTY_ALWAYS_FALSE_ON_TYPE_ID, 
              Messages.UNARY_OP_EMPTY_ALWAYS_FALSE_ON_TYPE);
     }
     
     /**
      * @return a configured diagnostic
      */
     public Diagnostic create_UNARY_OP_MINUS_ON_NULL_ALWAYS_ZERO()
     {
         return create(UNARY_OP_MINUS_ON_NULL_ALWAYS_ZERO_ID, 
             Messages.UNARY_OP_MINUS_ON_NULL_ALWAYS_ZERO);
     }
     
     /**
      * @param operatorName
      * @return a configured diagnostic
      */
     public Diagnostic create_UNARY_OP_COULD_NOT_MAKE_NUMERIC_COERCION(final String operatorName)
     {
         final String message = 
             NLS.bind(Messages.UNARY_OP_COULD_NOT_MAKE_NUMERIC_COERCION,
                     operatorName);
         return create(UNARY_OP_COULD_NOT_MAKE_NUMERIC_COERCION_ID, message);
     }
     
     /**
      * @param operatorName
      * @return a configured diagnostic
      */
     public Diagnostic create_UNARY_OP_STRING_CONVERSION_NOT_GUARANTEED(final String operatorName)
     {
         final String message =
             NLS.bind(Messages.UNARY_OP_STRING_CONVERSION_NOT_GUARANTEED,
                                 operatorName);
         return create(UNARY_OP_STRING_CONVERSION_NOT_GUARANTEED_ID, message);
     }
     
     /**
      * @return a configured diagnostic
      */
     public Diagnostic create_UNARY_OP_CANNOT_COERCE_ARGUMENT_TO_BOOLEAN()
     {
         return create(UNARY_OP_CANNOT_COERCE_ARGUMENT_TO_BOOLEAN_ID, 
                 Messages.UNARY_OP_CANNOT_COERCE_ARGUMENT_TO_BOOLEAN);
     }
     
     /**
      * @param result
      * @param whichSelected
      * @return a configured diagnostic
      */
     public Diagnostic create_TERNARY_OP_CHOICE_IS_ALWAYS_SAME(boolean result, String whichSelected)
     {
         final String message =
             NLS.bind(Messages.TERNARY_OP_CHOICE_IS_ALWAYS_SAME, 
                     Boolean.valueOf(result), whichSelected);
         return create(TERNARY_OP_CHOICE_IS_ALWAYS_SAME_ID, message);
     }
     
     /**
      * @return a configured diagnostic
      */
     public Diagnostic create_TERNARY_OP_CANNOT_COERCE_CHOICE_TO_BOOLEAN()
     {
         return create(TERNARY_OP_CANNOT_COERCE_CHOICE_TO_BOOLEAN_ID, 
                 Messages.TERNARY_OP_CANNOT_COERCE_CHOICE_TO_BOOLEAN_ID);
     }
     
     /**
      * Method bindings cannot participate in expressions involving operators
      * Per JSP 1.1 spec section 5.2.1
      * 
      * @return a configured message
      */
     public Diagnostic create_CANNOT_APPLY_OPERATOR_TO_METHOD_BINDING()
     {
         return create(CANNOT_APPLY_OPERATOR_TO_METHOD_BINDING_ID, 
                 Messages.CANNOT_APPLY_OPERATORS_TO_MB);
     }
 
     /**
      * @param curMemberSymbol
      * @param owningMember
      * @return a configured message
      */
     public Diagnostic create_MEMBER_NOT_FOUND(String curMemberSymbol, String owningMember)
     {
         return create(MEMBER_NOT_FOUND_ID,
                 NLS.bind(Messages.VM_PROP_NAME_NOT_FOUND, curMemberSymbol, owningMember));
     }
 
     /**
      * @param variableName
      * @return a configured message
      */
     public Diagnostic create_VARIABLE_NOT_FOUND(String variableName)
     {
         return create(VARIABLE_NOT_FOUND_ID
                 , NLS.bind(Messages.VM_ROOT_NAME_NOT_FOUND, variableName));
     }
 
     /**
      * @return a configured message
      */
     public Diagnostic create_MISSING_CLOSING_EXPR_BRACKET()
     {
         return create(MISSING_CLOSING_EXPR_BRACKET_ID
                 , Messages.MISSING_CLOSING_EXPR_BRACKET);
     }
     
     /**
      * @return a configured message
      */
     public Diagnostic create_GENERAL_SYNTAX_ERROR()
     {
         return create(GENERAL_SYNTAX_ERROR_ID, Messages.GENERAL_SYNTAX_ERROR);
     }
 
     /**
      * @return a configured message
      */
     public Diagnostic create_EMPTY_EL_EXPRESSION()
     {
         return create(EMPTY_EL_EXPRESSION_ID, Messages.EMPTY_EL_EXPRESSION);
     }
 
     /**
      * @return a configured diagnostic
      */
     public Diagnostic create_BINARY_OP_DOT_WITH_VALUEB_NULL()
     {
         return create(BINARY_OP_DOT_WITH_VALUEB_NULL_ID
                 , Messages.BINARY_OP_DOT_WITH_VALUEB_NULL);
     }
     
     /**
      * @param valAName
      * @param valueBName
      * @return a configured diagnostic
      */
     public Diagnostic create_BINARY_OP_DOT_WITH_VALUEA_MAP_SHOULD_USE_ARRAY(final String valAName, final String valueBName)
     {
         final Object[] formatArgs = new Object[] {valAName, valueBName};
         return create(BINARY_OP_DOT_WITH_DOTTED_KEY_SHOULD_USE_ARRAY_ID,
                 MessageFormat.format(Messages.BINARY_OP_DOT_WITH_DOTTED_KEY_SHOULD_USE_ARRAY,formatArgs));
     }
     
     /**
      * @param value
      * @return an array index out of bounds diagnostic
      */
     public Diagnostic create_POSSIBLE_ARRAY_INDEX_OUT_OF_BOUNDS(final Integer value)
     {
         final Object[] formatArgs = new Object[] {value};
 
         return create(POSSIBLE_ARRAY_INDEX_OUT_OF_BOUNDS_ID
                 , MessageFormat.format(Messages.POSSIBLE_ARRAY_INDEX_OUT_OF_BOUNDS,formatArgs));
     }
     
     /**
      * @param operatorName
      * @param invariantResult
      * @param enumName
      * @param fieldName
      * @return a diagnostic
      */
     public Diagnostic create_BINARY_COMPARISON_WITH_ENUM_AND_CONST_ALWAYS_SAME(final String operatorName, final boolean invariantResult, final String enumName,  final String fieldName)
     {
         return create(BINARY_COMPARISON_WITH_ENUM_ALWAYS_SAME_ID
                 , MessageFormat.format(Messages.BINARY_COMPARISON_WITH_ENUM_AND_CONST_ALWAYS_SAME
                                       , new Object[] {operatorName
                                       , Boolean.valueOf(invariantResult), enumName, fieldName}));
     }
 
     /**
      * @param operatorName
      * @param invariantResult
      * @param enumName1
      * @param enumName2
      * @return a diagnostic
      */
     public Diagnostic create_BINARY_COMPARISON_WITH_TWO_ENUMS_ALWAYS_SAME(final String operatorName, final boolean invariantResult, final String enumName1, final String enumName2)
     {
         return create(BINARY_COMPARISON_WITH_ENUM_ALWAYS_SAME_ID
                 , MessageFormat.format(Messages.BINARY_COMPARISON_WITH_TWO_ENUMS_ALWAYS_SAME
                                       , new Object[] {operatorName
                                       , Boolean.valueOf(invariantResult), enumName1, enumName2}));
     }
     
     /**
      * @param operatorName
      * @param invariantResult
      * @param enumName
      * @param nonEnum
      * @return a diagnostic
      */
     public Diagnostic create_BINARY_COMPARISON_WITH_ENUM_AND_UNCOERCABLE_NONCONST_ALWAYS_SAME(final String operatorName, final boolean invariantResult, final String enumName, final String nonEnum)
     {
         return create(BINARY_COMPARISON_WITH_ENUM_ALWAYS_SAME_ID
                 , MessageFormat.format(Messages.BINARY_COMPARISON_WITH_ENUM_AND_UNCOERCABLE_NONCONST_ALWAYS_SAME
                                       , new Object[] {operatorName
                                       , Boolean.valueOf(invariantResult), enumName, nonEnum}));
     }
     
     /**
      * @return a diagnostic
      */
     public Diagnostic create_BINARY_OP_COMPARISON_OF_ENUMS_INCOMPATIBLE()
     {
         return create(BINARY_OP_COMPARISON_OF_ENUMS_INCOMPATIBLE_ID
                 , Messages.BINARY_OP_COMPARISON_OF_ENUMS_INCOMPATIBLE);
     }
 
     private BasicDiagnostic create(int diagnosticId, String message)
     {
         final int severity = ELValidationPreferences.getDefaultSeverity(diagnosticId);
         return new BasicDiagnostic(severity, "" //$NON-NLS-1$
                 , diagnosticId
                , message
                 , null);
     }
 }
