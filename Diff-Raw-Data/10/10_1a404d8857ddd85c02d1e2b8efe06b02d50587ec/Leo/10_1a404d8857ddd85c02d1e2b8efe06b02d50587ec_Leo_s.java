 /** 
  Project "com.quui.chat.core" (C) 2006 Fabian Steeg
 
  This library is free software; you can redistribute it and/or modify it under the terms of the GNU Lesser General Public License as published by the Free Software Foundation; either version 2.1 of the License, or (at your option) any later version.
 
  This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.
 
  You should have received a copy of the GNU Lesser General Public License along with this library; if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
  */
 package com.quui.chat.commands;
 
 import java.util.List;
 
 /**
  * dict.leo.org based german-english and english-german translation
  * 
  * @author Fabian Steeg (fsteeg)
  */
 public class Leo {
     // leo adress for search queries
     private final static String ADRESS = "http://dict.leo.org/?lp=ende&lang=de&searchLoc=0&cmpType=relaxed&relink=on&sectHdr=on&spellToler=on&search=";
 
     // regex to match and capture the word or expressions in both languages from
     // the pretty messy leo html
     private final static String REGEX = "<td class=\"td1\" nowrap width=\"5%\">(.*?)<td class=\"td1\" nowrap width=\"2%\">&nbsp;</td>.*?<td class=\"td1\" valign=\"middle\" width=\"43%\">(.*?)</td>";
 
     // the encoding the leo site uses
     private final static String ENCODING = "iso-8859-15";
 
     /**
      * @param word
      *            The german or english word to look up
      * @return The result, containing multiple words, english and german
      */
     public static String lookup(String word) {
         String lookupAdress = ADRESS + word;
         WebsiteLookup lookup = new WebsiteLookup(lookupAdress, ENCODING);
         List<String> results = lookup.match(REGEX);
         String result = "[" + word + "] ";
         for (String s : results) {
             // abbreviate the query word:
             String changed = s.toLowerCase().replaceAll(word.toLowerCase(),
                     word.charAt(0) + ".").trim();
             if (s.equals(WebsiteLookup.DELIM))
                 result = result.trim() + "; ";
             else
                 result = result + "\"" + changed + "\"" + " ";
         }
        return result;
     }
 }
