 /**
  * The owner of the original code is SURFnet BV.
  *
  * Portions created by the original owner are Copyright (C) 2011-2012 the
  * original owner. All Rights Reserved.
  *
  * Portions created by other contributors are Copyright (C) the contributor.
  * All Rights Reserved.
  *
  * Contributor(s):
  *   (Contributors insert name & email here)
  *
  * This file is part of the SURFnet7 Bandwidth on Demand software.
  *
  * The SURFnet7 Bandwidth on Demand software is free software: you can
  * redistribute it and/or modify it under the terms of the BSD license
  * included with this distribution.
  *
  * If the BSD license cannot be found with this distribution, it is available
  * at the following location <http://www.opensource.org/licenses/BSD-3-Clause>
  */
 package nl.surfnet.bod.web.security;
 
 import java.util.Collection;
 import java.util.List;
 import java.util.Set;
 
 import nl.surfnet.bod.domain.BodRole;
 import nl.surfnet.bod.domain.PhysicalResourceGroup;
 import nl.surfnet.bod.domain.UserGroup;
 import nl.surfnet.bod.domain.VirtualResourceGroup;
 import nl.surfnet.bod.service.GroupService;
 import nl.surfnet.bod.service.PhysicalResourceGroupService;
 import nl.surfnet.bod.service.VirtualResourceGroupService;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.beans.factory.annotation.Value;
 import org.springframework.security.core.Authentication;
 import org.springframework.security.core.GrantedAuthority;
 import org.springframework.security.core.authority.GrantedAuthorityImpl;
 import org.springframework.security.core.userdetails.AuthenticationUserDetailsService;
 import org.springframework.security.core.userdetails.UsernameNotFoundException;
 import org.springframework.util.CollectionUtils;
 
 import com.google.common.base.Function;
 import com.google.common.base.Predicate;
 import com.google.common.collect.Iterables;
 import com.google.common.collect.Lists;
 import com.google.common.collect.Ordering;
 import com.google.common.collect.Sets;
 
 public class RichUserDetailsService implements AuthenticationUserDetailsService {
 
   private final Logger logger = LoggerFactory.getLogger(RichUserDetailsService.class);
 
   private final static Ordering<BodRole> BY_ROLE_NAME = Ordering.natural().onResultOf(new Function<BodRole, String>() {
     public String apply(BodRole role) {
       return role.getRoleName();
     }
   });
 
   @Value("${os.group.noc}")
   private String nocEngineerGroupId;
 
   @Autowired
   private GroupService groupService;
 
   @Autowired
   private PhysicalResourceGroupService physicalResourceGroupService;
 
   @Autowired
   private VirtualResourceGroupService virtualResourceGroupService;
 
   @Override
   public RichUserDetails loadUserDetails(Authentication token) throws UsernameNotFoundException {
     RichPrincipal principal = (RichPrincipal) token.getPrincipal();
     Collection<UserGroup> groups = groupService.getGroups(principal.getNameId());
 
     logger.debug("Found groups: '{}' for name-id: '{}'", groups, principal.getNameId());
 
     updateVirtualResourceGroups(groups);
 
     List<GrantedAuthority> authorities = getAuthorities(groups);
 
     RichUserDetails userDetails = new RichUserDetails(principal.getNameId(), principal.getDisplayName(),
         principal.getEmail(), authorities, groups);
 
     userDetails.setBodRoles(determineRoles(userDetails.getUserGroups()));
 
     userDetails.switchRoleTo(CollectionUtils.isEmpty(userDetails.getBodRoles()) ? null : userDetails.getBodRoles().get(
         0));
     return userDetails;
   }
 
   /**
    * Determines for which userGroups there is a {@link PhysicalResourceGroup}
    * related and creates a {@link BodRole} based on that information.
    * 
    * @param Collection
    *          <UserGroup> groups to process
    * @return List<BodRole> List with roles sorted on the roleName
    */
   List<BodRole> determineRoles(Collection<UserGroup> userGroups) {
 
     Set<BodRole> roles = Sets.newHashSet();
 
     for (UserGroup userGroup : userGroups) {
       roles.add(determineNocRole(userGroup));
       roles.addAll(determineManagerRole(userGroup));
       roles.add(determineUserRole(userGroup));
     }
 
     roles.remove(null);
 
     return BY_ROLE_NAME.sortedCopy(roles);
   }
 
   BodRole determineNocRole(UserGroup userGroup) {
     BodRole nocRole = null;
 
     if (isNocEngineerGroup(Lists.newArrayList(userGroup))) {
       nocRole = new BodRole(userGroup, Security.RoleEnum.NOC_ENGINEER.name());
     }
 
     return nocRole;
   }
 
   List<BodRole> determineManagerRole(UserGroup userGroup) {
     List<BodRole> managerRoles = Lists.newArrayList();
     for (PhysicalResourceGroup physicalResourceGroup : physicalResourceGroupService.findByAdminGroup(userGroup.getId())) {
 
       if (physicalResourceGroup != null) {
         if (isIctManager(Lists.newArrayList(userGroup))) {
           managerRoles.add(new BodRole(userGroup, Security.RoleEnum.ICT_MANAGER.name(), physicalResourceGroup
               .getInstitute()));
         }
       }
     }
     return managerRoles;
   }
 
  private BodRole determineUserRole(UserGroup userGroup) {
     BodRole userRole = null;
 
     if (isUser(Lists.newArrayList(userGroup))) {
       userRole = new BodRole(userGroup, Security.RoleEnum.USER.name());
     }
 
     return userRole;
   }
 
   private List<GrantedAuthority> getAuthorities(Collection<UserGroup> groups) {
     List<GrantedAuthority> authorities = Lists.newArrayList();
     if (isNocEngineerGroup(groups)) {
       authorities.add(createAuthority(Security.RoleEnum.NOC_ENGINEER.name()));
     }
     if (isIctManager(groups)) {
       authorities.add(createAuthority(Security.RoleEnum.ICT_MANAGER.name()));
     }
     if (isUser(groups)) {
       authorities.add(createAuthority(Security.RoleEnum.USER.name()));
     }
     return authorities;
   }
 
   private void updateVirtualResourceGroups(Collection<UserGroup> userGroups) {
     for (UserGroup userGroup : userGroups) {
       VirtualResourceGroup vrg = virtualResourceGroupService.findBySurfconextGroupId(userGroup.getId());
 
       if (vrg == null) {
         continue;
       }
 
       if (!vrg.getName().equals(userGroup.getName()) || !vrg.getDescription().equals(userGroup.getDescription())) {
         vrg.setDescription(userGroup.getDescription());
         vrg.setName(userGroup.getName());
 
         virtualResourceGroupService.update(vrg);
       }
     }
   }
 
   private boolean isIctManager(Collection<UserGroup> groups) {
     return !physicalResourceGroupService.findAllForAdminGroups(groups).isEmpty();
   }
 
   public boolean isUser(Collection<UserGroup> groups) {
     return !virtualResourceGroupService.findByUserGroups(groups).isEmpty();
   }
 
   private GrantedAuthority createAuthority(String role) {
     return new GrantedAuthorityImpl(role);
   }
 
   private boolean isNocEngineerGroup(Collection<UserGroup> groups) {
     return Iterables.any(groups, new Predicate<UserGroup>() {
       @Override
       public boolean apply(UserGroup group) {
         return nocEngineerGroupId.equals(group.getId());
       }
     });
   }
 
   protected void setNocEngineerGroupId(String groupId) {
     this.nocEngineerGroupId = groupId;
   }
 }
