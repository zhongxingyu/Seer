 package org.icefaces.ace.model.table;
 
 import org.icefaces.ace.util.collections.ExpressionListToValueListTransformer;
 
 import javax.el.ELContext;
 import javax.el.ELResolver;
 import javax.faces.context.FacesContext;
import java.io.Serializable;
 import java.util.List;
 
 /**
  * Copyright 2010-2011 ICEsoft Technologies Canada Corp.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  *
  * See the License for the specific language governing permissions and
  * limitations under the License.
  *
  */
public class CellSelections implements Serializable {
     private Object rowObject;
     private List<String> selectedFieldNames;
 
     public CellSelections(Object o, List<String> strings) {
         rowObject = o;
         selectedFieldNames = strings;
     }
 
     public Object getRowObject() {
         return rowObject;
     }
 
     public void setRowObject(Object rowObject) {
         this.rowObject = rowObject;
     }
 
     public List<String> getSelectedFieldNames() {
         return selectedFieldNames;
     }
 
     public void setSelectedFieldNames(List<String> selectedFieldNames) {
         this.selectedFieldNames = selectedFieldNames;
     }
 
     public List<Object> getSelectedFieldValues() {
         FacesContext context = FacesContext.getCurrentInstance();
         ELContext elContext = context.getELContext();
         ELResolver resolver = elContext.getELResolver();
         ExpressionListToValueListTransformer transformer =
                 new ExpressionListToValueListTransformer(context, elContext, resolver, rowObject);
 
         return transformer.transform(getSelectedFieldNames());
     }
 }
