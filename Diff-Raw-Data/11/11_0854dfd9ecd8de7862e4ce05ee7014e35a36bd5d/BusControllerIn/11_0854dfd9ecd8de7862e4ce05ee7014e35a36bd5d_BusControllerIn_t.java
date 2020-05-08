 /**
  * Apache License 2.0
  */
 package com.googlecode.madschuelerturnier.business.bus;
 
 import com.googlecode.madschuelerturnier.business.Business;
import com.googlecode.madschuelerturnier.model.DBAuthUser;
 import com.googlecode.madschuelerturnier.model.integration.IncommingMessage;
 import com.googlecode.madschuelerturnier.model.Mannschaft;
 import com.googlecode.madschuelerturnier.model.integration.StartFile;
import com.googlecode.madschuelerturnier.persistence.repository.DBAuthUserRepository;
 import com.googlecode.madschuelerturnier.persistence.repository.MannschaftRepository;
 import org.apache.log4j.Logger;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.context.ApplicationListener;
 import org.springframework.stereotype.Controller;
 
 import java.io.Serializable;
 
 /**
  * Produziert Events, die in angeschlossene Remote Contexte gesendet werden. Ebenfalls werden
  * ankommende Events verarbeitet.
  *
  * @author $Author: marthaler.worb@gmail.com $
  * @since 1.2.8
  */
 @Controller
 public class BusControllerIn implements ApplicationListener<IncommingMessage> {
 
     private static final Logger LOG = Logger.getLogger(BusControllerIn.class);
 
     @Autowired
     private MannschaftRepository repo;
 
     @Autowired
    private DBAuthUserRepository authUserRepository;

    @Autowired
     private Business business;
 
     @Override
     public void onApplicationEvent(IncommingMessage event) {
         Serializable obj = event.getPayload();
         LOG.info("BusControllerIn: message von anderem remote kontext angekommen: " + obj);
         if(obj instanceof Mannschaft){
             repo.save((Mannschaft) obj);
         }
         if(obj instanceof StartFile){
             business.generateSpielFromXLS(((StartFile) obj).getContent());
         }
        if(obj instanceof DBAuthUser){
            authUserRepository.save((DBAuthUser) obj);
        }
 
     }
 }
