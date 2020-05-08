 package com.pk.cwierkacz.processor.handlers;
 
 import java.io.BufferedInputStream;
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import com.pk.cwierkacz.http.Action;
 import com.pk.cwierkacz.http.Status;
 import com.pk.cwierkacz.http.request.AddTweeterAccountRequest;
 import com.pk.cwierkacz.http.response.Response;
 import com.pk.cwierkacz.http.response.ResponseImpl;
 import com.pk.cwierkacz.model.ApplicationData;
import com.pk.cwierkacz.model.dao.SessionDao;
 import com.pk.cwierkacz.model.dao.TwitterAccountDao;
 import com.pk.cwierkacz.model.dao.UserDao;
 import com.pk.cwierkacz.model.service.ServiceRepo;
import com.pk.cwierkacz.model.service.SessionService;
 import com.pk.cwierkacz.model.service.UserService;
 import com.pk.cwierkacz.processor.handlers.halpers.HttpClient;
 import com.pk.cwierkacz.twitter.OAuthAuthentication;
 import com.pk.cwierkacz.twitter.TwitterAuthenticationException;
 
 public class WireTweetAccount implements Handler
 {
 
     private final UserService userService;
    private final SessionService sessionService;
 
     private final HttpClient httpClient;
 
     public WireTweetAccount() {
         userService = ServiceRepo.getInstance().getService(UserService.class);
        sessionService = ServiceRepo.getInstance().getService(SessionService.class);
         httpClient = new HttpClient();
     }
 
     @Override
     public boolean isHandleable( ApplicationData applicationData ) {
         return applicationData.getRequest().getAction().equals(Action.ADDTWEETACCOUNT);
     }
 
     Map<Long, OAuthAuthentication> map = new HashMap<>();
 
     @Override
     public void handle( ApplicationData appData ) {
         AddTweeterAccountRequest accRequest = (AddTweeterAccountRequest) appData.getRequest();
 
        SessionDao sessionDao = sessionService.getByToken(accRequest.getTokenId());
        UserDao user = userService.getBySessionId(sessionDao);
         if ( user != null && user.getSession().getCurrentToken() != accRequest.getTokenId() ) {
             Response response = ResponseImpl.create(Status.DENY, "Invalid Token", accRequest.getTokenId());
             appData.setResponse(response);
             return;
         }
         List<String> accountsName = new ArrayList<>();
         for ( TwitterAccountDao accountDao : user.getAccounts() ) {
             accountsName.add(accountDao.getAccountName());
         }
 
         if ( accountsName.contains(accRequest.getLoginTweet()) ) {
             Response response = ResponseImpl.create(Status.DENY,
                                                     "User already has this accout",
                                                     accRequest.getTokenId());
             appData.setResponse(response);
             return;
         }
 
        TwitterAccountDao account = TwitterAccountDao.create(0, user, accRequest.getLoginTweet(), null, null);
         account.setId(1l);
         OAuthAuthentication userAuthentication = null;
         if ( map.containsKey(accRequest.getTokenId()) ) {
             userAuthentication = map.get(accRequest.getTokenId());
         }
         else {
             userAuthentication = new OAuthAuthentication(account);
             map.put(accRequest.getTokenId(), userAuthentication);
         }
         String pin = null;
         try {
             String url = userAuthentication.getAuthenticationURL();
 
             String authUrl = "https://api.twitter.com/oauth/authorize";
 
             BufferedReader reader = null;
             try {
                 reader = new BufferedReader(new InputStreamReader(new BufferedInputStream(httpClient.getUrlConnection(url,
                                                                                                                       false,
                                                                                                                       null)
                                                                                                     .getInputStream()),
                                                                   HttpClient.CHARSET_UTF8));
 
                 String token = extractToken(reader);
                 String outhToken = url.split("=")[ 1 ];
                 String postData = "authenticity_token=" +
                                   token +
                                   "&oauth_token=" +
                                   outhToken +
                                   "&session%5Busername_or_email%5D=" +
                                   accRequest.getLoginTweet() +
                                   "&session%5Bpassword%5D=" +
                                   accRequest.getPasswordTweet();
 
                 reader = new BufferedReader(new InputStreamReader(new BufferedInputStream(httpClient.getUrlConnection(authUrl,
                                                                                                                       true,
                                                                                                                       postData)
                                                                                                     .getInputStream()),
                                                                   HttpClient.CHARSET_UTF8));
                 pin = extractPin(reader);
                 reader.close();
             }
             catch ( IOException e ) {
                 // TODO Auto-generated catch block
                 e.printStackTrace();
             }
 
         }
         catch ( TwitterAuthenticationException e ) {
             Response response = ResponseImpl.create(Status.ERROR, e.getMessage(), accRequest.getTokenId());
             appData.setResponse(response);
             return;
         }
 
         try {
             account = userAuthentication.authenticate(pin, true);
         }
         catch ( TwitterAuthenticationException e ) {
             // TODO Auto-generated catch block
             e.printStackTrace();
         }
 
         user.getAccounts().add(account);
         userService.saveOrUpdate(user);
 
         Response response = ResponseImpl.create(Status.OK,
                                                 "Account succesfully wired",
                                                 accRequest.getTokenId());
         appData.setResponse(response);
         return;
     }
 
     private String extractPin( BufferedReader reader ) throws IOException {
         String pin = null;
         String line;
         StringBuilder stringBuilder = new StringBuilder();
 
         while ( null != ( line = reader.readLine() ) ) {
             stringBuilder.append(line);
             if ( line.contains("<code>") ) {
                 pin = line.split("[><]")[ 4 ];
             }
         }
         return pin;
     }
 
     private String extractToken( BufferedReader reader ) throws IOException {
         StringBuilder res = new StringBuilder();
         String line = "";
         while ( ( line = reader.readLine() ) != null ) {
             res.append(line);
             if ( line.contains("authenticity_token") ) {
                 String[] splited = line.split("\\s");
                 for ( String part : splited ) {
                     if ( part.contains("value") ) {
                         return part.split("\"")[ 1 ];
 
                     }
                 }
                 break;
             }
         }
         return null;
     }
 
 }
