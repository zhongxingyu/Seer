 /*
  * Tracing debug code insertion utility.
  * Copyright (C) 2013  Zuben El Acribi
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  *
  */
 
 package codegen;
 
 import java.util.ArrayList;
 
 import codegen.Annotations.Type;
 
 import bnf.Tree;
 import bnf.ParseTree;
 
 public class DebugCodeInserter {
 
 	private ParseTree parseTree;
 	private Annotations ann;
 
 	public DebugCodeInserter(ParseTree parseTree, Annotations ann) {
 		this.parseTree = parseTree;
 		this.ann = ann;
 		ann.newFile(parseTree.filename);
 	}
 
 	public DebugCodeInserter run() {
 		insertDebugCode(parseTree.tree);
 		return this;
 	}
 
 	void insertDebugCode(Tree tree) {
 		if (tree.def.parent.node.equals("MethodDeclaratorRest")) { // MethodDeclaratorRest: FormalParameters {'[' ']'} ['throws' QualifiedIdentifierList] (Block | ';')
 
 			Tree params = tree.branches.get(0);
 			tree = tree.branches.get(3).branches.get(0);
 			if (tree == null) {
 				return;
 			}
 			block(tree, params);
 
 		} else if (tree.def.parent.node.equals("VoidMethodDeclaratorRest")) { // VoidMethodDeclaratorRest: FormalParameters ['throws' QualifiedIdentifierList] (Block | ';')
 
 			Tree params = tree.branches.get(0);
 			tree = tree.branches.get(2).branches.get(0);
 			if (tree == null) {
 				return;
 			}
 			block(tree, params);
 
 		} else if (tree.def.parent.node.equals("ConstructorDeclaratorRest")) { // ConstructorDeclaratorRest: FormalParameters ['throws' QualifiedIdentifierList] Block
 
 			block(tree.branches.get(2), tree.branches.get(0));
 			
 		} else if (tree.def.parent.node.equals("Expression")) {
 
 			ArrayList<String> assignmentVars = new ArrayList<String>();
 			if (traceableExpression(tree, assignmentVars)) {
 				tree.prefix = "$.$.$(" + ann.annotation(Type.expr, tree.begin, tree.end) + "l, ";
 				tree.suffix = ")";
 			}
 			if (!(tree.parent != null && tree.parent.parent != null && tree.parent.parent.def.node.equals("ForVariableDeclaratorsRest ';' [Expression] ';' [ForUpdate]"))) {
 				if (assignmentVars.size() > 0) {
 					StringBuffer buff = new StringBuffer();
 					for (String s: assignmentVars) {
 						buff.append(" $.$.var(" + ann.annotation(Type.var, tree.begin, tree.end) + "l, \"");
 						buff.append(s);
 						buff.append("\", ");
 						buff.append(s);
 						buff.append(");");
 					}
 					if (tree.parent != null && tree.parent.def.node.equals("StatementExpression ';'")) {
 						if (tree.parent.suffix != null) {
 							tree.parent.suffix += buff;
 						} else {
 							tree.parent.suffix = buff.toString();
 						}
 					}
 				}
 			}
 			
 		} else if (tree.def.parent.node.equals("BlockStatement")) { // BlockStatement: LocalVariableDeclarationStatement | ClassOrInterfaceDeclaration | [Identifier ':'] Statement
 
 			if (tree.branches.get(0) != null) { // LocalVariableDeclarationStatement: { VariableModifier } Type VariableDeclarators ';'
 				tree = tree.branches.get(0);
 				tree.prefix = " $.$.step(" + ann.annotation(Type.step, tree.begin, tree.end) + "l);";
 				Tree t = tree.branches.get(2); // VariableDeclarators: VariableDeclarator { ',' VariableDeclarator }
 				StringBuffer buff = new StringBuffer();
 				String var = t.branches.get(0).branches.get(0).node;
 				buff.append(" $.$.var(" + ann.annotation(Type.var, tree.begin, tree.end) + "l, \"");
 				buff.append(var);
 				buff.append("\", ");
 				buff.append(var);
 				buff.append(");");
 				t = t.branches.get(1);
 				for (Tree b: t.branches) {
 					var = b.branches.get(1).branches.get(0).node;
 					buff.append(" $.$.var(" + ann.annotation(Type.vardecl, t.begin, t.end) + "l, \"");
 					buff.append(var);
 					buff.append("\", ");
 					buff.append(var);
 					buff.append(");");
 				}
 				tree.suffix = buff.toString();
			} else if (tree.branches.size() > 2 && tree.branches.get(2) != null) { // [Identifier ':'] Statement
 				tree = tree.branches.get(2);
 				tree.prefix = " $.$.step(" + ann.annotation(Type.step, tree.begin, tree.end) + "l);";
 			}
 			
 		} else if (tree.def.node.startsWith("'if'") && tree.def.branches.size() > 0) { // 'if' ParExpression Statement ['else' Statement]
 			
 			Tree t = tree.branches.get(2);
 			t.prefix = "{ $.$.step(" + ann.annotation(Type.step, tree.begin, tree.end) + "l);";
 			t.suffix = " }";
 			if (tree.branches.size() > 3 && tree.branches.get(3).node.length() > 0) {
 				t = tree.branches.get(3).branches.get(1);
 				t.prefix = "{ $.$.step(" + ann.annotation(Type.step, tree.begin, tree.end) + "l);";
 				t.suffix = " }";
 			}
 			
 		} else if (tree.def.node.startsWith("'for'") && tree.def.branches.size() > 0) { // 'for' '(' ForControl ')' Statement
 			
 			ArrayList<String> declaredVars = new ArrayList<String>();
 			Tree t = tree.branches.get(2).branches.get(0); // {VariableModifier} Type VariableDeclaratorId ForVarControlRest
 			if (t != null) {
 				Tree u = t.branches.get(2).branches.get(0);
 				declaredVars.add(u.node);
 				u = t.branches.get(3); // ForVariableDeclaratorsRest ';' [Expression] ';' [ForUpdate] | ':' Expression
 				if (u.branches.get(0) != null) {
 					u = u.branches.get(0).branches.get(0); // [ '=' VariableInitializer ] { ',' VariableDeclarator }
 					u = u.branches.get(1);
 					for (Tree b: u.branches) {
 						declaredVars.add(b.branches.get(1).branches.get(0).node);
 					}
 				}
 			}
 			scope(tree);
 			StringBuffer buff = new StringBuffer();
 			for (String s: declaredVars) {
 				buff.append(" $.$.var(" + ann.annotation(Type.vardecl, tree.begin, tree.end) + "l, \"");
 				buff.append(s);
 				buff.append("\", ");
 				buff.append(s);
 				buff.append("); ");
 			}
 			t = tree.branches.get(4);
 			t.prefix = "{ " + buff + " $.$.step(" + ann.annotation(Type.step, tree.begin, tree.end) + "l);";
 			t.suffix = " } ";
 
 		} else if (tree.def.node.startsWith("'do'") && tree.def.branches.size() > 0) { // 'do' Statement 'while' ParExpression ';'
 			
 			Tree t = tree.branches.get(1);
 			t.prefix = "{ $.$.step(" + ann.annotation(Type.step, tree.begin, tree.end) + "l);";
 			t.suffix = " } ";
 			scope(tree);
 			
 		} else if (tree.def.node.startsWith("'while'") && tree.def.branches.size() > 0) { // 'while' ParExpression Statement
 			
 			Tree t = tree.branches.get(2);
 			t.prefix = "{ $.$.step(" + ann.annotation(Type.step, tree.begin, tree.end) + "l);";
 			t.suffix = " } ";
 			scope(tree);
 			
 		} else if (tree.def.node.equals("{Modifier}")) {
 			
 			boolean visibility = false;
 			for (Tree b: tree.branches) {
 				if (b.node.equals("protected") || b.node.equals("private")) {
 					b.prefix = "public";
 					b.hide = true;
 					visibility = true;
 					break;
 				} else if (b.node.equals("public")) {
 					visibility = true;
 					break;
 				}
 			}
 			if (!visibility) {
 				tree.prefix = "public ";
 				if (tree.begin == tree.end) {
 					Tree t = tree.parent;
 					int i = 0;
 					for (; i < t.branches.size(); i++) {
 						if (t.branches.get(i) == tree) {
 							break;
 						}
 					}
 					tree.begin = t.branches.get(i + 1).begin;
 				}
 			}
 			
 		}
 
 		for (Tree b: tree.branches) {
 			if (b != null) {
 				insertDebugCode(b);
 			}
 		}
 	}
 	
 	void block(Tree t, Tree params) {
 		t.prefix = formalParameters(params);
 		t.suffix = " finally { $.$.endscope(" + ann.annotation(Type.endscope, t.begin, t.end) + "l); } }";
 	}
 
 	String formalParameters(Tree t) {
 		String[] params = getListOfFormalParameters(t);
 		StringBuffer buff = new StringBuffer();
 		buff.append("{ $.$.scope(" + ann.annotation(Type.scope, t.begin, t.end) + "l); ");
 		for (int i = 0; i < params.length; i++) {
 			buff.append("$.$.var(" + ann.annotation(Type.vardecl, t.begin, t.end) + "l, \"");
 			buff.append(params[i]);
 			buff.append("\", ");
 			buff.append(params[i]);
 			buff.append("); ");
 		}
 		buff.append("$.$.step(" + ann.annotation(Type.step, t.begin, t.end) + "l); try ");
 		return buff.toString();
 	}
 	
 	String[] getListOfFormalParameters(Tree t) { // FormalParameters: '(' [FormalParameterDecls] ')'
 		ArrayList<String> l = new ArrayList<String>();
 		if (t.branches.get(1).node.length() > 0) {
 			while (true) {
 				t = t.branches.get(1); // FormalParameterDecls: {VariableModifier} Type FormalParameterDeclsRest
 				t = t.branches.get(2); // FormalParameterDeclsRest: VariableDeclaratorId [ ',' FormalParameterDecls ] | '...' VariableDeclaratorId
 				if (t.branches.get(0) != null) {
 					t = t.branches.get(0);
 					l.add(t.branches.get(0).node);
 					if (t.branches.get(1).node.length() > 0) {
 						t = t.branches.get(1);
 					} else {
 						break;
 					}
 				} else {
 					t = t.branches.get(1);
 					l.add(t.branches.get(1).node);
 					break;
 				}
 			}
 		}
 		return l.toArray(new String[0]);
 	}
 
 	boolean traceableExpression(Tree t, ArrayList<String> assignmentVars) { // Expression: Expression1 [ AssignmentOperator Expression ]
 		Tree original = t;
 		if (t.branches.get(1).node.length() > 0) {
 			String name = t.branches.get(0).node;
 			boolean isIdentitifer = true;
 			for (int i = 0; i < name.length(); i++) {
 				if (!Character.isLetterOrDigit(name.charAt(i))) {
 					isIdentitifer = false;
 					break;
 				}
 			}
 			if (isIdentitifer) {
 				assignmentVars.add(name);
 			}
 			t = t.branches.get(1).branches.get(1);
 			traceableExpression(t, assignmentVars);
 			return false;
 		} else {
 			t = t.branches.get(0); // Expression1: Expression2 [ Expression1Rest ]
 			if (t.branches.get(1).node.length() == 0) { // Expression1Rest: '?' Expression ':' Expression1
 //				Tree u = t.branches.get(0);
 //				u.prefix = "$.$.$(";
 //				u.suffix = ")";
 //				traceableExpression(t.branches.get(0), assignmentVars);
 //				traceableExpression(t.branches.get(1).branches.get(1), assignmentVars);
 //				traceableExpression(t.branches.get(1).branches.get(3), assignmentVars);
 //			} else {
 				t = t.branches.get(0); // Expression2: Expression3 [ Expression2Rest ]
 				if (t.branches.get(1).node.length() == 0) {
 					t = t.branches.get(0); // Expression3: PrefixOp Expression3 | '(' ( Type | Expression ) ')' Expression3 | Primary { Selector } { PostfixOp })
 					if (t.branches.size() > 2 && t.branches.get(2) != null) {
 						t = t.branches.get(2);
 						Tree u = t.branches.get(0).branches.get(0);
 						if (u != null) {
 							return false; // Literal
 						}
 						u = t.branches.get(0);
 						if (u.branches.size() > 7) {
 							u = u.branches.get(7);
 							if (u != null) { // Identifier { '.' Identifier } [IdentifierSuffix]
 								if (u.branches.size() > 2 && u.branches.get(2).node.length() > 0) {
 									return !original.parent.def.node.equals("StatementExpression ';'");
 								}
 							}
 						}
 					}
 				}
 			}
 		}
 		return true;
 	}
 
 	void scope(Tree t) {
 		while (t.parent != null && t.parent.parent != null && t.parent.parent.def.node.equals("Identifier ':' Statement")) {
 			t = t.parent.parent; // Statement: Block | ';' | Identifier ':' Statement | ...
 		}
 		if (t.parent != null && t.parent.parent != null && t.parent.parent.def.node.equals("[Identifier ':'] Statement") && t.parent.parent.branches.get(0).node.length() > 0) {
 			t = t.parent.parent; // [Identifier ':'] Statement
 		}
 		if (t.prefix == null) {
 			t.prefix = "";
 		}
 		t.prefix += "$.$.scope(" + ann.annotation(Type.scope, t.begin, t.end) + "l); try { ";
 		if (t.suffix == null) {
 			t.suffix = "";
 		}
 		t.suffix += " } finally { $.$.endscope(" + ann.annotation(Type.endscope, t.begin, t.end) + "l); } ";
 	}
 
 	public ParseTree getParseTree() {
 		return parseTree;
 	}
 
 }
