 package com.nuclearw.farion;
 
 public class ColorConverter {
 	private static final String[][] convertTable = {
 		{"\u000300" , "\u00A7f"},	// White
 		{"\u000301" , "\u00A70"},	// Black
 		{"\u000302" , "\u00A71"},	// Dark Blue
 		{"\u000303" , "\u00A72"},	// Dark Green
 		{"\u000304" , "\u00A74"},	// Red
 		{"\u000305" , "\u00A76"},	// Brown
 		{"\u000306" , "\u00A75"},	// Purple
 		{"\u000307" , "\u00A7a"},	// Olive
 		{"\u000308" , "\u00A7e"},	// Yellow
 		{"\u000309" , "\u00A72"},	// Green
 		{"\u000310" , "\u00A73"},	// Teal
 		{"\u000311" , "\u00A7b"},	// Cyan
 		{"\u000312" , "\u00A79"},	// Blue
 		{"\u000313" , "\u00A7d"},	// Magenta
 		{"\u000314" , "\u00A78"},	// Dark Gray
 		{"\u000315" , "\u00A77"},	// Light Gray
 		{"\u000f" , "\u00A7r"},	// Reset
 		{"\u0002" , "\u00A7l"},	// Bold
 		{"\u001f" , "\u00A7n"},	// Underline
 	};
 
 	public static String ircToMinecraft(String input) {
 		input = removeIrcBackgroundColors(input);
 		return swapFromTable(input, 0);
 	}
 
 	public static String minecraftToIrc(String input) {
 		return swapFromTable(input, 1);
 	}
 
 	private static String swapFromTable(String input, int from) {
 		int to = (from == 1) ? 0 : 1;
 		for(String[] pair : convertTable) {
 			input = input.replaceAll(pair[from], pair[to]);
 		}
 		return input;
 	}
 
 	private static String removeIrcBackgroundColors(String input) {
		return input.replaceAll("\u0003([0-9]{1}[0-5]?){1}(?:,[0-9]{1}[0-5]?){1}(.*)", "\u0003$1$2");
 	}
 }
