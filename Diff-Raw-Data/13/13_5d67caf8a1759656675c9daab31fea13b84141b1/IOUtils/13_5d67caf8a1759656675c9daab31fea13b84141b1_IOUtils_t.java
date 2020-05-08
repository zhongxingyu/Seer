 /*
  * Copyright (c) 2012 David Kellum
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
 
 package com.gravitext.jruby;
 
 import java.nio.ByteBuffer;
 import java.nio.CharBuffer;
 
 import org.jruby.Ruby;
 import org.jruby.RubyString;
 import org.jruby.runtime.ThreadContext;
 import org.jruby.runtime.builtin.IRubyObject;
 import org.jruby.util.ByteList;
 
 import com.gravitext.util.Charsets;
 
 import org.jcodings.specific.ASCIIEncoding;
import org.jcodings.specific.USASCIIEncoding;
 import org.jcodings.specific.UTF8Encoding;
 
 public class IOUtils
 {
     public static IRubyObject toRubyString( final ThreadContext tc,
                                             final String value )
     {
         return toRubyString( tc.getRuntime(), value );
     }
 
     public static IRubyObject toRubyString( final ThreadContext tc,
                                             final CharSequence value )
     {
         return toRubyString( tc.getRuntime(), value );
     }
 
     public static IRubyObject toRubyString( final Ruby runtime,
                                             final String value )
     {
         if( value == null) return runtime.getNil();
 
         return new RubyString( runtime,
                                runtime.getString(),
                                toByteList( value ) );
     }
 
     public static IRubyObject toRubyString( final Ruby runtime,
                                             final CharSequence value )
     {
         if( value == null) return runtime.getNil();
 
         return new RubyString( runtime,
                                runtime.getString(),
                                toByteList( value ) );
     }
 
     public static ByteList toByteList( final String value )
     {
         return new ByteList( value.getBytes( Charsets.UTF_8 ),
                              UTF8Encoding.INSTANCE,
                              false );
     }
 
     public static ByteList toByteList( final CharSequence value )
     {
         if( value instanceof String ) {
             return toByteList( (String) value );
         }
 
         final CharBuffer cbuff = ( value instanceof CharBuffer ) ?
             ( (CharBuffer) value ).duplicate() : CharBuffer.wrap( value );
 
         final ByteBuffer out = Charsets.UTF_8.encode( cbuff );
 
         return new ByteList( out.array(),
                              out.arrayOffset() + out.position(),
                              out.remaining(),
                              UTF8Encoding.INSTANCE,
                              false );
     }
 
     public static CharSequence fromRubyObject( final ThreadContext tc,
                                                final IRubyObject value )
     {
         if( value.isNil() ) return null;
 
         return fromRubyString( value.convertToString() );
     }
 
     public static CharSequence fromRubyString( RubyString str )
     {
         final ByteList blist = str.getByteList();
 
        if( !( blist.getEncoding() == UTF8Encoding.INSTANCE  ||
               blist.getEncoding() == ASCIIEncoding.INSTANCE ||
               blist.getEncoding() == USASCIIEncoding.INSTANCE ) ) {
             throw new RuntimeException( "Expecting only UTF-8 or ASCII here: " +
                                         blist.getEncoding().toString() );
         }
         // FIXME: Is ASCIIEncoding trustworthy, allowing use of faster
         // ASCII decode?
 
         final ByteBuffer buf = toByteBuffer( blist );
         return Charsets.UTF_8.decode( buf );
     }
 
     public static String stringFromRuby( RubyString str )
     {
         final ByteList blist = str.getByteList();
 
        if( !( blist.getEncoding() == UTF8Encoding.INSTANCE  ||
               blist.getEncoding() == ASCIIEncoding.INSTANCE ||
               blist.getEncoding() == USASCIIEncoding.INSTANCE ) ) {
             throw new RuntimeException( "Expecting only UTF-8 or ASCII here: " +
                                         blist.getEncoding().toString() );
         }
         // FIXME: Is ASCIIEncoding trustworthy, allowing use of faster
         // ASCII decode?
 
         return new String( blist.unsafeBytes(),
                            blist.begin(),
                            blist.length(),
                            Charsets.UTF_8 );
     }
 
     public static ByteBuffer toByteBuffer( RubyString str )
     {
         return toByteBuffer( str.getByteList() );
     }
 
     public static ByteBuffer toByteBuffer( ByteList blist )
     {
         return ByteBuffer.wrap( blist.unsafeBytes(),
                                 blist.begin(),
                                 blist.length() );
     }
 
 }
