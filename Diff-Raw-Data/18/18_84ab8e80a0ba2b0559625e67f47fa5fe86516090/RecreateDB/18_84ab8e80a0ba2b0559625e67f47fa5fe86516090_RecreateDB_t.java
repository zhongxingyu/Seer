 package org.motechproject.ghana.mtn.tools;
 
 import org.apache.log4j.Logger;
import org.quartz.SchedulerException;
 import org.springframework.context.ApplicationContext;
 import org.springframework.context.support.ClassPathXmlApplicationContext;
 
 public class RecreateDB {
     static Logger log = Logger.getLogger(RecreateDB.class);
     public static final String APPLICATION_CONTEXT_XML = "/seed/applicationContext-tools.xml";
 
    public static void main(String[] args) throws SchedulerException {
         ApplicationContext context = new ClassPathXmlApplicationContext(APPLICATION_CONTEXT_XML);
         CouchDB couchDB = context.getBean(CouchDB.class);
         couchDB.recreate();
        ((ClassPathXmlApplicationContext) context).close();
     }
 }
