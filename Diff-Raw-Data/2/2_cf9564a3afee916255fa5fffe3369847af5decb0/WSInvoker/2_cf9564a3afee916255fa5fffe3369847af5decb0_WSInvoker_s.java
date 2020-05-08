 package org.yawlfoundation.yawl.wsinvoker;
 
 import java.beans.PropertyDescriptor;
 import java.io.StringWriter;
 import java.math.BigDecimal;
 import java.math.BigInteger;
 import java.net.URL;
 import java.security.AccessController;
 import java.security.PrivilegedAction;
 import java.util.Arrays;
 import java.util.Calendar;
 import java.util.Collection;
 import java.util.Date;
 import java.util.GregorianCalendar;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.UUID;
 import java.util.concurrent.atomic.AtomicBoolean;
 import java.util.concurrent.atomic.AtomicInteger;
 import java.util.concurrent.atomic.AtomicLong;
 
 import javax.xml.bind.JAXBContext;
 import javax.xml.bind.JAXBElement;
 import javax.xml.bind.Marshaller;
 import javax.xml.bind.Unmarshaller;
 import javax.xml.namespace.QName;
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 
 import org.apache.commons.beanutils.PropertyUtils;
 import org.apache.cxf.aegis.util.stax.JDOMStreamReader;
 import org.apache.cxf.common.util.StringUtils;
 import org.apache.cxf.endpoint.Client;
 import org.apache.cxf.endpoint.Endpoint;
 import org.apache.cxf.jaxws.endpoint.dynamic.JaxWsDynamicClientFactory;
 import org.apache.cxf.service.model.BindingInfo;
 import org.apache.cxf.service.model.BindingMessageInfo;
 import org.apache.cxf.service.model.BindingOperationInfo;
 import org.apache.cxf.service.model.MessagePartInfo;
 import org.apache.cxf.service.model.ServiceInfo;
 import org.apache.cxf.transport.http.HTTPConduit;
 import org.apache.cxf.transports.http.configuration.HTTPClientPolicy;
 import org.apache.log4j.Logger;
 import org.jdom.Element;
 import org.jdom.input.DOMBuilder;
 import org.w3c.dom.Document;
 import org.xml.sax.SAXException;
 
 public class WSInvoker {
 	
 	public static final String WS_SCALARRESPONSE_PARAMNAME = "WSScalarResponse";
 	public static final String WS_COMPLEXREQUEST_PARAMNAME = "WSComplexRequest";
 	public static final String WS_COMPLEXRESPONSE_PARAMNAME = "WSComplexResponse";
 	public static final String WS_COMPLEXRESPONSE_ELEMENTNAME = "message";
 	
 	private Logger _log = Logger.getLogger(this.getClass());
 	
 	@SuppressWarnings("unchecked")
 	protected static final Set<Class<?>> SCALAR = new HashSet<Class<?>>(Arrays.asList(
 	        String.class, Boolean.class, Byte.class, Character.class, Short.class, Integer.class, Float.class, Long.class, Double.class,
 	        BigInteger.class, BigDecimal.class, AtomicBoolean.class, AtomicInteger.class, AtomicLong.class, Date.class, Calendar.class, GregorianCalendar.class,
 	        UUID.class
 	));
 
 
 	
 	/**
 	 * 
 	 * @param wsdl
 	 * @param serviceName
 	 * @param bindingName
 	 * @param operationName
 	 * @param inputData
 	 * @return result values as key-value map
 	 * @throws Exception
 	 */
 	public Object invoke(URL wsdl, String serviceName, String bindingName,
 			String operationName, Element inputData) throws Exception
 	{
 		// only enable when debugging, can cause OutOfMemoryError on big messages
 		//_log.warn("Input data: " + new XMLOutputter().outputString(inputData));
 		
 		JaxWsDynamicClientFactory factory = JaxWsDynamicClientFactory.newInstance();
 		_log.warn("Reading WSDL document from '" + wsdl + "'");
 		Client client = factory.createClient(wsdl.toExternalForm());
 		
         Endpoint endpoint = client.getEndpoint();
         
         List<ServiceInfo> serviceInfos = endpoint.getService().getServiceInfos();
         ServiceInfo serviceInfo = StringUtils.isEmpty(serviceName) 
         								? serviceInfos.get(0)
         								: findService(serviceInfos, serviceName);
         								
         _log.warn("Service: " + serviceInfo.getName());
         
         Collection<BindingInfo> bindings = serviceInfo.getBindings();
         BindingInfo binding = StringUtils.isEmpty(bindingName)
         							? bindings.iterator().next()
         							: findBinding(bindings, bindingName);
         							
         _log.warn("Binding: " + binding.getName());
         							
         BindingOperationInfo operation = findOperation(binding.getOperations(), operationName);
         
        _log.warn("Operation: " + operation);
         
         BindingMessageInfo inputMessageInfo = operation.getInput();
         List<MessagePartInfo> parts = inputMessageInfo.getMessageParts();
         MessagePartInfo partInfo = parts.get(0);
         
         Class<?> partClass = partInfo.getTypeClass();
         PropertyDescriptor[] partProperties = PropertyUtils.getPropertyDescriptors(partClass);
         
         Object inputObject = partClass.newInstance();
                 
         List<Element> children = inputData.getChildren();
         
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
         	
         	Object inputPartObject = Unmarshall(input, propertyClass);
         	
         	_log.warn("param name: " + propName + ", value: " + inputPartObject);
         	
         	property.getWriteMethod().invoke(inputObject, inputPartObject);
         }
         
         Object[] results = client.invoke(operation, inputObject);
         
         if (results.length == 0)
         {
         	return null;
         }
         Object result = results[0];
         return result;
 	}
 	
 	public static Map<String, Object> ExtractFields(Object invokeResult) throws Exception
 	{
 		Map<String, Object> returns = new HashMap<String, Object>();
 		if (invokeResult == null)
 		{
 			return returns;
 		}
               
         if (SCALAR.contains(invokeResult.getClass()))
         {
         	returns.put(WS_SCALARRESPONSE_PARAMNAME, invokeResult);
         }
         else
         {
         	PropertyDescriptor[] resultProperties = PropertyUtils.getPropertyDescriptors(invokeResult.getClass());
             for (PropertyDescriptor property : resultProperties)
             {
             	String propName = property.getName();
             	
             	// check if this is a real bean property
             	if (property.getWriteMethod() == null)
             	{
             		continue;
             	}
           	
             	Object value = property.getReadMethod().invoke(invokeResult);
             	returns.put(propName, value);
             }
         }
         return returns;
 	}
 	
 	protected static Object Unmarshall(final Element xmlElement, final Class<?> clas) throws SAXException
 	{	
 		final JDOMStreamReader reader = new JDOMStreamReader(xmlElement);
 
 		return AccessController.doPrivileged(new PrivilegedAction<Object>() {
 
 			public Object run() {
 
 				JAXBContext jc;
 				try {
 					jc = JAXBContext.newInstance(clas);
 					Unmarshaller um = jc.createUnmarshaller();
 					return um.unmarshal(reader, clas).getValue();
 				} catch (Exception e) {
 					throw new RuntimeException(e);
 				}
 			}
 		});
 	}
 	
 	public static Element Marshall(final Object input) throws Exception
 	{	
 		DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
         DocumentBuilder builder = builderFactory.newDocumentBuilder();
         final Document marshallResult = builder.newDocument();
 	
 		final StringWriter writer = new StringWriter();
 		AccessController.doPrivileged(new PrivilegedAction<Boolean>() {
 
 			public Boolean run() {
 
 				JAXBContext jc;
 				try {
 					jc = JAXBContext.newInstance(input.getClass());
 					Marshaller m = jc.createMarshaller();
 					m.setProperty(Marshaller.JAXB_FRAGMENT, true);
 					m.marshal(
 							new JAXBElement(new QName("foo"),input.getClass(), input),
 							marshallResult);
 					return true;
 				} catch (Exception e) {
 					throw new RuntimeException(e);
 				}
 			}
 		});
 		
 		// convert from org.w3c.Node to jdom
 		DOMBuilder jdomBuilder = new DOMBuilder();
 		org.jdom.Document jdomDoc = jdomBuilder.build(marshallResult);
 		Element rootElement = jdomDoc.getRootElement().setName(WS_COMPLEXRESPONSE_ELEMENTNAME);
 		rootElement.detach();
 		
 		return rootElement;
 	}
 
 	protected static BindingOperationInfo findOperation(Collection<BindingOperationInfo> ops, String opName)
 	{
 		for (BindingOperationInfo op : ops)
 		{
 			if (op.getName().getLocalPart().equalsIgnoreCase(opName))
 			{
 				return op;
 			}
 		}
 		throw new IllegalArgumentException("Operation " + opName + " not found!");
 	}
 	
 	protected static ServiceInfo findService(Collection<ServiceInfo> services, String serviceName)
 	{
 		for (ServiceInfo service : services)
 		{
 			if (service.getName().getLocalPart().equalsIgnoreCase(serviceName))
 			{
 				return service;
 			}
 		}
 		throw new IllegalArgumentException("Service " + serviceName + " not found!");
 	}
 	
 	protected static BindingInfo findBinding(Collection<BindingInfo> bindings, String bindingName)
 	{
 		for (BindingInfo binding : bindings)
 		{
 			if (binding.getName().getLocalPart().equalsIgnoreCase(bindingName))
 			{
 				return binding;
 			}
 		}
 		throw new IllegalArgumentException("Binding " + bindingName + " not found!");
 	}
 }
