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
             printParseTree(iCode, "", false);
             System.out.println();
         }
 
         System.out.println("\n----------Printing Symbol Table---------\n");
         printSymbolTableStack(symbolTableStack);
     }
 
 
     public void printParseTree(IntermediateCode intermediateCode, String space, boolean prevQuote) {
         if (intermediateCode == null) {
             return;
         }
         boolean pQuote = prevQuote;
 
         if (intermediateCode.getText() != null) {
             if (intermediateCode.getText().compareTo("'") == 0) {
                 System.out.print("\n");
                 System.out.print(space);
                 System.out.print(intermediateCode.getText());
                 pQuote = true;
             }else {
                 if (intermediateCode.getText().equalsIgnoreCase("define")){
                     if (pQuote) {
                         pQuote = false;
                     }else{
                         System.out.print("\n" + space);
                     }
                     space = space + "  ";
                     System.out.print("(");
                 }
                 System.out.print(intermediateCode.getText() + ' ');
             }
         }else {
             if (pQuote) {
                 pQuote = false;
             }else{
                 System.out.print("\n" + space);
             }
             space = space + "  ";
             System.out.print('(');
         }
 
         printParseTree(intermediateCode.getCar(), space, pQuote);
         printParseTree(intermediateCode.getCdr(), space, pQuote);
 
         if (intermediateCode.getCdr() == null) {
             System.out.print(")");
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
