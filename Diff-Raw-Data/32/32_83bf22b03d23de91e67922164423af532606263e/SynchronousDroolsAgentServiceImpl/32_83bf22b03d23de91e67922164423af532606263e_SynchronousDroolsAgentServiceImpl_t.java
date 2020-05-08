 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package org.drools.dssagentserver;
 
 
 import java.util.List;
 import javax.jws.WebService;
 import javax.xml.bind.annotation.XmlSeeAlso;
 
 
 
 
 import org.drools.fipa.ACLMessage;
 import org.drools.fipa.DroolsAgent;
 import org.drools.fipa.body.acts.Agree;
 import org.drools.fipa.body.acts.Failure;
 import org.drools.fipa.body.acts.Inform;
 import org.drools.fipa.body.acts.InformIf;
 import org.drools.fipa.body.acts.InformRef;
 import org.drools.fipa.body.acts.QueryIf;
 import org.drools.fipa.body.acts.QueryRef;
 import org.drools.fipa.body.acts.Request;
 import org.drools.fipa.body.acts.RequestWhen;
 import org.drools.fipa.body.content.Action;
 import org.drools.fipa.body.content.Query;
 import org.drools.fipa.body.content.Ref;
 import org.drools.fipa.body.content.Rule;
 import org.drools.fipa.mappers.MyMapArgsEntryType;
 import org.drools.fipa.mappers.MyMapReferenceEntryType;
 
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.context.ApplicationContext;
 import org.springframework.context.support.ClassPathXmlApplicationContext;
 
 /**
  * @author salaboy
  * @author esteban
  */
 
 @WebService()
 @XmlSeeAlso(value={Inform.class, QueryIf.class, InformIf.class, 
                     Agree.class, Failure.class, Action.class, Rule.class, 
                     QueryRef.class, Query.class, 
                     Ref.class, InformRef.class, Request.class, RequestWhen.class,
                     MyMapReferenceEntryType.class, MyMapArgsEntryType.class})
 public class SynchronousDroolsAgentServiceImpl implements SynchronousDroolsAgentService {
     private static Logger logger = LoggerFactory.getLogger(SynchronousDroolsAgentServiceImpl.class);
     private DroolsAgent agent;
     private SynchronousDroolsAgentResponseInformer responseInformer;
 
     public SynchronousDroolsAgentServiceImpl() {
         ApplicationContext ctx = new ClassPathXmlApplicationContext("dss-agents.xml");
         this.agent =  (DroolsAgent) ctx.getBean("agent");
         this.responseInformer = (SynchronousDroolsAgentResponseInformer) ctx.getBean("syncResponseInformer");
     }
 
     
     @Override
     public List<ACLMessage> tell(ACLMessage message) {
         logger.info(" >>> IN Message -> "+message.getPerformative().name());
        System.out.println(">>> IN Message -> "+message.getPerformative().name());
         try {
             agent.tell(message);
         } catch (Throwable t) {
            System.out.println(">>>>>>>>>>>>> exception => "+t.getMessage());
         }
         List<ACLMessage> retrieveResponses = responseInformer.retrieveResponses(message);
        if(retrieveResponses != null ){
            System.out.println(" >>> Number of OUT Messages -> "+retrieveResponses.size());
            logger.info(" >>> Number of OUT Messages -> "+retrieveResponses.size());
            for(ACLMessage outMessage : retrieveResponses){
                logger.info(" >>> OUT Message -> "+outMessage.getPerformative().name());
                System.out.println(" >>> OUT Message -> "+outMessage.getPerformative().name());
            }
        }else{
            System.out.println(" >>> 0 OUT Messages");
         }
         return retrieveResponses;
     }
 
 
 }
