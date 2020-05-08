 /**
  * Copyright 2009-2010 the original author or authors.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package net.javacrumbs.springws.test;
 
 import java.io.IOException;
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.List;
 
 import net.javacrumbs.springws.test.util.DefaultXmlUtil;
 import net.javacrumbs.springws.test.util.XmlUtil;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.springframework.ws.FaultAwareWebServiceMessage;
 import org.springframework.ws.WebServiceMessage;
 import org.springframework.ws.WebServiceMessageFactory;
 import org.springframework.ws.context.DefaultMessageContext;
 import org.springframework.ws.context.MessageContext;
 import org.springframework.ws.server.EndpointInterceptor;
 import org.springframework.ws.transport.WebServiceConnection;
 
 
 /**
  * Mock WS connection that instead of actually calling the WS uses {@link RequestProcessor}s
  * to validate all the requests and generate responses.
  * @author Lukas Krecan
  *
  */
 public class MockWebServiceConnection implements WebServiceConnection {
 
 	private final URI uri;
 
 	private WebServiceMessage request;
 	
 	private List<RequestProcessor> requestProcessors;
 	
 	private List<EndpointInterceptor> interceptors = Collections.emptyList();
 	
 	private XmlUtil xmlUtil = DefaultXmlUtil.getInstance();
 	
 	protected final Log logger = LogFactory.getLog(getClass());
 	
 	public MockWebServiceConnection(URI uri) {
 		this.uri = uri;
 	}
 
 	/**
 	 * Stores the message.
 	 */
 	public void send(WebServiceMessage message) throws IOException {
 		request = message;
 	}
 
 	/**
 	 * Generates mock response
 	 */
 	public WebServiceMessage receive(WebServiceMessageFactory messageFactory) throws IOException {
 		DefaultMessageContext messageContext = new DefaultMessageContext(request, messageFactory);
		boolean callRequestProcessors = handleRequest(messageContext);
 		if (callRequestProcessors)
 		{
 			WebServiceMessage response = generateResponse(messageFactory);
 			messageContext.setResponse(response);
 		}
 		handleResponse(messageContext);
 		return messageContext.getResponse();
 	}
 
	/**
	 * Iterates over all interceptors. If one of them returns false, false is returned. True is returned otherwise.
	 * @param messageContext
	 * @return
	 * @throws IOException
	 */
	protected boolean handleRequest(MessageContext messageContext) throws IOException {
 		for (EndpointInterceptor interceptor:interceptors)
 		{
 			try {
 				if (!interceptor.handleRequest(messageContext, null))
 				{
 					return false;
 				}
 			} catch (Exception e) {
 				throw new IOException("Unexpected exception",e);
 			}
 		}
 		return true;
 	}
 
 	/**
 	 * Processes response using interceptors.
 	 * @param messageContext
 	 * @throws IOException
 	 */
 	protected void handleResponse(MessageContext messageContext) throws IOException {
 		if (!interceptors.isEmpty())
 		{
 			boolean hasFault = hasFault(messageContext);
 			for (EndpointInterceptor interceptor:interceptors)
 			{
 				try {
 					if (!hasFault)
 					{
 						if (!interceptor.handleResponse(messageContext, null)) return;
 					}
 					else
 					{
 						if (!interceptor.handleFault(messageContext, null)) return;
 					}
 				} catch (Exception e) {
 					throw new IOException("Unexpected exception",e);
 				}
 			}
 		}
 	}
 	/**
 	 * Returns true if the message has fault.
 	 * @param messageContext
 	 * @return
 	 */
 	protected boolean hasFault(MessageContext messageContext) {
 		boolean hasFault = false;
         WebServiceMessage response = messageContext.getResponse();
         if (response instanceof FaultAwareWebServiceMessage) {
             hasFault = ((FaultAwareWebServiceMessage) response).hasFault();
         }
 		return hasFault;
 	}
 
 	/**
 	 * Calls all request processors. If a processor returns <code>null</code>, the next processor is called. If all processor return  <code>null</code>
 	 * {@link MockWebServiceConnection#handleResponseNotFound} method is called. In default implementation it throws {@link NoResponseGeneratorSpecifiedException}.
 	 * @param messageFactory
 	 * @return
 	 * @throws IOException
 	 */
 	protected WebServiceMessage generateResponse(WebServiceMessageFactory messageFactory) throws IOException {
 		WebServiceMessage response = null;
 		if (requestProcessors!=null)
 		{
 			for (RequestProcessor responseGenerator: requestProcessors)
 			{
 				response = responseGenerator.processRequest(uri, messageFactory, request);
 				if (response!=null)
 				{
 					return response;
 				}
 			}
 		}
 		return handleResponseNotFound(messageFactory);
 	}
 
 	/**
 	 * Throws {@link NoResponseGeneratorSpecifiedException}. Can be overrriden.
 	 * @param messageFactory
 	 * @return
 	 */
 	protected WebServiceMessage handleResponseNotFound(WebServiceMessageFactory messageFactory) {
 		throw new NoResponseGeneratorSpecifiedException("No response found for request. Please, log category \"net.javacrumbs.springws.test.lookup\" on DEBUG level to find out the reasons. ");
 	}
 	
 
 	public void close() throws IOException {
 		
 	}
 
 	public String getErrorMessage() throws IOException {
 		return null;
 	}
 
 	public URI getUri() throws URISyntaxException {
 		return uri;
 	}
 
 	public boolean hasError() throws IOException {
 		return false;
 	}
 
 
 	
 	public WebServiceMessage getRequest() {
 		return request;
 	}
 
 
 	public XmlUtil getXmlUtil() {
 		return xmlUtil;
 	}
 
 	public void setXmlUtil(XmlUtil xmlUtil) {
 		this.xmlUtil = xmlUtil;
 	}
 
 	public Collection<RequestProcessor> getRequestProcessors() {
 		return requestProcessors;
 	}
 
 	public void setRequestProcessors(List<RequestProcessor> responseGenerators) {
 		this.requestProcessors = responseGenerators;
 	}
 
 	public List<EndpointInterceptor> getInterceptors() {
 		return interceptors;
 	}
 
 	public void setInterceptors(List<EndpointInterceptor> interceptors) {
 		this.interceptors = interceptors;
 	}
 }
