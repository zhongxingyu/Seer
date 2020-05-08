 /*******************************************************************************
  * Copyright (c) 2005, 2012 eBay Inc.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  *******************************************************************************/
 package org.eclipse.vjet.dsf.jstojava.translator;
 
 import java.util.List;
 
 import org.eclipse.mod.wst.jsdt.internal.compiler.ast.Expression;
 import org.eclipse.mod.wst.jsdt.internal.compiler.ast.ObjectLiteral;
 import org.eclipse.mod.wst.jsdt.internal.compiler.ast.ObjectLiteralField;
 import org.eclipse.vjet.dsf.jsgen.shared.ids.ScopeIds;
 import org.eclipse.vjet.dsf.jst.BaseJstNode;
 import org.eclipse.vjet.dsf.jst.IJstType;
 import org.eclipse.vjet.dsf.jst.declaration.JstArg;
 import org.eclipse.vjet.dsf.jst.declaration.JstCache;
 import org.eclipse.vjet.dsf.jst.declaration.JstFunctionRefType;
 import org.eclipse.vjet.dsf.jst.declaration.JstMethod;
 import org.eclipse.vjet.dsf.jst.declaration.JstModifiers;
 import org.eclipse.vjet.dsf.jst.declaration.JstObjectLiteralType;
 import org.eclipse.vjet.dsf.jst.declaration.JstPackage;
 import org.eclipse.vjet.dsf.jst.declaration.JstProperty;
 import org.eclipse.vjet.dsf.jst.declaration.JstType;
 import org.eclipse.vjet.dsf.jst.expr.FieldAccessExpr;
 import org.eclipse.vjet.dsf.jst.expr.FuncExpr;
 import org.eclipse.vjet.dsf.jst.meta.IJsCommentMeta;
 import org.eclipse.vjet.dsf.jst.term.JstLiteral;
 import org.eclipse.vjet.dsf.jst.term.NV;
 import org.eclipse.vjet.dsf.jst.term.ObjLiteral;
 import org.eclipse.vjet.dsf.jst.token.IExpr;
 import org.eclipse.vjet.dsf.jst.traversal.JstDepthFirstTraversal;
 import org.eclipse.vjet.dsf.jst.util.JstCommentHelper;
 import org.eclipse.vjet.dsf.jstojava.parser.comments.JsParam;
 import org.eclipse.vjet.dsf.jstojava.translator.robust.ast2jst.BaseAst2JstTranslator;
 import org.eclipse.vjet.dsf.jstojava.translator.robust.ast2jst.TranslatorFactory;
 
 public class DefsTranslator extends BasePropsProtosTranslator {
 	public DefsTranslator(TranslateCtx ctx) {
 		super(ctx);
 		type = ScopeIds.DEFS;
 	}
 
 	@Override
 	public void process(Expression expr, JstType jstType) {
 		// TODO Auto-generated method stub
 		super.process(expr, jstType);
 
 		if (m_ctx.hasFunctionTypeRefReplacements()) {
 			JstDepthFirstTraversal.accept(jstType, new FunctionTypeRefVisitor(
 					m_ctx.getFunctionTypeRefReplacements()));
 		}
 
 	}
 
 	protected void translateExpr(Expression expr, JstType jstType) {
 		getCtx().enterBlock(type);
 		try {
 			BaseAst2JstTranslator translator;
 			if (expr instanceof ObjectLiteral) {
 				ObjectLiteral literal = (ObjectLiteral) expr;
 				if (literal.fields != null) {
 					for (ObjectLiteralField field : literal.fields) {
 						if (field.getInitializer() instanceof ObjectLiteral) {
 							JstObjectLiteralType objLitType = new JstObjectLiteralType(
 									field.fieldName.toString());
 							if (m_ctx.getCurrentType().getPackage() != null) {
 								objLitType.setPackage(new JstPackage(m_ctx
 										.getCurrentType().getPackage()
 										.getName()
 										+ "."
 										+ m_ctx.getCurrentType().getName()));
 							}
 							// jstObjLitCfgType.addProperty(new
 							// JstProperty(objLitType, propName));
 							// jstObjLitCfgType.addOType(objLitType);
 
 							jstType.addOType(objLitType);
 						}
 					}
 				}
 			}
 
 			// if (expr instanceof ObjectLiteral) {
 			// translator = new VjoOLTranslator(getCtx());
 			// } else {
 			// TODO - Throw error here...
 			translator = TranslatorFactory.getTranslator(expr, getCtx());
 			// }
 			translator.setParent(jstType);
 			Object node = translator.translate(expr);
 			// post process Object Literal
 
 			if (node instanceof ObjLiteral) {
 				ObjLiteral literal = ((ObjLiteral) node);
 				for (NV field : literal.getNVs()) {
 					processDef(jstType, literal, field);
 				}
 
 			}
 
 		} finally {
 			getCtx().exitBlock();
 		}
 	}
 
 	private void processDef(JstType jstType, ObjLiteral literal, NV field) {
 
 		IExpr value = field.getValue();
 		if (value != null) {
 
 			if (value instanceof ObjLiteral) {
 				processObjLiteralDef(jstType, field.getName(),
 						(ObjLiteral) value);
 			} else if (value instanceof FuncExpr) {
 				processFunctionDef(jstType, ((FuncExpr) value).getFunc());
 
 			} else if (value instanceof FieldAccessExpr) {
 				IJstType type = ((FieldAccessExpr) value).getType();
 				IJstType fnType = JstCache.getInstance().getType("Function");
 				if (fnType != null && fnType.equals(type)) {
 
 					processFunction(jstType, field.getName(),
 							(FieldAccessExpr) value, field);
 
 				}
 			} else if (value instanceof BaseJstNode) {
 				processObjLiteralDef(jstType, field.getName(),
 						(BaseJstNode) value, field);
 			}
 
 		}
 
 	}
 
 	// take the object literal and construct
 	// JstObjLiteralType
 	private void processObjLiteralDef(JstType jstType, String name,
 			ObjLiteral value) {
 
 		JstObjectLiteralType otype = (JstObjectLiteralType) jstType
 				.getOType(name);
 
 		if (otype == null) {
 			otype = new JstObjectLiteralType(name);
 		}
 		otype.setPackage(new JstPackage(jstType.getName()));
 		// TODO add source info
 		JstCache.getInstance().addOType(otype);
 		jstType.addProperty(new JstProperty(otype, name));
 		for (int i = 0; i < value.getNVs().size(); i++) {
 			NV field = value.getNVs().get(i);
 
 			JstProperty prop = createPropertyFromNV(field);
 
 			otype.addProperty(prop);
 			if (field.isOptional()) {
 				otype.addOptionalField(prop);
 			}
 		}
 		jstType.addOType(otype);
 	}
 
 	private void processObjLiteralDef(JstType jstType, String name,
 			BaseJstNode value, NV field) {
 		if (value instanceof FieldAccessExpr) {
 			FieldAccessExpr fieldAccessExpr = (FieldAccessExpr) value;
 			IJstType type = fieldAccessExpr.getType();
 			IJstType fnType = JstCache.getInstance().getType("Function");
 			if (fnType != null && fnType.equals(type)) {
 				processFunction(jstType, name, fieldAccessExpr, field);
 			}
 		} else {
 			JstObjectLiteralType otype = new JstObjectLiteralType(name);
 			otype.setPackage(new JstPackage(jstType.getName()));
 			// TODO add source info
 			JstCache.getInstance().addOType(otype);
 			jstType.addProperty(new JstProperty(otype, name));
 
 			jstType.addOType(otype);
 		}
 	}
 
 	private void processFunction(JstType jstType, String name,
 			FieldAccessExpr fieldAccessExpr, NV nv) {
 		List<IJsCommentMeta> commentMeta = TranslateHelper
 				.findMetaFromExpr(fieldAccessExpr);
 		
 		
 		
 		if (commentMeta != null) {
 			JstMethod meth = (JstMethod) TranslateHelper.MethodTranslateHelper
 					.createJstSynthesizedMethod(commentMeta, m_ctx, name);
			JstMethod meth2 = new JstMethod(meth.getName(),
					meth.getModifiers(), meth.getRtnType());
			for (JstArg a : meth.getArgs()) {
				meth2.addArg(a);
			}
			meth2.setSource(nv.getSource());
			processFunctionDef(jstType, meth2);
			jstType.addMethod(meth2);
 		}
 
 	}
 
 	private IJsCommentMeta getLongestArgList(List<IJsCommentMeta> metaArr) {
 
 		IJsCommentMeta maxMeta = null;
 		int maxParamCount = 0;
 		List<JsParam> params = null;
 		for (IJsCommentMeta meta : metaArr) {
 			if (maxMeta == null) {
 				maxMeta = meta;
 				params = TranslateHelper.getParams(meta);
 				if (params != null) {
 					maxParamCount = params.size();
 				}
 			} else {
 				params = TranslateHelper.getParams(meta);
 				if (params != null && params.size() > maxParamCount) {
 					maxParamCount = params.size();
 					maxMeta = meta;
 				}
 			}
 		}
 		return maxMeta;
 	}
 
 	private JstProperty createPropertyFromNV(NV nv) {
 		IExpr value = nv.getValue();
 
 		if ((nv.getName() != null) && (nv.getName().length() > 0)
 				&& (value instanceof BaseJstNode)) {
 			BaseJstNode literal = (BaseJstNode) value;
 			// create the JstProperty
 			// problem with result type
 			IJstType jstType = null;
 			IJstType type = nv.getIdentifier().getType();
 			if (type != null) {
 				jstType = type;
 			}
 			if (jstType == null) {
 				List<IJsCommentMeta> commentMeta = TranslateHelper
 						.findMetaFromExpr(literal);
 				IJsCommentMeta meta = null;
 				if (commentMeta != null) {
 					meta = commentMeta.get(0);
 				}
 				if (meta != null) {
 					jstType = TranslateHelper.findType(m_ctx, meta.getTyping(),
 							meta);
 					
 					// done with meta array removing from tree
 //					meta = null;
 //					List<BaseJstNode> children = literal.getChildren();
 //					for (IJstNode child : children) {
 //						if (child instanceof JsCommentMetaNode) {
 //							literal.removeChild(child);
 //							break;
 //						}
 //					}
 				}
 
 			}
 
 			if (jstType == null) {
 				jstType = value.getResultType();
 			}
 			if (jstType == null) {
 				jstType = JstCache.getInstance().getType("Object");
 			}
 
 			JstProperty jstProperty = new JstProperty(jstType, nv.getName(),
 					value, new JstModifiers().setPublic());
 			jstProperty.setSource(nv.getSource());
 
 		
 
 //			 StringBuilder sb = new StringBuilder();
 //			 sb.append(JstCommentHelper.getCommentsAsString(jstType,nv.getCommentLocations()));
 //			 for(String comment: nv.getComments()){
 //				 sb.append(comment);
 //			 }
 			 jstProperty.setCommentLocations(nv.getCommentLocations());
 			 
 //			 JsDocHelper.addJsDoc(sb.toString(), jstProperty);
 
 			
 			return jstProperty;
 		}
 		return null;
 	}
 
 	private void processFunctionDef(JstType type, JstMethod mtd) {
 		if (mtd != null) {
 			type.addMethod(mtd);
 
 			JstFunctionRefType ref = new JstFunctionRefType(mtd);
 			ref.setPackage(new JstPackage(type.getName()));
 			type.addOType(ref);
 		}
 
 	}
 
 }
