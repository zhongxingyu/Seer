 /**
  * 
  */
 package com.esc.msu;
 
 import org.snmp4j.PDU;
 import org.snmp4j.smi.VariableBinding;
 
 import java.util.Vector;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.Vector;
 
 /**
  * comprises a response for an SNMP gateway request
  */
 public class SnmpGatewayResponse {
 
 	private final String requestType;
 	private final Date requestStart;
 	private final long requestDuration;
 	private final String target;
 	private final PDU response;	
 	private final Vector<String> requestVbs;
 	
 	/**
 	 * construct a response for an SNMP gateway request
 	 *  
 	 * @param requestType the request type (GET or GETNEXT)
 	 * @param requestStart the time in milliseconds when the request was sent
 	 * @param requestDuration the duration in millesconds between the time the
 	 * 				request was sent and a response was received
 	 * @param target the target SNMP Agent to which the request was sent
 	 * @param requestVbs the set of requested variable bindings (varbinds)
 	 * @param response the response PDU received for the associated request
 	 */
 	@SuppressWarnings("unchecked")
 	public SnmpGatewayResponse(String requestType,
 							   long requestStart, long requestDuration,
 			                   String target,
 			                   Vector<String> requestVbs,
 			                   PDU response) {
 		
 		this.requestType = new String(requestType);
 		this.requestStart = new Date(requestStart);
 		this.requestDuration = requestDuration;
 		this.target = new String(target);
 		this.requestVbs = (Vector<String> )requestVbs.clone();
 		this.response = response;	
 	}
 
 	/**
 	 * get the date and time the request was sent
 	 * @return a displayable string providing the date and time the request was sent
 	 */
 	public String getStart() {
 		return this.requestStart.toString();
 	}
 	
 	/**
 	 * get the target SNMP Agent involved with the request/response
 	 * @return a displayable string of the target SNMP Agent IP Address
 	 */
 	public String getTarget() {
 		return new String(this.target);
 	}
 	/**
 	 * get the duration between the time the request was sent and the
 	 * response received
 	 * @return a displayable string providing the duration in milliseconds
 	 */
 	public String getDuration() {
 		return new String(Long.toString(this.requestDuration) + " milliseconds");
 	}
 	
 	/**
 	 * get the request type of the original request sent for this response
 	 * @return the request type of the original request
 	 */
 	public String getRequestType() {
 		return new String(this.requestType);
 	}
 	
 	/**
 	 * get the response error-index.  
 	 * <p>
 	 * This value identifies which
 	 * request varbind is associated with the error-status.  The first
 	 * varbind index is 1.
 	 * <p>
 	 * @return failure -1 (no response was received)
 	 * <br>    success the response error-index
 	 */
 	public int getErrorIndex() {
 		int e = -1;
 		if (this.response != null) {
 			e = this.response.getErrorIndex();
 		}
 		return e;
 	}
 	
 	/**
 	 * get the response error-status .
 	 * <p>
 	 * @return 	failure -1 (no response was received)
 	 * <br>     success the response error-status
 	 */
 	public int getErrorStatus() {
 		int e = -1;
 		if (this.response != null) {
 			e = this.response.getErrorStatus();
 		}
 		return e;
 	}
 	
 	/**
 	 * get the text associated with the response error-status
 	 * <p>
 	 * @return failure the string "no response was received"
 	 *  <br>   success string containing the error-status text
 	 */
 	public String getErrorStatusText() {
 		String s;
 		
 		if (this.response != null) {
		    s = new String(this.response.getErrorStatusText());
		    if ((this.response.getErrorStatus() != 0)  &&
		        (this.response.getErrorIndex()  >  0)) {
		    	s = s.concat(" on " + this.requestVbs.get(this.response.getErrorIndex() -1));
		    }
 		} else {
 			s = new String("no response was received");
 		}
 		return s;
 	}
 	
 	/**
 	 * get the response varbindlist as a HashMap<String(OID), String<Value)>
 	 * <p>
 	 *       NOTE- the varbind ordering in the HashMap may NOT be consistent
 	 *             with the ordering in the SNMP response PDU
 	 * <p>
	 * @return failure an empty HashMap  (no response was received)
 	 * <br>    success a HashMap containing a representation of the response varbindlist
 	 */
 	@SuppressWarnings("unchecked")
 	public HashMap<String, String> getResponseVarbinds() {
		HashMap<String, String> vbl = new HashMap<String, String>();
 		
		if (this.response != null) {	
 			Vector<VariableBinding> vbs = this.response.getVariableBindings();
 			for(VariableBinding vb : vbs) {
 				vbl.put(vb.getOid().toString(), vb.getVariable().toString());
 			}
 		}
 		return vbl;
 	}
 	
 	/**
 	 * get the synopsis of the Snmp Response.  This method uses the other
 	 * methods defined in this class
 	 * 
 	 * @return a String suitable for displaying the Snmp Response.
 	 */
 	public String getSynopsis() {
 		return new String(this.getRequestType() + " sent to " + this.getTarget() +
 				   "\n\t started at " + this.getStart() +
 				   " response received after " + this.getDuration() +
 				   "\n\t with error_status=" + this.getErrorStatusText() + 
 				   " and error_index=" + this.getErrorIndex() +
 				   "\n\t and varbindlist " + this.getResponseVarbinds());
 	}
 }
