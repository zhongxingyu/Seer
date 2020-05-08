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
 package org.seasar.teeda.extension.util;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.LinkedHashMap;
 import java.util.List;
 import java.util.Map;
 
 import javax.faces.component.UIComponent;
 import javax.faces.component.UIForm;
 import javax.faces.context.FacesContext;
 import javax.faces.context.ResponseWriter;
 import javax.faces.internal.scope.PageScope;
 
 import org.seasar.teeda.core.JsfConstants;
 import org.seasar.teeda.core.render.Base64EncodeConverter;
 import org.seasar.teeda.extension.ExtensionConstants;
 import org.seasar.teeda.extension.component.UIBody;
 import org.seasar.teeda.extension.render.RendererListener;
 import org.seasar.teeda.extension.render.TBodyRenderer;
 
 /**
  * @author koichik
  */
 public class ConditionUtil {
 
     public static final String KEY = "org.seasar.teeda.extension.Condition";
 
     public static Boolean getCondition(final FacesContext context,
             final String clientId) {
         final Map conditions = getConditions(context);
         if (conditions == null) {
             return null;
         }
         return (Boolean) conditions.get(clientId);
     }
 
     public static void addCondition(final FacesContext context,
             final String clientId, final boolean value) {
         final Map conditions = getOrCreateConditions(context);
         conditions.put(clientId, Boolean.valueOf(value));
     }
 
     public static Boolean removeCondition(final FacesContext context,
             final String clientId) {
         final Map conditions = getConditions(context);
         if (conditions == null) {
             return null;
         }
         return (Boolean) conditions.remove(clientId);
     }
 
     public static Map getConditions(final FacesContext context) {
         final Map map = PageScope.getContext(context);
         if (map == null) {
             return null;
         }
         return (Map) map.get(KEY);
     }
 
     public static Map getOrCreateConditions(final FacesContext context) {
         final Map map = PageScope.getOrCreateContext(context);
         Map conditions = (Map) map.get(KEY);
         if (conditions == null) {
             conditions = new LinkedHashMap(128);
             map.put(KEY, conditions);
         }
         return conditions;
     }
 
     public static void setConditions(final FacesContext context,
             final Map conditions) {
         final Map map = PageScope.getOrCreateContext(context);
         map.put(KEY, conditions);
     }
 
     public static void removeConditions(final FacesContext context) {
         final Map map = PageScope.getContext(context);
         if (map != null) {
             map.remove(KEY);
         }
     }
 
     public static List getForms(final FacesContext context) {
         final Map map = context.getExternalContext().getRequestMap();
         final List forms = (List) map.get(KEY);
         return forms;
     }
 
     public static void addForm(final FacesContext context, final UIForm form) {
         final Map map = context.getExternalContext().getRequestMap();
         List forms = (List) map.get(KEY);
         if (forms == null) {
             forms = new ArrayList();
             map.put(KEY, forms);
             registerRendererListener(form.getParent());
         }
         forms.add(form.getId());
     }
 
     protected static void registerRendererListener(UIComponent component) {
         while (component != null) {
             if (component instanceof UIBody) {
                 TBodyRenderer.addRendererListener(((UIBody) component),
                         new ConditionRendererListener());
                 break;
             }
             component = component.getParent();
         }
     }
 
     public static class ConditionRendererListener implements RendererListener {
 
         public void renderBeforeBodyEnd(final FacesContext context)
                 throws IOException {
             final Map conditions = getConditions(context);
             if (conditions == null || conditions.isEmpty()) {
                 return;
             }
             final List forms = getForms(context);
             if (forms == null || forms.isEmpty()) {
                 return;
             }
             renderJavascript(context.getResponseWriter(), conditions, forms);
         }
 
         protected static void renderJavascript(final ResponseWriter writer,
                 final Map conditions, final List forms) throws IOException {
             writer.write(JsfConstants.LINE_SP);
             writer.startElement(JsfConstants.SCRIPT_ELEM, null);
             writer.writeAttribute(JsfConstants.LANGUAGE_ATTR,
                     JsfConstants.JAVASCRIPT_VALUE, null);
             writer.writeAttribute(JsfConstants.TYPE_ATTR,
                     JsfConstants.TEXT_JAVASCRIPT_VALUE, null);
             writer.write(JsfConstants.LINE_SP);
             writer.write("<!--");
             writer.write(JsfConstants.LINE_SP);
 
             writer.write("var forms = [");
             for (int i = 0; i < forms.size(); ++i) {
                 final String form = (String) forms.get(i);
                 writer.write("'");
                 writer.write(form);
                 writer.write("'");
                 if (i < forms.size() - 1) {
                     writer.write(", ");
                 }
             }
             writer.write("];");
             writer.write(JsfConstants.LINE_SP);
 
            writer.write("for (var i = 0, len = forms.length; i < len; ++i) {");
             writer.write(JsfConstants.LINE_SP);
 
             writer.write("  var hidden = document.createElement('input');");
             writer.write(JsfConstants.LINE_SP);
             writer.write("  hidden.setAttribute('type', 'hidden');");
             writer.write(JsfConstants.LINE_SP);
             writer.write("  hidden.setAttribute('name', '");
             writer.write(ExtensionConstants.CONDITIONS_PARAMETER);
             writer.write("');");
             writer.write(JsfConstants.LINE_SP);
             writer.write("  hidden.setAttribute('value', '");
             final Base64EncodeConverter converter = new Base64EncodeConverter();
             final String value = converter.getAsEncodeString(conditions);
             writer.write(value);
             writer.write("');");
             writer.write(JsfConstants.LINE_SP);
 
             writer.write("  var form = document.getElementById(forms[i]);");
             writer.write(JsfConstants.LINE_SP);
             writer.write("  form.appendChild(hidden);");
             writer.write(JsfConstants.LINE_SP);
 
             writer.write("}");
             writer.write(JsfConstants.LINE_SP);
 
             writer.write(JsfConstants.LINE_SP);
             writer.write("//-->");
             writer.write(JsfConstants.LINE_SP);
             writer.endElement(JsfConstants.SCRIPT_ELEM);
         }
     }
 
 }
