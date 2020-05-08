 package com.pms.service.service.impl;
 
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import com.pms.service.dbhelper.DBQuery;
 import com.pms.service.dbhelper.DBQueryOpertion;
 import com.pms.service.exception.ApiResponseException;
 import com.pms.service.mockbean.ApiConstants;
 import com.pms.service.mockbean.ArrivalNoticeBean;
 import com.pms.service.mockbean.DBBean;
 import com.pms.service.mockbean.GroupBean;
 import com.pms.service.mockbean.ProjectBean;
 import com.pms.service.mockbean.PurchaseCommonBean;
 import com.pms.service.mockbean.PurchaseContract;
 import com.pms.service.mockbean.PurchaseRequest;
 import com.pms.service.mockbean.SalesContractBean;
 import com.pms.service.mockbean.ShipBean;
 import com.pms.service.mockbean.ShipCountBean;
 import com.pms.service.mockbean.UserBean;
 import com.pms.service.service.AbstractService;
 import com.pms.service.service.IArrivalNoticeService;
 import com.pms.service.service.IPurchaseContractService;
 import com.pms.service.service.IPurchaseService;
 import com.pms.service.service.IShipService;
 import com.pms.service.util.ApiUtil;
 import com.pms.service.util.DateUtil;
 import com.pms.service.util.EmailUtil;
 
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
         Map<String, Object> result = dao.findOne(ApiConstants.MONGO_ID, params.get(ApiConstants.MONGO_ID), DBBean.SHIP);
         List<Map<String, Object>> mergeEqListBasicInfo = scs.mergeEqListBasicInfo(result.get(SalesContractBean.SC_EQ_LIST));
         
         List<Map<String, Object>> list =  laodShipRestEqLit(mergeEqListBasicInfo, result.get(ShipBean.SHIP_SALES_CONTRACT_ID).toString(), true);
         
 		scs.mergeCommonFieldsFromSc(result, result.get(ShipBean.SHIP_SALES_CONTRACT_ID));
 
         
         result.put(SalesContractBean.SC_EQ_LIST, list);
         if (result.get(ShipBean.SHIP_DELIVERY_START_DATE) != null) {
             if(result.get(ShipBean.SHIP_DELIVERY_START_DATE) instanceof String){
                 
             }else{
                 result.put(ShipBean.SHIP_DELIVERY_START_DATE, DateUtil.getStringByDate((Date) result.get(ShipBean.SHIP_DELIVERY_START_DATE)));
             }
         }
 
         if (result.get(ShipBean.SHIP_DELIVERY_TIME) != null) {
             if(result.get(ShipBean.SHIP_DELIVERY_TIME) instanceof String){
                 
             }else{
                 result.put(ShipBean.SHIP_DELIVERY_TIME, DateUtil.getStringByDate((Date) result.get(ShipBean.SHIP_DELIVERY_TIME)));
             }
         }
         return result;
     }
 
     public Map<String, Object> list(Map<String, Object> params) {
         mergeMyTaskQuery(params, DBBean.SHIP);
 
         if (isDepotManager()) {
             params.put(ShipBean.SHIP_TYPE, new DBQuery(DBQueryOpertion.NOT_EQUALS, ArrivalNoticeBean.SHIP_TYPE_1_0));
         } else if (isPurchase()) {
             params.put(ShipBean.SHIP_TYPE, ArrivalNoticeBean.SHIP_TYPE_1_0);
 
         }
         return dao.list(params, DBBean.SHIP);
     }
 
 	public Map<String, Object> update(Map<String, Object> params) {
 		return dao.updateById(params, DBBean.SHIP);
 	}
 
 	public void destroy(Map<String, Object> params) {
 		List<String> ids = new ArrayList<String>();
 		ids.add(String.valueOf(params.get(ApiConstants.MONGO_ID)));
 		dao.deleteByIds(ids, DBBean.SHIP);
 	}
 
     public Map<String, Object> create(Map<String, Object> params) {
         if (ApiUtil.isEmpty(params.get(ShipBean.SHIP_STATUS))) {
             params.put(ShipBean.SHIP_STATUS, ShipBean.SHIP_STATUS_DRAFT);
         }
         
         if (params.get(ShipBean.SHIP_DELIVERY_START_DATE) != null) {
            params.put(ShipBean.SHIP_DELIVERY_START_DATE, DateUtil.getDate((String) params.get(ShipBean.SHIP_DELIVERY_START_DATE)));
         }
         if (params.get(ShipBean.SHIP_DELIVERY_TIME) != null) {
            params.put(ShipBean.SHIP_DELIVERY_TIME, DateUtil.getDate((String) params.get(ShipBean.SHIP_DELIVERY_TIME)));   
         }
         
 
         
        List<Map<String, Object>> eqList = (List<Map<String, Object>>) params.get("eqcostList");
        
        for(Map<String, Object> eq: eqList){
            
        	if(!ApiUtil.isValid(eq.get("eqcostShipAmount"))){        		
        		eq.put("eqcostShipAmount", 0);
        	}

        }
        
        
         if (params.get(ApiConstants.MONGO_ID) != null) {
             return update(params);
         } else {
         	// 发货申请编号
         	String code = "FHSQ-";
         	String[] limitKeys = { ShipBean.SHIP_CODE };
         	Map<String, Object> lastRecord = dao.getLastRecordByCreatedOn(DBBean.SHIP, null, limitKeys);
         	String scCode = (String) params.get(ShipBean.SHIP_SALES_CONTRACT_CODE);
         	String[] scCodeArr = scCode.split("-");
         	if (scCodeArr.length > 3) {
         		if (scCodeArr[2].length() > 2) {
         			code += scCodeArr[2].substring(2);
 				} else {
 					int i = Integer.parseInt(scCodeArr[2]);
 					code += String.format("%02d", i);
 				}
         		if (scCodeArr[3].length() > 2) {
         			code += scCodeArr[3].substring(2);
 				} else {
 					int i = Integer.parseInt(scCodeArr[3]);
 					code += String.format("%02d", i);
 				}
         		code += "-";
 			}
         	
         	if (ApiUtil.isEmpty(lastRecord)) {
         		code += "0001";
     		} else {
     			String shipCode = (String) lastRecord.get(ShipBean.SHIP_CODE);
     	    	String codeNum = shipCode.substring(shipCode.length()-4, shipCode.length());
     	    	int i = 0;
     	    	try {
     	    		i = Integer.parseInt(codeNum);
 				} catch (Exception e) {
 					// TODO: handle exception
 				}
     	    	
     	    	i++;
     	    	String str = String.format("%04d", i);
     	    	code += str;
     		}
         	params.put(ShipBean.SHIP_CODE, code);
             return dao.add(params, DBBean.SHIP);
         }
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
         purchaseEqList = scs.mergeEqListBasicInfo(purchaseEqList);
         		
         List<Map<String, Object>> shipMergedEqList = laodShipRestEqLit(purchaseEqList, saleId, false);
               
 		Map<String, Object> res = new HashMap<String, Object>();
 		res.put(ApiConstants.RESULTS_DATA, shipMergedEqList);
 		return res;
 	}
 
     private List<Map<String, Object>> laodShipRestEqLit(List<Map<String, Object>> purchaseEqList, String saleId, boolean loadExists) {
         Map<String, Object> query = new HashMap<String, Object>();
         query.put(SalesContractBean.SC_ID, saleId);
         query.put(ApiConstants.LIMIT_KEYS, ArrivalNoticeBean.EQ_LIST);
 
         Map<String, Integer> arrivedCountMap = countEqByKeyWithMultiKey(query, DBBean.ARRIVAL_NOTICE, ArrivalNoticeBean.EQCOST_ARRIVAL_AMOUNT, null, new String[] { ArrivalNoticeBean.SHIP_TYPE });
 
         // 已发货的数量统计
         Map<String, Object> parameters = new HashMap<String, Object>();
         parameters.put(ShipBean.SHIP_STATUS, new DBQuery(DBQueryOpertion.IN, new String[] { ShipBean.SHIP_STATUS_SUBMIT, ShipBean.SHIP_STATUS_FIRST_APPROVE, ShipBean.SHIP_STATUS_FINAL_APPROVE, ShipBean.SHIP_STATUS_CLOSE }));
         parameters.put(ShipBean.SHIP_SALES_CONTRACT_ID, saleId);
         Map<String, Integer> shipedCountMap = countEqByKeyWithMultiKey(parameters, DBBean.SHIP, ShipBean.EQCOST_SHIP_AMOUNT, null, new String[] { ArrivalNoticeBean.SHIP_TYPE });
         List<Map<String, Object>> shipMergedEqList = new ArrayList<Map<String, Object>>();
         Set<String> shipIds = new HashSet<String>();
 
         for (Map<String, Object> eqMap : purchaseEqList) {
             String shipType = "";
             if (eqMap.get(ArrivalNoticeBean.SHIP_TYPE) != null) {
                 shipType = eqMap.get(ArrivalNoticeBean.SHIP_TYPE).toString();
             }
             Object id = eqMap.get(ApiConstants.MONGO_ID).toString() + shipType;
 
             if (!shipIds.contains(id.toString())) {
                 shipIds.add(id.toString());
                 if (shipedCountMap.get(id) != null && arrivedCountMap.get(id) != null) {
                     eqMap.put(ShipBean.SHIP_LEFT_AMOUNT, arrivedCountMap.get(id) - shipedCountMap.get(id));
                     if (!loadExists) {
                         eqMap.put(ShipBean.EQCOST_SHIP_AMOUNT, arrivedCountMap.get(id) - shipedCountMap.get(id));
                     }
                 } else {
                     eqMap.put(ShipBean.SHIP_LEFT_AMOUNT, arrivedCountMap.get(id));
                     if (!loadExists) {
                         eqMap.put(ShipBean.EQCOST_SHIP_AMOUNT, arrivedCountMap.get(id));
                     }
                 }
                 shipMergedEqList.add(eqMap);
             }
 
         }
 
         if (!loadExists) {
             removeEmptyEqList(shipMergedEqList, ShipBean.SHIP_LEFT_AMOUNT);
         }
         return shipMergedEqList;
     }
 	
     public Map<String, Object> submit(Map<String, Object> params) {
         params.put(ShipBean.SHIP_DATE, ApiUtil.formateDate(new Date(), "yyy-MM-dd"));
         params.put(ShipBean.SHIP_STATUS, ShipBean.SHIP_STATUS_SUBMIT);
         return create(params);
     }
 	   
     public Map<String, Object> approve(Map<String, Object> params) {
         Map<String, Object> oldShip = dao.findOne(ApiConstants.MONGO_ID, params.get(ApiConstants.MONGO_ID), new String[] { SalesContractBean.SC_EQ_LIST, ShipBean.SHIP_STATUS }, DBBean.SHIP);
 
         if (ShipBean.SHIP_STATUS_SUBMIT.equalsIgnoreCase((String) oldShip.get(ShipBean.SHIP_STATUS))) {
             params.put(ShipBean.SHIP_STATUS, ShipBean.SHIP_STATUS_FIRST_APPROVE);
             this.dao.updateById(params, DBBean.SHIP);
 
         } else {
             params.put(ShipBean.SHIP_STATUS, ShipBean.SHIP_STATUS_FINAL_APPROVE);
             this.dao.updateById(params, DBBean.SHIP);
 
             List<Map<String, Object>> eqMapList = (List<Map<String, Object>>) oldShip.get(SalesContractBean.SC_EQ_LIST);
 
             Map<String, Object> shipMap = new HashMap<String, Object>();
             Set<String> contractIds = new HashSet<String>();
             for (Map<String, Object> eq : eqMapList) {
                 if (eq.get(PurchaseContract.PURCHASE_CONTRACT_TYPE) != null && eq.get(PurchaseContract.PURCHASE_CONTRACT_TYPE).toString().equalsIgnoreCase(PurchaseCommonBean.CONTRACT_EXECUTE_BJ_MAKE)) {
                     if (eq.get(PurchaseCommonBean.PURCHASE_CONTRACT_ID) != null) {
                         contractIds.add(eq.get(PurchaseCommonBean.PURCHASE_CONTRACT_ID).toString());
                         shipMap.put(eq.get(PurchaseCommonBean.PURCHASE_CONTRACT_ID).toString(), eq.get(PurchaseCommonBean.PURCHASE_CONTRACT_CODE));
                     }
                 }
             }
 
             for (String contractId : contractIds) {
                 // 只统计此订单下的同样的设备清单
                 Map<String, Object> compareMap = new HashMap<String, Object>();
                 compareMap.put("purchaseContractId", contractId);
 
                 Map<String, Object> query = new HashMap<String, Object>();
                 query.put(ShipBean.SHIP_STATUS, new DBQuery(DBQueryOpertion.IN, new String[] { ShipBean.SHIP_STATUS_FIRST_APPROVE, ShipBean.SHIP_STATUS_CLOSE }));
                 query.put("eqcostList.purchaseContractId", contractId);
                 Map<String, Integer> repCountMap = countEqByKey(query, DBBean.SHIP, ShipBean.EQCOST_SHIP_AMOUNT, null, compareMap);
 
                 Map<String, Object> conQuery = new HashMap<String, Object>();
                 conQuery.put(ApiConstants.MONGO_ID, contractId);
                 Map<String, Integer> contractCountMap = countEqByKey(conQuery, DBBean.PURCHASE_CONTRACT, "eqcostApplyAmount", null);
 
                 boolean sendMail = true;
                 for (String key : contractCountMap.keySet()) {
 
                     if (repCountMap.get(key) == null) {
                         sendMail = false;
                         break;
                     } else {
                         if (repCountMap.get(key) < contractCountMap.get(key)) {
                             sendMail = false;
                             break;
                         }
                     }
                 }
 
                 if (sendMail) {
                     Map<String, Object> userQuery = new HashMap<String, Object>();
                     userQuery.put(UserBean.GROUPS, new DBQuery(DBQueryOpertion.IN, this.dao.findOne(GroupBean.GROUP_NAME, GroupBean.PURCHASE_VALUE, DBBean.USER_GROUP).get(ApiConstants.MONGO_ID)));
                     userQuery.put(ApiConstants.LIMIT_KEYS, UserBean.EMAIL);
                     List<Object> emails = this.dao.listLimitKeyValues(userQuery, DBBean.USER);
                     String subject = String.format("采购合同 - %s -已发货完毕", shipMap.get(contractId));
                     String content = String.format("采购合同 - %s -已发货完毕", shipMap.get(contractId));
                     EmailUtil.sendMail(subject, emails, content);
 
                 }
             }
         }
         return new HashMap<String, Object>();
     }
     
     public Map<String, Object> reject(Map<String, Object> params){
         params.put(ShipBean.SHIP_STATUS, ShipBean.SHIP_STATUS_REJECT);
         return this.dao.updateById(params, DBBean.SHIP); 
     }
 	
 	public Map<String, Object> confirmShipData(Map<String, Object> params) {
 		List<Map<String, Object>> eqlist = (List<Map<String, Object>>) params.get(ShipBean.SHIP_EQ_LIST);
 		boolean close = true;
 //		for (Map<String, Object> eq : eqlist) {
 //
 //			Object shipType = eq.get(ArrivalNoticeBean.SHIP_TYPE);
 //			if (!ApiUtil.isEmpty(shipType)
 //			        && (shipType.toString().equalsIgnoreCase(ArrivalNoticeBean.SHIP_TYPE_1) || shipType.toString().equalsIgnoreCase(ArrivalNoticeBean.SHIP_TYPE_1_0))) {
 //				// 直发才需要检查数量
 //				int arrivalAmount = ApiUtil.getInteger(eq.get(ShipBean.SHIP_EQ_ACTURE_AMOUNT), 0);
 //				int amount = ApiUtil.getInteger(eq.get(ShipBean.EQCOST_SHIP_AMOUNT), 0);
 //				if (amount != arrivalAmount) {
 //					close = false;
 //					break;
 //				}
 //			}
 //		}
 		
 		if (close) {
 			params.put(ShipBean.SHIP_STATUS, ShipBean.SHIP_STATUS_CLOSE);
 			
 			  
             Map<String, Object> ship = dao.findOne(ApiConstants.MONGO_ID, params.get(ApiConstants.MONGO_ID), DBBean.SHIP);
 
             Map<String, Object> repositoryOut = new HashMap<String, Object>();
             repositoryOut.put(ShipBean.SHIP_PROJECT_ID, ship.get(ShipBean.SHIP_PROJECT_ID));
             repositoryOut.put(ShipBean.SHIP_PROJECT_NAME, ship.get(ShipBean.SHIP_PROJECT_NAME));
             repositoryOut.put(ProjectBean.PROJECT_CODE, ship.get(ProjectBean.PROJECT_CODE));
             repositoryOut.put(ShipBean.SHIP_SALES_CONTRACT_ID, ship.get(ShipBean.SHIP_SALES_CONTRACT_ID));
             repositoryOut.put(ShipBean.SHIP_SALES_CONTRACT_CODE, ship.get(ShipBean.SHIP_SALES_CONTRACT_CODE));
             repositoryOut.put("outDate",  ship.get(ShipBean.SHIP_DATE));
             
             
             repositoryOut.put("type", "out");
             repositoryOut.put("status", PurchaseRequest.STATUS_OUT_REPOSITORY_NEED_CONFIRM);
 
             if(ship.get("eqcostList")!=null){
                 
                 List<Map<String, Object>> eqList = (List<Map<String, Object>>) ship.get("eqcostList");
                 
                 for(Map<String, Object> eq: eqList){
                     
                 	if(ApiUtil.isValid(eq.get("eqcostShipAmount"))){
                 		
                 		eq.put("eqcostApplyAmount", eq.get("eqcostShipAmount"));
                 	}else{
                 		eq.put("eqcostApplyAmount", 0);
                 	}
 
                 }
                 removeEmptyEqList(eqList, "eqcostApplyAmount");
                 repositoryOut.put("eqcostList", eqList);
 
             }
             
             scs.mergeCommonFieldsFromSc(repositoryOut,  ship.get(ShipBean.SHIP_SALES_CONTRACT_ID));
 
             pService.updateRepositoryRequest(repositoryOut);
             
 		}
 		
 		return dao.updateById(params, DBBean.SHIP);
 	}
 	
 
 	
 	// 统计三类虚拟的采购合同在每月的发货合计
     public Map<String, Object> doCount(Map<String, Object> params) {
 
         Map<String, Object> shipCount = this.dao.findOne(ApiConstants.MONGO_ID, params.get(ApiConstants.MONGO_ID), DBBean.SHIP_COUNT);
 
         if (shipCount != null && shipCount.get("status").toString().equalsIgnoreCase("已结算")) {
             return shipCount;
         }
 
         String date = (String) shipCount.get(ShipCountBean.SHIP_COUNT_DATE);
         String shipType = (String) shipCount.get(ArrivalNoticeBean.SHIP_TYPE);
 
         SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM");
         Date countDate = null;
         try {
             countDate = sdf.parse(date);
         } catch (ParseException e) {
             // TODO Auto-generated catch block
             e.printStackTrace();
         }
         Calendar cal = Calendar.getInstance();
         cal.setTime(countDate);
         cal.add(Calendar.MONTH, -1);
         cal.set(Calendar.DAY_OF_MONTH, 20);
         Date startDate = cal.getTime();
         
         cal = Calendar.getInstance();
         cal.setTime(countDate);
         cal.set(Calendar.DAY_OF_MONTH, 19);
         Date endDate = cal.getTime();
 
         Map<String, Object> shipQuery = new HashMap<String, Object>();
         
         Object[] dateQuery = { startDate, endDate };
         shipQuery.put(ShipBean.SHIP_DELIVERY_START_DATE, new DBQuery(DBQueryOpertion.BETWEEN_AND, dateQuery));
         
         if(shipType.equalsIgnoreCase(PurchaseCommonBean.CONTRACT_EXECUTE_ALLOCATE_BJ_REPO_VALUE)){
             // 调拨
             shipQuery.put(SalesContractBean.SC_EQ_LIST + "." + ArrivalNoticeBean.SHIP_TYPE, shipType);
         }else{
             // 三类虚拟采购合同
             shipQuery.put(SalesContractBean.SC_EQ_LIST + "." + PurchaseContract.PURCHASE_CONTRACT_TYPE, shipType);
         }
         // 申请状态
         List<String> statusList = new ArrayList<String>();
 //        statusList.add(ShipBean.SHIP_STATUS_APPROVE);
         statusList.add(ShipBean.SHIP_STATUS_CLOSE);
         shipQuery.put(ShipBean.SHIP_STATUS, new DBQuery(DBQueryOpertion.IN, statusList));
         String[] limitKeys = { SalesContractBean.SC_EQ_LIST };
         shipQuery.put(ApiConstants.LIMIT_KEYS, limitKeys);
         Map<String, Object> shipMap = dao.list(shipQuery, DBBean.SHIP);
         List<Map<String, Object>> shipList = (List<Map<String, Object>>) shipMap.get(ApiConstants.RESULTS_DATA);
 
         int totalAmount = 0;
         int totalMonty = 0;
         if (!ApiUtil.isEmpty(shipList)) {
             List<Map<String, Object>>  allShipEqList = new ArrayList<Map<String, Object>>();
             for (Map<String, Object> ship : shipList) {
                 List<Map<String, Object>> shipEqList = (List<Map<String, Object>>) ship.get(SalesContractBean.SC_EQ_LIST);
                 for (Map<String, Object> shipEq : shipEqList) {
                     allShipEqList.add(shipEq);
                     //FIXME: 先取真实发货数
                     totalAmount += ApiUtil.getInteger(shipEq.get(ShipBean.EQCOST_SHIP_AMOUNT), 0);
                     
                     //FIXME: 采购单价
                     totalMonty += ApiUtil.getInteger(shipEq.get(ShipBean.EQCOST_SHIP_AMOUNT), 0) * ApiUtil.getInteger(shipEq.get("eqcostBasePrice"), 0);
                 }
             }
 
 
 
             shipCount.put(ShipCountBean.SHIP_TOTAL_AMOUNT, totalAmount);
             shipCount.put(ShipCountBean.SHIP_TOTAL_MONEY, totalMonty);
             shipCount.put(SalesContractBean.SC_EQ_LIST, allShipEqList);
 
             dao.updateById(shipCount, DBBean.SHIP_COUNT);
         }
 
         return shipCount;
     }
 	
 	// 发货统计
 	public Map<String, Object> listShipCount(Map<String, Object> params) {
 		return dao.list(params, DBBean.SHIP_COUNT);
 	}
 	
 	public Map<String, Object> listCountEq(Map<String, Object> params) {
 		String[] limitKeys = { SalesContractBean.SC_EQ_LIST };
 		Map<String, Object> countMap = dao.findOne(ApiConstants.MONGO_ID, params.get(ApiConstants.MONGO_ID), limitKeys, DBBean.SHIP_COUNT);
 		Map<String, Object> res = new HashMap<String, Object>();
 		res.put(ApiConstants.RESULTS_DATA, scs.mergeEqListBasicInfo(countMap.get(SalesContractBean.SC_EQ_LIST)));
 		return res;
 	}
 	
 	
     public Map<String, Object> getShipCount(Map<String, Object> params) {
         return doCount(params);
     }
 
     public Map<String, Object> submitShipCount(Map<String, Object> params) {        
         Map<String, Object> shipCount = new HashMap<String, Object>();        
         shipCount = this.dao.findOne(ApiConstants.MONGO_ID, params.get(ApiConstants.MONGO_ID),  DBBean.SHIP_COUNT);
         String date = (String) shipCount.get(ShipCountBean.SHIP_COUNT_DATE);
 
         SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM");
         Date countDate = null;
         try {
             countDate = sdf.parse(date);
         } catch (ParseException e) {
             // TODO Auto-generated catch block
             e.printStackTrace();
         }
         Calendar cal = Calendar.getInstance();
         cal.setTime(countDate);        
         cal.add(Calendar.MONTH, 1);
         Date startDate = cal.getTime();
         
         if (startDate.getTime() > new Date().getTime()) {
             throw new ApiResponseException("", "", "结算不能在当月做");
         }
         
         shipCount = new HashMap<String, Object>();
         shipCount.put("status", "已结算");
         shipCount.put(ApiConstants.MONGO_ID, params.get(ApiConstants.MONGO_ID));
         this.dao.updateById(shipCount, DBBean.SHIP_COUNT);
 
         Map<String, Object> nextShipCount = new HashMap<String, Object>();
         nextShipCount.put(ArrivalNoticeBean.SHIP_TYPE, shipCount.get(ArrivalNoticeBean.SHIP_TYPE));
         nextShipCount.put(ShipCountBean.SHIP_COUNT_DATE, sdf.format(startDate));
         nextShipCount.put(ShipCountBean.SHIP_TOTAL_AMOUNT, 0);
         nextShipCount.put(ShipCountBean.SHIP_TOTAL_MONEY, 0);
         nextShipCount.put("status", "未结算");
         
 
         this.dao.add(nextShipCount, DBBean.SHIP_COUNT);
         
         return null;
     }
 
 	
 
 }
