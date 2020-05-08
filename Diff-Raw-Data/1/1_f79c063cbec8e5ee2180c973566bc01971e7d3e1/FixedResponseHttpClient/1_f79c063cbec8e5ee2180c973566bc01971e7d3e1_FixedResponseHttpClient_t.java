 /* Copyright 2010 Meta Broadcast Ltd
 
 Licensed under the Apache License, Version 2.0 (the "License"); you
 may not use this file except in compliance with the License. You may
 obtain a copy of the License at
 
 http://www.apache.org/licenses/LICENSE-2.0
 
 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 implied. See the License for the specific language governing
 permissions and limitations under the License. */
 
 package org.atlasapi.remotesite;
 
 import java.io.ByteArrayInputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.URL;
 
 import org.apache.commons.io.IOUtils;
 
 import com.google.common.base.Charsets;
 import com.google.common.io.Resources;
 import com.metabroadcast.common.http.HttpException;
 import com.metabroadcast.common.http.HttpResponse;
 import com.metabroadcast.common.http.HttpResponsePrologue;
 import com.metabroadcast.common.http.HttpStatusCodeException;
 import com.metabroadcast.common.http.Payload;
 import com.metabroadcast.common.http.SimpleHttpClient;
 import com.metabroadcast.common.http.SimpleHttpRequest;
 
 public class FixedResponseHttpClient implements SimpleHttpClient {
 
 	private final String respondsTo;
 	private final String data;
 
 	public FixedResponseHttpClient(String respondsTo, String data) {
 		this.respondsTo = respondsTo;
 		this.data = data;
 	}
 	
 	@Override
 	public HttpResponse get(String url) throws HttpException {
 		if (respondsTo.equals(url)) {
 			return HttpResponse.sucessfulResponse(data);
 		}
 		throw new HttpStatusCodeException(404, "Not found");
 	}
 	
     @Override
 	public <T> T get(SimpleHttpRequest<T> request) throws HttpException, Exception {
 	    if (respondsTo.equals(request.getUrl())) {
 	        ByteArrayInputStream in = new ByteArrayInputStream(data.getBytes(Charsets.UTF_8));
             return request.getTransformer().transform(new HttpResponsePrologue(200), in);
 	    }
 	    throw new HttpStatusCodeException(404, "Not found");
     }
     
 	@Override
 	public String getContentsOf(String url) throws HttpException {
 		if (respondsTo.equals(url)) {
 			return data;
 		}
 		throw new HttpStatusCodeException(404, "Not found");
 	}
 
 	@Override
 	public HttpResponse head(String string) throws HttpException {
 		throw new UnsupportedOperationException();
 	}
 
 	@Override
 	public HttpResponse post(String url, Payload data) throws HttpException {
 		throw new UnsupportedOperationException();
 	}
 	
 	public static FixedResponseHttpClient respondTo(String url, String response) {
 		return new FixedResponseHttpClient(url, response);
 	}
 	
 	public static FixedResponseHttpClient respondTo(String url, URL response) {
 		try {
 			return respondTo(url, Resources.newInputStreamSupplier(response).getInput());
 		} catch (IOException e) {
 			throw new RuntimeException(e);
 		}
 	}
 	
 	public static FixedResponseHttpClient respondTo(String url, InputStream response) {
 		try {
 			return new FixedResponseHttpClient(url, IOUtils.toString(response));
 		} catch (IOException e) {
 			throw new RuntimeException(e);
 		}
 	}
 
     @Override
     public HttpResponse put(String arg0, Payload arg1) throws HttpException {
         throw new UnsupportedOperationException();
     }
 
     public HttpResponse delete(String string) throws HttpException {
         throw new UnsupportedOperationException();
     }
 }
