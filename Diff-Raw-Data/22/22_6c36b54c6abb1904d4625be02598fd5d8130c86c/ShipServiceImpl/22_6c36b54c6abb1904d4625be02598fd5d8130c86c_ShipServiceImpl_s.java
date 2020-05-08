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
 import com.pms.service.mockbean.DBBean;
 import com.pms.service.mockbean.EqCostListBean;
 import com.pms.service.mockbean.SalesContractBean;
 import com.pms.service.mockbean.ShipBean;
 import com.pms.service.service.AbstractService;
 import com.pms.service.service.IArrivalNoticeService;
 import com.pms.service.service.IPurchaseContractService;
 import com.pms.service.service.IPurchaseService;
 import com.pms.service.service.IShipService;
 import com.pms.service.util.ApiUtil;
 
 public class ShipServiceImpl extends AbstractService implements IShipService {
 	
 	private IPurchaseContractService pService;
 	
 	private IArrivalNoticeService arrivalService;
 		
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
 	
 	
 
 	public IArrivalNoticeService getArrivalService() {
         return arrivalService;
     }
 
     public void setArrivalService(IArrivalNoticeService arrivalService) {
         this.arrivalService = arrivalService;
     }
     
 
     @Override
 	public String geValidatorFileName() {
 		return "ship";
 	}
 	
 	public Map<String, Object> get(Map<String, Object> params) {
 		return dao.findOne(ApiConstants.MONGO_ID, params.get(ApiConstants.MONGO_ID), DBBean.SHIP);
 	}
 
 	public Map<String, Object> list(Map<String, Object> params) {
 	    mergeMyTaskQuery(params, DBBean.SHIP);
 		return dao.list(params, DBBean.SHIP);
 	}
 
 	public Map<String, Object> update(Map<String, Object> params) {
 		params.put(ShipBean.SHIP_STATUS, ShipBean.SHIP_STATUS_DRAFT);
 		return dao.updateById(params, DBBean.SHIP);
 	}
 
 	public void destroy(Map<String, Object> params) {
 		List<String> ids = new ArrayList<String>();
 		ids.add(String.valueOf(params.get(ApiConstants.MONGO_ID)));
 		dao.deleteByIds(ids, DBBean.SHIP);
 	}
 
 	public Map<String, Object> create(Map<String, Object> params) {
 		String status;
 		if (params.containsKey(ShipBean.SHIP_STATUS)) {
 			status = params.get(ShipBean.SHIP_STATUS).toString();
 		} else {
 			status = ShipBean.SHIP_STATUS_DRAFT;
 		}
 		
 		params.put(ShipBean.SHIP_STATUS, status);
 		return dao.add(params, DBBean.SHIP);
 	}
 	
 	public Map<String, Object> listCanShipEq(Map<String, Object> params) {
 		params.put(ArrivalNoticeBean.NOTICE_STATUS, ArrivalNoticeBean.NOTICE_STATUS_NORMAL);
 		dao.list(params, DBBean.ARRIVAL_NOTICE);
 		return null;
 	}
 	
 	public Map<String, Object> eqlist(Map<String, Object> params) {
 		
 		String saleId = (String) params.get(ShipBean.SHIP_SALES_CONTRACT_ID);
 		// 已到货 的 设备清单，来自于调拨申请,入库和直发到货通知
 		Map<String, Object> map = arrivalService.listEqListByScIDForShip(saleId);
 		List<Map<String, Object>> purchaseEqList = (List<Map<String, Object>>) map.get(SalesContractBean.SC_EQ_LIST);
		scs.mergeEqListBasicInfo(purchaseEqList);
 
 		
 		//已发货的数量统计
         Map<String, Object> parameters = new HashMap<String, Object>();
 //        parameters.put(ShipBean.SHIP_STATUS, ShipBean.SHIP_STATUS_APPROVE);
         parameters.put(ShipBean.SHIP_SALES_CONTRACT_ID, saleId);
         Map<String, Integer> shipedCountMap = countEqByKey(parameters, DBBean.SHIP, "eqcostAmount", null);
         
         for (Map<String, Object> eqMap : purchaseEqList) {
             int count = ApiUtil.getInteger(eqMap.get("eqcostAmount"), 0);
             if (shipedCountMap.get(eqMap.get(ApiConstants.MONGO_ID)) != null) {
                 eqMap.put(ShipBean.SHIP_LEFT_AMOUNT, count - shipedCountMap.get(eqMap.get(ApiConstants.MONGO_ID)));
                 eqMap.put("eqcostAmount", count - shipedCountMap.get(eqMap.get(ApiConstants.MONGO_ID)));
             } else {
                 eqMap.put(ShipBean.SHIP_LEFT_AMOUNT, count);
             }
         }
 
 		Map<String, Object> res = new HashMap<String, Object>();
 		res.put(ApiConstants.RESULTS_DATA, purchaseEqList);
 		return res;
 	}
 	
 	public Map<String, Object> option(Map<String, Object> params) {
 		Map<String, Object> result = null;
 		if (params.containsKey(ShipBean.SHIP_STATUS)) {
 			String status = params.get(ShipBean.SHIP_STATUS).toString();
 			Map<String, Object> cc = dao.findOne(ApiConstants.MONGO_ID, params.get(ApiConstants.MONGO_ID), DBBean.SHIP);
 	        params.put(ApiConstants.MONGO_ID, cc.get(ApiConstants.MONGO_ID));
 	        params.put(ShipBean.SHIP_STATUS, status);
 	        
 	        if (status.equals(ShipBean.SHIP_STATUS_SUBMIT)) {
 	    		params.put(ShipBean.SHIP_DATE, ApiUtil.formateDate(new Date(), "yyy-MM-dd"));
 			}
 
 	        result =  dao.updateById(params, DBBean.SHIP);
 		}
         
         return result;
     }
 	
 	public Map<String, Object> record(Map<String, Object> params) {
 		List<Map<String, Object>> eqlist = (List<Map<String, Object>>) params.get(ShipBean.SHIP_EQ_LIST);
 		boolean close = true;
 		for (Map<String, Object> eq:eqlist) {
 			if (ShipBean.SHIP_EQ_GIVE_UP_FAULSE.equals(eq.get(ShipBean.SHIP_EQ_GIVE_UP))) {
 				Double arrivalAmount = (Double) eq.get(ShipBean.SHIP_EQ_ARRIVAL_AMOUNT);
 				Double amount = (Double) eq.get(EqCostListBean.EQ_LIST_AMOUNT);
 				if (amount != arrivalAmount) {
 					close = false;
 					break;
 				}
 			}
 		}
 		
 		if (close) {
 			params.put(ShipBean.SHIP_STATUS, ShipBean.SHIP_STATUS_CLOSE);
 		}
 		
 		return dao.updateById(params, DBBean.SHIP);
 	}
 	
 	// 更新到货通知中已被申请发货的设备数量
 	private void updateArrivalNotice(List<Map<String, Object>> eqlist) {
 		Map<String, Object> noticeKeyEqList = new HashMap<String, Object>();
 		for (Map<String, Object> eq:eqlist) {
 			Map<String, Object> eqList = new HashMap<String, Object>();
 			String noticeId = (String) eq.get(ArrivalNoticeBean.NOTICE_ID);
 			if (noticeKeyEqList.containsKey(noticeId)) {
 				eqList = (Map<String, Object>) noticeKeyEqList.get(noticeId);
 			}
 			eqList.put((String) eq.get(ApiConstants.MONGO_ID), eq);
 			noticeKeyEqList.put(noticeId, eqList);
 		}
 		
 		for (Map.Entry mapEntry : noticeKeyEqList.entrySet()) {
 			Map<String, Object> notice = dao.findOne(ApiConstants.MONGO_ID, mapEntry.getKey(), DBBean.ARRIVAL_NOTICE);
 			List<Map<String, Object>> noticeEqList = (List<Map<String, Object>>) notice.get(ArrivalNoticeBean.EQ_LIST);
 			Map<String, Object> eqIdKey = (Map<String, Object>) mapEntry.getValue();
 			// 是否关闭到货通知 - 全部发货时关闭
 			boolean close = true;
 			for (Map<String, Object> eq:noticeEqList) {
 				if (eqIdKey.containsKey(eq.get(ApiConstants.MONGO_ID))) {
 					Map<String, Object> shipEqInfo = (Map<String, Object>) eqIdKey.get(eq.get(ApiConstants.MONGO_ID));
 					// 发货数量
 					Double shipAmount = (Double) shipEqInfo.get(ShipBean.EQCOST_SHIP_AMOUNT);
 					Double arrivalAmount = (Double) eq.get(ArrivalNoticeBean.EQCOST_ARRIVAL_AMOUNT);
 					eq.put(ShipBean.EQCOST_SHIP_AMOUNT, shipAmount);
 					if (shipAmount != arrivalAmount) {
 						close = false;
 					}
 				} else {
 					close = false;
 				}
 			}
 			if (close) {
 				notice.put(ArrivalNoticeBean.NOTICE_STATUS, ArrivalNoticeBean.NOTICE_STATUS_CLOSE);
 			}
 			notice.put(ArrivalNoticeBean.EQ_LIST, noticeEqList);
 			arrivalService.update(notice);
 		}
 	}
 
 }
