 package org.yawlfoundation.yawl.wsinvoker;
 
 import org.apache.log4j.Logger;
 import org.jdom.Element;
 import org.jdom.output.XMLOutputter;
 import org.yawlfoundation.yawl.elements.data.YParameter;
 import org.yawlfoundation.yawl.engine.YSpecificationID;
 import org.yawlfoundation.yawl.engine.interfce.TaskInformation;
 import org.yawlfoundation.yawl.engine.interfce.WorkItemRecord;
 import org.yawlfoundation.yawl.engine.interfce.YParametersSchema;
 import org.yawlfoundation.yawl.engine.interfce.interfaceB.InterfaceBWebsideController;
 
 import javax.servlet.RequestDispatcher;
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import java.io.IOException;
 import java.net.URL;
 import java.util.Hashtable;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 
 public class WSInvokerController extends InterfaceBWebsideController {
 
     private String _sessionHandle = null;
     private Logger _log = Logger.getLogger(this.getClass());
 
     private static final String WSDL_LOCATION_PARAMNAME = "YawlWSInvokerWSDLLocation";
     // the following two params don't have the "Yawl" prefix because they should be optional
     // and "Yawl" params cannot be created by the user
     // this is a work-around because there is no way in defining optional parameters in task inputs
     private static final String WSDL_SERVICENAME_PARAMNAME = "WSInvokerServiceName";
     private static final String WSDL_BINDINGNAME_PARAMNAME = "WSInvokerBindingName";
     private static final String WSDL_OPERATIONNAME_PARAMNAME = "YawlWSInvokerOperationName";
 
     private static final String _engineUser = "wsInvoker2Service";
     private static final String _enginePassword = "yWSInvoker2";
 
 
     /**
      * Implements InterfaceBWebsideController.  It recieves messages from the engine
      * notifying an enabled task and acts accordingly.  In this case it takes the message,
      * tries to check out the work item, and if successful it begins to start up a web service
      * invokation.
      * @param enabledWorkItem
      */
     public void handleEnabledWorkItemEvent(WorkItemRecord enabledWorkItem) {
         try {
             if (!checkConnection(_sessionHandle)) {
                 _sessionHandle = connect(_engineUser, _enginePassword);
             }
             if (successful(_sessionHandle)) {
                 List<WorkItemRecord> executingChildren =
                         checkOutAllInstancesOfThisTask(enabledWorkItem, _sessionHandle);
                 for (WorkItemRecord itemRecord : executingChildren) {
                     Element inputData = itemRecord.getDataList();
                     String wsdlLocation = inputData.getChildText(WSDL_LOCATION_PARAMNAME);
                     String serviceName = inputData.getChildText(WSDL_SERVICENAME_PARAMNAME);
                     String bindingName = inputData.getChildText(WSDL_BINDINGNAME_PARAMNAME);
                     String operationName = inputData.getChildText(WSDL_OPERATIONNAME_PARAMNAME);
 
                     Element webServiceArgsData = (Element) inputData.clone();
                     webServiceArgsData.removeChild(WSDL_LOCATION_PARAMNAME);
                     webServiceArgsData.removeChild(WSDL_SERVICENAME_PARAMNAME);
                     webServiceArgsData.removeChild(WSDL_BINDINGNAME_PARAMNAME);
                     webServiceArgsData.removeChild(WSDL_OPERATIONNAME_PARAMNAME);
 
                     // only enable when debugging, can cause OutOfMemoryError on big messages
                     //_log.warn("Input data: " + new XMLOutputter().outputString(webServiceArgsData));
                     
                     Element complexRequest = 
                     	webServiceArgsData.getChild(WSInvoker.WS_COMPLEXREQUEST_PARAMNAME);
                     Element wsRequestData = complexRequest != null ? complexRequest : webServiceArgsData;
                     
                     //Dealing with soapfaults
                     Element caseDataBoundForEngine = prepareReplyRootElement(enabledWorkItem, _sessionHandle);
                     try {
                         Object rawReplyFromWebServiceBeingInvoked =
                             	new WSInvoker().invoke(
                                     		new URL(wsdlLocation),
                                     		serviceName,
                                     		bindingName,
                                     		operationName,
                                     		wsRequestData);
 
                         Map<String, Object> replyFromWebServiceBeingInvoked = 
                         	WSInvoker.ExtractFields(rawReplyFromWebServiceBeingInvoked); 
                         
                         _log.warn("\n\nReply from Web service being invoked is :" +
                                 replyFromWebServiceBeingInvoked);    
 
                         Map<String, String> outputDataTypes = getOutputDataTypes(enabledWorkItem);
                         if (outputDataTypes.containsKey(WSInvoker.WS_COMPLEXRESPONSE_PARAMNAME) &&
                             	rawReplyFromWebServiceBeingInvoked != null)
                             {               
                             	if (replyFromWebServiceBeingInvoked.containsKey(WSInvoker.WS_SCALARRESPONSE_PARAMNAME)) {
                             		throw new IllegalArgumentException(
                             				"Web service returned a scalar result," +
                             				" no complex response param possible!");
                             	}
                             	System.out.println("replyMsg class = " + rawReplyFromWebServiceBeingInvoked.getClass().getName());
                         		Element content = new Element(WSInvoker.WS_COMPLEXRESPONSE_PARAMNAME);
                                 Element response = WSInvoker.Marshall(rawReplyFromWebServiceBeingInvoked);
                                 content.addContent(response);
                                 caseDataBoundForEngine.addContent(content);
                             }
                         
                         for (String varName : replyFromWebServiceBeingInvoked.keySet()) {
                             Object replyMsg = replyFromWebServiceBeingInvoked.get(varName);
                             System.out.println("replyMsgProperty class = " + replyMsg.getClass().getName());
                             String varVal = replyMsg.toString();
                             String outputVarName = findOutputVarName(outputDataTypes.keySet(), varName);
                             if (outputVarName == null)
                             {
                             	_log.warn(varName + " not found in output params");
                             	continue;
                             }
                             String varType = outputDataTypes.get(outputVarName);
                             if ((varType != null) && (! varType.endsWith("string"))) {
                                 varVal = validateValue(varType, varVal);
                             }
                             Element content = new Element(outputVarName);
                             content.setText(varVal);
                             caseDataBoundForEngine.addContent(content);
                         }
                         
                     } catch (Exception e) {
                     	Element errorContent = new Element("errormessage");
                     	errorContent.setText(e.getMessage());
                     	caseDataBoundForEngine.addContent(errorContent);
                     }
 
                     _logger.warn("\nResult of item [" +
                             itemRecord.getID() + "] checkin is : " +
                             checkInWorkItem(
                                     itemRecord.getID(),
                                     inputData,
                                     caseDataBoundForEngine, null,
                                     _sessionHandle));
                 }
             }
 
         } catch (Exception e) {
             _logger.error(e.getMessage(), e);
         }
     }
 
     private static String findOutputVarName(Set<String> outputVarNames, String wsResponseVarName) {
     	
     	// if web service result is just single value without a name and there is
     	// only one output task param then choose it no matter which name it has -> convenience
    	/*if (outputVarNames.size() == 1 && wsResponseVarName.equals(WSInvoker.WS_SCALARRESPONSE_PARAMNAME))
     	{
     		String varName = outputVarNames.iterator().next();
     		if (!varName.equals(WSInvoker.WS_COMPLEXRESPONSE_PARAMNAME))
     		{
     			return varName;
     		}
    	}*/
    	// Hack to allow the errorMessage parameter.
    	if (outputVarNames.size() == 2 && wsResponseVarName.equals(WSInvoker.WS_SCALARRESPONSE_PARAMNAME))
    	{
    		for (String varName : outputVarNames) 
    		{
    			if (!varName.equals(WSInvoker.WS_COMPLEXRESPONSE_PARAMNAME) && 
    					!varName.equals("errorMessage")) 
    			{
    				return varName;
    			}
    		}
     	}
     	
 		for (String var : outputVarNames)
 		{
 			if (var.equalsIgnoreCase(wsResponseVarName))
 			{
 				return var;
 			}
 		}
 		return null;
 	}
 
 	private Map<String, String> getOutputDataTypes(WorkItemRecord wir) throws IOException {
         Map<String, String> dataTypes = new Hashtable<String, String>();
         TaskInformation taskInfo = this.getTaskInformation(
                 new YSpecificationID(wir), wir.getTaskID(), _sessionHandle);
         if (taskInfo != null) {
             YParametersSchema schema = taskInfo.getParamSchema();
             if (schema != null) {
                 for (YParameter param : schema.getOutputParams()) {
                     dataTypes.put(param.getPreferredName(), param.getDataTypeName());
                 }
             }
         }
         return dataTypes;
     }
 
     private String validateValue(String type, String value) {
         if (type.endsWith("boolean")) {
             return String.valueOf(value.equalsIgnoreCase("true"));
         }
         try {
             if (type.endsWith("integer")) {
                return String.valueOf(new Integer(value));
             }
             else if (type.endsWith("double")) {
                return String.valueOf(new Double(value));
             }
             else if (type.endsWith("float")) {
                return String.valueOf(new Float(value));
             }
             else return value;    // we tried!
         }
         catch (NumberFormatException nfe) {
             if (type.endsWith("integer")) {
                 return "0";
             }
             else {
                 return "0.0";
             }
         }
     }
 
     public void handleCancelledWorkItemEvent(WorkItemRecord workItemRecord) {
 
     }
 
     public YParameter[] describeRequiredParams() {
         YParameter[] params = new YParameter[3];
         YParameter param;
 
         param = new YParameter(null, YParameter._INPUT_PARAM_TYPE);
         param.setDataTypeAndName(XSD_ANYURI_TYPE, WSDL_LOCATION_PARAMNAME, XSD_NAMESPACE);
         param.setDocumentation("This is the location of the WSDL for the Web Service");
         params[0] = param;
         
         /* see notes on string constants in class header
         param = new YParameter(null, YParameter._INPUT_PARAM_TYPE);
         param.setDataTypeAndName(XSD_NCNAME_TYPE, WSDL_SERVICENAME_PARAMNAME, XSD_NAMESPACE);
         param.setDocumentation("This is the service name as defined in the WSDL. (optional)");
         params[1] = param;
         
         param = new YParameter(null, YParameter._INPUT_PARAM_TYPE);
         param.setDataTypeAndName(XSD_NCNAME_TYPE, WSDL_BINDINGNAME_PARAMNAME, XSD_NAMESPACE);
         param.setDocumentation("This is the binding name as defined in the WSDL. (optional)");
         params[2] = param;*/
 
         param = new YParameter(null, YParameter._INPUT_PARAM_TYPE);
         param.setDataTypeAndName(XSD_NCNAME_TYPE, WSDL_OPERATIONNAME_PARAMNAME, XSD_NAMESPACE);
         param.setDocumentation("This is the operation name of the Web service - inside the WSDL.");
         params[1] = param;
         
         param = new YParameter(null, YParameter._OUTPUT_PARAM_TYPE);
         param.setDataTypeAndName("string", "errormessage", XSD_NAMESPACE);
         param.setDocumentation("The error message in case of a Soap fault");
         params[2] = param;
         
         return params;
     }
 
 
     public void doGet(HttpServletRequest request, HttpServletResponse response)
             throws ServletException, IOException {
 
         RequestDispatcher dispatcher =
                 request.getRequestDispatcher("/authServlet");
 
         dispatcher.forward(request, response);
     }
 
 
 }
 
 
