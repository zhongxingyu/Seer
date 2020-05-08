 // $Id$
 //==============================================================================
 // FileName testmail.java
 // CodeJock patrick@nelware.com 
 //-----------------------------------------------------------------------------
 // Description: Java (class) code
 // Location...: 
 // Company....: Nelware
 //-----------------------------------------------------------------------------
 // This program is distributed strictly as a learning aid and Nelware
 // disclaims all warranties - including but not limited to: fitness for a
 // particular purpose, merchantability, loss of business, harm to your
 // system, etc... ALWAYS BACK UP YOUR SYSTEM BEFORE INSTALLING ANY SCRIPT
 // OR PROGRAM FROM ANY SOURCE!
 //-----------------------------------------------------------------------------
 //     *** Copyright (c) 2008 Nelware LLC.  All Rights Rreserved. ***
 //==============================================================================
 package TestMail;
 
 import org.apache.commons.mail.*;
 //import javax.mail.*;
 
 public class testmail {
 /**====================================================== 
  * @name   testmail Class
  * @author patrick@nelware.com  
  **====================================================*/
 
   //---------Begin Attributes---------
   //----------End Attributes----------
   
   //--------Begin Constructors--------
   //public testmail() { }
   //---------End Constructors---------
     
   //-------Begin Nested Classes------
   //--------End Nested Classes-------
 
   //-----------Begin Methods----------
   //------------End Methods-----------
 
   public static void main(String[] args) {
   //-----------------------------------------------------
   try {
     SimpleEmail email = new SimpleEmail();
    email.setHostName("smtp.1and1.com");
     email.addTo("patrick@nelsons.name", "Patrick Nelson");
     email.setFrom("patrick@nelsons.name", "Me");
     email.setSubject("Test message");
     email.setMsg("This is a simple test of commons-email");
     email.send();
   } catch (Exception e) {
     System.out.println(e.getMessage());
     //throw e;
   }
   System.out.println("Sent");
 
 //  HtmlEmail email = new HtmlEmail();
 //  email.setHostName(mailserver);
 //  email.setAuthentication(username, password);
 //  email.setSmtpPort(port);
 //  email.setFrom(fromEmail);
 //  email.addTo(to);
 //  email.setSubject(subject);
 //  email.setTextMsg(textBody);
 //  email.setHtmlMsg(htmlBody);
 //  email.setDebug(true);
 //  email.send();
   } //---eom---
 
 } //===eoc===
 
 
 /*+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
 /* notes
 /*+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
 -------------------------------------------------------------------------------
 
 -------------------------------------------------------------------------------
 /*+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++*/
