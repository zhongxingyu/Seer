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
 
 import com.orangeleap.tangerine.domain.PostBatch;
 import com.orangeleap.tangerine.service.ConstituentService;
 import com.orangeleap.tangerine.service.PostBatchService;
 import com.orangeleap.tangerine.type.AccessType;
 import com.orangeleap.tangerine.type.PageType;
 import com.orangeleap.tangerine.util.StringConstants;
 import com.orangeleap.tangerine.web.common.SortInfo;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.ModelMap;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.util.WebUtils;
 
 import javax.annotation.Resource;
 import javax.servlet.http.HttpServletRequest;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Locale;
 import java.util.Map;
 
 @Controller
 public class BatchListController extends TangerineJsonListController {
 
     @Resource(name = "postBatchService")
     private PostBatchService postBatchService;
 
     @Resource(name = "constituentService")
     private ConstituentService constituentService;
 
     @SuppressWarnings("unchecked")
     public void checkAccess(HttpServletRequest request) {
         Map<String, AccessType> pageAccess = (Map<String, AccessType>) WebUtils.getSessionAttribute(request, "pageAccess");
         if (pageAccess.get(PageType.batch.getPageName()) != AccessType.ALLOWED) {
             throw new RuntimeException("You are not authorized to access this page"); // TODO: use invalid access exception and move to filter
         }
     }
 
     @SuppressWarnings("unchecked")
     @RequestMapping("/batchList.json")
     public ModelMap getBatchList(HttpServletRequest request, boolean showRanBatches, SortInfo sort) {
         List<Map<String, Object>> returnList = getPostBatchList(showRanBatches, sort, request.getLocale());
         ModelMap map = new ModelMap(StringConstants.ROWS, returnList);
         map.put(StringConstants.TOTAL_ROWS, returnList.size());
         return map;
     }
 
     private List<Map<String, Object>> getPostBatchList(boolean showRanBatches, SortInfo sort, Locale locale) {
         List<Map<String, Object>> returnList = new ArrayList<Map<String, Object>>();
 
     	List<PostBatch> batches = postBatchService.readBatches(showRanBatches, sort, locale);
     	for (PostBatch batch : batches) {
             Map<String, Object> map = new HashMap<String, Object>();
 
             map.put(StringConstants.ID, batch.getId());
             map.put("batchType", batch.getBatchType());
             map.put("batchDesc", batch.getBatchDesc());
             map.put("executed", batch.isExecuted());
             map.put("executedDate", batch.getExecutedDate());
             map.put("createDate", batch.getCreateDate());
            map.put("executedByUser", batch.getExecutedByUser());
             returnList.add(map);
     	}
     	return returnList;
     }
 }
