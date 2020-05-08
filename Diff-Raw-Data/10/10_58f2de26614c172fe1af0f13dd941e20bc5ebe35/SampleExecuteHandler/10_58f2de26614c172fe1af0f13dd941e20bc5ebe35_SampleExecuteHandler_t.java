 /*
  * Copyright (c) 2000-2003 Netspective Communications LLC. All rights reserved.
  *
  * Netspective Communications LLC ("Netspective") permits redistribution, modification and use of this file in source
  * and binary form ("The Software") under the Netspective Source License ("NSL" or "The License"). The following
  * conditions are provided as a summary of the NSL but the NSL remains the canonical license and must be accepted
  * before using The Software. Any use of The Software indicates agreement with the NSL.
  *
  * 1. Each copy or derived work of The Software must preserve the copyright notice and this notice unmodified.
  *
  * 2. Redistribution of The Software is allowed in object code form only (as Java .class files or a .jar file
  *    containing the .class files) and only as part of an application that uses The Software as part of its primary
  *    functionality. No distribution of the package is allowed as part of a software development kit, other library,
  *    or development tool without written consent of Netspective. Any modified form of The Software is bound by these
  *    same restrictions.
  *
  * 3. Redistributions of The Software in any form must include an unmodified copy of The License, normally in a plain
  *    ASCII text file unless otherwise agreed to, in writing, by Netspective.
  *
  * 4. The names "Netspective", "Axiom", "Commons", "Junxion", and "Sparx" are trademarks of Netspective and may not be
  *    used to endorse products derived from The Software without without written consent of Netspective. "Netspective",
  *    "Axiom", "Commons", "Junxion", and "Sparx" may not appear in the names of products derived from The Software
  *    without written consent of Netspective.
  *
  * 5. Please attribute functionality where possible. We suggest using the "powered by Netspective" button or creating
  *    a "powered by Netspective(tm)" link to http://www.netspective.com for each application using The Software.
  *
  * THE SOFTWARE IS PROVIDED "AS IS" WITHOUT A WARRANTY OF ANY KIND. ALL EXPRESS OR IMPLIED REPRESENTATIONS AND
  * WARRANTIES, INCLUDING ANY IMPLIED WARRANTY OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE OR NON-INFRINGEMENT,
  * ARE HEREBY DISCLAIMED.
  *
  * NETSPECTIVE AND ITS LICENSORS SHALL NOT BE LIABLE FOR ANY DAMAGES SUFFERED BY LICENSEE OR ANY THIRD PARTY AS A
  * RESULT OF USING OR DISTRIBUTING THE SOFTWARE. IN NO EVENT WILL NETSPECTIVE OR ITS LICENSORS BE LIABLE FOR ANY LOST
  * REVENUE, PROFIT OR DATA, OR FOR DIRECT, INDIRECT, SPECIAL, CONSEQUENTIAL, INCIDENTAL OR PUNITIVE DAMAGES, HOWEVER
  * CAUSED AND REGARDLESS OF THE THEORY OF LIABILITY, ARISING OUT OF THE USE OF OR INABILITY TO USE THE SOFTWARE, EVEN
  * IF HE HAS BEEN ADVISED OF THE POSSIBILITY OF SUCH DAMAGES.
  *
  * @author Shahid N. Shah
  */
 
 /**
 * $Id: SampleExecuteHandler.java,v 1.2 2004-03-05 14:48:57 zahara.khan Exp $
  */
 
 package app.form.exec.delegation;
 
 import java.io.Writer;
 import java.io.IOException;
 
 import com.netspective.sparx.form.handler.DialogExecuteHandler;
 import com.netspective.sparx.form.DialogContext;
 import com.netspective.sparx.form.DialogExecuteException;
 
 public class SampleExecuteHandler implements DialogExecuteHandler
 {
     public void executeDialog(Writer writer, DialogContext dc) throws IOException, DialogExecuteException
     {
         writer.write("Welcome to NEFS <b>" + dc.getFieldStates().getState("full_name").getValue().getTextValue() +
                      "</b>. ");
         writer.write("You are running the dialog called <b>"+ dc.getDialog().getName() +"</b> in package <b>"+
                      dc.getDialog().getNameSpace().getNameSpaceId() +"</b>.");
 
        // because we're handling both forms (exec1a and exec1b we need to check if we're running the second form)
        if(dc.getDialog().getQualifiedName().equals("form.exec.delegation.exec1b"))
             writer.write("<p>You are <b>"+ dc.getFieldStates().getState("age").getValue().getIntValue() + "</b> " +
                          "years old.");
 
         writer.write("<p>");
         writer.write("This code demonstrated how you can take two pieces of data and execute an arbritrary class by " +
                      "supplying an &lt;on-execute&gt; tag in the &lt;dialog&gt;. ");
         writer.write("This method of execution is called <i>Dialog Delegation</i> because a class separate from the " +
                      "Dialog class handles the execution.<p>");
 
        String relativePath = "/WEB-INF/classes/app/form/exec/delegation/SampleExecuteHandler.java";
         String sourceFileLink = dc.getConsoleFileBrowserLinkShowAlt(
                                    dc.getServlet().getServletConfig().getServletContext().getRealPath(relativePath),
                                    relativePath);
         writer.write("You may review the code at " + sourceFileLink + ".");
         writer.write("<p>");
         writer.write("<a href=\""+ dc.getNavigationContext().getActivePage().getName() +"\">Clear the dialog</a>");
     }
 }
