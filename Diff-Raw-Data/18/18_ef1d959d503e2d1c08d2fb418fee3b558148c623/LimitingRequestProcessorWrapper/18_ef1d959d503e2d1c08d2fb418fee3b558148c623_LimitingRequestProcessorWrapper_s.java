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
 package net.javacrumbs.springws.test.simple;
 
 import java.io.IOException;
 import java.net.URI;
 
 import net.javacrumbs.springws.test.RequestProcessor;
 import net.javacrumbs.springws.test.WsTestException;
 
 import org.springframework.ws.WebServiceMessage;
 import org.springframework.ws.WebServiceMessageFactory;
 
 /**
  * Request processor wrapper that limits number of {@link #processRequest(URI, WebServiceMessageFactory, WebServiceMessage)} calls.
  * If number of calls is higher then {@link #maxNumberOfProcessedRequests}, wrapped processor is not called and null is returned.
  * If number of calls is lower then {@link #minNumberOfProcessedRequests} and verify is called, {@link WsTestException} is thrown.  
  * 
  * @author Lukas Krecan
  *
  */
 public class LimitingRequestProcessorWrapper implements RequestProcessor, LimitingRequestProcessor {
 
 	private final RequestProcessor wrappedRequestProcessor;
 	
 	private int numberOfProcessedRequests = 0;
 	
 	private int minNumberOfProcessedRequests = 1;
 	
 	private int maxNumberOfProcessedRequests = 1;
 	
 	private final String requestProcessorDescription;
 	
 	public LimitingRequestProcessorWrapper(RequestProcessor wrappedRequestProcessor, String requestProcessorDescription) {
 		this.wrappedRequestProcessor = wrappedRequestProcessor;
 		this.requestProcessorDescription = requestProcessorDescription;
 	}
 
 
 
 	public WebServiceMessage processRequest(URI uri, WebServiceMessageFactory messageFactory, WebServiceMessage request)
 			throws IOException {
 		numberOfProcessedRequests++;
 		if (numberOfProcessedRequests>maxNumberOfProcessedRequests)
 		{
 			return null;
 		}
 		else
 		{
 			return wrappedRequestProcessor.processRequest(uri, messageFactory, request);
 		}
 	}
 
 
 
 	public int getNumberOfProcessedRequests() {
 		return numberOfProcessedRequests;
 	}
 
 	/* (non-Javadoc)
 	 * @see net.javacrumbs.springws.test.simple.VerifiableRequestProcessor#verify()
 	 */
 	public void verify() throws WsTestException{
 		if (numberOfProcessedRequests<minNumberOfProcessedRequests)
 		{
			throw new WsTestException(generateErrorMessage());
 		}
 	}
 
 
 
	private String generateErrorMessage() {
		return requestProcessorDescription + ": Unexpected call, expected from "+minNumberOfProcessedRequests+" to "+maxNumberOfProcessedRequests+" calls, was "+numberOfProcessedRequests+".";
	}


 	public RequestProcessor getWrappedRequestProcessor() {
 		return wrappedRequestProcessor;
 	}
 
 	/* (non-Javadoc)
 	 * @see net.javacrumbs.springws.test.simple.VerifiableRequestProcessor#getMinNumberOfProcessedRequests()
 	 */
 	public int getMinNumberOfProcessedRequests() {
 		return minNumberOfProcessedRequests;
 	}
 
 
 
 	/* (non-Javadoc)
 	 * @see net.javacrumbs.springws.test.simple.VerifiableRequestProcessor#setMinNumberOfProcessedRequests(int)
 	 */
 	public void setMinNumberOfProcessedRequests(int minNumberOfProcessedRequests) {
 		this.minNumberOfProcessedRequests = minNumberOfProcessedRequests;
 	}
 
 
 
 	/* (non-Javadoc)
 	 * @see net.javacrumbs.springws.test.simple.VerifiableRequestProcessor#getMaxNumberOfProcessedRequests()
 	 */
 	public int getMaxNumberOfProcessedRequests() {
 		return maxNumberOfProcessedRequests;
 	}
 
 
 
 	/* (non-Javadoc)
 	 * @see net.javacrumbs.springws.test.simple.VerifiableRequestProcessor#setMaxNumberOfProcessedRequests(int)
 	 */
 	public void setMaxNumberOfProcessedRequests(int maxNumberOfProcessedRequests) {
 		this.maxNumberOfProcessedRequests = maxNumberOfProcessedRequests;
 	}
 
 
 
 	public void setNumberOfProcessedRequests(int numberOfProcessedRequests) {
 		this.numberOfProcessedRequests = numberOfProcessedRequests;
 	}
 
 
 
 	String getRequestProcessorDescription() {
 		return requestProcessorDescription;
 	}
 
 
 
 
 }
