 package com.cs.fwk.api.logging;
 
 import java.util.Set;
 
 import com.cs.fwk.api.Element;
 import com.cs.fwk.api.OutputPort;
 import com.cs.fwk.api.Timestamp;
 import com.cs.fwk.util.Conversion;
 
 /**
  * Container for one Router Audit Log entry
  * 
  * @author pjf
  *
  */
 public class RtrAuditLog extends BaseLog {
 	private final int rawData;
 	private final Set<Element> takers;		// element that took the event
 	private final int qTimeMicroticks;		// queueing time
 	private final int delTimeMicroticks;	// delivery time
 	
 	private static final int CLK_TIME_NS = 10;	// FIXME microtick time
 
 	/**
 	 * Construct a Router Audit Log entry
 	 * 
 	 * @param timestamp
 	 * @param source
 	 * @param sourcePort 
 	 * @param takers
 	 * @param instrumentId
 	 * @param qTime
 	 * @param deltime
 	 * @param data
 	 * @param fdata 
 	 */
 	public RtrAuditLog(Timestamp timestamp, Element source, OutputPort sourcePort, Set<Element> takers, 
 			int instrumentId, int tickref, int qTime, int deltime, int rawData) {
 		super(timestamp,instrumentId,tickref,source,sourcePort);
 		this.qTimeMicroticks = qTime;
 		this.delTimeMicroticks = deltime;
 		this.rawData = rawData;
 		this.takers = takers;
 	}
 
 	@Override
 	public String getType() {
 		return "RTR";
 	}
 
 	public int getRawData() {
 		return rawData;
 	}
 
 	public Set<Element> getTakers() {
 		return takers;
 	}
 
 	public int getqTimeMicroticks() {
 		return qTimeMicroticks;
 	}
 
 	public int getDelTimeMicroticks() {
 		return delTimeMicroticks;
 	}
 
 	@Override
 	public String getDispValue() {
 		String val = "0x" + Conversion.toHexIntString(rawData);;
 		OutputPort srcPort = getSourcePort();
 		if (srcPort != null) {
 			val = srcPort.dataToString(rawData);
 		}
 		return val;
 	}
 
 	@Override
 	public String toString() {
 		StringBuffer buf = new StringBuffer();
 		buf.append("Router:");
 		buf.append(getTimestamp());
 		buf.append(": src:"); buf.append(getShortName(getSrcElement())); buf.append(":"); 
 		buf.append(getSourcePort().getName());
 		buf.append(",instr="); buf.append(getInstrumentId());
 		buf.append(",tickref="); buf.append(getTickref());
		buf.append(" Data="); buf.append(rawData);
 		buf.append(" Takers:[ ");
 		for (Element el : takers) {
 			buf.append(getShortName(el));
 			buf.append(" ");
 		}
 		buf.append("] qtime=");
 		buf.append(qTimeMicroticks*CLK_TIME_NS); buf.append("ns");
 		buf.append(" delTime=");
 		buf.append(delTimeMicroticks*CLK_TIME_NS); buf.append("ns"); 
 		return buf.toString();
 	}
 	
 	private String getShortName(Element el) {
 		if (el == null) {
 			return "null";
 		}
 		return el.getShortName();
 	}
 
 
 	@Override
 	public int hashCode() {
 		final int prime = 31;
 		int result = super.hashCode();
 		result = prime * result + rawData;
 		return result;
 	}
 
 	@Override
 	public boolean equals(Object obj) {
 		if (this == obj)
 			return true;
 		if (obj == null)
 			return false;
 		if (getClass() != obj.getClass())
 			return false;
 		RtrAuditLog other = (RtrAuditLog) obj;
 		if (rawData != Float.floatToIntBits(other.rawData))
 			return false;
 		if (!super.equals(obj)) {
 			return false;
 		}
 		return true;
 	}
 
 	
 
 }
