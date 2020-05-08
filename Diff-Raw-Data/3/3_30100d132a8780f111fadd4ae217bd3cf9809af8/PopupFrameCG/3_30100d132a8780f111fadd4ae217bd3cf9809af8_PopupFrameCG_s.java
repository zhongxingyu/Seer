 /*
  * Copyright 2000,2006 wingS development team.
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
 
 package org.wingx.plaf.css;
 
 import java.io.IOException;
 import java.util.ArrayList;
import java.util.HashSet;
 import java.util.List;
 import org.wings.SComponent;
 import org.wings.event.SParentFrameEvent;
 import org.wings.event.SParentFrameListener;
 import org.wings.header.SessionHeaders;
 import org.wings.io.Device;
 import org.wings.plaf.css.AbstractComponentCG;
 import org.wings.plaf.css.Utils;
 import org.wings.plaf.css.dwr.CallableManager;
 import org.wings.plaf.css.script.OnHeadersLoadedScript;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.wings.session.ScriptManager;
 import org.wings.util.SStringBuilder;
 import org.wingx.XPopupFrame;
 
 public class PopupFrameCG extends AbstractComponentCG implements org.wingx.plaf.PopupFrameCG, SParentFrameListener {
 
     private final transient static Log log = LogFactory.getLog(PopupFrameCG.class);
 
     protected final List headers = new ArrayList();
 
     public PopupFrameCG() {
        headers.add(Utils.createExternalizedCSSHeader("org/wings/js/yui/container/assets/container.css")); // TODO
         headers.add(Utils.createExternalizedJSHeader("org/wingx/popupframe/popupframe.js"));
     }
 
     public void writeInternal(Device device, SComponent component) throws IOException {
 
         device.print("<div id='").print(component.getName()).print("' />");
 
         XPopupFrame popup = (XPopupFrame) component;
 
         SStringBuilder code = new SStringBuilder("function() {");
         code
                 .append("popupFrame = new wingS.PopupFrame('").append(popup.getName()).append("',")
                 .append(popup.getWidth()).append(",")
                 .append(popup.getHeight())
                 .append(");")
                 .append("}");
 
         ScriptManager.getInstance().addScriptListener(new OnHeadersLoadedScript(code.toString(), false));
     }
 
     public void installCG(final SComponent comp) {
         super.installCG(comp);
         comp.addParentFrameListener(this);
     }
 
     public void parentFrameAdded(SParentFrameEvent e) {
         SessionHeaders.getInstance().registerHeaders(headers);
 
         // expose data source to java script by using DWR        
         CallableManager.getInstance().registerCallable("test", e.getComponent(), SPopupFrameInterface.class);
     }
 
     public void parentFrameRemoved(SParentFrameEvent e) {
         SessionHeaders.getInstance().deregisterHeaders(headers);
         CallableManager.getInstance().unregisterCallable("test");
     }
 
     interface SPopupFrameInterface {
         public String getFrameUrl();
     }
     
 }
