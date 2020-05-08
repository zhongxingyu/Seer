 /*
 Copyright (c) 2006, Lucas Holt
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
 
 package com.justjournal.core;
 
 import com.justjournal.db.SQLHelper;
 import sun.jdbc.rowset.CachedRowSet;
 
 /**
  * Track the number of users, entry and comment statistics, and
  * other information.
  *
  * @author Lucas Holt
 * @version $id$
  */
 public class Statistics {
 
     /**
      * Determine the number of users registered.
      *
      * @return The number of users or -1 on an error.
      */
     public int users() {
         int count = -1;
         String sql = "SELECT count(*) FROM user;";
 
         try {
             CachedRowSet rs = SQLHelper.executeResultSet(sql);
 
             if (rs.next()) {
                 count = rs.getInt(1);
             }
 
             rs.close();
         } catch (Exception e) {
 
         }
 
         return count;
     }
 
     /**
      * Determine the number of journal entries posted.
      *
      * @return The number of entries or -1 on error.
      */
     public int entries() {
         int count = -1;
         String sql = "SELECT count(*) FROM entry;";
 
         try {
             CachedRowSet rs = SQLHelper.executeResultSet(sql);
 
             if (rs.next()) {
                 count = rs.getInt(1);
             }
 
             rs.close();
         } catch (Exception e) {
 
         }
 
         return count;
     }
 
     /**
      * Determine the number of comments posted.
      *
      * @return The number of comments or -1 on error.
      */
     public int comments() {
         int count = -1;
         String sql = "SELECT count(*) FROM comments;";
 
         try {
             CachedRowSet rs = SQLHelper.executeResultSet(sql);
 
             if (rs.next()) {
                 count = rs.getInt(1);
             }
 
             rs.close();
         } catch (Exception e) {
 
         }
 
         return count;
     }
 
     /**
      * Determine the number of journal styles posted.
      *
      * @return The number of styles or -1 on error.
      */
     public int styles() {
         int count = -1;
         String sql = "SELECT count(*) FROM style;";
 
         try {
             CachedRowSet rs = SQLHelper.executeResultSet(sql);
 
             if (rs.next()) {
                 count = rs.getInt(1);
             }
 
             rs.close();
         } catch (Exception e) {
 
         }
 
         return count;
     }
 
 }
