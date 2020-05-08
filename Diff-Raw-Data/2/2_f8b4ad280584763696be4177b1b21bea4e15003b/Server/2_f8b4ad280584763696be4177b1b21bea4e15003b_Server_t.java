 package edu.ch.unifr.diuf.testing_tool;
 
 /**
  *
  * @author Teodor Macicas
  */
 public class Server extends Machine
 {    
      // this is also used by the clients to know where to connect to
      private String serverListenIp;
      private int serverPort;
      private String serverReadCons; 
      private String serverWriteCons; 
      private String serverTransLockGran; 
      private int serverReplicationFactor; 
      
      // each test will use one graph
      private String serverSourceGraph;
      private String serverDestGraph;
      private int serverResetGraph;
      private int serverSnapshotGraph;
      
      // if set to 'yes', in case of failure restartAttempts is tried 
      private String faultTolerant;
      // number of max retrials in case of failure
      private int restartAttempts;
      private int useZookeeper;
      
      private String testName;
     
      public Server() {
         this.serverListenIp = "0.0.0.0";
         this.serverPort = 8088;
      }
      
      public Server(String ipAddress, int port, String sshUsername) 
             throws WrongIpAddressException, WrongPortNumberException {
         super();
         this.setIpAddress(ipAddress);
         this.setPort(port);
         this.setSSHUsername(sshUsername);
         this.serverListenIp = "0.0.0.0";
         this.serverPort = 8088;
      }
      
      /**
       * 
       * @param ipAddress
       * @throws WrongIpAddressException 
       */
      public void setServerHTTPListenAddress(String ipAddress) 
              throws WrongIpAddressException { 
          if( Utils.validateIpAddress(ipAddress) )
                this.serverListenIp = ipAddress;
         else
             throw new WrongIpAddressException(ipAddress + " server listen IP address"
                     + " cannot be set due to validation errors");
      }
      
      /**
       * 
       * @return 
       */
      public String getServerHTTPListenAddress() { 
          return this.serverListenIp;
      }
      
      /**
       * 
       * @param httpPort
       * @throws WrongPortNumberException 
       */
     public void setServerHttpPort(int httpPort) throws WrongPortNumberException { 
         if( ! Utils.validateRemotePort(httpPort) )
             throw new WrongPortNumberException("Server http port number is "
                     + "not valid.");
         this.serverPort = httpPort;
     }
      
     /**
      * 
      * @return 
      */
     public int getServerHttpPort() { 
         return this.serverPort;
     }
     
     /**
      * 
      * @param faultTolerant 
      */
     public void setFaultTolerant(String faultTolerant) { 
         if( faultTolerant.length() == 0 || ! faultTolerant.equals("yes")) 
             this.faultTolerant = "no";
         else
             this.faultTolerant = "yes";
     }
     
     /**
      * 
      * @return 
      */
     public String getFaultTolerant() { 
         return this.faultTolerant;
     }
     
     /**
      * 
      * @param restartAttempts 
      */
     public void setRestartAttempts(int restartAttempts) { 
         this.restartAttempts = restartAttempts;
     }
     
     /**
      * 
      * @return 
      */
     public int getRestartAttempts() { 
         return this.restartAttempts;
     }
     
     /**
      * 
      * @param graph 
      */
     public void setSourceGraph(String graph) { 
         this.serverSourceGraph = graph;
     }
     
     /**
      * 
      * @return 
      */
     public String getSourceGraph() { 
         return this.serverSourceGraph;
     }
    
     /**
      * 
      * @param graph 
      */
     public void setDestGraph(String graph) { 
         this.serverDestGraph = graph;
     }
     
     /**
      * 
      * @return 
      */
     public String getDestGraph() { 
         return this.serverDestGraph;
     }
     
     /**
      * 
      * @param reset 
      */
     public void setGraphReset(int reset) { 
         this.serverResetGraph = reset;
     }
     
     /**
      * 
      * @return 
      */
     public int getGraphReset() { 
         return this.serverResetGraph;
     }
 
     /**
      * 
      * @param readCons 
      */
     public void setReadCons(String readCons) { 
         this.serverReadCons = readCons;
     }
     
     /**
      * 
      * @return 
      */
     public String getReadCons() { 
         return this.serverReadCons;
     }
     
     /**
      * 
      * @param writeCons 
      */
     public void setWriteCons(String writeCons) { 
         this.serverWriteCons = writeCons;
     }
     
     /**
      * 
      * @return 
      */
     public String getWriteCons() { 
         return this.serverWriteCons;
     }
     
     /**
      * 
      * @param transLockGran 
      */
     public void setTransLockGran(String transLockGran) { 
         this.serverTransLockGran = transLockGran;
     }
     
     /**
      * 
      * @return 
      */
     public String getTransLockGran() { 
         return this.serverTransLockGran;
     }
     
     /**
      * 
      * @param replFactor
      */
     public void setReplFactor(int replFactor) { 
         this.serverReplicationFactor = replFactor;
     }
     
     /**
      * 
      * @return 
      */
     public int getReplFactor() { 
         return this.serverReplicationFactor;
     }
     
     /**
      * 
      * @param snapshot 
      */
     public void setGraphSnapshot(int snapshot) { 
         this.serverSnapshotGraph = snapshot;
     }
     
     /**
      * 
      * @return 
      */
     public int getGraphSnapshot() { 
         return this.serverSnapshotGraph;
     }
     
     /**
      * 
      * @param use 
      */
     public void setUseZookeeper(int use) { 
         this.useZookeeper = use;
     }
     
     /**
      * 
      * @return 
      */
     public int getUseZookeeper() { 
         return this.useZookeeper;
     }
     
     /**
      * 
      * @param testName 
      */
     public void setTestName(String testName) { 
         this.testName = testName;
     }
     
     /**
      * 
      * @return 
      */
     public String getTestName() { 
        if( testName.contains("/") ) 
            return this.testName.substring(testName.indexOf("/")+1);
         return this.testName;
     }
 }
