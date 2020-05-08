 package iKnodeSdk;
 
 import java.net.HttpURLConnection;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.OutputStream;
 import java.io.InputStream;
 
 import com.google.gson.Gson;
 
 /**
  * Defines the Application Client.
  * 
  * @author jgemedina
  * @version 0.1
  */
 public final class ApplicationClient {
 	/**
 	 * Defines the base service url.
 	 * 
 	 * @since 0.1
 	 */
 	private static final String SERVICE_URL = "https://api.iknode.com";
 	
 	/**
 	 * User Id.
 	 * 
 	 * @since 0.1
 	 */
 	private String _userId;
 	
 	/**
 	 * API Key.
 	 * 
 	 * @since 0.1
 	 */
 	private String _apiKey;
 	
 	/**
 	 * Application Name.
 	 * 
 	 * @since 0.1
 	 */
 	private String _applicationName;
 	
 	/**
 	 * Creates a new instance of the {@link ApplicationClient} class.
 	 * 
 	 * @param userId User Id.
 	 * @param apiKey API Key.
 	 * @param applicationName Application Name.
 	 * 
 	 * @since 0.1
 	 */
 	public ApplicationClient(final String userId, final String apiKey, final String applicationName)
 	{
 		this._userId = userId;
 		this._apiKey = apiKey;
 		this._applicationName = applicationName;
 	}
 
 	/**
 	 * Executes the method with the selected parameters for the current application.
 	 * 
 	 * @param resultType Result Type expected.
 	 * @param methodName Method Name.
 	 * @param parameters Variable Parameters to use.
 	 * @return Result Object.
 	 * @since 0.1
 	 */
	public <T extends Object> T execute(final Class<T> resultType, final String methodName, final MethodParameter... parameters)
 	{
 		final String result = this.executeRequest(this._userId, this._apiKey, this._applicationName, methodName, parameters);
 		
 		T responseObject = new Gson().fromJson(result, resultType);
 		
 		return responseObject;
 	}
 		
 	/**
 	 * Executes the method with the selected parameters for the current application.
 	 * 
 	 * @param userId User Id.
 	 * @param apiKey API Key.
 	 * @param applicationName Application Name.
 	 * @param methodName Method to execute.
 	 * @param parameters Variable parameters to use.
 	 * @return Result Object.
 	 * @since 0.1
 	 */
 	private String executeRequest(final String userId, final String apiKey, final String applicationName, final String methodName, final MethodParameter... parameters)
 	{	
 		String result;
 		final String appServiceUrl = String.format("%s/Applications/execute/%s/%s", SERVICE_URL, applicationName, methodName);
 		
 		try {
 			HttpURLConnection connection = this.getRequestUrlConnection(appServiceUrl);
 			
 			connection.setRequestMethod("POST");
 			connection.setRequestProperty("Content-Type", "application/json");
 			connection.addRequestProperty("iKnode-UserId", userId);
 			connection.addRequestProperty("iKnode-ApiKey", apiKey);
 			
 			// Make the request...
 			byte[] requestData = this.formatParameters(parameters).getBytes();
 			OutputStream os = connection.getOutputStream();
 			os.write(requestData, 0, requestData.length);
 			
 			// Let's hear back from it!
 			InputStream responseStream = connection.getInputStream();
 			BufferedReader bufferedStream = new BufferedReader(new InputStreamReader(responseStream));
 			
 			String line;
 			StringBuffer responseBuffr = new StringBuffer();
 			while((line = bufferedStream.readLine()) != null) {
 				responseBuffr.append(line);
 			}
 			
 			result = this.cleanResult(responseBuffr.toString());
 		} catch(Exception ex) {
 			result = null;
 		}
 		
 		return result;
 	}
 	
 	/**
 	 * Gets the connection to the Service Url.
 	 * 
 	 * @param appServiceUrl Service Url.
 	 * @return {@link HttpURLConnection} instance.
 	 * @since 0.1
 	 */
 	private HttpURLConnection getRequestUrlConnection(final String appServiceUrl)
 			throws MalformedURLException,
 				   IOException,
 				   IllegalStateException
 	{
 		HttpURLConnection connection;
 				
 		try {
 			final URL url = new URL(appServiceUrl);
 			connection = (HttpURLConnection) url.openConnection();
 			connection.setDoOutput(true);
 			connection.setDoInput(true);
 		} catch (MalformedURLException muex) {			
 			connection = null;
 		} catch(IOException ioex) {			
 			connection = null;
 		} catch(IllegalStateException isex) {
 			connection = null;
 		}
 
 		return connection;
 	}
 	
 	/**
 	 * Cleans the execution result string.
 	 * 
 	 * @param result Result string
 	 * @return Cleaned result string
 	 * @since 0.1
 	 */
	private String cleanResult(String result)
 	{
		if(result.startsWith("<string")) {
			if(result.endsWith("</string>")) {
				result = result.replace("<string xmlns=\"http://schemas.microsoft.com/2003/10/Serialization/\">", "");
				result = result.replace("</string>", "");		
			} else {
				result = result.replace("<string xmlns=\"http://schemas.microsoft.com/2003/10/Serialization/\" />", "");
			}			
		}
 		
		result = result.replaceAll("^\"|\"$", "");
		result = result.replaceAll("\\\\\"", "\"");
 		
		return result;
 	}
 	
 	/**
 	 * Formats the parameter array to be used for the JSON REST Command.
 	 *  
 	 * @param parameters Parameters array.
 	 * @return Formatted parameters string.
 	 * @since 0.1
 	 */
 	private String formatParameters(final MethodParameter... parameters)
 	{
 		StringBuffer params = new StringBuffer();
 		
 		params.append("{ \"parameters\": \"{");
 
         int index = 0;
         int count = parameters.length;
         
         for(MethodParameter parameter : parameters) {
             String paramValue = parameter.getValue().toString();
 
             paramValue = new Gson().toJson(parameter.getValue());
             paramValue = paramValue.replace("\"", "\\\"");
 
             params.append(String.format(" %s:'%s'", parameter.getName(), paramValue));
             
             if(++index < count) {
                 params.append(",");
             }
         }
 
         params.append("}\" }");
 
         return params.toString();		
 	}	
 }
