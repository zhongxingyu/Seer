 /*******************************************************************************
  * Copyright (c) 2009 xored software, Inc.  
  *
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html  
  *
  * Contributors:
  *     xored software, Inc. - initial API and Implementation (Vladimir Belov)
  *******************************************************************************/
 
 package org.eclipse.dltk.javascript.parser;
 
 import org.antlr.runtime.CharStream;
 import org.antlr.runtime.MismatchedTokenException;
 import org.antlr.runtime.RecognitionException;
 
 public class JavaScriptTokenSource extends JavaScriptLexer implements
 		JSTokenSource {
 
 	public JavaScriptTokenSource(CharStream input) {
 		super(input);
 	}
 
 	private int xmlMode = MODE_JS;
 
 	public int getMode() {
 		return xmlMode;
 	}
 
 	public void setMode(int mode) {
 		this.xmlMode = mode;
 	}
 
 	public void seek(int index) {
 		input.seek(index);
 	}
 
 	@Override
 	public void mTokens() throws RecognitionException {
 		if (xmlMode == MODE_XML) {
 			readNextXml();
 		} else if (xmlMode == MODE_EXPRESSION) {
 			try {
 				super.mTokens();
 			} catch (RecognitionException e) {
				recover(e);
 				type = XMLFragmentError;
 				emit();
 			}
 		} else {
 			super.mTokens();
 		}
 	}
 
 	private int xmlOpenTagsCount;
 	@SuppressWarnings("unused")
 	private boolean xmlIsAttribute;
 	private boolean xmlIsTagContent;
 
 	@Override
 	protected void readFirstXml() throws RecognitionException {
 		assert xmlMode == MODE_JS;
 		xmlOpenTagsCount = 0;
 		xmlIsAttribute = false;
 		xmlIsTagContent = false;
 		readNextXml();
 	}
 
 	private void readNextXml() {
 		try {
 			type = readXmlToken();
 			emit();
 		} catch (LexerException e) {
 			type = XMLFragmentError;
 			emit();
 		}
 	}
 
 	private int readXmlToken() {
 		int c;
 		while ((c = input.LA(1)) != EOF) {
 			if (xmlIsTagContent) {
 				switch (c) {
 				case '>':
 					matchAny();
 					xmlIsTagContent = false;
 					xmlIsAttribute = false;
 					break;
 				case '/':
 					matchAny();
 					if (input.LA(1) == '>') {
 						matchAny();
 						xmlIsTagContent = false;
 						xmlOpenTagsCount--;
 					}
 					break;
 				case '{':
 					// matchAny();
 					return XMLFragment;
 				case '\'':
 				case '\"':
 					matchAny();
 					readQuotedString(c);
 					break;
 				case '=':
 					matchAny();
 					xmlIsAttribute = true;
 					break;
 				case ' ':
 				case '\t':
 				case '\r':
 				case '\n':
 					matchAny();
 					break;
 				default:
 					matchAny();
 					xmlIsAttribute = false;
 					break;
 				}
 				if (!xmlIsTagContent && xmlOpenTagsCount == 0) {
 					return XMLFragmentEnd;
 				}
 			} else {
 				switch (c) {
 				case '<':
 					matchAny();
 					switch (input.LA(1)) {
 					case '!':
 						matchAny();
 						switch (input.LA(1)) {
 						case '-':
 							matchAny();
 							if (input.LA(1) == '-') {
 								matchAny();
 								readXmlComment();
 							} else {
 								throw new LexerException("msg.XML.bad.form");
 							}
 							break;
 						case '[':
 							matchAny();
 							try {
 								match("CDATA[");
 							} catch (MismatchedTokenException e) {
 								throw new LexerException("CDATA[ expected");
 							}
 							readCDATA();
 							break;
 						default:
 							readEntity();
 							break;
 						}
 						break;
 					case '?':
 						matchAny();
 						readPI();
 						break;
 					case '/':
 						// End tag
 						matchAny();
 						if (xmlOpenTagsCount == 0) {
 							throw new LexerException("msg.XML.bad.form");
 						}
 						xmlIsTagContent = true;
 						xmlOpenTagsCount--;
 						break;
 					default:
 						// Start tag
 						xmlIsTagContent = true;
 						xmlOpenTagsCount++;
 						break;
 					}
 					// /
 					break;
 				case '{':
 					// matchAny();
 					return XMLFragment;
 				default:
 					matchAny();
 					break;
 				}
 			}
 		}
 		throw new LexerException("msg.XML.bad.form");
 	}
 
 	private void readQuotedString(int quote) {
 		int c;
 		while ((c = input.LA(1)) != EOF) {
 			matchAny();
 			if (c == quote) {
 				return;
 			}
 		}
 		throw new LexerException("msg.XML.bad.form");
 	}
 
 	private void readXmlComment() {
 		int c;
 		while ((c = input.LA(1)) != EOF) {
 			matchAny();
 			if (c == '-' && input.LA(1) == '-' && input.LA(2) == '>') {
 				return;
 			}
 		}
 		throw new LexerException("msg.XML.bad.form");
 	}
 
 	private void readCDATA() {
 		int c;
 		while ((c = input.LA(1)) != EOF) {
 			matchAny();
 			if (c == ']' && input.LA(1) == ']' && input.LA(2) == '>')
 				return;
 		}
 		throw new LexerException("msg.XML.bad.form");
 	}
 
 	private void readEntity() {
 		int declTags = 1;
 		int c;
 		while ((c = input.LA(1)) != EOF) {
 			matchAny();
 			switch (c) {
 			case '<':
 				declTags++;
 				break;
 			case '>':
 				declTags--;
 				if (declTags == 0)
 					return;
 				break;
 			}
 		}
 		throw new LexerException("msg.XML.bad.form");
 	}
 
 	private void readPI() {
 		int c;
 		while ((c = input.LA(1)) != EOF) {
 			matchAny();
 			if (c == '?' && input.LA(1) == '>') {
 				matchAny();
 				return;
 			}
 		}
 		throw new LexerException("msg.XML.bad.form");
 	}
 
 	@SuppressWarnings("serial")
 	private static class LexerException extends RuntimeException {
 
 		public LexerException(String message) {
 			super(message);
 		}
 
 	}
 
 }
