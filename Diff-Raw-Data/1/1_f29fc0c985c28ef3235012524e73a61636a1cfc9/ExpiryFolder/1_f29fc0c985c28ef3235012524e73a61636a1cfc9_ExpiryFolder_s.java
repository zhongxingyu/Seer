 /*
  * $Id$
  */
 package org.xins.common.collections.expiry;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import org.xins.common.Log;
 import org.xins.common.MandatoryArgumentChecker;
 import org.xins.common.threads.Doorman;
 
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
  */
 public final class ExpiryFolder
 extends Object {
 
    //-------------------------------------------------------------------------
    // Class fields
    //-------------------------------------------------------------------------
 
    /**
     * The name of this class.
     */
    private static final String EXPIRY_FOLDER_CLASSNAME = ExpiryFolder.class.getName();
 
    /**
     * The initial size for the queue of threads waiting to obtain read or
     * write access to a resource.
     */
    private static final int INITIAL_QUEUE_SIZE = 89;
 
 
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
     * @param strictChecking
     *    flag that indicates if checking of thread synchronization operations
     *    should be strict or loose.
     *
     * @param maxQueueWaitTime
     *    the maximum time a thread can wait in the queue for obtaining read or
     *    write access to a resource, must be &gt; 0L.
     *
     * @throws IllegalArgumentException
     *    if <code>name == null || strategy == null || maxQueueWaitTime &lt;= 0L</code>.
     */
    public ExpiryFolder(String         name,
                        ExpiryStrategy strategy,
                        boolean        strictChecking,
                        long           maxQueueWaitTime)
    throws IllegalArgumentException {
 
       // Check preconditions
       MandatoryArgumentChecker.check("name", name, "strategy", strategy);
 
       // Initialize fields
       _name             = name;
       _asString         = "ExpiryFolder \"" + _name + '"';
       _strategy         = strategy;
       _recentlyAccessed = new HashMap(89);
       _slotCount        = strategy.getSlotCount();
       _slots            = new Map[_slotCount];
       _lastSlot         = _slotCount - 1;
       _sizeLock         = new Object();
       _listeners        = new ArrayList(5);
 
       // Initialize all the fields in _slots
       for (int i = 0; i < _slotCount; i++) {
          _slots[i] = new HashMap(89);
       }
 
       // Create the doormen
       _recentlyAccessedDoorman = new Doorman("recentlyAccessed", strictChecking, INITIAL_QUEUE_SIZE, maxQueueWaitTime);
       _slotsDoorman            = new Doorman("slots",            strictChecking, INITIAL_QUEUE_SIZE, maxQueueWaitTime);
 
       // Notify the strategy
       strategy.folderAdded(this);
    }
 
 
    //-------------------------------------------------------------------------
    // Fields
    //-------------------------------------------------------------------------
 
    /**
     * The name of this expiry folder.
     */
    private final String _name;
 
    /**
     * String representation. Cannot be <code>null</code>.
     */
    private final String _asString;
 
    /**
     * The strategy used. This field cannot be <code>null</code>.
     */
    private final ExpiryStrategy _strategy;
 
    /**
     * The most recently accessed entries. This field cannot be
     * <code>null</code>. The entries in this map will expire after
     * {@link ExpiryStrategy#getTimeOut()} milliseconds, plus at maximum
     * {@link ExpiryStrategy#getPrecision()} milliseconds.
     */
    private volatile Map _recentlyAccessed;
 
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
     * accessed. The further back in the array, the faster the entries will
     * expire.
     */
    private final Map[] _slots;
 
    /**
     * Doorman protecting the field <code>_recentlyAccessed</code>.
     */
    private final Doorman _recentlyAccessedDoorman;
 
    /**
     * Doorman protecting the field <code>_slots</code>.
     */
    private final Doorman _slotsDoorman;
 
    /**
     * The size of this folder.
     */
    private int _size;
 
    /**
     * Lock for the <code>_size</code>.
     */
    private final Object _sizeLock;
 
    /**
     * The set of listeners. May be empty, but never is <code>null</code>.
     */
    private final List _listeners;
 
 
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
     * <p>If any entries are expirable, they will be removed from this folder.
     */
    void tick() {
 
       // Allocate memory _before_ entering any doorman, so if this fails, then
       // we don't hold any locks
       Map newRecentlyAccessed = new HashMap();
 
       // First enter the protected area for '_recentlyAccessed', because that
       // is the most difficult to enter
       _recentlyAccessedDoorman.enterAsWriter();
 
       // Then enter the protected area for '_slots' as well
       _slotsDoorman.enterAsWriter();
 
       // Keep a link to the old map with recently accessed elements and then
       // reset _recentlyAccessed so we can leave the protected area for
       // '_recentlyAccessed' right away
       Map oldRecentlyAccessed = _recentlyAccessed;
       _recentlyAccessed       = newRecentlyAccessed;
 
       // Leave the protected area for '_recentlyAccessed' first, because that
       // is the heaviest used
       _recentlyAccessedDoorman.leaveAsWriter();
 
       // Shift the slots
       Map toBeExpired = _slots[_lastSlot];
       for (int i = _lastSlot; i > 0; i--) {
          _slots[i] = _slots[i - 1];
       }
       _slots[0] = oldRecentlyAccessed;
 
       // Leave the protected area for '_slots' as well.
       _slotsDoorman.leaveAsWriter();
 
       // Adjust the size
       int toBeExpiredSize = toBeExpired == null ? 0 : toBeExpired.size();
       if (toBeExpiredSize > 0) {
          synchronized (_sizeLock) {
             _size -= toBeExpiredSize;
             newSize = _size;
             if (_size < 0) {
                _size = 0;
             }
          }
 
          // If the new size was negative, it has been fixed already, but
          // report it now, after the synchronized section
          if (newSize < 0) {
             Log.log_3006(EXPIRY_FOLDER_CLASSNAME, "tick()", "Size of expiry folder \"" + _name + "\" dropped to " + newSize + ", adjusted it to 0.");
          }
          Log.log_3400(_asString, toBeExpiredSize, newSize);
       } else {
          Log.log_3400(_asString, 0, _size);
       }
 
       // XXX: Should we do this in a separate thread, so all locks held by the
       //      ExpiryStrategy are released?
 
       // Get a copy of the list of listeners
       List listeners;
       synchronized (_listeners) {
          listeners = new ArrayList(_listeners);
       }
 
       // Notify all listeners
       int count = listeners.size();
       if (count > 0) {
          Map unmodifiableExpired = Collections.unmodifiableMap(toBeExpired);
          for (int i = 0; i < count; i++) {
             ExpiryListener listener = (ExpiryListener) listeners.get(i);
             listener.expired(this, unmodifiableExpired);
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
       _recentlyAccessedDoorman.enterAsReader();
       Object value;
       try {
          value = _recentlyAccessed.get(key);
       } finally {
          _recentlyAccessedDoorman.leaveAsReader();
       }
 
       // If not found, then look in the slots
       // TODO: Determine whether enterAsReader() is really good enough. It
       //       seems that enterAsWriter() should be called. However, this may
       //       have a major impact on performance.
       if (value == null) {
          _slotsDoorman.enterAsReader();
          try {
             for (int i = 0; i < _slotCount && value == null; i++) {
                value = _slots[i].remove(key);
             }
          } finally {
             _slotsDoorman.leaveAsReader();
          }
 
          if (value != null) {
             _recentlyAccessedDoorman.enterAsWriter();
             try {
                _recentlyAccessed.put(key, value);
             } finally {
                _recentlyAccessedDoorman.leaveAsWriter();
             }
          }
       }
 
       return value;
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
 
       // Search in the recently accessed map first
       _recentlyAccessedDoorman.enterAsReader();
       Object value;
       try {
          value = _recentlyAccessed.get(key);
       } finally {
          _recentlyAccessedDoorman.leaveAsReader();
       }
 
       // If not found, then look in the slots
       if (value == null) {
          _slotsDoorman.enterAsReader();
          try {
             for (int i = 0; i < _slotCount && value == null; i++) {
                value = _slots[i].get(key);
             }
          } finally {
             _slotsDoorman.leaveAsReader();
          }
       }
 
       return value;
    }
 
    /**
     * Associates the specified value with the specified key in this folder.
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
       _recentlyAccessedDoorman.enterAsWriter();
       try {
          _recentlyAccessed.put(key, value);
 
          // Bump the size
          synchronized (_sizeLock) {
             _size++;
          }
       } finally {
          _recentlyAccessedDoorman.leaveAsWriter();
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
 
       // Remove the key in the set of recently accessed entries
       _recentlyAccessedDoorman.enterAsReader();
       Object value;
       try {
          value = _recentlyAccessed.remove(key);
 
          if (value != null) {
 
             // Bump the size
             synchronized (_sizeLock) {
                _size--;
             }
          }
       } finally {
          _recentlyAccessedDoorman.leaveAsReader();
       }
 
       // If not found, then look in the slots
       if (value == null) {
          _slotsDoorman.enterAsReader();
          try {
             for (int i = 0; i < _slotCount && value == null; i++) {
                value = _slots[i].remove(key);
             }
 
             if (value != null) {
 
                // Bump the size
                synchronized (_sizeLock) {
                   _size--;
                }
             }
          } finally {
             _slotsDoorman.leaveAsReader();
          }
       }
 
       return value;
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
          throw new IllegalArgumentException("The folder can not be copied into itself.");
       }
       if (newFolder.getStrategy().getPrecision() != getStrategy().getPrecision()) {
          throw new IllegalArgumentException("The folders must have the same precision.");
       }
 
       // Copy the recentlyAccessed
       _recentlyAccessedDoorman.enterAsReader();
       newFolder._recentlyAccessedDoorman.enterAsWriter();
       try {
          newFolder._recentlyAccessed = _recentlyAccessed;
          synchronized(newFolder._sizeLock) {
             newFolder._size = newFolder._recentlyAccessed.size();
          }
       } finally {
          try {
             newFolder._recentlyAccessedDoorman.leaveAsWriter();
          } finally {
             _recentlyAccessedDoorman.leaveAsReader();
          }
       }
 
       // Copy the slots
       _slotsDoorman.enterAsReader();
       newFolder._slotsDoorman.enterAsWriter();
       try {
          for (int i = 0; i < _slotCount && i < newFolder._slotCount; i++) {
             newFolder._slots[i] = _slots[i];
             synchronized(newFolder._sizeLock) {
                newFolder._size += newFolder._slots[i].size();
             }
          }
       } finally {
          try {
             newFolder._slotsDoorman.leaveAsWriter();
          } finally {
             _slotsDoorman.leaveAsReader();
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
 }
