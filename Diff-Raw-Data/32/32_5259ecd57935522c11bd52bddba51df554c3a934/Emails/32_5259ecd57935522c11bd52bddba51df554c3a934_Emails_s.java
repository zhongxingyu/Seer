 /**
  * Copyright (c) 2012, SURFnet BV
  * All rights reserved.
  *
  * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the
  * following conditions are met:
  *
  *   * Redistributions of source code must retain the above copyright notice, this list of conditions and the following
  *     disclaimer.
  *   * Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following
  *     disclaimer in the documentation and/or other materials provided with the distribution.
  *   * Neither the name of the SURFnet BV nor the names of its contributors may be used to endorse or promote products
  *     derived from this software without specific prior written permission.
  *
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES,
  * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
  * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
  * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
  * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
  * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
  * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  */
 package nl.surfnet.bod.service;
 
 import javax.servlet.http.HttpServletRequest;
 
 import nl.surfnet.bod.domain.VirtualPort;
 import nl.surfnet.bod.domain.VirtualPortRequestLink;
 import nl.surfnet.bod.web.security.RichUserDetails;
 
 import org.joda.time.DateTime;
 import org.joda.time.format.DateTimeFormat;
 
 import com.google.common.base.Function;
 import com.google.common.base.Optional;
 import com.google.common.base.Throwables;
 
 public final class Emails {
 
   private static final String FOOTER = //
       "\n\nKind regards,\n" //
       + "The Bandwidth on Demand Application team";
 
   public static class ActivationEmail {
     private static final String ACTIVATION_BODY = //
     "Dear BoD Administrator,\n\n" //
         + "Please use the link below to verify the email address of the BoD administrators of institute: %s" //
         + FOOTER;
 
     public static String body(String link, String institute) {
       return String.format(ACTIVATION_BODY, link);
     }
 
     public static String subject(String prg) {
       return String.format("[SURFnet BoD] Verify email address of %s", prg);
     }
   }
 
   public static class VirtualPortRequestMail {
     private static final String VIRTUAL_PORT_REQUEST_BODY = //
     "Dear BoD Administrator,\n\n" //
         + "You have received a new Virtual Port Request.\n\n" //
         + "From: %s (%s)\n" //
         + "Team: %s\n" //
         + "Preferred name: %s\n" //
         + "Minimum Bandwidth: %d Mbit/s\n" //
         + "Reason: %s\n" //
         + "Institute: %s\n\n" //
         + "Click on the following link %s to create the virtual port." //
         + FOOTER;
 
     public static String body(RichUserDetails from, VirtualPortRequestLink requestLink, String link) {
       return String.format(VIRTUAL_PORT_REQUEST_BODY,
           from.getDisplayName(), from.getEmail().or("Unknown Email"),
           requestLink.getVirtualResourceGroup().getName(), requestLink.getUserLabel(),
           requestLink.getMinBandwidth(), requestLink.getMessage(),
           requestLink.getPhysicalResourceGroup().getInstitute().getName(), link);
     }
 
     public static String subject(RichUserDetails user) {
       return String.format("[BoD] Virtual Port Request from %s", user.getDisplayName());
     }
   }
 
   public static final class VirtualPortRequestApproveMail {
     private static final String BODY = //
     "Dear %s,\n\n" //
         + "Your Request for a Virtual Port from %s for your team %s has been approved.\n" //
         + "The Virtual Port is now available with the name '%s'. It has a max. bandwidth of %d Mbit/s." //
         + FOOTER;
 
     public static String body(VirtualPortRequestLink link, VirtualPort port) {
       return String.format(BODY, link.getRequestorName(), link.getPhysicalResourceGroup().getName(), link
           .getVirtualResourceGroup().getName(), port.getUserLabel(), port.getMaxBandwidth());
     }
 
     public static String subject(VirtualPort port) {
       return String.format("[BoD] Virtual Port %s created", port.getUserLabel());
     }
   }
 
   public static class VirtualPortRequestDeclineMail {
     private static final String BODY = //
         "Dear %s,\n\n" //
         + "You Request for a Virtual Port from %s for your team %s has been declined.\n" //
         + "The reason the BoD Administrator gave:\n%s"
         + FOOTER;
 
     public static String subject() {
       return "[BoD] Your Virtual Port Request has been declined";
     }
 
     public static String body(VirtualPortRequestLink link, String message) {
       return String.format(BODY,
           link.getRequestorName(), link.getPhysicalResourceGroup().getName(), link.getVirtualResourceGroup().getName(),
           message);
     }
   }
 
   public static class ErrorMail {
     private static final String ERROR_MAIL_BODY =
       "Dear BoD Team,\n\n"
       + "An exception occured.\n\n"
       + "User: %s (%s)\n"
       + "Username: %s\n"
       + "Request: %s (%s)\n"
       + "Around: %s\n"
       + "Stacktrace:\n%s" + FOOTER;
 
     public static String subject(String envUrl, Throwable throwable) {
      return String.format("[Exception on %s] %s", envUrl, throwable.getMessage());
     }
 
     public static String body(Throwable throwable, Optional<RichUserDetails> user, Optional<HttpServletRequest> request) {
       String userDisplayName = getOrUserUnknown(new Function<RichUserDetails, String>() {
         @Override
         public String apply(RichUserDetails input) {
           return input.getDisplayName();
         }
       }, user);
 
       String userEmail = getOrUserUnknown(new Function<RichUserDetails, String>() {
         @Override
         public String apply(RichUserDetails input) {
           return input.getEmail().or("Email not known");
         }
       }, user);
 
       String username = getOrUserUnknown(new Function<RichUserDetails, String>() {
         @Override
         public String apply(RichUserDetails input) {
           return input.getUsername();
         }
       }, user);
 
       String requestUrl = getOrRequestUnknown(new Function<HttpServletRequest, String>() {
         @Override
         public String apply(HttpServletRequest request) {
           return request.getRequestURL().append(request.getQueryString() != null ? "?" + request.getQueryString() : "").toString();
         }
       }, request);
 
       String requestMethod = getOrRequestUnknown(new Function<HttpServletRequest, String>() {
         @Override
         public String apply(HttpServletRequest request) {
           return request.getMethod();
         }
       }, request);
 
       return String.format(
         ERROR_MAIL_BODY,
         userDisplayName,
         userEmail,
         username,
         requestUrl,
         requestMethod,
         DateTimeFormat.mediumDateTime().print(DateTime.now()),
         Throwables.getStackTraceAsString(throwable));
     }
 
     private static String getOrUserUnknown(Function<RichUserDetails, String> function, Optional<RichUserDetails> user) {
       return user.transform(function).or("Unknown");
     }
 
     private static String getOrRequestUnknown(Function<HttpServletRequest, String> function, Optional<HttpServletRequest> request) {
       return request.transform(function).or("No request available");
     }
   }
 
 }
