 package org.cotrix.security.impl;
 
 import static org.cotrix.user.PredefinedUsers.*;
 
 import java.util.Iterator;
 
 import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.SessionScoped;
 import javax.enterprise.inject.Instance;
 import javax.enterprise.inject.Produces;
 import javax.inject.Inject;
 import javax.servlet.http.HttpServletRequest;
 
 import org.cotrix.common.cdi.BeanSession;
 import org.cotrix.common.cdi.Current;
 import org.cotrix.repository.UserRepository;
 import org.cotrix.security.LoginService;
 import org.cotrix.security.Realm;
 import org.cotrix.security.Token;
 import org.cotrix.security.TokenCollector;
 import org.cotrix.security.exceptions.UnknownUserException;
 import org.cotrix.user.User;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 @ApplicationScoped
 public class DefaultLoginService implements LoginService {
 	
 	private static Logger log = LoggerFactory.getLogger(LoginService.class);
 	
 	@Inject
 	private Instance<TokenCollector> collectors;
 	
 	@Inject
 	private Instance<Realm<?>> realms;
 	
 	@Inject
 	private UserRepository users;
 	
 	@Inject
 	private BeanSession session;
 	
 	
 	@Override
 	public User login(HttpServletRequest request) {
 	
 		Token token = collectTokenFrom(request);
 		
 		User user = null;
 		
 		//no credentials provided, it's a guest
 		if (token==null)
 			user = guest;
 		else {
 						
 			//from credentials to an identity
 			String identity = identifyFrom(token);
 			
 			if (identity==null)
 				throw new UnknownUserException("unknown user for token "+token);
 			
 			//from identity to currentUser
 			user = users.lookup(identity);
 			
 			if (user==null)
 				throw new UnknownUserException("unknown user "+identity);
 			
 			//log only for non-guests
 			log.info("{} ({}) has logged in",user.id(),user.name());
 		}
 		
 		//remember for this session
 		session.add(User.class,user);
 
 		return user;
 	}
 	
	@Produces @SessionScoped
 	public @Current User currentUser() {
 	
 		try {
 			return session.get(User.class);
 		}
 		catch(IllegalStateException e) {
 
 			//this should never happen: let's be more specific
			throw new IllegalAccessError("a protected resource has passed through the barrier without an authenticated user");
 			
 		}		
 	}
 	
 	//helpers
 	private Token collectTokenFrom(HttpServletRequest request) {
 	
 		Token token =null;
 
 		Iterator<TokenCollector> it = collectors.iterator();
 		
 		while (it.hasNext() && token==null)
 			token = it.next().token(request);
 		
 		return token;
 	}
 	
 	private String identifyFrom(Token token) {
 		
 		Iterator<Realm<?>> it = realms.iterator();
 		
 		String id = null;
 		
 		search: while (it.hasNext() && id==null) {
 			
 			@SuppressWarnings("unchecked")
 			Realm<Token> authenticator = (Realm<Token>) it.next();
 			
 			if (authenticator.supports(token)) {
 				id = authenticator.login(token);
 				if (id!=null)
 					break search;
 			}
 		}
 		
 		return id;
 	}
 }
