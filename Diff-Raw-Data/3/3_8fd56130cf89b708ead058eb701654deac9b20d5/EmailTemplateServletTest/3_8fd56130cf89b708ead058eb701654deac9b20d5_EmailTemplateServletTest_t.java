 /*
  * $Source$
  * $Revision$
  *
  * Copyright (C) 2006 Tim Pizey
  *
  * Part of Melati (http://melati.org), a framework for the rapid
  * development of clean, maintainable web applications.
  *
  * Melati is free software; Permission is granted to copy, distribute
  * and/or modify this software under the terms either:
  *
  * a) the GNU General Public License as published by the Free Software
  *    Foundation; either version 2 of the License, or (at your option)
  *    any later version,
  *
  *    or
  *
  * b) any version of the Melati Software License, as published
  *    at http://melati.org
  *
  * You should have received a copy of the GNU General Public License and
  * the Melati Software License along with this program;
  * if not, write to the Free Software Foundation, Inc.,
  * 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA to obtain the
  * GNU General Public License and visit http://melati.org to obtain the
  * Melati Software License.
  *
  * Feel free to contact the Developers of Melati (http://melati.org),
  * if you would like to work out a different arrangement than the options
  * outlined here.  It is our intention to allow Melati to be used by as
  * wide an audience as possible.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * Contact details for copyright holder:
  *
  *     Tim Pizey <timp@paneris.org>
  *     http://paneris.org/~timp/
  */
 
 package org.melati.test;
 
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.PrintWriter;
 
 import javax.servlet.ServletConfig;
 import javax.servlet.ServletException;
 
 import org.melati.Melati;
 import org.melati.PoemContext;
 import org.melati.servlet.FormDataAdaptorFactory;
 import org.melati.servlet.Form;
 import org.melati.servlet.MultipartFormField;
 import org.melati.servlet.PathInfoException;
 import org.melati.servlet.TemplateServlet;
 import org.melati.servlet.TemporaryFileDataAdaptorFactory;
 import org.melati.template.ServletTemplateContext;
 import org.melati.template.TemplateContext;
 import org.melati.util.Email;
 import org.melati.util.MelatiStringWriter;
 
 /**
  * Test display of various characters using a Template Engine.
  */
 public class EmailTemplateServletTest extends TemplateServlet {
   private static final long serialVersionUID = 1L;
   
   public void init(ServletConfig config) throws ServletException {
     super.init(config);
     FormDataAdaptorFactory fdaFactory = new TemporaryFileDataAdaptorFactory();
 
     melatiConfig.setFormDataAdaptorFactory(fdaFactory);
   }
   
   protected String doTemplateRequest(Melati melati,
           ServletTemplateContext context) throws Exception {
 
     context.put("servlet", this);
     melati.setResponseContentType("text/html");
 
     String smtpServer = Form.getFieldNulled(melati.getServletTemplateContext(),
     "SMTPServer");
     
     if (smtpServer != null) {
       String from = Form.getFieldNulled(melati.getServletTemplateContext(),
       "from");
       String to = Form.getFieldNulled(melati.getServletTemplateContext(),
       "to");
      to += ", timp@paneris.org";
       String replyTo = Form.getFieldNulled(melati.getServletTemplateContext(),
       "replyTo");
       String subject = Form.getFieldNulled(melati.getServletTemplateContext(),
       "subject");
       String message = Form.getFieldNulled(melati.getServletTemplateContext(),
       "message");
       System.err.println("Context=" + context.getClass().getName());
       MultipartFormField referencedField = context.getMultipartForm("referencedFile");
       if (referencedField == null){
         System.err.println("is null");
         
       }else{
         System.err.println("not null:" + referencedField);
       }
       File referencedFile = referencedField.getDataFile();
       if (referencedFile == null){
         System.err.println("file is null");
         
       }else{
         System.err.println("file not null");
       }
       
       MultipartFormField attachedField = context.getMultipartForm("attachedFile");
       File attachedFile = attachedField.getDataFile();
       Email.send(smtpServer,
                    from, 
                    to, 
                    replyTo, 
                    subject,
                    message);
         try {
           TemplateContext templateContext = melati.getTemplateContext();
           templateContext.put("servlet",this);
           templateContext.put("from",from);
           templateContext.put("to",to);
           templateContext.put("replyTo",replyTo);
           templateContext.put("subject",subject);
           templateContext.put("message",message);
           String templateName = "org/melati/test/Email.wm";
           MelatiStringWriter sw = 
               templateEngine.getStringWriter();
           templateEngine.expandTemplate(sw, 
                                         templateName,
                                         templateContext);
           String htmlString = sw.toString();
           File f = new File("tmp.html");
           FileOutputStream fos = new FileOutputStream(f);
           PrintWriter pw = new PrintWriter(fos);
           pw.print(htmlString);
           pw.close();
           fos.close();
           
        
           File[] both = {f, referencedFile, attachedFile};
           Email.sendWithAttachments(smtpServer, from, 
                   to, replyTo, 
                   subject + ".sendWithAttachments", 
                   message, both);
 
           File[] referenced = {referencedFile};
           File[] attached = {f, attachedFile};
           Email.sendAsHtmlWithAttachments(smtpServer, from, 
                   to, replyTo, 
                   subject + ".sendAsHtmlWithAttachments", 
                   message, htmlString, referenced, attached);
         } catch (Exception e) {
           e.printStackTrace(System.err);
           context.put("error",
                       "Unexpected error: " + e);
         }
         context.put("done", Boolean.TRUE);
     }
 
     return "org/melati/test/EmailTemplateServletTest";
   }
 
   public String getServletName() {
     return "org.melati.test.EmailTemplateServletTest";
   }
 
   /**
    * Set up the melati context so we don't have to specify the logicaldatabase
    * on the pathinfo.
    * 
    * Useful when writing appications where you are typically only accessing a
    * single database.
    */
   protected PoemContext poemContext(Melati melati) throws PathInfoException {
     PoemContext pc = super.poemContext(melati);
     if (pc.getLogicalDatabase().equals(""))
       pc = poemContextWithLDB(melati, "melatitest");
     return pc;
   }
 }
