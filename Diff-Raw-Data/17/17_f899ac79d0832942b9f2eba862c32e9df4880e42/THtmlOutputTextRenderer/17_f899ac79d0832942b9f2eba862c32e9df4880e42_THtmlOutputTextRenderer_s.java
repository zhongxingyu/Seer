 /*
  * Copyright 2004-2007 the Seasar Foundation and the Others.
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
 package org.seasar.teeda.extension.render.html;
 
 import java.io.IOException;
 
 import javax.faces.component.html.HtmlOutputText;
 import javax.faces.context.FacesContext;
 import javax.faces.context.ResponseWriter;
 import javax.faces.internal.LabelUtil;
 
 import org.seasar.framework.util.StringUtil;
 import org.seasar.teeda.core.JsfConstants;
 import org.seasar.teeda.core.render.html.HtmlOutputTextRenderer;
 import org.seasar.teeda.core.util.RendererUtil;
 import org.seasar.teeda.core.util.ValueHolderUtil;
 import org.seasar.teeda.extension.ExtensionConstants;
 import org.seasar.teeda.extension.component.html.THtmlOutputText;
 
 /**
  * @author shot
  * @author yone
  */
 public class THtmlOutputTextRenderer extends HtmlOutputTextRenderer {
 
     public static final String COMPONENT_FAMILY = THtmlOutputText.COMPONENT_FAMILY;
 
     public static final String RENDERER_TYPE = THtmlOutputText.DEFAULT_RENDERER_TYPE;
 
     public THtmlOutputTextRenderer() {
         addIgnoreAttributeName(ExtensionConstants.KEY_ATTR);
         addIgnoreAttributeName(ExtensionConstants.PROPERTIES_NAME_ATTR);
         //addIgnoreAttributeName(ExtensionConstants.PROPERTIES_NAME_ATTR);
         addIgnoreAttributeName(ExtensionConstants.DEFAULT_KEY);
         addIgnoreAttributeName(ExtensionConstants.DEFAULT_PROPERTIES_NAME_ATTR);
     }
 
     protected void encodeHtmlOutputTextEnd(FacesContext context,
             HtmlOutputText htmlOutputText) throws IOException {
         final THtmlOutputText text = (THtmlOutputText) htmlOutputText;
         final ResponseWriter writer = context.getResponseWriter();
         final String id = getIdForRender(context, htmlOutputText);
        final boolean isLabel = (id != null && id.endsWith("Label"));
         boolean startSpan = false;
         final boolean invisible = text.isInvisible();
         if (containsAttributeForRender(htmlOutputText, getIgnoreAttribute())) {
            if (!invisible) { 
                 writer.startElement(JsfConstants.SPAN_ELEM, htmlOutputText);
                startSpan = true;    
                RendererUtil.renderIdAttributeIfNecessary(writer, htmlOutputText,
                        id);
                renderRemainAttributes(htmlOutputText, writer, getIgnoreAttribute());
             }
         }
         String value = null;
         if (isLabel) {
             final String key = text.getKey();
             final String propertiesName = text.getPropertiesName();
             final String defaultKey = text.getDefaultKey();
             final String defaultPropertiesName = text
                     .getDefaultPropertiesName();
             value = LabelUtil.getLabelValue(key, propertiesName, defaultKey,
                     defaultPropertiesName);
         }
         if (StringUtil.isEmpty(value)) {
             value = ValueHolderUtil.getValueForRender(context, htmlOutputText);
         }
         if (htmlOutputText.isEscape()) {
             writer.writeText(value, null);
         } else {
             writer.write(value);
         }
         if (startSpan) {
             writer.endElement(JsfConstants.SPAN_ELEM);
         }
     }
 
 }
