 package edu.hm.dako.echo;
 
 import edu.hm.dako.echo.benchmarking.BenchmarkingClient;
 import edu.hm.dako.echo.benchmarking.BenchmarkingClientUserInterface;
 import edu.hm.dako.echo.benchmarking.UserInterfaceInputParameters;
 import edu.hm.dako.echo.benchmarking.UserInterfaceInputParameters.ImplementationType;
 import edu.hm.dako.echo.benchmarking.UserInterfaceResultData;
 import edu.hm.dako.echo.benchmarking.UserInterfaceStartData;
 import edu.hm.dako.echo.server.EchoServer;
 import edu.hm.dako.echo.server.ServerFactory;
 import junit.framework.Assert;
 import org.junit.Test;
 
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 import java.util.concurrent.TimeUnit;
 
 public class Tester implements BenchmarkingClientUserInterface {
 
     private UserInterfaceResultData data;   // Ergebnisdaten des Tests
     private static final int NUMBER_OF_MESSAGES = 100;
     private static final int NUMBER_OF_CLIENTS = 10;
 
     private class TestStarterEchoServer implements Runnable {
 
         private EchoServer echoServer;
 
         public TestStarterEchoServer(ImplementationType type) {
             try {
                 echoServer = ServerFactory.getServer(type);
             } catch (Exception e) {
                 throw new RuntimeException(e);
             }
         }
 
         @Override
         public void run() {
             Thread.currentThread().setName("TestStarterEchoServerThread");
             System.out.println("Testserver startet");
             echoServer.start();
         }
 
 
         public void stopServer() {
             System.out.println("Stoppe Testserver");
             try {
                 echoServer.stop();
             } catch (Exception e) {
                 throw new RuntimeException(e);
             }
             System.out.println("Testserver ist gestoppt");
         }
     }
 
     @Test
     public void testRmiMultiThreaded() throws Exception {
         runTests(ImplementationType.RmiMultiThreaded);
     }
 
     @Test
     public void testTcpSingle() throws Exception {
         runTests(ImplementationType.TCPSingleThreaded);
     }
 
     @Test
     public void testUdpSingle() throws Exception {
         runTests(ImplementationType.UDPSingleThreaded);
     }
 
     @Test
     public void testTcpMulti() throws Exception {
         runTests(ImplementationType.TCPMultiThreaded);
     }
 
     @Test
     public void testUdpMulti() throws Exception {
         runTests(ImplementationType.UDPMultiThreaded);
     }
 
     private void runTests(ImplementationType type) throws InterruptedException {
         ExecutorService executorService = Executors.newSingleThreadExecutor();
         TestStarterEchoServer testServer = new TestStarterEchoServer(type);
         executorService.submit(testServer);
 
         // Input-Parameter
         UserInterfaceInputParameters param = new UserInterfaceInputParameters();
         param.setNumberOfMessages(NUMBER_OF_MESSAGES);
         param.setNumberOfClients(NUMBER_OF_CLIENTS);
         param.setImplementationType(type);
         
         //Fix RMI-Test
         if (ImplementationType.RmiMultiThreaded == type) {
         	param.setRemoteServerPort(ServerFactory.RMI_SERVER_PORT);
         }
 
         // Benchmarking-Client instanzieren und Benchmark starten
         BenchmarkingClient benchClient = new BenchmarkingClient();
         benchClient.executeTest(param, this);
         Assert.assertEquals(data.getNumberOfSentRequests(), data.getNumberOfResponses());
         Assert.assertTrue(data.getNumberOfSentRequests() >= NUMBER_OF_MESSAGES * NUMBER_OF_CLIENTS);
         testServer.stopServer();
         executorService.shutdown();
         try {
             executorService.awaitTermination(10, TimeUnit.MINUTES);
         } catch (InterruptedException e) {
             e.printStackTrace();
         }
     }
 
     @Override
     public void showStartData(UserInterfaceStartData data) {
         // Auto-generated method stub
     }
 
     @Override
     public void showResultData(UserInterfaceResultData data) {
         this.data = data;
 
     }
 
     @Override
     public void setMessageLine(String message) {
         // Auto-generated method stub
     }
 
     @Override
     public void resetCurrentRunTime() {
         // Auto-generated method stub
     }
 
     @Override
     public void addCurrentRunTime(long sec) {
         // Auto-generated method stub
     }
 }
