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
  * The Original Code is Content Registry 3
  *
  * The Initial Owner of the Original Code is European Environment
  * Agency. Portions created by TripleDev or Zero Technologies are Copyright
  * (C) European Environment Agency.  All Rights Reserved.
  *
  * Contributor(s):
  *        Juhan Voolaid
  */
 
 package eionet.meta.service;
 
 import java.io.IOException;
 import java.io.StringWriter;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.Hashtable;
 import java.util.Map;
 
 import javax.mail.internet.InternetAddress;
 import javax.mail.internet.MimeMessage;
 
 import org.apache.commons.lang.StringUtils;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.mail.javamail.JavaMailSender;
 import org.springframework.mail.javamail.MimeMessageHelper;
 import org.springframework.mail.javamail.MimeMessagePreparator;
 import org.springframework.stereotype.Service;
 
 import eionet.directory.DirServiceException;
 import eionet.directory.DirectoryService;
 import eionet.meta.dao.ISiteCodeDAO;
 import eionet.meta.notif.SiteCodeAddedNotification;
 import eionet.meta.notif.SiteCodeAllocationNotification;
 import eionet.meta.service.data.AllocationResult;
 import eionet.meta.service.data.SiteCodeFilter;
 import eionet.meta.service.data.SiteCodeResult;
 import eionet.util.Props;
 import eionet.util.PropsIF;
 import freemarker.template.Configuration;
 import freemarker.template.Template;
 import freemarker.template.TemplateException;
 
 /**
  * E-mail service.
  *
  * @author Juhan Voolaid
  */
 @Service
 public class EmailServiceImpl implements IEmailService {
 
     /** Placeholder for country code. */
     private static final String COUNTRY_CODE_PLACEHOLDER = "[iso_country_code]";
 
     /** Placeholder for member or collaborative country abbreviation. */
     private static final String MC_CC_PLACEHOLDER = "[mc_or_cc]";
 
     /** Member country string in LDAP role. */
     private static final String MC = "mc";
 
     /** Collaborative country string in LDAP role. */
     private static final String CC = "cc";
 
     /** Freemarker template engine configuration. */
     @Autowired
     private Configuration configuration;
 
     /** Java mail sender. */
     @Autowired
     private JavaMailSender mailSender;
 
     /** Site Code DAO. */
     @Autowired
     private ISiteCodeDAO siteCodeDao;
 
     /**
      * {@inheritDoc}
      */
     @Override
     public void notifySiteCodeAllocation(String country, AllocationResult allocationResult, boolean adminRole)
             throws ServiceException {
         try {
             SiteCodeAllocationNotification notification = new SiteCodeAllocationNotification();
             notification.setAllocationTime(allocationResult.getAllocationTime().toString());
             notification.setUsername(allocationResult.getUserName());
             notification.setCountry(country);
             notification.setNofAvailableCodes(Integer.toString(siteCodeDao.getFeeSiteCodeAmount()));
             notification.setTotalNofAllocatedCodes(Integer.toString(siteCodeDao.getCountryUnusedAllocations(country, false)));
             notification.setNofCodesAllocatedByEvent(Integer.toString(allocationResult.getAmount()));
 
             SiteCodeFilter filter = new SiteCodeFilter();
             filter.setDateAllocated(allocationResult.getAllocationTime());
             filter.setUserAllocated(allocationResult.getUserName());
            filter.setUsePaging(false);
             SiteCodeResult siteCodes = siteCodeDao.searchSiteCodes(filter);
 
             notification.setSiteCodes(siteCodes.getList());
             notification.setAdminRole(adminRole);
 
             final String[] to;
             // if test e-mail is provided, then do not send notification to actual receivers
             if (!StringUtils.isEmpty(Props.getProperty(PropsIF.SITE_CODE_TEST_NOTIFICATION_TO))) {
                 notification.setTest(true);
                 notification.setTo(StringUtils.join(parseRoleAddresses(country), ","));
                 to = StringUtils.split(Props.getProperty(PropsIF.SITE_CODE_TEST_NOTIFICATION_TO), ",");
             } else {
                 to = parseRoleAddresses(country);
             }
             Map<String, Object> map = new HashMap<String, Object>();
             map.put("data", notification);
 
             final String text = processTemplate("site_code_allocation.ftl", map);
 
             MimeMessagePreparator mimeMessagePreparator = new MimeMessagePreparator() {
                 @Override
                 public void prepare(MimeMessage mimeMessage) throws Exception {
                     MimeMessageHelper message = new MimeMessageHelper(mimeMessage, false);
                     message.setText(text, false);
                     message.setFrom(new InternetAddress(Props.getRequiredProperty(PropsIF.SITE_CODE_NOTIFICATION_FROM)));
                     message.setSubject("Site codes allocated");
                     message.setTo(to);
                 }
             };
             mailSender.send(mimeMessagePreparator);
 
         } catch (Exception e) {
             throw new ServiceException("Failed to send allocation notification: " + e.toString(), e);
         }
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public void notifySiteCodeReservation(String userName, int startIdentifier, int reserveAmount) throws ServiceException {
         try {
             SiteCodeAddedNotification notification = new SiteCodeAddedNotification();
             notification.setCreatedTime(new Date().toString());
             notification.setUsername(userName);
             notification.setNewCodesStartIdentifier(Integer.toString(startIdentifier));
             notification.setNofAddedCodes(Integer.toString(reserveAmount));
             notification.setNewCodesEndIdentifier(Integer.toString(startIdentifier + reserveAmount - 1));
             notification.setTotalNumberOfAvailableCodes(Integer.toString(siteCodeDao.getFeeSiteCodeAmount()));
 
             final String[] to;
             // if test e-mail is provided, then do not send notification to actual receivers
             if (!StringUtils.isEmpty(Props.getProperty(PropsIF.SITE_CODE_TEST_NOTIFICATION_TO))) {
                 notification.setTest(true);
                 notification.setTo(Props.getRequiredProperty(PropsIF.SITE_CODE_RESERVE_NOTIFICATION_TO));
                 to = StringUtils.split(Props.getProperty(PropsIF.SITE_CODE_TEST_NOTIFICATION_TO), ",");
             } else {
                 to = StringUtils.split(Props.getRequiredProperty(PropsIF.SITE_CODE_RESERVE_NOTIFICATION_TO), ",");
             }
             Map<String, Object> map = new HashMap<String, Object>();
             map.put("data", notification);
 
             final String text = processTemplate("site_code_reservation.ftl", map);
 
             MimeMessagePreparator mimeMessagePreparator = new MimeMessagePreparator() {
                 @Override
                 public void prepare(MimeMessage mimeMessage) throws Exception {
                     MimeMessageHelper message = new MimeMessageHelper(mimeMessage, false);
                     message.setText(text, false);
                     message.setFrom(new InternetAddress(Props.getRequiredProperty(PropsIF.SITE_CODE_NOTIFICATION_FROM)));
                     message.setSubject("New site codes added");
                     message.setTo(to);
                 }
             };
             mailSender.send(mimeMessagePreparator);
 
         } catch (Exception e) {
             throw new ServiceException("Failed to send new site codes reservation notification: " + e.toString(), e);
         }
     }
 
     /**
      * Processes template and returns the result.
      *
      * @param templateName
      * @param data
      * @return
      * @throws TemplateException
      * @throws IOException
      */
     private String processTemplate(String templateName, Map<String, Object> data) throws TemplateException, IOException {
         Template template = configuration.getTemplate(templateName, "utf-8");
         StringWriter writer = new StringWriter();
         template.process(data, writer);
         return writer.toString();
     }
 
     /**
      * Parse LDAP role e-mail addresses and replace country code and member/collaborative country abbreviations.
      *
      * @param country
      * @return
      * @throws DirServiceException
      */
     private String[] parseRoleAddresses(String country) throws DirServiceException {
         String recipients = Props.getRequiredProperty(PropsIF.SITE_CODE_ALLOCATE_NOTIFICATION_TO);
         recipients = StringUtils.replace(recipients, COUNTRY_CODE_PLACEHOLDER, country.toLowerCase());
         String[] to = StringUtils.split(recipients, ",");
 
         for (int i = 0; i < to.length; i++) {
             if (to[i].contains(MC_CC_PLACEHOLDER)) {
                 // test if it is member country
                 String roleId = StringUtils.substringBefore(to[i], "@");
                 String mcRoleId = StringUtils.replace(roleId, MC_CC_PLACEHOLDER, MC);
                 if (roleExists(mcRoleId)) {
                     to[i] = StringUtils.replace(to[i], MC_CC_PLACEHOLDER, MC);
                     continue;
                 }
                 // test if it is collaborative country country
                 String ccRoleId = StringUtils.replace(roleId, MC_CC_PLACEHOLDER, CC);
                 if (roleExists(ccRoleId)) {
                     to[i] = StringUtils.replace(to[i], MC_CC_PLACEHOLDER, CC);
                 }
             }
         }
 
         return to;
     }
 
     /**
      * Check if role exists in LDAP.
      *
      * @param roleId
      * @return true if role is present.
      */
     private boolean roleExists(String roleId) {
         try {
             Hashtable<String, Object> role = DirectoryService.getRole(roleId);
             if (role != null) {
                 return true;
             } else {
                 return false;
             }
         } catch (DirServiceException e) {
             // role does not exist
             return false;
         }
     }
 }
