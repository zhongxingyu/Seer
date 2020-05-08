 package edu.hust.k54.controller;
 
 import edu.hust.k54.persistence.Taikhoandangnhap;
 import edu.hust.k54.persistence.TaikhoandangnhapHome;
 
 
public class SessionController {
 	private static final int GUEST_PERMISSION = 0;
 	private static final int STAFF_PERMISSION = 1;
 	private static final int MANAGER_PERMISSION = 2;
 	private static final int ADMIN_PERMISSION = 3;
 	
 	public Representative login(String userName, String passWord){
 		TaikhoandangnhapHome checkLogin = new TaikhoandangnhapHome();
 		Taikhoandangnhap persion = new Taikhoandangnhap();
 		persion.setUsername(userName);
 		persion.setPass(passWord);
 		if(!checkLogin.findByExample(persion).isEmpty()){
 			//return (Pep)checkLogin.findByExample(persion).get(0);
 		}
 		return null;
		
 	}
 	
 	public boolean logout(){
 		return true;
 	}
 
 }
