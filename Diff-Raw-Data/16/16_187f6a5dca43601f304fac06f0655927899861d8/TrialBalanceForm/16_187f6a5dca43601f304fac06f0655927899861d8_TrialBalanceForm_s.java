 package com.leansoftwarelabs.mypis.view.backing;
 
 import com.leansoftwarelabs.mypis.service.GLEntryFacadeBean;
 
 import java.text.SimpleDateFormat;
 
 import java.util.Calendar;
 import java.util.Date;
 import java.util.List;
 
 import javax.annotation.PostConstruct;
 
 import javax.faces.event.ActionEvent;
 
 import javax.naming.Context;
 import javax.naming.InitialContext;
 
 public class TrialBalanceForm {
     private GLEntryFacadeBean service;
     private List<Object[]> trialBalanceData;
     private Date startDate;
     private Date endDate;
     
     
     @PostConstruct
     public void init(){
         Calendar cal = Calendar.getInstance();
         endDate = cal.getTime();
         cal.set(Calendar.MONTH, 0);
         cal.set(Calendar.DAY_OF_MONTH, 1);
         startDate = cal.getTime();
     }
     public GLEntryFacadeBean getService() {
         if (service == null) {
             try {
                 final Context context = new InitialContext();
                 service = (GLEntryFacadeBean) context.lookup("java:comp/env/ejb/local/GLEntryFacade");
             } catch (Exception ex) {
                 //TODO : bubble up exception or put in log file.
                 ex.printStackTrace();
             }
         }
         return service;
     }
 
 
     public List<Object[]> getTrialBalanceData() {
         return trialBalanceData;
     }
     
     public String getReportTitle(){
         if(trialBalanceData == null){
             return "";
         }
         StringBuilder builder = new StringBuilder("Trial Balance Report from: ");
         SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
         builder.append(df.format(startDate));
         builder.append(" To: ");
         builder.append(df.format(endDate));
         return builder.toString();
     }
 
 
     public void setStartDate(Date startDate) {
         this.startDate = startDate;
     }
 
     public Date getStartDate() {
         return startDate;
     }
 
     public void setEndDate(Date endDate) {
         this.endDate = endDate;
     }
 
     public Date getEndDate() {
         return endDate;
     }
 
     public void runReport(ActionEvent actionEvent) {
         trialBalanceData = getService().getTrialBalanceData(startDate, endDate);
     }
 }
