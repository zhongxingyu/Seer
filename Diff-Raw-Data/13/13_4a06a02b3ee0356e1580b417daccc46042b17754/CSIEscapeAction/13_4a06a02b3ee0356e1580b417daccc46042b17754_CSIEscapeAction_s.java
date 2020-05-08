 package terminator.terminal.escape;
 
 import e.util.*;
 import terminator.model.*;
 import terminator.terminal.*;
 
 /**
 Parses 'CSI' escape sequences.  Such sequences always have '[' as their first character,
 and then are sometimes followed by a '?' character, then optionally a list of numbers
 separated by ';' characters, followed by the final character which tells us what to do with
 all that stuff.
 
 @author Phil Norman
 */
 
 public class CSIEscapeAction implements TerminalAction {
 	private TerminalControl control;
 	private String sequence;
 	
 	public CSIEscapeAction(TerminalControl control, String sequence) {
 		this.control = control;
 		this.sequence = sequence;
 	}
 
         public void perform(TerminalModelModifier model) {
                 char lastChar = sequence.charAt(sequence.length() - 1);
                 String midSequence = sequence.substring(1, sequence.length() - 1);
                 switch (lastChar) {
                 case 'A': moveCursor(model, midSequence, 0, -1); break;
                 case 'B': moveCursor(model, midSequence, 0, 1); break;
                 case 'C': moveCursor(model, midSequence, 1, 0); break;
                 case 'D': moveCursor(model, midSequence, -1, 0); break;
                 case 'H': moveCursorTo(model, midSequence); break;
                 case 'K': clearLine(model, midSequence); break;
                 case 'J': clearScreen(model, midSequence); break;
                 case 'L': insertLines(model, midSequence); break;
                 case 'M': deleteLines(model, midSequence); break;
                 case 'P': deleteCharacters(model, midSequence); break;
                 case 'h': setDecPrivateMode(model, midSequence, true); break;
                 case 'l': setDecPrivateMode(model, midSequence, false); break;
                 case 'm': processFontEscape(model, midSequence); break;
                 case 'r': setScrollingRegion(model, midSequence); break;
                 default: Log.warn("unknown CSI sequence " + StringUtilities.escapeForJava(sequence)); break;
                 }
         }
 
 	private String getSequenceType(char lastChar) {
 		switch (lastChar) {
 		case 'A': return "Cursor up";
 		case 'B': return "Cursor down";
 		case 'C': return "Cursor right";
 		case 'D': return "Cursor left";
 		case 'H': return "Move cursor to";
 		case 'K': return "Kill line contents";
 		case 'J': return "Kill lines";
 		case 'L': return "Insert lines";
 		case 'M': return "Delete lines";
 		case 'P': return "Delete characters";
 		case 'h': return "Set DEC private mode";
 		case 'l': return "Clear DEC private mode";
 		case 'm': return "Set font, color, etc";
 		case 'r': return "Set scrolling region";
 		default: return "Unknown:" + lastChar;
 		}
 	}
 	
 	public String toString() {
 		char lastChar = sequence.charAt(sequence.length() - 1);
 		return "CSIEscapeAction[" + getSequenceType(lastChar) + "]";
 	}
 
         private int parseParameter(String string, int standard) {
                 return string.length() == 0 ? standard : Integer.parseInt(string);
         }
 
         private int parseCount(String string) {
                 return parseParameter(string, 1);
         }
 
         private int parseType(String string) {
                 return parseParameter(string, 0);
         }
 
         private void deleteLines(TerminalModelModifier model, String seq) {
                 model.deleteLines(parseCount(seq));
 	}
 	
         private void insertLines(TerminalModelModifier model, String seq) {
 		model.insertLines(parseCount(seq));
 	}
 	
         private void setDecPrivateMode(TerminalModelModifier model, String seq, boolean value) {
 		boolean isPrivateMode = seq.startsWith("?");
 		String[] modes = (isPrivateMode ? seq.substring(1) : seq).split(";");
 		for (String modeString : modes) {
 			int mode = Integer.parseInt(modeString);
 			if (isPrivateMode) {
 				switch (mode) {
 				case 25:
 					model.setCursorVisible(value);
 					break;
 				default:
 					Log.warn("Unknown private mode " + mode + " in [" + StringUtilities.escapeForJava(seq) + (value ? 'h' : 'l'));
 				}
 			} else {
 				switch (mode) {
 				case 4:
 					model.setInsertMode(value);
 					break;
 				default:
 					Log.warn("Unknown mode " + mode + " in [" + StringUtilities.escapeForJava(seq) + (value ? 'h' : 'l'));
 				}
 			}
 		}
 	}
 	
         private void setScrollingRegion(TerminalModelModifier model, String seq) {
 		int index = seq.indexOf(';');
 		if (index == -1)
                         return;
                 model.setScrollingRegion(Integer.parseInt(seq.substring(0, index)) - 1,
                                          Integer.parseInt(seq.substring(index + 1)) - 1);
 	}
 
         private void deleteCharacters(TerminalModelModifier model, String seq) {
 		model.deleteCharacters(parseCount(seq));
 	}
 
         private void clearLine(TerminalModelModifier model, String seq) {
                 int type = parseType(seq);
                 switch (type) {
                case 0: model.clearToBeginningOfLine(); break;
                case 1: model.clearToEndOfLine(); break;
                 default: Log.warn("Unknown line clearing request " + type); break;
                 }
 	}
 	
         private void clearScreen(TerminalModelModifier model, String seq) {
                 int type = parseType(seq);
                 switch (parseType(seq)) {
                 case 0: model.clearToEndOfScreen(); break;
                 default: Log.warn("Unknown screen clearing request " + type); break;
                 }
 	}
 	
 	private void moveCursorTo(TerminalModelModifier model, String seq) {
 		int row = 1;
 		int column = 1;
 		int splitIndex = seq.indexOf(';');
 		if (splitIndex != -1) {
 			row = Integer.parseInt(seq.substring(0, splitIndex));
 			column = Integer.parseInt(seq.substring(splitIndex + 1));
 		}
 		model.setCursorPosition(row - 1, column - 1);
 	}
 	
         private void moveCursor(TerminalModelModifier model, String seq, int xDirection, int yDirection) {
                 int count = parseCount(seq);
 		if (xDirection != 0) {
 			model.moveCursorHorizontally(xDirection * count);
 		}
 		if (yDirection != 0) {
 			model.moveCursorVertically(yDirection * count);
 		}
 	}
 	
         private void processFontEscape(TerminalModelModifier model, String seq) {
 		int oldStyle = model.getStyle();
 		int foreground = StyledText.getForeground(oldStyle);
 		int background = StyledText.getBackground(oldStyle);
 		boolean isReverseVideo = StyledText.isReverseVideo(oldStyle);
 		boolean isUnderlined = StyledText.isUnderlined(oldStyle);
 		boolean hasForeground = StyledText.hasForeground(oldStyle);
 		boolean hasBackground = StyledText.hasBackground(oldStyle);
 		String[] chunks = seq.split(";");
 		for (String chunk : chunks) {
 			int value = (chunk.length() == 0) ? 0 : Integer.parseInt(chunk);
 			switch (value) {
 			case 0:
 				// Clear all attributes.
 				hasForeground = false;
 				hasBackground = false;
 				isReverseVideo = false;
 				isUnderlined = false;
 				break;
 			case 4:
 				isUnderlined = true;
 				break;
 			case 7:
 				isReverseVideo = true;
 				break;
 			case 24:
 				isUnderlined = false;
 				break;
 			case 27:
 				isReverseVideo = false;
 				break;
 			case 30:
 			case 31:
 			case 32:
 			case 33:
 			case 34:
 			case 35:
 			case 36:
 			case 37:
 				// Set foreground color.
 				foreground = value - 30;
 				hasForeground = true;
 				break;
 			case 39:
 				// Use default foreground color.
 				hasForeground = false;
 				break;
 			case 40:
 			case 41:
 			case 42:
 			case 43:
 			case 44:
 			case 45:
 			case 46:
 			case 47:
 				// Set background color.
 				background = value - 40;
 				hasBackground = true;
 				break;
 			case 49:
 				// Use default background color.
 				hasBackground = false;
 				break;
 			default:
 				Log.warn("Unknown attribute " + value + " in [" + StringUtilities.escapeForJava(seq));
 				break;
 			}
 		}
 		model.setStyle(StyledText.getStyle(foreground, hasForeground, background, hasBackground, isUnderlined, isReverseVideo));
 	}
 }
