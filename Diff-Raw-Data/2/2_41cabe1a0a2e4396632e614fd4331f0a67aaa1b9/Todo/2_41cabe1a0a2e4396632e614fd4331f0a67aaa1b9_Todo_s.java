 /* Copyright (c) Kenneth Prugh 2012
 
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
 
 public class Todo {
     private final static Connection db = Postgres.getInstance().getConnection();
     private static String selectStatement;
     private static PreparedStatement prepStmt;
     private static ResultSet rs;
 
     public String parseTodo(final Message m)
     {
         // !todo [for target] <msg>
         String todoString = m.msg.substring(5).trim();
         if (todoString.length() == 0)
         {
             return getTodo(m);
         }
 
         String target = m.user;
         if (todoString.startsWith("for "))
         {
             try
             { 
                 target = todoString.split(" ", 3)[1].trim();
                 String todo = todoString.split(" ", 3)[2];
                 return newTodo(todo, target);
             }
             catch (ArrayIndexOutOfBoundsException e)
             {
                 return "Invalid input";
             }
         }
         
         return newTodo(todoString, target);
     }
 
     private String getTodo(final Message m)
     {
         int userid = -1;
         userid = getUserID(m.user);
         StringBuilder todo = new StringBuilder();
 
         String selectStatement = "SELECT todo_id,todo FROM todos WHERE user_id = ? ";
         try {
             prepStmt = db.prepareStatement(selectStatement);
             prepStmt.setInt(1, userid);
             rs = prepStmt.executeQuery();
 
             int todoid = -1;
             int c = 0;
             while (rs.next()) {
                 if (c > 0)
                 {
                     todo.append(" | ");
                 }
                 ++c;
                 todoid = rs.getInt(1);
                 todo.append("#");
                 todo.append(todoid);
                 todo.append(" ");
                 todo.append(rs.getString(2));
             }
         } catch (SQLException e) {
             e.printStackTrace();
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
         if (todo.length() == 0)
         {
             return "You have no todos";
         }
 
         return todo.toString();
     }
 
     private String newTodo(String todo, String target)
     {
         int userid = getUserID(target);
 
         selectStatement = "INSERT INTO todos (todo, user_id) VALUES(?, ?)";
         try {
             prepStmt = db.prepareStatement(selectStatement);
             prepStmt.setString(1, todo);
             prepStmt.setInt(2, userid);
             prepStmt.execute();
         } catch (SQLException e) {
             e.printStackTrace();
             return "Error adding todo";
         } finally {
             if (prepStmt != null) {
                 try { 
                     prepStmt.close();
                 } catch (SQLException e) {}
             }
         }
 
         return "todo added successfully";
     }
 
     public String deleteTodo(final Message m)
     {
         String todoString = m.msg.substring(5).trim();
         if (todoString.length() == 0)
         {
             return "No todo id specified";
         }
 
         int userid = getUserID(m.user);
         int todoid = -1;
         try
         { 
            todoid = Integer.parseInt(todoString.split(" ")[1].trim());
         }
         catch (ArrayIndexOutOfBoundsException e)
         {
             return "Invalid input";
         }
 
         int todo_user_id = -1;
         selectStatement = "SELECT user_id FROM todos where todo_id = ?";
         try { 
             prepStmt = db.prepareStatement(selectStatement);
             prepStmt.setInt(1, todoid);
             rs = prepStmt.executeQuery();
             if (rs.next())
             {
                 todo_user_id = rs.getInt(1);
             }
         } catch (SQLException e) {
             todo_user_id = -1;
         } finally {
             if (prepStmt != null) {
                 try { 
                     prepStmt.close();
                 } catch (SQLException e) {}
             }
         }
 
         if (userid != todo_user_id)
         {
             return "You do not own todo #" + todoid;
         }
 
         selectStatement = "DELETE FROM todos WHERE todo_id = ?";
         try {
             prepStmt = db.prepareStatement(selectStatement);
             prepStmt.setInt(1, todoid);
             prepStmt.execute();
         } catch (SQLException e) {
             e.printStackTrace();
             return "Error deleting todo";
         } finally {
             if (prepStmt != null) {
                 try { 
                     prepStmt.close();
                 } catch (SQLException e) {}
             }
         }
 
         return "todo #" + todoid + " deleted";
     }
 
     private static int getUserID(String user)
     {
         int id = -1;
         selectStatement = "SELECT user_id FROM susers where suser = ?";
         try { 
             prepStmt = db.prepareStatement(selectStatement);
             prepStmt.setString(1, user);
             rs = prepStmt.executeQuery();
             if (rs.next())
             {
                 id = rs.getInt(1);
             }
         } catch (SQLException e) {
             id = -1;
         } finally {
             if (prepStmt != null) {
                 try { 
                     prepStmt.close();
                 } catch (SQLException e) {}
             }
         }
 
         if (id == -1)
         {
             insertUser(user);
             return getUserID(user);
         }
 
         return id;
     }
 
     private static void insertUser(String user)
     {
         selectStatement = "INSERT INTO susers (suser) VALUES(?)";
         try { 
             prepStmt = db.prepareStatement(selectStatement);
             prepStmt.setString(1, user);
             prepStmt.execute();
         } catch (SQLException e) {
         } finally {
             if (prepStmt != null) {
                 try { 
                     prepStmt.close();
                 } catch (SQLException e) {}
             }
         }
     }
 }
 
