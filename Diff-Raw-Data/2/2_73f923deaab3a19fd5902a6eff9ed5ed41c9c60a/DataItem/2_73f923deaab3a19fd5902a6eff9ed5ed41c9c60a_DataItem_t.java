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
 import com.amee.domain.AMEEStatus;
 import com.amee.domain.ObjectType;
 import com.amee.domain.data.builder.v2.ItemValueBuilder;
 import com.amee.domain.sheet.Choice;
 import com.amee.platform.science.StartEndDate;
 import org.hibernate.annotations.Index;
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 
 import javax.persistence.Column;
 import javax.persistence.DiscriminatorValue;
 import javax.persistence.Entity;
 import java.util.Date;
 
 @Entity
 @DiscriminatorValue("DI")
 public class DataItem extends Item {
 
     public final static int PATH_SIZE = 255;
 
     // The UNIX time epoch, which is 1970-01-01 00:00:00. See: http://en.wikipedia.org/wiki/Unix_epoch
    public final static Date EPOCH = new Date(0);
 
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
         for (Choice choice : itemDefinition.getDrillDownChoices()) {
             itemValue = getItemValue(choice.getName());
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
 
     private void buildElement(Document document, Element element, boolean detailed, boolean showHistory) {
         element.setAttribute("uid", getUid());
         element.appendChild(APIUtils.getElement(document, "Name", getDisplayName()));
         Element itemValuesElem = document.createElement("ItemValues");
         if (showHistory) {
             buildElementItemValuesWithHistory(document, itemValuesElem);
         } else {
             buildElementItemValues(document, itemValuesElem);
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
 
     private void buildElementItemValues(Document document, Element itemValuesElem) {
         for (ItemValue itemValue : getItemValues()) {
             itemValue.setBuilder(new ItemValueBuilder(itemValue));
             itemValuesElem.appendChild(itemValue.getElement(document, false));
         }
     }
 
     private void buildElementItemValuesWithHistory(Document document, Element itemValuesElem) {
         for (Object o1 : getItemValuesMap().keySet()) {
             String path = (String) o1;
             Element itemValueSeries = document.createElement("ItemValueSeries");
             itemValueSeries.setAttribute("path", path);
             for (Object o2 : getAllItemValues(path)) {
                 ItemValue itemValue = (ItemValue) o2;
                 itemValue.setBuilder(new ItemValueBuilder(itemValue));
                 itemValueSeries.appendChild(itemValue.getElement(document, false));
             }
             itemValuesElem.appendChild(itemValueSeries);
         }
     }
 
     private void buildJSON(JSONObject obj, boolean detailed, boolean showHistory) throws JSONException {
         obj.put("uid", getUid());
         obj.put("name", getDisplayName());
         JSONArray itemValues = new JSONArray();
         if (showHistory) {
             buildJSONItemValuesWithHistory(itemValues);
         } else {
             buildJSONItemValues(itemValues);
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
 
     private void buildJSONItemValues(JSONArray itemValues) throws JSONException {
         for (ItemValue itemValue : getItemValues()) {
             itemValue.setBuilder(new ItemValueBuilder(itemValue));
             itemValues.put(itemValue.getJSONObject(false));
         }
     }
 
     private void buildJSONItemValuesWithHistory(JSONArray itemValues) throws JSONException {
         for (Object o1 : getItemValuesMap().keySet()) {
             String path = (String) o1;
             JSONObject values = new JSONObject();
             JSONArray valueSet = new JSONArray();
             for (Object o2 : getAllItemValues(path)) {
                 ItemValue itemValue = (ItemValue) o2;
                 itemValue.setBuilder(new ItemValueBuilder(itemValue));
                 valueSet.put(itemValue.getJSONObject(false));
             }
             values.put(path, valueSet);
             itemValues.put(values);
         }
     }
 
     /**
      * Get the JSON representation of this DataItem.
      *
      * @param detailed    - true if a detailed representation is required.
      * @param showHistory - true if the representation should include any historical sequences of {@link ItemValue)s.
      * @return the JSON representation.
      * @throws JSONException
      */
     public JSONObject getJSONObject(boolean detailed, boolean showHistory) throws JSONException {
         JSONObject obj = new JSONObject();
         buildJSON(obj, detailed, showHistory);
         obj.put("path", getPath());
         obj.put("label", getLabel());
         obj.put("startDate", getStartDate().toString());
         obj.put("endDate", (getEndDate() != null) ? getEndDate().toString() : "");
         return obj;
     }
 
     @Override
     public JSONObject getJSONObject(boolean detailed) throws JSONException {
         return getJSONObject(detailed, false);
     }
 
     /**
      * Get the DOM representation of this DataItem.
      *
      * @param detailed    - true if a detailed representation is required.
      * @param showHistory - true if the representation should include any historical sequences of {@link ItemValue)s.
      * @return the DOM representation.
      */
     public Element getElement(Document document, boolean detailed, boolean showHistory) {
         Element dataItemElement = document.createElement("DataItem");
         buildElement(document, dataItemElement, detailed, showHistory);
         dataItemElement.appendChild(APIUtils.getElement(document, "Path", getDisplayPath()));
         dataItemElement.appendChild(APIUtils.getElement(document, "Label", getLabel()));
         dataItemElement.appendChild(APIUtils.getElement(document, "StartDate", getStartDate().toString()));
         dataItemElement.appendChild(APIUtils.getElement(document, "EndDate", (getEndDate() != null) ? getEndDate().toString() : ""));
         return dataItemElement;
     }
 
     public Element getElement(Document document, boolean detailed) {
         return getElement(document, detailed, false);
     }
 
     public String getResolvedPath() {
         if (getPath().isEmpty()) {
             return getUid();
         } else {
             return getPath();
         }
     }
 
     public String getPath() {
         return path;
     }
 
     public void setPath(String path) {
         if (path == null) {
             path = "";
         }
         this.path = path;
     }
 
     public ObjectType getObjectType() {
         return ObjectType.DI;
     }
 
     @Override
     public boolean isTrash() {
         return status.equals(AMEEStatus.TRASH) || getDataCategory().isTrash() || getItemDefinition().isTrash();
     }
 
     /**
      * A DataItem always has the epoch as the startDate.
      *
      * @return EPOCH
      */
     @Override
     public StartEndDate getStartDate() {
         return new StartEndDate(EPOCH);
     }
 
     /**
      * A DataItem never has an endDate.
      *
      * @return null
      */
     @Override
     public StartEndDate getEndDate() {
         return null;
     }
 }
