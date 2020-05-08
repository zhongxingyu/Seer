 package com.paymium.paytunia.PaytuniaAPI;
 
 import java.io.BufferedReader;
 import java.io.DataOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.UnsupportedEncodingException;
 import java.lang.reflect.Type;
 import java.net.HttpURLConnection;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import org.apache.commons.codec.binary.Base64;
 
 import com.google.gson.Gson;
 import com.google.gson.GsonBuilder;
 import com.google.gson.JsonElement;
 import com.google.gson.JsonObject;
 import com.google.gson.reflect.TypeToken;
 import com.paymium.paytunia.PaytuniaAPI.exceptions.ConnectionNotInitializedException;
 
 public final class Connection 
 {
 
 	private String backendUrl;
 	private String username;
 	private String password;
 	private Gson gson;
 	private String authenticationString;
 	private Boolean isInitialized = false;
 	private static Connection instance;
 	private static Account cachedAccount=null;
 	
 	private String header;
 
 	private Connection() 
 	{
 		super();
 	}
 
 	public final static Connection getInstance() 
 	{
 		if (Connection.instance == null) 
 		{
 			synchronized (Connection.class) 
 			{
 				if (Connection.instance == null) 
 				{
 					Connection.instance = new Connection();
 				}
 			}
 		}
 
 		return Connection.instance;
 	}
 
 	public Boolean isInitialized() 
 	{
 		return (isInitialized);
 	}
 
 	public Connection initialize(String backendUrl, String username, String password)
 	{
 		this.backendUrl = backendUrl;
 		this.username = username;
 		this.password = password;
 
 		GsonBuilder gsonBuilder = new GsonBuilder();
 
 		// TODO : Handle timezones properly
 		gson = gsonBuilder.setDateFormat("yyyy-MM-dd'T'HH:mm:ss").create();
 
 		String plainAuthenticationString = this.username + ":" + this.password;
 
 		try 
 		{
 			authenticationString = new String(Base64.encodeBase64(plainAuthenticationString.getBytes("UTF-8")));
 		} 
 		catch (UnsupportedEncodingException e) 
 		{
 			// This should never happen
 		}
 
 		this.isInitialized = true;
 
 		return (Connection.getInstance());
 	}
 
 	
 	
 	// Method GET and POST
 	private String doRequest(String httpVerb, String path) throws IOException, ConnectionNotInitializedException 
 	{
 		return (doRequest(httpVerb, path, null));
 	}
 
 	private String doRequest(String httpVerb, String path, JsonObject jsonData) throws IOException, ConnectionNotInitializedException 
 	{
 
 		if (!isInitialized()) 
 		{
 			throw new ConnectionNotInitializedException("Connection has not been initialized");
 		}
 
 		URL requestURL = new URL(backendUrl + path);
 		HttpURLConnection backendConnection = (HttpURLConnection) requestURL.openConnection();
 
 		backendConnection.setRequestProperty("Authorization", "Basic " + authenticationString);
 		backendConnection.setRequestProperty("Accept", "application/json");
 		backendConnection.setRequestMethod(httpVerb);
 
 		if (httpVerb == HttpVerb.POST) 
 		{
 			if (jsonData == null) 
 			{
 				throw new IllegalArgumentException("Cannot POST with empty body");
 			}
 
 			String jsonString = jsonData.toString();
 
 			backendConnection.setDoOutput(true);
 			backendConnection.setRequestProperty("Content-Type", "application/json");
 			backendConnection.setRequestProperty("Content-Length", Integer.toString(jsonString.getBytes().length));
 
 			// Send request
 			DataOutputStream dataOutputStream = new DataOutputStream(backendConnection.getOutputStream());
 			dataOutputStream.writeBytes(jsonString);
 			dataOutputStream.flush();
 			dataOutputStream.close();
 
 		}
 
 		
 		if (backendConnection.getResponseCode() > 400)
 		{
 			InputStream errorStream = backendConnection.getErrorStream();
 			BufferedReader errorReader = new BufferedReader(new InputStreamReader(errorStream));
 			
 			StringBuilder errorBuilder = new StringBuilder();
 			String line = null;
 			
 			while ((line = errorReader.readLine()) != null) 
 			{
 				errorBuilder.append(line);
 			}
 
 			System.out.println("Code d'erreur : " + backendConnection.getResponseCode());
 			System.out.println("Message d'erreur : " + errorBuilder.toString());
 			System.out.println("Return : " + String.valueOf(backendConnection.getResponseCode()) + " " + errorBuilder.toString());
 			
 			return ("Resulat : " + String.valueOf(backendConnection.getResponseCode()) + " " + errorBuilder.toString());
 
 		}
 		
 		else
 		{
 			backendConnection.connect();
 			InputStream responseStream = (InputStream) backendConnection.getInputStream();
 			BufferedReader responseReader = new BufferedReader(new InputStreamReader(responseStream));
 
 			StringBuilder responseBuilder = new StringBuilder();
 			String line = null;
 			
 			while ((line = responseReader.readLine()) != null) 
 			{
 				responseBuilder.append(line);
 			}
 			backendConnection.disconnect();	
 			
 			System.out.println("Code de succes :"+backendConnection.getResponseCode());
 			System.out.println("Message de succes :"+responseBuilder.toString());
 			System.out.println("Message de succes de la pagniation  (S'il existe) : "+backendConnection.getHeaderField("Pagination"));
 			this.header = backendConnection.getHeaderField("Pagination");			
 			System.out.println("Return " + responseBuilder.toString());
 			
 			return (responseBuilder.toString());
 		}
 	}
 
 	
 	// Method get transactions (premier page - 20 recent transactions)
 	public ArrayList<Transfer> getTransfers() throws IOException, ConnectionNotInitializedException 
 	{
 		Type transfersArray = new TypeToken<ArrayList<Transfer>>() {}.getType();
 		return (gson.fromJson(doRequest(HttpVerb.GET, "/account/transfers"), transfersArray));
 	}
 	
 	
 	// Method get transactions (according to page number and number of transactions per page)
 	public ArrayList<Transfer> getTransfers(int page,int per_page) throws IOException, ConnectionNotInitializedException 
 	{
 		Type transfersArray = new TypeToken<ArrayList<Transfer>>() {}.getType();
 		if (page == 0 && per_page == 0)
 		{
 			return (gson.fromJson(doRequest(HttpVerb.GET, "/account/transfers"), transfersArray));
 		}
 		else
 		{
 			return (gson.fromJson(doRequest(HttpVerb.GET, "/account/transfers?page="+page+"&"+"per_page="+per_page), transfersArray));
 		}
 		
 	}
 	
 	// Method get description of invoice (Get HEADER of RESPONSE)
 	public Invoice getInvoice(int page,int per_page) throws IOException, ConnectionNotInitializedException 
 	{
 		if (page == 0 && per_page == 0)
 		{
 			doRequest(HttpVerb.GET, "/account/transfers");
 			return (gson.fromJson(header, Invoice.class));
 		}
 		else
 		{
 			doRequest(HttpVerb.GET, "/account/transfers?page="+page+"&"+"per_page="+per_page);
 			return (gson.fromJson(header, Invoice.class));
 		}
 	}
 	
 	
 	// Authentication
 	public Account getAccount() throws IOException, ConnectionNotInitializedException 
 	{
 		return (getAccount(false));
 	}
 
 	public Account getAccount(Boolean cached) throws IOException, ConnectionNotInitializedException 
 	{
 		//if(!cached || cachedAccount == null)
 		//{
 			Pattern pattern;
 			Matcher matcher;
 			pattern = Pattern.compile("UNCONFIRMED_BTC");
 			matcher = pattern.matcher(doRequest(HttpVerb.GET, "/account"));
 			
 			if (matcher.find())
 			{
 				System.out.println("AUTHENTICATION SUCCESSFUL!!");
 				cachedAccount = gson.fromJson(doRequest(HttpVerb.GET, "/account"), Account.class);
 				return cachedAccount;
 			}		
 		//}	
 		System.out.println("AUTHENTICATION FAIL!!");
 		return null;
 
 		/*if (!cached || cachedAccount == null) 
 		{
 			cachedAccount = gson.fromJson(doRequest(HttpVerb.GET, "/account"), Account.class);
 		}
 		
 		return (cachedAccount);*/
 	}
 
 	// Post a transfer
 	public Transfer postTransfer(Transfer transfer) throws IOException, ConnectionNotInitializedException 
 	{
 		JsonElement transferData = gson.toJsonTree(transfer, Transfer.class);
 		JsonObject jsonData = new JsonObject();
 
 		jsonData.add("transfer", transferData);
 
 		Pattern pattern;
 		Matcher matcher;
 		pattern = Pattern.compile("address",1);
 		pattern = Pattern.compile("amount",2);
 		pattern = Pattern.compile(transfer.getAmount().toString(),3);
 		pattern = Pattern.compile("email",4);
 
		String response = doRequest(HttpVerb.POST, "/account/transfers", jsonData);
		matcher = pattern.matcher(response);
 		
 		if( matcher.find(1) && matcher.find(2) && matcher.find(3) && matcher.find(4))
 		{
 			System.out.println("Your transfer is done");
			return (gson.fromJson(response, Transfer.class));
 		}
 		
 		System.out.println("Please check the address of beneficiary");
		
 		return null;
 
 	}
 
 	
 	//Register your device id
 	public String registerDevice(String deviceId) throws IOException, ConnectionNotInitializedException 
 	{
 		JsonElement deviceData = gson.toJsonTree(new NewDevice(deviceId), NewDevice.class);
 		JsonObject jsonData = new JsonObject();
 
 		jsonData.add("android_device", deviceData);
 
 		return doRequest(HttpVerb.POST, "/user/android_devices", jsonData);
 	}
 	
 	//Delete your device id
 	public Device deleteDevice(String deviceId) throws IOException, ConnectionNotInitializedException 
 	{	
 		Device device = gson.fromJson(this.registerDevice(deviceId), Device.class);
 		
 		doRequest(HttpVerb.DELETE, "/user/android_devices/" + String.valueOf(device.getId()));
 		
 		return device;
 	}
 }
