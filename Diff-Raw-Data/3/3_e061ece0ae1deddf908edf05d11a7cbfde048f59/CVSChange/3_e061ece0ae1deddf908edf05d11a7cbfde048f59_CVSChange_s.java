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
 
 package com.sun.javanet.cvsnews;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.StringWriter;
 import java.net.URL;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 /**
  * {@link CodeChange} for CVS.
  *
  * @author Kohsuke Kawaguchi
  */
 public class CVSChange extends CodeChange {
     /**
      * Revision of the new file.
      */
     public final String revision;
 
     public CVSChange(String fileName, URL url, String revision) {
         super(fileName, url);
         this.revision = revision;
     }
 
     public String toString() {
         return fileName+':'+revision;
     }
     
     /**
      * Obtains the CVS repository's exact timestamp for this change.
      */
     public Date determineTimstamp() throws IOException, InterruptedException {
         ProcessBuilder pb = new ProcessBuilder();
         pb.command(
             "cvs",
             "-d:pserver:guest@cvs.dev.java.net:/cvs",
             "rlog",
             "-N",
            "-r",
            revision,
             fileName.substring(1)); // trim off the first '/'
         pb.redirectErrorStream(true);
 
         Process proc = pb.start();
         proc.getOutputStream().close();
         BufferedReader r = new BufferedReader(new InputStreamReader(proc.getInputStream()));
         String line;
         Date result=null;
         StringWriter out = new StringWriter();
         while((line=r.readLine())!=null) {
             out.write(line);
             out.write('\n');
             if(result==null) {
                 Matcher m = DATE_PATTERN.matcher(line);
                 if(m.matches()) {
                     try {
                         result = DATE_FORMAT.parse(m.group(1));
                     } catch (ParseException e) {
                         throw new IOException("Failed to parse "+m.group(1));
                     }
                 }
             }
         }
 
         // wait for the completion
         proc.waitFor();
 
         throw new IOException("cvs output:\n"+out);
     }
 
     private static final Pattern DATE_PATTERN = Pattern.compile("^date: (..../../.. ..:..:..);.+");
 
     private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
 }
