 package com.tinkerpop.rexster.protocol;
 
 import com.tinkerpop.rexster.Tokens;
 import com.tinkerpop.rexster.client.RexsterClient;
 import org.msgpack.type.Value;
 import java.util.List;
 import java.util.Map;
 
 /**
  * A bit of an experiment.
  */
 public class TryRexProSessionless {
 
     private static int cycle = 0;
 
     public static void main(final String[] args) throws Exception {
         int c = Integer.parseInt(args[1]);
         final int exerciseTime = Integer.parseInt(args[2]) * 60 * 1000;
 
         for (int ix = 0; ix < c; ix++) {
             new Thread(new Runnable() {
 
                 @Override
                 public void run() {
                     lotsOfCalls(args[0].split(","), exerciseTime);
                 }
             }).start();
         }
 
         Thread.currentThread().join();
     }
 
     private static void lotsOfCalls(final String[] hosts, final int exerciseTime){
 
         final long start = System.currentTimeMillis();
         long checkpoint = System.currentTimeMillis();
 
        while ((start - checkpoint) < exerciseTime) {
             cycle++;
             System.out.println("Exercise cycle: " + cycle);
 
             try {
 
                 final RexsterClient client = new RexsterClient(hosts);
                 final List<Map<String, Value>> results = client.gremlin("g=rexster.getGraph('gratefulgraph');g.V;");
                 int counter = 1;
                 for (Map<String, Value> result : results) {
                     final String vId = result.get(Tokens._ID).asRawValue().getString();
                     final List<Map<String, Value>> innerResults = client.gremlin(String.format("g=rexster.getGraph('gratefulgraph');g.v(%s)", vId));
                     System.out.println(innerResults.get(0));
                     counter++;
                 }
 
                long end = System.currentTimeMillis() - checkpoint;
                 System.out.println((checkpoint - start) + ":" + end);
                 System.out.println(counter / (end / 1000));
             } catch (Exception ex) {
                 ex.printStackTrace();
             }
         }
     }
 }
