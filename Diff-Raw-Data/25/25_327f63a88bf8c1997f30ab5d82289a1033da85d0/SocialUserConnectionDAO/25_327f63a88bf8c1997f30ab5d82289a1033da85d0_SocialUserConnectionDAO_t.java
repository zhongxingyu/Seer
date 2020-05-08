 package org.safaproject.safa.social.dao;
 
 import java.util.List;
 import java.util.Set;
 
 import org.safaproject.safa.dao.SocialUserDAO;
 import org.safaproject.safa.model.user.SocialUser;
 import org.safaproject.safa.social.service.SocialUserService;
import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.security.crypto.encrypt.TextEncryptor;
 import org.springframework.social.connect.Connection;
 import org.springframework.social.connect.ConnectionFactoryLocator;
 import org.springframework.social.connect.ConnectionRepository;
 import org.springframework.social.connect.UsersConnectionRepository;
 
 import com.google.common.base.Function;
 import com.google.common.base.Preconditions;
 import com.google.common.collect.Collections2;
 import com.google.common.collect.Lists;
 import com.google.common.collect.Sets;
 
 public class SocialUserConnectionDAO implements UsersConnectionRepository {
 
	@Autowired
 	private SocialUserDAO socialUserDAO;
 
 	private final ConnectionFactoryLocator connectionFactoryLocator;
 
 	private final TextEncryptor textEncryptor;
 
	@Autowired
 	private SocialUserService socialUserService;
 
 	public SocialUserConnectionDAO(
 			ConnectionFactoryLocator connectionFactoryLocator,
 			TextEncryptor textEncryptor) {
 		this.connectionFactoryLocator = connectionFactoryLocator;
 		this.textEncryptor = textEncryptor;
 	}
 
 	@Override
 	public ConnectionRepository createConnectionRepository(String userId) {
 		Preconditions.checkArgument(userId != null);
 		return new ConnectionDAO(userId, connectionFactoryLocator,
 				textEncryptor);
 	}
 
 	@Override
 	public Set<String> findUserIdsConnectedTo(String providerId,
 			Set<String> providerUserIds) {
 
 		return Sets.newHashSet(Collections2.transform(socialUserDAO
 				.getSocialUserCriteriaBuilder().withProviderId(providerId)
 				.withProviderUserIds(providerUserIds).list(),
 				new SocialUserToId()));
 	}
 
 	/**
 	 * If no user with that Connection is found, a new SocialUser is created in
 	 * the System
 	 **/
 	@Override
 	public List<String> findUserIdsWithConnection(Connection<?> connection) {
 		List<SocialUser> users = socialUserService
 				.findUsersWithConnection(connection);
 
 		if (users.isEmpty()) {
 			SocialUser newUser = socialUserService
 					.createUserWithConnection(connection);
 			users.add(newUser);
 			createConnectionRepository(newUser.getUserId()).addConnection(
 					connection);
 		}
 
 		return Lists.newArrayList(Collections2.transform(users,
 				new SocialUserToId()));
 	}
 
 	public void setSocialUserDAO(SocialUserDAO socialUserDAO) {
 		this.socialUserDAO = socialUserDAO;
 	}
 
 	public ConnectionFactoryLocator getConnectionFactoryLocator() {
 		return connectionFactoryLocator;
 	}
 
 	public void setSocialUserService(SocialUserService socialUserService) {
 		this.socialUserService = socialUserService;
 	}
 
 	private class SocialUserToId implements Function<SocialUser, String> {
 
 		@Override
 		public String apply(SocialUser user) {
 			return user.getUserId();
 		}
 
 	}
 
 }
