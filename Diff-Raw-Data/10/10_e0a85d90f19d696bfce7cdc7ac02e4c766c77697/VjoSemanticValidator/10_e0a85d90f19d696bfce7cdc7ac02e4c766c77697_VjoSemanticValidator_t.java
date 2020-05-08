 /*******************************************************************************
  * Copyright (c) 2005, 2012 eBay Inc.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  *******************************************************************************/
 package org.eclipse.vjet.dsf.jsgen.shared.validation.vjo;
 
 import java.util.List;
 
 import org.eclipse.vjet.dsf.jsgen.shared.util.JstDisplayUtils;
 import org.eclipse.vjet.dsf.jsgen.shared.validation.vjo.semantic.VjoConstants;
 import org.eclipse.vjet.dsf.jsgen.shared.validation.vjo.semantic.rules.VjoSemanticRuleRepo;
 import org.eclipse.vjet.dsf.jsgen.shared.validation.vjo.semantic.rules.rulectx.BaseVjoSemanticRuleCtx;
 import org.eclipse.vjet.dsf.jsgen.shared.validation.vjo.semantic.rules.util.AccessControlUtil;
 import org.eclipse.vjet.dsf.jsgen.shared.validation.vjo.semantic.rules.util.TypeCheckUtil;
 import org.eclipse.vjet.dsf.jsgen.shared.validation.vjo.semantic.validator.VjoJstBlockValidator;
 import org.eclipse.vjet.dsf.jsgen.shared.validation.vjo.semantic.validator.VjoObjLiteralValidator;
 import org.eclipse.vjet.dsf.jsgen.shared.validation.vjo.visitor.IVjoValidationListener;
 import org.eclipse.vjet.dsf.jsgen.shared.validation.vjo.visitor.IVjoValidationVisitorEvent;
 import org.eclipse.vjet.dsf.jsgen.shared.validation.vjo.visitor.VjoValidationVisitorState;
 import org.eclipse.vjet.dsf.jst.IInferred;
 import org.eclipse.vjet.dsf.jst.IJstMethod;
 import org.eclipse.vjet.dsf.jst.IJstNode;
 import org.eclipse.vjet.dsf.jst.IJstProperty;
 import org.eclipse.vjet.dsf.jst.IJstRefType;
 import org.eclipse.vjet.dsf.jst.IJstType;
 import org.eclipse.vjet.dsf.jst.declaration.JstArg;
 import org.eclipse.vjet.dsf.jst.declaration.JstArray;
 import org.eclipse.vjet.dsf.jst.declaration.JstAttributedType;
 import org.eclipse.vjet.dsf.jst.declaration.JstDeferredType;
 import org.eclipse.vjet.dsf.jst.declaration.JstFuncType;
 import org.eclipse.vjet.dsf.jst.declaration.JstFunctionRefType;
 import org.eclipse.vjet.dsf.jst.declaration.JstInferredType;
 import org.eclipse.vjet.dsf.jst.declaration.JstMethod;
 import org.eclipse.vjet.dsf.jst.declaration.JstMixedType;
 import org.eclipse.vjet.dsf.jst.declaration.JstObjectLiteralType;
 import org.eclipse.vjet.dsf.jst.declaration.JstPackage;
 import org.eclipse.vjet.dsf.jst.declaration.JstParamType;
 import org.eclipse.vjet.dsf.jst.declaration.JstProxyType;
 import org.eclipse.vjet.dsf.jst.declaration.JstRefType;
 import org.eclipse.vjet.dsf.jst.declaration.JstType;
 import org.eclipse.vjet.dsf.jst.declaration.JstTypeWithArgs;
 import org.eclipse.vjet.dsf.jst.declaration.JstVariantType;
 import org.eclipse.vjet.dsf.jst.declaration.JstWildcardType;
 import org.eclipse.vjet.dsf.jst.declaration.SynthOlType;
 import org.eclipse.vjet.dsf.jst.token.IExpr;
 import org.eclipse.vjet.vjo.lib.LibManager;
 
 /**
  * <p>
  * base semantic validator
  * provides
  * 	1) expression hold/lookup capabilities
  *  2) utilities for validators as keyword validation, rule fire, value normalizations
  *  3) visitor event listener interfaced
  * </p>
  * 
  *
  */
 public abstract class VjoSemanticValidator implements 
 	IVjoValidationListener{
 	
 	protected boolean isJavaKeyword(final String name){
 		final String[] arr = new String[VjoConstants.JAVA_ONLY_KEYWORDS.size()];
 		return isKeyword(name, VjoConstants.JAVA_ONLY_KEYWORDS.toArray(arr));
 	}
 	
 	protected boolean isVjoKeyword(final String name, final List<String> keywords){
 		final String[] arr = new String[keywords.size()];
 		return isKeyword(name, keywords.toArray(arr));
 	}
 	
 	protected boolean isVjoKeyword(final String name, final String[] keywords){
 		return isKeyword(name, keywords);
 	}
 
 	protected JstMethod lookUpMethod(IJstNode node){
 		if(node == null){
 			return null;
 		}
 		else if(node instanceof JstType){
 			return null;
 		}
 		else if(node instanceof JstMethod){
 			return (JstMethod)node;
 		}
 		
 		return lookUpMethod(node.getParentNode());
 	}
 	
 	private boolean isKeyword(final String name, final String[] keywords){
 		
 		if (( name.startsWith("\"") && name.endsWith("\"") )
 				|| (name.startsWith("\'") && name.endsWith("\'") )){
 			return false;
 		}
 		
 		for(String vjoKeyword : keywords){
 			if(vjoKeyword.equals(name)){
 				return true;
 			}
 		}
 		return false;
 	}
 	
 	protected VjoSemanticValidator(){
 		
 	}
 
 	protected IJstType getTargetType(final IJstType jstType){
 		if(jstType != null && jstType instanceof JstProxyType){
 			return ((JstProxyType)jstType).getType();
 		}
 		return jstType;
 	}
 	
 	protected IJstType getKnownType(final VjoValidationCtx ctx,
 			final IJstType targetType, 
 			IJstType unknownType){
 		if(targetType == null || unknownType == null){
 			return VjoConstants.ARBITARY;
 		}
 		
 		if (unknownType instanceof JstParamType) {
 			return VjoConstants.ARBITARY; //resolved
 		}
 		
 		if (unknownType instanceof IJstRefType) {
 			unknownType = ((IJstRefType)unknownType).getReferencedNode();
 		}
 		
 		if (unknownType instanceof JstArray) {
 			return unknownType;// = ((JstArray)unknownType).getComponentType();
 		}
 		
 		if(unknownType instanceof JstFunctionRefType){
 			return VjoConstants.ARBITARY;// resolved
 		}
 		
 		if(unknownType instanceof JstObjectLiteralType){
 			return VjoConstants.ARBITARY;// resolved
 		}
 		
 		
 		if(unknownType instanceof JstAttributedType){
 
 			final IJstType typeSpacedUnknownType = ctx.getTypeSpaceType(((JstAttributedType) unknownType).getAttributorType());
 			// contains attribute?
 			
 			if(typeSpacedUnknownType==null){
 				return null;
 			}
 			if(typeSpacedUnknownType.getName().equals("Global")){
 				return unknownType;
 			}
 			
 			if(((JstAttributedType) unknownType).getJstBinding() ==null){
 				return null;
 			}
 			return unknownType;
 			
 		}
 
 		// TODO should otype really require needs? disable for now
 //		if (unknownType instanceof IJstOType && unknownType.getParentNode() != null){
 //			unknownType = unknownType.getParentNode().getOwnerType();
 //		}
 		
 		final JstPackage jstPackage = unknownType.getPackage();
 		if (jstPackage != null){
 			if(jstPackage.getGroupName().length() > 0) {
 				return VjoConstants.ARBITARY; //resolved
 			}
 		}
 		
 		final IJstType typeSpacedUnknownType = ctx.getTypeSpaceType(unknownType);
 		final JstPackage unknownPackage = typeSpacedUnknownType.getPackage();
 		if(unknownPackage != null){
 			final String unknownGroupName = unknownPackage.getGroupName();
 			//native groups
 			if(LibManager.JAVA_PRIMITIVE_LIB_NAME.equals(unknownGroupName) 
 					|| LibManager.JS_NATIVE_GLOBAL_LIB_NAME.equals(unknownGroupName) 
 					|| LibManager.JS_NATIVE_LIB_NAME.equals(unknownGroupName) 
 					|| LibManager.VJO_BASE_LIB_NAME.equals(unknownGroupName)
 					|| LibManager.VJO_JAVA_LIB_NAME.equals(unknownGroupName)){
 				return typeSpacedUnknownType;
 			}
 		}
 		//These 2 exceptional cases should not present, but due to parser's result type not correct
 		if("Object".equals(typeSpacedUnknownType.getSimpleName())){
 			return typeSpacedUnknownType;
 		}
 		
 		//Added  by  Eric.Ma on 201005025 for jira 263 Otype validation bug
 		if(unknownType != null && unknownType instanceof JstObjectLiteralType){
 			return unknownType;
 		}
 		//End of Added
 		
 	
 		
 		//dependencies types
 		final String unknownTypeName = unknownPackage != null && "this.vj$".equals(unknownPackage.getName()) ? unknownType.getSimpleName() : unknownType.getName();
 		for(IJstType depType : ctx.getDependencyVerifier(targetType).getDirectDependenciesFilteredByGroup(targetType)){
 			if (depType == null || depType.getSimpleName() == null){
 				continue;
 			}
 			else if(depType.getName().equals(unknownTypeName)){
 				return depType;
 			}
 			else if(depType.getSimpleName().equals(unknownTypeName)){
 				return depType;
 			}
 		}
 		// check secondary types
 
 		List<? extends IJstType> secondaryTypes =  targetType.getSecondaryTypes();
 		if (secondaryTypes != null && !secondaryTypes.isEmpty()) {
 			String typeName = unknownTypeName;
 			if(unknownType instanceof JstAttributedType){
 				JstAttributedType atype = (JstAttributedType)unknownType;
 				typeName = atype.getAttributorType().getName();
 			}
 			for (IJstType secondaryType : secondaryTypes) {
 				if (secondaryType.getName().equals(typeName))
 					return secondaryType;
 			}
 		}		
 		// check param types
 		List<JstParamType> paramTypeList = targetType.getParamTypes();
 		
 		if (paramTypeList != null && !paramTypeList.isEmpty()) {
 			for (JstParamType paramType : paramTypeList) {
 				if (paramType.getSimpleName().equals(unknownTypeName))
 					return paramType;
 			}
 		}
 		
 		return null;
 	}
 	
 	/**
 	 * <p> log the semantic problem with the ctx's node
 	 * stop validation if problem's policy is met
 	 * </p>
 	 * @param <Ctx>
 	 * @param ctx
 	 * @param rule
 	 * @param ruleCtx
 	 * @return
 	 * @throws VjoValidationRuntimeException
 	 */
 	public <Ctx extends IVjoSemanticRuleCtx> boolean satisfyRule(final VjoValidationCtx ctx,
 			final IVjoSemanticRule<Ctx> rule, 
 			final Ctx ruleCtx)
 		throws VjoValidationRuntimeException{
 		ruleCtx.setMode(ctx.getValidationMode());
 		final VjoSemanticProblem problem = rule.fire(ruleCtx);
 		if(problem == null){
 			return true;
 		} 
 		else{
 			ctx.addProblem(ruleCtx.getNode(), problem);
 			final VjoSemanticRulePolicy policy = VjoGroupRulesCache.getInstance().getRulePolicy(ctx.getGroupId(),rule);
 			if(policy.stopOnFirstError()){
 				//bugfix, problems are only added to ctx when the validator is being removed
 				//exception could shortcut the happening of this, adding problems here instead
 				throw new VjoValidationRuntimeException(problem.toString());
 			}
 			return false;
 		}
 	}
 
 	public abstract List<Class<? extends IJstNode>> getTargetNodeTypes();
 	
 	protected void onPreAllChildrenEvent(final IVjoValidationVisitorEvent event){
 		//do nothing
 	}
 	
 	protected void onPreChildEvent(final IVjoValidationVisitorEvent event){
 		//do nothing
 	}
 
 	protected void onPostAllChildrenEvent(final IVjoValidationVisitorEvent event){
 		//do nothing
 	}
 	
 	protected void onPostChildEvent(final IVjoValidationVisitorEvent event){
 		//do nothing
 	}
 	
 	/**
 	 * @see IVjoValidationListener
 	 */
 	public void onEvent(final IVjoValidationVisitorEvent event){ 
 		if(VjoValidationVisitorState.BEFORE_ALL_CHILDREN.equals(event.getVisitState())){
 			onPreAllChildrenEvent(event);
 		}
 		else if(VjoValidationVisitorState.BEFORE_CHILD.equals(event.getVisitState())){
 			onPreChildEvent(event);
 		}
 		else if(VjoValidationVisitorState.AFTER_CHILD.equals(event.getVisitState())){
 			onPostChildEvent(event);
 		}
 		else if(VjoValidationVisitorState.AFTER_ALL_CHILDREN.equals(event.getVisitState())){
 			onPostAllChildrenEvent(event);
 		}
 	}
 	
 	protected void checkAndReportMissingParamTypeUpperBound(IJstType ownerType, final VjoValidationCtx ctx, List<JstParamType> listParamTypes) {
 		
 		for (JstParamType paramType : listParamTypes) {			
 			List<IJstType> bounds = paramType.getBounds();
 			
 			if (bounds == null || bounds.isEmpty()) {
 				continue;
 			}
 		
 			IJstType upperBound = bounds.get(0);
 			
 			checkAndReportMissingImportProblem(ownerType, ownerType, ctx, upperBound);
 		}		
 	}
 	
 	protected boolean checkAndReportMissingImportProblem(IJstType ownerType, IJstNode node, final VjoValidationCtx ctx, IJstType targetType) {
 		if (targetType instanceof IInferred) {
 			return false; //no import requirement for inferred type
 		}
 		
 		IJstType knownType = getKnownType(ctx, ownerType, targetType);
 		
 		if(ownerType != null 
 				&& knownType == null
 				&& targetType.getPackage() == null
 				&& !"ERROR_UNDEFINED_TYPE".equals(targetType.getSimpleName())){
 			//report problem, type unknown
 			if(targetType.getPackage() == null){
 				if(!ctx.getMissingImportTypes().contains(targetType)){
 					ctx.addMissingImportType(targetType);
 				}
 				
 				final BaseVjoSemanticRuleCtx ruleCtx = new BaseVjoSemanticRuleCtx(node, ctx.getGroupId(), new String[]{targetType.getName()});
 				satisfyRule(ctx, VjoSemanticRuleRepo.getInstance().UNKNOWN_TYPE_MISSING_IMPORT, ruleCtx);
 				//as type is unknown, shouldn't raise subsequent validation errors in its usages according to Justin's mail
 				return true;
 			}
 		}
 		
 		return false;
 	}
 	
 	protected void validateComplexType(final VjoValidationCtx ctx,
 			final IJstNode jstNode,
 			final String display, 
 			IJstType type) {
 		if(type == null){
 			return;
 		}
 		else if(type instanceof JstArray){
 			validateComplexType(ctx, jstNode, display, ((JstArray)type).getComponentType());
 		}
 		else if(type instanceof JstAttributedType){
 			validateAttributedType(ctx, jstNode, display, (JstAttributedType)type);
 		}
 		else if (type instanceof JstVariantType) {
 			validateVariantType(ctx, jstNode, display, (JstVariantType)type);
 		}
 		else if (type instanceof JstMixedType) {
 			validateMixedType(ctx, jstNode, display, (JstMixedType)type);
 		}
 		else if(type instanceof JstTypeWithArgs){
 			validateJstWithArgs(jstNode.getOwnerType(), jstNode, display, ctx, (JstTypeWithArgs)type);
 		}
 		else if(type instanceof JstWildcardType){
 			validateComplexType(ctx, jstNode, display, ((JstWildcardType)type).getType());
 		}
 		else if(type instanceof IJstRefType){
 			validateComplexType(ctx, jstNode, display, ((IJstRefType)type).getReferencedNode());
 		}
 		else if(type instanceof JstFuncType){
 			final JstFuncType declaredFuncType = (JstFuncType) type;
 			final IJstMethod declaredMethod = declaredFuncType.getFunction();
 			if(declaredMethod != null){
 				validateComplexType(ctx, jstNode, declaredMethod.getName().getName(), declaredMethod.getRtnType());
 				for(JstArg arg: declaredMethod.getArgs()){
 					validateComplexType(ctx, arg, arg.getName(), arg.getType());
 				}
 			}
 		}
 		else if(type instanceof JstDeferredType){
 			final JstDeferredType deferredType = (JstDeferredType)type;
 			final IJstType resolvedType = deferredType.getResolvedType();
 			if(resolvedType != null){
 				validateJstDeferredType(ctx, jstNode, deferredType,
 						resolvedType);
 			}
 		}
 		else if(type instanceof SynthOlType){
 			final SynthOlType synthOlType = (SynthOlType)type;
 			if(synthOlType.getResolvedOTypes()!=null){
 				for(IJstType resolvedOType :  synthOlType.getResolvedOTypes()){
 					
 					if(resolvedOType != null && isOType(resolvedOType)){//resolved type must be an otype
 						validateSynthOlType(ctx, jstNode, synthOlType,
 								(JstObjectLiteralType)resolvedOType);
 					}
 				}
 			}
 			
 		}
 		else{
 			//validating if the type isn't resolved, which results in a missing import etc.
 			validateUnknownTypes(ctx, jstNode, type);
 		}
 	}
 
 	private boolean isOType(final IJstType resolvedOType) {
 		return resolvedOType instanceof JstObjectLiteralType;
 	}
 
 	private void validateUnknownTypes(final VjoValidationCtx ctx,
 			final IJstNode jstNode, IJstType type) {
 		final VjoSemanticRuleRepo ruleRepo = VjoSemanticRuleRepo.getInstance();
 		final IJstType ownerType = jstNode.getOwnerType() != null ? jstNode.getOwnerType() : ctx.getJstNode().getOwnerType();
 		if(ownerType != null){
 			IJstType knownType = getKnownType(ctx, ownerType, type);
 			if(ownerType != null 
 					&& knownType == null
 					&& !"ERROR_UNDEFINED_TYPE".equals(type.getSimpleName())
 					&& !JstWildcardType.DEFAULT_NAME.equals(type.getSimpleName())){
 				//report problem, type unknown
 				if(type instanceof JstInferredType){
 					type = ((JstInferredType) type).getType();
 				}
 				
 				if(type instanceof JstMixedType){
 					for (IJstType mixedType : ((JstMixedType) type).getMixedTypes()) {
 						checkUnresolvedType(ctx, jstNode, mixedType, ruleRepo);
 					}
 				}else if(type instanceof JstVariantType){
 					for (IJstType mixedType : ((JstVariantType) type).getVariantTypes()) {
 						checkUnresolvedType(ctx, jstNode, mixedType, ruleRepo);
 					}
 				}else{
 					
 					checkUnresolvedType(ctx, jstNode, type, ruleRepo);
 				}
 			}
 		}
 	}
 
 	private void checkUnresolvedType(final VjoValidationCtx ctx,
 			final IJstNode jstNode, IJstType type,
 			final VjoSemanticRuleRepo ruleRepo) {
 		if(type instanceof JstRefType){
 			return;
 		}
 		if(!ctx.getUnresolvedTypes().contains(type.getName())){
 			if(!ctx.getMissingImportTypes().contains(type)){
 				ctx.addMissingImportType(type);
 			}
 			
 			final BaseVjoSemanticRuleCtx ruleCtx = new BaseVjoSemanticRuleCtx(jstNode, ctx.getGroupId(), new String[]{type.getName()});
 			satisfyRule(ctx, ruleRepo.UNKNOWN_TYPE_MISSING_IMPORT, ruleCtx);
 		}
 	}
 
 	private void validateJstDeferredType(final VjoValidationCtx ctx,
 			final IJstNode jstNode, final JstDeferredType deferredType,
 			final IJstType resolvedType) {
 		boolean satisfied = false;
		if(deferredType.getCandidateTypes().size()==0){
			return;
		}
 		final StringBuilder candidates = new StringBuilder().append('{');
 		for(IJstType candidateType: deferredType.getCandidateTypes()){
 			if(satisifies(resolvedType, candidateType)){
 				satisfied = true;
 			}
 			else{
 				candidates.append(candidateType.getName()).append(',');
 			}
 		}
 		if(!satisfied){
 			//report problem
 			final BaseVjoSemanticRuleCtx ruleCtx = new BaseVjoSemanticRuleCtx(jstNode, ctx.getGroupId(), new String[]{resolvedType.getName(), candidates.append('}').toString(), resolvedType.getName()});
 			satisfyRule(ctx, VjoSemanticRuleRepo.getInstance().ASSIGNABLE, ruleCtx);
 		}
 	}
 	
 	private boolean satisifies(final IJstType resolvedType,
 			IJstType candidateType) {
 		if(candidateType == null){
 			return true;
 		}
 		else if(candidateType instanceof JstDeferredType){
 			for(IJstType recursiveCandidateType: ((JstDeferredType)candidateType).getCandidateTypes()){
 				if(satisifies(resolvedType, recursiveCandidateType)){
 					return true;
 				}
 			}
 			return false;
 		}
 		else{
 			return TypeCheckUtil.isAssignable(resolvedType, candidateType);
 		}
 	}
 	
 	/**
 	 * this validates the object literal against the otype definition as the following
 	 * 1. check if all required properties from otype presents in the object literal
 	 * 2. check if all properties given by the object literal, if has def in otype
 	 * 3. the type check of the properties are moved to the @see {@link VjoObjLiteralValidator}
 	 * @param ctx
 	 * @param jstNode
 	 * @param synthOlType
 	 * @param resolvedOType
 	 */
 	private void validateSynthOlType(final VjoValidationCtx ctx,
 			final IJstNode jstNode, final SynthOlType synthOlType,
 			final JstObjectLiteralType resolvedOType) {
 		if(synthOlType == null
 				|| resolvedOType == null
 				|| !isOType(resolvedOType)){
 			throw new IllegalArgumentException("obj literal couldn't be null, otype couldn't be null, otype must be OType");
 		}
 		
 		final boolean hasOptional  = resolvedOType.hasOptionalFields();
 		final StringBuilder noneMatched = new StringBuilder().append('{');
 		boolean matched = true;
 		for(IJstProperty pty: resolvedOType.getAllPossibleProperties(false, true)){
 			final String ptyName = pty.getName() != null ? pty.getName().getName() : null;
 			if(ptyName == null){
 				continue;
 			}
 			
 			final IJstProperty ptyMatched = synthOlType.getProperty(ptyName);
 			if(ptyMatched == null){
 				if(hasOptional && resolvedOType.isOptionalField(pty)){
 					continue;
 				}
 				//bugfix by huzhou@ebay.com to allow otype properties lookup in method of object literal
 				final IJstMethod matchMethod = synthOlType.getMethod(ptyName);
 				if(matchMethod != null){
 					if(pty.getType() instanceof JstFuncType){
 						continue;
 					}
 				}
 				//error
 				noneMatched.append(ptyName).append(',');
 				matched = false;
 			}
 		}
 		if(!matched){
 			noneMatched.setCharAt(noneMatched.length() - 1, '}');
 			final BaseVjoSemanticRuleCtx ruleCtx = new BaseVjoSemanticRuleCtx(jstNode, ctx.getGroupId(), new String[]{noneMatched.toString()});
 			satisfyRule(ctx, VjoSemanticRuleRepo.getInstance().OBJLITERAL_ASSIGNABLE, ruleCtx);
 		}
 	}
 	
 	protected void validateVariantType(final VjoValidationCtx ctx,
 		final IJstNode jstNode,
 		final String display, 
 		JstVariantType declaredType) {
 		for (IJstType type : declaredType.getVariantTypes()) {
 			validateComplexType(ctx, jstNode, display, type);
 		}		
 	}
 	
 	protected void validateMixedType(final VjoValidationCtx ctx,
 		final IJstNode jstNode,
 		final String display, 
 		JstMixedType declaredType) {
 		for (IJstType type : declaredType.getMixedTypes()) {
 			validateComplexType(ctx, jstNode, display, type);
 		}		
 	}
 	
 	protected void validateAttributedType(final VjoValidationCtx ctx,
 			final IJstNode jstNode,
 			final String display, 
 			JstAttributedType declaredType) {
 		//enhancement by huzhou to validate none-existing attribute bindings
 		final VjoSemanticRuleRepo ruleRepo = VjoSemanticRuleRepo.getInstance();
 		final JstAttributedType attributedType = (JstAttributedType)declaredType;
 		final String attributedName = attributedType.getAttributeName();
 		final IJstNode attributedBinding = attributedType.getJstBinding();
 		
 		validateComplexType(ctx, jstNode, display, attributedType.getAttributorType());
 		
 		if(attributedBinding == null){
 			boolean specialReasonFound = false;
 			//handle this.vj$ attributor case differently
 			if(attributedType.getName() != null 
 					&& attributedType.getName().startsWith("this.vj$.")){
 				satisfyRule(ctx, ruleRepo.ATTRIBUTOR_SHOULD_NOT_USE_VJ_RUNTIME,
 						new BaseVjoSemanticRuleCtx
 							(jstNode, ctx.getGroupId(), new String[]{attributedName, display}));
 				specialReasonFound = true;
 			}
 			else if(attributedType.isStaticAttribute()){
 				if(attributedType.getMethod(attributedName, false, true) != null){
 					//none static attribute referenced staticly
 					satisfyRule(ctx, ruleRepo.NONE_STATIC_METHOD_SHOULD_NOT_BE_ACCESSED_FROM_STATIC_SCOPE,
 							new BaseVjoSemanticRuleCtx
 								(jstNode, ctx.getGroupId(), new String[]{attributedName, display}));
 					specialReasonFound = true;
 				}
 				else if(attributedType.getProperty(attributedName, false, true) != null){
 					//none static attribute referenced staticly
 					satisfyRule(ctx, ruleRepo.NONE_STATIC_PROPERTY_SHOULD_NOT_BE_ACCESSED_FROM_STATIC_SCOPE,
 							new BaseVjoSemanticRuleCtx
 								(jstNode, ctx.getGroupId(), new String[]{attributedName, display}));
 					specialReasonFound = true;
 				}
 			}
 			else if(!attributedType.isStaticAttribute()){
 				if(attributedType.getMethod(attributedName, true, true) != null){
 					//static attribute referenced none-staticly
 					satisfyRule(ctx, ruleRepo.STATIC_METHOD_SHOULD_NOT_BE_ACCESSED_FROM_NONE_STATIC_SCOPE,
 							new BaseVjoSemanticRuleCtx
 								(jstNode, ctx.getGroupId(), new String[]{attributedName, display}));
 					specialReasonFound = true;
 				}
 				else if(attributedType.getProperty(attributedName, true, true) != null){
 					//static attribute referenced none-staticly
 					satisfyRule(ctx, ruleRepo.STATIC_PROPERTY_SHOULD_NOT_BE_ACCESSED_FROM_NONE_STATIC_SCOPE,
 							new BaseVjoSemanticRuleCtx
 								(jstNode, ctx.getGroupId(), new String[]{attributedName, display}));
 					specialReasonFound = true;
 				}
 			}
 			
 			if(!specialReasonFound){
 				final IJstType ownerType = jstNode.getOwnerType() != null ? jstNode.getOwnerType() : ctx.getJstNode().getOwnerType();
 				if(ownerType != null){
 					final IJstType knownType = getKnownType(ctx, ownerType, attributedType.getAttributorType());
 					if(knownType == null){
 						//the error is already reported by missing import, no need to report property undefined again
 						specialReasonFound = true;
 					}
 				}
 			}
 
 			if(!specialReasonFound){
 				satisfyRule(ctx, ruleRepo.PROPERTY_SHOULD_BE_DEFINED,
 						new BaseVjoSemanticRuleCtx
 							(jstNode, ctx.getGroupId(), new String[]{attributedName, display}));
 			}
 		}
 		else if(attributedBinding instanceof IJstMethod){
 			//enhancement by huzhou to validate the attribute's visibility
 			final IJstMethod attributedMethod = (IJstMethod)attributedBinding;
 			final IJstType ownerType = attributedMethod.getOwnerType();
 			final IJstType callerType = jstNode.getOwnerType() != null ? jstNode.getOwnerType() : ctx.getJstNode().getOwnerType();
 			if(attributedMethod != null && ownerType != null && callerType != null){
 				final boolean visible = AccessControlUtil.isVisible(attributedMethod, ownerType, callerType);
 				if(!visible){
 					satisfyRule(ctx, ruleRepo.METHOD_SHOULD_BE_VISIBLE,
 							new BaseVjoSemanticRuleCtx
 								(jstNode, ctx.getGroupId(), new String[]{attributedName, display}));
 				}
 			}
 		}
 		else if(attributedBinding instanceof IJstProperty){
 			final IJstProperty attributedProperty = (IJstProperty)attributedBinding;
 			final IJstType ownerType = attributedProperty.getOwnerType();
 			final IJstType callerType = jstNode.getOwnerType() != null ? jstNode.getOwnerType() : ctx.getJstNode().getOwnerType();
 			if(attributedProperty != null && ownerType != null && callerType != null){
 				final boolean visible = AccessControlUtil.isVisible(attributedProperty, ownerType, callerType);
 				if(!visible){
 					satisfyRule(ctx, ruleRepo.PROPERTY_SHOULD_BE_VISIBLE,
 							new BaseVjoSemanticRuleCtx
 								(jstNode, ctx.getGroupId(), new String[]{attributedName, display}));
 				}
 			}
 		}
 	}
 	
 	protected boolean validateJstWithArgs(final IJstType ownerType, 
 			final IJstNode node, 
 			final String display,
 			final VjoValidationCtx ctx, 
 			final JstTypeWithArgs typeWithArgs) {
 		List<JstParamType> listParamTypes = typeWithArgs.getType().getParamTypes();
 		List<IJstType> listArgTypes = typeWithArgs.getArgTypes();
 		
 		int argSize = listArgTypes.size();	
 		int paramSize = listParamTypes.size();			
 		
 		final VjoSemanticRuleRepo ruleRepo = VjoSemanticRuleRepo.getInstance();
 		if (argSize != paramSize) {
 			String[] arguments = new String[2];
 			arguments[0] = typeWithArgs.getType().getSimpleName() + ((JstType)typeWithArgs.getType()).getParamsDecoration();
 			arguments[1] = typeWithArgs.getSimpleName() + typeWithArgs.getArgsDecoration();
 			final BaseVjoSemanticRuleCtx ruleCtx = new BaseVjoSemanticRuleCtx(node, ctx.getGroupId(), arguments);
 			satisfyRule(ctx, ruleRepo.GENERIC_PARAM_NUM_MISMATCH, ruleCtx);
 			return false;
 		}
 		
 		for (int i = 0; i < argSize; i++) {
 			IJstType argType = listArgTypes.get(i);
 			validateComplexType(ctx, node, display, argType);
 			
 			if (checkAndReportMissingImportProblem(ownerType, node, ctx, argType)) {
 				return false;
 			}
 			
 			List<IJstType> bounds = listParamTypes.get(i).getBounds();
 			
 			if (bounds == null || bounds.isEmpty()) {
 				continue;
 			}
 		
 			IJstType upperBound = bounds.get(0);
 						
 			if (!satisifies(upperBound, argType)) {
 				String[] arguments = new String[2];
 				arguments[0] = typeWithArgs.getType().getSimpleName() + ((JstType)typeWithArgs.getType()).getParamsDecoration();
 				arguments[1] = displayType(typeWithArgs);
 				final BaseVjoSemanticRuleCtx ruleCtx = new BaseVjoSemanticRuleCtx(node, ctx.getGroupId(), arguments);
 				satisfyRule(ctx, ruleRepo.GENERIC_PARAM_TYPE_MISMATCH, ruleCtx);
 				return false;
 			}
 		}
 		
 		return true;		
 	}
 	
 	/**
 	 * recursively display type's simple name
 	 * @param type
 	 * @return
 	 */
 	protected String displayType(final IJstType type){
 		
 		final StringBuilder sb = new StringBuilder();
 		
 		if(type == null){
 			return "NULL";
 		}
 		else if(type instanceof JstArray){
 			return sb.append(displayType(((JstArray)type).getComponentType())).append("[]").toString();
 		}
 		else if(type instanceof JstWildcardType){
 			sb.append("?");
 			final JstWildcardType p = (JstWildcardType)type;
 			if (((JstWildcardType)p).isUpperBound()){
 				sb.append(" extends ").append(displayType(p.getType()));
 			}
 			else if (((JstWildcardType)p).isLowerBound()){
 				sb.append(" super ").append(displayType(p.getType()));
 			}
 			return sb.toString();
 		}
 		else if(type instanceof JstTypeWithArgs){
 			final JstTypeWithArgs withArgsType = (JstTypeWithArgs)type;
 			sb.append(displayType(withArgsType.getType()));
 			for(IJstType argType: withArgsType.getArgTypes()){
 				sb.append(displayType(argType)).append(',');
 			}
 			return sb.charAt(sb.length() - 1) == ',' ? sb.substring(0, sb.length() - 2) : sb.toString();
 		}
 		else if(type instanceof JstAttributedType){
 			final JstAttributedType attributedType = (JstAttributedType)type;
 			final IJstNode node = attributedType.getJstBinding();
 			if(node instanceof IJstType){
 				return displayType((IJstType)node);
 			}
 			else if(node instanceof IJstProperty){
 				return displayType(((IJstProperty)node).getType());
 			}
 			else if(node instanceof IJstMethod){
 				return displayType(((IJstMethod)node).getRtnType());
 			}
 			else{
 				return "UNKNOWN";
 			}
 		}
 		else if(type instanceof JstFuncType){
 			final JstFuncType jstFuncType = (JstFuncType)type;
 			final IJstMethod func = jstFuncType.getFunction();
 			if(func != null && !func.isDispatcher()){
 				return JstDisplayUtils.getFullMethodString(func, null, false);
 			}
 			else{
 				return "FUNCTION";
 			}
 		}
 		else{
 			return type.getSimpleName();
 		}
 	}
 
 	//added by huzhou@ebay.com
 	//to allow all validators to use
 	//its main purpose is to verify if a type is known in the typespace or not
 	//it looks for type's direct dependencies 1st
 	//then it falls to typespace to look for global types
 	//after all these effort, it gives false as result
 	/**
 	 * @see VjoJstBlockValidator which used this util to verify {needs, inherits, satisfies, mixin, inactive needs} etc.
 	 * @see VjoMtdInvocationValidator which will use this to verify vjo.getType() invocation
 	 */
 	public static void validateTypeResolution(final VjoSemanticValidator validator,
 			final VjoValidationCtx ctx,
 			final IJstType syntaxOwnerType,
 			final IVjoDependencyVerifiable depVerifier, 
 			final IExpr arg,
 			final String typeName) {
 		if(!depVerifier.verify(syntaxOwnerType, typeName)){
 			if(typeName.length() > 0){//bugfix by roy, empty type name will be given alone
 				final BaseVjoSemanticRuleCtx ruleCtx = new BaseVjoSemanticRuleCtx(arg, ctx.getGroupId(),new String[]{typeName});
 				validator.satisfyRule(ctx, VjoSemanticRuleRepo.getInstance().UNKNOWN_TYPE_NOT_IN_TYPE_SPACE, ruleCtx);
 			}
 			
 			ctx.addUnresolvedType(typeName);
 		}
 	}
 }
