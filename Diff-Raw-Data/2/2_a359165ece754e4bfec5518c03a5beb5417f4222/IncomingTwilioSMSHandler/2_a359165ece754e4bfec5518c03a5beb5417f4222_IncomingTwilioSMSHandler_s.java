 /*
  * Copyright (C) 2013 StackFrame, LLC
  * This code is licensed under GPLv2.
  */
 package com.stackframe.sarariman.telephony.twilio;
 
 import com.stackframe.sarariman.Sarariman;
 import com.stackframe.sarariman.telephony.SMSEvent;
 import java.io.IOException;
 import java.util.Enumeration;
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 /**
  *
  * @author mcculley
  */
 public class IncomingTwilioSMSHandler extends HttpServlet {
 
     private TwilioSMSGatewayImpl gateway;
 
     @Override
     public void init() throws ServletException {
         super.init();
         Sarariman sarariman = (Sarariman)getServletContext().getAttribute("sarariman");
         gateway = (TwilioSMSGatewayImpl)sarariman.getSMSGateway();
     }
 
     /**
      * Handles the HTTP
      * <code>POST</code> method.
      *
      * @param request servlet request
      * @param response servlet response
      * @throws ServletException if a servlet-specific error occurs
      * @throws IOException if an I/O error occurs
      */
     @Override
     protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
         Enumeration<String> names = request.getParameterNames();
         while (names.hasMoreElements()) {
             String name = names.nextElement();
             System.err.print("parameterName=" + name);
             System.err.println(": value=" + request.getParameter(name));
         }
 
         names = request.getHeaderNames();
         while (names.hasMoreElements()) {
             String name = names.nextElement();
             System.err.print("headerName=" + name);
             System.err.println(": value=" + request.getHeader(name));
         }
 
         String accountSid = request.getParameter("AccountSid");
         String expectedAccountSid = gateway.getRestClient().getAccountSid();
         if (!expectedAccountSid.equals(accountSid)) {
            // FIXME: Is this enough to verify this actually came from Twilio? Should we check IP address?
             throw new ServletException("account ID does not match");
         }
 
         String from = request.getParameter("From");
         String to = request.getParameter("To");
         String body = request.getParameter("Body");
         String status = request.getParameter("SmsStatus");
         long now = System.currentTimeMillis();
         SMSEvent e = new SMSEvent(from, to, body, now, status);
         gateway.distribute(e);
     }
 
 }
