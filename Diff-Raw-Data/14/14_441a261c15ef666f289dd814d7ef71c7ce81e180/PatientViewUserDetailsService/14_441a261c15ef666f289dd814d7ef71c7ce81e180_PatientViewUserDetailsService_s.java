 /*
  * PatientView
  *
  * Copyright (c) Worth Solutions Limited 2004-2013
  *
  * This file is part of PatientView.
  *
  * PatientView is free software: you can redistribute it and/or modify it under the terms of the
  * GNU General Public License as published by the Free Software Foundation, either version 3 of the License,
  * or (at your option) any later version.
  * PatientView is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even
  * the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
  * GNU General Public License for more details.
  * You should have received a copy of the GNU General Public License along with PatientView in a file
  * titled COPYING. If not, see <http://www.gnu.org/licenses/>.
  *
  * @package PatientView
  * @link http://www.patientview.org
  * @author PatientView <info@patientview.org>
  * @copyright Copyright (c) 2004-2013, Worth Solutions Limited
  * @license http://www.gnu.org/licenses/gpl-3.0.html The GNU General Public License V3.0
  */
 
 package org.patientview.security.impl;
 
 import org.patientview.patientview.model.SpecialtyUserRole;
 import org.patientview.security.model.SecurityUser;
 import org.patientview.service.SecurityUserManager;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.security.core.GrantedAuthority;
 import org.springframework.security.core.authority.AuthorityUtils;
 import org.springframework.security.core.userdetails.UserDetails;
 import org.springframework.security.core.userdetails.UserDetailsService;
 import org.springframework.transaction.annotation.Propagation;
 import org.springframework.transaction.annotation.Transactional;
 
 import javax.inject.Inject;
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  * Grab the user from the database
  */
 @Transactional(propagation = Propagation.REQUIRED)
 public class PatientViewUserDetailsService implements UserDetailsService {
 
     private static final Logger LOGGER = LoggerFactory.getLogger(PatientViewUserDetailsService.class);
 
     @Inject
     private SecurityUserManager securityUserManager;
 
     @Override
     public UserDetails loadUserByUsername(String s) {
 
        LOGGER.debug("Attempting to load user for name {}", s);
         SecurityUser securityUser = null;
         org.patientview.patientview.model.User user = securityUserManager.get(s);
 
         if (user != null) {
 
             List<SpecialtyUserRole> specialtyUserRoles = securityUserManager.getSpecialtyUserRoles(user);
 
             if (specialtyUserRoles != null && specialtyUserRoles.size() > 0) {
 
                 List<String> roles = new ArrayList<String>();
 
                 for (SpecialtyUserRole specialtyUserRole : specialtyUserRoles) {
 
                     // convert to spring security convention
                     String role = "ROLE_" + (specialtyUserRole.getSpecialty().getContext() + "_"
                             + specialtyUserRole.getRole()).toUpperCase();
                     roles.add(role);
                 }
 
                 // add in a place order role so that we can bounce users to the login page when no Specialty is set
                 roles.add("ROLE_ANY_USER");
 
                 List<GrantedAuthority> authorities
                         = AuthorityUtils.createAuthorityList(roles.toArray(new String[roles.size()]));
 
                 // we inform spring security if the user has been locked out due to failed login attempts here
                 securityUser = new SecurityUser(
                         user.getUsername(),
                         user.getPassword(),
                         true, // enabled
                         true, // account not expired
                         true, // credentials not expired
                         !user.isAccountlocked(), // true - account not locked
                         authorities);
             } else {
                LOGGER.debug("Unable to find roles for user to attempt login from database: {}", s);
             }
         } else {
            LOGGER.debug("Unable to find user to attempt login from database: {}", s);
         }
 
         return securityUser;
     }
 }
