 /**
  * This file is part of AMEE.
  *
  * AMEE is free software; you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation; either version 3 of the License, or
  * (at your option) any later version.
  *
  * AMEE is free software and is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  *
  * Created by http://www.dgen.net.
  * Website http://www.amee.cc
  */
 package com.amee.domain.data;
 
 import com.amee.core.APIUtils;
 import com.amee.core.ObjectType;
 import com.amee.domain.data.builder.v2.ItemValueBuilder;
 import com.amee.domain.sheet.Choice;
 import org.hibernate.annotations.Index;
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 
 import javax.persistence.Column;
 import javax.persistence.DiscriminatorValue;
 import javax.persistence.Entity;
 
 @Entity
 @DiscriminatorValue("DI")
 public class DataItem extends Item {
 
     public final static int PATH_SIZE = 255;
 
     @Column(name = "PATH", length = PATH_SIZE, nullable = true)
     @Index(name = "PATH_IND")
     private String path = "";
 
     public DataItem() {
         super();
     }
 
     public DataItem(DataCategory dataCategory, ItemDefinition itemDefinition) {
         super(dataCategory, itemDefinition);
     }
 
     public String toString() {
         return "DataItem_" + getUid();
     }
 
     public String getLabel() {
         String label = "";
         ItemValue itemValue;
         ItemDefinition itemDefinition = getItemDefinition();
         ItemValueMap itemValuesMap = getItemValuesMap();
         for (Choice choice : itemDefinition.getDrillDownChoices()) {
             itemValue = itemValuesMap.get(choice.getName());
             if ((itemValue != null) &&
                     (itemValue.getValue().length() > 0) &&
                     !itemValue.getValue().equals("-")) {
                 if (label.length() > 0) {
                     label = label.concat(", ");
                 }
                 label = label.concat(itemValue.getValue());
             }
         }
         if (label.length() == 0) {
             label = getDisplayPath();
         }
         return label;
     }
 
     private void buildElement(Document document, Element element, boolean detailed) {
         element.setAttribute("uid", getUid());
         element.appendChild(APIUtils.getElement(document, "Name", getDisplayName()));
         Element itemValuesElem = document.createElement("ItemValues");
         for (ItemValue itemValue : getItemValues()) {
             itemValue.setBuilder(new ItemValueBuilder(itemValue));
             itemValuesElem.appendChild(itemValue.getElement(document, false));
         }
         element.appendChild(itemValuesElem);
         if (detailed) {
             element.setAttribute("created", getCreated().toString());
             element.setAttribute("modified", getModified().toString());
             element.appendChild(getEnvironment().getIdentityElement(document));
             element.appendChild(getItemDefinition().getIdentityElement(document));
             element.appendChild(getDataCategory().getIdentityElement(document));
         }
     }
 
     private void buildElement(JSONObject obj, boolean detailed) throws JSONException {
         obj.put("uid", getUid());
         obj.put("name", getDisplayName());
         JSONArray itemValues = new JSONArray();
         for (ItemValue itemValue : getItemValues()) {
             itemValue.setBuilder(new ItemValueBuilder(itemValue));
             itemValues.put(itemValue.getJSONObject(false));
         }
         obj.put("itemValues", itemValues);
         if (detailed) {
             obj.put("created", getCreated());
             obj.put("modified", getModified());
             obj.put("environment", getEnvironment().getJSONObject());
             obj.put("itemDefinition", getItemDefinition().getJSONObject());
             obj.put("dataCategory", getDataCategory().getIdentityJSONObject());
         }
     }
 
     @Override
     public JSONObject getJSONObject(boolean detailed) throws JSONException {
         JSONObject obj = new JSONObject();
         buildElement(obj, detailed);
         obj.put("path", getPath());
         obj.put("label", getLabel());
         obj.put("startDate", getStartDate().toString());
         obj.put("endDate", (getEndDate() != null) ? getEndDate().toString() : "");
         return obj;
     }
 
     public Element getElement(Document document, boolean detailed) {
         Element dataItemElement = document.createElement("DataItem");
         buildElement(document, dataItemElement, detailed);
         dataItemElement.appendChild(APIUtils.getElement(document, "Path", getDisplayPath()));
         dataItemElement.appendChild(APIUtils.getElement(document, "Label", getLabel()));
         dataItemElement.appendChild(APIUtils.getElement(document, "StartDate", getStartDate().toString()));
         dataItemElement.appendChild(APIUtils.getElement(document, "EndDate", (getEndDate() != null) ? getEndDate().toString() : ""));
         return dataItemElement;
     }
 
    public String getResolvedPath() {
        if (getPath().isEmpty()) {
            return getUid();
        } else {
            return getPath();
        }
    }

     @Override
     public String getPath() {
         return path;
     }
 
     public void setPath(String path) {
         if (path == null) {
             path = "";
         }
         this.path = path;
     }
 
     @Override
     public ObjectType getObjectType() {
         return ObjectType.DI;
     }
 }
