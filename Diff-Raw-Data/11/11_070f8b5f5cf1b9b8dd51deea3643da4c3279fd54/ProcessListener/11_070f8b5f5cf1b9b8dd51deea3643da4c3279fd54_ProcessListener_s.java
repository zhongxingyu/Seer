 package com.ssm.llp.biz.event;
 
import org.activiti.engine.*;
 import org.apache.log4j.Logger;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.context.ApplicationListener;
 
 import java.util.Map;
 
 /**
  * @author rafizan.baharum
  * @since 9/15/13
  */
 public class ProcessListener implements ApplicationListener<ProcessEvent> {
 
     private static final Logger log = Logger.getLogger(ProcessListener.class);
 
     @Override
     public void onApplicationEvent(ProcessEvent event) {
     }
 }
