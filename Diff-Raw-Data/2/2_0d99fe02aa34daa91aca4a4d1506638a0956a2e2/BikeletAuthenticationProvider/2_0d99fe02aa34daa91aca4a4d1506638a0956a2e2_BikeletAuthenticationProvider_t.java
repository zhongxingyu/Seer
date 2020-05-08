 package com.sjsu.bikelet.service;
 
 import com.sjsu.bikelet.domain.BikeLetUser;
 import com.sjsu.bikelet.domain.UserRole;
 import com.sjsu.bikelet.model.BikeletUserPrinciple;
 import org.apache.commons.lang3.StringUtils;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.dao.EmptyResultDataAccessException;
 import org.springframework.security.authentication.BadCredentialsException;
 import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
 import org.springframework.security.authentication.dao.AbstractUserDetailsAuthenticationProvider;
 import org.springframework.security.core.AuthenticationException;
 import org.springframework.security.core.GrantedAuthority;
 import org.springframework.security.core.authority.SimpleGrantedAuthority;
 import org.springframework.security.core.userdetails.UserDetails;
 
 import javax.persistence.EntityNotFoundException;
 import javax.persistence.NonUniqueResultException;
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  * Created with IntelliJ IDEA.
  * User: ckempaiah
  * Date: 2/9/13
  * Time: 2:13 PM
  * To change this template use File | Settings | File Templates.
  */
 public class BikeletAuthenticationProvider extends AbstractUserDetailsAuthenticationProvider {
 
     @Autowired
     BikeLetUserService userService;
     @Autowired
     UserRoleService userRoleService;
 
     @Override
     protected void additionalAuthenticationChecks(UserDetails userDetails, UsernamePasswordAuthenticationToken authenticationToken) throws AuthenticationException {
 
         //To change body of implemented methods use File | Settings | File Templates.
     }
 
     @Override
     protected UserDetails retrieveUser(String userName, UsernamePasswordAuthenticationToken authenticationToken) throws AuthenticationException {
         BikeLetUser user = null;
         Long tenantId = null;
         Long programId = null;
         String password = (String) authenticationToken.getCredentials();
         if (StringUtils.isBlank(password)) {
             throw new BadCredentialsException("Please enter password");
         }
         List<GrantedAuthority> authorities = new ArrayList<GrantedAuthority>();
         try {
             user = userService.findBikeLetUser(userName, password);
 
             if (user == null) {
                 throw new BadCredentialsException("Invalid username or password");
             }
             UserRole role = userRoleService.findUserRoleByUserId(user.getId());
 
             String roleName = role.getRoleId().getRoleName();
             if (StringUtils.isNotBlank(roleName)){
                 authorities.add(new SimpleGrantedAuthority(roleName));
             } else {
                 authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
             }
 
             if (user.getTenantId() != null) {
             	tenantId = user.getTenantId().getId();
             }
             if (user.getProgramId() != null) {
            	programId = user.getProgramId().getId();
             }
         } catch (EmptyResultDataAccessException e) {
             throw new BadCredentialsException("Invalid username or password");
         } catch (EntityNotFoundException e) {
             throw new BadCredentialsException("Invalid user");
         } catch (NonUniqueResultException e) {
             throw new BadCredentialsException("Non-unique user, contact administrator");
         }
 
         return new BikeletUserPrinciple(user.getId(), userName, password, true, // enabled
                 true, // account not expired
                 true, // credentials not expired
                 true, // account not locked
                 authorities,
                 tenantId,
                 programId);
     }
 }
