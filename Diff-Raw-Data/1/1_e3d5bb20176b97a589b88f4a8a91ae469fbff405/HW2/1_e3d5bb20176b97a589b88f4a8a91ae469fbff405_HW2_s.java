 package homework2;
 
 import java.io.*;
 import org.antlr.runtime.*;
 
     public class HW2 {
     	
     static OpenInventorData oid;
 
     public static void main(String[] args) throws FileNotFoundException, IOException, RecognitionException {
     	
     //Parsing Input
    	//ANTLRInputStream input = new ANTLRInputStream(new FileInputStream(FILE));
     	ANTLRInputStream input = new ANTLRInputStream(System.in);
         OpenInventorLexer lexer = new OpenInventorLexer(input);
         CommonTokenStream tokens = new CommonTokenStream(lexer);
         OpenInventorParser parser = new OpenInventorParser(tokens);
                 	
         oid = parser.openinventor();
 
     //Pixel Size of Window
         int xRes, yRes;
         if (args.length < 2) {
             xRes = 400;
             yRes = 400;
         }
         else {
             xRes = Integer.parseInt(args[0]);
             yRes = Integer.parseInt(args[1]);
         }
 
     //Run Wireframe Program
         Wireframe w = new Wireframe(oid);
         w.wireframe(xRes, yRes);
         
         //System.out.println("End of Program");
         
         System.exit(0);
     }
 
 }
