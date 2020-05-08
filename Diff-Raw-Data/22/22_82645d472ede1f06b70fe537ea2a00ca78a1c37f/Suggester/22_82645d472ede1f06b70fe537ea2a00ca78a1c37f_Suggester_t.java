 /*   _______ __ __                    _______                    __   
  *  |     __|__|  |.--.--.-----.----.|_     _|.----.-----.--.--.|  |_ 
  *  |__     |  |  ||  |  |  -__|   _|  |   |  |   _|  _  |  |  ||   _|
  *  |_______|__|__| \___/|_____|__|    |___|  |__| |_____|_____||____|
  * 
  *  Copyright 2008 - Gustav Tiger, Henrik Steen and Gustav Sothell
  * 
  *  This program is free software: you can redistribute it and/or modify
  *  it under the terms of the GNU General Public License as published by
  *  the Free Software Foundation, either version 3 of the License, or
  *  (at your option) any later version.
  * 
  *  This program is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU General Public License for more details.
  * 
  *  You should have received a copy of the GNU General Public License
  *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
  *
  */
 package silvertrout.plugins;
 
 import java.nio.ByteBuffer;
 
 import java.util.regex.Pattern;
 import java.util.regex.Matcher;
 
 import java.util.HashSet;
 import java.util.HashMap;
 
 import java.net.URL;
 import java.net.HttpURLConnection;
 
 import silvertrout.Channel;
 import silvertrout.User;
 import silvertrout.commons.ConnectHelper;
 
 /**
  * JBT-plugin to fetch google suggestions
  * 
  * Beta version with URL and URLConnection support. Tries to do some charset
  * conversion based on HTTP headers. TODO: Check meta tag for additional
  * information about charset.
  *
  * @author reggna
  * @author tigge
  * @version Beta 3.0
  */
 public class Suggester extends silvertrout.Plugin {
     /* connection information */
 
     private static final int port = 80;
     private static final String connection = "http";
     private static final String server = "google.se";
     private static final String file = "/search?&q=";
     /* Max content length (in bytes) to grab to check for header */
     private static final int maxContentLength = 16384;
     /* A list containing words that will not be checked */
     private HashSet<String> blackList;
     /* A map containing all words that have previously been checked, and any */
     /* suggestions connected to that word */
     private HashMap<String, String> old;
 
     /**
      * Constructor, initiates the hashmap och hashset
      * Adding some words to the blackList
      */
     public Suggester() {
         blackList = new HashSet<String>();
         old = new HashMap<String, String>();
         blackList.add("hej");
     }
 
     @Override
     public void onPrivmsg(User user, Channel channel, String message) {
         /* restrict to the channel #it06 for test purpose */
         if (channel == null || !channel.getName().equals("#it06")) {
             return;
         }
         for (String s : message.split(" ")) {
             /* check if the message contains a username, if so: change the message */
             /* to something in the blacklist */
             for (User u : channel.getUsers().keySet()) {
                 if (s.contains(u.getNickname())) {
                     s = "hej";
                 }
             }
 
             /* if the message is in the blacklist, go to the next one */
             if (blackList.contains(s)) {
                 continue;
             }
 
             /* do we have this word in our "vocabulary" (old), else we check google */
             String t;
             if (old.containsKey(s)) {
                 t = old.get(s);
             } else {
                 t = getSuggestion(s);
             }
             old.put(s, t);
 
             /* if we have found a suggestion, print it to the channel */
             if (t != null) {
                 channel.sendPrivmsg(t);
             }
         }
     }
 
     /**
      * Method that google a word, and returns the "did you mean: ..."-suggestion
      * connected to that word
      *
      *@param s the string to check for suggestion
      *@return The google "did you mean: ..."-suggestion, or null if not found
      */
    private static String getSuggestion(String s) {
        String suggestion = ConnectHelper.Connect(connection, server, file + s, 
            port, maxContentLength);
        suggestion = suggestion.substring(suggestion.indexOf("class=p><b><i>")+14);
        suggestion = suggestion.substring(0, suggestion.indexOf("<"));
        if(suggestion.indexOf(">") > 0) return null;
        return suggestion;
    }
    
    public static void main(String[] args){
      System.out.println(getSuggestion(args[0]));
     }
 }
