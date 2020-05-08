 /*******************************************************************************
  * Copyright (c) 2005, 2012 eBay Inc.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  *******************************************************************************/
 package org.eclipse.vjet.vjo.tool.codecompletion.handler;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Set;
 import java.util.TreeSet;
 
 import org.eclipse.vjet.dsf.jsgen.shared.ids.ScopeIds;
 import org.eclipse.vjet.dsf.jst.BaseJstNode;
 import org.eclipse.vjet.dsf.jst.IJstGlobalFunc;
 import org.eclipse.vjet.dsf.jst.IJstGlobalProp;
 import org.eclipse.vjet.dsf.jst.IJstMethod;
 import org.eclipse.vjet.dsf.jst.IJstNode;
 import org.eclipse.vjet.dsf.jst.IJstType;
 import org.eclipse.vjet.dsf.jst.declaration.JstArg;
 import org.eclipse.vjet.dsf.jst.declaration.JstExtendedType;
 import org.eclipse.vjet.dsf.jst.declaration.JstFuncType;
 import org.eclipse.vjet.dsf.jst.declaration.JstFunctionRefType;
 import org.eclipse.vjet.dsf.jst.declaration.JstMethod;
 import org.eclipse.vjet.dsf.jst.declaration.JstTypeRefType;
 import org.eclipse.vjet.dsf.jst.expr.FieldAccessExpr;
 import org.eclipse.vjet.dsf.jst.expr.JstArrayInitializer;
 import org.eclipse.vjet.dsf.jst.expr.MtdInvocationExpr;
 import org.eclipse.vjet.dsf.jst.expr.ObjCreationExpr;
 import org.eclipse.vjet.dsf.jst.term.JstIdentifier;
 import org.eclipse.vjet.dsf.jst.term.ObjLiteral;
 import org.eclipse.vjet.dsf.jst.term.SimpleLiteral;
 import org.eclipse.vjet.dsf.jst.token.IExpr;
 import org.eclipse.vjet.dsf.jstojava.resolver.FunctionParamsMetaRegistry;
 import org.eclipse.vjet.dsf.jstojava.translator.robust.completion.IJstCompletion;
 import org.eclipse.vjet.dsf.jstojava.translator.robust.completion.JstComletionOnMessageSend;
 import org.eclipse.vjet.dsf.jstojava.translator.robust.completion.JstCompletion;
 import org.eclipse.vjet.dsf.jstojava.translator.robust.completion.JstCompletionOnMemberAccess;
 import org.eclipse.vjet.dsf.jstojava.translator.robust.completion.JstCompletionOnQualifiedNameReference;
 import org.eclipse.vjet.dsf.jstojava.translator.robust.completion.JstCompletionOnSingleNameReference;
 import org.eclipse.vjet.dsf.jstojava.translator.robust.completion.JstFieldOrMethodCompletion;
 import org.eclipse.vjet.dsf.jstojava.translator.robust.completion.JstInheritsOnTypeCompletion;
 import org.eclipse.vjet.dsf.jstojava.translator.robust.completion.JstKeywordCompletion;
 import org.eclipse.vjet.dsf.jstojava.translator.robust.completion.JstNeedsOnTypeCompletion;
 import org.eclipse.vjet.dsf.jstojava.translator.robust.completion.JstSatisfiesOnTypeCompletion;
 import org.eclipse.vjet.dsf.jstojava.translator.robust.completion.JstTypeCompletion;
 import org.eclipse.vjet.vjo.tool.codecompletion.CodeCompletionUtils;
 import org.eclipse.vjet.vjo.tool.codecompletion.IVjoCcHandler;
 import org.eclipse.vjet.vjo.tool.codecompletion.StringUtils;
 import org.eclipse.vjet.vjo.tool.codecompletion.VjoCcCtx;
 import org.eclipse.vjet.vjo.tool.codecompletion.advisor.VjoCCVjoUtilityAdvisor;
 import org.eclipse.vjet.vjo.tool.codecompletion.advisor.VjoCcAliasProposalAdvisor;
 import org.eclipse.vjet.vjo.tool.codecompletion.advisor.VjoCcCTypeProposalAdvisor;
 import org.eclipse.vjet.vjo.tool.codecompletion.advisor.VjoCcConstructorGenProposalAdvisor;
 import org.eclipse.vjet.vjo.tool.codecompletion.advisor.VjoCcDerivedPropMethodAdvisor;
 import org.eclipse.vjet.vjo.tool.codecompletion.advisor.VjoCcEnumElementAdvisor;
 import org.eclipse.vjet.vjo.tool.codecompletion.advisor.VjoCcFunctionArgumentAdvisor;
 import org.eclipse.vjet.vjo.tool.codecompletion.advisor.VjoCcFunctionGenProposalAdvisor;
 import org.eclipse.vjet.vjo.tool.codecompletion.advisor.VjoCcGlobalAdvisor;
 import org.eclipse.vjet.vjo.tool.codecompletion.advisor.VjoCcGlobalExtensionAdvisor;
 import org.eclipse.vjet.vjo.tool.codecompletion.advisor.VjoCcInterfaceProposalAdvisor;
 import org.eclipse.vjet.vjo.tool.codecompletion.advisor.VjoCcKeywordAdvisor;
 import org.eclipse.vjet.vjo.tool.codecompletion.advisor.VjoCcKeywordInMethodProposalAdvisor;
 import org.eclipse.vjet.vjo.tool.codecompletion.advisor.VjoCcMTypeProposalAdvisor;
 import org.eclipse.vjet.vjo.tool.codecompletion.advisor.VjoCcNeedsItemProposalAdvisor;
 import org.eclipse.vjet.vjo.tool.codecompletion.advisor.VjoCcObjLiteralAdvisor;
 import org.eclipse.vjet.vjo.tool.codecompletion.advisor.VjoCcOuterPropMethodProposalAdvisor;
 import org.eclipse.vjet.vjo.tool.codecompletion.advisor.VjoCcOverrideProposalAdvisor;
 import org.eclipse.vjet.vjo.tool.codecompletion.advisor.VjoCcOwnerTypeProposalAdvisor;
 import org.eclipse.vjet.vjo.tool.codecompletion.advisor.VjoCcPackageProposalAdvisor;
 import org.eclipse.vjet.vjo.tool.codecompletion.advisor.VjoCcParameterHintAdvisor;
 import org.eclipse.vjet.vjo.tool.codecompletion.advisor.VjoCcParameterProposalAdvisor;
 import org.eclipse.vjet.vjo.tool.codecompletion.advisor.VjoCcPropMethodProposalAdvisor;
 import org.eclipse.vjet.vjo.tool.codecompletion.advisor.VjoCcStaticPropMethodProposalAdvisor;
 import org.eclipse.vjet.vjo.tool.codecompletion.advisor.VjoCcThisProposalAdvisor;
 import org.eclipse.vjet.vjo.tool.codecompletion.advisor.VjoCcTypeNameAdvisor;
 import org.eclipse.vjet.vjo.tool.codecompletion.advisor.VjoCcTypeNameAliasProposalAdvisor;
 import org.eclipse.vjet.vjo.tool.codecompletion.advisor.VjoCcTypeProposalAdvisor;
 import org.eclipse.vjet.vjo.tool.codecompletion.advisor.VjoCcVariableProposalAdvisor;
 import org.eclipse.vjet.vjo.tool.codecompletion.advisor.keyword.CompletionConstants;
 
 
 /**
  * 
  */
 public class VjoCcHandler implements IVjoCcHandler {
 
 	public String[] handle(VjoCcCtx ctx) {
 
 		final List<String> result = new ArrayList<String>();
 
 		if (ctx.isInSciptUnitArea()) {
 			return analyseFromScriptUnit(ctx);
 		}
 		JstCompletion completion = ctx.getCompletion();
 		if (completion == null) {
 			return new String[0];
 		}
 		if (completion instanceof JstInheritsOnTypeCompletion) {
 			return new String[] { VjoCcCTypeProposalAdvisor.ID };
 		} else if (completion instanceof JstNeedsOnTypeCompletion) {
 			return new String[] { VjoCcTypeProposalAdvisor.ID,
 					VjoCcPackageProposalAdvisor.ID };
 		} else if (completion instanceof JstSatisfiesOnTypeCompletion) {
 			return new String[] { VjoCcInterfaceProposalAdvisor.ID };
 		} else if (completion instanceof JstFieldOrMethodCompletion) {
 			IJstType type = ctx.getActingType();
 			List<String> list = new ArrayList<String>();
 			if(completion.inScope(ScopeIds.METHOD) || completion.inScope(ScopeIds.INITS)
 					|| completion.inScope(ScopeIds.METHOD_CALL) ) {
 				list.add(VjoCcObjLiteralAdvisor.ID);
 			}
 			else{
 				if(completion.getRealParent() instanceof ObjLiteral && completion.inScope(ScopeIds.PROPS)){
 					list.add(VjoCcObjLiteralAdvisor.ID);
 				}
 				// if bol is true, no need to add constructor proposal
 				boolean bol = (type == null || type.isInterface() || type.isMixin());
 				list.add(VjoCcFunctionGenProposalAdvisor.ID);
 				if (!ctx.isInStatic()) {
 					list.add(VjoCcOverrideProposalAdvisor.ID);
 					if (!bol) {
 						list.add(VjoCcConstructorGenProposalAdvisor.ID);
 					}
 				}
 			}
 
 
 			// ctx.putInfo(INFO_KEY_STATICFUNCTION, ctx.isInStatic());
 			String[] ss = new String[list.size()];
 			list.toArray(ss);
 			return ss;
 		} else if (completion instanceof JstCompletionOnQualifiedNameReference) {
 			return handleJstCompletionOnQualifiedNameReference(ctx);
 		} else if (completion instanceof JstCompletionOnSingleNameReference) {
 			// this completion also returned when cursor is out of function.
 			if (ctx.isInMtdCall()) {
 				final String completionToken = completion.getToken();
 				if("".equals(completionToken)){
 					// xxx(<cursor>a)
 					IJstNode node = ctx.findNearestNode();
 					MtdInvocationExpr mtdInvo = null;
 					if (node == null) {
 						mtdInvo = null;
 					} else if (node instanceof MtdInvocationExpr) {
 						mtdInvo = (MtdInvocationExpr) node;
 					} else if (node.getParentNode() instanceof MtdInvocationExpr) {
 						mtdInvo = (MtdInvocationExpr) node.getParentNode();
 						if (!mtdInvo.getArgs().contains(node)) {
 							mtdInvo = null;
 						}
 					}
 					if (mtdInvo != null) {
 						// This advisor is available, Only when parameter can be
 						// found,
 						if (VjoCcParameterHintAdvisor.isAvailable(mtdInvo, ctx)) {
 							ctx.putInfo(VjoCcCtx.INFO_KEY_PARAMETER_HINT, mtdInvo);
 							return new String[] { VjoCcParameterHintAdvisor.ID };
 						}
 					}
 				}
 				else if("function".startsWith(completionToken)){
 					final IJstNode node = completion.getRealParent();
 					if(node instanceof JstIdentifier){
 						final IJstNode nodeParent = node.getParentNode();
 						if(nodeParent instanceof MtdInvocationExpr){
 							final MtdInvocationExpr mtdInvocationExpr = (MtdInvocationExpr)nodeParent;
 							final int position = mtdInvocationExpr.getArgs().indexOf(node);
 							if(position >= 0
 									&& mtdInvocationExpr.getMethod() instanceof IJstMethod){
 								final List<JstArg> parameters = ((IJstMethod)mtdInvocationExpr.getMethod()).getArgs();
 								if(parameters != null && parameters.size() > position){
 									final IJstType parameterType = parameters.get(position).getType();
 									if(isExtendedFunc(parameterType) || parameterType instanceof JstFuncType || parameterType instanceof JstFunctionRefType){
 										//part of arguments, register for cc function argument advisor
 										ctx.putInfo(VjoCcCtx.INFO_KEY_ARGUMENT, node);
 										return new String[] { VjoCcFunctionArgumentAdvisor.ID };
 									}
 								}
 							}
 						}
 					}
 				}
 			}
 			else if (!completion.inScope(ScopeIds.METHOD)) {
 				if (completion.inScope(ScopeIds.INITS)) {
 
 					return new String[] { VjoCcPackageProposalAdvisor.ID,
 							VjoCcCTypeProposalAdvisor.ID,
 							VjoCcThisProposalAdvisor.ID,
 							VjoCcNeedsItemProposalAdvisor.ID,
 							VjoCcGlobalAdvisor.ID,
 							VjoCcAliasProposalAdvisor.ID,
 							VjoCCVjoUtilityAdvisor.ID,
 							VjoCcStaticPropMethodProposalAdvisor.ID,
 							VjoCcKeywordInMethodProposalAdvisor.ID,
 							VjoCcVariableProposalAdvisor.ID};
 
 				}else if (ctx.isInStatic()) {
 					return new String[] { VjoCcGlobalAdvisor.ID,
 							VjoCCVjoUtilityAdvisor.ID };
 				} else {
 					return new String[] { VjoCcGlobalAdvisor.ID,
 							VjoCCVjoUtilityAdvisor.ID };
 				}
 			}
 			if (ctx.isInObjectCreateExpr()) {
 				return new String[] { VjoCcAliasProposalAdvisor.ID,
 						VjoCcCTypeProposalAdvisor.ID,
 						VjoCcThisProposalAdvisor.ID };
 			}
 			if (ctx.isInSimpeLiteral()) {
 				return new String[0];
 			}
 
 			IJstType type = ctx.getActingType();
 			if (type != null && type.isEnum()) {
 				result.add(VjoCcEnumElementAdvisor.ID);
 			}
 			result.add(VjoCcParameterProposalAdvisor.ID);
 			result.add(VjoCcVariableProposalAdvisor.ID);
 			result.add(VjoCcStaticPropMethodProposalAdvisor.ID);
 			result.add(VjoCcGlobalAdvisor.ID);
 			result.add(VjoCcKeywordInMethodProposalAdvisor.ID);
 			result.add(VjoCcThisProposalAdvisor.ID);
 			if (ctx.isCalledFromInnerType()) {
 				result.add(VjoCcOuterPropMethodProposalAdvisor.ID);
 			}
 			String token = ctx.getToken();
 			if (StringUtils.isBlankOrEmpty(token)) {
 				String nToken = ctx.calculateToken();
 				if (!StringUtils.isBlankOrEmpty(nToken)) {
 					// after some keyword, the token will not right, need
 					// recalculate it
 					token = nToken;
 				}
 			}
 			if (!StringUtils.isBlankOrEmpty(token)) {
 				ctx.setActingToken(token);
 				result.add(VjoCcTypeProposalAdvisor.ID);
 				result.add(VjoCcAliasProposalAdvisor.ID);
 				result.add(VjoCcPackageProposalAdvisor.ID);
 				ctx.setActingPackageToken(token);
 			} else {
 				result.add(VjoCcOwnerTypeProposalAdvisor.ID);
 			}
 			if (!ctx.isInStaticMethod()) {
 				result.add(VjoCcPropMethodProposalAdvisor.ID);
 				result.add(VjoCcDerivedPropMethodAdvisor.ID);
 			}
 			String[] temp = new String[result.size()];
 
 			return result.toArray(temp);
 		} else if (completion instanceof JstCompletionOnMemberAccess) {
 			IJstNode node = completion.getRealParent();
 			if (node != null && node instanceof IJstType) {
 				IJstType type = (IJstType) node;
 				ctx.setCalledType(type);
 				return new String[] { VjoCcPropMethodProposalAdvisor.ID,
 						VjoCcDerivedPropMethodAdvisor.ID};
 			} else {
 				return new String[] {};
 			}
 		} else if (completion instanceof JstComletionOnMessageSend) {
 			// Scene:
 			// vjo.ctype().satifies('<cursor>')
 			if (isSyntaxBlockCall(completion)) {
 				return handleSyntaxBlockOnMessageSend(ctx, completion);
 			}
 			// xxx( <cursor> a, b)
 			if (completion.getRealParent() instanceof MtdInvocationExpr) {
 
 				List<String> advisors = new ArrayList<String>();
 				MtdInvocationExpr mtd = (MtdInvocationExpr) completion
 						.getRealParent();
 				// TODO problem when this is jst arg not jstmethod
 				JstMethod method = (JstMethod)mtd.getMethod();
 				if(method!=null){
 					IJstType type = method.getOwnerType();
 
 					if(method.isFuncArgMetaExtensionEnabled()){
 
 						String targetFunc = method.getOwnerType().getName()
 								+ (method.isStatic() ? "::" : ":")
 								+ method.getName().getName();
 
 						if(FunctionParamsMetaRegistry.getInstance().isFirstArgumentType(targetFunc,type.getPackage().getGroupName() )){
 							// TODO jstmethod to method key add to utility method
 							// TODO add alias advisor
 							advisors.add(VjoCcTypeProposalAdvisor.ID);
 							advisors.add(VjoCcTypeNameAliasProposalAdvisor.ID);
 							// TODO add package list here
 						//	advisors.add(VjoCcPackageProposalAdvisor.ID);
 							return advisors.toArray(new String[advisors.size()]); 
 						}
 
 					}
 				}
 
 				// TODO add extension here for other libraries to add custom advisors
 				// method key as input
 				// return list of advisors
 
 				// This advisor is available, Only when parameter can be found,
 				// by huzhou@ebay.com propose vjo.getType with package/type proposal
 				if(isVjoGetTypeProposal(mtd)){
 					//must be syntax scope, otherwise, the type string will be replaced by this.vj$
 					//must set package info, otherwise, type partial match won't happen
 					ctx.putInfo(VjoCcCtx.INFO_KEY_IN_TYPE_SCOPE, true);
 					ctx.setActingPackageToken(ctx.getToken());
 					advisors.add(VjoCcTypeProposalAdvisor.ID);
 					advisors.add(VjoCcPackageProposalAdvisor.ID);
 					return advisors.toArray(new String[advisors.size()]);
 				}
 				if (VjoCcParameterHintAdvisor.isAvailable(mtd, ctx)) {
 					ctx.putInfo(VjoCcCtx.INFO_KEY_PARAMETER_HINT, mtd);
 					advisors.add(VjoCcParameterHintAdvisor.ID);
 					return advisors.toArray(new String[advisors.size()]);
 				}
 			} else if (completion.getRealParent() instanceof ObjCreationExpr) {
 				ObjCreationExpr oce = (ObjCreationExpr) completion
 						.getRealParent();
 				MtdInvocationExpr mtd = oce.getInvocationExpr();
 				// This advisor is available, Only when parameter can be found,
 				if (mtd != null
 						&& VjoCcParameterHintAdvisor.isAvailable(mtd, ctx)) {
 					ctx.putInfo(VjoCcCtx.INFO_KEY_PARAMETER_HINT, mtd);
 					return new String[] { VjoCcParameterHintAdvisor.ID };
 				}
 			}
 			// Jack, scene: window.xx(<cursor>)
 			// TODO: the following code maybe never be used
 			ctx.setActingToken("");
 			if (ctx.isInStatic()) {
 				return new String[] { VjoCcThisProposalAdvisor.ID,
 						VjoCcStaticPropMethodProposalAdvisor.ID,
 						VjoCcGlobalAdvisor.ID, VjoCcVariableProposalAdvisor.ID,
 						VjoCcParameterProposalAdvisor.ID };
 			} else {
 				return new String[] { VjoCcThisProposalAdvisor.ID,
 						VjoCcStaticPropMethodProposalAdvisor.ID,
 						VjoCcPropMethodProposalAdvisor.ID,
 						VjoCcGlobalAdvisor.ID, VjoCcVariableProposalAdvisor.ID,
 						VjoCcParameterProposalAdvisor.ID };
 			}
 		} else if (completion instanceof JstTypeCompletion) {
 
 			return analyseFromScriptUnit(ctx);
 			// return new String[] { VjoCcKeywordAdvisor.ID };
 
 		} else if (completion instanceof JstKeywordCompletion) {
 			return new String[] { VjoCcKeywordAdvisor.ID };
 		}
 		return new String[0];
 	}
 
 	private boolean isExtendedFunc(IJstType parameterType) {
 		if(parameterType instanceof JstExtendedType){
 			JstExtendedType ept = (JstExtendedType)parameterType;
 			if(ept.getTargetType().getName().equals("Function")){
 				return true;
 			}
 		}
 		return false;
 	}
 
 	private boolean isVjoGetTypeProposal(MtdInvocationExpr mtd) {
 		return mtd != null
 				&& mtd.getQualifyExpr() != null
 				&& mtd.getMethodIdentifier() != null
 				&& "vjo".equals(mtd.getQualifyExpr().toExprText())
 				&& "getType".equals(mtd.getMethodIdentifier().toExprText());
 	}
 
 	private String[] handleSyntaxBlockOnMessageSend(VjoCcCtx ctx,
 			JstCompletion completion) {
 
 		IJstNode parentNode = completion.getRealParent();
 		MtdInvocationExpr expr = (MtdInvocationExpr) parentNode;
 		String key = expr.getMethodIdentifier().toExprText();
 		String stype = expr.getMethod().getOwnerType().getSimpleName();
 		ctx.putInfo(VjoCcCtx.INFO_KEY_IN_TYPE_SCOPE, true);
 		ctx.setActingPackageToken(ctx.getToken());
 		if (CodeCompletionUtils.isTypeDeclare(key)) {
 			return new String[] { VjoCcTypeNameAdvisor.ID };
 		} else if (CodeCompletionUtils.isCTypeRefDeclare(key, stype)) {
 			return new String[] { VjoCcCTypeProposalAdvisor.ID,
 					VjoCcPackageProposalAdvisor.ID };
 		} else if (CodeCompletionUtils.isInterfaceRefDeclare(key, stype)) {
 			return new String[] { VjoCcInterfaceProposalAdvisor.ID,
 					VjoCcPackageProposalAdvisor.ID };
 		} else if (CodeCompletionUtils.isMixinTypeRefDeclare(key)) {
 			ctx.setMtypeEabled(true);
 			return new String[] { VjoCcMTypeProposalAdvisor.ID,
 					VjoCcPackageProposalAdvisor.ID };
 		} else if (CodeCompletionUtils.isTypeRefDeclare(key)) {
 			return new String[] { VjoCcTypeProposalAdvisor.ID,
 					VjoCcPackageProposalAdvisor.ID };
 		}
 		return new String[0];
 
 	}
 
 	private boolean isSyntaxBlockCall(JstCompletion completion) {
 		IJstNode parentNode = completion.getRealParent();
 		if (!(parentNode instanceof MtdInvocationExpr)) {
 			return false;
 		}
 		MtdInvocationExpr expr = (MtdInvocationExpr) parentNode;
 		IJstNode method = expr.getMethod();
 		if (method == null) {
 			return false;
 		}
 		IJstType type = method.getOwnerType();
 		if(type==null){
 			return false;
 		}
 		String typeName = type.getName();
 		// TODO: here should use group name, but the result is empty now.
 		if (CodeCompletionUtils.isTypeType(typeName)) {
 			return true;
 		}
 		return false;
 	}
 
 	/**
 	 * When cursor is out of Props or Protos, JstCompletion is not enought to
 	 * calculate the proposals, then handle will calculate from ISciptUnit
 	 * 
 	 * @param ctx
 	 * @return
 	 */
 	private String[] analyseFromScriptUnit(VjoCcCtx ctx) {
 		MtdInvocationExpr mtdInvo = ctx.getSMtdInvo();
 		JstIdentifier jidentifier = ctx.getSIdentifer();
 		IJstCompletion completion = ctx.getCompletion();
 		SimpleLiteral sl = ctx.getSSimpleLiteral();
 		if (sl != null) {
 			String token = ctx.getSSimpleToken();
 			String stype = ctx.getSTypeStrForCC();
 			if (token == null) {// wrong place (<Cursor>"")
 				return new String[0];
 			}
 			ctx.setActingToken(token);
 			BaseJstNode pnode = sl.getParentNode();
 			if (pnode instanceof JstArrayInitializer) {
 				pnode = pnode.getParentNode();
 			}
 			if (!(pnode instanceof MtdInvocationExpr)) {
 				return new String[0];
 			}
 			String key = ((MtdInvocationExpr) pnode).getMethodIdentifier()
 					.toExprText();
 			ctx.setActingPackageToken(token);
 			ctx.putInfo(VjoCcCtx.INFO_KEY_IN_TYPE_SCOPE, true);
 			if (CodeCompletionUtils.isTypeDeclare(key)) {
 				return new String[] { VjoCcTypeNameAdvisor.ID };
 			} else if (CodeCompletionUtils.isCTypeRefDeclare(key, stype)) {
 				return new String[] { VjoCcCTypeProposalAdvisor.ID,
 						VjoCcPackageProposalAdvisor.ID };
 			} else if (CodeCompletionUtils.isInterfaceRefDeclare(key, stype)) {
 				return new String[] { VjoCcInterfaceProposalAdvisor.ID,
 						VjoCcPackageProposalAdvisor.ID };
 			} else if (CodeCompletionUtils.isMixinTypeRefDeclare(key)) {
 				ctx.setMtypeEabled(true);
 				return new String[] { VjoCcMTypeProposalAdvisor.ID,
 						VjoCcPackageProposalAdvisor.ID };
 			} else if (CodeCompletionUtils.isTypeRefDeclare(key)) {
 				return new String[] { VjoCcTypeProposalAdvisor.ID,
 						VjoCcPackageProposalAdvisor.ID };
 			}
 		}
 		if (mtdInvo != null) {
 			if (jidentifier == null) {
 				return new String[] { VjoCCVjoUtilityAdvisor.ID };
 			} else if ("endType".equals(jidentifier.getName())) {
 				// This is temp way, we should calculate it based on the type
 				// method returns
 				return new String[0];
 			} else {
 				IJstNode pNode = jidentifier.getParentNode();
 				IJstType tempType = jidentifier.getType();
 				if (tempType == null && pNode != null
 						&& (pNode instanceof MtdInvocationExpr)) {
 					MtdInvocationExpr mie = (MtdInvocationExpr) pNode;
 					JstIdentifier mtdidentifer = (JstIdentifier) mie
 							.getMethodIdentifier();
 					if (mtdidentifer != null
 							&& mtdidentifer.getResultType() != null) {// This
 						tempType = mtdidentifer.getResultType();
 					} else {
 						tempType = findCurrentTypeType(ctx);
 					}
 				}
 				ctx.putInfo(VjoCcCtx.INFO_KEY_IN_TYPE_SCOPE, true);
 				ctx.setCalledType(tempType);
 				return new String[] { VjoCcPropMethodProposalAdvisor.ID,
 						VjoCcDerivedPropMethodAdvisor.ID,
 						VjoCcStaticPropMethodProposalAdvisor.ID };
 			}
 		} else {
 			if (jidentifier != null && "endType".equals(jidentifier.getName())) {
 				// This is temp way, we should calculate it based on the type
 				// method returns
 				return new String[0];
 			}
 			// if cursor is out of ''
 			IJstType type = ctx.findBaseType("vjo");
 			String[] stack = ctx.getSKeywordStack();
 			// this code will never be used, see the VjoCcCtx.m_identifier how
 			// to calculate out
 			// if (ctx.ifSContainKeyword("endType")) {
 			// return new String[0];
 			// }
 			if (stack.length == 0) {
 				if (completion == null) {
 					return new String[0];
 				} else {
 					// It happens when there are js expression before the vjo
 					// definition.
 					// See bug 5076
 					IJstType jstType = ((JstCompletion) completion)
 							.getOwnerType();
 					if (completion instanceof JstKeywordCompletion
 							&& jstType != null) {
 						String[] tproposals = completion.getCompletion();
 						if (tproposals.length == 1
 								&& tproposals[0].equals("vjo")) {
 							// it is not a good way used to propose "vjo"
 							return new String[] { VjoCCVjoUtilityAdvisor.ID };
 						}
 						String token = ((JstCompletion) completion).getToken();
 						if (jstType != null && jstType.getName() != null
 								&& token != null) {
 							return genScriptProposalFromTypeAndToken(ctx,
 									jstType, token);
 
 						}
 					}
 				}
 
 			} else {
 				type = findCurrentTypeType(ctx);
 			}
 			ctx.setCalledType(type);
 			return new String[] { VjoCcPropMethodProposalAdvisor.ID,
 					VjoCcDerivedPropMethodAdvisor.ID,
 					VjoCcStaticPropMethodProposalAdvisor.ID };
 		}
 
 		// if (mtdInvo == null) {
 		// IJstType jstType = ctx.getActingType();
 		// // can not make judgement by IScriptUnit, only depend on
 		// // JstKeywCompletion
 		// if (completion != null
 		// && completion instanceof JstKeywordCompletion) {
 		// String[] tproposals = completion.getCompletion();
 		// if (tproposals.length == 1 && tproposals[0].equals("vjo")) {
 		// // it is not a good way used to propose "vjo"
 		// return new String[] { VjoCCVjoUtilityAdvisor.ID };
 		// }
 		// String token = ((JstCompletion) completion).getToken();
 		// if (jstType != null && jstType.getName() != null
 		// && token != null) {
 		// return genScriptProposalFromTypeAndToken(ctx, jstType,
 		// token);
 		//
 		// }
 		// }
 		//
 		// }
 		// return new String[0];
 	}
 
 	private IJstType findCurrentTypeType(VjoCcCtx ctx) {
 		String typeStr = ctx.getSTypeStrForCC();
 		if (StringUtils.isBlankOrEmpty(typeStr)) {
 			return ctx.findBaseType("vjo");
 		}
 		IJstType ttype = ctx.findBaseType(typeStr);
 		return ttype;
 	}
 
 	private String[] genScriptProposalFromTypeAndToken(VjoCcCtx ctx,
 			IJstType jstType, String token) {
 		IJstType vjoType = ctx.findBaseType("vjo");
 		String methodName = "ctype";
 		if (jstType.isMixin()) {
 			methodName = "mtype";
 		} else if (jstType.isInterface()) {
 			methodName = "itype";
 		} else if (jstType.isOType()) {
 			methodName = "otype";
 		} else if (jstType.isEnum()) {
 			methodName = "etype";
 		} else {
 			methodName = "ctype";
 		}
 		IJstMethod method = vjoType.getMethod(methodName);
 		if (method == null) {
 			return new String[0];
 		} else {
 			ctx.setCalledType(method.getRtnType());
 			ctx.setActingToken(token);
 			return new String[] { VjoCcPropMethodProposalAdvisor.ID,
 					VjoCcDerivedPropMethodAdvisor.ID,
 					VjoCcStaticPropMethodProposalAdvisor.ID };
 		}
 	}
 
 	private String[] handleJstCompletionOnQualifiedNameReference(VjoCcCtx ctx) {
 		JstCompletion completion = ctx.getCompletion();
 		String token = completion.getToken();
 		return handleCommonExpr(ctx, token);
 	}
 
 	private String trimToken(String token) {
 		if (StringUtils.isBlankOrEmpty(token)) {
 			return "";
 		}
 		token = token.trim();
 		if (token.indexOf("\n") > 0) {
 			token = token.substring(token.lastIndexOf("\n") + 1);
 		}
 		if (token.indexOf("\t") > 0) {
 			token = token.substring(token.lastIndexOf("\t") + 1);
 		}
 		if (token.indexOf(".") > 0) {
 			// this case is a bug for JstCompletion, when document.a\n abc, the
 			// token will be document.a, should be a, seems the \n can not be
 			// taken care correctly.
 			token = token.substring(token.lastIndexOf(".") + 1);
 		}
 		return token;
 	}
 
 	private String[] handleCommonExpr(VjoCcCtx ctx, String str) {
 		String token = trimToken(str);
 		if (isDeclareToken(token)) {// xx:xx
 			return handlerDeclareToken(ctx, token);
 		}
 		JstCompletion completion = ctx.getCompletion();
 		IJstNode node = completion.getRealParent();
 		//List<String> result = new ArrayList<String>();
 		Set<String> result = new TreeSet<String>();
 		IJstType type = null;
 		boolean afterThis = false;
 		if (node instanceof FieldAccessExpr) {
 			FieldAccessExpr fae = (FieldAccessExpr) node;
 			IExpr qualifier = fae.getExpr();
 			if (qualifier instanceof JstIdentifier) {
 				IJstNode binding = ((JstIdentifier)qualifier).getJstBinding();
 				String globalVarName = null;
 				if (binding instanceof IJstGlobalProp) {
 					globalVarName = ((IJstGlobalProp)binding).getName().getName();
 				} else if (binding instanceof IJstGlobalFunc) {
 					globalVarName = ((IJstGlobalFunc)binding).getName().getName();
 				}
 				if (globalVarName != null) {
 					boolean hasGlobalExtension =
 						ctx.getJstTypeSpaceMgr().getQueryExecutor().hasGlobalExtension(globalVarName);
 					if (hasGlobalExtension) {
 						result.add(VjoCcGlobalExtensionAdvisor.ID);
 					}
 				}
 			}else if(qualifier instanceof MtdInvocationExpr){
 				MtdInvocationExpr mtdInvo = ((MtdInvocationExpr)qualifier);
				type = mtdInvo.getResultType();
				
 			}
 			
 			if(type==null){
 				type = fae.getExpr().getResultType();
 			}
 			if (!StringUtils.isBlankOrEmpty(str) && !StringUtils.isVj$Expr(str)) {
 				ctx.setActingPackageToken(str);
 				result.add(VjoCcPackageProposalAdvisor.ID);
 				result.add(VjoCcTypeProposalAdvisor.ID);
 
 			}
 		} else if (node instanceof MtdInvocationExpr) {
 			// Scenerior: v.<cursor>method();
 			MtdInvocationExpr mie = (MtdInvocationExpr) node;
 			IExpr jid = mie.getQualifyExpr();
 			if (jid != null) {
 				type = jid.getResultType();
 				afterThis = CompletionConstants.THIS.equals(jid.toExprText());
 				if (afterThis) {
 					ctx.setPositionType(VjoCcCtx.POSITION_AFTER_THIS);
 					type = completion.getOwnerType();
 					if (ctx.isInStatic()) {
 						type = new JstTypeRefType(type);
 					}
 				} else if (jid instanceof FieldAccessExpr) {
 					FieldAccessExpr fae = (FieldAccessExpr) jid;
 					String exprText = fae.toExprText();
 					if (!StringUtils.isBlankOrEmpty(exprText)
 							&& !StringUtils.isVj$Expr(exprText)) {
 						ctx.setActingPackageToken(exprText+ "." + completion.getToken());
 						result.add(VjoCcPackageProposalAdvisor.ID);
 						result.add(VjoCcTypeProposalAdvisor.ID);
 					}
 				}
 				else if (jid instanceof JstIdentifier) {
 					JstIdentifier identifier = (JstIdentifier)jid;
 					String idStr = identifier.getName();
 					if (!StringUtils.isBlankOrEmpty(idStr)) {
 						ctx.setActingPackageToken(idStr + "." + completion.getToken());
 						result.add(VjoCcPackageProposalAdvisor.ID);
 						result.add(VjoCcTypeProposalAdvisor.ID);
 					}
 				}else if(jid instanceof MtdInvocationExpr){
 					MtdInvocationExpr mtdInvo = ((MtdInvocationExpr)jid);
 					IJstMethod iJstMethod = (IJstMethod)mtdInvo.getMethod();
 					if(iJstMethod!=null){
 						type = iJstMethod.getRtnType();
 					}
 				}
 			}
 			if (type == null) {
 				IJstNode method = mie.getMethod();
 				if (method == null) {
 					type = mie.getResultType();
 				} else {
 					type = method.getOwnerType();
 					ctx.setPositionType(VjoCcCtx.POSITION_AFTER_THIS);
 				}
 			}
 
 		}
 
 		if (type == null) {
 			String[] ss = new String[result.size()];
 			ss = result.toArray(ss);
 			return ss;
 		}
 		if (ctx.callFromClass(completion, type)) {
 			result.add(VjoCcEnumElementAdvisor.ID);
 			result.add(VjoCcStaticPropMethodProposalAdvisor.ID);
 		} else {
 			result.add(VjoCcPropMethodProposalAdvisor.ID);
 			result.add(VjoCcDerivedPropMethodAdvisor.ID);
 		}
 		ctx.setCalledType(type);
 		ctx.setActingToken(token);
 		String[] sresult = new String[result.size()];
 		result.toArray(sresult);
 		return sresult;
 	}
 
 	private String[] handlerDeclareToken(VjoCcCtx ctx, String token) {
 		String newToken = token.substring(token.indexOf(":"));
 		ctx.setActingToken(newToken);
 		if (ctx.isInStatic()) {
 			return new String[] { VjoCcThisProposalAdvisor.ID,
 					VjoCcStaticPropMethodProposalAdvisor.ID,
 					VjoCcGlobalAdvisor.ID };
 		} else {
 			return new String[] { VjoCcThisProposalAdvisor.ID,
 					VjoCcStaticPropMethodProposalAdvisor.ID,
 					VjoCcPropMethodProposalAdvisor.ID, VjoCcDerivedPropMethodAdvisor.ID,
 					VjoCcGlobalAdvisor.ID };
 		}
 	}
 
 	private boolean isDeclareToken(String token) {
 		if (token.indexOf(":") > -1) {
 			return true;
 		}
 		return false;
 	}
 
 	protected void handleCommentCompletion(VjoCcCtx ctx) {}
 
 }
