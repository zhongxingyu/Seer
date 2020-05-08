 package com.luel.easyb.domain;
 
 public interface AccountRepository {
 
 	public Account findByAccountId(String acctId);
	public void update(Account account);
 }
