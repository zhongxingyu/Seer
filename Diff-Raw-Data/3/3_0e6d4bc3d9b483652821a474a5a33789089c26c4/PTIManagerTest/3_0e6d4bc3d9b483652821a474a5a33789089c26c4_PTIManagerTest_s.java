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
 
 
 package dk.dbc.opensearch.components.pti;
 
 
 import dk.dbc.opensearch.common.config.PTIManagerConfig;
 import dk.dbc.opensearch.common.db.Processqueue;
 import dk.dbc.opensearch.common.fedora.IObjectRepository;
 import dk.dbc.opensearch.common.types.CompletedTask;
 import dk.dbc.opensearch.common.types.SimplePair;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Vector;
 import java.util.concurrent.FutureTask;
 import java.util.concurrent.ThreadPoolExecutor;
 
 import org.junit.*;
 import org.compass.core.Compass;
 
 import mockit.Mock;
 import mockit.MockClass;
 import mockit.Mockit;
 
 
 import java.sql.SQLException;
 import org.apache.commons.configuration.ConfigurationException;
 import java.net.MalformedURLException;
 import java.util.concurrent.RejectedExecutionException;
 import java.io.IOException;
 import javax.xml.rpc.ServiceException;
 
 import static org.easymock.classextension.EasyMock.*;
 
 
 /**
  * 
  */
 public class PTIManagerTest 
 {
     PTIManager ptiManager;
     Processqueue mockPQ = createMock( Processqueue.class );
     PTIPool mockPTIPool = createMock( PTIPool.class);
     CompletedTask mockCompletedTask = createMock( CompletedTask.class );
 
     IObjectRepository objectRepository = createMock( IObjectRepository.class );
 
     static FutureTask mockFuture = createMock( FutureTask.class );
     static CompletedTask dummyTask = new CompletedTask( mockFuture, new SimplePair< Boolean, Integer >( true, 1 ) );
     static Vector< CompletedTask > checkJobsVector =  new Vector< CompletedTask >();
 
 
     @MockClass( realClass = PTIManagerConfig.class )
     public static class MockPTIManagerConfig
     {
         @Mock public static String getQueueResultsetMaxSize()
         {
             return "2";
         }
         @Mock public static String getRejectedSleepTime()
         {
             return "5";
         }
  
     }
 
 //    @MockClass( realClass = FedoraObjectRepository.class )
 //    public static class MockObjectRepository
 //    {
 //
 //    }
 
     ThreadPoolExecutor mockExecutor = createMock( ThreadPoolExecutor.class );
     Compass mockCompass = createMock( Compass.class );
    SimplePair< String, Integer > mockSimplePair = createMock( SimplePair.class );
 
 
     @MockClass( realClass =  PTIPool.class )
     public static class MockPTIPool
     {
         @Mock public void $init( ThreadPoolExecutor threadpool, Compass compass, HashMap< SimplePair < String, String >, ArrayList< String > > jobMap ) 
         {
         
         }
 
         @Mock public void submit( String fedoraHandle, Integer queueID ) 
         {
             if( queueID == 1 )
             {
                 throw new RejectedExecutionException( "test" );
             }
         }
 
         @Mock public Vector< CompletedTask > checkJobs()
         {
             checkJobsVector.add( dummyTask );
             //System.out.println( "size of checkJobs: " + checkJobsVector.size() );
             return checkJobsVector;
         }
     
     }
 
 
     // @MockClass( realClass =  .class )
 
 
     /**
      *
      */
     @Before 
     public void SetUp() { }
 
     /**
      *
      */
     @After public void TearDown() {
 
         Mockit.tearDownMocks();
         reset( mockPQ );
         reset( mockPTIPool );
         reset( mockCompletedTask );
        reset( mockSimplePair );
         reset( mockCompass );
         reset( mockFuture );
         reset( mockExecutor );
     }
 
     /**
      * Testing the instantiation of the PTIManager.
      */
     @Test public void testConstructor() throws ClassNotFoundException, SQLException, ConfigurationException 
     {
         /**
          * setup
          */
         Mockit.setUpMocks( MockPTIManagerConfig.class );
 
         /**
          * expectations
          */
 
         expect( mockPQ.deActivate() ).andReturn( 0 );
 
         /**
          * replay
          */
 
         replay( mockPQ );
         replay( mockPTIPool );
 
         /**
          * do stuff
          */
 
         ptiManager = new PTIManager( mockPTIPool, mockPQ );
 
         /**
          * verify
          */
         verify( mockPQ );
         verify( mockPTIPool );
       
     }
 
     /**
      * tests the update methods happy path
      */
  
     @Test @Ignore public void testUpdateMethodHappyPath() throws ClassNotFoundException, SQLException, ConfigurationException, InterruptedException, ServiceException, MalformedURLException, IOException
     {
         /**
          * setup
          */
         Mockit.setUpMocks( MockPTIManagerConfig.class );
         Vector< SimplePair< String, Integer > > newJobs = new Vector< SimplePair< String, Integer > >();
         newJobs.add( new SimplePair< String, Integer >( "test1", 1 ) );
         newJobs.add( new SimplePair< String, Integer >( "test2", 2 ) );
        
 
         Vector< CompletedTask<SimplePair<Boolean, Integer>> > finishedJobs =  new Vector< CompletedTask<SimplePair<Boolean, Integer>> >();
         finishedJobs.add( mockCompletedTask );
 
         /**
          * expectations
          */
         //constructor
         expect( mockPQ.deActivate() ).andReturn( 0 );
         //update method
         expect( mockPQ.pop( 2 ) ).andReturn( newJobs );
         //while loop on newJobs
         mockPTIPool.submit( "test1", 1 );
         mockPTIPool.submit( "test2", 2 );
 
         //out of while loop
 //        expect( mockPTIPool.checkJobs() ).andReturn( finishedJobs );
 //        expect( mockCompletedTask.getResult() ).andReturn( new SimplePair< Boolean, Integer >( true, 1 ) );
 //        mockPQ.commit( 1 );
         
         /**
          * replay
          */
         
         replay( mockPQ );
         replay( mockPTIPool);
         replay( mockCompletedTask );
             
         /**
          * do stuff
          */
 
         ptiManager = new PTIManager( mockPTIPool, mockPQ );
         ptiManager.update();
 
         /**
          * verify
          */
         verify( mockPTIPool );
         verify( mockPQ );
         verify( mockCompletedTask );
     }
 
     /**
      * Tests the behaviour of the update method when the finishedjobs contains a 
      * CompletedTask with a null value 
      */
 }
