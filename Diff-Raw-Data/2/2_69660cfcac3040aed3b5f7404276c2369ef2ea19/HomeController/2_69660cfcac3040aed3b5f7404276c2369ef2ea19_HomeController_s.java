 package org.urlMonitor.controller;
 
 import java.util.List;
 
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.ModelMap;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.RequestParam;
 import org.springframework.web.bind.annotation.ResponseBody;
 import org.urlMonitor.model.Monitor;
 import org.urlMonitor.service.UrlMonitorService;
 
 @Controller
 public class HomeController
 {
    @Autowired
    private UrlMonitorService urlMonitorService;
 
   @RequestMapping(value = "/", method = RequestMethod.GET)
    public String GetIndexPage(ModelMap model)
    {
       model.addAttribute("monitorList", urlMonitorService.getMonitorList());
       return "index";
    }
 
    @RequestMapping(value = "/updateStatus", method = RequestMethod.GET)
    public @ResponseBody
    List<Monitor> refreshPage()
    {
       return urlMonitorService.getMonitorList();
    }
 
    @RequestMapping(value = "/filterList", method = RequestMethod.GET)
    public String filterList(@RequestParam String filterText, ModelMap model)
    {
       model.addAttribute("monitorList", urlMonitorService.getMonitorList(filterText));
       return "view/content";
    }
 }
