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
 
 import com.cloudbees.javanet.cvsnews.CodeChange;
 import com.cloudbees.javanet.cvsnews.Commit;
 import com.cloudbees.javanet.cvsnews.GitHubCommit;
 import hudson.plugins.jira.soap.JiraSoapService;
 import hudson.plugins.jira.soap.JiraSoapServiceService;
 import hudson.plugins.jira.soap.JiraSoapServiceServiceLocator;
 import hudson.plugins.jira.soap.RemoteComment;
 import hudson.plugins.jira.soap.RemoteFieldValue;
 import hudson.plugins.jira.soap.RemoteIssue;
 import org.apache.axis.AxisFault;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.net.URL;
 import java.text.MessageFormat;
 import java.text.SimpleDateFormat;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.HashSet;
 import java.util.Properties;
 import java.util.Set;
 import java.util.regex.Pattern;
 
 /**
  * Subcommand that reads e-mail from stdin and updates the issue tracker.
  *
  * @author Kohsuke Kawaguchi
  */
 public class UpdateCommand extends AbstractIssueCommand {
     private final File credential = new File(HOME, ".java.net.scm_issue_link");
 
     public int execute() throws Exception {
         System.out.println("Parsing stdin");
         return execute(parse(System.in));
     }
 
     public int execute(Collection<? extends Commit> commits) throws Exception {
         for (Commit commit : commits) {
             Set<Issue> issues = parseIssues(commit);
 
             System.out.println("Found "+issues);
             if(issues.isEmpty())
                 continue;   // no issue link
 
             String msg = createUpdateMessage(commit);
 
             boolean markedAsFixed = FIXED.matcher(commit.log).find();
 
             for (Issue issue : issues) {
                 if (PROJECTS.contains(issue.projectName)) {
                     System.out.println("Updating "+issue);
                     // update JIRA
                     JiraSoapServiceService jiraSoapServiceGetter = new JiraSoapServiceServiceLocator();
 
                     Properties props = new Properties();
                     props.load(new FileInputStream(credential));
 
                     String id = issue.projectName.toUpperCase() + "-" + issue.number;
 
                     JiraSoapService service = jiraSoapServiceGetter.getJirasoapserviceV2(new URL(new URL("http://issues.jenkins-ci.org/"), "rpc/soap/jirasoapservice-v2"));
                     String userName = props.getProperty("userName");
                     String securityToken = service.login(userName,props.getProperty("password"));
 
                     // if an issue doesn't exist an exception will be thrown
                     RemoteIssue i = service.getIssue(securityToken, id);
 
                     // is this commit already reported?
                     RemoteComment[] comments = service.getComments(securityToken, id);
                     if (isAlreadyCommented(commit,userName,comments))
                         continue;
 
 
                     // add comment
                     RemoteComment c = new RemoteComment();
                     c.setBody(msg);
                     service.addComment(securityToken, id, c);
 
                     // resolve.
                     // comment set here doesn't work. see http://jira.atlassian.com/browse/JRA-11278
                     if (markedAsFixed && issues.size()==1) {
                         try {
                             service.progressWorkflowAction(securityToken,id,"5" /*this is apparently the ID for "resolved"*/,
                                 new RemoteFieldValue[]{new RemoteFieldValue("comment",new String[]{"closing comment"})});
                         } catch (AxisFault e) {
                             // if the issue cannot be put into the "resolved" state
                             // (perhaps it's already in that state), let it be. Or else
                             // we end up with the carpet bombing like HUDSON-2552.
                             // See HUDSON-5133 for the failure mode.
                             System.err.println("Failed to mark the issue as resolved");
                             e.printStackTrace();
                         }
                     }
                 }
             }
         }
 
         return 0;
     }
 
     /**
      * Returns true if the given commit is already mentioned in one of the comments.
      */
     private boolean isAlreadyCommented(Commit commit, String userName, RemoteComment[] comments) {
         String msg = createUpdateMessage(commit);
 
         for (RemoteComment comment : comments) {
             if (!comment.getAuthor().equals(userName))
                 continue;
 
             // TODO: do this for Subversion and CVS, although GitHub is the only place where
             // we can possibly get multiple notifications for the same commit
             // (when a ref moves and includes existing commits)
             if (commit instanceof GitHubCommit) {
                 GitHubCommit ghc = (GitHubCommit) commit;
                 if (comment.getBody().contains(ghc.commitSha1+"\nLog"))
                     return true;
             }
         }
         return false;
     }
 
     private String createUpdateMessage(Commit _commit) {
         StringBuilder buf = new StringBuilder();
         buf.append("Code changed in "+_commit.project+"\n");
         buf.append(MessageFormat.format("User: {0}\n",_commit.userName));
         buf.append("Path:\n");
 
         if (_commit instanceof GitHubCommit) {
             GitHubCommit commit = (GitHubCommit) _commit;
 
             for (CodeChange cc : commit.getCodeChanges()) {
                 buf.append(MessageFormat.format(" {0}\n",cc.fileName));
             }
             buf.append("http://jenkins-ci.org/commit/").append(commit.repository).append('/').append(commit.commitSha1);
         } else {
             throw new AssertionError("Unrecognized commit type "+_commit.getClass());
         }
 
         buf.append("\n");
         buf.append("Log:\n");
         buf.append(_commit.log);
 
         return buf.toString();
     }
 
     /**
      * Marked for marking bug as fixed.
      */
    private static final Pattern FIXED = Pattern.compile("\\[.*(fixed|FIXED|FIX|FIXES).*\\]");
 
     private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyyMMddHHmmss");
 
     private static final Set<String> PROJECTS = new HashSet<String>(Arrays.asList("jenkins","infra"));
 }
