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
 
 import java.text.DateFormat;
 import java.util.Enumeration;
 import java.util.Vector;
 import java.util.Map;
 import java.io.ByteArrayOutputStream;
 import java.io.PrintStream;
 import org.melati.util.Transaction;
 import org.melati.util.Transactioned;
 import org.melati.util.MappedEnumeration;
 import org.melati.util.FlattenedEnumeration;
 import org.melati.util.MelatiLocale;
 
 /**
  * The object representing a single table row; this is the <B>PO</B> in POEM!
  * <p>
  * Instances are also used to represent selection criteria  
  * in the dialect of SQL of this DBMS.
  * Features have now been added specifically to support this but
  * this functionality might later be factored out into separate
  * types specifically for that purpose.
  *
  * @author WilliamC At paneris.org
  * @author jimw At paneris.org (Representing query constructs)
  */
 
 public class Persistent extends Transactioned implements Cloneable, Persistable {
   private Table table;
   private Integer troid;        // null if a floating object
   private AccessToken clearedToken;
   private boolean
       knownCanRead = false, knownCanWrite = false, knownCanDelete = false;
 
   /**
    * Might this object have as yet unsaved modifications?
    * <p>
    * This is set to true when a write lock is obtained and this
    * happens when a value is assigned to a column, except when an
    * "unsafe" method is used.
    * <p>
    * It is set to false when this is written to the database,
    * even if not yet committed.
    */
   boolean dirty = false;
 
   private static final int NONEXISTENT = 0, EXISTENT = 1, DELETED = 2;
   private int status = NONEXISTENT;
 
   private Object[] extras = null;
 
   /**
    * Constructor.
    * @param table the table of the Persistent
    * @param troid its Table Row Object Id
    */
   public Persistent(Table table, Integer troid) {
     super(table.getDatabase());
     this.table = table;
     this.troid = troid;
   }
 
 
   /**
    * Constructor.
    */
   public Persistent() {
   }
 
   // 
   // --------
   //  Status
   // --------
   // 
 
   final void setStatusNonexistent() {
     status = NONEXISTENT;
   }
 
   final void setStatusExistent() {
     status = EXISTENT;
   }
 
   /**
    * @return whether this object has been deleted
    */
   public final boolean statusNonexistent() {
     return status == NONEXISTENT;
   }
 
   /**
    * @return whether this object has been deleted
    */
   public final boolean statusExistent() {
     return status == EXISTENT;
   }
 
   // 
   // ***************
   //  Transactioned
   // ***************
   // 
 
   /**
    * Throws an exception if this Persistent has a null troid.
    */
   private void assertNotFloating() {
     if (troid == null)
       throw new InvalidOperationOnFloatingPersistentPoemException(this);
   }
 
   /**
    * Throws an exception if this Persistent has a status of DELETED.
    */
   private void assertNotDeleted() {
     if (status == DELETED)
       throw new RowDisappearedPoemException(this);
   }
 
   /**
   * Called if not uptodate.
    * 
    * @see org.melati.util.Transactioned#load(org.melati.util.Transaction)
    */
   protected void load(Transaction transaction) {
     if (troid == null)
       throw new InvalidOperationOnFloatingPersistentPoemException(this);
 
     table.load((PoemTransaction)transaction, this);
     // table will clear our dirty flag and set status
   }
 
   /**
    * Whether we are up to date with respect to current Transaction.
    * <p>
    * Return the inheritted validity flag.
    * {@inheritDoc}
    * @see org.melati.util.Transactioned#upToDate(org.melati.util.Transaction)
    */
   protected boolean upToDate(Transaction transaction) {
     return valid;
   }
 
   /**
    * Write the persistent to the database if this might be necessary.
    * <p>
    * It may be necessary if field values have been set since we last
    * did a write i.e. this persistent is dirty.
    * It will not be necessary if this persistent is deleted.
    * An exception will occur if it does not exist in the database.
    */
   protected void writeDown(Transaction transaction) {
     if (status != DELETED) {
       assertNotFloating();
       table.writeDown((PoemTransaction)transaction, this);
       // table will clear our dirty flag
     }
   }
 
   protected void writeLock(Transaction transaction) {
     if (troid != null) {
       super.writeLock(transaction);
       assertNotDeleted();
       dirty = true;
       table.notifyTouched((PoemTransaction)transaction, this);
     }
   }
 
   /**
    * This is just to make this method available to <TT>Table</TT>.
    */
   protected void readLock(Transaction transaction) {
     if (troid != null) {
       super.readLock(transaction);
       assertNotDeleted();
     }
   }
 
   /**
    * Previously deletion was treated as non-rollbackable, 
    * as deleteAndCommit was the only deletion mechanism. 
    * 
    * @see org.melati.util.Transactioned#commit(org.melati.util.Transaction)
    */
   protected void commit(Transaction transaction) {
     //if (status != DELETED) {
       assertNotFloating();
       super.commit(transaction);
     //}
   }
 
   protected void rollback(Transaction transaction) {
     //if (status != DELETED) {
     assertNotFloating();
     if (status == DELETED)
       status = EXISTENT;
     super.rollback(transaction);
     //}
   }
 
   // 
   // ************
   //  Persistent
   // ************
   // 
 
  /** 
   * A convenience method to mark this object as persistent.
   */
   public void makePersistent() {
     getTable().create(this);
   }
   
   synchronized Object[] extras() {
     if (extras == null)
       extras = new Object[table.extrasCount()];
     return extras;
   }
 
  /**
   * The Table from which the object comes, 
   * complete with metadata.
    * @return the Table
    */
   public final Table getTable() {
     return table;
   }
 
   synchronized void setTable(Table table, Integer troid) {
     setTransactionPool(table.getDatabase());
     this.table = table;
     this.troid = troid;
   }
 
 
  /**
   * @return The database from which the object comes.  <I>I.e.</I>
   * <TT>getTable().getDatabase()</TT>.
   */
   public final Database getDatabase() {
     return table.getDatabase();
   }
 
   /**
    * @return The Table Row Object Id for this Persistent.
    * 
    * FIXME This shouldn't be public because we don't in principle want people
    * to know even the troid of an object they aren't allowed to read.  However,
    * I think this information may leak out elsewhere.
    */
   public final Integer troid() {
     return troid;
   }
 
   /**
    * The object's troid.
    *
    * @return Every record (object) in a POEM database must have a
    *         troid (table row ID, or table-unique non-nullable integer primary
    *         key), often but not necessarily called <TT>id</TT>, so that it can
    *         be conveniently `named' for retrieval.
    *
    * @exception AccessPoemException
    *                if <TT>assertCanRead</TT> fails
    *
    * @see Table#getObject(java.lang.Integer)
    * @see #assertCanRead()
    */
 
   public final Integer getTroid() throws AccessPoemException {
     assertCanRead();
     return troid();
   }
 
   // 
   // ----------------
   //  Access control
   // ----------------
   // 
 
   protected void existenceLock(SessionToken sessionToken) {
     super.readLock(sessionToken.transaction);
   }
 
   protected void readLock(SessionToken sessionToken)
       throws AccessPoemException {
     assertCanRead(sessionToken.accessToken);
     readLock(sessionToken.transaction);
   }
 
   protected void writeLock(SessionToken sessionToken)
       throws AccessPoemException {
     if (troid != null)
       assertCanWrite(sessionToken.accessToken);
     writeLock(sessionToken.transaction);
   }
 
   protected void deleteLock(SessionToken sessionToken)
       throws AccessPoemException {
     if (troid != null)
       assertCanDelete(sessionToken.accessToken);
     writeLock(sessionToken.transaction);
   }
 
   /**
    * Lock without actually reading.
    */
   public void existenceLock() {
     existenceLock(PoemThread.sessionToken());
   }
 
   /**
    * Check if we may read this object and then lock it.
    * @throws AccessPoemException if current AccessToken does not give read Capability
    */
   protected void readLock() throws AccessPoemException {
     readLock(PoemThread.sessionToken());
   }
 
   /**
    * Check if we may write to this object and then lock it.
    * @throws AccessPoemException if current AccessToken does not give write Capability
    */
   protected void writeLock() throws AccessPoemException {
     writeLock(PoemThread.sessionToken());
   }
 
   /**
    * The capability required for reading the object's field values.  This is
    * used by <TT>assertCanRead</TT> (unless that's been overridden) to obtain a
    * <TT>Capability</TT> for comparison against the caller's
    * <TT>AccessToken</TT>.
    *
    * @return the capability specified by the record's <TT>canread</TT> field, 
    *         or <TT>null</TT> if it doesn't have one or its value is SQL
    *         <TT>NULL</TT>
    *
    * @see #assertCanRead
    */
 
   protected Capability getCanRead() {
     Column crCol = getTable().canReadColumn();
     return
         crCol == null ? null :
             (Capability)crCol.getType().cookedOfRaw(crCol.getRaw_unsafe(this));
   }
 
   /**
    * Check that you have read access to the object.  Which is to say: the
    * <TT>AccessToken</TT> associated with the POEM task executing in the
    * running thread confers the <TT>Capability</TT> required for inspecting the
    * object's fields.  The access token is set when the task is invoked using
    * <TT>Database.inSession</TT>.  The capability is determined by
    * <TT>getCanRead</TT>, which by default means the capability defined in the
    * record's <TT>canread</TT> field in the database.  If that's <TT>null</TT>,
    * the table's default <TT>canread</TT> capability is obtained using
    * <TT>getTable().getDefaultCanRead()</TT>.  If that is <TT>null</TT> as
    * well, access is granted unconditionally.
    *
    * <P>
    *
    * Although this check can in theory be quite time-consuming, in practice
    * this isn't a problem, because the most recent access token for which the
    * check succeeded is cached; repeat accesses from within the same 
    * transaction are therefore quick.
    *
    * <P>
    *
    * Application programmers can override this method to implement their own
    * programmatic access policies.  For instance, POEM's own <TT>TableInfo</TT>
    * class overrides it with an empty method in order to disable all read
    * protection on <TT>TableInfo</TT> objects.  More interestingly, you could
    * implement a check that depends on the values of the object's fields:
    * for example, you could allow read access to an invoice record to its
    * issuing and receiving parties.
    *
    * @param token       the access token on the basis of which readability is
    *                    being claimed
    *
    * @exception AccessPoemException if the check fails
    *
    * @see #getCanRead
    * @see Database#inSession
    * @see Table#getDefaultCanRead
    *
    * @todo Ensure token is not stale
    */
 
   public void assertCanRead(AccessToken token)
       throws AccessPoemException {
     // FIXME!!!! this is wrong because token could be stale ...
     if (!(clearedToken == token && knownCanRead) && troid != null) {
       Capability canRead = getCanRead();
       if (canRead == null)
         canRead = getTable().getDefaultCanRead();
       if (canRead != null) {
         if (!token.givesCapability(canRead))
           throw new ReadPersistentAccessPoemException(this, token, canRead);
         if (clearedToken != token) {
           knownCanWrite = false;
           knownCanDelete = false;
         }
         clearedToken = token;
         knownCanRead = true;
       }
     }
   }
 
   /**
    * @throws AccessPoemException if current accessToken does not grant read capability
    */
   public final void assertCanRead() throws AccessPoemException {
     assertCanRead(PoemThread.accessToken());
   }
 
   /**
    * @return Whether the object is readable by current AccessToken
    *
    * @see #assertCanRead()
    */
 
   public final boolean getReadable() {
     try {
       assertCanRead();
       return true;
     }
     catch (AccessPoemException e) {
       return false;
     }
   }
 
   /**
    * The capability required for writing the object's field values.  This is
    * used by <TT>assertCanWrite</TT> (unless that's been overridden) to obtain 
    * a <TT>Capability</TT> for comparison against the caller's
    * <TT>AccessToken</TT>.
    *
    * @return the capability specified by the record's <TT>canwrite</TT> field,
    *         or <TT>null</TT> if it doesn't have one or its value is SQL
    *         <TT>NULL</TT>
    *
    * @see #assertCanWrite
    */
   protected Capability getCanWrite() {
     Column cwCol = getTable().canWriteColumn();
     return
         cwCol == null ? null :
             (Capability)cwCol.getType().cookedOfRaw(cwCol.getRaw_unsafe(this));
   }
 
   /**
    * Check that you have write access to the object.  Which is to say: the
    * <TT>AccessToken</TT> associated with the POEM task executing in the
    * running thread confers the <TT>Capability</TT> required for updating the
    * object's fields.  The remarks made about <TT>assertCanRead</TT> apply
    * (<I>mutatis mutandis</I>) here as well.
    *
    * @see #assertCanRead()
    * @see #getCanWrite
    * @see Table#getDefaultCanWrite
    * 
    * @todo Ensure token is not stale
    */
 
   public void assertCanWrite(AccessToken token)
       throws AccessPoemException {
     // FIXME!!!! this is wrong because token could be stale ...
     if (!(clearedToken == token && knownCanWrite) && troid != null) {
       Capability canWrite = getCanWrite();
       if (canWrite == null)
         canWrite = getTable().getDefaultCanWrite();
       if (canWrite != null) {
         if (!token.givesCapability(canWrite))
           throw new WritePersistentAccessPoemException(this, token, canWrite);
         if (clearedToken != token) {
           knownCanRead = false;
           knownCanDelete = false;
         }
         clearedToken = token;
         knownCanWrite = true;
       }
     }
   }
 
   /**
    * @throws AccessPoemException if current accessToken does not grant wraite capability
    */
   public final void assertCanWrite() throws AccessPoemException {
     assertCanWrite(PoemThread.accessToken());
   }
 
   /**
    * The capability required for deleting the object.  This is
    * used by <TT>assertCanDelete</TT> (unless that's been overridden) 
    * to obtain a <TT>Capability</TT> for comparison against the caller's
    * <TT>AccessToken</TT>.
    *
    * @return the capability specified by the record's <TT>candelete</TT> field,
    *         or <TT>null</TT> if it doesn't have one or its value is SQL
    *         <TT>NULL</TT>
    *
    * @see #assertCanDelete
    */
   protected Capability getCanDelete() {
     Column cwCol = getTable().canDeleteColumn();
     return
         cwCol == null ? null :
             (Capability)cwCol.getType().cookedOfRaw(cwCol.getRaw_unsafe(this));
   }
 
   /**
    * Check that you have delete access to the object.  Which is to say: the
    * <TT>AccessToken</TT> associated with the POEM task executing in the
    * running thread confers the <TT>Capability</TT> required for updating the
    * object's fields.  The remarks made about <TT>assertCanRead</TT> apply
    * (<I>mutatis mutandis</I>) here as well.
    *
    * @see #assertCanRead()
    * @see #getCanDelete
    * @see Table#getDefaultCanDelete
    *
    * @todo Ensure token is not stale
    */
 
   public void assertCanDelete(AccessToken token)
       throws AccessPoemException {
     // FIXME!!!! this is wrong because token could be stale ...
     if (!(clearedToken == token && knownCanDelete) && troid != null) {
       Capability canDelete = getCanDelete();
       if (canDelete == null)
         canDelete = getTable().getDefaultCanDelete();
       if (canDelete != null) {
         if (!token.givesCapability(canDelete))
           throw new DeletePersistentAccessPoemException(this, token, canDelete);
         if (clearedToken != token) {
           knownCanRead = false;
           knownCanWrite = false;
         }
         clearedToken = token;
         knownCanDelete = true;
       }
     }
   }
 
   /**
    * @throws AccessPoemException if current accessToken does not grant delete capability
    */
   public final void assertCanDelete() throws AccessPoemException {
     assertCanDelete(PoemThread.accessToken());
   }
 
   /**
    * The capability required to select the object.
    * <p>
    * There is no <code>assertCanSelect()</code> yet because I don't understand
    * this stale token stuff!
    *
    * @return the capability the user needs to select this record
    * @todo document use-case or delete
    */
   protected Capability getCanSelect() {
     Column c = getTable().canSelectColumn();
     return c == null ? null :
         (Capability)c.getType().cookedOfRaw(c.getRaw_unsafe(this));
   }
 
   /**
    * Check that you have create access to the object.  Which is to say: the
    * <TT>AccessToken</TT> associated with the POEM task executing in the
    * running thread confers the <TT>Capability</TT> required for creating the
    * object. The capability is determined solely by <TT>getCanCreate</TT>
    * from the table. Unlike <TT>assertCanRead</TT> and <TT>assertCanWrite</TT>
    * there is no idea of having a default <TT>Capability</TT> defined 
    * in the table which could be overriden by a <TT>canwrite</TT> field
    * in the persistent (since the persisent has not yet been been written).
    *
    * <P>
    *
    * Application programmers can override this method to implement their own
    * programmatic access policies.
    *
    * @see #assertCanRead()
    * @see #assertCanWrite()
    * @see Table#getCanCreate
    */
 
   public void assertCanCreate(AccessToken token) {
     Capability canCreate = getTable().getCanCreate();
     if (canCreate != null && !token.givesCapability(canCreate))
       throw new CreationAccessPoemException(getTable(), token, canCreate);
   }
 
   /**
    * @throws AccessPoemException if current accessToken does not grant create capability
    */
   public final void assertCanCreate() throws AccessPoemException {
     assertCanCreate(PoemThread.accessToken());
   }
 
 
   // 
   // ============================
   //  Reading and writing fields
   // ============================
   // 
 
   // 
   // ------
   //  Raws
   // ------
   // 
 
   /**
    * The `identifying value' of one of the object's fields.  This is the value
    * which is actually stored in the database, given to you as a basic Java
    * type; currently, the only fields for which this differs from the `true
    * value' returned from <TT>getCooked</TT> are reference fields with type
    * <TT>ReferencePoemType</TT>.
    *
    * <P>
    *
    * If the field <TT><I>baz</I></TT> is defined in the DSD as part of a table
    * called <TT><I>foo</I></TT>, then the table's records will be represented
    * by an application-specialised subclass of <TT>Persistent</TT> called
    * <TT><I>Foo</I></TT> which provides a typed <TT>get<I>Baz</I></TT> method.
    * So the easiest way to be sure of your types is to predeclare any fields
    * you use in the DSD, use the typed field-access methods, and let the
    * compiler take the strain.  When working with generic <TT>Persistent</TT>s,
    * you probably want to use <TT>getField</TT>.
    *
    * <P>
    *
    * The value returned is relative to the transaction associated with the
    * calling thread, as set up by <TT>Database.inSession</TT>.  This means that
    * you never see the value of a field change in your transaction because of
    * another transaction's activities, unless you do a
    * <TT>PoemThread.commit()</TT> or a <TT>PoemThread.rollback()</TT>.  If you
    * need to, you can store a <TT>Persistent</TT> in a permanent data structure
    * and access it in different sessions over time---or even from concurrently
    * running sessions, though this may slow down access checking; each
    * transaction will see the value it expects.
    *
    * @param name        the name of the field (<I>i.e.</I> the name of the
    *                    column in the RDBMS and DSD)
    *
    * @return The field's `identifying value'; this will be a <TT>String</TT>,
    *         <TT>Boolean</TT>, <TT>Integer</TT>, <TT>Double</TT> or
    *         <TT>Date</TT> as appropriate.  If the field is a reference field,
    *         the result is an <TT>Integer</TT> giving the troid of the referee.
    *         If you want references to be resolved transparently to
    *         <TT>Persistent</TT>s, use <TT>getCooked</TT>.  
    *         If you want a string representation of the field, 
    *         use <TT>getRawString</TT> or <TT>getCookedString</TT>.
    *
    * @exception NoSuchColumnPoemException
    *                if the field named doesn't exist
    * @exception AccessPoemException
    *                if the calling thread doesn't have read access to the
    *                object (see <TT>assertCanRead</TT>)
    *
    * @see #getCooked
    * @see #getRawString
    * @see #getCookedString
    * @see #getField
    * @see Database#inSession
    * @see PoemThread#commit
    * @see PoemThread#rollback
    * @see #assertCanRead()
    */
   public Object getRaw(String name)
       throws NoSuchColumnPoemException, AccessPoemException {
     return getTable().getColumn(name).getRaw(this);
   }
 
   /**
    * A string representation of the `identifying value' of one of the object's
    * fields.  The value returned is relative to the transaction associated with
    * the calling thread, as set up by <TT>Database.inSession</TT>: see the
    * remarks made about <TT>getRaw</TT>.
    *
    * @param name        the name of the field (<I>i.e.</I> the name of the
    *                    column in the RDBMS and DSD)
    *
    * @return Roughly, the string the underlying RDBMS would display if asked
    *         to show the field's value.  If you want reference fields to be
    *         represented by their referee's <TT>displayString()</TT> (by
    *         default, its primary display field) rather than by its troid, use
    *         <TT>getCookedString</TT>.  If you want the field's value as an
    *         appropriate Java type like <TT>Integer</TT>, use <TT>getRaw</TT>
    *         or <TT>getCooked</TT>---or an equivalent, but type-safe, method
    *         derived from the DSD.
    *
    * @exception NoSuchColumnPoemException
    *                if the field named doesn't exist
    * @exception AccessPoemException
    *                if the calling thread doesn't have read access to the
    *                object (see <TT>assertCanRead</TT>)
    *
    * @see #getCookedString
    * @see #getRaw
    * @see #getCooked
    * @see #assertCanRead()
    */
 
   public final String getRawString(String name)
       throws AccessPoemException, NoSuchColumnPoemException {
     Column column = getTable().getColumn(name);
     return column.getType().stringOfRaw(column.getRaw(this));
   }
 
   /**
    * Set the `identifying value' of one of the record's fields.  This is the
    * value which is actually stored in the database, given by you as a basic
    * Java type; currently, the only fields for which this differs from the
    * `true value' expected by <TT>setCooked</TT> are reference fields with type
    * <TT>ReferencePoemType</TT>.
    *
    * <P>
    *
    * If the field <TT><I>baz</I></TT> is defined in the DSD as part of a table
    * called <TT><I>foo</I></TT>, then the table's records will be represented
    * by an application-specialised subclass of <TT>Persistent</TT> called
    * <TT><I>Foo</I></TT> which provides a typed <TT>set<I>Baz</I></TT>
    * method.  So the easiest way to be sure of your types is to predeclare any
    * fields you use in the DSD, use the typed field-access methods, and let the
    * compiler take the strain.  When working with generic <TT>Persistent</TT>s,
    * you probably mean <TT>setRawString</TT> anyway.
    *
    * <P>
    *
    * The change you make to the field's value will only be visible to the
    * calling thread, until it successfully completes the task started by
    * <TT>Database.inSession</TT>, or does an explicit
    * <TT>PoemThread.commit()</TT>.  Up to that point the change can be undone
    * by calling <TT>PoemThread.rollback()</TT>, and will be undone
    * automatically if the task terminates with an uncaught exception.
    *
    * <P>
    *
    * In fact, your changes are not written down to the database, even relative
    * to an uncommitted transaction, until it's actually necessary.  So multiple
    * calls to <TT>setRaw</TT> and relatives will not cause multiple SQL
    * <TT>UPDATE</TT>s to be issued.
    *
    * @param name        the name of the field (<I>i.e.</I> the name of the
    *                    column in the RDBMS and DSD)
    *
    * @param raw       The new value for the field: a <TT>String</TT>,
    *                    <TT>Boolean</TT>, <TT>Integer</TT>, <TT>Double</TT> or
    *                    <TT>Date</TT> as appropriate.  If the field is a
    *                    reference field: an <TT>Integer</TT> giving the troid
    *                    of the referee.  If you want to pass referees as actual
    *                    <TT>Persistent</TT>s, use <TT>setCooked</TT>.  If you
    *                    want to set the field from a string representation
    *                    (<I>e.g.</I> typed in by the user), use
    *                    <TT>setRawString</TT>.
    *
    * @exception NoSuchColumnPoemException
    *                if the field named doesn't exist
    * @exception AccessPoemException
    *                if the calling thread doesn't have write access to the
    *                object (see <TT>assertCanWrite</TT>)
    * @exception ValidationPoemException
    *                if <TT>raw</TT> is not a valid value for the field
    *                (<I>e.g.</I> a string is too long)
    *
    * @see #setCooked
    * @see #setRawString
    * @see #assertCanWrite()
    * @see Database#inSession
    * @see PoemThread#commit
    * @see PoemThread#rollback
    */
 
   public void setRaw(String name, Object raw)
       throws NoSuchColumnPoemException, AccessPoemException,
              ValidationPoemException {
     getTable().getColumn(name).setRaw(this, raw);
   }
 
   /**
    * Set the `identifying value' of one of the record's fields from a string
    * representation.  The remarks about sessions (transactions) and DSD-derived
    * type-safe methods made for <TT>setRaw</TT> apply here too.
    *
    * @param name        the name of the field (<I>i.e.</I> the name of the
    *                    column in the RDBMS and DSD)
    *
    * @param string      A string that will be parsed to obtain the new value
    *                    for the field.  If it's a reference field, this should
    *                    be a decimal representation of the referee's troid.  If
    *                    you want to set fields to values defined by appropriate
    *                    Java types, use <TT>setRaw</TT> or <TT>setCooked</TT>.
    *
    * @exception NoSuchColumnPoemException
    *                if the field named doesn't exist
    * @exception AccessPoemException
    *                if the calling thread doesn't have write access to the
    *                object (see <TT>assertCanWrite</TT>)
    * @exception ParsingPoemException
    *                if <TT>string</TT> doesn't parse as a value of the
    *                appropriate type
    * @exception ValidationPoemException
    *                if <TT>string</TT> parses to an invalid value for the field
    *                (<I>e.g.</I> it's too wide)
    *
    * @see #setRaw
    * @see #setCooked
    * @see #assertCanWrite()
    */
 
   public final void setRawString(String name, String string)
       throws NoSuchColumnPoemException, AccessPoemException,
              ParsingPoemException, ValidationPoemException {
     Column column = getTable().getColumn(name);
     column.setRaw(this, column.getType().rawOfString(string));
   }
 
   // 
   // --------
   //  Values
   // --------
   // 
 
   /**
    * The `true value' of one of the object's fields.  This is the
    * fully-interpreted value rather than the one actually stored in the
    * database; currently, the only fields for which this differs from the
    * `identifying value' return from <TT>getRaw</TT> are reference fields
    * with type <TT>ReferencePoemType</TT>.
    *
    * <P>
    *
    * The value returned is relative to the transaction associated with the
    * calling thread, as set up by <TT>Database.inSession</TT>: see the remarks
    * made about <TT>getRaw</TT>.
    *
    * <P>
    *
    * The easiest way to be sure of your types is to predeclare any fields you
    * use in the DSD, or use <TT>getField</TT>.  Again, see the remarks made
    * about <TT>getRaw</TT>.
    *
    * @return The field's `true value'; this will be a <TT>String</TT>,
    *         <TT>Boolean</TT>, <TT>Integer</TT>, <TT>Double</TT>,
    *         <TT>Date</TT>, or, if the field is a reference field, a
    *         <TT>Persistent</TT> representing the referee.  If you just want to
    *         see referees' troids, use <TT>getRaw</TT>.  If you want a string
    *         representation of the field, use <TT>getRawString</TT> or
    *         <TT>getCookedString</TT>.
    *
    * @exception NoSuchColumnPoemException
    *                if the field named doesn't exist
    * @exception AccessPoemException
    *                if the calling thread doesn't have read access to the
    *                object (see <TT>assertCanRead</TT>)
    *
    * @see #getRaw
    * @see #getRawString
    * @see #getCookedString
    * @see #getField
    * @see #assertCanRead()
    */
 
   public Object getCooked(String name)
       throws NoSuchColumnPoemException, AccessPoemException {
     return getTable().getColumn(name).getCooked(this);
   }
 
   /**
    * A string representation of the `true value' of one of the object's fields.
    * For example the return value for the user table's category field would be 
    * User. 
    * The value returned is relative to the transaction associated with the
    * calling thread, as set up by <TT>Database.inSession</TT>: see the remarks
    * made about <TT>getRaw</TT>.
    *
    * @param name        the name of the field (<I>i.e.</I> the name of the
    *                    column in the RDBMS and DSD)
    * @param locale      A MelatiLocale eg MelatiLocale.HERE
    * @param style       A date format
    *
    * @return The string the underlying RDBMS would display if asked
    *         to show the field's value, except that reference fields are
    *         represented by their referee's <TT>displayString()</TT> (by
    *         default, its primary display field) rather than by its troid.  If
    *         you want to see troids instead, use <TT>getRawString</TT>.  If
    *         you want the field's value as an appropriate Java type like
    *         <TT>Integer</TT>, use <TT>getRaw</TT> or <TT>getCooked</TT>---or
    *         an equivalent, but type-safe, method derived from the DSD.
    *
    * @exception NoSuchColumnPoemException
    *                if the field named doesn't exist
    * @exception AccessPoemException
    *                if the calling thread doesn't have read access to the
    *                object (see <TT>assertCanRead</TT>)
    *
    * @see #getRawString
    * @see #getRaw
    * @see #getCooked
    * @see #assertCanRead()
    * @see #displayString
    */
 
   public final String getCookedString(String name, MelatiLocale locale,
                                      int style)
       throws NoSuchColumnPoemException, AccessPoemException {
     Column column = getTable().getColumn(name);
     return column.getType().stringOfCooked(column.getCooked(this),
                                           locale, style);
   }
 
   /**
    * Set the `true value' of one of the record's fields.  Like
    * <TT>setRaw</TT>, but reference fields expect to see a
    * <TT>Persistent</TT> representing their new referee rather than an
    * <TT>Integer</TT> specifying its troid.  The remarks about sessions
    * (transactions) and DSD-derived type-safe methods made for
    * <TT>setRaw</TT> apply here too.
    *
    * @param name        the name of the field (<I>i.e.</I> the name of the
    *                    column in the RDBMS and DSD)
    *
    * @param cooked      the new value for the field: a <TT>String</TT>,
    *                    <TT>Boolean</TT>, <TT>Integer</TT>, <TT>Double</TT>,
    *                    <TT>Date</TT> or, for a reference field, a
    *                    <TT>Persistent</TT>.  If you want to pass referees as
    *                    troids, use <TT>setRaw</TT>.  If you want to set the
    *                    field from a string representation (<I>e.g.</I> typed
    *                    in by the user), use <TT>setRawString</TT>.
    *
    * @exception NoSuchColumnPoemException
    *                if the field named doesn't exist
    * @exception AccessPoemException
    *                if the calling thread doesn't have write access to the
    *                object (see <TT>assertCanWrite</TT>)
    * @exception ValidationPoemException
    *                if <TT>cooked</TT> is not a valid value for the field
    *                (<I>e.g.</I> a string is too long)
    *
    * @see #setRaw
    * @see #setRawString
    * @see #assertCanWrite()
    */
 
   public void setCooked(String name, Object cooked)
       throws NoSuchColumnPoemException, ValidationPoemException,
              AccessPoemException {
     getTable().getColumn(name).setCooked(this, cooked);
   }
 
   // 
   // --------
   //  Fields
   // --------
   // 
 
   /**
    * The value of one of the object's fields, wrapped up with type information
    * sufficient for rendering it.  Basically, value plus name plus type.  This
    * is the form in which Melati's templating facilities expect to receive
    * values for displaying them or creating input boxes.
    *
    * <P>
    *
    * If the field <TT><I>baz</I></TT> is defined in the DSD as part of a table
    * called <TT><I>foo</I></TT>, then the table's records will be represented
    * by an application-specialised subclass of <TT>Persistent</TT> called
    * <TT><I>Foo</I></TT> which provides a <TT>get<I>Baz</I>Field</TT> method.
    *
    * @param name column name
    * @return the Field of that name
    * @throws NoSuchColumnPoemException if there is no column of that name
    * @throws AccessPoemException if the current AccessToken does not grant access capability
    * @see org.melati.template.MarkupLanguage#input(org.melati.poem.Field)
    */
   public final Field getField(String name)
       throws NoSuchColumnPoemException, AccessPoemException {
     return getTable().getColumn(name).asField(this);
   }
 
   /**
    * Create Fields from Columns. 
    * 
    * @param columns an Enumeration of Columns
    * @return an Enumeration of Fields 
    */
   public Enumeration fieldsOfColumns(Enumeration columns) {
     final Persistent _this = this;
     return
         new MappedEnumeration(columns) {
           public Object mapped(Object column) {
             return ((Column)column).asField(_this);
           }
         };
   }
 
   /**
    * The values of all the object's fields, wrapped up with type information
    * sufficient for rendering them.
    *
    * @return an <TT>Enumeration</TT> of <TT>Field</TT>s
    */
 
   public Enumeration getFields() {
     return fieldsOfColumns(getTable().columns());
   }
 
   /**
    * The values of all the object's fields designated for inclusion in full
    * record displays, wrapped up with type information sufficient for rendering
    * them.
    *
    * @return an <TT>Enumeration</TT> of <TT>Field</TT>s
    * @see DisplayLevel#record
    */
 
   public Enumeration getRecordDisplayFields() {
     return fieldsOfColumns(getTable().getRecordDisplayColumns());
   }
 
   /**
    * All fields at the detailed display level in display order.
    *
    * @return an <TT>Enumeration</TT> of <TT>Field</TT>s
    * @see DisplayLevel#detail
    */
   public Enumeration getDetailDisplayFields() {
     return fieldsOfColumns(getTable().getDetailDisplayColumns());
   }
 
   /**
    * All fields at the summary display level in display order.
    *
    * @return an <TT>Enumeration</TT> of <TT>Field</TT>s
    * @see DisplayLevel#summary
    */
   public Enumeration getSummaryDisplayFields() {
     return fieldsOfColumns(getTable().getSummaryDisplayColumns());
   }
 
   /**
    * @return an <TT>Enumeration</TT> of searchable <TT>Field</TT>s
    */
   public Enumeration getSearchCriterionFields() {
     return fieldsOfColumns(getTable().getSearchCriterionColumns());
   }
 
   /**
    * @return the Primary Display Column as a Field
    */
   public Field getPrimaryDisplayField() {
     return getTable().displayColumn().asField(this);
   }
 
   // 
   // ==================
   //  Other operations
   // ==================
   // 
 
   /**
    * Delete the object.  Before the record is deleted from the database, POEM
    * checks to see if it is the target of any reference fields.  What happens
    * in this case is determined by the <TT>integrityfix</TT> setting of the
    * referring column, unless that's overridden via the
    * <TT>integrityFixOfColumn</TT> argument.  By default, a
    * <TT>DeletionIntegrityPoemException</TT> is thrown, but this behaviour can
    * be changed through the admin interface.
    *
    * @see IntegrityFix
    * @see PoemThread#commit
    *
    * @param integrityFixOfColumn
    *            A map from {@link Column} to {@link IntegrityFix} which says
    *            how referential integrity is to be maintained for each column
    *            that can refer to the object being deleted.  May be
    *            <TT>null</TT> to mean `empty'.  If a column isn't mentioned,
    *            the default behaviour for the column is used.  (The default 
    *            is {@link StandardIntegrityFix#prevent}.)
    */
   public void delete(Map integrityFixOfColumn) {
     
     assertNotFloating();
 
     deleteLock(PoemThread.sessionToken());
 
     Enumeration columns = getDatabase().referencesTo(getTable());
     Vector refEnumerations = new Vector();
 
     // FIXME These integrity fixes may result in calls to postWrite()
     // unless the deletion status is changed first. Not sure what the
     // side effects would be. JimW
     while (columns.hasMoreElements()) {
       Column column = (Column)columns.nextElement();
 
       IntegrityFix fix;
       try {
         fix = integrityFixOfColumn == null ?
                 null : (IntegrityFix)integrityFixOfColumn.get(column);
       }
       catch (ClassCastException e) {
         throw new AppBugPoemException(
             "integrityFixOfColumn argument to Persistent.deleteAndCommit " +
                 "is meant to be a Map from Column to IntegrityFix",
             e);
       }
 
       if (fix == null)
         fix = column.getIntegrityFix();
 
       refEnumerations.addElement(
           fix.referencesTo(this, column, column.selectionWhereEq(troid()),
                            integrityFixOfColumn));
 
     }
 
     Enumeration refs = new FlattenedEnumeration(refEnumerations.elements());
 
     if (refs.hasMoreElements())
       throw new DeletionIntegrityPoemException(this, refs);
 
     delete_unsafe();
   }
 
   /**
    * Delete without access checks.
    */
   public void delete_unsafe() {
     assertNotFloating();
     SessionToken sessionToken = PoemThread.sessionToken();
     deleteLock(sessionToken);
     try {
       status = DELETED;
       table.delete(troid(), sessionToken.transaction);
     } catch (PoemException e) {
       status = EXISTENT;
       throw e;
     }
   }
 
  
   /**
    * Delete this persistent, with default integrity checks, 
    * ie disallow deletion if object referred to by others.
    */
   public final void delete() {
     delete(null);
   }
 
   /**
    * Delete the object, with even more safety checks for referential integrity.
    * As {@link #delete(java.util.Map)}, but waits for exclusive access to the
    * database before doing the delete, and commits the session immediately
    * afterwards.  
    * <p>
    * This used to be the only deletion entry point allowed, but
    * now we ensure that the possible race condition involving new
    * pointers to the deleted object created during the deletion process is
    * covered. So it is recommended to use {@link #delete(java.util.Map)}
    * unless you really want this functionality.
    *
    */
   public void deleteAndCommit(Map integrityFixOfColumn)
       throws AccessPoemException, DeletionIntegrityPoemException {
 
     getDatabase().beginExclusiveLock();
     try {
       delete(integrityFixOfColumn);
       PoemThread.commit();
     }
     catch (RuntimeException e) {
       PoemThread.rollback();
       throw e;
     }
     finally {
       getDatabase().endExclusiveLock();
     }
   }
 
   /**
    * Convenience method with default integrity fix. 
    * 
    * @throws AccessPoemException
    * @throws DeletionIntegrityPoemException
    */
   public final void deleteAndCommit()
       throws AccessPoemException, DeletionIntegrityPoemException {
     deleteAndCommit(null);
   }
 
   /**
    * Create a new object like this one.
    * This Persistent must not be floating.
    * 
    * @return A floating clone
    */
   public Persistent duplicated() throws AccessPoemException {
     assertNotFloating();
     assertNotDeleted();
     return (Persistent)clone();
   }
 
   /**
    * Create a new persistent like this one, regardless of 
    * whether this Persistent has been written to the dbms yet.
    * @return A floating clone
    */
   public Persistent duplicatedFloating() throws AccessPoemException {
     return (Persistent)clone();
   }
 
   // 
   // ===========
   //  Utilities
   // ===========
   // 
 
   /**
    * A string briefly describing the object for diagnostic purposes.  The name
    * of its table and its troid.
    * {@inheritDoc}
    * @see java.lang.Object#toString()
    */
   public String toString() {
     if (getTable() == null) {
        return "null/" + troid();      
     }
     return getTable().getName() + "/" + troid();
   }
 
   /**
    * A string describing the object for the purposes of rendering it in lists
    * presented to the user.  Unless overridden, this returns the value picked
    * out by the designated `primary display column' of the table from which the
    * object comes.  If there is no such column, the object's troid is returned
    * (as a decimal string).
    * 
    * @param locale our locale
    * @param style 
    *      a DateFormat (only applicable to those rare objects whose summary column is a date)
    * @return the String to display
    * @throws AccessPoemException 
    *         if current User does not have viewing {@link Capability}
    */
   public String displayString(MelatiLocale locale, int style)
       throws AccessPoemException {
     Column displayColumn = getTable().displayColumn();
     return displayColumn.getType().stringOfCooked(displayColumn.getCooked(this),
                                                   locale, style);
   }
 
   /**
    * Defaults to DateFormat.MEDIUM.
    * @return Default String for display.
    * 
    * @throws AccessPoemException 
    *         if current User does not have viewing {@link Capability}
    */
   public String displayString(MelatiLocale locale) 
       throws AccessPoemException {
     return displayString(locale, DateFormat.MEDIUM);
   }
   /**
    * @return Default String for display.
    * 
    * @throws AccessPoemException 
    *         if current User does not have viewing {@link Capability}
    */
   public String displayString() 
       throws AccessPoemException {
     return displayString(MelatiLocale.HERE, DateFormat.MEDIUM);
   }
 
   // 
   // ===============
   //  Support stuff
   // ===============
   // 
 
   /**
    * {@inheritDoc}
    * @see java.lang.Object#hashCode()
    */
   public final int hashCode() {
     if (troid == null)
       throw new InvalidOperationOnFloatingPersistentPoemException(this);
 
     return getTable().hashCode() + troid().intValue();
   }
 
   /**
    * {@inheritDoc}
    * @see java.lang.Object#equals(java.lang.Object)
    */
   public final boolean equals(Object object) {
     if (object == null || !(object instanceof Persistent))
       return false;
     else {
       Persistent other = (Persistent)object;
       return other.troid() == troid() &&
              other.getTable() == getTable();
     }
   }
 
   /**
    * {@inheritDoc}
    * @see org.melati.util.Transactioned#invalidate()
    */
   public synchronized void invalidate() {
     assertNotFloating();
     super.invalidate();
     extras = null;
   }
 
   /**
    * {@inheritDoc}
    * @see java.lang.Object#clone()
    */
   protected Object clone() {
     // to clone it you have to be able to read it
     assertCanRead();
     Persistent it;
     try {
       it = (Persistent)super.clone();
     }
     catch (CloneNotSupportedException e) {
       throw new UnexpectedExceptionPoemException(e);
     }
 
     it.extras = (Object[])extras().clone();
     it.reset();
     it.troid = null;
     it.status = NONEXISTENT;
 
     return it;
   }
 
   /**
    * @return the dump String
    */
   public String dump() {
     ByteArrayOutputStream baos = new ByteArrayOutputStream();
     PrintStream ps = new PrintStream(baos);
     dump(ps);
     return baos.toString();
   }
 
   /**
    * Dump to a PrintStream.
    * @param p the PrintStream to dump to
    */
   public void dump(PrintStream p) {
     p.println(getTable().getName() + "/" + troid());
     for (Enumeration f = getRecordDisplayFields(); f.hasMoreElements();) {
       p.print("  ");
       ((Field)f.nextElement()).dump(p);
       p.println();
     }
   }
 
   /**
    * Called after this persistent is written to the database
    * on being inserted or modified.
    * <p>
    * This is called after postInsert() or postModify().
    * <p>
    * This is low level and there is a limit to what you can
    * do here without causing infinitely recursive calls.
    */
   public void postWrite() {
   }
 
   /**
    * Called after this persistent is written to the database
    * for the first time.
    * <p>
    * This is low level and there is a limit to what you can
    * do here without causing infinitely recursive calls.
    */
   public void postInsert() {
   }
 
   /**
    * Called after this persistent is updated and written to the
    * database replacing the existing record it represents.
    * <p>
    * Not called when it is written to the database for the
    * first time.
    * <p>
    * This is low level and there is a limit to what you can
    * do here without causing infinitely recursive calls.
    */
   public void postModify() {
   }
 
   /**
    * Optionally called before an instance is edited by the user.
    * <p>
    * Used in conjunction with {@link #postEdit(boolean)} and a
    * {@link Persistent.Saved} this helps enforce data model
    * constraints based on the columns that have changed.
    * <p>
    * See {@link #postEdit(boolean)} for additional comments.
    * However, it is not called when a newly created row is
    * edited.
    */
   public void preEdit() {
   }
 
   /**
    * Optionally called after this instance is edited by a user.
    * <p>
    * Unlike {@link #postModify()} and {@link #postInsert()} this
    * is not called during
    * {@link #writeDown(Transaction)} but can be called by
    * applications such as {@link org.melati.admin#Admin} after
    * individual field edits by the user have been reflected in
    * the instance.
    * <p>
    * It can be be overridden to enforce data model constraints
    * such as validity of columns relative to other columns.
    * These will be enforced when the admin system is used.
    * <p>
    * This is a higher level method than {@link #postModify()}
    * so is less likely to lead to infinite recursion or other
    * such problems.
    * <p>
    * Sorry for the lack of signature consistency with the
    * lower level methods but I got tired of having to call
    * my own application specific common method.
    *
    * @param creating Are we in the process of creating a new record?
    */
   public void postEdit(boolean creating) {
   }
 
   // 
   // ================================
   // Use to Represent Query Contructs
   // ================================
   //
 
   /**
    * Return a SELECT query to count rows matching criteria represented
    * by this object.
    *
    * @param includeDeleted whether to include soft deleted records
    * @param excludeUnselectable Whether to append unselectable exclusion SQL 
    * @return an SQL query string
    */
   public String countMatchSQL(boolean includeDeleted,
                               boolean excludeUnselectable) {
     return getTable().countSQL(
       fromClause(),
       getTable().whereClause(this),
       includeDeleted, excludeUnselectable);
   }
 
   /**
    * Return an SQL FROM clause for use when selecting rows using criteria
    * represented by this object.
    * <p>
    * By default just the table name is returned, quoted as necessary for
    * the DBMS.
    * One way of changing this is to override {@link #otherMatchTables()}.
    * <p>
    * Subtypes must ensure the result is compatible with the
    * result of {@link Table #appendWhereClause(StringBuffer, Persistent)}.
    * @return an SQL snippet 
    */
   public String fromClause() {
     String result = getTable().quotedName();
     Table[] other = otherMatchTables();
     for (int i = 0; i < other.length; i++) {
       result += ", " + other[i].quotedName();
     }
     return result;
   }
 
   /**
    * Return any other tables involved in the SELECT query for which
    * this represents criteria.
    * <p>
    * Note that this does not support aliases, unless we implement
    * these through a subtype of {@link Table}.
    * @return an empty Array
    */
   public Table[] otherMatchTables() {
     return new Table[0];
   }
 
   /**
    * Member object for saving a copy of this object and comparing
    * it with the original.
    * <p>
    * Consider this subject to review.
    * JimW re-uses it but there is the possibility that something
    * similar can be implemented in terms of lower level
    * functionality.
    * <p>
    * Typical usage is that you save the object before updating
    * it to reflect user edits and then ensure integrity of the
    * data model by performing model specific actions based on
    * the differences.
    * <p>
    * Used in conjunction with {@link Persistent#preEdit()} and
    * {@link Persistent#postEdit(boolean)} it is possible to ensure that
    * details of the data model defined in this way are
    * respected by Melati admin and other apps.
    *
    * @todo Consider requirements for locking and avoiding
    * deadlock here and generally when updating one row as a
    * result of edits to another.
    */
   public class Saved {
 
     Persistent copy = null;
 
     /**
      * Save a copy of the persistent.
      * <p>
      * It is an bug if there is already one saved.
      */
     public void save() {
       if (copy != null) {
         throw new IllegalStateException("Bug in caller");
       }
       ensureSaved();
     }
 
     /**
      * Save a copy of the persistent if this has not been done.
      */
     public final void ensureSaved() {
       if (copy == null) {
         copy = (Persistent)Persistent.this.clone();
       }
     }
 
     /**
      * Substitute a brand new instance for a saved one.
      * <p>
      * This is possibly a way of simplifying
      * {@link Persistent#postEdit(boolean)}
      * when a new object has been created and edited.
      * Call this and from then on much of the processing will be the
      * same as when an existing object is edited.
      */
     public void substituteNew() {
       if (copy != null) {
         throw new IllegalStateException("Bug in caller");
       }
       copy = getTable().newPersistent();
     }
 
     /**
      * Discard any saved copy.
      */
     public void discard() {
       copy = null;
     }
 
     /**
      * @return a reference to the saved copy or null.
      */
     public Persistent get() {
       return copy;
     }
 
     /**
      * Has the given column been changed?
      * @return whether the column has changed
      */
     public boolean isDifferent(Column column) {
       return ! column.asField(Persistent.this).sameRawAs(column.asField(copy));
     }
 
   }
 
 }
