 package com.vaadin.addon.sqlcontainer.query;
 
 import java.io.IOException;
 import java.sql.Connection;
 import java.sql.DatabaseMetaData;
 import java.sql.ResultSet;
 import java.sql.ResultSetMetaData;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.EventObject;
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 
 import com.vaadin.addon.sqlcontainer.ColumnProperty;
 import com.vaadin.addon.sqlcontainer.RowId;
 import com.vaadin.addon.sqlcontainer.RowItem;
 import com.vaadin.addon.sqlcontainer.TemporaryRowId;
 import com.vaadin.addon.sqlcontainer.connection.JDBCConnectionPool;
 import com.vaadin.addon.sqlcontainer.query.Filter.ComparisonType;
 import com.vaadin.addon.sqlcontainer.query.generator.DefaultSQLGenerator;
 import com.vaadin.addon.sqlcontainer.query.generator.MSSQLGenerator;
 import com.vaadin.addon.sqlcontainer.query.generator.SQLGenerator;
 
 @SuppressWarnings("serial")
 public class TableQuery implements QueryDelegate,
         QueryDelegate.RowIdChangeNotifier {
 
     private FilteringMode filterMode = FilteringMode.FILTERING_MODE_INCLUSIVE;
 
     private String tableName;
     private List<String> primaryKeyColumns;
     private String versionColumn;
 
     private List<Filter> filters;
     private List<OrderBy> orderBys;
 
     private SQLGenerator sqlGenerator;
 
     private JDBCConnectionPool connectionPool;
 
     /** Transaction handling */
     private transient Connection activeConnection;
     private boolean transactionOpen;
 
     /** Set to true to output generated SQL Queries to System.out */
     private boolean debug = false;
 
    /** Row ID change listeners */
    private LinkedList<RowIdChangeListener> rowIdChangeListeners;
    /** Row ID change events, stored until commit() is called */
    private List<RowIdChangeEvent> bufferedEvents = new ArrayList<RowIdChangeEvent>();
 
     /**
      * Prevent no-parameters instantiation of TableQuery
      */
     @SuppressWarnings("unused")
     private TableQuery() {
     }
 
     /**
      * Creates a new TableQuery using the given connection pool, SQL generator
      * and table name to fetch the data from. All parameters must be non-null.
      * 
      * @param tableName
      *            Name of the database table to connect to
      * @param connectionPool
      *            Connection pool for accessing the database
      * @param sqlGenerator
      *            SQL query generator implementation
      */
     public TableQuery(String tableName, JDBCConnectionPool connectionPool,
             SQLGenerator sqlGenerator) {
         if (tableName == null || tableName.trim().length() < 1
                 || connectionPool == null || sqlGenerator == null) {
             throw new IllegalArgumentException(
                     "All parameters must be non-null and a table name must be given.");
         }
         this.tableName = tableName;
         this.sqlGenerator = sqlGenerator;
         this.connectionPool = connectionPool;
         fetchMetaData();
     }
 
     /**
      * Creates a new TableQuery using the given connection pool and table name
      * to fetch the data from. All parameters must be non-null. The default SQL
      * generator will be used for queries.
      * 
      * @param tableName
      *            Name of the database table to connect to
      * @param connectionPool
      *            Connection pool for accessing the database
      */
     public TableQuery(String tableName, JDBCConnectionPool connectionPool) {
         this(tableName, connectionPool, new DefaultSQLGenerator());
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see com.vaadin.addon.sqlcontainer.query.QueryDelegate#getCount()
      */
     public int getCount() throws SQLException {
         debug("Fetching count...");
         String query = sqlGenerator.generateSelectQuery(tableName, filters,
                 filterMode, null, 0, 0, "COUNT(*)");
         boolean shouldCloseTransaction = false;
         if (!transactionOpen) {
             shouldCloseTransaction = true;
             beginTransaction();
         }
         ResultSet r = executeQuery(query);
         r.next();
         int count = r.getInt(1);
         if (shouldCloseTransaction) {
             commit();
         }
         return count;
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see com.vaadin.addon.sqlcontainer.query.QueryDelegate#getResults(int,
      * int)
      */
     public ResultSet getResults(int offset, int pagelength) throws SQLException {
         String query;
         /*
          * If no ordering is explicitly set, results will be ordered by the
          * first primary key column.
          */
         if (orderBys == null || orderBys.isEmpty()) {
             List<OrderBy> ob = new ArrayList<OrderBy>();
             ob.add(new OrderBy(primaryKeyColumns.get(0), true));
             query = sqlGenerator.generateSelectQuery(tableName, filters,
                     filterMode, ob, offset, pagelength, null);
         } else {
             query = sqlGenerator.generateSelectQuery(tableName, filters,
                     filterMode, orderBys, offset, pagelength, null);
         }
         return executeQuery(query);
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see com.vaadin.addon.sqlcontainer.query.QueryDelegate#
      * implementationRespectsPagingLimits()
      */
     public boolean implementationRespectsPagingLimits() {
         return true;
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see
      * com.vaadin.addon.sqlcontainer.query.QueryDelegate#storeRow(com.vaadin
      * .addon.sqlcontainer.RowItem)
      */
     public int storeRow(RowItem row) throws UnsupportedOperationException,
             SQLException {
         if (row == null) {
             throw new IllegalArgumentException("Row argument must be non-null.");
         }
         String query = null;
         if (row.getId() instanceof TemporaryRowId) {
             try {
                 ((ColumnProperty) row.getItemProperty(versionColumn))
                         .setVersionColumn(true);
             } catch (Exception e) {
                 throw new IllegalStateException(
                         "Version column not set or does not exist.", e);
             }
             query = sqlGenerator.generateInsertQuery(tableName, row);
             return executeUpdateReturnKeys(query, row);
         } else {
             try {
                 ((ColumnProperty) row.getItemProperty(versionColumn))
                         .setVersionColumn(true);
             } catch (Exception e) {
                 throw new IllegalStateException(
                         "Version column not set or does not exist.", e);
             }
             query = sqlGenerator.generateUpdateQuery(tableName, row);
             return executeUpdate(query);
         }
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see
      * com.vaadin.addon.sqlcontainer.query.QueryDelegate#setFilters(java.util
      * .List)
      */
     public void setFilters(List<Filter> filters, FilteringMode filteringMode)
             throws UnsupportedOperationException {
         filterMode = filteringMode;
         if (filters == null) {
             this.filters = null;
             return;
         }
         this.filters = Collections.unmodifiableList(filters);
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see
      * com.vaadin.addon.sqlcontainer.query.QueryDelegate#setFilters(java.util
      * .List)
      */
     public void setFilters(List<Filter> filters)
             throws UnsupportedOperationException {
         this.setFilters(filters, FilteringMode.FILTERING_MODE_INCLUSIVE);
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see
      * com.vaadin.addon.sqlcontainer.query.QueryDelegate#setOrderBy(java.util
      * .List)
      */
     public void setOrderBy(List<OrderBy> orderBys)
             throws UnsupportedOperationException {
         if (orderBys == null) {
             this.orderBys = null;
             return;
         }
         this.orderBys = Collections.unmodifiableList(orderBys);
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see com.vaadin.addon.sqlcontainer.query.QueryDelegate#beginTransaction()
      */
     public void beginTransaction() throws UnsupportedOperationException,
             SQLException {
         if (transactionOpen && activeConnection != null) {
             throw new IllegalStateException();
         }
         debug("DB -> begin transaction");
         activeConnection = connectionPool.reserveConnection();
         activeConnection.setAutoCommit(false);
         transactionOpen = true;
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see com.vaadin.addon.sqlcontainer.query.QueryDelegate#commit()
      */
     public void commit() throws UnsupportedOperationException, SQLException {
         if (transactionOpen && activeConnection != null) {
             debug("DB -> commit");
             activeConnection.commit();
             connectionPool.releaseConnection(activeConnection);
         } else {
             throw new SQLException("No active transaction");
         }
         transactionOpen = false;

        /* Handle firing row ID change events */
        RowIdChangeEvent[] unFiredEvents = bufferedEvents
                .toArray(new RowIdChangeEvent[] {});
        bufferedEvents.clear();
        if (rowIdChangeListeners != null && !rowIdChangeListeners.isEmpty()) {
            for (RowIdChangeListener r : rowIdChangeListeners) {
                for (RowIdChangeEvent e : unFiredEvents) {
                    r.rowIdChange(e);
                }
            }
        }
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see com.vaadin.addon.sqlcontainer.query.QueryDelegate#rollback()
      */
     public void rollback() throws UnsupportedOperationException, SQLException {
         if (transactionOpen && activeConnection != null) {
             debug("DB -> rollback");
             activeConnection.rollback();
             connectionPool.releaseConnection(activeConnection);
         } else {
             throw new SQLException("No active transaction");
         }
         transactionOpen = false;
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see
      * com.vaadin.addon.sqlcontainer.query.QueryDelegate#getPrimaryKeyColumns()
      */
     public List<String> getPrimaryKeyColumns() {
         return Collections.unmodifiableList(primaryKeyColumns);
     }
 
     public String getVersionColumn() {
         return versionColumn;
     }
 
     public void setVersionColumn(String versionColumn) {
         this.versionColumn = versionColumn;
     }
 
     public String getTableName() {
         return tableName;
     }
 
     public SQLGenerator getSqlGenerator() {
         return sqlGenerator;
     }
 
     /**
      * Executes the given query string using either the active connection if a
      * transaction is already open, or a new connection from this query's
      * connection pool.
      * 
      * @param query
      *            Query to execute
      * @return ResultSet of the query
      * @throws SQLException
      */
     private ResultSet executeQuery(String query) throws SQLException {
         Connection c = null;
         if (transactionOpen && activeConnection != null) {
             c = activeConnection;
         } else {
             throw new SQLException("No active transaction!");
         }
         Statement statement = c.createStatement(
                 ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
         debug("DB -> " + query);
         return statement.executeQuery(query);
     }
 
     /**
      * Executes the given update query string using either the active connection
      * if a transaction is already open, or a new connection from this query's
      * connection pool.
      * 
      * @param query
      *            Query to execute
      * @return Number of affected rows
      * @throws SQLException
      */
     private int executeUpdate(String query) throws SQLException {
         Connection c = null;
         try {
             if (transactionOpen && activeConnection != null) {
                 c = activeConnection;
             } else {
                 c = connectionPool.reserveConnection();
             }
             Statement statement = c.createStatement(
                     ResultSet.TYPE_SCROLL_INSENSITIVE,
                     ResultSet.CONCUR_READ_ONLY);
             debug("DB -> " + query);
             return statement.executeUpdate(query);
         } finally {
             if (!transactionOpen) {
                 connectionPool.releaseConnection(c);
             }
         }
     }
 
     /**
      * Executes the given update query string using either the active connection
      * if a transaction is already open, or a new connection from this query's
      * connection pool.
      * 
      * Additionally notifies registered RowIdChangeListeners of a changed RowId.
      * 
      * @param query
      *            Query to execute
      * @return Number of affected rows
      * @throws SQLException
      */
     private int executeUpdateReturnKeys(String query, RowItem row)
             throws SQLException {
         Connection c = null;
         try {
             if (transactionOpen && activeConnection != null) {
                 c = activeConnection;
             } else {
                 c = connectionPool.reserveConnection();
             }
             Statement statement = c.createStatement();
 
             debug("DB -> " + query);
             int result = statement.executeUpdate(query, primaryKeyColumns
                     .toArray(new String[0]));
 
             try {
                 /* Fetch primary key values and generate a map out of them. */
                 Map<String, Object> values = new HashMap<String, Object>();
                 ResultSet genKeys = statement.getGeneratedKeys();
                 ResultSetMetaData rsmd = genKeys.getMetaData();
                 int colCount = rsmd.getColumnCount();
                 if (genKeys.next()) {
                     for (int i = 1; i <= colCount; i++) {
                         values.put(rsmd.getColumnName(i), genKeys.getObject(i));
                     }
                 }
                 /* Generate new RowId */
                 List<Object> newRowId = new ArrayList<Object>();
                 if (values.size() == 1) {
                     if (primaryKeyColumns.size() == 1) {
                         newRowId.add(values.get(values.keySet().iterator()
                                 .next()));
                     } else {
                         for (String s : primaryKeyColumns) {
                             if (!((ColumnProperty) row.getItemProperty(s))
                                     .isReadOnlyChangeAllowed()) {
                                 newRowId.add(values.get(values.keySet()
                                         .iterator().next()));
                             } else {
                                 newRowId.add(values.get(s));
                             }
                         }
                     }
                 } else {
                     for (String s : primaryKeyColumns) {
                         newRowId.add(values.get(s));
                     }
                 }
                 RowId newId = new RowId(newRowId.toArray());
                bufferedEvents.add(new RowIdChangeEvent(row.getId(), newId));
             } catch (Exception e) {
                 e.printStackTrace();
                 debug("Failed to fetch key values on insert: " + e.getMessage());
             }
 
             return result;
         } finally {
             if (!transactionOpen) {
                 connectionPool.releaseConnection(c);
             }
         }
     }
 
     /**
      * Fetches name(s) of primary key column(s) from DB metadata.
      * 
      * Also tries to get the escape string to be used in search strings.
      */
     private void fetchMetaData() {
         Connection c = null;
         try {
             c = connectionPool.reserveConnection();
             DatabaseMetaData dbmd = c.getMetaData();
             if (dbmd != null) {
                 ResultSet tables = dbmd.getTables(null, null, tableName, null);
                 if (!tables.next()) {
                     tables = dbmd.getTables(null, null,
                             tableName.toUpperCase(), null);
                     if (!tables.next()) {
                         throw new IllegalArgumentException(
                                 "Table with the name \""
                                         + tableName
                                         + "\" was not found. Check your database contents.");
                     } else {
                         tableName = tableName.toUpperCase();
                     }
                 }
                 ResultSet rs = dbmd.getPrimaryKeys(null, null, tableName);
                 List<String> names = new ArrayList<String>();
                 while (rs.next()) {
                     names.add(rs.getString("COLUMN_NAME"));
                 }
                 if (!names.isEmpty()) {
                     primaryKeyColumns = names;
                 }
                 if (primaryKeyColumns == null || primaryKeyColumns.isEmpty()) {
                     throw new IllegalArgumentException(
                             "Primary key constraints have not been defined for the table \""
                                     + tableName
                                     + "\". Use FreeFormQuery to access this table.");
                 }
                 for (String colName : primaryKeyColumns) {
                     if (colName.equalsIgnoreCase("rownum")) {
                         if (getSqlGenerator() instanceof MSSQLGenerator
                                 || getSqlGenerator() instanceof MSSQLGenerator) {
                             throw new IllegalArgumentException(
                                     "When using Oracle or MSSQL, a primary key column"
                                             + " named \'rownum\' is not allowed!");
                         }
                     }
                 }
             }
         } catch (SQLException e) {
             throw new RuntimeException(e);
         } finally {
             connectionPool.releaseConnection(c);
         }
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see
      * com.vaadin.addon.sqlcontainer.query.QueryDelegate#removeRow(com.vaadin
      * .addon.sqlcontainer.RowItem)
      */
     public boolean removeRow(RowItem row) throws UnsupportedOperationException,
             SQLException {
         debug("Removing row with id: " + row.getId().getId()[0].toString());
         if (executeUpdate(sqlGenerator.generateDeleteQuery(getTableName(), row)) == 1) {
             return true;
         }
         return false;
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see
      * com.vaadin.addon.sqlcontainer.query.QueryDelegate#containsRowWithKey(
      * java.lang.Object[])
      */
     public boolean containsRowWithKey(Object... keys) throws SQLException {
         ArrayList<Filter> filtersAndKeys = new ArrayList<Filter>();
         if (filters != null) {
             filtersAndKeys.addAll(filters);
         }
         int ix = 0;
         for (String colName : primaryKeyColumns) {
             filtersAndKeys.add(new Filter(colName, ComparisonType.EQUALS,
                     String.valueOf(keys[ix])));
             ix++;
         }
         String query = sqlGenerator.generateSelectQuery(tableName,
                 filtersAndKeys, orderBys, 0, 0, "*");
 
         boolean shouldCloseTransaction = false;
         if (!transactionOpen) {
             shouldCloseTransaction = true;
             beginTransaction();
         }
         try {
             ResultSet rs = executeQuery(query);
             boolean contains = rs.next();
             rs.close();
             return contains;
         } finally {
             if (shouldCloseTransaction) {
                 commit();
             }
         }
     }
 
     /**
      * Output a debug message
      * 
      * @param message
      */
     private void debug(String message) {
         if (debug) {
             System.out.println(message);
         }
     }
 
     /**
      * Enable or disable debug mode.
      * 
      * @param debug
      */
     public void setDebug(boolean debug) {
         this.debug = debug;
     }
 
     private void writeObject(java.io.ObjectOutputStream out) throws IOException {
         try {
             rollback();
         } catch (SQLException ignored) {
         }
         out.defaultWriteObject();
     }
 
     /**
      * Simple RowIdChangeEvent implementation.
      */
     public class RowIdChangeEvent extends EventObject implements
             QueryDelegate.RowIdChangeEvent {
 
         private RowId oldId;
         private RowId newId;
 
         private RowIdChangeEvent(RowId oldId, RowId newId) {
             super(oldId);
             this.oldId = oldId;
             this.newId = newId;
         }
 
         public RowId getNewRowId() {
             return newId;
         }
 
         public RowId getOldRowId() {
             return oldId;
         }
     }
 
     public void addListener(RowIdChangeListener listener) {
         if (rowIdChangeListeners == null) {
             rowIdChangeListeners = new LinkedList<QueryDelegate.RowIdChangeListener>();
         }
         rowIdChangeListeners.add(listener);
     }
 
     public void removeListener(RowIdChangeListener listener) {
         if (rowIdChangeListeners != null) {
             rowIdChangeListeners.remove(listener);
         }
     }
 }
