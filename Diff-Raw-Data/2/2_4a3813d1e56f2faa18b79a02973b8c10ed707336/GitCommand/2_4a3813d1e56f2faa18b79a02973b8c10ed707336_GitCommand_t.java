 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 
 package com.md87.charliebravo.commands;
 
 import com.dmdirc.util.Downloader;
 import com.md87.charliebravo.Command;
 import com.md87.charliebravo.Followup;
 import com.md87.charliebravo.InputHandler;
 import com.md87.charliebravo.Response;
 import java.net.URLEncoder;
 import java.nio.charset.Charset;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 /**
  *
  * @author chris
  */
 public class GitCommand implements Command {
 
     public void execute(InputHandler handler, Response response, String line) throws Exception {
         if (line.isEmpty()) {
             response.sendMessage("You need to specify a revision", true);
         } else {
             final List<String> result = Downloader.getPage(
                     "http://git.dmdirc.com/cgit.cgi/client/commit/?id="
                     + URLEncoder.encode(line, Charset.defaultCharset().name()));
             final StringBuilder builder = new StringBuilder();
 
             for (String resline : result) {
                 builder.append(resline);
             }
 
             if (builder.indexOf("<div class=\"error\">Bad object id:") > -1) {
                 response.sendMessage("That commit was not found", true);
             } else {
                 Matcher matcher = Pattern.compile("<th>author</th>"
                         + "<td>(.*?) &lt;(.*?)&gt;</td><td class='right'>(.*?)</td>")
                         .matcher(builder);
                 matcher.find();
                 final String authorName = matcher.group(1);
                 final String authorEmail = matcher.group(2);
                 final String commitDate = matcher.group(3);
 
                 matcher = Pattern.compile("<th>commit</th><td colspan='2' class='sha1'>"
                         + "<a href='/cgit.cgi/client/commit/\\?id=(.*?)'>").matcher(builder);
                 matcher.find();
                 final String commitHash = matcher.group(1);
 
                matcher = Pattern.compile("<div class='commit-subject'>(.*?)(<a .*?)?</div>")
                         .matcher(builder);
                 matcher.find();
                 final String commitSubject = matcher.group(1);
                 
                 matcher = Pattern.compile("<div class='diffstat-summary'>(.*?)</div>")
                         .matcher(builder);
                 matcher.find();
                 final String commitDiff = matcher.group(1);
 
                 response.sendMessage("commit " + commitHash + " was made by "
                         + authorName + " with message '" + commitSubject + "'");
                 response.addFollowup(new DiffFollowup(commitDiff, commitHash));
                 response.addFollowup(new DetailsFollowup(authorName, authorEmail,
                         commitHash, commitDate));
             }
             
         }
 
     }
 
     protected static class DiffFollowup implements Followup {
 
         private final String data;
         private final String hash;
 
         public DiffFollowup(String data, String hash) {
             this.data = data;
             this.hash = hash;
         }
 
         public boolean matches(String line) {
             return line.equalsIgnoreCase("diff");
         }
 
         public void execute(InputHandler handler, Response response, String line) throws Exception {
             response.setInheritFollows(true);
             response.sendMessage("there were " + data.replaceAll("^(.*), (.*?)$", "$1 and $2")
                     + " in commit "
                     + hash + ". See http://git.dmdirc.com/cgit.cgi/client/diff/?id=" + hash);
         }
 
     }
 
     protected static class DetailsFollowup implements Followup {
 
         private final String authorName, authorEmail, hash, time;
 
         public DetailsFollowup(String authorName, String authorEmail, String hash, String time) {
             this.authorName = authorName;
             this.authorEmail = authorEmail;
             this.hash = hash;
             this.time = time;
         }
 
         public boolean matches(String line) {
             return line.equalsIgnoreCase("details");
         }
 
         public void execute(InputHandler handler, Response response, String line) throws Exception {
             response.setInheritFollows(true);
             response.sendMessage("commit " + hash + " was made on " + time + " by "
                     + authorName + " <" + authorEmail + ">. " +
                     "See http://git.dmdirc.com/cgit.cgi/client/commit/?id=" + hash);
         }
 
     }
 
 }
