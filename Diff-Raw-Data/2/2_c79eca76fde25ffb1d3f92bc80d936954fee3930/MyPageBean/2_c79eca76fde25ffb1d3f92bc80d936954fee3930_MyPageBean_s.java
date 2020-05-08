 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package BB;
 
 import Model.AbstractPerson;
 import Model.Account;
 import Model.Person;
 import EJB.UserRegistry;
 import EJB.WorkerRegistry;
 import Model.Worker;
 import java.io.Serializable;
 import javax.annotation.PostConstruct;
 import javax.ejb.EJB;
 import javax.enterprise.context.SessionScoped;
 import javax.faces.context.FacesContext;
 import javax.faces.event.ActionEvent;
 import javax.inject.Named;
 import javax.servlet.http.HttpSession;
 
 /**
  *
  * @author kristofferskjutar
  */
 @Named("mypageBean")
 @SessionScoped
 public class MyPageBean implements Serializable {
     
    @EJB
    private UserRegistry reg;
    
    @EJB
    private WorkerRegistry wReg;
    
    private String username;
     
    private Long idNumber;
    
    private String name;
    
    private String adress;
    
    private String telephoneNumber;
    
    private String emailAdress;
    
    
    private String picUrl;
     private Account a;
     private Worker person;
    
    @PostConstruct
    public void init()
    {
        Long modelId =  (Long)FacesContext.getCurrentInstance()
                 .getExternalContext().getSessionMap().get("id");
        a = reg.find(modelId);
       person = (Worker)a.getPerson();
        setUsername(a.getUserName());
        setAdress(person.getAddress());
        setEmailAdress(person.getMail());
        setIdNumber(person.getIdNumber());
        setTelephoneNumber(person.getPhoneNbr());
        setName(person.getName());
        setPicUrl(person.getPicUrl());
        initSessionList();
        
    }
    
    private void initSessionList()
    {
        
    }
    
    public String update()
    {
        person.setAddress(getAdress());
        person.setIdNumber(getIdNumber());
        person.setMail(getEmailAdress());
        person.setName(getName());
        person.setPhoneNbr(getTelephoneNumber());
        person.setPicUrl(picUrl);
        
        //Worker w = new Worker(person.getId(),getIdNumber(), getName(), getEmailAdress(), 
        //        getTelephoneNumber(), getAdress());
         
        wReg.update(person);    
        return "MyPage";
    }
    
    
    
 
     /**
      * @return the idNumber
      */
     public Long getIdNumber() {
         return idNumber;
     }
 
     /**
      * @param idNumber the idNumber to set
      */
     public void setIdNumber(Long idNumber) {
         this.idNumber = idNumber;
     }
 
     /**
      * @return the name
      */
     public String getName() {
         return name;
     }
 
     /**
      * @param name the name to set
      */
     public void setName(String name) {
         this.name = name;
     }
 
     /**
      * @return the adress
      */
     public String getAdress() {
         return adress;
     }
 
     /**
      * @param adress the adress to set
      */
     public void setAdress(String adress) {
         this.adress = adress;
     }
 
     /**
      * @return the telephoneNumber
      */
     public String getTelephoneNumber() {
         return telephoneNumber;
     }
 
     /**
      * @param telephoneNumber the telephoneNumber to set
      */
     public void setTelephoneNumber(String telephoneNumber) {
         this.telephoneNumber = telephoneNumber;
     }
 
     /**
      * @return the emailAdress
      */
     public String getEmailAdress() {
         return emailAdress;
     }
 
     /**
      * @param emailAdress the emailAdress to set
      */
     public void setEmailAdress(String emailAdress) {
         this.emailAdress = emailAdress;
     }
 
     /**
      * @return the picUrl
      */
     public String getPicUrl() {
         return picUrl;
     }
 
     /**
      * @param picUrl the picUrl to set
      */
     public void setPicUrl(String picUrl) {
         this.picUrl = picUrl;
     }
 
     /**
      * @return the username
      */
     public String getUsername() {
         return username;
     }
 
     /**
      * @param username the username to set
      */
     public void setUsername(String username) {
         this.username = username;
     }
     
     
     
 }
