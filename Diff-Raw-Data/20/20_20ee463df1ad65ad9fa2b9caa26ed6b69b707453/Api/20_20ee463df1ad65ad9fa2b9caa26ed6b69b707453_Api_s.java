 package net.dmcloud.cloudkey;
 
 import java.io.IOException;
 import java.util.Map;
 import net.dmcloud.util.*;
 import org.codehaus.jackson.map.ObjectMapper;
 
 public class Api
 {
 	public static int SECLEVEL_NONE = 0;
 	public static int SECLEVEL_DELEGATE = 1 << 0;
 	public static int SECLEVEL_ASNUM = 1 << 1;
 	public static int SECLEVEL_IP = 1 << 2;
 	public static int SECLEVEL_USERAGENT = 1 << 3;
 	public static int SECLEVEL_USEONCE = 1 << 4;
 	public static int SECLEVEL_COUNTRY = 1 << 5;
 	public static int SECLEVEL_REFERER = 1 << 6;
 	public static int SECLEVEL_REFERER_STRICT = 1 << 15;
 
 	public static String API_URL = "http://api.dmcloud.net";
 	public static String CDN_URL = "http://cdn.dmcloud.net";
 	public static String STATIC_URL = "http://static.dmcloud.net";
 
 	protected String user_id = null;
 	protected String api_key = null;
 	protected String base_url = null;
 	protected String cdn_url = null;
 	protected String proxy = null;
 
 	public Api(String _user_id, String _api_key)
 	{
 		this(_user_id, _api_key, CloudKey.API_URL, CloudKey.CDN_URL, "");
 	}
 
 	public Api(String _user_id, String _api_key, String _base_url, String _cdn_url, String _proxy)
 	{
 		this.user_id = _user_id;
 		this.api_key = _api_key;
 		this.base_url = _base_url;
 		this.cdn_url = _cdn_url;
 		this.proxy = _proxy;
 	}
 
 	public DCObject call(String call, DCObject args) throws DCException
 	{
 		ObjectMapper mapper = new ObjectMapper();
 
 		DCObject jo = DCObject.create()
 							  .push("call", call)
 							  .push("args", args);
 
 		jo.push("auth", this.user_id + ":"  + Helpers.md5(this.user_id + Helpers.normalize(jo) + this.api_key));
 
         String json_encoded = null;
         DCObject json_response = null;
         try {
             json_encoded = mapper.writeValueAsString(jo);
         } catch(IOException e) {
             throw new DCException("JSON serialization error: " + e.getMessage());
         }
 		String response = Helpers.curl(this.base_url + "/api", json_encoded);
         try {
             json_response = DCObject.create(mapper.readValue(response, Map.class));
         } catch(IOException e) {
             throw new DCException("JSON deserialization error: " + e.getMessage());
         }
 		if (json_response.containsKey("error"))
 		{
 			String message = "";
 			int error_code = Integer.parseInt(json_response.pull("error.code"));
 			switch (error_code)
 			{
 				case 200  : message = "ProcessorException"; break;
 				case 300  : message = "TransportException"; break;
 				case 400  : message = "AuthenticationErrorException"; break;
 				case 410  : message = "RateLimitExceededException"; break;
 				case 500  : message = "SerializerException"; break;
 
 				case 600  : message = "InvalidRequestException"; break;
 				case 610  : message = "InvalidObjectException"; break;
 				case 620  : message = "InvalidMethodException"; break;
 				case 630  : message = "InvalidParamException"; break;
 
 				case 1000 : message = "ApplicationException"; break;
 				case 1010 : message = "NotFoundException"; break;
 				case 1020 : message = "ExistsException"; break;
 				case 1030 : message = "LimitExceededException"; break;
 
 				default   : message = "RPCException (error:" + json_response.pull("error.code").toString() + ")"; break;
 			}
 			message += " : " + json_response.pull("error.message").toString();
 			throw new DCException(message, error_code);
 		}
 		return DCObject.create((Map)json_response.get("result"));
 	}
 }
