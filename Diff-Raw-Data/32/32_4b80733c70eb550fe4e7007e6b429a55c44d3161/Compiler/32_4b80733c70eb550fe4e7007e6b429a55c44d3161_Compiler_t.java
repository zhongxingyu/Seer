import java.io.BufferedReader;
 import java.io.FileNotFoundException;
 import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
 import java.io.UnsupportedEncodingException;
 import java.io.PrintWriter;
 
import org.omg.CORBA.portable.InputStream;

 public class Compiler {
     public static void main(String[] args) {
		String returnMessage = "";
 		PrintWriter writer;
 		try {
 			//writer = new PrintWriter("C:\\Users\\johnsond\\Documents\\NJIT Classes\\CS 490 (Design in Software Engineer)\\cs490\\Text.txt", "UTF-8");
 			String preCode = "public class sourceCode {"
 					+ "public static void main(String[] args) {";
 			String endCode = "}}";
 			writer = new PrintWriter("/afs/cad/u/d/j/dj65/public_html/cs490/output/sourceCode.java", "UTF-8");
 			writer.println(preCode);
 			writer.println(args[0]); 
 			writer.println(endCode);
 			writer.close();
 			Runtime rt = Runtime.getRuntime();
 			try {
 				Process pr = rt.exec("javac /afs/cad/u/d/j/dj65/public_html/cs490/output/sourceCode.java");
				String s = null;
		        BufferedReader stdInput = new BufferedReader(new InputStreamReader(pr.getInputStream()));
		        BufferedReader stdError = new BufferedReader(new InputStreamReader(pr.getErrorStream()));
		           returnMessage = "Here is the standard output of the command:\n\n";
		           while ((s = stdInput.readLine()) != null) {
		        	   returnMessage += s+"\n";
		           }
		           returnMessage += "\nHere is the standard error of the command (if any):\n\n";
		           while ((s = stdError.readLine()) != null) {
		        	   returnMessage += s+"\n";
		           }
 			} catch (IOException e) {
			//} catch (Exception e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
				String error = e.getMessage();
				returnMessage = "Get some text";
				writer = new PrintWriter("/afs/cad/u/d/j/dj65/public_html/cs490/output/error.txt", "UTF-8");
				writer.println(error);
				writer.close();
 			}
 		} catch (FileNotFoundException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace(); 
 		} catch (UnsupportedEncodingException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
		System.out.println(returnMessage);
     }
 }  
