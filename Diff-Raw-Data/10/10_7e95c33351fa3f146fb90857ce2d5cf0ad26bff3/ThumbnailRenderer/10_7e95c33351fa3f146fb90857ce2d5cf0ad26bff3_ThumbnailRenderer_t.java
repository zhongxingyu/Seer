 /*
  * Copyright 2004-2012 ICEsoft Technologies Canada Corp.
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
 
 package org.icefaces.mobi.component.thumbnail;
 
 
 import java.io.IOException;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import javax.faces.application.ProjectStage;
 import javax.faces.component.UIComponent;
 import javax.faces.context.FacesContext;
 import javax.faces.context.ResponseWriter;
 import javax.faces.render.Renderer;
 
 import org.icefaces.mobi.utils.HTML;
 import org.icefaces.mobi.utils.JSFUtils;
 import org.icefaces.mobi.utils.MobiJSFUtils;
 import org.icemobile.util.ClientDescriptor;
 
 public class ThumbnailRenderer extends Renderer {
     private static final Logger logger =
             Logger.getLogger(ThumbnailRenderer.class.toString());
 
 
     public void encodeEnd(FacesContext facesContext, UIComponent uiComponent)
             throws IOException {
         ClientDescriptor cd = MobiJSFUtils.getClientDescriptor();
         if (cd.isDesktopBrowser()){
             return;
         }
         ResponseWriter writer = facesContext.getResponseWriter();
 
         Thumbnail thumbnail = (Thumbnail) uiComponent;
         String cameraId = thumbnail.getFor();
         String cameraClientId = null;
 
         if (cameraId == null &&
                 (facesContext.isProjectStage(ProjectStage.Development) ||
                 logger.isLoggable(Level.FINER))) {
             logger.warning("'for' attribute cannot be null");
         }
         UIComponent cameraComp = thumbnail.findComponent(cameraId);
         String thumbId = cameraId + "-thumb";
         if (null != cameraComp) {
             cameraClientId = cameraComp.getClientId();
             thumbId = cameraClientId + "-thumb";
         } else if (facesContext.isProjectStage(ProjectStage.Development) ||
                 logger.isLoggable(Level.FINER)){
             logger.finer(" Cannot find camera component with id=" + cameraId);
         }
 
         // boolean disabled = thumbnail.isDisabled();
         writer.startElement("span", uiComponent);
         // write out style for input button, same as default device button.
         String thumbClass = "mobi-thumb";
         if (MobiJSFUtils.uploadInProgress(cameraClientId))  {
             thumbClass = "mobi-thumb-done";
         } 
         JSFUtils.writeConcatenatedStyleClasses(writer, thumbClass,
                 thumbnail.getStyleClass());
         writer.writeAttribute(HTML.STYLE_ATTR, thumbnail.getStyle(), HTML.STYLE_ATTR);
         writer.writeAttribute(HTML.ID_ATTR, "span-thumb", null);
         writer.startElement("img", uiComponent);
         writer.writeAttribute(HTML.ID_ATTR, thumbId, null);
         writer.writeAttribute(HTML.WIDTH_ATTR, "64", null);
         writer.writeAttribute(HTML.HEIGHT_ATTR, "64", null);
         writer.endElement("img");
        writer.endElement("span");
     }
 
 }
