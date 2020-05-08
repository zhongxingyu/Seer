 /*
 Copyright (c) 2005, Lucas Holt
 All rights reserved.
 
 Redistribution and use in source and binary forms, with or without modification, are
 permitted provided that the following conditions are met:
 
   Redistributions of source code must retain the above copyright notice, this list of
   conditions and the following disclaimer.
 
   Redistributions in binary form must reproduce the above copyright notice, this
   list of conditions and the following disclaimer in the documentation and/or other
   materials provided with the distribution.
 
   Neither the name of the Just Journal nor the names of its contributors
   may be used to endorse or promote products derived from this software without
   specific prior written permission.
 
 THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND
 CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES,
 INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
 INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE
 GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR
 BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY
 OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR
 TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY
 OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 POSSIBILITY OF SUCH DAMAGE.
 */
 
 package com.justjournal.db;
 
 import org.apache.log4j.Category;
 import sun.jdbc.rowset.CachedRowSet;
 
 import java.util.ArrayList;
 import java.util.Collection;
 
 /**
  * Provides access to journal entries in the data tier.  Several parts of this
  * class could be optimized.  We are using several database connects when only
  * one is needed.
  *
  * @author Lucas Holt
  * @version 1.1
  * @see EntryTo
  * @since 1.0
  *        User: laffer1
  *        Date: Sep 20, 2003
  *        Time: 8:48:24 PM
  *        <p/>
  *        1.1 Fixed a bug selecting older entries.
  *        1.0 initial release
  */
 public final class EntryDAO {
     private final static int MAX_ENTRIES = 20;
     private final static int MAX_FRIENDS = 15;
 
     private static final Category log = Category.getInstance(EntryDAO.class.getName());
 
     /**
      * Adds a journal entry to the data store.
      *
      * @param et journal entry object to be added
      * @return true if no error occured.
      */
     public static boolean add(final EntryTo et) {
         boolean noError = true;
         int records = 0;
         String allowComments = "Y";
         String emailComments = "Y";
         String autoFormat = "Y";
 
         if (!et.getAllowComments())
             allowComments = "N";
 
         if (!et.getEmailComments())
             emailComments = "N";
 
         if (!et.getAutoFormat())
             autoFormat = "N";
 
         final String sqlStmt =
                 new StringBuffer().append("INSERT INTO entry (id,uid,date,subject,mood,music,location,body,security,allow_comments,email_comments,autoformat) values(NULL,'").append(et.getUserId()).append("','").append(et.getDate()).append("','").append(et.getSubject()).append("','").append(et.getMoodId()).append("','").append(et.getMusic()).append("','").append(et.getLocationId()).append("','").append(et.getBody()).append("','").append(et.getSecurityLevel()).append("','").append(allowComments).append("','").append(emailComments).append("','").append(autoFormat).append("');").toString();
 
         try {
             records = SQLHelper.executeNonQuery(sqlStmt);
         } catch (Exception e) {
             noError = false;
         }
 
         if (records != 1)
             noError = false;
 
         return noError;
     }
 
     /**
      * Update a journal entry with different information.  User can alter
      * date, subject, body, security, mood, music, and location.
      *
      * @param et journal entry that needs to be altered.
      * @return true if no error occured.
      */
     public static boolean update(final EntryTo et) {
         boolean noError = true;
         int records = 0;
         String allowComments = "Y";
         String emailComments = "Y";
         String autoFormat = "Y";
 
         if (et.getId() > 0) {
             if (!et.getAllowComments())
                 allowComments = "N";
 
             if (!et.getEmailComments())
                 emailComments = "N";
 
             if (!et.getAutoFormat())
                 autoFormat = "N";
 
             final String sqlStmt =
                     new StringBuffer().append("Update entry SET date='").append(et.getDate().toString()).append("', subject='").append(et.getSubject()).append("', body='").append(et.getBody()).append("', security='").append(et.getSecurityLevel()).append("', location='").append(et.getLocationId()).append("', mood='").append(et.getMoodId()).append("', music='").append(et.getMusic()).append("', allow_comments='").append(allowComments).append("', email_comments='").append(emailComments).append("', autoformat='").append(autoFormat).append("' WHERE id='").append(et.getId()).append("' LIMIT 1;").toString();
 
             try {
                 records = SQLHelper.executeNonQuery(sqlStmt);
             } catch (Exception e) {
                 noError = false;
             }
 
             if (records != 1)
                 noError = false;
         } else {
             noError = false;
         }
 
         return noError;
     }
 
     public static boolean delete(final int entryId, final int userId) {
         boolean noError = true;
         int records = 0;
 
         final String sqlStmt = "DELETE FROM entry WHERE id='" + entryId + "' AND uid='" + userId + "' LIMIT 1;";
 
         if (entryId > 0 && userId > 0) {
             try {
                 records = SQLHelper.executeNonQuery(sqlStmt);
             } catch (Exception e) {
                 noError = false;
             }
         } else {
             noError = false;
         }
 
         if (records != 1)
             noError = false;
 
         return noError;
     }
 
     public static EntryTo viewSingle(final int entryId, final int userId) {
         final EntryTo et = new EntryTo();
         CachedRowSet rs = null;
         CachedRowSet rsComment = null;
         String sqlStmt2;
         String sqlStatement;
 
         sqlStatement = new StringBuffer().append("SELECT us.id As id, us.username, ").append("eh.date As date, eh.subject As subject, eh.music, eh.body, ").append("mood.title As moodt, location.title As location, eh.id As entryid, ").append("eh.security as security, eh.autoformat, eh.allow_comments, eh.email_comments, location.id as locationid, mood.id as moodid ").append("FROM user As us, entry As eh, ").append("mood, location ").append("WHERE eh.id='").append(entryId).append("' AND eh.uid='").append(userId).append("' AND us.id=eh.uid AND mood.id=eh.mood AND location.id=eh.location ORDER BY eh.date DESC, eh.id DESC LIMIT 1;").toString();
 
         try {
             rs = SQLHelper.executeResultSet(sqlStatement);
 
             if (rs.next()) {
 
                 et.setUserName(rs.getString("username"));
 
                 et.setId(rs.getInt("entryid"));
                 et.setUserId(rs.getInt("id"));
                 et.setDate(rs.getString("date"));
                 et.setSubject(rs.getString("subject"));
                 et.setBody(rs.getString("body"));
                 et.setLocationId(rs.getInt("locationid"));
                 et.setMoodId(rs.getInt("moodid"));
                 et.setMusic(rs.getString("music"));
                 et.setSecurityLevel(rs.getInt("security"));
                 et.setMoodName(rs.getString("moodt"));
                 et.setLocationName(rs.getString("location"));
 
                 if (rs.getString("email_comments").compareTo("Y") == 0)
                     et.setEmailComments(true);
                 else
                     et.setEmailComments(false);
 
                 if (rs.getString("allow_comments").compareTo("Y") == 0)
                     et.setAllowComments(true);
                 else
                     et.setAllowComments(false);
 
                 if (rs.getString("autoformat").compareTo("Y") == 0)
                     et.setAutoFormat(true);
                 else
                     et.setAutoFormat(false);
 
                 try {
                     sqlStmt2 = "SELECT count(comments.id) As comid FROM comments WHERE eid='" + rs.getString("entryid") + "';";
                     rsComment = SQLHelper.executeResultSet(sqlStmt2);
                     if (rsComment.next()) {
                         if (rsComment.getInt("comid") > 0)
                             et.setCommentCount(rsComment.getInt("comid"));
                     }
 
                     rsComment.close();
                     rsComment = null;
                 } catch (Exception ex) {
                     if (rsComment != null) {
                         try {
                             rsComment.close();
                         } catch (Exception e) {
                             // NOTHING TO DO
                         }
                     }
                 }
 
             }
 
             rs.close();
 
         } catch (Exception e1) {
             if (rs != null) {
                 try {
                     rs.close();
                 } catch (Exception e) {
                     // NOTHING TO DO
                 }
             }
         }
 
 
         return et;
 
     }
 
     public static EntryTo viewSingle(final int entryId, final boolean thisUser) {
         final EntryTo et = new EntryTo();
         CachedRowSet rs = null;
         CachedRowSet rsComment = null;
         String sqlStmt2;
         String sqlStatement;
 
         if (thisUser)  // no security
         {
             sqlStatement = new StringBuffer().append("SELECT us.id As id, us.username, ").append("eh.date As date, eh.subject As subject, eh.music, eh.body, ").append("mood.title As moodt, location.title As location, eh.id As entryid, ").append("eh.security as security, eh.autoformat, eh.allow_comments, eh.email_comments, location.id as locationid, mood.id as moodid ").append("FROM user As us, entry As eh, ").append("mood, location ").append("WHERE eh.id='").append(entryId).append("' AND us.id=eh.uid AND mood.id=eh.mood AND location.id=eh.location ORDER BY eh.date DESC, eh.id DESC LIMIT 1;").toString();
         } else {
             sqlStatement = new StringBuffer().append("SELECT us.id As id, us.username, ").append("eh.date As date, eh.subject As subject, eh.music, eh.body, ").append("mood.title As moodt, location.title As location, eh.id As entryid, ").append("eh.security as security, eh.autoformat, eh.allow_comments, eh.email_comments, location.id as locationid, mood.id as moodid ").append("FROM user As us, entry As eh, ").append("mood, location ").append("WHERE eh.id='").append(entryId).append("' AND us.id=eh.uid AND mood.id=eh.mood AND location.id=eh.location AND eh.security=2 ORDER BY eh.date DESC, eh.id DESC LIMIT 1;").toString();
         }
 
         try {
             rs = SQLHelper.executeResultSet(sqlStatement);
 
             if (rs.next()) {
 
                 et.setUserName(rs.getString("username"));
 
                 et.setId(rs.getInt("entryid"));
                 et.setUserId(rs.getInt("id"));
                 et.setDate(rs.getString("date"));
                 et.setSubject(rs.getString("subject"));
                 et.setBody(rs.getString("body"));
                 et.setLocationId(rs.getInt("locationid"));
                 et.setMoodId(rs.getInt("moodid"));
                 et.setMusic(rs.getString("music"));
                 et.setSecurityLevel(rs.getInt("security"));
                 et.setMoodName(rs.getString("moodt"));
                 et.setLocationName(rs.getString("location"));
 
                 if (rs.getString("email_comments").compareTo("Y") == 0)
                     et.setEmailComments(true);
                 else
                     et.setEmailComments(false);
 
                 if (rs.getString("allow_comments").compareTo("Y") == 0)
                     et.setAllowComments(true);
                 else
                     et.setAllowComments(false);
 
                 if (rs.getString("autoformat").compareTo("Y") == 0)
                     et.setAutoFormat(true);
                 else
                     et.setAutoFormat(false);
 
                 try {
                     sqlStmt2 = "SELECT count(comments.id) As comid FROM comments WHERE eid='" + rs.getString("entryid") + "';";
                     rsComment = SQLHelper.executeResultSet(sqlStmt2);
                     if (rsComment.next()) {
                         if (rsComment.getInt("comid") > 0)
                             et.setCommentCount(rsComment.getInt("comid"));
                     }
 
                     rsComment.close();
                     rsComment = null;
                 } catch (Exception ex) {
                     if (rsComment != null) {
                         try {
                             rsComment.close();
                         } catch (Exception e) {
                             // NOTHING TO DO
                         }
                     }
                 }
 
             }
 
             rs.close();
 
         } catch (Exception e1) {
             if (rs != null) {
                 try {
                     rs.close();
                 } catch (Exception e) {
                     // NOTHING TO DO
                 }
             }
         }
 
 
         return et;
     }
 
     public Collection<EntryTo> view(final String userName, final boolean thisUser) {
         return view(userName, thisUser, 0); // don't skip any!
     }
 
     /**
      * view retrieves journal entries from the database.
      *
      * @param userName user who own's the entries.
      * @param thisUser is the owner the one accessing the data?
      * @param skip     number of records to skip (in the past)
      * @return A <code>Collection</code> of entries.
      */
     public static Collection<EntryTo> view(final String userName, final boolean thisUser, final int skip) {
 
         if (log.isDebugEnabled())
             log.debug("view: starting view of entries.");
 
         final ArrayList<EntryTo> entries = new ArrayList<EntryTo>(MAX_ENTRIES);
 
         String sqlStatement;
         String sqlStmt2; // for comment count
         CachedRowSet rs = null;
         CachedRowSet rsComment = null;
         EntryTo et;
         final int PAGE_SIZE = 20;
 
         if (thisUser) {
             if (log.isDebugEnabled())
                 log.debug("view: this user is logged in.");
 
             // NO SECURITY RESTRICTION
             sqlStatement = "SELECT us.id As id, " +
                     "eh.date As date, eh.subject As subject, eh.music, eh.body, " +
                     "mood.title As moodt, location.title As location, eh.id As entryid, " +
                     "eh.security as security, eh.autoformat, eh.allow_comments, eh.email_comments, location.id as locationid, mood.id as moodid " +
                     "FROM user As us, entry As eh, " +
                     "mood, location " +
                     "WHERE us.userName='" + userName +
                     "' AND us.id=eh.uid AND mood.id=eh.mood AND location.id=eh.location ORDER BY eh.date DESC, eh.id DESC LIMIT "
                     + Integer.toString(skip) + "," + Integer.toString(PAGE_SIZE) + ";";
         } else {
             if (log.isDebugEnabled())
                 log.debug("view: this user is not logged in.");
 
             // PUBLIC ONLY
             sqlStatement = "SELECT us.id As id, " +
                     "eh.date As date, eh.subject As subject, eh.music, eh.body, " +
                     "mood.title As moodt, location.title As location, eh.id As entryid, " +
                     "eh.security as security, eh.autoformat, eh.allow_comments, eh.email_comments, location.id as locationid, mood.id as moodid " +
                     "FROM user As us, entry As eh, " +
                     "mood, location " +
                     "WHERE us.userName='" + userName +
                     "' AND us.id=eh.uid AND mood.id=eh.mood AND location.id=eh.location AND eh.security=2 ORDER BY eh.date DESC, eh.id DESC LIMIT "
                     + Integer.toString(skip) + "," + Integer.toString(PAGE_SIZE) + ";";
         }
 
         try {
             if (log.isDebugEnabled())
                 log.debug("view: execute sql statement");
 
             rs = SQLHelper.executeResultSet(sqlStatement);
 
             while (rs.next()) {
                 if (log.isDebugEnabled())
                     log.debug("view: create EntryTo object and populate it.");
 
                 et = new EntryTo();
 
                 et.setUserName(userName);
 
                 et.setId(rs.getInt("entryid"));
                 et.setUserId(rs.getInt("id"));
                 et.setDate(rs.getString("date"));
                 et.setSubject(rs.getString("subject"));
                 et.setBody(rs.getString("body"));
                 et.setLocationId(rs.getInt("locationid"));
                 et.setMoodId(rs.getInt("moodid"));
                 et.setMusic(rs.getString("music"));
                 et.setSecurityLevel(rs.getInt("security"));
                 et.setMoodName(rs.getString("moodt"));
                 et.setLocationName(rs.getString("location"));
 
                 if (rs.getString("email_comments").compareTo("Y") == 0)
                     et.setEmailComments(true);
                 else
                     et.setEmailComments(false);
 
                 if (rs.getString("allow_comments").compareTo("Y") == 0)
                     et.setAllowComments(true);
                 else
                     et.setAllowComments(false);
 
                 if (rs.getString("autoformat").compareTo("Y") == 0)
                     et.setAutoFormat(true);
                 else
                     et.setAutoFormat(false);
 
 
                 try {
                     sqlStmt2 = "SELECT count(comments.id) As comid FROM comments WHERE eid='" + rs.getString("entryid") + "';";
                     rsComment = SQLHelper.executeResultSet(sqlStmt2);
                     if (rsComment.next()) {
                         if (rsComment.getInt("comid") > 0)
                             et.setCommentCount(rsComment.getInt("comid"));
                     }
 
                     rsComment.close();
                     rsComment = null; // dont close twice
                 } catch (Exception ex) {
                     if (rsComment != null) {
                         try {
                             rsComment.close();
                         } catch (Exception e) {
                             // NOTHING TO DO
                         }
                     }
                 }
 
                 if (log.isDebugEnabled())
                     log.debug("view: ET contains " + et.toString());
 
                 entries.add(et);
             }
 
             rs.close();
 
         } catch (Exception e1) {
             if (log.isDebugEnabled())
                 log.debug("view: exception is: " + e1.getMessage() + "\n" + e1.toString());
 
             if (rs != null) {
                 try {
                     rs.close();
                 } catch (Exception e) {
                     // NOTHING TO DO
                 }
             }
         }
 
         return entries;
     }
 
     public static Collection<EntryTo> viewFriends(final int userID, final int aUserId)
             throws IllegalArgumentException {
         if (log.isDebugEnabled())
             log.debug("viewFriends: Initializing");
 
         final ArrayList<EntryTo> entries = new ArrayList<EntryTo>(MAX_FRIENDS);
         EntryTo et;
 
         String sqlStatement;
         String sqlStmt2; // for comment count
         CachedRowSet rs = null;
         CachedRowSet rsComment = null;
 
         if (userID < 1)
             throw new IllegalArgumentException("userID must be greater than zero");
 
        /* First get a list of friends
        TODO: FIX INFORMATION DISCLOSURE
        instead do 1:1 on user:auth user
        */

         if (aUserId > 0)
             sqlStatement =
                    new StringBuffer().append("SELECT friends.friendid As id, us.username, eh.date as date, eh.subject as subject, eh.music, eh.body, eh.security, eh.autoformat, eh.allow_comments, eh.email_comments, mood.title as mood, mood.id as moodid, location.id as locid, location.title as location, eh.id As entryid FROM user as us, entry as eh, mood, location, friends WHERE friends.id='").append(userID).append("' AND friends.friendid = eh.uid AND mood.id=eh.mood AND location.id=eh.location AND friends.friendid=us.id AND (eh.security=2 OR (eh.security=1 AND friends.friendid IN (SELECT friendid FROM friends WHERE friends.id='").append(userID).append("')) ) ORDER by eh.date DESC LIMIT 0,15;").toString();
         else if (aUserId == 0)
             // no user logged in.. just spit out public entries.
             sqlStatement =
                     new StringBuffer().append("SELECT friends.friendid As id, us.username, eh.date as date, eh.subject as subject, eh.music, eh.body, eh.security, eh.autoformat, eh.allow_comments, eh.email_comments, mood.title as mood, mood.id as moodid, location.id as locid, location.title as location, eh.id As entryid FROM user as us, entry as eh, mood, location, friends WHERE friends.id='").append(userID).append("' AND friends.friendid = eh.uid AND mood.id=eh.mood AND location.id=eh.location AND friends.friendid=us.id AND eh.security=2 ORDER by eh.date DESC LIMIT 0,15;").toString();
         else
             throw new IllegalArgumentException("aUserId must be greater than -1");
 
         if (log.isDebugEnabled())
             log.debug("viewFriends: SQL is " + sqlStatement);
 
         try {
             rs = SQLHelper.executeResultSet(sqlStatement);
 
             if (log.isDebugEnabled())
                 log.debug("viewFriends: Records returned " + rs.size());
 
             while (rs.next()) {
                 et = new EntryTo();
 
                 et.setUserName(rs.getString("username"));
 
                 et.setId(rs.getInt("entryid"));
                 et.setUserId(rs.getInt("id"));
                 et.setDate(rs.getString("date"));
                 et.setSubject(rs.getString("subject"));
                 et.setBody(rs.getString("body"));
                 et.setLocationId(rs.getInt("locid"));
                 et.setMoodId(rs.getInt("moodid"));
                 et.setMusic(rs.getString("music"));
                 et.setSecurityLevel(rs.getInt("security"));
                 et.setMoodName(rs.getString("mood"));
                 et.setLocationName(rs.getString("location"));
 
                 if (rs.getString("email_comments").compareTo("Y") == 0)
                     et.setEmailComments(true);
                 else
                     et.setEmailComments(false);
 
                 if (rs.getString("allow_comments").compareTo("Y") == 0)
                     et.setAllowComments(true);
                 else
                     et.setAllowComments(false);
 
                 if (rs.getString("autoformat").compareTo("Y") == 0)
                     et.setAutoFormat(true);
                 else
                     et.setAutoFormat(false);
 
                 try {
                     sqlStmt2 = "SELECT count(comments.id) As comid FROM comments WHERE eid='" + rs.getString("entryid") + "';";
                     rsComment = SQLHelper.executeResultSet(sqlStmt2);
 
                     if (rsComment.next()) {
                         if (rsComment.getInt("comid") > 0)
                             et.setCommentCount(rsComment.getInt("comid"));
                     }
 
                     rsComment.close();
                     rsComment = null;
                 } catch (Exception ex) {
                     if (rsComment != null) {
                         if (log.isDebugEnabled())
                             log.debug("viewFriends: Exception is " + ex.getMessage() + "\n" + ex.toString());
 
                         try {
                             rsComment.close();
                         } catch (Exception e) {
                             // NOTHING TO DO
                         }
                     }
                 }
 
                 if (log.isDebugEnabled())
                     log.debug("viewFriends: entry is " + et.toString());
 
                 entries.add(et);
             }
 
             rs.close();
             rs = null;
         } catch (Exception e1) {
             if (log.isDebugEnabled())
                 log.debug("viewFriends: Exception is " + e1.getMessage() + "\n" + e1.toString());
 
             if (rs != null) {
                 try {
                     rs.close();
                 } catch (Exception e) {
                     // NOTHING TO DO
                 }
             }
         }
 
         return entries;
     }
 
     public static CachedRowSet ViewCalendarYear(final int year,
                                                 final String userName,
                                                 final boolean thisUser)
             throws java.lang.ClassNotFoundException, java.lang.IllegalAccessException, Exception {
         String sqlStatement;
         CachedRowSet RS;
 
         if (thisUser) {
             // NO SECURITY RESTRICTION
             sqlStatement = "SELECT us.id As id, " +
                     " eh.date As date, " +
                     " eh.id As entryid " +
                     " FROM user As us, entry As eh " +
                     " WHERE us.username = '" + userName +
                     "' AND YEAR(eh.date)=" + year + " AND us.id = eh.uid ORDER BY eh.date DESC, eh.id DESC;";
         } else {
             // PUBLIC ONLY
             sqlStatement = "SELECT us.id As id, " +
                     " eh.date As date, " +
                     " eh.id As entryid " +
                     "FROM user As us, entry As eh WHERE us.username = '" + userName +
                     "' AND YEAR(eh.date)=" + year + " AND us.id = eh.uid AND eh.security = '2' ORDER BY eh.date DESC, eh.id DESC;";
         }
 
         RS = SQLHelper.executeResultSet(sqlStatement);
 
         return RS;
     }
 
     public static CachedRowSet ViewCalendarMonth(final int year,
                                                  final int month,
                                                  final String userName,
                                                  final boolean thisUser)
             throws java.lang.ClassNotFoundException, java.lang.IllegalAccessException, Exception {
 
         String sqlStatement;
         CachedRowSet RS;
 
         if (thisUser) {
             // NO SECURITY RESTRICTION
             sqlStatement = "SELECT us.id As id, " +
                     " eh.date As date, " +
                     " eh.id As entryid, eh.subject as subject " +
                     " FROM user As us, entry As eh " +
                     " WHERE us.username = '" + userName +
                     "' AND YEAR(eh.date)=" + year + " AND MONTH(eh.date)=" + month +
                     " AND us.id = eh.uid ORDER BY eh.date DESC, eh.id DESC;";
         } else {
             // PUBLIC ONLY
             sqlStatement = "SELECT us.id As id, " +
                     " eh.date As date, " +
                     " eh.id As entryid, eh.subject as subject " +
                     "FROM user As us, entry As eh WHERE us.username = '" + userName +
                     "' AND YEAR(eh.date)=" + year + " AND MONTH(eh.date)=" + month +
                     " AND us.id = eh.uid AND eh.security = '2' ORDER BY eh.date DESC, eh.id DESC;";
         }
 
         RS = SQLHelper.executeResultSet(sqlStatement);
 
 
         return RS;
     }
 
     public static CachedRowSet ViewCalendarDay(final int year,
                                                final int month,
                                                final int day,
                                                final String userName,
                                                final boolean thisUser) {
 
         String sqlStatement;
         CachedRowSet RS = null;
 
         if (thisUser) {
             // NO SECURITY RESTRICTION
             sqlStatement = "SELECT us.id As id, us.name As name, " +
                     "eh.date As date, eh.subject As subject, eh.music, eh.body, " +
                     "mood.title As mood, location.title As location, mood.id as moodid, location.id as locid, eh.id As entryid, eh.security " +
                     "FROM user As us, entry As eh, " +
                     "mood, location " +
                     "WHERE us.userName='" + userName + "' AND YEAR(eh.date)=" + year + " AND MONTH(eh.date)=" + month +
                     " AND DAYOFMONTH(eh.date)=" + day +
                     " AND us.id=eh.uid AND mood.id=eh.mood AND location.id=eh.location ORDER BY eh.date DESC, eh.id DESC;";
         } else {
             // PUBLIC ONLY
             sqlStatement = "SELECT us.id As id, us.name As name, " +
                     "eh.date As date, eh.subject As subject, eh.music, eh.body, " +
                     "mood.title As mood, location.title As location, mood.id as moodid, location.id as locid, eh.id As entryid, eh.security " +
                     "FROM user As us, entry As eh, " +
                     "mood, location " +
                     "WHERE us.userName='" + userName + "' AND YEAR(eh.date)=" + year + " AND MONTH(eh.date)=" + month +
                     " AND DAYOFMONTH(eh.date)=" + day +
                     " AND us.id=eh.uid AND mood.id=eh.mood AND location.id=eh.location AND eh.security=2 ORDER BY eh.date DESC, eh.id DESC;";
         }
 
         try {
             RS = SQLHelper.executeResultSet(sqlStatement);
         } catch (Exception e1) {
 
         }
 
         return RS;
     }
 
     /**
      * Retrieves top 15 posts from all user's public journal entries
      * for submission on front page. (RSS feed, etc)
      *
      * @return Top 15 recent public entries
      */
     public static Collection<EntryTo> viewRecentAllUsers() {
 
         if (log.isDebugEnabled())
             log.debug("view: starting viewRecentAllUsers().");
 
         final int SIZE = 15;
         final ArrayList<EntryTo> entries = new ArrayList<EntryTo>(SIZE);
 
         String sqlStatement;
         CachedRowSet rs = null;
         EntryTo et;
 
         // PUBLIC ONLY
         sqlStatement = "SELECT us.id As id, us.userName, " +
                 "eh.date As date, eh.subject As subject, eh.music, eh.body, " +
                 "mood.title As moodt, location.title As location, eh.id As entryid, " +
                 "eh.security as security, eh.autoformat, eh.allow_comments, eh.email_comments, location.id as locationid, mood.id as moodid " +
                 "FROM user As us, entry As eh, " +
                 "mood, location " +
                 "WHERE " +
                 " us.id=eh.uid AND mood.id=eh.mood AND location.id=eh.location AND eh.security=2 ORDER BY eh.date DESC, eh.id DESC LIMIT "
                 + "0," + Integer.toString(SIZE) + ";";
 
         try {
             if (log.isDebugEnabled())
                 log.debug("viewRecentAllUsers: execute sql statement");
 
             rs = SQLHelper.executeResultSet(sqlStatement);
 
             while (rs.next()) {
                 if (log.isDebugEnabled())
                     log.debug("viewRecentAllUsers: create EntryTo object and populate it.");
 
                 et = new EntryTo();
 
                 et.setUserName(rs.getString("userName"));
                 et.setId(rs.getInt("entryid"));
                 et.setUserId(rs.getInt("id"));
                 et.setDate(rs.getString("date"));
                 et.setSubject(rs.getString("subject"));
                 et.setBody(rs.getString("body"));
                 et.setLocationId(rs.getInt("locationid"));
                 et.setMoodId(rs.getInt("moodid"));
                 et.setMusic(rs.getString("music"));
                 et.setSecurityLevel(rs.getInt("security"));
                 et.setMoodName(rs.getString("moodt"));
                 et.setLocationName(rs.getString("location"));
 
                 if (rs.getString("email_comments").compareTo("Y") == 0)
                     et.setEmailComments(true);
                 else
                     et.setEmailComments(false);
 
                 if (rs.getString("allow_comments").compareTo("Y") == 0)
                     et.setAllowComments(true);
                 else
                     et.setAllowComments(false);
 
                 if (rs.getString("autoformat").compareTo("Y") == 0)
                     et.setAutoFormat(true);
                 else
                     et.setAutoFormat(false);
 
                 if (log.isDebugEnabled())
                     log.debug("viewRecentAllUsers: ET contains " + et.toString());
 
                 entries.add(et);
             }
 
             rs.close();
 
         } catch (Exception e1) {
             if (log.isDebugEnabled())
                 log.debug("viewRecentAllUsers: exception is: " + e1.getMessage() + "\n" + e1.toString());
 
             if (rs != null) {
                 try {
                     rs.close();
                 } catch (Exception e) {
                     // NOTHING TO DO
                 }
             }
         }
 
         return entries;
     }
 
     /**
      * Retrieve the 15 most recent journal entries from unique users.  So
      * if I post 10 entries, it will only select the most recent entry
      * and the entries of 14 other users.  (another words 1 per user)
      *
      * @return Entries from 15 different users (most recent)
      */
     public static Collection<EntryTo> viewRecentUniqueUsers() {
 
         if (log.isDebugEnabled())
             log.debug("view: starting viewRecentUniqueUsers().");
 
         final int SIZE = 15;
         final ArrayList<EntryTo> entries = new ArrayList<EntryTo>(SIZE);
 
         String sqlStatement;
         CachedRowSet rs = null;
         EntryTo et;
 
         // PUBLIC ONLY
         sqlStatement = "call entry_view_latest_rss();";
 
         try {
             if (log.isDebugEnabled())
                 log.debug("viewRecentUniqueUsers: execute sql statement");
 
             rs = SQLHelper.executeResultSet(sqlStatement);
 
             while (rs.next()) {
                 if (log.isDebugEnabled())
                     log.debug("viewRecentUniqueUsers: create EntryTo object and populate it.");
 
                 et = new EntryTo();
 
                 et.setUserName(rs.getString("userName"));
                 et.setId(rs.getInt("entryid"));
                 et.setUserId(rs.getInt("id"));
                 et.setDate(rs.getString("date"));
                 et.setSubject(rs.getString("subject"));
                 et.setBody(rs.getString("body"));
                 et.setLocationId(rs.getInt("locationid"));
                 et.setMoodId(rs.getInt("moodid"));
                 et.setMusic(rs.getString("music"));
                 et.setSecurityLevel(rs.getInt("security"));
                 et.setMoodName(rs.getString("moodt"));
                 et.setLocationName(rs.getString("location"));
 
                 if (rs.getString("email_comments").compareTo("Y") == 0)
                     et.setEmailComments(true);
                 else
                     et.setEmailComments(false);
 
                 if (rs.getString("allow_comments").compareTo("Y") == 0)
                     et.setAllowComments(true);
                 else
                     et.setAllowComments(false);
 
                 if (rs.getString("autoformat").compareTo("Y") == 0)
                     et.setAutoFormat(true);
                 else
                     et.setAutoFormat(false);
 
                 if (log.isDebugEnabled())
                     log.debug("viewRecentUniqueUsers: ET contains " + et.toString());
 
                 entries.add(et);
             }
 
             rs.close();
 
         } catch (Exception e1) {
             if (log.isDebugEnabled())
                 log.debug("viewRecentUniqueUsers: exception is: " + e1.getMessage() + "\n" + e1.toString());
 
             if (rs != null) {
                 try {
                     rs.close();
                 } catch (Exception e) {
                     // NOTHING TO DO
                 }
             }
         }
 
         return entries;
     }
 }
