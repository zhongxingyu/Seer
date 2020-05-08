 package org.vpac.grisu.frontend.view.cli;
 
 import java.io.IOException;
 import java.util.Collection;
 
 import jline.ConsoleReader;
 
 import org.apache.commons.lang.StringUtils;
 
 import com.google.common.collect.ImmutableList;
 
 public class CliHelpers {
 	
 	private static ConsoleReader consoleReader;
 	
 	private static ConsoleReader getConsoleReader() {
 		if ( consoleReader == null ) {
 			try {
 				consoleReader = new ConsoleReader();
 			} catch (IOException e) {
 				throw new RuntimeException();
 			}
 		}
 		return consoleReader;
 	}
 	
 	public static String getUserChoice(Collection<String> collection, String nonSelectionText) {
 		return getUserChoice(collection, null, null, nonSelectionText);
 	}
 
 	public static String getUserChoice(Collection<String> collection, String prompt, String defaultValue, String nonSelectionText) {
 		
 		if ( StringUtils.isBlank(prompt) ) {
 			prompt = "Enter selection";
 		}
 
 		int defaultIndex = -1;
 		ImmutableList<String> list = ImmutableList.copyOf(collection.iterator());
 		for ( int i=0; i<list.size(); i++ ) {
 			System.out.println("["+(i+1)+"] "+list.get(i));
 			if ( list.get(i).equals(defaultValue) ) {
 				defaultIndex = i+1;
 			}
 		}
 		
 		if ( StringUtils.isNotBlank(nonSelectionText) ) {
 			System.out.println("\n\n[0] "+nonSelectionText);
 			prompt = prompt + "[0]: ";
 			defaultIndex = 0;
 		} else {
 			prompt = prompt + ": ";
 		}
 		
 		int choice = -1;
 		int startIndex = 0;
 		if ( StringUtils.isBlank(nonSelectionText) ) {
 			startIndex = 1;
 		}
		while ( (choice < startIndex) || (choice >= list.size()) ) {
 			String input;
 			try {
 				input = getConsoleReader().readLine(prompt);
 			} catch (IOException e) {
 				throw new RuntimeException(e);
 			}
 			
 			if ( StringUtils.isBlank(input) ) {
 				if ( (StringUtils.isNotBlank(nonSelectionText) && defaultIndex >= 0) 
 				     || (StringUtils.isBlank(nonSelectionText) && defaultIndex >= 1) ) {
 					choice = defaultIndex;
 				} else {
 					continue;
 				}
 			} else {
 				try {
 					choice = Integer.parseInt(input);
 				} catch ( Exception e) {
 					continue;
 				}
 			}
 		}
 		
 		if ( choice == 0 ) {
 			return null;
 		} else {
 			return list.get(choice-1);
 		}
 		
 		
 	}
 	
 }
