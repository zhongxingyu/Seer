 /*
  * Copyright (c) 2010 David Kellum
  *
  * Licensed under the Apache License, Version 2.0 (the "License"); you
  * may not use this file except in compliance with the License.  You may
  * obtain a copy of the License at
  *
  *    http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
  * implied.  See the License for the specific language governing
  * permissions and limitations under the License.
  */
 
 package com.gravitext.xml.tree;
 
 import com.gravitext.htmap.Key;
 
 public final class Characters extends Node
 {
     public static final Key<CharSequence> CONTENT =
         COMPAT_KEY_SPACE.create( "content", CharSequence.class );
 
     public Characters( CharSequence chars )
     {
         if( chars == null ) {
             throw new NullPointerException( getClass().getName() );
         }
 
         _chars = chars;
     }
 
     public CharSequence characters()
     {
         return _chars;
     }
 
     public void setCharacters( CharSequence chars )
     {
        if( chars == null ) {
            throw new NullPointerException( "setCharacters" );
        }

         _chars = chars;
     }
 
     public <T> T get( Key<T> key )
     {
         if( key == CONTENT ) {
             return key.valueType().cast( characters() );
         }
         return super.get( key );
     }
 
     public <T> T remove( Key<T> key )
     {
         if( key == CONTENT ) {
             CharSequence old = characters();
             setCharacters( EMPTY_CHARS );
             return key.valueType().cast( old );
         }
         return super.remove( key );
     }
 
     public <T, V extends T> T set( Key<T> key, V value )
     {
         if( key == CONTENT ) {
             CharSequence old = characters();
             setCharacters( CONTENT.valueType().cast( value ) );
             return key.valueType().cast( old );
         }
         return super.set( key, value );
     }
 
     private CharSequence _chars;
 
     private static final String EMPTY_CHARS = "";
 }
