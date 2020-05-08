 /*******************************************************************************
  * Copyright (c) 2005, 2012 eBay Inc.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  *******************************************************************************/
 package org.eclipse.vjet.dsf.jstojava.translator.robust.ast2jst;
 
 import org.eclipse.vjet.dsf.jsgen.shared.ids.ScopeIds;
 import org.eclipse.vjet.dsf.jst.declaration.JstBlock;
import org.eclipse.vjet.dsf.jst.expr.ConditionalExpr;
 import org.eclipse.vjet.dsf.jst.stmt.WhileStmt;
 import org.eclipse.vjet.dsf.jst.token.IExpr;
 import org.eclipse.vjet.dsf.jst.token.IStmt;
 import org.eclipse.vjet.dsf.jstojava.translator.TranslateHelper;
 import org.eclipse.mod.wst.jsdt.internal.compiler.ast.WhileStatement;
 
 public class WhileStatementTranslator extends
 		BaseAst2JstTranslator<WhileStatement, WhileStmt> {
 
 	@Override
 	protected WhileStmt doTranslate(WhileStatement statement) {
 		WhileStmt whileStmt = new WhileStmt();
 		if (m_parent != null){
 			m_parent.addChild(whileStmt);
 		}
 		try {
 			m_ctx.enterBlock(ScopeIds.LOOP);
 			doTranslate(statement, whileStmt);
 		} finally {
 			m_ctx.exitBlock();
 		}
 		return whileStmt;
 	}
 
 	private void doTranslate(WhileStatement statement, WhileStmt whileStmt) {
 		if (statement.condition != null) {
 			IExpr cond = (IExpr) getTranslatorAndTranslate(statement.condition,
 					whileStmt);
 			whileStmt.setCondition(TranslateHelper.buildCondition(cond));
 		}
 		if (!statement.isEmptyBlock()) {
 			Object obj = getTranslatorAndTranslate(statement.action, whileStmt.getBody());
 			if(!(obj instanceof JstBlock)){
				if(obj instanceof IStmt){
					whileStmt.getBody().addStmt((IStmt)obj);
				}else if(obj instanceof ConditionalExpr){
					// TODO test this case
					whileStmt.getBody().addChild((ConditionalExpr)obj);
					
				}
				// TODO what to do when  org.eclipse.vjet.dsf.jst.expr.ConditionalExpr
 			}
 		}
 		whileStmt.setSource(TranslateHelper.getSource(statement, m_ctx.getSourceUtil()));
 	}
 
 }
