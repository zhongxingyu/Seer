 /*
  * Copyright 2012-2013 James Geboski <jgeboski@gmail.com>
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package org.jgeboski.vindicator.storage;
 
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.ArrayList;
 
 import org.jgeboski.vindicator.exception.StorageException;
 import org.jgeboski.vindicator.storage.sql.Database;
 import org.jgeboski.vindicator.storage.sql.SQLStatement;
import org.jgeboski.vindicator.storage.sql.SQLType;
 
 public class StorageSQL implements Storage
 {
     private String TABLE_TARGETS = "targets";
 
     private String url;
     private String username;
     private String password;
     private String prefix;
 
     private Database database;
 
     public StorageSQL(String url, String username, String password,
                       String prefix)
         throws StorageException
     {
         SQLStatement stmt;
        String       ainc;
 
         this.url      = url;
         this.username = username;
         this.password = password;
         this.prefix   = prefix;
 
         if (prefix != null)
             TABLE_TARGETS = prefix + TABLE_TARGETS;
 
         try {
             database = new Database(url, username, password, prefix);
         } catch (SQLException e) {
             throw new StorageException(e);
         }
 
         if (database.hasTable(TABLE_TARGETS))
             return;
 
         stmt = database.createStatement();
        ainc = (database.getType() == SQLType.MYSQL) ? "AUTO_INCREMENT" : "";
 
         stmt.store(
             "CREATE TABLE", TABLE_TARGETS, "(",
                "id INTEGER PRIMARY KEY ", ainc, ",",
                 "target varchar(16) NOT NULL,",
                 "issuer varchar(16) NOT NULL,",
                 "message varchar(255) NOT NULL,",
                "timeout INTEGER(11) NOT NULL,",
                "time INTEGER(11) NOT NULL,",
                "flags SMALLINT(6) NOT NULL",
             ")");
 
         try {
             stmt.executeUpdate();
             stmt.close();
         } catch (SQLException e) {
             throw new StorageException(e);
         }
     }
 
     public StorageSQL(String url)
         throws StorageException
     {
         this(url, null, null, null);
     }
 
     public void close()
     {
         if (database != null)
             database.close();
     }
 
     public void add(TargetObject to)
         throws StorageException
     {
         SQLStatement stmt;
 
         stmt = database.createStatement();
 
         stmt.store(
             "INSERT INTO", TABLE_TARGETS,
                 "(target, issuer, message, timeout, time, flags)",
                 "VALUES (?, ?, ?, ?, ?, ?)", null,
             to.target, to.issuer, to.message, to.timeout, to.timeout, to.flags);
 
         try {
             stmt.execute();
             stmt.close();
         } catch (SQLException e) {
             throw new StorageException(e);
         }
     }
 
     public void remove(int id)
         throws StorageException
     {
         SQLStatement stmt;
 
         if (id < 1)
             return;
 
         stmt = database.createStatement();
 
         stmt.store(
             "DELETE FROM", TABLE_TARGETS,
                 "WHERE id = ?", null,
             id);
 
         try {
             stmt.executeUpdate();
             stmt.close();
         } catch (SQLException e) {
             throw new StorageException(e);
         }
     }
 
     public void remove(TargetObject to)
         throws StorageException
     {
         remove(to.id);
     }
 
     public TargetObject[] getTargets(String target)
         throws StorageException
     {
         ArrayList<TargetObject> ret;
 
         SQLStatement stmt;
         ResultSet    rs;
         TargetObject to;
 
         if (target == null)
             return new TargetObject[0];
 
         stmt = database.createStatement();
         ret  = new ArrayList<TargetObject>();
 
         stmt.store(
             "SELECT * FROM", TABLE_TARGETS,
                 "WHERE target = ?", null,
             target);
 
         try {
             rs = stmt.executeQuery();
 
             if (rs == null)
                 return new TargetObject[0];
 
             while (rs.next()) {
                 to = new TargetObject();
 
                 to.id      = rs.getInt("id");
                 to.target  = rs.getString("target");
                 to.issuer  = rs.getString("issuer");
                 to.message = rs.getString("message");
                 to.timeout = rs.getLong("timeout");
                 to.time    = rs.getLong("time");
                 to.flags   = rs.getInt("flags");
 
                 ret.add(to);
             }
 
             stmt.close();
         } catch (SQLException e) {
             throw new StorageException(e);
         }
 
         return ret.toArray(new TargetObject[0]);
     }
 }
