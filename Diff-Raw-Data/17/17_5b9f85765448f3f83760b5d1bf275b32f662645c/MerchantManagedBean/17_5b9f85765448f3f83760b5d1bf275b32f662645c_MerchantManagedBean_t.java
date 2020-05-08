 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package SMMS.managedbean;
 
 import ERMS.session.EPasswordHashSessionBean;
 import ERMS.session.EmailSessionBean;
 import Exception.ExistException;
 import SMMS.entity.MerchantEntity;
 import SMMS.session.MerchantSessionBean;
 import java.io.IOException;
import java.io.Serializable;
 import java.util.Collection;
 import java.util.Date;
 import java.util.Timer;
 import java.util.TimerTask;
 import java.util.UUID;
 import javax.annotation.PostConstruct;
 import javax.annotation.Resource;
 import javax.ejb.EJB;
 import javax.ejb.Schedule;
 import javax.ejb.SessionContext;
 import javax.ejb.Timeout;
 import javax.ejb.TimerService;
 import javax.faces.application.FacesMessage;
 import javax.faces.bean.ManagedBean;
 import javax.faces.bean.ViewScoped;
 import javax.faces.context.FacesContext;
 import javax.faces.event.ActionEvent;
 
 /**
  *
  * @author Cookie
  */
 @ManagedBean
 @ViewScoped
 public class MerchantManagedBean implements Serializable{
 
     @EJB
     private EmailSessionBean emailSessionBean;
     @EJB
     private EPasswordHashSessionBean ePasswordHashSessionBean;
     @EJB
     private MerchantSessionBean merchantSessionBean;
     private MerchantEntity merchant;
     
 
     @PostConstruct
     public void init() {
         FacesContext.getCurrentInstance().getExternalContext().getSession(true);
     }
 
     public MerchantManagedBean() {
         merchant = new MerchantEntity();
     }
 
 //    public void createTimers(ActionEvent event) {
 //        System.out.println("in creating timers");
 //        merchantSessionBean.createTimers();
 //    }
 ////
 //    public void cancelTimers() {
 //        TimerService timerService = ctx.getTimerService();
 //        Collection timers = timerService.getTimers();
 //        for (Object obj : timers) {
 //            Timer timer = (Timer) obj;
 //            if (timer.getInfo().toString()) {
 //                timer.cancel();
 //            }
 //        }
 //    }
 
 //    @Timeout
 //    public void handleTimeout(Timer timer) {
 ////        if (timer.toString().equals("EJBTIMER")) {//Do something}}}
 //            Date currentDate = new Date();
 //            System.out.println("No1: we are in merchant managedbean: trying this hahaha lalala" + currentDate);
         
//    }
 
 //    public static int count = 0;
 //    public Timer timer = new Timer();
 //    public TimerTask task = new TimerTask() {
 //        @Override
 //        public void run() {
 //            count++;
 //            if (count > 6) {
 //                timer.cancel();
 //                timer.purge();
 //            }
 //            Date currentDate = new Date();
 //            System.out.println("No2: we are in merchant managedbean: trying this hahaha lalala" + currentDate);
 //        }
 //    };
 //
 //    public void test(ActionEvent event) {
 //        System.out.println("No1: in test");
 ////        timer.scheduleAtFixedRate(task, 0, 1000);
 //        timer.scheduleAtFixedRate(task, 0, 1000);
 //    }
     /**
      * Creates a new instance of AddEmployeeManagedBean
      */
     public void saveNewMerchant(ActionEvent event) throws IOException, ExistException {
         System.out.println("add new merchant: " + merchant.getMerchantEmail());
         if (merchantSessionBean.getMerchantById(merchant.getMerchantEmail()) != null) {
             FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Mercant already exists", ""));
             return;
         } else {
             String initialPwd = "";
             String uuid = UUID.randomUUID().toString();
             String[] sArray = uuid.split("-");
             initialPwd = sArray[0];
             merchant.setMerchantPassword(initialPwd);
             System.out.println("add new merchant hash password!");
             merchant.setMerchantPassword(ePasswordHashSessionBean.hashPassword(merchant.getMerchantPassword()));
             System.out.println("finished hashing");
 
             try {
                 System.out.println("we are in save new merchant in managedbean" + merchant.getMerchantEmail());
                 merchantSessionBean.addMerchant(merchant);
                 System.out.println("we are after adding merchant in managedbean");
             } catch (Exception e) {
                 FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Error occurs when adding new merchant", ""));
                 return;
             }
             FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "New Merchant saved.", ""));
 //            emailSessionBean.emailInitialPassward(employee.getPersonalEmail(), initialPwd); //send email
             emailSessionBean.emailInitialPassward(merchant.getMerchantEmail(), initialPwd);
             System.out.println("email already sent");
             merchant = new MerchantEntity();
 
         }
     }
 
     public void oneMore(ActionEvent event) throws IOException {
         FacesContext.getCurrentInstance().getExternalContext().redirect("addMerchant.xhtml");
     }
 
     public MerchantEntity getMerchant() {
         return merchant;
     }
 
     public void setMerchant(MerchantEntity merchant) {
         this.merchant = merchant;
     }
     /**
      * Creates a new instance of MerchantManagedBean
      */
 }
