 package com.omartech.tdg.mapper;
 
import java.util.List;

 import org.apache.ibatis.annotations.Param;
 
 import com.omartech.tdg.model.PasswordKey;
 
 public interface PasswordKeyMapper {
 
 	public void insertPasswordKey(PasswordKey key);
 	
	public List<PasswordKey> getPasswordKeyList(@Param("userType")String userType, @Param("email") String email);
 }
