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
 
 import org.wings.SClickable;
 import org.wings.SComponent;
 import org.wings.SConstants;
 import org.wings.SFrame;
 import org.wings.event.SParentFrameEvent;
 import org.wings.event.SParentFrameListener;
 import org.wings.externalizer.ExternalizeManager;
 import org.wings.header.Script;
 import org.wings.io.Device;
 import org.wings.plaf.css.MSIEButtonFix;
 import org.wings.plaf.css.Utils;
 import org.wings.resource.ClasspathResource;
 import org.wings.resource.DefaultURLResource;
 import org.wings.session.SessionManager;
 
 /**
  * @author ole
  *
  */
 public class ClickableCG extends org.wings.plaf.css.ClickableCG implements SParentFrameListener, MSIEButtonFix {
     private static final String FORMS_JS = (String) SessionManager
     .getSession().getCGManager().getObject("JScripts.form",
             String.class);
 
 
     protected void writeButtonStart(Device device, SClickable button) throws IOException {
        device.print("<button  onclick=\"addHiddenField(this.form,'");
         device.print(button.getParentFrame().getEventEpoch());
         device.print(SConstants.UID_DIVIDER);
         device.print(SConstants.IEFIX_BUTTONACTION);
         device.print("','");
        device.print(button.getEventTarget().getEncodedLowLevelEventId());
         device.print(SConstants.UID_DIVIDER);
         Utils.write(device, button.getEvent());
         device.print("')\"");
     }
 
     public void installCG(SComponent component) {
         super.installCG(component);
         component.addParentFrameListener(this);
     }
 
     public void parentFrameAdded(SParentFrameEvent e) {
         SFrame parentFrame = e.getParentFrame();
         ClasspathResource res = new ClasspathResource(FORMS_JS, "text/javascript");
         String jScriptUrl = SessionManager.getSession().getExternalizeManager().externalize(res, ExternalizeManager.GLOBAL);
         parentFrame.addHeader(new Script("text/javascript", new DefaultURLResource(jScriptUrl)));
     }
 
     public void parentFrameRemoved(SParentFrameEvent e) {
     }
 
 }
