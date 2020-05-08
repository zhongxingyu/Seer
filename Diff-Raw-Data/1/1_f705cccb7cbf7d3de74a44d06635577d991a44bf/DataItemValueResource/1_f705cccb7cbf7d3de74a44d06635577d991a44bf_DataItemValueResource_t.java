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
 package com.amee.restlet.data;
 
 import com.amee.core.APIUtils;
 import com.amee.core.ThreadBeanHolder;
 import com.amee.domain.AMEEEntity;
 import com.amee.domain.AMEEStatus;
 import com.amee.domain.LocaleConstants;
 import com.amee.domain.StartEndDate;
 import com.amee.domain.data.DataCategory;
 import com.amee.domain.data.ItemValue;
 import com.amee.domain.data.ItemValueLocaleName;
 import com.amee.domain.data.LocaleName;
 import com.amee.domain.data.builder.v2.ItemValueBuilder;
 import com.amee.restlet.RequestContext;
 import com.amee.restlet.utils.APIFault;
 import com.amee.service.data.DataConstants;
 import org.apache.commons.collections.CollectionUtils;
 import org.apache.commons.collections.Predicate;
 import org.apache.commons.lang.StringUtils;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 import org.restlet.Context;
 import org.restlet.data.Form;
 import org.restlet.data.Request;
 import org.restlet.data.Response;
 import org.restlet.resource.Representation;
 import org.springframework.context.annotation.Scope;
 import org.springframework.stereotype.Component;
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Date;
 import java.util.List;
 import java.util.Map;
 
 //TODO - Move to builder model
 
 @Component
 @Scope("prototype")
 public class DataItemValueResource extends BaseDataResource implements Serializable {
 
     private final Log log = LogFactory.getLog(getClass());
 
     // Will be null is a sequence of ItemValues is being requested.
     private ItemValue itemValue;
 
     // Will be null is a single ItemValue is being requested.
     private List<ItemValue> itemValues;
 
     // The request may include a parameter which specifies how to retrieve a historical sequence of ItemValues.
     private int valuesPerPage = 1;
 
     @Override
     public void initialise(Context context, Request request, Response response) {
         super.initialise(context, request, response);
         setDataItemByPathOrUid(request.getAttributes().get("itemPath").toString());
         setDataItemValue(request);
         if (getDataItemValue() != null) {
             ((RequestContext) ThreadBeanHolder.get("ctx")).setItemValue(getDataItemValue());
         }
     }
 
     /**
      * Returns true if fetched objects for this request are valid.
      *
      * @return true if valid, otherwise false
      */
     @Override
     public boolean isValid() {
         return super.isValid() &&
                 (getDataItem() != null) &&
                 (isItemValueValid() || isItemValuesValid());
     }
 
     /**
      * Returns true if itemValue is valid. Internally calls isItemValueValid(ItemValue itemValue).
      * <p/>
      * An ItemValue is valid if; it is not trashed, it belongs to the current DataItem, it belongs
      * to the current Environment.
      *
      * @return true if the itemValue is valid, otherwise false
      */
     private boolean isItemValueValid() {
         return isItemValueValid(itemValue);
     }
 
     /**
      * Returns true if itemValue is valid.
      * <p/>
      * An ItemValue is valid if; it is not trashed, it belongs to the current DataItem, it belongs
      * to the current Environment.
      *
      * @param itemValue to validate
      * @return true if the itemValue is valid, otherwise false
      */
     private boolean isItemValueValid(ItemValue itemValue) {
         return (itemValue != null) &&
                 !itemValue.isTrash() &&
                 itemValue.getItem().equals(getDataItem()) &&
                 itemValue.getEnvironment().equals(getActiveEnvironment());
     }
 
     /**
      * Returns true if the itemValues list is valid.
      * <p/>
      * Each ItemValue is checked with isItemValueValid(ItemValue itemValue).
      * <p/>
      * The itemValues list may be modified during a call (invalid items will be removed).
      *
      * @return true if the itemValues is valid, otherwise false
      */
     @SuppressWarnings(value = "unchecked")
     private boolean isItemValuesValid() {
 
         // Must have a list if ItemValues.
         if (itemValues == null) {
             return false;
         }
 
         // Validate all ItemValues in the itemValues list and remove any invalid items.
         itemValues = (List<ItemValue>) CollectionUtils.select(itemValues, new Predicate() {
             public boolean evaluate(Object o) {
                 return isItemValueValid((ItemValue) o);
             }
         });
 
         // The itemValues list is invalid if it is empty.
         return !itemValues.isEmpty();
     }
 
     @Override
     public List<AMEEEntity> getEntities() {
         List<AMEEEntity> entities = new ArrayList<AMEEEntity>();
         entities.add(getDataItem());
         DataCategory dc = getDataItem().getDataCategory();
         while (dc != null) {
             entities.add(dc);
             dc = dc.getDataCategory();
         }
         entities.add(getActiveEnvironment());
         Collections.reverse(entities);
         return entities;
     }
 
     @Override
     public String getTemplatePath() {
         return getAPIVersion() + "/" + DataConstants.VIEW_ITEM_VALUE;
     }
 
     @Override
     // Note, itemValues (historical sequences) are not supported in V1 API and templates are only used in v1 API.
     public Map<String, Object> getTemplateValues() {
         Map<String, Object> values = super.getTemplateValues();
         values.put("browser", dataBrowser);
         values.put("dataItem", getDataItem());
         values.put("itemValue", itemValue);
         values.put("node", itemValue);
         values.put("availableLocales", LocaleConstants.AVAILABLE_LOCALES.keySet());
         return values;
     }
 
     private void setDataItemValue(Request request) {
 
         Form query = request.getResourceRef().getQueryAsForm();
 
         // Must have a DataItem.
         if (getDataItem() == null) {
             return;
         }
 
         // Get the ItemValue identifier, which could be a path or a uid.
         String itemValueIdentifier = request.getAttributes().get("valuePath").toString();
 
         // Identifier must not be empty.
         if (itemValueIdentifier.isEmpty()) {
             return;
         }
 
         // The resource may receive a startDate parameter that sets the current date in an historical sequence of
         // ItemValues.
         Date startDate = new Date();
         if (StringUtils.isNotBlank(query.getFirstValue("startDate"))) {
             startDate = new StartEndDate(query.getFirstValue("startDate"));
         }
 
         // The request may include a parameter which specifies how to retrieve a historical sequence of ItemValues.
         if (StringUtils.isNumeric(query.getFirstValue("valuesPerPage"))) {
             valuesPerPage = Integer.parseInt(query.getFirstValue("valuesPerPage"));
         }
 
         // TODO: Implement paging.
         // Retrieve all itemValues in a historical sequence if mandated in the request (get=all), otherwise retrieve
         // the closest match.
         if (valuesPerPage > 1) {
             itemValues = getDataItem().getAllItemValues(itemValueIdentifier);
         } else {
             itemValue = getDataItem().getItemValue(itemValueIdentifier, startDate);
         }
     }
 
     @Override
     public JSONObject getJSONObject() throws JSONException {
         JSONObject obj = new JSONObject();
         if (itemValue != null) {
             itemValue.setBuilder(new ItemValueBuilder(itemValue));
             obj.put("itemValue", itemValue.getJSONObject());
         } else {
             JSONArray values = new JSONArray();
             for (ItemValue iv : itemValues) {
                 iv.setBuilder(new ItemValueBuilder(iv));
                 values.put(iv.getJSONObject(false));
             }
             obj.put("itemValues", values);
         }
         obj.put("dataItem", getDataItem().getIdentityJSONObject());
         obj.put("path", pathItem.getFullPath());
         return obj;
     }
 
     @Override
     public Element getElement(Document document) {
         Element element = document.createElement("DataItemValueResource");
         if (itemValue != null) {
             itemValue.setBuilder(new ItemValueBuilder(itemValue));
             element.appendChild(itemValue.getElement(document));
         } else {
             Element values = document.createElement("ItemValues");
             for (ItemValue iv : itemValues) {
                 iv.setBuilder(new ItemValueBuilder(iv));
                 values.appendChild(iv.getElement(document, false));
             }
             element.appendChild(values);
         }
         element.appendChild(getDataItem().getIdentityElement(document));
         element.appendChild(APIUtils.getElement(document, "Path", pathItem.getFullPath()));
         return element;
     }
 
     @Override
     public boolean allowPost() {
         // POSTs to Data ItemValues are never allowed.
         return false;
     }
 
     /**
      * Update an ItemValue based on PUT parameters.
      *
      * @param entity representation
      */
     @Override
     public void doStore(Representation entity) {
 
         log.debug("doStore()");
 
         Form form = getForm();
 
         // Update the ItemValue value field if parameter is present.
         // NOTE: This code makes it impossible to set the value to empty or null.
         if (StringUtils.isNotBlank(form.getFirstValue("value"))) {
             itemValue.setValue(form.getFirstValue("value"));
         }
 
         // Parse any submitted locale values
         for (String name : form.getNames()) {
             if (name.startsWith("value_")) {
 
                 // Get the locale name and locale value from parameters.
                 String locale = name.substring(name.indexOf("_") + 1);
                 String localeValueStr = form.getFirstValue(name);
 
                 // Locale value cannot be blank and locale must exist.
                 if (StringUtils.isBlank(localeValueStr) || !LocaleConstants.AVAILABLE_LOCALES.containsKey(locale)) {
                     badRequest(APIFault.INVALID_PARAMETERS);
                     return;
                 }
 
                 // Does this locale already have an entry?
                 if (itemValue.getLocaleValues().containsKey(locale)) {
                     // Update the locale.
                     LocaleName localeName = itemValue.getLocaleValues().get(locale);
                     localeName.setName(localeValueStr);
                     // Should we remove this locale?
                     if (form.getNames().contains("remove_value_" + locale)) {
                         localeName.setStatus(AMEEStatus.TRASH);
                     }
                 } else {
                     // Create a locale entry based on supplied locale name and value.
                     LocaleName localeName =
                             new ItemValueLocaleName(itemValue, LocaleConstants.AVAILABLE_LOCALES.get(locale), localeValueStr);
                     itemValue.addLocaleName(localeName);
                 }
             }
         }
 
         // Has a startDate parameter been submitted?
         if (StringUtils.isNotBlank(form.getFirstValue("startDate"))) {
 
             // Parse the startDate parameter into a Date object.
             Date startDate = new StartEndDate(form.getFirstValue("startDate"));
 
             // Can't amend the startDate of the first ItemValue in a history (startDate == DI.startDate)
             if (itemValue.getStartDate().equals(getDataItem().getStartDate())) {
                 log.warn("doStore() badRequest - Trying to update the startDate of the first DIV in a history.");
                 badRequest(APIFault.INVALID_RESOURCE_MODIFICATION);
                 return;
             }
 
             // The submitted startDate must be on or after the epoch.
             if (!getDataItem().isWithinLifeTime(startDate)) {
                 log.warn("doStore() badRequest - Trying to update a DIV to start before the epoch.");
                 badRequest(APIFault.INVALID_DATE_RANGE);
                 return;
             }
 
             // Update the startDate field, the parameter was valid.
             itemValue.setStartDate(startDate);
         }
 
         // Always invalidate the DataCategory caches.
         dataService.invalidate(getDataItem().getDataCategory());
 
         // Update was a success.
         successfulPut(getFullPath());
     }
 
     /**
      * DELETEs an ItemValue from the DataItem. An ItemValue can only be removed if there is at least one
      * equivalent remaining ItemValue. Within a DataItem at least one ItemValue must
      * exist per ItemValueDefinition for the ItemDefinition.
      */
     @Override
     public void doRemove() {
         log.debug("doRemove()");
         int remaining = getDataItem().getAllItemValues(itemValue.getItemValueDefinition().getPath()).size();
         if (remaining > 1) {
             dataService.remove(itemValue);
             dataService.invalidate(getDataItem().getDataCategory());
             successfulDelete(pathItem.getParent().getFullPath());
         } else {
             badRequest(APIFault.DELETE_MUST_LEAVE_AT_LEAST_ONE_ITEM_VALUE);
         }
     }
 
     public ItemValue getDataItemValue() {
         return itemValue;
     }
 }
