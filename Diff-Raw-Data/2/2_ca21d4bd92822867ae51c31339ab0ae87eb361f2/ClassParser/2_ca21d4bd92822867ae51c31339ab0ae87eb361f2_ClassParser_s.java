 package org.ita.testrefactoring.astparser;
 
 import org.eclipse.jdt.core.dom.ASTVisitor;
 import org.eclipse.jdt.core.dom.FieldDeclaration;
 import org.eclipse.jdt.core.dom.MethodDeclaration;
 import org.ita.testrefactoring.metacode.Class;
 import org.ita.testrefactoring.metacode.ParserException;
 import org.ita.testrefactoring.metacode.Type;
 
 class ClassParser implements ASTTypeParser<ASTClass> {
 	
 	private class ClassVisitor extends ASTVisitor {
 
 		private ASTClass clazz;
 
 		public void setClass(ASTClass clazz) {
 			this.clazz = clazz;
 		}
 		
 		@Override
 		public boolean visit(FieldDeclaration node) {
 			// TODO: Continuar daqui
 			ASTField field = clazz.createField(node.toString());
 			
 			return false;
 		}
 		
 		@Override
 		public boolean visit(MethodDeclaration node) {
 			return false;
 		}
 	}
 
 	private ASTClass clazz;
 
 	@Override
 	public void setType(ASTClass type) {
 		this.clazz = type;
 	}
 
 	@Override
 	public void parse() throws ParserException {
 		ClassVisitor visitor = new ClassVisitor();
 		
 		visitor.setClass(clazz);
 		
 		String superClassName;
 		
 		org.eclipse.jdt.core.dom.Type superclassNode = clazz.getASTObject().getSuperclassType();
 		
 		if (superclassNode == null) {
 			superClassName = "java.lang.Object";
 		} else {
 			superClassName = superclassNode.resolveBinding().getQualifiedName();
 		}
 		
 		ASTEnvironment environment = clazz.getPackage().getEnvironment();
 		Type superClass = environment.getTypeCache().get(superClassName);
 		
 		if (superClass == null) {
//			environment.createDummyClass(superClassName);
 		}
 		
 		if ((superClass.getKind() != TypeKind.CLASS) && (superClass.getKind() != TypeKind.UNKNOWN)) {
 			throw new ParserException("Super classe de \"" + clazz.getQualifiedName() + "\" inválida (\"" + superClass.getQualifiedName() + ")");
 		}
 		
 		// Aqui superClass deve ser uma classe, já que getKind devolveu CLASS...
 		clazz.setParent((Class) superClass);
 		
 		clazz.getASTObject().accept(visitor);
 	}
 
 }
