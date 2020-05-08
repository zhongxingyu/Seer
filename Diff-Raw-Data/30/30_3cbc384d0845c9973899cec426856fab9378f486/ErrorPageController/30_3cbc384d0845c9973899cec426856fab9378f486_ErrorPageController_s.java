 package org.inigma.shared.webapp;
 
 import javax.servlet.http.HttpServletRequest;
 
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.PathVariable;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.ResponseBody;
 
 @Controller
 public class ErrorPageController {
     private static class ErrorPageResponse {
         private int code;
        private String message;
 
        public ErrorPageResponse(int code, String msg) {
             this.code = code;
            this.message = msg;
         }
 
         public int getCode() {
             return code;
         }
 
        public String getMessage() {
            return message;
         }
     }
 
     @RequestMapping("/error/page/{code}")
     @ResponseBody
     public ErrorPageResponse handleErrorPage(@PathVariable int code, HttpServletRequest req) {
        return new ErrorPageResponse(code, req.toString());
     }
 }
