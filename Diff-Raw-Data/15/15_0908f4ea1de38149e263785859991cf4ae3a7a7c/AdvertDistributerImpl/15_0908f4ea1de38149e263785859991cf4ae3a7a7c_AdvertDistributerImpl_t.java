 package com.gumtree.api.service.advertdistributer.impl;
 
 import com.gumtree.api.entity.Advert;
 import com.gumtree.api.service.advertdistributer.AdvertDistributer;
 import com.gumtree.api.service.apireader.impl.CApiAdvertsReader;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.beans.factory.annotation.Value;
 import org.springframework.stereotype.Service;
 
 import java.util.LinkedList;
 import java.util.List;
 
 /**
  * Created by IntelliJ IDEA.
  * User: markkelly
  * Date: 02/11/2011
  * Time: 20:41
  * To change this template use File | Settings | File Templates.
  */
 
 @Service
 public class AdvertDistributerImpl extends Thread implements AdvertDistributer{
 
     private static final Logger LOGGER = LoggerFactory.getLogger(AdvertDistributerImpl.class);
 
     @Value("${gumtree.adverts.drop.milliseconds:1000}")
     private long dropAdvertsMilliseconds;
 
     @Value("${gumtree.recentAdverts.stub:false}")
     private boolean useStubData;
 
     @Value("${gumtree.adverts.reload.number:30}")
     private int queueReload;
 
     @Autowired
     private CApiAdvertsReader capiAdvertsReader;
 
     private List<Advert> currentAdverts = new LinkedList<Advert>();
 
    private void startUp() {
 
         if(!useStubData) {
             currentAdverts = filter(capiAdvertsReader.getStartUpRecentAdverts());
 
         } else {
             LOGGER.info("Stub Data set so skip load up adverts on server startup");
         }
     }
 
     private void addAdverts() {
 
         List<Advert> recentAdverts = capiAdvertsReader.getMostRecentAdverts();
 
         //LOGGER.info("Adding Adverts - current size "+ currentAdverts.size() +" + "+ recentAdverts.size());
 
         currentAdverts.addAll(filter(recentAdverts));
     }
 
     private void dropAdvert() {
 
         //LOGGER.info("Dropping Adverts - current size " + currentAdverts.size());
 
         if(currentAdverts.size() > 0)
             currentAdverts.remove(0);
 
     }
 
     @Override
     public List<Advert> getCurrentAdverts() {
         return currentAdverts;
     }
 
     @Override
     public void run() {
 
        startUp();

         while(true &  !useStubData ) {
 
             dropAdvert();
 
             if(currentAdverts.size() <= queueReload)
                 addAdverts();
 
             try {
                 sleep(dropAdvertsMilliseconds);
             } catch (InterruptedException e){
                 LOGGER.error("Interrupted Exception caught " + e.getCause());
                 break;
             }
 
         }
     }
 
     //remove any adverts which do not fit the criteria to be displayed
     public static List<Advert> filter(List<Advert> allAds) {
         List<Advert> ads = new LinkedList<Advert>();
 
         for(int i = 0; i < allAds.size(); i++) {
 
             if(allAds.get(i).canDisplay())
                 ads.add(allAds.get(i));
         }
 
         return ads;
     }
 }
