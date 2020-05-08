 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package com.abada.esper.listener;
 
 import ca.uhn.hl7v2.model.Message;
 import com.abada.eva.api.Action;
 import com.espertech.esper.client.EventBean;
 import com.espertech.esper.client.UpdateListener;
 import java.io.InputStream;
 import java.util.Map;
 import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
 import org.springframework.context.support.GenericApplicationContext;
 import org.springframework.core.io.InputStreamResource;
 import org.springframework.core.io.Resource;
 
 /**
  *
  * @author katsu
  */
 public class EsperListener extends GenericApplicationContext implements UpdateListener{
     /**
      * Bean definition reader for XML bean definitions
      */
     private final XmlBeanDefinitionReader reader = new XmlBeanDefinitionReader(this);
     /**
      * Spring xml where is all the context configuration
      */
     private InputStreamResource xmlStreamConfig;
     private Map<String,Action> actions;        
 
     public EsperListener(InputStream is) {
         super();
 
         xmlStreamConfig = new InputStreamResource(is);        
 
         reader.setValidationMode(XmlBeanDefinitionReader.VALIDATION_XSD);
 
         load(xmlStreamConfig);
         refresh();
         //Find Action
         this.actions=this.getBeansOfType(Action.class);
     }
     
     public void update(EventBean[] newEvents, EventBean[] oldEvents) {        
         if (actions!=null && !actions.isEmpty()){
             Message [] old=create(oldEvents);
             Message [] newe=create(newEvents);
             
             for (Action a:actions.values()){
                 try{
                     a.doIt(old, newe);
                 }catch (Exception e){
                     logger.error("Error executing action.", e);
                 }
             }
         }
     }
 
     private Message[] create(EventBean[] events) {
         if (events==null) {
             return null;
         }
         Message [] result=new Message[events.length];
         for (int i=0;i<events.length;i++){
            result[i]=(Message)events[i].getUnderlying();
         }
         return result;
     }
     
     /**
      * Load bean definitions from the given XML resources.
      *
      * @param resources one or more resources to load from
      */
     private void load(Resource... resources) {
         this.reader.loadBeanDefinitions(resources);
     }
     
 }
