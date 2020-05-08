 package com.nclodger.control.action.order;
 
 import com.nclodger.control.action.Action;
 import com.nclodger.domain.Order;
 import com.nclodger.domain.PromoCode;
 import com.nclodger.domain.User;
 import com.nclodger.dao.PromoCodeDAO;
 import com.nclodger.logic.UserFacadeInterface;
 import com.nclodger.mail.MailConfirmation;
 import com.nclodger.webservices.Hotel;
 
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
         //ApplicationContext ctx = new ClassPathXmlApplicationContext("bean-config.xml");
 
         //check
        String promo =request.getParameter("promocode");
         request.getSession().setAttribute("promocode", promo);
 
         PromoCodeDAO pcDAO = (PromoCodeDAO) ctx.getBean("promocodeDAO");
         PromoCode pm = null;
         if(promo!=null) {
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
         Hotel h = (Hotel)request.getSession().getAttribute("hotel");
         User user =  (User)request.getSession().getAttribute("userfull");
 
         Order order = new Order();
         order.setH(h);
         order.setPromo(pm);
         order.setUserid(user.getId());
         order.setStart_date((String)request.getSession().getAttribute("checkindate"));
         order.setEnd_date((String)request.getSession().getAttribute("checkoutdate"));
 
         facade.calculateFinalPrice(order);
         request.setAttribute("finalprice", order.getFinal_price());
         facade.saveOrder(order);
 
 
         //Double final_price = h.getRoomPrice() - h.getRoomPrice()*getPromoPercent(promo);
         //request.setAttribute("finalprice", final_price);
 
 
         return "orderfinish";
     }
 }
