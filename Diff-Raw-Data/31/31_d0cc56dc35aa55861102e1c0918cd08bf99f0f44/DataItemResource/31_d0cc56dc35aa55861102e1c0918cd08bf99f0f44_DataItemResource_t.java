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
 
 import com.amee.calculation.service.CalculationService;
 import com.amee.core.APIUtils;
 import com.amee.domain.StartEndDate;
 import com.amee.domain.data.DataItem;
 import com.amee.domain.data.ItemValue;
 import com.amee.domain.data.ItemValueMap;
 import com.amee.domain.sheet.Choice;
 import com.amee.domain.sheet.Choices;
 import com.amee.service.data.DataConstants;
 import com.amee.service.data.DataService;
 import org.apache.commons.lang.StringUtils;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.json.JSONException;
 import org.json.JSONObject;
 import org.restlet.Context;
 import org.restlet.data.Form;
 import org.restlet.data.Request;
 import org.restlet.data.Response;
 import org.restlet.resource.Representation;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.context.annotation.Scope;
 import org.springframework.stereotype.Component;
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 //TODO - move to builder model
 @Component
 @Scope("prototype")
 public class DataItemResource extends BaseDataResource implements Serializable {
 
     private final Log log = LogFactory.getLog(getClass());
 
     @Autowired
     private DataService dataService;
 
     @Autowired
     private CalculationService calculationService;
 
     private List<Choice> parameters = new ArrayList<Choice>();
 
     @Override
     public void initialise(Context context, Request request, Response response) {
         super.initialise(context, request, response);
         Form query = request.getResourceRef().getQueryAsForm();
         setDataCategory(request.getAttributes().get("categoryUid").toString());
         setDataItemByPathOrUid(request.getAttributes().get("itemPath").toString());
         for (String key : query.getNames()) {
             parameters.add(new Choice(key, query.getValues(key)));
         }
     }
 
     @Override
     public boolean isValid() {
         return super.isValid() &&
                 (getDataItem() != null) &&
                 (getDataCategory() != null) &&
                 getDataItem().getDataCategory().equals(getDataCategory()) &&
                 getDataItem().getEnvironment().equals(environment);
     }
 
     @Override
     public String getTemplatePath() {
         return getAPIVersion() + "/" + DataConstants.VIEW_DATA_ITEM;
     }
 
     @Override
     public Map<String, Object> getTemplateValues() {
         DataItem dataItem = getDataItem();
         Choices userValueChoices = dataService.getUserValueChoices(dataItem, getAPIVersion());
         userValueChoices.merge(parameters);
         Map<String, Object> values = super.getTemplateValues();
         values.put("browser", dataBrowser);
         values.put("dataItem", dataItem);
         values.put("node", dataItem);
         values.put("userValueChoices", userValueChoices);
         values.put("amountPerMonth", calculationService.calculate(dataItem, userValueChoices, getAPIVersion()));
         return values;
     }
 
     @Override
     public JSONObject getJSONObject() throws JSONException {
         DataItem dataItem = getDataItem();
         Choices userValueChoices = dataService.getUserValueChoices(dataItem, getAPIVersion());
         userValueChoices.merge(parameters);
         JSONObject obj = new JSONObject();
         obj.put("dataItem", dataItem.getJSONObject(true));
         obj.put("path", pathItem.getFullPath());
         obj.put("userValueChoices", userValueChoices.getJSONObject());
         obj.put("amountPerMonth", calculationService.calculate(dataItem, userValueChoices, getAPIVersion()));
         return obj;
     }
 
     @Override
     public Element getElement(Document document) {
         DataItem dataItem = getDataItem();
         Choices userValueChoices = dataService.getUserValueChoices(dataItem, getAPIVersion());
         userValueChoices.merge(parameters);
         Element element = document.createElement("DataItemResource");
         element.appendChild(dataItem.getElement(document, true));
         element.appendChild(APIUtils.getElement(document, "Path", pathItem.getFullPath()));
         element.appendChild(userValueChoices.getElement(document));
         element.appendChild(APIUtils.getElement(document, "AmountPerMonth",
                 calculationService.calculate(dataItem, userValueChoices, getAPIVersion()).toString()));
         return element;
     }
 
     @Override
     public void handleGet() {
         log.debug("handleGet");
         if (dataBrowser.getDataItemActions().isAllowView()) {
             super.handleGet();
         } else {
             notAuthorized();
         }
     }
 
     @Override
     public boolean allowPut() {
         return true;
     }
 
     @Override
     public void storeRepresentation(Representation entity) {
         log.debug("storeRepresentation()");
         if (dataBrowser.getDataItemActions().isAllowModify()) {
             Form form = getForm();
             DataItem dataItem = getDataItem();
             Set<String> names = form.getNames();
 
             // are we updating this DataItem?
             if (names.contains("name")) {
                 dataItem.setName(form.getFirstValue("name"));
             }
 
             if (names.contains("path")) {
                 dataItem.setPath(form.getFirstValue("path"));
             }
 
             // update 'startDate' value
             if (names.contains("startDate")) {
                 dataItem.setStartDate(new StartEndDate(form.getFirstValue("startDate")));
             }
 
             // update 'endDate' value
             if (names.contains("endDate")) {
                 if (StringUtils.isNotBlank(form.getFirstValue("endDate"))) {
                     dataItem.setEndDate(new StartEndDate(form.getFirstValue("endDate")));
                 } else {
                     dataItem.setEndDate(null);
                 }
             } else {
                 if (form.getNames().contains("duration")) {
                     StartEndDate endDate = dataItem.getStartDate().plus(form.getFirstValue("duration"));
                     dataItem.setEndDate(endDate);
                 }
             }
 
             if (dataItem.getEndDate() != null &&
                     dataItem.getEndDate().before(dataItem.getStartDate())) {
                 badRequest();
                 return;
             }
 
             // update ItemValues if supplied
             ItemValueMap itemValues = dataItem.getItemValuesMap();
             for (String name : form.getNames()) {
                 ItemValue itemValue = itemValues.get(name);
                 if (itemValue != null) {
                     itemValue.setValue(form.getFirstValue(name));
                 }
             }
 
             // clear caches
             dataService.clearCaches(dataItem.getDataCategory());
            successfulPut(getParentPath() + "/" + dataItem.getResolvedPath());
         } else {
             notAuthorized();
         }
     }
 
     @Override
     public boolean allowDelete() {
         return true;
     }
 
     @Override
     public void removeRepresentations() {
         log.debug("removeRepresentations()");
         if (dataBrowser.getDataItemActions().isAllowDelete()) {
             DataItem dataItem = getDataItem();
             dataService.clearCaches(dataItem.getDataCategory());
             dataService.remove(dataItem);
             successfulDelete(pathItem.getParent().getFullPath());
         } else {
             notAuthorized();
         }
     }
 }
