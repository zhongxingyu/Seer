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
 package org.seasar.teeda.extension.html.factory;
 
 import java.util.HashMap;
 import java.util.Map;
 
import javax.faces.context.FacesContext;

 import org.seasar.framework.util.StringUtil;
 import org.seasar.teeda.core.JsfConstants;
import org.seasar.teeda.core.util.PortletUtil;
 import org.seasar.teeda.extension.ExtensionConstants;
 import org.seasar.teeda.extension.html.ActionDesc;
 import org.seasar.teeda.extension.html.ElementNode;
 import org.seasar.teeda.extension.html.ElementProcessor;
 import org.seasar.teeda.extension.html.PageDesc;
 import org.seasar.teeda.extension.html.processor.ElementProcessorImpl;
 
 /**
  * @author higa
  *
  */
 public class OutputLinkFactory extends AbstractElementProcessorFactory {
 
     private static final String TAG_NAME = "outputLink";
 
     private static final String PARAM_TAG_NAME = "param";
 
     private static final String FIXED_PREFIX = "fixed_";
 
     public boolean isMatch(ElementNode elementNode, PageDesc pageDesc,
             ActionDesc actionDesc) {
         if (!JsfConstants.ANCHOR_ELEM
                 .equalsIgnoreCase(elementNode.getTagName())) {
             return false;
         }
         String id = elementNode.getId();
         if (id == null) {
             return false;
         }
        // PortletSupport
        //TODO facesContext?
        if (PortletUtil.isPortlet(FacesContext.getCurrentInstance())) {
            return id.startsWith(ExtensionConstants.GO_PREFIX)
                    || id.startsWith(ExtensionConstants.JUMP_PREFIX);
        }
         return id.startsWith(ExtensionConstants.GO_PREFIX);
     }
 
     protected void customizeProperties(Map properties, ElementNode elementNode,
             PageDesc pageDesc, ActionDesc actionDesc) {
         super
                 .customizeProperties(properties, elementNode, pageDesc,
                         actionDesc);
         renameProperty(properties, JsfConstants.HREF_ATTR,
                 JsfConstants.VALUE_ATTR);
     }
 
     protected void customizeProcessor(ElementProcessor processor,
             ElementNode elementNode, PageDesc pageDesc, ActionDesc actionDesc) {
         super.customizeProcessor(processor, elementNode, pageDesc, actionDesc);
         if (pageDesc == null) {
             return;
         }
         String value = processor.getProperty(JsfConstants.VALUE_ATTR);
         if (value == null) {
             return;
         }
         int index = value.lastIndexOf('?');
         if (index < 0) {
             return;
         }
         StringBuffer buf = new StringBuffer(50);
         String queryString = value.substring(index + 1);
         String[] entries = StringUtil.split(queryString, "&");
         for (int i = 0; i < entries.length; ++i) {
             String entry = entries[i];
             if (entry.startsWith(FIXED_PREFIX)) {
                 buf.append(entry.substring(FIXED_PREFIX.length()));
                 buf.append("&");
             } else {
                 String[] elems = StringUtil.split(entry, " =");
                 if (pageDesc.hasProperty(elems[0])) {
                     appendParam(processor, pageDesc, elems[0]);
                 } else {
                     buf.append(entry);
                     buf.append("&");
                 }
             }
         }
         value = value.substring(0, index);
         if (buf.length() != 0) {
             buf.setLength(buf.length() - 1);
             value = value + "?" + buf.toString();
         }
         processor.setProperty(JsfConstants.VALUE_ATTR, value);
     }
 
     protected void appendParam(ElementProcessor processor, PageDesc pageDesc,
             String name) {
         Class tagClass = getTagClass(JsfConstants.JSF_CORE_URI, PARAM_TAG_NAME);
         Map props = new HashMap();
         props.put(JsfConstants.NAME_ATTR, name);
         props.put(JsfConstants.VALUE_ATTR, getBindingExpression(pageDesc
                 .getPageName(), name));
         ElementProcessor paramProcessor = new ElementProcessorImpl(tagClass,
                 props);
         processor.addElement(paramProcessor);
     }
 
     protected String getTagName() {
         return TAG_NAME;
     }
 
     protected String getUri() {
         return ExtensionConstants.TEEDA_EXTENSION_URI;
     }
 }
