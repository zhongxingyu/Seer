 /*******************************************************************************
  * Copyright (c) 2010 xored software, Inc.  
  *
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html  
  *
  * Contributors:
  *     xored software, Inc. - initial API and Implementation (Alex Panchenko)
  *******************************************************************************/
 
 package org.eclipse.dltk.internal.javascript.validation;
 
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.dltk.core.builder.IBuildContext;
 import org.eclipse.dltk.core.builder.IBuildParticipant;
 import org.eclipse.dltk.javascript.ast.AbstractNavigationVisitor;
 import org.eclipse.dltk.javascript.ast.BreakStatement;
import org.eclipse.dltk.javascript.ast.CaseClause;
 import org.eclipse.dltk.javascript.ast.CatchClause;
 import org.eclipse.dltk.javascript.ast.ContinueStatement;
import org.eclipse.dltk.javascript.ast.DefaultClause;
 import org.eclipse.dltk.javascript.ast.DoWhileStatement;
 import org.eclipse.dltk.javascript.ast.ForEachInStatement;
 import org.eclipse.dltk.javascript.ast.ForInStatement;
 import org.eclipse.dltk.javascript.ast.ForStatement;
 import org.eclipse.dltk.javascript.ast.FunctionStatement;
 import org.eclipse.dltk.javascript.ast.IfStatement;
 import org.eclipse.dltk.javascript.ast.ReturnStatement;
 import org.eclipse.dltk.javascript.ast.Script;
 import org.eclipse.dltk.javascript.ast.Statement;
 import org.eclipse.dltk.javascript.ast.StatementBlock;
import org.eclipse.dltk.javascript.ast.SwitchComponent;
 import org.eclipse.dltk.javascript.ast.SwitchStatement;
 import org.eclipse.dltk.javascript.ast.ThrowStatement;
 import org.eclipse.dltk.javascript.ast.TryStatement;
 import org.eclipse.dltk.javascript.ast.WhileStatement;
 import org.eclipse.dltk.javascript.core.JavaScriptProblems;
 import org.eclipse.dltk.javascript.parser.Reporter;
 import org.eclipse.osgi.util.NLS;
 
 public class FlowValidation extends AbstractNavigationVisitor<FlowStatus>
 		implements IBuildParticipant {
 
 	private Reporter reporter;
 	private FlowScope scope;
 
 	public void build(IBuildContext context) throws CoreException {
 		final Script script = JavaScriptValidations.parse(context);
 		if (script == null) {
 			return;
 		}
 		reporter = JavaScriptValidations.createReporter(context);
 		scope = new FlowScope();
 		visit(script);
 	}
 
 	@Override
 	public FlowStatus visitReturnStatement(ReturnStatement node) {
 		final FlowEndKind kind = node.getValue() != null ? FlowEndKind.RETURNS_VALUE
 				: FlowEndKind.RETURNS;
 		if (scope.add(kind) && scope.size() > 1) {
 			reporter.setMessage(JavaScriptProblems.RETURN_INCONSISTENT,
 					"return statement is inconsistent with previous usage");
 			reporter.setRange(node.sourceStart(), node.sourceEnd());
 			reporter.report();
 		}
 		final FlowStatus status = new FlowStatus();
 		if (node.getValue() == null) {
 			status.returnWithoutValue = true;
 		} else {
 			status.returnValue = true;
 		}
 		return status;
 	}
 
 	public FlowStatus visitThrowStatement(ThrowStatement node) {
 		if (node.getException() != null) {
 			visit(node.getException());
 		}
 		final FlowStatus status = new FlowStatus();
 		status.returnThrow = true;
 		return status;
 	}
 
 	@Override
 	public FlowStatus visitStatementBlock(StatementBlock node) {
 		FlowStatus status = new FlowStatus();
 		status.noReturn = true;
 		int startRange = Integer.MAX_VALUE;
 		int endRange = -1;
 		for (Statement statement : node.getStatements()) {
 			if (status.isTerminated()) {
 				if (startRange > statement.sourceStart())
 					startRange = statement.sourceStart();
 				if (endRange < statement.sourceEnd())
 					endRange = statement.sourceEnd();
 
 			} else {
 				status.add(visit(statement));
 			}
 		}
 		if (startRange != Integer.MAX_VALUE) {
 			reporter.setMessage(JavaScriptProblems.UNREACHABLE_CODE,
 					"unreachable code");
 			reporter.setRange(startRange, endRange);
 			reporter.report();
 		}
 		return status;
 	}
 
 	@Override
 	public FlowStatus visitIfStatement(IfStatement node) {
 
 		FlowStatus status = new FlowStatus();
 		status.noReturn = true;
 
 		if (node.getThenStatement() != null) {
 			FlowStatus thenFlow = visit(node.getThenStatement());
 			if (thenFlow != null) {
 				status.noReturn = thenFlow.noReturn;
 				status.returnValue = thenFlow.returnValue;
 				status.returnWithoutValue = thenFlow.returnWithoutValue;
 			}
 		}
 		if (node.getElseStatement() != null) {
 			FlowStatus elseFlow = visit(node.getElseStatement());
 			if (elseFlow != null) {
 				if (status.noReturn != elseFlow.noReturn)
 					status.noReturn = true;
 				if (status.returnValue != elseFlow.returnValue)
 					status.returnValue = true;
 				if (status.returnWithoutValue != elseFlow.returnWithoutValue)
 					status.returnWithoutValue = true;
 			} else {
 				status.noReturn = true;
 			}
 
 		} else {
 			status.noReturn = true;
 		}
 
 		return status;
 	}
 
 	private static FlowStatus clearBreak(final FlowStatus status) {
 		if (status != null) {
 			status.isBreak = false;
 		}
 		return status;
 	}
 
 	@Override
 	public FlowStatus visitForStatement(ForStatement node) {
 		return clearBreak(super.visitForStatement(node));
 	}
 
 	@Override
 	public FlowStatus visitForInStatement(ForInStatement node) {
 		return clearBreak(super.visitForInStatement(node));
 	}
 
 	@Override
 	public FlowStatus visitForEachInStatement(ForEachInStatement node) {
 		return clearBreak(super.visitForEachInStatement(node));
 	}
 
 	@Override
 	public FlowStatus visitWhileStatement(WhileStatement node) {
 		return clearBreak(super.visitWhileStatement(node));
 	}
 
 	@Override
 	public FlowStatus visitDoWhileStatement(DoWhileStatement node) {
 		return clearBreak(super.visitDoWhileStatement(node));
 	}
 
 	@Override
 	public FlowStatus visitFunctionStatement(FunctionStatement node) {
 		final FlowScope savedScope = scope;
 		scope = new FlowScope();
 		try {
 			final FlowStatus result = super.visitFunctionStatement(node);
 			if (scope.contains(FlowEndKind.RETURNS_VALUE)
 					&& (scope.contains(FlowEndKind.RETURNS) || result.noReturn)) {
 				reporter.setMessage(
 						JavaScriptProblems.FUNCTION_NOT_ALWAYS_RETURN_VALUE,
 						node.getName() != null ? NLS.bind(
 								"function {0} does not always return a value",
 								node.getName().getName())
 								: "anonymous function does not always return a value");
 				reporter.setRange(node.getBody().getRC(), node.getBody()
 						.getRC() + 1);
 				reporter.report();
 			}
 			return result;
 		} finally {
 			scope = savedScope;
 		}
 	}
 
 	@Override
 	public FlowStatus visitBreakStatement(BreakStatement node) {
 		final FlowStatus status = new FlowStatus();
 		status.isBreak = true;
 		return status;
 	}
 
 	@Override
 	public FlowStatus visitContinueStatement(ContinueStatement node) {
 		final FlowStatus status = new FlowStatus();
 		status.isBreak = true;
 		return status;
 	}
 
 	@Override
 	public FlowStatus visitTryStatement(TryStatement node) {
 		final FlowStatus status = new FlowStatus();
 		final FlowStatus body = visit(node.getBody());
 		status.add(body);
 		if (!node.getCatches().isEmpty()) {
 			status.returnThrow = false;
 		}
 		for (CatchClause catchClause : node.getCatches()) {
 			final Statement catchStatement = catchClause.getStatement();
 			if (catchStatement != null) {
 				final FlowStatus c = visit(catchStatement);
 				if (!c.isReturned()) {
 					status.add(c);
 				}
 			}
 		}
 		if (node.getFinally() != null) {
 			final Statement finallyStatement = node.getFinally().getStatement();
 			if (finallyStatement != null) {
 				final FlowStatus f = visit(finallyStatement);
 				if (f.isReturned()) {
 					status.add(f);
 				}
 			}
 		}
 		return status;
 	}
 
 	@Override
 	public FlowStatus visitSwitchStatement(SwitchStatement node) {
		final FlowStatus status = new FlowStatus();
		boolean defaultClause = false;
		boolean noReturnValueInCase = false;
		if (node.getCondition() != null)
			visit(node.getCondition());
		for (SwitchComponent component : node.getCaseClauses()) {
			if (component instanceof CaseClause) {
				final CaseClause caseClause = (CaseClause) component;
				if (caseClause.getCondition() != null) {
					visit(caseClause.getCondition());
				}
			}
			defaultClause = component instanceof DefaultClause;
			for (Statement statement : component.getStatements()) {
				status.add(visit(statement));
				if (status.isReturned())
					break;
			}
			noReturnValueInCase = noReturnValueInCase || status.noReturn;
		}
		if ((status.isReturned() && !defaultClause) || noReturnValueInCase)
			status.noReturn = true;
		return status;
 	}
 }
