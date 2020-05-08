 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package wordbuilder;
 
 import database.DatabaseOperations;
 import edu.smu.tspell.wordnet.*;
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Scanner;
 
 /**
  *
  * @author chandra
  */
 public class ApiFetch {
 
     
 
     /**
      * Initialise instance for wordnet database search.
      */
     public static void initInstance() {
         if (!initiscalled) {
             database = WordNetDatabase.getFileInstance();
             initiscalled = true;
         }
 
     }
 
     /**
      *
      * @param input Word for which three level Hypernyms are to be obtained
      * @return Arraylist contains all three level Hypernyms and words similar to word 
      * searched. It contains data in following order if present: Similar words, NoundHypernym Level 1,2,3
      * words and Verb Hypernym 1,2,3 words.
      */
     public static ArrayList<String> getHypernym(String input) {
         initInstance();
         ArrayList<String> al2 = new ArrayList();
         Synset [] synadj = database.getSynsets(input, SynsetType.ADJECTIVE, false);
         if(synadj.length >0)
         {
         
         AdjectiveSynset synadj1 = (AdjectiveSynset) synadj[0];
         AdjectiveSynset [] synadjarr = synadj1.getSimilar();
         if(synadjarr.length > 0)
         {
             for(int j=0;j<synadjarr.length;j++)
             {
                 
             
              String[] simwords = synadjarr[j].getWordForms();
                 if (simwords.length > 0) // if there is noun hypernym of the given word present 
                 {
 
                 ///    al2.add(String.valueOf(hypernymwordsL1.length));
                     for (int i = 0; i < simwords.length; i++) {
                         al2.add(simwords[i]);
                     }
 
                 }
         }
         }
         
     }
         
         Synset[] synset = database.getSynsets(input, SynsetType.NOUN,false);
         if (synset.length > 0) {
             
             //al2.add("N:");
             NounSynset ns1 = (NounSynset) synset[0];                         // Top most synset in noun synsets.
             NounSynset[] nsL1 = ns1.getHypernyms();
             if (nsL1.length > 0) {
                 String[] hypernymwordsL1 = nsL1[0].getWordForms();
                 if (hypernymwordsL1.length > 0) // if there is noun hypernym of the given word present 
                 {
 
                 ///    al2.add(String.valueOf(hypernymwordsL1.length));
                     for (int i = 0; i < hypernymwordsL1.length; i++) {
                         al2.add(hypernymwordsL1[i]);
                     }
 
                 }
                 NounSynset[] nsL2 = nsL1[0].getHypernyms();
                 if (nsL2.length > 0) {
                     String[] hypernymwordsL2 = nsL2[0].getWordForms();
                     if (hypernymwordsL2.length > 0) // if there is noun hypernym of the given word present 
                     {
 
                       //  al2.add(String.valueOf(hypernymwordsL2.length));
                         al2.addAll(Arrays.asList(hypernymwordsL2));
 
                     }
                     NounSynset[] nsL3 = nsL2[0].getHypernyms();
                     if (nsL3.length > 0) {
                         String[] hypernymwordsL3 = nsL3[0].getWordForms();
                         if (hypernymwordsL3.length > 0) // if there is noun hypernym of the given word present 
                         {
 
                        //     al2.add(String.valueOf(hypernymwordsL3.length));
                             al2.addAll(Arrays.asList(hypernymwordsL3));
 
                         }
                     }
 
                 }
 
             }
 
         }
        
             Synset[] synsetverb = database.getSynsets(input, SynsetType.VERB,false);
         if(synsetverb.length>0)
         {
             
           //  al2.add("V:");
               VerbSynset vs1 =  (VerbSynset) synsetverb[0];                         // Top most synset in verb synsets.
             VerbSynset[] vsL1 = vs1.getHypernyms();
             if (vsL1.length > 0) {
                 String[] hypernymwordsL1 = vsL1[0].getWordForms();
                 if (hypernymwordsL1.length > 0) // if there is verb hypernym of the given word present 
                 {
 
                   //  al2.add(String.valueOf(hypernymwordsL1.length));
                     for (int i = 0; i < hypernymwordsL1.length; i++) {
                         al2.add(hypernymwordsL1[i]);
                     }
 
                 }
                 VerbSynset[] vsL2 = vsL1[0].getHypernyms();
                 if (vsL2.length > 0) {
                     String[] hypernymwordsL2 = vsL2[0].getWordForms();
                     if (hypernymwordsL2.length > 0) // if there is verb hypernym of the given word present 
                     {
 
                   //      al2.add(String.valueOf(hypernymwordsL2.length));
                         al2.addAll(Arrays.asList(hypernymwordsL2));
 
                     }
                     VerbSynset[] vsL3 = vsL2[0].getHypernyms();
                     if (vsL3.length > 0) {
                         String[] hypernymwordsL3 = vsL3[0].getWordForms();
                         if (hypernymwordsL3.length > 0) // if there is noun hypernym of the given word present 
                         {
 
                       //      al2.add(String.valueOf(hypernymwordsL3.length));
                             al2.addAll(Arrays.asList(hypernymwordsL3));
 
                         }
                     }
 
                 }
         }
         }
         
         
         return al2;
     }
 
     /**
      *
      * @param uid The user who has searched the word
      * @param word The word to be searched
      * @param why Purpose of searching the word viz, "q" or "s"
      * @return String Arraylist will contain meaning of the word and all the
      * wordforms present in the synset.
      */
     public static ArrayList<String> getMeaning(String uid, String word, String why) throws SQLException, ClassNotFoundException {
         initInstance();
         ArrayList<String> al1 = new ArrayList<>();
         Synset[] synset = database.getSynsets(word,null,false);
        
         int index = 0;
         if (synset.length > 0) {
             index = ApiFetch.getMaxFrequency(word);
             al1.add(synset[index].getDefinition());
             String[] wordForms = synset[index].getWordForms();
             for (int j = 0; j < wordForms.length; j++) {
                 al1.add(wordForms[j]);
             }
             
            if (why.equals("s") && (uid!=null))                                        
              {
              ArrayList<String> senddata = new ArrayList();
              senddata.add(uid);
              senddata.add(word);
              senddata.add(String.valueOf(synset[ApiFetch.getMaxFrequency(word)].getTagCount(word)));
              DatabaseOperations.updateHasSearched(senddata);
              }
         }
       
         return al1;
 
     }
     
     /**
      * 
      * @param inputword Word for which Max Frequency index is to be obtained
      * @return Integer representing the index of pertaining to Maximum Frequency.
      */
     public static int getMaxFrequency(String inputword)
     {
         initInstance();
        
         Synset[] synset = database.getSynsets(inputword,null,false);
         
         
         int[] frequency = new int[synset.length];
         int max = -1;
         int index = 0;
         int Maxindex =-1;
         
        
         if (synset.length > 0) {
             
             for (int i = 0; i < synset.length; i++) {
 
                 frequency[i] = synset[i].getTagCount(inputword);
                 
 
             }
             for (int j = 0; j < synset.length; j++) {
                 if (frequency[j] > max) {
                     index = j;
                     max = frequency[j];
                 }
 
             }
         }
         Maxindex =index;
         return Maxindex;
     }
 
     /**
      * 
      * @param input Word for which three level hyponyms are to be obtained
      * @return Arraylist contains all three level Hyponyms. First entry
      *         contains number of wordforms in Level1 Hyponym and corresponding 
      *         wordforms are followed, same thing is repeated for Level 2 and Level 3
      */
     public static ArrayList<String> getHyponym(String input) {
         initInstance();
         ArrayList<String> al3 = new ArrayList();
         Synset[] synset = database.getSynsets(input, SynsetType.NOUN,false);
         if (synset.length > 0) {
             NounSynset ns1 = (NounSynset) synset[0];                         // Top most synset in noun synsets.
             NounSynset[] nsL1 = ns1.getHyponyms();
             if (nsL1.length > 0) {
                 String[] hyponymwordsL1 = nsL1[0].getWordForms();
                 if (hyponymwordsL1.length > 0) // if there is noun hyponym of the given word present 
                 {
 
                 //    al3.add(String.valueOf(hyponymwordsL1.length));
                     for (int i = 0; i < hyponymwordsL1.length; i++) {
                         al3.add(hyponymwordsL1[i]);
                     }
 
                 }
                 NounSynset[] nsL2 = nsL1[0].getHyponyms();
                 if (nsL2.length > 0) {
                     String[] hyponymwordsL2 = nsL2[0].getWordForms();
                     if (hyponymwordsL2.length > 0) // if there is noun hyponym of the given word present 
                     {
 
                      //   al3.add(String.valueOf(hyponymwordsL2.length));
                         al3.addAll(Arrays.asList(hyponymwordsL2));
 
                     }
                     NounSynset[] nsL3 = nsL2[0].getHyponyms();
                     if (nsL3.length > 0) {
                         String[] hyponymwordsL3 = nsL3[0].getWordForms();
                         if (hyponymwordsL3.length > 0) // if there is noun hyponym of the given word present 
                         {
 
                           //  al3.add(String.valueOf(hyponymwordsL3.length));
                             al3.addAll(Arrays.asList(hyponymwordsL3));
 
                         }
                     }
 
                 }
 
             }
 
         }
         
         return al3;
     }
     private static WordNetDatabase database;
     private static boolean initiscalled = false;
 }
