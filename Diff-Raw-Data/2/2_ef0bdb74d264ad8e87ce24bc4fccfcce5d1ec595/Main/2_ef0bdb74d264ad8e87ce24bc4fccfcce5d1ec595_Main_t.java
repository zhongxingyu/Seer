 /*
  * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
  *
  * Copyright 1997-2008 Sun Microsystems, Inc. All rights reserved.
  *
  * The contents of this file are subject to the terms of either the GNU
  * General Public License Version 2 only ("GPL") or the Common Development
  * and Distribution License("CDDL") (collectively, the "License").  You
  * may not use this file except in compliance with the License. You can obtain
  * a copy of the License at https://glassfish.dev.java.net/public/CDDL+GPL.html
  * or glassfish/bootstrap/legal/LICENSE.txt.  See the License for the specific
  * language governing permissions and limitations under the License.
  *
  * When distributing the software, include this License Header Notice in each
  * file and include the License file at glassfish/bootstrap/legal/LICENSE.txt.
  * Sun designates this particular file as subject to the "Classpath" exception
  * as provided by Sun in the GPL Version 2 section of the License file that
  * accompanied this code.  If applicable, add the following below the License
  * Header, with the fields enclosed by brackets [] replaced by your own
  * identifying information: "Portions Copyrighted [year]
  * [name of copyright owner]"
  *
  * Contributor(s):
  *
  * If you wish your version of this file to be governed by only the CDDL or
  * only the GPL Version 2, indicate your decision by adding "[Contributor]
  * elects to include this software in this distribution under the [CDDL or GPL
  * Version 2] license."  If you don't indicate a single choice of license, a
  * recipient has the option to distribute your version of this file under
  * either the CDDL, the GPL Version 2 or to extend the choice of license to
  * its licensees as provided above.  However, if you add GPL Version 2 code
  * and therefore, elected the GPL Version 2 license, then the option applies
  * only if the new code is made subject to such option by the copyright
  * holder.
  *
  */
 
 package com.cloudbees.javanet.cvsnews.cli;
 
 import org.kohsuke.args4j.CmdLineException;
 import org.kohsuke.args4j.CmdLineParser;
 
 import java.util.Arrays;
 import java.util.List;
 
 /**
  * Reads CVS changelog e-mail from stdin and writes a news file to the current directory.
  *
  * @author Kohsuke Kawaguchi
  */
 public class Main {
     public static void main(String[] args) throws Exception {
         System.exit(run(args));
     }
 
     public static int run(String[] args) throws Exception {
         Command com;
         List<String> commandArgs;
         if(args.length==0) {
             System.err.println("Usage: java -jar parser.jar <subcommand>");
             return -1;
         } else {
             try {
                Class c = Class.forName("com.cloudbees.javanet.cvsnews.cli."+capitalize(args[0])+"Command");
                 com = (Command)c.newInstance();
             } catch (ClassNotFoundException e) {
                 System.err.println("No such command: "+args[0]);
                 return -1;
             }
             commandArgs = Arrays.asList(args).subList(1,args.length);
         }
 
         CmdLineParser p = new CmdLineParser(com);
         try {
             p.parseArgument(commandArgs.toArray(new String[0]));
             return com.execute();
         } catch (CmdLineException e) {
             p.printUsage(System.err);
             return -1;
         }
     }
 
     private static String capitalize(String text) {
         return Character.toUpperCase(text.charAt(0))+text.substring(1);
     }
 }
