 package de.wehub.lanetworking;
 
 import java.io.ByteArrayOutputStream;
 
 import org.apache.http.HttpResponse;
 import org.apache.http.auth.AuthScope;
 import org.apache.http.client.CredentialsProvider;
 import org.apache.http.impl.client.BasicCredentialsProvider;
 
 import android.os.AsyncTask;
 
 public abstract class LAAbstractOperation extends AsyncTask<Void, Void, LAAbstractOperation.LAOperationResult> {
 
 	protected LAAbstractRequest _request = null;
 	protected LAHTTPClient _client = null;
 	
 	public LAAbstractOperation(LAAbstractRequest request) {
 		_request = request;
 	}
 	
 	@Override
 	protected void onPreExecute() {
 		super.onPreExecute();
 	}
 	
 	protected abstract LAOperationResult onPostProcess(HttpResponse response, Exception ex);
 	protected abstract boolean onFinished(Object result, Exception ex);
 	
 	@Override
 	protected final void onPostExecute(LAOperationResult result) {
 		super.onPostExecute(result);
 		if(result != null) {
 			if(result.isFailed()) {
 				onFinished(null, result.getException());
 			} else {
 				onFinished(result.getResult(),null);
 			}
 		} else {
 			onFinished(null,new Exception("Please do not return null in the onPostProcess method"));
 		}
 	}
 	
 	@Override
 	protected LAOperationResult doInBackground(Void... arg0) {
 		try {
 			LAHTTPClient client = chooseClient();
 			_request.initRequest();
 			if(_request.getCredentials() != null) {
 				//	client.getParams().setAuthenticationPreemptive(true);
 				
 				CredentialsProvider credProvider = new BasicCredentialsProvider();
 			    credProvider.setCredentials(new AuthScope(AuthScope.ANY_HOST, AuthScope.ANY_PORT),
 		        _request.getCredentials());
 			    client.setCredentialsProvider(credProvider);
 			}
 			
 			return onPostProcess(client.execute(_request.getRequest()),null);
 		} catch(Exception ex) {
 			return onPostProcess(null,ex);
 		}
 	}
 	
 	public void start() {
 		execute();
 	}
 	
 	protected String getResponseString(HttpResponse response) throws Exception {
 		ByteArrayOutputStream out = new ByteArrayOutputStream();
         response.getEntity().writeTo(out);
         out.close();
         return out.toString();
 	}
 	
 	protected LAHTTPClient chooseClient() {
 		if(_client == null) {
 			return LAHTTPClient.getInstance();
 		}
 		return _client;
 	}
 	
 	public void setClient(LAHTTPClient client) {
 		_client = client;
 	}
 	
 	public LAHTTPClient getClient() {
 		return _client;
 	}
 	
 	protected class LAOperationResult {
 		Object _result = null;
 		
 		public LAOperationResult(Object result) {
 			_result = result;
 		}
 		
 		public boolean isFailed() {
 			if(_result instanceof Exception) {
 				return true;
 			}
 			return false;
 		}
 		
 		public Exception getException() {
 			if(isFailed()) {
 				return (Exception)_result;
 			}
 			return null;
 		}
 		
 		public Object getResult() {
 			return _result;
 		}
 
 	}
 }
