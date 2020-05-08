 package com.pk.cwierkacz.processor.handlers;
 
 import java.io.BufferedInputStream;
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.pk.cwierkacz.http.Action;
 import com.pk.cwierkacz.http.Status;
 import com.pk.cwierkacz.http.request.AddTweeterAccountRequest;
 import com.pk.cwierkacz.http.response.Response;
 import com.pk.cwierkacz.http.response.ResponseImpl;
 import com.pk.cwierkacz.model.AccountType;
 import com.pk.cwierkacz.model.ApplicationData;
 import com.pk.cwierkacz.model.dao.BridgeAccountDao;
 import com.pk.cwierkacz.model.dao.SessionDao;
 import com.pk.cwierkacz.model.dao.TwitterAccountDao;
 import com.pk.cwierkacz.model.dao.UserDao;
 import com.pk.cwierkacz.model.service.BridgeAccountService;
 import com.pk.cwierkacz.model.service.ServiceRepo;
 import com.pk.cwierkacz.model.service.SessionService;
 import com.pk.cwierkacz.model.service.TwitterAccountService;
 import com.pk.cwierkacz.model.service.UserService;
 import com.pk.cwierkacz.processor.handlers.helpers.HttpClient;
 import com.pk.cwierkacz.twitter.OAuthAuthentication;
 import com.pk.cwierkacz.twitter.TwitterAuthenticationException;
 import com.pk.cwierkacz.ws.SsiAdapter.Result;
 
 public class WireTweetAccount extends AbstractHandler
 {
     private final Logger logger = LoggerFactory.getLogger(WireTweetAccount.class);
 
     private final UserService userService;
     private final SessionService sessionService;
     private final TwitterAccountService twitterAccountService;
     private final BridgeAccountService bridgeAccountService;
 
     private final HttpClient httpClient;
 
     public WireTweetAccount() {
         super();
         userService = ServiceRepo.getInstance().getService(UserService.class);
         sessionService = ServiceRepo.getInstance().getService(SessionService.class);
         twitterAccountService = ServiceRepo.getInstance().getService(TwitterAccountService.class);
         bridgeAccountService = ServiceRepo.getInstance().getService(BridgeAccountService.class);
         httpClient = new HttpClient();
     }
 
     @Override
     public boolean isHandleable( ApplicationData applicationData ) {
         return applicationData.getRequest().getAction().equals(Action.LINKSOCIALACCOUNT);
     }
 
     @Override
     public void handle( ApplicationData appData ) {
 
         AddTweeterAccountRequest accRequest = (AddTweeterAccountRequest) appData.getRequest();
         Response response;
         if ( accRequest.getAccountType().equals(AccountType.TWITTER) ) {
             response = handleTweetAccount(accRequest);
         }
         else {
             response = handleBridgeAccount(accRequest);
         }
         appData.setResponse(response);
     }
 
     private Response handleTweetAccount( AddTweeterAccountRequest accRequest ) {
 
         SessionDao sessionDao = sessionService.getByToken(accRequest.getTokenId());
         UserDao user = userService.getBySessionId(sessionDao);
         if ( user != null && user.getSession().getCurrentToken() != accRequest.getTokenId() ) {
             Response response = ResponseImpl.create(Status.DENY, "Zły token.", accRequest.getTokenId());
             return response;
         }
         List<String> accountsName = new ArrayList<>();
         TwitterAccountDao accountDaoLinked = null;
         if ( user.getAccounts() != null ) {
             for ( TwitterAccountDao accountDao : user.getAccounts() ) {
                 accountsName.add(accountDao.getAccountName());
                 if ( accountDao.getName().equals(accRequest.getLoginTweet()) ) {
                     accountDaoLinked = accountDao;
                 }
             }
         }
 
         if ( accountsName.contains(accRequest.getLoginTweet()) ) {
             if ( accountDaoLinked != null && accountDaoLinked.isDeleted() ) {
                 accountDaoLinked.setDeleted(false);
                 twitterAccountService.saveOrUpdate(accountDaoLinked);
             }
             Response response = ResponseImpl.create(Status.OK,
                                                     "Konto już zostało powiązne.",
                                                     accRequest.getTokenId());
             return response;
         }
 
         TwitterAccountDao accountDao = twitterAccountService.getAccountByName(accRequest.getLoginTweet());
 
         /**
          * if ( accountDao != null ) { accountDao.addUser(user);
          * accountDao.setDeleted(false);
          * twitterAccountService.saveOrUpdate(accountDao);
          * user.getAccounts().add(accountDao); userService.saveOrUpdate(user);
          * Response response = ResponseImpl.create(Status.OK,
          * "Konto powiązano pomyślnie.", accRequest.getTokenId()); return
          * response; }
          */
 
         if ( accountDao == null ) {
             accountDao = TwitterAccountDao.create(0, user, accRequest.getLoginTweet(), null, null, null);
             accountDao.setId(1l);
         }
 
         OAuthAuthentication userAuthentication = null;
         String pin = null;
         try {
             userAuthentication = new OAuthAuthentication(accountDao);
 
             String url = userAuthentication.getAuthenticationURL();
             System.out.println(url);
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
                 e.printStackTrace();
             }
 
         }
         catch ( TwitterAuthenticationException e ) {
             e.printStackTrace();
             Response response = ResponseImpl.create(Status.ERROR, e.getMessage(), accRequest.getTokenId());
             return response;
         }
 
         try {
             logger.info("PIN:" + pin);
             accountDao = userAuthentication.authenticate(pin, true);
         }
         catch ( TwitterAuthenticationException e ) {
             e.printStackTrace();
         }
 
         accountDao.getUser().add(user);
         if ( twitterAccountService.getAccountByExternalId(accountDao.getExternalId()) != null ) {
             twitterAccountService.saveOrUpdate(accountDao);
         }
         else {
             twitterAccountService.save(accountDao);
         }
 
         user.getAccounts().add(accountDao);
         userService.saveOrUpdate(user);
 
         Response response = ResponseImpl.create(Status.OK,
                                                 "Konto powiązano pomyślnie.",
                                                 accRequest.getTokenId());
         return response;
     }
 
     private String extractPin( BufferedReader reader ) throws IOException {
         String pin = null;
         String line;
         StringBuilder stringBuilder = new StringBuilder();
 
         while ( null != ( line = reader.readLine() ) ) {
             System.out.println(line);
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
 
     private Response handleBridgeAccount( AddTweeterAccountRequest accRequest ) {
 
         SessionDao sessionDao = sessionService.getByToken(accRequest.getTokenId());
         UserDao user = userService.getBySessionId(sessionDao);
 
         List<String> accountsName = new ArrayList<>();
         for ( BridgeAccountDao accountDao : user.getBridgeAccounts() ) {
             accountsName.add(accountDao.getName());
         }
 
         if ( accountsName.contains(accRequest.getLoginTweet()) ) {
             Response response = ResponseImpl.create(Status.OK,
                                                     "Konto już zostało powiązne.",
                                                     accRequest.getTokenId());
             return response;
         }
 
         Result result = ssiAdapter.login(accRequest.getLoginTweet(),
                                          accRequest.getPasswordTweet(),
                                          accRequest.getAccountType());
         Response response = null;
         if ( result.isCorrect() ) {
 
             response = ResponseImpl.create(Status.OK, result.getMsg(), accRequest.getTokenId());
 
             BridgeAccountDao accountDao = bridgeAccountService.getAccountByNameFalse(accRequest.getLoginTweet(),
                                                                                      accRequest.getAccountType());
             if ( accountDao == null ) {
                 accountDao = new BridgeAccountDao();
                 accountDao.setAccessToken(result.getToken());
                 accountDao.setAccountType(accRequest.getAccountType());
                 accountDao.setName(accRequest.getLoginTweet());
             }
             accountDao.addUser(user);
             if ( accountDao.getId() == null ) {
                 bridgeAccountService.save(accountDao);
             }
             else {
                 bridgeAccountService.saveOrUpdate(accountDao);
             }
 
             user.getBridgeAccounts().add(accountDao);
             userService.saveOrUpdate(user);
         }
         else {
             response = ResponseImpl.create(Status.ERROR, result.getMsg(), accRequest.getTokenId());
         }
         return response;
     }
 }
