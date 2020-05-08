 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package BB;
 
 import Model.AbstractPerson;
 import Model.Account;
 import Model.Person;
 import EJB.UserRegistry;
 import java.io.Serializable;
 import javax.annotation.PostConstruct;
 import javax.ejb.EJB;
 import javax.enterprise.context.SessionScoped;
 import javax.faces.context.FacesContext;
 import javax.faces.event.ActionEvent;
 import javax.inject.Named;
 
 /**
  *
  * @author kristofferskjutar
  */
 @Named("mypageBean")
 @SessionScoped
 public class MyPageBean implements Serializable {
     
    @EJB
    private UserRegistry reg;
    
    private String username;
     
    private Long idNumber;
    
    private String name;
    
    private String adress;
    
    private String telephoneNumber;
    
    private String emailAdress;
    
    private String picUrl;
     private Account a;
     private AbstractPerson person;
    
    @PostConstruct
    public void init()
    {
        String sessionName =  (String)FacesContext.getCurrentInstance()
                 .getExternalContext().getSessionMap().get("username");
        a = reg.getByName(sessionName).get(0);
        person = a.getPerson();
        setUsername(a.getUserName());
        setAdress(person.getAddress());
        setEmailAdress(person.getMail());
        setIdNumber(person.getIdNumber());
        setTelephoneNumber(person.getPhoneNbr());
        setName(person.getName());
        setPicUrl(person.getPicUrl());
        
    }
    
    
    
    public String update()
    {
       Person p = new Person(person.getId(),getIdNumber(), getName(), getEmailAdress(), 
                getTelephoneNumber(), getAdress());
       p.setPicUrl(getPicUrl());
       Account updated = new Account(a.getId(), p, a.getUserName(), a.getPassWord());
        reg.update(updated);    
        FacesContext facesContext = FacesContext.getCurrentInstance();
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
