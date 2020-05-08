 package org.inigma.shared.webapp;
 
import javax.servlet.http.Cookie;
 import javax.servlet.http.HttpServletRequest;
 
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.PathVariable;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.ResponseBody;
 
 @Controller
 public class ErrorPageController {
     private static class ErrorPageResponse {
         private int code;
        private HttpServletRequest req;
 
        public ErrorPageResponse(int code, HttpServletRequest req) {
             this.code = code;
            this.req = req;
         }
 
         public int getCode() {
             return code;
         }
 
        public String getContextPath() {
            return req.getContextPath();
        }

        public Cookie[] getCookies() {
            return req.getCookies();
        }

        public String getMethod() {
            return req.getMethod();
        }
        
        public String getUri() {
            return (String) req.getAttribute("javax.servlet.forward.request_uri");
         }
     }
 
     @RequestMapping("/error/page/{code}")
     @ResponseBody
     public ErrorPageResponse handleErrorPage(@PathVariable int code, HttpServletRequest req) {
        return new ErrorPageResponse(code, req);
     }
 }
