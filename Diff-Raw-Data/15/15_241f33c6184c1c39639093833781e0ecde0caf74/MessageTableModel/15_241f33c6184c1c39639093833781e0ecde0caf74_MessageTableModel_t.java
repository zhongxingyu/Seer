 /*
   MessageTableModel.java / Frost
   Copyright (C) 2001  Jan-Thomas Czornack <jantho@users.sourceforge.net>
 
   This program is free software; you can redistribute it and/or
   modify it under the terms of the GNU General Public License as
   published by the Free Software Foundation; either version 2 of
   the License, or (at your option) any later version.
 
   This program is distributed in the hope that it will be useful,
   but WITHOUT ANY WARRANTY; without even the implied warranty of
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
   General Public License for more details.
 
   You should have received a copy of the GNU General Public License
   along with this program; if not, write to the Free Software
   Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 */
 
 package frost.gui.model;
 
 
 public class MessageTableModel extends SortedTableModel
 {
     static java.util.ResourceBundle LangRes = java.util.ResourceBundle.getBundle("res.LangRes");
 
     protected final static String columnNames[] = {
         LangRes.getString("Index"),
         LangRes.getString("From"),
         LangRes.getString("Subject"),
         "Sig",
         LangRes.getString("Date")
     };
     protected final static Class columnClasses[] = {
         String.class, //LangRes.getString("Index"),
         String.class, //LangRes.getString("From"),
         String.class, //LangRes.getString("Subject"),
         String.class, //"Sig",
         String.class //LangRes.getString("Date")
     };
 
     public MessageTableModel()
     {
         super();
        
     }
    
     public boolean isCellEditable(int row, int col)
     {
         return false;
     }
 
     public String getColumnName(int column)
     {
         if( column >= 0 && column < columnNames.length )
             return columnNames[column];
         return null;
     }
     public int getColumnCount()
     {
         return columnNames.length;
     }
     public Class getColumnClass(int column)
     {
         if( column >= 0 && column < columnClasses.length )
             return columnClasses[column];
         return null;
     }
 }
