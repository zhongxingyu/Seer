 package com.cs.fwk.sim.cbs;
 
 
 import com.cs.fwk.sim.bricks.BaseElement;
 import com.cs.fwk.sim.model.SimElement;
 import com.cs.fwk.sim.model.SimHost;
 import com.cs.fwk.sim.model.TickDataBasicResult;
 import com.cs.fwk.sim.model.TickDataSymbolResult;
 import com.cs.fwk.sim.model.TickDataVolPriceResult;
 import com.cs.fwk.sim.types.Event;
 import com.cs.fwk.api.Cmd;
 import com.cs.fwk.api.MatElementDefs;
 import com.cs.fwk.api.comms.CFDatagram;
 import com.cs.fwk.api.comms.CxnInt;
 import com.cs.fwk.api.util.ConfigItem;
 
 /**
  * Raw Market Order placer
  * 
  * Simple order placer
  *
  * 		buy requests come in on input 0
  * 		sell requests com in on input 1
  * 
  * 		order volume is constrained between min & max (configurable)
  * 		Position is contstrained at max_posn
  * 
  * 		Orders are output on the order output port.
  * 
  * @author pjf
  *
  */
 public class RMO extends BaseElement implements SimElement {
 	private final static org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(RMO.class);
 	private int c_minVol;
 	private int c_maxVol;
 	private int c_maxPosn;
 	private int c_ip;
 	private int c_port;
 	private int[] posn;
 	private float[] pnl;
 	
 	public RMO(int id, SimHost host) {
 		super(id, MatElementDefs.EL_TYP_LOG,host);
 		posn = new int[256];
 		pnl = new float[256];
 		resetInstrumentPositions();
 	}
 
 	@Override
 	protected void processConfig(ConfigItem cfg) {
 		switch (cfg.getItemId()) {
 		case MatElementDefs.EL_RMO_C_MIN_VOL: 	c_minVol = cfg.getRawData();	break;
 		case MatElementDefs.EL_RMO_C_MAX_VOL: 	c_maxVol = cfg.getRawData();	break;
 		case MatElementDefs.EL_RMO_C_MAX_POSN: 	c_maxPosn = cfg.getRawData();	break;
 		case MatElementDefs.EL_RMO_C_UDPPORT: 	c_port = cfg.getRawData();		break;
 		case MatElementDefs.EL_RMO_C_UDPIP: 	c_ip = cfg.getRawData();		break;
 		default: logger.warn(getIdStr() + "Unexpected configuration: " + cfg); break;
 		}
 	}
 
 	@Override
 	protected void processCmd(Cmd cmd) {
 		switch (cmd.getConfigId()) {
 		case MatElementDefs.EL_RMO_C_RESET_POSN: 
 			resetInstrumentPositions();
 			break;
 		default:
 			logger.warn(getIdStr() + "Unexpected configuration: " + cmd);
 			break;
 		}
 	}
 
 
 	private void resetInstrumentPositions() {
 		logger.info(getIdStr() + "Resetting instrument positions ...");
 		for (int i=0; i<256; i++) {
 			posn[i] = 0;
 			pnl[i] = 0.0f;
 		}
 	}
 
 	@Override
 	protected void processEvent(int input, Event evt) {
 		logger.debug("processEvent() - " + evt);
 		boolean buy = false;
 		boolean sell = false;
 		if ((evt.getRawData() & 0x00000001) == 0x00000001) {
 			if (input == 1) {
 				buy = true;
 			}
 			if (input == 2) {
 				sell = true;
 			}
 			if (buy || sell) {
 				// get tick data
 				try {
 					TickDataBasicResult basicRslt = (TickDataBasicResult) host.tickdata(elementId, evt.getTickref(), MatElementDefs.TDS_BASIC);
 					TickDataSymbolResult symRslt = (TickDataSymbolResult) host.tickdata(elementId, evt.getTickref(), MatElementDefs.TDS_SYMBOL);
 					TickDataVolPriceResult pvRslt = (TickDataVolPriceResult) host.tickdata(elementId, evt.getTickref(), MatElementDefs.TDS_VOL_PRICE_SP);
 					if (basicRslt.isValid() &&  symRslt.isValid() && pvRslt.isValid()) {
 						// determine volume
 						if (basicRslt.getVolumeInt() >= c_minVol) {
 							int orderVol = Math.min(basicRslt.getVolumeInt(), c_maxVol);
 							String symbol = symRslt.getSymbol();
 							float price = pvRslt.getPrice();
 							String dir = "Buy";
 							if (sell) {
 								dir = "Sell";
 							}
 							// correllate with current posn
 							int instr = evt.getInstrument_id();
 							int posnRollback = posn[instr];
 							if (sell) {
 								if (orderVol > posn[instr]) {
 									orderVol = posn[instr];
 								}
 								posn[instr] -= orderVol;
 							} else {
 								// buy
 								posn[instr] += orderVol;
 								if (posn[instr] > c_maxPosn) {
 									// kill order
 									posn[instr] = posnRollback;
 									orderVol = 0;
 								}
 							}
 							
 							float logPrice = price;
 							if (orderVol > 0) {
 								if (buy) {
 									pnl[instr] -= orderVol * price; 
 								} else {
 									pnl[instr] += orderVol * price; 
 									logPrice = -price;
 								}
 								logger.info(getLogId() + "--- tickref=" + evt.getTickref() +
 										" Place ORDER: " + dir + " " + orderVol +
 										" " + symbol + " at " + price +
 										" posn=" + posn[instr] + " pnl=" + pnl[instr]);
 								transmitOrder(symbol,dir,price,orderVol);
 								Event evtOut = new Event(host.getCurrentSimTime(),elementId,0,evt.getInstrument_id(),evt.getTickref(), logPrice);
 								publishEvent(evtOut,1);	
 							}
 						}
 					}
 				} catch (Exception e) {
 					logger.error("Error placing order - " + e.getMessage());
 				}
 			}
 		}
 	}
 
 	/**
 	 * Encode and transmit the order
 	 * 
 	 * 
 	 * 		        8b     8b     64b        32b         32b
 	 *			-----------------------------------------------------
 	 *			|msg Size|B/S|instr_symbol|price fp(4)|volume uint32|
 	 *			-----------------------------------------------------
 	 *
 	 * @param symbol
 	 * @param dir
 	 * @param price
 	 * @param orderVol
 	 */
 	private void transmitOrder(String symbol, String dir, float price, int orderVol) {
 		// Encode the data
 		byte[] data = new byte[19];
 		int upto = 0;
 		data[upto++] = 17;
 		data[upto++] = (byte) dir.charAt(0);
 		for (int i=0; i<8; i++) {
 			data[i+2] = (byte) symbol.charAt(i);
 		}
 		upto += 8;
 		int iprice = Float.floatToIntBits(price);
 		data[upto++] = (byte) ((iprice >> 24) & 0xff);
 		data[upto++] = (byte) ((iprice >> 16) & 0xff);
 		data[upto++] = (byte) ((iprice >> 8) & 0xff);
 		data[upto++] = (byte) (iprice & 0xff);
 		data[upto++] = (byte) ((orderVol >> 24) & 0xff);
 		data[upto++] = (byte) ((orderVol >> 16) & 0xff);
 		data[upto++] = (byte) ((orderVol >> 8) & 0xff);
 		data[upto++] = (byte) (orderVol & 0xff);
 		// send it
		String ip = "" + c_ip;
		if (c_ip == 0) {
			ip = "direct";
		}
 		CxnInt cxn;
 		try {
 			cxn = host.getCxnOrLoopback(ip);
 			CFDatagram dgram = new CFDatagram(c_port,data);
 			cxn.send(dgram);
 		} catch (Exception e) {
 			logger.error("Unable to get cxn or send data - " + e.getMessage());
 		}
 	}
 
 	@Override
 	protected String getTypeName() {
 		return "RMO";
 	}
 
 
 }
