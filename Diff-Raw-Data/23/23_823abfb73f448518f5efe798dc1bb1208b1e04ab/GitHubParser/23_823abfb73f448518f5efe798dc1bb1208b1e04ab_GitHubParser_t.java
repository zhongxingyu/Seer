 package com.cloudbees.javanet.cvsnews;
 
 import javax.mail.MessagingException;
 import javax.mail.internet.MimeMessage;
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.StringReader;
 import java.net.URL;
 import java.text.ParseException;
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  * Parses GitHub commit notification e-mail.
  *
  * @author Kohsuke Kawaguchi
  */
 public class GitHubParser extends NewsParser {
     enum State {
         DEFAULT,
         PARSING_CHANGED_PATH,
         PARSING_COMMIT,
         PARSING_LOG
     }
 
     @Override
     public List<GitHubCommit> parse(MimeMessage msg) throws ParseException {
         try {
             Object content = msg.getContent();
             if (!(content instanceof String))
                 throw new ParseException("Unrecognized content type " + content, -1);
 
 
             List<GitHubCommit> commits = new ArrayList<GitHubCommit>();
 
             BufferedReader in = new BufferedReader(new StringReader(content.toString()));
 
             String commit=null, author=null, url=null;
             State state = State.DEFAULT;
             StringBuilder log = new StringBuilder();
             List<GitHubCodeChange> paths = new ArrayList<GitHubCodeChange>();
 
             String line;
             while ((line = in.readLine()) != null) {
                 switch (state) {
                 case DEFAULT:
                     if (line.startsWith("Commit: ")) {
                         commit = line.substring("Commit: ".length()).trim();
                         state = State.PARSING_COMMIT;
                         continue;
                     }
                     if (line.startsWith("Author: ")) {
                         author = line.substring("Author: ".length()).trim();
                         continue;
                     }
 //                if(line.startsWith("Date: ")) {
 //                    date = somehowParseDateLine(line);
 //                    continue;
 //                }
                     if (line.startsWith("Changed paths:")) {
                         state = State.PARSING_CHANGED_PATH;
                         continue;
                     }
                     if (line.equals("-----------")) {
                         state = State.PARSING_LOG;
                         continue;
                     }
                     break;
 
                 case PARSING_COMMIT:
                     url = line.trim();
                     state = State.DEFAULT;
                     break;
 
                 case PARSING_CHANGED_PATH: {
                     if (line.length() == 0) {
                         state = State.DEFAULT;
                         break;
                     }
 
                     String fileName = line.trim().substring(2);
                     paths.add(new GitHubCodeChange(fileName, new URL(url+"#diff-"+paths.size())));
                     break;
                 }
 
                 case PARSING_LOG:
                     if (line.startsWith("Commit: ")) {
                         // completed the whole thing
                         // TODO: parse the project correctly
                         GitHubCommit c = new GitHubCommit(commit,url,"hudson",author,null,log.toString());
                         c.addCodeChanges(paths);
                         commits.add(c);
 
                         // reset parser state
                         author = url = null;
                        log.setLength(0);
                         paths.clear();
 
                         // and parse this commit line
                         commit = line.substring("Commit: ".length()).trim();
                         state = State.PARSING_COMMIT;
                         break;
                     }
                     log.append(line).append('\n');
                     break;
                 }
             }
 
             // from the last one
             if (commit!=null) {
                 GitHubCommit c = new GitHubCommit(commit,url,"hudson",author,null,log.toString());
                 c.addCodeChanges(paths);
                 commits.add(c);
             }
 
             return commits;
         } catch (IOException e) {
             // impossible
             throw new Error(e);
         } catch (MessagingException e) {
             // impossible
             throw new Error(e);
         }
     }
 
 /*
     Sample e-mail
 
 
 Branch: refs/heads/master
 Home:   https://github.com/hudson/hudson
 
 Commit: 3f2ab68b248a8104053227b074221937f7ab3176
     https://github.com/hudson/hudson/commit/3f2ab68b248a8104053227b074221937f7ab3176
 Author: Olivier Lamy <olamy@apache.org>
 Date:   2011-01-13 (Thu, 13 Jan 2011)
 
 Changed paths:
   M maven-plugin/src/main/java/hudson/maven/reporters/SurefireArchiver.java
   A test/src/test/java/hudson/maven/MavenBuildSurefireFailedTest.java
   A test/src/test/resources/hudson/maven/maven-multimodule-unit-failure.zip
 
 Log Message:
 -----------
 [HUDSON-8415] M2 and M3 builds behave differently when tests fail
 
 
 Commit: fbb54532d50a14c64a3de6a75d4cc5aa95a8d7c7
     https://github.com/hudson/hudson/commit/fbb54532d50a14c64a3de6a75d4cc5aa95a8d7c7
 Author: Olivier Lamy <olamy@apache.org>
 Date:   2011-01-13 (Thu, 13 Jan 2011)
 
 Changed paths:
   M changelog.html
 
 Log Message:
 -----------
 update changelog for HUDSON-8415
 
 
 Commit: 373397f561e0d5af6f67085fd26f64c8b1d3aa4c
     https://github.com/hudson/hudson/commit/373397f561e0d5af6f67085fd26f64c8b1d3aa4c
 Author: Olivier Lamy <olamy@apache.org>
 Date:   2011-01-13 (Thu, 13 Jan 2011)
 
 Changed paths:
   M core/src/main/resources/hudson/model/Messages_ja.properties
   A core/src/main/resources/hudson/security/FederatedLoginService/UnclaimedIdentityException/error_ja.properties
   M core/src/main/resources/hudson/security/HudsonPrivateSecurityRealm/_entryForm_ja.properties
   M core/src/main/resources/hudson/security/Messages_ja.properties
   A core/src/main/resources/lib/hudson/project/config-blockWhenDownstreamBuilding_ja.properties
   M core/src/main/resources/lib/hudson/project/config-blockWhenUpstreamBuilding_ja.properties
   A war/src/main/webapp/help/project-config/block-downstream-building_ja.html
   M war/src/main/webapp/help/project-config/block-upstream-building_ja.html
 
 Log Message:
 -----------
 Merge branch 'master' of github.com:hudson/hudson
 
 
 
 
  */
 }
