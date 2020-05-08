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
 
 
 import dk.dbc.opensearch.common.fedora.FedoraHandle;
 import dk.dbc.opensearch.common.fedora.ObjectRepositoryException;
 import dk.dbc.opensearch.common.helpers.OpensearchNamespaceContext;
 import dk.dbc.opensearch.common.metadata.DublinCore;
 import dk.dbc.opensearch.common.metadata.DublinCoreElement;
 import dk.dbc.opensearch.common.pluginframework.ICreateCargoContainer;
 import dk.dbc.opensearch.common.pluginframework.PluginException;
 import dk.dbc.opensearch.common.pluginframework.PluginType;
 import dk.dbc.opensearch.common.types.CargoContainer;
 import dk.dbc.opensearch.common.types.CargoObject;
 import dk.dbc.opensearch.common.types.DataStreamType;
 import dk.dbc.opensearch.components.datadock.DatadockJob;
 import dk.dbc.opensearch.common.types.IndexingAlias;
 
 import java.io.ByteArrayInputStream;
 import java.io.IOException;
 import java.io.InputStream;
 
 import java.net.MalformedURLException;
 import javax.xml.namespace.NamespaceContext;
 import javax.xml.parsers.ParserConfigurationException;
 import javax.xml.rpc.ServiceException;
 import javax.xml.transform.TransformerException;
 import javax.xml.xpath.XPath;
 import javax.xml.xpath.XPathExpression;
 import javax.xml.xpath.XPathExpressionException;
 import javax.xml.xpath.XPathFactory;
 
 import org.apache.commons.configuration.ConfigurationException;
 import org.apache.log4j.Logger;
 import org.xml.sax.InputSource;
 
 
 /**
  * The MarcxchangeHarvester plugin creates a {@link CargoContainer} with
  * {@link DublinCore} metadata from a marcxchange XML formatted inputdata.
  * The plugin does no explicit validation on the incoming material, and only
  * tries to construct dublin core metadata elements from the input data. If
  * this fails, an empty metadata element will be added to the CargoContainer,
  * which will also contain the (incorrect) data given to the plugin.
  */
 public class MarcxchangeHarvester implements ICreateCargoContainer
 {
     private static Logger log = Logger.getLogger( MarcxchangeHarvester.class );
 
     
     private String submitter;
     private String format;
     private byte[] data;
 
     private PluginType pluginType = PluginType.HARVEST;
     private final FedoraHandle fedoraHandle;
 
     public MarcxchangeHarvester() throws PluginException
     {
         try
         {
             this.fedoraHandle = new FedoraHandle();
         }
         catch( ObjectRepositoryException ex )
         {
             String error = String.format( "Failed to get connection to fedora base" );
             log.error( error );
             throw new PluginException( error, ex );
         }
     }
     
     
     public CargoContainer getCargoContainer( DatadockJob job, byte[] data ) throws PluginException
     {
         this.submitter = job.getSubmitter();
         this.format = job.getFormat();
         this.data = data;
 
         return createCargoContainerFromFile();
     }
 
 
     /**
      *
      * @return the CargoContainer from
      * @throws TransformerException 
      * @throws ParserConfigurationException 
      * @throws XPathExpressionException 
      * @throws IOException if the data cannot be read
      */
     private CargoContainer createCargoContainerFromFile() throws PluginException
     {
         String[] pid = null;
         try
         {
             pid = fedoraHandle.getNextPID( 1,  this.submitter );
         }
         catch( ServiceException ex )
         {
             String error = String.format( "Could not get pid for %s", this.submitter );
             log.error( error );
             throw new PluginException( error, ex );
         }
         catch( ConfigurationException ex )
         {
             String error = String.format( "Could not get pid for %s", this.submitter );
             log.error( error );
             throw new PluginException( error, ex );
         }
         catch( MalformedURLException ex )
         {
             String error = String.format( "Could not get pid for %s", this.submitter );
             log.error( error );
             throw new PluginException( error, ex );
         }
         catch( IOException ex )
         {
             String error = String.format( "Could not get pid for %s", this.submitter );
             log.error( error );
             throw new PluginException( error, ex );
         }
         catch( IllegalStateException ex )
         {
             String error = String.format( "Could not get pid for %s", this.submitter );
             log.error( error );
             throw new PluginException( error, ex );
         }
         if( null == pid && 1 != pid.length )
         {
             String error = String.format( "pid is empty for namespace '%s', but no exception was caught.", this.submitter );
             log.error( error );
             throw new PluginException( new IllegalStateException( error ) );
         }
 
         CargoContainer cargo = new CargoContainer( pid[0] );
  
         try
         {
             /** \todo: hardcoded values for mimetype, language*/
             cargo.add( DataStreamType.OriginalData, this.format, this.submitter, "da", "text/xml", IndexingAlias.Danmarcxchange, data );
 
             log.trace( "Constructing DC datastream" );
 
             DublinCore dcStream = createDublinCore( cargo );
             if( dcStream.elementCount() == 0 )
             {
                 log.warn( String.format( "No information was added to dublin core data for data with pid '%s'", cargo.getIdentifier() ) );
             }
             else
             {
                 log.debug( String.format( "MH cargo dcTitle '%s'", dcStream.getDCValue( DublinCoreElement.ELEMENT_TITLE ) ) );
             }
             cargo.addMetaData( dcStream );
         }
         catch ( IOException ioe )
         {
         	String msg = String.format( "Could not construct CargoContainer %s", ioe.getMessage() );
         	log.error( msg );
             throw new PluginException( msg, ioe );
         }
         if( cargo.getCargoObjectCount() < 1 )
         {
             log.warn( "No objects added to CargoContanier" );
         }
         log.trace(String.format( "num of objects in cargo: %s", cargo.getCargoObjectCount() ) );
        log.trace(String.format( "CargoContainer has DublinCore element == %s", cargo.getDublinCoreMetaData().elementCount() != 0 ) );
         return cargo;
     }
 
     
     private String getDCVariable( byte[] bytes, String xPathStr ) throws PluginException
     {
     	NamespaceContext nsc = new OpensearchNamespaceContext();
     	XPath xpath = XPathFactory.newInstance().newXPath();
         xpath.setNamespaceContext( nsc );
         XPathExpression xPathExpression = null;        
         
         InputSource workRelationSource = new InputSource( new ByteArrayInputStream( bytes ) );        
         String dcVariable = null;
         
         log.debug( String.format( "xpathStr = '%s'", xPathStr ) );
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
         
         log.debug( String.format( "Found dcVariable: '%s'", dcVariable ) );
         
         return dcVariable;
     }
     
     
     public PluginType getPluginType()
     {
         return pluginType;
     }
 
     private DublinCore createDublinCore( CargoContainer cargo ) throws PluginException
     {
         String identifier = cargo.getIdentifier();
         DublinCore dc = new DublinCore( identifier );
     	CargoObject co = cargo.getCargoObject( DataStreamType.OriginalData );
 
         if ( co == null )
         {
             String error = "Original data CargoObject is null";
             log.error( error );
             throw new IllegalStateException( error );
         }
 
         byte[] b = co.getBytes();
 
 	    String titleXpathStr = "/ting:container/dkabm:record/dc:title[1]";
 	    log.trace( String.format( "finding dcTitle using xpath: '%s'", titleXpathStr ) );
 		String dcTitle = getDCVariable( b, titleXpathStr );
 	    log.trace( String.format( "cargo setting dcTitle with value '%s'", dcTitle ) );
         dc.setTitle( dcTitle );
 
         String creatorXpathStr = "/ting:container/dkabm:record/dc:creator[1]";
 	    log.trace( String.format( "finding dcCreator using xpath: '%s'", creatorXpathStr ) );
         String dcCreator = getDCVariable( b, creatorXpathStr );
 	    log.trace( String.format( "cargo setting dcCreator with value '%s'", dcCreator ) );
         dc.setCreator( dcCreator );
 
         String typeXpathStr = "/ting:container/dkabm:record/dc:type[@xsi:type]";
 	    log.trace( String.format( "finding dcType using xpath: '%s'", typeXpathStr ) );
         String dcType = getDCVariable( b, typeXpathStr );
 	    log.trace( String.format( "cargo setting dcType with value '%s'", dcType ) );
         dc.setType( dcType );
 
         String sourceXpathStr = "/ting:container/dkabm:record/dc:source[1]";
 	    log.trace( String.format( "finding dcSource using xpath: '%s'", sourceXpathStr ) );
         String dcSource = getDCVariable( b, sourceXpathStr );
 	    log.trace( String.format( "cargo setting dcSource with value '%s'", dcSource ) );
         dc.setSource( dcSource );
 
         String relationXpathStr = "/*/*/*/*[@tag='014']/*[@code='a']";
 	    log.trace( String.format( "finding dcRelation using xpath: '%s'", relationXpathStr ) );
         String dcRelation = getDCVariable( b, relationXpathStr );
 	    log.trace( String.format( "cargo setting dcRelation with value '%s'", dcRelation ) );
         dc.setRelation( dcRelation );
 
 	    log.debug( String.format( "setting variables in cargo container: dcTitle '%s'; dcCreator '%s'; dcType '%s'; dcSource '%s'", dcTitle, dcCreator, dcType, dcSource ) );
 
         return dc;
     }
 }
