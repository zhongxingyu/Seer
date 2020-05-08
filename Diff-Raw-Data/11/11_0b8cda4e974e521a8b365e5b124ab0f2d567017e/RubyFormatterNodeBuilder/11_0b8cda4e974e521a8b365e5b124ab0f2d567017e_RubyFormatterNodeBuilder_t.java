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
 package org.eclipse.dltk.ruby.formatter.internal;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.Iterator;
 import java.util.List;
 
 import org.eclipse.dltk.formatter.nodes.AbstractFormatterNodeBuilder;
 import org.eclipse.dltk.formatter.nodes.FormatterRootNode;
 import org.eclipse.dltk.formatter.nodes.IFormatterContainerNode;
 import org.eclipse.dltk.formatter.nodes.IFormatterDocument;
 import org.eclipse.dltk.formatter.nodes.IFormatterTextNode;
 import org.eclipse.dltk.ruby.formatter.internal.nodes.FormatterAtBeginNode;
 import org.eclipse.dltk.ruby.formatter.internal.nodes.FormatterAtEndNode;
 import org.eclipse.dltk.ruby.formatter.internal.nodes.FormatterBeginNode;
 import org.eclipse.dltk.ruby.formatter.internal.nodes.FormatterCaseNode;
 import org.eclipse.dltk.ruby.formatter.internal.nodes.FormatterClassNode;
 import org.eclipse.dltk.ruby.formatter.internal.nodes.FormatterDoNode;
 import org.eclipse.dltk.ruby.formatter.internal.nodes.FormatterElseIfNode;
 import org.eclipse.dltk.ruby.formatter.internal.nodes.FormatterEnsureNode;
 import org.eclipse.dltk.ruby.formatter.internal.nodes.FormatterForNode;
 import org.eclipse.dltk.ruby.formatter.internal.nodes.FormatterHereDocNode;
 import org.eclipse.dltk.ruby.formatter.internal.nodes.FormatterIfElseNode;
 import org.eclipse.dltk.ruby.formatter.internal.nodes.FormatterIfEndNode;
 import org.eclipse.dltk.ruby.formatter.internal.nodes.FormatterIfNode;
 import org.eclipse.dltk.ruby.formatter.internal.nodes.FormatterMethodNode;
 import org.eclipse.dltk.ruby.formatter.internal.nodes.FormatterModifierNode;
 import org.eclipse.dltk.ruby.formatter.internal.nodes.FormatterRDocNode;
 import org.eclipse.dltk.ruby.formatter.internal.nodes.FormatterRescueElseNode;
 import org.eclipse.dltk.ruby.formatter.internal.nodes.FormatterRescueNode;
 import org.eclipse.dltk.ruby.formatter.internal.nodes.FormatterStringNode;
 import org.eclipse.dltk.ruby.formatter.internal.nodes.FormatterUntilNode;
 import org.eclipse.dltk.ruby.formatter.internal.nodes.FormatterWhenElseNode;
 import org.eclipse.dltk.ruby.formatter.internal.nodes.FormatterWhenNode;
 import org.eclipse.dltk.ruby.formatter.internal.nodes.FormatterWhileNode;
 import org.eclipse.jface.text.IRegion;
 import org.eclipse.jface.text.Region;
 import org.eclipse.osgi.util.NLS;
 import org.jruby.ast.ArgumentNode;
 import org.jruby.ast.BeginNode;
 import org.jruby.ast.CaseNode;
 import org.jruby.ast.ClassNode;
 import org.jruby.ast.CommentNode;
 import org.jruby.ast.DRegexpNode;
 import org.jruby.ast.DStrNode;
 import org.jruby.ast.DefnNode;
 import org.jruby.ast.DefsNode;
 import org.jruby.ast.EnsureNode;
 import org.jruby.ast.ForNode;
 import org.jruby.ast.IfNode;
 import org.jruby.ast.IterNode;
 import org.jruby.ast.ListNode;
 import org.jruby.ast.MethodDefNode;
 import org.jruby.ast.ModuleNode;
 import org.jruby.ast.Node;
 import org.jruby.ast.PostExeNode;
 import org.jruby.ast.RegexpNode;
 import org.jruby.ast.RescueBodyNode;
 import org.jruby.ast.RescueNode;
 import org.jruby.ast.SClassNode;
 import org.jruby.ast.StrNode;
 import org.jruby.ast.UntilNode;
 import org.jruby.ast.WhenNode;
 import org.jruby.ast.WhileNode;
 import org.jruby.ast.XStrNode;
 import org.jruby.ast.ext.ElseNode;
 import org.jruby.ast.ext.HeredocNode;
 import org.jruby.ast.ext.PreExeNode;
 import org.jruby.ast.visitor.AbstractVisitor;
 import org.jruby.evaluator.Instruction;
 import org.jruby.lexer.yacc.ISourcePosition;
 import org.jruby.lexer.yacc.ISourcePositionHolder;
 import org.jruby.parser.RubyParserResult;
 
 public class RubyFormatterNodeBuilder extends AbstractFormatterNodeBuilder {
 
 	public IFormatterContainerNode build(RubyParserResult result,
 			final IFormatterDocument document) {
 		final IFormatterContainerNode root = new FormatterRootNode(document);
 		start(root);
 		result.getAST().accept(new AbstractVisitor() {
 
 			protected Instruction visitNode(Node visited) {
 				visitChildren(visited);
 				return null;
 			}
 
 			public Instruction visitClassNode(ClassNode visited) {
 				FormatterClassNode classNode = new FormatterClassNode(document);
 				classNode.setBegin(createTextNode(document, visited
 						.getClassKeyword()));
 				push(classNode);
 				visitChildren(visited);
 				checkedPop(classNode, visited.getEnd().getPosition()
 						.getStartOffset());
 				classNode.setEnd(createTextNode(document, visited.getEnd()));
 				return null;
 			}
 
 			public Instruction visitSClassNode(SClassNode visited) {
 				FormatterClassNode classNode = new FormatterClassNode(document);
 				classNode.setBegin(createTextNode(document, visited
 						.getClassKeyword()));
 				push(classNode);
 				visitChildren(visited);
 				checkedPop(classNode, visited.getEnd().getPosition()
 						.getStartOffset());
 				classNode.setEnd(createTextNode(document, visited.getEnd()));
 				return null;
 			}
 
 			public Instruction visitModuleNode(ModuleNode visited) {
 				FormatterClassNode moduleNode = new FormatterClassNode(document);
 				moduleNode.setBegin(createTextNode(document, visited
 						.getKeyword()));
 				push(moduleNode);
 				visitChildren(visited);
 				checkedPop(moduleNode, visited.getEnd().getPosition()
 						.getStartOffset());
 				moduleNode.setEnd(createTextNode(document, visited.getEnd()));
 				return null;
 			}
 
 			public Instruction visitDefnNode(DefnNode visited) {
 				return visitMethodDefNode(visited);
 			}
 
 			public Instruction visitDefsNode(DefsNode visited) {
 				return visitMethodDefNode(visited);
 			}
 
 			private Instruction visitMethodDefNode(MethodDefNode visited) {
 				FormatterMethodNode methodNode = new FormatterMethodNode(
 						document);
 				methodNode
 						.setBegin(createTextNode(document, visited
 								.getStartOffset(), visited.getNameNode()
 								.getEndOffset()));
 				push(methodNode);
 				visitChildren(visited);
 				checkedPop(methodNode, visited.getEnd().getPosition()
 						.getStartOffset());
 				methodNode.setEnd(createTextNode(document, visited.getEnd()));
 				return null;
 			}
 
 			public Instruction visitWhileNode(WhileNode visited) {
 				if (!visited.isBlock()) {
 					visitChildren(visited);
 					return null;
 				}
 				FormatterWhileNode whileNode = new FormatterWhileNode(document);
 				whileNode.setBegin(createTextNode(document, visited
 						.getKeyword()));
 				push(whileNode);
 				visitChildren(visited);
 				checkedPop(whileNode, visited.getEnd().getPosition()
 						.getStartOffset());
 				whileNode.setEnd(createTextNode(document, visited.getEnd()));
 				return null;
 			}
 
 			public Instruction visitIterNode(IterNode visited) {
 				FormatterDoNode forNode = new FormatterDoNode(document);
 				forNode.setBegin(createTextNode(document, visited.getBegin()));
 				push(forNode);
 				visitChildren(visited);
 				checkedPop(forNode, visited.getEnd().getPosition()
 						.getStartOffset());
 				forNode.setEnd(createTextNode(document, visited.getEnd()));
 				return null;
 			}
 
 			public Instruction visitForNode(ForNode visited) {
 				FormatterForNode forNode = new FormatterForNode(document);
 				forNode.setBegin(createTextNode(document, visited.getBegin()));
 				push(forNode);
 				visitChildren(visited);
 				checkedPop(forNode, visited.getEnd().getPosition()
 						.getStartOffset());
 				forNode.setEnd(createTextNode(document, visited.getEnd()));
 				return null;
 			}
 
 			public Instruction visitUntilNode(UntilNode visited) {
 				if (!visited.isBlock()) {
 					visitChild(visited.getBodyNode());
 					FormatterModifierNode block = new FormatterModifierNode(
 							document);
 					block.addChild(createTextNode(document, visited
 							.getKeyword()));
 					push(block);
 					visitChild(visited.getConditionNode());
 					checkedPop(block, visited.getConditionNode().getEndOffset());
 					return null;
 				}
 				FormatterUntilNode untilNode = new FormatterUntilNode(document);
 				untilNode.setBegin(createTextNode(document, visited
 						.getKeyword()));
 				push(untilNode);
 				visitChildren(visited);
 				checkedPop(untilNode, visited.getEnd().getPosition()
 						.getStartOffset());
 				untilNode.setEnd(createTextNode(document, visited.getEnd()));
 				return null;
 			}
 
 			public Instruction visitCaseNode(CaseNode visited) {
 				FormatterCaseNode caseNode = new FormatterCaseNode(document);
				final int caseEnd = visited.getCaseKeyword().getPosition()
						.getEndOffset();
 				caseNode.setBegin(createTextNode(document, visited
 						.getStartOffset(), caseEnd));
 				push(caseNode);
 				Node branch = visited.getFirstWhenNode();
 				while (branch != null) {
 					if (branch instanceof WhenNode) {
 						WhenNode whenBranch = (WhenNode) branch;
 						FormatterWhenNode whenNode = new FormatterWhenNode(
 								document);
 						whenNode.setBegin(createTextNode(document, branch
 								.getStartOffset(), whenBranch
 								.getExpressionNodes().getEndOffset()));
 						push(whenNode);
 						visitChild(whenBranch.getBodyNode());
 						branch = ((WhenNode) branch).getNextCase();
 						checkedPop(whenNode, branch != null ? branch
 								.getStartOffset() : visited.getEnd()
 								.getPosition().getStartOffset());
 					} else if (branch instanceof ElseNode) {
 						ElseNode elseBranch = (ElseNode) branch;
 						FormatterWhenElseNode whenElseNode = new FormatterWhenElseNode(
 								document);
 						whenElseNode.setBegin(createTextNode(document,
 								elseBranch.getStartOffset(), elseBranch
 										.getElseKeyword().getPosition()
 										.getEndOffset()));
 						push(whenElseNode);
 						visitChild(elseBranch.getStatement());
 						checkedPop(whenElseNode, visited.getEnd().getPosition()
 								.getStartOffset());
 						branch = null;
 					} else {
 						RubyFormatterPlugin.warn(NLS.bind(
 								"Unexpected {0} class in case/when expression",
 								branch.getClass().getName()),
 								new DumpStackOnly());
 						break;
 					}
 				}
 				checkedPop(caseNode, visited.getEnd().getPosition()
 						.getStartOffset());
 				caseNode.setEnd(createTextNode(document, visited.getEnd()));
 				return null;
 			}
 
 			public Instruction visitCommentNode(CommentNode visited) {
 				FormatterRDocNode commentNode = new FormatterRDocNode(document,
 						visited.getStartOffset(), visited.getEndOffset());
 				addChild(commentNode);
 				return null;
 			}
 
 			public Instruction visitIfNode(IfNode visited) {
 				if (visited.isInline()) {
 					List children = new ArrayList(3);
 					if (visited.getThenBody() != null) {
 						children.add(visited.getThenBody());
 					}
 					if (visited.getElseBody() != null) {
 						children.add(visited.getElseBody());
 					}
 					if (visited.getCondition() != null) {
 						children.add(visited.getCondition());
 					}
 					if (!children.isEmpty()) {
 						Collections.sort(children, POSITION_COMPARATOR);
 						visitChildren(children);
 					}
 					return null;
 				}
 				FormatterIfNode ifNode = new FormatterIfNode(document);
 				ifNode.setBegin(createTextNode(document, visited
 						.getStartOffset(), visited.getCondition()
 						.getEndOffset()));
 				push(ifNode);
 				visitChild(visited.getFirstBody());
 				checkedPop(ifNode, visited.getSecondBody() != null ? visited
 						.getSecondBody().getStartOffset() : visited
 						.getEndKeyword().getPosition().getStartOffset());
 				Node branch = visited.getSecondBody();
 				while (branch != null) {
 					if (branch instanceof IfNode.ElseIf) {
 						final IfNode.ElseIf elseIfBranch = (IfNode.ElseIf) branch;
 						FormatterElseIfNode elseIfNode = new FormatterElseIfNode(
 								document);
 						elseIfNode.setBegin(createTextNode(document,
 								elseIfBranch.getStartOffset(), elseIfBranch
 										.getCondition().getEndOffset()));
 						push(elseIfNode);
 						visitChild(elseIfBranch.getFirstBody());
 						branch = ((IfNode.ElseIf) branch).getSecondBody();
 						checkedPop(elseIfNode, branch != null ? branch
 								.getStartOffset() : visited.getEndKeyword()
 								.getPosition().getStartOffset());
 					} else if (branch instanceof ElseNode) {
 						final ElseNode elseBranch = (ElseNode) branch;
 						FormatterIfElseNode elseNode = new FormatterIfElseNode(
 								document);
 						elseNode.setBegin(createTextNode(document, elseBranch
 								.getElseKeyword()));
 						push(elseNode);
 						visitChild(elseBranch.getStatement());
 						checkedPop(elseNode, visited.getEndKeyword()
 								.getPosition().getStartOffset());
 						branch = null;
 					} else {
 						RubyFormatterPlugin.warn(NLS.bind(
 								"Unexpected {0} class in if expression", branch
 										.getClass().getName()),
 								new DumpStackOnly());
 						break;
 					}
 				}
 				addChild(new FormatterIfEndNode(document, visited
 						.getEndKeyword().getPosition()));
 				return null;
 			}
 
 			/*
 			 * @see
 			 * org.jruby.ast.visitor.AbstractVisitor#visitBeginNode(org.jruby
 			 * .ast.BeginNode)
 			 */
 			public Instruction visitBeginNode(BeginNode visited) {
 				FormatterBeginNode beginNode = new FormatterBeginNode(document);
 				beginNode.setBegin(createTextNode(document, visited
 						.getBeginKeyword()));
 				push(beginNode);
 				visitChild(visited.getBodyNode());
 				checkedPop(beginNode, visited.getEndKeyword().getPosition()
 						.getStartOffset());
 				beginNode.setEnd(createTextNode(document, visited
 						.getEndKeyword()));
 				return null;
 			}
 
 			public Instruction visitRescueNode(RescueNode visited) {
 				if (visited.isInline()) {
 					return null;
 				}
 				visitChild(visited.getBodyNode());
 				RescueBodyNode node = visited.getRescueNode();
 				while (node != null) {
 					FormatterRescueNode rescueNode = new FormatterRescueNode(
 							document);
 					rescueNode.setBegin(createTextNode(document, node
 							.getRescueKeyword().getPosition().getStartOffset(),
 							node.getExceptionNodes() != null ? node
 									.getExceptionNodes().getEndOffset() : node
 									.getRescueKeyword().getPosition()
 									.getEndOffset()));
 					push(rescueNode);
 					visitChild(node.getBodyNode());
 					node = node.getOptRescueNode();
 					final int rescueEnd;
 					if (node != null) {
 						rescueEnd = node.getStartOffset();
 					} else if (visited.getElseNode() != null) {
 						rescueEnd = visited.getElseNode().getStartOffset();
 					} else {
 						rescueEnd = -1;
 					}
 					checkedPop(rescueNode, rescueEnd);
 				}
 				if (visited.getElseNode() != null) {
 					final ElseNode elseBranch = (ElseNode) visited
 							.getElseNode();
 					FormatterRescueElseNode elseNode = new FormatterRescueElseNode(
 							document);
 					elseNode.setBegin(createTextNode(document, elseBranch
 							.getElseKeyword()));
 					push(elseNode);
 					visitChild(elseBranch.getStatement());
 					checkedPop(elseNode, -1);
 				}
 				return null;
 			}
 
 			public Instruction visitEnsureNode(EnsureNode visited) {
 				visitChild(visited.getBodyNode());
 				FormatterEnsureNode ensureNode = new FormatterEnsureNode(
 						document);
 				ensureNode.setBegin(createTextNode(document, visited
 						.getEnsureNode().getKeyword()));
 				push(ensureNode);
 				visitChild(visited.getEnsureNode().getStatement());
 				checkedPop(ensureNode, -1);
 				return null;
 			}
 
 			public Instruction visitPreExeNode(PreExeNode visited) {
 				FormatterAtBeginNode endNode = new FormatterAtBeginNode(
 						document);
 				endNode.setBegin(createTextNode(document, visited.getKeyword()
 						.getPosition().getStartOffset(), visited.getLeftBrace()
 						.getPosition().getEndOffset()));
 				push(endNode);
 				visitChildren(visited);
 				checkedPop(endNode, visited.getRightBrace().getPosition()
 						.getStartOffset());
 				endNode
 						.setEnd(createTextNode(document, visited
 								.getRightBrace()));
 				return null;
 			}
 
 			public Instruction visitPostExeNode(PostExeNode visited) {
 				FormatterAtEndNode endNode = new FormatterAtEndNode(document);
 				endNode.setBegin(createTextNode(document, visited
 						.getEndKeyword().getPosition().getStartOffset(),
 						visited.getLeftBrace().getPosition().getEndOffset()));
 				push(endNode);
 				visitChildren(visited);
 				checkedPop(endNode, visited.getRightBrace().getPosition()
 						.getStartOffset());
 				endNode
 						.setEnd(createTextNode(document, visited
 								.getRightBrace()));
 				return null;
 			}
 
 			public Instruction visitStrNode(StrNode visited) {
 				FormatterStringNode strNode = new FormatterStringNode(document,
 						visited.getStartOffset(), visited.getEndOffset());
 				addChild(strNode);
 				return null;
 			}
 
 			public Instruction visitDStrNode(DStrNode visited) {
 				FormatterStringNode strNode = new FormatterStringNode(document,
 						visited.getStartOffset(), visited.getEndOffset());
 				addChild(strNode);
 				return null;
 			}
 
 			public Instruction visitRegexpNode(RegexpNode visited) {
 				FormatterStringNode strNode = new FormatterStringNode(document,
 						visited.getStartOffset(), visited.getEndOffset());
 				addChild(strNode);
 				return null;
 			}
 
 			public Instruction visitDRegxNode(DRegexpNode visited) {
 				FormatterStringNode strNode = new FormatterStringNode(document,
 						visited.getStartOffset(), visited.getEndOffset());
 				addChild(strNode);
 				return null;
 			}
 
 			public Instruction visitXStrNode(XStrNode visited) {
 				FormatterStringNode strNode = new FormatterStringNode(document,
 						visited.getStartOffset(), visited.getEndOffset());
 				addChild(strNode);
 				return null;
 			}
 
 			public Instruction visitHeredocNode(HeredocNode visited) {
 				FormatterHereDocNode heredocNode = new FormatterHereDocNode(
 						document, visited.getStartOffset(), visited
 								.getEndOffset(), visited.isIndent());
 				addChild(heredocNode);
 				heredocNode.setContentRegion(createRegion(visited.getContent()
 						.getPosition()));
 				heredocNode.setEndMarkerRegion(createRegion(visited
 						.getEndMarker().getPosition()));
 				return null;
 			}
 
 			private void visitChildren(Node visited) {
				final List children = visited.childNodes();
				if (!children.isEmpty()) {
					visitChildren(children);
				}
 			}
 
 			private void visitChildren(List children) {
 				for (Iterator i = children.iterator(); i.hasNext();) {
 					final Node child = (Node) i.next();
 					visitChild(child);
 				}
 			}
 
 			private void visitChild(final Node child) {
 				if (child != null && isVisitable(child)) {
 					child.accept(this);
 				}
 			}
 
 			private boolean isVisitable(Node node) {
 				return !(node instanceof ArgumentNode)
 						&& node.getClass() != ListNode.class;
 			}
 
 		});
 		checkedPop(root, document.getLength());
 		return root;
 	}
 
 	/**
 	 * @param holder
 	 * @return
 	 */
 	private IFormatterTextNode createTextNode(IFormatterDocument document,
 			ISourcePositionHolder holder) {
 		return createTextNode(document, holder.getPosition());
 	}
 
 	/**
 	 * @param position
 	 * @return
 	 */
 	private IFormatterTextNode createTextNode(IFormatterDocument document,
 			ISourcePosition position) {
 		return createTextNode(document, position.getStartOffset(), position
 				.getEndOffset());
 	}
 
 	private static IRegion createRegion(ISourcePosition position) {
 		return new Region(position.getStartOffset(), position.getEndOffset()
 				- position.getStartOffset());
 	}
 
 	protected static final Comparator POSITION_COMPARATOR = new Comparator() {
 
 		public int compare(Object o1, Object o2) {
 			final Node node1 = (Node) o1;
 			final Node node2 = (Node) o2;
 			return node1.getStartOffset() - node2.getStartOffset();
 		}
 
 	};
 
 }
