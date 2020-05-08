 package uk.ac.ucl.comp2010.bestgroup;
 import java.util.*;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import uk.ac.ucl.comp2010.bestgroup.AST.*;
 
 public class SemanticsVisitor extends Visitor{
 	
 	final static Logger LOGGER = Logger.getLogger(SemanticsVisitor.class .getName());
     LinkedList<HashMap<String, DeclNode>> symbolTables;
     
     public void error(String err, Node node) {
     	System.out.println(err + " (line " + node.lineNumber + ", col " + node.charNumber + ")");
     }
     
     public void errorlog(String err, Node node){
     	LOGGER.log(Level.SEVERE, err + " (line " + node.lineNumber + ", col " + node.charNumber + ")");
     }
     
     public void errorlog(String err, Node node, Level logLevel){
     	LOGGER.log(logLevel, err + " (line " + node.lineNumber + ", col " + node.charNumber + ")");
     }
     
 	public SemanticsVisitor() {
 		symbolTables = new LinkedList<HashMap<String, DeclNode>>();
 	}
     
     private void insert(String id, DeclNode node){
         symbolTables.getLast().put(id, node);
     }
 
     private void beginScope() {
         symbolTables.addLast(new HashMap<String, DeclNode>());
     }
 
     private void endScope(){
         symbolTables.removeLast();
     }
 
     //Possibly change to boolean return-type?
     private DeclNode lookup(String id){
         for(ListIterator<HashMap<String, DeclNode>> it = symbolTables.listIterator(symbolTables.size()); it.hasPrevious();){
             DeclNode declaration = it.previous().get(id);
             if(declaration != null){
                 return declaration;
             }
         }
         return null;
     }
     
     private DeclNode lookupFirst(String id){
     	return symbolTables.getFirst().get(id);
     }
     
     //could possibly give more detailed error description if I change it from boolean to int or string and at least 3 returns.
     private boolean lookupProperty(String id, String property){
     	//Looks if variable has been declared
     	DeclNode variableDecl = lookup(id);
     	//System.out.println("Entering, looking for: " + id);
     	if(variableDecl != null){
     		//System.out.println(variableDecl.toString());
     	}
     	if(variableDecl instanceof VarDeclNode){
     		//Now need to check what type it is and if it has been declared at the top.
     		String variableType = ((VarDeclNode) variableDecl).var.type;
     		//System.out.println("Found: " + variableType);
     		DeclNode customType = symbolTables.getFirst().get(variableType);
     		if(customType != null){
     			if(customType instanceof DatatypeDeclNode){
     				return true;
     			}
     		}
     	}
     	return false;
     }
 
     @Override
     public Object visit(IntNode node) {
         return "int";
     }
     
     @Override
     public Object visit(ProgramNode node){
     	beginScope();
     	//System.out.println("Beginning Scope, declaration size: " + node.declarations.size());
     	super.visitList(node.declarations);
     	beginScope();
     	visit(node.main);
     	endScope();
     	endScope();
 		return "programnode";
     	
     }
 
     @Override
     //To-do: need to check if what the variable is actually declaring, e.g. if it is calling a function, etc. exists.
     public Object visit(VarDeclNode node){
     	visitList(node.value);
     	HashMap<String, DeclNode> latestTable = symbolTables.getLast();
     	if(! latestTable.containsKey(node.var.id)){
     		latestTable.put(node.var.id, node);
     	}else{
     		errorlog("Can't declare variable twice in same scope", node);
     	}
         
         return "vardecl";
         //for declaring variables
     }
 
     @Override
     public Object visit(FuncDeclNode node){
     	insert(node.id, node);
     	beginScope();
     	//System.out.println("FuncDeclNode: Begin Scope");
         visitList(node.args);
         visit(node.body);
         endScope();
         //System.out.println("End Scope");
         return "funcdecl";
         // for declaring functions
     }
     
     @Override
     public Object visit(VarTypeNode node){
     	//System.out.println(node.id);
     	VarDeclNode paramDecl = new VarDeclNode(node, new LinkedList<ExprNode>());
     	insert(node.id, paramDecl);
         // for declaring functions
 		return "Bla";
     }
 
     @Override
     public Object visit(DatatypeDeclNode node){
     	//System.out.println("DatatypeDeclNode:" + node.id);
     	if(symbolTables.size() == 1){
     		symbolTables.getFirst().put(node.id, node);
     	}else{
    		errorlog("Error! Custom Datatype can only be declared before the main block.", node);
     	}
         
         return "datatypedecl";
         // for declaring datatypes
     }
     
     //Question: An AccessorNode ever only has two elements in node.path, right?
     //TO-DO: Check the second element. E.g. if it is a tdef, check that you are accessing an element that exists.
     @Override
     public Object visit(AccessorNode node){
    		String variableId = node.path.getFirst();
 		DeclNode vardecl = lookup(variableId);
 		if(vardecl == null && !(vardecl instanceof VarDeclNode)){
			errorlog("Variable " + variableId + " does not exist.", node);
 			return null;
 		} else {
 			if(node.path.size() == 1) {
 				return ((VarDeclNode)vardecl).var.type;
 			} else {
 				String t = ((VarDeclNode)vardecl).var.type;
 				DeclNode type;
 				pathloop: for(int p=0; p<node.path.size()-1; p++) {
 					type = lookupFirst(t);
 					for(ListIterator<VarTypeNode> ti = ((DatatypeDeclNode)type).fields.listIterator(); ti.hasNext();) {
 						VarTypeNode n = ti.next();
 						if(n.id.equals(node.path.get(p+1))) {
 							t = n.type;
 							continue pathloop;
 						}
 					}
 					String e = node.path.getFirst();
 					for(int i=1; i<=p; i++) {
 						if(i>0) {e+=".";}
 						e += node.path.get(p);					
 					}
 					e += " (type: " + t + ") does not have property " + node.path.get(p+1);
 					errorlog(e,node);
 					return null;
 				}
 				return t;
 			}
 		}
     }
 
     @Override
     public Object visit(FuncCallExprNode node){
     	DeclNode fdef = lookupFirst(node.id);
     	if(fdef == null || !(fdef instanceof FuncDeclNode)) {
    		errorlog("Function " + node.id + " does not exist.", node);
     		return null;
     	} else if(node.args.size() != ((FuncDeclNode)fdef).args.size()){
    		errorlog("Function " + node.id + " should take " + ((FuncDeclNode)fdef).args.size() + " argument(s) (" + node.args.size() + " given).", node);
     	} else {
     		for(int i=0; i<node.args.size(); i++) {
     			String refType = (String) visit(node.args.get(i));
     			String declType = (String)(((FuncDeclNode)fdef).args.get(i).type);
     			if(! isSupertype(refType, declType)) {
     				errorlog("Argument " + (i+1) + " of function " + node.id + " should be of type " + declType + " (" + refType + " given)", node.args.get(i));
     			}
     		}
     	}
     	return ((FuncDeclNode)fdef).type;
         // for calling functions
     }
     
     public boolean isSupertype(String sub, String sup) {
     	if(sub == sup)
     		return true;
     	if(sub == "int" || sup == "float")
     		return true;
     	return false;
     }
     
     //compares concatNodes.
     @Override
     public Object visit(ConcatNode node){
     	String left = (String)node.left.toString();
     	String right = (String)node.right.toString();
     	if(left.equals("tuple") && right.equals("tuple")){
     		return (String)"tuple";
     	}
     	else if (left.equals("list") && right.equals("list")){
     		return (String)"list";
     	}
     	return (String)"ConcatNode error";
     }
     
     @Override
     public Object visit(NumericOperationNode node) {
         String leftType = (String)visit(node.left);
         String rightType = (String)visit(node.right);
         if(leftType == "int" && rightType == "int") {
             //System.out.println("int");
             return "int";
         } else if(leftType == "int" && rightType == "float" || leftType == "float" && rightType == "int" || leftType == "float" && rightType == "float"){
             //System.out.println("float");
             return "float";
         } else {
             //System.out.println("Cannot understand: " + leftType + " " + node.op + " " + rightType);
             return null;
         }
     }
     
     @Override
     public Object visit(BoolNode node){
     	return (String)"bool";
     }
     
     @Override
     public Object visit(CharNode node){
     	return (String)"char";
     }
     
     @Override
     public Object visit(ComparisonNode node){
     	String left = (String)node.left.toString();
     	String right = (String)node.right.toString();
     	if((left.equals("int") && right.equals("int")) || (left.equals("float") && right.equals("float")) || (left.equals("bool") && right.equals("bool")) || (left.equals("int") && right.equals("float")) || (left.equals("int") && right.equals("bool"))){
     		return (String) "bool";
     	}
     	return (String) "ComparisonNode error";
     }
     
     @Override
     public Object visit(EqualsNode node){
     	String left = (String)node.left.toString();
     	String right = (String)node.right.toString();
     	if (left == (String)"bool" && right == (String)"bool"){
     		return (String)"bool";
     	}
     	return (String)"EqualsNode error";
     }
     
     @Override
     public Object visit(FloatNode node){
     	return (String)"float";  	
     }
     
     /*@Override
     public Object visit(IfNode node){
     	//beginScope();
     	if ((node.true_block.toString() != null) || (node.false_block.toString() != null)){
     		for
     	}
     }*/
     
     
     public Object visit(String node){
     	return (String)"string";
     }
 
 }
