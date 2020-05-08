 package erki.xpeter.parsers;
 
 import java.util.TreeMap;
 
 import erki.api.util.Log;
 import erki.api.util.Observer;
 import erki.xpeter.Bot;
 import erki.xpeter.msg.Message;
 import erki.xpeter.msg.TextMessage;
 
 public class Sed implements Parser, Observer<TextMessage> {
     
     private TreeMap<String, String> lastSaid = new TreeMap<String, String>();
     
     @Override
     public void init(Bot bot) {
         bot.register(TextMessage.class, this);
     }
     
     @Override
     public void destroy(Bot bot) {
         bot.deregister(TextMessage.class, this);
     }
     
     @Override
     public void inform(TextMessage msg) {
         String text = msg.getText();
         String nick = msg.getNick();
         
        if (lastSaid.containsKey(nick) && text.startsWith("s") && text.length() > 1) {
             String delimiter = text.substring(1, 2);
             String rest = text.substring(2, text.length() - 1);
             
             if (text.endsWith(delimiter) && rest.contains(delimiter)) {
                 String regex = rest.substring(0, rest.indexOf(delimiter));
                 String replacement = rest.substring(rest.indexOf(delimiter) + 1);
                 Log.debug("Replacing " + regex + " with " + replacement + ".");
                 String result = lastSaid.get(nick).replaceAll(regex, replacement);
                 msg.respond(new Message(nick + " meinte: " + result));
                 return;
             }
         }
         
         lastSaid.put(nick, text);
     }
 }
