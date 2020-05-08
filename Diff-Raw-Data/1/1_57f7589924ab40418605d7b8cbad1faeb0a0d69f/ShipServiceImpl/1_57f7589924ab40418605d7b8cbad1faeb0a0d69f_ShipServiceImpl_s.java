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
 
 import com.pms.service.cfg.ConfigurationManager;
 import com.pms.service.dbhelper.DBQuery;
 import com.pms.service.dbhelper.DBQueryOpertion;
 import com.pms.service.mockbean.ApiConstants;
 import com.pms.service.mockbean.ArrivalNoticeBean;
 import com.pms.service.mockbean.DBBean;
 import com.pms.service.mockbean.GroupBean;
 import com.pms.service.mockbean.PurchaseCommonBean;
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
         result.put(SalesContractBean.SC_EQ_LIST, scs.mergeEqListBasicInfo(result.get(SalesContractBean.SC_EQ_LIST)));
         return result;
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
         if (params.get(ShipBean.SHIP_STATUS) == null) {
             params.put(ShipBean.SHIP_STATUS, ShipBean.SHIP_STATUS_DRAFT);
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
 		
 		//已发货的数量统计
         Map<String, Object> parameters = new HashMap<String, Object>();
 //        parameters.put(ShipBean.SHIP_STATUS, ShipBean.SHIP_STATUS_APPROVE);
         parameters.put(ShipBean.SHIP_SALES_CONTRACT_ID, saleId);
         Map<String, Integer> shipedCountMap = countEqByKey(parameters, DBBean.SHIP, ShipBean.EQCOST_SHIP_AMOUNT, null);
         
         for (Map<String, Object> eqMap : purchaseEqList) {
             int arriveCount = ApiUtil.getInteger(eqMap.get(ArrivalNoticeBean.EQCOST_ARRIVAL_AMOUNT), 0);
             if (shipedCountMap.get(eqMap.get(ApiConstants.MONGO_ID)) != null) {
                 eqMap.put(ShipBean.SHIP_LEFT_AMOUNT, arriveCount - shipedCountMap.get(eqMap.get(ApiConstants.MONGO_ID)));
                 eqMap.put(ShipBean.EQCOST_SHIP_AMOUNT, arriveCount - shipedCountMap.get(eqMap.get(ApiConstants.MONGO_ID)));
             } else {
                 eqMap.put(ShipBean.SHIP_LEFT_AMOUNT, arriveCount);
                 eqMap.put(ShipBean.EQCOST_SHIP_AMOUNT, arriveCount);
             }
         }
 
 		Map<String, Object> res = new HashMap<String, Object>();
 		res.put(ApiConstants.RESULTS_DATA, purchaseEqList);
 		return res;
 	}
 	
     public Map<String, Object> submit(Map<String, Object> params) {
         params.put(ShipBean.SHIP_DATE, ApiUtil.formateDate(new Date(), "yyy-MM-dd"));
         params.put(ShipBean.SHIP_STATUS, ShipBean.SHIP_STATUS_SUBMIT);
         return create(params);
     }
 	   
     public Map<String, Object> approve(Map<String, Object> params){
         params.put(ShipBean.SHIP_STATUS, ShipBean.SHIP_STATUS_APPROVE);
         this.dao.updateById(params, DBBean.SHIP);
         
         Map<String, Object> eqList = dao.findOne(ApiConstants.MONGO_ID, params.get(ApiConstants.MONGO_ID), new String[] { SalesContractBean.SC_EQ_LIST },
                 DBBean.SHIP);  
         
         List<Map<String, Object>> eqMapList = (List<Map<String, Object>>) eqList.get(SalesContractBean.SC_EQ_LIST);
         
         Map<String, Object> shipMap = new HashMap<String, Object>();
         Set<String> contractIds = new HashSet<String>();
         for (Map<String, Object> eq : eqMapList) {
             if (eq.get(PurchaseCommonBean.CONTRACT_EXECUTE_CATE) != null && eq.get(PurchaseCommonBean.CONTRACT_EXECUTE_CATE).toString().equalsIgnoreCase(PurchaseCommonBean.CONTRACT_EXECUTE_BJ_MAKE)) {
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
             query.put(ShipBean.SHIP_STATUS, new DBQuery(DBQueryOpertion.IN, new String[]{ShipBean.SHIP_STATUS_APPROVE, ShipBean.SHIP_STATUS_CLOSE}));            
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
         
         return new HashMap<String, Object>();
     }
     
     public Map<String, Object> reject(Map<String, Object> params){
         params.put(ShipBean.SHIP_STATUS, ShipBean.SHIP_STATUS_REJECT);
         return this.dao.updateById(params, DBBean.SHIP); 
     }
 	
 	public Map<String, Object> record(Map<String, Object> params) {
 		List<Map<String, Object>> eqlist = (List<Map<String, Object>>) params.get(ShipBean.SHIP_EQ_LIST);
 		boolean close = true;
         for (Map<String, Object> eq : eqlist) {
 
             if (ApiUtil.isEmpty(eq.get(ShipBean.REPOSITORY_NAME))) {
                 //直发才需要检查数量
                 int arrivalAmount = ApiUtil.getInteger(eq.get(ShipBean.SHIP_EQ_ACTURE_AMOUNT), 0);
                 int amount = ApiUtil.getInteger(eq.get(ShipBean.EQCOST_SHIP_AMOUNT), 0);
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
 	
 	// 统计三类虚拟的采购合同在每月的发货合计
 	public Map<String, Object> doCount(Map<String, Object> params) {
 		String date = (String) params.get(ShipCountBean.SHIP_COUNT_DATE);
 		String cate =  (String) params.get(PurchaseCommonBean.CONTRACT_EXECUTE_CATE);
 		
 		String key = null;
 		if (PurchaseCommonBean.CONTRACT_EXECUTE_CATE_BEIJINGDAICAI.equals(cate)) {
 			key = "bjdc";
 		} else if (PurchaseCommonBean.CONTRACT_EXECUTE_BJ_REPO.equals(cate)) {
 			key = "bjkc";
 		} else if (PurchaseCommonBean.CONTRACT_EXECUTE_BJ_MAKE.equals(cate)) {
 			key = "bjsc";
 		}
 		
 		date = ConfigurationManager.getProperty(key);
 		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM");
 		Date dt = null;
 		try {
 			dt = sdf.parse(date);
 		} catch (ParseException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		Calendar cal = Calendar.getInstance();
 		cal.setTime(dt);
 		cal.add(Calendar.MONTH, 1);
 		Date dt1 = cal.getTime();
 		String nextCountDate = sdf.format(dt1);
 		ConfigurationManager.setProperties(key, nextCountDate);
 		
 		Map<String, Object> shipQuery = new HashMap<String, Object>();
 		// 日期范围
 		cal.add(Calendar.MONTH, -2);
 		Date dt2 = cal.getTime();
 		String startDate = sdf.format(dt2) + "-21";
 		String endDate = date + "-20";
 		Object[] ta = { startDate, endDate };
 		shipQuery.put(ShipBean.SHIP_ISSUE_TIME, new DBQuery(DBQueryOpertion.BETWEEN_AND, ta));
 		// 三类虚拟采购合同
 		shipQuery.put(SalesContractBean.SC_EQ_LIST+"."+PurchaseCommonBean.CONTRACT_EXECUTE_CATE, cate);
 		// 申请状态
 		List<String> statusList = new ArrayList<String>();
 		statusList.add(ShipBean.SHIP_STATUS_APPROVE);
 		statusList.add(ShipBean.SHIP_STATUS_CLOSE);
 		shipQuery.put(ShipBean.SHIP_STATUS, new DBQuery(DBQueryOpertion.IN, statusList));
 		String[] limitKeys = { SalesContractBean.SC_EQ_LIST };
 		shipQuery.put(ApiConstants.LIMIT_KEYS, limitKeys);
 		Map<String, Object> shipMap = dao.list(shipQuery, DBBean.SHIP);
 		List<Map<String, Object>> shipList = (List<Map<String, Object>>) shipMap.get(ApiConstants.RESULTS_DATA);
 		
 		Map<String, Object> returnMap = new HashMap<String, Object>();
 		if (!ApiUtil.isEmpty(shipList)) {
 			// 采购订单id
 			Set<Object> orderIdSet = new HashSet();
 			
 			for (Map<String, Object> ship : shipList) {
 				List<Map<String, Object>> shipEqList = (List<Map<String, Object>>) ship.get(SalesContractBean.SC_EQ_LIST);
 				for (Map<String, Object> shipEq : shipEqList) {
 					orderIdSet.add(shipEq.get(PurchaseCommonBean.PURCHASE_ORDER_ID));
 				}
 			}
 			
 			// 获取采购订单中的采购价格
 			Map<String, Object> orderQuery = new HashMap<String, Object>();
 			orderQuery.put(ApiConstants.MONGO_ID, new DBQuery(DBQueryOpertion.IN, new ArrayList<Object>(orderIdSet)));
 			Map<String, Object> orderInfoMap = dao.listToOneMapAndIdAsKey(orderQuery, DBBean.PURCHASE_ORDER);
 			
 			Map<String, Object> eqIdKeyMap = new HashMap<String, Object>();
 			Map<String, Object> orderIdKeyMap = new HashMap<String, Object>();
 			
 			for (Map.Entry mapEntry : orderInfoMap.entrySet()) {
 				// 采购订单id
 				String orderId =  (String) mapEntry.getKey();
 				// 对应采购订单信息
 				Map<String, Object> orderMap = (Map<String, Object>) mapEntry.getValue();
 				List<Map<String, Object>> orderEqList = (List<Map<String, Object>>) orderMap.get(SalesContractBean.SC_EQ_LIST);
 				for (Map<String, Object> orderEq : orderEqList) {
 					eqIdKeyMap.put((String) orderEq.get(ApiConstants.MONGO_ID), orderEq.get(PurchaseCommonBean.EQCOST_PRODUCT_UNIT_PRICE));
 				}
 				orderIdKeyMap.put(orderId, eqIdKeyMap);
 			}
 			
 			Map<String, Object> resultMap = new HashMap<String, Object>();
 			
 			Double totalAmount = 0.0;
 			Double totalMoney = 0.0;
 			
 			for (Map<String, Object> ship : shipList) {
 				List<Map<String, Object>> shipEqList = (List<Map<String, Object>>) ship.get(SalesContractBean.SC_EQ_LIST);
 				for (Map<String, Object> shipEq : shipEqList) {
 					String eqId = (String) shipEq.get(ApiConstants.MONGO_ID);
 					// 发货数量
 					Double shipAmount;
 					if (shipEq.containsKey(ShipBean.SHIP_EQ_ACTURE_AMOUNT)) {
 						shipAmount = (Double) ApiUtil.getDouble(shipEq, ShipBean.SHIP_EQ_ACTURE_AMOUNT, 0);
 					} else {
 						shipAmount = (Double) ApiUtil.getDouble(shipEq, ShipBean.EQCOST_SHIP_AMOUNT, 0);
 					}
 					totalAmount += shipAmount;
 					
 					Map<String, Object> map = (Map<String, Object>) orderIdKeyMap.get(shipEq.get(PurchaseCommonBean.PURCHASE_ORDER_ID));
 					// 价格
 					Double eqcostProductUnitPrice = ApiUtil.getDouble(map, eqId, 0);
 					// 发货金额
 					Double money = shipAmount * eqcostProductUnitPrice;
 					
 					totalMoney += money;
 					
 					Map<String, Object> listMap = new HashMap<String, Object>();
 					if (resultMap.containsKey(eqId)) {
 						Map<String, Object> countMap = (Map<String, Object>) resultMap.get(eqId);
 						listMap.put(ShipCountBean.VC_SHIP_AMOUNT, shipAmount + ApiUtil.getDouble(countMap, ShipCountBean.VC_SHIP_AMOUNT, 0));
 						listMap.put(ShipCountBean.VC_SHIP_MONEY, money + ApiUtil.getDouble(countMap, ShipCountBean.VC_SHIP_MONEY, 0));
 					} else {
 						listMap.put(ApiConstants.MONGO_ID, eqId);
 						listMap.put(ShipCountBean.VC_SHIP_AMOUNT, shipAmount);
 						listMap.put(ShipCountBean.VC_SHIP_MONEY, money);
 					}
 					resultMap.put(eqId, listMap);
 				}
 			}
 			
 			List<Map<String, Object>> returnList = new ArrayList<Map<String, Object>>();
 			for (Map.Entry mapEntry : resultMap.entrySet()) {
 				returnList.add((Map<String, Object>) mapEntry.getValue());
 			}
 			
 			Map<String, Object> map = new HashMap<String, Object>();
 			map.put(ShipCountBean.SHIP_COUNT_DATE, date);
 			map.put(PurchaseCommonBean.CONTRACT_EXECUTE_CATE, cate);
 			map.put(ShipCountBean.SHIP_TOTAL_AMOUNT, totalAmount);
 			map.put(ShipCountBean.SHIP_TOTAL_MONEY, totalMoney);
 			map.put(SalesContractBean.SC_EQ_LIST, returnList);
 			
 			returnMap = dao.add(map, DBBean.SHIP_COUNT);
 		}
 		
 		return returnMap;
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
 	
 	public Map<String, Object> getCountDate() {
 		Map<String, Object> dateMap = new HashMap<String, Object>();
 		dateMap.put("bjdc", ConfigurationManager.getProperty("bjdc"));
 		dateMap.put("bjkc", ConfigurationManager.getProperty("bjkc"));
 		dateMap.put("bjsc", ConfigurationManager.getProperty("bjsc"));
 		return dateMap;
 	}
 	
 	private void aotuCountShip() {
 		// 取最后生成的一条记录 - 判断是否需要统计
     	String[] limitKeys = { ShipCountBean.SHIP_COUNT_DATE };
     	Map<String, Object> lastRecord = dao.getLastRecordByCreatedOn(DBBean.SHIP_COUNT, null, limitKeys);
         
         Map<String, Object> shipQuery = new HashMap<String, Object>();
     	
         Boolean count = true;
         
     	if (!ApiUtil.isEmpty(lastRecord)) {
     		String lastCountDate = (String) lastRecord.get(ShipCountBean.SHIP_COUNT_DATE);
     		String[] lastCountDateArr = lastCountDate.split("-");
     		int lastCountMonth = Integer.parseInt(lastCountDateArr[1]);
     		
         	Calendar cal = Calendar.getInstance();
             int day = cal.get(Calendar.DATE);
             int month = cal.get(Calendar.MONTH) + 1;
             int year = cal.get(Calendar.YEAR);
             if (day < 21) {
             	month--;
     		}
             if (lastCountMonth < month) {
             	String lastCountMonthStr = String.format("%02d", lastCountMonth);
         		String startDate = lastCountDateArr[0] + "-" + lastCountMonthStr + "-21";
         		String monthStr = String.format("%02d", month);
         		String endDate = year + "-" + monthStr + "-20";
             	Object[] ta = { startDate, endDate };
             	shipQuery.put(ShipBean.SHIP_ISSUE_TIME, new DBQuery(DBQueryOpertion.BETWEEN_AND, ta));
     		} else {
     			count = false;
 			}
 		}
     	
     	if (count) {
     		// 三类虚拟采购合同
     		shipQuery.put(SalesContractBean.SC_EQ_LIST+"."+PurchaseCommonBean.CONTRACT_EXECUTE_CATE, new DBQuery(DBQueryOpertion.NOT_NULL));
     		// 申请状态
     		List<String> statusList = new ArrayList<String>();
     		statusList.add(ShipBean.SHIP_STATUS_APPROVE);
     		statusList.add(ShipBean.SHIP_STATUS_CLOSE);
     		shipQuery.put(ShipBean.SHIP_STATUS, new DBQuery(DBQueryOpertion.IN, statusList));
     		String[] shipLimitKeys = { ShipBean.SHIP_ISSUE_TIME };
     		shipQuery.put(ApiConstants.LIMIT_KEYS, shipLimitKeys);
     		Map<String, Object> shipMap = dao.list(shipQuery, DBBean.SHIP);
     		List<Map<String, Object>> shipList = (List<Map<String, Object>>) shipMap.get(ApiConstants.RESULTS_DATA);
     		
     		Set<String> countDates = new HashSet<String>();
     		for (Map<String, Object> ship : shipList) {
     			String issueDate = (String) ship.get(ShipBean.SHIP_ISSUE_TIME);
     			String[] issueDateArr = issueDate.split("-");
     			int issueMonth = Integer.parseInt(issueDateArr[1]);
     			int issueDay = Integer.parseInt(issueDateArr[2]);
     			if (issueDay > 20) {
         			issueMonth++;
     			}
     			String countDate = issueDateArr[0] + "-" + String.format("%02d", issueMonth);
     			countDates.add(countDate);
     		}
     		
     		for (String string : countDates) {
 //    			shipCountOfVPC(PurchaseCommonBean.CONTRACT_EXECUTE_CATE_BEIJINGDAICAI, string);
 //    			shipCountOfVPC(PurchaseCommonBean.CONTRACT_EXECUTE_BJ_REPO, string);
 //    			shipCountOfVPC(PurchaseCommonBean.CONTRACT_EXECUTE_BJ_MAKE, string);
     		}
 		}
 	}
 
 }
