 package jdressel.Derporia64;
 //James Dressel and James Robertson
 import java.io.IOException;
 import javax.servlet.*;
 import javax.servlet.http.*;
 import java.io.*;
 import java.util.*;
 
 
 public class ProcessVote extends HttpServlet {
 
 private String vote;
 
 private Assertion underVote;
 private String id;
 public void doGet(HttpServletRequest request, HttpServletResponse res) throws ServletException, IOException  {
  
 	/*
 	* Send the user back to where they belong
 	*/
  
 	//String destination="http://apps-swe432.vse.gmu.edu:8080/swe432/servlet/jdressel.Derporia64.Voting";
 	//res.sendRedirect(res.encodeRedirectURL(destination));
  	HttpSession session = request.getSession();
 	setVariables(request);
 	if(session.getAttribute("username")==null){
 		res.sendRedirect(res.encodeRedirectURL("http://apps-swe432.vse.gmu.edu:8080/swe432/jsp/jdressel/Derporia64/Login.jsp"));//ERROR, send the user back
 	} else {
 
 		//TODO check for empty
 
 		Object d = getServletContext().getAttribute("jdresselAssertionSet");
 	
 		if(d==null){
 			//TODO
 		} else {
 		Set<Assertion> assertions = (Set<Assertion>)d;
 			
 	
 
 		for(Iterator<Assertion> assertionIterator = assertions.iterator(); assertionIterator.hasNext();){
 			Assertion assertion = assertionIterator.next();
 			
 			if(assertion.getId().equals(id)){
 				
 				underVote = assertion;
 	
				if(underVote.getUN().equals(((User) session.getAttribute("username")).getUN())||(User) session.getAttribute("username")==null){
 					//out.println("same");
 					//out.println("<p class=/"error/">You cannot vote on your own post!<p>");
 					//res.sendRedirect(res.encodeRedirectURL("http://reddit.com"));
 				}
 				else if(vote.equals("convinced")){
 					if(underVote.getVoters().contains((User)session.getAttribute("username"))&&((User) session.getAttribute("username")).getConvinced().contains(underVote)) { //User has voted on this post in this way
 						//TODO alert user they have already voted in this way
 					}
 					else{
 						underVote.voteConvinced((User)session.getAttribute("username"));
 						assertions.add(underVote);
 						((User) session.getAttribute("username")).voteConvinced(underVote);
 					}
 				}
 
 
 				else if(vote.equals("unsure")){
 				if(underVote.getVoters().contains((User)session.getAttribute("username"))&&((User) session.getAttribute("username")).getUnsure().contains(underVote)) { //User has voted on this post in this way
 						//TODO alert user they have already voted in this way
 					}
 					else{
 						underVote.voteUnsure((User)session.getAttribute("username"));
 						assertions.add(underVote);
 						((User) session.getAttribute("username")).voteUnsure(underVote);
 					}
 				}
 
 
 				else if(vote.equals("disagree")){
 					if(underVote.getVoters().contains((User)session.getAttribute("username"))&&((User) session.getAttribute("username")).getDisagree().contains(underVote)) { //User has voted on this post in this way
 						//TODO alert user they have already voted in this way
 					}
 					else{
 						underVote.voteDisagree((User)session.getAttribute("username"));
 						assertions.add(underVote);
 						((User) session.getAttribute("username")).voteDisagree(underVote);
 					}
 				}
 
 
 				assertions.add(underVote);
 				//getServletContext().setAttribute("jdresselAssertionSet",assertions);
 				res.sendRedirect(res.encodeRedirectURL("http://apps-swe432.vse.gmu.edu:8080/swe432/servlet/jdressel.Derporia64.Voting"));
 			}
 		
 		}
 
 		
 	}
 
 	
 
 
 }
  }
 
 private void setVariables(HttpServletRequest request){
 		vote = request.getParameter("vote")==null ? "" : request.getParameter("vote");
 		id = request.getParameter("id")==null ? "" : request.getParameter("id");
 	}
 
 
 }
