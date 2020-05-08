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
     * Constructs a new <code>ExpiryFolder</code> with the specified name and
     * strategy. When the strategy is stopped (see
     * {@link ExpiryStrategy#stop()} then this folder becomes invalid and can
     * no longer be used.
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
     * @throws IllegalStateException
     *    if the strategy is already stopped.
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
                                       + TextUtils.quote(strategy.toString())
                                       + ']';
 
       // TRACE: Enter constructor
       Log.log_1000(CLASSNAME, CONSTRUCTOR_DETAIL);
 
       // Check arguments
       MandatoryArgumentChecker.check("name", name, "strategy", strategy);
 
       // Initialize fields
       _lock             = new Object();
       _name             = name;
       _strategy         = strategy;
       _strategyStopped  = false;
       _asString         = CLASSNAME + ' ' + CONSTRUCTOR_DETAIL;
       _recentlyAccessed = new HashMap(INITIAL_QUEUE_SIZE);
       _slotCount        = strategy.getSlotCount();
       _slots            = new HashMap[_slotCount];
       _lastSlot         = _slotCount - 1;
       _listeners        = new ArrayList(5);
 
       // Initialize all slots to a new HashMap
       for (int i = 0; i < _slotCount; i++) {
          _slots[i] = new HashMap(INITIAL_QUEUE_SIZE);
       }
 
      // Notify the strategy that we listen to it
       strategy.folderAdded(this);
 
       // TRACE: Leave constructor
       Log.log_1002(CLASSNAME, CONSTRUCTOR_DETAIL);
    }
 
    /**
     * Constructs a new <code>ExpiryFolder</code> with the specified name and
     * strategy and some specific tweaks. When the strategy is stopped (see
     * {@link ExpiryStrategy#stop()} then this folder becomes invalid and can
     * no longer be used.
     *
     * <p><em>Since XINS 1.0.1, the arguments <code>strictChecking</code> and
     * <code>maxQueueWaitTime</code> are not used at all.</em>
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
     * Lock object.
     */
    private final Object _lock;
 
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
    private ExpiryStrategy _strategy;
 
    /**
     * Flag that indicates whether the associated strategy has already stopped.
     * If it has, then this folder becomes invalid.
     */
    private boolean _strategyStopped;
 
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
    private HashMap _recentlyAccessed;
 
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
    private HashMap[] _slots;
 
    /**
     * The set of listeners. May be empty, but never is <code>null</code>.
     */
    private ArrayList _listeners;
 
 
    //-------------------------------------------------------------------------
    // Methods
    //-------------------------------------------------------------------------
 
    /**
     * Checks that the associated expiry strategy was not yet stopped. If it
     * was, then an {@link IllegalStateException} is thrown.
     *
     * @throws IllegalStateException
     *    if the associated {@link ExpiryStrategy} was stopped.
     */
   private void assertStrategyNotStopped() {
       if (_strategyStopped) {
          throw new IllegalStateException(
             "The associated ExpiryStrategy has stopped already.");
       }
    }
 
    /**
     * Callback method, called by the <code>ExpiryStrategy</code> to indicate
     * it was stopped.
     */
    void strategyStopped() {
 
       _strategyStopped = true;
 
       // TODO: Log
 
       synchronized (_lock) {
          _strategy         = null;
          _recentlyAccessed = null;
          _slots            = null;
          _listeners        = null;
       }
    }
 
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
     *
     * @throws IllegalStateException
     *    if the associated {@link ExpiryStrategy} has stopped already.
     */
    void tick() throws IllegalStateException {
 
       final String THIS_METHOD = "tick()";
 
       // Check state
       assertStrategyNotStopped();
 
       HashMap toBeExpired;
       HashMap refMap = null;
       synchronized (_lock) {
 
          // Shift the slots
          toBeExpired = _slots[_lastSlot];
          for (int i = _lastSlot; i > 0; i--) {
             _slots[i] = _slots[i - 1];
          }
          _slots[0] = _recentlyAccessed;
 
          // Removed the entries expired in the last slot
          if (!_slots[_lastSlot].isEmpty()) {
             Iterator keyIterator = _slots[_lastSlot].keySet().iterator();
             while (keyIterator.hasNext()) {
                Object key   = keyIterator.next();
                Entry  entry = (Entry) _slots[_lastSlot].get(key);
                if (entry.isExpired()) {
                   keyIterator.remove();
                   if (refMap == null) {
                      refMap = new HashMap();
                   }
                   refMap.put(key, entry.getReference());
                }
             }
          }
          
          // Copy all references from the wrapping Entry objects
          if (!toBeExpired.isEmpty()) {
             Iterator keyIterator = toBeExpired.keySet().iterator();
             while (keyIterator.hasNext()) {
                Object key   = keyIterator.next();
                Entry  entry = (Entry) toBeExpired.get(key);
                if (entry.isExpired()) {
 
                   // Create a map for the object references, if necessary
                   if (refMap == null) {
                      refMap = new HashMap();
                   }
 
                   // Store the entry that needs expiring in the refMap
                   refMap.put(key, entry.getReference());
                } else {
                   final String DETAIL = "Entry marked for expiry should have expired. Key as string is \""
                                       + entry.getReference().toString()
                                       + "\".";
                   Utils.logProgrammingError(CLASSNAME, THIS_METHOD,
                                             CLASSNAME, THIS_METHOD,
                                             DETAIL);
                }
             }
          }
          
          // Recycle the old HashMap
          toBeExpired.clear();
          _recentlyAccessed = toBeExpired;
       }
 
       // Determine how may objects are to be sent to the listeners
       int refMapSize       = refMap == null
                            ? 0
                            : refMap.size();
 
       // Log this
       Log.log_1400(_asString, refMapSize);
 
       // If set of objects for listeners is empty, then short-circuit
       if (refMapSize < 1 || _listeners.size() < 1) {
          return;
       }
 
       // XXX: Should we do this in separate thread(s) ?
 
       // Get a copy of the list of listeners
       synchronized (_listeners) {
 
          // If appropriate, notify the listeners
          if (refMap != null && refMap.size() > 0) {
             Map unmodifiableExpired = Collections.unmodifiableMap(refMap);
             int listenerCount = _listeners.size();
             for (int i = 0; i < listenerCount; i++) {
                ExpiryListener listener = (ExpiryListener) _listeners.get(i);
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
     * @throws IllegalStateException
     *    if the associated {@link ExpiryStrategy} has stopped already.
     *
     * @throws IllegalArgumentException
     *    if <code>listener == null</code>.
     */
    public void addListener(ExpiryListener listener)
    throws IllegalStateException, IllegalArgumentException {
 
       // Check state
       assertStrategyNotStopped();
 
       // Check arguments
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
     * @throws IllegalStateException
     *    if the associated {@link ExpiryStrategy} has stopped already.
     *
     * @throws IllegalArgumentException
     *    if <code>listener == null</code>.
     */
    public void removeListener(ExpiryListener listener)
    throws IllegalStateException, IllegalArgumentException {
 
       // Check state
       assertStrategyNotStopped();
 
       // Check arguments
       MandatoryArgumentChecker.check("listener", listener);
 
       synchronized (_listeners) {
          _listeners.remove(listener);
       }
    }
 
    /**
     * Determines the number of non-expired entries in the specified
     * <code>HashMap</code>. If any entries are expired, they will be removed.
     *
     * @param map
     *    the map in which the non-expired entries are counted.
     *
     * @return
     *    the size of the specified map, always &gt;= 0.
     *
     * @throws IllegalStateException
     *    if the associated {@link ExpiryStrategy} has stopped already.
     *
     * @throws IllegalArgumentException
     *    if <code>map == null</code>.
     */
    private int sizeOf(HashMap map)
    throws IllegalStateException, IllegalArgumentException {
 
       // Check state
       assertStrategyNotStopped();
 
       // Check arguments
       MandatoryArgumentChecker.check("map", map);
 
       int size = 0;
 
       synchronized (_lock) {
          Iterator keyIterator = map.keySet().iterator();
          while (keyIterator.hasNext()) {
             Object key   = keyIterator.next();
             Entry  entry = (Entry) map.get(key);
             if (!entry.isExpired()) {
                size++;
             }
          }
       }
 
       return size;
    }
 
    /**
     * Gets the number of entries.
     *
     * @return
     *    the number of entries in this expiry folder, always &gt;= 0.
     *
     * @throws IllegalStateException
     *    if the associated {@link ExpiryStrategy} has stopped already.
     */
    public int size()
    throws IllegalStateException {
 
       // Check state
       assertStrategyNotStopped();
 
       // Always get the lock for _recentlyAccessed first
       synchronized (_lock) {
 
          int size = sizeOf(_recentlyAccessed);
          for (int i = 0; i < _slotCount; i++) {
             size += sizeOf(_slots[i]);
          }
 
          return size;
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
     * @throws IllegalStateException
     *    if the associated {@link ExpiryStrategy} has stopped already.
     *
     * @throws IllegalArgumentException
     *    if <code>key == null</code>.
     */
    public Object get(Object key)
    throws IllegalStateException, IllegalArgumentException {
 
       // Check state
       assertStrategyNotStopped();
 
       // Check arguments
       MandatoryArgumentChecker.check("key", key);
 
       // Search in the recently accessed map first
       Entry entry;
       synchronized (_lock) {
          entry = (Entry) _recentlyAccessed.get(key);
 
          // Entry found in recently accessed
          if (entry != null) {
 
             // Entry is already expired
             if (entry.isExpired()) {
                return null;
 
             // Entry is not expired, touch it and return the reference
             } else {
                entry.touch();
                return entry.getReference();
             }
 
          // Not found in recently accessed, look in slots
          } else {
 
             // Go through all slots
             for (int i = 0; i < _slotCount; i++) {
                entry = (Entry) _slots[i].remove(key);
 
                if (entry != null) {
 
                   // Entry is already expired, update the map and size and
                   // return null
                   if (entry.isExpired()) {
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
     * @throws IllegalStateException
     *    if the associated {@link ExpiryStrategy} has stopped already.
     *
     * @throws IllegalArgumentException
     *    if <code>key == null</code>.
     */
    public Object find(Object key)
    throws IllegalStateException, IllegalArgumentException {
 
       // Check state
       assertStrategyNotStopped();
 
       // Check arguments
       MandatoryArgumentChecker.check("key", key);
 
       Object value;
 
       // Search in the recently accessed map first
       synchronized (_lock) {
          value = _recentlyAccessed.get(key);
 
          // If not found, then look in the slots
          if (value == null) {
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
     * @throws IllegalStateException
     *    if the associated {@link ExpiryStrategy} has stopped already.
     *
     * @throws IllegalArgumentException
     *    if <code>key == null || value == null</code>.
     */
    public void put(Object key, Object value)
    throws IllegalStateException, IllegalArgumentException {
 
       // Check state
       assertStrategyNotStopped();
 
       // Check arguments
       MandatoryArgumentChecker.check("key", key, "value", value);
 
       // Store the association in the set of recently accessed entries
       synchronized (_lock) {
          Entry entry = new Entry(value);
          _recentlyAccessed.put(key, entry);
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
     * @throws IllegalStateException
     *    if the associated {@link ExpiryStrategy} has stopped already.
     *
     * @throws IllegalArgumentException
     *    if <code>key == null</code>.
     */
    public Object remove(Object key)
    throws IllegalStateException, IllegalArgumentException {
 
       // Check state
       assertStrategyNotStopped();
 
       // Check arguments
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
     * @throws IllegalStateException
     *    if the associated {@link ExpiryStrategy} has stopped already.
     *
     * @throws IllegalArgumentException
     *    if <code>newFolder == null</code> or <code>newFolder == this</code>
     *    or the precision is the newFolder is not the same as for this folder.
     */
    public void copy(ExpiryFolder newFolder)
    throws IllegalStateException, IllegalArgumentException {
 
       final String THIS_METHOD = "copy(" + CLASSNAME + ')';
 
       // Check state
       assertStrategyNotStopped();
 
       // Check arguments
       MandatoryArgumentChecker.check("newFolder", newFolder);
       if (newFolder == this) {
          final String DETAIL = "Folder can not be copied into itself.";
          Utils.logProgrammingError(CLASSNAME, THIS_METHOD,
                                    Utils.getCallingClass(),
                                    Utils.getCallingMethod(),
                                    DETAIL);
          throw new IllegalArgumentException(DETAIL);
       }
       if (newFolder.getStrategy().getPrecision() != getStrategy().getPrecision()) {
          final String DETAIL = "Folders must have the same precision.";
          Utils.logProgrammingError(CLASSNAME, THIS_METHOD,
                                    Utils.getCallingClass(),
                                    Utils.getCallingMethod(),
                                    DETAIL);
          throw new IllegalArgumentException(DETAIL);
       }
 
       synchronized (_lock) {
          synchronized (newFolder._lock) {
 
             // Copy the recentlyAccessed
             newFolder._recentlyAccessed = new HashMap(_recentlyAccessed);
 
             // Copy the slots
             for (int i = 0; i < _slotCount && i < newFolder._slotCount; i++) {
                newFolder._slots[i] = new HashMap(_slots[i]);
             }
          }
       }
    }
 
    /**
     * Returns the strategy associated with this folder
     *
     * @return
     *    the strategy, never <code>null</code>.
     *
     * @throws IllegalStateException
     *    if the associated strategy has already stopped.
     */
    public ExpiryStrategy getStrategy()
    throws IllegalStateException {
 
       // Check state
       assertStrategyNotStopped();
 
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
