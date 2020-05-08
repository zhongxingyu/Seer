 package com.jinheyu.lite_mms.data_structures;
 
 import android.os.Parcel;
 import android.os.Parcelable;
 import android.util.SparseArray;
 
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  * Created with IntelliJ IDEA.
  * User: yangminghua
  * Date: 13-8-27
  * Time: 下午12:07
  */
 public class WorkCommand implements Parcelable {
 
     public static final Creator<WorkCommand> CREATOR = new Creator<WorkCommand>() {
         @Override
         public WorkCommand createFromParcel(Parcel source) {
             return new WorkCommand(source);
         }
 
         @Override
         public WorkCommand[] newArray(int size) {
             return new WorkCommand[0];
         }
     };
     private final static SparseArray<String> mSatuses = new SparseArray<String>() {{
         put(Constants.STATUS_DISPATCHING, "待排产");
         put(Constants.STATUS_ASSIGNING, "待分配");
         put(Constants.STATUS_LOCKED, "已锁定");
         put(Constants.STATUS_ENDING, "待结转或结束");
         put(Constants.STATUS_QUALITY_INSPECTING, "待质检");
         put(Constants.STATUS_REFUSED, "车间主任打回");
         put(Constants.STATUS_FINISHED, "已结束");
     }};
     private final static SparseArray<String> mHandleTypes = new SparseArray<String>() {{
         put(Constants.HT_NORMAL, "正常加工");
         put(Constants.HT_REPAIRE, "返修");
         put(Constants.HT_REPLATE, "返镀");
     }};
     private int id;
     private String orderCreateDate;
     private int departmentId;
     private int org_cnt;
     private int org_weight;
     private boolean urgent;
     private String previous_procedure;
     private String procedure;
     private int processed_cnt;
     private int processed_weight;
     private int status;
     private int subOrderId;
     private String tag;
     private int teamId;
     private String tech_req;
     private String picPath;
     private int handleType;
     private int previousWorkCommandId;
     private String spec;
     private String type;
     private String lastMod;
     private int orderType;
     private String unit;
     private int orderNumber;
     private String customerName;
     private boolean reject;
     private String productName;
     private List<QualityInspectionReport> qualityInspectionReportList;
    private String smallPicPath;
 
     public WorkCommand(Parcel parcel) {
         this.id = parcel.readInt();
         this.picPath = parcel.readString();
         this.org_weight = parcel.readInt();
         this.org_cnt = parcel.readInt();
         this.orderType = parcel.readInt();
         this.processed_weight = parcel.readInt();
         this.processed_cnt = parcel.readInt();
         this.status = parcel.readInt();
         this.departmentId = parcel.readInt();
         this.teamId = parcel.readInt();
         this.unit = parcel.readString();
         this.customerName = parcel.readString();
         this.orderNumber = parcel.readInt();
         this.urgent = parcel.readInt() == Constants.TRUE;
         this.handleType = parcel.readInt();
         this.reject = parcel.readInt() == Constants.TRUE;
         this.productName = parcel.readString();
         this.procedure = parcel.readString();
         this.tech_req = parcel.readString();
         this.previous_procedure = parcel.readString();
         this.subOrderId = parcel.readInt();
         this.qualityInspectionReportList = new ArrayList<QualityInspectionReport>();
         this.orderCreateDate = parcel.readString();
        this.smallPicPath = parcel.readString();
         parcel.readTypedList(this.qualityInspectionReportList, QualityInspectionReport.CREATOR);
     }
 
     public WorkCommand(int id, String productName, int org_cnt, int org_weight, int status, boolean isUrgent, boolean isRejected) {
         this.productName = productName;
         this.org_cnt = org_cnt;
         this.org_weight = org_weight;
         this.id = id;
         this.status = status;
         this.urgent = isUrgent;
         this.reject = isRejected;
         this.qualityInspectionReportList = new ArrayList<QualityInspectionReport>();
     }
 
     public static String getStatusString(int status) {
         return mSatuses.get(status);
     }
 
     public void addQualityInspectionReport(QualityInspectionReport qualityInspectionReport) {
         this.qualityInspectionReportList.add(qualityInspectionReport);
     }
 
     /**
      * Describe the kinds of special objects contained in this Parcelable's
      * marshalled representation.
      *
      * @return a bitmask indicating the set of special object types marshalled
      * by the Parcelable.
      */
     @Override
     public int describeContents() {
         return 0;
     }
 
     public String getCustomerName() {
         return customerName;
     }
 
    public String getSmallPicPath() {
        return smallPicPath;
    }

     public void setCustomerName(String customerName) {
         this.customerName = customerName;
     }
 
     public int getDepartmentId() {
         return departmentId;
     }
 
     public void setDepartmentId(int departmentId) {
         this.departmentId = departmentId;
     }
 
     public int getHandleType() {
         return handleType;
     }
 
     public void setHandleType(int handleType) {
         this.handleType = handleType;
     }
 
     public String getHandleTypeString() {
         return mHandleTypes.get(handleType);
     }
 
     public int getId() {
         return id;
     }
 
     public String getOrderCreateDate() {
         return orderCreateDate;
     }
 
     public void setOrderCreateDate(String orderCreateDate) {
         this.orderCreateDate = orderCreateDate;
     }
 
     public int getOrderNumber() {
         return orderNumber;
     }
 
     public void setOrderNumber(int orderNumber) {
         this.orderNumber = orderNumber;
     }
 
     public int getOrderType() {
         return this.orderType;
     }
 
     public void setOrderType(int orderType) {
         this.orderType = orderType;
     }
 
     public int getOrgCnt() {
         return org_cnt;
     }
 
     public int getOrgWeight() {
         return org_weight;
     }
 
     public String getPicPath() {
         return picPath;
     }
 
     public void setPicPath(String picPath) {
         this.picPath = picPath;
     }
 
     public String getPreviousProcedure() {
         return previous_procedure;
     }
 
     public void setPreviousProcedure(String previous_procedure) {
         this.previous_procedure = previous_procedure;
     }
 
     public String getProcedure() {
         return procedure;
     }
 
     public void setProcedure(String procedure) {
         this.procedure = procedure;
     }
 
     public int getProcessedCnt() {
         return processed_cnt;
     }
 
     public void setProcessedCnt(int processedCnt) {
         this.processed_cnt = processedCnt;
     }
 
     public int getProcessedWeight() {
         return processed_weight;
     }
 
     public void setProcessedWeight(int processed_weight) {
         this.processed_weight = processed_weight;
     }
 
     public String getProductName() {
         return productName;
     }
 
     public List<QualityInspectionReport> getQualityInspectionReportList() {
         return this.qualityInspectionReportList;
     }
 
     public String getSpec() {
         return spec;
     }
 
    public void setSmallPicPath(String smallPicPath) {
        this.smallPicPath = smallPicPath;
    }

     public void setSpec(String spec) {
         this.spec = spec;
     }
 
     public int getStatus() {
         return status;
     }
 
     public String getStatusString() {
         return mSatuses.get(status);
     }
 
     public int getSubOrderId() {
         return subOrderId;
     }
 
     public void setSubOrderId(int subOrderId) {
         this.subOrderId = subOrderId;
     }
 
     public int getTeamId() {
         return teamId;
     }
 
     public void setTeamId(int teamId) {
         this.teamId = teamId;
     }
 
     public String getTechReq() {
         return tech_req;
     }
 
     public void setTechReq(String tech_req) {
         this.tech_req = tech_req;
     }
 
     public String getType() {
         return type;
     }
 
     public void setType(String type) {
         this.type = type;
     }
 
     public String getUnit() {
         return unit;
     }
 
     public void setUnit(String unit) {
         this.unit = unit;
     }
 
     public boolean isRejected() {
         return reject;
     }
 
     public boolean isUrgent() {
         return urgent;
     }
 
     public boolean measuredByWeight() {
         return orderType == Constants.STANDARD_ORDER_TYPE;
     }
 
     /**
      * Flatten this object in to a Parcel.
      *
      * @param dest  The Parcel in which the object should be written.
      * @param flags Additional flags about how the object should be written.
      *              May be 0 or {@link #PARCELABLE_WRITE_RETURN_VALUE}.
      */
     @Override
     public void writeToParcel(Parcel dest, int flags) {
         dest.writeInt(id);
         dest.writeString(picPath);
         dest.writeInt(org_weight);
         dest.writeInt(org_cnt);
         dest.writeInt(orderType);
         dest.writeInt(processed_weight);
         dest.writeInt(processed_cnt);
         dest.writeInt(status);
         dest.writeInt(departmentId);
         dest.writeInt(teamId);
         dest.writeString(unit);
         dest.writeString(customerName);
         dest.writeInt(orderNumber);
         dest.writeInt(urgent ? Constants.TRUE : Constants.FALSE);
         dest.writeInt(handleType);
         dest.writeInt(reject ? Constants.TRUE : Constants.FALSE);
         dest.writeString(productName);
         dest.writeString(procedure);
         dest.writeString(tech_req);
         dest.writeString(previous_procedure);
         dest.writeInt(subOrderId);
         dest.writeString(orderCreateDate);
        dest.writeString(smallPicPath);
         dest.writeTypedList(qualityInspectionReportList);
     }
 }
