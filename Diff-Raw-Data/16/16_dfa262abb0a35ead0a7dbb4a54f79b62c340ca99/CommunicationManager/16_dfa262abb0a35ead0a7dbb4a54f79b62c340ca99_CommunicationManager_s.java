 /**
  * Manages every call to the ViSH API and its result.
  */
 package dit.upm.es.ging.vishmobile.core;
 
 import java.io.BufferedInputStream;
 import java.io.DataOutputStream;
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.HttpURLConnection;
 import java.net.MalformedURLException;
 import java.net.URL;
 
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.util.Log;
 
 
 /**
  * @author Daniel Gallego Vico
  * @author Aldo Gordillo Méndez
  *
  */
 public class CommunicationManager {
 	
 	
 	// Classic Singleton implementation.
 	private static CommunicationManager instance = null;
 	
 	/**
 	 * Protected constructor avoids common instantiation from other packages
 	 */
 	private CommunicationManager() {
 			
 	}
 	
 	/**
 	 * Singleton management
 	 * @return instance of CommunicationManager
 	 */
 	public static CommunicationManager getInstance() {
       if(instance == null) {
          instance = new CommunicationManager();
       }
       return instance;
 	}
 	
 	/* 
 	 * ======================================== 
 	 * 				API METHODS
 	 * ======================================== 
 	 */
 	
 	/**
 	 * Checks the validity of a given authentication token.
 	 * As there is no specific method in the API to check it,
 	 * a request to /home is done to find out if the 
 	 * authentication token given is valid
 	 * 
 	 * @param authenticationToken
 	 * @return
 	 */
 	public ServerResponse checkAuthenticationTokenValidity(String authenticationToken) {
 		ServerResponse response;
 		
 		if (authenticationToken == null) {
 			// TODO Add better control of Base64 Encoding exception
 			return null;
 		} else {
 			String uri = Constants.getServerURI() + Constants.USER_INFO_PATH;
 			response = getRequestAuthorizationBasic(uri, authenticationToken);
 		}
 		return response;
 	}
 	
 	
 	/* 
 	 * ======================================== 
 	 * 				HTTP GET REQUESTS 
 	 * ======================================== 
 	 */
 	
 	
 	/**
 	 * HTTP GET request
 	 * *****************
 	 * 
 	 * @param uri
 	 * @return
 	 */
 	private ServerResponse getRequest(String uri) {
 		return this.getRequestAuthorizationBasic(uri, Model.getAuthenticationToken());
 	}
 	
 	/**
 	 * HTTP GET request (Authorization: Basic)
 	 * ****************************************
 	 * 
 	 * @param uri
 	 * @param authenticationToken
 	 * @return
 	 */
 	private ServerResponse getRequestAuthorizationBasic(String uri, String authenticationToken) {
 		
 		// the string to store the response text from the server
         ServerResponse response = new ServerResponse();
         
 		try {
 			URL url = new URL(uri);
 	        HttpURLConnection getConnection = (HttpURLConnection) url.openConnection();
 	        getConnection.setDoInput(true);
 	        getConnection.setRequestMethod("GET");
 	        getConnection.setRequestProperty("Authorization", authenticationToken);
 
 	        // Starts the query
 	        getConnection.connect();
 	        
 //	        Log.i("GET request: ", uri);
 	        
 	        int responseCode = getConnection.getResponseCode();
 	        response.setResponseCode(responseCode);
 	        
 	        InputStream inStream;
 	        if(CommunicationUtils.isErrorResponseCode(responseCode)){
 	        	inStream = getConnection.getErrorStream();
 	        } else {
 	        	inStream = getConnection.getInputStream();
 	        }
   
 	        InputStream inBufStream = new BufferedInputStream(inStream);
 	        String responseText = CommunicationUtils.readStream(inBufStream);
 	        inStream.close();
 	        
 	        String contentType = CommunicationUtils.getContentType(getConnection.getContentType());
 	        if(contentType.equals(Constants.FORMAT_JSON)){
 	        	try {
 	        		response.setResponseResult(new JSONObject(responseText));
 	        	} catch (JSONException e){
 	        		e.printStackTrace();
 	        	}
 	        } else if(contentType.equals(Constants.FORMAT_HTML)){
 	        	//TODO...
 	        }
 	       
 	        // Close getConnections
 	        getConnection.disconnect();
 		} catch(MalformedURLException e) {
 			e.printStackTrace();
 		} catch(IOException e) {
 			if (e.getMessage().contains("authentication challenge")) {
 				//401: Unauthorized
 				response.setResponseCode(HttpURLConnection.HTTP_UNAUTHORIZED);
 			}
 			e.printStackTrace();
 		}
 		return response;
 	}
 	
 	
 	
 	/* 
 	 * ======================================== 
 	 * 				HTTP POST REQUESTS 
 	 * ======================================== 
 	 */
 	
 	/**
 	 * UPLOAD DOCUMENT
 	 * ******************
 	 * 
 	 * @param filePath
 	 * @param Title
 	 * @param Description
 	 * @return
 	 */
 	public static ServerResponse uploadDocument(String filePath, String title, String description) {
 		return postRequestUploadFile(Constants.getServerURI()+Constants.DOCUMENTS_PATH, filePath, title, description, Model.getAuthenticationToken());
 	}
 	
 	/**
 	 * HTTP POST request (Authorization: Basic)
 	 * ******************************************
 	 * Request to upload a file using multipart and basic authentication.
 	 * @param url
 	 * @param filePath
 	 * @param authenticationToken
 	 * @return
 	 */
 	private static ServerResponse postRequestUploadFile(String uri, String filePath, String title, String description, String authenticationToken) {
 		ServerResponse response = new ServerResponse();
 			
 		Log.d("postRequestUploadFile with uri",uri);
 		Log.d("postRequestUploadFile with filePath",filePath);
 		Log.d("postRequestUploadFile with title",title);
 		Log.d("postRequestUploadFile with description",description);
 		
 		HttpURLConnection connection = null;
 		DataOutputStream outputStream = null;
 	
 		File file = new File(filePath);
 	
 		try	{		
 			URL url = new URL(uri);
 			connection = (HttpURLConnection) url.openConnection();
 		
 			// Allow Inputs & Outputs
 			connection.setDoInput(true);
 			connection.setDoOutput(true);
 			connection.setUseCaches(false);
 	
 			// Enable POST method
 			connection.setRequestMethod("POST");
 		        
 		    // Authorization header
 		    connection.setRequestProperty("Authorization", authenticationToken);  
 		        
 			connection.setRequestProperty("Connection", "Keep-Alive");
 			
 			//Multipart Header
 			String boundary = CommunicationUtils.generateBoundary();
 			connection.setRequestProperty("Content-Type", "multipart/form-data;boundary="+boundary);
 			
 			outputStream = new DataOutputStream(connection.getOutputStream());
 			
 			CommunicationUtils.writeFileInMultipartField(outputStream,file);
 			if(title!=null){
 				CommunicationUtils.writeMultipartField(outputStream, "Content-Disposition: form-data; name=\"document[title]\"",title);
 			}
 			if(description!=null){
 				CommunicationUtils.writeMultipartField(outputStream, "Content-Disposition: form-data; name=\"document[description]\"",description);
 			}
 						
 			outputStream.writeBytes(CommunicationUtils.twoHyphens + boundary + CommunicationUtils.twoHyphens + CommunicationUtils.lineEnd);
 	
 			outputStream.flush();
 			outputStream.close();
 			
 			// Responses from the server (code and message)
 			int responseCode = connection.getResponseCode();
 			response.setResponseCode(responseCode);
 			Log.d("Server Response code", Integer.toString(responseCode));
 			
 			String serverResponseMessage = connection.getResponseMessage();
 			Log.d("serverResponseMessage",serverResponseMessage);
 			
 			InputStream inStream;
 	        if(CommunicationUtils.isErrorResponseCode(responseCode)){
 	        	inStream = connection.getErrorStream();
 	        } else {
 	        	inStream = connection.getInputStream();
 	        }
 	        InputStream inBufStream = new BufferedInputStream(inStream);
 	        String responseText = CommunicationUtils.readStream(inBufStream);
 	        inStream.close();
 	        
 	        Log.d("responseText",responseText);
 	        
 	        String contentType = CommunicationUtils.getContentType(connection.getContentType());
 	        if(contentType.equals(Constants.FORMAT_JSON)){
 	        	try {
 	        		response.setResponseResult(new JSONObject(responseText));
 	        	} catch (JSONException e){
 	        		e.printStackTrace();
 	        	}
 	        }
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		return response;
 	}
 	
 	
 	/**
 	 * Just for Testing
 	 * @return
 	 */
 	public static ServerResponse uploadTestDocument(){
		return postRequestUploadFile(Constants.getServerURI()+Constants.DOCUMENTS_PATH, Constants.PATH_IMAGE_TEST, "Title", "Description", Constants.AUTH_TOKEN_TEST);
 	}
 	
 
 }
