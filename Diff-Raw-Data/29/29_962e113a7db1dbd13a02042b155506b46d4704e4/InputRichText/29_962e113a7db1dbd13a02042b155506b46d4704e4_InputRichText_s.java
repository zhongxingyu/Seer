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
 
 package com.icesoft.faces.component.inputrichtext;
 
 import com.icesoft.faces.component.CSS_DEFAULT;
 import com.icesoft.faces.component.ext.taglib.Util;
 import com.icesoft.faces.util.CoreUtils;
 
 import javax.faces.application.ResourceDependencies;
 import javax.faces.application.ResourceDependency;
 import javax.faces.component.UIInput;
 import javax.faces.context.FacesContext;
 import javax.faces.el.ValueBinding;
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.Map;
 
 @ResourceDependencies({
         @ResourceDependency(name = "inputrichtext/ckeditor/ckeditor.mapping.js"),
         @ResourceDependency(name = "inputrichtext/ckeditor/ckeditor.js"),
         @ResourceDependency(name = "inputrichtext/ckeditor_ext.js")
 })
 public class InputRichText extends UIInput {
     public static final String COMPONENT_TYPE = "com.icesoft.faces.InputRichText";
     public static final String DEFAULT_RENDERER_TYPE = "com.icesoft.faces.InputRichTextRenderer";
 
 
     private Boolean partialSubmit = null;
 
 
     private String language;
     private String _for;
     private String style;
     private String styleClass;
     private String width;
     private String height;
 
 
     private String toolbar;
     private String customConfigPath;
     private Boolean disabled = null;
     private String skin = null;
     private Boolean saveOnSubmit = null;
 
 
     public String getRendererType() {
         return DEFAULT_RENDERER_TYPE;
     }
 
     public String getComponentType() {
         return COMPONENT_TYPE;
     }
 
     public void decode(FacesContext facesContext) {
         Map map = facesContext.getExternalContext().getRequestParameterMap();
         String clientId = getClientId(facesContext);
         if (map.containsKey(clientId)) {
             String newValue = map.get(clientId).toString().replace('\n', ' ');
             setSubmittedValue(newValue);
         }
         super.decode(facesContext);
     }
 
     public void encodeBegin(FacesContext context) throws IOException {
         super.encodeBegin(context);
     }
 
     /**
      * <p>Set the value of the <code>language</code> property.</p>
      */
     public void setLanguage(String language) {
         this.language = language;
     }
 
     /**
      * <p>Return the value of the <code>language</code> property.</p>
      */
     public String getLanguage() {
         if (language != null) {
             return language;
         }
         ValueBinding vb = getValueBinding("language");
         return vb != null ? (String) vb.getValue(getFacesContext()) : "en";
     }
 
     /**
      * <p>Set the value of the <code>for</code> property.</p>
      */
     public void setFor(String _for) {
         this._for = _for;
     }
 
     /**
      * <p>Return the value of the <code>language</code> property.</p>
      */
     public String getFor() {
         if (_for != null) {
             return _for;
         }
         ValueBinding vb = getValueBinding("for");
         return vb != null ? (String) vb.getValue(getFacesContext()) : "";
     }
 
     boolean isToolbarOnly() {
         return false;
     }
 
     /**
      * <p>Set the value of the <code>style</code> property.</p>
      */
     public void setStyle(String style) {
         this.style = style;
     }
 
     /**
      * <p>Return the value of the <code>style</code> property.</p>
      */
     public String getStyle() {
         if (style != null) {
             return style;
         }
         ValueBinding vb = getValueBinding("style");
         return vb != null ? (String) vb.getValue(getFacesContext()) : null;
     }
 
 
     /**
      * <p>Set the value of the <code>styleClass</code> property.</p>
      */
     public void setStyleClass(String styleClass) {
         this.styleClass = styleClass;
     }
 
     /**
      * <p>Return the value of the <code>styleClass</code> property.</p>
      */
     public String getStyleClass() {
         return Util.getQualifiedStyleClass(this,
                 styleClass,
                 CSS_DEFAULT.INPUT_RICH_TEXT,
                 "styleClass");
 
     }
 
     /**
      * <p>Set the value of the <code>width</code> property.</p>
      */
     public void setWidth(String width) {
         this.width = width;
     }
 
     /**
      * <p>Return the value of the <code>width</code> property.</p>
      */
     public String getWidth() {
         if (width != null) {
             return width;
         }
         ValueBinding vb = getValueBinding("width");
         return vb != null ? (String) vb.getValue(getFacesContext()) : "100%";
     }
 
     /**
      * <p>Set the value of the <code>height</code> property.</p>
      */
     public void setHeight(String height) {
         this.height = height;
     }
 
     /**
      * <p>Return the value of the <code>height</code> property.</p>
      */
     public String getHeight() {
         if (height != null) {
             return height;
         }
         ValueBinding vb = getValueBinding("height");
         return vb != null ? (String) vb.getValue(getFacesContext()) : "100";
     }
 
     private static byte[] toByteArray(InputStream input) throws IOException {
         ByteArrayOutputStream output = new ByteArrayOutputStream();
         byte[] buf = new byte[4096];
         int len = 0;
         while ((len = input.read(buf)) > -1) output.write(buf, 0, len);
         return output.toByteArray();
     }
 
     /**
      * <p>Set the value of the <code>toolbar</code> property.</p>
      */
     public void setToolbar(String toolbar) {
         this.toolbar = toolbar;
     }
 
     /**
      * <p>Return the value of the <code>toolbar</code> property.</p>
      */
     public String getToolbar() {
         if (toolbar != null) {
             return toolbar;
         }
         ValueBinding vb = getValueBinding("toolbar");
         return vb != null ? (String) vb.getValue(getFacesContext()) : "Default";
     }
 
     /**
      * <p>Set the value of the <code>customConfigPath</code> property.</p>
      */
     public void setCustomConfigPath(String customConfigPath) {
         this.customConfigPath = customConfigPath;
     }
 
     /**
      * <p>Return the value of the <code>customConfigPath</code> property.</p>
      */
     public String getCustomConfigPath() {
         if (customConfigPath != null) {
             return CoreUtils.resolveResourceURL(getFacesContext(), "/"+ customConfigPath);
         }
         ValueBinding vb = getValueBinding("customConfigPath");
         return vb != null ? CoreUtils.resolveResourceURL(getFacesContext(), "/"+ (String) vb.getValue(getFacesContext())) : null;
     }
 
     /**
      * <p>Set the value of the <code>disabled</code> property.</p>
      */
     public void setDisabled(boolean disabled) {
         this.disabled = new Boolean(disabled);
     }
 
     /**
      * <p>Return the value of the <code>disabled</code> property.</p>
      */
     public boolean isDisabled() {
         if (disabled != null) {
             return disabled.booleanValue();
         }
         ValueBinding vb = getValueBinding("disabled");
         return vb != null ? ((Boolean) vb.getValue(getFacesContext()))
                 .booleanValue() : false;
     }
 
     /**
      * <p>Set the value of the <code>skin</code> property.</p>
      */
     public void setSkin(String skin) {
         this.skin = skin;
     }
 
     /**
      * <p>Return the value of the <code>skin</code> property.</p>
      */
     public String getSkin() {
         if (skin != null) {
             return skin;
         }
         ValueBinding vb = getValueBinding("skin");
         return vb != null ? (String) vb.getValue(getFacesContext()) : "default";
     }
 
     /**
      * <p>Set the value of the <code>saveOnSubmit</code> property.</p>
      */
     public void setSaveOnSubmit(boolean saveOnSubmit) {
         this.saveOnSubmit = new Boolean(saveOnSubmit);
     }
 
     /**
      * <p>Return the value of the <code>saveOnSubmit</code> property.</p>
      */
     public boolean isSaveOnSubmit() {
         if (saveOnSubmit != null) {
             return saveOnSubmit.booleanValue();
         }
         ValueBinding vb = getValueBinding("saveOnSubmit");
         return vb != null ? ((Boolean) vb.getValue(getFacesContext()))
                 .booleanValue() : false;
     }
 
     public void setPartialSubmit(boolean partialSubmit) {
         this.partialSubmit = Boolean.valueOf(partialSubmit);
     }
 
     public boolean getPartialSubmit() {
         if (partialSubmit != null) {
             return partialSubmit.booleanValue();
         }
         ValueBinding vb = getValueBinding("partialSubmit");
         Boolean boolVal =
                 vb != null ? (Boolean) vb.getValue(getFacesContext()) : null;
         return boolVal != null ? boolVal.booleanValue() :
                 Util.isParentPartialSubmit(this);
     }
 
     public Object saveState(FacesContext context) {
         Object values[] = new Object[13];
         values[0] = super.saveState(context);
         values[1] = customConfigPath;
         values[2] = disabled;
         values[3] = _for;
         values[4] = styleClass;
         values[5] = height;
         values[6] = language;
         values[7] = saveOnSubmit;
         values[8] = skin;
         values[9] = style;
         values[10] = toolbar;
         values[11] = width;
         values[12] = partialSubmit;
         return values;
     }
 
     public void restoreState(FacesContext context, Object state) {
         Object values[] = (Object[]) state;
         super.restoreState(context, values[0]);
         customConfigPath = (String) values[1];
         disabled = (Boolean) values[2];
         _for = (String) values[3];
         styleClass = (String) values[4];
         height = (String) values[5];
         language = (String) values[6];
         saveOnSubmit = (Boolean) values[7];
         skin = (String) values[8];
         style = (String) values[9];
         toolbar = (String) values[10];
         width = (String) values[11];
         partialSubmit = (Boolean) values[12];
     }
 }
 
 
