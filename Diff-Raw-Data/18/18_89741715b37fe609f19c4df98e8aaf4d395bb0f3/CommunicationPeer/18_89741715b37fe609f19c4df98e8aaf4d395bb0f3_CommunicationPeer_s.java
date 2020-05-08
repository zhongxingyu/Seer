 package org.nebulostore.communication;
 
 import java.io.IOException;
 import java.util.concurrent.BlockingQueue;
 import java.util.concurrent.LinkedBlockingQueue;
 
 import org.apache.commons.configuration.ConfigurationException;
 import org.apache.commons.configuration.ConversionException;
 import org.apache.commons.configuration.XMLConfiguration;
 import org.apache.log4j.Logger;
 import org.nebulostore.appcore.Message;
 import org.nebulostore.appcore.Module;
 import org.nebulostore.appcore.exceptions.NebuloException;
 import org.nebulostore.communication.address.CommAddress;
 import org.nebulostore.communication.bdbdht.BdbPeer;
 import org.nebulostore.communication.bootstrap.BootstrapClient;
 import org.nebulostore.communication.bootstrap.BootstrapServer;
 import org.nebulostore.communication.bootstrap.BootstrapService;
 import org.nebulostore.communication.gossip.PeerGossipService;
 import org.nebulostore.communication.messages.CommMessage;
 import org.nebulostore.communication.messages.CommPeerFoundMessage;
 import org.nebulostore.communication.messages.ErrorCommMessage;
 import org.nebulostore.communication.messages.ReconfigureDHTAckMessage;
 import org.nebulostore.communication.messages.ReconfigureDHTMessage;
 import org.nebulostore.communication.messages.bdbdht.BdbMessageWrapper;
 import org.nebulostore.communication.messages.bdbdht.HolderAdvertisementMessage;
 import org.nebulostore.communication.messages.dht.DHTMessage;
 import org.nebulostore.communication.messages.dht.InDHTMessage;
 import org.nebulostore.communication.messages.dht.OutDHTMessage;
 import org.nebulostore.communication.messages.gossip.PeerGossipMessage;
 import org.nebulostore.communication.socket.ListenerService;
 import org.nebulostore.communication.socket.MessengerService;
 
 //TODO(grzegorzmilka) add closing through message instead of interrupt
 //TODO(grzegorzmilka) apply Visitator pattern to message communication
 /**
  * Main module for communication with outside world.
  *
  * So far it only handles BDB.
  * @author Marcin Walas
  * @author Grzegorz Milka
  */
 public class CommunicationPeer extends Module {
   private static Logger logger_ = Logger.getLogger(CommunicationPeer.class);
   private static final String CONFIGURATION_PATH =
     "resources/conf/communication/CommunicationPeer.xml";
 
   /**
    * Module for handling bootstraping peer to network.
    *
    * It can be either server or client, depending on configuration
    */
   private static BootstrapService bootstrapService_;
 
   /**
    * DHT module available to higher layers.
    *
    * Note that it was implemented by Marcin and I(grzegorzmilka) left it mostly
    * as is. Only BDB works.
    */
   private Module dhtPeer_;
   private final BlockingQueue<Message> dhtPeerInQueue_;
   private Thread dhtPeerThread_;
 
   /**
    * Main module for listening for messages.
    *
    * It works listens for incoming TCP connections from messengerService
    */
   private ListenerService listenerService_;
   private Thread listenerThread_;
 
   /**
    * Main module for sending messages across the network.
    */
   private MessengerService messengerService_;
   private BlockingQueue<Message> messengerServiceInQueue_;
   private Thread messengerThread_;
 
   /**
    * Module handling peer gossiping.
    *
    * Once every x seconds gossiper gossips to get updated view on the network.
    */
   private BlockingQueue<Message> gossipServiceInQueue_;
   private PeerGossipService gossipService_;
   private Thread gossipThread_;
 
 
   private boolean isServer_;
   // Is the server shutting down.
   private Boolean isEnding_ = false;
 
   private static int commCliPort_;
 
   public CommunicationPeer(BlockingQueue<Message> inQueue,
       BlockingQueue<Message> outQueue) throws NebuloException {
     super(inQueue, outQueue);
     logger_.debug("Starting CommunicationPeer");
 
     XMLConfiguration config = null;
 
     try {
       config = new XMLConfiguration(CONFIGURATION_PATH);
     } catch (ConfigurationException cex) {
       logger_.error("Configuration read error in: " + CONFIGURATION_PATH);
       throw new NebuloException("Couldn't read configuration file at: " +
           CONFIGURATION_PATH, cex);
     }
 
     /* Load port numbers from config file */
     int bootstrapPort;
     int bootstrapTP2PPort;
     int tP2PPort;
 
     try {
       String commCliPortConf = "ports.comm-cli-port";
       String bPortConf = "ports.bootstrap-port";
       String bServTP2PPortConf = "ports.bootstrap-server-tomp2p-port";
       String tP2PPortConf = "ports.tomp2p-port";
 
       commCliPort_ = config.getInt(commCliPortConf, -1);
       bootstrapPort = config.getInt(bPortConf, -1);
       bootstrapTP2PPort = config.getInt(bServTP2PPortConf, -1);
       tP2PPort = config.getInt(tP2PPortConf, -1);
 
       if (commCliPort_ == -1 || bootstrapPort == -1 ||
           bootstrapTP2PPort == -1 || tP2PPort == -1) {
         logger_.error(
             "One of port numbers is not specified in configuration file.");
         throw new NebuloException(
             "One of port numbers is not specified in configuration file.");
       }
     } catch (ConversionException e) {
       logger_.error("One of port numbers is not correctly " +
           "specified in configuration file: " + e);
       throw new NebuloException("One of port numbers is not correctly " +
           "specified in configuration file.", e);
     }
 
     messengerServiceInQueue_ = new LinkedBlockingQueue<Message>();
     gossipServiceInQueue_ = new LinkedBlockingQueue<Message>();
     dhtPeerInQueue_ = new LinkedBlockingQueue<Message>();
 
     try {
       listenerService_ = new ListenerService(inQueue_, commCliPort_);
     } catch (IOException e) {
       throw new NebuloException("Couldn't initialize listener.", e);
     }
 
     isServer_ = config.getString("bootstrap.mode", "client").equals("server");
 
     String bootstrapServerAddress = config.getString("bootstrap.address", "none");
     if (bootstrapServerAddress.equals("none")) {
       throw new IllegalArgumentException("Bootstrap client address is not set.");
     }
     if (!isServer_) {
       bootstrapService_ = new BootstrapClient(bootstrapServerAddress,
           commCliPort_, bootstrapPort, tP2PPort, bootstrapTP2PPort);
       logger_.info("Created BootstrapClient.");
       inQueue_.add(new CommPeerFoundMessage(bootstrapService_.getBootstrapCommAddress(),
             bootstrapService_.getResolver().getMyCommAddress()));
     } else {
       bootstrapService_ = new BootstrapServer(bootstrapServerAddress,
               commCliPort_, bootstrapPort, bootstrapTP2PPort);
       try {
         bootstrapService_.startUpService();
       } catch (IOException e) {
         logger_.error("IOException: " + e +
             " caught when starting up BootstrapServer.");
         throw new NebuloException("IOException at bootstrap", e);
       }
       Thread bootstrap = new Thread((BootstrapServer) bootstrapService_,
           "Nebulostore.Communication.Bootstrap");
       bootstrap.setDaemon(true);
       bootstrap.start();
       logger_.info("Created BootstrapServer.");
     }
 
     messengerService_ = new MessengerService(messengerServiceInQueue_,
         inQueue_, bootstrapService_.getResolver());
 
     gossipService_ = new PeerGossipService(gossipServiceInQueue_, inQueue_,
         bootstrapService_.getResolver().getMyCommAddress(),
         bootstrapService_.getBootstrapCommAddress());
 
     listenerThread_ = new Thread(
         listenerService_, "Nebulostore.Communication.ListenerService");
     listenerThread_.setDaemon(true);
     listenerThread_.start();
     gossipThread_ =
       new Thread(gossipService_, "Nebulostore.Communication.GossipService");
     gossipThread_.setDaemon(true);
     gossipThread_.start();
     messengerThread_ = new Thread(
         messengerService_, "Nebulostore.Communication.MessengerService");
     messengerThread_.setDaemon(true);
     messengerThread_.start();
 
     logger_.info("Created and started auxiliary services.");
 
     if (!config.getString("dht.provider", "bdb").equals("none")) {
       reconfigureDHT(config.getString("dht.provider", "bdb"), null);
     } else {
       dhtPeer_ = null;
     }
   }
 
   //NOTE-GM Why was this made static? If CommunicationPeer is not singleton it
   //should be "unstaticed". Ask about it on nebulo mailing. (TODO)
   public static CommAddress getPeerAddress() {
     return bootstrapService_.getResolver().getMyCommAddress();
   }
 
   /**
    * Kills this peer with its submodules.
    *
    * During the shutting down any messages sent to this peer will generate warn
    * log message. Remember to call interrupt when after call to endModule.
    *
    * @author Grzegorz Milka
    */
   @Override
   public void endModule() {
     logger_.info("Starting endModule procedure of CommunicationPeer.");
     synchronized (isEnding_) {
       isEnding_ = true;
     }
     messengerService_.endModule();
     messengerThread_.interrupt();
     while (true) {
       try {
         messengerThread_.join();
         break;
       } catch (InterruptedException e) {
         logger_.warn("Caught InterruptedException when joining messengerThread.");
       }
     }
     logger_.info("MessengerThread ended.");
     listenerService_.endModule();
     listenerThread_.interrupt();
     while (true) {
       try {
         listenerThread_.join();
         break;
       } catch (InterruptedException e) {
         logger_.warn("Caught InterruptedException when joining listener.");
       }
     }
     logger_.info("ListenerThread ended.");
     gossipService_.endModule();
     gossipThread_.interrupt();
     while (true) {
       try {
         gossipThread_.join();
         break;
       } catch (InterruptedException e) {
         logger_.warn("Caught InterruptedException when joining gossiper.");
       }
     }
     logger_.info("GossipThread ended.");
     bootstrapService_.shutdownService();
     logger_.info("BootstrapService shutdown.");
     super.endModule();
   }
 
   @Override
   protected void processMessage(Message msg) {
     synchronized (isEnding_) {
       if (isEnding_) {
         logger_.warn("Can not process message, because commPeer is " +
             "shutting down.");
         return;
       }
       logger_.debug("Processing message: " + msg);
 
       if (msg instanceof ErrorCommMessage) {
         logger_.info("Error comm message. Returning it to Dispatcher");
         gossipServiceInQueue_.add(msg);
         outQueue_.add(msg);
       } else if (msg instanceof ReconfigureDHTMessage) {
         try {
           logger_.info("Got reconfigure request with jobId: " + msg.getId());
           reconfigureDHT(((ReconfigureDHTMessage) msg).getProvider(),
               (ReconfigureDHTMessage) msg);
         } catch (NebuloException e) {
           logger_.error(e);
         }
       } else if (msg instanceof HolderAdvertisementMessage) {
         dhtPeerInQueue_.add(msg);
       } else if (msg instanceof CommPeerFoundMessage) {
         logger_.debug("CommPeerFound message forwarded to Dispatcher");
         outQueue_.add(msg);
       } else if (msg instanceof DHTMessage) {
         if (msg instanceof InDHTMessage) {
           logger_.debug("InDHTMessage forwarded to DHT" + msg.getClass().toString());
           dhtPeerInQueue_.add(msg);
         } else if (msg instanceof OutDHTMessage) {
           logger_.debug("OutDHTMessage forwarded to Dispatcher" + msg.getClass().toString());
           outQueue_.add(msg);
         } else {
           logger_.error("Unrecognized DHTMessage: " + msg);
         }
       } else if (msg instanceof BdbMessageWrapper) {
         logger_.debug("BDB DHT message received");
         BdbMessageWrapper casted = (BdbMessageWrapper) msg;
         if (casted.getWrapped() instanceof InDHTMessage) {
           logger_.debug("BDB DHT message forwarded to DHT");
           dhtPeerInQueue_.add(casted.getWrapped());
         } else if (casted.getWrapped() instanceof OutDHTMessage) {
           logger_.debug("BDB DHT message forwarded to Dispatcher");
           outQueue_.add(casted);
         } else {
           logger_.error("Unrecognized BdbMessageWrapper: " + msg);
         }
       } else if (msg instanceof PeerGossipMessage) {
         if (((CommMessage) msg).getDestinationAddress().equals(
               bootstrapService_.getResolver().getMyCommAddress()))
           gossipServiceInQueue_.add(msg);
         else
           messengerServiceInQueue_.add(msg);
       } else if (msg instanceof CommMessage) {
         if (((CommMessage) msg).getSourceAddress() == null) {
           ((CommMessage) msg).setSourceAddress(getPeerAddress());
         }
 
         if (((CommMessage) msg).getDestinationAddress() == null) {
           logger_.error("Null destination address set for " + msg + ". Dropping the message.");
         } else if (((CommMessage) msg).getDestinationAddress().equals(
               bootstrapService_.getResolver().getMyCommAddress())) {
           logger_.debug("message forwarded to Dispatcher");
           outQueue_.add(msg);
         } else {
           logger_.debug("message forwarded to MessengerService");
           messengerServiceInQueue_.add(msg);
         }
       } else {
         logger_.warn("Unrecognized message of type " + msg);
       }
     }
   }
 
   /**
    * Starts up and configures DHTPeer.
    *
    * @author Marcin Walas
    */
   private void reconfigureDHT(String dhtProvider,
       ReconfigureDHTMessage reconfigureRequest) throws NebuloException {
 
     if (dhtProvider.equals("bdb") && (dhtPeer_ instanceof BdbPeer)) {
       if (reconfigureRequest != null && ((BdbPeer) dhtPeer_).getHolderAddress() != null) {
         outQueue_.add(new ReconfigureDHTAckMessage(reconfigureRequest));
       }
     } else {
       if (dhtPeerThread_ != null) {
         dhtPeer_.endModule();
         dhtPeerThread_.interrupt();
       }
 
       if (dhtProvider.equals("bdb")) {
         dhtPeer_ = new BdbPeer(dhtPeerInQueue_, outQueue_, getPeerAddress(),
             messengerServiceInQueue_, reconfigureRequest);
       } else {
         throw new NebuloException("Unsupported DHT Provider in configuration");
       }
       dhtPeerThread_ = new Thread(dhtPeer_, "Nebulostore.Communication.DHT");
       dhtPeerThread_.setDaemon(true);
       dhtPeerThread_.start();
     }
   }
 }
