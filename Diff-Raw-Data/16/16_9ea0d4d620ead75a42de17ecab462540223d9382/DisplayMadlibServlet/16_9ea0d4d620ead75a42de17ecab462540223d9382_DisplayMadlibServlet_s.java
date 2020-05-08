 package madlibs;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.Scanner;
 
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 @SuppressWarnings("serial")
 public class DisplayMadlibServlet extends HttpServlet {
 	public void doPost(HttpServletRequest req, HttpServletResponse resp)
 	throws IOException {
 		doGet(req, resp);
 	}
 	
 	public void doGet(HttpServletRequest req, HttpServletResponse resp)
 			throws IOException {
 		resp.setContentType("text/html");
 		
 		outHeader( resp );
 		
 		String fileName = req.getParameter("file");
 		
 		File file = new File(fileName);
 		
 		if ( file.exists() ) {
 			Scanner scan = new Scanner(file);
 			fillPlaceHolders(scan, req, resp);
 		} else {
 			resp.getWriter().println("File does not exist.");
 		}
 		outFooter( resp );
 	} // end of doGet
 	
 	// scan file to find all placeholders
 	public static void fillPlaceHolders(Scanner scanTemplate, 
 			HttpServletRequest req, HttpServletResponse resp) throws IOException {
 		
 		int startPH = 0; int endPH; int endPrevPH;
 		int phIndex = 1;
 		while (scanTemplate.hasNextLine()) {
 			endPH = 0;
 			endPrevPH = 0;
 			resp.getWriter().println();
 			String line = scanTemplate.nextLine();
 			while ((endPrevPH + 2 < line.length()) && (line.substring(endPrevPH).contains("<"))) {
 				String partLine = line.substring(endPrevPH);
 				if (partLine.contains("<") && 											//the line has a <
 						(partLine.substring(partLine.indexOf("<")).contains(">")) && 		//followed by a >
 						(partLine.substring(partLine.indexOf("<")).indexOf(">") > 1) &&		//with at least one character
 						!(partLine.substring(partLine.indexOf("<"), (partLine.indexOf("<") + 	//but no spaces in between them
 								partLine.substring(partLine.indexOf("<")).indexOf(">"))).contains(" "))) {
 					//find start/finish index of next placeholder
 					
 					startPH = partLine.indexOf("<");
 					endPH = startPH + partLine.substring(partLine.indexOf("<")).indexOf(">");
 					
 					if (partLine.substring(0, 1).equals(">")) {
 						resp.getWriter().print(partLine.substring(1, startPH));
 					} else {
 						resp.getWriter().print(partLine.substring(0, startPH));
 					}
 					
 					resp.getWriter().print(req.getParameter("ph" + phIndex));
 					phIndex++;
 									
 
 					endPrevPH += endPH;
 				} //end if contains <, >, !<>, !<a b>
 			} //end while the remainder of the line has a <
 			if (line.substring(endPrevPH).length() > 2) {
 				if (endPrevPH == 0) {
 					resp.getWriter().print(line);
 				} else {
 					resp.getWriter().print(line.substring(endPrevPH + 1));
 				}
 			} //end if length
 		}//end while hasNextLine
 		
 		
 		
 	}
 
 	public static void outHeader(HttpServletResponse resp) throws IOException {
		String head = "<head> <style type=\"text/css\">\n@font-face { font-family: 'Cubano'; font-style: normal; src: local(\"Cubano-Regular\"), url(./fonts/Cubano-Regular.otf) format(\"opentype\"); }\n@font-face { font-family: 'Bariol'; font-style: normal; src: local(\"Bariol_Regular\"), url(./fonts/Bariol_Regular.otf) format(\"opentype\"); }\nbody { background-image: url('wavegrid.png'); background-color: #DDDDDD; } table { width: 500px; margin-left: auto; margin-right: auto; } td.title { font-family: Cubano; text-align: center; font-size: 220px; margin-top: -50px; padding-bottom: 50px; color: #666666; line-height: 70%; letter-spacing: -5px;} pre { text-align: left; font-family: Bariol;font-size: 35px; line-height: 125%; color: #333333; } a { text-decoration: none; font-size: 40px; color: #006699;} </style> </head>";
 		
 		resp.getWriter().println("<html>");
 		resp.getWriter().println(head);
 		resp.getWriter().println("<body><table><tr><td class=\"title\">STORY</td></tr><tr><td><pre>");
 
 	}
 	
 	public static void outLine(HttpServletResponse resp, String line, String value) throws IOException {
 		resp.getWriter().println(line + ": " + value);
 					
 	}
 	
 	public static void outFooter(HttpServletResponse resp) throws IOException {
		resp.getWriter().println("<br><a href=\"index.html\">Play Again</a></pre></td></tr></table></body></html>");
 	}
 }
