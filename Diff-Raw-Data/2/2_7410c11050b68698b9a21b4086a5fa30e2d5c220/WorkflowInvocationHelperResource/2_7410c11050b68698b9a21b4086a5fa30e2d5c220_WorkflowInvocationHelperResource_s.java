 package org.cagrid.workflow.helper.invocation.service.globus.resource;
 
 import java.rmi.RemoteException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.ListIterator;
 import java.util.Map;
 import java.util.Set;
 import java.util.Map.Entry;
 import java.util.concurrent.locks.Condition;
 import java.util.concurrent.locks.Lock;
 import java.util.concurrent.locks.ReentrantLock;
 
 import javax.xml.namespace.QName;
 import javax.xml.soap.SOAPElement;
 import javax.xml.soap.Text;
 
 import org.apache.axis.message.MessageElement;
 import org.apache.axis.message.addressing.EndpointReference;
 import org.apache.axis.message.addressing.EndpointReferenceType;
 import org.apache.axis.types.URI.MalformedURIException;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.cagrid.workflow.helper.descriptor.EventTimePeriod;
 import org.cagrid.workflow.helper.descriptor.InputParameter;
 import org.cagrid.workflow.helper.descriptor.InputParameterDescriptor;
 import org.cagrid.workflow.helper.descriptor.OperationInputMessageDescriptor;
 import org.cagrid.workflow.helper.descriptor.OperationOutputTransportDescriptor;
 import org.cagrid.workflow.helper.descriptor.OutputReady;
 import org.cagrid.workflow.helper.descriptor.Status;
 import org.cagrid.workflow.helper.descriptor.TimestampedStatus;
 import org.cagrid.workflow.helper.descriptor.WorkflowInvocationHelperDescriptor;
 import org.cagrid.workflow.helper.instance.service.globus.resource.CredentialAccess;
 import org.cagrid.workflow.helper.instrumentation.InstrumentationRecord;
 import org.cagrid.workflow.helper.invocation.DeliveryEnumerator;
 import org.cagrid.workflow.helper.invocation.client.WorkflowInvocationHelperClient;
 import org.cagrid.workflow.helper.util.ConversionUtil;
 import org.cagrid.workflow.helper.util.ServiceInvocationUtil;
 import org.globus.gsi.GlobusCredential;
 import org.globus.wsrf.NotifyCallback;
 import org.globus.wsrf.ResourceException;
 import org.globus.wsrf.container.ContainerException;
 import org.w3c.dom.Node;
 
 
 /** 
  * The implementation of this WorkflowInvocationHelperResource type.
  * 
  * @created by Introduce Toolkit version 1.2
  * 
  */
 public class WorkflowInvocationHelperResource extends WorkflowInvocationHelperResourceBase implements NotifyCallback {
 
 	private static Log logger = LogFactory.getLog(WorkflowInvocationHelperResource.class);
 
 
 
 	private QName outputType = null;
 	private boolean outputIsArray;
 	private WorkflowInvocationHelperDescriptor operationDesc = null;
 	private OperationInputMessageDescriptor input_desc = null;
 	private OperationOutputTransportDescriptor output_desc = null;
 	private InputParameter[] paramData = new InputParameter[0];
 	private CredentialAccess credentialAccess;      // Interface to retrieve GlobusCredential from the InstanceHelper (necessary to invoke secure operations)
 	private EndpointReference serviceOperationEPR;  // EPR of this instance. Used as key to retrieve GlobusCredential from the InstanceHelper
 	private String serviceOperationEPRString;
 	private boolean isSecure = false;    // Enable/Disable secure invocation
 	private boolean waitExplicitStart = true;   // true: method 'start' initiates execution. false: execution starts as soon as all parameters are set
 	private boolean isReceivingStream = false;  // State variable to control the read from a stream
 
 
 	// Synchronization variables
 	private Map<String, Lock> outputReadyKey = new HashMap<String, Lock>();
 	private Map<String, Condition> outputReadyCondition = new HashMap<String, Condition>();
 	private Map<String, Boolean> outputReady = new HashMap<String, Boolean>();
 
 	// Instrumentation information
 	private InstrumentationRecord step_times;
 
 	
 	// True when InvocationHelper execution already started
 	private boolean alreadyStarted = false;
 
 	public synchronized boolean executeIfReady() {
 
 
 		// Make sure all expected parameters have been retrieved before executing 
 		if( !allParametersSet() ){ 
 
 			// Generate a callback so the caller can proceed
 			try {
 				this.setOutputReady(OutputReady.FALSE);
 			} catch (ResourceException e) {
 				e.printStackTrace();
 			}
 			return false; 
 		}
 
 		logger.info("Execution started for "+ getOperationDesc().getOperationQName().getLocalPart()); 
 
 		this.changeStatus(Status.RUNNING);
 		try {
 			this.setOutputReady(OutputReady.FALSE);  // Generate a callback so the caller can proceed
 		} catch (ResourceException e2) {
 			e2.printStackTrace();
 			logger.error(e2);
 		}
 
 		final Thread th = new Thread(new Runnable() {
 
 			public synchronized void run() {
 
 
 				logger.info("-- Thread started --");
 
 				// we have all the input data needed to execute so lets execute
 				// 1. make execution call with axis
 				List<Node> service_response = new ArrayList<Node>();
 				try {
 
 
 					logger.info("Operation: "+ getOperationDesc().getOperationQName()); 
 
 					final boolean invocationIsSecure = isSecure();  
 					logger.info("[RUNNABLE] Blocking until credential is provided");
 					GlobusCredential credential = invocationIsSecure ? getCredential() : null;
 
 					logger.info("[RUNNABLE] Retrieved credential: "+ credential); 
 
 					InputParameterDescriptor[] input_desc = getInput_desc().getInputParam();
 					InputParameter[] input_value = getParamData();
 
 					boolean parameterIsArray = false;
 					boolean dataIsArray = false; 
 					boolean serviceAlreadyInvoked = false;
 
 
 					/* Inspect each parameter so we can determine what we're supposed to do with the provided values */
 					EndpointReferenceType operationEpr = new EndpointReference(getOperationDesc().getServiceURL());
 					WorkflowInvocationHelperClient operationClient = new WorkflowInvocationHelperClient(operationEpr);
 
 					EndpointReference enclosingInvocationHelperEPR = serviceOperationEPR;
 					WorkflowInvocationHelperClient enclosingInvocationHelperClient = new WorkflowInvocationHelperClient(enclosingInvocationHelperEPR);
 
 					System.out.println("Operation EPR: "+ operationEpr);
 
 					for(int input = 0; input < input_value.length; input++){
 
 						dataIsArray = parameterIsArray = false;
 
 						final int paramIndex = input_value[input].getParamIndex();
 						parameterIsArray = input_desc[paramIndex].getParameterIsArray();
 						final String paramData = input_value[input].getData();
 
 						// Verify whether the soap object represents an array or not
 						dataIsArray = DataIsArray(paramData);
 
 						logger.debug("Parameter is array = "+ parameterIsArray + " ; data is array = "+ dataIsArray);
 
 						// If data is array and parameter is not, we need to generate one request for each array element 
 						if( dataIsArray && !parameterIsArray ){ 
 
 
 							// Extract the array elements from the received data and forward each one to its appropriate destination
 							List<String> array_elements = getArrayElementsFromData(paramData);
 							ListIterator<String> array_elements_iter = array_elements.listIterator();
 
 
 							// Let the next stage know we will start streaming output to it 
 							enclosingInvocationHelperClient.startStreaming();
 
 
 							while( array_elements_iter.hasNext() ){ 
 
 
 								String curr_array_str = array_elements_iter.next();
 
 
 								if( !array_elements_iter.hasNext() ){
 
 									// Let the next stage know we will stop streaming output to it
 									enclosingInvocationHelperClient.endStreaming();		
 								}
 
 
 								// Create new inputs, with the original input value substituted for a new one (only the current array element as data)
 								InputParameter[] new_input_params = input_value.clone();
 								new_input_params[input].setData(curr_array_str);
 
 
 								// Invoke service according to its security configuration 
 								Node response_node = null;
 								if( invocationIsSecure ){
 
 									response_node = ServiceInvocationUtil.generateSecureRequest(getOperationDesc(), getInput_desc(), getOutput_desc(), 
 											new_input_params, getCredential(), enclosingInvocationHelperClient); 							
 								}
 								else {
 
 									response_node = ServiceInvocationUtil.generateUnsecureRequest(getOperationDesc(), getInput_desc(), getOutput_desc(), 
 											new_input_params, enclosingInvocationHelperClient);									
 								}
 								service_response.add(response_node);
 								serviceAlreadyInvoked = true;
 							}							
 						}
 					}
 
 
 					if( !serviceAlreadyInvoked ){  // Usual service invocation
 
 						logger.info("Streaming not applicable");
 
 						/* Invoke service according to its security configuration */
 						if( invocationIsSecure ){
 
 							logger.info("Invoking secure service");
 
 							service_response.add(ServiceInvocationUtil.generateSecureRequest(getOperationDesc(), getInput_desc(), getOutput_desc(), 
 									input_value, getCredential(), operationClient));
 						}
 						else {
 
 							logger.info("Invoking non-secure service"); 
 							service_response.add(ServiceInvocationUtil.generateUnsecureRequest(getOperationDesc(), getInput_desc(), getOutput_desc(),
 									input_value, operationClient));
 						}
 
 					}
 
 				} catch (Exception e1) {
 					System.err.println("ERROR processing " + getOperationDesc().getOperationQName() + " : " + e1.getMessage());					
 					changeStatus(Status.ERROR);
 					return;					
 				}
 
 
 				// Invocation done: change the status to "delivering output"				 
 				changeStatus(Status.GENERATING_OUTPUT);
 
 
 				// See list contents
 				Node curr_node = null;
 				logger.debug("----------------------------");
 				for(ListIterator<Node> it = service_response.listIterator(); it.hasNext(); ){
 					curr_node = it.next();
 					logger.debug("Curr node is: "+curr_node);
 				}
 				logger.debug("----------------------------");  // */
 
 
 								
 				/* Process each response and send the outputs to the appropriate service */
 				ListIterator<Node> service_response_iterator = service_response.listIterator();
 				while( service_response_iterator.hasNext() ){
 
 
 					final Node curr_response = service_response_iterator.next();
 
 					String node_string = ""+curr_response; // Don't delete the empty string! A compile-time error will occur!!
 
 
 					// 2. get result send parts where ever the transport descriptor
 					// tells me
 					org.cagrid.workflow.helper.descriptor.OperationOutputTransportDescriptor desc = getOutput_desc();
 
 					if(desc != null){
 
 						final int num_params = (desc.getParamDescriptor() != null)? desc.getParamDescriptor().length : 0; 
 
 						for (int i = 0; i < num_params; i++) {
 							
 
 							final org.cagrid.workflow.helper.descriptor.OperationOutputParameterTransportDescriptor pdesc = desc.getParamDescriptor(i);
 							try {
 								InputParameter iparam = new InputParameter();
 								iparam.setParamIndex(pdesc.getParamIndex());
 
 
 								// need to get that data out of the response;
 								// first, prepare all namespace mappings to the query
 								String data = null;
 								try {
 
 									data = ServiceInvocationUtil.applyXPathQuery(node_string, pdesc.getLocationQuery(), pdesc.getQueryNamespaces(), pdesc.getType());
 								} catch (Exception e) {
 									logger.error(e.getMessage(), e);
 									e.printStackTrace();
 								}
 								iparam.setData(data);
 
 
 								boolean outputIsArray = DataIsArray(data);  
 								boolean nextStageInputIsArray = pdesc.isExpectedTypeIsArray();  
 
 								logger.debug("[After getting operation's output] outputIsArray? "+ outputIsArray +". nextStageInputIsArray? "+ nextStageInputIsArray);
 								logger.debug("\tfor query '" + pdesc.getLocationQuery() + "' we got\t'"+ data +"'"); 
 
 								// send the data to the next workflow helper instance
 								if( pdesc.getDestinationEPR() != null ){
 									
 
 									EndpointReferenceType next_destination = null;  // next destination to forward data
 
 									/* If we can't do streaming, do usual forwarding. Otherwise, forward each output element to 
 									 * a destination following a delivery policy */
 									if( !outputIsArray || nextStageInputIsArray ){    
 
 
 										// Do usual forwarding
 										logger.debug("Doing usual forwarding after getting operation's output");
 										next_destination = pdesc.getDestinationEPR()[0];  // This might change when we have multiple destinations
 										WorkflowInvocationHelperClient client = new WorkflowInvocationHelperClient(next_destination);
 										//										logger.info("Setting parameter to stage identified by "+ client.getEndpointReference());
 										client.setParameter(iparam);
 
 									}
 									else {  // Do streaming between stages 
 
 
 
 										// Enable streaming in the output recipient
 										logger.debug("Streaming output after getting operation's output");
 										next_destination = pdesc.getDestinationEPR()[0];  //  This might change when we have multiple destinations
 										WorkflowInvocationHelperClient client = new WorkflowInvocationHelperClient(next_destination);
 										client.startStreaming();
 										logger.debug("Streaming enabled");
 
 										// Subscribe to notifications of output availability
 										subscribeWithCallback(OutputReady.getTypeDesc().getXmlType(), client);  // The next method is asynchronous, so we need to register a callback
 										//client.start(); // No longer necessary
 
 
 										// Get array elements
 										List<String> array_elements = getArrayElementsFromData(data);
 										// Prepare for enumerate the destination of each array element
 										DeliveryEnumerator destinations_iter = new DeliveryEnumerator(pdesc.getDeliveryPolicy(), pdesc.getDestinationEPR());
 
 
 										// Iterate over the array elements' list, forwarding each one to a (possibly) different location
 										ListIterator<String> array_iter = array_elements.listIterator();
 										logger.debug("Iterating over each element of the output");
 										while( array_iter.hasNext() ){
 
 											String curr_array_element = array_iter.next();
 
 											if( !array_iter.hasNext() ){
 
 												// Disable streaming in the output recipient
 												client.endStreaming();
 
 												logger.debug("Disabling streaming"); 
 											} 
 
 											iparam.setData(curr_array_element);
 											logger.debug("Current array element sent"); 
 
 
 											// Get one of the possible destinations according to the delivery policy
 											if( destinations_iter.hasNext() ){
 												next_destination = destinations_iter.next();
 											}
 											else {
 												logger.error("[executeIfReady] No destination could be retrieved");
 												break;
 											}
 
 											// Send the data to the appropriate InvocationHelper 
 											client.setParameter(iparam);
 
 
 											/* Wait for the callback to be made. 
 											 * NOTE: since we are streaming a set of data elements to the same stage 
 											 * AND input parameter, we are required to block until a callback is received.
 											 * However, when several data elements are sent to potentially different stages 
 											 * and/or input parameters, we MUST NOT block but proceed to send remaining 
 											 * data elements, otherwise a deadlock may occur. One such deadlock situation would
 											 * happen when one stage is responsible for giving more than one of a stage's input 
 											 * parameters.
 											 * */
 											waitForCallback(client.getEPRString());
 									
 										} // End of array elements										
 									}
 								}
 								else {
 									
 									logger.error("No destination assigned to current parameter (in "+ getOperationDesc().getOperationQName() +").");
 									logger.error("Value of parameter is: \n"+iparam.getData());
 									System.err.flush();
 								}
 
 							} catch (MalformedURIException e) {
 								changeStatus(Status.ERROR);	
 								e.printStackTrace();
 
 							} catch (RemoteException e) {
 								changeStatus(Status.ERROR);
 								e.printStackTrace();
 							} 
 						}
 					}
 
 				}
 				try {
 					// Calculate the next status and change the status of the execution. Note: when 
 					// reading from a stream, the stage won't terminate until the end of the stream is reached.  
 					finishRun();						
 				} catch (ResourceException e) {
 					logger.error(e.getMessage(), e);
 					e.printStackTrace();
 				} catch (Exception e) {
 					logger.error(e.getMessage(), e);
 					e.printStackTrace();
 				}
 
 				logger.info("-- Thread finished --");
 				return;
 			}
 
 		});
 
 
 		/* Start thread and wait for it to finish */
 		try {
 
 			th.start();
 			th.join();
 
 			if( this.getTimestampedStatus().getStatus().equals(Status.RUNNING) ){
 
 				this.finishRun();
 			}
 		} 
 		catch (InterruptedException e) {
 
 			e.printStackTrace();
 			changeStatus(Status.ERROR);
 
 		} catch (ResourceException e) {
 			e.printStackTrace();
 			changeStatus(Status.ERROR);			
 		}
 		catch (Throwable e) {
 			e.printStackTrace();
 			changeStatus(Status.ERROR);
 		}
 		logger.info("END");
 
 		return true;
 	}
 
 
 	/** Subscribe for receiving notifications of a certain type and register the subscription internally */
 	private void subscribeWithCallback(QName xmlType,
 			WorkflowInvocationHelperClient client) {
 
 		try{
 
 			// Register the subscription internally
 			String key = client.getEPRString();
 			if(key == null){
 				System.err.println("[InvocationHElperResource::subscribeWithCallback] ERROR: Unable to retrieve EPR String");
 			}
 			this.outputReady.put(key, Boolean.FALSE);
 			Lock mutex = new ReentrantLock();
 			Condition condition = mutex.newCondition();
 			this.outputReadyKey.put(key, mutex);
 			this.outputReadyCondition.put(key, condition);
 
 
 			// Subscribe
 			client.subscribeWithCallback(xmlType, this);
 
 		} catch (RemoteException e) {
 			logger.error(e);
 			e.printStackTrace();
 		} catch (ContainerException e) {
 			logger.error(e);
 			e.printStackTrace();
 		} catch (MalformedURIException e) {
 			logger.error(e);
 			e.printStackTrace();
 		} 
 	}
 
 
 	// Wait on condition variable until a notification is received and we can proceed
 	private void waitForCallback(String key) {
 
 		if( !this.outputReadyKey.containsKey(key) ){
 			logger.error("Unknown key received: "+ key);
 			return;
 		}
 
 		logger.info("Waiting for callback");
 		this.outputReadyKey.get(key).lock();
 		try{
 
 			if(!this.outputReady.get(key).booleanValue()){
 
 				this.outputReadyCondition.get(key).await();
 			}
 
 		} catch (InterruptedException e) {
 			e.printStackTrace();
 		} finally{
 			this.outputReadyKey.get(key).unlock();
 		}
 		logger.info("Callback received");
 	}
 
 
 
 
 	/**
 	 * Change the state of a particular execution to finished. Specifically, the status will be set to FINISHED unless we have to allow streaming.  
 	 * In the latter case, the status will be reset to "waiting for inputs" (or "ready for execution", when there are no inputs to wait for).
 	 * 
 	 * */
 	private void finishRun() throws ResourceException {
 
 		Status nextStatus = this.isReceivingStream ? Status.WAITING : Status.FINISHED;
 		changeStatus(nextStatus);
 		logger.info("[finishRun] Set status to "+ nextStatus +" ("+ getOperationDesc().getOperationQName() +")");
 
 		// Generate event that will dispatch all callbacks
 		System.out.println("[finishRun] Output is ready"); //DEBUG
 		this.setOutputReady(OutputReady.TRUE);
 	}
 
 
 	private boolean allParametersSet() {
 
 		for (int i = 0; i < paramData.length; i++) {
 			if (paramData[i] == null) {
 				return false;
 			}
 		}
 		return true;
 	}
 
 
 	private int numSetParameters() {
 
 		int received_values = 0;
 
 		for (int i = 0; i < paramData.length; i++) {
 			if (paramData[i] != null) {
 				received_values++;
 			}
 		}
 		return received_values;
 	}
 
 
 	private int numParameters(){
 		return paramData.length;
 	}
 
 
 	/** Get an array represented as a SOAP element and extracts the array elements
 	 * 
 	 * @param paramData The SOAP element whose child is an array
 	 * @return A list with all array elements represented as a String
 	 *  
 	 *  */
 	private List<String> getArrayElementsFromData(String paramData) {
 
 
 		List<String> output_elements = new ArrayList<String>(); 
 
 		Iterator array = ConversionUtil.String2SOAPElement(paramData);
 		SOAPElement enclosing_tag = null;
 		if( array.hasNext() ){
 			enclosing_tag = (SOAPElement) array.next();
 		}
 
 		// Extract each element and generate a request to the service
 		Iterator array_elements = enclosing_tag.getChildElements();
 		while( array_elements.hasNext() ){
 
 
 			Object curr_array_element = array_elements.next();
 			String curr_array_str = null;
 
 			if(curr_array_element instanceof org.apache.axis.message.Text){
 				Text txt = (Text) curr_array_element;
 				curr_array_str = txt.getNodeValue();
 				output_elements.add(curr_array_str);
 			}
 
 			// 2nd case) Parameter is represented as a list of attributes (complex type, simple type array, complex type array)
 			else if( curr_array_element instanceof javax.xml.soap.SOAPElement ){
 
 				SOAPElement tree = (SOAPElement) curr_array_element;
 				curr_array_str = ""+tree;
 				output_elements.add(curr_array_str);
 			} 
 		}
 
 		return output_elements;
 	}
 
 
 
 	private boolean DataIsArray(String paramData) {
 
 		boolean dataIsArray = false;
 		Iterator data_soap_iterator = ConversionUtil.String2SOAPElement(paramData);
 
 		while(data_soap_iterator.hasNext()){
 
 			Object curr_obj = data_soap_iterator.next();
 
 			if( curr_obj instanceof javax.xml.soap.SOAPElement ){
 
 
 				SOAPElement enclosing_tag = (SOAPElement) curr_obj; // This object represent a tag like <Response/>
 
 				Iterator array_elements = enclosing_tag.getChildElements(); 
 				if( array_elements.hasNext() ){
 
 
 					Object curr_elem = array_elements.next();
 
 					if( curr_elem instanceof javax.xml.soap.SOAPElement ){
 
 						SOAPElement first_element = (SOAPElement) curr_elem;
 						final String first_element_name = first_element.getLocalName();
 
 						if( array_elements.hasNext() ){ // Arrays must have two elements with same name, so let's check this
 
 							Object next_obj = array_elements.next();
 
 
 							SOAPElement second_element = (SOAPElement) next_obj;
 							final String second_element_name = second_element.getLocalName();
 
 							if(first_element_name.equals(second_element_name)){
 
 								dataIsArray = true;
 								break;
 							}
 						}
 					}
 				}
 			}
 		} 
 
 		return dataIsArray;
 	}
 
 
 
 	/** The method below is used for debugging purposes  */
 	/*private static void printParameters(InputParameter[] paramData){
 
 
 		String output = "";
 		output += "INPUT RECEIVED [printParameters]:\n";
 		for(int i=0; i < paramData.length; i++){
 			output += '['+paramData[i].getData()+" : "+paramData[i].getParamIndex()+"]\n";
 		}
 		output += "END PRINT PARAMETERS\n";
 		logger.info(output);
 		System.out.flush();
 		return;
 	} // */
 
 
 	public void initializeInstrumentationRecord(String stageGUID){
 
 		this.step_times  = new InstrumentationRecord(stageGUID);
 		try {
 			this.step_times.eventStart(Status.UNCONFIGURED.toString());
 		} catch (Exception e) {
 			logger.error(e.getMessage(), e);
 			e.printStackTrace();
 		}
 	} 
 
 
 	public synchronized void setParameters(InputParameter[] params) {
 		for (int i = 0; i < params.length; i++) {
 			setParameter(params[i]);
 		}
 	}
 
 
 	public synchronized void setParameter(InputParameter param) {
 
 		logger.info("BEGIN setParameter");  
 
 		Status curr_status = this.getTimestampedStatus().getStatus();
 		logger.info("status is "+curr_status);
 
 		if(curr_status.equals(Status.WAITING) || curr_status.equals(Status.FINISHED) || curr_status.equals(Status.READY) ){
 
 			// Warn about the overwriting that is about to be performed 
 			if(curr_status.equals(Status.READY)){
 				logger.warn("[Operation "+ this.getOperationDesc().getOperationQName() +" ; parameter index "+ param.getParamIndex() 
 						+"] All parameters were already set. Parameter "+ param.getParamIndex() +" is being overriden"); 
 			}
 
 
 			if (param != null) {
 				paramData[param.getParamIndex()] = param;
 			}
 
 			System.out.println("[setParameter] Received parameter "+ this.numSetParameters() +" of "+ this.numParameters() + " for "+ this.operationDesc.getOperationQName()); //DEBUG
 
 			// If all parameters are already set, new status is READY do execute
 			if(  this.allParametersSet() ){
 
 				changeStatus(Status.READY);
 				logger.info("[setParameter] Status is READY for "+ this.getOperationDesc().getOperationQName());				
 
 
 
 				if( !this.waitExplicitStart ){  // Run stage. There will be a callback in the end of the execution
 
 					// If ready to run, start the stage's execution and return. The method returns before the stage's execution finishes
 					logger.info("Starting stage execution and returning");
 					System.out.println("Starting stage execution and returning"); //DEBUG
 
 					final Thread th = new Thread(new Runnable(){
 
 						public synchronized void run() {
 
 
 							// BEGIN DEBUG
 							//							System.out.println("[1] BEGIN Printing environment variables");
 							//							System.out.println("GLOBUS_LOCATION = "+ System.getProperty("GLOBUS_LOCATION"));
 							//							System.out.println("[1] END Printing environment variables");
 							// END DEBUG
 
 
 							executeIfReady();   // poll to see if we can execute
 						}
 					});
 
 					th.start();
 
 				}
 				else{  // Generate a callback so the caller can proceed, since the stage won't run right now 
 
 					try {
 						this.setOutputReady(OutputReady.FALSE);
 					} catch (ResourceException e) {
 						e.printStackTrace();
 					}
 
 				}
 			}
 			else{  // Generate a callback so the caller can proceed setting input parameters
 				try {
 					System.out.println("Postponing execution until all inputs are set"); //DEBUG
 					this.setOutputReady(OutputReady.FALSE);
 				} catch (ResourceException e) {
 					e.printStackTrace();
 				}
 
 			}
 		}
 		else {
 			System.err.println("[Operation "+ this.getOperationDesc().getOperationQName() +" ; parameter index "+ param.getParamIndex() 
 					+"] setParameter is allowed only when state is WAITING or FINISHED. Current state: "+curr_status);
 
 			// Generate a callback so the caller can proceed setting input parameters
 			try {
 				this.setOutputReady(OutputReady.FALSE);
 			} catch (ResourceException e) {
 				e.printStackTrace();
 			}
 		}
 		logger.info("END setParameter");
 	}
 
 
 
 	public OperationInputMessageDescriptor getInput_desc() {
 		return input_desc;
 	}
 
 
 	public synchronized void setInput_desc(OperationInputMessageDescriptor input_desc) {
 
 		Status curr_status = this.getTimestampedStatus().getStatus();
 		logger.info("status is "+curr_status); 
 
 		if(curr_status.equals(Status.UNCONFIGURED)){
 
 			this.input_desc = input_desc;
 
 			// Prepare for receiving the arguments (if there's any)
 			final int num_params = (this.input_desc.getInputParam() != null)? this.input_desc.getInputParam().length : 0 ; 
 			this.paramData = new InputParameter[num_params];
 			changeStatus(Status.INPUTCONFIGURED);			
 		}
 		else {
 			logger.error("Input setting is allowed only when state is UNCONFIGURED. Current state: "+curr_status);
 		}
 	}
 
 
 	public OperationOutputTransportDescriptor getOutput_desc() {
 		return output_desc;
 	}
 
 
 	public synchronized void setOutput_desc(OperationOutputTransportDescriptor output_desc) {
 
 		Status curr_status = this.getTimestampedStatus().getStatus();
 		logger.info("BEGIN");
 		logger.info("status is "+curr_status); 
 
 
 
 		if(curr_status.equals(Status.INPUTCONFIGURED)){
 
 
 
 			this.output_desc = output_desc;
 			changeStatus(Status.WAITING);
 
 			// Skip the 'setParameter' step if we don't have any expected input. 
 			// Though, if the service is secure and the credential wasn't provided yet, a deadlock might occur
 			if(((this.getParamData() == null) || (this.getParamData().length == 0)) ){
 				logger.info("[setOutput_desc] No parameters needed, ready to execute");
 				changeStatus(Status.READY);
 				logger.info("[setOutput_desc] Status is READY for "+ this.getOperationDesc().getOperationQName()); 
 			} 
 
 		}
 		else {
 			System.err.println("Output setting is allowed only when state is INPUTCONFIGURED. Current state: "+curr_status);
 		}
 
 		logger.info("END"); 
 
 	}
 
 	public InputParameter[] getParamData() {
 		return paramData;
 	}
 
 
 	public synchronized void setParamData(InputParameter[] paramData) {
 		this.paramData = paramData;
 	}
 
 
 	public GlobusCredential getCredential() throws RemoteException {
 
 		GlobusCredential retval = this.getCredentialAccess().getCredential(this.serviceOperationEPR);
 		return retval;
 	}
 
 
 	public QName getOutputType() {
 		return outputType;
 	}
 
 
 	public void setOutputType(QName outputType) {
 		this.outputType = outputType;
 	}
 
 
 	public WorkflowInvocationHelperDescriptor getOperationDesc() {
 		return operationDesc;
 	} 
 
 
 	public void setOperationDesc(WorkflowInvocationHelperDescriptor operationDesc) {
 		this.operationDesc = operationDesc;
 		this.isSecure = (this.operationDesc.getWorkflowInvocationSecurityDescriptor() != null);
 	}
 
 
 
 
 	public CredentialAccess getCredentialAccess() {
 		return credentialAccess;
 	}
 
 
 
 
 	public void setCredentialAccess(CredentialAccess credentialAccess) {
 		this.credentialAccess = credentialAccess;
 	}
 
 
 
 
 	public String getServiceOperationEPRString(){
 		return serviceOperationEPRString;
 	}
 
 
 
 
 	public void setServiceOperationEPR(EndpointReference serviceOperationEPR) {
 		this.serviceOperationEPR = serviceOperationEPR;
 		this.serviceOperationEPRString = this.serviceOperationEPR.toString(); // This value is used as a GUID and should not be modified after this initialization
 	} // */
 
 
 
 
 	public boolean isSecure() {
 		return isSecure;
 	}
 
 
 
 
 	public void setSecure(boolean isSecure) {
 		this.isSecure = isSecure;
 	}
 
 
 
 
 	public EndpointReference getServiceOperationEPR() {
 		return serviceOperationEPR;
 	}
 
 
 
 
 	public void start() throws RemoteException {
 
 		
 		// Do nothing if this method was already called once
 		if(this.alreadyStarted){
 			
 			OutputReady currValue = this.getOutputReady();
 			this.setOutputReady(currValue);  // Send back a callback so the caller can proceed
 		}
 		
 		
 		
 		logger.info("STARTING execution for "+ getOperationDesc().getOperationQName().getLocalPart());
 
 		// If all parameters were set, start execution
 		if( this.getTimestampedStatus().getStatus().equals(Status.READY)){
 
 
 			Thread th = new Thread(new Runnable(){
 
				@Override
 				public void run() {
 
 					// BEGIN DEBUG
 					//					System.out.println("[2] BEGIN Printing environment variables");
 					//					System.out.println("GLOBUS_LOCATION = "+ System.getProperty("GLOBUS_LOCATION"));
 					//					System.out.println("[2] END Printing environment variables");
 					// END DEBUG
 
 
 					executeIfReady();
 				}
 			});
 
 			th.start();
 
 		}
 		else if( this.getTimestampedStatus().getStatus().equals(Status.WAITING) || this.getTimestampedStatus().getStatus().equals(Status.FINISHED)){
 
 			this.setOutputReady(OutputReady.FALSE);
 
 
 			// If the parameters are not set, prepare to start as soon as they are all set
 			logger.info("Postponing execution until all inputs are available");
 			System.out.println("Postponing execution until all inputs are available"); //DEBUG
 			this.waitExplicitStart  = false;		
 		}
 		else {
 			this.setOutputReady(OutputReady.FALSE);
 
 			throw new RemoteException("Method 'start' can only be invoked when status is WAITING or FINISHED. " +
 					"Current status is "+ this.getTimestampedStatus().getStatus().toString());
 		}
 	}
 
 
 
 
 	/* (non-Javadoc)
 	 * @see org.cagrid.workflow.helper.invocation.service.globus.resource.WorkflowInvocationHelperResourceBase#remove()
 	 */
 	@Override
 	public void remove() throws ResourceException {
 
 		logger.info("Destroying resource for "+ this.getOperationDesc().getOperationQName());
 
 		super.remove();
 
 		logger.info("Done");
 		return;
 	}
 
 
 
 	public void changeStatus(Status new_status){
 
 
 		// Store time for the recently finished step
 		try {
 			this.step_times.eventEnd(this.getTimestampedStatus().getStatus().toString());  // Mark the previous phase as finished
 
 
 			// Update current status
 			int nextTimestamp = this.getTimestampedStatus().getTimestamp() + 1;
 
 			this.setTimestampedStatus(new TimestampedStatus(new_status, nextTimestamp));
 			this.step_times.eventStart(new_status.toString());
 
 
 			if(new_status.equals(Status.FINISHED)){
 
 				this.step_times.eventEnd(Status.FINISHED.toString());	
 
 				// Copy instrumentation data to a resource property 
 				EventTimePeriod[] instrumentaion_data = this.step_times.retrieveRecordAsArray();
 				this.setInstrumentationRecord(new org.cagrid.workflow.helper.descriptor.
 						InstrumentationRecord(getOperationDesc().getOperationQName().toString(), instrumentaion_data));
 
 			}
 
 		} catch (ResourceException e) {
 			logger.error(e.getMessage(), e);
 			e.printStackTrace();
 		} catch (Exception e) {
 			logger.error(e.getMessage(), e);
 			e.printStackTrace();
 		}
 		logger.info("Set status to "+ new_status);
 	}
 
 
 
 	/**
 	 * @return the outputIsArray
 	 */
 	public boolean isOutputIsArray() {
 		return outputIsArray;
 	}
 
 
 
 
 	/**
 	 * @param outputIsArray the outputIsArray to set
 	 */
 	public void setOutputIsArray(boolean outputIsArray) {
 		this.outputIsArray = outputIsArray;
 	}
 
 
 
 
 	public void setReceivingStream() {
 
 		logger.info("Streaming started.."); 
 		this.isReceivingStream = true;
 	}
 
 
 
 
 	public void unsetReceivingStream() {
 
 		logger.info("Streaming ended..");
 		this.isReceivingStream = false;
 
 	}
 
 
 	public void deliver(List arg0, EndpointReferenceType arg1, Object arg2) {
 
 		org.oasis.wsrf.properties.ResourcePropertyValueChangeNotificationType changeMessage = ((org.globus.wsrf.core.notification.ResourcePropertyValueChangeNotificationElementType) arg2)
 		.getResourcePropertyValueChangeNotification();
 
 		MessageElement actual_property = changeMessage.getNewValue().get_any()[0];
 		QName message_qname = actual_property.getQName();
 		boolean isOutputReadyReport = message_qname.equals(OutputReady.getTypeDesc().getXmlType());
 		String stageKey = null;
 		try {
 			stageKey = new WorkflowInvocationHelperClient(arg1).getEPRString(); 
 		} catch (RemoteException e1) {
 			e1.printStackTrace();
 		} catch (MalformedURIException e1) {
 			e1.printStackTrace();
 		}   
 
 
 		/// Handle output availability reports
 		if(isOutputReadyReport){
 
 
 			if(!this.outputReadyKey.containsKey(stageKey)){
 				logger.error("Received notification from unregistered stage");
 				return;
 			}
 
 
 			OutputReady outputReady = null;
 			try {
 				outputReady = (OutputReady) actual_property.getValueAsType(message_qname, OutputReady.class);
 			} catch (Exception e) {
 				e.printStackTrace();
 			}
 			Boolean outputReadyValue = outputReady.equals(OutputReady.TRUE);
 			logger.info("Received new output availability report: "+ outputReadyValue + " from "+ arg1);
 
 
 			// Signal that a notification was received
 			this.outputReadyKey.get(stageKey).lock();
 			try{
 
 				// Before signaling, check the sender is registered as a stage we are subscribers of
 				if(this.outputReady.containsKey(stageKey)){
 
 					this.outputReady.put(stageKey, outputReadyValue);
 					this.outputReadyCondition.get(stageKey).signalAll();  // Tell waiting threads they can proceed
 				}
 				else {
 					logger.info("Unidentified stage has sent a notification: "+ stageKey);
 				}
 
 			} finally{
 				this.outputReadyKey.get(stageKey).unlock();
 			}
 
 		}
 		else {
 			logger.warn("Unknown notification type received");
 		}
 	}
 }
 
