 package com.dacklabs.spookyaction.shared;
 
 /**
  * Object that executes commands on an editing surface.
  * 
  * @author "David Ackerman (david.w.ackerman@gmail.com)"
  */
 public class CommandExecutor {
 
 	public static void processCommand(LineBasedEditor editingSurface, Command command) {
 		int lineNumber = command.getLineNumber();
 		StringBuffer line = editingSurface.getLine(lineNumber);
 
 		for (int i = 0; i < command.getRepeated(); i++) {
 
 			switch (command.getType()) {
 			case KEY:
 				line.insert(command.getOffset(), command.getData());
 				editingSurface.updateLine(lineNumber, line);
				break;
 			case BACKSPACE:
 				if (command.getOffset() < 1) {
 					if (lineNumber > 0) {
 						StringBuffer previousLine = editingSurface.getLine(lineNumber - 1);
 						previousLine.append(line);
 						editingSurface.updateLine(lineNumber - 1, previousLine);
 						editingSurface.removeLine(lineNumber);
 					}
 				} else {
 					line.deleteCharAt(command.getOffset() - 1);
 					editingSurface.updateLine(lineNumber, line);
 				}
				break;
 			case NEWLINE:
 				String afterCursor = line.substring(command.getOffset());
 				line.delete(command.getOffset(), line.length());
 				editingSurface.insertLine(lineNumber + 1, new StringBuffer(afterCursor));
 				editingSurface.updateLine(lineNumber, line);
				break;
 			}
 		}
 
 		switch (command.getType()) {
 		case MOVE_LINE:
 			int repeated = command.getRepeated();
 			if (repeated > 0) {
 				for (int i = 0; i < repeated; i++) {
 					editingSurface.swapLines(lineNumber + i, lineNumber + i + 1);
 				}
 			} else {
 				for (int i = 0; i > repeated; i--) {
 					editingSurface.swapLines(lineNumber + i, lineNumber + i - 1);
 				}
 			}
 		}
 	}
 }
