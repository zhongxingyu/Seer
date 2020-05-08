 /*******************************************************************************
  * Copyright (c) 2005, 2012 eBay Inc.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  *******************************************************************************/
 package org.eclipse.vjet.dsf.jstojava.translator.robust.ast2jst;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.eclipse.vjet.dsf.jsgen.shared.ids.ScopeIds;
 import org.eclipse.vjet.dsf.jsgen.shared.validation.common.ScopeId;
 import org.eclipse.vjet.dsf.jst.IJstNode;
 import org.eclipse.vjet.dsf.jst.term.NV;
 import org.eclipse.vjet.dsf.jst.term.ObjLiteral;
 import org.eclipse.vjet.dsf.jstojava.translator.TranslateHelper;
 import org.eclipse.vjet.dsf.jstojava.translator.robust.completion.JstCompletion;
 import org.eclipse.vjet.dsf.jstojava.translator.robust.completion.JstFieldOrMethodCompletion;
 import org.eclipse.mod.wst.jsdt.core.ast.IASTNode;
 import org.eclipse.mod.wst.jsdt.core.ast.IObjectLiteralField;
 import org.eclipse.mod.wst.jsdt.core.compiler.CategorizedProblem;
 import org.eclipse.mod.wst.jsdt.internal.compiler.ast.ObjectLiteral;
 import org.eclipse.mod.wst.jsdt.internal.compiler.ast.ObjectLiteralField;
 
 public class ObjectLiteralTranslator extends
 		BaseAst2JstTranslator<ObjectLiteral, IJstNode> {
 
 	private static List<Character> CONTROL_SYMBOLS = new ArrayList<Character>();
 
 	static {
 		CONTROL_SYMBOLS.add(',');
 		CONTROL_SYMBOLS.add(':');
 		CONTROL_SYMBOLS.add(';');		
 	}
 
 	private ObjLiteral m_result;
 	
 	@Override
 	protected IJstNode doTranslate(ObjectLiteral astObjectLiteral) {
 
 		ObjLiteral result = null;
 		if(m_ctx.isSkipJsExtSyntaxArgs()){
 			result = new ObjLiteral();
 			
 		}
 		
 		if (astObjectLiteral.fields != null) {
 			result = processObjectLiteral(astObjectLiteral);
 		} else {
 			result = new ObjLiteral();
 			result.setSource(TranslateHelper.getSource(astObjectLiteral,m_ctx.getSourceUtil()));
 		}
 		ScopeId scopeId = m_ctx.getCurrentScope();
 		if (scopeId == ScopeIds.PROPS
 				|| scopeId == ScopeIds.PROTOS
 				|| scopeId == ScopeIds.VALUES
 //				|| scopeId == ScopeIds.DEFS
 				) {
 
 			return null;
 		}
 
 		m_result = result;
 		return result;
 	}
 
 	 ObjLiteral processObjectLiteral(ObjectLiteral astObjectLiteral) {
 		List<ObjectLiteralField> astFields = new ArrayList<ObjectLiteralField>(
 				astObjectLiteral.fields.length);
 		ObjLiteral result = new ObjLiteral();
 		TranslateHelper.addSourceInfo(astObjectLiteral, result, m_ctx.getSourceUtil());
 		for (int i = 0; i < astObjectLiteral.fields.length; i++) {
 			astFields.add(astObjectLiteral.fields[i]);
 		}
 		int len = astFields.size();
 		for (int i = 0; i < len; i++) {
 			// Getting next node startOffset for comment parsing purpose
 			int nextNodeStart;
 			if (i + 1 < astFields.size()) {
 				nextNodeStart = astFields.get(i + 1).sourceStart();
 			} else {
 				nextNodeStart = astObjectLiteral.sourceEnd();
 			}
 			m_ctx.setNextNodeSourceStart(nextNodeStart);
 			if (i == 0) {
 				m_ctx.setPreviousNodeSourceEnd(astObjectLiteral.sourceStart());
 			} else {
 				m_ctx.setPreviousNodeSourceEnd(astFields.get(i - 1).sourceEnd());
 			}
 			ObjectLiteralField astObjectLiteralField = astFields.get(i);
 			BaseAst2JstTranslator translator = getTranslator(astObjectLiteralField);
 			//translator.setParent(m_parent);
 			IJstNode jstNode = (IJstNode) translator
 					.translate(astObjectLiteralField);
 
 			int sourceEnd = jstNode != null && jstNode.getSource()!=null ? jstNode.getSource()
					.getEndOffSet() : astObjectLiteralField.fieldName.sourceEnd;
			int sourceStart = jstNode != null && jstNode.getSource()!=null ? jstNode.getSource()
							.getStartOffSet() : astObjectLiteralField.sourceStart;		
 			m_ctx.setPreviousNodeSourceEnd(sourceEnd);
 
 			if (jstNode instanceof NV) {
				if(result.getNV(((NV) jstNode).getName())==null){
					result.add((NV) jstNode);
				}else{
					m_ctx.getErrorReporter().error("Duplicate literal field: " +((NV)jstNode).getName(), 
							m_ctx.getCurrentType()!=null? m_ctx.getCurrentType().getName():null,
							sourceStart,
							sourceEnd, 
							m_ctx.getLineInfoProvider().line(sourceStart),
							m_ctx.getLineInfoProvider().line(sourceEnd));
				}
 			}
 
 			// add restored ast object literal fields to list and process
 			List<IASTNode> recoveredNodes = translator.getRecoveredNodes();
 			int added = 0;
 			for (IASTNode node : recoveredNodes) {
 				if (node instanceof ObjectLiteralField) {
 					astFields.add(i + 1 + added, (ObjectLiteralField) node);
 					added++;
 				}
 			}
 		}
 		if (len > 0) {
 			m_ctx.setPreviousNodeSourceEnd(astObjectLiteral.sourceEnd);
 		}
 		return result;
 	}
 
 	@Override
 	protected JstCompletion createCompletion(ObjectLiteral astNode,
 			boolean isAfterSource) {
 
 		JstCompletion completion = null;
 
 		CategorizedProblem problem = findProblem();
 		if (problem != null) {
 
 			int problemStart = problem.getSourceStart();
 			char[] source = m_ctx.getOriginalSource();
 			String prefix = new String(source, problemStart, m_ctx
 					.getCompletionPos()
 					- problemStart);
 			String wholeError = new String(source, problemStart, problem
 					.getSourceEnd()
 					+ 1 - problemStart);
 			if (prefix.length() == 0) {
 				// prefix = getErrorPrefix(source,m_ctx.getCompletionPos());
 				// wholeError = prefix;
 			}
 			if (prefix.length() > 0 && isJavaIdentifier(prefix)) {
 				completion = new JstFieldOrMethodCompletion(m_result, isStaticBlock());
 				// TODO check if completion end is OK
 				completion.setSource(createSource(problemStart, problem
 						.getSourceEnd() + 1, m_ctx.getSourceUtil()));
 				completion.setToken(prefix);
 				completion.setCompositeToken(wholeError);
 				// stop search for completions
 				m_ctx.setCreatedCompletion(true);
 			}
 
 		}
 
 		if (!isAfterSource && completion == null) {
 			if(inFieldArea(astNode)) {
 				return null;
 			}
 			// BMS 2012-05-10 Fix proposal problem when there's no token and the cursor is against the comma
 			String token = "";
 			// If butted up against a comma, this is OK
 			int index = m_ctx.getCompletionPos() - 1;
 			char c = m_ctx.getOriginalSource()[index];
 			if (c != ',')
 			{
 				int lastFieldPos = getLastFieldPos(astNode);
 				//String token = getToken(lastFieldPos);
 				token = getToken(lastFieldPos);
 
 				//Has no token, or syntax error, return null JstCompletion.
 				if (token == null || token.indexOf("(") >= 0) {
 					return null;
 				}
 			}
 		//	int lastFieldPos = getLastFieldPos(astNode);
 		//	String token = getToken(lastFieldPos);
 			//Has no token, or syntax error, return null JstCompletion.
 //			if (token == null || token.indexOf("(") >= 0) {
 //				return null;
 //			}
 			completion = new JstFieldOrMethodCompletion(m_result,
 					isStaticBlock());
 			if (token.trim().length() == 0) {
 				completion.setCompositeToken(token.trim());
 			} else {
 				completion.setCompositeToken(token);
 			}
 			completion.setToken(token.trim());
 			int startSource = m_ctx.getCompletionPos() - token.length();
 			int endSource = startSource + token.length();
 			completion.setSource(createSource(startSource, endSource, m_ctx.getSourceUtil()));
 			// completion.setCompositeToken("");
 
 			// stop search for completions
 			// m_ctx.setCompletionPos(-1);
 			m_ctx.setCreatedCompletion(true);
 		}
 		return completion;
 	}
 
 	private boolean inFieldArea(ObjectLiteral astNode) {
 
 		int cursorPos = m_ctx.getCompletionPos() - 1;
 		if (astNode.getFields() == null) {
 			return false;
 		}
 		for(IObjectLiteralField field : astNode.getFields()) {
 			if (field.sourceStart() < cursorPos && field.sourceEnd() >= cursorPos) {
 				return true;
 			}
 		}
 		return false;
 	}
 
 	private boolean isStaticBlock() {
 		return ScopeIds.PROPS == m_ctx.getCurrentScope();
 	}
 
 	/**
 	 * During token calculation, the result token should not cover the pre ObjetLiteralField.
 	 * or it is not a valid token, or there is some syntax error, so give up completion
 	 * @param lastFieldPos
 	 * @return
 	 */
 	private String	 getToken(int lastFieldPos) {
 		
 		int index = m_ctx.getCompletionPos() - 1;
 		StringBuffer buffer = new StringBuffer();
 		
 		boolean started = false;
 		
 		for (int i = index; i >= 0; i--) {
 			
 			char c = m_ctx.getOriginalSource()[i];
 			
 			if (c=='{' || c=='}' || c=='\r' || c=='\n'){
 				break;
 			}
 			//Jack: to check if the index has covered the pre field, if it is, should 
 			if (i <= lastFieldPos) {
 				return null;
 			}
 			if (CONTROL_SYMBOLS.contains(c)) {
 				if (started) {
 					break;
 				}
 			} else {
 				started = true;
 			}
 
 			if (started) {
 				buffer.insert(0, c);
 			}
 		}
 		
 		return buffer.toString();
 	}
 	
 	
 	
 	private int getLastFieldPos(ObjectLiteral astNode) {
 		int cursorPos = m_ctx.getCompletionPos() - 1;
 		int lastPos = 0;
 		if (astNode.getFields() == null) {
 			return lastPos;
 		}
 		for(IObjectLiteralField field : astNode.getFields()) {
 			int temp = field.sourceEnd();
 			if (temp < cursorPos && temp > lastPos) {
 				lastPos = temp;
 			}
 		}
 		return lastPos;
 	}
 
 	private CategorizedProblem findProblem() {
 		CategorizedProblem problemAtCursor = null;
 		CategorizedProblem[] problems = m_ctx.getAST().compilationResult()
 				.getAllProblems();
 		if (problems != null) {
 			int completionPos = m_ctx.getCompletionPos();
 			for (CategorizedProblem problem : problems) {
 				if (problem.getSourceStart() <= completionPos
 						&& completionPos <= problem.getSourceEnd() + 1) {
 					problemAtCursor = problem;
 					break;
 				}
 			}
 		}
 		return problemAtCursor;
 	}
 }
