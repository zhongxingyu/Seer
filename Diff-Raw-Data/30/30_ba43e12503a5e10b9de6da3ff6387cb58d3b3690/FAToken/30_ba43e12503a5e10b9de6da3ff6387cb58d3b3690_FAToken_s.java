 package net.dmcloud.util;
 
 import java.util.Map;
 import java.util.HashMap;
 import java.util.Date;
 import java.io.IOException;
 import java.security.SecureRandom;
 import java.math.BigInteger;
 import net.dmcloud.util.DCObject;
 import net.dmcloud.cloudkey.Helpers;
 import org.codehaus.jackson.map.ObjectMapper;
 import org.apache.commons.codec.binary.Base64;
 
 public class FAToken {
 
     private String user_id  = null;
     private String media_id = null;
     private String api_key = null;
     private String nonce = null;
    private HashMap<String,String> rights;
    private HashMap<String,String> meta;
     private String callback_url = null;
     private Integer expires = 0;
     private Integer maxReplay = 0;
 
     public static int DEFAULT_TIMEOUT = 600;
 
     public FAToken(String token, String api_key) throws Exception {
         this(token, api_key, true);
     }
 
     public FAToken(String token, String api_key, Boolean check_auth) throws Exception {
 		ObjectMapper mapper;
 		DCObject token_info;
         String auth;
 
         this.api_key = api_key;
 
         if (Base64.isBase64(token)) {
             token = new String(Base64.decodeBase64(token));
         }
 
         mapper = new ObjectMapper();
         token_info = DCObject.create(mapper.readValue(token, Map.class));
         user_id = token_info.get("user_id").toString();
         media_id = token_info.get("media_id").toString();
         expires = Integer.parseInt(token_info.get("expires").toString());
         if (token_info.containsKey("max_replay")) {
             maxReplay = Integer.parseInt(token_info.get("max_replay").toString());
         }
         if (token_info.containsKey("nonce")) {
             nonce = token_info.get("nonce").toString();
         }
         if (token_info.containsKey("rights")) {
             rights = (HashMap<String,String>)token_info.get("rights");
         }
         if (token_info.containsKey("meta")) {
             meta = (HashMap<String,String>)token_info.get("meta");
         }
         if (token_info.containsKey("callback_url")) {
             callback_url = token_info.get("callback_url").toString();
         }
 
         if (check_auth == true) {
             auth = token_info.get("auth").toString();
 
             token_info.remove("auth");
             if (!getAuth(token_info).equals(auth)) {
                 throw new Exception("Auth doesn't match");
             }
         }
     }
 
     public FAToken(String user_id, String media_id, String api_key) {
         this(user_id, media_id, api_key, (int)(new Date().getTime() / 1000) + DEFAULT_TIMEOUT);
     }
 
     public FAToken(String user_id, String media_id, String api_key, Integer expires) {
         this.user_id = user_id;
         this.media_id = media_id;
         this.api_key = api_key;
         this.rights = new HashMap<String,String>();
         this.meta = new HashMap<String,String>();
         this.expires = expires;
     }
 
     public Boolean isExpired() {
         Date expireDate = new Date(Long.valueOf(expires) * 1000);
         return expireDate.before(new Date());
     }
 
     public void setExpires(Integer expires) {
         this.expires = expires;
     }
 
     public Integer getExpires() {
         return this.expires;
     }
 
     public void setMaxReplay(Integer maxReplay) {
         this.maxReplay = maxReplay;
     }
 
     public Integer getMaxReplay() {
         return this.maxReplay;
     }
 
     public String getUserId() {
         return user_id;
     }
 
     public String getMediaId() {
         return media_id;
     }
 
     public void setRight(String name, String value) {
         rights.put(name, value);
     }
 
     public HashMap<String,String> getRights() {
         return rights;
     }
 
     public void setMeta(String name, String value) {
         meta.put(name, value);
     }
 
     public HashMap<String,String> getMeta() {
         return meta;
     }
 
     public void setCallbackUrl(String callback_url) {
         this.callback_url = callback_url;
     }
 
     public String getCallbackUrl() {
         return this.callback_url;
     }
 
     public void setNonce(String nonce) {
         this.nonce = nonce;
     }
 
     public String getNonce() {
         return nonce;
     }
 
     public void genNonce() {
         SecureRandom random = new SecureRandom();
         nonce = new BigInteger(130, random).toString(32);
     }
 
     public String getAuth(DCObject token_info) {
         return Helpers.md5(Helpers.normalize(token_info) + api_key);
     }
 
     public String toJSON() throws Exception {
 		ObjectMapper mapper;
         DCObject token_info;

         if (nonce == null) {
             genNonce();
         }
 		token_info = DCObject.create()
             .push("user_id", user_id)
             .push("media_id", media_id)
             .push("expires", expires)
             .push("nonce", nonce);
         if (maxReplay > 0) {
             token_info.push("max_replay", maxReplay);
         }
         if (callback_url != null) {
             token_info.push("callback_url", callback_url);
         }
         if (!rights.isEmpty()) {
             token_info.push("rights", rights);
         }
         if (!meta.isEmpty()) {
             token_info.push("meta", meta);
         }
         mapper = new ObjectMapper();
 		token_info.push("auth", getAuth(token_info));
         return mapper.writeValueAsString(token_info);
     }
 
     public String toBase64() throws Exception {
         return Base64.encodeBase64String(toJSON().getBytes());
    }
 
 }
