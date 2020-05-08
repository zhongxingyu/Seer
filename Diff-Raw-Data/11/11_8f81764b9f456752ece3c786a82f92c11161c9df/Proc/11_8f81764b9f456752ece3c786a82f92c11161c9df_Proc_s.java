 package membership;
 
 
 import communication.*;
 import filesystem.ReplicaManager;
 import filesystem.SDFS;
 import misc.TimeMachine;
 import org.apache.log4j.Logger;
 
 import java.net.InetAddress;
 import java.net.UnknownHostException;
 import java.util.UUID;
 
 import static communication.Messages.*;
 
 public class Proc {
     private Integer tcpPort;
     private Integer udpPort;
     private Integer fileServerPort;
 
     private TCPServer tcpServer;
     private UDPServer udpServer;
     private TCPFileServer fileServer;
 
     private Boolean isTCPServerStarted;
     private Boolean isUDPServerStarted;
     private Boolean isFileServerStarted;
 
     private Gossip gossip;
     private FailureDetector failureDetector;
     private ScanningThread scanningThread;
 
     private SDFS SDFileSystem;
 
     private ReplicaManager replicaManager;
 
     private Logger logger = Logger.getLogger(Proc.class);
     private Integer timeStamp;
     private Integer localTime;
     private String id;
     private ProcessIdentifier identifier;
     private String hostAddress;
     private MemberList memberList;
 
     public Proc(Integer tcpPort) {
         this.timeStamp = 0;
         this.id = UUID.randomUUID().toString();
         this.tcpPort = tcpPort;
         this.udpPort = tcpPort + 1; // UDPPort is always set to TCPPort+1
         this.fileServerPort = tcpPort + 2;
         memberList = new MemberList();
 
         this.isTCPServerStarted = this.isUDPServerStarted = this.isFileServerStarted = false ;
     }
 
     private void initIdentifier() {
         try {
             InetAddress addr = InetAddress.getLocalHost();
             hostAddress = addr.getHostAddress();
             identifier = ProcessIdentifierFactory.generateProcessIdentifier(id, hostAddress, tcpPort, timeStamp);
             System.out.println(identifier.toString());
         } catch (UnknownHostException e) {
             logger.fatal("Unknown local host", e);
             System.exit(-1);
         }
 
     }
 
 
     public void init() {
         initIdentifier();
         addProcToMemberList(identifier);
 
         //TODO: before init new serve, old server should be closed.
         //init server
         initTCPServer();
         initUDPServer();
         initFileServer();
 
         //init gossip
         initGossip();
 
         //init failure detector
 //        initFailureDetector();
         initMemberListScanningThread();
 
         initSDFS();
 
         //init timeMachine
         TimeMachine.init();
     }
 
     public void stop() {
         tcpServer.stop();
         udpServer.stop();
         fileServer.stop();
         gossip.stop();
         scanningThread.stop();
     }
 
     private void initGossip() {
         gossip = new Gossip();
         gossip.setProc(this);
 
         gossip.start();
     }
 
     private void initFailureDetector() {
         failureDetector = new FailureDetector();
         failureDetector.setProc(this);
 
         failureDetector.start();
     }
 
     private void initMemberListScanningThread() {
         scanningThread = new ScanningThread();
         scanningThread.setProc(this);
         scanningThread.startScan();
     }
 
     private void initReplicaManger(){
         replicaManager = new ReplicaManager();
         replicaManager.setProc(this);
         replicaManager.start();
     }
 
     public void initTCPServer() {
         tcpServer = new TCPServer(tcpPort);
         tcpServer.setProc(this);
 
         if(tcpServer.start()) {
             isTCPServerStarted = true;
             logger.info("TCP Server starts successfully, listening to port " + tcpPort);
         } else {
             System.err.println("TCP Server starts failed, please check configuration");
             logger.fatal("TCP Server starts failed");
         }
     }
 
     public void initUDPServer() {
         udpServer = new UDPServer(udpPort);
         udpServer.setProc(this);
 
         if(udpServer.start()) {
             isUDPServerStarted = true;
             logger.info("UDP Server starts successfully, listening to port " + udpPort);
         } else {
             System.err.println("UDP Server starts failed, please check configuration");
             logger.fatal("UDP Server starts failed");
         }
     }
 
     private void initFileServer() {
         fileServer = new TCPFileServer(fileServerPort);
         fileServer.setProc(this);
 
         if(fileServer.start()) {
             isFileServerStarted = true;
             logger.info("TCP File Server starts successfully, listening to port " + fileServerPort);
         } else {
             System.err.println("TCP File Server starts failed, please check configuration");
             logger.fatal("TCP Server starts failed");
         }
     }
 
     private void initSDFS() {
         SDFileSystem = new SDFS();
         SDFileSystem.init();
         SDFileSystem.setProc(this);
     }
 
     public ProcessIdentifier getIdentifier() {
         return ProcessIdentifierFactory.generateProcessIdentifier(id, hostAddress, tcpPort, timeStamp);
     }
 
     public String getId() {
         return id;
     }
 
     public MemberList getMemberList() {
         return memberList;
     }
 
     public void addProcToMemberList(ProcessIdentifier processIdentifier) {
         memberList.add(processIdentifier);
     }
 
     public Integer getTimeStamp() {
         synchronized (this) {
             return timeStamp;
         }
     }
 
     public Integer increaseAndGetTimeStamp() {
         synchronized (this) {
             return ++timeStamp;
         }
     }
 
     public Integer getTcpPort() {
         return tcpPort;
     }
 
     public FailureDetector getFailureDetector() {
         return failureDetector;
     }
 
     public Proc setMemberList(MemberList memberList) {
         this.memberList = memberList;
         return this;
     }
 
     public TCPFileServer getFileServer() {
         return fileServer;
     }
 
     public SDFS getSDFS() {
         return SDFileSystem;
     }
 }
