 package fr.ybo.ybotv.android.service;
 
 
 import android.util.Log;
 import com.google.gson.Gson;
 import com.google.gson.GsonBuilder;
 import com.google.gson.JsonSyntaxException;
 import com.google.gson.reflect.TypeToken;
 import fr.ybo.ybotv.android.exception.YboTvErreurReseau;
 import fr.ybo.ybotv.android.exception.YboTvException;
 import fr.ybo.ybotv.android.modele.Channel;
 import fr.ybo.ybotv.android.modele.Programme;
 import fr.ybo.ybotv.android.util.HttpUtils;
 import org.apache.http.HttpResponse;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.client.methods.HttpUriRequest;
 
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.Reader;
 import java.net.MalformedURLException;
 import java.util.List;
 
 public class YboTvService {
 
     private static final YboTvService instance = new YboTvService();
 
    public static final String SERVEUR = "http://ybo-tv.ybonnel.fr/";
 
     private static final String CHANNEL_SERVICE_URL = "data/channel/";
 
     private static final String PROGRAMME_SERVICE_URL = "data/programme/";
 
     private static final String CHANNEL_PARAMETER = "channel/";
 
     private YboTvService() {
     }
 
     public static YboTvService getInstance() {
         return instance;
     }
 
     private static final int MAX_RETRY = 5;
 
     private <T> List<T> getObjects(String url, TypeToken<List<T>> typeToken) throws YboTvErreurReseau {
         int countRetry = 0;
 
         while (countRetry < MAX_RETRY - 1) {
             countRetry++;
             try {
                 return getObjectsWithoutRetry(url, typeToken);
             } catch (YboTvErreurReseau erreurReseau) {
                 Log.e("YboTv", "Erreur réseau (" + countRetry + ") en accédant à l'url : " + url);
                 Log.e("YboTv", Log.getStackTraceString(erreurReseau));
             }
         }
         return getObjectsWithoutRetry(url, typeToken);
     }
 
 
     private <T> List<T> getObjectsWithoutRetry(String url, TypeToken<List<T>> typeToken) throws YboTvErreurReseau {
 
         Reader reader = null;
         try {
             Log.d("YboTv", "Url demandee : " + url);
             HttpClient client = HttpUtils.getHttpClient();
             HttpUriRequest request = new HttpGet(url);
             HttpResponse reponse = client.execute(request);
             reader = new InputStreamReader(reponse.getEntity().getContent());
             GsonBuilder gsonBuilder = new GsonBuilder();
             Gson gson = gsonBuilder.create();
             return gson.fromJson(reader, typeToken.getType());
         } catch (MalformedURLException e) {
             throw new YboTvException(e);
         } catch (IOException e) {
             throw new YboTvErreurReseau(e);
         } catch (JsonSyntaxException e) {
             if (e.getCause() instanceof IOException) {
                 throw new YboTvErreurReseau(e);
             } else {
                 throw new YboTvException(e);
             }
         } finally {
             if (reader != null) {
                 try { reader.close(); } catch (Exception ignore) {}
             }
         }
     }
 
     public List<Channel> getChannels() throws YboTvErreurReseau {
         return getObjects(SERVEUR + CHANNEL_SERVICE_URL, new TypeToken<List<Channel>>(){});
     }
 
     public List<Programme> getProgrammes(Channel channel) throws YboTvErreurReseau {
         return getObjects(SERVEUR + PROGRAMME_SERVICE_URL + CHANNEL_PARAMETER + channel.getId(), new TypeToken<List<Programme>>(){});
     }
 }
