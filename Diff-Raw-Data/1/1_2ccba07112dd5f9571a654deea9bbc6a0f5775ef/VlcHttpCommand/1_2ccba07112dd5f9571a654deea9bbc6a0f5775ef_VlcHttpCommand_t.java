 package com.guillaumecharmetant.vlchttpcontroller;
 
 import java.net.URI;
 
 import org.apache.http.client.methods.HttpGet;
 
 import android.os.AsyncTask;
 
 public class VlcHttpCommand extends AsyncTask<Void, Integer, VlcHttpResponse> {
 	private VlcHttpController controller;
 	private String name;
 	private HttpGet request;
 	private VlcHttpCommandResponseHandler responseHandler = null;
 
 	public VlcHttpCommand(VlcHttpController controller, String name, URI commandURI, VlcHttpCommandResponseHandler responseHandler) {
 		this.controller = controller;
		this.name = name;
 		this.request = new HttpGet(commandURI);
 		this.setResponseHandler(responseHandler);
 	}
 	
 	public void setResponseHandler(VlcHttpCommandResponseHandler responseHandler) {
 		this.responseHandler = responseHandler;
 	}
 
 	public String getName() {
 		return this.name;
 	}
 
 	@Override
 	protected VlcHttpResponse doInBackground(Void... params) {
 		try {
 			return new VlcHttpResponse(this, this.controller.httpClient.execute(this.request));
 		} catch (Exception e) {
 			return new VlcHttpResponse(this, e);
 		}
 	}
 	
 	@Override
 	protected void onPostExecute(VlcHttpResponse response) {
 		if (this.responseHandler != null) {
 			this.responseHandler.handleResponse(this.controller, response);
 		}
 	}
 	
 	@Override
 	public String toString() {
 		URI requestUri = this.request.getURI();
 		return requestUri == null ? "(none)" : requestUri.toString();
 	}
 }
