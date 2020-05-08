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
 
 
 package dk.dbc.opensearch.plugins;
 
 import dk.dbc.commons.javascript.E4XXMLHeaderStripper;
 import dk.dbc.commons.types.Pair;
 import dk.dbc.jslib.Environment;
 import dk.dbc.opensearch.fedora.FcrepoModifier;
 import dk.dbc.opensearch.fedora.FcrepoReader;
 import dk.dbc.opensearch.fedora.FcrepoUtils;
 import dk.dbc.opensearch.fedora.ObjectRepositoryException;
 import dk.dbc.opensearch.pluginframework.IPluginEnvironment;
 import dk.dbc.opensearch.pluginframework.PluginEnvironmentUtils;
 import dk.dbc.opensearch.pluginframework.PluginException;
 import dk.dbc.opensearch.types.CargoContainer;
 import dk.dbc.opensearch.types.CargoObject;
 import dk.dbc.opensearch.types.DataStreamType;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 public class StoreEnvironment implements IPluginEnvironment
 {
 
     private static Logger log = LoggerFactory.getLogger( StoreEnvironment.class );
 
     private final FcrepoReader reader;
     private final FcrepoModifier modifier;
     private Environment jsEnvironment;
 
     // For validation:
     private static final String javascriptStr = "javascript";
     private static final String entryFuncStr  = "entryfunction";
 
     private final String entryPointFunc;
     private final String javascript;
 
     public StoreEnvironment( FcrepoReader reader, FcrepoModifier modifier,
             Map<String, String> args, String scriptPath ) throws PluginException
     {
         this.reader = reader;
         this.modifier = modifier;
 
         List<Pair<String, Object>> objectList = new ArrayList<Pair<String, Object>>();
         objectList.add( new Pair<String, Object>( "Log", log ) );
 
         this.entryPointFunc = args.get( StoreEnvironment.entryFuncStr );
         this.javascript = args.get( StoreEnvironment.javascriptStr );
         if( javascript != null && javascript.length() > 0 )
         {
             this.validateArguments( args, objectList, scriptPath );
             this.jsEnvironment = PluginEnvironmentUtils.initializeJavaScriptEnvironment( javascript, objectList, scriptPath );
         }
         else
         {
             // Use old behaviour
             jsEnvironment = null;
         }
 
         log.trace( "Checking JavaScript environment (outer)" );
         if( jsEnvironment == null )
         {
             log.trace( "JavaScript environment is null" );
         }
         else
         {
             log.trace( "JavaScript environment is initialized" );
         }
     }
 
 
     public CargoContainer run( CargoContainer cargo ) throws PluginException
     {
         CargoObject co = cargo.getCargoObject( DataStreamType.OriginalData );
         String submitter = co.getSubmitter();
         String format = co.getFormat();
         String language = co.getLang();
         String XML = new String( E4XXMLHeaderStripper.strip( co.getBytes() ) ); // stripping: <?xml...?>
         String pidStr = cargo.getIdentifierAsString();// get the pid of the cargocontainer
         boolean hasObject = false;
         // Let JavaScript decide if record should be deleted or stored
         boolean deleteRecord = false;
         if (jsEnvironment != null)
         {
             log.debug( "Calling JavaScript to determine if record should be deleted");
             deleteRecord = ( (Boolean) jsEnvironment.callMethod( entryPointFunc, new Object[] { submitter, format, language, XML, pidStr } ) ).booleanValue();
             log.debug( String.format( "js to determine if it is a deleterecord returned: '%s'", deleteRecord ) );
         }
         else
         {
             log.debug(String.format("JavaScript not defined for [%s:%s], skipping", format, submitter));
         }
        
         //determining whether we have the object or not
         hasObject = reader.hasObject( pidStr );
         log.debug( String.format( "hasObject( %s ) returned %b",pidStr, hasObject ) );
 
         //
         // We check whether the given record is marked deleted or not and whether the object is currently in the repository.
         // We use deletedRecord in the outer if-statement because that is our "control"-boolean, that is, the most import thing
         // to check.
         //
         if( deleteRecord )
         {
             if( !hasObject )
             {
                 // Delete-record for an object that is not in the repository
                 // Note: We cannot delete an unexisting record!
                 String warning = String.format( "Cannot delete nonexisting object from repository: [%s]", pidStr );
                 log.warn( warning );
                 throw new PluginException( warning );
             }
             else
             { // hasObject
                 log.info( String.format( "Object will be deleted: pid [%s]", pidStr ) );
                 try
                 {
                     String logm = String.format( "Datadock: %s deleted with pid %s", format, pidStr );
                     FcrepoUtils.removeInboundRelations( reader, modifier, pidStr );
                     modifier.deleteObject( pidStr, logm );
                     FcrepoUtils.removeOutboundRelations( reader, modifier, pidStr );
                     cargo.setIsDeleteRecord( deleteRecord );
                 }
                 catch( ObjectRepositoryException e )
                 {
                     String error = String.format( "Failed to mark CargoContainer as deleted and/or remove relations. Pid: [%s], submitter: [%s] and format: [%s]", pidStr, submitter, format );
                     log.error( error, e );
                     throw new PluginException( error, e );
                 }
             }
         }
         else
         { // !deleteRecord
             if( hasObject )
             {
                 log.info( String.format( "Purging object: [%s]", pidStr ) );
                 try
                 {
                     FcrepoUtils.removeInboundRelations( reader, modifier, pidStr );
                     modifier.purgeObject( pidStr, "purge before store hack" );
                 }
                 catch( ObjectRepositoryException e )
                 {
                     String error = String.format( "exception caught when trying to remove inbound relations and/or purge object: [%s]", pidStr );
                     log.error( error, e );
                     throw new PluginException( error, e );
                 }
             }
 
             // Ingesting object
             try
             {
                 String logm = String.format( "Datadock: [%s] inserted with pid [%s]", format, pidStr );
                 modifier.storeObject( cargo, logm, "auto" );
             }
             catch( ObjectRepositoryException e )
             {
                 String error = String.format( "exception caught when trying to store object: [%s]", pidStr );
                 log.error( error, e );
                 throw new PluginException( error, e );
             }
         }
 
         return cargo;
     }
 
 
     /**
      * This function will validate the following argumentnames: "javascript" and "entryfunction".
      * All other argumentnames will be silently ignored.
      * Currently the "entryfunction" is not tested for validity.
      *
      * @param Map< String, String > the argumentmap containing argumentnames as keys and arguments as values
      * @param List< Pair< String, Object > > A list of objects used to initialize the JavaScript environment.
      *
      * @throws PluginException if an argumentname is not found in the argumentmap or if one of the arguments cannot be used to instantiate the pluginenvironment.
      */
     private void validateArguments( Map< String, String > args, List< Pair< String, Object > > objectList,
 				    String scriptPath ) throws PluginException
     {
         log.info( "Validating Arguments - Begin" );
 
         // Validating existence of mandatory entrys:
         if( !PluginEnvironmentUtils.validateMandatoryArgumentName( StoreEnvironment.javascriptStr, args ) )
         {
             throw new PluginException( String.format( "Could not find argument: %s", StoreEnvironment.javascriptStr ) );
         }
         if( !PluginEnvironmentUtils.validateMandatoryArgumentName( StoreEnvironment.entryFuncStr, args ) )
         {
             throw new PluginException( String.format( "Could not find argument: %s", StoreEnvironment.entryFuncStr ) );
         }
 
         Environment tmpJsEnv = PluginEnvironmentUtils.initializeJavaScriptEnvironment( args.get( StoreEnvironment.javascriptStr ), objectList, scriptPath );
 
         // Validating JavaScript function entries.
         if( !PluginEnvironmentUtils.validateJavaScriptFunction( tmpJsEnv, args.get( StoreEnvironment.entryFuncStr ) ) )
         {
             throw new PluginException( String.format( "Could not use %s as function in javascript", args.get( StoreEnvironment.entryFuncStr ) ) );
         }
 
         log.info( "Validating Arguments - End" );
     }
     
 }
