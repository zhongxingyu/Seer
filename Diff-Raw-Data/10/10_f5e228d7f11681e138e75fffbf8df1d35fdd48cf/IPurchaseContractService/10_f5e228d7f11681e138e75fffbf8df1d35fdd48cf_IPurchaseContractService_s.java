 package com.pms.service.service;
 
 import java.io.InputStream;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 public interface IPurchaseContractService {
 
     public Map<String, Object> getPurchaseContract(Map<String, Object> parameters);
     
     public Map<String, Object> getPurchaseBack(Map<String, Object> parameters);
     
     public Map<String, Object> listPurchaseContracts(Map<String, Object> parameters);
     
     public Map<String, Object> listProjectsAndSuppliersFromContractsForRepositorySelect(Map<String, Object> parameters);
             
     public Map<String, Object> listEqListByProjectAndSupplierForRepository(Map<String, Object> params);
     
     public Map<String, Object> listSalesContractsForShipSelect(Map<String, Object> params);  
         
     public void deletePurchaseContract(Map<String, Object> contract);
     
     public Map<String, Object> updatePurchaseContract(Map<String, Object> contract);
     
     public Map<String, Object> listPurchaseOrders(Map<String, Object> parameters);
     
     public void deletePurchaseOrder(Map<String, Object> order);
     
     public Map<String, Object> updatePurchaseOrder(Map<String, Object> order);
 
     public void approvePurchaseOrder(Map<String, Object> order);
     
     public Map<String, Object> rejectPurchaseOrder(Map<String, Object> order);
     
     public Map<String, Object> cancelPurchaseOrder(Map<String, Object> request);
     
     public Map<String, Object> approvePurchaseContract(Map<String, Object> params);
     
     public Map<String, Object> rejectPurchaseContract(Map<String, Object> params);
     
     public Map<String, Object> listBackRequestForSelect();
     
     public Map<String, Object> listApprovedPurchaseRequestForSelect();
 
     public Map<String, Object> getPurchaseOrder(Map<String, Object> parameters);
     
     
     public List<Map<String,Object>> listApprovedPurchaseContractCosts(String salesContractId);
     
     public Map<String, Object> listApprovedPurchaseOrderForSelect();
     
     
     public Map<String, Object> listPurchaseRequests(Map<String, Object> parameters);
     
     public Map<String, Object> updatePurchaseRequest(Map<String, Object> request);
 
 
     public Map<String, Object> approvePurchaseRequest(Map<String, Object> request);
     
     public Map<String, Object> abrogatePurchaseRequest(Map<String, Object> request);
     
     public Map<String, Object> rejectPurchaseRequest(Map<String, Object> request);
     
     public Map<String, Object> getPurchaseRequest(Map<String, Object> parameters);
     
     public void deletePurchaseRequest(Map<String, Object> order);
 
     public Map<String, Object> listRepositoryRequests(Map<String, Object> parameters);
     
     public Map<String, Object> getRepositoryRequest(Map<String, Object> parserListJsonParameters);
 
     public void deleteRepositoryRequest(Map<String, Object> parserJsonParameters);
 
     public Map<String, Object> updateRepositoryRequest(Map<String, Object> parserListJsonParameters);
 
     public Map<String, Object> approveRepositoryRequest(Map<String, Object> parserJsonParameters);
 
     public Map<String, Object> rejectRepositoryRequest(Map<String, Object> parserJsonParameters);
     
     public Map<String, Object> cancelRepositoryRequest(Map<String, Object> params);
     
     public Map<String, Object> listSelectForPayment(Map<String, Object> params);
     
     public Map<String, Object> listPaymoney(Map<String, Object> params);
 
     public Map<String, Object> savePaymoney(Map<String, Object> params);
     
     public void destoryPayMoney(Map<String,Object> params);
     
     public Map<String, Object> listGetInvoice(Map<String, Object> params);
 
     public Map<String, Object> saveGetInvoice(Map<String, Object> params);
     
     public Map<String, Object> loadGetInvoice(Map<String, Object> params);
     
     public Map<String, Object>viewPCForInvoice(Map<String, Object> params);
     
     public Map<String, Object> prepareGetInvoice(Map<String, Object> params);
     
     public Map<String, Object> updateGetInvoice(Map<String, Object> params);
     
     public void destroyGetInvoice(Map<String, Object> params);
 
     public Map<String, Object> listProjectsForRepositoryDirect(Map<String, Object> parserJsonParameters);
 
     public Map<String, Object> updatePurchaseContractPo(HashMap<String, Object> params);
 
 	public Map<String, Object> importContractHistoryData(InputStream inputStream);
 
     public void backContractToOrder(HashMap<String, Object> parserJsonParameters);
 
     public void deletePurchaseRepository(HashMap<String, Object> parserJsonParameters);
 
     public Map<String, Object> listPurchaseOrderForArrivalSelect();
 
    
 
 }
