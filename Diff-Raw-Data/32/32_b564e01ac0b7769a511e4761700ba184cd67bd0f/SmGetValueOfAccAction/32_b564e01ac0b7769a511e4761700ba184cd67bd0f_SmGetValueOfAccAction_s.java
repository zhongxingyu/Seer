 package com.nclodger.control.action.sm;
 
 import com.nclodger.additional.AccommodationTotalValue;
 import com.nclodger.additional.HotelTotalOrder;
 import com.nclodger.control.action.Action;
 import com.nclodger.dao.SMDAO;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import java.util.ArrayList;
 
 /**
  * Created with IntelliJ IDEA.
  * User: pasha
  * Date: 11/27/13
  * Time: 9:46 PM
  * To change this template use File | Settings | File Templates.
  */
 public class SmGetValueOfAccAction extends Action {
     @Override
     public String execute(HttpServletRequest request, HttpServletResponse response) throws Exception {
         SMDAO smdao = new SMDAO();
 
 /*
         ArrayList<AccommodationTotalValue> alist;
         alist = smdao.sortAccommodationbyValuable();
 
         request.setAttribute("mostvalacc",alist);*/
         //
         String startdate =  request.getParameter("start_date") ;
         String enddate =request.getParameter("end_date") ;
         ArrayList<AccommodationTotalValue> alist;
         if(startdate.length()<7 && enddate.length()<7){
             alist = smdao.sortAccommodationbyValuable();
         }
         else{
             alist = smdao.sortAccommodationbyValuableWithTimeFrame(startdate,enddate);
         }
 
         request.setAttribute("mostvalacc",alist);
 
         return "smsettings";
     }
 }
 
 
