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
  * \file MarcxchangeHarvester.java
  * \brief 
  */
 
 
 package dk.dbc.opensearch.plugins;
 
 
 import dk.dbc.opensearch.common.helpers.OpensearchNamespaceContext;
 import dk.dbc.opensearch.common.xml.XMLUtils;
 import dk.dbc.opensearch.common.pluginframework.ICreateCargoContainer;
 import dk.dbc.opensearch.common.pluginframework.PluginException;
 import dk.dbc.opensearch.common.pluginframework.PluginType;
 import dk.dbc.opensearch.common.types.CargoContainer;
 import dk.dbc.opensearch.common.types.CargoObject;
 import dk.dbc.opensearch.common.types.DataStreamType;
 import dk.dbc.opensearch.common.types.InputPair;
 import dk.dbc.opensearch.common.types.Pair;
 import dk.dbc.opensearch.components.datadock.DatadockJob;
 import dk.dbc.opensearch.common.types.IndexingAlias;
 
 import java.io.ByteArrayInputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.UnsupportedEncodingException;
 
 import javax.xml.namespace.NamespaceContext;
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.parsers.ParserConfigurationException;
 import javax.xml.transform.TransformerException;
 import javax.xml.xpath.XPath;
 import javax.xml.xpath.XPathExpression;
 import javax.xml.xpath.XPathExpressionException;
 import javax.xml.xpath.XPathFactory;
 
 import org.apache.log4j.Logger;
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.xml.sax.InputSource;
 
 
 public class MarcxchangeHarvester implements ICreateCargoContainer
 {
     private static Logger log = Logger.getLogger( MarcxchangeHarvester.class );
 
     
     //    private String submitter;
     //private String format;
     //private byte[] data;
 
     private PluginType pluginType = PluginType.HARVEST;
 
 
     /**
      * \todo: Implement this method
      *
      * @param data
      * @param xml
      * @return
      * @throws PluginException
      */
     public CargoContainer getCargoContainer( InputStream data, InputStream xml) throws PluginException
     {
         throw new PluginException( "Not implemented yet!" );
     }
     
     
     public CargoContainer getCargoContainer( DatadockJob job, byte[] data ) throws PluginException
     {
 //         this.submitter = job.getSubmitter();
 //         this.format = job.getFormat();
 //         this.data = data;
 
         return createCargoContainerFromFile( job.getSubmitter(), job.getFormat() , data );
     }
 
 
     /**
      *
      * @return the CargoContainer from
      * @throws TransformerException 
      * @throws ParserConfigurationException 
      * @throws XPathExpressionException 
      * @throws IOException if the data cannot be read
      */
     private CargoContainer createCargoContainerFromFile( String submitter, String format, byte[] data ) throws PluginException
     {
         CargoContainer cargo = new CargoContainer();
         /** \todo: hardcoded values for mimetype, langugage and data type */
         String mimetype = "text/xml";
         String lang = "da";
         DataStreamType dataStreamName = DataStreamType.OriginalData;
  
         try 
         {
             cargo.add( dataStreamName, format, submitter, lang, mimetype, IndexingAlias.Danmarcxchange, data );
 
             // CONSTRUCTING DC DATASTREAM
             log.debug( "Constructing DC datastream" );
             //byte[] dcByteArray = constructDC( cargo );
             Pair< byte[], CargoContainer > pair = constructDC( cargo );
             byte[] dcByteArray = pair.getFirst();
             log.debug( "MH dcByteArray: " + new String( dcByteArray ) );
             cargo = pair.getSecond();
             log.debug( String.format( "MH cargo dcTitle '%s'", cargo.getDCTitle() ) );
             cargo.add( DataStreamType.DublinCoreData, "dc", "dbc", "da", "text/xml", IndexingAlias.None, dcByteArray );
             
         } 
         catch ( IOException ioe ) 
         {
         	String msg = "Could not construct CargoContainer";
         	log.error( msg );
             throw new PluginException( msg, ioe );
         }
         catch ( Exception e )
         {
             log.error( String.format( "Exception of type: %s cast with message: %s", e.getClass(), e.getMessage() ) );

             /**
              * \Todo: the error should be propagated out to the caller. bug 9582
              */
         }
         
         log.debug(String.format( "num of objects in cargo: %s", cargo.getCargoObjectCount() ) );        
         return cargo;
     }
 
     
     private Pair< byte[], CargoContainer > constructDC( CargoContainer cargo ) throws PluginException 
     {
     	log.debug( "Entering constructDC" );
     	byte[] byteArray = null;
     	
     	CargoObject co = cargo.getCargoObject( DataStreamType.OriginalData );
             
         if ( co == null )
         {
             String error = "Original data CargoObject is null";
             log.error( error );
             throw new IllegalStateException( error );
         }
         
         byte[] b = co.getBytes();       
         log.debug( "CargoObject byteArray: " + new String( b ) );
         
         DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance(); //factory.setNamespaceAware(true);
 	    DocumentBuilder builder;
 		try 
 		{
 			builder = factory.newDocumentBuilder();
 		} 
 		catch ( ParserConfigurationException pce ) 
 		{
 			String msg = "Could not parse configuration";
 			log.error( msg );
 			throw new PluginException( msg, pce );
 		}
 		
 		log.debug( "Building new document before xpath stuff" );
 	    Document dcDoc = builder.newDocument();
 	    Element rootElement = dcDoc.createElementNS( "http://www.openarchives.org/OAI/2.0/oai_dc/", "oai_dc:dc" );    	    
 	    rootElement.setAttributeNS( "http://www.w3.org/2000/xmlns/", "xmlns:oai_dc", "http://www.openarchives.org/OAI/2.0/oai_dc/" );
 	    rootElement.setAttributeNS( "http://www.w3.org/2000/xmlns/", "xmlns:dc", "http://purl.org/dc/elements/1.1/" );
 	    dcDoc.appendChild( rootElement );
         
 	    Element e; 
 	    
 	    /*e = dcDoc.createElement( "dc:identifier" );
 	    e.appendChild( dcDoc.createTextNode( cargo.getDCIdentifier() ) );
         rootElement.appendChild( e );*/
 	    
 	    e = dcDoc.createElement( "dc:title" );    	    
 	    String titleXpathStr = "/ting:container/dkabm:record/dc:title[1]";
 	    log.debug( String.format( "finding dcTitle using xpath: '%s'", titleXpathStr ) );
         String dcTitle;
 		dcTitle = getDCVariable( b, titleXpathStr );
 		e.appendChild( dcDoc.createTextNode( dcTitle ) );
 	    rootElement.appendChild( e );
 	    log.debug( String.format( "cargo setting dcTitle with value '%s'", dcTitle ) );
 	    cargo.setDCTitle( dcTitle );
 	 
 	    e = dcDoc.createElement( "dc:creator" );
 	    String creatorXpathStr = "/ting:container/dkabm:record/dc:creator[1]";
 	    log.debug( String.format( "finding dcCreator using xpath: '%s'", creatorXpathStr ) );
         String dcCreator = getDCVariable( b, creatorXpathStr );
         e.appendChild( dcDoc.createTextNode( dcCreator ) );
 	    rootElement.appendChild( e );
 	    log.debug( String.format( "cargo setting dcCreator with value '%s'", dcCreator ) );
 	    cargo.setDCCreator( dcCreator );
 	 
 	    e = dcDoc.createElement( "dc:type" );
 	    String typeXpathStr = "/ting:container/dkabm:record/dc:type[@xsi:type]";
 	    log.debug( String.format( "finding dcType using xpath: '%s'", typeXpathStr ) );
         String dcType = getDCVariable( b, typeXpathStr );
 	    e.appendChild( dcDoc.createTextNode( dcType ) );
 	    rootElement.appendChild( e );
 	    log.debug( String.format( "cargo setting dcType with value '%s'", dcType ) );
 	    cargo.setDCType( dcType );
 	    
 	    e = dcDoc.createElement( "dc:source" );
 	    String sourceXpathStr = "/ting:container/dkabm:record/dc:source[1]";
 	    log.debug( String.format( "finding dcSource using xpath: '%s'", sourceXpathStr ) );
         String dcSource = getDCVariable( b, sourceXpathStr );            
 		e.appendChild( dcDoc.createTextNode( dcSource ) );
 	    rootElement.appendChild( e );
 	    log.debug( String.format( "cargo setting dcSource with value '%s'", dcSource ) );
 	    cargo.setDCSource( dcSource );
 	    
 	    e = dcDoc.createElement( "dc:relation" );
 	    String relationXpathStr = "/*/*/*/*[@tag='014']/*[@code='a']";
 	    log.debug( String.format( "finding dcRelation using xpath: '%s'", relationXpathStr ) );
         String dcRelation = getDCVariable( b, relationXpathStr );            
 		e.appendChild( dcDoc.createTextNode( dcRelation ) );
 	    rootElement.appendChild( e );
 	    log.debug( String.format( "cargo setting dcRelation with value '%s'", dcRelation ) );
 	    cargo.setDCRelation( dcRelation );	    
     	    
 	    log.debug( String.format( "setting variables in cargo container: dcTitle '%s'; dcCreator '%s'; dcType '%s'; dcSource '%s'", dcTitle, dcCreator, dcType, dcSource ) );
     	try 
     	{
 			byteArray = XMLUtils.getByteArray( rootElement );
 		} 
     	catch ( UnsupportedEncodingException uee ) 
     	{
     		String msg = String.format( "Could obtain byte array due to unsupported encoding. Exception thrown in class '%s'", XMLUtils.class.toString() );
     		log.error( msg );
 			throw new PluginException( msg, uee );
 		} 
     	catch ( TransformerException te ) 
     	{
     		String msg = String.format( "Could not transform dom to stream. Exception thrown in class", XMLUtils.class.toString() );
     		log.error( msg );
 			throw new PluginException( msg, te );
 		}
     	System.out.println( "byte array: " + new String( byteArray ) );    	    	
 
     	Pair< byte[], CargoContainer > ret = new InputPair< byte[], CargoContainer >( byteArray, cargo );    	
     	
 		return ret;
 	}
     
     
     private String getDCVariable( byte[] bytes, String xPathStr ) throws PluginException
     {
     	NamespaceContext nsc = new OpensearchNamespaceContext();
     	XPath xpath = XPathFactory.newInstance().newXPath();
         xpath.setNamespaceContext( nsc );
         XPathExpression xPathExpression = null;        
         
         InputSource workRelationSource = new InputSource( new ByteArrayInputStream( bytes ) );        
         String dcVariable = null;
         
         log.debug( String.format( "MWR xpathStr = '%s'", xPathStr ) );
         try 
         {
 			xPathExpression = xpath.compile( xPathStr );
 			dcVariable = xPathExpression.evaluate( workRelationSource );            
         } 
         catch ( XPathExpressionException xpee ) 
         {
         	String msg = String.format( "Could not evaluate with xpath expression '%s'", xPathExpression );
         	log.error( msg );
 			throw new PluginException( msg, xpee );
 		}
         
         log.debug( String.format( "MWR found dcVariable: '%s'", dcVariable ) );
         
         return dcVariable;
     }
     
     
     public PluginType getPluginType()
     {
         return pluginType;
     }
 }
