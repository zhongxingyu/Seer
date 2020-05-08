 /*
  * $Source$
  * $Revision$
  *
  * Copyright (C) 2000 William Chesters
  *
  * Part of Melati (http://melati.org), a framework for the rapid
  * development of clean, maintainable web applications.
  *
  * Melati is free software; Permission is granted to copy, distribute
  * and/or modify this software under the terms either:
  *
  * a) the GNU General Public License as published by the Free Software
  *    Foundation; either version 2 of the License, or (at your option)
  *    any later version,
  *
  *    or
  *
  * b) any version of the Melati Software License, as published
  *    at http://melati.org
  *
  * You should have received a copy of the GNU General Public License and
  * the Melati Software License along with this program;
  * if not, write to the Free Software Foundation, Inc.,
  * 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA to obtain the
  * GNU General Public License and visit http://melati.org to obtain the
  * Melati Software License.
  *
  * Feel free to contact the Developers of Melati (http://melati.org),
  * if you would like to work out a different arrangement than the options
  * outlined here.  It is our intention to allow Melati to be used by as
  * wide an audience as possible.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * Contact details for copyright holder:
  *
  *     William Chesters <williamc@paneris.org>
  *     http://paneris.org/~williamc
  *     Obrechtstraat 114, 2517VX Den Haag, The Netherlands
  */
 
 package org.melati.poem;
 
 import org.melati.util.*;
 import java.sql.*;
 import java.util.*;
 
 public abstract class CachedQuery {
 
   protected PreparedStatementFactory statements = null;
   protected Vector rows = null;
   private long tableSerial;
   protected Table table;
   private String query;
   private Table otherTables[];
   private long otherTablesSerial[];
 
   public CachedQuery(final Table table,
                      final String query,
                      final Table otherTables[]) {
     this.table = table;
     this.query = query;
     this.otherTables = otherTables;
     if (otherTables != null)
       otherTablesSerial = new long[otherTables.length];
   }
 
   public CachedQuery(final Table table, final String query) {
     this(table,query,null);
   }
 
   protected PreparedStatementFactory statements() {
     if (statements == null)
       statements = new PreparedStatementFactory(
                        table.getDatabase(),
                        query);
 
     return statements;
   }
 
   protected abstract Object extract(ResultSet rs) throws SQLException;
 
   protected void compute() {
     Vector rows = this.rows;
     SessionToken token = PoemThread.sessionToken();
     if (rows == null || somethingHasChanged(token.transaction)) {
       rows = new Vector();
       try {
         ResultSet rs = statements().resultSet(token);
         try {
           while (rs.next())
             rows.addElement(extract(rs));
         }
         finally {
           try { rs.close(); } catch (Exception e) {}
         }
       }
       catch (SQLException e) {
        throw new SQLSeriousPoemException(e);
       }
       this.rows = rows;
       updateSerials(token.transaction);
     }
   }
   
   private boolean somethingHasChanged(PoemTransaction transaction) {
     if (table.serial(transaction) != tableSerial)
       return true;
 
     if (otherTables != null) {
       for (int i = 0; i < otherTables.length; i++) {
         if (otherTables[i].serial(transaction) != otherTablesSerial[i])
           return true;
       }
     }
 
     return false;
   }
 
   private void updateSerials(PoemTransaction transaction) {
     tableSerial = table.serial(transaction);
     if (otherTables != null) {
       for (int i=0; i<otherTables.length; i++) {
         otherTablesSerial[i] = otherTables[i].serial(transaction);
       }
     }
   }
 
   public Table getTable() {
     return table;
   }
 
   public boolean outOfDate() {
     return somethingHasChanged(PoemThread.transaction());
   }
 }
