 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package fr.univnantes.atal.poubatal;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 /**
  *
  * @author sildar
  */
 public class DataManager {
 
     URL urlToFetch;
     List<CollectePoint> points;
 
     public DataManager() {
 
        
         String host = "cache.cites-u.univ-nantes.fr";
         //String host = "cache.wifi.univ-nantes.fr";
         System.getProperties().put("proxySet", "true");
         System.getProperties().put("proxyHost", host);
         System.getProperties().put("proxyPort", "3128");
         
         try {
             points = new ArrayList<CollectePoint>();
 
             String urlString = "http://data.nantes.fr/api/publication/"
                     + "JOURS_COLLECTE_DECHETS_VDN/JOURS_COLLECTE_DECHETS_VDN_STBL/content/"
                     + "?format=csv";
 
             urlToFetch = new URL(urlString);
 
             InputStreamReader in = new InputStreamReader(
                     urlToFetch.openConnection().getInputStream());
             BufferedReader buffer = new BufferedReader(in);
 
             //reads the header
             String nextLine = buffer.readLine();
             //reads first line
             nextLine = buffer.readLine();
 
             while (nextLine != null) {
                 //System.out.println(nextLine);
                 points.add(new CollectePoint(nextLine));
                 nextLine = buffer.readLine();
             }
 
         } catch (IOException ex) {
             Logger.getLogger(DataManager.class.getName()).log(Level.SEVERE, null, ex);
         }
     }
 
     public List<CollectePoint> getPoints() {
         return points;
     }
 
     public void updateData() {
         try {
             InputStreamReader in = new InputStreamReader(
                     urlToFetch.openConnection().getInputStream());
             BufferedReader buffer = new BufferedReader(in);
 
             //reads the header
             String nextLine = buffer.readLine();
             //reads first line
             nextLine = buffer.readLine();
 
             while (nextLine != null) {
                 //System.out.println(nextLine);
                 points.add(new CollectePoint(nextLine));
                 nextLine = buffer.readLine();
             }
 
         } catch (IOException ex) {
             Logger.getLogger(DataManager.class.getName()).log(Level.SEVERE, null, ex);
         }
     }
 }
