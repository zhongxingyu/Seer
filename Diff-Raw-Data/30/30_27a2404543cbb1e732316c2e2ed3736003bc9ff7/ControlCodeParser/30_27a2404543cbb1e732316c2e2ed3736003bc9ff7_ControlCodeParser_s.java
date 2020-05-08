 package shared;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.custom.StyleRange;
 
 public class ControlCodeParser {
 	public static List<StyleRange> parseControlCodes(String input) {
 		return parseControlCodes(input,0);
 	}
 	
 	public static List<StyleRange> parseControlCodes(String input, int dispos) {
 		List<StyleRange> styleRanges = new ArrayList<StyleRange>();
 		if(input == null || input.length() == 0) return styleRanges;
 		
 		Customs custom = new Customs();
 		
 		StyleRange current = new StyleRange();
 		
 		int offset = 0;
 		int addToOffset = 0;
 		
 		boolean newRange = false;
 		
 		boolean bold = false;
 		boolean oldBold = false;
 		
 		boolean underline = false;
 		boolean oldUnderline = false;
 		
 		boolean italic = false;
 		boolean oldItalic = false;
 		
 		int foregroundColor = -1;
 		int previousFgColor = -1;
 		
 		int backgroundColor = -1;
 		int previousBgColor = -1;
 		
 		current.start = dispos;
 		
 		char[] inputArray = input.toCharArray();
 		
 		for(int i = 0; i < input.length(); i++) {
 			char curchar = inputArray[i];
 			oldBold = bold;
 			oldUnderline = underline;
 			oldItalic = italic;
 			
 			previousFgColor = foregroundColor;
 			previousBgColor = backgroundColor;
 			
 			switch(curchar) {
 				case '\u000f': //Normal (Kills all formatting)
 					bold = false;
 					underline = false;
 					italic = false;
 					foregroundColor = -1;
 					backgroundColor = -1;
 					
 					newRange = true;
 					addToOffset++;
 					break;
 				case '\u0002': //Bold
 					bold = !bold;
 					newRange = true;
 					addToOffset++;
 					break;
 				case '\u001f': //Underline
 					underline = !underline;
 					newRange = true;
 					addToOffset++;
 					break;
 				case '\u0016': //Italics
 					italic = !italic;
 					newRange = true;
 					addToOffset++;
 					break;
 				case '\u0003': //Color
 					char nextchar;
 					boolean pastComma = false;
 					String fgColor = "";
 					String bgColor = "";
 					
 					for(int j = i; j < i+5; j++) {
 						nextchar = inputArray[j+1];
 						if(nextchar >= '0' && nextchar <= '9') {
 							if(!pastComma) {
 								if(fgColor.length() < 2) fgColor += nextchar;
 								else break;
 							}
 							else {
 								if(bgColor.length() < 2) bgColor += nextchar;
 								else break;
 							}
 						}
 						else if(nextchar == ',') {
 							if(!pastComma) pastComma = true;
 							else break;
 						}
 						else break;
 					}
 					
 					if(fgColor.length() > 0) foregroundColor = Integer.parseInt(fgColor);
 					else foregroundColor = -1;
 					if(bgColor.length() > 0) backgroundColor = Integer.parseInt(bgColor);
 					else backgroundColor = -1;
 					
 					newRange = true;
 					
 					addToOffset++;
 					addToOffset += fgColor.length();
 					if(pastComma) addToOffset++;
 					addToOffset += bgColor.length();
 					
 					break;
 			}
 			
 			if(newRange) {
 				current.length = (i - offset + dispos) - current.start;
 				
 				offset += addToOffset;
 								
 				styleRanges.add(current);
 				
 				current = (StyleRange)current.clone();
 				
 				if(bold != oldBold) {
 					if(bold) current.fontStyle |= SWT.BOLD;
 					else current.fontStyle &= ~SWT.BOLD;
 				}
 				if(italic != oldItalic) {
 					if(italic) current.fontStyle |= SWT.ITALIC;
 					else current.fontStyle &= ~SWT.ITALIC;
 				}
 				if(underline != oldUnderline) current.underline = underline;
 				
 				if(previousFgColor != foregroundColor) {
 					if(foregroundColor != -1) {
 						for(String colorName : custom.ircColors.keySet()) {
 							if(custom.ircColors.get(colorName) == foregroundColor) {
 								current.foreground = custom.colors.get(colorName);
 							}
 						}
 					}
 					else {
 						current.foreground = null;
 					}
 				}
 				if(previousBgColor != backgroundColor) {
 					if(backgroundColor != -1) {
 						for(String colorName : custom.ircColors.keySet()) {
 							if(custom.ircColors.get(colorName) == backgroundColor) {
 								current.background = custom.colors.get(colorName);
 							}
 						}
 					}
 					else {
 						current.background = null;
 					}
 				}
 					
 				newRange = false;
 				current.start = i - offset + dispos + addToOffset;
 				
 				addToOffset = 0;
 			}
 		}
 		
 		styleRanges.add(current);
 		
		current.length = (input.length()+dispos) - offset - 2 - current.start;
 				
 		return styleRanges;
 	}
 }
