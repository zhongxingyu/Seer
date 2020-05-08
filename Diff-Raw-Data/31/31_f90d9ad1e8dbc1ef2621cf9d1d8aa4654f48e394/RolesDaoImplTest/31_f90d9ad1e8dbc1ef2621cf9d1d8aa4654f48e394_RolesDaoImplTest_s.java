 package org.carlspring.strongbox.dao.rdbms.impl;
 
 import org.carlspring.strongbox.dao.rdbms.RolesDao;
 import org.carlspring.strongbox.jaas.Role;
 
 import java.sql.SQLException;
 
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.test.context.ContextConfiguration;
 import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
 import static org.junit.Assert.*;
 
 /**
  * @author mtodorov
  */
 @RunWith(SpringJUnit4ClassRunner.class)
 @ContextConfiguration(locations={"/META-INF/spring/strongbox-*-context.xml", "classpath*:/META-INF/spring/strongbox-*-context.xml"})
 public class RolesDaoImplTest
 {
 
     public static final String ROLE_NAME = "Read";
 
     @Autowired
     private RolesDao rolesDao;
 
 
     @Test
     public void testCreateRole()
             throws Exception
     {
         Role role = new Role();
         role.setName(ROLE_NAME);
         role.setDescription("An observation role");
 
         final long countOld = rolesDao.count();
 
         rolesDao.createRole(role);
 
         final long countNew = rolesDao.count();
 
         assertTrue("Failed to create role '" + role.getName() + "'!", countOld < countNew);
 
         Role createdRole = rolesDao.findRole(ROLE_NAME);
 
         assertNotNull("Failed to lookup role!", createdRole);
         assertTrue("Failed to lookup role!", createdRole.getRoleId() > 0);
 
         System.out.println("roleid: " + createdRole.getRoleId());
    }
 
    @Test
    public void testUpdateRole()
            throws SQLException
    {
        final Role role = rolesDao.findRole(ROLE_NAME);
         final String description = "Permission to read objects";
 
         role.setDescription(description);
 
         rolesDao.updateRole(role);
 
         final Role updatedRole = rolesDao.findRole(ROLE_NAME);
 
         assertEquals("Failed to update the role!", description, updatedRole.getDescription());
    }
 
    @Test
    public void testDeleteRole()
            throws SQLException
    {
        final Role role = rolesDao.findRole(ROLE_NAME);
        rolesDao.removeRoleById(role.getRoleId());
    }

    @Test
    public void testCount()
            throws Exception
    {
         final long count = rolesDao.count();
 
         assertTrue("Failed to get the number of available roles!", count > 0);
 
         System.out.println("Number of roles in database: " + count);
     }
 
 }
