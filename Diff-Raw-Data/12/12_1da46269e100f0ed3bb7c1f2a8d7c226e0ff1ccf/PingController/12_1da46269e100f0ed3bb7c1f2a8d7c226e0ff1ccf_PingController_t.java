 package org.motechproject.care.reporting.web.controller;
 
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.ResponseBody;
 
 @Controller
 @RequestMapping("/web-api")
 public class PingController {
 
    @RequestMapping(value = "/ping-care-reporting-bundle", method = RequestMethod.GET)
     @ResponseBody
     public String pingPage() {
        return "CareReportingBundle Ping Page";
     }
 }
