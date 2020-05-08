 package com.pms.service.service.impl;
 
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.HashMap;
import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
import java.util.Set;
 
 import org.apache.logging.log4j.LogManager;
 import org.apache.logging.log4j.Logger;
 
 import com.pms.service.dbhelper.DBQuery;
 import com.pms.service.dbhelper.DBQueryOpertion;
 import com.pms.service.mockbean.ApiConstants;
 import com.pms.service.mockbean.CustomerBean;
 import com.pms.service.mockbean.DBBean;
 import com.pms.service.mockbean.PayMoneyBean;
 import com.pms.service.mockbean.ProjectBean;
 import com.pms.service.mockbean.PurchaseBack;
 import com.pms.service.mockbean.PurchaseCommonBean;
 import com.pms.service.mockbean.PurchaseRequest;
 import com.pms.service.mockbean.PurchaseRequestOrder;
 import com.pms.service.mockbean.SalesContractBean;
 import com.pms.service.mockbean.UserBean;
 import com.pms.service.service.AbstractService;
 import com.pms.service.service.IPurchaseContractService;
 import com.pms.service.service.IPurchaseService;
 import com.pms.service.service.ISalesContractService;
 import com.pms.service.service.impl.PurchaseServiceImpl.PurchaseStatus;
 import com.pms.service.util.ApiUtil;
 
 public class PurchaseContractServiceImpl extends AbstractService implements IPurchaseContractService {
 
     private static final String APPROVED = PurchaseRequestOrder.STATUS_APPROVED;
     private static final Logger logger = LogManager.getLogger(PurchaseContractServiceImpl.class);
     
     
     private IPurchaseService backService;
     
     private ISalesContractService scs;
     
     @Override
     public String geValidatorFileName() {
         return null;
     }
 
 
     @Override
     public Map<String, Object> listPurchaseContracts() {
         Map<String, Object> results = dao.list(null, DBBean.PURCHASE_CONTRACT);
         List<Map<String, Object>> list = (List<Map<String, Object>>) results.get(ApiConstants.RESULTS_DATA);
         
         for(Map<String, Object> data: list){
             Map<String, Object> query = new HashMap<String, Object>();
             query.put(ApiConstants.MONGO_ID, data.get("supplierName"));
             
             
             Map<String, Object> relatedProjectInfo = this.dao.findOneByQuery(query, DBBean.SUPPLIER);
             
             data.put("supplierName", relatedProjectInfo.get("supplierName"));
         }
         
         return results;
         
     }
     
     
     public List<Map<String,Object>> listApprovedPurchaseContractCosts(String salesContractCode){
         Map<String, Object> query = new HashMap<String, Object>();
         query.put(ApiConstants.LIMIT_KEYS, new String[]{SalesContractBean.SC_EQ_LIST});
         query.put(PurchaseRequestOrder.PROCESS_STATUS, APPROVED);
 
         Map<String, Object>  results = this.dao.findOneByQuery(query, DBBean.PURCHASE_CONTRACT);
         List<Map<String,Object>> list = (List<Map<String, Object>>) results.get("eqcostList");
         return list;
     }
     
     public Map<String, Object> getPurchaseContract(HashMap<String, Object> parameters){
         
         return this.dao.findOne(ApiConstants.MONGO_ID, parameters.get(ApiConstants.MONGO_ID), DBBean.PURCHASE_CONTRACT);
     }
 
 
     @Override
     public void deletePurchaseContract(Map<String, Object> contract) {
         List<String> ids = new ArrayList<String>();
         ids.add(contract.get(ApiConstants.MONGO_ID).toString());
         dao.deleteByIds(ids, DBBean.PURCHASE_CONTRACT);
 
     }
 
     @Override
     public Map<String, Object> updatePurchaseContract(Map<String, Object> contract) {
         return updatePurchase(contract, DBBean.PURCHASE_CONTRACT);
     }
  
 
     @Override
     public Map<String, Object> listPurchaseOrders() {
         
         Map<String, Object> results = dao.list(null, DBBean.PURCHASE_ORDER);
         List<Map<String, Object>> list = (List<Map<String, Object>>) results.get(ApiConstants.RESULTS_DATA);
         
         for(Map<String, Object> data: list){
             
             
             Map<String, Object> relatedProjectInfo = getRelatedProjectInfo(data);
             data.put("customerName", relatedProjectInfo.get(ProjectBean.PROJECT_CUSTOMER));
             data.put("projectName", relatedProjectInfo.get("projectName"));
             data.put("projectManager", relatedProjectInfo.get("projectManager"));
         }
         
         return results;
         
     }
 
     @Override
     public void deletePurchaseOrder(Map<String, Object> contract) {
         List<String> ids = new ArrayList<String>();
         ids.add(contract.get(ApiConstants.MONGO_ID).toString());
         dao.deleteByIds(ids, DBBean.PURCHASE_ORDER);
 
     }
 
     @Override
     public Map<String, Object> updatePurchaseOrder(Map<String, Object> parameters) {
         PurchaseRequest request = (PurchaseRequest) new PurchaseRequest().toEntity(parameters);
         if (ApiUtil.isEmpty(parameters.get(ApiConstants.MONGO_ID))) {
             if (request.getStatus() == null) {
                 request.setStatus(PurchaseRequestOrder.STATUS_DRAFT);
             }
             request.setApprovedDate(null);
             request.setPurchaseOrderCode("order_" + String.valueOf(new Date().getTime()));
             Object eqList = parameters.get(SalesContractBean.SC_EQ_LIST);            
             parameters = request.toMap();
             parameters.put(SalesContractBean.SC_EQ_LIST, eqList);
 
         }
 
         return updatePurchase(parameters, DBBean.PURCHASE_ORDER);
     }
     
     public Map<String, Object> getPurchaseOrder(HashMap<String, Object> parameters){
         Map<String, Object> result =  this.dao.findOne(ApiConstants.MONGO_ID, parameters.get(ApiConstants.MONGO_ID), DBBean.PURCHASE_ORDER);        
         return mergeProjectInfo(result);
     }
 
     public Map<String, Object> approvePurchaseContract(HashMap<String, Object> order) {
         return processRequest(order, DBBean.PURCHASE_CONTRACT, APPROVED);
     }
     
     
     public Map<String, Object> processRequest(HashMap<String, Object> request, String db, String status){
         
         Map<String, Object> cc = dao.findOne(ApiConstants.MONGO_ID, request.get(ApiConstants.MONGO_ID), db);
         request.put(ApiConstants.MONGO_ID, cc.get(ApiConstants.MONGO_ID));
         request.put(PurchaseRequestOrder.PROCESS_STATUS, status);
         request.put(PurchaseRequestOrder.APPROVED_DATE, ApiUtil.formateDate(new Date(), "yyy-MM-dd"));
 
         Map<String, Object> result =  dao.updateById(request, db);
         
         if(cc.get(PurchaseRequestOrder.SALES_CONTRACT_CODE)!=null){
             updateSummaryUnderContract(db, cc.get(PurchaseRequestOrder.SALES_CONTRACT_CODE).toString());
         }
         
         return result;
         
     }
 
     public Map<String, Object> rejectPurchaseContract(HashMap<String, Object> order) {       
         return processRequest(order, DBBean.PURCHASE_CONTRACT, PurchaseRequestOrder.STATUS_REJECTED);
     }
 
     public Map<String, Object> approvePurchaseOrder(HashMap<String, Object> order) {
         return processRequest(order, DBBean.PURCHASE_ORDER, APPROVED);
     }
 
     public Map<String, Object> rejectPurchaseOrder(HashMap<String, Object> order) {
         return processRequest(order, DBBean.PURCHASE_ORDER, PurchaseRequestOrder.STATUS_REJECTED);
     }
 
     /**
      * 
      * 选择已批准的备货申请，返回_id, pbCode, scCode 字段
      * 
      */
     public Map<String, Object> listBackRequestForSelect() {        
         Map<String, Object> query = new HashMap<String, Object>();      
         query.put(PurchaseBack.pbStatus, PurchaseStatus.submited.toString());        
         query.put(ApiConstants.LIMIT_KEYS, new String[] { PurchaseBack.pbCode, PurchaseBack.scCode});
         return dao.list(query, DBBean.PURCHASE_BACK);
     }
     
     
     public Map<String, Object> listPurchaseRequests(){
         
         Map<String, Object> roleQuery = new HashMap<String, Object>();
         
         if(isDepartmentManager()){
             roleQuery.put(PurchaseRequestOrder.PROCESS_STATUS, PurchaseRequestOrder.STATUS_NEW);
         }
         
         if (isPurchase()) {
             roleQuery.put(PurchaseRequestOrder.PROCESS_STATUS, new DBQuery(DBQueryOpertion.IN, new String[] { PurchaseRequestOrder.MANAGER_APPROVED, PurchaseRequestOrder.STATUS_APPROVED, PurchaseRequestOrder.STATUS_REJECTED }));
         }
         
         Map<String, Object> results = dao.list(roleQuery, DBBean.PURCHASE_REQUEST);
         List<Map<String, Object>> list = (List<Map<String, Object>>) results.get(ApiConstants.RESULTS_DATA);
         
         
         
         for (Map<String, Object> data : list) {
      
             Map<String, Object> relatedProjectInfo = getRelatedProjectInfo(data);
 
             if (relatedProjectInfo != null) {
                 data.put("customerName", relatedProjectInfo.get(ProjectBean.PROJECT_CUSTOMER));
                 data.put("projectName", relatedProjectInfo.get("projectName"));
                 data.put("projectManager", relatedProjectInfo.get("projectManager"));
             }
         }
         
         return results;
     }
     
     
     public Map<String, Object> getRelatedProjectInfo(Map<String, Object> params){
 
         //FIXME: code refine
         PurchaseCommonBean request = (PurchaseCommonBean) new PurchaseCommonBean().toEntity(params);
         
         Map<String, Object> project = dao.findOne(ApiConstants.MONGO_ID, request.getProjectId(), DBBean.PROJECT);
 
         Map<String, Object> pmQuery = new HashMap<String, Object>();
         pmQuery.put(ApiConstants.LIMIT_KEYS, new String[] { UserBean.USER_NAME });
         pmQuery.put(ApiConstants.MONGO_ID, project.get(ProjectBean.PROJECT_MANAGER));
 
         Map<String, Object> pmData = dao.findOneByQuery(pmQuery, DBBean.USER);
         project.put(ProjectBean.PROJECT_MANAGER, pmData.get(UserBean.USER_NAME));
 
         
         String cId = (String) project.get(ProjectBean.PROJECT_CUSTOMER);
         
         Map<String, Object> customerQuery = new HashMap<String, Object>();
         customerQuery.put(ApiConstants.LIMIT_KEYS, new String[] {CustomerBean.NAME});
         customerQuery.put(ApiConstants.MONGO_ID, cId);
         Map<String, Object> customerData = dao.findOneByQuery(customerQuery, DBBean.CUSTOMER);
         
         project.put(ProjectBean.PROJECT_CUSTOMER, customerData.get(CustomerBean.NAME));
         
         return project;
     }
     
     public Map<String, Object> listApprovedPurchaseRequestForSelect(){
         Map<String, Object> query = new HashMap<String, Object>();
 //        query.put(PurchaseRequestBean.STATUS, APPROVED);
         query.put(ApiConstants.LIMIT_KEYS, new String[] { "purchaseRequestCode" });
         return dao.list(query, DBBean.PURCHASE_REQUEST);
     }
     
     public Map<String, Object> updatePurchaseRequest(Map<String, Object> parameters) {
         PurchaseRequest request = (PurchaseRequest) new PurchaseRequest().toEntity(parameters);
         if (ApiUtil.isEmpty(parameters.get(ApiConstants.MONGO_ID))) {
             
             if (request.getStatus() == null) {
                 request.setStatus(PurchaseRequestOrder.STATUS_DRAFT);
             }
             
             //根据销售合同id查询项目和客户ID
             Map<String, Object> query = new HashMap<String, Object>();
             query.put(ApiConstants.MONGO_ID, request.getSalesContractId());
             query.put(ApiConstants.LIMIT_KEYS, new String[]{SalesContractBean.SC_PROJECT_ID, SalesContractBean.SC_CUSTOMER_ID});
             
             Map<String, Object> sc = this.dao.findOneByQuery(query, DBBean.SALES_CONTRACT);
             request.setProjectId(sc.get(SalesContractBean.SC_PROJECT_ID).toString());
             request.setPurchaseRequestCode("request_" + String.valueOf(new Date().getTime()));
             Object eqList = parameters.get(SalesContractBean.SC_EQ_LIST);
             parameters = request.toMap();
             parameters.put(SalesContractBean.SC_EQ_LIST, eqList);
         }
         
 
         return updatePurchase(parameters, DBBean.PURCHASE_REQUEST);
     }
     
     public Map<String, Object> updatePurchase(Map<String, Object> parameters, String db) {
         Map<String, Object> result = null;
 
         if (ApiUtil.isEmpty(parameters.get(ApiConstants.MONGO_ID))) {
             result = this.dao.add(parameters, db);
         } else {
             result = dao.updateById(parameters, db);
         }
 
 //        Map<String, Object> cc = this.dao.findOne(ApiConstants.MONGO_ID, result.get(ApiConstants.MONGO_ID), new String[] { PurchaseRequestOrder.SALES_CONTRACT_CODE }, db);
 //        if (cc.get(PurchaseRequestOrder.SALES_CONTRACT_CODE) != null) {
 //            updateSummaryUnderContract(db, cc.get(PurchaseRequestOrder.SALES_CONTRACT_CODE).toString());
 //        }
 
         return result;
 
     }
 
 
     public Map<String, Object> approvePurchaseRequest(HashMap<String, Object> request){
         
         if(isDepartmentManager()){
             return processRequest(request, DBBean.PURCHASE_REQUEST, PurchaseRequestOrder.MANAGER_APPROVED);
         }else{
             return processRequest(request, DBBean.PURCHASE_REQUEST, APPROVED);
         }
     }
     
     public Map<String, Object> cancelPurchaseRequest(HashMap<String, Object> request){
         return processRequest(request, DBBean.PURCHASE_REQUEST, PurchaseRequestOrder.STATUS_CANCELLED);
     }
     
     public Map<String, Object> rejectPurchaseRequest(HashMap<String, Object> request){
         return processRequest(request, DBBean.PURCHASE_REQUEST, PurchaseRequestOrder.STATUS_REJECTED);
     }
     
     public Map<String, Object> getPurchaseRequest(HashMap<String, Object> parameters){
 
         Map<String, Object> result =  this.dao.findOne(ApiConstants.MONGO_ID, parameters.get(ApiConstants.MONGO_ID), DBBean.PURCHASE_REQUEST);
         return mergeProjectInfo(result);
 
     }
 
     
     public void deletePurchaseRequest(Map<String, Object> order){
         
     }
        
     
     public Map<String, Object> getBackRequestForSelect(HashMap<String, Object> parameters) {
         return backService.loadBack(parameters);
 
     }
 
 
     private Map<String, Object> mergeProjectInfo(Map<String, Object> result) {
            
            Map<String, Object> relatedProjectInfo = getRelatedProjectInfo(result);
            
            result.put("projectName", relatedProjectInfo.get(ProjectBean.PROJECT_NAME));
            result.put("projectCode", relatedProjectInfo.get(ProjectBean.PROJECT_CODE));
 
            return result;
     }
     
     
     private void updateSummaryUnderContract(String db, String scId){
         
         Map<String, Object> query = new HashMap<String, Object>();
         query.put(PurchaseRequestOrder.SALES_CONTRACT_CODE, scId);
         query.put(PurchaseRequestOrder.PROCESS_STATUS, new DBQuery(DBQueryOpertion.IN, new String[]{PurchaseRequestOrder.STATUS_APPROVED, PurchaseRequestOrder.STATUS_NEW}));
         //TODO: query requried keys
         //        query.put(ApiConstants.LIMIT_KEYS, new String[]{});
         
         Map<String, Object> results = this.dao.list(query, db);
         
         if(results !=null){
             List<Map<String, Object>> list = (List<Map<String, Object>>) results.get(ApiConstants.RESULTS_DATA);
             
             float totalPercent = 0;
             float totalMoneyPercent = 0;
             
             for(Map<String, Object> result: list){
                 result.put("requestTotalOfCountract", list.size());
                 if(result.get("numbersPercentOfContract")!=null){
                     totalPercent = totalPercent + Float.parseFloat(result.get("numbersPercentOfContract").toString());
                 }
                 
                 if(result.get("moneyPercentOfContract")!=null){
                     totalMoneyPercent = totalMoneyPercent + Float.parseFloat(result.get("moneyPercentOfContract").toString());
                 }
             }
             
             for(Map<String, Object> result: list){
                 result.put("allRequestedNumbersOfCountract", totalPercent);
                 result.put("totalRequestedMoneyOfContract", totalMoneyPercent);
                 this.dao.updateById(result, db);
             }
             
         }
     }
 
 
     @Override
     public Map<String, Object> listRepositoryRequests() {
         return this.dao.list(null, DBBean.REPOSITORY);
     }
 
 
     @Override
     public Map<String, Object> addRepositoryRequest(HashMap<String, Object> parserListJsonParameters) {
         return updatePurchase(parserListJsonParameters, DBBean.REPOSITORY);
         
     }
 
 
     @Override
     public Map<String, Object> getRepositoryRequest(HashMap<String, Object> parameters) {
         return this.dao.findOne(ApiConstants.MONGO_ID, parameters.get(ApiConstants.MONGO_ID), DBBean.REPOSITORY);
     }
 
 
     @Override
     public void deleteRepositoryRequest(HashMap<String, Object> parserJsonParameters) {
         
     }
 
 
     @Override
     public Map<String, Object> updateRepositoryRequest(HashMap<String, Object> parameters) {
         return updatePurchase(parameters, DBBean.REPOSITORY);
         
     }
 
 
     @Override
     public Map<String, Object> approveRepositoryRequest(HashMap<String, Object> parserJsonParameters) {
         return null;
     }
 
 
     @Override
     public Map<String, Object> rejectRepositoryRequest(HashMap<String, Object> parserJsonParameters) {
         return null;
     }
     
     //采购合同列表为付款
 	public Map<String, Object> listSelectForPayment(HashMap<String, Object> params) {
     	Map<String,Object> query = new HashMap<String,Object>();
     	query.put(ApiConstants.LIMIT_KEYS, new String[]{"purchaseContractCode","supplierName"});
         Map<String, Object> results = dao.list(query, DBBean.PURCHASE_CONTRACT);
         List<Map<String, Object>> list = (List<Map<String, Object>>) results.get(ApiConstants.RESULTS_DATA);
         for(Map<String, Object> data: list){
             Map<String, Object> query2 = new HashMap<String, Object>();
             query2.put(ApiConstants.MONGO_ID, data.get("supplierName"));
             Map<String, Object> relatedProjectInfo = this.dao.findOneByQuery(query2, DBBean.SUPPLIER);
             data.put("supplierName", relatedProjectInfo.get("supplierName"));
             data.put("supplierCardName", relatedProjectInfo.get("supplierName"));
             data.put("supplierCardCode", relatedProjectInfo.get("supplierName"));
         }        
         return results;
 	}
 
 	@Override
 	public Map<String, Object> listPaymoney(HashMap<String, Object> params) {
 		//String pcId = (String) params.get(PayMoneyBean.purchaseContractId);
 		Map<String,Object> query1 = new HashMap<String,Object>();
 		Map<String,Object> mapPay = dao.list(query1, DBBean.PAY_MONEY);
 		return mapPay;
 	}
 
 	@Override
 	public Map<String, Object> addPaymoney(HashMap<String, Object> params) {
 		Map<String,Object> obj = new HashMap<String,Object>();
 		obj.put(PayMoneyBean.payMoney, params.get(PayMoneyBean.payMoney));
 		obj.put(PayMoneyBean.payDate, params.get(PayMoneyBean.payDate));
 		obj.put(PayMoneyBean.purchaseContractId, params.get(PayMoneyBean.purchaseContractId));
 		obj.put(PayMoneyBean.supplierCardCode, params.get(PayMoneyBean.supplierCardCode));
 		obj.put(PayMoneyBean.supplierCardName, params.get(PayMoneyBean.supplierCardName));
 		return dao.add(params, DBBean.PAY_MONEY);
 	}
 
 	@Override
 	public Map<String, Object> updatePaymoney(HashMap<String, Object> params) {
 		Map<String,Object> obj = new HashMap<String,Object>();
 		obj.put(ApiConstants.MONGO_ID, params.get(ApiConstants.MONGO_ID));
 		obj.put(PayMoneyBean.payMoney, params.get(PayMoneyBean.payMoney));
 		obj.put(PayMoneyBean.payDate, params.get(PayMoneyBean.payDate));
 		obj.put(PayMoneyBean.purchaseContractId, params.get(PayMoneyBean.purchaseContractId));
 		obj.put(PayMoneyBean.supplierCardCode, params.get(PayMoneyBean.supplierCardCode));
 		obj.put(PayMoneyBean.supplierCardName, params.get(PayMoneyBean.supplierCardName));
 		return dao.updateById(params, DBBean.PAY_MONEY);
 	}
 
 	public ISalesContractService getScs() {
         return scs;
     }
 
 
     public void setScs(ISalesContractService scs) {
         this.scs = scs;
     }
 
 
     public IPurchaseService getBackService() {
         return backService;
     }
 
 
     public void setBackService(IPurchaseService backService) {
         this.backService = backService;
     }
 
     
     
 
 }
