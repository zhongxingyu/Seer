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
 
 package org.eclipse.jst.jsf.validation.internal.el.operators;
 
 import java.math.BigDecimal;
 import java.math.BigInteger;
 
 import org.eclipse.emf.common.util.Diagnostic;
 import org.eclipse.jdt.core.IType;
 import org.eclipse.jst.jsf.common.internal.types.BooleanLiteralType;
 import org.eclipse.jst.jsf.common.internal.types.IAssignable;
 import org.eclipse.jst.jsf.common.internal.types.LiteralType;
 import org.eclipse.jst.jsf.common.internal.types.StringLiteralType;
 import org.eclipse.jst.jsf.common.internal.types.TypeCoercer;
 import org.eclipse.jst.jsf.common.internal.types.TypeCoercionException;
 import org.eclipse.jst.jsf.common.internal.types.TypeConstants;
 import org.eclipse.jst.jsf.common.internal.types.TypeTransformer;
 import org.eclipse.jst.jsf.common.internal.types.ValueType;
 import org.eclipse.jst.jsf.common.util.TypeUtil;
 import org.eclipse.jst.jsf.context.symbol.internal.util.IObjectSymbolBasedValueType;
 import org.eclipse.jst.jsf.validation.internal.el.diagnostics.DiagnosticFactory;
 
 /**
  * A relational binary operator for equality: "==" or "!="
  * 
  * @author cbateman
  *
  */
 /*package*/ abstract class EqualityRelationalBinaryOperator extends RelationalBinaryOperator 
 {
     EqualityRelationalBinaryOperator(final DiagnosticFactory diagnosticFactory, String jsfVersion) 
     {
         super(diagnosticFactory, jsfVersion);
     }
 
     /**
      * @param firstArg
      * @param secondArg
      * @return the result of the operation
      */
     protected abstract boolean doRealOperation(Boolean firstArg, Boolean secondArg);
 
     /* (non-Javadoc)
      * @see org.eclipse.jst.jsf.validation.internal.el.operators.BinaryOperator#performOperation(org.eclipse.jst.jsf.core.internal.types.ValueType, org.eclipse.jst.jsf.core.internal.types.ValueType)
      */
     public ValueType performOperation(ValueType firstArg, ValueType secondArg) 
     {
        // JSP.2.3.5.7 step 1 if operands are equal, then true for ==, false for !=
         if (TypeCoercer.typeIsNull(firstArg.getSignature())
                && TypeCoercer.typeIsNull(secondArg.getSignature()))
         {
            // perform the operation on two arguments that are equal.
            return BooleanLiteralType.valueOf(doRealOperation(Integer.valueOf(4), Integer.valueOf(4)));
         }
         
         String boxedFirstType = TypeTransformer.transformBoxPrimitives(firstArg.getSignature());
         String boxedSecondType = TypeTransformer.transformBoxPrimitives(secondArg.getSignature());
         
         // JSP.2.3.5.7 step 3, if either is BigDecimal, promote both and compare
         if (TypeConstants.TYPE_BIG_DOUBLE.equals(boxedFirstType)
                 || TypeConstants.TYPE_BIG_DOUBLE.equals(boxedSecondType))
         {
             return handleNumericComparison(firstArg, secondArg, BigDecimal.class);
         }
         
         // JSP.2.3.5.7, step 4 if either is a float or double, promote both to 
         // double and compare
         if (TypeConstants.TYPE_BOXED_DOUBLE.equals(boxedFirstType)
                 || TypeConstants.TYPE_BOXED_FLOAT.equals(boxedFirstType)
                 || TypeConstants.TYPE_BOXED_DOUBLE.equals(boxedSecondType)
                 || TypeConstants.TYPE_BOXED_FLOAT.equals(boxedSecondType))
         {
             return handleNumericComparison(firstArg, secondArg, Double.class);
         }
         
         // JSP.2.3.5.7, step 5 if either is a big integer, promote and compare
         if (TypeConstants.TYPE_BIG_INTEGER.equals(boxedFirstType)
                 || TypeConstants.TYPE_BIG_INTEGER.equals(boxedSecondType))
         {
             return handleNumericComparison(firstArg, secondArg, BigInteger.class);
         }
         
         // JSP.2.3.5.7, step 6 if either is Long or smaller, coerce both to Long
         if (TypeConstants.TYPE_BOXED_LONG.equals(boxedFirstType)
                 || TypeConstants.TYPE_BOXED_LONG.equals(boxedSecondType)
                 || TypeConstants.TYPE_BOXED_INTEGER.equals(boxedFirstType)
                 || TypeConstants.TYPE_BOXED_INTEGER.equals(boxedSecondType)
                 || TypeConstants.TYPE_BOXED_SHORT.equals(boxedFirstType)
                 || TypeConstants.TYPE_BOXED_SHORT.equals(boxedSecondType)
                 || TypeConstants.TYPE_BOXED_BYTE.equals(boxedFirstType)
                 || TypeConstants.TYPE_BOXED_BYTE.equals(boxedSecondType)
                 || TypeConstants.SIGNATURE_BOXED_CHARACTER.equals(boxedFirstType)
                 || TypeConstants.SIGNATURE_BOXED_CHARACTER.equals(boxedSecondType))
         {
             return handleNumericComparison(firstArg, secondArg, Long.class);
         }
         
         // JSP.2.3.5.7, step 7 if either is a boolean, coerce to boolean
         if (TypeConstants.TYPE_BOXED_BOOLEAN.equals(boxedFirstType)
                 || TypeConstants.TYPE_BOXED_BOOLEAN.equals(boxedSecondType))
         {
             return handleBooleanComparison(firstArg, secondArg);
         }
         
         // Unified EL 1.8.2, step 8 if either is a enum, then coerce both to enum
         // NOTE: we handle the JSF 1.1 case also where enums are treated as non-coercable
         // Object's
         if (firstArg.isEnumType() || secondArg.isEnumType())
         {
             return handleEnumComparison(firstArg, secondArg);
         }
         
         // JSP.2.3.5.7, step 8 if either is a string, coerce to string and
         // compare lexically
         if (TypeConstants.TYPE_STRING.equals(boxedFirstType)
                 || TypeConstants.TYPE_STRING.equals(boxedSecondType))
         {
             return handleStringComparison(firstArg, secondArg);
         }
         
         // otherwise, an equal compare will be done A.equals(B).  Since 
        return new ValueType(TypeConstants.TYPE_BOOLEAN, IAssignable.ASSIGNMENT_TYPE_RHS);
     }
 
     private ValueType handleEnumComparison(ValueType firstArg,
             ValueType secondArg) 
     {
         assert firstArg.isEnumType() || secondArg.isEnumType();
         
         // if the first is not an enum, then we have non-Enum == Enum case
         if (!firstArg.isEnumType())
         {
             return handleComparsionOfEnumAndNonEnum(secondArg, firstArg);
         }
         
         // if the second is not an enum, then we have Enum == non-Enum case
         if (!secondArg.isEnumType())
         {
             return handleComparsionOfEnumAndNonEnum(firstArg, secondArg);
         }
         
         // only other case is they are both enums.  Check if they are directly
         // comparable.
         if (TypeUtil.canNeverBeEqual(firstArg.getSignature(), secondArg.getSignature()))
         {
             boolean result = doRealOperation("foo", "notFoo");  // just simulate the operation where the operands are not equal //$NON-NLS-1$ //$NON-NLS-2$
             
             return BooleanLiteralType.valueOf(result);
         }
         
         // otherwise, all we know is that it's a boolean
         return new ValueType(TypeConstants.TYPE_BOOLEAN, IAssignable.ASSIGNMENT_TYPE_RHS);
     }
 
     private ValueType handleComparsionOfEnumAndNonEnum(ValueType enumType,
             ValueType nonEnumType) 
     {
         // the only literal value that could have got us here is a 
         // StringLiteralValue since the others a filtered out before this is
         // called
         if (nonEnumType instanceof LiteralType)
         {
             assert nonEnumType instanceof StringLiteralType;
             
             Diagnostic result = validateIfEnumToStringComparison(((StringLiteralType)nonEnumType).getLiteralValue(), enumType);
             
             if (result != null)
             {
                 // compare two things that aren't equal
                 return BooleanLiteralType.valueOf(doRealOperation("foo", "foo_")); //$NON-NLS-1$ //$NON-NLS-2$
             }
             return new ValueType(TypeConstants.TYPE_BOOLEAN, IAssignable.ASSIGNMENT_TYPE_RHS);
         }
         
         // if the arg is a String, then we can't prove anything before runtime
         if (nonEnumType.isInstanceOf(TypeConstants.TYPE_STRING))
         {
             return new ValueType(TypeConstants.TYPE_BOOLEAN, IAssignable.ASSIGNMENT_TYPE_RHS);
         }
         // otherwise, we know it will result in a problem since one is an enum
         // and the other isn't so simply do a comparison on two things that aren't equals
         return BooleanLiteralType.valueOf(doRealOperation("foo", "foo_")); //$NON-NLS-1$ //$NON-NLS-2$
     }
 
     public Diagnostic validate(ValueType firstArg, ValueType secondArg) {
         
         // JSP.2.3.5.7 step 2 if either operand is null, then not equal
         if (TypeCoercer.typeIsNull(firstArg.getSignature())
                && TypeCoercer.typeIsNull(secondArg.getSignature()))
         {
            // perform the operation on two arguments that are equal.
            final boolean result = doRealOperation(Integer.valueOf(4), Integer.valueOf(4));
             return _diagnosticFactory.create_BINARY_OP_EQUALITY_COMP_WITH_NULL_ALWAYS_EVAL_SAME(Boolean.toString(result));
         }
 
         final String boxedFirstType = 
             TypeTransformer.transformBoxPrimitives(firstArg.getSignature());
         final String boxedSecondType = 
             TypeTransformer.transformBoxPrimitives(secondArg.getSignature());
         
         // JSP.2.3.5.7 step 3, if either is BigDecimal, promote both and compare
         if (TypeConstants.TYPE_BIG_DOUBLE.equals(boxedFirstType)
                 || TypeConstants.TYPE_BIG_DOUBLE.equals(boxedSecondType))
         {
             return validateNumericComparison(firstArg, secondArg, BigDecimal.class);
         }
         
         // JSP.2.3.5.7, step 4 if either is a float or double, promote both to 
         // double and compare
         if (TypeConstants.TYPE_BOXED_DOUBLE.equals(boxedFirstType)
                 || TypeConstants.TYPE_BOXED_FLOAT.equals(boxedFirstType)
                 || TypeConstants.TYPE_BOXED_DOUBLE.equals(boxedSecondType)
                 || TypeConstants.TYPE_BOXED_FLOAT.equals(boxedSecondType))
         {
             return validateNumericComparison(firstArg, secondArg, Double.class);
         }
         
         // JSP.2.3.5.7, step 5 if either is a big integer, promote and compare
         if (TypeConstants.TYPE_BIG_INTEGER.equals(boxedFirstType)
                 || TypeConstants.TYPE_BIG_INTEGER.equals(boxedSecondType))
         {
             return validateNumericComparison(firstArg, secondArg, BigInteger.class);
         }
         
         // JSP.2.3.5.7, step 6 if either is Long or smaller, coerce both to Long
         if (TypeConstants.TYPE_BOXED_LONG.equals(boxedFirstType)
                 || TypeConstants.TYPE_BOXED_LONG.equals(boxedSecondType)
                 || TypeConstants.TYPE_BOXED_INTEGER.equals(boxedFirstType)
                 || TypeConstants.TYPE_BOXED_INTEGER.equals(boxedSecondType)
                 || TypeConstants.TYPE_BOXED_SHORT.equals(boxedFirstType)
                 || TypeConstants.TYPE_BOXED_SHORT.equals(boxedSecondType)
                 || TypeConstants.TYPE_BOXED_BYTE.equals(boxedFirstType)
                 || TypeConstants.TYPE_BOXED_BYTE.equals(boxedSecondType)
                 || TypeConstants.SIGNATURE_BOXED_CHARACTER.equals(boxedFirstType)
                 || TypeConstants.SIGNATURE_BOXED_CHARACTER.equals(boxedSecondType))
         {
             return validateNumericComparison(firstArg, secondArg, Long.class);
         }
         
         // JSP.2.3.5.7, step 7 if either is a boolean, coerce to boolean
         if (TypeConstants.TYPE_BOXED_BOOLEAN.equals(boxedFirstType)
                 || TypeConstants.TYPE_BOXED_BOOLEAN.equals(boxedSecondType))
         {
             return validateBooleanComparison(firstArg, secondArg);
         }
         
         // Unified EL 1.8.2, step 8 if either is a enum, then coerce both to enum
         // NOTE: we handle the JSF 1.1 case also where enums are treated as non-coercable
         // Object's
         if (firstArg.isEnumType() || secondArg.isEnumType())
         {
             return validateEnumComparison(firstArg, secondArg);
         }
         
         // JSP.2.3.5.7, step 8 if either is a string, coerce to string and
         // compare lexically
         if (TypeConstants.TYPE_STRING.equals(boxedFirstType)
                 || TypeConstants.TYPE_STRING.equals(boxedSecondType))
         {
             return validateStringComparison(firstArg, secondArg);
         }
 
         // otherwise, an equal compare will be done A.equals(B).  Since 
         return Diagnostic.OK_INSTANCE;
     }
 
     
     /**
      * Both types are coerced to boolean before comparison
      * 
      * @param firstArg
      * @param secondArg
      * @return the result of the comparison
      */
     private ValueType handleBooleanComparison(ValueType firstArg, ValueType secondArg)
     {
         boolean canCoerceFirstArg =  
             TypeCoercer.canCoerceToBoolean(TypeTransformer.transformBoxPrimitives(firstArg.getSignature()));
         boolean canCoerceSecondArg = TypeCoercer.canCoerceToBoolean(TypeTransformer.transformBoxPrimitives(secondArg.getSignature()));
 
         if (! (canCoerceFirstArg && canCoerceSecondArg))
         {
             return null;
         }
         
         if (firstArg instanceof LiteralType && secondArg instanceof LiteralType)
         {
             try
             {
                 Boolean firstValue = ((LiteralType)firstArg).coerceToBoolean();
                 Boolean secondValue = ((LiteralType)secondArg).coerceToBoolean();
                 
                 if (firstValue != null && secondValue != null)
                 {
                     boolean result = doRealOperation(firstValue, secondValue);
                     return result ? 
                                BooleanLiteralType.TRUE : 
                                        BooleanLiteralType.FALSE;
                 }
             }
             catch (TypeCoercionException tce)
             {
                 throw new AssertionError("should never get here; have already checked coercability above"); //$NON-NLS-1$
             }
         }
         
         // otherwise, we have a valid comparison that results in boolean
         return new ValueType(TypeConstants.TYPE_BOOLEAN, IAssignable.ASSIGNMENT_TYPE_RHS);
     }
     
     private Diagnostic validateBooleanComparison(ValueType firstType, ValueType secondType)
     {
         boolean canCoerceFirstArg =  
             TypeCoercer.canCoerceToBoolean(TypeTransformer.transformBoxPrimitives(firstType.getSignature()));
         boolean canCoerceSecondArg = TypeCoercer.canCoerceToBoolean(TypeTransformer.transformBoxPrimitives(secondType.getSignature()));
 
         if (!canCoerceFirstArg)
         {
             return _diagnosticFactory.create_BINARY_OP_CANNOT_COERCE_ARGUMENT_TO_BOOLEAN(Messages.getString("EqualityRelationalBinaryOperator.FirstArgument")); //$NON-NLS-1$
         }
         
         if (!canCoerceSecondArg)
         {
             return _diagnosticFactory.create_BINARY_OP_CANNOT_COERCE_ARGUMENT_TO_BOOLEAN(Messages.getString("EqualityRelationalBinaryOperator.SecondArgument")); //$NON-NLS-1$
         }
         
         if (firstType instanceof LiteralType && secondType instanceof LiteralType)
         {
             try
             {
                 Boolean firstValue = ((LiteralType)firstType).coerceToBoolean();
                 Boolean secondValue = ((LiteralType)secondType).coerceToBoolean();
                 
                 if (firstValue != null && secondValue != null)
                 {
                     final boolean result = 
                         doRealOperation(firstValue, secondValue);
                     return _diagnosticFactory.
                         create_BINARY_OP_CONSTANT_EXPRESSION_ALWAYS_EVAL_SAME(getOperationName(), Boolean.toString(result));
                 }
             }
             catch (TypeCoercionException tce)
             {
                 throw new AssertionError("should never get here; have already checked coercability above"); //$NON-NLS-1$
             }
         }
         
         // otherwise, we have a valid comparison
         return Diagnostic.OK_INSTANCE;
     }
     
     @Override
     protected Diagnostic validateStringComparison(ValueType firstType,
             ValueType secondType) 
     {
         String firstValue = null;
 
         if (firstType instanceof LiteralType)
         {
             firstValue = ((LiteralType)firstType).getLiteralValue();
         }
 
         String secondValue = null;
         if (secondType instanceof LiteralType)
         {
             secondValue = ((LiteralType)secondType).getLiteralValue();
         }
 
         if (firstValue != null)
         { 
             Diagnostic result = validateIfEnumToStringComparison(firstValue, secondType);
             
             if (result != null)
             {
                 return result;
             }
         }
 
         if (secondValue != null)
         {
             Diagnostic result = validateIfEnumToStringComparison(secondValue, firstType);
             
             if (result != null)
             {
                 return result;
             }
         }
         
         // if it's a string to enum compare, do the default parent thing
         return super.validateStringComparison(firstType, secondType);
     }
 
     
     @Override
     protected ValueType handleStringComparison(ValueType firstType,
             ValueType secondType) 
     {
         String firstValue = null;
 
         if (firstType instanceof LiteralType)
         {
             firstValue = ((LiteralType)firstType).getLiteralValue();
         }
 
         String secondValue = null;
         if (secondType instanceof LiteralType)
         {
             secondValue = ((LiteralType)secondType).getLiteralValue();
         }
 
         if (firstValue != null)
         { 
             Diagnostic result = validateIfEnumToStringComparison(firstValue, secondType);
             
             if (result != null)
             {
                 return handleIfEnumToNonMemberStringComparison(firstValue, secondType);
             }
         }
 
         if (secondValue != null)
         {
             Diagnostic result = validateIfEnumToStringComparison(secondValue, firstType);
             
             if (result != null)
             {
                 return handleIfEnumToNonMemberStringComparison(secondValue, firstType);
             }
         }
 
         // otherwise, do the super thing
         return super.handleStringComparison(firstType, secondType);
     }
 
     private Diagnostic validateEnumComparison(final ValueType firstArg, final ValueType secondArg)
     {
         assert firstArg.isEnumType() || secondArg.isEnumType();
         
         // if the first is not an enum, then we have non-Enum == Enum case
         if (!firstArg.isEnumType())
         {
             return validateComparsionOfEnumAndNonEnum(firstArg, secondArg);
         }
         
         // if the second is not an enum, then we have Enum == non-Enum case
         if (!secondArg.isEnumType())
         {
             return validateComparsionOfEnumAndNonEnum(secondArg, firstArg);
         }
         
         // only other case is they are both enums.  Check if they are directly
         // comparable.
         if (TypeUtil.canNeverBeEqual(firstArg.getSignature(), secondArg.getSignature()))
         {
             return _diagnosticFactory.
                 create_BINARY_COMPARISON_WITH_TWO_ENUMS_ALWAYS_SAME
                     (getOperationName()
                      , doRealOperation("foo", "notFoo")  // just simulate the operation where the operands are not equal //$NON-NLS-1$ //$NON-NLS-2$
                      , TypeUtil.getFullyQualifiedName(firstArg.getSignature())
                      , TypeUtil.getFullyQualifiedName(secondArg.getSignature())); 
         }
         
         // otherwise, it's all good
         return Diagnostic.OK_INSTANCE;
     }
     
     private Diagnostic validateComparsionOfEnumAndNonEnum(final ValueType nonEnumType, final ValueType enumType)
     {
         // the only literal value that could have got us here is a 
         // StringLiteralValue since the others a filtered out before this is
         // called
         if (nonEnumType instanceof LiteralType)
         {
             assert nonEnumType instanceof StringLiteralType;
             
             Diagnostic result = validateIfEnumToStringComparison(((StringLiteralType)nonEnumType).getLiteralValue(), enumType);
             
             if (result != null)
             {
                 return result;
             }
             return Diagnostic.OK_INSTANCE;
         }
         
         // if the arg is a String, then we can't prove anything before runtime
         if (nonEnumType.isInstanceOf(TypeConstants.TYPE_STRING))
         {
             return Diagnostic.OK_INSTANCE;
         }
         // otherwise, we know it will result in a problem since one is an enum
         // and the other isn't
         return _diagnosticFactory.
             create_BINARY_COMPARISON_WITH_ENUM_AND_UNCOERCABLE_NONCONST_ALWAYS_SAME
                 (getOperationName()
                  , doRealOperation("foo", "notFoo")  // just simulate the operation where the operands are not equal //$NON-NLS-1$ //$NON-NLS-2$
                  , TypeUtil.getFullyQualifiedName(enumType.getSignature())
                  , TypeUtil.getFullyQualifiedName(nonEnumType.getSignature())); 
     }
     
     private Diagnostic validateIfEnumToStringComparison(final String literalValue, final ValueType validateIfEnum)
     {
         if (validateIfEnum.isEnumType()
                 && validateIfEnum instanceof IObjectSymbolBasedValueType)
         {
             final IObjectSymbolBasedValueType symbolValueType =
                 (IObjectSymbolBasedValueType) validateIfEnum;
             
             IType type = symbolValueType.getSymbol().getTypeDescriptor().resolveType(symbolValueType.getSymbol().getTypeDescriptor().getTypeSignature());
             
             if (type != null && !TypeUtil.isEnumMember(type, literalValue))
             {
                 return _diagnosticFactory.
                     create_BINARY_COMPARISON_WITH_ENUM_AND_CONST_ALWAYS_SAME
                         (getOperationName()
                          , doRealOperation(literalValue, literalValue+"_")  // just simulate the operation where the operands are not equal //$NON-NLS-1$
                          , TypeUtil.getFullyQualifiedName(validateIfEnum.getSignature())
                          , literalValue);
             }
         }
        
         return null;
     }
     
     private ValueType handleIfEnumToNonMemberStringComparison(final String literalValue, final ValueType enumValueType)
     {
         // we need to apply the real operation to literalValue and any string that !equals(literalValue)
         // since it's not a member of the enum
         return BooleanLiteralType.valueOf(doRealOperation(literalValue, literalValue+"_")); //$NON-NLS-1$
     }
 }
