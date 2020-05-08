 /*
  * JBoss, Home of Professional Open Source.
  * Copyright 2008, Red Hat Middleware LLC, and individual contributors
  * as indicated by the @author tags. See the copyright.txt file in the
  * distribution for a full listing of individual contributors.
  *
  * This is free software; you can redistribute it and/or modify it
  * under the terms of the GNU Lesser General Public License as
  * published by the Free Software Foundation; either version 2.1 of
  * the License, or (at your option) any later version.
  *
  * This software is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
  * Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public
  * License along with this software; if not, write to the Free
  * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
  * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
  */
 package org.jboss.jmx.adaptor.snmp.agent;
 
 import java.io.InputStream;
 import java.net.InetAddress;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.List;
 import java.util.SortedMap;
 import java.util.SortedSet;
 import java.util.TreeMap;
 import java.util.TreeSet;
 import java.util.Vector;
 
 import javax.management.Attribute;
 import javax.management.MBeanServer;
 import javax.management.ObjectName;
 
 import org.jboss.jmx.adaptor.snmp.config.attribute.AttributeMappings;
 import org.jboss.jmx.adaptor.snmp.config.attribute.ManagedBean;
 import org.jboss.jmx.adaptor.snmp.config.attribute.MappedAttribute;
 import org.jboss.logging.Logger;
 import org.jboss.xb.binding.ObjectModelFactory;
 import org.jboss.xb.binding.Unmarshaller;
 import org.jboss.xb.binding.UnmarshallerFactory;
 import org.snmp4j.PDU;
 import org.snmp4j.PDUv1;
 import org.snmp4j.ScopedPDU;
 import org.snmp4j.Snmp;
 import org.snmp4j.mp.SnmpConstants;
 import org.snmp4j.smi.Counter32;
 import org.snmp4j.smi.Counter64;
 import org.snmp4j.smi.Integer32;
 import org.snmp4j.smi.Null;
 import org.snmp4j.smi.OID;
 import org.snmp4j.smi.OctetString;
 import org.snmp4j.smi.TimeTicks;
 import org.snmp4j.smi.Variable;
 import org.snmp4j.smi.VariableBinding;
 import org.snmp4j.util.DefaultPDUFactory;
 
 /**
  * Implement RequestHandler with mapping of snmp get/set requests
  * to JMX mbean attribute gets/sets. Currently only v1 / v2 PDUs are supported by the 
  * agent service. 
  *
  * @author <a href="mailto:hwr@pilhuhn.de>">Heiko W. Rupp</a>
  * @author <a href="mailto:dimitris@jboss.org">Dimitris Andreadis</a>
  * @author <a href="mailto:thauser@redhat.com"> or <a href="mailto:tom.hauser@gmail.com">Thomas Hauser</a>
  * @version $Revision: 110789 $
  */
 public class RequestHandlerImpl extends RequestHandlerSupport
    implements Reconfigurable
 {
 	// Protected Data ------------------------------------------------
 
 	private static final String NO_ENTRY_FOUND_FOR_OID = "No bind entry found for oid ";
 	private static final String SKIP_ENTRY = " - skipping entry";
 
 
 	/** Bindings from oid to mbean */
 	protected SortedMap bindings = new TreeMap();
 	
 	/** keep track of the instances of variables */
 	private SortedSet oidKeys = null;
 	
 	/** keep track of the objects created */
 	private SortedSet objectKeys = null;
 
 	/** Has this RequestHandler instance been initialized? */
 	private boolean initialized = false;
 
 
 	// Constructors --------------------------------------------------
 
 	/**
 	 * Default CTOR
 	 */
 	public RequestHandlerImpl() 
 	{
 		bindings = new TreeMap();
 		oidKeys = new TreeSet();
 		objectKeys = new TreeSet();
 	}
 
 	// RequestHandler Implementation ---------------------------------
 
 	/**
 	 * Initialize
 	 * 
 	 * @param resourceName A file containing get/set mappings
 	 * @param server Our MBean-Server
 	 * @param log The logger we use
 	 * @param uptime The uptime of the snmp-agent subsystem.
 	 */
 	public void initialize(String resourceName, MBeanServer server, Logger log, Clock uptime)
       throws Exception
    {
       log.debug("initialize() with res=" + resourceName);
 	   super.initialize(resourceName, server, log, uptime);
 		if (resourceName != null)
 			initializeBindings();
 		else
 			log.warn("No RequestHandlerResName configured, disabling snmp-get");
 
 		initialized = true;
 	}
 
    // TODO: change all error handling to Exceptions. Much more extensible. 
    //		this must be done because the use of return values could be much more useful if exceptions were thrown instead.
    // TODO: getValueFor, setValueFor, and getNextOid all need to throw appropriate exceptions. 
    // Reconfigurable Implementation ---------------------------------
    /**
     * Reconfigures the RequestHandler
     */
    public void reconfigure(String resName) throws Exception
    {
       if (resName == null || resName.equals(""))
          throw new IllegalArgumentException("Null or empty resName, cannot reconfigure");
 
       if (initialized == false)
          throw new IllegalStateException("Cannot reconfigure, not initialized yet");
       
       this.resourceName = resName;
    
       // Wipe out old entries
       bindings.clear();
       
       // Fetch them again
       initializeBindings();
    }
    
 	// SnmpAgentHandler Implementation -------------------------------
 
    /** 
     * <P>
     * This method handles SNMP GetBulk requests received in this session. Request
     * is already validated. Builds a response and passes it back.
     * A GetBulk request has two additional fields: 
     * nonRepeaters: the amount of scalar objects in the PDU
     * maxRepetitions: the number of getNext requests to be done on the scalar objects.
     * The parameter PDU is interpreted as follows: 
     * The first nonRepeaters elements in the VariableBinding list are retrieved with a simple GET snmp call.
     * The remaining (size - nonRepeaters) elements in the VariableBinding list have maxRepetitions GETNEXT calls
     * upon them. All of these VariableBindings are added into the response. 
     * The resultant response will have N + (M*R) VariableBindings
     * This method functions according to the protocol definition in RFC-3416
     * 
     * </P>
     * @param pdu
     * 		contains the following:
     * 		a list of OIDs 
     * 		an Integer > 0 indicating the number of non-repeaters (set with PDU.setNonRepeaters())
     * 		an Integer >= 0 indicating the number of repetitions   (set with PDU.setMaxRepetitions())
     * @return a PDU filled with the appropriate values, and if values were not found, null for those values. GetBulk
     * 		  will rarely error out completely if non-zero list of VariableBindings is provided in the parameter PDU.
     * 		  It may be filled with no useful data, but will always return a PDU with some content. 
     * 
     */
    
    	public PDU snmpReceivedGetBulk(PDU pdu){
    		PDU response = getResponsePDU(pdu);
    		if(response == null) {
    			return response;
    		}
    		
 		final boolean trace = log.isTraceEnabled();
 		if (trace) {
 			log.trace("requestID=" + pdu.getRequestID() + ", elementCount="
 					+ pdu.size());
 		}
 
 		int errorIndex = 1; 
 		int nonRepeaters = pdu.getNonRepeaters();
 		if (nonRepeaters<0)
 			nonRepeaters=0;
 		
 		int maxRepetitions = pdu.getMaxRepetitions();
 		if (maxRepetitions<0)
 			maxRepetitions=0;
 		Vector<VariableBinding> vbList = pdu.getVariableBindings();
 		VariableBinding vb = null;
 		Variable var = null;
 		
 		if (vbList.size() == 0){ // we were given an empty list in the PDU
 			log.debug("snmpReceivedGetBulk: No VariableBindings in received PDU");
 			makeErrorPdu(response, pdu, errorIndex, PDU.genErr);
 			return response;
 		}
 		for (int i=0; i < Math.min(nonRepeaters, vbList.size());i++){
 			vb = (VariableBinding)vbList.get(i);
 			OID oid = vb.getOid();
 			OID noid = null;
 			
 			try { 
 				noid = getNextOid(oid);
 			}
 			catch (EndOfMibViewException e){
 				log.debug("snmpReceivedGetBulk: End of MIB View in NonRepeaters.");
 				var = Null.endOfMibView;
 				response.add(new VariableBinding(oid, var));
 				continue;
 			}
 			try {
 				var = getValueFor(noid);
 			} 
 			catch (NoSuchInstanceException e){ 
 				// this won't happen because of using getnext;
 				log.debug("snmpReceivedGetBulk: An instance of an OID didn't exist.");
 				response.add(new VariableBinding(oid, Null.noSuchInstance));
 				continue;
 			} 
 			catch (VariableTypeException e){
 				log.debug("snmpReceivedGetBulk: Couldn't convert a Variable to a correct type.");
 				makeErrorPdu(response, pdu, errorIndex, PDU.genErr);
 				return response;
 			}
 			response.add(new VariableBinding(noid,var));	
 			errorIndex++;
 		}
 		
 			// for the remaining it.size() bindings, we perform maxRepetitions successive getNext calls
 		for (int i = nonRepeaters; i < vbList.size(); i++){
 			vb = (VariableBinding)vbList.get(i);
 			OID oid = vb.getOid();
 			OID noid = null;
 			try {
 				noid = getNextOid(oid);
 			}
 			
 			catch (EndOfMibViewException e) {
 				// we know that doing more getnext in this case will just give more end of MIB view results. so we try the next VB.
 				response.add(new VariableBinding(oid, Null.endOfMibView));
 				continue;
 			}
 							
 			for (int j = 0; j < maxRepetitions; j++){
 				try {
 					var = getValueFor(noid);
 				} 
 				// since we're using getNext to retrieve the OIDs, this can't happen. we'll always have an instance.
 				catch (NoSuchInstanceException e) {
 					var = Null.noSuchInstance;						
 				} 
 				catch (VariableTypeException e) {
 					makeErrorPdu(response, pdu, errorIndex, PDU.genErr);
 					var = Null.instance;						
 				}
 				
 				response.add(new VariableBinding(noid,var));
 				
 				try {
 					noid = getNextOid(noid);
 				} 
 				catch (EndOfMibViewException e) {
 					// doing more repetitions here is a waste of time. 
 					response.add(new VariableBinding(noid, Null.endOfMibView));
 					break;
 				}
 			}
 			errorIndex++;	
 		}
 		return response;
    	}
    	   
     /**
 	 * <P>
 	 * This method is defined to handle SNMP GET and GETNEXT requests that are received by
 	 * the session. The request has already been validated by the system. This
 	 * routine will build a response and pass it back to the caller.
 	 * The behaviour is defined in RFC-3416 for v2 protocols.
 	 * </P>
 	 * 
 	 * @param pdu
 	 *            The SNMP pdu.           
 	 * 
 	 * @return a PDU filled in with the proper response, or a PDU filled with appropriate 
 	 * 		   error indications. Both the error type (ErrorStatus) and error index (the binding in the 
 	 * 		   PDU being processed that caused the error are returned.)
 	 */
 	public PDU snmpReceivedGet (PDU pdu)
 	{
 		PDU response;
 		// this counts the number of VariableBindings and indicates which one caused a problem, if any
 		int errorIndex = 1;
 		// flags the end of the MIB 
 		boolean endOfMib = false;
 		
 		response = getResponsePDU(pdu); 		
 		//indicate by copying the Req ID that this response is for the PDU sent with that ReqID.
 		
 		final boolean trace = log.isTraceEnabled();
 		
 			if (trace) {
 				log.trace("requestID=" + pdu.getRequestID() + ", elementCount="
 						+ pdu.size());
 			}
 		
 		Iterator it = pdu.getVariableBindings().iterator();
 		VariableBinding newVB;
 		Variable var;
 		OID noid = null;
 			
 		while (it.hasNext()){
 			VariableBinding vb = (VariableBinding)it.next();
 			OID oid = vb.getOid();
 			
 			if (pdu.getType()==PDU.GETNEXT){
 				// we get the lexicographically next OID from the given one.
 				try{
 				noid = getNextOid(oid);
 				}
 				// if there are no lexicographically larger OIDs than this one
 				catch(EndOfMibViewException e){
 					log.debug("snmpReceivedGet: GETNEXT operation left the MIB View");
 					endOfMib=true;
 				}
 				
 				if (endOfMib){
 					newVB = new VariableBinding(oid);
 					var = Null.endOfMibView;
 				}
 				else{
 					newVB = new VariableBinding(noid);
 					try {
 						var = getValueFor(noid);
 					}
 					catch (NoSuchInstanceException e) {
 						log.debug("snmpReceivedGet: GETNEXT operation returned null. No such OID.");
 						var = Null.noSuchInstance;
 					}
 					catch (VariableTypeException e) {
 						log.debug("snmpReceivedGet: GETNEXT operation could not convert the returned value for " + noid + " into an appropriate type.");
 						makeErrorPdu(response, pdu, errorIndex, PDU.genErr);
 						return response;
 					}
 				}
 			}
 			else {
 				newVB = new VariableBinding(oid);
 				var = null;
 				// check the existence of the object for the requested instance
 				// the object is the OID with the last number removed.
 				if (checkObject(oid)){				
 					try {
 						var = getValueFor(oid);
 					}
 					catch (NoSuchInstanceException e) {
 						log.debug("snmpReceivedGet: GET operation returned null. No such Instance.");
 						var = Null.noSuchInstance;
 					}
 					catch (VariableTypeException e) {
 						log.debug("snmpReceivedGet: GET operation could not convert the returned value for " + oid + " into an appropriate type.");
 						makeErrorPdu(response, pdu, errorIndex, PDU.genErr);
 						return response;
 						
 						}
 					
 				}
 				// if we get here, there's no such object.
 				else{
 					var = Null.noSuchObject;						
 				}
 					
 			}
 			newVB.setVariable(var);
 			response.add(newVB);
 			errorIndex++;
 	 	}
 		//TODO: check size constraints of the sender
 		return response;
 	}
 
 	/**
 	 * <P>
 	 * This method is defined to handle SNMP Set requests that are received by
 	 * the session. The request has already been validated by the system. This
 	 * routine will build a response and pass it back to the caller. 
 	 * Upon any error, this method will rollback all sets that were made before the 
 	 * failure occured.
 	 * The behaviour is defined in RFC-3416
 	 * </P>
 	 * 
 	 * @param pdu
 	 *           The SNMP pdu
 	 * 
 	 * @return PDU filled with the new value of the given OID and an error of 0 on success,
 	 * 		   or a PDU with the same VB list as given and an error status indicating why the operation failed,
 	 * 		   and which VariableBinding caused the problem. Any changes that have completed before this problem occured
 	 * 	       will be undone, as per RFC-3416
 	 */
 	public PDU snmpReceivedSet(PDU pdu)
    {
 		PDU response = getResponsePDU(pdu);
 		// the modified OID entries so far.  
 		HashSet<VariableBinding> modified = new HashSet<VariableBinding>(); 
 		int errorIndex = 1;
 		Variable var, oldVar = null; // oldVar variable for storing into modified
 		
 		final boolean trace = log.isTraceEnabled();
 		// TODO: why is this before a null check? if the pdu is null, pdu.getRequestID() and pdu.size() will both fail.
 		if (trace) {
 			log.trace("requestID=" + pdu.getRequestID() + ", elementCount="
 					+ pdu.size());
 		}
 		
 		//iterate through the VB in this PDU
 		Iterator<VariableBinding> it = pdu.getVariableBindings().iterator();
 		
 		while (it.hasNext()){
 			// if any set fails for any reason, changes must be undone. 
 			// so changes that pass all tests should be stored, and all applied
 			// after each VB has been checked. 
 			VariableBinding vb = it.next();
 			OID oid = vb.getOid();
 			Variable newVal = vb.getVariable();
 			//setup the new variable binding to put into the response pdu
 			VariableBinding newVB = new VariableBinding(oid,newVal);
 			
 			try{
 				oldVar = getValueFor(oid);
 				modified.add(new VariableBinding(oid, oldVar)); // keep a record of the old variable binding.
 				if (checkObject(oid)){
 					var = setValueFor(oid,newVal);
 				}
 				else{
 					log.debug("snmpReceivedSet: no object for SET request");
 					undoSets(modified);
 					makeErrorPdu(response, pdu, errorIndex, PDU.noAccess);
 					return response;
 				}
 			}
 			catch (NoSuchInstanceException e){
 				log.debug("snmpReceivedSet: attempt to set a non-existent instance: " + oid.last() + " of object: " + oid.trim());
 				undoSets(modified);
 				makeErrorPdu(response, pdu, errorIndex, PDU.noCreation);
 				return response;
 			}
 			catch (VariableTypeException e){
 				log.debug("snmpReceievedSet: could not convert the given value into an appropriate type: " +newVal);
 				undoSets(modified);
 				makeErrorPdu(response, pdu, errorIndex, PDU.wrongType);
 				return response;
 			}
 			catch (ReadOnlyException e){
 				log.debug("snmpReceivedSet: attempt to set a read-only attribute: " + newVB);
 				undoSets(modified);
 				makeErrorPdu(response, pdu, errorIndex, PDU.notWritable);
 				return response;
 			}
 			catch (Exception e){
 				log.debug("snmpReceivedSet: catastrophe!!! General variable validation error." + " " + e);
 				e.printStackTrace();
 				undoSets(modified);
 				makeErrorPdu(response, pdu, errorIndex, PDU.genErr);
 				return response;
 			}
 			
 			// if we get here, we modified the value successfully. 
 			response.add(newVB);
 			errorIndex++;
 		}
 	return response;
    }		
 	
 	/**
 	 * This method returns the correct response PDU from the given request PDU
 	 * passed in parameters.
 	 */
 	private PDU getResponsePDU(PDU pdu) {
 		PDU response;						
 		if (pdu instanceof ScopedPDU){
 			/* ScopedPDUs are not fully supported. this check is here so that when
 			 * SnmpAgentService is updated to support v3 correctly, we won't have to 
 			 * add it in.*/
 			response = DefaultPDUFactory.createPDU(SnmpConstants.version3);			
 			/* A.3.4.  Applications that Send Responses
 			   The contextEngineID, contextName, securityModel, securityName,
 			   securityLevel, and stateReference parameters are from the initial
 			   processPdu primitive.  The PDU and statusInformation are the results
 			   of processing.*/
 			((ScopedPDU)response).setContextEngineID(((ScopedPDU) pdu).getContextEngineID());
 			((ScopedPDU)response).setContextName(((ScopedPDU) pdu).getContextName());
 		} else if (pdu instanceof PDUv1){
 			if(pdu.getType() == PDU.GETBULK) {
 				log.debug("snmpReceievedGetBulk: cannot getBulk with V1 PDU.");
 				return null;			
 			}
 			// v1
 			response = DefaultPDUFactory.createPDU(SnmpConstants.version1);
 		} else {
 			// v2c
 			response = new PDU();
 		}
 		response.setType(PDU.RESPONSE);
 		return response;
 	}	
 	
 	/**
 	 * <P>
 	 * This method is defined to handle SNMP requests that are received by the
 	 * session. The parameters allow the handler to determine the host, port,
 	 * and community string of the received PDU
 	 * </P>
 	 * 
 	 * @param session
 	 *            The SNMP session
 	 * @param manager
 	 *            The remote sender
 	 * @param port
 	 *            The remote senders port
 	 * @param community
 	 *            The community string
 	 * @param pdu
 	 *            The SNMP pdu
 	 * 
 	 */
 	public void snmpReceivedPdu(Snmp session, InetAddress manager,
 			int port, OctetString community, PDU pdu)
    {
 //		log.error("Message from manager " + manager.toString() + " on port " + port);
 //		int cmd = pdu.getCommand();
 //		log.error("Unsupported PDU command......... " + cmd);
 	}
 
 	/**
 	 * <P>
 	 * This method is invoked if an error occurs in the session. The error code
 	 * that represents the failure will be passed in the second parameter,
 	 * 'error'. The error codes can be found in the class SnmpAgentSession
 	 * class.
 	 * </P>
 	 * 
 	 * <P>
 	 * If a particular PDU is part of the error condition it will be passed in
 	 * the third parameter, 'pdu'. The pdu will be of the type SnmpPduRequest or
 	 * SnmpPduTrap object. The handler should use the "instanceof" operator to
 	 * determine which type the object is. Also, the object may be null if the
 	 * error condition is not associated with a particular PDU.
 	 * </P>
 	 * 
 	 * @param session
 	 *            The SNMP Session
 	 * @param error
 	 *            The error condition value.
 	 * @param ref
 	 *            The PDU reference, or potentially null. It may also be an
 	 *            exception.
 	 */
 	public void SnmpAgentSessionError(Snmp session, int error, Object ref)
    {
 //		log.error("An error occured in the trap session");
 //		log.error("Session error code = " + error);
 //		if (ref != null)
 //      {
 //			log.error("Session error reference: " + ref.toString());
 //		}
 //
 //		if (error == SnmpAgentSession.ERROR_EXCEPTION)
 //      {
 //			synchronized (session)
 //         {
 //				session.notify(); // close the session
 //			}
 //		}
 	}
 
    // Private -------------------------------------------------------
    
 	/**
 	 * Initialize the bindings from the file given in resourceName
 	 */
 	private void initializeBindings() throws Exception
    {
       log.debug("Reading resource: '" + resourceName + "'");
       
       ObjectModelFactory omf = new AttributeMappingsBinding();
       InputStream is = null;
       AttributeMappings mappings = null;
       try
       {
          // locate resource
          is = SecurityActions.getThreadContextClassLoaderResource(resourceName);
          
          // create unmarshaller
          Unmarshaller unmarshaller = UnmarshallerFactory.newInstance().newUnmarshaller();
 
          // let JBossXB do it's magic using the AttributeMappingsBinding
          mappings = (AttributeMappings)unmarshaller.unmarshal(is, omf, null);         
       }
       catch (Exception e)
       {
          log.error("Accessing resource '" + resourceName + "'");
          throw e;
       }
       finally
       {
          if (is != null)
          {
             // close the XML stream
             is.close();            
          }
       }
       if (mappings == null)
       {
          log.warn("No bindings found in " + resourceName);
          return;         
       }
       log.debug("Found " + mappings.size() + " attribute mappings"); 		
 		/**
 		 * We have the MBeans now. Put them into the bindungs.
 		 */
 
 		Iterator it = mappings.iterator();
 		while (it.hasNext())
       {
 		   ManagedBean mmb = (ManagedBean)it.next();
 		   String oidPrefix = mmb.getOidPrefix();
 		   List attrs = mmb.getAttributes();
 		   Iterator aIt = attrs.iterator();
 		   while (aIt.hasNext())
 		   {
 			  Object check = aIt.next();
  
 			  
 			  MappedAttribute ma = (MappedAttribute)check;
 			  		  
 			  String oid;
 			  if (oidPrefix != null){
 				  oid = oidPrefix + ma.getOid();
 				  addObjectEntry(new OID(oidPrefix));
 			  }
 			  else{
 				  oid = ma.getOid();
 				  OID objectOID = new OID(oid);
       			  addObjectEntry(objectOID.trim());
 			  }
 			 
 			  addBindEntry(oid, mmb.getName(), ma.getName(),ma.isReadWrite());
 
 			  
   		   }
       }
    }
 	
 	/** This method adds a new ObjectEntry to the set of Object OIDs.
 	 * @param String representation of the OID to add. 
 	 * 
 	 * **/
 	private void addObjectEntry(OID oid){
 		if (objectKeys.contains(oid))
 			log.debug("duplicate object " + oid + SKIP_ENTRY);
 		
 		if (oid == null)
 			log.debug("null oid for object");
 		objectKeys.add(oid);
 				
 	}
 	
 	/** 
 	 * 
 	 * @param oid The OID bound to this particular attribute
 	 * @param mmb the name of the MBean server
 	 * @param ma the name of the MBeam attribute the OID is concerning
 	 * @param rw indicates whether this Attribute is read-write or not (readonly if false)
 	 */
 	
 	private void addBindEntry(String oid, String mmb, String ma, boolean rw){
 	  BindEntry be = new BindEntry(oid, mmb, ma);
 	  be.isReadWrite = rw;
 	  
 	  OID coid = new OID(oid);
 	  if (log.isTraceEnabled())
 		  log.trace("New bind entry   " + be);
 	  if (bindings.containsKey(coid)) {
 		  log.info("Duplicate oid " + oid + SKIP_ENTRY);
 	  }
 	  if (mmb == null || mmb.equals(""))
 	  {
 		  log.info("Invalid mbean name for oid " + oid + SKIP_ENTRY);
 	  }
 	  if (ma == null || ma.equals(""))
 	  {
 		  log.info("Invalid attribute name " + ma + " for oid " + oid + SKIP_ENTRY);
 	  }
 	  bindings.put(coid, be);
 	  oidKeys.add(coid);
 
 	}
 	
 	/**
 	 * This method checks the existence of an object of a GET / SET request.  
 	 * This is as opposed to a specific instance, which has a value tied to it. this method simply
 	 * returns a boolean.
 	 * 
 	 * @param oid
 	 * @return
 	 * @throws NoSuchObjectException
 	 * @throws VariableTypeException
 	 */
 	
 	private boolean checkObject(final OID oid) {
 		OID coid = oid.trim(); 
 		return objectKeys.contains(coid);
 	}	
 	
 	/**
 	 * Return the current value for the given oid
 	 * 
 	 * @param oid
 	 *            The oid we want a value for
 	 * @return Null if no value present
 	 */
 	private Variable getValueFor(final OID oid) throws NoSuchInstanceException, VariableTypeException {
 
 		BindEntry be = findBindEntryForOid(oid);
 		Variable ssy = null;
 		if (be != null)
      {
 			if (log.isTraceEnabled())
 				log.debug("getValueFor: Found entry " + be.toString() + " for oid " + oid);
         
 			try
         {
 			   Object val = server.getAttribute(be.mbean, be.attr.getName());
 			   ssy = prepForPdu(val);
 		}
 		catch (VariableTypeException e){
 			log.debug("getValueFor: didn't find a suitable data type for the requested data");
 			throw e;
 		}
         catch (Exception e)
         {
 				log.warn("getValueFor: (" + be.mbean.toString() + ", "
 						+ be.attr.getName() + ": " + e.toString());
         }
      }
      else
      {		
     	 	
 			log.debug("getValueFor: " + NO_ENTRY_FOUND_FOR_OID + oid);
 			throw new NoSuchInstanceException();
 		}
 		return ssy;
 	}
 
 	
 	
 	/**This method takes an Object that is typically going to be 
 	 * put into a VariableBinding for use in a PDU, and thus must be converted
 	 * into an SNMP type based on it's type. This Object is usually read 
 	 * from the MBean server.
 	 *  
 	 * @param val The value needing conversion. 
 	 * @return the converted value. null on failure.
 	 * @throws VariableTypeException if the method was unable to convert val's type into an 
 	 * equivalent SMI type.
 	 * 
 	 */
 	
 	private Variable prepForPdu(final Object val) throws VariableTypeException{
 		Variable result = null;
 		//TODO: all types managed by the PDU
 		if (val instanceof Long)
         {
 			result = new OctetString(((Long)val).toString());
 		}
 		else if (val instanceof Boolean)
         {
 			if(((Boolean)val).booleanValue())
 				result = new Integer32(1);
 			else 
 				result = new Integer32(0);
 		}
         else if (val instanceof String)
         {
         	result = new OctetString((String) val);
 		}
         else if (val instanceof Integer)
         {
 			result = new Integer32((Integer)val);
 		}
         else if (val instanceof OID)     
         {
         	result = new OID((OID)val);
         }
         else if (val instanceof TimeTicks)
         {
         	// the SNMP4J class TimeTicks default toString method is formatted horribly. This 
         	// call emulates the joesnmp SnmpTimeTicks display.
        	result = new OctetString(((TimeTicks)val).toString("{0} d {1} h {2} m {3} s {4} hs"));
         }
         else if (val instanceof Counter32)
         {
      	   	result = (Counter32) val;
         }
         else
         {
         	throw new VariableTypeException(); // no instance of an SNMP Variable could be created for type
         }
 		return result;
 	}	
 	
 	/** 
 	 * Takes an instance of org.snmp4j.smi.Variable and returns a Java primitive
 	 * representation. This is used to set mbean values given a PDU.
 	 * 
 	 * @param val the value to be converted
 	 * @return an Object created with the proper type. null if failure
 	 * @throws VariableTypeException if the method was unable to convert the type of val into
 	 * a type compatible with the MBean server.
 	 */
 	private Object convertVariableToValue(final Variable val, final Object attribute) throws VariableTypeException{
 		Object result = null;
 		if (val instanceof OctetString)
 		{	
 			if(attribute instanceof Long) {
 				result = Long.parseLong(val.toString());
 			} else {
 				result = val.toString();
 			}
 		}
 		else if (val instanceof Integer32)
 		{
 			if(attribute instanceof Boolean) {
 				if(((Integer32)val).getValue() == 0) {
 					result = Boolean.FALSE;
 				} else {
 					result = Boolean.TRUE;
 				}
 			} else {
 				result = Integer.valueOf(((Integer32)val).getValue());
 			}
 		}
 		else if (val instanceof Counter32)
 		{
 			result = Long.valueOf(((Counter32)val).getValue());
 		}
 		else if (val instanceof Counter64)
 		{
 			result = Long.valueOf(((Counter64)val).getValue());
 		}		
 		else{
 			throw new VariableTypeException(); //no instance could be created.
 		}
 		// TODO do more mumbo jumbo for type casting / changing
 		return result;
 		
 	}
 	
 //	/**
 //	 * Set a jmx attribute
 //	 * @param oid The oid to set. This is translated into a mbean / attribute pair
 //	 * @param newVal The new value to set
 //	 * @return null on success, non-null on failure
 //	 * @throws ReadOnlyException If the referred entry is read only.
 //	 */
 	
 	//TODO: currently, if one accesses a String and attempts to set it to an Int or Double value,
 	// the response is success, but the result is failure. This must be changed by investigating 
 	// what value is given from this method when such a situation arises, and then either throwing an exception 
 	// or handling that value in the caller. /** THIS HAS BEEN IMPLEMENTED. AN EXCEPTION SHOULD BE THROWN. **/
 	//TODO: decide what to do in the case of a non writeable entry. (Exception thrown)
 	
 	private Variable setValueFor(final OID oid, final Variable newVal) 
 	throws ReadOnlyException, VariableTypeException, NoSuchInstanceException {
 		final boolean trace = log.isTraceEnabled();
 		
 		BindEntry be = findBindEntryForOid(oid);
 		Variable ssy = null;
 		
 		if (trace)
 			log.trace("setValueFor: found bind entry for " + oid);
 		
 		if (be != null)
 		{
 			if (trace)
 				log.trace("setValueFor: " + be.toString());
          
 			if (be.isReadWrite == false)
 			{
 				if (trace)
 					log.trace("setValueFor: this is marked read only");
             
 				throw new ReadOnlyException(oid);
 			}
 			try
 			{		
 				Object other = server.getAttribute(be.mbean, be.attr.getName());
 				Object val = convertVariableToValue(newVal, other);
 				
 				if (val.getClass() != other.getClass() ){
 					log.debug("setValueFor: attempt to set an MBean Attribute with the wrong type.");
 					ssy = newVal;
 				}
 								
 				Attribute at = new Attribute(be.attr.getName(), val);
 				server.setAttribute(be.mbean, at);
 			
 				if (trace)
 					log.trace("setValueFor: set attribute in mbean server");
 			}
 			catch (VariableTypeException e){
 				log.debug("setValueFor: didn't find a suitable data type for newVal " + newVal);
 				throw e;
 			}
 			catch (Exception e)
 			{
 				log.debug("setValueFor: exception " + e.getMessage());
 				ssy = newVal;
 			}
 		}
 		else
 		{
 			throw new NoSuchInstanceException();
 			/*ssy = newVal;
 			log.info("setValueFor: " + NO_ENTRY_FOUND_FOR_OID + oid + " on the mbean server");*/
 		}
 		return ssy;
 	}
 
 
 	/** This method is used by snmpReceivedSet to reverse any changes in a 
 	 * SET PDU if an error is encountered before finishing.
 	 * 
 	 * @param modified HashSet containing OID,Val mappings
 	 */
 	private void undoSets(HashSet modified){
 		Iterator<VariableBinding> iter = modified.iterator();
 		
 		while (iter.hasNext()){
 			try{
 			VariableBinding vb = iter.next();
 			OID oid = vb.getOid();
 			Variable var = vb.getVariable();
 			setValueFor(oid,var);// this will not fail, because it succeeded earlier.
 			}
 			catch(NoSuchInstanceException e){
 				//impossible
 			}
 			catch(VariableTypeException e){
 				//impossible
 			}
 			catch(ReadOnlyException e){
 				//impossible;
 			}
 		}		
 	}
 	
 
 	/**
 	 * Lookup a BindEntry on the given oid. 
 	 * 
 	 * @param oid The oid look up.
 	 * @return a bind entry or null.
 	 */
 	private BindEntry findBindEntryForOid(final OID oid) {
 		
 		//param probably not supposed to be OID type
 		OID coid= new OID(oid);
 		
 		//add possible oid format checking
 		//we should not be stripping off the last number if it's 0! that's the instance identifier,
 		//and indicates that the given variable is a scalar value! (not part of a table.)
 		/*if (coid.last() == 0)
 		{
 			coid.removeLast();
 		}*/
 		BindEntry be = (BindEntry)bindings.get(coid);
 
 		return be;
 	}
 	
 //	/**
 //	 * Lookup a BinEntry on the given oid. If the oid ends in .0,
 //	 * then the .0 will be stripped of before the search.
 //	 * @param oid The oid look up.
 //	 * @return a bind entry or null.
 //	 */
 //	private BindEntry findBindEntryForOid(final SnmpObjectId oid) {
 //		
 //		ComparableSnmpObjectId coid= new ComparableSnmpObjectId(oid);
 //		
 //		if (coid.isLeaf())
 //		{
 //			coid = coid.removeLastPart();
 //		}
 //		BindEntry be = (BindEntry)bindings.get(coid);
 //
 //		return be;
 //	}
 
 	/**
 	 * Return the next oid that is larger than ours.
 	 * @param oid the starting oid
 	 * @return the next oid or null if none found.
 	 * 	 * @throws EndOfMibViewException if there is no greater OID than the given, valid, oid. 
 	 */
 	private OID getNextOid(final OID oid) throws EndOfMibViewException {
 		OID coid = new OID(oid);
 
 
 		SortedSet ret;
 		ret=oidKeys.tailSet(oid);  // get oids >= oid
 		Iterator it = ret.iterator();
 		OID roid=null;
 		
 		/*
 		 * If there are elements in the tail set, then
 		 * - get first one.
 		 * - if first is input (which it is supposed to be according to the contract of
 		 *   SortedSet.tailSet() , then get next, which is the 
 		 *   one we look for.
 		 */
 		if (it.hasNext()){
 			
 			roid = (OID)it.next(); // oid
 		}
 		// if the tailSet is empty...there aren't any OID larger than the one given. so we're at the End of the MIB.
 		else{ 
 			log.debug("getNextOid: Placeholder. There is no lexically larger OID than the input.");
 			throw new EndOfMibViewException();
 		}
 		
 		if (roid.compareTo(coid)==0) // input elment
 		{
 			// if there is a next element, then it is ours.
 			// perhaps change this to try/catch also
 			if (it.hasNext()) 
 			{
 				roid = (OID)it.next();
 			}
 			else
 			{
 				log.debug("getNextOid: Placeholder. There is no lexically larger OID than the input.");
 				// end of list
 				throw new EndOfMibViewException();
 			}
 		}
  		
 		// Check if still in subtree if requested to stay within
 		// THIS SHOULD BE A MANAGER FUNCTION, NOT AGENT.
 /*		if (stayInSubtree && roid != null)
 		{
 			//OID parent = coid.removeLast();
 			// this emulates the functionality of the "isRoot" in SnmpObjectId from joesnmp
 			if (coid.leftMostCompare((coid.size()-1), roid) != 0){
 				log.debug("getNextOid: Placeholder. The traversal has left the subtree.");
 				throw new EndOfMibViewException();
 			}
 		}*/
 
 		return roid;
 	}
 
 	/** This utility method is used to construct an error PDU. This code
 	 * was repeated so many times it was prudent to give it it's own method.
 	 * @param response This PDU is the one being modified into an error PDU.
 	 * @param oid The OID to contain the error Null instance.
 	 * @param errNo The error number defined in the PDU class that indicates a given failure
 	 * @param errInd the VariableBinding in the PDU that caused the error. 
 	 */
 	private void makeErrorPdu(PDU response, PDU pdu, int errorIndex, int err){
 		response.clear();
 		response.addAll(pdu.toArray());
 		response.setErrorIndex(errorIndex);
 		response.setErrorStatus(err);
 	}
 
    // Inner Class ---------------------------------------------------
    
 	/**
 	 * An entry containing the mapping between oid and mbean/attribute
 	 * 
 	 * @author <a href="mailto:pilhuhn@user.sf.net>">Heiko W. Rupp</a>
 	 */
 	private class BindEntry implements Comparable {
 		private final OID oid;
 
 		private ObjectName mbean;
 		private Attribute attr;
 		private String mName;
 		private String aName;      
 		private boolean isReadWrite = false;
 
 		/**
 		 * Constructs a new BindEntry
 		 * 
 		 * @param oid
 		 *            The SNMP-oid, this entry will use.
 		 * @param mbName
 		 *            The name of an MBean with attribute to query
 		 * @param attrName
 		 *            The name of the attribute to query
 		 */
 		BindEntry(final String oidString, final String mbName, final String attrName) {
 			this(new OID(oidString), 
 					mbName,
 					attrName);
 		}
 		
 		/**
 		 * Constructs a new BindEntry.
 		 * @param coid The SNMP-oid, this entry will use.
 		 * @param mbName The name of an MBean with attribute to query
 		 * @param attrName The name of the attribute to query
 		 */
 		BindEntry(final OID coid, final String mbName, final String attrName) {
 			oid = coid;
 			this.mName = mbName;
 			this.aName = attrName;
 			try
          {
 			   mbean = new ObjectName(mbName);
 				attr = new Attribute(attrName, null);
 
 			}
          catch (Exception e)
          {
             log.warn(e.toString());
 				mName = "-unset-";
 				aName = "-unset-";
 			}
 		}
 
 		/**
 		 * A string representation of this BindEntry
 		 */
 		public String toString() {
 			StringBuffer buf = new StringBuffer();
 			buf.append("[oid=");
 			buf.append(oid).append(", mbean=");
 			buf.append(mName).append(", attr=");
 			buf.append(aName).append(", rw=");
 			buf.append(isReadWrite).append("]");
 
 			return buf.toString();
 		}
 
 		public Attribute getAttr() {
 			return attr;
 		}
 
 		public ObjectName getMbean()
       {
 			return mbean;
 		}
 
 		public OID getOid()
       {
 			return oid;
 		}
 
 
 		/**
 		 * Compare two BindEntries. Ordering is defined at oid-level.
 		 * 
 		 * @param other
 		 *            The BindEntry to compare to.
 		 * @return 0 on equals, 1 if this is bigger than other
 		 */
 		public int compareTo(Object other)
       {
 			if (other == null)
 				throw new NullPointerException("Can't compare to NULL");
 
 			if (!(other instanceof BindEntry))
 				throw new ClassCastException("Parameter is no BindEntry");
 
 			// trivial case
 			if (this.equals(other))
 				return 0;
          
 			BindEntry obe = (BindEntry) other;
 //			if (getOid().equals(obe.getOid()))
 //				return 0;
 
 			int res =oid.compareTo(obe.getOid());
 			return res;
 		}
 
 	}
 
 	
 	
 }
 
 /** Brian Shim commented this out for reference i'm guessing **/
 //try
 //{
 //	SnmpPduRequest response = null;
 //	int pduLength = pdu.getLength();
 //	final boolean trace = log.isTraceEnabled();
 //
 //	if (trace)
 //		log.trace("requestId=" + pdu.getRequestId() + ", pduLength="
 //				+ pduLength + ", getNext=" + getNext);
 //
 //	SnmpVarBind[] vblist = new SnmpVarBind[pduLength];
 //	int errorStatus = SnmpPduPacket.ErrNoError;
 //	int errorIndex = 0;
 //
 //	// Process for each varibind in the request
 //	for (int i = 0; i < pduLength; i++)
 //	{
 //		boolean good = true;
 //		SnmpVarBind vb = pdu.getVarBindAt(i);
 //		SnmpObjectId oid = vb.getName();
 //START OF GETNEXT
 //		if (getNext) //i don't care about GETNEXT, doing GET first
 //		{
 //			/*
 //			 * We call getNextOid() to find out what is the next valid OID
 //			 * instance in the supported MIB (sub-)tree. Assign that OID to the
 //			 * VB List and then proceed same as that of get request. If the
 //			 * passed oid is already the last, we flag it.
 //			 */
 //			ComparableSnmpObjectId coid = new ComparableSnmpObjectId(oid);
 //			oid = getNextOid(coid, true);
 //			if (oid == null)
 //			{
 //				good = false;
 //			}
 //			else
 //			{
 //				pdu.setVarBindAt(i, new SnmpVarBind(oid));
 //			}
 //		}
 //End of GETNEXT
 //		if (oid!=null)
 //			vblist[i] = new SnmpVarBind(oid);
 //		else
 //			vblist[i] = new SnmpVarBind(vb.getName()); // oid passed in
 //		
 //
 //		if (trace)
 //			log.trace("oid=" + oid);
 //
 //		SnmpSyntax result = null;
 //		if (good && bindings != null)
 //			result = getValueFor(oid);
 //
 //		if (trace)
 //			log.trace("got result of " + result);
 //
 //		if (result == null || !good)
 //		{
 //			errorStatus = SnmpPduPacket.ErrNoSuchName;
 //			errorIndex = i + 1;
 //			log.debug("Error Occured " + vb.getName().toString());
 //		} 
 //		else
 //		{
 //			vblist[i].setValue(result);
 //			log.debug("Varbind[" + i + "] := "
 //							+ vblist[i].getName().toString());
 //			log.debug(" --> " + vblist[i].getValue().toString());
 //		}
 //	} // for ...
 //	response = new SnmpPduRequest(SnmpPduPacket.RESPONSE, vblist);
 //	response.setErrorStatus(errorStatus);
 //	response.setErrorIndex(errorIndex);
 //	return response;
 //} catch (Exception e)
 //{
 //	// TODO Auto-generated catch block
 //	e.printStackTrace();
 //	return null;
 //}
