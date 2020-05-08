 /*
  jBilling - The Enterprise Open Source Billing System
  Copyright (C) 2003-2010 Enterprise jBilling Software Ltd. and Emiliano Conde
 
  This file is part of jbilling.
 
  jbilling is free software: you can redistribute it and/or modify
  it under the terms of the GNU Affero General Public License as published by
  the Free Software Foundation, either version 3 of the License, or
  (at your option) any later version.
 
  jbilling is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU Affero General Public License for more details.
 
  You should have received a copy of the GNU Affero General Public License
  along with jbilling.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package com.sapienter.jbilling.client.authentication;
 
 import com.sapienter.jbilling.server.user.UserBL;
import com.sapienter.jbilling.server.user.contact.db.ContactDAS;
import com.sapienter.jbilling.server.user.contact.db.ContactDTO;
 import com.sapienter.jbilling.server.user.db.UserDTO;
 import com.sapienter.jbilling.server.user.permisson.db.PermissionDTO;
 import com.sapienter.jbilling.server.user.permisson.db.RoleDTO;
 import grails.plugins.springsecurity.SpringSecurityService;
 import org.codehaus.groovy.grails.plugins.springsecurity.GrailsUserDetailsService;
 import org.codehaus.groovy.grails.plugins.springsecurity.SpringSecurityUtils;
 import org.springframework.dao.DataAccessException;
 import org.springframework.security.core.GrantedAuthority;
 import org.springframework.security.core.authority.GrantedAuthorityImpl;
 import org.springframework.security.core.userdetails.UserDetails;
 import org.springframework.security.core.userdetails.UsernameNotFoundException;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.List;
 import java.util.Locale;
 
 /**
  * An implementation of the GrailsUserDetailsService for use with the default DaoAuthenticationProvider. This
  * class fetches a user from the database and builds a list of granted authorities from the users assigned
  * permissions and roles.
  *
  * This must be used with the {@link CompanyUserAuthenticationFilter} to provide the company ID as part
  * of the username to load.
  *
  * @author Brian Cowdery
  * @since 04-10-2010
  */
 public class CompanyUserDetailsService implements GrailsUserDetailsService {
 
     // empty list of roles for use if the given credentials don't resolve to a
     // usable UserDetails. Contains a single entry that does not grant any permissions.
     private static final List<GrantedAuthority> NO_AUTHORITIES;
     static {
         NO_AUTHORITIES = new ArrayList<GrantedAuthority>(1);
         NO_AUTHORITIES.add(new GrantedAuthorityImpl(SpringSecurityUtils.NO_ROLE));
     }
 
     private SpringSecurityService springSecurityService;
 
     public SpringSecurityService getSpringSecurityService() {
         return springSecurityService;
     }
 
     public void setSpringSecurityService(SpringSecurityService springSecurityService) {
         this.springSecurityService = springSecurityService;
     }    
 
     public UserDetails loadUserByUsername(String s, boolean loadRoles)
             throws UsernameNotFoundException, DataAccessException {
         return loadUserByUsername(s);
     }
 
     public UserDetails loadUserByUsername(String s) throws UsernameNotFoundException, DataAccessException {
         // get the user for the given name
         // CompanyUserAuthenticationFilter concatenates the user name with the entity id
         String[] tokens = s.split(CompanyUserAuthenticationFilter.VALUE_SEPARATOR);
         String username = tokens[0];
         Integer entityId = Integer.valueOf(tokens[1]);
 
         UserBL bl = new UserBL(username, entityId);
         UserDTO user = bl.getEntity();
 
         if (user == null)
             throw new UsernameNotFoundException("User '" + s + "' not found", username);
 
         // collect granted permissions and roles
         // this is a bad use of generics, the UserDetails signature should be <? extends GrantedAuthority>
         Collection<GrantedAuthority> authorities = new ArrayList<GrantedAuthority>();
 
         for (PermissionDTO permission : bl.getPermissions()) {
             permission.initializeAuthority();
             authorities.add(permission);
         }
         
         for (RoleDTO role : user.getRoles()) {
             role.initializeAuthority();            
             authorities.add(role);
         }
 
         // return user details for the retrieved account
         return new CompanyUserDetails(user.getUserName(), user.getPassword(), user.isEnabled(),
                                       !user.isAccountExpired(), !user.isPasswordExpired(), !user.isAccountLocked(),
                                       authorities.isEmpty() ? NO_AUTHORITIES : authorities,
                                      user, getLocale(user),
                                       user.getId(), user.getEntity().getId(), user.getLanguage().getId());
     }

    private Locale getLocale(UserDTO user) {
        String languageCode = user.getLanguage().getCode();

        ContactDTO contact = new ContactDAS().findPrimaryContact(user.getId());
        String countryCode = contact.getCountryCode();

        return countryCode != null ? new Locale(languageCode, countryCode) : new Locale(languageCode);
    }
 }
