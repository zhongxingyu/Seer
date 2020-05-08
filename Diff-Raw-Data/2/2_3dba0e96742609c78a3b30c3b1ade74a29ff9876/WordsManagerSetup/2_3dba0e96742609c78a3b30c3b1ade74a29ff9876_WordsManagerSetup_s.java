 package fr.iutvalence.java.mp.thelasttyper.client.util;
 
 import java.io.IOException;
 import java.sql.SQLException;
 
 import fr.iutvalence.java.mp.thelasttyper.client.data.Constants;
 import fr.iutvalence.java.mp.thelasttyper.client.data.WordsManager;
 import fr.iutvalence.java.mp.thelasttyper.client.data.wordsmanager.ArrayListWordsManager;
 import fr.iutvalence.java.mp.thelasttyper.client.data.wordsmanager.TextfileWordsManager;
 
 /**
  * this class setup the wordsManager used in the main class.
  * @author Breci
  *
  */
 public class WordsManagerSetup
 {
     /** First static method which setup the wordsManager
      * @param url
      *              database url
      * @param user 
      *              user for the database
      * @param password 
      *              user's password for the database
      * @return the WordsManager used on the main class
      */
     public static WordsManager setup(String url,String user, String password)
     {
         try
         {
             //TODO connexion SQL
         }
         catch (SQLException e1)//TODO Exception SQL
         { //Si on a pas réussi à se co à la BDD
             try
             {
                 return new TextfileWordsManager(Constants.WORD_TEXTFILE);
             }
             catch(IOException e2)
             {//If we can't read the wordfile
              //do nothing, just let it go to the arrayList
             }
         }
        return new ArrayListWordsManager(); //ArrayList of random words
         
     }
 }
