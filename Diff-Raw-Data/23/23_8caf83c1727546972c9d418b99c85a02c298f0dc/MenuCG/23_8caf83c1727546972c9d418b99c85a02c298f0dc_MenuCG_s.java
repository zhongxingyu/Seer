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
 
 
 import org.wings.*;
 import org.wings.io.Device;
 import org.wings.plaf.Update;
 
 import java.awt.event.ActionListener;
 import java.io.IOException;
 
 public final class MenuCG extends org.wings.plaf.css.MenuItemCG implements
         org.wings.plaf.MenuCG {
 
     private static final long serialVersionUID = 1L;
     private SResourceIcon arrowIcon = new SResourceIcon("org/wings/icons/MenuArrowRight.gif");
     {
         arrowIcon.getId();
     }
     private SResourceIcon arrowIconDisabled = new SResourceIcon("org/wings/icons/MenuArrowRight_Disabled.gif");
     {
         arrowIconDisabled.getId();
     }
 
     public void installCG(final SComponent comp) {
         super.installCG(comp);
     }
 
     public void uninstallCG(final SComponent comp) {
     }
 
     public void writePopup(final Device device, SMenu menu)
             throws IOException {
         if (menu.isEnabled()) {
             device.print("<ul");
             writeListAttributes(device, menu);
             device.print(" class=\"SMenu\">");
             for (int i = 0; i < menu.getMenuComponentCount(); i++) {
                 SComponent menuItem = menu.getMenuComponent(i);
 
                 if (menuItem.isVisible()) {
                     device.print("\n <li class=\"");
                     if (menuItem instanceof SMenuItem) {
                         if (menuItem instanceof SMenu) {
                             device.print("SMenu");
                         } else {
                             device.print("SMenuItem");
                         }
                         if (!menuItem.isEnabled()) {
                             device.print("_Disabled");
                        }                        
                         printScriptHandlers(device, menuItem, "onmouseover");
                     } else {
                        device.print("SMenuComponent");
                     }
                    device.print("\">");
                     if (menuItem instanceof SMenuItem) {
                         device.print("<a href=\"#\"");
                         if (menuItem instanceof SMenu) {
                             if (menuItem.isEnabled()) {
                                 device.print(" class=\"x sub\"");
                             } else {
                                 device.print(" class=\"y sub\"");
                             }
                         }
                         if (menuItem.getListeners(ActionListener.class).length == 0) {
                             // Prevent an unnecessary server roundtrip in case we've
                             // got a menu or menu item with no actions attached to it.
                             if (menuItem instanceof SMenu) {
                                 // In case we've got a (sub-) menu we render the same
                                 // 'ScriptHandler' as we've already done on 'mouseover'.
                                 // Actually this is not mandatory but it makes menues
                                 // usable on devices where 'mouseover' isn't working,
                                 // e.g. on this fancy e-board in our presentation room.
                                 printScriptHandlers(device, menuItem, "onclick");
                             }
                         } else {
                             Utils.printClickability(
                                     device,
                                     menuItem,
                                     ((SMenuItem) menuItem).getToggleSelectionParameter(),
                                     menuItem.isEnabled(),
                                     menuItem.getShowAsFormComponent());
                         }
                         device.print(">");
                     }
                     menuItem.write(device);
                     if (menuItem instanceof SMenuItem) {
                         device.print("</a>");
                     }
                     if (menuItem.isEnabled() && menuItem instanceof SMenu) {
                         menuItem.putClientProperty("popup", Boolean.TRUE);
                         menuItem.write(device);
                         menuItem.putClientProperty("popup", null);
                     }
                     device.print("</li>");
                 }
             }
             device.print("</ul>");
         }
         device.print("\n");
     }
 
     /* (non-Javadoc)
      * @see org.wings.plaf.css.MenuCG#printScriptHandlers(org.wings.io.Device, org.wings.SComponent)
      */
     protected void printScriptHandlers(Device device, SComponent menuItem, String handler) throws IOException {
         // Print the script handlers if this is a SMenu OR if the parent has both, items and menus as childs.
         // In the latter case a menu item might need to close an open submenu from a menu on the same level.
         SMenuItem tMenuItem = (SMenuItem) menuItem;
         if (!(tMenuItem instanceof SMenu)) {
             if (tMenuItem.getParentMenu() != null && tMenuItem.getParentMenu() instanceof SMenu) {
                 SMenu tParentMenu = (SMenu) tMenuItem.getParentMenu();
                 boolean tHasMenuChild = false;
                 boolean tHasMenuItemChild = false;
                 for (int tChildIndex = 0; tChildIndex < tParentMenu.getMenuComponentCount(); tChildIndex++) {
                     SComponent tChild = tParentMenu.getChild(tChildIndex);
                     if (tChild instanceof SMenu) {
                         tHasMenuChild = true;
                     } else {
                         tHasMenuItemChild = true;
                     }
                 }
 
                 // No handler if not both types are present
                 if (!(tHasMenuChild && tHasMenuItemChild)) {
                     return;
                 }
             }
         }
 
         device.print(" ").print(handler).print("=\"wpm_openMenu(event, '");
         device.print(tMenuItem.getName());
         device.print("_pop','");
         device.print(tMenuItem.getParentMenu().getName());
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
             String text = ((SMenuItem) menu.getMenuComponent(i)).getText();
             if (text != null && text.length() > maxLength) {
                 maxLength = text.length();
                 if (menu.getMenuComponent(i) instanceof SMenu) {
                     maxLength = maxLength + 2; //graphics
                 }
             }
         }
         device.print(" style=\"width:");
         String stringLength = String.valueOf(maxLength * menu.getWidthScaleFactor());
         device.print(stringLength.substring(0, stringLength.lastIndexOf('.') + 2));
         device.print("em;\"");
         device.print(" id=\"");
         device.print(menu.getName());
         device.print("_pop\"");
     }
 
     public void writeInternal(final Device device, final SComponent _c)
         throws IOException {
         SMenu menu = (SMenu) _c;
         if (menu.getClientProperty("popup") == null)
             writeItemContent(device, menu);
         else
             writePopup(device, menu);
     }
 
     public Update getComponentUpdate(SComponent component) {
         SComponent parentMenu = ((SMenu) component).getParentMenu();
         if (parentMenu != null)
             return parentMenu.getCG().getComponentUpdate(parentMenu);
         else
             return new ComponentUpdate(this, component);
     }
 
     protected static class ComponentUpdate extends AbstractComponentCG.ComponentUpdate {
 
         public ComponentUpdate(AbstractComponentCG cg, SComponent component) {
             super(cg, component);
         }
 
         @Override
         public Handler getHandler() {
             component.putClientProperty("popup", Boolean.TRUE);
             UpdateHandler handler = (UpdateHandler) super.getHandler();
             component.putClientProperty("popup", null);
 
             handler.setName("componentMenu");
             handler.setParameter(0, component.getName() + "_pop");
 
             return handler;
         }
 
     }
 
 }
