 /**
  * Copyright 2013 markiewb
  *
  * Licensed under the Apache License, Version 2.0 (the "License"); you may not
  * use this file except in compliance with the License. You may obtain a copy of
  * the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
  * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
  * License for the specific language governing permissions and limitations under
  * the License.
  */
 package de.markiewb.netbeans.plugin.git.openinexternalviewer.strategies;
 
 import de.markiewb.netbeans.plugin.git.openinexternalviewer.RepoStrategy;
 import java.text.MessageFormat;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 import org.openide.util.lookup.ServiceProvider;
 
 /**
  * https://github.com/markiewb/nb-git-branch-in-statusbar.git
  * https://github.com/markiewb/nb-git-branch-in-statusbar/commits/master
  * git@github.com:markiewb/nb-git-branch-in-statusbar.git
  *
  * @author markiewb
  */
 @ServiceProvider(service = RepoStrategy.class)
 public final class GitHubStrategyImpl implements RepoStrategy {
 
     private final Pattern pHttp = Pattern.compile("(?<protocol>http|https)://(?<username>.+?@)?(?<server>github.com)/(?<repo>.+)\\.git");
     private final Pattern pGit = Pattern.compile("(?<username>git)@(?<server>github.com):(?<repo>.+)\\.git");
 
     @Override
     public String getUrl(String remote, String branchName, String branchRevId) {
         String url = null;
         if (this.supports(remote)) {
             Pattern p = this.getMatchingPattern(remote);
             Matcher matcher = p.matcher(remote);
             matcher.find();
             String protocol = "https";
             try {
                 protocol = matcher.group("protocol");
             }
            catch (IndexOutOfBoundsException e) {
                 // Do nothing, but use default "https".
             }
             String username = matcher.group("username");
             String server = matcher.group("server");
             String repo = matcher.group("repo");
             url = MessageFormat.format("{0}://{1}/{2}/commits/{3}", protocol, server, repo, branchName);
         }
         return url;
     }
 
     @Override
     public boolean supports(String remote) {
         return (pHttp.matcher(remote).matches() || pGit.matcher(remote).matches());
     }
 
     @Override
     public String getLabel() {
         return "GitHub";
     }
 
     protected Pattern getMatchingPattern(String remote) {
         if (pHttp.matcher(remote).matches()) {
             return pHttp;
         }
         else if (pGit.matcher(remote).matches()) {
             return pGit;
         }
         return null;
     }
 
 }
