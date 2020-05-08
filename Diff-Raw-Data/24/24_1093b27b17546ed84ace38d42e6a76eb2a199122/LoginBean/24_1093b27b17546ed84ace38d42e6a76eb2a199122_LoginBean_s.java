 package com.WebDemail.webview;
 
 import com.WebDemail.service.Login;
 
 import javax.ejb.EJB;
 import javax.faces.application.FacesMessage;
 import javax.faces.bean.ManagedBean;
 import javax.faces.bean.ManagedProperty;
 import javax.faces.bean.SessionScoped;
 import javax.faces.component.UIComponent;
 import javax.faces.context.FacesContext;
 
 /**
  * Created with IntelliJ IDEA.
  * User: Korshunov Dmitry
  * Date: 05.07.13
  * Time: 22:32
  * To change this template use File | Settings | File Templates.
  */
 @ManagedBean
 @SessionScoped
 public class LoginBean {
     private String phone;
     private String password;
     private UIComponent loginActionBtn;
 
     @ManagedProperty(value="#{userDataBean}")
     private UserDataBean userDataBean;
 
    @EJB
    private Login login;
 
     public String loginMethod(){
        if(login.validateLogin(phone, password)){
            login.service(phone, password);
             return "addresses.jsf?faces-redirect=true";
         }else{
             FacesContext facesContext = FacesContext.getCurrentInstance();
             facesContext.addMessage(loginActionBtn.getClientId(facesContext), new FacesMessage("Invalid user data!."));
             return null;
         }
     }
 
     public String getPhone() {
         return phone;
     }
 
     public void setPhone(String phone) {
         this.phone = phone;
     }
 
     public String getPassword() {
         return password;
     }
 
     public void setPassword(String password) {
         this.password = password;
     }
 
    public Login getLogin() {
        return login;
    }

    public void setLogin(Login login) {
        this.login = login;
    }
 
     public UIComponent getLoginActionBtn() {
         return loginActionBtn;
     }
 
     public void setLoginActionBtn(UIComponent loginActionBtn) {
         this.loginActionBtn = loginActionBtn;
     }
 
     public UserDataBean getUserDataBean() {
         return userDataBean;
     }
 
     public void setUserDataBean(UserDataBean userDataBean) {
         this.userDataBean = userDataBean;
     }
 }
