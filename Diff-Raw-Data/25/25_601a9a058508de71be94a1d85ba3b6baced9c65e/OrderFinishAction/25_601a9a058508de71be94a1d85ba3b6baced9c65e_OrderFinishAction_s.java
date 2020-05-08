 package com.nclodger.control.action.order;
 
 import com.nclodger.control.action.Action;
 import com.nclodger.domain.Users;
 import com.nclodger.dao.PromoCodeDAO;
 import com.nclodger.mail.MailConfirmation;
 import com.nclodger.webservices.Hotel;
 import org.springframework.context.ApplicationContext;
 import org.springframework.context.support.ClassPathXmlApplicationContext;
 
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
         String promo = (String)request.getAttribute("promocode");
         request.getSession().setAttribute("promocode", promo);
 
         PromoCodeDAO pcDAO = (PromoCodeDAO) ctx.getBean("promocodeDAO");
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
                 }
            }
 
         }
 
 
         Hotel h = (Hotel)request.getSession().getAttribute("hotel");
         Double final_price = h.getRoomPrice() - h.getRoomPrice()*getPromoPercent(promo);
         request.setAttribute("finalprice", final_price);
         MailConfirmation mailconfirm =(MailConfirmation) ctx.getBean("mailconfirmation");
         //TODO save order in db
         Users user =  (Users)request.getSession().getAttribute("userfull");
         mailconfirm.sendMailOrder(user.getEmail(),final_price,h);
         return "orderfinish";
     }
 }
