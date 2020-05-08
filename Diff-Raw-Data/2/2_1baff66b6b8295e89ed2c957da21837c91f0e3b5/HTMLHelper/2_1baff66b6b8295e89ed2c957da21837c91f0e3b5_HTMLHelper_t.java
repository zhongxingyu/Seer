 package helpers;
 
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.*;
 
 import com.mysql.jdbc.Statement;
 
 import Accounts.Account;
 
 
 public class HTMLHelper {
 	private static final String HOME_LINK = "<li class=header style=\"float:left\"><img src=\"ness.png\" style=\"width:50px; height:50px\"></li><li class=header style=\"float:left\"><a class=header href=\"HomePage\">Kwiz Kid</a></li>";
 	private static final String TAKE_QUIZ_LINK = "<li class=header><a class=header href=\"QuizCatalogServlet\">Take a Quiz!</a></li>";
 	private static final String CREATE_LINK = "<li class=header><a class=header href=\"BeginQuizCreationServlet\">Create Quiz</a></li>";
 	private static final String STATS_LINK = "<li class=header><a class=header href=\"SiteStatsServlet\">Site Stats</a></li>";
 	private static final String OUTBOX_LINK = "<li class=header><a class=header href=\"MailManagementServlet?index=outbox\">Outbox</a></li>";
 	private static final String INBOX_LINK = "<li class=header><a class=header href=\"MailManagementServlet?index=inbox\">Inbox</a></li>";
 	private static final String FUN_FACT = "<li class=header><a class=header href=\"http://en.wikipedia.org/wiki/Special:Random\">Fun Fact</a></li>";
 	private static final String LOGOUT = "<li class=header><form action=\"AcctManagementServlet\" method=\"post\"><input type=\"hidden\" name =\"Action\" value=\"Logout\"><input style=\"float:right\" type=\"submit\" value=\"Logout\"></form></li>";
 	public static final String QUIZ_ICON = "<img class=quiz src=\"http://upload.wikimedia.org/wikipedia/commons/1/13/Blue_square_Q.PNG\">";
 	private static final String DEFAULT_PROFILE_PIC = "http://www.iconsdb.com/icons/download/caribbean-blue/user-256.gif";
 	private static final String BLUE_STAR_ICON = "http://www.gettyicons.com/free-icons/136/stars/png/256/star_blue_256.png";
 	private static final String MAIL_ICON ="http://cdn1.iconfinder.com/data/icons/metro-uinvert-dock/256/Mail.png";
 	public static final String STATISTICS_ICON = "http://12starsmedia.com/wp-content/uploads/2012/02/VideoStatsIcon.gif";
 	private static final String ACTION_ICON ="http://myyearwithoutclothesshopping.com/wp-content/uploads/2013/01/take-action-click-icon.jpg";
 	private static final String ADMIN_ICON = "http://cdn1.iconfinder.com/data/icons/meBaze-Freebies/512/setting.png";
 	public static final String QUIZ_ICON2 = "http://upload.wikimedia.org/wikipedia/commons/1/13/Blue_square_Q.PNG";
 	
 	
 	
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
 		fullHeader.append(HOME_LINK);
 		fullHeader.append(LOGOUT);
 		fullHeader.append(FUN_FACT);
 		fullHeader.append(TAKE_QUIZ_LINK);
 		fullHeader.append(CREATE_LINK);
 		fullHeader.append(OUTBOX_LINK);
 		fullHeader.append(INBOX_LINK);
 		if (isAdmin) fullHeader.append(STATS_LINK);
 		fullHeader.append("</ul></div><div style=\"min-height:80px\"></div>");
 		
 		return fullHeader.toString();
 	}
 	
 	public static String printBasicHeader(){
 		StringBuilder fullHeader = new StringBuilder();
 		fullHeader.append("<div class=header><ul class=header>");
 		fullHeader.append(HOME_LINK);
 		fullHeader.append(FUN_FACT);
 		fullHeader.append(TAKE_QUIZ_LINK);
 		fullHeader.append("</ul></div><div style=\"min-height:80px\"></div>");
 		return fullHeader.toString();
 	}
 	
 	public static String printQuizListing(int id, String name, String cat, Account author){
 		StringBuilder listing = new StringBuilder();
 		listing.append("<div class=quiz>");
 		listing.append(QUIZ_ICON);
 		listing.append("<ul class=boxlisting>");
 		listing.append("<li class=quiz><b>"+name+"</b></li>");
 		listing.append("<li class=quiz>Category: " + cat );
 		listing.append("<b><a class=quiz href= \"QuizTitleServlet?id="+id+"\">Take Quiz</a></b></li>");
 		listing.append("<li class=quiz>Creator:<a href =\"ProfileServlet?user="+author.getName()+"\">"+author.getName()+"</a></li>");
 		listing.append("</div>");
 		
 		return listing.toString();
 	}
 	
 	public static String printTitle(String img, String name, ArrayList<String> actions){
 		if (img.isEmpty()) img = DEFAULT_PROFILE_PIC;
 		StringBuilder titleBox = new StringBuilder();
 		titleBox.append("<div class=quiz>");
 		titleBox.append("<img class=quiz src=\""+img+"\">");
 		titleBox.append("<h1 class=title>"+name+"</h1>");
 		titleBox.append("<ul style=\"list-style-type:none\">");
 		for (String action : actions){
 			titleBox.append("<li class=quiz style=\"display:inline\">");
 			titleBox.append(action);
 			titleBox.append("</li>");
 		}
 		
 		titleBox.append("</ul></div>");
 		return titleBox.toString();
 	}
 	
 	public static String printActionList(String img, String title, ArrayList<String> actions){
 		if (img.isEmpty()) img = getImage(title);
 		StringBuilder actionBox = new StringBuilder();
 		actionBox.append("<div class=quiz>");
 		actionBox.append("<h2 class=title>");
 		actionBox.append("<img class=quiz style=\"height:30px\" style src=\""+img+"\"><strong style=\"margin-left:15px\">");
 		actionBox.append(title+"</strong></h2>");
 		actionBox.append("<ul style=\"list-style-type:none\">");
 		for (String action : actions){
 			actionBox.append("<li class=quiz>");
 			actionBox.append("<b>");
 			actionBox.append(action);
 			actionBox.append("</b>");
 			actionBox.append("</li>");
 		}
 		actionBox.append("</ul></div>");
 		
 		return actionBox.toString();
 	}
 
 	
 	
 	public static String printUserListing(String name){
 		StringBuilder listing = new StringBuilder();
 		listing.append("<div class=quiz>");
 		listing.append("<h3>"+name);
 		listing.append("<a class=quiz href= \"ProfileServlet?user="+name+"\">Profile</a>");
 		listing.append("</h3></div>");
 		
 		return listing.toString();
 	}
 	
 	public static String printEnhancedUserListing(String name, ArrayList<String> actions){
 		StringBuilder listing = new StringBuilder();
 		listing.append("<div class=quiz>");
 		listing.append("<h3>"+name);
 		listing.append("<a class=quiz href= \"ProfileServlet?user="+name+"\">Profile</a>");
 		for (String action: actions){
 			listing.append(action);
 		}
 		listing.append("</h3></div>");
 		
 		return listing.toString();
 	}
 	
 	public static String printNewsFeed(ArrayList<String> adminNews, ArrayList<String> friendActivity){
 		StringBuilder newsBox = new StringBuilder();
 		newsBox.append("<div style=\"min-width:210px;top:60px; position:fixed;height:96%; left:1060px; min-height:700px; right:0;\">");
 		newsBox.append("<div class=newsfeed style=\"height:30px\"><div class=news><b>Newsfeed</b></div></div>");
		newsBox.append("<div class=newsfeed style=\"top:41px; height:40%\">");
 		for (String update : adminNews){
 			newsBox.append("<div class=news>");
 			newsBox.append(update);
 			newsBox.append("</div>");
 		}
 		newsBox.append("</div>");
 		newsBox.append("<div class=newsfeed style=\"bottom:0;height:51%\">");
 		for (String update : friendActivity){
 			newsBox.append("<div class=news>");
 			newsBox.append(update);
 			newsBox.append("</div>");
 		}
 		newsBox.append("</div></div>");
 		return newsBox.toString();
 	}
 	
 	
 	private static String getImage(String name){
 		if (name.equals("Achievements")) return BLUE_STAR_ICON;
 		else if(name.equals("Friends")) return DEFAULT_PROFILE_PIC;
 		else if(name.equals("Mail")) return MAIL_ICON;
 		else if (name.equals("Statistics")) return STATISTICS_ICON;
 		else if (name.equals("Actions")) return ACTION_ICON;
 		else if (name.equals("Administration")) return ADMIN_ICON;
 		else return BLUE_STAR_ICON;
 		
 	}
 }
