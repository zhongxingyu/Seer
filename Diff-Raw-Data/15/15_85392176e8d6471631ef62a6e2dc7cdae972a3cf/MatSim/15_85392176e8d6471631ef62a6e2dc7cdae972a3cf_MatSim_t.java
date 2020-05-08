 package com.cs.fwk.sim;
 
 import java.io.IOException;
 import java.net.SocketException;
 import java.net.UnknownHostException;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import org.apache.log4j.Logger;
 
 import com.cs.fwk.api.AttrSysType;
 import com.cs.fwk.api.Attribute;
 import com.cs.fwk.api.Cmd;
 import com.cs.fwk.api.Element;
 import com.cs.fwk.api.InputPort;
 import com.cs.fwk.api.LkuResult;
 import com.cs.fwk.api.MatElementDefs;
 import com.cs.fwk.api.MatSimInt;
 import com.cs.fwk.api.Timestamp;
 import com.cs.fwk.api.comms.CBRawStatus;
 import com.cs.fwk.api.comms.CxnInt;
 import com.cs.fwk.api.comms.EvtLogRaw;
 import com.cs.fwk.api.comms.MATCommsApi;
 import com.cs.fwk.api.logging.LkuAuditLog;
 import com.cs.fwk.api.logging.MatLogger;
 import com.cs.fwk.api.logging.RtrAuditLog;
 import com.cs.fwk.api.util.ConfigItem;
 import com.cs.fwk.api.util.HwStatus;
 import com.cs.fwk.core.sys.MATComms;
 import com.cs.fwk.sim.cbs.CBFactory;
 import com.cs.fwk.sim.model.BaseState;
 import com.cs.fwk.sim.model.ClockTick;
 import com.cs.fwk.sim.model.LookupResult;
 import com.cs.fwk.sim.model.LookupValidity;
 import com.cs.fwk.sim.model.SimAccess;
 import com.cs.fwk.sim.model.SimElement;
 import com.cs.fwk.sim.model.SimHost;
 import com.cs.fwk.sim.model.TickdataResult;
 import com.cs.fwk.sim.sys.Router;
 import com.cs.fwk.sim.types.Event;
 import com.cs.fwk.util.SystemServicesInt;
 import com.cs.fwk.util.comms.UDPCxn;
 
 
 public class MatSim extends MATComms implements SimHost, SimAccess, MatSimInt {
 
 	private static final String SIM_VER = "SIM v1.01";
 
 	private final static Logger logger = Logger.getLogger(MatSim.class);
 	private final Map<Integer,SimElement> simElements; // keyed on el id
 	private final LkuAuditLogger lkuAuditLogger;
 	private final RtrAuditLogger rtrAuditLogger;
 	private final Router router;
 	private SystemServicesInt sysServices;
 	private Clock clk;
 	private boolean stopOnError;
 	private int nextTickref;
 	
 	public MatSim(MatLogger logger, SystemServicesInt sysServices) throws SocketException, UnknownHostException {
 		super(0);
 		simElements = new HashMap<Integer,SimElement>();
 		lkuAuditLogger = new LkuAuditLogger();
 		rtrAuditLogger = new RtrAuditLogger();
 		router = new Router(this);
 		this.sysServices = sysServices;
 		initSim(logger);
 	}
 
 	public MatSim() throws SocketException, UnknownHostException {
 		super(0);
 		simElements = new HashMap<Integer,SimElement>();
 		lkuAuditLogger = new LkuAuditLogger();
 		rtrAuditLogger = new RtrAuditLogger();
 		router = new Router(this);
 	}
 
 	@Override
 	public void setArgs(MatLogger logger, SystemServicesInt services) throws Exception {
 			this.sysServices = services;
 			initSim(logger);
 	}
 
 	private void initSim(MatLogger logger) throws SocketException, UnknownHostException {
 		CxnInt cxn = new UDPCxn("localhost");
 		setCxn(cxn);		
 		stopOnError = true;
 		int simSpeed = 0;
 		if (System.getProperty("simslow") != null) {
 			simSpeed = 20;
 		}
 		logger.info("starting with simSpeed = " + simSpeed);
 		clk = new Clock(this,simSpeed,logger);
 		nextTickref = 1;
 		subscribeIncomingMsgs(6000, this);
 	}
 
 
 
 	public void setClock(Clock clock) {
 		this.clk = clock;
 	}
 	
 	@Override
 	public void init(Collection<Element> prjElements) throws Exception {
 		// instantiate the elements according to the project requirements
 		for (Element el : prjElements) {
 			if (el.getId() != 0) {
 				SimElement se = CBFactory.create(el.getId(),el.getHWType(),this);
 				if (se != null) {
 					simElements.put(new Integer(el.getId()),se);
 				} else {
 					String msg = "cant create simulation element for type " + 
 						el.getHWType() + " at el_id=" + el.getId();
 						logger.error(msg);
 						throw new Exception(msg);
 				}
 			}
 		}
 	}
 
 	/**
 	 * Transfer the configuration to the simulation elements
 	 */
 	@Override
 	public void sendConfig(List<Element> collection) throws Exception {
 		for (Element el : collection) {
 			// set attributes
 			for (Attribute attr : el.getAttributes()) {
 				if (attr.getConfigId() > 0) {
 					// only config items that are not pseudo-attrs
 					List<ConfigItem> configs = attr.getConfigList();
 					for (ConfigItem cfg : configs) {
 						for (SimElement se : simElements.values()) {
 							se.putConfig(cfg);
 						}
 						lkuAuditLogger.putConfig(cfg);
 						rtrAuditLogger.putConfig(cfg);
 					}
 				}
 			}
 			// set connections
 			for (InputPort ip : el.getInputs()) {
 				if (ip.getConnectedSrc() != null) {
 					ConfigItem cfg = new ConfigItem(el.getId(), MatElementDefs.EL_C_SRC_ROUTE,
 							ip.getConnectedSrc(),ip.getId());
 					for (SimElement se : simElements.values()) {
 						se.putConfig(cfg);
 					}
 				}
 			}
 			// set config done for this element
 			SimElement se = getSimElement(el.getId());
 			if (se != null) {
 				se.putConfig(new ConfigItem(el.getId(),AttrSysType.SYSTEM,MatElementDefs.EL_C_CFG_DONE,0));
 			}
 		}
 		router.cfgDone();
 	}
 
 	@Override
 	public void requestStatus() throws Exception {
 		for (SimElement se : simElements.values()) {
 			se.getStatus();
 		}
 		BaseState rbs = router.getBaseState();
 		int rcnt = router.getCount();
 		publishElementStatusUpdate(0, "Router",rbs.toString(),0,rcnt);
 	}
 
 	@Override
 	public void requestStatus(Element element) throws Exception {
 		SimElement se = getSimElement(element.getId());
 		if (se != null) {
 			se.getStatus();
 		}
 	}
 
 	@Override
 	public void sendCmd(Cmd cmd) throws Exception {
 		router.putCmd(cmd);
 		for (SimElement se : simElements.values()) {
 			se.putCmd(cmd);
 		}
 	}
 	
 	/**
 	 * Halt the simulation
 	 * 
 	 * @param reason
 	 */
 	public void haltSim(String reason) {
 		logger.warn("Halting simulation because of " + reason);
 		shutdown();
 	}
 
 	/* (non-Javadoc)
 	 * @see com.cs.fwk.sim.MatSimInt#shutdown()
 	 */
 	@Override
 	public void shutdown() {
 		logger.info("shutdown(): shutting down ...");
 //		for (SimElement se : simElements.values()) {
 //			se.shutdown();
 //		}
 		router.shutdown();
 		clk.shutdown();
 	}
 
 	@Override
 	public long getHWSignature() throws Exception {
		HwStatus st = new HwStatus(mat.getSWSignature(),10000,SIM_VER);
 		setHwStatus(st);
 		return getHWStatus().getHwSig();
 	}
 
 
 	@Override
 	public void publishEvent(Event evt, int latency) {
 		logger.debug("PublishEvent(" + evt + "," + latency + ")");
 		router.post(evt, latency);
 	}
 
 	@Override
 	public void publishEventLog(Timestamp ts, int srcId, int srcPort, int instrumentId, 
 			int tickref, int rawValue) {
 		logger.debug("publishEventLog()");
 		EvtLogRaw evt = new EvtLogRaw(ts, srcId, srcPort, instrumentId, tickref, rawValue);
 		List<EvtLogRaw> list = new ArrayList<EvtLogRaw>();
 		list.add(evt);
 		processEvtLogs(list);
 	}
 
 	@Override
 	public void publishElementStatusUpdate(int elementId, String type,
 			String basisState, int intState, int evtCount) {
 		CBRawStatus st = new CBRawStatus(elementId, type, basisState, intState, evtCount);
 		List<CBRawStatus> list = new ArrayList<CBRawStatus>();
 		list.add(st);
 		processCBStatus(list);
 	}
 
 
 	/**
 	 * Get the sim element for the particular ID
 	 * 
 	 * @param id - id of element
 	 * @return SimElement or null
 	 */
 	protected SimElement getSimElement(int id) {
 		if (id == 0) {
 			return null;
 		}
 		return simElements.get(new Integer(id));
 	}
 
 	@Override
 	public void notifyError(String msg) {
 		logger.error("notifyError()" + msg);
 		if (stopOnError) {
 			haltSim(msg);
 		}
 	}
 
 	@Override
 	public LookupResult lookup(int source, int instrumentId, int tickref, int lookupKey, int target)
 			throws Exception {
 		LookupResult result = null;
 		Timestamp startTime = clk.getSimTime();
 		String logstr = "lookup(src=" + source + ",instr=" + instrumentId +
 				",key=" + lookupKey + "): ";
 		Element responder = null;
 		for (SimElement se : simElements.values()) {
 			result = se.handleLookup(instrumentId, tickref, lookupKey, target);
 			if (!result.getValidity().equals(LookupValidity.TIMEOUT)) {
 				// this element handled the request - so break out of the loop
 				responder = mat.getModel().getElement(se.getId());
 				break;
 			}
 		}
 		Timestamp endTime = clk.getSimTime();
 		int lookupTime = (int) (endTime.getMicroticks() - startTime.getMicroticks());
 		LkuResult resultCode = result.getLkuResult();
 		Element sourceEl = mat.getModel().getElement(source);
 		int arg = 0;
 		lkuAuditLogger.addLog(startTime,sourceEl,instrumentId,tickref,lookupKey, arg, 
 				responder,resultCode,result.getFloatData(),lookupTime);
 		logger.debug(logstr + " returned " + result);
 		return result;
 	}
 	
 
 	@Override
 	public void postEventToElements(Event evt) throws ElementException {
 		if (evt.getSrc() != 0) {
 			Timestamp startDelivery = clk.getSimTime();
 			Set<Element> takers = new HashSet<Element>();
 			for (SimElement se : simElements.values()) {
 				try {
 					boolean taken = se.putEvent(evt);
 					if (taken) {
 						int id = se.getId();
 						Element el = mat.getModel().getElement(id);
 						if (el == null) {
 							notifyError("postEventToElements(" + evt + ") taker id=" + id + " has no model element");
 						} else {
 							takers.add(el);
 						}
 					}
 				} catch (Exception e) {
 					String msg = "Simulation error processing event into:" +
 						se + " Event=" + evt + " - " + e.getMessage();
 					notifyError(msg);
 				}
 			}
 			Timestamp endDelivery = clk.getSimTime();
 			long qtime = startDelivery.getMicroticks() - evt.getTimestamp().getMicroticks();
 			long deltime = endDelivery.getMicroticks() - startDelivery.getMicroticks();
 			Element source = mat.getModel().getElement(evt.getSrc());
 			rtrAuditLogger.addLog(evt.getTimestamp(),source,evt.getSrcPort(),takers,
 					evt.getInstrument_id(),evt.getTickref(), (int) qtime, (int) deltime, 
 					evt.getFloatData());
 		}
 	}
 
 	@Override
 	public Timestamp getCurrentSimTime() {
 		return clk.getSimTime();
 	}
 
 	@Override
 	public void publishMicroTick(Timestamp simTime) {
 		router.simMicroTick(simTime);
 		checkLkuAuditLogAutosend();
 		checkRtrAuditLogAutosend();
 	}
 
 	private void checkLkuAuditLogAutosend() {
 		List<LkuAuditLog> logs = lkuAuditLogger.checkAutoSend();
 		if (logs != null) {
 			notifyLkuAuditLogsReceipt(logs);			
 		}
 	}
 
 	private void checkRtrAuditLogAutosend() {
 		List<RtrAuditLog> logs = rtrAuditLogger.checkAutoSend();
 		if (logs != null) {
 			notifyRtrAuditLogsReceipt(logs);			
 		}
 	}
 
 	@Override
 	public void publishClockTick(ClockTick tick) {
 		for (SimElement se : simElements.values()) {
 			se.processTick(tick);
 		}
 	}
 
 	/* (non-Javadoc)
 	 * @see com.cs.fwk.sim.MatSimInt#start()
 	 */
 	@Override
 	public void start() {
 		logger.info("-------------- sim start -------------");
 		router.start();
 		clk.start();
 	}
 
 	@Override
 	public void synchroniseClock(long syncOrigin) throws Exception {
 		logger.info("synchroniseClock(" + syncOrigin + ")");
 		clk.sync(syncOrigin);		
 	}
 
 	@Override
 	public void requestLkuAuditLogs() throws Exception {
 		logger.info("requestLkuAuditLogs()");
 		List<LkuAuditLog> logs = lkuAuditLogger.getLogs(80);
 		notifyLkuAuditLogsReceipt(logs);
 	}
 
 	@Override
 	public void requestRtrAuditLogs() throws Exception {
 		logger.info("requestRtrAuditLogs()");
 		List<RtrAuditLog> logs = rtrAuditLogger.getLogs(80);
 		notifyRtrAuditLogsReceipt(logs);
 	}
 
 	@Override
 	public int getTickref() {
 		int ret = 0;
 		synchronized(this) {
 			ret = nextTickref;
 			nextTickref++;
 			if (nextTickref == 256) {
 				nextTickref = 1;
 			}
 		}
 		return ret;
 	}
 
 	@Override
 	@SuppressWarnings("unused")
 	public TickdataResult tickdata(int source, int tickref, int tickdataKey) throws Exception {
 		TickdataResult result = null;
 		Timestamp startTime = clk.getSimTime();
 		String logstr = "tickdata(src=" + source + ",tickref=" + tickref +
 				",key=" + tickdataKey + "): ";
 		Element responder = null;
 		for (SimElement se : simElements.values()) {
 			result = se.handleTickdata(tickref, tickdataKey);
 			if (result.isValid()) {
 				// this element handled the request - so break out of the loop
 				responder = mat.getModel().getElement(se.getId());
 				break;
 			}
 		}
 		Timestamp endTime = clk.getSimTime();
 		int lookupTime = (int) (endTime.getMicroticks() - startTime.getMicroticks());
 		logger.debug(logstr + " returned " + result);
 		return result;
 	}
 
 	@Override
 	public CxnInt getCxnOrLoopback(String ip) throws SocketException,
 			UnknownHostException {
 		return sysServices.getCxnOrLoopback(ip);
 	}
 
 	@Override
 	public void resetCounters() throws IOException {
 		router.resetCounters();
 		ConfigItem cfg = new ConfigItem(MatElementDefs.EL_ID_ALL, 
 				AttrSysType.SYSTEM, MatElementDefs.EL_C_RESET_CNTRS,0);
 		for (SimElement se : simElements.values()) {
 			try {
 				se.putConfig(cfg);
 			} catch (Exception e) {
 				throw new IOException("resetCounters(): " + e.getMessage());
 			}
 		}
 	}
 
 	@Override
 	public MATCommsApi getComms() {
 		return this;
 	}
 
 	@Override
 	public void resetConfig(int elId) throws IOException {
 		logger.warn("resetConfig for elID=" + elId);		
 	}
 
 	@Override
 	public String getApiVersion() {
 		return SIM_VER;
 	}
 	
 	@Override
 	public String toString() {
 		return "MATSim ver " + getApiVersion();
 	}
 
 
 }
