 package de.perdoctus.synolib;
 
 import de.perdoctus.synolib.exceptions.CommunicationException;
 import de.perdoctus.synolib.exceptions.LoginException;
 import de.perdoctus.synolib.exceptions.SynoException;
 import de.perdoctus.synolib.requests.AddUrlRequest;
 import de.perdoctus.synolib.requests.GetAllRequest;
 import de.perdoctus.synolib.requests.LoginRequest;
 import de.perdoctus.synolib.responses.AddUrlResponse;
 import de.perdoctus.synolib.responses.GetAllResponse;
 import de.perdoctus.synolib.responses.LoginResponse;
 
 import java.net.URI;
 import java.net.URISyntaxException;
 
 /**
  * Created by IntelliJ IDEA.
  * User: cgiesche
  * Date: 26.02.12
  * Time: 21:26
  * To change this template use File | Settings | File Templates.
  */
 public class DownloadRedirectorClient {
     
     private String username = "";
     private String password = "";
     
    RequestExecutor executor;
     private String  sessionId;
     
     public DownloadRedirectorClient() {
         
     }
     
     public DownloadRedirectorClient(String username, String password, URI synoUri) {
         this.password = password;
         this.username = username;
         
         this.executor = new RequestExecutor(synoUri);
     }
     
     public AddUrlResponse addDownloadUrl(URI uri) throws SynoException {
         if (sessionId == null) {
             login();
         }
 
         AddUrlRequest request = new AddUrlRequest(uri, sessionId);
         AddUrlResponse response = executor.executeRequest(request, AddUrlResponse.class);
 
         return response;
     }
 
     public GetAllResponse getAllDownloads() throws SynoException {
         if (sessionId == null) {
             login();
         }
 
         GetAllRequest request = new GetAllRequest(sessionId);
         GetAllResponse response = executor.executeRequest(request, GetAllResponse.class);
 
         return response;
     }
 
     public void forceReloginOnNextRequest() {
         this.sessionId = null;
     }
 
     public String getPassword() {
         return password;
     }
 
     public void setPassword(String password) {
         this.password = password;
     }
 
     public String getUsername() {
         return username;
     }
 
     public void setUsername(String username) {
         this.username = username;
     }
     
     public void setSynoUri(URI uri) throws URISyntaxException {
         this.executor = new RequestExecutor(uri);
     }
 
     private void login() throws SynoException {
         LoginRequest request = new LoginRequest(username, password);
         LoginResponse response = executor.executeRequest(request, LoginResponse.class);
 
         if (response.isSuccess()) {
             if (response.isLoginSuccess()) {
                   this.sessionId = response.getId();
             } else {
                 throw new LoginException("Login failed.");
             }
         } else {
             throw new CommunicationException("LoginRequest failed.");
         }
     }
 }
