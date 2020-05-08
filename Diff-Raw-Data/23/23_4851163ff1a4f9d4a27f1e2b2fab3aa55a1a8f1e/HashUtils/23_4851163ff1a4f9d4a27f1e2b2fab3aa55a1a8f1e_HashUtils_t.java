 package com.mrroman.springgames.utils;
 
 import org.springframework.util.DigestUtils;
 
 public class HashUtils {
 
 	public String md5(String text) {
		return DigestUtils.md5DigestAsHex(text.getBytes());
 	}
 	
 }
