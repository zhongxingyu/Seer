 /*
  * (C) Copyright 2006-2010 Nuxeo SAS (http://nuxeo.com/) and contributors.
  *
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the GNU Lesser General Public License
  * (LGPL) version 2.1 which accompanies this distribution, and is available at
  * http://www.gnu.org/licenses/lgpl.html
  *
  * This library is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
  * Lesser General Public License for more details.
  *
  * Contributors:
  *     bstefanescu
  */
 package org.nuxeo.ide.connect;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.eclipse.jdt.core.ICompilationUnit;
 import org.eclipse.jdt.core.IType;
 
 /**
  * @author <a href="mailto:bs@nuxeo.com">Bogdan Stefanescu</a>
  * 
  */
 public class OperationModel {
 
     protected IType type;
 
     protected Map<String, String> properties;
 
     protected String[] signature;
 
     protected List<Param> params;
 
     public OperationModel(IType type) {
         this.type = type;
         this.properties = new HashMap<String, String>();
         this.params = new ArrayList<Param>();
     }
 
     public IType getType() {
         return type;
     }
 
     public ICompilationUnit getCompilationUnit() {
         return type.getCompilationUnit();
     }
 
     public String getId() {
         return properties.get("id");
     }
 
     public String getCategory() {
         return properties.get("category");
     }
 
     public String getLabel() {
         return properties.get("label");
     }
 
     public String getRequires() {
         return properties.get("requires");
     }
 
     public String getDescription() {
         return properties.get("description");
     }
 
     public String getSince() {
         return properties.get("since");
     }
 
     public String getProperty(String key) {
         return properties.get(key);
     }
 
     public Map<String, String> getProperties() {
         return properties;
     }
 
     public String[] getSignature() {
         return signature;
     }
 
     public void setSignature(String[] signature) {
         this.signature = signature;
     }
 
     public List<Param> getParams() {
         return params;
     }
 
     public void addParam(Param param) {
         params.add(param);
     }
 
     public static class Param {
         protected String type;
 
         protected String name;
 
         protected boolean required = true;
 
        protected String[] values = new String[0];
 
         protected String widget;
 
         protected int order;
 
         public Param() {
         }
 
         public void setType(String type) {
             this.type = type;
         }
 
         public String getType() {
             return type;
         }
 
         public String getName() {
             return name;
         }
 
         public void setName(String name) {
             this.name = name;
         }
 
         public boolean isRequired() {
             return required;
         }
 
         public void setRequired(boolean required) {
             this.required = required;
         }
 
         public int getOrder() {
             return order;
         }
 
         public void setOrder(int order) {
             this.order = order;
         }
 
         public String[] getValues() {
             return values;
         }
 
         public void setValues(String[] values) {
             this.values = values;
         }
 
         public String getWidget() {
             return widget;
         }
 
         public void setWidget(String widget) {
             this.widget = widget;
         }
     }
 
 }
