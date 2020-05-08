 /*
 Copyright (c) 2003-2006, Lucas Holt
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
 
 import com.justjournal.utility.StringUtil;
 
 import javax.sql.rowset.CachedRowSet;
 import java.util.ArrayList;
 
 /**
  * Manipulate and fetch comments
  *
  * @author Lucas Holt
 * @version $Id: CommentDao.java,v 1.9 2008/08/31 20:55:24 laffer1 Exp $
  *          User: laffer1
  *          Date: Dec 25, 2003
  *          Time: 2:19:28 PM
  */
 public final class CommentDao {
 
     /**
      * Add a comment on a journal entry
      *
      * @param comment A user comment to change
      * @return true if no error occured.
      */
     public boolean add(final CommentTo comment) {
 
         final String sqlStmt =
                 "Insert INTO comments (id,uid,eid,date,subject,body) values(NULL,'"
                         + comment.getUserId() + "','"
                         + comment.getEid() + "','"
                         + comment.getDate() + "','"
                         + StringUtil.replace(comment.getSubject(), '\'', "\\\'") + "','"
                         + StringUtil.replace(comment.getBody(), '\'', "\\\'") + "');";
 
         return BaseDao.add(sqlStmt);
     }
 
     /**
      * change a comment on a journal entry.
      *
      * @param comment A user comment to change
      * @return true if no error occured.
      */
     public boolean update(final CommentTo comment) {
 
         final String sqlStmt = "Update comments SET subject='" +
                 StringUtil.replace(comment.getSubject(), '\'', "\\\'")
                 + "', body='" +
                 StringUtil.replace(comment.getBody(), '\'', "\\\'")
                 + "' WHERE id='" +
                 comment.getId() + "' AND uid='" +
                 comment.getUserId() + "' AND eid='" +
                 comment.getEid() + "' LIMIT 1;";
 
         return BaseDao.edit(sqlStmt);
     }
 
     public static boolean delete(final int commentId, final int userId) {
         boolean noError = true;
         final String sqlStmt = "DELETE FROM comments WHERE id='" + commentId + "' AND uid='" + userId + "' LIMIT 1;";
 
         if (commentId > 0 && userId > 0) {
             try {
                 SQLHelper.executeNonQuery(sqlStmt);
             } catch (Exception e) {
                 noError = false;
             }
         } else {
             noError = false;
         }
 
         return noError;
     }
 
     /**
      * Deletes comments belonging to a specific entry id.
      * used by the delete entry logic.  Not recommended for
      * direct calls by Users.
      *
     * @param entryId
      * @return true on success, false on any error.
      */
     public static boolean deleteByEntry(final int entryId) {
         boolean noError = true;
         final String sqlStmt = "DELETE FROM comments WHERE eid='" + entryId + "' LIMIT 1;";
 
         if (entryId > 0) {
             try {
                 SQLHelper.executeNonQuery(sqlStmt);
             } catch (Exception e) {
                 noError = false;
             }
         } else {
             noError = false;
         }
 
         return noError;
     }
 
     public static CommentTo viewSingle(final int commentId) {
         CachedRowSet rs = null;
         final CommentTo comment = new CommentTo();
         final String sqlStmt =
                 "Select user.username, comments.date,comments.subject,comments.body, comments.uid, comments.id As cid, comments.eid FROM comments,user WHERE comments.id='"
                         + commentId + "' AND comments.uid = user.id;";
 
         try {
             rs = SQLHelper.executeResultSet(sqlStmt);
 
             if (rs.next()) {
                 // set the properites on the bean
                 comment.setId(rs.getInt("cid"));
                 comment.setUserName(rs.getString("username"));
                 comment.setDate(rs.getString("date"));
                 comment.setSubject(rs.getString("subject"));
                 comment.setBody(rs.getString("body"));
                 comment.setEid(rs.getInt("eid"));
                 comment.setUserId(rs.getInt("uid"));
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
 
         return comment;
     }
 
     public static ArrayList<CommentTo> view(final int entryId) {
         final ArrayList<CommentTo> comments = new ArrayList<CommentTo>(5);  // 5 is average comments on entry?
         CachedRowSet rs = null;
         CommentTo comment;
         final String sqlStmt =
                 "Select user.username, comments.date,comments.subject,comments.body, comments.uid, comments.id As cid FROM comments,user WHERE comments.eid='"
                         + entryId + "' AND comments.uid = user.id;";
 
         try {
             rs = SQLHelper.executeResultSet(sqlStmt);
 
             while (rs.next()) {
                 // create a new comment to put in the array list
                 comment = new CommentTo();
 
                 // set the properites on the bean
                 comment.setId(rs.getInt("cid"));
                 comment.setUserName(rs.getString("username"));
                 comment.setDate(rs.getString("date"));
                 comment.setSubject(rs.getString("subject"));
                 comment.setBody(rs.getString("body"));
                 comment.setEid(entryId);
                 comment.setUserId(rs.getInt("uid"));
 
                 // add to the array list
                 comments.add(comment);
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
 
         return comments;
 
     }
 
      /**
      * Count the number of blog comments this user has made.
     * @param userName
     * @return
     * @throws Exception
      */
    public static int commentCount( final String userName ) throws Exception {
         String sqlStatement;
         CachedRowSet RS;
         int count = 0;
 
         sqlStatement = "SELECT count(*) " +
                 " FROM user As us, comments As com " +
                 " WHERE us.username = '" + userName +
                " AND us.id = com.uid;";
 
         RS = SQLHelper.executeResultSet(sqlStatement);
 
         if (RS.next())
             count = RS.getInt(1);
         RS.close();
 
         return count;
     }
 
 }
