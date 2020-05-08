 package com.projektur.course.bank;
 
 public class Account {
 
	private Integer balance;
	
	public Account() {
		balance = new Integer(0);
	}
 	public boolean setBalance(int i) {
		balance = i;
 		return true;
 	}
 
 	public Integer getBalance() {
 		// TODO Auto-generated method stub
		return balance;
 	}
 
 }
