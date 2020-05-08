 package org.zoneproject.extractor.plugin.extractarticlescontent;
 
 import de.l3s.boilerpipe.BoilerpipeProcessingException;
 import java.net.MalformedURLException;
 import org.zoneproject.extractor.utils.Item;
 import org.zoneproject.extractor.utils.Prop;
 import org.zoneproject.extractor.utils.VirtuosoDatabase;
 
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
 public class DownloadThread extends Thread  {
   private Item item;
   private static final org.apache.log4j.Logger  logger = org.apache.log4j.Logger.getLogger(App.class);
 
   public DownloadThread(Item item) {
     this.item = item;
   }
   public void run() {
       run(0);
   }
   public void run(int restartLevel) {
       try {
           logger.info("Add ExtractArticlesContent for item: "+item.getUri());
 
           String content = ExtractArticleContent.getContent(item);
 
           VirtuosoDatabase.addAnnotation(item.getUri(), new Prop(App.PLUGIN_URI,"true"));
          if(content != null) VirtuosoDatabase.addAnnotation(item.getUri(), new Prop(App.PLUGIN_RESULT_URI,content));
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
