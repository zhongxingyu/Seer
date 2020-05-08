 package controllers;
 
 import play.mvc.Controller;
 import play.*;
 import models.*;
 import java.security.MessageDigest;
 import Secure.*;
 public class EditPassword extends Controller {
 	private final static String[] hexDigits = {"0","1","2","3","4","5","6","7","8","9","A","B","C","D","E","F"};
 	public static void index() {
 		render();
 	}
 
	public static void editPassword(String userId, String previous, String newpass, String newpass2) {
		User existUser = User.find("byUserid",userId).first();
 		previous = encodeByMD5(previous);
 		newpass = encodeByMD5(newpass);
 		newpass2 = encodeByMD5(newpass2);
 		if(previous.equals(existUser.password)) {
 			if(newpass2.equals(newpass)) {
 				existUser.password = newpass;
 				existUser.save();
 				String message = "edit sucess";
 				//Secure.logout();
				session.clear();
				Application.index();
 			}
 			else {
 				String error = "ERROR";
 				render(error);
 			}
 		}
 		else {
 			String error = "Not corret.";
 			render(error);
 		}
 	}
 
 	public static String encodeByMD5(String str){ 
         if (str!=null) { 
             try { 
                 //创建具有指定算法名称的信息摘要 
                 MessageDigest md5 = MessageDigest.getInstance("MD5"); 
                 //使用指定的字节数组对摘要进行最后更新，然后完成摘要计算 
                 byte[] results = md5.digest(str.getBytes()); 
                 //将得到的字节数组变成字符串返回  
                 String result = byteArrayToHexString(results); 
                 return result; 
             } catch (Exception e) { 
                 e.printStackTrace(); 
             } 
         } 
         return null; 
     }
 	
 	public static String byteArrayToHexString(byte[] b){ 
         StringBuffer resultSb = new StringBuffer(); 
         for(int i=0;i<b.length;i++){ 
             resultSb.append(byteToHexString(b[i])); 
         } 
         return resultSb.toString(); 
     }
 	
 	public static String byteToHexString(byte b){ 
         int n = b; 
         if(n<0) 
         n=256+n; 
         int d1 = n/16; 
         int d2 = n%16; 
         return hexDigits[d1] + hexDigits[d2]; 
     }//加密
 }
