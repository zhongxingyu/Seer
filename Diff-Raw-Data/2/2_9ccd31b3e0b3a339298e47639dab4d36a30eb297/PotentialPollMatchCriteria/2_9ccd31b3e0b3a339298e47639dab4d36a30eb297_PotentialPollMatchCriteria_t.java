 package com.jreddit.pollingbot;
 
 import java.io.*;
 import java.util.*;
 import java.util.regex.*;
 
 import com.omrlnr.jreddit.*;
 import com.omrlnr.jreddit.utils.Utils;
 
 import com.jreddit.botkernel.*;
 
 /**
  *
  *  Matches posts that might potentially make a good poll.
  *  Posts back in its home reddit for humans to examine the posts.
  *
  */
 public class PotentialPollMatchCriteria extends BaseMatchCriteria {
                
     private static final int LIMIT = 10;
     
     private static final String SUGGESTIONS_TITLE = "poll suggestion";
 
     public PotentialPollMatchCriteria(PollingBot bot) {
         super(bot);
     }
 
     protected boolean matchBody(Thing thing, String body) {
         //
         // Check for potentials game requests which 
         // will need a human to look at.
         //
         Object lock = PersistenceUtils.getDatabaseLock();
         synchronized(lock) {
             if(PersistenceUtils.isBotReplied(thing.getId())) {
                 BotKernel.getBotKernel().log(
                         "FINEST Already matched:\n" + thing);
                 return false;
             }
             
             String pattern = 
                 "(who|what|when|where) (is|are|were) (.*)?(better|best|worse|worst)";
 
             Pattern r = Pattern.compile(pattern);
             Matcher m = r.matcher(body.toLowerCase());
             if(m.find()) { 
 
                 BotKernel.getBotKernel().log(
                         "INFO Found potential poll match in:\n" + body);
 
                 String subreddit = _bot.getSuggestionSub();
                 if(subreddit != null && !subreddit.equals("")) {
               
                     BotKernel.getBotKernel().log(
                                 "INFO Looking for suggestions thread");
 
                     try {
                         List<Submission> submissions =
                                 Submissions.getSubmissions(
                                                 _bot.getUser(),
                                                 subreddit,
                                                 Submissions.ListingType.HOT,
                                                 LIMIT,
                                                 (String)null,
                                                 (String)null );
 
                         for(Submission submission: submissions) {
 
                             if(submission.getTitle().toLowerCase().indexOf(SUGGESTIONS_TITLE) != -1) {
                                 BotKernel.getBotKernel().log(
                                             "INFO Found suggestions thread");
                                 String url = thing.getUrl();
                                 _bot.sendComment( submission, 
                                    "Potential poll comment in " +
                                     "/r/" + thing.getSubreddit() + "  \n\n" +
                                     "----\n" + 
                                     (url == null ? 
                                         submission.getUrl() : url) + "  \n\n" +
                                     body + "  \n\n");
                                 PersistenceUtils.setBotReplied(thing.getId());
                                 break;
                             }
                         }
 
                     } catch (IOException ioe) {
                         ioe.printStackTrace();
                         BotKernel.getBotKernel().log("ERROR caught " + ioe);
                     }
                 }
             }
         }
 
         //
         // Don't bother having the framework call back into the bot with
         // an event, since we can handle that here.
         //
         return false;
     }
 
 }
 
