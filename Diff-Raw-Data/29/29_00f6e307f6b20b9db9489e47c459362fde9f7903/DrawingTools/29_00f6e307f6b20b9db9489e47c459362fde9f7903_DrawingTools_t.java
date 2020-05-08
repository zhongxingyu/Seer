 package com.wolvencraft.prison.mines.util;
 
 public enum DrawingTools {
	CornerTopLeft('+'),
	CornerTopRight('+'),
	CornerBottomLeft('+'),
	CornerBottomRight('+'),
	LineHorizontal('-'),
	LineVertical('|'),
	WhiteSpace(' '),
	Color('\u00A7');
 	
	DrawingTools(char character) {
 		this.character = character;
 	}
 	
	char character;
 	
 	@Override
	public String toString() { return character + ""; }
	
	public char toChar() { return character; }
 	
 	public static String getAllCharacters() {
 		String all = "";
 		for(DrawingTools tool : DrawingTools.values()) all += tool.toString();
 		return all;
 	}
 	
 	public static boolean isPresent(char ch) {
 		for(DrawingTools tool : DrawingTools.values()) {
 			if(tool.toString().equals(ch)) return true;
 		}
 		return false;
 	}
 	
 	public static int getTrueLength(String str) {
 		boolean skipNext = false;
 		int length = 0;
 		
 		for(char ch : str.toCharArray()) {
 			if(skipNext) {
 				skipNext = false;
 				continue;
 			}
 			
			if(ch == '&' || ch == DrawingTools.Color.toChar()) {
 				skipNext = true;
 				continue;
 			}
 			
 			length++;
 		}
 		return length;
 	}
 }
