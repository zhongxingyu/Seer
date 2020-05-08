 package controllers;
 
 import java.security.MessageDigest;
 import models.User;
 import play.mvc.With;
 @With(Secure.class)
 public class Security extends Secure.Security{
 	private final static String[] hexDigits = {"0","1","2","3","4","5","6","7","8","9","A","B","C","D","E","F"};
 	static boolean authenticate(String username, String password) {
 		password = encodeByMD5(password);
 		if(!username.matches("[\\w\\.\\-]+@([\\w\\-]+\\.)+[\\w\\-]+")) {
 			return false;
 		}
 		else {
			User tempUser = User.find("byEmail",username).first();
			String userId = tempUser.userid;
			session.put("userId",userId);
			
 			if(User.connect(username, password) != null) {
 				return true;
 			}
 			else {
 				return false;
 			}
 		}
 	}//验证密码
 	
 	static void onDisconnected() {
 		session.clear();
 		Application.index();
 	}
 	
 	static void onAuthenticated() {
 		if(check("admin")) {
 			Admin.index();
 		}
 		else {
 			Application.index();
 		}
 	}
 	
 	static boolean check(String profile) {
 		if("admin".equals(profile)) {
 			if(User.find("byEmail", connected()).<User>first().authority == 1) {
 				System.out.println(User.find("byEmail", connected()).<User>first().authority);
 				return true;
 			}
 			else {
 				return false;
 			}
 		}
 		return false;
 	}//检查是否管理员身份
 	
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
