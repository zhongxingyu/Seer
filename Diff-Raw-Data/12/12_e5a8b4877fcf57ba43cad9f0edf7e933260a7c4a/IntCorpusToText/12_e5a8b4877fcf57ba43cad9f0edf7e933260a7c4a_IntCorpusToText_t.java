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
 package eu.monnetproject.translation.topics.experiments;
 
 import eu.monnetproject.translation.topics.CLIOpts;
 import eu.monnetproject.translation.topics.WordMap;
 import java.io.DataInputStream;
 import java.io.EOFException;
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.PrintStream;
 
 /**
  *
  * @author John McCrae
  */
 public class IntCorpusToText {
 
     enum Interleave {
         NO,
         FIRST,
         SECOND
     }
     
     public static void main(String[] args) throws Exception {
         final CLIOpts opts = new CLIOpts(args);
         
         final Interleave interleave = opts.enumOptional("i", Interleave.class, Interleave.NO, "Is the corpus interleaved: NO, FIRST, SECOND");
         
         final File wordMapFile = opts.roFile("wordMap[.gz|bz2]", "The map from words to integer IDs");
         
         final int W = opts.intValue("W", "The number of distinct tokens in the corpus");
 
         final File corpusFile = opts.roFile("corpusFile[.gz|bz2]", "The corpus");
 
         final PrintStream out = opts.outFileOrStdout();
 
         if(!opts.verify(IntCorpusToText.class)) {
             return;
         }
         
         final String[] invMap;
         System.err.println("Reading word map");
         invMap = WordMap.inverseFromFile(wordMapFile, W, true);
 
         final InputStream corpusIn = CLIOpts.openInputAsMaybeZipped(corpusFile);
 
         intCorpus2Text(invMap, corpusIn, out, interleave);
     }
 
     private static void intCorpus2Text(String[] invMap, InputStream corpusIn, PrintStream out, Interleave interleave) throws IOException {
         final DataInputStream data = new DataInputStream(corpusIn);
         boolean odd = true;
         while (data.available() > 0) {
             try {
                 int i = data.readInt();
                 if (i != 0) {
                     if((odd || interleave != Interleave.FIRST) &&
                             (!odd || interleave != Interleave.SECOND)) {
                         out.print(invMap[i]);
                         out.print(" ");
                     }
                 } else {
                     if((odd || interleave != Interleave.FIRST) &&
                             (!odd || interleave != Interleave.SECOND)) {
                         out.println();
                     }
                    odd = !odd;
                     //out.println();
                 }
             } catch(EOFException x) {
                 break;
             }
         }
         data.close();
     }
 }
