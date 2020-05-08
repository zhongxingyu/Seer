 package com.onb.otp.service.impl;
 
 import java.util.HashSet;
 import java.util.Set;
 
 import org.apache.commons.lang.RandomStringUtils;
import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Service;
 
 import com.onb.otp.domain.OneTimePassword;
 import com.onb.otp.domain.OneTimePasswordList;
import com.onb.otp.persistence.base.OneTimePasswordListDaoBase;
 import com.onb.otp.service.base.PasswordServiceBase;
 
 @Service
 public class PasswordService implements PasswordServiceBase {
 	
 	private static final int LIST_SIZE = 100;
	@Autowired
	private OneTimePasswordListDaoBase passwordListDao;
 
 	public OneTimePasswordList generatePasswordList() {
 		OneTimePasswordList passwordList = new OneTimePasswordList();
 		passwordList.setPasswords(generatePasswords());
		passwordListDao.save(passwordList);
 		return passwordList;
 	}
 	
 	private Set<OneTimePassword> generatePasswords() {
 		Set<OneTimePassword> passwords = new HashSet<OneTimePassword>();
 		for(int index=0; index<LIST_SIZE; index++) {
 			passwords.add(new OneTimePassword(RandomStringUtils.randomAlphanumeric(10)));
 		}
 		return passwords;
 	}
 }
