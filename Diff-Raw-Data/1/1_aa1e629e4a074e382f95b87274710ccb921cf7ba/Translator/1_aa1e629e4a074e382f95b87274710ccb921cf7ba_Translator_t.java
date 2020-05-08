 /**
  * Copyright (c) 2011. Physion Consulting LLC
  * All rights reserved.
  */
 package com.physion.ebuilder.translator;
 
 
 /**
  * An interface for the ExpressionTreeToRowData and RowDataToExpressionTree
  * translators that holds some constant values that both classes use.
  */
 public interface Translator {
 
     public static final String CLVE_BOOLEAN = "ovation.BooleanValue";
     public static final String CLVE_STRING = "ovation.StringValue";
     public static final String CLVE_INTEGER = "ovation.IntegerValue";
     public static final String CLVE_FLOAT = "ovation.FloatingPointValue";
     public static final String CLVE_DATE = "ovation.DateValue";
 
     public static final String AE_VALUE = "value";
     public static final String AE_THIS = "this";
 
     public static final String OE_NOT = "not";
     public static final String OE_OR = "or";
     public static final String OE_AND = "and";
     public static final String OE_ALL = "all";
     public static final String OE_COUNT = "count";
     public static final String OE_AS = "as";
     public static final String OE_PARAMETER = "parameter";
     public static final String OE_ANY = "any";
     public static final String OE_ELEMENTS_OF_TYPE = "elementsOfType";
    public static final String OE_CONTAINING_EXPERIMENTS = "containing_experiments";
 
     public static final String OE_EQUALS = "==";
     public static final String OE_NOT_EQUALS = "!=";
     public static final String OE_LESS_THAN = "<";
     public static final String OE_GREATER_THAN = ">";
     public static final String OE_LESS_THAN_EQUALS = "<=";
     public static final String OE_GREATER_THAN_EQUALS = ">=";
     public static final String OE_MATCHES_CASE_SENSITIVE = "=~";
     public static final String OE_MATCHES_CASE_INSENSITIVE = "=~~";
     public static final String OE_DOES_NOT_MATCH_CASE_SENSITIVE = "!~";
     public static final String OE_DOES_NOT_MATCH_CASE_INSENSITIVE = "!~~";
 
     public static final String OE_IS_NULL = "isnull";
     // Note there is no OE_IS_NOT_NULL value.
 
     public static final String OE_DOT = ".";
 }
