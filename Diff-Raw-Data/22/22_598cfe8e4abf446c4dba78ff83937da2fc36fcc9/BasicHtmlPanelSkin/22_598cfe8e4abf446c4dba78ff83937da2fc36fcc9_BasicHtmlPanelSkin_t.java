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
 * $Id: BasicHtmlPanelSkin.java,v 1.21 2003-10-12 05:20:54 aye.thu Exp $
  */
 
 package com.netspective.sparx.theme.basic;
 
 import java.io.Writer;
 import java.io.IOException;
 
 import com.netspective.sparx.theme.Theme;
 import com.netspective.sparx.panel.*;
 import com.netspective.commons.value.ValueSource;
 import com.netspective.commons.value.ValueContext;
 import com.netspective.commons.value.source.RedirectValueSource;
 import com.netspective.commons.command.CommandNotFoundException;
 import com.netspective.commons.command.Command;
 import com.netspective.commons.xdm.XdmBitmaskedFlagsAttribute;
 
 public class BasicHtmlPanelSkin extends AbstractThemeSkin implements HtmlPanelSkin
 {
     public static class Flags extends XdmBitmaskedFlagsAttribute
     {
         public static final int FULL_WIDTH = 1;
         public static final int SHOW_BANNER = FULL_WIDTH * 2;
         public static final int STARTCUSTOM = SHOW_BANNER * 2;
 
         public static final XdmBitmaskedFlagsAttribute.FlagDefn[] FLAGDEFNS = new XdmBitmaskedFlagsAttribute.FlagDefn[]
         {
             new FlagDefn(ACCESS_XDM, "FULL_WIDTH", FULL_WIDTH),
             new FlagDefn(ACCESS_XDM, "SHOW_BANNER", SHOW_BANNER),
         };
 
         public XdmBitmaskedFlagsAttribute.FlagDefn[] getFlagsDefns()
         {
             return FLAGDEFNS;
         }
     }
 
     private boolean defaultPanel;
     protected String panelClassNamePrefix;
     protected String panelResourcesPrefix;
     protected String contentDivClass = "";
     protected Flags flags;
 
     public BasicHtmlPanelSkin()
     {
         super();
         flags = createFlags();
     }
 
     public BasicHtmlPanelSkin(Theme theme, String name, String panelClassNamePrefix, String panelResourcesPrefix, boolean fullWidth)
     {
         super(theme, name);
         flags = createFlags();
         setPanelClassNamePrefix(panelClassNamePrefix);
         setPanelResourcesPrefix(panelResourcesPrefix);
         flags.setFlag(Flags.SHOW_BANNER);
         if(fullWidth)
             flags.setFlag(Flags.FULL_WIDTH);
     }
 
     public boolean isDefault()
     {
         return defaultPanel;
     }
 
     public void setDefault(boolean defaultPanel)
     {
         this.defaultPanel = defaultPanel;
     }
 
     public Flags createFlags()
     {
         return new Flags();
     }
 
     public String getPanelClassNamePrefix()
     {
         return panelClassNamePrefix;
     }
 
     public void setPanelClassNamePrefix(String panelClassNamePrefix)
     {
         this.panelClassNamePrefix = panelClassNamePrefix;
     }
 
     public String getPanelResourcesPrefix()
     {
         return panelResourcesPrefix;
     }
 
     public void setPanelResourcesPrefix(String panelResourcesPrefix)
     {
         this.panelResourcesPrefix = panelResourcesPrefix;
     }
 
     public Flags getFlags()
     {
         return flags;
     }
 
     public void setFlags(Flags flags)
     {
         this.flags.copy(flags);
     }
 
     public String getContentDivClass()
     {
         return contentDivClass;
     }
 
     public void setContentDivClass(String contentDivClass)
     {
         this.contentDivClass = contentDivClass;
     }
 
     public void produceHeadingExtras(Writer writer, HtmlPanelValueContext vc, HtmlPanelFrame frame) throws IOException
     {
         HtmlPanelActions actions = frame.getActions();
 
         if(actions != null && actions.size() > 0)
         {
             Theme theme = getTheme();
             int colCount = 0;
 
             // create a temporary string buffer for the HTML of the heading action items
             StringBuffer itemBuffer = new StringBuffer();
             for (int i=0; i < actions.size(); i++)
             {
                 if (i != 0)
                 {
                     itemBuffer.append("            <td bgcolor=\"white\"><img src=\"" + theme.getResourceUrl("/images/" + panelResourcesPrefix + "/login/spacer.gif") + "\" width=\"5\" height=\"5\"></td>");
                     colCount++;
                 }
                 HtmlPanelAction item = actions.get(i);
                 String itemUrl = "TODO"; //item.getParameters();
                 String itemCaption = item.getCaption().getTextValue(vc);
                 ValueSource itemIcon = item.getIcon();
                 if (itemIcon != null)
                 {
                     // icon for this item is defined so use the passed in image INSTEAD of using the CSS based background image
                     itemBuffer.append("            <td class=\""+ panelClassNamePrefix +"-frame-action-item\" width=\"18\"></td>");
                     colCount++;
                 }
                 else
                 {
                     itemBuffer.append("            <td class=\""+ panelClassNamePrefix +"-frame-action-item\" width=\"18\"><img src=\"" + theme.getResourceUrl("/images/" + panelResourcesPrefix + "/spacer.gif") + "\" width=\"18\" height=\"19\"></td>");
                     colCount++;
                 }
                 itemBuffer.append("            <td class=\""+ panelClassNamePrefix +"-frame-action-box\">" +
                         "<a class=\""+ panelClassNamePrefix +"-frame-action\" href=\""+ itemUrl + "\">&nbsp;" + itemCaption + "&nbsp;</a></td>");
                 colCount++;
             }
 
             if (itemBuffer.length() > 0)
                 writer.write(itemBuffer.toString());
         }
     }
 
     public void renderPanelRegistration(Writer writer, HtmlPanelValueContext vc) throws IOException
     {
         HtmlPanel panel = vc.getPanel();
         writer.write("<script>ACTIVE_PANEL_PARENT.registerPanel(new Panel(\""+ panel.getPanelIdentifier() +"\", false, PANELSTYLE_TOPLEVEL, \""+ panelClassNamePrefix +"\"))</script>\n");
     }
 
     public void renderFrameBegin(Writer writer, HtmlPanelValueContext vc) throws IOException
     {
         HtmlPanel panel = vc.getPanel();
         HtmlPanelFrame frame = panel.getFrame();
 
         Theme theme = getTheme();
 
         writer.write("<table id=\""+ panel.getPanelIdentifier() +"_frame\" border=\"0\" cellspacing=\"0\" cellpadding=\"0\" nowrap ");
         if(flags.flagIsSet(Flags.FULL_WIDTH))
             writer.write("width='100%' ");
         writer.write(">\n");
 
         if(frame.hasHeadingOrFooting())
         {
             String heading = null;
             ValueSource hvs = frame.getHeading();
             if(hvs != null)
                 heading = hvs.getValue(vc).getTextValue();
             if(heading != null && !frame.hideHeading())
             {
                 writer.write("<tr>\n");
                 writer.write("    <td class=\""+ panelClassNamePrefix +"\">\n");
                 writer.write("    <table width=\"100%\" border=\"0\" cellspacing=\"0\" cellpadding=\"0\" nowrap>\n");
                 writer.write("        <tr>\n");
                 if (frame.getFlags().flagIsSet(HtmlPanelFrame.Flags.ALLOW_COLLAPSE))
                 {
                     if (vc.isMinimized())
                         writer.write("            <td id=\""+ panel.getPanelIdentifier() +"_action\" class=\""+ panelClassNamePrefix +"-frame-heading-action-expand\" align=\"left\" valign=\"middle\" nowrap width=\"17\" onclick=\"ALL_PANELS.togglePanelExpandCollapse('"+ panel.getPanelIdentifier() +"')\">" +
                             "<!-- <img src=\"" + theme.getResourceUrl("/images/" + panelResourcesPrefix + "/spacer.gif") + "\" alt=\"\" height=\"5\" width=\"17\" border=\"0\">--></td>");
                     else
                         writer.write("            <td id=\""+ panel.getPanelIdentifier() +"_action\" class=\""+ panelClassNamePrefix +"-frame-heading-action-collapse\"   align=\"left\" valign=\"middle\" nowrap width=\"17\" onclick=\"ALL_PANELS.togglePanelExpandCollapse('"+ panel.getPanelIdentifier() +"')\">" +
                             "<!-- <img src=\"" + theme.getResourceUrl("/images/" + panelResourcesPrefix + "/spacer.gif") + "\" alt=\"\" height=\"5\" width=\"17\" border=\"0\"> --></td>");
 
                     writer.write("<script>ALL_PANELS.getPanel(\""+ panel.getPanelIdentifier() +"\").minimized = "+ (vc.isMinimized() ? "true" : "false") +"</script>");
                 }
                 else
                 {
                     writer.write("            <td class=\""+ panelClassNamePrefix +"-frame-heading-action-left-blank\" align=\"left\" valign=\"middle\" nowrap width=\"17\">" +
                         "<!-- <img src=\"" + theme.getResourceUrl("/images/" + panelResourcesPrefix + "/spacer.gif") + "\" alt=\"\" height=\"5\" width=\"17\" border=\"0\">--></td>\n");
                 }
                 writer.write("            <td class=\""+ panelClassNamePrefix +"-frame-heading\" align=\"left\" valign=\"middle\" nowrap>" + heading +
                         "</td>\n");
                 writer.write("            <td class=\""+ panelClassNamePrefix +"-frame-heading-action-right-blank\" align=\"center\" valign=\"middle\" nowrap width=\"17\">" +
                     "<!-- <img src=\"" + theme.getResourceUrl("/images/" + panelResourcesPrefix + "/spacer.gif") + "\" alt=\"\" height=\"5\" width=\"17\" border=\"0\">--></td>\n");
                 writer.write("            <td class=\""+ panelClassNamePrefix +"-frame-mid\" align=\"right\" valign=\"top\" nowrap width=\"100%\">" +
                     "<!-- <img src=\"" + theme.getResourceUrl("/images/" + panelResourcesPrefix + "/spacer.gif") + "\" alt=\"\" height=\"5\" width=\"100%\" border=\"0\">--></td>\n");
                 writer.write("            <td class=\""+ panelClassNamePrefix +"-frame-end-cap\" align=\"right\" valign=\"top\" nowrap width=\"2\"></td>\n");
                 produceHeadingExtras(writer, vc, frame);
                 writer.write("        </tr>\n");
                 writer.write("    </table>\n");
                 writer.write("    </td>\n");
                 writer.write("</tr>\n");
             }
         }
 
         renderBanner(writer, vc);
 
         int height = panel.getHeight();
        int width = panel.getWidth();
         if(height > 0)
            writer.write("<tr id=\""+ panel.getPanelIdentifier() +"_content\">\n     <td class=\""+ panelClassNamePrefix +"-content\"><div class='"+ contentDivClass +"' style=\"width: " + width + "; height: "+ height +"; overflow: auto;\">\n");
         else
            writer.write("<tr id=\""+ panel.getPanelIdentifier() +"_content\">\n     <td class=\""+ panelClassNamePrefix +"-content\"><div class='"+ contentDivClass +"' style=\"width: " + width + ";\">\n");
     }
 
     public void renderFrameEnd(Writer writer, HtmlPanelValueContext vc) throws IOException
     {
         HtmlPanel panel = vc.getPanel();
         HtmlPanelFrame frame = panel.getFrame();
 
         writer.write("    </div></td>\n");
         writer.write("</tr>\n");
 
         if(frame.hasHeadingOrFooting())
         {
             ValueSource fvs = frame.getFooting();
             if(fvs != null)
             {
                 writer.write("<tr id=\""+ panel.getPanelIdentifier() +"_banner_footer\">\n");
                 writer.write("    <td class=\""+ panelClassNamePrefix +"-banner-footer\">" + fvs.getTextValue(vc) + "</td>\n");
                 writer.write("</tr>\n");
             }
         }
 
         writer.write("</table>\n");
     }
 
     /**
      * Generates the html for banner action items
      * @param writer
      * @param rc
      * @param actions
      * @throws IOException
      * @throws CommandNotFoundException
      */
     protected void produceBannerActionsHtml(Writer writer, HtmlPanelValueContext rc, HtmlPanelActions actions) throws IOException, CommandNotFoundException
     {
         int actionsCount = actions.size();
         if(actionsCount == 0) return;
         String bannerItemFontAttrs = "";
 
         if(actions.getStyle().getValueIndex() == HtmlPanelActions.Style.HORIZONTAL)
         {
             for(int i = 0; i < actionsCount; i++)
             {
                 HtmlPanelAction action = actions.get(i);
                 ValueSource itemCaption = action.getCaption();
                 ValueSource itemIcon = action.getIcon();
                 String caption = "";
                 RedirectValueSource itemRedirect = action.getRedirect();

                 if (itemRedirect == null)
                 {
                     caption = itemCaption != null ? itemCaption.getValue(rc).getTextValue() : "item" + i;
                 }
                 else
                 {
                    String hint = action.getHint() != null ? action.getHint().getValue(rc).getTextValue() : "";
                     caption = constructRedirect(rc, itemRedirect, itemCaption != null ? itemCaption.getValue(rc).getTextValue() : "item" + i,
                            hint, null);
                 }
                 writer.write("<font " + bannerItemFontAttrs + ">");
                 if(i > 0)
                     writer.write(", ");
                 if(itemIcon != null)
                     writer.write("<img src='" + itemIcon.getValue(rc) + "'>");
                 writer.write(caption);
                 writer.write("</font>");
             }
         }
         else
         {
             writer.write("<table border=0 cellspacing=0>");
             for(int i = 0; i < actionsCount; i++)
             {
                 HtmlPanelAction action = actions.get(i);
                 RedirectValueSource itemRedirect= action.getRedirect();
                 ValueSource itemCaption = action.getCaption();
                 ValueSource itemIcon = action.getIcon();
                 HtmlPanelActions childItems = action.getChildren();
                 String caption = "";
                 if (itemRedirect == null)
                 {
                     caption = itemCaption != null ? itemCaption.getValue(rc).getTextValue() : "item" + i;
                 }
                 else
                 {
                    String hint = action.getHint() != null ? action.getHint().getValue(rc).getTextValue() : "";
                     caption = constructRedirect(rc, itemRedirect, itemCaption != null ? itemCaption.getValue(rc).getTextValue() : "item" + i,
                            hint, null);
                 }
                 writer.write("<tr><td>");
                 writer.write(itemIcon != null ? "<img src='" + itemIcon.getValue(rc) + "'>" : "-");
                 writer.write("</td>");
                 writer.write("<td><font " + bannerItemFontAttrs + ">");
                 if(caption != null)
                     writer.write(caption);
                 if(childItems != null && childItems.size() > 0)
                     produceBannerActionsHtml(writer, rc, childItems);
                 writer.write("</font></td>");
                 writer.write("</tr>");
             }
             writer.write("</table>");
         }
     }
 
     /**
      * Displays the report banner html. Utilizes the THEME
      * @param writer
      * @param vc
      * @throws java.io.IOException
      */
     protected void renderBanner(Writer writer, HtmlPanelValueContext vc) throws IOException
     {
         HtmlPanel panel = vc.getPanel();
         HtmlPanelBanner banner = panel.getBanner();
         if (banner == null)
             return;
         HtmlPanelActions actions = banner.getActions();
         ValueSource content = banner.getContent();
         if(content == null && (actions == null || actions.size() == 0))
             return;
         writer.write("<tr id=\""+ panel.getPanelIdentifier() +"_banner\"><td class=\""+ panelClassNamePrefix +"-banner\">\n");
         if(content != null)
         {
             writer.write(content.getTextValue(vc));
         }
         else
         {
             try
             {
                 produceBannerActionsHtml(writer, vc, actions);
             }
             catch (CommandNotFoundException e)
             {
                 writer.write(e.toString());
             }
         }
         writer.write("</td></tr>\n");
     }
 
     /**
      * Constructs the redirect URL from the passed in command
      * @param rc
      * @param redirect
      * @param label
      * @param hint
      * @param target
      * @return
      */
     // TODO: This is the same as the one in BasicHtmlTabularReportSkin. Need to refactor!
     public String constructRedirect(ValueContext rc, ValueSource redirect, String label, String hint, String target)
     {
         if (redirect instanceof RedirectValueSource)
         {
             StringBuffer sb = new StringBuffer();
             String url = ((RedirectValueSource)redirect).getUrl(rc);
             if (url.startsWith("javascript"))
             {
                 sb.append("<a href=\"#\" onclick=\"" + url + "\"");
             }
             else
             {
                 sb.append("<a href='" + url + "'");
 
             }
             if (hint != null)
                 sb.append(" title=\"" + hint + "\"");
             if (target != null)
                 sb.append(" target=\"" + target + "\"");
             sb.append(">" + label + "</a>");
             return sb.toString();
         }
         return null;
     }
 
 }
