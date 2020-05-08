 package org.eclipse.dltk.ruby.ast;
 
 import org.eclipse.dltk.ast.ASTVisitor;
 import org.eclipse.dltk.ast.declarations.TypeDeclaration;
 import org.eclipse.dltk.ast.statements.Block;
 import org.eclipse.dltk.ast.statements.CompoundStatement;
 import org.eclipse.dltk.ast.statements.Statement;
 
 public class RubyModuleDeclaration extends TypeDeclaration {
 
 	private Statement name;
 	
 	public RubyModuleDeclaration(Statement name, Block body, int start, int end) {
 		super("", name.sourceStart(), name.sourceEnd(), start, end);
 		CompoundStatement el = new CompoundStatement();
 		this.setSuperClasses(el);
 		this.name = name;
 		this.fBody = body;
 		setStart(start);
 		setEnd(end);
 	}
 	
 	public Statement getClassName() {
 		return name;
 	}
 
 	public void traverse( ASTVisitor visitor ) throws Exception {
 
 		if( visitor.visit( this ) ) {
 			if( this.getClassName() != null ) {
 				this.getClassName().traverse( visitor );
 			}
 			if( this.getBody() != null ) {
 				getBody().traverse( visitor );
 			}
			visitor.endvisit( this );
 		}
 	}
 	
 }
