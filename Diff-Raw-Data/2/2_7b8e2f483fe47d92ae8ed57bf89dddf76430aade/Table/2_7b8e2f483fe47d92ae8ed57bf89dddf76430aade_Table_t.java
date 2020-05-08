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
 
 import java.util.*;
 import java.sql.*;
 import org.melati.util.*;
 import org.melati.poem.dbms.*;
 
 public class Table {
 
   public static final int CACHE_LIMIT_DEFAULT = 100;
 
   private Table _this = this;
 
   private Database database;
   private String name;
   private String quotedName;
   private DefinitionSource definitionSource;
 
   TableInfo info = null;
 
   private TableListener[] listeners = {};
 
   private Column[] columns = {};
   private Hashtable columnsByName = new Hashtable();
 
   private Column troidColumn = null;
   private Column deletedColumn = null;
   private Column canReadColumn = null;
   private Column canWriteColumn = null;
   private Column canDeleteColumn = null;
   private Column displayColumn = null;
   private Column searchColumn = null;
 
   private String defaultOrderByClause = null;
 
   private Column[][] displayColumns = new Column[DisplayLevel.count()][];
   private Column[] searchColumns = null;
 
   private TransactionedSerial serial;
 
   private CachedSelection allTroids = null;
   private Hashtable cachedSelections = new Hashtable();
   private Hashtable cachedCounts = new Hashtable();
   private Hashtable cachedExists = new Hashtable();
 
   public Table(Database database, String name,
                DefinitionSource definitionSource) {
     this.database = database;
     this.name = name;
     // Don't do this here as the database does not know about the dbms yet
     // this.quotedName = database.quotedName(name);
     this.definitionSource = definitionSource;
     serial = new TransactionedSerial(database);
   }
 
   protected void postInitialise() {
     clearColumnInfoCaches();
     database.getColumnInfoTable().addListener(
         new TableListener() {
           public void notifyTouched(PoemTransaction transaction, Table table,
                                     Persistent persistent) {
             _this.notifyColumnInfo((ColumnInfo)persistent);
           }
 
           public void notifyUncached(Table table) {
             _this.clearColumnInfoCaches();
           }
         });
   }
 
   // 
   // ===========
   //  Accessors
   // ===========
   // 
 
   /**
    * The database to which the table is attached.
    */
 
   public final Database getDatabase() {
     return database;
   }
 
   /**
    * The table's programmatic name.  Identical with its name in the DSD (if the
    * table was defined there), in its <TT>tableinfo</TT> entry, and in the
    * RDBMS itself.
    */
 
   public final String getName() {
     return name;
   }
 
   final String quotedName() {
     if (quotedName == null) quotedName = database.quotedName(name);
     return quotedName;
   }
 
   /**
    * The human-readable name of the table.  POEM itself doesn't use this, but
    * it's available to applications and Melati's generic admin system as a
    * default label for the table and caption for its records.
    */
 
   public final String getDisplayName() {
     return info.getDisplayname();
   }
 
   /**
    * A brief description of the table's function.  POEM itself doesn't use
    * this, but it's available to applications and Melati's generic admin system
    * as a default label for the table and caption for its records.
    */
 
   public final String getDescription() {
     return info.getDescription();
   }
 
   /**
    * The category of this table.  POEM itself doesn't use
    * this, but it's available to applications and Melati's generic admin system
    * as a default label for the table and caption for its records.
    */
 
   public final TableCategory getCategory() {
      return info.getCategory();
   }
 
   /**
    * FIXME not sure we really want people using this directly?
    */
 
   public final TableInfo getInfo() {
      return info;
   }
 
   /**
    * The troid (<TT>id</TT>) of the table's entry in the <TT>tableinfo</TT>
    * table.  It will always have one (except during initialisation, which the
    * application programmer will never see).
    */
 
   public final Integer tableInfoID() {
     return info == null ? null : info.troid();
   }
 
   /**
    * The table's column with a given name.  If the table is defined in the DSD
    * under the name <TT><I>foo</I></TT>, there will be an
    * application-specialised <TT>Table</TT> subclass, called
    * <TT><I>Foo</I>Table</TT> (and available as <TT>get<I>Foo</I>Table</TT>
    * from the application-specialised <TT>Database</TT> subclass) which has
    * extra named methods for accessing the table's predefined <TT>Column</TT>s.
    *
    * @exception NoSuchColumnPoemException if there is no column with that name
    */
 
   public final Column getColumn(String name) throws NoSuchColumnPoemException {
     Column column = (Column)columnsByName.get(name);
     if (column == null)
       throw new NoSuchColumnPoemException(this, name);
     else
       return column;
   }
 
   /**
    * All the table's columns.
    *
    * @return an <TT>Enumeration</TT> of <TT>Column</TT>s
    * @see Column
    */
 
   public final Enumeration columns() {
     return new ArrayEnumeration(columns);
   }
 
   public final int getColumnsCount() {
     return columns.length;
   }
 
   Column columnWithColumnInfoID(int columnInfoID) {
     for (Enumeration c = columns(); c.hasMoreElements();) {
       Column column = (Column)c.nextElement();
       Integer id = column.columnInfoID();
       if (id != null && id.intValue() == columnInfoID)
         return column;
     }
 
     return null;
   }
 
   /**
    * The table's troid column.  Every table in a POEM database must have a
    * troid (table row ID, or table-unique non-nullable integer primary key),
    * often but not necessarily called <TT>id</TT>, so that it can be
    * conveniently `named'.
    *
    * @see #getObject(java.lang.Integer)
    */
 
   public final Column troidColumn() {
     return troidColumn;
   }
 
   /**
    * The table's deleted-flag column, if any.  FIXME.
    */
 
   public final Column deletedColumn() {
     return deletedColumn;
   }
 
   /**
    * The table's primary display column, if any.  This is the column used to
    * represent records from the table concisely in reports or whatever.  It is
    * determined at initialisation time by examining the <TT>Column</TT>s
    * <TT>getPrimaryDisplay()</TT> flags.
    *
    * @return the table's display column, or <TT>null</TT> if it hasn't got one
    *
   * @see Column#setColumnInfo()
    * @see ReferencePoemType#_stringOfCooked
    */
 
   public final Column displayColumn() {
     return displayColumn == null ? troidColumn : displayColumn;
   }
 
   final void setDisplayColumn(Column column) {
     displayColumn = column;
   }
 
   /**
   * in a similar manner to the primary display column, each table can have a 
   * primary criterion column
   */
 
   public final Column primaryCriterionColumn() {
     return searchColumn;
   }
 
   void setSearchColumn(Column column) {
     searchColumn = column;
   }
 
   String defaultOrderByClause() {
     String clause = defaultOrderByClause;
 
     if (clause == null) {
       clause = EnumUtils.concatenated(
           ", ",
           new MappedEnumeration(new ArrayEnumeration(SortUtils.sorted(
               new Order() {
                 public boolean lessOrEqual(Object a, Object b) {
                   return
                       ((Column)a).getDisplayOrderPriority().intValue() <=
                       ((Column)b).getDisplayOrderPriority().intValue();
                 }
               },
               new FilteredEnumeration(columns()) {
                 public boolean isIncluded(Object column) {
                   return ((Column)column).getDisplayOrderPriority() != null;
                 }
               }))) {
             public Object mapped(Object column) {
               String sort = ((Column)column).quotedName();
               if (((Column)column).getSortDescending()) sort += " desc";
               return sort;
             }
           });
 
       if (clause.equals(""))
         clause = displayColumn().quotedName();
 
       defaultOrderByClause = clause;
     }
 
     return clause;
   }
 
   private void clearColumnInfoCaches() {
     defaultOrderByClause = null;
     for (int i = 0; i < displayColumns.length; ++i)
       displayColumns[i] = null;
   }
 
   void notifyColumnInfo(ColumnInfo info) {
     // FIXME info == null means deleted: effect is too broad really
     if (info == null || info.getTableinfo_unsafe().equals(tableInfoID()))
       clearColumnInfoCaches();
   }
 
   private Column[] columnsWhere(String whereClause) {
     // get the col IDs from the committed session
 
     Enumeration colIDs =
         getDatabase().getColumnInfoTable().troidSelection(
             "tableinfo = " + tableInfoID() + " AND (" + whereClause + ")",
             null, false, null);
 
     Vector them = new Vector();
     while (colIDs.hasMoreElements()) {
       Column column =
           columnWithColumnInfoID(((Integer)colIDs.nextElement()).intValue());
       // null shouldn't happen but let's not gratuitously fail if it does
       if (column != null)
         them.addElement(column);
     }
 
     Column[] columns = new Column[them.size()];
     them.copyInto(columns);
     return columns;
   }
 
   public final Enumeration displayColumns(DisplayLevel level) {
     Column[] columns = displayColumns[level.index.intValue()];
 
     if (columns == null)
       displayColumns[level.index.intValue()] = columns =
 	  columnsWhere("displaylevel <= " + level.index);
 
     return new ArrayEnumeration(columns);
   }
 
   public final int displayColumnsCount(DisplayLevel level) {
     int l = level.index.intValue();
     if (displayColumns[l] == null)
       // FIXME race
       displayColumns(level);
 
     return displayColumns[l].length;
   }
 
   public final Enumeration getDetailDisplayColumns() {
     return displayColumns(DisplayLevel.detail);
   }
 
   public final int getDetailDisplayColumnsCount() {
     return displayColumnsCount(DisplayLevel.detail);
   }
 
   /**
    * The table's columns designated for display in a record, in display order.
    *
    * @return an <TT>Enumeration</TT> of <TT>Column</TT>s
    * @see Column
    */
 
   public final Enumeration getRecordDisplayColumns() {
     return displayColumns(DisplayLevel.record);
   }
 
   public final int getRecordDisplayColumnsCount() {
     return displayColumnsCount(DisplayLevel.record);
   }
 
   /**
    * The table's columns designated for display in a record summary, in display
    * order.
    *
    * @return an <TT>Enumeration</TT> of <TT>Column</TT>s
    * @see Column
    */
 
   public final Enumeration getSummaryDisplayColumns() {
     return displayColumns(DisplayLevel.summary);
   }
   
   /**
    * The table's columns designated for use as search criteria, in display
    * order.
    *
    * @return an <TT>Enumeration</TT> of <TT>Column</TT>s
    * @see Column
    */
 
   public final Enumeration getSearchCriterionColumns() {
     Column[] columns = searchColumns;
 
     if (columns == null)
       searchColumns = columns = columnsWhere("searchability <= " +
 					     Searchability.yes.index);
 
     return new ArrayEnumeration(searchColumns);
   }
 
   public final int getSearchCriterionColumnsCount() {
     if (searchColumns == null)
       // FIXME race
       getSearchCriterionColumns();
       
     return searchColumns.length;
   }
 
   private Dbms dbms() {
     return getDatabase().getDbms();
   }
 
   // 
   // =========================
   //  Low-level DB operations
   // =========================
   // 
 
   // 
   // -----------
   //  Structure
   // -----------
   // 
 
   private void dbModifyStructure(String sql)
       throws StructuralModificationFailedPoemException {
     // We have to do this to avoid blocking
 
     if (PoemThread.inSession())
       PoemThread.commit();
 
     try {
       database.getCommittedConnection().createStatement().executeUpdate(sql);
       database.log(new StructuralModificationLogEvent(sql));
     }
     catch (SQLException e) {
       throw new StructuralModificationFailedPoemException(sql, e);
     }
   }
 
   private void dbCreateTable() {
     StringBuffer sqb = new StringBuffer();
     sqb.append("CREATE TABLE " + quotedName() + " (");
     for (int c = 0; c < columns.length; ++c) {
       if (c != 0) sqb.append(", ");
       sqb.append(columns[c].quotedName() + " " +
                  columns[c].getSQLType().sqlDefinition(dbms()));
     }
 
     sqb.append(")");
 
     dbModifyStructure(sqb.toString());
   }
 
   private void dbAddColumn(Column column) {
     dbModifyStructure(
         "ALTER TABLE " + quotedName() +
         " ADD COLUMN " + column.quotedName() +
         " " + column.getSQLType().sqlDefinition(getDatabase().getDbms()));
   }
 
   private void dbCreateIndex(Column column) {
     if (column.getIndexed())
       dbModifyStructure(
           "CREATE " + (column.getUnique() ? "UNIQUE " : "") + "INDEX " +
           database._quotedName(name + "_" + column.getName() + "_index") +
           " ON " + quotedName() + " " +
           "(" + column.quotedName() + 
            getDatabase().getDbms().getIndexLength(column) + ")");
   }
 
   // 
   // -------------------------------
   //  Standard `PreparedStatement's
   // -------------------------------
   // 
 
   private PreparedStatement simpleInsert(Connection connection) {
     StringBuffer sql = new StringBuffer();
     sql.append("INSERT INTO " + quotedName() + " (");
     for (int c = 0; c < columns.length; ++c) {
       if (c > 0) sql.append(", ");
       sql.append(columns[c].quotedName());
     }
     sql.append(") VALUES (");
     for (int c = 0; c < columns.length; ++c) {
       if (c > 0) sql.append(", ");
       sql.append("?");
     }
 
     sql.append(")");
 
     try {
       return connection.prepareStatement(sql.toString());
     }
     catch (SQLException e) {
       throw new SimplePrepareFailedPoemException(sql.toString(), e);
     }
   }
 
   private PreparedStatement simpleGet(Connection connection) {
     StringBuffer sql = new StringBuffer();
     sql.append("SELECT ");
     for (int c = 0; c < columns.length; ++c) {
       if (c > 0) sql.append(", ");
       sql.append(columns[c].quotedName());
     }
     sql.append(" FROM " + quotedName() +
                " WHERE " + troidColumn.quotedName() + " = ?");
 
     try {
       return connection.prepareStatement(sql.toString());
     }
     catch (SQLException e) {
       throw new SimplePrepareFailedPoemException(sql.toString(), e);
     }
   }
 
   private PreparedStatement simpleModify(Connection connection) {
     // FIXME synchronize this too
     StringBuffer sql = new StringBuffer();
     sql.append("UPDATE " + quotedName() + " SET ");
     for (int c = 0; c < columns.length; ++c) {
       if (c > 0) sql.append(", ");
       sql.append(columns[c].quotedName());
       sql.append(" = ?");
     }
     sql.append(" WHERE " + troidColumn.quotedName() + " = ?");
 
     try {
       return connection.prepareStatement(sql.toString());
     }
     catch (SQLException e) {
       throw new SimplePrepareFailedPoemException(sql.toString(), e);
     }
   }
 
   // 
   // -----------------------------
   //  Transaction-specific things
   // -----------------------------
   // 
 
   private class TransactionStuff {
     PreparedStatement insert, modify, get;
     TransactionStuff(Connection connection) {
       insert = _this.simpleInsert(connection);
       modify = _this.simpleModify(connection);
       get = _this.simpleGet(connection);
     }
   }
 
   private CachedIndexFactory transactionStuffs = new CachedIndexFactory() {
     public Object reallyGet(int index) {
       // "Table.this" is attempt to work around Dietmar's problem with JDK1.3.1
       return new TransactionStuff(
           Table.this.database.poemTransaction(index).getConnection());
     }
   };
 
   private TransactionStuff committedTransactionStuff = null;
 
   private synchronized TransactionStuff getCommittedTransactionStuff() {
     if (committedTransactionStuff == null)
       committedTransactionStuff =
           new TransactionStuff(database.getCommittedConnection());
     return committedTransactionStuff;
   }
 
   // 
   // --------------------
   //  Loading and saving
   // --------------------
   // 
 
   private void load(PreparedStatement select, Persistent persistent) {
     try {
       synchronized (select) {
         select.setInt(1, persistent.troid().intValue());
         ResultSet rs = select.executeQuery();
         try {
           if (database.logSQL())
             database.log(new SQLLogEvent(select.toString()));
 
           if (!rs.next())
             persistent.setStatusNonexistent();
           else {
             persistent.setStatusExistent();
             for (int c = 0; c < columns.length; ++c)
               columns[c].load_unsafe(rs, c + 1, persistent);
           }
 
           persistent.dirty = false;
           persistent.markValid();
 
           if (rs.next())
             throw new DuplicateTroidPoemException(this, persistent.troid());
         }
         finally {
           try { rs.close(); } catch (Exception e) {}
         }
       }
     }
     catch (SQLException e) {
       throw new SimpleRetrievalFailedPoemException(e);
     }
     catch (ParsingPoemException e) {
       throw new UnexpectedParsingPoemException(e);
     }
     catch (ValidationPoemException e) {
       throw new UnexpectedValidationPoemException(e);
     }
   }
 
   void load(PoemTransaction transaction, Persistent persistent) {
     load(transaction == null ?
             getCommittedTransactionStuff().get :
             ((TransactionStuff)transactionStuffs.get(transaction.index)).get,
          persistent);
   }
 
   private void modify(PoemTransaction transaction, Persistent persistent) {
     PreparedStatement modify =
         ((TransactionStuff)transactionStuffs.get(transaction.index)).modify;
     synchronized (modify) {
       for (int c = 0; c < columns.length; ++c)
         columns[c].save_unsafe(persistent, modify, c + 1);
 
       try {
         modify.setInt(columns.length + 1, persistent.troid().intValue());
       }
       catch (SQLException e) {
         throw new SQLSeriousPoemException(e);
       }
 
       try {
         modify.executeUpdate();
       }
       catch (SQLException e) {
         throw dbms().exceptionForUpdate(this, modify, false, e);
       }
 
       if (database.logSQL())
         database.log(new SQLLogEvent(modify.toString()));
     }
     persistent.postModify();
   }
 
   private void insert(PoemTransaction transaction, Persistent persistent) {
     PreparedStatement insert =
         ((TransactionStuff)transactionStuffs.get(transaction.index)).insert;
     synchronized (insert) {
       for (int c = 0; c < columns.length; ++c)
         columns[c].save_unsafe(persistent, insert, c + 1);
       try {
         insert.executeUpdate();
       }
       catch (SQLException e) {
         throw dbms().exceptionForUpdate(this, insert, true, e);
       }
       if (database.logSQL())
         database.log(new SQLLogEvent(insert.toString()));
     }
 
     persistent.postInsert();
   }
 
   void delete(Integer troid, PoemTransaction transaction) {
     String sql =
         "DELETE FROM " + quotedName() +
         " WHERE " + troidColumn.quotedName() + " = " +
         troid.toString();
 
     try {
       Connection connection;
       if (transaction == null)
         connection = getDatabase().getCommittedConnection();
       else {
         transaction.writeDown();
         connection = transaction.getConnection();
       }
 
       connection.createStatement().executeUpdate(sql);
       if (database.logSQL())
         database.log(new SQLLogEvent(sql));
 
       cache.delete(troid);
     }
     catch (SQLException e) {
       throw new ExecutingSQLPoemException(sql, e);
     }
   }
 
   void writeDown(PoemTransaction transaction, Persistent persistent) {
     // no race, provided that the one-thread-per-transaction parity is
     // maintained
 
     if (persistent.dirty) {
       troidColumn.setRaw_unsafe(persistent, persistent.troid());
 
       if (persistent.statusExistent())
         modify(transaction, persistent);
       else if (persistent.statusNonexistent()) {
         insert(transaction, persistent);
         persistent.setStatusExistent();
       }
 
       persistent.dirty = false;
       persistent.postWrite();
     }
   }
 
   // 
   // ============
   //  Operations
   // ============
   // 
 
   // 
   // ----------
   //  Cacheing
   // ----------
   // 
 
   private Cache cache = new Cache(CACHE_LIMIT_DEFAULT);
 
   private static final Procedure invalidator =
       new Procedure() {
         public void apply(Object arg) {
           ((Transactioned)arg).invalidate();
         }
       };
 
   void uncacheContents() {
     cache.iterate(invalidator);
     serial.invalidate();
     TableListener[] listeners = this.listeners;
     for (int l = 0; l < listeners.length; ++l)
       listeners[l].notifyUncached(this);
   }
 
   void trimCache(int maxSize) {
     cache.trim(maxSize);
   }
 
   public Cache.Info getCacheInfo() {
     return cache.getInfo();
   }
 
   public void addListener(TableListener listener) {
     listeners = (TableListener[])ArrayUtils.added(listeners, listener);
   }
 
   /**
    * Notify the table that one if its records is about to be changed in a
    * transaction.  You can (with care) use this to support cacheing of
    * frequently-used facts about the table's records.  For instance,
    * <TT>GroupMembershipTable</TT> and <TT>GroupCapabilityTable</TT> override
    * this to inform <TT>UserTable</TT> that its cache of users' capabilities
    * has become invalid.
    *
    * @param transaction the transaction in which the change will be made
    * @param persistent  the record to be changed
    *
    * @see GroupMembershipTable#notifyTouched
    */
 
   void notifyTouched(PoemTransaction transaction, Persistent persistent) {
     serial.increment(transaction);
 
     TableListener[] listeners = this.listeners;
     for (int l = 0; l < listeners.length; ++l)
       listeners[l].notifyTouched(transaction, this, persistent);
   }
 
   public long serial(PoemTransaction transaction) {
     return serial.current(transaction);
   }
 
   public void readLock() {
     serial(PoemThread.transaction());
   }
 
   // 
   // ----------
   //  Fetching
   // ----------
   // 
 
   /**
    * The object from the table with a given troid.
    *
    * @param troid       Every record (object) in a POEM database must have a
    *                    troid (table row ID, or table-unique non-nullable
    *                    integer primary key), often but not necessarily called
    *                    <TT>id</TT>, so that it can be conveniently `named' for
    *                    retrieval by this method.
    *
    * @return A <TT>Persistent</TT> representing the record with the given troid;
    *         or, if the table was defined in the DSD under the name
    *         <TT><I>foo</I></TT>, an application-specialised subclass
    *         <TT><I>Foo</I></TT> of <TT>Persistent</TT>.  In that case, there
    *         will also be an application-specialised <TT>Table</TT> subclass,
    *         called <TT><I>Foo</I>Table</TT> (and available as
    *         <TT>get<I>Foo</I>Table</TT> from the application-specialised
    *         <TT>Database</TT> subclass), which has a matching method
    *         <TT>get<I>Foo</I>Object</TT> for obtaining the specialised object
    *         under its own type.  Note that no access checks are done at this
    *         stage: you may not be able to do anything with the object handle
    *         returned from this method without provoking a
    *         <TT>PoemAccessException</TT>.
    *
    * @exception NoSuchRowPoemException
    *                if there is no row in the table with the given troid
    *
    * @see Persistent#getTroid()
    */
 
   public Persistent getObject(Integer troid) throws NoSuchRowPoemException {
     Persistent persistent = (Persistent)cache.get(troid);
 
     if (persistent == null) {
       persistent = newPersistent();
       claim(persistent, troid);
       load(PoemThread.transaction(), persistent);
       if (persistent.statusExistent())
         synchronized (cache) {
           Persistent tryAgain = (Persistent)cache.get(troid);
           if (tryAgain == null)
             cache.put(troid, persistent);
           else
             persistent = tryAgain;
         }
     }
 
     if (!persistent.statusExistent())
       throw new NoSuchRowPoemException(this, troid);
 
     persistent.existenceLock(PoemThread.sessionToken());
 
     return persistent;
   }
 
   /**
    * The object from the table with a given troid.  See previous.
    *
    * @see #getObject(java.lang.Integer)
    */
 
   public Persistent getObject(int troid) throws NoSuchRowPoemException {
     return getObject(new Integer(troid));
   }
 
   // 
   // -----------
   //  Searching
   // -----------
   // 
 
   String selectionSQL(String whereClause, String orderByClause,
                       boolean includeDeleted) {
     if (deletedColumn != null && !includeDeleted)
       whereClause =
           (whereClause == null || whereClause.equals("") ?
                "" : "(" + whereClause + ") AND ") +
           "NOT " + deletedColumn.getName();
 
     if (orderByClause == null) 
       orderByClause = defaultOrderByClause();
 
     // FIXME must work in some kind of limit
       
     return
         "SELECT " + troidColumn.quotedName() +
         " FROM " + quotedName() +
         (whereClause == null || whereClause.equals("") ?
              "" : " WHERE " + whereClause) +
         // actually orderByClause is never null (since fallback is id)
         (orderByClause == null || orderByClause.equals("") ?
              "" : " ORDER BY " + orderByClause);
   }
 
   private ResultSet selectionResultSet(
       String whereClause, String orderByClause, boolean includeDeleted,
       PoemTransaction transaction)
           throws SQLPoemException {
 
     String sql = selectionSQL(whereClause, orderByClause, includeDeleted);
             
     try {
       Connection connection;
       if (transaction == null)
         connection = getDatabase().getCommittedConnection();
       else {
         transaction.writeDown();
         connection = transaction.getConnection();
       }
 
       ResultSet rs = connection.createStatement().executeQuery(sql);
 
       SessionToken token = PoemThread._sessionToken();
       if (token != null)
         token.toTidy().add(rs);
 
       if (database.logSQL())
         database.log(new SQLLogEvent(sql));
       return rs;
     }
     catch (SQLException e) {
       throw new ExecutingSQLPoemException(sql, e);
     }
   }
 
   public Enumeration troidSelection(
       String whereClause, String orderByClause,
       boolean includeDeleted, PoemTransaction transaction) {
     ResultSet them = selectionResultSet(whereClause, orderByClause,
                                         includeDeleted, transaction);
     return
         new ResultSetEnumeration(them) {
           public Object mapped(ResultSet rs) throws SQLException {
             return new Integer(rs.getInt(1));
           }
         };
   }
 
   protected void rememberAllTroids(boolean flag) {
     if (flag) {
       if (allTroids == null)
 	allTroids = new CachedSelection(this, null, null);
     }
     else
       allTroids = null;
   }
 
   protected void setCacheLimit(Integer limit) {
     cache.setSize(limit == null ? CACHE_LIMIT_DEFAULT : limit.intValue());
   }
 
   /**
    * A <TT>SELECT</TT>ion of troids of objects from the table meeting given
    * criteria.
    *
    * If the orderByClause is null, then the default order by clause is applied.
    * If the orderByClause is an empty string, ie "", then no ordering is applied
    *
    * @return an <TT>Enumeration</TT> of <TT>Integer</TT>s, which can be mapped
    *         onto <TT>Persistent</TT> objects using <TT>getObject</TT>;
    *         or you can just use <TT>selection</TT>
    *
    * @see #getObject(java.lang.Integer)
    * @see #selection(java.lang.String, java.lang.String, boolean)
    */
 
   public Enumeration troidSelection(String whereClause, String orderByClause,
                                     boolean includeDeleted)
       throws SQLPoemException {
     CachedSelection allTroids = this.allTroids;
     if (allTroids != null &&
         (whereClause == null || whereClause.equals("")) &&
 	(orderByClause == null || orderByClause.equals("") ||
 	   orderByClause == /* sic, for speed */ defaultOrderByClause()) &&
 	!includeDeleted)
       return allTroids.troids();
     else
       return troidSelection(whereClause, orderByClause, includeDeleted,
                             PoemThread.transaction());
   }
 
   /**
    * All the objects in the table.
    *
    * @return An <TT>Enumeration</TT> of <TT>Persistent</TT>s, or, if the table
    *         was defined in the DSD under the name <TT><I>foo</I></TT>, of
    *         application-specialised subclasses <TT><I>Foo</I></TT>.  Note
    *         that no access checks are done at this stage: you may not be able
    *         to do anything with some of the object handles in the enumeration
    *         without provoking a <TT>PoemAccessException</TT>.  If the table
    *         has a <TT>deleted</TT> column, the objects flagged as deleted will
    *         be passed over.
    */
 
   public Enumeration selection() throws SQLPoemException {
     return selection(null, null, false);
   }
 
   /**
    * A <TT>SELECT</TT>ion of objects from the table meeting given criteria.
    * This is one way to run a search against the database and return the
    * results as a series of typed POEM objects.
    *
    * @param whereClause         SQL <TT>SELECT</TT>ion criteria for the search:
    *                            the part that should appear after the
    *                            <TT>WHERE</TT> keyword
    *
    * @return An <TT>Enumeration</TT> of <TT>Persistent</TT>s, or, if the table
    *         was defined in the DSD under the name <TT><I>foo</I></TT>, of
    *         application-specialised subclasses <TT><I>Foo</I></TT>.  Note
    *         that no access checks are done at this stage: you may not be able
    *         to do anything with some of the object handles in the enumeration
    *         without provoking a <TT>PoemAccessException</TT>.  If the table
    *         has a <TT>deleted</TT> column, the objects flagged as deleted will
    *         be passed over.
    *
    * @see Column#selectionWhereEq(java.lang.Object)
    */
 
   public final Enumeration selection(String whereClause)
       throws SQLPoemException {
     return selection(whereClause, null, false);
   }
 
 
   public Persistent firstSelection(String whereClause) {
     Enumeration them = selection(whereClause);
     return them.hasMoreElements() ? (Persistent)them.nextElement() : null;
   }
 
   /**
    * A <TT>SELECT</TT>ion of objects from the table meeting given criteria,
    * possibly including those flagged as deleted.
    *
    * If the orderByClause is null, then the default order by clause is applied.
    * If the orderByClause is an empty string, ie "", then no ordering is applied
    *
    * @param includeDeleted      whether to return objects flagged as deleted
    *                            (ignored if the table doesn't have a
    *                            <TT>deleted</TT> column)
    *
    * @see #selection(java.lang.String)
    */
    
    public Enumeration selection(String whereClause, String orderByClause,
                                 boolean includeDeleted)
       throws SQLPoemException {
     return
         new MappedEnumeration(troidSelection(whereClause, orderByClause,
                                              includeDeleted)) {
           public Object mapped(Object troid) {
             return getObject((Integer)troid);
           }
         };
   }
 
   /**
    * A <TT>SELECT</TT>ion of objects from the table meeting given criteria,
    * possibly including those flagged as deleted.  The results are returned in 'pages'.
    *
    * If the orderByClause is null, then the default order by clause is applied.
    * If the orderByClause is an empty string, ie "", then no ordering is applied
    *
    * @param includeDeleted      whether to return objects flagged as deleted
    *                            (ignored if the table doesn't have a
    *                            <TT>deleted</TT> column)
    *
    * @see #selection(java.lang.String)
    */
   public PageEnumeration selection(
       String whereClause, String orderByClause, boolean includeDeleted,
       int pageStart, int pageSize)
           throws SQLPoemException {
     // FIXME do this more sensibly where SQL permits
     return new DumbPageEnumeration(
         selection(whereClause, orderByClause, includeDeleted),
         pageStart, pageSize, 200);
   }
 
   String countSQL(String whereClause) {
     return "SELECT count(*) FROM " + quotedName() +
         (whereClause == null || whereClause.equals("") ? "" :
              " WHERE " + whereClause);
   }
 
   public int count(String whereClause)
       throws SQLPoemException {
 
     String sql = countSQL(whereClause);
 
     try {
       PoemTransaction transaction = PoemThread.transaction();
       Connection connection;
       if (transaction == null)
         connection = getDatabase().getCommittedConnection();
       else {
         transaction.writeDown();
         connection = transaction.getConnection();
       }
 
       ResultSet rs = connection.createStatement().executeQuery(sql);
       if (database.logSQL())
         database.log(new SQLLogEvent(sql));
       rs.next();
       int count = rs.getInt(1);
       rs.close();
       return count;
     }
     catch (SQLException e) {
       throw new ExecutingSQLPoemException(sql, e);
     }
   }
 
   public boolean exists(String whereClause) throws SQLPoemException {
     return count(whereClause) > 0;
   }
 
   /**
    * FIXME you can't search for NULLs ...
    */
 
   public void appendWhereClause(StringBuffer clause, Persistent persistent) {
     Column[] columns = this.columns;
     boolean hadOne = false;
     for (int c = 0; c < columns.length; ++c) {
       Column column = columns[c];
       Object raw = column.getRaw_unsafe(persistent);
       if (raw != null) {
         if (hadOne)
           clause.append(" AND ");
         else
           hadOne = true;
 
         if (column.getType() instanceof StringPoemType) {
 	  clause.append( 
 	    getDatabase().getDbms().caseInsensitiveRegExpSQL(
                   column.quotedName(),
                   column.getSQLType().quotedRaw(raw)));
         } else {
           clause.append(column.quotedName());
           clause.append(" = ");
           clause.append(column.getSQLType().quotedRaw(raw));
         }			
       }
     }
   }
 
   public String whereClause(Persistent persistent) {
     StringBuffer clause = new StringBuffer();
     appendWhereClause(clause, persistent);
     return clause.toString();
   }
 
   public String cnfWhereClause(Enumeration persistents) {
     StringBuffer clause = new StringBuffer();
 
     boolean hadOne = false;
     while (persistents.hasMoreElements()) {
       if (hadOne)
         clause.append(" OR ");
       else
         hadOne = true;
       clause.append("(");
       appendWhereClause(clause, (Persistent)persistents.nextElement());
       clause.append(")");
     }
 
     return clause.toString();
   }
 
   public boolean exists(Persistent persistent) {
     return exists(whereClause(persistent));
   }
 
   /**
    * All the objects in the table which refer to a given object.  If none of
    * the table's columns are reference columns, the <TT>Enumeration</TT>
    * returned will obviously be empty.  This is used by
    * <TT>Persistent.delete()</TT> to determine whether deleting an object would
    * destroy the integrity of any references.  It is not guaranteed to be
    * quick to execute!
    *
    * @return an <TT>Enumeration</TT> of <TT>Persistent</TT>s
    */
 
   public Enumeration referencesTo(final Persistent object) {
     return new FlattenedEnumeration(
         new MappedEnumeration(columns()) {
           public Object mapped(Object column) {
             return ((Column)column).referencesTo(object);
           }
         });
   }
 
   // All the columns in the table which refer to a given table.
 
   public Enumeration referencesTo(final Table table) {
     return
       new FilteredEnumeration(columns()) {
         public boolean isIncluded(Object column) {
           PoemType type = ((Column)column).getType();
           return type instanceof ReferencePoemType &&
                  ((ReferencePoemType)type).targetTable() == table;
         }
       };
   }
 
   // 
   // ----------
   //  Creation
   // ----------
   // 
 
   private void validate(Persistent persistent)
       throws FieldContentsPoemException {
     for (int c = 0; c < columns.length; ++c) {
       Column column = columns[c];
       try {
         column.getType().assertValidRaw(column.getRaw_unsafe(persistent));
       }
       catch (Exception e) {
         throw new FieldContentsPoemException(column, e);
       }
     }
   }
 
   private int nextTroid = -1;
 
   protected synchronized Integer troidFor(Persistent persistent) {
     if (nextTroid == -1)
       throw new PoemBugPoemException();
     return new Integer(nextTroid++);
   }
 
   public static class AccessibleCreationException extends AccessPoemException {
     public AccessibleCreationException(AccessPoemException e) {
       super(e);
     }
 
     public String getActionDescription() {
       return "create an object which can only be accessed by users with the " +
              "capability " + capability;
     }
 
     public String getMessage() {
       return "You cannot " + getActionDescription() + " since your access " +
              "token " + token + " doesn't confer that capability";
     }
   }
 
   public void create(Persistent persistent)
       throws AccessPoemException, ValidationPoemException,
              InitialisationPoemException {
 
     SessionToken sessionToken = PoemThread.sessionToken();
 
     if (persistent.getTable() == null)
       persistent.setTable(this, null);
     persistent.assertCanCreate(sessionToken.accessToken);
 
     claim(persistent, troidFor(persistent));
     persistent.setStatusNonexistent();
 
     // Are the values they have put in legal; is the result something they
     // could have created by writing into a record?
 
     try {
       validate(persistent);
     }
     catch (Exception e) {
       throw new InitialisationPoemException(this, e);
     }
 
     // Lock the cache while we try an initial write-down to see if the DB picks
     // up any inconsistencies like duplicated unique fields
 
     synchronized (cache) {
       persistent.dirty = true;
       writeDown(sessionToken.transaction, persistent);
 
       // OK, it worked.  Plug the object into the cache.
 
       persistent.readLock(sessionToken.transaction);
       cache.put(persistent.troid(), persistent);
     }
 
     notifyTouched(sessionToken.transaction, persistent);
   }
 
   /**
    * Create a new object (record) in the table.
    *
    * @param initialiser         A piece of code for setting the new object's
    *                            initial values.  You'll probably want to define
    *                            it as an anonymous class.
    *
    * @return A <TT>Persistent</TT> representing the new object, or, if the
    *         table was defined in the DSD under the name <TT><I>foo</I></TT>,
    *         an application-specialised subclass <TT><I>Foo</I></TT> of
    *         <TT>Persistent</TT>.
    *
    * @exception CreationAccessPoemException
    *                if the calling thread's <TT>AccessToken</TT> doesn't allow
    *                you to create records in the table
    * @exception AccessPoemException
    *                if <TT>initialiser</TT> provokes one during its work (which
    *                is unlikely, since POEM's standard checks are disabled
    *                while it runs)
    * @exception ValidationPoemException
    *                if <TT>initialiser</TT> provokes one during its work
    * @exception InitialisationPoemException
    *                if the object is left by <TT>initialiser</TT> in a state in
    *                which not all of its fields have legal values, or in which
    *                the calling thread would not be allowed write access to the
    *                object under its <TT>AccessToken</TT>---<I>i.e.</I> you
    *                can't create objects you wouldn't be allowed to write to.
    *
    * @see Initialiser#init(org.melati.poem.Persistent)
    * @see PoemThread#accessToken()
    * @see #getCanCreate()
    */
 
   public Persistent create(Initialiser initialiser)
       throws AccessPoemException, ValidationPoemException,
              InitialisationPoemException {
     Persistent persistent = newPersistent();
     initialiser.init(persistent);
     create(persistent);
     return persistent;
   }
 
   private void claim(Persistent persistent, Integer troid) {
     // We don't want to end up with two of this object in the cache
  
     if (cache.get(troid) != null)
       throw new DuplicateTroidPoemException(this, troid);
 
     if (persistent.troid() != null)
       throw new DoubleCreatePoemException(persistent);
 
     persistent.setTable(this, troid);
 
     troidColumn.setRaw_unsafe(persistent, troid);
     if (deletedColumn != null)
       deletedColumn.setRaw_unsafe(persistent, Boolean.FALSE);
   }
 
   /**
    * A freshly minted <TT>Persistent</TT> object for the table.
    */
 
   public final Persistent newPersistent() {
     Persistent it = _newPersistent();
     it.setTable(this, null);
     return it;
   }
 
   /**
    * A freshly minted, and uninitialised, <TT>Persistent</TT> object for the
    * table.  You don't ever have to call this and there is no point in doing so
    * This method is overridden in application-specialised <TT>Table</TT>
    * subclasses derived from the Data Structure Definition.
    */
 
   protected Persistent _newPersistent() {
     return new Persistent();
   }
 
   public void delete_unsafe(String whereClause) {
     serial.increment(PoemThread.transaction());
     getDatabase().sqlUpdate("DELETE FROM " + quotedName + " WHERE " + whereClause);
     uncacheContents();
   }
 
   /**
    * The number of `extra' (non-DSD-defined) columns in the table.
    */
 
   int extrasCount() {
     return extrasIndex;
   }
 
   // 
   // ----------------
   //  Access control
   // ----------------
   // 
 
   /**
    * The capability required for reading records from the table, unless
    * overridden in the record itself.  This simply comes from the table's
    * record in the <TT>tableinfo</TT> table.
    *
    * @see Persistent#getCanRead()
    */
 
   public final Capability getDefaultCanRead() {
     return info == null ? null : info.getDefaultcanread();
   }
 
   /**
    * The capability required for updating records in the table, unless
    * overridden in the record itself.  This simply comes from the table's
    * record in the <TT>tableinfo</TT> table.
    *
    * @see Persistent#getCanWrite()
    */
 
   public final Capability getDefaultCanWrite() {
     return info == null ? null : info.getDefaultcanwrite();
   }
 
   public final Capability getDefaultCanDelete() {
     return info == null ? null : info.getDefaultcandelete();
   }
 
   /**
    * The capability required for creating records in the table.  This simply
    * comes from the table's record in the <TT>tableinfo</TT> table.
    *
    * @see #create(org.melati.poem.Initialiser)
    */
 
   public final Capability getCanCreate() {
     return info == null ? null : info.getCancreate();
   }
 
   final Column canReadColumn() {
     return canReadColumn;
   }
 
   final Column canWriteColumn() {
     return canWriteColumn;
   }
 
   final Column canDeleteColumn() {
     return canDeleteColumn;
   }
 
   // 
   // -----------
   //  Structure
   // -----------
   // 
 
   public Column addColumnAndCommit(ColumnInfo info) throws PoemException {
 
     // Set the new column up
 
     Column column = ExtraColumn.from(this, info, extrasIndex++,
                                      DefinitionSource.runtime);
     column.setColumnInfo(info);
 
     // Do a dry run to make sure no problems (ALTER TABLE ADD COLUMN is
     // well-nigh irrevocable in Postgres)
 
     defineColumn(column, false);
 
     // ALTER TABLE ADD COLUMN
 
     database.beginStructuralModification();
     dbAddColumn(column);
     try {
       synchronized (cache) {    // belt and braces
         uncacheContents();
 	transactionStuffs.invalidate();
         defineColumn(column, true);
       }
       PoemThread.commit();
     }
     finally {
       database.endStructuralModification();
     }
 
     return column;
   }
 
   // 
   // ===========
   //  Utilities
   // ===========
   // 
 
   /**
    * A concise string to stand in for the table.  The table's name and a
    * description of where it was defined (the DSD, the metadata tables or the
    * JDBC metadata).
    */
 
   public String toString() {
     return getName() + " (from " + definitionSource + ")";
   }
 
   /**
    * Print some diagnostic information about the contents and consistency of
    * POEM's cache for this table to stderr.
    */
 
   public void dumpCacheAnalysis() {
     System.err.println("\n-------- Analysis of " + name + "'s cache\n");
     cache.dumpAnalysis();
   }
 
   /**
    * Print information about the structure of the database to stdout.
    */
 
   public void dump() {
     System.out.println("=== table " + name +
                        " (tableinfo id " + tableInfoID() + ")");
     for (int c = 0; c < columns.length; ++c)
       columns[c].dump();
   }
 
   /**
    * FIXME to be documented.
    */
 
   public CachedSelection cachedSelection(String whereClause,
                                            String orderByClause) {
     String key = whereClause + "/" + orderByClause;
     CachedSelection them = (CachedSelection)cachedSelections.get(key);
     if (them == null) {
       CachedSelection newThem =
           new CachedSelection(this, whereClause, orderByClause);
       synchronized (cachedSelections) {
         them = (CachedSelection)cachedSelections.get(key);
         if (them == null)
           cachedSelections.put(key, them = newThem);
       }
     }
     return them;
   }
 
   public CachedCount cachedCount(String whereClause) {
     CachedCount it = (CachedCount)cachedCounts.get(whereClause);
     if (it == null) {
       CachedCount newIt =
           new CachedCount(this, whereClause);
       synchronized (cachedCounts) {
         it = (CachedCount)cachedCounts.get(whereClause);
         if (it == null)
           cachedCounts.put(whereClause, it = newIt);
       }
     }
     return it;
   }
 
   public CachedExists cachedExists(String whereClause) {
     CachedExists it = (CachedExists)cachedExists.get(whereClause);
     if (it == null) {
       CachedExists newIt =
           new CachedExists(this, whereClause);
       synchronized (cachedExists) {
         it = (CachedExists)cachedExists.get(whereClause);
         if (it == null)
           cachedExists.put(whereClause, it = newIt);
       }
     }
     return it;
   }
 
   public RestrictedReferencePoemType cachedSelectionType(
       String whereClause, String orderByClause, boolean nullable) {
     return new RestrictedReferencePoemType(
                cachedSelection(whereClause, orderByClause), nullable);
   }
 
   /**
    * Make up a <TT>Field</TT> object whose possible values are a selected
    * subset of the records in the table.  You can make a "dropdown" offering a
    * choice of your green customers by putting this in your handler
    *
    * <BLOCKQUOTE><PRE>
    * context.put("greens",
    *             melati.getDatabase().getCustomerTable().cachedSelectionField(
    *                 "colour = 'green'", null, true, null, "greens"));
    * </PRE></BLOCKQUOTE>
    *
    * and this in your template
    *
    * <BLOCKQUOTE><PRE>
    *   Select a customer: $ml.input($greens)
    * </PRE></BLOCKQUOTE>
    *
    * The list of member records is implicitly cached---permanently, and however
    * big it turns out to be.  So don't go mad with this.  It is recomputed on
    * demand if the contents of the table are changed.  The <TT>whereClause</TT>
    * and <TT>orderByClause</TT> you pass in are checked to see if you have
    * asked for the same list before, so however many times you call this
    * method, you should only trigger actual <TT>SELECT</TT>s when the table
    * contents have changed.  The list is also transaction-safe, in that it will
    * always reflect the state of affairs within your transaction even if you
    * haven't done a commit.
    *
    * @param whereClause         an SQL expression (the bit after the
    *                            <TT>SELECT</TT> ... <TT>WHERE</TT>) for picking
    *                            out the records you want
    *
    * @param orderByClause       a comma-separated list of column names which
    *                            determine the order in which the records are
    *                            presented; if this is <TT>null</TT>, the
    *                            <TT>displayorderpriority</TT> attributes of the
    *                            table's columns determine the order
    *
    * @param nullable            whether to allow a blank <TT>NULL</TT> option
    *                            as the first possibility
    *
    * @param selectedTroid       the troid of the record to which the
    *                            <TT>SELECT</TT> field should initially be set
    *
    * @param name                the HTML name attribute of the field,
    *                            <I>i.e.</I>
    *                            <TT>&lt;SELECT NAME=<I>name</I>&gt;</TT>
    */
 
   public Field cachedSelectionField(
       String whereClause, String orderByClause, boolean nullable,
       Integer selectedTroid, String name) {
     return new Field(
         selectedTroid,
         new BaseFieldAttributes(name,
                                 cachedSelectionType(whereClause,
                                                     orderByClause, nullable)));
   }
 
   // 
   // ================
   //  Initialization
   // ================
   // 
 
   /**
    * Don't call this.  Columns should be defined either in the DSD (in which
    * case the boilerplate code generated by the preprocessor will call this
    * method) or directly in the RDBMS (in which case the initialisation code
    * will).
    */
 
   protected synchronized void defineColumn(Column column, boolean reallyDoIt)
       throws DuplicateColumnNamePoemException,
              DuplicateTroidColumnPoemException,
              DuplicateDeletedColumnPoemException {
     if (column.getTable() != this)
       throw new ColumnInUsePoemException(this, column);
 
     if (columnsByName.get(column.getName()) != null)
       throw new DuplicateColumnNamePoemException(this, column);
 
     if (column.isTroidColumn()) {
       if (troidColumn != null)
         throw new DuplicateTroidColumnPoemException(this, column);
       if (reallyDoIt)
         troidColumn = column;
     }
     else if (column.isDeletedColumn()) {
       if (deletedColumn != null)
         throw new DuplicateDeletedColumnPoemException(this, column);
       if (reallyDoIt)
         deletedColumn = column;
     }
     else {
       if (reallyDoIt) {
         PoemType type = column.getType();
         if (type instanceof ReferencePoemType &&
             ((ReferencePoemType)type).targetTable() ==
                  database.getCapabilityTable()) {
           if (column.getName().equals("canread"))
             canReadColumn = column;
           else if (column.getName().equals("canwrite"))
             canWriteColumn = column;
           else if (column.getName().equals("candelete"))
             canDeleteColumn = column;
         }
       }
     }
 
     if (reallyDoIt) {
       column.setTable(this);
       columns = (Column[])ArrayUtils.added(columns, column);
       columnsByName.put(column.getName(), column);
     }
   }
 
   protected final void defineColumn(Column column)
       throws DuplicateColumnNamePoemException,
              DuplicateTroidColumnPoemException,
              DuplicateDeletedColumnPoemException {
     defineColumn(column, true);
   }
 
   private void _defineColumn(Column column) {
     try {
       defineColumn(column);
     }
     catch (DuplicateColumnNamePoemException e) {
       throw new UnexpectedExceptionPoemException(e);
     }
     catch (DuplicateTroidColumnPoemException e) {
       throw new UnexpectedExceptionPoemException(e);
     }
   }
 
   int extrasIndex = 0;
 
   void setTableInfo(TableInfo tableInfo) {
     info = tableInfo;
     rememberAllTroids(tableInfo.getSeqcached().booleanValue());
     setCacheLimit(tableInfo.getCachelimit());
   }
   
   public TableInfo getTableInfo() {
     return info;
   }
 
   /**
    * The `factory-default' display name for the table.  By default this is the
    * table's programmatic name, capitalised.  Application-specialised tables
    * override this to return any <TT>(displayname = </TT>...<TT>)</TT> provided
    * in the DSD.  This is only ever used at startup time when creating
    * <TT>columninfo</TT> records for tables that don't have them.
    */
 
   protected String defaultDisplayName() {
     return StringUtils.capitalised(getName());
   }
 
   protected int defaultDisplayOrder() {
     return 100;
   }
 
   /**
    * The `factory-default' description for the table, or <TT>null</TT> if it
    * doesn't have one.  Application-specialised tables override this to return
    * any <TT>(description = </TT>...<TT>)</TT> provided in the DSD.  This is
    * only ever used at startup time when creating <TT>columninfo</TT> records
    * for tables that don't have them.
    */
 
   protected String defaultDescription() {
     return null;
   }
 
   protected Integer defaultCacheLimit() {
     return new Integer(CACHE_LIMIT_DEFAULT);
   }
 
   protected boolean defaultRememberAllTroids() {
     return false;
   }
 
   protected String defaultCategory() {
     return "Normal";
   }
 
   TableInfo defaultTableInfo() {
     return new TableInfo(
         getName(), defaultDisplayName(), defaultDisplayOrder(),
         defaultDescription(), defaultCacheLimit(), defaultRememberAllTroids(),
         database.getTableCategoryTable().ensure(defaultCategory()));
   }
 
   void createTableInfo() throws PoemException {
     if (info == null) {
       info = defaultTableInfo();
       getDatabase().getTableInfoTable().create(info);
       setTableInfo(info);
     }
   }
 
   synchronized void unifyWithColumnInfo() throws PoemException {
 
     // Match columnInfo with our columns
 
     if (info == null)
       throw new PoemBugPoemException("Get the initialisation order right ...");
 
     for (Enumeration ci =
              database.getColumnInfoTable().getTableinfoColumn().
                  selectionWhereEq(info.troid());
          ci.hasMoreElements();) {
       ColumnInfo columnInfo = (ColumnInfo)ci.nextElement();
       Column column = (Column)columnsByName.get(columnInfo.getName());
       if (column == null) {
         column = ExtraColumn.from(this, columnInfo, extrasIndex++,
                                   DefinitionSource.infoTables);
         _defineColumn(column);
       }
 
       column.setColumnInfo(columnInfo);
     }
 
     // Conversely, make columnInfo for any columns which don't have it
 
     for (Enumeration c = columns(); c.hasMoreElements();)
       ((Column)c.nextElement()).createColumnInfo();
   }
 
   public synchronized void unifyWithDB(ResultSet colDescs)
       throws SQLException, PoemException {
 
     Hashtable dbColumns = new Hashtable();
 
     int dbIndex = 0;
     if (colDescs != null){
 
       for (; colDescs.next(); ++dbIndex) {
         String colName = colDescs.getString("COLUMN_NAME");
 //        System.err.println("Table.unifyWithDB.colDescs:" + colName);
         Column column = (Column)columnsByName.get(
                           dbms().melatiName(colName));
 
         if (column == null) {
           SQLPoemType colType =
               database.defaultPoemTypeOfColumnMetaData(colDescs);
 
           // magically make eligible columns called "id" and "deleted"
           // into designed troid and soft-deleted-flag columns
           // FIXME this may not be a good idea
 
           if (troidColumn == null && colName.equals("id") &&
               dbms().canRepresent(colType, TroidPoemType.it) != null)
             colType = TroidPoemType.it;
 
           if (deletedColumn == null && colName.equals("deleted") &&
               dbms().canRepresent(colType, DeletedPoemType.it) != null)
             colType = DeletedPoemType.it;
 
           column = new ExtraColumn(this, 
                                    dbms().melatiName(
                                       colDescs.getString("COLUMN_NAME")),
                                    colType, DefinitionSource.sqlMetaData,
                                    extrasIndex++);
 
           _defineColumn(column);
 
           // FIXME hack: info == null happens when *InfoTable are unified with
           // the database---obviously they haven't been initialised yet but it
           // gets fixed in the next round when all tables (including them,
           // again) are unified
 
           if (info != null)
             column.createColumnInfo();
         }
         else {
           column.assertMatches(colDescs);
         }
         dbColumns.put(column, Boolean.TRUE);
       }
     } // else System.err.println("Table.UnifyWithDB called with null ResultsSet");
 
     if (dbIndex == 0) {
       // OK, we simply don't exist ...
       // ie the Database MetaData  Result Set passed in was empty or null
 //      System.err.println("Table.UnifyWithDB called with null or empty ResultsSet");
       dbCreateTable();
     } else {
       // silently create any missing columns
       for (int c = 0; c < columns.length; ++c) {
         if (dbColumns.get(columns[c]) == null)
           dbAddColumn(columns[c]);
       }
     }
 
     if (troidColumn == null)
       throw new NoTroidColumnException(this);
 
     // FIXME hack: info == null happens when *InfoTable are unified with
     // the database---obviously they haven't been initialised yet but it
     // gets fixed in the next round when all tables (including them,
     // again) are unified
 
     if (info != null) {
 
       // Check indices are unique
 
       Hashtable dbHasIndexForColumn = new Hashtable();
       ResultSet index =
           getDatabase().getCommittedConnection().getMetaData().
               getIndexInfo(null, "", getDatabase().getDbms().
                                unreservedName(getName()), false, true);
       while (index.next()) {
         try {
           String columnName = getDatabase().getDbms().
                                melatiName(index.getString("COLUMN_NAME"));
           Column column = getColumn(columnName);
           column.unifyWithIndex(index);
           dbHasIndexForColumn.put(column, Boolean.TRUE);
         }
         catch (NoSuchColumnPoemException e) {
           // hmm, let's just ignore this since it will never happen
         }
       }
 
       // Silently create any missing indices
 
       for (int c = 0; c < columns.length; ++c) {
         if (dbHasIndexForColumn.get(columns[c]) != Boolean.TRUE)
           dbCreateIndex(columns[c]);
       }
     }
 
     // Where should we start numbering new records?
 
     if (PoemThread.inSession())
       PoemThread.writeDown();
 
     String sql = 
         "SELECT " + troidColumn.quotedName() +
         " FROM " + quotedName() +
         " ORDER BY " + troidColumn.quotedName() + " DESC";
     try {
       ResultSet maxTroid =
           getDatabase().getCommittedConnection().createStatement().
               executeQuery(sql);
       if (database.logSQL())
         database.log(new SQLLogEvent(sql));
       if (maxTroid.next())
         nextTroid = maxTroid.getInt(1) + 1;
       else
         nextTroid = 0;
     }
     catch (SQLException e) {
       throw new SQLSeriousPoemException(e);
     }
   }
 
   protected void init() {
   }
 }
