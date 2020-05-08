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
 
 
 package dk.dbc.opensearch.plugins;
 
 import dk.dbc.opensearch.common.fedora.FedoraAdministration;
 import dk.dbc.opensearch.common.fedora.FedoraObjectRelations;
 import dk.dbc.opensearch.common.fedora.PIDManager;
 import dk.dbc.opensearch.common.pluginframework.IRelation;
 import dk.dbc.opensearch.common.pluginframework.PluginException;
 import dk.dbc.opensearch.common.pluginframework.PluginType;
 import dk.dbc.opensearch.common.types.CargoContainer;
 
 import java.io.IOException;
 import java.rmi.RemoteException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Vector;
 
 import javax.xml.rpc.ServiceException;
 
 import org.apache.commons.configuration.ConfigurationException;
 import org.apache.log4j.Logger;
 
 
 /**
  * Plugin for annotating docbook carcoContainers
  */
 public class MarcxchangeWorkRelation implements IRelation
 {
     private static Logger log = Logger.getLogger( MarcxchangeWorkRelation.class );
 
 
     private PluginType pluginType = PluginType.RELATION;
 
     private Vector<String> types;
     private FedoraAdministration fa;
 
 
     /**
      * Constructor for the MarcxchangeWorlkRelation plugin.
      */
     public MarcxchangeWorkRelation()
     {
         log.debug( "MarcxchangeWorkRelation constructor called" );
     
         types = new Vector<String>();
         types.add( "Anmeldelse" );
         types.add( "Artikel" );
         types.add( "Avis" );
         types.add( "Avisartikel" );
         types.add( "Tidsskrift" );
         types.add( "Tidsskriftsartikel" );
 
         fa = new FedoraAdministration();
     }
 
 
     /**
      * The "main" method of this plugin. Request a relation from
      * a webservice. If a relation is available it is added to the
      * cargocontainer in a new stream typed RelsExtData
      *
      * @param CargoContainer The CargoContainer to add relations to
      *
      * @returns A CargoContainer containing relations
      * 
      * @throws PluginException thrown if anything goes wrong during annotation.
      */
     public CargoContainer getCargoContainer( CargoContainer cargo ) throws PluginException, RemoteException, ConfigurationException, ServiceException, IOException//, ConfigurationException, MalformedURLException, ServiceException, IOException
     {
         log.trace( "getCargoContainer() called" );
 
         if ( cargo == null )
         {
             log.error( "MarcxchangeWorkRelation getCargoContainer cargo is null" );
             throw new PluginException( new NullPointerException( "MarcxchangeWorkRelation getCargoContainer throws NullPointerException" ) );
         }
 
         boolean ok = false;
         //ok = addWorkRelationForMaterial( cargo );
         ok = addWorkRelationForMaterial2( cargo );
 
         if ( ! ok )
         {
             log.error( String.format( "could not add work relation on pid %s", cargo.getDCIdentifier() ) );
         }
 
         return cargo;
     }
 
 
     private boolean addWorkRelationForMaterial2( CargoContainer cargo ) throws RemoteException, ConfigurationException, ServiceException, IOException
     {
         String dcTitle = cargo.getDCTitle();
         String dcType = cargo.getDCType();
         String dcCreator = cargo.getDCCreator();
         String dcSource = cargo.getDCSource();
 
         FedoraObjectRelations fedor = new FedoraObjectRelations();
 
         String relation = "isMemberOf";
         List< String > workRelations = new ArrayList<String>();
         if( ! types.contains( dcType ) )
         {
            log.debug( String.format( "finding work relations for dcType %s", dcType ) );
             List< String > tTitleWorkRelations = new ArrayList< String >();
             List< String > sTitleWorkRelations = new ArrayList< String >();
             List< String > sSourceWorkRelations = new ArrayList< String >();
             List< String > tSourceWorkRelations = new ArrayList< String >();
 
            log.debug( String.format( "WR with dcSource '%s' and dcTitle '%s'", dcSource, dcTitle) );
             if ( ! dcSource.equals( "" ) )
             {
                 sSourceWorkRelations = fedor.getSubjectRelations( "source", dcSource, relation );
                if ( sSourceWorkRelations.isEmpty() && ! dcTitle.equals( "" ) ) 
                 {
                     sTitleWorkRelations = fedor.getSubjectRelations( "source", dcTitle, relation );
                 }
             }
 
             if ( ! dcTitle.equals( "" ) )
             {
                if ( ! dcSource.equals( "" ) )
                {
                    tSourceWorkRelations = fedor.getSubjectRelations( "title", dcSource, relation );
                }
                else
                //if ( tSourceWorkRelations.isEmpty() )
                 {
                     tTitleWorkRelations = fedor.getSubjectRelations( "title", dcTitle, relation );
                 }
             }
             
             if( ! sSourceWorkRelations.isEmpty() )
             {
                 workRelations.addAll( sSourceWorkRelations );
             }
             else if( ! sTitleWorkRelations.isEmpty() )
             {
                 workRelations.addAll( sTitleWorkRelations );
             }
             else if( ! tSourceWorkRelations.isEmpty() )
             {
                 workRelations.addAll( tSourceWorkRelations );
             }
             else if( ! tTitleWorkRelations.isEmpty() )
             {
                 workRelations.addAll( tTitleWorkRelations );
             }
             else
             {
                 log.debug( String.format( "No matching posts found for '%s' or '%s'", dcTitle, dcSource ) );
             }
         }
         else
         {
 
            List< String > titlePids = new ArrayList<String>();
            if ( ! ( dcTitle.equals( "" ) || dcCreator.equals( "" ) ) )
            {
                fedor.getSubjectRelations( "title", dcTitle, "creator", dcCreator, relation ); //fa.findMatchingFieldPids( "title", dcTitle );
            }
             /*List<String> creatorPids = fedor.getSubjectRelations( "creator", dcCreator, relation ); //fa.findMatchingFieldPids( "creator", dcCreator );
             titlePids.retainAll( creatorPids );*/
 
             if( ! titlePids.isEmpty() )
             {
                 workRelations.addAll( titlePids );
             }
             else
             {
                 log.debug( String.format( "No matching posts found for '%s' or '%s'", dcTitle, dcCreator ) );
             }
         }
         log.debug( String.format( "workRelations = %s", workRelations.toString() ) ); //Arrays.deepToString( resultPids.toArray() )
 
         //find work-pid for all pids in pids
         HashMap<String, String> workPids = new HashMap<String, String>();
 
         for( String wr : workRelations )
         {
             //List<String> workRelationPid = fedor.getSubjectRelationships2( pid, "isMemberOf" );
             log.debug( String.format( "work %s, workRelationPid = %s", wr, wr.toString() ) );
 
             /*if( workRelationPid.isEmpty() )
             {
                 log.warn( String.format( "no workrelation was found for pid %s", pid ) );
             }
             else if( workRelationPid.size() > 1 )
             {
                 //there should be only one work-pid per pid, ie. the work-relation
                 // should be identical for all found pids, as they match on the
                 // workrelation criteria
                 log.warn( String.format( "the pid %s has more than one work it relates to (works: %s)", pid, workRelationPid.toString() ) );
                 /** \todo: should more be done about this?*/
             //}
 
             /*if( workRelationPid.size() >= 1 )
             {
                 // the client has been duly warned, now we'll just select the first
                 // (and hopefully only) of the returned workrelation pids
                 workPids.put( pid, workRelationPid.get( 0 ) );
             }*/
         }
 
         // lets check that there was only one workpid for all the returned
         // materials:
         String theone = null;
         /*ArrayList<String> howmany = new ArrayList<String>( workPids.values() );
         log.debug( String.format( "workpids list = %s", howmany.toString() ) );
         if( howmany.size() > 1 )
         {
             log.warn( String.format( "The CargoContainer %s matched more than one workrelation ( = %s)", cargo.getDCTitle(), howmany.toString() ) );
         }
         else if( howmany.isEmpty() )*/
         if ( workRelations.isEmpty() )
         {
             // this is a new workrelation, lets get a pid
             theone = PIDManager.getInstance().getNextPID( "work" );
         }
         else
         {
             theone = workRelations.get( 0 );
         }
 
         /*if( howmany.size() >= 1)
         {
             //again, the client has been duly warned, we simply take the first, and
             //hopefully only value:
             theone = howmany.get( 0 );
             log.debug( String.format( "Adding workrelation %s to pid %s", theone, cargo.getDCIdentifier() ) );
         }*/
 
         log.debug( String.format( "Trying to add %s to the collection %s", cargo.getDCIdentifier(), theone ) );
         // and add this workrelation pid as the workrelationpid of the
         // cargocontainer material:*/
         return fedor.addPidToCollection( cargo.getDCIdentifier(), theone );
     }
 
 
     private boolean addWorkRelationForMaterial( CargoContainer cargo ) throws RemoteException, ConfigurationException, ServiceException, IOException
     {
         String dcTitle = cargo.getDCTitle();
         String dcType = cargo.getDCType();
         String dcCreator = cargo.getDCCreator();
         String dcSource = cargo.getDCSource();
 
         FedoraObjectRelations fedor = new FedoraObjectRelations();
 
         List<String> pids = new ArrayList<String>();
         if( !types.contains( dcType ) )
         {
             List<String> titlePids = fa.findMatchingFieldPids( "title", dcTitle );
             List<String> sTitlePids = fa.findMatchingFieldPids( "source", dcTitle );
             List<String> sourcePids = fa.findMatchingFieldPids( "source", dcSource );
             List<String> tSourcePids = fa.findMatchingFieldPids( "title", dcSource );
 
             if( !titlePids.isEmpty() )
             {
                 pids.addAll( titlePids );
             }
             else if( !sTitlePids.isEmpty() )
             {
                 pids.addAll( sTitlePids );
             }
             else if( !sourcePids.isEmpty() )
             {
                 pids.addAll( sourcePids );
             }
             else if( !tSourcePids.isEmpty() )
             {
                 pids.addAll( tSourcePids );
             }
             else
             {
                 log.debug( String.format( "No matching posts found for '%s' or '%s'", dcTitle, dcSource ) );
             }
         }
         else
         {
             List<String> titlePids = fa.findMatchingFieldPids( "title", dcTitle );
             List<String> creatorPids = fa.findMatchingFieldPids( "creator", dcCreator );
             titlePids.retainAll( creatorPids );
 
             if( !titlePids.isEmpty() )
             {
                 pids.addAll( titlePids );
             }
             else
             {
                 log.debug( String.format( "No matching posts found for '%s' or '%s'", dcTitle, dcCreator ) );
             }
         }
         log.debug( String.format( "pids = %s", pids.toString() ) );
 
         //find work-pid for all pids in pids
         HashMap<String, String> workPids = new HashMap<String, String>();
 
         for( String pid : pids )
         {
             List<String> workRelationPid = fedor.getSubjectRelationships( pid, "isMemberOf" );
             log.debug( String.format( "pid %s, workRelationPid = %s", pid, workRelationPid.toString() ) );
             if( workRelationPid.isEmpty() )
             {
                 log.warn( String.format( "no workrelation was found for pid %s", pid ) );
             }
             else if( workRelationPid.size() > 1 )
             {
                 //there should be only one work-pid per pid, ie. the work-relation 
                 // should be identical for all found pids, as they match on the
                 // workrelation criteria
                 log.warn( String.format( "the pid %s has more than one work it relates to (works: %s)", pid, workRelationPid.toString() ) );
                 /** \todo: should more be done about this?*/
             }
 
             if( workRelationPid.size() >= 1 )
             {
                 // the client has been duly warned, now we'll just select the first
                 // (and hopefully only) of the returned workrelation pids
                 workPids.put( pid, workRelationPid.get( 0 ) );
             }
         }
 
         // lets check that there was only one workpid for all the returned
         // materials:
         String theone = null;
         ArrayList<String> howmany = new ArrayList<String>( workPids.values() );
         log.debug( String.format( "workpids list = %s", howmany.toString() ) );
         if( howmany.size() > 1 )
         {
             log.warn( String.format( "The CargoContainer %s matched more than one workrelation ( = %s)", cargo.getDCTitle(), howmany.toString() ) );
         }
         else if( howmany.isEmpty() )
         {
             // this is a new workrelation, lets get a pid
             theone = PIDManager.getInstance().getNextPID( "work" );
         }
 
         if( howmany.size() >= 1)
         {
             //again, the client has been duly warned, we simply take the first, and
             //hopefully only value:
             theone = howmany.get( 0 );
             log.debug( String.format( "Adding workrelation %s to pid %s", theone, cargo.getDCIdentifier() ) );
         }
 
         log.debug( String.format( "Trying to add %s to the collection %s", cargo.getDCIdentifier(), theone ) );
         // and add this workrelation pid as the workrelationpid of the
         // cargocontainer material:
         return fedor.addPidToCollection( cargo.getDCIdentifier(), theone );
     }
     
 
     public PluginType getPluginType()
     {
         return pluginType;
     }
 }
