 package com.github.tteofili.nlputils;
 
 import java.util.Collection;
 
 /**
  * A language model calculate the probability <i>p</i> (between 0 and 1) of a
  * certain set of <code>T</code> objects, given a vocabulary.
  */
 public interface LanguageModel<T> {
 
    /**
     * Calculate the probability of a sentence given a vocabulary
     *
     * @param vocabulary a {@link Collection} of objects of type <code>T</code>
     * @param sample     the sample to evaluate the probability for
     * @return a <code>double</code> between <code>0</code> and <code>1</code>
     */
    public double calculateProbability(Collection<T> vocabulary, T sample);
 
 }
