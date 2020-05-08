 /*
  * $Id$
  * Copyright 2000,2005 wingS development team.
  *
  * This file is part of wingS (http://www.j-wings.org).
  *
  * wingS is free software; you can redistribute it and/or modify
  * it under the terms of the GNU Lesser General Public License
  * as published by the Free Software Foundation; either version 2.1
  * of the License, or (at your option) any later version.
  *
  * Please see COPYING for the complete licence.
  */
 package org.wings.plaf.css;
 
 
 import org.wings.SIcon;
 import org.wings.io.Device;
import org.wings.session.BrowserType;
import org.wings.session.SessionManager;
 import java.io.IOException;
 
 public abstract class AbstractLabelCG extends AbstractComponentCG {
 
     /** Prefer other writeText method! */
     protected void writeText(Device device, String text) throws IOException {
         writeText(device, text, false);
     }
 
     protected void writeText(Device device, String text, boolean wordWrap) throws IOException {
        boolean isIE = SessionManager.getSession().getUserAgent().getBrowserType().equals(BrowserType.IE);
 
         if ((text.length() > 5) && (text.startsWith("<html>"))) {
             Utils.writeRaw(device, text.substring(6));
        } else if (isIE) {
            // IE doesn't handle whitespace:pre correctly, so we do it hardcore by quoting spaces
            // or not (depending on wordwrap)
             Utils.quote(device, text, true, !wordWrap, false);
        } else {
             // Other browser handle the CSS property 'white-space' correctly.
             // The default is set as in swing: don't wrap:
             if (!wordWrap)
                 Utils.quote(device, text, false, false, false);
             else {
                 // non-default case: overwrite css property, set back to wrap
                 device.print("<span style=\"white-space: normal;\">");
                 Utils.quote(device, text, true, false, false);
                 device.print("</span>");
             }
        }
     }
 
     protected void writeIcon(Device device, SIcon icon) throws IOException {
         device.print("<img");
         Utils.optAttribute(device, "src", icon.getURL());
         Utils.optAttribute(device, "width", icon.getIconWidth());
         Utils.optAttribute(device, "height", icon.getIconHeight());
         device.print(" alt=\"");
         device.print(icon.getIconTitle());
         device.print("\"/>");
     }
 
 }
