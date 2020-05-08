 /*
  * Copyright (c) 2000-2004 Netspective Communications LLC. All rights reserved.
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
  *    used to endorse or appear in products derived from The Software without written consent of Netspective.
  *
  * THE SOFTWARE IS PROVIDED "AS IS" WITHOUT A WARRANTY OF ANY KIND. ALL EXPRESS OR IMPLIED REPRESENTATIONS AND
  * WARRANTIES, INCLUDING ANY IMPLIED WARRANTY OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE OR NON-INFRINGEMENT,
  * ARE HEREBY DISCLAIMED.
  *
  * NETSPECTIVE AND ITS LICENSORS SHALL NOT BE LIABLE FOR ANY DAMAGES SUFFERED BY LICENSEE OR ANY THIRD PARTY AS A
  * RESULT OF USING OR DISTRIBUTING THE SOFTWARE. IN NO EVENT WILL NETSPECTIVE OR ITS LICENSORS BE LIABLE FOR ANY LOST
  * REVENUE, PROFIT OR DATA, OR FOR DIRECT, INDIRECT, SPECIAL, CONSEQUENTIAL, INCIDENTAL OR PUNITIVE DAMAGES, HOWEVER
  * CAUSED AND REGARDLESS OF THE THEORY OF LIABILITY, ARISING OUT OF THE USE OF OR INABILITY TO USE THE SOFTWARE, EVEN
  * IF IT HAS BEEN ADVISED OF THE POSSIBILITY OF SUCH DAMAGES.
  */
 package com.netspective.sparx.theme.sampler;
 
 import java.io.IOException;
 import java.io.Writer;
 
 import com.netspective.commons.Product;
 import com.netspective.commons.security.AuthenticatedOrganization;
 import com.netspective.commons.security.AuthenticatedUser;
 import com.netspective.commons.value.ValueSource;
 import com.netspective.commons.value.ValueSourceSpecification;
 import com.netspective.commons.value.source.StaticValueSource;
 import com.netspective.sparx.ProjectComponent;
 import com.netspective.sparx.navigate.NavigationContext;
 import com.netspective.sparx.theme.Theme;
 import com.netspective.sparx.theme.console.ConsoleNavigationSkin;
 import com.netspective.sparx.value.source.PageIdValueSource;
 
 public class SamplerNavigationSkin extends ConsoleNavigationSkin
 {
     private boolean showProductVersion = false;
     private ValueSource productVersionHref;
     private ValueSource activeUserHref = new StaticValueSource("#");
     private ValueSource activeUserOrgHref = new StaticValueSource("#");
 
     public SamplerNavigationSkin(Theme theme, String name)
     {
         super(theme, name);
         productVersionHref = new PageIdValueSource();
         productVersionHref.initialize(new ValueSourceSpecification("page-id:/about"));
     }
 
     public boolean isShowProductVersion()
     {
         return showProductVersion;
     }
 
     public void setShowProductVersion(boolean showProductVersion)
     {
         this.showProductVersion = showProductVersion;
     }
 
     public ValueSource getProductVersionHref()
     {
         return productVersionHref;
     }
 
     public void setProductVersionHref(ValueSource productVersionHref)
     {
         this.productVersionHref = productVersionHref;
     }
 
     public ValueSource getActiveUserHref()
     {
         return activeUserHref;
     }
 
     public void setActiveUserHref(ValueSource activeUserHref)
     {
         this.activeUserHref = activeUserHref;
     }
 
     public ValueSource getActiveUserOrgHref()
     {
         return activeUserOrgHref;
     }
 
     public void setActiveUserOrgHref(ValueSource activeUserOrgHref)
     {
         this.activeUserOrgHref = activeUserOrgHref;
     }
 
     /**
      * Render the authenticated user information and the logout navigation link
      */
     public void renderAuthenticatedUser(Writer writer, NavigationContext nc) throws IOException
     {
         AuthenticatedUser authUser = nc.getAuthenticatedUser();
 
         String personId = authUser != null ? authUser.getUserId().toString() : "Not logged in";
         String personName = authUser != null ? authUser.getUserName() : "Not logged in";
        AuthenticatedOrganization authOrg = authUser.getOrganizations().getPrimaryOrganization();
 
         if(authUser != null && authUser.isRemembered())
             personName += " (remembered)";
 
         Object orgId = null;
         String orgName = null;
         String orgLabel = null;
         if(authOrg != null)
         {
             orgId = authOrg.getOrgId();
             orgName = authOrg.getOrgName();
             orgLabel = authOrg.getOrgLabel();
         }
 
         Theme theme = getTheme();
 
         writer.write("<!-- Active User Begins -->\n");
         writer.write("<table class=\"active-user-table\" width=\"100%\" border=\"0\" cellspacing=\"0\" cellpadding=\"0\">\n");
         writer.write("<tr>\n");
         writer.write("	<td><img src=\"" + theme.getResourceUrl("/images/spacer.gif") + "\" alt=\"\" height=\"100%\" width=\"10\" border=\"0\"></td>\n");
 
         if(isShowProductVersion())
         {
             final Product product = nc.getProject().getProduct();
             writer.write("	<td nowrap><span class=\"active-user-heading\">&nbsp;<a class=\"active-user\" href=\"" + productVersionHref.getTextValue(nc) + "\" title=\"" + product.getProductName() + " " + product.getVersionAndBuild() + "\">&nbsp;&nbsp;" + product.getVersionAndBuildShort() + "</a></span></td>");
             writer.write("	<td><img src=\"" + theme.getResourceUrl("/images/spacer.gif") + "\" alt=\"\" height=\"100%\" width=\"20\" border=\"0\"></td>\n");
         }
 
         writer.write("	<td valign=\"middle\" nowrap >\n");
         writer.write("		<table border=\"0\" cellspacing=\"0\" cellpadding=\"0\">\n");
         writer.write("			<tr>\n");
         writer.write("				<td class=\"active-user-anchor\"><img class=\"active-user-anchor\" src=\"" + theme.getResourceUrl("/images/spacer.gif") + "\" alt=\"\" " +
                      "height=\"100%\" width=\"100%\" border=\"0\"></td>\n");
         writer.write("				<td nowrap><span class=\"active-user-heading\">&nbsp;User&nbsp;</span></td>\n");
         writer.write("				<td nowrap><a class=\"active-user\" href=\"" + activeUserHref.getTextValue(nc) + "\" title=\"User ID is '" + personId + "'\">&nbsp;&nbsp;" +
                      personName + "</a></td>\n");
         writer.write("			</tr>\n");
         writer.write("		</table>\n");
         writer.write("	</td>\n");
 
         ProjectComponent projectManager = nc.getProjectComponent();
         boolean haveErrors = projectManager.hasErrorsOrWarnings();
 
         if(orgName != null && !orgName.equals(personName))
         {
             writer.write("	<td><img src=\"" + theme.getResourceUrl("/images/spacer.gif") + "\" alt=\"\" height=\"100%\" width=\"20\" border=\"0\"></td>\n");
             writer.write(haveErrors ? "	<td>\n" : "	<td width=100%>\n");
             writer.write("		<table border=\"0\" cellspacing=\"0\" cellpadding=\"0\">\n");
             writer.write("			<tr>\n");
             writer.write("				<td class=\"active-user-anchor\"><img class=\"active-user-anchor\" src=\"" + theme.getResourceUrl("/images/spacer.gif") + "\" alt=\"\" height=\"100%\" width=\"100%\" border=\"0\"></td>\n");
             writer.write("				<td nowrap><span class=\"active-user-heading\">&nbsp;" + orgLabel + "&nbsp;</span></td>\n");
             writer.write("				<td nowrap><a class=\"active-user\" href=\"" + activeUserOrgHref.getTextValue(nc) + "\" title=\"" + orgLabel + " ID is '" + orgId + "'\">&nbsp;&nbsp;" + orgName + "</a></td>\n");
             writer.write("			</tr>\n");
             writer.write("		</table>\n");
             writer.write("	</td>\n");
         }
 
         if(haveErrors)
         {
             int errorsCount = projectManager.getErrors().size() + projectManager.getWarnings().size();
 
             writer.write("	<td><img src=\"" + theme.getResourceUrl("/images/spacer.gif") + "\" alt=\"\" height=\"100%\" width=\"20\" border=\"0\"></td>\n");
             writer.write("	<td width=\"100%\">\n");
 
             writer.write("		<table border=\"0\" cellspacing=\"0\" cellpadding=\"0\">\n");
             writer.write("			<tr>\n");
             writer.write("				<td class=\"error-alert-anchor\"><img class=\"error-alert-anchor\" src=\"" + theme.getResourceUrl("/images/spacer.gif") + "\" alt=\"\" height=\"100%\" width=\"100%\" border=\"0\"></td>\n");
             writer.write("				<td nowrap><a class=\"error-alert\" href=\"" + nc.getServletRootUrl() + "/project/input-source#errors\"><span class=\"error-alert-heading\">&nbsp;Errors/Warnings&nbsp;</span></a></td>\n");
             writer.write("				<td nowrap><a class=\"error-alert\" href=\"" + nc.getServletRootUrl() + "/project/input-source#errors\">&nbsp;&nbsp;" +
                          errorsCount + "</a></td>\n");
             writer.write("			</tr>\n");
             writer.write("		</table>\n");
             writer.write("	</td>\n");
         }
 
         writer.write("	<td nowrap width=\"50\" >\n");
         writer.write("		<table border=\"0\" cellspacing=\"0\" cellpadding=\"0\">\n");
         writer.write("			<tr>\n");
         writer.write("				<td class=\"active-user-anchor\"><img class=\"active-user-anchor\" src=\"" +
                      theme.getResourceUrl("/images/spacer.gif") + "\" alt=\"\" height=\"100%\" width=\"100%\" border=\"0\"></td>\n");
         writer.write("				<td nowrap><span class=\"active-user-heading\">&nbsp;Action&nbsp;</span></td>\n");
         writer.write("				<td nowrap><a class=\"active-user\" href=\"" + nc.getRootUrl() + "?_logout=yes\">&nbsp;&nbsp;Logout&nbsp;</a></td>\n");
         writer.write("			</tr>\n");
         writer.write("		</table>\n");
         writer.write("	</td>\n");
         writer.write("	<td><img src=\"" + theme.getResourceUrl("/images/spacer.gif") + "\" alt=\"\" height=\"100%\" width=\"20\" border=\"0\"></td>\n");
         writer.write("</tr>\n");
         writer.write("</table>\n");
 
         writer.write("<!-- Active User Ends -->\n");
     }
 }
