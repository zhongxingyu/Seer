 package de.zib.gndms.kit.network.test;
 import de.zib.gndms.kit.network.GNDMSFileTransfer;
 /*
  * Copyright 2008-2011 Zuse Institute Berlin (ZIB)
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 
 
 import de.zib.gndms.kit.network.NetworkAuxiliariesProvider;
 import de.zib.gndms.kit.network.PersistentMarkerListener;
 import de.zib.gndms.model.gorfx.FTPTransferState;
 import de.zib.gndms.model.test.ModelEntityTestBase;
 import org.apache.axis.types.URI;
 import org.apache.log4j.PropertyConfigurator;
 import org.globus.ftp.GridFTPClient;
 import org.globus.ftp.exception.ClientException;
 import org.globus.ftp.exception.ServerException;
 import org.jetbrains.annotations.NotNull;
 import org.joda.time.DateTime;
 import org.testng.Assert;
 import org.testng.annotations.*;
 
 import javax.persistence.EntityManager;
 import java.io.IOException;
 
 /**
  * @author  try ma ik jo rr a zib
  * @version  $Id$
  * <p/>
  * User: mjorra, Date: 06.10.2008, Time: 11:05:48
  *
  * To run this test properly you need the transfer test data files.
  * Ask your local test data files provider for info.
  *
  * Make sure the sourceURI points to the directory containing this files.
  *
  * Commands to generate test files:
  *          dd if=/dev/urandom of=a_1KB_file bs=1k count=1
  *          dd if=/dev/urandom of=b_1MB_file bs=1k count=1024
  *          dd if=/dev/urandom of=c_1GB_file bs=1M count=1024
  */
 public class TransferStateTest extends ModelEntityTestBase {
 
     private TransferTestMetaData transferData;
     private String logFileConfig;
     private final String TRANSFER_KEY = "transfer-test-a000-a000-fakekey00001";
     private FTPTransferState transferState;
     private EntityManager eM;
 
 
     @Parameters( {"srcURI", "destURI", "logFileCfg", "dbPath", "dbName" } )
     public TransferStateTest( @NotNull String srcURI, @NotNull String destURI, @NotNull String logFileCfg,
                               @NotNull String dbPath, @Optional( "c3grid" ) String dbName )
     {
         super( dbPath, dbName );
 
         // transferData = new TransferTestThreeFiles( srcURI, destURI );
         // transferData = new DirectoryTransferTest( srcURI, destURI );
 
         logFileConfig = logFileCfg;
 
 
         System.out.println( "srcURI: " + srcURI );
         System.out.println( "destURI: " + destURI );
         System.out.println( "logFileCfg: " + logFileCfg );
         System.out.println( "dbPath: " + getDbPath( ) );
         System.out.println( "dbName: " + getDbName( ) );
     }
 
 
     @BeforeClass( groups={ "net" } )
     public void beforeClass ( ) {
 
         PropertyConfigurator.configure( logFileConfig );
 
         transferData.initialize();
 
         eM = getEntityManager();
         transferState = (FTPTransferState) eM.find( FTPTransferState.class, TRANSFER_KEY );
     }
 
 
     @AfterClass( groups={ "net" } )
     public void afterClass ( ) {
         tryCloseEMF();
     }
 
 
     @Test( groups={ "net" } )
     public void testIt ( ) throws ServerException, IOException, ClientException {
 
         GridFTPClient src = null;
         GridFTPClient dest = null;
         // see NetworAuxiliariesProvider for a description of the code below
         try {
             if( transferState == null ) {
                 newTransfer( );
             } else {
                 System.out.println( "Resuming transfer" );
             }
             PersistentMarkerListener pml = new PersistentMarkerListener( );
            //pml.setEntityManager( eM );
             pml.setTransferState( transferState );
 
             URI suri = new URI ( transferData.getSourceURI( ) );
             URI duri = new URI ( transferData.getDestinationURI( ) );
 
             // obtain clients
             src = NetworkAuxiliariesProvider.getGridFTPClientFactory().createClient( suri, null );
             dest = NetworkAuxiliariesProvider.getGridFTPClientFactory().createClient( duri, null );
 
 
             // setup transfer handler
             GNDMSFileTransfer transfer = new GNDMSFileTransfer();
             transfer.setSourceClient( src );
             transfer.setSourcePath( suri.getPath() );
 
             transfer.setDestinationClient( dest );
             transfer.setDestinationPath( duri.getPath() );
 
             transfer.setFiles( transferData.getFileMap( ) );
 
             // estimate transfer time
             long ets = transfer.estimateTransferSize(  );
             Assert.assertEquals( ets, transferData.expectedTransferSize( ), "Transfer size mismatch" );
 
             DateTime dat = new DateTime( );
             Float tt = NetworkAuxiliariesProvider.getBandWidthEstimater().estimateBandWidthFromTo( suri.getHost( ), duri.getHost( ) );
             Assert.assertNotNull ( tt, "estimated band width" );
             System.out.println( "Estimated transfer time in sec.: "
                 + NetworkAuxiliariesProvider.calculateTransferTime( ets, tt.floatValue(), -1 ) );
 
             /*
             // listing files
             System.out.println( "fetching file listing" );
             Vector<FileInfo> inf = src.list( );
             for( FileInfo fi: inf )
                 System.out.println( fi );
                 */
 
             System.out.println( "Starting transfer at " + (new DateTime( )).toString( ) );
             transfer.performPersistentTransfer( pml );
             System.out.println( "Finished transfer at " + (new DateTime( )).toString( ) );
 
             
         } finally {
             eM.close();
             try {
             if( src != null )
                 src.close();
             if( dest != null )
                 dest.close();
             } catch ( Exception e ) {
                 System.out.println( "closing server connection gives error: " + e.getMessage() );
             }
         }
     }
 
     
     private void newTransfer( ) {
         
         System.out.println( "Initializing new transfer" );
         transferState = new FTPTransferState();
         transferState.setTransferId( TRANSFER_KEY );
     }
 }
