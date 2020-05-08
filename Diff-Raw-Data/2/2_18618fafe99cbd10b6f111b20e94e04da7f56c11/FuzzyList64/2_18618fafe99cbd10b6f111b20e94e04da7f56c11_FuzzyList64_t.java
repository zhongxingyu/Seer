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
 
 package iudex.simhash.brutefuzzy;
 
 import java.util.Collection;
 
 /**
  * A linked array, brute-force scan implementation of FuzzySet64.
  */
 public final class FuzzyList64
     implements FuzzySet64
 {
     public FuzzyList64( int capacity, final int thresholdBits )
     {
         // Use ... 1, 2, 4, 8k bytes list segments.
         // The -8 (*8 = 64 bytes) is for size of this + array overhead
         // (see below) to keep complete segment at page boundary size.
              if( capacity > ( ( 1024 - 8 ) *  2  ) ) capacity = 1024 - 8;
         else if( capacity > ( (  512 - 8 ) *  2  ) ) capacity =  512 - 8;
         else if( capacity > ( (  256 - 8 ) * 3/2 ) ) capacity =  256 - 8;
         else if( capacity > ( (  128 - 8 ) * 3/2 ) ) capacity =  128 - 8;
         else if( capacity > ( (   64 - 8 ) * 3/2 ) ) capacity =   64 - 8;
         else if( capacity > ( (   32 - 8 ) * 3/2 ) ) capacity =   32 - 8;
         else capacity = 0;
 
         if( capacity > 0 ) {
             _set = new long[ capacity ];
         }
         _thresholdBits = thresholdBits;
     }
 
     public boolean addIfNotFound( final long key )
     {
         final boolean vacant = ! find( key );
         if( vacant ) store( key );
         return vacant;
     }
 
     public boolean find( final long key )
     {
         final int end = _length;
         final long[] set = _set;
         for( int i = 0; i < end; ++i ) {
             if ( fuzzyMatch( set[i], key ) ) return true;
         }
         if( _next != null ) return _next.find( key );
         return false;
     }
 
     public boolean findAll( final long key, final Collection<Long> matches )
     {
         boolean exactMatch = false;
         final int end = _length;
         final long[] set = _set;
         for( int i = 0; i < end; ++i ) {
             if ( fuzzyMatch( set[i], key ) ) {
                 matches.add( set[i] );
                 if( set[i] == key ) exactMatch = true;
             }
         }
         if( _next != null ) {
             if( _next.findAll( key, matches ) ) exactMatch = true;
         }
 
         return exactMatch;
     }
 
     public boolean addFindAll( long key, Collection<Long> matches )
     {
         boolean exactMatch = findAll( key, matches );
         if( ! exactMatch ) store( key );
         return exactMatch;
     }
 
     public boolean remove( final long key )
     {
         boolean found = false;
         final int end = _length;
         final long[] set = _set;
         for( int i = 0; i < end; ++i ) {
             if ( set[i] == key ) {
                 if( _length - i - 1 > 0 ) {
                     System.arraycopy( set, i + 1, set, i, _length - i - 1 );
                 }
                 --_length;
                 found = true;
                 break;
             }
         }
         if( !found && ( _next != null ) ) {
             found = _next.remove( key );
         }
 
         return found;
     }
 
     public boolean fuzzyMatch( final long a, final long b )
     {
         final long xor = a ^ b;
 
         int diff = Integer.bitCount( (int) xor );
         if( diff <= _thresholdBits ) {
             diff +=  Integer.bitCount( (int) ( xor >> 32 ) );
             return ( diff <= _thresholdBits );
         }
         return false;
     }
 
     void store( final long key )
     {
         if( _length < _set.length ) {
             _set[ _length++ ] = key;
         }
         else {
            // Start chaining at 1024 total segment size
             if( ( _set.length < ( 128 - 8 ) ) ) {
 
                 long[] snew = new long[ _set.length * 2 + 8 ];
                 System.arraycopy( _set, 0, snew, 0, _length );
                 _set = snew;
 
                 _set[ _length++ ] = key;
             }
             else {
                 if( _next == null ) {
                     _next = new FuzzyList64( _set.length, _thresholdBits );
                 }
                 _next.store( key );
             }
         }
     }
 
     //x86_64 size: (this: 2 * 8 ) + 4 + 8 + 4 + 8 +
     //                            (array: 3*8 ) = 8 * 8 = 64 bytes
     private final int _thresholdBits;
     private static final long[] EMPTY_SET = {};
     private long[] _set = EMPTY_SET;
     private int _length = 0;
     private FuzzyList64 _next = null;
 }
