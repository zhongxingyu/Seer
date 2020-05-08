 /**
  * Project "com.quui.chat.core" (C) 2004 Fabian Steeg This library is free
  * software; you can redistribute it and/or modify it under the terms of the GNU
  * Lesser General Public License as published by the Free Software Foundation;
  * either version 2.1 of the License, or (at your option) any later version.
  * This library is distributed in the hope that it will be useful, but WITHOUT
  * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
  * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
  * details. You should have received a copy of the GNU Lesser General Public
  * License along with this library; if not, write to the Free Software
  * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
  */
 
 package com.quui.chat.mind;
 
 import java.io.Serializable;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Random;
 import java.util.Vector;
 
 import com.quui.chat.Log;
 
 /**
  * A topic consists of keys and answers.
  * @author Fabian Steeg (fsteeg)
  */
 public class Topic implements Serializable, Comparable<Topic> {
     private static final long serialVersionUID = 1L;
 
     private Map<String, Integer> answers;
 
     private double score = 0;
 
     private Vector<String> keys;
 
     private Random randomGen = new Random();
 
     /**
      * Flag to indicate if a word was selected by synonym, meaning it is close
      * to a user input.
      */
     private boolean wasPickedBySynonym = false;
 
     /**
      * @return Returns the wasPickedBySynonym.
      */
     public boolean isWasPickedBySynonym() {
         return wasPickedBySynonym;
     }
 
     private String name;
 
     /**
      * @return The name of the topic (The WordNet definition)
      */
     public String getName() {
         return name;
     }
 
     /**
      * @param name The name to set
      */
     public void setName(String name) {
         this.name = name;
     }
 
     public Topic(String name) {
         this.name = name;
         keys = new Vector<String>();
         answers = new HashMap<String, Integer>();
     }
 
     /**
      * For XML Serialization
      * @param name The name of the topic
      * @param keys The keys
      * @param answers The mapping of answers to frequencies
      */
     public Topic(String name, Vector<String> keys, Map<String, Integer> answers) {
         this.name = name;
         this.keys = keys;
         this.answers = answers;
     }
 
     /**
      * @return The mapping of answers to frequencies
      */
     public Map<String, Integer> getAnswers() {
         return answers;
     }
 
     /**
      * @param newKey The new key to add to this topic. complex keys are not
      *            added or only partially added
      */
     public void addKey(String newKey) {
         if (newKey.endsWith(")")) {
             newKey = newKey.substring(0, newKey.indexOf("("));
         }
         if (keys.contains(newKey)) {
             return;
         }
         String key = newKey.toLowerCase();
         Log.logger.debug("Adding key: '" + key + "' to topic: '" + name + "'");
         keys.add(key);
     }
 
     /**
      * Adds a string as an answer, removing "_", counting how often that answer
      * was told
      * @param answer The answer to add
      */
     public void addAnswer(String answer) {
         answer = answer.replaceAll("_", " ");
         Log.logger.debug("Adding answer: '" + answer + "' to topic: '" + name
                 + "'");
         if (!answers.containsKey(answer)) {
             answers.put(answer, Integer.valueOf(1));
         } else {
             // count up the frequency for the answer
             answers.put(answer, answers.get(answer).intValue() + 1);
         }
 
     }
 
     /**
      * @return A more or less random answer for this topic
      */
     public String getAnswer() {
         Log.logger.debug("[will get random answer for the topic: '" + name
                 + "]");
         /*
          * generate a vector with dublicate answers for answers with frequencies
          * higher than 1:
          */
         Vector<String> v = new Vector<String>();
         for (String answer : answers.keySet()) {
             int answerFreq = answers.get(answer).intValue();
             for (int i = 0; i < answerFreq; i++) {
                 v.add(answer);
             }
         }
         int numberOfAnswers = v.size();
         if (numberOfAnswers > 0) {
             int randomPos = randomGen.nextInt(numberOfAnswers);
             randomPos = randomGen.nextInt(numberOfAnswers);
             return v.elementAt(randomPos);
         }
         return null;
     }
 
     /**
      * {@inheritDoc}
      * @see java.lang.Comparable#compareTo(java.lang.Object)
      */
     public int compareTo(Topic o) {
         return this.name.compareTo(o.getName());
     }
 
     /**
      * {@inheritDoc}
      * @see java.lang.Object#hashCode()
      */
     @Override
     public int hashCode() {
         return this.name.hashCode();
     }
 
     /**
      * {@inheritDoc}
      * @see java.lang.Object#equals(java.lang.Object)
      */
     @Override
     public boolean equals(Object obj) {
        return obj instanceof Topic && ((Topic) obj).compareTo(this) == 0;
     }
 
     /**
      * @param keyToTest The key to test
      * @return Returns true if this topic contains the key
      */
     public boolean containsKey(String keyToTest) {
         return this.keys.contains(keyToTest);
     }
 
     /**
      * @param answerToTest The answer to test
      * @return True is this Topic contains the answer
      */
     public boolean containsAnswer(String answerToTest) {
         return answers.containsKey(answerToTest);
     }
 
     /**
      * @param maxScore The score to set
      */
     public void setScore(double maxScore) {
         this.score = maxScore;
 
     }
 
     /**
      * @return The score of this topic
      */
     public double getScore() {
         return this.score;
     }
 
     /**
      * @return The keys
      */
     public Vector<String> getKeys() {
         return keys;
     }
 
 }
