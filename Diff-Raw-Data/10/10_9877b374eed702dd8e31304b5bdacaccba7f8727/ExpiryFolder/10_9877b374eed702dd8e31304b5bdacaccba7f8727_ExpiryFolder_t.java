 /*
  * $Id$
  *
  * Copyright 2003-2005 Wanadoo Nederland B.V.
  * See the COPYRIGHT file for redistribution and use restrictions.
  */
 package org.xins.common.collections.expiry;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 
 import org.xins.common.Log;
 import org.xins.common.MandatoryArgumentChecker;
 import org.xins.common.Utils;
 
 import org.xins.common.text.TextUtils;
 
 /**
  * Expiry folder. Contains values indexed by key. Entries in this folder will
  * expire after a predefined amount of time, unless their lifetime is extended
  * within that timeframe. This is done using the {@link #get(Object)} method.
  *
  * <p>Listeners are supported. Listeners are added using the
  * {@link #addListener(ExpiryListener)} method and removed using the
  * {@link #removeListener(ExpiryListener)} method. If a listener is registered
  * multiple times, it will receive the events multiple times as well. And it
  * will have to be removed multiple times as well.
  *
  * <p>This class is thread-safe.
  *
  * @version $Revision$ $Date$
  * @author Ernst de Haan (<a href="mailto:ernst.dehaan@nl.wanadoo.com">ernst.dehaan@nl.wanadoo.com</a>)
  * @author Anthony Goubard (<a href="mailto:anthony.goubard@nl.wanadoo.com">anthony.goubard@nl.wanadoo.com</a>)
  *
  * @since XINS 1.0.0
  */
 public final class ExpiryFolder
 extends Object {
 
    //-------------------------------------------------------------------------
    // Class fields
    //-------------------------------------------------------------------------
 
    /**
     * The name of this class.
     */
    private static final String CLASSNAME = ExpiryFolder.class.getName();
 
    /**
     * The initial size for the queue of threads waiting to obtain read or
     * write access to a resource.
     */
    private static final int INITIAL_QUEUE_SIZE = 89;
 
    /**
     * The number of instances of this class.
     */
    private static int INSTANCE_COUNT;
 
    /**
     * Lock object for <code>INSTANCE_COUNT</code>.
     */
    private static final Object INSTANCE_COUNT_LOCK = new Object();
 
 
    //-------------------------------------------------------------------------
    // Class functions
    //-------------------------------------------------------------------------
 
    //-------------------------------------------------------------------------
    // Constructor
    //-------------------------------------------------------------------------
 
    /**
     * Constructs a new <code>ExpiryFolder</code>.
     *
     * @param name
     *    description of this folder, to be used in log and exception messages,
     *    not <code>null</code>.
     *
     * @param strategy
     *    the strategy that should be applied, not <code>null</code>.
     *
     * @throws IllegalArgumentException
     *    if <code>name == null || strategy == null</code>.
     *
     * @since XINS 1.0.1
     */
    public ExpiryFolder(String         name,
                        ExpiryStrategy strategy)
    throws IllegalArgumentException {
 
       // Determine instance number
       synchronized (INSTANCE_COUNT_LOCK) {
          _instanceNum = INSTANCE_COUNT++;
       }
 
       final String CONSTRUCTOR_DETAIL = "#"
                                       + _instanceNum
                                       + " [name="
                                       + TextUtils.quote(name)
                                       + "; strategy="
                                       + TextUtils.quote(name)
                                       + ']';
 
       Log.log_1000(CLASSNAME, CONSTRUCTOR_DETAIL);
 
       // Check preconditions
       MandatoryArgumentChecker.check("name", name, "strategy", strategy);
 
       // Initialize fields
       _name                 = name;
       _strategy             = strategy;
       _asString             = CLASSNAME + ' ' + CONSTRUCTOR_DETAIL;
       _recentlyAccessed     = new HashMap(89);
       _recentlyAccessedLock = new Object();
       _slotCount            = strategy.getSlotCount();
       _slots                = new HashMap[_slotCount];
       _lastSlot             = _slotCount - 1;
       _sizeLock             = new Object();
       _listeners            = new ArrayList(5);
 
       // Initialize all slots to a new HashMap
       for (int i = 0; i < _slotCount; i++) {
          _slots[i] = new HashMap(89);
       }
 
       // Notify the strategy that we listen to it
       strategy.folderAdded(this);
 
       Log.log_1002(CLASSNAME, CONSTRUCTOR_DETAIL);
    }
 
    /**
     * Constructs a new <code>ExpiryFolder</code>.
     *
     * @param name
     *    description of this folder, to be used in log and exception messages,
     *    not <code>null</code>.
     *
     * @param strategy
     *    the strategy that should be applied, not <code>null</code>.
     *
     * @param strictChecking
     *    flag that indicates if checking of thread synchronization operations
     *    should be strict or loose.
     *
     * @param maxQueueWaitTime
     *    the maximum time in milliseconds a thread can wait in the queue for
     *    obtaining read or write access to a resource, must be &gt; 0L.
     *
     * @throws IllegalArgumentException
     *    if <code>name             ==    null
     *          || strategy         ==    null
     *          || maxQueueWaitTime &lt;= 0L</code>.
     *
     * @deprecated
     *    Deprecated since XINS 1.0.1.
     *    Use the constructor {@link #ExpiryFolder(String,ExpiryStrategy)}
     *    instead.
     */
    public ExpiryFolder(String         name,
                        ExpiryStrategy strategy,
                        boolean        strictChecking,
                        long           maxQueueWaitTime)
    throws IllegalArgumentException {
       this(name, strategy);
 
       // Check the extra documented precondition
       if (maxQueueWaitTime <= 0L) {
          final String DETAIL = "maxQueueWaitTime ("
                              + maxQueueWaitTime
                              + "L) <= 0L";
          throw new IllegalArgumentException(DETAIL);
       }
    }
 
 
    //-------------------------------------------------------------------------
    // Fields
    //-------------------------------------------------------------------------
 
    /**
     * The instance number of this instance.
     */
    private final int _instanceNum;
 
    /**
     * The name of this expiry folder.
     */
    private final String _name;
 
    /**
     * The strategy used. This field cannot be <code>null</code>.
     */
    private final ExpiryStrategy _strategy;
 
    /**
     * String representation. Cannot be <code>null</code>.
     */
    private final String _asString;
 
    /**
     * The most recently accessed entries. This field cannot be
     * <code>null</code>. The entries in this map will expire after
     * {@link ExpiryStrategy#getTimeOut()} milliseconds, plus at maximum
     * {@link ExpiryStrategy#getPrecision()} milliseconds.
     */
    private volatile HashMap _recentlyAccessed;
 
    /**
     * Number of active slots. Always equals
     * {@link #_slots}<code>.length</code>.
     */
    private final int _slotCount;
 
    /**
     * The index of the last slot. This is always
     * {@link #_slotCount}<code> - 1</code>.
     */
    private final int _lastSlot;
 
    /**
     * Slots to contain the maps with entries that are not the most recently
     * accessed. The further back in the array, the sooner the entries will
     * expire.
     */
    private final HashMap[] _slots;
 
    /**
     * Lock for accessing the <code>_recentlyAccessed</code> field.
     */
    private final Object _recentlyAccessedLock;
 
    /**
     * The size of this folder. If code needs to write to this field, then it
     * should lock on {@link #_sizeLock}.
     */
    private int _size;
 
    /**
     * Lock for writing to the <code>_size</code> field.
     */
    private final Object _sizeLock;
 
    /**
     * The set of listeners. May be empty, but never is <code>null</code>.
     */
    private final ArrayList _listeners;
 
 
    //-------------------------------------------------------------------------
    // Methods
    //-------------------------------------------------------------------------
 
    /**
     * Returns the name given to this expiry folder.
     *
     * @return
     *    the name assigned to this expiry folder, not <code>null</code>.
     */
    public final String getName() {
       return _name;
    }
 
    /**
     * Notifies this map that the precision time frame has passed since the
     * last tick.
     *
     * <p>Entries that are expirable may be removed from this folder.
     */
    void tick() {
 
       final String THIS_METHOD = "tick()";
 
       // Allocate memory for the new map of recently accessed entries outside
       // the synchronized sections
       HashMap newRecentlyAccessed = new HashMap();
 
       // Store the entries that need to be expired in this map
       HashMap toBeExpired;
 
       // Always get the lock for _recentlyAccessed first
       synchronized (_recentlyAccessedLock) {
 
          // Then get the lock for _slots
          synchronized (_slots) {
 
             // Keep a link to the old map with recently accessed elements and
             // then reset _recentlyAccessed
             HashMap oldRecentlyAccessed = _recentlyAccessed;
             _recentlyAccessed           = newRecentlyAccessed;
 
             // Shift the slots
             toBeExpired = _slots[_lastSlot];
             for (int i = _lastSlot; i > 0; i--) {
                _slots[i] = _slots[i - 1];
             }
             _slots[0] = oldRecentlyAccessed;
          }
       }
 
       // Adjust the size
       int toBeExpiredSize = (toBeExpired == null)
                           ? 0
                           : toBeExpired.size();
       if (toBeExpiredSize > 0) {
          int newSize;
          synchronized (_sizeLock) {
             _size -= toBeExpiredSize;
             newSize = _size;
             if (_size < 0) {
                _size = 0;
             }
          }
 
          // If the new size was negative, it has been fixed already, but
          // report it now, outside the synchronized section
          if (newSize < 0) {
             final String DETAIL = "Size of expiry folder \""
                                 + _name
                                 + "\" dropped to "
                                 + newSize
                                 + ", adjusted it to 0.";
             Utils.logProgrammingError(CLASSNAME, THIS_METHOD,
                                       CLASSNAME, THIS_METHOD,
                                       DETAIL);
          }
          Log.log_1400(_asString, toBeExpiredSize, newSize);
       } else {
          Log.log_1400(_asString, 0, _size);
       }
 
       // XXX: Should we do this in a separate thread, so all locks held by the
       //      ExpiryStrategy are released?
 
       // Get a copy of the list of listeners
       List listeners;
       synchronized (_listeners) {
          listeners = (_listeners.size() == 0) ? null : new ArrayList(_listeners);
       }
 
       // Notify all listeners
       if (listeners != null) {
          int count = listeners.size();
 
          // Pass object references to listeners, not Entry objects
          Map refMap = new HashMap();
         Iterator keyIterator = toBeExpired.keySet().iterator();
         while (keyIterator.hasNext()) {
            Object key   = keyIterator.next();
            Entry  entry = (Entry) toBeExpired.get(key);
             if (entry.isExpired()) {
               refMap.put(key, entry.getReference());
             } else {
                final String DETAIL = "Entry marked for expiry should not be expired yet. Key as string is \""
                                    + entry.getReference().toString()
                                    + "\".";
                Utils.logProgrammingError(CLASSNAME, THIS_METHOD,
                                          CLASSNAME, THIS_METHOD,
                                          DETAIL);
             }
          }
 
          if (refMap.size() > 0) {
             Map unmodifiableExpired = Collections.unmodifiableMap(refMap);
             for (int i = 0; i < count; i++) {
                ExpiryListener listener = (ExpiryListener) listeners.get(i);
                listener.expired(this, unmodifiableExpired);
             }
          }
       }
    }
 
    /**
     * Adds the specified object as a listener for expiry events.
     *
     * @param listener
     *    the listener to be registered, cannot be <code>null</code>.
     *
     * @throws IllegalArgumentException
     *    if <code>listener == null</code>.
     */
    public void addListener(ExpiryListener listener)
    throws IllegalArgumentException {
       MandatoryArgumentChecker.check("listener", listener);
 
       synchronized (_listeners) {
          _listeners.add(listener);
       }
    }
 
    /**
     * Removes the specified object as a listener for expiry events.
     *
     * <p>If the listener cannot be found, then nothing happens.
     *
     * @param listener
     *    the listener to be unregistered, cannot be <code>null</code>.
     *
     * @throws IllegalArgumentException
     *    if <code>listener == null</code>.
     */
    public void removeListener(ExpiryListener listener)
    throws IllegalArgumentException {
       MandatoryArgumentChecker.check("listener", listener);
 
       synchronized (_listeners) {
          _listeners.remove(listener);
       }
    }
 
    /**
     * Gets the number of entries.
     *
     * @return
     *    the number of entries in this expiry folder, always &gt;= 0.
     */
    public int size() {
       synchronized (_sizeLock) {
          return _size;
       }
    }
 
    /**
     * Gets the value associated with a key and extends the lifetime of the
     * matching entry, if there was a match.
     *
     * <p>The more recently the specified entry was accessed, the faster the
     * lookup.
     *
     * @param key
     *    the key to lookup, cannot be <code>null</code>.
     *
     * @return
     *    the value associated with the specified key, or <code>null</code> if
     *    and only if this folder does not contain an entry with the specified
     *    key.
     *
     * @throws IllegalArgumentException
     *    if <code>key == null</code>.
     */
    public Object get(Object key) throws IllegalArgumentException {
 
       // Check preconditions
       MandatoryArgumentChecker.check("key", key);
 
       // Search in the recently accessed map first
       Entry entry;
       synchronized (_recentlyAccessedLock) {
          entry = (Entry) _recentlyAccessed.get(key);
 
          // Entry found in recently accessed
          if (entry != null) {
 
             // Entry is already expired, update the map and size and return null
             if (entry.isExpired()) {
                _recentlyAccessed.remove(key);
                synchronized (_sizeLock) {
                   _size--;
                }
                return null;
 
             // Entry is not expired, touch it and return the reference
             } else {
                entry.touch();
                return entry.getReference();
             }
 
          // Not found in recently accessed, look in slots
          } else {
             synchronized (_slots) {
 
                // Go through all slots
                for (int i = 0; i < _slotCount; i++) {
                   entry = (Entry) _slots[i].remove(key);
 
                   if (entry != null) {
 
                      // Entry is already expired, update the map and size and
                      // return null
                      if (entry.isExpired()) {
                         synchronized (_sizeLock) {
                            _size--;
                         }
                         return null;
 
                      // Entry is not expired, touch it, store in the recently
                      // accessed and return the reference
                      } else {
                         entry.touch();
                         _recentlyAccessed.put(key, entry);
                         return entry.getReference();
                      }
                   }
                }
 
                // Nothing found in any of the slots
                return null;
             }
          }
       }
    }
 
    /**
     * Finds the value associated with a key. The lifetime of the matching
     * entry is not extended.
     *
     * <p>The more recently the specified entry was accessed, the faster the
     * lookup.
     *
     * @param key
     *    the key to lookup, cannot be <code>null</code>.
     *
     * @return
     *    the value associated with the specified key, or <code>null</code> if
     *    and only if this folder does not contain an entry with the specified
     *    key.
     *
     * @throws IllegalArgumentException
     *    if <code>key == null</code>.
     */
    public Object find(Object key) throws IllegalArgumentException {
 
       // Check preconditions
       MandatoryArgumentChecker.check("key", key);
 
       Object value;
 
       // Search in the recently accessed map first
       synchronized (_recentlyAccessedLock) {
          value = _recentlyAccessed.get(key);
       }
 
       // If not found, then look in the slots
       if (value == null) {
          synchronized (_slots) {
             for (int i = 0; i < _slotCount && value == null; i++) {
                value = _slots[i].get(key);
             }
          }
       }
 
       if (value == null) {
          return null;
       }
 
       Entry entry = (Entry) value;
       if (entry.isExpired()) {
          return null;
       } else {
          return entry.getReference();
       }
    }
 
    /**
     * Associates the specified key with the specified value.
     *
     * @param key
     *    they key for the entry, cannot be <code>null</code>.
     *
     * @param value
     *    they value for the entry, cannot be <code>null</code>.
     *
     * @throws IllegalArgumentException
     *    if <code>key == null || value == null</code>.
     */
    public void put(Object key, Object value)
    throws IllegalArgumentException {
 
       // Check preconditions
       MandatoryArgumentChecker.check("key", key, "value", value);
 
       // Store the association in the set of recently accessed entries
       synchronized (_recentlyAccessedLock) {
          Entry entry = new Entry(value);
          _recentlyAccessed.put(key, entry);
       }
 
       // Bump the size
       synchronized (_sizeLock) {
          _size++;
       }
    }
 
    /**
     * Removes the specified key from this folder.
     *
     * @param key
     *    the key for the entry, cannot be <code>null</code>.
     *
     * @return
     *    the old value associated with the specified key, or <code>null</code>
     *    if and only if this folder does not contain an entry with the
     *    specified key.
     *
     * @throws IllegalArgumentException
     *    if <code>key == null</code>.
     */
    public Object remove(Object key)
    throws IllegalArgumentException {
 
       // Check preconditions
       MandatoryArgumentChecker.check("key", key);
 
       Object value;
 
       // Remove the key in the set of recently accessed entries
       synchronized (_recentlyAccessed) {
          value = _recentlyAccessed.remove(key);
       }
 
       // If not found, then look in the slots
       if (value == null) {
          synchronized (_slots) {
             for (int i = 0; i < _slotCount && value == null; i++) {
                value = _slots[i].remove(key);
             }
          }
       }
 
       if (value == null) {
          return null;
       }
 
       // Decrease the size
       synchronized (_sizeLock) {
          _size--;
       }
 
       Entry entry = (Entry) value;
       if (entry.isExpired()) {
          return null;
       } else {
          return entry.getReference();
       }
    }
 
    /**
     * Copies the entries of this ExpiryFolder into another one.
     * This method does not perform a deep copy, so if a key is added or
     * removed, both folders will be modified.
     *
     * @param newFolder
     *    the new folder where the entries should be copied into,
     *    cannot be <code>null</code>, cannot be <code>this</code>.
     *
     * @throws IllegalArgumentException
     *    if <code>newFolder == null</code> or <code>newFolder == this</code>
     *    or the precision is the newFolder is not the same as for this folder.
     */
    public void copy(ExpiryFolder newFolder)
    throws IllegalArgumentException {
 
       // Check preconditions
       MandatoryArgumentChecker.check("newFolder", newFolder);
       if (newFolder == this) {
          // TODO: Log programming error
          throw new IllegalArgumentException("The folder can not be copied into itself.");
       }
       if (newFolder.getStrategy().getPrecision() != getStrategy().getPrecision()) {
          // TODO: Log programming error
          throw new IllegalArgumentException("The folders must have the same precision.");
       }
 
       synchronized (_recentlyAccessedLock) {
          synchronized (newFolder._recentlyAccessedLock) {
             synchronized (_slots) {
                synchronized (newFolder._slots) {
 
                   // Copy the recentlyAccessed
                   newFolder._recentlyAccessed = new HashMap(_recentlyAccessed);
 
                   // Copy the slots
                   for (int i = 0; i < _slotCount && i < newFolder._slotCount; i++) {
                      newFolder._slots[i] = new HashMap(_slots[i]);
                   }
 
                   // Copy the size
                   synchronized (newFolder._sizeLock) {
                      newFolder._size = _size;
                   }
                }
             }
          }
       }
    }
 
    /**
     * Returns the strategy associated with this folder
     *
     * @return
     *    the strategy, never <code>null</code>.
     */
    public ExpiryStrategy getStrategy() {
       return _strategy;
    }
 
    /**
     * Returns a textual representation of this object.
     *
     * @return
     *    a textual representation of this <code>ExpiryFolder</code>, which
     *    includes the name.
     */
    public String toString() {
       return _asString;
    }
 
 
    //-------------------------------------------------------------------------
    // Inner classes
    //-------------------------------------------------------------------------
 
    /**
     * Entry in an expiry folder. Combination of the referenced object and a
     * timestamp. The timestamp indicates when the object should be expired.
     *
     * @version $Revision$ $Date$
     * @author Ernst de Haan (<a href="mailto:ernst.dehaan@nl.wanadoo.com">ernst.dehaan@nl.wanadoo.com</a>)
     */
    private class Entry extends Object {
 
       //----------------------------------------------------------------------
       // Constructors
       //----------------------------------------------------------------------
 
       /**
        * Constructs a new <code>Entry</code>.
        *
        * @param reference
        *    reference to the object, should not be <code>null</code> (although
        *    it is not checked).
        */
       private Entry(Object reference) {
          _reference  = reference;
          touch();
       }
 
 
       //----------------------------------------------------------------------
       // Fields
       //----------------------------------------------------------------------
 
       /**
        * Reference to the object. Should not be <code>null</code>.
        */
       private final Object _reference;
 
       /**
        * The time at which this entry should expire.
        */
       private long _expiryTime;
 
 
       //----------------------------------------------------------------------
       // Methods
       //----------------------------------------------------------------------
 
       /**
        * Retrieves the reference to the object.
        *
        * @return
        *    the reference to the object, should not be <code>null</code>.
        */
       public Object getReference() {
          return _reference;
       }
 
       /**
        * Checks if this entry is expired.
        *
        * @return
        *    <code>true</code> if this entry is expired, <code>false</code>
        *    otherwise.
        */
       public boolean isExpired() {
          return System.currentTimeMillis() >= _expiryTime;
       }
 
       /**
        * Touches this entry, resetting the expiry time.
        */
       public void touch() {
          _expiryTime = System.currentTimeMillis() + _strategy.getTimeOut();
       }
    }
 }
