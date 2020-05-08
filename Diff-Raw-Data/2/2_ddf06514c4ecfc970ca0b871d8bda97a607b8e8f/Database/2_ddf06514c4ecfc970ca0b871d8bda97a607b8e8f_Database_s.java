 /*
  * This file is part of SQLDatabaseAPI (2012).
  *
  * SQLDatabaseAPI is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * SQLDatabaseAPI is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with SQLDatabaseAPI.  If not, see <http://www.gnu.org/licenses/>.
  *
  * Last modified: 29.12.12 16:30
  */
 
 package com.p000ison.dev.sqlapi;
 
 import com.p000ison.dev.sqlapi.annotation.DatabaseTable;
 import com.p000ison.dev.sqlapi.exception.DatabaseConnectionException;
 import com.p000ison.dev.sqlapi.exception.QueryException;
 import com.p000ison.dev.sqlapi.exception.RegistrationException;
 import com.p000ison.dev.sqlapi.query.PreparedQuery;
 import com.p000ison.dev.sqlapi.query.PreparedSelectQuery;
 import com.p000ison.dev.sqlapi.query.SelectQuery;
 
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 import java.util.concurrent.locks.Lock;
 import java.util.concurrent.locks.ReentrantLock;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 /**
  * A Database holds the connection to a database. If you can to extend this API you will have to implement this
  * and modify it for you database engine.
  */
 public abstract class Database {
 
     /**
      * The configuration object which holds all settings
      */
     private DatabaseConfiguration configuration;
     /**
      * Whether old columns should be dropped
      */
     private boolean dropOldColumns = false;
     /**
      * A map of registered tables (classes) and a list of columns
      */
     private Set<RegisteredTable> registeredTables = new HashSet<RegisteredTable>();
 
     public static final int UNSUPPORTED_TYPE = Integer.MAX_VALUE;
 
     private static Logger logger;
 
     private static final Lock accessLock = new ReentrantLock();
 
     static void log(Level level, String msg, Object... args) {
         if (logger == null) {
             return;
         }
 
         logger.log(level, String.format(msg, args));
     }
 
     public static void setLogger(Logger logger) {
         Database.logger = logger;
     }
 
     /**
      * Creates a new database connection based on the configuration
      *
      * @param configuration The database configuration
      */
     protected Database(DatabaseConfiguration configuration) throws DatabaseConnectionException {
         this.configuration = configuration;
         String driver = configuration.getDriverName();
 
         try {
             Class.forName(driver);
         } catch (Exception e) {
             throw new DatabaseConnectionException(configuration, "Failed to load driver " + driver + "!");
         }
     }
 
     /**
      * Gets the name of a table
      *
      * @param clazz The class of the {@link TableObject}.
      * @return The name
      */
     static String getTableName(Class<? extends TableObject> clazz) {
         DatabaseTable annotation = clazz.getAnnotation(DatabaseTable.class);
         return annotation == null ? null : annotation.name();
     }
 
     public void saveStoredValues(Class<? extends TableObject> clazz) {
         RegisteredTable table = getRegisteredTable(clazz);
         table.saveStoredValues();
     }
 
     /**
      * Closes the connection to the database
      *
      * @throws QueryException
      */
     public final void close() throws QueryException {
         for (RegisteredTable table : registeredTables) {
             table.close();
         }
         closeDatabaseConnection();
     }
 
     protected abstract void closeDatabaseConnection() throws QueryException;
 
     /**
      * Creates a new instance of a TableBuilder. This is used to build the queries to create/modify a table.
      *
      * @param table The class of the TableObject
      * @return The TableBuilder
      */
     protected abstract TableBuilder createTableBuilder(Class<? extends TableObject> table);
 
     /**
      * Registers a new TableObject for further use
      *
      * @param table The object to register
      */
     public final RegisteredTable registerTable(TableObject table) {
         return registerTable(table.getClass());
     }
 
     /**
      * Registers a class for further use
      *
      * @param table The class to register
      */
     public final synchronized RegisteredTable registerTable(Class<? extends TableObject> table) {
         TableBuilder builder = createTableBuilder(table);
         RegisteredTable registeredTable = new RegisteredTable(builder.getTableName(), table, builder.getColumns(), builder.getDefaultConstructor());
 
         builder.createTable().createModifyQuery();
 
         for (StringBuilder query : builder.getBuilders()) {
             if (query.length() == 0) {
                 continue;
             }
             log(Level.INFO, "Generating and updating table %s!", registeredTable.getName());
             executeDirectUpdate(query.toString());
         }
 
         registeredTable.prepareAllStatements(this);
         registeredTables.add(registeredTable);
 
         return registeredTable;
     }
 
     /**
      * Checks whether this the connection to the database is still established
      *
      * @return Whether the the the connection is still established
      */
     public abstract boolean isConnected();
 
     /**
      * Checks whether the database exists already.
      *
      * @param table The table to check for
      * @return Whether the table exists
      */
     public abstract boolean existsDatabaseTable(String table);
 
     /**
      * Gets a list of all columns in the database.
      *
      * @param table The table to look up
      * @return A list of columns
      */
     public abstract List<String> getDatabaseColumns(String table);
 
     /**
      * Returns the RegisteredTable of a registered class
      *
      * @param table The table to look for
      * @return The RegisteredTable
      * @throws RegistrationException If the table is not registered
      */
     public synchronized RegisteredTable getRegisteredTable(Class<? extends TableObject> table) {
         for (RegisteredTable registeredTable : registeredTables) {
             if (registeredTable.isRegisteredClass(table)) {
                 return registeredTable;
             }
         }
         throw new RegistrationException(table, "The class %s is not registered!", table.getName());
     }
 
     /**
      * Constructs a new SelectQuery for further use. This should be synchronized with the Database instance
      *
      * @param <T> a TableObject type
      * @return The SelectQuery
      */
     public <T extends TableObject> SelectQuery<T> select() {
         return new DefaultSelectQuery<T>(this);
     }
 
     /**
      * Saves a object to the table in your database. The class of the object must not be not registered!
      * If the there is already an entry in the database with the id of the object or the id is equal or less than 0 the
      * table gets updated else a new entry gets inserted.
      *
      * @param tableObject The object to insert/update
      * @throws RegistrationException If the table is not registered
      */
     public void save(TableObject tableObject) {
         RegisteredTable table = getRegisteredTable(tableObject);
         Column idColumn = table.getIDColumn();
 
         if (((Number) idColumn.getValue(tableObject)).intValue() <= 0 || !existsEntry(table, tableObject)) {
             insert(table, tableObject, idColumn);
         } else {
             update(table, tableObject, idColumn);
         }
     }
 
     public void delete(TableObject tableObject) {
         accessLock.lock();
         try {
             RegisteredTable table = getRegisteredTable(tableObject);
             Column idColumn = table.getIDColumn();
 
             PreparedQuery statement = table.getPreparedDeleteStatement();
            statement.set(0, idColumn.getValue(tableObject));
             statement.update();
         } finally {
             accessLock.unlock();
         }
     }
 
     private RegisteredTable getRegisteredTable(TableObject obj) {
         return getRegisteredTable(obj.getClass());
     }
 
     /**
      * Attempts to update the object in the database
      *
      * @param tableObject The object to update
      * @throws RegistrationException If the table is not registered
      */
     public void update(TableObject tableObject) {
         RegisteredTable table = getRegisteredTable(tableObject);
         Column idColumn = table.getIDColumn();
 
         update(table, tableObject, idColumn);
     }
 
     /**
      * Attempts to insert a entry in the database. Can throw an exception if for example there is a unique column!
      *
      * @param tableObject The object to insert
      * @throws QueryException if the query fails
      */
     public void insert(TableObject tableObject) {
         RegisteredTable table = getRegisteredTable(tableObject);
         Column idColumn = table.getIDColumn();
 
         insert(table, tableObject, idColumn);
     }
 
     private void insert(RegisteredTable registeredTable, TableObject object, Column idColumn) {
         accessLock.lock();
         try {
             PreparedQuery insert = registeredTable.getPreparedInsertStatement();
             setColumnValues(insert, registeredTable, object, idColumn);
             insert.update();
             idColumn.setValue(object, getLastEntryId(registeredTable));
         } finally {
             accessLock.unlock();
         }
     }
 
     private void update(RegisteredTable registeredTable, TableObject object, Column idColumn) {
         accessLock.lock();
         try {
             PreparedQuery update = registeredTable.getPreparedUpdateStatement();
             int i = setColumnValues(update, registeredTable, object, idColumn);
             update.set(idColumn, i, idColumn.getValue(object));
             update.update();
         } finally {
             accessLock.unlock();
         }
     }
 
     public void addUpdateBatch(TableObject object) {
         RegisteredTable table = getRegisteredTable(object);
         PreparedQuery update = table.getPreparedUpdateStatement();
         accessLock.lock();
         try {
             Column id = table.getIDColumn();
             int i = setColumnValues(update, table, object, table.getIDColumn());
             update.set(id, i, id.getValue(object));
             update.addBatch();
         } finally {
             accessLock.unlock();
         }
     }
 
     public void addInsertBatch(TableObject object) {
         RegisteredTable table = getRegisteredTable(object);
         PreparedQuery update = table.getPreparedInsertStatement();
         accessLock.lock();
         try {
             setColumnValues(update, table, object, table.getIDColumn());
             update.addBatch();
         } finally {
             accessLock.unlock();
         }
     }
 
     public void addDeleteBatch(TableObject object) {
         RegisteredTable table = getRegisteredTable(object);
         PreparedQuery update = table.getPreparedDeleteStatement();
         accessLock.lock();
         try {
             update.set(0, table.getIDColumn().getValue(object));
             update.addBatch();
         } finally {
             accessLock.unlock();
         }
     }
 
     /**
      * Runs the batch and executes the stored commands
      *
      * @param clazz   The class
      * @param bitmask Defines whether to run the update, insert or delete statements. Example: 1 | 1 << 1 | 1 << 2 for all
      */
     public void executeBatch(Class<? extends TableObject> clazz, int bitmask) {
         executeBatch(getRegisteredTable(clazz), bitmask);
     }
 
     /**
      * Runs the batch and executes the stored commands
      *
      * @param table   The class
      * @param bitmask Defines whether to run the update, insert or delete statements. Example: 1 | 1 << 1 | 1 << 2(0111) for all. The first bit defines updating, the second inserting and the last deleting
      */
     public void executeBatch(RegisteredTable table, int bitmask) {
         if ((bitmask & 1) != 0) {
             executeUpdateBatch(table);
         }
         if ((bitmask & 1 << 1) != 0) {
             executeInsertBatch(table);
         }
         if ((bitmask & 1 << 2) != 0) {
             executeDeleteBatch(table);
         }
     }
 
     public void executeUpdateBatch(RegisteredTable table) {
         accessLock.lock();
         try {
             PreparedQuery update = table.getPreparedUpdateStatement();
             update.executeBatches();
         } finally {
             accessLock.unlock();
         }
     }
 
     public void executeUpdateBatch(Class<? extends TableObject> table) {
         executeUpdateBatch(getRegisteredTable(table));
     }
 
     public void executeInsertBatch(RegisteredTable table) {
         accessLock.lock();
         try {
             PreparedQuery insert = table.getPreparedInsertStatement();
             insert.executeBatches();
         } finally {
             accessLock.unlock();
         }
     }
 
     public void executeInsertBatch(Class<? extends TableObject> table) {
         executeInsertBatch(getRegisteredTable(table));
     }
 
     public void executeDeleteBatch(RegisteredTable table) {
         accessLock.lock();
         try {
             PreparedQuery delete = table.getPreparedDeleteStatement();
             delete.executeBatches();
         } finally {
             accessLock.unlock();
         }
     }
 
     public void executeDeleteBatch(Class<? extends TableObject> table) {
         executeDeleteBatch(getRegisteredTable(table));
     }
 
     private int setColumnValues(PreparedQuery statement, RegisteredTable registeredTable, TableObject object, Column idColumn) {
         List<Column> registeredColumns = registeredTable.getRegisteredColumns();
         int i = 0;
         for (Column column : registeredColumns) {
             if (column.equals(idColumn)) {
                 continue;
             }
 
             Object value = column.getValue(object);
             statement.set(column, i, value);
             i++;
         }
 
         return i;
     }
 
     protected DatabaseConfiguration getConfiguration() {
         return configuration;
     }
 
     public final boolean isDropOldColumns() {
         return dropOldColumns;
     }
 
     public final void setDropOldColumns(boolean dropOldColumns) {
         this.dropOldColumns = dropOldColumns;
     }
 
     /**
      * Creates a new PreparedQuery which can be executed now or later.
      *
      * @param query The query to prepare
      * @return A PreparedQuery
      */
     public abstract PreparedQuery createPreparedStatement(String query);
 
     protected abstract <T extends TableObject> PreparedSelectQuery<T> createPreparedSelectQuery(String query, RegisteredTable table);
 
     public abstract boolean executeDirectUpdate(String query);
 
     public abstract boolean existsEntry(RegisteredTable table, TableObject object);
 
     public abstract boolean existsEntry(TableObject object);
 
     protected abstract long getLastEntryId(RegisteredTable table);
 
     /**
      * Checks whether the class is supported by this database/database engine
      *
      * @param type The type to check for
      * @return Whether the type is supported
      */
     public abstract boolean isSupported(Class<?> type);
 
     /**
      * Copies all entries of a table from this database to another database.
      * The is a 100% copy. If the a entry with the same id exists it gets updated.
      *
      * @param table The table
      * @param to    The destination
      * @param <T>   The type of the table
      */
     public <T extends TableObject> void copy(Class<T> table, Database to) {
         RegisteredTable registeredTable = to.getRegisteredTable(table);
         PreparedSelectQuery<T> prepare = this.<T>select().from(table).prepare();
         PreparedQuery statement = registeredTable.createFullInsertStatement(to);
 
         for (T entry : prepare.getResults()) {
 
             if (to.existsEntry(registeredTable, entry)) {
                 to.update(entry);
             } else {
                 int i = 0;
                 for (Column column : registeredTable.getRegisteredColumns()) {
                     statement.set(column, i, column.getValue(entry));
                     i++;
                 }
                 statement.update();
             }
         }
 
         prepare.close();
         statement.update();
     }
 
     public boolean isRegistered(Class<? extends TableObject> table) {
         try {
             getRegisteredTable(table);
             return true;
         } catch (RegistrationException e) {
             return false;
         }
     }
 
     public boolean isRegistered(RegisteredTable table) {
         return registeredTables.contains(table);
     }
 
     @Override
     public boolean equals(Object o) {
         if (this == o) return true;
         if (o == null || getClass() != o.getClass()) return false;
 
         Database database = (Database) o;
 
         return !(configuration != null ? !configuration.equals(database.configuration) : database.configuration != null);
 
     }
 
     @Override
     public int hashCode() {
         return configuration != null ? configuration.hashCode() : 0;
     }
 
     public abstract boolean testConnection();
 
     public abstract boolean isAutoReset();
 
     public boolean isAutoReconnect() {
         return getConfiguration() != null && getConfiguration().isAutoReconnect();
     }
 
     public abstract String getEngineName();
 
     public static Lock getAccessLock() {
         return accessLock;
     }
 
     public abstract void sendKeepAliveQuery();
 }
