 /*
 * JBoss, a division of Red Hat
 * Copyright 2006, Red Hat Middleware, LLC, and individual contributors as indicated
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
 
 package org.picketlink.idm.impl.api;
 
 import org.picketlink.idm.api.IdentitySearchCriteria;
 import org.picketlink.idm.api.IdentitySession;
 import org.picketlink.idm.api.Group;
 import org.picketlink.idm.api.SortOrder;
 import org.picketlink.idm.api.User;
 import org.picketlink.idm.api.RoleType;
 import org.picketlink.idm.api.IdentitySessionFactory;
 import org.picketlink.idm.api.Role;
 
 import junit.framework.Assert;
 
 import java.util.Map;
 import java.util.HashMap;
 
 /**
  *
  * @author <a href="mailto:boleslaw.dawidowicz at redhat.com">Boleslaw Dawidowicz</a>
  * @version : 0.1 $
  */
 public class RoleManagerTest extends Assert
 {
    //TODO:
    //TODO: - exception handling
    //TODO: - search criteria
    //TODO: - features description
 
    APITestContext ctx;
 
    private String ORGANIZATION = "ORGANIZATION";
    private String ORGANIZATION_UNIT = "ORGANIZATION_UNIT";
    private String DIVISION = "DIVISION";
    private String DEPARTMENT = "DEPARTMENT";
    private String PROJECT = "PROJECT";
    private String PEOPLE = "PEOPLE";
 
    public RoleManagerTest(APITestContext ctx)
    {
       this.ctx = ctx;
    }
 
    public void setCtx(APITestContext ctx)
    {
       this.ctx = ctx;
    }
 
    public APITestContext getCtx()
    {
       return ctx;
    }
 
    public void testMethods(String realmName) throws Exception
    {
       IdentitySessionFactory factory = ctx.getIdentitySessionFactory();
 
       IdentitySession session = factory.createIdentitySession(realmName);
 
       ctx.begin();
 
 
 
       // RoleTypes - pure
       assertEquals(0, session.getRoleManager().findRoleTypes(null).size());
 
       // Create / remove
       RoleType rt1 = session.getRoleManager().createRoleType("rt1");
       RoleType rt2 = session.getRoleManager().createRoleType("rt2");
       RoleType rt3 = session.getRoleManager().createRoleType("rt3");
       RoleType rt4 = session.getRoleManager().createRoleType("rt4");
 
       assertEquals(4, session.getRoleManager().findRoleTypes(null).size());
 
       session.getRoleManager().removeRoleType(rt1);
       session.getRoleManager().removeRoleType(rt2);
 
       assertEquals(2, session.getRoleManager().findRoleTypes(null).size());
 
       session.getRoleManager().removeRoleType("rt3");
       session.getRoleManager().removeRoleType("rt4");
 
       assertEquals(0, session.getRoleManager().findRoleTypes(null).size());
 
       // Create all entities for tests
 
       User user1 = session.getPersistenceManager().createUser("user1");
       User user2 = session.getPersistenceManager().createUser("user2");
       User user3 = session.getPersistenceManager().createUser("user3");
       User user4 = session.getPersistenceManager().createUser("user4");
 
       Group group1 = session.getPersistenceManager().createGroup("group1", ORGANIZATION);
       Group group2 = session.getPersistenceManager().createGroup("group2", ORGANIZATION_UNIT);
       Group group3 = session.getPersistenceManager().createGroup("group3", ORGANIZATION_UNIT);
       Group group4 = session.getPersistenceManager().createGroup("group4", DEPARTMENT);
       Group group5 = session.getPersistenceManager().createGroup("group5", DEPARTMENT);
 
       rt1 = session.getRoleManager().createRoleType("rt1");
       rt2 = session.getRoleManager().createRoleType("rt2");
       rt3 = session.getRoleManager().createRoleType("rt3");
       rt4 = session.getRoleManager().createRoleType("rt4");
 
       assertEquals(4, session.getRoleManager().findRoleTypes(null).size());
 
 
 
 
       // Create/remove and find roles
 
       Role role1 = session.getRoleManager().createRole(rt1, user1, group1);
       Role role2 = session.getRoleManager().createRole(rt1, user1, group2);
       Role role3 = session.getRoleManager().createRole("rt2", user1.getKey(), group1.getKey());
 
 
 
 
       //
 
       assertEquals(2, session.getRoleManager().findRoles(user1, rt1).size());
       assertEquals(1, session.getRoleManager().findRoles(user1, rt2).size());
       assertEquals(1, session.getRoleManager().findRoles(group1, rt1).size());
       assertEquals(1, session.getRoleManager().findRoles(group1, rt2).size());
 
       //
 
       assertEquals(2, session.getRoleManager().findGroupRoleTypes(group1).size());
       assertEquals(1, session.getRoleManager().findGroupRoleTypes(group2.getKey(), null).size());
 
       //
 
       assertEquals(2, session.getRoleManager().findRoles(user1, rt1).size());
       assertEquals(1, session.getRoleManager().findRoles(user1.getKey(), "rt2").size());
       assertEquals(1, session.getRoleManager().findRoles(group1.getKey(), "rt1").size());
       assertEquals(1, session.getRoleManager().findRoles(group1, rt1).size());
 
       // Some testing with search criteria and null roleType
 
      IdentitySearchCriteria crit = new IdentitySearchCriteriaImpl().page(0, 2);
       crit.sort(SortOrder.ASCENDING);
      assertEquals(3, session.getRoleManager().findRoles(user1, null, null).size());
      assertEquals(2, session.getRoleManager().findRoles(user1, null, crit).size());
      assertEquals(2, session.getRoleManager().findRoles(group1, null, null).size());
       assertEquals(2, session.getRoleManager().findRoles(group1, null, crit).size());
       assertEquals(1, session.getRoleManager().findRoles(group2, null, crit).size());
 
       //
 
       assertEquals(2, session.getRoleManager().findGroupsWithRelatedRole(user1, null).size());
       assertEquals(1, session.getRoleManager().findGroupsWithRelatedRole(user1.getKey(), ORGANIZATION, null).size());
 
       //
 
       assertEquals(2, session.getRoleManager().findRoleTypes(user1, group1).size());
       assertEquals(1, session.getRoleManager().findRoleTypes(user1.getKey(), group2.getKey(), null).size());
 
       //
 
       assertEquals(2, session.getRoleManager().findUserRoleTypes(user1).size());
       assertEquals(0, session.getRoleManager().findUserRoleTypes(user2.getKey(), null).size());
 
       //
       assertTrue(session.getRoleManager().hasRole(user1, group1, rt1));
       assertTrue(session.getRoleManager().hasRole(user1, group1, rt2));
       assertTrue(session.getRoleManager().hasRole("user1", group2.getKey(), "rt1"));
       assertFalse(session.getRoleManager().hasRole("user2", group2.getKey(), "rt1"));
       assertFalse(session.getRoleManager().hasRole(user1, group1, rt3));
       assertFalse(session.getRoleManager().hasRole(user1, group4, rt3));
 
       //
       assertEquals(2, session.getRoleManager().getRolesCount(group1, null, null));
       assertEquals(1, session.getRoleManager().getRolesCount(group2, null, null));
 
       // Role properties
 
       assertEquals(0, session.getRoleManager().getProperties(role1).keySet().size());
       assertEquals(0, session.getRoleManager().getProperties(rt1).keySet().size());
       assertEquals(0, session.getRoleManager().getProperties(rt1.getName()).keySet().size());
 
       session.getRoleManager().setProperty(role1, "roleProp1", "val1");
       session.getRoleManager().setProperty(role1, "roleProp1", "val2");
       session.getRoleManager().setProperty(role1, "roleProp2", "val1");
       session.getRoleManager().setProperty(rt1, "rtProp1", "val1");
       session.getRoleManager().setProperty(rt1.getName(), "rtProp1", "val2");
       session.getRoleManager().setProperty(rt1, "rtProp2", "val1");
       session.getRoleManager().setProperty(rt1, "rtProp3", "val1");
 
       assertEquals(2, session.getRoleManager().getProperties(role1).keySet().size());
       assertEquals(3, session.getRoleManager().getProperties(rt1).keySet().size());
       assertEquals(3, session.getRoleManager().getProperties(rt1.getName()).keySet().size());
 
       assertEquals("val2", session.getRoleManager().getProperties(role1).get("roleProp1"));
       assertEquals("val1", session.getRoleManager().getProperties(role1).get("roleProp2"));
       assertEquals("val2", session.getRoleManager().getProperties(rt1).get("rtProp1"));
       assertEquals("val1", session.getRoleManager().getProperties(rt1).get("rtProp2"));
       assertEquals("val1", session.getRoleManager().getProperties(rt1).get("rtProp3"));
 
 
       Map<String, String> props = new HashMap<String, String>();
       props.put("prop5", "val1");
 
       session.getRoleManager().setProperties(role2, props);
 
       assertEquals(1, session.getRoleManager().getProperties(role2).keySet().size());
 
       assertEquals("val1", session.getRoleManager().getProperties(role2).get("prop5"));
 
 
       props.put("prop6", "val3");
       session.getRoleManager().setProperties(rt2, props);
       session.getRoleManager().setProperties(rt3.getName(), props);
 
       assertEquals(2, session.getRoleManager().getProperties(rt2).keySet().size());
 
       assertEquals("val3", session.getRoleManager().getProperties(rt2).get("prop6"));
       assertEquals("val3", session.getRoleManager().getProperties(rt3).get("prop6"));
 
 
       session.getRoleManager().removeProperty(role2, "prop5");
       assertEquals(0, session.getRoleManager().getProperties(role2).keySet().size());
 
       session.getRoleManager().removeProperty(rt2, "prop6");
       assertEquals(1, session.getRoleManager().getProperties(rt2).keySet().size());
       session.getRoleManager().removeProperty(rt2, "prop5");
       assertEquals(0, session.getRoleManager().getProperties(rt2).keySet().size());
 
       
       // Remove roles
 
       session.getRoleManager().removeRole(rt1, user1, group1);
       session.getRoleManager().removeRole(role2);
       session.getRoleManager().removeRole("rt2", user1.getKey(), group1.getKey());
 
       assertFalse(session.getRoleManager().hasRole(user1, group1, rt1));
       assertFalse(session.getRoleManager().hasRole(user1, group1, rt2));
       assertFalse(session.getRoleManager().hasRole("user1", group2.getKey(), "rt1"));
 
       //
 
       assertEquals(0, session.getRoleManager().findRoles(user1, rt1).size());
       assertEquals(0, session.getRoleManager().findRoles(user1, rt2).size());
       assertEquals(0, session.getRoleManager().findRoles(group1, rt1).size());
       assertEquals(0, session.getRoleManager().findRoles(group1, rt2).size());
 
       //
 
       assertEquals(0, session.getRoleManager().findGroupRoleTypes(group1).size());
       assertEquals(0, session.getRoleManager().findGroupRoleTypes(group2.getKey(), null).size());
 
       //
 
       assertEquals(0, session.getRoleManager().findRoles(user1, rt1).size());
       assertEquals(0, session.getRoleManager().findRoles(user1.getKey(), "rt2").size());
       assertEquals(0, session.getRoleManager().findRoles(group1.getKey(), "rt1").size());
       assertEquals(0, session.getRoleManager().findRoles(group1, rt1).size());
 
       // Some testing with criteria and null roleType
 
       assertEquals(0, session.getRoleManager().findRoles(user1, null, crit).size());
       assertEquals(0, session.getRoleManager().findRoles(group1, null, crit).size());
       assertEquals(0, session.getRoleManager().findRoles(group2, null, crit).size());
 
       //
 
       assertEquals(0, session.getRoleManager().findGroupsWithRelatedRole(user1, null).size());
       assertEquals(0, session.getRoleManager().findGroupsWithRelatedRole(user1.getKey(), ORGANIZATION, null).size());
 
       //
 
       assertEquals(0, session.getRoleManager().findRoleTypes(user1, group1).size());
       assertEquals(0, session.getRoleManager().findRoleTypes(user1.getKey(), group2.getKey(), null).size());
 
       //
 
       assertEquals(0, session.getRoleManager().findUserRoleTypes(user1).size());
       assertEquals(0, session.getRoleManager().findUserRoleTypes(user2.getKey(), null).size());
 
 
       // Create roleType and role, remove roleType and check if role was properly removed
 
       session.getPersistenceManager().createUser("u1");
       Group g1 = session.getPersistenceManager().createGroup("g1", ORGANIZATION);
 
       //session.getRoleManager().createRoleType("rt1");
 
       session.getRoleManager().createRole("rt1", "u1", g1.getKey());
 
       assertNotNull(session.getRoleManager().getRole("rt1", "u1", g1.getKey()));
 
       session.getRoleManager().removeRoleType("rt1");
 
       assertNull(session.getRoleManager().getRole("rt1", "u1", g1.getKey()));
 
 
 
       ctx.commit();
 
    }
 
 
 
 }
