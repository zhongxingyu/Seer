 package com.pedrero.eclihand.service.impl;
 
 import java.util.HashSet;
 import java.util.Set;
 
 import javax.annotation.Resource;
 
 import org.springframework.stereotype.Service;
 import org.springframework.transaction.annotation.Transactional;
 
 import com.pedrero.eclihand.converter.UserConverter;
 import com.pedrero.eclihand.dao.PlayerDao;
 import com.pedrero.eclihand.dao.UserDao;
 import com.pedrero.eclihand.model.domain.Credential;
 import com.pedrero.eclihand.model.domain.Player;
 import com.pedrero.eclihand.model.domain.User;
 import com.pedrero.eclihand.model.domain.UserType;
 import com.pedrero.eclihand.model.dto.AuthorizationDto;
 import com.pedrero.eclihand.model.dto.ProfileDto;
 import com.pedrero.eclihand.model.dto.UserDto;
 import com.pedrero.eclihand.service.PlayerService;
 import com.pedrero.eclihand.service.UserService;
 
 @Service
 public class UserServiceImpl extends DataObjectServiceImpl<UserDto, User>
 		implements UserService {
 	public static final String AGE_WHEN_PLAYING_FOR_TEAM = "age.when.playing.for.team";
 
 	@Resource
 	private UserConverter userConverter;
 
 	@Resource
 	private UserDao<User> userDao;
 
 	@Resource
 	private PlayerDao<Player> playerDao;
 
 	@Resource
 	private PlayerService playerService;
 
 	@Override
 	public UserConverter getConverter() {
 		return userConverter;
 	}
 
 	public void setTeamConverter(UserConverter userConverter) {
 		this.userConverter = userConverter;
 	}
 
 	@Override
 	public UserDao<User> getDao() {
 		return userDao;
 	}
 
 	public void setTeamDao(UserDao<User> userDao) {
 		this.userDao = userDao;
 	}
 
 	@Override
 	public Set<Credential> retrieveCredentialsFor(UserDto user) {
 		Set<Credential> credentials = new HashSet<Credential>();
 		for (ProfileDto profile : findById(user.getId()).getProfiles()) {
 			for (AuthorizationDto authorization : profile.getAuthorizations()) {
 				credentials.add(authorization.getCredential());
 			}
 		}
 		return credentials;
 	}
 
 	@Override
 	@Transactional
 	public UserDto retrieveGuestUser() {
 		User guestUser = getDao().findByUserType(UserType.GUEST);
 		return getConverter().convertToDto(guestUser);
 	}
 
 }
