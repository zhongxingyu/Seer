 /* vim: set ts=4 sw=4 et: */
 
 package org.gitorious.scrapfilbleu.android;
 
 import java.io.IOException;
 import java.net.SocketTimeoutException;
 import java.util.Map;
 import java.util.ArrayList;
 import java.util.Iterator;
 
 import android.util.Log;
 
 import android.os.AsyncTask;
 
 import org.jsoup.Jsoup;
 import org.jsoup.Connection.Response;
 import org.jsoup.Connection.Method;
 import org.jsoup.nodes.Document;
 import org.jsoup.nodes.Element;
 import org.jsoup.select.Elements;
 
 public class BusJourney {
     private String cityDep;
     private String cityArr;
     private String stopDep;
     private String stopArr;
     private String date;
     private String hour;
     private String minute;
     private String sens;
     private String criteria;
 
     private String urlbase;
     private String urlraz;
 
     private Map<String, String> cookies;
     private ArrayList<Journey> journeys;
 
     public BusJourney() {
         this.urlraz = new URLs("id=1-1").getURLraz();
         this.urlbase = new URLs("id=1-1&etape=1").getURL();
         this.journeys = new ArrayList<Journey>();
     }
 
     public ArrayList<Journey> getBusJourneys(BusToursActivity.ProcessScrapping parent) throws java.io.IOException, java.net.SocketTimeoutException, ScrappingException {
         String dep = this.cityDep + " - " + this.stopDep;
         String arr = this.cityArr  + " - " + this.stopArr;
 
         Log.e("BusTours:BusJourney", "RAZ=" + this.urlraz);
         Log.e("BusTours:BusJourney", "URL=" + this.urlbase);
 
         parent.progress(20, R.string.jsoupAskRaz);
 
         Log.e("BusTours:BusJourney", "dep='" + dep + "'");
         Log.e("BusTours:BusJourney", "arr='" + arr + "'");
 
         Response res = Jsoup.connect(this.urlraz).method(Method.GET).execute();
         Log.e("BusTours:BusJourney", "Got RAZ.");
 
         parent.progress(30, R.string.jsoupGotRaz);
 
         this.cookies = res.cookies();
 
         Document reply = Jsoup.connect(this.urlbase)
             .cookies(this.cookies)
             .data("Departure", dep)
             .data("Arrival", arr)
             .data("Sens", this.sens)
             .data("Date", this.date)
             .data("Hour", this.hour)
             .data("Minute", this.minute)
             .data("Criteria", this.criteria)
             .post();
         Log.e("BusTours:BusJourney", "Posted form.");
 
         parent.progress(40, R.string.jsoupPostedForm);
 
         Elements navig = reply.getElementsByAttributeValue("class", "navig");
         Log.e("BusTours:BusJourney", "Retrieved elements.");
         if (navig.isEmpty()) {
             Log.e("BusTours:BusJourney", "NO Navig !!!");
             Log.e("BusTours:BusJourney", "BODY::" + reply.body().html());
             throw new ScrappingException("Not a result page");
         }
 
         parent.progress(50, R.string.jsoupGotNavig);
 
         Elements table = reply.getElementsByAttributeValue("summary", "Propositions");
         if (table.isEmpty()) {
             Log.e("BusTours:BusJourney", "NO Table !!!");
             Log.e("BusTours:BusJourney", "navig::" + navig.html());
             throw new ScrappingException("Missing table");
         }
 
         parent.progress(55, R.string.jsoupGotPropositions);
 
         Elements trips = table.first().getElementsByTag("tr");
         if (trips.isEmpty()) {
             Log.e("BusTours:BusJourney", "NO Trips !!!");
             Log.e("BusTours:BusJourney", "table::" + table.html());
             throw new ScrappingException("No journey");
         }
 
         parent.progress(60, R.string.jsoupGotJourneys);
 
         Iterator<Element> it = trips.iterator();
         // bypass first element, table heading
         it.next();
         int iProgress = 60;
         while (it.hasNext()) {
             parent.progress(iProgress, R.string.jsoupGotTrip);
             this.journeys.add(new Journey(it.next(), this.cookies));
             iProgress += 10;
         }
 
         return this.journeys;
     }
 
 //    public void getBusJourneysDetails() {
 //        Iterator<Journey> jit = this.journeys.iterator();
 //        while (jit.hasNext()) {
 //            jit.next().getDetails();
 //        }
 //    }
 
     public String pruneAccents(String s) {
         s = s.replaceAll("[èéêë]","e");
         s = s.replaceAll("[ûù]","u");
         s = s.replaceAll("[ïî]","i");
         s = s.replaceAll("[àâ]","a");
        s = s.replaceAll("ô","o");
         s = s.replaceAll("[ÈÉÊË]","E");
         s = s.replaceAll("[ÛÙ]","U");
         s = s.replaceAll("[ÏÎ]","I");
         s = s.replaceAll("[ÀÂ]","A");
         s = s.replaceAll("Ô","O");
         return s;
     }
 
     public void setCityDep(String v) {
         this.cityDep = this.pruneAccents(v);
     }
 
     public void setCityArr(String v) {
         this.cityArr = this.pruneAccents(v);
     }
 
     public void setStopDep(String v) {
         this.stopDep = this.pruneAccents(v);
     }
 
     public void setStopArr(String v) {
         this.stopArr = this.pruneAccents(v);
     }
 
     public void setDate(String v) {
         this.date = v;
     }
 
     public void setHour(String v) {
         this.hour = v;
     }
 
     public void setMinute(String v) {
         this.minute = v;
     }
 
     public void setSens(String v) {
         this.sens = v;
     }
 
     public void setCriteria(String v) {
         this.criteria = v;
     }
 }
