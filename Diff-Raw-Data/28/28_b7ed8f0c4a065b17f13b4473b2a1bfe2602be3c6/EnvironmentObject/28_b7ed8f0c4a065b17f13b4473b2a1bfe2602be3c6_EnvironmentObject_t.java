 package edu.berkeley.cs.builtin.objects;
 
 import edu.berkeley.cs.builtin.functions.NativeFunction;
 import edu.berkeley.cs.parser.SymbolTable;
 
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
 public class EnvironmentObject extends CObject {
     public static EnvironmentObject instance = new EnvironmentObject();
 
     private EnvironmentObject() {
         this.addNewRule();
         this.addSymbol(SymbolTable.getInstance().getId("var"));
        this.addMeta(SymbolTable.getInstance().token);
         this.addSymbol(SymbolTable.getInstance().getId("="));
         this.addMeta(SymbolTable.getInstance().expr);
         this.addAction(new NativeFunction("assignment"));
 
 //        this.addNewRule();
 //        this.addMeta(SymbolTable.getInstance().token);
 //        this.addAction(new NativeFunction("returnArgument"));
 
 
         this.addNewRule();
         this.addSymbol(SymbolTable.getInstance().getId("def"));
         this.addAction(new NativeFunction("newDefinitionEater"));
 
 //        this.addNewRule();
 //        this.addSymbol(SymbolTable.getInstance().getId("=="));
 //        this.addMeta(SymbolTable.getInstance().argument);
 //        this.addAction(new NativeFunction("equality"));
 //
 //        this.addNewRule();
 //        this.addSymbol(SymbolTable.getInstance().getId("!="));
 //        this.addMeta(SymbolTable.getInstance().argument);
 //        this.addAction(new NativeFunction("disequality"));
 
         this.addNewRule();
         this.addSymbol(SymbolTable.getInstance().getId("("));
         this.addMeta(SymbolTable.getInstance().expr);
         this.addSymbol(SymbolTable.getInstance().getId(")"));
         this.addAction(new NativeFunction("returnArgument"));
 
         this.addNewRule();
         this.addSymbol(SymbolTable.getInstance().getId("load"));
         this.addMeta(SymbolTable.getInstance().expr);
         this.addAction(new NativeFunction("loadFile"));
 
         this.addNewRule();
         this.addSymbol(SymbolTable.getInstance().getId("print"));
         this.addMeta(SymbolTable.getInstance().expr);
         this.addAction(new NativeFunction("print"));
 
         this.addNewRule();
         this.addSymbol(SymbolTable.getInstance().getId("new"));
         this.addSymbol(SymbolTable.getInstance().getId("Object"));
         this.addAction(new NativeFunction("newObject"));
 
         this.addNewRule();
         this.addSymbol(SymbolTable.getInstance().getId("assert"));
         this.addMeta(SymbolTable.getInstance().expr);
         this.addAction(new NativeFunction("assertEquality"));
 
         this.addNewRule();
         this.addSymbol(SymbolTable.getInstance().getId("if"));
         this.addMeta(SymbolTable.getInstance().expr);
         this.addSymbol(SymbolTable.getInstance().getId("then"));
         this.addMeta(SymbolTable.getInstance().expr);
         this.addSymbol(SymbolTable.getInstance().getId("else"));
         this.addMeta(SymbolTable.getInstance().expr);
         this.addAction(new NativeFunction("ifAction"));
 
         this.addNewRule();
         this.addSymbol(SymbolTable.getInstance().getId("while"));
         this.addMeta(SymbolTable.getInstance().expr);
         this.addMeta(SymbolTable.getInstance().expr);
         this.addAction(new NativeFunction("whileAction"));
 
         this.addNewRule();
         this.addSymbol(SymbolTable.getInstance().getId("once"));
         this.addMeta(SymbolTable.getInstance().expr);
         this.addAction(new NativeFunction("onceAction"));
 
 //        this.addNewRule();
 //        this.addSymbol(SymbolTable.getInstance().getId("LS"));
 //        this.addAction(new GetField(new Reference(this)));
 
     }
 
 }
