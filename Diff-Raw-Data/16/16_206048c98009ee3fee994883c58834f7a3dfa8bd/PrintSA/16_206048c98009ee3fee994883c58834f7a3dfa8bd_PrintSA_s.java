 /*--------------------------------------------------------------------------
  *  Copyright 2011 utgenome.org
  *
  *  Licensed under the Apache License, Version 2.0 (the "License");
  *  you may not use this file except in compliance with the License.
  *  You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  *  Unless required by applicable law or agreed to in writing, software
  *  distributed under the License is distributed on an "AS IS" BASIS,
  *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  *  See the License for the specific language governing permissions and
  *  limitations under the License.
  *--------------------------------------------------------------------------*/
 //--------------------------------------
 // genome-weaver Project
 //
 // PrintSA.java
 // Since: 2011/02/17
 //
 // $URL$ 
 // $Author$
 //--------------------------------------
 package org.utgenome.weaver.align;
 
 import java.io.StringWriter;
 
 import org.utgenome.gwt.utgb.client.bio.IUPAC;
 import org.xerial.util.opt.Argument;
 import org.xerial.util.opt.Command;
 
 public class PrintSA implements Command
 {
 
     @Override
     public String name() {
         return "sa-debug";
     }
 
     @Override
     public String getOneLineDescription() {
         return "print SA and BWT for debugging";
     }
 
     @Override
     public Object getOptionHolder() {
         return this;
     }
 
     @Argument(index = 0, required = true)
     private String seq;
 
     @Override
     public void execute(String[] args) throws Exception {
 
         IUPACSequence s = new IUPACSequence(seq);
         LSeq SA = new LSAIS.IntArray(new int[(int) s.textSize()], 0);
         LSAIS.suffixsort(s, SA, IUPAC.values().length);
 
         IUPACSequence bwt = new IUPACSequence(s.textSize());
         Pac2BWT.bwt(s, SA, bwt);
 
         for (int i = 0; i < SA.textSize(); ++i) {
             int sa = (int) SA.lookup(i);
             System.out.println(String.format("%3d %3d %s %s", i, sa, rotateLeft(seq, sa), bwt.getIUPAC(i)));
         }
     }
 
     public static String rotateLeft(String s, int shift) {
         StringWriter w = new StringWriter();
        for (int i = shift; i < shift + s.length(); ++i) {
 
             if (i == s.length()) {
                 w.append("$");
                 continue;
             }
             int pos = i % s.length();
             w.append(s.charAt(pos));
         }
         return w.toString();
     }
 
 }
