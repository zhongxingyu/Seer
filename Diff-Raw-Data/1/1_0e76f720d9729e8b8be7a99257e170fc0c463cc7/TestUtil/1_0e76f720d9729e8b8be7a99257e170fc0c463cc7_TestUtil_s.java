 package net.messze.valahol.service;
 
 import com.google.common.base.Charsets;
 import com.google.common.io.Resources;
 import com.google.inject.AbstractModule;
 import com.google.inject.Guice;
 import com.google.inject.Injector;
 import com.google.inject.name.Names;
 import com.mongodb.BasicDBObject;
 import com.mongodb.DB;
 import com.mongodb.util.JSON;
 import net.messze.valahol.AuthApi;
 import org.bson.types.ObjectId;
 import org.mockito.Mockito;
 
 import javax.inject.Inject;
 import javax.inject.Named;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpSession;
 import java.io.IOException;
 import java.util.Properties;
 
 public class TestUtil {
 
     private static void restore(DB db, String collection, String... files) throws IOException {
         db.getCollection(collection).drop();
         for (String file : files) {
             BasicDBObject obj = (BasicDBObject) JSON.parse(Resources.toString(Resources.getResource(file), Charsets.UTF_8));
             if (obj.get("_id") != null) {
                 obj.put("_id", new ObjectId(obj.get("_id").toString()));
             }
             db.getCollection(collection).save(obj);
         }
     }
 
     public static void defaultState(DB db) throws IOException {
         restore(db, "user", "user1.json", "user2.json");
         restore(db, "puzzle", "puzzle1.json", "puzzle2.json");
         restore(db, "solution", "solution1.json", "solution2.json", "solution3.json");
         restore(db, "guess", "guess1.json");
     }
 
     public static HttpServletRequest requestWithUserSession(String userId) {
         HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
         HttpSession session = Mockito.mock(HttpSession.class);
         Mockito.when(session.getAttribute(AuthApi.USER_ID)).thenReturn(userId);
         Mockito.when(request.getSession()).thenReturn(session);
         return request;
     }
 
     public static <T> T createApi(final Class<T> type) {
         Injector injector = Guice.createInjector(new AbstractModule() {
             @Override
             protected void configure() {
                 Properties defaultProperties = new Properties();
                 defaultProperties.put("mongoHost", "localhost");
                 defaultProperties.put("mongoPort", "27017");
                 defaultProperties.put("mongoDb", "valahol");
 
                 defaultProperties.put("googleApiSecret", "valahol");
                 defaultProperties.put("googleApiClientId", "valahol");
                 Names.bindProperties(binder(),defaultProperties);
                 bind(type);
                 bind(MongodbPersistence.class);
             }
         });
 
         return injector.getInstance(type);
     }
 
 
 }
