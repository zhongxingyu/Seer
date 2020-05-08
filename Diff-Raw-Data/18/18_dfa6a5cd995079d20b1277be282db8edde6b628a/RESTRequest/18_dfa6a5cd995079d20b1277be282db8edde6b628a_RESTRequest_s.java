 package com.example.getconnected.rest;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.UnsupportedEncodingException;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Scanner;
 
 import org.apache.http.HttpResponse;
 import org.apache.http.NameValuePair;
 import org.apache.http.client.entity.UrlEncodedFormEntity;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.client.methods.HttpPut;
 import org.apache.http.client.methods.HttpUriRequest;
 import org.apache.http.client.utils.URLEncodedUtils;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.message.BasicNameValuePair;
 
 import android.os.AsyncTask;
 
 /**
  * The RESTRequest class simplifies the process of making RESTful requests to the web address of choice.
  */
 public class RESTRequest extends AsyncTask<Void, Void, String>
 {
 	/** The enumeration of available HTTP request methods. */
 	public enum Method { GET, POST, PUT };
 	
 	/** The enumeration of request's default accepted data types. */
 	public enum HeaderAcceptedData
 	{
 		TEXT ("text/plain"), HTML ("text/html"), JSON ("application/json"), XML ("application/xml");
 		
 		private String headerAcceptedData;
 		
 		/**
 		 * @param headerAcceptedData
 		 */
 		private HeaderAcceptedData(String headerAcceptedData)
 		{
 			this.headerAcceptedData = headerAcceptedData;
 		}
 		
 		/**
 		 * @return headerAcceptedData
 		 */
 		public String getHeaderAcceptedData()
 		{
 			return headerAcceptedData;
 		}
 	};
 	
 	/** The address an HTTP request will be sent to. */
 	protected String address;
 	
 	/** The method with which the HTTP request is sent. */
 	protected Method method;
 	
 	/** The accepted data type to set in the request's Accept header. */
 	protected String headerAcceptedData;
 	
 	/** The ID to pass along with the event. This helps recognizing an event fired by a class that needs to handle multiple REST requests. */
 	protected String ID;
 	
 	/** The parameters sent along with the request. */
 	protected List<NameValuePair> parameters;
 	
 	/** The listener classes that need to be notified of a finished request. */
 	protected ArrayList<RESTRequestListener> eventListeners;
 	
 	/**
 	 * Overloads the RESTRequest(String address, String ID) constructor.
 	 * 
 	 * @param address
 	 */
 	public RESTRequest(String address)
 	{
 		this(address, "");
 	}
 	
 	/**
 	 * Overloads the RESTRequest(String address, Method method, String ID) constructor.
 	 * 
 	 * @param address,
 	 * @param ID
 	 */
 	public RESTRequest(String address, String ID)
 	{
 		this(address, Method.GET, ID);
 	}
 	
 	/**
 	 * Overloads the RESTRequest(String address, Method method, String headerAcceptedData, String ID) constructor.
 	 * 
 	 * @param address
 	 * @param method
 	 * @param ID
 	 */
 	public RESTRequest(String address, Method method, String ID)
 	{
 		this(address, method, HeaderAcceptedData.JSON.getHeaderAcceptedData(), ID);
 	}
 	
 	/**
 	 * Constructs a new RESTRequest with the address, method, and accepted data type.
 	 * 
 	 * The passed ID will be sent along with the fired event to be able to recognize the specific
 	 * event in a class that handles multiple REST requests.
 	 * 
 	 * @param address
 	 * @param method
 	 * @param headerAcceptedData
 	 * @param ID
 	 */
 	public RESTRequest(String address, Method method, String headerAcceptedData, String ID)
 	{
 		this.address            = address;
 		this.method             = method;
 		this.headerAcceptedData = headerAcceptedData;
 		this.ID                 = ID;
 		
 		parameters = new ArrayList<NameValuePair>();
 		
 		eventListeners = new ArrayList<RESTRequestListener>();
 	}
 	
 	/**
 	 * @return address
 	 */
 	public String getAddress()
 	{
 		return address;
 	}
 
 	/**
 	 * @param address
 	 */
 	public void setAddress(String address)
 	{
 		this.address = address;
 	}
 	
 	/**
 	 * @return method
 	 */
 	public Method getMethod()
 	{
 		return method;
 	}
 	
 	/**
 	 * @param method
 	 */
 	public void setMethod(Method method)
 	{
 		this.method = method;
 	}
 	
 	/**
 	 * @return headerAcceptedData
 	 */
 	public String getHeaderAcceptedData()
 	{
 		return headerAcceptedData;
 	}
 	
 	/**
 	 * @param headerAcceptedData
 	 */
 	public void setHeaderAcceptedData(String headerAcceptedData)
 	{
 		this.headerAcceptedData = headerAcceptedData;
 	}
 	
 	/**
 	 * @return ID
 	 */
 	public String getID()
 	{
 		return ID;
 	}
 
 	/**
 	 * @param address
 	 */
 	public void setID(String ID)
 	{
 		this.ID = ID;
 	}
 	
 	/**
 	 * @param key
 	 * @param value
 	 */
 	public void putString(String key, String value)
 	{
 		parameters.add(new BasicNameValuePair(key, value));
 	}
 	
 	/**
 	 * @param eventListener
 	 */
 	public void addEventListener(RESTRequestListener eventListener)
 	{
 		this.eventListeners.add(eventListener);
 	}
 	
 	@Override
 	protected String doInBackground(Void... voids)
 	{
 		DefaultHttpClient defaultHttpClient = new DefaultHttpClient();
 
 		HttpUriRequest httpRequest = null; 
 		
 		// Get the correct request method
 		try
 		{
 			switch (method)
 			{
 				case GET:
 					// Set URL and encode parameters
					System.out.println(URLEncodedUtils.format(parameters, "utf-8"));
					httpRequest = new HttpGet(address + URLEncodedUtils.format(parameters, "utf-8"));
 					break;
 	
 				case POST:
 					httpRequest = new HttpPost(address);
 					
 					// Encode parameters as entity
 					((HttpPost) httpRequest).setEntity(new UrlEncodedFormEntity(parameters));
 					break;
 					
 				case PUT:
 					httpRequest = new HttpPut(address);
 					
 					// Encode parameters as entity
 					((HttpPut) httpRequest).setEntity(new UrlEncodedFormEntity(parameters));
 					break;
 					
 				default:
 					return "-1";
 			}
 		}
 		catch (IllegalArgumentException e)
 		{
 			e.printStackTrace();
 			return "-2";
 		}
 		catch (UnsupportedEncodingException e)
 		{
 			return "-3";
 		}
 		
 		// Indicate what data needs to be received
 		httpRequest.setHeader("Accept", headerAcceptedData);
 
 		InputStream inputStream = null;
 		
 		Scanner scanner = null;
 		
 		try
 		{
 			// Run request
 			HttpResponse httpResponse = defaultHttpClient.execute(httpRequest);
 			
 			// Get content of response
 			inputStream = httpResponse.getEntity().getContent();
 			
 			// Trick to read all data from a stream in one line: https://weblogs.java.net/blog/pat/archive/2004/10/stupid_scanner_1.html
 			scanner = new Scanner(inputStream).useDelimiter("\\A");
 
 			String result = "";
 			
 			// Read lines into result
 			while (scanner.hasNext())
 			{
 				result += scanner.next();
 			}
 			
 			if (result.length() > 0)
 			{
 				return result;
 			}
 		}
 		catch (IOException e)
 		{
 			return "-4";
 		}
 		finally // Close opened utilities
 		{
 			if (scanner != null)
 			{
 				scanner.close();
 			}
 			
 			if (inputStream != null)
 			{
 				try
 				{
 					inputStream.close();
 				}
 				catch (IOException e) { }
 			}
 		}
 		
 		return "-5";
 	}
 	
 	@Override
 	protected void onPreExecute()
 	{
 		super.onPreExecute();
 		
 		for (RESTRequestListener eventListener : eventListeners)
 		{
 			// Create new RESTRequestEvent to be handled by the event listener
 			eventListener.RESTRequestOnPreExecute(new RESTRequestEvent(this, ID));
 		}
 	}
 	
 	@Override
 	protected void onProgressUpdate(Void... voids)
 	{
 		super.onProgressUpdate(voids);
 		
 		for (RESTRequestListener eventListener : eventListeners)
 		{
 			// Create new RESTRequestEvent to be handled by the event listener
 			eventListener.RESTRequestOnProgressUpdate(new RESTRequestEvent(this, ID));
 		}
 	}
 	
 	@Override
 	protected void onPostExecute(String result)
 	{
 		for (RESTRequestListener eventListener : eventListeners)
 		{
 			// Create new RESTRequestEvent to be handled by the event listener
 			eventListener.RESTRequestOnPostExecute(new RESTRequestEvent(this, result, ID));
 		}
 	}
 }
