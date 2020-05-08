 /*   _______ __ __                    _______                    __   
  *  |     __|__|  |.--.--.-----.----.|_     _|.----.-----.--.--.|  |_ 
  *  |__     |  |  ||  |  |  -__|   _|  |   |  |   _|  _  |  |  ||   _|
  *  |_______|__|__| \___/|_____|__|    |___|  |__| |_____|_____||____|
  * 
  *  Copyright 2008 - Gustav Tiger, Henrik Steen and Gustav "Gussoh" Sohtell
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
 package silvertrout.plugins.titlegiver;
 
 
 import java.util.regex.Pattern;
 import java.util.regex.Matcher;
 
 import java.util.List;
 
 
 import silvertrout.commons.EscapeUtils;
 import silvertrout.commons.ConnectHelper;
 import silvertrout.Channel;
 import silvertrout.User;
 
 /**
  * JBT-plugin to fetch a web page's title and print it to the channel
  * 
  * Beta version with URL and URLConnection support. Tries to do some charset
  * conversion based on HTTP headers. TODO: Check meta tag for additional
  * information about charset.
  *
  * @author reggna
  * @author tigge
  * @version Beta 3.0
  */
 public class TitleGiver extends silvertrout.Plugin {
 
     // Max content length (in bytes) to grab to check for header
     private static final int maxContentLength = 4096;
 
     @Override
     public void onPrivmsg(User user, Channel channel, String message) {
         List<String> s = getTitles(message);
         for (String t : s) {
             if (channel != null) {
                 channel.sendPrivmsg(t);
             } else {
                 getNetwork().getConnection().sendPrivmsg(user.getNickname(), t);
             }
         }
     }
 
     /**
      *
      * @param message
      * @return
      */
     public static List<String> getTitles(String message) {
         java.util.ArrayList<String> r = new java.util.ArrayList<String>();
         Pattern p = Pattern.compile("(http|https):\\/\\/([\\w\\.-]+)(?:\\:" +
                 "(\\d+))?([\\/\\_\\+\\-\\w\\?\\#\\%\\&\\(\\)\\.\\=]*)?");
         Matcher m = p.matcher(message);
         while (m.find()) {
             String title = getTitle(m.group(0), m.group(1), m.group(2), m.group(3), m.group(4));
             if (title != null && !title.equals("")) {
                 r.add(title);
             }
         }
         return r;
     }
 
     /**
      *
      * @param url
      * @param connection
      * @param server
      * @param port
      * @param file
      * @return
      */
     public static String getTitle(String url, String connection, String server, String port, String file) {
         if (port != null) {
             return getTitle(url, connection, server, Integer.parseInt(port), file);
         } else {
             return getTitle(url, connection, server, 0, file);
         }
     }
 
     /**
      *
      * @param url
      * @param connection
      * @param server
      * @param port
      * @param file
      * @return
      */
     public static String getTitle(String url, String connection, String server, int port, String file) {
 
         /* Set current port */
         if (port == 0) {
             if (connection.equals("https")) {
                 port = 443;
             } else {
                 port = 80;
             }
         }
 
         // Find title
         String page = ConnectHelper.Connect(connection, server, file, port, maxContentLength * 10);
         if (page != null) {
             String titlePattern = "(?is)<title>([^<]+)";
             Pattern pt = Pattern.compile(titlePattern);
             Matcher mt = pt.matcher(page);
 
             if (mt.find()) {
                 String title = mt.group(1);
                 return EscapeUtils.unescapeHtml(title.replaceAll("\\s+", " ").trim());
             }
             System.out.println("No title found");
         }
         System.out.println("Could be no content (if no title found)");
         System.out.println("Failed on fetching title for some reason");
         return null;
 
     }
 
     /* For offline test: */
     /*public static void main(String args[]) {
     List<String> s = getTitles(args[0]);
     for(String t: s) {
     System.out.println("Title: " +t);
     }
     }*/
 }
