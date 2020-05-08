 package com.terradue.dsi;
 
 /*
  *  Copyright 2012 Terradue srl
  *
  *  Licensed under the Apache License, Version 2.0 (the "License");
  *  you may not use this file except in compliance with the License.
  *  You may obtain a copy of the License at
  *
  *       http://www.apache.org/licenses/LICENSE-2.0
  *
  *  Unless required by applicable law or agreed to in writing, software
  *  distributed under the License is distributed on an "AS IS" BASIS,
  *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  *  See the License for the specific language governing permissions and
  *  limitations under the License.
  */
 
 import static java.lang.System.exit;
 
 import java.util.Collection;
 
 import com.beust.jcommander.Parameter;
 import com.beust.jcommander.Parameters;
 import com.google.inject.Inject;
 import com.google.inject.name.Named;
 import com.sun.jersey.api.client.GenericType;
 import com.terradue.dsi.model.NetworkStorage;
 import com.terradue.dsi.model.NetworkStorageCreation;
 
 /**
  * @since 0.2
  */
 @Parameters( commandDescription = "Creates a Network Storage." )
 public final class CreateStorage
     extends BaseTool
 {
 
     public static void main( String[] args )
     {
         exit( new CreateStorage().execute( args ) );
     }
 
     @Parameter( names = { "--name" }, description = "The storage name" )
     private String name;
 
     @Parameter( names = { "--description" }, description = "The storage description" )
     private String description;
 
     @Parameter( names = { "--storage-provider" }, description = "The storage provider" )
     private String provider = "NetApp";
 
     @Parameter( names = { "--size" }, description = "The storage size, in GB (10Gb is the lesser accepted value)" )
     private int size = 10;
 
     @Parameter( names = { "--protocol" }, description = "The storage exported protocol" )
     private String exportProtocol = "nfs";
 
     @Parameter( names = { "--network" }, description = "The storage Network ID" )
     private String networkId;
 
     @Parameter( names = { "--provider" }, description = "The DSI provider ID" )
     private String providerId;
 
     @Parameter( names = { "--qualifier" }, description = "The DSI qualifier ID" )
     private String qualifierId;
 
     @Inject
     @Override
     public void setServiceUrl( @Named( "service.storages" ) String serviceUrl )
     {
         super.setServiceUrl( serviceUrl );
     }
 
     @Override
     protected void execute()
         throws Exception
     {
         logger.info( "Registering storage {} ...", name );
 
         NetworkStorageCreation networkStorage = new NetworkStorageCreation.Builder()
                                                 .setDescription( description )
                                                 .setExternalProtocol( exportProtocol )
                                                 .setName( name )
                                                 .setNetworkId( networkId )
                                                 .setProvider( provider )
                                                 .setProviderId( providerId )
                                                 .setQualifierId( qualifierId )
                                                 .setSize( size )
                                                 .build();
 
         restClient.resource( serviceUrl ).post( networkStorage );
 
         Collection<NetworkStorage> storages = restClient.resource( serviceUrl )
                                                         .get( new GenericType<Collection<NetworkStorage>>(){} );
 
         for ( NetworkStorage storage : storages )
         {
             if ( name.equals( storage.getName() ) )
             {
                logger.info( "Storage created with id: {}, it can be mount by running `sudo mount -t %s %s /mount`",
                              storage.getId(), storage.getExportProtocol(), storage.getExportUrl() );
                 return;
             }
         }
 
         logger.warn( "Storage is not available yet, back checking storages later" );
     }
 
 }
