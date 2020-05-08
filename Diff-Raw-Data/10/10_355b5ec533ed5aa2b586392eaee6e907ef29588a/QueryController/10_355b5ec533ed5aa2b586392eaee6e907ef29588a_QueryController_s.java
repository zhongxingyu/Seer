 package com.abudko.reseller.huuto.mvc;
 
 import java.io.UnsupportedEncodingException;
 import java.lang.reflect.InvocationTargetException;
 import java.net.URISyntaxException;
 import java.util.List;
 
 import javax.annotation.Resource;
 
 import org.apache.commons.beanutils.BeanUtils;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.Model;
 import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
 
import com.abudko.reseller.huuto.query.enumeration.*;
 import com.abudko.reseller.huuto.query.html.list.QueryListResponse;
 import com.abudko.reseller.huuto.query.params.QueryParams;
 import com.abudko.reseller.huuto.query.service.QueryService;
 
 @Controller
 public class QueryController {
 
     @Resource
     private QueryService queryService;
 
     @RequestMapping(value = "/items/search", method = RequestMethod.GET)
     public String search(@RequestParam(required = false) MultiValueMap<String, String> params, Model model)
             throws IllegalAccessException, InvocationTargetException, UnsupportedEncodingException, URISyntaxException {
         QueryParams queryParams = getQueryParamsFrom(params);
         List<QueryListResponse> responses = queryService.search(queryParams);
         model.addAttribute("items", responses);
         return "items";
     }
 
     private QueryParams getQueryParamsFrom(MultiValueMap<String, String> params) throws IllegalAccessException,
             InvocationTargetException {
         QueryParams queryParams = new QueryParams();
         BeanUtils.populate(queryParams.getSimpleParams(), params);
         populateEnums(queryParams, params);
         return queryParams;
     }
 
     private void populateEnums(QueryParams queryParams, MultiValueMap<String, String> params) {
         List<String> sellStyles = params.get("sellStyle");
         if (sellStyles != null) {
             String sellStyle = sellStyles.get(0);
             queryParams.setSellStyle(SellStyle.valueOf(sellStyle));
         }
 
         List<String> statuses = params.get("status");
         if (statuses != null) {
             String status = statuses.get(0);
             queryParams.setStatus(Status.valueOf(status));
         }
 
         List<String> classifications = params.get("classification");
         if (classifications != null) {
             String classification = classifications.get(0);
             queryParams.setClassification(Classification.valueOf(classification));
         }
 
         List<String> sellerTypes = params.get("sellerType");
         if (sellerTypes != null) {
             String sellerType = sellerTypes.get(0);
             queryParams.setSellerType(SellerType.valueOf(sellerType));
         }
     }
 }
