 /*******************************************************************************
  * Copyright (c) 2000, 2005 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     IBM Corporation - initial API and implementation
  *******************************************************************************/
 package descent.core.dom;
 
 import java.util.List;
 
 /**
  * Internal AST visitor for serializing an AST in a quick and dirty fashion.
  * For various reasons the resulting string is not necessarily legal
  * Java code; and even if it is legal Java code, it is not necessarily the string
  * that corresponds to the given AST. Although useless for most purposes, it's
  * fine for generating debug print strings.
  * <p>
  * Example usage:
  * <code>
  * <pre>
  *    NaiveASTFlattener p = new NaiveASTFlattener();
  *    node.accept(p);
  *    String result = p.getResult();
  * </pre>
  * </code>
  * Call the <code>reset</code> method to clear the previous result before reusing an
  * existing instance.
  * </p>
  */
 class NaiveASTFlattener extends ASTVisitor {
 	
 	private final String EMPTY= ""; //$NON-NLS-1$
 	private final String LINE_END= "\n"; //$NON-NLS-1$
 	
 	/**
 	 * The string buffer into which the serialized representation of the AST is
 	 * written.
 	 */
 	private StringBuffer buffer;	
 	private int indent = 0;
 	
 	/**
 	 * Creates a new AST printer.
 	 */
 	NaiveASTFlattener() {
 		this.buffer = new StringBuffer();
 	}
 	
 	/**
 	 * Returns the string accumulated in the visit.
 	 *
 	 * @return the serialized 
 	 */
 	public String getResult() {
 		return this.buffer.toString();
 	}
 	
 	/**
 	 * Resets this printer so that it can be used again.
 	 */
 	public void reset() {
 		this.buffer.setLength(0);
 	}
 	
 	void printIndent() {
 		for (int i = 0; i < this.indent; i++) 
 			this.buffer.append("  "); //$NON-NLS-1$
 	}
 	
 	void visitModifiers(List<Modifier> ext) {
 		visitList(ext, " ", EMPTY, " ");
 	}
 	
 	void visitPreDDocss(List<? extends ASTNode> ext) {
 		visitList(ext, LINE_END, EMPTY, LINE_END);
 	}
 	
 	void visitList(List<? extends ASTNode> ext, String separator) {
 		visitList(ext, separator, EMPTY, EMPTY);
 	}
 	
 	void visitList(List<? extends ASTNode> ext, String separator, String pre, String post) {
 		if (ext.isEmpty()) return;
 		
 		int i = 0;
 		this.buffer.append(pre);
 		for(ASTNode p : ext) {
 			if (i > 0) {
 				this.buffer.append(separator);
 			}
 			p.accept(this);
 			i++;
 		}
 		this.buffer.append(post);
 	}
 	
 	@Override
 	public boolean visit(AggregateDeclaration node) {
 		visitPreDDocss(node.preDDocs());
 		printIndent();
 		visitModifiers(node.modifiers());
 		this.buffer.append(node.getKind().getToken());
 		this.buffer.append(" ");
 		if (node.getName() != null) {
 			node.getName().accept(this);
 		}
 		visitList(node.templateParameters(), ", ", "(", ")");
 		visitList(node.baseClasses(), ", ", " : ", EMPTY);
 		this.buffer.append(" {\n");
 		this.indent++;
 		visitList(node.declarations(), LINE_END, EMPTY, LINE_END);
 		this.indent--;
 		printIndent();
 		this.buffer.append("}");
 		if (node.getPostDDoc() != null) {
 			this.buffer.append(" ");
 			node.getPostDDoc().accept(this);
 		}
 		return false;
 	}
 	
 	@Override
 	public boolean visit(AliasDeclaration node) {
 		visitPreDDocss(node.preDDocs());
 		printIndent();
 		visitModifiers(node.modifiers());
 		this.buffer.append("alias ");
 		node.getType().accept(this);
 		this.buffer.append(" ");
 		visitList(node.fragments(), ", ");
 		this.buffer.append(";");
 		if (node.getPostDDoc() != null) {
 			this.buffer.append(" ");
 			node.getPostDDoc().accept(this);
 		}
 		return false;
 	}
 	
 	@Override
 	public boolean visit(AliasDeclarationFragment node) {
 		node.getName().accept(this);
 		return false;
 	}
 	
 	@Override
 	public boolean visit(AliasTemplateParameter node) {
 		this.buffer.append("alias ");
 		node.getName().accept(this);
 		if (node.getSpecificType() != null) {
 			this.buffer.append(" : ");
 			node.getSpecificType().accept(this);
 		}
 		if (node.getDefaultType() != null) {
 			this.buffer.append(" = ");
 			node.getDefaultType().accept(this);
 		}
 		return false;
 	}
 	
 	@Override
 	public boolean visit(AlignDeclaration node) {
 		visitPreDDocss(node.preDDocs());
 		printIndent();
 		visitModifiers(node.modifiers());
 		this.buffer.append("align");
 		if (node.getAlign() >= 2) {
 			this.buffer.append("(");
 			this.buffer.append(node.getAlign());
 			this.buffer.append(")");
 		}
 		this.buffer.append(" {\n");
 		this.indent++;
 		visitList(node.declarations(), LINE_END, EMPTY, LINE_END);
 		this.indent--;
 		printIndent();
 		this.buffer.append("}");
 		if (node.getPostDDoc() != null) {
 			this.buffer.append(" ");
 			node.getPostDDoc().accept(this);
 		}
 		return false;
 	}
 	
 	@Override
 	public boolean visit(Argument node) {
 		boolean mustAppendSpace = true;
 		switch(node.getPassageMode()) {
 		case DEFAULT: mustAppendSpace = false; break;
 		case IN: this.buffer.append("in"); break;
 		case INOUT: this.buffer.append("inout"); break;
 		case LAZY: this.buffer.append("lazy"); break;
 		case OUT: this.buffer.append("out"); break;
 		}
 		
 		if (node.getType() != null) {
 			if (mustAppendSpace) {
 				this.buffer.append(" ");
 			}
 			node.getType().accept(this);
 			mustAppendSpace = true;
 		}
 		if (node.getName() != null) {
 			if (mustAppendSpace) {
 				this.buffer.append(" ");
 			}
 			node.getName().accept(this);
 		}
 		if (node.getDefaultValue() != null) {
 			this.buffer.append(" = ");
 			node.getDefaultValue().accept(this);
 		}
 		return false;
 	}
 	
 	@Override
 	public boolean visit(ArrayAccess node) {
 		node.getArray().accept(this);
 		this.buffer.append("[");
 		visitList(node.indexes(), ", ");
 		this.buffer.append("]");
 		return false;
 	}
 	
 	@Override
 	public boolean visit(ArrayInitializer node) {
 		this.buffer.append("[");
 		visitList(node.fragments(), ", ");
 		this.buffer.append("]");
 		return false;
 	}
 	
 	@Override
 	public boolean visit(ArrayInitializerFragment node) {
 		if (node.getExpression() != null) {
 			node.getExpression().accept(this);
 			this.buffer.append(": ");
 		}
 		node.getInitializer().accept(this);
 		return false;
 	}
 	
 	@Override
 	public boolean visit(ArrayLiteral node) {
 		this.buffer.append("[");
 		visitList(node.arguments(), ", ");
 		this.buffer.append("]");
 		return false;
 	}
 	
 	@Override
 	public boolean visit(AsmBlock node) {
 		printIndent();
 		this.buffer.append("asm {\n");
 		this.indent++;
 		visitList(node.statements(), LINE_END, EMPTY, LINE_END);
 		this.indent--;
 		printIndent();
 		this.buffer.append("}");
 		return false;
 	}
 	
 	@Override
 	public boolean visit(AsmStatement node) {
 		printIndent();
 		visitList(node.tokens(), " ");
 		this.buffer.append(";");
 		return false;
 	}
 	
 	@Override
 	public boolean visit(AsmToken node) {
 		this.buffer.append(node.getToken());
 		return false;
 	}
 	
 	@Override
 	public boolean visit(AssertExpression node) {
 		this.buffer.append("assert(");
 		node.getExpression().accept(this);
 		if (node.getMessage() != null) {
 			this.buffer.append(", ");
 			node.getMessage().accept(this);
 		}
 		this.buffer.append(")");
 		return false;
 	}
 	
 	@Override
 	public boolean visit(Assignment node) {
 		node.getLeftHandSide().accept(this);
 		this.buffer.append(" ");
 		this.buffer.append(node.getOperator().toString());
 		this.buffer.append(" ");
 		node.getRightHandSide().accept(this);
 		return false;
 	}
 	
 	@Override
 	public boolean visit(AssociativeArrayType node) {
 		node.getComponentType().accept(this);
 		this.buffer.append("[");
 		node.getKeyType().accept(this);
 		this.buffer.append("]");
 		return false;
 	}
 	
 	@Override
 	public boolean visit(BaseClass node) {
 		if (node.getModifier() != null) {
 			node.getModifier().accept(this);
 			this.buffer.append(" ");
 		}
 		node.getType().accept(this);
 		return false;
 	}
 	
 	@Override
 	public boolean visit(Block node) {
 		//printIndent();
 		this.buffer.append("{\n");
 		this.indent++;
 		visitList(node.statements(), LINE_END, EMPTY, LINE_END);
 		this.indent--;
 		printIndent();
 		this.buffer.append("}");
 		return false;
 	}
 	
 	@Override
 	public boolean visit(BooleanLiteral node) {
 		this.buffer.append(node.booleanValue());
 		return false;
 	}
 	
 	@Override
 	public boolean visit(BreakStatement node) {
 		printIndent();
 		this.buffer.append("break");
 		if (node.getLabel() != null) {
 			this.buffer.append(" ");
 			node.getLabel().accept(this);
 		}
 		this.buffer.append(";");
 		return false;
 	}
 	
 	@Override
 	public boolean visit(CallExpression node) {
 		node.getExpression().accept(this);
 		this.buffer.append("(");
 		visitList(node.arguments(), ", ");
 		this.buffer.append(")");
 		return false;
 	}
 	
 	@Override
 	public boolean visit(SwitchCase node) {
 		printIndent();
 		this.buffer.append("case ");
 		node.getExpression().accept(this);
 		this.buffer.append(": ");
 		node.getBody().accept(this);
 		return false;
 	}
 	
 	@Override
 	public boolean visit(CastExpression node) {
 		this.buffer.append("cast(");
 		node.getType().accept(this);
 		this.buffer.append(") ");
 		node.getExpression().accept(this);
 		return false;
 	}
 	
 	@Override
 	public boolean visit(CatchClause node) {
 		printIndent();
 		this.buffer.append("catch");
 		if (node.getType() != null) {
 			this.buffer.append("(");
 			node.getType().accept(this);
 			if (node.getName() != null) {
 				this.buffer.append(" ");
 				node.getName().accept(this);
 			}
 			this.buffer.append(")");
 		}
 		this.buffer.append(" ");
 		node.getBody().accept(this);
 		return false;
 	}
 	
 	@Override
 	public boolean visit(CharacterLiteral node) {
 		this.buffer.append(node.getEscapedValue());
 		return false;
 	}
 	
 	@Override
 	public boolean visit(CodeComment node) {
 		switch(node.getKind()) {
 		case BLOCK_COMMENT: this.buffer.append("/* */"); break;
 		case LINE_COMMENT: this.buffer.append("//"); break;
 		case PLUS_COMMENT: this.buffer.append("/+ +/"); break;
 		}
 		return false;
 	}
 	
 	@Override
 	public boolean visit(CompilationUnit node) {
 		if (node.getModuleDeclaration() != null) {
 			node.getModuleDeclaration().accept(this);
 			this.buffer.append(LINE_END);
 		}
 		
 		visitList(node.declarations(), LINE_END, EMPTY, LINE_END);
 		return false;
 	}
 	
 	@Override
 	public boolean visit(ConditionalExpression node) {
 		node.getExpression().accept(this);
 		this.buffer.append(" ? ");
 		node.getThenExpression().accept(this);
 		this.buffer.append(" : ");
 		node.getElseExpression().accept(this);
 		return false;
 	}
 	
 	@Override
 	public boolean visit(ConstructorDeclaration node) {
 		visitPreDDocss(node.preDDocs());
 		printIndent();
 		visitModifiers(node.modifiers());
 		switch(node.getKind()) {
 		case CONSTRUCTOR:
 			this.buffer.append("this");
 			break;
 		case DELETE:
 			this.buffer.append("delete");
 			break;
 		case DESTRUCTOR:
 			this.buffer.append("~this");
 			break;
 		case NEW:
 			this.buffer.append("new");
 			break;
 		case STATIC_CONSTRUCTOR:
 			this.buffer.append("static this");
 			break;
 		case STATIC_DESTRUCTOR:
 			this.buffer.append("static ~this");
 			break;
 		}
 		this.buffer.append("(");
 		visitList(node.arguments(), ", ");
 		if (node.isVariadic()) {
 			this.buffer.append("...");
 		}
 		this.buffer.append(")");
 		if (node.getPrecondition() != null) {
 			this.buffer.append(LINE_END);
 			printIndent();
 			this.buffer.append("in ");
 			node.getPrecondition().accept(this);
 			this.buffer.append(LINE_END);
 			printIndent();
 		}
 		if (node.getPostcondition() != null) {
 			this.buffer.append(LINE_END);
 			printIndent();
 			this.buffer.append("out ");
 			node.getPostcondition().accept(this);
 			this.buffer.append(LINE_END);
 			printIndent();
 		}
 		if (node.getPrecondition() != null || node.getPostcondition() != null) {
 			this.buffer.append("body");
 		}
 		this.buffer.append(" ");
 		node.getBody().accept(this);
 		if (node.getPostDDoc() != null) {
 			this.buffer.append(" ");
 			node.getPostDDoc().accept(this);
 		}
 		return false;
 	}
 	
 	@Override
 	public boolean visit(ContinueStatement node) {
 		printIndent();
 		this.buffer.append("continue");
 		if (node.getLabel() != null) {
 			this.buffer.append(" ");
 			node.getLabel().accept(this);
 		}
 		this.buffer.append(";");
 		return false;
 	}
 	
 	@Override
 	public boolean visit(DDocComment node) {
 		this.buffer.append(node.getText());
 		return false;
 	}
 	
 	@Override
 	public boolean visit(DebugAssignment node) {
 		visitPreDDocss(node.preDDocs());
 		printIndent();
 		visitModifiers(node.modifiers());
 		this.buffer.append("debug = ");
 		node.getVersion().accept(this);
 		this.buffer.append(";");
 		if (node.getPostDDoc() != null) {
 			this.buffer.append(" ");
 			node.getPostDDoc().accept(this);
 		}
 		return false;
 	}
 	
 	@Override
 	public boolean visit(DebugDeclaration node) {
 		visitPreDDocss(node.preDDocs());
 		printIndent();
 		visitModifiers(node.modifiers());
 		this.buffer.append("debug");
 		if (node.getVersion() != null) {
 			this.buffer.append("(");
 			node.getVersion().accept(this);
 			this.buffer.append(")");
 		}
 		this.buffer.append(" {\n");
 		this.indent++;
 		visitList(node.thenDeclarations(), LINE_END, EMPTY, LINE_END);
 		this.indent--;
 		this.buffer.append("}");
 		if (!node.elseDeclarations().isEmpty()) {
 			this.buffer.append(" else {\n");
 			this.indent++;
 			visitList(node.elseDeclarations(), LINE_END, EMPTY, LINE_END);
 			this.indent--;
 			this.buffer.append("}");
 		}
 		if (node.getPostDDoc() != null) {
 			this.buffer.append(" ");
 			node.getPostDDoc().accept(this);
 		}
 		return false;
 	}
 	
 	@Override
 	public boolean visit(DebugStatement node) {
 		printIndent();
 		this.buffer.append("debug");
 		if (node.getVersion() != null) {
 			this.buffer.append("(");
 			node.getVersion().accept(this);
 			this.buffer.append(")");
 		}
 		this.buffer.append(" ");
 		node.getThenBody().accept(this);
 		if (node.getElseBody() != null) {
 			this.buffer.append(" else ");
 			node.getElseBody().accept(this);
 		}
 		return false;
 	}
 	
 	@Override
 	public boolean visit(DeclarationStatement node) {
 		printIndent();
 		node.getDeclaration().accept(this);
 		return false;
 	}
 	
 	@Override
 	public boolean visit(DefaultStatement node) {
 		printIndent();
 		this.buffer.append("default: ");
 		node.getBody().accept(this);
 		return false;
 	}
 	
 	@Override
 	public boolean visit(DelegateType node) {
 		node.getReturnType().accept(this);
 		this.buffer.append(" ");
 		if (node.isFunctionPointer()) {
 			this.buffer.append("function");
 		} else {
 			this.buffer.append("delegate");
 		}
 		this.buffer.append("(");
 		visitList(node.arguments(), ", ");
 		if (node.isVariadic()) {
 			this.buffer.append("...");
 		}
 		this.buffer.append(")");
 		return false;
 	}
 	
 	@Override
 	public boolean visit(DeleteExpression node) {
 		this.buffer.append("delete ");
 		node.getExpression().accept(this);
 		return false;
 	}
 	
 	@Override
 	public boolean visit(DollarLiteral node) {
 		this.buffer.append("$");
 		return false;
 	}
 	
 	@Override
 	public boolean visit(DoStatement node) {
 		printIndent();
 		this.buffer.append("do ");
 		node.getBody().accept(this);
 		this.buffer.append(" while(");
 		node.getExpression().accept(this);
 		this.buffer.append(");");
 		return false;
 	}
 	
 	@Override
 	public boolean visit(DotIdentifierExpression node) {
 		if (node.getExpression() != null) {
 			node.getExpression().accept(this);
 		}
 		this.buffer.append(".");
 		node.getName().accept(this);
 		return false;
 	}
 	
 	@Override
 	public boolean visit(DotTemplateTypeExpression node) {
 		if (node.getExpression() != null) {
 			node.getExpression().accept(this);
 		}
 		this.buffer.append(".");
 		node.getTemplateType().accept(this);
 		return false;
 	}
 	
 	@Override
 	public boolean visit(DynamicArrayType node) {
 		node.getComponentType().accept(this);
 		this.buffer.append("[]");
 		return false;
 	}
 	
 	@Override
 	public boolean visit(EnumDeclaration node) {
 		visitPreDDocss(node.preDDocs());
 		printIndent();
 		visitModifiers(node.modifiers());
 		this.buffer.append("enum");
 		if (node.getName() != null) {
 			this.buffer.append(" ");
 			node.getName().accept(this);
 		}
 		if (node.getBaseType() != null) {
 			this.buffer.append(" : ");
 			node.getBaseType().accept(this);
 		}
 		this.buffer.append(" {\n");
 		this.indent++;
 		visitList(node.enumMembers(), ",\n", EMPTY, LINE_END);
 		this.indent--;
 		this.buffer.append("}");
 		if (node.getPostDDoc() != null) {
 			this.buffer.append(" ");
 			node.getPostDDoc().accept(this);
 		}
 		return false;
 	}
 	
 	@Override
 	public boolean visit(EnumMember node) {
 		printIndent();
 		node.getName().accept(this);
 		if (node.getValue() != null) {
 			this.buffer.append(" = ");
 			node.getValue().accept(this);
 		}
 		return false;
 	}
 	
 	@Override
 	public boolean visit(ExpressionInitializer node) {
 		node.getExpression().accept(this);
 		return false;
 	}
 	
 	@Override
 	public boolean visit(ExpressionStatement node) {
 		printIndent();
 		node.getExpression().accept(this);
 		this.buffer.append(";");
 		return false;
 	}
 	
 	@Override
 	public boolean visit(ExternDeclaration node) {
 		visitPreDDocss(node.preDDocs());
 		printIndent();
 		visitModifiers(node.modifiers());
 		this.buffer.append("extern");
 		switch(node.getLinkage()) {
 		case C: this.buffer.append("(C)"); break;
 		case CPP: this.buffer.append("(C++)"); break;
 		case D: this.buffer.append("(D)"); break;
 		case DEFAULT: break;
 		case PASCAL: this.buffer.append("(Pascal)"); break;
 		case WINDOWS: this.buffer.append("(Windows)"); break;
 		}
 		this.buffer.append(" {\n");
 		this.indent++;
 		visitList(node.declarations(), LINE_END, EMPTY, LINE_END);
 		this.indent--;
 		this.buffer.append("}");
 		if (node.getPostDDoc() != null) {
 			this.buffer.append(" ");
 			node.getPostDDoc().accept(this);
 		}
 		return false;
 	}
 	
 	@Override
 	public boolean visit(ForeachStatement node) {
 		printIndent();
 		this.buffer.append("foreach");
 		if (node.isReverse()) {
 			this.buffer.append("_reverse");
 		}
 		this.buffer.append("(");
 		visitList(node.arguments(), ", ");
 		this.buffer.append("; ");
 		if (node.getExpression() != null) {
 			node.getExpression().accept(this);
 		}
 		this.buffer.append(") ");
 		node.getBody().accept(this);
 		return false;
 	}
 	
 	@Override
 	public boolean visit(ForStatement node) {
 		printIndent();
 		this.buffer.append("for(");
 		if (node.getInitializer() != null) {
 			node.getInitializer().accept(this);
 		} else {
 			this.buffer.append("; ");
 		}
 		if (node.getCondition() != null) {
 			node.getCondition().accept(this);
 		}
 		this.buffer.append("; ");
 		if (node.getIncrement() != null) {
 			node.getIncrement().accept(this);
 		}
 		this.buffer.append(") ");
 		node.getBody().accept(this);
 		return false;
 	}
 	
 	@Override
 	public boolean visit(FunctionDeclaration node) {
 		visitPreDDocss(node.preDDocs());
 		printIndent();
 		visitModifiers(node.modifiers());
 		node.getReturnType().accept(this);
 		this.buffer.append(" ");
 		node.getName().accept(this);
 		visitList(node.templateParameters(), ", ", "(", ")");
 		this.buffer.append("(");
 		visitList(node.arguments(), ", ");
 		if (node.isVariadic()) {
 			this.buffer.append("...");
 		}
 		this.buffer.append(")");
 		if (node.getPrecondition() != null) {
 			this.buffer.append(LINE_END);
 			printIndent();
 			this.buffer.append("in ");
 			node.getPrecondition().accept(this);
 			this.buffer.append(LINE_END);
 			printIndent();
 		}
 		if (node.getPostcondition() != null) {
 			this.buffer.append(LINE_END);
 			printIndent();
 			this.buffer.append("out ");
 			node.getPostcondition().accept(this);
 			this.buffer.append(LINE_END);
 			printIndent();
 		}
		if (node.getPrecondition() != null || node.getPostcondition() != null) {
			this.buffer.append("body");
 		}
		this.buffer.append(" ");
		node.getBody().accept(this);
 		if (node.getPostDDoc() != null) {
 			this.buffer.append(" ");
 			node.getPostDDoc().accept(this);
 		}
 		return false;
 	}
 	
 	@Override
 	public boolean visit(FunctionLiteralDeclarationExpression node) {
 		switch(node.getSyntax()) {
 		case DELEGATE: this.buffer.append("delegate "); break;
 		case EMPTY: break;
 		case FUNCTION: this.buffer.append("function "); break;
 		}
 		this.buffer.append("(");
 		visitList(node.arguments(), ", ");
 		if (node.isVariadic()) {
 			this.buffer.append("...");
 		}
 		this.buffer.append(")");
 		if (node.getPrecondition() != null) {
 			this.buffer.append(LINE_END);
 			printIndent();
 			this.buffer.append(" in ");
 			node.getPrecondition().accept(this);
 			this.buffer.append(LINE_END);
 			printIndent();
 		}
 		if (node.getPostcondition() != null) {
 			this.buffer.append(LINE_END);
 			printIndent();
 			this.buffer.append(" out ");
 			node.getPostcondition().accept(this);
 			this.buffer.append(LINE_END);
 			printIndent();
 		}
 		if (node.getPrecondition() != null || node.getPostcondition() != null) {
 			this.buffer.append(" body");
 		}
 		this.buffer.append(" ");
 		node.getBody().accept(this);
 		return false;
 	}
 	
 	@Override
 	public boolean visit(GotoCaseStatement node) {
 		printIndent();
 		this.buffer.append("goto case ");
 		node.getLabel().accept(this);
 		this.buffer.append(";");
 		return false;
 	}
 	
 	@Override
 	public boolean visit(GotoDefaultStatement node) {
 		printIndent();
 		this.buffer.append("goto default;");
 		return false;
 	}
 	
 	@Override
 	public boolean visit(GotoStatement node) {
 		printIndent();
 		this.buffer.append("goto ");
 		node.getLabel().accept(this);
 		this.buffer.append(";");
 		return false;
 	}
 	
 	@Override
 	public boolean visit(IfStatement node) {
 		printIndent();
 		this.buffer.append("if(");
 		if (node.getArgument() != null) {
 			node.getArgument().accept(this);
 			this.buffer.append(" = ");
 		}
 		node.getExpression().accept(this);
 		this.buffer.append(") ");
 		node.getThenBody().accept(this);
 		if (node.getElseBody() != null) {
 			this.buffer.append(" else ");
 			node.getElseBody().accept(this);
 		}
 		return false;
 	}
 	
 	@Override
 	public boolean visit(IftypeDeclaration node) {
 		visitPreDDocss(node.preDDocs());
 		printIndent();
 		visitModifiers(node.modifiers());
 		this.buffer.append("iftype(");
 		if (node.getTestType() != null) {
 			node.getTestType().accept(this);
 		}
 		if (node.getName() != null) {
 			this.buffer.append(" ");
 			node.getName().accept(this);
 		}
 		if (node.getMatchingType() != null) {
 			switch(node.getKind()) {
 			case EQUALS: this.buffer.append(" = "); break;
 			case EXTENDS: this.buffer.append(" : "); break;
 			case NONE: break;
 			}
 			node.getMatchingType().accept(this);
 		}
 		this.buffer.append(") {\n");
 		this.indent++;
 		visitList(node.thenDeclarations(), LINE_END, EMPTY, LINE_END);
 		this.indent--;
 		this.buffer.append("}");
 		if (!node.elseDeclarations().isEmpty()) {
 			this.buffer.append(" else {\n");
 			this.indent++;
 			visitList(node.elseDeclarations(), LINE_END, EMPTY, LINE_END);
 			this.indent--;
 			this.buffer.append("}");
 		}
 		if (node.getPostDDoc() != null) {
 			this.buffer.append(" ");
 			node.getPostDDoc().accept(this);
 		}
 		return false;
 	}
 	
 	@Override
 	public boolean visit(IftypeStatement node) {
 		printIndent();
 		this.buffer.append("iftype(");
 		node.getTestType().accept(this);
 		if (node.getName() != null) {
 			this.buffer.append(" ");
 			node.getName().accept(this);
 		}
 		if (node.getMatchingType() != null) {
 			switch(node.getKind()) {
 			case EQUALS: this.buffer.append(" = "); break;
 			case EXTENDS: this.buffer.append(" : "); break;
 			case NONE: break;
 			}
 			node.getMatchingType().accept(this);
 		}
 		this.buffer.append(") ");
 		node.getThenBody().accept(this);
 		if (node.getElseBody() != null) {
 			this.buffer.append(" else ");
 			node.getElseBody().accept(this);
 		}
 		return false;
 	}
 	
 	@Override
 	public boolean visit(Import node) {
 		if (node.getAlias() != null) {
 			node.getAlias().accept(this);
 			this.buffer.append(" = ");
 		}
 		node.getName().accept(this);
 		if (!node.selectiveImports().isEmpty()) {
 			this.buffer.append(" : ");
 			visitList(node.selectiveImports(), ", ");
 		}
 		return false;
 	}
 	
 	@Override
 	public boolean visit(ImportDeclaration node) {
 		visitPreDDocss(node.preDDocs());
 		printIndent();
 		visitModifiers(node.modifiers());
 		if (node.isStatic()) {
 			this.buffer.append("static ");
 		}
 		this.buffer.append("import ");
 		visitList(node.imports(), ", ");
 		this.buffer.append(";");
 		if (node.getPostDDoc() != null) {
 			this.buffer.append(" ");
 			node.getPostDDoc().accept(this);
 		}
 		return false;
 	}
 	
 	@Override
 	public boolean visit(InfixExpression node) {
 		node.getLeftOperand().accept(this);
 		this.buffer.append(" ");
 		this.buffer.append(node.getOperator().toString());
 		this.buffer.append(" ");
 		node.getRightOperand().accept(this);
 		return false;
 	}
 	
 	@Override
 	public boolean visit(InvariantDeclaration node) {
 		visitPreDDocss(node.preDDocs());
 		printIndent();
 		visitModifiers(node.modifiers());
 		this.buffer.append("invariant ");
 		node.getBody().accept(this);
 		if (node.getPostDDoc() != null) {
 			this.buffer.append(" ");
 			node.getPostDDoc().accept(this);
 		}
 		return false;
 	}
 	
 	@Override
 	public boolean visit(IsTypeExpression node) {
 		this.buffer.append("is(");
 		node.getType().accept(this);
 		if (node.getName() != null) {
 			this.buffer.append(" ");
 			node.getName().accept(this);
 		}
 		if (node.getSpecialization() != null) {
 			if (node.isSameComparison()) {
 				this.buffer.append(" == ");
 			} else {
 				this.buffer.append(" : ");
 			}
 			node.getSpecialization().accept(this);
 		}
 		this.buffer.append(")");
 		return false;
 	}
 	
 	@Override
 	public boolean visit(IsTypeSpecializationExpression node) {
 		this.buffer.append("is(");
 		node.getType().accept(this);
 		if (node.getName() != null) {
 			this.buffer.append(" ");
 			node.getName().accept(this);
 		}
 		if (node.getSpecialization() != null) {
 			if (node.isSameComparison()) {
 				this.buffer.append(" == ");
 			} else {
 				this.buffer.append(" : ");
 			}
 			this.buffer.append(node.getSpecialization().toString().toLowerCase());
 		}
 		this.buffer.append(")");
 		return false;
 	}
 	
 	@Override
 	public boolean visit(LabeledStatement node) {
 		printIndent();
 		node.getLabel().accept(this);
 		this.buffer.append(": ");
 		node.getBody().accept(this);
 		return false;
 	}
 	
 	@Override
 	public boolean visit(TemplateMixinDeclaration node) {
 		visitPreDDocss(node.preDDocs());
 		printIndent();
 		visitModifiers(node.modifiers());
 		this.buffer.append("mixin ");
 		node.getType().accept(this);
 		if (node.getName() != null) {
 			this.buffer.append(" ");
 			node.getName().accept(this);
 		}
 		this.buffer.append(";");
 		if (node.getPostDDoc() != null) {
 			this.buffer.append(" ");
 			node.getPostDDoc().accept(this);
 		}
 		return false;
 	}
 	
 	@Override
 	public boolean visit(Modifier node) {
 		this.buffer.append(node.getModifierKeyword().toString());
 		return false;
 	}
 	
 	@Override
 	public boolean visit(ModifierDeclaration node) {
 		visitPreDDocss(node.preDDocs());
 		printIndent();
 		visitModifiers(node.modifiers());
 		node.getModifier().accept(this);
 		this.buffer.append(" {\n");
 		this.indent++;
 		visitList(node.declarations(), LINE_END, EMPTY, LINE_END);
 		this.indent--;
 		this.buffer.append("}");
 		if (node.getPostDDoc() != null) {
 			this.buffer.append(" ");
 			node.getPostDDoc().accept(this);
 		}
 		return false;
 	}
 	
 	@Override
 	public boolean visit(ModuleDeclaration node) {
 		visitPreDDocss(node.preDDocs());
 		printIndent();
 		this.buffer.append("module ");
 		node.getName().accept(this);
 		this.buffer.append(";");
 		if (node.getPostDDoc() != null) {
 			this.buffer.append(" ");
 			node.getPostDDoc().accept(this);
 		}
 		return false;
 	}
 	
 	@Override
 	public boolean visit(NewAnonymousClassExpression node) {
 		if (node.getExpression() != null) {
 			node.getExpression().accept(this);
 			this.buffer.append(".");
 		}
 		this.buffer.append("new ");
 		visitList(node.newArguments(), ", ", "(", ") ");
 		this.buffer.append("class ");
 		visitList(node.constructorArguments(), ", ", "(", ") ");
 		visitList(node.baseClasses(), ", ");
 		this.buffer.append(" {\n");
 		this.indent++;
 		visitList(node.declarations(), LINE_END, EMPTY, LINE_END);
 		this.indent--;
 		this.buffer.append("}");
 		return false;
 	}
 	
 	@Override
 	public boolean visit(NewExpression node) {
 		if (node.getExpression() != null) {
 			node.getExpression().accept(this);
 			this.buffer.append(".");
 		}
 		this.buffer.append("new ");
 		visitList(node.newArguments(), ", ", "(", ") ");
 		node.getType().accept(this);
 		visitList(node.constructorArguments(), ", ", "(", ")");
 		return false;
 	}
 	
 	@Override
 	public boolean visit(NullLiteral node) {
 		this.buffer.append("null");
 		return false;
 	}
 	
 	@Override
 	public boolean visit(NumberLiteral node) {
 		this.buffer.append(node.getToken());
 		return false;
 	}
 	
 	@Override
 	public boolean visit(ParenthesizedExpression node) {
 		this.buffer.append("(");
 		node.getExpression().accept(this);
 		this.buffer.append(")");
 		return false;
 	}
 	
 	@Override
 	public boolean visit(PointerType node) {
 		node.getComponentType().accept(this);
 		this.buffer.append("*");
 		return false;
 	}
 	
 	@Override
 	public boolean visit(PostfixExpression node) {
 		node.getOperand().accept(this);
 		this.buffer.append(node.getOperator().toString());
 		return false;
 	}
 	
 	@Override
 	public boolean visit(Pragma node) {
 		this.buffer.append("#\n");
 		return false;
 	}
 	
 	@Override
 	public boolean visit(PragmaDeclaration node) {
 		visitPreDDocss(node.preDDocs());
 		printIndent();
 		visitModifiers(node.modifiers());
 		this.buffer.append("pragma(");
 		node.getName().accept(this);
 		visitList(node.arguments(), ", ", ", ", EMPTY);
 		this.buffer.append(")");
 		if (!node.declarations().isEmpty()) {
 			this.buffer.append(" {\n");
 			this.indent++;
 			visitList(node.declarations(), LINE_END, EMPTY, LINE_END);
 			this.indent--;
 			this.buffer.append("}");
 		}
 		if (node.getPostDDoc() != null) {
 			this.buffer.append(" ");
 			node.getPostDDoc().accept(this);
 		}
 		return false;
 	}
 	
 	@Override
 	public boolean visit(PragmaStatement node) {
 		printIndent();
 		this.buffer.append("pragma(");
 		node.getName().accept(this);
 		visitList(node.arguments(), ", ", ", ", EMPTY);
 		this.buffer.append(")");
 		if (node.getBody() != null) {
 			this.buffer.append(" ");
 			node.getBody().accept(this);
 		} else {
 			this.buffer.append(";");
 		}
 		return false;
 	}
 	
 	@Override
 	public boolean visit(PrefixExpression node) {
 		this.buffer.append(node.getOperator().toString());
 		node.getOperand().accept(this);		
 		return false;
 	}
 	
 	@Override
 	public boolean visit(PrimitiveType node) {
 		this.buffer.append(node.getPrimitiveTypeCode());
 		return false;
 	}
 	
 	@Override
 	public boolean visit(QualifiedName node) {
 		node.appendName(this.buffer);
 		return false;
 	}
 	
 	@Override
 	public boolean visit(QualifiedType node) {
 		if (node.getQualifier() != null) {
 			node.getQualifier().accept(this);
 		}
 		this.buffer.append(".");
 		node.getType().accept(this);
 		return false;
 	}
 	
 	@Override
 	public boolean visit(ReturnStatement node) {
 		printIndent();
 		this.buffer.append("return");
 		if (node.getExpression() != null) {
 			this.buffer.append(" ");
 			node.getExpression().accept(this);
 		}
 		this.buffer.append(";");
 		return false;
 	}
 	
 	@Override
 	public boolean visit(ScopeStatement node) {
 		printIndent();
 		this.buffer.append("scope(");
 		this.buffer.append(node.getEvent().toString().toLowerCase());
 		this.buffer.append(") ");
 		node.getBody().accept(this);
 		return false;
 	}
 	
 	@Override
 	public boolean visit(SelectiveImport node) {
 		if (node.getAlias() != null) {
 			node.getAlias().accept(this);
 			this.buffer.append(" = ");
 		}
 		node.getName().accept(this);
 		return false;
 	}
 	
 	@Override
 	public boolean visit(SimpleName node) {
 		node.appendName(this.buffer);
 		return false;
 	}
 	
 	@Override
 	public boolean visit(SimpleType node) {
 		node.getName().accept(this);
 		return false;
 	}
 	
 	@Override
 	public boolean visit(SliceExpression node) {
 		node.getExpression().accept(this);
 		this.buffer.append("[");
 		if (node.getFromExpression() != null && node.getToExpression() != null) {
 			node.getFromExpression().accept(this);
 			this.buffer.append(" .. ");
 			node.getToExpression().accept(this);
 		}
 		this.buffer.append("]");
 		return false;
 	}
 	
 	@Override
 	public boolean visit(SliceType node) {
 		node.getComponentType().accept(this);
 		this.buffer.append("[");
 		if (node.getFromExpression() != null && node.getToExpression() != null) {
 			node.getFromExpression().accept(this);
 			this.buffer.append(" .. ");
 			node.getToExpression().accept(this);
 		}
 		this.buffer.append("]");
 		return false;
 	}
 	
 	@Override
 	public boolean visit(StaticArrayType node) {
 		node.getComponentType().accept(this);
 		this.buffer.append("[");
 		node.getSize().accept(this);
 		this.buffer.append("]");
 		return false;
 	}
 	
 	@Override
 	public boolean visit(StaticAssert node) {
 		visitPreDDocss(node.preDDocs());
 		printIndent();
 		visitModifiers(node.modifiers());
 		this.buffer.append("static assert(");
 		node.getExpression().accept(this);
 		if (node.getMessage() != null) {
 			this.buffer.append(", ");
 			node.getMessage().accept(this);
 		}
 		this.buffer.append(")");
 		if (node.getPostDDoc() != null) {
 			this.buffer.append(" ");
 			node.getPostDDoc().accept(this);
 		}
 		return false;
 	}
 	
 	@Override
 	public boolean visit(StaticAssertStatement node) {
 		printIndent();
 		node.getStaticAssert().accept(this);
 		this.buffer.append(";");
 		return false;
 	}
 	
 	@Override
 	public boolean visit(StaticIfDeclaration node) {
 		visitPreDDocss(node.preDDocs());
 		printIndent();
 		visitModifiers(node.modifiers());
 		this.buffer.append("static if(");
 		node.getExpression().accept(this);
 		this.buffer.append(") {\n");
 		this.indent++;
 		visitList(node.thenDeclarations(), LINE_END, EMPTY, LINE_END);
 		this.indent--;
 		this.buffer.append("}");
 		if (!node.elseDeclarations().isEmpty()) {
 			this.buffer.append(" else {\n");
 			this.indent++;
 			visitList(node.elseDeclarations(), LINE_END, EMPTY, LINE_END);
 			this.indent--;
 			this.buffer.append("}");
 		}
 		if (node.getPostDDoc() != null) {
 			this.buffer.append(" ");
 			node.getPostDDoc().accept(this);
 		}
 		return false;
 	}
 	
 	@Override
 	public boolean visit(StaticIfStatement node) {
 		printIndent();
 		this.buffer.append("static if(");
 		node.getExpression().accept(this);
 		this.buffer.append(") ");
 		node.getThenBody().accept(this);
 		if (node.getElseBody() != null) {
 			this.buffer.append(" else ");
 			node.getElseBody().accept(this);
 		}
 		return false;
 	}
 
 	@Override
 	public boolean visit(StringLiteral node) {
 		this.buffer.append(node.getEscapedValue());
 		return false;
 	}
 	
 	@Override
 	public boolean visit(StringsExpression node) {
 		visitList(node.stringLiterals(), " ");
 		return false;
 	}
 	
 	@Override
 	public boolean visit(StructInitializer node) {
 		this.buffer.append("{ ");
 		visitList(node.fragments(), ", ");
 		this.buffer.append("}");
 		return false;
 	}
 	
 	@Override
 	public boolean visit(StructInitializerFragment node) {
 		if (node.getName() != null) {
 			node.getName().accept(this);
 			this.buffer.append(": ");
 		}
 		node.getInitializer().accept(this);
 		return false;
 	}
 	
 	@Override
 	public boolean visit(SuperLiteral node) {
 		this.buffer.append("super");
 		return false;
 	}
 	
 	@Override
 	public boolean visit(SwitchStatement node) {
 		printIndent();
 		this.buffer.append("switch(");
 		node.getExpression().accept(this);
 		this.buffer.append(") ");
 		node.getBody().accept(this);
 		return false;
 	}
 	
 	@Override
 	public boolean visit(SynchronizedStatement node) {
 		printIndent();
 		this.buffer.append("synchronized");
 		if (node.getExpression() != null) {
 			this.buffer.append("(");
 			node.getExpression().accept(this);
 			this.buffer.append(")");
 		}
 		this.buffer.append(" ");
 		node.getBody().accept(this);
 		return false;
 	}
 	
 	@Override
 	public boolean visit(TemplateDeclaration node) {
 		visitPreDDocss(node.preDDocs());
 		printIndent();
 		visitModifiers(node.modifiers());
 		this.buffer.append("template ");
 		node.getName().accept(this);
 		visitList(node.templateParameters(), ", ", "(", ")");
 		this.buffer.append(" {\n");
 		this.indent++;
 		visitList(node.declarations(), LINE_END, EMPTY, LINE_END);
 		this.indent--;
 		this.buffer.append("}");
 		if (node.getPostDDoc() != null) {
 			this.buffer.append(" ");
 			node.getPostDDoc().accept(this);
 		}
 		return false;
 	}
 	
 	@Override
 	public boolean visit(TemplateType node) {
 		node.getName().accept(this);
 		this.buffer.append("!");
 		visitList(node.arguments(), ", ", "(", ")");
 		return false;
 	}
 	
 	@Override
 	public boolean visit(ThisLiteral node) {
 		this.buffer.append("this");
 		return false;
 	}
 	
 	@Override
 	public boolean visit(ThrowStatement node) {
 		printIndent();
 		this.buffer.append("throw ");
 		node.getExpression().accept(this);
 		this.buffer.append(";");
 		return false;
 	}
 	
 	@Override
 	public boolean visit(TryStatement node) {
 		printIndent();
 		this.buffer.append("try ");
 		node.getBody().accept(this);
 		this.buffer.append(LINE_END);
 		visitList(node.catchClauses(), LINE_END, EMPTY, LINE_END);
 		if (node.getFinally() != null) {
 			this.buffer.append(" finally ");
 			node.getFinally().accept(this);
 		}
 		return false;
 	}
 	
 	@Override
 	public boolean visit(TupleTemplateParameter node) {
 		node.getName().accept(this);
 		this.buffer.append(" ...");
 		return false;
 	}
 	
 	@Override
 	public boolean visit(TypedefDeclaration node) {
 		visitPreDDocss(node.preDDocs());
 		printIndent();
 		visitModifiers(node.modifiers());
 		this.buffer.append("typedef ");
 		node.getType().accept(this);
 		this.buffer.append(" ");
 		visitList(node.fragments(), ", ");
 		this.buffer.append(";");
 		if (node.getPostDDoc() != null) {
 			this.buffer.append(" ");
 			node.getPostDDoc().accept(this);
 		}
 		return false;
 	}
 	
 	@Override
 	public boolean visit(TypedefDeclarationFragment node) {
 		node.getName().accept(this);
 		if (node.getInitializer() != null) {
 			this.buffer.append(" = ");
 			node.getInitializer().accept(this);
 		}
 		return false;
 	}
 	
 	@Override
 	public boolean visit(TypeDotIdentifierExpression node) {
 		node.getType().accept(this);
 		this.buffer.append(".");
 		node.getName().accept(this);
 		return false;
 	}
 	
 	@Override
 	public boolean visit(TypeExpression node) {
 		node.getType().accept(this);
 		return false;
 	}
 	
 	@Override
 	public boolean visit(TypeidExpression node) {
 		this.buffer.append("typeid(");
 		node.getType().accept(this);
 		this.buffer.append(")");
 		return false;
 	}
 	
 	@Override
 	public boolean visit(TypeofType node) {
 		this.buffer.append("typeof(");
 		node.getExpression().accept(this);
 		this.buffer.append(")");
 		return false;
 	}
 	
 	@Override
 	public boolean visit(TypeTemplateParameter node) {
 		node.getName().accept(this);
 		if (node.getSpecificType() != null) {
 			this.buffer.append(" : ");
 			node.getSpecificType().accept(this);
 		}
 		if (node.getDefaultType() != null) {
 			this.buffer.append(" = ");
 			node.getDefaultType().accept(this);
 		}
 		return false;
 	}
 	
 	@Override
 	public boolean visit(UnitTestDeclaration node) {
 		visitPreDDocss(node.preDDocs());
 		printIndent();
 		visitModifiers(node.modifiers());
 		this.buffer.append("unittest ");
 		node.getBody().accept(this);
 		if (node.getPostDDoc() != null) {
 			this.buffer.append(" ");
 			node.getPostDDoc().accept(this);
 		}
 		return false;
 	}
 	
 	@Override
 	public boolean visit(ValueTemplateParameter node) {
 		node.getName().accept(this);
 		if (node.getSpecificValue() != null) {
 			this.buffer.append(" : ");
 			node.getSpecificValue().accept(this);
 		}
 		if (node.getDefaultValue() != null) {
 			this.buffer.append(" = ");
 			node.getDefaultValue().accept(this);
 		}
 		return false;
 	}
 	
 	@Override
 	public boolean visit(VariableDeclaration node) {
 		visitPreDDocss(node.preDDocs());
 		printIndent();
 		visitModifiers(node.modifiers());
 		if (node.getType() != null) {
 			node.getType().accept(this);
 			this.buffer.append(" ");
 		}
 		visitList(node.fragments(), ", ");
 		this.buffer.append(";");
 		if (node.getPostDDoc() != null) {
 			this.buffer.append(" ");
 			node.getPostDDoc().accept(this);
 		}
 		return false;
 	}
 	
 	@Override
 	public boolean visit(VariableDeclarationFragment node) {
 		node.getName().accept(this);
 		if (node.getInitializer() != null) {
 			this.buffer.append(" = ");
 			node.getInitializer().accept(this);
 		}
 		return false;
 	}
 	
 	@Override
 	public boolean visit(Version node) {
 		this.buffer.append(node.getValue());
 		return false;
 	}
 	
 	@Override
 	public boolean visit(VersionAssignment node) {
 		visitPreDDocss(node.preDDocs());
 		printIndent();
 		visitModifiers(node.modifiers());
 		this.buffer.append("version = ");
 		node.getVersion().accept(this);
 		this.buffer.append(";");
 		if (node.getPostDDoc() != null) {
 			this.buffer.append(" ");
 			node.getPostDDoc().accept(this);
 		}
 		return false;
 	}
 	
 	@Override
 	public boolean visit(VersionDeclaration node) {
 		visitPreDDocss(node.preDDocs());
 		printIndent();
 		visitModifiers(node.modifiers());
 		this.buffer.append("version");
 		if (node.getVersion() != null) {
 			this.buffer.append("(");
 			node.getVersion().accept(this);
 			this.buffer.append(")");
 		}
 		this.buffer.append(" {\n");
 		this.indent++;
 		visitList(node.thenDeclarations(), LINE_END, EMPTY, LINE_END);
 		this.indent--;
 		this.buffer.append("}");
 		if (!node.elseDeclarations().isEmpty()) {
 			this.buffer.append(" else {\n");
 			this.indent++;
 			visitList(node.elseDeclarations(), LINE_END, EMPTY, LINE_END);
 			this.indent--;
 			this.buffer.append("}");
 		}
 		if (node.getPostDDoc() != null) {
 			this.buffer.append(" ");
 			node.getPostDDoc().accept(this);
 		}
 		return false;
 	}
 	
 	@Override
 	public boolean visit(VersionStatement node) {
 		printIndent();
 		this.buffer.append("version");
 		if (node.getVersion() != null) {
 			this.buffer.append("(");
 			node.getVersion().accept(this);
 			this.buffer.append(")");
 		}
 		this.buffer.append(" ");
 		node.getThenBody().accept(this);
 		if (node.getElseBody() != null) {
 			this.buffer.append(" else ");
 			node.getElseBody().accept(this);
 		}
 		return false;
 	}
 	
 	@Override
 	public boolean visit(VoidInitializer node) {
 		this.buffer.append("void");
 		return false;
 	}
 	
 	@Override
 	public boolean visit(VolatileStatement node) {
 		printIndent();
 		this.buffer.append("volatile ");
 		node.getBody().accept(this);
 		return false;
 	}
 	
 	@Override
 	public boolean visit(WhileStatement node) {
 		printIndent();
 		this.buffer.append("while(");
 		node.getExpression().accept(this);
 		this.buffer.append(") ");
 		node.getBody().accept(this);
 		return false;
 	}
 	
 	@Override
 	public boolean visit(WithStatement node) {
 		printIndent();
 		this.buffer.append("with(");
 		node.getExpression().accept(this);
 		this.buffer.append(") ");
 		node.getBody().accept(this);
 		return false;
 	}
 	
 	@Override
 	public boolean visit(FileImportExpression node) {
 		printIndent();
 		this.buffer.append("import(");
 		node.getExpression().accept(this);
 		this.buffer.append(");");
 		return false;
 	}
 	
 	@Override
 	public boolean visit(MixinDeclaration node) {
 		printIndent();
 		this.buffer.append("mixin(");
 		node.getExpression().accept(this);
 		this.buffer.append(");");
 		return false;
 	}
 	
 	@Override
 	public boolean visit(MixinExpression node) {
 		printIndent();
 		this.buffer.append("mixin(");
 		node.getExpression().accept(this);
 		this.buffer.append(");");
 		return false;
 	}
 	
 	@Override
 	public boolean visit(EmptyStatement node) {
 		printIndent();
 		this.buffer.append(";");
 		return false;
 	}
 	
 }
