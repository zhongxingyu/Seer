 package com.pms.service.util;
 
 import java.math.BigDecimal;
 import java.text.DecimalFormat;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.LinkedHashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.Random;
 
 import org.apache.logging.log4j.LogManager;
 import org.apache.logging.log4j.Logger;
 
 import com.pms.service.exception.ApiResponseException;
 import com.pms.service.mockbean.ApiConstants;
 import com.pms.service.service.impl.SalesContractServiceImpl;
 import com.pms.service.util.status.ResponseCodeConstants;
 
 public class ApiUtil {
 	private static Logger logger = LogManager.getLogger(SalesContractServiceImpl.class);
 
 	public static void main(String[] arg){
 		System.out.println(isEmpty(1));
 	}
 
     public static boolean limitKeyExists(String[] list, String checked) {
 
         if (list == null) {
             return true;
         }
         for (String value : list) {
 
             if (value.equals(checked)) {
                 return true;
             }
         }
 
         return false;
 
     }
 
     public static boolean isValid(Object param){
     	return !isEmpty(param);
     }
     public static boolean isEmpty(Object param) {
 
         if (param == null) {
             return true;
         }
 
         if (param instanceof Map) {
             return ((Map) param).isEmpty();
         }
         if (param instanceof List) {
             return ((List) param).isEmpty();
         }
         String parameter = param.toString();
         if (parameter.trim().length() == 0) {
             return true;
         }
 
         if ("null".equalsIgnoreCase(parameter)) {
             return true;
         }
         if ("".equalsIgnoreCase(parameter)) {
             return true;
         }
         return false;
     }
  
     public static Integer getIntegerParam(Map<String, Object> params, String key, int defaultValue) {
         Object value = null;
         if (params != null){
             value = params.get(key);
         }
         Integer in = getInteger(value);
         in = (in == null) ? defaultValue : in;
         return in;
     }
     
     public static Integer getInteger(Map<String, Object> params, String key, int defaultValue) {
         Object value = null;
         if (params != null){
             value = params.get(key);
         }
         Integer in = null;
         if (value != null) {
             try {
             	in = Integer.valueOf(String.valueOf(value));
             } catch (Exception e) {
                return getInteger(value);
             }
         }
         in = (in == null) ? defaultValue : in;
         return in;
     }
 
     //转换 String==>double（保留两位小数）
     public static Double getDouble(String value) {
         Double in = 0.00;
         if (value != null && !value.trim().isEmpty()) {
             try {
             	DecimalFormat df = new DecimalFormat("#.00");
             	in = Double.valueOf(value);
             	in = Double.valueOf(df.format(in));
             } catch (Exception e) {
                logger.error(String.format("Double parameter illegal [%s]", value));
             }
         }        
         return in;
     }
 
     //转换 String==>double（保留4位小数）
     public static Double getDoubleMultiply100(String value) {
         Double in = 0.00;
         if (value != null && !value.trim().isEmpty()) {
             try {
             	DecimalFormat df = new DecimalFormat("#.00");
             	in = Double.valueOf(value);
             	in = in*100;
             	in = Double.valueOf(df.format(in));
             } catch (Exception e) {
                 throw new ApiResponseException(String.format("Double parameter illegal [%s]", value),
                         ResponseCodeConstants.NUMBER_PARAMETER_ILLEGAL);
             }
         }        
         return in;
     }
  
     public static Double getDouble(String value,double defaultValue) {
         Double in = null;
         if (value != null && !value.isEmpty()) {
             try {
             	in = Double.valueOf(value);
             } catch (Exception e) {
                 throw new ApiResponseException(String.format("Double parameter illegal [%s]", value), ResponseCodeConstants.NUMBER_PARAMETER_ILLEGAL);
             }
         }
         if(in == null) in = defaultValue;
         return in;
     }
     
     public static Double getDouble(Map<String, Object> params, String key) {
         Object value = null;
         if (params != null){
             value = params.get(key);
         }
         Double in = null;
         if (!ApiUtil.isEmpty(value)) {
             try {
             	in = Double.valueOf(String.valueOf(value));
             } catch (Exception e) {
                 throw new ApiResponseException(String.format("Integer parameter illegal [%s]", value),
                         ResponseCodeConstants.NUMBER_PARAMETER_ILLEGAL);
             }
         }
         return in;    	
     }
     public static Double getDouble(Map<String, Object> params, String key, double defaultValue) {
         Double in = getDouble(params,key);
         if(in == null) in = defaultValue;
         return in;
     }
     
     public static Integer getIntegerParam(Map<String, Object> params, String key) {
         Object value = null;
         if (params != null){
             value = params.get(key);
         } 
         return getInteger(value);
     }
 
     public  static Integer getInteger(Object value) {
         Integer result = null;
 
         if (value != null) {
             try {
                 result = (int) Float.parseFloat(String.valueOf(value));
             } catch (NumberFormatException e) {
                 try {
                     result = Integer.parseInt(String.valueOf(value));
                 } catch (NumberFormatException e1) {
 
                     logger.error(String.format("Integer parameter illegal [%s]", value));
                 }
             }
 
         }
 
         return result;
     }
 
     public  static Integer getInteger(Object value,int defaultValue) {
         Integer result = null;
 
         if (value != null) {
             try {
                 result = (int) Float.parseFloat(String.valueOf(value));
             } catch (NumberFormatException e) {
                 try {
                     result = Integer.parseInt(String.valueOf(value));
                 } catch (NumberFormatException e1) {
                     throw new ApiResponseException(String.format("Integer parameter illegal [%s]", value),
                             ResponseCodeConstants.NUMBER_PARAMETER_ILLEGAL);
                 }
             }
 
         }
         if(result == null) result = defaultValue;
         return result;
     }
     
     public static Long getLongParam(Map<String, Object> params, String key){
     	Long result = null;
     	if (params.get(key) != null){
     		try {
 				result = Long.parseLong(String.valueOf(params.get(key)));
 			} catch (NumberFormatException e) {
 				throw new ApiResponseException(String.format("Long type parameter illegal [%s]", params), 
 						ResponseCodeConstants.NUMBER_PARAMETER_ILLEGAL);
 			}
     	}
     	
     	return result;
     }
     
     
     public static boolean isIllegalGender(Object gender) {
         if (gender == null) {
             return false;
         }
         Integer userGender = getInteger(gender);
    
         if (userGender.equals(0) || userGender.equals(1)) {
             return true;
         }
 
         return false;
 
     }
     
     public static Float getFloatParam(Map<String, Object> params, String key){
     	Float result = 0f;
     	Object value = params.get(key); 
     	if (!isEmpty(value)){
     		try {
 				result = Float.parseFloat(String.valueOf(params.get(key)));
 			} catch (NumberFormatException e) {
 				logger.error(String.format("Float type parameter illegal [%s][%s]", key, params.get(key)));
 			}
     	}
     	
     	return result;
     }
     
     public static Float getFloatParam(Object value){
     	Float result = 0f;
     	if (!isEmpty(value)){
     		try {
 				result = Float.parseFloat(String.valueOf(value));
 			} catch (NumberFormatException e) {
 				logger.error(String.format("Float type parameter illegal [%s]", value));
 			}
     	}
     	
     	return result;
     }
     
     public static Integer getRandomIntByCap(int cap){
     	Random random = new Random();
     	int num = random.nextInt(cap);
     	if (num == 0){
     		num++;
     	}
     	return num;
     }
     
     public static Map<String, String> sortLinkMap(Map<String, String> map){
         ArrayList<Entry<String,String>> list = new ArrayList<Entry<String,String>>(map.entrySet());   
           
         Collections.sort(list, new Comparator<Object>(){
             public int compare(Object e1, Object e2){   
                 int v1 = Integer.parseInt(((Entry<String,String>)e1).getValue().toString());   
                 int v2 = Integer.parseInt(((Entry)e2).getValue().toString());   
                 return v1-v2;   
             }   
         });   
         
         Map<String, String> sortResult = new LinkedHashMap<String, String>();
         for (Entry<String,String> e : list){
             if (Integer.parseInt(e.getValue()) > 0)
                 sortResult.put(e.getKey(), e.getValue());
         }
         
         return sortResult;
     }
     
     
     public static String connectValueWithMapList(List<Map<String, Object>> data, String key, String split) {
         StringBuffer sb = new StringBuffer();
         for (int i = 0; i < data.size(); i++) {
             String name = (String) data.get(i).get(key);
             if (i == 0) {
                 sb.append(name);
             } else {
                 sb.append(split).append(name);
             }
         }
 
         return sb.toString();
     }
     
     public static String connectValueWithObjectList(List<Object> data, String key, String split) {
         StringBuffer sb = new StringBuffer();
         for (int i = 0; i < data.size(); i++) {
             Map<String, Object> map = (Map<String, Object>)data.get(i);
             String name = (String) map.get(key);
             if (i == 0) {
                 sb.append(name);
             } else {
                 sb.append(split).append(name);
             }
         }
 
         return sb.toString();
     }
 
     
     public static List<String> getMongoIds(Map<String, Object> listResult) {
         List<Map<String, Object>> allIds = (List<Map<String, Object>>) listResult.get(ApiConstants.RESULTS_DATA);
         List<String> ids = new ArrayList<String>();
         for (Map map : allIds) {
             ids.add(map.get(ApiConstants.MONGO_ID).toString());
         }
         return ids;
     }
     
     
 
     public static void mergeMongoResult(Map<String, Object> result, Map<String, Object> addResult){
         List<Map<String, Object>> list = (List<Map<String, Object>>)result.get(ApiConstants.RESULTS_DATA);
         List<Map<String, Object>> listAdd = (List<Map<String, Object>>)addResult.get(ApiConstants.RESULTS_DATA);
         list.addAll(listAdd);
     }
     
     
     public static String formateDate(Date date, String patten) {
         SimpleDateFormat format = new SimpleDateFormat(patten);
         return format.format(date);
 
     }
     
     
 
     public static void updateDataValue(Map<String, Object> data) {
         List<String> floatFields = new ArrayList<String>();
         floatFields.add("qualityMoney");
         floatFields.add("contractAmount");
         floatFields.add("equipmentAmount");
         floatFields.add("serviceAmount");
         floatFields.add("estimateEqCost0");
         floatFields.add("estimateEqCost1");
         floatFields.add("estimateSubCost");
         floatFields.add("estimatePMCost");
         floatFields.add("estimateDeepDesignCost");
         floatFields.add("estimateDebugCost");
         floatFields.add("estimateOtherCost");
         floatFields.add("estimateTax");
         floatFields.add("totalEstimateCost");
         floatFields.add("estimateGrossProfit");
         floatFields.add("eqcostBasePrice");
         floatFields.add("eqcostSalesBasePrice");
         floatFields.add("eqcostLastBasePrice");
         floatFields.add("eqcostTotalAmount");
         floatFields.add("pbMoney");
         floatFields.add("addNewEqCostMoney");
         floatFields.add("estimateGrossProfit");
         
         
        
         if (ApiUtil.isValid(data)) {
             for (String key : data.keySet()) {
 
                 if (data.get(key) instanceof Map) {
                     updateDataValue((Map<String, Object>) data.get(key));
                 } else if (data.get(key) instanceof List) {
                     List<Object> objects = (List<Object>) data.get(key);
                     for(Object obj: objects){
                         if(obj instanceof Map){
                             updateDataValue((Map<String, Object>)obj);
                         }
                     }
 
                 } else {
                     if (floatFields.contains(key)) {
                         Float f = ApiUtil.getFloatParam(data, key);
                         BigDecimal b = new BigDecimal(f);
                         float f1 = b.setScale(2, BigDecimal.ROUND_HALF_UP).floatValue();
                         data.put(key, f1);
                     }
                 }
             }
 
         }
 
     }
 
 }
