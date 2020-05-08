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
  * \file FoxmlDocument.java
  * \brief
  */
 
 
 package dk.dbc.opensearch.common.fedora;
 
 
 import dk.dbc.opensearch.common.fedora.FedoraNamespaceContext.FedoraNamespace;
 import fedora.utilities.Base64;
 import fedora.utilities.NamespaceContextImpl;
 import fedora.utilities.XmlTransformUtility;
 
 import java.io.IOException;
 import java.io.OutputStream;
 import java.io.StringReader;
 import java.net.URL;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.parsers.ParserConfigurationException;
 import javax.xml.transform.Result;
 import javax.xml.transform.Source;
 import javax.xml.transform.Transformer;
 import javax.xml.transform.TransformerConfigurationException;
 import javax.xml.transform.TransformerException;
 import javax.xml.transform.TransformerFactory;
 import javax.xml.transform.dom.DOMSource;
 import javax.xml.transform.stream.StreamResult;
 import javax.xml.validation.Schema;
 import javax.xml.validation.SchemaFactory;
 import javax.xml.validation.Validator;
 import javax.xml.xpath.XPath;
 import javax.xml.xpath.XPathConstants;
 import javax.xml.xpath.XPathExpressionException;
 import javax.xml.xpath.XPathFactory;
 
 import org.apache.log4j.Logger;
 
 import org.w3c.dom.DOMImplementation;
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 import org.xml.sax.InputSource;
 import org.xml.sax.SAXException;
 
 
 /**
  * Class that represents the fedora object xml document. This class is a 
  * reimplementation of the Foxml11Document found in the fedora.utilities
  * namespace. It was largely inadequate for our purposes
  */
 public final class FoxmlDocument
 {
     static Logger log = Logger.getLogger( FoxmlDocument.class );
 
 
     /**
      * Defines the system-wide namespace for use in all valid foxml elements
      */
     public static final String FOXML_NS = FedoraNamespaceContext.FedoraNamespace.FOXML.getURI();
     public static final String FEDORA_MODEL = FedoraNamespaceContext.FedoraNamespace.FEDORAMODEL.getURI();
     public static final FedoraNamespace model = FedoraNamespaceContext.FedoraNamespace.FEDORAMODEL;
     public static final FedoraNamespace view = FedoraNamespaceContext.FedoraNamespace.FEDORAVIEW;
     public static final String FOXML_VERSION_NS = FOXML_NS + "";
 
     private DocumentBuilder builder;
     private Document doc;
     private Element rootElement;
     private Element objectProperties;
     private XPathFactory factory;
     private XPath xpath;
     private TransformerFactory xformFactory;
 
 
     /**
      * Helper class for defining properties on the Digital Object
      */
     public enum Property
     {
         STATE( model.getElementURI( "state" ) ),
         LABEL( model.getElementURI( "label" ) ),
         CONTENT_MODEL( model.getElementURI( "contentModel" ) ),
         CREATE_DATE( model.getElementURI( "createdDate" ) ),
         OWNERID( model.getElementURI( "ownerId" ) ),
         MOD_DATE( view.getElementURI( "lastModifiedDate" ) );
         private final String uri;
 
         Property( String uri )
         {
             this.uri = uri;
         }
 
         String uri()
         {
             return uri;
         }
     }
 
 
     /**
      * Helper class for defining the state of the individual datastreams in the
      * Digital Object
      */
     public enum State
     {
         /**
          * Active: The object is published and available.
          */
         A,
         /**
          * Inactive: The object is not publicly available.
          */
         I,
         /**
          * Deleted (but not purged from the repository)
          */
         D;
     }
 
 
     /**
      * Helper class for defining the controlgroup of the individual datastreams.
      */
     public enum ControlGroup
     {
         /**
          * Internal XML Content - the content is stored as XML
          * in-line within the digital object XML file
          */
         X,
         /**
          * Managed Content - the content is stored in the repository
          * and the digital object XML maintains an internal
          * identifier that can be used to retrieve the content from
          * storage
          */
         M,
         /**
          * Externally Referenced Content (not yet implemented) - the
          * content is stored outside the repository and the digital
          * object XML maintains a URL that can be dereferenced by the
          * repository to retrieve the content from a remote
          * location. While the datastream content is stored outside of
          * the Fedora repository, at runtime, when an access request
          * for this type of datastream is made, the Fedora repository
          * will use this URL to get the content from its remote
          * location, and the Fedora repository will mediate access to
          * the content. This means that behind the scenes, Fedora will
          * grab the content and stream in out the the client
          * requesting the content as if it were served up directly by
          * Fedora. This is a good way to create digital objects that
          * point to distributed content, but still have the repository
          * in charge of serving it up.
          */
         E,
         /**
          * Redirect Referenced Content (not supported)- the content
          * is stored outside the repository and the digital object
          * XML maintains a URL that is used to redirect the client
          * when an access request is made. The content is not
          * streamed through the repository. This is beneficial when
          * you want a digital object to have a Datastream that is
          * stored and served by some external service, and you want
          * the repository to get out of the way when it comes time to
          * serve the content up. A good example is when you want a
          * Datastream to be content that is stored and served by a
          * streaming media server. In such a case, you would want to
          * pass control to the media server to actually stream the
          * content to a client (e.g., video streaming), rather than
          * have Fedora in the middle re-streaming the content out.
          */
         R;
     }
 
 
     /**
      * Helper class to specify the LocationType of a referring String if the
      * {@link ControlGroup} has specified referenced content
      * ({@code FoxmlDocument.ControlGroup.R})
      */
     public enum LocationType
     {
         /**
          * the referring String denotes a pid in a fedora repository
          */
         INTERNAL_ID,
         /**
          * the referring String denotes an Url
          */
         URL;
     }
 
 
     /**
      * Creates a skeletal FedoraObject document. Its serialized representation
      * can be obtained through the
      * {@link FoxmlDocument#serialize(java.io.OutputStream, java.net.URL)} method
      * 
      * @param pid the pid to be the identifier for the Digital Object
      * @param label short description of the contents of the Digital Object
      * @param owner the owner of the Digital Object
      * @param timestamp the time of creation of the Digital Object,
      *        System.currentTimeMillis() is a fine choice
      * @throws ParserConfigurationException
      */
     public FoxmlDocument( State state, String pid, String label, String owner, long timestamp ) throws ParserConfigurationException
     {
         /** \todo: a fedora document v1.1 pid must conform to the following rules
          * a maximum length of 64 chars
          * must satisfy the pattern "([A-Za-z0-9]|-|\.)+:(([A-Za-z0-9])|-|\.|~|_|(%[0-9A-F]{2}))+"
          */
         initDocument( pid );
         constructFoxmlProperties( state, label, owner, getTimestamp( timestamp ) );
     }
 
 
     private void initDocument( String id ) throws ParserConfigurationException
     {
         DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
         dbFactory.setNamespaceAware( true );
 
         builder = dbFactory.newDocumentBuilder();
         DOMImplementation impl = builder.getDOMImplementation();
         doc = impl.createDocument( FOXML_NS, "foxml:digitalObject", null );
         rootElement = doc.getDocumentElement();
         rootElement.setAttributeNS( "http://www.w3.org/2000/xmlns/",
                 "xmlns:xsi",
                 "http://www.w3.org/1999/XMLSchema-instance" );
         rootElement.setAttributeNS( "http://www.w3.org/1999/XMLSchema-instance",
                 "xsi:schemaLocation",
                 "info:fedora/fedora-system:def/foxml# http://www.fedora.info/definitions/1/0/foxml1-1.xsd" );
         rootElement.setAttribute( "VERSION", "1.1" );
         rootElement.setAttribute( "PID", id );
 
         factory = XPathFactory.newInstance();
         xpath = factory.newXPath();
         xformFactory = XmlTransformUtility.getTransformerFactory();
 
         NamespaceContextImpl nsCtx = new NamespaceContextImpl();
         nsCtx.addNamespace( "foxml", FOXML_NS );
 
 
         xpath.setNamespaceContext( nsCtx );
     }
 
 
     private void constructFoxmlProperties( State state, String label, String owner, String timestamp )
     {
         addObjectProperty( Property.STATE, state.toString() );
         addObjectProperty( Property.LABEL, label );
         addObjectProperty( Property.OWNERID, owner );
         addObjectProperty( Property.CREATE_DATE, timestamp );
         // Upon creation, the last modified date == created date
         addObjectProperty( Property.MOD_DATE, timestamp );
     }
 
 
     private void addObjectProperties()
     {
         if( objectProperties == null )
         {
             objectProperties = doc.createElementNS( FOXML_NS, "foxml:objectProperties" );
             rootElement.appendChild( objectProperties );
         }
     }
 
 
     private void addObjectProperty( Property name, String value )
     {
         addObjectProperties();
         Element property = doc.createElementNS( FOXML_NS, "foxml:property" );
         property.setAttribute( "NAME", name.uri );
         property.setAttribute( "VALUE", value );
         objectProperties.appendChild( property );
     }
 
 
     /**
      * Adds a Datastream to the DigitalObject Document
      *
      * Please note that this method also handles the construction of the
      * underlying DatastreamVersion
      *
      * @param id
      * @param state
      * @param controlGroup
      * @param versionable
      * @param label
      * @param co
      */
     private String addDatastream( String id,
                                   State state,
                                   ControlGroup controlGroup,
                                   boolean versionable ) throws XPathExpressionException, IOException
     {
         Element ds = doc.createElementNS( FOXML_NS, "foxml:datastream" );
         ds.setAttribute( "ID", id );
         ds.setAttribute( "STATE", state.toString() );
         ds.setAttribute( "CONTROL_GROUP", controlGroup.toString() );
         ds.setAttribute( "VERSIONABLE", Boolean.toString( versionable ) );
         rootElement.appendChild( ds );
 
         return id;
     }
 
 
     private void addDatastreamVersion( String dsId,
                                        String dsvId,
                                        String mimeType,
                                        String label,
                                        int size,
                                        String created ) throws XPathExpressionException
     {
 
         /**
         * \Todo: This if will always be false, whats the purpose? bug: 9767 
          */
         if ( false && dsId.contains( ":" )) {
             throw new IllegalArgumentException( String.format( " addDataStreamVersion called with id containing : ID=\"%s\"", dsId ) );
         }
         String expr = String.format( "//foxml:datastream[@ID='%s']", dsId );
         NodeList nodes = (NodeList) xpath.evaluate( expr, doc, XPathConstants.NODESET );
         Node node = nodes.item( 0 );
         if( node == null )
         {
             throw new IllegalArgumentException( dsId + "does not exist." );
         }
 
         if( dsvId == null || dsvId.equals( "" ) )
         {
             dsvId = dsId + ".0";
         }
 
         Element dsv = doc.createElementNS( FOXML_NS, "foxml:datastreamVersion" );
         dsv.setAttribute( "ID", dsvId );
         dsv.setAttribute( "MIMETYPE", mimeType );
         dsv.setAttribute( "LABEL", label );
         if( size != 0 )
         {
             dsv.setAttribute( "SIZE", Integer.toString( size ) );
         }
         dsv.setAttribute( "CREATED", created );
         node.appendChild( dsv );
     }
 
 
     /**
      * Adds a dublincore xml document (in the form of a string) to the Digital
      * Object. There is no checks regarding the validity of the dublin core xml
      * or even the well-formedness of the xml contained in the string
      * (allthough) a SAXException will be thrown when trying to add
      * non-well-formed xml to the Digital Object xml document.
      * @param dcdata the dublin core xml document in a string representation.
      * @param timenow a long suitable for ingesting into {@link java.util.Date(long)}
      * @throws XPathExpressionException
      */
     public void addDublinCoreDatastream( String dcdata, long timenow ) throws XPathExpressionException, SAXException, IOException
     {
         String label = "Dublin Core data";
         String id = "DC";
         this.addXmlContent( id, dcdata, label, timenow, true );
     }
 
     /**
      * </foxml:datastream>
 <foxml:datastream ID="RELS-EXT" STATE="A" CONTROL_GROUP="X" VERSIONABLE="false">
 <foxml:datastreamVersion ID="RELS-EXT.0" LABEL="Relationships" CREATED="2009-10-29T21:46:25.715Z" MIMETYPE="application/rdf+xml" FORMAT_URI="info:fedora/fedora-system:FedoraRELSExt-1.0" SIZE="235">
 <foxml:xmlContent>
 <rdf:RDF xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#">
 
 <rdf:Description rdf:about="info:fedora/710100:20071273">
         <isMemberOfWork xmlns="http://oss.dbc.dk/rdf/dkbib#">work:3</isMemberOfWork>
 </rdf:Description>
 
 </rdf:RDF>
 </foxml:xmlContent>
 </foxml:datastreamVersion>
 </foxml:datastream>
 
      * @param relsContent
      * @param timenow
      * @throws IOException 
      * @throws XPathExpressionException 
      * @throws SAXException 
      */
     public void addRelsExtDataStream( String relsContent, long timenow) throws XPathExpressionException, IOException, SAXException {
         String label="Relationships";
         String id = "RELS-EXT";
         String format_URI="info:fedora/fedora-system:FedoraRELSExt-1.0";
         
         String datastreamId = id;
         String xmlContent = relsContent;
         boolean versionable = false;
         
 
         String dsId = addDatastream( datastreamId, State.A, ControlGroup.X, versionable );
         String dsvId = dsId + ".0";
 
         log.debug( String.format( "addXmlContent: %s, %s, %s, %s", datastreamId, xmlContent, label, dsId ) );
 
         addDatastreamVersion( dsId, dsvId, "application/rdf+xml", label, xmlContent.length(), getTimestamp( timenow ) );
         Document contentDoc = builder.parse( new InputSource( new StringReader( xmlContent ) ) );
         Node importedContent = doc.adoptNode( contentDoc.getDocumentElement() );
         Node dsv = getDatastreamVersion( dsvId );
         Element content = doc.createElementNS( FOXML_NS, "foxml:xmlContent" );
         dsv.appendChild( content );
         content.appendChild( importedContent );
 
         
     }
 
     /**
      * Constructs a datastream and a datastreamversion in the Digital Object.
      * The mimetype of the datastream is set to "text/xml" no matter what mimetype
      * the delivered {@code xmlContent} has.
      * 
      * @param datastreamId id for the datastream to be contructed inside the Digital Object
      * @param xmlContent the xml document in a string representation.
      * @param versionable a boolean indicating whether the fedora repository
      *        should manage versioning of the datastream
      * @throws SAXException
      * @throws IOException
      */
     public void addXmlContent( String datastreamId, String xmlContent, String label, long timenow, boolean versionable ) throws SAXException, IOException, XPathExpressionException
     {
         String dsId = addDatastream( datastreamId, State.A, ControlGroup.X, versionable );
         String dsvId = dsId + ".0";
 
         log.debug( String.format( "addXmlContent: %s, %s, %s, %s", datastreamId, xmlContent, label, dsId ) );
 
         addDatastreamVersion( dsId, dsvId, "text/xml", label, xmlContent.length(), getTimestamp( timenow ) );
         Document contentDoc = builder.parse( new InputSource( new StringReader( xmlContent ) ) );
         Node importedContent = doc.adoptNode( contentDoc.getDocumentElement() );
         Node dsv = getDatastreamVersion( dsvId );
         Element content = doc.createElementNS( FOXML_NS, "foxml:xmlContent" );
         dsv.appendChild( content );
         content.appendChild( importedContent );
     }
 
 
     /**
      * Method for adding data to the Digital Object, which will not be
      * interpreted by the serialization mechanism or by the fedora repository
      * 
      * @param datastreamId id for the datastream to be contructed inside the Digital Object
      * @param content a byte[] containing the data
      * @param timenow a long suitable for ingesting into {@link java.util.Date(long)}
      * @throws SAXException
      * @throws IOException
      */
     public void addBinaryContent( String datastreamId, byte[] content, String label, String mime, long timenow ) throws IOException, XPathExpressionException
     {
         String dsId = addDatastream( datastreamId, State.A, ControlGroup.M, false );
         String dsvId = dsId + ".0";
         addDatastreamVersion( dsId, dsvId, mime, label, content.length, getTimestamp( timenow ) );
         String b = Base64.encodeToString( content );
         Node dsv = getDatastreamVersion( dsvId );
         Element binelement = doc.createElementNS( FOXML_NS, "foxml:binaryContent" );
         dsv.appendChild( binelement );
         binelement.setTextContent( b );
     }
 
 
     /**
      * Add a content location as a datastream on the Digital Object.
      * The content can be either stored internally in the fedora repository,
      * in which case type should be set to {@link INTERNAL_REF}, or externally stored
      * in which case type should be set to {@link URL}. This method performs no checks
      * on whether the content referred by ref actually exists or is reachable,
      * nor does it perform any checks on the uri-scheme of ref.
      *
      * @param datastreamId id for the datastream to be contructed inside the Digital Object
      * @param ref url that references the content
      * @param type LocationType specifying what kind of reference {@link ref} denotes
      * @throws XPathExpressionException
      */
     public void addContentLocation( String datastreamId, String ref, String label, String mimetype, LocationType type, long timenow ) throws XPathExpressionException, SAXException, IOException
     {
         String dsId = addDatastream( datastreamId, State.A, ControlGroup.E, true );
         String dsvId = dsId + ".0";
         addDatastreamVersion( dsId, dsvId, mimetype, label, 0, getTimestamp( timenow ) );
         String expr = String.format( "//foxml:datastreamVersion[@ID='%s']/foxml:contentLocation", datastreamId );
 
         NodeList nodes = (NodeList) xpath.evaluate( expr, doc, XPathConstants.NODESET );
         Element location = (Element) nodes.item( 0 );
         if( location == null )
         {
             location = setContentLocationElement( dsvId );
         }
 
         location.setAttribute( "REF", ref );
         location.setAttribute( "TYPE", type.name() );
     }
 
 
     private Element setContentLocationElement( String dsvId )
     {
         Node node = getDatastreamVersion( dsvId );
         Element location = doc.createElementNS( FOXML_NS, "foxml:contentLocation" );
         node.appendChild( location );
 
         return location;
     }
 
 
     /**
      * Get datastreamversion identified by dsvId
      */
     private Node getDatastreamVersion( String dsvId )
     {
         String expr = String.format( "//foxml:datastreamVersion[@ID='%s']", dsvId );
 
         try
         {
             NodeList nodes = (NodeList) xpath.evaluate( expr, doc, XPathConstants.NODESET );
             Node node = nodes.item( 0 );
             if( node == null )
             {
                 throw new IllegalArgumentException( String.format( "%s does not exist.", dsvId ) );
             }
 
             return node;
         }
         catch( XPathExpressionException e )
         {
             throw new IllegalArgumentException( String.format( "%s does not exist.", dsvId ) );
         }
     }
 
 
     /**
      * gets a string representation of a timestamp. If 0l is given as an
      * argument, a timestamp constructed from System.currentTimeMillis is
      * returned
      */
     private String getTimestamp( long time )
     {
         if( time == 0 )
         {
             time = System.currentTimeMillis();
         }
 
         return new SimpleDateFormat( "yyyy-MM-dd'T'HH:mm:ss.SSS" ).format( new Date( time ) );
     }
 
 
     /**
      * Serializes the FoxmlDocument into a foxml 1.1 string representation that
      * is written to the OutputStream
      * @param out the OutputStream to write the foxml serialization to
      * @param schemaurl Optional: Schema to validate the serialization against. If no validation is wanted, set this parameter to null
      * @throws TransformerConfigurationException
      * @throws TransformerException
      */
     public void serialize( OutputStream out, URL schemaurl ) throws TransformerConfigurationException, TransformerException, SAXException, IOException
     {
         Transformer idTransform;
         idTransform = xformFactory.newTransformer();
         Source input = new DOMSource( doc );
         if( schemaurl != null )
         {
             SchemaFactory schemaf = javax.xml.validation.SchemaFactory.newInstance( FOXML_NS );
             Schema schema = schemaf.newSchema( schemaurl );
             Validator validator = schema.newValidator();
             validator.validate( input );
         }
 
         Result output = new StreamResult( out );
         idTransform.transform( input, output );
     }
 }
