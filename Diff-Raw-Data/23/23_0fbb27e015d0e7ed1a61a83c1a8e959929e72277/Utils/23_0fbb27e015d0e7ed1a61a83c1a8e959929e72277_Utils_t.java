 /*
  * Copyright 2000,2005 wingS development team.
  *
  * This file is part of wingS (http://wingsframework.org).
  *
  * wingS is free software; you can redistribute it and/or modify
  * it under the terms of the GNU Lesser General Public License
  * as published by the Free Software Foundation; either version 2.1
  * of the License, or (at your option) any later version.
  *
  * Please see COPYING for the complete licence.
  */
 package org.wings.plaf.css;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.wings.*;
 import org.wings.border.SAbstractBorder;
 import org.wings.border.SDefaultBorder;
 import org.wings.border.SBorder;
 import org.wings.externalizer.ExternalizeManager;
 import org.wings.header.JavaScriptHeader;
 import org.wings.header.Link;
 import org.wings.header.Script;
 import org.wings.header.StyleSheetHeader;
 import org.wings.io.Device;
 import org.wings.io.NullDevice;
 import org.wings.resource.*;
 import org.wings.script.JavaScriptDOMListener;
 import org.wings.script.JavaScriptEvent;
 import org.wings.script.JavaScriptListener;
 import org.wings.script.ScriptListener;
 import org.wings.session.BrowserType;
 import org.wings.session.Session;
 import org.wings.session.SessionManager;
 import org.wings.style.Style;
 import org.wings.style.CSSStyle;
 import org.wings.style.CSSAttributeSet;
 
 import java.awt.*;
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.StringTokenizer;
 
 /**
  * Utils.java
  * <p/>
  * Helper class that collects static methods usable from CGs.
  *
  * @author <a href="mailto:mreinsch@to.com">Michael Reinsch</a>
  */
 public final class Utils {
     /**
      * Apache jakarta commons logger
      */
     private static final Log log = LogFactory.getLog(Utils.class);
 
     /**
      * Print debug information in generated HTML
      */
     public final static boolean PRINT_DEBUG;
     public final static boolean PRINT_PRETTY;
 
     public final static String HEADER_LOADED_CALLBACK =
         "if (typeof wingS != \"undefined\") {\n" +
         "    wingS.global.finishedLoadingHeader();\n" +
         "}";
 
     static {
         Session session = SessionManager.getSession();
         // Respect settings from resource.properties
         Boolean printDebug = (Boolean) ResourceManager.getObject("SComponent.printDebug", Boolean.class);
         Boolean printPretty = (Boolean) ResourceManager.getObject("SComponent.printPretty", Boolean.class);
         // May be overriden in i.e. web.xml. Hopefully we touch the class inside a session for the first time
         if (session != null) {
             if (session.getProperty("SComponent.printDebug") != null)
                 printDebug = Boolean.valueOf((String) session.getProperty("SComponent.printDebug"));
             if (session.getProperty("SComponent.printPretty") != null)
                 printPretty = Boolean.valueOf((String) session.getProperty("SComponent.printPretty"));
         }
         PRINT_DEBUG = printDebug.booleanValue();
         PRINT_PRETTY = printPretty.booleanValue();
     }
 
     protected final static char[] hexDigits = {
             '0', '1', '2', '3', '4', '5',
             '6', '7', '8', '9', 'a', 'b',
             'c', 'd', 'e', 'f'};
 
     private Utils() {
     }
 
     /**
      * Default list of javascript events to exlcude in {@link #writeEvents(org.wings.io.Device, org.wings.SComponent, String[])}
      */
     public final static String[] EXCLUDE_ON_CLICK = new String[]{JavaScriptEvent.ON_CLICK};
 
     /**
      * Renders a container using its Layout manager or fallback just one after another.
      */
     public static void renderContainer(Device d, SContainer c) throws IOException {
         final SLayoutManager layout = c.getLayout();
 
         if (layout == null) {
             d.print("<tbody><tr><td>");
             // just write out the components one after another
             for (int i = 0; i < c.getComponentCount(); i++) {
                 c.getComponent(i).write(d);
             }
             d.print("</td></tr></tbody>");
         }
         else {
             layout.write(d);
         }
     }
 
     /**
      * Render inline event listeners attached to the passed component exlucding types of suppressed listeners
      *
      * @param device                      output device
      * @param c                           component to retrieve listeners from
      * @param suppressScriptListenerTypes Array of String i.e. <code>new String[] { JavaScriptEvent.ON_CLICK } )</code>
      */
     public static void writeEvents(final Device device, final SComponent c, final String[] suppressScriptListenerTypes)
             throws IOException {
         if (!c.isEnabled())
             return;
 
         Set<String> types = new HashSet<String>();
         // Create set of strings (in lower case) defining the event types to be suppressed
         if (suppressScriptListenerTypes != null && suppressScriptListenerTypes.length > 0) {
             for (String suppressScriptListenerType : suppressScriptListenerTypes) {
                 types.add(suppressScriptListenerType.toLowerCase());
             }
         }
         ScriptListener[] listeners = c.getScriptListeners();
         if (listeners.length > 0) {
             Map<String, String> eventScripts = new HashMap<String, String>();
             // Fill map with script codes grouped by event type (key)
             for (final ScriptListener script : listeners) {
                 if (types.contains(script.getEvent().toLowerCase())) {
                     continue;
                 }
                 // If its a DOM event we are finished here
                 if (script instanceof JavaScriptDOMListener) {
                     continue;
                 }
                 final String event = script.getEvent();
                 String eventScriptCode = script.getCode();
 
                 if (event == null
                         || event.length() == 0
                         || eventScriptCode == null
                         || eventScriptCode.length() == 0) {
                     continue;
                 }
 
                 if (eventScripts.containsKey(event)) {
                     String savedEventScriptCode = eventScripts.get(event);
                     eventScriptCode = savedEventScriptCode
                             + (savedEventScriptCode.trim().endsWith(";") ? "" : ";")
                             + eventScriptCode;
                 }
                 eventScripts.put(event, eventScriptCode);
             }
 
             // Print map of script codes grouped by event type (key)
             for (final String event : eventScripts.keySet()) {
                 final String code = eventScripts.get(event);
                 Utils.optAttribute(device, event, code);
             }
         }
     }
 
     /**
      * Returns the according event ID for the given component.
      */
     public static String event(SComponent component) {
         if (component instanceof SClickable)
             return ((SClickable)component).getEventTarget().getLowLevelEventId();
         else
             return component.getLowLevelEventId();
     }
 
     /**
      * HTML allows 4 values for the horizontal align property of a div element.
      *
      * @param d     Output
      * @param align Please refer {@link SConstants}
      * @throws IOException
      */
     public static void printDivHorizontalAlignment(Device d, int align) throws IOException {
         printTableHorizontalAlignment(d, align);
     }
 
     /**
      * Horizontal alignment for TABLE cells. i.e. <code>align="center"</code>
      */
     private static void printTableHorizontalAlignment(final Device d, final int align)
             throws IOException {
         if (align == SConstants.NO_ALIGN) {
             // d.print(" align=\"left\"");
         }
         else if (align == SConstants.LEFT) {
             d.print(" align=\"left\"");
         }
         else if (align == SConstants.CENTER) {
             d.print(" align=\"center\"");
         }
         else if (align == SConstants.RIGHT) {
             d.print(" align=\"right\"");
         }
         else if (align == SConstants.JUSTIFY) {
             d.print(" align=\"justify\"");
         }
     }
 
     /**
      * HTML allows 4 values for the vertical align property of a div element.
      *
      * @param d     Output
      * @param align Please refer {@link SConstants}
      * @throws IOException
      */
     public static void printDivVerticalAlignment(Device d, int align) throws IOException {
         printTableVerticalAlignment(d, align);
     }
 
     /**
      * Vertical alignment for TABLE cells. i.e. <code>valign="top"</code>
      */
     private static void printTableVerticalAlignment(Device d, int align)
             throws IOException {
         if (align == SConstants.NO_ALIGN) {
             //d.print(" valign=\"center\"");
         }
         else if (align == SConstants.CENTER) {
             d.print(" valign=\"middle\"");
         }
         else if (align == SConstants.TOP) {
             d.print(" valign=\"top\"");
         }
         else if (align == SConstants.BOTTOM) {
             d.print(" valign=\"bottom\"");
         }
         else if (align == SConstants.BASELINE) {
             d.print(" valign=\"baseline\"");
         }
     }
 
     /**
      * Renders the alignment commands for a table cell (horzontal and vertical).
      * To ensure a consistent behaviour you have to pass the default alignment applied for <code>SConstants.NO_ALIGN</code>.
      *
      * @param defaultHorizontal default horizontal alignment to use is not aligned
      * @param defaultVertical   default vertical alignment to use if component is not aligned
      */
     public static void printTableCellAlignment(final Device d, final SComponent c,
                                                final int defaultHorizontal, final int defaultVertical)
             throws IOException {
         if (c != null) {
             final int horizontalAlignment = c.getHorizontalAlignment();
             final int verticalAlignment = c.getVerticalAlignment();
             printTableHorizontalAlignment(d, horizontalAlignment != SConstants.NO_ALIGN ? horizontalAlignment : defaultHorizontal);
             printTableVerticalAlignment(d, verticalAlignment != SConstants.NO_ALIGN ? verticalAlignment : defaultVertical);
         }
     }
 
     public static String toColorString(int rgb) {
         char[] buf = new char[6];
         int digits = 6;
         do {
             buf[--digits] = hexDigits[rgb & 15];
             rgb >>>= 4;
         }
         while (digits != 0);
 
         return new String(buf);
     }
 
     public static String toColorString(java.awt.Color c) {
         return toColorString(c.getRGB());
     }
 
     /**
      * Generates a StringBuilder containing inlined CSS styles for the following properties of a SComponent:
      * <p><ul><li>Preffered Size</li><li>Font</li><li>Background- and Foregroud color.</li></ul>
      *
      * @param component Component to grab parameters from.
      */
     public static StringBuilder generateCSSComponentInlineStyle(SComponent component) {
         final StringBuilder styleString = new StringBuilder();
         appendCSSInlineSize(styleString, component);
         appendCSSComponentInlineColorStyle(styleString, component);
         appendCSSComponentInlineFontStyle(styleString, component);
         return styleString;
     }
 
     /**
      * Append a inline CSS style definition for the passed component of the aspect foreground- and background color.
      *
      * @param styleString StringBuilder to append to
      * @param component   Component to use as style source
      * @return The passed styleString
      */
     public static StringBuilder appendCSSComponentInlineColorStyle(StringBuilder styleString, final SComponent component) {
         if (component != null) {
             if (component.getBackground() != null) {
                 styleString.append("background-color:#").append(toColorString(component.getBackground())).append(";");
             }
 
             if (component.getForeground() != null) {
                 styleString.append("color:#").append(toColorString(component.getForeground())).append(";");
             }
         }
         return styleString;
     }
 
     /**
      * Append a inline CSS style definition for the passed component of the aspect font properties.
      *
      * @param styleString StringBuilder to append to
      * @param component   Component to use as style source
      * @return The passed styleString
      */
     public static StringBuilder appendCSSComponentInlineFontStyle(final StringBuilder styleString, final SComponent component) {
         if (component != null && component.getFont() != null) {
             final SFont font = component.getFont();
             styleString.append("font-size:").append(font.getSize()).append("pt;");
             styleString.append("font-style:").append((font.getStyle() & SFont.ITALIC) > 0 ? "italic;" : "normal;");
             styleString.append("font-weight:").append((font.getStyle() & SFont.BOLD) > 0 ? "bold;" : "normal;");
             styleString.append("font-family:").append(font.getFace()).append(";");
         }
         return styleString;
     }
 
     /**
      * Appends a CSS inline style string for the preferred size of the passed component to the passed stringbuffer.
      * <p>Sample: <code>width:100%;heigth=15px"</code>
      */
     public static StringBuilder appendCSSInlineSize(StringBuilder styleString, SComponent component) {
         if (component == null)
             return styleString;
         SDimension preferredSize = component.getPreferredSize();
         if (preferredSize != null) {
             boolean msie = isMSIE(component);
             if (msie && "px".equals(preferredSize.getWidthUnit())) {
                 int oversize = calculateHorizontalOversize(component, false);
                 styleString
                         .append("width:")
                         .append(preferredSize.getWidthInt() - oversize)
                         .append("px;");
             }
             else if (!SDimension.AUTO.equals(preferredSize.getWidthUnit()))
                 styleString.append("width:").append(preferredSize.getWidth()).append(';');
 
             if (msie && "px".equals(preferredSize.getHeightUnit())) {
                 int oversize = calculateVerticalOversize(component, false);
                 styleString
                         .append("height:")
                         .append(preferredSize.getHeightInt() - oversize)
                         .append("px;");
             }
             else if (!SDimension.AUTO.equals(preferredSize.getHeightUnit()))
                 styleString.append("height:").append(preferredSize.getHeight()).append(';');
         }
         return styleString;
     }
 
     public static StringBuilder generateCSSInlineBorder(StringBuilder styles, int borderSize) {
         if (borderSize > 0) {
             styles.append("border:").append(borderSize).append("px solid black;");
         }
         else {
             //styleString.append("border:none;"); Not necessary. Default
         }
         return styles;
     }
 
     /**
      * Prints a HTML style attribute with widht/height of 100% if the passed dimension defines a height or width..
      * <p>Sample: <code> style="width:100%;"</code>
      * <p/>
      * <p>This is typicall needed to stretch inner HTML element to expand to the full dimenstion defined
      * on an outer, sized HTML element. Otherwise the component would appear to small (as size is applied only
      * on the invisible outer limiting element)
      *
      * @param device        Device to print to
      * @param preferredSize trigger dimension
      */
     public static void printCSSInlineFullSize(Device device, SDimension preferredSize) throws IOException {
         if ((preferredSize != null) &&
                 (!SDimension.AUTO.equals(preferredSize.getWidth()) ||
                   !SDimension.AUTO.equals(preferredSize.getHeight())))
         {
             // opera doesn't show height 100% when parent has no defined height
             if (!SDimension.AUTO.equals(preferredSize.getHeight())) {
                 device.print(" style=\"width:100%;height:100%\"");
             } else {
                 device.print(" style=\"width:100%\"");
             }
         }
     }
 
     /**
      * Prints a HTML style attribute with widht/height of 100% if the passed dimension defines a height or width..
      * <p>Sample: <code> style="width:100%;"</code>
      * <p/>
      * <p>This is typicall needed to stretch inner HTML element to expand to the full dimenstion defined
      * on an outer, sized HTML element. Otherwise the component would appear to small (as size is applied only
      * on the invisible outer limiting element)
      *
      * @param pStringBuilder buffer to append to
      * @param pComponent      preferredSize trigger dimension
      */
     public static void appendCSSInlineFullSize(StringBuilder pStringBuilder, SComponent pComponent) {
         SDimension preferredSize = pComponent.getPreferredSize();
         if (preferredSize != null &&
             (!SDimension.AUTO.equals(preferredSize.getWidth()) || !SDimension.AUTO.equals(preferredSize.getHeight())))
         {
             pStringBuilder.append("width:100%;height:100%;");
         }
     }
 
     /**
      * Writes an {X|HT}ML quoted string according to RFC 1866.
      * '"', '<', '>', '&'  become '&quot;', '&lt;', '&gt;', '&amp;'
      *
      * @param d              The device to print out on
      * @param s              the String to print
      * @param quoteNewline   should newlines be transformed into <code>&lt;br&gt;</code> tags
      * @param quoteSpaces    should spaces be transformed into <code>&amp;nbsp</code>  chars
      * @param quoteApostroph Quote apostroph <code>'</code> by <code>\'</code>
      * @throws IOException
      */
     public static void quote(final Device d, final String s, final boolean quoteNewline,
                              final boolean quoteSpaces, final boolean quoteApostroph)
             throws IOException {
         if (s == null) {
             return;
         }
         char[] chars = s.toCharArray();
         char c;
         int last = 0;
         for (int pos = 0; pos < chars.length; ++pos) {
             c = chars[pos];
             // write special characters as code ..
             if (c < 32 || c > 127) {
                 d.print(chars, last, (pos - last));
                 if (c == '\n' && quoteNewline) {
                     d.print("<br>");
                     if (pos == chars.length -1 || chars[pos + 1] == '\n') // insert &nbsp; in empty/sequence <br>s.
                         d.print("&nbsp;");
                 }
                 else {
                     d.print("&#");
                     d.print((int) c);
                     d.print(';');
                 } // end of if ()
                 last = pos + 1;
             }
             else {
                 switch (c) {
                     case '&':
                         d.print(chars, last, (pos - last));
                         d.print("&amp;");
                         last = pos + 1;
                         break;
                     case '"':
                         d.print(chars, last, (pos - last));
                         d.print("&quot;");
                         last = pos + 1;
                         break;
                     case '<':
                         d.print(chars, last, (pos - last));
                         d.print("&lt;");
                         last = pos + 1;
                         break;
                     case '>':
                         d.print(chars, last, (pos - last));
                         d.print("&gt;");
                         last = pos + 1;
                         break;
                         /*
                          * watchout: we cannot replace _space_ by &nbsp;
                          * since non-breakable-space is a different
                          * character: isolatin-char 160, not 32.
                          * This will result in a confusion in forms:
                          *   - the user enters space, presses submit
                          *   - the form content is written to the Device by wingS,
                          *     space is replaced by &nbsp;
                          *   - the next time the form is submitted, we get
                          *     isolatin-char 160, _not_ space.
                          * (at least Konqueror behaves this correct; mozilla does not)
                          *                                                       Henner
                          *
                          * But we must do this for IE, since it doesn't accept the
                          * white-space: pre; property...so conditionalize it.
                          *                                                       Ole
                          */
                     case ' ':
                         if (quoteSpaces) {
                             d.print(chars, last, (pos - last));
                             d.print("&nbsp;");
                             last = pos + 1;
                         }
                         break;
                         /* Needed for i.e. js-code. */
                     case '\'':
                         if (quoteApostroph) {
                             d.print(chars, last, (pos - last));
                             d.print('\\');
                             last = pos;
                         }
                         break;
 
                 }
             }
         }
         d.print(chars, last, chars.length - last);
     }
 
     /**
      * Writes text to the device without any HTML tag content.
      * @param device The output device to use for quoting
      * @param htmlWrappedText The text which may contain HTML to strip.
      * @return The amount of characters written to the ouput device
      * @throws IOException
      */
     public static int writeWithoutHTML(final Device device, final String htmlWrappedText) throws IOException {
         final char[] chars = htmlWrappedText.toCharArray();
         int pos = 0;
         int len = 0;
        int openBraces = 0;
         for (int c = 0; c < chars.length; c++) {
             switch (chars[c]) {
                 case '\n':
                     chars[c] = ' ';
                     break;
                case '<':                	
                	if (openBraces == 0) {
	                	len += (c - pos);
	                    device.print(chars, pos, c - pos);
                	}
                	openBraces++;
                     break;
                 case '>':
                	openBraces--;
                	if (openBraces == 0) {
                		pos = c + 1;
                	}
             }
         }
         final int remain = chars.length - pos;
         device.print(chars, pos, remain);
         len += remain;
         return len;
     }
 
     /**
      * write string as it is
      *
      * @param d
      * @param s
      * @throws IOException
      */
     public static void writeRaw(Device d, String s) throws IOException {
         if (s == null) {
             return;
         }
         d.print(s);
     }
 
     /**
      * writes the given String to the device. The string is quoted, i.e.
      * for all special characters in *ML, their appropriate entity is
      * returned.
      * If the String starts with '<html>', the content is regarded being
      * HTML-code and is written as is (without the <html> tag).
      */
     public static void write(Device d, String s) throws IOException {
         writeQuoted(d, s, false);
     }
 
 
     /**
      * writes the given String to the device. The string is quoted, i.e.
      * for all special characters in *ML, their appropriate entity is
      * returned.
      * If the String starts with '<html>', the content is regarded being
      * HTML-code and is written as is (without the <html> tag).
      * It is possible to define the quoteNewline behavoiur
      */
     public static void writeQuoted(Device d, String s, boolean quoteNewline) throws IOException {
         if (s == null) {
             return;
         }
         if ((s.length() > 5) && (s.substring(0,6).equalsIgnoreCase("<html>"))) {
             writeRaw(d, s.substring(6));
         }
         else {
             quote(d, s, quoteNewline, false, false);
         }
     }
 
     /**
      * Prints an <b>optional</b> attribute. If the String value has a content
      * (value != null && value.length > 0), the attrib is added otherwise
      * it is left out
      */
     public static void optAttribute(final Device d, String attr, final StringBuilder value)
             throws IOException {
         optAttribute(d, attr, value != null ? value.toString() : null);
     }
 
     /**
      * Prints an <b>optional</b> attribute. If the String value has a content
      * (value != null && value.length > 0), the attrib is added otherwise
      * it is left out
      */
     public static void optAttribute(final Device d, final char[] attributeName, final String value)
             throws IOException {
         if (value != null && value.length() > 0) {
             d.print(' ').print(attributeName).print("=\"");
             quote(d, value, true, false, false);
             d.print('"');
         }
     }
 
     /**
      * Prints an <b>optional</b> attribute. If the String value has a content
      * (value != null && value.length > 0), the attrib is added otherwise
      * it is left out
      */
     public static void optAttribute(final Device d, final String attributeName, final String value)
             throws IOException {
         if (value != null && value.length() > 0) {
             d.print(' ').print(attributeName).print("=\"");
             quote(d, value, true, false, false);
             d.print('"');
         }
     }
 
     /**
      * Prints an <b>mandatory</b> attribute. If the String value has a content
      * (value != null && value.length > 0), the attrib is added otherwise
      * it is left out
      */
     public static void attribute(final Device d, final String attr, final String value) throws IOException {
             d.print(' ').print(attr).print("=\"");
             if (value != null)
                quote(d, value, true, false, false);
             d.print('"');
     }
 
     /**
      * Prints an <b>optional</b> attribute. If the String value has a content
      * (value != null && value.length > 0), the attrib is added otherwise
      * it is left out
      */
     public static void optAttribute(final Device d, final String attr, final Color value)
             throws IOException {
         if (value != null) {
             d.print(' ');
             d.print(attr);
             d.print("=\"");
             write(d, value);
             d.print('"');
         }
     }
 
     /**
      * Prints an optional, renderable attribute.
      */
     public static void optAttribute(final Device d, final String attr, final Renderable r)
             throws IOException {
         if (r != null) {
             d.print(' ');
             d.print(attr);
             d.print("=\"");
             r.write(d);
             d.print('"');
         }
     }
 
     /**
      * Prints an <b>optional</b> attribute. If the integer value is greater than 0,
      * the attrib is added otherwise it is left out
      */
     public static void optAttribute(final Device d, final char[] attributeName, final int value)
             throws IOException {
         if (value > 0) {
             d.print(' ');
             d.print(attributeName);
             d.print("=\"");
             d.print(String.valueOf(value));
             d.print('"');
         }
     }
 
     /**
      * Prints an <b>optional</b> attribute. If the integer value is greater than 0,
      * the attrib is added otherwise it is left out
      */
     public static void optAttribute(final Device d, final String attributeName, final int value)
             throws IOException {
         if (value > 0) {
             d.print(' ');
             d.print(attributeName);
             d.print("=\"");
             d.print(String.valueOf(value));
             d.print('"');
         }
     }
 
     /**
      * Prints an <b>optional</b> attribute. If the dimension value not equals <i>null</i>
      * the attrib is added otherwise it is left out
      */
     public static void optAttribute(final Device d, final String attr, final SDimension value)
             throws IOException {
         if (value != null) {
             d.print(' ');
             d.print(attr);
             d.print("=\"");
             write(d, value.toString());
             d.print('"');
         }
     }
 
     /**
      * Prints all <b>optional</b> attributes that are contained in the
      * <code>Map</code>. The keys of the map should be instances
      * of <code>String</code> and the values one of the following
      * classes.<br/>
      * <ul>
      * <li>org.wings.util.StringBuilder</li>
      * <li>java.lang.String</li>
      * <li>java.awt.Color</li>
      * <li>org.wings.Renderable</li>
      * <li>java.lang.Integer</li>
      * <li>org.wings.SDimension</li>
      * </ul>
      *
      * @param d          The device to print the <b>optional</b> attributes.
      * @param attributes The <b>optional</b> attributes. The key is the attribute
      *                   name and the value is the attribute value.
      * @throws IOException The exception maybe thrown if an error occurs
      *                     while trying to write to device.
      */
     public static void optAttributes(final Device d, final Map attributes) throws IOException {
         if (attributes != null) {
             for (final Object o : attributes.entrySet()) {
                 Map.Entry entries = (Map.Entry) o;
 
                 Object key = entries.getKey();
                 if (key instanceof String) {
                     String attr = (String) key;
 
                     Object value = entries.getValue();
                     if (value instanceof StringBuilder) {
                         Utils.optAttribute(d, attr, (StringBuilder) value);
                     } else if (value instanceof String) {
                         Utils.optAttribute(d, attr, (String) value);
                     } else if (value instanceof Color) {
                         Utils.optAttribute(d, attr, (Color) value);
                     } else if (value instanceof Renderable) {
                         Utils.optAttribute(d, attr, (Renderable) value);
                     } else if (value instanceof Integer) {
                         Utils.optAttribute(d, attr, ((Integer) value).intValue());
                     } else if (value instanceof SDimension) {
                         Utils.optAttribute(d, attr, (SDimension) value);
                     }
                 }
             }
         }
     }
 
     /**
      * writes the given java.awt.Color to the device. Speed optimized;
      * character conversion avoided.
      */
     public static void write(final Device d, final Color c) throws IOException {
         d.print('#');
         int rgb = (c == null) ? 0 : c.getRGB();
         int mask = 0xf00000;
         for (int bitPos = 20; bitPos >= 0; bitPos -= 4) {
             d.print(hexDigits[(rgb & mask) >>> bitPos]);
             mask >>>= 4;
         }
     }
 
     /**
      * writes anything Renderable
      */
     public static void write(final Device d, final Renderable r) throws IOException {
         if (r == null) {
             return;
         }
         r.write(d);
     }
 
     /*
      * testing purposes.
      */
     public static void main(String argv[]) throws Exception {
         Color c = new Color(255, 254, 7);
         Device d = new org.wings.io.StringBuilderDevice(1024);
         write(d, c);
         quote(d, "\nThis is a <abc> string \"; foo & sons\nmoin", true, false, false);
         d.print(String.valueOf(-42));
         d.print(String.valueOf(Integer.MIN_VALUE));
 
         write(d, "hello test&nbsp;\n");
         write(d, "<html>hallo test&nbsp;\n");
 
         d = new org.wings.io.NullDevice();
         long start = System.currentTimeMillis();
         for (int i = 0; i < 1000000; ++i) {
             quote(d, "this is a little & foo", true, false, false);
         }
         System.out.println("took: " + (System.currentTimeMillis() - start) + "ms");
     }
 
     /**
      * Helper method for CGs to print out debug information in output stream.
      * If {@link #PRINT_DEBUG} prints the passed string and returns the current {@link Device}.
      * In other case omits ouput and returns a {@link NullDevice}
      *
      * @param d The original device
      * @return The original device or a {@link NullDevice}
      */
     public static Device printDebug(final Device d, final char[] s) throws IOException {
         if (PRINT_DEBUG) {
             return d.print(s);
         }
         else {
             return d;
         }
     }
 
     /**
      * Helper method for CGs to print out debug information in output stream.
      * If {@link #PRINT_DEBUG} prints the passed string and returns the current {@link Device}.
      * In other case omits ouput and returns a {@link NullDevice}
      *
      * @param d The original device
      * @return The original device or a {@link NullDevice}
      */
     public static Device printDebug(final Device d, final String s) throws IOException {
         if (PRINT_DEBUG) {
             return d.print(s);
         }
         else {
             return d;
         }
     }
 
     /**
      * Prints a hierarchical idented newline if debug mode is enabled.
      * {@link #printNewline(org.wings.io.Device, org.wings.SComponent)}
      */
     public static Device printDebugNewline(Device d, SComponent currentComponent) throws IOException {
         if (PRINT_DEBUG) {
             return printNewline(d, currentComponent);
         }
         else {
             return NullDevice.DEFAULT;
         }
     }
 
     /**
      * Prints a hierarchical idented newline. For each surrounding container of the passed component one ident level.
      */
     public static Device printNewline(final Device d, SComponent currentComponent) throws IOException {
         // special we save every ms handling for holger ;-)
         /* (OL) I took out the test for PRINT_DEBUG, since
          * sometimes we just need newlines (example tabbedPane)
          * I hope Holger doesn't need that microsecond ;)
          */
         if (currentComponent == null) {
             return d;
         }
         d.print('\n');
 
         if (PRINT_PRETTY) {
             SContainer current = currentComponent.getParent();
             while (current != null) {
                 d.print('\t');
                 current = current.getParent();
             }
         }
         return d;
     }
 
     /**
      * Prints a hierarchical idented newline. For each surrounding container of the passed component one ident level.
      */
     public static Device printNewline(final Device d, SComponent currentComponent, int offset) throws IOException {
         d.print('\n');
         if (PRINT_PRETTY) {
             while (currentComponent != null) {
                 d.print('\t');
                 currentComponent = currentComponent.getParent();
             }
         }
 
         while (offset > 0) {
             d.print('\t');
             offset--;
         }
         return d;
     }
 
 
     /**
      * loads a script from disk through the classloader.
      *
      * @param path the path where the script can be found
      * @return the script as a String
      */
     public static String loadScript(final String path) {
         InputStream in = null;
         BufferedReader reader = null;
 
         try {
             in = Thread.currentThread().getContextClassLoader().getResourceAsStream(path);
             reader = new BufferedReader(new InputStreamReader(in));
             StringBuilder buffer = new StringBuilder();
             String line;
             while ((line = reader.readLine()) != null) {
                 buffer.append(line).append("\n");
             }
             buffer.append("\n");
 
             return buffer.toString();
         }
         catch (Exception e) {
             log.warn("Unable to load script '" + path + "'", e);
             return "";
         }
         finally {
             try {
                 in.close();
             }
             catch (Exception ign) {
             }
             try {
                 reader.close();
             }
             catch (Exception ign1) {
             }
         }
     }
 
     /**
      * prints a String. Substitutes spaces with nbsp's
      */
     public static String nonBreakingSpaces(String text) {
         return text.replace(' ', '\u00A0');
     }
 
 
     /**
      * Takes a string, tokenizes it and appends the wordSuffix on each word.
      *
      * @param words      A list of words, may be <code>null</code>.
      * @param wordSuffix A suffix string to append to each word
      * @return modified string (<code>i.e. "slayout myclass","_box"</code>  gets <code>"slayout_box myclass_box"</code>).
      */
     public static String appendSuffixesToWords(String words, String wordSuffix) {
         if (words == null || words.length() == 0 || wordSuffix == null || wordSuffix.length() == 0) {
             return words;
         }
 
         // trivial case
         if (words.indexOf(' ') < 0) {
             return words + wordSuffix;
         }
 
         // more than one word
         StringTokenizer tokenizer = new StringTokenizer(words, " ");
         StringBuilder returnValue = new StringBuilder();
         while (tokenizer.hasMoreElements()) {
             returnValue.append(tokenizer.nextToken()).append(wordSuffix);
             if (tokenizer.hasMoreTokens()) {
                 returnValue.append(' ');
             }
         }
 
         return returnValue.toString();
     }
 
     /**
      * Prepends the component style class set on the component to the existing style string.
      *
      * @param component   Component may be <code>null</code> and may have a <code>null</code> style string.
      * @param styleString The style string to append
      */
     public static StringBuilder joinStyles(final SComponent component, final StringBuilder styleString) {
         if (component != null && component.getStyle() != null) {
             if (styleString != null) {
                 styleString.insert(0, " ");
                 styleString.insert(0, component.getStyle());
                 return styleString;
             }
             else {
                 return new StringBuilder(component.getStyle());
             }
         }
         else {
             return styleString;
         }
     }
 
     /**
      * Prepends the component style class set on the component to the existing style string.
      *
      * @param component   Component may be <code>null</code> and may have a <code>null</code> style string.
      * @param styleString The style string to append
      */
     public static String joinStyles(final SComponent component, final String styleString) {
         if (component != null && component.getStyle() != null) {
             if (styleString != null) {
                 return component.getStyle() + " " + styleString;
             }
             else {
                 return component.getStyle();
             }
         }
         else {
             return styleString != null ? styleString : "";
         }
     }
 
     public static void printButtonStart(Device device, SComponent eventTarget, String eventValue,
             boolean b, boolean showAsFormComponent) throws IOException {
         printButtonStart(device, eventTarget, eventValue, b, showAsFormComponent, null);
     }
 
     public static void printButtonStart(final Device device, final SComponent component, final String eventValue,
             final boolean enabled, final boolean formComponent, String cssClassName) throws IOException {
         if (enabled) {
             device.print("<a href=\"#\"");
             printClickability(device, component, eventValue, enabled, formComponent);
         } else {
             device.print("<span");
         }
 
         Utils.optAttribute(device, "class", cssClassName);
     }
 
     public static void printButtonEnd(final Device device, final boolean enabled) throws IOException {
         if (enabled)
             device.print("</a>");
         else
             device.print("</span>");
     }
 
     public static void printClickability(final Device device, final SComponent component, final String eventValue,
             final boolean enabled, final boolean formComponent) throws IOException {
         if (enabled) {
             // Render onclick JS listeners
             device.print(" onclick=\"wingS.request.sendEvent(event,");
             device.print(formComponent);
             device.print(',');
             device.print(!component.isReloadForced());
             device.print(",'");
             device.print(Utils.event(component));
             device.print("','");
             if (eventValue != null) {
                 device.print(eventValue);
             }
             device.print('\'');
             device.print(collectJavaScriptListenerCode(component, JavaScriptEvent.ON_CLICK));
             device.print("); return false;\"");
 
             // Render remaining JS listeners
             Utils.writeEvents(device, component, EXCLUDE_ON_CLICK);
         }
     }
 
     /**
      * Renders inline the javascript code attached to the passed javascipt event type
      * on the component. Used to allow usage of javascript events by the framework
      * as well as by the application itself.
      * <p> For an example: See the <code>wingS.request.sendEvent()</code>.
      *
      * @param component           The component wearing the event handler
      * @param javascriptEventType the event type declared in {@link JavaScriptEvent}
      * @return javascript code fragment n the form of <code>,new Array(function(){...},function(){...})</code>
      */
     public static String collectJavaScriptListenerCode(final SComponent component, final String javascriptEventType) {
         StringBuilder script = null;
         JavaScriptListener[] eventListeners = getEventTypeListeners(component, javascriptEventType);
         if (eventListeners != null && eventListeners.length > 0) {
             for (int i = 0; i < eventListeners.length; ++i) {
                 if (eventListeners[i].getCode() != null) {
                     if (script == null) {
                         script = new StringBuilder(64);
                     }
                     if (i > 0) {
                         script.append(",");
                     }
                     script.append("function(){").append(eventListeners[i].getCode()).append("}");
                 }
             }
             if (script != null && script.length() > 0) {
                 script.insert(0, ",new Array(");
                 script.append(")");
             }
         }
         return script != null ? script.toString() : "";
     }
 
     /**
      * @param button          The component wearing the event handler
      * @param javaScriptEvent the event type declared in {@link JavaScriptEvent}
      * @return The attached listeners to event type
      */
     private static JavaScriptListener[] getEventTypeListeners(final SComponent button, final String javaScriptEvent) {
         ArrayList<JavaScriptListener> result = new ArrayList<JavaScriptListener>();
         ScriptListener[] listeners = button.getScriptListeners();
         for (ScriptListener listener : listeners) {
             if (listener instanceof JavaScriptListener) {
                 JavaScriptListener jsListener = (JavaScriptListener) listener;
                 if (javaScriptEvent.equals(jsListener.getEvent().toLowerCase())) {
                     result.add(jsListener);
                 }
             }
         }
         return result.toArray(new JavaScriptListener[result.size()]);
     }
 
     public static StringBuilder inlineStyles(Style style) {
         if (style != null) {
             StringBuilder tabArea = new StringBuilder();
             tabArea.append(style.toString());
             return tabArea;
         }
         else {
             return null;
         }
     }
 
     /**
      * @return true if current browser is microsoft exploder
      */
     public static boolean isMSIE(SComponent component) {
         return component.getSession().getUserAgent().getBrowserType() == BrowserType.IE;
     }
 
     /**
      * @return true if current browser is microsoft exploder
      */
     public static boolean isMSIE() {
         return SessionManager.getSession().getUserAgent().getBrowserType() == BrowserType.IE;
     }
 
     /**
      * @param insets The inset param to test
      * @return <code>true</code> if any valid inset greater zero is set
      */
     public static boolean hasInsets(Insets insets) {
         return insets != null && (insets.top > 0 || insets.left > 0 || insets.right > 0 || insets.bottom > 0);
     }
 
     /**
      * Sets the preferred size of the given component. In case the component's current
      * dimension is unmodifiable (e.g. FULLAREA, FULLWIDTH, ...) a new dimension object
      * with the desired initial values is created and set for the component.
      *
      * @param component the component which needs to be changed in size
      * @param width the new width for the given component
      * @param height the new height for the given component
      */
     public static void setPreferredSize(SComponent component, String width, String height) {
         try {
             component.getPreferredSize().setSize(width, height);
         } catch(UnsupportedOperationException e) {
             component.setPreferredSize(new SDimension(width, height));
         }
     }
 
     /**
      * Sets the preferred size of the given component. In case the component's current
      * dimension is unmodifiable (e.g. FULLAREA, FULLWIDTH, ...) a new dimension object
      * with the desired initial values is created and set for the component.
      *
      * @param component the component which needs to be changed in size
      * @param width the new width for the given component
      * @param height the new height for the given component
      */
     public static void setPreferredSize(SComponent component, int width, int height) {
         try {
             component.getPreferredSize().setSize(width, height);
         } catch(UnsupportedOperationException e) {
             component.setPreferredSize(new SDimension(width, height));
         }
     }
 
     public static void optFullSize(Device device, SComponent component) throws IOException {
         SDimension dim = component.getPreferredSize();
         if (dim != null) {
             String width = dim.getWidth();
             boolean widthSet = width != null && !"".equals(width) && !SDimension.AUTO.equals(width);
             String height = dim.getHeight();
             boolean heightSet = height != null && !"".equals(height) && !SDimension.AUTO.equals(height);
             StringBuilder style = new StringBuilder();
             if (widthSet) {
                 style.append("width:100%;");
             }
             if (heightSet) {
                 style.append("height:100%;");
             }
             if (style.length() > 0)
                 Utils.optAttribute(device, "style", style);
         }
     }
 
     /**
      * Converts a hgap/vgap in according inline css padding style.
      *
      * @param insets The insets to generate CSS padding declaration
      * @return Empty or filled stringbuffer with padding declaration
      */
     public static StringBuilder createInlineStylesForInsets(Insets insets) {
         return createInlineStylesForInsets(new StringBuilder(), insets);
     }
 
     /**
      * Converts a hgap/vgap in according inline css padding style.
      *
      * @param device Appender to append inset style.
      * @param insets The insets to generate CSS padding declaration
      * @return Empty or filled stringbuffer with padding declaration
      */
     public static Device printInlineStylesForInsets(final Device device, final Insets insets) throws IOException {
         if (insets != null && (insets.top > 0 || insets.left > 0 || insets.right > 0 || insets.bottom > 0)) {
             if (insets.top == insets.left && insets.left == insets.right && insets.right == insets.bottom) {
                 device.print(" style=\"padding:").print(insets.top).print("px;\"");
             }
             else if (insets.top == insets.bottom && insets.left == insets.right) {
                 device.print(" style=\"padding:").print(insets.top).print("px ").print(insets.right).print("px;\"");
             }
             else {
                 device.print(" style=\"padding:").print(insets.top).print("px ").print(insets.right).print("px ")
                         .print(insets.bottom).print("px ").print(insets.left).print("px;\"");
             }
         }
         return device;
     }
 
     /**
      * Converts a hgap/vgap in according inline css padding style.
      *
      * @param styles Appender to append inset style.
      * @param insets The insets to generate CSS padding declaration
      * @return Empty or filled stringbuffer with padding declaration
      */
     public static StringBuilder createInlineStylesForInsets(StringBuilder styles, Insets insets) {
         if (insets != null && (insets.top > 0 || insets.left > 0 || insets.right > 0 || insets.bottom > 0)) {
             if (insets.top == insets.left && insets.left == insets.right && insets.right == insets.bottom) {
                 styles.append("padding:").append(insets.top).append("px;");
             }
             else if (insets.top == insets.bottom && insets.left == insets.right) {
                 styles.append("padding:").append(insets.top).append("px ").append(insets.right).append("px;");
             }
             else {
                 styles.append("padding:").append(insets.top).append("px ").append(insets.right).append("px ")
                         .append(insets.bottom).append("px ").append(insets.left).append("px;");
             }
         }
         return styles;
     }
 
     public static int calculateHorizontalOversize(SComponent component, boolean percentageUnitOnly) {
         if (component != null && isMSIE(component) && component instanceof STextComponent) {
             SDimension preferredSize = component.getPreferredSize();
             if (preferredSize != null) {
                 String widthUnit = preferredSize.getWidthUnit();
                 if (!SDimension.AUTO.equals(widthUnit)) {
                     if (percentageUnitOnly && !"%".equals(widthUnit))
                         return 0;
 
                     SAbstractBorder border = (SAbstractBorder) component.getBorder();
                     if (border != SDefaultBorder.INSTANCE) {
                         int oversize = 0;
                         int thickness = border.getThickness(SConstants.LEFT);
                         if (thickness != -1)
                             oversize += thickness;
                         thickness = border.getThickness(SConstants.RIGHT);
                         if (thickness != -1)
                             oversize += thickness;
                         final Insets insets = border.getInsets();
                         if (insets != null) {
                             oversize += insets.left + insets.right;
                         }
                         return oversize;
                     }
                     else {
                         return ((Integer)component.getClientProperty("horizontalOversize")).intValue();
                     }
                 }
             }
         }
         return 0;
     }
 
     public static int calculateVerticalOversize(SComponent component, boolean percentageUnitOnly) {
         if (component != null && isMSIE(component) && component instanceof STextComponent) {
             SDimension preferredSize = component.getPreferredSize();
             if (preferredSize != null) {
                 String heightUnit = preferredSize.getHeightUnit();
                 if (!SDimension.AUTO.equals(heightUnit)) {
                     if (percentageUnitOnly && !"%".equals(heightUnit))
                         return 0;
 
                     SAbstractBorder border = (SAbstractBorder) component.getBorder();
                     if (border != SDefaultBorder.INSTANCE) {
                         int oversize = 0;
                         int thickness = border.getThickness(SConstants.TOP);
                         if (thickness != -1)
                             oversize += thickness;
                         thickness = border.getThickness(SConstants.BOTTOM);
                         if (thickness != -1)
                             oversize += thickness;
                         final Insets insets = border.getInsets();
                         if (insets != null) {
                             oversize += insets.top + insets.bottom;
                         }
                         return oversize;
                     }
                     else {
                         return 4;
                     }
                 }
             }
         }
         return 0;
     }
 
     /**
      * Lookup keys for wings resources
      */
     public static final String JS_WINGS_ALL= "JS.wingsAll";
     public static final String JS_WINGS_ALL_DEBUG = "JS.wingsAllDebug";
 
     /**
      * Lookup keys for yui resources
      */
     public static final String CSS_YUI_ASSETS_CALENDAR = "CSS.yuiAssetsCalendar";
     public static final String CSS_YUI_ASSETS_CONTAINER = "CSS.yuiAssetsContainer";
     public static final String CSS_YUI_ASSETS_EDITOR = "CSS.yuiAssetsEditor";
     public static final String CSS_YUI_ASSETS_SIMPLE_EDITOR = "CSS.yuiAssetsSimpleeditor";
     public static final String IMG_YUI_ASSETS_EDITOR_SPRITE = "IMG.yuiAssetsEditorSprite";
     public static final String IMG_YUI_ASSETS_EDITOR_SPRITE_ACTIVE = "IMG.yuiAssetsEditorSpriteActive";
     public static final String CSS_YUI_ASSETS_LOGGER = "CSS.yuiAssetsLogger";
     public static final String IMG_YUI_ASSETS_SPRITE = "IMG.yuiAssetsSprite";
     public static final String JS_YUI_AUTOCOMPLETE = "JS.yuiAutocomplete";
     public static final String JS_YUI_CALENDAR = "JS.yuiCalendar";
     public static final String JS_YUI_CONTAINER = "JS.yuiContainer";
     public static final String JS_YUI_DATASOURCE = "JS.yuiDatasource";
     public static final String JS_YUI_EDITOR = "JS.yuiEditor";
     public static final String JS_YUI_EDITOR_SIMPLE = "JS.yuiEditorSimple";
     public static final String JS_YUI_LOGGER = "JS.yuiLogger";
     public static final String JS_YUI_SLIDER = "JS.yuiSlider";
     public static final String JS_YUI_UTILITIES = "JS.yuiUtilities";
     
     public static final String JS_YUI_ANIMATION_DEBUG = "JS.yuiAnimationDebug";
     public static final String JS_YUI_AUTOCOMPLETE_DEBUG = "JS.yuiAutocompleteDebug";
     public static final String JS_YUI_CALENDAR_DEBUG = "JS.yuiCalendarDebug";
     public static final String JS_YUI_CONNECTION_DEBUG = "JS.yuiConnectionDebug";
     public static final String JS_YUI_CONTAINER_DEBUG = "JS.yuiContainerDebug";
     public static final String JS_YUI_DATASOURCE_DEBUG = "JS.yuiDatasourceDebug";
     public static final String JS_YUI_DOM_DEBUG = "JS.yuiDomDebug";
     public static final String JS_YUI_DRAGDROP_DEBUG = "JS.yuiDragdropDebug";
     public static final String JS_YUI_EDITOR_DEBUG = "JS.yuiEditorDebug";
     public static final String JS_YUI_EDITOR_SIMPLE_DEBUG = "JS.yuiEditorSimpleDebug";
     public static final String JS_YUI_ELEMENT_DEBUG = "JS.yuiElementDebug";
     public static final String JS_YUI_EVENT_DEBUG = "JS.yuiEventDebug";
     public static final String JS_YUI_LOGGER_DEBUG = "JS.yuiLoggerDebug";
     public static final String JS_YUI_SLIDER_DEBUG = "JS.yuiSliderDebug";
     public static final String JS_YUI_YAHOO_DEBUG = "JS.yuiYahooDebug";
 
     /**
      * Lookup keys for other resources
      */
     public static final String JS_ETC_MENU = "JS.etcMenu";
     public static final String JS_ETC_POPUP = "JS.etcPopup";
     public static final String JS_ETC_WZ_TOOLTIP = "JS.etcWzTooltip";
     
     /**
      * Lookup keys for debug resources
      */
     public static final String JS_DEBUG_PI = "JS.debugPi";
     public static final String CSS_DEBUG_FIREBUG_LITE = "CSS.debugFirebugLite";
     public static final String JS_DEBUG_FIREBUG_LITE = "JS.debugFirebugLite";
     public static final String IMG_DEBUG_FIREBUG_LITE_SPRITE = "IMG.debugFirebugLiteSprite";
     public static final String IMG_DEBUG_FIREBUG_LITE_SPACER = "IMG.debugFirebugLiteSpacer";
     public static final String IMG_DEBUG_FIREBUG_LITE_OPEN = "IMG.debugFirebugLiteOpen";
     public static final String IMG_DEBUG_FIREBUG_LITE_CLOSE = "IMG.debugFirebugLiteClose";
     
     /**
      * Debug header management
      */
     private static final Map<Script, Script[]> debugHeaders = new HashMap<Script, Script[]>();
     private static final Map<Script, String> debugHeaderMappings = new HashMap<Script, String>();
     private static final Map<String, String[]> specialHeaderMappings = new HashMap<String, String[]>();
     
     private static final Renderable[] consoleHeaders = new Renderable[] {
         Utils.createExternalizedJSHeaderFromProperty(Utils.JS_DEBUG_PI),
         Utils.createExternalizedCSSHeaderFromProperty(Utils.CSS_DEBUG_FIREBUG_LITE),
         Utils.createExternalizedJSHeaderFromProperty(Utils.JS_DEBUG_FIREBUG_LITE),
         Utils.createExternalizedCSSHeaderFromProperty(Utils.CSS_YUI_ASSETS_LOGGER),
         Utils.createExternalizedJSHeaderFromProperty(Utils.JS_YUI_LOGGER_DEBUG)
     };
     
     static {
         specialHeaderMappings.put(
             JS_YUI_UTILITIES, new String[] {
                 JS_YUI_YAHOO_DEBUG,
                 JS_YUI_DOM_DEBUG,
                 JS_YUI_EVENT_DEBUG,
                 JS_YUI_CONNECTION_DEBUG,
                 JS_YUI_ANIMATION_DEBUG,
                 JS_YUI_DRAGDROP_DEBUG,
                 JS_YUI_ELEMENT_DEBUG
             }
         );
         
         new SResourceIcon((String) ResourceManager.getObject(Utils.IMG_DEBUG_FIREBUG_LITE_SPRITE, String.class)).getId();
         new SResourceIcon((String) ResourceManager.getObject(Utils.IMG_DEBUG_FIREBUG_LITE_SPACER, String.class)).getId();
         new SResourceIcon((String) ResourceManager.getObject(Utils.IMG_DEBUG_FIREBUG_LITE_OPEN, String.class)).getId();
         new SResourceIcon((String) ResourceManager.getObject(Utils.IMG_DEBUG_FIREBUG_LITE_CLOSE, String.class)).getId();
     }
     
     public static Script[] getDebugHeaders(Object header) {
         if (header instanceof Script) {
             Script script = (Script) header;
             String jsResourceProperty = debugHeaderMappings.get(script);
             if (jsResourceProperty != null) {
                 Script[] jsDebugHeaders = debugHeaders.get(script);
                 if (jsDebugHeaders == null) {
                     String[] jsResourceProperties = specialHeaderMappings.get(jsResourceProperty);
                     if (jsResourceProperties == null) {
                         jsResourceProperties = new String[] { jsResourceProperty + "Debug" };
                     }
                     int jsDebugHeaderCount = jsResourceProperties.length;
                     jsDebugHeaders = new Script[jsDebugHeaderCount];
                     for (int i = 0; i < jsDebugHeaderCount; ++i) {
                         Script jsDebugHeader = Utils.createExternalizedJSHeaderFromProperty(jsResourceProperties[i], false);
                         if (jsDebugHeader == null) {
                             return null;
                         } else {
                             jsDebugHeaders[i] = jsDebugHeader;
                         }
                     }
                     debugHeaders.put(script, jsDebugHeaders);
                 }
                 return jsDebugHeaders;
             }
         }
         return null;
     }
     
     public static Renderable[] getConsoleHeaders() {
         return consoleHeaders;
     }
     
     /**
      * Load a Javascript library that comes with wingS by a property. Check <code>JS_XXX</code> constants.
      * @param jsResourceProperty A property lookup key, preferably by a constant in this utility class
      * @return A script reference to the desired script addable as header
      */
     public static Script createExternalizedJSHeaderFromProperty(String jsResourceProperty) {
         return createExternalizedJSHeaderFromProperty(jsResourceProperty, true);
     }
     
     private static Script createExternalizedJSHeaderFromProperty(String jsResourceProperty, boolean remember) {
         String jsClassPath = (String) ResourceManager.getObject(jsResourceProperty, String.class);
         Script header = createExternalizedJSHeader(jsClassPath);
         if (header != null && remember) {
             debugHeaderMappings.put(header, jsResourceProperty);
         }
         return header;
     }
 
     /**
      * Load a Javascript library from the classpath.
      * @param jsClassPath A classpath to the .js-file
      * @return A script reference to the desired script addable as header
      */
     public static Script createExternalizedJSHeader(String jsClassPath) {
         if (jsClassPath == null) {
             return null;
         }
         ClassPathResource res = new ClassPathJavascriptResource(jsClassPath, HEADER_LOADED_CALLBACK);
         ExternalizeManager extMgr = SessionManager.getSession().getExternalizeManager();
         String jsUrl = extMgr.externalize(res, ExternalizeManager.GLOBAL);
         return new JavaScriptHeader(new SessionResource(jsUrl));
     }
 
     /**
      * Load a Stylesheet document that comes with wingS by a property. Check <code>CSS_XXX</code> constants.
      * @param cssResourceProperty A property lookup key, preferably by a constant in this utility class
      * @return A Link reference to the desired stylesheet addable as header
      */
     public static Link createExternalizedCSSHeaderFromProperty(String cssResourceProperty) {
         String cssClassPath = (String) ResourceManager.getObject(cssResourceProperty, String.class);
         return createExternalizedCSSHeader(cssClassPath);
     }
 
     /**
      * Load a Stylesheet document from the classpath.
      * @param cssClassPath A classpath to the .css-file
      * @return A Link reference to the desired stylesheet addable as header
      */
     public static Link createExternalizedCSSHeader(String cssClassPath) {
         ClassPathResource res = new ClassPathResource(cssClassPath, "text/css");
         ExternalizeManager extMgr = SessionManager.getSession().getExternalizeManager();
         String cssUrl = extMgr.externalize(res, ExternalizeManager.GLOBAL);
         return new StyleSheetHeader(new SessionResource(cssUrl));
     }
 
     public static Renderable listToJsArray(List list) {
         return new JSArray(list);
     }
 
     public static Renderable mapToJsObject(Map map) {
         return new JSObject(map);
     }
 
     private static final char[] BACKSLASH_N = "\\n".toCharArray();
     private static final char[] BACKSLASH_R = "\\r".toCharArray();
     private static final char[] BACKSLASH_T = "\\t".toCharArray();
     public static void encodeJS(Device d, Object o) throws IOException {
         // The common cases that never need escaping.
         if (o == null) {
             d.print("null");
             return;
         }
         if (o instanceof Number || o instanceof Boolean) {
             d.print(o.toString());
             return;
         }
         if (o instanceof Renderable) {
             ((Renderable)o).write(d);
             return;
         }
 
         d.print('\'');
         final String stringRep = o.toString();
         final char chars[] = stringRep.toCharArray();
         char c;
         int last = 0;
         for (int pos = 0; pos < chars.length; ++pos) {
             c = chars[pos];
             switch (c) {
             case '\'':
                 d.print(chars, last, (pos - last));
                 d.print('\\');
                 last = pos;  // next starts with single quote
                 break;
             case '\\':
                 d.print(chars, last, (pos - last));
                 d.print('\\');
                 last = pos;  // next starts with backslash
                 break;
             case '\b':
                 d.print(chars, last, (pos - last));
                 d.print("\\b");
                 last = pos + 1;
                 break;
             case '\f':
                 d.print(chars, last, (pos - last));
                 d.print("\\f");
                 last = pos + 1;
                 break;
             case '\n':
                 d.print(chars, last, (pos - last));
                 d.print(BACKSLASH_N);
                 last = pos + 1;
                 break;
             case '\r':
                 d.print(chars, last, (pos - last));
                 d.print(BACKSLASH_R);
                 last = pos + 1;
                 break;
             case '\t':
                 d.print(chars, last, (pos - last));
                 d.print(BACKSLASH_T);
                 last = pos + 1;
                 break;
             case '/':  // Regular expressions start with a slash.
                 d.print(chars, last, (pos - last));
                 d.print('\\');
                 last = pos;  // next starts with slash.
                 break;
             default:
                 if ((c >= '\u0000' && c <= '\u001F') || c >= '\u007F') {
                     d.print(chars, last, pos-last);
                     final String hex = Integer.toHexString(c);
                     d.print("\\u");
                     for (int j = 4 - hex.length(); j > 0; --j) {
                         d.print('0');
                     }
                     d.print(hex);
                     last = pos + 1;
                 }
             }
         }
         d.print(chars, last, chars.length - last);
         d.print('\'');
     }
 
     public static void writeAllAttributes(Device device, SComponent component) throws IOException {
         optAttribute(device, "class", component.getStyle());
         optAttribute(device, "id", component.getName());
         optAttribute(device, "style", getInlineStyles(component));
 
         if (component instanceof LowLevelEventListener) {
             optAttribute(device, "eid", component.getLowLevelEventId());
         }
 
         // Tooltip handling
         writeTooltipMouseOver(device, component);
 
         // Component popup menu
         writeContextMenu(device, component);
     }
 
     public static String getInlineStyles(SComponent component) {
         // write inline styles
         final StringBuilder builder = new StringBuilder();
 
         appendCSSInlineSize(builder, component);
 
         // Determine Style String
         Style allStyle = component.getDynamicStyle(SComponent.SELECTOR_ALL);
         if (component instanceof SAbstractIconTextCompound && ((SAbstractIconTextCompound)component).isSelected()) {
             // present, SComponent.getDynamicStyle() always is instance of CSSStyle
             CSSStyle selectedStyle = (CSSStyle)component.getDynamicStyle(SAbstractIconTextCompound.SELECTOR_SELECTED);
             if (selectedStyle != null) {
               if (allStyle != null) {
                   // make a copy to modify
                   allStyle = new CSSStyle(SComponent.SELECTOR_ALL, (CSSAttributeSet) allStyle);
                   allStyle.putAll(selectedStyle);
               } else {
                   allStyle = selectedStyle;
               }
             }
         }
         // Render Style string
         if (allStyle != null)
             builder.append(allStyle.toString());
 
         final SBorder border = component.getBorder();
         if (border != null) {
             if (border.getAttributes() != null)
                 builder.append(border.getAttributes().toString());
         }
         else
             builder.append("border:none;padding:0px");
 
         return builder.toString();
     }
 
     /**
      * Write JS code for context menus. Common implementaton for MSIE and gecko.
      */
     public static void writeContextMenu(Device device, SComponent component) throws IOException {
         final SPopupMenu menu = component.getComponentPopupMenu();
         if (menu != null && menu.isEnabled()) {
             final String componentId = menu.getName();
             final String popupId = componentId + "_pop";
             device.print(" onContextMenu=\"return wpm_menuPopup(event, '");
             device.print(popupId);
             device.print("');\" onMouseDown=\"return wpm_menuPopup(event, '");
             device.print(popupId);
             device.print("');\"");
         }
     }
 
     /**
      * Write Tooltip code.
      */
     public static void writeTooltipMouseOver(Device device, SComponent component)
     throws IOException {
         final String toolTipText = component != null ? component.getToolTipText() : null;
         if (toolTipText != null && toolTipText.length() > 0) {
             device.print(" onmouseover=\"Tip('");
             quote(device, toolTipText, true, false, true);
            device.print("', DURATION, 15000)\"");
         }
     }
 
     /**
      * Write Tooltip code.
      */
     public static void writeTooltipMouseOver(Device device, String toolTipText)
         throws IOException {
         if (toolTipText != null && toolTipText.length() > 0) {
             device.print(" onmouseover=\"Tip('");
             quote(device, toolTipText, true, false, true);
            device.print("', DURATION, 15000)\"");
         }
     }
 
     public static final boolean hasDimension(final SComponent component) {
         SDimension dim = component.getPreferredSize();
         return dim != null && (dim.getHeightInt() != SDimension.AUTO_INT
                                || dim.getWidthInt() != SDimension.AUTO_INT);
     }
 
     private static class JSArray implements Renderable {
         private final List list;
 
         public JSArray(List list) {
             this.list = list;
         }
 
         public void write(Device d) throws IOException {
             d.print('[');
             final Iterator it = list.iterator();
             boolean isFirst = true;
             while (it.hasNext()) {
                 if (!isFirst) d.print(',');
                 encodeJS(d, it.next());
                 isFirst = false;
             }
             d.print(']');
         }
 
         @Override
         public boolean equals(Object object) {
             return list.equals(object);
         }
 
         @Override
         public int hashCode() {
             return list.hashCode();
         }
     }
 
     private static final class JSObject implements Renderable {
         private final Map map;
 
         public JSObject(Map map) {
             this.map = map;
         }
 
         public void write(Device d) throws IOException {
             final Iterator it = map.entrySet().iterator();
             boolean isFirst = true;
             d.print('{');
             while (it.hasNext()) {
                 if (!isFirst) d.print(',');
                 Map.Entry entry = (Map.Entry) it.next();
                 d.print('\'').print(entry.getKey().toString()).print("':");
                 encodeJS(d, entry.getValue());
                 isFirst = false;
             }
             d.print('}');
         }
 
         @Override
         public boolean equals(Object object) {
             return map.equals(object);
         }
 
         @Override
         public int hashCode() {
             return map.hashCode();
         }
     }
 }
