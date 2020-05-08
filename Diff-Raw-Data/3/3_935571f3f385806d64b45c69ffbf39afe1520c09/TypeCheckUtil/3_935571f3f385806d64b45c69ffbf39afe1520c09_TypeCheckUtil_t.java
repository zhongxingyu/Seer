 /*******************************************************************************
  * Copyright (c) 2005, 2012 eBay Inc.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  *******************************************************************************/
 package org.eclipse.vjet.dsf.jsgen.shared.validation.vjo.semantic.rules.util;
 
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.List;
 
 import org.eclipse.vjet.dsf.jsgen.shared.validation.vjo.semantic.VjoConstants;
 import org.eclipse.vjet.dsf.jst.IInferred;
 import org.eclipse.vjet.dsf.jst.IJstMethod;
 import org.eclipse.vjet.dsf.jst.IJstNode;
 import org.eclipse.vjet.dsf.jst.IJstProperty;
 import org.eclipse.vjet.dsf.jst.IJstRefType;
 import org.eclipse.vjet.dsf.jst.IJstType;
 import org.eclipse.vjet.dsf.jst.IJstTypeReference;
 import org.eclipse.vjet.dsf.jst.datatype.JstReservedTypes;
 import org.eclipse.vjet.dsf.jst.declaration.JstArg;
 import org.eclipse.vjet.dsf.jst.declaration.JstArray;
 import org.eclipse.vjet.dsf.jst.declaration.JstAttributedType;
 import org.eclipse.vjet.dsf.jst.declaration.JstCache;
 import org.eclipse.vjet.dsf.jst.declaration.JstExtendedType;
 import org.eclipse.vjet.dsf.jst.declaration.JstFuncType;
 import org.eclipse.vjet.dsf.jst.declaration.JstFunctionRefType;
 import org.eclipse.vjet.dsf.jst.declaration.JstMixedType;
 import org.eclipse.vjet.dsf.jst.declaration.JstObjectLiteralType;
 import org.eclipse.vjet.dsf.jst.declaration.JstPackage;
 import org.eclipse.vjet.dsf.jst.declaration.JstParamType;
 import org.eclipse.vjet.dsf.jst.declaration.JstTypeWithArgs;
 import org.eclipse.vjet.dsf.jst.declaration.JstVariantType;
 import org.eclipse.vjet.dsf.jst.declaration.JstWildcardType;
 import org.eclipse.vjet.dsf.jst.declaration.SynthOlType;
 
 public class TypeCheckUtil {
 
 	public static IJstType getFunctionNativeType() {
 		return JstCache.getInstance().getType("Function");
 	}
 
 	public static IJstType getObjectNativeType() {
 		return JstCache.getInstance().getType("Object");
 	}
 	
 	public static IJstType getUndefinedNativeType() {
 		return JstCache.getInstance().getType("Undefined");
 	}
 
 	public static boolean equals(final IJstType one, final IJstType two) {
 		if (one == null || two == null) {
 			return false;
 		}
 		if (one.equals(two)) {
 			return true;
 		}
 
 		String name1 = one.getName();
 		String name2 = two.getName();
 		if (name1 != null) {
 			if (name1.equals(name2)) {
 				return true;
 			}
 			return ("Object".equals(name1) || SynthOlType.TYPE_NAME
 					.equals(name1))
 					&& ("Object".equals(name2) || SynthOlType.TYPE_NAME
 							.equals(name2));
 		}
 		return false;
 	}
 
 	/**
 	 * bugfix, support Function, Array, ObjectLiteral as js native types
 	 * 
 	 * @param type
 	 * @return
 	 */
 	protected static IJstType toJsNativeType(IJstType type) {
 		if (type == null) {
 			return null;
 		}
 
 		if (type instanceof SynthOlType) {
 			return null;
 		}
 
 		for (IJstType jsNativeType : VjoConstants.NativeTypes
 				.getJstNativeTypes()) {
 			if (equals(jsNativeType, type)) {
 				return jsNativeType;
 			}
 		}
 
 		if (equals(JstReservedTypes.JavaPrimitive.BOOLEAN, type)) {
 			return VjoConstants.NativeTypes.getPrimitiveBooleanJstType();
 		} else if (equals(JstReservedTypes.JavaPrimitive.BYTE, type)) {
 			return VjoConstants.NativeTypes.getNumberJstType();
 		} else if (equals(JstReservedTypes.JavaPrimitive.CHAR, type)) {
 			return VjoConstants.NativeTypes.getStringJstType();
 		} else if (equals(JstReservedTypes.JavaPrimitive.DOUBLE, type)
 				|| equals(JstReservedTypes.JavaPrimitive.FLOAT, type)) {
 			return VjoConstants.NativeTypes.getDoubleJstType();
 		} else if (equals(JstReservedTypes.JavaPrimitive.INT, type)
 				|| equals(JstReservedTypes.JavaPrimitive.SHORT, type)
 				|| equals(JstReservedTypes.JavaPrimitive.LONG, type)) {
 			return VjoConstants.NativeTypes.getIntJstType();
 		} else if (equals(JstReservedTypes.Other.VOID, type)) {
 			return JstReservedTypes.Other.VOID;
 		} else if (type instanceof JstFunctionRefType
 				|| "Function".equals(type.getSimpleName())) {
 			return getFunctionNativeType();
 		} else if (type instanceof JstArray
 				|| "Array".equals(type.getSimpleName())) {
 			return VjoConstants.NativeTypes.getArrayJstType();
 		}
 		// else if(type instanceof JstObjectLiteralType){
 		// return getObjectNativeType();
 		// }
 
 		return null;
 	}
 
 	protected static boolean isAssignableJsNativeType(IJstType lhs, IJstType rhs) {
 		if (equals(VjoConstants.NativeTypes.getObjectJstType(), lhs)) {
 			return true;
 		} else if (VjoConstants.NativeTypes.getPrimitiveBooleanJstType()
 				.equals(lhs)) {
 			return VjoConstants.NativeTypes.getPrimitiveBooleanJstType()
 					.equals(rhs);
 			// || VjoConstants.NativeTypes.getNumberJstType().equals(rhs);
 		}
 		 else if (VjoConstants.NativeTypes.getAliasBooleanJstType()
 					.equals(lhs)) {
 				return VjoConstants.NativeTypes.getBooleanJstType()
 						.equals(rhs);
 				// || VjoConstants.NativeTypes.getNumberJstType().equals(rhs);
 			}else if (VjoConstants.NativeTypes.getNumberJstType().equals(lhs)
 				|| VjoConstants.NativeTypes.getAliasNumberJstType().equals(lhs)) {
 			return VjoConstants.NativeTypes.getNumberJstType().equals(rhs)
 					|| VjoConstants.NativeTypes.getPrimitiveBooleanJstType()
 							.equals(rhs)
 					|| VjoConstants.NativeTypes.getIntJstType().equals(rhs)
 					|| VjoConstants.NativeTypes.getDoubleJstType().equals(rhs);
 			// bugfix, boolean in js is 0/1
 		} else if (VjoConstants.NativeTypes.getIntJstType().equals(lhs)) {
 			return VjoConstants.NativeTypes.getNumberJstType().equals(rhs)
 					|| VjoConstants.NativeTypes.getPrimitiveBooleanJstType()
 							.equals(rhs)
 					|| VjoConstants.NativeTypes.getIntJstType().equals(rhs);
 		} else if (VjoConstants.NativeTypes.getDoubleJstType().equals(lhs)) {
 			return VjoConstants.NativeTypes.getNumberJstType().equals(rhs)
 					|| VjoConstants.NativeTypes.getDoubleJstType().equals(rhs);
 		} else if (VjoConstants.NativeTypes.getStringJstType().equals(lhs)) {
 			return VjoConstants.NativeTypes.getRegExpJstType().equals(rhs)
 					|| VjoConstants.NativeTypes.getStringJstType().equals(rhs);
 		} else if (VjoConstants.NativeTypes.getAliasStringJstType().equals(lhs)) {
 			return VjoConstants.NativeTypes.getRegExpJstType().equals(rhs)
 					|| VjoConstants.NativeTypes.getStringJstType().equals(rhs);
 		} else if (VjoConstants.NativeTypes.getAliasDateJstType().equals(lhs)) {
 			return VjoConstants.NativeTypes.getDateJstType().equals(rhs);
 		} else if (VjoConstants.NativeTypes.getAliasArrayJstType().equals(lhs)) {
 			return VjoConstants.NativeTypes.getArrayJstType().equals(rhs);
 		} else if (VjoConstants.NativeTypes.getRegExpJstType().equals(lhs)) {
 			return VjoConstants.NativeTypes.getRegExpJstType().equals(rhs)
 					|| VjoConstants.NativeTypes.getStringJstType().equals(rhs);
 		}
 
 		if (lhs != null) {
 			return equals(lhs, rhs);
 		} else {
 			return false;
 		}
 	}
 
 	protected static boolean isAssignableVjoType(IJstType lhs, IJstType rhs) {
 		if (lhs instanceof JstExtendedType) {
 			lhs = ((JstExtendedType) lhs).getTargetType();
 		}
 		if (rhs instanceof JstExtendedType) {
 			rhs = ((JstExtendedType) rhs).getTargetType();
 		}
 
 		// if(rhs.getAlias().equals(lhs.getName())){
 		// return true;
 		// }
 
 		if (lhs == rhs) { // check if types are the same
 			return true;
 		}
 
 		if (lhs instanceof JstParamType) {
 			JstParamType paramType = (JstParamType) lhs;
 
 			if (paramType.getType() == rhs) {
 				return true;
 			}
 		}
 
 		// TODO check JstTypeWithArgs
 
 		if (isAssignableVjoGenericType(lhs, rhs)) {
 			return true;
 		}
 
 		for (IJstType inheritedType : rhs.getExtends()) {
 			if (equals(VjoConstants.NativeTypes.getObjectJstType(),
 					inheritedType)) {
 				continue;
 			}
 			if (isAssignableVjoType(lhs, inheritedType)) {
 				return true;
 			}
 		}
 
 		if (lhs.isInterface()) {
 			for (IJstType interfaceType : rhs.getSatisfies()) {
 				if (isAssignableVjoType(lhs, interfaceType)) {
 					return true;
 				}
 			}
 		} else if (lhs.isMixin()) {
 			for (IJstTypeReference mixinTypeRef : rhs.getMixinsRef()) {
 				if (isAssignableVjoType(lhs, mixinTypeRef.getReferencedType())) {
 					return true;
 				}
 			}
 		}
 
 		return false;
 	}
 
 	private static boolean isAssignableVjoGenericType(IJstType one, IJstType two) {
 		// if(equals(one, two)){
 		// return true;
 		// }
 		// if(one.equals(two)){
 		// return true;
 		// }
 		// Added by Eric.Ma 20100416
 		if (one instanceof JstParamType && two instanceof JstParamType) {
 			IJstType oneParentType = one.getExtend();
 			IJstType twoParentType = two.getExtend();
 			if (oneParentType != null && twoParentType != null) {
 				if (isObject(oneParentType) && isObject(twoParentType)) {
 					return true;
 				}
 			}
 		}
 		// End of added
 		if (one instanceof JstWildcardType) {
 			one = ((JstWildcardType) one).getType();
 			return isAssignable(one, two);
 		}
 
 		if (two instanceof JstWildcardType) {
 			two = ((JstWildcardType) two).getType();
 			return isAssignable(one, two);
 		}
 
 		if (one instanceof JstTypeWithArgs) {
 			if (two instanceof JstTypeWithArgs) {
 				// one and two are both generics
 				return isAssignableVjoTypeWithArgs(null, null,
 						(JstTypeWithArgs) one, (JstTypeWithArgs) two);
 			} else {
 				// one is generics
 				return isAssignableVjoGenericType((JstTypeWithArgs) one, two);
 			}
 		} else if (two instanceof JstTypeWithArgs) {
 			// two is generics
 			return isAssignableVjoGenericType((JstTypeWithArgs) two, one);
 		}
 		return false;
 	}
 
 	private static boolean isAssignableVjoGenericType(JstTypeWithArgs one,
 			IJstType two) {
 		return isAssignable(one.getType(), two);
 	}
 
 	private static boolean isAssignableVjoTypeWithArgs(
 			JstTypeWithArgs typeWithArgsOne, JstTypeWithArgs typeWithArgsTwo,
 			JstTypeWithArgs one, JstTypeWithArgs two) {
 		if (isAssignable(one.getType(), two.getType())) {
 
 			final List<IJstType> oneArgTypes = one.getArgTypes();
 			final List<IJstType> twoArgTypes = two.getArgTypes();
 
 			if (oneArgTypes.size() == twoArgTypes.size()) {
 				for (int i = 0, len = oneArgTypes.size(); i < len; i++) {
 					IJstType oneArgType = oneArgTypes.get(i);
 					IJstType twoArgType = twoArgTypes.get(i);
 
 					if (oneArgType == twoArgType) {
 						continue;
 					}
 
 					if (oneArgType instanceof JstTypeWithArgs
 							&& twoArgType instanceof JstTypeWithArgs) {
 						if (!isAssignableVjoTypeWithArgs(typeWithArgsOne,
 								typeWithArgsTwo, (JstTypeWithArgs) oneArgType,
 								(JstTypeWithArgs) twoArgType)) {
 							return false;
 						} else {
 							continue;
 						}
 					}
 
 					// added by Eric.Ma 20100401 Handle two args both as
 					// JstWildcardType situation
 					if (oneArgType instanceof JstWildcardType
 							&& twoArgType instanceof JstWildcardType) {
 						if (isSuperType(
 								((JstWildcardType) twoArgType).isUpperBound(),
 								((JstWildcardType) oneArgType).isUpperBound(),
 								getWildCardType((JstWildcardType) twoArgType),
 								getWildCardType((JstWildcardType) oneArgType))) {
 							continue;
 						} else {
 							return false;
 						}
 					}
 
 					// special handle : param type as JSTparamType, to compare
 					// visibility is enough!
 					// if(oneArgType instanceof JstParamType && twoArgType
 					// instanceof JstParamType){
 					if (oneArgType instanceof JstParamType) {
 						if (isSuperType(twoArgType.getOwnerType(),
 								oneArgType.getOwnerType())) {
 
 							continue;
 						}
 					}
 					// end of added
 
 					IJstType oneArgType2 = null;
 					IJstType twoArgType2 = twoArgType;
 					if (oneArgType instanceof JstWildcardType) {
 						oneArgType2 = ((JstWildcardType) oneArgType).getType();
 						if (JstWildcardType.DEFAULT_NAME.equals(oneArgType2
 								.getName())) {
 							oneArgType2 = JstCache.getInstance().getType(
 									"Undefined");
 						}
 						if (((JstWildcardType) oneArgType).isLowerBound()
 								&& isSuperType(twoArgType2, oneArgType2)) {
 							continue;
 						}
 						if (((JstWildcardType) oneArgType).isUpperBound()
 								&& isSuperType(oneArgType2, twoArgType2)) {
 							continue;
 						}
 					}
 
 					if (twoArgType instanceof JstWildcardType) {
 						twoArgType2 = ((JstWildcardType) twoArgType).getType();
 					}
 
 					if (oneArgType2 instanceof JstParamType
 							&& typeWithArgsOne != null) {
 						IJstType argType = typeWithArgsOne
 								.getParamArgType((JstParamType) oneArgType2);
 
 						if (argType != null) {
 							oneArgType2 = argType;
 						}
 					}
 
 					if (twoArgType2 instanceof JstParamType
 							&& typeWithArgsTwo != null) {
 						IJstType argType = typeWithArgsTwo
 								.getParamArgType((JstParamType) twoArgType2);
 
 						if (argType != null) {
 							twoArgType2 = argType;
 						}
 					}
 
 					// TODO allow boundary
 					if (oneArgType2 != null
 							&& (((JstWildcardType) oneArgType).isUpperBound()
 									&& isAssignable(twoArgType2, oneArgType2) || (((JstWildcardType) oneArgType)
 									.isLowerBound() && isAssignable(
 									oneArgType2, twoArgType2)))) {
 						continue;
 					} else if (JstWildcardType.class
 							.isAssignableFrom(oneArgType.getClass())) {
 						final JstWildcardType wildCardType = ((JstWildcardType) oneArgType);
 						final IJstType assignableType = JstWildcardType.class
 								.isAssignableFrom(twoArgType.getClass()) ? ((JstWildcardType) twoArgType)
 								.getType() : twoArgType;
 
 						if (wildCardType.isLowerBound()) {
 							if (!isAssignable(wildCardType.getType(),
 									assignableType)) {
 								return false;
 							}
 						} else if (wildCardType.isUpperBound()) {
 							if (!isAssignable(assignableType,
 									wildCardType.getType())) {
 								return false;
 							}
 						} else {
 							// ? wild card, allowing any type
 							continue;
 						}
 					} else {
 						return false;
 					}
 				}
 				return true;
 			}
 		}
 
 		return false;
 	}
 
 	public static IJstType getWildCardType(final JstWildcardType type) {
 		final IJstType actualType = type.getType();
 		if (JstWildcardType.DEFAULT_NAME.equals(actualType.getName())) {
 			return JstCache.getInstance().getType("Object");
 		}
 		return actualType;
 	}
 
 	public static IJstType assign(final IJstType assignTo,
 			final IJstType assignFrom) {
 		final IJstType assignToTransformed = toJsNativeType(assignTo);
 		if (assignToTransformed != null) {
 			if (VjoConstants.NativeTypes.getStringJstType().equals(
 					assignToTransformed)) {
 				return assignTo;// Object#toString
 			}
 		}
 
 		if (isAssignable(assignTo, assignFrom)) {
 			return assignFrom;
 		} else {
 			return assignTo;
 		}
 	}
 
 	public static boolean isAssignable(List<IJstType> assignToList,
 			List<IJstType> assignFromList) {
 		if (assignToList == null || assignFromList == null
 				|| assignToList.size() <= 0 || assignFromList.size() <= 0) {
 			return false;// TODO should probably be an exception,
 							// IllegalArgumentException etc.
 		}
 
 		for (IJstType assignFrom : assignFromList) {
 			for (IJstType assignTo : assignToList) {
 				if (isAssignable(assignTo, assignFrom)) {
 					return true;// for overloading
 				}
 			}
 		}
 
 		return false;
 	}
 
 	public static boolean isAssignable(IJstType assignTo,
 			List<IJstType> assignFromList) {
 		if (assignTo == null || assignFromList == null
 				|| assignFromList.size() <= 0) {
 			return false;// TODO should probably be an exception,
 							// IllegalArgumentException etc.
 		}
 
 		for (IJstType assignFrom : assignFromList) {
 			if (isAssignable(assignTo, assignFrom)) {
 				return true;// for overloading
 			}
 		}
 
 		return false;
 	}
 
 	public static boolean isAssignable(JstTypeWithArgs typeWithArgsTo,
 			JstTypeWithArgs typeWithArgsFrom, JstTypeWithArgs assignTo,
 			JstTypeWithArgs assignFrom) {
 
 		return isAssignableVjoTypeWithArgs(typeWithArgsTo, typeWithArgsFrom,
 				assignTo, assignFrom);
 	}
 
 	public static boolean isAssignable(IJstType assignTo, IJstType assignFrom) {
 		if (assignTo instanceof IInferred || assignFrom instanceof IInferred) {
 			return true; // inferred type can be reassigned
 		}
 		if (isObject(assignTo)) {
 			return true;
 		}
 		if(isUndefined(assignTo)){
 			return true;
 		}
 		if (assignTo instanceof IJstRefType && assignTo.isFType()) {
 			assignTo = ((IJstRefType) assignTo).getReferencedNode();
 		}
 
 		if (assignFrom instanceof IJstRefType && assignFrom.isFType()) {
 			assignFrom = ((IJstRefType) assignFrom).getReferencedNode();
 		}
 
 		// enhancement by huzhou to support JstAttributedType
 		if (assignTo instanceof JstAttributedType) {
 			assignTo = getAttributedType((JstAttributedType) assignTo);
 			if (assignTo == null) {
 				// no attributed type binding available, assume object type
 				return true;
 			}
 		}
 		if (assignFrom instanceof JstAttributedType) {
 			assignFrom = getAttributedType((JstAttributedType) assignFrom);
 			if (assignFrom == null) {
 				// no attributed type binding available, assume object type
 				return true;
 			}
 		}
 
 		if (assignTo instanceof JstExtendedType) {
 			assignTo = ((JstExtendedType) assignTo).getTargetType();
 		}
 
 		if (assignFrom instanceof JstExtendedType) {
 			assignFrom = ((JstExtendedType) assignFrom).getTargetType();
 		}
 
 		if (assignFrom instanceof JstVariantType) {
 			for (IJstType vType : ((JstVariantType) assignFrom)
 					.getVariantTypes()) {
 				if (isAssignable(assignTo, vType)) {
 					return true;
 				}
 			}
 			return false;
 		}
 
 		if (assignTo instanceof JstVariantType) {
 			for (IJstType vType : ((JstVariantType) assignTo).getVariantTypes()) {
 				if (isAssignable(vType, assignFrom)) {
 					return true;
 				}
 			}
 			return false;
 		}
 
 		if (assignTo instanceof JstMixedType) {
 			
 			for (IJstType mType : ((JstMixedType) assignTo).getMixedTypes()) {
 				if (isAssignable(mType, assignFrom)) {
 					return true;
 				}
 			}
 			return false;
 		}
 
 		if (assignFrom instanceof JstMixedType) {
 			for (IJstType mType : ((JstMixedType) assignFrom).getMixedTypes()) {
 				if (isAssignable(assignTo, mType)) {
 					return true;
 				}
 			}
 			return false;
 		}
 
 		IJstType fromType = assignFrom;
 		IJstType toType = assignTo;
 		if (assignTo instanceof IJstRefType) {
 			// we can only assign from another JstTypeRefType
			if (!(assignFrom instanceof IJstRefType)
					&& !isUndefined(assignFrom)) {
 				return false;
 			}
 			toType = ((IJstRefType) assignTo).getReferencedNode();
 		}
 		if (assignFrom instanceof IJstRefType) {
 			// added by huzhou@ebay.com, any type reference should be assignable
 			// to Function
 			if ("Function".equals(assignTo.getName())) {
 				return true;
 			}
 			// we can only assign from another JstTypeRefType
 			else if (!(assignTo instanceof IJstRefType)) {
 				return false;
 			}
 			fromType = ((IJstRefType) assignFrom).getReferencedNode();
 		}
 
 		final Boolean checkFunction = checkFunction(assignTo, assignFrom);
 		if (checkFunction != null) {
 			return checkFunction.booleanValue();
 		}
 
 		final Boolean checkObjLiteral = checkObjLiteral(assignTo, assignFrom);
 		if (checkObjLiteral != null) {
 			return checkObjLiteral.booleanValue();
 		}
 
 		
 		// bugfix when both type are null
 		if (toType == fromType) {
 			return true;
 		} else if (toType == null || fromType == null) {
 			return false;
 		}
 
 		// bugfix, NULL & UNDEFINED assignment support
 		else if (VjoConstants.ARBITARY.equals(toType)
 				|| VjoConstants.ARBITARY.equals(fromType)) {
 			return true;
 		} else if (VjoConstants.NULL.equals(fromType)
 				||"Undefined".equals(fromType.getName())
 				|| VjoConstants.NULL.equals(toType)) {
 			return true;
 		}
 
 		final IJstType assignToTransformed = toJsNativeType(toType);
 		final IJstType assignFromTransformed = toJsNativeType(fromType);
 
 		if (!(fromType instanceof SynthOlType)
 				&& equals(VjoConstants.NativeTypes.getObjectJstType(), fromType)) {
 			// Modified by Eric.Ma 20100408
 			if (toType instanceof JstTypeWithArgs) {
 				if (isObject(assignTo)) {
 					return true;
 				}
 				return false;
 			}
 			// End of modified
 //			return true;
 		}
 		if (assignToTransformed != null) {
 			return isAssignableJsNativeType(assignToTransformed,
 					assignFromTransformed);
 		} else {
 			return isAssignableVjoType(toType, fromType);
 		}
 	}
 
 	/**
 	 * added by huzhou to handle obj literal assignments ObjLiteral type is
 	 * different from Object type as the value must be an Object Literal
 	 * declaration or equivalent Therefore an ObjLiteral value could only be
 	 * assigned to Object (handled by caller) or ObjLiteral or Otype
 	 * 
 	 * @param assignTo
 	 * @param assignFrom
 	 * @return
 	 */
 	private static Boolean checkObjLiteral(IJstType assignTo,
 			IJstType assignFrom) {
 		if ((assignTo instanceof IJstType && "ObjLiteral".equals(assignTo
 				.getSimpleName()))// raw ObjLiteral type
 				|| (assignTo instanceof SynthOlType && ((SynthOlType) assignTo)
 						.getResolvedOTypes() == null)) {// unresolved SynthOlType
 			return (assignFrom instanceof IJstType && "ObjLiteral"
 					.equals(assignFrom.getSimpleName()))
 					|| assignFrom instanceof SynthOlType
 					|| assignFrom instanceof JstObjectLiteralType ? Boolean.TRUE
 					: Boolean.FALSE;
 		} else if (assignTo instanceof JstObjectLiteralType) {
 			if (assignFrom instanceof JstObjectLiteralType) {
 				return isAssignable((JstObjectLiteralType) assignTo,
 						(JstObjectLiteralType) assignFrom) ? Boolean.TRUE
 						: Boolean.FALSE;
 			} else if (assignFrom instanceof SynthOlType) {
 				
 				
 				if (((SynthOlType) assignFrom).getResolvedOTypes() != null) {
 					SynthOlType syntType  = (SynthOlType) assignFrom;
 					boolean isOTypeAssignable = false;
 					for(IJstType otype: syntType.getResolvedOTypes()){
 						if(otype!=null){
 							isOTypeAssignable = isAssignable((JstObjectLiteralType) assignTo,
 									otype) ? Boolean.TRUE
 							: Boolean.FALSE;
 							if(isOTypeAssignable){
 								return true;
 							}
 						}
 					}
 				} 
 				
 				else {// when SynthOlType isn't bound to otype, it should also
 						// match the lhs's properties declarations
 						// it's not enforced because there's an otype
 						// declaration case, where otype defines
 						// JstObjectLiteralType member
 						// and its property expression is obj literal with
 						// SynthOlType
 						// but the SynthOlType doesn't have any properties or
 						// members at all
 						// seems that the otype translation doesn't care the
 						// expression value translation at all
 					return Boolean.TRUE;
 				}
 			}
 		}
 		return null;
 	}
 
 	private static boolean isAssignable(final JstObjectLiteralType lhs,
 			final JstObjectLiteralType rhs) {
 		if (lhs == rhs) {
 			return true;
 		}
 
 		for (IJstProperty lhsPty : lhs.getProperties()) {
 			final IJstProperty rhsPty = rhs.getProperty(lhsPty.getName()
 					.getName());
 			if (rhsPty != null
 					&& !isAssignable(lhsPty.getType(), rhsPty.getType())) {
 				return false;
 			} else if (!lhs.isOptionalField(lhsPty)) {
 				return false;
 			}
 		}
 		return true;
 	}
 
 	private static Boolean checkFunction(IJstType assignTo, IJstType assignFrom) {
 		if (assignTo instanceof JstFuncType
 				&& assignFrom instanceof JstFuncType) {
 			return Boolean.valueOf(isAssignable(
 					((JstFuncType) assignTo).getFunction(),
 					((JstFuncType) assignFrom).getFunction()));
 		}
 
 		if (assignTo instanceof JstFunctionRefType
 				&& assignFrom instanceof JstFunctionRefType) {
 			return Boolean.valueOf(isAssignable(
 					((JstFunctionRefType) assignTo).getMethodRef(),
 					((JstFunctionRefType) assignFrom).getMethodRef()));
 		}
 
 		if (assignTo instanceof JstFuncType
 				&& assignFrom instanceof JstFunctionRefType) {
 			return Boolean.valueOf(isAssignable(
 					((JstFuncType) assignTo).getFunction(),
 					((JstFunctionRefType) assignFrom).getMethodRef()));
 		}
 
 		if (assignTo instanceof JstFunctionRefType
 				&& assignFrom instanceof JstFuncType) {
 			return Boolean.valueOf(isAssignable(
 					((JstFunctionRefType) assignTo).getMethodRef(),
 					((JstFuncType) assignFrom).getFunction()));
 		}
 
 		if (assignTo instanceof JstFuncType && assignFrom != null
 				&& assignFrom.isFType()) {
 			final IJstMethod invoke = assignFrom.getMethod("_invoke_", true);
 			if (invoke == null) {
 				return Boolean.FALSE;
 			}
 			return Boolean.valueOf(isAssignable(
 					((JstFuncType) assignTo).getFunction(), invoke));
 		}
 
 		if (assignTo instanceof JstFunctionRefType && assignFrom != null
 				&& assignFrom.isFType()) {
 			final IJstMethod invoke = assignFrom.getMethod("_invoke_", true);
 			if (invoke == null) {
 				return Boolean.FALSE;
 			}
 			return Boolean.valueOf(isAssignable(
 					((JstFunctionRefType) assignTo).getMethodRef(), invoke));
 		}
 
 		return null;
 	}
 
 	public static boolean isAssignable(final IJstMethod lhsMethod,
 			final IJstMethod rhsMethod) {
 		final List<IJstMethod> lhsMethods = new LinkedList<IJstMethod>();
 		final List<IJstMethod> rhsMethods = new LinkedList<IJstMethod>();
 		if (lhsMethod.isDispatcher()) {
 			lhsMethods.addAll(lhsMethod.getOverloaded());
 		} else {
 			lhsMethods.add(lhsMethod);
 		}
 		if (rhsMethod.isDispatcher()) {
 			rhsMethods.addAll(rhsMethod.getOverloaded());
 		} else {
 			rhsMethods.add(rhsMethod);
 		}
 
 		NEXT_LHS: for (IJstMethod lhsIter : lhsMethods) {
 			for (IJstMethod rhsIter : rhsMethods) {
 				if (isAssignableNoOverload(lhsIter, rhsIter)) {
 					continue NEXT_LHS;
 				}
 			}
 			return false;
 		}
 		return true;
 	}
 
 	private static boolean isAssignableNoOverload(final IJstMethod lhsMethod,
 			final IJstMethod rhsMethod) {
 		if (lhsMethod == null || rhsMethod == null) {
 			throw new IllegalArgumentException("method must not be null");
 		}
 
 		final IJstType lhsRtnType = lhsMethod.getRtnType();
 		final IJstType rhsRtnType = rhsMethod.getRtnType();
 		if (lhsRtnType != null && rhsRtnType != null) {
 			if (!"void".equals(lhsRtnType.getName())) {// if lhs is void, rhs's
 														// rtn type won't impact
 														// the assignabilities
 				if (!isAssignable(lhsRtnType, rhsRtnType)) { // return type of
 																// rhs should be
 																// more restrict
 					return false;
 				}
 			}
 		}
 		final List<JstArg> lhsParams = lhsMethod.getArgs();
 		final int lhsParamsLength = lhsParams.size();
 
 		final List<JstArg> rhsParams = paddingRhsParams(rhsMethod,
 				lhsParamsLength);
 		final int rhsParamsLength = rhsParams.size();
 
 		if (lhsParamsLength != rhsParamsLength) {
 			return false;
 		}
 
 		for (Iterator<JstArg> lhsIt = lhsParams.iterator(), rhsIt = rhsParams
 				.iterator(); lhsIt.hasNext() && rhsIt.hasNext();) {
 			final JstArg lhsParam = lhsIt.next();
 			final JstArg rhsParam = rhsIt.next();
 			if (lhsParam == null || rhsParam == null) {
 				throw new IllegalArgumentException(
 						"method argument should not be null");
 			}
 			if (lhsParam.isVariable() && !rhsParam.isVariable()) {
 				return false;
 			}
 			// fix by huzhou to tell the difference between variable lengthed
 			// parameters
 			// if lhs is variable lengthed while rhs isn't, it won't match
 			if (!isAssignable(rhsParam.getType(), lhsParam.getType())) { // parameter
 																			// type
 																			// of
 																			// rhs
 																			// should
 																			// be
 																			// less
 																			// restrict
 				return false;
 			}
 		}
 
 		return true;
 	}
 
 	/**
 	 * helper for isAssignableNoOverload, which only pads the rhs's params list
 	 * when: 1. lhs's params list is longer than rhs' 2. rhs's last param is a
 	 * variable lengthed param 3. the padding result will make the rhs' params
 	 * list as long as the lhs'
 	 * 
 	 * @param rhsMethod
 	 * @param lhsParamsLength
 	 * @return
 	 */
 	private static List<JstArg> paddingRhsParams(final IJstMethod rhsMethod,
 			final int lhsParamsLength) {
 		final List<JstArg> rhsParams = rhsMethod.getArgs();
 		final int rhsParamsLength = rhsParams.size();
 		final JstArg lastRhsParam = rhsParamsLength > 0 ? rhsParams
 				.get(rhsParamsLength - 1) : null;
 		// padding for rhs as it's last variable is variable lengthed
 		if (lhsParamsLength > rhsParamsLength/* && rhsParamsLength > 0 */) {
 			if (lastRhsParam != null && lastRhsParam.isVariable()) {
 				final List<JstArg> paddingRhsParams = new LinkedList<JstArg>(
 						rhsParams);
 				for (int i = rhsParamsLength; i < lhsParamsLength; i++) {
 					paddingRhsParams.add(lastRhsParam);
 				}
 				return paddingRhsParams;
 			} else {// last param exists and isn't variable
 				final List<JstArg> paddingRhsParams = new LinkedList<JstArg>(
 						rhsParams);
 				for (int i = rhsParamsLength; i < lhsParamsLength; i++) {
 					// adding Object param to accept lhs' arguments
 					paddingRhsParams.add(new JstArg(JstCache.getInstance()
 							.getType("Object"), "proxy", true));
 				}
 				return paddingRhsParams;
 			}
 		}
 		// bugfix by huzhou@ebay.com
 		// when lhs's param length is one smaller than rhs's, we further check
 		// if rhs' last param is variable lengthed
 		// if both condition met, we use the rhs's parameters excluding the last
 		// variable lengthed one
 		else if (lhsParamsLength == rhsParamsLength - 1
 				&& lastRhsParam.isVariable()) {
 			return rhsParams.subList(0, lhsParamsLength);
 		}
 		return rhsParams;
 	}
 
 	public static boolean isCastable(IJstType origin, IJstType castTo) {
 		final IJstType originTransformed = toJsNativeType(origin);
 		if (originTransformed != null) {
 			return isAssignableJsNativeType(originTransformed,
 					toJsNativeType(castTo));
 		} else {
 			return isAssignableVjoType(origin, castTo)
 					|| isAssignableVjoType(castTo, origin);
 		}
 	}
 
 	public static boolean isArbitary(IJstType exprValue) {
 		return exprValue != null
 				&& (VjoConstants.ARBITARY.equals(exprValue) || "Object"
 						.equals(exprValue.getName()));
 	}
 
 	public static boolean isBoolean(IJstType exprValue) {
 		if (exprValue instanceof JstVariantType) {
 			for (IJstType type : ((JstVariantType) exprValue).getVariantTypes()) {
 				if (isBoolean(type)) {
 					return true;
 				}
 			}
 			return false;
 		}
 		if (exprValue instanceof JstMixedType) {
 			for (IJstType type : ((JstMixedType) exprValue).getMixedTypes()) {
 				if (isBoolean(type)) {
 					return true;
 				}
 			}
 			return false;
 		}
 		if (isArbitary(exprValue)) {
 			return true;
 		}
 
 		if (exprValue != null) {
 			if (!VjoConstants.NativeTypes.getPrimitiveBooleanJstType().equals(
 					toJsNativeType(exprValue))) {
 				return false;
 			}
 		}
 
 		return true;
 	}
 
 	public static boolean isObject(IJstType exprValue) {
 		if (isArbitary(exprValue)) {
 			return true;
 		}
 
 		if (exprValue != null) {
 			if (!equals(VjoConstants.NativeTypes.getObjectJstType(),
 					toJsNativeType(exprValue))) {
 				return false;
 			}
 		}
 
 		return true;
 	}
 	
 	public static boolean isUndefined(IJstType exprValue) {
 //		if (isArbitary(exprValue)) {
 //			return true;
 //		}
 
 		if (exprValue != null) {
 			if (!exprValue.getName().equals(getUndefinedNativeType().getName())) {
 				return false;
 			}
 		}
 
 		return true;
 	}
 
 	public static boolean isVoid(IJstType exprValue) {
 		// if(isArbitary(exprValue)){
 		// return true;
 		// }
 		//
 		if (exprValue != null) {
 			if (equals(JstReservedTypes.Other.VOID, toJsNativeType(exprValue))) {
 				return true;
 			}
 		}
 
 		return false;
 	}
 
 	public static boolean isNumber(IJstType exprValue) {
 		if (exprValue instanceof JstVariantType) {
 			for (IJstType type : ((JstVariantType) exprValue).getVariantTypes()) {
 				if (isNumber(type)) {
 					return true;
 				}
 			}
 			return false;
 		}
 		if (exprValue instanceof JstMixedType) {
 			for (IJstType type : ((JstMixedType) exprValue).getMixedTypes()) {
 				if (isNumber(type)) {
 					return true;
 				}
 			}
 			return false;
 		}
 		if (isArbitary(exprValue)) {
 			return true;
 		}
 
 		if (exprValue != null) {
 			if (!equals(VjoConstants.NativeTypes.getNumberJstType(),
 					toJsNativeType(exprValue))) {
 				return false;
 			}
 		}
 
 		return true;
 	}
 
 	// public static boolean isDate(IJstType exprValue){
 	// if (exprValue instanceof JstVariantType) {
 	// for (IJstType type : ((JstVariantType)exprValue).getVariantTypes()) {
 	// if (isDate(type)) {
 	// return true;
 	// }
 	// }
 	// return false;
 	// }
 	// if(isArbitary(exprValue)){
 	// return true;
 	// }
 	//
 	// if(exprValue != null){
 	// if(exprValue != null && !
 	// ("Date".equals(exprValue.getSimpleName()))
 	// && LibManager.JS_NATIVE_LIB_NAME.equals(getGroupName(exprValue))){
 	// return false;
 	// }
 	// }
 	//
 	// return true;
 	// }
 
 	private static String getGroupName(IJstType type) {
 		if (type == null) {
 			return null;
 		}
 		final JstPackage pkg = type.getPackage();
 		if (pkg != null) {
 			return pkg.getGroupName();
 		}
 
 		return null;
 	}
 
 	public static boolean isString(IJstType exprValue) {
 		if (exprValue instanceof JstVariantType) {
 			for (IJstType type : ((JstVariantType) exprValue).getVariantTypes()) {
 				if (isString(type)) {
 					return true;
 				}
 			}
 			return false;
 		}
 		if (exprValue instanceof JstMixedType) {
 			for (IJstType type : ((JstMixedType) exprValue).getMixedTypes()) {
 				if (isString(type)) {
 					return true;
 				}
 			}
 			return false;
 		}
 		if (isArbitary(exprValue)) {
 			return true;
 		}
 
 		if (exprValue != null) {
 			if (!equals(VjoConstants.NativeTypes.getStringJstType(),
 					toJsNativeType(exprValue))) {
 				return false;
 			}
 		}
 
 		return true;
 	}
 
 	/**
 	 * @param oneUpper
 	 * @param secUpper
 	 * @param oneType
 	 * @param secType
 	 * @return boolean
 	 * 
 	 *         Sec type is param type , one type is arg type
 	 */
 	public static boolean isSuperType(boolean oneUpper, boolean secUpper,
 			IJstType oneType, IJstType secType) {
 		if (oneUpper && !secUpper) {// arg extends parameter supper
 			return false;
 		}
 		return isSuperType(oneType, secType);
 	}
 
 	public static boolean isSuperType(IJstType fromType, IJstType toType) {
 		if (fromType == null || toType == null || fromType.getName() == null
 				|| toType.getName() == null) {
 			return false;
 		}
 		if (isObject(toType)) {
 			return true;
 		}
 		List<String> list = new ArrayList<String>();
 		IJstType stype = fromType.getExtend();
 		while (stype != null && stype.getName() != null) {
 			list.add(stype.getName());
 			stype = stype.getExtend();
 		}
 		if (toType.getExtend() != null && toType.getExtend().getName() != null) {
 			if (fromType.equals(toType.getExtend())) { // Handle type is Object
 				if (toType.getPackage() != null) {// handle to type is template
 													// generic
 					return false;
 				}
 				return true;
 			}
 			return list.contains(toType.getExtend().getName());
 		}
 		return false;
 	}
 
 	public static IJstType getAttributedType(
 			final JstAttributedType attributedType) {
 		final IJstNode attributedBinding = attributedType.getJstBinding();
 		if (attributedBinding instanceof IJstProperty) {
 			return ((IJstProperty) attributedBinding).getType();
 		} else if (attributedBinding instanceof IJstMethod) {
 			return new JstFuncType(((IJstMethod) attributedBinding));
 		}
 
 		return null;
 	}
 }
