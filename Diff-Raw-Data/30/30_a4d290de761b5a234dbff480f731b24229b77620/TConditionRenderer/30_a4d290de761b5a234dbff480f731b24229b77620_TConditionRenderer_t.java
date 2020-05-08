 /*
  * Copyright 2004-2008 the Seasar Foundation and the Others.
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
 import javax.faces.internal.IgnoreAttribute;
 
 import org.seasar.teeda.core.JsfConstants;
 import org.seasar.teeda.core.render.AbstractRenderer;
 import org.seasar.teeda.core.util.RendererUtil;
 import org.seasar.teeda.extension.ExtensionConstants;
 import org.seasar.teeda.extension.component.TCondition;
 
 /**
  * @author shot
  */
 public class TConditionRenderer extends AbstractRenderer {
 
     public static final String COMPONENT_FAMILY = "org.seasar.teeda.extension.Condition";
 
     public static final String RENDERER_TYPE = "org.seasar.teeda.extension.Condition";
 
     private final IgnoreAttribute attribute = new IgnoreAttribute();
     {
         attribute.addAttributeName(JsfConstants.ID_ATTR);
         attribute.addAttributeName(ExtensionConstants.SUBMITTED);
        attribute.addAttributeName(ExtensionConstants.TAGNAME_ATTR);
        attribute.addAttributeName(ExtensionConstants.REFRESH_ATTR);
        attribute.addAttributeName(ExtensionConstants.OMITTAG_ATTR);
        attribute.addAttributeName(ExtensionConstants.INVISIBLE_ATTR);
     }
 
     public void encodeBegin(FacesContext context, UIComponent component)
             throws IOException {
         super.encodeBegin(context, component);
         if (!component.isRendered()) {
             return;
         }
         TCondition condition = (TCondition) component;
         final boolean invisible = condition.isInvisible();
         final boolean omittag = condition.isOmittag();
         if (!(invisible || omittag)) {
             final ResponseWriter writer = context.getResponseWriter();
             writer.startElement(condition.getTagName(), condition);
             RendererUtil.renderIdAttributeIfNecessary(writer, component,
                     getIdForRender(context, condition));
             renderRemainAttributes(condition, writer, attribute);
         }
     }
 
     public void encodeEnd(FacesContext context, UIComponent component)
             throws IOException {
         super.encodeEnd(context, component);
         if (!component.isRendered()) {
             return;
         }
         TCondition condition = (TCondition) component;
         final boolean invisible = condition.isInvisible();
         final boolean omittag = condition.isOmittag();
         if (!(invisible || omittag)) {
             final ResponseWriter writer = context.getResponseWriter();
             writer.endElement(condition.getTagName());
         }
     }
 
     public boolean getRendersChildren() {
         return true;
     }
 
 }
