 package com.cqlybest.admin.controller;
 
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.Model;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.RequestParam;
 
 import com.cqlybest.common.service.SmsService;
 
 @Controller
 public class SmsController {
 
   @Autowired
   private SmsService smsService;
 
   @RequestMapping(value = "/sms/list.html", method = RequestMethod.GET)
   public void list(@RequestParam(defaultValue = "0") int page, Model model) {
     page = Math.max(1, page);
     int pageSize = 20;
     model.addAttribute("page", page);
     model.addAttribute("pageSize", pageSize);
     model.addAttribute("total", smsService.querySmsTotal());
     model.addAttribute("smss", smsService.querySmss(page, pageSize));
   }
 
   @RequestMapping(value = "/sms/send.html", method = RequestMethod.POST)
   public void send(@RequestParam String phone, @RequestParam String content, Model model) {
    smsService.send(phone, content);
   }
 
 
 }
