 package com.mrroman.springgames.utils;
 
 import org.springframework.util.DigestUtils;
 
 public class HashUtils {
 
 	public String md5(String text) {
		return new String(DigestUtils.md5Digest(text.getBytes()));
 	}
 	
 }
