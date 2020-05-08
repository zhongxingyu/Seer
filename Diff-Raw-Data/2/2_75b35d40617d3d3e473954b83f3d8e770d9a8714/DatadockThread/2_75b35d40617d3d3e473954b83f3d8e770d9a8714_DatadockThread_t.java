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
  * \brief Thread handling in datadock framework
  */
 
 package dk.dbc.opensearch.components.datadock;
 
 
 import dk.dbc.opensearch.common.db.IProcessqueue;
 import dk.dbc.opensearch.common.fedora.IObjectRepository;
 import dk.dbc.opensearch.common.pluginframework.IAnnotate;
 import dk.dbc.opensearch.common.pluginframework.ICreateCargoContainer;
 import dk.dbc.opensearch.common.pluginframework.IPluggable;
 import dk.dbc.opensearch.common.pluginframework.IRepositoryStore;
 import dk.dbc.opensearch.common.pluginframework.IRelation;
 import dk.dbc.opensearch.common.pluginframework.PluginException;
 import dk.dbc.opensearch.common.pluginframework.PluginResolver;
 import dk.dbc.opensearch.common.pluginframework.PluginResolverException;
 import dk.dbc.opensearch.common.types.CargoContainer;
 import dk.dbc.opensearch.components.harvest.IHarvest;
 import dk.dbc.opensearch.components.harvest.HarvesterInvalidStatusChangeException;
 import dk.dbc.opensearch.components.harvest.HarvesterUnknownIdentifierException;
 import dk.dbc.opensearch.components.harvest.HarvesterIOException;
 
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.sql.SQLException;
 import java.text.ParseException;
 import java.util.ArrayList;
 import java.util.concurrent.Callable;
 
 import javax.xml.parsers.ParserConfigurationException;
 import javax.xml.rpc.ServiceException;
 import javax.xml.transform.TransformerConfigurationException;
 import javax.xml.transform.TransformerException;
 import javax.xml.xpath.XPathExpressionException;
 
 import org.apache.commons.configuration.ConfigurationException;
 import org.apache.log4j.Logger;
 import org.xml.sax.SAXException;
 
 
 /**
  * \ingroup datadock \brief The public interface for the OpenSearch
  * DataDockService DataDock, together with DataDockPool, is the primary
  * accesspoint for the delivery of material to be saved in the Fedora repository
  * and processed by lucene. The DataDock interface allows clients to submit data
  * that represents a textual material to be stored in a Fedora repository and
  * indexed by Lucene. When submitted, the data is validated against a dictionary
  * of possible handlers using the supplied metadata and object-information. All
  * methods throw exceptions on errors.
  * 
  * \todo a schema for errors returned should be defined
  * 
  * DataDock is the central class in the datadock component. This class offers
  * the service of receiving data, metadata and objectinfo for later processing
  * with lucene. The primary responsibility of the DataDock is to validate
  * incoming data and construct data-carrying objects for use within OpenSearch.
  * Furthermore, DataDock starts the process of fedora storing, data processing,
  * indexing and search-capabilities
  */
 public class DatadockThread implements Callable<Boolean>
 {
     private Logger                  log = Logger.getLogger( DatadockThread.class );
 
     
     private IHarvest                harvester;
     private CargoContainer          cargo;
     private IProcessqueue           queue;
     private DatadockJob             datadockJob;
     private String                  submitter;
     private String                  format;
     private ArrayList<String>       list;
     private String                  result;
     private final IObjectRepository objectRepository;
     private PluginResolver pluginResolver;
     
 
     /**
      *\todo: Wheet out in the Exceptions
      * 
      * DataDock is initialized with a DatadockJob containing information about
      * the data to be 'docked' into to system
      * 
      * @param datadockJob
      *            the information about the data to be docked
      * @param processqueue
      *            the processqueue handler
      * @param jobMap
      *            information about the tasks that should be solved by the
      *            pluginframework
      * @throws ConfigurationException if no ObjectRepository could be reached
      * @throws ParserConfigurationException
      * @throws PluginResolverException
      * @throws NullPointerException
      * @throws SAXException
      */
     public DatadockThread( DatadockJob datadockJob, IProcessqueue processqueue, IObjectRepository objectRepository, IHarvest harvester, PluginResolver pluginResolver ) throws ConfigurationException, FileNotFoundException, IOException, NullPointerException, PluginResolverException, ParserConfigurationException, SAXException, ServiceException
     {
         log.trace( String.format( "Entering DatadockThread Constructor" ) );
         
         this.datadockJob = datadockJob;
         this.harvester = harvester;
         this.objectRepository = objectRepository;
         this.pluginResolver = pluginResolver;
         
         // Each pair identifies a plugin by p1:submitter and p2:format
         submitter = datadockJob.getSubmitter();
         format = datadockJob.getFormat();
         
         log.trace( String.format( "submitter: %s, format: %s", submitter, format ) );
         log.trace( String.format( "Calling jobMap.get( new Pair< String, String >( %s, %s ) )", submitter, format ) );
         
         list = DatadockJobsMap.getDatadockPluginsList( submitter, format );
         if (list == null)
         {
             throw new NullPointerException(String.format("The returned list from the DatadockJobsMap.getDatadockJobsMap( %s, %s ) is null", submitter, format ) );
         }
 
         log.trace( "constructor PluginList " + list.toString() );
         
         queue = processqueue;
         
         log.trace( String.format( "DatadockThread Construction finished" ) );
     }
     
 
     /**
      * call() is the thread entry method on the DataDock. Call operates on the
      * DataDock object, and all data critical for its success is given at
      * DataDock initialization. This method is used with
      * java.util.concurrent.FutureTask, which upon finalisation (completion,
      * exception or termination) will return an estimation of how long time it
      * will take to bzw. index and save in fedora the data given with the
      * CargoContainer. \see dk.dbc.opensearch.tools.Estimation
      * 
      * @returns true if job is performed
      * @throws PluginResolverException
      *             if the PluginResolver encountered problems, see
      *             dk.dbc.opensearch
      *             .common.pluginframework.PluginResolverException and
      *             dk.dbc.opensearch.common.pluginframework.PluginResolver
      * @throws FileNotFoundException
      *             if the PluginResolver.getPlugin cant find the file its
      *             searcing for
      * @throws IOException
      * @throws ParserConfigurationException
      *             if the PluginResolver or CargoContainer has troubles with the
      *             DocumentBuilder and DocumentBuilderFactory
      * @throws InstantationException
      *             if the PluginResolver cant instantiate a plugin
      * @throws IllegalAccessException
      *             if the PluginResolver cant access the desired plugin
      * @throws ClassNotFoundException
      *             if the PluginResolver cant find the desired plugin class
      * @throws SAXException
      *             when the a harvest or an annotate plugin have problems with
      *             the data
      * @throws MarshalException
      * @throws ValidationException
      * @throws IllegalStateException
      * @throws ServiceException
      * @throws IOException
      * @throws ParseException
      */
     public Boolean call() throws PluginResolverException, IOException, FileNotFoundException, ParserConfigurationException, InstantiationException, IllegalAccessException, ClassNotFoundException, SAXException, IllegalStateException, ServiceException, IOException, ParseException, XPathExpressionException, PluginException, SQLException, TransformerException, TransformerConfigurationException, ConfigurationException, HarvesterUnknownIdentifierException, HarvesterInvalidStatusChangeException, HarvesterIOException
     {
         // Must be implemented due to class implementing Callable< Boolean > interface.
         // Method is to be extended when we connect to 'Posthuset'
         log.trace( "DatadockThread call method called" );
 
         // Validate plugins
         log.debug( String.format( "pluginList classname %s", list.toString() ) );
         Boolean success = null;
         byte[] data = null;
         long timer = 0;
 
         try
         {
             for ( String classname : list )
             {
                 log.trace( "DatadockThread getPlugin 'classname' " + classname );
 
                 IPluggable plugin = pluginResolver.getPlugin( classname );               
                 
                 log.trace( String.format( "getPluginType = '%s'", plugin.getPluginType() ) );
 
                 switch ( plugin.getPluginType() )
                 {
                     case HARVEST:
                         //get data from harvester
                         try
                         {
                             data = harvester.getData( datadockJob.getIdentifier() );
                         }
                         catch( HarvesterUnknownIdentifierException huie )
                         {
                             log.error( String.format( "could not get data from harvester, exception message: %s, terminating thread", huie.getMessage() ) );
                             throw new HarvesterUnknownIdentifierException( huie.getMessage() );
                         }
 
                         log.trace( String.format( "case HARVEST pluginType %s", plugin.getPluginType().toString() ) );
 
                         ICreateCargoContainer harvestPlugin = (ICreateCargoContainer)plugin;
                         timer = System.currentTimeMillis();
 
                         cargo = harvestPlugin.getCargoContainer( datadockJob, data );
 
                         timer = System.currentTimeMillis() - timer;
                         log.trace( String.format( "Timing: ( HARVEST ) %s", timer ) );
 
                         if( cargo.getCargoObjectCount() < 1 )
                         {
                             String error = String.format( "Plugin returned a null CargoContainer" );
                             log.error( error );
                             throw new IllegalStateException( error );
                         }
 
                         break;
                     case ANNOTATE:
                         log.trace( String.format( "case ANNOTATE pluginType %s", plugin.getPluginType().toString() ) );
 
                         IAnnotate annotatePlugin = (IAnnotate)plugin;
 
                         if ( cargo == null)
                         {
                             String error = String.format( "Previous plugin returned a null CargoContainer" );
                             log.error( error );
                             throw new IllegalStateException( error );
                         }
 
                         timer = System.currentTimeMillis();
 
                         try
                         {
                             cargo = annotatePlugin.getCargoContainer( cargo );
                         }
                         catch ( PluginException pe )
                         {
                             log.debug( String.format( "Annotate plugin exception caught: %s", pe.getMessage() ) );
                             throw pe;
                         }
 
                         timer = System.currentTimeMillis() - timer;
                         log.trace( String.format( "Timing: ( ANNOTATE ) %s", timer ) );
 
                         break;
                     case RELATION:
                         log.trace( String.format( "case RELATION pluginType %s", plugin.getPluginType().toString() ) );
 
                         if( cargo == null)
                         {
                             String error = String.format( "Previous plugin returned a null CargoContainer" );
                             log.error( error );
                             throw new IllegalStateException( error );
                         }
 
                         IRelation relationPlugin = (IRelation)plugin;
                         relationPlugin.setObjectRepository( this.objectRepository );
                         timer = System.currentTimeMillis();
 
                         cargo = relationPlugin.getCargoContainer( cargo );
 
                         timer = System.currentTimeMillis() - timer;
                         log.trace( String.format( "Timing: ( RELATION, %s ) %s", classname, timer ) );
 
                         break;
                     case STORE:
                         log.trace( String.format( "case STORE pluginType %s", plugin.getPluginType().toString() ) );
 
                         IRepositoryStore repositoryStore = (IRepositoryStore)plugin;
                         repositoryStore.setObjectRepository( this.objectRepository );
                         timer = System.currentTimeMillis();
                         cargo = repositoryStore.storeCargoContainer( cargo );
                         timer = System.currentTimeMillis() - timer;
                         log.trace( String.format( "Timing: ( STORE ) %s", timer ) );
 
                         // DO NOT CHANGE log level to trace or debug!
                         log.info( "STORE pid: " + cargo.getIdentifier() );
 
                         break;
                     default:
                         log.warn( String.format( "plugin.getPluginType ('%s') did not match HARVEST or ANNOTATE", plugin.getPluginType() ) );
                 }
             }
 
             String identifierAsString = cargo.getIdentifierAsString();          
 
             //push to processqueue job to processqueue
             queue.push( identifierAsString );
 
             //inform the harvester that it was a success
             try
             {
                 harvester.setStatusSuccess( datadockJob.getIdentifier(), identifierAsString );
 
             }
             catch ( HarvesterInvalidStatusChangeException hisce )
             {
                 log.error( hisce.getMessage() , hisce);
                 return false;
             }
 
             return success;
         }
         catch ( Exception e )
         {
             String errorMsg = String.format( "Error in %s plugin handling. Message: %s", this.getClass().toString(), e.getMessage() );
            log.error( String.format( "setting status to FAILURE for identifier: %s with message: '%s'", datadockJob.getIdentifier(), errorMsg ), e);
             harvester.setStatusFailure( datadockJob.getIdentifier(), errorMsg );
             return success;
         }
     }
 }
