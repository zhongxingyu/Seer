 package syntaxtree;
 import bytecode.*;
 import bytecode.CodeStruct;
 import bytecode.CodeFile;
 import bytecode.CodeProcedure;
 import bytecode.type.*;
 
 public class VarDecl extends Decl {
   String name;
   String type;
   
   public VarDecl (String name, String type) {
     this.name = name;
     this.type = type;
   }
 
 
   // Add variable to CodeStruct
   @Override
   public void generateCode(CodeFile file, CodeStruct struct, CodeProcedure proc) {
     
     //Only CodeFile given, assume global variable
     if ((file != null) && (struct == null) && (proc == null) ){
       System.out.println("ADDING VAR '" + this.name + "' TO GLOBAL SCOPE");
       file.addVariable(this.name);

      file.updateVariable(name, new RefType(file.structNumber(type)));
     }
     
     // If struct is given we assume struct declaration
     else if (struct != null) {
       System.out.println("ADDING VAR '" + this.name + "' TO STRUCT");
       
       if(type.equals("float")) 
         struct.addVariable(this.name, FloatType.TYPE);
 	    else if(type.equals("int"))
         struct.addVariable(this.name, IntType.TYPE);
 	    else if(type.equals("boolean"))
         struct.addVariable(this.name, BoolType.TYPE);
 	    else if(type.equals("string"))
         struct.addVariable(this.name, StringType.TYPE);
     
     }
   }
 
 
     @Override
     public String semanticChecker(SymbolTable table){
 
 	//go to top symboltable level
 	SymbolTable top = table;
 	SymbolTable test;
 	while(top.ascend() != null){
 	    top = top.ascend();
 	}
 	test = null;
  
 	//check if variable exist
 	if(top.locate(name) != null || (test = table.locate(name)) != null){
 	    return "semantic error"; //syntaxError
 	}
 
 	//create new entry
 	table.newEntry(type, name);
 
 	return type; //yeah baby!
     }
 
 
 
 
   @Override
   public String printAst(int offset) {       
     return spaces(offset) + "(VAR_DECL (TYPE " + type + ") (NAME " + name +"))\n";
   }
 }
