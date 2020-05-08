 package magelets;
 
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.util.Map;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 
 /**
  * Servlet implementation class optMAGE_1
  */
 public class optMAGE_1 extends Magelet {
 	
 	private static final long serialVersionUID = 1L;
     
 	private static final String validHeaders = "validate.txt";
     private static final String script = "optMAGEv0.9.pl";
     private static final String oligoFile = "OUToligos.txt";
     private static final String dumpFile = "OUTalldump.txt";
     private static final String inputParameterFileName = "INPUTparam.txt";
     private static final String servletFolder ="/optMage_1/";
     private static final String inputTargetFileName = "INPUTtarg.txt"; 
 
     /**
      * @see Magelet#Magelet()
      */
     public optMAGE_1() {
         super();
         
     }
 
 	/**
 	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
 	 */
 	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
 		
 		// Set response type and create an output printer
 		response.setContentType("application/x-www-form-urlencoded");
 		PrintWriter out = response.getWriter();
 
 		// Get Servlet Directory
 		String directory = getDirectory(servletFolder) + "/";
 		System.out.println("POST Received");
 		
 		// Load and validate parameters
 		Map<String,String[]> parameters = load(request);
 				
 		try{
 			if (validate(directory+validHeaders,parameters)) {
 				
 				TextFile.deleteIfPossible(directory+oligoFile);
 				TextFile.deleteIfPossible(directory+dumpFile);
 			    TextFile.deleteIfPossible(directory+inputTargetFileName);
 			    TextFile.deleteIfPossible(directory+inputParameterFileName);
 				
 				// Generate the inputParameterFile and inputTargetFile and write those to file system
 				generate(directory, inputParameterFileName, MageEditor.PARAMETER, parameters );
 				generate(directory, inputTargetFileName, MageEditor.TARGET, parameters );
 				
 				// Something related to the genome would go here
 				
 				// Execute the optMAGE script
 				execute(directory, script);
 				
 				// Read the MAGE results;
 			    this.map.put( MageEditor.RESULT, TextFile.getLinesAsArray(directory+oligoFile ));
 			    this.result = this.buildURLfromMap();
 			    
 			    // Delete the files we just created
 			    //TextFile.delete(directory+oligoFile);
 			    TextFile.deleteIfPossible(directory+dumpFile);
 			    // Something about renaming the genome file would go here.
 			    //TextFile.delete(directory+inputTargetFileName);
 			    //TextFile.delete(directory+inputParameterFileName);
 			}
 			//else { this.result = "Invalid Request Parameters"; }
 		}
 		catch (Exception EE){ 
 			EE.printStackTrace();
 			this.map.remove(MageEditor.ERROR);
			this.map.put(MageEditor.ERROR, new String[] {EE.toString()});
 			this.result = this.buildURLfromMap();
 		}
 		finally{
 			
 			// Print to console and return the results
 			out.write(this.result);
 			System.out.println(this.result);
 			
 			// Close the output Stream.
 			out.close();
 		}
 		
 	}
 }
