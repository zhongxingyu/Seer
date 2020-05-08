 package com.onb.otp.service.base;
 
import java.util.List;

import com.onb.otp.domain.OneTimePassword;
 import com.onb.otp.domain.OneTimePasswordList;
 
 public interface PasswordServiceBase {
 	public OneTimePasswordList generatePasswordList();
 }
