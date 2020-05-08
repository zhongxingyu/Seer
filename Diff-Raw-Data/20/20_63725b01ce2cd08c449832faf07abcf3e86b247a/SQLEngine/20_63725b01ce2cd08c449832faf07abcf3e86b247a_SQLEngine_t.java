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
 
 package org.jgeboski.vindicator.storage.engine;
 
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.jgeboski.vindicator.storage.Storage;
 import org.jgeboski.vindicator.storage.StorageAddress;
 import org.jgeboski.vindicator.storage.StorageException;
 import org.jgeboski.vindicator.storage.StorageLogin;
 import org.jgeboski.vindicator.storage.StoragePlayer;
 import org.jgeboski.vindicator.storage.StorageRecord;
 import org.jgeboski.vindicator.util.sql.Database;
 import org.jgeboski.vindicator.util.sql.SQLStatement;
 import org.jgeboski.vindicator.util.sql.SQLType;
 import org.jgeboski.vindicator.util.StrUtils;
 
 public class SQLEngine extends Storage
 {
     private String TABLE_ADDRESSES = "addresses";
     private String TABLE_LOGINS    = "logins";
     private String TABLE_RECORDS   = "records";
 
     private String url;
     private String username;
     private String password;
     private String prefix;
 
     private Database database;
 
     public SQLEngine(String url, String username, String password,
                      String prefix)
         throws StorageException
     {
         this.url      = url;
         this.username = username;
         this.password = password;
         this.prefix   = prefix;
 
         if (prefix != null) {
             TABLE_ADDRESSES = prefix + TABLE_ADDRESSES;
             TABLE_LOGINS    = prefix + TABLE_LOGINS;
             TABLE_RECORDS   = prefix + TABLE_RECORDS;
         }
 
         try {
             database = new Database(url, username, password, prefix);
         } catch (SQLException e) {
             throw new StorageException(e);
         }
 
         initLogins();
         initRecords();
     }
 
     public SQLEngine(String url)
         throws StorageException
     {
         this(url, null, null, null);
     }
 
     public void close()
     {
         if (database != null)
             database.close();
     }
 
     public boolean convertible()
     {
         int size;
 
         if (database.hasTable(TABLE_ADDRESSES))
             return true;
 
         size = database.getColumnSize(TABLE_RECORDS, "issuer");
 
         if ((size != 36) && (database.getType() == SQLType.MYSQL))
             return true;
 
         return false;
     }
 
     public void add(StorageLogin login)
         throws StorageException
     {
         SQLStatement stmt;
 
         stmt = database.createStatement();
 
         if (!database.hasTable(TABLE_ADDRESSES)) {
             stmt.query("INSERT INTO", TABLE_LOGINS,
                          "(ident, alias, address, count, time)",
                       "VALUES (?, ?, ?, ?, ?)");
             stmt.param(login.player.ident, login.player.alias,
                        login.address.ident, login.count, login.time);
         } else {
             stmt.query("INSERT INTO", TABLE_ADDRESSES,
                          "(player, address, logins, time)",
                        "VALUES (?, ?, ?, ?)");
             stmt.param(login.player.ident, login.address.ident,
                        login.count, login.time);
         }
 
         try {
             stmt.executeUpdate();
             stmt.close();
         } catch (SQLException e) {
             stmt.close();
             throw new StorageException(e);
         }
     }
 
     public void add(StorageRecord recd)
         throws StorageException
     {
         SQLStatement stmt;
 
         stmt = database.createStatement();
         stmt.query("INSERT INTO", TABLE_RECORDS,
                      "(target, issuer, message, timeout, time, flags)",
                    "VALUES (?, ?, ?, ?, ?, ?)");
         stmt.param(recd.target.ident, recd.issuer.ident, recd.message,
                    recd.timeout, recd.time, recd.flags);
 
         try {
             stmt.executeUpdate();
             stmt.close();
         } catch (SQLException e) {
             stmt.close();
             throw new StorageException(e);
         }
     }
 
     public void addLogins(List<StorageLogin> logins)
         throws StorageException
     {
         SQLStatement stmt;
         List<String> queries;
         String       query;
 
         if (logins.size() < 1)
             return;
 
         queries = new ArrayList<String>();
         stmt    = database.createStatement();
 
         if (!database.hasTable(TABLE_ADDRESSES)) {
             query = "(?, ?, ?, ?, ?)";
 
             stmt.query("INSERT INTO", TABLE_LOGINS,
                          "(ident, alias, address, count, time)",
                        "VALUES");
 
             for (StorageLogin l : logins) {
                 queries.add(query);
                 stmt.param(l.player.ident, l.player.alias,
                            l.address.ident, l.count, l.time);
             }
         } else {
             query = "(?, ?, ?, ?)";
 
             stmt.query("INSERT INTO", TABLE_ADDRESSES,
                          "(player, address, logins, time)",
                        "VALUES");
 
             for (StorageLogin l : logins) {
                 queries.add(query);
                 stmt.param(l.player.ident, l.address.ident, l.count, l.time);
             }
         }
 
         query = StrUtils.join(queries, ", ");
         stmt.query(query);
 
         try {
             stmt.executeUpdate();
             stmt.close();
         } catch (SQLException e) {
             stmt.close();
             throw new StorageException(e);
         }
     }
 
     public void addRecords(List<StorageRecord> recds)
         throws StorageException
     {
         SQLStatement stmt;
         List<String> queries;
         String       query;
 
         if (recds.size() < 1)
             return;
 
         queries = new ArrayList<String>();
         query   = "(?, ?, ?, ?, ?, ?)";
         stmt    = database.createStatement();
 
         stmt.query("INSERT INTO", TABLE_RECORDS,
                      "(target, issuer, message, timeout, time, flags)",
                    "VALUES");
 
         for (StorageRecord r : recds) {
             queries.add(query);
             stmt.param(r.target.ident, r.issuer.ident, r.message,
                        r.timeout, r.time, r.flags);
         }
 
         query = StrUtils.join(queries, ", ");
         stmt.query(query);
 
         try {
             stmt.executeUpdate();
             stmt.close();
         } catch (SQLException e) {
             stmt.close();
             throw new StorageException(e);
         }
     }
 
     public void clearLogins()
         throws StorageException
     {
         SQLStatement stmt;
 
         if (database.hasTable(TABLE_ADDRESSES)) {
             stmt = database.createStatement();
             stmt.query("DROP TABLE", TABLE_ADDRESSES);
 
             try {
                 stmt.executeUpdate();
                 stmt.close();
             } catch (SQLException e) {
                 stmt.close();
                 throw new StorageException(e);
             }
         }
 
         if (database.hasTable(TABLE_LOGINS)) {
             stmt = database.createStatement();
             stmt.query("DROP TABLE", TABLE_LOGINS);
 
             try {
                 stmt.executeUpdate();
                 stmt.close();
             } catch (SQLException e) {
                 stmt.close();
                 throw new StorageException(e);
             }
         }
 
         initLogins();
     }
 
     public void clearRecords()
         throws StorageException
     {
         SQLStatement stmt;
 
         if (database.hasTable(TABLE_RECORDS)) {
             stmt = database.createStatement();
             stmt.query("DROP TABLE", TABLE_RECORDS);
 
             try {
                 stmt.executeUpdate();
                 stmt.close();
             } catch (SQLException e) {
                 stmt.close();
                 throw new StorageException(e);
             }
         }
 
         initRecords();
     }
 
     public StorageLogin getLogin(StorageLogin login)
         throws StorageException
     {
         List<StorageLogin> logins;
         SQLStatement stmt;
 
         stmt = database.createStatement();
 
         if (!database.hasTable(TABLE_ADDRESSES)) {
             stmt.query("SELECT * FROM", TABLE_LOGINS,
                          "WHERE ident = ? OR alias = ? AND address = ?",
                        "ORDER BY time DESC LIMIT 1");
             stmt.param(login.player.ident, login.player.alias,
                        login.address.ident);
         } else {
             stmt.query("SELECT * FROM", TABLE_ADDRESSES,
                          "WHERE player = ? AND address = ?",
                        "ORDER BY time DESC LIMIT 1");
             stmt.param(login.player.ident, login.address.ident);
         }
 
         logins = getLogins(stmt);
         return (logins.size() > 0) ? logins.get(0) : null;
     }
 
     public StorageLogin getLoginLast(StoragePlayer plyr)
         throws StorageException
     {
         List<StorageLogin> logins;
         SQLStatement stmt;
 
         stmt = database.createStatement();
 
         if (!database.hasTable(TABLE_ADDRESSES)) {
             stmt.query("SELECT * FROM", TABLE_LOGINS,
                          "WHERE ident = ? OR alias = ?",
                        "ORDER BY time DESC LIMIT 1");
             stmt.param(plyr.ident, plyr.alias);
         } else {
             stmt.query("SELECT * FROM", TABLE_ADDRESSES,
                          "WHERE player = ?",
                        "ORDER BY time DESC LIMIT 1");
             stmt.param(plyr.ident);
         }
 
         logins = getLogins(stmt);
         return (logins.size() > 0) ? logins.get(0) : null;
     }
 
     public List<StorageLogin> getLogins()
         throws StorageException
     {
         SQLStatement stmt;
 
         stmt = database.createStatement();
 
         if (!database.hasTable(TABLE_ADDRESSES))
             stmt.query("SELECT * FROM", TABLE_LOGINS);
         else
             stmt.query("SELECT * FROM", TABLE_ADDRESSES);
 
         return getLogins(stmt);
     }
 
     public List<StorageLogin> getLogins(StorageAddress addr)
         throws StorageException
     {
         SQLStatement stmt;
 
         stmt = database.createStatement();
 
         if (!database.hasTable(TABLE_ADDRESSES)) {
             stmt.query("SELECT * FROM", TABLE_LOGINS,
                          "WHERE address = ?");
         } else {
             stmt.query("SELECT * FROM", TABLE_ADDRESSES,
                          "WHERE address = ?");
         }
 
         stmt.param(addr.ident);
         return getLogins(stmt);
     }
 
     public List<StorageLogin> getLogins(StoragePlayer plyr)
         throws StorageException
     {
         SQLStatement stmt;
 
         stmt = database.createStatement();
 
         if (!database.hasTable(TABLE_ADDRESSES)) {
             stmt.query("SELECT * FROM", TABLE_LOGINS,
                          "WHERE ident = ? OR alias = ?");
             stmt.param(plyr.ident, plyr.alias);
         } else {
             stmt.query("SELECT * FROM", TABLE_ADDRESSES,
                          "WHERE player = ?");
             stmt.param(plyr.ident);
         }
 
         return getLogins(stmt);
     }
 
     public List<StorageRecord> getRecords()
         throws StorageException
     {
         SQLStatement stmt;
 
         stmt = database.createStatement();
         stmt.query("SELECT * FROM", TABLE_RECORDS);
 
         return getRecords(stmt);
     }
 
     public List<StorageRecord> getRecords(StorageAddress addr)
         throws StorageException
     {
         SQLStatement stmt;
 
         stmt = database.createStatement();
 
         stmt.query("SELECT * FROM", TABLE_RECORDS,
                      "WHERE target = ?");
         stmt.param(addr.ident);
 
         return getRecords(stmt);
     }
 
     public List<StorageRecord> getRecords(StoragePlayer plyr)
         throws StorageException
     {
         SQLStatement stmt;
 
         stmt = database.createStatement();
 
         stmt.query("SELECT * FROM", TABLE_RECORDS,
                      "WHERE target = ?");
         stmt.param(plyr.ident);
 
         return getRecords(stmt);
     }
 
     public void remove(StorageLogin login)
         throws StorageException
     {
         SQLStatement stmt;
 
         if (login.id < 1)
             return;
 
         stmt = database.createStatement();
 
         if (!database.hasTable(TABLE_ADDRESSES)) {
             stmt.query("DELETE FROM", TABLE_LOGINS,
                          "WHERE id = ?");
         } else {
             stmt.query("DELETE FROM", TABLE_ADDRESSES,
                          "WHERE id = ?");
         }
 
         stmt.param(login.id);
 
         try {
             stmt.executeUpdate();
             stmt.close();
         } catch (SQLException e) {
             stmt.close();
             throw new StorageException(e);
         }
     }
 
     public void remove(StorageRecord recd)
         throws StorageException
     {
         SQLStatement stmt;
 
         if (recd.id < 1)
             return;
 
         stmt = database.createStatement();
         stmt.query("DELETE FROM", TABLE_RECORDS,
                      "WHERE id = ?");
         stmt.param(recd.id);
 
         try {
             stmt.executeUpdate();
             stmt.close();
         } catch (SQLException e) {
             stmt.close();
             throw new StorageException(e);
         }
     }
 
     public void removeLogins(List<StorageLogin> logins)
         throws StorageException
     {
         SQLStatement stmt;
         List<String> queries;
         String       query;
 
         if (logins.size() < 1)
             return;
 
         queries = new ArrayList<String>();
         query   = "id = ?";
         stmt    = database.createStatement();
 
         if (!database.hasTable(TABLE_ADDRESSES)) {
             stmt.query("DELETE FROM", TABLE_LOGINS,
                          "WHERE");
         } else {
             stmt.query("DELETE FROM", TABLE_ADDRESSES,
                          "WHERE");
         }
 
         for (StorageLogin l : logins) {
             queries.add(query);
             stmt.param(l.id);
         }
 
         query = StrUtils.join(queries, " OR ");
         stmt.query(query);
 
         try {
             stmt.executeUpdate();
             stmt.close();
         } catch (SQLException e) {
             stmt.close();
             throw new StorageException(e);
         }
     }
 
     public void removeRecords(List<StorageRecord> recds)
         throws StorageException
     {
         SQLStatement stmt;
         List<String> queries;
         String       query;
 
         if (recds.size() < 1)
             return;
 
         queries = new ArrayList<String>();
         query   = "id = ?";
         stmt    = database.createStatement();
 
         stmt.query("DELETE FROM", TABLE_RECORDS,
                      "WHERE");
 
         for (StorageRecord r : recds) {
             queries.add(query);
             stmt.param(r.id);
         }
 
         query = StrUtils.join(queries, " OR ");
         stmt.query(query);
 
         try {
             stmt.executeUpdate();
             stmt.close();
         } catch (SQLException e) {
             stmt.close();
             throw new StorageException(e);
         }
     }
 
     public void update(StorageLogin login)
         throws StorageException
     {
         SQLStatement stmt;
 
         if (login.id < 1)
             return;
 
         stmt = database.createStatement();
 
         if (!database.hasTable(TABLE_ADDRESSES)) {
             stmt.query("UPDATE", TABLE_LOGINS, "SET",
                          "ident = ?,",
                          "alias = ?,",
                          "address = ?,",
                          "count = ?,",
                          "time = ?",
                        "WHERE id = ?");
             stmt.param(login.player.ident, login.player.alias,
                        login.address.ident, login.count, login.time,
                        login.id);
         } else {
             stmt.query("UPDATE", TABLE_ADDRESSES, "SET",
                          "player = ?,",
                          "address = ?,",
                          "logins = ?,",
                          "time = ?",
                        "WHERE id = ?");
             stmt.param(login.player.ident, login.address.ident,
                        login.count, login.time, login.id);
         }
 
         try {
             stmt.execute();
             stmt.close();
         } catch (SQLException e) {
             stmt.close();
             throw new StorageException(e);
         }
     }
 
     public void update(StorageRecord recd)
         throws StorageException
     {
         SQLStatement stmt;
 
         if (recd.id < 1)
             return;
 
         stmt = database.createStatement();
         stmt.query("UPDATE", TABLE_RECORDS, "SET",
                      "target = ?,",
                      "issuer = ?,",
                      "message = ?,",
                      "timeout = ?,",
                      "time = ?,",
                      "flags = ?",
                    "WHERE id = ?");
         stmt.param(recd.target.ident, recd.issuer.ident, recd.message,
                    recd.timeout, recd.time, recd.flags, recd.id);
 
         try {
             stmt.execute();
             stmt.close();
         } catch (SQLException e) {
             stmt.close();
             throw new StorageException(e);
         }
     }
 
     private void initLogins()
         throws StorageException
     {
         SQLStatement stmt;
         String       ainc;
 
         if (database.hasTable(TABLE_LOGINS) || database.hasTable(TABLE_ADDRESSES))
             return;
 
         stmt = database.createStatement();
         ainc = (database.getType() == SQLType.MYSQL) ? "AUTO_INCREMENT" : "";
 
         stmt.query("CREATE TABLE", TABLE_LOGINS, "(",
                      "id INTEGER PRIMARY KEY ", ainc, ",",
                      "ident VARCHAR(36) NOT NULL,",
                      "alias VARCHAR(16) NOT NULL,",
                      "address VARCHAR(45) NOT NULL,",
                      "count SMALLINT(6) NOT NULL,",
                      "time INTEGER(11) NOT NULL",
                    ")");
 
         try {
             stmt.executeUpdate();
             stmt.close();
         } catch (SQLException e) {
             stmt.close();
             throw new StorageException(e);
         }
     }
 
     private void initRecords()
         throws StorageException
     {
         SQLStatement stmt;
         String       ainc;
 
         if (database.hasTable(TABLE_RECORDS))
             return;
 
         stmt = database.createStatement();
         ainc = (database.getType() == SQLType.MYSQL) ? "AUTO_INCREMENT" : "";
 
         stmt.query("CREATE TABLE", TABLE_RECORDS, "(",
                      "id INTEGER PRIMARY KEY ", ainc, ",",
                      "target VARCHAR(45) NOT NULL,",
                      "issuer VARCHAR(36) NOT NULL,",
                      "message VARCHAR(255) NOT NULL,",
                      "timeout INTEGER(11) NOT NULL,",
                      "time INTEGER(11) NOT NULL,",
                      "flags SMALLINT(6) NOT NULL",
                    ")");
 
         try {
             stmt.executeUpdate();
             stmt.close();
         } catch (SQLException e) {
             stmt.close();
             throw new StorageException(e);
         }
     }
 
     private List<StorageLogin> getLogins(SQLStatement stmt)
         throws StorageException
     {
         ArrayList<StorageLogin> logins;
         StorageLogin   login;
         StoragePlayer  plyr;
         StorageAddress addr;
         ResultSet      rs;
         String         ident;
         String         alias;
         String         saddr;
 
         logins = new ArrayList<StorageLogin>();
 
         try {
             rs = stmt.executeQuery();
 
             if (!database.hasTable(TABLE_ADDRESSES)) {
                 while (rs.next()) {
                     ident = rs.getString("ident");
                     alias = rs.getString("alias");
                     saddr = rs.getString("address");
 
                     plyr  = new StoragePlayer(ident, alias);
                     addr  = new StorageAddress(saddr);
                     login = new StorageLogin(plyr, addr);
 
                     login.id    = rs.getInt("id");
                     login.count = rs.getInt("count");
                     login.time  = rs.getLong("time");
 
                     logins.add(login);
                 }
             } else {
                 while (rs.next()) {
                     ident = rs.getString("player");
                     saddr = rs.getString("address");
 
                     login = new StorageLogin(ident, saddr);
 
                     login.id    = rs.getInt("id");
                     login.count = rs.getInt("logins");
                     login.time  = rs.getLong("time");
 
                     logins.add(login);
                 }
             }
 
             stmt.close();
         } catch (SQLException e) {
             stmt.close();
             throw new StorageException(e);
         }
 
         return logins;
     }
 
     private List<StorageRecord> getRecords(SQLStatement stmt)
         throws StorageException
     {
         ArrayList<StorageRecord> recds;
         StorageRecord recd;
         ResultSet     rs;
         String        target;
         String        issuer;
 
         recds = new ArrayList<StorageRecord>();
 
         try {
             rs = stmt.executeQuery();
 
             while (rs.next()) {
                 target = rs.getString("target");
                 issuer = rs.getString("issuer");
 
                 recd = new StorageRecord(target, issuer);
 
                 recd.id      = rs.getInt("id");
                 recd.message = rs.getString("message");
                 recd.timeout = rs.getLong("timeout");
                 recd.time    = rs.getLong("time");
                 recd.flags   = rs.getInt("flags");
 
                 recds.add(recd);
             }
 
             stmt.close();
         } catch (SQLException e) {
             stmt.close();
             throw new StorageException(e);
         }
 
         return recds;
     }
 }
