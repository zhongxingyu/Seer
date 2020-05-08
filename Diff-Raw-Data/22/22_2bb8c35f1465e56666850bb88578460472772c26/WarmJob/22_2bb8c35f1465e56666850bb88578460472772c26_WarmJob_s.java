 package com.abudko.reseller.huuto.task.warm;
 
 import org.quartz.Job;
 import org.quartz.JobExecutionContext;
 import org.quartz.JobExecutionException;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Component;
 import org.springframework.web.client.RestTemplate;
 
 @Component
 public class WarmJob implements Job {
 
     private static final String URI = "http://reseller.alexbud.cloudbees.net/items/search";
    
    private final Logger log = LoggerFactory.getLogger(getClass());
 
    @Autowired
    private RestTemplate restTemplate;
 
     public void execute(JobExecutionContext context) throws JobExecutionException {
         log.info("Warming: " + URI);
        
        restTemplate.getForObject(URI, String.class);
     }
 
 }
