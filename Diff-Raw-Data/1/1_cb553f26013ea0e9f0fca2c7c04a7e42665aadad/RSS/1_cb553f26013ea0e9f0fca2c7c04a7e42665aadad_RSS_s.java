 package addon.rss;
 
 import bashoid.Addon;
 import bashoid.Message;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 import utils.Config;
 
 import static utils.Constants.*;
 
 
 public class RSS extends Addon {
 
     private enum Cmds {
         INVALID, LIST, SHOW;
     };
     private static final String configKeyName = "channelName";
     private static final String configKeyUrl = "channelUrl";
     private static final String configKeyCount = "showMsgsCount";
     private static final int MESSAGE_MAX_LENGTH = 450;
 
     private List<Feed> feeds = new ArrayList<Feed>();
     private byte showMsgsCount;
     private boolean firstRun = true;
 
 
     public RSS() {
         setPeriodicUpdate(60000);
 
         Config config = new Config("rss.xml");
 
         for(short i = 1; true; ++i) {
             String name = config.getValue(configKeyName + i, null);
             String url = config.getValue(configKeyUrl + i, null);
             if(name == null || url == null)
                 break;
             feeds.add(new Feed(name, url));
         }
 
         feeds.add(new EzFeed());
 
         try {
             showMsgsCount = Integer.valueOf(config.getValue(configKeyCount, "5")).byteValue();
         } catch(NumberFormatException e) {
             showMsgsCount = 5;
         }
 
         checkFeeds();
     }
 
     private void checkFeeds() {
         List<String> msgs = null;
         for(Feed f : feeds) {
             try {
                 msgs = f.check(showMsgsCount);
                 if( !msgs.isEmpty() && !firstRun )
                     sendChainedMessages(f, msgs);
             } catch(IOException e) {
                 setError(e);
             }
         }
         firstRun = false;
     }
 
     private String executeCmd(Cmds cmd, String message, String author) {
         if(feeds.isEmpty())
             return "No channels available";
 
         switch(cmd) {
             case LIST:
             {
                 String msg = "";
                 for(Feed f : feeds)
                     msg += f.getName() + " ";
                 return msg;
             }
             case SHOW:
             {
                 String channel;
                 int index = message.indexOf(" ", message.indexOf("show"));
                 int end = message.indexOf(" ", index+1);
                 if(end == NOT_FOUND)
                     end = message.length();
 
                 channel = message.substring(index+1, end);
                 for(Feed f : feeds) {
                     if(channel.equalsIgnoreCase(f.getName())) {
                         List<String> messages = f.getLastMessages(showMsgsCount);
                         sendMessage(author, "Last " +  showMsgsCount + " messages for rss channel \"" + f.getName() + "\":");
                         for(String s : messages)
                             sendMessage(author, s);
                         break;
                     }
                 }
                 return null;
             }
         }
         return null;
     }
 
     private Cmds getCommand(String message) {
         int begin = message.indexOf(' ') + 1;
         int end = message.indexOf(' ', begin);
         if(end == NOT_FOUND)
             end = message.length();
 
         String cmd = message.substring(begin, end);
 
         if     (cmd.equals("list")) return Cmds.LIST;
         else if(cmd.equals("show")) return Cmds.SHOW;
         else                        return Cmds.INVALID;
     }
 
     private void sendChainedMessages(Feed feed, List<String> messages) {
         List<String> outputLines = new ArrayList<String>();
         String chain = feed.getName() + ": ";
         final String SEPARATOR = " | ";
         boolean isFirstMessageInChain = true;
 
         for (String message : messages) {
             if ( message.length() > MESSAGE_MAX_LENGTH ) {
                 sendMessageToChannels(chain);
                 sendMessageToChannels(message);
                 chain = "";
                 isFirstMessageInChain = true;
             } else if ( message.length() + chain.length() > MESSAGE_MAX_LENGTH ) {
                 sendMessageToChannels(chain);
                 chain = message;
                 isFirstMessageInChain = false;
             } else {
                 if (!isFirstMessageInChain)
                     chain += SEPARATOR;
                 chain += message;
             }
         }
 
         if ( chain.length() > 0 )
             sendMessageToChannels(chain);
     }
 
     @Override
     public boolean shouldReact(Message message) {
          return message.text.startsWith("rss") && getCommand(message.text) != Cmds.INVALID;
     }
 
     @Override
     protected void setReaction(Message message) {
         Cmds cmd = getCommand(message.text);
         if(cmd == Cmds.INVALID)
             return;
         String result = executeCmd(cmd, message.text, message.author);
         if(result != null)
             reaction.add(result);
     }
 
     @Override
     public void periodicAddonUpdate() {
         checkFeeds();
     }
 }
