 /*
  * CDDL HEADER START
  *
  * The contents of this file are subject to the terms of the
  * Common Development and Distribution License, Version 1.0 only
  * (the "License").  You may not use this file except in compliance
  * with the License.
  *
  * You can obtain a copy of the license at license/ESCIDOC.LICENSE
  * or http://www.escidoc.de/license.
  * See the License for the specific language governing permissions
  * and limitations under the License.
  *
  * When distributing Covered Code, include this CDDL HEADER in each
  * file and include the License file at license/ESCIDOC.LICENSE.
  * If applicable, add the following below this CDDL HEADER, with the
  * fields enclosed by brackets "[]" replaced with your own identifying
  * information: Portions Copyright [yyyy] [name of copyright owner]
  *
  * CDDL HEADER END
  */
 
 /*
  * Copyright 2008 Fachinformationszentrum Karlsruhe Gesellschaft
  * fuer wissenschaftlich-technische Information mbH and Max-Planck-
  * Gesellschaft zur Foerderung der Wissenschaft e.V.
  * All rights reserved.  Use is subject to license terms.
  */
 package de.escidoc.core.aa.convert;
 
 import com.sun.xacml.attr.AttributeDesignator;
 import com.sun.xacml.attr.StringAttribute;
 import com.sun.xacml.cond.Apply;
 import de.escidoc.core.common.business.fedora.resources.Values;
 
 import java.util.List;
 import java.util.regex.Pattern;
 
 /**
  * This is a helper class to convert a XACML condition into an SQL fragment.
  *
  * @author André Schenk
  */
 public class ConditionParser {
 
    private static final Pattern SPLIT_PATTERN = Pattern.compile(" ");
 
     private Values values;
 
     /**
      * Extract the attribute name from a function URI.
      *
      * @param function function URI
      * @return the last part of the URI path
      */
     private static String getAttribute(final String function) {
         String result = function;
 
         if (function != null) {
             final int index = function.lastIndexOf(':');
 
             if (index >= 0 && index < function.length() - 1) {
                 result = function.substring(index + 1);
             }
         }
         return result;
     }
 
     /**
      * Inner class that stores a tuple of the form (function, operand1, operand2).
      */
     private static final class Function {
 
         private final String operation;
 
         private final String operand1;
 
         private final String operand2;
 
         /**
          * Constructor.
          *
          * @param operation operation
          * @param operand1  first operand
          * @param operand2  second operand
          */
         private Function(final String operation, final String operand1, final String operand2) {
             this.operation = operation;
             this.operand1 = operand1;
             this.operand2 = operand2;
         }
 
         /**
          * Get a string representation of this object.
          *
          * @return string representation of this object
          */
         public String toString() {
             return "[operation=" + this.operation + ", operand1=" + this.operand1 + ",operand2=" + this.operand2 + ']';
         }
     }
 
     /**
      * Parse an Apply object.
      *
      * @param condition apply object
      * @return tuple of the form (function, operand1, operand2)
      */
     private Function parseApply(final Apply condition) {
         Function result = null;
 
         if (condition != null) {
             final String operation = condition.getFunction().getIdentifier().toString();
 
             final List<?> children = condition.getChildren();
 
             if (children != null) {
                 if (children.size() == 1) {
                     if (operation.equals(Values.FUNCTION_STRING_ONE_AND_ONLY)) {
                         if (children.get(0) instanceof AttributeDesignator) {
                             result =
                                 new Function(operation, ((AttributeDesignator) children.get(0)).getId().toString(),
                                     null);
                         }
                         else {
                             throw new IllegalArgumentException(children.get(0).getClass().getName()
                                 + ": unexpected operand type");
                         }
                     }
                     else {
                         throw new IllegalArgumentException(operation + ": unexpected function");
                     }
                 }
                 else if (children.size() == 2) {
                     if (operation.equals(Values.FUNCTION_STRING_CONTAINS)) {
                         if (children.get(0) instanceof StringAttribute && children.get(1) instanceof Apply) {
                             final Function nestedFunction = parseApply((Apply) children.get(1));
 
                             if (nestedFunction.operation.equals(Values.FUNCTION_STRING_ONE_AND_ONLY)) {
                                 final String operand1 = values.getOperand(getAttribute(nestedFunction.operand1));
 
                                 if (operand1 == null) {
                                     throw new IllegalArgumentException(nestedFunction.operand1 + ": unknown operand");
                                 }
                                 result =
                                     new Function(operation, parseContains(((StringAttribute) children.get(0))
                                         .getValue(), operand1), null);
                             }
                             else {
                                 throw new IllegalArgumentException(nestedFunction.operation + ": unexpected function");
                             }
                         }
                         else {
                             throw new IllegalArgumentException(children.get(0).getClass().getName() + " or "
                                 + children.get(1).getClass().getName() + ": unexpected operand type");
                         }
                     }
                     else {
                         final String operand1;
 
                         if (children.get(0) instanceof Apply) {
                             operand1 = parseApply((Apply) children.get(0)).operand1;
                         }
                         else if (children.get(0) instanceof StringAttribute) {
                             operand1 = ((StringAttribute) children.get(0)).getValue();
                         }
                         else {
                             throw new IllegalArgumentException(children.get(0).getClass().getName()
                                 + ": unexpected operand type");
                         }
                         final String operand2;
                         if (children.get(1) instanceof Apply) {
                             operand2 = parseApply((Apply) children.get(1)).operand1;
                         }
                         else if (children.get(1) instanceof StringAttribute) {
                             operand2 = ((StringAttribute) children.get(1)).getValue();
                         }
                         else {
                             throw new IllegalArgumentException(children.get(1).getClass().getName()
                                 + ": unexpected operand type");
                         }
                         result = new Function(operation, operand1, operand2);
                     }
                 }
                 else {
                     throw new IllegalArgumentException("operation with " + children.size() + " operands not allowed");
                 }
             }
             else {
                 throw new IllegalArgumentException("missing children");
             }
         }
         return result;
     }
 
     /**
      * Parse the given condition and convert it into SQL / Lucene.
      *
      * @param condition XACML condition
      * @return SQL fragment representing the XACML condition
      */
     public String parse(final Apply condition) {
         final StringBuilder result = new StringBuilder();
         final Function function = parseApply(condition);
 
         if (function != null) {
             final String sqlFunction = values.getFunction(function.operation);
 
             if (sqlFunction != null) {
                 result.append('(');
                 if (function.operation.equals(Values.FUNCTION_AND)) {
                     result.append(values.getContainsCondition(values.getAndCondition(function.operand1,
                         function.operand2)));
                 }
                 else if (function.operation.equals(Values.FUNCTION_STRING_CONTAINS)) {
                     result.append(values.getContainsCondition(function.operand1));
                 }
                 else if (function.operation.equals(Values.FUNCTION_STRING_EQUAL)) {
                     final String operand1 = values.getOperand(getAttribute(function.operand1));
 
                     if (operand1 == null) {
                         throw new IllegalArgumentException(function.operand1 + ": unknown operand");
                     }
 
                     final String operand2 = values.getOperand(getAttribute(function.operand2));
 
                     if (operand2 == null) {
                         throw new IllegalArgumentException(function.operand2 + ": unknown operand");
                     }
 
                     if (operand1.equals(Values.USER_ID)) {
                         result.append(values.getEqualCondition(operand2, operand1));
                     }
                     else if (operand2.equals(Values.USER_ID)) {
                         result.append(values.getEqualCondition(operand1, operand2));
                     }
                     else {
                         throw new IllegalArgumentException(function + ": unknown function");
                     }
                 }
                 else if (function.operation.equals(Values.FUNCTION_STRING_ONE_AND_ONLY)) {
                     result.append(function.operand1);
                 }
                 else {
                     throw new IllegalArgumentException(function + ": unknown function");
                 }
                 result.append(')');
             }
             else {
                 throw new IllegalArgumentException(function + ": unknown function");
             }
         }
         return result.toString();
     }
 
     /**
      * Parse the self defined function "string-contains" and create an SQL snippet from it.
      *
      * @param list  list of possible values
      * @param value value which must match one of the values given in the above list
      * @return SQL equivalent for that function
      */
     private String parseContains(final String list, final String value) {
         String result = "";
         final String[] listValues = SPLIT_PATTERN.split(list);
 
         for (final String listvalue : listValues) {
             result =
                 result.length() > 0 ? values.getOrCondition(result, values.getKeyValueCondition(value, listvalue)) : values
                     .getKeyValueCondition(value, listvalue);
         }
         return result;
     }
 
     /**
      * Injects the filter values object.
      *
      * @param values filter values object from Spring
      */
     public void setValues(final Values values) {
         this.values = values;
     }
 }
