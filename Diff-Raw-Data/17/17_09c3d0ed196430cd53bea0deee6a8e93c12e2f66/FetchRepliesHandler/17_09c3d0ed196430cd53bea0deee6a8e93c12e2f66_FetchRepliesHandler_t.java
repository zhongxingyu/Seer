 package com.pk.cwierkacz.processor.handlers;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.apache.commons.lang3.StringUtils;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.pk.cwierkacz.http.Action;
 import com.pk.cwierkacz.http.Status;
 import com.pk.cwierkacz.http.request.FetchRepliesRequest;
 import com.pk.cwierkacz.http.response.Response;
 import com.pk.cwierkacz.http.response.ResponseImpl;
 import com.pk.cwierkacz.http.response.dto.Message;
 import com.pk.cwierkacz.model.ApplicationData;
 import com.pk.cwierkacz.model.Result;
 import com.pk.cwierkacz.model.dao.TweetDao;
 import com.pk.cwierkacz.model.dao.TwitterAccountDao;
 import com.pk.cwierkacz.model.service.ServiceRepo;
 import com.pk.cwierkacz.model.service.TweetService;
import com.pk.cwierkacz.processor.handlers.helpers.AttachmentsWithResources;
 import com.pk.cwierkacz.processor.handlers.helpers.FileSaver;
 import com.pk.cwierkacz.twitter.TweetsResult;
 import com.pk.cwierkacz.twitter.TwitterAccount;
 import com.pk.cwierkacz.twitter.TwitterAccountMap;
 import com.pk.cwierkacz.twitter.TwitterActionException;
 import com.pk.cwierkacz.twitter.TwitterAuthenticationException;
 
 public class FetchRepliesHandler extends AbstractHandler
 {
     private static final Logger LOGGER = LoggerFactory.getLogger(FetchMessagesHandler.class);
 
     private final TweetService tweetService;
 
     private final FileSaver fileSaver;
 
     private TweetDao tweetWithImg( TweetDao t ) throws IOException {
        AttachmentsWithResources awr = fileSaver.saveFileFromUrl(t.getTwitterImageUrl());
        if ( awr != null )
            t.setImagePath(awr.getImgPath());
         return t;
     }
 
     public FetchRepliesHandler() {
         this.tweetService = ServiceRepo.getInstance().getService(TweetService.class);
         this.fileSaver = new FileSaver();
     }
 
     @Override
     public boolean isHandleable( ApplicationData applicationData ) {
         return applicationData.getRequest().getAction().equals(Action.FETCHREPLIES);
     }
 
     @Override
     public Result checkSecurity( ApplicationData appData ) {
         FetchRepliesRequest fetchRequest = (FetchRepliesRequest) appData.getRequest();
 
         if ( fetchRequest.getReplayFor() <= 0 ) {
             return new Result("Aby pobrać odpowiedzi na tweeta musisz podać identyfikator tweeta na którego chcesz pobrać wiadomości",
                               Status.ERROR);
         }
 
         return new Result("OK", Status.OK);
     }
 
     @Override
     public void handle( ApplicationData appData ) {
         FetchRepliesRequest fetchRequest = (FetchRepliesRequest) appData.getRequest();
         StringBuilder errorBuilder = new StringBuilder();
         List<TweetDao> mergedTweets = new ArrayList<TweetDao>();
         TwitterAccountDao accountDao = null;
         try {
             TweetDao replyTweet = tweetService.getTweetById(fetchRequest.getReplayFor());
             if ( replyTweet != null ) {
                 accountDao = replyTweet.getCreator();
                 TweetDao last = tweetService.getLastActualReplies(replyTweet);
                 if ( last == null )
                     last = replyTweet;
                 TwitterAccount account;
                 account = TwitterAccountMap.getTwitterAccount(accountDao);
                 TweetsResult result = account.getTweetsFromMentionsAndUserTimeline(last);
                 for ( TweetDao tweet : result.getReadyTweets() ) {
                     tweetService.save(tweetWithImg(tweet));
                 }
                 for ( TweetDao tweet : result.fulfilledNoReady() ) {
                     tweetService.save(tweetWithImg(tweet));
                 }
 
                 mergedTweets = tweetService.getActualReplies(replyTweet);
             }
             //else nothing to do, because we may don't know what is owner of the main tweet
         }
         catch ( TwitterAuthenticationException e ) {
             LOGGER.error(getError(e));
             errorBuilder = appendError(errorBuilder, "Błąd autoryzacji dla: " +
                                                      accountDao.getAccountName() +
                                                      ".", e);
         }
         catch ( TwitterActionException e ) {
             LOGGER.error(getError(e));
             errorBuilder = appendError(errorBuilder,
                                        "Bład pobierania tweetów dla " + accountDao.getAccountName() + ".",
                                        e);
         }
         catch ( Throwable e ) {
             LOGGER.error(getError(e));
             errorBuilder = appendError(errorBuilder, "Wewnętrzny bład aplikacji.", e);
         }
 
         Response response;
 
         String errors = errorBuilder.toString();
         if ( StringUtils.isEmpty(errors) ) {
             response = ResponseImpl.create(Status.OK,
                                            "Pobrano pomyślnie wszystkie tweety. ",
                                            appData.getRequest().getTokenId())
                                    .buildFetchResponse(Message.apply(mergedTweets));
             ;
         }
         else {
             response = ResponseImpl.create(Status.ERROR,
                                            "Bład pobierania tweetów. " + errors,
                                            appData.getRequest().getTokenId())
                                    .buildFetchResponse(Message.apply(mergedTweets));
         }
 
         appData.setResponse(response);
 
     }
 }
