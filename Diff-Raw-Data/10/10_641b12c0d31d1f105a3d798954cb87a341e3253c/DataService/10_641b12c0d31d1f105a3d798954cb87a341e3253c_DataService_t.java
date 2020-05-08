 package service;
 
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 
 import models.Carte;
 import models.Data;
 
 import com.mongodb.DBCollection;
 import com.mongodb.DBCursor;
 import com.mongodb.DBObject;
 
 public class DataService {
 
     public static List<Data> getDataFromCarte(String uuid) {
         List<Data> datas = new ArrayList<Data>();
         Carte carte = CarteService.findMapByUuid(uuid);
         DBCollection dataColl = MongoDb.db().getCollection("carte-" + uuid);
         DBCursor cursor = dataColl.find();
 
         Integer i = 0;
         while (cursor.hasNext()) {
             i++;
             Data data = new Data();
             DBObject mongo = cursor.next();
 
             if (mongo.containsField(Geocoding.LATITUDE))
                 data.latitude = Float.valueOf((String) mongo.get(Geocoding.LATITUDE));
             if (mongo.containsField(Geocoding.LONGITUDE))
                 data.longitude = Float.valueOf((String) mongo.get(Geocoding.LONGITUDE));
 
             List<String> headers = DataService.getDataHeader(uuid);
             String txt = "";
             if (carte.pattern != null && !carte.pattern.equals("")) {
                 txt = carte.pattern;
                 for (int j = 0; j < headers.size(); j++) {
                     String key = headers.get(j);
                     if (!key.equals("UUID")) {
                         String value = "";
                         if (mongo.containsField(key)) {
                             value = "" + mongo.get(key);
                         }
                         data.data.add(value);
                         txt = txt.replaceAll("\\{\\{" + key + "\\}\\}", value);
                     }
                 }
             }
             else {
                 txt = "<ul>";
                 for (int j = 0; j < headers.size(); j++) {
                     String key = headers.get(j);
                     if (!key.equals("UUID")) {
                         if (mongo.containsField(key)) {
                             if (!key.equals(Geocoding.LATITUDE) && !key.equals(Geocoding.LONGITUDE)) {
                                 txt += "<li><strong>" + key + ":</strong>" + mongo.get(key) + "</li>";
                             }
                             data.data.add((String) mongo.get(key));
                         }
                         else {
                             data.data.add("");
                         }
                     }
                 }
             }
             data.text = txt.replaceAll("[\n\r]", "");
             datas.add(data);
         }
         return datas;
     }
 
     public static List<String> getDataHeader(String uuid) {
         List<String> headers = new ArrayList<String>();
 
         DBCollection dataColl = MongoDb.db().getCollection("carte-" + uuid);
         DBCursor cursor = dataColl.find();
 
         Integer i = 0;
         i++;
 
        if (cursor.hasNext()) {
             DBObject mongo = cursor.next();
             Iterator<String> iter = mongo.keySet().iterator();
             iter.next();
             while (iter.hasNext()) {
                 String key = iter.next();
                 if (!key.equals("UUID")) {
                    headers.add(key);
                 }
             }
         }
         return headers;
     }
 
     public static void deleteDataCarte(String uuid) {
         DBCollection dataColl = MongoDb.db().getCollection("carte-" + uuid);
         dataColl.drop();
     }
 }
