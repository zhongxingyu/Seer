 package syntaxtree;
 import java.util.List;
 import bytecode.*;
 import bytecode.type.*;
 import bytecode.instructions.*;
 import java.util.HashMap;
 import java.util.Collection;
 import java.util.Iterator;
 
 public class AssignStmt extends Stmt{
 
   Exp var;
   Exp exp;
 
 	public AssignStmt(Exp var, Exp exp){
   	this.var = var;
 	  this.exp = exp;
 	}
 
 
   @Override
       public void generateCode(CodeFile file, CodeStruct struct, CodeProcedure proc, SymbolTable table){
     
       //We only need the name of this one, so I commented it out the old code
       //var.generateCode(file, null, proc);
       String varname = var.getName();
       String structName = var.gete1Name();
       
       System.out.println("ASSIGN VARIABLE: varname: " + varname + " structname: " + var.gete1Name());
       
       // I really hope this value gets pushed to stack!
       exp.generateCode(file, null, proc, table); 
 
 
       if(proc != null){ //If proc is delivered, assume the assignstmt is in a procedure
 	  // Store whatever is on stack to local variable varname
 	  if(structName.equals("")){
	      int bah = proc.variableNumber(varname);
	      if(bah == -1){

		  proc.addInstruction(new STOREGLOBAL(file.globalVariableNumber(varname)));
		  
	      }else{
		  int crap = proc.addInstruction(new STORELOCAL(proc.variableNumber(varname)));
	      }
 	      //try to get type
 	      //proc.addInstruction(new PUSHSTRING(exp.getName()));
 	      // System.out.println("SAAAAAAAAAAAAAAP " + varname + " crap : " + crap);
 	  }else{
 	      SymbolTable structt = null;
 
 	      Collection c = table.entries.values();
 	      Iterator it = c.iterator();
 
 	      Collection b;
 	      Iterator itr;
 
 	      while(it.hasNext()){
 		  SymbolTable tmp = (SymbolTable)it.next();
 		  if(tmp.name.equals(structName)){
 		      structt = tmp;
 		      break;
 		  }else{
 		      b = tmp.entries.values();
 		      itr = b.iterator();
 		      while(itr.hasNext()){
 			  SymbolTable crap = (SymbolTable)itr.next();
 			  if(crap.name.equals(structName)){
 			      structt = crap;
 			      break;
 			  }
 		      }
 		  }
 	      }
 
	      System.out.println("structt name: " + structt.name + " sstruct type: " + structt.type);
	      System.out.println("structName: " + structName + " varName: " + varname);
	      int tester = proc.addInstruction(new LOADLOCAL(proc.variableNumber(structName)));
	      System.out.println("tester: " + tester);
 

		  proc.addInstruction(new PUTFIELD(proc.fieldNumber(structt.type, varname), proc.structNumber(structt.type))); //How to get struct type?
 	  }
       } else { // Global scope = global variable
	  System.out.println("ELSEEEEEEEEEEEE");

 	  // Not working!
 	  //file.addInstruction(new STOREGLOBAL(file.variableNumber(varname)));
       }
       
   }
 
 
 
 
 
 
     @Override
 	public String semanticChecker(SymbolTable table, String type){
 	SymbolTable vres, eres;
 
 	
        	vres = var.semanticChecker(table);
 	if(vres == null)
 	    return "semantic error"; //error
 
 
 	eres = exp.semanticChecker(table);
 	if(eres == null)
 	    return "semantic error"; //error	
 
 
 	if(vres.type.equals(eres.type)){
 	    return vres.type;
 	}else if(vres.type.equals("float") && eres.type.equals("int")){
 	    return vres.type;
 	}else{
 	    return "semantic error";
 	}
 
     }
 
 
 
 	@Override
 	public String printAst(int offset){
 	String out;	
 
 	out = spaces(offset) + "(ASSIGN_STMT\n"; 
 	out+= spaces(offset + 2) + var.printAst(offset + 2) + "\n";
 	
 	out+= spaces(offset + 2) + exp.printAst(offset + 2) + "\n";
 
 	out += spaces(offset) + ")\n";
 
 	return out;
 	}
 
 
 
 
 }
