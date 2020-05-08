 /*
  * Copyright (c) 2007-2012 David Kellum
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *    http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package com.gravitext.htmap;
 
 import java.util.AbstractMap;
 import java.util.AbstractSet;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Iterator;
 import java.util.List;
 import java.util.NoSuchElementException;
 import java.util.Set;
 
 /**
  * Heterogeneous Type-safe {@code HTMap} implementation based on a
  * simple array. This implementation uses the {@code KeySpace}
  * assigned unique {@code Key.id()} as an index into a contiguous
  * array.  It provides constant-time high performance basic Map
  * operations and very low overhead, in particular for small
  * KeySpace's and short lived maps.  Memory overhead or initial
  * allocation cost may be a concern for very large KeySpace's and
  * sparse maps.  All keys added to a given {@code ArrayHTMap} instance
  * must belong to the same {@link KeySpace} as passed on
  * construction. Null keys are not supported as per {@code HTMap}.
  * Null values are not supported.
  *
  * <p>ArrayHTMap instances are not internally synchronized. If
  * multiple threads have concurrent access to a single instance, and
  * any of these might invoke mutator operations, then all operations
  * must be externally synchronized.</p>
  *
  * @see Key
  * @see KeySpace
  * @author David Kellum
  */
 @SuppressWarnings("unchecked")
 public class ArrayHTMap
     extends AbstractMap<Key, Object>
     implements HTMap
 {
     /**
      * Construct given the KeySpace to which all subsequently added
      * keys will be part.
      */
     public ArrayHTMap( KeySpace space )
     {
         _space = space;
         _values = new Object[ space.size() ]; //NPE on null space
     }
 
     /**
      * Construct as copy of other. This ArrayHTMap will have the same
      * KeySpace as other.
      */
     public ArrayHTMap( ArrayHTMap other )
     {
         _space = other._space;
         _values = (Object[]) other._values.clone();
         _size = other._size;
     }
 
     /**
      * {@inheritDoc}
      * @throws IllegalArgumentException if key is not from the same
      * KeySpace assigned on creation of the map.
      * @throws ClassCastException {@inheritDoc}
      * @throws NullPointerException if key or value is null
      */
     public <T, V extends T> T set( Key<T> key, V value )
     {
         checkKey( key );
         checkValue( key, value );
 
         int i = key.id();
         expand( i );
         T prior = cast( key, i );
         _values[i] = value;
         if( prior == null ) ++_size;
 
         return prior;
     }
 
     @Override
     public Object put( Key key, Object value )
     {
         return set( key, value );
     }
 
     /**
      * {@inheritDoc}
      * @throws IllegalArgumentException if key is not from the same
      * KeySpace assigned on creation of the map.
      * @throws NullPointerException {@inheritDoc}
      */
     public <T> T get( Key<T> key )
     {
         checkKey( key );
 
         int i = key.id();
         if( i >= _values.length ) return null;
 
         return cast( key, i );
     }
 
     @Override
     public Object get( Object key )
     {
         return get( (Key) key );
     }
 
     @Override
     public boolean containsKey( Object key )
     {
         return ( get( (Key) key ) != null );
     }
 
     /**
      * {@inheritDoc}
      * @throws IllegalArgumentException if key is not from the same
      * KeySpace assigned on creation of the map.
      * @throws NullPointerException {@inheritDoc}
      */
     public <T> T remove( Key<T> key )
     {
         checkKey( key );
 
         int i = key.id();
         if( i >= _values.length ) return null;
 
         T prior = cast( key, i );
         if( prior != null ) {
             _values[i] = null;
             --_size;
         }
         return prior;
     }
 
     @Override
     public Object remove( Object key )
     {
         return remove( (Key) key );
     }
 
     @Override
     public void clear()
     {
         Arrays.fill( _values, null );
         _size = 0;
     }
 
     @Override
     public int size()
     {
         return _size;
     }
 
     @Override
     public String toString()
     {
         final StringBuilder out = new StringBuilder( 256 );
         render( new ArrayList<ArrayHTMap>(3), out );
         return out.toString();
     }
 
     @Override
     public Set<Entry<Key, Object>> entrySet()
     {
         return new EntrySet();
     }
 
     @Override
     public ArrayHTMap clone()
     {
         return new ArrayHTMap( this );
     }
 
     private <T> T cast( Key<T> key, int i )
     {
         return (T) _values[i];
         //Not required, and slower: key.valueType().cast( _values[i] );
     }
 
     private void expand( int index )
     {
         if( index >= _values.length ) {
             Object[] newVals = new Object[ _space.size() ];
             System.arraycopy( _values, 0, newVals, 0, _values.length );
             _values = newVals;
         }
     }
 
     private void checkKey( Key key )
     {
         if( key == null ) throw new NullPointerException( "key" );
         if( key.space() != _space ) {
             throw new IllegalArgumentException(
                "Key is not from KeySpace of this ArrayHTMap." );
         }
     }
 
     private void checkValue( Key key, Object value )
     {
         if( value == null ) throw new NullPointerException( "value" );
         if( ! key.valueType().isInstance( value ) ) {
             throw new ClassCastException( String.format(
                 "Value type %s not assignable to Key '%s' with value type %s.",
                 value.getClass().getName(),
                 key.name(),
                 key.valueType().getName() ) );
         }
     }
 
     private void render( List<ArrayHTMap> visited, StringBuilder out )
     {
         out.append( getClass().getSimpleName() );
         out.append( '@' );
         out.append( Integer.toHexString( System.identityHashCode( this ) ) );
 
         // Check if this is in visited already, by identity.
         boolean found = false;
         for( ArrayHTMap v : visited ) {
             if( v == this ) {
                 found = true;
                 break;
             }
         }
 
         if( !found ) {
             visited.add( this );
             out.append( '{' );
             final List<Key> keys = _space.keySequence();
             final int end = _values.length;
             boolean first = true;
             for( int i = 0; i < end; ++i ) {
                 Object value = _values[ i ];
                 if( value != null ) {
 
                     if( first ) {
                         out.append( ' ' );
                         first = false;
                     }
                     else out.append( ", " );
 
                     out.append( keys.get( i ).name() ).append( '=' );
 
                     if( value instanceof ArrayHTMap ) {
                         ( (ArrayHTMap) value ).render( visited, out );
                     }
                     else {
                         out.append( value.toString() );
                     }
                 }
             }
             if( !first ) out.append( ' ' );
             out.append( '}' );
         }
     }
 
     private final class EntrySet
         extends AbstractSet<Entry<Key, Object>>
     {
 
         @Override
         public void clear()
         {
             ArrayHTMap.this.clear();
         }
 
         @Override
         public boolean contains( Object e )
         {
             Entry<Key, Object> entry = (Entry<Key, Object>) e;
 
             Object value = ArrayHTMap.this.get( entry.getKey() );
             return ( ( value == null ) ? false :
                        value.equals( entry.getValue() ) );
         }
 
         @Override
         public boolean add( Entry<Key, Object> entry)
         {
             Object prior = set( entry.getKey(), entry.getValue() );
             return ( ( prior == null ) ?
                      true : ( ! prior.equals( entry.getValue() ) ) );
         }
 
         @Override
         public boolean remove( Object e )
         {
             Entry<Key, Object> entry = (Entry<Key, Object>) e;
             return ( ArrayHTMap.this.remove( entry.getKey() ) != null );
         }
 
         @Override
         public Iterator<Entry<Key, Object>> iterator()
         {
             return new EntryIterator( _space.keySequence() );
         }
 
         @Override
         public int size()
         {
             return _size;
         }
 
     }
 
     private final class EntryIterator
         implements Iterator<Entry<Key, Object>>
     {
         public EntryIterator( List<Key> keySequence )
         {
             seek();
             _keySequence = keySequence;
         }
         public boolean hasNext()
         {
             return (_current < _values.length );
         }
         public Entry<Key, Object> next()
         {
             if( ! hasNext() ) throw new NoSuchElementException();
 
             KeyMapEntry entry =
                 new KeyMapEntry( _keySequence.get( _current ), _current );
             _last = _current;
             seek();
             return entry;
         }
 
         public void remove()
         {
             _values[_last] = null;
             --_size;
         }
 
         private void seek()
         {
             while( ( ++_current < _values.length ) &&
                    ( _values[_current] == null ) );
         }
 
         private List<Key> _keySequence;
         private int _current = -1;
         private int _last = -1;
     }
 
     private final class KeyMapEntry
         implements Entry<Key, Object>
     {
         public KeyMapEntry( Key key, int index )
         {
             _key = key;
             _index = index;
         }
 
         public Key getKey()
         {
             return _key;
         }
         public Object getValue()
         {
             return _values[_index];
         }
 
         public Object setValue( Object value )
         {
             return ArrayHTMap.this.set( _key, value );
         }
 
         @Override
         public boolean equals( Object e )
         {
             if( ! ( e instanceof Entry ) ) return false;
             Entry entry = (Entry) e;
 
             return ( getKey().equals( entry.getKey() ) &&
                      getValue().equals( entry.getValue() ) );
         }
 
         @Override
         public int hashCode()
         {
             return ( getKey().hashCode() ^ getValue().hashCode() );
         }
 
         private Key _key;
         private int _index;
     }
 
     private final KeySpace _space;
     private Object[] _values;
     private int _size = 0;
 }
