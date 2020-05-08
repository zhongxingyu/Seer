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
 package dk.dbc.opensearch.common.metadata;
 
 import dk.dbc.opensearch.common.types.CargoContainer;
 import dk.dbc.opensearch.common.types.CargoObject;
 import dk.dbc.opensearch.common.types.DataStreamType;
 import dk.dbc.opensearch.common.types.IndexingAlias;
 import dk.dbc.opensearch.common.types.InputPair;
 
 import dk.dbc.opensearch.common.types.OpenSearchTransformException;
 import java.io.ByteArrayInputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 import javax.xml.stream.XMLEventReader;
 import javax.xml.stream.XMLInputFactory;
 import javax.xml.stream.XMLOutputFactory;
 import javax.xml.stream.XMLStreamConstants;
 import javax.xml.stream.XMLStreamException;
 import javax.xml.stream.XMLStreamReader;
 import javax.xml.stream.XMLStreamWriter;
 import javax.xml.stream.events.Attribute;
 import javax.xml.stream.events.StartElement;
 import javax.xml.stream.events.XMLEvent;
 import javax.xml.transform.Source;
 import javax.xml.transform.stream.StreamSource;
 import javax.xml.validation.Schema;
 import javax.xml.validation.SchemaFactory;
 import javax.xml.validation.Validator;
 import org.apache.log4j.Logger;
 import org.xml.sax.SAXException;
 
 
 /**
  * Represents metadata that describes (for third party
  * components/libraries) which data is contained in a CargoContainer
  * and how it can be reconstructed using the information contained in
  * the administration stream.
  *
  * This class is primarily intended to be used with serializing, but
  * it can be used in general to retrieve information on the
  * CargoContainer.
  */
 public class AdministrationStream implements MetaData
 {
     private static final String schemaString = "<xsd:schema xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\"><xsd:attribute name=\"name\" type=\"xsd:string\"/><xsd:element name=\"admin-stream\"><xsd:complexType><xsd:sequence><xsd:element name=\"indexingalias\"><xsd:complexType><xsd:attribute ref=\"name\" use=\"required\"/></xsd:complexType></xsd:element><xsd:element name=\"streams\"><xsd:complexType><xsd:sequence><xsd:element name=\"stream\" maxOccurs=\"unbounded\"><xsd:complexType><xsd:attribute name=\"format\" type=\"xsd:string\" use=\"required\"/><xsd:attribute name=\"id\" type=\"xsd:string\" use=\"required\"/><xsd:attribute name=\"index\" type=\"xsd:string\" use=\"required\"/><xsd:attribute name=\"lang\" type=\"xsd:string\" use=\"required\"/><xsd:attribute name=\"mimetype\" type=\"xsd:string\" use=\"required\"/><xsd:attribute name=\"streamNameType\" type=\"xsd:string\" use=\"required\"/><xsd:attribute name=\"submitter\" type=\"xsd:string\" use=\"required\"/></xsd:complexType></xsd:element></xsd:sequence></xsd:complexType></xsd:element></xsd:sequence></xsd:complexType></xsd:element></xsd:schema>";
 
     private Map<Integer, HashMap<AdministrationStreamElement, String>> admvalues;
     private static final String identifier = "adminData";
 
     /**
      * The DataStreamType identifying the metadata.
      */
     public static final DataStreamType type = DataStreamType.AdminData;
 
     private static Logger log = Logger.getLogger( AdministrationStream.class );
     private String indexingAlias;
 
     /**
      * Constructs an empty AdministrationStream object. The client can use the
      * {@link #addStream(CargoObject, String) to add information about
      * data into the AdministrationStream.
      */
     public AdministrationStream( String indexingAlias )
     {
         this.indexingAlias = indexingAlias;
         admvalues = new HashMap<Integer, HashMap<AdministrationStreamElement, String>>();
     }
 
     /**
      * Constructs an AdministrationStream object from
      * adminstrationstream xml, optionally validating the input XML
      * {@link InputStream}. Not validating the input xml saves time,
      * but it is then the responsibility of the client to ensure that
      * the input xml is valid and parsable according to the
      * administration stream xml Schema.
      */
     public AdministrationStream( InputStream in, boolean validating ) throws XMLStreamException, SAXException, IOException
     {
         if( in == null )
         {
             String error = String.format( "InputStream is null, cannot construct adminstration stream" );
             log.error( error );
             throw new IllegalStateException( error );
         }
         
         if( validating )
         {
             String ADM_NS = "http://www.w3.org/2001/XMLSchema";
             Source schemaurl = new StreamSource( new ByteArrayInputStream( schemaString.getBytes() ) );
             SchemaFactory schemaf = javax.xml.validation.SchemaFactory.newInstance( ADM_NS );
             Schema schema = schemaf.newSchema( schemaurl );
             Validator validator = schema.newValidator();
             Source inXML = new StreamSource( in );
             validator.validate( inXML );
             in.reset();
         }
 
         admvalues = new HashMap<Integer, HashMap<AdministrationStreamElement, String>>();
 
         XMLInputFactory infac = XMLInputFactory.newInstance();
         XMLStreamReader parser = infac.createXMLStreamReader( in );
         XMLEventReader eventReader = infac.createXMLEventReader( parser );
 
         XMLEvent event;
 
         StartElement element;
         int counter = 0;
         while( parser.hasNext() )
         {
             event = eventReader.nextEvent();
             if( XMLStreamConstants.START_ELEMENT == event.getEventType() )
             {
                 element = event.asStartElement();
                 if( element.getName().getLocalPart().equals( "indexingalias" ) )
                 {
                     Attribute name = (Attribute) element.getAttributes().next(); //we assume only one attribute here.
                     this.indexingAlias = name.getValue();
                 }
                 else if( element.getName().getLocalPart().equals( "stream" ) )
                 {
                     admvalues.put( counter, new HashMap<AdministrationStreamElement, String>() );
                     HashMap<AdministrationStreamElement, String> stream = admvalues.get( counter++ );
 
                     Attribute attribute;
                     for( Iterator<Attribute> attributeIter = element.getAttributes(); attributeIter.hasNext(); )
                     {
                         attribute = attributeIter.next();
                         stream.put( AdministrationStreamElement.valueOf( attribute.getName().getLocalPart().toUpperCase() ), attribute.getValue() );
                     }
                 }
             }
         }
         assert (admvalues.size() == counter);
         assert (this.indexingAlias != null );
     }
 
     /**
      * Adds information about a data object as a stream in the
      * administrationstream. {@code id} by convention is the
      * DataStreamId.
      */
     public boolean addStream( CargoObject obj, String id )
     {
         int pos = admvalues.size();
         boolean added = false;
         HashMap<AdministrationStreamElement, String> map = new HashMap<AdministrationStreamElement, String>( 6 );
         map.put( AdministrationStreamElement.FORMAT, obj.getFormat() );
         map.put( AdministrationStreamElement.INDEX, Integer.toString( pos ) );
         map.put( AdministrationStreamElement.LANG, obj.getLang() );
         map.put( AdministrationStreamElement.MIMETYPE, obj.getMimeType() );
         map.put( AdministrationStreamElement.STREAMNAMETYPE, obj.getDataStreamType().getName() );
         map.put( AdministrationStreamElement.SUBMITTER, obj.getSubmitter() );
         map.put( AdministrationStreamElement.ID, id );
         admvalues.put( new Integer( pos ), map );
         if( pos + 1 == admvalues.size() )
         {
             added = true;
         }
         return added;
     }
 
     public boolean addStream( MetaData obj, String id )
     {
         boolean added = false;
         if( obj.getType() == DataStreamType.AdminData )
         {
             log.warn( "Refusing to add adminstream data information to admin stream" );
             added = false;
         }
         else if( obj.getType() == DataStreamType.DublinCoreData )
         {
             DublinCore dc = (DublinCore) obj;
 
             int pos = admvalues.size();
             HashMap<AdministrationStreamElement, String> map = new HashMap<AdministrationStreamElement, String>( 6 );
             map.put( AdministrationStreamElement.FORMAT, dc.getType().getName() );
             map.put( AdministrationStreamElement.INDEX, Integer.toString( pos ) );
             map.put( AdministrationStreamElement.LANG, "dc" );
             map.put( AdministrationStreamElement.MIMETYPE, "text/xml" );
             map.put( AdministrationStreamElement.STREAMNAMETYPE, dc.getType().getName() );
             map.put( AdministrationStreamElement.SUBMITTER, "dbc" );
             map.put( AdministrationStreamElement.ID, id );
             admvalues.put( new Integer( pos ), map );
             if( pos + 1 == admvalues.size() )
             {
                 added = true;
             }
         }
         return added;
     }
 
 
     /**
      * Returns a {@link List} containing an {@link InputPair}
      * containing an {@link Integer} denoting the index position of
      * the data element and another {@link InputPair} containing a
      * {@link CargoObject} and its identifier (which by convention is
      * the DataStreamId).
      */
     public List<InputPair<Integer, InputPair<String, CargoObject>>> getStreams() throws IOException
     {
         List<InputPair<Integer, InputPair<String, CargoObject>>> retlist = new ArrayList<InputPair<Integer, InputPair<String,CargoObject>>>( admvalues.size() );
         CargoContainer cargo = new CargoContainer();
         for( Entry<Integer, HashMap<AdministrationStreamElement, String>> set : admvalues.entrySet() )
         {
             cargo.add( DataStreamType.getDataStreamTypeFrom(
                     set.getValue().get( AdministrationStreamElement.STREAMNAMETYPE ) ),
                     set.getValue().get( AdministrationStreamElement.FORMAT ),
                     set.getValue().get( AdministrationStreamElement.SUBMITTER ),
                     set.getValue().get( AdministrationStreamElement.LANG ),
                     set.getValue().get( AdministrationStreamElement.MIMETYPE ),
                     IndexingAlias.getIndexingAlias( this.indexingAlias ),
                     "ihatefakedata".getBytes() );
             //String is the datastreamId and CargoObject holds all metadata on the stream, sans the data itself
             InputPair<String, CargoObject> indexvalue = new InputPair<String, CargoObject>( 
                     set.getValue().get( AdministrationStreamElement.ID ),
                     cargo.getCargoObject( DataStreamType.getDataStreamTypeFrom( set.getValue().get( AdministrationStreamElement.STREAMNAMETYPE ) ) ) );
             retlist.add( new InputPair<Integer, InputPair<String,CargoObject>>( set.getKey(), indexvalue ) );
         }
         return retlist;
     }
 
 
     /**
      * Removes a stream from the administrationstream identified by {@code id}.
      */
     public boolean removeStream( String id )
     {
         boolean success = false;
         Integer index = null;
         for( Entry<Integer, HashMap<AdministrationStreamElement, String>> set : admvalues.entrySet() )
         {
             for( Entry<AdministrationStreamElement, String> streamelem : set.getValue().entrySet() )
             {
                 if( streamelem.getKey() == AdministrationStreamElement.ID )
                 {
                     if( streamelem.getValue().toLowerCase().equals( id.toLowerCase() ) )
                     {
                         index = set.getKey();
                     }
                 }
             }
         }
         if( index == null )
         {
             log.warn( String.format( "Could not remove stream with id %s", id ) );
         }
         else
         {
             admvalues.remove( index );
             success = true;
         }
         return success;
     }
 
     /**
      * Gets the number of streams contained in the administrationstream.
      */
     public int getCount()
     {
         return admvalues.size();
     }
 
 
     /**
      * Gets the number of streams contained in the
      * administrationstream that has the {@link DataStreamType} {@code
      * dst}.
      */
     public int getCount( DataStreamType dst )
     {
         int counter = 0;
         for( Entry<Integer, HashMap<AdministrationStreamElement, String>> set : admvalues.entrySet() )
         {
             for( Entry<AdministrationStreamElement, String> streamelem : set.getValue().entrySet() )
             {
                 if( streamelem.getKey() == AdministrationStreamElement.STREAMNAMETYPE )
                 {
                     if( streamelem.getValue().toLowerCase().equals( dst.getName().toLowerCase() ) )
                     {
                         counter++;
                     }
                 }
             }
         }
         return counter;
     }
 
 
     /**
     <admin-stream>
     <indexingalias name="danmarcxchange"/>
     <streams>
     <stream format="katalog" id="originalData.0" index="0" lang="da" mimetype="text/xml"
     streamNameType="originalData" submitter="710100"/>
     <stream format="dc" id="dublinCoreData.0" index="1" lang="da" mimetype="text/xml"
     streamNameType="dublinCoreData" submitter="dbc"/>
     </streams>
     </admin-stream>
      */
     @Override
     public void serialize( OutputStream out ) throws OpenSearchTransformException
     {
         if( this.indexingAlias == null )
         {
             String error = "Refusing to serialize AdministrationStream with no content";
             log.error( error );
             throw new IllegalStateException( error );
         }
 
         // Create an output factory
         XMLOutputFactory xmlof = XMLOutputFactory.newInstance();
         XMLStreamWriter xmlw;
         try
         {
             xmlw = xmlof.createXMLStreamWriter( out );
             // Set namespace prefix defaulting for all created writers
 
             xmlw.writeStartDocument();
             xmlw.writeStartElement( "admin-stream" );
             xmlw.writeStartElement( "indexingalias" );
             xmlw.writeAttribute( "name", this.indexingAlias );
             xmlw.writeEndElement();//closes "indexingalias" element
 
             xmlw.writeStartElement( "streams" );
             for( Entry<Integer, HashMap<AdministrationStreamElement, String>> set : admvalues.entrySet() )
             {
                 xmlw.writeStartElement( "stream" );
                 for( Entry<AdministrationStreamElement, String> streamelem : set.getValue().entrySet() )
                 {
                     xmlw.writeAttribute( streamelem.getKey().localName(), streamelem.getValue() );
                 }
                 xmlw.writeEndElement();//closes "stream" element
 
             }
             xmlw.writeEndElement();//closes "streams" element
             xmlw.writeEndElement();//closes "admin-stream" element
             xmlw.writeEndDocument();//closes document
             xmlw.flush();
         }
         catch( XMLStreamException ex )
         {
             String error = "Could not write to stream writer";
             log.error( error );
             throw new OpenSearchTransformException( error, ex );
 
         }
 
     }
 
     /**
      * This implementation returns the identifier as identifier
      * @return
      */
     @Override
     public String getIdentifier()
     {
         return identifier;
     }
 
     /**
      *
      * @return the type of this metadata element
      */
     @Override
     public DataStreamType getType()
     {
         return type;
     }


 }
