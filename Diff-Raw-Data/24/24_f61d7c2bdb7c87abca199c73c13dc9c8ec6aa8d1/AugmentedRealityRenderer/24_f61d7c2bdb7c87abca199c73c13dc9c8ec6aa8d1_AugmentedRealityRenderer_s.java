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
 package org.icefaces.mobi.component.augmentedreality;
 
 import org.icefaces.mobi.renderkit.BaseInputRenderer;
 import org.icefaces.impl.application.AuxUploadResourceHandler;
 import org.icefaces.mobi.utils.HTML;
 import org.icefaces.mobi.utils.Utils;
 import org.icefaces.mobi.utils.MobiJSFUtils;
 import org.icefaces.util.EnvUtils;
 import org.icemobile.util.CSSUtils;
 
 import javax.faces.application.ProjectStage;
 import javax.faces.component.UIComponent;
 import javax.faces.context.FacesContext;
 import javax.faces.context.ResponseWriter;
 import java.io.IOException;
 import java.util.Map;
 import java.net.URLEncoder;
 import java.util.Collection;
 import java.util.logging.Level;
 
 
 public class AugmentedRealityRenderer extends BaseInputRenderer  {
     private static final String LOC_LABEL = "locationLabel";
     private static final String LOC_LAT = "locationLat";
     private static final String LOC_LON = "locationLon";
     private static final String LOC_ALT = "locationAlt";
     private static final String LOC_DIR = "locationDir";
     private static final String LOC_ICON = "locationIcon";
     private static final String MARK_MODEL = "markerModel";
     private static final String MARK_LABEL = "markerLabel";
 
      public void encodeEnd(FacesContext facesContext, UIComponent uiComponent)
              throws IOException {
          ResponseWriter writer = facesContext.getResponseWriter();
          String clientId = uiComponent.getClientId(facesContext);
          AugmentedReality ag = (AugmentedReality)uiComponent;
 
         boolean isEnhanced = EnvUtils.isEnhancedBrowser(facesContext);
          boolean isAuxUpload = EnvUtils.isAuxUploadBrowser(facesContext);
          if (!isEnhanced && !isAuxUpload) {  //no container or SX, use text field
              writer.startElement(HTML.INPUT_ELEM, uiComponent);
              writer.writeAttribute(HTML.ID_ATTR, clientId, null);
              writer.writeAttribute(HTML.NAME_ATTR, clientId, null);
              writer.endElement(HTML.INPUT_ELEM);
              return;
          }
          writer.startElement(HTML.BUTTON_ELEM, uiComponent);
          writer.writeAttribute(HTML.ID_ATTR, clientId, null);
          String buttonValue=ag.getButtonLabel();
          if (null!=ag.getStyle()){
              String style= ag.getStyle();
              if ( style.trim().length() > 0) {
                  writer.writeAttribute(HTML.STYLE_ATTR, style, HTML.STYLE_ATTR);
              }
          }
          StringBuilder defaultClass = new StringBuilder(CSSUtils.STYLECLASS_BUTTON);
          if (null!=ag.getStyleClass()) {
              String styleClass = ag.getStyleClass();
              defaultClass.append(" ").append(styleClass);
          }
          writer.writeAttribute(HTML.CLASS_ATTR, defaultClass, HTML.CLASS_ATTR);
 
         String arParams = "";
         String urlBase = ag.getUrlBase();
         if (null != urlBase)  {
             arParams = "ub=" + URLEncoder.encode(urlBase) + "&";
         }
         for (UIComponent child : ag.getChildren())  {
             if (child instanceof AugmentedRealityLocations) {
                 AugmentedRealityLocations locations = 
                         (AugmentedRealityLocations) child;
                 arParams += iterateLocations(facesContext, locations);
             }
             if (child instanceof AugmentedRealityMarkers) {
                 AugmentedRealityMarkers markers =
                         (AugmentedRealityMarkers) child;
                 arParams += iterateMarkers(facesContext, markers);
                 arParams += "v=vuforia" + "&";
             }
         }
 
         //allow legacy params value for now
         if ("".equals(arParams))  {
             arParams = ag.getParams();
         }
         String script;
         if (isAuxUpload)  {
             writer.writeAttribute("data-params", arParams, null);
             writer.writeAttribute("data-command", "aug", null);
             String sessionId = MobiJSFUtils.getSessionIdCookie();
             writer.writeAttribute("data-jsessionid", sessionId, null);
             script = "ice.mobi.sx(this);";
         } else {
             script = "ice.aug( '" + clientId + "', '" + 
                     arParams + "' );return false;";
         }
         writer.writeAttribute(HTML.ONCLICK_ATTR, script, null);
          writer.writeText(buttonValue, null);
         writer.endElement(HTML.BUTTON_ELEM);
      }
 
     @Override
     public void decode(FacesContext facesContext, UIComponent component) {
         Map parameterMap = facesContext.getExternalContext()
                 .getRequestParameterMap();
         AugmentedReality aug = (AugmentedReality) component;
         String clientId = component.getClientId();
         Object submitted = parameterMap.get(clientId);
         if (null == submitted)  {
             Map auxMap = AuxUploadResourceHandler.getAuxRequestMap();
             submitted = auxMap.get(clientId);
         }
         if (null != submitted) {
             String submittedString = String.valueOf(submitted);
             if (submittedString != null){
                 Object convertedValue = this.getConvertedValue(facesContext, component, submittedString);
                 this.setSubmittedValue(aug, convertedValue);
             }
 
         }
     }
 
 
     String iterateLocations(FacesContext facesContext, 
             AugmentedRealityLocations locations)  {
         String var = locations.getVar();
         if (null == var) {
             //cannot iterate without a var
             return null;
         }
         StringBuilder result = new StringBuilder();
         
         Map<String, Object> requestMap =
               facesContext.getExternalContext().getRequestMap();
         Collection items = (Collection) locations.getValue();
         //an optimization will be to find the longest common prefix of
         //the itemIcon values and set urlBase to that
         for (Object item : items)  {
             Object oldVar = requestMap.put(var, item);
             Map<String,Object> attrs = locations.getAttributes();
             String itemLabel = (String) attrs.get(LOC_LABEL);
             result.append(URLEncoder.encode(itemLabel)).append("=");
             result.append(attrs.get(LOC_LAT)).append(",");
             result.append(attrs.get(LOC_LON)).append(",");
 
             Object itemAlt = attrs.get(LOC_ALT);
             if (null != itemAlt)  {
                 result.append(itemAlt);
             }
             result.append(",");
             Object itemDir = attrs.get(LOC_DIR);
             if (null != itemDir)  {
                 result.append(itemDir);
             }
             result.append(",");
             String itemIcon = (String) attrs.get(LOC_ICON);
             if (null != itemIcon)  {
                 result.append(URLEncoder.encode(itemIcon));
             }
             result.append("&");
             requestMap.put(var, oldVar);
         }
         return result.toString();
     }
 
     String iterateMarkers(FacesContext facesContext,
             AugmentedRealityMarkers markers)  {
         String var = markers.getVar();
         if (null == var) {
             //cannot iterate without a var
             return null;
         }
         StringBuilder result = new StringBuilder();
         
         Map<String, Object> requestMap =
               facesContext.getExternalContext().getRequestMap();
         Collection items = (Collection) markers.getValue();
         int index = 0;
         for (Object item : items)  {
             Object oldVar = requestMap.put(var, item);
             Map<String,Object> attrs = markers.getAttributes();
             String markerLabel = (String) attrs.get(MARK_LABEL);
             String itemID = "_" + index;
             result.append(markerLabel + itemID).append("=");
 
             String markerModel = (String) attrs.get(MARK_MODEL);
             if (null != markerModel)  {
                 result.append(URLEncoder.encode(markerModel));
             }
             result.append("&");
             requestMap.put(var, oldVar);
             index++;
         }
         return result.toString();
    }
 
 }
