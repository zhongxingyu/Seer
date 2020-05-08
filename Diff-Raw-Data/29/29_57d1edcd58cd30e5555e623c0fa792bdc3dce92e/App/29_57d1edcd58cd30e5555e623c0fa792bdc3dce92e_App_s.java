 package org.zoneproject.extractor.plugin.extractarticlescontent;
 
 /*
  * #%L
  * ZONE-plugin-ExtractArticlesContent
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
 
 import de.l3s.boilerpipe.BoilerpipeProcessingException;
 import java.net.MalformedURLException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 import org.zoneproject.extractor.utils.Database;
 import org.zoneproject.extractor.utils.Item;
 import org.zoneproject.extractor.utils.Prop;
 import org.zoneproject.extractor.utils.VirtuosoDatabase;
 import org.zoneproject.extractor.utils.ZoneOntology;
 
 /**
  *
  * @author Desclaux Christophe <christophe@zouig.org>
  */
 public class App 
 {
     private static final org.apache.log4j.Logger  logger = org.apache.log4j.Logger.getLogger(App.class);
     public static String PLUGIN_URI = ZoneOntology.PLUGIN_EXTRACT_ARTICLES_CONTENT;
     public static String PLUGIN_RESULT_URI = ZoneOntology.PLUGIN_EXTRACT_ARTICLES_CONTENT_RES;
     public static int SIM_DOWNLOADS = 50;
     
     private static final String URL_REGEX = "\\(?\\b(http://|www[.])[-A-Za-z0-9+&@#/%?=~_()|!:,.;]*[-A-Za-z0-9+&@#/%=~_()|]";
     
     public App(){
         String [] tmp = {};
         App.main(tmp);
     }
      /**
      * @param args the command line arguments
      */
     public static void main(String[] args) {
         Item[] items;
         DownloadThread[] th;
         
         HashMap<String, ArrayList<Prop>> props; 
         while(true){
             do{
                 props = new HashMap<String, ArrayList<Prop>>();
                 items = VirtuosoDatabase.getItemsNotAnotatedForOnePlugin(PLUGIN_URI,SIM_DOWNLOADS);
                 th = new DownloadThread[items.length];
                 if(items == null){
                     continue;
                 }
                 logger.info("ExtractArticlesContent has "+items.length+" items to annotate");
                 Item curItem;
                 for(int curItemId = 0; curItemId < items.length ; curItemId++){
                     curItem = items[curItemId];
 
 
 
 
                     th[curItemId] = new DownloadThread(curItem,props);
                     th[curItemId].start();
                 }
 
                 for(int curItemId = 0; curItemId < items.length ; curItemId++){
                    try {
                         if(th[curItemId] == null)continue;
                        th[curItemId].join();
                    } catch (InterruptedException ex) {
                    logger.warn(ex);
                    }
                 }
                 Database.addAnnotations(props);
             }while(items == null || items.length > 0);
             logger.info("done");
             try{Thread.currentThread().sleep(1000);}catch(Exception ie){}
         }
     }
 }
 
 class DownloadThread extends Thread  {
    private Item item;
     private HashMap<String, ArrayList<Prop>> props;
     private static final org.apache.log4j.Logger  logger = org.apache.log4j.Logger.getLogger(App.class);
 
     public DownloadThread(Item item, HashMap<String, ArrayList<Prop>> props) {
       this.props = props;
       this.item = item;
     }
     public void run() {
         run(0);
     }
     public void run(int restartLevel) {
         try {
             logger.info("Add ExtractArticlesContent for item: "+item.getUri());
 
             String content = ExtractArticleContent.getContent(item);
 
             props.put(item.getUri(), new ArrayList<Prop>());
             props.get(item.getUri()).add(new Prop(App.PLUGIN_URI,"true"));
             if(content != null){
                 props.get(item.getUri()).add(new Prop(App.PLUGIN_RESULT_URI,content));
             }
         } catch (BoilerpipeProcessingException ex) {
             logger.warn("annotation process because of download error for "+item.getUri());
         } catch (MalformedURLException ex) {
             logger.warn("annotation process because of malformed Uri for "+item.getUri());
         } catch (java.io.IOException ex) {
             logger.warn("annotation process because of download error for "+item.getUri());
         }catch (com.hp.hpl.jena.shared.JenaException ex){
             logger.warn("annotation process because of virtuoso partial error "+item.getUri());
         }finally{
             VirtuosoDatabase.addAnnotation(item.getUri(), new Prop(App.PLUGIN_URI,"true"));
         }
     }
 }
