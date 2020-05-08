 package ca.uwaterloo.joos.symboltable;
 
 import java.util.List;
 
 import ca.uwaterloo.joos.ast.ASTNode;
 import ca.uwaterloo.joos.ast.ASTNode.ChildTypeUnmatchException;
 import ca.uwaterloo.joos.ast.FileUnit;
 import ca.uwaterloo.joos.ast.decl.ImportDeclaration;
 import ca.uwaterloo.joos.ast.decl.MethodDeclaration;
 import ca.uwaterloo.joos.ast.decl.OnDemandImport;
 import ca.uwaterloo.joos.ast.decl.PackageDeclaration;
 import ca.uwaterloo.joos.ast.decl.SingleImport;
 import ca.uwaterloo.joos.ast.decl.TypeDeclaration;
 import ca.uwaterloo.joos.ast.visitor.ASTVisitor;
 
 public class DeepDeclVisitor extends SemanticsVisitor {
 
 	public DeepDeclVisitor(SymbolTable table) {
 		super(table);
 	}
 
 	public void willVisit(ASTNode node) throws Exception {
 
 		if (node instanceof PackageDeclaration) {
 			PackageDeclaration PNode = (PackageDeclaration) node;
 			String name = PNode.getPackageName();
 			
 			// Get the symbol table for the given package 
 			// Create one if not exists
 			Scope table = this.table.getScope(name);
 			
 			// Push current scope into the view stack
 			this.pushScope(table);
 		} else if (node instanceof TypeDeclaration) {
 			Scope currentScope = this.getCurrentScope();
 			String name = ((TypeDeclaration) node).getIdentifier();
 			name = currentScope.getName() + "." + name + "{}";
 			
 			// Second: get the class description scope
 			Scope table = this.table.getScope(name);
 			
 			table.appendScope(currentScope, 10);
 			
 			// Add java.lang implicitly
 			Scope javaLang = this.table.getScope("java.lang");
 			if(javaLang != null) {
 				table.addPublicMembers(javaLang, 20);
 			}
 			else {
 //				throw new Exception("Missing java.lang");
 			}
 			
 			List<ImportDeclaration> imports = ((FileUnit) node.getParent()).getImportDeclarations();
 			for(ImportDeclaration anImport: imports) {
 				if(anImport instanceof SingleImport) {
					String domain = anImport.getImportName().getName() + "{}";
					if(this.table.containScope(domain)) {
						table.addPublicMembers(this.table.getScope(domain), 100);
 					}
 					else {
 						throw new Exception("Unknown Single Import " + anImport.getIdentifier());
 					}
 				} else if(anImport instanceof OnDemandImport) {
					String domain = anImport.getImportName().getName();
					if(this.table.containScope(domain)) {
						table.addPublicMembers(this.table.getScope(domain), 90);
 					}
 					else {
 						throw new Exception("Unknown Single Import " + anImport.getIdentifier());
 					}
 				}
 			}
 			
 			// Push current scope into the view stack
 			this.pushScope(table);
 		} else if (node instanceof MethodDeclaration) {
 			// Make a new symbol table which builds
 			String name = this.getCurrentScope().signatureOfMethod((MethodDeclaration) node);
 			Scope scope = this.table.getScope(name);
 			
 			scope.appendScope(this.getCurrentScope(), 0);
 			
 			this.pushScope(scope);
 		}
 		
 	}
 
 	public void didVisit(ASTNode node) {
 		if (node instanceof TypeDeclaration) {
 			this.popScope();
 		} else if (node instanceof MethodDeclaration) {
 			this.popScope();
 		}
 	}
 
 	public boolean visit(ASTNode node) throws ChildTypeUnmatchException, Exception {
 		if (node instanceof MethodDeclaration) {
 			ASTVisitor blockVisitor = new BlockVisitor(this.viewStack, this.table);
 			node.accept(blockVisitor);
 			
 			return false;
 		}
 
 		return true;
 	}
 
 }
