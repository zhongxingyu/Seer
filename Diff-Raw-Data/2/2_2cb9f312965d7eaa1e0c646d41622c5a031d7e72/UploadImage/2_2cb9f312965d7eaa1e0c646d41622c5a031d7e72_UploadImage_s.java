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
 
 import static com.google.inject.Scopes.SINGLETON;
 import static it.sauronsoftware.ftp4j.FTPClient.*;
 import static java.lang.String.format;
 import static java.lang.System.exit;
 import static javax.ws.rs.core.UriBuilder.fromUri;
 import it.sauronsoftware.ftp4j.FTPClient;
 import it.sauronsoftware.ftp4j.FTPDataTransferListener;
 
 import java.io.File;
 import java.util.HashMap;
 import java.util.Map;
 
 import javax.inject.Inject;
 import javax.inject.Named;
 
 import org.slf4j.Logger;
 
 import com.beust.jcommander.Parameter;
 import com.beust.jcommander.Parameters;
 import com.beust.jcommander.converters.FileConverter;
 import com.terradue.dsi.model.UploadTicket;
 import com.terradue.dsi.wire.FTPClientProvider;
 
 @Parameters( commandDescription = "Upload an image on DSI Cloud" )
 public final class UploadImage
     extends BaseTool
 {
 
     public static void main( String[] args )
     {
         exit( new UploadImage().execute( args ) );
     }
 
     @Parameter( names = { "--appliance" }, description = "The DSI applicance name" )
     private String applianceName;
 
    @Parameter( names = { "--description" }, description = "The DSI applicance name" )
     private String applianceDescription;
 
     @Parameter( names = { "--provider" }, description = "The DSI provider ID" )
     private String providerId;
 
     @Parameter( names = { "--qualifier" }, description = "The DSI qualifier ID" )
     private String qualifierId;
 
     @Parameter( names = { "--operating-system" }, description = "The DSI applicance Operating System [OPTIONAL]" )
     private String applianceOS = "Linux";
 
     @Parameter( names = { "--appliance-id" }, description = "The DSI applicance OS ID" )
     private String applianceOsId;
 
     @Parameter(
         names = { "--image" },
         description = "Path of the image descriptor (*.vmx) to upload (*.vmdk will be automatically detected)",
         converter = FileConverter.class
     )
     private File image;
 
     @Inject
     private FTPClient ftpsClient;
 
     private final Map<String, Integer> ftpProtocolMappings = new HashMap<String, Integer>();
 
     @Inject
     @Override
     public void setServiceUrl( @Named( "service.upload" ) String serviceUrl )
     {
         super.setServiceUrl( serviceUrl );
     }
 
     public void setFtpsClient( FTPClient ftpsClient )
     {
         this.ftpsClient = ftpsClient;
     }
 
     public UploadImage()
     {
         ftpProtocolMappings.put( "ftp", SECURITY_FTP );
         ftpProtocolMappings.put( "ftps", SECURITY_FTPS );
         ftpProtocolMappings.put( "ftpes", SECURITY_FTPES );
     }
 
     @Override
     protected void bindConfigurations()
     {
         super.bindConfigurations();
         bind( FTPClient.class ).toProvider( FTPClientProvider.class ).in( SINGLETON );
     }
 
     @Override
     public void execute()
         throws Exception
     {
         if ( !image.exists() || image.isDirectory() )
         {
             throw new IllegalArgumentException( format( "File %s must be an existing file (directories not supported)",
                                                         image ) );
         }
 
         // extension checker
         int extSeparator = image.getName().lastIndexOf( '.' );
         String extension = image.getName().substring( extSeparator + 1 );
 
         if ( !"vmx".equalsIgnoreCase( extension ) )
         {
             throw new IllegalArgumentException( format( "File %s is not a valid VMware Configuration File (.vmx)",
                                                         image ) );
         }
 
         String imageName = image.getName().substring( 0, extSeparator );
         File physicalImage = new File( image.getParent(), format( "%s.vmdk", imageName ) );
         if ( !physicalImage.exists() )
         {
             throw new IllegalArgumentException( format( "File %s not found!",
                                                         physicalImage ) );
         }
 
         logger.info( "Requesting FTP location where uploading images..." );
 
         UploadTicket uploadTicket = restClient.resource( fromUri( serviceUrl )
                                                          .queryParam( "providerId", providerId )
                                                          .queryParam( "qualifierId", qualifierId )
                                                          .queryParam( "applianceName", applianceName )
                                                          .queryParam( "applianceDescription", applianceDescription )
                                                          .queryParam( "applianceOS", applianceOS )
                                                          .queryParam( "applianceOsId", applianceOsId )
                                                          .build() ).get( UploadTicket.class );
 
         logger.info( "Uploading image: {} on {} (expires on {})...",
                      new Object[]
                      {
                          image.getAbsolutePath(),
                          uploadTicket.getFtpLocation().toString(),
                          uploadTicket.getExpirationDate()
                      } );
 
         logger.info( "Connecting to {}...", uploadTicket.getFtpLocation().getHost() );
 
         Integer securityLevel = ftpProtocolMappings.get( uploadTicket.getFtpLocation().getScheme().toLowerCase() );
         ftpsClient.setSecurity( securityLevel.intValue() );
 
         ftpsClient.connect( uploadTicket.getFtpLocation().getHost() );
         ftpsClient.setPassive( true );
 
         try
         {
             ftpsClient.login( "anonymous", "" );
             logger.info( "Successfully logged in! Moving to working directory {}",
                          uploadTicket.getFtpLocation().getPath() );
 
             ftpsClient.changeDirectory( uploadTicket.getFtpLocation().getPath() );
 
             upload( image, TYPE_TEXTUAL );
             upload( physicalImage, TYPE_BINARY );
         }
         finally
         {
             logger.info( "Disconnecting from {} server...", uploadTicket.getFtpLocation().getHost() );
 
             if ( ftpsClient.isConnected() )
             {
                 ftpsClient.disconnect( false );
             }
 
             logger.info( "Connnection closed, bye." );
         }
     }
 
     private void upload( File file, int type )
         throws Exception
     {
         ftpsClient.setType( type );
         ftpsClient.upload( file, new UploadTransferListener( logger, file ) );
     }
 
     private static final class UploadTransferListener
         implements FTPDataTransferListener
     {
 
         private final Logger logger;
 
         private final File toBeUploaded;
 
         private long transferred = 0;
 
         public UploadTransferListener( Logger logger, File toBeUploaded )
         {
             this.logger = logger;
             this.toBeUploaded = toBeUploaded;
         }
 
         @Override
         public void aborted()
         {
             logger.warn( "File {} transfer aborted unexpectetly, contact the DSI OPS", toBeUploaded );
         }
 
         @Override
         public void completed()
         {
             logger.info( "File {} trasfer complete", toBeUploaded );
         }
 
         @Override
         public void failed()
         {
             logger.error( "File {} transfer corrupted, contact the DSI OPS", toBeUploaded );
         }
 
         @Override
         public void started()
         {
             logger.info( "Started trasferring file {}...", toBeUploaded );
         }
 
         @Override
         public void transferred( int transferred )
         {
             this.transferred += transferred;
             System.out.printf( "%s%%\r", ( ( this.transferred * 100 ) / toBeUploaded.length() ) );
         }
 
     }
 
 }
