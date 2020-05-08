 package jKlout2;
 
 import java.io.IOException;
 import java.util.ArrayList;
 
 import com.google.gson.Gson;
 import com.google.gson.GsonBuilder;
 import com.google.gson.JsonArray;
 import com.google.gson.JsonElement;
 import com.google.gson.JsonParser;
 import jKlout2.model.Identity;
 import jKlout2.model.Influence;
 import java.util.List;
 import jKlout2.model.KloutError;
 import jKlout2.types.KloutNetwork;
 import jKlout2.model.Score;
 import jKlout2.model.Topic;
 import jKlout2.model.User;
 
 class KloutImpl implements Klout {
 
     // base klout url
     private static final String KLOUT_BASE_URL = "http://api.klout.com/v2/";
     private String kloutApiKey;
     private static final String IDENTITY = "identity.json";
     private static final String USER = "user.json";
     private static final String INFORMATION_INFLUENCE = "influence";
     private static final String INFORMATION_TOPICS = "topics";
     private static final String INFORMATION_SCORE = "score";
     private static Gson gson;
     private final HttpConnector connector;
 
     /**
      * constructor for a Klout-object.
      *
      * the constructor is package private, so the factory can only generate a
      * klout-object
      *
      * @param apiKey
      * @param connector
      */
     KloutImpl(String apiKey, HttpConnector connector) {
         this.kloutApiKey = apiKey;
         this.connector = connector;
         this.gson = new GsonBuilder().registerTypeAdapter(List.class, new InfluenceItemListDeserializer()).create();
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public Score getUserScore(String userId) throws KloutException {
         String url = KLOUT_BASE_URL;
         url += USER + "/" + userId + "/" + INFORMATION_SCORE + "?key="
                 + this.kloutApiKey;
         try {
             connector.setURL(url);
             String json = connector.getContent();
             Score s = gson.fromJson(json, Score.class);
             return s;
         } catch (IOException ex) {
             throw new KloutException("something went wrong with the connection");
         } catch (IllegalStateException ex) {
             KloutError e = gson.fromJson(ex.getMessage(), KloutError.class);
             throw new KloutException(e.getError() + ": " + e.getDescription());
         }
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public User getUser(String userId) throws KloutException {
         String url = KLOUT_BASE_URL;
         url += USER + "/" + userId + "?key="
                 + this.kloutApiKey;
         try {
             connector.setURL(url);
             String json = connector.getContent();
             User s = gson.fromJson(json, User.class);
             return s;
         } catch (IOException ex) {
             throw new KloutException("something went wrong with the connection");
         } catch (IllegalStateException ex) {
             KloutError e = gson.fromJson(ex.getMessage(), KloutError.class);
             throw new KloutException(e.getError() + ": " + e.getDescription());
         }
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public List<Topic> getTopics(String userId) throws KloutException {
         String url = KLOUT_BASE_URL;
         url += USER + "/" + userId + "/" + INFORMATION_TOPICS + "?key="
                 + this.kloutApiKey;
         List<Topic> topicList = new ArrayList<Topic>();
         try {
             connector.setURL(url);
             String json = connector.getContent();
             JsonArray jsonO = new JsonParser().parse(json).getAsJsonArray();
             for (JsonElement jsonElement : jsonO) {
                 Topic t = gson.fromJson(jsonElement, Topic.class);
                 topicList.add(t);
             }
         } catch (IOException ex) {
             throw new KloutException("something went wrong with the connection");
         } catch (IllegalStateException ex) {
             KloutError e = gson.fromJson(ex.getMessage(), KloutError.class);
             throw new KloutException(e.getError() + ": " + e.getDescription());
         }
         return topicList;
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public Influence getInfluence(String userId) throws KloutException {
         String url = KLOUT_BASE_URL;
         url += USER + "/" + userId + "/" + INFORMATION_INFLUENCE + "?key="
                 + this.kloutApiKey;
         try {
             connector.setURL(url);
             String json = connector.getContent();
             Influence s = gson.fromJson(json, Influence.class);
             return s;
         } catch (IOException ex) {
             throw new KloutException("something went wrong with the connection");
         } catch (IllegalStateException ex) {

             KloutError e = gson.fromJson(ex.getMessage(), KloutError.class);
             throw new KloutException(e.getError() + ": " + e.getDescription());
         }
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public Identity getIdentityFromTwitterID(String twitterId) throws KloutException {
         String url = KLOUT_BASE_URL;
         url += IDENTITY + "/" + KloutNetwork.TWITTER.getShortName() + "/" + twitterId + "?key=" + this.kloutApiKey;
         try {
             connector.setURL(url);
             String json = connector.getContent();
             Identity i = gson.fromJson(json, Identity.class);
             return i;
         } catch (IOException ex) {
             throw new KloutException("something went wrong with the connection");
         } catch (IllegalStateException ex) {
             KloutError e = gson.fromJson(ex.getMessage(), KloutError.class);
             throw new KloutException(e.getError() + ": " + e.getDescription());
         }
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public Identity getIdentityFromGooglePlusID(String googlePlusId) throws KloutException {
         String url = KLOUT_BASE_URL;
         url += IDENTITY + "/" + KloutNetwork.GOOGLE_PLUS.getShortName() + "/" + googlePlusId + "?key=" + this.kloutApiKey;
         try {
             connector.setURL(url);
             String json = connector.getContent();
             Identity i = gson.fromJson(json, Identity.class);
             return i;
         } catch (IOException ex) {
             throw new KloutException("something went wrong with the connection");
         } catch (IllegalStateException ex) {
             KloutError e = gson.fromJson(ex.getMessage(), KloutError.class);
             throw new KloutException(e.getError() + ": " + e.getDescription());
         }
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public Identity getIdentityFromTwitterScreenName(String screenName) throws KloutException {
         String url = KLOUT_BASE_URL;
        url += IDENTITY + "/" + KloutNetwork.TWITTER.getLongName() + "?screenName=" + screenName + "&key=" + this.kloutApiKey;
         try {
             connector.setURL(url);
             String json = connector.getContent();
             Identity i = gson.fromJson(json, Identity.class);
             return i;
         } catch (IOException ex) {
             throw new KloutException("something went wrong with the connection");
         } catch (IllegalStateException ex) {
             KloutError e = gson.fromJson(ex.getMessage(), KloutError.class);
             throw new KloutException(e.getError() + ": " + e.getDescription());
         }
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public Identity getIdentityFromKloutID(String kloutID, KloutNetwork targetNetwork) throws KloutException {
         String url = KLOUT_BASE_URL;
         url += IDENTITY + "/" + KloutNetwork.KLOUT.getLongName() + "/" + kloutID + "/" + targetNetwork.getShortName() + "?key=" + this.kloutApiKey;
         try {
             connector.setURL(url);
             String json = connector.getContent();
             Identity i = gson.fromJson(json, Identity.class);
             return i;
         } catch (IOException ex) {
             throw new KloutException("something went wrong with the connection");
         } catch (IllegalStateException ex) {
             KloutError e = gson.fromJson(ex.getMessage(), KloutError.class);
             throw new KloutException(e.getError() + ": " + e.getDescription());
         }
     }
 }
