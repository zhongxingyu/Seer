 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package tarccompiler;
 
 import datamodels.Node;
 import datamodels.SymbolTableModel;
 import datamodels.Tree;
 import java.util.ArrayList;
 import storage.SymbolTable;
 
 
 public class SemanticAnalyzer {
     
     // Attributes
     private Tree astTree;
     private SymbolTable symbolTable;
     private ArrayList<String> list;
     private String semanticErrorMessage;
     
     // Constructor
     public SemanticAnalyzer(Tree tree, SymbolTable symTbl, ArrayList<String> list){
         this.semanticErrorMessage = "Success";
         this.astTree = tree;
         this.symbolTable = symTbl;
         this.list = list;
         this.storeToken(astTree.getRoot(), list);
         displaySymbolTable();  
         //System.err.println(list);
     }
     
     // Methods
     //<editor-fold defaultstate="collapsed" desc="Debugging Methods">
     public void displaySymbolTable(){
         System.out.println("\n\nSymbol Table:");
         System.out.println("token \t\t tokenVal \t\t datatype \t\t scope \t\t actual value");
         for(int i=0; i<symbolTable.table.size(); i++){
             SymbolTableModel temp = symbolTable.table.get(i);
             System.out.println(temp.token+" \t\t "+temp.tokenValue+" \t\t\t "+temp.datatype+" \t\t\t "+temp.scope+"\t\t\t"+temp.actualValue);
         }
     }
     //</editor-fold>
     
     // Returns a list of strings containing all terminals: #main, #char, #bool, #func
     private void storeToken(Node n, ArrayList<String> s){
         ArrayList<Node> temp = n.getNodeChildren();
         for(Node new_n : temp){
             if(!new_n.getNodeData().equals("~")){
                 s.add(new_n.getNodeData());
             }
             storeToken(new_n, s);
         } 
     }
     
     private ArrayList<String> storeAllFunc(int index){
         ArrayList<String> ret = new ArrayList<String>();
         int j=index+1;
         int numOfComma = 0;
         
         //first store functionName
         ret.add(list.get(j));
         for(; !list.get(j).equals(")"); j++){
         
             if(list.get(j).equals("#int") || list.get(j).equals("#char") || list.get(j).equals("#boolean")){
                 ret.add(list.get(j)+"^"+list.get(j+1));
             }
             if(list.get(j).equals(","))
                 numOfComma++;
         }//end of for loop
         
         ret.add(Integer.toString(numOfComma+1));
         return ret;
     }
     
     public void semAnt(){
      
         ArrayList<String> listOfDecStms = new ArrayList<String>();
         ArrayList<String> listOfFuncWIndex = new ArrayList<String>();
         ArrayList<String> listOfFunc = new ArrayList<String>();
         ArrayList<String> listOfFuncCalls = new ArrayList<String>();
         ArrayList<String> listOfPuts = new ArrayList<String>();
         
         int i, j, exit = 0;
         
        //traverse in the list and store all necessary terminals 
        for(i=0; i<list.size(); i++){
            
            if(list.get(0).equals("#main")){
                exit = 1;
            }
            /***********  storing of indeces of functions and main *********/
            if(list.get(i).equals("#main")){
                listOfFuncWIndex.add(Integer.toString(i));
                listOfFuncWIndex.add(list.get(i));
            }
            
            /***********  storing of function information  *********/
            if(list.get(i).equals("#func")){
                
                /***********  storing of indeces of functions and main *********/
                listOfFuncWIndex.add(Integer.toString(i+1));
                listOfFuncWIndex.add(list.get(i+1));
                
                String concat = new String();
                int numOfComma = 0;
                j = i+1;
                 //first store functionName
                 listOfFunc.add(list.get(j));
                 
                 if(list.get(j+2).equals(")"))
                     numOfComma = -1;
                 for(; !list.get(j).equals(")"); j++){
 
                     if(list.get(j).equals("#int") || list.get(j).equals("#char") || list.get(j).equals("#boolean")){
                         concat = concat + list.get(j)+"^"+list.get(j+1)+"~"; 
                     }
                     if(list.get(j).equals(","))
                         numOfComma++;
                 }//end of for loop
                 listOfFunc.add(concat);
                 listOfFunc.add(Integer.toString(numOfComma+1));
            }
            /*********** END OF:   storing of function information  *********/
            
             /*********** storing of declaration statements  *********/
            else if(list.get(i).equals("=")){
                //check first if variable is of type #char, type char have special case: the use of ''
                     int incrementer = 2;
                     int valIncrementer = 1;
                     int verifyIfIn = 0;
                     
                      if(list.get(i+1).equals("'")){
                         incrementer = 4;
                         valIncrementer = 2;
                     }
                      
                     if(list.get(i+incrementer).equals(";")){
                         listOfDecStms.add(list.get(i-1)); //left
                         
                         if(valIncrementer==2){
                             listOfDecStms.add("~"+list.get(i+valIncrementer)); //right
                         }
                         else listOfDecStms.add(list.get(i+valIncrementer));
                         
                         listOfDecStms.add(listOfFuncWIndex.get(listOfFuncWIndex.size()-1)); //left_DeclaredIn
                         
                         for(j=0; j<symbolTable.getLast()+1; j++){
                             if(symbolTable.table.get(j).tokenValue.equals(list.get(i-1)) 
                                     && symbolTable.table.get(j).scope.equals(listOfFuncWIndex.get(listOfFuncWIndex.size()-1))){
                                //System.err.println(list.get(i-1));
                                 listOfDecStms.add(symbolTable.table.get(j).datatype);
                                 verifyIfIn = 1;
                             }
                         }
                         if(verifyIfIn==0)
                                 listOfDecStms.add("");
                         }
            }
            /***********END OF: storing of declaration statements  *********/
            
            if(list.get(i).equals("#puts")){
                if(!list.get(i+2).equals("\"")){
                listOfPuts.add(list.get(i+2));
                listOfPuts.add(listOfFuncWIndex.get(listOfFuncWIndex.size()-1));
                }
            }
            
            int incrementer = 1;
            if(exit!=1){
                incrementer = 2;
            }
                /*********** storing of function call info  *********/
                if(list.get(i).equals("(")&& !list.get(i-incrementer).equals("#func") && !list.get(i-1).equals("#main") && !list.get(i-1).equals("#puts") && !list.get(i-1).equals("while")
                         && !list.get(i-1).equals("if")){
                    if(!list.get(i+1).equals("#int") && !list.get(i+1).equals("#char") && !list.get(i+1).equals("#boolean")){
 
                        listOfFuncCalls.add(Integer.toString(i-1)); //index of function call
                        listOfFuncCalls.add(list.get(i-1));          //name of function call
                        listOfFuncCalls.add(listOfFuncWIndex.get(listOfFuncWIndex.size()-1)); //caller
 
                        String concat = new String();
                        for(j=i; !list.get(j).equals(")") && j<list.size()-1; j++){
                            if(!list.get(j).equals(",") && !list.get(j).equals("("))
                                 concat = concat + list.get(j)+"~"; 
                         }
                        listOfFuncCalls.add(concat); //func call arguments
                    }
                 }//end of for loop
            /*********** END OF: storing of function call info  *********/
            
     }
        /******** adding the end index and last********/
        listOfFuncWIndex.add(Integer.toString(list.size()-1));
        listOfFuncWIndex.add("last");
          
         /*********** SEMANTIC PHASE START: checking of duplicate variables in a function/main **********/
         checkDuplicateVars();
         
         /*********** SEMANTIC PHASE START: checking of declaration statements **********/
         if(checkDuplicateVars()!=true)
             checkDeclarationStatements(listOfDecStms);
         
         /*********** SEMANTIC PHASE START: checking of duplicate functions **********/
         checkDuplicateFuncs();
         
         /*********** SEMANTIC PHASE START: checking validity of function calls **********/
         emptyFuncParam(listOfFunc);
         checkValidFuncCalls(listOfFunc, listOfFuncCalls);
         
         
         putsChecker(listOfPuts);
         
       }
     
     private void emptyFuncParam(ArrayList<String> allFuncs){
         int i;
         Boolean flag = false;
         
         for(i=0; i<allFuncs.size() && flag==false; i=i+3){
             if(allFuncs.get(i+2).equals(Integer.toString(0)))
                 this.setNoParamMessage();
         }
         
     }
     
     private void putsChecker(ArrayList<String> putsList){
         int i, j;
         Boolean check = false;
         for(i=0; i<putsList.size();i=i+2){
             for(j=0; j<symbolTable.getLast()+1; j++){
                 if(symbolTable.table.get(j).tokenValue.equals(putsList.get(i)) && symbolTable.table.get(j).scope.equals(putsList.get(i+1))){
                     check = true;
                 }
             }
             if(check==false)
                 this.setUndeclaredVarMessage();
         }
     }
     private Boolean checkDuplicateVars(){
         int i, j;
         Boolean ret = false;
         
         for(i=0; i<symbolTable.getLast()+1; i++){
             for(j=0; j<symbolTable.getLast()+1; j++){
                 if(i!=j && symbolTable.table.get(i).tokenValue.equals(symbolTable.table.get(j).tokenValue)){
                     if(symbolTable.table.get(i).scope.equals(symbolTable.table.get(j).scope)){
                         this.setDuplicateVarMessage();
                         ret = true;
                     }
                 }
             }
         }
         return ret;
     }
     public void checkDuplicateFuncs(){
         int i, j;
         
         for(i=0; i<symbolTable.getLast()+1; i++){
             for(j=0; j<symbolTable.getLast()+1; j++){
                 if(i!=j && symbolTable.table.get(i).tokenValue.equals(symbolTable.table.get(j).tokenValue)
                         && symbolTable.table.get(i).datatype.equals(symbolTable.table.get(j).datatype)){
                     if(symbolTable.table.get(i).scope.equals("#func") && symbolTable.table.get(i).scope.equals(symbolTable.table.get(j).scope))
                        this.setDuplicateFuncNameMessage();
                 }
             }
         }
     }
     
     private void checkValidFuncCalls(ArrayList<String> allFuncs, ArrayList<String> allFuncCalls){
         int i, j;
         Boolean validFunc = false;
         
         for(i=0; i<allFuncCalls.size(); i=i+4){
             for(j=0; j<allFuncs.size(); j=j+3){
                 if(allFuncCalls.get(i+1).equals(allFuncs.get(j))){
                     validFunc = true;
                     
                     if(allFuncCalls.get(i+3).equals("") && allFuncs.get(j+2).equals(Integer.toString(0)))
                         System.err.println("func has 0 parameters");
                     
                     else if(allFuncCalls.get(i+3).equals("") && !allFuncs.get(j+2).equals(Integer.toString(0)))
                         validFunc = false;
                     
                     else if(!allFuncCalls.get(i+3).equals("") && !allFuncs.get(j+2).equals(Integer.toString(0))){
                         int k, l;
                         String split_parameters[] = allFuncs.get(j+1).split("\\~");
                         String split_arguments[]=allFuncCalls.get(i+3).split("\\~");
                             for(l = 0; l<split_parameters.length; l++){
                                 System.err.println("SPLIT"+split_parameters[l]);
                             }
                             for(k =0; k<split_arguments.length; k++){
                                 int err = 0;
                                 try{
                                     Integer.parseInt(split_arguments[k]);
                                     err = 1;
                                 }catch(NumberFormatException e) {
                                     err = 0;
                                 }
                                 
                                 if("'".equals(split_arguments[k]) || err==1)
                                     this.setFuncParamtypeMismatchMessage();
                                 
                                 
                                 System.err.println("SPLIT"+split_arguments[k]);
                             }
                            
                         //function call have the same no. of arguments as the function header; datatype and variable matching    
                         if(l==k){
                             for(k=0; k<split_arguments.length; k++){
                                 for(l=0; l<split_parameters.length; l++){
                                    
                                    int n = 0;
                                    if(split_parameters[k].contains("#int"))
                                        n = 5;
                                    else if(split_parameters[k].contains("#char"))
                                        n = 6;
                                    else if(split_parameters[k].contains("#boolean"))
                                        n = 9;
                                    
                                    if(split_arguments[k].equals(split_parameters[k].substring(n))){
                                         for(int m=0; m<symbolTable.getLast()+1; m++){
                                             if(symbolTable.table.get(m).tokenValue.equals(split_arguments[k]) &&
                                                     symbolTable.table.get(m).scope.equals(allFuncCalls.get(i+2))){
                                                     String dt = symbolTable.table.get(m).datatype;
                                                     
                                                     if(!split_parameters[k].contains(dt))
                                                         this.setFuncParamtypeMismatchMessage();
                                             }
                                            
                                         }
                                     }
                                     else this.setParameterMessage();
                                 }
                             }
                             
                         }
                         else this.setParameterMessage();
                     }
                     
                 }
             }
             if(validFunc==false)
                 this.setInvalidFuncMessage();
         }
     }
     
     private void checkDeclarationStatements(ArrayList<String> DecStatements){
         int i, j, k, in = 0;
         //ArrayList<String> errors = new ArrayList<String>();
         Boolean verifyIfDeclared = false;
         Boolean verifyAnotherVar = false;
         
         for(i=0; i<DecStatements.size(); i=i+4){
             for(j=0; j<symbolTable.getLast()+1; j++){
                 //first check if left is a declared variable
                 if(symbolTable.table.get(j).tokenValue.equals(DecStatements.get(i)) && symbolTable.table.get(j).scope.equals(DecStatements.get(i+2))){
                     verifyIfDeclared = true;
                 }
                 //after verifying validity of variable, check the assignment statement, TYPE CHECKING
                 if(verifyIfDeclared==true){
                     for(k = 0; k<symbolTable.getLast()+1;k++){
                         if(DecStatements.get(i+1).equals(symbolTable.table.get(k).tokenValue) && DecStatements.get(i+2).equals(symbolTable.table.get(k).scope)){
                             in = 1;
                             if(DecStatements.get(i+3).equals(symbolTable.table.get(k).datatype))
                                 verifyAnotherVar=true;
                             else verifyAnotherVar = false;
                             }
                     }
                     //to-be-assigned value is an actual value (not a variable)
                      if(in!=1 && verifyAnotherVar==false){
                          Boolean checktype = checkType(DecStatements.get(i+1),DecStatements.get(i+3));
                          if(checktype==false)
                              this.setAssignmentMessage();
                          
                          else symbolTable.table.get(j).actualValue = DecStatements.get(i+1);
                      }
                      if(in==1 && verifyAnotherVar==false){
                         this.setAssignmentMessage();
                     }
                 }
             }
             if(verifyIfDeclared==false){
                 this.setUndeclaredVarMessage();
             }
         }
     }
     
     private Boolean checkType(String value, String dataType){
         Boolean ret = false;
         
         if( "#int".equals(dataType)){
             try{
                 Integer.parseInt(value);
                 return true;
             }catch(NumberFormatException e) {
                 return false;
             }
         }
         else if("#char".equals(dataType)){
             value = value.substring(1);
             ret = (value.length()>1)? false: true;
         }
         else if("#boolean".equals(dataType)){
             ret = ("false".equals(value)|| "true".equals(value))? true: false;
         }
         return ret;
     }
     
     public String getMessage(){
         return semanticErrorMessage;
     }
     
     private void setNoParamMessage(){
         semanticErrorMessage = "A function has no parameters.";
     }
     
     private void setAssignmentMessage(){
         semanticErrorMessage = "Invalid assignment statement.";
     }
     
     private void setInvalidFuncMessage(){
         semanticErrorMessage = "Invalid function call.";
     }
     
     private void setParameterMessage(){
         semanticErrorMessage = "Parameter Mismatch.";
     }
     
     private void setLValueMessage(){
         semanticErrorMessage = "LValue required.";
     }
     
     private void setMainMessage(){
         semanticErrorMessage = "Invalid reserved word.";
     }
     
     private void setDuplicateFuncNameMessage(){
         semanticErrorMessage = "Duplicate Function found.";
     }
     
     private void setDuplicateVarMessage(){
         semanticErrorMessage = "Duplicate Variable found.";
     }
     
     private void setUndeclaredVarMessage(){
         semanticErrorMessage = "Undeclared variable used.";
     }
     
     private void setFuncParamtypeMismatchMessage(){
         semanticErrorMessage = "Function Parameter Type Mismatch.";
     }
 
     
 }
