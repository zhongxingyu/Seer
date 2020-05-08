 /* 
  * Copyright (c) Ericsson AB, 2013
  * All rights reserved.
  *
  * License terms:
  *
  * Redistribution and use in source and binary forms, 
  * with or without modification, are permitted provided 
  * that the following conditions are met:
  *     * Redistributions of source code must retain the above 
  *       copyright notice, this list of conditions and the 
  *       following disclaimer.
  *     * Redistributions in binary form must reproduce the 
  *       above copyright notice, this list of conditions and 
  *       the following disclaimer in the documentation and/or 
  *       other materials provided with the distribution.
  *     * Neither the name of the copyright holder nor the names 
  *       of its contributors may be used to endorse or promote 
  *       products derived from this software without specific 
  *       prior written permission.
  *
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND 
  * CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, 
  * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF 
  * MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE 
  * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR 
  * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, 
  * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT 
  * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; 
  * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) 
  * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN 
  * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR 
  * OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS 
  * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  */
 
 package org.caltoopia.codegen.printer;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.PrintStream;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import org.caltoopia.cli.Cal2C;
 import org.caltoopia.cli.CompilationSession;
 import org.caltoopia.codegen.CEnvironment;
 import org.caltoopia.codegen.CodegenError;
 
 public class CPrintBuildFiles {
 
     CompilationSession session = null;
     PrintStream out = null;
     CEnvironment cenv = null;
 
     public CPrintBuildFiles(CompilationSession session, CEnvironment cenv) {
         this.session = session;
         this.out = session.getOutputStream();
         this.cenv = cenv;
     }
     
     public void MonoMakefile() throws Exception {
         //Print the makefile
         File dst = new File(session.getOutputFolder() + File.separator + "Makefile");
         out.println("Copying '" + dst + "'");
         BufferedReader reader = null;
        //InputStream src = this.getClass().getResourceAsStream("Makefile");
        InputStream src = Cal2C.class.getResourceAsStream("Makefile");
         reader = new BufferedReader(new InputStreamReader(src));
         
         String line = null;
         if(!dst.exists()) {
             dst.createNewFile();
         }
         
         PrintStream writer = new PrintStream(new FileOutputStream(dst));
              
         while((line = reader.readLine()) != null) {
             writer.println(line);
         }
         writer.close();
     }
     
     public void MonoConfigFile(String needSdl) throws Exception {
         //Print make config file including annotation information
         String file = session.getOutputFolder() + File.separator + "config.mk";            
         out.println("Writing '" + file + "'");
         PrintStream config = new PrintStream(file);
         config.print("SOURCES =");
         for(String str : cenv.sourceFiles) {
             config.print(" " + str);
         }
 
         if(!cenv.sources.isEmpty()) {
             Set<String> sources = new HashSet<String>();
             for(String str : cenv.sources.split(" *, *")) {
                 if(!str.trim().isEmpty() && !sources.contains(str.trim())) {
                     config.print(" "+str.trim());
                     sources.add(str.trim());
                 }
             }
         }
         config.println();
         String topNetwork = session.getElaboratedNetwork().getType().getName();
         config.println("EXEC_NAME = " + topNetwork.substring(topNetwork.lastIndexOf(".")+1));
         config.println("RUNTIME_ROOT = " + session.getRuntimePath());
         config.println("SDL = " + needSdl);
         if(session.debugPrint()==CompilationSession.DEBUG_TYPE_ACTIONUSER || session.debugPrint()==CompilationSession.DEBUG_TYPE_USER)
             config.println("DPRINT = y");
         else
             config.println("DPRINT = n");
 
         if(!cenv.includePaths.isEmpty()) {
             config.print("CINCLUDES = ");
             Set<String> printed = new HashSet<String>();
             for(String str : cenv.includePaths.split(" *, *")) {
                 if(!str.trim().isEmpty() && !printed.contains(str.trim())) {
                     config.print("-I"+str.trim() + " ");
                     printed.add(str.trim());
                 }
             }
             config.println();
         }
         if(!cenv.libraryPaths.isEmpty()) {
             config.print("CLIBRARIES = ");
             Set<String> printed = new HashSet<String>();
             for(String str : cenv.libraryPaths.split(" *, *")) {
                 if(!str.trim().isEmpty() && !printed.contains(str.trim())) {
                     config.print("-L"+str.trim() + " ");
                     printed.add(str.trim());
                 }
             }
             config.println();
         }
         if(!cenv.libraries.isEmpty()) {
             config.print("LDLIBS = ");
             Set<String> printed = new HashSet<String>();
             for(String str : cenv.libraries.split(" *, *")) {
                 if(!str.trim().isEmpty() && !printed.contains(str.trim())) {
                     config.print("-l"+str.trim() + " ");
                     printed.add(str.trim());
                 }
             }
             config.println();
         }
         
         if(session.getTarget().contains("mac")) {
             config.println("CC = gcc");
             config.println("TARGET = mac");
         } else if(session.getTarget().contains("linux")) {
             config.println("TARGET = linux");
         }
 
         config.close();
 
     }
 }
