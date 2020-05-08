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
 * $Id: HtmlLayoutPanel.java,v 1.26 2004-04-21 22:31:20 aye.thu Exp $
  */
 
 package com.netspective.sparx.panel;
 
 import com.netspective.commons.io.InputSourceLocator;
 import com.netspective.commons.xdm.XmlDataModelSchema;
 import com.netspective.commons.xml.template.Template;
 import com.netspective.commons.xml.template.TemplateConsumerDefn;
 import com.netspective.sparx.form.DialogContext;
 import com.netspective.sparx.navigate.NavigationContext;
 import com.netspective.sparx.theme.Theme;
 import com.netspective.sparx.value.HttpServletValueContext;
 
 import java.io.IOException;
 import java.io.StringWriter;
 import java.io.Writer;
 
 /**
  * Main class for handling the XDM tag &lt;Panels&gt;. Serves as the container of
  * multiple panels.  Provides functionalities for automatically rendering the
  * layout, for the multiple panels, based on the set style.
  */
 public class HtmlLayoutPanel implements HtmlPanel
 {
     public static final XmlDataModelSchema.Options XML_DATA_MODEL_SCHEMA_OPTIONS = new XmlDataModelSchema.Options().setIgnorePcData(true);
 
     private static int panelNumber = 0;
     private int height = -1, width = -1;
     private HtmlPanels children = new BasicHtmlPanels();
     private HtmlPanelFrame frame;
     private HtmlPanelBanner banner;
     private String identifier = "HtmlLayoutPanel_" + getNextPanelNumber();
     private boolean allowViewSource;
     private InputSourceLocator inputSourceLocator;
 
     synchronized static private final int getNextPanelNumber()
     {
         return ++panelNumber;
     }
 
     public HtmlLayoutPanel()
     {
         frame = createFrame();
         banner = createBanner();
         banner = createBanner();
     }
 
     public TemplateConsumerDefn getTemplateConsumerDefn()
     {
         return AbstractHtmlTabularReportPanel.templateConsumer;
     }
 
     public InputSourceLocator getInputSourceLocator()
     {
         return inputSourceLocator;
     }
 
     public void setInputSourceLocator(InputSourceLocator locator)
     {
         this.inputSourceLocator = locator;
     }
 
     public void registerTemplateConsumption(Template template)
     {
     }
 
     public String getPanelIdentifier()
     {
         return identifier;
     }
 
     /**
      * Sets the identifier to be used for this panel container. An identifier is a
      * field whose values may only contain uppercase letters, numbers, and an underscore.
      *
      * @param identifier identifier for the panel container
      */
     public void setIdentifier(String identifier)
     {
         this.identifier = identifier;
     }
 
     public int getHeight()
     {
         return height;
     }
 
 
     /**
      * Sets the height of this panel container.
      *
      * @param height height of this panel container
      */
     public void setHeight(int height)
     {
         this.height = height;
     }
 
     public int getWidth()
     {
         return width;
     }
 
     /**
      * Sets the width of this panel conainer
      *
      * @param width width of the panel
      */
     public void setWidth(int width)
     {
         this.width = width;
     }
 
     public HtmlPanelFrame getFrame()
     {
         return frame;
     }
 
     public void setFrame(HtmlPanelFrame rf)
     {
         frame = rf;
     }
 
     public HtmlPanelBanner getBanner()
     {
         return banner;
     }
 
     public void setBanner(HtmlPanelBanner value)
     {
         banner = value;
     }
 
     public HtmlLayoutPanel createPanels()
     {
         return new HtmlLayoutPanel();
     }
 
     public void addPanels(HtmlLayoutPanel panel)
     {
         children.add(panel);
     }
 
     public HtmlPanelFrame createFrame()
     {
         if(frame == null)
             frame = new HtmlPanelFrame();
         return frame;
     }
 
     public HtmlPanelBanner createBanner()
     {
         if(banner == null)
             banner = new HtmlPanelBanner();
         return banner;
     }
 
     public boolean affectsNavigationContext(NavigationContext nc)
     {
         for(int i = 0; i < children.size(); i++)
         {
             if(children.get(i).affectsNavigationContext(nc))
                 return true;
         }
 
         return false;
     }
 
     public int getStyle()
     {
         return children.getStyle();
     }
 
     /**
      * Sets the style for the panels container and all the panels it contains.
      * Sparx provides some built-in panel styles such as tabbed, two-columns, vertical
      * and horizontal.
      *
      * @param style style for the panels in this container
      */
     public void setStyle(HtmlPanelsStyleEnumeratedAttribute style)
     {
         children.setStyle(style);
     }
 
     public HtmlPanel createPanel()
     {
         return new TemplateContentPanel();
     }
 
     public void addPanel(HtmlPanel panel)
     {
         children.add(panel);
     }
 
     public HtmlPanels getChildren()
     {
         return children;
     }
 
     public boolean isAllowViewSource(HttpServletValueContext vc)
     {
         return allowViewSource;
     }
 
     /**
      * Sets whether or not to allow the user to view the XML source for the panels.
      *
      * @param allowViewSource if <code>true</code>, a link is displayed for viewing
      *                        the XDM code in a separate panel.  If <code>no</code>,
      *                        the link for viewing XDM code is not displayed.
      */
     public void setAllowViewSource(boolean allowViewSource)
     {
         this.allowViewSource = allowViewSource;
     }
 
     public void renderViewSource(Writer writer, NavigationContext nc) throws IOException
     {
         AbstractPanel.renderPanelViewSource(writer, nc, this);
     }
 
     public void render(Writer writer, NavigationContext nc, Theme theme, int flags) throws IOException
     {
         switch(getStyle())
         {
             case HtmlPanelsStyleEnumeratedAttribute.VERTICAL:
                 for(int i = 0; i < children.size(); i++)
                 {
                     writer.write("<div style='padding-bottom: 12'>");
                     children.get(i).render(writer, nc, theme, flags);
                     writer.write("</div>");
                 }
                 break;
 
             case HtmlPanelsStyleEnumeratedAttribute.HORIZONTAL:
                 writer.write("<table cellspacing=0 cellpadding=9><tr valign=top>");
                 for(int i = 0; i < children.size(); i++)
                 {
                     writer.write("<td>");
                     children.get(i).render(writer, nc, theme, flags);
                     writer.write("</td>");
                 }
                 writer.write("</tr></table>");
                 break;
             case HtmlPanelsStyleEnumeratedAttribute.TWO_COLUMNS:
                 writer.write("<table cellspacing=0 cellpadding=9>");
                 int counter = 0;
                 for(int i = 0; i < children.size(); i++)
                 {
                     counter++;
                     if (counter == 1)
                     {
                         writer.write("<tr valign=top><td>");
                         children.get(i).render(writer, nc, theme, flags);
                         writer.write("</td>");
                     }
                     else if (counter == 2)
                     {
                         writer.write("<td>");
                         children.get(i).render(writer, nc, theme, flags);
                         writer.write("</td></tr>");
                         counter = 0;
                     }
                 }
                 if (counter != 0)
                     writer.write("<td>&nbsp;</td></tr>");
                 writer.write("</table>");
                 break;
 
             case HtmlPanelsStyleEnumeratedAttribute.TWO_COLUMNS_STACKED:
                 StringWriter columnOne = new StringWriter();
                 StringWriter columnTwo = new StringWriter();
                 int column = 0;
                 for(int i = 0; i < children.size(); i++)
                 {
                     column++;
                     if (column == 1)
                     {
                        columnOne.write("<div class=\"panel-layout-column\">");
                         children.get(i).render(columnOne, nc, theme, flags);
                        columnOne.write("</div>");
                     }
                     else if (column == 2)
                     {
                        columnTwo.write("<div class=\"panel-layout-column\">");
                         children.get(i).render(columnTwo, nc, theme, flags);
                         column = 0;
                        columnTwo.write("</div>");
                     }
                 }
                writer.write("<table class=\"panel-layout-table\">\n");
                writer.write("<tr valign=\"top\">\n<td>" + columnOne + "</td>\n");
                writer.write("<td>" + columnTwo + "</td>\n</tr>\n");
                writer.write("</table>\n");
                 break;
 
             case HtmlPanelsStyleEnumeratedAttribute.TABBED:
                 HtmlPanelValueContext vc = new BasicHtmlPanelValueContext(nc.getServlet(), nc.getRequest(), nc.getResponse(), this);
                 HtmlPanelSkin tabbedPanelSkin = theme.getTabbedPanelSkin();
                 tabbedPanelSkin.renderPanelRegistration(writer, vc);
                 tabbedPanelSkin.renderFrameBegin(writer, vc);
                 writer.write("<script>startParentPanel(ALL_PANELS.getPanel(\""+ getPanelIdentifier() +"\"))</script>\n");
                 writer.write("<div class=\"panel-output-tabs\">");
                 writer.write("<table id=\""+ getPanelIdentifier() + "_tabs\" class=\"panel-output-tabs\"><tr>");
                 for(int i = 0; i < children.size(); i++)
                 {
                     HtmlPanel panel = children.get(i);
                     writer.write("<td>");
                     writer.write("  <a id=\""+ panel.getPanelIdentifier() +"_tab\" class=\"panel-output-tab\" href=\"javascript:ALL_PANELS.getPanel('"+ getPanelIdentifier() +"').children.togglePanelExpandCollapse('"+ panel.getPanelIdentifier() +"')\">&nbsp;");
                     writer.write(      panel.getFrame().getHeading().getTextValue(vc));
                     writer.write("  </a></td>");
                 }
                 writer.write("</tr></table></div>");
 
                 for(int i = 0; i < children.size(); i++)
                 {
                     HtmlPanel panel = children.get(i);
                     writer.write("<div id=\""+ panel.getPanelIdentifier() + "_container\" style='display: none'>");
                     panel.render(writer, nc, theme, HtmlPanel.RENDERFLAG_NOFRAME);
                     writer.write("</div>");
                     writer.write("<script>ACTIVE_PANEL_PARENT.getPanel(\""+ panel.getPanelIdentifier() +"\").setStyle(PANELSTYLE_TABBED);</script>\n");
                 }
                 tabbedPanelSkin.renderFrameEnd(writer, vc);
                 writer.write("<script>endParentPanel()</script>\n");
                 break;
         }
     }
 
     public void render(Writer writer, DialogContext dc, Theme theme, int flags) throws IOException
     {
         switch(getStyle())
         {
             case HtmlPanelsStyleEnumeratedAttribute.VERTICAL:
                 for(int i = 0; i < children.size(); i++)
                 {
                     writer.write("<div style='padding-bottom: 12'>");
                     children.get(i).render(writer, dc, theme, flags);
                     writer.write("</div>");
                 }
                 break;
 
             case HtmlPanelsStyleEnumeratedAttribute.HORIZONTAL:
                 writer.write("<table cellspacing=0 cellpadding=9><tr valign=top>");
                 for(int i = 0; i < children.size(); i++)
                 {
                     writer.write("<td>");
                     children.get(i).render(writer, dc, theme, flags);
                     writer.write("</td>");
                 }
                 writer.write("</tr></table>");
                 break;
 
             case HtmlPanelsStyleEnumeratedAttribute.TWO_COLUMNS:
                 writer.write("<table cellspacing=0 cellpadding=9>");
                 int counter = 0;
                 for(int i = 0; i < children.size(); i++)
                 {
                     counter++;
                     if (counter == 1)
                     {
                         writer.write("<tr valign=top><td>");
                         children.get(i).render(writer, dc, theme, flags);
                         writer.write("</td>");
                     }
                     else if (counter == 2)
                     {
                         writer.write("<td>");
                         children.get(i).render(writer, dc, theme, flags);
                         writer.write("</td></tr>");
                         counter = 0;
                     }
                 }
                 if (counter != 0)
                     writer.write("<td>&nbsp;</td></tr>");
                 writer.write("</table>");
                 break;
 
             case HtmlPanelsStyleEnumeratedAttribute.TABBED:
                 BasicHtmlPanelValueContext vc = new BasicHtmlPanelValueContext(dc.getServlet(), dc.getRequest(), dc.getResponse(), this);
                 HtmlPanelSkin tabbedPanelSkin = theme.getTabbedPanelSkin();
                 vc.setDialogContext(dc);
                 tabbedPanelSkin.renderPanelRegistration(writer, vc);
                 tabbedPanelSkin.renderFrameBegin(writer, vc);
                 writer.write("<script>startParentPanel(ALL_PANELS.getPanel(\""+ getPanelIdentifier() +"\"))</script>\n");
                 writer.write("<div class=\"panel-output-tabs\">");
                 writer.write("<table id=\""+ getPanelIdentifier() + "_tabs\" class=\"panel-output-tabs\"><tr>");
                 for(int i = 0; i < children.size(); i++)
                 {
                     HtmlPanel panel = children.get(i);
                     writer.write("<td>");
                     writer.write("  <a id=\""+ panel.getPanelIdentifier() +"_tab\" class=\"panel-output-tab\" href=\"javascript:ALL_PANELS.getPanel('"+ getPanelIdentifier() +"').children.togglePanelExpandCollapse('"+ panel.getPanelIdentifier() +"')\">&nbsp;");
                     writer.write(      panel.getFrame().getHeading().getTextValue(vc));
                     writer.write("  </a></td>");
                 }
                 writer.write("</tr></table></div>");
 
                 for(int i = 0; i < children.size(); i++)
                 {
                     HtmlPanel panel = children.get(i);
                     writer.write("<div id=\""+ panel.getPanelIdentifier() + "_container\" style='display: none'>");
                     panel.render(writer, dc, theme, HtmlPanel.RENDERFLAG_NOFRAME);
                     writer.write("</div>");
                     writer.write("<script>ACTIVE_PANEL_PARENT.getPanel(\""+ panel.getPanelIdentifier() +"\").setStyle(PANELSTYLE_TABBED);</script>\n");
                 }
                 tabbedPanelSkin.renderFrameEnd(writer, vc);
                 writer.write("<script>endParentPanel()</script>\n");
                 break;
         }
     }
 }
