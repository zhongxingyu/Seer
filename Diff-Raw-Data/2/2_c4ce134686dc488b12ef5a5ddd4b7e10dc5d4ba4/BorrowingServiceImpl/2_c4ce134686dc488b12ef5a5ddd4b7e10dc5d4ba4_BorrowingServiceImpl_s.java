 package com.pms.service.service.impl;
 
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import com.pms.service.dbhelper.DBQuery;
 import com.pms.service.dbhelper.DBQueryOpertion;
 import com.pms.service.dbhelper.DBQueryUtil;
 import com.pms.service.exception.ApiResponseException;
 import com.pms.service.mockbean.ApiConstants;
 import com.pms.service.mockbean.BorrowingBean;
 import com.pms.service.mockbean.CustomerBean;
 import com.pms.service.mockbean.DBBean;
 import com.pms.service.mockbean.EqCostListBean;
 import com.pms.service.mockbean.ProjectBean;
 import com.pms.service.mockbean.ReturnBean;
 import com.pms.service.mockbean.SalesContractBean;
 import com.pms.service.mockbean.ShipBean;
 import com.pms.service.mockbean.UserBean;
 import com.pms.service.service.AbstractService;
 import com.pms.service.service.IArrivalNoticeService;
 import com.pms.service.service.IBorrowingService;
 import com.pms.service.service.IPurchaseContractService;
 import com.pms.service.service.IReturnService;
 import com.pms.service.service.IShipService;
 import com.pms.service.util.ApiUtil;
 import com.pms.service.util.status.ResponseCodeConstants;
 
 public class BorrowingServiceImpl extends AbstractService implements IBorrowingService {
 	
 	private IPurchaseContractService pService;
 	
 	private IShipService shipService;
 	
 	private IReturnService returnService;
 
 	private IArrivalNoticeService arrivalService;
 
 	   
 	public IShipService getShipService() {
 		return shipService;
 	}
 
 	public void setShipService(IShipService shipService) {
 		this.shipService = shipService;
 	}
 
 	public IPurchaseContractService getpService() {
 		return pService;
 	}
 
 	public void setpService(IPurchaseContractService pService) {
 		this.pService = pService;
 	}
 	
 
 	public IReturnService getReturnService() {
 		return returnService;
 	}
 
 	public void setReturnService(IReturnService returnService) {
 		this.returnService = returnService;
 	}
 
 	
 	public IArrivalNoticeService getArrivalService() {
         return arrivalService;
     }
 
     public void setArrivalService(IArrivalNoticeService arrivalService) {
         this.arrivalService = arrivalService;
     }
 
     @Override
 	public String geValidatorFileName() {
 		return "borrowing";
 	}
 	
 	public Map<String, Object> get(Map<String, Object> params) {
 		Map<String, Object> result = dao.findOne(ApiConstants.MONGO_ID, params.get(ApiConstants.MONGO_ID), DBBean.BORROWING);
 //		String inProjectId = result.get(BorrowingBean.BORROW_IN_PROJECT_ID).toString();
 //		String outProjectId = result.get(BorrowingBean.BORROW_OUT_PROJECT_ID).toString();
 		return result;
 	}
 
 	public Map<String, Object> list(Map<String, Object> params) {
 
 	    mergeMyTaskQuery(params, DBBean.BORROWING);
 		Map<String, Object> result = dao.list(params, DBBean.BORROWING);
 		
 		List<Map<String, Object>> list = (List<Map<String, Object>>) result.get(ApiConstants.RESULTS_DATA);
 		
 		List<String> pId = new ArrayList<String>();
 		for (Map<String, Object> p:list){
 			if (p.containsKey(BorrowingBean.BORROW_IN_PROJECT_ID)) {
 				String inId = p.get(BorrowingBean.BORROW_IN_PROJECT_ID).toString();
 				if (!inId.equals("")) {
 					pId.add(inId.toString());
 				}
 			}
 			if (p.containsKey(BorrowingBean.BORROW_OUT_PROJECT_ID)) {
 				String outId = p.get(BorrowingBean.BORROW_OUT_PROJECT_ID).toString();
 				if (!outId.equals("")) {
 					pId.add(outId.toString());
 				}
 			}
 		}
 		
 		// 有项目信息需要获取
 		if (!pId.isEmpty()) {
 			// 获取project信息
 			Map<String, Object> queryContract = new HashMap<String, Object>();
 			queryContract.put(ApiConstants.MONGO_ID, new DBQuery(DBQueryOpertion.IN, pId));
 			Map<String, Object> cInfoMap = dao.listToOneMapByKey(queryContract, DBBean.PROJECT, ApiConstants.MONGO_ID);
 			
 			List<String> pmId = new ArrayList<String>();
 			
 			for (Map<String, Object> p:list){
 				String inProjectId = p.get(BorrowingBean.BORROW_IN_PROJECT_ID).toString();
 				Map<String, Object> inProjectMap = (Map<String, Object>) cInfoMap.get(inProjectId);
 				if (inProjectMap != null) {
 					p.put(BorrowingBean.BORROW_IN_PROJECT_CODE, inProjectMap.get(ProjectBean.PROJECT_CODE));
 					p.put(BorrowingBean.BORROW_IN_PROJECT_NAME, inProjectMap.get(ProjectBean.PROJECT_NAME));
 					p.put(BorrowingBean.BORROW_IN_PROJECT_MANAGER, inProjectMap.get(ProjectBean.PROJECT_MANAGER));
 					pmId.add(inProjectMap.get(ProjectBean.PROJECT_MANAGER).toString());
 				}
 				
 				String outProjectId = p.get(BorrowingBean.BORROW_OUT_PROJECT_ID).toString();
 				Map<String, Object> outProjectMap = (Map<String, Object>) cInfoMap.get(outProjectId);
 				if (outProjectMap != null) {
 					p.put(BorrowingBean.BORROW_OUT_PROJECT_CODE, outProjectMap.get(ProjectBean.PROJECT_CODE));
 					p.put(BorrowingBean.BORROW_OUT_PROJECT_NAME, outProjectMap.get(ProjectBean.PROJECT_NAME));
 					p.put(BorrowingBean.BORROW_OUT_PROJECT_MANAGER, outProjectMap.get(ProjectBean.PROJECT_MANAGER));
 					pmId.add(outProjectMap.get(ProjectBean.PROJECT_MANAGER).toString());
 				}
 			}
 			
 			if (!pmId.isEmpty()) {
 				// 获取项目负责人信息
 				Map<String, Object> pmQueryContract = new HashMap<String, Object>();
 				pmQueryContract.put(ApiConstants.MONGO_ID, new DBQuery(DBQueryOpertion.IN, pmId));
 				pmQueryContract.put(ApiConstants.LIMIT_KEYS, new String[] {UserBean.USER_NAME});
 				Map<String, Object> pmMap = dao.listToOneMapByKey(pmQueryContract, DBBean.USER, ApiConstants.MONGO_ID);
 				
 				for (Map<String, Object> p:list){
 					if (p.containsKey(BorrowingBean.BORROW_IN_PROJECT_MANAGER)) {
 						String inPmId = p.get(BorrowingBean.BORROW_IN_PROJECT_MANAGER).toString();
 						if (!inPmId.equals("")) {
 							Map<String, Object> inPmMap = (Map<String, Object>) pmMap.get(inPmId);
 							if (inPmMap != null) {
 								p.put(BorrowingBean.BORROW_IN_PROJECT_MANAGER, inPmMap.get(UserBean.USER_NAME));
 							}
 						}
 					}
 					
 					if (p.containsKey(BorrowingBean.BORROW_OUT_PROJECT_MANAGER)) {
 						String outPmId = p.get(BorrowingBean.BORROW_OUT_PROJECT_MANAGER).toString();
 						if (!outPmId.equals("")) {
 							Map<String, Object> outPmMap = (Map<String, Object>) pmMap.get(outPmId);
 							if (outPmMap != null) {
 								p.put(BorrowingBean.BORROW_OUT_PROJECT_MANAGER, outPmMap.get(UserBean.USER_NAME));
 							}
 						}
 					}
 				}
 			}
 		}
 		
 		return result;
 	}
 
 	public Map<String, Object> update(Map<String, Object> params) {
 		// 变回草稿状态
 		params.put(BorrowingBean.BORROW_STATUS, "0");
 		return dao.updateById(params, DBBean.BORROWING);
 	}
 
 	public void destroy(Map<String, Object> params) {
 		List<String> ids = new ArrayList<String>();
 		ids.add(String.valueOf(params.get(ApiConstants.MONGO_ID)));
 		dao.deleteByIds(ids, DBBean.BORROWING);
 	}
 
 	public Map<String, Object> create(Map<String, Object> params) {
 		params.put(BorrowingBean.BORROW_STATUS, "0");
 		Map<String, Object> user = dao.findOne(ApiConstants.MONGO_ID, getCurrentUserId(), DBBean.USER);
     	params.put(BorrowingBean.BORROW_APPLICANT, user.get(UserBean.USER_NAME));
 		return dao.add(params, DBBean.BORROWING);
 	}
 	
 	public Map<String, Object> eqlist(Map<String, Object> params) {
 		
 		String saleId = (String) params.get(BorrowingBean.BORROW_IN_SALES_CONTRACT_ID);
 		
 		// 已采购的货物
 		List<Map<String, Object>> purchaseEqList = pService.listApprovedPurchaseContractCosts(saleId);
 		
 		// 已到货货品
 		Map<String, Object> map = arrivalService.listByScIdForBorrowing(saleId);
 		
 		List<Map<String, Object>> shipedEqList = (List<Map<String, Object>>) map.get(SalesContractBean.SC_EQ_LIST);
 		
 		Map<String, Double> alloEqList = new HashMap<String, Double>();
 
 		// 采购
 		for (Map<String, Object> p:purchaseEqList){
 			if (p != null) {
 				String id = p.get(ApiConstants.MONGO_ID).toString();
				Double amount = (Double) p.get(EqCostListBean.EQ_LIST_AMOUNT);
 				alloEqList.put(id, amount);
 			}
 		}
 		
 		// 结果数据
 		List<Map<String, Object>> result = new ArrayList<Map<String, Object>>();
 		
 		if (alloEqList != null) {
 			// - 已到货
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
 			for (Map.Entry mapEntry : alloEqList.entrySet()) {
 				Map<String, Object> eqMap = (Map<String, Object>) eqInfoMap.get(mapEntry.getKey().toString());
 				if (eqMap != null) {
 					eqMap.put(EqCostListBean.EQ_LIST_AMOUNT, mapEntry.getValue());
 					result.add(eqMap);
 				}
 			}
 		}
 		
 		
 		
 		Map<String, Object> res = new HashMap<String, Object>();
 		res.put(ApiConstants.RESULTS_DATA, result);
 		return res;
 	}
 
 	public Map<String, Object> option(Map<String, Object> params) {
 		Map<String, Object> result = null;
 		if (params.containsKey(BorrowingBean.BORROW_STATUS)) {
 			String status = params.get(BorrowingBean.BORROW_STATUS).toString();
 			Map<String, Object> cc = dao.findOne(ApiConstants.MONGO_ID, params.get(ApiConstants.MONGO_ID), DBBean.BORROWING);
 	        params.put(ApiConstants.MONGO_ID, cc.get(ApiConstants.MONGO_ID));
 	        params.put(BorrowingBean.BORROW_STATUS, status);
 	        
 	        if (status.equals("1")) {
 	    		params.put(BorrowingBean.BORROW_DATE, ApiUtil.formateDate(new Date(), "yyy-MM-dd"));
 			}
 
 	        result =  dao.updateById(params, DBBean.BORROWING);
 	        
 	        // 库管批准借货申请
 	        if (status.equals("2")) {
 	        	Map<String, Object> borrowing = dao.findOne(ApiConstants.MONGO_ID, params.get(ApiConstants.MONGO_ID), DBBean.BORROWING);
 	        	createShip(borrowing);
 	        	createReturn(borrowing);
 			}
 		}
         
         return result;
     }
 	
 	// 生成发货申请
 	private Map<String, Object> createShip(Map<String, Object> params) {
 		Map<String, Object> shipParams = new HashMap<String, Object>();
     	shipParams.put(ShipBean.SHIP_CODE, params.get(ShipBean.SHIP_CODE));
     	shipParams.put(ShipBean.SHIP_DEPARTMENT, params.get(ShipBean.SHIP_DEPARTMENT));
     	shipParams.put(ShipBean.SHIP_STATUS, ShipBean.SHIP_STATUS_APPROVE);
     	shipParams.put(ShipBean.SHIP_DATE, ApiUtil.formateDate(new Date(), "yyy-MM-dd"));
     	shipParams.put(ShipBean.SHIP_TYPE, params.get(ShipBean.SHIP_TYPE));
     	shipParams.put(ShipBean.SHIP_WAREHOUSE, params.get(ShipBean.SHIP_WAREHOUSE));
     	shipParams.put(ShipBean.SHIP_PROJECT_ID, params.get(BorrowingBean.BORROW_IN_PROJECT_ID));
     	shipParams.put(ShipBean.SHIP_PROJECT_NAME, params.get(BorrowingBean.BORROW_IN_PROJECT_NAME));
     	shipParams.put(ShipBean.SHIP_SALES_CONTRACT_ID, params.get(BorrowingBean.BORROW_IN_SALES_CONTRACT_ID));
     	shipParams.put(ShipBean.SHIP_SALES_CONTRACT_CODE, params.get(BorrowingBean.BORROW_IN_SALES_CONTRACT_CODE));
     	shipParams.put(ShipBean.SHIP_SALES_CONTRACT_TYPE, params.get(BorrowingBean.BORROW_IN_SALES_CONTRACT_TYPE));
     	shipParams.put(ShipBean.SHIP_CUSTOMER_NAME, params.get(BorrowingBean.BORROW_IN_PROJECT_CUSTOMER));
     	shipParams.put(ShipBean.SHIP_DELIVERY_CONTACT, params.get(ShipBean.SHIP_DELIVERY_CONTACT));
     	shipParams.put(ShipBean.SHIP_DELIVERY_CONTACTWAY, params.get(ShipBean.SHIP_DELIVERY_CONTACTWAY));
     	shipParams.put(ShipBean.SHIP_DELIVERY_UNIT, params.get(ShipBean.SHIP_DELIVERY_UNIT));
     	shipParams.put(ShipBean.SHIP_DELIVERY_ADDRESS, params.get(ShipBean.SHIP_DELIVERY_ADDRESS));
     	shipParams.put(ShipBean.SHIP_ISSUE_TIME, params.get(ShipBean.SHIP_ISSUE_TIME));
     	shipParams.put(ShipBean.SHIP_DELIVERY_TIME, params.get(ShipBean.SHIP_DELIVERY_TIME));
     	shipParams.put(ShipBean.SHIP_DELIVERY_REQUIREMENTS, params.get(ShipBean.SHIP_DELIVERY_REQUIREMENTS));
     	shipParams.put(ShipBean.SHIP_OTHER_DELIVERY_REQUIREMENTS, params.get(ShipBean.SHIP_OTHER_DELIVERY_REQUIREMENTS));
     	shipParams.put(ShipBean.SHIP_EQ_LIST, params.get(BorrowingBean.SHIP_EQ_LIST));
 		return shipService.create(shipParams);
 	}
 	
 	// 生成待还货记录
 	private Map<String, Object> createReturn(Map<String, Object> params) {
 		Map<String, Object> returnParams = new HashMap<String, Object>();
 		returnParams.put(ReturnBean.BORROW_ID, params.get(ApiConstants.MONGO_ID));
 		returnParams.put(ReturnBean.BORROW_CODE, params.get(BorrowingBean.BORROW_CODE));
 		return returnService.create(returnParams);
 	}
 	
 	public Map<String, Object> listScByProjectForBorrowing(Map<String, Object> params) {
 		String pId = null;
 		if (params.containsKey(SalesContractBean.SC_PROJECT_ID)) {
 			pId = params.get(SalesContractBean.SC_PROJECT_ID).toString();
 		}
 		
 		if (ApiUtil.isEmpty(pId)){
 			throw new ApiResponseException(String.format("Project id is empty", params), ResponseCodeConstants.PROJECT_ID_IS_EMPTY.toString());
 		}
 		Map<String, Object> project = dao.findOne(ApiConstants.MONGO_ID, pId, DBBean.PROJECT);
 		Map<String, Object> customer = dao.findOne(ApiConstants.MONGO_ID, project.get(ProjectBean.PROJECT_CUSTOMER), DBBean.CUSTOMER);
 		String cName = (String) customer.get(CustomerBean.NAME);
 		
 		Map<String, Object> projectQuery = new HashMap<String, Object>();
 		projectQuery.put(SalesContractBean.SC_PROJECT_ID, pId);
 		
 		Map<String, Object> statusQuery = new HashMap<String, Object>();
 		statusQuery.put(SalesContractBean.SC_RUNNING_STATUS, SalesContractBean.SC_RUNNING_STATUS_RUNNING);
 		
 		Map<String, Object> typeQuery = new HashMap<String, Object>();
 		typeQuery.put(SalesContractBean.SC_TYPE, SalesContractBean.SC_TYPE_SC_WIRING);
 		typeQuery.put(SalesContractBean.SC_TYPE, SalesContractBean.SC_TYPE_INTEGRATION_WIRING);
 
 		Map<String, Object> query = new HashMap<String, Object>();
 		query.put("project", DBQueryUtil.buildQueryObject(projectQuery, false));
 		query.put("status", DBQueryUtil.buildQueryObject(statusQuery, false));
 		query.put("type", DBQueryUtil.buildQueryObject(typeQuery, false));
 		query.put(ApiConstants.LIMIT_KEYS, new String[] {SalesContractBean.SC_CODE, SalesContractBean.SC_TYPE});
 		Map<String, Object> result = dao.list(query, DBBean.SALES_CONTRACT);
 		List<Map<String, Object>> resultList = (List<Map<String, Object>>) result.get(ApiConstants.RESULTS_DATA);
 		for (Map<String, Object> sc : resultList){
 			sc.put(ProjectBean.PROJECT_CUSTOMER, cName);
 		}
 		return result;
 	}
 
 }
