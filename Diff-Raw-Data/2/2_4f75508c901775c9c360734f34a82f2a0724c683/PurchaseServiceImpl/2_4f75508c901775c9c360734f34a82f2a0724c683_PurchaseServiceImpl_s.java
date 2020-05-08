 package com.pms.service.service.impl;
 
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.LinkedHashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 
 import org.apache.commons.chain.web.MapEntry;
 import org.apache.logging.log4j.LogManager;
 import org.apache.logging.log4j.Logger;
 
 import com.pms.service.dbhelper.DBQuery;
 import com.pms.service.dbhelper.DBQueryOpertion;
 import com.pms.service.exception.ApiResponseException;
 import com.pms.service.mockbean.ApiConstants;
 import com.pms.service.mockbean.CustomerBean;
 import com.pms.service.mockbean.DBBean;
 import com.pms.service.mockbean.EqCostListBean;
 import com.pms.service.mockbean.ProjectBean;
 import com.pms.service.mockbean.PurchaseBack;
 import com.pms.service.mockbean.PurchaseCommonBean;
 import com.pms.service.mockbean.PurchaseRequest;
 import com.pms.service.mockbean.SalesContractBean;
 import com.pms.service.mockbean.UserBean;
 import com.pms.service.service.AbstractService;
 import com.pms.service.service.IArrivalNoticeService;
 import com.pms.service.service.IPurchaseService;
 import com.pms.service.util.ApiUtil;
 import com.pms.service.util.DateUtil;
 import com.pms.service.util.status.ResponseCodeConstants;
 
 public class PurchaseServiceImpl extends AbstractService implements IPurchaseService {
 
     private static final Logger logger = LogManager.getLogger(PurchaseServiceImpl.class);
 	
     private IArrivalNoticeService arrivalNoticeService;
     
     public IArrivalNoticeService getArrivalNoticeService() {
 		return arrivalNoticeService;
 	}
 
 	public void setArrivalNoticeService(IArrivalNoticeService arrivalNoticeService) {
 		this.arrivalNoticeService = arrivalNoticeService;
 	}
 
 	/**
      * @param scId
      */
 	public Map<String, Object> prepareBack(Map<String, Object> params) {
 		Map<String,Object> request = new LinkedHashMap<String,Object>();
 		request.put(PurchaseBack.pbStatus, PurchaseStatus.saved.toString());
 		request.put(PurchaseBack.scId, params.get(PurchaseBack.scId));
 		mergeSalesContract(request);
 		mergeEqcost(request);
 		
 		List<Map<String, Object>> eqList = (List<Map<String, Object>>) request.get(PurchaseBack.eqcostList);
 		if(eqList != null){
 			for(Map<String,Object> obj : eqList){		
 			    obj.put(PurchaseBack.pbTotalCount, obj.get(PurchaseBack.eqcostLeftAmount));
 			    obj.put(PurchaseBack.pbComment, obj.get(EqCostListBean.EQ_LIST_MEMO));
 			}
 		}
 		removeEmptyEqList(eqList, PurchaseBack.eqcostLeftAmount);
 		request.put(PurchaseBack.pbDepartment, request.get("projectManagerDepartment"));
 		request.remove("projectManagerDepartment");
 		request.put(PurchaseBack.pbSpecialRequireRadio, new String[]{});
 		return request;
 	}
 
     /**
      * @param params._id
      */
 	public Map<String, Object> loadBack(Map<String, Object> params) {
 		Map<String,Object> request = dao.findOne(ApiConstants.MONGO_ID, params.get(ApiConstants.MONGO_ID), DBBean.PURCHASE_BACK);
 		mergeSalesContract(request);
 		mergeEqcost(request);
 		return request;
 	}
 	
     /**
      * @param back object info
      */
 	public Map<String, Object> saveBack(Map<String, Object> params) {
 		Map<String,Object> newObj = new HashMap<String,Object>();
 	    newObj.put(PurchaseBack.pbStatus, PurchaseStatus.saved.toString());
 		return saveOrUpdate(params, newObj);
 	}
 
 	@Override
     public Map<String, Object> submitBack(Map<String, Object> params) {
         Map<String, Object> newObj = new HashMap<String, Object>();
         newObj.put(PurchaseBack.pbStatus, PurchaseStatus.submited.toString());
         newObj.put(PurchaseBack.pbSubmitDate, DateUtil.getDateString(new Date()));
         return saveOrUpdate(params, newObj);
     }
 
 	@Override
     public Map<String, Object> approveBack(Map<String, Object> params) {
         Map<String, Object> newObj = new HashMap<String, Object>();
         newObj.put(ApiConstants.MONGO_ID, params.get(ApiConstants.MONGO_ID));
         newObj.put(PurchaseBack.pbStatus, PurchaseStatus.approved.toString());
         newObj.put(PurchaseBack.pbOperateDate, DateUtil.getDateString(new Date()));
 	    String oldComment = (String)dao.querySingleKeyById(PurchaseBack.pbComment, params.get(ApiConstants.MONGO_ID), DBBean.PURCHASE_BACK);
 	    String comment = (String)params.get("tempComment");
 	    comment = recordComment("批准",comment,oldComment);
 	    newObj.put(PurchaseBack.pbComment, comment);
         return dao.updateById(newObj, DBBean.PURCHASE_BACK);
     }
 	
 	@Override
     public Map<String, Object> rejectBack(Map<String, Object> params) {
         Map<String, Object> newObj = new HashMap<String, Object>();
         newObj.put(ApiConstants.MONGO_ID, params.get(ApiConstants.MONGO_ID));
         newObj.put(PurchaseBack.pbStatus, PurchaseStatus.rejected.toString());
         newObj.put(PurchaseBack.pbOperateDate, DateUtil.getDateString(new Date()));
 	    String oldComment = (String)dao.querySingleKeyById(PurchaseBack.pbComment, params.get(ApiConstants.MONGO_ID), DBBean.PURCHASE_BACK);
 	    String comment = (String)params.get("tempComment");
 	    comment = recordComment("拒绝",comment,oldComment);
 	    newObj.put(PurchaseBack.pbComment, comment);
         return dao.updateById(newObj, DBBean.PURCHASE_BACK);
     }
 	
     private Map<String, Object> saveOrUpdate(Map<String, Object> params, Map<String, Object> newObj) {
         newObj.put(ApiConstants.MONGO_ID, params.get(ApiConstants.MONGO_ID));
         newObj.put(PurchaseBack.pbDepartment, params.get(PurchaseBack.pbDepartment));
         newObj.put(PurchaseBack.pbType, params.get(PurchaseBack.pbType));
         newObj.put(PurchaseBack.pbSpecialRequire, params.get(PurchaseBack.pbSpecialRequire));
         newObj.put(PurchaseBack.pbSpecialRequireRadio, params.get(PurchaseBack.pbSpecialRequireRadio));
         newObj.put(PurchaseBack.pbPlanDate, DateUtil.converUIDate(params.get(PurchaseBack.pbPlanDate)));
         newObj.put(PurchaseBack.scId, params.get(PurchaseBack.scId));
         if (params.get(PurchaseBack.pbCode) == null) {
             newObj.put(PurchaseBack.pbCode, generateCode("BHSQ", DBBean.PURCHASE_BACK, PurchaseBack.pbCode));
         }
         
         String status = String.valueOf(newObj.get(PurchaseBack.pbStatus));
 	    String oldComment = (String)dao.querySingleKeyById(PurchaseBack.pbComment, params.get(ApiConstants.MONGO_ID), DBBean.PURCHASE_BACK);
 	    String comment = (String)params.get("tempComment");
 	    if(PurchaseStatus.submited.toString().equals(status)){
 	    	comment = recordComment("提交",comment,oldComment);
 	    	newObj.putAll(checkEqCountForBack(params,true));
 	    } else {
 	    	comment = recordComment("保存",comment,oldComment);
 	    	newObj.putAll(checkEqCountForBack(params,false));
 	    }
 	    newObj.put(PurchaseBack.pbComment, comment);
 	    
         Map<String, Object> sc = dao.findOne(ApiConstants.MONGO_ID, params.get(PurchaseBack.scId), new String[] { SalesContractBean.SC_CODE,SalesContractBean.SC_PROJECT_ID }, DBBean.SALES_CONTRACT);
         newObj.put(PurchaseBack.scCode, sc.get(SalesContractBean.SC_CODE));
 
         scs.mergeCommonFieldsFromSc(newObj, params.get(PurchaseBack.scId));
         
         Map<String, Object> res = dao.save(newObj, DBBean.PURCHASE_BACK);
         return res;
     }
 
 	@Override
 	public Map<String, Object> pendingBack(Map<String, Object> params) {
 		Map<String,Object> obj = new HashMap<String,Object>();
 		obj.put(ApiConstants.MONGO_ID, params.get(ApiConstants.MONGO_ID));
 		obj.put(PurchaseBack.pbStatus, PurchaseStatus.interruption.toString());
 		
 	    String oldComment = (String)dao.querySingleKeyById(PurchaseBack.pbComment, params.get(ApiConstants.MONGO_ID), DBBean.PURCHASE_BACK);
 	    String comment = (String)params.get("tempComment");
 	    comment = recordComment("中止",comment,oldComment);
 	    obj.put(PurchaseBack.pbComment, comment);
 		
 		
 		//TODO: 中止了备货，调拨和采购怎么办？
 		Map<String,Object> res = dao.updateById(obj, DBBean.PURCHASE_BACK);
 	      
 /*        Map<String, Object> resqeury = this.dao.findOne(ApiConstants.MONGO_ID, params.get(ApiConstants.MONGO_ID), new String[]{EqCostListBean.EQ_LIST_SC_ID}, DBBean.PURCHASE_BACK);     
         updateEqLeftCountInEqDB(resqeury); */
         
         return res;
 	}
 
 	///////////////////////////allot 调拨///////////
 	@Override
 	public Map<String, Object> submitAllot(Map<String, Object> params) {
 		Map<String,Object> obj = new HashMap<String,Object>();
 		obj.put(PurchaseBack.scId, params.get(PurchaseBack.scId));
 		obj.put(PurchaseBack.pbId, params.get(PurchaseBack.pbId));
 		obj.put(PurchaseBack.pbCode, params.get(PurchaseBack.pbCode));
 		obj.put(PurchaseBack.paStatus, PurchaseStatus.submited.toString());
 		obj.put(PurchaseBack.paSubmitDate, DateUtil.getDateString(new Date()));
 		obj.put(PurchaseBack.paShelfCode, params.get(PurchaseBack.paShelfCode));
 		obj.put(PurchaseBack.paCode, generateCode("DBSQ", DBBean.PURCHASE_ALLOCATE, PurchaseBack.paCode));
 		
 	    String comment = (String)params.get("tempComment");
 	    comment = recordComment("提交",comment,null);
 	    obj.put(PurchaseBack.paComment, comment);
 		
 		obj.putAll(checkEqCountForAllot(params));
 		
 		scs.mergeCommonFieldsFromSc(obj, params.get(PurchaseBack.scId));
 		Map<String, Object> result =  dao.add(obj, DBBean.PURCHASE_ALLOCATE);
 		//updateEqLeftCountInEqDB(result);
 		return result;
 	}
 
 	public Map<String, Object> loadAllot(Map<String, Object> params) {
 		Map<String,Object> allot = dao.findOne(ApiConstants.MONGO_ID, params.get(ApiConstants.MONGO_ID), DBBean.PURCHASE_ALLOCATE);
 		mergeEqcostForAllot(allot);
 		mergeSalesContract(allot);
 		return allot;
 	}
 
 	public Map<String, Object> prepareAllot(Map<String, Object> params) {
 		Map<String,Object> allot = new HashMap<String,Object>();
 		allot.put(PurchaseBack.pbId, params.get(PurchaseBack.pbId));
 		allot.put(PurchaseBack.paStatus, PurchaseStatus.saved.toString());
 		mergeEqcostForAllot(allot);//整合scId eqCostList purchaseBack
 		mergeSalesContract(allot);
 		
 		List<Map<String, Object>> eqList = (List<Map<String, Object>>) allot.get(PurchaseBack.eqcostList);
 		if(eqList != null){
 			removeEmptyEqList(eqList, PurchaseBack.pbLeftCount);
 			for(Map<String,Object> obj : eqList){		
 			    obj.put(PurchaseBack.paCount, obj.get(PurchaseBack.pbLeftCount));
 			}
 		}		
 		
 		return allot;
 	}
 	
 	public Map<String, Object> approveAllot(Map<String, Object> params) {
 		Map<String,Object> allot = dao.findOne(ApiConstants.MONGO_ID, params.get(ApiConstants.MONGO_ID), DBBean.PURCHASE_ALLOCATE);
 		String dbStatus = String.valueOf(allot.get(PurchaseBack.paStatus));
 
 	    String oldComment = (String)dao.querySingleKeyById(PurchaseBack.paComment, params.get(ApiConstants.MONGO_ID), DBBean.PURCHASE_ALLOCATE);
 	    String comment = (String)params.get("tempComment");
 	    
 		String status = null;
 		if (PurchaseStatus.finalApprove.toString().equals(dbStatus)) {
             status = PurchaseStatus.done.toString();
             comment = recordComment("结束", comment, oldComment);
             allot.put(PurchaseBack.paNumber, params.get(PurchaseBack.paNumber));
         }else if (PurchaseStatus.approved.toString().equals(dbStatus)) {
             status = PurchaseStatus.finalApprove.toString();
             comment = recordComment("终审", comment, oldComment);
         } else {
             status = PurchaseStatus.approved.toString();
             comment = recordComment("初审", comment, oldComment);
             allot.put(PurchaseBack.eqcostList, params.get(PurchaseBack.eqcostList));
         }
 
 	    allot.put(PurchaseBack.paComment, comment);
 		allot.put(PurchaseBack.paStatus, status);
 		allot.put(PurchaseBack.paApproveDate, DateUtil.getDateString(new Date()));
 		allot = dao.updateById(allot, DBBean.PURCHASE_ALLOCATE);
 		
 		if(PurchaseStatus.done.toString().equals(status)){
 			// 批准调拨申请时生成到货通知
 			createArrivalNotice(allot);
 		}
 	    //Map<String, Object> resqeury = this.dao.findOne(ApiConstants.MONGO_ID, params.get(ApiConstants.MONGO_ID), new String[]{EqCostListBean.EQ_LIST_SC_ID}, DBBean.PURCHASE_ALLOCATE);     
 	    //updateEqLeftCountInEqDB(resqeury); 
 	    return allot;
 	}
 	
 	private void createArrivalNotice(Map<String, Object> params) {
 		arrivalNoticeService.createByAllocate(params);
 	}
 	
 	@Override
 	public Map<String, Object> rejectAllot(Map<String, Object> params) {
 		Map<String,Object> allot = dao.findOne(ApiConstants.MONGO_ID, params.get(ApiConstants.MONGO_ID), DBBean.PURCHASE_ALLOCATE);
 		Map<String,Object> obj = new HashMap<String,Object>();
 		obj.put(ApiConstants.MONGO_ID, allot.get(ApiConstants.MONGO_ID));
 		obj.put(PurchaseBack.paStatus, PurchaseStatus.rejected.toString());
 
 	    String oldComment = (String)allot.get(PurchaseBack.paComment);
 	    String comment = (String)params.get("tempComment");
 	    comment = recordComment("拒绝",comment,oldComment);
 	    obj.put(PurchaseBack.paComment, comment);
 	    
         Map<String, Object> res = dao.updateById(obj, DBBean.PURCHASE_ALLOCATE);
 /*        Map<String, Object> resqeury = this.dao.findOne(ApiConstants.MONGO_ID, params.get(ApiConstants.MONGO_ID), new String[] { EqCostListBean.EQ_LIST_SC_ID },
                 DBBean.PURCHASE_ALLOCATE);
         updateEqLeftCountInEqDB(resqeury);*/
         return res;
 	}
 
 	public Map<String, Object> listBack(Map<String, Object> params) {
 		String[] keys = new String[]{
 				ApiConstants.CREATOR,PurchaseBack.pbCode,PurchaseBack.pbType,
 				PurchaseBack.pbStatus,PurchaseBack.pbMoney,PurchaseBack.scId,
 				PurchaseBack.pbSubmitDate, PurchaseBack.prId,PurchaseBack.poId
 		};
 		params.put(ApiConstants.LIMIT_KEYS, keys);
 		mergeDataRoleQueryWithProjectAndScType(params);
 		mergeMyTaskQuery(params, DBBean.PURCHASE_BACK);
 		
 	    mergeRefSearchQuery(params, ProjectBean.PROJECT_CUSTOMER, ProjectBean.PROJECT_CUSTOMER, CustomerBean.NAME,  DBBean.CUSTOMER);
 	    mergeRefSearchQuery(params, ProjectBean.PROJECT_MANAGER, ProjectBean.PROJECT_MANAGER, UserBean.USER_NAME,  DBBean.USER);
 	    mergeRefSearchQuery(params, SalesContractBean.SC_PROJECT_ID, ProjectBean.PROJECT_NAME, ProjectBean.PROJECT_NAME, DBBean.PROJECT);
 	    mergeRefSearchQuery(params, SalesContractBean.SC_PROJECT_ID, ProjectBean.PROJECT_CODE, ProjectBean.PROJECT_CODE, DBBean.PROJECT);
 	    mergeRefSearchQuery(params, PurchaseBack.scId, SalesContractBean.SC_CODE, SalesContractBean.SC_CODE, DBBean.SALES_CONTRACT);
 
 		
 		Map<String,Object> queryKeys = new HashMap<String,Object>();
 		queryKeys.put(ApiConstants.LIMIT_KEYS, keys);
 		queryKeys.put(ApiConstants.LIMIT, params.get(ApiConstants.LIMIT));
 		queryKeys.put(ApiConstants.LIMIT_START, params.get(ApiConstants.LIMIT_START));
 		
 		Map<String,Object> map = dao.list(params, DBBean.PURCHASE_BACK);
 		mergeSalesContract(map);
 		return map;
 	}
 	
 	@Override
 	public Map<String, Object> listAllBack(Map<String, Object> params) {
 		String[] keys = new String[]{
 				ApiConstants.CREATOR,PurchaseBack.pbCode,PurchaseBack.pbType,
 				PurchaseBack.pbStatus,PurchaseBack.pbMoney,PurchaseBack.scId,
 				PurchaseBack.pbSubmitDate, PurchaseBack.prId,PurchaseBack.poId
 		};
 		params.put(ApiConstants.LIMIT_KEYS, keys);
 		mergeDataRoleQueryWithProjectAndScType(params);
 		mergeMyTaskQuery(params, DBBean.PURCHASE_BACK);
 		Map<String,Object> map = dao.list(params, DBBean.PURCHASE_BACK);
 		mergeSalesContract(map);
 		return map;
 	}
 	
 	@Override
 	public Map<String, Object> listCheckedBack(Map<String, Object> params) {
 		String[] keys = new String[]{PurchaseBack.pbCode,PurchaseBack.pbType,PurchaseBack.pbStatus,
 				PurchaseBack.pbMoney,PurchaseBack.scId,PurchaseBack.pbSubmitDate, PurchaseBack.prId};
 		params.put(ApiConstants.LIMIT_KEYS, keys);
 		params.put(PurchaseBack.pbStatus, PurchaseStatus.approved.toString());
 		mergeDataRoleQueryWithProjectAndScType(params);
 		mergeMyTaskQuery(params, DBBean.PURCHASE_BACK);
 		
 	    mergeRefSearchQuery(params, ProjectBean.PROJECT_CUSTOMER, ProjectBean.PROJECT_CUSTOMER, CustomerBean.NAME,  DBBean.CUSTOMER);
 	    mergeRefSearchQuery(params, ProjectBean.PROJECT_MANAGER, ProjectBean.PROJECT_MANAGER, UserBean.USER_NAME,  DBBean.USER);
 	    mergeRefSearchQuery(params, SalesContractBean.SC_PROJECT_ID, ProjectBean.PROJECT_NAME, ProjectBean.PROJECT_NAME, DBBean.PROJECT);
 	    mergeRefSearchQuery(params, SalesContractBean.SC_PROJECT_ID, ProjectBean.PROJECT_CODE, ProjectBean.PROJECT_CODE, DBBean.PROJECT);
 	    mergeRefSearchQuery(params, PurchaseBack.scId, SalesContractBean.SC_CODE, SalesContractBean.SC_CODE, DBBean.SALES_CONTRACT);
 	        
 		Map<String,Object> map = dao.list(params, DBBean.PURCHASE_BACK);
 		//排除不能再拆分的备货申请
 		List<Map<String,Object>> list = (List<Map<String,Object>>)map.get(ApiConstants.RESULTS_DATA);
 		List<Map<String,Object>> list2 = new ArrayList<Map<String,Object>>();
 		for(int i=0;i<list.size();i++){
 			Map<String,Object> item = list.get(i);
 			if(item != null){
 				boolean isZero = true;
 		        Map<String, Integer> restCountMap = countRestEqByBackId(item.get(ApiConstants.MONGO_ID).toString());
 		        for (Entry<String, Integer> entry : restCountMap.entrySet()) {
 		            if(entry.getValue() != 0){
 		            	isZero = false;
 		            	break;
 		            }
 		        }
 				if(!isZero){
 					list2.add(item);
 				}
 			}
 		}
 		map.put(ApiConstants.RESULTS_DATA, list2);
 		mergeSalesContract(map);
 		return map;
 	}
 
 	@Override
     public Map<String, Object> listAllot(Map<String, Object> params) {
         mergeDataRoleQueryWithProjectAndScType(params);
         mergeMyTaskQuery(params, DBBean.PURCHASE_ALLOCATE);
         mergeRefSearchQuery(params, ProjectBean.PROJECT_CUSTOMER, ProjectBean.PROJECT_CUSTOMER, CustomerBean.NAME, DBBean.CUSTOMER);
         mergeRefSearchQuery(params, ProjectBean.PROJECT_MANAGER, ProjectBean.PROJECT_MANAGER, UserBean.USER_NAME, DBBean.USER);
         mergeRefSearchQuery(params, SalesContractBean.SC_PROJECT_ID, ProjectBean.PROJECT_NAME, ProjectBean.PROJECT_NAME, DBBean.PROJECT);
         mergeRefSearchQuery(params, SalesContractBean.SC_PROJECT_ID, ProjectBean.PROJECT_CODE, ProjectBean.PROJECT_CODE, DBBean.PROJECT);
         mergeRefSearchQuery(params, PurchaseBack.scId, SalesContractBean.SC_CODE, SalesContractBean.SC_CODE, DBBean.SALES_CONTRACT);
         Map<String, Object> map = dao.list(params, DBBean.PURCHASE_ALLOCATE);
         mergeSalesContract(map);
         return map;
     }
 
 
 	@Override
 	public void destoryBack(Map<String, Object> params) {
 		Map<String,Object> back = dao.findOne(ApiConstants.MONGO_ID, params.get(ApiConstants.MONGO_ID), DBBean.PURCHASE_BACK);
 		String dbStatus = (String)back.get(PurchaseBack.pbStatus);
 		if(PurchaseStatus.saved.toString().equals(dbStatus) || PurchaseStatus.rejected.toString().equals(dbStatus)){
 			List<String> ids = new ArrayList<String>();
 			ids.add(String.valueOf(params.get(ApiConstants.MONGO_ID)));
 			dao.deleteByIds(ids, DBBean.PURCHASE_BACK);
 		}
 	}
 
 	/**整合项目合同信息
 	 * @param 支持 单个或多个对象(对象中需包含字段scId)
 	 * */
 	private Map<String,Object> mergeSalesContract(Map<String,Object> params){
 		List<Map<String,Object>> list = new ArrayList<Map<String,Object>>();
 		if(params.containsKey(ApiConstants.RESULTS_DATA)){
 			list = (List<Map<String,Object>>)params.get(ApiConstants.RESULTS_DATA);
 		}else {
 			list.add(params);
 		}
 		List<String> ids = new ArrayList<String>();
 		for(Map<String,Object> re : list){
 			String id = (String)re.get(PurchaseBack.scId);
 			if(!ids.contains(id) && id != null) {
 				ids.add(id);
 			}
 		}
 		Map<String,Object> baseInfoMap = scs.getBaseInfoByIds(ids);
 		for(Map<String,Object> re : list){
 			re.putAll((Map)baseInfoMap.get(re.get(PurchaseBack.scId)));
 		}
 		return params;
 	}
 	
 	/**
 	 * 整合清单基本信息 以及 合同下面的成本清单的剩余数量
 	 * 注：对已提交的备货申请，只加载提交的货物清单信息
 	 * **/
 	@SuppressWarnings("unchecked")
 	private void mergeEqcost(Map<String,Object> params){
 		String scId = (String)params.get(PurchaseBack.scId);
 
 		if(params.get(PurchaseBack.eqcostList) == null){
 		    params.put(PurchaseBack.eqcostList, scs.listMergedEqListBySC(params).get(ApiConstants.RESULTS_DATA));
 		}
 		List<Map<String,Object>> eqList = scs.mergeEqListBasicInfo(params.get(PurchaseBack.eqcostList));
 
         Map<String,Integer> scCountMap = countBackRestEqByScId(scId);
 
 		if(eqList != null){
 			for(Map<String,Object> obj : eqList){		
 			    obj.put(PurchaseBack.eqcostLeftAmount, scCountMap.get(obj.get(ApiConstants.MONGO_ID)));
 			}
 		}
 		params.put(PurchaseBack.eqcostList, eqList);
 
 	}
 
 	/**整合调拨清单：加载所有的备货清单列表*/
 	private void mergeEqcostForAllot(Map<String,Object> params){
 		//1. 备货清单
 		Map<String, Object> back = dao.findOne(ApiConstants.MONGO_ID, params.get(PurchaseBack.pbId), DBBean.PURCHASE_BACK);
 
 		params.put(PurchaseBack.scId, back.get(PurchaseBack.scId));
 		params.put(PurchaseBack.scCode, back.get(PurchaseBack.scCode));
 		params.put(PurchaseBack.pbCode, back.get(PurchaseBack.pbCode));
 		params.put(PurchaseBack.pbDepartment, back.get(PurchaseBack.pbDepartment));
 		params.put(PurchaseBack.pbPlanDate, back.get(PurchaseBack.pbPlanDate));
 		params.put(PurchaseBack.pbType, back.get(PurchaseBack.pbType));
 		params.put(PurchaseBack.pbSpecialRequire, back.get(PurchaseBack.pbSpecialRequire));
 		params.put(PurchaseBack.pbSpecialRequireRadio, back.get(PurchaseBack.pbSpecialRequireRadio));		
 		
 		mergeBackRestEqCount(back);//整合备货剩余数量
 		
 
         List<Map<String,Object>> eqList = scs.mergeEqListBasicInfo(back.get(PurchaseBack.eqcostList));
         
         List<Map<String, Object>> allotEqList = new ArrayList<Map<String, Object>>();
         if(params.get(SalesContractBean.SC_EQ_LIST)!=null){
             allotEqList = (List<Map<String, Object>>) params.get(SalesContractBean.SC_EQ_LIST);
         }else{
             allotEqList = (List<Map<String, Object>>) back.get(SalesContractBean.SC_EQ_LIST);
         }
 
         for (Map<String, Object> allotEq : allotEqList) {
             for (Map<String, Object> backEq : eqList) {
                 if (allotEq.get(ApiConstants.MONGO_ID).toString().equals(backEq.get(ApiConstants.MONGO_ID).toString())) {
                 	allotEq.putAll(backEq);
                     break;
                 }
             }
         }        
 		params.put(PurchaseBack.eqcostList, allotEqList);
 	}	
 
 	/**
 	 * 备货申请：验证货物数量，计算申请总额
 	 * @param params.scId params.eqcostList
 	 * */
 	public Map<String,Object> checkEqCountForBack(Map<String,Object> params, boolean checkZero) {
 		
 		//1. 获取上传的备货清单
 		Map<String,Map<String,Object>> backEqMap = new LinkedHashMap<String,Map<String,Object>>();
 		List<Map<String,Object>> backEqList = (List<Map<String,Object>>)params.get(PurchaseBack.eqcostList);
 		if(backEqList != null){
 			for(Map<String,Object> obj : backEqList){
 				backEqMap.put(String.valueOf(obj.get(ApiConstants.MONGO_ID)), obj);
 			}
 		}
 		
 		//2. 获取合同剩余数量
 		String scId = (String)params.get(PurchaseBack.scId);
 		Map<String,Integer> scCountMap = countBackRestEqByScId(scId);
 		
 		//3. 获取合同下面的设备价格
 		Map<String,Object> query = new HashMap<String,Object>();
 		query.put(ApiConstants.MONGO_ID, new DBQuery(DBQueryOpertion.IN, new ArrayList<String>(backEqMap.keySet())));
 		Map<String, Object> eqMap = dao.list(query, DBBean.EQ_COST);
 		List<Map<String,Object>> eqList = (List<Map<String,Object>>)eqMap.get(ApiConstants.RESULTS_DATA);
 		Map<String, Double> eqPriceMap = new HashMap<String, Double>();
 		for(Map<String,Object> eq : eqList){
             if (eq.get(ApiConstants.MONGO_ID) != null) {
             	eqPriceMap.put(eq.get(ApiConstants.MONGO_ID).toString(), ApiUtil.getDouble(eq,EqCostListBean.EQ_LIST_BASE_PRICE, 0));
             }
 		}
 		
 		Double money = 0.0;
 		List<Map<String,Object>> itemList = new ArrayList<Map<String,Object>>();
 		//3.1 检验货物有效性 ID 数量
 		for(String id : backEqMap.keySet()){
 			if(scCountMap.containsKey(id)){
 				Double price = eqPriceMap.containsKey(id) ? eqPriceMap.get(id) : 0;
 				Integer pbCount = ApiUtil.getInteger(backEqMap.get(id), PurchaseBack.pbTotalCount, 0);
 				String comment =  (String)backEqMap.get(id).get(PurchaseBack.pbComment);
 				if(checkZero && pbCount == 0){
 					continue;
 				}
 				if(pbCount > scCountMap.get(id)){
 					throw new ApiResponseException(String.format("Save purchase back error [%s]", backEqMap.get(id)), ResponseCodeConstants.EQCOST_APPLY_ERROR); 
 				}
 				money += price*pbCount;
 				Map<String,Object> item = new HashMap<String,Object>();
 				item.put(ApiConstants.MONGO_ID, id);
 				item.put(PurchaseBack.pbTotalCount, pbCount);
 				item.put(PurchaseBack.pbComment, comment);
 				itemList.add(item);
 			}
 		}
 		Map<String,Object> result = new HashMap<String,Object>();
 		result.put(PurchaseBack.eqcostList, itemList);
 		result.put(PurchaseBack.pbMoney, money);
 		return result;
 	}
 	
 	/**
 	 * 调拨申请申请：验证货物数量，计算申请总额
 	 * @param params.pbId params.eqcostList
 	 * */
 	public Map<String,Object> checkEqCountForAllot(Map<String,Object> params) {
 		
 		//1. 获取上传的调拨设备清单
 		List<Map<String,Object>> allotEqList = (List<Map<String,Object>>)params.get(PurchaseBack.eqcostList);
 		Map<String,Map<String,Object>> allotEqMap = new LinkedHashMap<String,Map<String,Object>>();
 		if(allotEqList != null){
 			for(Map<String,Object> obj : allotEqList){
 				allotEqMap.put(String.valueOf(obj.get(ApiConstants.MONGO_ID)), obj);
 			}
 		}
 		
 		//2. 获取备货剩余数量
 		Map<String,Integer> backLeftCountMap = countRestEqByBackId((String)params.get(PurchaseBack.pbId));
 		
 		//3.1 检验货物有效性 ID 数量
 		List<Map<String,Object>> itemList = new ArrayList<Map<String,Object>>();
 		for(String id : allotEqMap.keySet()){
 			if(backLeftCountMap.containsKey(id)){
 				Integer count = ApiUtil.getInteger(allotEqMap.get(id), PurchaseBack.paCount, 0);
 				String comment =  (String)allotEqMap.get(id).get(PurchaseBack.paComment);
 				
 				if(count == 0){
 					continue;
 				}
 				if(count > backLeftCountMap.get(id)){
 					throw new ApiResponseException(String.format("Save purchase allot error [%s]", allotEqMap.get(id)), ResponseCodeConstants.EQCOST_APPLY_ERROR); 
 				}
 				Map<String,Object> item = new HashMap<String,Object>();
 				item.put(ApiConstants.MONGO_ID, id);
 				item.put(PurchaseBack.paCount, count);
 				item.put(PurchaseBack.paComment, comment);
 				itemList.add(item);
 			}
 		}
 		Map<String,Object> result = new HashMap<String,Object>();
 		result.put(PurchaseBack.eqcostList, itemList);
 		return result;
 	}
 	
 	
 
 	
 	/**{_id:allotCount}*/
 	public Map<String,Double> getAllotEqCountBySalesContractId(String saleId){
 		Map<String,Double> result = new HashMap<String,Double>(); 
 		Map<String, Object> query = new HashMap<String, Object>();
 		query.put(PurchaseBack.scId, saleId);
 		query.put(PurchaseBack.paStatus, PurchaseStatus.approved.toString());
 		query.put(ApiConstants.LIMIT_KEYS, new String[]{PurchaseBack.eqcostList});
 		Map<String,Object> map = dao.list(query, DBBean.PURCHASE_ALLOCATE);
 		List<Map<String,Object>> list = (List<Map<String,Object>>)map.get(ApiConstants.RESULTS_DATA);
 		for(Map<String,Object> obj : list){
 			List<Map<String,Object>> eqList = (List<Map<String,Object>>)obj.get(PurchaseBack.eqcostList);
 			for(Map<String,Object> eq : eqList){
 				String key = (String)eq.get(ApiConstants.MONGO_ID);
 				Double value = ApiUtil.getDouble(eq, PurchaseBack.paCount,0);
 				if(result.containsKey(key)){
 					result.put(key, result.get(key)+value);
 				}else{
 					result.put(key, value);
 				}
 			}
 		}
 		return result;
 	}
 	
 
 	public enum PurchaseStatus {
     	saved,submited,approved,rejected,interruption,finalApprove,done;
 		@Override
 		public String toString() {
 			String value = "undefine";
 			switch(this){
 				case saved: value="草稿"; break;
 				case submited: value="已提交"; break;
 				case approved: value="已批准"; break;
 				case rejected: value="已拒绝"; break;
 				case finalApprove: value="已终审"; break;
 				case done: value="已结束"; break;
 				case interruption: value="已中止"; break;
 				default: break;
 			}
 			return value;
 		}
     }
 	
 
 	@Override
 	public String geValidatorFileName() {
 		return "purchase";
 	}
 	
 	/**整合备货的剩余数量： 备货剩余数量 = 备货数量 - 已调拨申请 - 已采购申请*/
     public Map<String, Object> mergeBackRestEqCount(Map<String, Object> back) {
         if (back.get(SalesContractBean.SC_EQ_LIST) == null) {
             back = dao.findOne(ApiConstants.MONGO_ID, back.get(ApiConstants.MONGO_ID), DBBean.PURCHASE_BACK);
         }
         List<Map<String, Object>> eqBackMapList = (List<Map<String, Object>>) back.get(SalesContractBean.SC_EQ_LIST);
         Map<String, Integer> restCountMap = countRestEqByBackId(back.get(ApiConstants.MONGO_ID).toString());
         for (Map<String, Object> eqMap : eqBackMapList) {
             eqMap.put(PurchaseBack.pbLeftCount, restCountMap.get(eqMap.get(ApiConstants.MONGO_ID)));
         }
         
         return back;
     }
 
     
     /**查询某合同下面剩余的成本设备数量: 成本数量 - 备货总和**/
     public Map<String, Integer> countBackRestEqByScId(String scId) {
         // 获取成本设备清单
     	Map<String,Object> eqQuery = new HashMap<String,Object>();
     	eqQuery.put(EqCostListBean.EQ_LIST_SC_ID, scId);
 		Map<String, Object> eqMap = scs.listMergedEqListBySC(eqQuery);
 		List<Map<String,Object>> eqList = (List<Map<String,Object>>)eqMap.get(ApiConstants.RESULTS_DATA);
 		
 		Map<String, Integer> scEqTotalCountMap = new HashMap<String, Integer>();
 		for(Map<String,Object> eq : eqList){
             if (eq.get(ApiConstants.MONGO_ID) != null) {
             	scEqTotalCountMap.put(eq.get(ApiConstants.MONGO_ID).toString(), ApiUtil.getInteger(eq,EqCostListBean.EQ_LIST_REAL_AMOUNT, 0));
             }
 		}
 		
         //获取总备货清单
         Map<String, Object> backQuery = new HashMap<String, Object>();
         backQuery.put(PurchaseBack.scId, scId);
         List<String> sl = new ArrayList<String>();
         sl.add(PurchaseStatus.submited.toString());
         sl.add(PurchaseStatus.approved.toString());
         backQuery.put(PurchaseBack.pbStatus, new DBQuery(DBQueryOpertion.IN, sl)); 
         Map<String, Integer> backEqCountMap = countEqByKey(backQuery, DBBean.PURCHASE_BACK, PurchaseBack.pbTotalCount, null);
         // 计算剩余数量
         Map<String, Integer> restEqCount = new HashMap<String, Integer>();
         for (String id : scEqTotalCountMap.keySet()) {
             int eqTotalCount = 0;
             int pbTotalCount = 0;
             if (scEqTotalCountMap.get(id) != null) {
             	eqTotalCount = scEqTotalCountMap.get(id);
             }
             if (backEqCountMap.get(id) != null) {
             	pbTotalCount = backEqCountMap.get(id);
             }
             restEqCount.put(id, eqTotalCount - pbTotalCount);
         }
         return restEqCount;
     }
     
     /**根据备货申请id查询此备货下面剩余的数量：备货数量 -已采购申请数量 - 已调拨数量*/
     public Map<String, Integer> countRestEqByBackId(String backId) {
         
         Map<String, Object> backQuery = new HashMap<String, Object>();
         backQuery.put(ApiConstants.MONGO_ID, backId);
         //备货申请下总数集合
         Map<String, Integer> backEqCountMap = countEqByKey(backQuery, DBBean.PURCHASE_BACK, PurchaseBack.pbTotalCount, null);
 
         // 获取已发的采购申请的数据总和
         Map<String, Object> purchaseRequestQuery = new HashMap<String, Object>();
         purchaseRequestQuery.put(PurchaseRequest.BACK_REQUEST_ID, backId);
         purchaseRequestQuery.put(PurchaseRequest.PROCESS_STATUS, new DBQuery(DBQueryOpertion.NOT_IN, new String[] { PurchaseRequest.STATUS_ABROGATED, PurchaseRequest.STATUS_DRAFT, PurchaseRequest.STATUS_CANCELLED }));
         Map<String, Integer> requestEqCountMap = countEqByKey(purchaseRequestQuery, DBBean.PURCHASE_REQUEST, PurchaseCommonBean.EQCOST_APPLY_AMOUNT, null);
 
         // 获取调拨中的数据总和
         Map<String, Object> allocateQuery = new HashMap<String, Object>();
         allocateQuery.put(PurchaseBack.pbId, backId);
         List<String> sl = new ArrayList<String>();
         sl.add(PurchaseStatus.submited.toString());
         sl.add(PurchaseStatus.approved.toString());
         sl.add(PurchaseStatus.finalApprove.toString());
         sl.add(PurchaseStatus.done.toString());
         allocateQuery.put(PurchaseBack.paStatus, new DBQuery(DBQueryOpertion.IN, sl));
         Map<String, Integer> allocatEqCountMap = countEqByKey(allocateQuery, DBBean.PURCHASE_ALLOCATE, PurchaseBack.paCount, null);
 
         // 计算剩余数量
         Map<String, Integer> restEqCount = new HashMap<String, Integer>();
 
         for (String id : backEqCountMap.keySet()) {
             int prCount = 0;
             int paCount = 0;
             if (requestEqCountMap.get(id) != null) {
                 prCount = requestEqCountMap.get(id);
             }
             if (allocatEqCountMap.get(id) != null) {
                 paCount = allocatEqCountMap.get(id);
             }
 
             restEqCount.put(id, backEqCountMap.get(id) - prCount - paCount);
         }
 
         return restEqCount;
     }
 	
 }
