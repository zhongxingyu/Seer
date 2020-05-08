 package com.pk.cwierkacz.processor.handlers;
 
 import java.util.Set;
 
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
 import com.pk.cwierkacz.model.service.TwitterAccountService;
 import com.pk.cwierkacz.model.service.UserService;
 
 public class UnwireTweetAcocunt extends AbstractHandler
 {
 
     private final UserService userService;
     private final SessionService sessionService;
     private final TwitterAccountService accountService;
 
     public UnwireTweetAcocunt() {
         super();
         userService = ServiceRepo.getInstance().getService(UserService.class);
         sessionService = ServiceRepo.getInstance().getService(SessionService.class);
         accountService = ServiceRepo.getInstance().getService(TwitterAccountService.class);
     }
 
     @Override
     public boolean isHandleable( ApplicationData applicationData ) {
         return applicationData.getRequest().getAction().equals(Action.UNLINKSOCIALACCOUNT);
     }
 
     @Override
     public void handle( ApplicationData appData ) {
         AddTweeterAccountRequest accRequest = (AddTweeterAccountRequest) appData.getRequest();
 
         SessionDao sessionDao = sessionService.getByToken(accRequest.getTokenId());
         UserDao user = userService.getBySessionId(sessionDao);
         if ( user != null && user.getSession().getCurrentToken() != accRequest.getTokenId() ) {
             Response response = ResponseImpl.create(Status.DENY, "Zły token.", accRequest.getTokenId());
             appData.setResponse(response);
             return;
         }
 
         Set<TwitterAccountDao> accounts = user.getAccounts();
 
         TwitterAccountDao accountDaoToRemove = null;
 
         for ( TwitterAccountDao accountDao : accounts ) {
             if ( accountDao.getAccountName().equals(accRequest.getLoginTweet()) ) {
                 accountDaoToRemove = accountDao;
             }
         }
        accountService.delete(accountDaoToRemove);
 
         Response response = ResponseImpl.create(Status.OK, "Konto odwiązano.", accRequest.getTokenId());
         appData.setResponse(response);
         return;
     }
 }
