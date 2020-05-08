 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package controller;
 
 /**
  *
  * @author Vincent
  */
 import org.springframework.web.servlet.mvc.SimpleFormController;
 import org.springframework.web.servlet.*;
 
 public class RegisterAction extends SimpleFormController {
 
     public static String tipsout;
 
     @Override
     protected ModelAndView onSubmit(Object command) throws Exception {
         RegisterForm registerForm = (RegisterForm) command;
 
         String status = registerForm.getStatus();
         String email = registerForm.getEmail();
         String usernamePrefix = registerForm.getUsernamePrefix();
         String username = registerForm.getUsername();
         String password = registerForm.getPassword();
         String sex = registerForm.getSex();
         String ip = "";
 //        String ip = GetIp.getClientIpAddr();
 
 //        System.out.println(status);
 //        System.out.println(email);
 //        System.out.println(usernamePrefix);
 //        System.out.println(username);
 //        System.out.println(password);
 //        System.out.println(sex);
 
         switch (status) {
             case "1"://正版注冊頁面
 
                 OfficialCheck.setEmail(email);
                 OfficialCheck.setPassword(password);
 
                 if (!OfficialCheck.officialCheck()) {//正版帳號檢查失敗
                     tipsout = "正版帳號或密碼錯誤";
                 } else {//正版帳號檢查通過
 
                     DatabaseModify databaseModify = new DatabaseModify();
                     DatabaseModify.setDBname("Account");
                     DatabaseModify.setTablename("users");
                     DatabaseModify.setMethod("INSERT");
                     username = OfficialCheck.getResultUsername();
                     String[][] columnValue = {
                         {"status", status},
                         {"username", username},
                         {"password", Encode.md5(password)},
                         {"sex", sex},
                         {"email", email},
                         {"ip", ip}
                     };
                     DatabaseModify.setColumnValue(columnValue);
 
                     if (databaseModify.insert()) {//寫入成功
                         tipsout = "你想幹嘛?";
                         registerForm.setUsernameOut(usernamePrefix, username);
                         registerForm.setPasswordOut(password);
                         return new ModelAndView(this.getSuccessView(), "user", tipsout);
                     } else {//寫入失敗
                         System.out.println(OfficialCheck.getResult());
                         if (databaseModify.errorCode == 1062) {//sql錯誤訊息比對
                             tipsout = "此帳號已註冊過";
                         } else {
                             tipsout = "註冊失敗";
                         }
                     }
                 }
                 return new ModelAndView(this.getFormView(), "tips1", tipsout);//預計跳回頁面並顯示錯誤訊息
             case "0"://非正版注冊頁面
                 if (!StringCheck.usernameCheck(username)) {//格式檢查錯誤
                     tipsout = "帳號格式錯誤";
                 } else if (!StringCheck.passwordCheck(password)) {
                    tipsout = "密碼要4~12位";
                 } else if (!StringCheck.usernamePrefixCheck(usernamePrefix)) {
                     tipsout = "前綴字不合規定";
                 } else {//格式正確
 
                     DatabaseModify databaseModify = new DatabaseModify();
                     DatabaseModify.setDBname("Account");
                     DatabaseModify.setTablename("users");
                     DatabaseModify.setMethod("insert");
                     String[][] columnValue = {
                         {"status", status},
                         {"username", usernamePrefix + username},
                         {"password", Encode.md5(password)},
                         {"sex", sex},
                         {"ip", ip}
                     };
 
                     DatabaseModify.setColumnValue(columnValue);
 
                     if (databaseModify.insert()) {//寫入成功
                         tipsout = "你想幹嘛?";
                         registerForm.setUsernameOut(usernamePrefix, username);
                         registerForm.setPasswordOut(password);
                         return new ModelAndView(this.getSuccessView(), "user", tipsout);
                     } else {//寫入失敗
                         if (databaseModify.errorCode == 1062) {//sql錯誤訊息比對
                             tipsout = "此帳號已註冊過";
                         } else {
                             tipsout = "註冊失敗";
                         }
                     }
                 }
                 return new ModelAndView(this.getFormView(), "tips0", tipsout);//預計跳回頁面並顯示錯誤訊息
             default://未知的頁面
                 return new ModelAndView(this.getErrorView());
         }
     }
 
     private String getErrorView() {
         throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
     }
 }
