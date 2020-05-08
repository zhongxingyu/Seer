 package com.pjf.mat.util.comms;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 import org.apache.log4j.Logger;
 
 import com.pjf.mat.api.Attribute;
 import com.pjf.mat.api.Comms;
 import com.pjf.mat.api.Element;
 import com.pjf.mat.api.EventLog;
 import com.pjf.mat.api.InputPort;
 import com.pjf.mat.api.LkuAuditLog;
 import com.pjf.mat.api.LkuResult;
 import com.pjf.mat.api.MatApi;
 import com.pjf.mat.api.MatElementDefs;
 import com.pjf.mat.api.NotificationCallback;
 import com.pjf.mat.api.OutputPort;
 import com.pjf.mat.api.RtrAuditLog;
 import com.pjf.mat.api.Status;
 import com.pjf.mat.api.Timestamp;
 import com.pjf.mat.util.Conversion;
 import com.pjf.mat.util.ElementStatus;
 
 public abstract class BaseComms implements Comms {
 	private final static Logger logger = Logger.getLogger(BaseComms.class);
 	
 	protected MatApi mat;
 	protected Collection<NotificationCallback> notificationSubscribers;
 
 	// basis states
 	protected final byte BS_INIT	= 1;
 	protected final byte BS_CFG		= 2;
 	protected final byte BS_RST		= 3;
 	protected final byte BS_RUN		= 4;
 
 	public BaseComms() {
 		notificationSubscribers = new ArrayList<NotificationCallback>();
 	}
 	
 	public void setMat(MatApi mat) {
 		this.mat = mat;
 	}
 	
 	public UDPCxn getCxn() {
 		return null;
 	}
 
 	protected class EncodedConfigItemList {
 		private int itemCount;
 		private int upto;
 		private byte[] data;
 		
 		public EncodedConfigItemList() {
 			itemCount = 0;
 			upto = 1;
 			data = new byte[1500];
 		}
 
 		private void putRaw(int id, int configId, int arg, int value) {
 			data[upto++] = (byte) id;
 			data[upto++] = (byte) configId;
 			data[upto++] = (byte) arg;
 			data[upto++] = (byte) ((value >> 24) & 0xff);
 			data[upto++] = (byte) ((value >> 16) & 0xff);
 			data[upto++] = (byte) ((value >> 8) & 0xff);
 			data[upto++] = (byte) (value & 0xff);
 			itemCount++;
 			logger.debug("cfg.put(el=" + id + " cfgId=" + configId + " data=0x" + Integer.toHexString(value) +
 					") msg = [" + toHexString(data,upto-6,upto-1) + "]");			
 		}
 		
 		public void putConfigItem(int id, int configId, int arg, int value) {
 			putRaw(id,configId | 0x80,arg,value);
 		}
 		
 		public void putSystemItem(int id, int configId, int arg, int value) {
 			putRaw(id,configId | 0x40,arg,value);
 		}
 
 		public void putCmdItem(int id, int configId, int arg, int value) {
 			putRaw(id,configId | 0xc0,arg,value);
 		}
 
 
 		public byte[] getData() {
 			data[0] = (byte) itemCount;
 			byte[] buf = new byte[upto];
 			System.arraycopy(data, 0, buf, 0, upto);
 			return buf;
 		}
 		
 		public int getItemCount() {
 			return itemCount;
 		}
 		
 		public int getLength() {
 			return upto;
 		}
 		
 		@Override
 		public String toString() {
 			StringBuffer buf = new StringBuffer();
 			buf.append(itemCount); buf.append("/"); buf.append(upto);
 			buf.append("[");
 			buf.append(Conversion.toHexString(getData()));
 			buf.append("]");
 			return buf.toString();
 		}
 	}
 
 
 	@Override
 	public void addNotificationSubscriber(NotificationCallback subscriber) {
 		notificationSubscribers.add(subscriber);		
 	}
 
 	/**
 	 * Process a message that has been received from the MAT
 	 * 
 	 * @param msg
 	 */
 	protected void processIncomingMsg(byte[] msg) {
 		byte cmd = msg[0];
 		switch (cmd) {
 		case MatElementDefs.ST_TX_HWSIG: processHWSigMsg(msg); break;
 		case MatElementDefs.ST_TX_STATUS: processStatusMsg(msg);	break;
 		case MatElementDefs.ST_TX_EVTLOG: processEventLogMsg(msg); break;
 		case MatElementDefs.ST_TX_LKUAUDIT: processLkuAuditLogMsg(msg); break;
 		case MatElementDefs.ST_TX_RTRAUDIT: processRtrAuditLogMsg(msg); break;
 		default: logger.error("Unkown status message received: [" + Conversion.toHexString(msg)); break;
 		}
 	}
 
 
 
 	/**
 	 * Process HW signature received - store it and release sem
 	 * @param msg
 	 */
 	private void processHWSigMsg(byte[] msg) {
 		long hwSig = Conversion.getLongFromBytes(msg,1,8);
 		processRxHwSig(hwSig);
 	}
 
 	/**
 	 * Template method for handling receipt of HW signature
 	 * 
 	 * @param sig
 	 */
 	protected void processRxHwSig(long hwSig) {
 		logger.warn("processRxHwSig() - signature received - default action is do nothing");
 	}
 
 	/**
 	 * Process a status message. May contain one or more status elements
 	 * 
 	 * @param msg	- message, starting from cmd byte
 	 */
 	protected void processStatusMsg(byte[] msg) {
 		byte items = msg[1];
 		int upto = 2;
 		while (items-- > 0) {
 			// process first/next status item
 			int id = msg[upto++];
 			int type = msg[upto++];
 			int basisState = msg[upto++];
 			int intState = msg[upto++];
 			int evtCount = Conversion.getIntFromBytes(msg,upto,4);
 			upto += 4;
 			String typeStr = MatElementDefs.ElementTypeToString(type);
 			String basisStateStr = "";
 			switch(basisState) {
 			case BS_INIT:	basisStateStr = "INIT";							break;
 			case BS_CFG:	basisStateStr = "CFG";							break;
 			case BS_RST:	basisStateStr = "RST";							break;
 			case BS_RUN:	basisStateStr = "RUN";							break;
 			default:		basisStateStr = "unknown(" + basisState + ")";	break;
 			}
 			processNewStatusUpdate(id,typeStr,basisStateStr,intState,evtCount);
 		}		
 	}
 
 
 
 	protected void processNewStatusUpdate(int id, String type,
 			String basisState, int intState, int evtCount) {
 		
 		Element element = mat.getModel().getElement(id);
 		String srcName = "unknown";
 		if (element != null) {			
 			srcName = element.getType();
 			// Update status in the element
 			Status newStatus = new ElementStatus(basisState,intState,evtCount);
 			element.setStatus(newStatus);
 		}
 
 		logger.debug("processNewStatusUpdate(): id=" + id + 
 				" name=" + srcName + 
 				" type=" + type +
 				" basis-state=" + basisState + " int-state=" + intState +
 				" event-count=" + evtCount);
 		for (NotificationCallback subscriber : notificationSubscribers) {
 			subscriber.notifyElementStatusUpdate(element);
 		}
 
 	}
 
 	protected void processEventLogMsg(byte[] msg) {
 		byte items = msg[1];
 		int upto = 2;
 		while (items-- > 0) {
 			// process first/next log item
 			int src = msg[upto++];
 			int port = msg[upto++];
 			int instrId = msg[upto++];
 			long timestamp = Conversion.getLongFromBytes(msg,upto,6);
 			upto+=6;
 			int data = Conversion.getIntFromBytes(msg,upto,4);
 			upto +=4;
 			notifyEvent(new Timestamp(timestamp),src, port, instrId, data);
 		}		
 	}
 
 	/**
 	 * @param msg
 	 * |#items|ts(48bit)|RQ_EL_ID|instr|lku_op|RSP_EL_ID|rsp time|rslt|data(32bit)| .. next ..|
 	 */
 	private void processLkuAuditLogMsg(byte[] msg) {
 		byte items = msg[1];
 		int upto = 2;
 		List<LkuAuditLog> logs = new ArrayList<LkuAuditLog>();
 		while (items-- > 0) {
 			// process first/next log item
 			long timestamp = Conversion.getLongFromBytes(msg,upto,6);
 			upto+=6;
 			int requesterId = msg[upto++];
 			int instrId = msg[upto++];
 			int op = msg[upto++];
 			int responderId = msg[upto++];
 			int rspTime = msg[upto++];
 			int resultCode = msg[upto++];
 			int data = Conversion.getIntFromBytes(msg,upto,4);
 			float fdata = Float.intBitsToFloat(data);
 			upto +=4;
 			// resolve format of incoming binary data
 			Element requester = mat.getModel().getElement(requesterId);
 			Element responder = mat.getModel().getElement(responderId);
 			LkuResult rslt = LkuResult.UNKNOWN;
 			switch(resultCode) {
 			case 0:	rslt = LkuResult.NO_DATA;	break;
 			case 1:	rslt = LkuResult.OK;		break;
 			case 2:	rslt = LkuResult.ERROR;		break;
 			case 3:	rslt = LkuResult.TIMEOUT;	break;
 			}
 			LkuAuditLog log = new LkuAuditLog(new Timestamp(timestamp),requester,instrId,
 					op,responder,rspTime,rslt,fdata);
 			logs.add(log);
 		}
 		notifyLkuAuditLogsReceipt(logs);
 	}
 	
 	/**
 	 * @param msg
 	 * |#items|ts(48bit)|src|takers(32 bit map)|instr|data(32bit)|qtime|delivery time| .. next ..|
 	 */
 	private void processRtrAuditLogMsg(byte[] msg) {
 		byte items = msg[1];
 		int upto = 2;
 		List<RtrAuditLog> logs = new ArrayList<RtrAuditLog>();
 		while (items-- > 0) {
 			// process first/next log item
 			long timestamp = Conversion.getLongFromBytes(msg,upto,6);
 			upto+=6;
 			int sourceId = msg[upto++];
 			int sourcePort = msg[upto++];
 			int takerBitmap = Conversion.getIntFromBytes(msg,upto,4);
 			upto += 4;
 			int instrId = msg[upto++];
 			int data = Conversion.getIntFromBytes(msg,upto,4);
 			float fdata = Float.intBitsToFloat(data);
 			upto +=4;
 			int qTime = msg[upto++];
 			int delTime = msg[upto++];
 			// resolve format of incoming binary data
 			Element source = mat.getModel().getElement(sourceId);
 			Set<Element> takers = ConvertBitmapToElementSet(takerBitmap);
 			RtrAuditLog log = new RtrAuditLog(new Timestamp(timestamp),source,sourcePort,takers,
 					instrId,qTime,delTime,fdata);
 			logs.add(log);
 		}
 		notifyRtrAuditLogsReceipt(logs);
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
 			t /= 2;
 		}
 		return set;
 	}
 
 	/**
 	 * Notify subscribers of a collection of LKU audit logs
 	 * @param logs
 	 */
 	protected void notifyLkuAuditLogsReceipt(Collection<LkuAuditLog> logs) {
 		for (NotificationCallback subscriber : notificationSubscribers) {
 			subscriber.notifyLkuAuditLogReceipt(logs);
 		}
 	}
 
 	/**
 	 * Notify subscribers of a collection of LKU audit logs
 	 * @param logs
 	 */
 	protected void notifyRtrAuditLogsReceipt(Collection<RtrAuditLog> logs) {
 		for (NotificationCallback subscriber : notificationSubscribers) {
 			subscriber.notifyRtrAuditLogReceipt(logs);
 		}
 	}
 
 	/**
 	 * Notify subscribers of new event
 	 * 
 	 * @param ts 
 	 * @param src
 	 * @param instrId
 	 * @param data
 	 * @param data2 
 	 */
 	protected void notifyEvent(Timestamp ts, int src, int port, int instrId, int data) {
 		OutputPort op = null;
 		if (src > 0  &&  mat != null) {
 			Element el = mat.getModel().getElement(src);
 			if (el != null) {
 				if (el.getOutputs().size() > 0) {
 					op = el.getOutputs().get(port);
 				}
 			}
 		}
 		String value = "undefined";
 		if (op != null) {
 			value = op.dataToString(data);
 		} else {
 			float fval = Float.intBitsToFloat(data);
 			value = Float.toString(fval);
 		}
 		Element srcElement = mat.getModel().getElement(src);
 		EventLog evt = new EventLog(ts,srcElement,op,instrId, data, value);
 		logger.debug("Event from element=" + evt);
 		for (NotificationCallback subscriber : notificationSubscribers) {
 			subscriber.notifyEventLog(evt);
 		}
 	}
 	
 
 	/**
 	 * 		Encodes the configuration for an item into a byte array comprising
 	 * 		a number of config items.
 	 * 
 	 *		each item is 6 bytes:
 	 *		------------------------------------------------------------------------------
 	 *		| element ID (8 bit) | cmd (1bit) config item (7 bits) | data H..L (32 bits) |
 	 *		------------------------------------------------------------------------------
 	 * @param cfg 
 	 * @throws Exception 
 	 */
 	protected void hwEncodeConfig(Element el, EncodedConfigItemList cfg) throws Exception {	
 		// encode attribute values
 		for (Attribute attr : el.getAttributes()) {
 			switch (attr.getSysType()) {
 			
 			case NORMAL:
 				cfg.putConfigItem(el.getId(),attr.getConfigId(),0,attr.getEncodedData());
 				break;
 				
 			case SYSTEM:
 				cfg.putSystemItem(el.getId(),attr.getConfigId(),0,attr.getEncodedData());
 				break;
 				
 			case LKU_TARGET:
				cfg.putSystemItem(el.getId(),MatElementDefs.EL_C_CFG_LKU_TRG,0,
 						(attr.getConfigId() << 8) | (attr.getEncodedData() & 0xff));
 				break;
 				
 			default:
 				throw new Exception("Dont know how to encode: " + attr);
 			}
 		}		
 		// encode connections
 		for (InputPort ip : el.getInputs()) {
 			OutputPort src = ip.getConnectedSrc();
 			if (src != null) {
 				int val = ((ip.getId()-1) << 16) | (src.getId()-1) << 8 | src.getParent().getId();
 				cfg.putSystemItem(el.getId(), MatElementDefs.EL_C_SRC_ROUTE, 0, val);
 			}
 		}		
 		// put config done for this element
 		cfg.putSystemItem(el.getId(), MatElementDefs.EL_C_CFG_DONE, 0, 0);
 	}
 
 
 	
 	protected String toHexString(byte[] data, int start, int end) {
 		byte[] buf = new byte[end-start+1];
 		System.arraycopy(data, start, buf, 0, end-start+1);
 		return Conversion.toHexString(buf);
 	}
 
 }
