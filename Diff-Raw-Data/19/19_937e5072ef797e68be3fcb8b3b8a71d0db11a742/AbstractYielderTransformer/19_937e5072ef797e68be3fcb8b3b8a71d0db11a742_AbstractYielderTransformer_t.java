 package com.infomancers.collections.yield.asmbase;
 
 import org.objectweb.asm.ClassReader;
 import org.objectweb.asm.commons.EmptyVisitor;
 import org.objectweb.asm.util.CheckClassAdapter;
 import org.objectweb.asm.util.TraceClassVisitor;
 
 import java.io.PrintWriter;
 import java.lang.instrument.ClassFileTransformer;
 import java.lang.instrument.IllegalClassFormatException;
 import java.security.ProtectionDomain;
 
 /**
  * Copyright (c) 2007, Aviad Ben Dov
  * <p/>
  * All rights reserved.
  * <p/>
  * Redistribution and use in source and binary forms, with or without modification,
  * are permitted provided that the following conditions are met:
  * <p/>
  * 1. Redistributions of source code must retain the above copyright notice, this list
  * of conditions and the following disclaimer.
  * 2. Redistributions in binary form must reproduce the above copyright notice, this
  * list of conditions and the following disclaimer in the documentation and/or other
  * materials provided with the distribution.
  * 3. Neither the name of Infomancers, Ltd. nor the names of its contributors may be
  * used to endorse or promote products derived from this software without specific
  * prior written permission.
  * <p/>
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
  * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
  * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
  * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
  * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
  * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
  * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
  * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
  * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
  * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
  * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  */
 public abstract class AbstractYielderTransformer implements ClassFileTransformer {
     private final boolean debug;
 
     public AbstractYielderTransformer(boolean debug) {
         this.debug = debug;
     }
 
     public final byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
         byte[] result = classfileBuffer;
 
         if (classBeingRedefined == null) {
             try {
                 // first pass - gather statistics
                 ClassReader reader = new ClassReader(classfileBuffer);
                 final YieldReturnCounter counter = new YieldReturnCounter(new EmptyVisitor());
                 final LocalVariableMapper mapper = new LocalVariableMapper(counter);
                 YielderChecker checker = new YielderChecker(mapper);
 
                 reader.accept(checker, 0);
 
                 if (checker.isYielder()) {
                     // second pass - write new code
                     trace("Before", result);
 
                     YielderInformationContainer info = new DelegatingInformationContainer(counter, mapper);
 
                     if (debug) {
                         System.out.println("info: [" + info + "]");
                     }
 
                     result = enhanceClass(reader, info);
 
                     trace("After", result);
                     check(result);
                 }
             } catch (Exception e) {
                 e.printStackTrace();
             }
         }
         return result;
     }
 
     protected abstract byte[] enhanceClass(ClassReader reader, YielderInformationContainer info);
 
     private void trace(String title, byte[] classfileBytes) {
         if (debug) {
             TraceClassVisitor traceClassVisitor = new TraceClassVisitor(new PrintWriter(System.out));
 
             System.out.println();
             System.out.println("<------------- " + title + " ---------------> ");
             new ClassReader(classfileBytes).accept(traceClassVisitor, 0);
         }
     }
 
     private void check(byte[] bytes) {
         if (debug) {
            CheckClassAdapter.verify(new ClassReader(bytes), true, new PrintWriter(System.out));
         }
     }
 
 }
