 package com.esc.msu;
 
 import java.io.IOException;
 import java.util.ArrayList;
 
 import org.snmp4j.PDU;
 import org.snmp4j.tools.console.SnmpRequest;
 
 import coldfusion.eventgateway.Gateway;
 import coldfusion.eventgateway.GatewayHelper;
 import coldfusion.eventgateway.GatewayServices;
 import coldfusion.eventgateway.Logger;
 
 public class SnmpGatewayHelper implements GatewayHelper {
 
 	private String gatewayID;
 	/**
 	 * The handle to the CF gateway service
 	 */
 	private GatewayServices gatewayServices;
 	/**
 	 * our instance of the Logger for log messages
 	 */
 	private Logger logger = null;
 	
 	public SnmpGatewayHelper(String gatewayID) {
 		
 		this.gatewayID = gatewayID;
 		
 		this.gatewayServices = GatewayServices.getGatewayServices();
 	    this.logger = this.gatewayServices.getLogger(this.gatewayID + "-helper");
 	    this.logger.info("Instantiating " + this.gatewayID);
 	}
 	
 	/**
 	 * Perform the requested SNMP Request on behalf of the gateway
 	 * 
 	 * @param type the request type, either "GET" or "GETNEXT"
 	 * @param cred the set of SNMP credentials for the target SNMP Agent
 	 * @param vbs the set of MIB variable bindings (varbinds) to retrieve
 	 * @return the SNMP Response corresponding to the provided Request
 	 * 
 	 * @throws IOException
 	 */
 	private SnmpGatewayResponse snmpGatewayRequest(String type,
 												   SnmpGatewayCredentials cred,
 												   SnmpGatewayVarbinds vbs) throws IOException {
 
 		ArrayList<String> args = extractArgs(type, cred, vbs);
 		
 		SnmpRequest sr = new SnmpRequest(args.toArray(new String[args.size()]));
 		long start = System.currentTimeMillis();
 		PDU response = sr.send();
 		long duration = System.currentTimeMillis() - start;
 		
 		SnmpGatewayResponse sgr = new SnmpGatewayResponse(type, start, duration,
 			                   cred.getTargetAddress(),
 			                   vbs, response);
 		this.logger.info("response is: \n" + sgr.getSynopsis());
 		return sgr;
 	}
 	/**
 	 * invoke an SNMP Get-Request
 	 * 
 	 * @param cred the set of SNMP credentials for the target SNMP Agent
 	 * @param vbs the set of MIB variable bindings (varbinds) to retrieve 
 	 * @return the SNMP Response corresponding to the provided Request
 	 * 
 	 * @throws IOException
 	 */
 	public SnmpGatewayResponse snmpGatewayGet(SnmpGatewayCredentials cred,
 											  SnmpGatewayVarbinds vbs) throws IOException {
		
 		return this.snmpGatewayRequest("GET", cred, vbs);
 	}
 	
 	
 	/**
 	 * invoke an SNMP GetNext-Request
 	 * 
 	 * @param cred the set of SNMP credentials for the target SNMP Agent
 	 * @param vbs the set of MIB variable bindings (varbinds) to retrieve 
 	 * @return the SNMP Response corresponding to the provided Request
 	 * 
 	 * @throws IOException
 	 */
 	public SnmpGatewayResponse snmpGatewayGetNext(SnmpGatewayCredentials cred,
 			  									  SnmpGatewayVarbinds vbs) throws IOException {
 		
 		return this.snmpGatewayRequest("GETNEXT", cred, vbs);
 	}
 	
 
 	/**
 	 * Extract the set of calling arguments and organize for use by the
 	 * org.snmp4j.tools.console.SnmpRequest class
 	 *  
 	 * @param pdu the type of SNMP PDU (GET or GETNEXT)
 	 * @param cred the set of SNMP credentials for the target SNMP Agent
 	 * @param vbs the set of MIB variable bindings (varbinds) to retrieve
 	 * @return an ArrayList<String> containing the set of calling arguments
 	 *         to pass to SnmpRequest
 	 */
 	private ArrayList<String> extractArgs(String pdu, SnmpGatewayCredentials cred, 	
 										   SnmpGatewayVarbinds vbs) {
 		ArrayList<String> args = new ArrayList<String>();
 		
 		// turn off log4j output
 		args.add("-d");		
 		args.add("OFF");
 		// identify the Request PDU type (GET or GETNEXT)		
 		args.add("-p");
 		args.add(pdu);
 		// identify the SNMP version (v1 or v2c)
 		args.add("-v");
 		args.add(cred.getSnmpVersionArg());
 		// identify the community string
 		args.add("-c");
 		args.add(cred.getTargetCommunity());
 		// identify the target SNMP Agent
 		args.add(cred.getTargetAddress() + cred.getTargetPortArg());
 		// specify the set of requested varbinds
 		args.addAll(vbs);
 		
 		return(args);
 	}
 	
 }
