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
 package org.seasar.teeda.extension.html.factory;
 
 import java.util.Iterator;
 import java.util.Map;
 
 import org.seasar.framework.convention.NamingConvention;
 import org.seasar.framework.util.StringUtil;
 import org.seasar.teeda.core.JsfConstants;
 import org.seasar.teeda.extension.ExtensionConstants;
 import org.seasar.teeda.extension.html.ActionDesc;
 import org.seasar.teeda.extension.html.ElementNode;
 import org.seasar.teeda.extension.html.PageDesc;
import org.seasar.teeda.extension.util.TeedaExtensionConfiguration;
 
 /**
  * @author shot
  */
 public class OutputLabelFactory extends AbstractElementProcessorFactory {
 
     private static final String TAG_NAME = "outputLabel";
 
     private NamingConvention namingConvention;
 
     public OutputLabelFactory() {
     }
 
     public boolean isLeaf() {
         return true;
     }
 
     public boolean isMatch(final ElementNode elementNode,
             final PageDesc pageDesc, final ActionDesc actionDesc) {
        if (TeedaExtensionConfiguration.getInstance().disableLabelFactory) {
            return false;
        }
         final String tagName = elementNode.getTagName();
         if (!JsfConstants.LABEL_ELEM.equalsIgnoreCase(tagName)) {
             return false;
         }
         final String id = elementNode.getId();
         return (id != null);
     }
 
     protected void customizeProperties(final Map properties,
             final ElementNode elementNode, final PageDesc pageDesc,
             final ActionDesc actionDesc) {
         super
                 .customizeProperties(properties, elementNode, pageDesc,
                         actionDesc);
         LabelFactoryUtil.storeTemplateValue(properties, elementNode);
         if (pageDesc == null) {
             return;
         }
         LabelFactoryUtil.storeLabelAttributesTo(properties, elementNode,
                 pageDesc, namingConvention);
     }
 
     protected void customizeDynamicProperties(final String base,
             final Map properties, final ElementNode elementNode,
             final PageDesc pageDesc, final ActionDesc actionDesc) {
         if (base == null) {
             return;
         }
         for (final Iterator i = properties.keySet().iterator(); i.hasNext();) {
             final String key = (String) i.next();
             customizeDynamicProperty(base, key, properties, elementNode,
                     pageDesc, actionDesc);
         }
     }
 
     protected void customizeDynamicProperty(final String base,
             final String name, final Map properties,
             final ElementNode elementNode, final PageDesc pageDesc,
             final ActionDesc actionDesc) {
         if (pageDesc == null) {
             return;
         }
         final String pageName = pageDesc.getPageName();
         if (!StringUtil.isEmpty(base)) {
             final String propName = base + "Value";
             if (pageDesc.hasDynamicProperty(propName)) {
                 properties.put(JsfConstants.VALUE_ATTR, getBindingExpression(
                         pageName, propName));
             }
         }
     }
 
     protected String getTagName() {
         return TAG_NAME;
     }
 
     protected String getUri() {
         return ExtensionConstants.TEEDA_EXTENSION_URI;
     }
 
     public NamingConvention getNamingConvention() {
         return namingConvention;
     }
 
     public void setNamingConvention(final NamingConvention namingConvention) {
         this.namingConvention = namingConvention;
     }
 
 }
