 /*******************************************************************************
  * Copyright (c) 2008 xored software, Inc.
  *
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     xored software, Inc. - initial API and Implementation (Alex Panchenko)
  *******************************************************************************/
 package org.eclipse.dltk.ruby.internal.ui.text;
 
 import java.util.Stack;
 
 import org.eclipse.dltk.ast.ASTNode;
 import org.eclipse.dltk.ast.declarations.Declaration;
 import org.eclipse.dltk.ast.expressions.BigNumericLiteral;
 import org.eclipse.dltk.ast.expressions.CallExpression;
 import org.eclipse.dltk.ast.expressions.FloatNumericLiteral;
 import org.eclipse.dltk.ast.expressions.NumericLiteral;
 import org.eclipse.dltk.ast.expressions.StringLiteral;
 import org.eclipse.dltk.ast.references.SimpleReference;
 import org.eclipse.dltk.ast.references.VariableReference;
 import org.eclipse.dltk.core.ISourceModule;
 import org.eclipse.dltk.core.ModelException;
 import org.eclipse.dltk.internal.ui.editor.semantic.highlighting.SemanticUpdateWorker;
 import org.eclipse.dltk.ruby.ast.RubyConstantDeclaration;
 import org.eclipse.dltk.ruby.ast.RubyDRegexpExpression;
 import org.eclipse.dltk.ruby.ast.RubyDynamicStringExpression;
 import org.eclipse.dltk.ruby.ast.RubyEvaluatableStringExpression;
 import org.eclipse.dltk.ruby.ast.RubyRegexpExpression;
 import org.eclipse.dltk.ruby.ast.RubySymbolReference;
 import org.eclipse.dltk.ruby.core.utils.RubySyntaxUtils;
 
 public class RubySemanticUpdateWorker extends SemanticUpdateWorker {
 
 	private static final int HL_REGEXP = 0;
 	private static final int HL_STRING = 1;
 	private static final int HL_SYMBOL = 2;
 	private static final int HL_LOCAL_VARIABLE = 3;
 	private static final int HL_INSTANCE_VARIABLE = 4;
 	private static final int HL_CLASS_VARIABLE = 5;
 	private static final int HL_GLOBAL_VARIABLE = 6;
 	private static final int HL_CONST = 7;
 	private static final int HL_NUMBER = 8;
 	private static final int HL_EVAL_EXPR = 9;
 	private static final int HL_DEFAULT = 10;
 
 	private final char[] content;
 
 	/**
 	 * @param presenter
 	 * @param highlightings
 	 * @param sourceModule
 	 * @throws ModelException
 	 */
 	public RubySemanticUpdateWorker(ISourceModule sourceModule)
 			throws ModelException {
 		this.content = sourceModule.getSourceAsCharArray();
 	}
 
 	private static final boolean ACTIVE = true;
 
 	public boolean visitGeneral(ASTNode node) throws Exception {
 		if (!ACTIVE) {
 			return true;
 		}
 		if (node instanceof RubyRegexpExpression
 				|| node instanceof RubyDRegexpExpression) {
 			handleRegexp(node);
 		} else if (node instanceof RubySymbolReference) {
 			addHighlightedPosition(node.sourceStart(), node.sourceEnd(),
 					HL_SYMBOL);
 		} else if (node instanceof VariableReference) {
 			handleVariableReference((VariableReference) node);
 		} else if (node instanceof StringLiteral) {
 			if (isStringLiteralNeeded(node)) {
 				addHighlightedPosition(node.sourceStart(), node.sourceEnd(),
 						HL_STRING);
 			}
 		} else if (node instanceof NumericLiteral
 				|| node instanceof FloatNumericLiteral
 				|| node instanceof BigNumericLiteral) {
 			addHighlightedPosition(node.sourceStart(), node.sourceEnd(),
 					HL_NUMBER);
 		} else if (node instanceof RubyEvaluatableStringExpression) {
 			handleEvaluatableExpression(node);
 		} else if (node instanceof CallExpression) {
 			final CallExpression call = (CallExpression) node;
			if (!RubySyntaxUtils.isRubyOperator(call.getName())) {
				final SimpleReference callName = call.getCallName();
				if (callName.sourceStart() >= 0
						&& callName.sourceEnd() > callName.sourceStart()) {
					addHighlightedPosition(call.sourceStart(),
							call.sourceEnd(), HL_DEFAULT);
				}
 			}
 		} else if (node instanceof Declaration) {
 			final Declaration declaration = (Declaration) node;
 			addHighlightedPosition(declaration.getNameStart(), declaration
 					.getNameEnd(), HL_DEFAULT);
 		} else if (node instanceof RubyConstantDeclaration) {
 			final RubyConstantDeclaration declaration = (RubyConstantDeclaration) node;
 			final SimpleReference name = declaration.getName();
 			addHighlightedPosition(name.sourceStart(), name.sourceEnd(),
 					HL_CONST);
 		}
 		stack.push(node);
 		return true;
 	}
 
 	private boolean isStringLiteralNeeded(ASTNode node) {
 		if (stack.empty()) {
 			return true;
 		}
 		final ASTNode top = (ASTNode) stack.peek();
 		if (top instanceof RubyDRegexpExpression) {
 			return false;
 		}
 		if (top instanceof RubyDynamicStringExpression) {
 			return node.sourceStart() >= top.sourceStart()
 					&& node.sourceEnd() <= top.sourceEnd();
 		}
 		return true;
 	}
 
 	public void endvisitGeneral(ASTNode node) throws Exception {
 		stack.pop();
 	}
 
 	private final Stack stack = new Stack();
 
 	private void handleVariableReference(VariableReference ref) {
 		final String varName = ref.getName();
 		if (varName.length() != 0) {
 			if (varName.charAt(0) == '$') {
 				addHighlightedPosition(ref.sourceStart(), ref.sourceEnd(),
 						HL_GLOBAL_VARIABLE);
 			} else if (varName.charAt(0) == '@') {
 				if (varName.length() > 2 && varName.charAt(1) == '@') {
 					addHighlightedPosition(ref.sourceStart(), ref.sourceEnd(),
 							HL_CLASS_VARIABLE);
 				} else {
 					addHighlightedPosition(ref.sourceStart(), ref.sourceEnd(),
 							HL_INSTANCE_VARIABLE);
 				}
 			} else {
 				addHighlightedPosition(ref.sourceStart(), ref.sourceEnd(),
 						HL_LOCAL_VARIABLE);
 			}
 		}
 	}
 
 	private void handleEvaluatableExpression(ASTNode node) {
 		int start = node.sourceStart();
 		int end = node.sourceEnd();
 		if (content[start] == '#' && content[start + 1] == '{') {
 			if (content[end - 1] == '\r') {
 				// FIXME JRuby bug
 				--end;
 			}
 			if (content[end - 1] == '}') {
 				addHighlightedPosition(start, start + 2, HL_EVAL_EXPR);
 				addHighlightedPosition(end - 1, end - 0, HL_EVAL_EXPR);
 			}
 		}
 	}
 
 	private void handleRegexp(ASTNode node) {
 		int start = node.sourceStart();
 		int end = node.sourceEnd();
 		if (start >= 1 && content[start - 1] == '/') {
 			--start;
 			if (end < content.length && content[end] == '/') {
 				++end;
 			}
 			while (end < content.length
 					&& RubySyntaxUtils.isValidRegexpModifier(content[end])) {
 				++end;
 			}
 		} else if (start >= 3 && content[start - 3] == '%'
 				&& content[start - 2] == 'r') {
 			char terminator = RubySyntaxUtils
 					.getPercentStringTerminator(content[start - 1]);
 			if (terminator != 0 && end < content.length
 					&& content[end] == terminator) {
 				start -= 3;
 				++end;
 				while (end < content.length
 						&& RubySyntaxUtils.isValidRegexpModifier(content[end])) {
 					++end;
 				}
 			}
 		}
 		addHighlightedPosition(start, end, HL_REGEXP);
 	}
 
 }
