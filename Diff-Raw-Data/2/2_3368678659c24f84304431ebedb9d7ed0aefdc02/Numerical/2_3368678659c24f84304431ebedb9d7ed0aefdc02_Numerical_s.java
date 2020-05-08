 /**
  * Copyright (c) 2002-2011 "Neo Technology,"
  * Network Engine for Objects in Lund AB [http://neotechnology.com]
  *
  * This file is part of Neo4j.
  *
  * Neo4j is free software: you can redistribute it and/or modify
  * it under the terms of the GNU Affero General Public License as
  * published by the Free Software Foundation, either version 3 of the
  * License, or (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU Affero General Public License for more details.
  *
  * You should have received a copy of the GNU Affero General Public License
  * along with this program. If not, see <http://www.gnu.org/licenses/>.
  */
 package org.neo4j.admin.tool.stringstat;
 
 public class Numerical extends StringType
 {
     private static final long MAX = ( (long) 1 ) << 60, MIN = -MAX;
 
     @Override
     boolean matches( String string )
     {
        if ( string.length() > 19 ) return false;
         char c = string.charAt( 0 );
         if ( !( c == '-' || ( c >= '0' && c <= '9' ) ) ) return false;
         long integer;
         try
         {
             integer = Long.parseLong( string );
         }
         catch ( Exception e )
         {
             return false;
         }
         return integer < MAX && integer >= MIN;
     }
 }
