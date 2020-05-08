 package com.pms.service.controller;
 
 import java.io.IOException;
 import java.util.Enumeration;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.apache.logging.log4j.LogManager;
 import org.apache.logging.log4j.Logger;
 
 import com.google.gson.Gson;
 import com.google.gson.JsonSyntaxException;
 import com.pms.service.cfg.ConfigurationManager;
 import com.pms.service.dbhelper.DBQuery;
 import com.pms.service.dbhelper.DBQueryOpertion;
 import com.pms.service.exception.ApiResponseException;
 import com.pms.service.mockbean.ApiConstants;
 import com.pms.service.util.ApiThreadLocal;
 import com.pms.service.util.ApiUtil;
 import com.pms.service.util.status.ResponseCodeConstants;
 import com.pms.service.util.status.ResponseStatus;
 
 public abstract class AbstractController {
     private static Logger logger = LogManager.getLogger(AbstractController.class);
 
     @SuppressWarnings("unchecked")
     protected HashMap<String, Object> parserJsonParameters(HttpServletRequest request, boolean emptyParameter) {
         HashMap<String, Object> parametersMap = parserParameters(request, emptyParameter);
 
         
         if (parametersMap.get("models") != null) {
             String v = parametersMap.get("models").toString();
             if (v.startsWith("[")) {
                 v = v.substring(1);
             }
 
             if (v.endsWith("]")) {
                 v = v.substring(0, v.lastIndexOf("]"));
             }
             parametersMap = new Gson().fromJson(v, HashMap.class);
         }
 
         if (parametersMap.get("_id") != null) {
             if (ApiUtil.isEmpty(parametersMap.get("_id"))) {
                 parametersMap.remove("_id");
             }
         }
         logger.debug(String.format("--------------Client post parameters for path [%s] is [%s]", request.getPathInfo(), parametersMap));
 
         return parametersMap;
 
     }
     
     @SuppressWarnings("unchecked")
     protected HashMap<String, Object> parserListJsonParameters(HttpServletRequest request, boolean emptyParameter) {
         HashMap<String, Object> parametersMap = parserParameters(request, emptyParameter);
 
         
         if (parametersMap.get("models") != null) {
             String v = parametersMap.get("models").toString();     
             parametersMap.put(ApiConstants.RESULTS_DATA, new Gson().fromJson(v, List.class));
             parametersMap.remove("models");
         }
 
         if (parametersMap.get("_id") != null) {
             if (ApiUtil.isEmpty(parametersMap.get("_id"))) {
                 parametersMap.remove("_id");
             }
         }
         logger.debug(String.format("--------------Client post parameters for path [%s] is [%s]", request.getPathInfo(), parametersMap));
 
         return parametersMap;
     }
 
     private HashMap<String, Object> parserParameters(HttpServletRequest request, boolean emptyParameter) {
         HashMap<String, Object> parametersMap = new HashMap<String, Object>();
 
         String parameters = request.getParameter(ApiConstants.JSON_PARAMETERS_LABEL);
 
         int filterLength = 0;
         if (parameters != null) {
             try {
                 parametersMap = new Gson().fromJson(parameters, HashMap.class);
             } catch (JsonSyntaxException e) {
                 // TODO
             }
 
         }
         Enumeration<?> parameterNames = request.getParameterNames();
         while (parameterNames.hasMoreElements()) {
             String pName = parameterNames.nextElement().toString();
 
             if (pName.toLowerCase().startsWith("filter[filters]".toLowerCase())) {
                 filterLength++;
             } else {
                 parametersMap.put(pName, request.getParameter(pName).trim());
             }
         }
 
         if (filterLength > 0) {
             // 每三个为一组
             int filters = (int) filterLength / 3;
             for (int i = 0; i < filters; i++) {
                 String key = request.getParameter("filter[filters][" + i + "][field]");
                 String operator = request.getParameter("filter[filters][" + i + "][operator]");
                 String value = request.getParameter("filter[filters][" + i + "][value]");
                 parametersMap.put(key, new DBQuery(DBQueryOpertion.getOperation(operator), value));
             }
         }
         if (!emptyParameter && (parametersMap == null || parametersMap.isEmpty())) {
             throw new ApiResponseException(String.format("Parameters required for path [%s]", request.getPathInfo()), ResponseCodeConstants.PARAMETERS_EMPTY.toString());
         }
 
         parametersMap.remove("_");
         parametersMap.remove("callback");
         parametersMap.remove("page");
         parametersMap.remove("take");
 
         if (parametersMap.get(ApiConstants.PAGE_SIZE) != null) {
             parametersMap.put(ApiConstants.LIMIT, parametersMap.get(ApiConstants.PAGE_SIZE));
             parametersMap.remove(ApiConstants.PAGE_SIZE);
         }
 
         if (parametersMap.get(ApiConstants.SKIP) != null) {
             parametersMap.put(ApiConstants.LIMIT_START, parametersMap.get(ApiConstants.SKIP));
             parametersMap.remove(ApiConstants.SKIP);
         }
 
         parametersMap.remove("filter[logic]");
         parametersMap.remove("filter");
 
         // FIXME: only support sort by one column
         if (parametersMap.get("sort[0][field]") != null) {
             Map<String, Object> orderBy = new HashMap<String, Object>();
             int orderDir = ApiConstants.DB_QUERY_ORDER_BY_ASC;
             if (parametersMap.get("sort[0][dir]") != null) {
                 if (!parametersMap.get("sort[0][dir]").toString().equalsIgnoreCase("asc")) {
                     orderDir = ApiConstants.DB_QUERY_ORDER_BY_DESC;
                 }
             }
             orderBy.put(parametersMap.get("sort[0][field]").toString(), orderDir);
 
             parametersMap.remove("sort[0][field]");
             parametersMap.remove("sort[0][dir]");
 
             parametersMap.put(ApiConstants.DB_QUERY_ORDER_BY, orderBy);
 
         }
         
         if (parametersMap.get(ApiConstants.MY_TASKS) != null) {
             ApiThreadLocal.set(ApiConstants.MY_TASKS, parametersMap.get(ApiConstants.MY_TASKS));
         }
         parametersMap.remove(ApiConstants.MY_TASKS);
         parametersMap.remove("_defaultId");
         parametersMap.remove("defaults");
         return parametersMap;
     }
     
 
     protected void responseWithData(Map<String, Object> data, HttpServletRequest request, HttpServletResponse response) {
         responseMsg(data, ResponseStatus.SUCCESS, request, response, null);
     }
     
     protected void responseWithData(Map<String, Object> data, HttpServletRequest request, HttpServletResponse response, String msgKey) {
         responseMsg(data, ResponseStatus.SUCCESS, request, response, msgKey);
     }
 
     protected void responseWithKeyValue(String key, String value, HttpServletRequest request, HttpServletResponse response) {
         if (key == null) {
             responseWithData(null, request, response);
         } else {
             Map<String, Object> temp = new HashMap<String, Object>();
             temp.put(key, value);
             responseWithData(temp, request, response);
         }
     }
     
     protected void forceLogin(HttpServletRequest request, HttpServletResponse response){
         response.setContentType("text/plain;charset=UTF-8");
         response.setContentType("application/x-javascript;charset=UTF-8");
         ((HttpServletResponse) response).addHeader("Accept-Encoding", "gzip, deflate");
         try {
             response.getWriter().write("forceLogin();");
         } catch (IOException e) {
             logger.fatal("Write response data to client failed!", e);
         }
     }
 
 
     /**
      * This function will return JSON data to Client
      * 
      * 
      * @param data
      *            data to return to client
      * @param dataKey
      *            if set dataKey, the JSON format use dataKey as the JSON key,
      *            data as it's value, and both the dataKey and "status" key are
      *            child of the JSON root node. If not set dataKey, the data and
      *            the "status" node are both the child of the JSON root node
      * @param status
      *            0:FAIL, 1: SUCCESS
      * @return
      */
     private void responseMsg(Map<String, Object> data, ResponseStatus status, HttpServletRequest request, HttpServletResponse response, String msgKey) {
 
         response.setContentType("text/plain;charset=UTF-8");
         response.addHeader("Accept-Encoding", "gzip, deflate");
 
         String jsonReturn = new Gson().toJson(data);
         String displayMsg = null;
         String callback = request.getParameter("callback");
         
         
         if(request.getParameter("mycallback") != null){
             callback = request.getParameter("mycallback");
         }
         if (callback != null) {
             response.setContentType("application/x-javascript;charset=UTF-8");
 
             if (status == ResponseStatus.FAIL) {
                 jsonReturn = "displayMsg(" + jsonReturn + ");";
             } else {
 
                 if (msgKey != null) {
                     displayMsg = "displayMsg({\"msg\": \"" + ConfigurationManager.getSystemMessage(msgKey) + "\"});";
                 }
 
                 if (data != null && data instanceof Map) {
                     Object responseMap = null; //data.get("data");
                     
                     //if(responseMap == null){
                         responseMap = data;
                     //}
                         //返回
                         jsonReturn = callback + "(" + new Gson().toJson(responseMap) + ");";
                    
                 } else {
                     //不返回任何数据           
                         jsonReturn =  callback + "([]);";              
                 }
 
             }
 
         }
         try {
             response.getWriter().write(jsonReturn);
         } catch (IOException e) {
             logger.fatal("Write response data to client failed!", e);
         }
 
     }
 
     protected void responseServerError(Throwable throwable, HttpServletRequest request, HttpServletResponse response) {
         Map<String, Object> temp = new HashMap<String, Object>();
         temp.put("status", ResponseStatus.FAIL.toString());
 
         if (throwable instanceof ApiResponseException) {
             ApiResponseException apiException = (ApiResponseException) throwable;
             temp.put("msg", apiException.getTipMsg());
             logger.error(String.format(" =========== API Validation failed with tip msg [%s] and log msg [%s] ", apiException.getTipMsg(), apiException.getMessage()));
 
         } else {
             temp.put("msg", "System Error");
         }
         responseMsg(temp, ResponseStatus.FAIL, request, response, null);
 
     }
     
 
 
 }
