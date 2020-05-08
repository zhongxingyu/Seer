 /*  
  * MailPrintResource.java  
  *   
  * Copyright (C) 2009 LWsystems GmbH & Co. KG  
  * This program is free software; you can redistribute it and/or 
  * modify it under the terms of the GNU General Public License as 
  * published by the Free Software Foundation; either version 2 of 
  * the License, or (at your option) any later version.
  * 
  * This program is distributed in the hope that it will be useful,  
  * but WITHOUT ANY WARRANTY; without even the implied warranty of  
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the  
  * GNU General Public License for more details.  
  *   
  * You should have received a copy of the GNU General Public License  
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.  
  */ 
 package de.lwsystems.mailarchive.web.resources;
 
 import de.lwsystems.mailarchive.repository.Repository;
 import java.io.IOException;
 import java.io.OutputStreamWriter;
 import org.wings.SFrame;
 import org.wings.io.Device;
 import org.wings.io.DeviceOutputStream;
 import org.wings.resource.DynamicResource;
 import org.wings.resource.ResourceNotFoundException;
 
 /**
  *
  * @author rene
  */
 public class MailPrintResource extends DynamicResource {
 
     private Repository repo;
     private String msgid = "";
     private String from = "";
     private String to = "";
     private String sent = "";
     private String subject = "";
     private String mainText = "";
 
     /**
      * 
      * @param from
      */
     public void setFrom(String from) {
         if (from != null) {
             this.from = from;
         }
     }
 
     /**
      * 
      * @param mainText
      */
     public void setMainText(String mainText) {
         if (mainText != null) {
             this.mainText = mainText;
         }
     }
 
     /**
      * 
      * @param sent
      */
     public void setSent(String sent) {
         if (sent != null) {
             this.sent = sent;
         }
     }
 
     /**
      * 
      * @param subject
      */
     public void setSubject(String subject) {
         if (subject != null) {
             this.subject = subject;
         }
     }
 
     /**
      * 
      * @param to
      */
     public void setTo(String to) {
         if (to != null) {
             this.to = to;
         }
     }
 
     /**
      * 
      * @param extension
      * @param mimeType
      */
     protected MailPrintResource(String extension, String mimeType) {
         super(extension, mimeType);
     }
 
     /**
      * 
      * @param frame
      */
     public MailPrintResource(SFrame frame) {
         this(frame, "", "text/html");
     }
 
     /**
      * 
      * @param frame
      * @param extension
      * @param mimeType
      */
     public MailPrintResource(SFrame frame, String extension, String mimeType) {
         super(frame, extension, mimeType);
     }
 
     /**
      * 
      * @param repo
      */
     public void setRepository(Repository repo) {
         this.repo = repo;
     }
 
     /**
      * 
      * @param out
      * @throws java.io.IOException
      * @throws org.wings.resource.ResourceNotFoundException
      */
     public void write(Device out) throws IOException, ResourceNotFoundException {
 
         DeviceOutputStream os = new DeviceOutputStream(out);
         OutputStreamWriter osw = new OutputStreamWriter(os);
         osw.write("<HTML>\n ");
         osw.write("<head><META HTTP-EQUIV=\"content-type\" CONTENT=\"text/html; charset=utf-8\"></head>");
         osw.write("<BODY onload=\" window.print()\" >");
         osw.write("<table>");
         osw.write("<tr><td><b>Von:<b></td>");
         osw.write("<td>"+from+"</td></tr>");
         osw.write("<tr><td><b>An:<b></td>");
         osw.write("<td>"+to+"</td></tr>");
        osw.write("<tr><td><b>Datum:<b></td>");
         osw.write("<td>"+sent+"</td></tr>");
         osw.write("<tr><td><b>Betreff:<b></td>");
         osw.write("<td>"+subject+"</td></tr>");
         osw.write("</table>");
         osw.write("<pre>"+mainText+"</pre>");
         osw.write("</BODY>\n</HTML>");
         osw.flush();
         osw.close();
         os.flush();
         os.close();
     }
 }
