 package com.cs.fwk.core.sys;
 
 
 import java.net.SocketException;
 import java.net.UnknownHostException;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.LinkedHashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import org.apache.log4j.Logger;
 
 import com.cs.fwk.api.Attribute;
 import com.cs.fwk.api.Cmd;
 import com.cs.fwk.api.Element;
 import com.cs.fwk.api.InputPort;
 import com.cs.fwk.api.MatApi;
 import com.cs.fwk.api.MatElementDefs;
 import com.cs.fwk.api.NotificationCallback;
 import com.cs.fwk.api.OutputPort;
 import com.cs.fwk.api.Status;
 import com.cs.fwk.api.comms.CBRawStatus;
 import com.cs.fwk.api.comms.CFCallback;
 import com.cs.fwk.api.comms.CFCommsInt;
 import com.cs.fwk.api.comms.CxnInt;
 import com.cs.fwk.api.comms.EvtLogRaw;
 import com.cs.fwk.api.comms.LkuAuditRawLog;
 import com.cs.fwk.api.comms.LoopbackInt;
 import com.cs.fwk.api.comms.MATCommsApi;
 import com.cs.fwk.api.comms.RtrAuditRawLog;
 import com.cs.fwk.api.logging.EventLog;
 import com.cs.fwk.api.logging.LkuAuditLog;
 import com.cs.fwk.api.logging.OrderLog;
 import com.cs.fwk.api.logging.RtrAuditLog;
 import com.cs.fwk.api.util.ConfigItem;
 import com.cs.fwk.api.util.HwStatus;
 import com.cs.fwk.util.Conversion;
 import com.cs.fwk.util.ElementStatus;
 import com.cs.fwk.util.TimeoutSemaphore;
 import com.cs.fwk.util.comms.CFComms;
 
 
 public class MATComms implements MATCommsApi, CFCallback {
 
	private static final String COMMS_VER = "01.02";
 
 	private final static Logger logger = Logger.getLogger(MATComms.class);
 	private static final long HWSIG_TIMEOUT_MS = 2000;
 	private static final int MIN_CFG_SPACE = 10;		// auto send when room for less than this many config items
 	private CFCommsInt cfComms = null;
 	private int port;
 	protected MatApi mat;
 	private Collection<NotificationCallback> notificationSubscribers;
 	private HwStatus hwStatus;
 	private TimeoutSemaphore hwSigSem;
 	private Map<Integer,LoopbackInt> inMsgSubscribers;
 
 	public MATComms(CxnInt cxn, int port) throws SocketException, UnknownHostException {
 		notificationSubscribers = new ArrayList<NotificationCallback>();
 		cfComms = new CFComms(cxn,port);
 		cfComms.setCallback(this);
 		this.port = port;
 		this.mat = null;
 		hwSigSem = new TimeoutSemaphore(0);
 		hwStatus = new HwStatus();
 		inMsgSubscribers = new HashMap<Integer,LoopbackInt>();
 	}
 
 	public MATComms(int port) throws SocketException, UnknownHostException {
 		notificationSubscribers = new ArrayList<NotificationCallback>();
 		this.port = port;
 		this.mat = null;
 		hwSigSem = new TimeoutSemaphore(0);
 		hwStatus = new HwStatus();
 		inMsgSubscribers = new HashMap<Integer,LoopbackInt>();
 	}
 
 	/**
 	 * Set connection for the Comms to FPGA
 	 * 
 	 * @param cxn
 	 */
 	public void setCxn(CxnInt cxn) {
 		cfComms = new CFComms(cxn,port);
 		cfComms.setCallback(this);
 	}
 
 	public void shutdown() {
 		if (cfComms != null) {
 			cfComms.shutdown();
 		}
 		hwSigSem.release();
 	}
 
 	@Override
 	public void setMat(MatApi mat) {
 		this.mat = mat;
 	}
 
 	@Override
 	public void addNotificationSubscriber(NotificationCallback subscriber) {
 		notificationSubscribers.add(subscriber);		
 	}
 
 	@Override
 	public void setHwStatus(HwStatus st) {
 		logger.info("setHwStatus() - new HW Status is: " + st);
 		this.hwStatus = st;
 	}
 
 	@Override
 	public HwStatus getHWStatus() {
 		return hwStatus;
 	}
 
 
 	@Override
 	public void sendConfig(List<Element> elements) throws Exception {
 		logger.info("Preparing to send config to addr:" + getCxn().getAddress() + " port:" + port);
 		cfComms.resetConfigBuffer();
 		for (Element cb : elements) {
 			if (cfComms.getConfigBufferSpace() < MIN_CFG_SPACE) {
 				logger.info("sendConfig(): Buf nearly full, autosending " + cfComms.getConfigBufferItemCount() + " config items");
 				cfComms.sendConfig();
 				cfComms.resetConfigBuffer();
 			}
 			hwEncodeConfig(cb);
 		}
 		// put config done for all CBs
 		cfComms.addSysConfigItem(MatElementDefs.EL_ID_ALL, MatElementDefs.EL_C_CFG_DONE, 0, 0);		
 		// send the config
 		logger.info("sendConfig(): encoded " + cfComms.getConfigBufferItemCount() + " items into " + cfComms.getConfigBufferLength() + " bytes");		
 		cfComms.sendConfig();
 	}
 
 	/**
 	 * 		Encodes the configuration for an item into a byte array comprising
 	 * 		a number of config items which are stored into the cfComms config buffer.
 	 * 		Note: does not send "CFG Done" for this CB
 	 * 
 	 * @param cfg 
 	 * @throws Exception 
 	 */
 	private void hwEncodeConfig(Element cb) throws Exception {	
 		// encode attribute values
 		for (Attribute attr : cb.getAttributes()) {
 			if (attr.getConfigId() >= 0) {
 				// Only configure attrs that are not pseudo attrs
 				switch (attr.getSysType()) {
 				
 				case NORMAL:
 					List<ConfigItem> configs = attr.getConfigList();
 					for (ConfigItem cfg : configs) {
 						switch (cfg.getSysType()) {
 						case NORMAL :	cfComms.addConfigItem(cfg.getElementId(), cfg.getItemId(), cfg.getArg(), cfg.getRawData());		break;
 						case SYSTEM :	cfComms.addSysConfigItem(cfg.getElementId(), cfg.getItemId(), cfg.getArg(), cfg.getRawData());	break;
 						default:	throw new Exception("Dont know how to encode sysType for " + cfg);
 						}
 					}
 					break;
 					
 				case SYSTEM:
 					cfComms.addSysConfigItem(cb.getId(), attr.getConfigId(), 0, attr.getEncodedData());
 					break;
 					
 				case LKU_TARGET:
 					cfComms.addSysConfigItem(cb.getId(), MatElementDefs.EL_C_CFG_LKU_TRG, 0, 
 							(attr.getConfigId() << 8) | (attr.getEncodedData() & 0xff));
 					break;
 					
 				default:
 					throw new Exception("Dont know how to encode: " + attr);
 				}
 			}
 		}		
 		// encode connections
 		for (InputPort ip : cb.getInputs()) {
 			OutputPort src = ip.getConnectedSrc();
 			if (src != null) {
 				cfComms.addCxnItem(src.getParent().getId(), src.getId()-1, cb.getId(), ip.getId()-1);		// src -> dest
 			}
 		}
 	}
 
 
 	@Override
 	public void sendCmd(Cmd cmd) throws Exception {
 		logger.debug("sendCmd(" + cmd.getFullName() + ")");
 		cfComms.sendSingleCmd(cmd.getParentID(),cmd.getConfigId(),cmd.getArg(), cmd.getData());
 	}
 
 
 	@Override
 	public void requestStatus() throws Exception {
 		cfComms.requestStatus();
 	}
 
 	@Override
 	public long getHWSignature() throws Exception{
 		// send request
 		cfComms.requestCFStatus();
 		// now wait till we have response back
 		// TODO add timeout
 		logger.info("getHWSignature() - waiting for CF Status from HW ...");
 		try {
 			hwSigSem.acquire(HWSIG_TIMEOUT_MS);
 		} catch (Exception e) {
 			logger.debug("getHWSignature(): Exception in acquiring semaphore.");
 		}
 		if (hwSigSem.timedOut()) {
 			throw new Exception("Request for HW Signature timed out");
 		}
 		return hwStatus.getHwSig();
 	}
 
 	@Override
 	public void requestStatus(Element cb) throws Exception {
 		cfComms.sendSingleCmd(MatElementDefs.EL_ID_SYSTEM_CONTROL, MatElementDefs.EL_C_STATUS_REQ, 0, cb.getId());
 	}
 
 
 	/**
 	 * @return underlying cxn used to do the comms
 	 */
 	@Override
 	public CxnInt getCxn() {
 		return cfComms.getCxn();
 	}
 
 	@Override
 	public void processCFStatus(HwStatus st) {
 		logger.info("processRxHwSig() - signature received: " + st);
 		hwStatus = st;
 		hwSigSem.release();
 	}
 
 	@Override
 	public void synchroniseClock(long syncOriginMs) throws Exception {
 		cfComms.synchroniseClock(syncOriginMs);
 		logger.info("synchroniseClock(" + syncOriginMs + ")");
 	}
 
 
 	@Override
 	public void requestLkuAuditLogs() throws Exception {
 		logger.info("requestLkuAuditLogs()");
 		cfComms.requestLkuAuditLogs();
 	}
 
 
 	@Override
 	public void processCBStatus(List<CBRawStatus> statusList) {
 		Set<Element> cbsUpdated = new LinkedHashSet<Element>();
 		for (CBRawStatus rs : statusList) {
 			Element cb = processNewStatusUpdate(rs);
 			if (cb != null) {
 				if (cb.hasStatusChanged(true)) {
 					cbsUpdated.add(cb);
 				}
 			}			
 		}
		logger.info("processStatusMsg(): received status for " + statusList.size() + " CBs, with " +
				cbsUpdated.size() + " changed.");
 		if (cbsUpdated.size() > 0) {
 			notifyElementStatusUpdate(cbsUpdated);
 		}
 	}
 	
 	/**
 	 * Notify subscribers of an element status update
 	 * @param list
 	 */
 	public void notifyElementStatusUpdate(Set<Element> list) {
 		for (NotificationCallback subscriber : notificationSubscribers) {
 			subscriber.notifyElementStatusUpdate(list);
 		}
 	}
 
 
 	/**
 	 * Handle status update for a CB
 	 * 
 	 * NOTE: this does not send any notifications
 	 * 
 	 * @param id - id of the CB (used to select from model)
 	 * @param type - type of the CB - to update
 	 * @param basisState - basis state to update
 	 * @param intState - internal state to update
 	 * @param evtCount - event count
 	 * @return the updated CB (or null if not found)
 	 */
 	private Element processNewStatusUpdate(CBRawStatus s) {	
 		int cbId = s.getId();
 		Element element = mat.getModel().getElement(cbId);
 		String srcName = "unknown";
 		if (element != null) {			
 			srcName = element.getType();
 			// Update status in the element
 			Status newStatus = new ElementStatus(s.getBasisStateStr(),s.getIntState(),s.getEvtCount());
 			element.setStatus(newStatus);
 			logger.debug("processNewStatusUpdate(): id=" + cbId + " name=" + srcName + " state=[" + s + "]");
 		} else {
 			logger.error("processNewStatusUpdate(): Error getting element, id=" + cbId + " state=[" + s + "]");
 		}
 		return element;
 	}
 
 	@Override
 	public void processEvtLogs(List<EvtLogRaw> rawLogs) {
 		for (EvtLogRaw rl : rawLogs) {
 			OutputPort outputPort = null;
 			if (rl.getSrc() > 0  &&  mat != null) {
 				Element el = mat.getModel().getElement(rl.getSrc());
 				if (el != null) {
 					if (el.getOutputs().size() > 0) {
 						outputPort = el.getOutputs().get(rl.getPort());
 					}
 				}
 			}
 			String value = "undefined";
 			if (outputPort != null) {
 				value = outputPort.dataToString(rl.getData());
 			} else {
 				float fval = Float.intBitsToFloat(rl.getData());
 				value = Float.toString(fval);
 			}
 			Element srcElement = mat.getModel().getElement(rl.getSrc());
 			EventLog evt = new EventLog(rl.getTs(),srcElement,outputPort,rl.getInstrId(), rl.getTickref(), rl.getData(), value);
 			logger.debug("Event from element=" + evt);
 			for (NotificationCallback subscriber : notificationSubscribers) {
 				subscriber.notifyEventLog(evt);
 			}
 		}
 	}
 
 	@Override
 	public void processLkuLogs(List<LkuAuditRawLog> rawLogs) {
 		List<LkuAuditLog> logs = new ArrayList<LkuAuditLog>();
 		for (LkuAuditRawLog rl : rawLogs) {
 			Element requester = mat.getModel().getElement(rl.getRequesterId());
 			Element responder = mat.getModel().getElement(rl.getResponderId());
 			LkuAuditLog log = new LkuAuditLog(rl.getTimestamp(),requester,rl.getInstrumentId(),
 					rl.getTickref(), rl.getOperation(), rl.getArg(), responder, rl.getRspTimeMicroticks(), rl.getResult(), rl.getData());
 			logs.add(log);
 		}
 		notifyLkuAuditLogsReceipt(logs);
 	}
 
 	@Override
 	public void processRtrLogs(List<RtrAuditRawLog> rawLogs) {
 		List<RtrAuditLog> logs = new ArrayList<RtrAuditLog>();
 		for (RtrAuditRawLog rl : rawLogs) {
 			Element source = mat.getModel().getElement(rl.getSourceId());
 			Set<Element> takers = ConvertBitmapToElementSet(rl.getTakerSet());
 			OutputPort op = null;
 			if (source != null) {
 				op = source.getOutputs().get(rl.getSourcePort());
 			}
 			RtrAuditLog log = new RtrAuditLog(rl.getTimestamp(),source,op,takers,
 					rl.getInstrumentId(),rl.getTickref(),rl.getqTimeMicroticks(),rl.getDelTimeMicroticks(),rl.getRawData());
 			if (source == null) {
 				logger.error("processRtrAuditLogMsg() - source element is null [" + log + "], sourceId=" + rl.getSourceId());
 			}
 			logs.add(log);
 		}
 		notifyRtrAuditLogsReceipt(logs);
 	}
 
 	@Override
 	public void processUnknownMsg(int destPort, byte[] msg) {
 		if (destPort == MatElementDefs.CS_RMO_ORDER_PORT) {
 			processIncomingOrder(msg);
 		} else {
 			logger.error("processUnknownMsg(): dest port=" + destPort);			
 		}		
 	}
 	
 	/**
 	 * Decode incoming order
 	 * 
 	 * 	|Size|B/S|symbol(8)|price fp(4)|volume uint32|
 	 *
 	 * @param msg
 	 */
 	private void processIncomingOrder(byte[] msg) {
 		int upto= 0;
 		StringBuffer symbolBuf = new StringBuffer();
 		String symbol;
 		int len = msg[upto++];
 		if (len == 17) {
 			char side = (char) msg[upto++];
 			for (int i=0; i<8; i++) {
 				symbolBuf.append(cvtChar(msg[upto++]));
 			}
 			symbol = symbolBuf.toString();
 			int priceData = Conversion.getIntFromBytes(msg,upto,4);
 			upto += 4;
 			float price = Float.intBitsToFloat(priceData);
 			int volume = Conversion.getIntFromBytes(msg,upto,4);	
 			logger.info("---- Order: side=" + side + ", symbol=[" + symbol + 
 					"], price=" + price + ", vol=" + volume);
 			// create Order log
 			OrderLog order = new OrderLog(symbol,side,price,volume);
 			for (NotificationCallback subscriber : notificationSubscribers) {
 				subscriber.notifyOrderReceipt(order);
 			}
 
 		} else {
 			logger.error("Order with incorrect len received. Len=" + len);
 		}
 	}
 	
 	
 	@Override
 	public void requestRtrAuditLogs() throws Exception {
 		logger.info("requestRtrAuditLogs()");
 		cfComms.requestLkuAuditLogs();
 	}
 
 
 	@Override
 	public void resetCounters() throws Exception {
 		logger.info("resetCounters()");
 		cfComms.resetCounters(MatElementDefs.EL_ID_ALL);
 	}
 
 
 	@Override
 	public void resetConfig(int cbId) throws Exception {
 		logger.info("resetConfig()");
 		cfComms.resetCBConfig(cbId);
 	}
 
 	/**
 	 * Notify subscribers of a list of LKU audit logs
 	 * @param logs
 	 */
 	protected void notifyLkuAuditLogsReceipt(List<LkuAuditLog> logs) {
 		for (NotificationCallback subscriber : notificationSubscribers) {
 			subscriber.notifyLkuAuditLogReceipt(logs);
 		}
 	}
 
 	/**
 	 * Notify subscribers of a list of LKU audit logs
 	 * @param logs
 	 */
 	protected void notifyRtrAuditLogsReceipt(List<RtrAuditLog> logs) {
 		for (NotificationCallback subscriber : notificationSubscribers) {
 			subscriber.notifyRtrAuditLogReceipt(logs);
 		}
 	}
 
 	/**
 	 * Convert a bitmap of elements into a set of elements
 	 * 
 	 * @param bitmap - bitmap of elements, bit 0=ID0, bit 31=ID31
 	 * @return set of elements or empty set if bitmap==0
 	 */
 	private Set<Element> ConvertBitmapToElementSet(int takers) {
 		int t = takers;
 		Set<Element> set = new HashSet<Element>();
 		int id = 0;
 		while (id < 32) {
 			if ( (t & 1) != 0) {
 				Element el = mat.getModel().getElement(id);
 				if (el == null) {
 					logger.error("ConvertBitmapToElementSet has unknown element id=" + id);
 				} else {
 					set.add(el);
 				}
 			}
 			id++;
 			t = t >>> 1;
 		}
 		return set;
 	}
 	
 	/**
 	 * Convert byte to char, substituting _ for unprintables
 	 * @param b - byte in
 	 * @return converted char
 	 */
 	private char cvtChar(byte b) {
 		char c = (char) b;
 		if (b < 0x20) {
 			c = '_';
 		};
 		return c;
 	}
 
 	@Override
 	public void injectLoopbackMsg(int port, byte[] msg) {
 		LoopbackInt cb = inMsgSubscribers.get(new Integer(port));
 		if (cb != null) {
 			// send the message to the subscriber
 			logger.debug("injectLoopbackMsg() forwarding injected msg on port " + port + " to " + cb);
 			cb.injectLoopbackMsg(port,msg);
 		} else {
 			cfComms.handleIncomingMsg(port,msg);
 		}
 	}
 
 	/**
 	 * Subscribe to incoming messages on a particular dest port
 	 * 
 	 * @param port - port to subscribe to
 	 * @param cb - callback to handle incoming messages
 	 */
 	public void subscribeIncomingMsgs(int port, LoopbackInt cb) {
 		inMsgSubscribers.put(new Integer(port), cb);
 		logger.info("subscribeIncomingMsgs() " + cb + " subscribed to port " + port); 
 	}
 
 	@Override
 	public String getApiVersion() {
 		return COMMS_VER;
 	}
 
 }
