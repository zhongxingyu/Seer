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
 
 
 package dk.dbc.opensearch.common.fedora;
 
 import dk.dbc.opensearch.common.metadata.DublinCore;
 import dk.dbc.opensearch.common.metadata.MetaData;
 import dk.dbc.opensearch.common.types.CargoContainer;
 import dk.dbc.opensearch.common.types.CargoObject;
 import dk.dbc.opensearch.common.types.ComparablePair;
 import dk.dbc.opensearch.common.types.DataStreamType;
 import dk.dbc.opensearch.common.types.OpenSearchTransformException;
 import dk.dbc.opensearch.common.types.Pair;
 
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 import java.net.MalformedURLException;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.List;
 
 import javax.xml.parsers.ParserConfigurationException;
 import javax.xml.rpc.ServiceException;
 import javax.xml.transform.TransformerConfigurationException;
 import javax.xml.transform.TransformerException;
 import javax.xml.xpath.XPathExpressionException;
 
 import org.apache.commons.configuration.ConfigurationException;
 import org.apache.log4j.Logger;
 import org.xml.sax.SAXException;
 
 
 /**
  * This class handles the construction of Fedora Digital Object XML (foxml)
  * from CargoContainers.
  */
 public class FedoraUtils
 {
     static Logger log = Logger.getLogger( FedoraUtils.class );
     private static FedoraHandle fedoraHandle;
 
 
     /**
      * Creates a fedora digital object document XML representation of a given
      * {@link #CargoContainer}
      *
      * @param cargo the CargoContainer to create a foxml representation of
      * @return a byte[] containing the xml representation
      * @throws ParserConfigurationException
      * @throws TransformerConfigurationException
      * @throws TransformerException
      */
     public static byte[] CargoContainerToFoxml( CargoContainer cargo ) throws OpenSearchTransformException, ObjectRepositoryException// ParserConfigurationException, TransformerConfigurationException, TransformerException, ServiceException, ConfigurationException, IOException, MalformedURLException, UnsupportedEncodingException, XPathExpressionException, SAXException, ObjectRepositoryException, OpenSearchTransformException
     {
         List<String> pid = new ArrayList<String>(1);
         fedoraHandle = new FedoraHandle();
         if( null == cargo.getIdentifier() || cargo.getIdentifier().equals( "" ) )
         {
             log.warn( "Could not find identifier for CargoContainer" );
             log.info( "Obtaining new pid for CargoContainer" );
 
             String prefix = cargo.getCargoObject( DataStreamType.OriginalData ).getSubmitter();
             try
             {
 
                 pid = Arrays.asList( fedoraHandle.getNextPID( 1,  prefix ) );
             }
             catch( ServiceException ex )
             {
                 String error  = String.format( "Could not retrieve new pid from namespace %s: %s", prefix, ex.getMessage() );
                 log.error( error );
                 throw new ObjectRepositoryException( error, ex );
             }
             catch( ConfigurationException ex )
             {
                 String error  = String.format( "Could not retrieve new pid from namespace %s: %s", prefix, ex.getMessage() );
                 log.error( error );
                 throw new ObjectRepositoryException( error, ex );
             }
             catch( MalformedURLException ex )
             {
                 String error  = String.format( "Could not retrieve new pid from namespace %s: %s", prefix, ex.getMessage() );
                 log.error( error );
                 throw new ObjectRepositoryException( error, ex );
             }
             catch( IOException ex )
             {
                 String error  = String.format( "Could not retrieve new pid from namespace %s: %s", prefix, ex.getMessage() );
                 log.error( error );
                 throw new ObjectRepositoryException( error, ex );
             }
             catch( IllegalStateException ex )
             {
                 String error  = String.format( "Could not retrieve new pid from namespace %s: %s", prefix, ex.getMessage() );
                 log.error( error );
                 throw new ObjectRepositoryException( error, ex );
             }
             if( null == pid && 1 != pid.size() )
             {
                 log.warn( String.format( "pid is empty for namespace '%s', but no exception was caught.", prefix ) );
                 return null;
             }
 
             cargo.setIdentifier( pid.get( 0 ) );
         }
         else
         {
             pid.add( cargo.getIdentifier() );
         }
         
         //\note: we always create inactive objects
         FoxmlDocument foxml;
         try
         {
             foxml = new FoxmlDocument( FoxmlDocument.State.I, pid.get( 0 ), cargo.getCargoObject( DataStreamType.OriginalData ).getFormat(), cargo.getCargoObject( DataStreamType.OriginalData ).getSubmitter(), System.currentTimeMillis() );
         }
         catch( ParserConfigurationException ex )
         {
             String error  = String.format( "Failed to construct fedora xml with pid %s", pid.get( 0 ) );
             log.error( error );
             throw new ObjectRepositoryException( error, ex );
         }
 
         /**
          * \todo:
          *
          * when iterating here:
          * if cargoObject.getDataStreamName (c.getDataStreamType() => c.getDataStreamType) is datastreamtype.RELSEXT
          * 	    1) Extend datastreamtype to contain RELSEXT
          * then constructDataStream must be called with true, false, true, that is, Versionable = true, External = false, and
          *      InlineData = true.
          */
         // get normal data from cargocontainer
         int cargo_count = cargo.getCargoObjectCount();
         List< ? extends Pair< Integer, String > > ordering = getOrderedMapping( cargo );
         for( int i = 0; i < cargo_count; i++ )
         {
             CargoObject c = cargo.getCargoObjects().get( i );
             try
             {
                 foxml.addBinaryContent( ordering.get( i ).getSecond(), c.getBytes(), c.getFormat(), c.getMimeType(), c.getTimestamp() );
             }
             catch( IOException ex )
             {
                 String error  = String.format( "Failed to add binary data to foxml from cargoobject %s", c.getDataStreamType().getName() );
                 log.error( error , ex);
                 throw new ObjectRepositoryException( error, ex );
             }
             catch( XPathExpressionException ex )
             {
                 String error  = String.format( "Failed to add binary data to foxml from cargoobject %s", c.getDataStreamType().getName() );
                 log.error( error , ex);
                 throw new ObjectRepositoryException( error, ex );
             }
         }
         
         //get metadata from cargocontainer
         for( MetaData meta: cargo.getMetaData() )
         {
             ByteArrayOutputStream baos = baos = new ByteArrayOutputStream();
             log.trace( String.format( "Serializing metadata %s with identifier %s", meta.getClass(), cargo.getIdentifier() ) );
             meta.serialize( baos, cargo.getIdentifier() );
             log.debug( String.format( "ja7s: metadata = %s", meta.getType().getName()) );
             try{
                  switch( meta.getType() ) {
                     
                     case  DublinCoreData: 
                         log.trace( "DublinCore output from serialization: "+new String( baos.toByteArray() ) );
                         foxml.addDublinCoreDatastream( new String( baos.toByteArray() ), System.currentTimeMillis() );
                         break;  
                     case RelsExtData:
                         foxml.addRelsExtDataStream( new String( baos.toByteArray() ), System.currentTimeMillis() );
                         break;
                     default :
                        foxml.addXmlContent( meta.getType().toString(), new String( baos.toByteArray() ), String.format( "Metadata: %s", meta.getIdentifier() ), System.currentTimeMillis(), true );
                     }                       
                 }
                 catch( XPathExpressionException ex )
                 {
                     String error = String.format( "With id %s; Failed to add metadata to foxml from MetaData\" %s\"", meta.getIdentifier(), new String( baos.toByteArray() ) );
                     log.error( error , ex);
                     throw new ObjectRepositoryException( error, ex );
                 }
                 catch( SAXException ex )
                 {
                     String error = String.format( "With id %s; Failed to add metadata to foxml from MetaData\" %s\"", meta.getIdentifier(), new String( baos.toByteArray() ) );
                     log.error( error , ex);
                     throw new ObjectRepositoryException( error, ex );
                 }
                 catch( IOException ex )
                 {
                     String error = String.format( "With id %s; Failed to add metadata to foxml from MetaData\" %s\"", meta.getIdentifier(), new String( baos.toByteArray() ) );
                     log.error( error , ex);
                     throw new ObjectRepositoryException( error, ex );
                 }            
         }
 
         ByteArrayOutputStream baos = new ByteArrayOutputStream();
         try
         {
             foxml.serialize( baos, null );
         }
         catch( TransformerConfigurationException ex )
         {
             String error = String.format( "Failed to construct foxml XML Document: %s", ex.getMessage() );
             log.error( error );
             throw new OpenSearchTransformException( error, ex );
         }
         catch( TransformerException ex )
         {
             String error = String.format( "Failed to construct foxml XML Document: %s", ex.getMessage() );
             log.error( error );
             throw new OpenSearchTransformException( error, ex );
         }
         catch( SAXException ex )
         {
             String error = String.format( "Failed to construct foxml XML Document: %s", ex.getMessage() );
             log.error( error );
             throw new OpenSearchTransformException( error, ex );
         }
         catch( IOException ex )
         {
             String error = String.format( "Failed to construct foxml XML Document: %s", ex.getMessage() );
             log.error( error );
             throw new OpenSearchTransformException( error, ex );
         }
 
         return baos.toByteArray();
     }
 
 
     /**
      * helper method that contructs an administration stream based on the
      * CargoObjects found in the supplied CargoContainer.
      * @param cargo a CargoContainer containing all CargoObjects that are to be
      *        ingested into the fedora repository
      * @return An element representing the adminstream xml
      * @throws ParserConfigurationException
      */
 //    private static Element constructAdminStream( CargoContainer cargo ) throws ParserConfigurationException
 //    {
 //
 //        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
 //        DocumentBuilder builder = factory.newDocumentBuilder();
 //
 //        Document admStream = builder.newDocument();
 //        Element root = admStream.createElement( "admin-stream" );
 //
 //        Element indexingaliasElem = admStream.createElement( "indexingalias" );
 //        indexingaliasElem.setAttribute( "name", cargo.getIndexingAlias( DataStreamType.OriginalData ).getName() );
 //        root.appendChild( (Node) indexingaliasElem );
 //
 //        Node streams = admStream.createElement( "streams" );
 //
 //        int counter = cargo.getCargoObjectCount();
 //        List<? extends Pair<Integer, String>> lst2 = getOrderedMapping( cargo );
 //        for( int i = 0; i < counter; i++ )
 //        {
 //            CargoObject c = cargo.getCargoObjects().get( i );
 //
 //            Element stream = admStream.createElement( "stream" );
 //
 //            stream.setAttribute( "id", lst2.get( i ).getSecond() );
 //            stream.setAttribute( "lang", c.getLang() );
 //            stream.setAttribute( "format", c.getFormat() );
 //            stream.setAttribute( "mimetype", c.getMimeType() );
 //            stream.setAttribute( "submitter", c.getSubmitter() );
 //            stream.setAttribute( "index", Integer.toString( lst2.get( i ).getFirst() ) );
 //            stream.setAttribute( "streamNameType", c.getDataStreamType().getName() );
 //            streams.appendChild( (Node) stream );
 //        }
 //
 //        root.appendChild( streams );
 //
 //        return root;
 //    }
 
     private static List<? extends Pair<Integer, String>> getOrderedMapping( CargoContainer cargo )
     {
         int cargo_count = cargo.getCargoObjectCount();
         log.trace( String.format( "Number of CargoObjects in Container %s", cargo_count ) );
 
         // Constructing list with datastream indexes and id
         List<ComparablePair<String, Integer>> lst = new ArrayList<ComparablePair<String, Integer>>();
         for( int i = 0; i < cargo_count; i++ )
         {
             CargoObject c = cargo.getCargoObjects().get( i );
             lst.add( new ComparablePair<String, Integer>( c.getDataStreamType().getName(), i ) );
         }
 
         Collections.sort( lst );
 
         // Add a number to the id according to the number of
         // datastreams with this datastreamname
         int j = 0;
         DataStreamType dsn = null;
 
         List<ComparablePair<Integer, String>> lst2 = new ArrayList<ComparablePair<Integer, String>>();
         for( Pair<String, Integer> p : lst )
         {
             if( dsn != DataStreamType.getDataStreamTypeFrom( p.getFirst() ) )
             {
                 j = 0;
             }
             else
             {
                 j += 1;
             }
 
             dsn = DataStreamType.getDataStreamTypeFrom( p.getFirst() );
 
             lst2.add( new ComparablePair<Integer, String>( p.getSecond(), p.getFirst() + "." + j ) );
         }
 
         lst2.add( new ComparablePair<Integer, String>( lst2.size(), DataStreamType.AdminData.getName() ) );
 
         Collections.sort( lst2 );
         return lst2;
     }
 }
