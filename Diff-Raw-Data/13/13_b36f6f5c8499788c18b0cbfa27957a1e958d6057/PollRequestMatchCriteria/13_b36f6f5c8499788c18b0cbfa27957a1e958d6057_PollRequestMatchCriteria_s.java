 package com.jreddit.pollingbot;
 
 import java.io.*;
 import java.util.*;
 import java.util.regex.*;
 
 import com.omrlnr.jreddit.*;
 import com.omrlnr.jreddit.utils.Utils;
 
 import com.jreddit.botkernel.*;
 
 /**
  *
  *
  */
 public class PollRequestMatchCriteria extends BaseMatchCriteria {
 
     private static final int MAX_OPTIONS = 20;
 
     private static final int USER_LIMIT         = 5;
     private static final int SUBREDDIT_LIMIT    = 5;
 
     private static final long RESET_TIME = 1000 * 60 * 60 * 24;
 
 
     //
     // Try to protect from abuse.
     //
     private Map<String, CreationStat> _creationMap = 
                                         new HashMap<String, CreationStat>();
     private Map<String, CreationStat> _subredditMap = 
                                         new HashMap<String, CreationStat>();
 
     private static class CreationStat {
         private Date _lastActivity;
         private int _pollCount;
 
         public CreationStat(Date date) {
             _lastActivity = date;
         }
 
         public Date getLastActivity()           { return _lastActivity; }
         public void setLastActivity(Date date)  { _lastActivity = date; }
 
         public int getCount()       { return _pollCount; }
         public void setCount(int c) { _pollCount = c; }
     }
 
     public PollRequestMatchCriteria(PollingBot bot) {
         super(bot);
     }
 
     protected boolean matchBody(Thing thing, String body) {
         
         // Some basic checks
         if(thing.getAuthor() == null) {
             return false;
         }
 
         //
         // Check that we haven't already started a poll for this request
         //
         Object lock = PersistenceUtils.getDatabaseLock();
         synchronized(lock) {
 
             if(thing.getCreatedDate().before(_bot.getReplyAfterDate())) {
                 //
                 // log("Comment too old to check for poll request " +
                 //            thing.getCreatedDate());
                 return false;
             }
             
             //
             // This should match a poll request 
             //
             String pattern = 
                 ".*(http(s)?:\\/\\/([^\\s]+))(\\s+)([^\\?]+\\?)\\n+(([\\-\\*\\+])\\s+([^\\n])+\\n)+";
     
             Pattern r = Pattern.compile(pattern);
             Matcher m = r.matcher(body);
             if(m.find()) { 
 
                 BotKernel.getBotKernel().log("INFO " +
                         "Found poll request in: \n" + body);
                 String url = m.group(1);
                 String title = m.group(5);
 
                 pattern = "(([\\-\\*\\+])\\s+([^\\n]+))\\n";
                 r = Pattern.compile(pattern);
                 m = r.matcher(body);
 
                 List<PollOption> list = new ArrayList<PollOption>();
                 int i = 0;
                 while(m.find()) {
                     String value = m.group(3);
                     list.add(new PollOption(i, value));
                     if(i > MAX_OPTIONS) {
                         break;
                     }
                 }
 
                 //
                 // Get the comment or submission we are to reply to.
                 //
                 pattern = "(http(s)?:\\/\\/([^\\s]+))\\/comments\\/(\\w+)(\\/\\w+\\/?(\\w+)?)?(\\/)?(\\s+)";
                 r = Pattern.compile(pattern);
                 m = r.matcher(body);
 
                 if(m.find()) {
 
                     String submission = m.group(4);     // Submission
                     String comment = m.group(6);     // Comment
                     Thing replyTo = null;
 
                     User user = _bot.getUser();
 
                     try {
 
                         if(submission != null && comment == null) {
                                 replyTo = Submissions.getSubmission(
                                     user, Thing.KIND_LINK + "_" + submission);
                         }
                         if(submission != null && comment != null) {
                                 replyTo = Comments.getComment(
                                     user, Thing.KIND_COMMENT + "_" + comment);
                         }
 
                         if(replyTo != null) {
 
                             if(PersistenceUtils.isPoll(replyTo.getId())) {
                                 //
                                 // Already started a poll for this URL.
                                 //
                                 BotKernel.getBotKernel().log("INFO " +
                                     "Already started a poll for comment " + replyTo);
                                 _bot.sendComment(thing, 
                                     "Sorry but I have already created a " +
                                     "poll in response to that comment or " +
                                     "submission." );
 
                                 return false;
                             }
                             
                             CreationStat subredditStat = null;
                             CreationStat userStat = 
                                             _creationMap.get(thing.getAuthor());
 
                             if(userStat == null) {
                                 userStat = new CreationStat(new Date());
                                 userStat.setCount(0);
                                 _creationMap.put(thing.getAuthor(), userStat);
                             } else {
                                 Date d = new Date();
                                 long diff = 
                                     (d.getTime()) - 
                                     (userStat.getLastActivity().getTime());
 
                                 if(diff > RESET_TIME) {
                                     userStat.setLastActivity(new Date());
                                     userStat.setCount(0);
                                 }
 
                                 if(userStat.getCount() > USER_LIMIT) {
 
                                     _bot.sendComment(thing, 
                                         "Sorry but you have already created " +
                                         "too many polls today. " +
                                         "Come back in a day or so " +
                                         "and try again. ");
 
                                     return false;
                                 }
                             }
                            
                             if(!_bot.getUnlimitedSubreddits().contains(replyTo.getSubreddit())) {
                                 subredditStat = 
                                     _subredditMap.get(replyTo.getSubreddit());
 
                                 if(subredditStat == null) {
                                     subredditStat = 
                                                 new CreationStat(new Date());
                                     subredditStat.setCount(0);
                                     _subredditMap.put(  replyTo.getSubreddit(),
                                                         subredditStat);
                                 } else {
             
                                     Date d = new Date();
                                     long diff = 
                                         (d.getTime()) - 
                                         (subredditStat.getLastActivity().getTime());
 
                                     if(diff > RESET_TIME) {
                                         subredditStat.setLastActivity(
                                                                     new Date());
                                         subredditStat.setCount(0);
                                     }
 
                                     if(subredditStat.getCount() > USER_LIMIT) {
 
                                         _bot.sendComment(thing, 
                                             "Sorry but I have already " +
                                             "created too many polls in " +
                                             "that subreddit today. " + 
                                             "If you are a moderator of " +
                                             "that subreddit and you would " +
                                             "like for these restrictions to " +
                                             "be lifted for your subreddit " +
                                             "allowing unlimited PollingBot " +
                                             "polls, please contact my " +
                                             "human. Otherwise try again in " +
                                             "about a day or so.");
     
                                         return false;
                                     }
                                 }
                             }
 
                             PollOption[] options = 
                                 (PollOption[])list.toArray(new PollOption[0]);
 
                             //
                             // Create poll in db
                             //
                             int pollId = 
                                 PersistenceUtils.createPoll(replyTo.getId(),
                                                             title );
                             //
                             // Associate options
                             //
                             PersistenceUtils.setPollOptions( pollId, options );
 
                             //
                             // Generate pretty markdown for the poll
                             //
                             String text = _bot.generatePollText( 
                                             pollId,
                                             title,
                                             options, 
                                             new HashMap<Integer, Integer>() );
 
                             //
                             // Post the poll comment content
                             //
                             String commentId = _bot.sendComment(replyTo, text);
 
                             //
                             // Update poll in db with comment id
                             //
                             PersistenceUtils.setPollCommentId(
                                                     pollId, commentId );
                            
                             //
                             // Update our creation stats
                             //
                             userStat.setCount(userStat.getCount()+1);
                             userStat.setLastActivity(new Date());
 
                             if(subredditStat != null) {
                                 subredditStat.setCount(
                                                 subredditStat.getCount()+1);
                                 subredditStat.setLastActivity(new Date());
                             }
                         }
 
                     } catch(IOException ioe) {
                             ioe.printStackTrace();
                             BotKernel.getBotKernel().log("ERROR caught " + ioe);
                     }
                 }
             } else {
                 // BotKernel.getBotKernel().log("INFO no match in body: \n" +
                 //    body);
             }
         }
 
         return false;
     }
 
 }
 
 
 
 
