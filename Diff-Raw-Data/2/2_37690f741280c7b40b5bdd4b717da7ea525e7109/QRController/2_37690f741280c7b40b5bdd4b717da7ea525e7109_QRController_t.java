 /*
  * The PID webservice offers SOAP methods to manage the Handle System(r) resolution technology.
  *
  * Copyright (C) 2010-2012, International Institute of Social History
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program. If not, see <http://www.gnu.org/licenses/>.
  */
 
 package org.socialhistoryservices.pid.controllers;
 
 import org.socialhistoryservices.pid.service.QRService;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.PathVariable;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.RequestParam;
 import org.springframework.web.multipart.MultipartFile;
 
 import javax.servlet.http.HttpServletResponse;
 import java.io.IOException;
 
 @Controller
 public class QRController {
 
     private QRService qrService;
 
     @RequestMapping("/qr/{na}/{pid}")
     public void encodeimage(@PathVariable("na") String na,
                             @PathVariable("pid") String id,
                             @RequestParam(value = "locatt", required = false) String locAtt,
                             @RequestParam(value = "r", required = false, defaultValue = "http://hdl.handle.net/") String handleResolverBaseUrl,
                             HttpServletResponse response) throws Exception {
 
         byte[] image;
         if (!handleResolverBaseUrl.endsWith("/")) handleResolverBaseUrl += "/";
         image = qrService.encode(handleResolverBaseUrl, na + "/" + id, locAtt);
         if (image == null) {
             image = qrService.qr404image();
             response.setStatus(404);
         }
         response.setContentType("image/png");
         response.setContentLength(image.length);
         response.getOutputStream().write(image);
     }
 
     @RequestMapping(value = "/qr", method = RequestMethod.POST)
     public String handleFormUpload(@RequestParam("image") MultipartFile file, HttpServletResponse response) throws IOException {
 
         String url = null;
         if (!file.isEmpty()) {
             try {
                 url = qrService.decode(file.getInputStream());
             } catch (Exception e) {
                // Do not do a thing
             }
         }
         if (url == null) {
             response.setStatus(404);
             return "fnf400";
         } else {
             response.setStatus(301);
             response.setHeader("Location", url);
             response.setHeader("Connection", "close");
             return null;
         }
     }
 
 
     public void setQrService(QRService qrService) {
         this.qrService = qrService;
     }
 }
