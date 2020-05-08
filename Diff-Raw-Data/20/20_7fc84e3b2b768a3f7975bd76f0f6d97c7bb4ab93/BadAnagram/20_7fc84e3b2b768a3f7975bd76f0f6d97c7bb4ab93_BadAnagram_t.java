 package topic6;
 
 import java.io.BufferedReader;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
 
 public class BadAnagram {
 String fileName;
     
    // the dictionary
    CopyOnWriteArrayList<String> wordsDictonary;
     //temp for all the combination of one words
     HashSet<String> wordsCombineList = new HashSet<String>();
     //store all the anagram
     HashSet<List<String>> sortedWords;
     
     public BadAnagram(String fileName){
         this.fileName = fileName;
        wordsDictonary = new CopyOnWriteArrayList<String>();
         sortedWords = new HashSet<List<String>>();        
     }
 
     public void loadDictionary() {
         BufferedReader br = null;
         try {
             br = new BufferedReader(new FileReader(fileName));
             String r = br.readLine();
             while (r != null) {
                 wordsDictonary.add(r);
                 r = br.readLine();
             }
         } catch (FileNotFoundException e) {
             e.printStackTrace();
         } catch (IOException e) {
             e.printStackTrace();
         } finally {
             try {
                 br.close();
             } catch (IOException e) {
                 e.printStackTrace();
             }
         }
     }
     
     public void buildMap() {
         if (wordsDictonary.isEmpty()) {
             return;
         }
         Iterator<String> it = wordsDictonary.iterator();
         while(it.hasNext()){
             createWordsCombineSet(it.next());
         }
     }
     
     private void WordsCombineSet(List<Character> datas, List<Character> target,
             int num) {
         if (target.size() == num) {
 //            System.out.println("datas: " + datas);
 //            System.out.println("target: " + target);
 //            System.out.println("num: " + num);
             wordsCombineList.add(stringListToString(target));
             return;
         }
         
         for (int i = 0; i < datas.size(); i++) {
             List<Character> newDatas = new ArrayList<Character>(datas);
             List<Character> newTarget = new ArrayList<Character>(target);
             newTarget.add(newDatas.get(i));
             newDatas.remove(i);
             WordsCombineSet(newDatas, newTarget, num);
         }
     }
 
     //find a possible anagrams for a words
     private void createWordsCombineSet(String s) {
         WordsCombineSet(stringToCharacterList(s), new ArrayList<Character>(), s.length());
         storeAnagram();
     }
     
     //filter the anagrams
     private void storeAnagram() {
         ArrayList<String> anagrams = new ArrayList<String>();
         for (String s : wordsCombineList) {
             if (wordsDictonary.contains(s)) {
                 anagrams.add(s);
             }
         }
         // incase search for more than twice when build the map;
        if (anagrams.size() < 1) {
            return;
         }
        sortedWords.add(anagrams);
         wordsCombineList.clear();
        for (String s : anagrams) {
            wordsDictonary.remove(s);
        }
     }
     
     private ArrayList<Character> stringToCharacterList(String word) {
         ArrayList<Character> charList = new ArrayList<Character>();
         char[] ch = word.toCharArray();
         for (int i = 0; i < ch.length; i++) {
             charList.add(ch[i]);
         }
         return charList;
     }
     
     public void sizeOfMap(){
         System.out.println("The size of Anagram is:" + sortedWords.size());
     }
 
     public void infoOfSingleWords() {
         System.out.println("The left size of dictionary is:" + wordsDictonary.size());
     }
     
     
     public String stringListToString(List<Character> target) {
         StringBuffer s = new StringBuffer();
         for (char t : target)
             s.append(t);
         return s.toString();
     }
 //  public String[] charListToStringList(ArrayList<Character> cl) {
 //  String[] so = new String[cl.size()];
 //  for (int i = 0; i < cl.size(); i++) {
 //      so[i] = String.valueOf(cl.get(i));
 //  }
 //  return so;
 //}
 
 
 }
