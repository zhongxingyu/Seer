 package com.pms.service.service;
 
 import java.io.UnsupportedEncodingException;
 import java.text.MessageFormat;
 import java.util.ArrayList;
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
 import com.pms.service.dbhelper.DBQueryUtil;
 import com.pms.service.exception.ApiResponseException;
 import com.pms.service.mockbean.ApiConstants;
 import com.pms.service.mockbean.DBBean;
 import com.pms.service.mockbean.GroupBean;
 import com.pms.service.mockbean.ProjectBean;
 import com.pms.service.mockbean.UserBean;
 import com.pms.service.util.ApiThreadLocal;
 import com.pms.service.util.ApiUtil;
 import com.pms.service.validators.ValidatorUtil;
 
 public abstract class AbstractService {
 
     protected ICommonDao dao = null;
     
     protected ISalesContractService scs;
 
 
 
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
 
     //部门助理
     protected boolean isDepartMentAssistant() {
 
         return inGroup(GroupBean.DEPARTMENT_ASSISTANT_VALUE);
 
     }
 
     //部门经理
     protected boolean isDepartmentManager() {
 
         return inGroup(GroupBean.DEPARTMENT_MANAGER_VALUE);
 
     }
 
     protected boolean isAdmin() {
 
         return inGroup(GroupBean.GROUP_ADMIN_VALUE);
 
     }
 
     //采购
     protected boolean isPurchase() {
 
         return inGroup(GroupBean.PURCHASE_VALUE);
 
     }
     
     //采购
     protected boolean isPM() {
 
         return inGroup(GroupBean.PM);
 
     }
 
     
     //库管
     protected boolean isDepotManager() {
 
         return inGroup(GroupBean.DEPOT_MANAGER_VALUE);
 
     }
     
     //财务
     protected boolean isFinance() {
 
         return inGroup(GroupBean.FINANCE);
 
     }
     
     protected String getCurrentUserId() {
 
         return ApiThreadLocal.getCurrentUserId();
     }
     
     
     private boolean inGroup(String groupName){
         if(ApiThreadLocal.get(UserBean.USER_ID) == null){
             return false;
         }else{
             String userId = ApiThreadLocal.get(UserBean.USER_ID).toString();
             Map<String, Object> query = new HashMap<String, Object>();
             query.put(ApiConstants.LIMIT_KEYS, ApiConstants.MONGO_ID);
             query.put(GroupBean.GROUP_NAME, groupName);
             Map<String, Object> group = this.dao.findOneByQuery(query, DBBean.USER_GROUP);
             String id = group == null? null : group.get(ApiConstants.MONGO_ID).toString();
             
             
             Map<String, Object> userQuery = new HashMap<String, Object>();
             userQuery.put(ApiConstants.MONGO_ID, userId);
             userQuery.put(UserBean.GROUPS, new DBQuery(DBQueryOpertion.IN, id));
             
             return this.dao.exist(userQuery, DBBean.USER);
             
         }
 
     }
 
     
     
     protected void mergeDataRoleQuery(Map<String, Object> param) {
        if (isAdmin() || isFinance() || isPurchase() || isCoo()) {
             // query all data
         } else {
             Map<String, Object> pmQuery = new HashMap<String, Object>();
             pmQuery.put(ProjectBean.PROJECT_MANAGER, getCurrentUserId());
             pmQuery.put(ApiConstants.CREATOR, getCurrentUserId());
 
             Map<String, Object> userQuery = new HashMap<String, Object>();
             userQuery.put(ApiConstants.MONGO_ID, getCurrentUserId());
             userQuery.put(ApiConstants.LIMIT_KEYS, UserBean.DEPARTMENT);
             Map<String, Object> user = this.dao.findOneByQuery(userQuery, DBBean.USER);
 
             if (user.get(UserBean.DEPARTMENT) != null) {
                 List<String> departements = (List<String>)user.get(UserBean.DEPARTMENT);
                 List<String> rolesIn = new ArrayList<String>();
                 for(String dep:  departements){
                     if(dep.equalsIgnoreCase(UserBean.USER_DEPARTMENT_PROJECT)){
                         rolesIn.add(ProjectBean.PROJECT_TYPE_PROJECT);
                     }else{
                         rolesIn.add(ProjectBean.PROJECT_TYPE_PRODUCT);
                         rolesIn.add(ProjectBean.PROJECT_TYPE_SERVICE);
                     }
                 }
                 
                 pmQuery.put(ProjectBean.PROJECT_TYPE, new DBQuery(DBQueryOpertion.IN, rolesIn));
             }
 
             // list creator or manager's data
             param.put(ProjectBean.PROJECT_MANAGER, DBQueryUtil.buildQueryObject(pmQuery, false));
         }
     }
     
 
     public ISalesContractService getScs() {
         return scs;
     }
 
     public void setScs(ISalesContractService scs) {
         this.scs = scs;
     }
 
     public String generateCode(String prefix, String db) {
         return prefix + "-" + this.dao.count(null, db) + 1;
     }
 
     public ICommonDao getDao() {
         return dao;
     }
 
     public void setDao(ICommonDao dao) {
         this.dao = dao;
     }
     
     
 
 }
