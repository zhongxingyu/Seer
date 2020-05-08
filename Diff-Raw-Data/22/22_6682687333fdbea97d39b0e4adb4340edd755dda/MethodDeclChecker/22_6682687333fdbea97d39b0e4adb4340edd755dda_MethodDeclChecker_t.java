 package ca.uwaterloo.joos.weeder;
 
 import ca.uwaterloo.joos.ast.ASTNode;
 import ca.uwaterloo.joos.ast.Modifiers.Modifier;
 import ca.uwaterloo.joos.ast.decl.ClassDeclaration;
 import ca.uwaterloo.joos.ast.decl.MethodDeclaration;
 import ca.uwaterloo.joos.ast.visitor.MethodDeclVisitor;
 
 public class MethodDeclChecker extends MethodDeclVisitor {
 
 	public MethodDeclChecker() {
 
 	}
 
 	@Override
 	protected void visitMethodDecl(MethodDeclaration node) throws Exception {
 		ASTNode astNode = node;
		Boolean insideClass = true;
 		if (node.getModifiers().getModifiers().contains(Modifier.ABSTRACT)) {
 			ClassDeclaration classDeclNode = null;
			while (!(astNode instanceof ClassDeclaration)) {
				if (astNode.getParent() != null) {
					astNode = astNode.getParent();
				} else {
					insideClass = false;
					break;
				}
 			}
			if (insideClass) {
				classDeclNode = (ClassDeclaration) astNode;
				if (!(classDeclNode.getModifiers().getModifiers().contains(Modifier.ABSTRACT))) {
					throw new Exception("class contains abstract Method must be abstract");
				}
 			}
			
 
 		}
 
 	}
 
 }
