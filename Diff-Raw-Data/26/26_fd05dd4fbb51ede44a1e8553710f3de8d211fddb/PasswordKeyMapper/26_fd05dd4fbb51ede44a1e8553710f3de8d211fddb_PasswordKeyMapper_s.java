 package com.omartech.tdg.mapper;
 
 import org.apache.ibatis.annotations.Param;
 
 import com.omartech.tdg.model.PasswordKey;
 
 public interface PasswordKeyMapper {
 
 	public void insertPasswordKey(PasswordKey key);
 	
	public PasswordKey getPasswordKey(@Param("userType")String userType, @Param("email") String email);
 }
