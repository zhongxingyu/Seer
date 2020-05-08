 package org.nsdl.mptstore.core;
 
 import java.sql.Connection;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.Statement;
 import java.sql.SQLException;
 
 import java.text.ParseException;
 
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.Map;
 import java.util.Set;
 
 import javax.sql.DataSource;
 
 import org.apache.log4j.Logger;
 
 import org.nsdl.mptstore.rdf.PredicateNode;
 import org.nsdl.mptstore.util.NTriplesUtil;
 
 /**
  * A <code>TableManager</code> designed to perform DDL operations
  * on separate connections from those used for DML.
  *
  * The DDL-in-a-separate-connection strategy employed by this
  * implementation should work with a wide variety of databases.
  *
  * @author cwilper@cs.cornell.edu
  */
 public class BasicTableManager implements TableManager {
 
     /**
      * The Logger for this class.
      */
     private static final Logger LOG =
             Logger.getLogger(BasicTableManager.class.getName());
 
     /**
      * The <code>DataSource</code> from which to obtain connections
      * for DDL operations.
      */
     private DataSource _dataSource;
 
     /**
      * The <code>DDLGenerator</code> for this instance.
      */
     private DDLGenerator _ddlGenerator;
 
     /**
      * The name of the table in which the table-to-predicate mappings are
      * persisted.
      */
     private String _mapTable;
 
     /**
      * The prefix for all predicate table names.
      */
     private String _soTablePrefix;
 
     /**
      * The in-memory predicate-to-table mapping.
      */
     private Map<PredicateNode, String> _map;
 
     /**
      * The in-memory table-to-predicate mapping.
      */
     private Map<String, PredicateNode> _reverseMap;
 
     /**
      * Initialize the table manager.
      *
      * This will create the map table if it doesn't yet exist,
      * and will read the current mappings into memory.
      *
      * @param dataSource The DataSource from which to obtain
      *        connections for DDL operations.
      * @param ddlGenerator The DDLGenerator to use when DDL is needed.
      * @param mapTable The name of the table in which the table-to-predicate
      *        mappings are persisted.
      * @param soTablePrefix The prefix for all predicate table names.
      * @throws SQLException if any kind of database error occurs.
      */
     public BasicTableManager(final DataSource dataSource,
                              final DDLGenerator ddlGenerator,
                              final String mapTable,
                              final String soTablePrefix) throws SQLException {
 
         _dataSource = dataSource;
         _ddlGenerator = ddlGenerator;
         _mapTable = mapTable;
         _soTablePrefix = soTablePrefix;
 
         Connection conn = dataSource.getConnection();
         try {
             if (!mapTableExists(conn)) {
                 LOG.info("Creating map table");
                 executeDDL(conn,
                            _ddlGenerator.getCreateMapTableDDL(
                            _mapTable).iterator());
             }
             loadMapTable(conn);
         } finally {
             try {
                 conn.close();
             } catch (SQLException e) {
                 LOG.warn("unable to close/release connection", e);
             }
         }
 
     }
 
     /** {@inheritDoc} */
     public String getOrMapTableFor(final PredicateNode predicate)
             throws SQLException {
         String table = getTableFor(predicate);
         if (table != null) {
             return table;
         } else {
             Connection conn = _dataSource.getConnection();
             try {
                 return mapTableFor(predicate, conn);
             } finally {
                 try {
                     conn.close();
                 } catch (SQLException e) {
                     LOG.warn("unable to close/release connection", e);
                 }
             }
         }
     }
 
     /** {@inheritDoc} */
     public String getTableFor(final PredicateNode predicate) {
         synchronized (_map) {
             return _map.get(predicate);
         }
     }
 
     /** {@inheritDoc} */
     public PredicateNode getPredicateFor(final String table) {
         synchronized (_map) {
             return _reverseMap.get(table);
         }
     }
 
     /** {@inheritDoc} */
     public Set<String> getTables() {
         synchronized (_map) {
             return new HashSet<String>(_reverseMap.keySet());
         }
     }
 
     /** {@inheritDoc} */
     public Set<PredicateNode> getPredicates() {
         synchronized (_map) {
             return new HashSet<PredicateNode>(_map.keySet());
         }
     }
 
     /** {@inheritDoc} */
     public int dropEmptyPredicateTables() throws SQLException {
         LOG.info("Dropping empty predicate tables");
         return dropPredicateTables(false);
     }
 
     /**
      * Check whether the map table exists.
      *
      * @param conn The connection to use for determining the table's existence.
      * @return true if so, false if not.
      * @throws SQLException if a database error occurs.
      */
     private boolean mapTableExists(final Connection conn) throws SQLException {
 
         ResultSet results = conn.getMetaData().getTables(null, null,
                                                          _mapTable, null);
         try {
             if (results.next()) {
                 LOG.info("Found pre-existing map table");
                 return true;
             }
         } finally {
             try {
                 results.close();
             } catch (SQLException e) {
                 LOG.warn("unable to close result set", e);
             }
         }
 
         // above only works for some dbs,
         // so if false, do another check
         Statement st = conn.createStatement();
         try {
             boolean exists = false;
             try {
                 results = null;
                 results = st.executeQuery("SELECT COUNT(*) FROM " + _mapTable);
                 exists = results.next();
             } catch (SQLException e) {
                // assume the db is just telling us the table doesn't exist
                return false;
             } finally {
                 try {
                     if (results != null) {
                         results.close();
                     }
                 } catch (SQLException e) {
                     LOG.warn("unable to close result set", e);
                 }
             }
             if (exists) {
                 LOG.info("Found pre-existing map table");
             } else {
                 LOG.info("Map table does not yet exist");
             }
             return exists;
         } finally {
             try {
                 st.close();
             } catch (SQLException e) {
                 LOG.warn("unable to close statement", e);
             }
         }
     }
 
     /**
      * Read the content of the map table into memory.
      *
      * @param conn The connection to use for reading.
      * @throws SQLException if a database error occurs.
      */
     private void loadMapTable(final Connection conn) throws SQLException {
         LOG.info("Loading map table");
         _map = new HashMap<PredicateNode, String>();
         _reverseMap = new HashMap<String, PredicateNode>();
         Statement st = conn.createStatement();
         ResultSet results = null;
         String pString = null;
         try {
             results = st.executeQuery("SELECT pKey, p FROM " + _mapTable);
             while (results.next()) {
                 String table = _soTablePrefix + results.getInt(1);
                 pString = results.getString(2);
                 PredicateNode predicate = NTriplesUtil.parsePredicate(pString);
                 _map.put(predicate, table);
                 _reverseMap.put(table, predicate);
             }
         } catch (ParseException e) {
             throw new SQLException("Unable to parse predicate ("
                     + pString + ") from map table. " + e.getMessage());
         } finally {
             if (results != null) {
                 try {
                     results.close();
                 } catch (SQLException e) {
                     LOG.warn("unable to close result set", e);
                 }
             }
             try {
                 st.close();
             } catch (SQLException e) {
                 LOG.warn("unable to close statement", e);
             }
         }
     }
 
     /**
      * Add a new table, mapping it to the given predicate.
      * <p>
      *   The new mapping will be persisted to the database
      *   and stored in memory.
      * </p>
      * <p>
      *   If a table is already mapped to the given predicate,
      *   no action will be taken; the table name will just be
      *   returned.
      * </p>
      *
      * @param predicate The predicate.
      * @param conn The connection on which to create the table and
      *        persist the mapping.
      * @return the table name.
      * @throws SQLException if a database error occurs.
      */
     private synchronized String mapTableFor(final PredicateNode predicate,
                                             final Connection conn)
             throws SQLException {
 
         // re-check map in case the predicate was added
         // while this thread was blocking
         String table = getTableFor(predicate);
         if (table != null) {
             return table;
         } else {
             LOG.info("Mapping new table for predicate: "
                     + predicate.toString());
             int id = addPredicateToMapTable(predicate, conn);
             try {
                 table = _soTablePrefix + id;
                 executeDDL(conn,
                            _ddlGenerator.getCreateSOTableDDL(table)
                            .iterator());
             } catch (SQLException e) {
                 try {
                     // since we're not on a transaction, we must manually
                     // back out the prior INSERT into the map table
                     deletePredicateFromMapTable(predicate, conn);
                 } catch (SQLException e2) {
                     LOG.warn("unable to clean up entry from map table after "
                             + "failure to create predicate table", e2);
                 }
                 throw e;
             }
             synchronized (_map) {
                 _map.put(predicate, table);
                 _reverseMap.put(table, predicate);
             }
             return table;
         }
     }
 
     /**
      * Add a new predicate to the map table.
      *
      * @param predicate The predicate to add.
      * @param conn The connection to use.
      * @return the auto-generated id for the predicate, used to formulate
      *         the name for the new predicate table.
      * @throws SQLException if a database error occurs.
      */
     private int addPredicateToMapTable(final PredicateNode predicate,
                                        final Connection conn)
             throws SQLException {
 
         String pString = predicate.toString();
 
         // add to map table
         PreparedStatement ps = conn.prepareStatement(
                 "INSERT INTO " + _mapTable + " (p) VALUES (?)");
         try {
             ps.setString(1, pString);
             ps.execute();
         } finally {
             try {
                 ps.close();
             } catch (SQLException e) {
                 LOG.warn("unable to close statement", e);
             }
         }
 
         // select db-generated id
         ps = conn.prepareStatement(
                 "SELECT pKey from " + _mapTable + " WHERE p = ?");
         try {
             ps.setString(1, pString);
             ResultSet rs = ps.executeQuery();
             try {
                 rs.next();
                 return rs.getInt(1);
             } finally {
                 try {
                     rs.close();
                 } catch (SQLException e) {
                     LOG.warn("unable to close result set", e);
                 }
             }
         } finally {
             try {
                 ps.close();
             } catch (SQLException e) {
                 LOG.warn("unable to close statement", e);
             }
         }
 
     }
 
     /**
      * Remove the persistent mapping of the given predicate.
      *
      * @param predicate The predicate whose mapping should be removed.
      * @param conn The connection on which to perform the operation.
      * @throws SQLException if a database error occurrs.
      */
     private void deletePredicateFromMapTable(final PredicateNode predicate,
                                              final Connection conn)
             throws SQLException {
         PreparedStatement ps = conn.prepareStatement(
                 "DELETE FROM " + _mapTable + " WHERE p = ?");
         try {
             ps.setString(1, predicate.toString());
             ps.execute();
         } finally {
             try {
                 ps.close();
             } catch (SQLException e) {
                 LOG.warn("unable to close statement", e);
             }
         }
     }
 
 
     /**
      * Execute the given DDL statements.
      *
      * @param conn The connection to use.
      * @param ddlIter The DDL to execute.
      * @throws SQLException if a database error occurs.
      */
     private void executeDDL(final Connection conn,
                             final Iterator<String> ddlIter)
             throws SQLException {
         Statement st = conn.createStatement();
         try {
             while (ddlIter.hasNext()) {
                 String ddl = ddlIter.next();
                 LOG.info("Executing DDL: " + ddl);
                 st.executeUpdate(ddl);
             }
         } finally {
             try {
                 st.close();
             } catch (SQLException e) {
                 LOG.warn("unable to close statement", e);
             }
         }
     }
 
     /**
      * Drop and unmap the indicated predicate tables.
      *
      * The <code>DataSource</code> given in the constructor
      * will be used for the connection.
      *
      * @param all Boolean indicating whether to drop all of them,
      *        or just the ones that are empty.
      * @return the number of dropped predicate tables.
      * @throws SQLException if a database error occurs.
      */
     private synchronized int dropPredicateTables(final boolean all)
             throws SQLException {
 
         int dropCount = 0;
         Connection conn = _dataSource.getConnection();
         try {
             Iterator<String> tables = getTables().iterator();
             while (tables.hasNext()) {
                 String table = tables.next();
                 if (all || isPredicateTableEmpty(table, conn)) {
                     PredicateNode predicate = getPredicateFor(table);
                     unmapPredicate(predicate, table, conn);
                     dropCount++;
                 }
             }
             return dropCount;
         } finally {
             try {
                 conn.close();
             } catch (SQLException e) {
                 LOG.warn("unable to close/release connection", e);
             }
         }
     }
 
     /** {@inheritDoc} */
     public int dropAllPredicateTables() throws SQLException {
         LOG.info("Dropping all predicate tables");
         return dropPredicateTables(true);
     }
 
     /**
      * Determine whether the given predicate table is empty.
      *
      * @param table The name of the table.
      * @param conn The connection on which to make the determination.
      * @return true if empty, false otherwise.
      * @throws SQLException if a database error occurs.
      */
     private boolean isPredicateTableEmpty(final String table,
                                           final Connection conn)
             throws SQLException {
         Statement st = conn.createStatement();
         try {
             ResultSet results = st.executeQuery("SELECT MAX(s) FROM " + table);
             try {
                 return !results.next();
             } finally {
                 try {
                     results.close();
                 } catch (SQLException e) {
                     LOG.warn("unable to close result set", e);
                 }
             }
         } finally {
             try {
                 st.close();
             } catch (SQLException e) {
                 LOG.warn("unable to close statement", e);
             }
         }
     }
 
     /**
      * Remove the predicate from the memory and database maps,
      * then drop the associated table.
      *
      * @param predicate The predicate to unmap.
      * @param table The associated predicate table.
      * @param conn The connection to use.
      * @throws SQLException if a database error occurs.
      */
     private void unmapPredicate(final PredicateNode predicate,
                                 final String table,
                                 final Connection conn)
             throws SQLException {
 
         String pString = predicate.toString();
 
         LOG.info("Unmapping " + pString
                 + " and dropping associated table: " + table);
 
         _map.remove(predicate);
         _reverseMap.remove(table);
 
         PreparedStatement ps = conn.prepareStatement(
                 "DELETE FROM " + _mapTable + " WHERE p = ?");
         try {
             ps.setString(1, pString);
             ps.execute();
         } finally {
             try {
                 ps.close();
             } catch (SQLException e) {
                 LOG.warn("unable to close statement", e);
             }
         }
 
         executeDDL(conn,
                    _ddlGenerator.getDropSOTableDDL(table).iterator());
 
     }
 
 }
