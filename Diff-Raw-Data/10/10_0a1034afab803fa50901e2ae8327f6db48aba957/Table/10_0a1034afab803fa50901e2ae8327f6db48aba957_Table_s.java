 package org.melati.poem;
 
 import org.melati.util.*;
 import java.sql.*;
 import java.util.*;
 
 /**
  * A table in a POEM database.  This is the minimal set of methods available;
  * if the table is defined in the data structure definition under the name
  * <TT><I>foo</I></TT>, there will be an application-specialised
  * <TT>Table</TT> subclass, called <TT><I>Foo</I>Table</TT> (and available as
  * <TT>get<I>Foo</I>Table</TT> from the application-specialised
  * <TT>Database</TT> subclass) which has extra, typed methods for retrieving
  * the application-specialised objects in the table, and methods for accessing
  * the table's predefined <TT>Column</TT>s.
  */
 
 public class Table {
 
   public static final int CACHE_LIMIT_DEFAULT = 100;
 
   private final Table _this = this;
 
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
   private Column displayColumn = null;
 
   private String defaultOrderByClause = null;
   private Column[] recordDisplayColumns = null;
   private Column[] summaryDisplayColumns = null;
   private Column[] searchCriterionColumns = null;
 
   private long nextSerial = 0L;
   private PoemFloatingVersionedObject serial;
 
   private PreparedSelection allTroids = null;
   private Hashtable cachedSelections = null;
 
   public Table(Database database, String name,
                DefinitionSource definitionSource)
       throws InvalidNamePoemException {
     this.database = database;
     this.name = name;
     this.quotedName = database.quotedName(name);
     this.definitionSource = definitionSource;
     serial =
         new PoemFloatingVersionedObject(database) {
           protected boolean upToDate(Session session, Version version) {
 	    return true;
 	  }
 
 	  protected Version backingVersion(Session session) {
 	    return new SerialledVersion(nextSerial++);
 	  }
         };
   }
 
   void postInitialise() {
     database.getColumnInfoTable().addListener(
         new TableListener() {
           public void notifyTouched(PoemSession session, Table table,
                                     Integer troid, Data data) {
             _this.notifyColumnInfo((ColumnInfoData)data);
           }
 
           public void notifyUncached(Table table) {
             _this.clearColumnInfoCaches();
           }
         });
   }
 
   private void clearColumnInfoCaches() {
     defaultOrderByClause = null;
     recordDisplayColumns = null;
     summaryDisplayColumns = null;
     searchCriterionColumns = null;
   }
 
   void notifyColumnInfo(ColumnInfoData data) {
     // FIXME data == null means deleted: effect is too broad really
     if (data == null ||
         ((ColumnInfoData)data).tableinfo.equals(tableInfoID()))
       clearColumnInfoCaches();
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
 
   final int getColumnsCount() {
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
 
   private String defaultOrderByClause() {
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
               return ((Column)column).quotedName();
             }
           });
 
       if (clause.equals(""))
         clause = displayColumn().quotedName();
 
       defaultOrderByClause = clause;
     }
 
     return clause;
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
 
   /**
    * The table's columns designated for display in a record, in display order.
    *
    * @return an <TT>Enumeration</TT> of <TT>Column</TT>s
    * @see Column
    */
 
   public final Enumeration getRecordDisplayColumns() {
     Column[] columns = recordDisplayColumns;
 
     if (columns == null)
       recordDisplayColumns = columns = columnsWhere("recorddisplay");
 
     return new ArrayEnumeration(columns);
   }
 
   /**
    * The table's columns designated for display in a record summary, in display
    * order.
    *
    * @return an <TT>Enumeration</TT> of <TT>Column</TT>s
    * @see Column
    */
 
   public final Enumeration getSummaryDisplayColumns() {
     Column[] columns = summaryDisplayColumns;
 
     if (columns == null)
       summaryDisplayColumns = columns = columnsWhere("summarydisplay");
 
     return new ArrayEnumeration(columns);
   }
 
   /**
    * The table's columns designated for use as search criteria, in display
    * order.
    *
    * @return an <TT>Enumeration</TT> of <TT>Column</TT>s
    * @see Column
    */
 
   public final Enumeration getSearchCriterionColumns() {
     Column[] columns = searchCriterionColumns;
 
     if (columns == null)
       searchCriterionColumns = columns = columnsWhere("searchcriterion");
 
     return new ArrayEnumeration(columns);
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
    * @see Column#getPrimaryDisplay()
    * @see ReferencePoemType#_stringOfValue(Object)
    */
 
   public final Column displayColumn() {
     return displayColumn == null ? troidColumn : displayColumn;
   }
 
   final void setDisplayColumn(Column column) {
     displayColumn = column;
   }
 
   /**
    * The troid (<TT>id</TT>) of the table's entry in the <TT>tableinfo</TT>
    * table.  It will always have one (except during initialisation, which the
    * application programmer will never see).
    */
 
   final Integer tableInfoID() {
     return info == null ? null : info.troid();
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
     sqb.append("CREATE TABLE " + quotedName + " (");
     for (int c = 0; c < columns.length; ++c) {
       if (c != 0) sqb.append(", ");
       sqb.append(columns[c].quotedName() + " " +
                  columns[c].getType().sqlDefinition());
     }
 
     sqb.append(")");
 
     dbModifyStructure(sqb.toString());
   }
 
   private void dbAddColumn(Column column) {
     dbModifyStructure(
         "ALTER TABLE " + quotedName +
         " ADD COLUMN " + column.quotedName() +
         " " + column.getType().sqlDefinition());
   }
 
   private void dbCreateIndex(Column column) {
     if (column.getIndexed())
       dbModifyStructure(
           "CREATE " + (column.getUnique() ? "UNIQUE " : "") + "INDEX " +
           database._quotedName(name + "_" + column.getName() + "_index") +
           " ON " + quotedName + " " +
           "(" + column.quotedName() + ")");
   }
 
   // 
   // -------------------------------
   //  Standard `PreparedStatement's
   // -------------------------------
   // 
 
   private PreparedStatement simpleInsert(Connection connection) {
     StringBuffer sql = new StringBuffer();
     sql.append("INSERT INTO " + quotedName + " (");
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
     sql.append(" FROM " + quotedName +
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
     sql.append("UPDATE " + quotedName + " SET ");
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
   // -------------------------
   //  Session-specific things
   // -------------------------
   // 
 
   private class SessionStuff {
     PreparedStatement insert, modify, get;
     SessionStuff(Connection connection) {
       insert = _this.simpleInsert(connection);
       modify = _this.simpleModify(connection);
       get = _this.simpleGet(connection);
     }
   }
 
   private CachedIndexFactory sessionStuffs = new CachedIndexFactory() {
     public Object reallyGet(int index) {
       return new SessionStuff(database.session(index).getConnection());
     }
   };
 
   private SessionStuff committedSessionStuff = null;
 
   private synchronized SessionStuff getCommittedSessionStuff() {
     if (committedSessionStuff == null)
       committedSessionStuff =
           new SessionStuff(database.getCommittedConnection());
     return committedSessionStuff;
   }
 
   // 
   // --------------------
   //  Loading and saving
   // --------------------
   // 
 
   private Data dbData(PreparedStatement select, Integer troid) {
     try {
       synchronized (select) {
         select.setInt(1, troid.intValue());
         ResultSet rs = select.executeQuery();
         if (database.logSQL)
           database.log(new SQLLogEvent(select.toString()));
 
         if (!rs.next())
           return null;
 
         Data data = newData();
         for (int c = 0; c < columns.length; ++c)
           columns[c].load(rs, c + 1, data);
 
         if (rs.next())
           throw new DuplicateTroidPoemException(this, troid);
 
         return data;
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
 
   Data dbData(PoemSession session, Integer troid) {
     return dbData(session == null ?
                       getCommittedSessionStuff().get :
                       ((SessionStuff)sessionStuffs.get(session.index())).get,
                   troid);
   }
 
   private void modify(PoemSession session, Integer troid, Data data) {
     PreparedStatement modify =
         ((SessionStuff)sessionStuffs.get(session.index())).modify;
     synchronized (modify) {
       for (int c = 0; c < columns.length; ++c)
         columns[c].save(data, modify, c + 1);
       try {
         modify.setInt(columns.length + 1, troid.intValue());
         modify.executeUpdate();
       }
       catch (SQLException e) {
         throw new SQLSeriousPoemException(e);
       }
       if (database.logSQL)
         database.log(new SQLLogEvent(modify.toString()));
     }
   }
 
   private void insert(PoemSession session, Integer troid, Data data) {
     PreparedStatement insert =
         ((SessionStuff)sessionStuffs.get(session.index())).insert;
     synchronized (insert) {
       for (int c = 0; c < columns.length; ++c)
         columns[c].save(data, insert, c + 1);
       try {
         insert.executeUpdate();
       }
       catch (SQLException e) {
         throw new PreparedSQLSeriousPoemException(insert, e);
       }
       if (database.logSQL)
         database.log(new SQLLogEvent(insert.toString()));
     }
   }
 
   void delete(Integer troid, PoemSession session) {
     String sql =
         "DELETE FROM " + quotedName +
         " WHERE " + troidColumn.quotedName() + " = " +
         troid.toString();
 
     try {
       Connection connection;
       if (session == null)
         connection = getDatabase().getCommittedConnection();
       else {
         session.writeDown();
         connection = session.getConnection();
       }
 
       connection.createStatement().executeUpdate(sql);
       if (database.logSQL)
         database.log(new SQLLogEvent(sql));
     }
     catch (SQLException e) {
       throw new ExecutingSQLPoemException(sql, e);
     }
   }
 
   void writeDown(PoemSession session, Integer troid, Data data) {
     // no race, provided that the one-thread-per-session parity is maintained
 
     if (data.dirty) {
       troidColumn.setIdent(data, troid);
 
       if (data.exists)
 	modify(session, troid, data);
       else {
 	insert(session, troid, data);
 	data.exists = true;
       }
 
       data.dirty = false;
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
 
   void uncacheContents() {
     cache.uncacheContents();
     serial.uncacheContents();
     TableListener[] listeners = this.listeners;
     for (int l = 0; l < listeners.length; ++l)
       listeners[l].notifyUncached(this);
   }
 
   void trimCache(int maxSize) {
     cache.trim(maxSize);
   }
 
   CachedVersionedRow versionedRow(Integer troid) {
     synchronized (cache) {
       CachedVersionedRow is = (CachedVersionedRow)cache.get(troid);
       if (is == null) {
         is = new CachedVersionedRow(this, troid);
         cache.put(is);
       }
       return is;
     }
   }
 
   long serial(Session session) {
     return ((SerialledVersion)serial.versionForReading(session)).serial;
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
     Persistent persistent = newPersistent();
     persistent.init(versionedRow(troid));
 
     try {
       persistent.dataUnchecked(PoemThread.session());
     }
     catch (RowDisappearedPoemException e) {
       throw new NoSuchRowPoemException(this, troid);
     }
 
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
 
     if (orderByClause == null || orderByClause.equals(""))
       orderByClause = defaultOrderByClause();
 
     // FIXME must work in some kind of limit
       
     return
         "SELECT " + troidColumn.quotedName() +
         " FROM " + quotedName +
         (whereClause == null || whereClause.equals("") ?
              "" : " WHERE " + whereClause) +
         // actually orderByClause is never null (since fallback is id)
         (orderByClause == null || orderByClause.equals("") ?
              "" : " ORDER BY " + orderByClause);
   }
 
   private ResultSet selectionResultSet(
       String whereClause, String orderByClause, boolean includeDeleted,
       PoemSession session)
           throws SQLPoemException {
 
     String sql = selectionSQL(whereClause, orderByClause, includeDeleted);
             
     try {
       Connection connection;
       if (session == null)
         connection = getDatabase().getCommittedConnection();
       else {
         session.writeDown();
         connection = session.getConnection();
       }
 
       ResultSet rs = connection.createStatement().executeQuery(sql);
       if (database.logSQL)
         database.log(new SQLLogEvent(sql));
       return rs;
     }
     catch (SQLException e) {
       throw new ExecutingSQLPoemException(sql, e);
     }
   }
 
   Enumeration troidSelection(
       String whereClause, String orderByClause,
       boolean includeDeleted, PoemSession session) {
     ResultSet them = selectionResultSet(whereClause, orderByClause,
                                         includeDeleted, session);
     return
         new ResultSetEnumeration(them) {
           public Object mapped(ResultSet rs) throws SQLException {
             return new Integer(rs.getInt(1));
           }
         };
   }
 
   protected void rememberAllTroids(boolean flag) {
     // FIXME can't actually cancel since would have to remove PS from observers
 
     if (flag && allTroids == null)
       allTroids = new PreparedSelection(this, null, null);
   }
 
   protected void setCacheLimit(Integer limit) {
     cache.setSize(limit == null ? CACHE_LIMIT_DEFAULT : limit.intValue());
   }
 
   /**
    * A <TT>SELECT</TT>ion of troids of objects from the table meeting given
    * criteria.
    *
    * @return an <TT>Enumeration</TT> of <TT>Integer</TT>s, which can be mapped
    *         onto <TT>Persistent</TT> objects using <TT>getObject</TT>;
    *         or you can just use <TT>selection</TT>
    *
    * @see #select(java.lang.String, boolean)
    * @see #getObject(java.lang.Integer)
    * @see #selection(java.lang.String, boolean)
    */
 
   Enumeration troidSelection(String whereClause, String orderByClause,
                              boolean includeDeleted)
       throws SQLPoemException {
     PreparedSelection allTroids = this.allTroids;
     if (allTroids != null &&
         whereClause == null && orderByClause == null && !includeDeleted)
       return allTroids.troids();
     else
       return troidSelection(whereClause, orderByClause, includeDeleted,
                             PoemThread.session());
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
 
   /**
    * A <TT>SELECT</TT>ion of objects from the table meeting given criteria,
    * possibly including those flagged as deleted.
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
 
   public PageEnumeration selection(
       String whereClause, String orderByClause, boolean includeDeleted,
       int pageStart, int pageSize)
           throws SQLPoemException {
     // FIXME do this more sensibly where SQL permits
     return new DumbPageEnumeration(
         selection(whereClause, orderByClause, includeDeleted),
         pageStart, pageSize, 200);
   }
 
   public int count(String whereClause)
       throws SQLPoemException {
     String sql =
         "SELECT count(*) FROM " + quotedName +
         (whereClause == null || whereClause.equals("") ? "" :
              " WHERE " + whereClause);
 
     try {
       PoemSession session = PoemThread.session();
       Connection connection;
       if (session == null)
         connection = getDatabase().getCommittedConnection();
       else {
         session.writeDown();
         connection = session.getConnection();
       }
 
       ResultSet rs = connection.createStatement().executeQuery(sql);
       if (database.logSQL)
         database.log(new SQLLogEvent(sql));
       rs.next();
       return rs.getInt(1);
     }
     catch (SQLException e) {
       throw new ExecutingSQLPoemException(sql, e);
     }
   }
 
   public boolean exists(String whereClause) throws SQLPoemException {
     // FIXME use EXISTS
     return count(whereClause) > 0;
   }
 
   /**
    * FIXME you can't search for NULLs ...
    */
 
   public void appendWhereClause(StringBuffer clause, Data data) {
     Column[] columns = this.columns;
     boolean hadOne = false;
     for (int c = 0; c < columns.length; ++c) {
       Column column = columns[c];
       Object ident = column.getIdent(data);
       if (ident != null) {
         if (hadOne)
           clause.append(" AND ");
         else
           hadOne = true;
 
         clause.append(column.quotedName());
         clause.append(" = ");
         clause.append(column.getType().quotedIdent(ident));
       }
     }
   }
 
   public String whereClause(Data data) {
     StringBuffer clause = new StringBuffer();
     appendWhereClause(clause, data);
     return clause.toString();
   }
 
   public String cnfWhereClause(Enumeration datas) {
     StringBuffer clause = new StringBuffer();
 
     boolean hadOne = false;
     while (datas.hasMoreElements()) {
       if (hadOne)
         clause.append(" OR ");
       else
         hadOne = true;
       clause.append("(");
       appendWhereClause(clause, (Data)datas.nextElement());
       clause.append(")");
     }
 
     return clause.toString();
   }
 
   public boolean exists(Data data) {
     return exists(whereClause(data));
   }
 
   /**
    * All the objects in the table which refer to a given object.  If none of
    * the table's columns are reference columns, the <TT>Enumeration</TT>
    * returned will obviously be empty.  This is used by
    * <TT>Persistent.delete()</TT> to determine whether deleting an object would
    * destroy the integrity of any references.  It is not guaranteed to be
    * particularly quick to execute!
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
 
   // 
   // ----------
   //  Creation
   // ----------
   // 
 
   private void validate(Data data) throws FieldContentsPoemException {
     for (int c = 0; c < columns.length; ++c) {
       Column column = columns[c];
       try {
         column.getType().assertValidIdent(column.getIdent(data));
       }
       catch (Exception e) {
         throw new FieldContentsPoemException(column, e);
       }
     }
   }
 
   private int nextTroid = -1;
 
   synchronized private Integer nextTroid() {
     if (nextTroid == -1)
       throw new PoemBugPoemException();
     return new Integer(nextTroid++);
   }
 
   final int troidBound() {
     if (nextTroid == -1)
       throw new PoemBugPoemException();
     return nextTroid;
   }
 
   // FIXME don't use the Data version 'cos it messes up triggers ...
 
   private Persistent create(Object initOrData)
       throws AccessPoemException, ValidationPoemException,
              InitialisationPoemException {
 
     SessionToken sessionToken = PoemThread.sessionToken();
 
     Capability canCreate = getCanCreate();
     if (canCreate != null &&
         !sessionToken.accessToken.givesCapability(canCreate))
       throw new CreationAccessPoemException(this, sessionToken.accessToken,
                                             canCreate);
 
     Integer troid = nextTroid();
 
     Data data;
     if (initOrData instanceof Data)
       data = (Data)initOrData;
     else
       data = newData();
 
     troidColumn.setIdent(data, troid);
     if (deletedColumn != null)
       deletedColumn.setIdent(data, Boolean.FALSE);
     data.exists = false;
 
     ConstructionVersionedRow dummyVersionedRow =
         new ConstructionVersionedRow(this, troid, data, sessionToken.session);
     Persistent object = newPersistent();
     object.initForConstruct(dummyVersionedRow, sessionToken.accessToken);
 
     if (initOrData instanceof Initialiser)
       // Let the user do their worst.
       ((Initialiser)initOrData).init(object);
 
     // Are the values they have put in legal; is the result something they
     // could have created by writing into a record; does the DB pick up any
     // inconsistencies like duplicated unique fields?
 
     try {
       validate(data);
       object.assertCanWrite(data, sessionToken.accessToken);
       writeDown(sessionToken.session, troid, data);
     }
     catch (Exception e) {
       throw new InitialisationPoemException(this, e);
     }
 
     // OK, it worked.  Plug the object into the cache.  FIXME this will result
     // in it getting writeDown-ed again but it will have been marked not dirty
     // so it won't cause DB activity.
 
     CachedVersionedRow versionedRow = versionedRow(troid);
     versionedRow.setVersion(sessionToken.session, data);
     object.init(versionedRow);
     return object;
   }
 
   /**
    * Create a new object (record) in the table.  FIXME don't use this because
    * it bypasses any extra logic the programmer may have put on `set' methods.
    */
 
   Persistent create(Data data) 
       throws AccessPoemException, ValidationPoemException,
              InitialisationPoemException {
     return create((Object)data);
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
       throws CreationAccessPoemException,
              AccessPoemException, ValidationPoemException,
              InitialisationPoemException {
     return create((Object)initialiser);
   }
 
   /**
    * A freshly minted <TT>Data</TT> object for the table.  These represent the
    * actual underlying field values of the objects in the table, but you don't
    * in general have to worry about them.  This method is overridden in
    * application-specialised <TT>Table</TT> subclasses derived from the Data
    * Structure Definition.
    *
    * @see User#_newData()
    */
 
   protected Data _newData() {
     return new Data();
   }
 
   public final Data newData() {
     Data them = _newData();
     them.extras = new Object[getExtrasCount()];
     return them;
   }
 
   /**
    * A freshly minted <TT>Persistent</TT> object for the table.  You don't ever
    * have to call this and there is no point in doing so.  This method is
    * overridden in application-specialised <TT>Table</TT> subclasses derived
    * from the Data Structure Definition.
    */
 
   protected Persistent newPersistent() {
     return new Persistent();
   }
 
   /**
    * The number of `extra' (non-DSD-defined) columns in the table.
    */
 
   int getExtrasCount() {
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
    * @see Persistent#getCanRead(org.melati.poem.Data)
    */
 
   public final Capability getDefaultCanRead() {
     return info.getDefaultcanread();
   }
 
   /**
    * The capability required for updating records in the table, unless
    * overridden in the record itself.  This simply comes from the table's
    * record in the <TT>tableinfo</TT> table.
    *
    * @see Persistent#getCanWrite(org.melati.poem.Data)
    */
 
   public final Capability getDefaultCanWrite() {
     return info.getDefaultcanwrite();
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
 
   /**
    * Notify the table that one if its records is about to be changed in a
    * session.  You can (with care) use this to support cacheing of
    * frequently-used facts about the table's records.  For instance,
    * <TT>GroupMembershipTable</TT> and <TT>GroupCapabilityTable</TT> override
    * this to inform <TT>UserTable</TT> that its cache of users' capabilities
    * has become invalid.
    *
    * @param session     the session in which the change will be made
    * @param troid       the troid of the record which has been changed
    * @param data        the new values of the record's fields
    *
    * @see GroupMembershipTable#notifyTouched
    */
 
   protected void notifyTouched(PoemSession session, Integer troid, Data data) {
 
     // Because `serial' is itself an AbstractVersionedObject, the Right Thing
     // will happen when the session is committed or rolled back.
 
     ((SerialledVersion)serial.versionForWriting(session)).serial = nextSerial++;
 
     TableListener[] listeners = this.listeners;
     for (int l = 0; l < listeners.length; ++l)
       listeners[l].notifyTouched(session, this, troid, data);
   }
 
   // 
   // -----------
   //  Structure
   // -----------
   // 
 
   public Column addColumnAndCommit(ColumnInfo info) throws PoemException {
     Column column = ExtraColumn.from(this, info, extrasIndex++,
                                      DefinitionSource.runtime);
     column.setColumnInfo(info);
     dbAddColumn(column);
     database.beginStructuralModification();
     try {
       synchronized (cache) {    // belt and braces
         uncacheContents();
         defineColumn(column);
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
 
   public PreparedSelection cachedSelection(String whereClause,
                                            String orderByClause) {
     String key = whereClause + "/" + orderByClause;
     PreparedSelection them = (PreparedSelection)cachedSelections.get(key);
     if (them == null) {
       PreparedSelection newThem =
           new PreparedSelection(this, whereClause, orderByClause);
       synchronized (cachedSelections) {
 	them = (PreparedSelection)cachedSelections.get(key);
 	if (them == null)
 	  cachedSelections.put(key, them = newThem);
       }
     }
 
     return them;
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
    * @param whereClause		an SQL expression (the bit after the
    *                            <TT>SELECT</TT> ... <TT>WHERE</TT>) for picking
    *                            out the records you want
    *
    * @param orderByClause	a comma-separated list of column names which
    *                            determine the order in which the records are
    *                            presented; if this is <TT>null</TT>, the
    *                            <TT>displayorderpriority</TT> attributes of the
    *                            table's columns determine the order
    *
    * @param nullable		whether to allow a blank <TT>NULL</TT> option
    *                            as the first possibility
    *
    * @param selectedTroid	the troid of the record to which the
    *                            <TT>SELECT</TT> field should initially be set
    *
    * @param name		the HTML name attribute of the field,
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
 
   protected synchronized void defineColumn(Column column)
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
       troidColumn = column;
     }
     else if (column.isDeletedColumn()) {
       if (deletedColumn != null)
         throw new DuplicateDeletedColumnPoemException(this, column);
       deletedColumn = column;
     }
     else {
       PoemType type = column.getType();
       if (type instanceof ReferencePoemType &&
           ((ReferencePoemType)type).targetTable() ==
                database.getCapabilityTable()) {
         if (column.getName().equals("canread"))
           canReadColumn = column;
         else if (column.getName().equals("canwrite"))
           canWriteColumn = column;
       }
     }
 
     column.setTable(this);
     columns = (Column[])ArrayUtils.added(columns, column);
     columnsByName.put(column.getName(), column);
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
 
   TableInfoData defaultTableInfoData() {
     return new TableInfoData(
         getName(), defaultDisplayName(), defaultDisplayOrder(),
         defaultDescription(), defaultCacheLimit(), defaultRememberAllTroids());
   }
 
   void createTableInfo() throws PoemException {
     if (info == null)
       setTableInfo(
           info = (TableInfo)getDatabase().getTableInfoTable().
                      create(defaultTableInfoData()));
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
 
   synchronized void unifyWithDB(ResultSet colDescs)
       throws SQLException, PoemException {
 
     Hashtable dbColumns = new Hashtable();
 
     int dbIndex = 0;
     if (colDescs != null)
       for (; colDescs.next(); ++dbIndex) {
         String colName = colDescs.getString("COLUMN_NAME");
         Column column = (Column)columnsByName.get(colName);
 
         if (column == null) {
           PoemType colType =
               database.defaultPoemTypeOfColumnMetaData(colDescs);
 
           // magically make eligible columns called "id" and "deleted"
           // into designed troid and soft-deleted-flag columns
           // FIXME this may not be a good idea
 
           if (troidColumn == null && colName.equals("id") &&
               colType.canBe(TroidPoemType.it))
             colType = TroidPoemType.it;
 
           if (deletedColumn == null && colName.equals("deleted") &&
               colType.canBe(DeletedPoemType.it))
             colType = DeletedPoemType.it;
 
           column = new ExtraColumn(this, colDescs.getString("COLUMN_NAME"),
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
         else
           column.assertMatches(colDescs);
 
         dbColumns.put(column, Boolean.TRUE);
       }
 
     if (dbIndex == 0)
       // OK, we simply don't exist ...
       dbCreateTable();
     else {
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
               getIndexInfo(null, "", getName(), false, true);
       while (index.next()) {
         try {
           Column column = getColumn(index.getString("COLUMN_NAME"));
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
         " FROM " + quotedName +
         " ORDER BY " + troidColumn.quotedName() + " DESC";
     try {
       ResultSet maxTroid =
           getDatabase().getCommittedConnection().createStatement().
               executeQuery(sql);
       if (database.logSQL)
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
 
   void addListener(TableListener listener) {
     listeners = (TableListener[])ArrayUtils.added(listeners, listener);
   }
 }
