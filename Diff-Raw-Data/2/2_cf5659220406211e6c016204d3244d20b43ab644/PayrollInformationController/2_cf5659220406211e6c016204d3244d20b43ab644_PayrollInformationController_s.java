 /**
  * Licensed to Jasig under one or more contributor license
  * agreements. See the NOTICE file distributed with this work
  * for additional information regarding copyright ownership.
  * Jasig licenses this file to you under the Apache License,
  * Version 2.0 (the "License"); you may not use this file
  * except in compliance with the License. You may obtain a
  * copy of the License at:
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing,
  * software distributed under the License is distributed on
  * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
  * KIND, either express or implied. See the License for the
  * specific language governing permissions and limitations
  * under the License.
  */
 
 package edu.wisc.portlet.hrs.web.payroll;
 
 import javax.portlet.PortletPreferences;
 import javax.portlet.PortletRequest;
 
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.ModelMap;
 import org.springframework.web.bind.annotation.ModelAttribute;
 import org.springframework.web.bind.annotation.RequestMapping;
 
 import edu.wisc.hr.dao.person.ContactInfoDao;
 import edu.wisc.hr.dm.person.PersonInformation;
 
import org.jasig.springframework.security.portlet.authentication.PrimaryAttributeUtils;
 
 import edu.wisc.portlet.hrs.web.HrsControllerBase;
 
 /**
  * @author Eric Dalquist
  * @version $Revision: 1.2 $
  */
 @Controller
 @RequestMapping("VIEW")
 public class PayrollInformationController extends HrsControllerBase {
     private ContactInfoDao contactInfoDao;
     
     @Autowired
     public void setContactInfoDao(ContactInfoDao contactInfoDao) {
         this.contactInfoDao = contactInfoDao;
     }
     
     /**
      * Gets the URL to a page describing your earning statement
      * @param request the request
      * @return the value of the portlet preference
      */
     @ModelAttribute("understandingEarningUrl")
     public final String getUnderstandingEarningUrl(PortletRequest request) {
         final PortletPreferences preferences = request.getPreferences();
         return preferences.getValue("understandingEarningUrl",null);
     }
 
     @RequestMapping
     public String viewContactInfo(ModelMap model, PortletRequest request) {
         final String emplId = PrimaryAttributeUtils.getPrimaryId();
         
         try {
             final PersonInformation personalData = this.contactInfoDao.getPersonalData(emplId);
             model.addAttribute("personalData", personalData);
         }
         catch (Exception e) {
             logger.warn("Caught exception while getting PersonalInformation for " + emplId + " in payroll information portlet, procceding without it.", e);
             model.addAttribute("personalDataError", true);
         }
         
         return "payrollInformation";
     }
 }
