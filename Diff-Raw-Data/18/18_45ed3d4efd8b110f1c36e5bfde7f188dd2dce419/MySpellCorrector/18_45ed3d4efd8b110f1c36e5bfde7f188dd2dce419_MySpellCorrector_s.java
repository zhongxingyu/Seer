 package spell;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.HashSet;
 import java.util.Scanner;
 import java.util.Set;
 
 public class MySpellCorrector implements SpellCorrector {
 
     private MyTrie tri = new MyTrie();
     
     public MySpellCorrector() {
         // create trie
         tri = new MyTrie();
     }
     
     @Override
     public void useDictionary(String dictionaryFileName) throws IOException {
        
         Scanner sc = new Scanner(new File(dictionaryFileName));
         while(sc.hasNext()){
             tri.add(sc.next());
         }
 //            System.out.println(tri.toString());
         sc.close();
 
     }
 
     @Override
     public String suggestSimilarWord(String inputWord) throws NoSimilarWordFoundException {
         if(inputWord == ""){
             return null;
         }
         if(tri.find(inputWord) != null){
             return inputWord;
         }
 
         Set<String> words = oneEditDistance(inputWord);
         String suggest = checkAlteredWords(words);
         if(suggest != null){
             return suggest;
         }
         
         for (String word : words) {
             for(String newWord : oneEditDistance(word)){
                words.add(newWord);
             }
//            words = oneEditDistance(word);
         }
  
 
        System.out.println(words.toString());
        suggest = checkAlteredWords(words);
         if(suggest != null){
             return suggest;
         }
 
         throw new NoSimilarWordFoundException();
     }
     
     public String checkAlteredWords(Set<String> words) {
         String suggest = null;
         int currentCount = 0;
         
         MyTrie.MyNode node = null;
         for (String str : words) {
             node = tri.find(str);
             if(node != null){
                 if(node.getCount() > currentCount) {
                     suggest = str;
                     currentCount = node.getCount();
                 }
                 else if(node.getCount() == currentCount && suggest.compareTo(str) > 0) {
                     suggest = str;
                     currentCount = node.getCount();
                 }
             }
         }
         return suggest;
     }
     
     public Set<String> oneEditDistance(String inputWord) {
 
         Set<String> words = new HashSet<String>();
 
         for (String str : deletions(inputWord)){
             words.add(str);
         }
         for (String str : transpositions(inputWord)){
             words.add(str);
         }			
         for (String str : alterations(inputWord)){
             words.add(str);
         }
         for (String str : insertions(inputWord)){
             words.add(str);
         }			
 
         return words;
     }
 
     public  Set<String> oneEditDistance(Set<String> origWords) {
         Set<String> words = new HashSet<String>();
 
         for (String word : origWords) {
             if (word.equals("")){
                 continue;
             }
             for (String s : oneEditDistance(word)) {
                 words.add(s);
             }
         }
 
         return words;
     }
         
     public String[] deletions(String inputWord) {
         String[] words = new String[inputWord.length()];
         for (int i = 0; i < words.length; i++) {
             words[i] = inputWord.substring(0, i) + inputWord.substring(i+1);
//            System.out.println(inputWord.substring(0, i));
//            System.out.println(inputWord.substring(i+1));
//            System.out.println("New Word");
         }
         return words;
     }
 
     public String[] transpositions(String inputWord) {
 //            System.out.println(inputWord.length());
         if(inputWord.length() > 0){
             
             String[] words = new String[inputWord.length() - 1];
             for (int i = 0; i < words.length; i++) {
                 words[i] = inputWord.substring(0, i) + inputWord.charAt(i+1) + inputWord.charAt(i) + inputWord.substring(i+2);
             }
             return words;
         }
         else {
             return new String [0];
         }
     }
 
     public String[] alterations(String inputWord) {
         String[] words = new String[inputWord.length() * 25];
         for (int i = 0; i < inputWord.length(); i++) {
             String currentLetter = Character.toString(inputWord.charAt(i));
             int j = 0;
             for (letter l : letter.values()) {
                 String c = l.toString().toLowerCase();
                 if (currentLetter.equalsIgnoreCase(c)){
                     continue;
                 }
                 words[i * 25 + j++] = inputWord.substring(0, i) + c + inputWord.substring(i+1);
             }
         }
         return words;
     }
 
     public String[] insertions(String inputWord) {
         String[] words = new String[(inputWord.length() + 1) * 26];
 //        System.out.println(inputWord.length());
         for (int i = 0; i <= inputWord.length(); i++) {
             int j = 0;
             for (letter l : letter.values()) {
                 String c = l.toString().toLowerCase();
                 words[i * 26 + j++] = inputWord.substring(0, i) + c + inputWord.substring(i);
             }
         }
         return words;
     }
 }
