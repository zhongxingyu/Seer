 package com.pms.service.service.impl;
 
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import com.pms.service.dbhelper.DBQuery;
 import com.pms.service.dbhelper.DBQueryOpertion;
 import com.pms.service.mockbean.ApiConstants;
 import com.pms.service.mockbean.ArrivalNoticeBean;
 import com.pms.service.mockbean.CustomerBean;
 import com.pms.service.mockbean.DBBean;
 import com.pms.service.mockbean.ProjectBean;
 import com.pms.service.mockbean.PurchaseBack;
 import com.pms.service.mockbean.PurchaseCommonBean;
 import com.pms.service.mockbean.PurchaseContract;
 import com.pms.service.mockbean.SalesContractBean;
 import com.pms.service.mockbean.ShipBean;
 import com.pms.service.mockbean.UserBean;
 import com.pms.service.service.AbstractService;
 import com.pms.service.service.IArrivalNoticeService;
 import com.pms.service.service.IPurchaseContractService;
 import com.pms.service.service.ISalesContractService;
 import com.pms.service.util.ApiUtil;
 
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
 	    mergeDataRoleQueryWithProject(params);
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
 
 	
 	public Map<String, Object> listProjectsForSelect(Map<String, Object> params){
 		Map<String, Object> query = new HashMap<String, Object>();
 		query.put(ApiConstants.LIMIT_KEYS, ProjectBean.PROJECT_ID);
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
 			if(ApiUtil.isEmpty(customerInfo)){
 				p.put(ProjectBean.PROJECT_CUSTOMER, "N/A");
 			}else{
 				p.put(ProjectBean.PROJECT_CUSTOMER, customerInfo.get(UserBean.USER_NAME));
 			}
 		}
 		
 		return result;
 	}
 	 
     public Map<String, Object> listEqListByScIDForShip(Object scId) {
         Map<String, Object> query = new HashMap<String, Object>();
         query.put(SalesContractBean.SC_ID, scId);
         query.put(ApiConstants.LIMIT_KEYS, ArrivalNoticeBean.EQ_LIST);
         return listEqlist(query);
     }
     
     public Map<String, Object> listByScIdForBorrowing(Object scId) {
 
         Map<String, Object> query = new HashMap<String, Object>();
         query.put(SalesContractBean.SC_ID, scId);
         query.put(ArrivalNoticeBean.SHIP_TYPE, new DBQuery(DBQueryOpertion.NOT_IN, new String[] { ArrivalNoticeBean.SHIP_TYPE_0, ArrivalNoticeBean.SHIP_TYPE_0_1 }));
         query.put(ApiConstants.LIMIT_KEYS, ArrivalNoticeBean.EQ_LIST);
         return listEqlist(query);
 
     }
 
     private Map<String, Object> listEqlist(Map<String, Object> query) {
         List<Object> obj = this.dao.listLimitKeyValues(query, DBBean.ARRIVAL_NOTICE);
         Map<String, Object> result = new HashMap<String, Object>();
         List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
         if (obj != null) {
             for (Object ob : obj) {
                 List<Map<String, Object>> eqlistMap = (List<Map<String, Object>>) ob;
                 list.addAll(eqlistMap);
             }
         }
 
         result.put(ArrivalNoticeBean.EQ_LIST, list);
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
 		
 		
 		//暂时注释掉，入库也会调用此方法
 //		if (!PurchaseCommonBean.STATUS_ORDER_FINISHED.equals(order.get(PurchaseCommonBean.PROCESS_STATUS))) {
 //			throw new ApiResponseException("采购未执行完毕", ResponseCodeConstants.PURCHASE_ORDER_UNFINISHED);
 //		}
 		
 		/**
 		 * TODO
 		 * 验证到货数量是否超过订单总数量
 		 */
 		
 		Map<String, Object> noticeParams = new HashMap<String, Object>();
 		noticeParams.put(ArrivalNoticeBean.NOTICE_STATUS, ArrivalNoticeBean.NOTICE_STATUS_NORMAL);
 		noticeParams.put(ArrivalNoticeBean.FOREIGN_KEY, order.get(ApiConstants.MONGO_ID));
 		noticeParams.put(ArrivalNoticeBean.FOREIGN_CODE, order.get(PurchaseCommonBean.PURCHASE_ORDER_CODE));
 		noticeParams.put(SalesContractBean.SC_ID, order.get(PurchaseCommonBean.SALES_COUNTRACT_ID));
 		
 		Object deliveryType = order.get(PurchaseContract.EQCOST_DELIVERY_TYPE);
         Object contractType = order.get(PurchaseContract.PURCHASE_CONTRACT_TYPE);
         
         String arriveryType = deliveryType.toString();
         
         if (deliveryType != null && deliveryType.toString().equalsIgnoreCase(PurchaseContract.EQCOST_DELIVERY_TYPE_REPOSITORY)) {
             if (contractType != null && contractType.toString().equalsIgnoreCase(PurchaseContract.CONTRACT_EXECUTE_CATE_BEIJINGDAICAI)) {
                 arriveryType = ArrivalNoticeBean.SHIP_TYPE_3;
             } else {
                 arriveryType = ArrivalNoticeBean.SHIP_TYPE_2;
             }
         }
         
         noticeParams.put(PurchaseContract.EQCOST_DELIVERY_TYPE, deliveryType);
         noticeParams.put(ArrivalNoticeBean.SHIP_TYPE, arriveryType);
 		noticeParams.put(ArrivalNoticeBean.ARRIVAL_DATE, ApiUtil.formateDate(new Date(), "yyy-MM-dd"));
 		
 		// 到货设备清单
 		List<Map<String, Object>> eqList = (List<Map<String, Object>>) params.get(SalesContractBean.SC_EQ_LIST);
 		List<Map<String, Object>> arrivalEqList = new ArrayList<Map<String, Object>>();
 		for (Map<String, Object> map : eqList) {
 			int arrivalAmount = (int) ApiUtil.getInteger(map.get(ArrivalNoticeBean.EQCOST_ARRIVAL_AMOUNT),0);
 			if (arrivalAmount > 0) {
 				Map<String, Object> eq = new HashMap<String, Object>();
 				eq.put(ApiConstants.MONGO_ID, map.get(ApiConstants.MONGO_ID));
 				eq.put(ArrivalNoticeBean.EQCOST_ARRIVAL_AMOUNT, map.get(ArrivalNoticeBean.EQCOST_ARRIVAL_AMOUNT));
 				
 				//如下的信息用来在页面分组展示
 				eq.put(PurchaseContract.EQCOST_DELIVERY_TYPE, deliveryType);
 				eq.put(ArrivalNoticeBean.SHIP_TYPE, arriveryType);
 				eq.put(PurchaseCommonBean.PURCHASE_CONTRACT_ID, map.get(PurchaseCommonBean.PURCHASE_CONTRACT_ID));
 				eq.put(PurchaseCommonBean.PURCHASE_CONTRACT_CODE, map.get(PurchaseCommonBean.PURCHASE_CONTRACT_CODE));
 				eq.put(PurchaseContract.PURCHASE_CONTRACT_TYPE, map.get(PurchaseContract.PURCHASE_CONTRACT_TYPE));
 				eq.put(PurchaseCommonBean.PURCHASE_ORDER_ID, map.get(PurchaseCommonBean.PURCHASE_ORDER_ID));
 				eq.put(PurchaseCommonBean.PURCHASE_ORDER_CODE, order.get(PurchaseCommonBean.PURCHASE_ORDER_CODE));
                 eq.put(PurchaseCommonBean.CONTRACT_EXECUTE_CATE, map.get(PurchaseCommonBean.CONTRACT_EXECUTE_CATE));  
                 eq.put(PurchaseCommonBean.PROJECT_ID, map.get(PurchaseCommonBean.PROJECT_ID));  
 				eq.put(PurchaseContract.SUPPLIER, map.get(PurchaseContract.SUPPLIER));
 				
 				arrivalEqList.add(eq);
 			}
 		}
 		noticeParams.put(ArrivalNoticeBean.EQ_LIST, arrivalEqList);
 		
 		// 项目，销售合同冗余信息
 		scs.mergeCommonFieldsFromSc(noticeParams, noticeParams.get(SalesContractBean.SC_ID));
 		
 		return dao.add(noticeParams, DBBean.ARRIVAL_NOTICE);
 	}
     
     /**
      * 调拨生产到货通知
      */
 	public Map<String, Object> createByAllocate(Map<String, Object> allot) {
 		Map<String, Object> noticeParams = new HashMap<String, Object>();
 		noticeParams.put(ArrivalNoticeBean.NOTICE_STATUS, ArrivalNoticeBean.NOTICE_STATUS_NORMAL);
 		noticeParams.put(ArrivalNoticeBean.FOREIGN_KEY, allot.get(ApiConstants.MONGO_ID));
 		noticeParams.put(ArrivalNoticeBean.FOREIGN_CODE, allot.get(PurchaseBack.paCode));
 		noticeParams.put(SalesContractBean.SC_ID, allot.get(PurchaseBack.scId));
 		noticeParams.put(ArrivalNoticeBean.SHIP_TYPE, allot.get(PurchaseBack.paShelfCode));
 		noticeParams.put(ArrivalNoticeBean.ARRIVAL_DATE, ApiUtil.formateDate(new Date(), "yyy-MM-dd"));
 		
 		// 到货设备清单
 		List<Map<String, Object>> eqList = (List<Map<String, Object>>) allot.get(SalesContractBean.SC_EQ_LIST);
 		eqList = scs.mergeEqListBasicInfo(eqList);
 		List<Map<String, Object>> arrivalEqList = new ArrayList<Map<String, Object>>();
 		for (Map<String, Object> map : eqList) {
 			Map<String, Object> eq = new HashMap<String, Object>();
 			eq.put(ApiConstants.MONGO_ID, map.get(ApiConstants.MONGO_ID));
 			eq.put(ArrivalNoticeBean.EQCOST_ARRIVAL_AMOUNT, map.get(PurchaseBack.paCount));
 			
 			eq.put(SalesContractBean.SC_ID, map.get(PurchaseBack.scId));
 			eq.put(SalesContractBean.SC_CODE, map.get(PurchaseBack.scCode));
 			eq.put(ProjectBean.PROJECT_ID, map.get(ProjectBean.PROJECT_ID));
 			eq.put(ProjectBean.PROJECT_CODE, map.get(ProjectBean.PROJECT_CODE));
 			eq.put(ArrivalNoticeBean.SHIP_TYPE, allot.get(PurchaseBack.paShelfCode));
 			
 			arrivalEqList.add(eq);
 		}
 		noticeParams.put(ArrivalNoticeBean.EQ_LIST, arrivalEqList);
 		
 		// 项目，销售合同冗余信息
 		scs.mergeCommonFieldsFromSc(noticeParams, noticeParams.get(SalesContractBean.SC_ID));
 		
 		return dao.add(noticeParams, DBBean.ARRIVAL_NOTICE);
 	}
 	
 	
     public Map<String, Object> loadArrivalEqListByOrder(Map<String, Object> params) {
         Map<String, Object> order = pService.getPurchaseOrder(params);
 
         List<Map<String, Object>> prEqList = (List<Map<String, Object>>) order.get(SalesContractBean.SC_EQ_LIST);
         Map<String, Integer> countMap = new HashMap<String, Integer>();
         for (Map<String, Object> orderEq : prEqList) {
             countMap.put(orderEq.get(ApiConstants.MONGO_ID).toString(), ApiUtil.getInteger(orderEq.get(PurchaseCommonBean.EQCOST_APPLY_AMOUNT), 0));
         }
         // 获取剩余可到货清单
         Map<String, Object> query = new HashMap<String, Object>();
         query.put(ArrivalNoticeBean.FOREIGN_KEY, order.get(ApiConstants.MONGO_ID));
         Map<String, Integer> arrivecount = countEqByKey(query, DBBean.ARRIVAL_NOTICE, ArrivalNoticeBean.EQCOST_ARRIVAL_AMOUNT, null);
        
         
         for (Map<String, Object> orderEq : prEqList) {
             orderEq.put("arrivedRequestCount", arrivecount.get(orderEq.get(ApiConstants.MONGO_ID).toString()));
         }
 
         
         Map<String, Integer> restEqCount = new HashMap<String, Integer>();
         for (String id : countMap.keySet()) {
             int eqCount = 0;
             int arrcount = 0;
             if (countMap.get(id) != null) {
                 eqCount = countMap.get(id);
             }
             if (arrivecount.get(id) != null) {
                 arrcount = arrivecount.get(id);
             }
             restEqCount.put(id, eqCount - arrcount);
         }
         
         for (Map<String, Object> orderEq : prEqList) {
             orderEq.put("orderRequestCount", restEqCount.get(orderEq.get(ApiConstants.MONGO_ID).toString()));
             
             orderEq.put("eqcostArrivalAmount", restEqCount.get(orderEq.get(ApiConstants.MONGO_ID).toString()));
         }
         
         removeEmptyEqList(prEqList, "orderRequestCount");
 
         return order;
     }
     
     public Map<String, Object> getArrivalNotice(Map<String, Object> params){
     	
     	Map<String, Object> arriveNotice =  this.dao.findOne(ApiConstants.MONGO_ID, params.get(ApiConstants.MONGO_ID), DBBean.ARRIVAL_NOTICE);
     	
     	Object eqcostList = arriveNotice.get("eqcostList");
 //    	scs.getSCAndCustomerInfo(params).mergeCommonFieldsFromSc(arriveNotice, arriveNotice.get("scId"));
 //    	scs.
     	arriveNotice.put("eqcostList", scs.mergeEqListBasicInfo(eqcostList));
     	
    	
    	pService.mergeProjectInfo(arriveNotice);
    	
     	return arriveNotice;
     }
 
 }
