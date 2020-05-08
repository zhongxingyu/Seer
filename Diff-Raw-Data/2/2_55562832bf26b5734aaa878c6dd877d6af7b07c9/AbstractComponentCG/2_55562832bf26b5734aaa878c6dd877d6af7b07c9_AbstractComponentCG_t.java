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
 
 import org.wings.LowLevelEventListener;
 import org.wings.SComponent;
 import org.wings.SConstants;
 import org.wings.SDimension;
 import org.wings.SIcon;
 import org.wings.SPopupMenu;
 import org.wings.SResourceIcon;
 import org.wings.border.SBorder;
 import org.wings.border.STitledBorder;
 import org.wings.dnd.DragSource;
 import org.wings.io.Device;
 import org.wings.plaf.ComponentCG;
 import org.wings.plaf.css.dwr.CallableManager;
 import org.wings.script.ScriptListener;
 import org.wings.style.Style;
 import org.wings.util.SStringBuilder;
 
 import javax.swing.*;
 import java.io.IOException;
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 
 /**
  * Partial CG implementation that is common to all ComponentCGs.
  *
  * @author <a href="mailto:engels@mercatis.de">Holger Engels</a>
  * @version $Revision$
  */
 public abstract class AbstractComponentCG implements ComponentCG, SConstants, Serializable {
     /**
      * An invisible icon / graphic (spacer graphic)
      */
     private static SIcon BLIND_ICON;
 
     protected AbstractComponentCG() {
     }
 
     protected void writeTablePrefix(Device device, SComponent component) throws IOException {
         writePrefix(device, component, true);
     }
 
     protected void writeTableSuffix(Device device, SComponent component) throws IOException {
         writeSuffix(device, component, true);
     }
 
     protected void writeDivPrefix(Device device, SComponent component) throws IOException {
         writePrefix(device, component, false);
     }
 
     protected void writeDivSuffix(Device device, SComponent component) throws IOException {
         writeSuffix(device, component, false);
     }
 
     private void writePrefix(Device device, SComponent component, boolean useTable) throws IOException {
         final boolean isTitleBorder = component.getBorder() instanceof STitledBorder;
         // This is the containing element of a component
         // it is responsible for styles, sizing...
         if (useTable) {
             device.print("<table"); // table
         } else {
             device.print("<div"); // div
         }
 
 
 // TODO FIXME we cant render this here.
         // render javascript event handlers
         //Utils.writeEvents(device, component, null);
 
         writeAllAttributes(device, component);
 
         if (useTable) {
             device.print("><tr><td>"); // table
         } else {
             device.print(">"); // div
         }
 
         // Special handling: Render title of STitledBorder
         if (isTitleBorder) {
             STitledBorder titledBorder = (STitledBorder) component.getBorder();
             device.print("<div class=\"STitledBorderLegend\">");
             device.print(titledBorder.getTitle());
             device.print("</div>");
         }
     }
 
     private void writeSuffix(Device device, SComponent component, boolean useTable) throws IOException {
         if (useTable) {
             device.print("</td></tr></table>");
         } else {
             device.print("</div>");
         }
     }
 
     protected final void writeAllAttributes(Device device, SComponent component) throws IOException {
         final boolean isTitleBorder = component.getBorder() instanceof STitledBorder;
 
         final String classname = component.getStyle();
         Utils.optAttribute(device, "class", isTitleBorder ? classname + " STitledBorder" : classname);
         Utils.optAttribute(device, "id", component.getName());
 
         Utils.optAttribute(device, "style", getInlineStyles(component));
 
         if (component instanceof LowLevelEventListener) {
             Utils.optAttribute(device, "eid", component.getEncodedLowLevelEventId());
         }
 
         // Tooltip handling
         writeTooltipMouseOver(device, component);
 
         // Component popup menu
         writeContextMenu(device, component);
     }
 
     protected String getInlineStyles(SComponent component) {
         // write inline styles
         final SStringBuilder builder = new SStringBuilder();
 
         if (component instanceof DragSource)
             builder.append("position:relative;");
 
         Utils.appendCSSInlineSize(builder, component.getPreferredSize());
 
         final Style allStyle = component.getDynamicStyle(SComponent.SELECTOR_ALL);
         if (allStyle != null)
             builder.append(allStyle.toString());
 
         final SBorder border = component.getBorder();
         if (border != null && border.getAttributes() != null)
             builder.append(border.getAttributes().toString());
 
         return builder.toString();
     }
 
     protected void writeInlineScripts(Device device, SComponent component) throws IOException {
         boolean scriptTagOpen = false;
         for (int i = 0; i < component.getScriptListeners().length; i++) {
             ScriptListener scriptListener = component.getScriptListeners()[i];
             String script = scriptListener.getScript();
             if (script != null) {
                 if (!scriptTagOpen) {
                     device.print("<script type=\"text/javascript\">");
                     scriptTagOpen = true;
                 }
                 device.print(script);
             }
         }
         if (scriptTagOpen)
             device.print("</script>");
     }
 
     /**
      * Write JS code for context menus. Common implementaton for MSIE and gecko.
      */
     protected static void writeContextMenu(Device device, SComponent component) throws IOException {
         final SPopupMenu menu = component.getComponentPopupMenu();
         if (menu != null) {
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
      * Write DomTT Tooltip code. Common handler for MSIE and Gecko PLAF.
      */
     protected static void writeTooltipMouseOver(Device device, SComponent component) throws IOException {
         final String toolTipText = component.getToolTipText();
         if (toolTipText != null) {
             device.print(" onmouseover=\"return makeTrue(domTT_activate(this, event, 'content', '");
             // javascript needs even more & special quoting
             // FIXME: do this more efficiently
            Utils.quote(device, toolTipText.replaceAll("'","\\'"), true, true, true);
             device.print("', 'predefined', 'default'));\"");
         }
     }
 
     /**
      * Install the appropriate CG for <code>component</code>.
      *
      * @param component the component
      */
     public void installCG(SComponent component) {
         Class clazz = component.getClass();
         while (clazz.getPackage() == null || !"org.wings".equals(clazz.getPackage().getName()))
             clazz = clazz.getSuperclass();
         String style = clazz.getName();
         style = style.substring(style.lastIndexOf('.') + 1);
         component.setStyle(style); // set default style name to component class (ie. SLabel).
     }
 
     /**
      * Uninstall the CG from <code>component</code>.
      *
      * @param component the component
      */
     public void uninstallCG(SComponent component) {
     }
 
     protected final SIcon getBlindIcon() {
         if (BLIND_ICON == null)
             BLIND_ICON = new SResourceIcon("org/wings/icons/blind.gif");
         return BLIND_ICON;
     }
 
     public void componentChanged(SComponent component) {
         InputMap inputMap = component.getInputMap();
         if (inputMap != null && inputMap.size() > 0) {
             if (!(inputMap instanceof VersionedInputMap)) {
                 inputMap = new VersionedInputMap(inputMap);
                 component.setInputMap(inputMap);
             }
 
             final VersionedInputMap versionedInputMap = (VersionedInputMap) inputMap;
             final Integer inputMapVersion = (Integer) component.getClientProperty("inputMapVersion");
             if (inputMapVersion == null || versionedInputMap.getVersion() != inputMapVersion.intValue()) {
                 InputMapScriptListener.install(component);
                 component.putClientProperty("inputMapVersion", new Integer(versionedInputMap.getVersion()));
             }
         }
 
         // Add script listener support.
         List scriptListenerList = component.getScriptListenerList();
         if (scriptListenerList != null && scriptListenerList.size() > 0) {
             if (!(scriptListenerList instanceof VersionedList)) {
                 scriptListenerList = new VersionedList(scriptListenerList);
                 component.setScriptListenerList(scriptListenerList);
             }
 
             final VersionedList versionedList = (VersionedList) scriptListenerList;
             final Integer scriptListenerListVersion = (Integer) component.getClientProperty("scriptListenerListVersion");
             if (scriptListenerListVersion == null || versionedList.getVersion() != scriptListenerListVersion.intValue())
             {
 
                 List removeCallables = new ArrayList();
                 // Remove all existing - and maybe unusable - DWR script listeners.
                 for (Iterator iter = CallableManager.getInstance().callableNames().iterator(); iter.hasNext();) {
                     Object o = iter.next();
                     if (o instanceof String) {
                         removeCallables.add(o);
                     }
                 }
 
                 for (Iterator iter = removeCallables.iterator(); iter.hasNext(); ) {
                     Object o = iter.next();
                     if (o instanceof String) {
                         CallableManager.getInstance().unregisterCallable((String) o);
                     }
                 }
 
                 // Add DWR script listener support.
                 ScriptListener[] scriptListeners = component.getScriptListeners();
                 for (int i = 0; i < scriptListeners.length; i++) {
                     if (scriptListeners[i] instanceof DWRScriptListener) {
                         DWRScriptListener scriptListener = (DWRScriptListener) scriptListeners[i];
                         CallableManager.getInstance().registerCallable(scriptListener.getCallableName(), scriptListener.getCallable());
 
                         // TODO: maybe unnecessary. check for former or later usage.
                         //component.putClientProperty("callable", scriptListener.getCallableName());
                     }
                 }
 
                 component.putClientProperty("scriptListenerListVersion", new Integer(versionedList.getVersion()));
             }
         }
     }
 
     protected final  boolean hasDimension(SComponent component) {
         SDimension dim = component.getPreferredSize();
         if (dim == null) return false;
         return (dim.getHeightInt() != SDimension.AUTO_INT || dim.getWidthInt() != SDimension.AUTO_INT);
     }
 
     public void write(Device device, SComponent component) throws IOException {        
         Utils.printDebug(device, "<!-- ").print(component.getName()).print(" -->");
         component.fireRenderEvent(SComponent.START_RENDERING);
 
         writeInternal(device, component);
 
         writeInlineScripts(device, component);
         component.fireRenderEvent(SComponent.DONE_RENDERING);
         Utils.printDebug(device, "<!-- /").print(component.getName()).print(" -->");
     }
 
     public abstract void writeInternal(Device device, SComponent component) throws IOException;
 
 }
