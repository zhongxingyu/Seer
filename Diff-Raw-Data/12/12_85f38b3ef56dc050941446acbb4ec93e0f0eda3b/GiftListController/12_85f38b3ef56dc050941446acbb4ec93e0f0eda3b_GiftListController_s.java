 package com.orangeleap.tangerine.json.controller;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import javax.annotation.Resource;
 import javax.servlet.http.HttpServletRequest;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.ModelMap;
 import org.springframework.web.bind.annotation.RequestMapping;
 
 import com.orangeleap.tangerine.service.GiftService;
 import com.orangeleap.tangerine.web.common.PaginatedResult;
 import com.orangeleap.tangerine.web.common.SortInfo;
 
 /**
  * This controller handles JSON requests for populating
  * the grid of gifts.
  * @version 1.0
  */
 @Controller
 public class GiftListController {
 
     /** Logger for this class and subclasses */
     protected final Log logger = LogFactory.getLog(getClass());
 
     private final static Map<String, Object> NAME_MAP = new HashMap<String, Object>();
 
     static {
         NAME_MAP.put("id", "id");
         NAME_MAP.put("date", "date");
        NAME_MAP.put("constituentId", "constituentId");
         NAME_MAP.put("amount", "amount");
        NAME_MAP.put("currencyCode", "currencyCode");
         NAME_MAP.put("type", "type");
         NAME_MAP.put("status", "status");
         NAME_MAP.put("comments", "comments");
        NAME_MAP.put("refNumber", "refNumber");
        NAME_MAP.put("authCode", "authCode");
        NAME_MAP.put("giftType", "giftType");
     }
     
     @Resource(name="giftService")
     private GiftService giftService;
 
     @SuppressWarnings("unchecked")
     @RequestMapping("/giftList.json")
     public ModelMap getGiftList(HttpServletRequest request, SortInfo sortInfo) {
         List<Map> rows = new ArrayList<Map>();
 
         // if we're not getting back a valid column name, possible SQL injection,
         // so send back an empty list.
         if (!sortInfo.validateSortField(NAME_MAP.keySet())) {
             logger.warn("getGiftList called with invalid sort column: [" + sortInfo.getSort() + "]");
             return new ModelMap("rows", rows);
         }
 
         // set the sort to the valid column name, based on the map
         sortInfo.setSort( (String) NAME_MAP.get(sortInfo.getSort()) );
 
         String personId = request.getParameter("personId");
         PaginatedResult result = giftService.readPaginatedGiftList(Long.valueOf(personId), sortInfo); 
 
         ModelMap map = new ModelMap("rows", result.getRows());
         map.put("totalRows", result.getRowCount());
         return map;
     }
 }
