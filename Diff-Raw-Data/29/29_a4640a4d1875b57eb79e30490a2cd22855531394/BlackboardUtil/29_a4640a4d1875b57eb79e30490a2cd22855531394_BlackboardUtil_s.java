 /**
  * Copyright 2010 David L. Whitehurst
  * 
  * Licensed under the Apache License, Version 2.0 
  * (the "License"); You may not use this file except 
  * in compliance with the License. You may obtain a 
  * copy of the License at http://www.apache.org/licenses/LICENSE-2.0
  *  
  * Unless required by applicable law or agreed to in writing, 
  * software distributed under the License is distributed on an 
  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, 
  * either express or implied. See the License for the specific 
  * language governing permissions and limitations under the 
  * License.
  * 
  */
 package org.dlw.ai.blackboard.util;
 
 import java.sql.Timestamp;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.List;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.dlw.ai.blackboard.Blackboard;
 import org.dlw.ai.blackboard.domain.Alphabet;
 import org.dlw.ai.blackboard.domain.Assertion;
 import org.dlw.ai.blackboard.domain.BlackboardObject;
 import org.dlw.ai.blackboard.domain.CipherLetter;
 import org.dlw.ai.blackboard.domain.Sentence;
 import org.dlw.ai.blackboard.domain.Word;
 
 /**
  * This class is used to provide utility functions for the blackboard object.
  * 
  * @author <a href="mailto:dlwhitehurst@gmail.com">David L. Whitehurst</a>
  * @version 1.0.0-RC
  */
 public final class BlackboardUtil {
 
     /**
      * Commons logging class instance
      */
     private static final Log log = LogFactory.getLog(BlackboardUtil.class);
 
     /**
      * Attribute class logger
      */
     private static Logger logger;
     
     /**
      * Public method to output all flat-hierarchy blackboard objects
      * 
      * @param blackboard
      */
     public static void outputSnapshot(Blackboard blackboard) {
 
         /**
          * Timestamp
          */
         Calendar calendar = Calendar.getInstance();
         java.util.Date now = calendar.getTime();
         Timestamp currentTimestamp = new java.sql.Timestamp(now.getTime());
         
         /**
          * Initialize logger
          */
         logger = Logger.getInstance();
         logger.wrap(log);
         logger.info("##############################################################################");
         logger.info("## BLACKBOARD SNAPSHOT " + currentTimestamp + " ###############################");
         logger.info("##############################################################################");
         
         for (BlackboardObject obj : blackboard) {
 
             if (obj.getClass().equals(
                     org.dlw.ai.blackboard.domain.Sentence.class)) {
                 Sentence sentence = (Sentence) obj;
                 logger.info("SENTENCE: " + sentence.value());
             }
             
             if (obj.getClass().equals(
                     org.dlw.ai.blackboard.domain.Word.class)) {
                 Word word = (Word) obj;
                 logger.info("WORD: " + word.value());
             }
             
             if (obj.getClass().equals(
                     org.dlw.ai.blackboard.domain.CipherLetter.class)) {
                 CipherLetter cipherLetter = (CipherLetter) obj;
                 logger.info("CIPHER: " + cipherLetter.value());
             }
             
             if (obj.getClass().equals(
                     org.dlw.ai.blackboard.domain.Alphabet.class)) {
                 Alphabet alphabet = (Alphabet) obj;
                 logger.info("ALPHABET: " + alphabet.getCipherLetter() + "=" + alphabet.getPlainLetter());
             }
 
             if (obj.getClass().equals(
                     org.dlw.ai.blackboard.domain.Assertion.class)) {
                 Assertion assertion = (Assertion) obj;
                 logger.info("ASSUMPTION: (retractable=" + assertion.isRetractable() + ") " + assertion.getReason());
             }
         
         }
         
         logger.info("##############################################################################");
         
     }
 
     /**
      * Public method to output the sentence and underline any corrected cipher
      * letters. Also any letters that are underlined are plaintext letters
      * (Alphabets) and also Affirmations exist in blackboard problem domain.
      */
     public static void outputProgress(Blackboard blackboard) {
 
         /**
          * Timestamp
          */
         Calendar calendar = Calendar.getInstance();
         java.util.Date now = calendar.getTime();
         Timestamp currentTimestamp = new java.sql.Timestamp(now.getTime());
         
         /**
          * Initialize logger
          */
         logger = Logger.getInstance();
         logger.wrap(log);
 
         Sentence sentence = null;
         
         for (BlackboardObject obj : blackboard) {
             if (obj.getClass().equals(
                     org.dlw.ai.blackboard.domain.Sentence.class)) {
                 sentence = (Sentence) obj;
             }
         }
 
         logger.info("##############################################################################");
         logger.info("## BLACKBOARD STATUS " + currentTimestamp + " #################################");
         logger.info("##############################################################################");
         
         logger.info("ORIGINAL: " + sentence.value());
 
         String markers = getAffirmationMarkings(sentence);
 
         /**
          * Use this to show that no markers exist
          */
         if (markers.equals("")) {
             markers = "___________________________";
         }
 
         logger.info("PROGRESS: " + markers);
         logger.info("##############################################################################");
 
     }
     
 
     /**
      * Private method to get affirmation markings for console output line below
      * working sentence
      * 
      * @param sentence
     * @return
      */
     private static String getAffirmationMarkings(Sentence sentence) {
 
         String markerLine = "";
         int wordcount = 0;
         int loopcount = 0;
 
         List<Word> words = sentence.getWords();
         wordcount = words.size();
 
         for (Word word : words) {
 
             List<CipherLetter> list = word.getLetters();
 
             for (CipherLetter letter : list) {
                 if (letter.getAffirmation().getSolvedLetter() != null) {
                         // affirmation and we have an assertion
                         markerLine = markerLine.concat(letter.getAffirmation().getSolvedLetter().getPlainLetter()); // was underscore
                     } else {
                         markerLine = markerLine.concat(letter.value()); // was space
                     }
             }
             if (loopcount < wordcount) {
                 markerLine = markerLine.concat(" ");
             }
             loopcount++;
 
         }
         return markerLine;
     }
 
     /**
      * Public method to get assertion statements
      * 
     * @param blackboard
     * @return
      */
     public static List<Assertion> getAssertionStatements(Blackboard blackboard) {
         ArrayList<Assertion> assertions = new ArrayList<Assertion>();
         for (BlackboardObject obj : blackboard) {
             if (obj.getClass().equals(
                     org.dlw.ai.blackboard.domain.Assertion.class)) {
                 Assertion assertion = (Assertion) obj;
                 if (!assertion.isRetractable()) {
                     assertions.add(assertion);
                 }
             }
         }
         
         return assertions;
     }
     
 }
