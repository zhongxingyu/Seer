 package com.smartsms.controllers;
 
 
 import com.smartsms.beans.FilterMessage;
 import com.smartsms.beans.Malformed;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.RequestBody;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 
 @Controller
 public class FilterMessageController {
 
     @RequestMapping(value = "/filter", method = RequestMethod.POST)
    public String redirect(@RequestBody FilterMessage filterMessage){
 
         FilterMessage fm = filterMessage;
         String Message = fm.getMessage();
 
        String [] splittedArray = Message.split("\\W+");
 
         Malformed ml = new Malformed();
        for(int i=0 ; i<((splittedArray.length)+1) ; i++)
        {
            for(int r=0 ; r<((ml.getMalformedKeyword().size())+1) ; r++)
            {
                 splittedArray[i] = ml.getMalformedKeyword().get(r);
             }
         }
         return "filter";
     }
 }
