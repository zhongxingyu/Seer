 package org.icemobile.samples.springbasic;
 
 import org.springframework.web.bind.annotation.ModelAttribute;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 
 import javax.servlet.http.HttpServletRequest;
 
 import org.springframework.stereotype.Controller;
 
 /**
  *
  */
 @Controller
 public class QRScanController {
 
     @RequestMapping(value = "/qrscan", method = RequestMethod.GET)
     public void variousGet(HttpServletRequest request, QRScanBean model) {
     }
 
     @RequestMapping(value = "/qrscan", method = RequestMethod.POST)
     public void variousPost(HttpServletRequest request,
                             QRScanBean model) {
     }
 
    @ModelAttribute("QRScanBean")
     public QRScanBean createBean() {
         return new QRScanBean();
     }
 }
 
