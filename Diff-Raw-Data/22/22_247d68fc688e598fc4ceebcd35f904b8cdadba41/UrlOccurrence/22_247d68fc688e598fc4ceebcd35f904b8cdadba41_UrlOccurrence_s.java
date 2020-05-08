 
 /*
  * Copyright 2004-2013 ICEsoft Technologies Canada Corp.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the
  * License. You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing,
  * software distributed under the License is distributed on an "AS
  * IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
  * express or implied. See the License for the specific language
  * governing permissions and limitations under the License.
  */
 
 package org.icefaces.ace.util.cssurlmapper;
 
 public class UrlOccurrence {
 
 	private StringBuilder startBuffer ;
 	private StringBuilder urlBuffer;
 	private StringBuilder endBuffer;
 	
 	private boolean quoted = false;
 	private boolean doubleQuoted = false;
 	
 	private boolean valid = true;
 	private boolean complete = false;
 	
 	private int state = 0;
 	
 	private int lastChar = 0;
 	private boolean inComments = false;
 	
 	public UrlOccurrence() {
 		startBuffer = new StringBuilder();
 		urlBuffer = new StringBuilder();
 		endBuffer = new StringBuilder();
 	}
 	
 	// feed one character at a time, while checking format validity
 	public void feedCharacter(int c) {
 		
 		switch (state) {
 			case 0: // first character: u
 				startBuffer.appendCodePoint(c);
 				state = 1;
 				break;
 			case 1: // 'r'
 				startBuffer.appendCodePoint(c);
 				if (c == 'r' || c == 'R') {
 					state = 2;
 				} else {
 					this.valid = false;
 				}
 				break;
 			case 2: // 'l'
 				startBuffer.appendCodePoint(c);
 				if (c == 'l' || c == 'L') {
 					state = 3;
 				} else {
 					this.valid = false;
 				}
 				break;
 			case 3: // '('
 				startBuffer.appendCodePoint(c);
 				if (c == '(') {
 					state = 4;
 				} else {
 					this.valid = false;
 				}
 				break;
 			case 4: // whitespace and comments before the actual url, starting quotes
 				// check first for comments
 				if (c == '*' && lastChar == '/' && !inComments) { // comments start
 					inComments = true;
 					startBuffer.appendCodePoint(lastChar);
 					startBuffer.appendCodePoint(c);
 					lastChar = 0;
 					break;
 				} else if (c == '/' && lastChar == '*' && inComments) { // comments end
 					inComments = false;
 					startBuffer.appendCodePoint(c);
 					lastChar = 0;
 					break;
 				} else if (!inComments && (lastChar == '/' || lastChar == '*')) { // actual url happens to start with '/' or '*', no quotes
 					state = 5;
 					urlBuffer.appendCodePoint(lastChar);
 					urlBuffer.appendCodePoint(c);
 					this.quoted = false;
 					this.doubleQuoted = false;
 				} else if (Character.isWhitespace(c) || inComments) { // just add character
 					startBuffer.appendCodePoint(c);
 				} else {
 					if (c == '/' || c == '*') {
 						// do nothing yet, wait to see if they are comment markers
 					} else if (c == '\'') { // single quotes case
 						state = 5;
 						this.quoted = true;
 						this.doubleQuoted = false;
 						startBuffer.appendCodePoint(c);
 					} else if (c == '"') { // double quotes case
 						state = 5;
 						this.quoted = true;
 						this.doubleQuoted = true;
 						startBuffer.appendCodePoint(c);
 					} else if (c == ')') { // special case: empty url
 						endBuffer.appendCodePoint(c);
 						this.complete = true;
 						state = -1;
 					} else { // unquoted case, start capturing the actual url
 						state = 5;
 						this.quoted = false;
 						this.doubleQuoted = false;
 						urlBuffer.appendCodePoint(c);
 					}
 				}
 				
 				lastChar = c;
 				
 				break;
 			case 5: // the actual url
 				if (!this.quoted) {
 					if (c != ')') { // we're looking for the closing bracket
 						urlBuffer.appendCodePoint(c);
 					} else {
 						// ending bracket might have been found
 						if (lastChar != '\\') {
 							endBuffer.appendCodePoint(c);
 							state = -1;
 							this.complete = true;
 						} else { // it was an escaped bracket, just add to buffer
 							urlBuffer.appendCodePoint(c);
 						}
 					}
 				} else {
 					if (c != '\'' && c != '"') { // we're expecting a closing quote
 						urlBuffer.appendCodePoint(c);
 					} else {
 						// closing quote might have been found
 						if ((c == '\'' && !this.doubleQuoted)
 							|| (c == '"' && this.doubleQuoted)) {
 							endBuffer.appendCodePoint(c);
 							state = 6;
 						} else { // false alarm, just add to buffer
 							urlBuffer.appendCodePoint(c);
 						}
 					}
 				}
 				
 				lastChar = c;
 				
 				break;
 			case 6: // whitespace and comments after the actual url, there might be other characters as well
 				endBuffer.appendCodePoint(c);
 				if (c == ')') {
					this.complete = true;
 				}
 				break;
 			default:
 				endBuffer.appendCodePoint(c);
 				this.valid = false;
 				break;
 		}
 	}
 	
 	public String getLeadingChars() {
 		return this.startBuffer.toString();
 	}
 	
 	public String getUrlChars() {
 		return this.urlBuffer.toString();
 	}
 	
 	public String getTrailingChars() {
 		return this.endBuffer.toString();
 	}
 	
 	public boolean isValid() {
 		return valid;
 	}
 	
 	public boolean isComplete() {
 		return complete;
 	}
 	
 	public String toString() {
 		return startBuffer.toString() + urlBuffer.toString() + endBuffer.toString();
 	}
 }
