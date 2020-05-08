 package com.versionone;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 
 import org.apache.amber.oauth2.client.OAuthClient;
 import org.apache.amber.oauth2.client.URLConnectionClient;
 import org.apache.amber.oauth2.client.request.OAuthClientRequest;
import org.apache.amber.oauth2.client.response.OAuthClientResponse;
import org.apache.amber.oauth2.client.response.OAuthResourceResponse;
 import org.apache.amber.oauth2.client.response.OAuthJSONAccessTokenResponse;
 import org.apache.amber.oauth2.common.exception.OAuthProblemException;
 import org.apache.amber.oauth2.common.exception.OAuthSystemException;
 import org.apache.amber.oauth2.common.message.types.GrantType;
 
 /**
  * Hello world!
  *
  */
 public class App 
 {
     public static void main( String[] args )
     {
     	OAuthClientRequest request;
     	OAuthJSONAccessTokenResponse response;
     	
         System.out.println("\n[STEP] Request Authorization");
         try {
 			request = OAuthClientRequest
 			        .authorizationLocation("https://www14.v1host.com/v1sdktesting/oauth.mvc/auth")
 			        .setClientId("client_fnd4wbhg")
 			        .setRedirectURI("urn:ietf:wg:oauth:2.0:oob")
 			        .setResponseType("code")
 			        .setScope("apiv1")
 			        .buildQueryMessage();
 		} catch (OAuthSystemException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 			return;
 		}
         System.out.println("Navigate to:");
         System.out.println(request.getLocationUri());
         try {
 			java.awt.Desktop.getDesktop().browse(java.net.URI.create(request.getLocationUri()));
 		} catch (IOException e2) {
 			// TODO Auto-generated catch block
 			e2.printStackTrace();
 		}
         
         System.out.println("\n[STEP] Get Authorization Code");
         System.out.println("Paste authorization code:");
         BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
         String code = null;
         try {
 			code = br.readLine();
 		} catch (IOException e1) {
 			// TODO Auto-generated catch block
 			e1.printStackTrace();
 			return;
 		}
         
         System.out.println("\n[STEP] Request Access Token");
         try {
 			request = OAuthClientRequest
 			        .tokenLocation("https://www14.v1host.com/v1sdktesting/oauth.mvc/token")
 			        .setGrantType(GrantType.AUTHORIZATION_CODE)
 			        .setClientId("client_fnd4wbhg")
 			        .setClientSecret("8xmxgjnzzgtpfwgvmnea")
 			        .setRedirectURI("urn:ietf:wg:oauth:2.0:oob")
 			        .setCode(code)
 			        .buildBodyMessage();
 		} catch (OAuthSystemException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 			return;
 		}
         
        OAuthClient client;
        client = new OAuthClient(new URLConnectionClient());
         try {
			response = client.accessToken(request);
 		} catch (OAuthSystemException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 			return;
 		} catch (OAuthProblemException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 			return;
 		}
         System.out.println("Access Token: " + response.getOAuthToken());
         System.out.println("Expires In: " + response.getExpiresIn());
 
         System.out.println("\n[STEP] Request Data");
         
 
     }
 }
