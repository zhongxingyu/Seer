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
 package silvertrout.plugins.trace;
 
 import java.util.regex.Pattern;
 import java.util.regex.Matcher;
 
 
 
 import silvertrout.commons.EscapeUtils;
 import silvertrout.commons.ConnectHelper;
 import silvertrout.Channel;
 import silvertrout.User;
 
 import silvertrout.commons.Base64Coder;
 
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
 public class Trace extends silvertrout.Plugin {
 
     /**
      *
      * @param eniroInformation
      * @return
      */
     public static String getName(String eniroInformation) {
        return getStuff(eniroInformation, "(?is)fileas=\"([^\"]+)");
     }
 
     /**
      *
      * @param eniroInformation
      * @return
      */
     public static String getAddress(String eniroInformation) {
         return getStuff(eniroInformation, "(?is)<span class=\"street-address\">([^<]+)");
     }
 
     /**
      *
      * @param eniroInformation
      * @return
      */
     public static String getPostalCode(String eniroInformation) {
         return getStuff(eniroInformation, "(?is)<span class=\"postal-code\">([^<]+)").replaceAll("\\D", "");
     }
 
     /**
      *
      * @param eniroInformation
      * @return
      */
     public static String getLocation(String eniroInformation) {
         return getStuff(eniroInformation, "(?is)<span class=\"locality\">([^<]+)").replaceAll("\\s+", "");
     }
 
     /**
      *
      * @param m
      * @param pattern
      * @return
      */
     public static String getStuff(String m, String pattern) {
         Matcher mt = Pattern.compile(pattern).matcher(m);
         if (mt.find()) {
             return EscapeUtils.unescapeHtml(mt.group(1));
         } else {
             return null;
         }
     }
 
     /**
      *
      * @param phoneNumber
      * @return
      */
     public static String getEniroInformation(String phoneNumber) {
         return ConnectHelper.Connect("http", "personer.eniro.se", "/query?search_word=" + phoneNumber, 80, 16384);
     }
 
     /**
      *
      * @param upplysningarInformation
      * @return
      */
     public static String getSSN(String upplysningarInformation) {
         String ssn = Base64Coder.decodeString(getStuff(upplysningarInformation, "(?is)show\\.aspx\\?id=([^\"]+)")).replaceAll("\\D", "");
         return ssn.substring(0, 8) + "-" + ssn.substring(8);
     }
 
     /**
      *
      * @param name
      * @param location
      * @return
      */
     public static String getUpplysningarInformation(String name, String location) {
         return ConnectHelper.Connect("http", "www.upplysning.se", "/search.aspx?bs=S%F6k&what=" + name + "&where=" + location, 80, 16384);
     }
 
     @Override
     public void onPrivmsg(User user, Channel channel, String message) {
         if (channel != null) {
             String[] parts = message.split("\\s");
             if (parts.length == 2 && parts[0].equals("!trace")) {
                 String ei = getEniroInformation(parts[1]);
                 String ret = "";
                 try {
                     String location = getLocation(ei);
                     ret = getName(ei) + ", " + getAddress(ei) + "   " + getPostalCode(ei) + " " + location + "    ";
                     location = java.net.URLEncoder.encode(location, "iso-8859-1");
                     String ui = getUpplysningarInformation(getName(ei).replaceAll(" ", "+"), getPostalCode(ei) + "+" + location);
                     ret += getSSN(ui);
                 } catch (Exception e) {
                     e.printStackTrace();
                 //sret = "Need more intertubez!";
                 }
                 channel.sendPrivmsg(user.getNickname() + ": " + ret);
             }
         }
     }
 }
