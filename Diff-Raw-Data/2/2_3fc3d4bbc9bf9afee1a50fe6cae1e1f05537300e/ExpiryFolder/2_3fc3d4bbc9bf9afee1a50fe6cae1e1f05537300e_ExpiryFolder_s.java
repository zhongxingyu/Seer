 /*
  * $Id$
  */
 package org.xins.util.collections.expiry;
 
 import java.util.HashMap;
 import java.util.Map;
 import org.xins.util.MandatoryArgumentChecker;
 
 /**
  * Expiry folder. Contains values indexed by key. Items in this map will
  * expire after a predefined amount of time, unless they're access within that
  * timeframe.
  *
  * <p>This class is thread-safe.
  *
  * @version $Revision$ $Date$
  * @author Ernst de Haan (<a href="mailto:znerd@FreeBSD.org">znerd@FreeBSD.org</a>)
  */
 public abstract class ExpiryFolder
 extends Object {
 
    //-------------------------------------------------------------------------
    // Class fields
    //-------------------------------------------------------------------------
 
    //-------------------------------------------------------------------------
    // Class functions
    //-------------------------------------------------------------------------
 
    //-------------------------------------------------------------------------
    // Constructor
    //-------------------------------------------------------------------------
 
    /**
     * Constructs a new <code>ExpiryMap</code>.
     *
     * @param strategy
     *    the strategy that should be applied, not <code>null</code>.
     *
     * @throws IllegalArgumentException
     *    if <code>strategy == null</code>.
     */
    public ExpiryFolder(ExpiryStrategy strategy)
    throws IllegalArgumentException {
 
       MandatoryArgumentChecker.check("strategy", strategy);
 
       _strategy = strategy;
 
       // XXX: Allow customization of Map construction?
       _recentlyAccessed = new HashMap(89);
 
       _slots = new Map[strategy.getSlotCount()];
    }
 
 
    //-------------------------------------------------------------------------
    // Fields
    //-------------------------------------------------------------------------
 
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
    private final Map _recentlyAccessed;
 
    /**
     * Slots to contain the maps with entries that are not the most recently
     * accessed. The further back in the array, the faster the entries will
     * expire.
     */
    private final Map[] _slots;
 
 
    //-------------------------------------------------------------------------
    // Methods
    //-------------------------------------------------------------------------
 
    /**
     * Touches the entry that is identified by the specified key.
     *
     * @param key
     *    the key that identifies the entry, can be <code>null</code>.
     *
     * @throws NoSuchEntryException
     *    if there was no entry with the specified key in this map; the entry
     *    may have expired.
     */
    public abstract void touch(Object key) throws NoSuchEntryException;
 
    /**
     * Notifies this map that the precision time frame has passed since the
     * last tick.
     *
     * <p>If any entries are expirable, they will be removed from this map.
     */
    abstract void tick();
 
    /**
     * Gets the number of entries.
     *
     * @return
     *    the number of entries in this expiry folder, always &gt;= 0.
     */
    public int size() {
       int size;
       synchronized (_recentlyAccessed) {
          synchronized (_slots) {
             size = _recentlyAccessed.size();
             for (int i = 0; i < _slots.length; i++) {
                size += _slots[i].size();
             }
          }
       }
       return size;
    }
 
    /**
     * Checks if this folder is completely empty.
     *
     * @return
     *    <code>true</code> if and only if there are no entries in this folder.
     */
    public boolean isEmpty() {
 
       synchronized (_recentlyAccessed) {
          if (_recentlyAccessed.isEmpty() == false) {
             return false;
          }
       }
 
       for (int i = 0; i < _slots.length; i++) {
          Map slot = _slots[i];
          synchronized (slot) {
             if (slot.isEmpty() == false) {
                return false;
             }
          }
       }
 
       return true;
    }
 
    /**
    * Checks if this folder contains a specific key.
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
 
       // Check _recentlyAccessed
       synchronized (_recentlyAccessed) {
          Object o = _recentlyAccessed.get(key);
          if (o != null) {
             return o;
          }
       }
 
       // Check all slots
       for (int i = 0; i < _slots.length; i++) {
          Map slot = _slots[i];
          synchronized (slot) {
             Object o = slot.get(key);
             if (o != null) {
                return o;
             }
          }
       }
 
       return null;
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
 
       synchronized (_recentlyAccessed) {
          _recentlyAccessed.put(key, value);
       }
    }
 
    /**
     * Removes all entries.
     */
    public void clear() {
       synchronized (_recentlyAccessed) {
          _recentlyAccessed.clear();
 
          for (int i = 0; i < _slots.length; i++) {
             Map slot = _slots[i];
             synchronized (slot) {
                slot.clear();
             }
          }
       }
    }
 }
