 package il.technion.ewolf.server.jsonDataHandlers;
 
 import static il.technion.ewolf.server.EWolfResponse.RES_BAD_REQUEST;
 import static il.technion.ewolf.server.EWolfResponse.RES_GENERIC_ERROR;
 
 import org.apache.commons.configuration.ConfigurationException;
 
 import il.technion.ewolf.server.EWolfResponse;
 import il.technion.ewolf.server.ServerResources;
 
 import com.google.gson.Gson;
 import com.google.gson.JsonElement;
 
 public class CreateAccountHandler implements JsonDataHandler {
 
 	private String configFile;
 
 	public CreateAccountHandler(String configFile) {
 		this.configFile = configFile;
 	}
 
 	private static class JsonReqCreateAccountParams {
		String username;
 		String name;
 		String password;
 	}
 
 	static class CreateAccountResponse extends EWolfResponse {
 		public CreateAccountResponse(String result) {
 			super(result);
 		}
 		public CreateAccountResponse() {
 		}
 	}
 
 	@Override
 	public Object handleData(JsonElement jsonReq) {
 		Gson gson = new Gson();
 		JsonReqCreateAccountParams jsonReqParams;
 		try {
 			jsonReqParams = gson.fromJson(jsonReq, JsonReqCreateAccountParams.class);
 		} catch (Exception e) {
 			e.printStackTrace();
 			return new CreateAccountResponse(RES_BAD_REQUEST);
 		}
 
		if (jsonReqParams.username == null || jsonReqParams.name == null
 										  || jsonReqParams.password == null) {
 			return new CreateAccountResponse(RES_BAD_REQUEST + 
 					": must specify username, name and password.");
 		}
		if (!jsonReqParams.username.matches("[a-zA-z0-9]+")) {
			return new CreateAccountResponse(RES_BAD_REQUEST +
					":username can contain only digits and letters.");
		}
 		try {
			ServerResources.setUserConfigurations(configFile, jsonReqParams.username,
 					jsonReqParams.name, jsonReqParams.password);
 		} catch (ConfigurationException e) {
 			e.printStackTrace();
 			return new CreateAccountResponse(RES_GENERIC_ERROR +
 					": Error while loading the properties file");
 		}
 		return new CreateAccountResponse();
 	}
 
 }
