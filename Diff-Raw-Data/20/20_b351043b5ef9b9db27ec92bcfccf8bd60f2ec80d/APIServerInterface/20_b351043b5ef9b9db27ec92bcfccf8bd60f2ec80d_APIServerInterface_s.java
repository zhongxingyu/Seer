 package com.xenon.greenup.api;
 
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.OutputStream;
 import java.net.HttpURLConnection;
 import java.net.URL;
 import java.util.concurrent.ExecutionException;
 
 import android.os.AsyncTask;
 import android.util.Log;
 
 public class APIServerInterface {
 	
 	
 	private final String BASE_URL = "https://greenupapp.appspot.com/api";
 	
 	//Class to serve as the main interface to the API server
 	//The application will invoke methods defined here to perform 
 	// the various requests
 	
 	//TODO: Make it display a toast notification when an exception is thrown
 	//TODO: Clean up logging output
 	
 	
 	//Retrieves a page of comments from the server, the type and page number are optional
 	public CommentPage getComments(String type, int page) {
 		if(type == null || type.equals(""))
 			return this.getAllComments(page);
 		StringBuilder sb = new StringBuilder(BASE_URL + "/comments?");
 		sb.append("type=" + type + "&" + "page=" + page);
 		String response = sendRequest(sb.toString());
 		return new CommentPage(response);
 	}
 	
 	//Retrieves a page of comments with no regard for the type
 	public CommentPage getAllComments(int page){
 		StringBuilder sb = new StringBuilder(); sb.append(BASE_URL).append("/comments?").append("page=").append(page);
 		String response = sendRequest(sb.toString());
 		return new CommentPage(response);
 	}
 	
 	//Submits a comment (POST), the pin is optional.  Returns an integer status code (codes TBD)
 	public void submitComments(String type, String message, int pin) {
 		Comment newComment = new Comment(type, message, pin);
 		String data = newComment.toJSON().toString();
 		String url = new StringBuilder(BASE_URL + "/comments").toString();
 		String response = sendRequestWithData(url,"POST",data);
 		Log.i("response",response);
 	}
 	
 	//Get a list of heatmap points for the specified coordinates, all parameters are optional (??)
 	public Heatmap getHeatmap(float latDegrees, float latOffset, float lonDegrees, float lonOffset, int precision){
 		StringBuilder sb = new StringBuilder(BASE_URL + "/heatmap?");
		//sb.append("latDegrees=" + latDegrees + "&latOffset=" + latOffset + "&");
		//sb.append("lonDegrees=" + lonDegrees + "&lonOffset=" + lonOffset + "&");
		//sb.append("precision=" + precision);
 		String response = sendRequest(sb.toString());
 		return new Heatmap(response);
 	}
 	
 	//Submit a heatmap point (PUT)
 	public void updateHeatmap(Heatmap h){
 		String data = h.toJSON().toString();
 		String url = new StringBuilder(BASE_URL + "/heatmap").toString();
 		String response = sendRequestWithData(url,"PUT",data);
 		Log.i("response",response);
 	}
 	
 	//Get a list of pins, all parameters are optional
 	public PinList getPins(float latDegrees, float latOffset, float lonDegrees, float lonOffset){
 		StringBuilder sb = new StringBuilder(BASE_URL + "/pins?");
 		String response = sendRequest(sb.toString());
 		return new PinList(response);
 	}
 	
 	//Submit a pin (POST)
 	public int submitPin(float latDegrees, float lonDegrees, String type, String message){
 		return 0;
 	}
 	
 	public int testConnection(){
 		sendRequest(BASE_URL);
 		return 0;
 	}
 	
 	private String sendRequestWithData(String url, String method, String data) {
 		APIRequestTask request = new APIRequestTask(url,method,data);
 		request.execute();
 		String response;
 		try {
 			response = request.get();
 		}
 		catch (Exception e) {
 			e.printStackTrace();
 			response = "Error, see stack trace";
 		}
 		Log.i("response",response);
 		return response;
 	}
 	
 	private String sendRequest(String url) {
 		APIRequestTask request = new APIRequestTask(url);
 		request.execute();
 		String response;
 		try {
 			response = request.get();
 		}
 		catch (Exception e) {
 			e.printStackTrace();
 			response = "Error, see stack trace";
 		}
 		Log.i("response",response);
 		return response;
 	}
 	
 	private class APIRequestTask extends AsyncTask<Void,Void,String>{
 		
 		private String url;
 		private String data;
 		private String method;
 		
 		//Constructor for GET requests
 		public APIRequestTask(String url) {
 			this.url = url;
 			this.method = "GET";
 		}
 		
 		//Constructor for POST and PUT requests
 		public APIRequestTask(String url,String method,String data) {
 			this.url = url;
 			this.data = data;
 			this.method = method;
 		}
 		
 		//Where the magic happens...
 		@Override
 		protected String doInBackground(Void...voids) {
 			String response;
 			URL urlObject;
 			try {
 				urlObject = new URL(url);
 				HttpURLConnection connection = (HttpURLConnection)urlObject.openConnection();
 				connection.setRequestMethod(method);
 				if (data != null) {
 					connection.setDoOutput(true);
 					int length = data.length();
 					connection.setFixedLengthStreamingMode(length);
 					OutputStream out = connection.getOutputStream();
 					out.write(data.toString().getBytes());
 				}
 				InputStreamReader reader = new InputStreamReader(connection.getInputStream());
 				StringBuilder sb = new StringBuilder();
 				
 				int readByte;
 				while ((readByte = reader.read()) != -1) {
 					sb.append((char)readByte);
 				}
 				response = sb.toString();
 			}
 			catch(IOException e) {
 				e.printStackTrace();
 				response = "Error occurred, see stack trace";
 			}
 			return response;
 		}
 	}
 	
 	
 	
 	
 	
 	
 	
 	
 }
