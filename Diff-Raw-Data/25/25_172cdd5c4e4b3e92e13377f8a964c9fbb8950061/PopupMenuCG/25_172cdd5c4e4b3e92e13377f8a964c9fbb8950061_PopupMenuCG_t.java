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
 package org.wings.plaf.css.msie;
 
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.wings.SComponent;
 import org.wings.SMenu;
 import org.wings.SMenuItem;
 import org.wings.SPopupMenu;
 import org.wings.io.Device;
 
import java.io.IOException;

 public class PopupMenuCG extends org.wings.plaf.css.PopupMenuCG {
     private final transient static Log log = LogFactory.getLog(PopupMenuCG.class);
 
     /* (non-Javadoc)
      * @see org.wings.plaf.css.PopupMenuCG#writeListAttributes(org.wings.io.Device, org.wings.SPopupMenu)
      */
     protected void writeListAttributes(final Device device, SPopupMenu menu) throws IOException {
         // calculate max length of children texts for sizing of layer
         int maxLength = 0;
         for (int i = 0; i < menu.getMenuComponentCount(); i++) {
             if (!(menu.getMenuComponent(i) instanceof SMenuItem))
                 continue;
             String text = ((SMenuItem)menu.getMenuComponent(i)).getText();
             if (text != null && text.length() > maxLength) {
                 maxLength = text.length();
                 if (menu.getMenuComponent(i) instanceof SMenu) {
                         maxLength = maxLength + 2; //graphics
                 }
             }
         }
         device.print(" style=\"width:");
         String stringLength = String.valueOf(maxLength * menu.getWidthScaleFactor());
         device.print(stringLength.substring(0,stringLength.lastIndexOf('.')+2));
         device.print("em;\"");
     }
 
     protected void printScriptHandlers(Device device, SComponent menuItem) throws IOException {
         device.print(" onmouseover=\"wpm_openMenu('");
         device.print(((SMenu)menuItem).getName());
        device.print("_pop','");
        device.print(((SMenu)menuItem).getParentMenu().getName());
         device.print("_pop');\"");
     }
 }
