 package com.pms.service.service.impl;
 
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import com.pms.service.dbhelper.DBQuery;
 import com.pms.service.dbhelper.DBQueryOpertion;
 import com.pms.service.mockbean.ApiConstants;
 import com.pms.service.mockbean.BorrowingBean;
 import com.pms.service.mockbean.DBBean;
 import com.pms.service.mockbean.EqCostListBean;
 import com.pms.service.mockbean.ShipBean;
 import com.pms.service.service.AbstractService;
 import com.pms.service.service.IPurchaseContractService;
 import com.pms.service.service.IPurchaseService;
 import com.pms.service.service.IShipService;
 import com.pms.service.util.ApiUtil;
 
 public class ShipServiceImpl extends AbstractService implements IShipService {
 	
 	private IPurchaseContractService pService;
 	
 	private IPurchaseService purchaseService;
 
 	public IPurchaseContractService getpService() {
 		return pService;
 	}
 
 	public IPurchaseService getPurchaseService() {
 		return purchaseService;
 	}
 
 	public void setPurchaseService(IPurchaseService purchaseService) {
 		this.purchaseService = purchaseService;
 	}
 
 	public void setpService(IPurchaseContractService pService) {
 		this.pService = pService;
 	}
 
 	@Override
 	public String geValidatorFileName() {
 		return "ship";
 	}
 	
 	public Map<String, Object> get(Map<String, Object> params) {
 		return dao.findOne(ApiConstants.MONGO_ID, params.get(ApiConstants.MONGO_ID), DBBean.SHIP);
 	}
 
 	public Map<String, Object> list(Map<String, Object> params) {
 		int limit = ApiUtil.getInteger(params, ApiConstants.PAGE_SIZE, 15);
 		int limitStart = ApiUtil.getInteger(params, ApiConstants.SKIP, 0);
 		Map<String, Object> queryMap = new HashMap<String, Object>();
 		queryMap.put(ApiConstants.LIMIT, limit);
 		queryMap.put(ApiConstants.LIMIT_START, limitStart);
 		return dao.list(queryMap, DBBean.SHIP);
 	}
 
 	public Map<String, Object> update(Map<String, Object> params) {
 		params.put(ShipBean.SHIP_STATUS, 0);
 		return dao.updateById(params, DBBean.SHIP);
 	}
 
 	public void destroy(Map<String, Object> params) {
 		List<String> ids = new ArrayList<String>();
 		ids.add(String.valueOf(params.get(ApiConstants.MONGO_ID)));
 		dao.deleteByIds(ids, DBBean.SHIP);
 	}
 
 	public Map<String, Object> create(Map<String, Object> params) {
 		params.put(ShipBean.SHIP_STATUS, 0);
 		params.put(ShipBean.SHIP_DATE, ApiUtil.formateDate(new Date(), "yyy-MM-dd"));
 		return dao.add(params, DBBean.SHIP);
 	}
 	
 	public Map<String, Object> eqlist(Map<String, Object> params) {
 		
 		String saleId = (String) params.get(ShipBean.SHIP_SALES_CONTRACT_ID);
 		
 		// 已批准的 调拨申请的 设备清单
 		Map<String, Double> alloEqList = purchaseService.getAllotEqCountBySalesContractId(saleId);
 		
 		// 已批准的 采购合同 的设备清单
 		List<Map<String, Object>> purchaseEqList = pService.listApprovedPurchaseContractCosts(saleId);
 		
 		// 已发货的设备清单
 		List<Map<String, Object>> shipedEqList = shipedList(saleId);
 		
 		// 调拨 + 采购
 		for (Map<String, Object> p:purchaseEqList){
 			String id = p.get(ApiConstants.MONGO_ID).toString();
 			Double amount = (Double) p.get(EqCostListBean.EQ_LIST_AMOUNT);
 			if (alloEqList.containsKey(id)) {
 				Double aAmount = alloEqList.get(id);
 				alloEqList.put(id, aAmount+amount);
 			} else {
 				alloEqList.put(id, amount);
 			}
 		}
 		
 		// - 已发货
 		for (Map<String, Object> s:shipedEqList){
 			String id = s.get(ApiConstants.MONGO_ID).toString();
 			if (alloEqList.containsKey(id)) {
 				Double amount = (Double) s.get(EqCostListBean.EQ_LIST_AMOUNT);
 				Double aAmount = alloEqList.get(id);
 				alloEqList.put(id, aAmount-amount);
 			}
 		}
 		
 		// 取设备信息
 		List<String> eqId = new ArrayList<String>();
 		for (String id : alloEqList.keySet()) {
 			eqId.add(id);
 		}
 		Map<String, Object> queryContract = new HashMap<String, Object>();
 		queryContract.put(ApiConstants.MONGO_ID, new DBQuery(DBQueryOpertion.IN, eqId));
 		Map<String, Object> eqInfoMap = dao.listToOneMapByKey(queryContract, DBBean.EQ_COST, ApiConstants.MONGO_ID);
 		
 		// 封装结果数据
 		List<Map<String, Object>> result = new ArrayList<Map<String, Object>>();
 		for (Map.Entry mapEntry : alloEqList.entrySet()) {
 			Map<String, Object> eqMap = (Map<String, Object>) eqInfoMap.get(mapEntry.getKey().toString());
 			if (eqMap != null) {
 				eqMap.put(EqCostListBean.EQ_LIST_AMOUNT, mapEntry.getValue());
 				result.add(eqMap);
 			}
 		}
 		
 		Map<String, Object> res = new HashMap<String, Object>();
 		res.put(ApiConstants.RESULTS_DATA, result);
 		return res;
 	}
 	
 	// 已批准的发货设备清单 - 入参 销售合同id
 	public List<Map<String,Object>> shipedList(String saleId) {
 		
 		Map<String, Object> parameters = new HashMap<String, Object>();
 		
		parameters.put(ShipBean.SHIP_STATUS, 2);
 		parameters.put(ShipBean.SHIP_SALES_CONTRACT_ID, saleId);
 		
 		Map<String, Object> result = dao.list(parameters, DBBean.SHIP);
 		List<Map<String, Object>> list = (List<Map<String, Object>>) result.get(ApiConstants.RESULTS_DATA);
 		
 		List<Map<String, Object>> eqList = new ArrayList<Map<String, Object>>();
 		for (Map<String, Object> p:list){
 			eqList.addAll((List<Map<String, Object>>) p.get(ShipBean.SHIP_EQ_LIST));
 		}
 		return eqList;
 	}
 	
 	public Map<String, Object> option(Map<String, Object> params) {
 		Map<String, Object> result = null;
 		if (params.containsKey(ShipBean.SHIP_STATUS)) {
 			Map<String, Object> cc = dao.findOne(ApiConstants.MONGO_ID, params.get(ApiConstants.MONGO_ID), DBBean.SHIP);
 	        params.put(ApiConstants.MONGO_ID, cc.get(ApiConstants.MONGO_ID));
 	        params.put(ShipBean.SHIP_STATUS, params.get(ShipBean.SHIP_STATUS));
 
 	        result =  dao.updateById(params, DBBean.SHIP);
 		}
         
         return result;
     }
 
 }
