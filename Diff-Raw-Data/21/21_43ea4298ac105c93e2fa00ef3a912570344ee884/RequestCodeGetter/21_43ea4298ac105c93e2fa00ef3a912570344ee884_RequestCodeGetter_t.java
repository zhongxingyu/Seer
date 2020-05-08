 /**
  * 
  */
 package org.easycluster.easycluster.http;
 
 import org.easycluster.easycluster.core.Transformer;
 import org.jboss.netty.handler.codec.http.HttpRequest;
 
 public class RequestCodeGetter implements Transformer<HttpRequest, String> {
 
 	@Override
 	public String transform(HttpRequest request) {
 		String requestCode = request.getUri();
 
 		requestCode = requestCode.trim();
 		if (requestCode.startsWith("/")) {
 			requestCode = requestCode.substring(1);
 		}

		// for eg: UpdateProvision?param1=111&param2=222
		int idx = requestCode.indexOf('?');
		if (-1 != idx) {
			requestCode = requestCode.substring(0, idx); // escape '?' character
															// and mroe
 		}
 
 		// for eg: http://appid.fivesky.net:4009/UpdateProvision
		idx = requestCode.lastIndexOf('/');
 		if (-1 != idx) {
 			requestCode = requestCode.substring(idx + 1); // escape '/'
 															// character
 		}
 
		if (requestCode.endsWith("/")) {
			requestCode = requestCode.substring(0, requestCode.length() - 1);
 		}

 		return requestCode;
 	}
 
 }
