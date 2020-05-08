 /*
   This file is part of opensearch.
   Copyright © 2009, Dansk Bibliotekscenter a/s, 
   Tempovej 7-11, DK-2750 Ballerup, Denmark. CVR: 15149043
   
   opensearch is free software: you can redistribute it and/or modify
   it under the terms of the GNU General Public License as published by
   the Free Software Foundation, either version 3 of the License, or
   (at your option) any later version.
   
   opensearch is distributed in the hope that it will be useful,
   but WITHOUT ANY WARRANTY; without even the implied warranty of
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
   GNU General Public License for more details.
   
   You should have received a copy of the GNU General Public License
   along with opensearch.  If not, see <http://www.gnu.org/licenses/>.
 */
 
 
 /** 
  * \file CompassEntityResolver.java
  * \brief 
  */
 
 
 package dk.dbc.opensearch.common.compass;
 
 
 import org.apache.log4j.Logger;
 
 import org.xml.sax.EntityResolver;
 import org.xml.sax.InputSource;
 
 
 public class CompassEntityResolver implements EntityResolver 
 {
     Logger log = Logger.getLogger( CompassEntityResolver.class );
 
 
     private String publicUrl;
     private String systemUrl;
 
 
     public CompassEntityResolver( String publicUrl, String systemUrl )
     {
         this.publicUrl = publicUrl;
         this.systemUrl = systemUrl;
     }
 
 
     public InputSource resolveEntity( String publicId, String systemId ) throws NullPointerException
     {
         log.debug( String.format( "Entering resolveEntity with publicId: '%s' and systemId: '%s'", publicId, systemId ) );
 
         if ( this.systemUrl != null && systemId.equals( publicUrl ) )
         {
             log.debug( String.format( "returning new InputSource( systemUrl: '%s' ); publicId: '%s'", systemUrl, publicId ) );
             return new InputSource( this.systemUrl );
         }
         else
         {
            String msg = "System identifier does not resolve with public url, using default EntityResolver";
            log.info( msg );
             return null;
         }
     }
 }
