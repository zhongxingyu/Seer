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
 
 
 import java.awt.event.ActionEvent;
 import java.awt.event.KeyEvent;
 import java.io.IOException;
 import java.util.HashMap;
 import java.util.Map;
 
 import javax.swing.AbstractAction;
 import javax.swing.Action;
 import javax.swing.ActionMap;
 import javax.swing.InputMap;
 import javax.swing.KeyStroke;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.wings.SComponent;
 import org.wings.SConstants;
 import org.wings.SIcon;
 import org.wings.STabbedPane;
 import org.wings.io.Device;
 import org.wings.session.Browser;
 import org.wings.session.BrowserType;
 import org.wings.session.SessionManager;
 import org.wings.style.CSSSelector;
 
 public class TabbedPaneCG extends AbstractComponentCG implements SConstants {
     private final transient static Log log = LogFactory.getLog(TabbedPaneCG.class);
 
     private static final Map placements = new HashMap();
     
     static {
         placements.put(new Integer(STabbedPane.TOP), "top");
         placements.put(new Integer(STabbedPane.BOTTOM), "bottom");
         placements.put(new Integer(STabbedPane.LEFT), "left");
         placements.put(new Integer(STabbedPane.RIGHT), "right");
     }
 
     public void installCG(SComponent component) {
         super.installCG(component);
 
         final STabbedPane tab = (STabbedPane) component;
         InputMap inputMap = new InputMap();
         inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, KeyEvent.ALT_DOWN_MASK, false), "previous");
         inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, KeyEvent.ALT_DOWN_MASK, false), "next");
         tab.setInputMap(inputMap);
 
         Action action = new AbstractAction() {
             public void actionPerformed(ActionEvent e) {
                 if (tab.getSelectedIndex() > 0 && "previous".equals(e.getActionCommand()))
                     tab.setSelectedIndex(tab.getSelectedIndex() - 1);
                 else if (tab.getSelectedIndex() < tab.getTabCount() - 1 && "next".equals(e.getActionCommand()))
                     tab.setSelectedIndex(tab.getSelectedIndex() + 1);
                 tab.requestFocus();
             }
         };
 
         ActionMap actionMap = new ActionMap();
         actionMap.put("previous", action);
         actionMap.put("next", action);
         tab.setActionMap(actionMap);
     }
 
     public void writeContent(final Device device, final SComponent component)
             throws java.io.IOException {
         STabbedPane tabbedPane = (STabbedPane) component;
         if (tabbedPane.getTabCount() > 0) {
             String style = component.getStyle();
             boolean childSelectorWorkaround = !component.getSession().getUserAgent().supportsCssChildSelector();
             int placement = tabbedPane.getTabPlacement();
     
             device.print("<table cellspacing=\"0\"");
             if (childSelectorWorkaround)
                 Utils.childSelectorWorkaround(device, style);
     
             Utils.printCSSInlineFullSize(device, component.getPreferredSize());
     
             Utils.writeEvents(device, component);
             device.print(">");
     
             if (placement == STabbedPane.TOP)
                 device.print("<tr><th placement=\"top\"");
             else if (placement == STabbedPane.LEFT)
                 device.print("<tr><th placement=\"left\"");
             else if (placement == STabbedPane.RIGHT)
                 device.print("<tr><td");
             else if (placement == STabbedPane.BOTTOM)
                 device.print("<tr><td");
     
             if (childSelectorWorkaround) {
                 if (placement == STabbedPane.TOP)
                     Utils.childSelectorWorkaround(device, "STabbedPane_top");
                 else if (placement == STabbedPane.LEFT)
                     Utils.childSelectorWorkaround(device, "STabbedPane_left");
                 else
                     Utils.childSelectorWorkaround(device, "STabbedPane_pane");
             }
             device.print(">");
     
             if (placement == STabbedPane.TOP || placement == STabbedPane.LEFT)
                 writeTabs(device, tabbedPane);
             else
                 writeSelectedPaneContent(device, tabbedPane);
     
             if (placement == STabbedPane.TOP)
                 device.print("</th></tr><tr><td");
             else if (placement == STabbedPane.LEFT)
                 device.print("</th><td");
             else if (placement == STabbedPane.RIGHT)
                 device.print("</td><th placement=\"right\"");
             else if (placement == STabbedPane.BOTTOM)
                 device.print("</td></tr><tr><th placement=\"bottom\"");
     
             if (childSelectorWorkaround) {
                 if (placement == STabbedPane.RIGHT)
                     Utils.childSelectorWorkaround(device, "STabbedPane_right");
                 else if (placement == STabbedPane.BOTTOM)
                     Utils.childSelectorWorkaround(device, "STabbedPane_bottom");
                 else
                     Utils.childSelectorWorkaround(device, "STabbedPane_pane");
             }
             device.print(">");
     
             if (placement == STabbedPane.TOP
                     || placement == STabbedPane.LEFT) {
                 writeSelectedPaneContent(device, tabbedPane);
                 device.print("</td></tr></table>");
             } else {
                 writeTabs(device, tabbedPane);
                 device.print("</th></tr></table>");
             }
         } else {
             Utils.printDebug(device, "<!-- tabbedPane has no tabs -->");
         }
     }
 
     /** Renders the currently selected pane of the tabbed Pane. */
     private void writeSelectedPaneContent(Device device, STabbedPane tabbedPane) throws IOException {
         SComponent selected = tabbedPane.getSelectedComponent();
         if (selected != null) {
             selected.write(device);
         }
     }
 
     private void writeTabs(Device device, STabbedPane tabbedPane) throws IOException {
         boolean childSelectorWorkaround = !tabbedPane.getSession().getUserAgent().supportsCssChildSelector();
         boolean showAsFormComponent = tabbedPane.getShowAsFormComponent();
        final Browser browser = tabbedPane.getSession().getUserAgent();
        // substitute whitespaces for konqueror and ie5.0x
        boolean nbspWorkaround = browser.getBrowserType().equals(
                BrowserType.KONQUEROR)
                || (browser.getBrowserType().equals(BrowserType.IE)
                        && browser.getMajorVersion() == 5 && browser
                        .getMinorVersion() <= .1);
 
         for (int i = 0; i < tabbedPane.getTabCount(); i++) {
             SIcon icon = tabbedPane.getIconAt(i);
             String title = tabbedPane.getTitleAt(i);
             String tooltip = tabbedPane.getToolTipText();
            if (nbspWorkaround)
                 title = nonBreakingSpaces(title);
             
             /*
              * needed here so that the tabs can be wrapped. else they are in
              * one long line. noticed in firefox and konqueror.
              */
             device.print("\n");
             
             if (showAsFormComponent)
                 device.print("<button name=\"")
                         .print(Utils.event(tabbedPane))
                         .print("\" value=\"")
                         .print(i)
                         .print("\"");
             else
                 device.print("<a href=\"")
                         .print(tabbedPane.getRequestURL()
                         .addParameter(Utils.event(tabbedPane) + "=" + i).toString())
                         .print("\"");
 
             if (i == tabbedPane.getSelectedIndex()) {
                 device.print(" selected=\"true\"");
                 if (tabbedPane.isFocusOwner())
                     Utils.optAttribute(device, "focus", tabbedPane.getName());
             }
 
             if (!tabbedPane.isEnabledAt(i))
                 device.print(" disabled=\"true\"");
 
             if (childSelectorWorkaround) {
                 String cssClassName = "STabbedPane_Tab_" + placements.get(new Integer(tabbedPane.getTabPlacement()));
                 if (i == tabbedPane.getSelectedIndex()) {
                     Utils.childSelectorWorkaround(device, cssClassName + " STabbedPane_Tab_selected");
                 } else if (!tabbedPane.isEnabledAt(i)) {
                     Utils.childSelectorWorkaround(device, cssClassName + " STabbedPane_Tab_disabled");
                 } else {
                     Utils.childSelectorWorkaround(device, cssClassName);
                 }
             }
             device.print(">");
 
             if (icon != null && tabbedPane.getTabPlacement() != STabbedPane.RIGHT) {
                 device.print("<img");
                 Utils.optAttribute(device, "src", icon.getURL());
                 Utils.optAttribute(device, "alt", tooltip);
                 Utils.optAttribute(device, "width", icon.getIconWidth());
                 Utils.optAttribute(device, "height", icon.getIconHeight());
                device.print(" style=\"margin-left:0.2em;\"/>");
             }
            device.print("&nbsp;");
 
             Utils.write(device, title);
             device.print("&nbsp;");
 
             if (icon != null && tabbedPane.getTabPlacement() == STabbedPane.RIGHT) {
                 device.print("&nbsp;<img");
                 Utils.optAttribute(device, "src", icon.getURL());
                 Utils.optAttribute(device, "alt", tooltip);
                 Utils.optAttribute(device, "width", icon.getIconWidth());
                 Utils.optAttribute(device, "height", icon.getIconHeight());
                 device.print("/>");
             }
 
             if (showAsFormComponent)
                 device.print("</button>");
             else
                 device.print("</a>");
         }
     }
 
     private String nonBreakingSpaces(String title) {
         return title.replace(' ', '\u00A0');
     }
 
     public CSSSelector  mapSelector(CSSSelector selector) {
         Browser browser = SessionManager.getSession().getUserAgent();
         CSSSelector mappedSelector = null;
         if (browser.getBrowserType().equals(BrowserType.IE))
             mappedSelector = (CSSSelector) msieMappings.get(selector);
         else
             mappedSelector = (CSSSelector) geckoMappings.get(selector);
         return mappedSelector != null ? mappedSelector : selector;
     }
 
     private static final Map msieMappings = new HashMap();
     private static final Map geckoMappings = new HashMap();
     static {
         msieMappings.put(STabbedPane.SELECTOR_SELECTION, new CSSSelector (" *.STabbedPane_selected"));
         msieMappings.put(STabbedPane.SELECTOR_CONTENT, new CSSSelector (" td.STabbedPane_pane"));
         msieMappings.put(STabbedPane.SELECTOR_TABS, new CSSSelector (" table.STabbedPane th"));
         geckoMappings.put(STabbedPane.SELECTOR_SELECTION, new CSSSelector (" > table > tbody > tr > th > *[selected=\"true\"]"));
         geckoMappings.put(STabbedPane.SELECTOR_CONTENT, new CSSSelector (" > table > tbody > tr > td"));
         geckoMappings.put(STabbedPane.SELECTOR_TABS, new CSSSelector (" > table > tbody > tr > th"));
     }
 }
