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
 package org.seasar.teeda.extension.component.html;
 
 import javax.faces.component.ComponentUtil_;
 import javax.faces.component.html.HtmlSelectOneRadio;
 import javax.faces.context.FacesContext;
 import javax.faces.el.VariableResolver;
import javax.faces.event.ValueChangeEvent;
 
import org.seasar.framework.util.AssertionUtil;
 import org.seasar.teeda.extension.ExtensionConstants;
 
 /**
  * @author shot
  */
 public class THtmlSelectOneRadio extends HtmlSelectOneRadio {
 
     public static final String COMPONENT_TYPE = "org.seasar.teeda.extension.HtmlSelectOneRadio";
 
     public static final String DEFAULT_RENDERER_TYPE = "org.seasar.teeda.extension.HtmlSelectOneRadio";
 
     private Integer col = null;
 
     private String pageName;
 
     private String labelName;
 
     public void validate(FacesContext context) {
        AssertionUtil.assertNotNull("context", context);
        final Object submittedValue = getSubmittedValue();
        final Object convertedValue = getConvertedValue(context, submittedValue);
        if (!isValid()) {
            return;
        }
        validateValue(context, convertedValue);
        if (!isValid()) {
            return;
        }
        Object previous = getValue();
        setValue(convertedValue);
        setSubmittedValue(null);
        if (compareValues(previous, convertedValue)) {
            queueEvent(new ValueChangeEvent(this, previous, convertedValue));
        }
         THtmlSelectUtil.takeOverLabel(context, this, getPage(context),
                 getLabelName());
     }
 
     private Object getPage(final FacesContext context) {
         final VariableResolver variableResolver = context.getApplication()
                 .getVariableResolver();
         return variableResolver.resolveVariable(context, getPageName());
     }
 
     public Object saveState(final FacesContext context) {
         final Object[] values = new Object[4];
         values[0] = super.saveState(context);
         values[1] = col;
         values[2] = pageName;
         values[3] = labelName;
         return values;
     }
 
     public void restoreState(final FacesContext context, final Object state) {
         final Object[] values = (Object[]) state;
         super.restoreState(context, values[0]);
         col = (Integer) values[1];
         pageName = (String) values[2];
         labelName = (String) values[3];
     }
 
     public Integer getCol() {
         if (col != null) {
             return col;
         }
         Object value = ComponentUtil_.getValueBindingValue(this,
                 ExtensionConstants.COL_ATTR);
         return (value != null) ? (Integer) value : null;
 
     }
 
     public void setCol(Integer col) {
         this.col = col;
     }
 
     public String getLabelName() {
         if (labelName != null) {
             return labelName;
         }
         Object value = ComponentUtil_.getValueBindingValue(this,
                 ExtensionConstants.LABEL_NAME_ATTR);
         return (value != null) ? (String) value : null;
     }
 
     public String getPageName() {
         if (pageName != null) {
             return pageName;
         }
         Object value = ComponentUtil_.getValueBindingValue(this,
                 ExtensionConstants.PAGE_NAME_ATTR);
         return (value != null) ? (String) value : null;
     }
 
     public void setLabelName(String labelName) {
         this.labelName = labelName;
     }
 
     public void setPageName(String pageName) {
         this.pageName = pageName;
     }
 
 }
