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
 import de.kile.zapfmaster2000.rest.model.zapfmaster2000.Admin;
 import de.kile.zapfmaster2000.rest.model.zapfmaster2000.Token;
 import de.kile.zapfmaster2000.rest.model.zapfmaster2000.User;
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
 	public String loginUser(String pUserName, String pPassword) {
 		String token = null;
 		if (pUserName != null && pPassword != null) {
 			Session session = Zapfmaster2000Core.INSTANCE
 					.getTransactionService().getSessionFactory()
 					.getCurrentSession();
 			Transaction tx = session.beginTransaction();
 
 			@SuppressWarnings("unchecked")
 			List<User> results = session
 					.createQuery(
 							"SELECT u FROM User u "
 									+ "WHERE u.name = :name AND u.password = :password")
 					.setString("name", pUserName)
 					.setString("password", pPassword).list();
 
 			if (results.size() == 1) {
 				// login succeeded
 				User user = results.get(0);
 				Account account = user.getAccount();
 				token = createNextToken();
 
 				Token tokenEntity = Zapfmaster2000Factory.eINSTANCE
 						.createToken();
 				tokenEntity.setAccount(account);
 				tokenEntity.setUser(user);
 				tokenEntity.setToken(token);
 				session.save(tokenEntity);
 
 			}
 			tx.commit();
 		}
 		return token;
 	}
 
 	@Override
 	public String loginAdmin(String adminName, String password) {
 		String token = null;
 		if (adminName != null && password != null) {
 			Session session = Zapfmaster2000Core.INSTANCE
 					.getTransactionService().getSessionFactory()
 					.getCurrentSession();
 			Transaction tx = session.beginTransaction();
 
 			@SuppressWarnings("unchecked")
			List<Admin> results = session
 					.createQuery(
 							"SELECT a FROM Admin a "
 									+ "WHERE a.name = :name AND a.password = :password")
 					.setString("name", adminName)
 					.setString("password", password).list();
 
 			if (results.size() == 1) {
 				// login succeeded
 				Admin admin = (Admin) results.get(0);
 				token = createNextToken();
 
 				Token tokenEntity = Zapfmaster2000Factory.eINSTANCE
 						.createToken();
 				tokenEntity.setAdmin(admin);
 				tokenEntity.setToken(token);
 				session.save(tokenEntity);
 
 			}
 			tx.commit();
 		}
 		return token;
 	}
 
 	@Override
 	public Account retrieveAccount(String pToken) {
 		Account account = null;
 
 		Session session = Zapfmaster2000Core.INSTANCE.getTransactionService()
 				.getSessionFactory().getCurrentSession();
 		Transaction tx = session.beginTransaction();
 
 		@SuppressWarnings("unchecked")
 		List<Token> result = session
 				.createQuery(
 						"SELECT t FROM Token t JOIN FETCH t.account "
 								+ "WHERE t.token = :token ")
 				.setString("token", pToken).list();
 
 		if (result.size() == 1) {
 			Token token = result.get(0);
 			account = token.getAccount();
 		}
 
 		tx.commit();
 		return account;
 	}
 
 	@Override
 	public User retrieveUser(String pToken) {
 		User user = null;
 
 		Session session = Zapfmaster2000Core.INSTANCE.getTransactionService()
 				.getSessionFactory().getCurrentSession();
 		Transaction tx = session.beginTransaction();
 
 		@SuppressWarnings("unchecked")
 		List<Token> result = session
 				.createQuery(
 						"SELECT t FROM Token t JOIN FETCH t.user AS u "
 								+ "JOIN FETCH u.account "
 								+ "WHERE t.token = :token")
 				.setString("token", pToken).list();
 
 		if (result.size() == 1) {
 			Token token = result.get(0);
 			user = token.getUser();
 		}
 
 		tx.commit();
 		return user;
 	}
 
 	@Override
 	public Admin retrieveAdmin(String token) {
 		Admin admin = null;
 
 		Session session = Zapfmaster2000Core.INSTANCE.getTransactionService()
 				.getSessionFactory().getCurrentSession();
 		Transaction tx = session.beginTransaction();
 
 		@SuppressWarnings("unchecked")
 		List<Token> result = session
 				.createQuery(
 						"SELECT t FROM Token t JOIN FETCH t.admin AS a "
 								+ "WHERE t.token = :token")
 				.setString("token", token).list();
 
 		if (result.size() == 1) {
 			Token dbToken = result.get(0);
 			admin = dbToken.getAdmin();
 		}
 
 		tx.commit();
 		return admin;
 	}
 
 	private String createNextToken() {
 		return new BigInteger(130, secureRandom).toString(32);
 	}
 
 }
