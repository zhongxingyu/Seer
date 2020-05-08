 package ru.anglerhood.lj.client.sql;
 
 import org.apache.commons.dbutils.QueryRunner;
 import org.apache.commons.dbutils.ResultSetHandler;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import ru.anglerhood.lj.api.xmlrpc.results.BlogEntry;
 import ru.anglerhood.lj.api.xmlrpc.results.Comment;
 import ru.anglerhood.lj.client.BlogEntryReader;
 
 import java.sql.*;
 import java.util.*;
 
 /*
 * Copyright (c) 2012, Anatoly Rybalchenko
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided 
 * that the following conditions are met:
 *
 *     * Redistributions of source code must retain the above copyright notice, this list of conditions 
 *       and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright notice, this list of conditions 
 *       and the following disclaimer in the documentation and/or other materials provided with the distribution.
 *     * The name of the author may not be used may not be used to endorse or 
 *       promote products derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" 
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, 
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR 
 * PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS 
 * BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, 
 * OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT 
 * OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; 
 * OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, 
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) 
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF 
 * THE POSSIBILITY OF SUCH DAMAGE.
 */
 public class SQLiteReader implements BlogEntryReader {
 
     private static Log logger = LogFactory.getLog(SQLiteReader.class);
     private Connection connection;
     private String journal;
     private QueryRunner runner = new QueryRunner();
     private List<Integer> ids;
     private Iterator<Integer> it;
 
     private static final String SELECT_ALL_ENTRY_IDS = String.format("SELECT %s from %s;", BlogEntry.ITEMID, SQLiteWriter.ENTRY);
     private static final String SELECT_ENTRY = "SELECT * from " + SQLiteWriter.ENTRY +
                                             " where " + BlogEntry.ITEMID + " = ?;";
 
     private static final String SELECT_COMMENT = "SELECT * from "  + SQLiteWriter.COMMENT +
                                                  " where " + Comment.ENTRYID + " = ?" +
                                                  " order by level, datepostunix asc;";
 
     public SQLiteReader(String journal) {
         try {
             Class.forName("org.sqlite.JDBC");
             connection = DriverManager.getConnection("jdbc:sqlite:" + journal + ".db");
 
             ResultSetHandler<List<Integer>> handler = new ResultSetHandler() {
                 @Override
                 public Object handle(ResultSet resultSet) throws SQLException {
                     List<Integer> result = new LinkedList<Integer>();
                     while(resultSet.next()) {
                         result.add(resultSet.getInt(BlogEntry.ITEMID));
                     }
                     return result;
                 }
             };
             ids = runner.query(connection, SELECT_ALL_ENTRY_IDS, handler);
             it = ids.iterator();
 
 
         } catch (ClassNotFoundException e) {
             logger.error("Could find JDBC driver for SQLite! " + e.getMessage());
         } catch (SQLException e) {
             logger.error("Invalid SQL: " + e.getMessage());
         }
         this.journal = journal;
     }
     @Override
     public BlogEntry readEntry(int entryId) {
         BlogEntry entry = null;
         try {
 
             ResultSetHandler<List<BlogEntry>>  handler = new BlogEntryHandler();
             List<BlogEntry> result = runner.query(connection, SELECT_ENTRY, handler , entryId);
             entry = result.get(0);
         } catch (SQLException e) {
             logger.error(String.format("SQL Error: %s", e.getMessage()));
         }
         return entry;
     }
 
     @Override
     public List<Comment> readComments(int entryId) {
         List<Comment> comments = new LinkedList<Comment>();
         ResultSetHandler<List<Comment>> handler = new CommentHandler();
         try {
             comments = runner.query(connection, SELECT_COMMENT, handler, entryId);
         } catch (SQLException e) {
             logger.error(String.format("SQL Error: %s, ", e.getMessage()));
         }
         return nestComments(comments);
     }
 
     private List<Comment> nestComments(List<Comment> comments) {
         List<Comment> result = new LinkedList<Comment>();
         SortedMap<Integer, SortedSet<Comment>> levels = new TreeMap<Integer, SortedSet<Comment>>();
         //distribute comments among levels
         int maxLevel = -1;
         for(Comment comment : comments) {
             if(comment.getLevel() > maxLevel) {
                 maxLevel = comment.getLevel();
 
                 Comparator<Comment> comparator = new Comparator<Comment>() {
                     @Override
                     public int compare(Comment o1, Comment o2) {
                         if (o1.getLevel().equals(o2.getLevel())) {
                             return o1.getDatePostUnix() - o2.getDatePostUnix();
                         } else {
                             return o1.getLevel() - o2.getLevel();
                         }
 
                     }
                 };
 
                 levels.put(maxLevel, new TreeSet<Comment>(comparator));
             }
 
             levels.get(comment.getLevel()).add(comment);
         }
 
         for(int i = levels.lastKey(); i > levels.firstKey(); i--){
             SortedSet<Comment> children = levels.get(i);
             SortedSet<Comment> parents = levels.get(i - 1);
             for(Comment child : children) {
                 for(Comment parent : parents) {
                     if(child.getParentDtalkId().equals(parent.getDtalkid())){
                         parent.addChild(child);
                     }
                 }
             }
         }
         result.addAll(levels.get(levels.firstKey()));
         return result;
     }
 
 
 
     @Override
     public boolean hasNext(){
         return it.hasNext();
     }
 
     @Override
     public BlogEntry next(){
         Integer id = it.next();
         return readEntry(id);
     }
 
 }
