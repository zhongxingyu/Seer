 /*
  * © Copyright 2008–2010 by Edgar Kalkowski <eMail@edgar-kalkowski.de>
  * 
  * This file is part of the chatbot xpeter.
  * 
  * The chatbot xpeter is free software; you can redistribute it and/or modify it under the terms of
  * the GNU General Public License as published by the Free Software Foundation; either version 3 of
  * the License, or (at your option) any later version.
  * 
  * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
  * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
  * General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License along with this program. If
  * not, see <http://www.gnu.org/licenses/>.
  */
 
 package erki.xpeter.parsers.rss;
 
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.LinkedList;
 import java.util.TreeMap;
 
 import org.gnu.stealthp.rsslib.RSSChannel;
 import org.gnu.stealthp.rsslib.RSSException;
 import org.gnu.stealthp.rsslib.RSSHandler;
 import org.gnu.stealthp.rsslib.RSSItem;
 import org.gnu.stealthp.rsslib.RSSParser;
 
 import erki.api.storage.Storage;
 import erki.api.util.Log;
 import erki.api.util.Observer;
 import erki.xpeter.Bot;
 import erki.xpeter.msg.Message;
 import erki.xpeter.msg.TextMessage;
 import erki.xpeter.parsers.Parser;
 import erki.xpeter.util.BotApi;
 import erki.xpeter.util.Keys;
 import erki.xpeter.util.StorageKey;
 
 /**
  * Allows the bot to reports news received from rss feeds.
  * 
  * @author Edgar Kalkowski
  */
 public class RssFeed implements Parser, Observer<TextMessage> {
     
     private static final StorageKey<TreeMap<String, FeedData>> key = new StorageKey<TreeMap<String, FeedData>>(
             Keys.RSS_FEEDS);
     
     private Storage<Keys> storage;
     
     private TreeMap<String, FeedData> feeds;
     
     private UpdateThread updateThread;
     
     @Override
     public void init(Bot bot) {
         storage = bot.getStorage();
         
         if (storage.contains(key)) {
             feeds = storage.get(key);
             Log.info("Stored feeds successfully loaded.");
         } else {
             feeds = new TreeMap<String, FeedData>();
             Log.info("No stored feeds found.");
         }
         
         updateThread = new UpdateThread(feeds, bot, key, storage);
         updateThread.start();
         bot.register(TextMessage.class, this);
     }
     
     @Override
     public void destroy(Bot bot) {
         bot.deregister(TextMessage.class, this);
         updateThread.kill();
     }
     
     @SuppressWarnings("unchecked")
     @Override
     public void inform(TextMessage msg) {
         String text = msg.getText();
         String botNick = msg.getBotNick();
         
         if (!BotApi.addresses(text, botNick)) {
             return;
         }
         
         text = BotApi.trimNick(text, botNick);
         
         String match = "[Nn]euer [fF]eed:? (.*)";
         
         if (text.matches(match)) {
             String url = text.replaceAll(match, "$1");
             Log.debug("Recognized feed url: “" + url + "”.");
             RSSHandler handler = new RSSHandler();
             
             try {
                 RSSParser.parseXmlFile(new URL(url), handler, false);
                 
                 RSSChannel channel = handler.getRSSChannel();
                 LinkedList<RSSItem> items = channel.getItems();
                 LinkedList<String> knownItems = new LinkedList<String>();
                 
                 for (RSSItem item : items) {
                     knownItems.add(item.toString());
                 }
                 
                 FeedData feed = new FeedData(channel.getTitle(), url, knownItems);
                 feeds.put(url, feed);
                 storage.add(key, feeds);
                 msg.respond(new Message("Ok, ist gespeichert."));
             } catch (MalformedURLException e) {
                 Log.error(e);
                 msg.respond(new Message("Die URL scheint ungültig zu sein. :("));
             } catch (RSSException e) {
                 Log.error(e);
                 msg.respond(new Message("Ich komm mit dem Feed nicht ganz klar … :("));
             }
         }
         
         match = "[fF]eed-?[tT]itel:? (\\S*) (.*)";
         
         if (text.matches(match)) {
             String identifier = text.replaceAll(match, "$1");
             String title = text.replaceAll(match, "$2");
             LinkedList<String> matches = new LinkedList<String>();
             
             for (String url : feeds.keySet()) {
                 FeedData feed = feeds.get(url);
                 
                 if (feed.getTitle().contains(identifier) || feed.getUrl().equals(identifier)) {
                     matches.add(url);
                 }
             }
             
             if (matches.isEmpty()) {
                 msg.respond(new Message("Ich konnte leider keinen passenden Feed finden. :("));
             } else if (matches.size() > 1) {
                 msg.respond(new Message("Es wurden mehrere Feeds gefunden. Bitte schränke die "
                         + "Auswahl mehr ein!"));
             } else {
                 String oldTitle = feeds.get(matches.get(0)).getTitle();
                 feeds.get(matches.get(0)).setTitle(title);
                 storage.add(key, feeds);
                 msg.respond(new Message("Ok. „" + oldTitle + "“ hat jetzt den Titel „" + title
                         + "“."));
             }
         }
         
         match = "(([wW]elche|[wW]as f(ue|ü)r) [fF]eeds (hast|kennst) "
                 + "[dD]u( so)?\\??|[fF]eed-?[dD]etails\\.?\\??!?)";
         
         if (text.matches(match)) {
             
             if (feeds.isEmpty()) {
                 msg.respond(new Message("Ich kenne leider gar keinen Feed. :("));
             } else if (feeds.size() == 1) {
                 FeedData feed = feeds.get(feeds.keySet().iterator().next());
                msg.respond(new Message("Ich kenne nur den einen Feed " + feed.getTitle() + " ("
                        + feed.getUrl() + ")."));
             } else {
                 String response = "Ich kenne die folgenden Feeds: ";
                 
                 for (String url : feeds.keySet()) {
                     FeedData feed = feeds.get(url);
                     
                     if (feed.isVerbose()) {
                         response += "\n – (mit Details) ";
                     } else {
                         response += "\n – (ohne Details) ";
                     }
                     
                     response += feed.getTitle() + " (" + feed.getUrl() + ")";
                 }
                 
                 msg.respond(new Message(response));
             }
         }
         
         match = "[fF]eed-?[Dd]etails (an|on|aus|off):? (.*)";
         
         if (text.matches(match)) {
             String mode = text.replaceAll(match, "$1");
             String identifier = text.replaceAll(match, "$2");
             Log.debug("Recognized feed identifier: “" + identifier + "”.");
             LinkedList<String> matches = new LinkedList<String>();
             
             for (String url : feeds.keySet()) {
                 FeedData feed = feeds.get(url);
                 
                 if (feed.getTitle().contains(identifier) || feed.getUrl().equals(identifier)) {
                     matches.add(feed.getTitle());
                     
                     if (mode.equals("an") || mode.equals("on")) {
                         feed.setVerbose(true);
                     } else {
                         feed.setVerbose(false);
                     }
                 }
             }
             
             if (matches.isEmpty()) {
                 msg.respond(new Message("Ich konnte leider keinen passenden Feed finden. :("));
             } else if (matches.size() == 1) {
                
                if (mode.equals("on") || mode.equals("an")) {
                    msg.respond(new Message("Ok. Ab sofort werden die Details des Feeds "
                            + matches.get(0) + " angezeigt."));
                } else {
                    msg.respond(new Message("Ok. Ab sofort werden die Details des Feeds "
                            + matches.get(0) + " nicht mehr angezeigt."));
                }
                
                 storage.add(key, feeds);
             } else {
                
                if (mode.equals("an") || mode.equals("on")) {
                    msg.respond(new Message("Ok. Ab sofort werden die Details der Feeds "
                            + BotApi.enumerate(matches) + " angezeigt."));
                } else {
                    msg.respond(new Message("Ok. Ab sofort werden die Details der Feeds "
                            + BotApi.enumerate(matches) + " nicht mehr angezeigt."));
                }
                
                 storage.add(key, feeds);
             }
         }
         
         match = "([Vv]ergiss|[Ll](oe|ö)sche) (den )?[fF]eed:? (.*)";
         
         if (text.matches(match)) {
             String identifier = text.replaceAll(match, "$4");
             Log.debug("Recognized feed identifier: “" + identifier + "”.");
             LinkedList<String> matches = new LinkedList<String>();
             String[] feedArray = feeds.keySet().toArray(new String[0]);
             
             for (String url : feedArray) {
                 FeedData feed = feeds.get(url);
                 
                 if (feed.getTitle().contains(identifier) || feed.getUrl().equals(identifier)) {
                     matches.add(feed.getTitle());
                     feeds.remove(url);
                 }
             }
             
             if (matches.isEmpty()) {
                 msg.respond(new Message("Ich konnte leider keinen passenden Feed finden. :("));
             } else if (matches.size() == 1) {
                 msg.respond(new Message("Ok. Der Feed „" + matches.get(0) + "“ wurde gelöscht."));
                 storage.add(key, feeds);
             } else {
                 msg.respond(new Message("Ok. Die Feeds " + BotApi.enumerate(matches)
                         + " wurden gelöscht."));
                 storage.add(key, feeds);
             }
         }
     }
 }
