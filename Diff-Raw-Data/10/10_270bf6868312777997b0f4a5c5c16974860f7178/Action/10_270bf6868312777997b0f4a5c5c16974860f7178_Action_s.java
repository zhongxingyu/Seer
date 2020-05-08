 package erki.xpeter.parsers;
 
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import erki.api.util.Log;
 import erki.api.util.Observer;
 import erki.xpeter.Bot;
 import erki.xpeter.BotInterface;
 import erki.xpeter.msg.Message;
 import erki.xpeter.util.BotApi;
 
 /**
  * This class represents an action that can be part of a parser derived from {@link SuperParser}. An
  * action is executed if the text of some message matches a given regular expression. Every action
  * reacts to exactly one regular expression.
  * 
  * @author Edgar Kalkowski <eMail@edgar-kalkowski.de>
  * @param <T>
  *        The type of message this action shall react upon.
  */
 public abstract class Action<T extends Message> implements Observer<T> {
     
     private Class<T> messageType;
     
     private final boolean mustAddress;
     
     private BotInterface bot;
     
     /**
      * Create a new Action.
      * 
      * @param messageType
      *        The type of message the new action shall react upon (sadly this parameter is required
      *        by Java because it cannot determine generic types at runtime).
      * @param mustAddress
      *        Specify whether or not this action shall only be executed if the bot is directly
      *        addressed (see {@link BotApi#addresses(String, String)} for details on how addressing
      *        is defined). If an action must not address the bot to be triggered it is also
      *        triggered when the bot is addressed.
      */
     public Action(Class<T> messageType, boolean mustAddress) {
         this.messageType = messageType;
         this.mustAddress = mustAddress;
     }
     
     /**
      * Register this action with a bot instance. This method is only used internally and should not
      * be called explicitely by someone who write actions.
      * 
      * @param bot
      *        The bot this action shall be registered with.
      */
     public void register(Bot bot) {
         this.bot = bot;
         bot.register(messageType, this);
     }
     
     /**
      * Deregister this action from a bot instance. This method is only used internally and should
      * not be called explicitely by someone who write actions.
      * 
      * @param bot
      *        The bot this action shall be deregistered from.
      */
     public void deregister(Bot bot) {
         bot.deregister(messageType, this);
     }
     
     /**
      * If the text of a message (of the correct message type) matches this regular expression this
      * action is triggered. The regular expression may contain pairs of parentheses (“(” and “)”).
      * The text between these parentheses is given as an argument to {@link #execute(String[])}.
      * 
      * @return The regular expression this action shall react upon.
      */
     public abstract String getRegex();
     
     /**
      * Get a human readable description of what this action does.
      * 
      * @return A string description of this action’s purpose.
      */
     public abstract String getDescription();
     
     /**
      * This method is called when the text of a message (of the correct type) received by the bot
      * matches {@link #getRegex()}.
      * 
      * @param args
      *        An array of arguments that contains the text between the special parentheses (see
      *        {@link #getRegex()}) in the regular expression. At position i stands the text between
      *        the i-th pair of parentheses.
      * @param message
      *        The message object that triggered this action.
      */
     public abstract void execute(String[] args, T message);
     
     /**
      * Access the bot instance this action belongs to.
      * 
      * @return This action’s bot instance.
      */
     public BotInterface getBot() {
         return bot;
     }
     
     /**
      * Check if the bot must be directly addressed for this action to be triggered.
      * 
      * @return {@code true} if the bot must be directly addressed, {@code false} otherwise.
      */
     public boolean mustAddress() {
         return mustAddress;
     }
     
     public void inform(T message) {
         String text = message.getText();
         String nick = message.getBotNick();
         
         if (mustAddress && !BotApi.addresses(text, nick)) {
             Log.debug(getClass().getSimpleName() + ": The bot is not addressed as required.");
             return;
         }
         
         if (BotApi.addresses(text, nick)) {
             text = BotApi.trimNick(text, nick);
         }
         
         if (getRegex() == null) {
             Log.warning(getClass().getSimpleName() + ": This action is defective!");
             return;
         }
         
         Pattern pattern = Pattern.compile(getRegex());
         Matcher matcher = pattern.matcher(text);
         
         if (matcher.matches()) {
             String[] groups = new String[matcher.groupCount()];
             
             for (int i = 0; i < groups.length; i++) {
                 groups[i] = matcher.group(i + 1);
             }
             
             execute(groups, message);
         } else {
             Log.debug(getClass().getSimpleName() + ": This action does not match.");
         }
     }
 }
