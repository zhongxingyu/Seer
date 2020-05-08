 
 package actions;
 
 import dao.UserinfoDao;
 import java.util.Date;
 import tables.Userinfo;
 
 public class User extends BaseAction {
     
     public static class LoginParam {
         String username;
         String password;
         String spamcode;
     }
     public String login() {
         LoginParam param = (LoginParam) getParam(LoginParam.class);
         if(!param.spamcode.equals("FUCKTHESPAMCODE")) {
             if(session("spamcode") == null || !param.spamcode.equals(session("spamcode")) ) {
                 return jsonResult("spamcode");
             }
         }
         
         if(param.username == null || param.username.equals("")){
         	return jsonResult("username");
         }
         
         if(param.password == null || param.password.equals("")){
         	return jsonResult("password");
         }
         
         UserinfoDao ud = new UserinfoDao();
         Userinfo ui = ud.findUserinfoByname(param.username);
         if(ui == null){
         	ui = ud.findUserinfoByMail(param.username);
         }
         if(ui == null){
         	return jsonResult("username");
         }
         
         if(ui.getUpassword().equals(MD5Util.MD5(param.password))){
         	session("myUserinfo",ui);
         	ui.setUstate(new Date());
         	ud.updateUserinfo(ui);
             return jsonResult("ok");
         }
         
         return jsonResult("password");
     }
     
     
     public static class RegisterParam {
         String username;
         String password[];
         String spamcode;
         String email;
     }
     
     public String register(){
     	RegisterParam param = (RegisterParam) getParam(RegisterParam.class);
         if(!param.spamcode.equals("FUCKTHESPAMCODE")) {
             if(session("spamcode") == null || !param.spamcode.equals(session("spamcode")) ) {
                 return jsonResult("spamcode");
             }
         }
         
         if(param.username == null || param.username.equals("")){
         	return jsonResult("username");
         }
         
         if(param.password[0] == null || param.password[0].equals("") || param.password[1] == null || param.password[1].equals("") || !param.password[0].equals(param.password[1])){
         	return jsonResult("password");
         }
         
         if(param.email == null || param.email.equals("")){
         	return jsonResult("email");
         }
         
         UserinfoDao ud = new UserinfoDao();
         Userinfo ui = ud.findUserinfoByMail(param.email);
         if(ui != null){
         	return jsonResult("email");
         }
         ui = ud.findUserinfoByname(param.username);
         if(ui != null){
             return jsonResult("username");
         }
         
         ui = new Userinfo();
         ui.setUname(param.username);
         ui.setUpassword(MD5Util.MD5(param.password[0]));
         ui.setUmail(param.email);
         ud.addUserinfo(ui);
         
         ui = ud.findUserinfoByname(param.username);
         ui.setUstate(new Date());
         ud.updateUserinfo(ui);
         session("myUserinfo",ui);
     	return jsonResult("ok");
     }
     
     
     public static class ChangePasswordParam {
         String oldPassword;
         String newPassword[];
     }
     
     public String changePassword(){
     	ChangePasswordParam param = (ChangePasswordParam) getParam(ChangePasswordParam.class);
     	if(param.oldPassword == null || param.oldPassword.equals("")){
     		return jsonResult("oldPassword");
     	}
     	
     	if(param.newPassword[0] == null || param.newPassword[0].equals("") || param.newPassword[1] == null || param.newPassword[1].equals("") || !param.newPassword[0].equals(param.newPassword[1])){
     		return jsonResult("newPassword");
     	}
     	
     	UserinfoDao ud = new UserinfoDao();
     	Userinfo ui;
     	ui = (Userinfo)session("myUserinfo");
     	Userinfo ui2 = ud.findUserinfoByid(ui.getUid());
     	if(ui2 == null){
     		return jsonResult("username");
     	}
     	if(!ui2.getUpassword().equals(param.oldPassword)){
     		return jsonResult("oldPassword");
     	}
     	ui.setUpassword(MD5Util.MD5(param.newPassword[0]));
     	ud.updateUserinfo(ui);
     	
     	return jsonResult("ok");
     }
     
     
     public String getMyUserinfo(){
     	Userinfo ui = (Userinfo)session("myUserinfo");
         if(ui != null) {
             ui.setUstate(new Date());
             UserinfoDao ud = new UserinfoDao();
             ud.updateUserinfo(ui);
         }
     	return jsonResult(ui);
     }
     
     
     public static class GetUserinfoByUidParam{
     	int userId;
     }
     
     public String getUserinfoByUid(){
     	GetUserinfoByUidParam param = (GetUserinfoByUidParam) getParam(GetUserinfoByUidParam.class);
     	UserinfoDao uDao = new UserinfoDao();
     	Userinfo ui = uDao.findUserinfoByid(param.userId);
     	return jsonResult(ui);
     }
     
     
     public static class GetUsernameByUidParam{
     	int userId;
     }
     
     public String getUsernameByUid(){
     	GetUsernameByUidParam param = (GetUsernameByUidParam) getParam(GetUsernameByUidParam.class);
     	UserinfoDao ud = new UserinfoDao();
     	Userinfo u = ud.findUserinfoByid(param.userId);
    	
     	return jsonResult(u.getUname());
     }
     
     
     public static class EditMyBasicInfoParam{
     	String username;
     	String email;
     	String phone;
     }
     
     public String editMyBasicInfo(){
     	EditMyBasicInfoParam param = (EditMyBasicInfoParam) getParam(EditMyBasicInfoParam.class);
     	UserinfoDao ud = new UserinfoDao();
     	Userinfo ui = (Userinfo)session("myUserinfo");
     	if(ui == null){
     		return jsonResult("username");
     	}
     	if(param.username == null || param.username.equals("")){
     		return jsonResult("username");
     	}
     	
     	ui.setUname(param.username);
         ui.setUmail(param.email);
         ui.setUphone(param.phone);
     	ud.updateUserinfo(ui);
     	    	
     	return jsonResult("ok");
     }
     
     
     public static class EditExtraSettingParam{
     	String portrait;
     	String sketch;
     }
     
     public String editExtraSetting(){
     	EditExtraSettingParam param = (EditExtraSettingParam) getParam(EditExtraSettingParam.class);
     	
     	if(param.portrait == null || param.portrait.equals("")){
     		return jsonResult("portrait");
     	}
 
     	UserinfoDao ud = new UserinfoDao();
     	Userinfo ui;
     	ui = (Userinfo)session("myUserinfo");
     	ui.setUportrait(param.portrait);
     	ui.setUsketch(param.sketch);
     	ud.updateUserinfo(ui);
     	
     	return jsonResult("ok");
     }
     
     
     public static class EditLikeLableSettingParam{
     	String label;
     }
     
     public String editLikeLableSetting(){
     	EditLikeLableSettingParam param = (EditLikeLableSettingParam) getParam(EditLikeLableSettingParam.class);
     	if(param.label == null){
     		return jsonResult("label");
     	}
     	UserinfoDao ud = new UserinfoDao();
     	Userinfo ui;
     	ui = (Userinfo)session("myUserinfo");
     	ui.setUlabel(param.label);
     	ud.updateUserinfo(ui);
     	
     	return jsonResult("ok");
     }
     
     public String logout(){
     	UserinfoDao ud = new UserinfoDao();
     	Userinfo ui = (Userinfo)session("myUserinfo");
     	ui.setUstate(null);
     	ud.updateUserinfo(ui);
     	session("myUserinfo",null);
     	return jsonResult("ok");
     }
     
     
     public static class GetUserPortraitByUidParam{
     	int uid;
     }
     
     public String getUserPortraitByUid(){
     	GetUserPortraitByUidParam param = (GetUserPortraitByUidParam) getParam(GetUserPortraitByUidParam.class);
     	UserinfoDao ud = new UserinfoDao();
     	Userinfo ui = ud.findUserinfoByid(param.uid);
     	if(ui == null){
     		return jsonResult("uid");
     	}
     	String portrait = ui.getUportrait();
     	return jsonResult(portrait);
     }
 }
