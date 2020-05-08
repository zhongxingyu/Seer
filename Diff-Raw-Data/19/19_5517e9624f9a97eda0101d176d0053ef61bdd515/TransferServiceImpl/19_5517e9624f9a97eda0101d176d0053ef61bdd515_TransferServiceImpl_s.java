 package com.luel.easyb.service;
 
 import org.springframework.beans.factory.annotation.Autowired;
 
 import com.luel.easyb.domain.Account;
 import com.luel.easyb.domain.AccountRepository;
 
 public class TransferServiceImpl implements TransferService {
 
 	@Autowired
 	private AccountRepository accountRepository;	
 	
 	//@Transactional
 	public TransferConfirmation transfer(long amount, String srcAccntID,
 			String destAcctId) {
 		
 		Account srcAct = accountRepository.findByAccountId(srcAccntID);
 		Account targetAct = accountRepository.findByAccountId(destAcctId);
 		
 		srcAct.withdraw(amount);
 		targetAct.deposit(amount);
 		
 		TransferConfirmation transferConfirmation = new TransferConfirmation();
 		transferConfirmation.setNewBalance(srcAct.getBalance());
 		return transferConfirmation;
 	}
 }
