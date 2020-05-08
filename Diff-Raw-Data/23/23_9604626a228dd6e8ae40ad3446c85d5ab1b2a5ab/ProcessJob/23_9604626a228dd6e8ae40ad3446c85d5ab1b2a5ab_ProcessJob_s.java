 package com.hoiio.cron;
 
 import java.util.List;
 
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.scheduling.annotation.Scheduled;
 
 import com.hoiio.controller.httpTerminator.ApiRequest;
 import com.hoiio.controller.httpTerminator.ApiUrls;
 import com.hoiio.controller.httpTerminator.HttpUtil;
 import com.hoiio.model.Job;
 import com.hoiio.redis.api.JobDAO;
 
 public class ProcessJob {
     @Autowired
     JobDAO jobDAO;
 
     @Scheduled(fixedDelay = 1000)
     public void process() {
         System.out.println("Running job");
         LoggerFactory.getLogger(ProcessJob.class).debug("Test");
         List<String> users = jobDAO.getUsers();
         for (String user : users ) {
             long len = jobDAO.len(user);
             for (int i = 0; i < len; i++) {
                 Job job = jobDAO.getJob(user);
                 ApiRequest request = new ApiRequest(ApiUrls.SEND_SMS);
                 request.addParam("app_id", "YALI5o4R6mIQLd7v");
                 request.addParam("access_token", "3jO5gTIPcjht88BR");
                 request.addParam("dest", job.getTo());
                 request.addParam("msg", "Test scheduled SMS");
                 System.out.println(HttpUtil.doHttpPost(request));
             }
         }
     }
 }
