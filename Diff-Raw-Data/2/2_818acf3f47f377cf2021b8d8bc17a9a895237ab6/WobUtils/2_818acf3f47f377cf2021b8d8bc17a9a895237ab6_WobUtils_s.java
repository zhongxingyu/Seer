 package com.google.wallet.objects;
 
 import java.io.ByteArrayInputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.InputStream;
 import java.security.GeneralSecurityException;
 import java.security.KeyStoreException;
 import java.security.SignatureException;
 import java.security.interfaces.RSAPrivateKey;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.List;
 
 import javax.servlet.ServletContext;
 
 import net.oauth.jsontoken.JsonToken;
 import net.oauth.jsontoken.crypto.RsaSHA256Signer;
 
 import org.joda.time.Instant;
 
 import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
 import com.google.api.client.http.HttpTransport;
 import com.google.api.client.http.javanet.NetHttpTransport;
 import com.google.api.client.json.GenericJson;
 import com.google.api.client.json.JsonFactory;
 import com.google.api.client.json.gson.GsonFactory;
 import com.google.api.client.util.SecurityUtils;
 import com.google.api.services.walletobjects.Walletobjects;
 import com.google.api.services.walletobjects.model.BoardingPassObject;
 import com.google.api.services.walletobjects.model.GenericObject;
 import com.google.api.services.walletobjects.model.LoyaltyObject;
 import com.google.api.services.walletobjects.model.OfferObject;
 import com.google.common.io.ByteStreams;
 import com.google.gson.Gson;
 import com.google.gson.JsonObject;
 
 public class WobUtils {
 
   private final String SAVE_TO_WALLET = "savetowallet";
   private final String LOYALTY_WEB = "loyaltywebservice";
   private final String WOB_PROD = "https://www.googleapis.com/auth/wallet_object.issuer";
   
   private String serviceAccountId;
   private String rsaKeyPath;
   private String applicationName;
   private Long issuerId;
   private GoogleCredential credential = null;
   
   private HttpTransport httpTransport;
   private JsonFactory jsonFactory;
   
   RsaSHA256Signer signer = null;
   
   /**
    * Constructor takes in servlet context to access web.xml config
    * parameters
    * 
    * @param context Servlet context
    * @throws FileNotFoundException
    * @throws IOException
    * @throws KeyStoreException
    * @throws GeneralSecurityException
    */
   public WobUtils(ServletContext context) throws FileNotFoundException, IOException, KeyStoreException, GeneralSecurityException{  
     serviceAccountId = context.getInitParameter("ServiceAccountId");
     rsaKeyPath = context.getInitParameter("ServiceAccountPrivateKey");
     applicationName = context.getInitParameter("ApplicationName");
     issuerId = Long.decode(context.getInitParameter("IssuerId"));
     httpTransport = new NetHttpTransport();
     jsonFactory = new GsonFactory();
     File file = new File(rsaKeyPath);
     byte[] bytes = ByteStreams.toByteArray(new FileInputStream(file));
     InputStream inputStream = new ByteArrayInputStream(bytes);
     RSAPrivateKey rsaKey = (RSAPrivateKey) SecurityUtils.loadPrivateKeyFromKeyStore(SecurityUtils.getPkcs12KeyStore(), inputStream, "notasecret", "privatekey", "notasecret");
     signer = new RsaSHA256Signer(serviceAccountId, null, rsaKey);
   }
   
   /**
    * Creates a Walletobjects client with sandbox and production scopes
    * 
    * @return Walletobjects client
    * @throws GeneralSecurityException
    * @throws IOException
    */
   public Walletobjects getClient() throws GeneralSecurityException, IOException{
     credential = getCredential();
     
     return new Walletobjects.Builder(httpTransport, jsonFactory, credential)
       .setApplicationName(applicationName)
       .build(); 
   }
   
   /**
    * Helper function to generate the Google Credential
    * @return
    * @throws GeneralSecurityException
    * @throws IOException
    */
   public GoogleCredential getCredential() throws GeneralSecurityException, IOException{ 
     List<String> scopes = new ArrayList<String>();
     scopes.add(WOB_PROD);
     
     return credential = new GoogleCredential.Builder().setTransport(httpTransport)
       .setJsonFactory(jsonFactory)
       .setServiceAccountId(serviceAccountId)
       .setServiceAccountScopes(scopes)
       .setServiceAccountPrivateKeyFromP12File(new File(rsaKeyPath))
       .build();
   }
   
   /**
    * Refreshes the access token and returns it
    * 
    * @return OAuth access token
    * @throws GeneralSecurityException
    * @throws IOException
    */
   public String accessToken() throws GeneralSecurityException, IOException {
     if(credential == null){
       credential = getCredential();
     }
     credential.refreshToken();
     return credential.getAccessToken();
   }
   
   /**
    * Generates the linking/signup JWT from a Wallet Object
    * 
    * @param object
    * @param resp
    * @return
    * @throws SignatureException
    */
  public String generateSignupJwt(GenericJson object, WebServiceResponse resp) throws SignatureException{   
     object.setFactory(new GsonFactory());
     JsonToken token = new JsonToken(signer);
     token.setAudience("google");
     token.setParam("typ", LOYALTY_WEB);
     token.setIssuedAt(new Instant(Calendar.getInstance().getTimeInMillis() - 5000L));
     Gson gson = new Gson();
     WobPayload payload = new WobPayload();
 
     if (LoyaltyObject.class.isAssignableFrom(object.getClass())){
       payload.addLoyaltyObject(gson.fromJson(object.toString(), GenericJson.class));
     } else if (OfferObject.class.isAssignableFrom(object.getClass())){
       payload.addOfferObject(gson.fromJson(object.toString(), GenericJson.class));
     } else if(GenericObject.class.isAssignableFrom(object.getClass())){
       payload.addGenericObject(gson.fromJson(object.toString(), GenericJson.class));
     } else if(BoardingPassObject.class.isAssignableFrom(object.getClass())){
       payload.addBoardingPassObject(gson.fromJson(object.toString(), GenericJson.class));
     } else 
       throw new IllegalArgumentException("Invalid Object type: " + object.getClass());
     
     payload.setResponse(resp);
     JsonObject obj = gson.toJsonTree(payload).getAsJsonObject();
     token.getPayloadAsJsonObject().add("payload", obj);
     return token.serializeAndSign();     
   }
   
   /**
    * Generates the Save to Wallet JWT from a Wallet Object
    * 
    * @param object
    * @param origins
    * @return
    * @throws SignatureException
    */
   public String generateSaveJwt(GenericJson object, List<String> origins) throws SignatureException{
     object.setFactory(jsonFactory);
     JsonToken token = new JsonToken(signer);
     token.setAudience("google");
     token.setParam("typ", SAVE_TO_WALLET);
     token.setIssuedAt(new Instant(Calendar.getInstance().getTimeInMillis() - 5000L));
     Gson gson = new Gson();
     WobPayload s2w = new WobPayload();
     
     if (LoyaltyObject.class.isAssignableFrom(object.getClass())){
       s2w.addLoyaltyObject(gson.fromJson(object.toString(), GenericJson.class));
     } else if (OfferObject.class.isAssignableFrom(object.getClass())){
       s2w.addOfferObject(gson.fromJson(object.toString(), GenericJson.class));
     } else if(GenericObject.class.isAssignableFrom(object.getClass())){
       s2w.addGenericObject(gson.fromJson(object.toString(), GenericJson.class));
     } else if(BoardingPassObject.class.isAssignableFrom(object.getClass())){
       s2w.addBoardingPassObject(gson.fromJson(object.toString(), GenericJson.class));
     } else 
       throw new IllegalArgumentException("Invalid Object type: " + object.getClass());
     
     JsonObject obj = gson.toJsonTree(s2w).getAsJsonObject();
     token.getPayloadAsJsonObject().add("payload", obj);
     token.getPayloadAsJsonObject().add("origins", gson.toJsonTree(origins));
     return token.serializeAndSign();    
   }
   
   /**
    * 
    * @return
    */
   public String getServiceAccountId() {
     return serviceAccountId;
   }
 
   /**
    * 
    * @return
    */
   public Long getIssuerId() {
     return issuerId;
   }
 }
