 package com.quanleimu.entity;
 import java.io.Serializable;
 public class WeiboAccessTokenWrapper  extends Object implements Serializable{
 	private static final long serialVersionUID = 1L;
 	private String token = null;
	private long expires_in = 0;
	public WeiboAccessTokenWrapper(String token, long expires){
 		this.token = token;
 		this.expires_in = expires;
 	}
 	public String getToken(){
 		return token;
 	}
	public long getExpires(){
 		return expires_in;
 	}
 }
