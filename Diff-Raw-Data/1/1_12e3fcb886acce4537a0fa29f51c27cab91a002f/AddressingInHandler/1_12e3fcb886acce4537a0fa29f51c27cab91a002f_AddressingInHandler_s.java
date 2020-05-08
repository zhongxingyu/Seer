 /*
  * Copyright 2005-2006 The Apache Software Foundation.
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
 package org.apache.servicemix.soap.handlers;
 
 import java.util.Iterator;
 import java.util.Map;
 
 import javax.xml.namespace.QName;
 
 import org.apache.servicemix.jbi.util.DOMUtil;
 import org.apache.servicemix.soap.Context;
 import org.apache.servicemix.soap.SoapFault;
 import org.apache.servicemix.soap.marshalers.SoapMessage;
 import org.w3c.dom.DocumentFragment;
 import org.w3c.dom.Element;
 
 /**
  * 
  * @author Guillaume Nodet
  * @version $Revision: 1.5 $
  * @since 3.0
  */
 public class AddressingInHandler extends AbstractHandler {
 
     public static final String WSA_NAMESPACE_200303 = "http://schemas.xmlsoap.org/ws/2003/03/addressing";
     public static final String WSA_NAMESPACE_200408 = "http://schemas.xmlsoap.org/ws/2004/08/addressing";
     public static final String WSA_NAMESPACE_200508 = "http://www.w3.org/2005/08/addressing";
     
     public static final String EL_ACTION = "Action";
     public static final String EL_ADDRESS = "Address";
     public static final String EL_FAULT_TO = "FaultTo";
     public static final String EL_FROM = "From";
     public static final String EL_MESSAGE_ID = "MessageID";
     public static final String EL_METADATA = "Metadata";
     public static final String EL_REFERENCE_PARAMETERS = "ReferenceParameters";
     public static final String EL_RELATES_TO = "RelatesTo";
     public static final String EL_REPLY_TO = "ReplyTo";
     public static final String EL_TO = "To";
     
 	public void process(Context context) throws Exception {
 		SoapMessage message = (SoapMessage) context.getProperty(Context.SOAP_MESSAGE);
     	String action = null;
     	String to = null;
     	String nsUri = null;
     	Map headers = message.getHeaders();
     	if (headers != null) {
 	    	for (Iterator it = headers.keySet().iterator(); it.hasNext();) {
 	    		QName qname = (QName) it.next();
 	    		Object value = headers.get(qname);
 	    		if (WSA_NAMESPACE_200303.equals(qname.getNamespaceURI()) ||
 	    			WSA_NAMESPACE_200408.equals(qname.getNamespaceURI()) ||
 	    			WSA_NAMESPACE_200508.equals(qname.getNamespaceURI())) {
 	    			if (nsUri == null) {
 	    				nsUri = qname.getNamespaceURI();
 	    			} else if (!nsUri.equals(qname.getNamespaceURI())) {
 	    				throw new SoapFault(SoapFault.SENDER, "Inconsistent use of wsa namespaces");
 	    			}
 		    		if (qname.getLocalPart().equals(AddressingInHandler.EL_ACTION)) {
 		    			Element el = (Element) ((DocumentFragment) value).getFirstChild();
 		    			action = DOMUtil.getElementText(el);
 		        		String[] parts = split(action);
 		        		context.setProperty(Context.INTERFACE, new QName(parts[0], parts[1]));
 		        		context.setProperty(Context.OPERATION, new QName(parts[0], parts[2]));
 		    		} else if (qname.getLocalPart().equals(AddressingInHandler.EL_TO)) {
 		    			Element el = (Element) ((DocumentFragment) value).getFirstChild();
 		    			to = DOMUtil.getElementText(el);
 		        		String[] parts = split(to);
 		        		context.setProperty(Context.SERVICE, new QName(parts[0], parts[1]));
 		        		context.setProperty(Context.ENDPOINT, parts[2]);
 		    		} else {
 		    			// TODO: what ?
 		    		}
 	    		}
 	    	}
     	}
 	}
     
     protected String[] split(String uri) {
 		char sep;
 		if (uri.indexOf('/') > 0) {
 			sep = '/';
 		} else {
 			sep = ':';
 		}
 		int idx1 = uri.lastIndexOf(sep);
 		int idx2 = uri.lastIndexOf(sep, idx1 - 1);
 		String epName = uri.substring(idx1 + 1);
 		String svcName = uri.substring(idx2 + 1, idx1);
 		String nsUri   = uri.substring(0, idx2);
     	return new String[] { nsUri, svcName, epName };
     }
     
 }
