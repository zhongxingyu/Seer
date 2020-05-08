 package action;
 
 import com.opensymphony.xwork2.ActionSupport;
 import com.sun.org.apache.xalan.internal.xsltc.trax.TemplatesHandlerImpl;
 import org.apache.struts2.ServletActionContext;
 import pojo.Register;
 import pojo.SiteUser;
 import service.RegisterService;
 import service.SiteUserService;
 
 import javax.servlet.Servlet;
 import java.sql.Timestamp;
 import java.util.Date;
 
 /**
  * User: yumingzhe
  * Date: 5/21/12
  * Time: 11:35 AM
  */
 
 public class AccountConfirm extends ActionSupport {
    private int uid;
     private String secret;
     private SiteUserService siteUserService;
     private RegisterService registerService;
 
     public RegisterService getRegisterService() {
         return registerService;
     }
 
     public void setRegisterService(RegisterService registerService) {
         this.registerService = registerService;
     }
 
     public SiteUserService getSiteUserService() {
         return siteUserService;
     }
 
     public void setSiteUserService(SiteUserService siteUserService) {
         this.siteUserService = siteUserService;
     }
 
    public int getUid() {
         return uid;
     }
 
    public void setUid(int uid) {
         this.uid = uid;
     }
 
     public String getSecret() {
         return secret;
     }
 
     public void setSecret(String secret) {
         this.secret = secret;
     }
 
     @Override
     public String execute() throws Exception {
        SiteUser siteUser = siteUserService.getSiteUserByUID(uid);
         if (siteUser == null) {
             this.addActionError("We can not activate your account because of the wrong activate link");
             return INPUT;
         }
         if (siteUser.getActive()) {
            this.addActionError("The user whose uid is " + uid + " has been activated");
             return INPUT;
         }
         Register register = registerService.getRegisterBySequence(secret);
         if (register == null) {
             this.addActionError("The activate link is invalid");
             return INPUT;
         }
        if (!register.getUid().equals(this.uid)) {
            this.addActionError("The activate link is unmatch with the user, we can not activate you");
             return INPUT;
         }
         String registerEmailTTL = (String) ServletActionContext.getServletContext().getAttribute("registerEmailTTL");
         if (registerEmailTTL == null) {
             registerEmailTTL = "24";
         }
         int ttl = Integer.parseInt(registerEmailTTL);
         //email validate timeout
         if (register.getRegisterTime().before(new Timestamp(new Date().getTime() - ttl * 1000 * 60 * 60))) {
             this.addActionError("Your validate email has expired, we can not activate your account");
             return ERROR;
         }
         siteUser.setActive(true);
         siteUserService.updateSiteUser(siteUser);
        registerService.deleteAllRegistersByUID(siteUser.getUid());
         return SUCCESS;
     }
 }
