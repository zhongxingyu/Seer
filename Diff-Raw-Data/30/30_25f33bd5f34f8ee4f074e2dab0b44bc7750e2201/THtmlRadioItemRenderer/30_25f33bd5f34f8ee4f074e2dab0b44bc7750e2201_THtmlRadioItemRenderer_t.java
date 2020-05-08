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
 package org.seasar.teeda.extension.render.html;
 
 import java.io.IOException;
 
 import javax.faces.component.UIComponent;
 import javax.faces.component.UIOutput;
 import javax.faces.component.html.HtmlSelectOneRadio;
 import javax.faces.context.FacesContext;
 import javax.faces.context.ResponseWriter;
 import javax.faces.convert.Converter;
 import javax.faces.convert.ConverterException;
 import javax.faces.internal.IgnoreAttribute;
 import javax.faces.internal.UIComponentUtil;
 
 import org.seasar.framework.util.StringUtil;
 import org.seasar.teeda.core.JsfConstants;
 import org.seasar.teeda.core.render.AbstractRenderer;
 import org.seasar.teeda.core.util.RendererUtil;
 import org.seasar.teeda.core.util.UIValueUtil;
 import org.seasar.teeda.core.util.ValueHolderUtil;
 import org.seasar.teeda.extension.component.html.THtmlRadioItem;
 
 /**
  * @author manhole
  */
 public class THtmlRadioItemRenderer extends AbstractRenderer {
 
     public static final String COMPONENT_FAMILY = THtmlRadioItem.COMPONENT_FAMILY;
 
     public static final String RENDERER_TYPE = THtmlRadioItem.DEFAULT_RENDERER_TYPE;
 
     private final IgnoreAttribute ignoreComponent = new IgnoreAttribute();
     {
         ignoreComponent.addAttributeName(JsfConstants.ID_ATTR);
         ignoreComponent.addAttributeName(JsfConstants.TYPE_ATTR);
         ignoreComponent.addAttributeName(JsfConstants.NAME_ATTR);
         ignoreComponent.addAttributeName(JsfConstants.VALUE_ATTR);
        ignoreComponent.addAttributeName(JsfConstants.DISABLED_ATTR);
         ignoreComponent.addAttributeName(JsfConstants.CHECKED_ATTR);
         ignoreComponent.addAttributeName(JsfConstants.ITEM_VALUE_ATTR);
     }
 
     public void encodeBegin(final FacesContext context,
             final UIComponent component) throws IOException {
         assertNotNull(context, component);
         if (!component.isRendered()) {
             return;
         }
         final THtmlRadioItem htmlInputRadio = (THtmlRadioItem) component;
         final String name = htmlInputRadio.getName();
         final HtmlSelectOneRadio parent = (HtmlSelectOneRadio) (name != null ? UIComponentUtil
                 .findParent(component, HtmlSelectOneRadio.class, name)
                 : component.getParent());
         final String valueStr = ValueHolderUtil.getValueForRender(context,
                 parent);
         final String clientId = parent.getClientId(context);
         final Converter converter = RendererUtil.findConverter(context, parent);
         final Object itemValue = htmlInputRadio.getValue();
         final String itemValueStr = UIValueUtil.getValueAsString(context,
                 component, itemValue, converter);
         final boolean checked = valueStr.equals(itemValueStr);
         final boolean disabled = parent.isDisabled() ||
                 htmlInputRadio.isDisabled();
         renderRadio(context, htmlInputRadio, itemValueStr, clientId, checked,
                 disabled);
     }
 
     protected void renderRadio(final FacesContext context,
             final THtmlRadioItem component, final String value,
             final String name, final boolean checked, final boolean disabled)
             throws IOException {
 
         final ResponseWriter writer = context.getResponseWriter();
         writer.startElement(JsfConstants.INPUT_ELEM, component);
         writer.writeAttribute(JsfConstants.TYPE_ATTR, JsfConstants.RADIO_VALUE,
                 null);
         RendererUtil.renderIdAttributeIfNecessary(writer, component,
                 getIdForRender(context, component));
         writer.writeAttribute(JsfConstants.NAME_ATTR, name, null);
         if (checked) {
             writer.writeAttribute(JsfConstants.CHECKED_ATTR,
                     JsfConstants.CHECKED_ATTR, null);
         }
         if (!StringUtil.isEmpty(value)) {
             writer.writeAttribute(JsfConstants.VALUE_ATTR, value, null);
         }
         renderRemainAttributes(component, writer, ignoreComponent);
         if (disabled) {
            renderDisabledAttribute(writer);
         }
         writer.endElement(JsfConstants.INPUT_ELEM);
     }
 
     public Object getConvertedValue(final FacesContext context,
             final UIComponent component, final Object submittedValue)
             throws ConverterException {
         assertNotNull(context, component);
         return RendererUtil.getConvertedUIOutputValue(context,
                 (UIOutput) component, submittedValue);
     }
 
 }
