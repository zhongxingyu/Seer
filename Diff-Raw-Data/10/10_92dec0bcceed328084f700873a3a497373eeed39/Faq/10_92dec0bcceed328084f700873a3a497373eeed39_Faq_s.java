 /* Copyright (c) Kenneth Prugh 2011
 
    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.
 
    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.
 
    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
 
 package modules;
 
 import database.Postgres;
 import irc.Message;
 
 import java.sql.Connection;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 
 public class Faq {
     private final static Connection db = Postgres.getInstance().getConnection();
     private static String selectStatement;
     private static PreparedStatement prepStmt;
     private static ResultSet rs;
 
     public static String parseInput(Message m) {
        String input = m.msg.split(" ", 3)[1].trim();
 
         if (input.equals("add"))
         {
             return addEntry(m);
         }
         else if (input.equals("remove"))
         {
             return removeEntry(m);
         }
         else
         {
             if (input.length() >= 2)
             {
                 return getEntry(m);
             }
             else
             {
                 return "Usage: !faq topic, !faq remove topic, !faq add topic text";
             }
         }
     }
 
     /* Add entry to database for the specified topic
      *
      * !faq add topic entry
      */
     private static String addEntry(Message m) {
         String[] input = m.msg.split(" ", 4);
 
         rs = null;
         prepStmt = null;
 
         try { 
             selectStatement = "UPDATE faq SET entry = ?,usr = ? WHERE topic = ? ";
             prepStmt = db.prepareStatement(selectStatement);
             prepStmt.setString(1, input[3]);
             prepStmt.setString(2, m.ident+m.host);
             prepStmt.setString(3, input[2]);
             int changed = prepStmt.executeUpdate();
             if (changed >= 1)
             {
                 return "Successfully updated faq for " + input[2];
             }
             else
             {
                 selectStatement = "INSERT INTO faq (topic, entry, usr) VALUES( ?, ?, ? )";
                 prepStmt = db.prepareStatement(selectStatement);
                 prepStmt.setString(1, input[2]);
                 prepStmt.setString(2, input[3]);
                 prepStmt.setString(3, m.ident+m.host);
                 prepStmt.execute();
                 return "Successfully added faq for " + input[2];
             }
         } catch (SQLException e) {
             return "Database accident";
         } finally {
             if (prepStmt != null) {
                 try { 
                     prepStmt.close();
                 } catch (SQLException e) {}
             }
             if (rs != null) {
                 try { 
                     rs.close();
                 } catch (SQLException e) {}
             }
         }
     }
 
     /* Remove entry to database for the specified topic 
      *
      * !faq remove topic
      */
     private static String removeEntry(Message m) {
         String[] input = m.msg.split(" ", 3);
 
         rs = null;
         prepStmt = null;
 
         try { 
             selectStatement = "DELETE FROM faq WHERE topic = ? ";
             prepStmt = db.prepareStatement(selectStatement);
             prepStmt.setString(1, input[2]);
             int changed = prepStmt.executeUpdate();
             if (changed >= 1)
             {
                 return "Successfully deleted faq for " + input[2];
             }
             else
             {
                 return "No faq for " + input[2];
             }
         } catch (SQLException e) {
             return "Database accident";
         } finally {
             if (prepStmt != null) {
                 try { 
                     prepStmt.close();
                 } catch (SQLException e) {}
             }
             if (rs != null) {
                 try { 
                     rs.close();
                 } catch (SQLException e) {}
             }
         }
     }
 
     /* Retrieve an entry for the specified topic
      *
      * !faq topic
      */
     private static String getEntry(Message m) {
         String[] input = m.msg.split(" ", 2);
 
         rs = null;
         prepStmt = null;
 
         try { 
             selectStatement = "SELECT entry FROM faq WHERE topic = ? ";
             prepStmt = db.prepareStatement(selectStatement);
             prepStmt.setString(1, input[1]);
             rs = prepStmt.executeQuery();
             if (rs.next())
             {
                 return input[1] + ": " + rs.getString(1);
             }
             else
             {
                 return "No faq for " + input[1];
             }
         } catch (SQLException e) {
             return "Database accident";
         } finally {
             if (prepStmt != null) {
                 try { 
                     prepStmt.close();
                 } catch (SQLException e) {}
             }
             if (rs != null) {
                 try { 
                     rs.close();
                 } catch (SQLException e) {}
             }
         }
     }
 }
 
