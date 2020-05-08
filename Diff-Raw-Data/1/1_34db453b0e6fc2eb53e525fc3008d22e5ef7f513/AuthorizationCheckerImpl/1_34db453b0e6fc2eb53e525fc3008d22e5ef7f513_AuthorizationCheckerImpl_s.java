 package org.mitre.rhex.oauth2;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import org.springframework.http.HttpStatus;
 import org.springframework.http.ResponseEntity;
 import org.springframework.stereotype.Service;
 import org.springframework.web.client.support.RestGatewaySupport;
 
@Service
 public class AuthorizationCheckerImpl extends RestGatewaySupport implements AuthorizationChecker {
 
 	private String oAuth2ServerUrl;
 	
 	public boolean checkAuthorization(String clientId, String token) {
 		Map<String, String> params = new HashMap<String, String>();
 		params.put("client_id", clientId);
 		params.put("token", token);
 		ResponseEntity<Void> response = getRestTemplate().getForEntity("http://localhost:8080/rhex-oauth2-server/oauth/token?client_id={client_id}&token={token}", Void.class, params);
 		return response.getStatusCode() == HttpStatus.OK;
 	}
 	
 	public void setOAuth2ServerUrl(String url) {
 		this.oAuth2ServerUrl = url;
 	}
 	
 	public String getOAuth2ServerUrl() {
 		return this.oAuth2ServerUrl;
 	}
 	
 }
