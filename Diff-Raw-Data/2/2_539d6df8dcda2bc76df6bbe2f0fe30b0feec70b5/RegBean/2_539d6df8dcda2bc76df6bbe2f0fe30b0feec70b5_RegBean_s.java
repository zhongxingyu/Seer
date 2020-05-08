 package org.sopac;
 
 import org.hibernate.Session;
 import org.primefaces.event.FlowEvent;
 import org.sopac.domain.Registration;
 
 import javax.faces.application.FacesMessage;
 import javax.faces.bean.ManagedBean;
 import javax.faces.bean.ViewScoped;
 import javax.faces.context.FacesContext;
 import javax.faces.event.ActionEvent;
 import javax.servlet.http.HttpServletRequest;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 import java.util.logging.Logger;
 
 /**
  * Created with IntelliJ IDEA.
  * User: sachin
  * Date: 5/20/12
  * Time: 12:03 PM
  * To change this template use File | Settings | File Templates.
  */
 @ManagedBean(name = "reg")
 @ViewScoped
 public class RegBean {
 
     private String exEmail;
 
     private List<Registered> registeredList = null;
 
     public List<Registered> getRegisteredList() {
         if (registeredList != null) return registeredList;
         registeredList = new ArrayList<Registered>();
         Session sess = HibernateUtil.getSessionFactory().openSession();
         List res = sess.createQuery("from Registration as r order by r.dateRegistered desc").list();
 
         ArrayList<String> checkList = new ArrayList<String>();
         for (int i = 0; i < res.size(); i++) {
             Registration r = (Registration) res.get(i);
             String email = r.getEmail();
             if (!checkList.contains(email)) {
                 Registered registered = new Registered();
                 registered.setFirstname(r.getFirstName());
                 registered.setSurname(r.getSurname());
                 email = r.getEmail().substring(0, email.indexOf("@") + 1);
                email = email + "xxxxxxxxxxx" + r.getEmail().substring(r.getEmail().indexOf("."), r.getEmail().length());
                 registered.setEmail(email);
                 registered.setOrganisation(r.getOrganisationName());
                 registeredList.add(registered);
                 checkList.add(r.getEmail());
             }
 
 
         }
         sess.close();
         return registeredList;
     }
 
     public void setRegisteredList(List<Registered> registeredList) {
         this.registeredList = registeredList;
     }
 
 
     public String getIpPort() {
         HttpServletRequest req = (HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest();
         return String.valueOf(req.getServerPort());
     }
 
     public void setIpPort(String ipPort) {
         this.ipPort = ipPort;
     }
 
     private String ipPort;
 
     public String getExEmail() {
         return exEmail;
     }
 
     public void setExEmail(String exEmail) {
         this.exEmail = exEmail;
     }
 
     public RegInfo getInfo() {
         return info;
     }
 
     public void setInfo(RegInfo info) {
         this.info = info;
     }
 
     RegInfo info = new RegInfo();
 
     private static Logger logger = Logger.getLogger(RegBean.class.getName());
 
     public String onFlowProcess(FlowEvent event) {
 
 
         logger.info("Current wizard step:" + event.getOldStep());
         logger.info("Next step:" + event.getNewStep());
         return event.getNewStep();
     }
 
     public void load(ActionEvent actionEvent) {
         System.out.println("Loading..");
         if (!getExEmail().contains("@") || !getExEmail().contains(".")) {
             System.out.println(getExEmail() + " ...");
             FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Invalid Email Provided", "Please go back and enter a correct email address.");
             FacesContext.getCurrentInstance().addMessage(null, msg);
             return;
         }
     }
 
     public void save(ActionEvent actionEvent) {
         //Persist user
         //FacesMessage msg = new FacesMessage("Successful", "Welcome :" + user.getFirstname());
         //FacesContext.getCurrentInstance().addMessage(null, msg);
         //System.out.println(info.getFirstName());
         //System.out.println(info.getEmail());
 
         if (!info.getEmail().contains("@") || !info.getEmail().contains(".")) {
             FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Invalid Email Provided", "Please go back and enter a correct email address.");
             FacesContext.getCurrentInstance().addMessage(null, msg);
             return;
         }
 
 
         try {
             //save
             Session s = HibernateUtil.getSessionFactory().openSession();
             s.beginTransaction();
             Registration r = new Registration();
             r.setTitle(info.getTitle());
             r.setAccomodationFirstChoice(info.getAccomodationFirstChoice());
             //r.setAccomodationSecondChoice(info.getAccomodationSecondChoice());
             //r.setAccomodationThirdChoice(info.getAccomodationThirdChoice());
             //r.setBrochures(info.isBrochures());
             r.setCountry(info.getCountry());
             r.setDateOfIssue(info.getDateOfIssue());
             r.setJobTitle(info.getJobTitle());
             //r.setDisplayBoard(info.isDisplayBoard());
             //r.setDisplayTable(info.isDisplayTable());
             r.setEmail(info.getEmail());
             r.setExpiryDate(info.getExpiryDate());
             r.setFax(info.getFax());
             r.setMailingAddress(info.getMailingAddress());
             r.setFirstName(info.getFirstName());
             r.setSurname(info.getSurname());
             r.setNationality(info.getNationality());
             r.setOrganisationName(info.getOrganisationName());
             String orgTypes = "";
             //for (String tmp : info.getOrganisationTypes()) orgTypes = orgTypes + tmp + ", ";
             //r.setOrganisationTypes(orgTypes);
             //r.setOther(info.isOther());
             //r.setOtherMaterials(info.getOtherMaterials());
             r.setPassportNo(info.getPassportNo());
             r.setPlaceOfIssue(info.getPlaceOfIssue());
             //r.setPosters(info.isPosters());
             r.setSpecialMealsRequired(info.getSpecialMealsRequired());
             r.setTelephone(info.getTelephone());
             //r.setTripFirstPreference(info.getTripFirstPreference());
             //r.setTripSecondPreference(info.getTripSecondPreference());
 
             r.setApnicWorkshop1(info.isApnicWorkshop1());
             r.setApnicWorkshop2((info.isApnicWorkshop2()));
 
             r.setDateRegistered(new Date());
 
             s.save(r);
             s.getTransaction().commit();
             s.close();
             System.out.println("Registration Saved.");
 
             FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "Registration Complete", "Thank you for registering for the 2012 PacINET PICISOC Conference.");
             FacesContext.getCurrentInstance().addMessage(null, msg);
         } catch (Exception ex) {
             FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Registration Failed", "Please contact <a href='mailto:sachindra@sopac.org'>sachindra@sopac.org</a> to report this issue.");
             FacesContext.getCurrentInstance().addMessage(null, msg);
         }
 
 
     }
 
 
 }
