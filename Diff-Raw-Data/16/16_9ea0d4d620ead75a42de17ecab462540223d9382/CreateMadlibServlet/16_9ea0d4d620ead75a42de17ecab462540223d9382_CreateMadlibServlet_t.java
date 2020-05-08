 package madlibs;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.Scanner;
 
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 @SuppressWarnings("serial")
 
 
 public class CreateMadlibServlet extends HttpServlet {
 	public void doPost(HttpServletRequest req, HttpServletResponse resp)
 	throws IOException {
 		doGet(req, resp);
 	}
 	
 	public void doGet(HttpServletRequest req, HttpServletResponse resp)
 			throws IOException {
 		
 		// web response: set the content type to be text/html
 		resp.setContentType("text/html");
 
 //		resp.setHeader("style", head);
 		
 		// get the file info
 		// to get the value of each input, always use the name and then
 		// req.getParameter(name);
 		String fileName = req.getParameter("file");
 		
 		// create the file
 		File file = new File(fileName);
 		
 		// check if file (template) exists
 		if ( file.exists() ) {
 			
 			
 			// output HTML Form header
 			formHeader( resp );
 			
 			// Output a Form element to remember file (template) name
 			formElement(resp, "", "hidden", "file", fileName);
 			
 			Scanner scan = new Scanner(file);
 			genPlaceHolders(scan, resp);
 			
 			// the last element in the HTML Form is submit.
 			formElement(resp, "", "mybutton", "submit", "display", "Submit");
 			
 			formFooter( resp );
 			
 		} else {
 			resp.getWriter().printf("Sorry, file %s does not exist.\n", file);
 		}
 	} // end of doPost
 	
 	// Scan file to find all placeholders
 	public static void genPlaceHolders(Scanner scanTemplate, HttpServletResponse resp) 
               throws IOException {
 	
 		int phIndex = 1;
 		int startPH = 0; int endPH = 0; phIndex = 1;
 		while (scanTemplate.hasNextLine()) {
 			endPH = 0;
 			String line = scanTemplate.nextLine();
 			while ((endPH + 2 < line.length()) && (line.substring(endPH).contains("<"))) {
 				line = line.substring(endPH);
 				if (line.contains("<") && 											//the line has a <
 						(line.substring(line.indexOf("<")).contains(">")) && 		//followed by a >
 						(line.substring(line.indexOf("<")).indexOf(">") > 1) &&		//with at least one character
 						!(line.substring(line.indexOf("<"), (line.indexOf("<") + 	//but no spaces in between them
 								line.substring(line.indexOf("<")).indexOf(">"))).contains(" "))) {
 					//find start/finish index of next placeholder
 					startPH = line.indexOf("<");
 					endPH = startPH + line.substring(line.indexOf("<")).indexOf(">");
 
 					//From the in-between space, make ithPH (all lowercase, replace - with " ")
 					String ithPH = line.substring(startPH + 1, endPH);
 					ithPH = ithPH.toLowerCase();
 					ithPH = ithPH.replace('-', ' ');
 
 					//Create a form element for this word:
 					formElement(resp, ithPH + ": ", "text", "ph" + phIndex, "");
 					phIndex++;
 					
 				} //end if contains <, >, !<>, !<a b>
 			} //end if the remainder of the line has a <
 		}//end while hasNextLine
 	
 	
 	
 	}
 	
 	public static void formHeader(HttpServletResponse resp) throws IOException {
		String head = "<head> <style type=\"text/css\">\n @font-face {font-family: 'Cubano'; font-style: normal; src: local(\"Cubano-Regular\"), url(./fonts/Cubano-Regular.otf) format(\"opentype\"); }\n@font-face { font-family: 'Bariol'; font-style: normal; src: local(\"Bariol_Regular\"), url(./fonts/Bariol_Regular.otf) format(\"opentype\"); } \nbody { background-image: url('wavegrid.png'); background-color: #DDDDDD;} span.footer { font-family: Bariol; font-size: 18px; padding-top: 5px; text-align: center; }  h1 {font-family: Cubano; font-size: 200px; margin-top: -50px; color: #666666; letter-spacing: -5px; } table { width: 600px; margin-left: auto; margin-right: auto; margin-top: -150px; } td { font-family: Bariol; font-size: 35px; line-height: 150%; text-align: right; color: #333333; } input {font-size:35px; border: 3px solid #333333; border-radius: 5px; font-family: Bariol; font-weight: bold; } input.mybutton {  font-family: Bariol;  font-size: 35px; font-weight: bold; color: #DDDDDD; background-color: 006699; } </style> </head>";
 		resp.getWriter().println(head);
 		resp.getWriter().println("<center>");
 		resp.getWriter().println("<h1>WORDS</h1>");
 		resp.getWriter().println("<form action=\"/display\" method=\"get\">");
 		resp.getWriter().println("<table>");
 	}
 	
 	public static void formElement(HttpServletResponse resp, String label, String type, String name, String value) throws IOException {
 		resp.getWriter().printf("<div><tr><td>%s</td><td><input type=\"%s\" name=\"%s\" value=\"%s\" autocomplete=\"off\"></td></tr></div>\n",
 					             label, type, name, value);
 					
 	}
 	
 	public static void formElement(HttpServletResponse resp, String label, String clss, String type, String name, String value) throws IOException {
 		resp.getWriter().printf("<div><tr><td>%s</td><td><input class=\"%s\" type=\"%s\" name=\"%s\" value=\"%s\"></td></tr></div>\n",
 					             label, clss, type, name, value);
 					
 	}
 	
 	public static void formFooter(HttpServletResponse resp) throws IOException {
 		resp.getWriter().println("</table>");
		resp.getWriter().println("</form><br><br>");
		resp.getWriter().println("<span class=\"footer\"> Mikayla Thompson | March 2013 | Yale CS112 </span>");
 		resp.getWriter().println("</center>");
 	}
 }
