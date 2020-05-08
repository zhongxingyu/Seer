 package edu.victone.scrabblah.logic.game;
 
 import java.util.HashMap;
 
 /**
  * Created with IntelliJ IDEA.
  * User: vwilson
  * Date: 10/8/13
  * Time: 3:29 AM
  */
 public class SubstringDB {
     Character node;
     HashMap<Character, SubstringTree> trees;
 
     public SubstringDB() {
         trees = new HashMap<Character, SubstringTree>(35, .75f);
 
         for (int i = 65; i < 91; i++) {
             trees.put(new Character((char) i), new SubstringTree());
 
         }
 
     }
 
     public void add(String string) {
 
     }
 
     public void add(StringBuilder stringBuilder) {
 
     }
 
     public boolean contains(String s) {
 
         return false;
     }
 
     public boolean contains(StringBuilder sb) {

     }
 }
