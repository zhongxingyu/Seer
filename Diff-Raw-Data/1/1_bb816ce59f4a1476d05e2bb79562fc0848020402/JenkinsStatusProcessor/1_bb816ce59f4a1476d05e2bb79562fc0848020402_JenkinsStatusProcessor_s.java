 package com.delineneo.processor;
 
import org.codehaus.jackson.map.ObjectMapper;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.scheduling.annotation.Scheduled;
 import org.springframework.stereotype.Component;
 import org.springframework.web.client.RestTemplate;
 
 /**
  * Created by IntelliJ IDEA.
  * User: deline
  * Date: 2/03/12
  * Time: 8:33 PM
  * To change this template use File | Settings | File Templates.
  */
 @Component
 public class JenkinsStatusProcessor {
     private static final String JENKINS_URL = "http://localhost:8080/job/JenkinsStatus/api/json" ;
     private RestTemplate restTemplate;
 
     @Scheduled(fixedDelay=5000)
     public void process() {
 
         String jsonString = restTemplate.getForObject(JENKINS_URL, String.class);
 
         System.out.println(">>>>>>>>> in the process method");
     }
 
     @Autowired
     public void setRestTemplate(RestTemplate restTemplate) {
         this.restTemplate = restTemplate;
     }
 }
