 /*
  * Copyright 2007 David Kellum
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
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.concurrent.atomic.AtomicReference;
 
 /**
  * A factory and container of unique {@link Key} instances. The
  * following code sample shows the common usage pattern:
  *
  * <code><pre>
  * public class MyKeys
  * {
  *     public static final KeySpace KEY_SPACE = new KeySpace();
  *
  *     public static final Key&lt;Double> DBLKEY =
  *         KEY_SPACE.create( "DBLKEY", Double.class );
  *
  *     public static final Key&lt;String> STRKEY =
  *         KEY_SPACE.create( "STRKEY", String.class );
  *
  *     public static final Key<List<String>> SLISTKEY =
  *         KEY_SPACE.createListKey( "SLISTKEY" )
  *
  * }
  * </pre></code>
  *
  * <p>KeySpace instances are internally synchronized to support
  * concurrent access interleaved with new key creation.  Copy-on-write
  * semantics are used, such that read operations are unsynchronized
  * while key creation is synchronized and relatively costly. This
  * trade-off is desirable given the usage pattern of key creation
  * commencing on system startup but generally completing shortly
  * thereafter, while read operations continue with high frequency.</p>
  *
  * @author David Kellum
  */
 @SuppressWarnings("unchecked")
 public final class KeySpace
 {
     /**
      * Create a new Key instance.
      * @param name a unique name. By convention, this is the String
      * equivalent of the constant to which the Key will be assigned.
      * @param valueType the runtime class type of values to be
      * associated with this key.
      * @throws IllegalArgumentException if a Key by the specified name
      * has already been created.
      */
     public synchronized <T> Key<T> create( String name, Class<T> valueType )
     {
         final State s = _state.get();
 
         if( s.keyNames.containsKey( name ) ) {
             throw new IllegalArgumentException(
                 "Invalid attempt to create a second key with name '" +
                 name + "'." );
         }
 
         Key<T> key = new Key<T>( name, valueType, this, s.length );
 
         _state.set( s.add( key ) );
 
         return key;
     }
 
     /**
      * Create a new Key of generic type. This variant is a convenience for
      * construction of keys of complex valueType which will be externally cast.
      * @param name a unique name. By convention, this is the String
      * equivalent of the constant to which the Key will be assigned.
     * @param genericType the runtime class type of values to be
      * associated with this key.
      * @throws IllegalArgumentException if a Key by the specified name
      * has already been created.
      */
     public synchronized Key<?> createGeneric( String name,
                                               Class<?> genericType )
     {
         return create( name, genericType );
     }
 
    /**
     * Create a new Key of List<T> type. This variant is a convenience for
     * construction of keys of List<?> type without requiring a cast.
     * @param name a unique name. By convention, this is the String
     * equivalent of the constant to which the Key will be assigned.
     * @throws IllegalArgumentException if a Key by the specified name
     * has already been created.
     */
     public synchronized <T> Key<List<T>> createListKey( String name )
     {
         return (Key<List<T>>) createGeneric( name, List.class );
     }
 
     /**
      * Get Key by name.
      * @return the key, or null if no such key by this name was
      * previously created.
      */
     public Key<?> get( String name )
     {
         return _state.get().keyNames.get( name );
     }
 
     /**
      * Get an existing key by name, or if no such key has yet been
      * created, create a new "anonymous" key with the specified name
      * and value type.  The Key is anonymous in that it will
      * typically not be assigned to a constant value by this method.
      * @return existing or new Key
      */
     public synchronized Key<?> getOrCreate( String name,
                                             Class<?> anonValueType )
     {
         final State s = _state.get();
         Key key = s.keyNames.get( name );
         if( key == null ) {
             key = create( name, anonValueType );
         }
         return key;
     }
 
     /**
      * Return the current number of keys that have been created in this
      * KeySpace.
      */
     public int size()
     {
         return _state.get().length;
     }
 
     /**
      * Return the list of Keys in this KeySpace in the order they were
      * created.  The index position of each key in the list will equal
      * the key.id().
      */
     List<Key> keySequence()
     {
         return _state.get().keySequence;
     }
 
     /**
      * Return the list of Keys in this KeySpace in the order they were
      * created.  The index position of each key in the list will equal
      * the key.id(). The returned list is an immutable snapshot.
      */
     public List<Key> keys()
     {
         return Collections.unmodifiableList( _state.get().keySequence );
     }
 
     private final static class State
     {
         public static final State EMPTY =
             new State( new ArrayList<Key>(), new HashMap<String,Key>() );
 
         public State add( Key key )
         {
             List<Key> seq = new ArrayList<Key>( length + 1 );
             seq.addAll( keySequence );
             seq.add( key );
 
             Map<String,Key> names = new HashMap<String,Key>( length + 1 );
             names.putAll( keyNames );
             names.put( key.name(), key );
 
             return new State( seq, names );
         }
 
         private State( List<Key> seq, Map<String, Key> names )
         {
             length = seq.size();
             keySequence = seq;
             keyNames = names;
         }
 
         final int length;
         final List<Key> keySequence;    //unmodified after construction
         final Map<String,Key> keyNames; //unmodified after construction
     }
 
     private final AtomicReference<State> _state =
         new AtomicReference<State>( State.EMPTY );
 }
