 /**
  * Copyright (C) 2011 Daniel Kurashige-Gollub, daniel@kurashige-gollub.de
  * Please see the README file for details.
  * 
  * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
  * in compliance with the License. You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software distributed under the License
  * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
  * or implied. See the License for the specific language governing permissions and limitations under
  * the License.
  */
 
 package de.kurashigegollub.dev.gcatest;
 
 import com.google.api.client.util.DateTime;
 import de.kurashigegollub.com.google.calender.CalendarEntry;
 import de.kurashigegollub.com.google.calender.CalendarFeed;
 import de.kurashigegollub.com.google.calender.Entry;
 import de.kurashigegollub.com.google.calender.EventEntry;
 import de.kurashigegollub.com.google.calender.EventFeed;
 import de.kurashigegollub.com.google.calender.Feed;
 import java.io.UnsupportedEncodingException;
 import java.net.URLEncoder;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 /**
  * Copyright by Daniel Kurashige-Gollub, 2011
  * @author Daniel Kurashige-Gollub, daniel@kurashige-gollub.de
  */
 public class HtmlView {
 
     public static String createListHtml(String baseUrl, Feed feed, DateTime dtnow, DateTime dt2weeks,
                                         String backUrl) {
 
         StringBuilder sb = new StringBuilder();
 
         if (feed.author != null) {
             sb.append("<span class=\"author\">Author: ");
             if (feed.author.name != null)
                 sb.append(feed.author.name);
             else
                 sb.append("&lt;no name found&gt;");
             if (feed.author.email != null && !feed.author.email.equalsIgnoreCase(feed.author.name))
                 sb.append(", ").append(feed.author.email);
             sb.append("</span><br><br>\n");
         }        
         
         //CalendarFeed and EventFeed are mutual exclusive classes, so this is okay to do.
         CalendarFeed cf = null;
         if (feed instanceof CalendarFeed)
             cf = (CalendarFeed) feed;
         EventFeed ef = null;
         if (feed instanceof EventFeed)
             ef = (EventFeed) feed;
         
         if (cf != null) {
             sb.append("<p>Please select a calendar to see its events for the next two weeks, from ");
             sb.append(dtnow.toString());
             sb.append(" until ");
             sb.append(dt2weeks.toString());
             sb.append(".</p>\n");                       
         }
         if (ef != null) {
             sb.append("<p>Please select the entry you want to send via your Gmail account.</p>\n");
            sb.append("<p><a href='").append(backUrl).append("'>Back to the previous page.</p>");
         }
 
         sb.append("<hr>");
 
         for (Entry entry : feed.getEntries()) {
             sb.append(createListEntryHtml(baseUrl, entry));
             sb.append("<br>");
         }       
 
         return sb.toString();
     }
 
     public static String createListEntryHtml(String baseUrl, Entry entry) {
         StringBuilder sb = new StringBuilder();
         
         String color = null;
         EventEntry event = null;
         if (entry instanceof EventEntry) {
             event = (EventEntry) entry;
         }
         CalendarEntry ce = null;
         if (entry instanceof CalendarEntry) {
             ce = (CalendarEntry)entry;
             color = ce.color != null ? ce.color.value : null;
         }
         
         sb.append("<div class=\"entry\">");
         sb.append("<span class=\"title\">");
         
         if (ce != null) {
             sb.append("<a href=\"");
             sb.append(createUrlForCalendar(baseUrl, entry.getDecodedId()));
             sb.append("\">");
         }
         sb.append(entry.title);
         sb.append("</a></span>");
         
         //sb.append("<br>");
         //sb.append("ID: ").append(entry.id).append(" - ").append(entry.getCalendarId());
         //sb.append("<br>");
         
         if (!Utils.isEmpty(color)) {
             sb.append("<br><span style='background-color:").append(color).append(";");
             sb.append("color:").append(color).append(";'>");
             sb.append("color for this calendar</span>");
         }
         
         sb.append("<br>");
         
         if (event != null && event.content != null) {
             sb.append("<span class=\"content\">");
             sb.append(event.content);
             sb.append("</span>");
             sb.append("<br>");
         }
 
         if (entry.author != null) {
             sb.append("<span class=\"author\">Author: ");
             if (entry.author.name != null)
                 sb.append(entry.author.name);
             else
                 sb.append("&lt;no name found&gt;");
             if (entry.author.email != null &&
                !entry.author.email.equalsIgnoreCase(entry.author.email)) {
 //               //for some calendars these two values may indeed be the same
 //               !entry.author.email.equalsIgnoreCase(entry.summary)
                 sb.append(", ").append(entry.author.email);
             }
             sb.append("</span>");
             sb.append("<br>");
         }
         
         sb.append("<span class=\"updated\">Updated: ");
         sb.append(entry.updated);
         sb.append("</span>");
         
         if (entry.summary != null) {
             sb.append("<br>");
             sb.append("<span class=\"summary\">Summary: ");
             sb.append(entry.summary);
             sb.append("</span>");
         }
         
         if (event != null) {
             //sb.append("<br><span class=\"id\">").append(event.getEventId()).append("</span>");
             if (event.when != null) {
                 if (event.when.startTime != null) {
                     sb.append("<br>");
                     sb.append("<span class=\"time\">Start: ");
                     sb.append(event.when.startTime);
                     sb.append("</span>");
                 }
                 if (event.when.endTime != null) {
                     sb.append("<br>");
                     sb.append("<span class=\"time\">End: ");
                     sb.append(event.when.endTime);
                     sb.append("</span>");
                 }
                 if (event.when.valueString != null) {
                     sb.append("<br>");
                     sb.append("<span class=\"info\">Info: ");
                     sb.append(event.when.valueString);
                     sb.append("</span>");
                 }
             }            
         }
         sb.append("</div>\n\n");
         return sb.toString();
     }
     
     private static String createUrlForCalendar(String base, String calendarId) {
         try {
             calendarId = URLEncoder.encode(calendarId, "utf-8");
         } catch (UnsupportedEncodingException ex) {
             Logger.getLogger(HtmlView.class.getName()).log(Level.SEVERE, null, ex);
         }
         String url = base;
         if (url.indexOf("?") != -1)
             url += "&calendarId=" + calendarId;
         else
             url += "?calendarId=" + calendarId;
         return url;
     }
 }
