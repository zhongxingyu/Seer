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
 package javax.faces.model;
 
 import java.io.Serializable;
 
 import org.seasar.framework.util.AssertionUtil;
 
 /**
  * @author shot
  * @author manhole
  */
 public class SelectItem implements Serializable {
 
     private static final long serialVersionUID = 1L;
 
     private Object value = null;
 
     private String label = null;
 
     private String description = null;
 
     private boolean disabled = false;
 
     public SelectItem() {
     }
 
     public SelectItem(Object value) {
         AssertionUtil.assertNotNull("value", value);
         this.value = value;
     }
 
     public SelectItem(Object value, String label) {
         this(value, label, null);
     }
 
     public SelectItem(Object value, String label, String description) {
         this(value, label, description, false);
     }
 
     public SelectItem(Object value, String label, String description,
             boolean disabled) {
         AssertionUtil.assertNotNull("value", value);
         AssertionUtil.assertNotNull("label", label);
         this.value = value;
         this.label = label;
         this.description = description;
         this.disabled = disabled;
     }
 
     public String getDescription() {
         return description;
     }
 
     public void setDescription(String description) {
         this.description = description;
     }
 
     public boolean isDisabled() {
         return disabled;
     }
 
     public void setDisabled(boolean disabled) {
         this.disabled = disabled;
     }
 
     public String getLabel() {
         return label;
     }
 
     public void setLabel(String label) {
         AssertionUtil.assertNotNull("label", label);
         this.label = label;
     }
 
     public Object getValue() {
         return value;
     }
 
     public void setValue(Object value) {
         AssertionUtil.assertNotNull("value", value);
         this.value = value;
     }
 
 }
