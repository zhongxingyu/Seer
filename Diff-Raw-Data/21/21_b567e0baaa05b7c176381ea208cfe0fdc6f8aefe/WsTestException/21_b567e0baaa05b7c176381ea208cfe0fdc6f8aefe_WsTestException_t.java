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
 
 import javax.xml.transform.Source;
 
 import net.javacrumbs.springws.test.util.DefaultXmlUtil;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.springframework.ws.WebServiceMessage;
 
 /**
  * Exception thrown when test validation fails.
  * @author Lukas Krecan
  *
  */
 public class WsTestException extends RuntimeException {
 	private static final long serialVersionUID = 8882366230559729512L;
 
 	private static final Log logger = LogFactory.getLog(WsTestException.class);
 	
 	private static final String SEPARATOR = "----------------------\n";
 
 	
 	private Source sourceMessage;
 	
 	private Source controlMessage;
 
 	public WsTestException() {
 		super();
 	}
 
 	public WsTestException(String errorMessage, Throwable cause) {
 		super(errorMessage, cause);
 	}
 	
 	public WsTestException(String errorMessage) {
 		super(errorMessage);
 	}
 
 	public WsTestException(String errorMessage, Source sourceMessage, Source controlMessage) {
 		super(errorMessage);
 		this.sourceMessage = sourceMessage;
 		this.controlMessage = controlMessage;
 	}
 	public WsTestException(String errorMessage, WebServiceMessage sourceMessage) {
 		super(errorMessage);
 		this.sourceMessage = DefaultXmlUtil.getInstance().getEnvelopeSource(sourceMessage);
 	}
 
 	public WsTestException(Throwable cause) {
 		super(cause);
 	}
 
 	public Source getSourceMessage() {
 		return sourceMessage;
 	}
 	
 	@Override
 	public String getMessage() {
 		String serializedSourceMessage = serializeSource("Source message:",sourceMessage);
 		String serializedControlMessage = serializeSource("Control message:",controlMessage);
 		return super.getMessage()+serializedSourceMessage+serializedControlMessage;
 	}
 
 	private String serializeSource(String label, Source source) {
 		try
 		{
			return source!=null?"\n"+SEPARATOR+label+"\n"+DefaultXmlUtil.getInstance().serializeDocument(source):"";
 		}
 		catch(Exception e)
 		{
 			logger.warn("Can not serialize "+label,e);
 			return "";
 		}
 	}
 
 }
