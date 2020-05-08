 package com.pms.service.service;
 
 import java.io.UnsupportedEncodingException;
 import java.text.MessageFormat;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Locale;
 import java.util.Map;
 
 import org.apache.commons.validator.Arg;
 import org.apache.commons.validator.Field;
 import org.apache.commons.validator.Form;
 import org.apache.commons.validator.Validator;
 import org.apache.commons.validator.ValidatorAction;
 import org.apache.commons.validator.ValidatorException;
 import org.apache.commons.validator.ValidatorResources;
 import org.apache.commons.validator.ValidatorResult;
 import org.apache.commons.validator.ValidatorResults;
 
 import com.pms.service.dao.ICommonDao;
 import com.pms.service.dbhelper.DBQuery;
 import com.pms.service.dbhelper.DBQueryOpertion;
 import com.pms.service.exception.ApiResponseException;
 import com.pms.service.mockbean.ApiConstants;
 import com.pms.service.mockbean.DBBean;
 import com.pms.service.mockbean.GroupBean;
 import com.pms.service.mockbean.UserBean;
 import com.pms.service.util.ApiThreadLocal;
 import com.pms.service.util.ApiUtil;
 import com.pms.service.validators.ValidatorUtil;
import com.sun.xml.internal.ws.api.pipe.helper.AbstractPipeImpl;
 
 public abstract class AbstractService {
 
     protected ICommonDao dao = null;
 
 
 
     public abstract String geValidatorFileName();
 
     @SuppressWarnings({ "rawtypes", "deprecation" })
     public void validate(Map<String, Object> map, String validatorForm) {
         ValidatorUtil.init();
         if (map == null) {
             map = new HashMap<String, Object>();
         }
         map.put("dao", this.getDao());
 
         ValidatorResources resources = ValidatorUtil.initValidatorResources().get(geValidatorFileName());
 
         // Create a validator with the ValidateBean actions for the bean
         // we're interested in.
         Validator validator = new Validator(resources, validatorForm);
         // Tell the validator which bean to validate against.
         validator.setParameter(Validator.BEAN_PARAM, map);
         ValidatorResults results = null;
 
         try {
             results = validator.validate();
         } catch (ValidatorException e) {
             // TODO Auto-generated catch block
             e.printStackTrace();
         }
 
         // Start by getting the form for the current locale and Bean.
         Form form = resources.getForm(Locale.CHINA, validatorForm);
         // Iterate over each of the properties of the Bean which had messages.
         Iterator propertyNames = results.getPropertyNames().iterator();
         while (propertyNames.hasNext()) {
             String propertyName = (String) propertyNames.next();
 
             // Get the Field associated with that property in the Form
             Field field = form.getField(propertyName);
 
             // Get the result of validating the property.
             ValidatorResult result = results.getValidatorResult(propertyName);
 
             // Get all the actions run against the property, and iterate over
             // their names.
             Map actionMap = result.getActionMap();
             Iterator keys = actionMap.keySet().iterator();
             String msg = "";
             while (keys.hasNext()) {
                 String actName = (String) keys.next();
                 // Get the Action for that name.
                 ValidatorAction action = resources.getValidatorAction(actName);
                 String actionMsgKey = field.getArg(0).getKey() + "." + action.getName();
                 // Look up the formatted name of the field from the Field arg0
                 String prettyFieldName = ValidatorUtil.intiBundle().getString(field.getArg(0).getKey());
 
                 boolean customMsg = false;
                 if (isArgExists(actionMsgKey, field)) {
                     customMsg = true;
                     prettyFieldName = ValidatorUtil.intiBundle().getString(actionMsgKey);
                 }
 
                 if (!result.isValid(actName)) {
                     String message = "{0}";
                     if (!customMsg) {
                         message = ValidatorUtil.intiBundle().getString(action.getMsg());
                     }
                     Object[] argsss = { prettyFieldName };
                     try {
                         msg = msg.concat(new String(MessageFormat.format(message, argsss).getBytes("ISO-8859-1"), "UTF-8"));
                     } catch (UnsupportedEncodingException e) {
                         e.printStackTrace();
                     }
                 }
             }
             if (!ApiUtil.isEmpty(msg)) {
                 throw new ApiResponseException(String.format("Validate [%s] failed with paramters [%s]", validatorForm, map), null, msg);
             }
         }
 
         map.remove("dao");
     }
 
 
 
     private boolean isArgExists(String key, Field field) {
 
         Arg[] args = field.getArgs("");
 
         for (Arg arg : args) {
             if (arg.getKey().equalsIgnoreCase(key)) {
                 return true;
             }
         }
 
         return false;
 
     }
     
     
     
     protected boolean isCoo() {
 
         return inGroup(GroupBean.COO_VALUE);
 
     }
 
     protected boolean isDepartMentAssistant() {
 
         return inGroup(GroupBean.DEPARTMENT_ASSISTANT_VALUE);
 
     }
 
     protected boolean isDepartmentManager() {
 
         return inGroup(GroupBean.DEPARTMENT_MANAGER_VALUE);
 
     }
 
     protected boolean isAdmin() {
 
         return inGroup(GroupBean.GROUP_ADMIN_VALUE);
 
     }
 
     protected boolean isPurchase() {
 
         return inGroup(GroupBean.PURCHASE_VALUE);
 
     }
 
     protected boolean isDepotManager() {
 
         return inGroup(GroupBean.DEPOT_MANAGER_VALUE);
 
     }
     
     protected String getCurrentUserId() {
 
         return ApiThreadLocal.getCurrentUserId();
     }
     
     
     private boolean inGroup(String groupName){
         if(ApiThreadLocal.get(UserBean.USER_ID) == null){
             return false;
         }else{
             String userId = ApiThreadLocal.get(UserBean.USER_ID).toString();
             List<String> userRoles = this.listUserRoleIds(userId);
             Map<String, Object> query = new HashMap<String, Object>();
             query.put(ApiConstants.LIMIT_KEYS, ApiConstants.MONGO_ID);
             query.put(GroupBean.GROUP_NAME, groupName);
             Map<String, Object> group = this.dao.findOneByQuery(query, DBBean.USER_GROUP);
             String id = group == null? null : group.get(ApiConstants.MONGO_ID).toString();
             if(userRoles.contains(id)){
                 return true;
             }
             
         }
         
         return false;
     }
     
     public List<String> listUserRoleIds(String userId) {
         Map<String, Object> query = new HashMap<String, Object>();
         query.put(ApiConstants.MONGO_ID, userId);
         query.put(ApiConstants.LIMIT_KEYS, new String[] { UserBean.GROUPS });
         Map<String, Object> user = dao.findOneByQuery(query, DBBean.USER);
         List<String> groups = (List<String>) user.get(UserBean.GROUPS);
         
         Map<String, Object> limitQuery = new HashMap<String, Object>();
         limitQuery.put(ApiConstants.MONGO_ID, new DBQuery(DBQueryOpertion.IN, groups));
         limitQuery.put(ApiConstants.LIMIT_KEYS, new String[]{GroupBean.ROLES});
         
         List<Object> list = dao.listLimitKeyValues(limitQuery, DBBean.USER_GROUP);
         List<String> roles = new ArrayList<String>();
 
         for(Object role: list){
             roles.addAll((Collection<? extends String>) role);
         }
         
         if(user.get(UserBean.OTHER_ROLES)!=null){
             roles.addAll((List<? extends String>) user.get(UserBean.OTHER_ROLES));
         }
 
         return roles;
     }
     
     
     public ICommonDao getDao() {
         return dao;
     }
 
     public void setDao(ICommonDao dao) {
         this.dao = dao;
     }
 
 }
