 package edu.berkeley.cs.builtin.objects;
 
 import edu.berkeley.cs.Interpreter;
 import edu.berkeley.cs.lexer.BasicScanner;
 import edu.berkeley.cs.lexer.Lexer;
 import edu.berkeley.cs.parser.CallFrame;
 import junit.framework.TestCase;
 
 import java.io.StringReader;
 
 /**
  * Copyright (c) 2006-2011,
  * Koushik Sen    <ksen@cs.berkeley.edu>
  * All rights reserved.
  * <p/>
  * Redistribution and use in source and binary forms, with or without
  * modification, are permitted provided that the following conditions are
  * met:
  * <p/>
  * 1. Redistributions of source code must retain the above copyright
  * notice, this list of conditions and the following disclaimer.
  * <p/>
  * 2. Redistributions in binary form must reproduce the above copyright
  * notice, this list of conditions and the following disclaimer in the
  * documentation and/or other materials provided with the distribution.
  * <p/>
  * 3. The names of the contributors may not be used to endorse or promote
  * products derived from this software without specific prior written
  * permission.
  * <p/>
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS
  * IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
  * TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A
  * PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER
  * OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
  * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
  * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
  * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
  * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
  * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
  * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  */
 public class CompoundTokenTest extends TestCase {
     public void testArray() throws Exception {
        Interpreter.interpret("def Array @argument @edu.berkeley.cs.builtin.objects.CArray endef ; var x = (Array 10); x[1] = 89; print (x[1]);");
     }
 }
 //def Array @argument @edu.berkeley.cs.builtin.objects.CArray endef ; var x = (Array 10); x[1] = 89; print (x[1]);
