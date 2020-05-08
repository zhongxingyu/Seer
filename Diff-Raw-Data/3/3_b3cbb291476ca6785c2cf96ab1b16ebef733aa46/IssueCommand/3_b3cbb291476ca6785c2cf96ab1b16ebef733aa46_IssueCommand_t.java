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
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 /**
  *
  * @author chris
  */
 public class IssueCommand implements Command {
 
     public void execute(InputHandler handler, Response response, String line) throws Exception {
         if (line.isEmpty() || !line.matches("^[0-9]+$")) {
             response.sendMessage("You need to specify an issue number", true);
         } else {
             final List<String> result = Downloader.getPage("http://bugs.dmdirc.com/view.php?id="
                     + line);
             final StringBuilder builder = new StringBuilder();
 
             for (String resline : result) {
                 builder.append(resline);
             }
 
             if (builder.indexOf("APPLICATION ERROR #1100") > -1) {
                 response.sendMessage("That issue was not found", true);
             } else if (builder.indexOf("<p>Access Denied.</p>") > -1) {
                 response.sendMessage("that issue is private. Please see "
                         + "http://bugs.dmdirc.com/view/" + line);
             } else {
                 final Map<String, String> data = new HashMap<String, String>();
 
                 final Pattern pattern = Pattern.compile(
                         "<td class=\"category\".*?>\\s*(.*?)\\s*"
                         + "</td>\\s*(?:<!--.*?-->\\s*)?<td.*?>\\s*(.*?)\\s*</td>",
                         Pattern.CASE_INSENSITIVE + Pattern.DOTALL);
                 final Matcher matcher = pattern.matcher(builder);
 
                 while (matcher.find()) {
                     data.put(matcher.group(1).toLowerCase(), matcher.group(2));
                 }
 
                 response.sendMessage("issue " + data.get("id") + " is \""
                         + data.get("summary").substring(9) + "\". Current "
                         + "status is " + data.get("status") + " ("
                        + data.get("resolution") + "). See http://bugs.dmdirc.com/view/"
                        + data.get("id"));
                 response.addFollowup(new IssueFollowup(data));
             }
             
         }
 
     }
 
     protected static class IssueFollowup implements Followup {
 
         private final Map<String, String> data;
 
         public IssueFollowup(Map<String, String> data) {
             this.data = data;
         }
 
         public boolean matches(String line) {
             return data.containsKey(line.toLowerCase());
         }
 
         public void execute(InputHandler handler, Response response, String line) throws Exception {
             response.setInheritFollows(true);
             response.sendMessage("the " + line.toLowerCase() + " of issue "
                     + data.get("id") + " is: " + data.get(line.toLowerCase()));
         }
 
     }
 
 }
