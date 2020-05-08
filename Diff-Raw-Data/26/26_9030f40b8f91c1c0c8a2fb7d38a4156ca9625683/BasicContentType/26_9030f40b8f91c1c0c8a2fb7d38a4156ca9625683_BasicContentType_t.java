 /*
  *  This file is part of xor
  *  Copyright © 2009, Steen Manniche.
  *
  *  xor is free software: you can redistribute it and/or modify
  *  it under the terms of the GNU General Public License as published by
  *  the Free Software Foundation, either version 3 of the License, or
  *  (at your option) any later version.
  *
  *  xor is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU General Public License for more details.
  *
  *  You should have received a copy of the GNU General Public License
  *  along with xor.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 
 package net.manniche.xor.types;
 
 /**
  * Basic content types defined for the object repository. The content types will
  * be communicated to the registered
  * {@link net.manniche.xor.server.RepositoryObserver observers} and related to
  * a {@link net.manniche.xor.types.RepositoryAction}.
  *
  * @see MetadataContentType
  *
  */
public enum BasicContentType implements ObjectRepositoryContentType{
     /**
      * The default/fallback content type.
      */
     BINARY_CONTENT( "application/octet-stream" );
 
     final String mimeType;
     BasicContentType( String mimeType )
     {
         this.mimeType = mimeType;
     }

    public static ObjectRepositoryContentType getContentType( String contentType ) throws TypeNotPresentException
    {
        for( BasicContentType type : BasicContentType.values() )
        {
            if( contentType.toUpperCase().equals( type.toString() ) )
            {
                return type;
            }
        }
        throw new TypeNotPresentException( contentType, new IllegalArgumentException( String.format( "No content type exists for %s", contentType ) ) );
    }

 }
