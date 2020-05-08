 package jdressel.Derporia64;
 //James Dressel and James Robertson
 import java.io.IOException;
 import javax.servlet.*;
 import javax.servlet.http.*;
 import java.io.*;
 import java.util.Iterator;
 import java.util.*;
 
 
 public class ProcessClaim extends HttpServlet {
 
 private String claim;
 private String assertions;
 
 
 private String username;
 public void doGet(HttpServletRequest request, HttpServletResponse res)
 throws ServletException, IOException  {
  
 	/*
 	* Send the user back to where they belong
 	*/
  
 	String destination="http://apps-swe432.vse.gmu.edu:8080/swe432/jsp/jdressel/Derporia64/Derporia.jsp";
 	res.sendRedirect(res.encodeRedirectURL(destination));
  
  }
 
 public void doPost(HttpServletRequest request, HttpServletResponse res) throws ServletException, IOException {
 	HttpSession session = request.getSession();
 	setClaimAssertion(request);
 	if(session.getAttribute("username")==null){
 		res.sendRedirect(res.encodeRedirectURL("http://apps-swe432.vse.gmu.edu:8080/swe432/jsp/jdressel/Derporia64/Login.jsp"));//ERROR, send the user back
 	} else {
		Assertion a = new Assertion(session.getAttribute("username").toString(), claim, assertions);
 		if(getServletContext().getAttribute("jdresselAssertionSet")==null){
 			Set<Assertion> assertions = new HashSet<Assertion>();
 			getServletContext().setAttribute("jdresselAssertionSet",assertions);
 		} 
 		Object d = getServletContext().getAttribute("jdresselAssertionSet");
 		Set setOfAssets = (Set)d;
 		setOfAssets.add(a);
 		getServletContext().setAttribute("jdresselAssertionSet",setOfAssets);
 				
 
 
 		res.sendRedirect(res.encodeRedirectURL("http://apps-swe432.vse.gmu.edu:8080/swe432/servlet/jdressel.Derporia64.Voting"));
 	}
 
 	
 
 
 }
 
 private void setClaimAssertion(HttpServletRequest request){
 		claim = request.getParameter("claim")==null ? "" : request.getParameter("claim");
 		assertions = request.getParameter("assertions")==null ? "" : request.getParameter("assertions");
 	}
 
 
 }
