 /**
  * Copyright (c) 2011, X-Juvi
  * All rights reserved.
  *
  * Redistribution and use in source and binary forms, with or without
  * modification, are permitted provided that the following conditions are met:
  *     * Redistributions of source code must retain the above copyright
  *       notice, this list of conditions and the following disclaimer.
  *     * Redistributions in binary form must reproduce the above copyright
  *       notice, this list of conditions and the following disclaimer in the
  *       documentation and/or other materials provided with the distribution.
  *     * Neither the name of X-Juvi
  * 	  nor the names of its contributors may be used to endorse or promote products
  *       derived from this software without specific prior written permission.
  *
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
  * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
  * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
  * DISCLAIMED. IN NO EVENT SHALL X-Juvi
  * BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
  * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
  * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
  * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
  * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
  * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  *
  * @author         Miguel Rojas (miguel.rojas@uni-dortmund.de), Florian Feldhaus (florian.feldhaus@uni-dortmund.de)
  * @version        1.0
  * @lastrevision   15.11.2011
  */
 package cloud.one.occi.internal;
 
 import java.io.File;
 import java.util.Hashtable;
 
 import org.springframework.stereotype.Component;
 
 import cloud.services.one.occi.ICompute;
 import cloud.services.one.occi.INetwork;
 import cloud.services.one.occi.IOCCIOneManager;
 import cloud.services.one.occi.IStorage;
 import cloud.services.one.occi.ITemplate;
 
 /**
  * This class represents the main Spring Java Bean of the OCCI-Client.  
  * This JavaBean provides utilities for creating, deleting and handling
  * of cloud resources (Network, Storage and VMs).  
  * 
  * OcciOneManagerImpl is in charge of making http-calls to services 
  * exposed by a OCCI-server. The results of those calls will be
  * normally handled by a GUI interface. 
  * 
  * Each operation (create, delete) associated to network, storage and vm 
  * is supported by a helper class which contains the attributes of the
  * respective resource (INetwork, IStorage, ICompute respectively).  
  * 
  * Note that this JavaBean and helper classes are fully independent of 
  * any WebController.
  * 
  * @author Miguel Rojas (email.miguel.rojas@googlemail.com)
  *
  */
 @Component( "occi-one" )
 public class OcciOneManagerImpl implements IOCCIOneManager 
 {
     // --------   NETWORK    --------
     @Override
     public String createNetwork( INetwork data )
     {
         try
         {
             Hashtable<String, String> headers = new Hashtable<String, String>();
             Hashtable<String, String> body    = new Hashtable<String, String>();
             
             headers.put( "Accept", "*/*" );
             headers.put( "Category", "network;scheme=\"http://schemas.ogf.org/occi/infrastructure#\";class=\"kind\";," +
                                      "ipnetwork;scheme=\"http://schemas.ogf.org/occi/infrastructure/network#\";class=\"kind\";" );
             
             String attr = "occi.core.title=\"%s\","         +
                           "occi.core.summary=\"%s\","       +
                           "occi.network.address=\"%s\","    +
                           "occi.network.allocation=\"%s\"," +
                           "occi.network.vlan=%s";
             
             headers.put( "X-OCCI-Attribute", String.format( attr,
                                                             data.getContent().get( INetwork.TITLE      ),
                                                             data.getContent().get( INetwork.SUMMARY    ),
                                                             data.getContent().get( INetwork.ADDRESS    ),
                                                             data.getContent().get( INetwork.ALLOCATION ),
                                                             data.getContent().get( INetwork.VLAN       )
                                                             ) );
             
             String URI = data.getContent().get( INetwork.URI ) + "/network/";
             OcciResponse response = HttpUtils.post( URI, 
                                                     headers, 
                                                     body );
             String result = null;
             if ( response.content != null ) result = response.content[ 0 ];
             return result;
         }
         catch ( Exception e )
         {
         }
         
         return null;
     }
 
     @Override
     public String deleteNetwork( INetwork data )
     {
         try
         {
             String ID = data.getContent().get( INetwork.ID );
             OcciResponse response = HttpUtils.delete( ID );
             return ""+response.code;
         }
         catch ( Exception e )
         {
         }
         
         return null;
     }
 
     // --------   STORAGE    --------
     @Override
     public String createStorage( IStorage data )
     {
         try
         {
             Hashtable<String, String> headers = new Hashtable<String, String>();
             Hashtable<String, String> body    = new Hashtable<String, String>();
             
             OcciResponse response = null;
             
             String cdmiLink = data.getContent().get( IStorage.CDMI_LINK );
             if ( cdmiLink != null && cdmiLink.startsWith( "http" ) )
             {
                 headers.put( "Accept"      , "text/occi" );
                 headers.put( "Content-Type", "text/occi" );
                 headers.put( "Category"    , "storage;scheme=\"http://schemas.ogf.org/occi/infrastructure#\";class=\"kind\";" );
                 
                 String link = "<%s>;rel=" + "\"http://schemas.ogf.org/occi/core#link\";category=\"http://schemas.ogf.org/occi/infrastructure#storagelink\";";
                 
                 headers.put( "Link", String.format( link,
                                                     cdmiLink
                             ) );
                 
                 String attr = "occi.core.title=\"%s\","  +
                               "occi.core.summary=\"%s\"" ;
                 
                 headers.put( "X-OCCI-Attribute", String.format( attr,
                                                                 data.getContent().get( IStorage.TITLE   ),
                                                                 data.getContent().get( IStorage.SUMMARY )
                 ) );
                 
                 String URI = data.getContent().get( IStorage.URI ) + "/storage/";
                 response = HttpUtils.post( URI, headers, body );
             }
             else
             {
                 headers.put( "Accept"      , "text/occi" );
                 headers.put( "Content-Type", "text/occi" );
                 headers.put( "Category", "storage;scheme=\"http://schemas.ogf.org/occi/infrastructure#\";class=\"kind\";" );
                 
                 String attr = "occi.core.title=\"%s\","  +
                 "occi.core.summary=\"%s\"" ;
                 
                 headers.put( "X-OCCI-Attribute", String.format( attr,
                                                                 data.getContent().get( IStorage.TITLE   ),
                                                                 data.getContent().get( IStorage.SUMMARY )
                 ) );
                 
                 File[] files = new File[]{ new File( data.getContent().get( IStorage.FILE ) ) };
                 String URI = data.getContent().get( IStorage.URI ) + "/storage/";
                 response = HttpUtils.multipartPost( URI, headers, 
                                                                  null, files );
             }
             
             String result = null;
             if ( response.content != null ) result = response.content[ 0 ];
             
             return result;
         }
         catch ( Exception e )
         {
         }
         
         return null;
     }
 
     @Override
     public String deleteStorage( IStorage data )
     {
         try
         {
             String ID = data.getContent().get( IStorage.ID );
             OcciResponse response = HttpUtils.delete( ID );
             return ""+response.code;
         }
         catch ( Exception e )
         {
         }
         
         return null;
     }
 
     // --------   COMPUTE    --------
     @Override
     public String createCompute( ICompute data )
     {
         try
         {
             Hashtable<String, String> headers = new Hashtable<String, String>();
             Hashtable<String, String> body    = new Hashtable<String, String>();
             
             headers.put( "Accept"      , "text/occi" );
             headers.put( "Content-Type", "text/occi" );
             headers.put( "Category"    , "compute;scheme=\"http://schemas.ogf.org/occi/infrastructure#\";class=\"kind\";" );
             
             String attr = "occi.core.title=\"%s\","           +
                           "occi.core.summary=\"%s\","         +
                           "occi.compute.architecture=\"%s\"," +
                           "occi.compute.cores=\"%s\","        +
                           "occi.compute.memory=%s";
             
             headers.put( "X-OCCI-Attribute", String.format( attr,
                                                             data.getContent().get( ICompute.TITLE        ),
                                                             data.getContent().get( ICompute.SUMMARY      ),
                                                             data.getContent().get( ICompute.ARCHITECTURE ),
                                                             data.getContent().get( ICompute.CORES        ),
                                                             data.getContent().get( ICompute.MEMORY       )
                                                             ) );
             
             String link = "";
             String storageID = data.getContent().get( ICompute.STORAGE );
             
             if ( storageID.startsWith( "http://" ) ) // it's a CDMI storage
             {
                 link = "</network/%s>;rel=" + "\"http://schemas.ogf.org/occi/infrastructure#network\";category=\"http://schemas.ogf.org/occi/core#link\";," +
                        "<%s>;rel=" + "\"http://schemas.ogf.org/occi/core#link\";category=\"http://schemas.ogf.org/occi/infrastructure#storagelink\";";
                 
                 headers.put( "Link", String.format( link,
                                                     data.getContent().get( ICompute.NETWORK ),
                                                     storageID
                             ) );
             }
             else // it's a OCCI-image
             {
                 link = "</network/%s>;rel=" + "\"http://schemas.ogf.org/occi/infrastructure#network\";category=\"http://schemas.ogf.org/occi/core#link\";," +
                        "</storage/%s>;rel=" + "\"http://schemas.ogf.org/occi/infrastructure#storage\";category=\"http://schemas.ogf.org/occi/core#link\";";
                 
                 headers.put( "Link", String.format( link,
                                                     data.getContent().get( ICompute.NETWORK ),
                                                     storageID
                 ) );
             }
             
             String URI = data.getContent().get( INetwork.URI ) + "/compute/";
             OcciResponse response = HttpUtils.post( URI, 
                                                     headers, 
                                                     body );
             String result = null;
             if ( response.content != null ) result = response.content[ 0 ];
             return result;
         }
         catch ( Exception e )
         {
         }
         
         return null;
     }
 
     @Override
     public String deleteCompute( ICompute data )
     {
         try
         {
             String ID = data.getContent().get( ICompute.ID );
             OcciResponse response = HttpUtils.delete( ID );
             return ""+response.code;
         }
         catch ( Exception e )
         {
         }
         
         return null;
     }
 
     @Override
     public String startCompute( ICompute data )
     {
         try
         {
             Hashtable<String, String> headers = new Hashtable<String, String>();
             Hashtable<String, String> body    = new Hashtable<String, String>();
             
             headers.put( "Accept"      , "text/occi" );
             headers.put( "Content-Type", "text/occi" );
             headers.put( "Category", "start; scheme=\"http://schemas.ogf.org/occi/infrastructure/compute/action#\";class=\"action\"" );
             
             headers.put( "X-OCCI-Attribute", "method=\"poweron\"" );
             
             String URI = data.getContent().get( ICompute.ID ) + "?action=start";
             OcciResponse response = HttpUtils.post( URI, headers, body );
             String result = null;
             if ( response.content != null ) result = response.content[ 0 ];
             return result;
         }
         catch ( Exception e )
         {
             e.printStackTrace();
         }
         
         return null;
     }
 
     @Override
     public String stopCompute( ICompute data )
     {
         try
         {
             Hashtable<String, String> headers = new Hashtable<String, String>();
             Hashtable<String, String> body    = new Hashtable<String, String>();
             
             headers.put( "Accept"      , "text/occi" );
             headers.put( "Content-Type", "text/occi" );
             headers.put( "Category", "stop; scheme=\"http://schemas.ogf.org/occi/infrastructure/compute/action#\";class=\"action\"" );
             
             headers.put( "X-OCCI-Attribute", "method=\"poweroff\"" );
             
             String URI = data.getContent().get( ICompute.ID ) + "?action=stop";
             OcciResponse response = HttpUtils.post( URI, headers, body );
             String result = null;
             if ( response.content != null ) result = response.content[ 0 ];
             return result;
         }
         catch ( Exception e )
         {
         }
         
         return null;
     }
 
     // --------   TEMPLATE    --------
     @Override
     public String createTemplate( ITemplate data )
     {
         // TODO: tobeimplemented
         return null;
     }
 
     @Override
     public String deleteTemplate( ITemplate data )
     {
         // TODO: tobeimplemented
         return null;
     }
     
     // --------   LISTs    --------
     
     public INetwork[] getNetworks( String uri )
     { 
         INetwork[] result = null;
         try
         {
             Hashtable<String, String> headers = new Hashtable<String, String>();
             headers.put( "Accept", "*/*" );
            headers.put( "Category", "network;scheme=\"http://schemas.ogf.org/occi/infrastructure#\";class=\"kind\";" );
             
             String URI = uri + "/network/";
             OcciResponse response = HttpUtils.get( URI, 
                                                    headers );
 
             if ( response.content != null && response.content.length > 0 && response.code != 500 )
             {
                 result = new INetwork[ response.content.length ];
                 int i = 0;
                 for ( String s : response.content )
                 {
                     result[ i ] = new INetwork();
                     String networkURI = s.substring( s.indexOf( "network" ) + 8 );
                     result[ i ].getContent().put( INetwork.ID , networkURI       );
                     result[ i ].getContent().put( INetwork.URI, URI + networkURI );
                     
                     // Details about this Storage
                     OcciResponse inner = HttpUtils.get( URI + networkURI, headers );
                     if ( inner != null && inner.content != null )
                     {
                         for ( String attr : inner.content )
                         {
                             if ( attr.contains( "-Attribute" ) )
                             {
                                 String ATT = attr.substring( attr.indexOf( "Attribute" ) + 11 );
                                 String[] tokens = ATT.split( "=" );
                                 if ( tokens.length == 2 )
                                 {
                                     result[ i ].getAttributes().put( tokens[ 0 ], tokens[ 1 ] );
                                 }
                                 else
                                 {
                                     result[ i ].getAttributes().put( tokens[ 0 ], "" );
                                 }
                             }
                         }
                     }
                     
                     i++;
                 }
             }
         }
         catch ( Exception e )
         {
             e.printStackTrace();
         }
         
         return result;
     }
     
     public IStorage[] getStorages( String uri )
     { 
         IStorage[] result = null;
         try
         {
             Hashtable<String, String> headers = new Hashtable<String, String>();
             headers.put( "Accept", "*/*" );
             headers.put( "Category", "storage;scheme=\"http://schemas.ogf.org/occi/infrastructure#\";class=\"kind\";" );
             
             String URI = uri + "/storage/";
             OcciResponse response = HttpUtils.get( URI, 
                                                    headers );
 
             if ( response.content != null && response.content.length > 0 && response.code != 500 )
             {
                 result = new IStorage[ response.content.length ];
                 int i = 0;
                 for ( String s : response.content )
                 {
                     result[ i ] = new IStorage();
                     String storageURI = s.substring( s.indexOf( "storage" ) + 8 );
                     result[ i ].getContent().put( IStorage.ID, storageURI        );
                     result[ i ].getContent().put( IStorage.URI, URI + storageURI );
                     
                     // Details about this Storage
                     OcciResponse inner = HttpUtils.get( URI + storageURI, headers );
                     if ( inner != null && inner.content != null )
                     {
                         for ( String attr : inner.content )
                         {
                             if ( attr.contains( "-Attribute" ) )
                             {
                                 String ATT = attr.substring( attr.indexOf( "Attribute" ) + 11 );
                                 String[] tokens = ATT.split( "=" );
                                 if ( tokens.length == 2 )
                                 {
                                     result[ i ].getAttributes().put( tokens[ 0 ], tokens[ 1 ] );
                                 }
                                 else
                                 {
                                     result[ i ].getAttributes().put( tokens[ 0 ], "" );
                                 }
                             }
                         }
                     }
                     
                     i++;
                 }
             }
         }
         catch ( Exception e )
         {
             e.printStackTrace();
         }
         
         return result;
     }
     
     public ICompute [] getComputes ( String uri )
     { 
         ICompute[] result = null;
         try
         {
             Hashtable<String, String> headers = new Hashtable<String, String>();
             headers.put( "Accept", "*/*" );
             headers.put( "Category", "compute; scheme=\"http://schemas.ogf.org/occi/infrastructure#\";class=\"kind\";" );
             
             String URI = uri + "/compute/";
             OcciResponse response = HttpUtils.get( URI, 
                                                    headers );
 
             if ( response.content != null && response.content.length > 0 && response.code != 500 )
             {
                 result = new ICompute[ response.content.length ];
                 int i = 0;
                 for ( String s : response.content )
                 {
                     result[ i ] = new ICompute();
                     String computeURI = s.substring( s.indexOf( "compute" ) + 8 );
                     result[ i ].getContent().put( ICompute.ID, computeURI        );
                     result[ i ].getContent().put( ICompute.URI, URI + computeURI );
                     
                     // Details about this Compute
                     OcciResponse inner = HttpUtils.get( URI + computeURI, headers );
                     if ( inner != null && inner.content != null )
                     {
                         for ( String attr : inner.content )
                         {
                             if ( attr.contains( "-Attribute" ) )
                             {
                                 String ATT = attr.substring( attr.indexOf( "Attribute" ) + 11 );
                                 String[] tokens = ATT.split( "=" );
                                 
                                 if ( tokens.length >= 2 )
                                 {
                                     //remove extra """
                                     tokens[ 0 ] = ATT.substring( 0, ATT.indexOf( "=" ) ).replace( "\"", "" );
                                     tokens[ 1 ] = ATT.substring( ATT.indexOf( "=" )+1 ).replace( "\"", "" );
                                 }
                                 else if ( tokens.length == 1 )
                                 {
                                     tokens = new String[]{ tokens[ 0 ].replace( "\"", "" ), "" };
                                 }
                                 
                                 result[ i ].getAttributes().put( tokens[ 0 ], tokens[ 1 ] );
                             }
                             else if ( attr.startsWith( "Link" ) ) // Format[  Link: </api/compute/ea97ad10-8272-4df3-a9dc-484b0aa75902?action=start>; ... ]
                             {
                                 String linkCnt = attr.substring( attr.indexOf( "<" )+1, attr.indexOf( ">" ) );
                                 
                                 result[ i ].getLinks().add( linkCnt );
                             }
                         }
                     }
                     
                     i++;
                 }
             }
         }
         catch ( Exception e )
         {
             e.printStackTrace();
         }
         
         return result;
     }
     
     public ITemplate[] getTemplates( String uri )
     { 
         ITemplate[] result = null;
         return result; 
     }
     
     public static void main( String[] args )
     {
         try
         {
             OcciOneManagerImpl occi = new OcciOneManagerImpl();
             ICompute[] computes = occi.getComputes( "http://129.217.211.147:3000" );
             if ( computes != null )
                 for ( ICompute s : computes )
                 {
                     System.out.println( s );
                 }
             else
             {
                 System.out.println( "nix" );
             }
             
             INetwork[] networks = occi.getNetworks( "http://129.217.211.147:3000" );
             if ( networks != null )
                 for ( INetwork s : networks )
                 {
                     System.out.println( s );
                 }
             else
             {
                 System.out.println( "nix" );
             }
             
             IStorage[] storages = occi.getStorages( "http://129.217.211.147:3000" );
             if ( storages != null )
                 for ( IStorage s : storages )
                 {
                     System.out.println( s );
                 }
             else
             {
                 System.out.println( "nix" );
             }
         }
         catch ( Exception e )
         {
             e.printStackTrace();
         }
     }
 }
