 /*
  * Copyright (c) 2005 Your Corporation. All Rights Reserved.
  */
 package org.wings.plaf.css;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.wings.*;
 import org.wings.io.Device;
 
 import java.io.IOException;
 
 /**
  * CG for a pagescroller.
  *
  * @author holger
  */
 public class PageScrollerCG extends AbstractComponentCG implements org.wings.plaf.PageScrollerCG {
     private final static transient Log log = LogFactory.getLog(PageScrollerCG.class);
 
     public static final int FORWARD = 0;
     public static final int BACKWARD = 1;
     public static final int FORWARD_BLOCK = 2;
     public static final int BACKWARD_BLOCK = 3;
     public static final int FIRST = 4;
     public static final int LAST = 5;
     private final static SIcon[][][] DEFAULT_ICONS = new SIcon[2][6][2];
 
     static {
         String[] postfixes = new String[6];
         String[] prefixes = new String[6];
         for (int orientation = 0; orientation < 2; orientation++) {
             prefixes[BACKWARD] = "";
             prefixes[FORWARD] = "";
             prefixes[FIRST] = "Margin";
             prefixes[LAST] = "Margin";
             prefixes[FORWARD_BLOCK] = "Block";
             prefixes[BACKWARD_BLOCK] = "Block";
             if (orientation == SConstants.VERTICAL) {
                 postfixes[BACKWARD] = "Up";
                 postfixes[FORWARD] = "Down";
                 postfixes[FIRST] = "Up";
                 postfixes[LAST] = "Down";
                 postfixes[BACKWARD_BLOCK] = "Up";
                 postfixes[FORWARD_BLOCK] = "Down";
             } else {
                 postfixes[BACKWARD] = "Left";
                 postfixes[FORWARD] = "Right";
                 postfixes[FIRST] = "Left";
                 postfixes[LAST] = "Right";
                 postfixes[BACKWARD_BLOCK] = "Left";
                 postfixes[FORWARD_BLOCK] = "Right";
             }
 
             for (int direction = 0; direction < postfixes.length; direction++) {
                 DEFAULT_ICONS[orientation][direction][0] =
                         new SResourceIcon("org/wings/icons/"
                         + prefixes[direction]
                         + "Scroll"
                         + postfixes[direction] + ".gif");
                 DEFAULT_ICONS[orientation][direction][1] =
                         new SResourceIcon("org/wings/icons/Disabled"
                         + prefixes[direction]
                         + "Scroll"
                         + postfixes[direction] + ".gif");
             }
         }
     }
 
     public void writeContent(Device d, SComponent c)
             throws IOException {
         log.debug("write = " + c);
         SPageScroller sb = (SPageScroller) c;
 
         if (sb.getLayoutMode() == SConstants.VERTICAL)
             writeVerticalPageScroller(d, sb);
         else
             writeHorizontalPageScroller(d, sb);
     }
 
     private void writeVerticalPageScroller(Device d, SPageScroller sb) throws IOException {
         int value = sb.getValue();
         int extent = sb.getExtent();
         int minimum = sb.getMinimum();
         int maximum = sb.getMaximum();
         boolean backEnabled = value > minimum;
         boolean forwardEnabled = value < maximum - extent;
 
         d.print("<table orientation=\"vertical\" class=\"SLayout\"><tbody>\n")
                 .print("<tr height=\"1%\">\n")
                 .print("<td height=\"1%\" class=\"SLayout\"><table area=\"buttons\" class=\"SLayout\"><tbody>\n");
 
         d.print("<tr><td class=\"SLayout\">");
         writeButton(d, sb, DEFAULT_ICONS[SConstants.VERTICAL][FIRST][0], "" + minimum);
         d.print("</td></tr>\n");
         d.print("<tr><td class=\"SLayout\">");
         writeButton(d, sb, DEFAULT_ICONS[SConstants.VERTICAL][BACKWARD][0], "" + (value - extent));
         d.print("</td></tr>\n");
 
         d.print("</tbody></table></td>\n")
                 .print("</tr>\n")
                 .print("<tr height=\"100%\">\n")
                 .print("<td class=\"SLayout\"><table area=\"pages\" height=\"100%\" class=\"SLayout\"><tbody>\n");
 
         int firstDirectPage = sb.getCurrentPage() - (sb.getDirectPages() - 1) / 2;
         firstDirectPage = Math.min(firstDirectPage, sb.getPageCount() - sb.getDirectPages());
         firstDirectPage = Math.max(firstDirectPage, 0);
 
         for (int i = 0; i < Math.min(sb.getDirectPages(), sb.getPageCount() - firstDirectPage); i++) {
             int page = firstDirectPage + i;
             d.print("<tr><td class=\"SLayout\">");
             writePage(d, sb, page);
             d.print("</td></tr>\n");
         }
 
         d.print("</tbody></table></td>\n")
                 .print("</tr>\n")
                 .print("<tr height=\"1%\">\n")
                 .print("<td height=\"1%\" class=\"SLayout\"><table area=\"buttons\" class=\"SLayout\"><tbody>\n");
 
         d.print("<tr><td class=\"SLayout\">");
         writeButton(d, sb, DEFAULT_ICONS[SConstants.VERTICAL][FORWARD][0], "" + (value + extent));
         d.print("</td></tr>\n");
         d.print("<tr><td class=\"SLayout\">");
         writeButton(d, sb, DEFAULT_ICONS[SConstants.VERTICAL][LAST][0], "" + (maximum + 1 - extent));
         d.print("</td></tr>\n");
 
         d.print("</tbody></table></td>\n")
                 .print("</tr>\n")
                 .print("</tbody></table>");
     }
 
     private void verticalArea(Device d, String s, int v) throws IOException {
         d.print("<tr><td class=\"SLayout\" style=\"background-color: ");
         d.print(s);
         d.print("\" height=\"");
         d.print(v + "%");
         d.print("\"></td></tr>\n");
     }
 
     private void writeHorizontalPageScroller(Device d, SPageScroller sb) throws IOException {
         int value = sb.getValue();
         int extent = sb.getExtent();
         int minimum = sb.getMinimum();
         int maximum = sb.getMaximum();
         boolean backEnabled = value > minimum;
         boolean forwardEnabled = value < maximum - extent;
 
         d.print("<table class=\"SLayout\" orientation=\"horizontal\"><tbody><tr>\n")
                 .print("<td class=\"SLayout\" width=\"1%\"><table area=\"buttons\"><tbody><tr>\n");
 
         d.print("<td class=\"SLayout\">");
         writeButton(d, sb, DEFAULT_ICONS[SConstants.HORIZONTAL][FIRST][0], "" + minimum);
         d.print("</td>\n");
         d.print("<td class=\"SLayout\">");
         writeButton(d, sb, DEFAULT_ICONS[SConstants.HORIZONTAL][BACKWARD][0], "" + (value - extent));
         d.print("</td>\n");
 
         d.print("</tr></tbody></table></td>\n")
                 .print("<td class=\"SLayout\" width=\"100%\"><table area=\"pages\" width=\"100%\"><tbody><tr>\n");
 
         int firstDirectPage = sb.getCurrentPage() - (sb.getDirectPages() - 1) / 2;
         firstDirectPage = Math.min(firstDirectPage, sb.getPageCount() - sb.getDirectPages());
         firstDirectPage = Math.max(firstDirectPage, 0);
 
         for (int i = 0; i < Math.min(sb.getDirectPages(), sb.getPageCount() - firstDirectPage); i++) {
             int page = firstDirectPage + i;
            d.print("<td class=\"SLayout\">");
             writePage(d, sb, page);
             d.print("</td>\n");
         }
 
         d.print("</tr></tbody></table></td>\n")
                 .print("<td class=\"SLayout\" width=\"1%\"><table area=\"buttons\"><tbody><tr>\n");
 
         d.print("<td class=\"SLayout\">");
         writeButton(d, sb, DEFAULT_ICONS[SConstants.HORIZONTAL][FORWARD][0], "" + (value + extent));
         d.print("</td>\n");
         d.print("<td class=\"SLayout\">");
         writeButton(d, sb, DEFAULT_ICONS[SConstants.HORIZONTAL][LAST][0], "" + (maximum + 1 - extent));
         d.print("</td>\n");
 
         d.print("</tr></tbody></table></td>\n")
                 .print("</tr></tbody></table>");
     }
 
     private void writePage(Device device, SPageScroller pageScroller, int page) throws IOException {
         boolean childSelectorWorkaround = !pageScroller.getSession().getUserAgent().supportsCssChildSelector();
         boolean showAsFormComponent = pageScroller.getShowAsFormComponent();
 
         if (showAsFormComponent) {
             writeButtonStart(device, pageScroller, String.valueOf(page * pageScroller.getExtent()));
             device.print(" type=\"submit\" name=\"")
                     .print(Utils.event(pageScroller))
                     .print("\" value=\"")
                     .print(String.valueOf(page * pageScroller.getExtent()))
                     .print("\"");
         } else {
             device.print("<a href=\"")
                     .print(pageScroller.getRequestURL()
                     .addParameter(Utils.event(pageScroller) + "=" + page * pageScroller.getExtent()).toString())
                     .print("\"");
         }
         device.print(">");
 
         device.print(Integer.toString(page + 1));
 
         if (showAsFormComponent)
             device.print("</button>\n");
         else
             device.print("</a>\n");
     }
 
     private void writeButton(Device device, SPageScroller pageScroller, SIcon icon, String event) throws IOException {
         boolean childSelectorWorkaround = !pageScroller.getSession().getUserAgent().supportsCssChildSelector();
         boolean showAsFormComponent = pageScroller.getShowAsFormComponent();
 
         if (showAsFormComponent) {
             writeButtonStart(device, pageScroller, event);
             device.print(" type=\"submit\" name=\"")
                     .print(Utils.event(pageScroller))
                     .print("\" value=\"")
                     .print(event)
                     .print("\"");
         } else {
             device.print("<a href=\"")
                     .print(pageScroller.getRequestURL()
                     .addParameter(Utils.event(pageScroller) + "=" + event).toString())
                     .print("\"");
         }
         device.print(">");
 
         device.print("<img");
         Utils.optAttribute(device, "src", icon.getURL());
         Utils.optAttribute(device, "width", icon.getIconWidth());
         Utils.optAttribute(device, "height", icon.getIconHeight());
         device.print(" alt=\"");
         device.print(icon.getIconTitle());
         device.print("\"/>");
 
         if (showAsFormComponent)
             device.print("</button>\n");
         else
             device.print("</a>\n");
     }
 
     /* (non-Javadoc)
      * @see org.wings.plaf.css.ScrollBarCG#writeButtonStart(org.wings.io.Device, org.wings.SScrollBar, java.lang.String)
      */
     protected void writeButtonStart(Device device, SPageScroller pageScroller, String event) throws IOException {
         device.print("<button");
     }
 }
