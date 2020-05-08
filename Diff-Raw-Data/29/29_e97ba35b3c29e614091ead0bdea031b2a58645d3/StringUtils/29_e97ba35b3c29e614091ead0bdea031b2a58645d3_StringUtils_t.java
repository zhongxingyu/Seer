 package edu.grinnell.csc207.goldstei1.utils;
 
 public class StringUtils {
 
 
 	/**
 	 * Method to split a string at every instance of a certain
 	 *  character "splitter"
 	 * 
 	 */
 	public static String[] splitAt(String str, char splitter) {
 		//create new string
 		//loop through to count how many times a splitter appears in str
 		int count = 0;
 		for (int i = 0; i < str.length(); i++) {
 			if(str.charAt(i) == splitter) {
 				count++;
 			}//if
 		}//for
 
 		//The number of strings after splitting will be count + 1
 		String[] splitStrings = new String[count + 1];
 
 		//loop through strings
 		//fill temporary StringBuffer with characters until we reach splitter
 		//transfer stringBuffer to array
 		int arrayPosition = 0;
 		StringBuffer part = new StringBuffer();
 		for (int i = 0; i < str.length(); i++) {
 			if(str.charAt(i) == splitter) {
 				splitStrings[arrayPosition] = part.toString();
 				arrayPosition++;
 				part.delete(0, part.length());
 			}//if
 			else {
 				part.append(str.charAt(i));
 			}//else
 		}//for
 		splitStrings[arrayPosition] = part.toString();
 		return splitStrings;
 	}//splitAt
 
 	/**
 	 * Method to split a string at every instance of a character "splitter"
 	 * except when this character is meant to be part of the string.
 	 */
 	public static String[] splitCSV(String str, char splitter) {
 
 		//first, count the number of splitters so we can 
 		//make an appropriately sized array
 		int count = 0;
 
 		for (int i = 0; i < str.length(); i++) { //loop through the string
 			if(str.charAt(i) == '\"') {  
 				i++; 					//ignore splitters inside quotes
 				while(str.charAt(i) != '\"') { 
 					i++;
 				}
 			}	
 			if(str.charAt(i) == splitter) { //count splitters outside quotes
 				count++;
 			}//if
 		}//for
 
 		//The number of strings after splitting will be count + 1
 		String[] splitStrings = new String[count + 1];
 
 		//then loop through string again
 		//fill temporary StringBuffer with characters until we reach valid splitter
 		int arrayPosition = 0;
 		int inQuote = 0;
 		StringBuffer part = new StringBuffer();
 
 		for (int i = 0; i < str.length(); i++) {
 			if(str.charAt(i) == '\"') { //enter into a quotation, disregarding splitters
 				if(str.charAt(i + 1) == '\"') {
 					i++;
 					part.append(str.charAt(i));
 				}//if
 				else if(inQuote == 1) { //if in quote mode, go out of quote mode
 					inQuote = 0;
 				}//else if
 				else { 
 					inQuote = 1; //when not in quote mode, go into quote mode		
 				}//else	
 			}//if
 			else if(str.charAt(i) == splitter && inQuote == 0) {
 				splitStrings[arrayPosition] = part.toString();
 				arrayPosition++;
 				part.delete(0, part.length());
 			}//else if
 			else {
 				part.append(str.charAt(i));
 			}//else
 		}//for
 
 
 		splitStrings[arrayPosition] = part.toString();
 		return splitStrings;
 	}//splitCSV
 
 	public static String deLeet(String leet) {
 
 		StringBuffer editor = new StringBuffer(leet);
 		int i;
 
 		for(i = 0; i < editor.length(); i++) { //recurse through string
 			switch (editor.charAt(i)) {
 			case '+': editor.setCharAt(i, 't');
 			break;
 			case '3': editor.setCharAt(i, 'e');
 			break;
 			case '1': editor.setCharAt(i, 'l');
 			break;
 			case '0': editor.setCharAt(i, 'o');
 			break;
 			case '@': editor.setCharAt(i, 'a');
 			break;
 			case '$': editor.setCharAt(i, 's');
 			break;
 			case '(': editor.setCharAt(i, 'c');
 			break;
 			case '|': if(i + 2 < editor.length()) {
 				if(editor.substring(i, i+3).toString().equals("|\\|")) {
 					editor.replace(i, i+3, "n");
 					break;
 				}//if(editor.substring...)
 				if(editor.substring(i, i+3).toString().equals("|_|")) {
 					editor.replace(i,  i+3, "u");
 					break;
 				}//if(editor.substring...)
 			}//if(i+2....)
 
 
 			if(i + 1 < editor.length()) {
 				if(editor.substring(i, i+2).toString().equals("|3")) {
 					editor.replace(i,  i+2, "b");
 					break;
 				}//if(editor.substring...)
 				if(editor.substring(i, i+2).toString().equals("|=")) {
 					editor.replace(i,  i+2, "f");
 					break;
 				}//if(editor.substring...)
 			}//if(i+1.....)
 			else {
 				break;
 			}//else
 			default: break;		 
 
 			}//switch
 		}//for
 		return editor.toString();
 	}//deLeet
 
 	public static String nameGame(String name){
 		//list of vowels
 		char[] vowels = {'a', 'e', 'i', 'o', 'u', 'y'};
 		StringBuffer nameStub = new StringBuffer(name);
		String ns = "";
 		int v;
 		
		nameStub.setCharAt(0, Character.toLowerCase(nameStub.charAt(0)));
		
 		//remove first letter(s) of name until vowel
 		for (int i = 0; i < nameStub.length(); i++){
 			for(v = 0; v < vowels.length; v++){
 				if (nameStub.charAt(i) == vowels[v]){
					ns = nameStub.substring(i, nameStub.length()).toString();
 					break;
 				}//if
 			}//for v
			if(ns.length() > 0){
 				break;
 			}//if
 		}//for i
 
 		String line1 = name + "!\n";
		String line2 = name + ", " + name + " bo" + " B" + ns + " Bonana fanna fo F" + ns +"\n";
		String line3 = "Fee fy mo M" + ns + ", " + name + "!";
 
 		return(line1+line2+line3);
 
 	}//nameGame
 }//StringUtils
 
 
 
