 package erki.xpeter.parsers;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutputStream;
 import java.util.LinkedList;
 import java.util.TreeMap;
 import java.util.TreeSet;
 
 import erki.api.util.Log;
 import erki.api.util.Observer;
 import erki.xpeter.Bot;
 import erki.xpeter.msg.DelayedMessage;
 import erki.xpeter.msg.TextMessage;
 import erki.xpeter.util.BotApi;
 
 public class SimpleLearner implements Parser, Observer<TextMessage> {
     
     private static final String CONFIG_FILE = "config" + File.separator + "knowledge";
     
     private TreeMap<String, TreeSet<String>> knowledge = new TreeMap<String, TreeSet<String>>();
     
     @Override
     public void init(Bot bot) {
         load();
         bot.register(TextMessage.class, this);
     }
     
     @Override
     public void destroy(Bot bot) {
         bot.deregister(TextMessage.class, this);
     }
     
     @SuppressWarnings("unchecked")
     private void load() {
         
         try {
             ObjectInputStream fileIn = new ObjectInputStream(new FileInputStream(CONFIG_FILE));
             knowledge = (TreeMap<String, TreeSet<String>>) fileIn.readObject();
             fileIn.close();
             
             for (String key : knowledge.keySet()) {
                 Log.debug("Loaded knowledge mapping “" + key + "” → “" + knowledge.get(key) + "”");
             }
             
             Log.info("Knowledge base successfully loaded from " + CONFIG_FILE + ".");
         } catch (FileNotFoundException e) {
             Log.info("Knowledge base could not be loaded because the file " + CONFIG_FILE
                     + " could not be found.");
             Log.info("Trying to learn something anyway.");
         } catch (IOException e) {
             Log.warning("An error occurred while trying to load the knowledge base from "
                     + CONFIG_FILE + ".");
             Log.info("Trying to learn something anyway.");
         } catch (ClassNotFoundException e) {
             Log.warning("Knowledge base could not be loaded because a necessary class was not "
                     + "found.");
             Log.info("Trying to learn something anyway.");
         }
     }
     
     private void save() {
         
         try {
             ObjectOutputStream fileOut = new ObjectOutputStream(new FileOutputStream(CONFIG_FILE));
             fileOut.writeObject(knowledge);
             fileOut.close();
             Log.info("Knowledge base successfully stored to " + CONFIG_FILE + ".");
         } catch (FileNotFoundException e) {
             Log.warning("Knowledge base could not be stored because the file " + CONFIG_FILE
                     + " could not be found!");
         } catch (IOException e) {
             Log.warning("An error occurred while storing the knowledge base to " + CONFIG_FILE
                     + "!");
         }
     }
     
     @Override
     public void inform(TextMessage msg) {
         String text = msg.getText();
         String nick = msg.getBotNick();
         boolean addresses = false;
         
         if (BotApi.addresses(text, nick)) {
             addresses = true;
             text = BotApi.trimNick(text, nick);
         }
         
         for (String verb : verbs()) {
             
             if (text.toLowerCase().contains(" " + verb + " ")) {
                 String key = text.substring(0, text.toLowerCase().indexOf(verb) - 1);
                 String value = text.substring(text.toLowerCase().indexOf(verb) + verb.length() + 1);
                 Log
                         .debug("Recognized knowledge: “" + key + "” → “" + verb + "” → “" + value
                                 + "”.");
                 
                 if (!this.knowledge.containsKey(key)) {
                     this.knowledge.put(key, new TreeSet<String>());
                 }
                 
                 this.knowledge.get(key).add(verb + " " + value);
                 save();
                 
                 if (addresses) {
                     msg.respond(new DelayedMessage("Ok, weißsch Bescheid.", 2000));
                     return;
                 }
                 
                 break;
             }
         }
         
         String query = "[wW]as (wei(ss|ß)t [dD]u|kannst [dD]u sagen) (ü|ue)ber (.*?)";
         
         if (addresses && text.matches(query)) {
             String key = text.replaceAll(query, "$4");
             String subkey = key.substring(0, key.length() - 1);
             Log.debug("Recognized query for “" + key + "”.");
             
             if (this.knowledge.containsKey(key) || this.knowledge.containsKey(subkey)) {
                 TreeSet<String> hits = this.knowledge.containsKey(key) ? this.knowledge.get(key)
                         : this.knowledge.get(subkey);
                 String hit = hits.toArray(new String[0])[(int) (Math.random() * hits.size())];
                 msg.respond(new DelayedMessage(this.knowledge.containsKey(key) ? key : subkey + " "
                         + hit, 3000));
                 return;
             } else {
                 String txt = "";
                int number = 5;
                 
                if (Math.random() > 1.0 / number) {
                     txt = "Kein Plan.";
                } else if (Math.random() > 2.0 / number) {
                     txt = "Davon weiß ich nichts.";
                } else if (Math.random() > 3.0 / number) {
                     txt = "Keine Ahnung.";
                 } else {
                     txt = "Darüber weiß ich nichts.";
                 }
                 
                 msg.respond(new DelayedMessage(txt, 3000));
                 return;
             }
         }
         
         boolean match = false;
         
         for (String key : this.knowledge.keySet()) {
             
             if (text.contains(key)) {
                 Log.debug("Someone mentioned “" + key + "”.");
                 match = true;
                 TreeSet<String> hits = this.knowledge.get(key);
                 String hit = hits.toArray(new String[0])[(int) (Math.random() * hits.size())];
                 
                 if (addresses) {
                     msg.respond(new DelayedMessage(key + " " + hit, 3000));
                 } else {
                     
                     // Also respond seldomly if not directly addressed.
                     if (Math.random() < 0.1) {
                         msg.respond(new DelayedMessage(key + " " + hit, 2000));
                     }
                 }
             }
         }
         
         if (addresses && !match) {
             String txt = "";
             int number = 5;
             
             if (Math.random() > 1.0 / number) {
                 txt = "Kein Plan.";
             } else if (Math.random() > 2.0 / number) {
                 txt = "Davon weiß ich nichts.";
             } else if (Math.random() > 3.0 / number) {
                 txt = "Keine Ahnung.";
             } else {
                 txt = "Darüber weiß ich nichts.";
             }
             
             msg.setDefaultResponse(new DelayedMessage(txt, 3000));
         }
     }
     
     private static LinkedList<String> verbs() {
         LinkedList<String> result = new LinkedList<String>();
         result.add("hat");
         result.add("ist");
         result.add("kann");
         result.add("wohnt");
         result.add("kommt");
         result.add("wird");
         result.add("sind");
         return result;
     }
 }
