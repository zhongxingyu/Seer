 package fr.iutvalence.java.mp.thelasttyper.client.data.wordsmanager;
 
 import java.io.BufferedReader;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.UnsupportedEncodingException;
 import java.util.List;
 
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
             this.words.add(new Word(Word.getDifficulty(s), Word.getValue(s), s));
 
             if (s == null)
             {
                 // file has been read completely
                 br.close();
                 return;
             }
         }
 
     }
 
     @Override
     public List<Word> getWordsList(int amount, int difficulty)
     {
        // TODO Auto-generated method stub
        return this.words;
     }
 
 }
