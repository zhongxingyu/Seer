 /* 
  * Copyright (c) 1998-2012 Citrix Online LLC
  * All Rights Reserved Worldwide.
  *
  * THIS PROGRAM IS CONFIDENTIAL AND PROPRIETARY TO CITRIX ONLINE
  * AND CONSTITUTES A VALUABLE TRADE SECRET.  Any unauthorized use,
  * reproduction, modification, or disclosure of this program is
  * strictly prohibited.  Any use of this program by an authorized
  * licensee is strictly subject to the terms and conditions,
  * including confidentiality obligations, set forth in the applicable
  * License and Co-Branding Agreement between Citrix Online LLC and
  * the licensee.
  */
 
 package spr9209;
 
 import java.io.IOException;
 import javax.servlet.http.HttpServletResponse;
 
 import org.springframework.http.HttpStatus;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.ExceptionHandler;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.ResponseBody;
 import org.springframework.web.bind.annotation.ResponseStatus;
 
 @Controller
 public class WebController {
 
     @RequestMapping("/request1")
     public void handleRequest1() {
         throw new IllegalArgumentException();
     }
 
     @RequestMapping("/request2")
     public void handleRequest2() {
         throw new NullPointerException();
     }
 
     @RequestMapping("/ping")
     @ResponseBody
     public String ping() {
         return "pong";
     }
 
     @ExceptionHandler(IllegalArgumentException.class)
     @ResponseStatus(HttpStatus.BAD_REQUEST)
     public void iaeHandler(HttpServletResponse response) {
         try {
            response.getWriter().println("Handling NullPointerException");
         } catch (IOException e) {
             //ignore
         }
     }
 }
