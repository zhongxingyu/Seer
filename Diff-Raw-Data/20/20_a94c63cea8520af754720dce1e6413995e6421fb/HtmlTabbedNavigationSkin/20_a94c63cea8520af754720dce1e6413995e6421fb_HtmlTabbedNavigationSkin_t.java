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
 * $Id: HtmlTabbedNavigationSkin.java,v 1.3 2003-03-03 07:16:14 aye.thu Exp $
  */
 
 package com.netspective.sparx.xaf.theme;
 
 import com.netspective.sparx.xaf.navigate.NavigationPage;
 import com.netspective.sparx.xaf.navigate.NavigationPath;
 import com.netspective.sparx.xaf.navigate.NavigationPathContext;
 import com.netspective.sparx.xaf.navigate.NavigationPathSkin;
 import com.netspective.sparx.xaf.navigate.NavigationTree;
 import com.netspective.sparx.xaf.security.AuthenticatedUser;
 import com.netspective.sparx.xaf.skin.SkinFactory;
 import org.w3c.dom.Element;
 
 import javax.servlet.Servlet;
 import java.io.IOException;
 import java.io.Writer;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 
 public class HtmlTabbedNavigationSkin implements NavigationPathSkin
 {
     static public final String HEADING_ACTION_IMAGE = "action-icon";
     static public final String HEADING_ENTITY_IMAGE = "entity-icon";
     static public final String HEADING_BACKGROUND_IMAGE = "heading-background";
     static public final String HEADING_MIDDLE_IMAGE = "heading-middle";
     static public final String TAB_SEPARATOR_IMAGE = "tab-separator";
     static public final String TAB_ON_IMAGE = "tab-on";
     static public final String TAB_OFF_IMAGE = "tab-off";
 
     protected NavigationStyle levelOneStyle;
     protected NavigationStyle levelTwoStyle;
     protected NavigationStyle levelThreeStyle;
     protected String cssFileName;
     protected String headerMarkerImageFileName;
     protected String headerSpacerImageFileName;
     protected String level1DividerImageFileName;
 
     protected String themeResourcesBasePath;
     protected String themeNavigationCssPath;
     protected String themeImagePath;
 
     protected String pageResourcesBasePath;
 
     protected String altSkinResourcesBasePath;
 
     protected String appRootPath;
     protected Theme theme;
 
     public HtmlTabbedNavigationSkin()
     {
         levelOneStyle = new Level1Style();
         levelTwoStyle = new Level2Style();
         levelThreeStyle = new Level3Style();
     }
 
     /**
      *
      * @param pageId
      * @param imageId
      * @param nc
      * @return
      */
     public String getImagePath(String pageId, String imageId, NavigationPathContext nc)
     {
         NavigationTree tree = nc.getOwnerTree();
         if (tree.getResources() == null)
         {
             // get all the image resources available with each page
             tree.discoverResources(appRootPath, getThemeImagePath() + "/pages", null);
             tree.resolveResources();
         }
 
         Map singlePageResources = (Map) tree.getResources().get(pageId);
         return (String) singlePageResources.get(imageId);
     }
 
     /**
      * Get the image path of the theme/style combination
      * @return
      */
     public String getThemeImagePath()
     {
         if (themeImagePath == null)
         {
             themeImagePath = theme.getCurrentStyle().getImagePath();
         }
         return themeImagePath;
     }
 
     public String getHeaderMarkerImageFileName()
     {
         return headerMarkerImageFileName;
     }
 
     public void setHeaderMarkerImageFileName(String headerMarkerImageFileName)
     {
         this.headerMarkerImageFileName = headerMarkerImageFileName;
     }
 
     public String getHeaderSpacerImageFileName()
     {
         return headerSpacerImageFileName;
     }
 
     public void setHeaderSpacerImageFileName(String headerSpacerImageFileName)
     {
         this.headerSpacerImageFileName = headerSpacerImageFileName;
     }
 
     public String getLevel1DividerImageFileName()
     {
         return level1DividerImageFileName;
     }
 
     public void setLevel1DividerImageFileName(String level1DividerImageFileName)
     {
         this.level1DividerImageFileName = level1DividerImageFileName;
     }
 
     /**
      * Create a context that can be used to render this navigation skin. Also get the current selected theme
      * in this context.
      * @param jspPageContext
      * @param tree
      * @param navTreeId
      * @param popup
      * @return
      */
     public NavigationPathContext createContext(javax.servlet.jsp.PageContext jspPageContext, NavigationTree tree, String navTreeId, boolean popup)
     {
         NavigationPathContext result = new NavigationPathContext(tree,
                 jspPageContext.getServletContext(),
                 (Servlet) jspPageContext.getPage(),
                 jspPageContext.getRequest(),
                 jspPageContext.getResponse(),
                 this, navTreeId);
         if (popup) result.setPopup(true);
 
         // associate a theme with this context
         theme = SkinFactory.getInstance().getCurrentTheme(result);
         return result;
     }
 
     public void renderPageMetaData(Writer writer, NavigationPathContext nc) throws IOException
     {
         NavigationPath activePath = nc.getActivePath();
 
         writer.write("<!-- Application Header Begins -->\n");
         writer.write("<html>\n");
         writer.write("<head>\n");
         writer.write("<title>" + (activePath != null ? activePath.getTitle(nc) : "") + "</title>\n");
         if (theme != null)
         {
             // get all the CSS files associated with this theme/style combination
             ThemeStyle style = theme.getCurrentStyle();
             Map cssResources = style.getCssResources();
             Iterator it = cssResources.values().iterator();
             while (it.hasNext())
             {
                 String css = (String) it.next();
                 writer.write("	<link rel=\"stylesheet\" href=\"" + nc.getRootUrl() + css + "\" type=\"text/css\">\n");
             }
         }
         //writer.write("	<link rel=\"stylesheet\" href=\"" + nc.getRootUrl() + cssFileName + "\" type=\"text/css\">\n");
         writer.write("</head>\n");
     }
 
     /**
      * Render the authenticated user information and the logout navigation link
      * @param writer
      * @param nc
      * @throws IOException
      */
     public void renderAuthenticatedUser(Writer writer, NavigationPathContext nc) throws IOException
     {
         AuthenticatedUser authUser = (AuthenticatedUser) nc.getSession().getAttribute("authenticated-user");
         if (authUser != null)
         {
             String personName = authUser != null ? authUser.getUserId() : "Not logged in";
             String personId = authUser != null ? authUser.getUserName() : "Not logged in";
             String orgName = authUser != null ? authUser.getUserOrgId() : "Not logged in";
             String orgId = authUser != null ? authUser.getUserOrgName() : "Not logged in";
 
             writer.write("<!-- Active User Begins -->\n");
             writer.write("<table class=\"active-user-table\" width=\"100%\" border=\"0\" cellspacing=\"0\" cellpadding=\"0\">\n");
             writer.write("<tr>\n");
             writer.write("	<td><img src=\"" + nc.getRootUrl() +  getThemeImagePath() + "/spacer.gif\" alt=\"\" height=\"100%\" width=\"10\" border=\"0\"></td>\n");
             writer.write("	<td valign=\"middle\" nowrap >\n");
             writer.write("		<table border=\"0\" cellspacing=\"0\" cellpadding=\"0\">\n");
             writer.write("			<tr>\n");
             writer.write("				<td class=\"active-user-anchor\"><img class=\"active-user-anchor\" src=\"" + nc.getRootUrl() +  getThemeImagePath() +
                     "/spacer.gif\" alt=\"\" " +
                     "height=\"100%\" width=\"100%\" border=\"0\"></td>\n");
             writer.write("				<td nowrap><span class=\"active-user-heading\">&nbsp;User&nbsp;</span></td>\n");
             writer.write("				<td nowrap><a class=\"active-user\" href=\"" + nc.getRootUrl() + "/person/summary.jsp?person_id=" + personId + "\">&nbsp;&nbsp;" +
                     personName.toUpperCase() + "</a></td>\n");
             writer.write("			</tr>\n");
             writer.write("		</table>\n");
             writer.write("	</td>\n");
             writer.write("	<td><img src=\"" + nc.getRootUrl() + getThemeImagePath() + "/spacer.gif\" alt=\"\" height=\"100%\" width=\"20\" border=\"0\"></td>\n");
             writer.write("	<td width=\"100%\">\n");
             if (orgName != null)
             {
                 writer.write("		<table border=\"0\" cellspacing=\"0\" cellpadding=\"0\">\n");
                 writer.write("			<tr>\n");
                 writer.write("				<td class=\"active-user-anchor\"><img class=\"active-user-anchor\" src=\"" +
                         getThemeImagePath() + "/spacer.gif\" alt=\"\" height=\"100%\" width=\"100%\" border=\"0\"></td>\n");
                 writer.write("				<td nowrap><span class=\"active-user-heading\">&nbsp;Org&nbsp;</span></td>\n");
                 writer.write("				<td nowrap><a class=\"active-user\" href=\"" + nc.getRootUrl() + "/org/summary.jsp?org_id=" + orgId + "\">" +
                         orgName.toUpperCase() + "</a></td>\n");
                 writer.write("			</tr>\n");
                 writer.write("		</table>\n");
             }
             else
             {
                 writer.write("<img src=\"" + nc.getRootUrl() + getThemeImagePath() + "/spacer.gif\" alt=\"\" height=\"1\" width=\"100%\" border=\"0\">");
             }
             writer.write("	</td>\n");
             writer.write("	<td nowrap width=\"50\" >\n");
             writer.write("		<table border=\"0\" cellspacing=\"0\" cellpadding=\"0\">\n");
             writer.write("			<tr>\n");
             writer.write("				<td class=\"active-user-anchor\"><img class=\"active-user-anchor\" src=\"" +
                     nc.getRootUrl() + getThemeImagePath() + "/spacer.gif\" alt=\"\" height=\"100%\" width=\"100%\" border=\"0\"></td>\n");
             writer.write("				<td nowrap><span class=\"active-user-heading\">&nbsp;Action&nbsp;</span></td>\n");
             writer.write("				<td nowrap><a class=\"active-user\" href=\"" + nc.getRootUrl() + "?_logout=yes\">&nbsp;&nbsp;Logout&nbsp;</a></td>\n");
             writer.write("			</tr>\n");
             writer.write("		</table>\n");
             writer.write("	</td>\n");
             writer.write("	<td><img src=\"" + nc.getRootUrl() + getThemeImagePath() + "/spacer.gif\" alt=\"\" height=\"100%\" width=\"20\" border=\"0\"></td>\n");
             writer.write("</tr>\n");
             writer.write("</table>\n");
 
             writer.write("<table width=\"100%\" border=\"0\" cellspacing=\"0\" cellpadding=\"0\">");
             writer.write("<tr>");
             writer.write("	<td><img src=\"" + nc.getRootUrl() + getThemeImagePath() + "/spacer.gif\" alt=\"\" height=\"1\" width=\"1\" border=\"0\"></td>");
             writer.write("</tr>");
             writer.write("</table>");
             writer.write("<!-- Active User Ends -->\n");
         }
     }
 
     public void renderPageMasthead(Writer writer, NavigationPathContext nc) throws IOException
     {
         writer.write("<body leftmargin=\"0\" marginheight=\"0\" marginwidth=\"0\" topmargin=\"0\">\n");
 
         renderAuthenticatedUser(writer, nc);
         writer.write("<!-- Master Header Begins -->\n");
         writer.write("<table class=\"masthead\" width=\"100%\" border=\"0\" cellspacing=\"0\" cellpadding=\"0\">\n");
         writer.write("	<tr>\n");
         writer.write("	    <td class=\"masthead-left\" valign=\"bottom\">\n");
         writer.write("	        <table border=\"0\" cellspacing=\"0\" cellpadding=\"0\">\n");
         writer.write("              <tr height=\"30\">\n");
         writer.write("	                <td align=\"left\" valign=\"middle\" height=\"30\">\n");
         writer.write("                      <table width=\"100%\" border=\"0\" cellspacing=\"0\" cellpadding=\"0\">\n");
         writer.write("                          <tr>\n");
         writer.write("                              <td align=\"left\" valign=\"middle\" width=\"10\">" +
                 "<img src=\"" + nc.getRootUrl() + getThemeImagePath() + "/spacer.gif\" width=\"10\" height=\"18\">" +
                 "</td>\n");
         //writer.write("                              <td class=\"masthead-logo\" align=\"left\" valign=\"middle\">" + (nc.getApplicationName(nc) == null ? "Application Name" : nc.getApplicationName(nc)) + "</td>\n");
         writer.write("                              <td class=\"masthead-logo\" align=\"left\" valign=\"middle\">" +
                 "<img src=\"" + nc.getRootUrl() + getThemeImagePath() + "/spacer.gif\" width=\"100%\" height=\"18\"></td>\n");
         writer.write("                          </tr>\n");
         writer.write("                      </table>\n");
         writer.write("                  </td>\n");
         writer.write("              </tr>\n");
         writer.write("<!-- Masthead Ends -->\n");
         writer.write("              <tr>\n");
         writer.write("                  <td align=\"left\" valign=\"bottom\">\n");
     }
 
     public void renderPageMenusLevelOne(Writer writer, NavigationPathContext nc) throws IOException
     {
         writer.write("<!-- Level 1 Begins -->\n");
         NavigationPath activePath = nc.getActivePath();
         levelOneStyle.renderHtml(writer,
                 activePath.getLevel() == 1 ? activePath : (NavigationPath) nc.getActivePath().getAncestorsList().get(1), nc);
         writer.write("<!-- Level 1 Ends -->\n");
 
         writer.write("                    </td>\n");
         writer.write("				</tr>\n");
         writer.write("			</table>\n");
         writer.write("		</td>\n");
         writer.write("		<td class=\"masthead-right\" align=\"right\" valign=\"bottom\" width=\"100%\">" +
                 "<img src=\"" + nc.getRootUrl() + getThemeImagePath() + "/spacer.gif\" width=\"100%\" height=\"32\"></td>\n");
         writer.write("	</tr>\n");
         writer.write("</table>\n");
     }
 
     /**
      * Generates the level two HTML
      * @param writer
      * @param nc
      * @throws IOException
      */
     public void renderPageMenusLevelTwo(Writer writer, NavigationPathContext nc) throws IOException
     {
         writer.write("<!-- Level Two Begins -->");
         NavigationPath activePath = nc.getActivePath();
         switch (activePath.getLevel())
         {
             case 1:
                 List activePathChildren = activePath.getChildrenList();
                 if (activePath.getMaxLevel() > 1 && activePathChildren.size() > 0)
                 {
                     levelTwoStyle.renderHtml(writer, (NavigationPath) activePath.getChildrenList().get(0), nc);
                 }
                 else
                 {
                     // even if there are no level two menu items display the level two background bar
                     writer.write("<table width=\"100%\" border=\"0\" cellspacing=\"0\" cellpadding=\"0\" height=\"10\">");
                     writer.write("	<tr>");
                     writer.write("	<td class=\"menu-level-2-table\" align=\"left\" valign=\"middle\">");
                     writer.write("	</td>");
                     writer.write("	</tr>");
                     writer.write("</table>");
                     /*
                     writer.write("<table width=\"100%\" border=\"0\" cellspacing=\"0\" cellpadding=\"0\">");
                     writer.write("  	<tr>");
                     writer.write("	<td class=\"menu-level-3-separator\" align=\"left\" valign=\"top\" width=\"150\">" +
                             "<img src=\"" + nc.getRootUrl() + getThemeImagePath() + "/body/spacer-big.gif\" alt=\"\" width=\"150\" height=\"12\" border=\"0\"></td>");
                     writer.write("	<td class=\"body-top-left\" width=\"12\"><img src=\"" + nc.getRootUrl() + getThemeImagePath() + "/body/spacer-big.gif\" alt=\"\" " +
                             "width=\"12\" height=\"12\" border=\"0\"></td>");
                     writer.write("	<td align=\"left\" valign=\"top\"><img src=\"" + nc.getRootUrl() + getThemeImagePath() + "/body/spacer-big.gif\" " +
                             "alt=\"\" height=\"12\" width=\"100%\" border=\"0\"></td>");
                     writer.write("	</tr>");
                     writer.write("</table>");
                     */
                 }
                 break;
 
             case 2:
                 levelTwoStyle.renderHtml(writer, activePath, nc);
                 break;
 
             case 3:
             case 4:
                 levelTwoStyle.renderHtml(writer, (NavigationPath) activePath.getAncestorsList().get(2), nc);
                 break;
 
         }
         writer.write("<!-- Level Two Ends -->");
     }
 
     public void renderPageMenusLevelThree(Writer writer, NavigationPathContext nc) throws IOException
     {
         NavigationPath activePath = nc.getActivePath();
         switch (activePath.getLevel())
         {
             case 2:
                 List activePathChildren = activePath.getChildrenList();
                 if (activePath.getMaxLevel() > 2 && activePathChildren.size() > 0)
                 {
                     writer.write("  <tr>\n");
                     writer.write("      <td class=\"menu-level3-separator\" align=\"left\" valign=\"top\" width=\"150\">" +
                         "<img src=\""+ nc.getRootUrl() + getThemeImagePath()+ "/spacer-big.gif\" alt=\"\" width=\"150\" height=\"12\" border=\"0\"></td>\n");
                     writer.write("      <td class=\"body-top-left\" width=\"12\"><img src=\""+ nc.getRootUrl() + getThemeImagePath()+ "/spacer-big.gif\" " +
                         "alt=\"\" width=\"12\" height=\"12\" border=\"0\"></td>\n");
                     writer.write("      <td align=\"left\" valign=\"top\"><img src=\""+ nc.getRootUrl() + getThemeImagePath()+
                         "/spacer-big.gif\" alt=\"\" height=\"12\" width=\"100%\" border=\"0\"></td>\n");
                     writer.write("  </tr>\n");
                     writer.write("  <tr>\n");
                     writer.write("      <td class=\"menu-table\" align=\"left\" valign=\"top\" width=\"150\" height=\"100%\">\n");
                     levelThreeStyle.renderHtml(writer, (NavigationPath) activePath.getChildrenList().get(0), nc);
                     writer.write("      </td>\n");
                 }
                 break;
 
             case 3:
                 writer.write("  <tr>\n");
                 writer.write("      <td class=\"menu-level3-separator\" align=\"left\" valign=\"top\" width=\"150\">" +
                     "<img src=\""+ nc.getRootUrl() + getThemeImagePath()+ "/spacer-big.gif\" alt=\"\" width=\"150\" height=\"12\" border=\"0\"></td>\n");
                 writer.write("      <td class=\"body-top-left\" width=\"12\"><img src=\""+ nc.getRootUrl() + getThemeImagePath()+ "/spacer-big.gif\" " +
                     "alt=\"\" width=\"12\" height=\"12\" border=\"0\"></td>\n");
                 writer.write("      <td align=\"left\" valign=\"top\"><img src=\""+ nc.getRootUrl() + getThemeImagePath()+
                     "/spacer-big.gif\" alt=\"\" height=\"12\" width=\"100%\" border=\"0\"></td>\n");
                 writer.write("  </tr>\n");
                 writer.write("  <tr>\n");
                 writer.write("      <td class=\"menu-table\" align=\"left\" valign=\"top\" width=\"150\" height=\"100%\">\n");
                 levelThreeStyle.renderHtml(writer, activePath, nc);
                 writer.write("      </td>\n");
                 break;
 
             case 4:
                 writer.write("  <tr>\n");
                 writer.write("      <td class=\"menu-level3-separator\" align=\"left\" valign=\"top\" width=\"150\">" +
                     "<img src=\""+ nc.getRootUrl() + getThemeImagePath()+ "/spacer-big.gif\" alt=\"\" width=\"150\" height=\"12\" border=\"0\"></td>\n");
                 writer.write("      <td class=\"body-top-left\" width=\"12\"><img src=\""+ nc.getRootUrl() + getThemeImagePath()+ "/spacer-big.gif\" " +
                     "alt=\"\" width=\"12\" height=\"12\" border=\"0\"></td>\n");
                 writer.write("      <td align=\"left\" valign=\"top\"><img src=\""+ nc.getRootUrl() + getThemeImagePath()+
                     "/spacer-big.gif\" alt=\"\" height=\"12\" width=\"100%\" border=\"0\"></td>\n");
                 writer.write("  </tr>\n");
                 writer.write("  <tr>\n");
                 writer.write("      <td class=\"menu-table\" align=\"left\" valign=\"top\" width=\"150\" height=\"100%\">\n");
                 levelThreeStyle.renderHtml(writer, (NavigationPath) activePath.getAncestorsList().get(3), nc);
                 writer.write("      </td>\n");
                 break;
             default:
                 writer.write("  <tr>\n");
                writer.write("      <td colspan=\"3\"><img src=\""+ nc.getRootUrl() + getThemeImagePath()+ "/spacer-big.gif\" " +
                    "alt=\"\" width=\"100%\" height=\"12\" border=\"0\"></td>\n");
                writer.write("  </tr>\n");
                writer.write("  <tr>\n");
                 break;
 
         }
     }
 
     public void renderPageHeader(Writer writer, NavigationPathContext nc) throws IOException
     {
         if (nc.isPopup())
             return;
 
         renderPageMasthead(writer, nc);
         renderPageMenusLevelOne(writer, nc);
         renderPageMenusLevelTwo(writer, nc);
 
         writer.write("<table width=\"100%\" border=\"0\" cellspacing=\"0\" cellpadding=\"0\" height=\"100%\">\n");
         renderPageMenusLevelThree(writer, nc);
 		writer.write("      <td align=\"left\" valign=\"top\" width=\"12\" height=\"100%\"></td>");
 		writer.write("      <td align=\"left\" valign=\"top\">");
 		writer.write("          <table width=\"100%\" border=\"0\" cellspacing=\"0\" cellpadding=\"0\">");
 		writer.write("              <tr>");
 		writer.write("                  <td align=\"left\" valign=\"top\" nowrap>");
 		writer.write("                  <div align=\"left\">");
         renderPageHeading(writer, nc);
 
     }
 
     /**
      * Renders the page heading if one exists
      * @param writer
      * @param nc
      * @throws IOException
      */
     private void renderPageHeading(Writer writer, NavigationPathContext nc) throws IOException
     {
 
         NavigationPage page = (NavigationPage) nc.getActivePath();
         String heading = page.getHeading(nc);
 
         String actionIcon = getImagePath(page.getId(), HEADING_ACTION_IMAGE, nc);
 
         writer.write("<!-- Page Header Begins -->\n<p>");
         writer.write("<table class=\"page-heading-table\" width=\"100%\" border=\"0\" cellspacing=\"0\" cellpadding=\"0\">\n");
         writer.write("    <tr>\n");
         writer.write("        <td align=\"left\" valign=\"middle\">\n");
         writer.write("            <table width=\"100%\" border=\"0\" cellspacing=\"0\" cellpadding=\"0\">\n");
         writer.write("                <tr>\n");
         if (actionIcon != null && actionIcon.length() > 0)
             writer.write("                    <td class=\"page-heading-icon\"><img class=\"page-icon\" src=\""+
                 nc.getRootUrl() + actionIcon + "\" alt=\"\" height=\"22\" width=\"22\" border=\"0\"></td>\n");
         else
             writer.write("                    <td class=\"page-heading-icon\"><img class=\"page-icon\" src=\""+
                 nc.getRootUrl() + getThemeImagePath()+ "/page-icons/home.gif\" alt=\"\" height=\"22\" width=\"22\" border=\"0\"></td>\n");
 
         writer.write("                    <td class=\"page-heading\">" + heading + "</td>\n");
         writer.write("                </tr>\n");
         writer.write("            </table>\n");
         writer.write("        </td>\n");
         writer.write("    </tr>\n");
         renderPageSubHeading(writer, nc);
         writer.write("</table>\n");
         writer.write("<!-- Page Header Ends -->\n<p>");
 
     }
 
     /**
      * Render the sub heading in the page content
      * @param writer
      * @param nc
      * @throws IOException
      */
     private void renderPageSubHeading(Writer writer, NavigationPathContext nc) throws IOException
     {
         NavigationPage page = (NavigationPage) nc.getActivePath();
         String subHeading = page.getSubHeading(nc);
 
         if (subHeading != null && subHeading.length() > 0)
         {
             writer.write("    <tr>\n");
             writer.write("        <td class=\"page-sub-heading-table\" align=\"left\" valign=\"top\">\n");
 
             writer.write("            <table width=\"100%\" border=\"0\" cellspacing=\"0\" cellpadding=\"0\">\n");
             writer.write("                <tr>\n");
             writer.write("                    <td align=\"left\" valign=\"top\">\n");
             writer.write("                        <table width=\"100%\" border=\"0\" cellspacing=\"0\" cellpadding=\"0\">\n");
             writer.write("                            <tr>\n");
             writer.write("                                <td align=\"left\" valign=\"middle\">\n");
             writer.write("                                    <p><span class=\"page-sub-heading\">" + subHeading + "</p>\n");
             writer.write("                                </td>\n");
             writer.write("                            </tr>\n");
 
             writer.write("                        </table>\n");
             writer.write("                    </td>\n");
             writer.write("                </tr>\n");
             writer.write("            </table>\n");
             writer.write("        </td>\n");
             writer.write("    </tr>\n");
 
         }
     }
 
     /**
      * Render the page content footer
      * @param writer
      * @param nc
      * @throws IOException
      */
     public void renderPageFooter(Writer writer, NavigationPathContext nc) throws IOException
     {
         if (nc.isPopup())
             return;
 
         String sparxResourcesUrl = nc.getRootUrl() + "/sparx/resources";
         writer.write("            </td>");
 		writer.write("          </tr>");
 		writer.write("	    </table>");
 		writer.write("		</td>");
 		writer.write("	</tr>");
 		writer.write("</table>");
         /*
         writer.write("   </TD></TR>");
         writer.write("   <tr><td valign=bottom align=center class=\"page_content\"><table cellpadding=3>");
         writer.write("        <tr>");
         writer.write("                <td align=center class=\"power_by_sparx_footer\"> ");
         writer.write("                        <a target=\"netspective\" href=\"http://www.netspective.com/\"> ");
         writer.write("                        <img border=\"0\" alt=\"Powered by Netspective Sparx\" src=\"" + sparxResourcesUrl + "/images/powered-by-sparx.gif\">");
         writer.write("                        </a>");
         writer.write("                </td> ");
         writer.write("                <td align=center class=\"copyright_footer\"> ");
         writer.write("                        <a href='" + nc.getRootUrl() + "/ace'>" + BuildConfiguration.getVersionAndBuild() + "</a>. &copy; Netspective.");
         writer.write("                </td> ");
         writer.write("        </tr></table>");
         writer.write("   </td></tr></table>");
         */
         writer.write("</body>");
     }
 
     abstract public class NavigationStyle
     {
         static public final int NAVFLAG_VERTICAL_DISPLAY = 1;
         static public final int NAVFLAG_USE_IMAGES = NAVFLAG_VERTICAL_DISPLAY * 2;
         static public final int NAVFLAG_EXPAND_MARGIN_LEFT = NAVFLAG_USE_IMAGES * 2;
         static public final int NAVFLAG_EXPAND_MARGIN_RIGHT = NAVFLAG_EXPAND_MARGIN_LEFT * 2;
 
         protected String tableAttrs;
         protected String tableClass;
 
         protected String innerSeparatorAttrs;
         protected String innerSeparatorClass;
         protected String innerSeparatorImgClass;
         protected String innerSeparatorImgAttrs;
 
         protected String outerSeparatorAttrs;
         protected String outerSeparatorClass;
         protected String outerSeparatorImgClass;
         protected String outerSeparatorImgAttrs;
 
         protected String containerAttrs;
         protected String containerClass;
 
         protected String navAttrs;
         protected String navClass;
         protected String navImgAttrs;
         protected String navLinkAttrs;
         protected String navLinkClass;
 
         protected String prependHtml;
         protected String appendHtml;
 
         protected long flags;
 
         public NavigationStyle()
         {
             this.tableAttrs = "cellspacing=\"0\" cellpadding=\"0\" border=\"0\" width=\"100%\"";
             this.tableClass = "class=\"function_tabs\"";
 
             this.outerSeparatorAttrs = "colspan=\"50\" width=\"100%\"";
             this.outerSeparatorClass = "";
             this.outerSeparatorImgAttrs = "height=2 width=10 border=0 alt=\"\"";
             this.outerSeparatorImgClass = "";
 
             this.innerSeparatorAttrs = "";
             this.innerSeparatorClass = "";
             this.innerSeparatorImgAttrs = "height=8 width=10 border=0 alt=\"\"";
             this.innerSeparatorImgClass = "";
 
             this.containerAttrs = "";
             this.containerClass = "";
 
             this.navAttrs = "nowrap";
             this.navClass = "class=\"function_tab_";
             this.navImgAttrs = "";
 
             this.navLinkAttrs = "";
             this.navLinkClass = "class=\"tab_navigation_";
 
             this.flags = NAVFLAG_VERTICAL_DISPLAY | NAVFLAG_EXPAND_MARGIN_RIGHT;
         }
 
         public void importFromXml(Element element)
         {
         }
 
         public final boolean flagIsSet(long flag)
         {
             return (flags & flag) == 0 ? false : true;
         }
 
 
         public final void setFlag(long flag)
         {
             flags |= flag;
         }
 
         public final void clearFlag(long flag)
         {
             flags &= ~flag;
         }
 
         public final void updateFlag(long flag, boolean set)
         {
             if (set) flags |= flag; else flags &= ~flag;
         }
 
         abstract public void renderHtml(Writer writer, NavigationPath currentNavTree, NavigationPathContext nc) throws IOException;
     }
 
     public class Level1Style extends NavigationStyle
     {
         public Level1Style()
         {
             this.navAttrs = "valign=\"bottom\" nowrap";
             this.navClass = "class=\"menu-level1-tab-";
             this.navImgAttrs = "";
 
             this.navLinkAttrs = "";
             this.navLinkClass = "class=\"menu-level1-";
 
             this.flags = NAVFLAG_EXPAND_MARGIN_RIGHT;
         }
 
         public void renderHtml(Writer writer, NavigationPath currentNavTree, NavigationPathContext nc) throws IOException
         {
             List tabElements = currentNavTree.getSibilingList();
 
             if (tabElements == null || tabElements.isEmpty())
             {
                 return;
             }
             writer.write("            <table width=\"100%\" cellspacing=\"0\" cellpadding=\"0\" border=\"0\">\n");
             writer.write("                <tr>\n");
             writer.write("                <td class=\"menu-level1-start\" valign=\"bottom\" nowrap><img src=\"" +
                     nc.getRootUrl()  + getThemeImagePath() + "/login/spacer.gif\" width=\"10\" height=\"10\"></td>");
             for (int i = 0; i < tabElements.size(); i++)
             {
                 NavigationPath tabElement = (NavigationPath) tabElements.get(i);
                 if (!nc.flagIsSet(tabElement.getId(), NavigationPath.NAVGPATHFLAG_INVISIBLE) &&
                         !nc.flagIsSet(tabElement.getId(), NavigationPath.NAVGPATHFLAG_HIDDEN))
                 {
                     if ( i == 0)
                     {
                         if (tabElement.isInActivePath(nc))
                         {
                             writer.write("                <td class=\"menu-level1-tab-start-on\" valign=\"bottom\" nowrap>" +
                                     "<span class=\"menu-level-1\">&nbsp;&nbsp;&nbsp;</span></td>");
                             writer.write("                    <td " + navClass + "on\" " + navAttrs + ">");
                             writer.write("<a " + navLinkAttrs + " " + navLinkClass + "on" +
                                     "\" href=\"" + tabElement.getUrl(nc) + "\">" + tabElement.getCaption(nc) + "&nbsp;&nbsp;</a></td>\n");
                         }
                         else
                         {
                             writer.write("                <td class=\"menu-level1-tab-start-off\" valign=\"bottom\" nowrap>" +
                                     "<span class=\"menu-level-1\">&nbsp;&nbsp;&nbsp;</span></td>");
                             writer.write("                    <td " + navClass + "off\" " + navAttrs + ">");
                             writer.write("<a " + navLinkAttrs + " " + navLinkClass + "off" +
                                     "\" href=\"" + tabElement.getUrl(nc) + "\">" + tabElement.getCaption(nc) + "&nbsp;&nbsp;</a></td>\n");
                         }
                     }
                     else
                     {
                         if (tabElement.isInActivePath(nc))
                         {
                                writer.write("                    <td class=\"menu-level1-tab-end-on\" valign=\"bottom\" nowrap><span class=\"menu-level-1\">" +
                                     "&nbsp;&nbsp;&nbsp;</span></td>\n");
 
                             writer.write("                    <td " + navClass + "on\" " + navAttrs + ">");
                             writer.write("<a " + navLinkAttrs + " " + navLinkClass + "on" +
                                     "\" href=\"" + tabElement.getUrl(nc) + "\">" + tabElement.getCaption(nc) + "&nbsp;&nbsp;</a></td>\n");
                         }
                         else
                         {
                                writer.write("                    <td class=\"menu-level1-tab-end-off\" valign=\"bottom\" nowrap><span class=\"menu-level-1\">" +
                                     "&nbsp;&nbsp;&nbsp;</span></td>\n");
 
                             writer.write("                    <td " + navClass + "off\" " + navAttrs + ">");
                             writer.write("<a " + navLinkAttrs + " " + navLinkClass + "off" +
                                     "\" href=\"" + tabElement.getUrl(nc) + "\">" + tabElement.getCaption(nc) + "&nbsp;&nbsp;</a></td>\n");
                         }
                     }
                 }
             }
             writer.write("              <td class=\"menu-level1-end\" valign=\"bottom\" nowrap><span class=\"menu-level1\">&nbsp;</span></td>");
             //writer.write("              <td class=\"menu-level1-fill\" align=\"left\" valign=\"top\" width=\"100%\">" +
             //        "<img src=\""+ nc.getRootUrl() + getThemeImagePath()+ "/spacer.gif\"  width=\"100%\"></td>\n");
             writer.write("               </tr>\n");
             writer.write("           </table>\n");
         }
     }
 
 
     public class Level2Style extends NavigationStyle
     {
         public Level2Style()
         {
             this.tableAttrs = "cellspacing=\"0\" cellpadding=\"0\" border=\"0\" width=\"100%\" height=\"27\"";
             this.tableClass = "class=\"menu-level2-table\"";
 
             this.outerSeparatorAttrs = "colspan=\"50\" width=\"100%\"";
             this.outerSeparatorClass = "";
             this.outerSeparatorImgAttrs = "height=2 width=10 border=0 alt=\"\"";
             this.outerSeparatorImgClass = "";
 
             this.innerSeparatorAttrs = "align=\"center\" nowrap";
             this.innerSeparatorClass = "";
             this.innerSeparatorImgAttrs = "height=8 width=10 border=0 alt=\"\"";
             this.innerSeparatorImgClass = "";
 
             this.containerAttrs = "";
             this.containerClass = "";
 
             this.navAttrs = "nowrap";
             this.navClass = "class=\"menu-level2-tab-";
             this.navImgAttrs = "";
 
             this.navLinkAttrs = "";
             this.navLinkClass = "class=\"menu-level2-";
 
             this.flags = NAVFLAG_EXPAND_MARGIN_RIGHT;
         }
 
         public void renderHtml(Writer writer, NavigationPath currentNavTree, NavigationPathContext nc) throws IOException
         {
             List tabElements = currentNavTree.getSibilingList();
 
             if (tabElements == null || tabElements.isEmpty())
             {
                 return;
             }
 
             writer.write("<table " + tableAttrs + ">\n");
             writer.write("<tr>\n");
             writer.write("	<td " + tableClass + " align=\"left\" valign=\"middle\">\n");
             writer.write("		<table border=\"0\" cellspacing=\"0\" cellpadding=\"0\">\n");
             writer.write("			<tr>\n");
             writer.write("			    <td align=\"center\" nowrap><a class=\"menu-level2\">&nbsp;&nbsp;</a></td>\n");
 
             for (int i = 0; i < tabElements.size(); i++)
             {
                 NavigationPath tabElement = (NavigationPath) tabElements.get(i);
                 if (!nc.flagIsSet(tabElement.getId(), NavigationPath.NAVGPATHFLAG_INVISIBLE) &&
                         !nc.flagIsSet(tabElement.getId(), NavigationPath.NAVGPATHFLAG_HIDDEN))
                 {
                     writer.write("<td " + navAttrs + " " + navClass + (tabElement.isInActivePath(nc) ? "on" : "off") + "\">");
                    writer.write("<a " + navLinkAttrs + " " + navLinkClass + (tabElement.isInActivePath(nc) ? "on" : "off") +
                            "\" href=\"" + tabElement.getUrl(nc) + "\">&nbsp;&nbsp;" + tabElement.getCaption(nc) + "&nbsp;&nbsp;</a></TD>\n");
                 }
             }
            //writer.write("			    <td align=\"center\" nowrap><a class=\"menu-level2\">&nbsp;&nbsp;</a></td>\n");
             writer.write("            </tr>\n");
             writer.write("        </table>\n");
             writer.write("    </td>\n");
             writer.write("</tr>\n");
             writer.write("</table>\n");
         }
     }
 
     public class Level3Style extends NavigationStyle
     {
         public Level3Style()
         {
             flags = NavigationStyle.NAVFLAG_VERTICAL_DISPLAY;
             tableAttrs = "width=\"100%\" border=\"0\" cellspacing=\"0\" cellpadding=\"0\" height=\"100%\"";
             tableClass = "";
             navAttrs = "align=\"left\" valign=\"middle\"";
             navClass = "class=\"menu-level3-tab-";
             navLinkClass = "class=\"menu-level3\"";
         }
 
         public void renderHtml(Writer writer, NavigationPath currentNavTree, NavigationPathContext nc) throws IOException
         {
             List sideBarElements = currentNavTree.getSibilingList();
             if (sideBarElements == null || sideBarElements.isEmpty())
             {
                 return;
             }
 
 			writer.write("      <!-- Level 3 Begins -->\n");
 			writer.write("      <table "+ tableClass + " " + tableAttrs + " >\n");
 
             for (int i = 0; i < sideBarElements.size(); i++)
             {
                 NavigationPath sideBarElement = (NavigationPath) sideBarElements.get(i);
                 if (!nc.flagIsSet(sideBarElement.getId(), NavigationPath.NAVGPATHFLAG_INVISIBLE) &&
                         !nc.flagIsSet(sideBarElement.getId(), NavigationPath.NAVGPATHFLAG_HIDDEN))
                 {
                         if (sideBarElement.isInActivePath(nc))
                         {
                             writer.write("      <tr>\n");
 							writer.write("          <td " + navClass + "on\" "+ navAttrs + ">" +
                                     "<nobr>" + sideBarElement.getCaption(nc) + "</nobr></td>\n");
 						    writer.write("      </tr>\n");
                         }
                         else
                         {
                             writer.write("      <tr>\n");
 						    writer.write("          <td " + navClass + "off\" "+ navAttrs + ">" +
                                 "<a " + navLinkClass + " href=\"" + sideBarElement.getUrl(nc) + "\"><nobr>" +
                                     sideBarElement.getCaption(nc) + "</nobr></a></td>\n");
 						    writer.write("      </tr>\n");
                         }
                 }
             }
             writer.write("          <tr height=\"100%\">\n");
 			writer.write("              <td class=\"menu-table-end\" align=\"left\" valign=\"top\" height=\"100%\">" +
                     "<img src=\""+ nc.getRootUrl() + getThemeImagePath()+ "/spacer-big.gif\" height=\"100%\" width=\"100%\"></td>\n");
 			writer.write("          </tr>\n");
 			writer.write("      </table>\n");
             writer.write("      <!-- Level 3 Ends -->\n");
         }
     }
 
 
 }
