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
 * $Id: AppLoginDialogSkin.java,v 1.3 2004-07-14 19:31:58 shahid.shah Exp $
  */
 
 package app;
 
 import java.io.Writer;
 import java.io.IOException;
 
import com.netspective.sparx.theme.basic.StandardLoginDialogSkin;
 import com.netspective.sparx.theme.Theme;
 import com.netspective.sparx.form.DialogContext;
 
public class AppLoginDialogSkin extends StandardLoginDialogSkin
 {
     public AppLoginDialogSkin(Theme theme, String name, String panelClassNamePrefix, String panelResourcesPrefix, boolean fullWidth)
     {
         super(theme, name, panelClassNamePrefix, panelResourcesPrefix, fullWidth);
     }
 
     public void renderHtml(Writer writer, DialogContext dc) throws IOException
     {
         String resourcesUrl = dc.getHttpRequest().getContextPath() + "/resources";
 
         writer.write("<title>Welcome to the Survey</title>");
         writer.write("</head>");
         writer.write("<body leftmargin='0' topmargin='0' marginwidth='0' marginheight='0'>\n");
         writer.write("<!-- BEGIN MASTHEAD -->\n");
         writer.write("<table width='100%' height='50' border='0' cellpadding='0' cellspacing='0' background='"+ resourcesUrl +"/top-back.gif'>\n");
         writer.write("  <tr>\n");
         writer.write("    <td><div align='center'><img src='"+ resourcesUrl +"/masthead.gif' width='520' height='50' alt='Risk Management'></div></td>\n");
         writer.write("    <td><div align='right'><img src='"+ resourcesUrl +"/marsh-logo.gif' width='126' height='50' alt='Marsh'></div></td>\n");
         writer.write("  </tr>\n");
         writer.write("</table>\n");
         writer.write("<!-- END MASTHEAD -->\n");
         writer.write("<center>");
         writer.write("<p>&nbsp;<p>");
 
         super.renderHtml(writer, dc);
     }
 }
