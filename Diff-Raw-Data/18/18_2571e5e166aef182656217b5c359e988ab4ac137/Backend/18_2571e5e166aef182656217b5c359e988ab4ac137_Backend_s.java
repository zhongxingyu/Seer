 package backend;
 
 import intermediate.IntermediateCode;
 import intermediate.SymbolTableStack;
 
 import java.io.IOException;
 
 public class Backend {
     public void process(IntermediateCode intermediateCode,
                         SymbolTableStack symbolTableStack) throws IOException
     {
         printParseTree(intermediateCode);
     }
 
     public void printParseTree(IntermediateCode intermediateCode) {
         if (intermediateCode == null) {
             return;
         }
 
         if (intermediateCode.getText() != null) {
            System.out.println(intermediateCode.getText() + '\t' + intermediateCode.getType());
         }
 
         printParseTree(intermediateCode.getCar());
         printParseTree(intermediateCode.getCdr());
     }
 }
