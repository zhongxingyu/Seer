 package superintents.util;
 
 import org.eclipse.jdt.core.dom.ASTVisitor;
 import org.eclipse.jdt.core.dom.MethodDeclaration;
 
 public class MethodASTVisitor extends ASTVisitor {
 
 	MethodDeclaration method = null;
 	String name;
 
 	public MethodASTVisitor(String name) {
 		this.name = name;
 	}
 
 	public boolean visit(MethodDeclaration node) {
 		if (node.getName().toString().equals(name))
 			method = node;
 		return true;
 	}
 
	public MethodDeclaration getMethod() {
 		return method;
 	}
 }
