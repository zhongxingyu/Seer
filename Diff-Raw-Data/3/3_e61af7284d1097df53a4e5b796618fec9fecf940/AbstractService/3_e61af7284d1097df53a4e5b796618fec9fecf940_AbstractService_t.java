 package com.pms.service.service;
 
 import java.io.UnsupportedEncodingException;
 import java.text.MessageFormat;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Locale;
 import java.util.Map;
 import java.util.Set;
 
 import org.apache.commons.validator.Arg;
 import org.apache.commons.validator.Field;
 import org.apache.commons.validator.Form;
 import org.apache.commons.validator.Validator;
 import org.apache.commons.validator.ValidatorAction;
 import org.apache.commons.validator.ValidatorException;
 import org.apache.commons.validator.ValidatorResources;
 import org.apache.commons.validator.ValidatorResult;
 import org.apache.commons.validator.ValidatorResults;
 import org.apache.poi.hssf.util.HSSFColor.ROYAL_BLUE;
 
 import com.pms.service.annotation.RoleValidConstants;
 import com.pms.service.dao.ICommonDao;
 import com.pms.service.dbhelper.DBQuery;
 import com.pms.service.dbhelper.DBQueryOpertion;
 import com.pms.service.dbhelper.DBQueryUtil;
 import com.pms.service.exception.ApiResponseException;
 import com.pms.service.mockbean.ApiConstants;
 import com.pms.service.mockbean.DBBean;
 import com.pms.service.mockbean.GroupBean;
 import com.pms.service.mockbean.ProjectBean;
 import com.pms.service.mockbean.PurchaseBack;
 import com.pms.service.mockbean.PurchaseRequest;
 import com.pms.service.mockbean.RoleBean;
 import com.pms.service.mockbean.SalesContractBean;
 import com.pms.service.mockbean.ShipBean;
 import com.pms.service.mockbean.UserBean;
 import com.pms.service.service.impl.PurchaseServiceImpl.PurchaseStatus;
 import com.pms.service.util.ApiThreadLocal;
 import com.pms.service.util.ApiUtil;
 import com.pms.service.util.DateUtil;
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
     protected boolean isProjectAssistant() {
 
         return inGroup(GroupBean.DEPARTMENT_ASSISTANT_VALUE);
 
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
 
     //助理
     protected boolean isDepartmentAssistant() {
 
         return inGroup(GroupBean.DEPARTMENT_ASSISTANT_VALUE);
 
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
     
     protected boolean isInDepartment(String depart) {
     	Map<String,Object> query = new HashMap<String,Object>();
     	query.put(UserBean.DEPARTMENT, depart);
     	query.put(ApiConstants.MONGO_ID, ApiThreadLocal.getCurrentUserId());
         return dao.exist(query, DBBean.USER);
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
     
     private boolean inRole(String roleId) {
 
         if (ApiThreadLocal.get(UserBean.USER_ID) == null) {
             return false;
 		} else {
 			String userId = ApiThreadLocal.get(UserBean.USER_ID).toString();
 
 			Map<String, Object> query = new HashMap<String, Object>();
 			query.put(RoleBean.ROLE_ID, roleId);
 			query.put(ApiConstants.LIMIT_KEYS, ApiConstants.MONGO_ID);
 
 			Map<String, Object> role = this.dao.findOneByQuery(query, DBBean.ROLE_ITEM);
 
 			List<String> roles = listUserRoleIds(userId);
 
 			if (role != null && role.get(ApiConstants.MONGO_ID) != null) {
 				return roles.contains(role.get(ApiConstants.MONGO_ID).toString());
 			}
 			return false;
 
 		}
 
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
     
     
     /**
      * 
      * @param params
      *            页面传递来的参数
      * @param myRealQueryKey
      *            页面搜索外键的字段，比如数据存的是customerId,页面传递过来的是customerName，customerId就是myQueryKey，customerName 就是mySearchDataKey
      * @param mySearchDataKey
      * @param refSearchKey
      * @param db
      *            相关联的数据库
      */
     protected void mergeRefSearchQuery(Map<String, Object> params, String myRealQueryKey, String mySearchDataKey, String refSearchKey, String db) {
         if (params.get(mySearchDataKey) != null && !ApiUtil.isEmpty(params)) {
             
             if (params.get(mySearchDataKey) instanceof DBQuery) {
                 DBQuery query = (DBQuery) params.get(mySearchDataKey);
 
                 Map<String, Object> refQuery = new HashMap<String, Object>();
                 refQuery.put(ApiConstants.LIMIT_KEYS, ApiConstants.MONGO_ID);
                 refQuery.put(refSearchKey, new DBQuery(query.getOperation(), query.getValue()));
 
                 params.remove(mySearchDataKey);
                 params.put(myRealQueryKey, new DBQuery(DBQueryOpertion.IN, this.dao.listLimitKeyValues(refQuery, db)));
             }
         }
     }
     
     protected void mergeDataRoleQueryWithProject(Map<String, Object> param) {
         Map<String, Object> pmQuery = new HashMap<String, Object>();
 
         if (isAdmin() || isFinance() || isPurchase() || isCoo() || isDepotManager() || isDepartmentAssistant()) {
             // query all data
         } else {
             pmQuery.put(ProjectBean.PROJECT_MANAGER, getCurrentUserId());
             pmQuery.put(ApiConstants.CREATOR, getCurrentUserId());
             // list creator or manager's data
             param.put(ProjectBean.PROJECT_MANAGER, DBQueryUtil.buildQueryObject(pmQuery, false));
         }
     }
     
     
     protected void mergeDataRoleQueryWithProjectAndScType(Map<String, Object> param) {
         Map<String, Object> pmQuery = new HashMap<String, Object>();
 
         if (isAdmin() || isFinance() || isPurchase() || isCoo() || isDepotManager() || isDepartmentAssistant()) {
             // query all data
         } else {
             pmQuery.put(ProjectBean.PROJECT_MANAGER, getCurrentUserId());
             pmQuery.put(ApiConstants.CREATOR, getCurrentUserId());
 
             Map<String, Object> userQuery = new HashMap<String, Object>();
             userQuery.put(ApiConstants.MONGO_ID, getCurrentUserId());
             userQuery.put(ApiConstants.LIMIT_KEYS, UserBean.DEPARTMENT);
             Map<String, Object> user = this.dao.findOneByQuery(userQuery, DBBean.USER);
             
             if (user.get(UserBean.DEPARTMENT) != null) {
                 String dep = user.get(UserBean.DEPARTMENT).toString();
 
                 // FIXME: put into constants
                 List<String> scTypesIn = new ArrayList<String>();
                 scTypesIn.add("弱电工程");
                 scTypesIn.add("产品集成（灯控/布线）");
                 scTypesIn.add("产品集成（楼控）");
                 scTypesIn.add("产品集成（其他）");
 
                 if (dep.equalsIgnoreCase(UserBean.USER_DEPARTMENT_PROJECT)) {
                     pmQuery.put(SalesContractBean.SC_TYPE, new DBQuery(DBQueryOpertion.IN, scTypesIn));
                 } else {
                     pmQuery.put(SalesContractBean.SC_TYPE, new DBQuery(DBQueryOpertion.NOT_IN, scTypesIn));
                 }
 
             }
 
             // list creator or manager's data
             param.put(ProjectBean.PROJECT_MANAGER, DBQueryUtil.buildQueryObject(pmQuery, false));
         }
     }
     
     
 
     protected Map<String, Object> getMyApprovedQuery() {
         Map<String, Object> taskQuery = new HashMap<String, Object>();
         taskQuery.put(ApiConstants.CREATOR, ApiThreadLocal.getCurrentUserId());      
         Map<String, Object> statusQuery = new HashMap<String, Object>();      
         statusQuery.put("status",  new DBQuery(DBQueryOpertion.IN, new String[]{PurchaseRequest.STATUS_APPROVED, ShipBean.SHIP_STATUS_FINAL_APPROVE, PurchaseRequest.STATUS_IN_REPOSITORY}));
         statusQuery.put(PurchaseBack.paStatus, new DBQuery(DBQueryOpertion.IN, new String[] { PurchaseStatus.approved.toString(), PurchaseStatus.firstApprove.toString(), PurchaseStatus.finalApprove.toString() }));
         //or query
         taskQuery.put("status", DBQueryUtil.buildQueryObject(statusQuery, false));
         return taskQuery;
     }
 
     protected Map<String, Object> getMyRejectedQuey() {
         Map<String, Object> taskQuery = new HashMap<String, Object>();
         taskQuery.put(ApiConstants.CREATOR, ApiThreadLocal.getCurrentUserId());      
         Map<String, Object> statusQuery = new HashMap<String, Object>();      
         statusQuery.put("status",  new DBQuery(DBQueryOpertion.IN, new String[]{PurchaseRequest.STATUS_REJECTED, ShipBean.SHIP_STATUS_REJECT}));
         statusQuery.put(PurchaseBack.pbStatus, PurchaseStatus.rejected.toString());
         statusQuery.put(PurchaseBack.paStatus, new DBQuery(DBQueryOpertion.IN, new String[] { PurchaseStatus.rejected.toString(), PurchaseStatus.firstRejected.toString(), PurchaseStatus.finalRejected.toString() }));
 
         //or query
         taskQuery.put("status", DBQueryUtil.buildQueryObject(statusQuery, false));
         return taskQuery;
     }
 
     protected Map<String, Object> getMyInprogressQuery(String db) {
         //我的待批
         Map<String, Object>  statusQuery = new HashMap<String, Object>();
 
         Map<String, Object> ownerQuery = new HashMap<String, Object>();
         ownerQuery.put(ApiConstants.CREATOR, ApiThreadLocal.getCurrentUserId());
 
         //FIXME 根据部门查询数据
         if (db.equalsIgnoreCase(DBBean.PURCHASE_REQUEST)) {
             if (inRole(RoleValidConstants.PURCHASE_REQUEST_PROCESS)) {
                 ownerQuery.remove(ApiConstants.CREATOR);
             }
             mergeDataRoleQueryWithProjectAndScType(ownerQuery);
         }
 
         if (db.equalsIgnoreCase(DBBean.PURCHASE_ORDER)) {
             if (inRole(RoleValidConstants.PURCHASE_ORDER_PROCESS)) {
                 ownerQuery.remove(ApiConstants.CREATOR);
             }
             mergeDataRoleQueryWithProjectAndScType(ownerQuery);
         }
 
         if (db.equalsIgnoreCase(DBBean.PURCHASE_CONTRACT)) {
             if (inRole(RoleValidConstants.PURCHASE_CONTRACT_PROCESS)) {
                 ownerQuery.remove(ApiConstants.CREATOR);
             }
         }
 
         if (db.equalsIgnoreCase(DBBean.BORROWING)) {
             if (inRole(RoleValidConstants.BORROWING_MANAGEMENT_PROCESS)) {
                 ownerQuery.remove(ApiConstants.CREATOR);
             }
         }
 
         if (db.equalsIgnoreCase(DBBean.PURCHASE_ALLOCATE)) {
             if (inRole(RoleValidConstants.PURCHASE_ALLOCATE_PROCESS)) {
                 ownerQuery.remove(ApiConstants.CREATOR);
             }
             mergeDataRoleQueryWithProjectAndScType(ownerQuery);
         }
 
         if (db.equalsIgnoreCase(DBBean.REPOSITORY)) {
             if (inRole(RoleValidConstants.REPOSITORY_MANAGEMENT_PROCESS)) {
                 ownerQuery.remove(ApiConstants.CREATOR);
             }
         }
 
         if (db.equalsIgnoreCase(DBBean.SHIP)) {
             if (inRole(RoleValidConstants.SHIP_MANAGEMENT_PROCESS) || inRole(RoleValidConstants.SHIP_MANAGEMENT_FINAL_PROCESS)) {
                 ownerQuery.remove(ApiConstants.CREATOR);
             }
         }
         
 		if (db.equalsIgnoreCase(DBBean.PURCHASE_BACK)) {
             if (inRole(RoleValidConstants.PURCHASE_BACK_PROCESS)) {
                 ownerQuery.remove(ApiConstants.CREATOR);
             }
             mergeDataRoleQueryWithProjectAndScType(ownerQuery);
             statusQuery.put(PurchaseBack.pbStatus, PurchaseStatus.submited.toString());
 
         }
         
         
         if (db.equalsIgnoreCase(DBBean.PURCHASE_ALLOCATE)) {
             if (inRole(RoleValidConstants.PURCHASE_ALLOCATE_PROCESS)) {
                 statusQuery.put(PurchaseBack.paStatus, PurchaseStatus.submited.toString());
                 ownerQuery.remove(ApiConstants.CREATOR);
             } else if (inRole(RoleValidConstants.PURCHASE_ALLOCATE_FINAL_PROCESS)) {
                 statusQuery.put(PurchaseBack.paStatus, PurchaseStatus.firstApprove.toString());
                 ownerQuery.remove(ApiConstants.CREATOR);
             }
         }else{
             statusQuery.put("status", new DBQuery(DBQueryOpertion.IN, new String[] { PurchaseRequest.STATUS_NEW, PurchaseRequest.STATUS_REPOSITORY_NEW, ShipBean.SHIP_STATUS_SUBMIT }));
         }
         // or query
         ownerQuery.put("status", DBQueryUtil.buildQueryObject(statusQuery, false));
 
         
         return ownerQuery;
     }
 
     protected Map<String, Object> getMyDraftQuery() {
         // 我的草稿
         Map<String, Object> taskQuery = new HashMap<String, Object>();
         taskQuery.put(ApiConstants.CREATOR, ApiThreadLocal.getCurrentUserId());
 
         Map<String, Object> statusQuery = new HashMap<String, Object>();
         statusQuery.put("status",  new DBQuery(DBQueryOpertion.IN, new String[] { PurchaseRequest.STATUS_DRAFT, ShipBean.SHIP_STATUS_DRAFT }));
         statusQuery.put(PurchaseBack.pbStatus, PurchaseStatus.saved.toString());
 
         // or query
         taskQuery.put("status", DBQueryUtil.buildQueryObject(statusQuery, false));
         return taskQuery;
     }
     
     protected void mergeMyTaskQuery(Map<String, Object> param, String db) {
 
         if (ApiThreadLocal.getMyTask() != null) {
 
             String task = ApiThreadLocal.getMyTask();
 
             if (task.equalsIgnoreCase("draft")) {
                 param.putAll(getMyDraftQuery());
             } else if (task.equalsIgnoreCase("inprogress")) {
                 param.putAll(getMyInprogressQuery(db));
             } else if (task.equalsIgnoreCase("rejected")) {
                 param.putAll(getMyRejectedQuey());
             } else if (task.equalsIgnoreCase("approved")) {
                 param.putAll(getMyApprovedQuery());
             } else if (task.equalsIgnoreCase("tip")) {
 
             }
         }
     }
     
     /**
      * 某个字段更新，相关联冗余存放该字段的地方都要同时更新。
      * @param collections 冗余存放某字段，需要同时更新的 集合
      * @param query 待更新记录的条件
      * @param updateKey 更新字段
      * @param updateValue 更新字段新的值
      */
     public void updateRelatedCollectionForTheSameField(String[] collections,Map<String, Object> query, String updateKey, String updateValue){
     	query.put(ApiConstants.LIMIT_KEYS, new String[] {ApiConstants.MONGO_ID});
     	for (int i=0; i<collections.length; i++){
     		String cName = collections[i];
     		List<Object> ids = dao.listLimitKeyValues(query, cName);
     		for (Object id : ids){
     			Map<String, Object> updateQuery = new HashMap<String, Object>();
         		updateQuery.put(updateKey, updateValue);
         		updateQuery.put(ApiConstants.MONGO_ID, id);
         		dao.updateById(updateQuery, cName);
     		}
     	}
     }
 
 
     public Map<String, Integer> countEqByKey(Map<String, Object> query, String db, String queryKey, Map<String, Integer> count) {        
         return countEqByKey(query, db, queryKey, count, null);
     }
     
     
     public Map<String, Integer> countEqByKey(Map<String, Object> query, String db, String queryKey, Map<String, Integer> count, Map<String, Object> compare) {
         query.put(ApiConstants.LIMIT_KEYS, SalesContractBean.SC_EQ_LIST);
         List<Object> list = this.dao.listLimitKeyValues(query, db);
         Map<String, Integer> eqCountMap = new HashMap<String, Integer>();
 
         if(count != null){
             eqCountMap = count;
         }
         if (list != null) {
             for (Object obj : list) {
                 if (obj != null) {
                     List<Map<String, Object>> eqlistMap = (List<Map<String, Object>>) obj;
                     for (Map<String, Object> eqMap : eqlistMap) {
                         
                         boolean needCount = true;
                         if(compare !=null && !compare.isEmpty()){
                            
                             for(String key: compare.keySet()){
                                 
                                 if(eqMap.get(key) == null){
                                     needCount = false;
                                 }else if(!eqMap.get(key).equals(compare.get(key))){
                                     needCount = false;
                                 }
                             }
                                 
                         }else{
                             needCount = true;
                         }
                         
                         if (needCount) {
                             if (eqCountMap.get(eqMap.get(ApiConstants.MONGO_ID).toString()) != null) {
                                 eqCountMap.put(eqMap.get(ApiConstants.MONGO_ID).toString(), ApiUtil.getInteger(eqMap.get(queryKey), 0) + ApiUtil.getInteger(eqCountMap.get(eqMap.get(ApiConstants.MONGO_ID).toString()), 0));
                             } else {
                                 eqCountMap.put(eqMap.get(ApiConstants.MONGO_ID).toString(), ApiUtil.getInteger(eqMap.get(queryKey), 0));
                             }
                         }
                         
                     }
                 }
             }
         }
         return eqCountMap;
     }
     
     
     public Map<String, Integer> countEqByKeyWithMultiKey(Map<String, Object> query, String db, String queryKey, Map<String, Integer> count, String[] keys) {
         query.put(ApiConstants.LIMIT_KEYS, SalesContractBean.SC_EQ_LIST);
         List<Object> list = this.dao.listLimitKeyValues(query, db);
         Map<String, Integer> eqCountMap = new HashMap<String, Integer>();
 
         if (count != null) {
             eqCountMap = count;
         }
         if (list != null) {
             for (Object obj : list) {
                 if (obj != null) {
                     List<Map<String, Object>> eqlistMap = (List<Map<String, Object>>) obj;
                     for (Map<String, Object> eqMap : eqlistMap) {
 
                         String multKey = "";
                         if (keys != null && keys.length > 0) {
                             for (String key : keys) {
                                 
                                 if (eqMap.get(key) != null) {
                                     multKey = multKey + eqMap.get(key).toString();
                                 }
                             }
 
                         }
                         String id = eqMap.get(ApiConstants.MONGO_ID).toString();
 
                         id = (id + multKey).trim();
                         if (eqCountMap.get(id) != null) {
                             eqCountMap.put(id, ApiUtil.getInteger(eqMap.get(queryKey), 0) + ApiUtil.getInteger(eqCountMap.get(id), 0));
                         } else {
                             eqCountMap.put(id, ApiUtil.getInteger(eqMap.get(queryKey), 0));
                         }
 
                     }
                 }
             }
         }
         return eqCountMap;
     }
     
     
     
     
     /**
      * 
      * 根据设备清单中的某个key来过滤数据小于等于0的数据
      * @param eqCostList
      * @param key
      */
     public void removeEmptyEqList(List<Map<String, Object>> eqCostList, String key) {
         List<Map<String, Object>> removedList = new ArrayList<Map<String, Object>>();
         for (Map<String, Object> data : eqCostList) {
             
             if(data.get(key) == null || ApiUtil.isEmpty(data.get(key))){
                 removedList.add(data); 
             }else if (! key.equalsIgnoreCase("eqCostList") && ApiUtil.getInteger(data.get(key), 0) <= 0) {
                 removedList.add(data);
             }
         }
 
         for (Map<String, Object> orderMap : removedList) {
             eqCostList.remove(orderMap);
         }
 
     }
 
     /**
      * 
      * 根据设备清单中的某个key来过滤数据小于等于0的数据
      * @param eqCostList
      * @param key
      */
     public void removeEmptyEqList(Map<String, Object> eqListMap, String key) {
         if (eqListMap.get("eqcostList") != null) {
             List<Map<String, Object>> eqCostList = (List<Map<String, Object>>) eqListMap.get("eqcostList");
             removeEmptyEqList(eqCostList, key);
         }
 
     }
     
     
     
     public String generateCode(String prefix, String db, String codeKey) {
         Map<String, Object> map = new HashMap<String, Object>();
         String[] limitKeys = { codeKey };
         Map<String, Object> queryMap = new HashMap<String, Object>();
         int year = DateUtil.getNowYearString();
		queryMap.put(codeKey, new DBQuery(DBQueryOpertion.LIKE, year));
		Map<String, Object> re = dao.getLastRecordByCreatedOn(db, queryMap, limitKeys);
         String code = null;
         if (re != null) {
             code = (String) re.get(codeKey);
         }
         Integer scCodeNo = 0;
 
         if (re != null && !ApiUtil.isEmpty(code)) {
             String scCodeNoString = code.substring(code.lastIndexOf("-") + 1, code.length());
             try {
                 scCodeNo = Integer.parseInt(scCodeNoString);
             } catch (NumberFormatException e) {
                 // TODO Auto-generated catch block
                 // e.printStackTrace(); 旧数据会出异常，就pCodeNo=1 开始
             }
         }
         scCodeNo = scCodeNo + 1;
         String codeNum = "000" + scCodeNo;
 
         codeNum = codeNum.substring(codeNum.length() - 4, codeNum.length());
         String genCode = prefix + "-" + year + "-" + codeNum;
         
         while (this.dao.exist(codeKey, genCode, db)) {
             scCodeNo = scCodeNo + 1;
             codeNum = "000" + scCodeNo;
             codeNum = codeNum.substring(codeNum.length() - 4, codeNum.length());
             genCode = prefix + "-" + year + "-" + codeNum;
         }
         return genCode;
     }
 
 	public String recordComment(String action,String newComment,String oldComment){
 		StringBuilder str = new StringBuilder();
 		String name = ApiThreadLocal.getCurrentUserName();
 		if(oldComment != null) str.append(oldComment).append("\n");
 		str.append(DateUtil.getDateString(new Date())).append(" ").append(name).append("").append(action);
 		if(newComment != null) str.append("： ").append(newComment);
 		return str.toString();
 	}
 
 	protected void mergeCreatorInfo(Map<String,Object> params){
 		List<Map<String,Object>> list = new ArrayList<Map<String,Object>>();
 		if(params.containsKey(ApiConstants.RESULTS_DATA)){
 			list = (List<Map<String,Object>>)params.get(ApiConstants.RESULTS_DATA);
 		}else {
 			list.add(params);
 		}
 		List<String> uIds = new ArrayList<String>();
 		for(Map<String,Object> obj : list){
 			String id = (String)obj.get(ApiConstants.CREATOR);
 			if(!uIds.contains(id))uIds.add(id);
 		}
 		Map<String,Object> uQuery = new HashMap<String,Object>();
 		uQuery.put(ApiConstants.LIMIT_KEYS, new String[]{UserBean.USER_NAME});
 		uQuery.put(ApiConstants.MONGO_ID, new DBQuery(DBQueryOpertion.IN, uIds));
 		Map<String,Object> users = dao.listToOneMapAndIdAsKey(uQuery, DBBean.USER);
 		for(Map<String,Object> obj : list){
 			String id = (String)obj.get(ApiConstants.CREATOR);
 			Map<String,Object> user = (Map<String,Object>)users.get(id);
 			obj.put("creatorName", user.get(UserBean.USER_NAME));
 		}
 	}
 	
     public ISalesContractService getScs() {
         return scs;
     }
 
     public void setScs(ISalesContractService scs) {
         this.scs = scs;
     }
 
 
     public ICommonDao getDao() {
         return dao;
     }
 
     public void setDao(ICommonDao dao) {
         this.dao = dao;
     }
     
     
 
 }
