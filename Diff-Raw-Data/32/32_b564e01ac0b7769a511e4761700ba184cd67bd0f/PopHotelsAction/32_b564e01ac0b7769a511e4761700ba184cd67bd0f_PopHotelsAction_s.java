 package com.nclodger.control.action.sm;
 
 import com.nclodger.additional.HotelTotalOrder;
 import com.nclodger.additional.ReportInExcel;
 import com.nclodger.control.action.Action;
 import com.nclodger.dao.SMDAO;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import java.util.ArrayList;
 
 /**
  * Created with IntelliJ IDEA.
  * User: pasha
  * Date: 11/21/13
  * Time: 1:02 AM
  */
 public class PopHotelsAction extends Action {
     @Override
     public String execute(HttpServletRequest request, HttpServletResponse response) throws Exception {
         SMDAO smdao = new SMDAO();
 
 
         String startdate =  request.getParameter("start_date") ;
         String enddate =request.getParameter("end_date") ;
         ArrayList<HotelTotalOrder> alist = new ArrayList<HotelTotalOrder>();
         if(startdate.length()<7 && enddate.length()<7){
             alist = smdao.sortHotelbyPopular();
         }
         else{
             alist = smdao.sortHotelbyPopularWithTimeFrame(startdate,enddate);
         }
       /*   request.setAttribute("start_date",startdate);
         request.setAttribute("end_date",enddate);*/
        /* response.set*/
         request.setAttribute("mostpophotel",alist);
 
 
         return "smsettings";
     }
 }
