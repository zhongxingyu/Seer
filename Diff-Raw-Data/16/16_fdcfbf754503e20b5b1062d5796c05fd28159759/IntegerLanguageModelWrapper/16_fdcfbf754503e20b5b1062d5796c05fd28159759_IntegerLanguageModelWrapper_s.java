 /**
  * *******************************************************************************
  * Copyright (c) 2011, Monnet Project All rights reserved.
  *
  * Redistribution and use in source and binary forms, with or without
  * modification, are permitted provided that the following conditions are met: *
  * Redistributions of source code must retain the above copyright notice, this
  * list of conditions and the following disclaimer. * Redistributions in binary
  * form must reproduce the above copyright notice, this list of conditions and
  * the following disclaimer in the documentation and/or other materials provided
  * with the distribution. * Neither the name of the Monnet Project nor the names
  * of its contributors may be used to endorse or promote products derived from
  * this software without specific prior written permission.
  *
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
  * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
  * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
  * ARE DISCLAIMED. IN NO EVENT SHALL THE MONNET PROJECT BE LIABLE FOR ANY
  * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
  * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
  * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
  * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
  * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
  * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  * *******************************************************************************
  */
 package eu.monnetproject.translation.fidel;
 
 import eu.monnetproject.translation.LanguageModel;
 import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
 import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
 import it.unimi.dsi.fastutil.objects.Object2IntMap;
 import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.ListIterator;
 
 /**
  *
  * @author John McCrae
  */
 public class IntegerLanguageModelWrapper implements IntegerLanguageModel {
 
     private final LanguageModel languageModel;
     private final Object2IntMap<String> wordMap = new WordMap();
     private final Int2ObjectMap<String> invWordMap = new Int2ObjectOpenHashMap<String>();
    private final Object wordMapLock = new Object();
     //private int W = 0;
 
     public IntegerLanguageModelWrapper(LanguageModel languageModel) {
         this.languageModel = languageModel;
     }
 
     @Override
     public double[] get(Phrase phrase) {
         final List<String> ls = new ArrayList<String>(phrase.p.length);
         for (int i = 0; i < phrase.n; i++) {
            synchronized(wordMapLock) {
                 ls.add(invWordMap.get(phrase.p[i + phrase.l]));
             }
         }
         final double[] score = new double[]{languageModel.score(ls)};
         if (Double.isInfinite(score[0]) || Double.isNaN(score[0])) {
             // Fallback case... try the lower-cased form
             final ListIterator<String> lsi = ls.listIterator();
             boolean differs = false;
             while (lsi.hasNext()) {
                 final String s = lsi.next();
                 if (s != null) {
                     final String sl = s.toLowerCase();
                     differs = (!s.equals(sl)) || differs;
                     lsi.set(sl);
                 }
             }
             if (differs) {
                 return new double[]{languageModel.score(ls)};
             } else {
                 return score;
             }
         } else {
             return score;
         }
     }
 
     @Override
     public int order() {
         return languageModel.getOrder();
     }
 
     @Override
     public Object2IntMap<String> wordMap() {
         return wordMap;
     }
 
     @Override
     public Int2ObjectMap<String> invWordMap() {
         return invWordMap;
     }
 
     private class WordMap extends Object2IntOpenHashMap<String> {
 
         int W = 0;
 
         @Override
         public boolean containsKey(Object k) {
             return true;
         }
 
         @Override
         public int getInt(Object k) {
            synchronized (wordMapLock) {
                 if (k instanceof String) {
                     if (super.containsKey((String) k)) {
                         return super.getInt(k);
                     } else {
                         super.put((String) k, W);
                         invWordMap.put(W, (String) k);
                         return W++;
                     }
                 } else {
                     throw new IllegalArgumentException();
                 }
             }
         }
 
         @Override
         public Integer get(Object ok) {
             return getInt(ok);
         }
     }
 }
