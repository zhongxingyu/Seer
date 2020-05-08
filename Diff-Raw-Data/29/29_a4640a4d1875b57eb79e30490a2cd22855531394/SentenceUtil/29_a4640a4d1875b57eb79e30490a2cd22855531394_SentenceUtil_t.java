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
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.StringTokenizer;
 
 import org.dlw.ai.blackboard.domain.CipherLetter;
 import org.dlw.ai.blackboard.domain.Sentence;
 import org.dlw.ai.blackboard.domain.Word;
 
 /**
  * This class is used to provide utility functions for sentence blackboard
  * objects.
  * 
  * @author <a href="mailto:dlwhitehurst@gmail.com">David L. Whitehurst</a>
  * @version 1.0.0-RC
  */
 public final class SentenceUtil {
 
     /**
      * Public method to replace all occurences of a cipher text letter with a
      * plain text equivalent.  This method does not affect blackboard objects. Only
      * the Sentence value (String) is changed.
      * 
      * @param sentence
      *      the Sentence blackboard object
      * @param cipher
      *      the cipher letter String to change
      * @param plain
      *      the plain text String replacement
     * @return updated sentence String
      */
     public static Sentence updateAllOccurrences(Sentence sentence,
             String cipher, String plain) {
         String tmp = sentence.value();
         tmp.replace(cipher, plain);
         sentence.setSentence(tmp);
         return sentence;
     }
 
     /**
      * 
      * @param sentence
      *      the Sentence object parent
      * @return
      *      a List of word blackboard objects
      */
     public static List<Word> getWords(final Sentence sentence) {
 
         StringTokenizer toker = new StringTokenizer(sentence.value());
         ArrayList<Word> words = new ArrayList<Word>();
 
         while (toker.hasMoreTokens()) {
             String tmpWord = toker.nextToken();
             Word word = new Word(tmpWord);
             words.add(word);
         }
 
         return words;
     }
 
     /**
      * Public method to return children cipher letters from word blackboard objects
      * 
      * @param word
      *      the Word object parent
      * @return
      *      a List of cipher letter blackboard objects
      */
     public static List<CipherLetter> getLetters(final Word word) {
 
         ArrayList<CipherLetter> letters = new ArrayList<CipherLetter>();
 
         int i = 0;
 
         for (i = 0; i < word.value().length(); i++) {
             CipherLetter letter = new CipherLetter(word.value().substring(i,
                     i + 1));
             letters.add(letter);
 
         }
 
         return letters;
     }
 
 }
