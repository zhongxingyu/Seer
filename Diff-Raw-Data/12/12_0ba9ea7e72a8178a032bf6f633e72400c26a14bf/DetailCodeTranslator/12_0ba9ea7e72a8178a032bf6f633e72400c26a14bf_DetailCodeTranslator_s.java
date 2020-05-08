 /*******************************************************************************
  * Copyright (c) 2011 protos software gmbh (http://www.protos.de).
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * CONTRIBUTORS:
  * 		Henrik Rentz-Reichert (initial contribution)
  * 
  *******************************************************************************/
 
 package org.eclipse.etrice.generator.base;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 
 import org.eclipse.etrice.core.room.ActorClass;
 import org.eclipse.etrice.core.room.Attribute;
 import org.eclipse.etrice.core.room.DetailCode;
 import org.eclipse.etrice.core.room.InterfaceItem;
 import org.eclipse.etrice.core.room.Message;
 import org.eclipse.etrice.core.room.Operation;
 import org.eclipse.etrice.core.room.util.RoomHelpers;
 
 /**
  * @author Henrik Rentz-Reichert
  *
  */
 public class DetailCodeTranslator {
 
 	public static interface ITranslationProvider {
 		String getAttributeText(Attribute att, String orig);
 		String getOperationText(Operation op, ArrayList<String> args, String orig);
 		String getInterfaceItemMessageText(InterfaceItem item, Message msg, ArrayList<String> args, String orig);
 		String getInterfaceItemMessageValue(InterfaceItem item, Message msg, String orig);
 	}
 
 	private static class Position {
 		int pos = 0;
 	}
 	
 	private ActorClass ac;
 	private ITranslationProvider provider;
 	private HashMap<String, InterfaceItem> name2item;
 	private HashMap<String, Attribute> name2attr;
 	private HashMap<String, Operation> name2op;
 	
 	public DetailCodeTranslator(ActorClass ac, ITranslationProvider provider) {
 		this.ac = ac;
 		this.provider = provider;
 		
 		prepare();
 	}
 	
 	public String translateDetailCode(DetailCode code) {
 		if (code==null)
 			return null;
 		
 		// concatenate lines
 		StringBuilder text = new StringBuilder();
 		for (String line : code.getCommands()) {
 			text.append(line+"\n");
 		}
 
 		StringBuilder result = new StringBuilder();
 		translateText(text.substring(0, text.length()-1), result);
 		
 		return result.toString();
 	}
 	
 	private void translateText(String text, StringBuilder result) {
 		Position curr = new Position();
 		int last = 0;
 		
 		while (curr.pos<text.length()) {
 			proceedToToken(text, curr);
 			
 			last = appendParsed(text, curr, last, result);
 			
 			String token = getToken(text, curr);
 			if (token.isEmpty()) {
				while (curr.pos<text.length() && !isTokenChar(text.charAt(curr.pos)))
 					++curr.pos;
 				last = appendParsed(text, curr, last, result);
 			}
 			else {
 				// translate token if possible
 				String translated = null;
 				Attribute attribute = name2attr.get(token);
 				if (attribute!=null) {
 					String orig = text.substring(last, curr.pos);
 					translated = provider.getAttributeText(attribute, orig);
 				}
 				else {
 					Operation operation = name2op.get(token);
 					if (operation!=null) {
 						ArrayList<String> args = getArgs(text, curr);
 						if (args!=null && operation.getArguments().size()==args.size()) {
 							// recursively apply this algorithm to each argument
 							for (int i=0; i<args.size(); ++i) {
 								StringBuilder transArg = new StringBuilder();
 								translateText(args.remove(i), transArg);
 								args.add(i, transArg.toString());
 							}
 							String orig = text.substring(last, curr.pos);
 							translated = provider.getOperationText(operation, args, orig);
 						}
 					}
 					else {
 						InterfaceItem item = name2item.get(token);
 						if (item!=null) {
 							int start = curr.pos;
 							Message msg = getMessage(text, curr, item, true);
 							if (msg!=null) {
 								ArrayList<String> args = getArgs(text, curr);
 								if (args!=null) {
 									if (argsMatching(msg, args)) {
 										String orig = text.substring(last, curr.pos);
 										translated = provider.getInterfaceItemMessageText(item, msg, args, orig);
 									}
 								}
 							}
 							else {
 								curr.pos = start;
 								msg = getMessage(text, curr, item, false);
 								if (msg!=null) {
 									if (text.charAt(curr.pos)!='(') {
 										String orig = text.substring(last, curr.pos);
 										translated = provider.getInterfaceItemMessageValue(item, msg, orig);
 									}
 								}
 							}
 						}
 					}
 				}
 				if (translated!=null) {
 					last = curr.pos;
 					result.append(translated);
 				}
 				else
 					last = appendParsed(text, curr, last, result);
 			}
 		}
 	}
 
 	/**
 	 * @param text
 	 * @param result
 	 * @return 
 	 */
 	private int appendParsed(String text, Position curr, int last, StringBuilder result) {
 		String str = text.substring(last, curr.pos);
 		result.append(str);
 		return curr.pos;
 	}
 
 	private boolean argsMatching(Message msg, ArrayList<String> args) {
 		if (msg.getData()==null && args.isEmpty())
 			return true;
 		if (msg.getData()!=null && args.size()==1)
 			return true;
 		
 		return false;
 	}
 
 	private void proceedToToken(String text, Position curr) {
 		boolean stop = false;
 		while (curr.pos<text.length() && !stop) {
 			if (text.charAt(curr.pos)=='"') {
 				skipString(text, curr);
 			}
 			else if (text.charAt(curr.pos)=='/') {
 				if (curr.pos+1<text.length()) {
 					if (text.charAt(curr.pos+1)=='/') {
 						skipSingleComment(text, curr);
 					}
 					else if (text.charAt(curr.pos+1)=='*') {
 						skipMultiComment(text, curr);
 					}
 				}
 			}
 			else if (Character.isWhitespace(text.charAt(curr.pos))) {
 				skipWhiteSpace(text, curr);
 			}
 			else
 				stop = true;
 		}
 	}
 	
 	private Message getMessage(String text, Position curr, InterfaceItem item, boolean outgoing) {
 		proceedToToken(text, curr);
 
 		if (text.charAt(curr.pos)!='.')
 			return null;
 		++curr.pos;
 		
 		proceedToToken(text, curr);
 		
 		String token = getToken(text, curr);
 		
 		List<Message> messages = RoomHelpers.getMessageList(item, outgoing);
 		for (Message message : messages) {
 			if (message.getName().equals(token))
 				return message;
 		}
 		
 		return null;
 	}
 	
 	private ArrayList<String> getArgs(String text, Position curr) {
 		proceedToToken(text, curr);
 
 		if (text.charAt(curr.pos)!='(')
 			return null;
 		++curr.pos;
 		
 		ArrayList<String> result = new ArrayList<String>();
 		
 		boolean stop = false;
 		do {
 			proceedToToken(text, curr);
 			if (text.charAt(curr.pos)!=')') {
 				String arg = getParam(text, curr);
 				result.add(arg);
 				proceedToToken(text, curr);
 			}
 			if (text.charAt(curr.pos)==',')
 				++curr.pos;
 			else
 				stop = true;
 		}
 		while (!stop);
 
 		if (text.charAt(curr.pos)!=')')
 			return null;
 		
 		++curr.pos;
 		
 		return result;
 	}
 
 	private String getToken(String text, Position curr) {
 		int begin = curr.pos;
 		while (curr.pos<text.length() && isTokenChar(text.charAt(curr.pos)))
 			++curr.pos;
 		String token = text.substring(begin, curr.pos);
 		return token;
 	}
 
 	private String getParam(String text, Position curr) {
 		int begin = curr.pos;
 		int parenthesisLevel = 0;
 		while (curr.pos<text.length()) {
 			if (text.charAt(curr.pos)=='(')
 				++parenthesisLevel;
 			else if (text.charAt(curr.pos)==')') {
 				if (parenthesisLevel==0)
 					break;
 				else
 					--parenthesisLevel;
 			}
 			else if (parenthesisLevel==0) {
 				if (text.charAt(curr.pos)==',')
 					break;
 			}
 			++curr.pos;
 		}
 		String token = text.substring(begin, curr.pos).trim();
 		return token;
 	}
 
 	private void skipWhiteSpace(String text, Position curr) {
 		while (curr.pos<text.length() && Character.isWhitespace(text.charAt(curr.pos)))
 			++curr.pos;
 	}
 
 	private void skipMultiComment(String text, Position curr) {
 		curr.pos += 2;
 		while (curr.pos<text.length()-1 && text.charAt(curr.pos++)!='*')
 			if (text.charAt(curr.pos)=='/')
 				break;
 		if (curr.pos<text.length())
 			++curr.pos;
 	}
 
 	private void skipSingleComment(String text, Position curr) {
 		while (curr.pos<text.length() && text.charAt(curr.pos)!='\n')
 			++curr.pos;
 		if (curr.pos<text.length())
 			++curr.pos;
 	}
 
 	private void skipString(String text, Position curr) {
 		while (++curr.pos<text.length() && text.charAt(curr.pos)!='"')
 			if (text.charAt(curr.pos)=='\\')
 				++curr.pos;
 	}
 
 	private boolean isTokenChar(char c) {
 		return Character.isDigit(c) || Character.isLetter(c) || c=='_';
 	}
 
 	private void prepare() {
 		name2item = new HashMap<String, InterfaceItem>();
 		List<InterfaceItem> items = RoomHelpers.getAllInterfaceItems(ac);
 		for (InterfaceItem item : items) {
 			name2item.put(item.getName(), item);
 		}
 
 		name2attr = new HashMap<String, Attribute>();
 		List<Attribute> attributes = RoomHelpers.getAllAttributes(ac);
 		for (Attribute attribute : attributes) {
 			name2attr.put(attribute.getName(), attribute);
 		}
 		
 		name2op = new HashMap<String, Operation>();
 		List<Operation> operations = RoomHelpers.getAllOperations(ac);
 		for (Operation operation : operations) {
 			name2op.put(operation.getName(), operation);
 		}
 	}
 }
