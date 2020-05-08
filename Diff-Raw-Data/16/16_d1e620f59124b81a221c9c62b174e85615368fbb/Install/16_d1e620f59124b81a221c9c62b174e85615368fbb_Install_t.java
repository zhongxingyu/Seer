 /*
  * The Campanile Project
  * Copyright (C) 2012 Stefano Fornari
  *
  * This program is free software; you can redistribute it and/or modify it under
  * the terms of the GNU Affero General Public License version 3 as published by
  * the Free Software Foundation with the addition of the following permission
  * added to Section 15 as permitted in Section 7(a): FOR ANY PART OF THE COVERED
  * WORK IN WHICH THE COPYRIGHT IS OWNED BY Stefano Fornari, Stefano Fornari
  * DISCLAIMS THE WARRANTY OF NON INFRINGEMENT OF THIRD PARTY RIGHTS.
  *
  * This program is distributed in the hope that it will be useful, but WITHOUT
  * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
  * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
  * details.
  *
  * You should have received a copy of the GNU Affero General Public License
  * along with this program; if not, see http://www.gnu.org/licenses or write to
  * the Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
  * MA 02110-1301 USA.
  */

package ste.campanile.star;

 import java.sql.Connection;
 import java.sql.SQLException;
 import java.sql.Statement;
 
 /**
  *
  * @author ste
  */
 public class Install {
 
     public static final String SQL_CREATE_TABLE =
             "create table item_properties (item_id int not null identity, star tinyint)";
     public static final String SQL_CHECK_FIELDS =
             "select star from item_properties";
     public static final String SQL_ADD_FIELDS =
             "alter table item_properties add column star tinyint";
 
     public static void installDB(Connection c) throws SQLException {
         Statement s = null;
 
         try {
             s = c.createStatement();
             s.executeUpdate(SQL_CREATE_TABLE);
             s.close(); s = null;
         } catch (SQLException e) {
             //
             // The table already exists; let's check the field
             //
             try {
                 s = c.createStatement();
                 s.executeQuery(SQL_CHECK_FIELDS);
                 s.close(); s = null;
             } catch (SQLException ee) {
                 //
                 // The field does not exist, let's add it
                 //
                 s = c.createStatement();
                 s.executeUpdate(SQL_ADD_FIELDS);
                 s.close(); s = null;
             }
         } finally {
             if (s != null) {
                 s.close();
             }
         }
     }
 }
