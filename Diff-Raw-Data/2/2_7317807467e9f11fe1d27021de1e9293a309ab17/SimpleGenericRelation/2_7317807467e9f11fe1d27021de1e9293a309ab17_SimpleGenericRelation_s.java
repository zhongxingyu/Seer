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
 
 import dk.dbc.opensearch.common.fedora.IObjectRepository;
 import dk.dbc.opensearch.common.pluginframework.IPluggable;
 import dk.dbc.opensearch.common.pluginframework.IPluginEnvironment;
 import dk.dbc.opensearch.common.pluginframework.PluginException;
 import dk.dbc.opensearch.common.pluginframework.PluginType;
 import dk.dbc.opensearch.common.types.CargoContainer;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import org.apache.log4j.Logger;
 
 
 /**
  * Plugin for creating relations between reviews and their target
  */
 public class SimpleGenericRelation implements IPluggable
 {
     private static Logger log = Logger.getLogger( SimpleGenericRelation.class );
 
     private PluginType pluginType = PluginType.RELATION;
 
     /**
      * Constructor for the SimpleGenericRelation plugin.
      */
     public SimpleGenericRelation( IObjectRepository repository ) throws PluginException
     {
     }
 
 
     /**
      * The "main" method of this plugin.
      *
      * @param cargo The CargoContainer to add the reviewOf relation to
      * and be the target of a hasSimpleGeneric relation on another object in the objectRepository
      *
      * @return A CargoContainer containing relations
      *
      * @throws PluginException thrown if anything goes wrong during annotation.
      */
     @Override
    synchronized public CargoContainer runPlugin( IPluginEnvironment ienv, CargoContainer cargo ) throws PluginException
     {
 	if ( !( ienv instanceof SimpleGenericRelationEnvironment) )
 	{
 	    String errMsg = String.format( "The given PluginEnvironment is of incorrect type. Expected: %s, got: %s", "SimpleGenericRelationEnvironment", ienv.getClass().getName() );
 	    log.error( errMsg );
 	    throw new PluginException( errMsg );
 	}
 	SimpleGenericRelationEnvironment env = (SimpleGenericRelationEnvironment)ienv;
 
         log.trace( "runPlugin() called" );
   
 	env.addRelation( cargo );
 
         return cargo;
     }
 
     @Override
     public PluginType getPluginType()
     {
         return pluginType;
     }
 
     @Override
     public IPluginEnvironment createEnvironment( IObjectRepository repository, Map< String, String > args ) throws PluginException
     {
     	return new SimpleGenericRelationEnvironment( repository, args );
     }
 
 }
