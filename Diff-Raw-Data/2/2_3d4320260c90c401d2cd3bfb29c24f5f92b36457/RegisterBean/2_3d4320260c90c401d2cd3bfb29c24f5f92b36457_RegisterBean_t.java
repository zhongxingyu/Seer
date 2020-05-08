 package com.marbl.rekeningrijders.website.bean;
 
 //<editor-fold defaultstate="collapsed" desc="Imports">
 import com.marbl.administration.domain.Driver;
 import com.marbl.rekeningrijders.website.service.RekeningRijdersService;
 import java.io.IOException;
 import java.io.Serializable;
 import java.util.Date;
 import java.util.Properties;
 import javax.enterprise.context.SessionScoped;
 import javax.faces.context.ExternalContext;
 import javax.faces.context.FacesContext;
 import javax.inject.Inject;
 import javax.inject.Named;
 import javax.mail.Authenticator;
 import javax.mail.Message;
 import javax.mail.MessagingException;
 import javax.mail.Session;
 import javax.mail.Transport;
 import javax.mail.internet.InternetAddress;
 import javax.mail.internet.MimeMessage;
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServletRequest;
 //</editor-fold>
 
 @Named
 @SessionScoped
 public class RegisterBean implements Serializable {
 
     //<editor-fold defaultstate="collapsed" desc="Fields">
     @Inject
     private RekeningRijdersService service;
     private int bsn;
     private String email;
     private String password;
     private String languageCode;
     private String firstName;
     private String lastName;
     private String residence;
     private String address;
     private String zipCode;
     private Date dateOfBirth;
     //</editor-fold>
 
     //<editor-fold defaultstate="collapsed" desc="Getters & Setters">
     public int getBsn() {
         return bsn;
     }
 
     public void setBsn(int bsn) {
         this.bsn = bsn;
     }
 
     public String getEmail() {
         return email;
     }
 
     public void setEmail(String email) {
         this.email = email;
     }
 
     public String getPassword() {
         return password;
     }
 
     public void setPassword(String password) {
         this.password = password;
     }
 
     public String getLanguageCode() {
         return languageCode;
     }
 
     public void setLanguageCode(String languageCode) {
         this.languageCode = languageCode;
     }
 
     public String getFirstName() {
         return firstName;
     }
 
     public void setFirstName(String firstName) {
         this.firstName = firstName;
     }
 
     public String getLastName() {
         return lastName;
     }
 
     public void setLastName(String lastName) {
         this.lastName = lastName;
     }
 
     public String getResidence() {
         return residence;
     }
 
     public void setResidence(String residence) {
         this.residence = residence;
     }
 
     public String getAddress() {
         return address;
     }
 
     public void setAddress(String address) {
         this.address = address;
     }
 
     public String getZipCode() {
         return zipCode;
     }
 
     public void setZipCode(String zipCode) {
         this.zipCode = zipCode;
     }
 
     public Date getDateOfBirth() {
         return dateOfBirth;
     }
 
     public void setDateOfBirth(Date dateOfBirth) {
         this.dateOfBirth = dateOfBirth;
     }
     //</editor-fold>
 
     //<editor-fold defaultstate="collapsed" desc="Methods">
     public void register() {
         Driver newDriver = new Driver(bsn, email, service.hash(password), languageCode, firstName, lastName, residence, address, zipCode, dateOfBirth, false);
         service.register(newDriver);
         sendMail();
     }
 
     public void sendMail() {
         final String emailUsername = "fontyspts7gserver@gmail.com";
         final String emailPassword = "swpts7control";
 
         Properties props = new Properties();
         props.put("mail.smtp.auth", "true");
         props.put("mail.smtp.starttls.enable", "true");
         props.put("mail.smtp.host", "smtp.gmail.com");
         props.put("mail.smtp.port", "587");
 
         Authenticator auth = new Authenticator() {
             @Override
             protected javax.mail.PasswordAuthentication getPasswordAuthentication() {
                 return new javax.mail.PasswordAuthentication(emailUsername, emailPassword);
             }
         };
 
         Session session = Session.getInstance(props, auth);
 
         try {
             Message message = new MimeMessage(session);
             message.setFrom(new InternetAddress("payment@RekeningRijders.no-ip.biz"));
             message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(email));
             message.setSubject("Activatie Rekening Rijders");
            message.setText("Klik op onderstaande link om uw account te activeren.\nhttp://localhost:8080/RekeningRijdersWebsiteM/activation.xhtml?bsn=" + bsn);
 
             Transport.send(message);
         } catch (MessagingException e) {
             throw new RuntimeException(e);
         }
     }
 
     public void activateDriver() {
         Driver driver = service.findDriver(bsn);
         driver.setActivated(true);
         service.editDriver(driver);
 
         FacesContext facesContext = FacesContext.getCurrentInstance();
         ExternalContext externalContext = facesContext.getExternalContext();
         HttpServletRequest request = (HttpServletRequest) externalContext.getRequest();
 
         try {
             request.logout();
             externalContext.redirect(".");
         } catch (IOException ex) {
         } catch (ServletException ex) {
         }
     }
     //</editor-fold>
 }
