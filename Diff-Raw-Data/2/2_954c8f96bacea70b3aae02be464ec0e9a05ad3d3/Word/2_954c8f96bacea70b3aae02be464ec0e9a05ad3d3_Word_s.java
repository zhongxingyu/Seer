 package edu.gwu.raminfar.iauthor.core;
 
 import java.util.regex.Pattern;
 
 /**
  * @author Amir Raminfar
  */
 public class Word implements Comparable<Word> {
    private static Pattern NON_ALPHA = Pattern.compile("[^a-z\\- ]");
 
     public enum Type {
         NOUN, PRONOUN, VERB, MODAL, ADJECTIVE, ADJECTIVE_SATELLITE, PARTICLE,
         ADVERB, CONJUNCTION, NUMBER, PREPOSITION, DETERMINER, TO, UNKNOWN
     }
 
     private String word;
     private Type type;
 
     public Word(String word, Type type) {
         this.word = NON_ALPHA.matcher(word.toLowerCase()).replaceAll("");
         this.type = type;
     }
 
     @Override
     public String toString() {
         return getText();
     }
 
     public Type getType() {
         return type;
     }
 
     public String getText() {
         return word;
     }
 
     @Override
     public boolean equals(Object o) {
         if (this == o) return true;
         if (o == null || getClass() != o.getClass()) return false;
         Word word1 = (Word) o;
         return !(type != word1.type || (word != null ? !word.equals(word1.word) : word1.word != null));
     }
 
     @Override
     public int hashCode() {
         int result = word != null ? word.hashCode() : 0;
         result = 31 * result + (type != null ? type.hashCode() : 0);
         return result;
     }
 
     @Override
     public int compareTo(Word o) {
         return word.compareTo(o.word);
     }
 }
