 package com.magicmicky.habitrpgmobileapp.onlineapi;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.UnsupportedEncodingException;
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.net.UnknownHostException;
 import java.util.zip.GZIPInputStream;
 
 import org.apache.http.Header;
 import org.apache.http.HttpEntity;
 import org.apache.http.HttpResponse;
 import org.apache.http.client.ClientProtocolException;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.methods.HttpRequestBase;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.json.JSONException;
 import org.json.JSONObject;
 /**
  * The main class that do the interaction with the webservice.
  * @author Mickael
  *
  */
 public abstract class WebServiceInteraction {
 	final private static String SUFFIX = "api/v1/";
 	final private String CMD;
 	final private OnHabitsAPIResult callback;
 	private HostConfig config;
 	
 	
 	/**
 	 * Create a new WebServiceInteraction based on a command, and a callback to call once finished
 	 * @param command the command to GET
 	 * @param callback the callback to call with the different information about the user
 	 * @param config the host configuration
 	 */
 	public WebServiceInteraction(String command, OnHabitsAPIResult callback, HostConfig config) {
 		this.CMD = command;
 		this.callback=callback;
 		this.config=config;
 	}
 	
 	/**
 	 * Get the data from the WebService
 	 * @return the {@link Answer}
 	 */
 	public final Answer getData() {
 		Answer result=null;
 	        // Making HTTP request
 	        try {
 	            // defaultHttpClient
 	            HttpClient client = new DefaultHttpClient();
 	            HttpRequestBase request = this.getRequest();
 	            String address = config.getAddress();
 	            address = address.concat(address.charAt(address.length()-1) == '/' ? SUFFIX + this.CMD : "/api/v1/" + this.CMD);
 
 	            request.setURI(new URI(address));
 	            request.addHeader("x-api-key", config.getApi());
 	            request.addHeader("x-api-user", config.getUser());
 	            request.addHeader("Accept-Encoding", "gzip");
 	            
 	            HttpResponse response = client.execute(request);
 	            if(response.getStatusLine().getStatusCode() !=200) {
 	            	if(response.getStatusLine().getStatusCode() == 504) {
 	            		throw new WebServiceException("The server is experiencing issues. Please either wait or change server.");
	            	} else if(response.getStatusLine().getStatusCode() == 401) {
	            		throw new WebServiceException("There was a problem with the authentication. Please check your settings.");
 	            	}
 	            	System.out.println(response.getStatusLine().getStatusCode() + "-" + response.getStatusLine().getReasonPhrase());
 	            	//throw new Exception("The server didn't answer 200");
 	            }
 	            HttpEntity httpEntity = response.getEntity();
 	            InputStream is = httpEntity.getContent();          
 	            Header contentEncoding = response.getFirstHeader("Content-Encoding");
 	            if (contentEncoding != null && contentEncoding.getValue().equalsIgnoreCase("gzip")) {
 	                is = new GZIPInputStream(is);
 	            }
 
 	            
 	            BufferedReader reader = new BufferedReader(new InputStreamReader(
 	                    is));
 	            StringBuilder sb = new StringBuilder();
 	            String line = null;
 	            while ((line = reader.readLine()) != null) {
 	                sb.append(line + "\n");
 
 	            }
 	            is.close();
 	            String jsonstr = sb.toString();
 	            System.out.println(jsonstr);
 	            JSONObject jsonObj = new JSONObject(jsonstr);
 	            result = findAnswer(jsonObj);
             } catch (UnsupportedEncodingException e) {
	            this.callback.onError("There was an error with the Encoding. Please check the server's address");
 	            e.printStackTrace();
 	        } catch (ClientProtocolException e) {
 	            this.callback.onError("There was an error with the client protocol?!");
 	            e.printStackTrace();
 	        } catch(UnknownHostException e) {
 	            this.callback.onError("No address associated with hostname. Please check your connection and your host settings");
 	        	e.printStackTrace();
 			}  catch (IOException e) {
 	            this.callback.onError("An error happened... Are you still connected to the internet?");
 	            e.printStackTrace();
 
 	        } catch(WebServiceException e) {
 				this.callback.onError(e.getMessage());
 				e.printStackTrace(); 
 			} catch (JSONException e) {
	            this.callback.onError("The server returned an unexpected answer. Feel free to change server in the settings");
 	            e.printStackTrace();
 
 	        } catch (URISyntaxException e) {
	            this.callback.onError("The server's URL isn't well formatted. Please check the server address in the settings");
 	        	e.printStackTrace();
 			}
 	        catch (Exception e) {
	            this.callback.onError("An unknown error happend... Maybe because of your connection, or of your settings?");
 	        	e.printStackTrace();
 			}
 	 
 	        return result;
 	 
 	    }
 	/**
 	 * Return the request pre filled with misc data (POST, GET, PUT, DELETE...)
 	 * @return the HttpRequest which is going to be used
 	 */
 	protected abstract HttpRequestBase getRequest();
 
 	/**
 	 * Retrieve the Answer based on a JSONObject
 	 * @param answer the JSONObject to parse
 	 * @return the answer object, fulfilled with the information
 	 */
 	protected abstract Answer findAnswer(JSONObject answer);
 	
 	/**
 	 * @return the callback
 	 */
 	public OnHabitsAPIResult getCallback() {
 		return callback;
 	}
 	/**
 	 * The Answer Type.<br/>
 	 * Is used to parse the JSON and call {@code callback} with the information
 	 * @author MagicMicky
 	 */
 	public abstract class Answer {
 		private JSONObject object;
 		protected OnHabitsAPIResult callback;
 		/**
 		 * Create a new Answer based on the object to parse, and the callback to call after.
 		 * @param obj the object to parse
 		 * @param callback the callback to call after parsing
 		 */
 		public Answer(JSONObject obj, OnHabitsAPIResult callback) {
 			this.setObject(obj);
 			this.callback =callback;
 		}
 		/**
 		 * Parse the {@code object}
 		 */
 		public abstract void parse();
 
 		/**
 		 * @return the object
 		 */
 		protected JSONObject getObject() {
 			return object;
 		}
 
 		/**
 		 * @param object the object to set
 		 */
 		protected void setObject(JSONObject object) {
 			this.object = object;
 		}
 	}
 	/**
 	 * The class that is used to throw exceptions from the webservice
 	 * @author Mickael
 	 */
 	private class WebServiceException extends Exception {
 		public WebServiceException(String message) {
 			super(message);
 		}
 		
 	}
 }
