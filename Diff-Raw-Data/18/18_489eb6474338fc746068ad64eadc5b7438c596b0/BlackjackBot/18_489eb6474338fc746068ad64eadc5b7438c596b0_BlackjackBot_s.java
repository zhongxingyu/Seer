 package com.jreddit.casinobots;
 
 import java.io.*;
 import java.util.*;
 import java.util.regex.*;
 
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 
 import com.omrlnr.jreddit.*;
 import com.omrlnr.jreddit.utils.Utils;
 
 import com.simplecards.*;
 import com.jreddit.botkernel.*;
 
 /**
  *
  * Reddit BlackjackBot
  *
  */
 public class BlackjackBot extends AbstractCasinoBot 
                             implements Bot, CrawlerListener {
 
     //
     // String representing back of a card. I.e. card face not visible.
     //
     private static final String CARD_BACK = "██";
 
     //
     // Unique bot name
     //
     private static final String BOT_NAME = "BLACKJACK_BOT";
 
     //
     // Some sleep times we will use, based on activity.
     //
     private static final int LONG_SLEEP     = 30;
     private static final int SHORT_SLEEP    = 20;
     private static final int MICRO_SLEEP    = 10;
 
     //
     // Sleep transition threshold
     // How many short sleeps we will do before entering long sleep.
     // (I.e. before becoming "inactive".)
     //
     private static final int INACTIVITY_THRESHOLD = 8;
 
 
     //
     // Config files for our properties and
     // bans.
     // NOTE these paths are relative to the botkernel working directory,
     // as we do not run the bot from here.
     //
     private static final String BANS_FILE = 
                         "../casinobots/scratch/blackjackbot/bans.txt";
     private static final String CONFIG_FILE = 
                         "../casinobots/scratch/blackjackbot/config.properties";
 
     private BlackjackEngine _engine;
 
     private int _activeCycles;
 
     private Date _startTime;
 
     private List<String> _banList;
 
     //
     // Stats for this run
     //
     private int _gamesStarted   = 0;
     private int _gamesPlayed    = 0;
 
     /**
      *
      * Provide a default no argument constructor
      *
      */
     public BlackjackBot() { }
 
     /**
      *
      * Initialize our bot.
      *
      */
     public void init() {
 
         //
         // Use this to keep track of when we are shutting down
         //
         _shutdown = false;
         
         Properties props = new Properties();
         try {
             log("Loading blackjack config properties...");
             FileInputStream in = new FileInputStream(CONFIG_FILE);
             props.load(in);
             in.close();
         } catch(IOException ioe) {
             ioe.printStackTrace();
             log("ERROR init()'ing " + BOT_NAME);
         }
 
         initProps(props);
 
         _engine = new BlackjackEngine();
 
         _startTime      = new Date();
         _banList        = new ArrayList<String>();
 
         // Connect
         try {
             _user.connect();
         } catch(IOException ioe) {
             log("ERROR conecting user for " + BOT_NAME);
         }
 
         //
         // Init bans from file.
         //
         PersistenceUtils.loadList(BANS_FILE, _banList, -1);
         log("Loaded bans:               " + _banList.size()  );
 
         _crawler = CasinoCrawler.getCrawler();
 
         //
         // Register ourselves with the Crawler
         //
         _crawler.addListener(this);
 
         //
         // Create a match criteria for the crawler to notify us
         // when we need to respond to a post.
         //
         CrawlerMatchCriteria criteria = new CrawlerMatchCriteria() {
                 
                 public boolean match(Thing thing) {
 
                     String body = null;
 
                     //
                     // Do not consider my own posts as criteria
                     //
                     String author = thing.getAuthor();
                     if( author != null &&
                         author.equals(_user.getUsername())) {
 
                         // log("Ignoring my own comment " + thing.getName());
                         return false;
                     }
 
                     if(thing instanceof Comment) {
                         Comment comment = (Comment)thing;
                         if(comment.getBody() != null) {
                             body = ((Comment)comment).getBody().toLowerCase();
                         }
                     }
 
                     if(thing instanceof Submission) {
                         Submission submission = (Submission)thing;
                         if( submission.isSelfPost() &&
                             submission.getSelftext() != null) {
                             
                             body = submission.getSelftext().toLowerCase();
                         }
                     }
 
                     if(body == null) {
                         // Still nothing at this point, it's not a match.
                         return false;
                     }
 
                     if(body.indexOf("blackjack") != -1) {
 
                         //
                         // We match.
                         // Check that we
                         // have not already matched, and therefore
                         // already replied, to this comment.
                         //
                         if(!PersistenceUtils.isBotReplied(
                                 BOT_NAME, thing.getName())) {
 
                             return true; 
                         }
                     }
 
                     return false;
                 }
 
                 public CrawlerListener getCrawlerListener() {
                     return BlackjackBot.this;
                 }
             };
 
         //
         // Add out match criteria to the crawler.
         //
         _crawler.addMatchCriteria(criteria);
 
         //
         // Register the crawler with the kernel.
         //
         BotKernel.getBotKernel().addCrawler(_crawler);
 
     }
 
     /**
      *
      * Return our unique name.
      *
      */
     public String getName() {
         return BOT_NAME;
     }
 
     /**
      *
      * Called when shutting down.
      *
      */
     public void shutdown() {
 
         //
         // Indicate to sleeping threads that we need to shut down.
         //
         _shutdown = true;
     }
 
 
 
     /** 
      *
      * Main game loop. Check for players responding to games,
      * and then respond back to player. 
      *
      */
     public void run() {
 
         Date lastActivity = new Date();
 
         //
         // Connect
         //
         try {
             _user.connect();
         } catch (IOException ioe) {
             ioe.printStackTrace();
             log("Error cannot connect.");
             return;
         }
 
         //
         // Start our cycle count for inactivity checking.
         //
         _activeCycles = 0;
 
         /**
          *  Main loop
          */
         while(true) {
 
             if(_shutdown) {
                 return;
             }
 
             try {
 
                 //
                 // Handle any existing games.
                 //
                 List<Message> messages = Messages.getMessages(
                                                 _user, 
                                                 Messages.MessageType.UNREAD );
                 continueGames(messages);
 
 
                 //
                 // DEBUG
                 //
                 // break;
                 
                 // log("Active cycles " + _activeCycles + 
                 //    ", deep sleep threshold " + INACTIVITY_THRESHOLD);
 
                 int sleep = SHORT_SLEEP;
                 if(messages.size() > 0) {
 
                     // log("There is activity. Resetting activeCycles count.");
                     //
                     // reset activeCycles.
                     //
                     _activeCycles = 0;
                     lastActivity = new Date();
 
                     log("Preparing for micro sleep...");
                     sleep = MICRO_SLEEP;
 
                 } else {
 
                     //
                     // If we've exceeded the active cycle threshold
                     // become "inactive" for long sleep.
                     //
                     if(_activeCycles >= INACTIVITY_THRESHOLD) {
                         // Default sleep time between checking for games.
                         log("Preparing for long sleep...");
                         sleep = LONG_SLEEP;
                     } else {
                         log("Preparing for short sleep...");
                     }
 
                 }
 
 
                 //
                 // log stats
                 //
                 log("");
                 log("Banned subs:   [" + getBanListAsString() + "]");
                 log("Games started: " + _gamesStarted );
                 log("Games played:  " + _gamesPlayed );
                 log("Running since: " + DATE_FORMAT.format(_startTime) );
                 log("Last activity: " + DATE_FORMAT.format(lastActivity) );
 
                 log("Sleeping...");
                 sleep(sleep);
 
                 _activeCycles++;
 
             } catch(RateLimitException rle) {
 
                 // rle.printStackTrace();
                 log("Caught RateLimitException: " + rle.getMessage());
 
                 int sleepSecs = rle.getRetryTime();
 
                 log("Sleeping " + sleepSecs + 
                     " seconds to recover from rate limit exception...");
 
                 sleep(sleepSecs);
 
             } catch(IOException ioe) {
                 ioe.printStackTrace();
                 // log("ERROR, EXITING!!!");
                 // System.exit(1);
             }
 
         }
 
     }
 
     /**
      *
      * Implement CrawlerListener.
      *
      * This should be a new game request.
      *
      */
     public void handleCrawlerEvent(CrawlerEvent event) {
 
         if(event.getType() != CrawlerEvent.CRAWLER_MATCH) {
             return;
         }
 
         Thing thing = event.getSource();
 
         // 
         // This is a crawler hit, so we are starting a new game.
         //
 
         //
         // Are we banned from the subreddit? If yes, skip it.
         //
         if(_banList.contains(thing.getSubreddit())) {
             log("Ignoring request in BANNED sub " + thing.getSubreddit());
             return;
         }
 
         if(!commonCrawlerEventChecks(thing)) {
             // Common crawler event checks have failed. 
             // We do not want to reply to this post or submission.
             return;
         }
 
         String body = null;
 
         if(thing instanceof Comment) {
             Comment comment = (Comment)thing;
             if(comment.getBody() != null) {
                 body = ((Comment)comment).getBody().toLowerCase();
                 log("Starting game from comment with: " + body);
             }
         }
 
         if(thing instanceof Submission) {
             Submission submission = (Submission)thing;
             if( submission.isSelfPost() &&
                 submission.getSelftext() != null) {
                             
                 body = submission.getSelftext().toLowerCase();
                 log("Starting game from submission with: " + body);
             }
         }
 
         if(body == null) {
             return;
         }
 
         //
         // Still allow for backwards compatibility and playing without 
         // betting in the casino.
         //
         int bet = -1;
 
         //
         // Check for a bet only in subreddits in which we will 
         // support that feature
         //
         if(CasinoCrawler.getCrawler().containsSubreddit(thing.getSubreddit())) {
 
             String pattern = "blackjack(bot)? (\\d+)";
             Pattern r = Pattern.compile(pattern);
             Matcher m = r.matcher(body);
             if(m.find()) {
                 try {
                     bet = Integer.parseInt(m.group(2));
                 } catch(NumberFormatException nfe) {
     
                 }
             }
         }
 
         String author = thing.getAuthor();
 
         String message = "";
 
         log("Starting new game with user " + author + " bet " + bet);
 
         Object dbLock = PersistenceUtils.getDatabaseLock();
         synchronized(dbLock) {
 
             boolean sufficientFunds = false;
 
             int bal = -1;
             if(bet != -1) {
                 bal = PersistenceUtils.getBankBalance(author);
             }
 
             if(bet == -1 || bal >= bet) {
 
 
                 //
                 // Get the dealer's hand
                 //
                 BlackjackHand dealerHand = 
                     new BlackjackHand( 
                             new BlackjackCard[] { _engine.dealCard() } );
          
                 //
                 // Get the player's hand
                 //
                 BlackjackHand playerHand = (BlackjackHand)_engine.dealHand();
         
                 //
                 // Create message for player
                 //
                 message = createGameOutput( dealerHand, playerHand, 
                                             author, bet);
            
                 //
                 // See if we just dealt them a blackjack.
                 // If yes, they win.
                 // Append message to player with winner info.
                 //
                 if(playerHand.isBlackjack()) {
                     message += "    ...  \n";
                     message += "    Game over. You win!  \n";
       
                     if(bet != -1) {
                         int oldBal = PersistenceUtils.getBankBalance(
                                                                 author );
                         PersistenceUtils.setBankBalance(author, 
                                                             oldBal + (bet*2));
                     }
                 }
                 
                 sufficientFunds = true;
 
             } else {
 
                 sufficientFunds = false;
 
                 message = 
                     "    You do not have sufficient funds to bet " + bet + 
                     " credit(s).  \n" +
                     "    Try betting fewer credits or try " +
                     "\"bankerbot credits\" to be granted credits.  \n";
             }
 
             //
             // Reply to player.
             //
             try {
 
                 if(bet != -1 && sufficientFunds) {
                     PersistenceUtils.setBankBalance(author, bal-bet);
                 }
 
                 if(bet == -1) {
                     //
                     // Don't pass author to sendComment() if we are not
                     // playing for credits.
                     //
                     author = null;
                 }
 
                 sendComment(thing, message, author);
 
                 _gamesStarted++;
 
             } catch(DeletedCommentException dce) {
 
                 log("Ignoring deleted item... " + thing);
 
             } catch(BannedUserException ioe) {
 
                 String subreddit = thing.getSubreddit();
                 addBan(subreddit);
 
             } catch(IOException ioe) {
 
                 ioe.printStackTrace(); 
 
                 //
                 // Some other error replying to comment...
                 //
                 log("ERROR replying to:\n" + thing);
             }
         }
 
         //
         // Mark player as replied to start game, so that we
         // do not reply again to this same game request.
         // Or at least we tried to reply to this. 
         // Might have failed with some error like "TOO_OLD"
         // or we were banned.
         // Anyway mark it.
         //
         PersistenceUtils.setBotReplied(BOT_NAME, thing.getName());
  
     }
 
 
     /**
      *
      * Continue in-progress games.
      *
      */
     private void continueGames(List<Message> messages) throws IOException {
         
         if(messages.size() == 0) {
             return;
         }
 
         log("Continuing " + messages.size() + " existing game(s)...");
         
         for(Message message: messages) {
             
             Date d = message.getCreatedDate();
 
             log("Checking post date " + d + " is later than start date " 
                 + _replyAfterDate);
 
             if(d == null) {
                 Messages.markAsRead(_user, message);
                 continue;
             }
             if(d.before(_replyAfterDate)) {
                 //
                 // NOTE
                 // This message occurred before we started running.
                 // Or in other words, while we were offline, assuming
                 // we went down. 
                 //
                 // In the event that the database was destroyed since 
                 // our last start, ignore these messages in order to avoid
                 // massive spamming 
                 //
                 // Ignore this. 
                 //
                 // log("Ignoring old message from " + d);
                 Messages.markAsRead(_user, message);
                 continue;
             }
 
             log("Checking subreddit " + message.getSubreddit());
 
             //
             // Are we banned from here? If yes, skip it.
             //
             String subreddit = message.getSubreddit();
             if( subreddit != null && _banList.contains(subreddit) ) {
 
                 log("Ignoring reply in BANNED sub " + subreddit);
 
                 Messages.markAsRead(_user, message);
                 continue;
             }
 
 
             String parentId = message.getParentId();
             String body = message.getBody().toLowerCase().trim();
 
             if(subreddit == null) {
 
                 //
                 // This is probably a PM. Could also check for t4 kind
                 // thing.getKind() == KIND_MESSAGE
                 // parent will not be null if it is a reply to another 
                 // PM. I.e. part of a PM conversation, only initial PM has
                 // no parent id.
 
                 //
                 // Enhancement: allow communication with the bot
                 // over PM. E.g.
                 // "addsub <subreddit>"
                 //
 
                 // Could be a banned message. E.g.
                 // "you have been banned from posting to [/r/IAmA:"
                 // "/r/WTF"
                 // Should try to parse this and remove the subreddit from
                 // our crawl??? We already handle the 
                 // 403 BannedUserException so maybe this is not necessary.
 
                 Messages.markAsRead(_user, message);
                 continue;
             }
 
             //
             // Do not play over PMs.
             //
             if(message.getKind().equals(Thing.KIND_MESSAGE)) {
                 log("Not playing over PM for\n" + message);
                 Messages.markAsRead(_user, message);
                 continue;
             }
 
             log("Checking PersistenceUtils.isBotReplied " + message.getName());
 
             //
             // Ensure I do not reply to messages more than once.
             //
             if(PersistenceUtils.isBotReplied(BOT_NAME, message.getName())) {
                 log("Not playing already replied to message\n" + message);
                 Messages.markAsRead(_user, message);
                 continue;
             }
 
             log("Getting parent comment to obtain game state.");
 
             //
             // Get game state
             //
             Comment parent = Comments.getComment(_user, parentId);
 
             if( parent == null ) {
 
                 //
                 // Can't parse game state. Nothing to do.
                 // Could maybe inform user that comment was deleted???
                 //
 
                 Messages.markAsRead(_user, message);
                 continue;
             }
          
             String parentBody = parent.getBody();
 
             if( parentBody == null ) {
 
                 //
                 // Can't parse game state. Nothing to do.
                 // Could maybe inform user that comment was deleted???
                 //
 
                 Messages.markAsRead(_user, message);
                 continue;
             }
 
             log("Checking for finished game.");
 
            if( parentBody != null &&
                parentBody.indexOf("Game over.") != -1) {
 
                 //
                 // This game is ended. Why are they still replying to us?
                 //
                 log("Skipping already finished game for reply: " + body
                     + " (author: " + message.getAuthor() + ")" );
 
                 Messages.markAsRead(_user, message);
                 continue;
             }
 
             String player = null;
             int bet = -1;
 
             log("Looking for player info.");
 
             //
             // Parse player and bet.
             //
             String pattern = "Player: (\\S+) bet: (\\d+) credit";
             Pattern r = Pattern.compile(pattern);
             Matcher m = r.matcher(parentBody);
             if(m.find()) {
                 player = m.group(1);
                 try {
                     bet = Integer.parseInt(m.group(2));
                 } catch( NumberFormatException nfe ) {
                     // Not much to do here, check for -1 as bet later.
                 }
             }
 
             log("Found player and bet " + player + " " + bet);
 
             if( player == null || bet == -1 ) {
                 // 
                 // We must not be playing a game for credits.
                 //
                 log("Playing game without credit bet for:\n" + parentBody);
 
             } else {
 
                 //
                 // We are playing a game for credits.
                 // Ensure this is the correct user to reply to and that we
                 // have not already played this turn.
                 //
              
                 if(PersistenceUtils.isBotReplied(BOT_NAME, parent.getName())) {
                     log("Already played this turn for " + message);
                     Messages.markAsRead(_user, message);
                     continue;
                 }
 
                 if(!message.getAuthor().equals(player)) {
                     //
                     // This is not a reply from the player, ignore it.
                     //
                     Messages.markAsRead(_user, message);
                     continue;
                 }
             }
 
             BlackjackHand dealerHand = parseHand(   parent.getBody(),
                                                     PARSE_DEALER_SECTION); 
             BlackjackHand playerHand = parseHand(   parent.getBody(),
                                                     PARSE_PLAYER_SECTION); 
 
             log("Playing game " + "(" + message.getSubreddit() + ")" );
             log("   " + dealerHand);
             log("   " + playerHand);
 
 
             // This is kludgy, but not going to bother right now with 
             // more advanced natural language processing than this
             // for now.
            
             if( body.equals("hit") ||
                 body.startsWith("hit") ||
                 body.indexOf("hit me") != -1) {
 
                 log("    Player hits.");
 
                 playerHand = (BlackjackHand)_engine.dealCard(playerHand);
 
                 String output = createGameOutput(   dealerHand, 
                                                     playerHand,
                                                     player,
                                                     bet );
                 if(playerHand.isBusted()) {
                     output += "    ...  \n";
                     output += "    Game over. You lose.  \n";
                 }
 
                 log("   " + dealerHand);
                 log("   " + playerHand);
 
                 //
                 // Reply to player
                 //
                 try {
 
                     if(bet == -1) {
                         // Don't send player info to sendComment() if
                         // we are not playing for credits.
                         player = null;
                     }
 
                     sendComment(message, output, player);
                     if(bet != -1) {
                         PersistenceUtils.setBotReplied( BOT_NAME, 
                                                         parent.getName());
                     }
                     PersistenceUtils.setBotReplied(BOT_NAME, message.getName());
                     _gamesPlayed++;
 
                 } catch(DeletedCommentException dce) {
 
                     log("Ignoring deleted comment... " + message);
 
                 } catch(BannedUserException ioe) {
 
                     addBan(subreddit);
                 } 
 
                 //
                 // We are done.
                 // Mark this message as read.
                 //
                 Messages.markAsRead(_user, message);
 
                 //
                 // Continue to next game.
                 //
                 continue;
             }
  
             // 
             // Check for stay/stand
             // 
          
             if( body.equals("stay") ||
                 body.equals("stand") ||
                 body.startsWith("stay") ||
                 body.startsWith("stand") ||
                 body.startsWith("thats good") ||
                 body.startsWith("that's good") ||
                 body.startsWith("thats enough") ||
                 body.startsWith("that's enough") ||
                 body.startsWith("im good") ||
                 body.startsWith("i'm good") ) {
 
                 log("    Player stays.");
 
                 //
                 // We'll player dealer stands on all 17's
                 // http://www.predictem.com/blackjack/dealer.php
                 //
 
                 //
                 // Flip dealer card.
                 //
                 dealerHand = (BlackjackHand)_engine.dealCard(dealerHand);
                 String output = createGameOutput(   dealerHand, 
                                                     playerHand,
                                                     player,
                                                     bet );
 
                 log("   ...");
                 log("   " + dealerHand);
                 log("   " + playerHand);
 
                 while(!dealerHand.isBusted()) {
                     //
                     // Check the current state of the dealer's hand.
                     // If they have 17 or more, stop hitting, break
                     // and check scores.
                     //
                     if(dealerHand.getValues()[0].intValue() >= 17) {
                         break;
                     }
 
                     //
                     // Reached here. Dealer must hit.
                     //
                     output += "    ...  \n";
                     output += "    Dealer must hit.  \n";
                     output += "    ...  \n";
 
                     dealerHand = (BlackjackHand)_engine.dealCard(dealerHand);
                     output += createGameOutput( dealerHand, 
                                                 playerHand,
                                                 null,
                                                 -1 );
                     
                     log("   ...");
                     log("   " + dealerHand);
                     log("   " + playerHand);
                 }
 
                 if(dealerHand.isBusted()) {
                     //
                     // Dealer busts
                     //
                     output += "    ...  \n";
                     output += "    Game over. You win!  \n";
 
                     Object dbLock = PersistenceUtils.getDatabaseLock();
                     if(bet != -1) {
                         synchronized(dbLock) {
                             int oldBal = PersistenceUtils.getBankBalance(
                                                                 player );
                             PersistenceUtils.setBankBalance(player, 
                                                             oldBal + (bet*2));
                         }
                     }
 
                 } else {
                     //
                     // Check scores.
                     //
                     int playerVal = playerHand.getValues()[0].intValue();
                     int dealerVal = dealerHand.getValues()[0].intValue();
                     if(playerVal == dealerVal) {
 
                         // Push
 
                         output += "    ...  \n";
                         output += "    Game over. Push.  \n";
 
                         Object dbLock = PersistenceUtils.getDatabaseLock();
                         if(bet != -1) {
                             synchronized(dbLock) {
                                 int oldBal = PersistenceUtils.getBankBalance(
                                                                 player );
                                 PersistenceUtils.setBankBalance(player, 
                                                             oldBal + (bet));
                             }
                         }
 
                     } else {
 
                         // Player wins
 
                         if(playerVal > dealerVal) {
                             output += "    ...  \n";
                             output += "    Game over. You win!  \n";
                    
                             Object dbLock = PersistenceUtils.getDatabaseLock();
                             if(bet != -1) {
                                 synchronized(dbLock) {
                                     int oldBal = 
                                         PersistenceUtils.getBankBalance(
                                                                 player );
                                     PersistenceUtils.setBankBalance(player, 
                                                             oldBal + (bet*2));
                                 }
                             }
 
                         } else {
 
                             // Player loses
 
                             output += "    ...  \n";
                             output += "    Game over. You lose.  \n";
                         }
                     }
                 }
 
                 //
                 // Send the output game state to the user.
                 //
                 try {
                     
                     if(bet == -1) {
                         // Don't send player to sendCOmment() if we are
                         // not playing for credits.
                         player = null;
                     }
 
                     sendComment(message, output, player);
                     if(bet != -1) {
                         PersistenceUtils.setBotReplied( BOT_NAME, 
                                                         parent.getName());
                     }
                     PersistenceUtils.setBotReplied(BOT_NAME, message.getName());
                     _gamesPlayed++;
 
                 } catch(DeletedCommentException dce) {
 
                     log("Ignoring deleted comment... " + message);
 
                 } catch(BannedUserException ioe) {
 
                     addBan(subreddit);
 
                 } 
 
                 //
                 // We are done.
                 // Mark this message as read.
                 //
                 Messages.markAsRead(_user, message);
 
                 //
                 // Continue to next game.
                 //
                 continue;
  
             }
 
             //
             // TODO add split? add double down? etc...
             //
 
             log("    Unknown command.");
 
             //
             // No supported actions (hit, stay) were detected.
             // Send the output 
             //
             String output = createGameOutput(   dealerHand, 
                                                 playerHand, 
                                                 player, 
                                                 bet         );
             output += "    ...  \n";
             output += "    Sorry, I don't understand. " +
                         "Try 'hit' or 'stand'.  \n";
             
             try {
 
                 if(bet == -1) {
                     // Don't send player info to sendComment() if
                     // we are not playing for credits.
                     player = null;
                 }
 
                 sendComment(message, output, player);
                 if(bet != -1) {
                     PersistenceUtils.setBotReplied( BOT_NAME, 
                                                     parent.getName());
                 }
                 PersistenceUtils.setBotReplied(BOT_NAME, message.getName());
 
             } catch(DeletedCommentException dce) {
 
                 log("Ignoring deleted comment... " + message);
 
             } catch(BannedUserException ioe) {
 
                 addBan(subreddit);
 
             } 
 
 
             //
             // No supported actions (hit, stay) were detected.
             // Mark message as read and move on.
             //
             Messages.markAsRead(_user, message);
 
         }
 
     }
 
     private static final int PARSE_DEALER_SECTION = 0;
     private static final int PARSE_PLAYER_SECTION = 1;
 
     /**
      * @param text      Game text
      * @param parseSec  The section of the text to parse. Either one of
      *                  PARSE_DEALER_SECTION or PARSE_PLAYER_SECTION
      */
     private BlackjackHand parseHand(String text, int parseSec) {
         String line = 
             text.split("\\r?\\n")[parseSec].split(": ")[1].split(" \\(")[0];
        
         String[] strCards = null;
 
         if( line.startsWith(CARD_BACK) ||
             line.startsWith("▩▩") ||
             line.startsWith("██") ||
             line.startsWith("??") ) {
 
             line = line.split(" ")[1];
             strCards = new String[] { line };
         } else {
             strCards = line.split(" ");
         }
 
         return (BlackjackHand)_engine.parseCards(strCards);
     }
 
     /**
      *
      * Draw the board. This will be the text of the post 
      * we reply to the user with.
      */
     private String createGameOutput(Hand dealerHand, 
                                     Hand playerHand, 
                                     String player,
                                     int bet ) {
 
         String ret = "";
         if(dealerHand.getCards().length == 1) {
             ret += "    Dealer hand: " + CARD_BACK + " " + dealerHand + "  \n";
         } else {
             ret += "    Dealer hand: " + dealerHand + "  \n";
         }
         ret += "    Player hand: " + playerHand + "  \n";
 
         if( player != null && bet != -1 ) {
             ret += "  \n";
             ret += "    Player: " + player + " bet: " + bet + " credit(s)\n";
         }
 
         return ret;
     }
 
    
     /**
      * Send a comment, append the bot's signature.
      */
     private void sendComment(Thing thing, String text, String player) 
                                                     throws IOException {
         int bal = -1; 
         if(player != null) { 
             Object dbLock = PersistenceUtils.getDatabaseLock();
             synchronized(dbLock) {
                 bal = PersistenceUtils.getBankBalance( player );
             }
 
             if(bal > 0) {
                 text += "\n\n" +
                     "|Credits\n" + 
                     "|---:\n" + 
                     "|" + bal + "\n";
             } else {
                 text += "\n\n" +
                     "|Credits\n" + 
                     "|---:\n" + 
                     "|0\n";
             }
         }
 
         text += "\n\n" +
                 "----\n" +
                 "Commands: hit, stand | " +
                 "[Visit Casino](/r/RoboCasino) | " +
                 "[Contact My Human](http://www.reddit.com/message/compose/?to=BlackjackPitboss)    ";
                 // +
                 // "\n\n" +
                 // "^^Please ^^remember ^^to ^^tip ^^your ^^dealer  \n";
 
         Comments.comment(_user, thing, text);
     }
 
     /**
      *
      * Add a subreddit to our ban set.
      *
      */
     private synchronized void addBan(String subreddit) {
         log("Adding ban for: " + subreddit);
         _banList.add(subreddit);
 
         PersistenceUtils.saveList(BANS_FILE, _banList);
     }
 
     /**
      * Return a String representation of the ban set.
      */
     private String getBanListAsString() {
         String ret = "";
         for(String subreddit: _banList) {
             if(ret.equals("")) {
                 ret = subreddit;
             } else {
                 ret += ", " + subreddit;
             }
         }
         return ret;
     }
 
 
 
 }
