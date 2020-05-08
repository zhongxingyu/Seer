 /*******************************************************************************
  * Copyright (c) 2006 Oracle Corporation.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *    Cameron Bateman/Oracle - initial API and implementation
  *    
  ********************************************************************************/
 
 package org.eclipse.jst.jsf.validation.internal;
 
 import org.eclipse.core.runtime.preferences.IScopeContext;
 import org.eclipse.emf.common.util.Diagnostic;
 import org.eclipse.jface.preference.IPreferenceStore;
 import org.eclipse.jst.jsf.core.internal.IJSFPreferenceModel;
 import org.eclipse.jst.jsf.core.internal.JSFCorePlugin;
 import org.eclipse.jst.jsf.validation.internal.el.diagnostics.DiagnosticFactory;
 
 /**
  * Model object for EL validation preferences
  * 
  * @author cbateman
  */
 public class ELValidationPreferences implements IJSFPreferenceModel
 {
     private final static String KEY_ENABLE_BUILD_VALIDATION = 
         "org.eclipse.jst.jsf.ui.ValidateJSFELBuild";
     private final static boolean DEFAULT_ENABLE_BUILD_VALIDATION = true;
 
     private final static String KEY_ENABLE_INCREMENTAL_VALIDATION = 
         "org.eclipse.jst.jsf.ui.ValidateJSFELIncremental";
     private final static boolean DEFAULT_ENABLE_INCREMENTAL_VALIDATION = false;
     
 
     private boolean         _enableBuildValidation;
     private boolean         _enableIncrementalValidation;
     private int[]           _severities;
 
     /**
      * Loads the object from the preference store provided
      * 
      * @param prefStore
      */
     public void load(IPreferenceStore  prefStore)
     {
         if (!prefStore.contains(KEY_ENABLE_BUILD_VALIDATION))
         {
             prefStore.setDefault(KEY_ENABLE_BUILD_VALIDATION, DEFAULT_ENABLE_BUILD_VALIDATION);
         }
         _enableBuildValidation = 
             prefStore.getBoolean(KEY_ENABLE_BUILD_VALIDATION);
 
         if (!prefStore.contains(KEY_ENABLE_INCREMENTAL_VALIDATION))
         {
             prefStore.setDefault(KEY_ENABLE_INCREMENTAL_VALIDATION, DEFAULT_ENABLE_INCREMENTAL_VALIDATION);
         }
         _enableIncrementalValidation = 
             prefStore.getBoolean(KEY_ENABLE_INCREMENTAL_VALIDATION);
         
         loadSeverities(prefStore);
     }
 
     private void loadSeverities(final IPreferenceStore  prefStore)
     {
         final int severities[] = getSeverities();
         
         for (int i = 0; i < DiagnosticFactory.NUM_IDS; i++)
         {
             final String key = getKeyById(i);
             
             if (!prefStore.contains(key))
             {
                 final int diagSeverity = getDefaultSeverity(i); 
                 final Severity severity  = mapDiagToSeverity(diagSeverity);
         
                 prefStore.setDefault(key, severity.toString());
             }
             final String  storedSeverity = prefStore.getString(key);
             severities[i] = mapSeverityToDiag(storedSeverity);
         }
     }
     /**
      * Copies the object into the preference store but DOES NOT SAVE IT
      * 
      * @param prefStore
      */
     public void commit(IPreferenceStore prefStore)
     {
         prefStore.setValue(KEY_ENABLE_BUILD_VALIDATION, _enableBuildValidation);
         prefStore.setValue(KEY_ENABLE_INCREMENTAL_VALIDATION, 
                            _enableIncrementalValidation);
         commitSeverities(prefStore);
     }
     
     private void commitSeverities(final IPreferenceStore prefStore)
     {
         final int severities[] = getSeverities();
         
         for (int i = 0; i < severities.length; i++)
         {
             final String key = getKeyById(i);
             prefStore.setValue(key
                 , mapDiagToSeverity(severities[i]).toString());
         }
     }
     
     /**
      * Reverts the model to it's defaults.  Does not commit to pref store. 
      */
     public void setDefaults()
     {
         setEnableBuildValidation(DEFAULT_ENABLE_BUILD_VALIDATION);
         setEnableIncrementalValidation(DEFAULT_ENABLE_INCREMENTAL_VALIDATION);
         setProblemSeverityDefaults();
     }
     
     private void setProblemSeverityDefaults()
     {
         final int[] severities = getSeverities();
         
         for (int i = 0; i < DiagnosticFactory.NUM_IDS; i++)
         {
             severities[i] = getDefaultSeverity(i); 
         }
     }
     
     public Object getValueByKey(IScopeContext context, String key) {
         // ignore context for now; will be used when we have project overrides
         if (KEY_ENABLE_BUILD_VALIDATION.equals(key))
         {
             return Boolean.valueOf(isEnableBuildValidation());
         }
         else if (KEY_ENABLE_INCREMENTAL_VALIDATION.equals(key))
         {
             return Boolean.valueOf(isEnableIncrementalValidation());
         }
         else
         {
             try
             {
                 final Severity severity = getSeverity(key);
                 return severity.toString();
             }
             catch (IllegalArgumentException e)
             {
                 // getIdByKey will throw this exception if key is not a valid
                 // severity key.  Ignore the exception here and fall-through
             }
         }
         
         return null; // not found
     }
 
     /* (non-Javadoc)
      * @see org.eclipse.jst.jsf.core.internal.IJSFPreferenceModel#getStoredValueByKey(org.eclipse.core.runtime.preferences.IScopeContext, java.lang.String)
      */
     public Object getStoredValueByKey(IScopeContext context, String key) {
         // ignore context for now; will be used when we have project overrides
         if (KEY_ENABLE_BUILD_VALIDATION.equals(key))
         {
             return Boolean.valueOf(context.getNode("org.eclipse.jst.jsf.ui").getBoolean(key, true));
         }
         else if (KEY_ENABLE_INCREMENTAL_VALIDATION.equals(key))
         {
             return Boolean.valueOf(context.getNode("org.eclipse.jst.jsf.ui").getBoolean(key, false));
         }
         else
         {
             try
             {
                 return context.getNode("org.eclipse.jst.jsf.core").get(key, mapDiagToSeverity(getDefaultSeverity(getIdByKey(key))).toString());
             }
             catch (IllegalArgumentException e)
             {
                 // getIdByKey will throw this exception if key is not a valid
                 // severity key.  Ignore the exception here and fall-through
             }
         }
         
         return null; // not found
     }
 
     /* (non-Javadoc)
      * @see org.eclipse.jst.jsf.core.internal.IJSFPreferenceModel#setValueByKey(org.eclipse.core.runtime.preferences.IScopeContext, java.lang.String, java.lang.Object)
      */
     public Object setValueByKey(IScopeContext context, String key, Object value) 
     {
         // ignore context for now; will be used when we have project overrides
         if (KEY_ENABLE_BUILD_VALIDATION.equals(key))
         {
             boolean oldValue = isEnableBuildValidation();
             boolean newValue = ((Boolean)value).booleanValue();
             setEnableBuildValidation(newValue);
             return Boolean.valueOf(oldValue);
         }
         else if (KEY_ENABLE_INCREMENTAL_VALIDATION.equals(key))
         {
             boolean oldValue = isEnableIncrementalValidation();
             boolean newValue = ((Boolean)value).booleanValue();
             setEnableIncrementalValidation(newValue);
             return Boolean.valueOf(oldValue);
         }
         else
         {
             final Severity oldValue = getSeverity(key);
             setSeverity(key, (Severity)value);
             return oldValue;
         }
     }
 
     /**
      * @return the build validation enablement
      */
     public boolean isEnableBuildValidation() 
     {
         return _enableBuildValidation;
     }
 
     /**
      * @return the incremental validation enablement
      */
     public boolean isEnableIncrementalValidation() 
     {
         return _enableIncrementalValidation;
     }
 
     /**
      * @param enableBuildValidation
      */
     public void setEnableBuildValidation(boolean enableBuildValidation) {
         _enableBuildValidation = enableBuildValidation;
     }
 
     /**
      * @param enableIncrementalValidation
      */
     public void setEnableIncrementalValidation(boolean enableIncrementalValidation) {
         _enableIncrementalValidation = enableIncrementalValidation;
     }
     
     /**
      * @param key
      * @return the severity
      */
     public Severity getSeverity(final String key)
     {
         final int severityDiag = _severities[getIdByKey(key)];
         final Severity severity = mapDiagToSeverity(severityDiag);
         return severity;
     }
 
     /**
      * @param key
      * @param severity
      */
     public void setSeverity(final String key, final Severity severity)
     {
         final int newSeverityDiag = mapSeverityToDiag(severity.toString());
         final int diagId = getIdByKey(key);
         _severities[diagId] = newSeverityDiag;
     }
     
     /**
      * @param diagnosticId
      * @return the severity as configured for diagnosticId.  The value
      * is relative to the Diagnostic class severity scheme
      */
     public final int getDiagnosticSeverity(final int diagnosticId)
     {
         return getSeverities()[diagnosticId];
     }
     
     private int[] getSeverities()
     {
         if (_severities == null)
         {
             _severities = new int[DiagnosticFactory.NUM_IDS];
         }
         
         return _severities;
     }
     
     /**
      * @param diagSeverity
      * @return a Severity preference value for a diagnostic severity
      */
     public static Severity mapDiagToSeverity(int diagSeverity)
     {
         switch(diagSeverity)
         {
             case Diagnostic.ERROR:
                 return Severity.ERROR;
             case Diagnostic.WARNING:
                 return Severity.WARNING;
             default:
                 return Severity.IGNORE;
         }
     }
     
     /**
      * @param severity
      * @return a Diagnostic severity level for a severity pref string
      */
     public static int mapSeverityToDiag(String severity)
     {
         if ("error".equals(severity))
         {
             return Diagnostic.ERROR;
         }
         else if ("warning".equals(severity))
         {
             return Diagnostic.WARNING;
         }
         else if ("ignore".equals(severity))
         {
             return Diagnostic.OK;
         }
         else
         {
             throw new IllegalArgumentException("Invalid enum name: "+severity);
         }
     }
     
     /**
      * @param diagnosticId
      * @return the default severity of a diagnostic
      */
     public static int getDefaultSeverity(final int diagnosticId)
     {
         switch(diagnosticId)
         {
             case DiagnosticFactory.BINARY_OP_BOTH_OPERANDS_NULL_ID:
                 return Diagnostic.WARNING;
             case DiagnosticFactory.BINARY_OP_POSSIBLE_DIVISION_BY_ZERO_ID:
                 return Diagnostic.ERROR;
             case DiagnosticFactory.BINARY_OP_COULD_NOT_MAKE_NUMERIC_COERCION_ID:
                 return Diagnostic.ERROR;
             case DiagnosticFactory.BINARY_OP_CONSTANT_EXPRESSION_ALWAYS_EVAL_SAME_ID:
                 return Diagnostic.WARNING;
             case DiagnosticFactory.BINARY_OP_EQUALITY_COMP_WITH_NULL_ALWAYS_EVAL_SAME_ID:
                 return Diagnostic.WARNING;
             case DiagnosticFactory.BINARY_OP_CANNOT_COERCE_ARGUMENT_TO_BOOLEAN_ID:
                 return Diagnostic.ERROR;
             case DiagnosticFactory.BINARY_OP_FIRST_ARGUMENT_SHORT_CIRCUITS_ID:
                 return Diagnostic.WARNING;
             case DiagnosticFactory.BINARY_OP_SECOND_ARGUMENT_ALWAYS_EVAL_SAME_ID:
                 return Diagnostic.WARNING;
             case DiagnosticFactory.BINARY_OP_NO_AVAILABLE_TYPE_COERCION_ID:
                 return Diagnostic.ERROR;
             case DiagnosticFactory.BINARY_OP_COULD_NOT_COERCE_LITERALS_TO_NUMBERS_ID:
                 return Diagnostic.ERROR;
             case DiagnosticFactory.UNARY_OP_CONSTANT_EXPRESSION_EVAL_SAME_ID:
                 return Diagnostic.WARNING;
             case DiagnosticFactory.UNARY_OP_EMPTY_ALWAYS_FALSE_ON_TYPE_ID:
                 return Diagnostic.WARNING;
             case DiagnosticFactory.UNARY_OP_MINUS_ON_NULL_ALWAYS_ZERO_ID:
                 return Diagnostic.WARNING;
             case DiagnosticFactory.UNARY_OP_COULD_NOT_MAKE_NUMERIC_COERCION_ID:
                 return Diagnostic.ERROR;
             case DiagnosticFactory.UNARY_OP_CANNOT_COERCE_ARGUMENT_TO_BOOLEAN_ID:
                 return Diagnostic.ERROR;
             case DiagnosticFactory.TERNARY_OP_CHOICE_IS_ALWAYS_SAME_ID:
                 return Diagnostic.WARNING;
             case DiagnosticFactory.TERNARY_OP_CANNOT_COERCE_CHOICE_TO_BOOLEAN_ID:
                 return Diagnostic.ERROR;
             case DiagnosticFactory.UNARY_OP_STRING_CONVERSION_NOT_GUARANTEED_ID:
                 return Diagnostic.WARNING;
             case DiagnosticFactory.CANNOT_APPLY_OPERATOR_TO_METHOD_BINDING_ID:
                 return Diagnostic.ERROR;
             case DiagnosticFactory.MEMBER_NOT_FOUND_ID:
                 return Diagnostic.WARNING;
             case DiagnosticFactory.VARIABLE_NOT_FOUND_ID:
                 return Diagnostic.WARNING;
             case DiagnosticFactory.MISSING_CLOSING_EXPR_BRACKET_ID:
                 return Diagnostic.ERROR;
             case DiagnosticFactory.GENERAL_SYNTAX_ERROR_ID:
                 return Diagnostic.WARNING;
             case DiagnosticFactory.EMPTY_EL_EXPRESSION_ID:
                 return Diagnostic.WARNING;
             case DiagnosticFactory.BINARY_OP_DOT_WITH_VALUEB_NULL_ID:
                 return Diagnostic.WARNING;
             case DiagnosticFactory.BINARY_OP_DOT_WITH_DOTTED_KEY_SHOULD_USE_ARRAY_ID:
                 return Diagnostic.WARNING;
             case DiagnosticFactory.POSSIBLE_ARRAY_INDEX_OUT_OF_BOUNDS_ID:
                 return Diagnostic.WARNING;
             case DiagnosticFactory.BINARY_COMPARISON_WITH_ENUM_ALWAYS_SAME_ID:
                 return Diagnostic.WARNING;
             case DiagnosticFactory.BINARY_OP_COMPARISON_OF_ENUMS_INCOMPATIBLE_ID:
                 return Diagnostic.ERROR;
            case DiagnosticFactory.MEMBER_IS_INTERMEDIATE_ID:
                return Diagnostic.WARNING;
             default:
                 throw new IllegalArgumentException("Diagnostic Id: "+ diagnosticId +" is out of range");
         }
     }
 
     /**
      * @param diagnosticId
      * @return the preference key for the corresponding diagnosticId in the el DiagnosticFactory
      */
     public static String getKeyById(final int diagnosticId)
     {
         switch(diagnosticId)
         {
             case DiagnosticFactory.BINARY_OP_BOTH_OPERANDS_NULL_ID:
                 return BINARY_OP_BOTH_OPERANDS_NULL;
             case DiagnosticFactory.BINARY_OP_POSSIBLE_DIVISION_BY_ZERO_ID:
                 return BINARY_OP_POSSIBLE_DIVISION_BY_ZERO;
             case DiagnosticFactory.BINARY_OP_COULD_NOT_MAKE_NUMERIC_COERCION_ID:
                 return BINARY_OP_COULD_NOT_MAKE_NUMERIC_COERCION;
             case DiagnosticFactory.BINARY_OP_CONSTANT_EXPRESSION_ALWAYS_EVAL_SAME_ID:
                 return BINARY_OP_CONSTANT_EXPRESSION_ALWAYS_EVAL_SAME;
             case DiagnosticFactory.BINARY_OP_EQUALITY_COMP_WITH_NULL_ALWAYS_EVAL_SAME_ID:
                 return BINARY_OP_EQUALITY_COMP_WITH_NULL_ALWAYS_EVAL_SAME;
             case DiagnosticFactory.BINARY_OP_CANNOT_COERCE_ARGUMENT_TO_BOOLEAN_ID:
                 return BINARY_OP_CANNOT_COERCE_ARGUMENT_TO_BOOLEAN;
             case DiagnosticFactory.BINARY_OP_FIRST_ARGUMENT_SHORT_CIRCUITS_ID:
                 return BINARY_OP_FIRST_ARGUMENT_SHORT_CIRCUITS;
             case DiagnosticFactory.BINARY_OP_SECOND_ARGUMENT_ALWAYS_EVAL_SAME_ID:
                 return BINARY_OP_SECOND_ARGUMENT_ALWAYS_EVAL_SAME;
             case DiagnosticFactory.BINARY_OP_NO_AVAILABLE_TYPE_COERCION_ID:
                 return BINARY_OP_NO_AVAILABLE_TYPE_COERCION;
             case DiagnosticFactory.BINARY_OP_COULD_NOT_COERCE_LITERALS_TO_NUMBERS_ID:
                 return BINARY_OP_COULD_NOT_COERCE_LITERALS_TO_NUMBERS;
             case DiagnosticFactory.UNARY_OP_CONSTANT_EXPRESSION_EVAL_SAME_ID:
                 return UNARY_OP_CONSTANT_EXPRESSION_EVAL_SAME;
             case DiagnosticFactory.UNARY_OP_EMPTY_ALWAYS_FALSE_ON_TYPE_ID:
                 return UNARY_OP_EMPTY_ALWAYS_FALSE_ON_TYPE;
             case DiagnosticFactory.UNARY_OP_MINUS_ON_NULL_ALWAYS_ZERO_ID:
                 return UNARY_OP_MINUS_ON_NULL_ALWAYS_ZERO;
             case DiagnosticFactory.UNARY_OP_COULD_NOT_MAKE_NUMERIC_COERCION_ID:
                 return UNARY_OP_COULD_NOT_MAKE_NUMERIC_COERCION;
             case DiagnosticFactory.UNARY_OP_CANNOT_COERCE_ARGUMENT_TO_BOOLEAN_ID:
                 return UNARY_OP_CANNOT_COERCE_ARGUMENT_TO_BOOLEAN;
             case DiagnosticFactory.TERNARY_OP_CHOICE_IS_ALWAYS_SAME_ID:
                 return TERNARY_OP_CHOICE_IS_ALWAYS_SAME;
             case DiagnosticFactory.TERNARY_OP_CANNOT_COERCE_CHOICE_TO_BOOLEAN_ID:
                 return TERNARY_OP_CANNOT_COERCE_CHOICE_TO_BOOLEAN;
             case DiagnosticFactory.UNARY_OP_STRING_CONVERSION_NOT_GUARANTEED_ID:
                 return UNARY_OP_STRING_CONVERSION_NOT_GUARANTEED;
             case DiagnosticFactory.CANNOT_APPLY_OPERATOR_TO_METHOD_BINDING_ID:
                 return CANNOT_APPLY_OPERATOR_TO_METHOD_BINDING;
             case DiagnosticFactory.MEMBER_NOT_FOUND_ID:
                 return MEMBER_NOT_FOUND;
             case DiagnosticFactory.VARIABLE_NOT_FOUND_ID:
                 return VARIABLE_NOT_FOUND;
             case DiagnosticFactory.MISSING_CLOSING_EXPR_BRACKET_ID:
                 return MISSING_CLOSING_EXPR_BRACKET;
             case DiagnosticFactory.GENERAL_SYNTAX_ERROR_ID:
                 return GENERAL_SYNTAX_ERROR;
             case DiagnosticFactory.EMPTY_EL_EXPRESSION_ID:
                 return EMPTY_EL_EXPRESSION;
             case DiagnosticFactory.BINARY_OP_DOT_WITH_VALUEB_NULL_ID:
                 return BINARY_OP_DOT_WITH_VALUEB_NULL;
             case DiagnosticFactory.BINARY_OP_DOT_WITH_DOTTED_KEY_SHOULD_USE_ARRAY_ID:
                 return BINARY_OP_DOT_WITH_DOTTED_KEY_SHOULD_USE_ARRAY;
             case DiagnosticFactory.POSSIBLE_ARRAY_INDEX_OUT_OF_BOUNDS_ID:
                 return POSSIBLE_ARRAY_INDEX_OUT_OF_BOUNDS;
             case DiagnosticFactory.BINARY_COMPARISON_WITH_ENUM_ALWAYS_SAME_ID:
                 return BINARY_COMPARISON_WITH_ENUM_ALWAYS_SAME;
             case DiagnosticFactory.BINARY_OP_COMPARISON_OF_ENUMS_INCOMPATIBLE_ID:
                 return BINARY_OP_COMPARISON_OF_ENUMS_INCOMPATIBLE;
            case DiagnosticFactory.MEMBER_IS_INTERMEDIATE_ID:
                return MEMBER_IS_INTERMEDIATE;
             default:
                 throw new IllegalArgumentException("Diagnostic Id: "+ diagnosticId +" is out of range");
         }
     }
     
     /**
      * @param key
      * @return the preference key for the corresponding diagnosticId in the el DiagnosticFactory
      */
     public static int getIdByKey(final String key)
     {
         if (BINARY_OP_BOTH_OPERANDS_NULL.equals(key))
         {
             return DiagnosticFactory.BINARY_OP_BOTH_OPERANDS_NULL_ID;
         }
         else if (BINARY_OP_POSSIBLE_DIVISION_BY_ZERO.equals(key))
         {
             return DiagnosticFactory.BINARY_OP_POSSIBLE_DIVISION_BY_ZERO_ID;
         }
         else if (BINARY_OP_COULD_NOT_MAKE_NUMERIC_COERCION.equals(key))
         {
             return DiagnosticFactory.BINARY_OP_COULD_NOT_MAKE_NUMERIC_COERCION_ID;
         }
         else if (BINARY_OP_CONSTANT_EXPRESSION_ALWAYS_EVAL_SAME.equals(key))
         {
             return DiagnosticFactory.BINARY_OP_CONSTANT_EXPRESSION_ALWAYS_EVAL_SAME_ID;
         }
         else if (BINARY_OP_EQUALITY_COMP_WITH_NULL_ALWAYS_EVAL_SAME.equals(key))
         {
             return DiagnosticFactory.BINARY_OP_EQUALITY_COMP_WITH_NULL_ALWAYS_EVAL_SAME_ID;
         }
         else if (BINARY_OP_CANNOT_COERCE_ARGUMENT_TO_BOOLEAN.equals(key))
         {
             return DiagnosticFactory.BINARY_OP_CANNOT_COERCE_ARGUMENT_TO_BOOLEAN_ID;
         }
         else if (BINARY_OP_FIRST_ARGUMENT_SHORT_CIRCUITS.equals(key))
         {
             return DiagnosticFactory.BINARY_OP_FIRST_ARGUMENT_SHORT_CIRCUITS_ID;
         }
         else if (BINARY_OP_SECOND_ARGUMENT_ALWAYS_EVAL_SAME.equals(key))
         {
             return DiagnosticFactory.BINARY_OP_SECOND_ARGUMENT_ALWAYS_EVAL_SAME_ID;
         }
         else if (BINARY_OP_NO_AVAILABLE_TYPE_COERCION.equals(key))
         {
             return DiagnosticFactory.BINARY_OP_NO_AVAILABLE_TYPE_COERCION_ID;
         }
         else if (BINARY_OP_COULD_NOT_COERCE_LITERALS_TO_NUMBERS.equals(key))
         {
             return DiagnosticFactory.BINARY_OP_COULD_NOT_COERCE_LITERALS_TO_NUMBERS_ID;
         }
         else if (UNARY_OP_CONSTANT_EXPRESSION_EVAL_SAME.equals(key))
         {
             return DiagnosticFactory.UNARY_OP_CONSTANT_EXPRESSION_EVAL_SAME_ID;
         }
         else if (UNARY_OP_EMPTY_ALWAYS_FALSE_ON_TYPE.equals(key))
         {
             return DiagnosticFactory.UNARY_OP_EMPTY_ALWAYS_FALSE_ON_TYPE_ID;
         }
         else if (UNARY_OP_MINUS_ON_NULL_ALWAYS_ZERO.equals(key))
         {
             return DiagnosticFactory.UNARY_OP_MINUS_ON_NULL_ALWAYS_ZERO_ID;
         }
         else if (UNARY_OP_COULD_NOT_MAKE_NUMERIC_COERCION.equals(key))
         {
             return DiagnosticFactory.UNARY_OP_COULD_NOT_MAKE_NUMERIC_COERCION_ID;
         }
         else if (UNARY_OP_CANNOT_COERCE_ARGUMENT_TO_BOOLEAN.equals(key))
         {
             return DiagnosticFactory.UNARY_OP_CANNOT_COERCE_ARGUMENT_TO_BOOLEAN_ID;
         }
         else if (TERNARY_OP_CHOICE_IS_ALWAYS_SAME.equals(key))
         {
             return DiagnosticFactory.TERNARY_OP_CHOICE_IS_ALWAYS_SAME_ID;
         }
         else if (TERNARY_OP_CANNOT_COERCE_CHOICE_TO_BOOLEAN.equals(key))
         {
             return DiagnosticFactory.TERNARY_OP_CANNOT_COERCE_CHOICE_TO_BOOLEAN_ID;
         }
         else if (UNARY_OP_STRING_CONVERSION_NOT_GUARANTEED.equals(key))
         {
             return DiagnosticFactory.UNARY_OP_STRING_CONVERSION_NOT_GUARANTEED_ID;
         }
         else if (CANNOT_APPLY_OPERATOR_TO_METHOD_BINDING.equals(key))
         {
             return DiagnosticFactory.CANNOT_APPLY_OPERATOR_TO_METHOD_BINDING_ID;
         }
         else if (MEMBER_NOT_FOUND.equals(key))
         {
             return DiagnosticFactory.MEMBER_NOT_FOUND_ID;
         }
         else if (VARIABLE_NOT_FOUND.equals(key))
         {
             return DiagnosticFactory.VARIABLE_NOT_FOUND_ID;
         }
         else if (MISSING_CLOSING_EXPR_BRACKET.equals(key))
         {
             return DiagnosticFactory.MISSING_CLOSING_EXPR_BRACKET_ID;
         }
         else if (GENERAL_SYNTAX_ERROR.equals(key))
         {
             return DiagnosticFactory.GENERAL_SYNTAX_ERROR_ID;
         }
         else if (EMPTY_EL_EXPRESSION.equals(key))
         {
            return DiagnosticFactory.EMPTY_EL_EXPRESSION_ID;
         }
         else if (BINARY_OP_DOT_WITH_VALUEB_NULL.equals(key))
         {
             return DiagnosticFactory.BINARY_OP_DOT_WITH_VALUEB_NULL_ID;
         }
         else if (BINARY_OP_DOT_WITH_DOTTED_KEY_SHOULD_USE_ARRAY.equals(key))
         {
             return DiagnosticFactory.BINARY_OP_DOT_WITH_DOTTED_KEY_SHOULD_USE_ARRAY_ID;
         }
         else if (POSSIBLE_ARRAY_INDEX_OUT_OF_BOUNDS.equals(key))
         {
             return DiagnosticFactory.POSSIBLE_ARRAY_INDEX_OUT_OF_BOUNDS_ID;
         }
         else if (BINARY_COMPARISON_WITH_ENUM_ALWAYS_SAME.equals(key))
         {
             return DiagnosticFactory.BINARY_COMPARISON_WITH_ENUM_ALWAYS_SAME_ID;
         }
         else if (BINARY_OP_COMPARISON_OF_ENUMS_INCOMPATIBLE.equals(key))
         {
             return DiagnosticFactory.BINARY_OP_COMPARISON_OF_ENUMS_INCOMPATIBLE_ID;
         }
        else if (MEMBER_IS_INTERMEDIATE.equals(key))
        {
            return DiagnosticFactory.MEMBER_IS_INTERMEDIATE_ID;
        }
         else
         {
             throw new IllegalArgumentException("Severity Key: "+ key);
         }
     }
     
     /**
      * e.g. createQualifiedKeyName("foo") -> org.eclipse.jst.jsf.core.foo
      * @param baseName
      * @return a plugin qualified key given the baseName
      * 
      */
     private static String createQualifiedKeyName(final String baseName)
     {
         return JSFCorePlugin.PLUGIN_ID + "." + baseName;
     }
     
     /**
      * preference key.  Match to DiagnosticFactory constants
      */
     public final static String BINARY_OP_BOTH_OPERANDS_NULL = 
         createQualifiedKeyName("BINARY_OP_BOTH_OPERANDS_NULL");
     /**
      * preference key.  Match to DiagnosticFactory constants
      */
     public final static String BINARY_OP_POSSIBLE_DIVISION_BY_ZERO = 
         createQualifiedKeyName("BINARY_OP_POSSIBLE_DIVISION_BY_ZERO");
     /**
      * preference key.  Match to DiagnosticFactory constants
      */
     public final static String BINARY_OP_COULD_NOT_MAKE_NUMERIC_COERCION = 
         createQualifiedKeyName("BINARY_OP_COULD_NOT_MAKE_NUMERIC_COERCION");
     /**
      * preference key.  Match to DiagnosticFactory constants
      */
     public final static String BINARY_OP_CONSTANT_EXPRESSION_ALWAYS_EVAL_SAME = 
         createQualifiedKeyName("BINARY_OP_CONSTANT_EXPRESSION_ALWAYS_EVAL_SAME");
     /**
      * preference key.  Match to DiagnosticFactory constants
      */
     public final static String BINARY_OP_EQUALITY_COMP_WITH_NULL_ALWAYS_EVAL_SAME = 
         createQualifiedKeyName("BINARY_OP_EQUALITY_COMP_WITH_NULL_ALWAYS_EVAL_SAME");
     /**
      * preference key.  Match to DiagnosticFactory constants
      */
     public final static String BINARY_OP_CANNOT_COERCE_ARGUMENT_TO_BOOLEAN = 
         createQualifiedKeyName("BINARY_OP_CANNOT_COERCE_ARGUMENT_TO_BOOLEAN");
     /**
      * preference key.  Match to DiagnosticFactory constants
      */
     public final static String BINARY_OP_FIRST_ARGUMENT_SHORT_CIRCUITS = 
         createQualifiedKeyName("BINARY_OP_FIRST_ARGUMENT_SHORT_CIRCUITS");
     /**
      * preference key.  Match to DiagnosticFactory constants
      */
     public final static String BINARY_OP_SECOND_ARGUMENT_ALWAYS_EVAL_SAME =
         createQualifiedKeyName("BINARY_OP_SECOND_ARGUMENT_ALWAYS_EVAL_SAME");
     /**
      * preference key.  Match to DiagnosticFactory constants
      */
     public final static String BINARY_OP_NO_AVAILABLE_TYPE_COERCION = 
         createQualifiedKeyName("BINARY_OP_NO_AVAILABLE_TYPE_COERCION");
     /**
      * preference key.  Match to DiagnosticFactory constants
      */
     public final static String BINARY_OP_COULD_NOT_COERCE_LITERALS_TO_NUMBERS = 
         createQualifiedKeyName("BINARY_OP_COULD_NOT_COERCE_LITERALS_TO_NUMBERS");
     /**
      * preference key.  Match to DiagnosticFactory constants
      */
     public final static String UNARY_OP_CONSTANT_EXPRESSION_EVAL_SAME = 
         createQualifiedKeyName("UNARY_OP_CONSTANT_EXPRESSION_EVAL_SAME");
     /**
      * preference key.  Match to DiagnosticFactory constants
      */
     public final static String UNARY_OP_EMPTY_ALWAYS_FALSE_ON_TYPE = 
         createQualifiedKeyName("UNARY_OP_EMPTY_ALWAYS_FALSE_ON_TYPE");
     /**
      * preference key.  Match to DiagnosticFactory constants
      */
     public final static String UNARY_OP_MINUS_ON_NULL_ALWAYS_ZERO = 
         createQualifiedKeyName("UNARY_OP_MINUS_ON_NULL_ALWAYS_ZERO");
     /**
      * preference key.  Match to DiagnosticFactory constants
      */
     public final static String UNARY_OP_COULD_NOT_MAKE_NUMERIC_COERCION = 
         createQualifiedKeyName("UNARY_OP_COULD_NOT_MAKE_NUMERIC_COERCION");
     /**
      * preference key.  Match to DiagnosticFactory constants
      */
     public final static String UNARY_OP_CANNOT_COERCE_ARGUMENT_TO_BOOLEAN = 
         createQualifiedKeyName("UNARY_OP_CANNOT_COERCE_ARGUMENT_TO_BOOLEAN");
     /**
      * preference key.  Match to DiagnosticFactory constants
      */
     public final static String TERNARY_OP_CHOICE_IS_ALWAYS_SAME = 
         createQualifiedKeyName("TERNARY_OP_CHOICE_IS_ALWAYS_SAME");
     /**
      * preference key.  Match to DiagnosticFactory constants
      */
     public final static String TERNARY_OP_CANNOT_COERCE_CHOICE_TO_BOOLEAN = 
         createQualifiedKeyName("TERNARY_OP_CANNOT_COERCE_CHOICE_TO_BOOLEAN"); 
     /**
      * preference key.  Match to DiagnosticFactory constants
      */
     public final static String UNARY_OP_STRING_CONVERSION_NOT_GUARANTEED = 
         createQualifiedKeyName("UNARY_OP_STRING_CONVERSION_NOT_GUARANTEED");
     /**
      * preference key.  Match to DiagnosticFactory constants
      */
     public final static String CANNOT_APPLY_OPERATOR_TO_METHOD_BINDING = 
         createQualifiedKeyName("CANNOT_APPLY_OPERATOR_TO_METHOD_BINDING");
     /**
      * preference key.  Match to DiagnosticFactory constants
      */
     public final static String MEMBER_NOT_FOUND = 
         createQualifiedKeyName("MEMBER_NOT_FOUND");
     /**
      * preference key.  Match to DiagnosticFactory constants
      */
     public final static String VARIABLE_NOT_FOUND = 
         createQualifiedKeyName("VARIABLE_NOT_FOUND");
     /**
      * preference key.  Match to DiagnosticFactory constants
      */
     public final static String MISSING_CLOSING_EXPR_BRACKET = 
         createQualifiedKeyName("MISSING_CLOSING_EXPR_BRACKET");
     /**
      * preference key.  Match to DiagnosticFactory constants
      */
     public final static String GENERAL_SYNTAX_ERROR = 
         createQualifiedKeyName("GENERAL_SYNTAX_ERROR");
     /**
      * preference key.  Match to DiagnosticFactory constants
      */
     public final static String EMPTY_EL_EXPRESSION = 
         createQualifiedKeyName("EMPTY_EL_EXPRESSION");
     /**
      * preference key.  Match to DiagnosticFactory constants
      */
     public final static String BINARY_OP_DOT_WITH_VALUEB_NULL = 
         createQualifiedKeyName("BINARY_OP_DOT_WITH_VALUEB_NULL");
     /**
      * preference key.  Match to DiagnosticFactory constants
      */
     public final static String BINARY_OP_DOT_WITH_DOTTED_KEY_SHOULD_USE_ARRAY = 
         createQualifiedKeyName("BINARY_OP_DOT_WITH_DOTTED_KEY_SHOULD_USE_ARRAY");
     /**
      * preference key.  Match to DiagnosticFactory constants
      */
     public final static String POSSIBLE_ARRAY_INDEX_OUT_OF_BOUNDS = 
         createQualifiedKeyName("POSSIBLE_ARRAY_INDEX_OUT_OF_BOUNDS");
     /**
      * preference key.  Match to DiagnosticFactory constants
      */
     public final static String BINARY_COMPARISON_WITH_ENUM_ALWAYS_SAME = 
         createQualifiedKeyName("BINARY_COMPARISON_WITH_ENUM_ALWAYS_SAME");
     /**
      * preference key.  Match to DiagnosticFactory constants
      */
     public final static String BINARY_OP_COMPARISON_OF_ENUMS_INCOMPATIBLE = 
         createQualifiedKeyName("BINARY_OP_COMPARISON_OF_ENUMS_INCOMPATIBLE");
     
    /**
     * preference key.  Match to DiagnosticFactory constants
     */
    public final static String MEMBER_IS_INTERMEDIATE =
        createQualifiedKeyName("MEMBER_IS_INTERMEDIATE");
    
     // values
     /**
      * Severity levels of problems
      */
     public enum Severity
     {
         /**
          * Indicates a problem severity of Error
          */
         ERROR 
         {
             public java.lang.String toString() {
                 return "error";
             }
         }
         ,
         /**
          * Indicates a problem severity of Warning
          */
         WARNING
         {
             public java.lang.String toString() {
                 return "warning";
             }
         }
         , 
         /**
          * Indicates no problem should be shown
          */
         IGNORE
         {
             public java.lang.String toString() {
                 return "ignore";
             }
         };
         
         /**
          * @param value
          * @return the enum based on the toString mappings
          */
         public static Severity valueOfString(String value)
         {
             if ("error".equals(value))
             {
                 return ERROR;
             }
             else if ("warning".equals(value))
             {
                 return WARNING;
             }
             else
             {
                 return IGNORE;
             }
         }
      }
 }
