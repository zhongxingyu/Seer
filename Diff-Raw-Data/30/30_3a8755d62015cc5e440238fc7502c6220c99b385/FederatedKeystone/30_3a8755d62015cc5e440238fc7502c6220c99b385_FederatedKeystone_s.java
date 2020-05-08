 package keystone;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.apache.http.HttpResponse;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.entity.StringEntity;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.json.JSONArray;
 import org.json.JSONObject;
 
 import connection.MyHttpClientTrustAll;
 import connection.OurUtil;
 
 public abstract class FederatedKeystone {
 
 	protected DefaultHttpClient httpClient;
 
 	public FederatedKeystone() {
 		this.httpClient = new MyHttpClientTrustAll();
 	}
 
 	public List<String> getRealmList(String keystoneEndpoint) throws Exception{
 		
 		HttpPost httpPostRequest = new HttpPost(keystoneEndpoint);
 		try {			
             
             //cria json sem conteudo e o insere no corpo (body) da requisicao
             StringEntity entity = new StringEntity("{}");
             
           
             entity.setContentType("application/json");
             httpPostRequest.setEntity(entity);
             httpPostRequest.addHeader("Content-type","application/json");
             httpPostRequest.addHeader("X-Authentication-Type","federated");
             
             System.out.println("request: " + httpPostRequest.toString());
             
             //vai tratar a resposta da requisio
             HttpResponse resp = httpClient.execute(httpPostRequest);
             
             //transforma resposta em uma string contendo o json da resposta
             String response = OurUtil.httpEntityToString(resp.getEntity());
             
             JSONObject jsonResp = new JSONObject(response);
             
             //OBS.: realm=IDP
             JSONArray realms = jsonResp.getJSONArray("realms");
             
             ArrayList<String> idps = new ArrayList<String>();
             
             for (int i = 0; i < realms.length(); ++i) {
 				JSONObject realm = realms.getJSONObject(i);
 				
 				idps.add(realm.getString("name"));
 				System.out.println("realm: " + realm.getString("name"));
 			}
             
             return idps;
 		} finally {
 			httpPostRequest.abort();
 	    }
 	}
 	
 	public String getIdPRequest(String keystoneEndpoint, String realm) {
 		
 		return null;
 		
 	}
 	
 	public abstract String getIdPResponse(String idpEndpoint, String idpRequest) ;
 	
 	public List<String> getUnscopedToken(String keystone, String idpResponse, String realm) {
 		return null;
 	}
 	
	public abstract void getScopedToken(String keystoneEndpoint, String idpResponse, String tenantFn);
 	
 	public String swapTokens(String keystoneEndpoint, String unscopedToken,
 			String tenantId) throws Exception {
 		
 		String result = null;
 		HttpPost httpPostRequest = new HttpPost(keystoneEndpoint + "/tokens");
 		
 		try {
 			StringEntity entity = new StringEntity(
 					"{\"auth\" : " +
 						"{\"token\" : " +
 							"{\"id\" : \"" + unscopedToken + "\"}, " + 
 							" \"tenantId\" : \"" + tenantId + "\"" +
 							"}"
 					+ "}");
 			entity.setContentType("application/json");
 			httpPostRequest.setEntity(entity);
 			httpPostRequest.addHeader("Content-type", "application/json");
 
 			HttpResponse keystoneResponse = httpClient.execute(httpPostRequest);
 
 			String responseAsString = OurUtil
 					.httpEntityToString(keystoneResponse.getEntity());
 
 			System.out.println("\n\nKeystone Scoped TOKEN:\n"
 					+ responseAsString);
 
 			return result;
 
 		} finally {
 			httpPostRequest.abort();
 		}
 	}
 }
