 package edu.victone.scrabblah.logic.game;
 
 import edu.victone.scrabblah.logic.game.concurrent.Producer;
 import edu.victone.scrabblah.logic.game.concurrent.SubstringConsumer;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.util.HashSet;
 import java.util.concurrent.LinkedBlockingQueue;
 
 /**
  * Created with IntelliJ IDEA.
  * User: vwilson
  * Date: 9/11/13
  * Time: 4:03 PM
  */
 
 public class Dictionary {
  private static HashSet<String> dictionary;
  private static PatriciaTrie substrings;
 
  public static void load(File dictionaryFile) throws FileNotFoundException {
     dictionary = new HashSet<>(360000);
     substrings = new PatriciaTrie();
 
     LinkedBlockingQueue<String> substringWorkPool = new LinkedBlockingQueue<>();
 
     Producer producer = new Producer(dictionaryFile, dictionary, substringWorkPool);
     SubstringConsumer substringConsumer = new SubstringConsumer(substringWorkPool, substrings);
 
     new Thread(producer).start();
     new Thread(substringConsumer).start();
   }
 
  public static boolean contains(String s) {
     return dictionary.contains(s.toUpperCase());
   }
 
  public static boolean containsSubstring(String s) {
     return substrings.contains(s);
   }

  public static boolean isLoaded() {
    return dictionary != null;
  }
 }
