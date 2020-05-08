 /**
  * Copyright © 2012 Joseph Walton-Rivers <webpigeon@unitycoders.co.uk>
  *
  * This file is part of uc_PircBotX.
  *
  * uc_PircBotX is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * uc_PircBotX is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with uc_PircBotX.  If not, see <http://www.gnu.org/licenses/>.
  */
 package uk.co.unitycoders.pircbotx.data.db;
 
 import java.sql.*;
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  *
  */
 public class LineModel {
     private final Connection conn;
     private final PreparedStatement createLine;
     private final PreparedStatement readLines;
     private final PreparedStatement randomLine;
 
     private final int LINE_COLUMN = 1;
 
     public LineModel(Connection conn) throws Exception{
         this.conn = conn;
         buildTable();
         createLine = conn.prepareStatement("INSERT INTO lines VALUES(?)");
         readLines = conn.prepareStatement("SELECT * FROM lines");
        randomLine = conn.prepareStatement("SELECT * FROM lines ORDER BY RANDOM LIMIT(1)");
     }
 
     private void buildTable() throws SQLException{
         Statement stmt = conn.createStatement();
         stmt.executeUpdate("CREATE TABLE IF NOT EXISTS lines (name string)");
     }
 
     public void storeLine(String line) throws SQLException{
         createLine.clearParameters();
         createLine.setString(LINE_COLUMN, line);
         createLine.execute();
     }
 
     public String getRandomLine() throws SQLException{
         ResultSet rs = randomLine.executeQuery();
         return rs.getString(1);
     }
 
     public List<String> getAllLines(){
         List<String> lines = new ArrayList<String>();
         try{
             ResultSet rs = readLines.executeQuery();
             while(rs.next()){
                 lines.add(rs.getString(LINE_COLUMN));
             }
             rs.close();
         }catch(SQLException ex){
             ex.printStackTrace();
         }
         return lines;
     }
 
 }
