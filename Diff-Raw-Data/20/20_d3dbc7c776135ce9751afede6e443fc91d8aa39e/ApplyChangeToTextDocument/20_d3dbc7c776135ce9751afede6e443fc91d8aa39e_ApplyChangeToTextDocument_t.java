 package edu.ycp.cs.netcoder.server.logchange;
 
 /**
  * Apply a Change object to a TextDocument.
  */
 public class ApplyChangeToTextDocument {
 	private static final boolean DEBUG = true;
 
 	/**
 	 * Apply a Change object to a TextDocument.
 	 * 
 	 * @param change a Change object
 	 * @param doc    a TextDocument to which the Change should be applied
 	 */
 	public void apply(Change change, TextDocument doc) {
 		String s, up;
 		
 		switch (change.getType()) {
 		case INSERT_TEXT:
 			if (change.getStartRow() == doc.getNumLines()) {
 				doc.append("");
 			}
 			s = doc.getLine(change.getStartRow());
 			up = s.substring(0, change.getStartColumn()) + change.getText() + s.substring(change.getStartColumn());
 			changeLine(doc, change.getStartRow(), up);
 			break;
 		case REMOVE_TEXT:
			if (change.getText().equals("\n")) {
				if (change.getStartRow() + 1 != change.getEndRow()) {
					throw new IllegalArgumentException("unexpected REMOVE_TEXT to remove newline: " + change);
				}
				// combine with line below
				s = doc.getLine(change.getStartRow());
				up = s.substring(0, s.length() - 1) + doc.getLine(change.getEndRow());
				doc.removeLine(change.getEndRow());
			} else {
				s = doc.getLine(change.getStartRow());
				up = s.substring(0, change.getStartColumn()) + s.substring(change.getEndColumn());
 			}
 			doc.setLine(change.getStartRow(), up);
 			break;
 		case INSERT_LINES:
 			for (int i = 0; i < change.getNumLines(); i++) {
 				doc.insertLine(change.getStartRow() + i, change.getLine(i) + "\n");
 			}
 			break;
 		case REMOVE_LINES:
 			for (int i = 0; i < change.getNumLines(); i++) {
 				doc.removeLine(change.getStartRow());
 			}
 			break;
 		default:
 			throw new IllegalStateException("Not handled? " + change.getType());
 		}
 		
 		// check integrity of TextDocument
 		if (DEBUG) {
 			for (int i = 0; i < doc.getNumLines(); i++) {
 				String line = doc.getLine(i);
 				int nl = line.indexOf('\n');
 				if (nl >= 0 && nl != line.length() - 1) {
 					throw new IllegalStateException("Line has enbedded newline!");
 				}
 			}
 		}
 	}
 	
 	/**
 	 * Change text at given line, inserting multiple lines as necessary
 	 * if text has embedded newlines.
 	 * 
 	 * @param doc   the TextDocument
 	 * @param index index of line to change
 	 * @param text  text to put at given index
 	 */
 	private void changeLine(TextDocument doc, int index, String text) {
 		int nl = text.indexOf('\n');
 
 		if (nl < 0 || nl == text.length() - 1) {
 			// Line either has no newline, or there is only one newline at
 			// the end of the line
 			doc.setLine(index, text);
 			return;
 		}
 		
 		// line contains embedded newlines: need to split
 		doc.removeLine(index);
 		boolean done = false;
 		while (!done) {
 			doc.insertLine(index, text.substring(0, nl+1));
 			index++;
 			text = text.substring(nl + 1);
 			nl = text.indexOf('\n');
 			done = (nl < 0);
 		}
 	}
 }
