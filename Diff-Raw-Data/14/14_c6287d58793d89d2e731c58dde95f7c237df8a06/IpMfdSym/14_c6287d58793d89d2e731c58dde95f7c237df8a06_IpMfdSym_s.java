 package com.cs.fwk.sim.cbs;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import org.apache.log4j.Logger;
 
 import com.cs.fwk.sim.bricks.BaseElement;
 import com.cs.fwk.sim.model.SimElement;
 import com.cs.fwk.sim.model.SimHost;
 import com.cs.fwk.sim.types.Event;
 import com.cs.fwk.sim.types.TickRefData;
 import com.cs.fwk.api.Cmd;
 import com.cs.fwk.api.MatElementDefs;
 import com.cs.fwk.api.comms.LoopbackInt;
 import com.cs.fwk.api.util.ConfigItem;
 import com.cs.fwk.util.Conversion;
 import com.cs.fwk.util.data.MarketEventType;
 import com.cs.fwk.util.data.TickData;
 
 /**
  * FileMfdSym reads from a file to create tick data with the same logic as cb_raw_mfd_sym.
  * It has additional configuration to set the delivery rate and filepath
  * 
  * The file is a csv file with the following columns:
  * 
  * 
  * @author pjf
  *
  */
 public class IpMfdSym extends BaseElement implements SimElement, LoopbackInt {
 	private final static Logger logger = Logger.getLogger(IpMfdSym.class);
 	private static final int LATENCY = 3;	// network (microticks)
 	private OpConfig c_trade;				// output handling for trade events
 	private OpConfig c_bid;					// output handling for bid events
 	private OpConfig c_ask;					// output handling for ask events
 	private int c_mktId;					// market identifier
 	private int c_port;						// port to listen to
 	private boolean started;
 	private String symLeft;
 	private String symRight;
 	private final Map<String,Integer> c_symbols;	// map of allowed symbols, index by symbol
 	
 	public enum op_t{PRICE,VOLUME};
 	
 	private class OpConfig {
 		final boolean isOn;
 		final op_t type;
 		final int port;
 		
 		OpConfig(int cfg) {
 			if ((cfg & 0x10) == 0) {
 				type = op_t.PRICE;
 			} else {
 				type = op_t.VOLUME;
 			}
 			if ((cfg & 0xf) == 0xf) {
 				port = 0;
 				isOn = false;
 			} else {				
 				port = cfg & 0xf;
 				isOn = true;
 			}
 		}
 
 		/**
 		 * Construct as "off" config
 		 */
 		public OpConfig() {
 			isOn = false;
 			type = op_t.PRICE;
 			port = 0;
 		}
 	}
 	
 		
 	public IpMfdSym(int id, SimHost host) throws Exception {
 		super(id, MatElementDefs.EL_TYP_TG1,host);
 		started = false;
 		c_symbols = new HashMap<String,Integer>();
 	}
 	
 	@Override
 	protected void processConfig(ConfigItem cfg) {
 		switch (cfg.getItemId()) {
 		case MatElementDefs.EL_MDF_C_TRADE	: c_trade = new OpConfig(cfg.getRawData());	break;
 		case MatElementDefs.EL_MDF_C_BID	: c_bid = new OpConfig(cfg.getRawData());	break;
 		case MatElementDefs.EL_MDF_C_ASK	: c_ask = new OpConfig(cfg.getRawData());	break;
 		case MatElementDefs.EL_MDF_C_ISYM_L	: symLeft = cfg.getStringData();			break;
 		case MatElementDefs.EL_MDF_C_ISYM_R	: symRight = cfg.getStringData();			break;
 		case MatElementDefs.EL_MDF_C_ISYM_ID: putSym(symLeft,symRight,cfg.getRawData()&0xff);	break;
 		case MatElementDefs.EL_MDF_C_UDPPORT: c_port= cfg.getRawData();					break;
 		case MatElementDefs.EL_MDF_C_MKTID	: c_mktId = cfg.getRawData() & 0xff;		break;
 		default: logger.warn(getIdStr() + "Unexpected configuration: " + cfg);			break;
 		}
 	}
 
 	/**
 	 * Add symbol to list of allowed symbols
 	 * 
 	 * @param symLeft - left 4 chars of symbol
 	 * @param symRight - right 4 chars of symbol
 	 * @param instrId - instrument ID
 	 */
 	private void putSym(String symLeft, String symRight, int instrId) {
 		String sym = symLeft + symRight;
 		c_symbols.put(sym, instrId);
 		logger.info(getIdStr() + "Adding Symbol [" + sym + "=" + instrId + "]");
 	}
 
 	@Override
 	protected void processCmd(Cmd cmd) {
 		switch (cmd.getConfigId()) {
 		case MatElementDefs.EL_MFD_C_START: 
 			logger.info(getIdStr() + "Starting...");
 			// we are about to start, direct incoming msgs on configured port to us
 			host.subscribeIncomingMsgs(c_port, this);
 			started = true;	
 			break;
 		default:
 			logger.warn(getIdStr() + "Unexpected command: " + cmd);
 			break;
 		}
 	}
 	
 	@Override
 	protected void processConfigDone() {
 		host.subscribeIncomingMsgs(c_port, this);
 		started = true;	
 	}
 
 	@Override
 	public void injectLoopbackMsg(int port, byte[] msg) {
 		logger.info("processIncomingMsg() from port " + port);
 		if (started) {
 			int numEvts = msg[0];
 			int upto = 1;
 			int latency = 0;
 			for (int n=1; n<=numEvts; n++) {
 				processEvent(n,msg,upto,latency);
 				latency += LATENCY;
 				upto += 18;
 			}
 		}
 	}
 	
 	/**
 	 * Process one event
 	 * 
 	 * @param n		event number in pkt
 	 * @param msg	base message from which to extract data
 	 * @param start	start point in base message
 	 */
 	private void processEvent(int n, byte[] msg, int start, int latency) {
 		int u = start;
 		int evtType = msg[u];
 		u++;
 		String sym = new String(msg,u,8);
 		u += 8;
 		int priceInt = Conversion.getIntFromBytes(msg,u,4);
 		u += 4;
		int priceNdp = msg[12];
 		int volInt = Conversion.getIntFromBytes(msg,u,4);
 		u += 4;
 		float price = priceInt;
		price = price / (10^priceNdp);
 		float vol = volInt;
 		TickData td;
 		try {
 			td = new TickData(evtType,sym,price,vol);
 			Integer instrId = c_symbols.get(td.symbol);
 			if (instrId != null) {
 				// this symbol is to be handled
 				countEvent();
 				OpConfig opc = new OpConfig();
 				switch (td.evt.get()) {
 				case MarketEventType.TRADE 	: opc = c_trade;	break;
 				case MarketEventType.BID 	: opc = c_bid;		break;
 				case MarketEventType.ASK 	: opc = c_ask;		break;
 				}
 				if (opc.isOn) {
 					// send event
 					float data;
 					if (opc.type == op_t.PRICE) {
 						data = td.price;
 					} else {
 						data = td.volume;
 					}
 					int tickref = host.getTickref();
 					TickRefData trd = new TickRefData(tickref,c_mktId,instrId,td);
 					putTickrefData(tickref,trd);
 					Event evt = new Event(host.getCurrentSimTime(),elementId, opc.port, instrId, tickref, data);
 					host.publishEvent(evt,latency);							
 				}
 			}
 		} catch (Exception e) {
 			logger.error("processIncomingMsg() - exception: " + e.getMessage());
 		}
 	}
 
 
 	@Override
 	protected String getTypeName() {
 		return "IpMfdSym";
 	}
 
 
 }
