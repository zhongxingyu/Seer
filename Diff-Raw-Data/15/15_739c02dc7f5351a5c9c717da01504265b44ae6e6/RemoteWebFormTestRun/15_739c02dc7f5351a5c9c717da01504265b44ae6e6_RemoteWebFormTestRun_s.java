 /*
  * The contents of this file are subject to the Mozilla Public
  * License Version 1.1 (the "License"); you may not use this file
  * except in compliance with the License. You may obtain a copy of
  * the License at http://www.mozilla.org/MPL/
  *
  * Software distributed under the License is distributed on an "AS
  * IS" basis, WITHOUT WARRANTY OF ANY KIND, either express or
  * implied. See the License for the specific language governing
  * rights and limitations under the License.
  *
  * The Original Code is Web Questionnaires 2
  *
  * The Initial Owner of the Original Code is European Environment
  * Agency. Portions created by TripleDev are Copyright
  * (C) European Environment Agency.  All Rights Reserved.
  *
  * Contributor(s):
  *        Anton Dmitrijev
  */
 package eionet.webq.web.controller;
 
import eionet.webq.dao.orm.UserFile;
import eionet.webq.service.FileNotAvailableException;
import eionet.webq.service.UserFileService;
import eionet.webq.service.WebFormService;
 import org.apache.commons.lang3.StringUtils;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.beans.factory.annotation.Qualifier;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestParam;
 
import javax.servlet.http.HttpServletRequest;
 
 /**
  * Test runner for remote form developers.
  */
 @Controller
 @RequestMapping("webform")
 public class RemoteWebFormTestRun {
     /**
      * User files service.
      */
     @Autowired
     UserFileService userFileService;
     /**
      * Remote web forms.
      */
     @Autowired
     @Qualifier("remoteWebForms")
     WebFormService webFormService;
 
     /**
      * Test run for remote web forms.
      *
      * @param webFormId web form id
      * @param instance form instance URL
      * @param additionalParameters additional request parameters
      * @param request request
      * @return redirect to web form
      * @throws FileNotAvailableException if web form's default instance is not available.
      */
     @RequestMapping("/test/run")
     public String webFormTestRun(@RequestParam int webFormId, @RequestParam(required = false) String instance,
                                  @RequestParam(required = false) String additionalParameters,
                                  HttpServletRequest request) throws FileNotAvailableException {
         int fileId = userFileService.saveBasedOnWebForm(new UserFile(), webFormService.findActiveWebFormById(webFormId));
         String redirect = "redirect:/xform/?formId=" + webFormId + "&fileId=" + fileId + "&base_uri=" + request.getContextPath();
         if (StringUtils.isNotEmpty(instance)) {
             redirect += "&instance=" + instance;
         }
         if (StringUtils.isNotEmpty(additionalParameters)) {
            redirect += additionalParameters;
         }
 
         return redirect;
     }
 }
