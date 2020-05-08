 package br.ufrj.dcc.wsn.network.proc;
 
 import java.util.Random;
 import java.util.Vector;
 
 import br.ufrj.dcc.wsn.link.ILinkInterface;
 import br.ufrj.dcc.wsn.link.PacketReader;
 import br.ufrj.dcc.wsn.link.PacketWriter;
 import br.ufrj.dcc.wsn.link.RangedLinkInterface;
 import br.ufrj.dcc.wsn.profile.Profiler;
 import br.ufrj.dcc.wsn.util.Logger;
 import br.ufrj.dcc.wsn.util.Sorter;
 
 import com.sun.spot.util.IEEEAddress;
 
 
 public class NetworkInterface implements Runnable {
 	public static final long BROADCAST = 0xFFFF;
 	public static final byte SYNC = 1;
 	public static final byte COORD = 2;
 	public static final byte DATA = 3;
 	public static final int BACKOFF_MAX_WAIT = 1000;
 	public static final int ROUTING_RULES_MIN = 0;
 	public static final int ROUTING_RULES_MAX = 100;
 	
 	private static final Random random = new Random();
 	private static NetworkInterface instance;
 
 	private final ILinkInterface link;
 	
 	private final Vector neighbors;
 	private final Thread receiver;
 	private final RoutingEntry mySelf;
 	private final Sorter sorter;
 	private final Logger log;
 	
 	private RoutingEntry parent;
 	private Application app;
 	
 	private NetworkInterface() {
 		this.link = RangedLinkInterface.getInstance();
 		this.neighbors = new Vector();
 		this.receiver = new Thread(this);
 		this.mySelf = new RoutingEntry();
 		this.sorter = new Sorter(this.neighbors);
 		this.log = link.getLog();
 	}
 	
 	public static NetworkInterface getInstance() {
 		if (instance == null)
 			instance = new NetworkInterface();
 		return instance;
 	}
 	
 	public boolean sendPacket(byte type, long address, Message message) {
 		message = app.processRoutingMessage(message, address);
 		
 		int messageLength = 1 + message.getLength();
 		PacketWriter writer = link.getWriter();
 		
 		synchronized (writer) {
 			writer.setSourceAddress(getAddress());
 			writer.setDestinationAddress(address);
 			writer.setLength(messageLength);
 			writer.setNext(type);
 			message.writeInto(writer);
 			Profiler.getInstance().transmiting(messageLength);
 			return link.flush();
 		}
 	}
 	
 	public boolean sendRoutingPacket(byte type, long address) {
 		log.log(Logger.NET, "sending   to "+IEEEAddress.toDottedHex(address)+" "+(type == SYNC ? "sync" : "coord")+mySelf);
 		return sendPacket(type, address, mySelf);
 	}
 	
 	public boolean hasNoRoute() {
 		return parent == null;
 	}
 	
 	public boolean sendDataPacket(Message message) {
 		if (hasNoRoute())
 			return false;
 		
 		log.log(Logger.NET, "sending   to "+IEEEAddress.toDottedHex(parent.getAddress())+" data"+message);
 		return sendPacket(DATA, parent.getAddress(), message);
 	}
 	
 	private void addNeighbor(RoutingEntry entry) {
 		int index = neighbors.indexOf(entry);
 
 		if (index == -1) {
 			neighbors.addElement(entry);
 		} else {
 			neighbors.setElementAt(entry, index);
 			log.log(Logger.NET, "neighbor "+IEEEAddress.toDottedHex(entry.getAddress()));
 		}
 	}
 	
 	private void resolveBelongToBackbone() {
 		int rules = app.getRoutingRules(neighbors, parent);
 
 		if (rules < ROUTING_RULES_MIN)
 			rules = ROUTING_RULES_MIN;
 		else if (rules > ROUTING_RULES_MAX)
 			rules = ROUTING_RULES_MAX;
 
 		boolean coord = random.nextInt(ROUTING_RULES_MAX) < rules;
 		if (coord) {
 			app.joinedToBackbone();
 			log.log(Logger.NET, "backbone");
 		}
 		mySelf.setCoord(coord);
 	}
 	
 	protected int getBackoffTime() {
 		return random.nextInt(BACKOFF_MAX_WAIT);
 	}
 	
 	public void startNewCycle(int cycle) {
 		mySelf.setCycle((byte)cycle);
 		mySelf.setHops((byte)0);
 		mySelf.setCoord(false);
 		mySelf.setEnergy(getSpentEnergy());
 		mySelf.setAddress(getAddress());
 		
 		sendRoutingPacket(SYNC, BROADCAST);
 		app.startRoutingCycle(mySelf.getCycle(), mySelf.isCoord());
 		log.log(Logger.NET, "cycle");
 	}
 	
 	private boolean startNewCycle(RoutingEntry sync) {
 		if (sync.getCycle() <= mySelf.getCycle())
 			return false;
 		
 		resolveBelongToBackbone();
 		mySelf.setCycle(sync.getCycle());
 		mySelf.setHops((byte)(sync.getHops() + 1));
 		mySelf.setEnergy(getSpentEnergy());
 		mySelf.setAddress(getAddress());
 		
 		sendRoutingPacket(SYNC, BROADCAST);
 		app.startRoutingCycle(mySelf.getCycle(), mySelf.isCoord());
 		log.log(Logger.NET, "cycle");
 		waitNotInterrupted(getBackoffTime());
 		
 		return true;
 	}
 
 	private void refreshParent() {
 		sorter.sort(mySelf.isCoord());
 		parent = (RoutingEntry)neighbors.elementAt(0);
 		log.log(Logger.NET, "cycle "+mySelf.getCycle()+" parent "+ IEEEAddress.toDottedHex(parent.getAddress()));
 	}
 	
 	private boolean hasRoute() {
 		refreshParent();
 		
 		return parent.isCoord();
 	}
 	
 	private void forceRouteThrougthMySelf() {
 		mySelf.setCoord(true);
 		sendRoutingPacket(SYNC, BROADCAST);
 		app.joinedToBackbone();
 
 		log.log(Logger.DIGEST, 	"digest "+
 				mySelf+","+
 				parent+","+
 				IEEEAddress.toDottedHex(parent.getAddress()));
 	}
 	
 	private void forceRouteThrougthParent() {
 		parent.setCoord(true);
 		sendRoutingPacket(COORD, parent.getAddress());
 
 		log.log(Logger.DIGEST, 	"digest "+
 				mySelf+","+
 				parent+","+
 				IEEEAddress.toDottedHex(parent.getAddress()));
 	}
 	
 	private void handleSync(PacketReader reader) {
 		RoutingEntry sync = new RoutingEntry();
 		sync.readFrom(reader);
 		log.log(Logger.NET, "arrived from "+IEEEAddress.toDottedHex(reader.getSourceAddress())+" sync"+sync);
 		
 		addNeighbor(sync);
 		
 		if (!startNewCycle(sync))
 			return;
 		
 		if (hasRoute())
 			return;
 		
 		forceRouteThrougthParent();
 	}
 
 	private void handleCoord(PacketReader reader) {
 		RoutingEntry coord = new RoutingEntry();
 		coord.readFrom(reader);
 		log.log(Logger.NET, "arrived from "+IEEEAddress.toDottedHex(reader.getSourceAddress())+" coord"+coord);
 		
 		addNeighbor(coord);
 		
 		forceRouteThrougthMySelf();
 	}
 	
 	private void handleData(PacketReader reader) {
 		Profiler.getInstance().stopProcessing();
 		Message message = app.processDataMessage(reader);
 		Profiler.getInstance().startProcessing();
 		
 		if (hasNoRoute() || message == null)
 			return;
 		
 		log.log(Logger.NET, "frwrdng   to "+IEEEAddress.toDottedHex(parent.getAddress())+" data"+message);
 		
 		sendDataPacket(message);
 	}
 	
 	public boolean waitNotInterrupted(int inTime) {
 		Profiler.getInstance().stopProcessing();
 		
 		try {
 			Thread.sleep(inTime);
 		} catch (InterruptedException e) {
 			return false;
 		}
 		
 		Profiler.getInstance().startProcessing();
 		return true;
 	}
 	
 	public void run() {
 		Profiler.getInstance().startProcessing();
 		
 		while (waitNotInterrupted(1)) {
 			log.log(Logger.NET, "waiting");
 			PacketReader reader = link.getReader();	
 			synchronized (reader) {
 				Profiler.getInstance().receiving(reader.getLength());
 				byte packetType = reader.getNextByte();
 				
 				if (packetType == SYNC) {
 					handleSync(reader);
 				} else if (packetType == COORD) {
 					handleCoord(reader);
 				} else {
 					handleData(reader);
 				}
 			}
 		}
 		
 		Profiler.getInstance().stopProcessing();
 	}
 	
 	public void startListening() {
 		receiver.start();
 	}
 	
 	public void interrupt() {
 		receiver.interrupt();
 	}
 
 	public short getSpentEnergy() {		
 		//return (short)Spot.getInstance().getPowerController().getBattery().getBatteryLevel();
 		
 		//Retorna o (64 - log2(energia gasta))
 		long energy = Profiler.getInstance().getSpentEnergy();
 		/*
 		short i;
 		
 		for (i = 63; i >= 0; --i) {
 			if ((energy & (1 << i)) == (1 << i))
 				break;
 		}
 		
 		log.log(Logger.NET, "energy "+energy+" log "+(64-i));
 		
 		return (short) (64 - i);
         */
        return ((short)(10000 - (1 << (energy/100))));
 	}
 	
 	public long getAddress() {
 		return link.getAddress();
 	}
 	
 	public void setApp(Application app) {
 		this.app = app;
 	}
 
 	public RoutingEntry getState() {
 		return mySelf;
 	}
 
 	public Logger getLog() {
 		return log;
 	}
 }
