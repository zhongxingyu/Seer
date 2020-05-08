 package com.pk.cwierkacz.http.request;
 
 import java.sql.Timestamp;
 import java.util.Date;
 import java.util.List;
 
 import org.joda.time.DateTime;
 
 import com.pk.cwierkacz.http.Action;
 
 // login,logout,createAccount - by Action
 public class RequestImpl implements
                         Request,
                         LoginRequest,
                         AddTweeterAccountRequest,
                         PublishRequest,
                         FetchTweetsRequest,
                         AccountManageRequest
 {
     //Basic
     private Action action;
     private Timestamp timestamp;
     private long tokenId;
 
     //SIGNIN, ADDACCOUNT
     private String userName;
 
     //ADDTWEETACCOUNT, DELTWEETACCOUNT
     private String loginTweet;
 
     //ADDTWEETACCOUNT
     private String passwordTweet;
 
     //FETCHTWEETS, PUBLISHTWEET
     private List<String> accounts;
 
     //FETCHTWEETS
     private int size;
     private DateTime dateFrom;
     private DateTime dateTo;
 
     //SIGNIN, DELACCOUNT
     private String password;
     private String newPassword;
 
     //FETCHTWEETS, PUBLISHTWEET
     private long replayFor;
     private long retweetFor;
 
     //PUBLISHTWEET
     private String tweetText;
 
     public static RequestImpl create( ) {
         RequestImpl requestImpl = new RequestImpl();
         requestImpl.timestamp = new Timestamp(new Date().getTime());
 
         return new RequestImpl();
     }
 
     public static RequestImpl create( Request request ) {
         RequestImpl impl = (RequestImpl) request;
 
         RequestImpl requestImpl = new RequestImpl();
         requestImpl.accounts = impl.getAccounts();
         requestImpl.action = impl.getAction();
         requestImpl.dateFrom = impl.getDateFrom();
         requestImpl.dateTo = impl.getDateTo();
         requestImpl.loginTweet = impl.getLoginTweet();
         requestImpl.newPassword = impl.getNewPassword();
         requestImpl.password = impl.getPassword();
         requestImpl.passwordTweet = impl.getPasswordTweet();
         requestImpl.replayFor = impl.getReplayFor();
         requestImpl.retweetFor = impl.getRetweetFor();
         requestImpl.size = impl.getSize();
         requestImpl.timestamp = impl.getTimestamp();
         requestImpl.tokenId = impl.getTokenId();
         requestImpl.tweetText = impl.getTweetText();
         requestImpl.userName = impl.getUserName();
         return requestImpl;
     }
 
     @SuppressWarnings( "unchecked" )
     public < T extends RequestImpl > T buildEmptyRequest( ) {
         this.action = Action.NOTRECOGNIZED;
         return (T) this;
     }
 
     @SuppressWarnings( "unchecked" )
     public < T extends RequestImpl > T buildBaseRequest( Action action, String userName ) {
         this.action = action;
         this.userName = userName;
         return (T) this;
     }
 
     @SuppressWarnings( "unchecked" )
     public < T extends RequestImpl > T withTokenId( Long tokenId ) {
         this.tokenId = tokenId;
         return (T) this;
     }
 
     @SuppressWarnings( "unchecked" )
     public < T extends RequestImpl > T withTimestamp( Timestamp timestamp ) {
         this.timestamp = timestamp;
         return (T) this;
     }
 
     @SuppressWarnings( "unchecked" )
     public < T extends RequestImpl > T buildLoginRequest( String password ) {
         this.password = password;
         return (T) this;
     }
 
     @SuppressWarnings( "unchecked" )
     public < T extends RequestImpl > T withNewPassword( String newPassword ) {
         this.newPassword = newPassword;
         return (T) this;
     }
 
     @SuppressWarnings( "unchecked" )
     public < T extends RequestImpl > T buildAddAccountTweetRequest( String loginTweet ) {
         this.loginTweet = loginTweet;
         return (T) this;
     }
 
     @SuppressWarnings( "unchecked" )
     public < T extends RequestImpl > T buildPublishRequest( String tweetText, List<String> accounts ) {
         this.tweetText = tweetText;
         this.accounts = accounts;
         return (T) this;
     }
 
     @SuppressWarnings( "unchecked" )
     public < T extends RequestImpl > T withReplayForID( Long replayForID ) {
         this.replayFor = replayForID;
         return (T) this;
     }
 
     @SuppressWarnings( "unchecked" )
     public < T extends Request > T buildPublishRequest( String tweetText,
                                                         List<String> accounts,
                                                         long replayForId ) {
         this.tweetText = tweetText;
         this.accounts = accounts;
        this.replayFor = replayForId;
         return (T) this;
     }
 
     @SuppressWarnings( "unchecked" )
     public < T extends RequestImpl > T withRetweetForId( Long retweetForId ) {
         this.retweetFor = retweetForId;
         return (T) this;
     }
 
     @SuppressWarnings( "unchecked" )
     public < T extends RequestImpl > T buildFetchRequest( List<String> accounts ) {
         this.accounts = accounts;
         return (T) this;
     }
 
     @SuppressWarnings( "unchecked" )
     public < T extends Request > T buildFetchRequest( List<String> accounts,
                                                       int size,
                                                       DateTime dateFrom,
                                                       long replayForId ) {
         this.accounts = accounts;
         this.size = size;
         this.dateFrom = dateFrom;
        this.replayFor = replayForId;
         return (T) this;
     }
 
     @SuppressWarnings( "unchecked" )
     public < T extends Request > T withSize( int size ) {
         this.size = size;
         return (T) this;
     }
 
     @SuppressWarnings( "unchecked" )
     public < T extends RequestImpl > T withDateFrom( DateTime dateTime ) {
         this.dateFrom = dateTime;
         return (T) this;
     }
 
     @SuppressWarnings( "unchecked" )
     public < T extends RequestImpl > T withDateTo( DateTime dateTime ) {
         this.dateTo = dateTime;
         return (T) this;
     }
 
     @SuppressWarnings( "unchecked" )
     public < T extends RequestImpl > T withPasswordTweet( String passwordTweet ) {
         this.passwordTweet = passwordTweet;
         return (T) this;
     }
 
     @Override
     public Action getAction( ) {
         return action;
     }
 
     @Override
     public Timestamp getTimestamp( ) {
         return timestamp;
     }
 
     @Override
     public long getTokenId( ) {
         return tokenId;
     }
 
     @Override
     public String getUserName( ) {
         return userName;
     }
 
     @Override
     public String getLoginTweet( ) {
         return loginTweet;
     }
 
     @Override
     public String getPasswordTweet( ) {
         return passwordTweet;
     }
 
     @Override
     public List<String> getAccounts( ) {
         return accounts;
     }
 
     @Override
     public int getSize( ) {
         return size;
     }
 
     @Override
     public DateTime getDateFrom( ) {
         return dateFrom;
     }
 
     @Override
     public DateTime getDateTo( ) {
         return dateTo;
     }
 
     @Override
     public String getPassword( ) {
         return password;
     }
 
     @Override
     public String getNewPassword( ) {
         return newPassword;
     }
 
     @Override
     public long getReplayFor( ) {
         return replayFor;
     }
 
     @Override
     public String getTweetText( ) {
         return tweetText;
     }
 
     @Override
     public long getRetweetFor( ) {
         return retweetFor;
     }
 }
