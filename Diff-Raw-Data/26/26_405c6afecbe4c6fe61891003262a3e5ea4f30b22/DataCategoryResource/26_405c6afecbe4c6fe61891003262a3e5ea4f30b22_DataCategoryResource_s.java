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
 
 import com.amee.base.utils.ThreadBeanHolder;
 import com.amee.domain.AMEEEntity;
 import com.amee.domain.AMEEStatus;
 import com.amee.domain.LocaleConstants;
 import com.amee.domain.ObjectType;
 import com.amee.domain.data.DataCategory;
 import com.amee.domain.data.DataItem;
 import com.amee.domain.data.ItemDefinition;
 import com.amee.domain.data.ItemValue;
 import com.amee.domain.path.PathItem;
 import com.amee.restlet.RequestContext;
 import com.amee.restlet.data.builder.DataCategoryResourceBuilder;
 import com.amee.restlet.utils.APIFault;
 import com.amee.restlet.utils.APIUtils;
 import com.amee.service.data.DataBrowser;
 import com.amee.service.data.DataConstants;
 import com.amee.service.data.DataService;
 import com.amee.service.definition.DefinitionService;
 import com.amee.service.locale.LocaleService;
 import org.apache.commons.lang.StringUtils;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.dom4j.DocumentException;
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 import org.restlet.Context;
import org.restlet.data.Form;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Request;
import org.restlet.data.Response;
 import org.restlet.resource.Representation;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.context.annotation.Scope;
 import org.springframework.stereotype.Component;
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 
 import java.io.IOException;
 import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
 
 @Component("dataCategoryResource")
 @Scope("prototype")
 public class DataCategoryResource extends BaseDataResource implements Serializable {
 
     private final Log log = LogFactory.getLog(getClass());
 
     @Autowired
     private LocaleService localeService;
 
     @Autowired
     private DataService dataService;
 
     @Autowired
     private DefinitionService definitionService;
 
     @Autowired
     private DataCategoryResourceBuilder builder;
 
     private List<DataCategory> dataCategories;
     private DataItem dataItem;
     private List<DataItem> dataItems;
     private String type = "";
 
     @Override
     public void initialise(Context context, Request request, Response response) {
         super.initialise(context, request, response);
         setDataCategory(request.getAttributes().get("categoryUid").toString());
         if (getDataCategory() != null) {
             ((RequestContext) ThreadBeanHolder.get("ctx")).setCategory(getDataCategory());
         }
     }
 
     @Override
     public boolean isValid() {
         return super.isValid() &&
                 getDataCategory() != null &&
                 !getDataCategory().isTrash() &&
                 getDataCategory().getEnvironment().equals(getActiveEnvironment());
     }
 
     @Override
     public List<AMEEEntity> getEntities() {
         List<AMEEEntity> entities = new ArrayList<AMEEEntity>();
         DataCategory dc = getDataCategory();
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
         return getAPIVersion() + "/" + DataConstants.VIEW_DATA_CATEGORY;
     }
 
     @Override
     public Map<String, Object> getTemplateValues() {
         Map<String, Object> values = super.getTemplateValues();
         values.putAll(builder.getTemplateValues(this));
         return values;
     }
 
     @Override
     public JSONObject getJSONObject() throws JSONException {
         return builder.getJSONObject(this);
     }
 
     @Override
     public Element getElement(Document document) {
         return builder.getElement(this, document);
     }
 
     @Override
     public void doGet() {
         log.debug("doGet()");
         if (getAPIVersion().isNotVersionOne()) {
             Form form = getRequest().getResourceRef().getQueryAsForm();
             dataBrowser.setQueryStartDate(form.getFirstValue("startDate"));
             dataBrowser.setQueryEndDate(form.getFirstValue("endDate"));
         }
         super.doGet();
     }
 
     @Override
     public void doAcceptOrStore(Representation entity) {
 
         log.debug("doAcceptOrStore()");
 
         DataCategory thisDataCategory = getDataCategory();
 
         dataItems = new ArrayList<DataItem>();
         dataCategories = new ArrayList<DataCategory>();
 
         MediaType mediaType = entity.getMediaType();
         if (MediaType.APPLICATION_XML.includes(mediaType)) {
             acceptXML(entity);
         } else if (MediaType.APPLICATION_JSON.includes(mediaType)) {
             acceptJSON(entity);
         } else {
             if (getRequest().getMethod().equals(Method.POST)) {
                 acceptFormPost(getForm());
             } else if (getRequest().getMethod().equals(Method.PUT)) {
                 acceptFormPut(getForm());
             }
         }
 
         if ((dataCategory != null) || (dataItem != null) || !dataCategories.isEmpty() || !dataItems.isEmpty()) {
             if (isPost()) {
                 if (isBatchPost()) {
                     dataService.invalidate(thisDataCategory);
                     successfulBatchPost();
                 } else if ((dataCategory != null) && type.equalsIgnoreCase(ObjectType.DC.getName())) {
                     dataService.invalidate(dataCategory);
                     dataService.invalidate(thisDataCategory);
                     successfulPost(getFullPath(), dataCategory.getPath());
                 } else if ((dataItem != null) && type.equalsIgnoreCase(ObjectType.DI.getName())) {
                     dataService.invalidate(thisDataCategory);
                     successfulPost(getFullPath(), dataItem.getUid());
                 } else {
                     badRequest();
                 }
             } else {
                 successfulPut(getFullPath());
             }
         } else {
             badRequest();
         }
     }
 
     protected void acceptJSON(Representation entity) {
         log.debug("acceptJSON()");
         setIsBatchPost(true);
         DataCategory dataCategory;
         DataItem dataItem;
         Form form;
         String key;
         JSONObject rootJSON;
         JSONArray dataCategoriesJSON;
         JSONArray dataItemsJSON;
         JSONObject itemJSON;
         try {
             rootJSON = new JSONObject(entity.getText());
             if (rootJSON.has("dataCategories")) {
                 dataCategoriesJSON = rootJSON.getJSONArray("dataCategories");
                 for (int i = 0; i < dataCategoriesJSON.length(); i++) {
                     itemJSON = dataCategoriesJSON.getJSONObject(i);
                     form = new Form();
                     for (Iterator iterator = itemJSON.keys(); iterator.hasNext();) {
                         key = (String) iterator.next();
                         form.add(key, itemJSON.getString(key));
                     }
                     dataCategory = acceptFormForDataCategory(form);
                     if (dataCategory != null) {
                         dataCategories.add(dataCategory);
                     } else {
                         log.warn("acceptJSON() - Data Category not added/modified");
                         return;
                     }
                 }
             }
             if (rootJSON.has("dataItems")) {
                 dataItemsJSON = rootJSON.getJSONArray("dataItems");
                 for (int i = 0; i < dataItemsJSON.length(); i++) {
                     itemJSON = dataItemsJSON.getJSONObject(i);
                     form = new Form();
                     for (Iterator iterator = itemJSON.keys(); iterator.hasNext();) {
                         key = (String) iterator.next();
                         form.add(key, itemJSON.getString(key));
                     }
                     dataItem = acceptFormForDataItem(form);
                     if (dataItem != null) {
                         dataItems.add(dataItem);
                     } else {
                         log.warn("acceptJSON() - Data Item not added/modified");
                         return;
                     }
                 }
             }
         } catch (JSONException e) {
             log.warn("acceptJSON() - Caught JSONException: " + e.getMessage(), e);
         } catch (IOException e) {
             log.warn("acceptJSON() - Caught JSONException: " + e.getMessage(), e);
         }
     }
 
     protected void acceptXML(Representation entity) {
         log.debug("acceptXML()");
         setIsBatchPost(true);
         DataCategory dataCategory;
         DataItem dataItem;
         Form form;
         org.dom4j.Element rootElem;
         org.dom4j.Element dataCategoriesElem;
         org.dom4j.Element dataItemsElem;
         org.dom4j.Element itemElem;
         org.dom4j.Element valueElem;
         try {
             rootElem = APIUtils.getRootElement(entity.getStream());
             if (rootElem.getName().equalsIgnoreCase("DataCategory")) {
                 // handle Data Categories
                 dataCategoriesElem = rootElem.element("DataCategories");
                 if (dataCategoriesElem != null) {
                     for (Object o1 : dataCategoriesElem.elements("DataCategory")) {
                         itemElem = (org.dom4j.Element) o1;
                         form = new Form();
                         for (Object o2 : itemElem.elements()) {
                             valueElem = (org.dom4j.Element) o2;
                             form.add(valueElem.getName(), valueElem.getText());
                         }
                         dataCategory = acceptFormForDataCategory(form);
                         if (dataCategory != null) {
                             dataCategories.add(dataCategory);
                         } else {
                             log.warn("acceptXML() - Data Category not added");
                             return;
                         }
                     }
                 }
                 // handle Data Items
                 dataItemsElem = rootElem.element("DataItems");
                 if (dataItemsElem != null) {
                     for (Object o1 : dataItemsElem.elements("DataItem")) {
                         itemElem = (org.dom4j.Element) o1;
                         form = new Form();
                         for (Object o2 : itemElem.elements()) {
                             valueElem = (org.dom4j.Element) o2;
                             form.add(valueElem.getName(), valueElem.getText());
                         }
                         dataItem = acceptFormForDataItem(form);
                         if (dataItem != null) {
                             dataItems.add(dataItem);
                         } else {
                             log.warn("acceptXML() - Data Item not added");
                             return;
                         }
                     }
                 }
             } else {
                 log.warn("acceptXML() - DataCategory not found");
             }
         } catch (DocumentException e) {
             log.warn("acceptXML() - Caught DocumentException: " + e.getMessage(), e);
         } catch (IOException e) {
             log.warn("acceptXML() - Caught IOException: " + e.getMessage(), e);
         }
     }
 
     protected void acceptFormPost(Form form) {
         log.debug("acceptFormPost()");
         type = form.getFirstValue("newObjectType");
         if (type != null) {
             if (type.equalsIgnoreCase(ObjectType.DC.getName())) {
                 dataCategory = acceptFormForDataCategory(form);
             } else if (type.equalsIgnoreCase(ObjectType.DI.getName())) {
                 dataItem = acceptFormForDataItem(form);
             } else {
                 badRequest();
             }
         } else {
             badRequest();
         }
     }
 
     protected DataCategory acceptFormForDataCategory(Form form) {
         log.debug("acceptFormForDataCategory()");
         DataCategory dataCategory = null;
         if (getRequest().getMethod().equals(Method.POST)) {
             dataCategory = acceptFormForDataCategoryPost(form);
         } else if (getRequest().getMethod().equals(Method.PUT)) {
             dataCategory = acceptFormForDataCategoryPut(form);
         }
         return dataCategory;
     }
 
     /**
      * Creates a new DataCategory based on a POST.
      *
      * @param form
      * @return
      */
     protected DataCategory acceptFormForDataCategoryPost(Form form) {
 
         DataCategory dataCategory = null;
         DataCategory thisDataCategory;
 
         thisDataCategory = getDataCategory();
 
         if (thisDataCategory.getAliasedCategory() == null) {
             // new DataCategory
             dataCategory = new DataCategory(thisDataCategory);
 
             // Is this a sym-link to another DataCategory?
             if (form.getNames().contains("aliasedTo")) {
                 String aliasedToUid = form.getFirstValue("aliasedTo");
                 DataCategory aliasedTo = dataService.getDataCategoryByUid(aliasedToUid);
                 if (aliasedTo != null) {
                     dataCategory.setAliasedTo(aliasedTo);
                 } else {
                     badRequest(APIFault.INVALID_PARAMETERS);
                     return null;
                 }
             } else if (form.getNames().contains("itemDefinitionUid")) {
                 ItemDefinition itemDefinition =
                         definitionService.getItemDefinitionByUid(
                                 thisDataCategory.getEnvironment(), form.getFirstValue("itemDefinitionUid"));
                 if (itemDefinition != null) {
                     dataCategory.setItemDefinition(itemDefinition);
                 }
             }
 
             dataCategory = populateDataCategory(form, dataCategory);
 
             if (!validDataCategory(dataCategory)) {
                 badRequest();
                 // TODO: Should we set dataCategory to null here?
             } else if (!isUnique(dataCategory)) {
                 badRequest(APIFault.DUPLICATE_ITEM);
                 dataCategory = null;
             } else {
                 dataCategory = acceptDataCategory(form, dataCategory);
             }
         } else {
             notAuthorized();
             // TODO: Should we set dataCategory to null here?
         }
         return dataCategory;
     }
 
     /**
      * Updates a DataCategory based on a PUT.
      *
      * @param form
      * @return
      */
     protected DataCategory acceptFormForDataCategoryPut(Form form) {
         DataCategory dataCategory = null;
         String uid = form.getFirstValue("dataCategoryUid");
         if (uid != null) {
             dataCategory = dataService.getDataCategoryByUid(uid);
             if (dataCategory != null) {
                 dataCategory = populateDataCategory(form, dataCategory);
                 if (!validDataCategory(dataCategory)) {
                     badRequest();
                 } else {
                     dataCategory = acceptDataCategory(form, dataCategory);
                 }
             }
         }
         return dataCategory;
     }
 
     private boolean validDataCategory(DataCategory dataCategory) {
         return !(StringUtils.isBlank(dataCategory.getName()) || StringUtils.isBlank(dataCategory.getPath()));
     }
 
    // A unique DataCategory is one with a unique path within it's set of un-trashed sibling DataCategories.

     private boolean isUnique(DataCategory dataCategory) {
         boolean unique = true;
         for (PathItem sibling : getPathItem().getChildrenByType(ObjectType.DC.getName())) {
             if (sibling.getPath().equals(dataCategory.getPath())) {
                 DataCategory dc = dataService.getDataCategoryByUid(sibling.getUid());
                 if (dc != null) {
                     if (!dc.equals(dataCategory) && !dc.isTrash()) {
                         unique = false;
                         break;
                     }
                 } else {
                    log.error("isUnique() PathItem for DataCategory without persistent entity: " + sibling.getUid());
                 }
             }
         }
         return unique;
     }
 
     private DataCategory populateDataCategory(Form form, DataCategory dataCategory) {
         dataCategory.setName(form.getFirstValue("name"));
         dataCategory.setPath(form.getFirstValue("path"));
         return dataCategory;
     }
 
     private DataCategory acceptDataCategory(Form form, DataCategory dataCategory) {
         dataService.persist(dataCategory);
         return dataCategory;
     }
 
     protected DataItem acceptFormForDataItem(Form form) {
 
         log.debug("acceptFormForDataItem()");
 
         String uid;
         DataItem dataItem = null;
         ItemDefinition itemDefinition;
         DataCategory thisDataCategory;
 
         thisDataCategory = getDataCategory();
         if (getRequest().getMethod().equals(Method.POST)) {
             // New DataItem.
             itemDefinition = thisDataCategory.getItemDefinition();
             if (itemDefinition != null) {
                 // Create new DataItem, persist it and populate it.
                 dataItem = new DataItem(thisDataCategory, itemDefinition);
                 dataService.persist(dataItem);
                 acceptDataItem(form, dataItem);
             } else {
                 badRequest();
             }
         } else if (getRequest().getMethod().equals(Method.PUT)) {
             // Update DataItem.
             uid = form.getFirstValue("dataItemUid");
             if (uid != null) {
                 dataItem = dataService.getDataItem(getActiveEnvironment(), uid);
                 if (dataItem != null) {
                     acceptDataItem(form, dataItem);
                 }
             }
         }
         return dataItem;
     }
 
     /**
      * Update the DataItem and contained ItemValues based on POST or PUT parameters. ItemValues can be identified
      * by their ItemValueDefinition path or, for existing DataItems, their specific UID. The 'name' and 'path'
      * properties of the DataItem can be set / updated.
      * <p/>
      * When updating ItemValues using the ItemValueDefinition path the appropriate instance will
      * be selected based the current time, this will always be the last ItemValue in chronological order. This
      * is only relevant for non drill-down ItemValues, as only once instance of these is allowed.
      *
      * @param form     to handle
      * @param dataItem to update
      */
     protected void acceptDataItem(Form form, DataItem dataItem) {
 
         Set<String> names = form.getNames();
 
         // Set 'name' value.
         if (names.contains("name")) {
             dataItem.setName(form.getFirstValue("name"));
             names.remove("name");
         }
 
         // Set 'path' value.
         if (names.contains("path")) {
             dataItem.setPath(form.getFirstValue("path"));
             names.remove("path");
         }
 
         // Update item values if supplied.
         for (String name : form.getNames()) {
             ItemValue itemValue = dataItem.getItemValue(name);
             if (itemValue != null) {
                 itemValue.setValue(form.getFirstValue(name));
             } else {
                 log.warn("acceptDataItem() An ItemValue identifier was specified that does not exist: " + name);
             }
         }
     }
 
     public void acceptFormPut(Form form) {
 
         log.debug("acceptFormPut()");
 
         DataCategory thisDataCategory;
         thisDataCategory = getDataCategory();
         if (form.getNames().contains("name")) {
             thisDataCategory.setName(form.getFirstValue("name"));
         }
         if (form.getNames().contains("path")) {
             thisDataCategory.setPath(form.getFirstValue("path"));
         }
         if (form.getNames().contains("itemDefinitionUid")) {
 
             // A sym-link category cannot have an itemDefinition
             if (dataCategory.getAliasedCategory() != null) {
                 badRequest(APIFault.INVALID_PARAMETERS);
                 return;
             } else {
                 ItemDefinition itemDefinition =
                         definitionService.getItemDefinitionByUid(thisDataCategory.getEnvironment(),
                                 form.getFirstValue("itemDefinitionUid"));
                 if (itemDefinition != null) {
                     thisDataCategory.setItemDefinition(itemDefinition);
                 } else {
                     thisDataCategory.setItemDefinition(null);
                 }
             }
         }
 
         String deprecated = form.getFirstValue("deprecated");
         if (StringUtils.isNotBlank(deprecated)) {
             if (deprecated.equals("true")) {
                 thisDataCategory.setStatus(AMEEStatus.DEPRECATED);
             } else if (deprecated.equals("false")) {
                 thisDataCategory.setStatus(AMEEStatus.ACTIVE);
             }
         }
 
         // Parse any submitted locale names.
         for (String name : form.getNames()) {
             if (name.startsWith("name_")) {
                 // Get locale and locale name to handle.
                 String locale = name.substring(name.indexOf("_") + 1);
                 // Validate - Must have an available locale.
                 if (!LocaleConstants.AVAILABLE_LOCALES.containsKey(locale)) {
                     badRequest(APIFault.INVALID_PARAMETERS);
                     return;
                 }
                 // Remove or Update/Create?
                 if (form.getNames().contains("remove_name_" + locale)) {
                     // Remove.
                     localeService.clearLocaleName(thisDataCategory, locale);
                     thisDataCategory.onModify();
                 } else {
                     // Update or create.
                     String localeNameStr = form.getFirstValue(name);
                     // Validate - Must have a locale name value.
                     if (StringUtils.isBlank(localeNameStr)) {
                         badRequest(APIFault.INVALID_PARAMETERS);
                         return;
                     }
                     // Do the update or create.
                     localeService.setLocaleName(thisDataCategory, locale, localeNameStr);
                     thisDataCategory.onModify();
                 }
             }
         }
 
         dataService.invalidate(thisDataCategory);
         successfulPut(getFullPath());
         dataCategory = thisDataCategory;
     }
 
     @Override
     public void doRemove() {
         log.debug("doRemove()");
         dataService.invalidate(getDataCategory());
         dataService.remove(getDataCategory());
         successfulDelete(pathItem.getParent().getFullPath());
     }
 
     // TODO: This is not used in the Java code. Is it used by a template or in a script?
 
     public DataService getDataService() {
         return dataService;
     }
 
     public DataBrowser getDataBrowser() {
         return dataBrowser;
     }
 
     public DataCategory getDataCategory() {
         return dataCategory;
     }
 
     public List<DataCategory> getDataCategories() {
         return dataCategories;
     }
 
     public DataItem getDataItem() {
         return dataItem;
     }
 
     public List<DataItem> getDataItems() {
         return dataItems;
     }
 }
