 package de.kile.zapfmaster2000.rest.impl.core.auth;
 
 import java.math.BigInteger;
 import java.security.SecureRandom;
 import java.util.List;
 
 import org.hibernate.Query;
 import org.hibernate.Session;
 import org.hibernate.Transaction;
 
 import de.kile.zapfmaster2000.rest.core.Zapfmaster2000Core;
 import de.kile.zapfmaster2000.rest.core.auth.AuthService;
 import de.kile.zapfmaster2000.rest.model.zapfmaster2000.Account;
 import de.kile.zapfmaster2000.rest.model.zapfmaster2000.Token;
 import de.kile.zapfmaster2000.rest.model.zapfmaster2000.Zapfmaster2000Factory;
 
 public class AuthServiceImpl implements AuthService {
 
 	private final SecureRandom secureRandom = new SecureRandom();
 
 	@Override
 	public String loginAccount(String pAccountName) {
 		String token = null;
 		if (pAccountName != null) {
 			Session session = Zapfmaster2000Core.INSTANCE
 					.getTransactionService().getSessionFactory()
 					.getCurrentSession();
 			Transaction tx = session.beginTransaction();
 
 			Query query = session.createQuery(
 					"SELECT a FROM Account a WHERE a.name = :name").setString(
 					"name", pAccountName);
 
 			@SuppressWarnings("unchecked")
 			List<Account> results = query.list();
 
 			if (results.size() == 1) {
 				// login succeeded
 				Account account = results.get(0);
 				token = createNextToken();
 
 				Token tokenEntity = Zapfmaster2000Factory.eINSTANCE
 						.createToken();
 				tokenEntity.setAccount(account);
 				tokenEntity.setToken(token);
 				session.save(tokenEntity);
 
 			}
 			tx.commit();
 		}
 		return token;
 	}
 
 	@Override
 	public Account retrieveAccount(String pToken) {
 
 		Session session = Zapfmaster2000Core.INSTANCE.getTransactionService()
 				.getSessionFactory().getCurrentSession();
 		Transaction tx = session.beginTransaction();
 
 		@SuppressWarnings("unchecked")
 		List<Token> result = session
				.createQuery("SELECT t FROM Token t WHERE t.token = :token")
 				.setString("token", pToken).list();
 
 		if (result.size() == 1) {
 			Token token = result.get(0);
			return token.getAccount();
 		}
 
 		tx.commit();
		return null;
 	}
 
 	private String createNextToken() {
 		return new BigInteger(130, secureRandom).toString(32);
 	}
 
 }
