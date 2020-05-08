 package com.amee.restlet.data.builder;
 
 import com.amee.base.utils.XMLUtils;
 import com.amee.domain.LocaleConstants;
 import com.amee.domain.ObjectType;
 import com.amee.domain.Pager;
 import com.amee.domain.data.DataCategory;
 import com.amee.domain.data.DataItem;
 import com.amee.domain.path.PathItem;
 import com.amee.domain.path.PathItemGroup;
 import com.amee.domain.sheet.Column;
 import com.amee.domain.sheet.Sheet;
 import com.amee.domain.sheet.SortOrder;
 import com.amee.restlet.data.DataCategoryResource;
 import com.amee.service.data.DataService;
 import com.amee.service.data.DataSheetService;
 import com.amee.service.definition.DefinitionService;
 import com.amee.service.path.PathItemService;
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Service;
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 
 import java.util.HashMap;
 import java.util.Map;
 
 /**
  * This file is part of AMEE.
  * <p/>
  * AMEE is free software; you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation; either version 3 of the License, or
  * (at your option) any later version.
  * <p/>
  * AMEE is free software and is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  * <p/>
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  * <p/>
  * Created by http://www.dgen.net.
  * Website http://www.amee.cc
  */
 @Service
 public class DataCategoryResourceBuilder {
 
     @Autowired
     private DataService dataService;
 
     @Autowired
     private DataSheetService dataSheetService;
 
     @Autowired
     private DefinitionService definitionService;
 
     @Autowired
     private PathItemService pathItemService;
 
     public JSONObject getJSONObject(DataCategoryResource resource) throws JSONException {
 
         // create JSON object
         JSONObject obj = new JSONObject();
         obj.put("path", resource.getPathItem().getFullPath());
 
         if (resource.isGet()) {
 
             // add DataCategory
             obj.put("dataCategory", resource.getDataCategory().getJSONObject(true));
 
             // list child Data Categories and child Data Items
             JSONObject children = new JSONObject();
 
             // Get PathItem.
             PathItem pathItem = getPathItem(resource);
 
             // add Data Categories via pathItem to children
             JSONArray dataCategories = new JSONArray();
             for (PathItem pi : pathItem.getChildrenByType(ObjectType.DC.getName())) {
                 dataCategories.put(pi.getJSONObject());
             }
             children.put("dataCategories", dataCategories);
 
             // addItemValue Sheet containing Data Items
             Sheet sheet = dataSheetService.getSheet(resource.getDataBrowser(), pathItem.getFullPath());
             if (sheet != null) {
                 String sortBy = resource.getRequest().getResourceRef().getQueryAsForm().getFirstValue("sortBy");
                 if (sortBy != null) {
                     Column c = sheet.getColumn(sortBy);
                     if (c != null) {
                         try {
                            c.setSortOrder(SortOrder.valueOf(resource.getRequest().getResourceRef().getQueryAsForm().getFirstValue("sortOrder", "")));
                         } catch (IllegalArgumentException e) {
                             // swallow
                         }
                         sheet = Sheet.getCopy(sheet);
                         sheet.getSortBy().getChoices().clear();
                         sheet.addSortBy(sortBy);
                         sheet.sortRows();
                     }
                 }
                 Pager pager = resource.getPager();
                 sheet = Sheet.getCopy(sheet, pager);
                 pager.setCurrentPage(resource.getPage());
                 children.put("dataItems", sheet.getJSONObject());
                 children.put("pager", pager.getJSONObject());
             } else {
                 children.put("dataItems", new JSONObject());
                 children.put("pager", new JSONObject());
             }
 
             // add children
             obj.put("children", children);
 
         } else if (resource.isPostOrPut()) {
 
             // DataCategories
             if (resource.getDataCategory() != null) {
                 obj.put("dataCategory", resource.getDataCategory().getJSONObject(true));
             } else if (resource.getDataCategories() != null) {
                 JSONArray dataCategories = new JSONArray();
                 obj.put("dataCategories", dataCategories);
                 for (DataCategory dc : resource.getDataCategories()) {
                     dataCategories.put(dc.getJSONObject(false));
                 }
             }
 
             // DataItems
             if (resource.getDataItem() != null) {
                 obj.put("dataItem", resource.getDataItem().getJSONObject(true));
             } else if (resource.getDataItems() != null) {
                 JSONArray dataItems = new JSONArray();
                 obj.put("dataItems", dataItems);
                 for (DataItem di : resource.getDataItems()) {
                     dataItems.put(di.getJSONObject(false));
                 }
             }
         }
 
         return obj;
     }
 
     public Element getElement(DataCategoryResource resource, Document document) {
 
         Element element = document.createElement("DataCategoryResource");
         element.appendChild(XMLUtils.getElement(document, "Path", resource.getPathItem().getFullPath()));
 
         if (resource.isGet()) {
 
             // add DataCategory
             element.appendChild(resource.getDataCategory().getElement(document, true));
 
             // list child Data Categories and child Data Items
             Element childrenElement = document.createElement("Children");
             element.appendChild(childrenElement);
 
             // Get PathItem.
             PathItem pathItem = getPathItem(resource);
 
             // add Data Categories
             Element dataCategoriesElement = document.createElement("DataCategories");
             for (PathItem pi : pathItem.getChildrenByType(ObjectType.DC.getName())) {
                 dataCategoriesElement.appendChild(pi.getElement(document));
             }
             childrenElement.appendChild(dataCategoriesElement);
 
             // list child Data Items via sheet
             Sheet sheet = dataSheetService.getSheet(resource.getDataBrowser(), pathItem.getFullPath());
             if (sheet != null) {
                 Pager pager = resource.getPager();
                 sheet = Sheet.getCopy(sheet, pager);
                 pager.setCurrentPage(resource.getPage());
                 childrenElement.appendChild(sheet.getElement(document, false));
                 childrenElement.appendChild(pager.getElement(document));
             }
 
         } else if (resource.isPostOrPut()) {
 
             // DataCategories
             if (resource.getDataCategory() != null) {
                 element.appendChild(resource.getDataCategory().getElement(document, false));
             } else if (resource.getDataCategories() != null) {
                 Element dataItemsElement = document.createElement("DataCategories");
                 element.appendChild(dataItemsElement);
                 for (DataCategory dc : resource.getDataCategories()) {
                     dataItemsElement.appendChild(dc.getElement(document, false));
                 }
             }
 
             // DataItems
             if (resource.getDataItem() != null) {
                 element.appendChild(resource.getDataItem().getElement(document, false));
             } else if (resource.getDataItems() != null) {
                 Element dataItemsElement = document.createElement("DataItems");
                 element.appendChild(dataItemsElement);
                 for (DataItem di : resource.getDataItems()) {
                     dataItemsElement.appendChild(di.getElement(document, false));
                 }
             }
         }
 
         return element;
     }
 
     public Map<String, Object> getTemplateValues(DataCategoryResource resource) {
         DataCategory dataCategory = resource.getDataCategory();
         PathItem pathItem = getPathItem(resource);
         Sheet sheet = dataSheetService.getSheet(resource.getDataBrowser(), pathItem.getFullPath());
         Map<String, Object> values = new HashMap<String, Object>();
         values.put("browser", resource.getDataBrowser());
         values.put("dataCategory", dataCategory);
         values.put("itemDefinition", dataCategory.getItemDefinition());
         values.put("user", resource.getActiveUser());
         values.put("itemDefinitions", definitionService.getItemDefinitions(resource.getActiveEnvironment()));
         values.put("node", dataCategory);
         values.put("availableLocales", LocaleConstants.AVAILABLE_LOCALES.keySet());
         if (sheet != null) {
             Pager pager = resource.getPager();
             sheet = Sheet.getCopy(sheet, pager);
             pager.setCurrentPage(resource.getPage());
             values.put("sheet", sheet);
             values.put("pager", pager);
         }
         values.put("pathItem", pathItem);
         // Ensure fullPath value comes from current pathItem and not symlink target pathItem.
         values.put("fullPath", "/data" + resource.getPathItem());
         return values;
     }
 
     public org.apache.abdera.model.Element getAtomElement() {
         return null;
     }
 
     private PathItem getPathItem(DataCategoryResource resource) {
         // If the DC is a symlink, use the target PathItem
         PathItem pathItem = resource.getPathItem();
         if (resource.getDataCategory().getAliasedCategory() != null) {
             PathItemGroup pathItemGroup = pathItemService.getPathItemGroup(resource.getActiveEnvironment());
             pathItem = pathItemGroup.findByUId(resource.getDataCategory().getAliasedCategory().getUid());
         }
         return pathItem;
     }
 }
