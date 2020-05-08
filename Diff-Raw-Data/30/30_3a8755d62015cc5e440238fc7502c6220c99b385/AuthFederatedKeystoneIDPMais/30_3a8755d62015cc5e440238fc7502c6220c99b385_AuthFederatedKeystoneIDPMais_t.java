 package federacao.idpmais;
 
 import java.io.IOException;
 import java.net.URLEncoder;
 
 import keystone.FederatedKeystone;
 
 import org.apache.http.HttpResponse;
 import org.apache.http.client.ClientProtocolException;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.entity.StringEntity;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import br.rnp.stcfed.sts.client.impl.IDPMaisClient;
 import connection.MyHttpClientTrustAll;
 import connection.OurUtil;
 
 public class AuthFederatedKeystoneIDPMais extends FederatedKeystone {
 	
 	public AuthFederatedKeystoneIDPMais() {
 		super();
 
 	}
 	
 	
 	public static void main(String args[]) {
 		
 		AuthFederatedKeystoneIDPMais fedAuthIDPMais = new AuthFederatedKeystoneIDPMais();
 		
 		
 		
 		fedAuthIDPMais.authFederatedKeystone("http://cana.ect.ufrn.br:5000/v2.0");
 		
 	}
 	/**
 	 * Makes the federated authentication
 	 * @param spEndpoint
 	 * @return
 	 */
 	public String authFederatedKeystone(String spEndpoint){
 		
 		
 		try {
 			
 			IDPMaisClient idpMais = new IDPMaisClient();
 			
 			String idpResponse = idpMais.getIDPMaisSAMLResponse("funcionario", "funcionario123", "http://idpstcfed.sj.ifsc.edu.br/RNPSecurityTokenService/RNPSTS", "http://cana.ect.ufrn.br:5000/v2.0/tokens");
 
 			sendSAMlrespToKeystone(idpResponse, "IdP-stcfed-IFSC", spEndpoint, httpClient);
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
 	
 	
 	
 	public void sendSAMlrespToKeystone(String idpResponse, String idpName, String spEndpoint, DefaultHttpClient userSpecificHttpClient) throws JSONException, ClientProtocolException, IOException{
 		
 		System.out.println("\n\n <<<<SPENDPOINT SENDING SAMLResponse>> "+ spEndpoint +"\n\n");
 		HttpPost httppost = new HttpPost(spEndpoint);
 		
 		try {			
             
 			
 			
             StringEntity entity = new StringEntity("{\"realm\":{\"name\":\""+idpName+"\"},\"idpResponse\":\"SAMLResponse=" + URLEncoder.encode(idpResponse, "UTF-8")+"\"}");
 
             System.out.println("JSON TO SEND:   <<<<<INI>>>>> \n" + OurUtil.httpEntityToString(entity)+ "\n<<<<<FIM>>>>>");
             entity.setContentType("application/json");
             httppost.setEntity(entity);
             httppost.addHeader("Content-type","application/json");
             httppost.addHeader("X-Authentication-Type","federated");
             
             
             //teste decodificando saml do thomas
 //            samlDecoded=new String(decoder.decode(idpResponse), "UTF-8");
 //			System.out.println("\n\n<INI>>>Saml decoded: \n\n"+samlDecoded+ "\n <FIM DECODED>>>>>>");
           
 			//vai tratar a resposta da requisio 
             HttpResponse requestResp= userSpecificHttpClient.execute(httppost);
             System.out.println("Http post sendSAMlrespToKeystone executed ");
             
             //transforma resposta em uma string contendo o json da resposta
             String responseAsString = OurUtil.httpEntityToString(requestResp.getEntity());
             System.out.println("\n\nKeystone Unscoped TOKEN:\n"+responseAsString);
             
             JSONObject jsonResp = new JSONObject(responseAsString);
             
             String unscopedToken = jsonResp.get("unscopedToken").toString();
             System.out.println("\n\nKeystone Unscoped TOKEN:\n"+unscopedToken);
             
             httppost = new HttpPost(spEndpoint + "/tokens");
             entity.setContentType("application/json");
             String tenantID = "2f0c5051363442859fa2e464cc6227c3";
             entity = new StringEntity(
             "{\"auth\" : " +
             "	{\"token\" : " +
             "		{\"id\" : \"" + unscopedToken + "\"}, " +
             		"\"tenantId\" : \"" + tenantID + "\"" +
             	"}" +
             "}"
             );
             httppost.setEntity(entity);
             httppost.addHeader("Content-type","application/json");
             
             requestResp= userSpecificHttpClient.execute(httppost);
             responseAsString = OurUtil.httpEntityToString(requestResp.getEntity());
             System.out.println("\n\nKeystone Scoped TOKEN:\n"+responseAsString);
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
