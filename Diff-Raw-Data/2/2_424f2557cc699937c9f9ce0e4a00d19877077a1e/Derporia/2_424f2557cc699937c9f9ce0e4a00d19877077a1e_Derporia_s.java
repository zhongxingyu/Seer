 package jdressel.DerporiaPremium;
 // James Dressel and James Robertson
 // Based on the Hello.java example
 // Import Servlet Libraries
 import javax.servlet.*;
 import javax.servlet.http.*;
 
 // Import Java Libraries
 import java.io.*;
 
 
 public class Derporia extends HttpServlet
 {
 
 public void doGet  (HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
    {
 
 	res.setContentType ("text/html");
 	PrintWriter out = res.getWriter ();
 
 	out.println("<!DOCTYPE html>");
 	out.println("<html>");
 	out.println("<head>");
 	out.println("<meta http-equiv=\"content-type\" content=\"text/html; charset=UTF-8\">");
 	out.println("<meta charset=\"UTF-8\">");
 	out.println("<title>Derporia: the never-ending land of claims</title>");
 	out.println("");
 	out.println("<!-- A project by James Dressel and James Robertson -->");
 	out.println("<link rel=\"stylesheet\" type=\"text/css\" href=\"http://mason.gmu.edu/~jrobertq/derporia/derporiaStyle.css\">");
 	out.println("");
 	out.println("<script src=\"http://mason.gmu.edu/~jrobertq/derporia/derporiaAssert.js\"> </script>");
 	out.println("</head>");
 	out.println("");
 	out.println("	<body>");
 	out.println("	<h1>Derporia: the never-ending land of <del>baseless</del> claims</h1>");
 	out.println("	<hr>");
 	out.println("	<br>");
 	out.println("	<h1>James Robertson and James Dressel</h1>");
 	out.println("	<hr>");
 	out.println("	<br>");
 	out.println("	<p>");
 	out.println("		Please enter make a claim and then assertions to support your claim. ");
 	out.println("	</p>");
 	out.println("	<table class=\"center\" border=\"1\">");
 	out.println("");
 	out.println("");
 	out.println("");
 	out.println("	<tbody><tr>");
 	out.println("		<td>");
	out.println("		<form name=\"assertionForm\"  onsubmit=\"return validateForm()\" action=\"http://apps-swe432.vse.gmu.edu:8080/swe432/servlet/jdressel.Results\" method=\"post\">");
 	out.println("");
 	out.println("			<textarea cols=\"30\" rows=\"1\" name=\"claim\" onclick=\"clearOnClick(this, \'Enter your claim\')\" onblur=\"defaultOnBlur(this, \'Enter your claim')\" autofocus=\"autofocus\">Enter your claim</textarea>");
 	out.println("		<br>");
 	out.println("			<textarea cols=\"30\" rows=\"5\" name=\"assertions\" size=\"20\" onclick=\"clearOnClick(this, \'Enter your assertions\')\" onblur=\"defaultOnBlur(this, \'Enter your assertions\')\">Enter your assertions</textarea>");
 	out.println("		<br>");
 	out.println("		</td>");
 	out.println("		</tr></tbody></table><table align=\"center\" width=\"14%\">");
 	out.println("		<tbody><tr>");
 	out.println("		<td align=\"left\">");
 	out.println("			<button type=\"button\" onclick=\"resetForm()\">Reset</button> ");
 	out.println("		</td>");
 	out.println("		<td align=\"right\">");
 	out.println("			<input type=\"submit\" name=\"Submit\"/>");
 	out.println("		</td>");
 	out.println("		</tr>");
 	out.println("		</tbody></table>");
 	out.println("		</form>");
 	out.println("	");
 	out.println("");
 	out.println("</body>");
 	out.println("</html>");
 
         out.close ();
 
     }
 }
