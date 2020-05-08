 /**
  * Copyright (C) 2010 cbrew <cbrew@acm.org>
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *         http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package edu.osu.nlp.chartparser;
 
 import java.util.Set;
 
 /**
  * A bottom-up strategy for the parser.
  */
 public class BottomUpStrategy extends Strategy {
 

     BottomUpStrategy(final Chart ch) {
         super(ch);
     }
 
     /**
      * Initialization for bottom-up strategy.
      * @param words the sentence to be tagged
      * @param topCats the allowable root categories.
      */
     @Override
    public final void initialize(final String[] words, final Set<String> topCats) {
         int i = 0;
         for (String word : words) {
             getMyChart().getAgenda().add(Edges.lexical(word, i));
             i++;
         }
     }
 
     /**
      * the bottom-up strategy predicts rule invocations here.
      *
      * @param label the label of the complete edge
      * @param position the position at which the predictions
      *  should be made.
      */
     @Override
    public final void predictFromComplete(final String label, final int position) {
         for (Rule r : getMyChart().rules) {
             if (r.getRhs().get(0).equals(label)) {
                 getMyChart().getAgenda().add(Edges.empty(r, position));
             }
         }
     }
 
     /**
      * The bottom-up strategy doesn't make predictions from
      * partials, but it does pair them with the corresponding
      * completes.
      *
      * @param edge the edge from which predictions should be made
      */
     @Override
     public final void predictFromPartial(final Edge edge) {
         int right = edge.getRight();
         Set<Edge> cs = getMyChart().getCompletes().get(right);
         getMyChart().pairwithcompletes(edge, cs);
     }
 }
