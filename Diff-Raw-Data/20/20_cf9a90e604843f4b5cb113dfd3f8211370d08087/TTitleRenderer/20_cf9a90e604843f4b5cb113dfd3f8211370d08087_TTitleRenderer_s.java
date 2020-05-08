 /*
  * Copyright 2004-2009 the Seasar Foundation and the Others.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
  * either express or implied. See the License for the specific language
  * governing permissions and limitations under the License.
  */
 package org.seasar.teeda.extension.render;
 
 import java.io.IOException;
 
 import javax.faces.component.UIComponent;
 import javax.faces.context.FacesContext;
 import javax.faces.context.ResponseWriter;
 import javax.faces.internal.LabelUtil;
 
 import org.seasar.framework.util.StringUtil;
 import org.seasar.teeda.core.JsfConstants;
 import org.seasar.teeda.core.render.AbstractRenderer;
 import org.seasar.teeda.core.util.RendererUtil;
 import org.seasar.teeda.core.util.ValueHolderUtil;
 import org.seasar.teeda.extension.ExtensionConstants;
 import org.seasar.teeda.extension.component.UITitle;
 
 /**
  * @author shot
  */
 public class TTitleRenderer extends AbstractRenderer {
 
     public static final String COMPONENT_FAMILY = "org.seasar.teeda.extension.Title";
 
     public static final String RENDERER_TYPE = "org.seasar.teeda.extension.Title";
 
     public void encodeBegin(FacesContext context, UIComponent component)
             throws IOException {
         assertNotNull(context, component);
         if (!component.isRendered()) {
             return;
         }
         UITitle title = (UITitle) component;
         encodeTitleBegin(context, title);
     }
 
     protected void encodeTitleBegin(FacesContext context, UITitle title)
             throws IOException {
         ResponseWriter writer = context.getResponseWriter();
         writer.startElement(ExtensionConstants.TITLE_ELEM, title);
         RendererUtil.renderIdAttributeIfNecessary(writer, title,
                 getIdForRender(context, title));
         String dir = title.getDir();
         if (!StringUtil.isEmpty(dir)) {
             writer.writeAttribute(JsfConstants.DIR_ATTR, dir, null);
         }
         String lang = title.getLang();
         if (!StringUtil.isEmpty(lang)) {
             writer.writeAttribute(JsfConstants.LANG_ATTR, lang, null);
         }
         String value = ValueHolderUtil.getValueForRender(context, title);
         if (StringUtil.isEmpty(value)) {
             final String key = title.getKey();
             final String propertiesName = title.getPropertiesName();
             final String defaultKey = title.getDefaultKey();
             final String defaultPropertiesName = title
                     .getDefaultPropertiesName();
             value = LabelUtil.getLabelValue(key, propertiesName, defaultKey,
                     defaultPropertiesName);
         }
        if (StringUtil.isEmpty(value)) {
            value = title.getTemplateValue();
         }
         if (value != null) {
            writer.writeText(value, null);
         }
     }
 
     public void encodeEnd(FacesContext context, UIComponent component)
             throws IOException {
         assertNotNull(context, component);
         if (!component.isRendered()) {
             return;
         }
         encodeTitleEnd(context, (UITitle) component);
     }
 
     protected void encodeTitleEnd(FacesContext context, UITitle title)
             throws IOException {
         ResponseWriter writer = context.getResponseWriter();
         writer.endElement(ExtensionConstants.TITLE_ELEM);
     }
 
 }
