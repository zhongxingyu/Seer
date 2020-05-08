 /*********************************************************************************
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
 import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
 import it.unimi.dsi.fastutil.ints.IntSet;
 import java.io.DataInputStream;
 import java.io.DataOutputStream;
 import java.io.EOFException;
 import java.io.File;
 
 /**
  *
  * @author John McCrae
  */
 public class FilterWordMap {
 
     public static void main(String[] args) throws Exception {
         final CLIOpts opts = new CLIOpts(args);
         final File corpusFile = opts.roFile("corpus[.gz|.bz2]", "The corpus to reduce the word map to");
         final File wordMapInFile = opts.roFile("wordMap", "The full word map to reduce");
         final File wordMapOutFile = opts.woFile("out", "The file to store the reduced wordMap");
         
        if(!opts.verify(FilterWordMap.class)) {
            return;
        }
        
         IntSet inCorpus = new IntOpenHashSet();
         
         final DataInputStream dis = new DataInputStream(CLIOpts.openInputAsMaybeZipped(corpusFile));
         
         while(dis.available() > 0) {
             try {
                 int i = dis.readInt();
                 inCorpus.add(i);
             } catch(EOFException x) {
                 break;
             }
         }
         
         dis.close();
         
         final DataInputStream in = new DataInputStream(CLIOpts.openInputAsMaybeZipped(wordMapInFile));
         final DataOutputStream out = new DataOutputStream(CLIOpts.openOutputAsMaybeZipped(wordMapOutFile));
         
         while(in.available() > 0) {
             try {
                 final String key = in.readUTF();
                 final int i = in.readInt();
                 if(inCorpus.contains(i)) {
                     out.writeUTF(key);
                     out.writeInt(i);
                 }
             } catch(EOFException x) {
                 break;
             }
         }
         in.close();
         out.flush();
         out.close();
         
     }
 }
