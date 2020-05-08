 package com.mythicacraft.IssueTrackerExecutors;
 
 
 import java.sql.SQLException;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import org.bukkit.ChatColor;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import org.bukkit.util.ChatPaginator;
 import org.bukkit.util.ChatPaginator.ChatPage;
 
 
 import com.mythicacraft.IssueTracker.cIssueTracker;
 
 public class IssueCommand implements CommandExecutor{
 
 	private cIssueTracker plugin;
 	public static String issueReason = "";
 	public static String senderName;
 	public static String closeIssueID;
 	public static String setStatus;
 	public static String closePlayer;
 	public String notifyPlayer;
 	public String pageStatus = "";
 	
 	public IssueCommand(cIssueTracker plugin){
 		this.plugin = plugin;
 	}
 	
 	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args){
 		senderName = sender.getName();
 		String status = "Open";
 		String reason = "";
 		SQLExecutors sqlExec = new SQLExecutors();
 		
 		if(sender.hasPermission("issuetracker.issue")){
 		
 		//Triggered when /issue [arg] is typed in any form
 		if(commandLabel.equalsIgnoreCase("issue")){
 			//When a player types /issue, /issue ?, /issue help
 			if(args.length == 0 || ((args[0].equalsIgnoreCase("?") || args[0].equalsIgnoreCase("help")) && args.length < 2)){
 				sender.sendMessage(ChatColor.GREEN + "-----" + ChatColor.YELLOW + "IssueTracker Help" + ChatColor.GREEN + "-----");
 				sender.sendMessage(ChatColor.YELLOW + "/issue create {Description}     " + ChatColor.BLUE + "Creates an issue");
 				sender.sendMessage(ChatColor.YELLOW + "/issue status     " + ChatColor.BLUE + "Displays issue status");
 				sender.sendMessage(ChatColor.YELLOW + "/issue close <issueID>     " + ChatColor.BLUE + "Removes or closes an issue");
 				sender.sendMessage(ChatColor.YELLOW + "/issue view closed     " + ChatColor.BLUE + "View your closed issues");
 				if(sender.hasPermission("issuetracker.admin")){
 					sender.sendMessage(ChatColor.GREEN + "-----" + ChatColor.YELLOW + "Admin Commands" + ChatColor.GREEN + "-----");
 					sender.sendMessage(ChatColor.YELLOW + "/issue status <issue_ID> (close/reviewed)     " + ChatColor.BLUE + "Sets status of an issue to close or reviewed");
 					sender.sendMessage(ChatColor.YELLOW + "/issue status all     " + ChatColor.BLUE + "Shows all open or reviewed issues");
 					sender.sendMessage(ChatColor.YELLOW + "/issue view closed <player>     " + ChatColor.BLUE + "Shows all closed issues submitted by <player>");
 				}
 			}
 			//When a player types '/issue status' do...
 			else if(args[0].equalsIgnoreCase("status")){
 				if (args.length == 1 && !sender.hasPermission("issuetracker.admin")){
 					try {				
 						//Calling the SELECT query for status
 						sqlExec.statusQuery();
 						//Pulls each row of the database. Displays each row
 						if(!SQLExecutors.selectSQL.next()){
 							sender.sendMessage(ChatColor.GREEN + "You have no issues to display!");
 						}
 						else {
 							SQLExecutors.selectSQL.beforeFirst();
 							while (SQLExecutors.selectSQL.next()) {
 								int tempstatus = SQLExecutors.selectSQL.getInt("status");
 								if (tempstatus == 2){
 									status = "Reviewed";
 								}
 								sender.sendMessage(ChatColor.BLUE + "Issue ID: " + ChatColor.GOLD + SQLExecutors.selectSQL.getString("issue_id") + ChatColor.BLUE + " - Status: " + ChatColor.GOLD + status + ChatColor.BLUE + " - " + ChatColor.GOLD + SQLExecutors.selectSQL.getString("reason"));
 								status = "Open";
 								}
 							//Close database connection
 							SQLExecutors.dbClose();
 						}
 					}	
 					catch (SQLException e) {
 						e.printStackTrace();
 						}
 					}
 				else if (sender.hasPermission("issuetracker.admin")){
 					//If only /issue status is typed treat as 1 page
 					if(args.length == 1){
 						try {				
 							//Calling the SELECT query for status
 							SQLExecutors.adminStatusQuery();
 							
 							//Pulls each row of the database. Displays each row
 							while (SQLExecutors.selectSQL.next()) {
 								
 								if(SQLExecutors.selectSQL.getString("reason").length() > 45){
 									reason = SQLExecutors.selectSQL.getString("reason").substring(0,45) + "...";
 								}
 								else{
 									reason = SQLExecutors.selectSQL.getString("reason");
 								}
 								int tempstatus = SQLExecutors.selectSQL.getInt("status");
 								if (tempstatus == 2){
 									status = "Reviewed";
 								}
								 pageStatus = pageStatus + ChatColor.BLUE + "> Issue #" + SQLExecutors.selectSQL.getString("issue_ID") + ": " + ChatColor.GOLD + reason + ChatColor.BLUE + "\n     " + ChatColor.DARK_GRAY + "Player: " + ChatColor.GRAY + SQLExecutors.selectSQL.getString("player") + ChatColor.DARK_GRAY + " - Status: " + ChatColor.GRAY + status + "\n ";
 						         status = "Open";
 							}
 							//Close database connection
 							SQLExecutors.dbClose();
 							pageSender(sender, 1);//use method to send pages
 							return true;
 							} 	
 						catch (SQLException e) {
 							e.printStackTrace();
 							}
 						}
 					//If /issue status # is typed
 					if(args.length == 2){
 						try {				
 							//Calling the SELECT query for status
 							SQLExecutors.adminStatusQuery();
 							
 							//Pulls each row of the database. Displays each row
 							while (SQLExecutors.selectSQL.next()) {
 								
 								if(SQLExecutors.selectSQL.getString("reason").length() > 45){
 									reason = SQLExecutors.selectSQL.getString("reason").substring(0,45) + "...";
 								}
 								else{
 									reason = SQLExecutors.selectSQL.getString("reason");
 								}
 								int tempstatus = SQLExecutors.selectSQL.getInt("status");
 								if (tempstatus == 2){
 									status = "Reviewed";
 								}
								 pageStatus = pageStatus + ChatColor.BLUE + "> Issue #" + SQLExecutors.selectSQL.getString("issue_ID") + ": " + ChatColor.GOLD + reason + ChatColor.BLUE + "\n     " + ChatColor.DARK_GRAY + "Player: " + ChatColor.GRAY + SQLExecutors.selectSQL.getString("player") + ChatColor.DARK_GRAY + " - Status: " + ChatColor.GRAY + status + "\n ";
 						         status = "Open";
 							}
 							//Close database connection
 							SQLExecutors.dbClose();
 							
 							if(letterCheck(args[1]) == true) { //use a method that checks argument for invalid characters (anything but numbers)
 								sender.sendMessage(ChatColor.RED + "That is not a valid page number!");
 								return true;
 							}
 
 							int userPage = Integer.parseInt(args[1]); //now that we know the first arg is a number, make it into an Int
 
 							if(userPage <= pageTotal()) { //as long as the given user page number is less than or equal to the total pages paginated from first string.
 								pageSender(sender, userPage); //use method to send pages
 								return true;
 							}
 							if(userPage > pageTotal()) { // if given user page number is more than the total page number.
 								sender.sendMessage(ChatColor.RED + "That is not a valid page number!");
 								return true;
 							}
 							return false;	
							} 			
 						catch (SQLException e) {
 							e.printStackTrace();
 							}
 						}
 					else if(args[0].equalsIgnoreCase("status") && args[2].equalsIgnoreCase("close") || args[2].equalsIgnoreCase("closed") || args[2].equalsIgnoreCase("reviewed") && args.length == 3){
 						closeIssueID = args[1];
 						//if '/issue status # close' is typed
 							if(args[2].equalsIgnoreCase("close") | args[2].equalsIgnoreCase("closed")){
 								setStatus = "3";
 								try {
 									sqlExec.adminSetQuery();
 									notifyPlayer = SQLExecutors.selectSQL.getString("player");
 									sender.sendMessage(ChatColor.GREEN + "Issue is now set as 'closed'.");
 									playerNotification();
 								} 
 								catch (SQLException e) {
 									sender.sendMessage(ChatColor.GOLD + sqlExec.errorString);
 									}
 								}
 							//if '/issue status # reviewed' is typed
 							else if (args[2].equalsIgnoreCase("reviewed")){
 								setStatus = "2";
 								try {
 									sqlExec.adminSetQuery();
 									notifyPlayer = SQLExecutors.selectSQL.getString("player");
 									sender.sendMessage(ChatColor.GREEN + "Issue is now set as 'reviewed'.");
 									playerNotification();
 								} 
 								catch (SQLException e) {
 									sender.sendMessage(ChatColor.GOLD + sqlExec.errorString);
 									}
 								}
 							else {
 								//if the format was wrong
 								sender.sendMessage(ChatColor.RED + "Please enter an appropriate issue status. (Close or Reviewed");
 								}
 					} //Close if status # close/reviewed is typed
 						else{
 							//If player does not have permission issuetracker.admin
 							sender.sendMessage(ChatColor.RED + "You do not have permissions to use this command!");
 						}
 					} 
 				}
 
 			//Triggers when /issue create args[x] is typed
 			else if(args[0].equalsIgnoreCase("create")){
 				 if(args.length >= 2){
 					for(int i = 1; i < args.length; i++){
 						issueReason += " " + args[i];
 					}
 					issueReason = issueReason.substring(1);
 					try {
 						sqlExec.createQuery();
 						} 
 					catch (SQLException e) {
 						e.printStackTrace();
 						}
 					sender.sendMessage(ChatColor.GREEN + "Your issue has successfully been submitted. A moderator will review it as soon as possible. You may type '/issue status' to view the status of your issues.");
 					issueReason = "";
 					for(Player mod: plugin.getServer().getOnlinePlayers()) {    
 		                if(mod.hasPermission("issuetracker.admin")) {      
 		                 mod.sendMessage(ChatColor.GOLD + "[IssueTracker] " + ChatColor.GREEN + "A player submitted an issue. Type '/issue status' to view it.");
 		                	}
 		            	}	
 				 	}
 				 
 				 else {
 					 sender.sendMessage(ChatColor.RED + "Please type '/issue create <message>' to submit an issue.");
 				 }
 				}
 			//Triggers when /issue close is typed
 			else if(args[0].equalsIgnoreCase("close")){
 				if(args.length == 2){
 					try {
 					closeIssueID = args[1];
 					sqlExec.closeQuery();
 					sender.sendMessage(ChatColor.GREEN + "You have successfully closed your issue!");
 					}
 					catch (Exception e) {
 						sender.sendMessage(ChatColor.RED + "You must enter a valid issue ID. Type '/issue status' to view your issues.");
 					}
 				}
 				else {
 					sender.sendMessage(ChatColor.RED + "Please type '/issue close <issue_ID>' to close an issue.");
 				}
 			}
 			//When a player types '/issue view closed' do...
 			else if(args[0].equalsIgnoreCase("view")){
 				if(args.length == 2 && !sender.hasPermission("issuetracker.admin")){
 					if(args[1].equalsIgnoreCase("closed")){
 						try {				
 							//Calling the SELECT query for status
 							sqlExec.viewCloseQuery();
 							//Pulls each row of the database. Displays each row
 							sender.sendMessage(ChatColor.BLUE + "*******" + ChatColor.GREEN + "Your closed tickets" + ChatColor.BLUE + "*******");
 							while (SQLExecutors.selectSQL.next()) {
 								sender.sendMessage(ChatColor.BLUE + "Issue ID: " + ChatColor.GOLD + SQLExecutors.selectSQL.getString("issue_id") + ChatColor.BLUE + " - " + ChatColor.GOLD + SQLExecutors.selectSQL.getString("reason"));
 								}
 							//Close database connection
 								SQLExecutors.dbClose();
 							} 	
 						catch (SQLException e) {
 							e.printStackTrace();
 							}
 						}
 					else {
 						sender.sendMessage(ChatColor.RED + "Please type '/issue view closed' to view your closed issues");
 					}
 				}
 				else if(sender.hasPermission("issuetracker.admin")){
 					if(args.length == 3){
 						if (args[1].equalsIgnoreCase("close") || args[1].equalsIgnoreCase("closed")){
 							try {				
 								closePlayer = args[2];
 								//Calling the SELECT query for status
 								sqlExec.adminCloseQuery();
 								//Pulls each row of the database. Displays each row
 								sender.sendMessage(ChatColor.BLUE + "*******" + ChatColor.GREEN + "Closed tickets for: " + closePlayer + ChatColor.BLUE + "*******");
 								while (SQLExecutors.selectSQL.next()) {
 									 sender.sendMessage(ChatColor.BLUE + "> Issue #" + SQLExecutors.selectSQL.getString("issue_ID") + ": " + ChatColor.GOLD + SQLExecutors.selectSQL.getString("reason"));
 									}
 								SQLExecutors.dbClose();
 								} 	
 							catch (SQLException e) {
 								sender.sendMessage("No closed issues found for player " + closePlayer);
 								}
 						} //if args 1 = close/closed
 						else {
 							sender.sendMessage(ChatColor.RED + "Please enter a valid issue ID. Type '/issue view closed <player>");
 						}
 					}
 					else {
 						sender.sendMessage(ChatColor.RED + "You must enter a valid players name: /issue view closed <player>");
 					}
 					
 				}
 				else {
 					sender.sendMessage(ChatColor.RED + "You do not have permissions to use this!");
 				}
 			}
 			//If none of the triggers are hit - tell them how to view correct syntax
 			else {
 				sender.sendMessage("Please type /issue for help.");
 				}
 			}
 		}
 		return true;
 		}
 	public void playerNotification(){
 		Player player = plugin.getServer().getPlayer(notifyPlayer);
 		if(player != null)
 		    player.sendMessage(ChatColor.GOLD + "[IssueTracker] " + ChatColor.GREEN + "Your issue has been udpated!");
 		}
 	public void pageSender(CommandSender sender, int pageNumber) { //method to send pages, accepts the sender object and the user given page number
         ChatPage message = ChatPaginator.paginate(pageStatus, pageNumber, 53, 8); //paginate string, pulling the page number the player provided. It creates the page with the lines 53 characters long and 8 lines per page
 	    String[] pages = message.getLines(); //puts the lines from the page into a string array
	    sender.sendMessage(ChatColor.BLUE + "*******" + ChatColor.GREEN + "All Open/Reviewed Statuses " + ChatColor.GOLD + "Page " + pageNumber + "/" + pageTotal() + ChatColor.BLUE + "*******" ); //header of page with current and total pages
 	    sender.sendMessage(pages); //send page string array
 	    if(pageNumber < pageTotal()) { //if page number is less than total, include this footer
 		    int nextPage = pageNumber + 1;
		    sender.sendMessage(ChatColor.BLUE + "*******" + ChatColor.GREEN + "Type \"/issue status " + nextPage + "\" for next page." + ChatColor.BLUE + "*******");
 	    }
	    pageStatus = "";
 	}
 	public int pageTotal() { //returns an Int of total pages
 	    ChatPage message = ChatPaginator.paginate(pageStatus, 1, 53, 8);
 	    int totalPages = message.getTotalPages();
 	    return totalPages;
 	}
 	public boolean letterCheck(String args) { //uses a regex to check for anything that ISN'T a number
         Pattern checkRegex = Pattern.compile("[\\D]");
         Matcher regexMatcher = checkRegex.matcher(args);
         if(regexMatcher.find()) {
     	return true;
         }
         return false;
 	}
 	
 }
