 package org.jmlspecs.jml4.boogie.ast;
 
 import java.util.ArrayList;
 
 public class DecoratorVisitor extends Visitor {
 	public boolean visit(Program term) {
 		for (int i = 0; i < term.getTypes().size(); i++) {
 			resolveTypes((TypeDeclaration)term.getTypes().get(i));
 		}
 
 		term.getStatements().add(new Comment("/*!BOOGIESTART!*/")); //$NON-NLS-1$
 		
 		resolveGlobals(term.getGlobals());
 		for (int i = 0; i < term.getProcedures().size(); i++) {
 			((BoogieNode)term.getProcedures().get(i)).traverse(this);
 		}
 		return false;
 	}
 	
 	public boolean visit(Procedure term) {
 		// traverse statements first (might generate new locals here)
 		for (int i = 0; i < term.getStatements().size(); i++) {
 			((BoogieNode)term.getStatements().get(i)).traverse(this);
 		}
 
 		for (int i = 0, count = 0; i < term.getArguments().size(); i++) {
 			VariableDeclaration var = (VariableDeclaration)term.getArguments().get(i);
 			BinaryExpression expr = typeCheckExpression(var);
 			if (expr != null) {
 				var.getScope().getProcedureScope().getRequires().add(count++, expr);
 			}
 		}
 
 		resolveLocals(term.getLocals());
 		
 		return false;
 	}
 	
 	public boolean visit(Assignment term) {
 		VariableDeclaration decl = term.getScope().lookupVariable(term.getVariable().getName());
 		if (decl != null && !decl.isLocal()) {
 			Procedure proc = term.getScope().getProcedureScope();
 			// don't add duplicates
 			for (int i = 0; i < proc.getModifies().size(); i++) {
 				if (proc.getModifies().get(i) instanceof VariableReference) {
 					VariableReference ref = (VariableReference)proc.getModifies().get(i);
 					if (ref.getName().equals(decl.getName().getName())) return false;
 				}
 			}
 			proc.getModifies().add(decl.getName());
 		}
 		return true;
 	}
 	
 	public boolean visit(CallStatement term) {
 		if (term.getOutVar() == null) {
			Procedure calledProcedure = term.getScope().lookupProcedure(term.getProcedureName());
 			if (calledProcedure != null) { // calledProcedure should not be null
				Procedure proc = term.getScope().getProcedureScope();
				
 				// check if procedure is called without outvar when spec defines one
 				// Boogie needs any method call to assign to as many out vars as in the procedure
 				// declaration
 				if (calledProcedure.getReturnType() != null) {
 					// need to declare a variable
 					String varName = "$unused_" + proc.getLocals().size(); //$NON-NLS-1$
 					VariableDeclaration decl = new VariableDeclaration(
 							new VariableReference(varName, null, proc),  
 							calledProcedure.getReturnType(), proc);
 					proc.addVariable(decl);
 					term.setOutVar(decl.getName());
 				}
				
				// add all modifies, requires and ensures clauses to callee
				proc.getModifies().addAll(calledProcedure.getModifies());
				//proc.getEnsures().addAll(calledProcedure.getEnsures());
				//proc.getRequires().addAll(calledProcedure.getRequires());
 			}
 		}
 		return true;
 	}
 	
 	private void resolveTypes(TypeDeclaration term) {
 		ArrayList stmts = term.getScope().getProgramScope().getStatements();
 		stmts.add(new ConstStatement(term, null, term.getScope()));
 		stmts.add(new Axiom(new BinaryExpression(new TokenLiteral(
 				term.getType().getTypeName()), "<:", //$NON-NLS-1$
 				new TokenLiteral(term.getSuperType().getTypeName()), 
 				false, null, term.getScope()), null, null, null, term.getScope()));
 	}
 
 	private void resolveLocals(ArrayList vars) {
 		for (int i = 0; i < vars.size(); i++) {
 			VariableDeclaration var = (VariableDeclaration)vars.get(i);
 			Procedure proc = var.getScope().getProcedureScope();
 			proc.getStatements().add(i, new VariableStatement(var, var.getJavaNode(), proc));
 		}
 		for (int i = 0, count = 0; i < vars.size(); i++) {
 			VariableDeclaration var = (VariableDeclaration)vars.get(i);
 			BinaryExpression expr = typeCheckExpression(var);
 			if (expr != null) {
 				Procedure proc = var.getScope().getProcedureScope();
 				proc.getStatements().add(vars.size() + count++, new AssumeStatement(expr, var.getJavaNode(), proc));
 			}
 		}
 	}
 	
 	private void resolveGlobals(ArrayList vars) {
 		for (int i = 0; i < vars.size(); i++) {
 			VariableDeclaration var = (VariableDeclaration)vars.get(i);
 			Program prog = var.getScope().getProgramScope();
 			prog.getStatements().add(new VariableStatement(var, var.getJavaNode(), prog));
 		}
 		for (int i = 0; i < vars.size(); i++) {
 			VariableDeclaration var = (VariableDeclaration)vars.get(i);
 			BinaryExpression expr = typeCheckExpression(var);
 			if (expr != null) {
 				Program prog = var.getScope().getProgramScope();
 				prog.getStatements().add(new Axiom(expr, null, null, var.getJavaNode(), prog));
 			}
 		}
 	}
 
 	private BinaryExpression typeCheckExpression(VariableDeclaration var) {
 		if (!var.isLocal() && !var.isConstant()) return null; // TODO make this work for globals non-const vars?
 		// type requirements
 		// TODO add support for array types
 		if (!(var.getType() instanceof MapTypeReference) && !var.getType().isNative()) {
 			return new BinaryExpression(new FunctionCall("$dtype", //$NON-NLS-1$ 
 					new Expression[] { var.getName() }, var.getJavaNode(), var.getScope()), 
 					"==", new TokenLiteral(var.getType().getTypeName()), false, //$NON-NLS-1$
 					var.getName().getJavaNode(), var.getScope()); 
 		}
 		return null;
 		
 	}
 }
