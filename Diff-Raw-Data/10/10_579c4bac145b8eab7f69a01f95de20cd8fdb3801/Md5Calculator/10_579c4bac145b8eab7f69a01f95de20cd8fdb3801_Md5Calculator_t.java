 /*
  * Copyright 2010 Peter Kuterna
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
 
 package net.peterkuterna.appengine.apps.devoxxsched.util;
 
 import java.math.BigInteger;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.security.MessageDigest;
 import java.security.NoSuchAlgorithmException;
 import java.util.concurrent.ExecutionException;
 import java.util.concurrent.Future;
 
import com.google.appengine.api.urlfetch.HTTPHeader;
import com.google.appengine.api.urlfetch.HTTPRequest;
 import com.google.appengine.api.urlfetch.HTTPResponse;
 import com.google.appengine.api.urlfetch.URLFetchService;
 import com.google.appengine.api.urlfetch.URLFetchServiceFactory;
 
 public class Md5Calculator {
 
 	private String requestUri;
 
 	public Md5Calculator(final String requestUri) {
 		this.requestUri = requestUri;
 	}
 	
 	public String calculateMd5() {
 		final byte[] response = getResponse(requestUri);
 		if (response != null) {
 			try {
 				MessageDigest mdEnc = MessageDigest.getInstance("MD5");
 				mdEnc.update(response);
 				return new BigInteger(1, mdEnc.digest()).toString(16);		
 			} catch (NoSuchAlgorithmException e) {
 			}
 		}
 		return null;
 	}
 	
 	private byte[] getResponse(final String requestUri) {
 		try {
 			final URLFetchService fetcher = URLFetchServiceFactory.getURLFetchService();
 			final URL url = new URL(requestUri);
			final HTTPRequest request = new HTTPRequest(url);
			request.setHeader(new HTTPHeader("Cache-Control", "no-cache,max-age=0"));
			request.setHeader(new HTTPHeader("Pragma", "no-cache"));
			Future<HTTPResponse> future = fetcher.fetchAsync(request);
 			HTTPResponse response = future.get();
 			if (response.getResponseCode() == 200) {
 				return response.getContent();
 			}
 		} catch (ExecutionException e) {
 			e.printStackTrace();
 		} catch (InterruptedException e) {
 			e.printStackTrace();
 		} catch (MalformedURLException e) {
 			e.printStackTrace();
 		}

 		return null;
 	}
 	
 }
