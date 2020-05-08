 package com.nclodger.control.action.sm;
 
 import com.nclodger.additional.AccommodationTotalValue;
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
  * Date: 11/27/13
  * Time: 7:42 PM
  * To change this template use File | Settings | File Templates.
  */
 public class SmSaveAccExcelAction extends Action {
     @Override
     public String execute(HttpServletRequest request, HttpServletResponse response) throws Exception {
         // If User is not Sales Manager or Administrator
         if(request.getSession().getAttribute("utype") == null ||
                 (request.getSession().getAttribute("utype").toString()).equals("1")) {
             return "home";
         }
 
         ReportInExcel re = new ReportInExcel();
         ArrayList<AccommodationTotalValue> alist;
         String start_date = request.getSession().getAttribute("start_date_excel").toString();
         String end_date = request.getSession().getAttribute("end_date_excel").toString();
         SMDAO smdao = new SMDAO();
        if(start_date.equals("") || end_date.equals("")){
           alist = smdao.sortAccommodationbyValuable();
        }
        else{
            alist = smdao.sortAccommodationbyValuableWithTimeFrame(start_date,end_date);
        }

         re.createMostValuableAccomodation(alist,response);
         return "smsetting";
     }
 }
