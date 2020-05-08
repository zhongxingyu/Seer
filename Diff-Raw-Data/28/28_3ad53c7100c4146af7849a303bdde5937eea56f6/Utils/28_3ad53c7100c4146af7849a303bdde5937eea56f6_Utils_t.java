 /*
  * $Id$
  * (c) Copyright 2000 wingS development team.
  *
  * This file is part of wingS (http://wings.mercatis.de).
  *
  * wingS is free software; you can redistribute it and/or modify
  * it under the terms of the GNU Lesser General Public License
  * as published by the Free Software Foundation; either version 2.1
  * of the License, or (at your option) any later version.
  *
  * Please see COPYING for the complete licence.
  */
 
 package org.wings.plaf.xhtml;
 
 import java.io.IOException;
 
 import java.awt.Color;
 
 import org.wings.*; import org.wings.border.*;
 import org.wings.border.*;
 import org.wings.style.*;
 import org.wings.io.Device;
 
 /**
  * @author Holger Engels
  * @version $Revision$
  */
 public final class Utils implements SConstants
 {
 
     final static char[] hexDigits = {
         '0' , '1' , '2' , '3' , '4' , '5' ,
         '6' , '7' , '8' , '9' , 'a' , 'b' ,
         'c' , 'd' , 'e' , 'f'};
 
 
     private Utils() {
     }
 
     public static void writeBorderPrefix(Device d, SBorder border)
         throws IOException
     {
         if (border != null)
             border.writePrefix(d);
     }
 
     public static void writeBorderPostfix(Device d, SBorder border)
         throws IOException
     {
         if (border != null)
             border.writePostfix(d);
     }
 
     public static void writeContainerContents(Device d, SContainer c)
         throws IOException
     {
         SLayoutManager layout = c.getLayout();
 
         if (layout != null) {
           layout.write(d);
         }
         else {
             for (int i=0; i < c.getComponentCount(); i++)
               c.getComponent(i).write(d);
         }
     }
 
     public static void writeHiddenComponent(Device d, String name, String value)
         throws IOException
     {
         d.print("<input type=\"hidden\" name=\"")
             .print(name).print("\" value=\"")
             .print(value).print("\" />"); 
     }
 
 
     public static String escapeSpecialChars(String text) {
         StringBuffer escaped = new StringBuffer(text.length());
         for (int i = 0; i < text.length(); i++) {
             char c = text.charAt(i);
             switch (c) {
             case '&' : escaped.append("&amp;"); break;
             case '<' : escaped.append("&lt;"); break;
             case '>' : escaped.append("&gt;"); break;
             case '"' : escaped.append("&quot;"); break;
             case '\n': escaped.append("<br />"); break; // HTML formatting
             default:   escaped.append(c);
             }
         }
         return escaped.toString();
     }
 
     /**
      * writes an {X|HT}ML quoted string according to RFC 1866.
      * '"', '<', '>', '&'  become '&quot;', '&lt;', '&gt;', '&amp;'
      */
     // not optimized yet.
     public static void quote(Device d, String s) throws IOException {
 	int len = s.length();
 	char c;
 	for (int pos = 0; pos < len; ++pos) {
 	    switch ((c = s.charAt(pos))) {
 	    case '&': d.print("&amp;"); break;
 	    case '"': d.print("&quot;");break;
 	    case '<': d.print("&lt;");  break;
 	    case '>': d.print("&gt;");  break;
 	    default: d.print(c);
 	    }
 	}
     }
 
     public static String toColorString(int rgb) {
         char[] buf = new char[6];
         int digits = 6;
         do {
             buf[--digits] = hexDigits[rgb & 15];
             rgb >>>= 4;
         }
         while (digits!=0);
 
         return new String(buf);
     }
 
     public static String toColorString(java.awt.Color c) {
         return toColorString(c.getRGB());
     }
    
    public static void printTableToolTip(Device s, SComponent c)
        throws IOException {
        String tooltip = null;
        if ( ( tooltip = c.getToolTipText() ) != null ) {
            s.print(" title=\""+tooltip+"\"");
        }
    }
 
     public static void printTableCellAlignment(Device s, SComponent c) 
         throws IOException {
         switch (c.getHorizontalAlignment()) {
         case NO_ALIGN:
             break;
         case CENTER:
             s.print(" align=\"center\"");
             break;
         case LEFT:
             s.print(" align=\"left\"");
             break;
         case RIGHT:
             s.print(" align=\"right\"");
             break;
         case JUSTIFY:
             s.print(" align=\"justify\"");
             break;
         }
     
         switch (c.getVerticalAlignment()) {
         case NO_ALIGN:
         case CENTER:
             break;
         case TOP:
             s.print(" valign=\"top\"");
             break;
         case BOTTOM:
             s.print(" valign=\"bottom\"");
             break;
         case BASELINE:
             s.print(" valign=\"baseline\"");
             break;
         }
     }
 
     /** Encolors the actual table cell with the background of the contained component. */
     public static void printTableCellColors(Device s, SComponent c) 
         throws IOException {
         // if (c.getForeground()!=null)
         //     s.print(" COLOR=#").
         //         print(toColorString(c.getForeground()));
 
        if (c.getBackground()!=null){
             s.print(" bgcolor=\"#")
                 .print(toColorString(c.getBackground()))
                 .print("\"");
            s.print(" style=\"background-color:#")
                .print(toColorString(c.getBackground()))
                .print(";\"");
        }
     }
 
     public static void printTableCellSpan(Device s, SComponent c) {
     }
 
 
     public static void printTableCellAttributes(Device s, SComponent c) 
         throws IOException {
         printTableCellColors(s, c);
         printTableCellAlignment(s,c);
         printTableCellSpan(s,c);
     }
 
     public static void printIcon(Device d, SIcon icon, String align) 
         throws IOException {
         if ( icon==null )
             return;
 
         d.print("<img src=\"");
         icon.getURL().write(d);
         d.print("\"");
         if (align != null)
             d.print(" valign=\"").print(align).print("\"");
         if ( icon.getIconWidth() > 0)
             d.print(" width=\"").print(icon.getIconWidth()).print("\"");
         if ( icon.getIconHeight() > 0)
             d.print(" height=\"").print(icon.getIconHeight()).print("\"");
         d.print(" border=\"0\">");
 
     }
 
     public static void printBlindIcon(Device d, SIcon icon, int height, 
                                       int width) throws IOException {
         d.print("<img src=\"").
             print(icon.getURL()).
             print("\"").
             print(" width=\"").print(width).print("\"").
             print(" height=\"").print(height).print("\"").
             print(" border=\"0\">");
 
     }
     
     
     /**
       * Test, if either background-, foreground color, font or border is set.
       * @return false, if no attribute was set, true otherwise.
       */
     public static boolean hasSpanAttributes(SComponent component) {
          return component != null && !component.getAttributes().isEmpty();
      }
     
     /**
      * Write all span-attributes except the <i>class</i>.
      * @return null, if not attribute was set, otherwise the attributes 
      *         in css syntax otherwise.
      */
     public static void writeSpanAttributes(Device d, SComponent component)
     	throws IOException
      {
         if (!hasSpanAttributes(component))
            return;
             
            Utils.writeAttributes(d, component);
      }
     
 
     /**
      * Write some attributes which are interesting for cellRendere Option Lists !
      * @return null, the attributes  in css syntax 
      */
     public static void writeAttributes(Device d, SComponent component)
     	throws IOException
      {
         java.awt.Color bgcolor = component.getBackground();
         java.awt.Color fgcolor = component.getForeground();
         SFont font = component.getFont();
         SBorder border = component.getBorder();
         SDimension dim = component.getPreferredSize();
         
         if (bgcolor != null) d.print("background-color:#").print(toColorString(bgcolor)).print(";");
         if (fgcolor != null) {
             d.print("font-color:#").print(toColorString(fgcolor)).print(";");
             d.print("color:#").print(toColorString(fgcolor)).print(";");
         }
         if (font != null) {
             int style = font.getStyle();
             d.print("font-size:").print(font.getSize()).print("pt;");
             d.print("font-style:").print((style & java.awt.Font.ITALIC) > 0 ?"italic;":"normal;");
             d.print("font-weight:").print((style & java.awt.Font.BOLD) > 0 ?"bold;":"normal;");
             d.print("font-family:").print(font.getFace()).print(";");
         }
         
         if (border != null)
             border.writeSpanAttributes(d);
 
         if (dim != null) {
             if (dim.width != null) d.print("width:").print(dim.width).print(";");
             if (dim.height != null) d.print("height:").print(dim.height).print(";");
         }
      }
 }
 
 /*
  * Local variables:
  * c-basic-offset: 4
  * indent-tabs-mode: nil
  * compile-command: "ant -emacs -find build.xml"
  * End:
  */
