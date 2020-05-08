 package util;
 
 import org.apache.commons.lang.StringUtils;
 
 /**
  * User: lbouin
  * Date: 21/08/12
  * Time: 01:02
  */
 public class URLHelper {
 
     public static final String USERS_SELF_URL = "https://api.foursquare.com/v2/users/self?oauth_token=USER_TOKEN&client_id=CLT_ID&client_secret=CLT_SECRET&v=VERSION";
     public static final String REPLY_URL = "https://api.foursquare.com/v2/checkins/PUSH_ID/reply?text=REPLY_TEXT&oauth_token=USER_TOKEN&v=VERSION";
 
     public static String buildUserSelfUrl(String userToken){
 
     String result = StringUtils.replace(USERS_SELF_URL,"USER_TOKEN",userToken);
     result = StringUtils.replace(result,"CLT_ID", FoursquareConfiguration.getInstance().getClientId());
     result = StringUtils.replace(result,"CLT_SECRET", FoursquareConfiguration.getInstance().getClientSecret());
     result = StringUtils.replace(result,"VERSION", FoursquareConfiguration.getInstance().getVersion());
 
     return result;
   }
 
   public static String buildReplyUrl(String pushedId, String text, String userToken){
 
     String result =  StringUtils.replace(REPLY_URL,"USER_TOKEN",userToken);
     result = StringUtils.replace(result, "PUSH_ID", ""+pushedId);
     result = StringUtils.replace(result, "REPLY_TEXT", text);
    result = StringUtils.replace(result,"VERSION", FoursquareConfiguration.getInstance().getVersion());
 
     return result;
   }
 
 
 }
