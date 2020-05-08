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
 
 package org.wings.plaf.xhtml.old;
 
 import java.io.IOException;
 import javax.swing.Icon;
 
 import org.wings.*;
 import org.wings.plaf.*;
 import org.wings.plaf.xhtml.*;
 import org.wings.io.*;
 import org.wings.externalizer.ExternalizeManager;
 
 public final class TabbedPaneCG
     extends org.wings.plaf.xhtml.TabbedPaneCG
 {
     Icon firstIcon;
     Icon transIcon;
 
     public void installCG(SComponent component) {
         super.installCG(component);
         firstIcon = component.getSession().getCGManager().getIcon("TabbedPaneCG.firstIcon");
         transIcon = LookAndFeel.makeIcon(TabbedPaneCG.class, "/org/wings/icons/transdot.gif");
     }
 
 
     // ignore tab placement for now .. always TOP
     public void write(Device d, SComponent c)
         throws IOException
     {
         SBorder border = c.getBorder();
         STabbedPane pane = (STabbedPane)c;
 
         String firstAdr = null;
         String transAdr = null;
 
         ExternalizeManager ext = c.getExternalizeManager();
         if (ext != null) {
             try {
                 firstAdr = ext.externalize(firstIcon);
                 transAdr = ext.externalize(transIcon);
             }
             catch (java.io.IOException e) {
                 System.err.println(e.getMessage());
                 e.printStackTrace(System.err);
             }
         }
 
         SContainer buttons;
         SContainer contents;
         if (SBorderLayout.CENTER.equals(pane.getConstraintAt(0))) {
             buttons = (SContainer)pane.getComponentAt(1);
             contents = (SContainer)pane.getComponentAt(0);
         }
         else {
             buttons = (SContainer)pane.getComponentAt(0);
             contents = (SContainer)pane.getComponentAt(1);
         }
 
         d.append("<table cellpadding=\"0\" cellspacing=\"0\" border=\"0\" width=\"100%\">")
             .append("<tr><td width=\"17\"></td><td bgcolor=\"#000000\"><img src=\"")
             .append(transAdr)
            .append("\" width=\"1\" height=\"1\" /></td></tr></table>\n");
 
         //d.append("<table cellpadding=\"0\" cellspacing=\"0\" border=\"0\" width=\"100%\">")
         //    .append("<tr><td width=\"17\"></td><td bgcolor=\"#000000\"><img height=\"1\" width=\"1\"/></td></tr></table>\n");
 
         for (int i=0; i < buttons.getComponentCount(); i++) {
             d.append("<img src=\"")
                 .append(firstAdr)
                 .append("\" />");
 
             buttons.getComponentAt(i).write(d);
             d.append(" \n");
         }
         
         d.append("<table width=\"100%\" cellpadding=\"0\" cellspacing=\"0\" border=\"1\"><tr><td>");
 
         contents.write(d);
 
         d.append("</td></tr></table>");
     }
 }
 
 /*
  * Local variables:
  * c-basic-offset: 4
  * indent-tabs-mode: nil
  * End:
  */
