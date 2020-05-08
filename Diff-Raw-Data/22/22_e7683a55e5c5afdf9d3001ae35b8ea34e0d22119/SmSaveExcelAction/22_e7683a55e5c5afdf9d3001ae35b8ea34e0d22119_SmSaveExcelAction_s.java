 package com.nclodger.control.action.sm;
 
 import com.nclodger.additional.HotelTotalOrder;
 import com.nclodger.additional.ReportInExcel;
 import com.nclodger.control.action.Action;
 import com.nclodger.dao.SMDAO;
 import com.nclodger.domain.Hotel;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import java.util.ArrayList;
 
 /**
  * Created with IntelliJ IDEA.
  * User: pasha
  * Date: 11/26/13
  * Time: 10:49 PM
  * To change this template use File | Settings | File Templates.
  */
 public class SmSaveExcelAction extends Action {
 
     @Override
     public String execute(HttpServletRequest request, HttpServletResponse response) throws Exception {
         // If User is not Sales Manager or Administrator
         if(request.getSession().getAttribute("utype") == null ||
                 (request.getSession().getAttribute("utype").toString()).equals("1")) {
             return "home";
         }
 
         ReportInExcel re = new ReportInExcel();
         String start_date = request.getSession().getAttribute("start_date_excel").toString();
         String end_date = request.getSession().getAttribute("end_date_excel").toString();
         ArrayList<HotelTotalOrder> hlist = new ArrayList<HotelTotalOrder>();
         SMDAO smdao = new SMDAO();
        /*hlist = smdao.sortHotelbyPopular();*/
        hlist = smdao.sortHotelbyPopularWithTimeFrame(start_date,end_date);
         re.createMostPopularHotel(hlist,response);
         return "smsetting";
     }
 }
