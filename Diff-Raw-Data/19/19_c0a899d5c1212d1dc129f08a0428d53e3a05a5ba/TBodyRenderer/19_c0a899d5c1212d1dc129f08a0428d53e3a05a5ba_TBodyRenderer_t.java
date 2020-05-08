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
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 
 import javax.faces.component.UIComponent;
 import javax.faces.context.FacesContext;
 import javax.faces.context.ResponseWriter;
 import javax.faces.internal.IgnoreAttribute;
 
 import org.seasar.framework.util.AssertionUtil;
 import org.seasar.teeda.core.JsfConstants;
 import org.seasar.teeda.core.exception.TagNotFoundRuntimeException;
 import org.seasar.teeda.core.render.AbstractRenderer;
 import org.seasar.teeda.extension.ExtensionConstants;
 import org.seasar.teeda.extension.component.UIBody;
 
 /**
  * @author shot
  */
 public class TBodyRenderer extends AbstractRenderer {
 
     public static final String COMPONENT_FAMILY = "org.seasar.teeda.extension.Body";
 
     public static final String RENDERER_TYPE = "org.seasar.teeda.extension.Body";
 
     private static final String KEY_RENDERER_LISTENER = RendererListener.class
             .getName()
             + "_KEY_LISTENER_STACK";
 
     private static final IgnoreAttribute IGNORE_COMPONENT;
 
     static {
         IGNORE_COMPONENT = buildIgnoreComponent();
     }
 
     public void encodeBegin(FacesContext context, UIComponent component)
             throws IOException {
         assertNotNull(context, component);
         final ResponseWriter writer = context.getResponseWriter();
         writer.startElement(ExtensionConstants.BODY_ELEM, component);
         renderRemainAttributes(component, writer, IGNORE_COMPONENT);
     }
 
     public static void addRendererListener(UIBody body,
             RendererListener listener) {
         AssertionUtil.assertNotNull("listener", listener);
         Map attributes = body.getAttributes();
         List listeners = (List) attributes.get(KEY_RENDERER_LISTENER);
         if (listeners == null) {
             listeners = new ArrayList();
         }
         listeners.add(listener);
         attributes.put(KEY_RENDERER_LISTENER, listeners);
     }
 
     public void encodeEnd(FacesContext context, UIComponent component)
             throws IOException {
         assertNotNull(context, component);
         final ResponseWriter writer = context.getResponseWriter();
         UIBody body = (UIBody) component;
         Map attributes = body.getAttributes();
         List listeners = (List) attributes.remove(KEY_RENDERER_LISTENER);
         if (listeners != null) {
             for (Iterator itr = listeners.iterator(); itr.hasNext();) {
                 RendererListener listener = (RendererListener) itr.next();
                 listener.renderBeforeBodyEnd(context);
             }
         }
         writer.endElement(ExtensionConstants.BODY_ELEM);
     }
 
     protected static IgnoreAttribute buildIgnoreComponent() {
         IgnoreAttribute ignore = new IgnoreAttribute();
         ignore.addAttributeName("javax.faces.webapp.COMPONENT_IDS");
         return ignore;
     }
 
     public static UIBody findParentBody(final UIComponent component) {
         UIComponent parent = component.getParent();
         while (parent != null && !(parent instanceof UIBody)) {
             parent = parent.getParent();
         }
         if (parent == null) {
             throw new TagNotFoundRuntimeException("body");
         }
         return (UIBody) parent;
     }
 
 }
