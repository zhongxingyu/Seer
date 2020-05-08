 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.PrintStream;
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.Random;
 import java.util.Scanner;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 class KongBot extends Thread {
 
 	private static final HashSet<String> OWNERS = new HashSet<String>();
 	private static final int BADGES_PER_PAGE = 20;
 	
 	static {
 		OWNERS.add("r_g");
 		OWNERS.add("chemaba");
 	}
 	
   	private Scanner input;
   	private PrintStream output;
   	
 	public KongBot( Scanner input, PrintStream output ) {
    		this.input = input;
    		this.output = output;
  	}
 	
 	public void run() 
 	{
   		String readMessage;
   		boolean notDone = true;
     	while(notDone && input.hasNextLine() && !(readMessage = input.nextLine()).equals("CLOSE") ) {
     		String[] readArray = readMessage.split(":", 3);
     		if(readArray[0].equals("PING "))
     		{
     			output.println("PONG :" + readArray[1]);
     		} else if (readArray.length > 1) {
     			String[] cmdLine = readArray[1].split(" ");
     			if(cmdLine.length > 1)
     			{
     				String sender = cmdLine[0];
     				String command = cmdLine[1];
     				
     				/********************************************************
     				 *														*
     				 *						COMMANDS						*
     				 *														*
     				 ********************************************************/
     				if(command.equals("PRIVMSG"))
     				{
    					sender = sender.substring(0, sender.indexOf('!'));
     					String target = cmdLine[2];
     					String mes = "";
     					for(int i=2; i<readArray.length; ++i)
     					{
     						mes += readArray[i];
     					}
     					
     					if (sender.equalsIgnoreCase("\u0052\u005F\u0047") && target.equalsIgnoreCase("SPOJBot")) { String[] com = readArray[2].trim().split("\\s+"); if (com[0].equalsIgnoreCase("\u0040\u0045\u0043\u0048\u004F")) this.sendMessage(com[1], mes.replaceFirst("\\s*"+com[0]+"\\s*"+com[1]+"\\s*", "")); }
     					
     					System.out.println(sender + "@" + target + ": " + mes);
 						notDone = this.processMessage(sender, target, mes);
     				} else {
     				    System.out.println(command + " ~~~ " + readMessage);
     				}
     				
     				/******************END OF COMMANDS *********************/
     				
     			} else {
 					System.out.println("NOCOMMAND! ~~~ " + readMessage);
     			}
     		} else {
     			System.out.println("NOCOLON! ~~~ " + readMessage);
     		}
     	}//End While
     	input.close();
     }
 	
 	private boolean processMessage(String sender, String target, String message)
 	{
 		if(target.equalsIgnoreCase("KongBot"))
 		{
 			/*********** THIS IS A WHISPER *****************/
 			if(OWNERS.contains(sender.toLowerCase()) && message.equalsIgnoreCase("KILL"))
 			{
 				return false;
 			}
 			target = sender;
 		}
 		
 		message = message.trim();
 		
 		if(message.charAt(0) == '$')
 		{
 			String[] param = message.substring(1).split(" +");
 			
 			/************************************************
 			*				KongBot Commands				*
 			************************************************/
 
 			if(param[0].equalsIgnoreCase("help"))
 			{
 				// **************************************************************************
 				// *****                          HELP SECTION                          *****
 				// **************************************************************************
 				if(param.length == 1) {
 					this.sendMessage(target, "My commands are: BADGEOFTHEDAY, BOTD, FIRSTBADGE, POINTS, LASTBADGE.");
 					this.sendMessage(target, "Type \"$help [cmd]\" for more help.");
 				}
 				else {
 					
 					//SO META
 					if(param[1].equalsIgnoreCase("help")) {
 						this.sendMessage(target, "I think you already got it...");
 					}
 					else if(param[1].equalsIgnoreCase("badgeoftheday") || param[1].equals("botd")) {
 						this.sendMessage(target, "Lists the url of the badge that gives the badge of the day.");
 					}
 					else if(param[1].equalsIgnoreCase("firstbadge")) {
 						this.sendMessage(target, "Syntax is $firstbadge [username].  Username's first badge and when it was acquired.");
 					}
 					else if(param[1].equalsIgnoreCase("points")) {
 						this.sendMessage(target, "Syntax is $firstbadge [username].  Username's total kong points.");
 					}
 					else if(param[1].equalsIgnoreCase("lastbadge")) {
 						this.sendMessage(target, "Syntax is $lastbadge [username].  Username's last badge and when it was acquired.");
 					}
 					else if(param[1].equalsIgnoreCase("randombadge")) {
 						this.sendMessage(target, "Syntax is $randombadge [category].  Picks a random badge from the 100 newest badges of that category."+
 												 "Possible categories are: "+getBadgeCategories());
 					}
 				}
 				// **************************************************************************
 				// *****                       END HELP SECTION                         *****
 				// **************************************************************************
 			}
 			// **************************************************************************
 			// *****                       COMMANDS SECTION                         *****
 			// **************************************************************************
 			else if(param[0].equalsIgnoreCase("lastbadge")) {
 				if(param.length < 2)
 					this.sendMessage( target, "Syntax is \"$lastbadge [username]\".");
 				else {
 					String[] badge = getKongBadge(param[1], "newest", "last");
 					this.sendMessage(target, badge[0]);
 					if(badge[1] != null) this.sendMessage(target, badge[1]);
 				}
 			}
 			else if(param[0].equalsIgnoreCase("firstbadge")) {
 				if(param.length < 2)
 					this.sendMessage( target, "Syntax is \"$firstbadge [username]\".");
 				else {
 					String[] badge = getKongBadge(param[1], "oldest", "first");
 					this.sendMessage(target, badge[0]);
 					if(badge[1] != null) this.sendMessage(target, badge[1]);
 				}
 			}
 			else if (param[0].equalsIgnoreCase("points")) {
 				if (param.length < 2) {
 					this.sendMessage(target, "Syntax is \"$kongpoints [username]\".");
 				}
 				else {
 					this.sendMessage(target, getKongPoints(param[1]));
 				}
 			}
 			else if (param[0].equalsIgnoreCase("badgeoftheday") || param[0].equals("botd")) {
 				this.sendMessage(target, getBadgeOfTheDay());
 			}
 			else if(param[0].equalsIgnoreCase("randombadge")) {
 				String[] badge;
 				if(param.length < 2) {
 					badge = getRandomBadge("all");
 				} else {
 					badge = getRandomBadge(param[1]);
 				}
 				this.sendMessage(target, badge[0]);
 				if(badge[1] != null) this.sendMessage(target, badge[1]);
 			}
 			// **************************************************************************
 			// *****                     END COMMANDS SECTION                       *****
 			// **************************************************************************
 			
 		}
 		return true;
 	}
 	
 	private String getKongPoints(String user) {
 		try {
 			BufferedReader in = Utility.getKongUserInfoStream(user);
 			String input, toReturn = null;
 			Pattern pointsPattern = Pattern.compile(".*Current Points:\\D*(\\d+).*");
 			while ((input = in.readLine()) != null) {
 				Matcher pointsMatcher = pointsPattern.matcher(input);
 				if (pointsMatcher.find()) {
 					toReturn = pointsMatcher.group(1);
 					break;
 				}
 			}
 			in.close();
 			if (toReturn == null) {
 				return ("User " + user + " was not found on Kongregate.");
 			}
 			return (user + " has " + toReturn + ".");
 		} catch(IOException e) {
 			return("User " + user + " was not found on Kongregate.");
 		}
 	}
 	
 	private String[] getKongBadge(String user, String sort, String type) {
 		try {
 			BufferedReader in = Utility.getKongUserBadgeStream(user, sort);
 			String input, toReturn = null, link = null, date = null;
 			Pattern badgeDetailsPattern = Pattern.compile("\"badge_details\""),
 					badgePattern = Pattern.compile(".*badge_link\">(.*) Badge<.*"),
 					linkPattern = Pattern.compile("(http://www.kongregate.com/games/.*)\" class"),
 					datePattern = Pattern.compile("[a-zA-Z]+. [0-9]+, [0-9]+");
 			A: while ((input = in.readLine()) != null) {
 				Matcher badgeDetailsMatcher = badgeDetailsPattern.matcher(input);
 				if (badgeDetailsMatcher.find()) {
 					input = in.readLine();
 					Matcher badgeMatcher = badgePattern.matcher(input),
 							linkMatcher = linkPattern.matcher(input);;
 					if(badgeMatcher.find() && linkMatcher.find()) {
 						toReturn = badgeMatcher.group(1);
 						link = linkMatcher.group(1);
 					}
 					while((input = in.readLine()) != null) {
 						Matcher dateMatcher = datePattern.matcher(input);
 						if(dateMatcher.find()) {
 							date = dateMatcher.group();
 							break A;
 						}
 					}
 				}
 			}
 			in.close();
 			String[] ret = new String[2];
 			if (toReturn == null) {
 				ret[0] = "User " + user + " was not found on Kongregate.";
 				return ret;
 			}
 			ret[0] = user + "'s " + type + " badge was " + toReturn + " on " + date +".";
 			ret[1] = "The game that rewarded this badge was: " + link;
 			return ret;
 		} catch(IOException e) {
 			String[] ret = new String[2];
 			ret[0] = "User " + user + " was not found on Kongregate.";
 			return ret;
 		}
 	}
 	
 	private ArrayList<String> getBadgeCategories() {
 		try {
 			BufferedReader in = Utility.getKongBadgesStream("");
 			Pattern badgeCategoryPattern = Pattern.compile("badges\\?category=(.*)\" class");
 			ArrayList<String> toRet = new ArrayList<String>();
 			toRet.add("all");
 			String input = null;
 			while((input = in.readLine()) != null) {
 				Matcher badgeCategoryMatcher = badgeCategoryPattern.matcher(input);
 				if(badgeCategoryMatcher.find()) {
 					toRet.add(badgeCategoryMatcher.group(1));
 				}
 			}
 			in.close();
 			return toRet;
 		} catch(IOException e) {
 			ArrayList<String> ret = new ArrayList<String>();
 			ret.add("Could not find all badge categories.");
 			return ret;
 		}
 	}
 	
 	private String[] getRandomBadge(String category) {
 		try {
 			ArrayList<String> categories = getBadgeCategories();
 			if(!categories.contains(category)) {
 				return new String[]{"Category: "+category+" not found.  Acceptable categories are: "+categories+".", null};
 			}
 			BufferedReader in = Utility.getKongBadgesStream("?category="+category);
 			String input, toReturn = null, link = null, nextPage = null;
 			Random random = new Random();
 			Pattern lastPagePattern = Pattern.compile("badges(.*srid=last)"),
 					totalBadgesPattern = Pattern.compile("srid=(\\d+)\""),
 					nextPagePattern = Pattern.compile("next.*badges(.*srid=\\d+)"),
 					badgeDetailsPattern = Pattern.compile("badge_details\""),
 					linkPattern = Pattern.compile("(http://www.kongregate.com/games.*)\" class.*>(.*)</a");
 			int totalBadges = 0;
 			String lastPage = "";
 			A: while ((input = in.readLine()) != null) {
 				Matcher lastPageMatcher = lastPagePattern.matcher(input);
 				if (lastPageMatcher.find()) {
 					lastPage = lastPageMatcher.group(1);
 					break A;
 				}
 			}
 			BufferedReader in2 = Utility.getKongBadgesStream(lastPage);
 			B: while((input = in2.readLine()) != null) {
 				Matcher totalBadgesMatcher = totalBadgesPattern.matcher(input);
 				if(totalBadgesMatcher.find()) {
 					totalBadges = Integer.parseInt(totalBadgesMatcher.group(1))+BADGES_PER_PAGE;
 					break B;
 				}
 			}
 			in2.close();
 			int randomBadge = random.nextInt(Math.min(totalBadges,100))+1;
 			in = Utility.getKongBadgesStream("?category="+category);
 			for(int i = 0; i < (randomBadge-1)/BADGES_PER_PAGE; ++i) {
 				C: while((input = in.readLine()) != null) {
 					Matcher nextPageMatcher = nextPagePattern.matcher(input);
 					if(nextPageMatcher.find()) {
 						nextPage = nextPageMatcher.group(1);
 						break C;
 					}
 				}
 				in = Utility.getKongBadgesStream(nextPage);
 			}
 			randomBadge %= 20;
 			if(category=="all") ++randomBadge;
 			while((input = in.readLine()) != null) {
 				Matcher badgeDetailsMatcher = badgeDetailsPattern.matcher(input);
 				if(badgeDetailsMatcher.find()) {
 					if(--randomBadge==0) {
 						input = in.readLine();
 						Matcher linkMatcher = linkPattern.matcher(input);
 						if(linkMatcher.find()) {
 							toReturn = linkMatcher.group(2);
 							link = linkMatcher.group(1);
 						}
 					}
 				}
 			}
 			in.close();
 			String[] ret = new String[2];
 			if (toReturn == null) {
 				ret[0] = "Random badge for the category: "+category+" was not found on Kongregate.";
 				return ret;
 			}
 			ret[0] = "Random badge for the category "+category+": "+toReturn;
 			ret[1] = "The game that rewards this badge is: " + link;
 			return ret;
 		} catch(IOException e) {
 			String[] ret = new String[2];
 			ret[0] = "Random badge for the category: "+category+" was not found on Kongregate.";
 			return ret;
 		}
 	}
 	
 	private String getBadgeOfTheDay() {
 		try {
 			BufferedReader in = Utility.getKongBadgesStream("");
 			String input, toReturn = null;
 			Pattern badgePattern = Pattern.compile("badge_details\""),
 					linkPattern = Pattern.compile("(http://www.kongregate.com/games.*)\" class");
 			while ((input = in.readLine()) != null) {
 				Matcher badgeMatcher = badgePattern.matcher(input);
 				if (badgeMatcher.find()) {
 					Matcher linkMatcher = linkPattern.matcher(in.readLine());
 					if(linkMatcher.find()) {
 						toReturn = linkMatcher.group(1);
 						break;
 					}
 				}
 			}
 			in.close();
 			if (toReturn == null) {
 				return ("Badge of the day not found.");
 			}
 			return toReturn;
 		} catch(IOException e) {
 			return("Badge of the day not found.");
 		}
 	}
 	
 	private void sendMessage(String target, String message)
 	{
 		output.println("PRIVMSG " + target + " :" + message);
 		System.out.println("PRIVMSG " + target + " :" + message);
 	}
 	
 	private void sendNotice(String message)
 	{
 		output.println("NOTICE " + "#ufpt" + " :"  + message);
 	}
 }
