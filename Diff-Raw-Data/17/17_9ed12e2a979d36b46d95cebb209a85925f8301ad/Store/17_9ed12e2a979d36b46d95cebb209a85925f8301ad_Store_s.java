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
 
 
 import dk.dbc.opensearch.common.fedora.IObjectRepository;
 import dk.dbc.opensearch.common.pluginframework.IPluginEnvironment;
 import dk.dbc.opensearch.common.pluginframework.IPluggable;
 import dk.dbc.opensearch.common.pluginframework.PluginException;
 import dk.dbc.opensearch.common.pluginframework.PluginType;
 import dk.dbc.opensearch.common.types.CargoContainer;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import org.apache.log4j.Logger;
 
 
 /**
  * Class for storing CargoContainers
  */
 public class Store implements IPluggable
 {
     private static Logger log = Logger.getLogger( Store.class );
 
     private PluginType pluginType = PluginType.STORE;
 
     StoreEnvironment env = null;
 
     public Store( IObjectRepository repository ) throws PluginException
     {
     }    
 
     /*
      * This funtion is synchronized in the case the rare event happens that two records with identical ID's,
      * eg. two updates of the record, are stored by two different threads simultaniously.
      * See bug#10534.
      */
     @Override
    synchronized public CargoContainer runPlugin( IPluginEnvironment ienv, CargoContainer cargo ) throws PluginException
     {
 	if ( !( ienv instanceof StoreEnvironment) )
 	{
 	    String errMsg = String.format( "The given PluginEnvironment is of incorrect type. Expected: %s, got: %s", "StoreEnvironment", ienv.getClass().getName() );
 	    log.error( errMsg );
 	    throw new PluginException( errMsg );
 	}
 
 	StoreEnvironment env = (StoreEnvironment)ienv;
 
     	log.trace( "Entering storeCargoContainer( CargoContainer )" );
 
	return env.run( cargo );
     }
 
     @Override
     public PluginType getPluginType()
     {
         return pluginType;
     }
 
     @Override
     public IPluginEnvironment createEnvironment( IObjectRepository repository, Map< String, String > args ) throws PluginException
     {
     	return new StoreEnvironment( repository, args );
     }
 
 }
