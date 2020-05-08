 package org.yawlfoundation.yawl.wsinvoker;
 
 import java.beans.PropertyDescriptor;
 import java.net.URL;
 import java.util.Collection;
 import java.util.List;
 import java.util.Map;
 
 import org.apache.cxf.endpoint.Client;
 import org.apache.cxf.endpoint.ClientImpl;
 import org.apache.cxf.endpoint.Endpoint;
 import org.apache.cxf.jaxws.endpoint.dynamic.JaxWsDynamicClientFactory;
 import org.apache.cxf.service.model.BindingInfo;
 import org.apache.cxf.service.model.BindingMessageInfo;
 import org.apache.cxf.service.model.BindingOperationInfo;
 import org.apache.cxf.service.model.MessagePartInfo;
 import org.apache.cxf.service.model.ServiceInfo;
 import org.apache.cxf.common.util.StringUtils;
 import org.jdom.Element;
 
 import org.apache.commons.beanutils.PropertyUtils;
 
 public class Testing {
 
 	public static void main(String[] args) {
 
 		
 		try {
 			String wsdlLocation = "http://localhost:8082/Architecture.xadl.proxy.service/ReconfigurationProxyService?wsdl";
			String serviceName = "{http://service.proxy.xadl.architecture/}ReconfigurationProxyService";
			String bindingName = "[BindingInfo http://schemas.xmlsoap.org/wsdl/soap/]";
 			String operationName = "getMyArchInstance";
 
 			Element wsRequestData = new Element("elemento");
 			URL urlWsdl = new URL(wsdlLocation);
 			
 			JaxWsDynamicClientFactory factory = JaxWsDynamicClientFactory.newInstance();
 			Client client = factory.createClient(urlWsdl.toExternalForm());
 			ClientImpl clientImpl = (ClientImpl) client;
 			Endpoint endpoint = client.getEndpoint();
 			
 		    Collection<ServiceInfo> serviceInfos = endpoint.getService().getServiceInfos();
 	        ServiceInfo serviceInfo = StringUtils.isEmpty(serviceName) 
 	        								? serviceInfos.iterator().next()
 	        								: WSInvoker.findService(serviceInfos, serviceName);
 	        System.out.println("Service: " + serviceInfo.getName());
 	        								
 	        Collection<BindingInfo> bindings = serviceInfo.getBindings();
 	        BindingInfo binding = StringUtils.isEmpty(bindingName)
 	               							? bindings.iterator().next()
 	               							: WSInvoker.findBinding(bindings, bindingName);
	        System.out.println("Binding: " + binding);
 	        
 	        BindingOperationInfo operation = WSInvoker.findOperation(binding.getOperations(), operationName);
 	        System.out.println("Operation: " + operation);
 	        
 	        BindingMessageInfo inputMessageInfo = operation.getInput();
 	        List<MessagePartInfo> parts = inputMessageInfo.getMessageParts();
 	        MessagePartInfo partInfo = parts.get(0);
 	        
 	        Class<?> partClass = partInfo.getTypeClass();
 	        PropertyDescriptor[] partProperties = PropertyUtils.getPropertyDescriptors(partClass);
 	        
 	        Object inputObject = partClass.newInstance();
 	        //Preparing the input parameters
 	        
  
 	        
 	        List<Element> children = wsRequestData.getChildren();
 	        
 	        for (PropertyDescriptor property : partProperties)
 	        {
 	        	String propName = property.getName();
 	        	
 	        	Element input = null;
 	        	for (Element child : children)
 	        	{
 	        		if (child.getName().equalsIgnoreCase(propName))
 	        		{
 	        			input = child;
 	        		}
 	        	}
 
 	        	if (property.getWriteMethod() == null || input == null)
 	        	{
 	        		continue;
 	        	}
 	        
 	        	Class<?> propertyClass = property.getPropertyType();
 	        	
 	        	Object inputPartObject = WSInvoker.Unmarshall(input, propertyClass);
 	        	
 	        	System.out.println("param name: " + propName + ", value: " + inputPartObject);
 	        	
 	        	property.getWriteMethod().invoke(inputObject, inputPartObject);
 	        }
 	        
 	        Object[] results = client.invoke(operation, inputObject);
 	        
 	        Object result = null;
 	        if (results.length > 0) {
 	        	result = results[0];
 	        }
 			
 	        
 			//Before here
 			Object rawReplyFromWebServiceBeingInvoked = result;
 			
 					//new WSInvoker().invoke(
 					//new URL(wsdlLocation), serviceName, bindingName,
 					//operationName, wsRequestData);
 
 			Map<String, Object> replyFromWebServiceBeingInvoked = WSInvoker
 					.ExtractFields(rawReplyFromWebServiceBeingInvoked);
 
 			for (String varName : replyFromWebServiceBeingInvoked.keySet()) {
 				System.out.println("----------");
 				Object replyMsg = replyFromWebServiceBeingInvoked.get(varName);
 				System.out.println("replyMsgProperty class = "
 						+ replyMsg.getClass().getName());
 				String varVal = replyMsg.toString();
 				System.out.println("Valor: " + varVal);
 			}
 	       
 	        
 	     
 		} catch (Exception e) {
 			System.out.println(e.getMessage());
 			e.printStackTrace();
 		}
 	}
 }
