 package net.amoebaman.gamemaster.utils;
 
 import java.util.HashMap;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.DyeColor;
 
 public class ChatUtils {
 
 	private static final int SCREEN_WIDTH = 316;
 	private static final int DEFAULT_CHAR_WIDTH = 6;
 	private static final HashMap<Character, Integer> IRREG_CHAR_WIDTH = new HashMap<Character, Integer>();
 	static{
 		IRREG_CHAR_WIDTH.put(' ', 4);
 		IRREG_CHAR_WIDTH.put('i', 2);
 		IRREG_CHAR_WIDTH.put('I', 4);
 		IRREG_CHAR_WIDTH.put('k', 5);
 		IRREG_CHAR_WIDTH.put('l', 3);
 		IRREG_CHAR_WIDTH.put('t', 4);
 		IRREG_CHAR_WIDTH.put('!', 2);
 		IRREG_CHAR_WIDTH.put('(', 5);
 		IRREG_CHAR_WIDTH.put(')', 5);
 		IRREG_CHAR_WIDTH.put('~', 7);
 		IRREG_CHAR_WIDTH.put(',', 2);
 		IRREG_CHAR_WIDTH.put('.', 2);
 		IRREG_CHAR_WIDTH.put('<', 5);
 		IRREG_CHAR_WIDTH.put('>', 5);
 		IRREG_CHAR_WIDTH.put(':', 2);
 		IRREG_CHAR_WIDTH.put(';', 2);
 		IRREG_CHAR_WIDTH.put('"', 5);
 		IRREG_CHAR_WIDTH.put('[', 4);
 		IRREG_CHAR_WIDTH.put(']', 4);
 		IRREG_CHAR_WIDTH.put('{', 5);
 		IRREG_CHAR_WIDTH.put('}', 5);
 		IRREG_CHAR_WIDTH.put('|', 2);
 		IRREG_CHAR_WIDTH.put('`', 0);
 		IRREG_CHAR_WIDTH.put('\'', 2);
 		IRREG_CHAR_WIDTH.put(ChatColor.COLOR_CHAR, 0);
 	}
 	
 	private static int getCharWidth(char value){
 		if(IRREG_CHAR_WIDTH.containsKey(value))
 			return IRREG_CHAR_WIDTH.get(value);
 		return DEFAULT_CHAR_WIDTH;
 	}
 	
 	private static int getStringWidth(String str){
 		int length = 0;
 		for(int i = 0; i < str.length(); i++)
 			if(i == 0)
 				length += getCharWidth(str.charAt(i));
 			else if(str.charAt(i - 1) != ChatColor.COLOR_CHAR)
 				length += getCharWidth(str.charAt(i));
 		return length;
 	}
 	
 	public static String spacerLine(){
 		return ChatColor.RESET.toString();
 	}
 	
 	public static String fillerLine(String pattern){
 		float length = getStringWidth(pattern);
 		int iterations = (int) (SCREEN_WIDTH / length);
 		String line = "";
 		for(int i = 0; i < iterations; i++)
 			line += pattern;
 		return centerAlign(line);
 	}
 	
 	public static String centerAlign(String text){
 		int numSpaces = ((SCREEN_WIDTH - getStringWidth(text)) / 2) / getCharWidth(' ');
 		for(int i = 0; i < numSpaces; i++)
 			text = " " + text;
 		return text;
 	}
 	
 	public static ChatColor dyeToChatColor(DyeColor color){
 		if(color == null) return ChatColor.RESET;
 		switch(color){
 		case BLACK: return ChatColor.BLACK;
 		case BLUE: return ChatColor.BLUE;
 		case BROWN: return ChatColor.BOLD;
 		case CYAN: return ChatColor.DARK_AQUA;
 		case GRAY: return ChatColor.DARK_GRAY;
 		case GREEN: return ChatColor.DARK_GREEN;
 		case LIGHT_BLUE: return ChatColor.BLUE;
 		case LIME: return ChatColor.GREEN;
 		case MAGENTA: return ChatColor.LIGHT_PURPLE;
 		case ORANGE: return ChatColor.GOLD;
 		case PINK: return ChatColor.RED;
 		case PURPLE: return ChatColor.DARK_PURPLE;
 		case RED: return ChatColor.RED;
 		case SILVER: return ChatColor.GRAY;
 		case WHITE: return ChatColor.WHITE;
 		case YELLOW: return ChatColor.YELLOW;
 		default: return ChatColor.RESET;
 		}
 	}
 	
 	public static String format(String line){
		line = line.replace("&&", ChatColor.COLOR_CHAR + "");
		line = line.replace("//", ChatColor.ITALIC.toString());
 		line = line.replace("**", ChatColor.BOLD.toString());
 		line = line.replace("__", ChatColor.UNDERLINE.toString());
 		line = line.replace("\\\\", ChatColor.RESET.toString());
 		return line;
 	}
 	
 	public static String format(String line, ColorScheme scheme){
 		line = scheme.standard + format(line);
 		line = line.replace("[[[", "[[");
 		line = line.replace("]]]", "]]");
 		line = line.replace("[[", scheme.emphasis.toString());
 		line = line.replace("]]", scheme.standard.toString());
 		line = line.replace("", "[");
 		line = line.replace("", "]");
 		return line;
 	}
 	
 	public static void bigBroadcast(String... lines){
 		Bukkit.broadcastMessage(spacerLine());
 		for(String str : lines)
 			Bukkit.broadcastMessage(centerAlign(format(str)));
 		Bukkit.broadcastMessage(spacerLine());
 	}
 	
 	public static void bigBroadcast(ColorScheme scheme, String... lines){
 		Bukkit.broadcastMessage(spacerLine());
 		for(String str : lines)
 			Bukkit.broadcastMessage(centerAlign(format(str, scheme)));
 		Bukkit.broadcastMessage(spacerLine());
 	}
 	
 	public enum ColorScheme{
 		NORMAL(ChatColor.GRAY, ChatColor.DARK_GRAY),
 		HIGHLIGHT(ChatColor.GOLD, ChatColor.DARK_RED),
 		ERROR(ChatColor.RED, ChatColor.DARK_GRAY),
 		;
 		public ChatColor standard, emphasis;
 		private ColorScheme(ChatColor standard, ChatColor emphasis){
 			this.standard = standard;
 			this.emphasis = emphasis;
 		}
 	}
 }
