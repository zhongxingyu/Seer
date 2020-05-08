 /*
  * Copyright (c) 2008-2011 David Kellum
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
 package iudex.http;
 
 /**
  * A name/value pair suitable for HTTP and other header representation. All
 * name and value instances must support toString() as serialized form.
  */
 public final class Header
 {
     public Header( Object name, Object value )
     {
         if( name == null ) throw new NullPointerException( "name" );
         if( value == null ) throw new NullPointerException( "value" );
 
         _name = name;
         _value = value;
     }
 
     public Object name()
     {
         return _name;
     }
     public Object value()
     {
         return _value;
     }
 
     @Override
     public String toString()
     {
         StringBuilder b = new StringBuilder( 128 );
         b.append( _name.toString() );
         b.append( ": " );
         b.append(  _value.toString() );
         return b.toString();
     }
 
     private final Object _name;
     private final Object _value;
 }
