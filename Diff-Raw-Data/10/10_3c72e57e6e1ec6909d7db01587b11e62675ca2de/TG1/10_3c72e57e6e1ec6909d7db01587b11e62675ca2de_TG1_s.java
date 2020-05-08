 package com.pjf.mat.sim.element;
 
 import org.apache.log4j.Logger;
 
 import com.pjf.mat.api.Cmd;
 import com.pjf.mat.api.MatElementDefs;
 import com.pjf.mat.sim.bricks.BaseElement;
 import com.pjf.mat.sim.model.BaseState;
 import com.pjf.mat.sim.model.ClockTick;
 import com.pjf.mat.sim.model.SimElement;
 import com.pjf.mat.sim.model.SimHost;
 import com.pjf.mat.sim.model.TickdataResult;
 import com.pjf.mat.sim.types.Event;
 import com.pjf.mat.api.util.ConfigItem;
 
 /**
  * TG1 creates events with a known data pattern
  * 
  * @author pjf
  *
  */
 public class TG1 extends BaseElement implements SimElement {
 	private final static Logger logger = Logger.getLogger(TG1.class);
 	private static final int LATENCY = 0;	// input to output latency (microticks)
 	private float c_iv;			// initial value
 	private float c_p1;			// parameter #1
 	private int c_len;			// length of pattern before stop
 	private int c_gap;			// # ticks between events
 	private float data;
 	private int gapCntr;
 	private int lenCntr;
 	private boolean started;
 	private int instrumentId;
 	
 	public TG1(int id, SimHost host) {
 		super(id, MatElementDefs.EL_TYP_TG1,host);
 		started = false;
 		instrumentId = 3;
 	}
 
 
 	@Override
 	protected void processConfig(ConfigItem cfg) {
 		switch (cfg.getItemId()) {
 		case MatElementDefs.EL_TG1_C_IV: c_iv = cfg.getFloatData();	break;
 		case MatElementDefs.EL_TG1_C_P1: c_p1 = cfg.getFloatData();	break;
 		case MatElementDefs.EL_TG1_C_LEN: c_len = cfg.getRawData();	break;
 		case MatElementDefs.EL_TG1_C_GAP: c_gap = cfg.getRawData();	break;
 		default: logger.warn(getIdStr() + "Unexpected configuration: " + cfg); break;
 		}
 	}
 
 	@Override
 	protected void processCmd(Cmd cmd) {
 		switch (cmd.getConfigId()) {
 		case MatElementDefs.EL_TG1_C_START: 
 			logger.info(getIdStr() + "Starting.");
 			gapCntr = c_gap;
 			data = c_iv;
 			lenCntr = c_len;
 			started = true;	
 			break;
 		default:
 			logger.warn(getIdStr() + "Unexpected configuration: " + cmd);
 			break;
 		}
 	}
 
 
 	@Override
 	public void processTick(ClockTick tick) {
 		if (baseState == BaseState.RUN  &&  started) {
 			logger.debug("processTick(): gapCntr= " + gapCntr);
 			if (gapCntr == 0) {
 				// send event
 				int tickref = host.getTickref();
 				Event evt = new Event(host.getCurrentSimTime(),elementId, instrumentId, tickref, data);
 				host.publishEvent(evt,LATENCY);
 				gapCntr = c_gap;
 				data += c_p1;
 				lenCntr--;
 				if (lenCntr < 0) {
 					logger.info(getIdStr() + "Stopping.");
 					started = false;
 				}
 			} else {
 				gapCntr--;
 			}
 		}		
 	}
 
 	@Override
 	public TickdataResult handleTickdata(int tickref, int tickdataKey) {
 		TickdataResult rslt = new TickdataResult(TICKDATA_TIMEOUT_DLY);
 		switch (tickdataKey) {
 		}
 		return rslt;
 	}
 
 
 	@Override
 	protected String getTypeName() {
 		return "TG1";
 	}
 
 }
