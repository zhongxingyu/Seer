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
 package eu.monnetproject.translation.langmodels;
 
 import java.util.Arrays;
 
 /**
  * Really just an int array, but has a proper hashCode() and equals()
  *
  * @author John McCrae
  */
 public class NGram implements Comparable<NGram> {
 
     public final int[] ngram;
 
     public NGram(int[] ngram) {
         this.ngram = ngram;
         assert ngramNonnegative(ngram);
     }
 
     @Override
     public int hashCode() {
         int hash = 5;
         hash = 61 * hash + Arrays.hashCode(this.ngram);
         return hash;
     }
 
     @Override
     public boolean equals(Object obj) {
         if (obj == null) {
             return false;
         }
         if (getClass() != obj.getClass()) {
             return false;
         }
         final NGram other = (NGram) obj;
         if (!Arrays.equals(this.ngram, other.ngram)) {
             return false;
         }
         return true;
     }
 
     @Override
     public String toString() {
         return "NGram" + Arrays.toString(ngram);
     }
     
     public NGram history() {
         return new NGram(Arrays.copyOfRange(ngram, 0, ngram.length-1));
     }
     
     public NGram future() {
         return new NGram(Arrays.copyOfRange(ngram, 1, ngram.length));
     }
 
     private boolean ngramNonnegative(int[] ngram) {
         for (int i : ngram) {
             if (i < 0) {
                 return false;
             }
         }
         return true;
     }
 
     @Override
     public int compareTo(NGram o) {
         if(o.ngram.length < this.ngram.length) {
             return -1;
         } else if(o.ngram.length > this.ngram.length) {
             return +1;
         } else {
             for(int i = 0; i < this.ngram.length; i++) {
                final int c = Integer.compare(this.ngram[i], o.ngram[i]);
                 if(c != 0) {
                     return c;
                 }
             }
             return 0;
         }
     }
 }
