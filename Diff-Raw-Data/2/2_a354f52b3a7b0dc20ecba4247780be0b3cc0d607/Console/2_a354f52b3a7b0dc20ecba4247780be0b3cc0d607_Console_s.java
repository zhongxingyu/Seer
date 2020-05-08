 /*
  * Copyright 2012, Gene McCulley
  * All rights reserved.
  *
  * Redistribution and use in source and binary forms, with or without 
  * modification, are permitted provided that the following conditions are met:
  *
  *   Redistributions of source code must retain the above copyright notice, 
  *   this list of conditions and the following disclaimer.
  *
  *   Redistributions in binary form must reproduce the above copyright 
  *   notice, this list of conditions and the following disclaimer in the 
  *   documentation and/or other materials provided with the distribution.
  *
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS 
  * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT 
  * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR 
  * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT 
  * HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, 
  * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT 
  * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, 
  * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY 
  * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT 
  * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE 
  * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  *
  */
 package com.stackframe.pattymelt;
 
 import javax.swing.JComponent;
 import javax.swing.JTextArea;
 
 /**
  * A console for DCPU-16.
  *
  * @author mcculley
  */
 public class Console {
 
     // FIXME: Add keyboard support
     private final int address;
     private final short[] memory;
     private final JTextArea textArea;
 
     private static final int numRows = 16, numColumns = 32, grid = numRows * numColumns;
 
     public Console(int address, short[] memory) {
         this.address = address;
         this.memory = memory;
         textArea = new JTextArea(numRows, numColumns);
     }
 
     public JComponent getWidget() {
         return textArea;
     }
 
     public void update() {
         StringBuilder buf = new StringBuilder();
         for (int i = 0; i < grid; i++) {
             short word = memory[address + i];
            char c = (char) (word & 0xff);
             buf.append(c);
         }
 
         textArea.setText(buf.toString());
     }
 }
