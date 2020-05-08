 // Copyright (C) 2012 Carles Muiños
 //
 // This file is part of TSAEnode.
 //
 // TSAEnode is free software: you can redistribute it and/or modify
 // it under the terms of the GNU General Public License as published by
 // the Free Software Foundation, either version 3 of the License, or
 // (at your option) any later version.
 //
 // TSAEnode is distributed in the hope that it will be useful,
 // but WITHOUT ANY WARRANTY; without even the implied warranty of
 // MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 // GNU General Public License for more details.
 //
 // You should have received a copy of the GNU General Public License
 // along with TSAEnode.  If not, see <http://www.gnu.org/licenses/>.
 
 package org.coderebels.tsaenode.core;
 
 import java.io.File;
 import java.net.InetAddress;
 import java.net.UnknownHostException;
 import java.rmi.Naming;
 import java.rmi.Remote;
 import java.rmi.RemoteException;
 import java.rmi.server.UnicastRemoteObject;
 import java.util.Timer;
 import java.util.TimerTask;
 import java.util.List;
 import java.util.concurrent.ConcurrentHashMap;
 
 import com.typesafe.config.Config;
 import com.typesafe.config.ConfigFactory;
 import com.typesafe.config.ConfigException;
 
 import org.apache.logging.log4j.Logger;
 import org.apache.logging.log4j.LogManager;
 
 import org.coderebels.tsaenode.core.common.Timestamp;
 import org.coderebels.tsaenode.core.file.FileData;
 import org.coderebels.tsaenode.core.file.IFileMgr;
 import org.coderebels.tsaenode.core.file.FileMgr;
 import org.coderebels.tsaenode.core.operation.Operation;
 import org.coderebels.tsaenode.core.operation.IOperationMgr;
 import org.coderebels.tsaenode.core.operation.OperationMgr;
 import org.coderebels.tsaenode.core.sync.Peer;
 import org.coderebels.tsaenode.core.sync.ISyncMgr;
 import org.coderebels.tsaenode.core.sync.TSAESyncMgr;
 
 
 /**
  * @author carles.ml.dev@gmail.com (Carles Muiños)
  */
 public class TSAEnode extends UnicastRemoteObject implements INode {
 
   private static Logger logger = LogManager.getLogger( TSAEnode.class.getName() );
 
   private final Object lock = new Object();
 
   private Timer scheduler;
 
   private String nodeId;
   private String nodeIP;
   private String nodePort;
   private String rmiPort;
   private String pubFolderPath;
   private long syncFreq;
   private boolean connected;
 
   private IFileMgr fileMgr;
   private IOperationMgr operationMgr;
   private ISyncMgr syncMgr;
 
 
   public TSAEnode() throws RemoteException, UnknownHostException, ConfigException {
     super();
 
     Config conf = ConfigFactory.load();
 
     nodeIP   = InetAddress.getLocalHost().getHostAddress();
     nodePort = conf.getString( "nodePort" );
     nodeId   = String.format( "%s:%s", nodeIP, nodePort );
     rmiPort  = conf.getString( "rmiPort" );
     pubFolderPath = conf.getString( "pubFolderPath" );
     syncFreq = conf.getMilliseconds( "syncFreq" );
     connected = false;
 
     String pubFolderURI  = String.format( "http://%s/public/%s", nodeIP, nodePort );
 
     System.setProperty( "nodeId", nodeId );
     System.setProperty( "nodeIP", nodeIP );
     System.setProperty( "pubFolderPath", pubFolderPath );
     System.setProperty( "pubFolderURI", pubFolderURI );
 
     fileMgr = new FileMgr();
     operationMgr = new OperationMgr( fileMgr );
     syncMgr = new TSAESyncMgr( operationMgr );
   }
 
 
   /* (non-Javadoc)
    * @see org.coderebels.tsaenode.core.INode#connect()
    */
   public boolean connect() throws RemoteException {
     logger.entry();
     logger.info( "Connecting..." );
 
    boolean done = joinGroup();
    done = done && setUp();
    done = done && scheduleSyncSession();
 
    if (done) connected = true;
 
     return logger.exit( done );
   }
 
   /* (non-Javadoc)
    * @see org.coderebels.tsaenode.core.INode#disconnect()
    */
   public boolean disconnect() throws RemoteException {
     logger.entry();
     logger.info( "Disconnecting..." );
 
    boolean done = cancelSyncSession();
    done = done && leaveGroup();
 
    if (done) connected = false;
 
     return logger.exit( done );
   }
 
   /* (non-Javadoc)
    * @see org.coderebels.tsaenode.core.INode#isConnected()
    */
   public boolean isConnected() throws RemoteException {
     return connected;
   }
 
   /* (non-Javadoc)
    * @see org.coderebels.tsaenode.core.INode#requestProfile()
    */
   public Peer requestProfile() throws RemoteException {
     logger.entry();
     logger.info( "Serving request for node profile..." );
 
     Peer profile = new Peer();
     profile.setId( nodeId );
     profile.setIP( nodeIP );
     profile.setPort( Integer.parseInt(nodePort) );
     profile.setRmiPort( Integer.parseInt(rmiPort) );
 
     return logger.exit( profile );
   }
 
   /* (non-Javadoc)
    * @see org.coderebels.tsaenode.core.INode#add(java.lang.String)
    */
   @Override
   public boolean add(String file) throws RemoteException {
     logger.entry( file );
     logger.info( "Serving request for file addition..." );
 
     boolean done = applyOperation( Operation.ADD, file );
 
     return logger.exit( done );
   }
 
   /* (non-Javadoc)
    * @see org.coderebels.tsaenode.core.INode#remove(java.lang.String)
    */
   @Override
   public boolean remove(String file) throws RemoteException {
     logger.entry( file );
     logger.info( "Serving request for file deletion..." );
 
     boolean done = applyOperation( Operation.REMOVE, file );
 
     return logger.exit( done );
   }
 
   /* (non-Javadoc)
    * @see org.coderebels.tsaenode.core.INode#requestFileIndex()
    */
   @Override
   public List<FileData> requestFileIndex() throws RemoteException {
     logger.entry();
     logger.info( "Serving request for file index..." );
     /*
      * 1) Delegate call to FileMgr through its interface --> IFileMgr#getFileIndex
      */
     List<FileData> index = fileMgr.getFileIndex();
 
     return logger.exit( index );
   }
 
   /* (non-Javadoc)
    * @see org.coderebels.tsaenode.core.INode#requestLog()
    */
   @Override
   public List<Operation> requestLog() throws RemoteException {
     logger.entry();
     logger.info( "Serving request for operation log..." );
     /*
      * 1) Delegate call to OperationMgr through its interface --> IOperationMgr#getLog
      */
     List<Operation> ops = operationMgr.getLog();
 
     return logger.exit( ops );
   }
 
   /* (non-Javadoc)
    * @see org.coderebels.tsaenode.core.INode#requestSummary()
    */
   @Override
   public ConcurrentHashMap<String, Timestamp> requestSummary() throws RemoteException {
     logger.entry();
     logger.info( "Serving request for summary vector..." );
     /*
      * 1) Delegate call to OperationMgr through its interface --> IOperationMgr#getSummary
      */
     ConcurrentHashMap<String, Timestamp> summary = operationMgr.getSummary();
 
     return logger.exit( summary );
   }
 
   /* (non-Javadoc)
    * @see org.coderebels.tsaenode.core.INode#requestAckSummary()
    */
   @Override
   public ConcurrentHashMap<String, ConcurrentHashMap<String, Timestamp>> requestAckSummary()
     throws RemoteException {
     logger.entry();
     logger.info( "Serving request for acknowledgement vector..." );
     /*
      * 1) Delegate call to OperationMgr through its interface --> IOperationMgr#getAcks
      */
     ConcurrentHashMap<String, ConcurrentHashMap<String, Timestamp>> ackSummary = operationMgr.getAcks();
 
     return logger.exit( ackSummary );
   }
 
   /* (non-Javadoc)
    * @see org.coderebels.tsaenode.core.INode#startTSAESession()
    */
   public boolean startTSAESession() throws RemoteException {
     logger.entry();
     logger.info( "Serving request for new synchronization session..." );
     /*
      * 1) Delegate call to TSAESyncMgr through its interface --> ISyncMgr#startSession
      */
     boolean done = false;
 
     try {
       done = syncMgr.startSession();
     } catch(Exception e) {
       logger.catching( e );
     }
 
     return logger.exit( done );
   }
 
   /* (non-Javadoc)
    * @see org.coderebels.tsaenode.core.INode#performTSAESession(java.util.List, java.util.concurrent.ConcurrentHashMap, java.util.concurrent.ConcurrentHashMap)
    */
   @Override
   public List<Operation> performTSAESession(List<Operation> ops,
                         ConcurrentHashMap<String, Timestamp> summary,
                         ConcurrentHashMap<String, ConcurrentHashMap<String, Timestamp>> acks)
     throws RemoteException {
     logger.entry( ops, summary, acks );
     logger.info( "Serving request for synchronization..." );
     /*
      * 1) Delegate call to TSAESyncMgr through its interface --> ISyncMgr#performSession
      */
     List<Operation> opsToSend = null;
 
     try {
       opsToSend = syncMgr.performSession( ops, summary, acks );
     } catch (Exception e) {
       logger.catching( e );
     }
 
     return logger.exit( opsToSend );
   }
 
 
   /**
    * Joins the group of nodes
    * @return true if done successfully; false otherwise
    */
   private boolean joinGroup() {
     logger.entry();
 
     boolean done = true;
 
     try {
       Naming.rebind( String.format("//%s:%s/node%s", nodeIP, rmiPort, nodePort), this );
     } catch (Exception e) {
       logger.catching( e );
       done = false;
     }
 
     return logger.exit( done );
   }
 
   /**
    * Leaves the group of nodes
    * @return true if done successfully; false otherwise
    */
   private boolean leaveGroup() {
     logger.entry();
 
     boolean done = true;
 
     try {
       Naming.unbind( String.format("//%s:%s/node%s", nodeIP, rmiPort, nodePort) );
     } catch (Exception e) {
       logger.catching( e );
       done = false;
     }
 
     return logger.exit( done );
   }
 
   /**
    * Sets up node data structures
    * @return true if done successfully; false otherwise
    */
   private boolean setUp() {
     logger.entry();
     logger.debug( "Setting up node..." );
 
     boolean done = true;
 
     try {
       File pubFolder = new File( pubFolderPath );
       if(!pubFolder.exists()) pubFolder.mkdirs();
 
       for(File file : pubFolder.listFiles()) {
         done = done && add( file.getAbsolutePath() );
       }
     } catch (Exception e) {
       logger.catching( e );
       done = false;
     }
 
     return logger.exit( done );
   }
 
   /**
    * Schedules synchronization sessions
    * @return true if done successfully; false otherwise
    */
   private boolean scheduleSyncSession() {
     logger.entry();
     logger.debug( "Scheduling synchronization session..." );
 
     boolean done = true;
 
     try {
       TSAEsession syncSession = new TSAEsession( this );
       long syncDelay = 0;
 
       scheduler = new Timer();
       scheduler.scheduleAtFixedRate( syncSession, syncDelay, syncFreq );
     } catch (Exception e) {
       logger.catching( e );
       done = false;
     }
 
     return logger.exit( done );
   }
 
   /**
    * Cancels synchronization sessions
    * @return true if done successfully; false otherwise
    */
   private boolean cancelSyncSession() {
     logger.entry();
     logger.debug( "Cancelling synchronization session..." );
 
     scheduler.cancel();
 
     return logger.exit( true );
   }
 
   /**
    * Applies operation of specified type to supplied file
    * @param type Type of operation
    * @param file File the operation will apply to
    * @return true if done successfully; false otherwise
    */
   private boolean applyOperation(int type, String file) {
     logger.entry( type, file );
     logger.debug( "Applying operation..." );
     /*
      * 1) Create a new operation
      * 2) Execute operation --> IOperationMgr#executeOperation
      * 3) Start synchronization session --> ISyncMgr#startSession
      * 4) Return true if done successfully
      */
     boolean done = false;
 
     try {
       synchronized (lock) {
         Operation op = operationMgr.createOperation( type, file );
         done = operationMgr.executeOperation( op );
       }
 
       done = done && syncMgr.startSession();
     } catch (Exception e) {
       logger.catching( e );
       done = false;
     }
 
     return logger.exit( done );
   }
 
 
   private class TSAEsession extends TimerTask {
 
     private INode node;
 
     public TSAEsession(INode node) {
       super();
       this.node = node;
     }
 
     /* (non-Javadoc)
     * @see java.util.TimerTask#run()
     */
     @Override
     public void run() {
       try {
         node.startTSAESession();
       } catch (Exception e) {}
     }
 
   }
 
 }
