 package net.sf.javagimmicks.collections.bidimap;
 
 import java.util.AbstractMap;
 import java.util.AbstractSet;
 import java.util.Iterator;
 import java.util.Map;
 import java.util.Set;
 
 /**
  * A default implementation of {@link BidiMap} that internally works with two
  * convenient {@link Map} (one for each "direction").
  */
 public class DualBidiMap<K, V> extends AbstractMap<K, V> implements BidiMap<K, V>
 {
    protected final Map<K, V> _forwardMap;
    protected final Map<V, K> _reverseMap;
 
    /**
     * Creates a new instance for the given forward and reverse {@link Map}
     * 
     * @param forwardMap
     *           the forward {@link Map} used to map keys to values
     * @param reverseMap
     *           the reverse {@link Map} used to map values to keys
     */
    public DualBidiMap(final Map<K, V> forwardMap, final Map<V, K> reverseMap)
    {
       _forwardMap = forwardMap;
       _reverseMap = reverseMap;
    }
 
    @Override
    public Set<Entry<K, V>> entrySet()
    {
       return new DualBidiEntrySet(getForwardMap().entrySet());
    }
 
    @Override
    public V put(final K key, final V value)
    {
       checkKey(key);
       checkValue(value);
 
       final V oldValue = getForwardMap().put(key, value);
       postProcessPut(key, value, oldValue);
 
       return oldValue;
    }
 
    @Override
    public V remove(final Object key)
    {
       final V value = getForwardMap().remove(key);
       getReverseMap().remove(value);
 
       return value;
    }
 
    @Override
    public BidiMap<V, K> inverseBidiMap()
    {
       return new InverseDualBidiMap(getReverseMap(), getForwardMap());
    }
 
    @Override
    public V get(final Object key)
    {
       return getForwardMap().get(key);
    }
 
    @Override
    public K getKey(final V value)
    {
       return getReverseMap().get(value);
    }
 
    protected Map<K, V> getForwardMap()
    {
       return _forwardMap;
    }
 
    protected Map<V, K> getReverseMap()
    {
       return _reverseMap;
    }
 
    protected void postProcessPut(final K key, final V newValue, final V oldValue)
    {
       final Map<V, K> reverseMap = getReverseMap();
 
       // Put the new value to the reverse map
       final K oldKey = reverseMap.put(newValue, key);
 
       // Update to forward map might have invalidated a key in the reverse map
       if (oldValue != null)
       {
          reverseMap.remove(oldValue);
       }
 
       // Update to reverse map might have invalidated a key in the forward map
       if (oldKey != null)
       {
          getForwardMap().remove(oldKey);
       }
    }
 
    protected static <K extends Object> void checkKey(final K value)
    {
       if (value == null)
       {
          throw new IllegalArgumentException("Null keys not allowed in BidiMaps!");
       }
    }
 
    protected static <V extends Object> void checkValue(final V value)
    {
       if (value == null)
       {
          throw new IllegalArgumentException("Null values not allowed in BidiMaps!");
       }
    }
 
    protected class DualBidiEntry implements Entry<K, V>
    {
       protected final Entry<K, V> _internalEntry;
 
       protected DualBidiEntry(final Entry<K, V> internalEntry)
       {
          this._internalEntry = internalEntry;
       }
 
       @Override
       public K getKey()
       {
          return _internalEntry.getKey();
       }
 
       @Override
       public V getValue()
       {
          return _internalEntry.getValue();
       }
 
       @Override
       public V setValue(final V value)
       {
          final V oldValue = _internalEntry.setValue(value);
 
          // Attention!
          // This might invalidate the Iterator that created this entry
          // But there is no workaround possible - it's the bidirectional nature
          postProcessPut(getKey(), value, oldValue);
 
          return oldValue;
       }
    }
 
    protected class DualBidiEntryIterator implements Iterator<Entry<K, V>>
    {
      protected final Iterator<Entry<K, V>> _internalIterator;
       protected Entry<K, V> _lastEntry;
 
       protected DualBidiEntryIterator(final Iterator<Entry<K, V>> internalIterator)
       {
         this._internalIterator = internalIterator;
       }
 
       @Override
       public boolean hasNext()
       {
         return _internalIterator.hasNext();
       }
 
       @Override
       public Entry<K, V> next()
       {
         _lastEntry = _internalIterator.next();
          return new DualBidiEntry(_lastEntry);
       }
 
       @Override
       public void remove()
       {
         _internalIterator.remove();
          _reverseMap.remove(_lastEntry.getValue());
       }
    }
 
    protected class DualBidiEntrySet extends AbstractSet<Entry<K, V>>
    {
       private final Set<Entry<K, V>> internalEntrySet;
 
       protected DualBidiEntrySet(final Set<Entry<K, V>> internalEntrySet)
       {
          this.internalEntrySet = internalEntrySet;
       }
 
       @Override
       public Iterator<Entry<K, V>> iterator()
       {
          return new DualBidiEntryIterator(internalEntrySet.iterator());
       }
 
       @Override
       public int size()
       {
          return internalEntrySet.size();
       }
    }
 
    protected class InverseDualBidiMap extends DualBidiMap<V, K>
    {
       protected InverseDualBidiMap(final Map<V, K> reverseMap, final Map<K, V> forwardMap)
       {
          super(reverseMap, forwardMap);
       }
 
       @Override
       public BidiMap<K, V> inverseBidiMap()
       {
          return DualBidiMap.this;
       }
    }
 }
