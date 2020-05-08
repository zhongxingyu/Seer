 /*******************************************************************************
  * Copyright (c) 2005, 2012 eBay Inc.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  *******************************************************************************/
 package org.eclipse.vjet.dsf.jstojava.translator.robust.ast2jst;
 
 import java.util.List;
 
 import org.eclipse.vjet.dsf.jsgen.shared.ids.ScopeIds;
 import org.eclipse.vjet.dsf.jst.BaseJstNode;
 import org.eclipse.vjet.dsf.jst.JstSource;
 import org.eclipse.vjet.dsf.jst.declaration.JstMethod;
 import org.eclipse.vjet.dsf.jst.expr.FuncExpr;
 import org.eclipse.vjet.dsf.jst.expr.MtdInvocationExpr;
 import org.eclipse.vjet.dsf.jst.meta.IJsCommentMeta;
 import org.eclipse.vjet.dsf.jst.term.JstIdentifier;
 import org.eclipse.vjet.dsf.jst.term.JstProxyIdentifier;
 import org.eclipse.vjet.dsf.jst.token.IExpr;
 import org.eclipse.vjet.dsf.jstojava.translator.TranslateHelper;
 import org.eclipse.vjet.dsf.jstojava.translator.robust.completion.JstComletionOnMessageSend;
 import org.eclipse.vjet.dsf.jstojava.translator.robust.completion.JstCompletion;
 import org.eclipse.vjet.dsf.jstojava.translator.robust.completion.JstCompletionOnQualifiedNameReference;
 import org.eclipse.mod.wst.jsdt.core.ast.IExpression;
 import org.eclipse.mod.wst.jsdt.internal.compiler.ast.MessageSend;
 import org.eclipse.mod.wst.jsdt.internal.compiler.ast.ObjectLiteral;
 
 public class MessageSendTranslator extends
 		BaseAst2JstTranslator<MessageSend, MtdInvocationExpr> {
 
 	private static final String EMPTY = "";
 
 	private MtdInvocationExpr jstInvocation;
 
 	@Override
 	protected MtdInvocationExpr doTranslate(MessageSend astMsgSend) {
 
 		String methodNameStr = null;
 		if (astMsgSend.getSelector() == null) {
 			IExpression astReceiver = astMsgSend.getReceiver();
 			BaseAst2JstTranslator translator = getTranslator(astReceiver);
 			IExpr mtdIdentifier = (IExpr) translator.translate(astReceiver);
 			if(mtdIdentifier instanceof JstIdentifier){
 				jstInvocation = new MtdInvocationExpr(mtdIdentifier);
 			}
 			else{
 				jstInvocation = new MtdInvocationExpr(new JstProxyIdentifier(mtdIdentifier));
 			}
 		} else {
 			methodNameStr = new String(astMsgSend.getSelector());
 			jstInvocation = new MtdInvocationExpr(methodNameStr);
 		}
 
 		try {
 			m_ctx.enterBlock(ScopeIds.METHOD_CALL);
 			IExpression[] args = astMsgSend.getArguments();
 			if (args != null) {
 				int prev = 0;
 				int next = 0;
 				int len = args.length;
 				for (int i = 0; i < len; i++) {
 					if (i == 0) {
 						IExpression rec = astMsgSend.getReceiver();
 						prev = (rec != null) ? rec.sourceStart() : astMsgSend
 								.sourceStart();
 					} else {
 						prev = args[i - 1].sourceEnd();
 					}
 					if (i + 1 < len) {
 						next = args[i + 1].sourceStart();
 					} else {
 						next = astMsgSend.statementEnd;
 					}
 					IExpression argExpression = args[i];
 					BaseAst2JstTranslator translator;
 					if (argExpression instanceof ObjectLiteral
 							&& (isVjoOL(methodNameStr))) {
 						translator = new VjoOLTranslator(m_ctx);
 					} else {
 						translator = getTranslator(argExpression);
 					}
 					Object stmt = translator.translate(argExpression);
 					if (stmt instanceof IExpr) {
 						
 						List<IJsCommentMeta> metaList = null;
 						IExpr jstArg = null;
 						if(stmt instanceof BaseJstNode){
 							metaList = TranslateHelper.findMetaFromExpr((BaseJstNode)stmt);
 						}
 						if(metaList != null){
 							jstArg = TranslateHelper.getCastable((IExpr)stmt, metaList, m_ctx);
 							// remove the node from jst tree due to memory footprint and performance issue
 							metaList = null;
 //							BaseJstNode baseJstNode = (BaseJstNode)stmt;
 //							List<BaseJstNode> children = baseJstNode.getChildren();
 //							for (IJstNode child : children) {
 //								if (child instanceof JsCommentMetaNode) {
 //									baseJstNode.removeChild(child);
 //									child = null;
 //									break;
 //								}
 //							}
 						}
 						else{
 							jstArg = TranslateHelper.getCastable(
 									(IExpr) stmt, argExpression, prev, next, m_ctx);
 						}
 						jstInvocation.addArg(jstArg);
 					} else if (stmt instanceof JstMethod) {
 						IExpr jstArg = new FuncExpr((JstMethod) stmt);
 						jstArg = TranslateHelper.getCastable(jstArg,
 								argExpression, prev, next, m_ctx);
 						jstInvocation.addArg(jstArg);
 					} else {
 						if (m_ctx.getCurrentType() != null
 								&& m_ctx.getCurrentType().getName() != null) {
 							String name = m_ctx.getCurrentType().getName();
 							m_ctx.getErrorReporter().error(
 									"Cannot cast jstArg from "
 											+ argExpression.getClass() + " to "
 											+ IExpr.class + " in "
 											+ MessageSendTranslator.class,
 									name, 0, 0);
 						}
 					}
 					// System.err.println();
 				}
 			}
 		} finally {
 			m_ctx.exitBlock();
 		}
 		int sourceStart = astMsgSend.sourceStart;
 		IExpression astReceiver = astMsgSend.getReceiver();
 		if (astReceiver != null) {
 			// Jack, when there is comment after astReceiver, it will return
 			// uncorrect position
 			sourceStart = calculateSourceStart(astMsgSend, astReceiver,
 					astMsgSend.getSelector());
 			// sourceStart = astMsgSend.receiver.sourceEnd + 1;
 		}
 		if (methodNameStr != null) {
 			// receiver
 			if (astReceiver != null) {
 				BaseAst2JstTranslator translator = getTranslator(astReceiver);
 				IExpr jstQualifier = (IExpr) translator.translate(astReceiver);
 				jstInvocation.setQualifyExpr(jstQualifier);
 				
 			}
 			// name source
 			JstSource nameSource = TranslateHelper.getMethodSource(m_ctx
 					.getOriginalSource(), m_ctx.getSourceUtil(), sourceStart,
 					astMsgSend.sourceEnd, methodNameStr.length());
 
 			((JstIdentifier) jstInvocation.getMethodIdentifier())
 					.setSource(nameSource);
 		}
 
 		int sourceEnd = astMsgSend.statementEnd;
 		if (sourceEnd == -1) {
 			sourceEnd = astMsgSend.sourceEnd;
 		}
 
 		JstSource methodSource = TranslateHelper.getMethodSource(m_ctx
 		// Jack: Can not understand why use the
 				// astMsgSend.toString().length(), it cause offset issue, now
 				// use
 				// sourceEnd directly
 				// .getOriginalSource(),m_ctx.getSourceUtil(),
 				// astMsgSend.sourceStart, sourceEnd, astMsgSend
 				// .toString().length());
 				.getOriginalSource(), m_ctx.getSourceUtil(),
 				astMsgSend.sourceStart, sourceEnd, sourceEnd
 						- astMsgSend.sourceStart);
 
 		jstInvocation.setSource(methodSource);
 
 		return jstInvocation;
 	}
 
 	private boolean isVjoOL(String methodNameStr) {
 		return "protos".equals(methodNameStr) || "props".equals(methodNameStr)
 				|| "defs".equals(methodNameStr);
 	}
 
 	/**
 	 * To calculate identifier's start point.
 	 * 
 	 *  Liu
 	 * @param cs
 	 * @param astReceiver
 	 * @param astReceiver
 	 * @param selector
 	 * @return
 	 */
 	private int calculateSourceStart(IExpression astIExpression,
 			IExpression astReceiver, char[] cs) {
 		int messageEnd = astIExpression.sourceEnd();
 		int receiverEnd = astReceiver.sourceEnd();
 		if (cs == null) {
 			return receiverEnd + 1;
 		}
 		if (messageEnd > receiverEnd
 				&& messageEnd < m_ctx.getOriginalSource().length) {
 			String s = new String(m_ctx.getOriginalSource(), receiverEnd,
 					messageEnd - receiverEnd);
 			int index = s.indexOf(new String(cs));
 			if (index >= 0)
 				return receiverEnd + index;
 		}
 		return receiverEnd + 1;
 	}
 
 	@Override
 	protected JstCompletion createCompletion(MessageSend node, boolean isAfter) {
 
 		// Jack: ERROR: here calling "toStrig()" will cause wrong offset
 		// String literal = node.toString();
 		int tempBegin = node.sourceStart();
 		if (node.getReceiver() != null) {
 			tempBegin = node.getReceiver().sourceEnd() + 1;
 		}
 		String literal = new String(m_ctx.getOriginalSource(), tempBegin, node
 				.sourceEnd()
 				- tempBegin + 1);
 		JstCompletion completion = null;
 		// Jack
 		int indexOpenBracket = tempBegin + literal.indexOf(OPEN_BRACKET);
 		int indexCloseBracket = tempBegin + literal.lastIndexOf(CLOSE_BRACKET);
 
 		completion = createCompletion(node, indexOpenBracket, indexCloseBracket);
 		if (completion != null) {
 			m_ctx.setCreatedCompletion(true);
 		}
 
 		return completion;
 	}
 
 	private JstCompletion createCompletion(MessageSend node, int indexOpen,
 			int indexClose) {
 
 		JstCompletion completion = null;
 
 		IExpr mtdIdentifier = jstInvocation.getMethodIdentifier();
 		if (!(mtdIdentifier instanceof JstIdentifier)) {
 			// TODO
 			return completion;
 		}
 		JstIdentifier mtdNameIdentifier = (JstIdentifier) mtdIdentifier;
 
 		JstSource nameSource = mtdNameIdentifier.getSource();
 
 		if (isInBracket(indexOpen, indexClose, node) && node.selector != null) {
 			String token = getTokenInArgurent(node);
 			if (isJavaIdentifier(token) || isJavaPackageIdentifier(token)) {
 				completion = new JstComletionOnMessageSend(
 						(BaseJstNode) jstInvocation);
 				completion.setToken(token);
 				completion.setCompositeToken(getCompositeTokenInArgument(node));
 			}
 		} else if (isInName(nameSource)) {
 			completion = new JstCompletionOnQualifiedNameReference(
 					jstInvocation);
 			String token = getNameToken(mtdNameIdentifier);
 			completion.setToken(token);
 			completion.setCompositeToken(mtdNameIdentifier.getName());
 			completion.setSource(createSource(token));
 		} else if (afterRecevier(node)) {// a.xx().<cursor>.yy()
 			completion = new JstCompletionOnQualifiedNameReference(
 					jstInvocation);
 			String token = getTokenByPos(node);
 			String compoiteToken = getCompositeTokenByPos(node, token);
 			completion.setToken(token);
 			completion.setCompositeToken(compoiteToken);
 			completion.setSource(createSource(token));
 		}
 		// Jack: try to comment this
 		// else {
 		// completion = new JstCompletionOnSingleNameReference(m_ctx
 		// .getCurrentType());
 		// completion.setToken(EMPTY);
 		// }
 
 		return completion;
 	}
 
 	private String getCompositeTokenInArgument(MessageSend node) {
 		IExpression[] exprs = node.getArguments();
 		int pos = m_ctx.getCompletionPos();
 		if (exprs == null || exprs.length == 0) {
 			return "";
 		}
 		for (IExpression expr : exprs) {
 			if (expr.sourceStart() < pos && expr.sourceEnd() >= pos) {
 				String s = new String(m_ctx.getOriginalSource(), expr
 						.sourceStart() + 1, expr.sourceEnd()
 						- expr.sourceStart() - 1);
 				s = (s + "a").trim();
 				s = s.substring(0, s.length() - 1);
 				return s;
 			}
 		}
 		return "";
 	}
 
 	private String getTokenInArgurent(MessageSend node) {
 		IExpression[] exprs = node.getArguments();
 		int pos = m_ctx.getCompletionPos();
 		if (exprs == null || exprs.length == 0) {
 			return "";
 		}
 		for (IExpression expr : exprs) {
 			if (expr.sourceStart() < pos && expr.sourceEnd() >= pos) {
 				String s = new String(m_ctx.getOriginalSource(), expr
 						.sourceStart() + 1, pos - expr.sourceStart() - 1);
 				s = (s + "a").trim();
 				s = s.substring(0, s.length() - 1);
 				return s;
 			}
 		}
 		return "";
 	}
 
 	private String getCompositeTokenByPos(MessageSend node, String preToken) {
 		int pos = m_ctx.getCompletionPos();
 		StringBuffer buff = new StringBuffer();
 		buff.append(preToken);
 		char[] chars = m_ctx.getOriginalSource();
 		pos++;
 		while (pos < chars.length && Character.isJavaIdentifierPart(chars[pos])) {
 			buff.append(chars[pos]);
 			pos++;
 		}
 		return buff.toString();
 
 	}
 
 	private String getTokenByPos(MessageSend node) {
 
 		int pos = m_ctx.getCompletionPos();
 		int recEnd = node.getReceiver().sourceEnd() + 1;
 		if (recEnd < pos) {
 			String s = new String(m_ctx.getOriginalSource(), recEnd, pos
 					- recEnd).trim();
 			if (s.startsWith(".")) {
 				s = s.substring(1, s.length());
 			}
 			return s;
 
 		} else {
 			return "";
 		}
 
 	}
 
 	private boolean afterRecevier(MessageSend node) {
 		int pos = m_ctx.getCompletionPos();
 		if (node.getReceiver() == null) {
 			return false;
 		}
 		int recEnd = node.getReceiver().sourceEnd() + 1;
 		if (recEnd < pos) {
 			String s = new String(m_ctx.getOriginalSource(), recEnd, pos
 					- recEnd).trim();
 			if (s.startsWith(".")) {
 				s = s.substring(1, s.length());
 			}
 			if (s.equals("")) {
 				return true;
 			}
 			if (Character.isWhitespace(s.charAt(s.length() - 1))) {
 				// remove
 				return false;
 			}
 			for (int i = 0; i < s.length(); i++) {
 				if (!Character.isJavaIdentifierPart(s.charAt(i))) {
 					return false;
 				}
 			}
 			return true;
 
 		} else {
 			return false;
 		}
 	}
 
 	private JstSource createSource(String token) {
 		return createSource(m_ctx.getCompletionPos() - token.length(), m_ctx
 				.getCompletionPos(), m_ctx.getSourceUtil());
 	}
 
 	private boolean isInName(JstSource nameSource) {
 		int pos = m_ctx.getCompletionPos();
 		return nameSource.getStartOffSet() <= pos
 				&& nameSource.getEndOffSet() + 1 >= pos;
 	}
 
 	private String getNameToken(JstIdentifier mtdNameIdentifier) {
 		String name = mtdNameIdentifier.getName();
 		JstSource nameSource = mtdNameIdentifier.getSource();
 		return name.substring(0, m_ctx.getCompletionPos()
 				- nameSource.getStartOffSet());
 	}
 
 	private boolean isInBracket(int indexOpen, int indexClose,
 			MessageSend astNode) {
 
 		int pos = m_ctx.getCompletionPos();
 		boolean isInBracket = false;
 
 		if (indexOpen != -1) {
 			isInBracket = pos > indexOpen && pos <= indexClose;
 		}
 
 		if (indexOpen != -1 && indexClose != -1 && astNode.statementEnd != -1) {
 			isInBracket = pos > indexOpen && pos <= astNode.statementEnd;
 		}
 
 		return isInBracket;
 	}
 
 }
