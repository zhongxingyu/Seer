 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package org.plok.web.beans;
 
 import javax.ejb.EJB;
 import javax.faces.bean.ManagedBean;
 import javax.faces.bean.RequestScoped;
 import javax.validation.constraints.NotNull;
 import javax.validation.constraints.Pattern;
 import java.io.UnsupportedEncodingException;
 import java.security.MessageDigest;
 import java.security.NoSuchAlgorithmException;
 
 import org.plok.model.persistence.Musician;
 import org.plok.model.persistence.auth.Login;
 import org.plok.model.persistence.auth.LoginGroup;
 import org.plok.model.persistence.facade.LoginFacade;
 import org.plok.model.persistence.facade.LoginGroupFacade;
 import org.plok.model.persistence.facade.MusicianFacade;
 
 @ManagedBean
 @RequestScoped
 public class CreateUserBean {
 
     @EJB
     private MusicianFacade facade;
     @EJB
     private LoginFacade loginFacade;
     @EJB
     private LoginGroupFacade loginGroupFacade;
    @NotNull
     private String userName;
     private String password;
    @Pattern(regexp = ".+@.+\\.[a-z]+")
     @NotNull
     private String email;
 
     public String getUserName() {
         return userName;
     }
 
     public void setUserName(String userName) {
         this.userName = userName;
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
 
     public void doCreate() throws UnsupportedEncodingException, NoSuchAlgorithmException {
         Musician musician = new Musician();
         Login login = new Login();
         login.setUsername(userName);
         login.setPassword(convertPass(password));
 
         LoginGroup loginGroup = new LoginGroup();
         loginGroup.setGroupname("default");
 
         loginGroup.setUsername(userName);
 
         musician.setUserName(userName);
         facade.create(musician);
         loginFacade.create(login);
         loginGroupFacade.create(loginGroup);
     }
 
     private String convertPass(String pass) throws UnsupportedEncodingException, NoSuchAlgorithmException {
         MessageDigest m = MessageDigest.getInstance("MD5");
         m.update(pass.getBytes("UTF8"));
         byte s[] = m.digest();
         String result = "";
         for (int i = 0; i < s.length; i++) {
             result += Integer.toHexString((0x000000ff & s[i]) | 0xffffff00).substring(6);
         }
         return result;
     }
 }
