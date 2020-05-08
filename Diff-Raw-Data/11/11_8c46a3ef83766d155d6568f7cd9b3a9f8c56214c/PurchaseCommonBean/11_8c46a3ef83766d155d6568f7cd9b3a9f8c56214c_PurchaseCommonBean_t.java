 package com.pms.service.mockbean;
 
 
 public class PurchaseCommonBean extends BaseEntity {
     
     private String projectId;
     
     private String customerId;
     
     private String approvedDate;
 
     private String backRequestCode;
 
     private String backRequestId;
 
     private String salesContractId;
     
     private String salesContractCode;
 
     private String purchaseOrderCode;
     
     private String purchaseOrderId;
 
     private String purchaseRequestCode;
     
     private String purchaseRequestId;
     
     private String comment;
     
     private String scContractType;
     
     
     private String requestedDate;
     
     
     private String approveComment;
     
 
     
     //FIX ME
     private String pbPlanDate;
     
     
     
     private String pbDepartment;
     
     private String purchaseContractType;
 
 
     public String getPurchaseContractType() {
 		return purchaseContractType;
 	}
 
 	public void setPurchaseContractType(String purchaseContractType) {
 		this.purchaseContractType = purchaseContractType;
 	}
 
 
     public String getApproveComment() {
 		return approveComment;
 	}
 
 	public void setApproveComment(String approveComment) {
 		this.approveComment = approveComment;
 	}
 
 	public String getPbPlanDate() {
         return pbPlanDate;
     }
 
     public void setPbPlanDate(String pbPlanDate) {
         this.pbPlanDate = pbPlanDate;
     }
 
 
     public String getPbDepartment() {
         return pbDepartment;
     }
 
     public void setPbDepartment(String pbDepartment) {
         this.pbDepartment = pbDepartment;
     }
     
 
     public String getRequestedDate() {
         return requestedDate;
     }
 
     public void setRequestedDate(String requestedDate) {
         this.requestedDate = requestedDate;
     }
 
     public String getProjectId() {
         return projectId;
     }
 
     public void setProjectId(String projectId) {
         this.projectId = projectId;
     }
     
     
 
     public String getCustomerId() {
         return customerId;
     }
 
     public void setCustomerId(String customerId) {
         this.customerId = customerId;
     }
 
     public String getBackRequestCode() {
         return backRequestCode;
     }
 
     public void setBackRequestCode(String backRequestCode) {
         this.backRequestCode = backRequestCode;
     }
 
     public String getBackRequestId() {
         return backRequestId;
     }
 
     public void setBackRequestId(String backRequestId) {
         this.backRequestId = backRequestId;
     }
 
 
     public String getPurchaseOrderId() {
         return purchaseOrderId;
     }
 
     public void setPurchaseOrderId(String purchaseOrderId) {
         this.purchaseOrderId = purchaseOrderId;
     }
 
     public String getApprovedDate() {
         return approvedDate;
     }
 
     public void setApprovedDate(String approvedDate) {
         this.approvedDate = approvedDate;
     }
 
     public String getPurchaseOrderCode() {
         return purchaseOrderCode;
     }
 
     public void setPurchaseOrderCode(String purchaseOrderCode) {
         this.purchaseOrderCode = purchaseOrderCode;
     }
 
     public String getPurchaseRequestCode() {
         return purchaseRequestCode;
     }
 
     public void setPurchaseRequestCode(String purchaseRequestCode) {
         this.purchaseRequestCode = purchaseRequestCode;
     }
 
     public String getSalesContractId() {
         return salesContractId;
     }
 
     public void setSalesContractId(String salesContractId) {
         this.salesContractId = salesContractId;
     }
 
     public String getSalesContractCode() {
         return salesContractCode;
     }
 
     public void setSalesContractCode(String salesContractCode) {
         this.salesContractCode = salesContractCode;
     }
 
 
     public String getPurchaseRequestId() {
         return purchaseRequestId;
     }
 
     public void setPurchaseRequestId(String purchaseRequestId) {
         this.purchaseRequestId = purchaseRequestId;
     }
 
     public String getComment() {
         return comment;
     }
 
     public void setComment(String comment) {
         this.comment = comment;
     }
     
     
 
     public String getScContractType() {
         return scContractType;
     }
 
     public void setScContractType(String scContractType) {
         this.scContractType = scContractType;
     }
 
 
 
     public static final String PROCESS_STATUS = "status";
     public static final String APPROVED_DATE = "approvedDate";
     public static final String SALES_CONTRACT_CODE = "salesContractCode";
 
     public static final String STATUS_DRAFT = "草稿";
     public static final String STATUS_SUBMITED = "已提交";
     public static final String STATUS_NEW = "审批中";
     public static final String STATUS_APPROVED = "审批通过";
     public static final String STATUS_REJECTED = "审批拒绝";
     public static final String MANAGER_APPROVED = "经理审批通过";
     public static final String STATUS_BACKED = "已退回";
     
     public static final String STATUS_ABROGATED_NEED_APPROVED = "废除待审批";
     public static final String STATUS_ABROGATED = "已废除";
     
     public static final String STATUS_CANCELLED = "已退回";
     
     public static final String STATUS_IN_REPOSITORY = "已入库";
     public static final String STATUS_OUT_REPOSITORY = "已出库";
     public static final String STATUS_REPOSITORY_NEW = "入库中";
    public static final String STATUS_IN_OUT_REPOSITORY = "已出库";
     public static final String STATUS_ORDERING = "采购中";
     
     public static final String STATUS_ORDER_LOCKED = "已锁定";
     public static final String STATUS_ORDER_FINISHED = "采购完毕";
     public static final String STATUS_COMPLETED = "执行完毕";
     
     //物流类型
     public static final String LOGISTICS_TYPE_VALUE_DIRECTY = "直发"; 
     public static final String EQCOST_DELIVERY_TYPE_DIRECTY = "直发现场"; 
     public static final String EQCOST_DELIVERY_TYPE_REPOSITORY = "入公司库"; 
     
     
     public static final String LOGISTICS_TYPE = "logisticsType";
     
     public static final String PURCHASE_REQUEST_CODE = "purchaseRequestCode";
     public static final String PURCHASE_REQUEST_ID = "purchaseRequestId";
     public static final String PURCHASE_ORDER_CODE = "purchaseOrderCode";
     public static final String PURCHASE_ORDER_ID = "purchaseOrderId";
     public static final String PURCHASE_CONTRACT_ID = "purchaseContractId";
     public static final String PURCHASE_CONTRACT_CODE = "purchaseContractCode";
     
     
     
     public static final String PROJECT_ID = "projectId";
     
     public static final String SALES_COUNTRACT_ID = "salesContractId";
     
     
     public static final String BACK_REQUEST_ID = "backRequestId";
     
     public static final String EQCOST_APPLY_AMOUNT = "eqcostApplyAmount";
     
     // 虚拟采购合同类型
     public static final String CONTRACT_EXECUTE_CATE = "contractExecuteCate";
     
     public static final String CONTRACT_EXECUTE_CATE_BEIJINGDAICAI = "施耐德北京代采";
     public static final String CONTRACT_EXECUTE_BJ_REPO = "施耐德北京库存";
     public static final String CONTRACT_EXECUTE_BJ_MAKE = "泰康北京生产";
     public static final String CONTRACT_EXECUTE_TAIKANG_BJ_REPO = "泰康北京库存";
     public static final String CONTRACT_EXECUTE_ALLOCATE_BJ_REPO = "调拨-北京备货货架";
     public static final String CONTRACT_EXECUTE_ALLOCATE_BJ_REPO_VALUE = "北京备货货架";
     public static final String CONTRACT_EXECUTE_NORMAL = "正常采购";
     public static final String CONTRACT_EXECUTE_CATE_NORMAL = "正常采购";
 
     
     public static final String EQCOST_PRODUCT_UNIT_PRICE = "eqcostProductUnitPrice";
     
     public static final String DESCRIPTION = "description";
     public static final String REMARK = "remark";
     
 }
