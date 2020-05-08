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
         ReportInExcel re = new ReportInExcel();
         ArrayList<AccommodationTotalValue> alist;
         SMDAO smdao = new SMDAO();
        alist = smdao.sortAccommodationbyValuable();
         re.createMostValuableAccomodation(alist,response);
         return "smsetting";
     }
 }
