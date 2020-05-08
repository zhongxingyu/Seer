 package dbc.opensearch.components.pti.tests;
 /** \brief UnitTest for PTIPoolAdm class */
 
 import dbc.opensearch.components.pti.PTIPoolAdm;
import dbc.opensearch.components.pti.PTIPool;
 import dbc.opensearch.tools.Processqueue;
 import dbc.opensearch.tools.Estimate;
 
 import org.junit.*;
 import static org.junit.Assert.*;
 
 import static org.easymock.classextension.EasyMock.*;
 
 import org.apache.log4j.Logger;
 
 import org.compass.core.Compass;
 import org.compass.core.CompassSession;
 
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.FutureTask;
 
 import com.mallardsoft.tuple.Pair;
 import com.mallardsoft.tuple.Tuple;
 import com.mallardsoft.tuple.Triple;
 
 import java.util.Vector;
 import java.util.Iterator;
 
 import dbc.opensearch.tools.PrivateAccessor;
 
 import java.lang.reflect.InvocationTargetException;
 import java.util.NoSuchElementException;
 import org.compass.core.converter.ConversionException;
 import java.util.concurrent.ExecutionException;
 
 public class PTIPoolAdmTest {
     
     boolean gotException;
     PTIPoolAdm ptiPoolAdm;
     PTIPool mockPTIPool;
     Processqueue mockProcessqueue;
     Estimate mockEstimate;
     Compass mockCompass;
     FutureTask mockFuture;
     Pair mockPair;
     Integer testInteger = 1;
     Triple< String, Integer, String > queueTriple1;
     Triple< String, Integer, String > queueTriple2;
     String exceptionString = "exception!!!";
 
     @Before public void setUp(){
         
         gotException = false;
         mockPTIPool = createMock( PTIPool.class );
         mockProcessqueue = createMock( Processqueue.class );
         mockEstimate = createMock( Estimate.class );
         mockCompass = createMock( Compass.class );
         mockPair = createMock( Pair.class );
         mockFuture = createMock( FutureTask.class );
         queueTriple1 = Triple.from( "testFedoraHandle", testInteger, "testItemID" );
         queueTriple2 = Triple.from( "testFedoraHandle", testInteger, "testItemID" );
  
         
         try{
             ptiPoolAdm = new PTIPoolAdm( mockPTIPool, mockProcessqueue, mockEstimate, mockCompass );
         }
         catch(Exception e){
             fail( String.format( "Caught Error, Should not have been thrown ", e.getMessage() ) ); 
 
         }
     }
     @After public void tearDown(){
 
         reset( mockProcessqueue );
         reset( mockEstimate );
         reset( mockCompass );
         reset( mockFuture );
         reset( mockPair );
         reset( mockPTIPool );
     }
 
     /**
      * Tests the general functionality of the PITPoolAdm when 
      * there is nothing on the processqueue
      */
     @Test public void emptyProcessqueueTest()throws Exception{
       
         /**1 setup: most done in setUp()
          */
         /**2 expectations
          */
         expect( mockProcessqueue.deActivate() ).andReturn( 2 );
         // call to startThreads
         expect( mockProcessqueue.pop() ).andThrow( new NoSuchElementException( exceptionString ) );
         /**3 execution
          */ 
         
         replay( mockProcessqueue );
         
         
         try{
         ptiPoolAdm.mainLoop();
         }catch( Exception e ) {
             Assert.fail( "unexpected exception" );
                 }
 
         /**4 verify
          */        
         verify( mockProcessqueue );
         
     }
 
     /**
      * Tests the startThreads method when there are elements on the queue
      */
     @Test public void startThreadsTest()throws Exception{ 
        
         /**1 setup: most done in setUp()
          */
         
         /**2 expectations
          */
         expect( mockProcessqueue.deActivate() ).andReturn( 2 );
         // call to startThreads
         expect( mockProcessqueue.pop() ).andReturn( queueTriple1 );
         expect( mockPTIPool.createAndJoinThread( isA( String.class), isA(String.class), isA(Estimate.class), isA(Compass.class) ) ).andReturn( mockFuture );
         expect( mockProcessqueue.pop() ).andReturn( queueTriple2 );
         expect( mockPTIPool.createAndJoinThread( isA( String.class), isA(String.class), isA(Estimate.class), isA(Compass.class) ) ).andReturn( mockFuture );
         expect( mockProcessqueue.pop() ).andThrow( new NoSuchElementException( exceptionString ) );
         //call to checkThreads
         expect( mockFuture.isDone() ).andReturn( false );
         expect( mockFuture.isDone() ).andReturn( true );
         expect( mockFuture.get() ).andReturn( 1l );
         mockProcessqueue.commit( testInteger );
         expect( mockFuture.isDone() ).andReturn( true );
         expect( mockFuture.get() ).andReturn( 1l );
         mockProcessqueue.commit( testInteger );
         // out of checkThreads
 
        
         /**3 execution
          */ 
         replay( mockFuture );
         replay( mockProcessqueue );
         replay( mockPTIPool );
         
         try{
         ptiPoolAdm.mainLoop();
         }catch( Exception e ) {
             Assert.fail( "unexpected exception" );
                 }
 
         /**4 verify
          */        
         verify( mockFuture );
         verify( mockProcessqueue );
         verify( mockPTIPool );      
     }
     
   @Test public void checkThreadsExecutionExceptionTest()throws Exception{
       
         /**1 setup: most done in setUp()
          */
       IllegalArgumentException iae = new IllegalArgumentException( exceptionString );
       ExecutionException ee = new ExecutionException( iae );
         /**2 expectations
          */
         expect( mockProcessqueue.deActivate() ).andReturn( 2 );
         // call to startThreads 
         expect( mockProcessqueue.pop() ).andReturn( queueTriple1 );
         expect( mockPTIPool.createAndJoinThread( isA( String.class), isA(String.class), isA(Estimate.class), isA(Compass.class) ) ).andReturn( mockFuture );
         expect( mockProcessqueue.pop() ).andThrow( new NoSuchElementException( exceptionString ) ); 
         //checkThreads
         expect( mockFuture.isDone() ).andReturn( true );
         expect( mockFuture.get() ).andThrow( ee ); 
                 
         /**3 execution
          */ 
         
         replay( mockProcessqueue );
         replay( mockFuture );
         replay( mockPTIPool );
         
         try{
         ptiPoolAdm.mainLoop();
         }catch( Exception e ) {
             gotException = true;
             assertTrue( e.getClass() == RuntimeException.class );
             assertTrue( e.getCause().getClass() == iae.getClass() );
             assertTrue( e.getCause().getMessage().equals( exceptionString ) );
                 }
         assertTrue( gotException );
 
         /**4 verify
          */        
         verify( mockProcessqueue );
         verify( mockFuture );
         verify( mockPTIPool );
         
     } 
 
     @Test public void checkThreadsConversionExceptionTest()throws Exception{
       
         /**1 setup: most done in setUp()
          */
       ConversionException ce = new ConversionException( exceptionString );
       ExecutionException ee = new ExecutionException( ce );
         /**2 expectations
          */
         expect( mockProcessqueue.deActivate() ).andReturn( 2 );
         // call to startThreads 
         expect( mockProcessqueue.pop() ).andReturn( queueTriple1 );
         expect( mockPTIPool.createAndJoinThread( isA( String.class), isA(String.class), isA(Estimate.class), isA(Compass.class) ) ).andReturn( mockFuture );
         expect( mockProcessqueue.pop() ).andThrow( new NoSuchElementException( exceptionString ) ); 
         //checkThreads
         expect( mockFuture.isDone() ).andReturn( true );
         expect( mockFuture.get() ).andThrow( ee );
         mockProcessqueue.commit( testInteger );
         
         /**3 execution
          */ 
         
         replay( mockProcessqueue );
         replay( mockFuture );
         replay( mockPTIPool );
         
         try{
         ptiPoolAdm.mainLoop();
         }catch( Exception e ) {
                 }
 
         /**4 verify
          */        
         verify( mockProcessqueue );
         verify( mockFuture );
         verify( mockPTIPool );
         
     }
 }
