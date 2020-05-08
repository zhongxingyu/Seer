 package federacao;
 
 import java.io.IOException;
 import java.net.URLEncoder;
 import java.util.ArrayList;
 import java.util.List;
 
 import keystone.FederatedKeystone;
 
 import org.apache.commons.codec.binary.Base64;
 import org.apache.http.HttpHost;
 import org.apache.http.HttpResponse;
 import org.apache.http.NameValuePair;
 import org.apache.http.client.ClientProtocolException;
 import org.apache.http.client.entity.UrlEncodedFormEntity;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.client.methods.HttpUriRequest;
 import org.apache.http.entity.StringEntity;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.message.BasicNameValuePair;
 import org.apache.http.protocol.BasicHttpContext;
 import org.apache.http.protocol.ExecutionContext;
 import org.apache.http.protocol.HTTP;
 import org.apache.http.protocol.HttpContext;
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 import org.jsoup.Jsoup;
 import org.jsoup.nodes.Document;
 import org.jsoup.nodes.Element;
 
 import connection.MyHttpClientTrustAll;
 import connection.OurUtil;
 
 public class AuthFederatedKeystone extends FederatedKeystone{
 	
 	static String KEYSTONE_ENDPOINT = "http://cana.ect.ufrn.br:5000/v2.0";
 	
 	public AuthFederatedKeystone() {
 		super();
 	}
 
 	public static void main(String[] args) {
 		
 		try {
 		AuthFederatedKeystone authFed = new AuthFederatedKeystone();
 		
 		//Pegando lista de IDPs
 		List<String> idpList = authFed.getRealmList(KEYSTONE_ENDPOINT);
 		
 		
 		authFed.authFederatedKeystone("http://cana.ect.ufrn.br:5000/v2.0");
 		}catch (Exception ex) {
 			ex.getMessage();
 			ex.printStackTrace();
 		}
 	}
 	
 	/**
 	 * Makes the federated authentication
 	 * @param spEndpoint
 	 * @return
 	 */
 	public String authFederatedKeystone(String spEndpoint){
 		MyHttpClientTrustAll httpclient = new MyHttpClientTrustAll();
 		//TEST
 		try {
 			
 			//para testes, seleciona atravs do .get(int);
 			String idpName = getIDPlistFromSP(spEndpoint, httpclient).get(2);
 
 			System.out.println("Selecionou o IDP "+ idpName);
 			String[] IDPcomp = getIDPspecificEndpointAndSAML("http://cana.ect.ufrn.br:5000/v2.0", idpName, httpclient);
 			
 			String idpResponse =sendSAMLtoIDPandSubmitCredentials(IDPcomp[0], IDPcomp[1], httpclient, "student", "student");
 
 			sendSAMlrespToKeystone(idpResponse, idpName, spEndpoint, httpclient);
 //			
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (Exception e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		
 		return " ";
 	}
 	
 	
 	/**
 	 * Creates a POST request to the Service Provide (federated keystone), requesting the IDP list
 	 * 
 	 * @param spEndpoint
 	 * @return
 	 */
 	public ArrayList<String> getIDPlistFromSP(String spEndpoint, DefaultHttpClient userSpecificHttpClient) throws IOException, Exception{
 		
 		HttpPost httppost = new HttpPost(spEndpoint);
 		
 		try {			
             
            //cria json sem conteudo e o insere no corpo (body) da requisi������o
             StringEntity entity = new StringEntity("{}");
             
           
             entity.setContentType("application/json");
             httppost.setEntity(entity);
             httppost.addHeader("Content-type","application/json");
             httppost.addHeader("X-Authentication-Type","federated");
             
             System.out.println("request: "+httppost.toString());
             
             //vai tratar a resposta da requisio
             HttpResponse resp= userSpecificHttpClient.execute(httppost);
             
             //transforma resposta em uma string contendo o json da resposta
             String response = OurUtil.httpEntityToString(resp.getEntity());
             
             JSONObject jsonResp = new JSONObject(response);
             
             //OBS.: realm=IDP
             JSONArray realms = jsonResp.getJSONArray("realms");
             
             ArrayList<String> idps = new ArrayList<String>();
             
             for (int i = 0; i < realms.length(); ++i) {
 				JSONObject realm = realms.getJSONObject(i);
 				
 				idps.add(realm.getString("name"));
 				System.out.println("realm: "+realm.getString("name"));
 			}
             
             return idps;
 		} finally {
 			httppost.abort();
 	    }
 	}
 	
 	/**
 	 * Requests the SP a IDP's endpoint and the SAML needed to send to it in order to authentication on the IDP
 	 * 
 	 * @param idpName
 	 * @return
 	 * @throws IOException 
 	 * @throws ClientProtocolException 
 	 * @throws JSONException 
 	 */
 	public String[] getIDPspecificEndpointAndSAML(String spEndpoint, String idpName, DefaultHttpClient userSpecificHttpClient) throws ClientProtocolException, IOException, JSONException{
 		
 		String[] responses = new String[2];
 		
 		
 		HttpPost httppost = new HttpPost(spEndpoint);
 		
 		try {			
             
             //cria json com crenciais para requisitar autenticacao
             StringEntity entity = new StringEntity("{\"realm\": {\"name\":\""+idpName+"\"}}");
             
           
             entity.setContentType("application/json");
             httppost.setEntity(entity);
             httppost.addHeader("Content-type","application/json");
             httppost.addHeader("X-Authentication-Type","federated");
             
             
             //vai tratar a resposta da requisi����o
             HttpResponse requestResp= userSpecificHttpClient.execute(httppost);
             
             //transforma resposta em uma string contendo o json da resposta
             String responseAsString = OurUtil.httpEntityToString(requestResp.getEntity());
             
             JSONObject jsonResp = new JSONObject(responseAsString);
             
            responses[0]=jsonResp.getString("idpEndpoint"); 
            responses[1]=jsonResp.getString("idpRequest");
 
            
 //           System.out.println("idpEndpoint:\n"+responses[0]);
 //           
 //           System.out.println("idpRequest:\n"+responses[1]);
             
             return responses;
 		} finally {
 			httppost.abort();
 	    }
 		
 	}
 	
 	
 	
 	/**
 	 * This method will send the SAML received from SP to the selected IDP, that will return a IDP specific login html page. Then the user's credentials are
 	 * submited to the IDP the same way the page would. As a response, the IDP sends a HTML page containing a SAML 
 	 * @param endpoint
 	 * @param SAMLtoSend
 	 * @param userSpecificHttpClient
 	 * @param username
 	 * @param password
 	 * @return SAML to SP
 	 * @throws ClientProtocolException
 	 * @throws IOException
 	 */
 	public String sendSAMLtoIDPandSubmitCredentials(String endpoint, String SAMLtoSend, DefaultHttpClient userSpecificHttpClient, String username, String password) throws ClientProtocolException, IOException{
 		
 		System.out.println("sending SAML request to IDP endpoint: \n"+endpoint+SAMLtoSend);
 		HttpGet httpGet = new HttpGet(endpoint+SAMLtoSend);
 		
 		try {			
 			HttpContext context = new BasicHttpContext();
 			
             //vai pegar a resposta de requisi������o, que ��� uma p���gina HTML
             HttpResponse requestResp= userSpecificHttpClient.execute(httpGet, context);
             
             //transforma resposta em uma string contendo o json da resposta
             String responseAsString = OurUtil.httpEntityToString(requestResp.getEntity());
             
               
             HttpUriRequest currentReq = (HttpUriRequest) context.getAttribute(ExecutionContext.HTTP_REQUEST);
     		HttpHost currentHost = (HttpHost)  context.getAttribute(ExecutionContext.HTTP_TARGET_HOST);
     		String currentUrl = currentHost.toURI() + currentReq.getURI();
     		
             
           //Transforma o html recebido na requisio anterior 
     		Document idpDoc = Jsoup.parse(responseAsString);
     		
     	 //Pega o componente HTML "form" que contem o formulrio de insero de login e senha do usuario
     		Element idpFormElement = idpDoc.select("form").get(0);
     		
     		
     		/**Submitting user+password form **/
     		
     		HttpPost httpPost2 = new HttpPost(currentUrl);
     		List<NameValuePair> nameValuePairs2 = new ArrayList<NameValuePair>();
     		nameValuePairs2.add(new BasicNameValuePair("username", username));
     		nameValuePairs2.add(new BasicNameValuePair("password", password));
     		httpPost2.setEntity(new UrlEncodedFormEntity(nameValuePairs2, HTTP.UTF_8));
     		HttpResponse response2 = userSpecificHttpClient.execute(httpPost2);
     		String authResponseHtml = OurUtil.httpEntityToString(response2.getEntity());
     		
     		System.out.println("\nauthResponse\n"+authResponseHtml);
     		
 //    		System.out.println("*************Cookies after GETTING SAMLresponse FROM IDP*************");
 //    		
 //    		for(Cookie cookie: userSpecificHttpClient.getCookieStore().getCookies()){
 //    			System.out.println("cookie name: "+ cookie.getName());
 //    			System.out.println("cookie value: "+ cookie.getValue());
 //    			System.out.println("cookie toString: "+ cookie.toString());
 //    			System.out.println("cookie toString: ");
 //    		}
 //    		
 //    		System.out.println("***************************************");
 //    		
     		//Transforma o html recebido na requisio anterior 
     		Document idpDoc2 = Jsoup.parse(authResponseHtml);
     		
     	 //Pega o componente HTML "form" que contm o formulrio de insero de login e senha do usurio
     		Element idpSAMLresponse = idpDoc2.select("input").get(1);
 //    		System.out.println("PEGOU SAMLRESP "+  idpSAMLresponse.attr("value"));
             return  idpSAMLresponse.attr("value");
 		} finally {
 			httpGet.abort();
 	    }
 	}
 	
 	
 	
 	
 	public void sendSAMlrespToKeystone(String idpResponse, String idpName, String spEndpoint, DefaultHttpClient userSpecificHttpClient) throws JSONException, ClientProtocolException, IOException{
 		
 //		System.out.println("sendSAMlrespToKeystone endpoint: "+ spEndpoint );
 //		System.out.println("sendSAMlrespToKeystone idpResponse: "+ idpResponse );
 		System.out.println("\n\n <<<<SPENDPOINT SENDING SAMLResponse>> "+ spEndpoint +"\n\n");
 		HttpPost httppost = new HttpPost(spEndpoint);
 		
 		try {			
             
 			
 			Base64 decoder = new  Base64();
 			
 			
 			String samlDecoded=new String(decoder.decode(idpResponse));
 			
 			System.out.println("\n\n<INI>>>Saml decoded: \n\n"+samlDecoded+ "\n <FIM DECODED>>>>>>");
 			
 //			idpResponse = new String(decoder.encode(samlDecoded.getBytes()));
 			
 			//SAMLResponse obtido pelo Thomas, se descomentar a linha a seguir, funciona
 //			idpResponse="PHNhbWxwOlJlc3BvbnNlIHhtbG5zOnNhbWxwPSJ1cm46b2FzaXM6bmFtZXM6dGM6U0FNTDoyLjA6cHJvdG9jb2wiIHhtbG5zOnNhbWw9InVybjpvYXNpczpuYW1lczp0YzpTQU1MOjIuMDphc3NlcnRpb24iIElEPSJfMjM3NGY3N2Y3OWVmMjIyNWU0OTM3YmQ2YWZlODYyNDgxNTFiNzExNTUwIiBWZXJzaW9uPSIyLjAiIElzc3VlSW5zdGFudD0iMjAxMy0wNi0xN1QxODo1MjozN1oiIERlc3RpbmF0aW9uPSJodHRwczovL2xvY2FsaG9zdDo4MDgwIiBJblJlc3BvbnNlVG89InVybjp1dWlkOjIwYzZmMjRhLTQ5N2QtNDRlNS1iYzU3LWE3MzA0MTc0N2FhZCI%2BPHNhbWw6SXNzdWVyPmh0dHBzOi8vaWRwLmVjdC51ZnJuLmJyL3NpbXBsZXNhbWwvc2FtbDIvaWRwL21ldGFkYXRhLnBocDwvc2FtbDpJc3N1ZXI%2BPGRzOlNpZ25hdHVyZSB4bWxuczpkcz0iaHR0cDovL3d3dy53My5vcmcvMjAwMC8wOS94bWxkc2lnIyI%2BCiAgPGRzOlNpZ25lZEluZm8%2BPGRzOkNhbm9uaWNhbGl6YXRpb25NZXRob2QgQWxnb3JpdGhtPSJodHRwOi8vd3d3LnczLm9yZy8yMDAxLzEwL3htbC1leGMtYzE0biMiLz4KICAgIDxkczpTaWduYXR1cmVNZXRob2QgQWxnb3JpdGhtPSJodHRwOi8vd3d3LnczLm9yZy8yMDAwLzA5L3htbGRzaWcjcnNhLXNoYTEiLz4KICA8ZHM6UmVmZXJlbmNlIFVSST0iI18yMzc0Zjc3Zjc5ZWYyMjI1ZTQ5MzdiZDZhZmU4NjI0ODE1MWI3MTE1NTAiPjxkczpUcmFuc2Zvcm1zPjxkczpUcmFuc2Zvcm0gQWxnb3JpdGhtPSJodHRwOi8vd3d3LnczLm9yZy8yMDAwLzA5L3htbGRzaWcjZW52ZWxvcGVkLXNpZ25hdHVyZSIvPjxkczpUcmFuc2Zvcm0gQWxnb3JpdGhtPSJodHRwOi8vd3d3LnczLm9yZy8yMDAxLzEwL3htbC1leGMtYzE0biMiLz48L2RzOlRyYW5zZm9ybXM%2BPGRzOkRpZ2VzdE1ldGhvZCBBbGdvcml0aG09Imh0dHA6Ly93d3cudzMub3JnLzIwMDAvMDkveG1sZHNpZyNzaGExIi8%2BPGRzOkRpZ2VzdFZhbHVlPlpkVzZkcE5DektsQ0M1Z3BmTDB2N1FGckNmVT08L2RzOkRpZ2VzdFZhbHVlPjwvZHM6UmVmZXJlbmNlPjwvZHM6U2lnbmVkSW5mbz48ZHM6U2lnbmF0dXJlVmFsdWU%2BSTFmZDBJZXRXaXV4c3FJUDVSNUZtdElyVDFRSjVUUXJnaDl6NEZydWtqaUdvZStYQUozQUJqYzdJTzhPVTdXbU5jY0taT3RZMFlWSDFDL2pXY3h2T1JTdXNRNzl5VTFUdWpYN3dzZjZkZ3BqWWlJRytycUJ0ZzVRb0pnN1RacGdGVS9MZldRR0hjbG5Uc1hVZ09jTGVQczcrRDZLU0NrRG03OTF4Z0tyQUZjenpBZk5WS3FPaFFBb3dVWEROY1djcXAzWmdUcjRPa3RtcFBnb0Z4MmxYSkVabjhoSHJNVzFBa3BMbVY2bG1RTWZrSHZlYVBuNTIrVjl4cG5Ebi9kUG1TbG1ITmN0V0V0SUZFd0Z2SWsxTWtFd1k5cWdVVkFvY2lybnVKd1ZBcnl1THlTanpjb0xYY0RndlA3Vnp5K1Z5TEo3aEJIUUdzdjZrNkR3Vy9pNXR3PT08L2RzOlNpZ25hdHVyZVZhbHVlPgo8ZHM6S2V5SW5mbz48ZHM6WDUwOURhdGE%2BPGRzOlg1MDlDZXJ0aWZpY2F0ZT5NSUlEL3pDQ0F1ZWdBd0lCQWdJSkFQRmVvWnhFSmNXcU1BMEdDU3FHU0liM0RRRUJCUVVBTUlHVk1Rc3dDUVlEVlFRR0V3SmljakVjTUJvR0ExVUVDQXdUY21sdklHZHlZVzVrWlNCa2J5QnViM0owWlRFT01Bd0dBMVVFQnd3RmJtRjBZV3d4RFRBTEJnTlZCQW9NQkhWbWNtNHhEREFLQmdOVkJBc01BMlZqZERFWU1CWUdBMVVFQXd3UGFXUndMbVZqZEM1MVpuSnVMbUp5TVNFd0h3WUpLb1pJaHZjTkFRa0JGaEpyWVdSMVlYSmtiMEJuYldGcGJDNWpiMjB3SGhjTk1UTXdOREUyTVRnek9ETXhXaGNOTWpNd05ERTJNVGd6T0RNeFdqQ0JsVEVMTUFrR0ExVUVCaE1DWW5JeEhEQWFCZ05WQkFnTUUzSnBieUJuY21GdVpHVWdaRzhnYm05eWRHVXhEakFNQmdOVkJBY01CVzVoZEdGc01RMHdDd1lEVlFRS0RBUjFabkp1TVF3d0NnWURWUVFMREFObFkzUXhHREFXQmdOVkJBTU1EMmxrY0M1bFkzUXVkV1p5Ymk1aWNqRWhNQjhHQ1NxR1NJYjNEUUVKQVJZU2EyRmtkV0Z5Wkc5QVoyMWhhV3d1WTI5dE1JSUJJakFOQmdrcWhraUc5dzBCQVFFRkFBT0NBUThBTUlJQkNnS0NBUUVBeWNWUVJCMlVoTVlmd3cwQjQ3MHBrWkJEMGNGd2VEanVpYzZ6cHdDemJRVzh2RkdHWWVHemJBYjYxcDJVUFJYZzV6ZjRDZkh1RkNSRXIySXo0OTQ0UFRGRXJteVd0am9GQmdMcWtPRHRBa0xxaERMbTd4aXBUYUZ5UjEwbER6VzVXTHdiRjRuaDZGbDdpTjR1eWlOMWtWQ29JVk50eWt0YzhpVTFHT2RlYXBBNjZJbCtzbGV2Y0RySWFyYzJXNVVwWUZzdXpKb3FNRkZndU9IZGFJYXhtRm52YlBEV2psRkJkYmdLc0prZm9vL3lXeGVBbU55MStGQVdPTGNPM3lvUkdWVDk5eTJQVWlJNVoyOXg4YVVZZXlodGMrMjFyRE5VQVBTVTAvcHh2ZS91YWh0aG1YRXM4dHBnbnNhdW95MlNSOFoxWUtiUjJqRysvbzNsTjBTalZ3SURBUUFCbzFBd1RqQWRCZ05WSFE0RUZnUVUyVEFPRC96Rk45QjRnTWZMVUY2aVgvQm9aaVF3SHdZRFZSMGpCQmd3Rm9BVTJUQU9EL3pGTjlCNGdNZkxVRjZpWC9Cb1ppUXdEQVlEVlIwVEJBVXdBd0VCL3pBTkJna3Foa2lHOXcwQkFRVUZBQU9DQVFFQXdCTTg0UjFVcjlnLy9BZXkvNW9wMUk3QzZIbmNxOWN2QUV2Ym5sNHhUN1AzanViYURHSzc3MEZWOFl5bHRmcGlCeXRHc01qYmlTbE9CUVZCYk1QbHQreFVKd01FdSthbWlyUFBjdjlXdzcyY25WMUEwRVdpSU9IdnpRbWJoem4zd2diTXNXMVhBWGZaQkNFZzNJVDgwS2NDK0ZMREhhb2RJMzV6Vk9jV0J1cTU0SHVtRStNUmV5V21vTDd0Y0NJK05EdEJTRGlBZG5jeVpYM1ZoVXo0V1BZTE9FZnFaTkswVld6cmNrSis5dTF4VS9Wa3pJanFYL0ZCSHFVWFJoUUg2SURJbkhUNDZYQkRXUnc5Vi9yMm9jNXJaOEl6MWE3ZTBIVG1xcGRkU3lEZzFvMlZrdUJJYjdaME84Y3I2Y1oya3dyK05keUpQVjFXdlE4NWtiU2pYQT09PC9kczpYNTA5Q2VydGlmaWNhdGU%2BPC9kczpYNTA5RGF0YT48L2RzOktleUluZm8%2BPC9kczpTaWduYXR1cmU%2BPHNhbWxwOlN0YXR1cz48c2FtbHA6U3RhdHVzQ29kZSBWYWx1ZT0idXJuOm9hc2lzOm5hbWVzOnRjOlNBTUw6Mi4wOnN0YXR1czpTdWNjZXNzIi8%2BPC9zYW1scDpTdGF0dXM%2BPHNhbWw6QXNzZXJ0aW9uIHhtbG5zOnhzaT0iaHR0cDovL3d3dy53My5vcmcvMjAwMS9YTUxTY2hlbWEtaW5zdGFuY2UiIHhtbG5zOnhzPSJodHRwOi8vd3d3LnczLm9yZy8yMDAxL1hNTFNjaGVtYSIgSUQ9Il81NjU2YjFiMjA3OWFiOGRmZTNmYTg2MTI3OWRlMTVhNWEyNDdhZWY3NzMiIFZlcnNpb249IjIuMCIgSXNzdWVJbnN0YW50PSIyMDEzLTA2LTE3VDE4OjUyOjM3WiI%2BPHNhbWw6SXNzdWVyPmh0dHBzOi8vaWRwLmVjdC51ZnJuLmJyL3NpbXBsZXNhbWwvc2FtbDIvaWRwL21ldGFkYXRhLnBocDwvc2FtbDpJc3N1ZXI%2BPGRzOlNpZ25hdHVyZSB4bWxuczpkcz0iaHR0cDovL3d3dy53My5vcmcvMjAwMC8wOS94bWxkc2lnIyI%2BCiAgPGRzOlNpZ25lZEluZm8%2BPGRzOkNhbm9uaWNhbGl6YXRpb25NZXRob2QgQWxnb3JpdGhtPSJodHRwOi8vd3d3LnczLm9yZy8yMDAxLzEwL3htbC1leGMtYzE0biMiLz4KICAgIDxkczpTaWduYXR1cmVNZXRob2QgQWxnb3JpdGhtPSJodHRwOi8vd3d3LnczLm9yZy8yMDAwLzA5L3htbGRzaWcjcnNhLXNoYTEiLz4KICA8ZHM6UmVmZXJlbmNlIFVSST0iI181NjU2YjFiMjA3OWFiOGRmZTNmYTg2MTI3OWRlMTVhNWEyNDdhZWY3NzMiPjxkczpUcmFuc2Zvcm1zPjxkczpUcmFuc2Zvcm0gQWxnb3JpdGhtPSJodHRwOi8vd3d3LnczLm9yZy8yMDAwLzA5L3htbGRzaWcjZW52ZWxvcGVkLXNpZ25hdHVyZSIvPjxkczpUcmFuc2Zvcm0gQWxnb3JpdGhtPSJodHRwOi8vd3d3LnczLm9yZy8yMDAxLzEwL3htbC1leGMtYzE0biMiLz48L2RzOlRyYW5zZm9ybXM%2BPGRzOkRpZ2VzdE1ldGhvZCBBbGdvcml0aG09Imh0dHA6Ly93d3cudzMub3JnLzIwMDAvMDkveG1sZHNpZyNzaGExIi8%2BPGRzOkRpZ2VzdFZhbHVlPnBreTdpRmFkWE5VRzdOTXlWQnRLWGNGQTRiQT08L2RzOkRpZ2VzdFZhbHVlPjwvZHM6UmVmZXJlbmNlPjwvZHM6U2lnbmVkSW5mbz48ZHM6U2lnbmF0dXJlVmFsdWU%2BRFFFWWpGWFpJQVVPKzNTbXRhWUVBeTJrckFma09ldVpzKzFTKzFnd1pqbW1VM0dMamE4V3owSUVNUEdCWDdKYU5OWTBkVHl3SWpxYVBCay96ZS8yK1NvYXkrUm5JaDlZRlFqZnJjOXFXZFp2ZkhvVXhLMFAxSjQ3K211TGNGcjZodko1MHFaQkp6aXc3OWIwMGtLOWJRQVltR2hsMVduUHl6bUJRQ3I0WjVNVG51SEUxdm1DckxKbnhRVWtnbDU5T1dNVzJ6MFhuNnNVTFRFckk0NUR4VnMyR1g2SUpsSTFuZm1QV1pyczYvWnlYRllINUkrdUZvbFRuY1JQK0hXL1hTc1M0MHRXZU1HV3lMMkYwUWIrTzFYbGhkM0M5S0xxem9LK0w4UThqNnI0ZVlLN2Z5ZUQ3dXZZTVQydlpRQUhndXA0UWMyUUNuRGhkNjVuclAwWG9nPT08L2RzOlNpZ25hdHVyZVZhbHVlPgo8ZHM6S2V5SW5mbz48ZHM6WDUwOURhdGE%2BPGRzOlg1MDlDZXJ0aWZpY2F0ZT5NSUlEL3pDQ0F1ZWdBd0lCQWdJSkFQRmVvWnhFSmNXcU1BMEdDU3FHU0liM0RRRUJCUVVBTUlHVk1Rc3dDUVlEVlFRR0V3SmljakVjTUJvR0ExVUVDQXdUY21sdklHZHlZVzVrWlNCa2J5QnViM0owWlRFT01Bd0dBMVVFQnd3RmJtRjBZV3d4RFRBTEJnTlZCQW9NQkhWbWNtNHhEREFLQmdOVkJBc01BMlZqZERFWU1CWUdBMVVFQXd3UGFXUndMbVZqZEM1MVpuSnVMbUp5TVNFd0h3WUpLb1pJaHZjTkFRa0JGaEpyWVdSMVlYSmtiMEJuYldGcGJDNWpiMjB3SGhjTk1UTXdOREUyTVRnek9ETXhXaGNOTWpNd05ERTJNVGd6T0RNeFdqQ0JsVEVMTUFrR0ExVUVCaE1DWW5JeEhEQWFCZ05WQkFnTUUzSnBieUJuY21GdVpHVWdaRzhnYm05eWRHVXhEakFNQmdOVkJBY01CVzVoZEdGc01RMHdDd1lEVlFRS0RBUjFabkp1TVF3d0NnWURWUVFMREFObFkzUXhHREFXQmdOVkJBTU1EMmxrY0M1bFkzUXVkV1p5Ymk1aWNqRWhNQjhHQ1NxR1NJYjNEUUVKQVJZU2EyRmtkV0Z5Wkc5QVoyMWhhV3d1WTI5dE1JSUJJakFOQmdrcWhraUc5dzBCQVFFRkFBT0NBUThBTUlJQkNnS0NBUUVBeWNWUVJCMlVoTVlmd3cwQjQ3MHBrWkJEMGNGd2VEanVpYzZ6cHdDemJRVzh2RkdHWWVHemJBYjYxcDJVUFJYZzV6ZjRDZkh1RkNSRXIySXo0OTQ0UFRGRXJteVd0am9GQmdMcWtPRHRBa0xxaERMbTd4aXBUYUZ5UjEwbER6VzVXTHdiRjRuaDZGbDdpTjR1eWlOMWtWQ29JVk50eWt0YzhpVTFHT2RlYXBBNjZJbCtzbGV2Y0RySWFyYzJXNVVwWUZzdXpKb3FNRkZndU9IZGFJYXhtRm52YlBEV2psRkJkYmdLc0prZm9vL3lXeGVBbU55MStGQVdPTGNPM3lvUkdWVDk5eTJQVWlJNVoyOXg4YVVZZXlodGMrMjFyRE5VQVBTVTAvcHh2ZS91YWh0aG1YRXM4dHBnbnNhdW95MlNSOFoxWUtiUjJqRysvbzNsTjBTalZ3SURBUUFCbzFBd1RqQWRCZ05WSFE0RUZnUVUyVEFPRC96Rk45QjRnTWZMVUY2aVgvQm9aaVF3SHdZRFZSMGpCQmd3Rm9BVTJUQU9EL3pGTjlCNGdNZkxVRjZpWC9Cb1ppUXdEQVlEVlIwVEJBVXdBd0VCL3pBTkJna3Foa2lHOXcwQkFRVUZBQU9DQVFFQXdCTTg0UjFVcjlnLy9BZXkvNW9wMUk3QzZIbmNxOWN2QUV2Ym5sNHhUN1AzanViYURHSzc3MEZWOFl5bHRmcGlCeXRHc01qYmlTbE9CUVZCYk1QbHQreFVKd01FdSthbWlyUFBjdjlXdzcyY25WMUEwRVdpSU9IdnpRbWJoem4zd2diTXNXMVhBWGZaQkNFZzNJVDgwS2NDK0ZMREhhb2RJMzV6Vk9jV0J1cTU0SHVtRStNUmV5V21vTDd0Y0NJK05EdEJTRGlBZG5jeVpYM1ZoVXo0V1BZTE9FZnFaTkswVld6cmNrSis5dTF4VS9Wa3pJanFYL0ZCSHFVWFJoUUg2SURJbkhUNDZYQkRXUnc5Vi9yMm9jNXJaOEl6MWE3ZTBIVG1xcGRkU3lEZzFvMlZrdUJJYjdaME84Y3I2Y1oya3dyK05keUpQVjFXdlE4NWtiU2pYQT09PC9kczpYNTA5Q2VydGlmaWNhdGU%2BPC9kczpYNTA5RGF0YT48L2RzOktleUluZm8%2BPC9kczpTaWduYXR1cmU%2BPHNhbWw6U3ViamVjdD48c2FtbDpOYW1lSUQgU1BOYW1lUXVhbGlmaWVyPSJodHRwOi8vY2FuYS5lY3QudWZybi5icjo1MDAwLyIgRm9ybWF0PSJ1cm46b2FzaXM6bmFtZXM6dGM6U0FNTDoyLjA6bmFtZWlkLWZvcm1hdDp0cmFuc2llbnQiPl8zZjJiZTgwMTBiNjVhZmViMGNiYTY3ZjhiNjNlZmMwOTUxNmFmNjQ5MWI8L3NhbWw6TmFtZUlEPjxzYW1sOlN1YmplY3RDb25maXJtYXRpb24gTWV0aG9kPSJ1cm46b2FzaXM6bmFtZXM6dGM6U0FNTDoyLjA6Y206YmVhcmVyIj48c2FtbDpTdWJqZWN0Q29uZmlybWF0aW9uRGF0YSBOb3RPbk9yQWZ0ZXI9IjIwMTMtMDYtMTdUMTg6NTc6MzdaIiBSZWNpcGllbnQ9Imh0dHBzOi8vbG9jYWxob3N0OjgwODAiIEluUmVzcG9uc2VUbz0idXJuOnV1aWQ6MjBjNmYyNGEtNDk3ZC00NGU1LWJjNTctYTczMDQxNzQ3YWFkIi8%2BPC9zYW1sOlN1YmplY3RDb25maXJtYXRpb24%2BPC9zYW1sOlN1YmplY3Q%2BPHNhbWw6Q29uZGl0aW9ucyBOb3RCZWZvcmU9IjIwMTMtMDYtMTdUMTg6NTI6MDdaIiBOb3RPbk9yQWZ0ZXI9IjIwMTMtMDYtMTdUMTg6NTc6MzdaIj48c2FtbDpBdWRpZW5jZVJlc3RyaWN0aW9uPjxzYW1sOkF1ZGllbmNlPmh0dHA6Ly9jYW5hLmVjdC51ZnJuLmJyOjUwMDAvPC9zYW1sOkF1ZGllbmNlPjwvc2FtbDpBdWRpZW5jZVJlc3RyaWN0aW9uPjwvc2FtbDpDb25kaXRpb25zPjxzYW1sOkF1dGhuU3RhdGVtZW50IEF1dGhuSW5zdGFudD0iMjAxMy0wNi0xN1QxODo1MjozN1oiIFNlc3Npb25Ob3RPbk9yQWZ0ZXI9IjIwMTMtMDYtMThUMDI6NTI6MzdaIiBTZXNzaW9uSW5kZXg9Il9mYTJlMGM5MWQ0YTljZWFmMDNhZjhhYTA3NjI1ZWM2ZDNmODFmNzVjOTUiPjxzYW1sOkF1dGhuQ29udGV4dD48c2FtbDpBdXRobkNvbnRleHRDbGFzc1JlZj51cm46b2FzaXM6bmFtZXM6dGM6U0FNTDoyLjA6YWM6Y2xhc3NlczpQYXNzd29yZDwvc2FtbDpBdXRobkNvbnRleHRDbGFzc1JlZj48L3NhbWw6QXV0aG5Db250ZXh0Pjwvc2FtbDpBdXRoblN0YXRlbWVudD48c2FtbDpBdHRyaWJ1dGVTdGF0ZW1lbnQ%2BPHNhbWw6QXR0cmlidXRlIE5hbWU9ImNuIiBOYW1lRm9ybWF0PSJ1cm46b2FzaXM6bmFtZXM6dGM6U0FNTDoyLjA6YXR0cm5hbWUtZm9ybWF0OnVyaSI%2BPHNhbWw6QXR0cmlidXRlVmFsdWUgeHNpOnR5cGU9InhzOnN0cmluZyI%2Bc3R1ZGVudDwvc2FtbDpBdHRyaWJ1dGVWYWx1ZT48L3NhbWw6QXR0cmlidXRlPjxzYW1sOkF0dHJpYnV0ZSBOYW1lPSJzaCIgTmFtZUZvcm1hdD0idXJuOm9hc2lzOm5hbWVzOnRjOlNBTUw6Mi4wOmF0dHJuYW1lLWZvcm1hdDp1cmkiPjxzYW1sOkF0dHJpYnV0ZVZhbHVlIHhzaTp0eXBlPSJ4czpzdHJpbmciPnN0dWRlbnQ8L3NhbWw6QXR0cmlidXRlVmFsdWU%2BPC9zYW1sOkF0dHJpYnV0ZT48c2FtbDpBdHRyaWJ1dGUgTmFtZT0iZWR1UGVyc29uUHJpbmNpcGFsTmFtZSIgTmFtZUZvcm1hdD0idXJuOm9hc2lzOm5hbWVzOnRjOlNBTUw6Mi4wOmF0dHJuYW1lLWZvcm1hdDp1cmkiPjxzYW1sOkF0dHJpYnV0ZVZhbHVlIHhzaTp0eXBlPSJ4czpzdHJpbmciPnN0dWRlbnRzdHVkZW50QGlkcC5lY3QudWZybi5icjwvc2FtbDpBdHRyaWJ1dGVWYWx1ZT48L3NhbWw6QXR0cmlidXRlPjxzYW1sOkF0dHJpYnV0ZSBOYW1lPSJ1aWQiIE5hbWVGb3JtYXQ9InVybjpvYXNpczpuYW1lczp0YzpTQU1MOjIuMDphdHRybmFtZS1mb3JtYXQ6dXJpIj48c2FtbDpBdHRyaWJ1dGVWYWx1ZSB4c2k6dHlwZT0ieHM6c3RyaW5nIj5zdHVkZW50VWlkPC9zYW1sOkF0dHJpYnV0ZVZhbHVlPjwvc2FtbDpBdHRyaWJ1dGU%2BPHNhbWw6QXR0cmlidXRlIE5hbWU9ImJyRWR1QWZmaWxpYXRpb25UeXBlIiBOYW1lRm9ybWF0PSJ1cm46b2FzaXM6bmFtZXM6dGM6U0FNTDoyLjA6YXR0cm5hbWUtZm9ybWF0OnVyaSI%2BPHNhbWw6QXR0cmlidXRlVmFsdWUgeHNpOnR5cGU9InhzOnN0cmluZyI%2Bc3R1ZGVudDwvc2FtbDpBdHRyaWJ1dGVWYWx1ZT48L3NhbWw6QXR0cmlidXRlPjwvc2FtbDpBdHRyaWJ1dGVTdGF0ZW1lbnQ%2BPC9zYW1sOkFzc2VydGlvbj48L3NhbWxwOlJlc3BvbnNlPg%3D%3D";
 		
 			
             StringEntity entity = new StringEntity("{\"realm\":{\"name\":\""+idpName+"\"},\"idpResponse\":\"SAMLResponse=" + URLEncoder.encode(idpResponse, "UTF-8")+"\"}");
 
             System.out.println("JSON TO SEND:   <<<<<INI>>>>> \n" + OurUtil.httpEntityToString(entity)+ "\n<<<<<FIM>>>>>");
             entity.setContentType("application/json");
             httppost.setEntity(entity);
             httppost.addHeader("Content-type","application/json");
             httppost.addHeader("X-Authentication-Type","federated");
             
             
             //teste decodificando saml do thomas
 //            samlDecoded=new String(decoder.decode(idpResponse), "UTF-8");
 //			System.out.println("\n\n<INI>>>Saml decoded: \n\n"+samlDecoded+ "\n <FIM DECODED>>>>>>");
           
 			//vai tratar a resposta da requisicao 
             HttpResponse requestResp= userSpecificHttpClient.execute(httppost);
             System.out.println("Http post sendSAMlrespToKeystone executed ");
             
             //transforma resposta em uma string contendo o json da resposta
             String responseAsString = OurUtil.httpEntityToString(requestResp.getEntity());
             
 //            JSONObject jsonResp = new JSONObject(responseAsString);
             
            System.out.println("\n\nKeystone Unscoped TOKEN:\n"+responseAsString);
 //           
 		} finally {
 			httppost.abort();
 	    }
 		
 	}
 
	@Override
	public String getIdPResponse(String idpEndpoint, String idpRequest) {
		// TODO Auto-generated method stub
		return null;
	}

 }
