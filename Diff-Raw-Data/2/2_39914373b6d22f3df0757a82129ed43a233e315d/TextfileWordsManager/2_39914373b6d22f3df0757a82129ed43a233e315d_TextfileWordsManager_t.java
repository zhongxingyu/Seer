 package fr.iutvalence.java.mp.thelasttyper.client.data.wordsmanager;
 
 import java.io.BufferedReader;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.UnsupportedEncodingException;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Random;
 
 import fr.iutvalence.java.mp.thelasttyper.client.data.Word;
 import fr.iutvalence.java.mp.thelasttyper.client.data.WordsManager;
 
 /**
  * This class generate a word's list from a text file
  * 
  * @author culasb
  * 
  */
 public class TextfileWordsManager implements WordsManager
 {
     /**
      * This is list of words.
      */
     private List<Word> words;
 
 
     /**
      * Initialized and created a list of Words with the content of the file with
      * the given name
      * 
      * @param name
      *            the file name
      * @throws IOException
      *             if file is not found or is corrupted
      */
     public TextfileWordsManager(String name) throws IOException
     {
         
         this.words = new ArrayList<Word>();
         // opens file for binary reading
         // byte <- (file)
 
         FileInputStream is = new FileInputStream(name);
 
         // reading ASCII chars
         // char <- ( byte <- (file) )
         InputStreamReader isr = null;
         try
         {
             isr = new InputStreamReader(is, "US-ASCII");
         }
         catch (UnsupportedEncodingException e)
         {
             // ignore exception because it can not occur
         }
 
         // reading text lines
         // String <- (char <- ( byte <- (file) ))
         BufferedReader br = new BufferedReader(isr);
 
         while (true)
         {
             String s = br.readLine(); // TODO stocker les mots dans une liste.
 
             if (s == null)
             {
                 // file has been read completely
                 br.close();
                 return;
             }
            this.words.add(new Word( Word.getValue(s),Word.getDifficulty(s), s));
         }
 
     }
 
     @Override
     public List<Word> getWordsList(int amount, int difficulty)
     {
         Random rand =new Random();
         int count = 0;
         List<Word> wordsclone = new ArrayList<Word>();
         List<Word> res = new ArrayList<Word>();
         wordsclone.addAll(this.words);
         
         int random = rand.nextInt(wordsclone.size());
         while (count < amount)
         {
             while ((random < 0 || random > wordsclone.size())&&
                     wordsclone.get(random).getDifficulty() != difficulty)
                 {
                     random = rand.nextInt(wordsclone.size());
                 }
 
             
             res.add(wordsclone.get(random));
             wordsclone.remove(random);
             count++;
             if (count!= amount)
             random = rand.nextInt(wordsclone.size()); //reset random
         }
         return res;
     }
 
 }
