 package com.morgajel.spoe.web;
 /**
  * Simple object to catch the initial setPassword form.
  */
 public class SetPasswordForm {
 
 	public String checksum;
 	public String username;
 	public String password;
 	public String confirmPassword;
 
 	/**
	 * returns the checksum provided by a hidden field in the form
 	 */
 	public String getChecksum() {
 		return checksum;
 	}	
 	/** 
 	 * Sets the checksum; used to prove the user received the email.
 	 */
 	public void setChecksum(String checksum) {
 		this.checksum = checksum;
 	}
 	/**
 	 * Gets the username provided by the password form.
 	 */
 	public String getUsername() {
 		return username;
 	}
 	/**
 	 * Sets the username provided by the password form.
 	 */
 	public void setUsername(String username) {
 		this.username = username;
 	}
 	/**
 	 * Returns the password entered by the user- WARNING, PLAIN TEXT STILL!
 	 */
 	public String getPassword() {
 		return password;
 	}
 	/**
 	 * Sets the password entered by the user- WARNING, PLAIN TEXT STILL!
 	 */
 	public void setPassword(String password) {
 		this.password = password;
 	}
 	/**
 	 * Returns the confirmation password entered by the user- WARNING, PLAIN TEXT STILL!
 	 */
 	public String getConfirmPassword() {
 		return confirmPassword;
 	}
 	/**
 	 * Sets the confirmation password entered by the user- WARNING, PLAIN TEXT STILL!
 	 */
 	public void setConfirmPassword(String confirmPassword) {
 		this.confirmPassword = confirmPassword;
 	}
 	/**
 	 * Compares the passwords given, fails if they differ.
 	 */
 	public boolean comparePasswords(){
 		if (this.password.equals(confirmPassword)){
 			return true;
 		}else{
 			return false;
 		}
 	}
 }
