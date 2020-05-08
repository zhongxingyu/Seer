 package com.smartaleq.bukkit.dwarfcraft.ui;
 
 import java.util.List;
 
 import org.bukkit.Server;
 import org.bukkit.entity.Player;
 import org.bukkit.inventory.ItemStack;
 
 import com.smartaleq.bukkit.dwarfcraft.Dwarf;
 import com.smartaleq.bukkit.dwarfcraft.DwarfCraft;
 import com.smartaleq.bukkit.dwarfcraft.Effect;
 import com.smartaleq.bukkit.dwarfcraft.School;
 import com.smartaleq.bukkit.dwarfcraft.Skill;
 import com.smartaleq.bukkit.dwarfcraft.TrainingZone;
 
 public class Out {
 	
 /*
  * Messaging Statics	
  */
 	static final int lineLength = 68;
	static final int maxLines = 12;
 	
 /*
  *     Color Schema: 
  * 4 Dark Red 		- command name
  * c Red			- "Bad"
  * e Yellow		- "normal"
  * 6 Gold/orange	- DC general coloring
  * 2 Dark Green	- material/item
  * a Green			- "Good"
  * b Teal			- skill name/ID
  * 3 Dark Teal		- levels
  * 1 Dark Blue		- school name/id
  * 9 Blue			- dwarves
  * d Pink			- Help/info
  * 5 Purple		- effect ID
  * f White			-
  * 7 Gray			-
  * 8 Dark Gray		- zone 
  * 0 Black			-
  */
 	
 	
 	public static boolean generalHelp(Player player) {
 		if (DwarfCraft.debugMessagesThreshold < 1) System.out.println("Debug Message: started help6");
 		Out.sendMessage(player, "&d" + Messages.GENERALHELPMESSAGE.message, "&6[&d?&6] ");
 		return true;
 	}
 		
 	public static boolean commandHelp(Player player, CommandInfo c) {
 		if (DwarfCraft.debugMessagesThreshold < 1) System.out.println("Debug Message: started help5");
 		Out.sendMessage(player, "&d" + c.helpText, "&6[&d?&6] " );
 		return true;
 	}
 	
 	public static boolean info(Player player) {
 		Out.sendMessage(player, Messages.INFO.message, "&6[Info&6] ");
 		return true;
 	}
 
 	public static boolean commandList(Player player, int number) {
 		if (number == 1) {
 			Out.sendMessage(player, Messages.COMMANDLIST1.message,"&6[&d?&6] " );
 			return true;
 		}
 		if (number == 2){
 			Out.sendMessage(player, Messages.COMMANDLIST2.message,"&6[&d?&6] " );
 			return true;
 		}
 		return false;
 	}
 
 	public static boolean tutorial(Player player, int i) {
 		if (i == 1) Out.sendMessage(player, Messages.TUTORIAL1.message,"&6[&dDC&6] ");
 		else if (i == 2) Out.sendMessage(player, Messages.TUTORIAL2.message,"&6[&dDC&6] ");
 		else if (i == 3) Out.sendMessage(player, Messages.TUTORIAL3.message,"&6[&dDC&6] ");
 		else if (i == 4) Out.sendMessage(player, Messages.TUTORIAL4.message,"&6[&dDC&6] ");
 		else if (i == 5) Out.sendMessage(player, Messages.TUTORIAL5.message,"&6[&dDC&6] ");
 		else return false;
 		return true;
 	}
 	
 	public static boolean printSkillInfo(Player player, Skill skill){
 		//general line
 		Out.sendMessage(player, "&6  Skillinfo for &b"+skill.displayName+"&6 [&b"+skill.id+"&6] || Your level &3"+skill.level);
 		//effects lines
 		Out.sendMessage(player, "&6[&5EffectID&6]&f------&6[Effect]&f------" );
 		for(Effect effect:skill.effects){
 			if (effect != null)
 				Out.sendMessage(player, effect.describeLevel(skill.level), "&6[&5"+effect.id+"&6] ");
 		}
 		//training lines	
 		Out.sendMessage(player, "&6---Train costs for level &3"+skill.level+1+"&6 at a &1" + skill.school.toString() + " school&6---");
 		ItemStack[] costs = (Dwarf.find(player)).calculateTrainingCost(skill);
 		for(ItemStack item:costs){
 			if (item != null) Out.sendMessage(player, " &2" + item.toString() + "&6  --" , " &6-- ");
 		}
 		return true;
 	}
 	
 	public static boolean printSkillSheet(Dwarf dwarf, Player viewer) {
 		try {
 			String message1;
 			String message2 = "";
 			String prefix1 = "&6[&dSS&6] ";
 			String prefix2 = "&6[&dDC&6] ";	
 			message1 = ("&6Printing Skill Sheet for &9" + dwarf.player.getDisplayName() + prefix1 + "Dwarf &6Level is &3" + dwarf.getDwarfLevel());
 			sendMessage(viewer, message1, prefix1);
 			
 			if(dwarf.isElf){
 				message2 = ("&fElves &6don't have skills, numbskull");
 				sendMessage(viewer, message2, prefix2);
 				return true;
 			}
 			boolean odd = true;
 			for (Skill s:dwarf.skills){	
 				if(s == null) continue;
 				odd = !odd;
				String interim = String.format("&b%s  &6[&3%d&6]  ", s.displayName, s.level);
 				message2 = message2.concat(interim);
 				if (odd)
 					message2 = message2.concat("\n");
 			}
 			sendMessage(viewer, message2, prefix2);
 			return true;
 		} catch (Exception e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 			return false;
 		}
 	}	
 		
 	public static boolean effectInfo(Player player, Effect effect) {
 		Out.sendMessage(player, effect.describeLevel((Dwarf.find(player)).getSkill(effect).level), "&6[&5" +effect.id+"&6] ");
 		Out.sendMessage(player, effect.describeGeneral(), "&6[&5" +effect.id+"&6] ");
 		return false;
 	}
 		
 	public static void becameADwarf(Player player) {
 		sendMessage(player, Messages.DWARFSUCCESS.message, "&6[DC] ");
 	}
 	public static void confirmBecomingDwarf(Player player) {
 		sendMessage(player, Messages.DWARFCONFIRM.message,"&6[DC] ");
 	}
 	public static void becameAnElf(Player player) {
 		sendMessage(player, Messages.ELFSUCCESS.message, "&6[DC] ");
 	}	
 	public static void alreadyAnElf(Player player) {
 		sendMessage(player, Messages.ELFALREADY.message, "&6[DC] ");
 	}
 	public static void confirmBecomingAnElf(Player player) {
 		sendMessage(player, Messages.ELFCONFIRM.message,"&6[DC] ");
 	}
 
 	
 	public static boolean error(Player player, Messages error) {
 		if (error == Messages.ERRORBADINPUT){
 			Out.sendMessage(player, "&d" + Messages.ERRORBADINPUT.message, "&6[&d?&6] " );
 			return true;}
 		
 		return false;
 	}
 	
 
 	
 
 	
 	
 	
 	/**
 	 * Used to send messages to one player
 	 */
 	static void sendMessage(Player player, String message){
 		sendMessage(player, message, "");
 	}
 	
 	/**
 	 * Dwarf version
 	 */
 	static void sendMessage(Dwarf dwarf, String message){
 		sendMessage(dwarf.player, message);
 	}
 
 	/**
 	 * Used to send messages to one player with a prefix
 	 * @return 
 	 */
 	static void sendMessage(Player player, String message, String prefix){
 		message = parseColors(message);
 		prefix = parseColors(prefix);
 		messagePrinter(player, message, prefix);
 	}
 	
 	/**
 	 * Dwarf version
 	 */
 	static void sendMessage(Dwarf dwarf, String message, String prefix){
 		sendMessage(dwarf.player, message, prefix);
 	}
 	
 	/**
 	 * Used to send messages to many players
 	 */
 	static void sendMessage(Player[] playerArray, String message){
 		sendMessage(playerArray, message, "");
 	}
 	
 	/**
 	 * Used to send messages to many players with a prefix
 	 */
 	static void sendMessage(Player[] playerArray, String message, String prefix ){
 		for (Player p: playerArray) sendMessage(p, message, prefix);
 	}
 	
 	/**
 	 * Used to send messages to all players on a server
 	 */
 	static void sendBroadcast(Server server, String message){
 		sendBroadcast(server, message, "");
 	}
 	
 	/**
 	 * Used to send messages to all players on a server with a prefix TODO
 	 */
 	static void sendBroadcast(Server server, String message, String prefix){
 		Player[] playerArray = server.getOnlinePlayers();
 		sendMessage(playerArray, message, prefix);
 	}
 	
 	/**
 	 * Used to find the printed length of a string, removing two characters of length for each  found
 	 * @param string
 	 * @return
 	 */
 	static int lengthWithoutColors(String string){
 		String[] split = string.split("");
 		int length = 0;
 		if (split.length == 1) return string.length();
 		for(String section: split){
 			length += section.length() - 1;
 		}
 		return length;
 	}
 	
 	/**
 	 * Used to parse and send multiple line messages
 	 * Sends actual output commands
 	 */
 	static void messagePrinter(Player player, String message, String prefix){
 		// if no message throw exception		
 		if (DwarfCraft.debugMessagesThreshold < 1) System.out.println("Debug Message: started printer");
 		int messageSectionLength = lineLength - prefix.length();
 		String currentLine = "";
 		String words[] = message.split(" ");
 		String lastColor = "";
 		int lineTotal = 0;
 		for (String word: words){
 			if (lengthWithoutColors(word)>messageSectionLength) word = word.substring(0, messageSectionLength);
 			if (lengthWithoutColors(currentLine)+lengthWithoutColors(word) <= messageSectionLength){ 
 				currentLine = currentLine.concat(word+" ");
 			}
 			else {
 				player.sendMessage(prefix.concat(lastColor + currentLine));
 				lineTotal++;
 				if (lineTotal >= maxLines) return;
 				lastColor = lastColor(lastColor + currentLine);
 				currentLine = word+ " ";
 			}
 		}
 		lastColor = lastColor(lastColor + currentLine);
 		player.sendMessage(prefix.concat(lastColor + currentLine));
 	}
 	
 	private static String lastColor(String currentLine) {
 		String lastColor = "";
 		int lastIndex = currentLine.lastIndexOf("");
 		if(lastIndex == currentLine.length()) return "";
 		if(lastIndex != -1){
 			lastColor = currentLine.substring(lastIndex,lastIndex+2);
 		};
 		return lastColor;
 	}
 
 	/**
 	 * dwarf version
 	 */
 	static void messagePrinter(Dwarf dwarf, String message, String prefix){
 		messagePrinter(dwarf.player,message,prefix);
 	}
 	
 	/** 
 	 * Finds &0-F in a string and replaces it with the color symbol
 	 */
 	static String parseColors(String message){	
 		if (message == null){
 			if (DwarfCraft.debugMessagesThreshold < 2) System.out.println("Debug Message: printing null message!");
 			return null;
 		}
 		if (DwarfCraft.debugMessagesThreshold < 1) System.out.println("Debug Message: parsing colors for: "+ message);
 		for(int i =0; i<message.length();i++){
 			try{
 				if(message.charAt(i)=='&'){
 					if (message.charAt(i+1)=='0') message = message.replace("&0", "0");
 					else if (message.charAt(i+1)=='1') message = message.replace("&1", "1");
 					else if (message.charAt(i+1)=='2') message = message.replace("&2", "2");
 					else if (message.charAt(i+1)=='3') message = message.replace("&3", "3");			
 					else if (message.charAt(i+1)=='4') message = message.replace("&4", "4");
 					else if (message.charAt(i+1)=='5') message = message.replace("&5", "5");			
 					else if (message.charAt(i+1)=='6') message = message.replace("&6", "6");
 					else if (message.charAt(i+1)=='7') message = message.replace("&7", "7");			
 					else if (message.charAt(i+1)=='8') message = message.replace("&8", "8");
 					else if (message.charAt(i+1)=='9') message = message.replace("&9", "9");			
 					else if (message.charAt(i+1)=='a') message = message.replace("&a", "a");
 					else if (message.charAt(i+1)=='b') message = message.replace("&b", "b");
 					else if (message.charAt(i+1)=='c') message = message.replace("&c", "c");
 					else if (message.charAt(i+1)=='d') message = message.replace("&d", "d");
 					else if (message.charAt(i+1)=='e') message = message.replace("&e", "e");
 					else if (message.charAt(i+1)=='f') message = message.replace("&f", "f");
 					else message = message.replaceFirst("&", " AND ");
 				}
 			}
 			catch (Exception e){
 				e.printStackTrace();
 			}
 		}
 		message = message.replaceAll(" AND ", "&");
 		return message;
 	}
 
 	/**
 	 * Sends a welcome message based on race of player joining. Broadcasts to the whole server
 	 * @param server
 	 * @param dwarf
 	 */
 	public static void welcome(Server server, Dwarf dwarf) {
 		try {
 			String raceName = "";
 			if(dwarf.isElf) raceName = "&fElf";
 			else raceName = "&9Dwarf";
 			sendBroadcast(server, "&fWelcome, "+raceName+" &6"+dwarf.player.getName() ,"&6[DC]         ");
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 
 	/** 
 	 * Sends a player the list of all schools and ID's
 	 * @param player
 	 * @return
 	 */
 	public static boolean schoolList(Player player) {
 		String message="";
 		for(School school:School.values()){
 			message = message.concat("&1"+ school.name() + "&6(&1" + school.id+"&6)  ");
 		}
 		sendMessage(player, message, "&6[&dSchoolList&6] ");
 		return true;
 	}
 
 	/**
 	 * Sends a player details about a school including its skills
 	 * @param player
 	 * @param school
 	 * @param skills is an array of skills in the specific school
 	 */
 	public static void schoolInfo(Player player, School school, Skill[] skills) {
 		String message = "";
 		for(Skill s:skills){
 			if (s!=null) message = message.concat("&b" + s.displayName + "&6(&b" +s.id+"&6) ");
 		}
 		sendMessage(player, message, "&6[&1"+school.name()+"&6] ");
 	}
 
 	public static boolean here(Player player, List<TrainingZone> list) {
 		for (TrainingZone tz:list){
 			sendMessage(player, "&6You are in &8"+tz.name+"&6 a &1"+tz.school+"&6 training zone","&6[&8Zone]");
 		}
 		return false;
 	}
 
 
 
 
 
 
 
 
 
 
 
 }
