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
  * \file CargoObjectInfo.java
  * \brief The CargoObjectInfo class
  * \package datadock
  */
 package dk.dbc.opensearch.common.types;
 
 
 import java.util.Date;
 
 import org.apache.log4j.Logger;
 
 
 /**
  * Holds the metadata for {@link CargoObjects CargoObject} that are
  * contained in a {@link CargoContainer}
  */
 public class CargoObjectInfo
 {
     private static final Logger log = Logger.getLogger( CargoObjectInfo.class );
 
     /**
      * Property naming type of data stream.
      */
     private final DataStreamType dataStreamName;
 
     private final String format;
 
     /** \todo: the language of the submitted data determines which analyzer
      * should be used in the indexing process, therefore we want full
      * control of allowed languages
      */
     private final String language;
 
     /** \see CargoMimeType */
     private final CargoMimeType mimeType;
 
     /** \todo submitter is primarily thought as an authentication
      * prerequisite, it will probably change in time
      */
     private final String submitter;
 
     /** used to make statistics and estimates regarding the processtime of the dataobject */
     private final Date timestamp;
 
     /** unique identifier of the CargoObject */
     private final long id;
 
     /**
      * Constructs a {@link CargoObjectInfo} instance that acts as a container
      * for the metadata associated with a given {@link CargoContainer}s
      * data. 
      *
      * This object is supposed to be used in a {@link CargoObject},
      * therefore, please see the {@link
      * CargoObject#CargoObject(DataStreamType, String, String, String,
      * String, byte[]) CargoObject()} for implementation
      * documentation.
      *
      * A timestamp for an instance of the {@link CargoObjectInfo} will
      * be created at the time of creation of the instance.  The
      * timestamp can be retrieved with {@link
      * CargoObjectInfo#getTimestamp()}.  The intended use of the
      * timestamp is for timings in the datadock.
      *
      * @param dataStreamName the type of datastream.
      * @param mimeType the mimetype of the datastream
      * @param lang the language of the datastream 
      * @param submitter the submitter of the datastream
      * @param format the format of the datastream
      * @param id the id of the datastream
      *
      * @throws IllegalArgumentException if either of the arguments
      * {@code dataStreamName}, {@code mimeType}, {@code lang}, {@code
      * submitter} or {@code format} are null.
      */
     CargoObjectInfo( DataStreamType dataStreamName, 
                      CargoMimeType mimeType, 
                      String lang,
                      String submitter, 
                      String format,
                      long id )
     {
         log.debug( String.format( "Entering CargoObjectInfo" ) );
 
 	if ( dataStreamName == null ) 
 	{
 	    throw new IllegalArgumentException("CargoObject.dataStreamName can not be null.");
 	}
 	if ( mimeType == null ) 
 	{
 	    throw new IllegalArgumentException("CargoObject.mimeType can not be null.");
 	}
 	if ( lang == null ) 
 	{
 	    throw new IllegalArgumentException("CargoObject.lang can not be null.");
 	}
 	if ( submitter == null ) 
 	{
 	    throw new IllegalArgumentException("CargoObject.submitter can not be null.");
 	}
 	if ( format == null ) 
 	{
 	    throw new IllegalArgumentException("CargoObject.format can not be null.");
 	}
 
         this.dataStreamName = dataStreamName;
         this.mimeType = mimeType;
         this.language = lang;
         this.submitter = submitter;
         this.format = format;
         this.id = id;
         this.timestamp = new Date();
     }
     
     /**
      * Retrieves the datastreamtype.
      *
      * @return the type of the datastream.
      */ 
     DataStreamType getDataStreamType()
     {
         return dataStreamName;
     }
 
 
     /**
      * Retrieves the mimetype as a String converted through {@link
      * CargoMimeType#getMimeType()}. The returned mimetype is
     * therefore <i>not</i> the same as {@link
      * CargoMimeType#toString()}.
      *
      * @return mimetype as a {@link String}.
      *
      * @see CargoMimeType
      */ 
     String getMimeType()
     {
         return mimeType.getMimeType();
     }
 
 
     /**
      * Retrieves the language.
      *
      * @return language as a {@link String}.
      */ 
     String getLanguage()
     {
         return language;
     }
 
 
     /**
      * Retrieves the submitter.
      *
      * @return submitter as a {@link String}.
      */ 
     String getSubmitter()
     {
         return submitter;
     }
 
 
     /**
      * Retrieves the format.
      *
      * @return format as a {@link String}.
      */ 
     String getFormat()
     {
         return format;
     }
 
 
     /**
      * Retrieves the id.
      *
      * @return long representing the {@code id}.
      */ 
     long getId()
     {
         return id;
     }
 
 
     /**
      * Retrieves the timestamp as a long, representing the date of creation of this object.
      * Please notice, internally the timestamp is created by {@link Date#Date()}, and it is therefore
      * affected by locales, timezone etc.
      *
      * @return long representing date of creation.
      * 
      * @see Date
      */ 
     long getTimestamp()
     {
         return timestamp.getTime();
     }
 
 
     /**
      * Returns a {@link String} representation of this object in the following form:
      * <pre>
      * {@code
      *    CargoObjectInfo[ "DataStreamType" , "MimeType" , "language" , "submitter" , "format" , "id" ]
      * }
      * </pre>
      *
      * Where the text in the double-quotes represent the string from the actual variable. 
      * Notice, the double-quotes will not be contained in the {@link String}.
      */
     @Override
     public String toString()
     {
 	String stringrep = String.format( "CargoObjectInfo[ %s , %s , %s , %s , %s , %s ]",
 					  dataStreamName.toString(), mimeType.toString(),
 					  language, submitter, format, id );
 	return stringrep;
     }
 }
