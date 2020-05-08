 /*******************************************************************************
  * Copyright (c) 2005, 2012 eBay Inc.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  *******************************************************************************/
 package org.eclipse.vjet.dsf.jsgen.shared.validation.vjo.semantic.validator;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import org.eclipse.vjet.dsf.jsgen.shared.ids.VjoSyntaxProbIds;
 import org.eclipse.vjet.dsf.jsgen.shared.validation.vjo.VjoSemanticProblem;
 import org.eclipse.vjet.dsf.jsgen.shared.validation.vjo.VjoSemanticValidator;
 import org.eclipse.vjet.dsf.jsgen.shared.validation.vjo.VjoValidationCtx;
 import org.eclipse.vjet.dsf.jsgen.shared.validation.vjo.VjoValidationRuntimeException;
 import org.eclipse.vjet.dsf.jsgen.shared.validation.vjo.semantic.VjoConstants;
 import org.eclipse.vjet.dsf.jsgen.shared.validation.vjo.semantic.rules.VjoSemanticRuleRepo;
 import org.eclipse.vjet.dsf.jsgen.shared.validation.vjo.semantic.rules.rulectx.BaseVjoSemanticRuleCtx;
 import org.eclipse.vjet.dsf.jsgen.shared.validation.vjo.semantic.rules.rulectx.ClassBetterStartWithCapitalLetterRuleCtx;
 import org.eclipse.vjet.dsf.jsgen.shared.validation.vjo.semantic.rules.rulectx.MTypeShouldNotBeAsInnerTypeRuleCtx;
 import org.eclipse.vjet.dsf.jsgen.shared.validation.vjo.semantic.rules.rulectx.MTypeShouldNotHaveInnerTypesRuleCtx;
 import org.eclipse.vjet.dsf.jsgen.shared.validation.vjo.semantic.rules.rulectx.TypeNameShouldNotBeEmptyRuleCtx;
 import org.eclipse.vjet.dsf.jsgen.shared.validation.vjo.semantic.rules.util.TypeCheckUtil;
 import org.eclipse.vjet.dsf.jsgen.shared.validation.vjo.util.MixinValidationUtil;
 import org.eclipse.vjet.dsf.jsgen.shared.validation.vjo.visitor.IVjoValidationPostAllChildrenListener;
 import org.eclipse.vjet.dsf.jsgen.shared.validation.vjo.visitor.IVjoValidationPreAllChildrenListener;
 import org.eclipse.vjet.dsf.jsgen.shared.validation.vjo.visitor.IVjoValidationVisitorEvent;
 import org.eclipse.vjet.dsf.jst.FileBinding;
 import org.eclipse.vjet.dsf.jst.IJstMethod;
 import org.eclipse.vjet.dsf.jst.IJstNode;
 import org.eclipse.vjet.dsf.jst.IJstProperty;
 import org.eclipse.vjet.dsf.jst.IJstRefType;
 import org.eclipse.vjet.dsf.jst.IJstType;
 import org.eclipse.vjet.dsf.jst.IJstTypeReference;
 import org.eclipse.vjet.dsf.jst.IScriptProblem;
 import org.eclipse.vjet.dsf.jst.ISynthesized;
 import org.eclipse.vjet.dsf.jst.JstSource;
 import org.eclipse.vjet.dsf.jst.JstSource.IBinding;
 import org.eclipse.vjet.dsf.jst.declaration.JstArg;
 import org.eclipse.vjet.dsf.jst.declaration.JstConstructor;
 import org.eclipse.vjet.dsf.jst.declaration.JstMethod;
 import org.eclipse.vjet.dsf.jst.declaration.JstModifiers;
 import org.eclipse.vjet.dsf.jst.declaration.JstObjectLiteralType;
 import org.eclipse.vjet.dsf.jst.declaration.JstParamType;
 import org.eclipse.vjet.dsf.jst.declaration.JstProxyMethod;
 import org.eclipse.vjet.dsf.jst.declaration.JstProxyProperty;
 import org.eclipse.vjet.dsf.jst.declaration.JstProxyType;
 import org.eclipse.vjet.dsf.jst.declaration.JstType;
 import org.eclipse.vjet.dsf.jst.declaration.JstTypeWithArgs;
 import org.eclipse.vjet.dsf.jst.util.JstTypeHelper;
 import org.eclipse.vjet.vjo.lib.TsLibLoader;
 
 public class VjoJstTypeValidator 
 	extends VjoSemanticValidator
 	implements IVjoValidationPreAllChildrenListener,
 		IVjoValidationPostAllChildrenListener {
 	
 	private static List<Class<? extends IJstNode>> s_targetTypes;
 	
 	private static Pattern s_typePattern;
 	
 	static{
 		s_targetTypes = new ArrayList<Class<? extends IJstNode>>();
 		s_targetTypes.add(JstType.class);
 		
 		s_typePattern = Pattern.compile("([a-zA-Z_$][0-9a-zA-Z_$]*\\.)*[a-zA-Z_$][0-9a-zA-Z_$]*");
 	}
 	
 	@Override
 	public List<Class<? extends IJstNode>> getTargetNodeTypes() {
 		return s_targetTypes;
 	}
 
 	@Override
 	public void onPreAllChildrenEvent(final IVjoValidationVisitorEvent event){
 		final VjoValidationCtx ctx = event.getValidationCtx();
 		final IJstNode jstNode = event.getVisitNode();
 		if(!(jstNode instanceof JstType)){
 			return;
 		}
 		final JstType jstType = (JstType)jstNode;
 		validateBeforeAll(ctx, jstType);
 	}
 	
 	@Override
 	public void onPostAllChildrenEvent(final IVjoValidationVisitorEvent event){
 		final VjoValidationCtx ctx = event.getValidationCtx();
 		final IJstNode jstNode = event.getVisitNode();
 		if(!(jstNode instanceof JstType)){
 			return;
 		}
 		final JstType jstType = (JstType)jstNode;
 		validateAfterAll(ctx, jstType);
 	}
 	
 	private void validateAfterAll(final VjoValidationCtx ctx,
 			final JstType jstType) {
 		
 		validateTypePath(ctx, jstType);
 		validateTypeName(ctx, jstType);
 		
 		//method should be unique
 		//property should be unique
 		final Map<String, IJstNode> dupProtectMap = new HashMap<String, IJstNode>();
 		for(IJstMethod m : jstType.getMethods(true)){
 			if(m instanceof JstProxyMethod
 					|| m instanceof ISynthesized){
 				continue;
 			}
 			if(!dupProtectMap.containsKey(m.getName().getName())){
 				dupProtectMap.put(m.getName().getName(), m);
 			}
 			else if(m.getName().getName().trim().length() > 0){
 				final BaseVjoSemanticRuleCtx ruleCtx = new BaseVjoSemanticRuleCtx(m.getName(), ctx.getGroupId(), new String[]{m.getName().getName()});
 				satisfyRule(ctx, VjoSemanticRuleRepo.getInstance().DUPLICATE_METHOD, ruleCtx);
 			}
 		}
 		
 		for(IJstProperty p : jstType.getProperties(true)){
 			if(p instanceof JstProxyProperty 
 					|| p instanceof ISynthesized){
 				continue;
 			}
 			if(!dupProtectMap.containsKey(p.getName().getName())){
 				dupProtectMap.put(p.getName().getName(), p);
 			}
 			else if(p.getName().getName().trim().length() > 0){
 				final BaseVjoSemanticRuleCtx ruleCtx = new BaseVjoSemanticRuleCtx(p.getName(), ctx.getGroupId(), new String[]{p.getName().getName()});
 				satisfyRule(ctx, VjoSemanticRuleRepo.getInstance().DUPLICATE_PROPERTY, ruleCtx);
 			}
 		}
 		
 //		dupProtectMap.clear();
 		for(IJstMethod m : jstType.getMethods(false)){
 			if(m instanceof JstProxyMethod
 					|| m instanceof ISynthesized){
 				continue;
 			}
 			
 			final String mtdName = m.getName().getName();
 			if(!dupProtectMap.containsKey(mtdName)){
 				dupProtectMap.put(mtdName, m);
 			}
 			else{
 				final IJstNode potentialMtd = dupProtectMap.get(mtdName);
 				JstTypeWithArgs typeWithArgs = null;
 				IJstType jstIType = jstType;
 				
 				if (jstIType instanceof JstTypeWithArgs) {
 					typeWithArgs = (JstTypeWithArgs)jstIType;
 				}
 				
 				if(potentialMtd != null 
 						&& potentialMtd instanceof IJstMethod
 						&& ((IJstMethod)potentialMtd).isStatic()){
 					
 					//bugfix 8714, report this error only if the signature of static and non-static methods are the same
 					if(isCompatibleMethod(typeWithArgs, typeWithArgs, m, (IJstMethod)potentialMtd)){
 						final BaseVjoSemanticRuleCtx ruleCtx = new BaseVjoSemanticRuleCtx(m.getName(), ctx.getGroupId(), new String[]{mtdName, jstType.getName()});
 						satisfyRule(ctx, VjoSemanticRuleRepo.getInstance().OVERLAP_STATIC_AND_NONE_STATIC_METHOD, ruleCtx);
 					}
 				}
 				else if(m.getName().getName().trim().length() > 0){
 					final BaseVjoSemanticRuleCtx ruleCtx = new BaseVjoSemanticRuleCtx(m.getName(), ctx.getGroupId(), new String[]{m.getName().getName()});
 					satisfyRule(ctx, VjoSemanticRuleRepo.getInstance().DUPLICATE_METHOD, ruleCtx);
 				}
 			}
 		}
 		
 		for(IJstProperty p : jstType.getProperties(false)){
 			if(p instanceof JstProxyProperty
 					|| p instanceof ISynthesized){
 				continue;
 			}
 			
 			final String ptyName = p.getName().getName();
 			if(!dupProtectMap.containsKey(ptyName)){
 				dupProtectMap.put(ptyName, p);
 			}
 			else{
 				final IJstNode potentialPty = dupProtectMap.get(ptyName);
 				if(potentialPty != null
 						&& potentialPty instanceof IJstProperty
 						&& ((IJstProperty)potentialPty).isStatic()){
 					final BaseVjoSemanticRuleCtx ruleCtx = new BaseVjoSemanticRuleCtx(p.getName(), ctx.getGroupId(), new String[]{ptyName, jstType.getName()});
 					satisfyRule(ctx, VjoSemanticRuleRepo.getInstance().OVERLAP_STATIC_AND_NONE_STATIC_PROPERTY, ruleCtx);
 				}
 				else if(p.getName().getName().replaceAll("\\\"", "").length() > 0){
 					final BaseVjoSemanticRuleCtx ruleCtx = new BaseVjoSemanticRuleCtx(p.getName(), ctx.getGroupId(), new String[]{p.getName().getName()});
 					satisfyRule(ctx, VjoSemanticRuleRepo.getInstance().DUPLICATE_PROPERTY, ruleCtx);
 				}
 			}
 		}
 		
 		//indexing instance methods, group by name
 		final Map<String, List<IJstMethod>> instanceMethodMap = new HashMap<String, List<IJstMethod>>();
 		final List<? extends IJstMethod> allInstanceMethods = JstTypeHelper.getDeclaredMethods(jstType.getMethods(false, true));
 		for(IJstMethod m : allInstanceMethods){
 			List<IJstMethod> mList = instanceMethodMap.get(m.getName().getName());
 			if(mList == null){
 				mList = new ArrayList<IJstMethod>();
 				instanceMethodMap.put(m.getName().getName(), mList);
 			}
 			mList.add(m);
 		}
 		//indexing static methods, group by name
 		final Map<String, List<IJstMethod>> staticMethodMap = new HashMap<String, List<IJstMethod>>();
 		final List<? extends IJstMethod> allStaticMethods = JstTypeHelper.getDeclaredMethods(jstType.getMethods(true));
 		for(IJstMethod m : allStaticMethods){
 			List<IJstMethod> mList = staticMethodMap.get(m.getName().getName());
 			if(mList == null){
 				mList = new ArrayList<IJstMethod>();
 				staticMethodMap.put(m.getName().getName(), mList);
 			}
 			mList.add(m);
 		}
 		
 		//validate global vars
 //		done by VjoJstPropertyValidator#validatePropertyType shouldn't duplicate
 //		validateGlobals(ctx, jstType);
 		
 		//mixin method should not conflict
 		//mixin property should not conflict
 		validateMixin(ctx, jstType, instanceMethodMap, staticMethodMap);
 
 		//class should not be both abstract and final	
 		//interface declared methods must be implemented
 		//super class declared abstract method must be implemented, constructor should be defined
 		if(jstType.isClass()){
 			validateCType(ctx, jstType, instanceMethodMap);
 		}
 		else if(jstType.isEnum()){
 			validateEType(ctx, jstType, instanceMethodMap);
 		}
 		else if(jstType.isInterface()){
 			validateIType(ctx, jstType);
 		}
 		else if(jstType.isOType()){
 			validateOType(ctx, jstType);
 		}
 		else if(jstType.isMixin()){
 			validateMType(ctx, jstType);
 		}
 		validateAllTypes(ctx, jstType);
 	}
 	
 	private void validateMixin(final VjoValidationCtx ctx,
 			final JstType jstType,
 			final Map<String, List<IJstMethod>> instanceMethodMap,
 			final Map<String, List<IJstMethod>> staticMethodMap) {
 		for(IJstTypeReference mixinTypeRef : jstType.getMixinsRef()){
 			IJstType mixinType = mixinTypeRef.getReferencedType();
 			if(mixinType == null){
 				continue;
 			}
 			else{
 				mixinType = ctx.getTypeSpaceType(mixinType);
 			}
             // Added by Eric.Ma on 20100609 for ctype can't mixin ctype self
             if (mixinType.equals(jstType)) {
                 final BaseVjoSemanticRuleCtx ruleCtx = new BaseVjoSemanticRuleCtx(jstType, ctx.getGroupId(),
                         new String[] { jstType.getName()});
                 satisfyRule(ctx,
                         VjoSemanticRuleRepo
                                 .getInstance().MIXINED_TYPE_MUST_NOT_BE_ITSELF, ruleCtx);
                 continue;
             }
             // End of added.
             // Added by Eric.Ma on 20100609 for only mtype can be mixined
             if (!mixinType.isMixin()) {
                 final BaseVjoSemanticRuleCtx ruleCtx = new BaseVjoSemanticRuleCtx(jstType, ctx.getGroupId(),
                         new String[] { jstType.getName(), mixinType.getName() });
                 satisfyRule(ctx,
                         VjoSemanticRuleRepo
                                 .getInstance().MTYPE_SHOULD_ONLY_BE_MIXINED, ruleCtx);
                 continue;
             }
             // End of added.
             MixinValidationUtil.validateMixin(ctx, this, jstType, mixinType, instanceMethodMap, staticMethodMap, false);
 		}
 	}
 
 	private void validateIType(final VjoValidationCtx ctx, final JstType jstType) {
 		//interface should not be defined final
 		if(jstType.getModifiers().isFinal()){
 			if(jstType.getOuterType() == null
 						|| !jstType.getOuterType().isInterface()){
 				//report problem
 				final BaseVjoSemanticRuleCtx ruleCtx = new BaseVjoSemanticRuleCtx(jstType, ctx.getGroupId(), new String[]{jstType.getName()});
 				satisfyRule(ctx, VjoSemanticRuleRepo.getInstance().INTERFACE_SHOULD_NOT_BE_FINAL, ruleCtx);
 			}
 		}
 		
 		//interface should not define static methods
 		for(IJstMethod mtd : jstType.getMethods()){
 			if (mtd instanceof ISynthesized || mtd instanceof JstProxyMethod) {
 				continue;
 			}
 			if(mtd.getModifiers().isPrivate() || mtd.getModifiers().isProtected()){
 				//report problem
 				final BaseVjoSemanticRuleCtx ruleCtx = new BaseVjoSemanticRuleCtx(mtd.getName(), ctx.getGroupId(), new String[]{jstType.getName(), mtd.getName().getName()});
 				satisfyRule(ctx, VjoSemanticRuleRepo.getInstance().ITYPE_ALLOWS_ONLY_PUBLIC_MODIFIER, ruleCtx);
 			}
 			if(mtd.isStatic()){
 				final BaseVjoSemanticRuleCtx ruleCtx = new BaseVjoSemanticRuleCtx(mtd.getName(), ctx.getGroupId(), new String[]{jstType.getName(), mtd.getName().getName()});
 				satisfyRule(ctx, VjoSemanticRuleRepo.getInstance().ITYPE_SHOULD_NOT_DEFINE_STATIC_METHODS, ruleCtx);
 			}
 		}
 		
 		for(IJstProperty pty : jstType.getProperties()){
 			//TODO - is this accurate?
 			if (pty instanceof ISynthesized || pty instanceof JstProxyProperty) {
 				continue;
 			}
 			if(pty.getModifiers().isPrivate() || pty.getModifiers().isProtected()){
 				//report problem
 				final BaseVjoSemanticRuleCtx ruleCtx = new BaseVjoSemanticRuleCtx(pty.getName(), ctx.getGroupId(), new String[]{jstType.getName(), pty.getName().getName()});
 				satisfyRule(ctx, VjoSemanticRuleRepo.getInstance().ITYPE_ALLOWS_ONLY_PUBLIC_MODIFIER, ruleCtx);
 			}
 			
 			//fix on 080409; vjo.NEEDS_IMPL isn't a property anymore
 			if(!pty.isStatic()){
 				//for interface function defined as vjo.NEEDS_IMPL
 				final BaseVjoSemanticRuleCtx ruleCtx = new BaseVjoSemanticRuleCtx(pty.getName(), ctx.getGroupId(), new String[]{jstType.getName(), pty.getName().getName()});
 				satisfyRule(ctx, VjoSemanticRuleRepo.getInstance().ITYPE_SHOULD_NOT_HAVE_INSTANCE_PROPERTY, ruleCtx);
 			}
 		}
 		
 		//interface should not define none public inner types, bug 6545
 		for(IJstType innerType : jstType.getEmbededTypes()){
 			if(innerType.getModifiers().isPrivate() || innerType.getModifiers().isProtected()){
 				//report problem
 				final BaseVjoSemanticRuleCtx ruleCtx = new BaseVjoSemanticRuleCtx(innerType, ctx.getGroupId(), new String[]{jstType.getName(), innerType.getName()});
 				satisfyRule(ctx, VjoSemanticRuleRepo.getInstance().ITYPE_ALLOWS_ONLY_PUBLIC_MODIFIER, ruleCtx);
 			}
 			if(!innerType.getModifiers().isStatic()){
 				//report problem according to Sathish's clarification that itype cannot have none-static inner types
 				final BaseVjoSemanticRuleCtx ruleCtx = new BaseVjoSemanticRuleCtx(innerType, ctx.getGroupId(), new String[]{jstType.getName(), innerType.getName()});
 				satisfyRule(ctx, VjoSemanticRuleRepo.getInstance().ITYPE_SHOULD_NOT_HAVE_INSTANCE_PROPERTY, ruleCtx);
 			}
 		}
 		
 		//interface cannot have a none interface parent
 		for(IJstType extType : jstType.getExtends()){
 			extType = ctx.getTypeSpaceType(extType);
 			if(extType.getName().equals(jstType.getName())){//report problem, bug 6557, itype cannot inherits from itself
 				//report problem
 				final BaseVjoSemanticRuleCtx ruleCtx = new BaseVjoSemanticRuleCtx(jstType, ctx.getGroupId(), new String[]{jstType.getName()});
 				satisfyRule(ctx, VjoSemanticRuleRepo.getInstance().SUPER_CLASS_SHOULD_NOT_BE_THE_SAME, ruleCtx);
 			}
 			else if(!ctx.getUnresolvedTypes().contains(extType.getName())
 						&& !extType.isInterface()){
 				final BaseVjoSemanticRuleCtx ruleCtx = new BaseVjoSemanticRuleCtx(jstType, ctx.getGroupId(), new String[]{jstType.getName(), extType.getName()});
 				satisfyRule(ctx, VjoSemanticRuleRepo.getInstance().ITYPE_SHOULD_NOT_EXTEND_NONE_ITYPE_CLASS, ruleCtx);
 			}
 		}
 		
 		if(jstType.getConstructor() != null){
 			//report problem
 			final BaseVjoSemanticRuleCtx ruleCtx = new BaseVjoSemanticRuleCtx(jstType, ctx.getGroupId(), new String[]{jstType.getName()});
 			satisfyRule(ctx, VjoSemanticRuleRepo.getInstance().ITYPE_OR_MTYPE_SHOULD_NOT_HAVE_CONSTRUCTOR, ruleCtx);
 		}
 	}
 	
 
 	private void validateMType(final VjoValidationCtx ctx,
 			final JstType jstType){
 		// HZ: mtype is only a template and should be exempt from type validation.
 		//validate that mtype implements all interfaces it satisfies
         /* for (IJstType interfaceType : jstType.getSatisfies()) {
               interfaceType = ctx.getTypeSpaceType(interfaceType);
               if (!ctx.getUnresolvedTypes().contains(interfaceType.getName())
                       && !interfaceType.isInterface()) {
                   final BaseVjoSemanticRuleCtx ruleCtx = new
                           BaseVjoSemanticRuleCtx(jstType, ctx.getGroupId(),
                                   new String[] { jstType.getName(), interfaceType.getName() });
                   satisfyRule(ctx,
                           VjoSemanticRuleRepo.getInstance().CTYPE_SHOULD_NOT_IMPLEMENT_NONE_ITYPE_INTERFACE,
                           ruleCtx);
               }
 
               validateOverride(ctx, jstType, interfaceType);
           }
 
          for (IJstType expectType : jstType.getExpects()) {
              expectType = ctx.getTypeSpaceType(expectType);
              if (expectType.isInterface()) {
                  final List<? extends IJstMethod> expectMtds = expectType.getMethods(false, true);
                  for (IJstMethod expectMtd : expectMtds) {
                      final String expectMtdName = expectMtd.getName().getName();
                      final IJstMethod instanceMTypeMtd = jstType.getMethod(expectMtdName, false);
                      if (instanceMTypeMtd != null) {
                          final BaseVjoSemanticRuleCtx ruleCtx = new BaseVjoSemanticRuleCtx(instanceMTypeMtd.getName(),
                                  ctx.getGroupId(), new String[] { instanceMTypeMtd.getName().getName(),
                                          jstType.getName(), expectType.getName() });
                          satisfyRule(ctx, VjoSemanticRuleRepo.getInstance().MTYPE_EXPECTS_SHOULD_NOT_BE_OVERWRITTEN,
                                  ruleCtx);
                      }
                  }
              }
          }*/
 
 		//Added by Eric.Ma for add expect only accept ctype and itype
 	    if(jstType.getExpects().size() >0){
 	        for (IJstTypeReference typeRef : jstType.getExpectsRef()) {
 	        	IJstType type = typeRef.getReferencedType();
                 if(!type.isClass() && !type.isInterface()){
                   //report problem
                     final BaseVjoSemanticRuleCtx ruleCtx = new BaseVjoSemanticRuleCtx(typeRef, ctx.getGroupId(), new String[]{type.getName()});
                     satisfyRule(ctx, VjoSemanticRuleRepo.getInstance().EXPECTS_MUST_BE_CTYPE_ITYPE, ruleCtx);
                 }
             }
 	    }
 	    //End of added.
 	    
 		if(jstType.getConstructor() != null){
 			//report problem
 			final BaseVjoSemanticRuleCtx ruleCtx = new BaseVjoSemanticRuleCtx(jstType, ctx.getGroupId(), new String[]{jstType.getName()});
 			satisfyRule(ctx, VjoSemanticRuleRepo.getInstance().ITYPE_OR_MTYPE_SHOULD_NOT_HAVE_CONSTRUCTOR, ruleCtx);
 		}
 		
 		//bug mtype cannot have inner type or be an inner type
 		final MTypeShouldNotHaveInnerTypesRuleCtx ruleCtx1 = new MTypeShouldNotHaveInnerTypesRuleCtx(jstType, ctx.getGroupId(), new String[]{jstType.getName()}, jstType);
 		satisfyRule(ctx, VjoSemanticRuleRepo.getInstance().MTYPE_SHOULD_NOT_HAVE_INNER_TYPES, ruleCtx1);
 		
 		if (jstType.getOuterType() != null) {
 			final MTypeShouldNotBeAsInnerTypeRuleCtx ruleCtx2 = new MTypeShouldNotBeAsInnerTypeRuleCtx(jstType, ctx.getGroupId(), new String[]{jstType.getName(), jstType.getOuterType().getName()}, jstType);
 			satisfyRule(ctx, VjoSemanticRuleRepo.getInstance().MTYPE_SHOULD_NOT_BE_AS_INNER_TYPE, ruleCtx2);
 		}
 	}
 	
 	private void validateOType(final VjoValidationCtx ctx,
 			final JstType jstType){
 		for(IJstProperty pty: jstType.getProperties()){
 			if(pty instanceof ISynthesized || pty instanceof JstProxyProperty){
 				continue;
 			}
 			
 			if(pty.getType() != null
 					&& !(pty.getType() instanceof JstObjectLiteralType)){
 				final BaseVjoSemanticRuleCtx ruleCtx = new BaseVjoSemanticRuleCtx(pty.getName(), ctx.getGroupId(), new String[]{jstType.getName(), pty.getName().getName()});
 				satisfyRule(ctx, VjoSemanticRuleRepo.getInstance().OTYPE_SHOULD_NOT_HAVE_NONE_OBJ_LITERAL_PROPERTY, ruleCtx);
 			}
 		}
 		
 		//bug 6217, otype cannot have inner type or be an inner type
 		if(jstType.getEmbededTypes().size() > 0){
 			//report problem
 			final BaseVjoSemanticRuleCtx ruleCtx = new BaseVjoSemanticRuleCtx(jstType, ctx.getGroupId(), new String[]{jstType.getName()});
 			satisfyRule(ctx, VjoSemanticRuleRepo.getInstance().OTYPE_SHOULD_NOT_HAVE_INNER_TYPES, ruleCtx);
 		}
 		
 		if(jstType.getOuterType() != null
 				&& !jstType.getOuterType().isMetaType()){
 			//report problem
 			final BaseVjoSemanticRuleCtx ruleCtx = new BaseVjoSemanticRuleCtx(jstType, ctx.getGroupId(), new String[]{jstType.getName(), jstType.getOuterType().getName()});
 			satisfyRule(ctx, VjoSemanticRuleRepo.getInstance().OTYPE_SHOULD_NOT_BE_AS_INNER_TYPE, ruleCtx);
 		}
 		
 		if(jstType.getContainingType() != null
 				&& !jstType.getContainingType().isMetaType()){
 			//report problem
 			final BaseVjoSemanticRuleCtx ruleCtx = new BaseVjoSemanticRuleCtx(jstType, ctx.getGroupId(), new String[]{jstType.getName(), jstType.getContainingType().getName()});
 			satisfyRule(ctx, VjoSemanticRuleRepo.getInstance().OTYPE_SHOULD_NOT_BE_AS_INNER_TYPE, ruleCtx);
 		}
 	}
 
 	private void validateAllTypes(final VjoValidationCtx ctx,
 			final JstType jstType){
 		
 		//validate inner types' name not conflicting
 		if(jstType.getEmbededType(jstType.getSimpleName()) != null){
 			//report problem
 			final BaseVjoSemanticRuleCtx ruleCtx = new BaseVjoSemanticRuleCtx(jstType, ctx.getGroupId(), new String[]{jstType.getSimpleName(), jstType.getName()});
 			satisfyRule(ctx, VjoSemanticRuleRepo.getInstance().INNER_TYPE_SHOULD_NOT_HAVE_NAME_SAME_AS_EMBEDDING_TYPE, ruleCtx);
 		}
 		
 		//if outer type isn't interface, and inner type is non-static (interface's inner type are defactor static
 		if(jstType.getOuterType() != null
 				&& !jstType.getModifiers().isStatic()
 				&& !jstType.getOuterType().isInterface()){
 			//bug 6603, instance inner type cannot have static members
 			for(IJstProperty prop: jstType.getProperties(true)){
 				if (!(prop instanceof ISynthesized) && !prop.getModifiers().isFinal()) {
 					//report problem
 					final BaseVjoSemanticRuleCtx ruleCtx = new BaseVjoSemanticRuleCtx(prop.getName(), ctx.getGroupId(), new String[]{jstType.getName(), prop.getName().getName()});
 					satisfyRule(ctx, VjoSemanticRuleRepo.getInstance().INSTANCE_INNER_TYPE_SHOULD_NOT_HAVE_STATIC_MEMBERS, ruleCtx);
 				}
 			}
 			
 			for(IJstMethod mtd: jstType.getMethods(true)){
 				if (mtd instanceof ISynthesized) continue;
 				//report problem
 				final BaseVjoSemanticRuleCtx ruleCtx = new BaseVjoSemanticRuleCtx(mtd.getName(), ctx.getGroupId(), new String[]{jstType.getName(), mtd.getName().getName()});
 				satisfyRule(ctx, VjoSemanticRuleRepo.getInstance().INSTANCE_INNER_TYPE_SHOULD_NOT_HAVE_STATIC_MEMBERS, ruleCtx);
 			}
 		}
 		
 		//validation for missing active needs
 		//final List<? extends IJstType> activeNeeds = jstType.getImports();
 		
 		final List<IJstType> inherits = new ArrayList<IJstType>(jstType.getExtends());
 		final List<IJstType> satisfies = new ArrayList<IJstType>(jstType.getSatisfies());
 		collectInheritsSatisfiesFromSubTypes(jstType, inherits, satisfies);
 		
 		// satisfies are already in the import list
 		final List<IJstType> activeNeeds = new ArrayList<IJstType>(jstType.getImports());
 		
 		//traverse the jstType to collect needed types that are TypeRefType or constructor
 		final Set<IJstRefType> mustActivelyNeeded = ctx.getMustActivelyNeededTypes(jstType);
 		final Set<IJstType> dereferencedNeededTypes = new HashSet<IJstType>(mustActivelyNeeded.size());
 		
 		final Set<IJstType> eligibleNeededTypes = new HashSet<IJstType>();
 		eligibleNeededTypes.addAll(inherits);
 		eligibleNeededTypes.addAll(satisfies);
 		for(IJstType active : activeNeeds){
 			eligibleNeededTypes.addAll(getInnerOuterTypes(active));
 		}
 		eligibleNeededTypes.addAll(getInnerOuterTypes(jstType));
 		
 		final List<String> missingActiveNeedsTypes = new ArrayList<String>();
 		
 		for (IJstRefType neededType: mustActivelyNeeded){
 			dereferencedNeededTypes.add(neededType.getReferencedNode());
 			if (neededType == null ||
 			    neededType.isMixin() ||
 				neededType.isImpliedImport() ||
 				neededType.getPackage() != null && TsLibLoader.isDefaultLibName(neededType.getPackage().getGroupName()) ||
 				neededType.getReferencedNode() == jstType || 
 				checkTypeInList(eligibleNeededTypes, neededType.getReferencedNode())) {
 				continue;
 			}
 			//report problem of missing active needs
 			missingActiveNeedsTypes.add(neededType.getName());
 		}		
 		
 		if (!missingActiveNeedsTypes.isEmpty()) {		
 			StringBuilder missingActiveTypeNames = new StringBuilder();
 			OUT:
 			for (String neededType : missingActiveNeedsTypes) {
 				for(IJstType missingImport : ctx.getMissingImportTypes()){
 					if(neededType.equals(missingImport.getName())){
 						continue OUT;
 					}
 				}
 				if(ctx.getUnresolvedTypes().contains(neededType)){
 					continue OUT;
 				}
 				
 				if (missingActiveTypeNames.length() > 0) {
 					missingActiveTypeNames.append(", ");
 				}
 				missingActiveTypeNames.append(neededType);
 			}
 			if(missingActiveTypeNames.length() > 0){
 				final BaseVjoSemanticRuleCtx ruleCtx = new BaseVjoSemanticRuleCtx(jstType, ctx.getGroupId(), new String[]{missingActiveTypeNames.toString()});
 				satisfyRule(ctx, VjoSemanticRuleRepo.getInstance().CANNOT_USE_INACTIVE_NEED_ACTIVELY, ruleCtx);
 			}
 		}
 		
 		ArrayList<String> unusedActiveNeedsTypes = new ArrayList<String>();
 		
 		for (IJstRefType knownActivelyNeeded : ctx.getKnownActivelyNeededTypes(jstType)){
 			dereferencedNeededTypes.add(knownActivelyNeeded.getReferencedNode());
 		}
 		// report unused active needs, excluding those in satisfies list
 		for (IJstType activeNeedsType: activeNeeds) {
 			
 			if (activeNeedsType == null) {
 				continue;
 			}
 			if (checkTypeInList(satisfies, activeNeedsType) || 
 				checkTypeInList(inherits, activeNeedsType) || dereferencedNeededTypes.contains(activeNeedsType)) {
 				continue;
 			}
 			else if (!activeNeedsType.isFakeType()) {
 				unusedActiveNeedsTypes.add(activeNeedsType.getName());
 			}
 		}
 		
 		if (!unusedActiveNeedsTypes.isEmpty()) {		
 			StringBuilder unusedActiveTypeNames = new StringBuilder();
 			for (String neededType : unusedActiveNeedsTypes) {
 				if (unusedActiveTypeNames.length() > 0) {
 					unusedActiveTypeNames.append(", ");
 				}
 				unusedActiveTypeNames.append(neededType);
 			}
 			final BaseVjoSemanticRuleCtx ruleCtx = new BaseVjoSemanticRuleCtx(jstType, ctx.getGroupId(), new String[]{unusedActiveTypeNames.toString()});
 			satisfyRule(ctx, VjoSemanticRuleRepo.getInstance().UNUSED_ACTIVE_NEEDS, ruleCtx);
 		}
 		
 		List<JstParamType> listParamTypes = jstType.getParamTypes();
 		
 		if (listParamTypes != null && !listParamTypes.isEmpty()) {
 			checkAndReportMissingParamTypeUpperBound(jstType, ctx, listParamTypes);
 		}
 	}
 
 	private List<IJstType> getInnerOuterTypes(final IJstType jstType) {
 		final List<IJstType> innerOuterTypes = new LinkedList<IJstType>();
 		for(IJstType enclosingType = jstType.getContainingType(); enclosingType != null; enclosingType = enclosingType.getContainingType()){
 			innerOuterTypes.add(enclosingType);
 		}
 		innerOuterTypes.addAll(jstType.getEmbededTypes());
 		innerOuterTypes.add(jstType);
 		return innerOuterTypes;
 	}
 	
 	private boolean checkTypeInList(Collection<IJstType> list, IJstType type) {
 		for (IJstType t : list) {
 			if (t instanceof JstProxyType) {
 				t = ((JstProxyType)t).getType();
 			}
 			
 			if (type instanceof JstProxyType) {
 				type = ((JstProxyType)type).getType();
 			}
 			
 			if (t == type) {
 				return true;
 			}
 		}
 		
 		return false;
 	}
 	
 	private void collectInheritsSatisfiesFromSubTypes(final JstType jstType, List<IJstType> inherites, List<IJstType> satisfies) {
 		
 		List<JstType> embeddedTypes = jstType.getEmbededTypes();
 		
 		for (JstType innerType : embeddedTypes) {
 			inherites.addAll(innerType.getExtends());
 			satisfies.addAll(innerType.getSatisfies());
 			collectInheritsSatisfiesFromSubTypes(innerType, inherites, satisfies);
 		}	
 	}
 	
 	private void validateCType(final VjoValidationCtx ctx,
 			final JstType jstType,
 			final Map<String, List<IJstMethod>> instanceMethodMap) {
 		final boolean isAbstract = jstType.getModifiers().isAbstract();
 		
 		for(IJstProperty prop: jstType.getProperties()){
 			if(prop.isPrivate() 
 					&& !(prop instanceof ISynthesized)
 					&& jstType.equals(prop.getOwnerType())){
 				if(!ctx.getPropertyStatesTable().hasReferences(prop)){
 					//report problem
 					final BaseVjoSemanticRuleCtx ruleCtx = new BaseVjoSemanticRuleCtx(prop.getName(), ctx.getGroupId(), new String[]{prop.getName().getName()});
 					satisfyRule(ctx, VjoSemanticRuleRepo.getInstance().PRIVATE_PROPERTY_REFERENCED_NOWHERE, ruleCtx);
 				}
 			}
 		}
 		
 		//check methods defined in this class
 		for(IJstMethod meth :  jstType.getMethods()){
 			if (meth instanceof ISynthesized) continue;
 			if(meth.isAbstract()){
 				if(!isAbstract){
 					//report problem, class itself should be abstract
 					final BaseVjoSemanticRuleCtx ruleCtx = new BaseVjoSemanticRuleCtx(jstType, ctx.getGroupId(), new String[]{jstType.getName(), meth.getName().getName()});
 					satisfyRule(ctx, VjoSemanticRuleRepo.getInstance().CLASS_SHOULD_BE_ABSTRACT, ruleCtx);
 				}
 			}
 			if(isPrivateMethod(meth) && jstType.equals(meth.getOwnerType())){
 				if(!ctx.getMethodInvocationTable().hasReferences(meth)){
 					//report problem
 					final BaseVjoSemanticRuleCtx ruleCtx = new BaseVjoSemanticRuleCtx(meth.getName(), ctx.getGroupId(), new String[]{meth.getName().getName()});
 					satisfyRule(ctx, VjoSemanticRuleRepo.getInstance().PRIVATE_METHOD_REFERENCED_NOWHERE, ruleCtx);
 				}
 			}
 		}
 		
 		checkFinalProperties(ctx, jstType);
 		
 		// type can be either abstract or final
 		if(isAbstract){
 			if(jstType.getModifiers().isFinal()){
 				//report problem
 				final BaseVjoSemanticRuleCtx ruleCtx = new BaseVjoSemanticRuleCtx(jstType, ctx.getGroupId(), new String[]{jstType.getName()});
 				satisfyRule(ctx, VjoSemanticRuleRepo.getInstance().CLASS_SHOULD_NOT_BE_BOTH_FINAL_AND_ABSTRACT, ruleCtx);
 			}
 		}
 		else{
 			for(IJstType iType : jstType.getSatisfies()){
 				
 				IJstType interfaceType = ctx.getTypeSpaceType(iType);
 				//bugfix by roy, need to remove duplicate error if the type isn't resolved
 				if(!ctx.getUnresolvedTypes().contains(interfaceType.getName())
 						&& !interfaceType.isInterface()){
 					final BaseVjoSemanticRuleCtx ruleCtx = new BaseVjoSemanticRuleCtx(jstType, ctx.getGroupId(), new String[]{jstType.getName(), interfaceType.getName()});
 					satisfyRule(ctx, VjoSemanticRuleRepo.getInstance().CTYPE_SHOULD_NOT_IMPLEMENT_NONE_ITYPE_INTERFACE, ruleCtx);
 				}
 				
 				if (iType instanceof JstTypeWithArgs) {
 					validateOverride(ctx, jstType, iType);
 				}
 				else {
 					validateOverride(ctx, jstType, interfaceType);
 				}
 			}
 			
 			for(IJstType extType : jstType.getExtends()){
 				
 				IJstType extendType = ctx.getTypeSpaceType(extType);
 				JstTypeWithArgs typeWithArgs = null;
 				
 				if (extType instanceof JstTypeWithArgs) {
 					typeWithArgs = (JstTypeWithArgs)extType;
 				}
 				
 				if(extendType.getName().equals(jstType.getName())){//report problem, bug 6557, itype cannot inherits from itself
 					//report problem
 					final BaseVjoSemanticRuleCtx ruleCtx = new BaseVjoSemanticRuleCtx(jstType, ctx.getGroupId(), new String[]{jstType.getName()});
 					satisfyRule(ctx, VjoSemanticRuleRepo.getInstance().SUPER_CLASS_SHOULD_NOT_BE_THE_SAME, ruleCtx);
 				}
 				else if(!extendType.isClass() && !extendType.isMixin()){
 					//report problem
 					//can't extend from a none class type
 					final BaseVjoSemanticRuleCtx ruleCtx = new BaseVjoSemanticRuleCtx(jstType, ctx.getGroupId(), new String[]{jstType.getName(), extendType.getName()});
 					satisfyRule(ctx, VjoSemanticRuleRepo.getInstance().CTYPE_SHOULD_NOT_EXTEND_NONE_CTYPE_CLASS, ruleCtx);
 				}
 				
 				if(extendType.getModifiers().isFinal()){
 					//report problem
 					//can't extend from a final class
 					final BaseVjoSemanticRuleCtx ruleCtx = new BaseVjoSemanticRuleCtx(jstType, ctx.getGroupId(), new String[]{extendType.getName()});
 					satisfyRule(ctx, VjoSemanticRuleRepo.getInstance().SUPER_CLASS_SHOULD_NOT_BE_FINAL, ruleCtx);
 				}
 				//bugfix 5891
 				for(IJstMethod staticExtMtd: extendType.getMethods(true, false)){
 					if(isPrivateMethod(staticExtMtd)){
 						continue;
 					}
 					
 					List<IJstMethod> candidateMtds = instanceMethodMap.get(staticExtMtd.getName().getName());
 					if(candidateMtds != null){
 						boolean overrided = false;
 						IJstMethod overrideMtdFound = null;
 						
 						for(IJstMethod overrideMtd : candidateMtds){
 							if(overrideMtd != null){
 								
 								if(!overrideMtd.getParentNode().equals(jstType)){
 									continue;
 								}
 								
 								if(overrideMtd.equals(staticExtMtd)){
 									overrided = false;
 								}
 								else if(overrideMtd.isAbstract()){
 									overrided = false;
 								}
 								else if(isCompatibleMethod(typeWithArgs, typeWithArgs, staticExtMtd, overrideMtd)){
 									overrided  = true;
 									overrideMtdFound = overrideMtd;
 								}
 							}
 						}
 						if(overrided && overrideMtdFound != null){
 							//incompatible methods structure
 							final BaseVjoSemanticRuleCtx ruleCtx = new BaseVjoSemanticRuleCtx(overrideMtdFound.getName(), ctx.getGroupId(), new String[]{staticExtMtd.getName().getName(), extendType.getName(), jstType.getName()});
 							satisfyRule(ctx, VjoSemanticRuleRepo.getInstance().STATIC_METHOD_SHOULD_NOT_BE_OVERRIDEN, ruleCtx);
 						}
 					}
 				}
 				
 				if (extType instanceof JstTypeWithArgs) {
 					validateOverride(ctx, jstType, extType);
 				}
 				else {
 					validateOverride(ctx, jstType, extendType);
 				}
 			}
 		}
 	}
 
 	private boolean isPrivateMethod(IJstMethod meth) {
 		if(meth.isDispatcher()){
 			for(IJstMethod overload: meth.getOverloaded()){
 				if(overload.isPrivate()){
 					continue;
 				}
 				else{
 					return false;
 				}
 			}
 			return true;
 		}
 		else{
 			return meth.isPrivate();
 		}
 	}
 
 	private void checkFinalProperties(final VjoValidationCtx ctx,
 			final JstType jstType) {
 		// check if final properties are initialized
 		final List<IJstProperty> jstProperties = jstType.getProperties();
 		for (IJstProperty jstProperty : jstProperties){
 			boolean hasValue = false;
 			if (!jstProperty.isFinal() 
 					|| jstProperty instanceof ISynthesized
 					|| (jstProperty.getName() != null && "vj$".equals(jstProperty.getName().getName()))){
 				continue;
 			}
 			final String groupId = ctx.getGroupId();
 			
 			//Make sure the final non-static property has been initialized in all 
 			//constructors
 			if (!jstProperty.getModifiers().isStatic()) {
 				List<IJstMethod> methods = 
 					ctx.getFinalPropertyInitConstructors(jstProperty);
 				if (methods != null) {
 					for (IJstMethod method : jstType.getMethods()) {
 						String name = method.getName().getName();
 						boolean isOverloadedConstructor = 
 							method.getModifiers().isPrivate() &&
 							name.startsWith(JstConstructor.CONSTRUCTS) &&
 							name.endsWith(JstMethod.OVLD);
 							
 						if (isOverloadedConstructor &&
 								!methods.contains(method)) {
 							reportError(ctx, jstProperty, groupId);
 							return;
 						}
 					}
 					hasValue = true;
 				} else if (ctx.getPropertyStatesTable().hasAssigned(jstProperty)) {
 					hasValue = true;
 				}
 			}
 			if (ctx.getPropertyStatesTable().hasAssigned(jstProperty)) {
 				hasValue = true;
 			}
 			if(jstProperty.isFinal() && !hasValue){
 				reportError(ctx, jstProperty, groupId);
 			}
 		}
 	}
 
 	private void reportError(final VjoValidationCtx ctx, IJstProperty jstProperty,
 			final String groupId) {
 		String[] propertyArgs = new String[1];
 		if (jstProperty.getName() != null && jstProperty.getName().getName() != null){
 			propertyArgs[0] = jstProperty.getName().getName();
 		}
 		else{
 			propertyArgs[0] = "NULL";
 		}
 		final BaseVjoSemanticRuleCtx finalPropertyShouldInitializedRuleCtx = new BaseVjoSemanticRuleCtx(jstProperty.getName(), groupId, propertyArgs);
 		satisfyRule(ctx, VjoSemanticRuleRepo.getInstance().FINAL_PROPERTY_SHOULD_BE_INITIALIZED, finalPropertyShouldInitializedRuleCtx);
 	}
 	
 	private void validateEType(final VjoValidationCtx ctx,
 			final JstType jstType,
 			final Map<String, List<IJstMethod>> instanceMethodMap) {
 		
 		if(jstType.getModifiers().isAbstract()){
 			final BaseVjoSemanticRuleCtx ruleCtx = new BaseVjoSemanticRuleCtx(jstType, ctx.getGroupId(), new String[]{jstType.getName()});
 			satisfyRule(ctx, VjoSemanticRuleRepo.getInstance().ETYPE_SHOULD_NOT_BE_ABSTRACT_OR_PRIVATE_OR_PROTECTED, ruleCtx);
 		}//enum type cannot be abstract
 		
 		for(IJstProperty prop: jstType.getProperties()){
 			if(prop.isPrivate() 
 					&& !(prop instanceof ISynthesized)
 					&& jstType.equals(prop.getOwnerType())){
 				if(!ctx.getPropertyStatesTable().hasReferences(prop)){
 					//report problem
 					final BaseVjoSemanticRuleCtx ruleCtx = new BaseVjoSemanticRuleCtx(prop.getName(), ctx.getGroupId(), new String[]{prop.getName().getName()});
 					satisfyRule(ctx, VjoSemanticRuleRepo.getInstance().PRIVATE_PROPERTY_REFERENCED_NOWHERE, ruleCtx);
 				}
 			}
 		}
 		
 		//check methods defined in this class
 		for(IJstMethod meth : jstType.getMethods()){
 			if(meth.isAbstract()){
 				//report problem, enum method cannot be abstract
 				final BaseVjoSemanticRuleCtx ruleCtx = new BaseVjoSemanticRuleCtx(jstType, ctx.getGroupId(), new String[]{jstType.getName(), meth.getName().getName()});
 				satisfyRule(ctx, VjoSemanticRuleRepo.getInstance().ETYPE_SHOULD_NOT_DEFINE_ABSTRACT_METHOD_OR_PROPERTY, ruleCtx);
 			}
 			if(isPrivateMethod(meth) && jstType.equals(meth.getOwnerType())){
 				if(!ctx.getMethodInvocationTable().hasReferences(meth)){
 					//report problem
 					final BaseVjoSemanticRuleCtx ruleCtx = new BaseVjoSemanticRuleCtx(meth.getName(), ctx.getGroupId(), new String[]{meth.getName().getName()});
 					satisfyRule(ctx, VjoSemanticRuleRepo.getInstance().PRIVATE_METHOD_REFERENCED_NOWHERE, ruleCtx);
 				}
 			}
 		}
 		
 		// check for duplicate enum values
 		Set<String> dupEnumValueProtect = new HashSet<String>();
 		for(IJstProperty enumProp : jstType.getEnumValues()){
 			final String enumPropName = enumProp.getName().getName();
 			if(jstType.getEnumValue(enumPropName) != null){
 				if(dupEnumValueProtect.contains(enumPropName)){
 					//report problem, duplicate enum value detected;
 					final BaseVjoSemanticRuleCtx ruleCtx = new BaseVjoSemanticRuleCtx(enumProp.getName(), ctx.getGroupId(), new String[]{jstType.getName(), enumPropName});
 					satisfyRule(ctx, VjoSemanticRuleRepo.getInstance().ETYPE_SHOULD_NOT_HAVE_DUP_ENUM_VALUES, ruleCtx);
 				}
 				else{
 					dupEnumValueProtect.add(enumPropName);
 				}
 			}
 		}
 		
 		checkFinalProperties(ctx, jstType);
 		
 		if(jstType.getConstructor() != null){
 			final JstModifiers constructorMod = jstType.getConstructor().getModifiers();
 			if(constructorMod.isPublic() || constructorMod.isProtected()){
 				final BaseVjoSemanticRuleCtx ruleCtx = new BaseVjoSemanticRuleCtx(jstType, ctx.getGroupId(), new String[]{jstType.getName()});
 				satisfyRule(ctx, VjoSemanticRuleRepo.getInstance().ETYPE_SHOULD_NOT_DEFINE_PUBLIC_OR_PROTECTED_CONSTRUCTOR, ruleCtx);
 			}
 		}
 		
 		for(IJstType interfaceType : jstType.getSatisfies()){
 			interfaceType = ctx.getTypeSpaceType(interfaceType);
 			if(!ctx.getUnresolvedTypes().contains(interfaceType.getName())
 					&& !interfaceType.isInterface()){
 				final BaseVjoSemanticRuleCtx ruleCtx = new BaseVjoSemanticRuleCtx(jstType, ctx.getGroupId(), new String[]{jstType.getName(), interfaceType.getName()});
 				satisfyRule(ctx, VjoSemanticRuleRepo.getInstance().CTYPE_SHOULD_NOT_IMPLEMENT_NONE_ITYPE_INTERFACE, ruleCtx);
 			}
 			
 			validateOverride(ctx, jstType, interfaceType);
 		}
 	}
 
 	private void validateOverride(final VjoValidationCtx ctx,
 			final JstType jstType, IJstType extendType) {
 		
 		JstTypeWithArgs typeWithArgs = null;
 		
 		if (extendType instanceof JstTypeWithArgs) {
 			typeWithArgs = (JstTypeWithArgs)extendType;
 			extendType = typeWithArgs.getType();
 		}
 		
 		//bugfix 5296 for missing interface implementing methods
 //		List<IVjoSymbol> extTypeSymbols = ctx.getSymbolTable().getSymbolsInScope(extendType, EVjoSymbolType.INSTANCE_VARIABLE);
 		List<? extends IJstMethod> extTypeMtds = extendType.getMethods(false, true);
 		if(extTypeMtds != null){
 			for(IJstMethod extTypeMtd : extTypeMtds){
 				if(extTypeMtd == null){
 					continue;
 				}
 				
 				final IJstMethod toExtMethod = extTypeMtd;
 				//skip super private methods
 				if(isPrivateMethod(toExtMethod)){
 					continue;
 				}
 				
 				IJstMethod candidateMtd = jstType.getMethod(toExtMethod.getName().getName(), false, true);
 				IJstType candidateType = jstType;
 				
 				if (candidateMtd != null) {				
 					while (candidateType != null && candidateType.getMethod(candidateMtd.getName().getName(), false, false) != candidateMtd) {
 						candidateType = candidateType.getExtend();
 					}		
 				}
 				else {
 					candidateType = null;
 				}
 				
 				boolean overriden = false;
 				IJstMethod implMethodFound = null;
 				
 				if(candidateMtd != null){
 					final IJstMethod implMethod = candidateMtd;
 					if(implMethod.equals(toExtMethod)){
 						overriden = false;
 					}
 					else if(implMethod.isAbstract()){
 						overriden = false;
 					}
 					else{// if(isCompatibleMethod(toExtMethod, implMethod)){
 						overriden  = true;
 						implMethodFound = implMethod;
 					}
 				}
 				if(overriden && implMethodFound != null){
 					JstTypeWithArgs implType = null;
 					
 					if (candidateType != null && candidateType instanceof JstTypeWithArgs) {
 						implType = (JstTypeWithArgs)candidateType;
 					}
 					if(toExtMethod.isFinal()){
 						final BaseVjoSemanticRuleCtx ruleCtx = new BaseVjoSemanticRuleCtx(implMethodFound.getName(), ctx.getGroupId(), new String[]{toExtMethod.getName().getName()});
 						satisfyRule(ctx, VjoSemanticRuleRepo.getInstance().FINAL_METHOD_SHOULD_NOT_BE_OVERRIDEN, ruleCtx);
 					}
 					if(toExtMethod.isPublic()
 							&& (!implMethodFound.isPublic())
 						|| toExtMethod.isProtected() && isPrivateMethod(implMethodFound)){
 						//reducing the visibility, report problem
 						final BaseVjoSemanticRuleCtx ruleCtx = new BaseVjoSemanticRuleCtx(implMethodFound.getName(), ctx.getGroupId(), new String[]{toExtMethod.getName().getName()});
 						satisfyRule(ctx, VjoSemanticRuleRepo.getInstance().OVERRIDE_METHOD_SHOULD_NOT_REDUCE_VISIBILITY, ruleCtx);
 					}
 					else if(!isCompatibleMethod(typeWithArgs, implType, toExtMethod, implMethodFound)){
 						if(toExtMethod.isAbstract() || extendType.isInterface()){
 							final BaseVjoSemanticRuleCtx ruleCtx = new BaseVjoSemanticRuleCtx(jstType, ctx.getGroupId(), new String[]{toExtMethod.getName().getName()});
 							satisfyRule(ctx, VjoSemanticRuleRepo.getInstance().ABSTRACT_METHOD_MUST_BE_IMPLEMENTED, ruleCtx);
 						}
 						else if(!(candidateMtd instanceof ISynthesized)){
 							//incompatible methods structure
 							final BaseVjoSemanticRuleCtx ruleCtx = new BaseVjoSemanticRuleCtx(implMethodFound.getName(), ctx.getGroupId(), new String[]{toExtMethod.getName().getName()});
 							satisfyRule(ctx, VjoSemanticRuleRepo.getInstance().OVERRIDE_METHOD_SHOULD_HAVE_COMPATIBLE_SIGNATURE, ruleCtx);
 						}
 					}
 				}
 				else if(!overriden && (toExtMethod.isAbstract() || toExtMethod.getOwnerType().isInterface() || extendType.isInterface())){
 					final BaseVjoSemanticRuleCtx ruleCtx = new BaseVjoSemanticRuleCtx(jstType, ctx.getGroupId(), new String[]{toExtMethod.getName().getName()});
 					satisfyRule(ctx, VjoSemanticRuleRepo.getInstance().ABSTRACT_METHOD_MUST_BE_IMPLEMENTED, ruleCtx);
 				}
 			}
 		}
 		
 //		if(extendType.isClass()){
 //			for(IJstType extTypeInterface: extendType.getSatisfies()){
 //				validateOverride(ctx, jstType, extTypeInterface);
 //			}
 //		}
 	}
 	
 	private void validateTypeName(final VjoValidationCtx ctx,
 			final JstType jstType) {
 		if (jstType.isFakeType()) {
 			return;
 		}
 		if(jstType.getName() != null){
 			final String typeSimpleName = jstType.getSimpleName();
 			final TypeNameShouldNotBeEmptyRuleCtx notEmptyTypeNameruleCtx = new TypeNameShouldNotBeEmptyRuleCtx(jstType, ctx.getGroupId(), new String[]{typeSimpleName}, typeSimpleName);
 			satisfyRule(ctx, VjoSemanticRuleRepo.getInstance().TYPE_NAME_SHOULD_NOT_BE_EMPTY, notEmptyTypeNameruleCtx);
 			
 			final Matcher m = s_typePattern.matcher(jstType.getName());
 			if(!m.matches()){
 				final BaseVjoSemanticRuleCtx ruleCtx = new BaseVjoSemanticRuleCtx(jstType, ctx.getGroupId(), new String[]{jstType.getName()});
 				satisfyRule(ctx, VjoSemanticRuleRepo.getInstance().CLASS_NAME_CANNOT_HAVE_ILLEGAL_TOKEN, ruleCtx);
 			}
 			
 			if(!jstType.isMetaType() && jstType.getContainingType() == null && jstType.getOuterType() == null){
 				//validate that type name isn't start with lower case letter
 				//validator logic refactored into rule
 				final ClassBetterStartWithCapitalLetterRuleCtx ruleCtx = new ClassBetterStartWithCapitalLetterRuleCtx(jstType, ctx.getGroupId(), new String[]{typeSimpleName}, typeSimpleName);
 				satisfyRule(ctx, VjoSemanticRuleRepo.getInstance().CLASS_BETTER_START_WITH_NONE_CAPITAL_LETTER, ruleCtx);
 			}
 			
 			//further check type name for possible keywords
 			final String[] splitted = jstType.getName().split("\\.");
 			for(String section: splitted){
 				if(isVjoKeyword(section, VjoConstants.JAVA_FULL_KEYWORDS)){
 					final BaseVjoSemanticRuleCtx ruleCtx = new BaseVjoSemanticRuleCtx(jstType, ctx.getGroupId(), new String[]{jstType.getName()});
 					satisfyRule(ctx, VjoSemanticRuleRepo.getInstance().CLASS_NAME_CANNOT_HAVE_ILLEGAL_TOKEN, ruleCtx);
 					break;
 				}
 			}
 		}
 	}
 
 	private void validateTypePath(final VjoValidationCtx ctx,
 			final JstType jstType) {
 		if (jstType.isFakeType()) {
 			return;
 		}
 		if(jstType.getModifiers().isPublic()
 				&& jstType.getContainingType() == null){
 			final JstSource jstTypeSource = jstType.getSource();
 			if(jstTypeSource != null){
 				final IBinding jstTypeBnd = jstTypeSource.getBinding();
 				if(jstTypeBnd != null && jstTypeBnd instanceof FileBinding){
 					String jstTypePath = jstTypeBnd.getName();
 					if(jstTypePath != null){
 						if(jstTypePath.indexOf(File.separatorChar) >= 0){
 							jstTypePath = jstTypePath.replace(File.separatorChar, '.');
 						}
 						if (jstType.getPackage() != null) {
 							boolean isPackageValid = true;
 							String pkgPath = getPkgPath(jstTypePath, jstType.getPackage().getGroupName(), getFileName(jstTypePath));
 							String pkgName = jstType.getPackage().getName();
 							if(pkgName.length() > 0 && !pkgPath.endsWith(pkgName)){
 								isPackageValid = false;
 							} if (pkgName.length() == 0 && pkgPath.length() != 0) {
 								isPackageValid = false;
 							} else {
 								String typeName = jstType.getSimpleName();
 								String fileName = getFileName(jstTypePath);
 								if(typeName==null){
 									isPackageValid = false;
 								}else if (!typeName.equals(fileName)) {
 									isPackageValid = false;
 								}
 							}
 							if (!isPackageValid) {
 								//report problem
 								final BaseVjoSemanticRuleCtx ruleCtx = 
 									new BaseVjoSemanticRuleCtx(jstType, ctx.getGroupId(), new String[]{jstType.getName(), jstTypeBnd.getName()});
 								satisfyRule(ctx, VjoSemanticRuleRepo.getInstance().PUBLIC_CLASS_SHOULD_RESIDE_IN_CORRESPONDING_FILE, ruleCtx);
 							}
 						} 
 					}
 				}
 			}
 		}
 		
 		//none embedded type should be public and none-static, bug 6862
 		if(jstType.getContainingType() == null
 				&& jstType.getOuterType() == null){
 			//jst type should not be none-public
 			if(jstType.getModifiers().isProtected ()
 					|| jstType.getModifiers().isPrivate()){
 				//report problem of illegal modifier detection
 				final BaseVjoSemanticRuleCtx ruleCtx = new BaseVjoSemanticRuleCtx(jstType, ctx.getGroupId(), new String[]{jstType.getName()});
 				satisfyRule(ctx, VjoSemanticRuleRepo.getInstance().MAIN_CLASS_SHOULD_BE_PUBLIC, ruleCtx);
 			}
 		}
 	}
 
 	private String getFileName(String jstTypePath) {
 		String fileName = jstTypePath.substring(0, jstTypePath.lastIndexOf('.'));
 		fileName = fileName.substring(fileName.lastIndexOf('.')+1);
 		return fileName;
 	}
 
     private String getPkgPath(String jstTypePath, String groupName,
             String fileName) {
         String pkgPath = null;
         int groupIndex = jstTypePath.indexOf(groupName);
         int fileNameIndex;
         if (groupIndex > 0) {
             String temp = jstTypePath.substring(groupIndex + groupName.length()
                     + 1);
             temp = temp.substring(temp.indexOf('.') + 1);
             fileNameIndex = temp.lastIndexOf(fileName);
            if (fileNameIndex > 0 &&  fileNameIndex  -1 < temp.length()) {
                 pkgPath = temp.substring(0, fileNameIndex - 1);
             } else {
                 pkgPath="";
             }
         } else {
             pkgPath = jstTypePath.substring(0, jstTypePath
                     .lastIndexOf(fileName) - 1);
         }
         return pkgPath;
     }
 
 	private void validateBeforeAll(final VjoValidationCtx ctx,
 			final JstType jstType) {
 		final List<IScriptProblem> syntaxProblems = ctx.getSyntaxProblems(jstType);
 		if(syntaxProblems != null && syntaxProblems.size() > 0){
 			for(IScriptProblem syntaxProblem : syntaxProblems){
 				final VjoSemanticProblem semanticProblem = new VjoSemanticProblem(
 						syntaxProblem.getArguments(), 
 						//bugfix 5306
 						syntaxProblem.getID() != null ? syntaxProblem.getID() : VjoSyntaxProbIds.IncorrectVjoSyntax, 
 						syntaxProblem.getMessage(), 
 						syntaxProblem.getOriginatingFileName(), 
 						syntaxProblem.getSourceStart(), 
 						syntaxProblem.getSourceEnd(), 
 						syntaxProblem.getSourceLineNumber(), 
 						syntaxProblem.getColumn(), 
 						syntaxProblem.type()); 
 				ctx.addProblem(jstType, semanticProblem);
 			}
 			//bugfix, can't stop validation here, more semantic problems to uncover
 			throw new VjoValidationRuntimeException("syntax problems found");
 		}
 	}
 	
 	/**
 	 * <p>
 	 * checks if contract method is compatible with impl method
 	 * including their overloaded signatures
 	 * </p>
 	 * @param contract
 	 * @param impl
 	 * @return boolean
 	 */
 	public boolean isCompatibleMethod(JstTypeWithArgs typeWithArgsContract, JstTypeWithArgs typeWithArgsImpl,IJstMethod contract, IJstMethod impl){
 		final List<IJstMethod> contractOverloaded = getApi(contract);
 		
 		OUT:
 		for(IJstMethod contractMtd : contractOverloaded){
 			final List<IJstMethod> implOverloaded = getApi(impl);
 			for(IJstMethod implMtd : implOverloaded){
 				if(isCompatibleMethodNoOverload(typeWithArgsContract, typeWithArgsImpl, contractMtd, implMtd)){
 					continue OUT;
 				}
 			}
 			return false;
 		}
 		
 		return true;
 	}
 
 	private List<IJstMethod> getApi(IJstMethod contract) {
 		final List<IJstMethod> contractOverloaded = new LinkedList<IJstMethod>();
 		if(!contract.isDispatcher()){
 			contractOverloaded.add(contract);
 		}
 		else{
 			contractOverloaded.addAll(contract.getOverloaded());
 		}
 		return contractOverloaded;
 	}
 	
 	/**
 	 * TODO unify with {@link TypeCheckUtil#isAssignableNoOverload}
 	 * @param typeWithArgsContract
 	 * @param typeWithArgsImpl
 	 * @param contract
 	 * @param impl
 	 * @return
 	 */
 	private boolean isCompatibleMethodNoOverload(JstTypeWithArgs typeWithArgsContract, JstTypeWithArgs typeWithArgsImpl, IJstMethod contract, IJstMethod impl){
 		final List<JstArg> args = contract.getArgs();
 		final List<JstArg> mArgs = new ArrayList<JstArg>(impl.getArgs());
 		//padding for rhs as it's last variable is variable lengthed
 		if(mArgs.size() > args.size()){
 			if(mArgs.size() > 0){ 
 				JstArg lastRhsArg = mArgs.get(mArgs.size() - 1);
 				if(lastRhsArg != null
 						&& lastRhsArg.isVariable()){
 					for(int i = mArgs.size(); i < args.size(); i++){
 						mArgs.add(lastRhsArg);
 					}
 				}
 			}
 		}
 		if(args.size() != mArgs.size()){
 			//param compatibilities issue
 			return false;
 		}
 		
 		for(int i = 0, len = args.size(); i < len; i++){
 			final JstArg arg = args.get(i);
 			final JstArg mArg = mArgs.get(i);
 			
 			IJstType argType = arg.getType();
 			
 			if (typeWithArgsContract != null && argType instanceof JstParamType) {
 				argType = typeWithArgsContract.getParamArgType((JstParamType)argType);
 			}
 			
 			IJstType mArgType = mArg.getType();
 			
 			if (typeWithArgsImpl != null && mArgType instanceof JstParamType) {
 				mArgType = typeWithArgsImpl.getParamArgType((JstParamType)mArgType);
 			}
 			
 			if (argType instanceof JstTypeWithArgs && mArgType instanceof JstTypeWithArgs) {
 				if (!TypeCheckUtil.isAssignable(typeWithArgsContract, typeWithArgsImpl, (JstTypeWithArgs)argType, (JstTypeWithArgs)mArgType)) {
 					return false;
 				}
 			}			
 			//if arg & mArg doesn't match
 			else if(!TypeCheckUtil.isAssignable(argType, mArgType)){
 				return false;
 			}
 		}
 		
 		IJstType rtnType = contract.getRtnType();
 		IJstType mRtnType = impl.getRtnType();
 		
 		if (typeWithArgsContract != null && rtnType instanceof JstParamType) {
 			rtnType = typeWithArgsContract.getParamArgType((JstParamType)rtnType);
 		}
 		
 		if (typeWithArgsImpl != null && mRtnType instanceof JstParamType) {
 			mRtnType = typeWithArgsImpl.getParamArgType((JstParamType)mRtnType);
 		}
 		
 		if (rtnType instanceof JstTypeWithArgs && mRtnType instanceof JstTypeWithArgs) {
 			if (!TypeCheckUtil.isAssignable(typeWithArgsContract, typeWithArgsImpl, (JstTypeWithArgs)rtnType, (JstTypeWithArgs)mRtnType)) {
 				return false;
 			}
 		}		
 		else if(!TypeCheckUtil.isAssignable(rtnType, mRtnType)){
 			//return type compatibilities issue
 			return false;
 		}
 		
 		return true;
 	}
 }
