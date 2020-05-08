 package org.nebulostore.systest;
 
 import org.apache.log4j.Logger;
 import org.nebulostore.appcore.Peer;
 import org.nebulostore.appcore.exceptions.NebuloException;
 import org.nebulostore.conductor.ConductorServer;
 import org.nebulostore.dispatcher.messages.KillDispatcherMessage;
 
 /**
  * Class to run test server.
  * @author bolek
  * @author szymonmatejczyk
  */
 public class TestingPeer extends Peer {
   private static Logger logger_ = Logger.getLogger(TestingPeer.class);
   private static final String CLASS_LIST_CONFIG = "systest.testing-peer-class-list";
 
   @Override
   protected void runPeer() {
     logger_.info("Starting testing peer with appKey = " + appKey_);
     startPeer();
     putKey();
 
     String[] testClasses = config_.getStringArray(CLASS_LIST_CONFIG);
     logger_.info("Running " + testClasses.length + " tests.");
     for (String className : testClasses) {
       ConductorServer testServer = null;
       try {
         testServer = (ConductorServer) Class.forName(className).newInstance();
         logger_.info("Starting " + className + " test.");
         if (runTest(testServer, className)) {
           logger_.info("Test " + className + " succeeded!");
         } else {
           fatal("Test " + className + " failed!");
         }
       } catch (InstantiationException e) {
         fatal("Could not instantiate class " + className + ".");
       } catch (IllegalAccessException e) {
         fatal("Constructor for class " + className + " is not accessible.");
       } catch (ClassNotFoundException e) {
         fatal("Class " + className + " not found.");
       }
     }
     logger_.info("All tests finished successfully.");
    dispatcherInQueue_.add(new KillDispatcherMessage());
     finishPeer();
     System.exit(0);
   }
 
   private void fatal(String message) {
     logger_.fatal(message);
     System.exit(1);
   }
 
   private boolean runTest(ConductorServer testModule, String testName) {
     try {
       testModule.runThroughDispatcher(dispatcherInQueue_);
       testModule.getResult();
       return true;
     } catch (NebuloException exception) {
       logger_.error("NebuloException at test " + testName + " : " + exception.getMessage());
       return false;
     }
   }
 
   // TODO(bolek,szymonmatejczyk): Move these to a separate ConductorServer class.
   /*private void messagesPerfTests() {
     int count = 5;
     int maxPeers = 91;
     int minPeers = 5;
     int stepPeers = 10;
 
     int minEpoches = 10;
     int maxEpoches = 11;
     int stepEpoches = 2;
 
     int minMessagesInPhase = 1;
     int maxMessagesInPhase = 6;
     int stepMessagesInPhase = 1;
 
     // warmup
     int j = 0;
     int successful = 0;
     int epochesWarmup = 3;
     int messagesInPhaseWarmup = 5;
 
     for (int epoches = minEpoches; epoches < maxEpoches; epoches += stepEpoches) {
       for (int peers = minPeers; peers < maxPeers; peers += stepPeers) {
 
         successful = 0;
         while (successful < count) {
           String messagesDesc = "PerfMsg test [" + epochesWarmup + "\t" +
               peers + "\t" + messagesInPhaseWarmup + "] WARM " + "c: " + j;
           int toAdd = runTest(new PerformanceMessagesTestServer(peers + 10,
               peers, 380, messagesDesc, 5, 10, 1000), "MsgSrv " + messagesDesc +
               "\t" + j) ? 1 : 0;
 
           successful += toAdd;
           if (toAdd == 0) {
             logger_.info("Additionally sleeping after failed test");
             try {
               Thread.sleep(peers * 1000);
             } catch (InterruptedException e) {
               e.printStackTrace();
             }
           }
           logger_
           .info("Finished warmup Messages test. Moving to the next test.");
 
           try {
             Thread.sleep(peers * 1000);
           } catch (InterruptedException e) {
             e.printStackTrace();
           }
 
           j += 1;
         }
       }
     }
   }
 
   private void messagesTests() {
     int count = 5;
     int maxPeers = 91;
     int minPeers = 35;
     int stepPeers = 10;
 
     int minEpoches = 10;
     int maxEpoches = 11;
     int stepEpoches = 2;
 
     int minMessagesInPhase = 1;
     int maxMessagesInPhase = 6;
     int stepMessagesInPhase = 1;
 
     // warmup
     int j = 0;
     int successful = 0;
     int epochesWarmup = 3;
     int messagesInPhaseWarmup = 5;
 
     for (int epoches = minEpoches; epoches < maxEpoches; epoches += stepEpoches) {
       for (int peers = minPeers; peers < maxPeers; peers += stepPeers) {
 
         successful = 0;
         while (successful < count) {
           String messagesDesc = "Messages test [" + epochesWarmup + "\t" +
               peers + "\t" + messagesInPhaseWarmup + "] WARM " + "c: " + j;
           int toAdd = runTest(new MessagesTestServer(epochesWarmup, peers + 10,
               peers, 380, messagesDesc, messagesInPhaseWarmup), "MsgSrv " +
                   messagesDesc + "\t" + j) ? 1 : 0;
 
           successful += toAdd;
           if (toAdd == 0) {
             logger_.info("Additionally sleeping after failed test");
             try {
               Thread.sleep(peers * 10 * 1000);
             } catch (InterruptedException e) {
               e.printStackTrace();
             }
           }
           logger_
           .info("Finished warmup Messages test. Moving to the next test.");
 
           try {
             Thread.sleep(peers * 5 * 1000);
           } catch (InterruptedException e) {
             e.printStackTrace();
           }
 
           j += 1;
         }
 
         for (int messagesInPhase = minMessagesInPhase; messagesInPhase < maxMessagesInPhase;
             messagesInPhase += stepMessagesInPhase) {
 
           int i = 0;
           int succ = 0;
           while (succ < count) {
             String messagesDesc = "Msg [" + epoches + "\t" + peers + "\t" +
                 messagesInPhase + "] - " + "c: " + i;
             int toAdd = runTest(new MessagesTestServer(epoches, peers + 15,
                 peers, (peers * messagesInPhase) + epoches * 25, messagesDesc,
                 messagesInPhase), "MsgSrvr " + messagesDesc + "\t" + i) ? 1 : 0;
             logger_.info("Finished Messages test. Moving to the next test.");
 
             succ += toAdd;
 
             if (toAdd == 0) {
               logger_.info("Additionally sleeping after failed test");
               try {
                 Thread.sleep(peers * 10 * 1000);
               } catch (InterruptedException e) {
                 e.printStackTrace();
               }
             }
 
             try {
               Thread.sleep(peers * 5 * 1000);
             } catch (InterruptedException e) {
               e.printStackTrace();
             }
 
             i += 1;
           }
           try {
             Thread.sleep(peers * 10 * 1000);
           } catch (InterruptedException e) {
             e.printStackTrace();
           }
         }
       }
 
       try {
         Thread.sleep(600 * 1000);
       } catch (InterruptedException e) {
         e.printStackTrace();
       }
     }
 
   }
 
   private void dhtTests() {
     int count = 5;
 
     int maxPeers = 81;
     int minPeers = 20;
     int stepPeers = 5;
 
     int minEpoches = 7;
     int maxEpoches = 8;
     int stepEpoches = 2;
 
     int minKeysMultiplier = 1;
     int maxKeysMultiplier = 6;
     int stepKeysMultiplier = 1;
 
     for (int epoches = minEpoches; epoches < maxEpoches; epoches += stepEpoches) {
 
       for (int peers = minPeers; peers < maxPeers; peers += stepPeers) {
 
         int keysMultiplierWarmup = 3;
         for (int i = 0; i < 3; i++) {
           String kadDesc = "Kademlia test [" + epoches + "\t" + peers + "\t" +
               keysMultiplierWarmup + "] - WARM count: " + i;
           int toAdd = runTest(new KademliaDHTTestServer(epoches, peers + 10,
               peers, keysMultiplierWarmup, kadDesc), "KademliaDHTTestServer " +
                   kadDesc + "\t" + i) ? 1 : 0;
         }
 
         logger_.info("Finished Bdb DHT Test, performing Kademlia DHT Test...");
         for (int keysMultiplier = minKeysMultiplier; keysMultiplier < maxKeysMultiplier;
         keysMultiplier += stepKeysMultiplier) {
           int i = 0;
           int succ = 0;
           while (succ < count) {
             String kadDesc = "Kademlia test [" + epoches + "\t" + peers + "\t" +
                 keysMultiplier + "] - count: " + i;
             int toAdd = runTest(new KademliaDHTTestServer(epoches, peers + 10,
                 peers, keysMultiplier, kadDesc), "KademliaDHTTestServer " +
                     kadDesc + "\t" + i) ? 1 : 0;
             succ += toAdd;
             if (toAdd == 0) {
               logger_.info("Additionally sleeping after failed test");
               try {
                 Thread.sleep(peers * 3 * 1000);
               } catch (InterruptedException e) {
                 e.printStackTrace();
               }
             }
 
             try {
               Thread.sleep(peers * 1000);
             } catch (InterruptedException e) {
               e.printStackTrace();
             }
             i += 1;
           }
         }
 
       }
 
       for (int peers = minPeers; peers < maxPeers; peers += stepPeers) {
         int keysMultiplierWarmup = 3;
         for (int i = 0; i < 3; i++) {
           String kadDesc = "Bdb test [" + epoches + "\t" + peers + "\t" +
               keysMultiplierWarmup + "] - WARM count: " + i;
           int toAdd = runTest(new BdbDHTTestServer(epoches, peers + 10, peers,
               keysMultiplierWarmup, kadDesc), "BdbDHTTestServer " + kadDesc +
               "\t" + i) ? 1 : 0;
         }
 
         for (int keysMultiplier = minKeysMultiplier; keysMultiplier < maxKeysMultiplier;
             keysMultiplier += stepKeysMultiplier) {
           int i = 0;
           int succ = 0;
           while (succ < count) {
             String bdbDesc = "Bdb test [" + epoches + "\t" + peers + "\t" +
                 keysMultiplier + "] - count: " + i;
             int toAdd = runTest(new BdbDHTTestServer(epoches, peers + 10,
                 peers, keysMultiplier, bdbDesc), "BdbDHTTestServer " + bdbDesc +
                 "\t" + i) ? 1 : 0;
             i += 1;
 
             succ += toAdd;
 
             if (toAdd == 0) {
               logger_.info("Additionally sleeping after failed test");
               try {
                 Thread.sleep(peers * 1000);
               } catch (InterruptedException e) {
                 e.printStackTrace();
               }
             }
 
             try {
               Thread.sleep(peers * 1000);
             } catch (InterruptedException e) {
               e.printStackTrace();
             }
 
           }
         }
       }
     }
   }*/
 }
