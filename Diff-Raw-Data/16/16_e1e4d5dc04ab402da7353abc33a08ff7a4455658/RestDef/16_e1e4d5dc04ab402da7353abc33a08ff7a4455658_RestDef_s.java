 package com.usavich.rest.common;
 
 /**
  * Created with IntelliJ IDEA.
  * User: LeonLu
  * Date: 3/7/13
  * Time: 2:23 PM
  * To change this template use File | Settings | File Templates.
  */
 public interface RestDef {
     static final String PARAM_USER_EMAIL = "userEmail";
     static final String PARAM_PASSWORD = "password";
     static final String PARAM_USER_ID = "userId";
     static final String PARAM_CHECK_USER_UUID = "checkUuid";
     static final String PARAM_PLATFORM = "platform";
     static final String PARAM_LAST_UPDATE_TIME = "lastUpdateTime";
     static final String PARAM_MISSION_ID = "missionId";
    static final String PARAM_PACKAGE_ID = "packageId";
     static final String PARAM_PRODUCT_ID = "productId";
 
     static final String PARAM_PAGE_NUMBER = "pageNo";
     static final String PARAM_PAGE_SIZE = "pageSize";
 
     static final String HEADER_PARAM_MISSION_TYPE = "X-MISSION-TYPE";
     static final String HEADER_PARAM_KEY = "X-CLIENT-KEY";
 
     static final int defaultPageNo=1;
     static final int defaultPageSize=100;
 }
