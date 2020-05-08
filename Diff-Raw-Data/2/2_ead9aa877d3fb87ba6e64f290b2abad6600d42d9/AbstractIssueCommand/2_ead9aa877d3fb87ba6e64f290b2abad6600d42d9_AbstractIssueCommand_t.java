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
 
 import com.sun.javanet.cvsnews.Commit;
 
 import java.util.HashSet;
 import java.util.Set;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 /**
  * Partial {@link Command} implementation that deals with issues in the issue tracker.
  *
  * @author Kohsuke Kawaguchi
  */
 public abstract class AbstractIssueCommand extends AbstractCommand implements Command {
     /**
      * Discovers links to issues.
      */
     protected final Set<Issue> parseIssues(Commit commit) {
         Set<Issue> issues = new HashSet<Issue>();
 //        Matcher m = ISSUE_MARKER.matcher(commit.log);
 //        while(m.find())
 //            issues.add(new Issue(commit.project,m.group(1)));
 //
 //        Matcher m = ISSUE_MARKER2.matcher(commit.log);
 //        while(m.find())
//            issues.add(new Issue(commit.project,m.group(1)));
 
         Matcher m = ID_MARKER.matcher(commit.log);
         while(m.find())
             issues.add(new Issue(m.group(1),m.group(2)));
         return issues;
     }
 
     /**
      * Issue in an issue tracker.
      */
     public static final class Issue {
         public final String projectName;
         public final int number;
 
         public Issue(String projectName, int number) {
             this.projectName = projectName.toLowerCase();
             this.number = number;
         }
 
         public Issue(String projectName, String number) {
             this(projectName,Integer.parseInt(number));
         }
 
         public boolean equals(Object o) {
             if (this == o) return true;
             if (o == null || getClass() != o.getClass()) return false;
 
             UpdateCommand.Issue issue = (UpdateCommand.Issue) o;
 
             return number == issue.number && projectName.equals(issue.projectName);
 
         }
 
         public int hashCode() {
             int result;
             result = projectName.hashCode();
             result = 31 * result + number;
             return result;
         }
 
         public String toString() {
             return projectName+'-'+number;
         }
     }
 
     /**
      * Look for strings like "issue #350" and "issue 350"
      */
     private static final Pattern ISSUE_MARKER = Pattern.compile("\\b[Ii]ssue #?(\\d+)\\b");
 
     /**
      * Looks for the line "Issue number: #350" which is the default commit message format on java.net
      */
     private static final Pattern ISSUE_MARKER2 = Pattern.compile("\\bIssue number:\\s*#?(\\d+)\\b");
 
     /**
      * Look for full ID line like "JAXB-512"
      */
     private static final Pattern ID_MARKER = Pattern.compile("\\b([A-Z-]+)-(\\d+)\\b");
 }
