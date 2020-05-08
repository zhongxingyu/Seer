 /*
  * roomstore - an irc journaller using cassandra.
  *
  * Copyright 2011-2012 MeBigFatGuy.com
  * Copyright 2011-2012 Dave Brosius
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *    http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing,
  * software distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and limitations
  * under the License.
  */
 package com.mebigfatguy.roomstore;
 
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.List;
 
 import com.datastax.driver.core.PreparedStatement;
 import com.datastax.driver.core.ResultSet;
 import com.datastax.driver.core.Row;
 import com.datastax.driver.core.Session;
 import com.datastax.driver.core.exceptions.AlreadyExistsException;
 
 public class CassandraWriter {
 
     private Session session;
     private PreparedStatement addMessagePS;
     private PreparedStatement setLastAccessPS;
     private PreparedStatement getLastAccessPS;
     private PreparedStatement getMessagePS;
     private PreparedStatement getMessagesOnDatePS;
 
     public CassandraWriter(Session s) throws Exception {
         session = s;
         setUpSchema();
         setUpStatements();
     }
 
     public void addMessage(String channel, String sender, String hostname, String message) throws Exception {
 
         Calendar dayCal = Calendar.getInstance();
         Date dateTime = dayCal.getTime();
         
         dayCal.set(Calendar.HOUR_OF_DAY, 0);
         dayCal.set(Calendar.MINUTE, 0);
         dayCal.set(Calendar.SECOND, 0);
         dayCal.set(Calendar.MILLISECOND, 0);
         Date day = dayCal.getTime();   
         
         session.execute(addMessagePS.bind(day, channel, dateTime, sender, message));
         session.execute(setLastAccessPS.bind(sender, channel, day, dateTime));
     }
 
     public Message getLastMessage(String channel, String sender) throws Exception {
 
         ResultSet rs = session.execute(getLastAccessPS.bind(sender, channel));
         
         if (!rs.isExhausted()) {
             Row row = rs.one();
             Date day = row.getDate("last_seen_day");
             Date dateTime = row.getDate("last_seen_date_time");
             
             rs = session.execute(getMessagePS.bind(day, channel, dateTime, sender));
             if (!rs.isExhausted()) {
                 row = rs.one();
                 return new Message(channel, sender, dateTime, row.getString("message"));
             }
         }
         
         return null;
     }
     
     public List<Message> getMessages(String channel, Date day) {
         
         List<Message> messages = new ArrayList<Message>();
         ResultSet rs = session.execute(getMessagesOnDatePS.bind(day, channel));
         for (Row row : rs) {
             Message m = new Message(channel, row.getString("user"), row.getDate("date_time"), row.getString("message"));
             messages.add(0, m);
         }
         
         return messages; 
     }
 
     private void setUpSchema() throws Exception {
         
         try {
             session.execute("CREATE KEYSPACE roomstore WITH replication = { 'class' : 'SimpleStrategy', 'replication_factor' : 1 }");
         } catch (AlreadyExistsException aee) {
         }
 
         try {
             session.execute("CREATE TABLE roomstore.messages (day timestamp, channel text, date_time timestamp, user text, message text, primary key(day, channel, date_time, user)) with compact storage and clustering order by (channel desc, date_time desc)");
         } catch (AlreadyExistsException aee) {
         }
         
         try {
             session.execute("CREATE TABLE roomstore.users (user text, channel text, last_seen_day timestamp, last_seen_date_time timestamp, primary key(user, channel))");
         } catch (AlreadyExistsException aee) {
         }
     }
     
     private void setUpStatements() throws Exception {
         
         addMessagePS = session.prepare("insert into roomstore.messages (day, channel, date_time, user, message) values (?,?,?,?,?)");
         setLastAccessPS = session.prepare("insert into roomstore.users (user, channel, last_seen_day, last_seen_date_time) values (?,?,?,?)");
         getLastAccessPS = session.prepare("select last_seen_day, last_seen_date_time from roomstore.users where user = ? and channel = ?");
         getMessagePS = session.prepare("select message from roomstore.messages where day = ? and channel = ? and date_time = ? and user = ?");
         getMessagesOnDatePS = session.prepare("select user, date_time, message from roomstore.messages where day = ? and channel = ?");
     }
 }
