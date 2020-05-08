 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package tarccompiler;
 
 import datamodels.Node;
 import datamodels.SymbolTableModel;
 import datamodels.Tree;
 import java.util.ArrayList;
 import java.util.regex.Pattern;
 import storage.SymbolTable;
 
 
 public class SemanticAnalyzer {
     
     // Attributes
     private Tree astTree;
     private SymbolTable symbolTable;
     private ArrayList<String> list;
     
     // Constructor
     public SemanticAnalyzer(Tree tree, SymbolTable symTbl, ArrayList<String> list){
         
         this.astTree = tree;
         this.symbolTable = symTbl;
         this.list = list;
         storeToken(astTree.getRoot(), list);
         checkDataType();
         checkFuncCall();
         displaySymbolTable();
         
         }
     
     // Methods
     //<editor-fold defaultstate="collapsed" desc="Debugging Methods">
     private void displaySymbolTable(){
         System.out.println("\n\nSymbol Table:");
         System.out.println("token \t\t tokenVal \t\t datatype \t\t scope \t\t actual value");
         for(int i=0; i<symbolTable.table.size(); i++){
             SymbolTableModel temp = symbolTable.table.get(i);
             System.out.println(temp.token+" \t\t "+temp.tokenValue+" \t\t\t "+temp.datatype+" \t\t\t "+temp.scope+"\t\t\t"+temp.actualValue);
         }
     }
     //</editor-fold>
     
  // method will return a list of strings containing all terminals: #main, #char, #bool, #func
 void storeToken(Node n, ArrayList<String> s){
      //System.out.println("THIS IS NODE N!!!!!!!!" + n.getNodeData());
      ArrayList<Node> temp = n.getNodeChildren();
 
      for(Node new_n : temp){
             if(!new_n.getNodeData().equals("~"))
                     s.add(new_n.getNodeData());
 
     storeToken(new_n, s);
  } 
 }
     
 //checkMain will return the Node containing #main
 Boolean checkMain(Node root){
 		
 		int i;
                 Boolean verifyFunc = true;
 		ArrayList<String> allTokens = new ArrayList<String>();
 		
 		storeToken(root, allTokens);
                 System.err.println(allTokens);
 		
 		// check if there's main
 		if(allTokens.contains("#main"))
 		{
                     System.err.println(allTokens);
                     int index  = allTokens.indexOf("#main")+1;
                     System.err.println(index);
                     for(; index<allTokens.size() && !allTokens.get(index).equals("#func"); index++){
                     
                     System.out.print("\n" + index + allTokens.get(index));
                     if(allTokens.get(index).equals("#func")){
                         System.err.println("There is a function after #main");
                         verifyFunc = false;
                     }
                     else verifyFunc = true;
 		}
                     
                 }
                 System.err.println(allTokens);
                     
 return verifyFunc;	
 }   
 
 void checkDataType(){
 
         int i, j;
         ArrayList<Integer> storeFuncInList = new ArrayList<Integer>();
         ArrayList<Integer> storeFuncInST = new ArrayList<Integer>();
         ArrayList<String> storeAllDecVar = new ArrayList<String>();
        // int funcCounter = Collections.frequency(list, "#func");
 
         //store all indeces of function types found in the arraylist of tokens
         for(i = 0;i<list.size();i++){
             if(list.get(i).equals("#func") || list.get(i).equals("#main"))
                 storeFuncInList.add(i);
         }
         //store all indeces of function types found in symboltable
         for(j = 0; j<symbolTable.getLast(); j++){
             if( "#func".equals(symbolTable.table.get(j).datatype))
                 storeFuncInST.add(j);
         }
 
         for(i=0, j = storeFuncInList.get(i); i<storeFuncInList.size() && j<list.size()-1; j++){
 
             if(list.get(j).equals("=") && list.get(j+2).equals(";")){
                 String scopeOfVar;
                 String dt = null;
                 storeAllDecVar.add(list.get(j-1)); //varName
                 storeAllDecVar.add(list.get(j+1)); //actualValue
                 scopeOfVar = ("#func".equals(list.get(storeFuncInList.get(i))))? (list.get(storeFuncInList.get(i)+1)):"main";
 
                 //traverse in symbolTable to find out the datatype of the variable with declaration statement
                 int k = 0;
                 Boolean stopScan = false;
                 for(; stopScan!=true && k<=symbolTable.getLast(); k++){
                     String scopeWithHash = scopeOfVar;
 
                     if("main".equals(scopeOfVar))
                         scopeWithHash = "#main";
 
                     if(symbolTable.table.get(k).scope.equals(scopeWithHash) && symbolTable.table.get(k).tokenValue.equals(list.get(j-1)))
                     {   
                         dt = symbolTable.table.get(k).datatype;
                         stopScan = true; 
                     }
                 }
 
                 storeAllDecVar.add(dt);
                 storeAllDecVar.add("#"+scopeOfVar);
             }
 
             if("#main".equals(list.get(j+1)) || "#func".equals(list.get(j+1))){
                 i++;
             }
         }
 
         System.err.println("\nDEC STATEMENTS: "+storeAllDecVar);
 
         //Type Checking comes in
         Boolean verifyDT = true;
         Boolean verifyLR = true;
         for(i=0; i<storeAllDecVar.size(); i=i+4){
             verifyDT = checkType(storeAllDecVar.get(i+1), storeAllDecVar.get(i+2));
             verifyLR = LRCheck(storeAllDecVar.get(i), storeAllDecVar.get(i+2));
             System.err.println(storeAllDecVar.get(i) +" is a "+ storeAllDecVar.get(i+2)+ 
                     " variable with a value of "+ storeAllDecVar.get(i+1)+ " STATUS: "+verifyDT + ", LR Check: " + verifyLR);
 
             if(verifyDT.equals(true)){
               for(j = 0; j<=symbolTable.getLast(); j++){
                  if(symbolTable.table.get(j).tokenValue.equals(storeAllDecVar.get(i)) && symbolTable.table.get(j).datatype.equals(storeAllDecVar.get(i+2))){
                     symbolTable.table.get(j).actualValue = storeAllDecVar.get(i+1);
                  }
               }
                
             }
         }
 }
 
 Boolean checkType(String value, String dataType){
         //sample: x, add, #int
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
 
             int charVerifier = 0;
             //ret = (value.length()>1)? false: true;
             try{ 
                 Integer.parseInt(value);
                 charVerifier = 1;
             }catch(NumberFormatException e) { 
                 charVerifier = 0;
             }
             ret = (value.length()>1 || charVerifier==1)? false: true;
         }
         else if("#boolean".equals(dataType))
             ret = ("false".equals(value)|| "true".equals(value))? true: false;
 
         return ret;
 }
 
 Boolean LRCheck(String leftVal, String dataType){
         Boolean ret = false;
         int i;
         //first: traverse symbolTable, and look for the tokenValue that matches leftVal
         for(i = 0; i<symbolTable.getLast() && (symbolTable.table.get(i).tokenValue==leftVal &&symbolTable.table.get(i).datatype==dataType); i++);
 
         //if loop ended and i is still less than the last index of symbolTable, it means there was a match
         //leftVal is a variable
         if(i<symbolTable.getLast())
             ret = true;
 
         return ret;
 }
 
 void checkFuncCall(){
         
     //store Function Details in an Array List
         int i, j;
         ArrayList<String> storeAllFuncs = new ArrayList<String>();
         
         for(i = 0;i<list.size()-1 && !"#main".equals(list.get(i)); i++){
             if(list.get(i).equals("#func")){
                 storeAllFuncs.add(Integer.toString(i)); //1st: starting index of function in the list
                 storeAllFuncs.add(list.get(i+1)); //Name of Function
                 
                 //count no. of parameters
                 int numOfParam = 0;
                 for(j = i; j<list.size() && !list.get(j).equals(")"); j++){
                     if(list.get(j)=="#int"||list.get(j)=="#char"||list.get(j)=="#boolean")
                         numOfParam++;
                 }
                 storeAllFuncs.add(Integer.toString(numOfParam)); // Number of Parameters
             }
         }
         System.err.println("Function Information (by 3): "+storeAllFuncs);
    
         ArrayList<String> performFunc = new ArrayList<String>();
     //scan list for function calls
         for(i = 0;i<storeAllFuncs.size(); i=i+3){
             
             for(j = 0; j<list.size()-1; j++){
                 
                 if(list.get(j).equals("#"+storeAllFuncs.get(i+1))){
                     System.err.println("Function call at: "+j+" = #"+storeAllFuncs.get(i+1));
                     //storeFuncCalls.add(Integer.toString(j));
                     performFunc = checkFuncDetails(storeAllFuncs, j);
                 }
             }
             
         }
         
 }
 
 ArrayList<String> checkFuncDetails(ArrayList<String> allFuncs, int j){
     int i, countParam=0;
     int flagError = 0;
     ArrayList<String> funcVerify = new ArrayList<String>();
     ArrayList<String> ret = new ArrayList<String>();
     
     for(i=j+1; !")".equals(list.get(i+1)); i++){
         if(!",".equals(list.get(i+1))){    
             countParam++;
             funcVerify.add(list.get(i+1));
         }
     }
     System.err.println(funcVerify);
     
     for(i=0;i<allFuncs.size()-1; i=i+3){
         String FuncName="#"+allFuncs.get(i+1);
         if(FuncName.equals(list.get(j))){
             if(countParam!=Integer.parseInt(allFuncs.get(i+2))){
                 System.err.println("Number of parameters in function call at line "+j+" does not match with function.");
                 flagError = 1;
             }
         }
     }
     ArrayList<Integer> storeFuncInList = new ArrayList<Integer>();
     String callingFunc = null;
     
     //store all indeces of function types found in the arraylist of tokens
     for(i = 0;i<list.size();i++){
         if(list.get(i).equals("#func") || list.get(i).equals("#main"))
             storeFuncInList.add(i);
     }
     storeFuncInList.add(list.size()-1);
     
     for(i = 0; i<storeFuncInList.size()-1; i++){
         if(j>storeFuncInList.get(i) && j<storeFuncInList.get(i+1)){
             if(list.get(storeFuncInList.get(i)).equals("#func"))
                 callingFunc = list.get(storeFuncInList.get(i)+1);
             
             else callingFunc = "#main";
         }
     }
      System.err.println("CALLING FUNC : " + callingFunc);   
 
      //perform function
     if(flagError!=1){
         //traverse in symbolTable and look for matching calling function and tokenValue
         for(i=0; i<symbolTable.getLast(); i++){
            /* for(j=0; j<funcVerify.size();j++){
                 System.err.println("TEST THIS: "+symbolTable.table.get(i).tokenValue + " "+funcVerify.get(j) + symbolTable.table.get(i).scope+callingFunc);
                 if(symbolTable.table.get(i).tokenValue.equals(funcVerify.get(j)) && symbolTable.table.get(i).scope.equals(callingFunc)){
                     ret.add(Integer.toString(i));//symbolTable index
                     ret.add(symbolTable.table.get(i).tokenValue);
                     ret.add(symbolTable.table.get(i).actualValue);
                     System.err.println("NISULOD");
                 }
             }*/
             int flagCounter = 0;
             for(j=0; j<funcVerify.size() && flagCounter==0;j++){
                 if(symbolTable.table.get(i).tokenValue.equals(funcVerify.get(j)) && symbolTable.table.get(i).scope.equals(callingFunc)){
                     flagCounter = 1;
                     j--;
                 }
             }
             if(j<funcVerify.size())
                 ret.add(Integer.toString(i));//symbolTable index
         }
     }
 System.err.println("index in symboltable: "+ret);
     return ret;
 }

void performFunc(){
    
}
 }
