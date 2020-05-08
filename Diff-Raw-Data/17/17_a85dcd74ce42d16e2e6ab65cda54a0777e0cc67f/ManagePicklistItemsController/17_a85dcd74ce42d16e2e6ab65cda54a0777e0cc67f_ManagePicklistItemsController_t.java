 /*
  * Copyright (c) 2009. Orange Leap Inc. Active Constituent
  * Relationship Management Platform.
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package com.orangeleap.tangerine.json.controller.admin;
 
 import com.orangeleap.tangerine.domain.customization.Picklist;
 import com.orangeleap.tangerine.domain.customization.PicklistItem;
 import com.orangeleap.tangerine.service.PicklistItemService;
 import com.orangeleap.tangerine.util.OLLogger;
 import com.orangeleap.tangerine.util.StringConstants;
 import net.sf.json.JSONArray;
 import net.sf.json.JSONSerializer;
 import org.apache.commons.beanutils.DynaBean;
 import org.apache.commons.lang.math.NumberUtils;
 import org.apache.commons.logging.Log;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.ModelMap;
 import org.springframework.util.StringUtils;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestParam;
 
 import javax.annotation.Resource;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.HashMap;
 
 @Controller
 public class ManagePicklistItemsController {
     protected final Log logger = OLLogger.getLog(getClass());
 
     @Resource(name = "picklistItemService")
     private PicklistItemService picklistItemService;
 
     @SuppressWarnings("unchecked")
     @RequestMapping("/managePicklistItems.json")
     public ModelMap managePicklistItems(@RequestParam("rows") String rows, @RequestParam("picklistNameId") String picklistNameId) {
         if (logger.isTraceEnabled()) {
             logger.trace("managePicklistItems:");
         }
         ModelMap map = new ModelMap();
         Picklist originalPicklist = picklistItemService.getPicklist(picklistNameId);
         if (originalPicklist == null) {
             throw new IllegalArgumentException("The picklist was not found for " + picklistNameId);
         }
 
         JSONArray jsonArray = JSONArray.fromObject(rows);
         List<DynaBean> beans = (List<DynaBean>) JSONSerializer.toJava(jsonArray);
         List<PicklistItem> newModifiedItems = new ArrayList<PicklistItem>();
         for (DynaBean bean : beans) {
             Object id = bean.get(StringConstants.ID);
             if (id != null) {
                 PicklistItem item;
                 if (NumberUtils.isDigits(id.toString()) && Long.parseLong(id.toString()) > 0) {
                     item = picklistItemService.getPicklistItem(new Long(id.toString()));
                     if (item != null && ! item.getPicklistId().equals(originalPicklist.getId())) {
                         throw new IllegalArgumentException("The picklist item does not belong to the specified picklist.");
                     }
                 }
                 else {
                     // assume a new picklist item
                     item = new PicklistItem();
                     item.setPicklistId(originalPicklist.getId());
                 }
                 populateItem(item, bean);
                 newModifiedItems.add(item);
             }
         }
         List<PicklistItem> modifiedPicklistItems = picklistItemService.getPicklist(picklistNameId).getPicklistItems();
         picklistItemService.removeInvalidItems(modifiedPicklistItems);
 
         replaceExistingItemWithModifiedNew(modifiedPicklistItems, newModifiedItems);
         picklistItemService.maintainPicklistItems(originalPicklist, modifiedPicklistItems);
 
         List<Map<String, Object>> returnList = new ArrayList<Map<String, Object>>();
         for (PicklistItem item : modifiedPicklistItems) {
             if (item != null) {
                 Map<String, Object> pMap = new HashMap<String, Object>();
                 pMap.put(StringConstants.ID, item.getId());
                 pMap.put("itemName", item.getItemName());
                 pMap.put("displayVal", item.getDefaultDisplayValue());
                 pMap.put("desc", item.getLongDescription());
                 pMap.put("detail", item.getDetail());
                 pMap.put("inactive", item.isInactive());
                 pMap.put("itemOrder", item.getItemOrder());
                 returnList.add(pMap);
             }
         }
         map.put("rows", returnList);
         map.put("totalRows", returnList.size());
         map.put("success", Boolean.TRUE.toString().toLowerCase());
         return map;
     }
 
     private void replaceExistingItemWithModifiedNew(List<PicklistItem> picklistItems, List<PicklistItem> newModifiedItems) {
         for (int i = 0; i < picklistItems.size(); i++) {
             PicklistItem item = picklistItems.get(i);
             Iterator<PicklistItem> iter = newModifiedItems.iterator();
             while (iter.hasNext()) {
                 PicklistItem modifiedItem = iter.next();
                 if (modifiedItem.getId() != null && modifiedItem.getId().equals(item.getId())) {
                     // these are the modified items - replace
                     picklistItems.set(i, modifiedItem);
                     iter.remove();
                     break;
                 }
             }
         }
         Iterator<PicklistItem> iter = newModifiedItems.iterator();
         while (iter.hasNext()) {
             PicklistItem newItem = iter.next();
             // these are the new items - add
             picklistItems.add(newItem);
             iter.remove();
         }
     }
 
     private void populateItem(PicklistItem item, DynaBean bean) {
         if (item != null) {
             if (bean.getDynaClass().getDynaProperty("displayVal") != null && bean.get("displayVal") != null &&
                     StringUtils.hasText(bean.get("displayVal").toString())) {
                item.setDefaultDisplayValue(escapeScriptTag(bean.get("displayVal").toString()));
             }
             else if (item.isNew()) {
                 throw new IllegalArgumentException("Default display value is a required field.");
             }
             if (bean.getDynaClass().getDynaProperty("desc") != null && bean.get("desc") != null) {
                item.setLongDescription(escapeScriptTag(bean.get("desc").toString()));
             }
             if (bean.getDynaClass().getDynaProperty("detail") != null && bean.get("detail") != null) {
                item.setDetail(escapeScriptTag(bean.get("detail").toString()));
             }
             if (bean.getDynaClass().getDynaProperty("inactive") != null && bean.get("inactive") != null) {
                 item.setInactive(Boolean.TRUE.toString().equalsIgnoreCase(bean.get("inactive").toString()));
             }
             if (bean.getDynaClass().getDynaProperty("itemOrder") != null && bean.get("itemOrder") != null && NumberUtils.isDigits(bean.get("itemOrder").toString()) &&
                     Integer.parseInt(bean.get("itemOrder").toString()) > -1) {
                 item.setItemOrder(new Integer(bean.get("itemOrder").toString()));
             }
             else if (item.getItemOrder() == null) {
                 item.setItemOrder(1);
             }
         }
     }

    private String escapeScriptTag(String val) {
        if (val != null && (val.toLowerCase().indexOf("<script") > -1)) {
            int index = val.toLowerCase().indexOf("<script");
            val = new StringBuilder(val.substring(0, index)).append("&lt;script").append(val.substring(index + 7)).toString();
        }
        return val;
    }
 }
