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
 
 package com.orangeleap.tangerine.json.controller;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import javax.annotation.Resource;
 import javax.servlet.http.HttpServletRequest;
 
 import org.apache.commons.logging.Log;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.ModelMap;
 import org.springframework.web.bind.annotation.RequestMapping;
 
 import com.orangeleap.tangerine.domain.PostBatch;
 import com.orangeleap.tangerine.service.PostBatchService;
 import com.orangeleap.tangerine.util.OLLogger;
 import com.orangeleap.tangerine.web.common.PaginatedResult;
 import com.orangeleap.tangerine.web.common.SortInfo;
 
 /**
  * This controller handles JSON requests for populating
  * the grid of gifts.
  *
  * @version 1.0
  */
 @Controller
 public class PostbatchGiftListController {
 
     /**
      * Logger for this class and subclasses
      */
     protected final Log logger = OLLogger.getLog(getClass());
 
     private final static Map<String, Object> GIFT_NAME_MAP = new HashMap<String, Object>();
     private final static Map<String, Object> ADJUSTED_GIFT_NAME_MAP = new HashMap<String, Object>();
 
     static {
     	GIFT_NAME_MAP.put("id", "g.GIFT_ID");
     	GIFT_NAME_MAP.put("createdate", "g.CREATE_DATE");
     	GIFT_NAME_MAP.put("donationdate", "g.DONATION_DATE");
     	GIFT_NAME_MAP.put("amount", "g.AMOUNT");
     	GIFT_NAME_MAP.put("currencycode", "g.CURRENCY_CODE");
     	GIFT_NAME_MAP.put("paymenttype", "g.PAYMENT_TYPE");
     	GIFT_NAME_MAP.put("status", "g.GIFT_STATUS");
     }
 
     @Resource(name = "postBatchService")
     private PostBatchService postBatchService;
 
     @SuppressWarnings("unchecked")
     @RequestMapping("/postbatchGiftList.json")
     public ModelMap getPostbatchGiftList(HttpServletRequest request, SortInfo sortInfo) {
     	
     	List<Map> rows = new ArrayList<Map>();
     	
     	try {
 
 	        String sid = request.getParameter("id");
	        if (sid == null || sid.trim().length() == 0) return new ModelMap("rows", rows);
 	        long postbatchId = Long.valueOf(sid);
 	    	PostBatch postbatch = postBatchService.readBatch(postbatchId);
 	    	Map namemap = postbatch.getEntity().equals("gift")?GIFT_NAME_MAP:ADJUSTED_GIFT_NAME_MAP;
 	    	
 	
 	        // if we're not getting back a valid column name, possible SQL injection,
 	        // so send back an empty list.
 	        if (!sortInfo.validateSortField(namemap.keySet())) {
	            return new ModelMap("rows", rows);
 	        }
 	
 	        // set the sort to the valid column name, based on the map
 	        sortInfo.setSort((String) namemap.get(sortInfo.getSort()));
 	
 	        PaginatedResult result = postBatchService.getBatchSelectionList(postbatchId, sortInfo);
	        ModelMap map = new ModelMap("rows", result.getRows());
 	        map.put("totalRows", result.getRowCount());
 	        return map;
         
     	} catch (Exception e) {
     		logger.error(e);
    		return new ModelMap("rows", rows);
     	}
     }
 }
