 package com.github.tntim96.jmonitaur.web;
 
 import com.github.tntim96.jmonitaur.model.Status;
 import com.github.tntim96.jmonitaur.service.JMonitaurService;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.ModelMap;
 import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
 
 import javax.servlet.http.HttpServletResponse;
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.util.Date;
import java.util.List;
 
 @Controller
 public class StatusController {
 
     private JMonitaurService service;
 
     @Autowired
     public StatusController(JMonitaurService service) {
         this.service = service;
     }
 
    public
    @ResponseBody
    @RequestMapping(value = "/statusJSON", produces = "application/json")
    List<Status> getStatusJSON() {
        return service.getStatues();
    }

     @RequestMapping(value = {"/status"})
    public String getStatusHtml(ModelMap model) {
         model.put("statuses", service.getStatues());
         return "status";
     }
 
     @RequestMapping(value = {"/statusSSE"})
     public String statusSSE(HttpServletResponse response) throws IOException {
         response.setContentType("text/event-stream");
         PrintWriter pw = response.getWriter();
         for (int i = 0; i < 10; i++) {
             try {
                 pw.print("event: status-update\n");
                 pw.print("data: " + new Date().toString() + "\n\n");
                 pw.flush();
                 Thread.sleep(1000);
             } catch (InterruptedException e) {
                 e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
             }
         }
         return null;
     }
 }
