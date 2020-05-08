 package com.pms.service.service.impl;
 
 import java.text.ParseException;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import javax.print.attribute.standard.OrientationRequested;
 
 import org.apache.logging.log4j.LogManager;
 import org.apache.logging.log4j.Logger;
 
 import com.pms.service.dbhelper.DBQuery;
 import com.pms.service.dbhelper.DBQueryOpertion;
 import com.pms.service.mockbean.ApiConstants;
 import com.pms.service.mockbean.CustomerBean;
 import com.pms.service.mockbean.DBBean;
 import com.pms.service.mockbean.InvoiceBean;
 import com.pms.service.mockbean.PayMoneyBean;
 import com.pms.service.mockbean.ProjectBean;
 import com.pms.service.mockbean.PurchaseBack;
 import com.pms.service.mockbean.PurchaseCommonBean;
 import com.pms.service.mockbean.PurchaseRequest;
 import com.pms.service.mockbean.SalesContractBean;
 import com.pms.service.mockbean.UserBean;
 import com.pms.service.service.AbstractService;
 import com.pms.service.service.IPurchaseContractService;
 import com.pms.service.service.IPurchaseService;
 import com.pms.service.service.impl.PurchaseServiceImpl.PurchaseStatus;
 import com.pms.service.util.ApiUtil;
 import com.pms.service.util.DateUtil;
 
 public class PurchaseContractServiceImpl extends AbstractService implements IPurchaseContractService {
 
     private static final String APPROVED = PurchaseRequest.STATUS_APPROVED;
     private static final Logger logger = LogManager.getLogger(PurchaseContractServiceImpl.class);
 
     private IPurchaseService backService;
 
     @Override
     public String geValidatorFileName() {
         return null;
     }
 
     @Override
     public Map<String, Object> listPurchaseContracts(Map<String, Object> parameters) {
         // mergeDataRoleQuery(parameters);
         Map<String, Object> results = dao.list(parameters, DBBean.PURCHASE_CONTRACT);
         List<Map<String, Object>> list = (List<Map<String, Object>>) results.get(ApiConstants.RESULTS_DATA);
 
         for (Map<String, Object> data : list) {
             Map<String, Object> query = new HashMap<String, Object>();
             query.put(ApiConstants.MONGO_ID, data.get("supplier"));
 
             Map<String, Object> supplier = this.dao.findOneByQuery(query, DBBean.SUPPLIER);
 
             if (supplier != null) {
                 data.put("supplierName", supplier.get("supplierName"));
             }
         }
         return results;
 
     }
 
     public Map<String, Object> listContractsForRepositorySelect() {
         Map<String, Object> query = new HashMap<String, Object>();
         query.put(PurchaseCommonBean.PROCESS_STATUS, PurchaseCommonBean.STATUS_APPROVED);
         query.put(ApiConstants.LIMIT_KEYS, new String[] { "eqcostList.projectId", "supplier" });
 
         Map<String, Object> results = dao.list(query, DBBean.PURCHASE_CONTRACT);
 
         List<Map<String, Object>> list = (List<Map<String, Object>>) results.get(ApiConstants.RESULTS_DATA);
 
         List<String> projectIds = new ArrayList<String>();
         for (Map<String, Object> data : list) {
             List<Map<String, Object>> pList = (List<Map<String, Object>>) data.get("eqcostList");
 
             for (Map<String, Object> p : pList) {
                 projectIds.add(p.get("projectId").toString());
             }
         }
 
         Map<String, Object> projectQuery = new HashMap<String, Object>();
         projectQuery.put(ApiConstants.MONGO_ID, new DBQuery(DBQueryOpertion.IN, projectIds));
         projectQuery.put(ApiConstants.LIMIT_KEYS, ProjectBean.PROJECT_NAME);
 
         Map<String, Object> projects = this.dao.list(projectQuery, DBBean.PROJECT);
         return projects;
     }
 
     public Map<String, Object> listContractsSuppliersByProjectId(Map<String, Object> contract) {
         Map<String, Object> query = new HashMap<String, Object>();
         query.put(PurchaseCommonBean.PROCESS_STATUS, PurchaseCommonBean.STATUS_APPROVED);
         query.put(ApiConstants.LIMIT_KEYS, new String[] { "supplier" });
         query.put("eqcostList.projectId", contract.get("projectId"));
 
         Map<String, Object> results = dao.list(query, DBBean.PURCHASE_CONTRACT);
 
         List<Map<String, Object>> list = (List<Map<String, Object>>) results.get(ApiConstants.RESULTS_DATA);
 
         List<Object> supplierIds = new ArrayList<Object>();
         for (Map<String, Object> data : list) {
             supplierIds.add(data.get("supplier"));
         }
 
         Map<String, Object> supplierQuery = new HashMap<String, Object>();
         supplierQuery.put(ApiConstants.MONGO_ID, new DBQuery(DBQueryOpertion.IN, supplierIds));
         supplierQuery.put(ApiConstants.LIMIT_KEYS, "supplierName");
 
         Map<String, Object> suppliers = this.dao.list(supplierQuery, DBBean.SUPPLIER);
 
         return suppliers;
 
     }
 
     public Map<String, Object> listSalesContractsForShipSelect(Map<String, Object> params) {
 
         // if(params.get("type") ==null ){
         // return new HashMap<String, Object>();
         // } query.put("type", value)
 
         Map<String, Object> query = new HashMap<String, Object>();
         // query.put(PurchaseCommonBean.PROCESS_STATUS, PurchaseCommonBean.STATUS_OUT_REPOSITORY);
         query.put(ApiConstants.LIMIT_KEYS, new String[] { "eqcostList.scId", "eqcostList.projectId" });
         Set<Object> scIdsList = new HashSet();
 
         String db = DBBean.REPOSITORY;
         // if(params.get("type").toString().equalsIgnoreCase("1")){
         // db = DBBean.REPOSITORY
         // }
 
         List<Object> scResults = this.dao.listLimitKeyValues(query, DBBean.REPOSITORY);
 
         // 2个LIMIT_KEYS字段以上是返回的list中的对象是MAP
         for (Object sc : scResults) {
             Map<String, Object> scMap = (Map<String, Object>) sc;
             List<Map<String, Object>> list = (List<Map<String, Object>>) scMap.get(SalesContractBean.SC_EQ_LIST);
             for (Map<String, Object> eqsc : list) {
                 scIdsList.add(eqsc.get(SalesContractBean.SC_ID));
             }
 
         }
 
         Map<String, Object> scQuery = new HashMap<String, Object>();
         scQuery.put(ApiConstants.MONGO_ID, new DBQuery(DBQueryOpertion.IN, new ArrayList<Object>(scIdsList)));
         scQuery.put(ApiConstants.LIMIT_KEYS, new String[] { SalesContractBean.SC_CODE, SalesContractBean.SC_PROJECT_ID, SalesContractBean.SC_TYPE });
         Map<String, Object> scList = this.dao.list(scQuery, DBBean.SALES_CONTRACT);
 
         Map<String, Object> customers = this.dao.listToOneMapAndIdAsKey(null, DBBean.CUSTOMER);
         Map<String, Object> projects = this.dao.listToOneMapAndIdAsKey(null, DBBean.PROJECT);
 
         List<Map<String, Object>> scListItems = (List<Map<String, Object>>) scList.get(ApiConstants.RESULTS_DATA);
 
         for (Map<String, Object> scMap : scListItems) {
             Map<String, Object> project = (Map<String, Object>) projects.get(scMap.get(SalesContractBean.SC_PROJECT_ID));
 
             scMap.put(ProjectBean.PROJECT_NAME, project.get(ProjectBean.PROJECT_NAME));
             scMap.put(SalesContractBean.SC_PROJECT_ID, project.get(ApiConstants.MONGO_ID));
 
             Map<String, Object> customer = (Map<String, Object>) customers.get(project.get(ProjectBean.PROJECT_CUSTOMER));
             scMap.put("customerName", customer.get(CustomerBean.NAME));
             scMap.put(SalesContractBean.SC_CUSTOMER_ID, project.get(ProjectBean.PROJECT_CUSTOMER));
 
         }
 
         return scList;
 
     }
 
     public Map<String, Object> listEqcostListForShipByScIDAndType(Map<String, Object> params) {
 
         // String scId = params.get(SalesContractBean.SC_ID).toString();
         // String type = params.get("type").toString();
 
         Map<String, Object> query = new HashMap<String, Object>();
         // query.put(PurchaseCommonBean.PROCESS_STATUS,
         // PurchaseCommonBean.STATUS_OUT_REPOSITORY);
         query.put("eqcostList.scId", params.get(SalesContractBean.SC_ID));
         query.put(ApiConstants.LIMIT_KEYS, new String[] { "eqcostList" });
         List<Object> scResults = this.dao.listLimitKeyValues(query, DBBean.REPOSITORY);
 
         Map<String, Object> scQuery = new HashMap<String, Object>();
         scQuery.put(ApiConstants.LIMIT_KEYS, new String[] { SalesContractBean.SC_PROJECT_ID });
         scQuery.put(ApiConstants.MONGO_ID, params.get(SalesContractBean.SC_ID));
         Map<String, Object> map = this.dao.findOneByQuery(scQuery, DBBean.SALES_CONTRACT);
 
         Map<String, Object> customers = this.dao.listToOneMapAndIdAsKey(null, DBBean.CUSTOMER);
 
         List<Map<String, Object>> eqList = new ArrayList<Map<String, Object>>();
         for (Object eq : scResults) {
             List<Map<String, Object>> eqMaps = (List<Map<String, Object>>) eq;
 
             for (Map<String, Object> eqmap : eqMaps) {
                 if (eqmap.get(SalesContractBean.SC_ID).toString().equalsIgnoreCase(params.get(SalesContractBean.SC_ID).toString())) {
                     eqList.add(eqmap);
                 }
             }
         }
 
         Map<String, Object> proQuery = new HashMap<String, Object>();
         proQuery.put(ApiConstants.LIMIT_KEYS, new String[] { ProjectBean.PROJECT_CUSTOMER, ProjectBean.PROJECT_NAME });
         proQuery.put(ApiConstants.MONGO_ID, map.get(SalesContractBean.SC_PROJECT_ID));
         Map<String, Object> project = this.dao.findOneByQuery(proQuery, DBBean.PROJECT);
 
         Map<String, Object> eqResult = new HashMap<String, Object>();
         eqResult.put(SalesContractBean.SC_CUSTOMER_ID, project.get(ProjectBean.PROJECT_CUSTOMER));
         Map<String, Object> customer = (Map<String, Object>) customers.get(project.get(ProjectBean.PROJECT_CUSTOMER));
         eqResult.put("customerName", customer.get(CustomerBean.NAME));
         eqResult.put(SalesContractBean.SC_PROJECT_ID, project.get(ApiConstants.MONGO_ID));
         eqResult.put(ProjectBean.PROJECT_NAME, project.get(ProjectBean.PROJECT_NAME));
         eqResult.put(SalesContractBean.SC_EQ_LIST, eqList);
 
         logger.info(eqResult);
 
         return eqResult;
     }
 
     public Map<String, Object> listContractsByProjectAndSupplier(Map<String, Object> params) {
         Map<String, Object> query = new HashMap<String, Object>();
         Object projectId = params.get("projectId");
         query.put("eqcostList.projectId", projectId);
         query.put("supplier", params.get("supplier"));
         query.put(PurchaseCommonBean.PROCESS_STATUS, PurchaseCommonBean.STATUS_APPROVED);
 
         Map<String, Object> results = this.dao.list(query, DBBean.PURCHASE_CONTRACT);
         List<Map<String, Object>> list = (List<Map<String, Object>>) results.get(ApiConstants.RESULTS_DATA);
 
         List<Map<String, Object>> eqclist = new ArrayList<Map<String, Object>>();
 
         for (Map<String, Object> data : list) {
             List<Map<String, Object>> pList = (List<Map<String, Object>>) data.get("eqcostList");
 
             for (Map<String, Object> p : pList) {
                 if (p.get("projectId").equals(projectId)) {
                     eqclist.add(p);
                 }
             }
         }
 
         Map<String, Object> lresult = new HashMap<String, Object>();
         lresult.put("data", eqclist);
         return lresult;
 
     }
 
     public Map<String, Object> listContractsByProjectId(Map<String, Object> contract) {
 
         Map<String, Object> query = new HashMap<String, Object>();
         query.put(PurchaseCommonBean.PROCESS_STATUS, PurchaseCommonBean.STATUS_APPROVED);
         query.put("eqcostList.projectId", contract.get("projectId"));
 
         return this.dao.list(query, DBBean.PURCHASE_CONTRACT);
 
     }
 
     public List<Map<String, Object>> listApprovedPurchaseContractCosts(String salesContractId) {
 
         Map<String, Object> query = new HashMap<String, Object>();
         query.put("eqcostList.scId", salesContractId);
         query.put(PurchaseCommonBean.PROCESS_STATUS, PurchaseCommonBean.STATUS_APPROVED);
         query.put(ApiConstants.LIMIT_KEYS, new String[] { SalesContractBean.SC_EQ_LIST });
 
         Map<String, Object> results = this.dao.list(query, DBBean.PURCHASE_CONTRACT);
         List<Map<String, Object>> list = (List<Map<String, Object>>) results.get(ApiConstants.RESULTS_DATA);
 
         List<Map<String, Object>> eqclist = new ArrayList<Map<String, Object>>();
 
         if (list != null) {
             for (Map<String, Object> data : list) {
                 List<Map<String, Object>> pList = (List<Map<String, Object>>) data.get("eqcostList");
 
                 for (Map<String, Object> p : pList) {
                     if (p.get("scId").equals(salesContractId)) {
                         eqclist.add(p);
                     }
                 }
             }
         }
 
         return eqclist;
     }
 
     public Map<String, Object> getPurchaseContract(Map<String, Object> parameters) {
         Map<String, Object>  result = this.dao.findOne(ApiConstants.MONGO_ID, parameters.get(ApiConstants.MONGO_ID), DBBean.PURCHASE_CONTRACT);
         result.put(SalesContractBean.SC_EQ_LIST, mergeLoadedEqList(result.get(SalesContractBean.SC_EQ_LIST)));
         return result;
     }
 
     @Override
     public void deletePurchaseContract(Map<String, Object> contract) {
         List<String> ids = new ArrayList<String>();
         ids.add(contract.get(ApiConstants.MONGO_ID).toString());
         dao.deleteByIds(ids, DBBean.PURCHASE_CONTRACT);
 
     }
 
     @Override
     public Map<String, Object> updatePurchaseContract(Map<String, Object> contract) {
         Object eqList = contract.get(SalesContractBean.SC_EQ_LIST);
 
         String keys[] = new String[] { "eqcostApplyAmount", "orderEqcostCode", "orderEqcostName", "orderEqcostModel", "eqcostProductUnitPrice", "purchaseOrderCode",
                 "salesContractCode", "purchaseOrderId" };
         contract.put(SalesContractBean.SC_EQ_LIST, mergeSavedEqList(keys, eqList));
 
         return updatePurchase(contract, DBBean.PURCHASE_CONTRACT);
     }
 
     @Override
     public Map<String, Object> listPurchaseOrders(Map<String, Object> parameters) {
         mergeDataRoleQuery(parameters);
         Map<String, Object> results = dao.list(parameters, DBBean.PURCHASE_ORDER);
         List<Map<String, Object>> list = (List<Map<String, Object>>) results.get(ApiConstants.RESULTS_DATA);
 
         for (Map<String, Object> data : list) {
 
             Map<String, Object> relatedProjectInfo = getRelatedProjectInfo(data);
             data.put("customerName", relatedProjectInfo.get(ProjectBean.PROJECT_CUSTOMER));
             data.put("projectName", relatedProjectInfo.get("projectName"));
             data.put("projectManager", relatedProjectInfo.get("projectManager"));
         }
 
         return results;
 
     }
 
     public Map<String, Object> listApprovedPurchaseOrderForSelect() {
 
         Map<String, Object> query = new HashMap<String, Object>();
         query.put(PurchaseRequest.PROCESS_STATUS, PurchaseRequest.STATUS_APPROVED);
        Map<String, Object> results = dao.list(query, DBBean.PURCHASE_ORDER);
        List<Map<String, Object>> list = (List<Map<String, Object>>) results.get(ApiConstants.RESULTS_DATA);

        for (Map<String, Object> data : list) {
            data.put(SalesContractBean.SC_EQ_LIST, mergeLoadedEqList(data.get(SalesContractBean.SC_EQ_LIST)));
 
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
         Object eqList = parameters.get(SalesContractBean.SC_EQ_LIST);
 
         if (ApiUtil.isEmpty(parameters.get(ApiConstants.MONGO_ID))) {
             if (request.getStatus() == null) {
                 request.setStatus(PurchaseRequest.STATUS_DRAFT);
             }
             request.setApprovedDate(null);
             request.setPurchaseOrderCode(generateCode("CGDD", DBBean.PURCHASE_ORDER));
             parameters = request.toMap();
 
         }
         
         String keys[] = new String[] { "eqcostApplyAmount", "orderEqcostCode", "orderEqcostName", "orderEqcostModel", "eqcostProductUnitPrice" };
         parameters.put(SalesContractBean.SC_EQ_LIST, mergeSavedEqList(keys, eqList));
 
         Map<String, Object> order = updatePurchase(parameters, DBBean.PURCHASE_ORDER);
 
         Map<String, Object> prequest = this.dao.findOne(ApiConstants.MONGO_ID, order.get(PurchaseCommonBean.PURCHASE_REQUEST_ID),
                 new String[] { PurchaseCommonBean.PURCHASE_ORDER_ID }, DBBean.PURCHASE_REQUEST);
 
         if (prequest != null) {
             prequest.put(PurchaseCommonBean.PURCHASE_ORDER_ID, order.get(ApiConstants.MONGO_ID));
             prequest.put(PurchaseCommonBean.PURCHASE_ORDER_CODE, order.get(PurchaseCommonBean.PURCHASE_ORDER_CODE));
             this.dao.updateById(prequest, DBBean.PURCHASE_REQUEST);
         }
 
         return order;
     }
 
     private List<Map<String, Object>> mergeSavedEqList(String keys[], Object eqList) {
         List<String> finalKeys = new ArrayList<String>();
         for (String key : keys) {
             finalKeys.add(key);
         }
         finalKeys.add(ApiConstants.MONGO_ID);
         finalKeys.add(SalesContractBean.SC_ID);
         finalKeys.add(SalesContractBean.SC_PROJECT_ID);
 
         List<Map<String, Object>> orgin = (List<Map<String, Object>>) eqList;
         List<Map<String, Object>> maps = new ArrayList<Map<String, Object>>();
 
         for (Map<String, Object> old : orgin) {
             Map<String, Object> neq = new HashMap<String, Object>();
             for (String key : finalKeys) {
                 neq.put(key, old.get(key));
             }
             maps.add(neq);
         }
 
         return maps;
     }
 
     private List<Map<String, Object>> mergeLoadedEqList(Object eqList) {
         List<Map<String, Object>> orgin = (List<Map<String, Object>>) eqList;
 
         List<Map<String, Object>> mapLists = new ArrayList<Map<String, Object>>();
         Set<String> ids = new HashSet<String>();
 
         for (Map<String, Object> old : orgin) {
             if(old.get(ApiConstants.MONGO_ID)!=null){
                 ids.add(old.get(ApiConstants.MONGO_ID).toString());
             }
         }
 
         Map<String, Object> query = new HashMap<String, Object>();
         query.put(ApiConstants.MONGO_ID, new DBQuery(DBQueryOpertion.IN, new ArrayList<String>(ids)));
 
         List<Object> scEqList = this.dao.listLimitKeyValues(query, DBBean.EQ_COST);
 
         for (Object obj : scEqList) {
             Map<String, Object> scEq = (Map<String, Object>) obj;
 
             for (Map<String, Object> savedEq : orgin) {
 
                 if (savedEq.get(ApiConstants.MONGO_ID).toString().equalsIgnoreCase(scEq.get(ApiConstants.MONGO_ID).toString())) {
                     scEq.putAll(savedEq);
                     break;
                 }
             }
 
             mapLists.add(scEq);
         }
 
         return mapLists;
     }
 
     public Map<String, Object> getPurchaseOrder(Map<String, Object> parameters) {
         Map<String, Object> result = this.dao.findOne(ApiConstants.MONGO_ID, parameters.get(ApiConstants.MONGO_ID), DBBean.PURCHASE_ORDER);
         result.put(SalesContractBean.SC_EQ_LIST, mergeLoadedEqList(result.get(SalesContractBean.SC_EQ_LIST)));
         return mergeProjectInfo(result);
     }
 
     public Map<String, Object> approvePurchaseContract(Map<String, Object> order) {
         return processRequest(order, DBBean.PURCHASE_CONTRACT, APPROVED);
     }
 
     public Map<String, Object> processRequest(Map<String, Object> request, String db, String status) {
 
         Map<String, Object> cc = dao.findOne(ApiConstants.MONGO_ID, request.get(ApiConstants.MONGO_ID), db);
         request.put(ApiConstants.MONGO_ID, cc.get(ApiConstants.MONGO_ID));
         request.put(PurchaseRequest.PROCESS_STATUS, status);
         request.put(PurchaseRequest.APPROVED_DATE, ApiUtil.formateDate(new Date(), "yyy-MM-dd"));
 
         Map<String, Object> result = dao.updateById(request, db);
 
         if (cc.get(PurchaseRequest.SALES_CONTRACT_CODE) != null) {
             updateSummaryUnderContract(db, cc.get(PurchaseRequest.SALES_CONTRACT_CODE).toString());
         }
 
         return result;
     }
 
     public Map<String, Object> rejectPurchaseContract(Map<String, Object> order) {
         return processRequest(order, DBBean.PURCHASE_CONTRACT, PurchaseRequest.STATUS_REJECTED);
     }
 
     public Map<String, Object> approvePurchaseOrder(Map<String, Object> order) {
         return processRequest(order, DBBean.PURCHASE_ORDER, APPROVED);
     }
 
     public Map<String, Object> rejectPurchaseOrder(Map<String, Object> order) {
         return processRequest(order, DBBean.PURCHASE_ORDER, PurchaseRequest.STATUS_REJECTED);
     }
 
     /**
      * 
      * 选择已批准的备货申请，返回_id, pbCode, scCode 字段
      * 
      */
     public Map<String, Object> listBackRequestForSelect() {
         Map<String, Object> query = new HashMap<String, Object>();
         query.put(PurchaseBack.pbStatus, PurchaseStatus.submited.toString());
         query.put(PurchaseBack.prId, null);
         query.put(ApiConstants.LIMIT_KEYS, new String[] { PurchaseBack.pbCode, PurchaseBack.scCode });
         return dao.list(query, DBBean.PURCHASE_BACK);
     }
 
     public Map<String, Object> listPurchaseRequests(Map<String, Object> params) {
 
         if (params.get("approvePage") != null) {
             params.remove("approvePage");
             params.put(PurchaseRequest.PROCESS_STATUS, new DBQuery(DBQueryOpertion.NOT_IN, new String[] { PurchaseRequest.STATUS_DRAFT, PurchaseRequest.STATUS_CANCELLED }));
         }
 
         if (isSalesManager()) {
             params.put(PurchaseRequest.PROCESS_STATUS, PurchaseRequest.STATUS_NEW);
         }
 
         if (isPurchase()) {
             params.put(PurchaseRequest.PROCESS_STATUS, new DBQuery(DBQueryOpertion.IN, new String[] { PurchaseRequest.MANAGER_APPROVED, PurchaseRequest.STATUS_APPROVED,
                     PurchaseRequest.STATUS_REJECTED }));
         }
         mergeDataRoleQuery(params);
         Map<String, Object> results = dao.list(params, DBBean.PURCHASE_REQUEST);
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
 
     public Map<String, Object> getRelatedProjectInfo(Map<String, Object> params) {
 
         // FIXME: code refine
         PurchaseCommonBean request = (PurchaseCommonBean) new PurchaseCommonBean().toEntity(params);
 
         Map<String, Object> project = dao.findOne(ApiConstants.MONGO_ID, request.getProjectId(), DBBean.PROJECT);
 
         Map<String, Object> pmQuery = new HashMap<String, Object>();
         pmQuery.put(ApiConstants.LIMIT_KEYS, new String[] { UserBean.USER_NAME });
         pmQuery.put(ApiConstants.MONGO_ID, project.get(ProjectBean.PROJECT_MANAGER));
 
         Map<String, Object> pmData = dao.findOneByQuery(pmQuery, DBBean.USER);
         project.put(ProjectBean.PROJECT_MANAGER, pmData.get(UserBean.USER_NAME));
 
         String cId = (String) project.get(ProjectBean.PROJECT_CUSTOMER);
 
         Map<String, Object> customerQuery = new HashMap<String, Object>();
         customerQuery.put(ApiConstants.LIMIT_KEYS, new String[] { CustomerBean.NAME });
         customerQuery.put(ApiConstants.MONGO_ID, cId);
         Map<String, Object> customerData = dao.findOneByQuery(customerQuery, DBBean.CUSTOMER);
 
         project.put(ProjectBean.PROJECT_CUSTOMER, customerData.get(CustomerBean.NAME));
 
         return project;
     }
 
     public Map<String, Object> listApprovedPurchaseRequestForSelect() {
         Map<String, Object> query = new HashMap<String, Object>();
         query.put(PurchaseCommonBean.PROCESS_STATUS, APPROVED);
         query.put(PurchaseCommonBean.PURCHASE_ORDER_ID, null);
         query.put(ApiConstants.LIMIT_KEYS, new String[] { "purchaseRequestCode" });
         return dao.list(query, DBBean.PURCHASE_REQUEST);
     }
 
     public Map<String, Object> updatePurchaseRequest(Map<String, Object> parameters) {
         PurchaseRequest request = (PurchaseRequest) new PurchaseRequest().toEntity(parameters);
         boolean adding = false;
         Object eqList = parameters.get(SalesContractBean.SC_EQ_LIST);
 
         if (ApiUtil.isEmpty(parameters.get(ApiConstants.MONGO_ID))) {
 
             if (request.getStatus() == null) {
                 request.setStatus(PurchaseRequest.STATUS_DRAFT);
             }
 
             // 根据销售合同id查询项目和客户ID
             Map<String, Object> query = new HashMap<String, Object>();
             query.put(ApiConstants.MONGO_ID, request.getSalesContractId());
             query.put(ApiConstants.LIMIT_KEYS, new String[] { SalesContractBean.SC_PROJECT_ID, SalesContractBean.SC_CUSTOMER_ID, SalesContractBean.SC_TYPE });
 
             Map<String, Object> sc = this.dao.findOneByQuery(query, DBBean.SALES_CONTRACT);
             request.setProjectId(sc.get(SalesContractBean.SC_PROJECT_ID).toString());
             request.setPurchaseRequestCode(generateCode("CGSQ", DBBean.PURCHASE_ORDER));
 
             parameters = request.toMap();
 
             adding = true;
         }
         String keys[] = new String[] { "eqcostApplyAmount", "orderEqcostCode", "orderEqcostName", "orderEqcostModel", "eqcostProductUnitPrice" };
         parameters.put(SalesContractBean.SC_EQ_LIST, mergeSavedEqList(keys, eqList));
 
         Map<String, Object> prequest = updatePurchase(parameters, DBBean.PURCHASE_REQUEST);
 
         if (adding) {
             updateRequestIdForBack(prequest);
         }
 
         return prequest;
     }
 
     private void updateRequestIdForBack(Map<String, Object> prequest) {
         Map<String, Object> back = this.dao.findOne(ApiConstants.MONGO_ID, prequest.get("backRequestId"), new String[] { PurchaseBack.prId }, DBBean.PURCHASE_BACK);
         back.put(PurchaseBack.prId, prequest.get(ApiConstants.MONGO_ID));
         this.dao.updateById(back, DBBean.PURCHASE_BACK);
     }
 
     public Map<String, Object> updatePurchase(Map<String, Object> parameters, String db) {
 
         if (parameters.get(PurchaseCommonBean.SALES_COUNTRACT_ID) != null) {
             scs.mergeCommonFieldsFromSc(parameters, parameters.get(PurchaseCommonBean.SALES_COUNTRACT_ID));
 
         }
         Map<String, Object> result = null;
 
         if (ApiUtil.isEmpty(parameters.get(ApiConstants.MONGO_ID))) {
             result = this.dao.add(parameters, db);
         } else {
             result = dao.updateById(parameters, db);
         }
 
         // Map<String, Object> cc = this.dao.findOne(ApiConstants.MONGO_ID, result.get(ApiConstants.MONGO_ID), new
         // String[] { PurchaseRequestOrder.SALES_CONTRACT_CODE }, db);
         // if (cc.get(PurchaseRequestOrder.SALES_CONTRACT_CODE) != null) {
         // updateSummaryUnderContract(db, cc.get(PurchaseRequestOrder.SALES_CONTRACT_CODE).toString());
         // }
 
         return result;
 
     }
 
     public Map<String, Object> approvePurchaseRequest(Map<String, Object> request) {
 
         if (!isPurchase() && !isAdmin()) {
             return processRequest(request, DBBean.PURCHASE_REQUEST, PurchaseRequest.MANAGER_APPROVED);
         } else {
             return processRequest(request, DBBean.PURCHASE_REQUEST, APPROVED);
         }
     }
 
     public Map<String, Object> cancelPurchaseRequest(Map<String, Object> request) {
         return processRequest(request, DBBean.PURCHASE_REQUEST, PurchaseRequest.STATUS_CANCELLED);
     }
 
     public Map<String, Object> rejectPurchaseRequest(Map<String, Object> request) {
         return processRequest(request, DBBean.PURCHASE_REQUEST, PurchaseRequest.STATUS_REJECTED);
     }
 
     public Map<String, Object> getPurchaseRequest(Map<String, Object> parameters) {
 
         Map<String, Object> result = this.dao.findOne(ApiConstants.MONGO_ID, parameters.get(ApiConstants.MONGO_ID), DBBean.PURCHASE_REQUEST);
 
         result.put(SalesContractBean.SC_EQ_LIST, mergeLoadedEqList(result.get(SalesContractBean.SC_EQ_LIST)));
         return mergeProjectInfo(result);
 
     }
 
     public void deletePurchaseRequest(Map<String, Object> order) {
 
     }
 
     public Map<String, Object> getBackRequestForSelect(Map<String, Object> parameters) {
         return backService.loadBack(parameters);
 
     }
 
     private Map<String, Object> mergeProjectInfo(Map<String, Object> result) {
 
         Map<String, Object> relatedProjectInfo = getRelatedProjectInfo(result);
 
         result.put("projectName", relatedProjectInfo.get(ProjectBean.PROJECT_NAME));
         result.put("projectCode", relatedProjectInfo.get(ProjectBean.PROJECT_CODE));
 
         return result;
     }
 
     private void updateSummaryUnderContract(String db, String scId) {
 
         Map<String, Object> query = new HashMap<String, Object>();
         query.put(PurchaseRequest.SALES_CONTRACT_CODE, scId);
         query.put(PurchaseRequest.PROCESS_STATUS, new DBQuery(DBQueryOpertion.IN, new String[] { PurchaseRequest.STATUS_APPROVED, PurchaseRequest.STATUS_NEW }));
         // TODO: query requried keys
         // query.put(ApiConstants.LIMIT_KEYS, new String[]{});
 
         Map<String, Object> results = this.dao.list(query, db);
 
         if (results != null) {
             List<Map<String, Object>> list = (List<Map<String, Object>>) results.get(ApiConstants.RESULTS_DATA);
 
             float totalPercent = 0;
             float totalMoneyPercent = 0;
 
             for (Map<String, Object> result : list) {
                 result.put("requestTotalOfCountract", list.size());
                 if (result.get("numbersPercentOfContract") != null) {
                     totalPercent = totalPercent + Float.parseFloat(result.get("numbersPercentOfContract").toString());
                 }
 
                 if (result.get("moneyPercentOfContract") != null) {
                     totalMoneyPercent = totalMoneyPercent + Float.parseFloat(result.get("moneyPercentOfContract").toString());
                 }
             }
 
             for (Map<String, Object> result : list) {
                 result.put("allRequestedNumbersOfCountract", totalPercent);
                 result.put("totalRequestedMoneyOfContract", totalMoneyPercent);
                 this.dao.updateById(result, db);
             }
 
         }
     }
 
     @Override
     public Map<String, Object> listRepositoryRequests(Map<String, Object> params) {
         return this.dao.list(params, DBBean.REPOSITORY);
     }
 
     public Map<String, Object> listRepositoryByProjectId(Map<String, Object> params) {
         Map<String, Object> query = new HashMap<String, Object>();
         query.put(PurchaseRequest.PROCESS_STATUS, PurchaseRequest.STATUS_IN_REPOSITORY);
         query.put(PurchaseRequest.PROJECT_ID, params.get(PurchaseRequest.PROJECT_ID));
 
         Map<String, Object> result = this.dao.list(query, DBBean.REPOSITORY);
         List<Map<String, Object>> finalEqList = new ArrayList<Map<String, Object>>();
 
         if (result != null) {
             List<Map<String, Object>> list = (List<Map<String, Object>>) result.get(ApiConstants.RESULTS_DATA);
 
             for (Map<String, Object> res : list) {
                 if (res.get(SalesContractBean.SC_EQ_LIST) != null) {
                     List<Map<String, Object>> eqList = (List<Map<String, Object>>) res.get(SalesContractBean.SC_EQ_LIST);
                     finalEqList.addAll(eqList);
 
                 }
             }
 
         }
         Map<String, Object> rep = new HashMap<String, Object>();
         rep.put(ApiConstants.RESULTS_DATA, finalEqList);
         return rep;
     }
 
     @Override
     public Map<String, Object> addRepositoryRequest(Map<String, Object> parserListJsonParameters) {
         return updatePurchase(parserListJsonParameters, DBBean.REPOSITORY);
 
     }
 
     @Override
     public Map<String, Object> getRepositoryRequest(Map<String, Object> parameters) {
         Map<String, Object>  result = this.dao.findOne(ApiConstants.MONGO_ID, parameters.get(ApiConstants.MONGO_ID), DBBean.REPOSITORY);
         result.put(SalesContractBean.SC_EQ_LIST, mergeLoadedEqList(result.get(SalesContractBean.SC_EQ_LIST)));
         return result;
     }
 
     @Override
     public void deleteRepositoryRequest(Map<String, Object> parserJsonParameters) {
 
     }
 
     @Override
     public Map<String, Object> updateRepositoryRequest(Map<String, Object> parameters) {
         if (parameters.get("inDate") != null) {
             try {
                 parameters.put("inDate", DateUtil.getDate(parameters.get("inDate").toString()));
             } catch (ParseException e) {
                 logger.error(e);
             }
 
         }
 
         return updatePurchase(parameters, DBBean.REPOSITORY);
 
     }
 
     @Override
     public Map<String, Object> approveRepositoryRequest(Map<String, Object> params) {
         if (params.get("type") != null && params.get("type").toString().equalsIgnoreCase("in")) {
             return processRequest(params, DBBean.REPOSITORY, PurchaseRequest.STATUS_IN_REPOSITORY);
         } else {
             return processRequest(params, DBBean.REPOSITORY, PurchaseRequest.STATUS_OUT_REPOSITORY);
         }
     }
 
     public Map<String, Object> listProjectsFromRepositoryIn(Map<String, Object> params) {
         Map<String, Object> query = new HashMap<String, Object>();
         query.put(PurchaseRequest.PROCESS_STATUS, PurchaseRequest.STATUS_IN_REPOSITORY);
         query.put(ApiConstants.LIMIT_KEYS, PurchaseRequest.PROJECT_ID);
 
         List<Object> projectIds = this.dao.listLimitKeyValues(query, DBBean.REPOSITORY);
         Map<String, Object> pquery = new HashMap<String, Object>();
         pquery.put(ApiConstants.MONGO_ID, new DBQuery(DBQueryOpertion.IN, projectIds));
         pquery.put(ApiConstants.LIMIT_KEYS, ProjectBean.PROJECT_NAME);
 
         return this.dao.list(pquery, DBBean.PROJECT);
     }
 
     @Override
     public Map<String, Object> rejectRepositoryRequest(Map<String, Object> parserJsonParameters) {
         return null;
     }
 
     public Map<String, Object> cancelRepositoryRequest(Map<String, Object> params) {
         return processRequest(params, DBBean.REPOSITORY, PurchaseRequest.STATUS_CANCELLED);
     }
 
     // 采购合同列表为付款
     public Map<String, Object> listSelectForPayment(Map<String, Object> params) {
         Map<String, Object> query = new HashMap<String, Object>();
         query.put(ApiConstants.LIMIT_KEYS, new String[] { "purchaseContractCode", "supplierName" });
         Map<String, Object> results = dao.list(query, DBBean.PURCHASE_CONTRACT);
         return results;
     }
 
     @Override
     public Map<String, Object> listPaymoney(Map<String, Object> params) {
         Map<String, Object> query1 = new HashMap<String, Object>();
         Map<String, Object> map1 = dao.list(query1, DBBean.PAY_MONEY);
         List<Map<String, Object>> list1 = (List<Map<String, Object>>) map1.get(ApiConstants.RESULTS_DATA);
 
         Set<String> suIds = new HashSet<String>();
         for (Map<String, Object> obj : list1) {
             suIds.add((String) obj.get(PayMoneyBean.supplierId));
         }
         suIds.remove(null);
         suIds.remove("");
         if (!suIds.isEmpty()) {
             Map<String, Object> query02 = new HashMap<String, Object>();
             query02.put(ApiConstants.MONGO_ID, new DBQuery(DBQueryOpertion.IN, new ArrayList(suIds)));
             Map<String, Object> map2 = dao.listToOneMapAndIdAsKey(query02, DBBean.SUPPLIER);
             for (Map<String, Object> obj : list1) {
                 String id = (String) obj.get(PayMoneyBean.supplierId);
                 if (map2.containsKey(id)) {
                     Map<String, Object> su = (Map<String, Object>) map2.get(id);
                     obj.put("supplierName", su.get("supplierName"));
                 }
             }
         }
         return map1;
     }
 
     @Override
     public Map<String, Object> addPaymoney(Map<String, Object> params) {
         Map<String, Object> obj = new HashMap<String, Object>();
         obj.put(PayMoneyBean.payMoney, ApiUtil.getDouble(params, PayMoneyBean.payMoney));
         obj.put(PayMoneyBean.payDate, params.get(PayMoneyBean.payDate));
         obj.put(PayMoneyBean.purchaseContractId, params.get(PayMoneyBean.purchaseContractId));
         obj.put(PayMoneyBean.supplierCardCode, params.get(PayMoneyBean.supplierCardCode));
         obj.put(PayMoneyBean.supplierCardName, params.get(PayMoneyBean.supplierCardName));
         Map<String, Object> pc = dao.findOne(ApiConstants.MONGO_ID, params.get(PayMoneyBean.purchaseContractId), new String[] { "supplier", "purchaseContractCode" },
                 DBBean.PURCHASE_CONTRACT);
         obj.put(PayMoneyBean.purchaseContractCode, pc.get("purchaseContractCode"));
         obj.put(PayMoneyBean.supplierId, pc.get("supplier"));
         return dao.add(obj, DBBean.PAY_MONEY);
     }
 
     @Override
     public Map<String, Object> updatePaymoney(Map<String, Object> params) {
         Map<String, Object> obj = new HashMap<String, Object>();
         obj.put(ApiConstants.MONGO_ID, params.get(ApiConstants.MONGO_ID));
         obj.put(PayMoneyBean.payMoney, params.get(PayMoneyBean.payMoney));
         obj.put(PayMoneyBean.payDate, params.get(PayMoneyBean.payDate));
         obj.put(PayMoneyBean.purchaseContractId, params.get(PayMoneyBean.purchaseContractId));
         obj.put(PayMoneyBean.supplierCardCode, params.get(PayMoneyBean.supplierCardCode));
         obj.put(PayMoneyBean.supplierCardName, params.get(PayMoneyBean.supplierCardName));
         return dao.updateById(obj, DBBean.PAY_MONEY);
     }
 
     @Override
     public Map<String, Object> listGetInvoice(Map<String, Object> params) {
         return dao.list(null, DBBean.GET_INVOICE);
     }
 
     @Override
     public Map<String, Object> addGetInvoice(Map<String, Object> params) {
         return dao.add(params, DBBean.GET_INVOICE);
     }
 
     @Override
     public Map<String, Object> prepareGetInvoice(Map<String, Object> params) {
         String pcId = (String) params.get("purchaseContractId");
 
         // 1. 获取采购合同信息
         Map<String, Object> pc = dao.findOne(ApiConstants.MONGO_ID, pcId, DBBean.GET_INVOICE);
         // 2. 获取供货商信息
         Map<String, Object> supplier = dao.findOne(ApiConstants.MONGO_ID, pc.get("supplier"), DBBean.GET_INVOICE);
 
         Map<String, Object> newObj = new HashMap<String, Object>();
         newObj.put(InvoiceBean.purchaseContractId, pcId);
         newObj.put(InvoiceBean.getInvoiceStatus, InvoiceBean.statusUnSubmit);
         newObj.put(InvoiceBean.getInvoiceItemList, new ArrayList());
         newObj.put(InvoiceBean.getInvoiceSubmitDate, DateUtil.getDateString(new Date()));
 
         // //////////////////////////////////
         Map<String, Object> query = new HashMap<String, Object>();
         query.put(InvoiceBean.purchaseContractId, pcId);
         query.put(ApiConstants.LIMIT_KEYS, new String[] { InvoiceBean.getInvoiceMoney });
         List<Object> payInvoiceList = dao.listLimitKeyValues(query, DBBean.GET_INVOICE);
         Double totalMoney = 0.0;// 统计已开票的总金额
         for (Object obj : payInvoiceList) {
             totalMoney += ApiUtil.getDouble(String.valueOf(obj));
         }
         // 【临时字段】采购合同金额， 已收票额、 已付款额、 应付账款额、付款方式
         newObj.put("totalGetInvoiceMoney", totalMoney);// 已收票额,统计 收票表
         newObj.put("totalPayMoney", 0);// 已付款额 ，统计
         newObj.put("leftPayMoney", 0);// 应付账款额
         newObj.put("PayType", "");// 付款方式
         return newObj;
     }
 
     @Override
     public Map<String, Object> updateGetInvoice(Map<String, Object> params) {
         return dao.updateById(params, DBBean.GET_INVOICE);
     }
 
     @Override
     public void destroyGetInvoice(Map<String, Object> params) {
         List<String> ids = new ArrayList<String>();
         ids.add(String.valueOf(params.get(ApiConstants.MONGO_ID)));
         dao.deleteByIds(ids, DBBean.GET_INVOICE);
     }
 
     public IPurchaseService getBackService() {
         return backService;
     }
 
     public void setBackService(IPurchaseService backService) {
         this.backService = backService;
     }
 
 }
