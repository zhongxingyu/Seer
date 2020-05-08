 package jdressel.Derporia64;
 // James Dressel and James Robertson
 // Based on the Hello.java example
 // Import Servlet Libraries
 import javax.servlet.*;
 import javax.servlet.http.*;
 
 // Import Java Libraries
 import java.io.*;
 import java.util.*;
 
 
 public class Voting extends HttpServlet {
 private HttpServletRequest request;
 private final String styleSheet = "";
 
 public void doGet(HttpServletRequest request, HttpServletResponse res) throws ServletException, IOException {
 	this.request = request;	
 	printTop(res);
 	
 	printBody(request, res);
 	
 	printBottom(res);
 
     }
 	
 	public void printTop(HttpServletResponse res) throws IOException{
 		res.setContentType ("text/html");
 		PrintWriter out = res.getWriter();
 		
 		out.println("<!DOCTYPE html>");
 		out.println("<html><head>");
 		out.println("<meta http-equiv=\"content-type\" content=\"text/html\"; charset=\"UTF-8\">");
 		out.println("<meta charset=\"UTF-8\">");
 		out.println("<title>Derporia: the never-ending land of claimDEFWFWWEFs</title>");
 
 		out.println("<!-- A project by James Dressel and James Robertson -->");
 		out.println("<link rel=\"stylesheet\" type=\"text/css\" media=\"screen\" href=\"http://apps-swe432.vse.gmu.edu:8080/swe432/jsp/jdressel/Derporia64/derporiaVotingStyle.css\" />");
 
 		out.println("<script src=\"http://apps-swe432.vse.gmu.edu:8080/swe432/jsp/jdressel/Derporia64/votingStyle.js\" /> </script>");
 		out.println("</head>");
 
 		out.println("<body>");
 		out.println("<h1>Derporia: the never-ending land of <del>baseless</del> claims</h1>");
 
 		Object derp  = request.getSession().getAttribute("username")==null ? "" : request.getSession().getAttribute("username");
 	out.println(Utility.loginHeaderBanner(derp));
 		request.getSession().setAttribute("loginRequester","http://apps-swe432.vse.gmu.edu:8080/swe432/servlet/jdressel.Derporia64.Voting");
 		out.println("<a href=\"http://apps-swe432.vse.gmu.edu:8080/swe432/jsp/jdressel/Derporia64/Derporia.jsp\" class=\"center\"> Make and Assertion!</a>");
 
 	}
 	
 	public void printBody(HttpServletRequest request, HttpServletResponse res) throws IOException{
 		res.setContentType ("text/html");
 		PrintWriter out = res.getWriter();
 		HttpSession session = request.getSession();
 		
 		Object d = getServletContext().getAttribute("jdresselAssertionSet");
 		
 		if(d==null){
 			out.println("<p>There are currently no Assertions :(</p>");
 		} else {
 			@SuppressWarnings("unchecked")
 			Set<Assertion> assertions = (Set<Assertion>)d;
 
 			out.println("							<table class=\"mega\">");
 
 			for(Iterator<Assertion> assertionIterator = assertions.iterator(); assertionIterator.hasNext();){
 				Assertion assertion = assertionIterator.next();
 						out.println("<a href=\"http://apps-swe432.vse.gmu.edu:8080/swe432/servlet/jdressel.Derporia64.ProcessVote?vote=convinced&id=" + assertion.getId() +" \">Convinced</a>");
 out.println("<a href=\"http://apps-swe432.vse.gmu.edu:8080/swe432/servlet/jdressel.Derporia64.ProcessVote?vote=disagree&id=" + assertion.getId() +" \">disagree</a>");
 out.println("<a href=\"http://apps-swe432.vse.gmu.edu:8080/swe432/servlet/jdressel.Derporia64.ProcessVote?vote=unsure&id=" + assertion.getId() +" \">unsure</a>");
 
 				out.println("								<tr>");
 				out.println("									<td class=\"mega\">");
 				out.println("											<table class=\"center\">");
 				out.println("											<tbody>");
 				out.println("												<tr>");
 				out.println("													<td>");
				out.println("														<p>Post by <a href=\"http://apps-swe432.vse.gmu.edu:8080/swe432/jsp/jdressel/"+assertion.getUN()+".html\">"+assertion.getUN()+"</a></p>");
 				out.println("													</td>");
 				out.println("												</tr>");
 				out.println("												<tr>");
 				String username = (session.getAttribute("username") == null)?"":session.getAttribute("username").toString(); 
 				User user = new User(username);
 /*				if(username.equals(assertion.getUN())){
 					if(user.getConvinced().contains(assertion))
 						out.println("													<td class=\"green\">");
 					if(user.getUnsure().contains(assertion))
 						out.println("													<td class=\"yellow\">");
 					if(user.getDisagree().contains(assertion))
 						out.println("													<td class=\"red\">");
 				}
 				else*/
 					out.println("													<td>");
 				out.println("													<p class=\"center\"><b>Claim: <br />"+assertion.getName()+"</b></p>");
 				out.println("													<p class=\"center\"><i>Assertions: <br />"+assertion.getBody()+"</i></p>");
 				out.println("													<p>Convinced: "+assertion.getConvinced()+"<br>");
 				out.println("														Unsure:    "+assertion.getUnsure()+"<br>");
 				out.println("														Disagree:  "+assertion.getDisagree()+"</p>");
 				out.println("													</td>");
 				out.println("												</tr>");
 				out.println("											</tbody>");
 				out.println("											</table>		");
 				out.println("											<table class=\"center\">");
 				out.println("											<tbody><tr>");
 				out.println("												<td>");
 				out.println("												<table width=\"100%\">");
 				out.println("													<tbody><tr>");
 				out.println("													<td align=\"left\">");
 				out.println("														<button type=\"button\" class=\"red\" name=\"disagree\">Disagree</button> ");
 				out.println("													</td>");
 				out.println("													<td align=\"center\">");
 				out.println("														<button type=\"button\" class=\"yellow\" name=\"unsure\">Unsure</button> ");
 				out.println("													</td>");
 				out.println("													<td align=\"right\">");
 				out.println("														<button type=\"button\" class=\"green\" name=\"convinced\">Convinced</button> ");
 				out.println("													</td>");
 				out.println("													</tr>");
 				out.println("												</tbody></table>");
 				out.println("												</td>");
 				out.println("											</tr>");
 				out.println("											</tbody>");
 				out.println("											</table>");
 				out.println("									</td>");
 				out.println("								</tr>");
 				}
 				out.println("							</table>");
 		}
 			
 	}
 		
 	public void printBottom(HttpServletResponse res) throws IOException{
 		res.setContentType ("text/html");
 		PrintWriter out = res.getWriter();
 		
 		out.println("							<h4>By James Robertson and James Dressel</h4>");
 		out.println("							</body>");
 		out.println("							</html>");
 		
 		out.close();
 	}
 }
