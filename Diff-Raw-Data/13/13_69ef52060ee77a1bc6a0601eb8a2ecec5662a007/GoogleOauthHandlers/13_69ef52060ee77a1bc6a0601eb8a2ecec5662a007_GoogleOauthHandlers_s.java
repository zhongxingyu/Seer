 package com.britesnow.samplesocial.web;
 
 
 import com.britesnow.samplesocial.dao.SocialIdEntityDao;
 import com.britesnow.samplesocial.entity.SocialIdEntity;
 import com.britesnow.samplesocial.entity.User;
 import com.britesnow.samplesocial.oauth.OAuthType;
 import com.britesnow.samplesocial.oauth.OAuthUtils;
 import com.britesnow.samplesocial.service.GoogleAuthService;
 import com.britesnow.snow.util.JsonUtil;
 import com.britesnow.snow.web.RequestContext;
 import com.britesnow.snow.web.handler.annotation.WebModelHandler;
 import com.britesnow.snow.web.param.annotation.WebParam;
 import com.google.inject.Inject;
 import com.google.inject.Singleton;
 import org.scribe.model.*;
 import org.scribe.oauth.OAuthService;
 
 import java.io.IOException;
 import java.util.Date;
 import java.util.Map;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import static org.scribe.model.OAuthConstants.EMPTY_TOKEN;
 
 @Singleton
 public class GoogleOauthHandlers {
     @Inject
     private OAuthUtils oAuthUtils;
     @Inject
     private GoogleAuthService googleAuthService;
      @Inject
     private SocialIdEntityDao socialIdEntityDao;
 
     @WebModelHandler(startsWith = "/googleLogin")
     public void googleLogin(RequestContext rc) throws IOException {
         String authUrl = oAuthUtils.getOauthService(OAuthType.GOOGLE).getAuthorizationUrl(OAuthConstants.EMPTY_TOKEN);
         System.out.println(authUrl);
         rc.getRes().sendRedirect(authUrl);
     }
     @WebModelHandler(startsWith = "/googleCallback")
     public void googleCallback(RequestContext rc, @WebParam("code") String code) throws Exception {
         User user = rc.getUser(User.class);
         if (user!=null && code != null) {
             OAuthService googleService = oAuthUtils.getOauthService(OAuthType.GOOGLE);
             Verifier verifier = new Verifier(code);
             Token accessToken = googleService.getAccessToken(EMPTY_TOKEN, verifier);
             if (accessToken.getToken() != null) {
                 //get expire date
                 String rawResponse = accessToken.getRawResponse();
                 Pattern expire = Pattern.compile("\"expires_in\"\\s*:\\s*\"(\\d+)\"");
                 Matcher matcher = expire.matcher(rawResponse);
                 long expireDate =-1;
                 if (matcher.find()) {
 
                     expireDate = System.currentTimeMillis() + (Integer.valueOf(matcher.group(1)) - 100) * 100;
 
                 }
                 //get email
 //                OAuthRequest request = new OAuthRequest(Verb.GET, OAuthUtils.EMAIL_ENDPOINT);
 //                googleService.signRequest(accessToken, request);
 //                Response response = request.send();
 //                System.out.println(response.getBody());
 //                //todo extract email
                 //get userinfo
                 OAuthRequest request = new OAuthRequest(Verb.GET, OAuthUtils.PROFILE_ENDPOINT);
                 googleService.signRequest(accessToken, request);
                 Response response = request.send();
                 System.out.println(response.getBody());
                 Map profile = JsonUtil.toMapAndList(response.getBody());
                 System.out.println(profile);
                 //todo extract userinfo
                 SocialIdEntity social = googleAuthService.getSocialIdEntity(user.getId());
                 boolean newSocial = false;
                 if (social == null) {
                     social = new SocialIdEntity();
                     newSocial = true;
                 }
                 social.setEmail((String) profile.get("email"));
                 social.setUser_id(user.getId());
                 social.setToken(accessToken.getToken());
                social.setService("google");
                 social.setTokenDate(new Date(expireDate));
                 if (newSocial) {
                     socialIdEntityDao.save(social);
                 }else {
                     socialIdEntityDao.update(social);
                 }
             } else {
                 googleLogin(rc);
             }
 
         }
     }
 }
