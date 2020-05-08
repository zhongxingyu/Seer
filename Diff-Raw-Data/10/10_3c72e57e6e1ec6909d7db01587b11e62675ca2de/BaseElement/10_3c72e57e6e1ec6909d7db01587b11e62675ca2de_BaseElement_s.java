 package com.pjf.mat.sim.bricks;
 
 
 import java.util.HashMap;
 import java.util.Map;
 
 import org.apache.log4j.Logger;
 
 import com.pjf.mat.api.Cmd;
 import com.pjf.mat.api.MatElementDefs;
 import com.pjf.mat.api.util.ConfigItem;
 import com.pjf.mat.sim.model.BaseState;
 import com.pjf.mat.sim.model.ClockTick;
 import com.pjf.mat.sim.model.LookupResult;
 import com.pjf.mat.sim.model.LookupValidity;
 import com.pjf.mat.sim.model.SimElement;
 import com.pjf.mat.sim.model.SimHost;
 import com.pjf.mat.sim.model.TickDataBasicResult;
 import com.pjf.mat.sim.model.TickDataMktRefResult;
 import com.pjf.mat.sim.model.TickDataSymbolResult;
 import com.pjf.mat.sim.model.TickDataVolPriceResult;
 import com.pjf.mat.sim.model.TickdataResult;
 import com.pjf.mat.sim.types.Event;
 import com.pjf.mat.sim.types.TickRefData;
 import com.pjf.mat.util.Conversion;
 
 /**
  * Provides foundation functionality for elements:
  * 
  * - input thread mgt and input source routing filtering
  * - common config handling
  * - lookup ports for requesters and service providers
  * - status reporting
  * 
  * @author pjf
  *
  */
 public abstract class BaseElement implements SimElement {
 	private final static Logger logger = Logger.getLogger(BaseElement.class);
 	protected static final int LOOKUP_TIMEOUT_DLY = 5;	// microtick delay on lookup tmo
 	protected static final int TICKDATA_TIMEOUT_DLY = 5;	// microtick delay on tickdata tmo
 	protected static final int TICKDATA_DLY = 2;	// tickdata lookup delay, microticks
 	private static final int MAX_LKU_TARGETS = 4;
 	protected final int elementId;
 	protected final int elementHWType;
 	protected final SimHost host;
 	private SourceRoute[] srcRouting;
 	private final int MAX_INPUTS = 4;
 	protected BaseState baseState;
 	protected int evtCount;
 	private int[] lkuTargets;	
 	private Map<Integer,TickRefData> tickrefData; // index by tickref
 	private boolean[] opEnable;
 
 	/**
 	 * Class to hold a source route specification for one input
 	 */
 	private class SourceRoute {
 		private int element;		// source element ID
 		private int port;			// source port number
 		
 		public SourceRoute() {
 			clear();
 		}
 		
 		public void clear() {
 			element = 0;
 			port = 0;
 		}
 
 		/**
 		 * @return true if this source route matches the specified event source
 		 */
 		public boolean matches(Event evt) {
 			return (evt.getSrc() == element) && (evt.getSrcPort() == port);
 		}
 
 		public void set(int element, int port) {
 			this.element = element;
 			this.port = port;			
 		}
 		
 		@Override
 		public String toString() {
 			return "" + element + ":" + port;
 		}
 	}
 	
 	/**
 	 * Normal constructor
 	 * 
 	 * @param id		id of this element
 	 * @param hwType	type of this element (hw id)
 	 * @param host		reference to host interface
 	 */
 	public BaseElement(int id, int hwType, SimHost host) {
 		this.elementId = id;
 		this.elementHWType = hwType;
 		this.host = host;
 		baseState = BaseState.CFG;
 		evtCount = 0;
 		srcRouting = new SourceRoute[MAX_INPUTS];
 		for (int i=0; i<MAX_INPUTS; i++) {
 			srcRouting[i] = new SourceRoute();	// init with no connection
 		}
 		lkuTargets = new int[MAX_LKU_TARGETS+1];
 		// default is to address all lookup targets
 		for (int i=0; i<=MAX_LKU_TARGETS; i++) {
 			lkuTargets[i] = MatElementDefs.EL_ID_ALL;
 		}
 		tickrefData = new HashMap<Integer,TickRefData>();
 		opEnable = new boolean[4];
 		for (int i=0; i<4; i++) {
 			opEnable[i] = true;
 		}
 	}
 	
 
 	@Override
 	public int getId() {
 		return elementId;
 	}
 	
 	/**
 	 * Return particular lookup target
 	 * 
 	 * @param trgNum - index of lookup target to return
 	 * @return		 - value of that lookup target
 	 * @throws Exception
 	 */
 	protected int getLookupTarget(int trgNum) throws Exception {
 		if (trgNum > MAX_LKU_TARGETS) {
 			throw new Exception("trgNum out of range");
 		}
 		return lkuTargets[trgNum];
 	}
 
 	
 	/**
 	 * Handle an incoming event.
 	 * Process event if it has been routed to one of our inputs
 	 * 
 	 * @return true if took the event
 	 * @throws Exception 
 	 */
 	@Override
 	public boolean putEvent(Event evt) throws Exception {
 		boolean taken = false;
 		for (int ip=0; ip<MAX_INPUTS; ip++) {
 			if (srcRouting[ip].matches(evt)) {
 				logger.debug(getIdStr() + "Received Event on input " + (ip+1) +
 						": " + evt);
 				evtCount++;
 				processEvent(ip+1,evt);
 				taken = true;
 			}				
 		}
 		return taken;
 	}
 
 	@Override
 	public void putConfig(ConfigItem cfg) throws Exception {
 		if ((cfg.getElementId() == elementId) || (cfg.getElementId() == MatElementDefs.EL_ID_ALL)) {
 			// config item is for us
 			logger.debug(getIdStr() + "received CFG request: " + cfg);
 			// check if we can process generically
 			switch (cfg.getSysType()) {
 			case SYSTEM: doSysConfig(cfg);	break;
 	
 			case LKU_TARGET:
 				int num = (cfg.getRawData() >> 8) & 0xf;		// 0..15
 				int target = cfg.getRawData() & 0x3f;
 				if (num > MAX_LKU_TARGETS) {
 					throw new Exception("target number out of range for config: " + cfg);
 				}
 				lkuTargets[num] = target;
 				break;
 	
 			case NORMAL:
 				// pass config item up to the element
 				processConfig(cfg);
 				break;
 				
 			default: throw new Exception("Unhandled configuration item: " + cfg);
 			}
 		}
 	}
 		
 	private void doSysConfig(ConfigItem cfg) throws Exception {
 		switch (cfg.getItemId()) {
 		case MatElementDefs.EL_C_SRC_ROUTE: 
 			int input = (cfg.getRawData() >> 16) & 3;	// 0..3
 			int port = (cfg.getRawData() >> 8) & 3;	// 0..3
 			int source = cfg.getRawData() & 0x3f;
 			srcRouting[input].set(source,port);
 			break;
 		case MatElementDefs.EL_C_CFG_OP_ENA:
 			if ((cfg.getRawData() & 0x01) == 0) {
 				opEnable[0] = false;
 			}
 			if ((cfg.getRawData() & 0x02) == 0) {
 				opEnable[1] = false;
 			}
 			if ((cfg.getRawData() & 0x04) == 0) {
 				opEnable[2] = false;
 			}
 			if ((cfg.getRawData() & 0x08) == 0) {
 				opEnable[3] = false;
 			}
 			break;
 		case MatElementDefs.EL_C_RESET: 
 			setBaseState(BaseState.RST);
 			processReset();
 			break;
 		case MatElementDefs.EL_C_CFG_DONE: 
 			processConfigDone();
 			setBaseState(BaseState.RUN);
 			break;
 		default: throw new Exception("Unhandled configuration item: " + cfg);
 		}
 	}
 
 	/**
 	 * Lookup a value from the lookup bus
 	 * 
 	 * @param instrumentId
 	 * @param tickref
 	 * @param lookupKey
 	 * @param target - element to target for lookup
 	 * @return lookup result
 	 * @throws Exception if an error occurred
 	 */
 	protected LookupResult lookup(int instrumentId, int tickref, int lookupKey, int target) throws Exception {
 		LookupResult result = host.lookup(elementId, instrumentId, tickref, lookupKey, target);
 		return result;
 	}
 
 	/**
 	 * Lookup a value from the tickdata bus
 	 * 
 	 * @param tickref
 	 * @param tickdata Key
 	 * @return tickdata result
 	 * @throws Exception if an error occurred
 	 */
 	protected TickdataResult tickdata(int tickref, int tickdataKey) throws Exception {
 		TickdataResult result = host.tickdata(elementId,tickref, tickdataKey);
 		return result;
 	}
 
 
 	protected void processReset() {
 		// default behaviour is to move to the cfg phase
 		setBaseState(BaseState.CFG);		
 	}
 	
 	protected void setBaseState(BaseState newState) {
 		logger.info(getIdStr() + "base state moved from " + baseState + " to " + newState);
 		baseState = newState;
 	}
 	
 	@Override
 	public void getStatus() {
 		host.publishElementStatusUpdate(elementId, getTypeName(),
 				baseState.toString(), 0, evtCount);
 	}
 
 	@Override
 	public void putCmd(Cmd cmd) {
 		if (cmd.getParent().getId() == elementId) {
 			processCmd(cmd);
 		}		
 	}
 
 	
 	@Override
 	public void processTick(ClockTick tick) {
 		// default behaviour is to ignore clock ticks
 	}
 
 	/**
 	 * Template method to process a config item
 	 * 
 	 * @param cfg
 	 */
 	protected abstract void processConfig(ConfigItem cfg);
 	
 	/**
 	 * Template method called when all configuration is complete
 	 * 
 	 */
 	protected void processConfigDone() {
 		// default behaviour is nothing
 	}
 	
 	/**
 	 * Template method to process a command
 	 * 
 	 * @param cmd
 	 */
 	protected void processCmd(Cmd cmd) {
 		// default behaviour is to log a warning
 		logger.warn(getIdStr() + "unexpected cmd:" + cmd);		
 	}
 
 	/**
 	 * Template method to process an event
 	 * 
 	 * @param input - which input the event arrived on (1..4)
 	 * @param evt
 	 * @throws Exception
 	 */
 	protected void processEvent(int input, Event evt) throws Exception {
 		// default behaviour is to log a warning
 		logger.warn(getIdStr() + "unexpected event:" + evt);		
 	}
 
 	protected String getIdStr() {
 		return "SIM(id=" + elementId + 
 				",type=" + elementHWType + "/" + getTypeName() + "):";
 	}
 
 	protected abstract String getTypeName();
 	
 	@Override
 	public String toString() {
 		return getIdStr() + "basisState=" + baseState + ", evtCount=" + evtCount;
 	}
 	
 	@Override
 	public void shutdown() {
 		logger.debug("Element shutdown (default behaviour)");
 	}
 	
 	/**
 	 * @param val
 	 * @return string representation of val as float and hex
 	 */
 	protected String show(float val) {
 		StringBuffer buf = new StringBuffer("[");
 		buf.append(val);
 		buf.append(",");
 		buf.append(Conversion.toHexFloatString(val));
 		buf.append("]");
 		return buf.toString();
 	}
 
 	
 	@Override
 	public LookupResult handleLookup(int instrumentId, int tickref, int lookupKey, int target) throws Exception {
 		// default behaviour is timeout
 		LookupResult rslt = new LookupResult(elementId,LookupValidity.TIMEOUT,LOOKUP_TIMEOUT_DLY);
 		if ((target == elementId) || (target == MatElementDefs.EL_ID_ALL)) {
 			rslt = lookupBehaviour(instrumentId,tickref,lookupKey);
 		}
 		return rslt;
 	}
 
 
 	/**
 	 * Template method for lookup behaviour
 	 * 
 	 * @param instrumentId
 	 * @param tickref
 	 * @param lookupKey 
 	 * @return the lookup result (validity = timeout if none)
 	 * @throws Exception 
 	 */
 	protected LookupResult lookupBehaviour(int instrumentId, int tickref, int lookupKey) throws Exception {
 		return new LookupResult(elementId,LookupValidity.TIMEOUT,LOOKUP_TIMEOUT_DLY);
 	}
 	
 	/**
 	 * Add tickref data to the tickref data map
 	 * 
 	 * @param tickref - tickref to use as index
 	 * @param trd - the tickref data
 	 */
 	protected void putTickrefData(int tickref, TickRefData trd) {
 		logger.debug(getIdStr() + "Adding tickref data [" + trd + "] at tickref=" + tickref);
 		tickrefData.put(new Integer(tickref), trd);
 	}
 
 	
 	@Override
 	public TickdataResult handleTickdata(int tickref, int tickdataKey) throws Exception {
 		// default behaviour is to lookup via local tickrefData map
 		TickRefData trd = tickrefData.get(new Integer(tickref));
 		TickdataResult rslt = new TickdataResult(TICKDATA_TIMEOUT_DLY);
 		if (trd != null) {
 			switch (tickdataKey){
 				case MatElementDefs.TDS_BASIC :
 					rslt = new TickDataBasicResult(trd,TICKDATA_DLY); 
 					break;
 					
 				case MatElementDefs.TDS_VOL_PRICE_SP :
 					rslt = new TickDataVolPriceResult(trd,TICKDATA_DLY); 
 					break; 
 				
 				case MatElementDefs.TDS_SYMBOL :
 					rslt = new TickDataSymbolResult(trd,TICKDATA_DLY); 
 					break;
 				
 				case MatElementDefs.TDS_EVT_REF :
 					rslt = new TickDataMktRefResult(trd,TICKDATA_DLY); 
 					break;
 				
 				default : throw new Exception("unhandled tickdata key = " + tickdataKey);
 			}
 		}
 		return rslt;
 	}
 
 	/**
 	 * Inject an event into the system
 	 * 
 	 * This call filters according to the output enable configuration
 	 * 
 	 * @param evt
 	 * @param latency - # microticks model time from input to this event
 	 */
 	public void publishEvent(Event evt, int latency) {
 		int port = evt.getSrcPort();
 		if (opEnable[port]) {
 			host.publishEvent(evt,latency);			
 		}
 	}
 
 
 	
 	/**
 	 * @return an ID that can be used to identify the element for logging.
 	 */
 	public String getLogId() {
 		return "EL:" + elementId + "/" + getTypeName() + " :";
 	}
 
 }
