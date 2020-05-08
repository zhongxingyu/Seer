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
 
 
import java.io.IOException;

 import org.wings.SComponent;
 import org.wings.SMenu;
 import org.wings.SMenuItem;
 import org.wings.io.Device;
 
 public class MenuCG extends org.wings.plaf.css.MenuCG {
 
     /* (non-Javadoc)
      * @see org.wings.plaf.css.MenuCG#printScriptHandlers(org.wings.io.Device, org.wings.SComponent)
      */
     protected void printScriptHandlers(Device device, SComponent menuItem) throws IOException {
         device.print(" onmouseover=\"wpm_openMenu('");
         device.print(((SMenu)menuItem).getName());
        device.print("_pop');\" onmouseout=\"wpm_closeMenu('");
        device.print(((SMenu)menuItem).getName());
         device.print("_pop');\"");
     }
 
     /* (non-Javadoc)
      * @see org.wings.plaf.css.MenuCG#writeListAttributes(org.wings.io.Device, org.wings.SMenu)
      */
     protected void writeListAttributes(Device device, SMenu menu) throws IOException {
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
         device.print(" id=\"");
         device.print(menu.getName());
         device.print("_pop\"");
     }
 }
