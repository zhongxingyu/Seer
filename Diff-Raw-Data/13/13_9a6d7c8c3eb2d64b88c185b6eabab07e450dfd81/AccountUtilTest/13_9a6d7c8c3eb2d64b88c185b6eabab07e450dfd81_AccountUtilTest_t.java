 package ch.zhaw.mdp.fallstudie.jmail.core.account;
 
 import java.util.List;
 
 import junit.framework.Assert;
 
 import org.junit.BeforeClass;
 import org.junit.Test;
 
 import ch.zhaw.mdp.fallstudie.jmail.core.account.Account;
 import ch.zhaw.mdp.fallstudie.jmail.core.account.AccountUtil;
 
 /**
  * Tests the logic of {@link AccountUtil}.
  */
 public class AccountUtilTest {
 
 	@BeforeClass
 	public static void setup() {
 		System.setProperty("user.home", System.getProperty("java.io.tmpdir"));
 	}
 	
 	@Test
 	public void testLoadAccounts() {
 		List<Account> accounts = AccountUtil.loadAccounts();
		Assert.assertEquals(2, accounts.size());
		Assert.assertEquals("Test@Yahoo", accounts.get(0).getAccountName());
 		Assert.assertEquals("jmail.test@yahoo.com", accounts.get(0).getAddress());
 	}
 	
 	@Test
 	public void testSaveAccounts() {
 		List<Account> accounts = AccountUtil.loadAccounts();
		Assert.assertEquals(2, accounts.size());
 		
 		Account account = new Account();
 		account.setAccountName("TestAccount");
 		account.setAddress("test.account@foo.com");
 		
 		accounts.add(account);
 		
 		AccountUtil.saveAccounts(accounts);
 		accounts = AccountUtil.loadAccounts();
		Assert.assertEquals(3, accounts.size());
 		
 		accounts.remove(1);
 		
 		AccountUtil.saveAccounts(accounts);
 		accounts = AccountUtil.loadAccounts();
		Assert.assertEquals(2, accounts.size());
 	}
 	
 }
