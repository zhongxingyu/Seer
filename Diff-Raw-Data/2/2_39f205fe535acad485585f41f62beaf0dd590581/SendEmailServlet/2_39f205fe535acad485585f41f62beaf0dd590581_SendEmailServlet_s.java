 /*
  * Copyright 2010-2013, CloudBees Inc.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package localdomain.localhost;
 
 import javax.annotation.Resource;
 import javax.mail.Header;
 import javax.mail.Message;
 import javax.mail.Session;
 import javax.mail.Transport;
 import javax.mail.internet.InternetAddress;
 import javax.mail.internet.MimeMessage;
 import javax.servlet.ServletConfig;
 import javax.servlet.ServletException;
 import javax.servlet.annotation.WebServlet;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.sql.DataSource;
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.sql.Connection;
 import java.sql.ResultSet;
 import java.sql.Statement;
 import java.util.Enumeration;
 import java.util.logging.Logger;
 
 /**
  * @author <a href="mailto:cleclerc@cloudbees.com">Cyrille Le Clerc</a>
  */
 @WebServlet(value = "/send-email", loadOnStartup = 1)
 public class SendEmailServlet extends HttpServlet {
     protected final Logger logger = Logger.getLogger(getClass().getName());
     @Resource(name = "mail/SendGrid", lookup = "mail/SendGrid")
     private Session session;
 
     @Override
     public void init(ServletConfig config) throws ServletException {
         super.init(config);
         logger.info("Init - enable Mail Session debug");
         session.setDebug(true);
     }
 
     protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
 
         String recipientEmail = req.getParameter("recipient");
         String content = req.getParameter("message");
         if (content == null)
             content = "";
 
         if (content.length() > 40)
             content = content.substring(0, 40) + "...";
 
 
         PrintWriter writer = resp.getWriter();
 
         writer.println("<html>");
         writer.println("<head><title>SendEmailServlet</title></head>");
         writer.println("<body><h1>SendEmailServlet</h1>");
 
         writer.println("<h2>Email</h2>");
         try {
             Message message = new MimeMessage(session);
             message.setFrom(new InternetAddress("no-reply@example.com"));
             message.setRecipients(Message.RecipientType.TO, new InternetAddress[]{new InternetAddress(recipientEmail)});
             message.setSubject("CloudBees SendGrid Demo");
 
             message.setContent("CloudBees SendGrid Demo:" + content, "text/plain");
             Transport.send(message);
            logger.info("Message sent to " + recipientEmail + "<br/>");
             writer.write("<p>Message sent to " + recipientEmail + "</p>");
             writer.write("<code><pre>");
             for (Enumeration<Header> headers = message.getAllHeaders(); headers.hasMoreElements(); ) {
                 Header header = headers.nextElement();
                 writer.println(header.getName() + ": " + header.getValue());
             }
             writer.write("</pre></code>");
         } catch (Exception e) {
             writer.write("<code><pre>");
             e.printStackTrace(writer);
             writer.write("</pre></code>");
             e.printStackTrace();
         }
         writer.println("</body></html>");
 
     }
 }
