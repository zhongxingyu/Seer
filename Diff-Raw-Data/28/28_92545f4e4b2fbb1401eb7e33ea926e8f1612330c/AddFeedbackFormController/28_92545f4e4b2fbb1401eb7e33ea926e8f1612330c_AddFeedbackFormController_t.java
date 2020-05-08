 /**
  * The contents of this file are subject to the OpenMRS Public License
  * Version 1.0 (the "License"); you may not use this file except in
  * compliance with the License. You may obtain a copy of the License at
  * http://license.openmrs.org
  *
  * Software distributed under the License is distributed on an "AS IS"
  * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
  * License for the specific language governing rights and limitations
  * under the License.
  *
  * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
  */
 
 
 
 package org.openmrs.module.feedback.web;
 
 //~--- non-JDK imports --------------------------------------------------------
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 import org.openmrs.User;
 import org.openmrs.api.context.Context;
 import org.openmrs.module.feedback.Feedback;
 import org.openmrs.module.feedback.FeedbackService;
 import org.openmrs.module.feedback.FeedbackUser;
 import org.openmrs.notification.Message;
 import org.openmrs.web.WebConstants;
 
 import org.springframework.util.StringUtils;
 import org.springframework.web.bind.ServletRequestUtils;
 import org.springframework.web.multipart.MultipartFile;
 import org.springframework.web.multipart.MultipartHttpServletRequest;
 import org.springframework.web.servlet.mvc.SimpleFormController;
 import sun.misc.BASE64Decoder;
 
 //~--- JDK imports ------------------------------------------------------------
 
 import java.util.Date;
 import java.util.HashMap;
 import java.util.Map;
 
 import javax.servlet.http.HttpServletRequest;
 
 public class AddFeedbackFormController extends SimpleFormController {
 
     /** Logger for this class and subclasses */
     protected final Log log = LogFactory.getLog(getClass());
 
     @Override
     protected Boolean formBackingObject(HttpServletRequest request) throws Exception {
 
         /* To check wheather or not the subject , severity and feedback is empty or not */
         Boolean feedbackMessage = false;
         String  text            = "";
         String  subject         = request.getParameter("subject");
         String  severity        = request.getParameter("severity");
         String  feedback        = request.getParameter("feedback");
         String  receiver        = request.getParameter("fdbk_receiver");
         String  pageinfo        = request.getParameter("pageInfoPass");
 
 
         log.error("\n\n\n\n\n********** " + subject + severity + feedback + receiver + ">" + request.getParameter("pagecontext") + "\n\n\n\n\n**********");
 
         if (StringUtils.hasLength(subject) && StringUtils.hasLength(severity) && StringUtils.hasLength(severity)) {
             Object          o       = Context.getService(FeedbackService.class);
             FeedbackService service = (FeedbackService) o;
             Feedback        s       = new Feedback();
 
             s.setSubject(request.getParameter("subject"));
             s.setSeverity(request.getParameter("severity"));
             s.setPageinfo(pageinfo);
 
             /* To get the Stacktrace of the page from which the feedback is submitted */
             StackTraceElement[] c = Thread.currentThread().getStackTrace();
 
             if ("on".equals(request.getParameter("pagecontext"))) {
 
                 String manualStack = request.getParameter("stack");
                 if(manualStack != null){
                     feedback = "Feedback: " + feedback + " | User Generated Stacktrace: " + manualStack;
                 }
 
                 feedback = feedback + " | Automatic System Generated Stacktrace: ";
                 for (int i = 0; i < c.length; i++) {
                     feedback = feedback + System.getProperty("line.separator") + c[i].getFileName() + c[i].getMethodName() + c[i].getClass() + c[i].getLineNumber();
                 }
             }
 
             s.setContent(feedback);
 
             /* file upload in multiplerequest */
             if (request instanceof MultipartHttpServletRequest) {
                 MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) request;
                 MultipartFile               file             = (MultipartFile) multipartRequest.getFile("file");
 
                 if (!file.isEmpty()) {
                     if (file.getSize() <= 5242880) {
                         if (file.getOriginalFilename().endsWith(".jpeg") || file.getOriginalFilename().endsWith(".jpg")
                                 || file.getOriginalFilename().endsWith(".gif")
                                 || file.getOriginalFilename().endsWith(".png")) {
                             s.setMessage(file.getBytes());
                         } else {
                             request.getSession().setAttribute(WebConstants.OPENMRS_ERROR_ATTR,
                                                               "feedback.notification.feedback.error");
 
                             return false;
                         }
                     } else {
                         request.getSession().setAttribute(WebConstants.OPENMRS_ERROR_ATTR,
                                                           "feedback.notification.feedback.error");
 
                         return false;
                     }
                 }
             }
 
             String screenshotURL = request.getParameter("screenshotFile");
 
             if (screenshotURL != null) {
                 try {
                     String parts[] = screenshotURL.split(",");
                     BASE64Decoder decoder = new BASE64Decoder();
                     byte[] decodedBytes = decoder.decodeBuffer(parts[1]);
                     s.setScreenshot(decodedBytes);
 
                 } catch(Exception e){
                     e.printStackTrace();
                 }
             }
 
             /* Save the Feedback */
             service.saveFeedback(s);
 
            try{
                FeedbackUser feedbackUser = new FeedbackUser();
                feedbackUser.setFeedback(s);

                Context.addProxyPrivilege("View Users");
                feedbackUser.setUser(Context.getUserService().getUserByUsername(receiver));
                Context.removeProxyPrivilege("View Users");

                service.saveFeedbackUser(feedbackUser);
            }
            catch(Exception e){
                log.error(e);
            }
 
             request.getSession().setAttribute(
                 WebConstants.OPENMRS_MSG_ATTR,
                 Context.getAdministrationService().getGlobalProperty("feedback.ui.notification"));
 
             if ("Yes".equals(
                     Context.getUserContext().getAuthenticatedUser().getUserProperty("feedback_notificationReceipt"))) {
                 try {
 
                     // Create Message
                     Message message = new Message();
 
                     message.setSender(
                         Context.getAdministrationService().getGlobalProperty("feedback.notification.email"));
                     message.setRecipients(
                         Context.getUserContext().getAuthenticatedUser().getUserProperty("feedback_email"));
                     message.setSubject("Feedback submission confirmation mail");
                     message.setContent(Context.getAdministrationService().getGlobalProperty("feedback.notification")
                                        + "Ticket Number: " + s.getFeedbackId() + " Subject :" + s.getSubject());
                     message.setSentDate(new Date());
 
                     // Send message
                     Context.getMessageService().send(message);
                 } catch (Exception e) {
                     log.error("Unable to sent the email to the Email : "
                               + Context.getUserContext().getAuthenticatedUser().getUserProperty("feedback_email"));
                 }
             }
 
             try {
                 // Create Message
                 Message message = new Message();
 
                 message.setSender(Context.getAdministrationService().getGlobalProperty("feedback.notification.email"));
                 message.setRecipients(
                     Context.getAdministrationService().getGlobalProperty("feedback.admin.notification.email"));
                 message.setSubject("New feedback submitted");
                 message.setContent(Context.getAdministrationService().getGlobalProperty("feedback.admin.notification")
                                    + "Ticket Number: " + s.getFeedbackId() + " Subject : " + s.getSubject()
                                    + " Take Action :" + request.getScheme() + "://" + request.getServerName() + ":"
                                    + request.getServerPort() + request.getContextPath()
                                    + "/module/feedback/feedback.form?feedbackId=" + s.getFeedbackId() + "#command");
                 message.setSentDate(new Date());
 
                 // Send message
                 Context.getMessageService().send(message);
             } catch (Exception e) {
                 log.error(
                     "Unable to sent the email to the Email : "
                     + Context.getUserContext().getAuthenticatedUser().getUserProperty(
                         "feedback.admin.notification.email"));
             }
 
             feedbackMessage = true;
         }
 
         /* Reserved for future use for showing that the data is saved and the feedback is submitted */
         log.debug("Returning hello world text: " + text);
 
         return feedbackMessage;
     }
 
     @Override
     protected Map referenceData(HttpServletRequest req) throws Exception {
         Map<String, Object> map = new HashMap<String, Object>();
 
         /* Return List of Predefined Subjects and Severities for the feedback submission form */
         FeedbackService hService = (FeedbackService) Context.getService(FeedbackService.class);
 
         map.put("predefinedsubjects", hService.getPredefinedSubjects());
         map.put("severities", hService.getSeverities());
 
         return map;
     }
 }
 
 
 //~ Formatted by Jindent --- http://www.jindent.com
