 /*
  * Copyright (C) 2009 by Eric Herman <eric@freesa.org>
 * Copyright (C) 2009 by Robert Stewart
  * Use and distribution licensed under the 
  * GNU Lesser General Public License (LGPL) version 2.1.
  * See the COPYING file in the parent directory for full text.
  */
 package gearmanij.util;
 
 import static org.junit.Assert.assertEquals;
 import gearmanij.Connection;
 
 import java.io.PrintStream;
 import java.util.Collections;
 import java.util.List;
 import java.util.Map;
 
 public class TestUtil {
 
   public static void assertArraysEqual(final byte[] left, final byte[] right) {
     if (left == null || right == null) {
       assertEquals(left, right);
       return;
     }
 
     assertEquals("lengths differ", left.length, right.length);
     for (int i = 0; i < left.length; i++) {
       assertEquals("element " + i, left[i], right[i]);
     }
   }
 
   public static void assertEqualsIgnoreCase(String left, String right) {
     if (left != null && left.equalsIgnoreCase(right)) {
       return;
     }
     assertEquals(left, right);
   }
 
   /**
    * @Deprecated This is handy for hacking by hand, but for our automated tests
    *             we should make assertions about the contents rather than dump a
    *             bunch of noise to stdout
    */
   @Deprecated
   public static void dump(Map<String, List<String>> responses) {
     dump(System.out, responses);
   }
 
   public static void dump(PrintStream out, Map<String, List<String>> responses) {
     for (Map.Entry<String, List<String>> entry : responses.entrySet()) {
       out.println(entry.getKey() + " response:");
       for (String response : entry.getValue()) {
         out.println(response);
       }
     }
   }
 
   /**
    * Returns true if a worker with the specified ID is found when querying a job
    * server for information on all connected workers.
    * 
    * @param conn
    *          Connection to a job server
    * @param id
    *          ID of the worker that is being searched for
    * @return
    */
   public static boolean isWorkerFoundByID(Connection conn, String id) {
     return isFunctionRegisteredForWorker(conn, id, null, false);
   }
 
   /**
   * Returns true if a worker with the specified ID is found when querying a job
   * server for information on all connected workers.
    * 
    * @param conn
    *          Connection to a job server
    * @param id
    *          ID of the worker that is being searched for
    * @return
    */
   public static boolean isFunctionRegisteredForWorker(Connection conn,
       String id, String name) {
     return isFunctionRegisteredForWorker(conn, id, name, true);
   }
 
   private static boolean isFunctionRegisteredForWorker(Connection conn,
       String id, String name, boolean checkName) {
     Map<String, List<String>> map;
     map = conn.textMode(Collections.singletonList("WORKERS"));
     boolean foundFunction = false;
     for (String workers : map.keySet()) {
       List<String> workerList = map.get(workers);
       for (String workerInfo : workerList) {
         if (workerInfo.contains(id)) {
           if (!checkName || workerInfo.contains(name)) {
             foundFunction = true;
             break;
           }
         }
       }
     }
     return foundFunction;
   }
 
 }
