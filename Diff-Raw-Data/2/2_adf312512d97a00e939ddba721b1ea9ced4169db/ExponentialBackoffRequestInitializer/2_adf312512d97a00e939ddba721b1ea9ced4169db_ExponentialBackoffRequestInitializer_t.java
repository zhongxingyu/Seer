 package com.hackaton.social.google.client.http;
 
 import java.io.IOException;
 
 import com.google.api.client.http.HttpBackOffUnsuccessfulResponseHandler;
 import com.google.api.client.http.HttpRequest;
 import com.google.api.client.http.HttpRequestInitializer;
 import com.google.api.client.util.ExponentialBackOff;
 
 /**
  * @author mdaleki
  */
public class ExponentialBackoffRequestInitializer implements HttpRequestInitializer {
 	@Override
 	public void initialize(final HttpRequest request) throws IOException {
 		request.setUnsuccessfulResponseHandler(new HttpBackOffUnsuccessfulResponseHandler(new ExponentialBackOff()));
 	}
 }
