 package com.nclodger.control.action.order;
 
 import com.nclodger.control.action.Action;
 import com.nclodger.dao.OrderDAO;
 import com.nclodger.domain.*;
 import com.nclodger.dao.PromoCodeDAO;
 import com.nclodger.logic.UserFacadeInterface;
 import com.nclodger.webservices.HotelDTO;
 
 import javax.jms.Session;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 /**
  * Created with IntelliJ IDEA.
  * User: reshet
  * Date: 11/16/13
  * Time: 4:59 PM
  * To change this template use File | Settings | File Templates.
  */
 public class OrderFinishAction extends Action {
     private double getPromoPercent(String promo){
         return 0.20;
     }
 
     @Override
     public String execute(HttpServletRequest request, HttpServletResponse response) throws Exception {
         // If User is not authorized or
         if(request.getSession().getAttribute("utype") == null) {
             return "home";
         }
 
         //ApplicationContext ctx = new ClassPathXmlApplicationContext("bean-config.xml");
 
         //check
         String promo =request.getParameter("promocode");
         request.getSession().setAttribute("promocode", promo);
 
         PromoCodeDAO pcDAO = (PromoCodeDAO) ctx.getBean("promocodeDAO");
         PromoCode pm = null;
         if(!promo.equals("") || promo == null) {
             if(!pcDAO.isExist(promo)){
                 request.setAttribute("isExist",false);
                 return "orderstart";
             }
             else{
                 if(pcDAO.isUsed(promo)){
                     request.setAttribute("isUsed",true);
                     return "orderstart";
                 }
                 else{
                     if(pcDAO.isExpired(promo)){
                         request.setAttribute("isExpired",true);
                         return "orderstart";
                     }else{
                         //here promo is valid
                         pm = pcDAO.get(promo);
                         int b = 0;
                     }
                 }
 
             }
         }
 
         UserFacadeInterface facade = (UserFacadeInterface) ctx.getBean("userFacade");
         HotelDTO h = (HotelDTO)request.getSession().getAttribute("hotel");
 
         String payopt = (String)request.getParameter("payoption");
 
 
 
         //check if hotel exist in db and insert if !exist
         String country = request.getSession().getAttribute("country").toString();
         String city = request.getSession().getAttribute("city").toString();
         Hotel hotModel =  new Hotel(h.getName(),country,city,5,h.getLoc_lng(),h.getLoc_lat(),h.getId());
         OrderDAO odao = new OrderDAO();
         if(!odao.isExistHotelbyID(h.getId())){
            odao.insertHotel(hotModel);
         }
         //
         //check if Acc exist in db and insert if !exist
        /* String prstr =  h.getPrice();
         // parsing string to get number without USD and so on
         String delims = "[ ]+";
         String[] tokens = prstr.split(delims);
         double pr=0.0;
         try{
            // int print = Integer.parseInt(prstr);
             pr = Double.parseDouble(tokens[0]);
         }catch(NumberFormatException ex){ // handle your exception
 
         }*/
 
         //
         double pr = h.getRoomBasePrice();
 
         int hotelID = odao.getIDHotelByintID(h.getId());//get local system ID hotel;not integrated
         Accommodation acc = new Accommodation(hotelID,pr,5,h.getRoomType(),h.getRoomExpediaID());
         if(!odao.isExistAccbyID(acc.getRoomExpediaID())){
             odao.insertAccommodation(acc);
         }
 
         //end
 
 
         User user =  (User)request.getSession().getAttribute("userfull");
 
         Order order = new Order();
         order.setH(h);
         order.setPromo(pm);
         order.setUserid(user.getId());
         order.setPrice_list_choice(Integer.parseInt(payopt));
         order.setStart_date((String)request.getSession().getAttribute("checkindate"));
         order.setEnd_date((String)request.getSession().getAttribute("checkoutdate"));
 
         facade.calculateFinalPrice(order);
         request.setAttribute("finalprice", order.getFinal_price());
         facade.saveOrder(order);
 
 
         //Double final_price = h.getRoomBasePrice() - h.getRoomBasePrice()*getPromoPercent(promo);
         //request.setAttribute("finalprice", final_price);
 
 
         return "orderfinish";
     }
 }
