 package homework3;
 
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 
 import org.antlr.runtime.ANTLRInputStream;
 import org.antlr.runtime.CommonTokenStream;
 import org.antlr.runtime.RecognitionException;
 
 public class HW3 {
 
	public static void main(String[] args) throws FileNotFoundException, IOException, RecognitionException {
	
	//Parsing Input
		//ANTLRInputStream input = new ANTLRInputStream(new FileInputStream(FILE));
         ANTLRInputStream input = new ANTLRInputStream(System.in);
         OpenInventorNewLexer lexer = new OpenInventorNewLexer(input);
         CommonTokenStream tokens = new CommonTokenStream(lexer);
         OpenInventorNewParser parser = new OpenInventorNewParser(tokens);
                 	
         OpenInventorData oid = parser.openinventor();
         
     //Set Window Size
         int n, xRes, yRes;
         if (args.length < 3) {
             n = 0;
             xRes = 400;
             yRes = 400;
         }
         else {
             n = Integer.parseInt(args[0]);
             xRes = Integer.parseInt(args[1]);
             yRes = Integer.parseInt(args[2]);
         }
         Canvas c = new Canvas (xRes, yRes);
         
     //Run Shader, while saving to canvas
         ShadedRenderer sr = new ShadedRenderer (c);    
         sr.shaded (n, xRes, yRes, oid);
    
     //Generate the ppm based on data in Canvas (to output file called output.txt)
         Canvas.ppmGen(c.grid, xRes, yRes);
         
         System.exit(0);
         
	}
 
 }
