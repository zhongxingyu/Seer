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
         ReportInExcel re = new ReportInExcel();
         ArrayList<HotelTotalOrder> hlist = new ArrayList<HotelTotalOrder>();
         SMDAO smdao = new SMDAO();
        hlist = smdao.sortHotelbyPopular();
         re.createMostPopularHotel(hlist,response);
         return "smsetting";
     }
 }
