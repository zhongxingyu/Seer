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
  *     William Chesters <williamc At paneris.org>
  *     http://paneris.org/~williamc
  *     Obrechtstraat 114, 2517VX Den Haag, The Netherlands
  */
 
 package org.melati.poem;
 
 import java.sql.Connection;
 import java.sql.DatabaseMetaData;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.util.Enumeration;
 import java.util.Hashtable;
 import java.util.Vector;
 
 import org.melati.poem.dbms.Dbms;
 import org.melati.poem.dbms.DbmsFactory;
 import org.melati.poem.transaction.Transaction;
 import org.melati.poem.transaction.TransactionPool;
 import org.melati.poem.util.ArrayEnumeration;
 import org.melati.poem.util.ArrayUtils;
 import org.melati.poem.util.FlattenedEnumeration;
 import org.melati.poem.util.MappedEnumeration;
 import org.melati.poem.util.StringUtils;
 
 import EDU.oswego.cs.dl.util.concurrent.ReadWriteLock;
 import EDU.oswego.cs.dl.util.concurrent.WriterPreferenceReadWriteLock;
 
 /**
  * An RDBMS database.  Don't instantiate (or subclass) this class, but rather
  * {@link PoemDatabase}, which includes the boilerplate code for the standard
  * tables such as <TT>user</TT> and <TT>columninfo</TT> which all POEM
  * databases must contain.  If the database is predefined by a Data Structure
  * Definition <TT><I>Bar</I>.dsd</TT>, there will be an application-specialised
  * subclass of <TT>PoemDatabase</TT> called <TT><I>Bar</I>Database</TT> which
  * provides named methods for accessing application-specialised objects
  * representing the predefined tables.
  *
  * @see PoemDatabase
  */
 
 public abstract class Database implements TransactionPool {
 
   final Database _this = this;
 
   private Vector transactions = null; 
   private Vector freeTransactions = null;
 
   private Connection committedConnection;
   private final ReadWriteLock lock = new WriterPreferenceReadWriteLock();
   private long structureSerial = 0L;
 
   private Vector tables = new Vector();
   private Hashtable tablesByName = new Hashtable();
   private Table[] displayTables = null;
 
   private String name;
   private String displayName;
   private Dbms dbms;
   private boolean logSQL = false;
   private boolean logCommits = false;
   private int transactionsMax;
 
   private String connectionUrl;
   
   /**
    * Used in testing to check caching.
    */
   private int queryCount = 0;
   /** Used in tests to check caching etc. */
   private String lastQuery = null;
   
 
   //
   // ================
   //  Initialisation
   // ================
   //
 
   /**
    * Don't subclass this, subclass <TT>PoemDatabase</TT>.
    * @see PoemDatabase
    */
 
   public Database() {
   }
 
   private boolean initialised = false;
 
   /**
    * Initialise each table.
    */
   private synchronized void init() {
     if (!initialised) {
       for (Enumeration t = this.tables.elements(); t.hasMoreElements();)
         ((Table)t.nextElement()).init();
       initialised = true;
     }
   }
 
   private final boolean[] connecting = new boolean[1];
 
  /**
   * Thrown when a request is made whilst the connection to
   * the underlying database is still in progress.
   */
   public class ConnectingException extends PoemException {
     private static final long serialVersionUID = 1L;
     /**
      * {@inheritDoc}
      */
     public String getMessage() {
       return "Connection to the database is currently in progress; " +
              "please try again in a moment";
     }
   }
 
   /**
    * Connect to an RDBMS database.  This should be called once when the
    * application starts up; it will
    *
    * <UL>
    *   <LI> Open <TT>this.transactionsMax()</TT> JDBC <TT>Connection</TT>s to
    *        the database for subsequent `pooling'
    *   </LI>
    *   <LI> Unify (reconcile) the structural information about the database
    *        given in
    *
    *        <OL>
    *          <LI> the Database Structure Definition (<I>i.e.</I> embodied in
    *               the boilerplate code generated from it), including the
    *               POEM-standard tables defined in <TT>Poem.dsd</TT>;
    *          <LI> the metadata tables <TT>tableinfo</TT> and
    *               <TT>columninfo</TT>;
    *          <LI> the actual JDBC metadata from the RDBMS.
    *        </OL>
    *
    *        Any tables or columns defined in the DSD or the metadata tables,
    *        but not present in the actual database, will be created.
    *        <BR>
    *        Conversely, entries will be created in the metadata tables for
    *        tables and columns that don't have them.  If an inconsistency is
    *        detected between any of the three information sources (such as a
    *        fundamental type incompatibility, or a string field which is
    *        narrower in the database than it was declared to be), an exception
    *        will be thrown.  In that case the database will in theory be left
    *        untouched, except that in Postgres (at least) all structural
    *        updates happen immediately and irrevocably even if made from a
    *        transaction subsequently rolled back.
    *   </LI>
    * </UL>
    *
    * @param dbmsclass   The Melati DBMS class (see org/melati/poem/dbms)
    *                    to use, usually specified in
    *                    org.melati.LogicalDatabase.properties.
    *
    * @param url         The JDBC URL for the database; for instance
    *                    <TT>jdbc:postgresql:williamc</TT>.  It is the
    *                    programmer's responsibility to make sure that an
    *                    appropriate driver has been loaded.
    *
    * @param username    The username under which to establish JDBC connections
    *                    to the database.  This has nothing to do with the
    *                    user/group/capability authentication performed by
    *                    Melati.
    *
    * @param password    The password to go with the username.
    *
    * @param transactionsMaxP
    *                    The maximum number of concurrent Transactions allowed,
    *                    usually specified in
    *                    org.melati.LogicalDatabase.properties.
    *
    * @see #transactionsMax()
    */
   public void connect(String name, String dbmsclass, String url,
                       String username, String password,
                       int transactionsMaxP) throws PoemException {
 
     this.name = name;
     this.connectionUrl = url;
 
     synchronized (connecting) {
       if (connecting[0])
         throw new ConnectingException();
       connecting[0] = true;
     }
 
     try {
       setDbms(DbmsFactory.getDbms(dbmsclass));
 
       if (committedConnection != null)
         throw new ReconnectionPoemException();
 
       setTransactionsMax(transactionsMaxP);
       committedConnection = getDbms().getConnection(url, username, password);
       transactions = new Vector();
       for (int s = 0; s < transactionsMax(); ++s)
         transactions.add(
           new PoemTransaction(
               this,
               getDbms().getConnection(url, username, password),
               s));
 
       freeTransactions = (Vector)transactions.clone();
 
       try {
         // Perform any table specific initialisation, none by default
         init();
 
         // Bootstrap: set up the tableinfo and columninfo tables
         DatabaseMetaData m = committedConnection.getMetaData();
         getTableInfoTable().unifyWithDB(
             m.getColumns(null, dbms.getSchema(),
                          dbms.unreservedName(getTableInfoTable().getName()), null));
         getColumnInfoTable().unifyWithDB(
             m.getColumns(null, dbms.getSchema(),
                          dbms.unreservedName(getColumnInfoTable().getName()), null));
         getTableCategoryTable().unifyWithDB(
             m.getColumns(null, dbms.getSchema(),
                          dbms.unreservedName(getTableCategoryTable().getName()), null));
 
         inSession(AccessToken.root,
                   new PoemTask() {
                     public void run() throws PoemException {
                       try {
                         _this.unifyWithDB();
                       }
                       catch (SQLException e) {
                         throw new SQLPoemException(e);
                       }
                     }
 
                     public String toString() {
                       return "Unifying with DB";
                     }
                   });
       }
       catch (SQLException e) {
         if (committedConnection != null) disconnect();
         throw new UnificationPoemException(e);
       }
     } 
     catch (SQLPoemException e) { 
       if (committedConnection != null) disconnect();
       throw e;
     }
     finally {
       synchronized (connecting) {
         connecting[0] = false;
       }
     }
   }
 
   /**
    * Releases database connections.
    */
   public void disconnect() throws PoemException {
     if (committedConnection == null)
       throw new ReconnectionPoemException();
 
     try {
       Enumeration iter = freeTransactions.elements();
       while (iter.hasMoreElements()) {
         PoemTransaction txn = (PoemTransaction)iter.nextElement();
         txn.getConnection().close();
       }
       freeTransactions.removeAllElements();
       
       getDbms().shutdown(committedConnection);
       committedConnection.close();
     } catch (SQLException e) {
       throw new SQLPoemException(e);
     }
     committedConnection = null;
   }
   
   /**
    * Don't call this.  Tables should be defined either in the DSD (in which
    * case the boilerplate code generated by the preprocessor will call this
    * method), or directly in the RDBMS (in which case the initialisation code
    * will), or using <TT>addTableAndCommit</TT>.
    *
    * @see #addTableAndCommit
    */
   protected synchronized void defineTable(Table table)
       throws DuplicateTableNamePoemException {
     if (tablesByName.get(table.getName().toLowerCase()) != null)
       throw new DuplicateTableNamePoemException(this, table.getName());
     redefineTable(table);
   }
 
   protected synchronized void redefineTable(Table table) {
     if (table.getDatabase() != this)
       throw new TableInUsePoemException(this, table);
 
     if (tablesByName.get(table.getName().toLowerCase()) == null) {
       tablesByName.put(table.getName().toLowerCase(), table);
       tables.addElement(table);
     }
     else
       tables.setElementAt(table,
                           tables.indexOf(
                               tablesByName.put(table.getName().toLowerCase(), table)));
     displayTables = null;
   }
 
   private ResultSet columnsMetadata(DatabaseMetaData m, String tableName)
       throws SQLException {
     return m.getColumns(null, dbms.getSchema(), dbms.unreservedName(tableName), null);
   }
 
   /**
    * Add a Table to this Databse and commit the Transaction.
    * @param info Table metadata object
    * @param troidName name of troidColumn
    * @return new minted {@link Table} 
    */
   public Table addTableAndCommit(TableInfo info, String troidName)
       throws PoemException {
 
     // For permission control we rely on them having successfully created a
     // TableInfo
 
     Table table = new Table(this, info.getName(),
                             DefinitionSource.infoTables);
     table.defineColumn(new ExtraColumn(table, troidName,
                                        TroidPoemType.it,
                                        DefinitionSource.infoTables,
                                        table.extrasIndex++));
     table.setTableInfo(info);
     table.unifyWithColumnInfo();
     table.unifyWithDB(null);
 
     PoemThread.commit();
     defineTable(table);
 
     return table;
   }
   
   /**
    * @param info the tableInfo for the table to delete
    */
   public void deleteTableAndCommit(TableInfo info) { 
     beginStructuralModification();
     try {
       Table table = info.actualTable();
       info.delete(); // Ensure we have no references in metadata
       table.dbModifyStructure(" DROP TABLE " + table.quotedName());
       synchronized (tables) {
         tables.remove(table);
         tablesByName.remove(table.getName().toLowerCase());
         displayTables = (Table[])ArrayUtils.removed(displayTables, table);
         uncache();
         table.invalidateTransactionStuffs();
       }
       PoemThread.commit();
     }
     finally {
       endStructuralModification();
     }
   }
 
   private synchronized void unifyWithDB() throws PoemException, SQLException {
     boolean debug = false;
     // Check all tables defined in the tableInfo metadata table
     // defining the ones that don't exist
 
     for (Enumeration ti = getTableInfoTable().selection();
          ti.hasMoreElements();) {
       TableInfo tableInfo = (TableInfo)ti.nextElement();
       Table table = (Table)tablesByName.get(tableInfo.getName().toLowerCase());
       if (table == null) {
         if (debug) log("Defining table:" + tableInfo.getName());
         table = new Table(this, tableInfo.getName(),
                           DefinitionSource.infoTables);
         defineTable(table);
       }
       table.setTableInfo(tableInfo);
     }
 
     // Conversely, add tableInfo for the tables that do not have an entry in tableInfo
 
     for (Enumeration t = tables.elements(); t.hasMoreElements();)
       ((Table)t.nextElement()).createTableInfo();
 
     // Check all tables against columnInfo
 
     for (Enumeration t = tables.elements(); t.hasMoreElements();)
       ((Table)t.nextElement()).unifyWithColumnInfo();
 
     // Finally, check tables against the actual JDBC metadata
 
     String[] normalTables = { "TABLE" };
 
     DatabaseMetaData m = committedConnection.getMetaData();
     ResultSet tableDescs = m.getTables(null, dbms.getSchema(), null,
                                        normalTables);
     while (tableDescs.next()) {
       if (debug) log("Table:" + tableDescs.getString("TABLE_NAME") +
                         " Type:" + tableDescs.getString("TABLE_TYPE"));
       String tableName = dbms.melatiName(tableDescs.getString("TABLE_NAME"));
       Table table = null;
       if (tableName != null) { //dbms returning grotty table name (MSAccess)
         table = (Table)tablesByName.get(tableName.toLowerCase());
         if (table == null) {  // We do not know about this table
           if (debug) log("Unknown to POEM, with JDBC name " + tableName);
 
           // but we only want to include them if they have a plausible troid:
           ResultSet idCol = m.getColumns(null, dbms.getSchema(), dbms.unreservedName(tableName), 
               dbms.getJdbcMetadataName(dbms.unreservedName("id")));
           if (idCol.next()) { 
             if (debug) log("Got an ID column for discovered jdbc table ");
             if (dbms.canRepresent(
                    defaultPoemTypeOfColumnMetaData(idCol), TroidPoemType.it) != null) {
               try {
                 table = new Table(this, tableName,
                                   DefinitionSource.sqlMetaData);
                 defineTable(table);
               }
               catch (DuplicateTableNamePoemException e) {
                 throw new UnexpectedExceptionPoemException(e);
               }
               table.createTableInfo();
             } else if (debug) log("Can represent failed");
           }  else if (debug) log("Table " + dbms.unreservedName(tableName) + 
                   " appears not to have a troid column called " + 
                   dbms.getJdbcMetadataName(dbms.unreservedName("id")));
           /*
           // Try to promote the primary key to a troid
           else {
             ResultSet pKeys = m.getPrimaryKeys(null, dbms.getSchema(), tableName);
             if (pKeys.next()) {
               String keyName = pKeys.getString("COLUMN_NAME");
               if (!pKeys.next()) {
                 ResultSet keyCol = m.getColumns(null, dbms.getSchema(),
                                                 tableName, keyName);
                 if (keyCol.next() &&
                   dbms.canRepresent(defaultPoemTypeOfColumnMetaData(keyCol),
                                     TroidPoemType.it) != null) {
                   if (debug) log("Got a unique primary key");
                   try {
                     defineTable(table = new Table(this, tableName,
                                                   DefinitionSource.sqlMetaData));
                   }
                   catch (DuplicateTableNamePoemException e) {
                     throw new UnexpectedExceptionPoemException(e);
                   }
                   table.createTableInfo();
                 }
               }
             }
           } */
         } else if (debug) log("table not null:" + tableName);
       }
 
       if (table != null) {
          if (debug) log("table not null now:" + tableName);
          if (debug) log("columnsMetadata(m, tableName):"
                             + columnsMetadata(m, tableName));
          // Create the table if it has no metadata
          // unify with it either way
         table.unifyWithDB(columnsMetadata(m, tableName));
       } else if (debug) log("table still null, probably doesn't have a troid:" + tableName);
 
     }
 
     // ... and create any that simply don't exist
 
     for (Enumeration t = tables.elements(); t.hasMoreElements();) {
       Table table = (Table)t.nextElement();
       // bit yukky using getColumns ...
       ResultSet colDescs = columnsMetadata(m,
                                dbms.unreservedName(table.getName()));
       if (!colDescs.next()) {
         // System.err.println("Table has no columns in dbms:" +
         //                    dbms.unreservedName(table.getName()));
         table.unifyWithDB(null);
       }
     }
 
     for (Enumeration t = tables.elements(); t.hasMoreElements();)
       ((Table)t.nextElement()).postInitialise();
     
   }
 
   /**
    * Add database constraints.
    * The only constraints POEM expects are uniqueness and nullability.
    * POEM assumes that the db will exploit indexes where present.
    * However if you wish to export the db to a more DB oriented 
    * application or wish to use schema interrogation or visualisation tools 
    * then constraints can be added.
    * Whether constraints are added is controlled in 
    * org.melati.LogicalDatabase.properties. 
    */
   public void addConstraints() {
     inSession(AccessToken.root,
         new PoemTask() {
           public void run() throws PoemException {
             PoemThread.commit();
             beginStructuralModification();
             try {
               for (Enumeration t = tables.elements(); t.hasMoreElements();)
                 ((Table)t.nextElement()).dbAddConstraints();
               PoemThread.commit();
             }
             finally {
               endStructuralModification();
             }
           }
 
           public String toString() {
             return "Adding constraints to DB";
           }
         });
   }
 
   //
   // ==============
   //  Transactions
   // ==============
   //
 
   /**
    * The number of transactions available for concurrent use on the database.
    * This is the number of JDBC <TT>Connection</TT>s opened when the database
    * was <TT>connect</TT>ed, this can be set via LogicalDatabase.properties,
    * but defaults to 8 if not set.
    * 
    * {@inheritDoc}
    * @see org.melati.poem.transaction.TransactionPool#transactionsMax()
    */
   public final int transactionsMax() {
     return transactionsMax;
   }
 
   /**
    * Set the maximum number of transactions.
    * Note that this does not resize the transaction pool 
    * so should be called before the db is connected to.
    * 
    * {@inheritDoc}
    * @see org.melati.poem.transaction.TransactionPool#setTransactionsMax(int)
    */
   public final void setTransactionsMax(int t) {
     transactionsMax = t;
   }
 
   /**
    * {@inheritDoc}
    * @see org.melati.poem.transaction.TransactionPool#getTransactionsCount()
    */
   public int getTransactionsCount() {
     return transactions.size();
   }
 
   /**
    * {@inheritDoc}
    * @see org.melati.poem.transaction.TransactionPool#getFreeTransactionsCount()
    */
   public int getFreeTransactionsCount() {
     return freeTransactions.size();
   }
 
   //
   // ----------------------------------
   //  Keeping track of the Transactions
   // ----------------------------------
   //
 
   /**
    * Get a transaction for exclusive use.  It's simply taken off the freelist,
    * to be put back later.
    */
   private PoemTransaction openTransaction() {
     synchronized (freeTransactions) {
       if (freeTransactions.size() == 0)
        throw new NoMoreTransactionsException("Database " + name + " has no free transactions and " 
             + transactions.size() + " transactions.");
       PoemTransaction transaction =
           (PoemTransaction)freeTransactions.lastElement();
       freeTransactions.setSize(freeTransactions.size() - 1);
       return transaction; }
   }
 
   /**
    * Finish using a transaction, put it back on the freelist.
    */
   void notifyClosed(PoemTransaction transaction) {
     freeTransactions.addElement(transaction);
   }
 
   /**
    * Find a transaction by its index.
    * <p>
    * transaction(i).index() == i
    * @param index the index of the Transaction to return
    * @return the Transactoion with that index
    */
   public PoemTransaction poemTransaction(int index) {
     return (PoemTransaction)transactions.elementAt(index);
   }
 
   /**
    * {@inheritDoc}
    * @see org.melati.poem.transaction.TransactionPool#transaction(int)
    */
   public final Transaction transaction(int index) {
     return poemTransaction(index);
   }
 
   /**
    * @param trans a PoemTransaction
    * @return whether the Transaction is free
    */
   public boolean isFree(PoemTransaction trans) {
     return freeTransactions.contains(trans);
   }
 
   /**
    * Aquire a lock on the database.
    */
   public void beginExclusiveLock() {
     // FIXME yuk
 
     if (PoemThread.inSession())
       lock.readLock().release();
 
     try {
       lock.writeLock().acquire();
     }
     catch (InterruptedException e) {
       throw new InterruptedPoemException(e);
     }
   }
 
   /**
    * Release lock.
    */
   public void endExclusiveLock() {
     lock.writeLock().release();
 
     // FIXME yuk, see above
 
     if (PoemThread.inSession())
       try {
         lock.readLock().acquire();
       }
       catch (InterruptedException e) {
         throw new InterruptedPoemException(e);
       }
   }
 
   //
   // ---------------
   //  Starting them
   // ---------------
   //
 
   /**
    * Perform a PoemTask.
    * @param accessToken the AccessToken to run the task under
    * @param task the PoemTask to perform
    * @param useCommittedTransaction whether to use an insulated Transaction or the Committed one
    */
   private void perform(AccessToken accessToken, final PoemTask task,
                        boolean useCommittedTransaction) throws PoemException {
     try {
       lock.readLock().acquire();
     }
     catch (InterruptedException e) {
       throw new InterruptedPoemException(e);
     }
 
     final PoemTransaction transaction =
         useCommittedTransaction ? null : openTransaction();
     try {
       PoemThread.inSession(new PoemTask() {
                              public void run() throws PoemException {
                                  task.run();
                                  if (transaction != null) 
                                    transaction.close(true);
                              }
 
                              public String toString() {
                                return task.toString();
                              }
                            },
                            accessToken,
                            transaction);
     }
     finally {
       try {
         if (transaction != null && !isFree(transaction)) {
           transaction.close(false);
         }
       } finally {
 
         lock.readLock().release();
       }
     }
   }
 
   /**
    * Perform a task with the database.  Every access to a POEM database must be
    * made in the context of a `transaction' established using this method (note
    * that Melati programmers don't have to worry about this, because the
    * <TT>PoemServlet</TT> will have done this by the time they get control).
    *
    * @param accessToken    A token determining the <TT>Capability</TT>s
    *                       available to the task, which in turn determine
    *                       what data it can attempt to read and write
    *                       without triggering an
    *                       <TT>AccessPoemException</TT>.  Note that a
    *                       <TT>User</TT> can be an <TT>AccessToken</TT>.
    *
    * @param task           What to do: its <TT>run()</TT> is invoked, in
    *                       the current Java thread; until <TT>run()</TT>
    *                       returns, all POEM accesses made by the thread
    *                       are taken to be performed with the capabilities
    *                       given by <TT>accessToken</TT>, and in a private
    *                       transaction.  No changes made to the database
    *                       by other transactions will be visible to it (in the
    *                       sense that once it has seen a particular
    *                       version of a record, it will always
    *                       subsequently see the same one), and its own
    *                       changes will not be made permanent until it
    *                       completes successfully or performs an explicit
    *                       <TT>PoemThread.commit()</TT>.  If it terminates
    *                       with an exception or issues a
    *                       <TT>PoemThread.rollback()</TT> its changes will
    *                       be lost.  (The task is allowed to continue
    *                       after either a <TT>commit()</TT> or a
    *                       <TT>rollback()</TT>.)
    *
    * @see PoemThread
    * @see PoemThread#commit
    * @see PoemThread#rollback
    * @see User
    */
   public void inSession(AccessToken accessToken, PoemTask task) {
     perform(accessToken, task, false);
   }
 
   /**
    * Start a db session.
    * This is the very manual way of doing db work - not reccomended -
    * use inSession.
    */
   public void beginSession(AccessToken accessToken) {
     try {
       lock.readLock().acquire();
     }
     catch (InterruptedException e) {
       throw new InterruptedPoemException(e);
     }
     PoemTransaction transaction = openTransaction();
     try { 
       PoemThread.beginSession(accessToken,transaction);
     } catch (AlreadyInSessionPoemException e) { 
       notifyClosed(transaction);
       lock.readLock().release();
       throw e;
     }
   }
 
   /**
    * End a db session.
    * <p>
    * This is the very manual way of doing db work - not recommended -
    * use inSession.
    */
   public void endSession() {
     PoemTransaction tx = PoemThread.sessionToken().transaction;
     PoemThread.endSession();
     tx.close(true);
     lock.readLock().release();
 
   }
 
   /**
    * Perform a task with the database, but not in an insulated transaction.
    * The effect is the same as <TT>inSession</TT>, except that the task will
    * see changes to the database made by other transactions as they are
    * committed, and it is not allowed to make any changes of its own.
    * <p>
    * A modification will trigger a <code>WriteCommittedException</code>, 
    * however a create operation will trigger a NullPointerException, 
    * as we have no Transaction.
    * </p>
    * <p>
    * Not recommended; why exactly do you want to sidestep the Transaction handling?
    * </p>
    * @see #inSession
    */
   public void inCommittedTransaction(AccessToken accessToken, PoemTask task) {
     perform(accessToken, task, true);
   }
 
   //
   // ==================
   //  Accessing tables
   // ==================
   //
 
   /**
    * Retrieve the table with a given name.
    *
    * @param name        The name of the table to return, as in the RDBMS
    *                    database.  It's case-sensitive, and some RDBMSs such as
    *                    Postgres 6.4.2 (and perhaps other versions) treat upper
    *                    case letters in identifiers inconsistently, so the 
    *                    name is forced to lowercase.
    *
    * @return the Table of that name 
    *
    * @exception NoSuchTablePoemException
    *             if no table with the given name exists in the RDBMS
    */
   public final Table getTable(String name) throws NoSuchTablePoemException {
     Table table = (Table)tablesByName.get(name.toLowerCase());
     if (table == null) throw new NoSuchTablePoemException(this, name);
     return table;
   }
 
   /**
    * All the tables in the database.
    * NOTE This will include any deleted tables
    * 
    * @return an <TT>Enumeration</TT> of <TT>Table</TT>s, in no particular
    *         order.
    */
   public final Enumeration tables() {
      
     return tables.elements();
   }
 
   /**
    * Currently all the tables in the database in DisplayOrder
    * order.
    *
    * @return an <TT>Enumeration</TT> of <TT>Table</TT>s
    */
   public Enumeration getDisplayTables() {
     Table[] displayTablesL = this.displayTables;
 
     if (displayTablesL == null) {
       Enumeration tableIDs = getTableInfoTable().troidSelection(
         (String)null /* "displayable" */,
         quotedName("displayorder") + ", " + quotedName("name"),
         false, null);
 
       Vector them = new Vector();
       while (tableIDs.hasMoreElements()) {
         Table table =
             tableWithTableInfoID(((Integer)tableIDs.nextElement()).intValue());
         if (table != null)
           them.addElement(table);
       }
 
       displayTablesL = new Table[them.size()];
       them.copyInto(displayTablesL);
       this.displayTables = displayTablesL;
     }
 
     return new ArrayEnumeration(this.displayTables);
   }
 
   /**
    * The table with a given ID in the <TT>tableinfo</TT> table, or
    * <TT>null</TT>.
    *
    * @see #getTableInfoTable
    */
   Table tableWithTableInfoID(int tableInfoID) {
     for (Enumeration t = tables.elements(); t.hasMoreElements();) {
       Table table = (Table)t.nextElement();
       Integer id = table.tableInfoID();
       if (id != null && id.intValue() == tableInfoID)
         return table;
     }
 
     return null;
   }
 
  /**
   * @return All the {@link Column}s in the whole {@link Database}
   */
   public Enumeration columns() {
     return new FlattenedEnumeration(
         new MappedEnumeration(tables()) {
           public Object mapped(Object table) {
             return ((Table)table).columns();
           }
         });
   }
    
   /**    
    * @param columnInfoID   
    * @return the Column with the given troid   
    */  
   Column columnWithColumnInfoID(int columnInfoID) {  
     for (Enumeration t = tables.elements(); t.hasMoreElements();) {  
       Column column =  
           ((Table)t.nextElement()).columnWithColumnInfoID(columnInfoID);   
       if (column != null)  
         return column;   
     }  
     return null;   
   }  
 
   /**
    * @return The metadata table with information about all tables in the database.
    */
   public abstract TableInfoTable getTableInfoTable();
 
   /**
    * @return The Table Category Table.
    */
   public abstract TableCategoryTable getTableCategoryTable();
 
   /**
    * @return The metadata table with information about all columns in all tables in the
    * database.
    */
   public abstract ColumnInfoTable getColumnInfoTable();
 
   /**
    * The table of capabilities (required for reading and/or writing records)
    * defined for the database.  Users acquire capabilities in virtue of being
    * members of groups.
    *
    * @return the CapabilityTable
    * @see Persistent#getCanRead
    * @see Persistent#getCanWrite
    * @see Persistent#getCanDelete
    * @see Table#getDefaultCanRead
    * @see Table#getDefaultCanWrite
    * @see User
    * @see #getUserTable
    * @see Group
    * @see #getGroupTable
    */
   public abstract CapabilityTable getCapabilityTable();
 
   /**
    * @return the table of known users of the database
    */
   public abstract UserTable getUserTable();
 
   /**
    * @return the table of defined user groups for the database
    */
   public abstract GroupTable getGroupTable();
 
   /**
    * A user is a member of a group iff there is a record in this table to say so.
    * @return the table containing group-membership records
    */
   public abstract GroupMembershipTable getGroupMembershipTable();
 
   /**
    * The table containing group-capability records.  A group has a certain
    * capability iff there is a record in this table to say so.
    * @return the GroupCapability table 
    */
   public abstract GroupCapabilityTable getGroupCapabilityTable();
 
   /**
    * @return the Setting Table.
    */
   public abstract SettingTable getSettingTable();
 
   //
   // ========================
   //  Running arbitrary SQL
   // ========================
   //
 
   /**
    * Run an arbitrary SQL query against the database.  This is a low-level
    * <TT>java.sql.Statement.executeQuery</TT>, intended for fiddly queries for
    * which the higher-level methods are too clunky or inflexible.  <B>Note</B>
    * that it bypasses the access control mechanism!
    *
    * @return the ResultSet resulting from running the query
    * @see Table#selection()
    * @see Table#selection(java.lang.String)
    * @see Column#selectionWhereEq(java.lang.Object)
    */
   public ResultSet sqlQuery(String sql) throws SQLPoemException {
     SessionToken token = PoemThread.sessionToken();
     token.transaction.writeDown();
     try {
       Statement s = token.transaction.getConnection().createStatement();
       token.toTidy().add(s);
       ResultSet rs = s.executeQuery(sql);
       token.toTidy().add(rs);
       if (logSQL())
         log(new SQLLogEvent(sql));
       incrementQueryCount(sql);
       return rs;
     }
     catch (SQLException e) {
       throw new ExecutingSQLPoemException(sql, e);
     }
   }
 
   /**
    * Run an arbitrary SQL update against the database.  This is a low-level
    * <TT>java.sql.Statement.executeUpdate</TT>, intended for fiddly updates for
    * which the higher-level methods are too clunky or inflexible.
    * <p>  
    * NOTE This bypasses the access control mechanism.  Furthermore, the cache
    * will be left out of sync with the database and must be cleared out
    * (explicitly, manually) after the current transaction has been committed
    * or completed.
    *
    * @return either the row count for <code>INSERT</code>, <code>UPDATE</code>
    * or <code>DELETE</code> statements, or <code>0</code> for SQL statements 
    * that return nothing
    * 
    * @see Table#selection()
    * @see Table#selection(java.lang.String)
    * @see Column#selectionWhereEq(java.lang.Object)
    * @see #uncacheContents
    */
   public int sqlUpdate(String sql) throws SQLPoemException {
     SessionToken token = PoemThread.sessionToken();
     token.transaction.writeDown();
 
     try {
       Statement s = token.transaction.getConnection().createStatement();
       token.toTidy().add(s);
       int n = s.executeUpdate(sql);
       if (logSQL())
         log(new SQLLogEvent(sql));
       incrementQueryCount(sql);
       return n;
     }
     catch (SQLException e) {
       throw dbms.exceptionForUpdate(null, sql,
                                     sql.indexOf("INSERT") >= 0 ||
                                       sql.indexOf("insert") >= 0,
                                     e);
     }
   }
 
   //
   // =======
   //  Users
   // =======
   //
 
   private User guest = null;
   /**
    * @return the guest
    */
   public User guestUser() {
     if (guest == null)
       guest = getUserTable().guestUser();
     return guest;
   }
 
   private User administrator = null; 
   /**
    * @return the administrator
    */
   public User administratorUser() {
     if (administrator == null)
       administrator = getUserTable().administratorUser();
     return administrator;
   }
 
   
   /**
    * Get the raw SQL statement for this database's DBMS for Capability 
    * check for a User.
    * @param user
    * @param capability
    * @return the raw SQL appropriate for this db
    */
   public String givesCapabilitySQL(User user, Capability capability) {
     // NOTE Bootstrapping to troid or we get a stack overflow 
     return dbms.givesCapabilitySQL(user.troid(), capability.troid().toString());
   }
 
  /**
   * @todo Use a prepared statement to get Capabilities
   */
   private boolean dbGivesCapability(User user, Capability capability) {
 
     String sql = givesCapabilitySQL(user, capability);
     ResultSet rs = null;
     try {
       rs = sqlQuery(sql);
       return rs.next();
     }
     catch (SQLPoemException e) {
       throw new UnexpectedExceptionPoemException(e);
     }
     catch (SQLException e) {
       throw new SQLSeriousPoemException(e, sql);
     }
     finally {
       try { if (rs != null) rs.close(); } catch (Exception e) {
         System.err.println("Cannot close resultset after exception.");
       }
     }
   }
 
   private class UserCapabilityCache {
     private Hashtable userCapabilities = null;
     private long groupMembershipSerial;
     private long groupCapabilitySerial;
 
     boolean hasCapability(User user, Capability capability) {
       PoemTransaction transaction = PoemThread.transaction();
       long currentGroupMembershipSerial =
           getGroupMembershipTable().serial(transaction);
       long currentGroupCapabilitySerial =
           getGroupCapabilityTable().serial(transaction);
 
       if (userCapabilities == null ||
           groupMembershipSerial != currentGroupMembershipSerial ||
           groupCapabilitySerial != currentGroupCapabilitySerial) {
         userCapabilities = new Hashtable();
         groupMembershipSerial = currentGroupMembershipSerial;
         groupCapabilitySerial = currentGroupCapabilitySerial;
       }
 
       Long pair = new Long(
           (user.troid().longValue() << 32) | (capability.troid().longValue()));
       Boolean known = (Boolean)userCapabilities.get(pair);
 
       if (known != null)
         return known.booleanValue();
       else {
         boolean does = dbGivesCapability(user, capability);
         userCapabilities.put(pair, does ? Boolean.TRUE : Boolean.FALSE);
         return does;
       }
     }
   }
 
   private UserCapabilityCache capabilityCache = new UserCapabilityCache();
 
   /**
    * Check if a user has the specified Capability.
    * @param user the User to check
    * @param capability the Capability required
    * @return whether User has Capability
    */
   public boolean hasCapability(User user, Capability capability) {
     // no capability means that we always have access
     if (capability == null) return true;
     // otherwise, go to the cache
     return capabilityCache.hasCapability(user, capability);
   }
 
   /**
    * @return the guest token.
    */
   public AccessToken guestAccessToken() {
     return getUserTable().guestUser();
   }
 
   private Capability canAdminister = null;
   
   /**
    * @return the Capability required to administer this db.
    */
   public Capability administerCapability() {
     return getCapabilityTable().administer();
   }
 
  
   /**
    * By default, anyone can administer a database.
    *
    * @return the required {@link Capability} to administer the db
    * (<tt>null</tt> unless overridden)
    * @see org.melati.admin.Admin
    */
   public Capability getCanAdminister() {
     return canAdminister;
   }
   
   /**
    * Set administrator capability to default.
    * <p>
    * NOTE Once a database has had its <tt>canAdminister</tt> capability set 
    * there is no mechanism to set it back to null. 
    */
   public void setCanAdminister() {
     canAdminister = administerCapability();
   }
   /**
    * Set administrator capability to named Capability.
    * @param capabilityName name of Capability
    */
   public void setCanAdminister(String capabilityName) {
     canAdminister = getCapabilityTable().ensure(capabilityName);
   }
 
   //
   // ==========
   //  Cacheing
   // ==========
   //
 
   /**
    * Trim POEM's cache to a given size.
    *
    * @param maxSize     The data for all but this many records per table will
    *                    be dropped from POEM's cache, on a least-recently-used
    *                    basis.
    */
   public void trimCache(int maxSize) {
     for (Enumeration t = tables.elements(); t.hasMoreElements();)
       ((Table)t.nextElement()).trimCache(maxSize);
   }
 
   /**
    * Set the contents of the cache to empty.
    */
   public void uncache() {
     for (int t = 0; t < tables.size(); ++t)
       ((Table)tables.elementAt(t)).uncache();
   }
 
   //
   // ===========
   //  Utilities
   // ===========
   //
 
   /**
    * Find all references to specified object in all tables. 
    * 
    * @param object the object being referred to 
    * @return an Enumeration of {@link Persistent}s
    */
   public Enumeration referencesTo(final Persistent object) {
     return new FlattenedEnumeration(
         new MappedEnumeration(tables()) {
           public Object mapped(Object table) {
             return ((Table)table).referencesTo(object);
           }
         });
   }
 
   /**
    * @return An Enumeration of Columns referring to the specified Table. 
    */
   public Enumeration referencesTo(final Table tableIn) {
     return new FlattenedEnumeration(
         new MappedEnumeration(tables()) {
           public Object mapped(Object table) {
             return ((Table)table).referencesTo(tableIn);
           }
         });
   }
 
   /**
    * Print some diagnostic information about the contents and consistency of
    * POEM's cache to stderr.
    */
   public void dumpCacheAnalysis() {
     for (Enumeration t = tables.elements(); t.hasMoreElements();)
       ((Table)t.nextElement()).dumpCacheAnalysis();
   }
 
   /**
    * Print information about the structure of the database to stdout.
    */
   public void dump() {
     for (int t = 0; t < tables.size(); ++t) {
       System.out.println();
       ((Table)tables.elementAt(t)).dump();
     }
 
     System.err.println("there are " + getTransactionsCount() + " transactions " +
                        "of which " + getFreeTransactionsCount() + " are free");
   }
 
   //
   // =========================
   //  Database-specific stuff
   // =========================
   //
 
   /**
    * @return the Database Management System class of this db
    */
   public Dbms getDbms() {
       return dbms;
   }
 
   /**
    * Set the DBMS.
    * 
    * @param aDbms
    */
   private void setDbms(Dbms aDbms) {
       dbms = aDbms;
   }
 
   /**
    * Quote a name in the DBMS' specific dialect.
    * @param name
    * @return name quoted.
    */
   public final String quotedName(String name) {
       return getDbms().getQuotedName(name);
   }
 
   /**
    * The default {@link PoemType} corresponding to a ResultSet of JDBC metadata.
    *  
    * @param md the JDBC metadata
    * @return The appropriatePoemType
    */
   final SQLPoemType defaultPoemTypeOfColumnMetaData(ResultSet md)
       throws SQLException {
     return getDbms().defaultPoemTypeOfColumnMetaData(md);
   }
 
   //
   // =====================
   //  Technical utilities
   // =====================
   //
 
 
   /**
    * Returns the connection url.
    * If you want a simple name see LogicalDatabase.
    * {@inheritDoc}
    * @see java.lang.Object#toString()
    */
   public String toString() {
     if (connectionUrl == null)
       return "unconnected database";
     else
       return connectionUrl;
   }
 
   /**
    * @return the non-transactioned jdbc Connection
    */
   public Connection getCommittedConnection() {
     return committedConnection;
   }
 
   /**
    * @return whether logging is switched on
    */
   public boolean logSQL() {
     return logSQL;
   }
 
   /**
    * Toggle logging.
    */
   public void setLogSQL(boolean value) {
     logSQL = value;
   }
 
   /**
    * @return whether database commits should be logged
    */
   public boolean logCommits() {
     return logCommits;
   }
 
   /**
    * Toggle commit logging.
    */
   public void setLogCommits(boolean value) {
     logCommits = value;
   }
 
   void log(PoemLogEvent e) {
     System.err.println("---\n" + e.toString());
   }
   void log(String s) {
     System.err.println(s);
   }
 
   void beginStructuralModification() {
     beginExclusiveLock();
   }
 
   /**
    * Uncache, increment serial and release exclusive lock.
    */
   void endStructuralModification() {
     for (int t = 0; t < tables.size(); ++t)
       ((Table)tables.elementAt(t)).uncache();
     ++structureSerial;
     endExclusiveLock();
   }
 
   /**
    * @return an id incremented for each change
    */
   long structureSerial() {
     return structureSerial;
   }
 
   /**
    * Used in testing to check if the cache is being used 
    * or a new query is being issued. 
    * @return Returns the queryCount.
    */
   public int getQueryCount() {
     return queryCount;
   }
   /**
    * Increment query count.
    */
   public void incrementQueryCount(String sql) {
     lastQuery = sql;
     queryCount++; 
   }
 
   /**
    * @return the most recent query
    */
   public String getLastQuery() { 
     return lastQuery;
   }
   /**
    * @return the name
    */
   public String getName() {
     return name;
   }
 
   /**
    * @return the displayName
    */
   public String getDisplayName() {
     if (displayName == null) 
       return StringUtils.capitalised(getName());
     return displayName;
   }
 
   /**
    * @param displayName the displayName to set
    */
   public void setDisplayName(String displayName) {
     this.displayName = displayName;
   }
 
 }
 
