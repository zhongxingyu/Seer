 /**
  * class used to parse rss feed
  */
 package org.zoneproject.extractor.rssreader;
 
 /*
  * #%L
  * ZONE-RSSreader
  * %%
  * Copyright (C) 2012 ZONE-project
  * %%
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU Affero General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  * 
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU Affero General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  * #L%
  */
 
 import com.hp.hpl.jena.query.QuerySolution;
 import com.hp.hpl.jena.query.ResultSet;
 import com.sun.syndication.feed.synd.SyndContent;
 import com.sun.syndication.feed.synd.SyndEntry;
 import com.sun.syndication.feed.synd.SyndFeed;
 import com.sun.syndication.io.FeedException;
 import com.sun.syndication.io.SyndFeedInput;
 import com.sun.syndication.io.XmlReader;
 import java.io.File;
 import java.io.IOException;
 import java.net.URI;
 import java.net.URL;
 import java.net.UnknownHostException;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import org.zoneproject.extractor.utils.Database;
 import org.zoneproject.extractor.utils.Item;
 import org.zoneproject.extractor.utils.Prop;
 import org.zoneproject.extractor.utils.ZoneOntology;
 
 /**
  *
  * @author Desclaux Christophe <christophe@zouig.org>
  */
 public class RSSGetter {
     private static final org.apache.log4j.Logger  logger = org.apache.log4j.Logger.getLogger(RSSGetter.class);
     
     /**
      * transform a list of rss feeds links to a list of items
      * @param urls pointing to the rss feeds
      * @return the list of items
      */
     public static ArrayList<Item> getFlux(String[] urls) {
         ArrayList<Item> result = new ArrayList<Item>();
         for(int i=0; i < urls.length;i++){
             try{
                 XmlReader flux = new XmlReader(new URL(urls[i]));
                 result.addAll(RSSGetter.getFlux(urls[i],flux));
             } catch (java.net.UnknownHostException ex) {
                 logger.warn("RSS Feed "+urls[i]+" is offline");
             } catch (IOException ex) {
                 logger.warn("RSS Feed "+urls[i]+" is offline");
             }
         }
         return result;
     }
     
     /**
      * get the list of items contained in a rss feed link
      * @param url
      * @return the list of items
      */
     public static ArrayList<Item> getFlux(String url) {
         try {
             return RSSGetter.getFlux(url,new XmlReader(new URL(url)));
         } catch (Exception ex) {
             Logger.getLogger(RSSGetter.class.getName()).log(Level.SEVERE, null, ex);
         }
         return null;
     }
     
     /**
      * get the list of items contained in a rss feed file
      * @param file
      * @return 
      */
     public static ArrayList<Item> getFlux(File file) {
         try {
             return getFlux(file.getAbsoluteFile().toString(),new XmlReader(file));
         } catch (Exception ex) {
             Logger.getLogger(RSSGetter.class.getName()).log(Level.SEVERE, null, ex);
         }
         return null;
     }
     
     private static ArrayList<Item> getFlux(String source, XmlReader reader){
         ArrayList<Item> items = new ArrayList<Item>();
         
         SyndFeedInput sfi = new SyndFeedInput();
         try {
             SyndFeed feed = sfi.build(reader);
             List entries = new ArrayList();
             entries = feed.getEntries();
 
             for (int i = 0; i < entries.size(); i++){
                 SyndEntry entry = (SyndEntry)entries.get(i);
                 
                 //we clean bad Uris
                 String uri = entry.getLink();
                 uri = uri.replace("\t", "");
                 uri = uri.replace("\n", "");
                 while(uri.startsWith(" ")) {
                     uri = uri.substring(1);
                 }
                 while(uri.endsWith(" ")) {
                     uri = uri.substring(0,uri.length()-1);
                 }
                 //catch if the uri is local
                 if(uri.startsWith("/")){
                     uri = "http://"+URI.create(source).getHost()+""+uri;
                 }
                 
                 //create item
                 String description = "";
                 if(entry.getDescription() != null){
                     description = entry.getDescription().getValue();
                 }
                 Item cur = new Item(source, uri.toString(),entry.getTitle(),description,entry.getPublishedDate(),entry.getEnclosures());

                 //add item to list
                 items.add(cur);
             }
         }
         catch(NullPointerException e) {
             logger.warn("RSS Feed "+source+" not working"+e);
         }
         catch(IllegalArgumentException e) {
             logger.warn("RSS Feed "+source+" not working"+e);
         }
         catch(FeedException e) {
             logger.warn("RSS Feed "+source+" not working"+e);
         }
         return items;
     }
     
     public static String [] getSources(){
         String query = "SELECT *  WHERE {?uri rdf:type <"+ZoneOntology.SOURCES_TYPE+">.}";
         ResultSet res = Database.runSPARQLRequest(query, ZoneOntology.GRAPH_SOURCES);
         ArrayList<String> sources = new ArrayList<String>();
         while (res.hasNext()) {
             QuerySolution r = res.nextSolution();
             sources.add(r.get("?uri").toString());
         }
         return sources.toArray(new String[sources.size()]);
     }
     
     public static void main(String[] args){
         
         String fileURI = "http://europe1.fr.feedsportal.com/c/32376/f/546041/index.rss";
         ArrayList result = RSSGetter.getFlux(fileURI);
         logger.info(result);
     }
 }
