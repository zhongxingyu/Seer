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
  * \file
  * \brief
  */
 
 
 package dk.dbc.opensearch.common.metadata;
 
 import dk.dbc.opensearch.common.fedora.FedoraNamespaceContext;
 import dk.dbc.opensearch.common.fedora.FedoraNamespaceContext.FedoraNamespace;
 import dk.dbc.opensearch.common.types.DataStreamType;
 import dk.dbc.opensearch.common.types.OpenSearchTransformException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.text.SimpleDateFormat;
 import java.util.Collections;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Map.Entry;
 import javax.xml.namespace.QName;
 import javax.xml.stream.XMLEventReader;
 import javax.xml.stream.XMLInputFactory;
 import javax.xml.stream.XMLOutputFactory;
 import javax.xml.stream.XMLStreamConstants;
 import javax.xml.stream.XMLStreamException;
 import javax.xml.stream.XMLStreamReader;
 import javax.xml.stream.XMLStreamWriter;
 import javax.xml.stream.events.Characters;
 import javax.xml.stream.events.StartElement;
 import javax.xml.stream.events.EndElement;
 import javax.xml.stream.events.XMLEvent;
 import org.apache.log4j.Logger;
 
 
 /**
  * DublinCore is a class implementation of the Dublin Core Metadata Element Set,
  * Version 1.1 (http://dublincore.org/documents/dces/)
  *
  */
 public class DublinCore implements MetaData{
 
     private static final SimpleDateFormat dateFormat = new SimpleDateFormat( "yyyy-MM-dd'T'HH:mm:ss.SSS" );
 
     private Map<DublinCoreElement, String> dcvalues;
 
     public static final DataStreamType type = DataStreamType.DublinCoreData;
 
     private Logger log = Logger.getLogger( DublinCore.class );
     private static FedoraNamespace dc = new FedoraNamespaceContext().getNamespace( "dc" );
     private static FedoraNamespace oai_dc = new FedoraNamespaceContext().getNamespace( "oai_dc" );
 
     /**
      * Initializes an empty Dublin Core element, identified by {@code identifier}
      * @param identifier An unambiguous reference to the resource within a given
      * context. Recommended best practice is to use the digital repository object
      * identifier.
      */
     public DublinCore( String identifier )
     {
         dcvalues = new HashMap<DublinCoreElement, String>();
         dcvalues.put( DublinCoreElement.ELEMENT_IDENTIFIER, identifier);
     }
 
     /**
      * Initializes a Dublin Core element with values taken from {@code
      * inputValues}, identified by {@code identifier}
      * @param identifier An unambiguous reference to the resource within a given
      * context. Recommended best practice is to use the digital repository object
      * identifier.
      */
     public DublinCore( String identifier, Map<DublinCoreElement, String> inputValues )
     {
         dcvalues = new HashMap<DublinCoreElement, String>( inputValues );
         dcvalues.put( DublinCoreElement.ELEMENT_IDENTIFIER, identifier);
 
     }
 
     /**
      * Initializes a dublin core object based on the dublin core
      * document provided with {@code cdDocument}. No validation is performed on
      * the InputStream but values outside the dublin core metadata element set
      * is ignored.
      */
     public DublinCore( InputStream dcIn ) throws XMLStreamException
     {
         dcvalues = new HashMap<DublinCoreElement, String>();
         
         XMLInputFactory infac = XMLInputFactory.newInstance();
         XMLEventReader eventReader = infac.createXMLEventReader( dcIn );
 
         StartElement element;
         Characters chars;
 
         DublinCoreElement elementName = null;
         String elementText = null;
         while (eventReader.hasNext()) {
             XMLEvent event = (XMLEvent)eventReader.next();
             switch ( event.getEventType() )
             {
             case XMLStreamConstants.START_ELEMENT:
                 element = event.asStartElement();
                log.trace( String.format( "Got element: %s", element.getName().toString() ) );
                 if( element.getName().getPrefix().equals( dc.getPrefix() ) )
                 {
                     if( DublinCoreElement.hasLocalName( element.getName().getLocalPart() ) )
                     {
                         elementName = DublinCoreElement.fromString( element.getName().getLocalPart() );
                     }
                 }
                 break;
             case XMLStreamConstants.CHARACTERS:
                 chars = event.asCharacters();
                if( ! chars.getData().trim().isEmpty() )
                {
                    elementText = chars.getData();
                    log.trace( String.format( "Got text: %s", elementText ) );
                }
                 break;
             }
             if( elementName != null && elementText != null )
             {
                log.trace( String.format( "Adding element: '%s':'%s'", elementName, elementText ) );
                 dcvalues.put( elementName, elementText );
                 elementName = null;
                 elementText = null;
             }
 
         }
         eventReader.close();
 
     }
     
     public void setContributor( String contributor )
     {
         dcvalues.put( DublinCoreElement.ELEMENT_CONTRIBUTOR, contributor );
     }
     public void setCoverage( String coverage )
     {
         dcvalues.put( DublinCoreElement.ELEMENT_COVERAGE, coverage );
     }
     public void setCreator( String creator )
     {
         dcvalues.put( DublinCoreElement.ELEMENT_CREATOR, creator );
     }
     public void setDate( Date date )
     {
         String stringdate = dateFormat.format( date );
         dcvalues.put( DublinCoreElement.ELEMENT_DATE, stringdate );
     }
     public void setDescription( String description )
     {
         dcvalues.put( DublinCoreElement.ELEMENT_DESCRIPTION, description );
     }
     public void setFormat( String format )
     {
         dcvalues.put( DublinCoreElement.ELEMENT_FORMAT, format );
     }
     public void setIdentifier( String identifier )
     {
         if( dcvalues.containsKey( DublinCoreElement.ELEMENT_IDENTIFIER ) )
         {
             log.warn( String.format( "Overwriting DC identifier %s with %s", dcvalues.get( DublinCoreElement.ELEMENT_IDENTIFIER ), identifier ) );
         }
         dcvalues.put( DublinCoreElement.ELEMENT_IDENTIFIER, identifier );
     }
     public void setLanguage( String language )
     {
         dcvalues.put( DublinCoreElement.ELEMENT_LANGUAGE, language );
     }
     public void setPublisher( String publisher)
     {
         dcvalues.put( DublinCoreElement.ELEMENT_PUBLISHER, publisher );
     }
     public void setRelation( String relation )
     {
         dcvalues.put( DublinCoreElement.ELEMENT_RELATION, relation );
     }
     public void setRights( String rights )
     {
         dcvalues.put( DublinCoreElement.ELEMENT_RIGHTS, rights );
     }
     public void setSource( String source )
     {
         dcvalues.put( DublinCoreElement.ELEMENT_SOURCE, source );
     }
     public void setSubject( String subject )
     {
         dcvalues.put( DublinCoreElement.ELEMENT_SUBJECT, subject );
     }
     public void setTitle( String title )
     {
         dcvalues.put( DublinCoreElement.ELEMENT_TITLE, title );
     }
     public void setType( String type )
     {
         dcvalues.put( DublinCoreElement.ELEMENT_TYPE, type );
     }
 
     public int elementCount()
     {
         return dcvalues.size();
     }
 
 

    /** 
     * Retrieves values associated with the {@link DublinCoreElement} {@code
     * dcElement}. If no value was registered with the element, this
     * method will return an empty String.
     * 
     * @param dcElement a {@link DublinCoreElement}
     * 
     * @return the value associated with {@code dcElement} or an empty String
     */
     public String getDCValue( DublinCoreElement dcElement )
     {
         String retval = dcvalues.get( dcElement );
         if( retval == null )
         {
            log.warn( String.format( "No value registered for element %s", dcElement ) );
            retval = "";
         }
         return retval;
     }
 
     /* Example of output:
     <oai_dc:dc xmlns:dc="http://purl.org/dc/elements/1.1/"
     xmlns:oai_dc="http://www.openarchives.org/OAI/2.0/oai_dc/"
     xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
     xsi:schemaLocation="http://www.openarchives.org/OAI/2.0/oai_dc/
     http://www.openarchives.org/OAI/2.0/oai_dc.xsd">
     <dc:title>Harry Potter og Fønixordenen</dc:title>
     <dc:creator>Joanne K. Rowling</dc:creator>
     <dc:type>Bog</dc:type>
     <dc:identifier>710100:25082427</dc:identifier>
     <dc:source>Harry Potter and the Order of the Phoenix</dc:source>
     <dc:relation/>
     </oai_dc:dc>
      */
     @Override
     public void serialize( OutputStream out )throws OpenSearchTransformException
     {
 
         // Create an output factory
         XMLOutputFactory xmlof = XMLOutputFactory.newInstance();
         XMLStreamWriter xmlw;
         try
         {
             xmlw = xmlof.createXMLStreamWriter( out );
 
             xmlw.setDefaultNamespace( oai_dc.getURI() );
 
             xmlw.writeStartDocument();
             xmlw.writeStartElement( oai_dc.getURI(), dc.getPrefix() );
             xmlw.writeNamespace( oai_dc.getPrefix(), oai_dc.getURI() );
             xmlw.writeNamespace( dc.getPrefix(), dc.getURI() );
 
             for( Entry<DublinCoreElement, String> set : dcvalues.entrySet() )
             {
                 xmlw.writeStartElement( dc.getURI(), set.getKey().localName() );
                 if( set.getValue() != null )
                 {
                     xmlw.writeCharacters( set.getValue() );
                 }
                 xmlw.writeEndElement();
 
             }
             xmlw.writeEndElement();//closes "oai_dc:dc" element
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
 
     @Override
     public String getIdentifier()
     {
         return dcvalues.get( DublinCoreElement.ELEMENT_IDENTIFIER );
     }
 
     /**
      *
      * @return the type of this metadata element
      */
     public DataStreamType getType()
     {
         return type;
     }
     
 }
