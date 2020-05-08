 /*
  * Copyright (c) 2006, 2008, 2011 Lucas Holt
  * All rights reserved.
  *
  * Redistribution and use in source and binary forms, with or without
  * modification, are permitted provided that the following conditions
  * are met:
  * 1. Redistributions of source code must retain the above copyright
  *    notice, this list of conditions and the following disclaimer.
  * 2. Redistributions in binary form must reproduce the above copyright
  *    notice, this list of conditions and the following disclaimer in the
  *    documentation and/or other materials provided with the distribution.
  *
  * THIS SOFTWARE IS PROVIDED BY THE AUTHOR AND CONTRIBUTORS ``AS IS'' AND
  * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
  * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
  * ARE DISCLAIMED.  IN NO EVENT SHALL THE AUTHOR OR CONTRIBUTORS BE LIABLE
  * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
  * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS
  * OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
  * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT
  * LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY
  * OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
  * SUCH DAMAGE.
  */
 
 package com.justjournal.core;
 
 import com.justjournal.db.SQLHelper;
 import org.apache.cayenne.CayenneRuntimeException;
 import org.apache.cayenne.ObjectContext;
 import org.apache.cayenne.access.DataContext;
 import org.apache.cayenne.query.SelectQuery;
 import org.apache.log4j.Logger;
 
 import java.sql.ResultSet;
 import java.util.List;
 
 /**
  * Track the number of users, entry and comment statistics, and other information.
  *
  * @author Lucas Holt
  * @version $Id: Statistics.java,v 1.8 2012/06/23 18:15:31 laffer1 Exp $
  */
 public class Statistics {
     private static Logger log = Logger.getLogger(Statistics.class.getName());
 
     /**
      * Determine the number of users registered.
      *
      * @return The number of users or -1 on an error.
      */
     public int getUsers() {
         int count = -1;
         try {
             ObjectContext dataContext = DataContext.getThreadObjectContext();
 
             SelectQuery query = new SelectQuery(com.justjournal.model.User.class);
             List list = dataContext.performQuery(query);
             count = list.size();
         } catch (Exception e) {
             log.error("users(): " + e.getMessage());
         }
 
         return count;
     }
 
     /**
      * Determine the number of journal entries posted.
      *
      * @return The number of entries or -1 on error.
      */
     public int getEntries() {
        try {
            ObjectContext dataContext = DataContext.getThreadObjectContext();
            SelectQuery query = new SelectQuery(com.justjournal.model.Entry.class);
            List list = dataContext.performQuery(query);
            return list.size();
        } catch (CayenneRuntimeException ce) {
            log.error(ce);
        }
        return -1;
     }
 
     /**
      * Percentage of public entries as compared to total.
      *
      * @return public entries as %
      */
     public float getPublicEntries() {
         float percent;
         String sql = "SELECT count(*) FROM entry WHERE security='2';";
 
         percent = (((float) sqlCount(sql) / (float) getEntries()) * 100);
 
         if (log.isDebugEnabled())
             log.debug("publicEntries(): percent is " + percent);
 
         return percent;
     }
 
     /**
      * Percentage of friends entries as compared to total.
      *
      * @return friends entries as %
      */
     public float getFriendsEntries() {
         float percent;
         String sql = "SELECT count(*) FROM entry WHERE security='1';";
         percent = (((float) sqlCount(sql) / (float) getEntries()) * 100);
 
         if (log.isDebugEnabled())
             log.debug("friendsEntries(): percent is " + percent);
 
         return percent;
     }
 
     /**
      * Percentage of private entries as compared to total.
      *
      * @return private entries as %
      */
     public float getPrivateEntries() {
         float percent;
         String sql = "SELECT count(*) FROM entry WHERE security='0';";
         percent = (((float) sqlCount(sql) / (float) getEntries()) * 100);
 
         if (log.isDebugEnabled())
             log.debug("privateEntries(): percent is " + percent);
 
         return percent;
     }
 
     /**
      * Determine the number of comments posted.
      *
      * @return The number of comments or -1 on error.
      */
     public int getComments() {
         try {
             ObjectContext dataContext = DataContext.getThreadObjectContext();
             SelectQuery query = new SelectQuery(com.justjournal.model.Comments.class);
             List list = dataContext.performQuery(query);
             return list.size();
         } catch (CayenneRuntimeException ce) {
             log.error(ce);
         }
         return -1;
     }
 
     /**
      * Determine the number of journal styles posted.
      *
      * @return The number of styles or -1 on error.
      */
     public int getStyles() {
         try {
             ObjectContext dataContext = DataContext.getThreadObjectContext();
             SelectQuery query = new SelectQuery(com.justjournal.model.Style.class);
             List list = dataContext.performQuery(query);
             return list.size();
         } catch (CayenneRuntimeException ce) {
             log.error(ce);
         }
         return -1;
     }
 
     /**
      * Determine the number of Tags used on the site.
      *
      * @return tag count or -1 on error.
      */
     public int getTags() {
         try {
             ObjectContext dataContext = DataContext.getThreadObjectContext();
             SelectQuery query = new SelectQuery(com.justjournal.model.Tags.class);
             List list = dataContext.performQuery(query);
             return list.size();
         } catch (CayenneRuntimeException ce) {
             log.error(ce);
         }
         return -1;
     }
 
     /**
      * Perform a sql query returning a scalar int value typically form a count(*)
      *
      * @param sql query to perform
      * @return int scalar from sql query
      */
     private int sqlCount(String sql) {
         int count = -1;
 
         if (log.isDebugEnabled())
             log.debug("sqlCount(): sql is " + sql);
 
         try {
             ResultSet rs = SQLHelper.executeResultSet(sql);
 
             if (rs.next()) {
                 count = rs.getInt(1);
             }
 
             rs.close();
         } catch (Exception e) {
             log.error("sqlCount(): " + e.getMessage());
         }
 
         if (log.isDebugEnabled())
             log.debug("sqlCount(): count is " + count);
 
         return count;
     }
 
 }
