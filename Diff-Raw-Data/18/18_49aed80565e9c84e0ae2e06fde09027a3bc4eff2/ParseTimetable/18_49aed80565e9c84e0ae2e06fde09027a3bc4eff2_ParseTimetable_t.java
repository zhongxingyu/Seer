 import java.io.*;
 import java.sql.*;
 import java.util.*;
 import javax.swing.text.*;
 import javax.swing.text.html.*;
 import javax.swing.text.html.parser.*;
 
 public class ParseTimetable
 {
   static Connection conn;
 
   public static void main (String args[])
   {
     // import JDBC SQLite driver
     try
     {
       Class.forName("org.sqlite.JDBC");
     }
     catch (ClassNotFoundException e)
     {
       System.out.println("missing SQLite JDBC driver: run with -cp .:sqlitejdbc-vXXX.jar");
       System.out.println("latest jar file available at http://www.zentus.com/sqlitejdbc/");
       System.exit(0);
     }
 
     // open database
     try
     {
       conn = DriverManager.getConnection("jdbc:sqlite:ptv.db");
       Statement stat = conn.createStatement();
       stat.executeUpdate("create table if not exists stations (id integer primary key autoincrement, name string);");
      stat.executeUpdate("create table if not exists connections (source integer references stations (id), destination integer references stations (id), source_time integer, destination_time, monday boolean, tuesday boolean, wednesday boolean, thursday boolean, friday boolean, saturday boolean, sunday boolean, primary key (source, destination, source_time, destination_time, monday, tuesday, wednesday, thursday, friday, saturday, sunday) on conflict ignore);");
     }
     catch (SQLException e)
     {
       System.out.println("couldn't create/access database: ptv.db");
       System.out.println(e.getMessage());
       System.exit(0);
     }
 
     // check filename was passed
     if (args.length < 1)
     {
       System.out.println("syntax: java ParseTimetable <file>");
       System.exit(0);
     }
 
     Reader reader = null;
     try
     {
       // open file
       reader = new FileReader(args[0]);
     }
     catch (Exception e)
     {
       System.out.println("couldn't access file: " + args[0]);
       System.exit(0);
     }
 
     // parse file
     HTMLEditorKit.ParserCallback callback = new Callback();
     ParserDelegator delegator = new ParserDelegator();
     try
     {
       delegator.parse(reader, callback, true);
     }
     catch (Exception e)
     {
       System.out.println("error while parsing: " + e.getMessage());
       e.printStackTrace(System.out);
     }
 
     // done
     try
     {
       conn.close();
     }
     catch (SQLException e)
     {
       System.out.println("couldn't close database connection");
     }
   }
 
   static class Callback extends HTMLEditorKit.ParserCallback
   {
     // keeps track of open tags
     Stack<HTML.Tag> stack = new Stack<HTML.Tag>();
 
     int isProcessingStations = -1;
     ArrayList<Integer> stations = new ArrayList<Integer>(); // station id for each row of timetable
     int flindersStreetDivide = -1; // row of departing flinders street
     
     int isProcessingTimetableRow = -1;
     ArrayList<ArrayList<Integer>> columns = new ArrayList<ArrayList<Integer>>(); // store timetable since html does row by row
     int currentColumn = -1;
 
     public void handleStartTag (HTML.Tag tag, MutableAttributeSet a, int pos)
     {
       stack.push(tag);
 
       // started processing stations
       if (tag == HTML.Tag.DIV && a.containsAttribute(HTML.Attribute.ID, "ttMargin"))
       {
         isProcessingStations = stack.size();
       }
 
       // started processing a row
       if (tag == HTML.Tag.DIV && a.containsAttribute(HTML.Attribute.CLASS, "ttBodyTP"))
       {
         isProcessingTimetableRow = stack.size();
         currentColumn = 0;
       }
     }
 
     public void handleEndTag (HTML.Tag tag, int pos)
     {
       stack.pop();
 
       // finished processing stations
       if (stack.size() < isProcessingStations)
       {
         isProcessingStations = -1;
       }
 
       // finished processing timetable row
       if (stack.size() < isProcessingTimetableRow)
       {
         isProcessingTimetableRow = -1;
       }
 
       // done reading data, write it to the database
       if (tag == HTML.Tag.HTML)
       {
         PreparedStatement prep = null;
         // hooray for prepared statements
         try
         {
           prep = conn.prepareStatement("insert into connections values (?, ?, ?, ?, 1, 1, 1, 1, 1, 0, 0);");
         }
         catch (Exception e)
         {
           System.out.println(e.getMessage());
           return;
         }
 
         // process each column
         for (ArrayList<Integer> column : columns)
         {
           int prevStation = -1;
           int prevTime = -1;
 
           // process each station
           for (int row = 0; row < column.size(); row++)
           {
             // if the train stops here
             if (column.get(row) > -1)
             {
               // if there was a previous station and it's not also flinders street
               if (prevStation != -1 && row != flindersStreetDivide)
               {
                 try
                 {
                   prep.setInt(1, prevStation);
                   prep.setInt(2, stations.get(row));
                   prep.setInt(3, prevTime);
                   prep.setInt(4, column.get(row));
                   prep.addBatch();
                 }
                 catch (Exception e)
                 {
                   // oh noes, prep was closed?
                   System.out.println(e.getMessage());
                 }
               }
               
               prevStation = stations.get(row);
               prevTime = column.get(row);
             }
           }
         }
 
         // wait to commit all updates at once, WAY faster
         try
         {
           conn.setAutoCommit(false);
           prep.executeBatch();
           conn.commit();
           conn.setAutoCommit(true);
           prep.close();
         }
         catch (Exception e)
         {
           System.out.println(e.getMessage());
         }
       }
     }
 
     public void handleText (char data[], int pos)
     {
       HTML.Tag tag = stack.peek();
       String strData = new String(data);
 
       // found a station
       if (isProcessingStations != -1 && tag == HTML.Tag.A)
       {
         String station = strData.substring(0, strData.indexOf(" Station"));
 
         try
         {
           Statement stat = conn.createStatement();
 
           ResultSet rs = stat.executeQuery("select id from stations where name = '" + station + "';");
           int id = -1;
           while (rs.next())
           {
             id = rs.getInt("id");
           }
           rs.close();
 
           if (id == -1)
           {
             // new station
             stat.executeUpdate("insert into stations (name) values ('" + station + "')");
             rs = stat.executeQuery("select id from stations where name = '" + station + "';");
             while (rs.next())
             {
               id = rs.getInt("id");
             }
             rs.close();
           }
 
           stations.add(id);
         }
         catch (SQLException e)
         {
           // oh noes
           System.out.println(e.getMessage());
         }
       }
 
       // found flinders street divide
       if (isProcessingStations != -1 && tag == HTML.Tag.B && strData.indexOf("DEP") != -1)
       {
         flindersStreetDivide = stations.size() - 1;
       }
 
       // found timetable data
       if (isProcessingTimetableRow != -1 && (tag == HTML.Tag.SPAN || tag == HTML.Tag.B))
       {
         if (columns.size() <= currentColumn)
           columns.add(new ArrayList<Integer>());
 
         String[] parts = strData.split(":");
         int time = -1;
         if (parts.length > 1)
         {
           time = Integer.parseInt(parts[0]) * 60 + Integer.parseInt(parts[1]);
           if (tag == HTML.Tag.B)
             time += 12 * 60;
         }
 
         columns.get(currentColumn).add(time);
 
         currentColumn++;
       }
     }
   }
 }
