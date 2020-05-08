 package com.reversemind.glia.simple;
 
 import com.reversemind.glia.simple.shared.ISimpleEJB;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
 
 import javax.annotation.PostConstruct;
 import javax.ejb.Local;
 import javax.ejb.Startup;
 import javax.ejb.Stateless;
 import java.io.Serializable;
 
 /**
 *
  */
 @Stateless
 @Startup
 @Local(ISimpleEJB.class)
 public class SimpleEJB implements ISimpleEJB, Serializable {
 
    private static final Logger LOG = LoggerFactory.getLogger(SimpleEJB.class);

     @PostConstruct
     public void init(){
        LOG.info("Initialized");
     }
 
     public String getResult(String value){
         return "added value:" + value;
     }
 
 }
