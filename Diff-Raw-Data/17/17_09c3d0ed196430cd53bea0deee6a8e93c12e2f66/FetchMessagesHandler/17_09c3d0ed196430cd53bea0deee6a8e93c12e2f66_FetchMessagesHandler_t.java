 package com.pk.cwierkacz.processor.handlers;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.apache.commons.lang3.StringUtils;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.pk.cwierkacz.http.Action;
 import com.pk.cwierkacz.http.Status;
 import com.pk.cwierkacz.http.request.FetchMessagesRequest;
 import com.pk.cwierkacz.http.response.Response;
 import com.pk.cwierkacz.http.response.ResponseImpl;
 import com.pk.cwierkacz.http.response.dto.Account;
 import com.pk.cwierkacz.http.response.dto.Message;
 import com.pk.cwierkacz.model.ApplicationData;
 import com.pk.cwierkacz.model.dao.TweetDao;
 import com.pk.cwierkacz.model.dao.TwitterAccountDao;
 import com.pk.cwierkacz.model.service.ServiceRepo;
 import com.pk.cwierkacz.model.service.TweetService;
 import com.pk.cwierkacz.model.service.TwitterAccountService;
 import com.pk.cwierkacz.processor.handlers.helpers.AccountManager;
import com.pk.cwierkacz.processor.handlers.helpers.AttachmentsWithResources;
 import com.pk.cwierkacz.processor.handlers.helpers.FetchResult;
 import com.pk.cwierkacz.processor.handlers.helpers.FileSaver;
 import com.pk.cwierkacz.twitter.TweetsResult;
 import com.pk.cwierkacz.twitter.TwitterAccount;
 import com.pk.cwierkacz.twitter.TwitterAccountMap;
 import com.pk.cwierkacz.twitter.TwitterActionException;
 import com.pk.cwierkacz.twitter.TwitterAuthenticationException;
 
 public class FetchMessagesHandler extends AbstractHandler
 {
     private static final Logger LOGGER = LoggerFactory.getLogger(FetchMessagesHandler.class);
 
     private final TweetService tweetService;
 
     private final TwitterAccountService accountService;
 
     private final FileSaver fileSaver;
 
     private TweetDao tweetWithImg( TweetDao t ) throws IOException {
        AttachmentsWithResources awr = fileSaver.saveFileFromUrl(t.getTwitterImageUrl());
        if ( awr != null )
            t.setImagePath(awr.getImgPath());
         return t;
     }
 
     public FetchMessagesHandler() {
         this.tweetService = ServiceRepo.getInstance().getService(TweetService.class);
         this.accountService = ServiceRepo.getInstance().getService(TwitterAccountService.class);
         this.fileSaver = new FileSaver();
     }
 
     @Override
     public boolean isHandleable( ApplicationData applicationData ) {
         return applicationData.getRequest().getAction().equals(Action.FETCHMESSAGES);
     }
 
     @Override
     public void handle( ApplicationData appData ) {
 
         FetchMessagesRequest fetchRequest = (FetchMessagesRequest) appData.getRequest();
 
         FetchResult fetchResult = new FetchResult();
 
         try {
 
             AccountManager accountManager = new AccountManager(fetchRequest.getAccounts(),
                                                                fetchRequest.getTokenId());
 
             List<String> twitterLogins = accountManager.getTwitterAccountLogins();
             List<Account> bridgeAccounts = accountManager.getBridgeAccounts();
 
             fetchResult.add(handleToTwitter(appData, twitterLogins));
             fetchResult.add(handleToBridges(appData, bridgeAccounts));
             fetchResult.sort();
 
         }
         catch ( Throwable e ) {
             LOGGER.error(getError(e));
             fetchResult.appendBuilder(appendError("Wewnętrzny bład aplikacji.", e));
         }
 
         Response response;
 
         String errors = fetchResult.getErrorBuilder().toString();
         List<Message> limitedMsgs = fetchResult.getMessages(fetchRequest.getSize());
         if ( StringUtils.isEmpty(errors) ) {
             response = ResponseImpl.create(Status.OK,
                                            "Pobrano pomyślnie wszystkie tweety.",
                                            appData.getRequest().getTokenId()).buildFetchResponse(limitedMsgs);
             ;
         }
         else {
             response = ResponseImpl.create(Status.ERROR,
                                            "Bład pobierania tweetów dla: " + errors,
                                            appData.getRequest().getTokenId()).buildFetchResponse(limitedMsgs);
         }
 
         appData.setResponse(response);
 
     }
 
     public FetchResult handleToTwitter( ApplicationData appData, List<String> twitterAccountLogins ) {
 
         FetchMessagesRequest fetchRequest = (FetchMessagesRequest) appData.getRequest();
 
         StringBuilder errorBuilder = new StringBuilder();
         TweetsResult notReadyTweets = new TweetsResult();
         List<TwitterAccountDao> acs = new ArrayList<TwitterAccountDao>();
         List<TweetDao> mergedTweets = new ArrayList<TweetDao>();
 
         for ( String accountLogin : twitterAccountLogins ) {
             try {
                 TwitterAccountDao accountDao = accountService.getAccountByName(accountLogin);
                 if ( accountDao == null )
                     errorBuilder.append("Konto " + accountLogin + " nie istnieje. ");
                 else {
                     TweetDao last = tweetService.getLastActualTweetForAccount(accountDao);
                     if ( last != null ) {
                         try {
                             TwitterAccount account = TwitterAccountMap.getTwitterAccount(accountDao);
                             if ( fetchRequest.getDateTo() == null ||
                                  fetchRequest.getDateTo().isAfter(last.getCratedDate()) ) {
                                 TweetsResult result = account.getTweetsFromMentionsAndUserTimeline(last);
                                 for ( TweetDao tweet : result.getReadyTweets() ) {
                                     tweetService.save(tweetWithImg(tweet));
                                 }
                                 notReadyTweets = notReadyTweets.add(result);
                             }
                         }
                         catch ( TwitterAuthenticationException e ) {
                             LOGGER.error(getError(e));
                             errorBuilder = appendError(errorBuilder, "Bład autoryzacji dla konta: " +
                                                                      accountLogin +
                                                                      ".", e);
                         }
                         catch ( TwitterActionException e ) {
                             LOGGER.error(getError(e));
                             errorBuilder = appendError(errorBuilder, "Bład pobierania tweetów dla konta: " +
                                                                      accountLogin +
                                                                      ".", e);
                         }
                     }
                 }
                 acs.add(accountDao);
             }
             catch ( Throwable e ) {
                 LOGGER.error(getError(e));
                 errorBuilder = appendError(errorBuilder, "Wewnętrzny bład aplikacji dla konta: " +
                                                          accountLogin +
                                                          ".", e);
 
             }
         }
         try {
             for ( TweetDao tweet : notReadyTweets.fulfilledNoReady() ) {
                 tweetService.save(tweetWithImg(tweet));
             }
         }
         catch ( Exception e ) {
             LOGGER.error(getError(e));
             errorBuilder = appendError(errorBuilder, "Błąd przy zapisie nie gotowych tweetów. ", e);
         }
 
         mergedTweets = tweetService.getActualTweetForAccounts(acs,
                                                               fetchRequest.getDateFrom(),
                                                               fetchRequest.getDateTo(),
                                                               fetchRequest.getSize());
 
         List<Message> messages = Message.apply(mergedTweets);
 
         return new FetchResult(messages, errorBuilder);
 
     }
 
     public FetchResult handleToBridges( ApplicationData appData, List<Account> accounts ) {
 
         //TODO
         return new FetchResult(new ArrayList<Message>(), new StringBuilder());
 
     }
 }
