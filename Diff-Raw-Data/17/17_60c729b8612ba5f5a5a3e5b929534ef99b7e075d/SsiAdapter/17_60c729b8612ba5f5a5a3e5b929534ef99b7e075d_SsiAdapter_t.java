 package com.pk.cwierkacz.ws;
 
 import java.rmi.RemoteException;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Date;
 import java.util.List;
 
 import javax.activation.DataHandler;
 
 import org.apache.axiom.attachments.ByteArrayDataSource;
 import org.apache.axis2.AxisFault;
 
 import pl.edu.pk.ias.socialserviceintegration.IncorrectPasswordFault;
 import pl.edu.pk.ias.socialserviceintegration.TokenExpiredFault;
 import pl.edu.pk.ias.socialserviceintegration.UserNotExistFault;
 import pl.edu.pk.ias.socialserviceintegrationClient.SocialServiceIntegrationStub;
 import pl.edu.pk.ias.socialserviceintegrationClient.SocialServiceIntegrationStub.AccountsRequest;
 import pl.edu.pk.ias.socialserviceintegrationClient.SocialServiceIntegrationStub.AccountsRequestE;
 import pl.edu.pk.ias.socialserviceintegrationClient.SocialServiceIntegrationStub.AccountsResponseE;
 import pl.edu.pk.ias.socialserviceintegrationClient.SocialServiceIntegrationStub.File;
 import pl.edu.pk.ias.socialserviceintegrationClient.SocialServiceIntegrationStub.Filename;
 import pl.edu.pk.ias.socialserviceintegrationClient.SocialServiceIntegrationStub.GetItemsPreviewsRequest;
 import pl.edu.pk.ias.socialserviceintegrationClient.SocialServiceIntegrationStub.GetItemsPreviewsRequestE;
 import pl.edu.pk.ias.socialserviceintegrationClient.SocialServiceIntegrationStub.GetItemsPreviewsResponseE;
 import pl.edu.pk.ias.socialserviceintegrationClient.SocialServiceIntegrationStub.GetItemsRequest;
 import pl.edu.pk.ias.socialserviceintegrationClient.SocialServiceIntegrationStub.GetItemsRequestE;
 import pl.edu.pk.ias.socialserviceintegrationClient.SocialServiceIntegrationStub.GetItemsResponseE;
 import pl.edu.pk.ias.socialserviceintegrationClient.SocialServiceIntegrationStub.Item;
 import pl.edu.pk.ias.socialserviceintegrationClient.SocialServiceIntegrationStub.ItemId;
 import pl.edu.pk.ias.socialserviceintegrationClient.SocialServiceIntegrationStub.ItemPreview;
 import pl.edu.pk.ias.socialserviceintegrationClient.SocialServiceIntegrationStub.Login;
 import pl.edu.pk.ias.socialserviceintegrationClient.SocialServiceIntegrationStub.LoginRequest;
 import pl.edu.pk.ias.socialserviceintegrationClient.SocialServiceIntegrationStub.LoginRequestE;
 import pl.edu.pk.ias.socialserviceintegrationClient.SocialServiceIntegrationStub.LoginResponseE;
 import pl.edu.pk.ias.socialserviceintegrationClient.SocialServiceIntegrationStub.LogoutRequest;
 import pl.edu.pk.ias.socialserviceintegrationClient.SocialServiceIntegrationStub.LogoutRequestE;
 import pl.edu.pk.ias.socialserviceintegrationClient.SocialServiceIntegrationStub.LogoutResponseE;
 import pl.edu.pk.ias.socialserviceintegrationClient.SocialServiceIntegrationStub.Lss;
 import pl.edu.pk.ias.socialserviceintegrationClient.SocialServiceIntegrationStub.Message;
 import pl.edu.pk.ias.socialserviceintegrationClient.SocialServiceIntegrationStub.Password;
 import pl.edu.pk.ias.socialserviceintegrationClient.SocialServiceIntegrationStub.PublishRequest;
 import pl.edu.pk.ias.socialserviceintegrationClient.SocialServiceIntegrationStub.PublishRequestE;
 import pl.edu.pk.ias.socialserviceintegrationClient.SocialServiceIntegrationStub.PublishResponseE;
 import pl.edu.pk.ias.socialserviceintegrationClient.SocialServiceIntegrationStub.Token;
 
 import com.pk.cwierkacz.http.response.dto.Account;
 import com.pk.cwierkacz.model.AccountType;
 
 public class SsiAdapter
 {
     private SocialServiceIntegrationStub facebookService;
     private SocialServiceIntegrationStub flickrService;
     private SocialServiceIntegrationStub twitpicService;
 
     private static class InstanceHolder
     {
         public static SsiAdapter ssiAdapter = new SsiAdapter();
     }
 
     public static SsiAdapter getInstance( ) {
         return InstanceHolder.ssiAdapter;
     }
 
     private SsiAdapter() {
 
         try {
             facebookService = new SocialServiceIntegrationStub("http://mars.iti.pk.edu.pl/~lmk/SocialServiceIntegration.wsdl");
         }
         catch ( AxisFault e1 ) {
             e1.printStackTrace();
         }
 
         try {
             flickrService = new SocialServiceIntegrationStub("http://37.28.156.233:8080/FilckrIAS/services/integration/socialserviceintegration?wsdl");
         }
         catch ( AxisFault e1 ) {
             e1.printStackTrace();
         }
         try {
             twitpicService = new SocialServiceIntegrationStub("http://twitpic.piotrkubowicz.pl/soap?wsdl");
         }
         catch ( AxisFault e ) {
             e.printStackTrace();
         }
     }
 
     public Result login( String login, String password, AccountType accountType ) {
         LoginRequest loginRequest = new LoginRequest();
         Login logintype = new Login();
         logintype.setLogin(login);
         loginRequest.setLogin(logintype);
 
         Password passwordType = new Password();
         passwordType.setPassword(password);
 
         loginRequest.setPassword(passwordType);
 
         LoginRequestE loginRequestE = new LoginRequestE();
         loginRequestE.setLoginRequest(loginRequest);
 
         Result result = null;
         if ( AccountType.FACEBOOKBRIDGE.equals(accountType) ) {
             result = loginToPort(loginRequestE, facebookService);
         }
         else if ( AccountType.FLICKERBRIDGE.equals(accountType) ) {
             result = loginToPort(loginRequestE, flickrService);
         }
         else if ( AccountType.TWITPICBRIDGE.equals(accountType) ) {
             result = loginToPort(loginRequestE, twitpicService);
         }
         return result;
     }
 
     private Result loginToPort( LoginRequestE loginRequest, SocialServiceIntegrationStub port ) {
         Result result = null;
         LoginResponseE loginResponseE = null;
         try {
             loginResponseE = port.login(loginRequest);
         }
         catch ( RemoteException | UserNotExistFault | IncorrectPasswordFault e ) {
             e.printStackTrace();
            result = new Result(false, "Bład logowania.");
            return result;
         }
 
         result = new Result(true, "Zalogowano pomyślnie.");
         result.setToken(loginResponseE.getLoginResponse().getToken().getToken());
         return result;
     }
 
     public boolean logout( String token, AccountType accountType ) {
 
         LogoutRequestE logoutRequestE = new LogoutRequestE();
         LogoutRequest logoutRequest = new LogoutRequest();
         Token tokenType = new Token();
         tokenType.setToken(token);
         logoutRequest.setToken(tokenType);
 
         logoutRequestE.setLogoutRequest(logoutRequest);
 
         boolean result = false;
         if ( AccountType.FACEBOOKBRIDGE.equals(accountType) ) {
             result = logoutToPort(logoutRequestE, facebookService);
         }
         else if ( AccountType.FLICKERBRIDGE.equals(accountType) ) {
             result = logoutToPort(logoutRequestE, flickrService);
         }
         else if ( AccountType.TWITPICBRIDGE.equals(accountType) ) {
             result = logoutToPort(logoutRequestE, twitpicService);
         }
 
         //TODO remove token from user
         return result;
     }
 
     private boolean logoutToPort( LogoutRequestE logoutRequestE, SocialServiceIntegrationStub port ) {
 
         LogoutResponseE logoutResponse = null;
         try {
             logoutResponse = port.logout(logoutRequestE);
         }
         catch ( RemoteException e ) {
             e.printStackTrace();
         }
         return logoutResponse.getLogoutResponse().getIsOperationSuccess().getIsOperationSuccess();
     }
 
     public List<Account> getAccounts( String token, AccountType accountType ) {
 
         AccountsRequestE accountsRequestE = new AccountsRequestE();
         AccountsRequest accountsRequest = new AccountsRequest();
         Token tokenType = new Token();
         tokenType.setToken(token);
         accountsRequest.setToken(tokenType);
 
         accountsRequestE.setAccountsRequest(accountsRequest);
 
         List<Account> accounts = new ArrayList<>();
         if ( AccountType.FACEBOOKBRIDGE.getType().equals(accountType.getType()) ) {
             accounts.addAll(getAccountsToPort(accountsRequestE, facebookService, AccountType.FACEBOOKBRIDGE));
         }
         if ( AccountType.FLICKERBRIDGE.getType().equals(accountType.getType()) ) {
             accounts.addAll(getAccountsToPort(accountsRequestE, flickrService, AccountType.FLICKERBRIDGE));
         }
         if ( AccountType.TWITPICBRIDGE.getType().equals(accountType.getType()) ) {
             accounts.addAll(getAccountsToPort(accountsRequestE, twitpicService, AccountType.TWITPICBRIDGE));
         }
 
         return accounts;
     }
 
     private List<Account> getAccountsToPort( AccountsRequestE accountsRequestE,
                                              SocialServiceIntegrationStub wsPort,
                                              AccountType accountType ) {
         List<Account> accounts = new ArrayList<>();
 
         AccountsResponseE response = null;
         try {
             response = wsPort.accounts(accountsRequestE);
         }
         catch ( RemoteException | TokenExpiredFault e ) {
             e.printStackTrace();
         }
 
         for ( Lss name : response.getAccountsResponse().getLss() ) {
             accounts.add(new Account(name.getLss(), name.getLss(), accountType));
         }
         return accounts;
     }
 
     public ItemId publishTweet( AccountType accountType, String description, String account ) throws BridgeException {
         return publishTweet(accountType, description, account, null, null);
     }
 
     public ItemId publishTweet( AccountType accountType,
                                 String description,
                                 String account,
                                 String fileName,
                                 byte[] file ) throws BridgeException {
 
         //TODO: get token by accounType
         PublishRequestE publishRequestE = new PublishRequestE();
         PublishRequest publishRequest = new PublishRequest();
         Message message = new Message();
         message.setMessage(description);
         publishRequest.setMessage(message);
         File fileType = new File();
         fileType.setFile(new DataHandler(new ByteArrayDataSource(file)));
         publishRequest.setFile(fileType);
         Filename filenameType = new Filename();
         filenameType.setFilename(fileName);
         publishRequest.setFilename(filenameType);
         Lss lss = new Lss();
         lss.setLss(account);
         publishRequest.setLss(lss);
         publishRequest.setToken(null);
         publishRequestE.setPublishRequest(publishRequest);
         ItemId itemId = null;
         if ( AccountType.FACEBOOKBRIDGE.equals(accountType) ) {
             itemId = publishTweetToPort(publishRequestE, facebookService);
         }
         else if ( AccountType.FLICKERBRIDGE.equals(accountType) ) {
             itemId = publishTweetToPort(publishRequestE, flickrService);
         }
         else if ( AccountType.TWITPICBRIDGE.equals(accountType) ) {
             itemId = publishTweetToPort(publishRequestE, twitpicService);
         }
         return itemId;
 
     }
 
     public ItemId publishTweetToPort( PublishRequestE publishRequestE, SocialServiceIntegrationStub wsPort ) {
         PublishResponseE publishResponseE = null;
         try {
             publishResponseE = wsPort.publish(publishRequestE);
         }
         catch ( RemoteException | TokenExpiredFault e ) {
             e.printStackTrace();
         }
 
         return publishResponseE.getPublishResponse().getId();
     }
 
     public List<Item> fetchIteams( Date dateFrom, Date dateTo, String account ) throws BridgeException {
         List<Item> iteams = new ArrayList<>();
         iteams.addAll(fetchIteams(AccountType.FACEBOOKBRIDGE, dateFrom, dateTo, account));
         iteams.addAll(fetchIteams(AccountType.FLICKERBRIDGE, dateFrom, dateTo, account));
         iteams.addAll(fetchIteams(AccountType.TWITPICBRIDGE, dateFrom, dateTo, account));
         return iteams;
     }
 
     public List<Item> fetchIteams( AccountType accountType, Date dateFrom, Date dateTo, String account ) throws BridgeException {
         GetItemsPreviewsRequestE getItemsPreviewsRequestE = new GetItemsPreviewsRequestE();
         GetItemsPreviewsRequest getItemsPreviewsRequest = new GetItemsPreviewsRequest();
         getItemsPreviewsRequestE.setGetItemsPreviewsRequest(getItemsPreviewsRequest);
         //        getItemsPreviewsRequest.setDateFrom(dateFrom);
         //        getItemsPreviewsRequest.setDateTo(dateTo);
         //getItemsPreviewsRequest.setLss(account);
         getItemsPreviewsRequest.setToken(null);
         //TODO: token
         List<ItemPreview> itemsPrevs = Arrays.asList(getPrieviews(accountType, getItemsPreviewsRequestE));
 
         GetItemsRequestE getItemsRequestE = new GetItemsRequestE();
         GetItemsRequest getItemsRequest = new GetItemsRequest();
         getItemsPreviewsRequestE.setGetItemsPreviewsRequest(getItemsPreviewsRequest);
         getItemsRequest.setToken(null);
         //TODO: token
         List<ItemId> ids = new ArrayList<>();
         for ( ItemPreview itemPreview : itemsPrevs ) {
             ids.add(itemPreview.getId());
         }
         getItemsRequest.setIdsList(ids.toArray(new ItemId[0]));
 
         return Arrays.asList(getIteams(accountType, getItemsRequestE));
     }
 
     private ItemPreview[] getPrieviews( AccountType accountType,
                                         GetItemsPreviewsRequestE getItemsPreviewsRequestE ) throws BridgeException {
         ItemPreview[] itemPreviews = null;
         if ( AccountType.FACEBOOKBRIDGE.equals(accountType) ) {
             getPrieviewsToPort(getItemsPreviewsRequestE, facebookService);
         }
         else if ( AccountType.FLICKERBRIDGE.equals(accountType) ) {
             getPrieviewsToPort(getItemsPreviewsRequestE, flickrService);
         }
         else if ( AccountType.TWITPICBRIDGE.equals(accountType) ) {
             getPrieviewsToPort(getItemsPreviewsRequestE, twitpicService);
         }
         return itemPreviews;
     }
 
     private ItemPreview[] getPrieviewsToPort( GetItemsPreviewsRequestE getItemsPreviewsRequestE,
                                               SocialServiceIntegrationStub wsPort ) {
 
         GetItemsPreviewsResponseE getItemsPreviewsResponseE = null;
         try {
             getItemsPreviewsResponseE = wsPort.getItemsPreviews(getItemsPreviewsRequestE);
         }
         catch ( RemoteException | TokenExpiredFault e ) {
             // TODO Auto-generated catch block
             e.printStackTrace();
         }
 
         return getItemsPreviewsResponseE.getGetItemsPreviewsResponse().getItemPreviewsList();
 
     }
 
     public List<Item> getItems( AccountType accountType, List<String> lss, List<Long> ids ) throws BridgeException {
         GetItemsRequestE requestE = new GetItemsRequestE();
         GetItemsRequest request = new GetItemsRequest();
         requestE.setGetItemsRequest(request);
         List<ItemId> itemIds = new ArrayList<ItemId>();
         for ( int i = 0; i < ids.size(); i++ ) {
             ItemId itemId = new ItemId();
             itemId.setId(Long.toString(ids.get(i)));
             itemId.setLss(lss.get(i));
             itemIds.add(new ItemId());
         }
         request.setToken(null); //TODO
         request.setIdsList(itemIds.toArray(new ItemId[0]));
 
         return Arrays.asList(getIteams(accountType, requestE));
     }
 
     private Item[] getIteams( AccountType accountType, GetItemsRequestE getItemsRequestE ) throws BridgeException {
         Item[] items = null;
         if ( AccountType.FACEBOOKBRIDGE.equals(accountType) ) {
             items = getIteamsToPort(getItemsRequestE, facebookService);
         }
         else if ( AccountType.FLICKERBRIDGE.equals(accountType) ) {
             items = getIteamsToPort(getItemsRequestE, flickrService);
         }
         else if ( AccountType.TWITPICBRIDGE.equals(accountType) ) {
             items = getIteamsToPort(getItemsRequestE, twitpicService);
         }
         return items;
     }
 
     private Item[] getIteamsToPort( GetItemsRequestE getItemsRequestE, SocialServiceIntegrationStub wsPort ) {
         GetItemsResponseE getItemsResponseE = null;
         try {
             getItemsResponseE = wsPort.getItems(getItemsRequestE);
         }
         catch ( RemoteException | TokenExpiredFault e ) {
             // TODO Auto-generated catch block
             e.printStackTrace();
         }
         return getItemsResponseE.getGetItemsResponse().getItemsList();
 
     }
 
     public class Result
     {
         private final boolean correct;
 
         private final String msg;
 
         private String token;
 
         public Result( boolean result, String msg ) {
             this.msg = msg;
             this.correct = result;
         }
 
         public String getToken( ) {
             return token;
         }
 
         public void setToken( String token ) {
             this.token = token;
         }
 
         public boolean isCorrect( ) {
             return correct;
         }
 
         public String getMsg( ) {
             return msg;
         }
     }
 }
