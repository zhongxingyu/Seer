 package com.wcs.newsletter.util;
 
 /*
  * #%L
  * Webstar Newsletter
  * %%
  * Copyright (C) 2013 Webstar Csoport Kft.
  * %%
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as
  * published by the Free Software Foundation, either version 3 of the 
  * License, or (at your option) any later version.
  * 
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public 
  * License along with this program.  If not, see
  * <http://www.gnu.org/licenses/gpl-3.0.html>.
  * #L%
  */
 
 import com.liferay.faces.util.logging.Logger;
 import com.liferay.faces.util.logging.LoggerFactory;
 import com.liferay.mail.service.MailServiceUtil;
 import com.liferay.portal.kernel.mail.MailMessage;
 import com.liferay.portal.theme.ThemeDisplay;
 import com.wcs.newsletter.dto.NewsletterSenderList;
 import com.wcs.newsletter.dto.SendListElem;
 import javax.mail.internet.InternetAddress;
 
 public class NewsletterSenderThread implements Runnable {
 
     private static final Logger logger = LoggerFactory.getLogger(NewsletterSenderThread.class);
     private ThemeDisplay themeDisplay;
     private InternetAddress from;
     private String subject;
     private String htmlContent;
     private NewsletterSenderList newsletterSenderList;
 
     public NewsletterSenderThread(InternetAddress from, NewsletterSenderList newsletterSenderList, String subject, String htmlContent, ThemeDisplay themeDisplay) {
         this.from = from;
         this.subject = subject;
         this.htmlContent = htmlContent;
         this.newsletterSenderList = newsletterSenderList;
         this.themeDisplay = themeDisplay;
     }
 
     @Override
     public void run() {
         logger.info("NewsletterSenderThread start");
         for (SendListElem recipient : newsletterSenderList.getRecipients()) {
             sendEmail(recipient, from, subject, htmlContent, true);
         }
 
         logger.info("NewsletterSenderThread end");
     }
 
     public void sendEmail(SendListElem rcpt, InternetAddress from, String subject, String body, boolean sendHtml) {
         try {
             logger.info("sendEmail {0} {1} {2} {3}", new Object[]{from, subject, rcpt, body});
 
             MailMessage mailMessage = new MailMessage();
             String cancLink = "";
             if (rcpt.getNewsletterSubscriptionCategory() != null) {
                 cancLink = rcpt.getNewsletterSubscriptionCategory().getCancellationKey();
             }
             body = body.replace("###newsletterCategory###", rcpt.getCategoryName());
            body = body.replace("###portalUrl###", themeDisplay.getURLPortal());
            body = body.replace("###cancelattionLink###", "subscription?canckey=" + cancLink);
             mailMessage.setBody(body);
             mailMessage.setHTMLFormat(sendHtml);
             mailMessage.setFrom(from);
             mailMessage.setTo(rcpt.getSubscriptionEmail());
             mailMessage.setSubject(subject);
             MailServiceUtil.sendEmail(mailMessage);
         } catch (Exception e) {
             logger.error(e);
         }
     }
 }
