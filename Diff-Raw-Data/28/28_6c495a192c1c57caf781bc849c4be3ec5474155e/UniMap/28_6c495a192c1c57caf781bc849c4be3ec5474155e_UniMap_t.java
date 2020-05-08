 /*
  * Copyright (C) 2008-2009 David Kellum
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
 
 /**
 * Common use generic HTAccess container. Extends {@link ArrayHTMap}
  * with constructors using a single static KEY_SPACE. The set/put methods are
  * overridden to call remove(key) when passed a null value.
  *
  * @author David Kellum
  */
 public final class UniMap
     extends ArrayHTMap
 {
     public static final KeySpace KEY_SPACE = new KeySpace();
 
     public UniMap()
     {
          super( KEY_SPACE );
     }
 
     /**
      * Copy constructor, creates a shallow copy of other.
      */
     public UniMap( UniMap other )
     {
         super( other );
     }
 
     /**
     * {@inheritDoc}
     * In this implementation, if a null value is given then the key
     * is removed.
      */
     @Override
     public <T, V extends T> T set( Key<T> key, V value )
     {
         if( value != null ) {
             return super.set( key, value );
         }
         else {
             return remove( key );
         }
     }
 
     /**
     * {@inheritDoc}
     * In this implementation, if a null value is given then the key
     * is removed.
      */
     @SuppressWarnings("unchecked")
     @Override
     public Object put( Key key, Object value )
     {
         if( value != null ) {
             return super.put( key, value );
         }
         else {
             return remove( key );
         }
     }
 
     /**
      * Return a shallow copy of this UniMap.
      */
     public UniMap clone()
     {
         return new UniMap( this );
     }
 }
