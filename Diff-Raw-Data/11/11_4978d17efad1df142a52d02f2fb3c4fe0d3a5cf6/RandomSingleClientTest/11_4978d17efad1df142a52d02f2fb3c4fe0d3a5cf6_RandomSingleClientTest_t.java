 package edu.uw.zookeeper;
 
 import java.util.List;
 
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.junit.runners.JUnit4;
 
 import com.google.common.collect.Lists;
 import com.google.common.util.concurrent.ListenableFuture;
 import com.google.common.util.concurrent.Service;
 
 import edu.uw.zookeeper.client.ZNodeViewCache;
 import edu.uw.zookeeper.clients.common.CallUntilPresent;
 import edu.uw.zookeeper.clients.common.Generator;
 import edu.uw.zookeeper.clients.common.IterationCallable;
 import edu.uw.zookeeper.clients.common.SubmitCallable;
 import edu.uw.zookeeper.clients.random.BasicRequestGenerator;
 import edu.uw.zookeeper.common.ListAccumulator;
 import edu.uw.zookeeper.common.Pair;
 import edu.uw.zookeeper.common.ServiceMonitor;
 import edu.uw.zookeeper.protocol.Message;
 import edu.uw.zookeeper.protocol.Operation;
 import edu.uw.zookeeper.protocol.proto.Records;
 
 @RunWith(JUnit4.class)
 public class RandomSingleClientTest {
     
     @Test(timeout=30000)
     public void testRandom() throws Exception {
         SimpleServerAndClient client = SimpleServerAndClient.defaults().setDefaults();
         ServiceMonitor monitor = client.getRuntimeModule().getServiceMonitor();
         for (Service service: client.build()) {
             monitor.add(service);
         }
         monitor.startAsync().awaitRunning();
         
         ZNodeViewCache<?, Operation.Request, Message.ServerResponse<?>> cache = 
                 ZNodeViewCache.newInstance( 
                         client.getClientBuilder().getConnectionClientExecutor());
         int iterations = 100;
         Generator<Records.Request> requests = BasicRequestGenerator.create(cache);
         ListAccumulator<Pair<Records.Request, ListenableFuture<Message.ServerResponse<?>>>> accumulator = ListAccumulator.create(
                 SubmitCallable.create(requests, cache),
                 Lists.<Pair<Records.Request, ListenableFuture<Message.ServerResponse<?>>>>newArrayListWithCapacity(iterations)); 
         List<Pair<Records.Request, ListenableFuture<Message.ServerResponse<?>>>> results = 
                 CallUntilPresent.create(IterationCallable.create(iterations, iterations, accumulator)).call();
         for (Pair<Records.Request, ListenableFuture<Message.ServerResponse<?>>> result: results) {
            result.second().get();
            // hard to check for API errors here
            // since we are submitting requests asynchronously
         }
 
         monitor.stopAsync().awaitTerminated();
     }
 }
