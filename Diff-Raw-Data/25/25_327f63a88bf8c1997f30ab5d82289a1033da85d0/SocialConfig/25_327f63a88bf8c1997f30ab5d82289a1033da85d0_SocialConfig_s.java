 package org.safaproject.safa.social.config;
 
 import javax.inject.Inject;
 
 import org.safaproject.safa.dao.RoleDAO;
 import org.safaproject.safa.dao.SocialUserDAO;
 import org.safaproject.safa.social.dao.ConnectionDAO;
 import org.safaproject.safa.social.dao.SocialUserConnectionDAO;
 import org.safaproject.safa.social.service.SocialUserService;
 import org.springframework.context.annotation.Bean;
 import org.springframework.context.annotation.ComponentScan;
 import org.springframework.context.annotation.ComponentScan.Filter;
 import org.springframework.context.annotation.Configuration;
 import org.springframework.context.annotation.PropertySource;
 import org.springframework.context.annotation.Scope;
 import org.springframework.context.annotation.ScopedProxyMode;
 import org.springframework.core.env.Environment;
 import org.springframework.security.core.Authentication;
 import org.springframework.security.core.context.SecurityContextHolder;
 import org.springframework.security.crypto.encrypt.Encryptors;
 import org.springframework.social.connect.Connection;
 import org.springframework.social.connect.ConnectionFactoryLocator;
 import org.springframework.social.connect.ConnectionRepository;
 import org.springframework.social.connect.UsersConnectionRepository;
 import org.springframework.social.connect.support.ConnectionFactoryRegistry;
 import org.springframework.social.facebook.api.Facebook;
 import org.springframework.social.facebook.api.impl.FacebookTemplate;
 import org.springframework.social.facebook.connect.FacebookConnectionFactory;
 import org.springframework.transaction.annotation.EnableTransactionManagement;
 
 @Configuration
 @ComponentScan(basePackages = "org.safaproject.safa.social", excludeFilters = { @Filter(Configuration.class) })
 @PropertySource("classpath:application.properties")
 @EnableTransactionManagement
 public class SocialConfig {
 
 	@Inject
 	private Environment environment;
 
	@Inject
 	private SocialUserService socialUserService;
 
	@Inject
 	private SocialUserDAO socialUserDAO;
 
	@Inject
 	private RoleDAO roleDAO;
 
 	@Bean
 	public ConnectionFactoryLocator connectionFactoryLocator() {
 		ConnectionFactoryRegistry registry = new ConnectionFactoryRegistry();
 		registry.addConnectionFactory(new FacebookConnectionFactory(environment
 				.getProperty("facebook.clientId"), environment
 				.getProperty("facebook.clientSecret")));
 		return registry;
 	}
 
 	@Bean
 	public UsersConnectionRepository usersConnectionRepository() {
 		SocialUserConnectionDAO repository = new SocialUserConnectionDAO(
 				connectionFactoryLocator(), Encryptors.noOpText());
 		repository.setSocialUserService(socialUserService);
 		return repository;
 	}
 
 	@Bean
 	@Scope(value = "request", proxyMode = ScopedProxyMode.INTERFACES)
 	public ConnectionRepository connectionRepository() {
 		Authentication authentication = SecurityContextHolder.getContext()
 				.getAuthentication();
 		if (authentication == null) {
 			throw new IllegalStateException(
 					"Unable to get a ConnectionRepository: no user signed in");
 		}
 		ConnectionDAO dao = (ConnectionDAO) usersConnectionRepository()
 				.createConnectionRepository(authentication.getName());
 		dao.setRoleDAO(roleDAO);
 		dao.setSocialUserDAO(socialUserDAO);
 		return dao;
 	}
 	
 	@Bean
 	@Scope(value="request", proxyMode=ScopedProxyMode.INTERFACES)	
 	public Facebook facebook() {
 		Connection<Facebook> facebook = connectionRepository().findPrimaryConnection(Facebook.class);
 		return facebook != null ? facebook.getApi() : new FacebookTemplate();
 	}
 
 }
