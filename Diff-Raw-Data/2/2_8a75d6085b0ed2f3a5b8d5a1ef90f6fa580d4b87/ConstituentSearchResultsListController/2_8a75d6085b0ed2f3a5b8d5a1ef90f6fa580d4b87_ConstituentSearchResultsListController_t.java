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
 
 package com.orangeleap.tangerine.json.controller.list;
 
 import com.orangeleap.tangerine.domain.Constituent;
 import com.orangeleap.tangerine.domain.customization.SectionField;
 import com.orangeleap.tangerine.service.ConstituentService;
 import com.orangeleap.tangerine.web.common.SortInfo;
 import org.springframework.beans.BeanWrapper;
 import org.springframework.beans.PropertyAccessorFactory;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.ModelMap;
 import org.springframework.web.bind.annotation.RequestMapping;
 
 import javax.annotation.Resource;
 import javax.servlet.http.HttpServletRequest;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 
 @Controller
 public class ConstituentSearchResultsListController extends TangerineJsonSearchResultsController {
 
     @Resource(name = "constituentService")
     private ConstituentService constituentService;
     
     @SuppressWarnings("unchecked")
     @RequestMapping("/constituentSearch.json")
     public ModelMap searchConstituents(HttpServletRequest request, SortInfo sort) {
         List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
         List<SectionField> sectionFields = findSectionFields("constituentSearchResults");
 
         if (sort == null) {
             List<SectionField> filteredFields = pageCustomizationService.getFieldsExceptId(sectionFields);
             sort = new SortInfo(filteredFields.get(0).getFieldPropertyName(), "ASC", 0, 100);
         }
 
         BeanWrapper bw = PropertyAccessorFactory.forBeanPropertyAccess(new Constituent());
        List<Constituent> constituents = constituentService.searchConstituents(findSearchParameters(request, bw), sort, request.getLocale());
 
         addListFieldsToMap(request, sectionFields, constituents, list, false, false);
 
         ModelMap map = new ModelMap("rows", list);
         map.put("totalRows", constituents == null ? 0 : constituents.size());
         map.put("sortInfo", sort);
 
         return map;
     }
 }
