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
 
 package erki.xpeter.parsers;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutputStream;
 import java.util.Collection;
 import java.util.LinkedList;
 import java.util.TreeMap;
 
 import net.roarsoftware.lastfm.Caller;
 import net.roarsoftware.lastfm.Track;
 import net.roarsoftware.lastfm.User;
 import erki.api.util.Log;
 import erki.api.util.Observer;
 import erki.xpeter.Bot;
 import erki.xpeter.msg.DelayedMessage;
 import erki.xpeter.msg.TextMessage;
 import erki.xpeter.util.BotApi;
 
 /**
  * Enables the bot to retrieve information from Last.fm and tell people what they are listening to.
  * 
  * @author Edgar Kalkowski
  */
 public class LastFm implements Parser, Observer<TextMessage> {
     
     private static final String CONFIG_FILE = "config" + File.separator + "Last.fm";
     
     private static final String LAST_FM_API_KEY = "2ff20341447560ee47656ce6e572107e";
     
     private TreeMap<String, String> nicks;
     
     @Override
     public void init(Bot bot) {
         load();
         bot.register(TextMessage.class, this);
         Caller.getInstance().setUserAgent("xpeter");
     }
     
     @Override
     public void destroy(Bot bot) {
         bot.deregister(TextMessage.class, this);
     }
     
     @Override
     public void inform(TextMessage msg) {
         String nick = msg.getBotNick();
         String text = msg.getText();
         
         if (!BotApi.addresses(text, nick)) {
             return;
         }
         
         text = BotApi.trimNick(text, nick);
         String queryNick = null;
         String match = "([bB]ei )?[lL]ast\\.?[fF][mM] (hei(ss|ß)e ich|kennt( man)? mich als|bin "
                 + "ich bekannt (als|unter)) (.*?)";
         
         if (text.matches(match)) {
             String lastFmNick = text.replaceAll(match, "$6");
             lastFmNick = lastFmNick.replaceAll(" ", "");
             lastFmNick = lastFmNick.replaceAll("!", "");
             lastFmNick = lastFmNick.replaceAll("\\.", "");
             nicks.put(msg.getNick(), lastFmNick);
             msg.respond(new DelayedMessage(msg.getNick() + ": Ok, bei Last.fm heißt du also „"
                     + lastFmNick + "“.", 2000));
             save();
             return;
         }
         
         match = "[wW]as (soll ich h(oe|ö)ren|h(oe|ö)re ich gerne|f(ue|ü)r [iI]nterpreten)\\?";
         
         if (text.matches("[wW]as (f(ue|ü)r Musik )?soll ich( mir)? (an)?h(oe|ö)ren\\?")
                 || text.matches("[wW]as h(oe|ö)re ich gerne?\\?")
                 || text.matches("([wW]as f(ue|ü)r|[wW]elche) ([Ii]nterpreten|[Mm]usike?r?) "
                         + "(mag ich( gerne?)?|h(oe|ö)re ich gerne?)\\?")) {
             queryNick = msg.getNick();
             
             if (nicks.containsKey(queryNick)) {
                 queryNick = nicks.get(queryNick);
             }
             
             LinkedList<Track> tracks = new LinkedList<Track>(User.getTopTracks(queryNick,
                     LAST_FM_API_KEY));
             
             if (!tracks.isEmpty()) {
                 Track suggestion = tracks.get((int) (Math.random() * tracks.size()));
                 msg.respond(new DelayedMessage("Wie wär’s mit " + suggestion.getName() + " von "
                         + suggestion.getArtist() + "?", 2000));
             } else {
                 msg.respond(new DelayedMessage("Das weiß ich auch nicht.", 2000));
             }
             
             return;
         }
         
         if (text.toLowerCase().trim().startsWith("np ")) {
             queryNick = text.substring("np ".length());
             
             if (nicks.containsKey(queryNick)) {
                 queryNick = nicks.get(queryNick);
             }
         }
         
         if (text.toLowerCase().trim().startsWith("last.fm ")) {
             queryNick = text.substring("last.fm ".length());
             
             if (nicks.containsKey(queryNick)) {
                 queryNick = nicks.get(queryNick);
             }
         }
         
         if (text.toLowerCase().trim().startsWith("lastfm")) {
             queryNick = text.substring("lastfm".length());
             
             if (nicks.containsKey(queryNick)) {
                 queryNick = nicks.get(queryNick);
             }
         }
         
         if (text.toLowerCase().trim().equals("np") || text.toLowerCase().trim().equals("last.fm")
                 || text.toLowerCase().trim().equals("lastfm")) {
             
             if (nicks.containsKey(msg.getNick())) {
                 queryNick = nicks.get(msg.getNick());
             } else {
                 queryNick = msg.getNick();
             }
         }
         
         match = "[wW]as h(oe|ö)rt (.*?)( gerade)?\\??";
         
         if (text.matches(match)) {
             queryNick = text.replaceAll(match, "$2");
             
             if (nicks.containsKey(queryNick)) {
                 queryNick = nicks.get(queryNick);
             }
         }
         
        match = "[wW]as h(oe|ö)re ich( ge?rade)?\\??";
         
         if (text.matches(match)) {
             
             if (nicks.containsKey(msg.getNick())) {
                 queryNick = nicks.get(msg.getNick());
             } else {
                 queryNick = msg.getNick();
             }
         }
         
         if (queryNick != null) {
             Collection<Track> tracks = User.getRecentTracks(queryNick, LAST_FM_API_KEY);
             
             if (!queryNick.equals(msg.getNick())) {
                 queryNick = queryNick + " (" + msg.getNick() + ")";
             }
             
             if (!tracks.isEmpty()) {
                 Track track = tracks.iterator().next();
                 
                 if (track.isNowPlaying()) {
                     msg.respond(new DelayedMessage(queryNick + " hört gerade " + formatTrack(track)
                             + ".", 1000));
                 } else {
                     msg.respond(new DelayedMessage(queryNick + " hat zuletzt " + formatTrack(track)
                             + " gehört.", 1000));
                 }
                 
             } else {
                 msg.respond(new DelayedMessage("Last.fm weiß leider nicht, was " + queryNick
                         + " gerade hört. :(", 1500));
                 
                 if (((int) (Math.random() * 3)) == 0) {
                     msg.respond(new DelayedMessage("Tut mir Leid.", 2500));
                 }
             }
         }
     }
     
     private String formatTrack(Track track) {
         
         if (track.getAlbum() == null || track.getAlbum().equals("")) {
             return "„" + track.getName() + "“ von »" + track.getArtist() + "«";
         } else {
             return "„" + track.getName() + "“ von »" + track.getArtist() + "« auf dem Album „"
                     + track.getAlbum() + "“";
         }
     }
     
     private void save() {
         
         try {
             ObjectOutputStream fileOut = new ObjectOutputStream(new FileOutputStream(CONFIG_FILE));
             fileOut.writeObject(nicks);
             fileOut.close();
             Log.info("The Last.fm nicks were successfully stored to " + CONFIG_FILE + ".");
         } catch (FileNotFoundException e) {
             Log.warning("The Last.fm nicks could not be stored because the config file "
                     + CONFIG_FILE + " could not be found!");
         } catch (IOException e) {
             Log.warning("An error occurred while trying to save the Last.fm nicks ("
                     + e.getClass().getSimpleName() + ")!");
         }
     }
     
     @SuppressWarnings("unchecked")
     private void load() {
         
         try {
             ObjectInputStream fileIn = new ObjectInputStream(new FileInputStream(CONFIG_FILE));
             nicks = (TreeMap<String, String>) fileIn.readObject();
             fileIn.close();
             Log.info("The Last.fm nicks were successfully loaded from " + CONFIG_FILE + ".");
         } catch (FileNotFoundException e) {
             Log.info("No saved Last.fm nicks found. Starting with empty mapping.");
             nicks = new TreeMap<String, String>();
         } catch (IOException e) {
             Log.warning("An error occurred while reading the stored Last.fm nicks ("
                     + e.getClass().getSimpleName() + ").");
             Log.info("Trying to continue with an empty mapping.");
         } catch (ClassNotFoundException e) {
             Log.warning("Could not load Last.fm nicks because the class that stores the nick "
                     + "mapping could not be found!");
             Log.info("Trying to continue with an empty mapping.");
         }
     }
 }
