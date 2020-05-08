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
 
 import static org.hamcrest.MatcherAssert.assertThat;
 import static org.hamcrest.Matchers.hasSize;
 import static org.hamcrest.Matchers.is;
 import static org.hamcrest.Matchers.notNullValue;
 import static org.mockito.Mockito.verify;
 import static org.mockito.Mockito.when;
 
 import java.util.List;
 
 import nl.surfnet.bod.domain.BodRole;
 import nl.surfnet.bod.domain.Institute;
 import nl.surfnet.bod.domain.PhysicalResourceGroup;
 import nl.surfnet.bod.domain.UserGroup;
 import nl.surfnet.bod.domain.VirtualResourceGroup;
 import nl.surfnet.bod.service.GroupService;
 import nl.surfnet.bod.service.PhysicalResourceGroupService;
 import nl.surfnet.bod.service.VirtualResourceGroupService;
 import nl.surfnet.bod.support.InstituteFactory;
 import nl.surfnet.bod.support.PhysicalResourceGroupFactory;
 import nl.surfnet.bod.support.RichUserDetailsFactory;
 import nl.surfnet.bod.support.UserGroupFactory;
 import nl.surfnet.bod.support.VirtualResourceGroupFactory;
 
 import org.junit.Before;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.mockito.InjectMocks;
 import org.mockito.Mock;
 import org.mockito.runners.MockitoJUnitRunner;
 import org.springframework.security.core.Authentication;
 import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;
 
 import com.google.common.collect.ImmutableList;
 import com.google.common.collect.Lists;
 
 @RunWith(MockitoJUnitRunner.class)
 public class RichUserDetailsServiceTest {
 
   @InjectMocks
   private RichUserDetailsService subject;
   @Mock
   private GroupService groupServiceMock;
   @Mock
   private PhysicalResourceGroupService prgServiceMock;
   @Mock
   private VirtualResourceGroupService vrgServiceMock;
 
   @Before
   public void init() {
     subject.setNocEngineerGroupId("urn:noc-engineer");
   }
 
   @Test
   public void aNobody() {
     RichUserDetails userDetails = subject.loadUserDetails(new PreAuthenticatedAuthenticationToken(new RichPrincipal(
         "urn:alanvdam", "Alan van Dam", "alan@test.com"), "N/A"));
 
     assertThat(userDetails.getNameId(), is("urn:alanvdam"));
     assertThat(userDetails.getDisplayName(), is("Alan van Dam"));
     assertThat(userDetails.getAuthorities(), hasSize(0));
   }
 
   @Test
   public void aNormalUser() {
     ImmutableList<UserGroup> userGroups = listOf(new UserGroupFactory().setId("urn:klimaat-onderzoekers").create());
     when(groupServiceMock.getGroups("urn:alanvdam")).thenReturn(userGroups);
     when(vrgServiceMock.findByUserGroups(userGroups)).thenReturn(listOf(new VirtualResourceGroupFactory().create()));
 
     RichUserDetails userDetails = subject.loadUserDetails(new PreAuthenticatedAuthenticationToken(new RichPrincipal(
         "urn:alanvdam", "Alan van Dam", "alan@test.com"), "N/A"));
 
     assertThat(userDetails.getNameId(), is("urn:alanvdam"));
     assertThat(userDetails.getDisplayName(), is("Alan van Dam"));
     assertThat(userDetails.getAuthorities(), hasSize(1));
     assertThat(userDetails.getAuthorities().iterator().next().getAuthority(), is(Security.RoleEnum.USER.name()));
   }
 
   @Test
   public void aNocEngineer() {
     when(groupServiceMock.getGroups("urn:alanvdam")).thenReturn(
         listOf(new UserGroupFactory().setId("urn:noc-engineer").create()));
 
     RichUserDetails userDetails = subject.loadUserDetails(createToken("urn:alanvdam"));
 
     assertThat(userDetails.getNameId(), is("urn:alanvdam"));
     assertThat(userDetails.getDisplayName(), is("Alan van Dam"));
     assertThat(userDetails.getAuthorities(), hasSize(1));
     assertThat(userDetails.getAuthorities().iterator().next().getAuthority(), is(Security.RoleEnum.NOC_ENGINEER.name()));
   }
 
   @Test
   public void aIctManager() {
     ImmutableList<UserGroup> adminGroups = listOf(new UserGroupFactory().setId("urn:ict-manager").create());
     when(groupServiceMock.getGroups("urn:alanvdam")).thenReturn(adminGroups);
     when(prgServiceMock.findAllForAdminGroups(adminGroups)).thenReturn(
         listOf(new PhysicalResourceGroupFactory().create()));
 
     RichUserDetails userDetails = subject.loadUserDetails(createToken("urn:alanvdam"));
 
     assertThat(userDetails.getAuthorities(), hasSize(1));
     assertThat(userDetails.getAuthorities().iterator().next().getAuthority(), is(Security.RoleEnum.ICT_MANAGER.name()));
   }
 
   @Test
   public void shouldUpdateVirtualResourceGroupsIfChangedInSurfconext() {
     ImmutableList<UserGroup> userGroups = listOf(new UserGroupFactory().setId("urn:nameGroup").setName("new name")
         .create(), new UserGroupFactory().setId("urn:descGroup").setDescription("new desc").create());
 
     VirtualResourceGroup vrgNewName = new VirtualResourceGroupFactory().setName("old name").create();
     VirtualResourceGroup vrgNewDesc = new VirtualResourceGroupFactory().setDescription("old desc").create();
 
     when(groupServiceMock.getGroups("urn:alanvdam")).thenReturn(userGroups);
     when(vrgServiceMock.findBySurfconextGroupId("urn:nameGroup")).thenReturn(vrgNewName);
     when(vrgServiceMock.findBySurfconextGroupId("urn:descGroup")).thenReturn(vrgNewDesc);
 
     subject.loadUserDetails(createToken("urn:alanvdam"));
 
     assertThat(vrgNewName.getName(), is("new name"));
     assertThat(vrgNewDesc.getDescription(), is("new desc"));
     verify(vrgServiceMock).update(vrgNewDesc);
     verify(vrgServiceMock).update(vrgNewName);
   }
 
   @Test
   public void shouldAddRole() {
     UserGroup userGroup = new UserGroupFactory().setId("urn:nameGroup").setName("new name").create();
 
     PhysicalResourceGroup prg = new PhysicalResourceGroupFactory().create();
 
     when(groupServiceMock.getGroups("urn:alanvdam")).thenReturn(listOf(userGroup));
     when(prgServiceMock.findByAdminGroup(userGroup.getId())).thenReturn(listOf(prg));
 
     RichUserDetails userDetails = subject.loadUserDetails(new PreAuthenticatedAuthenticationToken(new RichPrincipal(
         "urn:alanvdam", "Alan van Dam", "alan@test.com"), "N/A"));
 
     assertThat(userDetails.getSelectedRole(), notNullValue());
     assertThat(userDetails.getBodRoles(), hasSize(0));
   }
 
   @Test
   public void shouldSetDefaultSelectedRole() {
     UserGroup userGroup = new UserGroupFactory().setId("urn:nameGroup").setName("new name").create();
 
     PhysicalResourceGroup prg = new PhysicalResourceGroupFactory().create();
 
     when(groupServiceMock.getGroups("urn:alanvdam")).thenReturn(listOf(userGroup));
     when(prgServiceMock.findByAdminGroup(userGroup.getId())).thenReturn(listOf(prg));
 
     RichUserDetails userDetails = subject.loadUserDetails(new PreAuthenticatedAuthenticationToken(new RichPrincipal(
         "urn:alanvdam", "Alan van Dam", "alan@test.com"), "N/A"));
 
     assertThat(userDetails.getSelectedRole(), notNullValue());
 
   }
 
   @Test
   public void shouldContainUserGroupData() {
     UserGroup userGroup = new UserGroupFactory().setId("urn:nameGroup").setName("new name").create();
     RichUserDetails userDetails = new RichUserDetailsFactory().addUserGroup(userGroup).create();
 
     Institute institute = new InstituteFactory().create();
     PhysicalResourceGroup prg = new PhysicalResourceGroupFactory().setInstitute(institute).create();
 
     when(groupServiceMock.getGroups("urn:alanvdam")).thenReturn(listOf(userGroup));
     when(prgServiceMock.findByAdminGroup(userGroup.getId())).thenReturn(listOf(prg));
     // Force role Manager
     when(prgServiceMock.findAllForAdminGroups(Lists.newArrayList(userGroup))).thenReturn(Lists.newArrayList(prg));
 
     List<BodRole> roles = subject.determineRoles(userDetails.getUserGroups());
 
     assertThat(roles, hasSize(1));
     BodRole bodRole = roles.get(0);
 
     assertThat(bodRole.getGroupId(), is(userGroup.getId()));
     assertThat(bodRole.getGroupName(), is(userGroup.getName()));
     assertThat(bodRole.getGroupDescription(), is(userGroup.getDescription()));
     assertThat(bodRole.getInstituteId(), is(institute.getId()));
     assertThat(bodRole.getInstituteName(), is(institute.getName()));
     assertThat(bodRole.getRoleName(), is(Security.RoleEnum.ICT_MANAGER.name()));
   }
 
   private static <E> ImmutableList<E> listOf(E... elements) {
     return ImmutableList.copyOf(elements);
   }
 
   private static Authentication createToken(String nameId) {
     return new PreAuthenticatedAuthenticationToken(new RichPrincipal(nameId, "Alan van Dam", "alan@test.com"), "N/A");
   }
 }
