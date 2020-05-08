 /*
  * Funambol is a mobile platform developed by Funambol, Inc. 
  * Copyright (C) 2010 Funambol, Inc.
  * 
  * This program is free software; you can redistribute it and/or modify it under
  * the terms of the GNU Affero General Public License version 3 as published by
  * the Free Software Foundation with the addition of the following permission 
  * added to Section 15 as permitted in Section 7(a): FOR ANY PART OF THE COVERED
  * WORK IN WHICH THE COPYRIGHT IS OWNED BY FUNAMBOL, FUNAMBOL DISCLAIMS THE 
  * WARRANTY OF NON INFRINGEMENT  OF THIRD PARTY RIGHTS.
  * 
  * This program is distributed in the hope that it will be useful, but WITHOUT
  * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
  * FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more
  * details.
  * 
  * You should have received a copy of the GNU Affero General Public License 
  * along with this program; if not, see http://www.gnu.org/licenses or write to
  * the Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
  * MA 02110-1301 USA.
  * 
  * You can contact Funambol, Inc. headquarters at 643 Bair Island Road, Suite 
  * 305, Redwood City, CA 94063, USA, or at email address info@funambol.com.
  * 
  * The interactive user interfaces in modified source and object code versions
  * of this program must display Appropriate Legal Notices, as required under
  * Section 5 of the GNU Affero General Public License version 3.
  * 
  * In accordance with Section 7(b) of the GNU Affero General Public License
  * version 3, these Appropriate Legal Notices must retain the display of the
  * "Powered by Funambol" logo. If the display of the logo is not reasonably 
  * feasible for technical reasons, the Appropriate Legal Notices must display
  * the words "Powered by Funambol".
  */
 
 package com.funambol.client.test.calendar;
 
 import java.io.ByteArrayOutputStream;
 import java.util.Hashtable;
 import java.util.Vector;
 import java.util.Enumeration;
 
 import org.json.me.JSONObject;
 import org.json.me.JSONArray;
 import com.funambol.sapisync.sapi.SapiHandler;
 import com.funambol.client.source.AppSyncSource;
 import com.funambol.client.source.AppSyncSourceManager;
 import com.funambol.client.test.Robot;
 import com.funambol.client.test.basic.BasicRobot;
 import com.funambol.client.test.ClientTestException;
 import com.funambol.client.configuration.Configuration;
 import com.funambol.sync.SyncItem;
 import com.funambol.sync.SyncSource;
 import com.funambol.util.StringUtil;
 import com.funambol.util.Log;
 
 
 public abstract class CalendarRobot extends Robot {
    
     private static final String TAG_LOG = "CalendarRobot";
 
     protected static final char FOLDING_INDENT_CHAR = ' ';
 
     protected long currentEventId = -1;
 
     protected long incrementalServerItemkey = 10000000;
 
     protected String eventAsVcal = null;
 
     protected BasicRobot basicRobot;
 
     protected AppSyncSourceManager appSourceManager;
 
     protected SapiHandler sapiHandler;
 
     public CalendarRobot(BasicRobot basicRobot, AppSyncSourceManager appSourceManager) {
         this.basicRobot = basicRobot;
         this.appSourceManager = appSourceManager;
     }
 
     public CalendarRobot() {
     }
 
 
 
     public void saveEventOnServer(String summary) throws Throwable {
 
         SapiHandler sapiHandler = getSapiHandler();
         Vector params = new Vector();
 
         // Is this an add or an update?
         boolean add;
         String id = findEventIdOnServer(summary);
 
         StringBuffer jsonEvent = new StringBuffer();
         jsonEvent.append("{");
         if (id != null) {
             jsonEvent.append("\"id\":\"").append(id).append("\",");
             add = false;
         } else {
             add = true;
         }
         jsonEvent.append("\"summary\":\"").append(summary).append("\",")
                  .append("\"privacy\":\"public\",")
                  .append("\"dtstart\":\"20090310T160000\",")
                  .append("\"dtend\":\"20090310T170000\",")
                  .append("\"tzdtstart\":\"Europe/London\",")
                  .append("\"tzdtend\":\"Europe/London\"")
                  .append("}");
        params.addElement("event=" + jsonEvent.toString());
         Log.trace(TAG_LOG, "Save event: " + jsonEvent.toString());
         JSONObject resp;
         if (add) {
             resp = sapiHandler.query("calendar","eventcreate",params,null,null);
         } else {
             resp = sapiHandler.query("calendar","eventmodify",params,null,null);
         }
 
         if (id == null) {
             id = resp.getString("id");
             Log.debug(TAG_LOG, "The new event has id=" + id);
         }
     }
 
     public void saveEventOnServerAsJSON(String jsonObj) throws Throwable {
 
         // Unescape the parameter
         jsonObj = StringUtil.replaceAll(jsonObj, "&quot;","\"");
         jsonObj = StringUtil.replaceAll(jsonObj, "&comma;",",");
 
         Log.trace(TAG_LOG, "JSON event is: " + jsonObj); 
 
         JSONObject jsonEvent = new JSONObject(jsonObj);
         // If the values are not available, an excpetion is thrown
         String summary = jsonEvent.getString("summary");
 
         Log.trace(TAG_LOG, "summary=" + summary);
 
         SapiHandler sapiHandler = getSapiHandler();
         Vector params = new Vector();
 
         // Is this an add or an update?
         String id = findEventIdOnServer(summary);
 
         Log.trace(TAG_LOG, "Server id=" + id);
         boolean add;
 
         if (id != null) {
             jsonEvent.put("id",id);
             add = false;
         } else {
             add = true;
         }
 
        params.addElement("data=" + jsonEvent.toString());
         JSONObject resp;
         if (add) {
             resp = sapiHandler.query("calendar","eventcreate",params,null,null);
         } else {
             resp = sapiHandler.query("calendar","eventmodify",params,null,null);
         }
 
         if (id == null) {
             id = resp.getString("id");
             Log.debug(TAG_LOG, "The new event has id=" + id);
         }
     }
 
     private String findEventIdOnServer(String summary) throws Throwable {
         SapiHandler sapiHandler = getSapiHandler();
 
         Vector params = new Vector();
         JSONObject range = new JSONObject();
         range.put("from","20080101");
         range.put("to","20120101");
         params.addElement("range="+range.toString());
         JSONObject resp = sapiHandler.query("calendar","get",params,null,null);
         if (resp.has("calendars")) {
             JSONArray calendars = resp.getJSONArray("calendars");
 
             for(int j=0;j<calendars.length();++j) {
                 JSONObject calendar = calendars.getJSONObject(j);
                 if (calendar.has("events")) {
                     JSONArray events = calendar.getJSONArray("events");
                     for(int i=0;i<events.length();++i) {
                         JSONObject e = events.getJSONObject(i);
                         String s = e.getString("summary");
 
                         if (summary.equals(s)) {
                             String id = e.getString("id");
                             return id;
                         }
                     }
                 }
             }
         }
         return null;
     }
 
 
     public void deleteEventOnServer(String summary) throws Throwable {
 
         String id = findEventIdOnServer(summary);
         if (id == null) {
             throw new ClientTestException("Event not available on server");
         }
         Log.trace(TAG_LOG, "Deleting event on server with key: " + id);
         SapiHandler sapiHandler = getSapiHandler();
         Vector params = new Vector();
         JSONObject param = new JSONObject();
         param.put("id", id);
        params.addElement("event="+param.toString());
         sapiHandler.query("calendar","eventdelete",params,null,null);
     }
 
     public void deleteAllEventsOnServer() throws Throwable {
 
         // Do this via SAPI to increase performance
         SapiHandler sapiHandler = getSapiHandler();
         sapiHandler.query("calendar","reset",null,null,null);
     }
 
     public void checkItemsCountOnServer(int count) throws Throwable {
 
         SapiHandler sapiHandler = getSapiHandler();
         JSONObject resp = sapiHandler.query("calendar","count",null,null,null);
         int serverCount = resp.getInt("count");
         assertTrue(count, serverCount, "Server events count mismatch");
     }
 
     public void setEventAsVCal(String vCal) throws Throwable{
         String[] sep = new String[]{"\\r\\n"};
         String[] parts = StringUtil.split(vCal, sep);
 
         ByteArrayOutputStream ostream = new ByteArrayOutputStream();
         for (int i=0;i<parts.length;i++){
             ostream.write(parts[i].getBytes());
             ostream.write("\r\n".getBytes());
         }
         eventAsVcal = ostream.toString();
         ostream.close();
     }
 
     public void setEventFromServer(String vCal) throws Throwable {
         vCal = StringUtil.replaceAll(vCal, "\\r\\n", "\r\n");
         Enumeration sources = getAppSyncSourceManager().getWorkingSources();
         AppSyncSource appSource = null;
         while(sources.hasMoreElements()) {
             appSource = (AppSyncSource)sources.nextElement();
             if (appSource.getId() == AppSyncSourceManager.EVENTS_ID) {
                 break;
             }
         }
         // We add an item via the SyncSource
         SyncSource source = appSource.getSyncSource();
         SyncItem item = new SyncItem("guid", "text/x-vcalendar", SyncItem.STATE_NEW, null);
         item.setContent(vCal.getBytes("UTF-8"));
 
         Vector items = new Vector();
         items.addElement(item);
         source.applyChanges(items);
     }
 
     protected AppSyncSourceManager getAppSyncSourceManager() {
         return appSourceManager;
     }
 
     private String cleanField(String fieldName, String value, Hashtable supportedValues) {
         String filter = (String)supportedValues.get(fieldName); 
         if (filter != null) {
             if (Log.isLoggable(Log.TRACE)) {
                 Log.trace(TAG_LOG, "Found filter for field: " + fieldName + "," + filter);
             }
             String values[] = StringUtil.split(value, ";");
             String filters[] = StringUtil.split(filter, ";");
             String res = "";
 
             for(int i=0;i<values.length;++i) {
                 String v = values[i];
                 boolean include;
                 if (i<filters.length) {
                     String f = filters[i];
                     if (f.length() > 0) {
                         include = true;
                     } else {
                         include = false;
                     }
                 } else {
                     include = true;
                 }
 
                 if (include) {
                     res = res + v;
                 }
                 if (i != values.length - 1) {
                     res = res + ";";
                 }
             }
             return res;
 
         } else {
             return value;
         }
     }
 
     private Vector getFieldsVector(String vcard) {
 
         String sep[] = {"\r\n"};
         String lines[] = StringUtil.split(vcard, sep);
 
         Vector fieldsAl = new Vector();
         String field = "";
         for(int i=0;i<lines.length;++i) {
             String line = lines[i];
             if(line.length() > 0 && line.charAt(0) == FOLDING_INDENT_CHAR) {
                 // this is a multi line field
                 field += line.substring(1); // cut the indent char
             } else {
                 if(!field.equals("")) {
                     fieldsAl.addElement(field);
                 }
                 field = line;
             }
         }
         // add the latest field
         fieldsAl.addElement(field);
 
         return fieldsAl;
     }
 
     private SapiHandler getSapiHandler() {
         if (sapiHandler == null) {
             Configuration configuration = getConfiguration();
             sapiHandler = new SapiHandler(StringUtil.extractAddressFromUrl(configuration.getSyncUrl()),
                                           configuration.getUsername(),
                                           configuration.getPassword());
 
         }
         return sapiHandler;
     }
 
 
 
     public abstract void createEmptyEvent() throws Throwable;
     public abstract void setEventField(String field, String value) throws Throwable;
     public abstract void loadEvent(String summary) throws Throwable;
     public abstract void saveEvent() throws Throwable;
     public void saveEvent(boolean save) throws Throwable { }
     public abstract void deleteEvent(String summary) throws Throwable;
     public abstract void deleteAllEvents() throws Throwable;
     public abstract void createEmptyRawEvent() throws Throwable;
     public abstract void setRawEventField(String fieldName, String fieldValue) throws Throwable;
     public abstract void setRawReminderField(String fieldName, String fieldValue) throws Throwable;
     public abstract void saveRawEvent() throws Throwable;
     public abstract void checkRawEventField(String fieldName, String fieldValue) throws Throwable;
     public abstract void checkRawReminderField(String fieldName, String fieldValue) throws Throwable;
     public abstract void checkRawEventAsVCal(String vcal) throws Throwable;
     protected abstract String getCurrentEventVCal() throws Throwable;
     protected abstract Configuration getConfiguration();
 
 
 }
