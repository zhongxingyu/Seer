 package com.quanleimu.entity;
 import java.io.Serializable;
 public class WeiboAccessTokenWrapper  extends Object implements Serializable{
 	private static final long serialVersionUID = 1L;
 	private String token = null;
	private String expires_in = null;
	public WeiboAccessTokenWrapper(String token, String expires){
 		this.token = token;
 		this.expires_in = expires;
 	}
 	public String getToken(){
 		return token;
 	}
	public String getExpires(){
 		return expires_in;
 	}
 }
