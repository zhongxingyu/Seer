 package helpers;
 
 import java.util.*;
 
 import Accounts.Account;
 
 
 public class HTMLHelper {
 	private static final String homeLink = "<li class=header style=\"float:left\"><a class=header href=\"HomePage\">Moses-Foe Quiz Project</a></li>";
 	private static final String takeQuizLink = "<li class=header><a class=header href=\"QuizCatalogServlet\">Take a Quiz!</a></li>";
 	private static final String createLink = "<li class=header><a class=header href=\"BeginQuizCreationServlet\">Create Quiz</a></li>";
 	private static final String statsLink = "<li class=header><a class=header href=\"SiteStatsServlet\">Site Stats</a></li>";
 	private static final String funFact = "<li class=header><a class=header href=\"http://en.wikipedia.org/wiki/Special:Random\">Fun Fact</a></li>";
 	
 	private HTMLHelper(){
 	
 	}
 	
 	public static String printHeader(Account loggedDude){
 		if (loggedDude != null){
 			return printFullHeader(loggedDude.isAdmin());
 		}
 		else{
 			return printBasicHeader();
 		}
 	}
 	
 	public static String printCSSLink(){
 		return "<link rel=\"stylesheet\" type=\"text/css\" href=\"mufasa.css\">";
 	}
 	
 	public static String contentStart(){
 		return "<div class=content>";
 	}
 	
 	public static String contentEnd(){
 		return "</div>";
 	}
 	
 	public static String printFullHeader(boolean isAdmin){
 		StringBuilder fullHeader = new StringBuilder();
 		fullHeader.append("<div class=header><ul class=header>");
 		fullHeader.append(homeLink);
 		fullHeader.append(funFact);
 		fullHeader.append(takeQuizLink);
 		fullHeader.append(createLink);
 		if (isAdmin) fullHeader.append(statsLink);
		fullHeader.append("</ul></div><div style=\"min-height:80px\"></div>");
 		return fullHeader.toString();
 	}
 	
 	public static String printBasicHeader(){
 		StringBuilder fullHeader = new StringBuilder();
 		fullHeader.append("<div class=header><ul class=header>");
 		fullHeader.append(homeLink);
 		fullHeader.append(funFact);
 		fullHeader.append("</ul></div><div style=\"min-height:80px\"></div>");
 		return fullHeader.toString();
 	}
 	
 	public static String printQuizListing(int id, String name){
 		StringBuilder listing = new StringBuilder();
 		listing.append("<div class=quiz>");
 		listing.append("<h3>"+name);
 		listing.append("<a class=quiz href= \"QuizTitleServlet?id="+id+"\">Take Quiz</a>");
 		listing.append("</h3></div>");
 		
 		return listing.toString();
 	}
 }
