 package backend;
 
 import intermediate.IntermediateCode;
 import intermediate.SymbolTable;
 import intermediate.SymbolTableEntry;
 import intermediate.SymbolTableStack;
 
 import java.io.IOException;
 import java.util.ArrayList;
 
 public class Backend {
     public void process(ArrayList<IntermediateCode> intermediateCodes,
                         SymbolTableStack symbolTableStack) throws IOException
     {
         System.out.println("\n----------Printing Parse Tree---------\n");
         for(IntermediateCode iCode : intermediateCodes) {
             printParseTree(iCode);
             System.out.println();
         }
 
 
         System.out.println("\n----------Printing Symbol Table---------\n");
         printSymbolTableStack(symbolTableStack);
     }
 
     public void printParseTree(IntermediateCode intermediateCode) {
         if (intermediateCode == null) {
             return;
         }
 
         if (intermediateCode.getText() != null) {
             if (intermediateCode.getText().compareTo("'") == 0) {
                 System.out.print(intermediateCode.getText());
             }
             else {
                 System.out.print(intermediateCode.getText() + ' ');
             }
         }
         else {
             System.out.print('(');
         }
 
         printParseTree(intermediateCode.getCar());
         printParseTree(intermediateCode.getCdr());
 
        if (intermediateCode.getCar() == null && intermediateCode.getCdr()==null) {
             System.out.println(')');
         }
     }
 
     public void printSymbolTableStack(SymbolTableStack symbolTableStack) {
         for (SymbolTable table : symbolTableStack) {
             for (SymbolTableEntry entry : table.values()) {
                 System.out.println(entry);
             }
         }
     }
 }
