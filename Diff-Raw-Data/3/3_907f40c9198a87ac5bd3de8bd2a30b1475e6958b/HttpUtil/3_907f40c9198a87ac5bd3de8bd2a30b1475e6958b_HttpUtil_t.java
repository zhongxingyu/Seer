 /* Copyright 2010 predic8 GmbH, www.predic8.com
 
    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at
 
    http://www.apache.org/licenses/LICENSE-2.0
 
    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License. */
 
 package com.predic8.membrane.client.core.util;
 
 import java.net.MalformedURLException;
 
 import com.predic8.membrane.core.Constants;
 import com.predic8.membrane.core.http.Header;
 import com.predic8.membrane.core.http.Request;
 import com.predic8.wsdl.AbstractSOAPBinding;
 import com.predic8.wsdl.BindingOperation;
 
 public class HttpUtil {
 
 	public static Request getRequest(BindingOperation bindingOperation, String url) {
 		Request req = new Request();
 		req.setHeader(getHeader(bindingOperation, url));
 
 		req.setMethod(Request.METHOD_POST);
 		req.setVersion(Constants.HTTP_VERSION_11);
 		
 		try {
 			req.setUri(SOAModelUtil.getPathAndQueryString(url));
 		} catch (MalformedURLException e) {
 			e.printStackTrace();
 		}
 		
 		req.setBodyContent(SOAModelUtil.getSOARequestBody(bindingOperation, null).getBytes());
 		return req;
 	}
 	
 	private static Header getHeader(BindingOperation bindingOperation, String url) {
 		Header header = new Header();
 		
		header.add(Header.CONTENT_TYPE, ((AbstractSOAPBinding)bindingOperation.getBinding().getBinding()).getContentType() + "; charset=utf-8");
 		
 		String action = bindingOperation.getOperation().getSoapAction();
 		if (action == null)
 			action = "";
 		
 		header.add("SOAPAction", action);
 		
 		header.add("Host", SOAModelUtil.getHost(url));
 		return header;
 	}
 	
 	
 	
 }
