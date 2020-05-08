 package com.pms.service.service.impl;
 
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import com.pms.service.dbhelper.DBQuery;
 import com.pms.service.dbhelper.DBQueryOpertion;
 import com.pms.service.exception.ApiResponseException;
 import com.pms.service.mockbean.ApiConstants;
 import com.pms.service.mockbean.ArrivalNoticeBean;
 import com.pms.service.mockbean.CustomerBean;
 import com.pms.service.mockbean.DBBean;
 import com.pms.service.mockbean.ProjectBean;
 import com.pms.service.mockbean.PurchaseCommonBean;
 import com.pms.service.mockbean.SalesContractBean;
 import com.pms.service.mockbean.UserBean;
 import com.pms.service.service.AbstractService;
 import com.pms.service.service.IArrivalNoticeService;
 import com.pms.service.service.IPurchaseContractService;
 import com.pms.service.service.ISalesContractService;
 import com.pms.service.util.ApiUtil;
 import com.pms.service.util.status.ResponseCodeConstants;
 
 public class ArrivalNoticeServiceImpl extends AbstractService implements IArrivalNoticeService {
 	
 	protected ISalesContractService scs;
 	
 	private IPurchaseContractService pService;
 
 	public ISalesContractService getScs() {
 		return scs;
 	}
 
 	public void setScs(ISalesContractService scs) {
 		this.scs = scs;
 	}
 
 	public IPurchaseContractService getpService() {
 		return pService;
 	}
 
 	public void setpService(IPurchaseContractService pService) {
 		this.pService = pService;
 	}
 
 	@Override
 	public String geValidatorFileName() {
 		return null;
 	}
 
 	public Map<String, Object> list(Map<String, Object> params) {
 	    mergeMyTaskQuery(params, DBBean.ARRIVAL_NOTICE);
 		return dao.list(params, DBBean.ARRIVAL_NOTICE);
 	}
 
 	public Map<String, Object> update(Map<String, Object> params) {
 		return dao.updateById(params, DBBean.ARRIVAL_NOTICE);
 	}
 
 	public void destroy(Map<String, Object> params) {
 		List<String> ids = new ArrayList<String>();
 		ids.add(String.valueOf(params.get(ApiConstants.MONGO_ID)));
 		dao.deleteByIds(ids, DBBean.ARRIVAL_NOTICE);
 	}
 
 	public Map<String, Object> create(Map<String, Object> params) {
 		
 		String foreignKey = (String) params.get(ArrivalNoticeBean.FOREIGN_KEY);
 		String type = (String) params.get(ArrivalNoticeBean.SHIP_TYPE);
 		
 		if (dao.exist(ArrivalNoticeBean.FOREIGN_KEY, foreignKey, DBBean.ARRIVAL_NOTICE)) {
 			throw new ApiResponseException("对应到货通知已存在", ResponseCodeConstants.ARRIVAL_NOTICE_ALREADY_EXIST);
 		}
 		
 		if (!ArrivalNoticeBean.SHIP_TYPE_0.equals(type)) {
 			Map<String, Object> order = dao.findOne(ApiConstants.MONGO_ID, foreignKey, DBBean.PURCHASE_ORDER);
 			if (order != null) {
 				if (order.containsKey(PurchaseCommonBean.PROCESS_STATUS)) {
 					if (!PurchaseCommonBean.STATUS_ORDER_FINISHED.equals(order.get(PurchaseCommonBean.PROCESS_STATUS))) {
 						throw new ApiResponseException("采购未执行完毕", ResponseCodeConstants.PURCHASE_ORDER_UNFINISHED);
 					}
 				}
 				String eqcostDeliveryType = (String) order.get("eqcostDeliveryType");
 				
 				params.put(ArrivalNoticeBean.FOREIGN_CODE, order.get(PurchaseCommonBean.PURCHASE_ORDER_CODE));
 				params.put(ArrivalNoticeBean.PROJECT_ID, order.get(PurchaseCommonBean.PROJECT_ID));
 				params.put(ArrivalNoticeBean.SALES_COUNTRACT_ID, order.get(PurchaseCommonBean.SALES_COUNTRACT_ID));
 				if(!ApiUtil.isEmpty(type)){
 				    params.put(ArrivalNoticeBean.SHIP_TYPE, type); 
 				}else{
 				    params.put(ArrivalNoticeBean.SHIP_TYPE, eqcostDeliveryType);
 				}
 				// 入库
 				if (type == null) {
 					params.put(ArrivalNoticeBean.EQ_LIST, params.get(SalesContractBean.SC_EQ_LIST));
 				} else {
 					params.put(ArrivalNoticeBean.EQ_LIST, order.get(SalesContractBean.SC_EQ_LIST));
 				}
 				
 			}
 		}
 		
 		params.put(ArrivalNoticeBean.ARRIVAL_DATE, ApiUtil.formateDate(new Date(), "yyy-MM-dd"));
 		return dao.add(params, DBBean.ARRIVAL_NOTICE);
 	}
 	
 	public Map<String, Object> listProjectsForSelect(Map<String, Object> params){
 		Map<String, Object> query = new HashMap<String, Object>();
 		query.put(ApiConstants.LIMIT_KEYS, ArrivalNoticeBean.PROJECT_ID);
 		List<Object> projectIds = this.dao.listLimitKeyValues(query, DBBean.ARRIVAL_NOTICE);
 		
 		Map<String, Object> projectQuery = new HashMap<String, Object>();
 		projectQuery.put(ApiConstants.MONGO_ID, new DBQuery(DBQueryOpertion.IN, projectIds));
 		projectQuery.put(ApiConstants.LIMIT_KEYS, new String[]{ProjectBean.PROJECT_NAME,ProjectBean.PROJECT_CODE, ProjectBean.PROJECT_MANAGER, 
 				ProjectBean.PROJECT_STATUS, ProjectBean.PROJECT_CUSTOMER});
      
 		Map<String, Object> result = dao.list(projectQuery, DBBean.PROJECT);
 		
 		List<Map<String, Object>> resultList = (List<Map<String, Object>>) result.get(ApiConstants.RESULTS_DATA); 
 		List<String> pmIds = new ArrayList<String>(); 
 		List<String> cIds = new ArrayList<String>();
 		for(Map<String, Object> p : resultList){
 			String pmid = (String)p.get(ProjectBean.PROJECT_MANAGER);
 			String cid = (String)p.get(ProjectBean.PROJECT_CUSTOMER);
 			if (!ApiUtil.isEmpty(pmid)){
 				pmIds.add(pmid);
 			}
 			if (!ApiUtil.isEmpty(cid)){
 				cIds.add(cid);
 			}
 		}
 		Map<String, Object> pmQuery = new HashMap<String, Object>();
 		pmQuery.put(ApiConstants.MONGO_ID, new DBQuery(DBQueryOpertion.IN, pmIds));
 		pmQuery.put(ApiConstants.LIMIT_KEYS, new String[] {UserBean.USER_NAME, UserBean.DEPARTMENT});
 		Map<String, Object> pmData = dao.listToOneMapAndIdAsKey(pmQuery, DBBean.USER);
 		
 		Map<String, Object> cusQuery = new HashMap<String, Object>();
 		cusQuery.put(ApiConstants.MONGO_ID, new DBQuery(DBQueryOpertion.IN, cIds));
 		cusQuery.put(ApiConstants.LIMIT_KEYS, new String[] {CustomerBean.NAME});
 		Map<String, Object> cusData = dao.listToOneMapAndIdAsKey(cusQuery, DBBean.USER);
 		
 		for (Map<String, Object> p : resultList){
 			String pmid = (String)p.get(ProjectBean.PROJECT_MANAGER);
 			Map<String, Object> pmInfo = (Map<String, Object>) pmData.get(pmid);
 			if(ApiUtil.isEmpty(pmInfo)){
 				p.put(ProjectBean.PROJECT_MANAGER, "N/A");
 				p.put(UserBean.DEPARTMENT, "N/A");
 			}else{
 				p.put(ProjectBean.PROJECT_MANAGER, pmInfo.get(UserBean.USER_NAME));
 				p.put(UserBean.DEPARTMENT, pmInfo.get(UserBean.DEPARTMENT));
 			}
 			
 			
 			String customerId = (String)p.get(ProjectBean.PROJECT_CUSTOMER);
 			Map<String, Object> customerInfo = (Map<String, Object>) cusData.get(customerId);
 			if(ApiUtil.isEmpty(pmInfo)){
 				p.put(ProjectBean.PROJECT_CUSTOMER, "N/A");
 			}else{
 				p.put(ProjectBean.PROJECT_CUSTOMER, pmInfo.get(UserBean.USER_NAME));
 			}
 		}
 		
 		return result;
 	}
 	 
     public Map<String, Object> listEqListByScIDForShip(Object scId) {
         Map<String, Object> query = new HashMap<String, Object>();
         query.put(ArrivalNoticeBean.SALES_COUNTRACT_ID, scId);
         query.put(ApiConstants.LIMIT_KEYS, ArrivalNoticeBean.EQ_LIST);
         return listEqlist(query);
     }
     
     public Map<String, Object> listByScIdForBorrowing(Object scId){
 
         Map<String, Object> query = new HashMap<String, Object>();
         query.put(ArrivalNoticeBean.SALES_COUNTRACT_ID, scId);    
         query.put(ArrivalNoticeBean.SHIP_TYPE, new DBQuery(DBQueryOpertion.NOT_IN, ArrivalNoticeBean.SHIP_TYPE_1));
         query.put(ApiConstants.LIMIT_KEYS, ArrivalNoticeBean.EQ_LIST);
         return listEqlist(query);
     
     }
 
     private Map<String, Object> listEqlist(Map<String, Object> query) {
         List<Object> obj = this.dao.listLimitKeyValues(query, DBBean.ARRIVAL_NOTICE);
 
         Map<String, Object> result = new HashMap<String, Object>();
 
        List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
        
        if (obj.size() == 0) {       
             result.put(ArrivalNoticeBean.EQ_LIST, new ArrayList<Map<String, Object>>());
        }else{
            
            for(Object ob: obj){
                List<Map<String, Object>> eqlistMap = (List<Map<String, Object>>) ob;
                list.addAll(eqlistMap);
            }
            result.put(ArrivalNoticeBean.EQ_LIST, list);
       
         }
         return result;
     }
     
     // 根据销售合同取到货设备清单
     public Map<String, Object> listCanShipEq(Map<String, Object> params) {
     	params.put(ArrivalNoticeBean.NOTICE_STATUS, ArrivalNoticeBean.NOTICE_STATUS_NORMAL);
     	Map<String, Object> noticeResult = dao.list(params, DBBean.ARRIVAL_NOTICE);
     	List<Map<String, Object>> noticeList = (List<Map<String, Object>>) noticeResult.get(ApiConstants.RESULTS_DATA);
     	
     	List<Map<String, Object>> eqList = new ArrayList<Map<String, Object>>();
 		for (Map<String, Object> notice:noticeList){
 			List<Map<String, Object>> list = (List<Map<String, Object>>) notice.get(ArrivalNoticeBean.EQ_LIST);
 			for (Map<String, Object> eq:list){
 				eq.put(ArrivalNoticeBean.NOTICE_ID, notice.get(ApiConstants.MONGO_ID));
 			}
 			eqList.addAll(list);
 		}
 		
 		Map<String, Object> res = new HashMap<String, Object>();
 		if (ApiUtil.isEmpty(eqList)) {
 			res.put(ApiConstants.RESULTS_DATA, eqList);
 		} else {
 			res.put(ApiConstants.RESULTS_DATA, scs.mergeEqListBasicInfo(eqList));
 		}
 		
 		return res;
     }
     
     /**
      * 获取订单信息和已到货数量 - 暂时没用
      * @param parameters
      * @return
      */
     public Map<String, Object> getPurchaseOrder(Map<String, Object> parameters) {
     	Map<String, Object> result = pService.getPurchaseOrder(parameters);
         List<Map<String, Object>> mergeLoadedEqList = (List<Map<String, Object>>) result.get(SalesContractBean.SC_EQ_LIST);
         
         Map<String, Object> noticeParams = new HashMap<String, Object>();
 		
         noticeParams.put(ArrivalNoticeBean.FOREIGN_KEY, parameters.get(ApiConstants.MONGO_ID));
 		Map<String, Object> notices = dao.list(parameters, DBBean.ARRIVAL_NOTICE);
 		List<Map<String, Object>> noticeList = (List<Map<String, Object>>) notices.get(ApiConstants.RESULTS_DATA);
         
 		// 计算已到货的设备数量
 		Map<String, Object> arrivalEqCount = new HashMap<String, Object>();
 		
 		for (Map<String, Object> notice : noticeList) {
 			List<Map<String, Object>> noticeEqList = (List<Map<String, Object>>) notice.get(ArrivalNoticeBean.EQ_LIST);
 			for (Map<String, Object> eq : noticeEqList) {
 				Double amount = 0.0;
 				if (arrivalEqCount.containsKey(eq.get(ApiConstants.MONGO_ID))) {
 					amount = (Double) eq.get(ArrivalNoticeBean.EQCOST_ARRIVAL_AMOUNT);
 				} else {
 
 				}
 			}
 		}
         
         return result;
     }
     
     /**
      * 订单生产到货通知
      */
 	public Map<String, Object> createByOrder(Map<String, Object> params) {
 		
 		Map<String, Object> order = dao.findOne(ApiConstants.MONGO_ID, params.get(ApiConstants.MONGO_ID), DBBean.PURCHASE_ORDER);
 		
 		if (!PurchaseCommonBean.STATUS_ORDER_FINISHED.equals(order.get(PurchaseCommonBean.PROCESS_STATUS))) {
 			throw new ApiResponseException("采购未执行完毕", ResponseCodeConstants.PURCHASE_ORDER_UNFINISHED);
 		}
 		
 		/**
 		 * TODO
 		 * 验证到货数量是否超过订单总数量
 		 */
 		
 		Map<String, Object> noticeParams = new HashMap<String, Object>();
 		noticeParams.put(ArrivalNoticeBean.NOTICE_STATUS, ArrivalNoticeBean.NOTICE_STATUS_NORMAL);
 		noticeParams.put(ArrivalNoticeBean.FOREIGN_KEY, order.get(ApiConstants.MONGO_ID));
 		noticeParams.put(ArrivalNoticeBean.FOREIGN_CODE, order.get(PurchaseCommonBean.PURCHASE_ORDER_CODE));
 		noticeParams.put(ArrivalNoticeBean.PROJECT_ID, order.get(PurchaseCommonBean.PROJECT_ID));
 		noticeParams.put(ArrivalNoticeBean.SALES_COUNTRACT_ID, order.get(PurchaseCommonBean.SALES_COUNTRACT_ID));
 		noticeParams.put(ArrivalNoticeBean.SHIP_TYPE, order.get("eqcostDeliveryType"));
 		noticeParams.put(ArrivalNoticeBean.ARRIVAL_DATE, ApiUtil.formateDate(new Date(), "yyy-MM-dd"));
 		
 		List<Map<String, Object>> eqList = (List<Map<String, Object>>) params.get(SalesContractBean.SC_EQ_LIST);
 		List<Map<String, Object>> arrivalEqList = new ArrayList<Map<String, Object>>();
 		for (Map<String, Object> map : eqList) {
 			Double arrivalAmount = (Double) map.get(ArrivalNoticeBean.EQCOST_ARRIVAL_AMOUNT);
 			if (arrivalAmount > 0) {
 				Map<String, Object> eq = new HashMap<String, Object>();
 				eq.put(ApiConstants.MONGO_ID, map.get(ApiConstants.MONGO_ID));
 				eq.put(ArrivalNoticeBean.EQCOST_ARRIVAL_AMOUNT, map.get(ArrivalNoticeBean.EQCOST_ARRIVAL_AMOUNT));
 				arrivalEqList.add(eq);
 			}
 		}
 		noticeParams.put(ArrivalNoticeBean.EQ_LIST, arrivalEqList);
 		
 		return dao.add(noticeParams, DBBean.ARRIVAL_NOTICE);
 	}
 
 }
