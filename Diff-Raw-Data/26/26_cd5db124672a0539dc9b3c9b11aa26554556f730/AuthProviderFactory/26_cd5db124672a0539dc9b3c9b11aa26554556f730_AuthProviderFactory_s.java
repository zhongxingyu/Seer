 package com.digt.auth;
 
 import java.util.concurrent.ConcurrentHashMap;
 
 import com.google.inject.Inject;
 
 public class AuthProviderFactory {
 
 	private ConcurrentHashMap<String, AuthProvider> provider;
 	
 	@Inject
 	public AuthProviderFactory(LoginFormAuthProvider form, GoogleAuthProvider google, 
 			FBookAuthProvider fbook, TestAuthProvider test, LogoutAuthProvider logout)
 	{
 		provider = new ConcurrentHashMap<String, AuthProvider>();
 		provider.put(LoginFormAuthProvider.AUTH_TYPE, form);
		provider.put(GoogleAuthProvider.AUTH_TYPE, google);
		provider.put(FBookAuthProvider.AUTH_TYPE, fbook);
		provider.put(TestAuthProvider.AUTH_TYPE, test);
 		provider.put(LogoutAuthProvider.AUTH_TYPE, logout);
 	}
 	
 	public AuthProvider getInstance(String type)
 	{
 		if (type == null) return null; 
 		return provider.get(type);
 	}
 }
