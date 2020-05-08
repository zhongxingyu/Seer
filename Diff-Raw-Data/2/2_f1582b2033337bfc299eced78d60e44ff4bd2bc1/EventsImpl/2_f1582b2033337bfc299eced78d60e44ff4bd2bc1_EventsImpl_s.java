 /*
  * Copyright (C) 2013 StackFrame, LLC
  * This code is licensed under GPLv2.
  */
 package com.stackframe.sarariman.events;
 
 import java.sql.Connection;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.util.ArrayList;
 import java.util.Collection;
 import javax.sql.DataSource;
 
 /**
  *
  * @author mcculley
  */
 public class EventsImpl implements Events {
 
     private final DataSource dataSource;
     private final String mountPoint;
 
     public EventsImpl(DataSource dataSource, String mountPoint) {
         this.dataSource = dataSource;
         this.mountPoint = mountPoint;
     }
 
     public Event get(int id) {
         return new EventImpl(id, dataSource, mountPoint + "events");
     }
 
     public Iterable<Event> getCurrent() {
         try {
             Connection connection = dataSource.getConnection();
             try {
                 Statement s = connection.createStatement();
                 try {
                    ResultSet r = s.executeQuery("SELECT id FROM company_events WHERE (begin >= DATE(NOW()) OR end >= DATE(NOW()))");
                     try {
                         Collection<Event> c = new ArrayList<Event>();
                         while (r.next()) {
                             c.add(get(r.getInt("id")));
                         }
 
                         return c;
                     } finally {
                         r.close();
                     }
                 } finally {
                     s.close();
                 }
             } finally {
                 connection.close();
             }
         } catch (SQLException se) {
             throw new RuntimeException(se);
         }
     }
 
 }
