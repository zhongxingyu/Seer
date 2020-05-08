 package cs4120.der34dlc287lg342.xi.typechecker;
 
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.LinkedList;
 
 import cs4120.der34dlc287lg342.xi.*;
 import cs4120.der34dlc287lg342.xi.ast.*;
 
 import edu.cornell.cs.cs4120.util.VisualizableTreeNode;
 import edu.cornell.cs.cs4120.xi.AbstractSyntaxNode;
 
 public class XiTypechecker {
 	public LinkedList<XiTypeContext> stack;
 	public static XiTypeContext globalContext;
 	
 	public AbstractSyntaxNode ast;
 	
 	public XiTypechecker(AbstractSyntaxNode ast) throws InvalidXiTypeException{
 		this.ast = ast;
 		globalContext = new XiTypeContext(false);
 		stack = new LinkedList<XiTypeContext>();
 		init();
 		stack.add(globalContext);
 	}
 	
 	// add function declarations found within interfaces into the global context
 	HashMap<String, XiType> include(String id) throws FileNotFoundException{
 		HashMap<String, XiType> interfaces = new HashMap<String, XiType>();
 		
 		FileReader reader = new FileReader(id+".ixi");
 		XiInterfaceParser parser = new XiInterfaceParser(reader, id+".ixi");
 		
 		AbstractSyntaxNode declarations = parser.parse();
 		for (VisualizableTreeNode child : declarations.children()){
 			FuncDeclNode decl = (FuncDeclNode)child;
 			IdNode identifier = (IdNode)decl.id;
 			interfaces.put(identifier.id, decl.type);
 		}
 		
 		return interfaces;
 	}
 	
 	private void init() throws InvalidXiTypeException{
 		// first pass
 		// precondition: ast is a program node
 		for (VisualizableTreeNode child : ast.children()){
 			// either UseNode or FuncDeclNode
 			if (child instanceof FuncDeclNode){
 				FuncDeclNode func = (FuncDeclNode)child;
 				IdNode identifier = (IdNode)func.id;
 				globalContext.add(identifier.id, func.type);
 			} else if (child instanceof UseNode){
 				UseNode use = (UseNode)child;
 				IdNode lib = (IdNode)use.lib;
 				HashMap<String, XiType> interfaces;
 				try {
 					interfaces = include(lib.id);
 				} catch (FileNotFoundException e) {
 					throw new InvalidXiTypeException("Interface file cannot be found for "+lib.id);
 				}
 				globalContext.add(interfaces);
 			} else {
 				throw new InvalidXiTypeException("Invalid Abstract Syntax Tree");
 			}
 		}
 	}
 	
 	public void typecheck() throws CompilationException{
 		// decorate the AST
 		// only expression nodes are associated with types
 		for (VisualizableTreeNode child : ast.children()){
 			if (child instanceof FuncDeclNode){
 				FuncDeclNode func = (FuncDeclNode)child;
 				// typecheck function
 				func.typecheck(stack);
 			}
 		}
 	}
 }
