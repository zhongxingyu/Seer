 package com.omartech.tdg.service;
 
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Service;
 
 import com.omartech.tdg.mapper.PasswordKeyMapper;
 import com.omartech.tdg.model.PasswordKey;
 
 /**
  * 用途是找回密码时生成key放到数据库
  * @author Sonyfe25cp
  * 2013-8-18
  */
 @Service
 public class PasswordKeyService {
 	
 	@Autowired
 	private PasswordKeyMapper passwordKeyMapper;
 	
 	public PasswordKey createKey(String userType, String email){
 		String key = createKey();
 		PasswordKey pk = new PasswordKey();
 		pk.setEmail(email);
 		pk.setSecret(key);
 		pk.setUserType(userType);
 		passwordKeyMapper.insertPasswordKey(pk);
 		return pk;
 	}
 	
 	public PasswordKey getPasswordKey(String userType, String email){
		PasswordKey key = passwordKeyMapper.getPasswordKey(userType, email);
 		return key;
 	}
 	
 	public String createKey(){
 		String[] array = {"a","b","c","d","e","f","g","h","i","j","k","l","m","n","o","p","q","r","s","t","u","v","w","x","y","z"};
 		String key = "";
 		for(int i = 0; i< 6; i ++){
 			double r = Math.random();
 			int index = (int) Math.round(r*25);
 			String tmp = array[index];
 			if(i%2==0){
 				tmp = tmp.toUpperCase();
 			}
 			key += tmp;
 		}
 		return key;
 	}
 
 	public PasswordKeyMapper getPasswordKeyMapper() {
 		return passwordKeyMapper;
 	}
 	public void setPasswordKeyMapper(PasswordKeyMapper passwordKeyMapper) {
 		this.passwordKeyMapper = passwordKeyMapper;
 	}
 }
