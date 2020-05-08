 package com.freakz.hokan_ng.core.service;
 
 import com.freakz.hokan_ng.common.engine.AsyncConnector;
 import com.freakz.hokan_ng.common.engine.Connector;
 import com.freakz.hokan_ng.common.engine.EngineConnector;
 import com.freakz.hokan_ng.common.engine.HokanCore;
 import com.freakz.hokan_ng.common.entity.Channel;
 import com.freakz.hokan_ng.common.entity.ChannelState;
 import com.freakz.hokan_ng.common.entity.IrcServerConfig;
 import com.freakz.hokan_ng.common.entity.IrcServerConfigState;
 import com.freakz.hokan_ng.common.entity.Network;
 import com.freakz.hokan_ng.common.entity.PropertyName;
 import com.freakz.hokan_ng.common.exception.HokanException;
 import com.freakz.hokan_ng.common.exception.HokanServiceException;
 import com.freakz.hokan_ng.common.service.ChannelService;
 import com.freakz.hokan_ng.common.service.NetworkService;
 import com.freakz.hokan_ng.common.service.PropertyService;
 import lombok.extern.slf4j.Slf4j;
 import org.springframework.beans.factory.DisposableBean;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.context.ApplicationContext;
 import org.springframework.stereotype.Service;
 
 import javax.annotation.PostConstruct;
 import java.util.Collection;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 /**
  * Date: 11/1/13
  * Time: 11:33 AM
  *
  * @author Petri Airio (petri.j.airio@gmail.com)
  */
 @Service
 @Slf4j
 public class ConnectionManagerServiceImpl implements ConnectionManagerService, EngineConnector, DisposableBean {
 
   @Autowired
   private ChannelService channelService;
 
   @Autowired
   private IrcServerConfigService ircServerConfigService;
 
   @Autowired
   private NetworkService networkService;
 
   @Autowired
   private PropertyService propertyService;
 
   @Autowired
   private ApplicationContext context;
 
   private Map<String, IrcServerConfig> configuredServers;
 
   private Map<String, HokanCore> connectedEngines = new HashMap<>();
 
   private Map<String, Connector> connectors = new HashMap<>();
   private String botNick;
 
   public ConnectionManagerServiceImpl() {
   }
 
   @PostConstruct
   public void postInit() throws HokanException {
 
     propertyService.setProperty(PropertyName.PROP_SYS_CORE_HTTP_UPTIME, "" + new Date().getTime());
 
     updateServerMap();
     for (IrcServerConfig server : this.configuredServers.values()) {
       if (server.getIrcServerConfigState() == IrcServerConfigState.CONNECTED) {
         try {
           connect(server.getNetwork());
         } catch (HokanException e) {
           log.error("Couldn't get engine online: " + server.getNetwork(), e);
         }
       }
     }
   }
 
   private boolean botNickOk() {
     try {
       this.botNick = propertyService.findProperty(PropertyName.PROP_SYS_BOT_NICK).getValue();
     } catch (Exception e) {
       log.error("Error occured {}", e);
       return false;
     }
     return botNick != null && botNick.length() > 0;
   }
 
   private void updateServerMap() {
     List<IrcServerConfig> servers = ircServerConfigService.getIrcServerConfigs();
     configuredServers = new HashMap<>();
     for (IrcServerConfig server : servers) {
       configuredServers.put(server.getNetwork().getName(), server);
     }
   }
 
   @Override
   public void updateServers() {
     updateServerMap();
   }
 
   @Override
   public void destroy() throws Exception {
     log.warn("Going to be destroyed!");
     abortConnectors();
     disconnectAll();
     Thread.sleep(2 * 1000);
     stopEngines();
     Thread.sleep(2 * 1000);
     log.warn("Destroy phase done!");
   }
 
 
   public void stopEngines() {
     for (HokanCore core : this.connectedEngines.values()) {
       try {
         core.dispose();
       } catch (Exception e) {
         log.error("Error stopping engines: {}", e);
       }
     }
   }
 
   // --- ConnectionManagerService
 
   public void abortConnectors() {
     for (Connector connector : getConnectors()) {
       log.warn("Aborting connector: " + connector);
       connector.abortConnect();
     }
   }
 
   @Override
   public void joinChannels(String networkName) throws HokanServiceException {
     Network network;
     try {
       network = networkService.getNetwork(networkName);
       HokanCore engine = this.connectedEngines.get(networkName);
       if (engine == null) {
         throw new HokanServiceException("Engine not online: " + networkName);
       }
       joinChannels(engine, network);
 
     } catch (HokanException e) {
       throw new HokanServiceException(e);
     }
   }
 
   @Override
   public void connect(String networkName) throws HokanServiceException {
     Network network;
     try {
       network = networkService.getNetwork(networkName);
       connect(network);
     } catch (HokanException e) {
       throw new HokanServiceException(e);
     }
   }
 
   public void connect(Network network) throws HokanServiceException {
 
     if (!botNickOk()) {
       throw new HokanServiceException("PropertyName.PROP_SYS_BOT_NICK not configured correctly: " + this.botNick);
     }
     updateServerMap();
     HokanCore engine = getConnectedEngine(network);
     if (engine != null) {
       throw new HokanServiceException("Engine already connected to network: " + engine);
     }
 
     IrcServerConfig configuredServer = configuredServers.get(network.getName());
     if (configuredServer == null) {
       throw new HokanServiceException("IrcServerConfig not found for network: " + network);
     }
     configuredServer.setIrcServerConfigState(IrcServerConfigState.CONNECTED);
     this.ircServerConfigService.updateIrcServerConfig(configuredServer);
 
     Connector connector;
     connector = this.connectors.get(configuredServer.getNetwork().getName());
     if (connector == null) {
       connector = context.getBean(AsyncConnector.class);
       this.connectors.put(configuredServer.getNetwork().getName(), connector);
       connector.connect(this.botNick, this, configuredServer);
     } else {
       throw new HokanServiceException("Going online attempt already going: " + configuredServer.getNetwork());
     }
 
   }
 
   @Override
   public void disconnect(String networkName) throws HokanServiceException {
     Network network;
     try {
       network = networkService.getNetwork(networkName);
     } catch (HokanException e) {
       throw new HokanServiceException(e);
     }
 
     HokanCore engine = this.connectedEngines.get(network.getName());
     if (engine == null) {
       throw new HokanServiceException("No connected engine found for network: " + network);
     }
     engine.getIrcServerConfig().setIrcServerConfigState(IrcServerConfigState.DISCONNECTED);
     this.ircServerConfigService.updateIrcServerConfig(engine.getIrcServerConfig());
     engine.disconnect();
     this.connectedEngines.remove(networkName);
     log.info("Disconnected engine: " + engine);
   }
 
   @Override
   public void disconnectAll() {
     String msg = "";
     for (HokanCore engine : this.connectedEngines.values()) {
       msg += engine.toString();
       msg += "\n";
       engine.disconnect();
       engine.dispose();
     }
     this.connectedEngines.clear();
     log.info("Disconnected all engines\n" + msg);
   }
 
   // -------
 
   public HokanCore getConnectedEngine(Network network) {
     return this.connectedEngines.get(network.getName());
   }
 
   @Override
   public Collection<Connector> getConnectors() {
     return this.connectors.values();
   }
 
 
   // ---- EngineConnector
 
 
   @Override
   public void engineConnectorTooManyConnectAttempts(Connector connector, IrcServerConfig configuredServer) {
     this.connectors.remove(configuredServer.getNetwork().getName());
     log.info("Too many connection attempts:" + connector);
   }
 
   @Override
   public void engineConnectorGotOnline(Connector connector, HokanCore engine) throws HokanException {
 
     IrcServerConfig config = engine.getIrcServerConfig();
     config.setIrcServerConfigState(IrcServerConfigState.CONNECTED);
     this.ircServerConfigService.updateIrcServerConfig(config);
     engine.setIrcServerConfig(config);
 
     Network network = config.getNetwork();
     if (network.getFirstConnected() == null) {
       network.setFirstConnected(new Date());
     }
     network.addToConnectCount(1);
 
     engine.startOutputQueue();
 
     this.connectors.remove(network.getName());
     this.connectedEngines.put(network.getName(), engine);
     this.networkService.updateNetwork(network);
 
     joinChannels(engine, network);
 
   }
 
   private void joinChannels(HokanCore engine, Network network) {
     List<Channel> channels = this.channelService.findChannels(network, ChannelState.JOINED);
     for (Channel channelToJoin : channels) {
       engine.joinChannel(channelToJoin.getChannelName());
     }
   }
 
   @Override
   public void engineConnectorDisconnected(HokanCore engine) {
     IrcServerConfig config = engine.getIrcServerConfig();
     Network network = config.getNetwork();
     this.connectedEngines.remove(network.getName());
     log.info("Engine disconnected: " + engine);
   }
 
  @Override
  public void engineConnectorPingTimeout(HokanCore hokanCore) {
    log.info("Engine ping timeout: {}", hokanCore);
    try {
      this.connectedEngines.remove(hokanCore.getIrcServerConfig().getNetwork().getName());
      connect(hokanCore.getIrcServerConfig().getNetwork().getName());
    } catch (HokanServiceException e) {
      log.error("Couldn't re-connect after ping timeout!", e);
    }
  }
 }
