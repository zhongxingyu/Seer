 package eu.pinnoo.garbagecalendar.util.parsers;
 
 import eu.pinnoo.garbagecalendar.data.Collection;
 import eu.pinnoo.garbagecalendar.data.CollectionsData;
 import eu.pinnoo.garbagecalendar.data.LocalConstants;
 import eu.pinnoo.garbagecalendar.data.Sector;
 import eu.pinnoo.garbagecalendar.data.UserData;
 import java.text.ParseException;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 /**
  *
  * @author Wouter Pinnoo <pinnoo.wouter@gmail.com>
  */
 public class CalendarParser extends Parser {
 
     @Override
     protected String getURL() {
         return LocalConstants.CALENDAR_URL;
     }
 
     @Override
     protected String getJSONArrayName() {
         return "IVAGO-Inzamelkalender";
     }
 
     /**
      *
      * @param data
      * @return 0 when fetching was successful, otherwise 1
      */
     @Override
     protected int fetchData(JSONArray data) {
         if (data == null) {
             return 1;
         }
         CollectionsData.getInstance().resetCollections();
         String previousDate = "";
         for (int i = 0; i < data.length(); i++) {
             try {
                 JSONObject obj = data.getJSONObject(i);
                 Sector sector = new Sector(obj.getString("locatie"));
                 if (UserData.getInstance().getAddress().getSector().equals(sector)) {
                     if (obj.getString("datum").equals(previousDate)) {
                        CollectionsData.getInstance().getLastCollection().addTypes(parseGarbageType(obj.getString("fractie")));
                     } else {
                         Collection col = new Collection(obj.getString("week"), obj.getString("dag"), LocalConstants.DATE_FORMATTER.parse(obj.getString("datum")), parseGarbageType(obj.getString("fractie")), sector);
                         CollectionsData.getInstance().addCollection(col);
                     }
                     previousDate = obj.getString("datum");
                 }
             } catch (JSONException ex) {
                 Logger.getLogger(CalendarParser.class.getName()).log(Level.SEVERE, null, ex);
                 return 1;
             } catch (NullPointerException e) {
                 Logger.getLogger(CalendarParser.class.getName()).log(Level.SEVERE, null, e);
                 return 1;
             } catch (ParseException ex) {
                 Logger.getLogger(CalendarParser.class.getName()).log(Level.SEVERE, null, ex);
                 return 1;
             }
         }
         return 0;
     }
 }
