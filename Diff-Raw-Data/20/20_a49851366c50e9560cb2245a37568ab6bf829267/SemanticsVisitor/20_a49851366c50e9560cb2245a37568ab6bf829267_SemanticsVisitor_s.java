 package uk.ac.ucl.comp2010.bestgroup;
 import java.util.*;
 
 import uk.ac.ucl.comp2010.bestgroup.AST.*;
 
 public class SemanticsVisitor extends Visitor{
 	
     LinkedList<HashMap<String, DeclNode>> symbolTables;
     
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
             //System.out.println("test");
         }
 
         return null;
         /*
         for(HashMap<String, DeclNode> table: symbolTables){
             if(table.get(id) != null){
                 return table.get(id);
             }
         }
         return null;*/
     }
     
     private DeclNode lookupFirst(String id){
     	return symbolTables.getFirst().get(id);
     }
     
     //could possibly give more detailed error description if I change it from boolean to int or string and at least 3 returns.
     //Note: What should happen if someone does e.g. f:person and a line later f:family and then accesses f.xx
     private boolean lookupProperty(String id, String property){
     	DeclNode customType = lookup(id);
     	System.out.println("Entering");
     	if(customType != null){
     		System.out.println(customType.toString());
     	}
     	if(customType instanceof VarDeclNode){
     		//((VarDeclNode) customType).var.type
     		System.out.println("OK");
     		/*LinkedList<VarTypeNode> customTypeFields = ((DatatypeDeclNode) customType).fields;
     		for(ListIterator<VarTypeNode> it = customTypeFields.listIterator(); it.hasNext();){
                 if(it.next().id == property){
                     return true;
                 }
             }*/
     	}
     	return false;
     }
 
     @Override
     public Object visit(IntNode node) {
         return "int";
     }
 
     @Override
     public Object visit(NumericOperationNode node) {
         String leftType = (String)visit(node.left);
         String rightType = (String)visit(node.right);
         if(leftType == "int" && rightType == "int") {
             System.out.println("int");
             return "int";
         } else if(leftType == "int" && rightType == "float" || leftType == "float" && rightType == "int" || leftType == "float" && rightType == "float"){
             System.out.println("float");
             return "float";
         } else {
             System.out.println("Cannot understand: " + leftType + " " + node.op + " " + rightType);
             return null;
         }
     }
     
     @Override
     public Object visit(ProgramNode node){
     	beginScope();
    	visit(node.declarations);
     	beginScope();
     	visit(node.main);
     	endScope();
     	endScope();
 		return "programnode";
     	
     }
 
     @Override
     public Object visit(VarDeclNode node){
     	HashMap<String, DeclNode> latestTable = symbolTables.getLast();
     	if(latestTable.get(node.var.id) == null){
     		latestTable.put(node.var.id, node);
     	}else{
     		System.out.println("Can't declare Variable with the same name twice.");
     	}
         
         System.out.println("vardecl");
         return "vardecl";
         //for declaring variables
     }
 
     @Override
     public Object visit(FuncDeclNode node){
         visit(node.args);
         beginScope();
         System.out.println("Begin Scope");
         visit(node.body);
         endScope();
         System.out.println("End Scope");
         return "funcdecl";
         // for declaring functions
     }
 
     @Override
     public Object visit(DatatypeDeclNode node){
     	System.out.println("DatatypeDeclNode:" + node.id);
     	if(symbolTables.size() == 1){
     		symbolTables.getFirst().put(node.id, node);
     	}else{
     		System.out.println("Error! Can't declare Datatype here. If you see this very message, some programmer has been lazy.");
     	}
         
         return "datatypedecl";
         // for declaring datatypes
     }
     
     //Question: An AccessorNode ever only has two elements in node.path, right?
     //TO-DO: Check the second element. E.g. if it is a tdef, check that you are accessing an element that exists.
     @Override
     public Object visit(AccessorNode node){
     	if(symbolTables.size() > 1){
     		/*if(node.path.size() <= 2){
     		String firstEl = node.path.getFirst();
     		System.out.println("---Start-----");
     		for(String el: node.path){
     			System.out.println(el);
     		}
     		System.out.println("----End-----");
     		//System.out.println(node.path.getFirst());
     		if(lookup(firstEl) != null){
     			if(node.path.size() == 2){
     				System.out.println(node.path.getFirst() + "---" + node.path.get(1));
     				if(lookupProperty(node.path.getFirst(), node.path.get(1))){
     					return "Success";
     				}else{
     					System.out.println("Error! Property you are trying to access does not exist");
     				}
     			}
     			return "First step successful";
     		}else{
     			System.out.println("Error! Data-type does not exist.");
     		}*/
     		if(node.path.size() == 1){
     			DeclNode variable = lookup(node.path.getFirst());
     			if(variable != null){
     				System.out.println("Variabl usage;");
     				//cast to VarDeclNode to get type and do stuff
     				return "Working";
     			}else{
     				System.err.println("Error! Variable does not exist.");
     			}
     		}else if(node.path.size() == 2){
     			if(lookupProperty(node.path.getFirst(), node.path.get(1))){
     				System.out.println("Woohey");
     			}else{
     				System.err.println("In ya face!");
     			}
     		}else{
     			System.out.println("Error. You can't chain multiple datatypes.");
     		}
     	}else{
     		System.out.println("Error! Can't access data-type here. If you see this very message, some programmer has been lazy.");
     	}
         System.out.println("AccessorNode");
         return "AccessorNode";
         // for accessing variables
     }
 
     @Override
     public Object visit(FuncCallExprNode node){
         System.out.println("FuncCallExprNode");
         return "FuncCallExprNode";
         // for calling functions
     }
 
 }
