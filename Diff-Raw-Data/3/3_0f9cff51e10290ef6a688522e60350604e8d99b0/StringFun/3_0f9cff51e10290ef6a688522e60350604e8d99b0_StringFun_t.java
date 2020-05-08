 package de.pinuts.helper;
 
 import java.lang.StringBuilder;
 import java.util.List;
 import java.util.ArrayList;
 import java.lang.Character;
 import java.lang.Math;
 
 
 public class StringFun {
 
     public static String reverse(String text){
 
         String reversedText = "";
         if(text != null) {
             reversedText = new StringBuilder(text).reverse().toString();
         }
        
         return reversedText;    
     }
 
     public static String shuffle(String text){
 
         String shuffledText = "";
 
         if(text != null) {
             List<Character> characters = new ArrayList<Character>();
             for(char c : text.toCharArray()){
                 characters.add(c);
             }
 
             StringBuilder output = new StringBuilder(text.length());
             while(characters.size()!=0){
                 int randPicker = (int)(Math.random()*characters.size());
                 output.append(characters.remove(randPicker));
             }
 
             shuffledText = output.toString();
         }
 
 
         return shuffledText;
     }
 
 }
