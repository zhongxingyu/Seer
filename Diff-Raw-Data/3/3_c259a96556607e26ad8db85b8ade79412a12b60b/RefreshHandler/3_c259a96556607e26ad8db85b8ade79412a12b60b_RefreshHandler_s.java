 /*
  * Copyright 2004-2013 ICEsoft Technologies Canada Corp.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the
  * License. You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing,
  * software distributed under the License is distributed on an "AS
  * IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
  * express or implied. See the License for the specific language
  * governing permissions and limitations under the License.
  */
 
 package org.icefaces.impl.facelets.tag.icefaces.core;
 
 import org.icefaces.impl.event.BridgeSetup;
 import org.icefaces.util.EnvUtils;
 
 import javax.faces.component.UIComponent;
 import javax.faces.component.UIOutput;
 import javax.faces.component.UIViewRoot;
 import javax.faces.context.FacesContext;
 import javax.faces.context.ResponseWriter;
 import javax.faces.view.facelets.FaceletContext;
 import javax.faces.view.facelets.TagAttribute;
 import javax.faces.view.facelets.TagConfig;
 import javax.faces.view.facelets.TagHandler;
 import java.io.IOException;
 
 public class RefreshHandler extends TagHandler {
 
     public RefreshHandler(TagConfig config) {
         super(config);
     }
 
     public void apply(FaceletContext ctx, UIComponent parent) throws IOException {
         boolean disabled = getDisabled();
         if (!disabled) {
             FacesContext context = FacesContext.getCurrentInstance();
             UIOutput refreshSetup = new RefreshSetupOutput(
                 getInterval(), getDuration(), disabled);
             UIViewRoot viewRoot = context.getViewRoot();
             refreshSetup.setTransient(true);
             refreshSetup.setId(viewRoot.createUniqueId(context, "_setupRefresh"));
             viewRoot.addComponentResource(context, refreshSetup, "body");
         }
     }
 
     public long getInterval()  {
         TagAttribute intervalAttribute = getAttribute("interval");
         long interval = intervalAttribute == null ? 10000 :
             (Long.valueOf(intervalAttribute.getValue()) * 1000);
             //seconds
         return interval;
     }
 
     public long getDuration()  {
         TagAttribute durationAttribute = getAttribute("duration");
         long duration = durationAttribute == null ? -1 :
             (Long.valueOf(durationAttribute.getValue()) * 60 * 1000);
             //minutes
         return duration;
     }
 
     public boolean getDisabled()  {
         TagAttribute disabledAttribute = getAttribute("disabled");
         boolean disabled = disabledAttribute == null ? false :
             Boolean.parseBoolean(disabledAttribute.getValue());
         disabled = getDuration() == 0 ? true : disabled;
         return disabled;
     }
 
 
     private class RefreshSetupOutput extends UIOutput {
         //this component is transient so can use member fields
         //rather than state saving
         private long interval;
         private long duration;
         private boolean disabled;
 
         public RefreshSetupOutput(long interval, long duration,
                 boolean disabled)  {
             this.interval = interval;
             this.duration = duration;
             this.disabled = disabled;
         }
 
         public void encodeBegin(FacesContext context) throws IOException {
             ResponseWriter writer = context.getResponseWriter();
             writer.startElement("span", this);
             writer.writeAttribute("id", getClientId(context), null);
             writer.startElement("script", this);
             writer.writeAttribute("type", "text/javascript", null);
             writer.writeText("ice.setupRefresh('", null);
             writer.writeText(BridgeSetup.getViewID(context.getExternalContext()), null);
             writer.writeText("', ", null);
             writer.writeText(interval, null);
             writer.writeText(", ", null);
             writer.writeText(duration, null);
             writer.writeText(", '", null);
             writer.writeText(getClientId(context), null);
             writer.writeText("');", null);
            writer.endElement("span");
             writer.endElement("script");
         }
     }
 }
