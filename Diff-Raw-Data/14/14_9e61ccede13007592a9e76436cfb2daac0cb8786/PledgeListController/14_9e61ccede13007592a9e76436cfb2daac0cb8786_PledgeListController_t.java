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
 
 import com.orangeleap.tangerine.domain.paymentInfo.Pledge;
 import com.orangeleap.tangerine.service.PledgeService;
 import com.orangeleap.tangerine.web.common.PaginatedResult;
 import com.orangeleap.tangerine.web.common.SortInfo;
 
 /**
  * This controller handles JSON requests for populating
  * the grid of pledges.
  * @version 1.0
  */
 @Controller
 public class PledgeListController {
 
     /** Logger for this class and subclasses */
     protected final Log logger = LogFactory.getLog(getClass());
 
     private final static Map<String, Object> NAME_MAP = new HashMap<String, Object>();
 
     static {
         NAME_MAP.put("id", "c.GIFT_ID");
         NAME_MAP.put("personId", "c.CONSTITUENT_ID");
         NAME_MAP.put("status", "c.PLEDGE_STATUS");
         NAME_MAP.put("amountpergift", "c.AMOUNT_PER_GIFT");
         NAME_MAP.put("amounttotal", "c.AMOUNT_TOTAL");
        NAME_MAP.put("amountpaid", "c.AMOUNT_PAID");
        NAME_MAP.put("amountremaining", "c.AMOUNT_REMAINING");
     }
     
     private Map<String,Object> pledgeToMap(Pledge c) {
 
         Map<String, Object> map = new HashMap<String, Object>();
         map.put("id", c.getId());
         map.put("personId", c.getPerson().getId());
         map.put("status", c.getPledgeStatus());
         map.put("amountpergift", c.getAmountPerGift());
         map.put("amounttotal", c.getAmountTotal());
         map.put("amountpaid", c.getAmountPaid());
         map.put("amountremaining", c.getAmountRemaining());
         
         return map;
 
     }
 
     @Resource(name="pledgeService")
     private PledgeService pledgeService;
 
     @SuppressWarnings("unchecked")
     @RequestMapping("/pledgeList.json")
     public ModelMap getPledgeList(HttpServletRequest request, SortInfo sortInfo) {
 
         List<Map> rows = new ArrayList<Map>();
 
         // if we're not getting back a valid column name, possible SQL injection,
         // so send back an empty list.
         if(!sortInfo.validateSortField(NAME_MAP.keySet())) {
             logger.warn("getPledgeList called with invalid sort column: [" + sortInfo.getSort() + "]");
             return new ModelMap("rows", rows);
         }
 
         // set the sort to the valid column name, based on the map
         sortInfo.setSort( (String) NAME_MAP.get(sortInfo.getSort()) );
 
         String personId = request.getParameter("personId");
         PaginatedResult result = pledgeService.readPaginatedPledgesByConstituentId(Long.valueOf(personId), sortInfo); 
 
         List<Pledge> list = result.getRows();
 
         for(Pledge g : list) {
             rows.add( pledgeToMap(g) );
         }
 
         ModelMap map = new ModelMap("rows", rows);
         map.put("totalRows", result.getRowCount());
         return map;
     }
     
 
 
 }
