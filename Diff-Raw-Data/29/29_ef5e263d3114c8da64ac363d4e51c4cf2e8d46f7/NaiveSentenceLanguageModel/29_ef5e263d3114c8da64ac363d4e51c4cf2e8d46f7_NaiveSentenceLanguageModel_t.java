 package com.github.tteofili.nlputils;
 
 import java.util.Collection;
 import java.util.Collections;
 
 /**
  * Simple sentence language model which just counts the no. of occurrences of
  * a sentence over the no. of sentences in the vocabulary.
  */
 public class NaiveSentenceLanguageModel<T> implements LanguageModel<T[]> {
  @Override
  public double calculateProbability(Collection<T[]> vocabulary, T[] sentence) {
    return Collections.frequency(vocabulary, sentence) / vocabulary.size();
  }
 }
