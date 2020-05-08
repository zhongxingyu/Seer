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
 
 import java.util.LinkedList;
 import java.util.List;
 
 import com.beust.jcommander.Parameter;
 import com.beust.jcommander.Parameters;
 import com.google.inject.Inject;
 import com.google.inject.name.Named;
 import com.sun.jersey.api.client.UniformInterfaceException;
 
 @Parameters( commandDescription = "Terminate the selected running instance" )
 public final class StopInstances
     extends BaseTool
 {
 
     public static void main( String[] args )
     {
         exit( new StopInstances().execute( args ) );
     }
 
     @Parameter( description = "The image identificator(s) as returned by the upload command" )
     protected List<String> ids = new LinkedList<String>();
 
     @Inject
     @Override
     public void setServiceUrl( @Named( "service.deployments" ) String serviceUrl )
     {
         super.setServiceUrl( serviceUrl );
     }
 
     @Override
     public void execute()
         throws Exception
     {
         for ( String id : ids )
         {
             stopInstance( id );
         }
     }
 
     boolean stopInstance( String id )
     {
         logger.info( "Stopping instance {} ...", id );
         try
         {
             restClient.resource( new StringBuilder( serviceUrl )
                                 .append( '/' )
                                 .append( id )
                                 .append( "/start" )
                                 .toString() )
                       .post();
             logger.info( "Instance {} successfully stopped", id );
             return true;
         }
         catch ( UniformInterfaceException e )
         {
            logger.warn( "An error occurred while stopping instance {}, server replied {}: {}",
                         new Object[] {
                             id,
                             e.getResponse().getClientResponseStatus(),
                             e.getResponse().getEntity( String.class )
                         } );
             return false;
         }
     }
 
 }
