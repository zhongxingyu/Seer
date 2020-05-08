 /*
  * Copyright (c) 2000-2002 Netspective Corporation -- all rights reserved
  *
  * Netspective Corporation permits redistribution, modification and use
  * of this file in source and binary form ("The Software") under the
  * Netspective Source License ("NSL" or "The License"). The following 
  * conditions are provided as a summary of the NSL but the NSL remains the 
  * canonical license and must be accepted before using The Software. Any use of
  * The Software indicates agreement with the NSL. 
  *
  * 1. Each copy or derived work of The Software must preserve the copyright
  *    notice and this notice unmodified.
  *
  * 2. Redistribution of The Software is allowed in object code form only 
  *    (as Java .class files or a .jar file containing the .class files) and only 
  *    as part of an application that uses The Software as part of its primary 
  *    functionality. No distribution of the package is allowed as part of a software 
  *    development kit, other library, or development tool without written consent of 
  *    Netspective Corporation. Any modified form of The Software is bound by 
  *    these same restrictions.
  * 
  * 3. Redistributions of The Software in any form must include an unmodified copy of 
  *    The License, normally in a plain ASCII text file unless otherwise agreed to,
  *    in writing, by Netspective Corporation.
  *
  * 4. The names "Sparx" and "Netspective" are trademarks of Netspective 
  *    Corporation and may not be used to endorse products derived from The 
  *    Software without without written consent of Netspective Corporation. "Sparx" 
  *    and "Netspective" may not appear in the names of products derived from The 
  *    Software without written consent of Netspective Corporation.
  *
  * 5. Please attribute functionality to Sparx where possible. We suggest using the 
  *    "powered by Sparx" button or creating a "powered by Sparx(tm)" link to
  *    http://www.netspective.com for each application using Sparx.
  *
  * The Software is provided "AS IS," without a warranty of any kind. 
  * ALL EXPRESS OR IMPLIED REPRESENTATIONS AND WARRANTIES, INCLUDING ANY
  * IMPLIED WARRANTY OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE
  * OR NON-INFRINGEMENT, ARE HEREBY DISCLAIMED.
  *
  * NETSPECTIVE CORPORATION AND ITS LICENSORS SHALL NOT BE LIABLE FOR ANY DAMAGES
  * SUFFERED BY LICENSEE OR ANY THIRD PARTY AS A RESULT OF USING OR DISTRIBUTING 
  * THE SOFTWARE. IN NO EVENT WILL NETSPECTIVE OR ITS LICENSORS BE LIABLE
  * FOR ANY LOST REVENUE, PROFIT OR DATA, OR FOR DIRECT, INDIRECT, SPECIAL,
  * CONSEQUENTIAL, INCIDENTAL OR PUNITIVE DAMAGES, HOWEVER CAUSED AND
  * REGARDLESS OF THE THEORY OF LIABILITY, ARISING OUT OF THE USE OF OR
  * INABILITY TO USE THE SOFTWARE, EVEN IF HE HAS BEEN ADVISED OF THE POSSIBILITY
  * OF SUCH DAMAGES.      
  *
  * @author Shahid N. Shah
  */
  
 /**
 * $Id: DialogDirector.java,v 1.3 2002-08-17 15:14:51 shahid.shah Exp $
  */
 
 package com.netspective.sparx.xaf.form;
 
 import java.io.IOException;
 import java.io.Writer;
 
 import org.w3c.dom.Element;
 
 import com.netspective.sparx.util.value.SingleValueSource;
 import com.netspective.sparx.util.value.ValueSourceFactory;
 
 public class DialogDirector extends DialogField
 {
     private SingleValueSource submitCaption;
     private SingleValueSource cancelCaption;
     private SingleValueSource pendingCaption;
     private SingleValueSource submitActionUrl;
     private SingleValueSource cancelActionUrl;
     private SingleValueSource pendingActionUrl;
 
     public DialogDirector()
     {
         this("director");
     }
 
     public DialogDirector(String name)
     {
         super(name, null);
         this.submitCaption = ValueSourceFactory.getSingleOrStaticValueSource("   OK   ");
         this.cancelCaption = ValueSourceFactory.getSingleOrStaticValueSource(" Cancel ");
     }
 
     public SingleValueSource getSubmitCaption()
     {
         return submitCaption;
     }
 
     public void setSubmitCaption(String value)
     {
         submitCaption = (value != null && value.length() > 0) ? ValueSourceFactory.getSingleOrStaticValueSource(value) : null;
     }
 
     public SingleValueSource getCancelCaption()
     {
         return cancelCaption;
     }
 
     public void setCancelCaption(String value)
     {
         cancelCaption = (value != null && value.length() > 0) ? ValueSourceFactory.getSingleOrStaticValueSource(value) : null;
     }
 
     public SingleValueSource getPendingCaption()
     {
         return pendingCaption;
     }
 
     public void setPendingCaption(String value)
     {
         this.pendingCaption = (value != null && value.length() > 0) ? ValueSourceFactory.getSingleOrStaticValueSource(value) : null;
     }
 
     public SingleValueSource getSubmitActionUrl()
     {
         return submitActionUrl;
     }
 
     public void setSubmitActionUrl(String value)
     {
         submitActionUrl = (value != null && value.length() > 0) ? ValueSourceFactory.getSingleOrStaticValueSource(value) : null;
     }
 
     public SingleValueSource getCancelActionUrl()
     {
         return cancelActionUrl;
     }
 
     public void setCancelActionUrl(String value)
     {
         cancelActionUrl = (value != null && value.length() > 0) ? ValueSourceFactory.getSingleOrStaticValueSource(value) : null;
     }
 
     public SingleValueSource getPendingActionUrl()
     {
         return pendingActionUrl;
     }
 
     public void setPendingActionUrl(String value)
     {
         this.pendingActionUrl = (value != null && value.length() > 0) ? ValueSourceFactory.getSingleOrStaticValueSource(value) : null;
     }
 
     public void importFromXml(Element elem)
     {
         super.importFromXml(elem);
 
         String value = elem.getAttribute("style");
         if("data".equals(value))
             submitCaption = ValueSourceFactory.getSingleOrStaticValueSource("  Save  ");
         else if("confirm".equals(value))
         {
             submitCaption = ValueSourceFactory.getSingleOrStaticValueSource("  Yes  ");
             cancelCaption = ValueSourceFactory.getSingleOrStaticValueSource("  No   ");
         }
 
         value = elem.getAttribute("submit-caption");
         if(value != null && value.length() > 0)
             submitCaption = ValueSourceFactory.getSingleOrStaticValueSource(value);
 
         value = elem.getAttribute("cancel-caption");
         if(value != null && value.length() > 0)
             cancelCaption = ValueSourceFactory.getSingleOrStaticValueSource(value);
 
         value = elem.getAttribute("pending-caption");
         if(value != null && value.length() > 0)
         {
             pendingCaption = ValueSourceFactory.getSingleOrStaticValueSource(value);
             value = elem.getAttribute("pending-url");
             if(value.length() != 0)
                 this.setPendingActionUrl(value);
         }
 
         value = elem.getAttribute("submit-url");
         if(value.length() != 0)
             this.setSubmitActionUrl(value);
 
         value = elem.getAttribute("cancel-url");
         if(value.length() != 0)
             this.setCancelActionUrl(value);
     }
 
     public void renderControlHtml(Writer writer, DialogContext dc) throws IOException
     {
         String attrs = dc.getSkin().getDefaultControlAttrs();
 
         String submitCaption = this.submitCaption.getValue(dc);
         String cancelCaption = this.cancelCaption.getValue(dc);
 
         switch(dc.getDataCommand())
         {
             case DialogContext.DATA_CMD_ADD:
             case DialogContext.DATA_CMD_EDIT:
                 submitCaption = " Save ";
                 break;
 
             case DialogContext.DATA_CMD_DELETE:
                 submitCaption = " Delete ";
                 break;
 
             case DialogContext.DATA_CMD_CONFIRM:
                 submitCaption = "  Yes  ";
                 cancelCaption = "  No   ";
                 break;
         }
 
         writer.write("<center>");
         writer.write("<input type='submit' value='");
         writer.write(submitCaption);
         writer.write("' ");
         writer.write(attrs);
         writer.write(">&nbsp;&nbsp;");
 
         if(pendingCaption != null)
         {
             writer.write("<input type='submit' name='"+Dialog.PARAMNAME_IGNORE_VALIDATION+"' value='");
             writer.write(pendingCaption.getValue(dc));
             writer.write("' ");
             writer.write(attrs);
             writer.write(">&nbsp;&nbsp;");
         }
 
         writer.write("<input type='button' value='");
         writer.write(cancelCaption);
         writer.write("' ");
         if(cancelActionUrl == null)
         {
            String referer = dc.getOriginalReferer();
            if(referer != null)
            {
                writer.write("onclick=\"document.location = '");
                writer.write(referer);
                writer.write("'\" ");
            }
            else
            {
                writer.write("onclick=\"history.back()\" ");
            }
         }
         else
         {
             String cancelStr = cancelActionUrl != null ? cancelActionUrl.getValue(dc) : null;
             if("back".equals(cancelStr))
             {
                 writer.write("onclick=\"history.back()\" ");
             }
             else if(cancelStr != null && cancelStr.startsWith("javascript:"))
             {
                 writer.write("onclick=\"");
                 writer.write(cancelStr);
                 writer.write("\" ");
             }
             else
             {
                 writer.write("onclick=\"document.location = '");
                 writer.write(cancelStr);
                 writer.write("'\" ");
             }
         }
         writer.write(attrs);
         writer.write("></center>");
     }
 }
