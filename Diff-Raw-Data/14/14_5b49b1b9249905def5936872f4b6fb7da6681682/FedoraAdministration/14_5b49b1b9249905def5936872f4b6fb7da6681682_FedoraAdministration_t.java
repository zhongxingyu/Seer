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
  * \file   FedoraAdministration.java
  * \brief  The class for administrating the Fedora Commons repository
  */
 
 package dk.dbc.opensearch.common.fedora;
 
 import dk.dbc.opensearch.common.types.CargoContainer;
 import dk.dbc.opensearch.common.types.CargoObject;
 import dk.dbc.opensearch.common.types.ComparablePair;
 import dk.dbc.opensearch.common.types.DataStreamType;
 import dk.dbc.opensearch.common.types.Pair;
 import dk.dbc.opensearch.common.fedora.PIDManager;
 import dk.dbc.opensearch.common.helpers.XMLFileReader;
 import dk.dbc.opensearch.common.types.IndexingAlias;
 
 import dk.dbc.opensearch.xsd.Datastream;
 import dk.dbc.opensearch.xsd.DatastreamVersion;
 import dk.dbc.opensearch.xsd.DatastreamVersionTypeChoice;
 import dk.dbc.opensearch.xsd.DigitalObject;
 import dk.dbc.opensearch.xsd.ObjectProperties;
 import dk.dbc.opensearch.xsd.Property;
 import dk.dbc.opensearch.xsd.PropertyType;
 import dk.dbc.opensearch.xsd.types.DatastreamTypeCONTROL_GROUPType;
 import dk.dbc.opensearch.xsd.types.DigitalObjectTypeVERSIONType;
 import dk.dbc.opensearch.xsd.types.PropertyTypeNAMEType;
 import dk.dbc.opensearch.xsd.types.StateType;
 
 import java.io.IOException;
 import java.io.ByteArrayInputStream;
 import java.io.FileOutputStream;
 import java.io.File;
 import java.io.StringWriter;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Date;
 import java.text.SimpleDateFormat;
 
 import java.net.MalformedURLException;
 import java.rmi.RemoteException;
 //import java.net.URL;
 
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.rpc.ServiceException;
 import javax.xml.parsers.ParserConfigurationException;
 import javax.xml.transform.Source;
 import javax.xml.transform.dom.DOMSource;
 import javax.xml.transform.Result;
 import javax.xml.transform.stream.StreamResult;
 import javax.xml.transform.Transformer;
 import javax.xml.transform.TransformerFactory;
 import javax.xml.transform.TransformerConfigurationException;
 
 import fedora.server.types.gen.RelationshipTuple;
 import fedora.server.types.gen.MIMETypedStream;
 import fedora.server.types.gen.ComparisonOperator;
 import fedora.server.types.gen.Condition;
 import fedora.server.types.gen.FieldSearchQuery;
 import fedora.server.types.gen.FieldSearchResult;
 import fedora.server.types.gen.ObjectFields;
 
 import org.exolab.castor.xml.MarshalException;
 import org.apache.axis.types.NonNegativeInteger;
 import org.apache.commons.configuration.ConfigurationException;
 import org.apache.log4j.Logger;
 
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.NodeList;
 import org.w3c.dom.Node;
 import org.xml.sax.InputSource;
 import org.xml.sax.SAXException;
 import org.exolab.castor.xml.MarshalException;
 import org.exolab.castor.xml.Marshaller;
 import org.exolab.castor.xml.ValidationException;
 import java.text.ParseException;
 import javax.xml.transform.TransformerException;
 import dk.dbc.opensearch.common.types.InputPair;
 import org.apache.commons.lang.NotImplementedException;
 import java.io.OutputStreamWriter;
 import java.io.ByteArrayOutputStream;
 import java.util.List;
 
 
 public class FedoraAdministration //extends FedoraHandle 
 implements IFedoraAdministration
 {
 
     static Logger log = Logger.getLogger( FedoraAdministration.class );
     // private PIDManager pidManager;
     protected static final SimpleDateFormat dateFormat =
        new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");
 
     //final PIDManager pidManager = new PIDManager();
 
     /**
      * The constructor initalizes the super class FedoraHandle which
      * handles the initiation of the connection with the fedora base
      *
      * @throws ConfigurationException Thrown if the FedoraHandler
      * Couldn't be initialized. @see FedoraHandle
      * @throws IOException Thrown if the FedoraHandler Couldn't be
      * initialized properly. @see FedoraHandle
      * @throws MalformedURLException Thrown if the FedoraHandler
      * Couldn't be initialized properly. @see FedoraHandle
      * @throws ServiceException Thrown if the FedoraHandler Couldn't
      * be initialized properly. @see FedoraHandle
      */
     public FedoraAdministration() throws ConfigurationException, 
                                          IOException, 
                                          MalformedURLException, 
                                          ServiceException
     {
         //super();
         // pidManager = new PIDManager();
     }
     /**
      * method to delete an object for good, based on the pid
      * @param pid, the identifier of the object to be removed
      * @param force, tells whether to purge the object even if it
      * breaks dependencies to other objects
      * @throws RemoteException if something on the serverside goes wrong.
      */
     public void deleteObject( String pid, boolean force ) throws RemoteException
     {
         String logm = "";
         //super.fem.purgeObject( pid, logm, force );
         FedoraHandle.fem.purgeObject( pid, logm, force );
     }
 
 
     /**
      * method for setting the delete flag on an object
      * @param pid, the identifier of the object to be marked as delete
      * @return true if the DigitalObject is marked
      */
     public boolean markObjectAsDeleted( String pid )
     {
         throw new NotImplementedException( "Lazyboy, not implemented yet." );
     }
 
     /**
      * method for getting an object in a CargoContainer based on its pid
      * @param pid, the identifier of the object to get
      * @return the CargoContainer representing the DigitalObject
      * @throws RemoteException if something on the serverside goes wrong.
      */
     public CargoContainer getDigitalObject( String pid ) throws IOException, 
                                                                 ParserConfigurationException, 
                                                                 RemoteException,
                                                                 ServiceException,
                                                                 SAXException
     {
 
         log.trace( String.format( "entering getDO( '%s' )", pid ) );
 
         //Pair< NodeList, NodeList > aliasAndStreamNL = getAdminStream( pid );
 
         Element adminStream = getAdminStream( pid );
         
         NodeList streamNodeList = getStreamNodes( adminStream );
 
         String indexingAlias = getIndexingAlias( adminStream );
 
         log.debug( String.format( "Iterating streams in nodelist" ) );
 
         CargoContainer cc = new CargoContainer();
 
         int streamNLLength = streamNodeList.getLength();
         for(int i = 0; i < streamNLLength; i++ )
         {
             Element stream = (Element)streamNodeList.item(i);
             String streamID = stream.getAttribute( "id" );
             MIMETypedStream dstream = FedoraHandle.fea.getDatastreamDissemination( pid, streamID, null);
             cc.add( DataStreamType.getDataStreamNameFrom( stream.getAttribute( "streamNameType" ) ),
                     stream.getAttribute( "format" ),
                     stream.getAttribute( "submitter" ),
                     stream.getAttribute( "lang" ),
                     stream.getAttribute( "mimetype" ),
                     IndexingAlias.getIndexingAlias( indexingAlias ),
                     dstream.getStream() );
         }
         return cc;
     }
 
     /**
      * method for storing an object in the Fedora base
      * @param theCC the CargoContainer to store
      * @param label, the label to put on the object
      * @return the pid of the object in the repository, null if unsuccesfull
      */
     public String storeCargoContainer( CargoContainer theCC, String label ) throws MalformedURLException, RemoteException, ServiceException, IOException, SAXException, ServiceException, MarshalException, ValidationException, ParseException, ParserConfigurationException, TransformerException, ConfigurationException
     {
         log.trace( "entering storeCC" );
 
         String returnVal = null;
 
         String submitter = theCC.getCargoObject( DataStreamType.OriginalData ).getSubmitter();
 
         String pid = PIDManager.getInstance().getNextPID( submitter );
 
         byte[] foxml = constructFoxml( theCC, pid, label );
         String logm = String.format( "%s inserted", label );
 
         String returnPid = FedoraHandle.getInstance().getAPIM().ingest( foxml, "info:fedora/fedora-system:FOXML-1.1", logm );
         if( pid.equals( returnPid ) )
         {
             returnVal = pid;
         }else
         {
             log.warn( String.format( "Pid '%s' does not equal expected pid '%s'", returnPid, pid ) );
             /** \todo: should we do more than this? If we got a pid
              * back, it means that we succeded, so what if it's not
              * the expected one?*/
         }
 
         return returnVal;
     }
 
     /**
      * method to retrive all DataStreams of a DataStreamType from an object
      * @param pid, identifies the object
      * @param streamtype, the name of the type of DataStream to get
      * @return a CargoContainer of CargoObjects each containing a DataStream,
      * is null if there are no DataStreams of the streamtype.
      */
     public CargoContainer getDataStreamsOfType( String pid, DataStreamType streamtype ) throws MalformedURLException, IOException, RemoteException, ParserConfigurationException, SAXException, ServiceException
     {
         log.trace( String.format( "Entering getDataStreamsOfType()" ) );
         CargoContainer cc = new CargoContainer();
 
         //Pair< NodeList, NodeList > aliasAndStreamNL = getAdminStream( pid );
         Element adminStream = getAdminStream( pid );
         
         NodeList streamNodeList = getStreamNodes( adminStream );
 
         String indexingAlias = getIndexingAlias( adminStream );
 
         log.debug( "iterating streams in nodelist to get the right streamtype" );
 
         int length = streamNodeList.getLength();
 
         for( int i = 0; i < length; i++ )
         {
             Element stream = (Element)streamNodeList.item(i);
             String typeOfStream = stream.getAttribute( "streamNameType" );
             if( typeOfStream.equals( streamtype.getName() ) )
             {
 
                 //build the CargoObject and add it to the list
                 String streamID = stream.getAttribute( "id");
                 MIMETypedStream dstream = FedoraHandle.fea.getDatastreamDissemination( pid, streamID, null );
                 byte[] bytestream = dstream.getStream();
 
                 cc.add( streamtype,
                         stream.getAttribute( "format" ),
                         stream.getAttribute( "lang" ),
                         stream.getAttribute( "submitter" ),
                         stream.getAttribute( "mimetype" ),
                         IndexingAlias.getIndexingAlias( indexingAlias ),
                         bytestream );
             }
         }
 
 
         return cc;
     }
 
     //create another method for getting a datastream form a DO identified by the streamID.
 
     /**
      * method for getting a datastream identified by its streamID
      * @param streamID, the identifier of the datastream to be retrieved
      * @param pid, the identifier of the object to get the stream from
      * @return CargoContainer with the datastream
      */
 
     public CargoContainer getDataStream( String streamID, String pid ) throws MalformedURLException, IOException, RemoteException, ServiceException, ParserConfigurationException, SAXException
     {
         CargoContainer cc = new CargoContainer();
 
         //Pair< NodeList, NodeList > aliasAndStreamNL = getAdminStream( pid );
         Element adminStream = getAdminStream( pid );
         
         NodeList streamNodeList = getStreamNodes( adminStream );
 
         String indexingAlias = getIndexingAlias( adminStream );
 
         log.debug( "iterating streams in nodelist to get info" );
 
         int length = streamNodeList.getLength();
         for( int i = 0; i < length; i++ )
         {
             Element stream = (Element)streamNodeList.item(i);
             String idOfStream = stream.getAttribute( "id" );
             if( streamID.equals( idOfStream ) )
             {
                 //build the CargoObject and add it to the list
                 MIMETypedStream dstream = FedoraHandle.fea.getDatastreamDissemination( pid, streamID, null );
                 byte[] bytestream = dstream.getStream();
 
                 cc.add( DataStreamType.getDataStreamNameFrom( stream.getAttribute( "streamNameType" ) ),
                         stream.getAttribute( "format" ),
                         stream.getAttribute( "lang" ),
                         stream.getAttribute( "submitter" ),
                         stream.getAttribute( "mimetype" ),
                         IndexingAlias.getIndexingAlias( indexingAlias ),
                         bytestream );
             }
         }
         return cc;
     }
     /**
      * method for adding a Datastream to an object
      * @param theFile, the file to save as a DataStream in a specified object
      * @param pid, the identifier of the object to save the datastream to
      * @param label the label to give the stream
      * @param versionable, tells whether to keep track of old versions or not
      * @param overwrite, tells whether to overwrite if the datastream exists
      * @return the dataStreamID of the added stream
      */
     public String addDataStreamToObject( CargoObject cargo, String pid, boolean versionable, boolean overwrite ) throws RemoteException, MalformedURLException, ParserConfigurationException, TransformerConfigurationException, TransformerException, SAXException, IOException
     {
         String sID = null;
         String logm = "";
         String adminLogm = "";
         String dsnName = cargo.getDataStreamName().getName();
 
         MIMETypedStream ds = FedoraHandle.fea.getDatastreamDissemination( pid, DataStreamType.AdminData.getName(), null );
         byte[] adminStream = ds.getStream();
         log.debug( String.format( "Got adminstream from fedora == %s", new String( adminStream ) ) );
         ByteArrayInputStream bis = new ByteArrayInputStream( adminStream );
         log.debug( String.format( "Trying to get root element from adminstream with length %s", bis.available() ) );
         Element root = XMLFileReader.getDocumentElement( new InputSource( bis ) );
 
         log.debug( String.format( "root element from adminstream == %s", root ) );
 
         DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
         DocumentBuilder builder = factory.newDocumentBuilder();
         Document admStream = builder.newDocument();
 
         Element newRoot = (Element)admStream.importNode( (Node)root, true );
         //Element newRoot = admStream.getDocumentElement();
 
         NodeList streamsNL = newRoot.getElementsByTagName( "streams" );
         Element streams = (Element)streamsNL.item( 0 );
         NodeList streamNL = streams.getElementsByTagName( "stream" );
         //iterate streamNL to get num of streams with dsnName as streamNameType
 
         NodeList indexingAliasElem = root.getElementsByTagName( "indexingalias" );
         String indexingAliasName = ((Element)indexingAliasElem.item( 0 )).getAttribute( "name" );
 
         log.debug( String.format( "Got indexingAlias = %s", indexingAliasName ) );
 
         Element oldStream;
         int count = 0;
         int streamNLLength = streamNL.getLength();
 
         //need to loop streams to get num of this type to create valid id
         for( int i = 0; i < streamNLLength; i++ )
         {
             oldStream = (Element)streamNL.item( i );
             if( oldStream.getAttribute( "streamNameType" ).equals( dsnName ) )
             {
                 count++;
             }
         }
 
         // 14: create a streamId
         if( sID == null )
         {
             sID = dsnName + "." + count;
         }
         Element stream = admStream.createElement( "stream" );
 
         stream.setAttribute( "id", sID );
         stream.setAttribute( "lang", cargo.getLang() );
         stream.setAttribute( "format", cargo.getFormat() );
         stream.setAttribute( "mimetype", cargo.getMimeType() );
         stream.setAttribute( "submitter", cargo.getSubmitter() );
         stream.setAttribute( "index", String.valueOf( count ) );
         stream.setAttribute( "streamNameType" , dsnName );
 
         // 15:add data for the new stream
         streams.appendChild( (Node) stream );
 
         // 18: make it into a String
         Source source = new DOMSource((Node) newRoot );
         StringWriter stringWriter = new StringWriter();
         File admFile = new File( "admFile" );
         admFile.deleteOnExit();
 
         Result result = new StreamResult( admFile );
         Result stringResult = new StreamResult( stringWriter );//debug
         TransformerFactory transformerFactory = TransformerFactory.newInstance();
         Transformer transformer = transformerFactory.newTransformer();
         transformer.transform(source, result);
         transformer.transform(source, stringResult);
         //debug
         String admStreamString = stringWriter.getBuffer().toString();
         System.out.println( String.format( "printing new adminstream: %s", admStreamString ) );
 
         // 20:use modify by reference
         String adminLabel= "admin [text/xml]";
         String adminMime = "text/xml";
         //SimpleDateFormat dateFormat = new SimpleDateFormat( "yyyy-MM-dd'T'HH:mm:ss.S" );
         String timeNow = dateFormat.format( new Date( System.currentTimeMillis() ) );
         adminLogm = "admin stream updated with added stream data" + timeNow;
 
         //upload the admFile
         String admLocation = FedoraHandle.fc.uploadFile( admFile );
 
         FedoraHandle.fem.modifyDatastreamByReference( pid, DataStreamType.AdminData.getName(), new String[] {}, adminLabel, adminMime, null, admLocation, null, null, adminLogm, true );
 
         //upload the content
         //byte[] to File
         File theFile = new File( "tempFile" );
         theFile.deleteOnExit();
         FileOutputStream fos = new FileOutputStream( theFile );
         fos.write( cargo.getBytes() );
         fos.flush();
         fos.close();
         String dsLocation = FedoraHandle.fc.uploadFile( theFile );
 
         logm = String.format( "added %s to the object with pid: %s", dsLocation, pid );
 
         String returnedSID = FedoraHandle.fem.addDatastream( pid, sID, new String[] {}, cargo.getFormat(), versionable, cargo.getMimeType(), null, dsLocation, "M", "A", null, null, logm );
 
         return returnedSID;
 
     }
     /**
      * method for modifying an existing dataStream in an object
      * @param cargo, the CargoObject containing the data to modify with
      * @param sID the id of the datastream to be modified
      * @param pid the id of the object to get a datastream updated
      * @param versionable, tells whether to keep track of old version of the stream
      * @param breakDependencies tells whether to update the datastream or not
      * if the operation breaks dependencies with other objects
      * @return the checksum of the datastream...
      */
 
     public String modifyDataStream( CargoObject cargo, String sID, String pid, boolean versionable, boolean breakDependencies ) throws RemoteException, MalformedURLException, IOException
     {
         String logm = String.format( "modified the object with pid: %s", pid );
         String[] altIDs;
         File theFile = new File( "tempFile" );
         theFile.deleteOnExit();
         FileOutputStream fos = new FileOutputStream( theFile );
         fos.write( cargo.getBytes() );
         fos.flush();
         fos.close();
 
         String dsLocation = FedoraHandle.fc.uploadFile( theFile );
 
         return FedoraHandle.fem.modifyDatastreamByReference( pid, sID, new String[] {}, cargo.getFormat(), cargo.getMimeType(), null, dsLocation, null, null, logm, breakDependencies );
 
 
     }
 
     /**
      * method for removing a datastream form an object in the Fedora base
      * @param pid, the indentifier of the object to remove from
      * @param sID, the identifier of the stream to remove
      * @param breakDependencies tells whether to break data contracts/dependencies
      * @param startDate, the earlyist date to remove stream versions from, can be null
      * @param endDate, the latest date to remove stream versions to, can be null
      * @return true if the stream was removed
      */
     public boolean removeDataStream( String pid, String sID, String startDate, String endDate, boolean breakDependencies ) throws RemoteException, ParserConfigurationException, TransformerConfigurationException, TransformerException, IOException, SAXException
     {
         String adminLogm = "";
 
         //10: get the adminstream to modify
         MIMETypedStream ds = FedoraHandle.fea.getDatastreamDissemination( pid,DataStreamType.AdminData.getName(), null );
         byte[] adminStream = ds.getStream();
         log.debug( String.format( "Got adminstream from fedora == %s", new String( adminStream ) ) );
         ByteArrayInputStream bis = new ByteArrayInputStream( adminStream );
         log.debug( String.format( "Trying to get root element from adminstream with length %s", bis.available() ) );
         Element rootOld = XMLFileReader.getDocumentElement( new InputSource( bis ) );
 
         log.debug( String.format( "root element from adminstream == %s", rootOld ) );
 
         DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
         DocumentBuilder builder = factory.newDocumentBuilder();
         Document admStream = builder.newDocument();
         //get indexingAlias
         NodeList indexingAliasElemOld = rootOld.getElementsByTagName( "indexingalias" );
         Node indexingAliasNodeOld = indexingAliasElemOld.item( 0 );
 
         //make root and indexingAlias part of the new document
         Element root = (Element)admStream.importNode( (Node)rootOld, false );
         Node indexingAliasNode = admStream.importNode( indexingAliasNodeOld, true );
 
         //append the indexingAlias to the root
         root.appendChild( indexingAliasNode );
 
         NodeList streamsNLOld = rootOld.getElementsByTagName( "streams" );
         Element streamsOld = (Element)streamsNLOld.item( 0 );
         Element streams = (Element)admStream.importNode( (Node)streamsOld, false );
         root.appendChild( (Node)streams );
 
         NodeList streamNLOld = streamsOld.getElementsByTagName( "stream" );
         //need to loop streams to get the type and index of the stream to be purged
         int purgeIndex = -1;
         String purgeStreamTypeName = "";
         Element oldStream;
         int streamNLLength = streamNLOld.getLength();
         for( int i = 0; i < streamNLLength; i++ )
         {
             oldStream = (Element)streamNLOld.item( i );
             if( oldStream.getAttribute( "id" ).equals( sID ) )
             {
                 purgeIndex = Integer.valueOf( oldStream.getAttribute( "index" ) );
                 purgeStreamTypeName = oldStream.getAttribute( "streamTypeName" );
             }
         }
 
         //need to loop streams again to get the streams to be moved to the admStream
         String currentStreamTypeName;
         int currentIndex;
         int newVal;
         for( int i = 0; i < streamNLLength; i++ )
         {
             oldStream = (Element)streamNLOld.item( i );
             //if not the stream to purge, import to admStream
             if( !oldStream.getAttribute( "id" ).equals( sID) )
             {
                 Element stream = (Element)admStream.importNode( (Node)oldStream, true );
                 //modify the index of the stream, if of same StreamType and index has higher value
                 currentStreamTypeName = stream.getAttribute( "streamTypeName" );
                 if( currentStreamTypeName.equals( purgeStreamTypeName ) )
                 {
                     currentIndex = Integer.valueOf( stream.getAttribute( "index" ) );
                     if( currentIndex > purgeIndex )
                     {
                         newVal = currentIndex - 1;
                         stream.setAttribute( "index", Integer.toString( newVal) );
                     }
                 }
 
                 streams.appendChild( (Node) stream );
             }
         }
 
         // 18: make the admin info into a File ( and a String for current debug)
         Source source = new DOMSource((Node) root );
         StringWriter stringWriter = new StringWriter();
         File admFile = new File( "admFile" );
         admFile.deleteOnExit();
 
         Result result = new StreamResult( admFile );
         Result stringResult = new StreamResult( stringWriter );//debug
         TransformerFactory transformerFactory = TransformerFactory.newInstance();
         Transformer transformer = transformerFactory.newTransformer();
         transformer.transform(source, result);
         transformer.transform(source, stringResult);
         //debug
         String admStreamString = stringWriter.getBuffer().toString();
         System.out.println( String.format( "printing new adminstream: %s", admStreamString ) );
 
         // 20:use modify by reference
         String adminLabel= "admin [text/xml]";
         String adminMime = "text/xml";
         //SimpleDateFormat dateFormat = new SimpleDateFormat( "yyyy-MM-dd'T'HH:mm:ss.S" );
         String timeNow = dateFormat.format( new Date( System.currentTimeMillis() ) );
         adminLogm = "admin stream updated with added stream data" + timeNow;
 
         //upload the admFile
         String admLocation = FedoraHandle.fc.uploadFile( admFile );
 
         FedoraHandle.fem.modifyDatastreamByReference( pid, DataStreamType.AdminData.getName(), new String[] {}, adminLabel, adminMime, null, admLocation, null, null, adminLogm, true );
 
         boolean retval = false;
         String logm = String.format( "removed stream %s from object %s", sID, pid );
 
         /**
          * \Todo: find out why the return val is String[] and not String. bug 9046
          */
         String[] stamp = FedoraHandle.fem.purgeDatastream( pid, sID, startDate, endDate, logm, breakDependencies );
         if( stamp != null )
         {
             retval = true;
         }
         return retval;
 
     }
 
     /**
      * method for adding a relation to an object
      * @param pid, the identifier of the object to add the realtion to
      * @param predicate, the predicate of the relation to add
      * @param targetPid, the object to relate the object to, can be a literal
      * @param literal, true if the targetPid is a literal
      * @param datatype, the datatype of the literal, optional
      * @return true if the relation was added
      */
     public boolean addRelation( String pid, String predicate, String targetPid, boolean literal, String datatype ) throws RemoteException
     {
         return FedoraHandle.fem.addRelationship( pid, predicate, targetPid, literal, datatype );
     }
 
     /**
      * method for getting the relationships an object has
      * @param pid, the object to get relations for
      * @param predicate, the predicate to search for, null means all
      * @return RelationshipTuple[] containing the following for each relationship found:
      * String subject, the object this method was called on
      * String predicate, 
      * String object, the target of the predicate
      * boolean isLiteral, tells if the object is a literal and not a pid
      * String datatype, tells what datatype to pass the object as if it is a literal
      */
     public RelationshipTuple[] getRelationships( String pid, String predicate ) throws RemoteException
     {
         return FedoraHandle.fem.getRelationships( pid, predicate);
     }
 
     /**
      * method to see if an object has a certain relationship to another object
      * Its a filtered version of the method getRelationships
      * @param subject, the pid of the object in question
      * @param predicate, the relationship in queation
      * @param target, the target of the predicate
      * @param isLiteral, true if the target is not an object in the base
      * @return true if the relationship exists
      */
     public boolean hasRelationship( String subject, String predicate, String target, boolean isLiteral ) throws RemoteException
     {
         RelationshipTuple[] rt = FedoraHandle.fem.getRelationships( subject, predicate );
         int rtLength = rt.length;
         for( int i = 0; i < rtLength; i++ )
         {
             if( rt[ i ].getObject().equals( target ) && rt[ i ].isIsLiteral() == isLiteral )
             {
                 return true;
             }
         }
         return false;
 
     }
 
     /**
      * method for finding pids of objects based on object properties
      * @param property, the property to match
      * @param operator, the operator to apply, "has", "eq", "lt", "le","gt" and "ge" are valid
      * @param value, the value the property adheres to
      * @return an array o pids of the matching objects
      */
     public String[] findObjectPids( String property, String operator, String value ) throws RemoteException
     {
 
         String[] resultFields = {"pid", "title"};
         NonNegativeInteger maxResults = new NonNegativeInteger( "1000" );
         // \Todo: check needed on the operator
         ComparisonOperator comp = ComparisonOperator.fromString( operator ); 
         Condition cond = new Condition();
         cond.setProperty( property );
         cond.setOperator( comp );
         cond.setValue( value );
         Condition[] condArray = { cond };
         FieldSearchQuery fsq = new FieldSearchQuery();
         fsq.setConditions( condArray );
         FieldSearchResult fsr = FedoraHandle.fea.findObjects( resultFields, maxResults, fsq );
         ObjectFields[] objectFields = fsr.getResultList();
         int ofLength = objectFields.length;
         String[] pids = new String[ ofLength ];
 
         for( int i = 0; i < ofLength; i++ )
         {
            //  String title = "";
 //             String[] titleArray = objectFields[ i ].getTitle();
 //             for( int j = 0; j < titleArray.length; j++ )
 //             {
 //                 title = title + titleArray[ j ];
 //             }
             pids[ i ] = objectFields[ i ].getPid();// + title;
         }
         return pids;
 
     }
 
 
 
 
     public boolean removeRelation( String pid, String predicate, String targetPid, boolean isLiteral, String datatype ) throws RemoteException
     {
         return FedoraHandle.fem.purgeRelationship( pid, predicate, targetPid, isLiteral, datatype );
     }
 
     /**
      * method for constructing the foxml to ingest
      * @param cargo, the CargoContainer to ingest
      * @param nextPid, the pid of the object to create
      * @param label, the label of the object, most often the format of the original data
      * @return a byte[] to ingest
      */
     static byte[] constructFoxml(CargoContainer cargo, String nextPid, String label) throws IOException, MarshalException, ValidationException, ParseException, ParserConfigurationException, SAXException, TransformerException, TransformerConfigurationException
     {
         log.debug( String.format( "Constructor( cargo, nextPid='%s', label='%s' ) called", nextPid, label ) );
 
         Date now = new Date(System.currentTimeMillis());
         return constructFoxml(cargo, nextPid, label, now);
     }
 
     /**
      * method for constructing the foxml to ingest
      * @param cargo, the CargoContainer to ingest
      * @param nextPid, the pid of the object to create
      * @param label, the label of the object, most often the format of the original data
      * @param now, the time of creation of the foxml
      * @return a byte[] to ingest
      */
     static byte[] constructFoxml( CargoContainer cargo, String nextPid, String label, Date now ) throws IOException, MarshalException, ValidationException, ParseException, ParserConfigurationException, SAXException, TransformerException, TransformerConfigurationException
     {
         log.trace( String.format( "Entering constructFoxml( cargo, nexPid='%s', label='%s', now )", nextPid, label ) );
 
         DigitalObject dot = initDigitalObject( "Active", label, "dbc", nextPid, now );
 
         String timeNow = dateFormat.format( now );
 
         int cargo_count = cargo.getCargoObjectCount();
         log.debug( String.format( "Number of CargoObjects in Container", cargo_count ) );
     
         // Constructing list with datastream indexes and id
     
         List< ComparablePair < String, Integer > > lst = new  ArrayList< ComparablePair < String, Integer > >();
         for(int i = 0; i < cargo_count; i++)
         {
             CargoObject c = cargo.getCargoObjects().get( i );
           
             lst.add( new ComparablePair< String, Integer >( c.getDataStreamName().getName(), i ) );
         }
     
         Collections.sort( lst );
 
         // Add a number to the id according to the number of
         // datastreams with this datastreamname
         int j = 0;
         DataStreamType dsn = null;
        
         List< ComparablePair<Integer, String> > lst2 = new ArrayList< ComparablePair <Integer, String> >();
         for( Pair<String, Integer> p : lst)
         {
             if( dsn != DataStreamType.getDataStreamNameFrom( p.getFirst() ) )
             {
                 j = 0;
             }
             else
             {
                 j += 1;
             }
 
             dsn = DataStreamType.getDataStreamNameFrom( p.getFirst() );
     
             lst2.add( new ComparablePair<Integer, String>( p.getSecond(), p.getFirst() + "." + j ) );
         }
      
         lst2.add( new ComparablePair<Integer, String>( lst2.size(), DataStreamType.AdminData.getName() ) );
      
         Collections.sort( lst2 );
 
         log.debug( "Constructing adminstream" );
 
         Element root = constructAdminStream( cargo, lst2 );
 
         // Transform document to xml string
         Source source = new DOMSource((Node) root );
         StringWriter stringWriter = new StringWriter();
         Result result = new StreamResult(stringWriter);
         TransformerFactory transformerFactory = TransformerFactory.newInstance();
         Transformer transformer = transformerFactory.newTransformer();
         transformer.transform(source, result);
         String admStreamString = stringWriter.getBuffer().toString();
         log.debug( String.format( "Constructed Administration stream for the CargoContainer=%s", admStreamString ) );
 
         // add the adminstream to the cargoContainer
         byte[] byteAdmArray = admStreamString.getBytes();
         cargo.add( DataStreamType.AdminData, "admin", "dbc", "da", "text/xml", IndexingAlias.None, byteAdmArray );
        
 
         log.debug( "Constructing foxml byte[] from cargoContainer" );
         cargo_count = cargo.getCargoObjectCount();//.getItemsCount();
 
         log.debug( String.format( "Length of CargoContainer including administration stream=%s", cargo_count ) );
         Datastream[] dsArray = new Datastream[ cargo_count ];
         for(int i = 0; i < cargo_count; i++)
         {
             CargoObject c = cargo.getCargoObjects().get( i );
            
             dsArray[i] = constructDatastream( c, timeNow, lst2.get( i ).getSecond() );
         }
 
         log.debug( String.format( "Successfully constructed datastreams from the CargoContainer. length of datastream[]='%s'", dsArray.length ) );
 
         // add the streams to the digital object
         dot.setDatastream( dsArray );
 
         log.debug( "Marshalling the digitalObject to a byte[]" );
         java.io.ByteArrayOutputStream out = new java.io.ByteArrayOutputStream();
         java.io.OutputStreamWriter outW = new java.io.OutputStreamWriter( out );
         Marshaller m = new Marshaller( outW ); // IOException
         m.marshal( dot ); // throws MarshallException, ValidationException
     
         byte[] ret = out.toByteArray();
 
         log.debug( String.format( "length of marshalled byte[]=%s", ret.length ) );
         return ret;
     }
 
     private static Element constructAdminStream( CargoContainer cargo, List< ComparablePair<Integer, String> > lst2 ) throws ParserConfigurationException
     {
         // Constructing adm stream
         DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
         DocumentBuilder builder = factory.newDocumentBuilder();
 
         Document admStream = builder.newDocument();
         Element root = admStream.createElement( "admin-stream" );
 
         Element indexingaliasElem = admStream.createElement( "indexingalias" );
         indexingaliasElem.setAttribute( "name", cargo.getIndexingAlias( DataStreamType.OriginalData ).getName() );
         root.appendChild( (Node)indexingaliasElem );
 
         Node streams = admStream.createElement( "streams" );
 
         int counter = cargo.getCargoObjectCount();
 
         for(int i = 0; i < counter; i++)
         {
             CargoObject c = cargo.getCargoObjects().get( i );
 
             Element stream = admStream.createElement( "stream" );
          
             stream.setAttribute( "id", lst2.get( i ).getSecond() );
             stream.setAttribute( "lang", c.getLang() );
             stream.setAttribute( "format", c.getFormat() );
             stream.setAttribute( "mimetype", c.getMimeType() );
             stream.setAttribute( "submitter", c.getSubmitter() );
             stream.setAttribute( "index", Integer.toString( lst2.get( i ).getFirst() ) );
             stream.setAttribute( "streamNameType" ,c.getDataStreamName().getName() );
             streams.appendChild( (Node) stream );
         }
 
         root.appendChild( streams );
 
         return root;
     }
 
 
     private Element getAdminStream( String pid ) throws IOException, 
                                                         ParserConfigurationException, 
                                                         RemoteException, 
                                                         ServiceException, 
                                                         SAXException
     {
         MIMETypedStream ds = FedoraHandle.fea.getDatastreamDissemination( pid, DataStreamType.AdminData.getName(), null );
 
         byte[] adminStream = ds.getStream();
         if( adminStream == null ) 
         {
             log.error( String.format( "Could not retrieve adminstration stream from Digital Object, aborting." ) );
             throw new IllegalStateException( String.format( "Could not retrieve administration stream from Digital Object with pid '%s'", pid ) );
         } 
         log.debug( String.format( "Got adminstream from fedora: %s", new String( adminStream ) ) );
 
         CargoContainer cc = new CargoContainer();
 
         ByteArrayInputStream bis = new ByteArrayInputStream( adminStream );
         log.debug( String.format( "Trying to get root element from adminstream with length %s", bis.available() ) );
         Element root = XMLFileReader.getDocumentElement( new InputSource( bis ) );
 
         log.debug( String.format( "root element from adminstream == %s", root ) );
 
         return root;
     }
 
 
     private String getIndexingAlias( Element adminStream )
     {
         NodeList indexingAliasElem = adminStream.getElementsByTagName( "indexingalias" );
         if( indexingAliasElem == null )
         {
             /** \todo: this if statement doesnt skip anything. What should we do? bug: 8878 */
             log.error( String.format( "Could not get indexingalias from adminstream, skipping " ) );
         }
         String indexingAliasName = ((Element)indexingAliasElem.item( 0 )).getAttribute( "name" );
 
         return indexingAliasName;
     }
 
 
     private NodeList getStreamNodes( Element adminStream )
     {
         NodeList streamsNL = adminStream.getElementsByTagName( "streams" );
         Element streams = (Element)streamsNL.item(0);
         NodeList streamNL = streams.getElementsByTagName( "stream" );
 
         return streamNL;
     }
        
     /** 
      * Serializes a DigitalObject into a byte[] containing the
      * serialized xml document.
      *
      * Through the serializing functionality provided by the castor
      * framework, the DigitalObject is validated before serialized. If
      * something is amiss with the object structure and castor finds
      * out in time, a ValidationException will be thrown. If castor is
      * unable, for other reasons, to serialize the object, a
      * MarshallException will be thrown.
      * 
      * @param dot the DigitalObject to be serialized
      * 
      * @return a byte[] containing the (xml-) serialized form of the DigitalObject
      */
     public static byte[] DigitalObjectAsByteArray( DigitalObject dot )throws IOException, MarshalException, ValidationException//, ParseException, ParserConfigurationException, SAXException, TransformerException, TransformerConfigurationException
     {
         log.debug( "Marshalling the digitalObject to a byte[]" );
         ByteArrayOutputStream out = new ByteArrayOutputStream();
         OutputStreamWriter outW = new OutputStreamWriter(out);
         Marshaller m = new Marshaller( outW ); // IOException
         m.marshal(dot); // throws MarshallException, ValidationException
         //log.debug( String.format( "Marshalled DigitalObject=%s", out.toString() ) );
         byte[] ret = out.toByteArray();
 
         log.debug( String.format( "length of marshalled byte[]=%s", ret.length ) );
         return ret;
 
     }
 
     /**
      * Initializes and returns a DigitalObject with no
      * DataStreams. This method defaults the timestamp to
      * System.currentTimeMillis
      *
      * @param state one of: Active, Inactive or Deleted
      * - Active: The object is published and available.
      * - Inactive: The object is not publicly available.
      * - Deleted: The object is deleted, and should not be available
      *            to anyone. It is still in the repository, and special
      *            administration tools should be able to resurrect it.
      * @param label A descriptive label of the Digitial Object
      * @param owner The (system) name of the owner of the Digital
      * Object. Please note that this has nothing to do with the
      * ownership of the material (although the names can and may
      * coincide).
      *
      * @return a DigitalObject with no DataStreams
      */
     private static DigitalObject initDigitalObject( String state,
                                                     String label,
                                                     String owner,
                                                     String pid )
     {
         Date timestamp = new Date( System.currentTimeMillis() );
         return initDigitalObject( state, label, owner, pid, timestamp );
     }
 
     /**
      * Initializes and returns a DigitalObject with no
      * DataStreams.
      * @see initDigitalObject( String, String, String ) for more info
      * @param state one of Active, Inactive or Deleted
      * @param label description of the DigitalObject
      * @param owner (System) owner of the DigitalObject
      * @param timestamp overrides the default (now) timestamp
      *
      * @return a DigitalObject with no DataStreams
      */
     private static DigitalObject initDigitalObject( String state,
                                                     String label,
                                                     String owner,
                                                     String pid,
                                                     Date timestamp )
     {
         //ObjectProperties holds all the Property types
         ObjectProperties op = new ObjectProperties();
 
         Property pState = new Property();
         pState.setNAME( PropertyTypeNAMEType.INFO_FEDORA_FEDORA_SYSTEM_DEF_MODEL_STATE );
         pState.setVALUE( state );
 
         Property pLabel = new Property();
         pLabel.setNAME( PropertyTypeNAMEType.INFO_FEDORA_FEDORA_SYSTEM_DEF_MODEL_LABEL );
         pLabel.setVALUE( label );
 
         PropertyType pOwner = new Property();
         pOwner.setNAME(PropertyTypeNAMEType.INFO_FEDORA_FEDORA_SYSTEM_DEF_MODEL_OWNERID);
         pOwner.setVALUE( owner );
 
        //SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.S");
         String timeNow = dateFormat.format( timestamp );
         Property pCreatedDate = new Property();
         pCreatedDate.setNAME( PropertyTypeNAMEType.INFO_FEDORA_FEDORA_SYSTEM_DEF_MODEL_CREATEDDATE );
         pCreatedDate.setVALUE( timeNow );
 
         // Upon creation, the last modified date == created date
         Property pLastModifiedDate = new Property();
         pLastModifiedDate.setNAME( PropertyTypeNAMEType.INFO_FEDORA_FEDORA_SYSTEM_DEF_VIEW_LASTMODIFIEDDATE );
         pLastModifiedDate.setVALUE( timeNow );
 
         Property[] props = new Property[] { pState, pLabel, (Property) pOwner,
                                             pCreatedDate, pLastModifiedDate };
         op.setProperty( props );
 
         DigitalObject dot = new DigitalObject();
         dot.setObjectProperties(op);
         dot.setVERSION(DigitalObjectTypeVERSIONType.VALUE_0);
         dot.setPID( pid );
 
         return dot;
     }
 
 
 
 
     /** 
      * Constructing a Datastream with a default timestamp (
      * System.currentTimeMillis )
      * 
      * @param co 
      * @param itemID 
      * 
      * @return 
      */    
     private static Datastream constructDatastream( CargoObject co,
                                                    String itemID ) throws ParseException, 
                                                                           IOException
     {
         Date timestamp = new Date( System.currentTimeMillis() );
         String timeNow = dateFormat.format( timestamp );
 
         return constructDatastream( co, timeNow, itemID );
     }
 
     /**
      * constructDatastream creates a Datastream object on the basis of a CargoObject
      *
      * @param co the CargoObject from which to get the data
      * @param timeNow
      * @param itemID
      *
      * @return A datastream suitable for ingestion into the DigitalObject
      */
     private static Datastream constructDatastream( CargoObject co,
                                                    String timeNow,
                                                    String itemID ) throws ParseException 
     {
         return constructDatastream( co, timeNow, itemID, false, false, false );
     }
 
     /**
      * Control Group: the approach used by the Datastream to represent or encapsulate the content as one of four types or control groups:
      *    - Internal XML Content - the content is stored as XML
      *      in-line within the digital object XML file
      *    - Managed Content - the content is stored in the repository
      *      and the digital object XML maintains an internal
      *      identifier that can be used to retrieve the content from
      *      storage
      *    - Externally Referenced Content (not yet implemented) - the
      *      content is stored outside the repository and the digital
      *      object XML maintains a URL that can be dereferenced by the
      *      repository to retrieve the content from a remote
      *      location. While the datastream content is stored outside of
      *      the Fedora repository, at runtime, when an access request
      *      for this type of datastream is made, the Fedora repository
      *      will use this URL to get the content from its remote
      *      location, and the Fedora repository will mediate access to
      *      the content. This means that behind the scenes, Fedora will
      *      grab the content and stream in out the the client
      *      requesting the content as if it were served up directly by
      *      Fedora. This is a good way to create digital objects that
      *      point to distributed content, but still have the repository
      *      in charge of serving it up.
      *    - Redirect Referenced Content (not supported)- the content
      *      is stored outside the repository and the digital object
      *      XML maintains a URL that is used to redirect the client
      *      when an access request is made. The content is not
      *      streamed through the repository. This is beneficial when
      *      you want a digital object to have a Datastream that is
      *      stored and served by some external service, and you want
      *      the repository to get out of the way when it comes time to
      *      serve the content up. A good example is when you want a
      *      Datastream to be content that is stored and served by a
      *      streaming media server. In such a case, you would want to
      *      pass control to the media server to actually stream the
      *      content to a client (e.g., video streaming), rather than
      *      have Fedora in the middle re-streaming the content out.
      */
     private static Datastream constructDatastream( CargoObject co,
                                                    String timeNow,
                                                    String itemID,
                                                    boolean versionable,
                                                    boolean externalData,
                                                    boolean inlineData ) throws ParseException
     {
         int srcLen = co.getContentLength();
         byte[] ba = co.getBytes();
 
         log.debug( String.format( "constructing datastream from cargoobject id=%s, format=%s, submitter=%s, mimetype=%s, contentlength=%s, datastreamtype=%s, indexingalias=%s, datastream id=%s",co.getId(), co.getFormat(),co.getSubmitter(),co.getMimeType(), co.getContentLength(), co.getDataStreamName(), co.getIndexingAlias(), itemID ) );
 
         DatastreamTypeCONTROL_GROUPType controlGroup = null;
         if( (! externalData ) && ( ! inlineData ) && ( co.getMimeType() == "text/xml" ) )
         {
             //Managed content
             controlGroup = DatastreamTypeCONTROL_GROUPType.M;
         }
         else if( ( ! externalData ) && ( inlineData ) && ( co.getMimeType() == "text/xml" )) {
             //Inline content
             controlGroup = DatastreamTypeCONTROL_GROUPType.X;
         }
         // else if( ( externalData ) && ( ! inlineData ) ){
         //     /**
         //      * external data cannot be inline, and this is regarded as
         //      * a client error, but we assume that the client wanted
         //      * the content referenced; we log a warning and proceed
         //      */
         //     log.warn( String.format( "Both externalData and inlineData was set to true, they are mutually exclusive, and we assume that the content should be an external reference" ) );
         //     controlGroup = DatastreamTypeCONTROL_GROUPType.E;
         // }
 
         // datastreamElement
         Datastream dataStreamElement = new Datastream();
 
         dataStreamElement.setCONTROL_GROUP( controlGroup );
 
         dataStreamElement.setID( itemID );
 
         /**
          * \todo: State type defaults to active. Client should interact with
          * datastream after this method if it wants something else
          */
         dataStreamElement.setSTATE( StateType.A );
         dataStreamElement.setVERSIONABLE( versionable );
 
         // datastreamVersionElement
         String itemId_version = itemID+".0";
 
         DatastreamVersion dataStreamVersionElement = new DatastreamVersion();
 
         dataStreamVersionElement.setCREATED( dateFormat.parse( timeNow ) );
 
         dataStreamVersionElement.setID( itemId_version );
 
         DatastreamVersionTypeChoice dVersTypeChoice = new DatastreamVersionTypeChoice();
 
         //ContentDigest binaryContent = new ContentDigest();
 
         dVersTypeChoice.setBinaryContent( ba );
 
         dataStreamVersionElement.setDatastreamVersionTypeChoice(dVersTypeChoice);
 
         String mimeLabel = String.format("%s [%s]", co.getFormat(), co.getMimeType());
         dataStreamVersionElement.setLABEL(mimeLabel);
         String mimeFormatted = String.format("%s", co.getMimeType());
         dataStreamVersionElement.setMIMETYPE( mimeFormatted );
 
         long lengthFormatted = (long) srcLen;
 
         dataStreamVersionElement.setSIZE( lengthFormatted );
 
         DatastreamVersion[] dsvArray = new DatastreamVersion[] { dataStreamVersionElement };
         dataStreamElement.setDatastreamVersion( dsvArray );
 
         log.debug( String.format( "Datastream element is valid=%s", dataStreamElement.isValid() ) );
 
         return dataStreamElement;
     }
 }
