 /**
  * Copyright (C) [2013] [The FURTHeR Project]
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *         http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package edu.utah.further.security.impl.service;
 
 import static org.hamcrest.Matchers.is;
 import static org.hamcrest.Matchers.notNullValue;
 import static org.junit.Assert.assertThat;
 
 import java.util.Collection;
 import java.util.Date;
 import java.util.Map;
 
 import org.junit.Before;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.security.core.userdetails.UsernameNotFoundException;
 import org.springframework.test.context.ContextConfiguration;
 import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
 import org.springframework.transaction.annotation.Transactional;
 
 import edu.utah.further.core.api.collections.CollectionUtil;
 import edu.utah.further.core.api.data.Dao;
 import edu.utah.further.core.api.scope.NamespaceService;
 import edu.utah.further.core.api.scope.Namespaces;
 import edu.utah.further.security.api.EnhancedUserDetails;
 import edu.utah.further.security.api.authentication.FederatedAuthenticationToken;
 import edu.utah.further.security.api.domain.Property;
 import edu.utah.further.security.api.domain.Role;
 import edu.utah.further.security.api.domain.User;
 import edu.utah.further.security.api.domain.UserProperty;
 import edu.utah.further.security.api.domain.UserRole;
 import edu.utah.further.security.api.services.ContextualAuthenticationUserDetailsService;
 import edu.utah.further.security.impl.domain.PropertyEntity;
 import edu.utah.further.security.impl.domain.RoleEntity;
 import edu.utah.further.security.impl.domain.UserEntity;
 import edu.utah.further.security.impl.domain.UserPropertyEntity;
 import edu.utah.further.security.impl.domain.UserRoleEntity;
 
 /**
  * Tests loading userdetails with a {@link FederatedAuthenticationToken} depending on the
  * datasource namespace.
  * <p>
  * -----------------------------------------------------------------------------------<br>
  * (c) 2008-2013 FURTHeR Project, Health Sciences IT, University of Utah<br>
  * Contact: {@code <further@utah.edu>}<br>
  * Biomedical Informatics, 26 South 2000 East<br>
  * Room 5775 HSEB, Salt Lake City, UT 84112<br>
  * Day Phone: 1-801-213-3288<br>
  * -----------------------------------------------------------------------------------
  * 
  * @author N. Dustin Schultz {@code <dustin.schultz@utah.edu>}
  * @version May 6, 2012
  */
 @RunWith(SpringJUnit4ClassRunner.class)
 @ContextConfiguration(locations =
 { "/security-impl-test-context-annotation.xml",
 		"/security-impl-test-context-datasource.xml" })
 public class UTestDatasourceAuthenticationUserDetailsService
 {
 	/**
 	 * A user details service that loads users depending on the datasource namespace.
 	 */
 	@Autowired
 	private ContextualAuthenticationUserDetailsService<Integer> userDetailsService;
 
 	/**
 	 * Data access object for saving a user for testing
 	 */
 	@Autowired
 	private Dao dao;
 
 	@Autowired
 	private NamespaceService namespaceService;
 
 	/**
 	 * Federated username
 	 */
 	private User user;
 
 	/**
 	 * Setup a user to authentication against.
 	 */
 	@Before
 	@Transactional
 	public void setup()
 	{
 		user = new UserEntity();
 		user.setFirstname("Dustin");
 		user.setLastname("Schultz");
 		user.setCreatedBy(user);
 		user.setCreatedDate(new Date());
 		user.setEmail("awesome@awesome.com");
 		user.setExpireDate(new Date());
 
 		final Property furtherProp = new PropertyEntity();
 		furtherProp.setName("unid");
 		furtherProp.setNamespace(new Long(namespaceService
 				.getNamespaceId(Namespaces.FURTHER)));
 		furtherProp.setDescription("University of Utah Network Id");
 
 		final UserProperty furtherUserProperty = new UserPropertyEntity();
 		furtherUserProperty.setProperty(furtherProp);
 		furtherUserProperty.setPropertyValue("u0405293");
 		furtherUserProperty.setUser(user);
 
 		final Property edwProp = new PropertyEntity();
 		edwProp.setName("username");
 		edwProp.setNamespace(new Long(2));
 		edwProp.setDescription("Some specific property not in the default namespace");
 
 		final UserProperty edwUserProperty = new UserPropertyEntity();
 		edwUserProperty.setProperty(edwProp);
 		edwUserProperty.setPropertyValue("ABCD");
 		edwUserProperty.setUser(user);
 
 		final Collection<UserProperty> properties = CollectionUtil.newList();
 		properties.add(furtherUserProperty);
 		properties.add(edwUserProperty);
 		user.setProperties(properties);
 
 		final Role role = new RoleEntity();
 		role.setName("further");
 		role.setDescription("FURTHeR users");
 
 		final UserRole userRole = new UserRoleEntity();
 		userRole.setCreatedDate(new Date());
 		userRole.setRole(role);
 		userRole.setUser(user);
 
 		final Collection<UserRole> roles = CollectionUtil.newList();
 		roles.add(userRole);
 		user.setRoles(roles);
 	}
 
 	/**
 	 * Loads user details with the default namespace of FURTHeR
 	 */
 	@Test
 	@Transactional
 	public void loadUserDetailsDefault()
 	{
 		dao.deleteAll(UserEntity.class);
 		final Long federatedUsername = dao.save(user);
 		dao.flush();
 		
 		final EnhancedUserDetails userDetails = userDetailsService
 				.loadUserDetails(new FederatedAuthenticationToken<EnhancedUserDetails>(
 						String.valueOf(federatedUsername)));
 		assertThat(userDetails, notNullValue());
 		assertThat(userDetails.getUsername(), is(String.valueOf(federatedUsername)));
 		final Map<String, String> properties = userDetails.getProperties();
 		assertThat(new Integer(properties.size()), is(new Integer(1)));
 		assertThat(properties.get("unid"), is("u0405293"));
 	}
 
 	/**
 	 * Loads user details with the EDW APO namespace
 	 */
 	@Test
 	@Transactional
 	public void loadUserDetailsEdwApo()
 	{
 		dao.deleteAll(UserEntity.class);
 		final Long federatedUsername = dao.save(user);
 		dao.flush();
 		
 		userDetailsService.setContext(new Integer(2));
 		final EnhancedUserDetails userDetails = userDetailsService
 				.loadUserDetails(new FederatedAuthenticationToken<EnhancedUserDetails>(
 						String.valueOf(federatedUsername)));
 		assertThat(userDetails, notNullValue());
 		assertThat(userDetails.getUsername(), is(String.valueOf(federatedUsername)));
 		final Map<String, String> properties = userDetails.getProperties();
 		assertThat(new Integer(properties.size()), is(new Integer(1)));
 		assertThat(properties.get("username"), is("ABCD"));
 	}
 
 	/**
 	 * Test loading a user that does not exist.
 	 */
 	@Test(expected = UsernameNotFoundException.class)
 	public void loadUserDetailsNotFound()
 	{
 		userDetailsService
 				.loadUserDetails(new FederatedAuthenticationToken<EnhancedUserDetails>(
 						"12345"));
 	}
 }
