 /**
  * Copyright (c) 2012 Daniele Pantaleone, Mathias Van Malderen
  *
  * Permission is hereby granted, free of charge, to any person obtaining a copy
  * of this software and associated documentation files (the "Software"), to deal
  * in the Software without restriction, including without limitation the rights
  * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
  * copies of the Software, and to permit persons to whom the Software is
  * furnished to do so, subject to the following conditions:
  *
  * The above copyright notice and this permission notice shall be included in
  * all copies or substantial portions of the Software.
  *
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
  * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
  * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
  * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
  * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
  * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
  * THE SOFTWARE.
  * 
  * @author      Daniele Pantaleone
  * @version     1.2.2
  * @copyright   Daniele Pantaleone, 10 February, 2013
  * @package     com.orion.console
  **/
 
 package com.orion.console;
 
 import static com.google.common.base.Preconditions.checkNotNull;
 
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.RandomAccessFile;
 import java.net.UnknownHostException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.LinkedHashMap;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import org.apache.commons.logging.Log;
 
 import com.orion.bot.Orion;
 import com.orion.command.Command;
 import com.orion.domain.Client;
 import com.orion.exception.ParserException;
 import com.orion.exception.RconException;
 import com.orion.urt.Color;
 import com.orion.urt.Cvar;
 import com.orion.urt.Game;
 import com.orion.urt.Team;
 import com.orion.utility.Rcon;
 import com.orion.utility.Splitter;
 
 public class UrT42Console implements Console {
     
     private static final int CHAT_DELAY = 1000;
     private static final int CENTER_SCREEN_DELAY = 2000;
     
     private static final int MAX_SAY_STRLEN = 62;
     
     private final Rcon rcon;
     private final Log log;
     
     private Game game;
     
     
     /**
      * Object constructor
      * 
      * @author Daniele Pantaleone 
      * @param  address The remote server address
      * @param  port The virtual port on which the server is accepting connections
      * @param  password The server RCON password
      * @param  orion Orion object reference
      * @throws UnknownHostException If the IP address of a host could not be determined
      * @throws RconException If the RCON utility object fails in being initialized
      **/
     public UrT42Console(String address, int port, String password, Orion orion) throws UnknownHostException, RconException {
         
         this.log = orion.log;
         this.game = orion.game;
         this.rcon = new Rcon(address, port, password, this.log);
         this.log.debug("Urban Terror 4.2 console initialized");
         
     }
     
     
     /**
      * Ban a <tt>Client</tt> from the server using the FS Auth System
      * 
      * @author Daniele Pantaleone
      * @param  slot The slot of the <tt>Client</tt> who is going to be banned
      * @param  days The number of days of the ban
      * @param  hours The number of hours of the ban
      * @param  mins The number of minutes of the ban
      * @throws UnsupportedOperationException If the Auth System has not been correctly initialized
      * @throws RconException If the RCON command fails in being executed
      **/
     public void authban(int slot, int days, int hours, int mins) throws UnsupportedOperationException, RconException {
         
         if (!this.game.isCvar("auth_enable") || !this.game.getCvar("auth_enable").getBoolean())
             throw new UnsupportedOperationException("auth system is disabled");
         
         if (!this.game.isCvar("auth_owners"))
             throw new UnsupportedOperationException("auth owners is not correctly set");
         
         this.rcon.sendNoRead("auth-ban " + slot + " " + days + " " + hours + " " + mins);
         
     }
     
     
     /**
      * Ban a <tt>Client</tt> from the server using the FS Auth System
      * 
      * @author Daniele Pantaleone
      * @param  client The <tt>Client</tt> who is going to be banned
      * @param  days The number of days of the ban
      * @param  hours The number of hours of the ban
      * @param  mins The number of minutes of the ban
      * @throws UnsupportedOperationException If the Auth System has not been correctly initialized
      * @throws RconException If the RCON command fails in being executed
      * @throws NullPointerException If the given <tt>Client</tt> is <tt>null</tt>
      **/
     public void authban(Client client, int days, int hours, int mins) throws UnsupportedOperationException, RconException, NullPointerException {
        checkNotNull(client);
        this.authban(client.getSlot(), days, hours, mins);
     }
     
     
     /**
      * Permban a <tt>Client</tt> from the server using the FS Auth System
      * 
      * @author Daniele Pantaleone
      * @param  slot The slot of the <tt>Client</tt> who is going to be banned permanently
      * @throws UnsupportedOperationException If the Auth System has not been correctly initialized
      * @throws RconException If the RCON command fails in being executed
      **/
     public void authpermban(int slot) throws UnsupportedOperationException, RconException {
         this.authban(slot, 0, 0, 0);
     }
     
     
     /**
      * Permban a <tt>Client</tt> from the server using the FS Auth System
      * 
      * @author Daniele Pantaleone
      * @param  client The <tt>Client</tt> who is going to be banned permanently
      * @throws UnsupportedOperationException If the Auth System has not been correctly initialized
      * @throws RconException If the RCON command fails in being executed
      * @throws NullPointerException If the given <tt>Client</tt> is <tt>null</tt>
      **/
     public void authpermban(Client client) throws UnsupportedOperationException, RconException, NullPointerException {
         this.authpermban(checkNotNull(client).getSlot());
     }
         
     
     /**
      * Fetch FS Auth System informations for the specified <tt>Client</tt>
      * 
      * @author Daniele Pantaleone
      * @param  slot The slot of the <tt>Client</tt> whose informations needs to be retrieved
      * @throws UnsupportedOperationException If the Auth System has not been correctly initialized
      * @throws RconException If the RCON command fails in being executed
      * @throws ParserException If the auth-whois response couldn't be parsed correctly
      * @return A <tt>Map</tt> containing the auth-whois command response
      **/
     public Map<String, String> authwhois(int slot) throws UnsupportedOperationException, RconException, ParserException {
         
         if (!this.game.isCvar("auth_enable") || !this.game.getCvar("auth_enable").getBoolean())
             throw new UnsupportedOperationException("auth system is disabled");
         
         Map<String, String> data = new HashMap<String, String>();
         String result = this.rcon.sendRead("auth-whois " + slot);
         
         // Collecting FS Auth System informations
         Pattern pattern = Pattern.compile("^auth:\\s*id:\\s*(?<slot>\\d+)\\s*-\\s*name:\\s*(?<name>\\w+)\\s*-\\s*login:\\s*(?<login>\\w*)\\s*-\\s*notoriety:\\s*(?<notoriety>.*)\\s*-\\s*level:\\s*(?<level>\\d+)\\s*-\\s*(?<rank>.*)$", Pattern.CASE_INSENSITIVE);
         Matcher matcher = pattern.matcher(result);
         
         if (!matcher.matches())
             throw new ParserException("could not parse auth-whois command response");
         
         data.put("slot",        matcher.group("slot"));
         data.put("name",        matcher.group("name"));
         data.put("login",       matcher.group("login").isEmpty() ? null : matcher.group("login"));
         data.put("notoriety",   matcher.group("login").isEmpty() ? null : matcher.group("notoriety"));
         data.put("level",       matcher.group("login").isEmpty() ? null : matcher.group("level"));
         data.put("rank",        matcher.group("login").isEmpty() ? null : matcher.group("rank"));
         
         return data;
         
     }
     
     
     /**
      * Fetch FS Auth System informations for the specified <tt>Client</tt>
      * 
      * @author Daniele Pantaleone
      * @param  client The <tt>Client</tt> whose informations needs to be retrieved
      * @throws UnsupportedOperationException If the Auth System has not been correctly initialized
      * @throws RconException If the RCON command fails in being executed
      * @throws ParserException If the auth-whois response couldn't be parsed correctly
      * @throws NullPointerException If the given <tt>Client</tt> is <tt>null</tt>
      * @return A <tt>Map</tt> containing the auth-whois command response
      **/
     public Map<String, String> authwhois(Client client) throws UnsupportedOperationException, RconException, ParserException, NullPointerException {
         return this.authwhois(checkNotNull(client).getSlot());
     }
     
     
     /**
      * Ban an IP address from the server permanently
      * 
      * @author Daniele Pantaleone
      * @param  ip The IP address to be banned 
      * @throws RconException If the RCON command fails in being executed
      * @throws NullPointerException If the given <tt>String</tt> is <tt>null</tt>
      **/
     public void ban(String ip) throws RconException, NullPointerException {
         this.rcon.sendNoRead("addip " + checkNotNull(ip));
     }
     
     
     /**
      * Ban a <tt>Client</tt> IP address from the server permanently
      * 
      * @author Daniele Pantaleone
      * @param  client The <tt>Client</tt> whose IP address is going to be banned
      * @throws RconException If the RCON command fails in being executed
      * @throws NullPointerException If the given <tt>Client</tt> is <tt>null</tt>
      **/
     public void ban(Client client) throws RconException, NullPointerException {
         this.ban(checkNotNull(client).getIp().getHostAddress());
     }
     
     
     /**
      * Write a bold message in the middle of the screen
      * 
      * @author Daniele Pantaleone
      * @param  message The message to be printed
      * @throws RconException If the RCON command fails in being executed
      * @throws NullPointerException If the given <tt>String</tt> is <tt>null</tt>
      **/
     public void bigtext(String message) throws RconException {
         
         if (checkNotNull(message).length() > MAX_SAY_STRLEN) {
             
             // Splitting the message into multiple sentences
             // In this way it won't overflow the game chat and it will print nicer
             List<String> collection = Splitter.split(message, MAX_SAY_STRLEN);
             
             for (String sentence: collection) {
                 // Printing separate sentences. We'll also introduce a sleep
                 // in between the messages since the a bigtext overlap a previous
                 // printed message with a new one. It would be unreadable
                 sentence = sentence.trim();
                 this.rcon.sendNoRead("bigtext \"" + Color.WHITE + sentence + "\"");
             
                 try { 
                     Thread.sleep(CENTER_SCREEN_DELAY); 
                 } catch (InterruptedException e) {
                     // Do nothing here...
                 }
                 
             }
             
         } else {
             
             // No need to split here. Just send the command
             this.rcon.sendNoRead("bigtext \"" + Color.WHITE + message + "\"");
             
         }
         
     }
     
     
     /**
      * Broadcast a message in the top-left screen
      * 
      * @author Daniele Pantaleone
      * @param  message The message to be sent
      * @throws RconException If the RCON commands fails in being executed
      * @throws NullPointerException If the given <tt>String</tt> is <tt>null</tt>
      **/
     public void broadcast(String message) throws RconException {
         
         if (checkNotNull(message).length() > MAX_SAY_STRLEN) {
             
             // Splitting the message into multiple sentences
             // In this way it won't overflow the game chat and it will print nicer
             List<String> collection = Splitter.split(message, MAX_SAY_STRLEN);
             
             for (String sentence: collection) {
             
                 // Sending the message
                 sentence = sentence.trim();
                 this.rcon.sendNoRead(Color.WHITE + sentence);
                 
                 try { 
                     Thread.sleep(CHAT_DELAY); 
                 } catch (InterruptedException e) {
                     // Do nothing here...
                 }
                 
             }
             
         } else {
             
             // No need to split here. Just send the command
             this.rcon.sendNoRead(Color.WHITE + message);
             
         }
         
     }
     
     
     /**
      * Cycle the current map on the server
      * 
      * @author Daniele Pantaleone
      * @throws RconException If the RCON commands fails in being executed
      **/
     public void cyclemap() throws RconException {
         this.rcon.sendNoRead("cyclemap");
     }
     
     
     /**
      * Retrieve userinfo data for the specified <tt>Client</tt> slot number
      * 
      * @author Daniele Pantaleone
      * @param  slot The slot of the <tt>Client</tt> whose informations needs to be retrieved
      * @throws RconException If the <tt>Client</tt> informations couldn't be retrieved
      * @return A <tt>Map</tt> containing userinfo data or <tt>null</tt> 
      *         if the <tt>Client</tt> is not connected anymore
      **/
     public Map<String, String> dumpuser(int slot) throws RconException {
         
         String result = this.rcon.sendRead("dumpuser " + slot);
         
         // This is the string we expect from the /rcon dumpuser <slot> command.
         // We need to parse it and build an HashMap containing the client data.
         //
         // userinfo
         // --------
         // ip                  93.40.100.128:59685
         // gear                GZJATWA
         // rate                25000
         // name                [FS]Fenix
         // racered             2
         
         Map<String, String> map = new LinkedHashMap<String, String>();
         Pattern pattern = Pattern.compile("^\\s*(?<key>\\w+)\\s+(?<value>.*)$");
         String[] lines = result.split("\n");
         
         for (String line: lines) {
             Matcher m = pattern.matcher(line);
             if (m.matches()) 
                 map.put(m.group("key"), m.group("value"));
         }
         
         return map.size() > 0 ? map : null;
         
     }
     
     
     /**
      * Retrieve userinfo data for the specified <tt>Client</tt>
      * 
      * @author Daniele Pantaleone
      * @param  client The <tt>Client</tt> whose informations needs to be retrieved
      * @throws RconException If the <tt>Client</tt> informations couldn't be retrieved
      * @throws NullPointerException If the given <tt>Client</tt> is <tt>null</tt>
      * @return A <tt>Map</tt> containing userinfo data or <tt>null</tt> 
      *         if the <tt>Client</tt> is not connected anymore
      **/
     public Map<String, String> dumpuser(Client client) throws RconException, NullPointerException {
         return this.dumpuser(checkNotNull(client).getSlot());
     }
       
     
     /**
      * Force a <tt>Client</tt> in the blue team
      * 
      * @author Daniele Pantaleone
      * @param  slot The slot of the <tt>Client</tt> who is going to be forced in the blue team
      * @throws RconException If the <tt>Client</tt> fails in being moved
      **/
     public void forceblue(int slot) throws RconException {
         // Since we do not have a Client object as input, we cannot match the current
         // client team. The RCON command is going to be executed anyway
         // NOTE: Use the previous version of the command if possible
         this.rcon.sendNoRead("forceteam " + slot + " blue");
     }
     
     
     /**
      * Force a <tt>Client</tt> in the blue team
      * 
      * @author Daniele Pantaleone
      * @param  client The <tt>Client</tt> who is going to be forced in the blue team
      * @throws RconException If the <tt>Client</tt> fails in being moved
      * @throws NullPointerException If the given <tt>Client</tt> is <tt>null</tt>
      **/
     public void forceblue(Client client) throws RconException {
 
         // Do not execute if the client is already in the specified team
         // This will prevent to overflow the server with RCON commands
         if (checkNotNull(client).getTeam() != Team.BLUE)
             this.forceblue(client.getSlot());
     }
     
     
     /**
      * Force a <tt>Client</tt> in the free team (aka autojoin)
      * 
      * @author Daniele Pantaleone
      * @param  slot The slot of the <tt>Client</tt> who is going to be forced in the free team
      * @throws RconException If the <tt>Client</tt> fails in being moved
      **/
     public void forcefree(int slot) throws RconException {
         this.rcon.sendNoRead("forceteam " + slot + " free");
     }
     
     
     /**
      * Force a <tt>Client</tt> in the free team (aka autojoin)
      * 
      * @author Daniele Pantaleone
      * @param  client The <tt>Client</tt> who is going to be forced in the free team
      * @throws RconException If the <tt>Client</tt> fails in being moved
      * @throws NullPointerException If the given <tt>Client</tt> is <tt>null</tt>
      **/
     public void forcefree(Client client) throws RconException, NullPointerException {
         this.forcefree(checkNotNull(client).getSlot());
     }
     
     
     /**
      * Force a <tt>Client</tt> in the red team
      * 
      * @author Daniele Pantaleone
      * @param  slot The slot of the <tt>Client</tt> who is going to be forced in the red team
      * @throws RconException If the <tt>Client</tt> fails in being moved
      **/
     public void forcered(int slot) throws RconException {
         // Since we do not have a Client object as input, we cannot match the current
         // client team. The RCON command is going to be executed anyway
         // NOTE: Use the previous version of the command if possible
         this.rcon.sendNoRead("forceteam " + slot + " red");
     }
     
     
     /**
      * Force a <tt>Client</tt> in the red team
      * 
      * @author Daniele Pantaleone
      * @param  client The <tt>Client</tt> who is going to be forced in the red team
      * @throws RconException If the <tt>Client</tt> fails in being moved
      * @throws NullPointerException If the given <tt>Client</tt> is <tt>null</tt>
      **/
     public void forcered(Client client) throws RconException, NullPointerException {
 
         // Do not execute if the client is already in the specified team
         // This will prevent to overflow the server with RCON commands
         if (checkNotNull(client).getTeam() != Team.RED)
             this.forcered(client.getSlot());
     }
     
     
     /**
      * Force a <tt>Client</tt> in the spectator team
      * 
      * @author Daniele Pantaleone
      * @param  slot The slot of the <tt>Client</tt> who is going to be forced in the spectator team
      * @throws RconException If the <tt>Client</tt> fails in being moved
      **/
     public void forcespec(int slot) throws RconException {
         // Since we do not have a Client object as input, we cannot match the current
         // client team. The RCON command is going to be executed anyway
         // NOTE: Use the previous version of the command if possible
         this.rcon.sendNoRead("forceteam " + slot + " spectator");
     }
     
     
     /**
      * Force a <tt>Client</tt> in the spectator team
      * 
      * @author Daniele Pantaleone
      * @param  client The <tt>Client</tt> who is going to be forced in the spectator team
      * @throws RconException If the <tt>Client</tt> fails in being moved
      * @throws NullPointerException If the given <tt>Client</tt> is <tt>null</tt>
      **/
     public void forcespec(Client client) throws RconException {
          
         // Do not execute if the client is already in the specified team
         // This will prevent to overflow the server with RCON commands
         if (checkNotNull(client).getTeam() != Team.RED)
             this.forcespec(client.getSlot());
     }
     
     
     /**
      * Force a <tt>Client</tt> in the specified team
      *
      * @author Daniele Pantaleone
      * @param  slot The slot of the <tt>Client</tt> who is going to be moved
      * @param  team The <tt>Team</tt> where to force the player in
      * @throws RconException If the <tt>Client</tt> fails in being moved
      **/
     public void forceteam(int slot, Team team) throws RconException {
         
         switch (team) {
         
             case RED:
                 this.forcered(slot);
                 break;
                 
             case BLUE:
                 this.forceblue(slot);
                 break;
                 
             case SPECTATOR:
                 this.forcespec(slot);
                 break;
             
             case FREE:
                 this.forcefree(slot);
                 break;
                 
         }
     
     }
     
     
     /**
      * Force a <tt>Client</tt> in the specified team
      *
      * @author Daniele Pantaleone
      * @param  client The <tt>Client</tt> who is going to be moved
      * @param  team The <tt>Team</tt> where to force the player in
      * @throws RconException If the <tt>Client</tt> fails in being moved
      * @throws NullPointerException If the given <tt>Client</tt> is <tt>null</tt>
      **/
     public void forceteam(Client client, Team team) throws RconException {
         this.forceteam(checkNotNull(client).getSlot(), team);
     }
         
     
     /**
      * Retrieve a CVAR from the server
      * 
      * @author Daniele Pantaleone
      * @param  name The CVAR name
      * @throws RconException If the CVAR could not be retrieved form the server
      * @throws NullPointerException If the given <tt>String</tt> is <tt>null</tt>
      * @return The <tt>Cvar</tt> object associated to the given CVAR name or <tt>null</tt> 
      *         if such CVAR is not set on the server
      **/
     public Cvar getCvar(String name) throws RconException, NullPointerException {
         
         try {
             
             String result = this.rcon.sendRead(checkNotNull(name)); 
             
             Pattern pattern = Pattern.compile("\\s*\\\"[\\w+]*\\\"\\sis:\\\"(?<value>[\\w:\\.\\-\\\\/]*)\\\".*", Pattern.CASE_INSENSITIVE);
             Matcher matcher = pattern.matcher(result);
             
             if (matcher.matches()) {
                 
                 String value = matcher.group("value");
                 
                 if (!value.trim().isEmpty()) {
                     this.log.trace("Retrieved CVAR [" + name + "] : " + value);
                     return new Cvar(name, value);
                 }
 
             }
         
         } catch (RconException e) {
             // Catch and re-throw the same Exception but with more details
             throw new RconException("Could not retrieve CVAR [" + name + "] : ", e);
         }
         
         // We'll eventually get here if the given CVAR
         // is not set on the server (mostly when doing
         // and Runtime CVAR retrieval upon user request
         return null;
         
     }
     
         
     /**
      * Return the current map name
      * 
      * @author Daniele Pantaleone
      * @throws RconException If the current map name couldn't be retrieved
      * @return The current map name 
      **/
     public String getMap() throws RconException {
         return this.getCvar("mapname").getString();
     }
     
     
     /**
      * Return a <tt>List</tt> of available maps
      * 
      * @author Daniele Pantaleone
      * @throws RconException If the map list couldn't be retrieved
      * @return A <tt>List</tt> of all the maps available on the server
      **/
     public List<String> getMapList() throws RconException {
         
         String result = this.rcon.sendRead("fdir *.bsp");
 
         List<String> maplist = new LinkedList<String>();
         Pattern pattern = Pattern.compile("^*maps/(?<mapname>.*).bsp$");
         
         String[] lines = result.split("\n");
 
         for (String line: lines) {
             Matcher matcher = pattern.matcher(line);
             if (matcher.matches()) 
                 maplist.add(matcher.group("mapname"));
         }
         
         return maplist;
     }
     
     
     /**
      * Return a <tt>List</tt> of maps matching the given search key
      * 
      * @author Daniele Pantaleone
      * @param  search The name of the map to search (or a part of it)
      * @throws RconException If the list of available maps couldn't be computed
      * @throws NullPointerException If the given <tt>String</tt> is <tt>null</tt>
      * @return A <tt>List</tt> of maps matching the given search key
      **/
     public List<String> getMapSoundingLike(String search) throws RconException, NullPointerException {
         
         List<String> collection = new LinkedList<String>();
         
         // Trimming and making lower case the search key
         search = checkNotNull(search).toLowerCase().trim();
         
         // Server map list not computed yet. Build the map list
         if (this.game.getMapList().isEmpty())
             this.game.setMapList(this.getMapList());
         
         for (String map : this.game.getMapList()) {
             if ((map != null) && (map.toLowerCase().contains(search.toLowerCase()))) {
                 collection.add(map);
             }
         }
         
         return collection;
         
     }
     
     
     /**
      * Return the name of the nextmap set on the server
      * 
      * @author Daniele Pantaleone
      * @throws RconException If an RCON command fails in being executed
      * @throws FileNotFoundException If the mapcycle file couldn't be found
      * @throws IOException If there is an error while reading the mapcycle file
      * @return The name of the nextmap set on the server or <tt>null</tt> 
      *         if it can't be computed
      **/
     public String getNextMap() throws RconException, FileNotFoundException, IOException {
         
         String path, line, firstmap, tmpmap, mapname;
         RandomAccessFile mapfile;
         List<String> maplist;
         
         // Checking if the nextmap on the server has been manually changed 
         // using an RCON command or after a callvote for nextmap being issued
         Cvar nextmap = this.getCvar("g_nextmap");
         if (nextmap != null)
             return nextmap.getString();
         
         if (!this.game.isCvar("fs_game"))
             this.game.setCvar(this.getCvar("fs_game"));
         
         if (!this.game.isCvar("g_mapcycle"))
             this.game.setCvar(this.getCvar("g_mapcycle"));
         
         if (!this.game.isCvar("mapname"))
             this.game.setCvar(this.getCvar("mapname"));
         
         try {
             
             if (!this.game.isCvar("fs_basepath"))
                 this.game.setCvar(this.getCvar("fs_basepath"));
             
             // Computing mapcycle filepath
             path = this.game.getCvar("fs_basepath").getString() +
                    System.getProperty("file.separator") +
                    this.game.getCvar("fs_game").getString() +
                    System.getProperty("file.separator") +
                    this.game.getCvar("g_mapcycle").getString();
             
             mapfile = new RandomAccessFile(path, "r");
             
             
         } catch (RconException | FileNotFoundException e1) {
             
             // We were not able to open the mapcycle file due to 
             // fs_basepath not being retrieved, or file not found
             this.log.warn("Could not open mapcycle file", e1);
             
             try {
                 
                 if (!this.game.isCvar("fs_homepath"))
                     this.game.setCvar(this.getCvar("fs_homepath"));
                 
                 // Computing mapcycle filepath
                 path = this.game.getCvar("fs_homepath").getString() +
                         System.getProperty("file.separator") +
                         this.game.getCvar("fs_game").getString() +
                         System.getProperty("file.separator") +
                         this.game.getCvar("g_mapcycle").getString();
                 
                 mapfile = new RandomAccessFile(path, "r");
                 
             } catch (RconException | FileNotFoundException e2) {
                 
                 this.log.warn("Could not open mapcycle file", e2);
                 throw e2;
                 
             } 
             
         }
         
         // Reading the mapcycle file
         maplist = new LinkedList<String>();
         while ((line = mapfile.readLine()) != null)
             maplist.add(line);
         
         mapfile.close();
        
         // No map listed
         if (maplist.size() == 0) {
             this.log.warn("Could not retrieve nextmap. Mapcycle file is empty");
             return null;
         }
         
         // Copying the 1st map name here it as our nextmap if we do
         // not manage to find a proper match for the current map name
         firstmap = maplist.get(0);
         
         try {
             
             mapname = this.game.getCvar("mapname").getString();
             tmpmap = maplist.remove(0);
             while (!mapname.equals(tmpmap))
                 tmpmap = maplist.remove(0);
             
             if (mapname.equals(tmpmap))
                 return (maplist.size() > 0) ? maplist.get(0) : firstmap;
             
             
         } catch(IndexOutOfBoundsException e) {
             
             // We are playing the last map listed
             // in the mapcycle file. The 1st one will
             // be again set as nextmap
             return firstmap;
         
         }
         
         // We failed somehow
         this.log.warn("Unable to compute nextmap name...");
         return null;
    
     }
     
  
     /**
      * Return a <tt>List</tt> containing the result of the <tt>/rcon players</tt> command
      * 
      * @author Daniele Pantaleone
      * @throws RconException If we couldn't fetch informations from the server
      * @return A <tt>List</tt> containing players informations
      **/
     public List<List<String>> getPlayers() throws RconException {
         
         String result = this.rcon.sendRead("players");
 
         // This is the string we expect from the /rcon players command
         // We need to parse it and build an Array with players informations
         //
         // Map: ut4_casa
         // Players: 1
         // Score: R:0 B:0
         // 0:  [FS]Fenix  SPECTATOR  k:0  d:0  ping:50  62.75.235.91:27960
         
         List<List<String>> collection = new LinkedList<List<String>>();
         Pattern pattern = Pattern.compile("^\\s*(?<slot>\\d+):\\s+(?<name>.*)\\s+(?<team>RED|BLUE|SPECTATOR|FREE)\\s+k:(?<kills>\\d+)\\s+d:(?<deaths>\\d+)\\s+ping:(?<ping>\\d+|CNCT|ZMBI)\\s*([?<address>\\d.]+):([?<port>\\d-]+)?$", Pattern.CASE_INSENSITIVE);
         String[] lines = result.split("\n");
         
         for (String line: lines) {
             
             Matcher matcher = pattern.matcher(line);
             
             if (matcher.matches()) {
                 
                 List<String> x = new ArrayList<String>();
                 x.add(matcher.group("slot"));
                 x.add(matcher.group("name"));
                 x.add(matcher.group("team"));
                 x.add(matcher.group("kills"));
                 x.add(matcher.group("deaths"));
                 x.add(matcher.group("ping"));
                 x.add(matcher.group("address"));
                 x.add(matcher.group("port"));
                 collection.add(x);
                 
             }
         }
             
         return collection;
         
     }
     
     
     /**
      * Return a <tt>List</tt> containing the result of the <tt>/rcon status</tt> command
      * 
      * @author Daniele Pantaleone
      * @throws RconException If we couldn't fetch informations from the server
      * @return A <tt>List</tt> containing status informations
      **/
     public List<List<String>> getStatus() throws RconException {
         
         String result = this.rcon.sendRead("status");
         
         // This is the string we expect from the /rcon status command
         // We need to parse it and build an Array with players informations
         //
         // map: ut4_casa
         // num score ping name            lastmsg address               qport rate
         // --- ----- ---- --------------- ------- --------------------- ----- -----
         //   1    19   33 [FS]Fenix            33 62.212.106.216:27960   5294 25000
         
         List<List<String>> collection = new LinkedList<List<String>>();
         Pattern pattern = Pattern.compile("^\\s*(?<slot>\\d+)\\s*(?<score>[\\d-]+)\\s*(?<ping>\\d+|CNCT|ZMBI)\\s*(?<name>.*?)\\s*(?<lastmsg>\\d+)\\s*(?<address>[\\d.]+|loopback|localhost):?(?<port>[\\d-]*)\\s*(?<qport>[\\d-]+)\\s*(?<rate>\\d+)$", Pattern.CASE_INSENSITIVE);
         String[] lines = result.split("\n");
 
         for (String line: lines) {
             
             Matcher matcher = pattern.matcher(line);
             
             if (matcher.matches()) {
                 
                 List<String> x = new LinkedList<String>();
                 x.add(matcher.group("slot"));
                 x.add(matcher.group("score"));
                 x.add(matcher.group("ping"));
                 x.add(matcher.group("name"));
                 x.add(matcher.group("lastmsg"));
                 x.add(matcher.group("address"));
                 x.add(matcher.group("port"));
                 x.add(matcher.group("qport"));
                 x.add(matcher.group("rate"));
 
                 collection.add(x);
                 
             }
         }
         
         return collection;
     }
     
     
     /**
      * Kick the specified <tt>Client</tt> from the server
      * 
      * @author Daniele Pantaleone
      * @param  client The slot of the <tt>Client</tt> who is going to be kicked from the server
      * @throws RconException If the RCON command fails in being executed
      **/
     public void kick(int slot) throws RconException {
         this.rcon.sendNoRead("kick " + slot);
     }
     
     
     /**
      * Kick the specified <tt>Client</tt> from the server
      * 
      * @author Daniele Pantaleone
      * @param  client The <tt>Client</tt> who is going to be kicked from the server
      * @throws RconException If the RCON command fails in being executed
      * @throws NullPointerException If the given <tt>Client</tt> is <tt>null</tt>
      **/
     public void kick(Client client) throws RconException, NullPointerException {
         this.kick(checkNotNull(client).getSlot());
     }
     
     
     /**
      * Kick the specified <tt>Client</tt> from the server by specifying a reason
      * 
      * @author Daniele Pantaleone
      * @param  slot The slot of the <tt>Client</tt> who is going to be kicked from the server
      * @param  reason The reason why the <tt>Client</tt> is going to be kicked
      * @throws RconException If the RCON command fails in being executed
      **/
     public void kick(int slot, String reason) throws RconException {
         
         if (reason == null) {
             this.kick(slot);
             return;
         }
         
         this.rcon.sendNoRead("kick " + slot + " " + reason);
     
     }
     
     
     /**
      * Kick the specified <tt>Client</tt> from the server by specifying a reason
      * 
      * @author Daniele Pantaleone
      * @param  client The <tt>Client</tt> who is going to be kicked from the server
      * @param  reason The reason why the <tt>Client</tt> is going to be kicked
      * @throws RconException If the RCON command fails in being executed
      * @throws NullPointerException If the given <tt>Client</tt> is <tt>null</tt>
      **/
     public void kick(Client client, String reason) throws RconException, NullPointerException {
         this.kick(checkNotNull(client).getSlot(), reason);
     }
    
     
     /**
      * Instantly kill a <tt>Client</tt>
      * 
      * @author Daniele Pantaleone
      * @param  slot The slot of the <tt>Client</tt> who is going to be killed
      * @throws RconException If the RCON command fails in being executed
      **/
     public void kill(int slot) throws RconException {
         this.rcon.sendNoRead("smite " + slot);
     }
     
     
     /**
      * Instantly kill a <tt>Client</tt>
      * 
      * @author Daniele Pantaleone
      * @param  client The <tt>Client</tt> who is going to be killed
      * @throws RconException If the RCON command fails in being executed
      * @throws NullPointerException If the given <tt>Client</tt> is <tt>null</tt>
      **/
     public void kill(Client client) throws RconException, NullPointerException {
         this.kill(checkNotNull(client).getSlot());
     }
     
     
     /**
      * Spawn the server onto a new level
      * 
      * @author Daniele Pantaleone
      * @param  mapname The name of the level to load
      * @throws RconException If the RCON command fails in being executed
      * @throws NullPointerException If the given <tt>String</tt> is <tt>null</tt>
      **/
     public void map(String mapname) throws RconException {
         this.rcon.sendNoRead("map " + checkNotNull(mapname));
     }
     
     
     /**
      * Mute a <tt>Client</tt>
      * 
      * @author Daniele Pantaleone 
      * @param  slot The slot of the <tt>Client</tt> who is going to be muted
      * @throws RconException If the <tt>Client</tt> couldn't be muted
      **/
     public void mute(int slot) throws RconException {
         this.rcon.sendNoRead("mute " + slot);
     }
     
     
     /**
      * Mute a <tt>Client</tt>
      * 
      * @author Daniele Pantaleone 
      * @param  client The <tt>Client</tt> who is going to be muted
      * @throws RconException If the <tt>Client</tt> couldn't be muted
      * @throws NullPointerException If the given <tt>Client</tt> is <tt>null</tt>
      **/
     public void mute(Client client) throws RconException {
         this.mute(checkNotNull(client).getSlot());
     }
     
     
     /**
      * Mute a <tt>Client</tt>
      * 
      * @author Daniele Pantaleone 
      * @param  slot The slot of the <tt>Client</tt> who is going to be muted
      * @param  seconds The amount of seconds the <tt>Client</tt> will be muted
      * @throws RconException If the <tt>Client</tt> couldn't be muted
      **/
     public void mute(int slot, int seconds) throws RconException {
         this.rcon.sendNoRead("mute " + slot + " " + seconds);
     }
     
     
     /**
      * Mute a <tt>Client</tt>
      * 
      * @author Daniele Pantaleone 
      * @param  client The <tt>Client</tt> who is going to be muted
      * @param  seconds The amount of seconds the <tt>Client</tt> will be muted
      * @throws RconException If the <tt>Client</tt> couldn't be muted
      * @throws NullPointerException If the given <tt>Client</tt> is <tt>null</tt>
      **/
     public void mute(Client client, int seconds) throws RconException {
         this.mute(checkNotNull(client).getSlot(), seconds);
     }
     
 
     /**
      * Nuke a <tt>Client</tt>
      * 
      * @author Daniele Pantaleone 
      * @param  slot The slot of the <tt>Client</tt> who is going to be nuked
      * @throws RconException If the <tt>Client</tt> couldn't be nuked
      **/
     public void nuke(int slot) throws RconException {
         this.rcon.sendNoRead("nuke " + slot);
     }
     
     
     /**
      * Nuke a <tt>Client</tt>
      * 
      * @author Daniele Pantaleone 
      * @param  client The <tt>Client</tt> who is going to be nuked
      * @throws RconException If the <tt>Client</tt> couldn't be nuked
      * @throws NullPointerException If the given <tt>Client</tt> is <tt>null</tt>
      **/
     public void nuke(Client client) throws RconException, NullPointerException {
         this.nuke(checkNotNull(client).getSlot());
     }
     
     
     /**
      * Print a message in the game chat
      * 
      * @author Daniele Pantaleone
      * @param  message The message to be printed
      * @throws RconException If the RCON command fails in being executed
      * @throws NullPointerException If the given <tt>String</tt> is <tt>null</tt>
      **/
     public void say(String message) throws RconException {
         
         if (checkNotNull(message).length() > MAX_SAY_STRLEN) {
             
             // Splitting the message into multiple sentences
             // In this way it won't overflow the game chat and it will print nicer
             List<String> collection = Splitter.split(message, MAX_SAY_STRLEN);
             
             for (String sentence: collection) {
                 
                 // Sending the message
                 sentence = sentence.trim();
                 this.rcon.sendNoRead("say " + Color.WHITE + sentence);
                 
                 try { 
                     Thread.sleep(CHAT_DELAY); 
                 } catch (InterruptedException e) {
                     // Do nothing here...
                 }
                 
             }
             
         } else {
             
             // No need to split here. Just send the command
             this.rcon.sendNoRead("say " + Color.WHITE + message);
             
         }
         
     }
     
     
     /**
      * Print an in-game message with visibility regulated by the command object
      * 
      * @author Daniele Pantaleone
      * @param  command The command issued
      * @param  message The message to be printed
      * @throws RconException If the RCON command fails in being executed
      * @throws NullPointerException If one of the given parameter is <tt>null</tt>
      **/
     public void sayLoudOrPm(Command command, String message) throws RconException {
         
         checkNotNull(command);
         checkNotNull(message);
         
         switch (command.getPrefix()) {
             
             case NORMAL:
                 this.tell(command.getClient(), message);
                 break;
             case LOUD:
                 this.say(message);
                 break;
             case BIG:
                 this.bigtext(message);
                 break;
         
         }
         
     }
       
     
     /**
      * Set a CVAR value
      * 
      * @author Daniele Pantaleone
      * @param  name The name of the CVAR
      * @param  value The value to assign to the CVAR
      * @throws RconException If the CVAR could not be set
      * @throws NullPointerException If one of the given parameters is <tt>null</tt>
      **/
     public void setCvar(String name, Object value) throws RconException {
         checkNotNull(name);
         checkNotNull(value);
         this.rcon.sendNoRead("set " + name + " \"" + String.valueOf(value) + "\"");
     }
     
     
     /**
      * Set a CVAR value
      * 
      * @author Daniele Pantaleone
      * @param  cvar The <tt>Cvar</tt> to be set on the server
      * @throws RconException If the CVAR could not be set
      * @throws NullPointerException If the given <tt>Cvar</tt> is <tt>null</tt>
      **/
     public void setCvar(Cvar cvar) throws RconException {
         checkNotNull(cvar);
         this.setCvar(cvar.getName(), cvar.getString());
     }
     
     
     /**
      * Slap a <tt>Client</tt>
      * 
      * @author Daniele Pantaleone 
      * @param  slot The slot of the <tt>Client</tt> who is going to be slapped
      * @throws RconException If the <tt>Client</tt> couldn't be slapped
      **/
     public void slap(int slot) throws RconException {
         this.rcon.sendNoRead("nuke " + slot);
     }
     
     
     /**
      * Slap a <tt>Client</tt>
      * 
      * @author Daniele Pantaleone 
      * @param  client The <tt>Client</tt> who is going to be slapped
      * @throws RconException If the <tt>Client</tt> couldn't be slapped
      * @throws NullPointerException If the given <tt>Client</tt> is <tt>null</tt>
      **/
     public void slap(Client client) throws RconException, NullPointerException {
         this.slap(checkNotNull(client).getSlot());
     }
 
     
     /**
      * Start recording a server side demo of a <tt>Client</tt>
      * 
      * @author Daniele Pantaleone
      * @param  slot The slot of the <tt>Client</tt> whose demo is going to be recorded
      * @throws RconException If the demo recording couldn't be started
      **/
     public void startserverdemo(int slot) throws RconException {
         this.rcon.sendNoRead("startserverdemo " + slot);
     }
     
     
     /**
      * Start recording a server side demo of a <tt>Client</tt>
      * 
      * @author Daniele Pantaleone
      * @param  client The <tt>Client</tt> whose demo is going to be recorded
      * @throws RconException If the demo recording couldn't be started
      * @throws NullPointerException If the given <tt>Client</tt> is <tt>null</tt>
      **/
     public void startserverdemo(Client client) throws RconException, NullPointerException {
         this.startserverdemo(checkNotNull(client).getSlot());
     }
     
     
     /**
      * Start recording a server side demo of all the online clients
      * 
      * @author Daniele Pantaleone
      * @throws RconException If the demo recording couldn't be started
      **/
     public void startserverdemo() throws RconException {
         this.rcon.sendNoRead("startserverdemo all");
     }
     
     
     /**
      * Stop recording a server side demo of a <tt>Client</tt>
      * 
      * @author Daniele Pantaleone
      * @param  slot The slot of the <tt>Client</tt> whose demo is going to be stopped
      * @throws RconException If the demo recording couldn't be stopped
      **/
     public void stopserverdemo(int slot) throws RconException {
         this.rcon.sendNoRead("stopserverdemo " + slot);
     }
     
     
     /**
      * Stop recording a server side demo of a <tt>Client</tt>
      * 
      * @author Daniele Pantaleone
      * @param  client The <tt>Client</tt> whose demo is going to be stopped
      * @throws RconException If the demo recording couldn't be stopped
      * @throws NullPointerException If the given <tt>Client</tt> is <tt>null</tt>
      **/
     public void stopserverdemo(Client client) throws RconException, NullPointerException {
         this.stopserverdemo(checkNotNull(client).getSlot());
     }
     
     
     /**
      * Stop recording a server side demo of all the online clients
      * 
      * @author Daniele Pantaleone
      * @throws RconException If the demo recording couldn't be stopped
      **/
     public void stopserverdemo() throws RconException {
         this.rcon.sendNoRead("stopserverdemo all");
     }
     
     
     /**
      * Send a private message to a <tt>Client</tt>
      * 
      * @author Daniele Pantaleone
      * @param  slot The slot of the <tt>Client</tt> who will receive the message
      * @param  message The message to be sent
      * @throws RconException If the RCON command fails in being executed
      * @throws NullPointerException If the given message is <tt>null</tt>
      **/
     public void tell(int slot, String message) throws RconException, NullPointerException {
         
         if (checkNotNull(message).length() > MAX_SAY_STRLEN) {
             
             // Splitting the message into multiple sentences
             // In this way it won't overflow the game chat and it will print nicer
             List<String> collection = Splitter.split(message, MAX_SAY_STRLEN);
             
             for (String sentence: collection) {
                 
                 // Sending the message
                 sentence = sentence.trim();
                 this.rcon.sendNoRead("tell " + slot + " " + Color.WHITE + sentence);
                 
                 try { 
                     Thread.sleep(CHAT_DELAY); 
                 } catch (InterruptedException e) {
                     // Do nothing here...
                 }
                 
             }
             
         } else {
             
             // No need to split here. Just send the command
             this.rcon.sendNoRead("tell " + slot + " " + Color.WHITE + message);
             
         }
 
     }
     
     
     /**
      * Send a private message to a <tt>Client</tt>
      * 
      * @author Daniele Pantaleone
      * @param  client The <tt>Client</tt> who will receive the message
      * @param  message The message to be sent
      * @throws RconException If the RCON command fails in being executed
      * @throws NullPointerException If one of the given parameters is <tt>null</tt>
      **/
     public void tell(Client client, String message) throws RconException, NullPointerException {
         this.tell(checkNotNull(client).getSlot(), message);
     }
     
 
     /**
      * Unban an IP address from the server
      * 
      * @author Daniele Pantaleone
      * @param  ip The IP address we want to unban
      * @throws RconException If the IP address couldn't be unbanned
      * @throws NullPointerException If the given <tt>String</tt> is <tt>null</tt>
      **/
     public void unban(String ip) throws RconException, NullPointerException {
         this.rcon.sendNoRead("removeip " + checkNotNull(ip));
     }
     
     
     /**
      * Unban a <tt>Client</tt> IP address from the server
      * 
      * @author Daniele Pantaleone
      * @param  client The <tt>Client</tt> whose IP address we want to unban
      * @throws RconException If the IP address couldn't be unbanned
      * @throws NullPointerException If the given <tt>Client</tt> is <tt>null</tt>
      **/
     public void unban(Client client) throws RconException, NullPointerException {
         this.unban(checkNotNull(client).getIp().getHostAddress());
     }
     
     
     /**
      * Write a message directly in the Urban Terror console<br>
      * Try to avoid the use of this command: use instead the other 
      * optimized methods available in this class
      * 
      * @author Daniele Pantaleone
      * @param  command The command to execute
      * @throws RconException If the RCON command fails in being executed
      * @throws NullPointerException If the given <tt>String</tt> is <tt>null</tt>
      * @return The server response to the RCON command
      **/
     public String write(String command) throws RconException, NullPointerException {
         return this.rcon.sendRead(checkNotNull(command));
     }
      
 }
