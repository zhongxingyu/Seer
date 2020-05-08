 package org.jmlspecs.jml4.boogie.ast;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 
 import org.eclipse.jdt.internal.compiler.ast.ASTNode;
 import org.jmlspecs.jml4.boogie.BoogieSource;
 
 public class Procedure extends BoogieNode implements Scope {
 	private ArrayList/*<VariableDeclaration>*/ locals;
 	private ArrayList/*<VariableDeclaration>*/ arguments;
 	private ArrayList/*<Statement>*/ statements;
 	private ArrayList/*<Expression>*/ requires;
 	private ArrayList/*<Expression>*/ ensures;
 	private ArrayList/*<MapVariableReference>*/ modifies;
 	private String name;
 	private TypeReference returnType;
 
	// for varaible generation
 	private final static String charmap = "abcdefghijklmnopqrstuvwxyz"; //$NON-NLS-1$
 	private String generatedSymbol = ""; //$NON-NLS-1$
 	
 	public Procedure(String name, TypeReference returnType, ASTNode javaNode, Program scope) {
 		super(javaNode, scope);
 		this.returnType = returnType;
 		this.name = name;
 		this.locals = new ArrayList();
 		this.arguments = new ArrayList();
 		this.statements = new ArrayList();
 		this.ensures = new ArrayList();
 		this.requires = new ArrayList();
 		this.modifies = new ArrayList();
 	}
 	
 	public TypeReference getReturnType() {
 		return returnType;
 	}
 	
 	public ArrayList getLocals() {
 		return locals;
 	}
 	
 	public ArrayList getArguments() {
 		return arguments;
 	}
 	
 	public ArrayList getStatements() {
 		return statements;
 	}
 
 	public ArrayList getModifies() {
 		return modifies;
 	}
 
 	public ArrayList getRequires() {
 		return requires;
 	}
 
 	public ArrayList getEnsures() {
 		return ensures;
 	}
 
 	public String getName() {
 		return name;
 	}
 	
 	private void printArguments(BoogieSource out) {
 		out.append(TOKEN_LPAREN);
 		for (int i = 0; i < getArguments().size(); i++) {
 			((VariableDeclaration)getArguments().get(i)).toBuffer(out);
 			if (i < getArguments().size() - 1) {
 				out.append(TOKEN_COMMA + TOKEN_SPACE);
 			}
 		}
 		out.append(TOKEN_RPAREN + TOKEN_SPACE);
 	}
 	
 	private void printReturns(BoogieSource out) {
 		if (getReturnType() == null) return;
 		out.append("returns" + TOKEN_SPACE + TOKEN_LPAREN); //$NON-NLS-1$
 		out.append(TOKEN_RESULT + TOKEN_COLON + TOKEN_SPACE);
 		getReturnType().toBuffer(out);
 		out.append(TOKEN_RPAREN + TOKEN_SPACE);
 	}
 
 	private void printSpec(BoogieSource out, String specName, ArrayList exprs) {
 		if (exprs.size() == 0) return;
 		for (int i = 0; i < exprs.size(); i++) {
 			Expression expr = (Expression)exprs.get(i);
 			out.append(specName + TOKEN_SPACE, expr.getJavaNode());
 			expr.toBuffer(out);
 			out.append(TOKEN_SEMICOLON + TOKEN_SPACE);
 		}
 	}
 	
 	private void printModifies(BoogieSource out) {
 		if (getModifies().size() == 0) return;
 		HashMap map = new HashMap();
 		for (int i = 0; i < getModifies().size(); i++) {
 			VariableReference ref = (VariableReference)getModifies().get(i);
 			
 			// modifies must be unique otherwise Boogie crashes!
 			if (map.get(ref.getName()) != null) continue; 
 			map.put(ref.getName(), new Boolean(true));
 			
 			out.append("modifies" + TOKEN_SPACE, ref.getJavaNode()); //$NON-NLS-1$
 			out.append(ref.getName() + TOKEN_SEMICOLON + TOKEN_SPACE);
 		}
 	}
 
 	public void toBuffer(BoogieSource out) {
 		out.append("procedure" + TOKEN_SPACE + getName()); //$NON-NLS-1$
 
 		printArguments(out);
 		printReturns(out);
 		printModifies(out);
 		printSpec(out, "requires", getRequires()); //$NON-NLS-1$
 		printSpec(out, "ensures", getEnsures()); //$NON-NLS-1$
 		
 		// statements
 		out.appendLine(TOKEN_LBRACE);
 		out.increaseIndent();
 		for (int i = 0; i < statements.size(); i++) {
 			Statement stmt = (Statement)statements.get(i);
 			if (stmt instanceof RemoveLocal) {
 				// FIXME this is not a good idea! See RemoveLocal.java
 				// for more information. Doing this in toBuffer destroys
 				// the locals map during traversal and makes it so this
 				// node will not be traversed in the same way twice.
 				((RemoveLocal)stmt).removeLocal();
 			}
 			stmt.toBuffer(out);
 		}
 		out.decreaseIndent();
 		out.append(TOKEN_EMPTY, getJavaNode());
 		out.appendLine(TOKEN_RBRACE);
 	}
 
 	public TypeDeclaration lookupType(String typeName) {
 		return getProgramScope().lookupType(typeName);
 	}
 
 	public VariableDeclaration lookupVariable(String variableName) {
 		for (int i = 0; i < getArguments().size(); i++) {
 			VariableDeclaration var = (VariableDeclaration)getArguments().get(i);
 			if (var.getName().getName().equals(variableName)) {
 				return var; 
 			}
 		}
 
 		for (int i = 0; i < getLocals().size(); i++) {
 			VariableDeclaration var = (VariableDeclaration)getLocals().get(i);
 			if (var.getName().getName().equals(variableName)) {
 				return var; 
 			}
 		}
 		
 		return getProgramScope().lookupVariable(variableName);
 	}
 	
 	public Procedure lookupProcedure(String procName) {
 		return getProgramScope().lookupProcedure(procName);
 	}
 
 	public void addType(TypeDeclaration type) {
 		getScope().addType(type);
 	}
 
 	public void addVariable(VariableDeclaration decl) {
 		getLocals().add(decl);
 		
 		if (!(decl.getName() instanceof VariableLengthReference)) {
 			registerVariable(decl);
 		}
 	}
 	
 	public void registerVariable(VariableDeclaration decl) {
 		if (!decl.getName().getName().equals("this")) { //$NON-NLS-1$
 			decl.setShortName(generateSymbol());
 		}
 	}
 
 	public void traverse(Visitor visitor) {
 		if (visitor.visit(this)) {
 			for (int i = 0; i < getArguments().size(); i++) {
 				((BoogieNode)getArguments().get(i)).traverse(visitor);
 			}
 			if (getReturnType() != null) {
 				getReturnType().traverse(visitor);
 			}
 			for (int i = 0; i < getModifies().size(); i++) {
 				((BoogieNode)getModifies().get(i)).traverse(visitor);
 			}
 			for (int i = 0; i < getRequires().size(); i++) {
 				((BoogieNode)getRequires().get(i)).traverse(visitor);
 			}
 			for (int i = 0; i < getEnsures().size(); i++) {
 				((BoogieNode)getEnsures().get(i)).traverse(visitor);
 			}
 			for (int i = 0; i < getLocals().size(); i++) {
 				((BoogieNode)getLocals().get(i)).traverse(visitor);
 			}
 			for (int i = 0; i < getStatements().size(); i++) {
 				((BoogieNode)getStatements().get(i)).traverse(visitor);
 			}
 		}
 		visitor.endVisit(this);
 	}
 
 	public Procedure getProcedureScope() {
 		return this;
 	}
 
 	public Program getProgramScope() {
 		return (Program)getScope();
 	}
 	
 	/**
 	 * Generates a new unique symbol name, also storing it as {@link #generatedSymbol}.
 	 *  
 	 * @return the next symbol value (the value of {@link #generatedSymbol})
 	 */
 	private synchronized String generateSymbol() {
 		if (generatedSymbol == "") { //$NON-NLS-1$
 			generatedSymbol = new String(new char[]{charmap.charAt(0)}, 0, 1);
 			return generatedSymbol;
 		}
 		
 		char[] sym = generatedSymbol.toCharArray();
 		for (int symindex = sym.length - 1; symindex >= 0; symindex--) {
 			char c = sym[symindex];
 			int index = charmap.indexOf(c);
 			if (index + 1 >= charmap.length()) {
 				c = charmap.charAt(0);
 				if (symindex == 0) {
 					// increment the string length ("zzz" turns into "aaaa")
 					sym = new char[sym.length + 1];
 					for (int i = 0; i < sym.length; i++) {
 						sym[i] = c;
 					}
 					break;
 				}
 				
 				// make everything else "a" ("azzz" goes to "baaa")
 				for (int i = symindex; i < sym.length; i++) { 
 					sym[i] = c;
 				}
 			}
 			else {
 				sym[symindex] = charmap.charAt(index + 1);
 				break;
 			}
 		}
 		
 		generatedSymbol = new String(sym);
 		return generatedSymbol;
 	}
 }
