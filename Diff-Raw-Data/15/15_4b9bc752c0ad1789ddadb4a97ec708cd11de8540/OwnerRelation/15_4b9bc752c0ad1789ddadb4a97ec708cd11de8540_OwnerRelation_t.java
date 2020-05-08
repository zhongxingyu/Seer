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
  * \brief Adding owner relation information to fedora repository objects
  */
 package dk.dbc.opensearch.plugins;
 
 import dk.dbc.opensearch.common.config.FileSystemConfig;
 import dk.dbc.opensearch.common.fedora.IObjectRepository;
 import dk.dbc.opensearch.common.fedora.PID;
 import dk.dbc.opensearch.common.fedora.FedoraRelsExt;
 import dk.dbc.opensearch.common.fedora.FedoraNamespaceContext.FedoraNamespace;
 import dk.dbc.opensearch.common.metadata.DBCBIB;
 import dk.dbc.opensearch.common.pluginframework.IRelation;
 import dk.dbc.opensearch.common.pluginframework.PluginException;
 import dk.dbc.opensearch.common.pluginframework.PluginType;
 import dk.dbc.opensearch.common.types.CargoContainer;
 import dk.dbc.opensearch.common.types.CargoObject;
 import dk.dbc.opensearch.common.types.DataStreamType;
 
 import java.io.ByteArrayOutputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.Map;
 import javax.script.Invocable;
 import javax.script.ScriptEngine;
 import javax.script.ScriptEngineManager;
 import javax.script.ScriptException;
 import org.apache.commons.configuration.ConfigurationException;
 import org.apache.log4j.Logger;
 
 
 /**
  * Plugin for adding owner relation to cargoContainers
  */
 public class OwnerRelation implements IRelation
 {
 
     private static Logger log = Logger.getLogger( OwnerRelation.class );
     private PluginType pluginType = PluginType.RELATION;
     private final Map<String, Invocable> scriptCache = Collections.synchronizedMap( new HashMap<String, Invocable>() );
     private final ScriptEngineManager manager = new ScriptEngineManager();
 
     /**
      * Constructor for the OwnerRelation plugin.
      * @throws PluginException 
      */
     public OwnerRelation() throws PluginException
     {
         log.trace( "OwnerRelation plugin constructed" );
     }
 
 
     /**
      * Tries to do a lookup of a cached instance of the script engine
      * based on the submitter. If no cached instances are found, a new
      * one is created from a supplied javascript matching the role of
      * this plugin.
      *
      * @param cargo
      * @return Configured script engine fitting the submitter value of the CargoContainer
      * @throws ConfigurationException
      * @throws FileNotFoundException
      * @throws ScriptException
      * @throws PluginException
      */
     Invocable lookupJavaScript( String submitter ) throws ConfigurationException, FileNotFoundException, ScriptException, PluginException
     {
         
         log.trace( String.format( "Entering lookupJavaScript" ) );
         if( submitter == null || submitter.isEmpty() )
         {
             throw new PluginException( new IllegalArgumentException( "submitter in CargoContainer is not set. Aborting." ) );
         }
 
         //lookup invocable in cache:
         if( this.scriptCache.containsKey( submitter ) )
         {
             log.trace( String.format( "Returning Invocable js for %s", submitter ) );
             return this.scriptCache.get( submitter );
         }
         else // ...or create new invocable + add it to the cache
         {
             ScriptEngine engine = manager.getEngineByName( "JavaScript" );
             /**
              * \Todo: Bug 9756: why is the spelleing of the relation as it is?
              */
             
             engine.put( "log", log );
            engine.put( "IS_MEMBER_OF_COLLECTION", DBCBIB.IS_MEMBER_OF_COLLECTION );
 
             String path = FileSystemConfig.getScriptPath();
             String jsFileName = path + "owner_relation.js";
 
             log.debug( String.format( "Using javascript at url '%s'", jsFileName ) );
             engine.eval( new java.io.FileReader( jsFileName ) );
 
             Invocable inv = (Invocable) engine;
 
             this.scriptCache.put( submitter, inv );
             log.trace( String.format( "Returning Invokable js for %s", submitter ) );
             return inv;//this.scriptCache.get( submitter );
         }
     }
 
 
     /**
      * Entry point of the plugin
      * @param CargoContainer The {@link CargoContainer} to construct the owner relations from
      * @returns a {@link CargoContainer} containing a RELS-EXT stream reflecting the owner relations
      * @throws PluginException, which wraps all exceptions thrown from
      * within this plugin, please refer to {@link PluginException} for
      * more information on how to retrieve information on the
      * originating exception.
      */
     @Override
     public CargoContainer getCargoContainer( CargoContainer cargo ) throws PluginException
     {
         log.trace( "getCargoContainer() called" );
 
         try
         {
             setOwnerRelations( cargo );
 
             //this.fedoraHandle.addRelationship( pid, "info:fedora/fedora-system:def/relations-external#isMemberOfCollection", owner, false, null );            
         }
         catch( ConfigurationException e )
         {
             log.error( "setOwnerRelation: Exception e", e );
             throw new PluginException( "Error setting OwnerRelation ", e );
         }
         catch( IOException e )
         {
             log.error( "setOwnerRelation: Exception e", e );
             throw new PluginException( "Error setting OwnerRelation ", e );
 
         }
         catch( Exception e )
         {
             log.error( "setOwnerRelation: Exception e", e );
             throw new PluginException( "Error setting OwnerRelation ", e );
         }
 
         return cargo;
     }
 
 
     public void setOwnerRelations( CargoContainer cargo ) throws ConfigurationException, FileNotFoundException, Exception
     {
         CargoObject co = null;
         if( ! cargo.hasCargo( DataStreamType.OriginalData ) )
         {
             String error = String.format( "CargoContainer with id '%s' has no OriginalData to contruct relations from, aborting", cargo.getIdentifier() );
             log.error( error );
             throw new PluginException( new IllegalStateException( error ) );
         }else
         {
             co = cargo.getCargoObject( DataStreamType.OriginalData );
         }
         
         String submitter = co.getSubmitter();
         String format = co.getFormat();
         
         if( null == submitter   ||
             submitter.isEmpty() ||
             null == format      ||
             format.isEmpty() )
         {
             String error = String.format( "CargoContainer with id '%s' has no information on submitter or format", cargo.getIdentifier() );
             log.error( error );
             throw new PluginException( new IllegalStateException( error ) );
         }
 
         Invocable inv = lookupJavaScript( submitter );
 
         FedoraRelsExt rels = (FedoraRelsExt) cargo.getMetaData( DataStreamType.RelsExtData );
         if( null == cargo.getIdentifier() || cargo.getIdentifier().isEmpty() )
         {
             log.warn( String.format( "CargoContainer has no identifier, this will be a problem in the RELS-EXT generation/validation" ) );
         }
 
         if( null == rels )
         {
             rels = new FedoraRelsExt( cargo.getIdentifier() );
         }
         
         log.debug( String.format( "Got RelsExt with id '%s'", rels.getIdentifier() ) );
 
         if( ! rels.getIdentifier().equals( cargo.getIdentifier() ) )//then something has gone terribly wrong
         {
             String error = String.format( "CargoContainer data has different identifier ('%s') than its Rels-ext metadata identifier (%s) ", cargo.getIdentifier(), rels.getIdentifier() );
             log.error( error );
             throw new PluginException( error );
         }
         
         rels = ( FedoraRelsExt ) inv.invokeFunction( "addOwnerRelation", 
                                                      rels, 
                                                      submitter, 
                                                      format );
 
         cargo.addMetaData( rels );
     }
 
 
     @Override
     public PluginType getPluginType()
     {
         return pluginType;
     }
 
 
     public void setObjectRepository( IObjectRepository objectRepository )
     {
     }
 }
