 package com.bmat.ella;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.net.URL;
 import java.net.URLConnection;
 import java.net.URLEncoder;
 import java.util.HashMap;
 
 import org.json.simple.JSONObject;
 import org.json.simple.parser.JSONParser;
 import org.json.simple.parser.ParseException;
 
 
 public class Request {
 	
 	private EllaConnection ellaConnection;
 	JSONParser jsonParser;
 	
 	public Request(EllaConnection ellaConnection){
 		this.ellaConnection = ellaConnection;
 		this.jsonParser = new JSONParser();
 	}
 	
 	
 	public JSONObject execute(String method, String collection, HashMap<String, String> searchTerms)throws ServiceException, IOException{
 		try{
 			JSONObject jsonObj = (JSONObject) this.jsonParser.parse(this.downloadResponse(method, collection, searchTerms));
 			Object response = jsonObj.get("response");
 			
 			if(response != null){
 				return (JSONObject) response;
 			}
 			else{
 				JSONObject error = (JSONObject)jsonObj.get("error");
 				throw new ServiceException((String)error.get("type"), (String)error.get("message"));
 			}
 		}
		catch(ParseException e){
			throw new RuntimeException(e);
 		}
 		
 	}
 	
 	private String downloadResponse(String method, String collection, HashMap<String, String> searchTerms) throws IOException{
 		String params = "";
 		String sep = "";
 		for(String key : searchTerms.keySet()){
 			params += sep + key + "=" + URLEncoder.encode(searchTerms.get(key), "utf-8"); 
 			sep = "&";
 		}
 		String url = this.ellaConnection.getEllaws() + "collections/" + collection + method + "?" + params;
		System.out.println("url: " + url);
 		
 		URLConnection urlCon = new URL(url).openConnection();
 		BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(urlCon.getInputStream()));
 		String inputLine, jsonResponse = "";		
 		while((inputLine = bufferedReader.readLine()) != null){
 			jsonResponse += inputLine;
 		}
 		bufferedReader.close();
		System.out.println("JSON RESPONSE: " + jsonResponse);
 		return jsonResponse;
 	}
 	
 	public EllaConnection getEllaConnection(){
 		return this.ellaConnection;
 	}
 }
