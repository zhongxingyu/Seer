 /*
  * Copyright (C) 2012 Fabian Hirschmann <fabian@hirschm.net>
  *
  * This program is free software; you can redistribute it and/or
  * modify it under the terms of the GNU General Public License
  * as published by the Free Software Foundation; either version 2
  * of the License, or (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program; if not, write to the Free Software
  * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
  */
 package com.github.fhirschmann.clozegen.lib.generator;
 
 import com.github.fhirschmann.clozegen.lib.multiset.MapMultiset;
import com.github.fhirschmann.clozegen.lib.multiset.ReadMultisets;
 import com.google.common.collect.Multiset;
 import java.io.IOException;
 import java.net.URL;
 
 /**
  * Represents a model for the preposition gap generator.
  *
  * @author Fabian Hirschmann <fabian@hirschm.net>
  */
 public class PrepositionGapGeneratorModel {
     /** A multiset of trigrams (A, p, B). */
     private Multiset<String> trigrams;
 
     /** A multiset of bigrams (A, p). */
     private MapMultiset<String, String> after;
 
     /** A multiset of bigrams (p, B). */
     private MapMultiset<String, String> before;
 
     /**
      * Loads the model from three files. Please see
      * {@link MultisetReader} for details on the file format.
      *
      * @param trigrams the URL to the trigrams (A, p, B)
      * @param after the URL to the bigrams (A, p)
      * @param before the URL to the bigrams (p, B)
      * @throws IOException on errors reading a file
      */
     public void load(final URL trigrams, final URL after, final URL before)
             throws IOException {
        this.trigrams = ReadMultisets.parseMultiset(trigrams);
        this.after = ReadMultisets.parseMapMultiset(after, 0);
        this.before = ReadMultisets.parseMapMultiset(before, 1);
     }
 
     /**
      * @return the trigrams (A, p, B)
      */
     public Multiset<String> getTrigrams() {
         return trigrams;
     }
 
     /**
      * @param trigrams the trigrams (A, p, B) to set
      */
     public void setTrigrams(final Multiset<String> trigrams) {
         this.trigrams = trigrams;
     }
 
     /**
      * @return the bigrams (A, p)
      */
     public MapMultiset<String, String> getAfter() {
         return after;
     }
 
     /**
      * @param after the bigrams (A, p) to set
      */
     public void setAfter(final MapMultiset<String, String> after) {
         this.after = after;
     }
 
     /**
      * @return the bigrams (p, B)
      */
     public MapMultiset<String, String> getBefore() {
         return before;
     }
 
     /**
      * @param before the bigrams (p, B) to set
      */
     public void setBefore(final MapMultiset<String, String> before) {
         this.before = before;
     }
 
     @Override
     public String toString() {
         // TODO
         return super.toString();
     }
 }
