 package com.twistlet.falcon.security.service;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.security.core.GrantedAuthority;
 import org.springframework.security.core.authority.SimpleGrantedAuthority;
 import org.springframework.security.core.userdetails.User;
 import org.springframework.security.core.userdetails.UserDetails;
 import org.springframework.security.core.userdetails.UserDetailsService;
 import org.springframework.security.core.userdetails.UsernameNotFoundException;
 import org.springframework.transaction.annotation.Transactional;
 
 import com.twistlet.falcon.model.entity.FalconRole;
 import com.twistlet.falcon.model.entity.FalconUser;
 import com.twistlet.falcon.model.entity.FalconUserRole;
 import com.twistlet.falcon.model.repository.FalconUserRepository;
 import com.twistlet.falcon.model.repository.FalconUserRoleRepository;
 
 public class UserDetailsServiceImpl implements UserDetailsService {
 
 	private final FalconUserRepository falconUserRepository;
 	private final FalconUserRoleRepository falconUserRoleRepository;
 
 	@Autowired
 	public UserDetailsServiceImpl(
 			final FalconUserRepository falconUserRepository,
 			final FalconUserRoleRepository falconUserRoleRepository) {
 		this.falconUserRepository = falconUserRepository;
 		this.falconUserRoleRepository = falconUserRoleRepository;
 	}
 
 	@Override
 	@Transactional(readOnly = true)
 	public UserDetails loadUserByUsername(final String username)
 			throws UsernameNotFoundException {
 		final FalconUser falconUser = falconUserRepository.findOne(username);
		if (falconUser == null || falconUser.getValid() == null  || falconUser.getValid() == false) {
 			throw new UsernameNotFoundException("Username " + username
 					+ " not found");
 		}
 		final List<FalconUserRole> list = falconUserRoleRepository
 				.findByFalconUserUsername(username);
 		final List<GrantedAuthority> authorities = new ArrayList<>();
 		for (final FalconUserRole falconUserRole : list) {
 			final FalconRole falconRole = falconUserRole.getFalconRole();
 			final GrantedAuthority grantedAuthority = new SimpleGrantedAuthority(
 					falconRole.getRoleName());
 			authorities.add(grantedAuthority);
 		}
 		return new User(username, falconUser.getPassword(), authorities);
 	}
 }
