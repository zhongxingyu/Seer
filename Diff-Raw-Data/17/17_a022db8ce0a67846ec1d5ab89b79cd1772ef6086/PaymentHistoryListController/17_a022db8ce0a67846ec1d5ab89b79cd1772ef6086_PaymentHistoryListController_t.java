 package com.orangeleap.tangerine.json.controller;
 
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import javax.annotation.Resource;
 import javax.servlet.http.HttpServletRequest;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.apache.commons.validator.GenericValidator;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.ModelMap;
 import org.springframework.web.bind.annotation.RequestMapping;
 
 import com.orangeleap.tangerine.domain.PaymentHistory;
 import com.orangeleap.tangerine.service.PaymentHistoryService;
 import com.orangeleap.tangerine.web.common.PaginatedResult;
 import com.orangeleap.tangerine.web.common.SortInfo;
 
 /**
  * This controller handles JSON requests for populating
  * the grid of payment history.
  * @version 1.0
  */
 @Controller
 public class PaymentHistoryListController {
 
     /** Logger for this class and subclasses */
     protected final Log logger = LogFactory.getLog(getClass());
 
     private final static Map<String, Object> NAME_MAP = new HashMap<String, Object>();
 
     static {
         NAME_MAP.put("id", "phis.PAYMENT_HISTORY_ID");
         NAME_MAP.put("date", "phis.TRANSACTION_DATE");
        NAME_MAP.put("personid", "phis.PERSON_ID");
         NAME_MAP.put("type", "phis.PAYMENT_HISTORY_TYPE");
        NAME_MAP.put("paymenttype", "phis.PAYMENT_TYPE");
         NAME_MAP.put("description", "phis.PAYMENT_DESC");
         NAME_MAP.put("amount", "phis.AMOUNT");
        NAME_MAP.put("currencycode", "phis.CURRENCY_CODE");
     }
     
     private Map<String,Object> paymentHistoryToMap(PaymentHistory ph) {
 
         DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
 
         Map<String, Object> map = new HashMap<String, Object>();
         map.put("id", ph.getId());
         map.put("date", formatter.format(ph.getTransactionDate()) );
        map.put("personid", ph.getPerson().getId());
         map.put("type", ph.getPaymentHistoryType().name());
        map.put("paymenttype", ph.getPaymentType());
         map.put("description", ExtUtil.scrub(ph.getDescription()));
         map.put("amount", ph.getAmount());
        map.put("currencycode", ph.getCurrencyCode());
 
         return map;
 
     }
     
     @Resource(name="paymentHistoryService")
     private PaymentHistoryService paymentHistoryService;
 
     @SuppressWarnings("unchecked")
     @RequestMapping("/paymentHistoryList.json")
     public ModelMap getPaymentHistory(HttpServletRequest request, SortInfo sortInfo) {
 
         List<Map> rows = new ArrayList<Map>();
 
         // if we're not getting back a valid column name, possible SQL injection,
         // so send back an empty list.
         if(!sortInfo.validateSortField(NAME_MAP.keySet())) {
             logger.warn("getgetPaymentHistory called with invalid sort column: [" + sortInfo.getSort() + "]");
             return new ModelMap("rows", rows);
         }
 
         // set the sort to the valid column name, based on the map
         sortInfo.setSort( (String) NAME_MAP.get(sortInfo.getSort()) );
 
         String personId = request.getParameter("personId");
         PaginatedResult result = null;
         if (GenericValidator.isBlankOrNull(personId)) {
             result = paymentHistoryService.readPaymentHistoryBySite(sortInfo); 
         } else {
             result = paymentHistoryService.readPaymentHistory(Long.valueOf(personId), sortInfo); 
         }
 
         List<PaymentHistory> list = result.getRows();
 
         for(PaymentHistory ph : list) {
             rows.add( paymentHistoryToMap(ph) );
         }
 
         ModelMap map = new ModelMap("rows", rows);
         map.put("totalRows", result.getRowCount());
         return map;
     }
     
 
 
 }
