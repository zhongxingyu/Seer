 package com.trackstudio.component;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 
 public class FieldMap {
     private int order;
     private String altKey;
     private String filterKey;
     private String fieldKey;
     public static final int LAST=45;
     public static final HashMap<String, String> allFields = new HashMap<String, String>();
 
     
     public static String getFilterKeyByFieldKey(String field){
        if (field.startsWith("UDF_SORT")){
             return "UDF"+field.substring("UDF_SORT".length());
         }
         return allFields.get(field);
     }
 
    public FieldMap(int order,String altKey, String filterKey, String fieldKey) {
         this.order=order;
         this.altKey = altKey;
         this.filterKey = filterKey;
         this.fieldKey = fieldKey;
         if (fieldKey!=null && filterKey!=null ) allFields.put(fieldKey, filterKey);
     }
 
     public static FieldMap createUDF(String caption, String filterKey, String fieldKey){
        return new FieldMap(LAST+caption.hashCode(),caption, filterKey, fieldKey);
     }
 
     public String getAltKey() {
         return altKey;
     }
 
     public String getFilterKey() {
         return filterKey;
     }
 
     public String getFieldKey() {
         return fieldKey;
     }
 
 
     public int getOrder() {
         return order;
     }
 
     public boolean equals(Object obj) {
         if (this == obj) return true;
         if (!(obj instanceof FieldMap)) return false;
 
         FieldMap fieldMap = (FieldMap) obj;
 
         if (fieldKey != null && fieldMap.fieldKey != null && fieldKey.equals(fieldMap.fieldKey)) return true;
         if (filterKey != null && fieldMap.filterKey != null && filterKey.equals(fieldMap.filterKey)) return true;
         return altKey != null && fieldMap.altKey != null && altKey.equals(fieldMap.altKey);
     }
 
     public static final FieldMap TASK_NUMBER = new FieldMap(1, "TASK_NUMBER", "tasknumber", "task_number");
     public static final FieldMap TASK_CATEGORY = new FieldMap(10, "CATEGORY", "category", "task_category");
     public static final FieldMap TASK_STATUS = new FieldMap(11, "TASK_STATE", "status", "task_status");
     public static final FieldMap TASK_RESOLUTION = new FieldMap(12, "RESOLUTION", "resolution", "task_resolution");
     public static final FieldMap SUSER_NAME = new FieldMap(14, "SUBMITTER", "submitter", "suser_name");
     public static final FieldMap SUSER_STATUS = new FieldMap(15, "SUBMITTER_STATUS", "submitter_stat", "suser_status");
     public static final FieldMap HUSER_NAME = new FieldMap(16, "HANDLER", "handler", "huser_name");
     public static final FieldMap HUSER_STATUS = new FieldMap(17, "HANDLER_STATUS", "handler_stat", "huser_status");
     public static final FieldMap TASK_DEADLINE = new FieldMap(21, "DEADLINE", "deadline", "task_deadline");
     public static final FieldMap TASK_SUBMITDATE = new FieldMap(18, "SUBMIT_DATE", "submit_date", "task_submitdate");
     public static final FieldMap TASK_UPDATEDATE = new FieldMap(19, "UPDATE_DATE", "updated_date", "task_updatedate");
     public static final FieldMap TASK_CLOSEDATE = new FieldMap(20, "CLOSE_DATE", "close_date", "task_closedate");
     public static final FieldMap TASK_BUDGET = new FieldMap(22, "BUDGET", "budget", "task_budget");
     public static final FieldMap TASK_ABUDGET = new FieldMap(23, "ABUDGET", "abudget", "task_abudget");
     public static final FieldMap TASK_CHILDCOUNT = new FieldMap(24, "SUBTASKS_AMOUNT", "childcount", "task_childcount");
     public static final FieldMap TASK_MESSAGECOUNT = new FieldMap(25, "MESSAGES_AMOUNT", "message_count", "task_messagecount");
     public static final FieldMap TASK_PRIORITY = new FieldMap(13, "PRIORITY", "priority", "task_priority");
     public static final FieldMap TASK_SHORTNAME = new FieldMap(9, "ALIAS", "alias", "task_shortname");
     public static final FieldMap TASK_NAME = new FieldMap(8, "NAME", "taskname", "task_name");
     public static final FieldMap TASK_DESCRIPTION = new FieldMap(26, "DESCRIPTION", "task_description", "task_description");
     public static final FieldMap MSG_SUSER_NAME = new FieldMap(27, "MESSAGE_SUBMITTER", "msg_submitter", "msg_suser_name");
     public static final FieldMap MSG_SUBMITDATE = new FieldMap(28,"MESSAGE_DATE", "msg_date", "msg_submitdate");
     public static final FieldMap MSG_MSTATUS = new FieldMap(29,"MESSAGE_TYPE", "msg_type", "msg_mstatus");
     public static final FieldMap MSG_HUSER_NAME = new FieldMap(30, "MESSAGE_HANDLER", "msg_handler", "msg_huser_name");
     public static final FieldMap MSG_RESOLUTION = new FieldMap(31, "MESSAGE_RESOLUTION", "msg_resolution", "msg_resolution");
     public static final FieldMap MSG_ABUDGET = new FieldMap(32, "MESSAGE_ABUDGET", "msg_budget", "msg_budget");
     public static final FieldMap TEXT_MSG = new FieldMap(33, "MESSAGE_DESCRIPTION", "msg_text", "text_msg");
     public static final FieldMap SEARCH = new FieldMap(34, "KEYWORD", "search", "search");
     public static final FieldMap USER_ACTIVE = new FieldMap(41, "ACTIVE", "active", "user_enabled");
     public static final FieldMap USER_EXPIREDATE = new FieldMap(42, "EXPIRE_DATE", "expiredate", "user_expiredate");
     public static final FieldMap USER_STATUS = new FieldMap(35, "PRSTATUS", "prstatus", "user_status");
     public static final FieldMap USER_LOCALE = new FieldMap(39, "LOCALE", "locale", "user_locale");
     public static final FieldMap USER_TIMEZONE = new FieldMap(40, "TIME_ZONE", "timezone", "user_timezone");
     public static final FieldMap USER_LOGIN = new FieldMap(2, "LOGIN", "login", "user_login");
     public static final FieldMap USER_NAME = new FieldMap(3, "USER_NAME", "name", "user_name");
     public static final FieldMap FULLPATH = new FieldMap(6, "RELATIVE_PATH", "fullpath", "full_path");
     public static final FieldMap USER_COMPANY = new FieldMap(36, "COMPANY", "company", "user_company");
     public static final FieldMap USER_EMAIL = new FieldMap(37, "EMAIL", "email", "user_email");
     public static final FieldMap USER_TEL = new FieldMap(38, "PHONE", "tel", "user_tel");
     public static final FieldMap USER_CHILDCOUNT = new FieldMap(44,"SUBORDINATED_USERS_AMOUNT", "childcount", "user_childcount");
     public static final FieldMap USER_CHILDALLOWED = new FieldMap(43,"USERS_ALLOWED", "childallowed", "user_childallowed");
     public static final FieldMap MESSAGEVIEW = new FieldMap(45,"HISTORY_VIEW", "message_view", "message_view");
     public static final FieldMap TASK_PARENT = new FieldMap(46, "TASK_PARENT", "taskparent", "task_parent");
     public static final FieldMap USER_PARENT = new FieldMap(47, "USER_PARENT", "userparent", "user_parent");
     public static final FieldMap MESSAGE_TASK = new FieldMap(48, "MESSAGE_TASK", "messagetask", "message_task");
     public static final FieldMap DEFAULT_PROJECT = new FieldMap(49, "DEFAULT_PROJECT", "defaultproject", "defaultproject");
     public static final FieldMap MSG_DEADLINE = new FieldMap(50, "MESSAGE_DEADLINE", "msg_deadline", "msg_deadline");
     public static final FieldMap MSG_BUDGET = new FieldMap(51, "MESSAGE_BUDGET", "msg_budget", "msg_budget");
     public static final FieldMap MSG_PRIORITY = new FieldMap(52, "MESSAGE_PRIORITY", "msg_priority", "msg_priority");
     public static final FieldMap USER_TEMPLATE = new FieldMap(40, "USER_TEMPLATE", "template", "user_template");
     public static final FieldMap USER_PASSWORD = new FieldMap(40, "USER_PASSWORD", "password", "user_password");
     public static final ArrayList<FieldMap> taskFields = new ArrayList<FieldMap>();
     public static final ArrayList<FieldMap> userFields = new ArrayList<FieldMap>();
     public static final ArrayList<FieldMap> messageFields = new ArrayList<FieldMap>();
 
     public String toString(){
         return getAltKey()+" : "+getFilterKey()+" = "+getFieldKey();
     }
     
     static {
         taskFields.add(TASK_NUMBER);
         taskFields.add(TASK_CATEGORY);
         taskFields.add(TASK_STATUS);
         taskFields.add(TASK_RESOLUTION);
         taskFields.add(SUSER_NAME);
         taskFields.add(SUSER_STATUS);
         taskFields.add(HUSER_NAME);
         taskFields.add(HUSER_STATUS);
         taskFields.add(TASK_DEADLINE);
         taskFields.add(TASK_SUBMITDATE);
         taskFields.add(TASK_UPDATEDATE);
         taskFields.add(TASK_CLOSEDATE);
         taskFields.add(TASK_BUDGET);
         taskFields.add(TASK_ABUDGET);
         taskFields.add(TASK_CHILDCOUNT);
         taskFields.add(TASK_MESSAGECOUNT);
         taskFields.add(TASK_PRIORITY);
         taskFields.add(TASK_SHORTNAME);
         taskFields.add(TASK_NAME);
         taskFields.add(TASK_DESCRIPTION);
         taskFields.add(FULLPATH);
 
         messageFields.add(MSG_SUSER_NAME);
         messageFields.add(MSG_SUBMITDATE);
         messageFields.add(MSG_MSTATUS);
         messageFields.add(MSG_HUSER_NAME);
         messageFields.add(MSG_RESOLUTION);
         messageFields.add(MSG_ABUDGET);
         messageFields.add(TEXT_MSG);
 
         userFields.add(USER_EXPIREDATE);
         userFields.add(USER_STATUS);
         userFields.add(USER_LOCALE);
         userFields.add(USER_TIMEZONE);
         userFields.add(USER_LOGIN);
         userFields.add(USER_NAME);
         userFields.add(FULLPATH);
 
         userFields.add(USER_COMPANY);
         userFields.add(USER_EMAIL);
         userFields.add(USER_TEL);
         userFields.add(USER_ACTIVE);
         userFields.add(USER_CHILDCOUNT);
         userFields.add(USER_CHILDALLOWED);
         userFields.add(USER_TEMPLATE);
         userFields.add(USER_PASSWORD);
     }
 }
