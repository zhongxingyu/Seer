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
 
 import javax.mail.MessagingException;
 import javax.mail.internet.MimeMessage;
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.StringReader;
 import java.io.StringWriter;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.text.ParseException;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 /* Sample changelog messages from java.net
 
 File Changes:
 
 Directory: /ws-test-harness/test-harness/test/testcases/jaxws/fromjava/server/
 ==============================================================================
 
 File [removed]: AddWebservice.java
 
 File [removed]: EndpointStopper.java
 
 
 ******************************************
 
 Directory: /ws-test-harness/test-harness/bootstrap/src/com/sun/xml/ws/test/
 ===========================================================================
 
 File [changed]: Bootstrap.java
 Url: https://ws-test-harness.dev.java.net/source/browse/ws-test-harness/test-harness/bootstrap/src/com/sun/xml/ws/test/Bootstrap.java?r1=1.3&r2=1.4
 Delta lines:  +11 -11
 
 
 ******************************************
 Directory: /ws-test-harness/test-harness/src/com/sun/xml/ws/test/model/
 =======================================================================
 
 File [added]: WSDL.java
 Url: https://ws-test-harness.dev.java.net/source/browse/ws-test-harness/test-harness/src/com/sun/xml/ws/test/model/WSDL.java?rev=1.1&content-type=text/vnd.viewcvs-markup
 Added lines: 27
 ---------------
 */
 /**
  * Parses {@link Commit} from java.net CVS changelog e-mail.
  *
  * @author Kohsuke Kawaguchi
  */
 public class CVSParser extends NewsParser {
     public CVSCommit parse(MimeMessage msg) throws ParseException {
         List<CVSChange> codeChanges = new ArrayList<CVSChange>();
 
         try {
             Object content = msg.getContent();
             if(!(content instanceof String))
                 throw new ParseException("Unrecognized content type "+content,-1);
 
             String project = getProjectName(msg);
 
             String branch = null;
             String user = null;
             Date date = null;
             boolean inLog = false;
             StringWriter log = new StringWriter();
 
             String directory = null;    // set to the value of "Directory:" when parsing diffs
             String file = null;         // set to the value of "File [...]:" when parsing diffs
             String url = null;          // set to the value of "Url:" when parsing diffs
 
             BufferedReader in = new BufferedReader(new StringReader(content.toString()));
             String line;
             while((line=in.readLine())!=null) {
                 if(line.length()==0) {
                     inLog = false;
                 }
 
                 if(line.startsWith("Tag: ")) {
                     branch = line.substring(5).trim();
                     continue;
                 }
                 if(line.startsWith("User: ")) {
                     user = line.substring(6).trim();
                     continue;
                 }
                 if(line.startsWith("Date: ")) {
                     date = parseDateLine(line);
                     continue;
                 }
                 if(line.startsWith("Log:")) {
                     inLog = true;
                     continue;
                 }
 
                 if(inLog) {
                     // this is a part of the log message
                     log.write(line.substring(1));   // cut off the first SP
                     log.write('\n');
                 } else {
                     Matcher m = DIRECTORY_LINE.matcher(line);
                     if(m.matches()) {
                         directory = m.group(1);
                         continue;
                     }
 
                     m = FILE_LINE.matcher(line);
                     if(m.matches()) {
                         // file always marks the start of new change
                         if(file!=null)
                             codeChanges.add(createCodeChange(directory,file,url));
                         file = m.group(2);
                         continue;
                     }
 
                     m = URL_LINE.matcher(line);
                     if(m.matches())
                         url = m.group(1);
                 }
             }
 
             // wrap up the last change
             if(file!=null)
                 codeChanges.add(createCodeChange(directory,file,url));
 
            CVSCommit item = new CVSCommit(project, branch, user, date, log.toString());
             item.addCodeChanges(codeChanges);
 
             return item;
         } catch (IOException e) {
             // impossible
             throw new Error(e);
         } catch (MessagingException e) {
             // impossible
             throw new Error(e);
         }
     }
 
     private CVSChange createCodeChange(String directory, String file, String url) throws MalformedURLException {
         // compute the revision
         String rev = null;
         if(url!=null) {
             Matcher m = DIFF_REVISION.matcher(url);
             if(m.find()) {
                 rev = m.group(2);
             } else {
                 m = NEW_REVISION.matcher(url);
                 if(m.find())
                     rev = m.group(1);
             }
         }
 
         return new CVSChange(directory+file,url==null?null:new URL(url),rev);
     }
 
     private static final Pattern DIRECTORY_LINE = Pattern.compile("^Directory: (/.+/)$");
     private static final Pattern FILE_LINE = Pattern.compile("^File \\[(changed|added|removed)\\]: (.+)$");
     private static final Pattern DIFF_LINE = Pattern.compile("^\\+\\+\\+ .+\t(20\\d\\d-\\d\\d-\\d\\d \\d\\d:\\d\\d:\\d\\d+0000)\t[0-9.]+$");
 
     private static final Pattern DIFF_REVISION = Pattern.compile("\\?r1=([0-9.]+)&r2=([0-9.]+)");
     private static final Pattern NEW_REVISION = Pattern.compile("\\?rev=([0-9.]+)&");
 }
