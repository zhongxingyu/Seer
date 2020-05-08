 /*
 * Copyright (C) 2009 by Robert Stewart
  * Use and distribution licensed under the 
  * GNU Lesser General Public License (LGPL) version 2.1.
  * See the COPYING file in the parent directory for full text.
  */
 package gearmanij;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertFalse;
 import static org.junit.Assert.assertTrue;
 import gearmanij.Worker.WorkerOption;
 import gearmanij.util.TestUtil;
 
 import java.util.Collection;
 import java.util.EnumSet;
 
 import org.junit.Ignore;
 import org.junit.Test;
 
 public class SimpleWorkerTest {
   @Test
   public void testWorkerOptions() {
     Worker worker = new SimpleWorker();
     assertEquals(worker.getWorkerOptions(), EnumSet.noneOf(WorkerOption.class));
   }
 
   @Test
   public void testRemoveOptions() {
     Worker worker = new SimpleWorker();
     worker.setWorkerOptions(WorkerOption.NON_BLOCKING, WorkerOption.GRAB_UNIQ);
     Collection<WorkerOption> c;
     c = EnumSet.of(WorkerOption.NON_BLOCKING, WorkerOption.GRAB_UNIQ);
     assertTrue(worker.getWorkerOptions().containsAll(c));
     worker.removeWorkerOptions(WorkerOption.GRAB_UNIQ);
     assertTrue(worker.getWorkerOptions().contains(WorkerOption.NON_BLOCKING));
     assertFalse(worker.getWorkerOptions().contains(WorkerOption.GRAB_UNIQ));
   }
 
   @Test
   public void testSetWorkerOptions() {
     Collection<WorkerOption> c = null;
     Worker worker = new SimpleWorker();
 
     worker.setWorkerOptions(WorkerOption.NON_BLOCKING);
     c = EnumSet.of(WorkerOption.NON_BLOCKING);
     assertTrue(worker.getWorkerOptions().containsAll(c));
 
     worker.clearWorkerOptions();
     worker.setWorkerOptions(WorkerOption.NON_BLOCKING,
         WorkerOption.NON_BLOCKING);
     c = EnumSet.of(WorkerOption.NON_BLOCKING);
     assertTrue(worker.getWorkerOptions().containsAll(c));
   }
 
   @Test
   public void testSetWorkerID() {
     Worker w = new SimpleWorker();
     Connection conn = new SocketConnection();
     String id = "SimpleWorker";
 
     try {
       w.addServer(conn);
       w.setWorkerID(id);
       assertTrue(TestUtil.isWorkerFoundByID(conn, id));
     } catch (Exception e) {
       e.printStackTrace();
     } finally {
       for (Exception e : w.close()) {
         e.printStackTrace();
       }
     }
   }
 
   @Test
   public void testRegisterFunction() {
     Worker w = new SimpleWorker();
     Connection conn = new SocketConnection();
     JobFunction digest = new DigestFunction();
     String id = "testRegisterFunction";
 
     try {
       w.addServer(conn);
       w.setWorkerID(id);
       w.registerFunction(digest);
       String name = digest.getName();
       assertTrue(TestUtil.isFunctionRegisteredForWorker(conn, id, name));
     } catch (Exception e) {
       e.printStackTrace();
     } finally {
       for (Exception e : w.close()) {
         e.printStackTrace();
       }
     }
   }
   
   /**
    * Registers a function that sleeps for 5 seconds with a timeout of only 1 second.
    */
   @Test
   public void testRegisterFunctionWithTimeout() {
     Worker w = new SimpleWorker();
     Connection conn = new SocketConnection();
     JobFunction reverse = new ReverseFunction();
    int delay = 10;
    int timeout = 2;
     String id = "testRegisterFunctionWithTimeout";
     PacketType type;
     
     // Set number of seconds to delay execution
     ((ReverseFunction) reverse).setDelay(delay);
 
     try {
       w.addServer(conn);
       w.setWorkerID(id);
       w.registerFunction(reverse, timeout);
       String name = reverse.getName();
       assertTrue(TestUtil.isFunctionRegisteredForWorker(conn, id, name));
       type = w.grabJob(conn);
       assertTrue(PacketType.JOB_ASSIGN == type);
       w.unregisterFunction(reverse);
       assertFalse(TestUtil.isFunctionRegisteredForWorker(conn, id, name));
       
     } catch (Exception e) {
       e.printStackTrace();
     } finally {
       for (Exception e : w.close()) {
         e.printStackTrace();
       }
     }
   }
 
   @Test
   public void testUnregisterFunction() {
     Worker w = new SimpleWorker();
     Connection conn = new SocketConnection();
     JobFunction digest = new DigestFunction();
     String id = "testUnregisterFunction";
 
     try {
       w.addServer(conn);
       w.setWorkerID(id);
       w.registerFunction(digest);
       String name = digest.getName();
       assertTrue(TestUtil.isFunctionRegisteredForWorker(conn, id, name));
       w.unregisterFunction(digest);
       assertFalse(TestUtil.isFunctionRegisteredForWorker(conn, id, name));
     } catch (Exception e) {
       e.printStackTrace();
     } finally {
       for (Exception e : w.close()) {
         e.printStackTrace();
       }
     }
   }
 
   /**
    * Note: This crashes gearmand 0.5 if run twice. The crash occurs when digest
    * is registered. So, ignoring this test for now.
    */
   @Test
   @Ignore
   public void testUnregisterAll() {
     Worker w = new SimpleWorker();
     Connection conn = new SocketConnection();
     JobFunction reverse = new ReverseFunction();
     String revName = reverse.getName();
     JobFunction digest = new DigestFunction();
     String digName = digest.getName();
     String id = "testUnregisterAll";
 
     try {
       w.addServer(conn);
       w.setWorkerID(id);
       w.registerFunction(reverse);
       w.registerFunction(digest);
       assertTrue(TestUtil.isFunctionRegisteredForWorker(conn, id, revName));
       assertTrue(TestUtil.isFunctionRegisteredForWorker(conn, id, digName));
       w.unregisterAll();
       assertFalse(TestUtil.isFunctionRegisteredForWorker(conn, id, revName));
       assertFalse(TestUtil.isFunctionRegisteredForWorker(conn, id, digName));
     } catch (Exception e) {
       e.printStackTrace();
     } finally {
       for (Exception e : w.close()) {
         e.printStackTrace();
       }
     }
   }
 
 }
