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
 
 public class ChannelFilter {
     private final static Connection db = Postgres.getInstance().getConnection();
     private static String selectStatement;
     private static PreparedStatement prepStmt;
     private static ResultSet rs;
 
     /* Should we check if a channel has any filtering at all first and return
      * immediately for channels with no filtering? Or just try to look up the
      * command immediately
      */
     public static boolean isCommandFiltered(final Message m)
     {
         rs = null;
         prepStmt = null;
 
         try
         { 
             selectStatement = "SELECT command FROM channelfilter WHERE channel = ? and command = ?";
             prepStmt = db.prepareStatement(selectStatement);
             prepStmt.setString(1, m.channel);
             prepStmt.setString(2, m.command);
             rs = prepStmt.executeQuery();
             if (rs.next())
             {
                 return true;
             }
             else
             {
                 return false;
             }
         } catch (SQLException e) {
             return false;
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
 
     public static String enableFilter(final Message m)
     {
         String command;
         try
         { 
              command = m.msg.split(" ")[1].trim();
         }
         catch (ArrayIndexOutOfBoundsException e)
         {
             return "Invalid input";
         }
 
         if (command.equals(""))
         {
             return "Invalid input";
         }
 
         selectStatement = "INSERT INTO channelfilter (channel, command) VALUES(?, ?) ";
         try
         {
             prepStmt = db.prepareStatement(selectStatement);
             prepStmt.setString(1, m.channel);
             prepStmt.setString(2, command);
             prepStmt.execute();
 
            return "command " + command + " enabled on channel " + m.channel;
         } catch (SQLException e) {
             return "Database error";
         } finally {
             if (prepStmt != null) {
                 try { 
                     prepStmt.close();
                 } catch (SQLException e) {}
             }
         }
     }
 
     public static String disableFilter(final Message m)
     {
         String command;
         try
         { 
              command = m.msg.split(" ")[1].trim();
         }
         catch (ArrayIndexOutOfBoundsException e)
         {
             return "Invalid input";
         }
 
         if (command.equals(""))
         {
             return "Invalid input";
         }
 
         selectStatement = "DELETE FROM channelfilter WHERE channel = ? and command = ?";
         try
         {
             prepStmt = db.prepareStatement(selectStatement);
             prepStmt.setString(1, m.channel);
             prepStmt.setString(2, command);
             int changed = prepStmt.executeUpdate();
 
             if (changed >= 1)
             {
                 return "command " + command + " enabled on channel " + m.channel;
             }
             else
             {
                 return "command " + command + " filter on channel " + m.channel + " not found";
             }
         } catch (SQLException e) {
             return "Database error";
         } finally {
             if (prepStmt != null) {
                 try { 
                     prepStmt.close();
                 } catch (SQLException e) {}
             }
         }
     }
 }
 
