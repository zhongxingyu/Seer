 package com.eli.index.manager;
 
 import com.eli.index.DocumentSupport;
 import com.eli.util.Config;
 import com.eli.util.TimeUtil;
 import org.apache.log4j.Logger;
 import org.apache.lucene.document.Document;
 import org.apache.lucene.search.Query;
 
 import java.io.File;
 import java.io.IOException;
import java.util.Date;
 
 /**
  * Created with IntelliJ IDEA.
  * User: shenchen
  * Date: 6/19/12
  * Time: 4:55 PM
  * To change this template use File | Settings | File Templates.
  */
 public enum ZhihuIndexManager {
     INSTANCE;
     private static final Logger logger = Logger.getLogger(ZhihuIndexManager.class);
 
     ZhihuIndex currentIndex;
 
     ZhihuIndexManager() {
         File files[] = Config.INDEX_DIR.listFiles();
         long maxVersion = 0;
         for(File f:files) {
             String name = f.getName();
             try {
                 long version = Long.parseLong(name);
                 if(version > maxVersion)
                     maxVersion = version;
             } catch (Exception e) {
             }
         }
         if(maxVersion != 0) {
             currentIndex = new ZhihuIndex(maxVersion);
        }  else {
            currentIndex = new ZhihuIndex((new Date()).getTime());
         }
     }
 
 
     public void commitIndex() {
         synchronized (this) {
             if(this.currentIndex != null)
                 this.currentIndex.flush();
         }
 
     }
 
 
 
 
 
     public void addDoc(DocumentSupport doc) {
         if(doc != null)
             this.addDoc(doc.toDocument());
     }
 
     public void addDoc(Document doc) {
         synchronized (this) {
             if(this.currentIndex != null)
                 this.currentIndex.addDocument(doc);
         }
     }
 
 
 
     public void delDoc(Query query) {
 
         synchronized (this) {
             if(this.currentIndex != null)
                 this.currentIndex.deleteDocument(query);
         }
     }
 
     public MultiNRTSearcherAgent acquire() {
         if(currentIndex == null) {
             logger.warn("no index ready for search");
             return null;
         }
 
         MultiNRTSearcherAgent agent = null;
 
         try {
             agent = currentIndex.getNrtManager().acquire();
         } catch (IOException e) {
             logger.error("error acquiring search agent", e);
             agent = null;
         }
         return agent;
     }
 
     public void release(MultiNRTSearcherAgent agent) {
         try {
             ZhihuNRTManager.release(agent);
         } catch (IOException e) {
             logger.error("error releasing search agent", e);
         }
     }
 
     public long currentIndexVersion() {
         if(currentIndex != null)
             return  currentIndex.getIndexVersion();
         return -1;
     }
 
 }
