 import java.io.*;
 import java.util.*;
 import javax.swing.*;
 import java.awt.*;
 import java.awt.event.*;
 import java.awt.BorderLayout;
 
 /** englishToPigLatin function made for Ticket:570
  @author Christian Rivera Cruz and Adam Kazberuk
  @version 05/12/2012 for lab05, cs56, S12
 */
 
 
 
 public class EnglishToPigLatin{
     /** The method that checks to see if the first character passed
      * to it is a vowel or a consonant
      * @param input the first letter of a word
      * @return result which is true when the first letter is a vowel
      */
 
 
     public static boolean isVowel(char input)
     {
 	boolean result=false;
 	    if(input=='a'||input=='A'||input=='e'||input=='E'
 	       ||input=='i'||input=='I'||input=='o'
 	       ||input=='O'||input=='u'||input=='U'){
 		result=true;
 	    }
 	return result;
     }
     
     /**
      * The method that converts the string from english to pig latin
      *
      * The method takes a string input, and checks the first letter in the string
      * If it is a vowel, then "way" is added to the end, and if it is a consonant
      * the first consonant is moved to the end of the word, and "ay" is added
      * TODO: fix it so that instead of just being the first character, the function moves
      * every consonant until the first vowel
      *
      * @param  input the input is the string typed into the box
      * @return String is returned, and it is reprinted into the text box below the input
      */
     
     public static String toPigLatin(String input){
 	
 	
 	String result="";
 	String toAdd="";
 	String words="";
 	int i=0;
 	char chr;
 	
 	
 	if(input.equals("")){
 	    return result;
 	}
 	
 	StringTokenizer token = new StringTokenizer(input);
 	while (token.hasMoreTokens()){
 	    words = token.nextToken();	    
 	    words.toLowerCase();
 	    char[] chararray = words.toCharArray();
 	    //the part that enforces the rules of pig latin
 	    if(isVowel(chararray[0])){
 		result+=words+"way ";
 	    }
 	    //else adapted from code found at http://technotip.com/216/position-left-most-vowel-javascript/
 	    else{
 		i=posFirstVowel(words);
 		for(int j=i; j<chararray.length; j++){
 		    result+=chararray[j];
 		}
 		for(int j=0; j<i; j++){
 		    result+=chararray[j];
 		}
 		result+="ay ";
 	    }
 	}
 	result = result.toLowerCase();
 	result = result.trim();
 	
 	//The following was adapted from code found online at 
 	//http://stackoverflow.com/questions/3100526/changing-charsequence-first-letter-to-upper-case-in-android
 	if(Character.isUpperCase(input.charAt(0))){
 	    return Character.toUpperCase(result.charAt(0))
 		+ (result.length() > 1 ? result.substring(1) : "");
 	}
 	else
 	    return result;
     }
 
     /**
      * The method that converts the string from pig latin to english
      *
      * The method takes a string input, and checks the first letter in the string
      * If it is a vowel, then "way" is added to the end, and if it is a consonant
      * the first consonant is moved to the end of the word, and "ay" is added
      * TODO: fix it so that instead of just being the first character, the function moves
      * every consonant until the first vowel
      *
      * @param  input the input is the string typed into the box
      * @return String is returned, and it is reprinted into the text box below the input
      */
     public static String[] toEnglish(String input) {
 
 	// 1) detect if first letter was vowel or consonant
 	//  a) if vowel, remove the -way or -yay and return
 	//  b) if consonant cluster, remove the -ay and move the consonant cluster to the front, then return
 
 	ArrayList<String> list = new ArrayList<String>();
 	ArrayList<Character> c = new ArrayList<Character>();
 	//create an array list of characters to work with:
 	for (char letter : input.toCharArray()) { c.add(letter); }
 	int l = c.size(); //Usage: c[l-X] refers to the Xth to last char
 	//Assume the input is already sanitized for spaces/newlines
 	//Check that it's actually pig latin:
 	if (c.get(l - 1) != 'y') {
 	    System.out.println("Not pig latin: " + input);
 	    list.add(input);
 	    return listToStringArray(list);
 	}
 	c.remove(l-1);//remove 'y'
 	c.remove(l-2);//remove 'a'
 	l = c.size(); //update size
 	//check if the word could have started with a vowel
 	if (c.get(l-1) == 'w' || c.get(l-1) == 'y')   {
 	    list.add(listToString(c));
 	}
 	while (!isVowel(c.get(l-1))) {
 	    //move consonant to front of string
 	    c.add(0, c.remove(l-1));	 
 	    //add to the list
 	    list.add(listToString(c));
 	}
 	
 	//return the list as an array
 	return listToStringArray(list);
 	
     }
 
 private static String listToString(ArrayList<Character> list)
 {
     String result = "";
     for (int i = 0; i < list.size(); i++) {
 	result += list.get(i);
     }
     return result;
 }
 
 private static String[] listToStringArray(ArrayList<String> list)
 {
     return Arrays.copyOf(list.toArray(), list.size(), String[].class);
 }
     /**
        This function makes our GUI for toPigLatin work, setup and other processes are handled in windowSetUp
     */
     
     public static void go(){
 	JFrame f = new JFrame("English to Pig Latin Translator");
 	f.setSize(600,400);
 	Container content = f.getContentPane();
 	content.setBackground(Color.white);
 	content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS)); 
 	content.add(new WindowSetUp());
 	f.setBackground(new Color(0,255,0));
 	
 	f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 	
 	f.setVisible(true);
     }
     
     public static int posFirstVowel(String input){
 	int count=0;
 	int length=input.length();
 	char [] chararray;
 	chararray = new char[length];
 	while(count<input.length()){
 	    chararray[count]=input.charAt(count);
 	    count++;
 	}
 	int i=0;
 	char chr;
 	for(i = 0; i < chararray.length; i++){
 	    chr = chararray[i];
 	    if(chr == 'a' || chr == 'e' || chr == 'i' 
 	       || chr == 'o' || chr == 'u')
 		break;
 	}
 	return i;
     }
     
     public static void main(String [] args){
 	go();
     }
 }
