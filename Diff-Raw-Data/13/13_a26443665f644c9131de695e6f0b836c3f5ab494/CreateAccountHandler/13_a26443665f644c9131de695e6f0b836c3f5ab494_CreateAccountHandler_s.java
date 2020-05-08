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
		String userame;
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
 
		if (jsonReqParams.userame == null || jsonReqParams.name == null
 										  || jsonReqParams.password == null) {
 			return new CreateAccountResponse(RES_BAD_REQUEST + 
 					": must specify username, name and password.");
 		}
 		try {
			ServerResources.setUserConfigurations(configFile, jsonReqParams.userame,
 					jsonReqParams.name, jsonReqParams.password);
 		} catch (ConfigurationException e) {
 			e.printStackTrace();
 			return new CreateAccountResponse(RES_GENERIC_ERROR +
 					": Error while loading the properties file");
 		}
 		return new CreateAccountResponse();
 	}
 
 }
