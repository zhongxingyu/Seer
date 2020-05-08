 package com.zotyo.accounts.persistence;
 
 import java.util.List;
 
 import javax.persistence.EntityManager;
 import javax.persistence.PersistenceContext;
 
 import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
 
 import com.zotyo.accounts.entity.AccountEntity;
 import com.zotyo.accounts.model.Account;
 
@Transactional
 @Repository
 public class AccountDAOJPAImpl implements AccountDAO {
 
     @PersistenceContext(unitName = "AccountsPU")
     private EntityManager em;
 	
 	public void createAccount(Account a) {
 		AccountEntity ae = new AccountEntity(a.getProject(), a.getEntryname());
 		ae.setPassword(a.getPassword());
 		ae.setPassword2(a.getPassword2());
 		ae.setTag(a.getTag());
 		ae.setUrl(a.getUrl());
 		ae.setLastModified(a.getLastModified());
		ae.setUsername(a.getUsername());
 		em.persist(ae);
 	}
 
 	public boolean deleteAccount(Account a) {
 		// TODO Auto-generated method stub
 		return false;
 	}
 
 	public Account getAccountByProjectAndName(String project, String name) {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	public List<Account> getAccounts() {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	public List<Account> getAccountsByProject(String project) {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	public Account updateAccount(Account a) {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 }
