 package com.postr.Translators;
 
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.security.NoSuchAlgorithmException;
 import java.util.Calendar;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.apache.xmlrpc.XmlRpcException;
 import org.apache.xmlrpc.client.XmlRpcClient;
 import org.apache.xmlrpc.client.XmlRpcClientConfigImpl;
 
 import com.postr.DataTypes.LJData;
 import com.postr.DataTypes.PasswordEncryptor;
 import com.postr.DataTypes.StringResult;
 
 
 public class LJTranslator {
 
 		protected String serverURL = "http://www.livejournal.com/interface/xmlrpc";
 		
 		protected StringResult SuccessResult(String message){
 			StringResult result = new StringResult();
 			result.setResult(message);
 			return result;
 		}
 		
 		protected StringResult ErrorResult(String message){
 			StringResult result = new StringResult();
 			result.setErrorMessage(message);
 			return result;
 		}
 		
 		@SuppressWarnings("unchecked")
 		public StringResult Login(String userName, String password) throws Exception
 		{
 			if (true) {
				return  SuccessResult("Logged in as Test User");	
 			}
 		    
 		    XmlRpcClient client = getClient();
 		    password = EncryptPassword(password);
 		    HashMap<String, Object> loginParams = getInitialisedCallParams(client, userName, password);
 		    Object[] params = new Object[]{loginParams};
 		    Map<String, String> postResult;
 		    try{
 		    	postResult =  (Map<String, String>) client.execute("LJ.XMLRPC.login", params);
 		    } catch (XmlRpcException e){
 		    	return ErrorResult(e.getMessage());
 		    }
 		    
 		    if (postResult.get("success")=="FAIL"){
 		    	return ErrorResult(postResult.get("errmsg"));
 		    }
 		    return SuccessResult("Logged in as " + postResult.get("fullname"));
 		}
 
 		
 		
 		@SuppressWarnings("unchecked")
 		public String Write(LJData ljData, String contents, String header, List<String> tags)  throws Exception{
 		    XmlRpcClient client = getClient();
 		    
 		    HashMap<String, Object> postParams = getInitialisedCallParams(client,ljData.getUserName(),ljData.getPassword());
 		    
 		    postParams.put("event", contents);
 		    postParams.put("subject", header) ;
 		    if (ljData.getPostPrivately()) {
 			    postParams.put("security","private");
 			}
 
 		    Calendar calendar = Calendar.getInstance(ljData.getTimeZone());
 		    postParams.put("year",calendar.get(Calendar.YEAR));
 		    postParams.put("mon",calendar.get(Calendar.MONTH)+1);
 		    postParams.put("day",calendar.get(Calendar.DAY_OF_MONTH));
 		    postParams.put("hour",calendar.get(Calendar.HOUR_OF_DAY));
 		    postParams.put("min",calendar.get(Calendar.MINUTE));
 		    
 		    HashMap<String,Object> options = new HashMap<String,Object>();
 		    String tagsToUse = "";
 		    for (String tag : tags) {
 		    	if(tagsToUse.length()>0)
 		    	{
 		    		tagsToUse+=",";
 		    	}
 				tagsToUse +=tag; 
 			}
 		    options.put("taglist", tagsToUse);
 		    options.put("opt_preformatted", true);
 		    postParams.put("props",options);
 		    
 		    Object[] params = new Object[]{postParams};
 		    Map<String, String> postResult;
 		    try{
 		    	postResult =  (Map<String, String>) client.execute("LJ.XMLRPC.postevent", params);
 		    } catch (XmlRpcException e){
 		    	if (e.getMessage().equals("Invalid password")){
 		    		return "Invalid Password";
 		    	} else{
 		    		throw e;
 		    	}
 		    }
 		    
 		    if (postResult.get("success")=="FAIL"){
 		    	return postResult.get("errmsg");
 		    }
 		    return "<A href=" + postResult.get("url")+ ">Link posted</A>";
 		}
 
 
 
 		@SuppressWarnings("unchecked")
 		private HashMap<String, Object> getInitialisedCallParams(
 				XmlRpcClient client, String userName, String password) throws XmlRpcException,
 				NoSuchAlgorithmException {
 			Map<String, String> challengeResult =  (Map<String, String>) client.execute("LJ.XMLRPC.getchallenge", new Object[0]);
 		    
 		    String challenge = (String) challengeResult.get("challenge");
 		    
 		    String response = PasswordEncryptor.MD5Hex(challenge+password);
 	    
 		    HashMap<String,Object> postParams = new HashMap<String,Object>();
 		    postParams.put("username", userName);
 		    postParams.put("auth_method", "challenge");
 		    postParams.put("auth_challenge", challenge);
 		    postParams.put("auth_response", response);
 			return postParams;
 		}
 
 
 
 		private XmlRpcClient getClient() throws MalformedURLException {
 			XmlRpcClientConfigImpl config = new XmlRpcClientConfigImpl();
 			config.setServerURL(new URL(serverURL));
 		    XmlRpcClient client = new XmlRpcClient();
 		    client.setConfig(config);
 			return client;
 		}
 		
 		public String EncryptPassword(String password) throws Exception{
 			return PasswordEncryptor.MD5Hex(password);
 		}
 }
