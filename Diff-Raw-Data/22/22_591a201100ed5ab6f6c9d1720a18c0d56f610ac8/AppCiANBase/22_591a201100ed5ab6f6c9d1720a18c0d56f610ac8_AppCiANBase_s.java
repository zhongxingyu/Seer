 package ext.jist.swans.app;
 
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 
 import com.sun.java_cup.internal.lalr_item;
 
 import jist.runtime.JistAPI;
 import jist.swans.Constants;
 import jist.swans.app.AppInterface;
 import jist.swans.mac.MacAddress;
 import jist.swans.misc.Message;
 import jist.swans.misc.Sizeof;
 import jist.swans.net.NetAddress;
 import jist.swans.net.NetInterface;
 import jist.swans.net.NetInterface.NetHandler;
 import ext.util.stats.DucksCompositionStats;
 
 public abstract class AppCiANBase implements AppInterface, NetHandler
 {
     /**
      * Defines the IP protocol number since we directly use the network layer,
      * surpassing e.g. transport layer (UDP,TCP) Note: this protocol number
      * should be defined in jist.swans.Constants but is done here for modularity
      * reasons
      */
     public static final short       NET_PROTOCOL_NUMBER     = 524;
 
     /**
      * Time window to send a service ad
      */
     public static final long        TIMEOUT_SEND_SERVICE_AD = 80 * Constants.MILLI_SECOND;
     /**
      * Cool-off time after sending in which messages can only be received
      */
     public static final long        TIMEOUT_COOL_OFF        = 20 * Constants.MILLI_SECOND;
     /**
      * sleep before sending the retry wf-req
      */
     public static final long        SLEEP_BEFORE_RETRY      = 2 * Constants.SECOND;
 
     // network entity.
     protected NetInterface          netEntity;
 
     // self-referencing proxy entity.
     protected Object                self;
 
     protected int                   nodeId;
 
     // composition stats accumulator
     protected DucksCompositionStats compositionStats;
 
     public AppCiANBase(int nodeId, DucksCompositionStats compositionStats) {
         this.nodeId = nodeId;
         this.compositionStats = compositionStats;
         // init self reference
         this.self = JistAPI.proxyMany(this, new Class[] { AppInterface.class, NetInterface.NetHandler.class });
     }
 
     public void run() {
         run(null);
     }
 
     public abstract void run(String[] args);
 
     public abstract void receive(Message msg, NetAddress src, MacAddress lastHop, byte macId, NetAddress dst,
             byte priority, byte ttl);
 
     /**
      * Set network entity.
      * 
      * @param netEntity
      *            network entity
      */
     public void setNetEntity(NetInterface netEntity) {
         this.netEntity = netEntity;
     }
 
     /**
      * Return self-referencing NETWORK proxy entity.
      * 
      * @return self-referencing NETWORK proxy entity
      */
     public NetInterface.NetHandler getNetProxy() {
         return (NetInterface.NetHandler) self;
     }
 
     /**
      * Return self-referencing APPLICATION proxy entity.
      * 
      * @return self-referencing APPLICATION proxy entity
      */
     public AppInterface getAppProxy() {
         return (AppInterface) self;
     }
 
     protected void send(Message msg) {
         netEntity.send(msg, NetAddress.ANY, NET_PROTOCOL_NUMBER, Constants.NET_PRIORITY_NORMAL, new Byte("0"));
     }
 
     protected void send(Message msg, NetAddress addr) {
         netEntity.send(msg, addr, NET_PROTOCOL_NUMBER, Constants.NET_PRIORITY_NORMAL, new Byte("0"));
     }
 }
 
 class CiANWorkflow
 {
     public final static char         SYMBOL_DESTINATION = '\r';
     public final static String       STRING_DESTINATION = "dst";
 
     private int                      initiatorId;
     private String                   id;
     private char[]                   services;
     private int[]                    inputs;
     private List<List<CiANProvider>> providers;
 
     /*
      * TODO createServices only creates a linear workflow for now but it can
     * easily be changed to a sequential workflow.
      * Please note though that there is no workflow validation like in CiAN
      * so you must be careful and provide a valid workflow.
      * 
      * The predecessors and successors lists are not used at the moment.
      * They should be sent inside the workflow request and providers should not
      * send anymore directly to provider for (service + 1) but for every
      * providers in the list of successors. Same goes when they receive a token,
      * they have to wait for tokens for every predecessors for a given service
      * before actually executing it.
      */
     private List<List<Character>>    predecessors;
     private List<List<Character>>    successors;
 
     public CiANWorkflow(int nodeId, int msgId, int reqSize) {
         this.id = (new Integer(nodeId)).toString() + "-" + (new Integer(msgId)).toString();
         this.initiatorId = nodeId;
         this.services = createServices(reqSize);
         this.inputs = createInputs(services.length);
         this.providers = createProviders(services.length);
     }
 
     public CiANWorkflow(String id, char[] services, int[] inputs, CiANProvider[] providers) {
         this.initiatorId = Integer.parseInt(id.split("-")[0]);
         this.id = id;
         this.services = services;
         this.inputs = inputs;
         this.providers = createProviders(providers);
     }
 
     private char[] createServices(int size) {
         char[] services = new char[size + 1];
         char service = 'A';
         int index = 0;
         predecessors = new ArrayList<List<Character>>(size);
         successors = new ArrayList<List<Character>>(size);
 
         while (index < size) {
             services[index] = service;
             List<Character> _predecessors = new ArrayList<Character>();
             if (0 != index)
                 _predecessors.add((char) (service - 1));
             List<Character> _successors = new ArrayList<Character>();
             _successors.add((size - 1 == index) ? SYMBOL_DESTINATION : (char) (service + 1));
             predecessors.add(_predecessors);
             successors.add(_successors);
 
             index++;
             service++;
         }
         services[size] = SYMBOL_DESTINATION;
         List<Character> _predecessors = new ArrayList<Character>();
         _predecessors.add(services[size - 1]);
         predecessors.add(_predecessors);
         successors.add(new ArrayList<Character>());
 
         // compositionStats.setLastService(Character.toString(services[size-1]));
         return services;
     }
 
     private int[] createInputs(int size) {
         int[] inputs = new int[size];
 
         for (int i = 0; i < size; i++) {
             if (i == 0)
                 inputs[i] = 1;
             else
                 inputs[i] = -1;
         }
 
         return inputs;
     }
 
     private List<List<CiANProvider>> createProviders(int size) {
         List<List<CiANProvider>> providers = new ArrayList<List<CiANProvider>>();
 
         for (int i = 0; i < size; i++) {
             List<CiANProvider> tmp = new ArrayList<CiANProvider>();
             if (i == size - 1)
                 tmp.add(new CiANProvider(initiatorId, 0));
             providers.add(tmp);
         }
 
         return providers;
     }
 
     private List<List<CiANProvider>> createProviders(CiANProvider[] _providers) {
         List<List<CiANProvider>> providers = new ArrayList<List<CiANProvider>>();
 
         for (CiANProvider provider : _providers) {
             List<CiANProvider> tmp = new ArrayList<CiANProvider>();
             tmp.add(provider);
             providers.add(tmp);
         }
 
         return providers;
     }
 
     public String getId() {
         return id;
     }
 
     public char[] getServices() {
         return services;
     }
 
     public int[] getInputs() {
         return inputs;
     }
 
     public List<List<CiANProvider>> getProviders() {
         return providers;
     }
 
     public void updateProviderFor(char service, CiANProvider provider) {
         int index = getIndexForService(service);
         boolean isAdded = false;
         if (index == -1)
             return;
 
         List<CiANProvider> l = providers.get(index);
         for (int i = 0; i < l.size(); i++) {
             CiANProvider cur = l.get(i);
             if (provider.getConnectivity() > cur.getConnectivity()) {
                 l.add(i, provider);
                 isAdded = true;
                 break;
             }
         }
         if (!isAdded)
             l.add(provider);
     }
 
     public List<CiANProvider> getProvidersFor(int index) {
         return providers.get(index);
     }
 
     public void updateInputFor(char service, int input) {
         int index = getIndexForService(service);
         if (index == -1)
             return;
 
         inputs[index] = input;
     }
 
     public int getIndexForService(char service) {
         for (int i = 0; i < services.length; i++) {
             if (services[i] == service)
                 return i;
         }
         return -1;
     }
 
     public int getInputForService(char service) {
         int input = -2;
 
         int index = getIndexForService(service);
         if (index > -1 && index < inputs.length) {
             return inputs[index];
         }
 
         return input;
     }
 
     public char getServiceForIndex(int index) {
         char s = '\0';
         if (index > -1 && index < services.length) {
             return services[index];
         }
         return s;
     }
 
     public List<Character> getPredecessorsForService(char service) {
         return predecessors.get(getIndexForService(service));
     }
 
     public List<Character> getSuccessorsForService(char service) {
         return successors.get(getIndexForService(service));
     }
 
     public List<List<Character>> getPredecessors() {
         return predecessors;
     }
 
     public List<List<Character>> getSuccessors() {
         return successors;
     }
 
     public char getSuccessorService(char service) {
         char s = '\0';
         int index = getIndexForService(service);
 
         if (index > -1 && index < services.length - 1) {
             return services[index + 1];
         }
 
         return s;
     }
 
     public char getLastRealService() {
         return services[services.length - 2];
     }
 
     public boolean isLastService(char service) {
         if (service == SYMBOL_DESTINATION) {
             return true;
         }
         return false;
     }
 
     public boolean isServiceProvided(int index) {
         if (providers.get(index).size() > 0) {
             return true;
         } else {
             return false;
         }
     }
 
     public boolean areAllServicesProvided() {
         for (List<CiANProvider> l : providers) {
             if (l.isEmpty()) {
                 return false;
             }
         }
 
         return true;
     }
 
     public boolean isPartOfProviderList(char service, int providerId) {
         List<CiANProvider> providers = getProvidersFor(getIndexForService(service));
         Iterator<CiANProvider> it = providers.iterator();
         CiANProvider p;
         while (it.hasNext()) {
             p = it.next();
             if (p.getNodeId() == providerId) {
                 return true;
             }
         }
         return false;
     }
 }
 
 class CiANProvider
 {
     int nodeId;
     int connectivity;
 
     public CiANProvider(int nodeId, int connectivity) {
         this.nodeId = nodeId;
         this.connectivity = connectivity;
     }
 
     public int getNodeId() {
         return nodeId;
     }
 
     public int getConnectivity() {
         return connectivity;
     }
 
     public NetAddress getAddress() {
         return new NetAddress(new byte[] { 0, 0, 0, (byte) nodeId });
     }
 }
 
 abstract class CiANWorkflowMessage implements Message
 {
     protected String id;
 
     public CiANWorkflowMessage(String id) {
         this.id = id;
     }
 
     public String getId() {
         return id;
     }
 
     public void getBytes(byte[] msg, int offset) {
         throw new RuntimeException("not implemented");
     }
 
     public abstract int getSize();
 }
 
 /*
  * TODO Should also store services predecessors and successors
  */
 class CiANWorkflowRequest extends CiANWorkflowMessage
 {
     private char[]         services;
     private int[]          inputs;
     private CiANProvider[] providers;
 
     public CiANWorkflowRequest(String id, char[] services, int[] inputs, CiANProvider[] providers) {
         super(id);
         this.services = services;
         this.inputs = inputs;
         this.providers = providers;
     }
 
     public char[] getServicesCopy() {
         char[] copy = new char[services.length];
         System.arraycopy(services, 0, copy, 0, services.length);
         return copy;
     }
 
     public int[] getInputCopy() {
         int[] copy = new int[inputs.length];
         System.arraycopy(inputs, 0, copy, 0, inputs.length);
         return copy;
     }
 
     public CiANProvider[] getProvidersCopy() {
         CiANProvider[] copy = new CiANProvider[providers.length];
         System.arraycopy(providers, 0, copy, 0, providers.length);
         return copy;
     }
 
     public int getSize() {
         return Sizeof.inst(id.toCharArray()) + Sizeof.inst(services) + Sizeof.inst(inputs) + Sizeof.inst(providers);
     }
 }
 
 class CiANToken extends CiANWorkflowMessage
 {
     private char service;
     private int  input;
 
     public CiANToken(String id, char nextService, int input) {
         super(id);
         this.service = nextService;
         this.input = input;
     }
 
     public char getService() {
         return service;
     }
 
     public int getInput() {
         return input;
     }
 
     @Override
     public int getSize() {
         return Sizeof.inst(id.toCharArray()) + Sizeof.inst(service) + Sizeof.inst(input);
     }
 }
 
 class CiANDiscoveryRequest extends CiANWorkflowMessage
 {
     private int    ttl;
     private int    version;
     private char[] services;
 
     public CiANDiscoveryRequest(String id, char[] services, int ttl) {
         super(id);
         this.services = services;
         this.ttl = ttl > 0 ? ttl : 1;
         this.version = 0;
     }
 
     public int getVersion() {
         return version;
     }
 
     public void incrementVersion() {
         ++version;
     }
 
     public boolean isExpired() {
         return 0 == --ttl;
     }
 
     public int getTtl() {
         return ttl;
     }
 
     public char[] getServices() {
         return services;
     }
 
     public int getSize() {
         return Sizeof.inst(id) + Sizeof.inst(ttl) + Sizeof.inst(services);
     }
 
     public NetAddress getInitiatorAddress() {
         return new NetAddress(new byte[] { 0, 0, 0, (byte) Integer.parseInt(id.split("-")[0]) });
     }
 }
 
 class CiANDiscoveryResponse extends CiANWorkflowMessage
 {
     private int    senderId;
     private char[] services;
     private int    finalTtl;
 
     public CiANDiscoveryResponse(String id, int senderId, char[] services, int finalTtl) {
         super(id);
         this.senderId = senderId;
         this.services = services;
         this.finalTtl = finalTtl;
     }
 
     public int getSenderId() {
         return senderId;
     }
 
     public char[] getServices() {
         return services;
     }
 
     public int getDistance(int initialTtl) {
         return initialTtl - finalTtl;
     }
 
     public int getSize() {
         return Sizeof.inst(id) + Sizeof.inst(senderId) + Sizeof.inst(services) + Sizeof.inst(finalTtl);
     }
 }
