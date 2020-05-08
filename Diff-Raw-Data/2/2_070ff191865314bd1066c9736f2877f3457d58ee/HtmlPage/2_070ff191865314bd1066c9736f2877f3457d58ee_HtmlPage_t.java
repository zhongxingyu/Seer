 /*
  *  Copyright 2010 mathieuancelin.
  * 
  *  Licensed under the Apache License, Version 2.0 (the "License");
  *  you may not use this file except in compliance with the License.
  *  You may obtain a copy of the License at
  * 
  *       http://www.apache.org/licenses/LICENSE-2.0
  * 
  *  Unless required by applicable law or agreed to in writing, software
  *  distributed under the License is distributed on an "AS IS" BASIS,
  *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  *  See the License for the specific language governing permissions and
  *  limitations under the License.
  *  under the License.
  */
 package cx.ath.mancel01.webframework.view;
 
 import cx.ath.mancel01.webframework.WebFramework;
 import cx.ath.mancel01.webframework.http.Response;
 import cx.ath.mancel01.webframework.http.StatusCodes;
 import java.io.ByteArrayOutputStream;
 
 /**
  *
  * @author mathieuancelin
  */
 public class HtmlPage extends Page {
     
     private final String html;
     private final String title;
 
    public HtmlPage(String title, String html) {
         this.html = html;
         this.title = title;
     }
 
     @Override
     public Response render() {
         long start = System.currentTimeMillis();
         Response res = new Response();
         res.out = new ByteArrayOutputStream();
         res.contentType = this.getContentType();
         String message = this.getMessage();
         res.out.write(message.getBytes(), 0, message.length());
         WebFramework.logger.trace("html page rendering : {} ms.", (System.currentTimeMillis() - start));
         return res;
     }
 
     @Override
     public int getStatusCode() {
         return StatusCodes.OK;
     }
 
     @Override
     public String getContentType() {
         return "text/html";
     }
 
     @Override
     public String getMessage() {
         return "<html><head><title>" + title + "</title></head>"
                         + "<body>" + html + "</body></html>";
     }
 }
