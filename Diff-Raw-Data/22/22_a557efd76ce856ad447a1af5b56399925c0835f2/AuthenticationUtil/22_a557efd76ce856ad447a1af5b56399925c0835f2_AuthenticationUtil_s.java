 package nl.topicus.onderwijs.security;
 
 import java.util.List;
 
 import nl.topicus.onderwijs.PizzaSession;
 import nl.topicus.onderwijs.dao.filters.AccountZoekFilter;
 import nl.topicus.onderwijs.entities.Account;
 import nl.topicus.onderwijs.models.ELModelFactory;
 import nl.topicus.onderwijs.providers.AccountProvider;
 
 import org.apache.shiro.SecurityUtils;
 import org.apache.shiro.authc.UsernamePasswordToken;
 import org.apache.shiro.subject.Subject;
 
 public class AuthenticationUtil
 {
 	public static void loggedOff()
 	{
 		Subject subject = SecurityUtils.getSubject();
 		subject.logout();
 		PizzaSession.get().setAccount(null);
 	}
 
 	public static void loggedOn(AccountProvider accProvider, String username, String password)
 			throws Exception
 	{
 		// Try login
 		Subject currentUser = SecurityUtils.getSubject();
 		currentUser.login(new UsernamePasswordToken(username, password));
 
 		// Set account
 
 		Account acc = null;
 		AccountZoekFilter filter = new AccountZoekFilter();
 		filter.setGebruikersnaam(username);
 
 		List<Account> accounts = accProvider.list(filter);
 
 		if (accounts.isEmpty())
 		{
 			acc = new Account();
 			acc.setGebruikersnaam(username);
 			acc.saveAndCommit();
 		}
 		else if (accounts.size() == 1)
 		{
 			acc = accounts.get(0);
 		}
 		else
 		{
 			throw new Exception("Unknown login error.");
 		}
 
 		// Set session
 		PizzaSession.get().setAccount(ELModelFactory.getModel(acc));
 
 	}
 }
