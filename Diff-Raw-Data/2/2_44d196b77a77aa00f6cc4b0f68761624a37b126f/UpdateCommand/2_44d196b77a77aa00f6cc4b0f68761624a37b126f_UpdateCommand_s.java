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
 
 package com.sun.javanet.cvsnews.cli;
 
 import com.sun.javanet.cvsnews.CVSChange;
 import com.sun.javanet.cvsnews.CVSCommit;
 import com.sun.javanet.cvsnews.CodeChange;
 import com.sun.javanet.cvsnews.Commit;
 import com.sun.javanet.cvsnews.SubversionCommit;
 import org.kohsuke.jnt.IssueEditor;
 import org.kohsuke.jnt.IssueResolution;
 import org.kohsuke.jnt.JNIssue;
 import org.kohsuke.jnt.JNProject;
 import org.kohsuke.jnt.JavaNet;
 import org.kohsuke.jnt.ProcessingException;
 
 import java.io.File;
 import java.io.IOException;
 import java.text.MessageFormat;
 import java.text.SimpleDateFormat;
 import java.util.Arrays;
 import java.util.HashSet;
 import java.util.Set;
 import java.util.regex.Pattern;
 
 /**
  * Subcommand that reads e-mail from stdin and adds news files.
  *
  * @author Kohsuke Kawaguchi
  */
 public class UpdateCommand extends AbstractIssueCommand {
 
     public int execute() throws Exception {
         System.out.println("Parsing stdin");
         Commit commit = parseStdin();
         Set<Issue> issues = parseIssues(commit);
 
         System.out.println("Found "+issues);
 
         String msg = createUpdateMessage(commit);
 
         boolean markedAsFixed = FIXED.matcher(commit.log).find();
 
         JavaNet con = JavaNet.connect(new File(HOME, ".java.net.scm_issue_link"));
         for (Issue issue : issues) {
             JNProject p = con.getProject(issue.projectName);
             if(!con.getMyself().getMyProjects().contains(p))
                 // not a participating project
                 continue;
 
             System.out.println("Updating "+issue);
             try {
                 JNIssue i = p.getIssueTracker().get(issue.number);
                 IssueEditor e = i.beginEdit();
                 if(markedAsFixed && issues.size()==1)
                     e.resolve(IssueResolution.FIXED);
                 e.commit(msg);
             } catch (ProcessingException e) {
                 e.printStackTrace();
             }
         }
 
         return 0;
     }
 
     private String createUpdateMessage(Commit _commit) {
         StringBuilder buf = new StringBuilder();
         buf.append("Code changed in "+_commit.project+"\n");
         buf.append(MessageFormat.format("User: {0}\n",_commit.userName));
         buf.append("Path:\n");
 
         if (_commit instanceof CVSCommit) {
             CVSCommit commit = (CVSCommit) _commit;
 
             boolean hasFisheye = FISHEYE_CVS_PROJECT.contains(commit.project);
 
             for (CVSChange cc : commit.getCodeChanges()) {
                 buf.append(MessageFormat.format(" {0} ({1})\n",cc.fileName,cc.revision));
                 if(!hasFisheye)
                     buf.append("   "+cc.url+"\n");
             }
             if(hasFisheye) {
                 try {
                     buf.append(MessageFormat.format(
                     " http://fisheye5.cenqua.com/changelog/{0}/?cs={1}:{2}:{3}\n",
                         commit.project,
                         commit.branch==null?"MAIN":commit.branch,
                         commit.userName,
                         DATE_FORMAT.format(commit.getCodeChanges().get(0).determineTimstamp())));
                 } catch (IOException e) {
                     e.printStackTrace();
                     buf.append("Failed to compute FishEye link "+e+"\n");
                 } catch (InterruptedException e) {
                     e.printStackTrace();
                     buf.append("Failed to compute FishEye link "+e+"\n");
                 }
             }
         } else {
             SubversionCommit commit = (SubversionCommit) _commit;
 
             boolean hasFisheye = FISHEYE_SUBVERSION_PROJECT.contains(commit.project);
 
             for (CodeChange cc : commit.getCodeChanges()) {
                 buf.append(MessageFormat.format(" {0}\n",cc.fileName));
                 if(!hasFisheye)
                     buf.append("   "+cc.url+"\n");
             }
             if(hasFisheye) {
                 buf.append(MessageFormat.format(
                 "http://fisheye4.cenqua.com/changelog/{0}/?cs={1}",
                     commit.project,
                     commit.revision));
             }
         }
 
         buf.append("\n");
         buf.append("Log:\n");
         buf.append(_commit.log);
 
         return buf.toString();
     }
 
     /**
      * Marked for marking bug as fixed.
      */
     private static final Pattern FIXED = Pattern.compile("\\b\\[.*(fixed|FIXED).*\\]\\b");
 
     // taken from http://fisheye5.cenqua.com/
     private static final Set<String> FISHEYE_CVS_PROJECT = new HashSet<String>(Arrays.asList(
             "actions",
             "cejug-classifieds",
             "clickstream",
             "databinding",
             "dwr",
             "equinox",
             "fi",
             "flamingo",
             "genericjmsra",
             "genesis",
             "glassfish",
             "hudson",
             "hyperjaxb",
             "hyperjaxb2",
             "hyperubl",
             "javaserverfaces-sources",
             "jax-rpc",
             "jax-rpc-sources",
             "jax-rpc-tck",
             "jax-ws-sources",
             "jax-ws-tck",
             "jax-wsa",
             "jax-wsa-sources",
             "jax-wsa-tck",
             "jaxb",
             "jaxb-architecture-document",
             "jaxb-sources",
             "jaxb-tck",
             "jaxb-verification",
             "jaxb-workshop",
             "jaxb2-commons",
             "jaxb2-sources",
             "jaxmail",
             "jaxp-sources",
             "jaxwsunit",
             "jdic",
             "jdnc",
             "jwsdp",
             "jwsdp-samples",
             "l2fprod-common",
             "laf-plugin",
             "laf-widget",
             "lg3d",
             "ognl",
             "open-esb",
             "open-jbi-components",
             "osuser",
             "osworkflow",
             "quartz",
             "saaj",
             "sailfin",
             "sbfb",
             "schoolbus",
             "semblance",
             "shard",
             "sitemesh",
             "sjsxp",
             "skinlf",
             "stax-utils",
             "substance",
             "swing-layout",
             "swinglabs",
             "swinglabs-demos",
             "swingworker",
             "swingx",
             "tda",
             "tonic",
             "truelicense",
             "truezip",
             "txw",
             "webleaf",
             "webleaftest",
             "webwork",
             "wizard",
             "wsit",
             "xmlidfilter",
             "xom",
             "xsom",
             "xwork",
             "xwss"));
 
     // taken from http://fisheye4.cenqua.com/
     private static final Set<String> FISHEYE_SUBVERSION_PROJECT = new HashSet<String>(Arrays.asList(
             "appfuse",
             "appfuse-light",
             "cougarsquared",
             "cqme",
             "diy",
             "glassfish-svn",
             "hk2",
             "jax-ws-commons",
             "jmimeinfo",
             "jtharness",
             "jxse-cms",
             "jxse-metering",
             "jxse-shell",
             "jxta-jxse",
             "mifos",
             "opencds",
             "openjdk",
             "openjfx-compiler",
             "phoneme",
             "rife-crud",
             "rife-jumpstart"
             ));
 
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyyMMddHHmmss");
 }
