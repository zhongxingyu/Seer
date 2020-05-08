 package com.lenin.project.service;
 
 import java.io.BufferedReader;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 
 import javax.crypto.Mac;
 import javax.crypto.spec.SecretKeySpec;
 
 import org.apache.commons.codec.binary.Hex;
 import org.apache.http.HttpEntity;
 import org.apache.http.HttpResponse;
 import org.apache.http.NameValuePair;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.entity.UrlEncodedFormEntity;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.message.BasicNameValuePair;
 import org.codehaus.jettison.json.JSONException;
 import org.codehaus.jettison.json.JSONObject;
 
 import com.lenin.project.domain.Transaction;
 
 public class BtceApi {
 	
 	private static long _nonce = System.currentTimeMillis() / 10000L;
 	
 	private static String _key = "XSR43QT2-B7PBL6EY-U6JCVFCM-7IMTI26B-7XEL3DGO";
 	private static String _secret = "a93adec600bd65960d26d779343b70700fbb4a93e333e15350b2bb1a21fb46de";
 	
 	public static Double currentRateLtcUsd = 0.0;
 	public static Double currentBuyRateLtcUsd = 0.0;
 	public static Double currentSellRateLtcUsd = 0.0;
 	
 	public static Double oldRateLtcUsd = 0.0;
 	
 	public static Double transactionFee = 0.002;
 	
 	
 	public static void updateRates() {
 		
 		try {
 	    	
 			//Create connection
 			HttpClient client = new DefaultHttpClient();
 			HttpGet get = new HttpGet("https://btc-e.com/api/2/ltc_usd/ticker");
     	
 			HttpResponse httpResponse = client.execute(get);
 			HttpEntity entity = httpResponse.getEntity();
 			
 			if(entity != null) {
     	    
 				InputStream instream = entity.getContent();
     	       
 				BufferedReader in = 
 					new BufferedReader(new InputStreamReader(instream));
     	        
 				String resultStr = "";
 				String inputLine = null;
 					
 				while((inputLine = in.readLine()) != null) {
 					//System.out.println(inputLine);
 					resultStr += inputLine;
 				}
     	        
 				in.close();
     	    	instream.close();
     	    	
     	    	JSONObject jsonResult = new JSONObject(resultStr);
     	    	JSONObject ticker = jsonResult.getJSONObject("ticker");
     	    	
     	    	currentRateLtcUsd = ticker.getDouble("last");
     	    	currentBuyRateLtcUsd = ticker.getDouble("buy");
     	    	currentSellRateLtcUsd = ticker.getDouble("sell");
     	    	
     	    	if(oldRateLtcUsd == 0.0) {
     	    		oldRateLtcUsd = currentRateLtcUsd;
     	    	}
     	    	
 			}
     	
     	} catch(Exception e) {
     		
     		//e.printStackTrace();
 
   	    }
 		
 	
 	}
 	
 	
 	public static JSONObject getAccountInfo() {
 		
 		List<NameValuePair> methodParams = new ArrayList<NameValuePair>();
 		methodParams.add(new BasicNameValuePair("method", "getInfo"));
 		JSONObject userInfoResult = authenticatedHTTPRequest(methodParams);
 		
 		return userInfoResult;
 		
 	}
 	
 	
 	public static JSONObject trade(Transaction transaction, Double feeFactor) {
 		
 		List<NameValuePair> methodParams = new ArrayList<NameValuePair>();
 		methodParams.add(new BasicNameValuePair("method", "Trade"));
 		methodParams.add(new BasicNameValuePair("type", transaction.getType()));
 		methodParams.add(new BasicNameValuePair("pair", transaction.getPair()));
 		methodParams.add(new BasicNameValuePair("amount", ""+(transaction.getAmount()*feeFactor)));
 		methodParams.add(new BasicNameValuePair("rate", ""+transaction.getRate()));
 		
 		JSONObject tradeResult = authenticatedHTTPRequest(methodParams);
 		
 		return tradeResult;
 		
 	}
 	
 	
 	public static Transaction createTransaction(String pair, Double amount, Double rate, String type) {
 		
 		Transaction transaction = new Transaction();
 		
 		transaction.setTime((new Date()).getTime());
 		transaction.setPair(pair);
 		transaction.setAmount(amount);
 		transaction.setRate(rate);
 		transaction.setType(type);
 		
 		return transaction;
 		
 	}
 	
 	
 	public static JSONObject authenticatedHTTPRequest(List<NameValuePair> methodParams) {
         
 		// Request parameters and other properties.
         List<NameValuePair> params = new ArrayList<NameValuePair>(2);
         params.add(new BasicNameValuePair("nonce", "" + ++_nonce));
         
         String paramsString = "nonce="+_nonce;
     	
         if(methodParams != null) {
         	for(NameValuePair nvp : methodParams) {
         		params.add(nvp);
         		paramsString += "&"+nvp.getName()+"="+nvp.getValue();
         	}
         }
         
         //System.out.println(paramsString);
         
         HttpClient httpclient = new DefaultHttpClient();
         HttpPost httppost = new HttpPost("https://btc-e.com/tapi");
         
         try {
         	
         	UrlEncodedFormEntity uefe = new UrlEncodedFormEntity(params, "UTF-8");
             httppost.setEntity(uefe);
             
     		SecretKeySpec key = new SecretKeySpec(_secret.getBytes("UTF-8"), "HmacSHA512");
     		Mac mac = Mac.getInstance("HmacSHA512" );
         	mac.init(key);
         	
         	String sign = Hex.encodeHexString(mac.doFinal(paramsString.getBytes("UTF-8")));
         	httppost.addHeader("Key", _key);
         	httppost.addHeader("Sign", sign);
         	
         } catch(Exception e) {
         	e.printStackTrace();
         }
         
         String result = "";
         
         try {
         
         	//Execute and get the response.
         	HttpResponse response = httpclient.execute(httppost);
         	HttpEntity entity = response.getEntity();
 
         	if(entity != null) {
         	
         		InputStream instream = entity.getContent();
         		
         		BufferedReader rd = new BufferedReader(new InputStreamReader(instream));
         		//System.out.println("Ready to read result...");
         	
         		String line = null;
         		while((line = rd.readLine()) != null) {
             		result += line;
             	}
             	
         	}
             
         } catch(Exception e) {
     		
        	System.out.println("Failed to make trade request: "+e.getMessage());
        	//e.printStackTrace();
         	return null;
         			
         }
         
         try {
         	
         	//System.out.println(result);
         	
         	JSONObject jsonResult = new JSONObject(result);
         	
         	return jsonResult;
         	
         } catch(JSONException e) {
         	
        	System.out.println("API server did not return a proper response. Error message: "+e.getMessage());
        	//e.printStackTrace();
         	return null;
         	
         }
         
         
         
     }
 
 
 	
 }
