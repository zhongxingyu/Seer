 package com.abudko.reseller.huuto.query.builder.csv;
 
 import java.lang.reflect.InvocationTargetException;
 import java.util.LinkedHashMap;
 import java.util.Map;
 
 import org.springframework.stereotype.Component;
 
 import com.abudko.reseller.huuto.query.builder.AbstractParamBuilder;
 import com.abudko.reseller.huuto.query.builder.ParamBuilderUtils;
 import com.abudko.reseller.huuto.query.params.SearchParams;
 
 @Component
 public class CsvParamBuilder extends AbstractParamBuilder {
 
     public String buildQuery(SearchParams params) throws IllegalAccessException, InvocationTargetException,
             NoSuchMethodException {
         Map<String, String> parameters = getParameters(params);
         return ParamBuilderUtils.buildRestfulUrl(parameters);
     }
 
     protected Map<String, String> getParameters(SearchParams params) {
         Map<String, String> result = new LinkedHashMap<String, String>();
 
         result.put("classification", params.getClassification());
         result.put("sellstyle", params.getSellstyle());
         result.put("status", params.getStatus());
         result.put("location", params.getLocation());
         result.put("biddernro", params.getBiddernro());
         result.put("zipcode", params.getZipcode());
         result.put("closingtime", params.getClosingtime());
         result.put("addtime", params.getAddtime());
         result.put("seller_type", params.getSeller_type());
         result.put("sellernro", params.getSellernro());
//        result.put("price_min", params.getPrice_min());
//        result.put("price_max", params.getPrice_max());
         result.put("words", params.getWords());
         result.put("category", params.getCategory());
         return result;
     }
 }
